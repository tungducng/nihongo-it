#!/usr/bin/env bash
set -euo pipefail
REPO_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
docker compose --env-file "$REPO_ROOT/docker/.env" -f "$REPO_ROOT/docker/docker-compose.yaml" stop postgres
