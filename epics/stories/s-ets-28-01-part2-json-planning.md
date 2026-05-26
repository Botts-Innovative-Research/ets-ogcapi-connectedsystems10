# S-ETS-28-01: Part 2 JSON Encoding

## Status
PARTIAL_IMPLEMENTED by Sprint 28 Generator. Public GeoRobotix TeamEngine smoke is a failed external-IUT check, not a passing E2E gate.

## User Instruction
Planning triggered by: "Continue'" after Sprint 27 Part 2 Update was implemented, reconciled, and pushed.

Generator triggered by: "Generate!"

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

## Generator Implementation
- Added `Part2JsonTests` with a new `part2json` TestNG group using official OGC 23-002 `/req/json` and `/conf/json` identifiers.
- Runtime coverage includes exact `/conf/json` declaration, SWE Common 3.0 JSON record-components prerequisite visibility, resource condition-gate visibility, read-only JSON mediatype checks, Annex A.9 bundled schema validation, dynamic Observation/Command/CommandResult evidence guards, and non-mutating `mediatype-write` API-definition advertisement checks.
- Added `com.networknt:json-schema-validator` for draft 2020-12 JSON Schema validation against bundled resources under `src/main/resources/schemas/`.
- Fixed schema loading to map `https://csapi-compliance.local/schemas/` to `classpath:schemas/`, so TeamEngine runtime resolves bundled Part 2 and shared schemas from the all-in-one ETS artifact.
- Added `VerifyPart2JsonTests` helper regressions for official identifiers, condition gates, JSON-compatible content types, Annex A.9 schema bundling and classpath loading, non-mutating write advertisement rules, and stable group naming.
- Updated `testng.xml` and `VerifyTestNGSuiteDependency` so `part2json` depends on `core common`, all runtime methods carry the `part2json` group, and Part 2 JSON classes are co-located.

## Generator Verification
- Formatter: Docker Maven `spring-javaformat:apply` returned BUILD SUCCESS.
- Focused Maven: `VerifyPart2JsonTests,VerifyTestNGSuiteDependency` returned `72 tests / 0 failures / 0 errors / 0 skipped`.
- Full Maven: `ops/test-results/sprint-ets-28-maven-2026-05-26.log` records BUILD SUCCESS with `230 tests / 0 failures / 0 errors / 3 skipped`.
- Mandatory GeoRobotix TeamEngine smoke ran from a temporary Git clone with `SMOKE_CONTAINER_NAME=ets-csapi-s28-json-georobotix-rerun` and `SMOKE_OUTPUT_DIR=/tmp/sprint-ets-28-json-georobotix-results-rerun`.
- GeoRobotix smoke result: `176 total / 29 passed / 16 failed / 131 skipped`; archived as `ops/test-results/sprint-ets-28-generator-georobotix-smoke-failed-2026-05-26.xml` and `ops/test-results/sprint-ets-28-generator-georobotix-smoke-container-failed-2026-05-26.log`.
- Public smoke failure interpretation: existing GeoRobotix SensorML/GeoJSON/SystemFeatures/Datastream/Observation reads still return HTTP 500; new Part 2 JSON failures are honest failures for `/datastreams` and `/observations` HTTP 500 plus `/controlstreams` JSON schema validation against `controlStreamCollection.json` (`validTime` oneOf ambiguity and missing `issueTime`, `executionTime`, `live`, and `async`).
- The previous TeamEngine schema-loader failure is closed: the rerun contains no `Failed to load json schema` or `could not be schema-validated` messages.
- Public-IUT safety: the archived container log contains 75 GeoRobotix `Request: GET ...` lines and no matched GeoRobotix `POST`, `PUT`, `PATCH`, or `DELETE` request lines. `scripts/no-mutation-oracle.py` was inconclusive for this log format, so the explicit grep is the recorded mutation check.
- Local OSH was not used as an accepted Sprint 28 gate in this shell; `field-hub-osh-1` remains unhealthy and unauthenticated `/sensorhub/api/conformance` returns HTTP 401 without a provided `SMOKE_AUTH_CREDENTIAL`.
- Raze implementation review: `.harness/evaluations/sprint-ets-28-adversarial-implementation.yaml` first returned `GAPS_FOUND` confidence 0.87 for narrow schema-loader regression coverage and stale story wording. After expanding schema-loader coverage across every Annex A.9 schema and removing the stale story wording, the focused recheck returned `APPROVE_WITH_CONCERNS` confidence 0.93 with no required fixes remaining. The remaining concern is non-blocking URI path escaping hardening.

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
- [x] Planning-only change is committed and pushed before Generator implementation.
- [x] Generator adds Part 2 JSON runtime tests and helper regressions with REQ/SCENARIO traceability.
- [x] TestNG wiring adds `part2json` with structural lint coverage.
- [x] Formatter and Maven verification are complete and archived.
- [x] Mandatory GeoRobotix TeamEngine smoke is complete and archived.
- [x] Failed public E2E outcome is documented honestly and not reported as passing.
- [x] Public GeoRobotix mutation check records no POST/PUT/PATCH/DELETE request lines.
- [ ] Full positive `/conf/json` closure remains open pending a healthy declaring IUT with valid JSON resources and SWE prerequisite evidence.
- [ ] Positive JSON write lifecycle behavior remains out of scope for this read-only increment.

## Out of Scope
- Public GeoRobotix mutation.
- Positive JSON write lifecycle behavior.
- SWE Common JSON/Text/Binary encoding classes.
- Semantic Observation/Command/CommandResult validation against parent DataStream or ControlStream schemas; the Generator records prerequisite/candidate evidence but avoids shape-only PASS.
- Observation-binding cross-class closure beyond the JSON dynamic-schema evidence guards.
