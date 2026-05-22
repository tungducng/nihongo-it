#!/usr/bin/env bash
# Start only the Postgres container. Playwright's webServer[] handles
# everything else (eureka, gateway, 4 BE services, 2 FE apps).
#
# Usage:
#   cd e2e
#   ./scripts/start-stack.sh

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
COMPOSE_FILE="$REPO_ROOT/docker/docker-compose.yaml"
ENV_FILE="$REPO_ROOT/docker/.env"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Missing $ENV_FILE. Copy docker/.env.example to docker/.env first." >&2
  exit 1
fi

echo "==> Starting postgres container..."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d postgres

echo "==> Waiting for postgres on localhost:5433..."
for i in {1..60}; do
  if (echo > /dev/tcp/localhost/5433) 2>/dev/null; then
    echo "==> Postgres ready. You can now run: npm test"
    exit 0
  fi
  sleep 1
done

echo "Postgres did not become reachable within 60s" >&2
exit 1
