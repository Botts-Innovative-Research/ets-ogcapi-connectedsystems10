#!/usr/bin/env bash
# REQ-ETS-PART2-013: safe entrypoint for the populated local OSH workflow.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
exec python3 "$REPO_ROOT/scripts/local_osh_populated_e2e.py"
