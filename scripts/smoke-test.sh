#!/usr/bin/env bash
# scripts/smoke-test.sh — ets-ogcapi-connectedsystems10
#
# REQ-ETS-TEAMENGINE-005, SCENARIO-ETS-CORE-SMOKE-001:
#   Build the Docker image, launch the container, wait for healthcheck, run
#   the CS API Core suite against the GeoRobotix demo IUT, archive the TestNG
#   XML report + container log to ops/test-results/, and exit 0 only when:
#     - the TestNG report is non-empty (total > 0)
#     - the report has zero failed/error tests (every @Test PASS or SKIP)
#     - the container's STARTUP log contains zero ERROR/SEVERE entries from
#       suite registration (later runtime SEVERE entries unrelated to suite
#       registration — e.g. Tomcat's "utf-8 encoding" warning during HTML
#       error-page rendering — are tolerated).
#
# Idempotent: every invocation tears down its own container before starting
# (container name `ets-csapi-smoke`), and stages a fresh report. Re-running
# back-to-back leaves clean state.
#
# Port handling: prefers host port 8081 (canonical, per REQ-ETS-TEAMENGINE-004
# + docker-compose.yml). If 8081 is busy (the WSL2 dev box ships a
# `field-hub-osh` container on 8081 as of 2026-04-28), falls back to 8082.
# Override via $SMOKE_PORT.
#
# Sprint 2 S-ETS-02-05 simplification (per ADR-009 + Raze s03 CONCERN-2/3):
#   - DROPPED host `mvn -B clean package -DskipTests` (now baked into Dockerfile
#     stage 1)
#   - DROPPED host `mvn dependency:copy-dependencies` (now baked into Dockerfile
#     stage 1)
#   - Smoke now requires ONLY Docker; no host JDK / Maven required.
#   - Step 5 metadata parse tightened (Raze s03 CONCERN-4): asserts ets-code +
#     version + title from /rest/suites/<code> JSON-or-XML metadata, not just
#     the etscode element.

set -eo pipefail
export DOCKER_BUILDKIT=1   # ADR-009 §Notes — required for --mount=type=cache

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

CONTAINER_NAME="${SMOKE_CONTAINER_NAME:-ets-csapi-smoke}"
IMAGE_TAG="${SMOKE_IMAGE_TAG:-ets-ogcapi-connectedsystems10:smoke}"
IUT_URL="${SMOKE_IUT_URL:-https://api.georobotix.io/ogc/t18/api}"
ETS_CODE="ogcapi-connectedsystems10"
TE_USER="${SMOKE_TE_USER:-ogctest}"
TE_PASS="${SMOKE_TE_PASS:-ogctest}"
HEALTH_TIMEOUT_S="${SMOKE_HEALTH_TIMEOUT_S:-180}"
RUN_TIMEOUT_S="${SMOKE_RUN_TIMEOUT_S:-600}"

DATE_STAMP="$(date -u +%Y-%m-%d)"
# REQ-ETS-CLEANUP-014 (Sprint 5 S-ETS-05-02): SMOKE_OUTPUT_DIR override.
#   Default: ${REPO_ROOT}/ops/test-results (preserves Sprint 1-4 behaviour for
#   normal developer runs). Gate runs MUST set
#   SMOKE_OUTPUT_DIR=/tmp/<role>-fresh-sprint5/test-results so artifacts never
#   touch the user worktree (closes the Sprint 2 + Sprint 4 worktree-pollution
#   recurrence — Quinn GAP-2 + Raze CONCERN). Absolute or relative path both
#   honoured (relative paths resolve against the current working directory).
ARCHIVE_DIR="${SMOKE_OUTPUT_DIR:-${REPO_ROOT}/ops/test-results}"
REPORT_XML="${ARCHIVE_DIR}/s-ets-01-03-teamengine-smoke-${DATE_STAMP}.xml"
LOG_FILE="${ARCHIVE_DIR}/s-ets-01-03-teamengine-container-${DATE_STAMP}.log"
mkdir -p "$ARCHIVE_DIR"

log() { echo "[smoke-test $(date -u +%H:%M:%S)] $*"; }
die() { echo "[smoke-test FATAL] $*" >&2; cleanup_silent; exit 1; }

cleanup_silent() {
  docker rm -f "$CONTAINER_NAME" >/dev/null 2>&1 || true
}
trap cleanup_silent EXIT

pick_port() {
  local p="${SMOKE_PORT:-}"
  if [[ -n "$p" ]]; then echo "$p"; return; fi
  for candidate in 8081 8082 8083; do
    if ! ss -ltn 2>/dev/null | awk '{print $4}' | grep -qE ":${candidate}$"; then
      echo "$candidate"; return
    fi
  done
  echo 8082  # fallback
}

