# Boot the full local stack: postgres (docker), then Eureka, then api-gateway +
# 4 BE services + 2 Next.js apps. Waits until each is healthy before returning.
#
# Strategy:
#   - Eureka must be UP before others start (otherwise their registration beans
#     can fail at startup). Other services are started in parallel after Eureka.
#   - Each background process logs to e2e/.stack-logs/<name>.log
#   - PIDs are saved to e2e/.stack-pids.json so stop-stack.ps1 can kill them.
#
# Usage:
#   cd e2e
#   ./scripts/start-stack.ps1            # default - full stack
#   ./scripts/start-stack.ps1 -SkipFE    # skip the 2 Next.js apps

[CmdletBinding()]
param(
    [switch]$SkipFE,
    # Skip ai-service. Default = ON because Spring AI 1.0.0 is incompatible
    # with Spring Boot 4 (RestClientAutoConfiguration moved). All AI-using
    # tests are gated behind E2E_OPENAI=1 so they SKIP without the service.
    [switch]$IncludeAI
)

$ErrorActionPreference = 'Stop'

$repoRoot    = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$composeFile = Join-Path $repoRoot 'docker/docker-compose.yaml'
$envFile     = Join-Path $repoRoot 'docker/.env'
$servicesDir = Join-Path $repoRoot 'services'
$feUserDir   = Join-Path $repoRoot 'frontend-user'
$feAdminDir  = Join-Path $repoRoot 'frontend-admin'
$logsDir     = Join-Path $PSScriptRoot '..\.stack-logs'
$pidsFile    = Join-Path $PSScriptRoot '..\.stack-pids.json'

if (-not (Test-Path $envFile)) {
    Write-Error "Missing $envFile. Copy docker/.env.example to docker/.env first."
}

# Load docker/.env into process env so child Spring Boot apps inherit
# JWT_SECRET, DB_*, OPENAI_API_KEY, etc. Lines like KEY=value (skip blanks/comments).
# Override hostnames that target docker network: child services run on localhost.
Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -and -not $line.StartsWith('#') -and $line -match '^([^=]+)=(.*)$') {
        $key = $matches[1].Trim()
        $val = $matches[2].Trim()
        # Rewrite the only known docker-internal hostname: postgres -> localhost (mapped 5433)
        $val = $val -replace 'jdbc:postgresql://postgres:5433/', 'jdbc:postgresql://localhost:5433/'
        $val = $val -replace '^http://python:8000', 'http://localhost:8000'
        Set-Item -Path "env:$key" -Value $val
    }
}

# Sanity defaults: each Spring service reads spring.datasource.url from a service-
# specific env var like LEARNING_DB_URL. Our docker compose sets these, but if they
# aren't in the .env they'll be undefined. The services' application.yml falls back
# to localhost-style URLs, but we set explicit overrides here for clarity.
if (-not $env:USER_DB_URL)         { $env:USER_DB_URL         = 'jdbc:postgresql://localhost:5433/user_service' }
if (-not $env:LEARNING_DB_URL)     { $env:LEARNING_DB_URL     = 'jdbc:postgresql://localhost:5433/learning_service' }
if (-not $env:NOTIFICATION_DB_URL) { $env:NOTIFICATION_DB_URL = 'jdbc:postgresql://localhost:5433/notification_service' }
if (-not $env:EUREKA_URL)          { $env:EUREKA_URL          = 'http://localhost:8761/eureka/' }
if (-not $env:SPRING_PROFILES_ACTIVE) { $env:SPRING_PROFILES_ACTIVE = 'default' }

# Force JVM timezone to the IANA name Postgres knows. Windows often reports
# "Asia/Saigon", which Postgres rejects; the IANA equivalent is "Asia/Ho_Chi_Minh".
$env:JAVA_TOOL_OPTIONS = '-Duser.timezone=Asia/Ho_Chi_Minh'
$env:TZ = 'Asia/Ho_Chi_Minh'

