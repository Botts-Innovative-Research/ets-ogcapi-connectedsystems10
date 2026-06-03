#!/usr/bin/env python3
"""Summarize TeamEngine artifact hygiene evidence.

REQ-ETS-CLEANUP-020:
  SCENARIO-ETS-CLEANUP-ARTIFACT-HYGIENE-SUMMARY-001
  SCENARIO-ETS-CLEANUP-ARTIFACT-CREDENTIAL-SCAN-001
"""

from __future__ import annotations

import argparse
import json
import re
import sys
import tempfile
import xml.etree.ElementTree as ET
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


REQUEST_LINE_RE = re.compile(r"\bRequest:\s+(GET|POST|PUT|PATCH|DELETE|OPTIONS|HEAD)\s+(\S+)", re.I)
REQUEST_METHOD_RE = re.compile(r"\bRequest method:\s*(GET|POST|PUT|PATCH|DELETE|OPTIONS|HEAD)\b", re.I)
REQUEST_URI_RE = re.compile(r"\bRequest URI:\s*(\S+)", re.I)
AUTH_HEADER_RE = re.compile(r"\bAuthorization\s*[:=]\s*(Basic|Bearer)\s+(?!<masked>\b)(\S+)", re.I)
BASIC_PATTERN_RE = re.compile(r"\bBasic\s+[A-Za-z0-9+/]{12,}={0,2}\b")
WRITE_METHODS = {"POST", "PUT", "PATCH", "DELETE"}


def utc_now() -> str:
    return datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8", errors="replace")


def parse_testng_report(path: Path) -> dict[str, Any]:
    root = ET.parse(path).getroot()
    totals = {
        "total": int(root.attrib.get("total", "0")),
        "passed": int(root.attrib.get("passed", "0")),
        "failed": int(root.attrib.get("failed", "0")),
        "skipped": int(root.attrib.get("skipped", "0")),
    }
    method_statuses = Counter()
    for method in root.iter("test-method"):
        status = method.attrib.get("status")
        if status:
            method_statuses[status.upper()] += 1
    return {
        "path": str(path),
        "totals": totals,
        "methodStatusCounts": dict(sorted(method_statuses.items())),
    }


def parse_requests(text: str, iut_prefixes: list[str]) -> dict[str, Any]:
    requests: list[tuple[str, str]] = []
    pending_method: str | None = None
    for line in text.splitlines():
        direct = REQUEST_LINE_RE.search(line)
        if direct:
            requests.append((direct.group(1).upper(), direct.group(2)))
            pending_method = None
            continue
        method_match = REQUEST_METHOD_RE.search(line)
        if method_match:
            pending_method = method_match.group(1).upper()
            continue
        uri_match = REQUEST_URI_RE.search(line)
        if uri_match and pending_method:
            requests.append((pending_method, uri_match.group(1)))
            pending_method = None

    all_methods = Counter(method for method, _url in requests)
    iut_requests = [(method, url) for method, url in requests if is_iut_url(url, iut_prefixes)]
    iut_methods = Counter(method for method, _url in iut_requests)
    iut_writes = [(method, url) for method, url in iut_requests if method in WRITE_METHODS]
    return {
        "recognizedRequestLogs": len(requests),
        "methodCounts": dict(sorted(all_methods.items())),
        "recognizedIutRequestLogs": len(iut_requests),
        "iutMethodCounts": dict(sorted(iut_methods.items())),
        "iutWriteCount": len(iut_writes),
        "iutWrites": [{"method": method, "url": url} for method, url in iut_writes[:25]],
    }


def is_iut_url(url: str, prefixes: list[str]) -> bool:
    if not prefixes:
        return False
    return any(url.startswith(prefix) for prefix in prefixes)


