#!/usr/bin/env python3
"""Behavioral tests for mutation-readiness-audit.py."""

# REQ-ETS-CLEANUP-023;
# SCENARIO-ETS-CLEANUP-MUTATION-READINESS-AUDIT-001.

import importlib.util
import pathlib
import tempfile
import unittest
from unittest import mock


SCRIPT_DIR = pathlib.Path(__file__).resolve().parent
AUDITOR_PATH = SCRIPT_DIR / "mutation-readiness-audit.py"
AUDITOR_SPEC = importlib.util.spec_from_file_location("mutation_readiness_audit", AUDITOR_PATH)
AUDITOR = importlib.util.module_from_spec(AUDITOR_SPEC)
AUDITOR_SPEC.loader.exec_module(AUDITOR)


class FakeClient:

    def __init__(self, declared, allow_by_path=None):
        self.declared = list(declared)
        self.allow_by_path = dict(allow_by_path or {})
        self.request_counts = {}
        self.unsafe_methods_issued = []

    def get_conformance(self):
        self.request_counts["GET"] = self.request_counts.get("GET", 0) + 1
        return {"status": 200, "contentType": "application/json"}, self.declared

    def options(self, path):
        self.request_counts["OPTIONS"] = self.request_counts.get("OPTIONS", 0) + 1
        methods = sorted(set(self.allow_by_path.get(path, [])))
        return {
            "status": 200,
            "contentType": "application/json",
            "allowMethods": methods,
        }


class MutationReadinessAuditTests(unittest.TestCase):

    def test_audit_is_read_only_and_keeps_all_mutation_candidates_non_exact(self):
        client = FakeClient(
            declared=[
                AUDITOR.CS1 + "/create-replace-delete",
                AUDITOR.FEATURES4 + "/create-replace-delete",
                AUDITOR.CS2 + "/create-replace-delete",
            ],
            allow_by_path={
                "/systems": ["GET", "POST", "OPTIONS"],
                "/systems/040g": ["GET", "PUT", "DELETE", "OPTIONS"],
            },
        )

        ids = {key: value or "040g" for key, value in AUDITOR.DEFAULT_IDS.items()}
        report = AUDITOR.build_audit(
            "http://example.test/api",
            client,
            ids,
            credential_supplied=True,
        )

        self.assertEqual(47, report["remainingCandidateProcedures"])
        self.assertFalse(report["exactPromotionReady"])
        self.assertTrue(report["credentialSupplied"])
        self.assertEqual([], report["unsafeMethodsIssued"])
        self.assertEqual({"GET": 1, "OPTIONS": 33}, report["requestMethodCounts"])
        self.assertIn("positive lifecycle proof", report["readinessScope"])
        self.assertEqual([], report["classesWithDeclarationAndMethodReadiness"])
        for class_report in report["classes"]:
            self.assertFalse(class_report["exactPromotionReady"])
            self.assertEqual(
                "declarations-and-advertised-methods-only",
                class_report["readinessScope"],
            )
            self.assertIn(
                "This audit is read-only readiness evidence only",
                report["exactPromotionPolicy"],
            )

    def test_missing_declarations_and_missing_ids_are_reported_as_blockers(self):
        client = FakeClient(declared=[])
        ids = dict(AUDITOR.DEFAULT_IDS)
        ids["observation"] = ""

        report = AUDITOR.build_audit("http://example.test/api", client, ids)
        update = next(item for item in report["classes"] if item["id"] == "part2Update")

        self.assertFalse(update["requiredConformancePresent"])
        self.assertIn(AUDITOR.CS2 + "/update", update["missingRequiredConformance"])
        self.assertTrue(update["missingConditionConformance"])
        skipped = [
            probe
            for probe in update["readinessProbes"]
            if probe.get("template") == "/observations/{observation}"
        ]
        self.assertEqual("empty resource id", skipped[0]["skipReason"])
        self.assertIn("missing required conformance declarations", update["exactPromotionBlockers"])

    def test_main_writes_json_without_serializing_credential_value(self):
        with tempfile.TemporaryDirectory() as directory:
            output = pathlib.Path(directory) / "audit.json"
            fake_client = FakeClient(
                declared=[
                    AUDITOR.CS1 + "/create-replace-delete",
                    AUDITOR.FEATURES4 + "/create-replace-delete",
                ]
            )
            with mock.patch.object(AUDITOR, "HttpClient", return_value=fake_client):
                status = AUDITOR.main(
                    [
                        "--iut-url",
                        "http://example.test/api",
                        "--auth-credential",
                        "Bearer should-not-be-written",
                        "--output",
                        str(output),
                    ]
                )

            self.assertEqual(0, status)
            text = output.read_text(encoding="utf-8")
            self.assertNotIn("should-not-be-written", text)
            self.assertIn('"credentialSupplied": true', text)

    def test_parse_allow_normalizes_case_and_empties(self):
        self.assertEqual(["DELETE", "GET", "POST"], AUDITOR.parse_allow("get, POST, ,delete,GET"))


if __name__ == "__main__":
    unittest.main()
