#!/usr/bin/env python3
"""Provision exact populated-IUT fixtures through the local OSH HTTP API."""

import argparse
import copy
import datetime
import ipaddress
import json
import os
import pathlib
import shutil
import stat
import subprocess
import sys
import urllib.error
import urllib.parse
import urllib.request


REQUIRED_MUTATION_ENABLED = "true"
REQUIRED_MUTATION_POLICY = "dedicated-mutable-iut"
ALLOWED_LOCAL_NAMES = {"127.0.0.1", "localhost", "::1"}
RUN_LABEL = "org.opengeospatial.ets.csapi.run-id"
ROLE_LABEL = "org.opengeospatial.ets.csapi.role"


def assert_local_target(iut_url):
    """Reject every mutation target except an explicit loopback HTTP endpoint."""
    parsed = urllib.parse.urlsplit(iut_url)
    hostname = (parsed.hostname or "").lower()
    if parsed.scheme != "http" or parsed.username or parsed.password:
        raise ValueError("IUT must be an unauthenticated loopback HTTP URL")
    try:
        is_loopback = ipaddress.ip_address(hostname).is_loopback
    except ValueError:
        is_loopback = hostname in ALLOWED_LOCAL_NAMES
    if not is_loopback:
        raise ValueError("IUT host must be 127.0.0.1, localhost, or another loopback address")
    if not parsed.port:
        raise ValueError("IUT URL must include an explicit loopback port")
    if parsed.query or parsed.fragment:
        raise ValueError("IUT URL must not include a query or fragment")
    return parsed


def selected_mount(document, destination):
    matches = [
        mount
        for mount in document.get("Mounts", [])
        if mount.get("Destination") == destination
    ]
    if len(matches) != 1:
        raise ValueError(f"owned container must have exactly one {destination} mount")
    return matches[0]


def validate_owned_target(iut_url, evidence_path):
    parsed = assert_local_target(iut_url)
    mode = stat.S_IMODE(evidence_path.stat().st_mode)
    if mode & 0o077:
        raise ValueError("ownership evidence must not be group/world accessible")
    ownership = json.loads(evidence_path.read_text(encoding="utf-8"))
    required = {
        "runId",
        "containerId",
        "containerName",
        "host",
        "hostPort",
        "containerPort",
        "apiPath",
        "stateSource",
        "installSource",
        "network",
    }
    if not required.issubset(ownership):
        raise ValueError("ownership evidence is incomplete")
    if (
        parsed.hostname != ownership["host"]
        or parsed.port != ownership["hostPort"]
        or parsed.path.rstrip("/") != ownership["apiPath"].rstrip("/")
    ):
        raise ValueError("IUT URL does not match owned container host, port, and API path")
    if ownership["host"] != "127.0.0.1" or ownership["containerPort"] != 8081:
        raise ValueError("ownership evidence does not identify the loopback OSH endpoint")

    docker_bin = os.environ.get("LOCAL_OSH_DOCKER_BIN", "docker")
    if shutil.which(docker_bin) is None:
        raise ValueError("Docker command required for ownership verification is unavailable")
    result = subprocess.run(
        [docker_bin, "inspect", ownership["containerId"]],
        check=False,
        capture_output=True,
        text=True,
    )
    if result.returncode != 0:
        raise ValueError("owned container is unavailable")
    try:
        documents = json.loads(result.stdout)
    except json.JSONDecodeError as error:
        raise ValueError("Docker ownership inspection was not JSON") from error
    if len(documents) != 1:
        raise ValueError("Docker ownership inspection was ambiguous")
    document = documents[0]
    labels = document.get("Config", {}).get("Labels") or {}
    if (
        document.get("Id") != ownership["containerId"]
        or document.get("Name") != "/" + ownership["containerName"]
        or not document.get("State", {}).get("Running")
        or labels.get(RUN_LABEL) != ownership["runId"]
        or labels.get(ROLE_LABEL) != "populated-osh"
    ):
        raise ValueError("Docker container identity or ownership labels do not match")
    ports = document.get("NetworkSettings", {}).get("Ports", {}).get("8081/tcp") or []
    expected_port = str(ownership["hostPort"])
    matching_ports = [
        item
        for item in ports
        if item.get("HostIp") == "127.0.0.1" and item.get("HostPort") == expected_port
    ]
    if len(matching_ports) != 1:
        raise ValueError("Docker published port does not match ownership evidence")
    networks = document.get("NetworkSettings", {}).get("Networks", {})
    if ownership["network"] not in networks:
        raise ValueError("owned container is not attached to the expected network")
    state_mount = selected_mount(document, "/state")
    install_mount = selected_mount(document, "/opt/osh")
    if (
        pathlib.Path(state_mount.get("Source", "")).resolve()
        != pathlib.Path(ownership["stateSource"]).resolve()
        or not state_mount.get("RW")
    ):
        raise ValueError("owned container state mount is not isolated writable state")
    if (
        pathlib.Path(install_mount.get("Source", "")).resolve()
        != pathlib.Path(ownership["installSource"]).resolve()
        or install_mount.get("RW")
    ):
        raise ValueError("owned container install mount is not the reviewed read-only install")
    return parsed, ownership