# ---------- Step 1: sanity check (multi-stage Dockerfile bakes mvn package + deps)
# Per ADR-009 (Sprint 2 S-ETS-02-05), the Dockerfile's builder stage runs the full
# Maven lifecycle inside the container with a BuildKit cache mount. Host mvn is no
# longer required — `git clone && bash scripts/smoke-test.sh` is sufficient.
log "step 1/8 — sanity check (multi-stage Dockerfile bakes mvn lifecycle)"
[[ -f Dockerfile ]] || die "Dockerfile missing — run from repo root"
[[ -f pom.xml ]] || die "pom.xml missing — run from repo root"

# ---------- Step 2: docker build (BuildKit; cold ~5-6min, warm ~30-90s)
log "step 2/8 — docker build $IMAGE_TAG (BuildKit ${DOCKER_BUILDKIT})"
docker build -t "$IMAGE_TAG" . >/dev/null 2>&1 || {
  log "build failed; rerunning with output for diagnostics"
  docker build -t "$IMAGE_TAG" .
  die "docker build returned non-zero"
}

# ---------- Step 3: launch container on a free port
SMOKE_PORT="$(pick_port)"
log "step 3/8 — docker run on host port ${SMOKE_PORT}"
cleanup_silent
# Sprint 4 S-ETS-04-04 fix (b): --add-host=host.docker.internal:host-gateway
# lets the container resolve host.docker.internal to the host's Docker
# bridge IP. Docker Desktop (macOS/Windows) auto-injects this; Docker Engine
# on Linux WITHOUT Desktop does NOT — the flag is required for the bash
# sabotage-test.sh stub-server primitive to be reachable from inside the
# container. Requires Docker 20.10+ (host-gateway keyword).
docker run -d --name "$CONTAINER_NAME" \
  --add-host=host.docker.internal:host-gateway \
  -p "${SMOKE_PORT}:8080" "$IMAGE_TAG" >/dev/null \
  || die "docker run failed (port $SMOKE_PORT)"

# ---------- Step 4: wait for /teamengine/ to return HTTP 200
log "step 4/8 — waiting for TeamEngine healthcheck (timeout ${HEALTH_TIMEOUT_S}s)"
deadline=$(( $(date +%s) + HEALTH_TIMEOUT_S ))
while (( $(date +%s) < deadline )); do
  status=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:${SMOKE_PORT}/teamengine/" || echo 000)
  if [[ "$status" == "200" ]]; then
    log "  healthcheck PASS at $(date -u +%H:%M:%S)"
    break
  fi
  sleep 5
done
[[ "$status" == "200" ]] || die "healthcheck never reached HTTP 200 (last=$status)"

# ---------- Step 5: verify our suite is registered (tightened per Raze s03 CONCERN-4)
# Sprint 2 tightening: assert ets-code AND version-from-pom AND title-from-pom in the
# /rest/suites listing. Mismatch on any of the three is a FATAL — surfaces drift
# between the deployed jar metadata and the pom.xml expectations.
EXPECTED_VERSION="${SMOKE_EXPECTED_VERSION:-0.1-SNAPSHOT}"
EXPECTED_TITLE_FRAGMENT="${SMOKE_EXPECTED_TITLE_FRAGMENT:-ogcapi-connectedsystems10}"
log "step 5/8 — verifying ${ETS_CODE} is registered (code + version + title metadata)"
suites_xml=$(curl -fsS -u "${TE_USER}:${TE_PASS}" -H "Accept: application/xml" \
    "http://localhost:${SMOKE_PORT}/teamengine/rest/suites") \
  || die "/rest/suites query failed"

if ! echo "$suites_xml" | grep -q "<etscode>${ETS_CODE}</etscode>"; then
  echo "$suites_xml"
  die "${ETS_CODE} not in suite list (etscode element missing)"
fi
if ! echo "$suites_xml" | grep -q "<version>${EXPECTED_VERSION}</version>"; then
  echo "$suites_xml"
  die "${ETS_CODE}: version mismatch (expected '${EXPECTED_VERSION}'); see /rest/suites response above"
fi
if ! echo "$suites_xml" | grep -qF "${EXPECTED_TITLE_FRAGMENT}"; then
  echo "$suites_xml"
  die "${ETS_CODE}: title metadata missing fragment '${EXPECTED_TITLE_FRAGMENT}'; see /rest/suites response above"
fi
log "  suite registered (code=${ETS_CODE} version=${EXPECTED_VERSION} title contains '${EXPECTED_TITLE_FRAGMENT}')"

