# Story S-ETS-63-01: Part 2 System Events Released ATS Closure

> Status: Complete | Epic: epic-ets-03-part2-classes | Sprint: ets-63 | Last updated: 2026-08-01

## Context

Sprint 63 follows Sprint 62's exact Command Feasibility closure. The target is
OGC 23-002 Clause 12 and Annex A.5, Requirements Class "System Events", using
official `/req/system-event` and `/conf/system-event` identifiers.

## Scope

1. Replace the historical Sprint 24 six-method System Events subset with
   exactly five released Annex A.5 TestNG procedures.
2. Remove standalone non-ATS declaration and prerequisite methods from the
   deployed SystemEvent class.
3. Change `part2systemevent` TestNG inheritance to the released prerequisites:
   Part 2 API Common and Part 1 System.
4. Promote all five released mappings to reviewed exact and regenerate the ATS
   coverage report.
5. Preserve read-only, bounded SystemEvent, ControlStream, System, and
   `/collections` behavior.
6. Document Annex A.5 copy-text inconsistencies without hiding them behind
   unlisted endpoint substitutions.

## Acceptance Criteria

- [x] `Part2SystemEventTests` exposes exactly five TestNG `@Test` methods, one
  per released OGC 23-002 `/conf/system-event` target.
- [x] Every deployed SystemEvent method traces `REQ-ETS-PART2-005` and one
  canonical released requirement URI in its `@Test` description.
- [x] `testng.xml` declares `part2systemevent` depends-on
  `part2apicommon systemfeatures`.
- [x] Runtime setup skips before SystemEvent IUT access when Part 2 API Common
  or Part 1 System prerequisite execution failed or skipped.
- [x] Reviewed coverage reports `2:/conf/system-event` as
  `5 exact / 0 candidate / 0 unmapped`.
- [x] Local OSH E2E is executed and documented honestly, including skips or
  failures caused by local OSH conformance declarations or data availability.
- [x] No IUT-bound mutation requests are emitted.

## Verification Evidence

- Focused test-first red: formatter normalization followed by compile failures
  for the missing Sprint 63 helpers; see
  `ops/test-results/sprint-ets-63-part2-system-event-2026-08-01/focused-red-reproduction.txt`.
- Corrected focused structural tests:
  `VerifyPart2SystemEventTests,VerifyTestNGSuiteDependency = 84/0/0/0`;
  see
  `ops/test-results/sprint-ets-63-part2-system-event-2026-08-01/focused-corrected.txt`.
- Coverage update:
  `VerifyReleasedAtsCoverage#coverageReportMatchesCompiledTestNgMetadata =
  1/0/0/0`; coverage audit `VerifyReleasedAtsCoverage = 23/0/0/0`; see
  `coverage-update.txt` and `coverage-audit.txt` in the Sprint 63 evidence
  directory.
- Formatter: BUILD SUCCESS; see `formatter.txt`.
- Full Docker Maven: `763 tests / 0 failures / 0 errors / 3 skipped`; see
  `full-maven.txt`.
- Local OSH TeamEngine smoke:
  `251 total / 36 passed / 21 failed / 194 skipped`. All five System Events
  procedures SKIP before SystemEvent IUT access because prerequisite
  `canonicalSystemsEndpointIsValid` skipped. The 21 failures are existing
  local OSH SensorML/Deployment/Procedure/Property/Sampling Feature gaps.
- No-mutation oracle: `recognized_iut_request_logs=182`; request methods are
  `GET=182`, zero POST/PUT/PATCH/DELETE.
- TeamEngine 6 runtime verification passed for final smoke image
  `sha256:fe0ae15f3f088bddae114aa7780bd507900f37c543b8294b2d4366a53b287c6e`.

## Definition of Done

- [x] CP-023, OpenSpec, story, contract, traceability, status, changelog, and
  test-results are reconciled.
- [x] Focused test-first red is captured.
- [x] Corrected focused tests pass.
- [x] Coverage update and audit pass.
- [x] Formatter and full Docker Maven run.
- [x] Local OSH TeamEngine smoke and no-mutation oracle run.
- [x] Raze review completes with no unresolved required fixes.
- [x] Commit `8d0c4fa` is pushed to Botts `main`.

## Out Of Scope

- Streaming/SSE event consumption.
- System History.
- Advanced Filtering event filters.
- Part 2 SystemEvent Create/Replace/Delete or Update lifecycle mutation.