# P9.2 — re-enable Flyway so E2E matches production. Each service runs its own
# migrations on startup. init-dbs.ps1 only creates empty databases now; schema
# is owned by Flyway.
# $env:SPRING_FLYWAY_ENABLED = 'false'

# P9.1 — production-realistic: re-enable Hibernate schema validation
# (matches the docker compose / production setup). Comment back out only if a
# real entity-schema mismatch is discovered and can't be fixed locally.
# $env:SPRING_JPA_HIBERNATE_DDL_AUTO = 'none'

# Disable rate limiting for E2E. Production keeps the default (true) via docker.
$env:APP_RATE_LIMIT_ENABLED = 'false'

# Disable refresh-token rotation for E2E so Playwright's storageState (captured
# once during seed) remains valid across every test in the run. Production
# keeps the default true — a leaked token there buys one access token before
# the family rotates.
$env:APP_REFRESH_TOKEN_ROTATION_ENABLED = 'false'

# Disable spring-boot-devtools restart watcher. It interposes a custom classloader
# that has caused response-stream corruption (half-closed chunked responses)
# in local bootRun. Production never sees this because the jar excludes devtools.
$env:SPRING_DEVTOOLS_RESTART_ENABLED = 'false'
$env:SPRING_DEVTOOLS_LIVERELOAD_ENABLED = 'false'

# Disable virtual threads for E2E. The combo of Tomcat 11 + virtual threads +
# CorrelationIdFilter sometimes omits the trailing 0-length chunk in chunked
# responses, causing Playwright/axios to abort while reading the body even when
# the response was logically complete. Production keeps virtual threads on.
$env:SPRING_THREADS_VIRTUAL_ENABLED = 'false'

New-Item -ItemType Directory -Force -Path $logsDir | Out-Null

function Wait-ForUrl {
    param([string]$Url, [int]$TimeoutSec = 180, [string]$Name)
    Write-Host ("    waiting for {0} @ {1} ... " -f $Name, $Url) -NoNewline
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        try {
            $r = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
            if ($r.StatusCode -ge 200 -and $r.StatusCode -lt 500) {
                Write-Host "UP" -ForegroundColor Green
                return $true
            }
        } catch {
            Start-Sleep -Milliseconds 1500
        }
    }
    Write-Host "TIMEOUT" -ForegroundColor Red
    return $false
}

function Wait-ForPort {
    param([string]$HostName = 'localhost', [int]$Port, [int]$TimeoutSec = 60, [string]$Name)
    Write-Host ("    waiting for {0} @ {1}:{2} ... " -f $Name, $HostName, $Port) -NoNewline
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        try {
            $tcp = New-Object System.Net.Sockets.TcpClient
            $tcp.Connect($HostName, $Port)
            $tcp.Close()
            Write-Host "UP" -ForegroundColor Green
            return $true
        } catch {
            Start-Sleep -Milliseconds 1000
        }
    }
    Write-Host "TIMEOUT" -ForegroundColor Red
    return $false
}

