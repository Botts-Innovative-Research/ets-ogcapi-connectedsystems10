#!/usr/bin/env bash
# scripts/sabotage-test.sh — ets-ogcapi-connectedsystems10
#
# REQ-ETS-CLEANUP-005, SCENARIO-ETS-CLEANUP-DEPENDENCY-SKIP-LIVE-001 (Sprint 3
# default mode) +
# REQ-ETS-CLEANUP-015, SCENARIO-ETS-CLEANUP-SABOTAGE-TARGET-001 (Sprint 5
# --target=systemfeatures mode):
#
#   Behavioural verification that TestNG's group-dependency mechanism causes
#   downstream @Tests to SKIP (not FAIL/ERROR) when an upstream conformance
#   class fails. The script supports TWO sabotage modes:
#
#   --target=core (DEFAULT — backward compatible with Sprint 3 + 4):
#     Launch a Python HTTP-500 stub server, run smoke against the stub URL,
#     observe Core FAIL + SystemFeatures SKIP (one-level cascade). This is
#     the "approach (b) bash sabotage script" canonical CITE-SC-grade artifact
#     per ADR-010, complementing the structural-lint unit test
#     VerifyTestNGSuiteDependency.
#
#   --target=systemfeatures (Sprint 5 S-ETS-05-03 — NEW for two-level
#     cascade gate evidence; lets Quinn/Raze invoke without manual Java edits):
#     Copy the source tree to a temp directory, sed-patch the FIRST @Test of
#     SystemFeaturesTests.java to throw AssertionError unconditionally,
#     rebuild the Docker image from the temp tree (different IMAGE_TAG to
#     avoid clobbering the dev cache), and run smoke against the
#     real GeoRobotix IUT. Observe SystemFeatures FAIL (1) + SKIP (5) +
#     Subsystems SKIP (4) + Procedures SKIP (P) + Deployments SKIP (D); Core
#     and Common PASS. The original SystemFeaturesTests.java in the user's
#     worktree is NEVER modified — all sabotage happens in the temp clone.
#
# Strategy for --target=core (per ADR-010 §"Approach to sabotage" —
# stub-server preferred):
#   1. Launch a Python HTTP stub server bound to an ephemeral port that
#      returns HTTP 500 to every request (sabotages Core's landing-page
#      assertion).
#   2. Run the existing smoke pipeline against the stub URL (instead of
#      GeoRobotix). Core's first @Test fails on the GET /; the
#      <group name="systemfeatures" depends-on="core"/> declaration in
#      testng.xml then cascades SKIP to all SystemFeatures @Tests.
#   3. Parse the produced TestNG XML report. Assert:
#        (a) at least one Core @Test has status="FAIL" (sabotage worked)
#        (b) every SystemFeatures @Test has status="SKIP" (NOT "FAIL"/"PASS")
#   4. Exit 0 on correct cascading-SKIP behavior; exit 1 if SystemFeatures
#      @Tests show FAIL or PASS (which would mean the dependency wiring is
#      broken — exactly the regression this script catches).
#
# Strategy for --target=systemfeatures (Sprint 5 — REQ-ETS-CLEANUP-015):
#   1. Validate prerequisites: docker daemon reachable; SystemFeaturesTests.java
#      present at the expected path.
#   2. mktemp -d a working dir under SABOTAGE_TMPDIR.
#   3. cp -r the source tree (excluding .git, target/, node_modules/) into
#      the temp dir to keep the user worktree pristine.
#   4. Use sed to inject `throw new AssertionError("SABOTAGED by --target=
#      systemfeatures Sprint 5 S-ETS-05-03");` as the FIRST line of the
#      systemsCollectionReturns200 method body in the temp copy of
#      SystemFeaturesTests.java. Verify the patch landed (grep check).
#   5. Run scripts/smoke-test.sh from inside the temp dir with a unique
#      IMAGE_TAG and CONTAINER_NAME to avoid clobbering the dev cache. Smoke
#      builds the image, runs against GeoRobotix, archives the TestNG XML.
#   6. Parse the TestNG XML. Assert:
#        (a) Core @Tests all PASS (Core unaffected)
#        (b) Common @Tests all PASS (Common is independent)
#        (c) at least one SystemFeatures @Test has status="FAIL" (sabotage
#            worked)
#        (d) every Subsystems @Test has status="SKIP" (two-level cascade)
#        (e) every Procedures @Test has status="SKIP" (two-level cascade)
#        (f) every Deployments @Test has status="SKIP" (two-level cascade)
#   7. Restore via temp-dir-discard (the original tree is never touched —
#      verify worktree status post-run for documentation; if dirty, that's a
#      script bug, not a temporary-state leak).
#   8. Archive the cascade evidence XML for the audit trail.
#
# Hermeticity / worktree-pollution constraint (per ADR-010 §Risks +
# Sprint 3 contract worktree_pollution_constraint):
#   - Stub server runs as a child Python process; cleaned up via trap.
#   - Sabotaged TestNG XML written to /tmp/sabotage-fresh-<ts>/ by default.
#     Use SABOTAGE_ARCHIVE_DIR=ops/test-results/ to opt into archiving the
#     evidence to the repo-relative artifact location for Quinn/Raze gate
#     review (per ADR-010 §"Archive for gate review" lane).
#   - Container removed on exit (trap cleanup).
#   - Source tree (.../conformance/core/LandingPageTests.java) NEVER mutated
#     — testng.xml-mutation sabotage is the documented backup; this script
#     uses the stub-server primary path only.
#
# Port collision (ADR-010 §Risks STUB-SERVER-PORT-COLLISION-IN-CI +
# architect-handoff.surfaced_risks STUB-SERVER-PORT-COLLISION-IN-CI):
#   Stub binds to port 0 (OS-assigned ephemeral) via socket.bind; the
#   resolved port is read back from the Python child and exported to the
#   smoke pipeline as SMOKE_IUT_URL. No hardcoded port; no collision risk.
#
# This script is AUTHORED in Generator Run 1 (S-ETS-03-01) but LIVE
# EXECUTION is deferred to the next Quinn/Raze gate run with proper Docker
# time budget (per Sprint 3 mitigation plan — three prior sub-agents in this
# autonomous loop hit timeouts on Docker-rebuild loops; the unit test
# VerifyTestNGSuiteDependency provides fast-feedback verification in the
# meantime). Do NOT execute as part of `mvn test` or in CI's main smoke job;
# run as a separate `verify-dependency-skip` job per ADR-010 role-boundary
# table.