def assert_mutation_gate():
    if os.environ.get("SMOKE_MUTATION_TESTS_ENABLED") != REQUIRED_MUTATION_ENABLED:
        raise ValueError("SMOKE_MUTATION_TESTS_ENABLED must equal true")
    if os.environ.get("SMOKE_MUTATION_IUT_POLICY") != REQUIRED_MUTATION_POLICY:
        raise ValueError(
            "SMOKE_MUTATION_IUT_POLICY must equal dedicated-mutable-iut"
        )


def substitute(value, variables):
    if isinstance(value, str):
        return value.format(**variables)
    if isinstance(value, list):
        return [substitute(item, variables) for item in value]
    if isinstance(value, dict):
        return {key: substitute(item, variables) for key, item in value.items()}
    return value


class ApiClient:

    def __init__(self, base_url, timeout):
        self.base_url = base_url.rstrip("/")
        self.timeout = timeout
        self.method_counts = {}

    def request(self, method, path, media_type=None, payload=None):
        self.method_counts[method] = self.method_counts.get(method, 0) + 1
        headers = {"Accept": "application/json, application/geo+json, application/om+json"}
        data = None
        if payload is not None:
            data = json.dumps(payload, separators=(",", ":")).encode("utf-8")
            headers["Content-Type"] = media_type
        request = urllib.request.Request(
            self.base_url + path, data=data, headers=headers, method=method
        )
        try:
            with urllib.request.urlopen(request, timeout=self.timeout) as response:
                body = response.read().decode("utf-8")
                return {
                    "status": response.status,
                    "location": response.headers.get("Location"),
                    "contentType": response.headers.get("Content-Type", ""),
                    "body": parse_json(body),
                }
        except urllib.error.HTTPError as error:
            body = error.read().decode("utf-8", errors="replace")
            raise RuntimeError(
                f"{method} {path} returned HTTP {error.code}: {body[:500]}"
            ) from error
        except urllib.error.URLError as error:
            raise RuntimeError(f"{method} {path} failed: {error.reason}") from error


def parse_json(body):
    if not body.strip():
        return None
    try:
        return json.loads(body)
    except json.JSONDecodeError as error:
        raise RuntimeError(f"response was not JSON: {body[:500]}") from error


def resource_id(response, resource_name):
    if response["status"] != 201:
        raise RuntimeError(
            f"{resource_name} create returned HTTP {response['status']}, expected 201"
        )
    location = response["location"]
    if not location:
        raise RuntimeError(f"{resource_name} create omitted Location")
    identifier = urllib.parse.urlsplit(location).path.rstrip("/").split("/")[-1]
    if not identifier:
        raise RuntimeError(f"{resource_name} Location had no resource id")
    return identifier


def require_get_json(client, path, description):
    response = client.request("GET", path)
    if response["status"] != 200 or response["body"] is None:
        raise RuntimeError(f"{description} was not readable JSON")
    return response


def validate_manifest(manifest):
    if manifest.get("schemaVersion") != 1:
        raise ValueError("fixture manifest schemaVersion must equal 1")
    static_fixtures = manifest.get("staticFixtures")
    dynamic_fixtures = manifest.get("dynamicFixtures")
    if not isinstance(static_fixtures, list) or len(static_fixtures) != 4:
        raise ValueError("fixture manifest must define four staticFixtures")
    expected_names = {"system", "procedure", "deployment", "samplingFeature"}
    if {fixture.get("name") for fixture in static_fixtures} != expected_names:
        raise ValueError("static fixture names are incomplete or duplicated")
    if not isinstance(dynamic_fixtures, dict) or set(dynamic_fixtures) != {
        "dataStream",
        "observation",
        "controlStream",
    }:
        raise ValueError("dynamic fixture definitions are incomplete")


def field_names(schema_body, member):
    schema = schema_body.get("schema", schema_body) if isinstance(schema_body, dict) else {}
    component = schema.get(member, {}) if isinstance(schema, dict) else {}
    fields = component.get("fields", []) if isinstance(component, dict) else []
    return [field.get("name") for field in fields if isinstance(field, dict)]


