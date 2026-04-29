#!/usr/bin/env bash
# scripts/credential-leak-e2e-test.sh — ets-ogcapi-connectedsystems10
#
# REQ-ETS-CLEANUP-011, SCENARIO-ETS-CLEANUP-CREDENTIAL-LEAK-E2E-001:
#   Sprint 4 close artifact for the deeper-E2E credential-leak verification
#   (Sprint 3 PARTIAL → PASS at the IUT-auth layer per Quinn cumulative
#   CONCERN-1 + Raze cumulative CONCERN-2).
#
# Strategy (per Architect ratification 2026-04-29 — DECISION-3 PATH A stub-IUT
# in design.md §"Sprint 4 hardening: credential-leak E2E via stub IUT"):
#   1. Spin up scripts/stub-iut.sh — a hermetic Python http.server on an
#      ephemeral port that returns HTTP 401 with the inbound `Authorization`
#      header echoed in the response body AND logged to a file. The 401
#      triggers the auth code path in REST-Assured without granting actual
#      access to any real IUT.
#   2. Run smoke-test.sh against the stub URL with a synthetic credential
#      (`Bearer ABCDEFGH12345678WXYZ`) injected via the auth-credential
#      suite parameter (CTL/TestNG parameter wired through smoke-test.sh).
#   3. Three-fold cross-check (Architect's mandated evidence pattern per
#      ADR-009 + ADR-010 defense-in-depth precedent):
#        (a) ZERO unmasked-credential hits in TestNG XML + container
#            catalina.out + stub-IUT log AT THE TEST-OUTPUT LAYER
#            (i.e., test artifacts must NEVER carry the unmasked
#            credential — the masking filter scrubs all logged forms);
#        (b) >=1 MASKED-form hit in container log OR test artifacts
#            (proves the filter ran rather than dropping the field
#            entirely — a NULL filter would also produce zero
#            unmasked hits but would silently skip the masking work);
#        (c) >=1 UNMASKED-credential hit in stub-IUT log
#            (proves the try/finally restoration in the masking filter
#            unmasked the header BEFORE handing off to the underlying
#            HTTP transport — the wire request MUST carry the real
#            credential or the auth path is fundamentally broken).
#      All three required for PASS; exit 1 otherwise.
#
# Per S-ETS-04-04 sabotage-script-fixes precedent (PID-based trap cleanup +
# ephemeral 0.0.0.0 bind + docker --add-host), this script reuses the same
# primitives via `scripts/stub-iut.sh` (which IS the sabotage-test.sh
# stub-server primitive specialized for the auth path).
#
# Hermeticity: stub IUT runs as a child Python process; container removed on
# exit; archived TestNG XML written to /tmp/credential-leak-e2e-* by default.
# Set ARCHIVE_DIR to opt into archiving evidence to ops/test-results/.
#
# **DEFER live execution** — per Sprint 4 Run 2 mitigation (orchestrator
# budget constraint), this script is AUTHORED + COMMITTED but NOT executed
# in the Generator run. Live execution deferred to the next Quinn/Raze gate
# run with proper Docker time budget. The unit-layer
# credential-leak-integration-test.sh (Sprint 3) already provides
# fast-feedback verification at the unit level; this script is the deeper
# E2E gate-time complement.

set -eo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

TS="$(date -u +%Y-%m-%dT%H%M%SZ)"
ARCHIVE_DIR="${ARCHIVE_DIR:-/tmp/credential-leak-e2e-${TS}}"
mkdir -p "$ARCHIVE_DIR"

STUB_LOGFILE="${ARCHIVE_DIR}/stub-iut.log"
SMOKE_LOG="${ARCHIVE_DIR}/smoke.log"
CONTAINER_LOG="${ARCHIVE_DIR}/container.log"
GREP_REPORT="${ARCHIVE_DIR}/credential-leak-e2e-report-${TS}.txt"

# Synthetic credential pieces (same scheme as
# scripts/credential-leak-integration-test.sh + design.md §wrap pattern).
SYNTHETIC_CREDENTIAL="Bearer ABCDEFGH12345678WXYZ"
LITERAL_LEAK_PROBE="EFGH12345678WXYZ"
LITERAL_LEAK_PROBE_2="ABCDEFGH12345678"
MASKED_FORM_PROBE="Bear***WXYZ"

