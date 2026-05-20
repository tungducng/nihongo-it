#!/usr/bin/env bash
# First-time local setup: agent jar + .env + stack up.
# Idempotent — safe to re-run.

set -euo pipefail
cd "$(dirname "$0")/.."

echo "==> 1/3 Downloading OTel javaagent (if missing)…"
if [[ ! -f docker/libs/opentelemetry-javaagent.jar ]]; then
  ./scripts/download-otel-agent.sh
else
  echo "    agent already present, skipping."
fi

echo "==> 2/3 Bootstrapping docker/.env…"
if [[ ! -f docker/.env ]]; then
  cp docker/.env.example docker/.env
  echo "    created docker/.env — open it and fill secrets (OPENAI_API_KEY, GOOGLE_*, MAIL_*, JWT_SECRET) before running services."
else
  echo "    docker/.env already present, leaving untouched."
fi

echo "==> 3/3 Bringing the stack up…"
make up

cat <<EOF

Stack started. Useful links once containers are healthy (~30-60s):
  • frontend-user   http://localhost:3000
  • frontend-admin  http://localhost:3002
  • API gateway     http://localhost:8080
  • Eureka          http://localhost:8761
  • Grafana         http://localhost:3001  (admin / admin)
  • Prometheus      http://localhost:9090
  • Tempo (HTTP)    http://localhost:3200

Inspect logs:   make logs
Stop:           make down
Nuke volumes:   make reset
EOF
