# YAS Adoption Plan — Nihongo IT

**Date:** 2026-05-19
**Reference:** `sample_project/yas/` + `sample_project/yas/docs/knowledge/` (13 docs)
**Purpose:** Tham khảo YAS (Yet Another Shop — ecommerce microservice production-grade) để nâng cấp Nihongo IT toàn diện: architecture, file layout, database, docker, observability, auth, messaging, scripts, K8s, CI/CD.

---

## Executive summary

YAS dạy 3 thứ cốt lõi mà Nihongo IT đang thiếu hoặc làm nhẹ:

1. **Production-grade observability** — OTel auto-instrumentation cho tracing distributed (Tempo) + log correlation (traceId trong MDC), không chỉ Prometheus + Loki rời rạc.
2. **Database-per-service trên 1 Postgres** — separate schemas/databases, init script, mỗi service tự chạy migration của mình; cộng với CDC (Debezium) nếu cần sync sang search.
3. **Shared common-library mạnh** — không chỉ exception + DTO mà còn audit base entity, mapper base, Kafka CDC base, AuthenticationUtils, ServiceUrlConfig.

Ngoài ra YAS có nhiều thứ hữu ích hạng nhì: BFF pattern (Spring Cloud Gateway + Redis session), 3-file Docker Compose tách concern, K8s với Helm, CI có Sonar + JaCoCo coverage gate + OWASP Dependency-Check + Gitleaks, automation UI testing.

Plan này chia 5 phase, ưu tiên giá trị-trên-công-sức. Phase 1 (DB split + OTel + common-library) là **must-do** cho production readiness. Phase 5 (K8s, BFF, Keycloak) là **nice-to-have** học hỏi nhưng có thể defer.

---

## Current vs YAS — gap analysis

| Lĩnh vực | Nihongo IT hiện tại | YAS | Khoảng cách |
|---|---|---|---|
| Build system | Mỗi service Gradle độc lập | Maven monorepo + parent pom | Trung. Có chủ đích, không cần đổi |
| Service count | 6 (eureka, gateway, user, learning, ai, notify, common) | 17+ services + 2 BFFs | Lớn, không cần catch-up |
| Database | 1 Postgres dùng chung, Flyway | DB per service (1 Postgres, nhiều DB), Liquibase | **Lớn** |
| Migrations | Flyway dưới mỗi service | Liquibase YAML master + ddl/data | Nhỏ. Flyway đủ tốt, giữ. |
| Auth | JWT in-house, gateway validate, services trust headers | Keycloak + OAuth2 + BFF + Redis session | **Lớn** (giữ JWT nếu không cần SSO) |
| Service discovery | Eureka | K8s DNS (cluster) hoặc Docker DNS | Đã có, giữ |
| Common module | `services/common/` — exception, gateway filter, logging | `common-library/` — exception + audit + mapper + Kafka CDC + CsvExporter + AuthUtils | **Trung** |
| Observability | Prometheus + Grafana + Loki + Promtail | OTel auto-agent → Collector → Prometheus + Tempo + Loki + Grafana + log correlation | **Lớn** (thiếu trace) |
| Messaging | Không có | Kafka + Debezium CDC → ES (Postgres → search) | Trung (chỉ làm khi có search) |
| Search | Không có | Elasticsearch | Nhỏ (chưa cần) |
| Docker | 1 compose file `docker/docker-compose.yaml` | 3 file: core / search / o11y, merge qua `COMPOSE_FILE` env | **Trung** |
| Frontend | Next.js 16 user (:3000) + admin (:3002), Tailwind + shadcn | Next.js 14 storefront + backoffice, Bootstrap, behind BFF | Nhỏ (stack hiện đại hơn YAS) |
| BFF | Không có; axios gọi gateway trực tiếp | 2 Spring Cloud Gateway BFFs, Redis session, TokenRelay | Trung |
| K8s | Không có; chỉ script GCP `deploy/` | Helm charts đầy đủ + operators (Postgres, ES, Kafka, Keycloak) | **Lớn** |
| CI/CD | 4 workflows (backend, frontend, python, codeql) | 21 workflows + Sonar + JaCoCo gate + Dep-Check + Gitleaks | Trung |
| Security scanning | CodeQL (chính), Dependabot, manual review | CodeQL + Sonar + OWASP Dep-Check + Gitleaks | Nhỏ |
| Logging format | Logback JSON (logstash encoder) | Logback JSON + OTel MDC → Loki | Trung (cần thêm trace ID) |
| Auditing entity | Mỗi entity tự định nghĩa `createdAt`/`updatedAt` | `AbstractAuditEntity` + `CustomAuditingEntityListener` | Nhỏ |
| Scripts | `deploy/` GCP-specific | `start-yas.sh`, `start-source-connectors.sh`, `workflows.sh`, `postgres_init.sql`, `k8s/deploy/*.sh` | Trung |
| Quality gates | Detekt + Ktlint + frontend lint | Checkstyle Google Style + Sonar gate + 80% coverage | Trung |