set -eo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

# Sprint 5 S-ETS-05-03 (REQ-ETS-CLEANUP-015): parse --target=<class> flag.
# Default (no flag, or --target=core) preserves Sprint 3 + 4 backward-compatible
# behaviour. --target=systemfeatures runs the temp-dir sed-patch + Docker
# rebuild + smoke + cascade-parse path.
SABOTAGE_TARGET="core"
for arg in "$@"; do
  case "$arg" in
    --target=core)
      SABOTAGE_TARGET="core"
      ;;
    --target=systemfeatures)
      SABOTAGE_TARGET="systemfeatures"
      ;;
    --target=*)
      echo "[sabotage-test FATAL] unsupported --target value: $arg" >&2
      echo "                       valid: --target=core (default) | --target=systemfeatures" >&2
      exit 2
      ;;
    -h|--help)
      echo "Usage: $(basename "$0") [--target=core|systemfeatures]"
      echo ""
      echo "  --target=core (default): HTTP-500 stub-server sabotage of Core; "
      echo "                           observe one-level SKIP cascade in SystemFeatures."
      echo "  --target=systemfeatures: temp-dir sed-patch sabotage of SystemFeatures; "
      echo "                           rebuild + smoke against GeoRobotix; observe "
      echo "                           two-level SKIP cascade in Subsystems + Procedures + "
      echo "                           Deployments. User worktree NEVER modified."
      exit 0
      ;;
    *)
      echo "[sabotage-test WARN] unknown argument ignored: $arg" >&2
      ;;
  esac
done

# Configuration with safe defaults
TS="$(date -u +%Y-%m-%dT%H%M%SZ)"
SABOTAGE_TMPDIR="${SABOTAGE_TMPDIR:-/tmp/sabotage-fresh-${TS}}"
SABOTAGE_ARCHIVE_DIR="${SABOTAGE_ARCHIVE_DIR:-${SABOTAGE_TMPDIR}}"
SABOTAGE_REPORT_XML="${SABOTAGE_ARCHIVE_DIR}/sprint-ets-03-dependency-skip-sabotage-${TS}.xml"
SABOTAGE_LOG="${SABOTAGE_ARCHIVE_DIR}/sprint-ets-03-dependency-skip-sabotage-${TS}.log"
mkdir -p "$SABOTAGE_ARCHIVE_DIR"

