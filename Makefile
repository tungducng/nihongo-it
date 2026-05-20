# Nihongo IT — developer convenience wrappers around docker compose + gradle.
# Run `make help` for a list. POSIX make (GNU make on Linux/macOS, also works
# with `make` shipped via Git Bash / MSYS on Windows).
#
# COMPOSE_FILE is configured in docker/.env so a plain `make up` loads both the
# core stack and the observability stack. Override with `MAKE_NO_O11Y=1` to skip
# observability for a lighter dev loop.

DOCKER_DIR        := docker
COMPOSE_CORE      := $(DOCKER_DIR)/docker-compose.yaml
COMPOSE_O11Y      := $(DOCKER_DIR)/docker-compose.o11y.yml
COMPOSE_FILES     := -f $(COMPOSE_CORE) -f $(COMPOSE_O11Y)
ifdef MAKE_NO_O11Y
COMPOSE_FILES     := -f $(COMPOSE_CORE)
endif
COMPOSE           := docker compose --env-file $(DOCKER_DIR)/.env $(COMPOSE_FILES)
SERVICES_DIR      := services
FRONTEND_USER     := frontend-user
FRONTEND_ADMIN    := frontend-admin

.PHONY: help
help: ## Show this help.
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m<target>\033[0m\n\nTargets:\n"} /^[a-zA-Z_-]+:.*?##/ { printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2 }' $(MAKEFILE_LIST)

# ── Docker stack ──────────────────────────────────────────────────────────────

.PHONY: up
up: agent ## Start the full stack (core + observability) detached.
	$(COMPOSE) up -d

.PHONY: up-core
up-core: ## Start only the core stack (no Prometheus/Loki/Tempo/Grafana).
	docker compose --env-file $(DOCKER_DIR)/.env -f $(COMPOSE_CORE) up -d

.PHONY: down
down: ## Stop the stack (containers + networks; keep volumes).
	$(COMPOSE) down

.PHONY: reset
reset: ## Stop and DELETE volumes — wipes DB data + Grafana state. Destructive.
	$(COMPOSE) down -v

.PHONY: ps
ps: ## Show container status.
	$(COMPOSE) ps

.PHONY: logs
logs: ## Tail logs (set SERVICE=user-service to scope).
	@if [ -z "$(SERVICE)" ]; then $(COMPOSE) logs -f --tail=200; else $(COMPOSE) logs -f --tail=200 $(SERVICE); fi

.PHONY: restart
restart: ## Restart a service (SERVICE=user-service).
	@if [ -z "$(SERVICE)" ]; then echo "Usage: make restart SERVICE=<name>"; exit 2; fi
	$(COMPOSE) restart $(SERVICE)

.PHONY: pull
pull: ## Pull latest images.
	$(COMPOSE) pull

# ── Build images ──────────────────────────────────────────────────────────────

.PHONY: build
build: ## Rebuild all docker images (no cache).
	$(COMPOSE) build --no-cache

.PHONY: build-be
build-be: ## Rebuild backend service images only.
	$(COMPOSE) build eureka-server api-gateway user-service learning-service ai-service notification-service

.PHONY: build-fe
build-fe: ## Rebuild Next.js frontend images.
	$(COMPOSE) build frontend-user frontend-admin

# ── DB shell + status ─────────────────────────────────────────────────────────

.PHONY: db
db: ## Open psql shell (DB=user_service|learning_service|notification_service).
	@DB=$${DB:-user_service}; \
	$(COMPOSE) exec postgres psql -U $${DB_USERNAME:-postgres} -d $$DB

.PHONY: db-list
db-list: ## List all databases in the cluster.
	$(COMPOSE) exec postgres psql -U $${DB_USERNAME:-postgres} -c '\l'

# ── Local gradle + npm verification (no docker) ───────────────────────────────

.PHONY: be-build
be-build: ## Gradle build all services, skip tests. Fast compile-only check.
	cd $(SERVICES_DIR) && ./gradlew build -x test

.PHONY: be-test
be-test: ## Gradle test all services.
	cd $(SERVICES_DIR) && ./gradlew test

.PHONY: be-format
be-format: ## ktlint auto-format all backend services.
	cd $(SERVICES_DIR) && ./gradlew ktlintFormat

.PHONY: fe-check
fe-check: ## type-check + lint + build both Next.js apps.
	cd $(FRONTEND_USER)  && npm run type-check && npm run lint && npm run build
	cd $(FRONTEND_ADMIN) && npm run type-check && npm run lint && npm run build

.PHONY: fe-test
fe-test: ## Run vitest on both apps.
	cd $(FRONTEND_USER)  && npm test
	cd $(FRONTEND_ADMIN) && npm test

# ── One-time setup ────────────────────────────────────────────────────────────

.PHONY: agent
agent: ## Download OTel javaagent if missing (mounted into BE containers).
	@test -f $(DOCKER_DIR)/libs/opentelemetry-javaagent.jar || ./scripts/download-otel-agent.sh

.PHONY: env
env: ## Copy docker/.env.example → docker/.env if missing.
	@test -f $(DOCKER_DIR)/.env || (cp $(DOCKER_DIR)/.env.example $(DOCKER_DIR)/.env && echo "Created $(DOCKER_DIR)/.env from example; edit secrets before `make up`.")
