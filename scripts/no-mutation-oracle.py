#!/usr/bin/env python3
"""Validate that smoke logs contain no IUT-bound mutation requests."""

import re
import sys
from urllib.parse import urlsplit

MUTATION_METHODS = {"POST", "PUT", "DELETE", "PATCH"}
SINGLE_LINE_RE = re.compile(r"^Request:\s+([A-Za-z]+)\s+(\S+)\s*$")


def same_iut(uri, iut_url):
    parsed = urlsplit(uri)
    iut = urlsplit(iut_url)
    if parsed.scheme != iut.scheme or parsed.netloc != iut.netloc:
        return False
    base = iut_url.rstrip("/")
    return uri == base or uri.startswith(base + "/") or uri.startswith(base + "?")


def scan(log_file, iut_url):
    last_method = None
    iut_request_count = 0
    violations = []

    with open(log_file, encoding="utf-8", errors="replace") as fh:
        for raw in fh:
            line = raw.strip()
            single = SINGLE_LINE_RE.match(line)
            if single:
                method = single.group(1).upper()
                uri = single.group(2)
                if same_iut(uri, iut_url):
                    iut_request_count += 1
                    if method in MUTATION_METHODS:
                        violations.append(f"{method} {uri}")
                last_method = None
                continue

            if line.startswith("Request method:"):
                last_method = line.split(":", 1)[1].strip().upper()
                continue

            if line.startswith("Request URI:"):
                uri = line.split(":", 1)[1].strip()
                if last_method is not None and same_iut(uri, iut_url):
                    iut_request_count += 1
                    if last_method in MUTATION_METHODS:
                        violations.append(f"{last_method} {uri}")
                last_method = None

    return iut_request_count, violations


def main(argv):
    if len(argv) != 3:
        print("usage: no-mutation-oracle.py <log-file> <iut-url>", file=sys.stderr)
        return 2
    iut_request_count, violations = scan(argv[1], argv[2].rstrip("/"))
    if violations:
        print("\n".join(violations))
        return 1
    if iut_request_count == 0:
        print("No IUT-bound request log lines found; no-mutation oracle is inconclusive.", file=sys.stderr)
        return 2
    print(f"recognized_iut_request_logs={iut_request_count}")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