---

## Phased adoption roadmap

5 phases, mỗi phase độc lập có thể merge và verify. Estimate là rough — depends on user availability.

| Phase | Theme | Effort | Outcome |
|---|---|---|---|
| **P1** | DB per service + Observability tracing | 2–3 days | Postgres split, OTel agent, Tempo traces |
| **P2** | Common library enrich + Audit base | 1–2 days | Audit entity, AuthenticationUtils, error helpers consolidated |
| **P3** | Docker reorganization + Scripts | 1 day | 3-compose split, startup scripts, postgres_init |
| **P4** | CI/CD hardening | 1–2 days | Sonar + JaCoCo + Dep-Check + Gitleaks |
| **P5** | (Optional) K8s + BFF + Keycloak | 1–2 weeks | Helm charts, BFF với Redis session, Keycloak realm |

---

## Phase 1 — DB-per-service + OTel observability

**Goal:** Tiệm cận production: mỗi service có DB riêng, có distributed tracing đầu cuối.

### 1.1 Database split (Postgres single instance, multiple DBs)

**Hiện trạng:** Một database `${POSTGRES_DB}` dùng chung; Flyway migrations trộn lẫn (`user_service`, `learning_service`, `flashcard` tables cùng schema).

**Mục tiêu:** Mỗi service có 1 database riêng cùng cluster Postgres.

**Steps:**
1. Tạo `docker/postgres_init.sql` (theo mẫu YAS):
   ```sql
   CREATE DATABASE user_service;
   CREATE DATABASE learning_service;
   CREATE DATABASE ai_service;
   CREATE DATABASE notification_service;
   ```
   Mount vào Postgres container: `volumes: - ./postgres_init.sql:/docker-entrypoint-initdb.d/postgres_init.sql:ro`
2. Mỗi service đặt `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/<service_db>` riêng.
3. Tách Flyway migrations theo service: copy migrations hiện có → split bằng table ownership (xem `services/<svc>/src/main/resources/db/migration/`). Nếu service A có FK sang table service B → đổi sang lưu chỉ ID, validate qua REST.
4. Verify: `docker compose down -v && docker compose up -d` → mỗi service tự chạy migration trên DB riêng.

**Files mới/đổi:**
- `docker/postgres_init.sql` (mới)
- `docker/docker-compose.yaml` — datasource URLs per service
- `services/<svc>/src/main/resources/db/migration/V*.sql` — tách

**Risk:** FK xuyên service. Mitigation: chuyển sang ID-only references (đã là pattern phổ biến trong nihongo-it).

### 1.2 OpenTelemetry agent + Tempo

**Hiện trạng:** Có Prometheus (metrics) + Loki (logs) + Promtail. Không có distributed tracing. Loki logs không có traceId — không correlate được.

**Mục tiêu:** Auto-instrument mọi service Java qua OTel javaagent → Collector → Tempo (traces) + Prometheus (metrics) + Loki (logs với traceId/spanId trong MDC).

**Steps:**
1. Download `opentelemetry-javaagent.jar` về `docker/libs/` (file ~25 MB). Add `.gitignore` cho thư mục đó hoặc commit kèm (YAS commit).
2. Tạo `docker/otel-collector/otelcol-config.yml`:
   - Receivers: OTLP gRPC :4317, HTTP :4318
   - Exporters: prometheusremotewrite → prometheus, otlp → tempo, loki → loki
   - Processors: batch, resource/loki (label extraction)
