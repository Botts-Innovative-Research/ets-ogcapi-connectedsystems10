# S-ETS-38-01: SimUAV Preseeded Populated-IUT Fixture

## Status
PARTIAL_IMPLEMENTED_E2E_BLOCKED_BY_OSH_LIMITATIONS

## User Instruction
Triggered by: "Proceed with the SimUAV preseeded populated-IUT fixture sprint"

## Scope
Make the local OSH SimUAV populated fixture reproducible and make ETS read-only candidate selection prefer parent resources with associated child evidence before declaring populated Observation/Command evidence unavailable.

- Requirement: `REQ-ETS-PART2-013`
- Scenarios:
  - `SCENARIO-ETS-PART2-013-SIMUAV-PRESEEDED-POPULATED-IUT-001`
  - `SCENARIO-ETS-PART2-013-POPULATED-CANDIDATE-SELECTION-001`
  - `SCENARIO-ETS-PART2-013-SIMUAV-TASKING-FIXTURE-001`
  - `SCENARIO-ETS-PART2-013-POSITIVE-LOCAL-OSH-CLOSURE-001`
  - `SCENARIO-ETS-PART2-013-MUTATION-SAFETY-001`
- ETS target code: Part 2 binding, JSON, SWE Common candidate selection for populated DataStream/Observation and ControlStream/Command evidence.
- Fixture target: local OSH on `field-hub_default` with SimUAV activated only for the dedicated populated preseed run.

## Acceptance Criteria

- [x] OpenSpec records the Sprint 38 SimUAV preseed and populated candidate-selection scenarios before implementation.
- [x] A reproducible fixture probe/preseed step records SimUAV system, DataStream, ControlStream, Command schema, waypoint Command POST, schema content types, and request-method evidence without recording credential values.
- [ ] A reproducible fixture probe/preseed step records parseable Observation child bodies, nested Command collection bodies, dereferenceable CommandStatus, and dereferenceable CommandResult evidence. SimUAV/OSH still returns empty bodies for nested child collections and a POST response command id that is not dereferenceable through `/commands/{id}`.
- [x] ETS candidate selection prefers DataStreams/ControlStreams with parseable scoped child collection evidence over first-item resources whose child collections are empty or unparsable.
- [x] Regression tests reference `REQ-ETS-PART2-013` and Sprint 38 scenarios in comments.
- [x] Strict schema evidence remains unchanged: `Content-Type: auto`, unsupported SWE text schema responses, and other OSH server limitations are not counted as PASS evidence.
- [x] Docker Maven verification for touched ETS modules was completed with a cached Docker Maven equivalent after the required wrapper repeatedly hit Maven Central connection resets.
- [x] Mandatory local OSH TeamEngine smoke was rerun against the SimUAV-preseeded populated IUT; failures caused by OSH limitations are documented rather than hidden.
- [x] Clean primary local OSH state was restored: SimUAV `autoStart=false`, OSH data reset, static `040g` fixtures reseeded, and clean TeamEngine smoke passed.
- [x] Raze reviews non-trivial changes before completion; focused recheck returned `APPROVE_WITH_CONCERNS` with no required fixes.

## Non-Goals

- Do not weaken Annex A.9 JSON or SWE schema validation.
- Do not treat OSH schema `Content-Type: auto` as JSON PASS evidence.
- Do not mutate public IUTs.
- Do not claim full populated binding closure unless TeamEngine and direct evidence prove live parent schema and child body evidence for the applicable requirement.

## Verification Evidence

- Python preseed compile: `python3 -m py_compile scripts/local-osh-simuav-preseed.py` passed.
- SimUAV preseed final evidence: `ops/test-results/sprint-ets-38-local-osh-simuav-preseed-r3-2026-06-03.json`.
  - Result: `PARTIAL_MISSING_OBSERVATION_OR_COMMAND_CHILD_EVIDENCE`.
  - Counts: 2 DataStreams, 3 ControlStreams, 2 DataStream schema endpoints, 3 command schema endpoints, `GET=102`, `POST=1`.
  - Command evidence: selected `inputName=waypoint_feasibility`, POST returned HTTP 200 and `statusCode=COMPLETED`.
  - Missing evidence: zero parseable Observation child items; nested Command collection returned HTTP 200 with empty body.
- ETS Maven verification:
  - Required `bash scripts/mvn-test-via-docker.sh` failed twice before tests on Maven Central `Connection reset`, and a later wrapper retry was stopped after stalling silently.
  - Equivalent Docker Maven run with a reusable host cache passed `294 tests / 0 failures / 0 errors / 3 skipped`; archived as `ops/test-results/sprint-ets-38-maven-clean-test-cached-2026-06-03.log`.
- SimUAV-populated TeamEngine smoke after static reseed:
  - Report: `ops/test-results/sprint-ets-38-simuav-preseed-teamengine-smoke-r3-2026-06-03.xml`.
  - Container log: `ops/test-results/sprint-ets-38-simuav-preseed-teamengine-container-r3-2026-06-03.log`.
  - Result: FAIL, `211 total / 83 passed / 29 failed / 99 skipped`.
  - Failure classes remain schema `Content-Type: auto`, SWE text schema HTTP 400, and empty Observation/Command child body evidence.
- Clean primary restore:
  - Post-Raze field-hub OSH rebuild/reset: `ops/test-results/sprint-ets-38-post-raze-field-hub-osh-rebuild-reset-2026-06-03.log`.
  - Post-Raze reseed: `ops/test-results/sprint-ets-38-post-raze-clean-local-osh-reseed-2026-06-03.json`.
  - Post-Raze root Observation probe: `ops/test-results/sprint-ets-38-post-raze-clean-local-osh-observations-probe-2026-06-03.txt`.
  - Post-Raze smoke report: `ops/test-results/sprint-ets-38-post-raze-clean-local-osh-smoke-2026-06-03.xml`.
  - Post-Raze smoke container log: `ops/test-results/sprint-ets-38-post-raze-clean-local-osh-container-2026-06-03.log`.
  - Result: PASS, `211 total / 68 passed / 0 failed / 143 skipped`, with zero IUT-bound POST/PUT/DELETE/PATCH in default clean smoke.
- Raze review:
  - Initial report: `.harness/evaluations/sprint-ets-38-adversarial-implementation.yaml` returned `GAPS_FOUND` for root Observation assertion coverage and missing Maven artifact.
  - Focused recheck: `.harness/evaluations/sprint-ets-38-adversarial-recheck.yaml` returned `APPROVE_WITH_CONCERNS`, confidence `0.92`, with `required_fixes: []`.
