#!/usr/bin/env python3
"""Extract and verify the released OGC API Connected Systems Annex A inventory."""

from __future__ import annotations

import argparse
import copy
import hashlib
import json
import re
import subprocess
import sys
from pathlib import Path
from typing import Any

SOURCE_REPOSITORY = "https://github.com/opengeospatial/ogcapi-connected-systems.git"
SOURCE_TAG = "v1.0.0"
SOURCE_COMMIT = "8e03b236a049849f2ccc24b4fd9fdce5ff69bed2"
DEFAULT_INVENTORY = Path(
    "src/main/resources/org/opengis/cite/ogcapiconnectedsystems10/ats/"
    "released-ats-inventory.json"
)

PARTS = (
    {
        "part": 1,
        "document": "OGC 23-001",
        "specificationBase": "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0",
        "sourcePath": "api/part1/standard/sections/annex-abstract-test-suite.adoc",
        "officialHtml": "https://docs.ogc.org/is/23-001/23-001.html",
        "officialPdf": "https://docs.ogc.org/is/23-001/23-001.pdf",
        "pdfSha256": "c444bff07193daf8ce880077b1d728127868b48c056fe35278129e04d439f9e4",
        "classCount": 13,
        "testCount": 110,
        "supportingTestCount": 2,
    },
    {
        "part": 2,
        "document": "OGC 23-002",
        "specificationBase": "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0",
        "sourcePath": "api/part2/standard/sections/annex-abstract-test-suite.adoc",
        "officialHtml": "https://docs.ogc.org/is/23-002/23-002.html",
        "officialPdf": "https://docs.ogc.org/is/23-002/23-002.pdf",
        "pdfSha256": "78531c637053890dd501bb153a0046261b9c03fa064d0888a39e2b0dc383d154",
        "classCount": 12,
        "testCount": 130,
        "supportingTestCount": 0,
    },
)

BLOCK_PATTERN = re.compile(
    r"\[(conformance_class|abstract_test)\]\s*\n====\s*\n(.*?)\n====",
    re.DOTALL,
)
METADATA_PATTERN = re.compile(r"^([\w-]+)::\s*(.*)$", re.MULTILINE)


class AuditError(RuntimeError):
    """A fail-closed inventory error."""


def parse_metadata(body: str) -> dict[str, list[str]]:
    metadata: dict[str, list[str]] = {}
    for key, value in METADATA_PATTERN.findall(body):
        metadata.setdefault(key, []).append(value.strip())
    return metadata


def single(metadata: dict[str, list[str]], key: str, context: str) -> str | None:
    values = metadata.get(key, [])
    if len(values) > 1:
        raise AuditError(f"{context}: duplicate {key} metadata")
    return values[0] if values else None


def parse_annex(text: str, part_config: dict[str, Any]) -> tuple[list[dict[str, Any]], list[dict[str, Any]]]:
    part = part_config["part"]
    base = part_config["specificationBase"]
    classes: list[dict[str, Any]] = []
    tests: list[dict[str, Any]] = []

    for kind, body in BLOCK_PATTERN.findall(text):
        metadata = parse_metadata(body)
        identifier = single(metadata, "identifier", f"Part {part} {kind}")
        if not identifier:
            raise AuditError(f"Part {part} {kind}: missing identifier")
        if not identifier.startswith("/conf/"):
            raise AuditError(f"Part {part}: invalid ATS identifier {identifier}")
        target = single(metadata, "target", f"Part {part} {identifier}")
        if target and not target.startswith(("/req/", "/rec/")):
            raise AuditError(f"Part {part} {identifier}: invalid target {target}")

        common = {
            "part": part,
            "identifier": identifier,
            "fullIdentifier": base + identifier,
            "target": target,
            "fullTarget": base + target if target else None,
        }
        if kind == "conformance_class":
            declared = metadata.get("conformance-test", [])
            classes.append({**common, "tests": declared})
        else:
            tests.append(
                {
                    **common,
                    "classIdentifier": None,
                    "supporting": False,
                }
            )

    membership: dict[str, str] = {}
    test_ids = {item["identifier"] for item in tests}
    for item in classes:
        for test_id in item["tests"]:
            if test_id not in test_ids:
                raise AuditError(
                    f"Part {part} {item['identifier']}: unknown declared test {test_id}"
                )
            if test_id in membership:
                raise AuditError(
                    f"Part {part} {test_id}: declared by both {membership[test_id]} "
                    f"and {item['identifier']}"
                )
            membership[test_id] = item["identifier"]

    for item in tests:
        class_identifier = membership.get(item["identifier"])
        item["classIdentifier"] = class_identifier
        item["supporting"] = class_identifier is None

    return classes, tests


def semantic_digest(classes: list[dict[str, Any]], tests: list[dict[str, Any]]) -> str:
    payload = json.dumps(
        {"classes": classes, "tests": tests},
        sort_keys=True,
        separators=(",", ":"),
        ensure_ascii=True,
    ).encode("ascii")
    return hashlib.sha256(payload).hexdigest()


