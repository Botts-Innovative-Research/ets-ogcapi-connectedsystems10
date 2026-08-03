#!/usr/bin/env python3
"""Behavioral regressions for the owned populated local OSH workflow."""

# REQ-ETS-PART2-013;
# SCENARIO-ETS-PART2-013-EPHEMERAL-POPULATED-IUT-001;
# SCENARIO-ETS-PART2-013-POPULATED-PROVISIONING-VERDICT-001;
# SCENARIO-ETS-PART2-013-POPULATED-EVIDENCE-001;
# SCENARIO-ETS-PART2-013-PRIMARY-STATE-ISOLATION-001;
# SCENARIO-ETS-PART2-013-POPULATED-COMMAND-PROBE-DIAGNOSTICS-001.
# REQ-ETS-CLEANUP-023;
# SCENARIO-ETS-CLEANUP-MUTATION-READINESS-AUDIT-001.

import copy
import importlib.util
import json
import os
import pathlib
import sys
import tempfile
import unittest
from unittest import mock

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import local_osh_populated_e2e as workflow_module


SCRIPT_DIR = pathlib.Path(__file__).resolve().parent
SEEDER_PATH = SCRIPT_DIR / "local-osh-populated-fixture.py"
SEEDER_SPEC = importlib.util.spec_from_file_location("local_osh_populated_fixture", SEEDER_PATH)
SEEDER = importlib.util.module_from_spec(SEEDER_SPEC)
SEEDER_SPEC.loader.exec_module(SEEDER)


def container_document(
    *,
    container_id="a" * 64,
    name="ets-csapi-populated-osh-testsafe-123",
    run_id="testsafe-123",
    state_source="/tmp/owned-state",
    install_source="/tmp/owned-install",
    host_port="18082",
):
    network_alias = workflow_module.docker_dns_alias(run_id)
    return {
        "Id": container_id,
        "Name": "/" + name,
        "Image": "sha256:" + "b" * 64,
        "Config": {
            "Image": workflow_module.DEFAULT_OSH_IMAGE,
            "Entrypoint": ["/entrypoint"],
            "Cmd": ["java"],
            "User": "1000:1000",
            "WorkingDir": "/state",
            "Labels": {
                workflow_module.RUN_LABEL: run_id,
                workflow_module.ROLE_LABEL: "populated-osh",
            },
        },
        "State": {"Running": True, "Status": "running"},
        "Mounts": [
            {
                "Type": "bind",
                "Source": state_source,
                "Destination": "/state",
                "RW": True,
            },
            {
                "Type": "bind",
                "Source": install_source,
                "Destination": "/opt/osh",
                "RW": False,
            },
        ],
        "NetworkSettings": {
            "Networks": {"field-hub_default": {"Aliases": [name, network_alias]}},
            "Ports": {"8081/tcp": [{"HostIp": "127.0.0.1", "HostPort": host_port}]},
        },
    }


def primary_document():
    document = container_document(
        name="field-hub-osh-1",
        run_id="not-used",
        state_source="/tmp/primary-state",
        install_source="/tmp/owned-install",
    )
    document["Config"]["Labels"] = {"service": "primary"}
    return document


class FakeDockerRunner:

    def __init__(self, document):
        self.document = document
        self.removed = False
        self.calls = []

    def run(self, args, *, check=True, env=None):
        del check, env
        args = [str(arg) for arg in args]
        self.calls.append(args)
        if args[1] == "inspect":
            if self.removed:
                return workflow_module.subprocess.CompletedProcess(args, 1, "", "missing")
            return workflow_module.subprocess.CompletedProcess(
                args, 0, json.dumps([self.document]), ""
            )
        if args[1:3] == ["rm", "-f"]:
            self.removed = True
            return workflow_module.subprocess.CompletedProcess(args, 0, args[-1], "")
        if args[1] == "logs":
            return workflow_module.subprocess.CompletedProcess(args, 0, "owned log\n", "")
        raise AssertionError(f"unexpected fake Docker command: {args}")

    def stream(self, args, output_file, *, env=None):
        del args, output_file, env
        raise AssertionError("stream was not expected")


class StreamCaptureRunner:

    def __init__(self, status=0):
        self.status = status
        self.calls = []

    def run(self, args, *, check=True, env=None):
        del args, check, env
        raise AssertionError("run was not expected")

    def stream(self, args, output_file, *, env=None):
        self.calls.append(
            {
                "args": [str(arg) for arg in args],
                "output": str(output_file),
                "env": dict(env or {}),
            }
        )
        pathlib.Path(output_file).write_text("audit stdout\n", encoding="utf-8")
        return self.status