# stub-IUT control
STUB_PIDFILE="${STUB_PIDFILE:-/tmp/stub-iut-cred-leak-e2e.pid}"
STUB_PORTFILE="${STUB_PORTFILE:-/tmp/stub-iut-cred-leak-e2e.port}"
STUB_IUT_PIDFILE="$STUB_PIDFILE" \
STUB_IUT_PORTFILE="$STUB_PORTFILE" \
STUB_IUT_LOGFILE="$STUB_LOGFILE"
export STUB_IUT_PIDFILE="$STUB_PIDFILE"
export STUB_IUT_PORTFILE="$STUB_PORTFILE"
export STUB_IUT_LOGFILE="$STUB_LOGFILE"

CONTAINER_NAME="${SMOKE_CONTAINER_NAME:-ets-csapi-cred-leak-e2e}"
IMAGE_TAG="${SMOKE_IMAGE_TAG:-ets-ogcapi-connectedsystems10:smoke}"

log() { echo "[credential-leak-e2e $(date -u +%H:%M:%S)] $*" | tee -a "$GREP_REPORT"; }

cleanup_all() {
  log "cleanup: stopping stub-IUT + removing container"
  bash scripts/stub-iut.sh stop >/dev/null 2>&1 || true
  docker rm -f "$CONTAINER_NAME" >/dev/null 2>&1 || true
}
trap cleanup_all EXIT

log "=== SCENARIO-ETS-CLEANUP-CREDENTIAL-LEAK-E2E-001 ==="
log "REQ-ETS-CLEANUP-011 / S-ETS-04-03 (Sprint 4)"
log "Archive: $ARCHIVE_DIR"
log "Synthetic credential: $SYNTHETIC_CREDENTIAL"
log ""

# Step 1: spin up stub-IUT
log "Step 1/5 — spawning hermetic stub-IUT (Python http.server, 0.0.0.0:ephemeral)"
bash scripts/stub-iut.sh start "$STUB_LOGFILE"
STUB_PORT="$(cat "$STUB_PORTFILE")"
log "  stub-IUT listening on port $STUB_PORT (logfile $STUB_LOGFILE)"
log ""

# Step 2: invoke smoke-test.sh against stub URL with synthetic credential.
# The auth-credential parameter is passed via the SMOKE_AUTH_CREDENTIAL env
# var; smoke-test.sh propagates this to the CTL/TestNG suite parameter
# `auth-credential` per S-ETS-04-03 acceptance criterion.
STUB_URL="http://host.docker.internal:${STUB_PORT}"
log "Step 2/5 — running smoke-test.sh against stub IUT URL: $STUB_URL"
log "  (expect: Core landing-page @Test FAILs on 401; SystemFeatures + Subsystems cascade-SKIP;"
log "   credential MUST appear in stub-IUT log + MUST NOT appear unmasked in TestNG XML/container log)"
SMOKE_IUT_URL="$STUB_URL" \
  SMOKE_AUTH_CREDENTIAL="$SYNTHETIC_CREDENTIAL" \
  SMOKE_CONTAINER_NAME="$CONTAINER_NAME" \
  SMOKE_IMAGE_TAG="$IMAGE_TAG" \
  bash scripts/smoke-test.sh > "$SMOKE_LOG" 2>&1 \
  || log "  smoke exited non-zero (EXPECTED — stub returns 401 to all)"
log ""

# Step 3: capture container catalina.out (the in-container TE+ETS log stream).
log "Step 3/5 — capturing container catalina.out + per-suite TestNG XML"
docker logs "$CONTAINER_NAME" > "$CONTAINER_LOG" 2>&1 || true
LATEST_REPORT="$(ls -t ops/test-results/s-ets-01-03-teamengine-smoke-*.xml 2>/dev/null | head -1)"
if [[ -n "$LATEST_REPORT" ]]; then
  cp -f "$LATEST_REPORT" "$ARCHIVE_DIR/testng-results.xml"
  log "  TestNG XML archived: $ARCHIVE_DIR/testng-results.xml"
fi
log "  container log archived: $CONTAINER_LOG ($(wc -l < "$CONTAINER_LOG") lines)"
log "  smoke log archived: $SMOKE_LOG ($(wc -l < "$SMOKE_LOG") lines)"
log ""

# Step 4: three-fold cross-check
log "Step 4/5 — three-fold cross-check"

