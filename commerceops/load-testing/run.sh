#!/usr/bin/env bash
# Runs a k6 script against a running CommerceOps backend, using the official
# k6 Docker image if k6 isn't installed locally.
#
# Usage:
#   ./run.sh mixed_read_load.js [-e VUS=50 -e DURATION=2m ...]
#   ./run.sh payment_concurrency.js -e VUS=20
#   BASE_URL=http://localhost:18080 ./run.sh stock_concurrency.js -e VUS=30 -e PRODUCT_ID=5

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCRIPT_NAME="${1:-}"
shift || true

if [[ -z "$SCRIPT_NAME" ]]; then
    echo "Usage: $0 <script.js> [k6 args...]" >&2
    exit 1
fi

BASE_URL="${BASE_URL:-http://localhost:8080}"

if command -v k6 >/dev/null 2>&1; then
    BASE_URL="$BASE_URL" k6 run "$@" "$SCRIPT_DIR/scripts/$SCRIPT_NAME"
else
    echo "k6 not found locally, running via Docker (grafana/k6)..."
    docker run --rm -i \
        --network host \
        -e BASE_URL="$BASE_URL" \
        -v "$SCRIPT_DIR/scripts:/scripts" \
        grafana/k6:0.54.0 run "$@" "/scripts/$SCRIPT_NAME"
fi