class StartOshRunner:

    def __init__(self, state, install):
        self.state = pathlib.Path(state)
        self.install = pathlib.Path(install)
        self.calls = []
        self.container_id = "c" * 64
        self.workflow = None

    def run(self, args, *, check=True, env=None):
        del check, env
        args = [str(arg) for arg in args]
        self.calls.append(args)
        if args[1] == "inspect" and args[-1] == self.workflow.osh_name:
            return workflow_module.subprocess.CompletedProcess(args, 1, "", "missing")
        if args[1] == "run":
            return workflow_module.subprocess.CompletedProcess(args, 0, self.container_id + "\n", "")
        if args[1] == "inspect" and args[-1] == self.container_id:
            document = container_document(
                container_id=self.container_id,
                name=self.workflow.osh_name,
                run_id=self.workflow.run_id,
                state_source=str(self.state),
                install_source=str(self.install),
            )
            return workflow_module.subprocess.CompletedProcess(
                args, 0, json.dumps([document]), ""
            )
        raise AssertionError(f"unexpected fake Docker command: {args}")

    def stream(self, args, output_file, *, env=None):
        del args, output_file, env
        raise AssertionError("stream was not expected")


class FinalizationProbe(workflow_module.PopulatedWorkflow):

    def __init__(self, phase, cleanup_failure=False):
        environment = {
            "LOCAL_OSH_RUN_ID": "testsafe-123",
            "SMOKE_OUTPUT_DIR": "/tmp/testsafe-output",
        }
        super().__init__(environment=environment)
        self.injected_phase = phase
        self.cleanup_failure = cleanup_failure
        self.calls = []

    def preflight(self):
        self.calls.append("preflight")

    def capture_primary(self, label):
        self.calls.append(f"capture-{label}")
        return {"stable": True}, ["stable"]

    def run_populated(self):
        self.phase = self.injected_phase
        raise workflow_module.WorkflowError(f"injected {self.injected_phase} failure")

    def cleanup_owned_resources(self):
        self.calls.append("cleanup")
        if self.cleanup_failure:
            raise workflow_module.WorkflowError("injected cleanup failure")
        self.cleanup_verdict = "PASS"

    def compare_primary(self):
        self.calls.append("compare-primary")
        self.primary_unchanged = True

    def run_clean_primary(self):
        self.calls.append("clean-primary")

    def build_summary(self):
        self.calls.append("summary")
        return 1