STUB_PIDFILE="${SABOTAGE_TMPDIR}/stub.pid"
STUB_PORTFILE="${SABOTAGE_TMPDIR}/stub.port"
mkdir -p "$SABOTAGE_TMPDIR"

CONTAINER_NAME="${SMOKE_CONTAINER_NAME:-ets-csapi-sabotage}"
IMAGE_TAG="${SMOKE_IMAGE_TAG:-ets-ogcapi-connectedsystems10:smoke}"

# Sprint 5 S-ETS-05-03: when --target=systemfeatures, override IMAGE_TAG +
# CONTAINER_NAME so the dev cache (smoke) is not clobbered by the sabotaged
# build artifacts.
if [[ "$SABOTAGE_TARGET" == "systemfeatures" ]]; then
  IMAGE_TAG="${SMOKE_IMAGE_TAG:-ets-ogcapi-connectedsystems10:sabotage-sf}"
  CONTAINER_NAME="${SMOKE_CONTAINER_NAME:-ets-csapi-sabotage-sf}"
  SABOTAGE_REPORT_XML="${SABOTAGE_ARCHIVE_DIR}/sprint-ets-05-03-sabotage-systemfeatures-cascade-${TS}.xml"
  SABOTAGE_LOG="${SABOTAGE_ARCHIVE_DIR}/sprint-ets-05-03-sabotage-systemfeatures-cascade-${TS}.log"
fi

log() { echo "[sabotage-test $(date -u +%H:%M:%S)] $*" | tee -a "$SABOTAGE_LOG"; }
die() { echo "[sabotage-test FATAL] $*" >&2; cleanup_all; exit 1; }

