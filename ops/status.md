# Operational Status — OGC API Connected Systems ETS

Last updated: 2026-06-03T18:02Z

## Fresh-Session Entry Point

Start future sessions in:

```bash
cd /home/nh/docker/gir/ets-ogcapi-connectedsystems10
```

Read these first:

- `AGENTS.md`
- `ops/SESSION-HANDOFF-2026-05-05-ETS-REPO-MIGRATION.md`
- `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- `_bmad/traceability.md`
- `.harness/handoffs/planner-handoff.yaml`
- `.harness/contracts/sprint-ets-40.yaml`

## Current State

The active project has moved from the frozen `csapi_compliance` web app repo into this Java/TestNG TeamEngine ETS repo.

Migrated context now lives here:

- `.harness/`
- `openspec/`
- `_bmad/`
- `epics/`
- selected `ops/*.md`
- `AGENTS.md`
- `scripts/orchestrate.py`
- `scripts/session-metrics.py`

Existing ETS evidence in `ops/test-results/` and `ops/server.md` was preserved.

## Current Code State

- ETS HEAD includes pushed Sprint 25 planning commit `2f4a6de Plan Sprint 25 Advanced Filtering`, reconciliation commits `5a8eef4 Reconcile Sprint 25 planning push` and `f251241 Update Sprint 25 planning metrics`, pushed Sprint 25 Generator commit `d9df3ad Implement Sprint 25 Advanced Filtering`, reconciliation commit `af53188 Reconcile Sprint 25 Generator push`, metrics commit `7d57d9f Update Sprint 25 final metrics`, pushed Sprint 26 planning commit `146c4c6 Plan Sprint 26 Part 2 CRD`, pushed reconciliation commit `930cb5c`, pushed Sprint 26 Generator commit `c2d9d1e Implement Sprint 26 Part 2 CRD with local OSH E2E gate`, pushed reconciliation commit `ab9b5f6 Reconcile Sprint 26 generator push`, pushed metrics commit `bf10caa Update Sprint 26 push metrics`, pushed Sprint 27 planning commit `eab12a8 Plan Sprint 27 Part 2 Update`, pushed planning reconciliation `2be355a Reconcile Sprint 27 planning push`, pushed Sprint 27 Generator commit `6ae8f1c Implement Sprint 27 Part 2 Update with local OSH E2E gate`, pushed Sprint 28 planning commit `5d95d55 Plan Sprint 28 Part 2 JSON`, pushed Sprint 28 Generator commit `5850210 Implement Sprint 28 Part 2 JSON`, pushed Sprint 29 planning/Generator/reconciliation commits, pushed Sprint 30 planning/Generator/reconciliation commits, pushed Sprint 31 planning/Generator/reconciliation commits through `7bbbc51 Update Sprint 31 final metrics`, pushed triage commit `eb9fb3a Clarify Sprint 31 next step`, pushed Sprint 32 planning commit `f2d2ab8 Plan Sprint 32 local OSH primary E2E`, pushed Sprint 32 planning reconciliation `1d84ab0 Reconcile Sprint 32 planning push`, pushed Sprint 32 Generator commit `37a2a43 Implement Sprint 32 Observation Command binding`, pushed Sprint 32 final metrics commit `1cd6884`, and pushed Sprint 33 planning commit `efb59e6 Plan Sprint 33 local OSH dynamic data seeding`.
- Latest csapi docs handoff commit before migration: `1568f36`
- Latest pushed story: `S-ETS-40-01` is pushed as `44c6a80 Record Sprint 40 OSH blocker closure`; sibling OSH ConSys implementation is committed locally as `79f89fb Patch ConSys populated binding blockers`.
- Latest pushed Generator work: Sprint 40 partially closes OSH ConSys populated binding blockers while preserving strict ETS assertions. Prior media-type/body/runtime blockers are cleared; populated binding remains open on OSH schema shape/mapping and missing positive Command child item evidence.
- Current handoff story: `S-ETS-40-01` is verified, Raze-reviewed, committed, and pushed as `44c6a80 Record Sprint 40 OSH blocker closure`. Focused OSH regressions passed `23/0/0/0`; ETS Docker Maven wrapper passed `294/0/0/3`; direct SimUAV r4 probes cleared prior blockers with nested Observation `items=1` and nested Command `count=0`; clean local OSH TeamEngine smoke passed `211/61/0/150`; populated SimUAV TeamEngine smoke still failed `211/86/17/108`; Raze focused recheck returned `APPROVE_WITH_CONCERNS` with no required fixes.
- Latest pushed tasking-fixture/evidence commit: `9f8a82f Record local OSH Sprint 37 and 38 evidence`.
- Latest pushed Java implementation/evidence commit: `44c6a80 Record Sprint 40 OSH blocker closure`.
- Push status: remote uses SSH; Sprint 40 blocker closure evidence pushed on 2026-06-03 as `44c6a80` (`8e8f679..44c6a80 main -> main`). Sibling OSH commit `79f89fb` is local only and not pushed to `opensensorhub/osh-core`.

## Session Handoff — Sprint ets-40 OSH ConSys Blocker Closure

Sprint 40 is verified as a partial closure. Do not restart from Sprint 39.

- Story: `epics/stories/s-ets-40-01-osh-consys-populated-binding-blockers.md`.
- Contract: `.harness/contracts/sprint-ets-40.yaml`.
- OSH code changed in sibling repo `/home/nh/docker/gir/osh-core` and committed locally as `79f89fb Patch ConSys populated binding blockers`: ConSys schema format negotiation, exact SWE Text media type handling, `cmdFormat` aliasing, JSON child collection/count bodies, and focused regressions.
- Runtime state: field-hub OSH is restored to the clean primary state with Sapient and SimUAV `autoStart=false`; `field-hub_osh-data` was reset and static `040g` fixtures were reseeded.
- Verification: OSH focused Gradle regressions PASS `23/0/0/0`; ETS Docker Maven wrapper PASS `294/0/0/3`; direct SimUAV format/body probes PASS for the Sprint 40 blocker checks; clean local OSH TeamEngine smoke PASS `211/61/0/150` with zero IUT-bound writes after restoring the tracked field-hub config and reseeding static fixtures.
- Raze review: initial Sprint 40 review returned `GAPS_FOUND`; focused recheck `.harness/evaluations/sprint-ets-40-adversarial-recheck.yaml` returned `APPROVE_WITH_CONCERNS`, confidence `0.91`, with no required fixes.
- Populated E2E status: final SimUAV-populated TeamEngine smoke FAIL `211/86/17/108`. Prior blockers are cleared, but remaining failures are schema shape/mapping issues in Observation/Command schema documents (`commandFormat`, SWE `recordSchema`, `encoding.type`, and `obsFormat`/schema consistency) plus missing positive Command child item evidence.
- Current required action: either patch OSH ConSys schema serialization to emit Annex A.9-compatible Observation/Command schema documents for JSON/SWE formats, or keep populated binding closure open and move to the next partial Part 2 class.

## Session Handoff — Sprint ets-39 Artifact Hygiene / Drift Harness

Sprint 39 is pushed. Do not restart from Sprint 38.

- Story: `epics/stories/s-ets-39-01-artifact-hygiene-drift-harness.md`.
- Contract: `.harness/contracts/sprint-ets-39.yaml`.
- Code changed: `scripts/artifact-hygiene.py` and `scripts/uri-drift-audit.py`.
- Verification: self-tests PASS, Python compile PASS, Docker Maven wrapper PASS `294/0/0/3`, clean local OSH TeamEngine smoke PASS `211/68/0/143`, initial Raze review `APPROVE_WITH_CONCERNS`, and focused Raze recheck `APPROVE` with no required fixes.
- Key artifacts: `ops/test-results/sprint-ets-39-artifact-hygiene-clean-local-osh-2026-06-03.json`, `ops/test-results/sprint-ets-39-uri-schema-drift-audit-2026-06-03.json`, and `ops/test-results/sprint-ets-39-maven-test-via-docker-2026-06-03.txt`.
- Current required action: choose the next work item. Full populated-IUT `part2binding` PASS remains blocked by OSH/fixture evidence issues listed below.

## Session Handoff — Sprint ets-38 SimUAV Preseed Partial

Resume from the current uncommitted worktree. Do not restart from Sprint 37.

- Story: `epics/stories/s-ets-38-01-simuav-preseeded-populated-iut-fixture.md`.
- Contract: `.harness/contracts/sprint-ets-38.yaml`.
- ETS code changed: new `Part2CandidateSelection`, Part 2 JSON/SWE/binding/datastream/controlstream/CRD candidate selection updates, `VerifyPart2CandidateSelection`, and `scripts/local-osh-simuav-preseed.py`.
- Fixture evidence: `ops/test-results/sprint-ets-38-local-osh-simuav-preseed-r3-2026-06-03.json` recorded two SimUAV DataStreams, three ControlStreams, schema endpoints, `GET=102`, `POST=1`, and waypoint feasibility Command POST HTTP 200 `COMPLETED`, without recording credential values. It still reported `PARTIAL_MISSING_OBSERVATION_OR_COMMAND_CHILD_EVIDENCE`.
- Verification archived: Python preseed compile PASS; Docker formatter PASS; cached Docker Maven equivalent `294/0/0/3`; final populated SimUAV TeamEngine smoke failure `211/83/29/99`; post-Raze clean local OSH reset/reseed smoke PASS `211/68/0/143`.
- E2E status at Sprint 38 close: clean primary local OSH was restored and passing, while populated closure was blocked by schema media-type, SWE text, child-body, and command dereference behavior. Superseding Sprint 40 evidence above clears the media-type/SWE text/child-body blockers and leaves schema shape/mapping plus missing positive Command child item evidence.
- Local OSH fixture state: Sapient and SimUAV `autoStart=false`; `field-hub_osh-data` was reset and static `040g` fixtures were reseeded. Local OSH BasicRealm credentials were rotated earlier in the session after session-output exposure; credential values are not recorded.
- Raze review: initial Sprint 38 review found `RAZE-ETS38-GAP-001` and `RAZE-ETS38-GAP-002`; focused recheck `.harness/evaluations/sprint-ets-38-adversarial-recheck.yaml` returned `APPROVE_WITH_CONCERNS` confidence `0.92` with no required fixes.
- Next required action: decide whether to address OSH ConSys child collection/body behavior or defer full populated binding closure as an upstream/local OSH limitation.

## Session Handoff — Sprint ets-36 Pushed

Resume from the current uncommitted worktree. Do not restart from Sprint 35.

- Story: `epics/stories/s-ets-36-01-binding-schema-json-request-shaping.md`.
- Contract: `.harness/contracts/sprint-ets-36.yaml`.
- Handoff: `.harness/handoffs/planner-handoff.yaml`.
- Code changed: `src/main/java/org/opengis/cite/ogcapiconnectedsystems10/conformance/part2/binding/Part2ObservationCommandBindingTests.java`.
- Regression changed: `src/test/java/org/opengis/cite/ogcapiconnectedsystems10/conformance/part2/binding/VerifyPart2ObservationCommandBindingTests.java`.
- Source probe: `ops/test-results/sprint-ets-36-local-osh-schema-request-shaping-source-probe-2026-06-02.txt`.
- Maven evidence: `ops/test-results/sprint-ets-36-focused-maven-2026-06-02.log`, `ops/test-results/sprint-ets-36-maven-clean-test-2026-06-02.log`, and `ops/test-results/sprint-ets-36-maven-clean-test-persistent-cache-2026-06-02.log`.
- E2E evidence: `ops/test-results/sprint-ets-36-clean-local-osh-smoke-2026-06-02.xml`, `ops/test-results/sprint-ets-36-clean-local-osh-container-2026-06-02.log`, and `ops/test-results/sprint-ets-36-clean-local-osh-no-mutation-2026-06-02.txt`.
- Raze review: `.harness/evaluations/sprint-ets-36-adversarial-implementation.yaml` final verdict `APPROVE_WITH_CONCERNS`, confidence `0.93`, no required fixes after OpenSpec reconciliation.
- Next required action: continue `REQ-ETS-PART2-013` only by addressing the remaining populated-IUT blockers: schema-valid stream collection metadata and live populated `?f=json` parent schema evidence, or explicitly scope an isolated fixture mode without weakening Annex A.9 validation.

## Sprint ets-36 Binding Schema Request Shaping

Narrow ETS-side schema media-type request shaping for `REQ-ETS-PART2-013`:

- Story: `epics/stories/s-ets-36-01-binding-schema-json-request-shaping.md`.
- Contract: `.harness/contracts/sprint-ets-36.yaml`.
- Status: `IMPLEMENTED_RAZE_APPROVED_PUSHED`.
- OpenSpec: `REQ-ETS-PART2-013` remains PARTIAL_IMPLEMENTED with `SPRINT_36_SCHEMA_JSON_REQUEST_SHAPING_IMPLEMENTED_WITH_STREAM_METADATA_BLOCKER`.
- Behavior: binding parent schema requests append `f=json` unless `f` or `format` already exists.
- Guardrail: strict `application/json` or `+json` content-type validation remains unchanged; `Content-Type: auto` is not PASS evidence.
- Formatter: PASS after bounded cached rerun; earlier unbounded cold-cache formatter run was stopped as stalled.
- Focused Maven: PASS, `14 tests / 0 failures / 0 errors / 0 skipped`.
- Required wrapper attempt: FAILED before tests due Maven Central `Connection reset` resolving `org.apache.maven.plugins:maven-plugins:pom:43`.
- Full Docker Maven fallback: PASS, `289 tests / 0 failures / 0 errors / 3 skipped`.
- Clean primary TeamEngine smoke: PASS, `211 total / 68 passed / 0 failed / 143 skipped`; no-mutation `GET=133`, `OPTIONS=2`, writes `0`.
- Raze review: initial `GAPS_FOUND` for stale OpenSpec deferred-status wording; focused recheck `APPROVE_WITH_CONCERNS`, confidence `0.93`, no required fixes.
- Commit: `50024c4 Implement Sprint 36 schema request shaping`, pushed `21e76de..50024c4 main -> main`.
- Remaining blocker: local OSH stream collection resources still omit schema-required metadata such as `live`, `async`, and some time ranges, and the clean empty-IUT smoke did not exercise populated live `?f=json` schema requests, so full populated `part2binding` PASS remains open.

## Sprint ets-35 SimUAV Tasking Fixture Evidence

Local OSH `osh-addons` SimUAV tasking fixture for CommandResult evidence:

- Story: `epics/stories/s-ets-35-01-local-osh-simuav-tasking-fixture.md`
- Contract: `.harness/contracts/sprint-ets-35.yaml`
- OpenSpec: `REQ-ETS-PART2-013` remains PARTIAL_IMPLEMENTED with `SPRINT_35_SIMUAV_TASKING_FIXTURE_VERIFIED_WITH_SCHEMA_BLOCKER`.
- Driver selected: SimUAV (`org.sensorhub.impl.sensor.simuav.SimUavDriver`) because it needs no external peer, creates local dynamic streams, exposes tasking ControlStreams, accepts a waypoint feasibility Command, and returns inline result data.
- Field-hub runtime note: SimUAV is configured in `/home/nh/docker/gir/sar-ops/field-hub/osh/config/config.json`, and `sensorhub-driver-simuav-1.0.0-bundle.jar` plus `sensorhub-datamodel-uxs-1.0.0.jar` are present in the field-hub OSH lib. SimUAV `autoStart=false` in the current primary smoke state.
- Isolated command E2E: SimUAV system `02kargmsuc2g` exposed DataStreams `03la3nu3m47g` and `026a5nuan1s0`, ControlStreams `03jtrf5qmkng`, `03lbn3mgpspg`, and `02481pm3g53g`; ControlStream `02481pm3g53g` accepted Command `02481pm3g53g1m6uvj80cc85rppg`; terminal CommandStatus was `COMPLETED` with inline result fields `reachable`, `time_to_waypoint`, and `battery_remaining`.
- Command E2E artifact: `ops/test-results/sprint-ets-35-local-osh-simuav-command-e2e-2026-06-02.json`.
- SimUAV-populated TeamEngine smoke: FAILED, `211 total / 84 passed / 28 failed / 99 skipped`; failures were Part 2 JSON/SWE schema validations against stream collection metadata plus schema endpoint `Content-Type: auto`. No-mutation evidence: `GET=150`, `OPTIONS=14`, writes `0`.
- SimUAV-populated smoke artifacts: `ops/test-results/sprint-ets-35-simuav-local-osh-smoke-failed-2026-06-02.xml`, `ops/test-results/sprint-ets-35-simuav-local-osh-container-failed-2026-06-02.log`, and `ops/test-results/sprint-ets-35-simuav-local-osh-smoke-failed-no-mutation-2026-06-02.txt`.
- Cleanup: SimUAV and Sapient are configured but disabled by default. Cleanup reset only `field-hub_osh-data`, then reseeded static `/systems/040g`, `/procedures/040g`, `/deployments/040g`, and `/samplingFeatures/040g` from `ops/local-osh-seed-fixtures.json`.
- Clean primary TeamEngine smoke: PASS, `211 total / 68 passed / 0 failed / 143 skipped`; no-mutation evidence `GET=133`, `OPTIONS=2`, writes `0`.
- Clean smoke artifacts: `ops/test-results/sprint-ets-35-clean-local-osh-smoke-2026-06-02.xml`, `ops/test-results/sprint-ets-35-clean-local-osh-container-2026-06-02.log`, and `ops/test-results/sprint-ets-35-clean-local-osh-no-mutation-2026-06-02.txt`.
- Raze review: `.harness/evaluations/sprint-ets-35-adversarial-implementation.yaml` records `APPROVE_WITH_CONCERNS`, confidence `0.90`, and no required fixes.
- Next action: do not claim full positive `part2binding` PASS until local OSH emits schema-valid stream collection metadata and JSON schema media types, or the ETS specifies an isolated fixture mode that preserves Annex A.9 validation while accepting body-valid schema evidence.

## Sprint ets-34 Tasking Fixture Evidence

Local OSH tasking driver fixture for acknowledged Command evidence:

- Story: `epics/stories/s-ets-34-01-local-osh-tasking-driver-fixture.md`
- Contract: `.harness/contracts/sprint-ets-34.yaml`
- OpenSpec: `REQ-ETS-PART2-013` remains PARTIAL_IMPLEMENTED with `SPRINT_34_TASKING_DRIVER_FIXTURE_VERIFIED_WITH_CONSTRAINTS`.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-06-01 during Sprint 32 planning; checked 2026-06-02 and not stale.
- Driver selected: Sapient (`sensorhub-driver-sapient-0.1.0.jar`) because it has a local OSH module, local SAPIENT TCP peer, LOOK_AT tasking support, and TaskAck-to-CommandStatus behavior.
- Field-hub runtime note: Sapient is configured in `/home/nh/docker/gir/sar-ops/field-hub/osh/config/config.json`, the jar is present in the field-hub OSH lib, TCP `12000` is mapped, and protobuf conflicts were moved aside so runtime protobuf `4.31.1` is active. Sapient `autoStart=false` in the current primary smoke state.
- Isolated command E2E: local SAPIENT node `eeee3401-0000-0000-0000-000000000034` registered; ControlStream `02nc9lm4r9p0` accepted Command `02nc9lm4r9p01sv2vf80cdtdkmf0`; protocol Task `f3ee6568d2fe4fc79ab57efc8b` was acknowledged; terminal CommandStatus was `COMPLETED`.
- Command E2E artifact: `ops/test-results/sprint-ets-34-local-osh-sapient-command-e2e-2026-06-02.json`.
- Sapient-populated TeamEngine smoke: FAILED, `211 total / 82 passed / 28 failed / 101 skipped`; failures were Part 2 JSON/SWE schema validations against Sapient DataStream/ControlStream resources. No-mutation evidence: `GET=149`, `OPTIONS=14`, writes `0`.
- Sapient-populated smoke artifacts: `ops/test-results/sprint-ets-34-local-osh-smoke-failed-2026-06-02.xml`, `ops/test-results/sprint-ets-34-local-osh-container-failed-2026-06-02.log`, and `ops/test-results/sprint-ets-34-local-osh-smoke-failed-no-mutation-2026-06-02.txt`.
- Cleanup: OSH did not delete module-owned Sapient streams/commands through CS API; cleanup reset only `field-hub_osh-data`, then reseeded static `/systems/040g`, `/procedures/040g`, `/deployments/040g`, and `/samplingFeatures/040g` from `ops/local-osh-seed-fixtures.json`.
- Clean primary TeamEngine smoke: PASS, `211 total / 68 passed / 0 failed / 143 skipped`; no-mutation evidence `GET=133`, `OPTIONS=2`, writes `0`.
- Clean smoke artifacts: `ops/test-results/sprint-ets-34-clean-local-osh-smoke-2026-06-02.xml`, `ops/test-results/sprint-ets-34-clean-local-osh-container-2026-06-02.log`, and `ops/test-results/sprint-ets-34-clean-local-osh-no-mutation-2026-06-02.txt`.
- Maven verification: Docker Maven `clean test` passed `288 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-34-maven-clean-test-2026-06-02.log`. An earlier unbounded wrapper attempt was stopped after the JVM blocked in an HTTPS dependency download before `clean`; the archived bounded rerun completed successfully.
- Next action: do not claim full positive `part2binding` PASS until the tasking fixture also yields schema-valid dynamic stream resources and inspectable JSON parent schema endpoints, or the ETS gains a documented isolated fixture mode that avoids contaminating the primary read-only schema suites.

## Sprint ets-33 Planning Evidence

Local OSH dynamic-data seed fixtures and inline status/result helper regressions:

- Story: `epics/stories/s-ets-33-01-local-osh-dynamic-data-seed-fixtures-planning.md`
- Contract: `.harness/contracts/sprint-ets-33.yaml`
- OpenSpec: `REQ-ETS-PART2-013` remains PARTIAL_IMPLEMENTED from Sprint 32, with Sprint 33 scenarios for local OSH dynamic-data seed fixtures, mutation safety, inline CommandStatus/CommandResult helper regressions, and positive local OSH closure.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-06-01 during Sprint 32 planning; checked 2026-06-02 and not stale.
- Local OSH dynamic-data probe artifact: `ops/test-results/sprint-ets-33-plan-local-osh-dynamic-data-probes-2026-06-02.txt`.
- Local OSH current seed state: existing System `/systems/040g`; `/datastreams`, `/observations`, `/controlstreams`, `/systems/040g/datastreams`, and `/systems/040g/controlstreams` return HTTP 200 JSON with empty `items`; `/commands` returns HTTP 400.
- Official Part 2 OpenAPI probe: relevant POST paths are `/systems/{systemId}/datastreams`, `/datastreams/{dataStreamId}/observations`, `/systems/{systemId}/controlstreams`, `/controlstreams/{controlStreamId}/commands`, `/commands/{cmdId}/status`, and `/commands/{cmdId}/result`.
- Planned manifest: `ops/local-osh-dynamic-data-seed-fixtures.json`, status `PLANNED_NOT_APPLIED`.
- Safety policy: Generator must not seed unless explicit dedicated mutable-IUT opt-in is present; GeoRobotix remains advisory-only and must not be mutated.
- Helper regression scope: add focused coverage for inline `status`, `commandStatus`, `result`, and `commandResult` missing/non-object/no-overlap/missing-required/type-mismatch behavior before populated-IUT closure is reported.
- Planning TeamEngine E2E: `SMOKE_AUTH_CREDENTIAL=<derived, not logged> SMOKE_CONTAINER_NAME=ets-csapi-s33-plan-local-osh SMOKE_TARGET=local-osh SMOKE_DOCKER_NETWORK=field-hub_default SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api SMOKE_OUTPUT_DIR=/tmp/sprint-ets-33-plan-local-osh-2026-06-02-results bash scripts/smoke-test.sh` passed `211 total / 68 passed / 0 failed / 143 skipped`.
- Planning E2E artifacts: `ops/test-results/sprint-ets-33-plan-local-osh-smoke-2026-06-02.xml`, `ops/test-results/sprint-ets-33-plan-local-osh-container-2026-06-02.log`, and `ops/test-results/sprint-ets-33-plan-local-osh-no-mutation-2026-06-02.txt`.
- No-mutation evidence: `recognized_iut_request_logs=135`, `GET=133`, `OPTIONS=2`, `POST=0`, `PUT=0`, `PATCH=0`, and `DELETE=0`.
- Raze planning review: `.harness/evaluations/sprint-ets-33-plan-adversarial.yaml` final verdict `APPROVE`, confidence 0.94. Required fixes `RAZE-ETS33-PLAN-GAP-001` and `RAZE-ETS33-PLAN-GAP-002` are closed; manifest concern `RAZE-ETS33-PLAN-CONCERN-001` is closed.
- Next action: start Generator from `.harness/contracts/sprint-ets-33.yaml`.

## Sprint ets-33 Generator Partial Discovery

Local OSH dynamic-data seed fixtures and inline status/result helper regressions:

- Code: `Part2ObservationCommandBindingTests.assertAvailableCommandInlineDataMatchesParentSchema` is package-visible for regression tests; runtime behavior is unchanged.
- Tests: `VerifyPart2ObservationCommandBindingTests` now covers missing inline status/result members, non-object SKIP, no-overlap failure, missing required field failure, primitive type mismatch failure, and direct `result` alias mismatch behavior for Command inline status/result evidence.
- Focused Maven: Docker Maven `mvn -B test -Dtest=VerifyPart2ObservationCommandBindingTests` passed `13 total / 0 failures / 0 errors / 0 skipped`.
- Full Maven: Docker Maven `mvn -B test` passed `288 total / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-33-generator-maven-2026-06-02.log`.
- Mutation-gated discovery artifacts:
  - `ops/test-results/sprint-ets-33-generator-local-osh-observation-seed-probe-2026-06-02.json`
  - `ops/test-results/sprint-ets-33-generator-local-osh-command-timeout-probe-2026-06-02.json`
  - `ops/test-results/sprint-ets-33-generator-local-osh-seed-shape-probe-2026-06-02.json`
  - `ops/test-results/sprint-ets-33-generator-local-osh-datastream-schema-variants-2026-06-02.json`
- Accepted Observation seed shape: `POST /systems/040g/datastreams` with `schema.obsFormat=application/om+json` and SWE DataRecord `resultSchema`, followed by `POST /datastreams/{id}/observations` with `Content-Type: application/om+json` and `result.temperature`; both created with HTTP 201 and cleaned up with HTTP 204.
- Accepted ControlStream seed shape: `POST /systems/040g/controlstreams` with `commandFormat=application/json` and SWE DataRecord `parametersSchema`; created with HTTP 201, schema was retrievable, and cleanup returned HTTP 204.
- Blocker: `POST /controlstreams/{id}/commands` timed out waiting for OSH command acknowledgement; nested Commands remained empty. CommandStatus/CommandResult fixture evidence cannot be claimed.
- Mandatory local OSH TeamEngine Generator smoke: authenticated `SMOKE_TARGET=local-osh` on `field-hub_default` rerun after Raze concern reconciliation passed `211 total / 68 passed / 0 failed / 143 skipped`.
- E2E artifacts: `ops/test-results/sprint-ets-33-generator-local-osh-smoke-2026-06-02.xml`, `ops/test-results/sprint-ets-33-generator-local-osh-container-2026-06-02.log`, `ops/test-results/sprint-ets-33-generator-local-osh-smoke-console-2026-06-02.log`, and `ops/test-results/sprint-ets-33-generator-local-osh-no-mutation-2026-06-02.txt`.
- No-mutation evidence: `recognized_iut_request_logs=135`, `GET=133`, `OPTIONS=2`, `POST=0`, `PUT=0`, `PATCH=0`, and `DELETE=0`.
- Raze implementation review: initial `APPROVE_WITH_CONCERNS`, no required fixes. Concerns were reconciled by adding direct `result` alias mismatch coverage and surfacing the current full-Maven evidence in OpenSpec; focused recheck returned `PASS`.
- Next action: continue `REQ-ETS-PART2-013` only if local OSH exposes an acknowledged Command resource or a documented no-ack Command creation path; otherwise keep full populated-IUT binding closure open.

## Sprint ets-32 Planning Evidence

Local OSH primary E2E and Observation/Command binding closure:

- Story: `epics/stories/s-ets-32-01-part2-observation-binding-local-osh-planning.md`
- Contract: `.harness/contracts/sprint-ets-32.yaml`
- OpenSpec: `REQ-ETS-PART2-013` is PARTIAL_IMPLEMENTED as an internal Observation/Command binding cross-class closure; `REQ-ETS-TEAMENGINE-006` is SPECIFIED_PLANNED for local OSH as the primary development E2E target.
- Scope planned: local OSH is the default sprint development IUT on Docker network `field-hub_default`; GeoRobotix public runs are advisory interoperability probes only.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-06-01 during Sprint 32 planning; not stale.
- OGC source verification: official OGC 23-002 HTML `https://docs.ogc.org/is/23-002/23-002.html` lists Part 2 classes but does not define `/conf/observation-binding`. Observation/Command binding remains an internal project closure derived from DataStream/Observation and ControlStream/Command schema requirements.
- Local OSH probe artifact: `ops/test-results/sprint-ets-32-plan-local-osh-probes-2026-06-01.txt`.
- Local OSH probe: `/conformance` returned HTTP 200 and declares `/conf/datastream`, `/conf/controlstream`, `/conf/json`, `/conf/swecommon-json`, `/conf/swecommon-text`, `/conf/swecommon-binary`, `/conf/create-replace-delete`, and `/conf/system-event`. It also declares non-standard `/conf/system-history`, which remains vendor evidence only.
- Local OSH seed state: `/datastreams?limit=1`, `/observations?limit=1`, and `/controlstreams?limit=1` returned HTTP 200 JSON with empty `items`; `/commands?limit=1` and `/systemEvents?limit=1` returned HTTP 400 JSON.
- Planning TeamEngine E2E: `SMOKE_AUTH_CREDENTIAL=<derived, not logged> SMOKE_CONTAINER_NAME=ets-csapi-s32-plan-local-osh SMOKE_TARGET=local-osh SMOKE_DOCKER_NETWORK=field-hub_default SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api SMOKE_OUTPUT_DIR=/tmp/sprint-ets-32-local-osh-plan-2026-06-01-results bash scripts/smoke-test.sh` passed `206 total / 65 passed / 0 failed / 141 skipped`.
- Planning E2E artifacts: `ops/test-results/sprint-ets-32-plan-local-osh-smoke-2026-06-01.xml`, `ops/test-results/sprint-ets-32-plan-local-osh-container-2026-06-01.log`, and `ops/test-results/sprint-ets-32-plan-local-osh-no-mutation-2026-06-01.txt`.
- No-mutation evidence: `recognized_iut_request_logs=132`, `GET=130`, `OPTIONS=2`, `POST=0`, `PUT=0`, `PATCH=0`, and `DELETE=0`.
- Generator guardrail: positive binding PASS requires parent schema evidence plus associated child Observation/Command-side resource evidence with concrete field/type mapping. Empty local OSH collections, declarations alone, sibling encodings, hardcoded examples, broad format lists, OPTIONS-only evidence, or any fabricated `/conf/observation-binding` class must not PASS.
- Raze planning review: `.harness/evaluations/sprint-ets-32-plan-adversarial.yaml` final verdict `APPROVE`, confidence 0.94. Required fixes `RAZE-ETS32-PLAN-GAP-001` and `RAZE-ETS32-PLAN-GAP-002` are closed.
- Next action: choose the next spec-first item. The most direct follow-up is documented local OSH dynamic-data seed fixtures plus helper regressions for inline CommandStatus/CommandResult closure, or the next unimplemented Part 2 coverage item if priority shifts.

## Sprint ets-32 Generator Evidence

Observation/Command binding cross-class closure with local OSH primary E2E:

- Implementation: `Part2ObservationCommandBindingTests` adds TestNG group `part2binding` for internal `REQ-ETS-PART2-013`; the group depends on `core common` only.
- Guardrails: no standalone OGC `/conf/observation-binding` class is required or advertised; declarations, empty collections, sibling encodings, format lists, hardcoded examples, and OPTIONS-only evidence cannot PASS the positive closure checks.
- Runtime outcome on current local OSH: `/conf/json`/read-only guard PASSes; no-standard-conformance-class and read-only self-documenting checks PASS; Observation/Command positive checks SKIP because local OSH currently has no candidate DataStream/Observation/ControlStream/Command evidence.
- Formatter: Docker Maven `mvn -B spring-javaformat:apply` returned BUILD SUCCESS.
- Focused Maven: Docker Maven with host networking and persistent `/tmp` cache returned BUILD SUCCESS, `83 tests / 0 failures / 0 errors / 0 skipped`.
- Full Maven: Docker Maven with host networking and persistent `/tmp` cache returned BUILD SUCCESS, `282 tests / 0 failures / 0 errors / 3 skipped`; post-gapfix log archived at `ops/test-results/sprint-ets-32-maven-postraze-2026-06-02.log`.
- Mandatory local OSH TeamEngine Generator smoke: authenticated `SMOKE_TARGET=local-osh` on `field-hub_default` passed `211 total / 68 passed / 0 failed / 143 skipped` after Raze gapfixes.
- Generator E2E artifacts: `ops/test-results/sprint-ets-32-generator-local-osh-smoke-postraze-2026-06-02.xml`, `ops/test-results/sprint-ets-32-generator-local-osh-container-postraze-2026-06-02.log`, and `ops/test-results/sprint-ets-32-generator-local-osh-no-mutation-postraze-2026-06-02.txt`.
- Generator local OSH safety: `scripts/no-mutation-oracle.py` recognized 135 IUT request logs; explicit counts found `GET=133`, `OPTIONS=2`, and zero `POST`, `PUT`, `PATCH`, or `DELETE`.
- Raze implementation review: `.harness/evaluations/sprint-ets-32-adversarial-implementation.yaml` records final `APPROVE_WITH_CONCERNS` confidence 0.91 after required gapfixes. No required fixes remain.

## Sprint ets-30 Generator Evidence

Part 2 SWE Common Text Encoding declaration-gated read-only subset:

- Story: `epics/stories/s-ets-30-01-part2-swecommon-text-planning.md`
- Contract: `.harness/contracts/sprint-ets-30.yaml`
- OpenSpec: `REQ-ETS-PART2-011` is PARTIAL_IMPLEMENTED for OGC 23-002 Clause 16.3; remaining Part 2 placeholders are `REQ-ETS-PART2-012..013`.
- Scope implemented: first declaration-gated, read-only Part 2 SWE Common Text Encoding subset using official `/req/swecommon-text` and `/conf/swecommon-text` identifiers.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-05-09; checked 2026-05-26 and not stale.
- OGC source verification: official OGC 23-002 HTML `https://docs.ogc.org/is/23-002/23-002.html`, Clause 16.3 "Requirements Class SWE Common Text Encoding" and Annex A.11.
- Normative requirement set: Requirements 115-122 cover SWE Common Text mediatype-read, mediatype-write, Observation Schema schema/mapping, Observation text encoding, Command Schema schema/mapping, and Command text encoding.
- Prerequisite: SWE Common 3.0 Text Encoding Rules (`/req/text-encoding-rules`; conformance prerequisite `/conf/text-encoding-rules`).
- Media type: exact `application/swe+text`; `application/swe+csv`, `application/swe+binary`, `application/swe+json`, `application/json`, and vendor media strings are not PASS evidence.
- Source inconsistency captured: Annex A.115's API-definition bullet mentions `application/swe+binary`, but Clause 16.3 and the retrieval/content-type steps use `application/swe+text`; Generator must document the inconsistency and not pass SWE Common Text from binary advertisement.
- Implementation: `Part2SweCommonTextTests` adds exact `/conf/swecommon-text` declaration, SWE 3.0 `/conf/text-encoding-rules` prerequisite visibility, `/conf/datastream`/`/conf/controlstream`/`/conf/create-replace-delete` resource condition gates, read-only `application/swe+text` media checks, bundled `observationSchemaSwe.json` and `commandSchemaSwe.json` metadata validation, canonical Time/IssueTime definition evidence guards, Observation/Command encoding guards, and non-mutating mediatype-write API-definition checks scoped to Observation/Command resource endpoints.
- Structural coverage: `VerifyPart2SweCommonTextTests` adds 11 helper regressions, `VerifyTestNGSuiteDependency` adds `part2swecommontext` dependency/method/co-location lint, and `testng.xml` wires `part2swecommontext` with `core common`.
- GeoRobotix planning probe raw transcript: `ops/test-results/sprint-ets-30-plan-georobotix-swetext-probes-2026-05-26.txt`.
- GeoRobotix planning probe: `/conformance` declares Part 2 `/conf/swecommon-text`, `/conf/swecommon-json`, `/conf/swecommon-binary`, `/conf/datastream`, `/conf/controlstream`, `/conf/create-replace-delete`, and `/conf/json`, but not SWE 3.0 `/conf/text-encoding-rules`, `/conf/json-encoding-rules`, or `/conf/binary-encoding-rules`, Part 2 `/conf/api-common`, `/conf/update`, or `/conf/advanced-filtering`.
- GeoRobotix read-health probes on 2026-05-26: `GET /datastreams?limit=1` with `Accept: application/json` returned HTTP 500 `application/json`; `GET /datastreams?limit=1` and `GET /observations?limit=1` with `Accept: application/swe+text` returned HTTP 500.
- GeoRobotix command-side evidence: `GET /controlstreams?limit=1` returned HTTP 200 `application/json`, first ID `0m4qpft9sdag`, with formats including `application/swe+csv` but not `application/swe+text`; `GET /controlstreams/0m4qpft9sdag/schema?cmdFormat=application/swe+text` returned HTTP 200 but reported `commandFormat=application/json` and `parametersSchema`, not `application/swe+text`, `recordSchema`, and `TextEncoding`.
- Candidate-resource limits: `GET /controlstreams/0m4qpft9sdag/commands?limit=1` with `Accept: application/swe+text` returned HTTP 200 `application/json` with empty `items`; `GET /commands?limit=1` with `Accept: application/swe+text` returned HTTP 400.
- Local OSH planning limit: `field-hub-osh-1` is running but unhealthy, current shell has no `SMOKE_AUTH_CREDENTIAL`, and unauthenticated `/sensorhub/api/conformance` returned HTTP 401.
- Planning E2E smoke: `SMOKE_CONTAINER_NAME=ets-csapi-s30-swetext-plan-georobotix SMOKE_OUTPUT_DIR=/tmp/sprint-ets-30-swetext-plan-georobotix-results bash scripts/smoke-test.sh` failed `186 total / 31 passed / 22 failed / 133 skipped`.
- E2E artifacts: `ops/test-results/sprint-ets-30-plan-georobotix-smoke-failed-2026-05-26.xml` and `ops/test-results/sprint-ets-30-plan-georobotix-smoke-container-failed-2026-05-26.log`.
- Public-IUT safety: `scripts/no-mutation-oracle.py` recognized 83 IUT request logs; explicit container-log grep found 83 GeoRobotix GET request lines and zero matched GeoRobotix POST/PUT/PATCH/DELETE request lines.
- Raze planning review: `.harness/evaluations/sprint-ets-30-plan-adversarial.yaml` returned `APPROVE_WITH_CONCERNS` confidence 0.93 with no required fixes. Low concern was bookkeeping-only and closed by post-review reconciliation.
- Formatter: Docker Maven `mvn -B spring-javaformat:apply` returned BUILD SUCCESS.
- Focused Maven: Docker Maven `mvn -B test -Dtest=VerifyPart2SweCommonTextTests,VerifyTestNGSuiteDependency` returned `81 tests / 0 failures / 0 errors / 0 skipped`.
- Full Maven: Docker Maven `mvn -B clean test` returned BUILD SUCCESS, `258 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-30-maven-2026-05-27.log`.
- Mandatory GeoRobotix TeamEngine Generator smoke: `SMOKE_CONTAINER_NAME=ets-csapi-s30-swetext-generator-georobotix SMOKE_OUTPUT_DIR=/tmp/sprint-ets-30-swetext-generator-georobotix-results bash scripts/smoke-test.sh` failed `196 total / 33 passed / 28 failed / 135 skipped`.
- Generator E2E artifacts: `ops/test-results/sprint-ets-30-generator-georobotix-smoke-failed-2026-05-27.xml`, `ops/test-results/sprint-ets-30-generator-georobotix-smoke-container-failed-2026-05-27.log`, and `ops/test-results/sprint-ets-30-generator-georobotix-smoke-console-failed-2026-05-27.log`.
- New SWE Common Text group outcome: 2 PASS (`/conf/swecommon-text` declaration and condition gates), 6 FAIL (Observation-side HTTP 500 and Command-side `/controlstreams` schema validation), and 2 SKIP (missing SWE 3.0 `/conf/text-encoding-rules` prerequisite and no parseable API-definition exact `application/swe+text` write advertisement).
- Generator public-IUT safety: `scripts/no-mutation-oracle.py` recognized 91 IUT request logs, and explicit counts found 91 GeoRobotix GET request lines and zero matched GeoRobotix POST/PUT/PATCH/DELETE request lines.
- Raze implementation review: initial `GAPS_FOUND` confidence 0.88 for missing test-code traceability comments on two critical scenarios. Gapfix added explicit comments for `SCENARIO-ETS-PART2-011-ANNEX-MEDIATYPE-HONESTY-001` and `SCENARIO-ETS-PART2-011-UNAVAILABLE-ENDPOINT-HONESTY-001`, reran formatter, focused Maven `81/0/0/0`, and full Maven `258/0/0/3`. Focused Raze recheck returned `APPROVE_WITH_CONCERNS` confidence 0.93 with no required fixes.
- Commit/push: Sprint 30 planning pushed as `3c68858 Plan Sprint 30 Part 2 SWE Common Text`; Sprint 30 Generator pushed as `b2aad06 Implement Sprint 30 Part 2 SWE Common Text` (`560945c..b2aad06 main -> main`).
- Next action completed by Sprint ets-31 planning; continue from Sprint ets-31 status below.

## Sprint ets-31 Planning Evidence

Part 2 SWE Common Binary Encoding declaration-gated read-only subset:

- Story: `epics/stories/s-ets-31-01-part2-swecommon-binary-planning.md`
- Contract: `.harness/contracts/sprint-ets-31.yaml`
- OpenSpec: `REQ-ETS-PART2-012` is SPECIFIED for OGC 23-002 Clause 16.4; remaining Part 2 observation-binding placeholder is `REQ-ETS-PART2-013`.
- Scope planned: first declaration-gated, read-only Part 2 SWE Common Binary Encoding subset using official `/req/swecommon-binary` and `/conf/swecommon-binary` identifiers.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-05-09; checked 2026-05-27 and not stale.
- OGC source verification: official OGC 23-002 HTML `https://docs.ogc.org/is/23-002/23-002.html`, Clause 16.4 "Requirements Class SWE Common Binary Encoding" and Annex A.12.
- Normative requirement set: Requirements 123-130 cover SWE Common Binary mediatype-read, mediatype-write, Observation Schema schema/mapping, Observation binary encoding, Command Schema schema/mapping, and Command binary encoding.
- Prerequisite: SWE Common 3.0 Binary Encoding Rules (`/req/binary-encoding-rules`; conformance prerequisite `/conf/binary-encoding-rules`).
- Media type: exact `application/swe+binary`; `application/vnd.ogc.swe+binary`, `application/swe+text`, `application/swe+csv`, `application/swe+json`, `application/json`, empty bytes, and vendor media strings are not PASS evidence.
- Source inconsistencies captured: Clause 16.4's media-type note still says "SWE Common Text encoding" while discussing binary media, and Annex A.127/A.130 mention Text encoding rules for binary Observation/Command validation. Generator must document these inconsistencies and require BinaryEncoding/Binary rules evidence.
- GeoRobotix planning probe raw transcript: `ops/test-results/sprint-ets-31-plan-georobotix-swebinary-probes-2026-05-27.txt`.
- GeoRobotix planning probe: `/conformance` declares Part 2 `/conf/swecommon-binary`, `/conf/swecommon-text`, `/conf/swecommon-json`, `/conf/datastream`, `/conf/controlstream`, `/conf/create-replace-delete`, and `/conf/json`, but not SWE 3.0 `/conf/binary-encoding-rules`, `/conf/text-encoding-rules`, or `/conf/json-encoding-rules`, Part 2 `/conf/api-common`, `/conf/update`, or `/conf/advanced-filtering`.
- GeoRobotix read-health probes on 2026-05-27: `GET /datastreams?limit=1` with `Accept: application/json` returned HTTP 500 `application/json`; `GET /datastreams?limit=1` and `GET /observations?limit=1` with `Accept: application/swe+binary` returned HTTP 500 with no body.
- GeoRobotix command-side evidence: `GET /controlstreams?limit=1` returned HTTP 200 `application/json`, first ID `0m4qpft9sdag`, with formats including `application/swe+binary`; `GET /controlstreams/0m4qpft9sdag/schema?cmdFormat=application/swe+binary` returned HTTP 200 but reported `commandFormat=application/json` and `parametersSchema`, not `application/swe+binary`, `recordSchema`, and `BinaryEncoding`.
- Candidate-resource limits: `GET /controlstreams/0m4qpft9sdag/commands?limit=1` with `Accept: application/swe+binary` returned HTTP 200 `application/json` with empty `items`; `GET /commands?limit=1` with `Accept: application/swe+binary` returned HTTP 400.
- Planning E2E smoke: `SMOKE_CONTAINER_NAME=ets-csapi-s31-swebinary-plan-georobotix SMOKE_OUTPUT_DIR=/tmp/sprint-ets-31-swebinary-plan-georobotix-results bash scripts/smoke-test.sh` failed `196 total / 33 passed / 28 failed / 135 skipped`.
- E2E artifacts: `ops/test-results/sprint-ets-31-plan-georobotix-smoke-failed-2026-05-27.xml`, `ops/test-results/sprint-ets-31-plan-georobotix-smoke-container-failed-2026-05-27.log`, and `ops/test-results/sprint-ets-31-plan-georobotix-smoke-console-failed-2026-05-27.log`.
- Public-IUT safety: `scripts/no-mutation-oracle.py` recognized 91 IUT request logs; explicit counts archived at `ops/test-results/sprint-ets-31-plan-georobotix-no-mutation-2026-05-27.txt` show `GET=91`, `POST=0`, `PUT=0`, `PATCH=0`, and `DELETE=0`.
- Raze planning review: `.harness/evaluations/sprint-ets-31-plan-adversarial.yaml` returned `APPROVE_WITH_CONCERNS` confidence 0.93. Required low bookkeeping fix `RAZE-ETS31-PLAN-CONCERN-001` was closed by post-review reconciliation.
- Commit/push: Sprint 31 planning pushed as `20a8e18 Plan Sprint 31 Part 2 SWE Common Binary` (`45717a3..20a8e18 main -> main`).
- Next action completed: Sprint 31 Generator implementation, mandatory verification, Raze implementation review, and push are complete.

## Sprint ets-31 Generator Evidence

Part 2 SWE Common Binary Encoding declaration-gated read-only subset:

- Story: `epics/stories/s-ets-31-01-part2-swecommon-binary-planning.md`
- Contract: `.harness/contracts/sprint-ets-31.yaml`
- OpenSpec: `REQ-ETS-PART2-012` is PARTIAL_IMPLEMENTED for OGC 23-002 Clause 16.4; remaining Part 2 observation-binding placeholder is `REQ-ETS-PART2-013`.
- Scope implemented: first declaration-gated, read-only Part 2 SWE Common Binary Encoding subset using official `/req/swecommon-binary` and `/conf/swecommon-binary` identifiers.
- Implementation: `Part2SweCommonBinaryTests` adds exact `/conf/swecommon-binary` declaration, SWE 3.0 `/conf/binary-encoding-rules` prerequisite visibility, `/conf/datastream`/`/conf/controlstream`/`/conf/create-replace-delete` resource condition gates, read-only `application/swe+binary` media checks, bundled `observationSchemaSwe.json` and `commandSchemaSwe.json` metadata validation, canonical Time/IssueTime definition guards, Observation/Command encoding guards, and non-mutating mediatype-write API-definition checks scoped to Observation/Command resource endpoints.
- Structural coverage: `VerifyPart2SweCommonBinaryTests` adds 11 helper regressions; `VerifyTestNGSuiteDependency` adds `part2swecommonbinary` dependency/method/co-location lint; `testng.xml` wires `part2swecommonbinary` with `core common`.
- Formatter: Docker Maven `mvn -B spring-javaformat:apply` returned BUILD SUCCESS.
- Focused Maven: Docker Maven `mvn -B test -Dtest=VerifyPart2SweCommonBinaryTests,VerifyTestNGSuiteDependency` returned `84 tests / 0 failures / 0 errors / 0 skipped`; log archived at `ops/test-results/sprint-ets-31-focused-maven-2026-05-27.log`.
- Full Maven: `bash scripts/mvn-test-via-docker.sh` returned BUILD SUCCESS, `272 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-31-full-maven-2026-05-27.log`.
- Mandatory GeoRobotix TeamEngine Generator smoke: `SMOKE_CONTAINER_NAME=ets-csapi-s31-swebinary-generator-georobotix SMOKE_OUTPUT_DIR=/tmp/sprint-ets-31-swebinary-generator-georobotix-results bash scripts/smoke-test.sh` failed `206 total / 35 passed / 34 failed / 137 skipped`.
- Generator E2E artifacts: `ops/test-results/sprint-ets-31-generator-georobotix-smoke-failed-2026-05-27.xml`, `ops/test-results/sprint-ets-31-generator-georobotix-smoke-container-failed-2026-05-27.log`, `ops/test-results/sprint-ets-31-generator-georobotix-smoke-console-failed-2026-05-27.log`, and `ops/test-results/sprint-ets-31-generator-georobotix-no-mutation-2026-05-27.txt`.
- New SWE Common Binary group outcome: 3 PASS, 6 FAIL, and 2 SKIP. Failures match GeoRobotix's missing SWE Binary prerequisite, Observation-side HTTP 500 state, and reachable ControlStream schema validation failure before Command SWE Binary evidence.
- Generator public-IUT safety: `scripts/no-mutation-oracle.py` recognized 99 IUT request logs, and explicit counts found 99 GeoRobotix GET request lines and zero matched GeoRobotix POST/PUT/PATCH/DELETE request lines.
- Raze implementation review: `.harness/evaluations/sprint-ets-31-adversarial-implementation.yaml` returned `APPROVE_WITH_CONCERNS` confidence 0.91. Required bookkeeping fix `RAZE-ETS31-IMPL-CONCERN-001` was closed by post-review reconciliation; no code-level required fixes were found.
- Commit/push: Sprint 31 Generator pushed as `b520b65 Implement Sprint 31 Part 2 SWE Common Binary` (`75eac75..b520b65 main -> main`).

## Sprint ets-29 Generator Evidence

Part 2 SWE Common JSON Encoding declaration-gated read-only subset:

- Story: `epics/stories/s-ets-29-01-part2-swecommon-json-planning.md`
- Contract: `.harness/contracts/sprint-ets-29.yaml`
- OpenSpec: `REQ-ETS-PART2-010` is PARTIAL_IMPLEMENTED for OGC 23-002 Clause 16.2. At Sprint 29 close, remaining Part 2 placeholders were `REQ-ETS-PART2-011..013`; Sprint 30 has since split out `REQ-ETS-PART2-011`.
- Scope implemented: first declaration-gated, read-only Part 2 SWE Common JSON Encoding subset using official `/req/swecommon-json` and `/conf/swecommon-json` identifiers.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-05-09; checked 2026-05-26 and not stale.
- OGC source verification: official OGC 23-002 HTML `https://docs.ogc.org/is/23-002/23-002.html`, Clause 16.2 "Requirements Class SWE Common JSON Encoding" and Annex A.10.
- Normative requirement set: Requirements 107-114 cover SWE Common JSON mediatype-read, mediatype-write, Observation Schema schema/mapping, Observation encoding, Command Schema schema/mapping, and Command encoding.
- Prerequisite: SWE Common 3.0 JSON Encoding Rules (`/req/json-encoding-rules`; conformance prerequisite `/conf/json-encoding-rules`).
- Media type: exact `application/swe+json`; the preliminary `application/vnd.ogc.swe+json` note is not used as PASS evidence.
- Implementation: `Part2SweCommonJsonTests` adds exact `/conf/swecommon-json` declaration, SWE 3.0 `/conf/json-encoding-rules` prerequisite visibility, `/conf/datastream`/`/conf/controlstream`/`/conf/create-replace-delete` resource condition gates, read-only `application/swe+json` media checks, bundled `observationSchemaSwe.json` and `commandSchemaSwe.json` validation, canonical Time/IssueTime definition evidence guards, Observation/Command encoding evidence guards, and non-mutating mediatype-write API-definition checks scoped to Observation/Command resource endpoints.
- Structural coverage: `VerifyPart2SweCommonJsonTests` adds 11 helper regressions, including rejection of noncanonical/missing Time definitions and unrelated/subresource write paths; `VerifyTestNGSuiteDependency` adds `part2swecommonjson` dependency/method/co-location lint, and `testng.xml` wires `part2swecommonjson` with `core common`.
- GeoRobotix planning probe: `/conformance` declares Part 2 `/conf/swecommon-json`, `/conf/swecommon-text`, `/conf/swecommon-binary`, `/conf/datastream`, `/conf/controlstream`, `/conf/create-replace-delete`, and `/conf/json`, but not SWE 3.0 `/conf/json-encoding-rules`, Part 2 `/conf/api-common`, `/conf/update`, or `/conf/advanced-filtering`.
- GeoRobotix read-health probes on 2026-05-26: `GET /datastreams?limit=1` with `Accept: application/json`, `GET /datastreams?limit=1` with `Accept: application/swe+json`, and `GET /observations?limit=1` with `Accept: application/swe+json` returned HTTP 500 `application/json`.
- GeoRobotix command-side evidence: `GET /controlstreams?limit=1` returned HTTP 200 `application/json`, first ID `0m4qpft9sdag`, with formats including `application/swe+json`; `GET /controlstreams/0m4qpft9sdag/schema?cmdFormat=application/swe+json` returned HTTP 200 but reported `commandFormat=application/json` and `parametersSchema`, not `application/swe+json`, `recordSchema`, and `JSONEncoding`.
- Candidate-resource limits: `GET /controlstreams/0m4qpft9sdag/commands?limit=1` with `Accept: application/swe+json` returned HTTP 200 JSON with empty `items`; `GET /commands?limit=1` and `GET /systemEvents?limit=1` with `Accept: application/swe+json` returned HTTP 400 JSON.
- Local OSH planning limit: `field-hub-osh-1` is running but unhealthy, current shell has no `SMOKE_AUTH_CREDENTIAL`, and unauthenticated `/sensorhub/api/conformance` returned HTTP 401.
- Planning E2E smoke: `SMOKE_CONTAINER_NAME=ets-csapi-s29-swejson-plan-georobotix SMOKE_OUTPUT_DIR=/tmp/sprint-ets-29-swejson-plan-georobotix-results bash scripts/smoke-test.sh` failed `176 total / 29 passed / 16 failed / 131 skipped`.
- E2E artifacts: `ops/test-results/sprint-ets-29-plan-georobotix-smoke-failed-2026-05-26.xml` and `ops/test-results/sprint-ets-29-plan-georobotix-smoke-container-failed-2026-05-26.log`.
- Public-IUT safety: explicit container-log grep found 75 GeoRobotix GET request lines and zero matched GeoRobotix POST/PUT/PATCH/DELETE request lines. `scripts/no-mutation-oracle.py` was inconclusive because no IUT-bound request lines were recognized in this log format.
- Raze planning review: `.harness/evaluations/sprint-ets-29-plan-adversarial.yaml` returned `APPROVE_WITH_CONCERNS` confidence 0.93 with no required fixes. Low non-blocking concern: direct planning probe transcripts are summarized rather than archived as raw standalone artifacts.
- Formatter: Docker Maven `mvn -B spring-javaformat:apply` returned BUILD SUCCESS.
- Focused Maven after the Raze gapfix: Docker Maven with the same `maven:3.9-eclipse-temurin-17` image and a persistent `/tmp` Maven cache returned `78 tests / 0 failures / 0 errors / 0 skipped`; final log archived at `ops/test-results/sprint-ets-29-focused-postraze-2026-05-26.log`.
- Full Maven after the Raze gapfix: Docker Maven `mvn -B clean test` with the same image and persistent `/tmp` Maven cache returned BUILD SUCCESS, `244 tests / 0 failures / 0 errors / 3 skipped`; final log archived at `ops/test-results/sprint-ets-29-maven-postraze-2026-05-26.log`. Earlier full-Maven attempts were archived as Maven Central network/reset artifacts, not project-test failures.
- Mandatory GeoRobotix TeamEngine smoke after the Raze gapfix: `SMOKE_CONTAINER_NAME=ets-csapi-s29-swejson-generator-postraze SMOKE_OUTPUT_DIR=/tmp/sprint-ets-29-swejson-generator-postraze-georobotix-results bash scripts/smoke-test.sh` failed `186 total / 31 passed / 22 failed / 133 skipped`.
- Generator E2E artifacts: `ops/test-results/sprint-ets-29-generator-postraze-georobotix-smoke-failed-2026-05-26.xml`, `ops/test-results/sprint-ets-29-generator-postraze-georobotix-smoke-container-failed-2026-05-26.log`, and `ops/test-results/sprint-ets-29-generator-postraze-georobotix-smoke-console-failed-2026-05-26.log`.
- New SWE Common JSON group outcome: 2 PASS (`/conf/swecommon-json` declaration and condition gates), 6 FAIL (Observation-side HTTP 500 and Command-side `/controlstreams` schema validation), and 2 SKIP (missing SWE 3.0 `/conf/json-encoding-rules` prerequisite and no parseable API-definition exact `application/swe+json` write advertisement).
- Generator public-IUT safety: `scripts/no-mutation-oracle.py` recognized 83 IUT request logs, and explicit container-log grep found 83 GeoRobotix GET request lines and zero matched GeoRobotix POST/PUT/PATCH/DELETE request lines.
- Raze implementation review: initial `GAPS_FOUND` caught noncanonical Time/IssueTime false-PASS evidence and unscoped write-operation evidence. Focused recheck returned `APPROVE_WITH_CONCERNS` confidence 0.94 with both required gaps closed and no remaining required fixes.
- Commit/push: Sprint 29 Generator pushed as `062d4b7 Implement Sprint 29 Part 2 SWE Common JSON` (`05c0ee4..062d4b7 main -> main`).
- Next action recorded at Sprint 29 close was to plan `REQ-ETS-PART2-011` SWE Common Text or provision a healthy declaring IUT for positive `/conf/swecommon-json` closure. Sprint 30 has since started the SWE Common Text planning path.

## Sprint ets-28 Generator Evidence

Part 2 JSON Encoding declaration-gated read-only subset:

- Story: `epics/stories/s-ets-28-01-part2-json-planning.md`
- Contract: `.harness/contracts/sprint-ets-28.yaml`
- OpenSpec: `REQ-ETS-PART2-009` is now PARTIAL_IMPLEMENTED for OGC 23-002 Clause 16.1. At Sprint 28 close, remaining Part 2 placeholders were `REQ-ETS-PART2-010..013`; Sprint 29 has since split out `REQ-ETS-PART2-010`.
- Implementation: `Part2JsonTests` adds exact `/conf/json` declaration, SWE Common JSON record-component prerequisite visibility, `/conf/datastream`/`/conf/controlstream`/`/conf/system-event` resource condition gates, read-only JSON media type checks, Annex A.9 bundled schema validation, dynamic Observation/Command/CommandResult evidence guards that avoid shape-only PASS, and non-mutating exact-`application/json` mediatype-write API-definition checks.
- Structural coverage: `VerifyPart2JsonTests` adds 8 helper regressions, `VerifyTestNGSuiteDependency` adds `part2json` dependency/method/co-location lint, and `testng.xml` wires `part2json` with `core common`.
- Formatter: Docker Maven `mvn -B spring-javaformat:apply` returned BUILD SUCCESS.
- Focused Maven: `bash scripts/mvn-test-via-docker.sh -Dtest=VerifyPart2JsonTests,VerifyTestNGSuiteDependency` returned `72 tests / 0 failures / 0 errors / 0 skipped`.
- Full Maven: Docker Maven `mvn clean test -Dmaven.repo.local=/m2 -Dmaven.artifact.threads=1` returned BUILD SUCCESS, `230 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-28-maven-2026-05-26.log`.
- Mandatory GeoRobotix TeamEngine smoke: final rerun `SMOKE_CONTAINER_NAME=ets-csapi-s28-json-georobotix-rerun SMOKE_OUTPUT_DIR=/tmp/sprint-ets-28-json-georobotix-results-rerun bash scripts/smoke-test.sh` failed `176 total / 29 passed / 16 failed / 131 skipped`.
- E2E artifacts: `ops/test-results/sprint-ets-28-generator-georobotix-smoke-failed-2026-05-26.xml` and `ops/test-results/sprint-ets-28-generator-georobotix-smoke-container-failed-2026-05-26.log`.
- E2E interpretation: the schema classpath mapper issue is fixed; the remaining failures are public-IUT HTTP 500 read-path failures and real `/controlstreams` JSON validation failures against `controlStreamCollection.json`.
- Public-IUT safety: explicit log grep found 75 GeoRobotix GET request lines and zero matched GeoRobotix POST/PUT/PATCH/DELETE request lines. `scripts/no-mutation-oracle.py` was inconclusive because no IUT-bound request lines were recognized in this log format.
- Current limitation: no accepted zero-failure Sprint 28 E2E gate exists yet. Full positive JSON closure needs a healthy declaring IUT with valid DataStream, Observation, ControlStream, Command, CommandStatus, CommandResult, SystemEvent, SWE record-component, and mediatype-write evidence.
- Raze implementation review: `.harness/evaluations/sprint-ets-28-adversarial-implementation.yaml` recorded initial `GAPS_FOUND` confidence 0.87 for narrow schema-loader regression coverage and stale story scope wording. Both required fixes were applied. Focused recheck returned `APPROVE_WITH_CONCERNS` confidence 0.93 with no required fixes; the remaining non-blocking concern is URI path escaping hardening for resource IDs.
- Next action: either find or prepare a healthy declaring IUT for positive `/conf/json` closure, or plan the next Part 2 placeholder `REQ-ETS-PART2-010`.

## Sprint ets-28 Planning Evidence

Part 2 JSON Encoding declaration-gated read-only subset:

- Story: `epics/stories/s-ets-28-01-part2-json-planning.md`
- Contract: `.harness/contracts/sprint-ets-28.yaml`
- OpenSpec: `REQ-ETS-PART2-009` was SPECIFIED_PLANNED during planning and is now PARTIAL_IMPLEMENTED by the Generator. At Sprint 28 planning close, remaining Part 2 placeholders were `REQ-ETS-PART2-010..013`; Sprint 29 has since split out `REQ-ETS-PART2-010`.
- Scope planned: first declaration-gated, read-only Part 2 JSON Encoding subset using official `/req/json` and `/conf/json` identifiers.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-05-09; checked 2026-05-26 and not stale.
- OGC source verification: official OGC 23-002 HTML `https://docs.ogc.org/is/23-002/23-002.html`, Clause 16.1 "Requirements Class JSON Encoding" and Annex A.9.
- Normative requirement set: Requirements 93-106 cover JSON media type read/write advertisement, DataStream, Observation Schema, Observation, Observation constraints, ControlStream, Command Schema, Command, Command constraints, CommandStatus, CommandResult, CommandResult constraints, and SystemEvent JSON representations.
- Prerequisite: SWE Common 3.0 JSON record components (`/req/json-record-components`; conformance prerequisite `/conf/json-record-components`).
- Resource condition gates planned: DataStream/Observation JSON assertions require `/conf/datastream`; ControlStream/Command/CommandStatus/CommandResult assertions require `/conf/controlstream`; SystemEvent assertions require `/conf/system-event`.
- GeoRobotix planning probe: `/conformance` declares Part 2 `/conf/json`, `/conf/datastream`, `/conf/controlstream`, `/conf/system-event`, `/conf/create-replace-delete`, `/conf/swecommon-json`, `/conf/swecommon-text`, and `/conf/swecommon-binary`, but not Part 2 `/conf/api-common`, `/conf/update`, `/conf/advanced-filtering`, or SWE 3.0 `/conf/json-record-components`.
- GeoRobotix read-health probes on 2026-05-26: `GET /datastreams?limit=1` and `GET /observations?limit=1` returned HTTP 500 text/html; `GET /controlstreams?limit=1` returned HTTP 200 application/json; `GET /systemEvents?limit=1` and `GET /systems/0mqcvdnfoca0/events?limit=1` returned HTTP 400 JSON.
- GeoRobotix JSON evidence: selected ControlStream `0m4qpft9sdag` advertises `application/json`; `/controlstreams/0m4qpft9sdag/schema?cmdFormat=application/json` returned HTTP 200 with `commandFormat=application/json` and `parametersSchema`; `/controlstreams/0m4qpft9sdag/commands?limit=1` returned HTTP 200 with no candidate Command item.
- Local OSH planning limit: `field-hub-osh-1` is running but unhealthy, current shell has no `SMOKE_AUTH_CREDENTIAL`, and unauthenticated `/sensorhub/api/conformance` returned HTTP 401.
- Planning E2E smoke: first attempt from `git archive` failed before TeamEngine because the Dockerfile expects `.git`. Rerun from a temporary Git clone reached TeamEngine and failed `160 total / 27 passed / 5 failed / 128 skipped` because the public IUT still returns HTTP 500 on existing read paths. Artifacts are `ops/test-results/sprint-ets-28-plan-georobotix-smoke-failed-2026-05-26.xml` and `ops/test-results/sprint-ets-28-plan-georobotix-smoke-container-failed-2026-05-26.log`.
- Public-IUT safety: `scripts/no-mutation-oracle.py` recognized 61 GeoRobotix IUT request logs; explicit container-log search found no matched GeoRobotix POST/PUT/PATCH/DELETE request lines.
- Raze planning review: `.harness/evaluations/sprint-ets-28-plan-adversarial.yaml` returned `APPROVE_WITH_CONCERNS` confidence 0.92 with no required fixes and one low non-blocking evidence-archival concern.
- Commit/push: planning commit `5d95d55 Plan Sprint 28 Part 2 JSON` was pushed over SSH (`13b34f7..5d95d55 main -> main`).
- Next action: continue from Generator evidence above.

## Sprint ets-26 Generator Evidence

Part 2 Create/Replace/Delete safety-gated subset:

- Story: `epics/stories/s-ets-26-01-part2-create-replace-delete-planning.md`
- Contract: `.harness/contracts/sprint-ets-26.yaml`
- OpenSpec: marks `REQ-ETS-PART2-007` PARTIAL_IMPLEMENTED for OGC 23-002 Clause 14. At Sprint 26 close, remaining placeholders were `REQ-ETS-PART2-008..013`; Sprint 27 later split Update into `REQ-ETS-PART2-008`.
- Scope implemented: first safety-gated Create/Replace/Delete subset using official `/req/create-replace-delete` and `/conf/create-replace-delete` identifiers.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-05-09; checked 2026-05-13 and not stale.
- OGC source verification: official OGC 23-002 HTML `https://docs.ogc.org/is/23-002/23-002.html`, Clause 14 "Requirements Class Create/Replace/Delete"; prerequisite is OGC API Features Part 4 Create/Replace/Delete.
- Normative requirement set: Requirements 63-78 cover DataStream, Observation, ControlStream, Command, CommandStatus, CommandResult, Feasibility, Feasibility status/result, and SystemEvent lifecycle behavior.
- GeoRobotix planning probe: `/conformance` declares `/conf/create-replace-delete` and OGC API Features Part 4 `/conf/create-replace-delete`, but does not declare Part 2 `/conf/api-common`, `/conf/update`, or `/conf/advanced-filtering`.
- GeoRobotix planning readiness probes: read-only OPTIONS requests for `/datastreams`, `/datastreams/{id}`, `/observations`, `/controlstreams`, `/commands`, `/controlstreams/{id}/commands`, `/systems/{id}/events`, `/systemEvents`, and `/feasibility` returned HTTP 200 with broad `Allow` headers including write methods.
- Raze gapfix: runtime DataStream, Observation, and ControlStream create-readiness probes now use scoped OGC 23-002 Clause 14 templates (`/systems/{sysId}/datastreams`, `/datastreams/{dsId}/observations`, and `/systems/{sysId}/controlstreams`) or SKIP when parent IDs cannot be established.
- Endpoint honesty probes: `GET /commands?limit=1`, `GET /systemEvents?limit=1`, and `GET /feasibility?limit=1` returned HTTP 400 `Invalid resource name`; `GET /systems/{id}/events?limit=1` returned HTTP 400 `Only streaming requests supported on this resource`.
- Verdict policy planned: exact declaration gate; keep Features Part 4 prerequisite visibility separate; default public GeoRobotix smoke must issue zero IUT-bound POST/PUT/DELETE/PATCH; OPTIONS evidence is readiness only and cannot PASS lifecycle behavior; positive lifecycle checks require `mutation-tests-enabled=true` and `mutation-iut-policy=dedicated-mutable-iut`.
- Implementation: `Part2CreateReplaceDeleteTests` adds 9 runtime checks for exact declaration, Features Part 4 prerequisite visibility, mutation safety, DataStream/Observation OPTIONS readiness, ControlStream/nested Command OPTIONS readiness, unavailable Command/Feasibility/SystemEvent honesty, and deferred lifecycle opt-in checks for DataStream/Observation, ControlStream/Command, and Feasibility/SystemEvent.
- Structural coverage: `VerifyPart2CreateReplaceDeleteTests` adds 9 helper regressions, and `VerifyTestNGSuiteDependency` adds group dependency, method tagging, and co-location checks for `part2createreplacedelete`.
- Out of scope for Generator: public-IUT mutation, full positive lifecycle coverage, cascade validation, Part 2 Update, JSON, SWE Common, and observation-binding closure.
- Raze planning review `.harness/evaluations/sprint-ets-26-plan-adversarial.yaml`: initial `GAPS_FOUND` confidence 0.94 for missing `ops/changelog.md` entry; fixed; focused recheck `APPROVE` confidence 0.96 with no remaining required fixes.
- Raze implementation review `.harness/evaluations/sprint-ets-26-adversarial-implementation.yaml`: initial `GAPS_FOUND` confidence 0.88 for non-normative global OPTIONS readiness probes. Focused recheck `.harness/evaluations/sprint-ets-26-adversarial-gapfix.yaml` returned `APPROVE_WITH_CONCERNS` confidence 0.94; `RAZE-ETS26-IMPL-GAP-001` is closed. The local OSH E2E blocker was later closed by seed fixture repair; GeoRobotix remains a failing advisory external check.
- Planning E2E smoke: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s26-plan bash scripts/smoke-test.sh` reported `137 total / 72 passed / 0 failed / 65 skipped` on GeoRobotix; report archived at `ops/test-results/sprint-ets-26-plan-smoke-2026-05-13.xml`, container log at `ops/test-results/sprint-ets-26-plan-smoke-container-2026-05-13.log`.
- No-mutation proof: GeoRobotix smoke recognized 100 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH.
- Formatter: Docker Maven `mvn -B spring-javaformat:apply` returned BUILD SUCCESS.
- Maven: `bash scripts/mvn-test-via-docker.sh` returned BUILD SUCCESS after the Raze gapfix, `207 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-26-maven-2026-05-22.log`.
- GeoRobotix advisory public smoke after the Raze gapfix: `146 total / 27 passed / 5 failed / 114 skipped`; failed because the public IUT returned HTTP 500 for existing SystemFeatures/Datastream/Observation reads. The new Part 2 CRD tests dependency-SKIP because `systemfeatures` did not finish successfully. Artifacts: `ops/test-results/sprint-ets-26-gapfix-georobotix-smoke-failed-2026-05-22.xml` and `ops/test-results/sprint-ets-26-gapfix-georobotix-smoke-container-failed-2026-05-22.log`.
- Direct GeoRobotix probes on 2026-05-22 returned HTTP 500 for `GET /systems/0mqcvdnfoca0`, `GET /datastreams?limit=1`, and `GET /observations?limit=2`.
- Local OSH fallback E2E smoke after the Raze gapfix with Basic auth and explicit mutable-IUT opt-in: `146 total / 61 passed / 4 failed / 81 skipped`; the 4 failures are existing SensorML deployment/procedure alternate-resource HTTP 500 checks. New Part 2 CRD runtime tests reported 3 PASS and 6 SKIP, with no Part 2 lifecycle mutation issued. Existing Part 1 system CRD checks did issue system POST/PUT/DELETE under the explicit opt-in. Artifacts: `ops/test-results/sprint-ets-26-gapfix-local-osh-smoke-failed-2026-05-22.xml` and `ops/test-results/sprint-ets-26-gapfix-local-osh-smoke-container-failed-2026-05-22.log`.
- Accepted local OSH E2E gate after adding Procedure/Deployment featureType metadata to the seed fixture and live records: `146 total / 62 passed / 0 failed / 84 skipped`. Direct `?f=sml3` probes for `/procedures/040g` and `/deployments/040g` returned HTTP 200 `application/sml+json`. `procedureSensorMlHasSchemaAndMapping` PASSed; deployment SensorML mapping and non-system relation-type checks SKIP honestly because OSH's generated SensorML has no `deployedSystems`/`links` evidence. New Part 2 CRD runtime tests remained 3 PASS and 6 SKIP with no Part 2 lifecycle mutation. Artifacts: `ops/test-results/sprint-ets-26-seedfix-local-osh-smoke-2026-05-22.xml` and `ops/test-results/sprint-ets-26-seedfix-local-osh-smoke-container-2026-05-22.log`.
- Raze seedfix review `.harness/evaluations/sprint-ets-26-local-osh-seedfix-raze.yaml`: initial `GAPS_FOUND` confidence 0.87 for stale fixture verification metadata; focused recheck returned `APPROVE_WITH_CONCERNS` confidence 0.94 after the fixture split historical vs current verification evidence. No required fixes remain.
- Raze local OSH E2E acceptance review `.harness/evaluations/sprint-ets-26-local-osh-e2e-acceptance-raze.yaml`: `APPROVE_WITH_CONCERNS` confidence 0.93 with no required fixes. Low concerns only: make the eventual commit message explicit about the broader policy change, and direct SensorML 200 probes are documented but not archived as raw standalone transcripts.
- Next action: plan the next Part 2 placeholder after Update.

## Sprint ets-27 Generator Evidence

Part 2 Update safety-gated subset:

- Story: `epics/stories/s-ets-27-01-part2-update-planning.md`
- Contract: `.harness/contracts/sprint-ets-27.yaml`
- OpenSpec: `REQ-ETS-PART2-008` is now PARTIAL_IMPLEMENTED for OGC 23-002 Clause 15; remaining Part 2 placeholders are `REQ-ETS-PART2-009..013`.
- Scope implemented: first safety-gated Update subset using official `/req/update` and `/conf/update` identifiers.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-05-09; checked during Sprint 27 planning on 2026-05-22 and not stale.
- OGC source verification: official OGC 23-002 HTML `https://docs.ogc.org/is/23-002/23-002.html`, Clause 15 "Requirements Class Update" and Annex A.8.
- Normative requirement set: Requirements 79-92 cover DataStream, Observation, ControlStream, Command, CommandStatus, CommandResult, Feasibility, Feasibility status/result, and SystemEvent PATCH behavior.
- Prerequisites: Part 2 `/req/create-replace-delete` and OGC API Features Part 4 `/req/update`; corresponding conformance prerequisites are Part 2 `/conf/create-replace-delete` and OGC API Features Part 4 `/conf/update`.
- Clause 15 condition gates: R79-R82 require `/conf/datastream`, R83-R88 require `/conf/controlstream`, R89-R91 require `/conf/feasibility`, and R92 requires `/conf/system-event`; missing condition classes SKIP prerequisite-incomplete rather than PASS from `/conf/update`, endpoint availability, sibling declarations, or OPTIONS.
- GeoRobotix planning probe: `/conformance` declares Part 2 `/conf/create-replace-delete` and OGC API Features Part 4 `/conf/create-replace-delete`, but does not declare Part 2 `/conf/api-common`, `/conf/update`, or `/conf/advanced-filtering`.
- GeoRobotix planning readiness probes: sampled read-only OPTIONS requests for DataStream, Observation, ControlStream, Command, Feasibility, SystemEvent, and system-scoped event endpoints returned HTTP 200 with broad `Allow` headers, but PATCH was absent.
- GeoRobotix read-health probes on 2026-05-22: `GET /systems/0mqcvdnfoca0`, `GET /datastreams?limit=1`, and `GET /observations?limit=1` returned HTTP 500; `GET /controlstreams?limit=1` returned HTTP 200 JSON.
- Local OSH probe: the container `field-hub-osh-1` is running and requires Basic auth. Unauthenticated `/conformance` returns HTTP 401; authenticated `/conformance` returns HTTP 200, does not declare `/conf/update`, and authenticated `OPTIONS /systems/040g` omits PATCH.
- Verdict policy implemented: exact `/conf/update` declaration gate; keep Part 2 CRD and Features Part 4 Update prerequisites separate; apply Clause 15 resource condition gates before any R79-R92 PASS; default public GeoRobotix smoke must issue zero IUT-bound PATCH/POST/PUT/DELETE; OPTIONS evidence is readiness only; declared `/conf/update` plus successful OPTIONS omitting PATCH fails readiness while lifecycle skips before PATCH; positive PATCH requires explicit dedicated mutable-IUT opt-in and changed-field GET proof.
- Implementation: `Part2UpdateTests` adds 14 runtime checks plus shared read-only setup for exact declaration, prerequisite visibility, condition-gate visibility, public-IUT mutation safety, DataStream/Observation PATCH readiness and deferred lifecycle checks, ControlStream/Command PATCH readiness and deferred lifecycle checks, separate Feasibility and SystemEvent PATCH readiness and deferred lifecycle checks, unavailable-endpoint honesty, and schema-rejection honesty.
- Structural coverage: `VerifyPart2UpdateTests` adds 9 helper regressions, and `VerifyTestNGSuiteDependency` adds group dependency, method-tagging, and co-location checks for `part2update`.
- TestNG wiring: `part2update` depends on `core common systemfeatures`, not Part 1 Update, Part 2 API Common, Part 2 Create/Replace/Delete, or resource-class groups; runtime prerequisite and condition-gate honesty remains visible.
- Formatter: Docker Maven `mvn -B spring-javaformat:apply` returned BUILD SUCCESS.
- Maven: `bash scripts/mvn-test-via-docker.sh` returned BUILD SUCCESS with `219 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-27-maven-2026-05-22.log`.
- Planning E2E: GeoRobotix TeamEngine smoke ran and failed `146 total / 27 passed / 5 failed / 114 skipped` because the public IUT still returns HTTP 500 on existing SystemFeatures/GeoJSON/SensorML/Datastream/Observation read paths. Artifacts are `ops/test-results/sprint-ets-27-plan-georobotix-smoke-failed-2026-05-22.xml` and `ops/test-results/sprint-ets-27-plan-georobotix-smoke-container-failed-2026-05-22.log`. No matched GeoRobotix PATCH/POST/PUT/DELETE request lines appear in the archived container log. Local OSH lacked a credential-bearing smoke environment at planning time, then was accepted during Generator verification.
- Generator E2E: GeoRobotix TeamEngine smoke ran and failed `160 total / 27 passed / 5 failed / 128 skipped` because the public IUT still returns HTTP 500 on existing read paths. Artifacts are `ops/test-results/sprint-ets-27-generator-georobotix-smoke-failed-2026-05-22.xml` and `ops/test-results/sprint-ets-27-generator-georobotix-smoke-container-failed-2026-05-22.log`. All 14 Part 2 Update runtime tests SKIP because `systemfeatures` does not finish successfully on the public IUT.
- Public-IUT safety: `scripts/no-mutation-oracle.py` recognized 61 GeoRobotix IUT request logs and found zero IUT-bound PATCH/POST/PUT/DELETE; an explicit container-log grep found no matched GeoRobotix write-method lines.
- Accepted local OSH E2E: authenticated TeamEngine smoke with explicit dedicated mutable-IUT opt-in passed `160 total / 62 passed / 0 failed / 98 skipped`. Artifacts are `ops/test-results/sprint-ets-27-generator-local-osh-smoke-2026-05-22.xml` and `ops/test-results/sprint-ets-27-generator-local-osh-smoke-container-2026-05-22.log`. All 14 Part 2 Update runtime tests SKIP because local OSH does not declare `/conf/update`, and the local OSH container log contains zero PATCH request lines.
- Raze planning review `.harness/evaluations/sprint-ets-27-plan-adversarial.yaml`: initial `GAPS_FOUND` confidence 0.91 for missing Clause 15 per-requirement condition gates. Focused recheck closed `RAZE-ETS27-PLAN-GAP-001` after the gapfix added those gates across OpenSpec, story, contract, traceability, epic, ops docs, and handoff; final verdict `APPROVE_WITH_CONCERNS` confidence 0.95 with no required fixes.
- Raze implementation review `.harness/evaluations/sprint-ets-27-adversarial-implementation.yaml`: initial `GAPS_FOUND` confidence 0.90 for stale story/traceability wording. Focused recheck `.harness/evaluations/sprint-ets-27-adversarial-implementation-recheck.yaml` closed `RAZE-ETS27-IMPL-GAP-001` with `APPROVE_WITH_CONCERNS` confidence 0.94 and no required fixes.
- Raze local OSH E2E acceptance review `.harness/evaluations/sprint-ets-27-local-osh-e2e-acceptance-raze.yaml`: `APPROVE_WITH_CONCERNS` confidence 0.94 with no required fixes; it confirms the accepted local OSH gate is zero-failure but partial for Update semantics because `/conf/update` is absent.
- Commit/push: planning commit `eab12a8 Plan Sprint 27 Part 2 Update` and planning reconciliation `2be355a Reconcile Sprint 27 planning push` were already pushed. Generator commit `6ae8f1c Implement Sprint 27 Part 2 Update with local OSH E2E gate` was pushed over SSH (`2be355a..6ae8f1c main -> main`).

## Sprint ets-25 Generator Evidence

Part 2 Advanced Filtering read-only declaration-gated subset:

- Story: `epics/stories/s-ets-25-01-part2-advanced-filtering-planning.md`
- Contract: `.harness/contracts/sprint-ets-25.yaml`
- OpenSpec: marks `REQ-ETS-PART2-006` PARTIAL_IMPLEMENTED for OGC 23-002 Clause 13. Sprint 26 later split `REQ-ETS-PART2-007` out for Create/Replace/Delete, and Sprint 27 split `REQ-ETS-PART2-008` out for Update.
- Scope implemented: first read-only, declaration-gated Advanced Filtering subset using official `/req/advanced-filtering` and `/conf/advanced-filtering` identifiers.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-05-09 after the Sprint 25 taxonomy correction; not stale.
- OGC source verification: official OGC 23-002 HTML `https://docs.ogc.org/is/23-002/23-002.html`, Clause 13 "Requirements Class Advanced Filtering"; prerequisites are `/req/api-common` and Part 1 `/req/advanced-filtering`.
- Normative requirement set: Requirements 45-62 cover DataStream, Observation, ControlStream, Command, CommandStatus, and SystemEvent filter query parameters.
- Taxonomy correction: OGC 23-002 Annex A does not define `/conf/system-history` or `/req/system-history`; GeoRobotix's `/conf/system-history` declaration is treated as non-standard/vendor extension evidence only.
- Implementation: `Part2AdvancedFilteringTests` adds 9 read-only runtime checks for exact `/conf/advanced-filtering` declaration, prerequisite visibility, DataStream time filters, DataStream `observedProperty`, Observation time filters, ControlStream time filters, ControlStream `controlledProperty`, Command filters when `/commands` is available, and SystemEvent `eventType` when `/systemEvents` is available.
- Structural coverage: `VerifyPart2AdvancedFilteringTests` adds 8 helper regressions, including strict Observation `phenomenonTime` evidence and malformed time-substring rejection; `VerifyTestNGSuiteDependency` adds `part2advancedfiltering` group/dependency/co-location checks; and `Part2ApiCommonTests` no longer treats `systemhistory` as an OGC Part 2 collection token.
- GeoRobotix planning probe: `/conformance` does not declare `/conf/advanced-filtering`.
- GeoRobotix read-only filter probes: selected `/datastreams` and `/controlstreams` filter requests returned HTTP 200 JSON with `items`; selected `/observations` time filters returned HTTP 200 JSON with empty `items`; `/commands` filter requests returned HTTP 400; `/systemEvents?eventType=...` returned HTTP 400; `/systems/{id}/events?eventType=...` returned HTTP 400 streaming-only.
- Verdict policy implemented: exact declaration gate; no Advanced Filtering PASS from undeclared HTTP 200 query behavior, empty result collections, endpoint availability alone, sibling Part 2 declarations, or the non-standard `/conf/system-history` declaration. Empty seed-derived filtered responses SKIP with reason and do not PASS.
- Out of scope: mutation, seed-resource creation, full FOI recursive graph traversal, streaming/SSE event filter consumption, and full closure for Command filters while `/commands` is unavailable.
- Raze planning review `.harness/evaluations/sprint-ets-25-plan-adversarial.yaml`: initial `GAPS_FOUND` for one stale `REQ-ETS-PART2-014` epic acceptance reference; fixed to `REQ-ETS-PART2-013`; recheck `APPROVE` confidence 0.96.
- Formatter: Docker Maven `mvn -B spring-javaformat:apply` BUILD SUCCESS.
- Raze gapfix: `obs-by-phenomenontime` now seeds and validates only `phenomenonTime`, with no `resultTime` fallback; `timeIntersects` now parses instants/intervals and rejects malformed substring evidence.
- Maven: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `195 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-25-maven-2026-05-13.log`.
- TeamEngine smoke: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s25-final bash scripts/smoke-test.sh` reported `137 total / 72 passed / 0 failed / 65 skipped`; report archived at `ops/test-results/sprint-ets-25-smoke-2026-05-13.xml`, container log at `ops/test-results/sprint-ets-25-smoke-container-2026-05-13.log`.
- No-mutation proof: GeoRobotix smoke recognized 100 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH.
- Runtime outcome: all 9 Part 2 Advanced Filtering runtime tests SKIP on GeoRobotix because `/conf/advanced-filtering` is not declared.
- Raze implementation review `.harness/evaluations/sprint-ets-25-adversarial-implementation.yaml`: `GAPS_FOUND` confidence 0.91. Required gap and low concern are fixed and verified. Focused gapfix review `.harness/evaluations/sprint-ets-25-adversarial-gapfix.yaml`: `APPROVE` confidence 0.96 with no required fixes.
- Commit/push: `2f4a6de Plan Sprint 25 Advanced Filtering` pushed over SSH on 2026-05-09 (`5dccb36..2f4a6de main -> main`).
- Commit/push: `d9df3ad Implement Sprint 25 Advanced Filtering` pushed over SSH on 2026-05-13 (`f251241..d9df3ad main -> main`).
- Next action: plan the next Part 2 sprint item or continue deferred Advanced Filtering coverage against a declaring IUT.

## Sprint ets-24 Generator Evidence

Part 2 System Events read-only declaration-gated subset:

- Story: `epics/stories/s-ets-24-01-part2-system-event-planning.md`
- Contract: `.harness/contracts/sprint-ets-24.yaml`
- Scope implemented: first read-only System Events subset using official OGC 23-002 `/req/system-event` and `/conf/system-event` identifiers.
- Implementation: `Part2SystemEventTests` adds 6 runtime checks for exact declaration, visible prerequisites, `/systemEvents`, normative `/systems/{sysId}/events`, optional canonical `/systemEvents/{id}` resource reads, and optional `itemType=SystemEvent` collections.
- Structural coverage: `VerifyPart2SystemEventTests` adds 5 helper regressions for official identifiers, normative path selection, SystemEvent resource evidence, collection item type, and collection shape; `VerifyTestNGSuiteDependency` adds `part2systemevent` group and co-location checks.
- Endpoint honesty: Requirement 42 uses `/systemEvents`; Requirement 43 uses `/systems/{sysId}/events`; Annex A.43's `/systems/{sysId}/systemEvents` string remains diagnostic only unless a standards-backed correction is documented.
- Formatter: Docker Maven `mvn -B spring-javaformat:apply` BUILD SUCCESS.
- Maven: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `183 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-24-maven-2026-05-09.log`.
- TeamEngine smoke: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s24 bash scripts/smoke-test.sh` reported `128 total / 72 passed / 0 failed / 56 skipped`; report archived at `ops/test-results/sprint-ets-24-smoke-2026-05-09.xml`, container log at `ops/test-results/sprint-ets-24-smoke-container-2026-05-09.log`.
- No-mutation proof: GeoRobotix smoke recognized 99 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH.
- Runtime outcome: GeoRobotix declares `/conf/system-event` but not `/conf/api-common`; 1 System Event test PASSed for exact declaration and 5 SKIP honestly for missing prerequisite, HTTP 400 `/systemEvents`, streaming-only HTTP 400 `/systems/{id}/events`, no SystemEvent resource evidence, and no advertised `itemType=SystemEvent` collection.
- Raze implementation review `.harness/evaluations/sprint-ets-24-adversarial-implementation.yaml` returned `APPROVE` confidence 0.94 with no required fixes.
- Commit/push: `6fa00c4 Implement Sprint 24 System Events` pushed over SSH on 2026-05-09 (`1f5a916..6fa00c4 main -> main`).
- Next action: plan the next Part 2 sprint item.

## Sprint ets-24 Planning Evidence

Part 2 System Events read-only declaration-gated subset:

- Story: `epics/stories/s-ets-24-01-part2-system-event-planning.md`
- Contract: `.harness/contracts/sprint-ets-24.yaml`
- OpenSpec: defines `REQ-ETS-PART2-005` for OGC 23-002 Clause 12. At Sprint 24 close the remaining placeholders were `REQ-ETS-PART2-006..014`; Sprint 25 later corrected that stale taxonomy by retiring `/conf/system-history`.
- Scope planned: first read-only, declaration-gated System Events subset using official `/req/system-event` and `/conf/system-event` identifiers.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-08 and not stale.
- OGC source verification: official OGC 23-002 HTML `https://docs.ogc.org/is/23-002/23-002.html`, Clause 12 "Requirements Class System Events"; prerequisites are `/req/api-common` and Part 1 `/req/system`.
- Normative endpoint guard: Requirement 42 uses `{api_root}/systemEvents`; Requirement 43 uses `{api_root}/systems/{sysId}/events`. Annex A.43's `/systems/{sysId}/systemEvents` text conflicts with Requirement 43 and is diagnostic-only unless a standards-backed correction is documented.
- GeoRobotix planning probe: `/conformance` declares `/conf/system-event` but not `/conf/api-common`.
- GeoRobotix read-only probes: `GET /systemEvents?limit=2` returned HTTP 400 `Invalid resource name: 'systemEvents'`; `GET /systems/0mqcvdnfoca0/events?limit=2` returned HTTP 400 JSON `Only streaming requests supported on this resource`; `GET /systems/0mqcvdnfoca0/systemEvents?limit=2` returned HTTP 400 `Invalid resource name: 'systemEvents'`; `/collections` did not expose `itemType=SystemEvent`.
- Verdict policy planned: gate System Events assertions on exact `/conf/system-event`; keep missing `/conf/api-common` prerequisite honesty separate; do not PASS from declaration alone, sibling Part 2 declarations, streaming-only HTTP 400 responses, or empty/generic collection shape.
- Out of scope: streaming/SSE event consumption, System History, Advanced Filtering event-by-type, Part 2 JSON schema closure, and mutation classes.
- Raze planning review `.harness/evaluations/sprint-ets-24-plan-adversarial.yaml` returned `APPROVE` confidence 0.93 with no required fixes.
- Planning-only docs change; no Java code, Maven, or TeamEngine smoke run yet.
- Next action: commit/push Sprint 24 planning, then start Generator.

## Sprint ets-20 Generator Evidence

Part 2 API Common read-only declaration-gated subset:

- Story: `epics/stories/s-ets-20-01-part2-api-common-planning.md`
- Contract: `.harness/contracts/sprint-ets-20.yaml`
- OpenSpec: activates `REQ-ETS-PART2-001`; remaining Part 2 classes remain deferred placeholders.
- Scope implemented: OGC 23-002 Requirements Class "Common" first read-only subset using official `/req/api-common`, `/conf/api-common`, `/req/api-common/resources`, and `/req/api-common/resource-collection` identifiers.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-07 and not stale.
- OGC source verification: official OGC 23-002 HTML `https://docs.ogc.org/is/23-002/23-002.html`, Clause 8 "Requirements Class Common"; prerequisite is Part 1 API Common.
- Correction: frozen web-app Part 2 `dynamic-common` / `dynamic-json` names are historical and must not be used in Java ETS `@Test` descriptions.
- GeoRobotix planning probe: `/conformance` declares Part 2 `/conf/datastream`, `/conf/controlstream`, `/conf/json`, `/conf/create-replace-delete`, `/conf/system-event`, `/conf/system-history`, and SWE Common encoding classes, but not `/conf/api-common`.
- GeoRobotix read-only probes: landing page exposes `datastreams` and `observations`; `GET /datastreams?limit=1`, `/observations?limit=1`, and `/controlstreams?limit=1` return HTTP 200 JSON with `items` and `links`; `GET /commands?limit=1` returns HTTP 400.
- Verdict policy implemented: absence of `/conf/api-common` is SKIP-with-reason for Part 2 API Common declaration/resource-judgment tests; sibling Part 2 class declarations do not imply API Common PASS.
- Implementation: `Part2ApiCommonTests` adds exact declaration gating, advertised-link-only Part 2 collection discovery, read-only `items`/`links` collection shape checks, and a Core/Common dependency runtime tracer.
- Structural lint: `VerifyTestNGSuiteDependency` checks `part2apicommon` group dependency and co-location; `VerifyPart2ApiCommonTests` prevents stale `dynamic-*` identifiers and synthesized `/commands` assumptions.
- Verification: formatter BUILD SUCCESS; Maven post-Raze rerun `152 tests / 0 failures / 0 errors / 3 skipped`; GeoRobotix TeamEngine smoke `93 total / 55 passed / 0 failed / 38 skipped`.
- No-mutation proof: GeoRobotix smoke recognized 71 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH entries.
- Raze planning review `.harness/evaluations/sprint-ets-20-plan-adversarial.yaml` returned `APPROVE_WITH_CONCERNS` confidence 0.92 with no required fixes. Non-blocking concern: broader Part 2 placeholder taxonomy still says 14 classes and duplicates API Common in the remaining placeholder block.
- Raze implementation review `.harness/evaluations/sprint-ets-20-adversarial-implementation.yaml` returned `APPROVE_WITH_CONCERNS` confidence 0.94 with no required fixes after dependency lint hardening.
- Out of scope: Part 2 JSON, Datastream/Observation closure, ControlStream/Command closure, SWE Common encodings, Part 2 CRD/Update mutation, and full schema validation.
- Next action completed by Sprint ets-21 Generator; continue from Sprint ets-21 status below.

## Sprint ets-21 Planning Evidence

Part 2 Datastreams & Observations read-only subset:

- Story: `epics/stories/s-ets-21-01-part2-datastream-planning.md`
- Contract: `.harness/contracts/sprint-ets-21.yaml`
- OpenSpec: defines `REQ-ETS-PART2-002` for OGC 23-002 Clause 9 and renumbers remaining Part 2 placeholders to `REQ-ETS-PART2-003..014`.
- Scope planned: first read-only, declaration-gated Datastreams & Observations subset using official `/req/datastream` and `/conf/datastream` identifiers.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-07 and not stale.
- OGC source verification: official OGC 23-002 HTML `https://docs.ogc.org/is/23-002/23-002.html`, Clause 9 "Requirements Class Datastreams & Observations"; prerequisite is `/req/api-common`.
- GeoRobotix planning probe: `/conformance` declares `/conf/datastream` but not `/conf/api-common`.
- GeoRobotix read-only probes: `GET /datastreams?limit=2`, `/observations?limit=2`, `/datastreams/0mirhn7lo1kg`, `/datastreams/0mirhn7lo1kg/schema`, `/datastreams/0mirhn7lo1kg/observations?limit=2`, and `/systems/0nar3cl0tk3g/datastreams?limit=1` returned HTTP 200 JSON.
- Important IUT state: selected nested Datastream observations collection is empty and has `items` only. Generator may use this as endpoint availability evidence only; `/req/datastream/obs-ref-from-datastream` needs actual nested Observation/reference evidence or SKIPs with an empty-IUT-state reason.
- Verdict policy planned: gate scoped Datastream endpoint PASS evidence on `/conf/datastream`; keep missing `/conf/api-common` prerequisite honesty separate; do not infer API Common PASS or full `/conf/datastream` closure from Datastream endpoint success.
- Raze planning review `.harness/evaluations/sprint-ets-21-plan-adversarial.yaml` returned `GAPS_FOUND` confidence 0.88 for two false-PASS risks.
- Raze gap-fix review `.harness/evaluations/sprint-ets-21-plan-gapfix.yaml` returned `APPROVE` confidence 0.95 after endpoint-vs-reference and prerequisite-closure wording was fixed.
- Commit/push: `cc43d46 Plan Sprint 21 Datastream` pushed over SSH on 2026-05-07 (`6ebe947..cc43d46 main -> main`).
- Out of scope: mutation, ControlStream, Command, Part 2 JSON, SWE Common encodings, Create/Replace/Delete, Update, and observation result validation against Datastream schema.

## Sprint ets-21 Generator Evidence

Part 2 Datastreams & Observations read-only subset:

- Story: `epics/stories/s-ets-21-01-part2-datastream-planning.md`
- Contract: `.harness/contracts/sprint-ets-21.yaml`
- Scope implemented: declaration-gated `/conf/datastream`, `/datastreams`, `/datastreams/{id}`, `/datastreams/{id}/schema`, `/observations`, `/observations/{id}`, `/datastreams/{id}/observations`, and bounded `/systems/{systemId}/datastreams` checks.
- Implementation: `Part2DatastreamTests` adds Datastream/Observation read-only checks and explicit `/conf/api-common` prerequisite SKIP for full closure; `VerifyPart2DatastreamTests` and `VerifyTestNGSuiteDependency` add helper and TestNG structural regressions.
- Verification: formatter BUILD SUCCESS; Maven `160 tests / 0 failures / 0 errors / 3 skipped` archived at `ops/test-results/sprint-ets-21-maven-2026-05-07.log`; GeoRobotix TeamEngine smoke `104 total / 64 passed / 0 failed / 40 skipped` archived at `ops/test-results/sprint-ets-21-teamengine-smoke-2026-05-07.xml`.
- No-mutation proof: GeoRobotix smoke recognized 82 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH entries.
- Runtime outcome: GeoRobotix declares `/conf/datastream` but not `/conf/api-common`, so scoped endpoint checks run while full `/conf/datastream` closure remains prerequisite-incomplete. Empty nested observations SKIP `/req/datastream/obs-ref-from-datastream` rather than producing PASS.
- Raze implementation review `.harness/evaluations/sprint-ets-21-adversarial-implementation.yaml` returned `GAPS_FOUND` confidence 0.90 for reconciliation/evidence gaps only; gap-fix `.harness/evaluations/sprint-ets-21-adversarial-gapfix.yaml` returned `APPROVE` confidence 0.96 after docs were corrected and Maven/smoke artifacts were archived under `ops/test-results/`.
- Next action: plan Sprint 22 for the next Part 2 item.

## Sprint ets-22 Planning Evidence

Part 2 Control Streams & Commands read-only subset:

- Story: `epics/stories/s-ets-22-01-part2-controlstream-planning.md`
- Contract: `.harness/contracts/sprint-ets-22.yaml`
- OpenSpec: defines `REQ-ETS-PART2-003` for OGC 23-002 Clause 10 and renumbers remaining Part 2 placeholders to `REQ-ETS-PART2-004..014`.
- Scope planned: first read-only, declaration-gated Control Streams & Commands subset using official `/req/controlstream` and `/conf/controlstream` identifiers.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-07 and not stale.
- OGC source verification: official OGC 23-002 HTML `https://docs.ogc.org/is/23-002/23-002.html`, Clause 10 "Requirements Class Control Streams & Commands"; prerequisite is `/req/api-common`.
- GeoRobotix planning probe: `/conformance` declares `/conf/controlstream` but not `/conf/api-common`.
- GeoRobotix read-only probes: `GET /controlstreams?limit=2`, `/controlstreams/0m4qpft9sdag`, `/controlstreams/0m4qpft9sdag/schema`, `/controlstreams/0m4qpft9sdag/commands?limit=2`, and `/systems/0m5ojudgr570/controlstreams?limit=2` returned HTTP 200 JSON.
- Important IUT state: selected nested ControlStream commands collection is empty and has `items` only. Generator may use this as endpoint availability evidence only; `/req/controlstream/cmd-ref-from-controlstream` needs actual nested Command/reference evidence or SKIPs with an empty-IUT-state reason.
- Important IUT gaps: `GET /commands?limit=2` and `GET /controls/0m4qpft9sdag` returned HTTP 400, so Generator must not PASS global Command endpoint or `/req/controlstream/canonical-url` from nested or alias evidence alone.
- Verdict policy planned: gate scoped ControlStream endpoint PASS evidence on `/conf/controlstream`; keep missing `/conf/api-common` prerequisite honesty separate; do not infer API Common PASS or full `/conf/controlstream` closure from ControlStream endpoint success.
- Raze planning review `.harness/evaluations/sprint-ets-22-plan-adversarial.yaml` returned `APPROVE` confidence 0.93 with no required fixes.
- Commit/push: `2ffed0c Plan Sprint 22 ControlStream` pushed over SSH on 2026-05-07 (`5c4bcf0..2ffed0c main -> main`).
- Out of scope: mutation, command creation, command feasibility, Command status/result closure, Part 2 JSON, SWE Common encodings, Create/Replace/Delete, Update, and command result validation against ControlStream schema.
- Next action completed by Sprint ets-22 Generator; continue from Sprint ets-22 Generator status below.

## Sprint ets-22 Generator Evidence

Part 2 Control Streams & Commands read-only subset:

- Story: `epics/stories/s-ets-22-01-part2-controlstream-planning.md`
- Contract: `.harness/contracts/sprint-ets-22.yaml`
- Scope implemented: declaration-gated `/conf/controlstream`, `/controlstreams`, selected `/controlstreams/{id}`, `/controlstreams/{id}/schema`, `/controlstreams/{id}/commands`, bounded `/systems/{systemId}/controlstreams`, `/commands` only when readable, `/controls/{id}` only when readable, and populated nested Command reference evidence only when present.
- Implementation: `Part2ControlStreamTests` adds the runtime checks and full-closure `/conf/api-common` prerequisite SKIP; `VerifyPart2ControlStreamTests` and `VerifyTestNGSuiteDependency` add helper and TestNG structural regressions.
- Verification: formatter BUILD SUCCESS; Maven `167 tests / 0 failures / 0 errors / 3 skipped` archived at `ops/test-results/sprint-ets-22-maven-2026-05-08.log`; GeoRobotix TeamEngine smoke `115 total / 71 passed / 0 failed / 44 skipped` archived at `ops/test-results/sprint-ets-22-smoke-2026-05-08.xml`.
- No-mutation proof: GeoRobotix smoke recognized 91 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH entries.
- Runtime outcome: GeoRobotix declares `/conf/controlstream` but not `/conf/api-common`, so scoped endpoint checks run while full `/conf/controlstream` closure remains prerequisite-incomplete. `/controls/{id}` and `/commands` return HTTP 400, so canonical URL and global Command endpoint checks SKIP. Empty nested Commands SKIP `/req/controlstream/cmd-ref-from-controlstream`.
- Raze implementation review `.harness/evaluations/sprint-ets-22-adversarial-implementation.yaml` returned `GAPS_FOUND` confidence 0.91 for reconciliation/evidence gaps only; gap-fix review `.harness/evaluations/sprint-ets-22-adversarial-gapfix.yaml` returned `APPROVE` confidence 0.95 after planner handoff was superseded and Maven evidence was archived.
- Next action: plan Sprint 23 for `REQ-ETS-PART2-004` `/conf/feasibility` unless reprioritized.

## Sprint ets-23 Planning Evidence

Part 2 Command Feasibility safety-gated subset:

- Story: `epics/stories/s-ets-23-01-part2-feasibility-planning.md`
- Contract: `.harness/contracts/sprint-ets-23.yaml`
- OpenSpec: defines `REQ-ETS-PART2-004` for OGC 23-002 Clause 11 and renumbers remaining Part 2 placeholders to `REQ-ETS-PART2-005..014`.
- Scope planned: safety-gated Command Feasibility subset using official `/req/feasibility` and `/conf/feasibility` identifiers.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-08 and not stale.
- OGC source verification: official OGC 23-002 HTML `https://docs.ogc.org/is/23-002/23-002.html`, Clause 11 "Requirements Class Command Feasibility"; prerequisite is `/req/controlstream`; normative statements are `/req/feasibility/canonical-url`, `/req/feasibility/ref-from-controlstream`, `/req/feasibility/status-endpoint`, `/req/feasibility/result-endpoint`, and `/req/feasibility/collections`.
- GeoRobotix planning probe: `/conformance` declares `/conf/controlstream` but not `/conf/feasibility` or `/conf/api-common`.
- GeoRobotix feasibility probes: `GET /feasibility?limit=2`, `GET /controlstreams/0m4qpft9sdag/feasibility?limit=2`, and `GET /controlstream/0m4qpft9sdag/feasibility?limit=2` returned HTTP 400 JSON `Invalid resource name` variants; `GET /collections?limit=100` did not expose `itemType=Feasibility`.
- Important safety policy: OGC states a feasibility request is initiated by creating a `Command` resource on the feasibility channel, so default public smoke must not issue feasibility POSTs. Positive feasibility creation checks require explicit safe/mutable-IUT opt-in.
- Important endpoint policy: `/req/feasibility/ref-from-controlstream` cites normative singular path `{api_root}/controlstream/{csId}/feasibility`. The plural `/controlstreams/{id}/feasibility` probe is diagnostic only and must not satisfy the requirement by itself.
- Verdict policy planned: gate all Feasibility assertions on exact `/conf/feasibility`; preserve `/req/controlstream` prerequisite honesty; require actual Feasibility resource evidence for canonical/status/result PASS; treat Feasibility collections as optional unless advertised.
- Raze planning review `.harness/evaluations/sprint-ets-23-plan-adversarial.yaml` returned `GAPS_FOUND` confidence 0.89 for endpoint-alias false-PASS risk and stale status metadata.
- Raze gap-fix review `.harness/evaluations/sprint-ets-23-plan-gapfix.yaml` returned `APPROVE` confidence 0.96 after the normative singular endpoint guard and metadata refresh were added.
- Commit/push: `61004e5 Plan Sprint 23 Feasibility` pushed over SSH on 2026-05-08 (`b83f29c..61004e5 main -> main`).
- Planning-only docs change; no Java code, Maven, or TeamEngine smoke run yet.
- Next action completed by Sprint ets-23 Generator; continue from Sprint ets-23 Generator status below.

## Sprint ets-23 Generator Evidence

Part 2 Command Feasibility safety-gated subset:

- Story: `epics/stories/s-ets-23-01-part2-feasibility-planning.md`
- Contract: `.harness/contracts/sprint-ets-23.yaml`
- Scope implemented: declaration-gated `/conf/feasibility`, `/req/controlstream` prerequisite visibility, normative singular `/controlstream/{csId}/feasibility` GET check, optional `/feasibility` collection/resource evidence, optional `/feasibility/{id}/status`, optional `/feasibility/{id}/result`, and optional `itemType=Feasibility` collection checks.
- Implementation: `Part2FeasibilityTests` adds 7 read-only/default-safe Feasibility tests; `VerifyPart2FeasibilityTests` adds helper regressions for official identifiers, singular endpoint path, Feasibility resource evidence, collection itemType, and collection shape; `VerifyTestNGSuiteDependency` adds `part2feasibility` group dependency and co-location regressions.
- Runtime safety: no Feasibility POST/PUT/DELETE/PATCH path is implemented. The default public GeoRobotix smoke SKIPs before any feasibility write because `/conf/feasibility` is absent.
- Formatter: Docker Maven `mvn -B spring-javaformat:apply` BUILD SUCCESS.
- Maven: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `175 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-23-maven-2026-05-08.log`.
- TeamEngine smoke: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s23-gapfix bash scripts/smoke-test.sh` reported `122 total / 71 passed / 0 failed / 51 skipped`; report archived at `ops/test-results/sprint-ets-23-smoke-2026-05-08.xml`, container log at `ops/test-results/sprint-ets-23-smoke-container-2026-05-08.log`.
- No-mutation proof: GeoRobotix smoke recognized 93 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH.
- Runtime outcome: all 7 Feasibility runtime tests SKIP honestly because GeoRobotix does not declare `/conf/feasibility`; no Feasibility PASS is inferred from `/conf/controlstream`.
- Raze implementation review `.harness/evaluations/sprint-ets-23-adversarial-implementation.yaml` returned `GAPS_FOUND` confidence 0.90 for a collection-shape-only canonical false-PASS risk; fixed by requiring Feasibility-shaped resource evidence before canonical/status/result PASS.
- Raze gap-fix review `.harness/evaluations/sprint-ets-23-adversarial-gapfix.yaml` returned `APPROVE` confidence 0.97 with no required fixes.
- Commit/push: `abba276 Implement Sprint 23 Feasibility` pushed over SSH on 2026-05-08 (`ab15704..abba276 main -> main`).
- Next action: plan the next Part 2 sprint item.

## Sprint ets-19 Generator Evidence

Encoding mediatype-write safety-gated checks:

- Story: `epics/stories/s-ets-19-01-encoding-mediatype-write-safety-gated.md`
- Contract: `.harness/contracts/sprint-ets-19.yaml`
- OpenSpec: extends `REQ-ETS-PART1-012`, `REQ-ETS-PART1-013`, and mutation-safety dependency `REQ-ETS-PART1-010`; GeoJSON/SensorML remain PARTIAL.
- Scope implemented: `Content-Type: application/geo+json` and `Content-Type: application/sml+json` parsing checks behind existing mutation opt-in and public-IUT hard-denial gates.
- Out of scope: default mutation against GeoRobotix, Part 2, full external schema validation, full GeoJSON/SensorML closure, and non-system mutation-side encoding coverage.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-07 and not stale.
- OGC source verification: official upstream GeoJSON and SensorML encoding clauses list `mediatype-write`; both condition write-side media type parsing on Create/Replace/Delete support.
- GeoRobotix planning probe: `/conformance` declares `/conf/create-replace-delete`, `/conf/geojson`, and `/conf/sensorml`; `OPTIONS /systems` and `OPTIONS /systems/0mqcvdnfoca0` advertise POST/PUT/DELETE.
- Implementation: `EncodingMediatypeWrite` helper plus GeoJSON and SensorML runtime tests; helper unit coverage prevents public-IUT mutation, status-only PASS, wrong-identity PASS, non-exact media type drift, and OSH-compatible GeoJSON body drift.
- Verification: formatter BUILD SUCCESS; Maven r3 BUILD SUCCESS with `144 tests / 0 failures / 0 errors / 3 skipped`; GeoRobotix TeamEngine smoke r3 `89 total / 55 passed / 0 failed / 34 skipped`.
- No-mutation proof: GeoRobotix smoke recognized 69 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH entries; both mediatype-write lifecycle tests SKIP before mutation by default.
- Local mutable-IUT proof: authenticated local OSH smoke r3 reported `89 total / 52 passed / 4 failed / 33 skipped`; both Sprint 19 mediatype-write tests PASSed with exact `Content-Type=application/geo+json` and `Content-Type=application/sml+json`, follow-up GET, and cleanup DELETE request-log evidence. The four local SensorML deployment/procedure HTTP 500 failures were outside Sprint 19 and were later fixed during the Sprint 26 seed repair.
- Raze planning review `.harness/evaluations/sprint-ets-19-plan-adversarial.yaml` returned `GAPS_FOUND` confidence 0.88 for a missing SensorML OpenSpec scenario body.
- Raze gap-fix review `.harness/evaluations/sprint-ets-19-plan-gapfix.yaml` returned `APPROVE` confidence 0.95 after adding `SCENARIO-ETS-PART1-013-SENSORML-MEDIATYPE-WRITE-SAFETY-GATED-001`.
- Raze implementation follow-up gapfix `.harness/evaluations/sprint-ets-19-adversarial-followup-gapfix.yaml` returned `APPROVE` confidence 0.94 after r3 reconciliation updates.
- Next action: start the next sprint item from pushed commit `4bdc930`.

## Sprint ets-18 Generator Evidence

Encoding relation-types breadth read-only checks:

- Story: `epics/stories/s-ets-18-01-encoding-relation-types-breadth-readonly.md`
- Contract: `.harness/contracts/sprint-ets-18.yaml`
- OpenSpec: extends `REQ-ETS-PART1-012` and `REQ-ETS-PART1-013`; both remain PARTIAL.
- Scope implemented: independent relation-types assertions for selected GeoJSON System/Deployment/Procedure/SamplingFeature and SensorML System/Deployment/Procedure resources.
- Out of scope: GeoJSON/SensorML `mediatype-write`, mutation behavior, full schema validation, Part 2, property GeoJSON mapping, and property-level `@link` relation-types PASS evidence.
- Implementation: `GeoJsonTests` adds Deployment, Procedure, and Sampling Feature relation-types checks; `SensorMlTests` adds Deployment and Procedure relation-types checks; `VerifyEncodingRelationTypes` adds 3 Sprint 18 breadth regressions.
- Maven: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `136 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-18-maven-2026-05-07.log`.
- TeamEngine smoke: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s18-generator bash scripts/smoke-test.sh`, result `87 total / 55 passed / 0 failed / 32 skipped`.
- Smoke no-mutation oracle: recognized 69 IUT-bound request-log entries and zero IUT-bound POST/PUT/DELETE/PATCH entries for GeoRobotix.
- Runtime outcome: GeoJSON System relation-types PASSed; GeoJSON Deployment, Procedure, and Sampling Feature SKIPped independently; SensorML System, Deployment, and Procedure SKIPped independently.
- Raze implementation review `.harness/evaluations/sprint-ets-18-adversarial-implementation.yaml` returned `APPROVE` confidence 0.92 with no required fixes.
- Historical note: Sprint 18 Generator was committed as `81b7dba`.

## Sprint ets-17 Generator Evidence

Encoding relation-types read-only link checks:

- Story: `epics/stories/s-ets-17-01-encoding-relation-types-readonly.md`
- Contract: `.harness/contracts/sprint-ets-17.yaml`
- OpenSpec: extends `REQ-ETS-PART1-012` and `REQ-ETS-PART1-013`; both remain PARTIAL.
- Scope implemented: selected-resource `/req/geojson/relation-types` and `/req/sensorml/relation-types` checks for associations encoded in JSON `links` members.
- Out of scope: GeoJSON/SensorML `mediatype-write`, mutation behavior, full schema validation, Part 2, and property-level `@link` mapping checks beyond existing mapping assertions.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-06 and not stale.
- OGC source verification: `api/part1/standard/sections/clause_20_requirements_class_geojson_encoding.adoc` and `api/part1/standard/sections/clause_21_requirements_class_sensorml_encoding.adoc` fetched HTTP 200 on 2026-05-06. Both clauses state that associations encoded in `links` must use the association name as the link relation type.
- GeoRobotix planning probe: `/systems/0mqcvdnfoca0` has links with generic `canonical`/`alternate` rels plus association rels `samplingFeatures` and `datastreams`; the association links already use association-name rels.
- GeoRobotix planning probe: `/deployments/16sp744ch58g` and `/procedures/164p7ed8l47g` expose only generic `canonical` and `alternate` links in `links`; deployment `deployedSystems@link` is under `properties`, not `links`.
- GeoRobotix planning probe: `/samplingFeatures/0mtff3l0oofg` has no `links` member; `hostedProcedure@link` is under `properties`, not `links`.
- GeoRobotix planning probe: observed SensorML system/deployment/procedure bodies did not expose links-member association links, so SensorML relation-types checks may SKIP honestly on this IUT until such links exist.
- Implemented verdict policy: PASS only when every detected links-member association uses a `rel` valid for the selected encoding and resource type; SKIP when no links-member association exists; FAIL when a links-member association URL is present but `rel` is missing, generic, not the association name, or valid only for another resource type.
- Resource-specific allowlists: GeoJSON System permits `parentSystem`, `subsystems`, `samplingFeatures`, `deployments`, `procedures`, `datastreams`, and `controlstreams`; GeoJSON Deployment permits `parentDeployment`, `subdeployments`, `featuresOfInterest`, `samplingFeatures`, `datastreams`, and `controlstreams`; GeoJSON Procedure permits `implementingSystems`; GeoJSON Sampling Feature permits `parentSystem`, `sampleOf`, `datastreams`, and `controlstreams`. SensorML System excludes `parentSystem` because it maps to `attachedTo`, not `links`; SensorML has no Sampling Feature representation.
- Raze planning review `.harness/evaluations/sprint-ets-17-plan-adversarial.yaml` returned `GAPS_FOUND` confidence 0.88 for a global association-name allowlist false PASS risk.
- Implementation: `EncodingRelationTypes` centralizes resource-specific allowlists; `GeoJsonTests` adds `geoJsonLinksMemberAssociationRelsUseResourceSpecificNames`; `SensorMlTests` adds `sensorMlLinksMemberAssociationRelsUseResourceSpecificNames`; `VerifyEncodingRelationTypes` adds 5 helper regressions.
- Maven: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `133 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-17-maven-2026-05-06.log`.
- TeamEngine smoke: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s17-generator bash scripts/smoke-test.sh`, result `82 total / 55 passed / 0 failed / 27 skipped`.
- Smoke no-mutation oracle: recognized 55 IUT-bound request-log entries and zero IUT-bound POST/PUT/DELETE/PATCH entries for GeoRobotix.
- Runtime outcome: GeoJSON relation-types PASSed on selected System links; SensorML relation-types SKIPped because the selected SensorML system representation has no top-level links-member association links.
- Raze gap-fix review `.harness/evaluations/sprint-ets-17-plan-gapfix.yaml` returned `APPROVE` confidence 0.94 with no remaining required fixes.
- Raze implementation review `.harness/evaluations/sprint-ets-17-adversarial-implementation.yaml` returned `APPROVE` confidence 0.91 with no required fixes.
- Historical note: Sprint 18 planning has started.

## Sprint ets-18 Planning Evidence

Encoding relation-types breadth read-only checks:

- Story: `epics/stories/s-ets-18-01-encoding-relation-types-breadth-readonly.md`
- Contract: `.harness/contracts/sprint-ets-18.yaml`
- OpenSpec: extends `REQ-ETS-PART1-012` and `REQ-ETS-PART1-013`; both remain PARTIAL.
- Scope planned: broaden Sprint 17 relation-types checks across selected GeoJSON System/Deployment/Procedure/SamplingFeature and SensorML System/Deployment/Procedure resources.
- Out of scope: GeoJSON/SensorML `mediatype-write`, mutation behavior, full schema validation, Part 2, property GeoJSON mapping, and property-level `@link` mapping checks beyond existing mapping assertions.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-07 and not stale.
- OGC source verification: `api/part1/standard/sections/clause_20_requirements_class_geojson_encoding.adoc` and `api/part1/standard/sections/clause_21_requirements_class_sensorml_encoding.adoc` fetched HTTP 200 on 2026-05-07. Both clauses state that associations encoded in `links` must use the association name as the link relation type.
- GeoRobotix planning probe: `/systems/0mqcvdnfoca0` has links with generic `canonical`/`alternate` rels plus association rels `samplingFeatures` and `datastreams`; this remains positive GeoJSON System evidence.
- GeoRobotix planning probe: `/deployments/16sp744ch58g` and `/procedures/164p7ed8l47g` expose only generic `canonical` and `alternate` links in `links`.
- GeoRobotix planning probe: `/samplingFeatures/0mtff3l0oofg` has no `links` member; `hostedProcedure@link` is under `properties`, not `links`.
- GeoRobotix planning probe: observed SensorML system/deployment/procedure bodies expose no top-level `links` member.
- Planned verdict policy: each encoding/resource pair must PASS, FAIL, or SKIP independently; the existing GeoJSON System PASS cannot hide non-system or SensorML SKIPs.
- Raze planning review `.harness/evaluations/sprint-ets-18-plan-adversarial.yaml` returned `APPROVE` confidence 0.92 with no required fixes.
- Historical note: Sprint 18 planning was committed as `41bf9e9`.

## Sprint ets-16 Generator Evidence

SensorML non-system read-only expansion:

- Story: `epics/stories/s-ets-16-01-sensorml-non-system-readonly-expansion.md`
- Contract: `.harness/contracts/sprint-ets-16.yaml`
- OpenSpec: extends `REQ-ETS-PART1-013`; status remains PARTIAL-IMPLEMENTED.
- Scope implemented: deployment, procedure, and property SensorML schema/mapping checks using read-only GET requests.
- Out of scope: `/req/sensorml/mediatype-write`, `/req/sensorml/relation-types`, full external SensorML 3.0 schema validation, mutation-side behavior, sampling feature SensorML claims, GeoJSON, Part 2, and any mutation request.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-06 and not stale.
- OGC source verification: `api/part1/standard/requirements/encoding/sensorml/requirements_class_sensorml.adoc` fetched HTTP 200 on 2026-05-06; upstream subrequirements list deployment/procedure/property schema and mapping paths.
- GeoRobotix probes: `/conformance` declares `/conf/sensorml`, `/conf/deployment`, `/conf/procedure`, and `/conf/property`.
- GeoRobotix fallback state: collection `Accept: application/sml+json` requests for `/deployments`, `/procedures`, and `/properties` returned `Content-Type: application/json` CS API wrappers; those wrappers must SKIP rather than PASS SensorML assertions.
- Positive item evidence: `/deployments/16sp744ch58g?f=sml3` returned SensorML JSON with `type=Deployment`, matching identity, and `deployedSystems`; `/procedures/164p7ed8l47g?f=sml3` returned SensorML JSON with `type=PhysicalSystem`, matching identity, and procedure structure.
- Implementation: `SensorMlTests` now has 9 @Tests, adding deployment, procedure, and property read-only SensorML checks with per-resource `/conf/deployment`, `/conf/procedure`, and `/conf/property` gating.
- Resource-specific predicates: deployment requires explicit SensorML `type=Deployment`, preserved identity, and non-empty `deployedSystems`; procedure requires explicit SensorML procedure-compatible type, preserved identity, and non-identity process/procedure structure beyond identifiers; property requires explicit property-compatible SensorML and id/uniqueId/definition/identifier mapping evidence when a property item exists.
- Current IUT state: `/properties` is empty, so `propertySensorMlHasSchemaAndMapping` SKIPs honestly until an IUT supplies a property item.
- Unit coverage: `VerifySensorMlResourceMappingAssertions` adds 6 helper regression tests for empty collection SKIP, first item extraction, identifiers-only procedure rejection, procedure structure acceptance, property evidence, and non-empty mapping values.
- Maven: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `128 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-16-maven-2026-05-06.log`.
- TeamEngine smoke: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s16-generator bash scripts/smoke-test.sh`, result `80 total / 54 passed / 0 failed / 26 skipped`.
- Smoke no-mutation oracle: recognized 53 IUT-bound request-log entries and zero IUT-bound POST/PUT/DELETE/PATCH entries for GeoRobotix.
- Runtime outcome: deployment and procedure SensorML checks PASS through `application/sml+json` alternate links; property SensorML check SKIPs because GeoRobotix `/properties` is empty.
- Raze planning review `.harness/evaluations/sprint-ets-16-plan-adversarial.yaml` returned `GAPS_FOUND` confidence 0.86. Required fixes applied in planning: added resource conformance-class gating for `/conf/deployment`, `/conf/procedure`, and `/conf/property`; tightened procedure mapping so `identifiers` alone cannot satisfy procedure-specific SensorML evidence.
- Raze planning gap-fix `.harness/evaluations/sprint-ets-16-plan-gapfix.yaml` returned `APPROVE` confidence 0.94 with no remaining required fixes.
- Raze implementation review `.harness/evaluations/sprint-ets-16-adversarial-implementation.yaml` returned `APPROVE` confidence 0.92 with no required fixes.
- Historical note: Sprint 16 Generator was committed as `72820e3`.

## Sprint ets-15 Generator Evidence

GeoJSON non-system read-only expansion:

- Story: `epics/stories/s-ets-15-01-geojson-non-system-readonly-expansion.md`
- Contract: `.harness/contracts/sprint-ets-15.yaml`
- OpenSpec: extends `REQ-ETS-PART1-012`; status remains PARTIAL-IMPLEMENTED.
- Scope implemented: deployment, procedure, and sampling feature GeoJSON schema/mapping checks using read-only collection requests.
- Out of scope: `/req/geojson/mediatype-write`, `/req/geojson/relation-types`, property GeoJSON mapping, full external GeoJSON schema validation, SensorML, Part 2, and any mutation request.
- Architecture freshness: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-06 and not stale.
- OGC source: `api/part1/standard/requirements/encoding/geojson/requirements_class_geojson.adoc`, fetched HTTP 200 on 2026-05-06. Relevant subrequirements are `/req/geojson/deployment-schema`, `/req/geojson/deployment-mappings`, `/req/geojson/procedure-schema`, `/req/geojson/procedure-mappings`, `/req/geojson/sf-schema`, and `/req/geojson/sf-mappings`.
- GeoRobotix planning probe: `/conformance` returned HTTP 200 and declares `/conf/geojson`, `/conf/deployment`, `/conf/procedure`, and `/conf/sf`.
- GeoRobotix non-system GeoJSON probe: `GET /deployments?limit=1`, `/procedures?limit=1`, and `/samplingFeatures?limit=1` with `Accept: application/geo+json` all returned HTTP 200 with `Content-Type: application/json` and top-level `items`; this is fallback evidence, not GeoJSON FeatureCollection PASS evidence.
- Implementation: `GeoJsonTests` now has 8 @Tests, adding `/deployments`, `/procedures`, and `/samplingFeatures` read-only GeoJSON checks. CS API default `items` wrappers without GeoJSON `features` SKIP with reason, not PASS schema/mapping assertions.
- Resource-specific predicates: deployment mapping checks `properties.deployedSystems@link`; procedure mapping checks `geometry == null` plus `properties.featureType`; sampling feature mapping checks `properties.featureType` plus `properties.hostedProcedure@link` or `properties.radius`. Generic Feature shape alone is not enough for schema/mapping PASS.
- Unit coverage: `VerifyGeoJsonResourceMappingAssertions` adds 5 helper regression tests for fallback SKIP and mapping-value behavior.
- Maven: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `122 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-15-maven-2026-05-06.log`.
- TeamEngine smoke: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s15-generator bash scripts/smoke-test.sh`, result `77 total / 52 passed / 0 failed / 25 skipped`.
- Smoke no-mutation oracle: recognized 44 IUT-bound request-log entries and zero IUT-bound POST/PUT/DELETE/PATCH entries for GeoRobotix.
- Raze planning review: `.harness/evaluations/sprint-ets-15-plan-adversarial.yaml` returned `GAPS_FOUND` confidence 0.86; required fix was to add resource-specific schema/mapping predicates.
- Raze planning gap-fix recheck: `.harness/evaluations/sprint-ets-15-plan-gapfix.yaml` returned `APPROVE` confidence 0.93 with no remaining required fixes.
- Raze implementation review: `.harness/evaluations/sprint-ets-15-adversarial-implementation.yaml` returned `APPROVE_WITH_CONCERNS` confidence 0.91 for stale class javadoc only.
- Raze gap-fix recheck: `.harness/evaluations/sprint-ets-15-adversarial-gapfix.yaml` returned `APPROVE` confidence 0.96 with no remaining required fixes or concerns.

## Sprint ets-14 Planning

Update positive mutable-IUT hardening:

- Story: `epics/stories/s-ets-14-01-update-positive-mutable-iut-hardening.md`
- Contract: `.harness/contracts/sprint-ets-14.yaml`
- OpenSpec: extends `REQ-ETS-PART1-011`; status remains PARTIAL unless positive PATCH executes and verifies a changed field.
- Scope planned: correct Update source-path citation to `requirements/crud/update`, add an OPTIONS/PATCH verdict matrix, require GET-after-PATCH changed-field assertion for `properties.name`, add focused unit coverage for status-only PATCH false positives, and record local OSH readiness honestly.
- OPTIONS/PATCH verdict matrix: missing `/conf/update`, absent mutation opt-in, public-IUT hard denial, no candidate System, or inconclusive OPTIONS are SKIP-before-PATCH states; declared `/conf/update` plus successful OPTIONS omitting PATCH FAILs readiness for `/req/update/system`, while lifecycle still SKIPs before PATCH; declared `/conf/update` plus explicit mutation opt-in plus `Allow: PATCH` may run guarded PATCH and must assert the changed field.
- Local OSH planning probe: `OPTIONS /systems/040g` returned HTTP 200 with `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS`; PATCH absent. Simple authenticated `/conformance` curl returned HTTP 401 with attempted basic credentials, so TeamEngine smoke credential path remains the authoritative local path.
- Guardrail: do not claim local OSH positive Update support without observed `/conf/update`, `OPTIONS PATCH`, and changed-field evidence. Default GeoRobotix smoke must still issue zero IUT-bound PATCH.
- Raze planning review: `.harness/evaluations/sprint-ets-14-plan-adversarial.yaml` returned `GAPS_FOUND` confidence 0.87; required fixes applied.
- Raze planning gap-fix review: `.harness/evaluations/sprint-ets-14-plan-gapfix.yaml` returned `APPROVE` confidence 0.93 with no remaining required fixes.
- Implementation: `UpdateTests.systemsPatchLifecycleOptIn` now requires GET after PATCH and asserts `properties.name` equals the intended patched value; `VerifyUpdateChangedFieldAssertion` adds four focused unit tests.
- Maven: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `117 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-14-maven-2026-05-06.log`.
- TeamEngine smoke: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s14-generator bash scripts/smoke-test.sh`, result `74 total / 52 passed / 0 failed / 22 skipped`.
- Smoke no-mutation oracle: recognized 41 IUT-bound request-log entries and zero IUT-bound POST/PUT/DELETE/PATCH entries for GeoRobotix.
- Local OSH readiness probe: `/conformance` HTTP 401; `OPTIONS /systems/040g` HTTP 200 with `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS`; PATCH absent. No local OSH PATCH was issued.
- Raze implementation review: `.harness/evaluations/sprint-ets-14-adversarial-implementation.yaml` returned `GAPS_FOUND` confidence 0.84 for missing REQ/SCENARIO trace comments in `VerifyUpdateChangedFieldAssertion`.
- Raze gap-fix recheck: `.harness/evaluations/sprint-ets-14-adversarial-gapfix.yaml` returned `APPROVE` confidence 0.94 with no remaining required fixes.

## Sprint ets-13 Generator Evidence

Update/PATCH safety-gated systems subset:

- Story: `epics/stories/s-ets-13-01-update-safety-gated-systems-subset.md`
- Contract: `.harness/contracts/sprint-ets-13.yaml`
- OpenSpec: `REQ-ETS-PART1-011`, status PARTIAL-IMPLEMENTED for Sprint 13
- Scope implemented: declaration-gated `/conf/update`, reuse Sprint 12 mutation opt-in parameters, non-mutating `OPTIONS /systems/{id}` PATCH readiness, default lifecycle SKIP-before-PATCH, public GeoRobotix hard-denial, Update -> CreateReplaceDelete dependency wiring, and PATCH-aware no-mutation smoke oracle
- Explicitly excluded: unguarded PATCH against GeoRobotix, deployment/procedure/sampling-feature/property PATCH, Feature Collection update paths from OGC ATS A.79-A.83, Part 2 `/conf/update`, optimistic locking, and PATCH media-type matrix including JSON Patch, merge patch, and content negotiation
- Corrected story ID: prior epic placeholder reused `S-ETS-07-03`; Sprint 13 planning corrected the Update story to `S-ETS-13-01`.
- Architecture freshness: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-06 and not stale.
- OGC source: OGC API - Connected Systems Part 1 Clause 17, Requirements Class "Update" `/req/update`; upstream source path `api/part1/standard/requirements/crud/update/requirements_class_update.adoc`; prerequisite `/req/create-replace-delete`; systems endpoint `{api_root}/systems/{id}` uses HTTP PATCH.
- GeoRobotix planning probe: `/conformance` does not declare `/conf/update`; `OPTIONS /systems/0mqcvdnfoca0` returns HTTP 200 with `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS` and no PATCH.
- Local OSH planning probe: unauthenticated `/conformance` returns HTTP 401; unauthenticated `OPTIONS /systems/040g` returns HTTP 200 with no PATCH in `Allow`.
- Planning interpretation: Sprint 13 should implement skip-first safety and wiring. Current default/public IUT evidence supports no positive PATCH conformance claim.
- Raze planning review: `.harness/evaluations/sprint-ets-13-plan-adversarial.yaml` verdict `APPROVE_WITH_CONCERNS` confidence 0.88. Two planning tightenings were applied: contract media-type exclusions now match story/spec, and OGC ATS A.79-A.83 collection item update paths are cited as deferred.
- Implementation: `UpdateTests.java` added with 5 `update` @Tests; `testng.xml` declares `<group name="update" depends-on="createreplacedelete"/>`; `VerifyTestNGSuiteDependency` adds 3 Update lint tests.
- No-mutation oracle: `scripts/no-mutation-oracle.py` and `scripts/smoke-test.sh` now treat PATCH as mutating alongside POST, PUT, and DELETE.
- Maven: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `113 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-13-maven-2026-05-06.log`.
- TeamEngine smoke: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results bash scripts/smoke-test.sh`, result `74 total / 52 passed / 0 failed / 22 skipped`.
- Smoke no-mutation oracle: integrated smoke oracle recognized 41 IUT-bound request-log entries and zero IUT-bound POST/PUT/DELETE/PATCH entries for `https://api.georobotix.io/ogc/t18/api`.
- Runtime note: default GeoRobotix Update config records missing `/conf/update`, then the five Update @Tests are dependency-skipped because the Update group depends on the default-skipped Create/Replace/Delete mutation safety gate. No PATCH was issued.
- Raze implementation review: `.harness/evaluations/sprint-ets-13-adversarial-implementation.yaml` verdict `GAPS_FOUND` confidence 0.86; required documentation/evidence fixes applied. Code safety, no-mutation oracle, TestNG dependency, OGC URI/scope fidelity, and unrelated dirty-file checks were acceptable.
- Raze gap-fix recheck: `.harness/evaluations/sprint-ets-13-adversarial-gapfix.yaml` verdict `APPROVE` confidence 0.91; no required fixes remain.
- Quinn Gate 3.5: `.harness/evaluations/sprint-ets-13-evaluator-gate.yaml` verdict `APPROVE_WITH_CONCERNS` confidence 0.91. Independent `/tmp` clone Maven reported `113 tests / 0 failures / 0 errors / 3 skipped`; independent TeamEngine smoke reported `74 total / 52 passed / 0 failed / 22 skipped`, with 41 recognized IUT-bound request-log entries and zero IUT-bound POST/PUT/DELETE/PATCH. Concerns are positive PATCH coverage pending a dedicated mutable IUT and stronger patched-field assertion before future promotion beyond PARTIAL.
- Raze review of Quinn artifact: `.harness/evaluations/sprint-ets-13-quinn-gate-raze-review.yaml` verdict `APPROVE` confidence 0.89 after correcting the Quinn artifact's sprint file list.
- Raze Gate 4: `.harness/evaluations/sprint-ets-13-adversarial-gate.yaml` verdict `APPROVE_WITH_CONCERNS` confidence 0.90. Independent `/tmp` clone Maven reported `113 tests / 0 failures / 0 errors / 3 skipped`; no-mutation oracle self-test passed; independent TeamEngine smoke reported `74 total / 52 passed / 0 failed / 22 skipped`, with 41 recognized IUT-bound request-log entries and zero IUT-bound POST/PUT/DELETE/PATCH. No required fixes. Low follow-up: decide whether missing `OPTIONS Allow: PATCH` should fail, skip, or be supplemented before positive mutable-IUT Update gates.

Sprint 13 guardrails:

- Default TeamEngine smoke MUST NOT issue IUT-bound PATCH.
- PATCH lifecycle assertions must SKIP before PATCH unless `mutation-tests-enabled=true` and `mutation-iut-policy=dedicated-mutable-iut`.
- Even when mutation parameters are present, known shared public GeoRobotix URLs are hard-denied before PATCH.
- OPTIONS checks may PASS only as non-mutating ETS readiness evidence, not OGC update conformance.
- No-mutation smoke proof must treat PATCH as a mutating method alongside POST, PUT, and DELETE.
- Do not promote REQ-ETS-PART1-011 beyond PARTIAL-IMPLEMENTED after this sprint.
- Do not implement Part 2 `/conf/update`.

## Sprint ets-12 Generator Evidence

Create/Replace/Delete safety-gated systems subset:

- Story: `epics/stories/s-ets-12-01-create-replace-delete-safety-gated.md`
- Contract: `.harness/contracts/sprint-ets-12.yaml`
- OpenSpec: `REQ-ETS-PART1-010`, status PARTIAL-IMPLEMENTED for Sprint 12
- Scope implemented: declaration-gated `/conf/create-replace-delete`, explicit mutation opt-in parameters, OPTIONS readiness preconditions, default lifecycle SKIP-before-POST, public GeoRobotix hard-denial, IUT-bound no-mutation log oracle, and `createreplacedelete -> systemfeatures` dependency wiring
- Explicitly excluded: unguarded mutation against GeoRobotix, deployments/subdeployments/procedures/sampling-features/properties CRUD, system delete cascade, custom collection propagation, `text/uri-list`, update/PATCH, and Part 2
- GeoRobotix runtime state: `/conformance` declares `/conf/create-replace-delete`; `OPTIONS /systems` and `OPTIONS /systems/0mqcvdnfoca0` advertise POST/PUT/DELETE; this is readiness evidence only and is not permission to mutate the public smoke target
- Maven: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `105 tests / 0 failures / 0 errors / 3 skipped`
- TeamEngine smoke: `/tmp/sprint-ets-12-generator-smoke-current-r3`, command `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s12-generator-r3 bash scripts/smoke-test.sh`, result `69 total / 52 passed / 0 failed / 17 skipped`
- CreateReplaceDelete runtime outcome against GeoRobotix: 4 PASS and 2 SKIP. The two SKIPs are the expected default safety gate and lifecycle opt-in checks.
- Smoke no-mutation oracle: integrated smoke oracle recognized 40 IUT-bound request log entries and zero IUT-bound POST/PUT/DELETE entries for `https://api.georobotix.io/ogc/t18/api`
- Raze implementation review: `.harness/evaluations/sprint-ets-12-adversarial-implementation.yaml` verdict `GAPS_FOUND` confidence 0.88; GAP-001 and GAP-002 plus low-risk Allow parsing concern were fixed same turn.
- Raze gap-fix review: `.harness/evaluations/sprint-ets-12-adversarial-gapfix.yaml` verdict `APPROVE_WITH_CONCERNS` confidence 0.91; no required fixes remain. Residual concerns: smoke stdout is not archived separately, and positive mutable-IUT lifecycle evidence remains future work.
- Local OSH mutable-IUT follow-up: existing OSH 2.0-beta2 stack in `../sar-ops/field-hub` runs at `http://localhost:8081/sensorhub/api`; TeamEngine reaches it through Docker network `field-hub_default` as `http://field-hub-osh-1:8081/sensorhub/api`.
- Local OSH fixes from probe: service-relative `Location: /systems/{id}` now resolves against the IUT service base; the replacement body preserves the created System `uid` so OSH accepts PUT.
- Local OSH verification: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `110 tests / 0 failures / 0 errors / 3 skipped`; mutable smoke `/tmp/ets-csapi-osh-mutable-smoke-r4` reported `69 total / 32 passed / 3 failed / 34 skipped`, with `systemsCreateReplaceDeleteLifecycle` PASS and real POST, PUT, DELETE observed.
- Local OSH fixture follow-up: `../sar-ops/field-hub/osh/config/config.json` `proxyBaseUrl` is set to `http://field-hub-osh-1:8081`, and the OSH H2 datastore now contains synthetic `/systems/040g`, `/procedures/040g`, `/deployments/040g`, and `/samplingFeatures/040g` seed resources. Exact payloads are versioned in `ops/local-osh-seed-fixtures.json`. The System seed uses `featureType=http://www.w3.org/ns/sosa/System`, which makes `/systems/040g?f=sml3` return local `application/sml+json`.
- Local OSH full-health verification: `/tmp/ets-csapi-osh-full-health-r3` with explicit mutable-IUT parameters reported `69 total / 50 passed / 0 failed / 19 skipped`; corrected smoke stdout printed `SMOKE PASS: total=69 passed=50 failed=0 skipped=19 ...`. The 19 skips are expected for undeclared/unpopulated out-of-scope surfaces, not failed health checks.
- Raze local OSH full-health review: `.harness/evaluations/sprint-ets-12-local-osh-full-health-raze.yaml` verdict `GAPS_FOUND` confidence 0.87. Required fixes applied: smoke stdout now prints exact parsed totals instead of `${total}/${total}`, and the seed payloads are versioned.
- Raze local OSH full-health gap-fix review: `.harness/evaluations/sprint-ets-12-local-osh-full-health-gapfix-raze.yaml` verdict `APPROVE` confidence 0.92; no required fixes remain.

Sprint 12 Generator guardrails:

- Default TeamEngine smoke MUST NOT issue IUT-bound POST, PUT, or DELETE from the Create/Replace/Delete suite.
- Lifecycle mutation assertions must SKIP before POST unless `mutation-tests-enabled=true` and `mutation-iut-policy=dedicated-mutable-iut`.
- Even when mutation parameters are present, known shared public GeoRobotix URLs are hard-denied before POST/PUT/DELETE.
- OPTIONS checks may PASS only as non-mutating ETS readiness evidence, not OGC lifecycle conformance.
- No-mutation smoke proof uses recognized REST Assured `Request: METHOD URI` entries and adjacent `Request method:` + `Request URI:` pairs filtered to the IUT base URL; TeamEngine control-plane POST is excluded.
- Do not promote REQ-ETS-PART1-010 beyond PARTIAL-IMPLEMENTED after this sprint.
- Do not implement `/conf/update` until the CRD safety gate is in place.

Raze planning review:

- Artifact: `.harness/evaluations/sprint-ets-12-plan-adversarial.yaml`
- Verdict: `GAPS_FOUND` confidence 0.87
- Required before Generator: separate OPTIONS readiness from OGC CRD lifecycle conformance; specify full mutation opt-in plumbing plus hard denial for public GeoRobotix; define an IUT-bound log oracle for no-mutation smoke evidence; reconcile stale Sprint 11 traceability/status drift
- Gap-fix review: `.harness/evaluations/sprint-ets-12-plan-gapfix.yaml` verdict `GAPS_FOUND` confidence 0.84. GAP-001, GAP-002, and GAP-004 are closed; GAP-003 was partial because one OpenSpec acceptance-scenario line still required no POST/PUT/DELETE anywhere in the container log instead of the IUT-bound request-log oracle.
- Final wording fix recheck: `.harness/evaluations/sprint-ets-12-plan-gapfix-2.yaml` verdict `APPROVE` confidence 0.93. OpenSpec now consistently uses the IUT-bound adjacent `Request method:` + `Request URI:` oracle, excludes TeamEngine control-plane POST, and the story broad scope sentence uses `IUT-bound`.

## Sprint ets-11 Plan

AdvancedFiltering systems/common-resource read-only subset:

- Story: `epics/stories/s-ets-11-01-advanced-filtering-readonly.md`
- Contract: `.harness/contracts/sprint-ets-11.yaml`
- OpenSpec: `REQ-ETS-PART1-009`, status PARTIAL-IMPLEMENTED after Sprint 11 Generator
- Scope: declaration-gated `/conf/advanced-filtering`, local ID_List helper, `/systems?id=...`, `/systems?q=...`, `/systems?geom=...` smoke shape, and `advancedfiltering -> systemfeatures` dependency wiring
- Explicitly excluded: create-replace-delete, update, Part 2, full association filters, full geometry intersection semantics, combined-filter truth tables, and endpoint parity across every resource type
- GeoRobotix planning state: `/conformance` does not currently declare `/conf/advanced-filtering`; undeclared read-only query behavior is planning evidence only, not conformance PASS evidence

Sprint 11 Generator guardrails:

- Raze planning review gaps were addressed by making ID/keyword filters non-vacuous after seed selection, adding explicit ID_List examples, and separating dependency evidence from default smoke totals.
- Raze gap-fix review: `.harness/evaluations/sprint-ets-11-plan-gapfix.yaml` APPROVE 0.92, static-only per instruction.
- Re-verify `/conformance` before implementing.
- All AdvancedFiltering tests must SKIP-with-reason when `/conf/advanced-filtering` is absent.
- Do not add POST/PUT/PATCH/DELETE requests.
- Do not promote REQ-ETS-PART1-009 beyond PARTIAL-IMPLEMENTED after this sprint.

## Sprint ets-11 Generator Evidence

AdvancedFiltering systems/common-resource read-only subset:

- `AdvancedFilteringTests.java` added with 6 read-only @Tests.
- `testng.xml` wires `<group name="advancedfiltering" depends-on="systemfeatures"/>`.
- `VerifyTestNGSuiteDependency` adds 3 AdvancedFiltering lint tests.
- Current GeoRobotix `/conformance` does not declare `/conf/advanced-filtering`; all 6 AdvancedFiltering @Tests SKIP-with-reason in default smoke.
- No POST/PUT/PATCH/DELETE calls were introduced.

Verification:

- Java formatter via Docker Maven - BUILD SUCCESS
- `bash scripts/mvn-test-via-docker.sh` - BUILD SUCCESS, `98 tests / 0 failures / 0 errors / 3 skipped`
- TeamEngine smoke from `/tmp/sprint-ets-11-generator-smoke` - `63 total / 48 passed / 0 failed / 15 skipped`
- Smoke report: `/tmp/sprint-ets-11-generator-smoke-results/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
- Container log: `/tmp/sprint-ets-11-generator-smoke-results/s-ets-01-03-teamengine-container-2026-05-05.log`
- Raze Gate 4: `.harness/evaluations/sprint-ets-11-adversarial-gate.yaml` APPROVE_WITH_CONCERNS 0.90; independent Maven from the worktree BUILD SUCCESS `98 tests / 0 failures / 0 errors / 3 skipped`; independent TeamEngine smoke from `/tmp/raze-sprint-ets-11` with `SMOKE_CONTAINER_NAME=raze-ets-csapi-smoke-s11` reported `63 total / 48 passed / 0 failed / 15 skipped`. All 6 AdvancedFiltering @Tests SKIP-with-reason because GeoRobotix does not declare `/conf/advanced-filtering`.
- Quinn Gate 3.5: `.harness/evaluations/sprint-ets-11-evaluator-gate.yaml` APPROVE_WITH_CONCERNS 0.90; independent Maven from `/tmp/quinn-sprint-ets-11-gate` BUILD SUCCESS `98 tests / 0 failures / 0 errors / 3 skipped` with log `/tmp/quinn-ets-csapi-mvn-s11.log` after one transient worktree surefire scan/load failure; independent TeamEngine smoke from `/tmp/quinn-sprint-ets-11-gate` with `SMOKE_CONTAINER_NAME=quinn-ets-csapi-smoke-s11` reported `63 total / 48 passed / 0 failed / 15 skipped`. All 6 AdvancedFiltering @Tests SKIP-with-reason because GeoRobotix does not declare `/conf/advanced-filtering`.

## Sprint ets-10 Evidence

SensorML systems read-only subset:

- `SensorMlTests.java` added with 6 read-only @Tests
- `testng.xml` wires `<group name="sensorml" depends-on="systemfeatures"/>`
- VerifyTestNGSuiteDependency adds 3 SensorML lint tests
- Full REQ-ETS-PART1-013 remains open for `mediatype-write`, `relation-types`, deployment/procedure/property SensorML schema/mapping, and full SensorML 3.0 JSON Schema validation

Verification:

- Generator: Java formatter via Docker Maven - BUILD SUCCESS
- Generator: `bash scripts/mvn-test-via-docker.sh` - BUILD SUCCESS, `95 tests / 0 failures / 0 errors / 3 skipped`
- Generator TeamEngine smoke from `/tmp/sprint-ets-10-generator-smoke-git-r2` - `57 total / 48 passed / 0 failed / 9 skipped`
- Quinn independent Maven from `/tmp/quinn-sprint-ets-10` - BUILD SUCCESS, `95 tests / 0 failures / 0 errors / 3 skipped`; surefire includes the three SensorML lint tests
- Quinn independent TeamEngine smoke with unique container `quinn-ets-csapi-smoke-s10` - `57 total / 48 passed / 0 failed / 9 skipped`
- SensorML runtime - 6 PASS; current GeoRobotix direct item `Accept: application/sml+json` falls back to explicit `application/sml+json` alternate link `https://api.georobotix.io/ogc/t18/api/systems/0mqcvdnfoca0?f=sml3`
- Collection-level `GET /systems` `items` JSON is not counted as SensorML PASS
- Raze implementation review initially found two gaps; both were fixed same-turn.
- Raze gap-fix review: `.harness/evaluations/sprint-ets-10-adversarial-gapfix.yaml` APPROVE 0.93, no final blockers.
- Quinn gate: `.harness/evaluations/sprint-ets-10-evaluator-gate.yaml` APPROVE_WITH_CONCERNS 0.91; no blockers.
- Raze Gate 4: `.harness/evaluations/sprint-ets-10-adversarial-gate.yaml` APPROVE 0.91; independent Maven from `/tmp/raze-sprint-ets-10` BUILD SUCCESS `95 tests / 0 failures / 0 errors / 3 skipped`; independent TeamEngine smoke with `SMOKE_CONTAINER_NAME=raze-ets-csapi-smoke-s10` reported `57 total / 48 passed / 0 failed / 9 skipped`.

## Sprint ets-09 Evidence

GeoJSON systems read-only subset:

- `GeoJsonTests.java` added with 5 read-only @Tests
- `testng.xml` wires `<group name="geojson" depends-on="systemfeatures"/>`
- VerifyTestNGSuiteDependency adds 3 GeoJSON lint tests
- Full REQ-ETS-PART1-012 remains open for `mediatype-write`, `relation-types`, property GeoJSON mapping, and full schema-validation closure; Sprint 15 now implements deployment/procedure/sampling-feature read-only schema/mapping checks.

Verification:

- Generator: `bash scripts/mvn-test-via-docker.sh` — BUILD SUCCESS, `92 tests / 0 failures / 0 errors / 3 skipped`
- Generator TeamEngine smoke from `/tmp/sprint-ets-09-smoke-fix` — `51 total / 42 passed / 0 failed / 9 skipped`
- Quinn independent Maven from `/tmp/quinn-sprint-ets-09` — BUILD SUCCESS, `92 tests / 0 failures / 0 errors / 3 skipped`
- Quinn independent TeamEngine smoke with unique container name — `51 total / 42 passed / 0 failed / 9 skipped`
- Raze independent Maven from `/tmp/raze-sprint-ets-09-review` — BUILD SUCCESS, `92 tests / 0 failures / 0 errors / 3 skipped`
- Raze independent TeamEngine smoke — `51 total / 42 passed / 0 failed / 9 skipped`
- GeoJSON runtime — 2 PASS + 3 SKIP; current GeoRobotix `items` JSON is not counted as GeoJSON PASS

Gate Results:

- `.harness/evaluations/sprint-ets-09-adversarial-implementation.yaml` — GAPS_FOUND 0.86 on mediatype-read overclaim
- `.harness/evaluations/sprint-ets-09-adversarial-gapfix.yaml` — APPROVE 0.94 after `b4a97de`
- `.harness/evaluations/sprint-ets-09-evaluator-gate.yaml` — Quinn APPROVE_WITH_CONCERNS 0.90; no blockers
- `.harness/evaluations/sprint-ets-09-adversarial-gate.yaml` — Raze APPROVE_WITH_CONCERNS 0.88; no required fixes

## Next Action

1. Start Generator for `S-ETS-21-01`.

## Dirty Worktree Notes

Current dirty worktree should contain only post-push status/metrics reconciliation until committed.