class WorkflowBehaviorTests(unittest.TestCase):

    def test_testng_verdict_comes_only_from_xml_and_extracts_exact_failures(self):
        with tempfile.TemporaryDirectory() as directory:
            report = pathlib.Path(directory) / "testng-results.xml"
            methods = "".join(
                (
                    f'<test-method status="FAIL" name="failure-{index}" '
                    f'description="description-{index}">'
                    f'<exception><message>message-{index}</message></exception>'
                    "</test-method>"
                )
                for index in range(28)
            )
            report.write_text(
                '<testng-results total="30" passed="1" failed="28" skipped="1">'
                f'<suite><test><class name="example.StrictTests">{methods}</class></test></suite>'
                "</testng-results>",
                encoding="utf-8",
            )
            evidence = workflow_module.parse_testng_report(report)
            self.assertEqual("FAIL", evidence["verdict"])
            self.assertEqual(28, len(evidence["failures"]))
            self.assertEqual(
                {
                    "class": "example.StrictTests",
                    "name": "failure-27",
                    "description": "description-27",
                    "message": "message-27",
                },
                evidence["failures"][-1],
            )

    def test_testng_rejects_unreconciled_totals(self):
        with tempfile.TemporaryDirectory() as directory:
            report = pathlib.Path(directory) / "testng-results.xml"
            report.write_text(
                '<testng-results total="2" passed="1" failed="0" skipped="0"/>',
                encoding="utf-8",
            )
            with self.assertRaisesRegex(workflow_module.WorkflowError, "do not reconcile"):
                workflow_module.parse_testng_report(report)

    def test_primary_fingerprint_detects_identity_image_config_state_network_and_mount_drift(self):
        baseline_document = primary_document()
        baseline = workflow_module.normalized_primary_fingerprint(baseline_document)
        mutations = [
            ("Id", "replacement-id"),
            ("Image", "sha256:replacement"),
            ("Config", "Cmd", ["different"]),
            ("State", "Running", False),
            ("NetworkSettings", "Networks", {"different-network": {}}),
            ("Mounts", 0, "Source", "/tmp/different-state"),
            ("Mounts", 1, "RW", True),
        ]
        for mutation in mutations:
            with self.subTest(mutation=mutation):
                document = copy.deepcopy(baseline_document)
                target = document
                for key in mutation[:-2]:
                    target = target[key]
                target[mutation[-2]] = mutation[-1]
                changed = workflow_module.normalized_primary_fingerprint(document)
                self.assertNotEqual(baseline, changed)

    def test_cleanup_removes_only_owned_container_id(self):
        document = container_document()
        runner = FakeDockerRunner(document)
        with tempfile.TemporaryDirectory() as directory:
            workflow = workflow_module.PopulatedWorkflow(
                runner=runner,
                environment={
                    "LOCAL_OSH_RUN_ID": "testsafe-123",
                    "SMOKE_OUTPUT_DIR": directory,
                },
            )
            workflow.owned_container_id = document["Id"]
            workflow.runtime_state = pathlib.Path("/tmp/owned-state")
            workflow.install = pathlib.Path("/tmp/owned-install")
            workflow.cleanup_owned_resources()
            removal = [call for call in runner.calls if call[1:3] == ["rm", "-f"]]
            self.assertEqual([["docker", "rm", "-f", document["Id"]]], removal)
            self.assertEqual("PASS", workflow.cleanup_verdict)

    def test_cleanup_refuses_unowned_container_and_never_removes_it(self):
        document = container_document()
        document["Config"]["Labels"][workflow_module.RUN_LABEL] = "another-run"
        runner = FakeDockerRunner(document)
        with tempfile.TemporaryDirectory() as directory:
            workflow = workflow_module.PopulatedWorkflow(
                runner=runner,
                environment={
                    "LOCAL_OSH_RUN_ID": "testsafe-123",
                    "SMOKE_OUTPUT_DIR": directory,
                },
            )
            workflow.owned_container_id = document["Id"]
            workflow.runtime_state = pathlib.Path("/tmp/owned-state")
            workflow.install = pathlib.Path("/tmp/owned-install")
            with self.assertRaisesRegex(workflow_module.WorkflowError, "ownership labels"):
                workflow.cleanup_owned_resources()
            self.assertFalse(any(call[1:3] == ["rm", "-f"] for call in runner.calls))

    def test_primary_and_existing_unrelated_names_are_rejected_without_removal(self):
        document = container_document(name="unrelated")
        runner = FakeDockerRunner(document)
        workflow = workflow_module.PopulatedWorkflow(
            runner=runner,
            environment={
                "LOCAL_OSH_RUN_ID": "testsafe-123",
                "SMOKE_OUTPUT_DIR": "/tmp/testsafe-output",
            },
        )
        with self.assertRaisesRegex(workflow_module.WorkflowError, "collides with primary"):
            workflow.require_name_available(workflow.primary_name)
        with self.assertRaisesRegex(workflow_module.WorkflowError, "unowned"):
            workflow.require_name_available("unrelated")
        self.assertFalse(any(call[1:3] == ["rm", "-f"] for call in runner.calls))

    def test_started_populated_iut_uses_short_network_alias_for_teamengine_url(self):
        run_id = "sprint-ets-72-readiness-20260803T014708Z"
        with tempfile.TemporaryDirectory() as directory:
            root = pathlib.Path(directory)
            state = root / "state"
            install = root / "install"
            output = root / "output"
            for path in (state, install, output):
                path.mkdir()
            runner = StartOshRunner(state, install)
            workflow = workflow_module.PopulatedWorkflow(
                runner=runner,
                environment={
                    "LOCAL_OSH_RUN_ID": run_id,
                    "SMOKE_OUTPUT_DIR": str(output),
                },
            )
            runner.workflow = workflow
            workflow.runtime_state = state
            workflow.install = install

            workflow.start_osh()

            docker_run = next(call for call in runner.calls if call[1] == "run")
            self.assertGreater(len(workflow.osh_name), 63)
            self.assertLessEqual(len(workflow.osh_alias), 63)
            self.assertIn("--network-alias", docker_run)
            self.assertIn(workflow.osh_alias, docker_run)
            self.assertEqual(
                f"http://{workflow.osh_alias}:8081/sensorhub/api",
                workflow.docker_iut_url,
            )
            ownership = json.loads((output / "ownership-evidence.json").read_text())
            self.assertEqual(workflow.osh_alias, ownership["networkAlias"])

    def test_every_started_abort_phase_runs_all_finalizers(self):
        for phase in ("startup", "provisioning", "populated-smoke", "evidence-parsing"):
            with self.subTest(phase=phase):
                workflow = FinalizationProbe(phase)
                self.assertEqual(1, workflow.execute())
                self.assertEqual(
                    [
                        "preflight",
                        "capture-before",
                        "cleanup",
                        "compare-primary",
                        "clean-primary",
                        "summary",
                    ],
                    workflow.calls,
                )
                self.assertEqual(phase, workflow.abort_phase)

    def test_cleanup_failure_does_not_skip_remaining_finalizers(self):
        workflow = FinalizationProbe("startup", cleanup_failure=True)
        self.assertEqual(1, workflow.execute())
        self.assertEqual(
            [
                "preflight",
                "capture-before",
                "cleanup",
                "compare-primary",
                "clean-primary",
                "summary",
            ],
            workflow.calls,
        )
        self.assertIn("cleanup: injected cleanup failure", workflow.finalization_errors)

    def test_mutation_readiness_audit_uses_provisioned_ids_without_forwarding_credentials(self):
        with tempfile.TemporaryDirectory() as directory:
            runner = StreamCaptureRunner()
            workflow = workflow_module.PopulatedWorkflow(
                runner=runner,
                environment={
                    "LOCAL_OSH_RUN_ID": "testsafe-123",
                    "SMOKE_OUTPUT_DIR": directory,
                    "SMOKE_AUTH_CREDENTIAL": "Bearer should-not-forward",
                },
            )
            workflow.loopback_url = "http://127.0.0.1:18082/sensorhub/api"
            (pathlib.Path(directory) / "provisioning-evidence.json").write_text(
                json.dumps({"resourceIds": {"system": "s1"}}), encoding="utf-8"
            )

            workflow.run_mutation_readiness_audit()

            self.assertEqual(0, workflow.populated_readiness_status)
            self.assertEqual(1, len(runner.calls))
            command = runner.calls[0]["args"]
            self.assertIn("--resource-ids-json", command)
            self.assertIn(str(pathlib.Path(directory) / "provisioning-evidence.json"), command)
            self.assertNotIn("SMOKE_AUTH_CREDENTIAL", runner.calls[0]["env"])


