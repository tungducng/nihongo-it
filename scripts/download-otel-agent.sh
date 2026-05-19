#!/usr/bin/env bash
# Downloads the OpenTelemetry Java agent into docker/libs/ so docker compose
# can mount it into every backend service. Pinned to a known-good version.
#
# Usage: ./scripts/download-otel-agent.sh

set -euo pipefail

VERSION="${OTEL_AGENT_VERSION:-2.10.0}"
DEST="docker/libs/opentelemetry-javaagent.jar"
URL="https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${VERSION}/opentelemetry-javaagent.jar"

mkdir -p "$(dirname "$DEST")"
echo "Downloading OTel Java agent v${VERSION}..."
curl -fsSL -o "$DEST" "$URL"
echo "Saved to $DEST ($(du -h "$DEST" | cut -f1))"