def validate_part(
    classes: list[dict[str, Any]],
    tests: list[dict[str, Any]],
    config: dict[str, Any],
) -> None:
    part = config["part"]
    class_ids = [item["identifier"] for item in classes]
    test_ids = [item["identifier"] for item in tests]
    if len(class_ids) != len(set(class_ids)):
        raise AuditError(f"Part {part}: duplicate conformance class identifier")
    if len(test_ids) != len(set(test_ids)):
        raise AuditError(f"Part {part}: duplicate abstract test identifier")
    supporting = sum(1 for item in tests if item["supporting"])
    expected = (
        config["classCount"],
        config["testCount"],
        config["supportingTestCount"],
    )
    actual = (len(classes), len(tests), supporting)
    if actual != expected:
        raise AuditError(
            f"Part {part}: expected classes/tests/supporting={expected}, got {actual}"
        )

    base = config["specificationBase"]
    tests_by_id = {item["identifier"]: item for item in tests}
    membership: dict[str, str] = {}
    for item in classes:
        identifier = item["identifier"]
        if item.get("fullIdentifier") != base + identifier:
            raise AuditError(f"Part {part} {identifier}: full identifier mismatch")
        target = item.get("target")
        if not isinstance(target, str) or not target.startswith(("/req/", "/rec/")):
            raise AuditError(f"Part {part} {identifier}: invalid class target")
        if item.get("fullTarget") != base + target:
            raise AuditError(f"Part {part} {identifier}: full target mismatch")
        declared = item.get("tests")
        if not isinstance(declared, list):
            raise AuditError(f"Part {part} {identifier}: tests must be an array")
        for test_id in declared:
            if test_id not in tests_by_id:
                raise AuditError(f"Part {part} {identifier}: unknown declared test {test_id}")
            if test_id in membership:
                raise AuditError(
                    f"Part {part} {test_id}: declared by both {membership[test_id]} "
                    f"and {identifier}"
                )
            membership[test_id] = identifier

    for item in tests:
        identifier = item["identifier"]
        if item.get("fullIdentifier") != base + identifier:
            raise AuditError(f"Part {part} {identifier}: full identifier mismatch")
        expected_class = membership.get(identifier)
        if item.get("classIdentifier") != expected_class:
            raise AuditError(
                f"Part {part} {identifier}: class membership mismatch "
                f"{item.get('classIdentifier')!r} != {expected_class!r}"
            )
        if item.get("supporting") != (expected_class is None):
            raise AuditError(f"Part {part} {identifier}: supporting classification mismatch")
        if item["supporting"] and item["target"] is not None:
            raise AuditError(
                f"Part {part} {identifier}: supporting test unexpectedly has target"
            )
        if not item["supporting"] and item["target"] is None:
            raise AuditError(
                f"Part {part} {identifier}: class test is missing target"
            )
        expected_full_target = base + item["target"] if item["target"] else None
        if item.get("fullTarget") != expected_full_target:
            raise AuditError(f"Part {part} {identifier}: full target mismatch")


