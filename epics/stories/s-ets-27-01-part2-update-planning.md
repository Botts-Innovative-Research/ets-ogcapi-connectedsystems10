# S-ETS-27-01: Part 2 Update safety-gated planning

## Status
Partial Implemented; Generator completed first safety-gated subset.

## User Instruction
Planning triggered by: "Continue" after Sprint 26 Part 2 Create/Replace/Delete was implemented, reconciled, and pushed.
Generator triggered by: "Continue" after Sprint 27 planning was pushed.

## Scope
Plan the first safety-gated Generator increment for OGC 23-002 Clause 15 Requirements Class "Update".

- Requirements class: `/req/update`
- Conformance class: `/conf/update`
- Prerequisites: `/req/create-replace-delete` and `http://www.opengis.net/spec/ogcapi-features-4/1.0/req/update`
- Conformance prerequisites visible in `/conformance`: Part 2 `/conf/create-replace-delete` and `http://www.opengis.net/spec/ogcapi-features-4/1.0/conf/update`
- Normative statements in scope for planning: Requirements 79-92, covering DataStream, Observation, ControlStream, Command, CommandStatus, CommandResult, Feasibility, Feasibility status/result, and SystemEvent PATCH behavior

## Planning Evidence
- Official source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 15 "Requirements Class Update" and Annex A.8.
- OGC 23-002 names the requirements class `/req/update` and conformance class `/conf/update`.
- OGC 23-002 says Part 2 Update uses HTTP PATCH through OGC API - Features - Part 4 Update semantics at Connected Systems resource endpoints.
- Clause 15 condition gates:
  - Requirements 79-82 apply only when the Datastreams & Observations class applies (`/conf/datastream`).
  - Requirements 83-88 apply only when the Control Streams & Commands class applies (`/conf/controlstream`).
  - Requirements 89-91 apply only when the Command Feasibility class applies (`/conf/feasibility`).
  - Requirement 92 applies only when the System Events class applies (`/conf/system-event`).
- The normative requirements are:
  - `/req/update/datastream`
  - `/req/update/datastream-update-schema`
  - `/req/update/observation`
  - `/req/update/observation-schema`
  - `/req/update/controlstream`
  - `/req/update/controlstream-update-schema`
  - `/req/update/command`
  - `/req/update/command-schema`
  - `/req/update/command-status`
  - `/req/update/command-result`
  - `/req/update/feasibility`
  - `/req/update/feasibility-status`
  - `/req/update/feasibility-result`
  - `/req/update/system-event`
- GeoRobotix `/conformance` currently declares Part 2 `/conf/create-replace-delete` and OGC API Features Part 4 `/conf/create-replace-delete`, but does not declare Part 2 `/conf/update`.
- GeoRobotix `/conformance` currently declares sibling Part 2 `/conf/datastream`, `/conf/controlstream`, `/conf/system-event`, `/conf/json`, and SWE Common encoding classes, but does not declare Part 2 `/conf/api-common`, `/conf/update`, or `/conf/advanced-filtering`.
- GeoRobotix read-only OPTIONS probes returned HTTP 200 and broad `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS` headers for sampled DataStream, Observation, ControlStream, Command, Feasibility, SystemEvent, and system-scoped events endpoints. PATCH was not advertised.
- GeoRobotix direct read probes still show the current public-IUT health issue from Sprint 26: `GET /systems/0mqcvdnfoca0`, `GET /datastreams?limit=1`, and `GET /observations?limit=1` returned HTTP 500; `GET /controlstreams?limit=1` returned HTTP 200 JSON.
- Local OSH is running as `field-hub-osh-1` and requires Basic auth. Unauthenticated `GET /sensorhub/api/conformance` returns HTTP 401. Authenticated `GET /sensorhub/api/conformance` returns HTTP 200, does not declare Part 2 `/conf/update`, and authenticated `OPTIONS /sensorhub/api/systems/040g` returns HTTP 200 with no PATCH in `Allow`.

