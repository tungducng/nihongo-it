# Stop all processes spawned by start-stack.ps1 + the postgres container.
#
# Reads .stack-pids.json. Falls back to killing every java / node process if
# the file is missing (use with caution).

$ErrorActionPreference = 'Continue'
$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$composeFile = Join-Path $repoRoot 'docker/docker-compose.yaml'
$envFile = Join-Path $repoRoot 'docker/.env'
$pidsFile = Join-Path $PSScriptRoot '..\.stack-pids.json'

if (Test-Path $pidsFile) {
    $pids = Get-Content $pidsFile -Raw | ConvertFrom-Json
    foreach ($prop in $pids.PSObject.Properties) {
        $thePid = $prop.Value
        Write-Host "  stop $($prop.Name) (pid=$thePid)"
        try {
            # Kill the cmd wrapper AND its java/node child by killing the whole tree.
            taskkill /PID $thePid /T /F 2>$null | Out-Null
        } catch {}
    }
    Remove-Item $pidsFile -Force
} else {
    Write-Host "No .stack-pids.json — falling back to taskkill java + node"
    Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
    Get-Process node -ErrorAction SilentlyContinue | Where-Object {
        $_.MainWindowTitle -match 'next' -or $_.Path -match 'next'
    } | Stop-Process -Force
}

Write-Host "Stopping postgres container..."
docker compose --env-file $envFile -f $composeFile stop postgres | Out-Null
Write-Host "Done." -ForegroundColor Green
