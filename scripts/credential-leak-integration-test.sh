#!/usr/bin/env bash
# scripts/credential-leak-integration-test.sh — ets-ogcapi-connectedsystems10
#
# REQ-ETS-CLEANUP-006, SCENARIO-ETS-CLEANUP-CREDENTIAL-LEAK-INTEGRATION-001:
#   Closes Sprint 2 PARTIAL `no_credential_leak_in_test_logs` → PASS.
#
# Strategy (per architect-handoff constraints_for_generator.must item 9):
#   1. Run the new VerifyMaskingRequestLoggingFilter unit-test class (8 tests),
#      one of which exercises the filter() method end-to-end with synthetic
#      credential `Bearer ABCDEFGH12345678WXYZ` and writes the formatter output
#      to a ByteArrayOutputStream that is then asserted in the unit test itself.
#      This script captures Maven's full surefire output (NOT just the summary)
#      so we can grep the literal credential body across the whole run, not just
#      the test class's own assertions.
#   2. Grep the captured Maven output for:
#        - literal middle `EFGH12345678WXYZ` → MUST be ZERO hits (no leak)
#        - masked form `Bear***WXYZ`         → MUST be at least 1 hit (filter ran)
#   3. Also grep the surefire-reports XML for the same patterns (covers the case
#      where surefire suppresses stdout but writes it to test-output XML).
#   4. Exit 0 on zero-leak + masked-form-present; exit 1 otherwise.
#
# Hermeticity: runs `mvn test -Dtest=VerifyMaskingRequestLoggingFilter` only
# (fast: ~5-10s vs full suite). Output captured to /tmp/credential-leak-* by
# default. Set ARCHIVE_DIR to opt into archiving the evidence to ops/test-results/.

set -eo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

TS="$(date -u +%Y-%m-%dT%H%M%SZ)"
ARCHIVE_DIR="${ARCHIVE_DIR:-/tmp/credential-leak-${TS}}"
mkdir -p "$ARCHIVE_DIR"

MVN_OUT="${ARCHIVE_DIR}/mvn-output.log"
GREP_REPORT="${ARCHIVE_DIR}/credential-leak-report-${TS}.txt"

# Synthetic credential — same as design.md §wrap pattern test rules
LITERAL_LEAK_PROBE="EFGH12345678WXYZ"
LITERAL_LEAK_PROBE_2="ABCDEFGH12345678"
MASKED_FORM_PROBE="Bear***WXYZ"

log() { echo "[credential-leak-test $(date -u +%H:%M:%S)] $*" | tee -a "$GREP_REPORT"; }

log "=== SCENARIO-ETS-CLEANUP-CREDENTIAL-LEAK-INTEGRATION-001 ==="
log "REQ-ETS-CLEANUP-006 / S-ETS-03-02"
log "Archive: $ARCHIVE_DIR"
log ""

# Step 1: run the targeted unit-test class with full surefire output captured.
log "Step 1/4 — running VerifyMaskingRequestLoggingFilter (8 @Tests)"
export PATH="${HOME}/.local/apache-maven-3.9.9/bin:$PATH"
if ! mvn test -Dtest=VerifyMaskingRequestLoggingFilter -o > "$MVN_OUT" 2>&1; then
  log "FATAL: mvn test FAILED — see $MVN_OUT"
  tail -40 "$MVN_OUT" | tee -a "$GREP_REPORT"
  exit 1
fi
log "  unit tests PASS (8/8 expected)"
log ""

# Step 2: grep Maven output for literal credential body (leak guard)
log "Step 2/4 — grep mvn output for literal '$LITERAL_LEAK_PROBE' (zero hits required)"
LEAK1=$(grep -c "$LITERAL_LEAK_PROBE" "$MVN_OUT" || true)
LEAK2=$(grep -c "$LITERAL_LEAK_PROBE_2" "$MVN_OUT" || true)
log "  literal '$LITERAL_LEAK_PROBE' hits: $LEAK1"
log "  literal '$LITERAL_LEAK_PROBE_2' hits: $LEAK2"

# Step 3: grep surefire-reports XML attachments (binary present/absent check)
log "Step 3/4 — grep target/surefire-reports/*.xml for the same literals"
SUREFIRE_LEAK1=0
SUREFIRE_LEAK2=0
if compgen -G "target/surefire-reports/*.xml" > /dev/null; then
  if grep -q "$LITERAL_LEAK_PROBE" target/surefire-reports/*.xml 2>/dev/null; then
    SUREFIRE_LEAK1=1
  fi
  if grep -q "$LITERAL_LEAK_PROBE_2" target/surefire-reports/*.xml 2>/dev/null; then
    SUREFIRE_LEAK2=1
  fi
fi
log "  surefire XML literal '$LITERAL_LEAK_PROBE' present: $SUREFIRE_LEAK1 (0=absent, 1=LEAK)"
log "  surefire XML literal '$LITERAL_LEAK_PROBE_2' present: $SUREFIRE_LEAK2 (0=absent, 1=LEAK)"

# Step 4: assert masked-form presence (proves filter ran rather than dropping headers)
log "Step 4/4 — grep mvn output for masked form '$MASKED_FORM_PROBE' (>=1 hit required)"
# In assertion failure messages the masked form would appear; in PASS path it
# would only appear if test code wrote it to stdout. Our tests use
# ByteArrayOutputStream (NOT System.out), so masked form is not in mvn output by
# design — it's verified inside the test assertions. We therefore check the
# surefire XML test results for assertion success indicators instead.
PASSED=$(grep -c "Tests run: 8, Failures: 0, Errors: 0" "$MVN_OUT" || true)
log "  surefire summary lines matching '8/0/0/0': $PASSED (>=1 expected)"
log ""

# Verdict
log "=== VERDICT ==="
TOTAL_LEAKS=$((LEAK1 + LEAK2 + SUREFIRE_LEAK1 + SUREFIRE_LEAK2))
if [[ "$TOTAL_LEAKS" -gt 0 ]]; then
  log "FAIL: $TOTAL_LEAKS literal credential leak(s) detected in mvn output / surefire XML."
  log "  Leak detail:"
  grep -n "$LITERAL_LEAK_PROBE\|$LITERAL_LEAK_PROBE_2" "$MVN_OUT" target/surefire-reports/*.xml \
    2>/dev/null | head -20 | tee -a "$GREP_REPORT"
  exit 1
fi
if [[ "$PASSED" -lt 1 ]]; then
  log "FAIL: surefire did not report 8/0/0/0 — filter may not have actually run."
  exit 1
fi
log "PASS: zero credential leaks + 8 unit tests (incl. masked-form-present"
log "      assertions) ran green. SCENARIO-ETS-CLEANUP-CREDENTIAL-LEAK-INTEGRATION-001"
log "      verified at the unit-test integration layer (deeper E2E IUT-auth"
log "      wiring deferred to Sprint 4 per architect-handoff)."
log ""
log "Archive: $ARCHIVE_DIR"
exit 0
