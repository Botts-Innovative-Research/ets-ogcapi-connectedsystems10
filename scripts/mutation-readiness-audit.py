#!/usr/bin/env python3
"""Audit remaining mutation-bound ATS candidates without issuing writes."""

from __future__ import annotations

import argparse
import datetime
import json
import pathlib
import re
import sys
import urllib.error
import urllib.parse
import urllib.request


CS1 = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf"
CS2 = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf"
FEATURES4 = "http://www.opengis.net/spec/ogcapi-features-4/1.0/conf"
OGCAPI4 = "http://www.opengis.net/spec/ogcapi-4/1.0/conf"
UNSAFE_METHODS = {"POST", "PUT", "PATCH", "DELETE"}

DEFAULT_IDS = {
    "system": "040g",
    "deployment": "040g",
    "procedure": "040g",
    "samplingFeature": "040g",
    "property": "040g",
    "dataStream": "040g",
    "observation": "",
    "controlStream": "040g",
    "command": "",
    "feasibility": "",
    "systemEvent": "",
}

ID_ARGUMENTS = {
    "system": "--system-id",
    "deployment": "--deployment-id",
    "procedure": "--procedure-id",
    "samplingFeature": "--sampling-feature-id",
    "property": "--property-id",
    "dataStream": "--datastream-id",
    "observation": "--observation-id",
    "controlStream": "--controlstream-id",
    "command": "--command-id",
    "feasibility": "--feasibility-id",
    "systemEvent": "--system-event-id",
}