class SeederOwnershipBehaviorTests(unittest.TestCase):

    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.root = pathlib.Path(self.temporary.name)
        self.state = self.root / "state"
        self.install = self.root / "install"
        self.state.mkdir()
        self.install.mkdir()
        self.document_path = self.root / "docker-inspect.json"
        self.docker = self.root / "fake-docker"
        self.docker.write_text(
            "#!/usr/bin/env bash\ncat \"$FAKE_DOCKER_DOCUMENT\"\n", encoding="utf-8"
        )
        self.docker.chmod(0o700)
        self.document = container_document(
            state_source=str(self.state), install_source=str(self.install)
        )
        self.ownership = {
            "schemaVersion": 1,
            "runId": "testsafe-123",
            "containerId": self.document["Id"],
            "containerName": "ets-csapi-populated-osh-testsafe-123",
            "host": "127.0.0.1",
            "hostPort": 18082,
            "containerPort": 8081,
            "apiPath": "/sensorhub/api",
            "stateSource": str(self.state),
            "installSource": str(self.install),
            "network": "field-hub_default",
        }
        self.ownership_path = self.root / "ownership.json"
        self.write_evidence()

    def tearDown(self):
        self.temporary.cleanup()

    def write_evidence(self):
        self.document_path.write_text(
            json.dumps([self.document]), encoding="utf-8"
        )
        self.ownership_path.write_text(
            json.dumps(self.ownership), encoding="utf-8"
        )
        self.ownership_path.chmod(0o600)

    def validate(self, url="http://127.0.0.1:18082/sensorhub/api"):
        environment = {
            "LOCAL_OSH_DOCKER_BIN": str(self.docker),
            "FAKE_DOCKER_DOCUMENT": str(self.document_path),
        }
        with mock.patch.dict(os.environ, environment, clear=False):
            return SEEDER.validate_owned_target(url, self.ownership_path)

    def test_accepts_exact_owned_endpoint(self):
        parsed, ownership = self.validate()
        self.assertEqual("127.0.0.1", parsed.hostname)
        self.assertEqual(self.document["Id"], ownership["containerId"])

    def test_rejects_non_loopback_credentialed_https_wrong_path_and_wrong_port(self):
        cases = [
            "http://example.test:18082/sensorhub/api",
            "http://user:secret@127.0.0.1:18082/sensorhub/api",
            "https://127.0.0.1:18082/sensorhub/api",
            "http://127.0.0.1:18082/different/api",
            "http://127.0.0.1:18083/sensorhub/api",
        ]
        for url in cases:
            with self.subTest(url=url):
                with self.assertRaises(ValueError):
                    self.validate(url)

    def test_rejects_primary_or_unrelated_loopback_container(self):
        self.document["Name"] = "/field-hub-osh-1"
        self.document["Config"]["Labels"] = {"service": "primary"}
        self.write_evidence()
        with self.assertRaisesRegex(ValueError, "ownership labels"):
            self.validate()

    def test_rejects_wrong_published_port_state_install_and_network(self):
        mutations = [
            ("NetworkSettings", "Ports", {"8081/tcp": []}),
            ("NetworkSettings", "Networks", {"other": {}}),
            ("Mounts", 0, "Source", "/tmp/other-state"),
            ("Mounts", 1, "RW", True),
        ]
        for mutation in mutations:
            with self.subTest(mutation=mutation):
                self.document = container_document(
                    state_source=str(self.state), install_source=str(self.install)
                )
                target = self.document
                for key in mutation[:-2]:
                    target = target[key]
                target[mutation[-2]] = mutation[-1]
                self.write_evidence()
                with self.assertRaises(ValueError):
                    self.validate()