3. Tạo `docker/tempo/tempo.yml` (single-node local storage).
4. Update `docker/docker-compose.yaml` (hoặc tách `docker-compose.o11y.yml`):
   - Add services: `otel-collector`, `tempo`
   - Cho mỗi BE service:
     ```yaml
     environment:
       JAVA_TOOL_OPTIONS: "-javaagent:/opentelemetry-javaagent.jar"
       OTEL_EXPORTER_OTLP_ENDPOINT: "http://otel-collector:4317"
       OTEL_SERVICE_NAME: "<service>"
       OTEL_RESOURCE_ATTRIBUTES: "service.namespace=nihongo-it"
       OTEL_INSTRUMENTATION_LOGBACK-MDC_ADD-BAGGAGE: "true"
     volumes:
       - ./libs/opentelemetry-javaagent.jar:/opentelemetry-javaagent.jar:ro
     ```
5. Update `deployment/logback-spring.xml` (mới — shared via mount):
   - Encoder: `LogstashEncoder` + OTel MDC pattern (`%X{trace_id}`, `%X{span_id}`)
   - Mount vào tất cả service: `./deployment/app-config/logback-spring.xml:/app-config/logback-spring.xml`
6. Provision Grafana datasources mới:
   - `docker/grafana-provisioning/datasources/tempo.yml`
   - Loki datasource cấu hình `derivedFields` linking traceId → Tempo
7. Add dependency vào mỗi service `build.gradle.kts`:
   ```kotlin
   implementation("io.opentelemetry.instrumentation:opentelemetry-logback-mdc-1.0:2.x")
   implementation("net.logstash.logback:logstash-logback-encoder:8.0")
   ```

**Files mới:**
- `docker/libs/opentelemetry-javaagent.jar`
- `docker/otel-collector/otelcol-config.yml`
- `docker/tempo/tempo.yml`
- `deployment/app-config/logback-spring.xml`
- `docker/grafana-provisioning/datasources/tempo.yml` (update Loki cho derivedFields)

**Verify:** Mở Grafana → click một log trong Loki → bấm traceId link → nhảy sang Tempo xem trace tree. Cross-service request từ FE → gateway → user-service → learning-service hiển thị thành 1 trace duy nhất.

---

## Phase 2 — Common library enrich + Audit base

**Hiện trạng:** `services/common/` có `BusinessException`, `GlobalExceptionHandler`, `GatewayHeaderAuthFilter`, `ErrorResponseDto`. Đủ cho exception nhưng thiếu audit + Kafka + mapper base.

**Mục tiêu:** Mở rộng `common` thành "thư viện vũ trang" mà YAS làm.

### 2.1 `AbstractAuditEntity`

Tạo `services/common/src/main/kotlin/com/example/common/entity/AbstractAuditEntity.kt`:

```kotlin
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class AbstractAuditEntity {
    @CreatedDate
    @Column(updatable = false)
    var createdAt: Instant? = null

    @CreatedBy
    @Column(updatable = false)
    var createdBy: String? = null

    @LastModifiedDate
    var updatedAt: Instant? = null

    @LastModifiedBy
    var updatedBy: String? = null
}
```

Plus `AuditConfig.kt` (`@EnableJpaAuditing` + `AuditorAware<String>` bean lấy từ JWT `X-User-Id` header).

**Apply tới:** All entities trong user-service, learning-service. Migration: drop manual `created_at`/`updated_at` columns hoặc rename.

### 2.2 `AuthenticationUtils`

Tạo `services/common/src/main/kotlin/com/example/common/security/AuthenticationUtils.kt`:

```kotlin
object AuthenticationUtils {
    fun extractUserId(): UUID = ...  // đọc X-User-Id header hoặc principal
    fun extractRole(): String? = ...
    fun extractEmail(): String? = ...
}
```

Replace inline `userAuthUtil.getCurrentUserId()` rải rác trong các services bằng common helper.

<!-- skip ### 2.3 `BaseMapper` (nếu adopt MapStruct)

YAS dùng MapStruct. Nihongo-it hiện tại không dùng (entity → DTO mapper inline). Có thể skip hoặc adopt:

```kotlin
interface BaseMapper<E, V> {
    fun toViewModel(entity: E): V
    fun toEntity(viewModel: V): E
    fun toViewModels(entities: List<E>): List<V>
}
``` -->

<!-- Decision: defer; chỉ adopt khi có nhu cầu nhiều mapper phức tạp. -->

<!--skip ### 2.4 `ApiResponseEnvelope<T>` (nếu sau-này muốn wrap lại)

Audit 2026-05-19 đã quyết **drop wrappers**. Nếu sau này đổi ý, common-library có thể add `ApiResponseEnvelope<T> { data, message? }` + axios interceptor unwrap. Để mở. -->

