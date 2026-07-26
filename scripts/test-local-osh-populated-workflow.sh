#!/usr/bin/env bash
# Behavioral safety gate for REQ-ETS-PART2-013 / S-ETS-44-01.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

PYTHONPYCACHEPREFIX=/tmp/ets-csapi-populated-workflow-pycache \
  python3 scripts/test_local_osh_populated_workflow.py
