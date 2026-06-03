#!/usr/bin/env python3
"""Report URI and schema bundle drift between the frozen web app and Java ETS.

REQ-ETS-SYNC-001:
  SCENARIO-ETS-SYNC-URI-DIFF-001
  SCENARIO-ETS-SYNC-URI-SCHEMA-DRIFT-AUDIT-001
REQ-ETS-CLEANUP-020:
  SCENARIO-ETS-SYNC-URI-SCHEMA-DRIFT-AUDIT-001
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import subprocess
import sys
import tempfile
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


URI_RE = re.compile(r"http://www\.opengis\.net/spec/ogcapi-connectedsystems-[12]/1\.0/(?:req|conf)/[A-Za-z0-9_.:/?#&=+-]+")
SOURCE_SUFFIXES = {".java", ".ts", ".tsx", ".js", ".jsx"}


def utc_now() -> str:
    return datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


def default_webapp_root() -> Path:
    return Path.cwd().parent / "csapi_compliance"


def extract_uris(root: Path, suffixes: set[str]) -> set[str]:
    if not root.exists():
        return set()
    uris: set[str] = set()
    paths = [root] if root.is_file() else sorted(path for path in root.rglob("*") if path.is_file())
    for path in paths:
        if path.suffix not in suffixes:
            continue
        text = path.read_text(encoding="utf-8", errors="replace")
        for match in URI_RE.finditer(text):
            uris.add(match.group(0).rstrip(".,;)]}'\""))
    return uris


def load_allowlist(path: Path | None) -> set[str]:
    if not path or not path.exists():
        return set()
    values: set[str] = set()
    for line in path.read_text(encoding="utf-8").splitlines():
        stripped = line.strip()
        if not stripped or stripped.startswith("#"):
            continue
        values.add(stripped.split()[0])
    return values


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def hash_schema_tree(root: Path) -> dict[str, str]:
    if not root.exists():
        return {}
    hashes: dict[str, str] = {}
    for path in sorted(p for p in root.rglob("*") if p.is_file()):
        if path.name.endswith(":Zone.Identifier"):
            continue
        rel = path.relative_to(root).as_posix()
        hashes[rel] = sha256(path)
    return hashes


def git_metadata(path: Path) -> dict[str, Any]:
    try:
        target = path if path.exists() else path.parent
        root = subprocess.check_output(
            ["git", "-C", str(target), "rev-parse", "--show-toplevel"],
            stderr=subprocess.DEVNULL,
            text=True,
        ).strip()
        head = subprocess.check_output(
            ["git", "-C", root, "rev-parse", "HEAD"],
            stderr=subprocess.DEVNULL,
            text=True,
        ).strip()
        status_lines = subprocess.check_output(
            ["git", "-C", root, "status", "--short"],
            stderr=subprocess.DEVNULL,
            text=True,
        ).splitlines()
        return {
            "available": True,
            "root": root,
            "head": head,
            "dirty": bool(status_lines),
            "statusEntryCount": len(status_lines),
        }
    except (OSError, subprocess.CalledProcessError) as exc:
        return {
            "available": False,
            "error": type(exc).__name__,
        }


def build_report(args: argparse.Namespace) -> dict[str, Any]:
    java_uris = extract_uris(args.java_source_root, {".java"})
    webapp_uris = extract_uris(args.webapp_registry_root, SOURCE_SUFFIXES)
    allowlist = load_allowlist(args.allowlist)
    missing_in_java_raw = webapp_uris - java_uris
    missing_in_webapp_raw = java_uris - webapp_uris
    allowlisted = (missing_in_java_raw | missing_in_webapp_raw) & allowlist
    missing_in_java = sorted(missing_in_java_raw - allowlist)
    missing_in_webapp = sorted(missing_in_webapp_raw - allowlist)

    java_schemas = hash_schema_tree(args.java_schema_root)
    webapp_schemas = hash_schema_tree(args.webapp_schema_root)
    java_schema_keys = set(java_schemas)
    webapp_schema_keys = set(webapp_schemas)
    missing_schemas_in_java = sorted(webapp_schema_keys - java_schema_keys)
    extra_schemas_in_java = sorted(java_schema_keys - webapp_schema_keys)
    mismatched_schemas = sorted(
        rel for rel in java_schema_keys & webapp_schema_keys if java_schemas[rel] != webapp_schemas[rel]
    )

    failures: list[str] = []
    if missing_in_java or missing_in_webapp:
        failures.append("uri_drift_detected")
    if missing_schemas_in_java or extra_schemas_in_java or mismatched_schemas:
        failures.append("schema_drift_detected")

    return {
        "generatedAt": utc_now(),
        "requirements": ["REQ-ETS-SYNC-001", "REQ-ETS-CLEANUP-020"],
        "scenarios": [
            "SCENARIO-ETS-SYNC-URI-DIFF-001",
            "SCENARIO-ETS-SYNC-URI-SCHEMA-DRIFT-AUDIT-001",
        ],
        "inputs": {
            "javaSourceRoot": str(args.java_source_root),
            "webappRegistryRoot": str(args.webapp_registry_root),
            "javaSchemaRoot": str(args.java_schema_root),
            "webappSchemaRoot": str(args.webapp_schema_root),
            "allowlist": str(args.allowlist) if args.allowlist else None,
        },
        "repositoryMetadata": {
            "javaEts": git_metadata(args.java_source_root),
            "webapp": git_metadata(args.webapp_registry_root),
        },
        "uriAudit": {
            "javaUriCount": len(java_uris),
            "webappUriCount": len(webapp_uris),
            "allowlistedCount": len(allowlisted),
            "missingInJavaCount": len(missing_in_java),
            "missingInWebappCount": len(missing_in_webapp),
            "missingInJava": missing_in_java,
            "missingInWebapp": missing_in_webapp,
            "allowlisted": sorted(allowlisted),
        },
        "schemaAudit": {
            "javaSchemaCount": len(java_schemas),
            "webappSchemaCount": len(webapp_schemas),
            "missingInJavaCount": len(missing_schemas_in_java),
            "extraInJavaCount": len(extra_schemas_in_java),
            "hashMismatchCount": len(mismatched_schemas),
            "missingInJava": missing_schemas_in_java,
            "extraInJava": extra_schemas_in_java,
            "hashMismatches": mismatched_schemas,
        },
        "policy": {
            "failOnDrift": args.fail_on_drift,
            "reportOnly": not args.fail_on_drift,
        },
        "verdict": "FAIL" if args.fail_on_drift and failures else "PASS",
        "driftDetected": bool(failures),
        "failures": failures,
    }


def self_test() -> None:
    # REQ-ETS-SYNC-001 / SCENARIO-ETS-SYNC-URI-SCHEMA-DRIFT-AUDIT-001
    with tempfile.TemporaryDirectory() as td:
        root = Path(td)
        java_root = root / "java"
        web_root = root / "web"
        java_schema = root / "java-schemas"
        web_schema = root / "web-schemas"
        for path in (java_root, web_root, java_schema, web_schema):
            path.mkdir(parents=True)
        common_uri = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/canonical-url"
        web_only_uri = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/collections"
        java_only_uri = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/json"
        (java_root / "Example.java").write_text(f'class Example {{ String a = "{common_uri}"; String b = "{java_only_uri}"; }}', encoding="utf-8")
        (web_root / "registry.ts").write_text(f"export const a = '{common_uri}'; export const b = '{web_only_uri}';\n", encoding="utf-8")
        (java_schema / "same.json").write_text('{"a":1}', encoding="utf-8")
        (web_schema / "same.json").write_text('{"a":1}', encoding="utf-8")
        (java_schema / "changed.json").write_text('{"a":2}', encoding="utf-8")
        (web_schema / "changed.json").write_text('{"a":3}', encoding="utf-8")
        (web_schema / "ignore.json:Zone.Identifier").write_text("[ZoneTransfer]\n", encoding="utf-8")
        allowlist = root / "allowlist.txt"
        allowlist.write_text(web_only_uri + "\n", encoding="utf-8")
        args = argparse.Namespace(
            java_source_root=java_root,
            webapp_registry_root=web_root,
            java_schema_root=java_schema,
            webapp_schema_root=web_schema,
            allowlist=allowlist,
            fail_on_drift=False,
        )
        report = build_report(args)
        assert report["verdict"] == "PASS", report
        assert report["driftDetected"] is True, report
        assert report["uriAudit"]["allowlistedCount"] == 1, report
        assert report["uriAudit"]["missingInWebappCount"] == 1, report
        assert report["schemaAudit"]["hashMismatchCount"] == 1, report
    print("uri-drift-audit self-test PASS")


def parse_args(argv: list[str]) -> argparse.Namespace:
    webapp_root = default_webapp_root()
    parser = argparse.ArgumentParser(description="Report URI and schema drift between frozen v1.0 web app and Java ETS.")
    parser.add_argument("--java-source-root", type=Path, default=Path("src/main/java"))
    parser.add_argument("--webapp-registry-root", type=Path, default=webapp_root / "src/engine/registry")
    parser.add_argument("--java-schema-root", type=Path, default=Path("src/main/resources/schemas"))
    parser.add_argument("--webapp-schema-root", type=Path, default=webapp_root / "schemas")
    parser.add_argument("--allowlist", type=Path, default=Path("ops/uri-coverage-allowlist.txt"))
    parser.add_argument("--output-json", type=Path, help="Write JSON report to this path.")
    parser.add_argument("--fail-on-drift", action="store_true", help="Exit non-zero when unallowlisted drift is detected.")
    parser.add_argument("--self-test", action="store_true", help="Run built-in self-test and exit.")
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv or sys.argv[1:])
    if args.self_test:
        self_test()
        return 0
    report = build_report(args)
    text = json.dumps(report, indent=2, sort_keys=True) + "\n"
    if args.output_json:
        args.output_json.write_text(text, encoding="utf-8")
    else:
        print(text, end="")
    return 1 if report["verdict"] == "FAIL" else 0


if __name__ == "__main__":
    raise SystemExit(main())