### 2.5 Kafka CDC base (chỉ adopt khi P5 messaging)

`BaseKafkaListenerConfig`, `BaseCdcConsumer`, `@RetrySupportDql` — only when adding Kafka.

**Output Phase 2:**
- `common/entity/AbstractAuditEntity.kt`
- `common/config/AuditConfig.kt`
- `common/security/AuthenticationUtils.kt`
- Updated entities trong user/learning services
- Migrations đổi/rename audit columns

---

## Phase 3 — Docker reorganization + Scripts

**Hiện trạng:** 1 file `docker/docker-compose.yaml` chứa hết: postgres + 6 BE services + 2 frontends + monitoring. ~250 dòng.

**Mục tiêu:** Tách thành 3 file giống YAS, dễ scale up/down từng layer khi dev.

### 3.1 Compose split

```
docker/
├── docker-compose.yaml          # CORE: postgres, eureka, gateway, services, frontends
├── docker-compose.o11y.yml      # OBSERVABILITY: prometheus, loki, promtail, tempo, otel-collector, grafana
├── docker-compose.search.yml    # SEARCH (P5): elasticsearch — defer
└── .env                          # COMPOSE_FILE=docker/docker-compose.yaml:docker/docker-compose.o11y.yml
```

`.env` ở root cho phép `docker compose up` mặc định dùng cả 2. Khi cần dev nhẹ (no o11y), `docker compose -f docker/docker-compose.yaml up`.

### 3.2 Compose ra ngoài thư mục root (optional)

YAS đặt compose ngay root. Nhân thi đặt trong `docker/` cũng OK. Để tránh đổi nhiều thứ, giữ trong `docker/` nhưng thêm symlink hoặc `Makefile`/`justfile` ở root để gọi tắt:

```makefile
up:
	docker compose -f docker/docker-compose.yaml -f docker/docker-compose.o11y.yml up -d
down:
	docker compose -f docker/docker-compose.yaml -f docker/docker-compose.o11y.yml down
logs:
	docker compose -f docker/docker-compose.yaml logs -f
```

### 3.3 Scripts

Tạo `scripts/` ở root:
- `scripts/start.sh` — wrapper `docker compose up`
- `scripts/stop.sh` — `docker compose down`
- `scripts/reset.sh` — `down -v` (wipe volumes)
- `scripts/logs.sh <service>` — tail logs
- `scripts/migrate-status.sh` — gọi Flyway info trên mỗi service

Sau khi P1 (DB split) xong, thêm:
- `scripts/db-shell.sh <service>` — `docker compose exec postgres psql -U admin <db>`

Khi P5 (Kafka):
- `scripts/start-source-connectors.sh` — register Debezium connectors

### 3.4 Application config tách ngoài

Tạo `deployment/app-config/` chứa file config chia sẻ:
- `logback-spring.xml` — JSON + OTel MDC (đã đề cập Phase 1)
- (optional) `application-prod.yaml` overrides nếu sau này deploy

Mount vào mọi service: `- ./deployment/app-config:/app-config:ro` + `SPRING_CONFIG_ADDITIONAL_LOCATION=/app-config/`.

---

## Phase 4 — CI/CD hardening

**Hiện trạng:** 4 workflows: backend (matrix), frontend (matrix), python, codeql. Build pass, không có coverage gate / SAST nâng cao.

**Mục tiêu:** Match production quality bar của YAS.

### 4.1 JaCoCo coverage gate (BE)

Mỗi service đã có `id("jacoco")` plugin trong `build.gradle.kts`. Add coverage rule:

```kotlin
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit { minimum = "0.60".toBigDecimal() }  // 60% start, raise gradually
        }
    }
}
tasks.check { dependsOn(tasks.jacocoTestCoverageVerification) }
```

Trong `.github/workflows/backend.yml`, sau bước test, add `madrapps/jacoco-report@v1.7.0` để post coverage delta lên PR.

<!-- ### 4.2 SonarCloud (optional)

Yêu cầu account SonarCloud + token. Tạo project, add workflow step:
```yaml
- name: SonarCloud Scan
  uses: SonarSource/sonarcloud-github-action@master
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

Bật quality gate (default: A code quality + no vulnerabilities + 80% coverage on new code). -->

### 4.3 OWASP Dependency-Check

Add Gradle plugin (root or per service):
```kotlin
plugins {
    id("org.owasp.dependencycheck") version "11.1.0"
}
```

Trong CI:
```yaml
- name: OWASP Dependency Check
  run: ./gradlew dependencyCheckAggregate
