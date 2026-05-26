# S-ETS-28-01: Part 2 JSON Encoding planning

## Status
Planned; Generator pending.

## User Instruction
Planning triggered by: "Continue'" after Sprint 27 Part 2 Update was implemented, reconciled, and pushed.

## Scope
Plan the first declaration-gated, read-only Generator increment for OGC 23-002 Clause 16.1 Requirements Class "JSON Encoding".

- Requirements class: `/req/json`
- Conformance class: `/conf/json`
- Prerequisite: `http://www.opengis.net/spec/SWE/3.0/req/json-record-components`
- Conformance prerequisite: `http://www.opengis.net/spec/SWE/3.0/conf/json-record-components`
- Normative statements in scope for planning: Requirements 93-106, covering JSON media type read/write advertisement, DataStream, Observation Schema, Observation, Observation constraints, ControlStream, Command Schema, Command, Command constraints, CommandStatus, CommandResult, CommandResult constraints, and SystemEvent JSON representations.

## Planning Evidence
- Official source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 16.1 "Requirements Class JSON Encoding" and Annex A.9.
- OGC 23-002 names the requirements class `/req/json` and conformance class `/conf/json`.
- OGC 23-002 lists SWE Common 3.0 JSON record components as the prerequisite.
- The normative requirements are:
  - `/req/json/mediatype-read`
  - `/req/json/mediatype-write`
  - `/req/json/datastream-schema`
  - `/req/json/obsschema-schema`
  - `/req/json/observation-schema`
  - `/req/json/observation-constraints`
  - `/req/json/controlstream-schema`
  - `/req/json/commandschema-schema`
  - `/req/json/command-schema`
  - `/req/json/command-constraints`
  - `/req/json/commandstatus-schema`
  - `/req/json/commandresult-schema`
  - `/req/json/commandresult-constraints`
  - `/req/json/systemevent-schema`
- Resource condition gates:
  - DataStream, Observation Schema, Observation, and Observation constraints require `/conf/datastream`.
  - ControlStream, Command Schema, Command, CommandStatus, CommandResult, and their constraints require `/conf/controlstream`.
  - SystemEvent JSON assertions require `/conf/system-event`.
- GeoRobotix `/conformance` currently declares Part 2 `/conf/json`, `/conf/datastream`, `/conf/controlstream`, `/conf/system-event`, `/conf/create-replace-delete`, `/conf/swecommon-json`, `/conf/swecommon-text`, and `/conf/swecommon-binary`. It still does not declare Part 2 `/conf/api-common`, `/conf/update`, or `/conf/advanced-filtering`.
- GeoRobotix `/conformance` does not currently expose the SWE Common 3.0 prerequisite `http://www.opengis.net/spec/SWE/3.0/conf/json-record-components`; Generator must report full `/conf/json` closure as prerequisite-incomplete unless that prerequisite is visible.
- GeoRobotix direct read probes on 2026-05-26:
  - `GET /conformance`: HTTP 200.
  - `GET /systems?limit=1`: HTTP 200 `application/json`.
  - `GET /datastreams?limit=1`: HTTP 500 `text/html`.
  - `GET /observations?limit=1`: HTTP 500 `text/html`.
  - `GET /controlstreams?limit=1`: HTTP 200 `application/json`.
  - `GET /controlstreams/0m4qpft9sdag`: HTTP 200, content type reported as `auto`.
  - `GET /controlstreams/0m4qpft9sdag/schema?cmdFormat=application/json`: HTTP 200, JSON body with `commandFormat=application/json` and `parametersSchema`.
  - `GET /controlstreams/0m4qpft9sdag/commands?limit=1`: HTTP 200 `application/json` but no candidate Command item.
  - `GET /systemEvents?limit=1`: HTTP 400 `application/json`.
  - `GET /systems/0mqcvdnfoca0/events?limit=1`: HTTP 400 `application/json`, streaming-only message.
- GeoRobotix selected ControlStream advertises formats including `application/json`, `application/swe+json`, `application/swe+csv`, `application/swe+xml`, and `application/swe+binary`.
- Local OSH is running as `field-hub-osh-1` but is currently unhealthy, requires Basic auth, and the current shell has no `SMOKE_AUTH_CREDENTIAL`. Unauthenticated `GET /sensorhub/api/conformance` returns HTTP 401.