cleanup_all() {
  # Kill stub server if still running
  if [[ -f "$STUB_PIDFILE" ]]; then
    local pid
    pid="$(cat "$STUB_PIDFILE" 2>/dev/null || true)"
    if [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null; then
      kill "$pid" 2>/dev/null || true
      sleep 0.5
      kill -9 "$pid" 2>/dev/null || true
    fi
    rm -f "$STUB_PIDFILE"
  fi
  # Remove docker container if still running
  docker rm -f "$CONTAINER_NAME" >/dev/null 2>&1 || true
}
trap cleanup_all EXIT

log "sabotage mode: --target=$SABOTAGE_TARGET"

# =========================================================================
# Sprint 5 S-ETS-05-03 (REQ-ETS-CLEANUP-015) — --target=systemfeatures branch
# =========================================================================
if [[ "$SABOTAGE_TARGET" == "systemfeatures" ]]; then

  # ---------- Step 1: prerequisites
  log "step 1/6 — validating prerequisites for --target=systemfeatures"
  command -v docker >/dev/null 2>&1 || die "docker not found in PATH; --target=systemfeatures requires Docker"
  docker info >/dev/null 2>&1 || die "docker daemon not reachable; --target=systemfeatures requires a running Docker daemon"
  SF_TESTS_REL="src/main/java/org/opengis/cite/ogcapiconnectedsystems10/conformance/systemfeatures/SystemFeaturesTests.java"
  [[ -f "$REPO_ROOT/$SF_TESTS_REL" ]] || die "expected SystemFeaturesTests.java at $REPO_ROOT/$SF_TESTS_REL; cannot sabotage"

  # ---------- Step 2: copy source tree to temp dir
  SABOTAGE_WORKTREE="${SABOTAGE_TMPDIR}/worktree"
  log "step 2/6 — copying repo to temp worktree at $SABOTAGE_WORKTREE"
  mkdir -p "$SABOTAGE_WORKTREE"
  # Use rsync if available (faster + better excludes); fall back to cp -r.
  # Sprint 6 S-ETS-06-02 (REQ-ETS-CLEANUP-015 → FULLY-IMPLEMENTED): the
  # `.git/` exclude was REMOVED. The project Dockerfile has `COPY .git ./.git`
  # for git-commit-sha manifest pinning per ADR-002; without `.git` in the
  # temp worktree, `docker build` fails at that COPY step. Sister `.git` is
  # ~5.2MB (well under any reasonable size threshold), so including it has
  # negligible rsync cost. Fallback `cp -a "$REPO_ROOT/." "$SABOTAGE_WORKTREE/"`
  # already preserves `.git` (it copied everything then `rm -rf`'d only
  # `target`); we drop the explicit `.git` removal there too for symmetry.
  if command -v rsync >/dev/null 2>&1; then
    rsync -a --exclude='target/' --exclude='node_modules/' \
      --exclude='ops/test-results/*.xml' --exclude='ops/test-results/*.log' \
      "$REPO_ROOT/" "$SABOTAGE_WORKTREE/"
  else
    cp -a "$REPO_ROOT/." "$SABOTAGE_WORKTREE/"
    rm -rf "$SABOTAGE_WORKTREE/target"
  fi

  # ---------- Step 3: sed-patch SystemFeaturesTests.java in temp tree
  SF_TESTS_TMP="$SABOTAGE_WORKTREE/$SF_TESTS_REL"
  log "step 3/6 — sed-patching $SF_TESTS_TMP (FIRST @Test method body)"
  [[ -f "$SF_TESTS_TMP" ]] || die "expected sabotaged copy at $SF_TESTS_TMP; rsync/cp failed"
  # Inject `throw new AssertionError("SABOTAGED ...");` as the FIRST statement of
  # systemsCollectionReturns200's body. Robust to whitespace: the @Test block
  # ends with `public void systemsCollectionReturns200() {`; we insert the
  # throw immediately after that opening brace. The python form is more robust
  # than sed for multi-line block matching across distros (BSD vs GNU sed
  # quirks), so we use python to do the targeted insertion.
  # Sprint 7 S-ETS-07-01 Wedge 1 (REQ-ETS-CLEANUP-018) — javac unreachable-statement fix:
  # The bare `throw new AssertionError(...)` injection makes the existing
  # `ETSAssert.assertStatus(...)` line below it unreachable per JLS §14.21,
  # which javac rejects with `[210,17] unreachable statement`. The
  # `if (true) throw new AssertionError(...)` idiom defeats javac reachability
  # analysis: an `if` with a constant boolean expression is reachable in BOTH
  # branches statically, so the existing assertStatus line remains compilable;
  # at runtime the `if (true)` guard ALWAYS fires and the sabotage semantics
  # are preserved (the assertion error still throws, SystemFeatures FAILs,
  # downstream cascade-SKIPs). 2-sprint-old latent defect (Sprint 5 GAP-2
  # `.git`-exclude masked it; Sprint 6 .git-include exposed it; Sprint 7
  # closes it).
  SABOTAGE_MARKER='if (true) throw new AssertionError("SABOTAGED by --target=systemfeatures Sprint 5 S-ETS-05-03");'
  python3 - "$SF_TESTS_TMP" "$SABOTAGE_MARKER" <<'PY'
import re
import sys

path, marker = sys.argv[1], sys.argv[2]
with open(path, 'r', encoding='utf-8') as f:
    src = f.read()

# Match `public void systemsCollectionReturns200() {` followed by optional
# whitespace + a newline; insert the throw on the next line.
pat = re.compile(
    r'(public\s+void\s+systemsCollectionReturns200\s*\(\s*\)\s*\{)',
    re.MULTILINE,
)
m = pat.search(src)
if not m:
    print(f"FATAL: could not find systemsCollectionReturns200 method header in {path}", file=sys.stderr)
    sys.exit(2)

new_src = src[:m.end()] + "\n\t\t" + marker + src[m.end():]
with open(path, 'w', encoding='utf-8') as f:
    f.write(new_src)
print(f"OK: injected sabotage marker after method header in {path}")
PY
  # Verify the patch landed
  if ! grep -q 'SABOTAGED by --target=systemfeatures' "$SF_TESTS_TMP"; then
    die "sabotage marker not found in $SF_TESTS_TMP after patch step"
  fi
  log "  sabotage marker grep-verified in temp tree"
  # Critical worktree-pollution guard: confirm the user's original file is UNTOUCHED.
  if grep -q 'SABOTAGED by --target=systemfeatures' "$REPO_ROOT/$SF_TESTS_REL"; then
    die "WORKTREE POLLUTION: sabotage marker leaked into $REPO_ROOT/$SF_TESTS_REL — abort."
  fi

  # ---------- Step 4: run smoke from temp worktree against GeoRobotix
  log "step 4/6 — running smoke from sabotaged temp tree (image $IMAGE_TAG, container $CONTAINER_NAME)"
  log "  IUT: ${SMOKE_IUT_URL:-https://api.georobotix.io/ogc/t18/api}"
  # Sprint 6 S-ETS-06-02 honest log message — capture smoke exit code so we
  # can distinguish Docker build failure from smoke @Test failure. Pre-Sprint
  # 6 the unconditional "EXPECTED — SystemFeatures FAIL on first @Test"
  # message fired even when the Docker build step failed (which was the
  # actual failure mode in Sprint 5 due to the .git rsync exclude).
  SMOKE_EXIT_CODE=0
  pushd "$SABOTAGE_WORKTREE" >/dev/null
  SMOKE_CONTAINER_NAME="$CONTAINER_NAME" \
    SMOKE_IMAGE_TAG="$IMAGE_TAG" \
    SMOKE_OUTPUT_DIR="${SABOTAGE_TMPDIR}/test-results" \
    bash scripts/smoke-test.sh 2>&1 | tee -a "$SABOTAGE_LOG" \
    || SMOKE_EXIT_CODE=$?
  # Note: with `set -o pipefail`, the pipeline exit code is the rightmost
  # non-zero in the pipe; the `|| ...` captures that. SMOKE_EXIT_CODE is 0
  # for clean smoke pass; non-zero for any failure mode. We disambiguate
  # using the presence/absence of the TestNG XML below.
  popd >/dev/null

  # Locate the smoke-test report — written to SMOKE_OUTPUT_DIR per S-ETS-05-02.
  # Sprint 7 S-ETS-07-01 Wedge 4 (REQ-ETS-CLEANUP-018) — pipefail-unreachable fix:
  # The previous `LATEST_REPORT="$(ls -t ... | head -1)"` pipeline killed the
  # script under `set -eo pipefail` when `ls` found no matching glob (the
  # normal case after Docker build failure — no XML produced). The disambig-
  # uation block at lines ~289-298 was never reached. Replace with a glob-safe
  # `for` idiom: nullglob would also work, but the `[[ -e ... ]]` guard inside
  # a default-glob loop returns the literal pattern when no match exists, and
  # the `-e` test rejects it gracefully. This idiom is portable across bash
  # versions and avoids the pipeline-exit-code issue under pipefail.
  LATEST_REPORT=""
  for _f in "${SABOTAGE_TMPDIR}/test-results"/s-ets-01-03-teamengine-smoke-*.xml; do
    [[ -e "$_f" ]] && LATEST_REPORT="$_f"
  done
  if [[ -z "$LATEST_REPORT" ]]; then
    if [[ "$SMOKE_EXIT_CODE" -ne 0 ]]; then
      log "  smoke exited non-zero with NO TestNG report — Docker build FAILED (not a sabotage-marker hit)"
      log "  (this is NOT the expected SystemFeatures-FAIL cascade; check container build for COPY/.git or other Docker errors above)"
    fi
    die "smoke-test.sh did not produce a TestNG report under ${SABOTAGE_TMPDIR}/test-results"
  fi
  if [[ "$SMOKE_EXIT_CODE" -ne 0 ]]; then
    log "  smoke exited non-zero (EXPECTED — SystemFeatures FAIL on first @Test); TestNG report present at $LATEST_REPORT"
  fi
  cp -f "$LATEST_REPORT" "$SABOTAGE_REPORT_XML"
  log "  TestNG report captured to $SABOTAGE_REPORT_XML"

  # ---------- Step 5: parse TestNG XML; assert two-level cascade
  log "step 5/6 — parsing TestNG report; asserting two-level cascade pattern"
  PARSE_RESULT="$(python3 - "$SABOTAGE_REPORT_XML" <<'PY'
import sys
import xml.etree.ElementTree as ET

report = sys.argv[1]
try:
    tree = ET.parse(report)
except ET.ParseError as e:
    print(f"FATAL: report unparseable: {e}", file=sys.stderr)
    sys.exit(2)
root = tree.getroot()

def classify(sig):
    if "conformance.core" in sig:
        return "core"
    if "conformance.systemfeatures" in sig:
        return "systemfeatures"
    if "conformance.common" in sig:
        return "common"
    if "conformance.subsystems" in sig:
        return "subsystems"
    if "conformance.procedures" in sig:
        return "procedures"
    if "conformance.deployments" in sig:
        return "deployments"
    return "other"

buckets = {k: [] for k in ("core", "systemfeatures", "common", "subsystems",
                            "procedures", "deployments", "other")}
for tm in root.iter("test-method"):
    if tm.get("is-config", "false").lower() == "true":
        continue
    sig = tm.get("signature", "") or ""
    name = tm.get("name", "")
    status = tm.get("status", "")
    buckets[classify(sig)].append((name, status))

for cls in ("core", "common", "systemfeatures", "subsystems", "procedures", "deployments"):
    rows = buckets[cls]
    print(f"{cls} @Tests seen: {len(rows)}")
    for n, s in rows:
        print(f"  {cls:14s} {s:8s}  {n}")

failures = []
# Core PASS, Common PASS
for cls in ("core", "common"):
    rows = buckets[cls]
    if not rows:
        failures.append(f"VERDICT FAIL: no {cls} @Tests seen (suite scope wrong)")
        continue
    not_pass = [r for r in rows if r[1] != "PASS"]
    if not_pass:
        failures.append(f"VERDICT FAIL: {cls} has {len(not_pass)} non-PASS results "
                        f"(should be all PASS — Core+Common are independent of SystemFeatures): {not_pass}")

# SystemFeatures: at least 1 FAIL (sabotage worked)
sf = buckets["systemfeatures"]
if not sf:
    failures.append("VERDICT FAIL: no SystemFeatures @Tests seen (suite scope wrong)")
else:
    sf_failed = [r for r in sf if r[1] == "FAIL"]
    if not sf_failed:
        failures.append("VERDICT FAIL: no SystemFeatures @Test FAILed (sabotage marker did not fire)")

# Subsystems, Procedures, Deployments: every @Test SKIP
for cls in ("subsystems", "procedures", "deployments"):
    rows = buckets[cls]
    if not rows:
        # If the class has no smoke @Tests yet (Sprint 5 may not include all
        # classes), skip the assertion rather than fail.
        print(f"NOTE: no {cls} @Tests seen; skipping cascade assertion for {cls}")
        continue
    not_skipped = [r for r in rows if r[1] != "SKIP"]
    if not_skipped:
        failures.append(f"VERDICT FAIL: {cls} has {len(not_skipped)} non-SKIP results "
                        f"(should be all SKIP via two-level cascade): {not_skipped}")

print()
if failures:
    for f in failures:
        print(f, file=sys.stderr)
    sys.exit(1)
print("VERDICT: PASS — Core+Common PASS, SystemFeatures FAILed, "
      "Subsystems+Procedures+Deployments cascade-SKIPped (two-level cascade verified)")
sys.exit(0)
PY
)" || PARSE_EXIT=$?
  PARSE_EXIT="${PARSE_EXIT:-0}"
  echo "$PARSE_RESULT" | tee -a "$SABOTAGE_LOG"

  if [[ "$PARSE_EXIT" -ne 0 ]]; then
    log "step 6/6 — VERDICT: FAIL (two-level cascade not verified)"
    log "  evidence archived at $SABOTAGE_REPORT_XML"
    exit 1
  fi

  # ---------- Step 6: cleanup + report
  log "step 6/6 — VERDICT: PASS (two-level cascade verified end-to-end)"
  log "  report: $SABOTAGE_REPORT_XML"
  log "  log:    $SABOTAGE_LOG"
  log "SABOTAGE PASS: SystemFeatures FAILed; Subsystems+Procedures+Deployments cascade-SKIPped"
  log "  (User worktree at $REPO_ROOT/$SF_TESTS_REL UNMODIFIED — sabotage was hermetic via temp clone.)"
  cleanup_all
  trap - EXIT
  exit 0