def scan_credentials(paths: list[Path], secrets: list[str]) -> dict[str, Any]:
    filtered_secrets = [secret for secret in secrets if len(secret) >= 8]
    hits = {
        "filesScanned": 0,
        "explicitSecretInputsProvided": len(secrets),
        "explicitSecretInputsScanned": len(filtered_secrets),
        "shortSecretInputsIgnored": len(secrets) - len(filtered_secrets),
        "unmaskedAuthorizationHeaderHits": 0,
        "genericBasicCredentialPatternHits": 0,
        "explicitSecretValueHits": 0,
        "filesWithHits": [],
    }
    files_with_hits: set[str] = set()
    for path in paths:
        text = read_text(path)
        hits["filesScanned"] += 1
        auth_hits = len(AUTH_HEADER_RE.findall(text))
        basic_hits = sum(1 for match in BASIC_PATTERN_RE.finditer(text) if "BasicDataSource" not in text[max(0, match.start() - 32):match.end() + 32])
        secret_hits = sum(text.count(secret) for secret in filtered_secrets)
        if auth_hits or basic_hits or secret_hits:
            files_with_hits.add(str(path))
        hits["unmaskedAuthorizationHeaderHits"] += auth_hits
        hits["genericBasicCredentialPatternHits"] += basic_hits
        hits["explicitSecretValueHits"] += secret_hits
    hits["filesWithHits"] = sorted(files_with_hits)
    return hits


def build_report(args: argparse.Namespace) -> dict[str, Any]:
    reports = [parse_testng_report(path) for path in args.testng_report]
    log_summaries = []
    for path in args.container_log:
        parsed = parse_requests(read_text(path), args.iut_prefix)
        parsed["path"] = str(path)
        log_summaries.append(parsed)

    scanned_paths = sorted({*args.scan_file, *args.testng_report, *args.container_log}, key=str)
    credential_summary = scan_credentials(scanned_paths, args.secret)
    total_iut_writes = sum(summary["iutWriteCount"] for summary in log_summaries)
    credential_leaks = (
        credential_summary["unmaskedAuthorizationHeaderHits"]
        + credential_summary["genericBasicCredentialPatternHits"]
        + credential_summary["explicitSecretValueHits"]
    )
    failures: list[str] = []
    if args.require_zero_iut_writes and total_iut_writes:
        failures.append("iut_write_methods_detected")
    if args.fail_on_credential_leak and credential_leaks:
        failures.append("credential_leak_detected")

    return {
        "generatedAt": utc_now(),
        "requirements": ["REQ-ETS-CLEANUP-020"],
        "scenarios": [
            "SCENARIO-ETS-CLEANUP-ARTIFACT-HYGIENE-SUMMARY-001",
            "SCENARIO-ETS-CLEANUP-ARTIFACT-CREDENTIAL-SCAN-001",
        ],
        "testngReports": reports,
        "containerLogs": log_summaries,
        "credentialScan": credential_summary,
        "policy": {
            "requireZeroIutWrites": args.require_zero_iut_writes,
            "failOnCredentialLeak": args.fail_on_credential_leak,
            "iutPrefixes": args.iut_prefix,
        },
        "verdict": "FAIL" if failures else "PASS",
        "failures": failures,
    }


def write_outputs(report: dict[str, Any], output_json: Path | None, output_text: Path | None) -> None:
    json_text = json.dumps(report, indent=2, sort_keys=True) + "\n"
    if output_json:
        output_json.write_text(json_text, encoding="utf-8")
    else:
        print(json_text, end="")
    if output_text:
        lines = [
            f"verdict={report['verdict']}",
            f"testngReports={len(report['testngReports'])}",
            f"containerLogs={len(report['containerLogs'])}",
            f"credentialFilesScanned={report['credentialScan']['filesScanned']}",
            f"explicitSecretInputsProvided={report['credentialScan']['explicitSecretInputsProvided']}",
            f"credentialLeaks={sum(report['credentialScan'][key] for key in ('unmaskedAuthorizationHeaderHits', 'genericBasicCredentialPatternHits', 'explicitSecretValueHits'))}",
            f"iutWriteCount={sum(log['iutWriteCount'] for log in report['containerLogs'])}",
        ]
        output_text.write_text("\n".join(lines) + "\n", encoding="utf-8")