class FakeSeederClient:

    def __init__(self, responses):
        self.responses = list(responses)
        self.calls = []
        self.timeout = 30.0

    def request_diagnostic(self, method, path, media_type=None, payload=None, timeout=None):
        self.calls.append(
            {
                "method": method,
                "path": path,
                "mediaType": media_type,
                "payload": payload,
                "timeout": timeout,
            }
        )
        if not self.responses:
            raise AssertionError("unexpected diagnostic request")
        return self.responses.pop(0)


class SeederCommandProbeBehaviorTests(unittest.TestCase):

    def test_command_probe_records_successful_post_and_nested_command_item(self):
        command_probe = {
            "collection": "/controlstreams/{controlStreamId}/commands",
            "nestedCollection": "/controlstreams/{controlStreamId}/commands?limit=10",
            "method": "POST",
            "mediaType": "application/json",
            "timeout": 5.0,
            "payload": {
                "issueTime": "{timestamp}",
                "parameters": {
                    "setpoint": 22.0,
                },
            },
        }
        client = FakeSeederClient(
            [
                {
                    "status": 201,
                    "location": "http://127.0.0.1/commands/cmd-1",
                    "contentType": "application/json",
                    "body": {"id": "cmd-1"},
                },
                {
                    "status": 200,
                    "contentType": "application/json",
                    "body": {"items": [{"id": "cmd-1"}]},
                },
            ]
        )

        evidence = SEEDER.command_probe_evidence(
            client,
            command_probe,
            {"controlStreamId": "cs-1", "timestamp": "2026-08-02T04:00:00Z"},
        )

        self.assertTrue(evidence["attempted"])
        self.assertEqual(201, evidence["post"]["status"])
        self.assertEqual(1, evidence["nestedCollection"]["itemCount"])
        self.assertEqual("cmd-1", evidence["discoveredCommandId"])
        self.assertEqual(5.0, client.calls[0]["timeout"])
        self.assertEqual(
            {"issueTime": "2026-08-02T04:00:00Z", "parameters": {"setpoint": 22.0}},
            client.calls[0]["payload"],
        )

    def test_command_probe_records_timeout_and_empty_nested_collection_as_diagnostics(self):
        command_probe = {
            "collection": "/controlstreams/{controlStreamId}/commands",
            "method": "POST",
            "mediaType": "application/json",
            "timeout": 1.0,
            "payload": {
                "parameters": {
                    "setpoint": 22.0,
                },
            },
        }
        client = FakeSeederClient(
            [
                {
                    "status": None,
                    "location": None,
                    "contentType": "",
                    "body": None,
                    "errorType": "timeout",
                    "error": "timed out",
                },
                {
                    "status": 200,
                    "contentType": "application/json",
                    "body": {"items": []},
                },
            ]
        )

        evidence = SEEDER.command_probe_evidence(
            client,
            command_probe,
            {"controlStreamId": "cs-1", "timestamp": "2026-08-02T04:00:00Z"},
        )

        self.assertTrue(evidence["attempted"])
        self.assertEqual("timeout", evidence["post"]["errorType"])
        self.assertEqual(0, evidence["nestedCollection"]["itemCount"])
        self.assertIsNone(evidence["discoveredCommandId"])


if __name__ == "__main__":
    unittest.main(verbosity=2)
