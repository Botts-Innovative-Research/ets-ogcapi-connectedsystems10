#!/usr/bin/env python3
"""Compare bundled resolver-normalized Property schemas with a pinned release."""

from __future__ import annotations

import argparse
import json
import posixpath
import sys
import tempfile
from pathlib import Path
from typing import Any
from urllib.parse import urldefrag, urlparse

SOURCE_COMMIT = "8e03b236a049849f2ccc24b4fd9fdce5ff69bed2"
LOCAL_PREFIX = "https://csapi-compliance.local/schemas/"
ENTRY_SCHEMAS = (
    "connected-systems-1/sensorml/property.json",
    "connected-systems-1/sensorml/propertyArray.json",
    "connected-systems-1/sensorml/propertyCollection.json",
)
RELEASE_MAPPINGS = (
    ("api/part1/openapi/schemas", "connected-systems-1"),
    ("api/part2/openapi/schemas", "connected-systems-2"),
    ("sensorml/schemas/json", "connected-systems-shared/sensorml/schemas/json"),
    ("swecommon/schemas/json", "connected-systems-shared/swecommon/schemas/json"),
    ("common", "connected-systems-shared/common"),
)


class ParityError(RuntimeError):
    """A fail-closed Property schema parity error."""


def release_path(release_root: Path, virtual_path: str) -> Path:
    for physical_prefix, virtual_prefix in RELEASE_MAPPINGS:
        if virtual_path == virtual_prefix or virtual_path.startswith(
            virtual_prefix + "/"
        ):
            suffix = virtual_path[len(virtual_prefix) :].lstrip("/")
            return release_root / physical_prefix / suffix
    raise ParityError(f"unmapped released virtual schema path: {virtual_path}")


def release_virtual_path(release_root: Path, physical_path: Path) -> str:
    resolved = physical_path.resolve()
    for physical_prefix, virtual_prefix in RELEASE_MAPPINGS:
        physical_root = (release_root / physical_prefix).resolve()
        try:
            suffix = resolved.relative_to(physical_root)
        except ValueError:
            continue
        return posixpath.join(virtual_prefix, suffix.as_posix())
    raise ParityError(f"released reference escapes mapped schema roots: {physical_path}")


def normalized_ref(
    value: str,
    virtual_path: str,
    physical_path: Path,
    release_root: Path | None,
) -> tuple[str, str | None]:
    target, fragment = urldefrag(value)
    parsed = urlparse(target)
    local_target: str | None = None
    if target.startswith(LOCAL_PREFIX):
        local_target = target[len(LOCAL_PREFIX) :]
    elif not parsed.scheme and target:
        if release_root is None:
            local_target = posixpath.normpath(
                posixpath.join(posixpath.dirname(virtual_path), target)
            )
        else:
            local_target = release_virtual_path(
                release_root, physical_path.parent / target
            )
    elif not target:
        local_target = virtual_path

    if local_target is None:
        return value, None
    normalized = "schema:///" + local_target
    if fragment:
        normalized += "#" + fragment
    return normalized, local_target


def normalize_document(
    document: Any,
    virtual_path: str,
    physical_path: Path,
    release_root: Path | None,
) -> tuple[Any, set[str]]:
    references: set[str] = set()

    def visit(value: Any) -> Any:
        if isinstance(value, list):
            return [visit(item) for item in value]
        if not isinstance(value, dict):
            return value

        normalized: dict[str, Any] = {}
        for key, item in value.items():
            if key == "$id":
                expected = LOCAL_PREFIX + virtual_path
                if release_root is not None or item != expected:
                    raise ParityError(
                        f"{virtual_path}: unexpected $id {item!r}; expected bundled "
                        f"resolver id {expected!r}"
                    )
                continue
            if key == "$ref":
                if not isinstance(item, str):
                    raise ParityError(f"{virtual_path}: non-string $ref")
                rewritten, target = normalized_ref(
                    item, virtual_path, physical_path, release_root
                )
                normalized[key] = rewritten
                if target is not None:
                    references.add(target)
                continue
            normalized[key] = visit(item)
        return normalized

    return visit(document), references


