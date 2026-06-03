# S-ETS-37-01: Local OSH Stream Metadata Unblock

## Status
PARTIAL_E2E_CLEAN_RESTORED_POPULATED_BLOCKED

## User Instruction
Triggered by: "Do it" after the explanation that local OSH must emit schema-valid stream metadata to unblock populated `REQ-ETS-PART2-013` closure.

## Scope
Unblock local OSH populated stream schema validation without weakening Annex A.9 validation.

- Requirement: `REQ-ETS-PART2-013`
- Scenarios:
  - `SCENARIO-ETS-PART2-013-LOCAL-OSH-STREAM-METADATA-001`
  - `SCENARIO-ETS-PART2-013-FORMAT-ASSERTION-NOW-001`
  - `SCENARIO-ETS-PART2-013-EMPTY-COLLECTION-BODIES-001`
  - `SCENARIO-ETS-PART2-013-POSITIVE-LOCAL-OSH-CLOSURE-001`
- ETS target code: Part 2 JSON/SWE schema validator factory construction.
- OSH target code: ConSys DataStream and ControlStream JSON serializers.

## Acceptance Criteria

- [x] OpenSpec records the Sprint 37 local OSH metadata and `now` format-assertion scenarios.
- [x] OSH DataStream JSON emits required `phenomenonTime`, `resultTime`, and `live` members even when ranges or live state are unknown.
- [x] OSH ControlStream JSON emits required `issueTime`, `executionTime`, `live`, and `async` members.
- [x] OSH regression tests verify the required stream metadata members on freshly created streams.
- [x] ETS schema validation asserts `format` so `"now"` is not accepted by the `date-time` branch of `timeInstantOrNow`.
- [x] ETS regression tests reference `REQ-ETS-PART2-013` and the new scenarios in comments.
- [x] OSH ConSys root Observation collection and count endpoints return parseable JSON bodies instead of HTTP 200 empty bodies; Command empty collection/count behavior is covered by focused OSH regression tests until a live nested Commands E2E fixture exists.
- [x] Maven/Gradle verification for touched ETS and OSH modules is archived.
- [x] Mandatory local OSH TeamEngine smoke is rerun against a rebuilt local OSH target and records request method counts; clean primary smoke passed after the follow-up empty-body fix, while populated smoke remains blocked.
- [x] Raze reviews non-trivial changes before completion.

## Non-Goals

- Do not weaken Annex A.9 schemas or required-field checks.
- Do not count declarations, empty collections, or body-valid non-JSON media types as binding PASS evidence.
- Do not claim full populated `part2binding` closure unless live parent schema and child body evidence are exercised end to end.
- Do not claim clean-smoke E2E coverage for nested Command empty collections while the reset/reseed IUT has no ControlStreams or Commands.

## Verification Evidence

- ETS Docker Maven: `ops/test-results/sprint-ets-37-ets-maven-test-2026-06-03.log`, `291 tests / 0 failures / 0 errors / 3 skipped`.
- OSH focused Gradle tests: `ops/test-results/sprint-ets-37-osh-TestDataStreams-2026-06-03.xml` (`9/0/0/0`) and `ops/test-results/sprint-ets-37-osh-TestControlStreams-2026-06-03.xml` (`1/0/0/0`).
- SimUAV metadata probe: `ops/test-results/sprint-ets-37-local-osh-simuav-stream-metadata-probe-2026-06-03.json`.
- SimUAV-populated TeamEngine smoke: FAIL, `211/78/35/98`; no-mutation `GET=178`, `OPTIONS=12`, writes `0`.
- Clean reset/reseed TeamEngine smoke: FAIL, `211/67/4/140`; no-mutation `GET=133`, `OPTIONS=2`, writes `0`.
- Follow-up OSH empty-body regressions: `ops/test-results/sprint-ets-37-followup-osh-TestObservations-2026-06-03.xml` (`5/0/0/0`) and `ops/test-results/sprint-ets-37-followup-osh-TestControlStreams-2026-06-03.xml` (`2/0/0/0`).
- Follow-up clean reset/reseed direct probe: `ops/test-results/sprint-ets-37-followup-empty-collection-live-probe-after-reset-reseed-2026-06-03.txt`.
- Follow-up clean reset/reseed TeamEngine smoke: PASS, `211/68/0/143`; no-mutation `GET=133`, `OPTIONS=2`, writes `0`.
- Post-Raze OSH empty-body regressions: `ops/test-results/sprint-ets-37-followup-osh-TestObservations-after-raze-2026-06-03.xml` (`5/0/0/0`) and `ops/test-results/sprint-ets-37-followup-osh-TestControlStreams-after-raze-2026-06-03.xml` (`2/0/0/0`).
- Post-Raze clean direct probe: `ops/test-results/sprint-ets-37-followup-empty-collection-live-probe-after-raze-2026-06-03.txt`.
- Post-Raze clean TeamEngine smoke: PASS, `211/68/0/143`; no-mutation `GET=133`, `OPTIONS=2`, writes `0`.

## Blockers

- Populated smoke: schema endpoint media types still return `Content-Type: auto` for non-binding `obsFormat`/`cmdFormat` paths; some child Observation/Command endpoints return empty JSON bodies; some SWE text schema requests return HTTP 400.
- Clean smoke blocker resolved: `/observations?limit=1`, `/observations?f=json&limit=1`, and `/observations/count?f=json` now return parseable JSON after reset/reseed and post-Raze hardening.
- Raze implementation review returned `APPROVE_WITH_CONCERNS`, confidence `0.93`, with no required fixes after focused reconciliation. Follow-up Raze returned `GAPS_FOUND`, confidence `0.88`; required fixes were applied by removing broad exception masking, reconciling docs, narrowing Command claims, and rotating local OSH credentials. Focused follow-up recheck returned `APPROVE_WITH_CONCERNS`, confidence `0.91`, with no required fixes.