fi
# =========================================================================
# End of --target=systemfeatures branch.
# Below: Sprint 3 default mode (--target=core; HTTP-500 stub-server sabotage)
# =========================================================================

# ---------- Step 1: launch HTTP stub server on ephemeral port (HTTP 500 to all)
log "step 1/5 — launching HTTP-500 stub server on ephemeral port"
python3 - "$STUB_PIDFILE" "$STUB_PORTFILE" <<'PY' &
import http.server
import socket
import socketserver
import sys
import threading

pidfile, portfile = sys.argv[1], sys.argv[2]

class Sabotage500(http.server.BaseHTTPRequestHandler):
    def _respond_500(self):
        self.send_response(500, "Sabotage")
        self.send_header("Content-Type", "application/json")
        body = b'{"sabotage": "Core sabotage stub returns 500 to all requests"}'
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)
    def do_GET(self): self._respond_500()
    def do_HEAD(self): self._respond_500()
    def do_POST(self): self._respond_500()
    def log_message(self, fmt, *args): pass  # suppress per-request stderr noise

# Bind to ephemeral OS-assigned port (port 0); read it back for the parent.
class Reusable(socketserver.ThreadingTCPServer):
    allow_reuse_address = True

# Bind to 0.0.0.0 (all interfaces) so a Docker container running the smoke
# pipeline can reach this stub via the host's Docker bridge IP exposed as
# host.docker.internal (set by smoke-test.sh's
# --add-host=host.docker.internal:host-gateway flag — Docker on Linux
# without Docker Desktop does NOT auto-resolve host.docker.internal).
# Sprint 4 S-ETS-04-04 fix (a): was 127.0.0.1 → unreachable from container.
with Reusable(("0.0.0.0", 0), Sabotage500) as srv:
    port = srv.server_address[1]
    with open(portfile, "w") as f:
        f.write(str(port))
    with open(pidfile, "w") as f:
        import os
        f.write(str(os.getpid()))
    srv.serve_forever()
