#!/usr/bin/env python3
"""Run a reproducible populated local OSH E2E attempt with strict ownership."""

from __future__ import annotations

import datetime
import difflib
import hashlib
import json
import os
import pathlib
import re
import secrets
import shutil
import subprocess
import sys
import tempfile
import time
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET
import zipfile


RUN_LABEL = "org.opengeospatial.ets.csapi.run-id"
ROLE_LABEL = "org.opengeospatial.ets.csapi.role"
DEFAULT_OSH_IMAGE = (
    "maven:3.9-eclipse-temurin-17"
    "@sha256:1ed5d1f54416b706707b4f3238f63a20bb06aab27c6d240090a2bb9ad895ed45"
)
EXPECTED_OSH_REMOTE = "https://github.com/opensensorhub/osh-core.git"
TESTNG_PATTERN = re.compile(r"^Request:\s+([A-Za-z]+)\s+(\S+)\s*$")


class WorkflowError(RuntimeError):
    """A controlled workflow failure that must still run finalization."""


class CommandRunner:
    """Subprocess boundary, replaceable by behavioral tests."""

    def run(self, args, *, check=True, env=None):
        result = subprocess.run(
            [str(arg) for arg in args],
            check=False,
            capture_output=True,
            text=True,
            env=env,
        )
        if check and result.returncode != 0:
            detail = (result.stderr or result.stdout).strip()
            raise WorkflowError(f"{args[0]} exited {result.returncode}: {detail[:1000]}")
        return result

    def stream(self, args, output_file, *, env=None):
        with output_file.open("w", encoding="utf-8") as log_file:
            process = subprocess.Popen(
                [str(arg) for arg in args],
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                text=True,
                env=env,
            )
            assert process.stdout is not None
            for line in process.stdout:
                sys.stdout.write(line)
                sys.stdout.flush()
                log_file.write(line)
                log_file.flush()
            return process.wait()


def utc_now():
    return datetime.datetime.now(datetime.timezone.utc).replace(microsecond=0)


def timestamp():
    return utc_now().isoformat().replace("+00:00", "Z")


def sha256_bytes(value):
    return hashlib.sha256(value).hexdigest()


