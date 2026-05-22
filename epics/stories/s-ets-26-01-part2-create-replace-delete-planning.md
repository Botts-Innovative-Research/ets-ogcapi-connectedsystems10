# S-ETS-26-01: Part 2 Create/Replace/Delete safety-gated planning

## Status
Partial Implemented; Generator code added. Accepted Sprint 26 E2E gate is the seeded local OSH full-stack TeamEngine run after fixture repair.

## User Instruction
Planning triggered by: "Continue" after Sprint 25 Advanced Filtering was implemented, reconciled, and pushed.

Generator resumed by: "we got disrupted - pick up where you left off."

## Scope
Plan and implement the first safety-gated Generator increment for OGC 23-002 Clause 14 Requirements Class "Create/Replace/Delete".

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
- Generator gapfix note: those planning probes are IUT-state diagnostics only. Runtime CRD readiness now uses OGC 23-002 Clause 14 scoped templates for DataStream, Observation, and ControlStream create readiness: `/systems/{sysId}/datastreams`, `/datastreams/{dsId}/observations`, and `/systems/{sysId}/controlstreams`.
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

## Generator Implementation
- Added `Part2CreateReplaceDeleteTests` with 9 safety-gated TestNG methods for exact Part 2 declaration, OGC API Features Part 4 prerequisite visibility, mutation safety, read-only OPTIONS readiness, unavailable endpoint honesty, and deferred positive lifecycle checks for DataStream/Observation, ControlStream/Command, and Feasibility/SystemEvent.
- Added `VerifyPart2CreateReplaceDeleteTests` with 9 helper regressions for official identifiers, scoped readiness path selection, associated-System evidence, exact conformance declaration matching, public GeoRobotix hard denial, explicit mutation parameters, `Allow` parsing, collection shape, and stable TestNG group naming.
- Extended `testng.xml` with `part2createreplacedelete` depending on `core common systemfeatures`.
- Extended `VerifyTestNGSuiteDependency` with structural checks for Part 2 CRD group dependencies, required method group tagging, and co-location with foundational/resource classes.
- Positive Part 2 POST/PUT/DELETE lifecycle mutation remains deferred until dedicated non-system fixtures and cleanup logic are implemented; the current lifecycle tests SKIP after the explicit mutation gate with precise no-mutation messages.

## Generator Verification
- Formatter: Docker Maven `mvn -B spring-javaformat:apply` returned BUILD SUCCESS.
- Raze implementation review: `.harness/evaluations/sprint-ets-26-adversarial-implementation.yaml` returned `GAPS_FOUND` confidence 0.88 for non-normative global OPTIONS readiness probes.
- Raze gapfix: DataStream, Observation, and ControlStream readiness probes now use scoped Clause 14 endpoint templates or SKIP when parent IDs cannot be established; global collection endpoints are not used for create-readiness PASS-style diagnostics.
- Focused Raze gapfix recheck: `.harness/evaluations/sprint-ets-26-adversarial-gapfix.yaml` returned `APPROVE_WITH_CONCERNS` confidence 0.94. `RAZE-ETS26-IMPL-GAP-001` is closed; at review time the remaining blocker was mandatory E2E.
- Maven: `bash scripts/mvn-test-via-docker.sh` returned BUILD SUCCESS after the gapfix; Surefire summary `207 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-26-maven-2026-05-22.log`.
- GeoRobotix TeamEngine smoke after the Raze gapfix: reached TestNG but failed `146 total / 27 passed / 5 failed / 114 skipped`; archived at `ops/test-results/sprint-ets-26-gapfix-georobotix-smoke-failed-2026-05-22.xml` and `ops/test-results/sprint-ets-26-gapfix-georobotix-smoke-container-failed-2026-05-22.log`.
- GeoRobotix blocker: direct probes returned HTTP 500 for `GET /systems/0mqcvdnfoca0`, `GET /datastreams?limit=1`, and `GET /observations?limit=2`; the new Part 2 CRD tests dependency-SKIP because `systemfeatures` did not finish successfully.
- GeoRobotix disposition: retained as advisory public interoperability evidence for Sprint 26 because the accepted E2E gate is the seeded local OSH IUT.
- Public-IUT mutation evidence: the GeoRobotix container log contains no logged IUT-bound POST, PUT, DELETE, or PATCH request.
- Local OSH fallback smoke after the Raze gapfix with Basic auth and explicit mutable-IUT opt-in reached TestNG but failed `146 total / 61 passed / 4 failed / 81 skipped`; archived at `ops/test-results/sprint-ets-26-gapfix-local-osh-smoke-failed-2026-05-22.xml` and `ops/test-results/sprint-ets-26-gapfix-local-osh-smoke-container-failed-2026-05-22.log`.
- Local OSH blocker: four pre-existing SensorML deployment/procedure alternate-resource checks returned HTTP 500; direct `GET /sensorhub/api/procedures/040g?f=sml3` also returned HTTP 500. New Part 2 CRD runtime tests reported 3 PASS and 6 SKIP, with Part 2 lifecycle mutation still deferred. Existing Part 1 system CRD checks issued system POST/PUT/DELETE under the explicit opt-in.
- Local OSH seedfix: added Procedure/Deployment `properties.featureType` to `ops/local-osh-seed-fixtures.json` and the live records. Direct `GET /sensorhub/api/procedures/040g?f=sml3` and `GET /sensorhub/api/deployments/040g?f=sml3` returned HTTP 200 `application/sml+json`.
- Local OSH seedfix smoke with Basic auth and explicit mutable-IUT opt-in completed with zero failures: `146 total / 62 passed / 0 failed / 84 skipped`; archived at `ops/test-results/sprint-ets-26-seedfix-local-osh-smoke-2026-05-22.xml` and `ops/test-results/sprint-ets-26-seedfix-local-osh-smoke-container-2026-05-22.log`.
- Local OSH seedfix outcome: `procedureSensorMlHasSchemaAndMapping` PASSed; deployment SensorML mapping and non-system SensorML relation-type checks SKIP honestly because OSH-generated SensorML lacks `deployedSystems`/`links` evidence. New Part 2 CRD runtime tests remained 3 PASS and 6 SKIP, with Part 2 lifecycle mutation still deferred.
- Raze seedfix evidence review: `.harness/evaluations/sprint-ets-26-local-osh-seedfix-raze.yaml` returned final `APPROVE_WITH_CONCERNS` confidence 0.94 after closing the stale fixture verification metadata gap.
- Raze local OSH E2E acceptance review: `.harness/evaluations/sprint-ets-26-local-osh-e2e-acceptance-raze.yaml` returned `APPROVE_WITH_CONCERNS` confidence 0.93 with no required fixes.