PY

# Wait up to 5 seconds for stub to come up
for _ in 1 2 3 4 5 6 7 8 9 10; do
  if [[ -f "$STUB_PORTFILE" ]] && [[ -s "$STUB_PORTFILE" ]]; then break; fi
  sleep 0.5
done
[[ -s "$STUB_PORTFILE" ]] || die "stub server failed to write port file at $STUB_PORTFILE"
STUB_PORT="$(cat "$STUB_PORTFILE")"
log "  stub server listening on port $STUB_PORT (pid $(cat "$STUB_PIDFILE" 2>/dev/null || echo ?))"

# Sanity-check stub returns 500
stub_status=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:${STUB_PORT}/" || echo 000)
[[ "$stub_status" == "500" ]] || die "stub server returned $stub_status (expected 500); aborting"

# ---------- Step 2: run smoke against stub URL (Core will FAIL; SystemFeatures should SKIP)
# host.docker.internal: container needs to reach the host's ephemeral port.
# On Linux Docker, --add-host=host.docker.internal:host-gateway makes this work.
STUB_URL="http://host.docker.internal:${STUB_PORT}"
log "step 2/5 — running smoke against sabotage stub URL: $STUB_URL"
log "  (Core landing-page @Test expected to FAIL; SystemFeatures @Tests expected to SKIP via depends-on=core)"