def self_test() -> None:
    # REQ-ETS-CLEANUP-020 / SCENARIO-ETS-CLEANUP-ARTIFACT-HYGIENE-SUMMARY-001
    with tempfile.TemporaryDirectory() as td:
        root = Path(td)
        xml = root / "report.xml"
        xml.write_text('<?xml version="1.0"?><testng-results total="2" passed="1" failed="0" skipped="1"/>', encoding="utf-8")
        clean_log = root / "clean.log"
        clean_log.write_text(
            "Request: GET http://iut.example/api\n\tAuthorization=<masked>\n",
            encoding="utf-8",
        )
        dirty_log = root / "dirty.log"
        dirty_log.write_text(
            "Request: POST http://iut.example/api/systems\nAuthorization=Basic QUJDREVGR0hJSktMTU4=\n",
            encoding="utf-8",
        )
        args = argparse.Namespace(
            testng_report=[xml],
            container_log=[clean_log],
            scan_file=[],
            iut_prefix=["http://iut.example/api"],
            secret=[],
            require_zero_iut_writes=True,
            fail_on_credential_leak=True,
        )
        report = build_report(args)
        assert report["verdict"] == "PASS", report
        assert report["testngReports"][0]["totals"]["total"] == 2, report
        assert report["containerLogs"][0]["iutMethodCounts"]["GET"] == 1, report

        # REQ-ETS-CLEANUP-020 / SCENARIO-ETS-CLEANUP-ARTIFACT-CREDENTIAL-SCAN-001
        args.container_log = [dirty_log]
        args.secret = ["supersecret"]
        report = build_report(args)
        assert report["verdict"] == "FAIL", report
        assert "iut_write_methods_detected" in report["failures"], report
        assert "credential_leak_detected" in report["failures"], report
        assert report["credentialScan"]["explicitSecretInputsProvided"] == 1, report
    print("artifact-hygiene self-test PASS")


def parse_args(argv: list[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Summarize TeamEngine artifact hygiene evidence.")
    parser.add_argument("--testng-report", action="append", type=Path, default=[], help="TestNG XML report path.")
    parser.add_argument("--container-log", action="append", type=Path, default=[], help="Smoke container log path.")
    parser.add_argument("--scan-file", action="append", type=Path, default=[], help="Additional file to credential-scan.")
    parser.add_argument("--iut-prefix", action="append", default=[], help="URL prefix used to classify IUT-bound requests.")
    parser.add_argument("--secret", action="append", default=[], help="Secret value to scan for. Values are counted but not printed.")
    parser.add_argument("--output-json", type=Path, help="Write JSON report to this path.")
    parser.add_argument("--output-text", type=Path, help="Write compact text summary to this path.")
    parser.add_argument("--require-zero-iut-writes", action="store_true", help="Fail if IUT-bound write methods are found.")
    parser.add_argument("--no-fail-on-credential-leak", dest="fail_on_credential_leak", action="store_false", help="Report credential leaks without failing.")
    parser.add_argument("--self-test", action="store_true", help="Run built-in self-test and exit.")
    parser.set_defaults(fail_on_credential_leak=True)
    args = parser.parse_args(argv)
    if args.self_test:
        return args
    if not args.testng_report and not args.container_log and not args.scan_file:
        parser.error("provide at least one --testng-report, --container-log, or --scan-file")
    return args


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv or sys.argv[1:])
    if args.self_test:
        self_test()
        return 0
    report = build_report(args)
    write_outputs(report, args.output_json, args.output_text)
    return 1 if report["verdict"] == "FAIL" else 0


if __name__ == "__main__":
    raise SystemExit(main())
