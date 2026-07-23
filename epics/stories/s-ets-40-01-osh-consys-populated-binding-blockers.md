# S-ETS-40-01: OSH ConSys Populated Binding Blocker Closure

## Status
RETIRED_OUT_OF_SCOPE_HISTORICAL_EVIDENCE_ONLY

> CP-003 and ADR-012 supersede this story. Do not execute or extend its OSH
> source-patch scope. The recorded patch is absent from the current OSH checkout
> and runtime; the evidence below is retained only as project chronology.

## User Instruction
Triggered by: "Do 1"

## Historical Scope
This story patched local OSH ConSys implementation blockers. That work is now
outside project scope and is not an approved implementation path.

- Requirements:
  - `REQ-ETS-PART2-013`
  - `REQ-ETS-TEAMENGINE-006`
- Scenarios:
  - `SCENARIO-ETS-PART2-013-OSH-SCHEMA-MEDIATYPE-001`
  - `SCENARIO-ETS-PART2-013-OSH-SWE-TEXT-MEDIATYPE-001`
  - `SCENARIO-ETS-PART2-013-OSH-COMMAND-SCHEMA-FORMAT-ALIAS-001`
  - `SCENARIO-ETS-PART2-013-POPULATED-CHILD-BODY-JSON-001`
  - `SCENARIO-ETS-PART2-013-SIMUAV-PRESEEDED-POPULATED-IUT-001`
  - `SCENARIO-ETS-PART2-013-POSITIVE-LOCAL-OSH-CLOSURE-001`

## Acceptance Criteria

- [x] OpenSpec, story, contract, epic, and traceability artifacts define the Sprint 40 OSH-side scope before implementation.
- [x] OSH ConSys advertises and accepts exact `application/swe+text` while preserving legacy CSV compatibility without counting it as exact SWE Text.
- [x] DataStream and ControlStream schema subresources return JSON-compatible schema representation media types instead of `auto` for non-browser schema requests.
- [x] ControlStream schema requests accept `cmdFormat` as an alias for `commandFormat`.
- [x] Observation and Command child collection/count reads return parseable JSON bodies for JSON `Accept` requests, including filtered or nested empty result sets.
- [x] Focused OSH ConSys regressions reference the Sprint 40 scenarios in comments.
- [x] OSH focused tests pass or any failure is documented with exact output.
- [x] The rebuilt local OSH image is exercised through TeamEngine against the documented local OSH IUT.
- [x] A SimUAV populated TeamEngine E2E run is attempted after the OSH patch; remaining failures are documented without claiming full populated closure.
- [x] Raze reviews non-trivial Sprint 40 changes before completion; focused recheck returned `APPROVE_WITH_CONCERNS` with no required fixes.

## Non-Goals

- Do not weaken ETS media-type, SWE Text, Annex A.9, or binding assertions.
- Do not treat `application/swe+csv` as exact OGC 23-002 SWE Common Text conformance evidence.
- Do not claim full positive populated-IUT binding closure unless TeamEngine E2E proves parent schema and child body evidence.
- Do not mutate public or shared IUTs.

## Verification Evidence

- Focused OSH ConSys regressions after Raze gapfixes: PASS, `TestDataStreams 10/0/0/0`, `TestControlStreams 5/0/0/0`, `TestObservations 8/0/0/0`; artifacts archived as `ops/test-results/sprint-ets-40-gapfix-r4-osh-*.xml`.
- Sibling OSH ConSys patch committed locally as `79f89fb Patch ConSys populated binding blockers`.
- ETS Docker Maven wrapper: PASS, `294 tests / 0 failures / 0 errors / 3 skipped`; artifact `ops/test-results/sprint-ets-40-maven-test-via-docker-2026-06-03.txt`.
- Direct SimUAV format/body probes after runtime compatibility fix: PASS for JSON-compatible schema response media types, exact `application/swe+text` with `TextEncoding`, `cmdFormat` aliasing, parseable Observation body evidence (`items=1`), and parseable Command empty bodies/counts; artifacts `ops/test-results/sprint-ets-40-gapfix-r4-local-osh-simuav-format-probes-2026-06-03.tsv` and `ops/test-results/sprint-ets-40-gapfix-r4-local-osh-simuav-format-probes-parsed-2026-06-03.tsv`.
- SimUAV preseed: PARTIAL, `GET=104`, `POST=1`, two DataStreams, three ControlStreams, waypoint feasibility Command HTTP 200 `COMPLETED`, nested Command body parseable JSON, but no positive Observation/Command child item evidence; artifact `ops/test-results/sprint-ets-40-local-osh-simuav-preseed-r2-2026-06-03.json`.
- SimUAV-populated TeamEngine E2E: FAIL, `211 total / 86 passed / 17 failed / 108 skipped`; artifacts `ops/test-results/sprint-ets-40-simuav-local-osh-smoke-final-2026-06-03.xml` and `ops/test-results/sprint-ets-40-simuav-local-osh-container-final-2026-06-03.log`.
- Clean primary local OSH TeamEngine E2E after field-hub config restore and static reseed: PASS, `211 total / 61 passed / 0 failed / 150 skipped`, zero IUT-bound POST/PUT/DELETE/PATCH; artifacts `ops/test-results/sprint-ets-40-gapfix-clean-local-osh-smoke-r2-2026-06-03.xml`, `ops/test-results/sprint-ets-40-gapfix-clean-local-osh-container-r2-2026-06-03.log`, `ops/test-results/sprint-ets-40-gapfix-clean-local-osh-no-mutation-r2-2026-06-03.txt`, and `ops/test-results/sprint-ets-40-gapfix-artifact-hygiene-clean-local-osh-r2-2026-06-03.json`.
- Raze focused recheck: `APPROVE_WITH_CONCERNS`, confidence `0.91`, no required fixes; artifact `.harness/evaluations/sprint-ets-40-adversarial-recheck.yaml`.

## Residual Blockers

Full populated-IUT closure is not claimed. The remaining SimUAV-populated failures are OSH schema serialization and fixture-population issues: Command schema documents still miss Annex A.9/SWE fields such as `commandFormat`, `recordSchema`, and `encoding`; Observation schema documents still fail SWE/JSON schema shape checks; final direct probes now show positive Observation child evidence, but no inspected ControlStream exposes positive associated Command child item evidence.