def provision(args):
    assert_mutation_gate()
    parsed_target, ownership = validate_owned_target(
        args.iut_url, args.ownership_evidence
    )
    with args.fixtures.open(encoding="utf-8") as fixture_file:
        manifest = json.load(fixture_file)
    validate_manifest(manifest)

    client = ApiClient(args.iut_url, args.timeout)
    generated_at = datetime.datetime.now(datetime.timezone.utc).replace(
        microsecond=0
    )
    timestamp = generated_at.isoformat().replace("+00:00", "Z")
    evidence = {
        "schemaVersion": 1,
        "generatedAt": timestamp,
        "target": {
            "scheme": parsed_target.scheme,
            "host": parsed_target.hostname,
            "port": parsed_target.port,
            "path": parsed_target.path,
            "loopbackOnly": True,
        },
        "ownership": {
            "runId": ownership["runId"],
            "containerId": ownership["containerId"],
            "containerName": ownership["containerName"],
            "stateSource": ownership["stateSource"],
        },
        "provisioningReady": False,
        "resourceIds": {},
        "requestMethodCounts": client.method_counts,
        "schemaEvidence": {},
        "observationEvidence": {},
        "credentialSupplied": False,
        "errors": [],
    }

    try:
        for fixture in manifest["staticFixtures"]:
            response = client.request(
                fixture["method"],
                fixture["collection"],
                fixture["mediaType"],
                copy.deepcopy(fixture["payload"]),
            )
            evidence["resourceIds"][fixture["name"]] = resource_id(
                response, fixture["name"]
            )

        variables = {
            "systemId": evidence["resourceIds"]["system"],
            "timestamp": timestamp,
        }
        data_stream = manifest["dynamicFixtures"]["dataStream"]
        data_stream_path = substitute(data_stream["collection"], variables)
        response = client.request(
            data_stream["method"],
            data_stream_path,
            data_stream["mediaType"],
            substitute(copy.deepcopy(data_stream["payload"]), variables),
        )
        variables["dataStreamId"] = resource_id(response, "dataStream")
        evidence["resourceIds"]["dataStream"] = variables["dataStreamId"]

        observation = manifest["dynamicFixtures"]["observation"]
        response = client.request(
            observation["method"],
            substitute(observation["collection"], variables),
            observation["mediaType"],
            substitute(copy.deepcopy(observation["payload"]), variables),
        )
        evidence["resourceIds"]["observation"] = resource_id(response, "observation")

        control_stream = manifest["dynamicFixtures"]["controlStream"]
        response = client.request(
            control_stream["method"],
            substitute(control_stream["collection"], variables),
            control_stream["mediaType"],
            substitute(copy.deepcopy(control_stream["payload"]), variables),
        )
        variables["controlStreamId"] = resource_id(response, "controlStream")
        evidence["resourceIds"]["controlStream"] = variables["controlStreamId"]

        data_schema = require_get_json(
            client,
            f"/datastreams/{variables['dataStreamId']}/schema?f=json",
            "DataStream schema",
        )
        control_schema = require_get_json(
            client,
            f"/controlstreams/{variables['controlStreamId']}/schema?f=json",
            "ControlStream schema",
        )
        observations = require_get_json(
            client,
            f"/datastreams/{variables['dataStreamId']}/observations?limit=10",
            "scoped Observation collection",
        )
        observation_items = (
            observations["body"].get("items", [])
            if isinstance(observations["body"], dict)
            else []
        )
        if not observation_items:
            raise RuntimeError("scoped Observation collection had no items")

        for resource_name, resource_id_value in evidence["resourceIds"].items():
            collection = {
                "system": "systems",
                "procedure": "procedures",
                "deployment": "deployments",
                "samplingFeature": "samplingFeatures",
                "dataStream": "datastreams",
                "observation": "observations",
                "controlStream": "controlstreams",
            }[resource_name]
            require_get_json(
                client,
                f"/{collection}/{resource_id_value}",
                f"{resource_name} resource",
            )

        evidence["schemaEvidence"] = {
            "dataStream": {
                "status": data_schema["status"],
                "contentType": data_schema["contentType"],
                "fieldNames": field_names(data_schema["body"], "resultSchema"),
            },
            "controlStream": {
                "status": control_schema["status"],
                "contentType": control_schema["contentType"],
                "fieldNames": field_names(
                    control_schema["body"], "parametersSchema"
                ),
            },
        }
        evidence["observationEvidence"] = {
            "status": observations["status"],
            "contentType": observations["contentType"],
            "itemCount": len(observation_items),
            "associatedDataStreamId": variables["dataStreamId"],
        }
        if evidence["schemaEvidence"]["dataStream"]["fieldNames"] != ["temperature"]:
            raise RuntimeError("DataStream schema did not retain temperature field")
        if evidence["schemaEvidence"]["controlStream"]["fieldNames"] != ["setpoint"]:
            raise RuntimeError("ControlStream schema did not retain setpoint field")
        evidence["provisioningReady"] = True
    except (KeyError, RuntimeError, ValueError) as error:
        evidence["errors"].append(str(error))

    evidence["requestMethodCounts"] = client.method_counts
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(evidence, indent=2) + "\n", encoding="utf-8")
    return 0 if evidence["provisioningReady"] else 1


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("--iut-url", required=True)
    parser.add_argument("--fixtures", type=pathlib.Path, required=True)
    parser.add_argument("--ownership-evidence", type=pathlib.Path, required=True)
    parser.add_argument("--output", type=pathlib.Path, required=True)
    parser.add_argument("--timeout", type=float, default=30.0)
    return parser.parse_args()


def main():
    args = parse_args()
    try:
        return provision(args)
    except (OSError, ValueError, json.JSONDecodeError) as error:
        print(f"[local-osh-populated-fixture] FATAL: {error}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    sys.exit(main())
