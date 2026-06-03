#!/usr/bin/env python3
"""Preseed/probe a dedicated local OSH SimUAV populated fixture.

REQ-ETS-PART2-013:
  SCENARIO-ETS-PART2-013-SIMUAV-PRESEEDED-POPULATED-IUT-001
  SCENARIO-ETS-PART2-013-POPULATED-CANDIDATE-SELECTION-001

This script intentionally records whether auth was supplied, never the credential value.
"""

from __future__ import annotations

import argparse
import json
import os
import sys
import time
from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Any
from urllib import error, parse, request


COMMAND_BODY = {
    "parameters": {
        "position": {
            "lat": 34.804,
            "lon": -86.723,
            "alt": 120.0,
        },
        "velocity": 12.0,
    }
}


@dataclass
class HttpResult:
    method: str
    path: str
    status: int
    content_type: str | None
    body: Any
    body_text: str


class Client:
    def __init__(self, base_url: str, auth_credential: str | None) -> None:
        self.base_url = base_url.rstrip("/") + "/"
        self.auth_credential = auth_credential
        self.method_counts: dict[str, int] = {}

    def get(self, path: str, accept: str = "application/json") -> HttpResult:
        return self._request("GET", path, None, accept)

    def post_json(self, path: str, body: Any) -> HttpResult:
        return self._request("POST", path, body, "application/json")

    def _request(self, method: str, path: str, body: Any, accept: str) -> HttpResult:
        self.method_counts[method] = self.method_counts.get(method, 0) + 1
        url = parse.urljoin(self.base_url, path)
        data = None
        headers = {"Accept": accept}
        if self.auth_credential:
            headers["Authorization"] = self.auth_credential
        if body is not None:
            data = json.dumps(body).encode("utf-8")
            headers["Content-Type"] = "application/json"
        req = request.Request(url, data=data, headers=headers, method=method)
        try:
            with request.urlopen(req, timeout=20) as response:
                raw = response.read()
                text = raw.decode("utf-8", errors="replace")
                content_type = response.headers.get("Content-Type")
                return HttpResult(method, path, response.status, content_type, parse_json(text), text)
        except error.HTTPError as exc:
            raw = exc.read()
            text = raw.decode("utf-8", errors="replace")
            return HttpResult(method, path, exc.code, exc.headers.get("Content-Type"), parse_json(text), text)


def parse_json(text: str) -> Any:
    if not text:
        return None
    try:
        return json.loads(text)
    except json.JSONDecodeError:
        return None


def items(result: HttpResult) -> list[dict[str, Any]]:
    body = result.body
    if not isinstance(body, dict):
        return []
    values = body.get("items")
    if not isinstance(values, list):
        return []
    return [item for item in values if isinstance(item, dict)]


def resource_id(resource: dict[str, Any]) -> str | None:
    value = resource.get("id")
    return value if isinstance(value, str) and value else None


def is_simuav_resource(resource: dict[str, Any]) -> bool:
    text = json.dumps(resource, sort_keys=True).lower()
    return "simuav" in text or "simulated uav" in text


def summarize_http(result: HttpResult, include_body: bool = True) -> dict[str, Any]:
    summary: dict[str, Any] = {
        "method": result.method,
        "path": result.path,
        "status": result.status,
        "contentType": result.content_type,
        "bodyParsedAsJson": result.body is not None,
    }
    if include_body:
        summary["body"] = result.body if result.body is not None else result.body_text
    return summary


def wait_for(predicate, timeout_s: int, interval_s: float = 2.0):
    deadline = time.time() + timeout_s
    last_value = None
    while time.time() < deadline:
        last_value = predicate()
        if last_value:
            return last_value
        time.sleep(interval_s)
    return last_value


def wait_for_collection_items(fetch, timeout_s: int, interval_s: float = 2.0) -> HttpResult | None:
    deadline = time.time() + timeout_s
    last_result = None
    while time.time() < deadline:
        last_result = fetch()
        if items(last_result):
            return last_result
        time.sleep(interval_s)
    return last_result