# Run smoke with explicit overrides: SMOKE_IUT_URL points at stub; container
# adds host.docker.internal mapping so curl from inside container reaches
# the ephemeral port. Use a unique container name to avoid collision with
# concurrent smoke runs.
SMOKE_IUT_URL="$STUB_URL" \
  SMOKE_CONTAINER_NAME="$CONTAINER_NAME" \
  SMOKE_IMAGE_TAG="$IMAGE_TAG" \
  bash scripts/smoke-test.sh 2>&1 | tee -a "$SABOTAGE_LOG" \
  || log "  smoke exited non-zero (EXPECTED — Core FAILs against sabotage stub)"

# Locate the smoke-test report — smoke-test.sh writes to ops/test-results/ by
# default. Capture it into the sabotage archive for behavioral evidence.
LATEST_REPORT="$(ls -t ops/test-results/s-ets-01-03-teamengine-smoke-*.xml 2>/dev/null | head -1)"
[[ -n "$LATEST_REPORT" ]] || die "smoke-test.sh did not produce a TestNG report; aborting"
cp -f "$LATEST_REPORT" "$SABOTAGE_REPORT_XML"
log "  TestNG report captured to $SABOTAGE_REPORT_XML"

# ---------- Step 3: parse TestNG XML; assert Core has FAIL + SystemFeatures all SKIP
log "step 3/5 — parsing TestNG report; asserting cascading-SKIP semantics"

