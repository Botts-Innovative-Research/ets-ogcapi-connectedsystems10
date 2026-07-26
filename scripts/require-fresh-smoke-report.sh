#!/usr/bin/env bash
# REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-DEPENDENCY-CASCADE-001.

set -euo pipefail

REPORT_DIR="${1:?usage: require-fresh-smoke-report.sh REPORT_DIR START_MARKER}"
START_MARKER="${2:?usage: require-fresh-smoke-report.sh REPORT_DIR START_MARKER}"

[[ -d "$REPORT_DIR" ]] || {
  echo "FATAL: smoke report directory does not exist: $REPORT_DIR" >&2
  exit 1
}
[[ -f "$START_MARKER" ]] || {
  echo "FATAL: smoke start marker does not exist: $START_MARKER" >&2
  exit 1
}

mapfile -d '' REPORTS < <(
  find "$REPORT_DIR" -maxdepth 1 -type f \
    -name 's-ets-01-03-teamengine-smoke-*.xml' \
    -newer "$START_MARKER" -print0
)

if [[ "${#REPORTS[@]}" -ne 1 ]]; then
  echo "FATAL: expected exactly one fresh smoke report in $REPORT_DIR; found ${#REPORTS[@]}" >&2
  exit 1
fi

printf '%s\n' "${REPORTS[0]}"
