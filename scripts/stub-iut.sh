#!/usr/bin/env bash
# scripts/stub-iut.sh — ets-ogcapi-connectedsystems10
#
# REQ-ETS-CLEANUP-011, SCENARIO-ETS-CLEANUP-CREDENTIAL-LEAK-E2E-001:
#   Hermetic stub IUT (Implementation Under Test) for credential-leak E2E
#   verification. Listens on an OS-assigned ephemeral port; for every HTTP
#   request, ECHOES the inbound `Authorization` header back in the JSON
#   response body and ALSO logs it to a file (the stub-IUT log). This lets
#   the credential-leak verifier prove that:
#     (i)  the test code DID send the `Authorization` header (stub-IUT log
#          shows the unmasked credential — proves the try/finally restoration
#          in MaskingRequestLoggingFilter unmasked the header before the
#          REST-Assured filter chain handed it off to the underlying
#          HttpURLConnection that produced the actual wire bytes), AND
#     (ii) the test artifacts (TestNG XML + container catalina.out) carry
#          ZERO unmasked-credential leaks (the masking filter must scrub the
#          credential from logged output but NOT from outbound wire requests).
#
# Composes with S-ETS-04-04 sabotage-test.sh stub-server primitive (same
# ephemeral-port + PID-trap pattern). The two stubs differ in payload:
# sabotage-test.sh stub returns HTTP 500 to break Core; this stub returns
# HTTP 401 with the inbound `Authorization` header echoed in the body to
# exercise the auth path without granting actual access.
#
# Usage:
#   bash scripts/stub-iut.sh start [logfile_path]   # background-spawn; writes /tmp/stub-iut.{pid,port}
#   bash scripts/stub-iut.sh stop                   # kills by PID, removes pid/port files
#   bash scripts/stub-iut.sh status                 # prints state of running stub (or "stopped")
#
# Per Architect-surfaced risk STUB-IUT-PORT-LEAK-ACROSS-SCRIPT-RUNS:
# trap cleanup MUST kill by PID (not by port) so a partial-run abort doesn't
# leave a zombie listener. The verify-credential-leak.sh wrapper sets up a
# trap EXIT that calls `bash scripts/stub-iut.sh stop` for safety.

set -eo pipefail

ACTION="${1:-start}"
PIDFILE="${STUB_IUT_PIDFILE:-/tmp/stub-iut.pid}"
PORTFILE="${STUB_IUT_PORTFILE:-/tmp/stub-iut.port}"
LOGFILE_DEFAULT="/tmp/stub-iut.log"
LOGFILE="${2:-${STUB_IUT_LOGFILE:-${LOGFILE_DEFAULT}}}"

case "$ACTION" in
  start)
    # If already running, refuse to start a second instance (port-leak guard).
    if [[ -f "$PIDFILE" ]]; then
      EXISTING_PID="$(cat "$PIDFILE" 2>/dev/null || true)"
      if [[ -n "$EXISTING_PID" ]] && kill -0 "$EXISTING_PID" 2>/dev/null; then
        echo "[stub-iut] already running (pid $EXISTING_PID, port $(cat "$PORTFILE" 2>/dev/null || echo ?))" >&2
        echo "[stub-iut] refusing to start a second instance; run 'bash scripts/stub-iut.sh stop' first" >&2
        exit 1
      fi
      # stale pidfile — clean up
      rm -f "$PIDFILE" "$PORTFILE"
    fi

    # Fresh log
    : > "$LOGFILE"

    # Background-spawn the Python http.server stub; capture PID for trap cleanup.
    python3 - "$PIDFILE" "$PORTFILE" "$LOGFILE" <<'PY' &
import http.server
import os
import socket
import socketserver
import sys
import threading
import time

pidfile, portfile, logfile = sys.argv[1], sys.argv[2], sys.argv[3]