function Start-BgProcess {
    param([string]$Name, [string]$WorkDir, [string]$Command, [string]$Arguments)
    $logFile = Join-Path $logsDir ("{0}.log" -f $Name)
    Write-Host ("    starting {0} (-> {1})" -f $Name, $logFile)
    $p = Start-Process -FilePath $Command -ArgumentList $Arguments `
        -WorkingDirectory $WorkDir `
        -WindowStyle Hidden `
        -RedirectStandardOutput $logFile `
        -RedirectStandardError ($logFile + '.err') `
        -PassThru
    return $p.Id
}

$pids = @{}

Write-Host "==> [1/4] Starting postgres container" -ForegroundColor Cyan
docker compose --env-file $envFile -f $composeFile up -d postgres | Out-Null
if (-not (Wait-ForPort -Port 5433 -Name 'postgres' -TimeoutSec 60)) {
    exit 1
}

$gradlew = Join-Path $servicesDir 'gradlew.bat'

function Gradle-Args { param([string]$Task); return ('/c ""' + $gradlew + '" ' + $Task + '"') }

Write-Host "==> [2/4] Starting Eureka server" -ForegroundColor Cyan
$pids.eureka = Start-BgProcess -Name 'eureka' -WorkDir $servicesDir -Command 'cmd.exe' -Arguments (Gradle-Args ':eureka-server:bootRun')
if (-not (Wait-ForUrl -Url 'http://localhost:8761/' -Name 'eureka' -TimeoutSec 240)) {
    Write-Host "Eureka failed. Check log:" -ForegroundColor Red
    Write-Host (Join-Path $logsDir 'eureka.log')
    exit 1
}

Write-Host "==> [3/4] Starting backend services in parallel" -ForegroundColor Cyan
$pids['api-gateway']      = Start-BgProcess -Name 'api-gateway'      -WorkDir $servicesDir -Command 'cmd.exe' -Arguments (Gradle-Args ':api-gateway:bootRun')
$pids['user-service']     = Start-BgProcess -Name 'user-service'     -WorkDir $servicesDir -Command 'cmd.exe' -Arguments (Gradle-Args ':user-service:bootRun')
$pids['learning-service'] = Start-BgProcess -Name 'learning-service' -WorkDir $servicesDir -Command 'cmd.exe' -Arguments (Gradle-Args ':learning-service:bootRun')
if ($IncludeAI) {
    $pids['ai-service']   = Start-BgProcess -Name 'ai-service'       -WorkDir $servicesDir -Command 'cmd.exe' -Arguments (Gradle-Args ':ai-service:bootRun')
}
$pids['notification']     = Start-BgProcess -Name 'notification'     -WorkDir $servicesDir -Command 'cmd.exe' -Arguments (Gradle-Args ':notification:bootRun')

$beTargets = @(
    @{ Name = 'api-gateway';      Url = 'http://localhost:8080/actuator/health' },
    @{ Name = 'user-service';     Url = 'http://localhost:8086/actuator/health' },
    @{ Name = 'learning-service'; Url = 'http://localhost:8088/actuator/health' },
    @{ Name = 'notification';     Url = 'http://localhost:8089/actuator/health' }
)
if ($IncludeAI) {
    $beTargets += @{ Name = 'ai-service'; Url = 'http://localhost:8087/actuator/health' }
}
foreach ($t in $beTargets) {
    if (-not (Wait-ForUrl -Url $t.Url -Name $t.Name -TimeoutSec 300)) {
        Write-Host ("{0} failed. Check log: {1}" -f $t.Name, (Join-Path $logsDir ($t.Name + '.log'))) -ForegroundColor Red
        exit 1
    }
}

if (-not $SkipFE) {
    Write-Host "==> [4/4] Starting frontends" -ForegroundColor Cyan
    $pids['frontend-user']  = Start-BgProcess -Name 'frontend-user'  -WorkDir $feUserDir  -Command 'cmd.exe' -Arguments '/c npm run dev'
    $pids['frontend-admin'] = Start-BgProcess -Name 'frontend-admin' -WorkDir $feAdminDir -Command 'cmd.exe' -Arguments '/c npm run dev'
    if (-not (Wait-ForUrl -Url 'http://localhost:3000' -Name 'frontend-user'  -TimeoutSec 180)) { exit 1 }
    if (-not (Wait-ForUrl -Url 'http://localhost:3001' -Name 'frontend-admin' -TimeoutSec 180)) { exit 1 }
} else {
    Write-Host "==> [4/4] Skipping frontends (-SkipFE)" -ForegroundColor Yellow
}

$pids | ConvertTo-Json | Out-File -FilePath $pidsFile -Encoding utf8

Write-Host ""
Write-Host "==> STACK READY. PIDs saved to $pidsFile" -ForegroundColor Green
Write-Host "    Logs:  $logsDir"
Write-Host "    Stop:  ./scripts/stop-stack.ps1"