def read_graph(
    root: Path, entries: tuple[str, ...], release: bool
) -> dict[str, Any]:
    pending = list(entries)
    graph: dict[str, Any] = {}
    while pending:
        virtual_path = pending.pop()
        if virtual_path in graph:
            continue
        physical_path = (
            release_path(root, virtual_path) if release else root / virtual_path
        )
        if not physical_path.is_file():
            raise ParityError(f"missing schema {physical_path}")
        try:
            document = json.loads(physical_path.read_text(encoding="utf-8"))
        except json.JSONDecodeError as error:
            raise ParityError(f"invalid JSON schema {physical_path}: {error}") from error
        normalized, references = normalize_document(
            document,
            virtual_path,
            physical_path,
            root if release else None,
        )
        graph[virtual_path] = normalized
        pending.extend(sorted(references - graph.keys()))
    return graph


def compare(release_root: Path, bundled_root: Path) -> dict[str, Any]:
    release_graph = read_graph(release_root, ENTRY_SCHEMAS, True)
    bundled_graph = read_graph(bundled_root, ENTRY_SCHEMAS, False)
    release_paths = set(release_graph)
    bundled_paths = set(bundled_graph)
    if release_paths != bundled_paths:
        raise ParityError(
            "transitive graph mismatch: "
            f"missingBundled={sorted(release_paths - bundled_paths)}, "
            f"extraBundled={sorted(bundled_paths - release_paths)}"
        )
    mismatches = [
        path
        for path in sorted(release_paths)
        if release_graph[path] != bundled_graph[path]
    ]
    if mismatches:
        raise ParityError(f"semantic schema mismatch: {mismatches}")
    return {
        "sourceCommit": SOURCE_COMMIT,
        "entrySchemas": list(ENTRY_SCHEMAS),
        "transitiveSchemaCount": len(release_graph),
        "semanticMismatches": [],
        "graphMismatches": [],
        "allowedNormalization": [
            "expected local resolver $id",
            "equivalent relative-to-absolute local $ref",
        ],
        "status": "PASS",
    }


def write_fixture(root: Path, bundled: bool, changed: bool = False) -> None:
    schema_root = (
        root / "connected-systems-1/sensorml" if bundled else root / "sensorml"
    )
    first = schema_root / "property.json"
    second = schema_root / "shared.json"
    first.parent.mkdir(parents=True, exist_ok=True)
    document: dict[str, Any] = {
        "type": "object" if not changed else "array",
        "$ref": "shared.json",
    }
    shared: dict[str, Any] = {"type": "object"}
    if bundled:
        document["$id"] = LOCAL_PREFIX + "connected-systems-1/sensorml/property.json"
        document["$ref"] = LOCAL_PREFIX + "connected-systems-1/sensorml/shared.json"
        shared["$id"] = LOCAL_PREFIX + "connected-systems-1/sensorml/shared.json"
    first.write_text(json.dumps(document), encoding="utf-8")
    second.write_text(json.dumps(shared), encoding="utf-8")


def self_test() -> None:
    global ENTRY_SCHEMAS
    original_entries = ENTRY_SCHEMAS
    ENTRY_SCHEMAS = ("connected-systems-1/sensorml/property.json",)
    try:
        with tempfile.TemporaryDirectory() as directory:
            base = Path(directory)
            release_root = base / "release"
            bundled_root = base / "bundled"
            write_fixture(release_root / "api/part1/openapi/schemas", False)
            write_fixture(bundled_root, True)
            result = compare(release_root, bundled_root)
            if result["transitiveSchemaCount"] != 2:
                raise ParityError("self-test did not traverse the reference graph")
            write_fixture(bundled_root, True, changed=True)
            try:
                compare(release_root, bundled_root)
            except ParityError:
                pass
            else:
                raise ParityError("self-test accepted semantic drift")
    finally:
        ENTRY_SCHEMAS = original_entries
    print("PASS: Property schema parity self-test")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--self-test", action="store_true")
    parser.add_argument("--release-root", type=Path)
    parser.add_argument(
        "--bundled-root",
        type=Path,
        default=Path("src/main/resources/schemas"),
    )
    parser.add_argument("--output", type=Path)
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    try:
        if args.self_test:
            self_test()
            return 0
        if args.release_root is None:
            raise ParityError("--release-root is required outside self-test")
        result = compare(args.release_root, args.bundled_root)
        serialized = json.dumps(result, indent=2, sort_keys=True) + "\n"
        if args.output:
            args.output.parent.mkdir(parents=True, exist_ok=True)
            args.output.write_text(serialized, encoding="utf-8")
        print(serialized, end="")
        return 0
    except (OSError, ParityError, json.JSONDecodeError) as error:
        print(f"FAIL: {error}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