# ---------- Step 6: invoke the suite against IUT
# REQ-ETS-CLEANUP-013 (Sprint 5 S-ETS-05-01 GAP-1 wedge fix):
#   When SMOKE_AUTH_CREDENTIAL is non-empty, propagate it as the
#   `auth-credential` TestNG suite parameter via an extra
#   `--data-urlencode` arg. The Java SuiteFixtureListener reads it and
#   configures REST-Assured's default request specification with an
#   `Authorization: <SMOKE_AUTH_CREDENTIAL>` header (so the
#   MaskingRequestLoggingFilter is exercised and the deeper-E2E
#   credential-leak verification in scripts/credential-leak-e2e-test.sh
#   actually proves end-to-end propagation rather than passing by
#   default-on-zero-credential accident). Closes Sprint 4 GAP-1.
#
# Backward-compat: when SMOKE_AUTH_CREDENTIAL is unset/empty, no
# auth-credential param is sent — Sprint 1-4 unauthenticated smoke against
# GeoRobotix continues to behave identically.
AUTH_CRED_ARGS=()
if [[ -n "${SMOKE_AUTH_CREDENTIAL:-}" ]]; then
  AUTH_CRED_ARGS+=(--data-urlencode "auth-credential=${SMOKE_AUTH_CREDENTIAL}")
  log "  SMOKE_AUTH_CREDENTIAL set (length=${#SMOKE_AUTH_CREDENTIAL}) — propagating as auth-credential suite parameter (REQ-ETS-CLEANUP-013)"
fi
log "step 6/8 — POST suite/${ETS_CODE}/run iut=${IUT_URL}"
http_code=$(curl -s -u "${TE_USER}:${TE_PASS}" -G \
    "http://localhost:${SMOKE_PORT}/teamengine/rest/suites/${ETS_CODE}/run" \
    --data-urlencode "iut=${IUT_URL}" \
    "${AUTH_CRED_ARGS[@]}" \
    -H "Accept: application/xml" \
    -o "$REPORT_XML" \
    -w "%{http_code}" \
    -m "$RUN_TIMEOUT_S") \
  || die "suite invocation curl failed"
[[ "$http_code" == "200" ]] || {
  echo "--- TestNG response body (HTTP $http_code) ---"
  head -50 "$REPORT_XML"
  die "suite invocation HTTP $http_code"
}

# ---------- Step 7: parse TestNG report, archive, validate
log "step 7/8 — validating TestNG report → $REPORT_XML"
[[ -s "$REPORT_XML" ]] || die "TestNG report is empty"

extract_attr() {
  python3 -c "
import sys, re
m = re.search(r'<testng-results[^>]*\\b$1=\"(\\d+)\"', open(sys.argv[1]).read())
print(m.group(1) if m else 'NA')
" "$REPORT_XML"
}

total=$(extract_attr total)
passed=$(extract_attr passed)
failed=$(extract_attr failed)
skipped=$(extract_attr skipped)

log "  TestNG: total=$total passed=$passed failed=$failed skipped=$skipped"

# Sprint 6 S-ETS-06-01 (REQ-ETS-CLEANUP-016) — container log capture timing fix.
# Capture catalina.out from the container BEFORE any die() trigger that would
# fire cleanup_silent (which removes the container, after which `docker logs`
# returns nothing). The downstream credential-leak-e2e three-fold cross-check
# prong (a)+(b) grep against this log; pre-Sprint-6 the log was empty because
# the die() path tore down the container first, making prong (a) pass
# vacuously and prong (b) miss the masked form. The capture itself is harmless
# (it does not stop the container; cleanup_silent on EXIT trap still tears
# down). Capture is non-fatal (`|| true`) so a transient docker error does
# not mask the real test verdict below.
docker logs "$CONTAINER_NAME" > "$LOG_FILE" 2>&1 || true

[[ "$total" =~ ^[0-9]+$ ]] || die "could not parse <testng-results total=...>"
(( total > 0 )) || die "TestNG report total=0 (no @Test methods ran)"
(( failed == 0 )) || die "TestNG report has failed=$failed (>0); see $REPORT_XML"

# ---------- Step 8: scan container logs for SEVERE during STARTUP
log "step 8/8 — scanning container startup log for ERROR/SEVERE"
# Container log already captured above (Sprint 6 timing fix); refresh in case
# additional log lines were written between the prior capture and this point.
docker logs "$CONTAINER_NAME" > "$LOG_FILE" 2>&1 || true

# Only inspect lines from container init through the "Server startup" line.
startup_block=$(awk '/Server startup in/{print; exit} {print}' "$LOG_FILE")
startup_severe=$(echo "$startup_block" | grep -E "^[0-9]{2}-[A-Za-z]{3}-[0-9]{4}.*(SEVERE|ERROR)" \
                  | grep -v "did not find a matching property\|maxActive is not used in DBCP2\|encoding \['utf-8'\]" \
                  || true)

if [[ -n "$startup_severe" ]]; then
  echo "--- startup ERROR/SEVERE entries ---"
  echo "$startup_severe"
  die "container startup logged ERROR/SEVERE entries during suite registration"
fi
log "  zero startup ERROR/SEVERE"

log "SMOKE PASS: ${total}/${total} @Test methods on $IUT_URL via TeamEngine"
log "  report: $REPORT_XML"
log "  log:    $LOG_FILE"

cleanup_silent
trap - EXIT
exit 0