def git_output(repository: Path, *arguments: str) -> str:
    process = subprocess.run(
        ["git", "-C", str(repository), *arguments],
        check=False,
        text=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    if process.returncode:
        detail = process.stderr.strip() or process.stdout.strip()
        raise AuditError(f"git {' '.join(arguments)} failed: {detail}")
    return process.stdout


def build_inventory(repository: Path) -> dict[str, Any]:
    resolved = git_output(repository, "rev-parse", f"{SOURCE_COMMIT}^{{commit}}").strip()
    if resolved != SOURCE_COMMIT:
        raise AuditError(f"Source commit resolved to {resolved}, expected {SOURCE_COMMIT}")
    tag_resolved = git_output(repository, "rev-parse", f"{SOURCE_TAG}^{{commit}}").strip()
    if tag_resolved != SOURCE_COMMIT:
        raise AuditError(
            f"Source tag {SOURCE_TAG} resolved to {tag_resolved}, expected {SOURCE_COMMIT}"
        )

    all_classes: list[dict[str, Any]] = []
    all_tests: list[dict[str, Any]] = []
    part_metadata: list[dict[str, Any]] = []
    for config in PARTS:
        source = git_output(
            repository,
            "show",
            f"{SOURCE_COMMIT}:{config['sourcePath']}",
        )
        classes, tests = parse_annex(source, config)
        validate_part(classes, tests, config)
        metadata = copy.deepcopy(config)
        metadata["semanticSha256"] = semantic_digest(classes, tests)
        part_metadata.append(metadata)
        all_classes.extend(classes)
        all_tests.extend(tests)

    return {
        "schemaVersion": "1.0",
        "source": {
            "repository": SOURCE_REPOSITORY,
            "tag": SOURCE_TAG,
            "commit": SOURCE_COMMIT,
            "parts": part_metadata,
        },
        "classes": all_classes,
        "tests": all_tests,
    }


def validate_inventory(inventory: dict[str, Any]) -> None:
    source = inventory.get("source", {})
    if inventory.get("schemaVersion") != "1.0":
        raise AuditError("Inventory schemaVersion must be 1.0")
    if source.get("repository") != SOURCE_REPOSITORY:
        raise AuditError("Inventory repository pin is incorrect")
    if source.get("tag") != SOURCE_TAG or source.get("commit") != SOURCE_COMMIT:
        raise AuditError("Inventory release tag/commit pin is incorrect")

    classes = inventory.get("classes")
    tests = inventory.get("tests")
    if not isinstance(classes, list) or not isinstance(tests, list):
        raise AuditError("Inventory classes/tests must be arrays")
    allowed_parts = {config["part"] for config in PARTS}
    for kind, items in (("class", classes), ("test", tests)):
        unexpected = sorted(
            {item.get("part") for item in items if item.get("part") not in allowed_parts},
            key=lambda value: str(value),
        )
        if unexpected:
            raise AuditError(f"Inventory contains unexpected {kind} parts: {unexpected}")
    source_parts = source.get("parts")
    if not isinstance(source_parts, list):
        raise AuditError("Inventory source parts must be an array")
    metadata_parts = [item.get("part") for item in source_parts]
    if len(metadata_parts) != len(allowed_parts) or set(metadata_parts) != allowed_parts:
        raise AuditError(f"Inventory source parts must be exactly {sorted(allowed_parts)}")
    for config in PARTS:
        part_classes = [item for item in classes if item.get("part") == config["part"]]
        part_tests = [item for item in tests if item.get("part") == config["part"]]
        validate_part(part_classes, part_tests, config)
        metadata = next(
            (
                item
                for item in source_parts
                if item.get("part") == config["part"]
            ),
            None,
        )
        if metadata is None:
            raise AuditError(f"Part {config['part']}: source metadata missing")
        actual_digest = semantic_digest(part_classes, part_tests)
        if metadata.get("semanticSha256") != actual_digest:
            raise AuditError(
                f"Part {config['part']}: semantic digest mismatch "
                f"{metadata.get('semanticSha256')} != {actual_digest}"
            )
        for key, expected in config.items():
            if metadata.get(key) != expected:
                raise AuditError(
                    f"Part {config['part']}: metadata {key} mismatch "
                    f"{metadata.get(key)!r} != {expected!r}"
                )


def write_json(path: Path, value: dict[str, Any]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2, ensure_ascii=True) + "\n")


def self_test() -> None:
    config = {
        "part": 9,
        "specificationBase": "http://example.test/spec/9",
        "classCount": 1,
        "testCount": 2,
        "supportingTestCount": 1,
    }
    source = """
[abstract_test]
====
[%metadata]
identifier:: /conf/example/support
====

[conformance_class]
====
[%metadata]
identifier:: /conf/example
target:: /req/example
conformance-test:: /conf/example/test
====

[abstract_test]
====
[%metadata]
identifier:: /conf/example/test
target:: /req/example/test
====
"""
    classes, tests = parse_annex(source, config)
    validate_part(classes, tests, config)
    if tests[0]["supporting"] is not True or tests[1]["classIdentifier"] != "/conf/example":
        raise AuditError("Self-test membership classification failed")

    tampered_tests = copy.deepcopy(tests)
    tampered_tests[1]["classIdentifier"] = "/conf/wrong-class"
    try:
        validate_part(classes, tampered_tests, config)
    except AuditError:
        pass
    else:
        raise AuditError("Self-test tampered class membership was not rejected")

    duplicate = source + """
[abstract_test]
====
[%metadata]
identifier:: /conf/example/test
target:: /req/example/test
====
"""
    try:
        duplicate_classes, duplicate_tests = parse_annex(duplicate, config)
        validate_part(duplicate_classes, duplicate_tests, config)
    except AuditError:
        pass
    else:
        raise AuditError("Self-test duplicate identifier was not rejected")

    print("PASS: ATS coverage audit self-test")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--self-test", action="store_true")
    parser.add_argument("--source-repo", type=Path)
    parser.add_argument("--inventory", type=Path, default=DEFAULT_INVENTORY)
    parser.add_argument("--write", action="store_true")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    try:
        if args.self_test:
            self_test()
            return 0
        if args.source_repo:
            generated = build_inventory(args.source_repo)
            if args.write:
                write_json(args.inventory, generated)
                print(f"WROTE: {args.inventory}")
            else:
                committed = json.loads(args.inventory.read_text())
                if committed != generated:
                    raise AuditError(
                        f"Committed inventory differs from {SOURCE_COMMIT} release source"
                    )
                print(
                    "PASS: committed ATS inventory matches released source "
                    f"{SOURCE_COMMIT}"
                )
        else:
            inventory = json.loads(args.inventory.read_text())
            validate_inventory(inventory)
            print("PASS: committed ATS inventory is internally consistent")
        return 0
    except (AuditError, OSError, json.JSONDecodeError) as error:
        print(f"FAIL: {error}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
