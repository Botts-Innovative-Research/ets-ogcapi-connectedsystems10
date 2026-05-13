# S-ETS-26-01: Part 2 Create/Replace/Delete safety-gated planning

## Status
Planned; Generator pending.

## User Instruction
Planning triggered by: "Continue" after Sprint 25 Advanced Filtering was implemented, reconciled, and pushed.

## Scope
Plan the first Generator increment for OGC 23-002 Clause 14 Requirements Class "Create/Replace/Delete".

- Requirements class: `/req/create-replace-delete`
- Conformance class: `/conf/create-replace-delete`
- Prerequisite: `http://www.opengis.net/spec/ogcapi-features-4/1.0/req/create-replace-delete`
- Conformance prerequisite visible in `/conformance`: `http://www.opengis.net/spec/ogcapi-features-4/1.0/conf/create-replace-delete`
- Normative statements in scope for planning: Requirements 63-78, covering DataStream, Observation, ControlStream, Command, CommandStatus, CommandResult, Feasibility, Feasibility status/result, and SystemEvent lifecycle behavior

## Planning Evidence
- Official source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 14 "Requirements Class Create/Replace/Delete" and Annex A.7.
- OGC 23-002 names the requirements class `/req/create-replace-delete` and conformance class `/conf/create-replace-delete`.
- OGC 23-002 lists OGC API - Features - Part 4 Create/Replace/Delete as the prerequisite.
- The normative requirements are:
  - `/req/create-replace-delete/datastream`
  - `/req/create-replace-delete/datastream-update-schema`
  - `/req/create-replace-delete/datastream-delete-cascade`
  - `/req/create-replace-delete/observation`
  - `/req/create-replace-delete/observation-schema`
  - `/req/create-replace-delete/controlstream`
  - `/req/create-replace-delete/controlstream-update-schema`
  - `/req/create-replace-delete/controlstream-delete-cascade`
  - `/req/create-replace-delete/command`
  - `/req/create-replace-delete/command-schema`
  - `/req/create-replace-delete/command-status`
  - `/req/create-replace-delete/command-result`
  - `/req/create-replace-delete/feasibility`
  - `/req/create-replace-delete/feasibility-status`
  - `/req/create-replace-delete/feasibility-result`
  - `/req/create-replace-delete/system-event`
- GeoRobotix `/conformance` declares `/conf/create-replace-delete`.
- GeoRobotix `/conformance` declares the OGC API - Features - Part 4 `/conf/create-replace-delete` prerequisite.
- GeoRobotix `/conformance` declares sibling Part 2 `/conf/datastream`, `/conf/controlstream`, `/conf/system-event`, `/conf/json`, and SWE Common encoding classes, but does not declare Part 2 `/conf/api-common`, `/conf/update`, or `/conf/advanced-filtering`.
- GeoRobotix read-only OPTIONS probes returned HTTP 200 and broad `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS` headers for `/datastreams`, `/datastreams/{id}`, `/observations`, `/controlstreams`, `/commands`, `/controlstreams/{id}/commands`, `/systems/{id}/events`, `/systemEvents`, and `/feasibility`.
- GeoRobotix `GET /commands?limit=1` returned HTTP 400 `Invalid resource name: 'commands'`.
- GeoRobotix `GET /systemEvents?limit=1` returned HTTP 400 `Invalid resource name: 'systemEvents'`.
- GeoRobotix `GET /systems/{id}/events?limit=1` returned HTTP 400 `Only streaming requests supported on this resource`.
- GeoRobotix `GET /feasibility?limit=1` returned HTTP 400 `Invalid resource name: 'feasibility'`.

## Generator Requirements
- Add a Part 2 Create/Replace/Delete TestNG group with official OGC 23-002 identifiers only.
- Gate all Part 2 CRD assertions on exact `/conf/create-replace-delete` declaration.
- Keep the OGC API - Features - Part 4 Create/Replace/Delete prerequisite visible and separate from Connected Systems declaration evidence.
- Preserve public-IUT safety: the default GeoRobotix smoke must issue zero IUT-bound POST, PUT, DELETE, or PATCH requests.
- Reuse the existing mutation safety parameters from Part 1 CRD: `mutation-tests-enabled=true` and `mutation-iut-policy=dedicated-mutable-iut`.
- Hard-deny mutation against public GeoRobotix before dispatch, even if the IUT declares `/conf/create-replace-delete` and advertises write methods via OPTIONS.
- Treat OPTIONS method discovery as readiness evidence only. OPTIONS alone must not PASS any lifecycle assertion.
- For the first Generator increment, implement safe declaration, prerequisite, safety-gate, and readiness checks before broader positive lifecycle work.
- Any positive POST/PUT/DELETE lifecycle tests must run only against a dedicated mutable IUT and must clean up created resources.
- SKIP honestly when `/commands`, `/feasibility`, `/systemEvents`, or `/systems/{sysId}/events` are unavailable, invalid resources, or streaming-only JSON-unreadable endpoints.

## Planning Verification
- TeamEngine smoke: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s26-plan bash scripts/smoke-test.sh` reported `137 total / 72 passed / 0 failed / 65 skipped` against GeoRobotix.
- No-mutation oracle: zero IUT-bound POST/PUT/DELETE/PATCH across 100 recognized request-log entries.
- Smoke artifacts: `ops/test-results/sprint-ets-26-plan-smoke-2026-05-13.xml` and `ops/test-results/sprint-ets-26-plan-smoke-container-2026-05-13.log`.
- No Java code changed in this planning sprint; Maven unit/lint verification remains a Generator gate.

## Definition of Done
- [x] OpenSpec splits `REQ-ETS-PART2-007` from the remaining placeholders and defines CRD-specific scenarios.
- [x] Story, sprint contract, traceability, epic, ops status, test-results, known issues, and planner handoff are reconciled for planning.
- [x] Planning captures official OGC identifiers, prerequisite, and Requirements 63-78.
- [x] Planning captures current GeoRobotix declaration, prerequisite, broad OPTIONS, and HTTP 400 endpoint state.
- [x] Planning explicitly blocks false PASS from OPTIONS evidence, sibling declarations, public-IUT mutation, and unavailable endpoints.
- [x] Planning E2E smoke ran against the real TeamEngine/GeoRobotix stack and verified zero IUT-bound mutation requests.
- [x] Raze reviews planning before Generator starts: `.harness/evaluations/sprint-ets-26-plan-adversarial.yaml` final verdict `APPROVE` confidence 0.96 after closing `RAZE-ETS26-PLAN-GAP-001`.
- [x] Planning-only change is committed and pushed before Generator implementation: `146c4c6 Plan Sprint 26 Part 2 CRD` (`7d57d9f..146c4c6 main -> main`).

## Out of Scope
- Implementing Java CRD tests in this planning sprint.
- Running POST, PUT, DELETE, or PATCH against GeoRobotix.
- Full positive lifecycle coverage for DataStreams, Observations, ControlStreams, Commands, Feasibility, or SystemEvents.
- Part 2 Update, JSON, SWE Common encodings, and observation-binding closure.
