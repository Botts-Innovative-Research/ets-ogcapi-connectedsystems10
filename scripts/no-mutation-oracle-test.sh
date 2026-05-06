#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ORACLE="${SCRIPT_DIR}/no-mutation-oracle.py"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

IUT="https://api.georobotix.io/ogc/t18/api"

cat >"${TMP_DIR}/single-pass.log" <<'LOG'
Request: POST http://localhost:8081/teamengine/rest/suites/ogcapi-connectedsystems10/run
Request: GET https://api.georobotix.io/ogc/t18/api/conformance
Request: OPTIONS https://api.georobotix.io/ogc/t18/api/systems
LOG
"$ORACLE" "${TMP_DIR}/single-pass.log" "$IUT" >/dev/null

cat >"${TMP_DIR}/single-fail.log" <<'LOG'
Request: POST https://api.georobotix.io/ogc/t18/api/systems
LOG
if "$ORACLE" "${TMP_DIR}/single-fail.log" "$IUT" >/dev/null 2>&1; then
  echo "expected single-line IUT POST to fail" >&2
  exit 1
fi

cat >"${TMP_DIR}/pair-fail.log" <<'LOG'
Request method: PUT
Request URI: https://api.georobotix.io/ogc/t18/api/systems/abc
LOG
if "$ORACLE" "${TMP_DIR}/pair-fail.log" "$IUT" >/dev/null 2>&1; then
  echo "expected pair-format IUT PUT to fail" >&2
  exit 1
fi

cat >"${TMP_DIR}/patch-fail.log" <<'LOG'
Request: PATCH https://api.georobotix.io/ogc/t18/api/systems/abc
LOG
if "$ORACLE" "${TMP_DIR}/patch-fail.log" "$IUT" >/dev/null 2>&1; then
  echo "expected single-line IUT PATCH to fail" >&2
  exit 1
fi

cat >"${TMP_DIR}/empty.log" <<'LOG'
No request log lines here.
LOG
if "$ORACLE" "${TMP_DIR}/empty.log" "$IUT" >/dev/null 2>&1; then
  echo "expected inconclusive log to fail" >&2
  exit 1
fi

echo "no-mutation oracle self-test PASS"
