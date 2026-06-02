# S-ETS-36-01: Binding Parent Schema JSON Request Shaping

## Status
IMPLEMENTED_RAZE_APPROVED_PUSHED

## User Instruction
Triggered by: "Continue" after Sprint 35 SimUAV evidence identified local OSH stream/schema representation as the remaining `REQ-ETS-PART2-013` blocker.

## Scope
Continue `REQ-ETS-PART2-013` by removing the ETS-side parent-schema media-type false blocker for binding checks only.

- Requirement: `REQ-ETS-PART2-013`
- Scenario: `SCENARIO-ETS-PART2-013-SCHEMA-JSON-REQUEST-SHAPING-001`
- Target code: `Part2ObservationCommandBindingTests`
- Target behavior: parent schema subresource GETs append `f=json` and still require `application/json` or `+json` response media type before PASS evidence.

## Source Probe
Archived source probe: `ops/test-results/sprint-ets-36-local-osh-schema-request-shaping-source-probe-2026-06-02.txt`.

Findings:

- Local OSH ConSys `BaseHandler.parseFormat` reads query parameter `f`.
- `BaseResourceHandler.getByKey` sets the response content type to the parsed response format.
- `DataStreamSchemaHandler` and `CommandStreamSchemaHandler` both allow `ResourceFormat.JSON`.
- Therefore the ETS can request `?f=json` for schema subresources instead of accepting `Content-Type: auto`.

## Acceptance Criteria

- [x] OpenSpec records `SCENARIO-ETS-PART2-013-SCHEMA-JSON-REQUEST-SHAPING-001`.
- [x] Runtime schema subresource requests append `f=json`.
- [x] Tests reference `REQ-ETS-PART2-013` and the new scenario in comments.
- [x] Strict media-type validation remains unchanged; `Content-Type: auto` is not accepted as PASS evidence.
- [x] Focused Maven verification passes.
- [x] Mandatory clean local OSH TeamEngine smoke is rerun and records zero write requests.
- [x] Raze reviews non-trivial changes before completion.

## Generator Evidence

- Code: `Part2ObservationCommandBindingTests.requiredJsonObject` now requests `schemaJsonRequestPath(path)`.
- Helper behavior: `schemaJsonRequestPath` appends `f=json` unless `f` or `format` already exists.
- Regression: `parentSchemaRequestsAddJsonFormatWithoutLooseningMediaTypeGate` covers no-query, existing commandFormat, existing f, and existing format cases.
- Formatter: bounded Docker Maven cached rerun passed after an earlier unbounded cold-cache formatter run was stopped as stalled.
- Focused Maven: `ops/test-results/sprint-ets-36-focused-maven-2026-06-02.log`, `14 tests / 0 failures / 0 errors / 0 skipped`.
- Required wrapper attempt: `ops/test-results/sprint-ets-36-maven-clean-test-2026-06-02.log` failed before tests because Maven Central reset while resolving `org.apache.maven.plugins:maven-plugins:pom:43`; this is dependency transfer failure, not a test failure.
- Full Docker Maven fallback: `ops/test-results/sprint-ets-36-maven-clean-test-persistent-cache-2026-06-02.log`, `289 tests / 0 failures / 0 errors / 3 skipped`.
- Clean local OSH TeamEngine smoke: `ops/test-results/sprint-ets-36-clean-local-osh-smoke-2026-06-02.xml`, `211 total / 68 passed / 0 failed / 143 skipped`.
- Clean local OSH no-mutation: `ops/test-results/sprint-ets-36-clean-local-osh-no-mutation-2026-06-02.txt`, `recognized_iut_request_logs=135`, `GET=133`, `OPTIONS=2`, writes `0`.
- Raze implementation review: `.harness/evaluations/sprint-ets-36-adversarial-implementation.yaml`, initial `GAPS_FOUND` for stale OpenSpec implementation-status wording; focused recheck returned `APPROVE_WITH_CONCERNS`, confidence `0.93`, with no required fixes.
- Raze concern: clean local OSH smoke did not exercise populated live `?f=json` schema requests because `/datastreams` and `/controlstreams` were empty. This remains documented and is not claimed as full populated `part2binding` PASS.
- Commit: `50024c4 Implement Sprint 36 schema request shaping`, pushed to `origin/main` on 2026-06-02.

## Non-Goals

- Do not claim full populated `part2binding` PASS.
- Do not change Annex A.9 JSON/SWE schema validation expectations.
- Do not patch or rebuild local OSH in this story.
- Do not enable Sapient or SimUAV in the primary smoke state.