CLASS_AUDITS = [
    {
        "id": "part1CreateReplaceDelete",
        "requirement": "REQ-ETS-PART1-010",
        "conformanceClass": "1:/conf/create-replace-delete",
        "candidateProcedures": 12,
        "requiredConformance": [
            f"{CS1}/create-replace-delete",
            f"{FEATURES4}/create-replace-delete",
        ],
        "prerequisiteConformance": [
            f"{CS1}/api-common",
            f"{OGCAPI4}/create-replace-delete",
        ],
        "probes": [
            ("/systems", ["POST"]),
            ("/systems/{system}", ["PUT", "DELETE"]),
            ("/deployments", ["POST"]),
            ("/deployments/{deployment}", ["PUT", "DELETE"]),
            ("/procedures", ["POST"]),
            ("/procedures/{procedure}", ["PUT", "DELETE"]),
            ("/samplingFeatures", ["POST"]),
            ("/samplingFeatures/{samplingFeature}", ["PUT", "DELETE"]),
            ("/properties", ["POST"]),
            ("/properties/{property}", ["PUT", "DELETE"]),
        ],
        "positiveEvidenceRequired": [
            "dedicated mutable-IUT POST/PUT/DELETE lifecycle execution",
            "changed resource GET proof after write",
            "identity-safe cleanup and primary-state isolation",
            "cascade, collection propagation, and text/uri-list evidence where applicable",
        ],
    },
    {
        "id": "part1Update",
        "requirement": "REQ-ETS-PART1-011",
        "conformanceClass": "1:/conf/update",
        "candidateProcedures": 5,
        "requiredConformance": [
            f"{CS1}/update",
            f"{FEATURES4}/update",
        ],
        "prerequisiteConformance": [
            f"{CS1}/api-common",
            f"{OGCAPI4}/update",
        ],
        "probes": [
            ("/systems/{system}", ["PATCH"]),
            ("/deployments/{deployment}", ["PATCH"]),
            ("/procedures/{procedure}", ["PATCH"]),
            ("/samplingFeatures/{samplingFeature}", ["PATCH"]),
            ("/properties/{property}", ["PATCH"]),
        ],
        "positiveEvidenceRequired": [
            "dedicated mutable-IUT PATCH lifecycle execution",
            "changed field GET proof after PATCH",
            "identity-safe cleanup and primary-state isolation",
            "applicability evidence for custom collections, async, and deadline behavior",
        ],
    },
    {
        "id": "part2CreateReplaceDelete",
        "requirement": "REQ-ETS-PART2-007",
        "conformanceClass": "2:/conf/create-replace-delete",
        "candidateProcedures": 16,
        "requiredConformance": [
            f"{CS2}/create-replace-delete",
            f"{FEATURES4}/create-replace-delete",
        ],
        "prerequisiteConformance": [
            f"{CS2}/api-common",
        ],
        "probes": [
            ("/systems/{system}/datastreams", ["POST"]),
            ("/datastreams/{dataStream}", ["PUT", "DELETE"]),
            ("/datastreams/{dataStream}/observations", ["POST"]),
            ("/observations/{observation}", ["PUT", "DELETE"]),
            ("/systems/{system}/controlstreams", ["POST"]),
            ("/controlstreams/{controlStream}", ["PUT", "DELETE"]),
            ("/controlstreams/{controlStream}/commands", ["POST"]),
            ("/commands/{command}", ["PUT", "DELETE"]),
            ("/feasibility", ["POST"]),
            ("/feasibility/{feasibility}", ["PUT", "DELETE"]),
            ("/systemEvents", ["POST"]),
            ("/systemEvents/{systemEvent}", ["PUT", "DELETE"]),
        ],
        "positiveEvidenceRequired": [
            "dedicated mutable-IUT POST/PUT/DELETE lifecycle execution",
            "changed resource GET proof after write",
            "identity-safe cleanup and primary-state isolation",
            "resource-specific command, feasibility, result/status, and event evidence",
        ],
    },
    {
        "id": "part2Update",
        "requirement": "REQ-ETS-PART2-008",
        "conformanceClass": "2:/conf/update",
        "candidateProcedures": 14,
        "requiredConformance": [
            f"{CS2}/update",
            f"{CS2}/create-replace-delete",
            f"{FEATURES4}/update",
        ],
        "prerequisiteConformance": [
            f"{CS2}/api-common",
        ],
        "conditionConformance": [
            f"{CS2}/datastream",
            f"{CS2}/controlstream",
            f"{CS2}/feasibility",
            f"{CS2}/system-event",
        ],
        "probes": [
            ("/datastreams/{dataStream}", ["PATCH"]),
            ("/observations/{observation}", ["PATCH"]),
            ("/controlstreams/{controlStream}", ["PATCH"]),
            ("/commands/{command}", ["PATCH"]),
            ("/feasibility/{feasibility}", ["PATCH"]),
            ("/systemEvents/{systemEvent}", ["PATCH"]),
        ],
        "positiveEvidenceRequired": [
            "dedicated mutable-IUT PATCH lifecycle execution",
            "changed field GET proof after PATCH",
            "identity-safe cleanup and primary-state isolation",
            "schema-rejection PATCH dispatch evidence",
        ],
    },
]