def sha256_file(path):
    digest = hashlib.sha256()
    with path.open("rb") as input_file:
        for block in iter(lambda: input_file.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def atomic_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_name(path.name + ".tmp")
    temporary.write_text(json.dumps(value, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    temporary.replace(path)


def normalize_remote(remote):
    return remote.rstrip("/")


def parse_manifest_build_number(jar_path):
    with zipfile.ZipFile(jar_path) as jar:
        manifest = jar.read("META-INF/MANIFEST.MF").decode("utf-8", errors="replace")
    for line in manifest.splitlines():
        if line.startswith("Bundle-BuildNumber:"):
            return line.split(":", 1)[1].strip()
    raise WorkflowError(f"{jar_path.name} has no Bundle-BuildNumber")


def installed_file_manifest(install):
    entries = []
    for path in sorted(item for item in install.rglob("*") if item.is_file()):
        entries.append(f"{sha256_file(path)}  {path.relative_to(install).as_posix()}")
    if not entries:
        raise WorkflowError("OSH installation manifest is empty")
    return entries


def state_file_manifest(state_source):
    entries = []
    for path in sorted(item for item in state_source.rglob("*") if item.is_file()):
        if path.name.endswith(".log") or path.name.endswith(".trace.db"):
            continue
        entries.append(f"{sha256_file(path)}  {path.relative_to(state_source).as_posix()}")
    return entries


def selected_mount(document, destination):
    matches = [mount for mount in document.get("Mounts", []) if mount.get("Destination") == destination]
    if len(matches) != 1:
        raise WorkflowError(f"expected one {destination} mount, found {len(matches)}")
    mount = matches[0]
    return {
        "type": mount.get("Type"),
        "source": str(pathlib.Path(mount.get("Source", "")).resolve()),
        "destination": destination,
        "rw": bool(mount.get("RW")),
    }


def normalized_primary_fingerprint(document):
    config = document.get("Config", {})
    state = document.get("State", {})
    networks = document.get("NetworkSettings", {}).get("Networks", {})
    state_mount = selected_mount(document, "/state")
    osh_mount = selected_mount(document, "/opt/osh")
    safe_config = {
        "image": config.get("Image"),
        "entrypoint": config.get("Entrypoint"),
        "cmd": config.get("Cmd"),
        "user": config.get("User"),
        "workingDir": config.get("WorkingDir"),
        "labels": config.get("Labels") or {},
    }
    return {
        "containerId": document.get("Id"),
        "name": document.get("Name"),
        "imageId": document.get("Image"),
        "configImage": config.get("Image"),
        "safeConfigHash": sha256_bytes(
            json.dumps(safe_config, sort_keys=True, separators=(",", ":")).encode("utf-8")
        ),
        "entrypoint": config.get("Entrypoint"),
        "command": config.get("Cmd"),
        "user": config.get("User"),
        "workingDir": config.get("WorkingDir"),
        "running": bool(state.get("Running")),
        "status": state.get("Status"),
        "networks": sorted(networks),
        "stateMount": state_mount,
        "oshMount": osh_mount,
    }


def parse_testng_report(report):
    try:
        root = ET.parse(report).getroot()
    except (ET.ParseError, OSError) as error:
        raise WorkflowError(f"invalid TestNG XML {report}: {error}") from error
    if root.tag != "testng-results":
        raise WorkflowError(f"{report} root is not testng-results")
    totals = {}
    for key in ("total", "passed", "failed", "skipped"):
        value = root.get(key)
        if value is None or not value.isdigit():
            raise WorkflowError(f"{report} has invalid {key} total")
        totals[key] = int(value)
    if totals["total"] <= 0:
        raise WorkflowError(f"{report} executed zero tests")
    if totals["total"] != totals["passed"] + totals["failed"] + totals["skipped"]:
        raise WorkflowError(f"{report} totals do not reconcile")
    failures = []
    for test_class in root.iter("class"):
        for test_method in test_class.iter("test-method"):
            if test_method.get("status") != "FAIL":
                continue
            exception = test_method.find("exception")
            message = ""
            if exception is not None:
                message_node = exception.find("message")
                if message_node is not None and message_node.text:
                    message = message_node.text.strip()
            failures.append(
                {
                    "class": test_class.get("name", ""),
                    "name": test_method.get("name", ""),
                    "description": test_method.get("description", ""),
                    "message": message,
                }
            )
    if len(failures) != totals["failed"]:
        raise WorkflowError(
            f"{report} declares {totals['failed']} failures but exposes {len(failures)} records"
        )
    return {
        "verdict": "PASS" if totals["failed"] == 0 else "FAIL",
        "totals": totals,
        "failures": failures,
    }


def startup_verdict(log_file):
    if not log_file or not log_file.is_file() or log_file.stat().st_size == 0:
        return "NOT_AVAILABLE"
    startup_lines = []
    startup_complete = False
    for line in log_file.read_text(encoding="utf-8", errors="replace").splitlines():
        startup_lines.append(line)
        if "Server startup in" in line:
            startup_complete = True
            break
    if not startup_complete:
        return "INCOMPLETE"
    ignored = (
        "did not find a matching property",
        "maxActive is not used in DBCP2",
        "encoding ['utf-8']",
    )
    severe = [
        line
        for line in startup_lines
        if re.match(r"^[0-9]{2}-[A-Za-z]{3}-[0-9]{4}.*(SEVERE|ERROR)", line)
        and not any(fragment in line for fragment in ignored)
    ]
    return "FAIL" if severe else "PASS"


def request_method_counts(log_file, iut_url):
    if not log_file or not log_file.is_file():
        return {}
    from urllib.parse import urlsplit

    expected = urlsplit(iut_url.rstrip("/"))
    base = iut_url.rstrip("/")
    counts = {}
    last_method = None
    for raw in log_file.read_text(encoding="utf-8", errors="replace").splitlines():
        line = raw.strip()
        match = TESTNG_PATTERN.match(line)
        if match:
            method, uri = match.groups()
            parsed = urlsplit(uri)
            if parsed.scheme == expected.scheme and parsed.netloc == expected.netloc:
                if uri == base or uri.startswith(base + "/") or uri.startswith(base + "?"):
                    counts[method.upper()] = counts.get(method.upper(), 0) + 1
            last_method = None
            continue
        if line.startswith("Request method:"):
            last_method = line.split(":", 1)[1].strip().upper()
            continue
        if line.startswith("Request URI:") and last_method:
            uri = line.split(":", 1)[1].strip()
            parsed = urlsplit(uri)
            if parsed.scheme == expected.scheme and parsed.netloc == expected.netloc:
                if uri == base or uri.startswith(base + "/") or uri.startswith(base + "?"):
                    counts[last_method] = counts.get(last_method, 0) + 1
            last_method = None
    return dict(sorted(counts.items()))


class PopulatedWorkflow:

    def __init__(self, runner=None, environment=None):
        self.runner = runner or CommandRunner()
        self.environment = dict(os.environ if environment is None else environment)
        self.repo_root = pathlib.Path(__file__).resolve().parents[1]
        self.source = pathlib.Path(
            self.environment.get("LOCAL_OSH_SOURCE", "/home/nh/docker/osh-core")
        ).resolve()
        self.install = pathlib.Path(
            self.environment.get("LOCAL_OSH_INSTALL", str(self.source / "build/install/osh-core"))
        ).resolve()
        self.upstream_ref = self.environment.get("LOCAL_OSH_UPSTREAM_REF", "origin/master")
        self.expected_remote = self.environment.get(
            "LOCAL_OSH_EXPECTED_REMOTE_URL", EXPECTED_OSH_REMOTE
        )
        self.config = pathlib.Path(
            self.environment.get(
                "LOCAL_OSH_CONFIG", str(self.repo_root / "ops/local-osh-gate-config.json")
            )
        ).resolve()
        self.fixtures = pathlib.Path(
            self.environment.get(
                "LOCAL_OSH_FIXTURES",
                str(self.repo_root / "ops/local-osh-populated-fixtures.json"),
            )
        ).resolve()
        self.network = self.environment.get("LOCAL_OSH_NETWORK", "field-hub_default")
        self.primary_name = self.environment.get("PRIMARY_OSH_CONTAINER", "field-hub-osh-1")
        self.clean_primary_url = self.environment.get(
            "CLEAN_PRIMARY_IUT_URL", "http://field-hub-osh-1:8081/sensorhub/api"
        )
        self.osh_image = self.environment.get("LOCAL_OSH_IMAGE", DEFAULT_OSH_IMAGE)
        self.keep_state = self.environment.get("LOCAL_OSH_KEEP_STATE", "false") == "true"
        self.start_timeout = int(self.environment.get("LOCAL_OSH_START_TIMEOUT_S", "180"))
        self.docker = self.environment.get("LOCAL_OSH_DOCKER_BIN", "docker")
        self.smoke_script = pathlib.Path(
            self.environment.get(
                "LOCAL_OSH_SMOKE_SCRIPT", str(self.repo_root / "scripts/smoke-test.sh")
            )
        ).resolve()
        self.seeder_script = pathlib.Path(
            self.environment.get(
                "LOCAL_OSH_SEEDER_SCRIPT",
                str(self.repo_root / "scripts/local-osh-populated-fixture.py"),
            )
        ).resolve()
        output_value = self.environment.get(
            "SMOKE_OUTPUT_DIR", "/tmp/ets-ogcapi-connectedsystems10-populated-local-osh"
        )
        self.output = pathlib.Path(output_value).expanduser().resolve()
        self.run_id = self.environment.get(
            "LOCAL_OSH_RUN_ID",
            utc_now().strftime("%Y%m%dT%H%M%SZ") + "-" + secrets.token_hex(6),
        )
        if not re.fullmatch(r"[A-Za-z0-9_.:-]{8,80}", self.run_id):
            raise WorkflowError("LOCAL_OSH_RUN_ID has unsupported format")
        self.osh_name = f"ets-csapi-populated-osh-{self.run_id}"
        self.populated_te_name = f"ets-csapi-populated-te-{self.run_id}"
        self.clean_te_name = f"ets-csapi-clean-primary-te-{self.run_id}"
        self.runtime_state = None
        self.owned_container_id = None
        self.loopback_url = None
        self.docker_iut_url = None
        self.primary_before = None
        self.primary_after = None
        self.primary_state_before = None
        self.primary_state_after = None
        self.primary_unchanged = False
        self.primary_differences = []
        self.cleanup_verdict = "NOT_RUN"
        self.retained_state_path = None
        self.provisioning_status = None
        self.populated_smoke_status = None
        self.clean_smoke_status = None
        self.abort_phase = None
        self.abort_error = None
        self.finalization_errors = []
        self.workflow_started = False
        self.phase = "preflight"
        self.inject_failure = self.environment.get("LOCAL_OSH_INJECT_FAILURE_PHASE", "")
        self.source_provenance = {}
        self.image_provenance = {}

    def log(self, message):
        print(
            f"[local-osh-populated-e2e {datetime.datetime.now().strftime('%H:%M:%S')}] {message}",
            flush=True,
        )

    def command(self, args, *, check=True, env=None):
        return self.runner.run(args, check=check, env=env)

    def docker_inspect(self, target, *, allow_missing=False):
        result = self.command([self.docker, "inspect", target], check=False)
        if result.returncode != 0:
            if allow_missing:
                return None
            raise WorkflowError(f"docker inspect failed for {target}: {result.stderr.strip()}")
        try:
            documents = json.loads(result.stdout)
        except json.JSONDecodeError as error:
            raise WorkflowError(f"docker inspect returned invalid JSON for {target}") from error
        if len(documents) != 1:
            raise WorkflowError(f"docker inspect returned {len(documents)} objects for {target}")
        return documents[0]

    def require_name_available(self, name):
        if name == self.primary_name:
            raise WorkflowError(f"generated container name collides with primary: {name}")
        if self.docker_inspect(name, allow_missing=True) is not None:
            raise WorkflowError(f"refusing to remove or reuse unowned container name: {name}")

    def preflight(self):
        if self.environment.get("SMOKE_MUTATION_TESTS_ENABLED") != "true":
            raise WorkflowError("SMOKE_MUTATION_TESTS_ENABLED must equal true")
        if self.environment.get("SMOKE_MUTATION_IUT_POLICY") != "dedicated-mutable-iut":
            raise WorkflowError(
                "SMOKE_MUTATION_IUT_POLICY must equal dedicated-mutable-iut"
            )
        if self.output == self.repo_root or self.repo_root in self.output.parents:
            raise WorkflowError("SMOKE_OUTPUT_DIR must be outside the repository")
        if self.output.exists() and any(self.output.iterdir()):
            raise WorkflowError("SMOKE_OUTPUT_DIR must be absent or empty")
        if not (self.source / ".git").is_dir():
            raise WorkflowError("LOCAL_OSH_SOURCE is not a Git checkout")
        if not (self.install / "lib").is_dir():
            raise WorkflowError("LOCAL_OSH_INSTALL has no lib directory")
        for required in (self.config, self.fixtures, self.smoke_script, self.seeder_script):
            if not required.is_file():
                raise WorkflowError(f"required file is missing: {required}")
        for command_name in (self.docker, "git", "python3", "bash"):
            if shutil.which(command_name) is None:
                raise WorkflowError(f"required command is unavailable: {command_name}")
        if len({self.primary_name, self.osh_name, self.populated_te_name, self.clean_te_name}) != 4:
            raise WorkflowError("primary and per-run container names must be distinct")
        self.command([self.docker, "network", "inspect", self.network])
        self.docker_inspect(self.primary_name)
        for name in (self.osh_name, self.populated_te_name, self.clean_te_name):
            self.require_name_available(name)
        self.output.mkdir(parents=True, exist_ok=True)
        (self.output / "populated-results").mkdir()
        (self.output / "clean-primary-results").mkdir()
        self.verify_source_and_install()
        self.record_source_files()

    def verify_source_and_install(self):
        status = self.command(["git", "-C", self.source, "status", "--porcelain"]).stdout
        if status.strip():
            raise WorkflowError("external OSH checkout is dirty")
        remote = self.command(
            ["git", "-C", self.source, "remote", "get-url", "origin"]
        ).stdout.strip()
        if normalize_remote(remote) != normalize_remote(self.expected_remote):
            raise WorkflowError(f"unexpected OSH origin URL: {remote}")
        head = self.command(["git", "-C", self.source, "rev-parse", "HEAD"]).stdout.strip()
        upstream = self.command(
            ["git", "-C", self.source, "rev-parse", self.upstream_ref]
        ).stdout.strip()
        ancestor = self.command(
            ["git", "-C", self.source, "merge-base", "--is-ancestor", head, upstream],
            check=False,
        )
        if ancestor.returncode != 0:
            raise WorkflowError("OSH HEAD is not an ancestor of the reviewed upstream")
        counts = self.command(
            [
                "git",
                "-C",
                self.source,
                "rev-list",
                "--left-right",
                "--count",
                f"{self.upstream_ref}...HEAD",
            ]
        ).stdout.split()
        if len(counts) != 2 or counts[1] != "0":
            raise WorkflowError("external OSH checkout has local commits ahead of upstream")
        consys_jars = sorted((self.install / "lib").glob("sensorhub-service-consys-*.jar"))
        if len(consys_jars) != 1:
            raise WorkflowError("expected exactly one installed ConSys service jar")
        build_number = parse_manifest_build_number(consys_jars[0])
        if not build_number or not head.startswith(build_number):
            raise WorkflowError(
                f"installed ConSys build {build_number!r} does not identify OSH HEAD {head}"
            )
        image_document = self.command(
            [self.docker, "image", "inspect", self.osh_image]
        ).stdout
        try:
            image = json.loads(image_document)[0]
        except (json.JSONDecodeError, IndexError) as error:
            raise WorkflowError("could not inspect pinned local OSH container image") from error
        expected_digest = self.osh_image.split("@", 1)[1] if "@" in self.osh_image else None
        repo_digests = image.get("RepoDigests") or []
        if expected_digest and not any(item.endswith("@" + expected_digest) for item in repo_digests):
            raise WorkflowError("local OSH image inspection did not confirm the pinned digest")
        install_manifest = installed_file_manifest(self.install)
        (self.output / "installed-files.sha256").write_text(
            "\n".join(install_manifest) + "\n", encoding="utf-8"
        )
        self.source_provenance = {
            "origin": remote,
            "headCommit": head,
            "upstreamRef": self.upstream_ref,
            "upstreamCommit": upstream,
            "headIsUpstreamAncestor": True,
            "behind": int(counts[0]),
            "ahead": int(counts[1]),
            "clean": True,
            "conSysJar": str(consys_jars[0]),
            "conSysBundleBuildNumber": build_number,
            "installManifest": "installed-files.sha256",
        }
        self.image_provenance = {
            "reference": self.osh_image,
            "imageId": image.get("Id"),
            "repoDigests": repo_digests,
        }
        atomic_json(self.output / "osh-source-provenance.json", self.source_provenance)
        atomic_json(self.output / "osh-image-provenance.json", self.image_provenance)
        (self.output / "local-osh-config.sha256").write_text(
            f"{sha256_file(self.config)}  {self.config}\n", encoding="utf-8"
        )
        (self.output / "local-osh-fixtures.sha256").write_text(
            f"{sha256_file(self.fixtures)}  {self.fixtures}\n", encoding="utf-8"
        )

    def record_source_files(self):
        paths = [
            self.repo_root / "scripts/local-osh-populated-e2e.sh",
            pathlib.Path(__file__).resolve(),
            self.seeder_script,
            self.smoke_script,
            self.repo_root / "scripts/test-local-osh-populated-workflow.sh",
            self.repo_root / "scripts/test_local_osh_populated_workflow.py",
            self.fixtures,
            self.repo_root
            / "src/test/java/org/opengis/cite/ogcapiconnectedsystems10/VerifyLocalOshPopulatedE2e.java",
        ]
        lines = [f"{sha256_file(path)}  {path.relative_to(self.repo_root)}" for path in paths]
        (self.output / "source-files.sha256").write_text(
            "\n".join(lines) + "\n", encoding="utf-8"
        )

    def capture_primary(self, label):
        document = self.docker_inspect(self.primary_name)
        fingerprint = normalized_primary_fingerprint(document)
        if not fingerprint["running"] or fingerprint["status"] != "running":
            raise WorkflowError("primary OSH container is not running")
        if fingerprint["oshMount"]["rw"]:
            raise WorkflowError("primary OSH /opt/osh mount is writable")
        if fingerprint["oshMount"]["source"] != str(self.install):
            raise WorkflowError("primary OSH /opt/osh mount is not the reviewed installation")
        state_source = pathlib.Path(fingerprint["stateMount"]["source"])
        if not state_source.is_dir():
            raise WorkflowError("primary OSH state source is unavailable")
        state_manifest = state_file_manifest(state_source)
        atomic_json(self.output / f"primary-state-{label}.fingerprint.json", fingerprint)
        atomic_json(self.output / f"primary-state-{label}.container.json", document)
        (self.output / f"primary-state-{label}.sha256").write_text(
            "\n".join(state_manifest) + ("\n" if state_manifest else ""),
            encoding="utf-8",
        )
        (self.output / f"primary-state-{label}.mount.txt").write_text(
            str(state_source) + "\n", encoding="utf-8"
        )
        return fingerprint, state_manifest

    def create_runtime(self):
        self.runtime_state = pathlib.Path(
            tempfile.mkdtemp(prefix="ets-csapi-populated-osh-state.", dir="/tmp")
        ).resolve()
        runtime_config = json.loads(self.config.read_text(encoding="utf-8"))
        for module in runtime_config:
            if module.get("id") == "s42-http-server":
                module["proxyBaseUrl"] = f"http://{self.osh_name}:8081"
        atomic_json(self.output / "local-osh-runtime-config.json", runtime_config)
        atomic_json(self.runtime_state / "config.json", runtime_config)

    def verify_owned_container(self, document):
        labels = document.get("Config", {}).get("Labels") or {}
        if self.owned_container_id and document.get("Id") != self.owned_container_id:
            raise WorkflowError("created OSH container ID changed")
        if document.get("Name") != "/" + self.osh_name:
            raise WorkflowError("created OSH container name changed")
        if labels.get(RUN_LABEL) != self.run_id or labels.get(ROLE_LABEL) != "populated-osh":
            raise WorkflowError("created OSH container ownership labels are invalid")
        if not document.get("State", {}).get("Running"):
            raise WorkflowError("created OSH container is not running")
        state_mount = selected_mount(document, "/state")
        osh_mount = selected_mount(document, "/opt/osh")
        if state_mount["source"] != str(self.runtime_state) or not state_mount["rw"]:
            raise WorkflowError("created OSH state mount is not owned isolated state")
        if osh_mount["source"] != str(self.install) or osh_mount["rw"]:
            raise WorkflowError("created OSH install mount is not reviewed read-only install")

    def start_osh(self):
        self.require_name_available(self.osh_name)
        result = self.command(
            [
                self.docker,
                "run",
                "-d",
                "--name",
                self.osh_name,
                "--label",
                f"{RUN_LABEL}={self.run_id}",
                "--label",
                f"{ROLE_LABEL}=populated-osh",
                "--network",
                self.network,
                "-p",
                "127.0.0.1::8081",
                "--user",
                f"{os.getuid()}:{os.getgid()}",
                "-e",
                "HOME=/state",
                "-v",
                f"{self.install}:/opt/osh:ro",
                "-v",
                f"{self.runtime_state}:/state",
                "-w",
                "/state",
                self.osh_image,
                "java",
                "-Xmx512m",
                "-Dlogback.configurationFile=/opt/osh/logback.xml",
                "-cp",
                "/opt/osh/lib/*",
                "org.sensorhub.impl.SensorHub",
                "/state/config.json",
            ]
        )
        container_id = result.stdout.strip()
        if not re.fullmatch(r"[0-9a-f]{12,64}", container_id):
            raise WorkflowError("docker run did not return a container ID")
        self.owned_container_id = container_id
        document = self.docker_inspect(container_id)
        self.verify_owned_container(document)
        atomic_json(self.output / "local-osh-container.json", document)
        ports = document.get("NetworkSettings", {}).get("Ports", {}).get("8081/tcp") or []
        loopback_ports = [
            item
            for item in ports
            if item.get("HostIp") == "127.0.0.1" and str(item.get("HostPort", "")).isdigit()
        ]
        if len(loopback_ports) != 1:
            raise WorkflowError("isolated OSH has no unique loopback-only published port")
        host_port = int(loopback_ports[0]["HostPort"])
        self.loopback_url = f"http://127.0.0.1:{host_port}/sensorhub/api"
        self.docker_iut_url = f"http://{self.osh_name}:8081/sensorhub/api"
        ownership = {
            "schemaVersion": 1,
            "runId": self.run_id,
            "containerId": container_id,
            "containerName": self.osh_name,
            "runLabel": RUN_LABEL,
            "roleLabel": ROLE_LABEL,
            "role": "populated-osh",
            "host": "127.0.0.1",
            "hostPort": host_port,
            "containerPort": 8081,
            "apiPath": "/sensorhub/api",
            "stateSource": str(self.runtime_state),
            "installSource": str(self.install),
            "network": self.network,
        }
        ownership_path = self.output / "ownership-evidence.json"
        atomic_json(ownership_path, ownership)
        ownership_path.chmod(0o600)

    def wait_for_osh(self):
        deadline = time.monotonic() + self.start_timeout
        last_error = "not attempted"
        while time.monotonic() < deadline:
            try:
                with urllib.request.urlopen(self.loopback_url, timeout=5) as response:
                    if response.status == 200:
                        return
                    last_error = f"HTTP {response.status}"
            except (urllib.error.URLError, TimeoutError, ConnectionError) as error:
                last_error = str(error)
            time.sleep(2)
        raise WorkflowError(f"isolated OSH did not become ready: {last_error}")

    def provision(self):
        environment = dict(self.environment)
        environment["LOCAL_OSH_DOCKER_BIN"] = self.docker
        log_path = self.output / "provisioning.stdout.log"
        self.provisioning_status = self.runner.stream(
            [
                "python3",
                self.seeder_script,
                "--iut-url",
                self.loopback_url,
                "--fixtures",
                self.fixtures,
                "--ownership-evidence",
                self.output / "ownership-evidence.json",
                "--output",
                self.output / "provisioning-evidence.json",
            ],
            log_path,
            env=environment,
        )
        if self.provisioning_status != 0:
            raise WorkflowError(f"fixture provisioning exited {self.provisioning_status}")
        evidence = json.loads(
            (self.output / "provisioning-evidence.json").read_text(encoding="utf-8")
        )
        if evidence.get("provisioningReady") is not True:
            raise WorkflowError("fixture provisioning did not report readiness")

    def run_smoke(self, target_url, results_dir, container_name, stdout_name, mutation):
        self.require_name_available(container_name)
        environment = dict(self.environment)
        environment.update(
            {
                "SMOKE_TARGET": "custom",
                "SMOKE_DOCKER_NETWORK": self.network,
                "SMOKE_IUT_URL": target_url,
                "SMOKE_CONTAINER_NAME": container_name,
                "SMOKE_OUTPUT_DIR": str(results_dir),
                "SMOKE_RUN_LABEL": self.run_id,
            }
        )
        if not mutation:
            environment.pop("SMOKE_MUTATION_TESTS_ENABLED", None)
            environment.pop("SMOKE_MUTATION_IUT_POLICY", None)
        return self.runner.stream(
            ["bash", self.smoke_script],
            self.output / stdout_name,
            env=environment,
        )

    def maybe_inject(self, phase):
        if self.inject_failure == phase:
            raise WorkflowError(f"injected {phase} failure")

    def run_populated(self):
        self.phase = "runtime-configuration"
        self.create_runtime()
        self.phase = "startup"
        self.start_osh()
        self.maybe_inject("startup")
        self.wait_for_osh()
        startup_log = self.command(
            [self.docker, "logs", self.owned_container_id], check=False
        )
        (self.output / "local-osh-startup.log").write_text(
            startup_log.stdout + startup_log.stderr, encoding="utf-8"
        )
        self.phase = "provisioning"
        self.maybe_inject("provisioning")
        self.provision()
        self.phase = "populated-smoke"
        self.maybe_inject("populated-smoke")
        self.populated_smoke_status = self.run_smoke(
            self.docker_iut_url,
            self.output / "populated-results",
            self.populated_te_name,
            "populated-smoke.stdout.log",
            True,
        )
        self.phase = "evidence-parsing"
        self.maybe_inject("evidence-parsing")
        self.phase = "populated-complete"

    def cleanup_owned_resources(self):
        errors = []
        if not self.owned_container_id:
            candidate = self.docker_inspect(self.osh_name, allow_missing=True)
            if candidate is not None:
                labels = candidate.get("Config", {}).get("Labels") or {}
                if (
                    candidate.get("Name") == "/" + self.osh_name
                    and labels.get(RUN_LABEL) == self.run_id
                    and labels.get(ROLE_LABEL) == "populated-osh"
                ):
                    self.owned_container_id = candidate.get("Id")
                else:
                    errors.append("refusing to remove same-named container without ownership")
        if self.owned_container_id:
            document = self.docker_inspect(self.owned_container_id, allow_missing=True)
            if document is not None:
                try:
                    self.verify_owned_container(document)
                    complete_log = self.command(
                        [self.docker, "logs", self.owned_container_id], check=False
                    )
                    (self.output / "local-osh-complete.log").write_text(
                        complete_log.stdout + complete_log.stderr, encoding="utf-8"
                    )
                    removal = self.command(
                        [self.docker, "rm", "-f", self.owned_container_id], check=False
                    )
                    if removal.returncode != 0:
                        errors.append(
                            f"owned container removal exited {removal.returncode}: "
                            f"{removal.stderr.strip()}"
                        )
                except WorkflowError as error:
                    errors.append(str(error))
            if self.docker_inspect(self.owned_container_id, allow_missing=True) is not None:
                errors.append("owned populated OSH container remains after cleanup")
        if self.runtime_state and self.runtime_state.exists():
            if self.keep_state:
                self.retained_state_path = str(self.runtime_state)
            else:
                try:
                    shutil.rmtree(self.runtime_state)
                except OSError:
                    helper = self.command(
                        [
                            self.docker,
                            "run",
                            "--rm",
                            "--label",
                            f"{RUN_LABEL}={self.run_id}",
                            "--label",
                            f"{ROLE_LABEL}=state-cleanup",
                            "-v",
                            f"{self.runtime_state}:/state",
                            self.osh_image,
                            "find",
                            "/state",
                            "-mindepth",
                            "1",
                            "-depth",
                            "-delete",
                        ],
                        check=False,
                    )
                    if helper.returncode == 0:
                        try:
                            self.runtime_state.rmdir()
                        except OSError:
                            pass
                    else:
                        errors.append(
                            f"state cleanup helper exited {helper.returncode}: "
                            f"{helper.stderr.strip()}"
                        )
                if self.runtime_state.exists():
                    errors.append(f"ephemeral state remains: {self.runtime_state}")
        if self.inject_failure == "cleanup":
            errors.append("injected cleanup failure")
        if errors:
            self.cleanup_verdict = "FAIL"
            raise WorkflowError("; ".join(errors))
        self.cleanup_verdict = "RETAINED" if self.retained_state_path else "PASS"

    def compare_primary(self):
        self.primary_after, self.primary_state_after = self.capture_primary("after")
        before_json = json.dumps(self.primary_before, indent=2, sort_keys=True).splitlines()
        after_json = json.dumps(self.primary_after, indent=2, sort_keys=True).splitlines()
        self.primary_differences = list(
            difflib.unified_diff(before_json, after_json, fromfile="before", tofile="after")
        )
        if self.primary_state_before != self.primary_state_after:
            self.primary_differences.append("primary state file hashes changed")
        self.primary_unchanged = not self.primary_differences
        (self.output / "primary-state-diff.txt").write_text(
            "\n".join(self.primary_differences) + ("\n" if self.primary_differences else ""),
            encoding="utf-8",
        )

    def run_clean_primary(self):
        self.clean_smoke_status = self.run_smoke(
            self.clean_primary_url,
            self.output / "clean-primary-results",
            self.clean_te_name,
            "clean-primary-smoke.stdout.log",
            False,
        )

    def find_single(self, directory, suffix):
        matches = sorted(directory.glob(suffix))
        if len(matches) != 1:
            raise WorkflowError(
                f"expected one {suffix} artifact under {directory}, found {len(matches)}"
            )
        return matches[0]

    def analyze_smoke(self, results_dir, target_url, prefix):
        report = self.find_single(results_dir, "*.xml")
        log_file = self.find_single(results_dir, "*.log")
        evidence = parse_testng_report(report)
        evidence.update(
            {
                "report": str(report.relative_to(self.output)),
                "log": str(log_file.relative_to(self.output)),
                "startupVerdict": startup_verdict(log_file),
                "requestMethodCounts": request_method_counts(log_file, target_url),
            }
        )
        atomic_json(self.output / f"{prefix}-failures.json", evidence["failures"])
        return evidence

    def workflow_gate_verdict(self, smoke_status, testng, clean=False):
        if not testng:
            return "ERROR"
        if testng["startupVerdict"] != "PASS":
            return "ERROR"
        if testng["verdict"] == "PASS":
            return "PASS" if smoke_status == 0 else "ERROR"
        if not clean and testng["verdict"] == "FAIL" and smoke_status == 1:
            return "COMPLETE_WITH_CONFORMANCE_FAILURE"
        return "ERROR"

    def write_artifact_manifest(self):
        lines = []
        for path in sorted(item for item in self.output.rglob("*") if item.is_file()):
            if path.name == "artifact-manifest.sha256":
                continue
            lines.append(f"{sha256_file(path)}  {path.relative_to(self.output).as_posix()}")
        (self.output / "artifact-manifest.sha256").write_text(
            "\n".join(lines) + "\n", encoding="utf-8"
        )

    def build_summary(self):
        provisioning = None
        provisioning_path = self.output / "provisioning-evidence.json"
        if provisioning_path.is_file():
            try:
                provisioning = json.loads(provisioning_path.read_text(encoding="utf-8"))
            except json.JSONDecodeError as error:
                self.finalization_errors.append(f"invalid provisioning evidence: {error}")
        provisioning_verdict = (
            "PASS" if provisioning and provisioning.get("provisioningReady") is True else "FAIL"
        )
        populated = None
        clean = None
        try:
            populated = self.analyze_smoke(
                self.output / "populated-results", self.docker_iut_url or "", "populated"
            )
        except WorkflowError as error:
            self.finalization_errors.append(f"populated evidence: {error}")
        try:
            clean = self.analyze_smoke(
                self.output / "clean-primary-results", self.clean_primary_url, "clean-primary"
            )
        except WorkflowError as error:
            self.finalization_errors.append(f"clean-primary evidence: {error}")
        populated_gate = self.workflow_gate_verdict(
            self.populated_smoke_status, populated, clean=False
        )
        clean_gate = self.workflow_gate_verdict(self.clean_smoke_status, clean, clean=True)
        conformance = populated["verdict"] if populated else "NOT_RUN"
        clean_conformance = clean["verdict"] if clean else "NOT_RUN"
        overall_pass = (
            not self.abort_error
            and provisioning_verdict == "PASS"
            and conformance == "PASS"
            and populated_gate == "PASS"
            and self.cleanup_verdict in {"PASS", "RETAINED"}
            and self.primary_unchanged
            and clean_conformance == "PASS"
            and clean_gate == "PASS"
            and not self.finalization_errors
        )
        summary = {
            "schemaVersion": 2,
            "generatedAt": timestamp(),
            "runId": self.run_id,
            "abortPhase": self.abort_phase,
            "abortError": self.abort_error,
            "provisioningVerdict": provisioning_verdict,
            "provisioningStatus": self.provisioning_status,
            "provisioningEvidence": "provisioning-evidence.json",
            "conformanceVerdict": conformance,
            "populatedGateVerdict": populated_gate,
            "populatedSmokeStatus": self.populated_smoke_status,
            "populated": populated,
            "cleanupVerdict": self.cleanup_verdict,
            "retainedStatePath": self.retained_state_path,
            "primaryStateUnchanged": self.primary_unchanged,
            "primaryDifferences": self.primary_differences,
            "cleanPrimaryConformanceVerdict": clean_conformance,
            "cleanPrimaryGateVerdict": clean_gate,
            "cleanPrimaryStatus": self.clean_smoke_status,
            "cleanPrimary": clean,
            "oshSourceProvenance": self.source_provenance,
            "oshImageProvenance": self.image_provenance,
            "teamEngineImageId": self.extract_teamengine_image_id(),
            "sourceFilesManifest": "source-files.sha256",
            "artifactManifest": "artifact-manifest.sha256",
            "finalizationErrors": self.finalization_errors,
            "overallWorkflowVerdict": "PASS" if overall_pass else "FAIL",
        }
        atomic_json(self.output / "run-summary.json", summary)
        self.write_artifact_manifest()
        return 0 if overall_pass else 1

    def extract_teamengine_image_id(self):
        path = self.output / "populated-smoke.stdout.log"
        if not path.is_file():
            return None
        matches = re.findall(
            r"FINAL_IMAGE_ID=(sha256:[0-9a-f]{64})",
            path.read_text(encoding="utf-8", errors="replace"),
        )
        return matches[-1] if matches else None

    def finalize(self):
        for label, operation in (
            ("cleanup", self.cleanup_owned_resources),
            ("primary isolation", self.compare_primary),
            ("clean-primary smoke", self.run_clean_primary),
        ):
            try:
                operation()
            except Exception as error:  # Finalization must continue through every gate.
                self.finalization_errors.append(f"{label}: {error}")
                if label == "cleanup":
                    self.cleanup_verdict = "FAIL"
                if label == "primary isolation":
                    self.primary_unchanged = False
        try:
            return self.build_summary()
        except Exception as error:
            self.log(f"summary generation failed: {error}")
            return 2

    def execute(self):
        try:
            self.preflight()
            self.primary_before, self.primary_state_before = self.capture_primary("before")
            self.workflow_started = True
            self.log(f"starting owned isolated OSH run {self.run_id}")
            self.run_populated()
        except Exception as error:
            self.abort_phase = self.phase
            self.abort_error = str(error)
            self.log(f"populated attempt aborted in {self.abort_phase}: {self.abort_error}")
        if not self.workflow_started:
            return 2
        final_status = self.finalize()
        self.log(f"summary: {self.output / 'run-summary.json'}")
        return final_status


def main():
    try:
        workflow = PopulatedWorkflow()
        return workflow.execute()
    except Exception as error:
        print(f"[local-osh-populated-e2e FATAL] {error}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    sys.exit(main())