PARSE_RESULT="$(python3 - "$SABOTAGE_REPORT_XML" <<'PY'
import sys
import xml.etree.ElementTree as ET

report = sys.argv[1]
try:
    tree = ET.parse(report)
except ET.ParseError as e:
    print(f"FATAL: report unparseable: {e}", file=sys.stderr)
    sys.exit(2)
root = tree.getroot()

# Find every <test-method> child with a status attribute. TestNG's
# canonical XML report is <testng-results><suite><test><class><test-method/>.
core_methods = []
sf_methods = []
for tm in root.iter("test-method"):
    status = tm.get("status", "")
    name = tm.get("name", "")
    cls = ""
    p = tm
    # Walk up to find the parent <class> for context
    parent_map = {c: p for p in tree.iter() for c in p}
    for ancestor in tm.iter():
        pass
    # Use signature to classify by class name
    sig = tm.get("signature", "") or ""
    # configuration methods (BeforeClass, etc) carry is-config="true"
    if tm.get("is-config", "false").lower() == "true":
        continue
    if "conformance.core" in sig:
        core_methods.append((name, status, sig))
    elif "conformance.systemfeatures" in sig:
        sf_methods.append((name, status, sig))

print(f"Core @Test methods seen: {len(core_methods)}")
for n, s, _ in core_methods:
    print(f"  Core  {s:8s}  {n}")
print(f"SystemFeatures @Test methods seen: {len(sf_methods)}")
for n, s, _ in sf_methods:
    print(f"  SF    {s:8s}  {n}")

# Acceptance assertions
core_failed = [m for m in core_methods if m[1] == "FAIL"]
sf_skipped = [m for m in sf_methods if m[1] == "SKIP"]
sf_not_skipped = [m for m in sf_methods if m[1] != "SKIP"]

print()
print(f"Core failures: {len(core_failed)}")
print(f"SF skipped:    {len(sf_skipped)}")
print(f"SF NOT skipped (should be 0): {len(sf_not_skipped)}")

if not core_methods:
    print("VERDICT: FAIL — no Core @Tests seen (sabotage stub may be unreachable)", file=sys.stderr)
    sys.exit(1)
if not core_failed:
    print("VERDICT: FAIL — no Core @Test FAILed (sabotage stub did not break Core)", file=sys.stderr)
    sys.exit(1)
if not sf_methods:
    print("VERDICT: FAIL — no SystemFeatures @Tests seen (suite scope wrong)", file=sys.stderr)
    sys.exit(1)
if sf_not_skipped:
    print(f"VERDICT: FAIL — {len(sf_not_skipped)} SystemFeatures @Tests did NOT SKIP "
          "(dependency wiring broken — should cascade SKIP from failed Core)", file=sys.stderr)
    for n, s, _ in sf_not_skipped:
        print(f"  offending: {s:8s}  {n}", file=sys.stderr)
    sys.exit(1)

print("VERDICT: PASS — Core FAILed (sabotage worked), all SystemFeatures @Tests SKIPped (cascading-SKIP wiring verified)")
sys.exit(0)
PY
)" || PARSE_EXIT=$?
PARSE_EXIT="${PARSE_EXIT:-0}"
echo "$PARSE_RESULT" | tee -a "$SABOTAGE_LOG"

if [[ "$PARSE_EXIT" -ne 0 ]]; then
  log "step 4/5 — VERDICT: FAIL (cascading-SKIP wiring not verified)"
  log "  evidence archived at $SABOTAGE_REPORT_XML"
  exit 1
fi

# ---------- Step 4 & 5: cleanup + report
log "step 4/5 — VERDICT: PASS (cascading-SKIP wiring verified end-to-end)"
log "step 5/5 — archiving evidence + cleaning up"
log "  report: $SABOTAGE_REPORT_XML"
log "  log:    $SABOTAGE_LOG"
log "SABOTAGE PASS: SystemFeatures @Tests cascade-SKIPped when Core was sabotaged via HTTP-500 stub"

cleanup_all
trap - EXIT
exit 0