def utc_timestamp():
    return datetime.datetime.now(datetime.timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z")


def atomic_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_name(path.name + ".tmp")
    temporary.write_text(json.dumps(value, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    temporary.replace(path)


def parse_allow(value):
    if not value:
        return []
    return sorted({item.strip().upper() for item in value.split(",") if item.strip()})


def extract_resource_ids(path):
    if path is None:
        return {}
    document = json.loads(path.read_text(encoding="utf-8"))
    values = document.get("resourceIds", document)
    return {key: str(value) for key, value in values.items() if value is not None}


def normalize_resource_ids(args):
    ids = dict(DEFAULT_IDS)
    ids.update(extract_resource_ids(args.resource_ids_json))
    for key, argument in ID_ARGUMENTS.items():
        value = getattr(args, argument[2:].replace("-", "_"))
        if value:
            ids[key] = value
    return ids


def format_probe_path(template, ids):
    for name in re.findall(r"{([^{}]+)}", template):
        if not ids.get(name):
            return None, "empty resource id"
    try:
        path = template.format(**ids)
    except KeyError as error:
        return None, f"missing placeholder {error.args[0]}"
    if "{" in path or "}" in path:
        return None, "unresolved placeholder"
    if "//" in path:
        return None, "empty resource id"
    return path, None


class HttpClient:

    def __init__(self, base_url, auth_credential=None, timeout=10.0):
        self.base_url = base_url.rstrip("/")
        self.auth_credential = auth_credential
        self.timeout = timeout
        self.request_counts = {}
        self.unsafe_methods_issued = []

    def request(self, method, path):
        method = method.upper()
        self.request_counts[method] = self.request_counts.get(method, 0) + 1
        if method in UNSAFE_METHODS:
            self.unsafe_methods_issued.append({"method": method, "path": path})
        headers = {"Accept": "application/json, */*"}
        if self.auth_credential:
            headers["Authorization"] = self.auth_credential
        request = urllib.request.Request(self.base_url + path, headers=headers, method=method)
        try:
            with urllib.request.urlopen(request, timeout=self.timeout) as response:
                body = response.read().decode("utf-8", errors="replace")
                return {
                    "status": response.status,
                    "contentType": response.headers.get("Content-Type", ""),
                    "allow": response.headers.get("Allow", ""),
                    "body": body,
                }
        except urllib.error.HTTPError as error:
            body = error.read().decode("utf-8", errors="replace")
            return {
                "status": error.code,
                "contentType": error.headers.get("Content-Type", ""),
                "allow": error.headers.get("Allow", ""),
                "body": body,
                "errorType": "http-error",
            }
        except (OSError, TimeoutError) as error:
            return {
                "status": None,
                "contentType": "",
                "allow": "",
                "body": "",
                "errorType": type(error).__name__,
                "error": str(error),
            }

    def get_conformance(self):
        response = self.request("GET", "/conformance")
        if response["status"] != 200:
            return response, []
        try:
            body = json.loads(response["body"])
        except json.JSONDecodeError:
            return response, []
        conforms_to = body.get("conformsTo", [])
        if not isinstance(conforms_to, list):
            conforms_to = []
        return response, [str(item) for item in conforms_to]

    def options(self, path):
        response = self.request("OPTIONS", path)
        response = dict(response)
        response["allowMethods"] = parse_allow(response.get("allow", ""))
        response.pop("body", None)
        return response


def audit_class(audit, declared, ids, client):
    required = audit["requiredConformance"]
    prerequisites = audit.get("prerequisiteConformance", [])
    condition = audit.get("conditionConformance", [])
    missing_required = [uri for uri in required if uri not in declared]
    missing_prerequisites = [uri for uri in prerequisites if uri not in declared]
    missing_condition = [uri for uri in condition if uri not in declared]
    blockers = []
    if missing_required:
        blockers.append("missing required conformance declarations")
    if missing_prerequisites:
        blockers.append("missing inherited prerequisite conformance declarations")
    if missing_condition:
        blockers.append("missing condition conformance declarations")

    probes = []
    missing_methods = []
    for template, expected_methods in audit["probes"]:
        path, skip_reason = format_probe_path(template, ids)
        if skip_reason:
            probes.append(
                {
                    "template": template,
                    "skipped": True,
                    "skipReason": skip_reason,
                    "expectedMethods": expected_methods,
                }
            )
            missing_methods.append({"path": template, "missing": expected_methods, "reason": skip_reason})
            continue
        response = client.options(path)
        allow_methods = response.get("allowMethods", [])
        missing = [method for method in expected_methods if method not in allow_methods]
        if missing:
            missing_methods.append({"path": path, "missing": missing, "allowMethods": allow_methods})
        probes.append(
            {
                "template": template,
                "path": path,
                "status": response.get("status"),
                "contentType": response.get("contentType", ""),
                "allowMethods": allow_methods,
                "expectedMethods": expected_methods,
                "advertisesExpectedMethods": not missing,
                "errorType": response.get("errorType"),
            }
        )
    if missing_methods:
        blockers.append("missing advertised mutation methods on readiness probes")

    positive_required = list(audit["positiveEvidenceRequired"])
    blockers.extend(positive_required)
    return {
        "id": audit["id"],
        "requirement": audit["requirement"],
        "conformanceClass": audit["conformanceClass"],
        "candidateProcedures": audit["candidateProcedures"],
        "requiredConformancePresent": not missing_required,
        "prerequisiteConformancePresent": not missing_prerequisites,
        "missingRequiredConformance": missing_required,
        "missingPrerequisiteConformance": missing_prerequisites,
        "missingConditionConformance": missing_condition,
        "readinessProbes": probes,
        "missingAdvertisedMethods": missing_methods,
        "readinessScope": "declarations-and-advertised-methods-only",
        "declarationAndMethodReadiness": not missing_required
        and not missing_condition
        and not missing_methods,
        "prerequisiteReadinessScope": "declarations-advertised-methods-and-inherited-prerequisites",
        "declarationMethodAndPrerequisiteReadiness": not missing_required
        and not missing_prerequisites
        and not missing_condition
        and not missing_methods,
        "exactPromotionReady": False,
        "exactPromotionBlockers": blockers,
    }


def build_audit(iut_url, client, ids, credential_supplied=False):
    conformance_response, declared = client.get_conformance()
    classes = [audit_class(item, set(declared), ids, client) for item in CLASS_AUDITS]
    total_candidates = sum(item["candidateProcedures"] for item in classes)
    ready_classes = [
        item["conformanceClass"] for item in classes if item["declarationAndMethodReadiness"]
    ]
    prerequisite_ready_classes = [
        item["conformanceClass"] for item in classes if item["declarationMethodAndPrerequisiteReadiness"]
    ]
    return {
        "schemaVersion": 1,
        "generatedAt": utc_timestamp(),
        "iutUrl": iut_url.rstrip("/"),
        "credentialSupplied": bool(credential_supplied),
        "requestMethodCounts": dict(sorted(client.request_counts.items())),
        "unsafeMethodsIssued": list(client.unsafe_methods_issued),
        "remainingCandidateProcedures": total_candidates,
        "readinessScope": "Read-only declaration and OPTIONS method advertisement audit; positive lifecycle proof is still required before exact promotion.",
        "classesWithDeclarationAndMethodReadiness": ready_classes,
        "classesWithDeclarationMethodAndPrerequisiteReadiness": prerequisite_ready_classes,
        "prerequisiteReadinessPolicy": "Prerequisite-aware readiness also requires inherited conformance declarations; exact positive lifecycle proof is still required before exact promotion.",
        "exactPromotionReady": False,
        "exactPromotionPolicy": "This audit is read-only readiness evidence only; it never promotes mutation candidates to reviewed exact mappings.",
        "conformance": {
            "status": conformance_response.get("status"),
            "contentType": conformance_response.get("contentType", ""),
            "declaredCount": len(declared),
            "declaredMutationRelevant": sorted(
                item
                for item in declared
                if "/create-replace-delete" in item
                or "/update" in item
                or "/api-common" in item
                or item.startswith(OGCAPI4)
                or item.startswith(CS2)
            ),
        },
        "resourceIds": ids,
        "classes": classes,
    }


def parse_args(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--iut-url", required=True)
    parser.add_argument("--output", required=True, type=pathlib.Path)
    parser.add_argument("--auth-credential", default=None)
    parser.add_argument("--timeout", type=float, default=10.0)
    parser.add_argument("--resource-ids-json", type=pathlib.Path)
    for key, argument in ID_ARGUMENTS.items():
        parser.add_argument(argument, dest=argument[2:].replace("-", "_"))
    return parser.parse_args(argv)


def main(argv=None):
    args = parse_args(sys.argv[1:] if argv is None else argv)
    auth = args.auth_credential
    ids = normalize_resource_ids(args)
    client = HttpClient(args.iut_url, auth_credential=auth, timeout=args.timeout)
    audit = build_audit(args.iut_url, client, ids, credential_supplied=auth is not None)
    atomic_json(args.output, audit)
    if audit["unsafeMethodsIssued"]:
        print("unsafe methods were issued; audit is invalid", file=sys.stderr)
        return 2
    print(
        "mutation readiness audit wrote "
        f"{args.output} with {audit['remainingCandidateProcedures']} candidate procedures; "
        f"declaration/method-ready classes: "
        f"{len(audit['classesWithDeclarationAndMethodReadiness'])}; "
        f"prerequisite-ready classes: "
        f"{len(audit['classesWithDeclarationMethodAndPrerequisiteReadiness'])}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