class CredentialEchoHandler(http.server.BaseHTTPRequestHandler):
    """Echoes the inbound Authorization header in a 401 JSON response body
    AND logs it to logfile. The 401 response triggers the REST-Assured
    auth code path in the IUT-side test harness without granting actual
    access (so the test cannot accidentally exercise unintended IUT
    state-mutation logic)."""

    def _echo_auth(self):
        auth_hdr = self.headers.get("Authorization", "<absent>")
        # Append to logfile — proves the credential WAS transmitted to the IUT.
        # This is the (c) prong of the three-fold cross-check in
        # verify-credential-leak.sh.
        try:
            with open(logfile, "a") as f:
                f.write(f"{time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())} {self.command} {self.path} Authorization={auth_hdr}\n")
        except Exception:
            # Don't let logging failure block the response.
            pass
        body = (
            '{"error": "stub-iut authentication required",'
            ' "received_authorization": ' + repr(auth_hdr).replace("'", '"') + '}'
        ).encode("utf-8")
        self.send_response(401, "Stub-IUT Auth Required")
        self.send_header("Content-Type", "application/json")
        self.send_header("WWW-Authenticate", 'Bearer realm="stub-iut"')
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):    self._echo_auth()
    def do_HEAD(self):   self._echo_auth()
    def do_POST(self):   self._echo_auth()
    def do_PUT(self):    self._echo_auth()
    def do_DELETE(self): self._echo_auth()

    def log_message(self, fmt, *args):
        # Suppress default per-request stderr logging; we have our own
        # logfile-based audit trail in _echo_auth.
        return


class Reusable(socketserver.ThreadingTCPServer):
    allow_reuse_address = True


# Bind to 0.0.0.0 (all interfaces) per S-ETS-04-04 fix — a Docker container
# running smoke-test.sh needs to reach this stub via host.docker.internal,
# which resolves to a Docker bridge IP that is NOT 127.0.0.1.
# Bind to OS-assigned ephemeral port (port 0).
with Reusable(("0.0.0.0", 0), CredentialEchoHandler) as srv:
    port = srv.server_address[1]
    with open(portfile, "w") as f:
        f.write(str(port))
    with open(pidfile, "w") as f:
        f.write(str(os.getpid()))
    srv.serve_forever()
PY

    # Wait up to 5 seconds for stub to come up.
    for _ in 1 2 3 4 5 6 7 8 9 10; do
      if [[ -f "$PORTFILE" ]] && [[ -s "$PORTFILE" ]]; then break; fi
      sleep 0.5
    done
    if [[ ! -s "$PORTFILE" ]]; then
      echo "[stub-iut] FATAL: failed to write port file at $PORTFILE within 5s" >&2
      exit 1
    fi
    PORT="$(cat "$PORTFILE")"
    PID="$(cat "$PIDFILE" 2>/dev/null || echo ?)"
    echo "[stub-iut] started: pid=$PID port=$PORT logfile=$LOGFILE"
    echo "[stub-iut] reachable via http://127.0.0.1:$PORT (host) or http://host.docker.internal:$PORT (containers w/ --add-host)"
    ;;

  stop)
    if [[ -f "$PIDFILE" ]]; then
      PID="$(cat "$PIDFILE" 2>/dev/null || true)"
      if [[ -n "$PID" ]] && kill -0 "$PID" 2>/dev/null; then
        kill "$PID" 2>/dev/null || true
        sleep 0.3
        kill -9 "$PID" 2>/dev/null || true
        echo "[stub-iut] stopped (pid $PID)"
      else
        echo "[stub-iut] no running process (stale pidfile)"
      fi
      rm -f "$PIDFILE" "$PORTFILE"
    else
      echo "[stub-iut] not running"
    fi
    ;;

  status)
    if [[ -f "$PIDFILE" ]]; then
      PID="$(cat "$PIDFILE" 2>/dev/null || true)"
      if [[ -n "$PID" ]] && kill -0 "$PID" 2>/dev/null; then
        echo "running pid=$PID port=$(cat "$PORTFILE" 2>/dev/null || echo ?)"
        exit 0
      fi
    fi
    echo "stopped"
    exit 1
    ;;

  *)
    echo "usage: $0 {start [logfile_path]|stop|status}" >&2
    exit 2
    ;;
esac