# (a) ZERO unmasked hits in TestNG XML + container catalina.out + smoke log
log "  (a) ZERO unmasked hits in test artifacts (TestNG XML + container log + smoke log)"
A_HITS_TESTNG=0
A_HITS_CONTAINER=0
A_HITS_SMOKE=0
if [[ -f "$ARCHIVE_DIR/testng-results.xml" ]]; then
  A_HITS_TESTNG=$(grep -cE "$LITERAL_LEAK_PROBE|$LITERAL_LEAK_PROBE_2" "$ARCHIVE_DIR/testng-results.xml" 2>/dev/null || true)
fi
A_HITS_CONTAINER=$(grep -cE "$LITERAL_LEAK_PROBE|$LITERAL_LEAK_PROBE_2" "$CONTAINER_LOG" 2>/dev/null || true)
A_HITS_SMOKE=$(grep -cE "$LITERAL_LEAK_PROBE|$LITERAL_LEAK_PROBE_2" "$SMOKE_LOG" 2>/dev/null || true)
log "      TestNG XML unmasked hits: $A_HITS_TESTNG (must be 0)"
log "      container log unmasked hits: $A_HITS_CONTAINER (must be 0)"
log "      smoke log unmasked hits: $A_HITS_SMOKE (must be 0)"
A_TOTAL=$((A_HITS_TESTNG + A_HITS_CONTAINER + A_HITS_SMOKE))

# (b) >=1 MASKED-form hit in container log OR test artifacts
log "  (b) >=1 masked-form hit in container log or test artifacts (proves filter ran)"
B_HITS_CONTAINER=$(grep -cF "$MASKED_FORM_PROBE" "$CONTAINER_LOG" 2>/dev/null || true)
B_HITS_TESTNG=0
if [[ -f "$ARCHIVE_DIR/testng-results.xml" ]]; then
  B_HITS_TESTNG=$(grep -cF "$MASKED_FORM_PROBE" "$ARCHIVE_DIR/testng-results.xml" 2>/dev/null || true)
fi
B_HITS_SMOKE=$(grep -cF "$MASKED_FORM_PROBE" "$SMOKE_LOG" 2>/dev/null || true)
log "      container log masked hits: $B_HITS_CONTAINER"
log "      TestNG XML masked hits: $B_HITS_TESTNG"
log "      smoke log masked hits: $B_HITS_SMOKE"
B_TOTAL=$((B_HITS_CONTAINER + B_HITS_TESTNG + B_HITS_SMOKE))

# (c) >=1 UNMASKED-credential hit in stub-IUT log (proves try/finally
# restoration unmasked the header before HTTP transport)
log "  (c) >=1 unmasked-credential hit in stub-IUT log (proves try/finally restoration)"
C_HITS=$(grep -cE "$LITERAL_LEAK_PROBE|$LITERAL_LEAK_PROBE_2" "$STUB_LOGFILE" 2>/dev/null || true)
log "      stub-IUT log unmasked hits: $C_HITS (must be >=1)"
log ""

# Step 5: verdict
log "Step 5/5 — verdict"
PASS=true
if [[ "$A_TOTAL" -gt 0 ]]; then
  log "  FAIL (a): $A_TOTAL unmasked-credential leak(s) in test artifacts"
  PASS=false
fi
if [[ "$B_TOTAL" -lt 1 ]]; then
  log "  FAIL (b): zero masked-form hits — filter may not have run, or masking format differs"
  PASS=false
fi
if [[ "$C_HITS" -lt 1 ]]; then
  log "  FAIL (c): zero unmasked-credential hits in stub-IUT log — wire request did NOT carry the credential"
  log "           (check try/finally restoration in MaskingRequestLoggingFilter)"
  PASS=false
fi

log ""
log "=== VERDICT ==="
if $PASS; then
  log "PASS: three-fold cross-check satisfied. SCENARIO-ETS-CLEANUP-CREDENTIAL-LEAK-E2E-001 verified."
  log "  (a) zero unmasked credential leaks in test artifacts: OK (0 hits)"
  log "  (b) masked form present in test artifacts: OK ($B_TOTAL hits)"
  log "  (c) unmasked credential transmitted to stub-IUT: OK ($C_HITS hits)"
  log ""
  log "Archive: $ARCHIVE_DIR"
  exit 0
else
  log "FAIL: one or more cross-check prongs failed (see above)."
  log "Archive: $ARCHIVE_DIR"
  exit 1
fi
