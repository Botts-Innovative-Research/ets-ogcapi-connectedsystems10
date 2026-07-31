# Story S-ETS-62-01: Part 2 Command Feasibility Released ATS Closure

> Status: Done | Epic: epic-ets-03-part2-classes | Sprint: ets-62 | Last updated: 2026-07-31

## Context

Sprint 62 follows Sprint 61's exact Control Streams and Commands closure. The
target is OGC 23-002 Clause 11 and Annex A.4, Requirements Class "Command
Feasibility", using official `/req/feasibility` and `/conf/feasibility`
identifiers.

## Scope

1. Replace the historical Sprint 23 safety subset with exactly five released
   Command Feasibility TestNG procedures.
2. Remove standalone non-ATS declaration, prerequisite, and safety tracer
   methods from the deployed Feasibility class.
3. Change `part2feasibility` TestNG inheritance to `part2controlstream`.
4. Promote all five released mappings to reviewed exact and regenerate the ATS
   coverage report.
5. Preserve read-only, bounded Command, Feasibility, CommandStatus,
   CommandResult, ControlStream, and `/collections` behavior.
6. Document Annex A.4 copy-text inconsistencies without hiding them behind
   unlisted endpoint substitutions.

## Acceptance Criteria

- [x] `Part2FeasibilityTests` exposes exactly five TestNG `@Test` methods, one
  per released OGC 23-002 `/conf/feasibility` target.
- [x] Every deployed Feasibility method traces `REQ-ETS-PART2-004` and one
  canonical released requirement URI in its `@Test` description.
- [x] `testng.xml` declares `part2feasibility` depends-on `part2controlstream`.
- [x] Runtime setup skips before Feasibility IUT access when ControlStream
  prerequisite execution failed or skipped.
- [x] Reviewed coverage reports `2:/conf/feasibility` as
  `5 exact / 0 candidate / 0 unmapped`.
- [x] Local OSH E2E is executed and documented honestly, including skips or
  failures caused by local OSH conformance declarations or data availability.
- [x] No IUT-bound mutation requests are emitted.

## Verification Evidence

- Focused test-first red: formatter first caught newly written test formatting;
  after formatting, the focused structural run failed at compile time because
  the new exact helper tests referenced missing
  `releasedControlStreamCommandPath(String)` and
  `isCollectionWithItemType(Map,String)` helpers.
- Corrected focused verification:
  `bash scripts/mvn-test-via-docker.sh -Dtest=VerifyPart2FeasibilityTests,VerifyTestNGSuiteDependency`
  passed `83 tests / 0 failures / 0 errors / 0 skipped`.
- Coverage update passed `1/0/0/0`; coverage audit passed `23/0/0/0`.
- Formatter passed. Full Docker Maven passed with existing skips:
  `760 tests / 0 failures / 0 errors / 3 skipped`.
- Local OSH TeamEngine smoke:
  `252 total / 36 passed / 21 failed / 195 skipped`. All five Sprint 62
  Feasibility procedures SKIP before Feasibility IUT access because
  `fetchPart2ControlStreamInputs` skipped through the local OSH prerequisite
  chain. The 21 failures are existing local OSH SensorML/collection/deployment
  gaps outside Sprint 62.
- No-mutation oracle: `recognized_iut_request_logs=184`; method counts are
  `GET=184`, zero POST/PUT/PATCH/DELETE.
- Durable E2E artifacts:
  `ops/test-results/sprint-ets-62-part2-feasibility-2026-07-31/`.

## Definition of Done

- [x] CP-022, OpenSpec, story, contract, traceability, status, changelog, and
  test-results are reconciled.
- [x] Focused test-first red is captured.
- [x] Corrected focused tests pass.
- [x] Coverage update and audit pass.
- [x] Formatter and full Docker Maven run.
- [x] Local OSH TeamEngine smoke and no-mutation oracle run.
- [x] Raze review completes with no unresolved required fixes.
- [x] Commit is pushed to Botts `main`.

## Out Of Scope

- Feasibility POST creation.
- Mutation lifecycle behavior.
- Part 2 Create/Replace/Delete or Update feasibility operations.
- Semantic feasibility-analysis validation beyond released resource schemas.