## Definition of Done
- [x] OpenSpec splits `REQ-ETS-PART2-007` from the remaining placeholders and defines CRD-specific scenarios.
- [x] Story, sprint contract, traceability, epic, ops status, test-results, known issues, and planner handoff are reconciled for planning.
- [x] Planning captures official OGC identifiers, prerequisite, and Requirements 63-78.
- [x] Planning captures current GeoRobotix declaration, prerequisite, broad OPTIONS, and HTTP 400 endpoint state.
- [x] Planning explicitly blocks false PASS from OPTIONS evidence, sibling declarations, public-IUT mutation, and unavailable endpoints.
- [x] Planning E2E smoke ran against the real TeamEngine/GeoRobotix stack and verified zero IUT-bound mutation requests.
- [x] Raze reviews planning before Generator starts: `.harness/evaluations/sprint-ets-26-plan-adversarial.yaml` final verdict `APPROVE` confidence 0.96 after closing `RAZE-ETS26-PLAN-GAP-001`.
- [x] Planning-only change is committed and pushed before Generator implementation: `146c4c6 Plan Sprint 26 Part 2 CRD` (`7d57d9f..146c4c6 main -> main`).
- [x] Generator adds the safety-gated Part 2 Create/Replace/Delete runtime subset and helper/structural regressions.
- [x] Formatter and Maven verification completed after implementation.
- [x] Seeded local OSH E2E smoke completes with zero failures after fixture repair: `146 total / 62 passed / 0 failed / 84 skipped`.
- [x] Raze seedfix evidence review completed: `APPROVE_WITH_CONCERNS` confidence 0.94, no required fixes remaining.
- [x] Raze local OSH E2E acceptance review completed: `APPROVE_WITH_CONCERNS` confidence 0.93, no required fixes remaining.
- [x] Default GeoRobotix failure is documented as advisory external-target evidence, not the Sprint 26 E2E gate.
- [x] Raze implementation review completed for Generator changes; required scoped-endpoint gapfix was applied.
- [x] Focused Raze gapfix recheck completed for the scoped-endpoint fix: `APPROVE_WITH_CONCERNS` confidence 0.94, no required code fixes.

## Out of Scope
- Full closure beyond the first safety-gated Java CRD subset implemented by the Generator.
- Running POST, PUT, DELETE, or PATCH against GeoRobotix.
- Full positive lifecycle coverage for DataStreams, Observations, ControlStreams, Commands, Feasibility, or SystemEvents.
- Part 2 Update, JSON, SWE Common encodings, and observation-binding closure.