## Generator Requirements
- Add a Part 2 JSON TestNG group with official OGC 23-002 identifiers only.
- Gate all JSON assertions on exact Part 2 `/conf/json` declaration.
- Keep the SWE Common 3.0 JSON record components prerequisite visible; do not claim full `/conf/json` closure when the prerequisite is absent.
- Condition resource-specific assertions on the relevant Part 2 resource classes before any PASS: `/conf/datastream`, `/conf/controlstream`, and `/conf/system-event`.
- Implement read-only media type checks using `Accept: application/json`; require HTTP 200, JSON-compatible content type, and parseable JSON before PASS.
- Validate available candidate resources and collections against the bundled schemas named by Annex A.9 under `src/main/resources/schemas/connected-systems-2/json/`.
- For Observation, Command, and CommandResult constraint checks, require concrete parent DataStream or ControlStream schema evidence and candidate child resource evidence before PASS.
- Treat missing candidate resources, empty nested command collections, unavailable `/commands`, HTTP 400 `/systemEvents`, streaming-only event endpoints, and GeoRobotix HTTP 500 Datastream/Observation reads honestly as SKIP or FAIL depending on whether the runtime requirement is reached.
- Check `mediatype-write` only through non-mutating API-definition or operation metadata in this first increment. Do not issue public GeoRobotix POST, PUT, PATCH, or DELETE.
- OPTIONS evidence alone must not PASS `mediatype-write`.
- Add tests/comments referencing `REQ-ETS-PART2-009` and `SCENARIO-ETS-PART2-009-*` IDs.

## Planning Verification
- Direct source verification used the official OGC 23-002 HTML for Clause 16.1 and Annex A.9.
- Live IUT probes were non-mutating: `/conformance`, bounded GETs, and schema/media read checks only.
- TeamEngine planning E2E against GeoRobotix ran from a temporary Git clone with `SMOKE_OUTPUT_DIR=/tmp/sprint-ets-28-plan-georobotix-results` and failed with the known public-IUT HTTP 500 pattern: `160 total / 27 passed / 5 failed / 128 skipped`.
- The first planning smoke attempt from `git archive` failed before TeamEngine because the Dockerfile expects `.git`; it was rerun from a temporary Git clone. This is an execution-harness issue, not an ETS runtime result.
- Archived artifacts: `ops/test-results/sprint-ets-28-plan-georobotix-smoke-failed-2026-05-26.xml` and `ops/test-results/sprint-ets-28-plan-georobotix-smoke-container-failed-2026-05-26.log`.
- Failure interpretation: the failures are existing SystemFeatures/GeoJSON/SensorML/Datastream/Observation read-path HTTP 500s on the public IUT, not new Part 2 JSON runtime behavior. No Part 2 JSON tests exist yet.
- Public-IUT safety check: `scripts/no-mutation-oracle.py` recognized 61 GeoRobotix IUT request logs; explicit container-log search found no matched IUT-bound POST, PUT, PATCH, or DELETE request lines for GeoRobotix.
- Raze planning review: `.harness/evaluations/sprint-ets-28-plan-adversarial.yaml` returned `APPROVE_WITH_CONCERNS` confidence 0.92 with no required fixes. Low concern only: direct JSON-specific probe bodies are summarized but not archived as raw transcripts; Generator must reproduce or archive any positive schema/readiness evidence used for PASS or SKIP behavior.

## Definition of Done
- [x] OpenSpec splits `REQ-ETS-PART2-009` out for Part 2 JSON and keeps remaining placeholders at `REQ-ETS-PART2-010..013`.
- [x] Story, sprint contract, traceability, epic, ops status, test-results, known issues, changelog, and planner handoff are reconciled for planning.
- [x] Planning captures official OGC identifiers, prerequisite, and Requirements 93-106.
- [x] Planning captures resource condition gates for `/conf/datastream`, `/conf/controlstream`, and `/conf/system-event`.
- [x] Planning captures current GeoRobotix declaration and read-health state.
- [x] Planning captures current local OSH unauthenticated readiness limits.
- [x] Planning explicitly blocks false PASS from declaration alone, sibling declarations, broad media-type lists, empty candidate sets, unavailable endpoints, OPTIONS-only write evidence, and public-IUT mutation.
- [x] Planning TeamEngine E2E evidence is captured and documented honestly.
- [x] Raze reviews planning before Generator starts.
- [ ] Planning-only change is committed and pushed before Generator implementation.

## Out of Scope
- Implementing Part 2 JSON runtime tests in this planning step.
- Public GeoRobotix mutation.
- Positive JSON write lifecycle behavior.
- SWE Common JSON/Text/Binary encoding classes.
- Observation-binding cross-class closure beyond planning constraints for JSON dynamic-schema checks.