## Generator Requirements
- Add a Part 2 Update TestNG group with official OGC 23-002 identifiers only.
- Gate all Part 2 Update assertions on exact `/conf/update` declaration.
- Keep Part 2 Create/Replace/Delete and OGC API Features Part 4 Update prerequisites visible and separate from declaration evidence.
- Gate each Requirement 79-92 assertion on its Clause 15 condition class before any PASS: `/conf/datastream` for DataStream/Observation, `/conf/controlstream` for ControlStream/Command/CommandStatus/CommandResult, `/conf/feasibility` for Feasibility/status/result, and `/conf/system-event` for SystemEvent.
- Missing condition classes must produce prerequisite-incomplete SKIP behavior, not PASS from `/conf/update`, endpoint availability, sibling declarations, or OPTIONS.
- Preserve public-IUT safety: the default GeoRobotix smoke must issue zero IUT-bound PATCH, POST, PUT, or DELETE requests.
- Reuse the existing mutation safety parameters: `mutation-tests-enabled=true` and `mutation-iut-policy=dedicated-mutable-iut`.
- Hard-deny PATCH against public GeoRobotix before dispatch, even if a future probe sees `/conf/update` or broad method advertisement.
- Treat OPTIONS method discovery as readiness evidence only. OPTIONS alone must not PASS any PATCH lifecycle assertion.
- If an IUT declares `/conf/update` and a successful OPTIONS response for the selected resource endpoint omits PATCH, readiness should FAIL for that endpoint while lifecycle PATCH SKIPs before dispatch.
- Positive PATCH lifecycle checks must run only against a dedicated mutable IUT, must verify the changed field by GET after PATCH, and must clean up any temporary resources where applicable.
- Schema-rejection requirements for DataStream, Observation, ControlStream, and Command PATCH must not be claimed without safe mutation opt-in and concrete parent schema evidence.
- SKIP honestly when `/commands`, `/feasibility`, `/systemEvents`, or `/systems/{sysId}/events` are unavailable, invalid resources, streaming-only, or lack candidate item evidence.

## Planning Verification
- Direct source verification used the official OGC 23-002 HTML for Clause 15 and Annex A.8.
- Live IUT probes were non-mutating: `/conformance`, OPTIONS, and bounded GET only.
- TeamEngine planning E2E against GeoRobotix ran with `SMOKE_OUTPUT_DIR=/tmp/sprint-ets-27-plan-georobotix-results` and failed with the known public-IUT HTTP 500 pattern: `146 total / 27 passed / 5 failed / 114 skipped`.
- Archived artifacts: `ops/test-results/sprint-ets-27-plan-georobotix-smoke-failed-2026-05-22.xml` and `ops/test-results/sprint-ets-27-plan-georobotix-smoke-container-failed-2026-05-22.log`.
- Failure interpretation: the failures are existing SystemFeatures/GeoJSON/SensorML/Datastream/Observation read-path HTTP 500s on the public IUT, not new Part 2 Update runtime behavior. No Part 2 Update tests exist yet.
- Public-IUT safety check: the archived container log contains no matched IUT-bound PATCH, POST, PUT, or DELETE request lines for GeoRobotix.
- Local OSH needs a credential-bearing smoke environment before it can be accepted for this Sprint 27 planning gate.

## Generator Implementation
- Added `Part2UpdateTests` with 14 runtime checks plus shared read-only setup for exact `/conf/update` declaration, Part 2 CRD and Features Part 4 Update prerequisite visibility, Clause 15 condition-gate visibility, public-IUT mutation safety, DataStream/Observation PATCH readiness and deferred lifecycle checks, ControlStream/Command PATCH readiness and deferred lifecycle checks, separate Feasibility and SystemEvent PATCH readiness and deferred lifecycle checks, unavailable-endpoint honesty, and schema-rejection honesty.
- Added `VerifyPart2UpdateTests` with 9 helper regressions for official identifiers, condition-gate missing-class reporting, exact declaration matching, public GeoRobotix hard denial, explicit mutation parameters, `Allow: PATCH` parsing, collection shape, condition messages, and group naming.
- Updated `testng.xml` with the `part2update` group and class. The group depends on `core common systemfeatures`, not Part 1 Update, Part 2 API Common, Part 2 Create/Replace/Delete, or resource-class groups, so prerequisite and condition-gate honesty remains runtime-visible.
- Updated `VerifyTestNGSuiteDependency` with structural checks for `part2update` dependencies, method tagging, and co-location with Core, Common, SystemFeatures, Part 2 Create/Replace/Delete, and the Part 2 resource classes.
- Positive PATCH lifecycle and schema-rejection dispatch remain deferred until a non-public dedicated mutable IUT declares `/conf/update`, advertises PATCH on selected endpoints, supplies concrete fixture/schema evidence, permits changed-field GET proof, and supports cleanup.