```

Threshold: fail nếu CVE score ≥ 7.0.

### 4.4 Gitleaks

Add `.gitleaks.toml` (allow `services/common/.../examples/*`, env templates) + workflow:
```yaml
- uses: gitleaks/gitleaks-action@v2
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

Chạy trên mọi PR; fail nếu có secret leak.

### 4.5 Frontend coverage

`vitest --coverage` trong `frontend-user` + `frontend-admin`; upload xml/lcov tới SonarCloud hoặc Codecov.

### 4.6 Quality gate summary

Sau Phase 4, mỗi PR phải pass:
- ✅ Build success (BE all services + FE both apps + Python)
- ✅ Unit tests + frontend tests pass
- ✅ JaCoCo coverage ≥ 60% (raise to 80% theo thời gian)
- ✅ CodeQL no new alerts
- ✅ Dep-Check no critical CVE
- ✅ Gitleaks no secret
- ✅ (Optional) SonarCloud quality gate

---
<!-- 
## Phase 5 — Optional: K8s + BFF + Keycloak (advanced)

Phase này lớn, có thể tách thành nhiều mini-phase. Chỉ làm khi có demand cụ thể (vd: deploy production GKE, cần SSO).

### 5.1 Kubernetes + Helm

YAS có `k8s/charts/<service>/` + `k8s/charts/backend/` (shared template). Cần học và làm:

**Steps:**
1. Generate Helm chart cho 1 service làm pilot (vd `user-service`):
   ```bash
   helm create k8s/charts/user-service
   ```
2. Customize: `templates/deployment.yaml`, `service.yaml`, `ingress.yaml`, `configmap.yaml`, `secret.yaml`.
3. Tạo `k8s/charts/backend/` shared chart (templates dùng chung) — DRY giữa các service.
4. Deploy script: `k8s/deploy/setup-cluster.sh` (Postgres operator, Prometheus operator, ...).
5. Test trên `kind` hoặc `minikube` local trước, sau đó GKE.

**Operators cần install** (theo YAS):
- Zalando Postgres Operator (DB)
- Strimzi Kafka Operator (nếu có Kafka từ P5.3)
- Prometheus Operator (ServiceMonitor)
- Keycloak Operator (nếu có Keycloak từ P5.2)
- OpenTelemetry Operator (auto-inject sidecar/agent)

### 5.2 Keycloak + OAuth2 + BFF (thay JWT in-house)

**Lý do làm:** SSO, password policies, MFA, social login (Google, GitHub) free khi có Keycloak.
**Lý do không làm:** Đang ổn với JWT custom, thêm Keycloak = thêm 1 service nặng (Postgres + Java) phải maintain.

**Steps nếu làm:**
1. Add Keycloak container vào `docker-compose.yaml` (image `quay.io/keycloak/keycloak:26.0`).
2. Export realm `nihongo-it` từ Keycloak UI → `identity/realm-export.json`. Auto-import via `--import-realm`.
3. Đổi 3 services BE thành OAuth2 resource server: `spring.security.oauth2.resourceserver.jwt.issuer-uri=...`.
4. Migrate user table khỏi user-service → để Keycloak quản users; user-service chỉ giữ profile (level, jlptGoal, preferences) liên kết qua Keycloak `sub` UUID.
5. Tạo 2 BFFs (Spring Cloud Gateway):
   - `services/storefront-bff/` cho `frontend-user`
   - `services/backoffice-bff/` cho `frontend-admin`
   - Mỗi BFF: OAuth2 client (Authorization Code + PKCE), session trong Redis, `TokenRelay` filter, route to gateway/services.
6. Frontend đổi: bỏ in-memory token, dùng cookie session do BFF set; axios không cần `Authorization` header (BFF tự inject).

**Risk:** Migration data — phải import existing users vào Keycloak. Plan rollback.

### 5.3 Kafka + CDC (chỉ khi có search/feed)

Chưa cần vì nihongo-it không có search. Khi muốn implement search (e.g. tìm từ vựng theo nhiều tiêu chí, suggest...):

1. Add Kafka + ZooKeeper + Kafka Connect + ES tới `docker-compose.search.yml`.
2. Tạo `kafka/connects/debezium-vocabulary.json` — capture changes trên `vocabulary` table.
3. Thêm `search-service` consume CDC topic, sync vào ES.
4. FE search call REST `search-service`.

Adopt `BaseKafkaListenerConfig` + `@RetrySupportDql` từ common-library (Phase 2.5). -->

<!-- ### 5.4 Sample data + automation UI

YAS có `sampledata/` service chạy seed data; `automation-ui/` Cucumber + Selenium E2E. -->
## Phase 6 — Playwright + agent + skills + mcp
**Nihongo IT:** đã có Flyway data migrations (V*__seed_*.sql). Đủ cho seed. Automation UI có thể thêm sau bằng Playwright (đã có plugin) — record test cases cho golden paths (đăng ký → login → tạo flashcard → review).
https://playwright.dev/agent-cli/skills

---

## Concrete file layout deltas

### Đề xuất directory tree sau khi hoàn thành P1-P4:

```
nihongo-it/
├── README.md
├── CLAUDE.md
├── Makefile                                # mới: up/down/logs/reset
├── docs/
│   ├── audits/2026-05-19-be-fe-contract-audit.md
│   └── plans/
│       ├── 2026-05-17-nextjs-migration.md
│       └── 2026-05-19-yas-adoption-plan.md   # file này
├── deployment/                             # mới
│   └── app-config/
│       └── logback-spring.xml              # mounted vào mọi BE service
├── docker/
│   ├── docker-compose.yaml                 # CORE
│   ├── docker-compose.o11y.yml             # OBSERVABILITY (new)
│   ├── docker-compose.search.yml           # SEARCH (P5)
│   ├── postgres_init.sql                   # mới: tạo db per service
│   ├── libs/
│   │   └── opentelemetry-javaagent.jar     # mới (gitignored hoặc commit)
│   ├── otel-collector/
│   │   └── otelcol-config.yml              # mới
│   ├── tempo/
│   │   └── tempo.yml                       # mới
│   ├── prometheus.yml
│   ├── loki-config.yml
│   ├── promtail-config.yml
│   └── grafana-provisioning/
│       ├── datasources/
│       │   ├── prometheus.yml
│       │   ├── loki.yml                    # update derivedFields → tempo
│       │   └── tempo.yml                   # mới
│       └── dashboards/
│           ├── service-overview.json
│           └── otel-collector.json
├── kafka/                                  # P5
│   └── connects/
│       └── debezium-vocabulary.json
├── k8s/                                    # P5
│   ├── charts/
│   │   ├── backend/                        # shared template
│   │   ├── user-service/
│   │   ├── learning-service/
│   │   └── ...
│   └── deploy/
│       ├── setup-cluster.sh
│       ├── setup-keycloak.sh
│       └── deploy-applications.sh
├── identity/                               # P5 — Keycloak realm
│   ├── realm-export.json
│   └── themes/
├── nginx/                                  # P5 — reverse proxy
│   └── templates/
│       └── default.conf.template
├── scripts/                                # mới
│   ├── start.sh
│   ├── stop.sh
│   ├── reset.sh
│   ├── logs.sh
│   ├── db-shell.sh
│   └── start-source-connectors.sh          # P5
├── services/
│   ├── common/
│   │   └── src/main/kotlin/com/example/common/
│   │       ├── entity/
│   │       │   └── AbstractAuditEntity.kt  # mới Phase 2
│   │       ├── config/
│   │       │   └── AuditConfig.kt          # mới Phase 2
│   │       ├── security/
│   │       │   ├── AuthenticationUtils.kt  # mới Phase 2
│   │       │   └── GatewayHeaderAuthFilter.kt
│   │       ├── exception/
│   │       │   ├── BusinessException.kt
│   │       │   └── GlobalExceptionHandler.kt
│   │       ├── dto/
│   │       │   └── ErrorResponseDto.kt
│   │       ├── kafka/                      # P5
│   │       │   ├── BaseKafkaListenerConfig.kt
│   │       │   ├── BaseCdcConsumer.kt
│   │       │   └── annotations/RetrySupportDql.kt
│   │       └── mapper/                     # P5 nếu adopt MapStruct
│   │           └── BaseMapper.kt
│   ├── eureka-server/
│   ├── api-gateway/
│   ├── user-service/
│   │   ├── build.gradle.kts                # add OTel deps
│   │   ├── settings.gradle.kts
│   │   └── src/main/resources/
│   │       └── db/migration/               # chỉ migrations của user-service
│   ├── learning-service/
│   ├── ai-service/
│   ├── notification/
│   └── (P5) storefront-bff/, backoffice-bff/
├── frontend-user/
├── frontend-admin/
├── python/
└── .github/workflows/
    ├── backend.yml                         # add Sonar + JaCoCo + Dep-Check
    ├── frontend.yml                        # add coverage upload
    ├── python.yml
    ├── codeql.yml
    ├── gitleaks.yml                        # mới Phase 4
    └── dependency-check.yml                # mới Phase 4
```

---

## Out of scope (deliberately NOT adopting from YAS)

| Item | Lý do bỏ |
|---|---|
| Maven monorepo | Mỗi service Gradle độc lập là decision có chủ đích trong CLAUDE.md. |
| BFF nếu không có Keycloak | BFF chỉ value khi cần session cookie + token relay; với JWT in-memory hiện tại, axios + gateway đủ. |
| Bootstrap CSS | Đã có Tailwind 4 + shadcn/ui, hiện đại hơn. |
| MapStruct | Nihongo IT entities đơn giản, mapper inline OK. |
| Eureka removal | YAS không dùng Eureka vì K8s DNS; Nihongo IT chưa K8s nên giữ Eureka. |
| Spring Cloud Gateway thay routing tự viết | Gateway hiện tại đã đủ rate-limit + JWT validate. |
| Custom Keycloak theme | Không cần branding deep. |
| `automation-ui` Cucumber | Adopt Playwright thay (đã có plugin trong session). |

---

## Risks & mitigations

1. **DB split breaks Flyway state** — migrations đã chạy trên DB chung sẽ có lịch sử lộn xộn khi tách. Mitigation: làm trên 1 môi trường sạch (`docker compose down -v`), không migrate prod data.

2. **OTel agent overhead** — auto-instrumentation thêm ~50–100 MB RAM mỗi service + 5–10% CPU. Trên máy 16 GB RAM ổn; máy yếu hơn cần tăng heap hoặc disable agent qua env.

3. **Liquibase vs Flyway migration** — KHÔNG migrate; giữ Flyway. YAS dùng Liquibase nhưng Flyway equivalent. Tránh churn.

4. **Keycloak adds complexity** — chỉ làm khi có nhu cầu SSO/MFA cụ thể. JWT custom đủ cho tỷ lệ active users hiện tại.

5. **Coverage gate fail CI** — set initial 60%, raise dần. Tránh block dev velocity.

6. **K8s without K8s knowledge** — Helm + operators cần học. Phase 5 ưu tiên sau khi P1-P4 ổn định.

---

## Execution order recommendation

**Tuần 1:** P1.1 (DB split) — biggest impact, foundation cho mọi thứ khác.
**Tuần 1–2:** P1.2 (OTel + Tempo) — phải song song với P1.1 vì đụng cùng compose file.
**Tuần 2–3:** P2 (common library enrich).
**Tuần 3:** P3 (Docker reorg + scripts).
**Tuần 4:** P4 (CI/CD hardening) — sau cùng vì cần code stable trước khi đặt coverage gate.
**Sau đó:** P5 nếu có demand.

Mỗi phase commit + push riêng để rollback dễ.

---

## Verification per phase

| Phase | Verify command |
|---|---|
| P1.1 DB split | `docker compose down -v && docker compose up -d && docker compose exec postgres psql -U admin -c '\l'` (thấy 4 databases) |
| P1.2 OTel | Mở `http://localhost:3001/` (Grafana) → Explore → Loki → click trace ID → mở Tempo |
| P2 audit entity | Tạo flashcard via API → check `created_by`, `updated_at` tự fill |
| P3 compose split | `docker compose -f docker/docker-compose.yaml up -d` (no o11y) → service up bình thường |
| P4 CI gates | Open PR với coverage < 60% → CI fail; với secret trong code → Gitleaks block |
| P5 K8s | `helm install user-service k8s/charts/user-service/ -n nihongo-it` → pod ready |

---

## Quick wins (nếu chỉ làm 1 thứ)

Nếu chỉ có thời gian cho **1 thay đổi**:
- **OTel + Tempo (P1.2)** — return on investment cao nhất. Debug production issues 10× nhanh hơn vì trace ID xuyên service + log.

Nếu chỉ có **1 buổi rảnh**:
- **scripts/ + Makefile (P3.3)** — 30 phút setup, lifetime payoff cho dev workflow.

Nếu chỉ làm **1 phase**:
- **P1 (DB split + OTel)** — production foundation.
