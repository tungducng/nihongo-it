#!/usr/bin/env bash
# Wait until a URL returns 2xx-4xx (anything < 500) or timeout.
# Usage: ./wait-for-url.sh <url> <timeout_seconds>

set -euo pipefail
URL="${1:?URL required}"
TIMEOUT="${2:-180}"

echo -n "  waiting for $URL ... "
for ((i = 0; i < TIMEOUT; i++)); do
  STATUS=$(curl -s -o /dev/null -w '%{http_code}' "$URL" || echo 000)
  if [[ "$STATUS" =~ ^[2-4] ]]; then
    echo "UP ($STATUS)"
    exit 0
  fi
  sleep 1
done

echo "TIMEOUT after ${TIMEOUT}s"
exit 1