## Generator Verification
- Formatter: Docker Maven `mvn -B spring-javaformat:apply` returned BUILD SUCCESS.
- Maven: `bash scripts/mvn-test-via-docker.sh` returned BUILD SUCCESS with `219 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-27-maven-2026-05-22.log`.
- TeamEngine GeoRobotix smoke: `SMOKE_CONTAINER_NAME=ets-csapi-s27-generator-georobotix SMOKE_OUTPUT_DIR=/tmp/sprint-ets-27-generator-georobotix-results bash scripts/smoke-test.sh` failed `160 total / 27 passed / 5 failed / 128 skipped`.
- GeoRobotix failure interpretation: the public IUT still returns HTTP 500 on existing read paths. Part 2 Update added 14 runtime tests; all 14 SKIP because `systemfeatures` did not finish successfully. This is not a zero-failure E2E pass.
- Public-IUT safety check: `scripts/no-mutation-oracle.py` recognized 61 GeoRobotix IUT request logs and found zero IUT-bound PATCH/POST/PUT/DELETE. An explicit `rg` scan also found no matched GeoRobotix write-method lines.
- Accepted local OSH E2E: authenticated TeamEngine smoke with explicit dedicated mutable-IUT opt-in passed `160 total / 62 passed / 0 failed / 98 skipped`. All 14 Part 2 Update runtime tests SKIP because local OSH does not declare Part 2 `/conf/update`, and the local OSH container log contains zero PATCH request lines.
- Archived artifacts: `ops/test-results/sprint-ets-27-generator-georobotix-smoke-failed-2026-05-22.xml`, `ops/test-results/sprint-ets-27-generator-georobotix-smoke-container-failed-2026-05-22.log`, `ops/test-results/sprint-ets-27-generator-local-osh-smoke-2026-05-22.xml`, and `ops/test-results/sprint-ets-27-generator-local-osh-smoke-container-2026-05-22.log`.

## Definition of Done
- [x] OpenSpec splits `REQ-ETS-PART2-008` out for Part 2 Update and keeps remaining placeholders at `REQ-ETS-PART2-009..013`.
- [x] Story, sprint contract, traceability, epic, ops status, test-results, known issues, changelog, and planner handoff are reconciled for planning.
- [x] Planning captures official OGC identifiers, prerequisites, and Requirements 79-92.
- [x] Planning captures Clause 15 per-requirement condition gates for `/conf/datastream`, `/conf/controlstream`, `/conf/feasibility`, and `/conf/system-event`.
- [x] Planning captures current GeoRobotix declaration, broad OPTIONS-without-PATCH, and HTTP 500 read-health state.
- [x] Planning captures current local OSH unauthenticated readiness limits.
- [x] Planning explicitly blocks false PASS from OPTIONS evidence, sibling declarations, public-IUT PATCH, and unavailable endpoints.
- [x] Planning TeamEngine E2E evidence is captured and explicitly documented as blocked by public-IUT health and missing local OSH credential.
- [x] Raze reviews planning before Generator starts: `.harness/evaluations/sprint-ets-27-plan-adversarial.yaml` final verdict `APPROVE_WITH_CONCERNS` confidence 0.95 after closing `RAZE-ETS27-PLAN-GAP-001`.
- [x] Planning-only change is committed and pushed before Generator implementation.
- [x] Generator adds runtime and helper regression coverage for the first safety-gated Part 2 Update subset.
- [x] TestNG wiring and structural lint cover `part2update`.
- [x] Formatter and Maven verification complete.
- [x] Mandatory GeoRobotix TeamEngine smoke is run and documented honestly as failed external/public-IUT evidence, not a pass.
- [x] Accepted local OSH TeamEngine E2E gate passes with zero failures.
- [x] Raze reviews Generator implementation before final completion: `.harness/evaluations/sprint-ets-27-adversarial-implementation.yaml` found docs-only gap `RAZE-ETS27-IMPL-GAP-001`; focused recheck `.harness/evaluations/sprint-ets-27-adversarial-implementation-recheck.yaml` closed it with `APPROVE_WITH_CONCERNS` confidence 0.94.
- [x] Raze reviews accepted local OSH E2E evidence: `.harness/evaluations/sprint-ets-27-local-osh-e2e-acceptance-raze.yaml` returned `APPROVE_WITH_CONCERNS` confidence 0.94 with no required fixes.

## Out of Scope
- Running PATCH against GeoRobotix.
- Positive PATCH lifecycle coverage without explicit dedicated mutable-IUT opt-in, concrete fixture/schema evidence, changed-field GET proof, and cleanup.
- Full closure of Part 2 JSON, SWE Common encodings, or observation-binding.