def first_observation_for_datastream(client: Client, datastream_id: str) -> HttpResult:
    return client.get(f"datastreams/{datastream_id}/observations?limit=1")


def first_command_for_controlstream(client: Client, controlstream_id: str) -> HttpResult:
    return client.get(f"controlstreams/{controlstream_id}/commands?limit=1")


def select_waypoint_controlstream(controlstreams: list[dict[str, Any]]) -> dict[str, Any]:
    def field_text(resource: dict[str, Any], *fields: str) -> str:
        return " ".join(str(resource.get(field, "")) for field in fields).lower()

    for controlstream in controlstreams:
        if field_text(controlstream, "inputName") == "waypoint_feasibility":
            return controlstream
    for controlstream in controlstreams:
        text = field_text(controlstream, "inputName", "name", "description")
        if "waypoint" in text or "feasibility" in text:
            return controlstream
    return controlstreams[0]


def main() -> int:
    parser = argparse.ArgumentParser(description="Preseed/probe a local OSH SimUAV populated fixture.")
    parser.add_argument("--iut-url", default=os.environ.get("SMOKE_IUT_URL", "http://localhost:8081/sensorhub/api"))
    parser.add_argument("--output", required=True)
    parser.add_argument("--timeout", type=int, default=120)
    parser.add_argument("--auth-credential", default=os.environ.get("SMOKE_AUTH_CREDENTIAL"))
    parser.add_argument("--no-command-post", action="store_true")
    args = parser.parse_args()

    if os.environ.get("SIMUAV_PRESEED_ENABLED") != "true":
        print("SIMUAV_PRESEED_ENABLED=true is required for this dedicated populated fixture.", file=sys.stderr)
        return 2
    if os.environ.get("SMOKE_MUTATION_TESTS_ENABLED") != "true" or os.environ.get(
        "SMOKE_MUTATION_IUT_POLICY"
    ) != "dedicated-mutable-iut":
        print("Dedicated mutable-IUT mutation gate is required for SimUAV command preseed.", file=sys.stderr)
        return 2

    client = Client(args.iut_url, args.auth_credential)
    started_at = datetime.now(timezone.utc).isoformat().replace("+00:00", "Z")
    evidence: dict[str, Any] = {
        "scenario": "SCENARIO-ETS-PART2-013-SIMUAV-PRESEEDED-POPULATED-IUT-001",
        "startedAt": started_at,
        "iutUrl": args.iut_url,
        "authCredentialRecorded": False,
        "authCredentialSupplied": bool(args.auth_credential),
        "mutationGate": {
            "SIMUAV_PRESEED_ENABLED": os.environ.get("SIMUAV_PRESEED_ENABLED"),
            "SMOKE_MUTATION_TESTS_ENABLED": os.environ.get("SMOKE_MUTATION_TESTS_ENABLED"),
            "SMOKE_MUTATION_IUT_POLICY": os.environ.get("SMOKE_MUTATION_IUT_POLICY"),
        },
    }

    evidence["conformance"] = summarize_http(client.get("conformance"), include_body=False)

    def dynamic_resources_ready() -> dict[str, Any] | None:
        datastreams_result = client.get("datastreams?limit=25")
        controlstreams_result = client.get("controlstreams?limit=25")
        datastreams = [item for item in items(datastreams_result) if is_simuav_resource(item)]
        controlstreams = [item for item in items(controlstreams_result) if is_simuav_resource(item)]
        if datastreams and controlstreams:
            return {
                "datastreamsResult": datastreams_result,
                "controlstreamsResult": controlstreams_result,
                "datastreams": datastreams,
                "controlstreams": controlstreams,
            }
        return None

    resources = wait_for(dynamic_resources_ready, args.timeout)
    if not resources:
        evidence["result"] = "FAIL_SIMUAV_DYNAMIC_RESOURCES_NOT_READY"
        evidence["requestMethodCounts"] = client.method_counts
        write_output(args.output, evidence)
        print(f"SimUAV dynamic resources were not ready within {args.timeout}s.", file=sys.stderr)
        return 1

    datastreams: list[dict[str, Any]] = resources["datastreams"]
    controlstreams: list[dict[str, Any]] = resources["controlstreams"]
    evidence["datastreams"] = datastreams
    evidence["controlstreams"] = controlstreams
    evidence["datastreamCollection"] = summarize_http(resources["datastreamsResult"], include_body=False)
    evidence["controlStreamCollection"] = summarize_http(resources["controlstreamsResult"], include_body=False)

    schemas: list[dict[str, Any]] = []
    observations: list[dict[str, Any]] = []
    observation_probes: list[dict[str, Any]] = []
    for datastream in datastreams:
        ds_id = resource_id(datastream)
        if not ds_id:
            continue
        schema = client.get(f"datastreams/{ds_id}/schema?f=json")
        schemas.append(summarize_http(schema))
        obs = wait_for_collection_items(
            lambda ds=ds_id: first_observation_for_datastream(client, ds),
            min(args.timeout, 60),
        )
        if obs and items(obs):
            observations.append({"datastreamId": ds_id, "response": summarize_http(obs)})
        else:
            observation_probes.append(
                {"datastreamId": ds_id, "response": summarize_http(obs) if obs else None, "itemCount": 0}
            )
    evidence["schemaEndpoints"] = schemas
    evidence["observations"] = observations
    evidence["observationProbesWithoutItems"] = observation_probes

    command_schemas: list[dict[str, Any]] = []
    for controlstream in controlstreams:
        cs_id = resource_id(controlstream)
        if not cs_id:
            continue
        schema = client.get(f"controlstreams/{cs_id}/schema?f=json")
        command_schemas.append(summarize_http(schema))
    evidence["commandSchemaEndpoints"] = command_schemas

    waypoint = select_waypoint_controlstream(controlstreams)
    waypoint_id = resource_id(waypoint)
    evidence["selectedCommandControlStream"] = waypoint
    if not waypoint_id:
        evidence["result"] = "FAIL_NO_CONTROLSTREAM_ID"
        evidence["requestMethodCounts"] = client.method_counts
        write_output(args.output, evidence)
        return 1

    if not args.no_command_post:
        command_post = client.post_json(f"controlstreams/{waypoint_id}/commands", COMMAND_BODY)
        evidence["commandPost"] = {
            "request": COMMAND_BODY,
            "response": summarize_http(command_post),
        }
    else:
        evidence["commandPost"] = {"skipped": True}

    command_collection = wait_for_collection_items(
        lambda: first_command_for_controlstream(client, waypoint_id),
        min(args.timeout, 60),
    )
    evidence["nestedCommands"] = summarize_http(command_collection) if command_collection else None

    command_id = None
    if command_collection:
        command_items = items(command_collection)
        if command_items:
            command_id = resource_id(command_items[0])
    if command_id:
        evidence["statusHistory"] = summarize_http(client.get(f"commands/{command_id}/status?limit=10"))
        evidence["commandResult"] = summarize_http(client.get(f"commands/{command_id}/result?limit=10"))
    evidence["requestMethodCounts"] = client.method_counts

    if not observations or not command_collection:
        evidence["result"] = "PARTIAL_MISSING_OBSERVATION_OR_COMMAND_CHILD_EVIDENCE"
        write_output(args.output, evidence)
        return 1

    evidence["result"] = "PASS_PRESEEDED_SIMUAV_POPULATED_EVIDENCE_READY"
    write_output(args.output, evidence)
    return 0


def write_output(path: str, evidence: dict[str, Any]) -> None:
    evidence["finishedAt"] = datetime.now(timezone.utc).isoformat().replace("+00:00", "Z")
    directory = os.path.dirname(path)
    if directory:
        os.makedirs(directory, exist_ok=True)
    with open(path, "w", encoding="utf-8") as handle:
        json.dump(evidence, handle, indent=2, sort_keys=True)
        handle.write("\n")


if __name__ == "__main__":
    sys.exit(main())
