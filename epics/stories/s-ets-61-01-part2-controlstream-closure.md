# Story S-ETS-61-01: Part 2 Control Streams and Commands Released ATS Closure

> Status: Done | Epic: epic-ets-03-part2-classes | Sprint: ets-61 | Last updated: 2026-07-31

## Context

Sprint 61 follows Sprint 59's Part 2 API Common exact closure and Sprint 60's
Datastream exact closure. The target is OGC 23-002 Clause 10 and Annex A.3,
Requirements Class "Control Streams and Commands", using official
`/req/controlstream` and `/conf/controlstream` identifiers.

## Scope

1. Replace the historical Sprint 22 subset with exactly eighteen released
   Control Streams and Commands TestNG procedures.
2. Add the ten currently unmapped procedures:
   `/sf-ref-from-controlstream`, `/foi-ref-from-controlstream`,
   `/ref-from-deployment`, `/collections`, `/cmd-canonical-url`,
   `/cmd-collections`, `/status-resources-endpoint`,
   `/command-status-endpoint`, `/result-resources-endpoint`, and
   `/command-result-endpoint`.
3. Remove standalone non-ATS declaration and API Common prerequisite tracer
   methods from the deployed ControlStream class.
4. Change `part2controlstream` TestNG inheritance to `part2apicommon`.
5. Promote all eighteen released mappings to reviewed exact and regenerate the
   ATS coverage report.
6. Preserve read-only, bounded ControlStream, Command, CommandStatus, and
   CommandResult HTTP behavior.

## Acceptance Criteria

- [x] `Part2ControlStreamTests` exposes exactly eighteen TestNG `@Test`
  methods, one per released OGC 23-002 `/conf/controlstream` target.
- [x] Every deployed ControlStream method traces `REQ-ETS-PART2-003` and one
  canonical released requirement URI in its `@Test` description.
- [x] `testng.xml` declares `part2controlstream` depends-on `part2apicommon`.
- [x] Runtime setup reads only immutable suite arguments before per-procedure
  IUT access.
- [x] Reviewed coverage reports `2:/conf/controlstream` as
  `18 exact / 0 candidate / 0 unmapped`.
- [x] Local OSH E2E is executed and documented honestly, including skips or
  failures caused by local OSH conformance declarations or data availability.
- [x] No IUT-bound mutation requests are emitted.

## Verification Evidence

- Test-first focused red: `88 tests / 3 failures / 6 errors / 0 skipped`.
- Corrected focused verification:
  `bash scripts/mvn-test-via-docker.sh -Dtest=VerifyPart2ControlStreamTests,VerifyTestNGSuiteDependency`
  passed `88/0/0/0`.
- Coverage update:
  `VerifyReleasedAtsCoverage#coverageReportMatchesCompiledTestNgMetadata`
  passed `1/0/0/0` with `-Dats.coverage.report.update=true`.
- Full coverage audit: `VerifyReleasedAtsCoverage` passed `23/0/0/0`.
- Full Docker Maven passed `758 tests / 0 failures / 0 errors / 3 skipped`.
- Mandatory local OSH TeamEngine smoke reached the real IUT and exited
  honestly non-green at `254 total / 36 passed / 21 failed / 197 skipped`.
  All eighteen Sprint 61 methods SKIP through `part2apicommon` because local
  OSH does not declare Part 2 `/conf/api-common`.
- No-mutation oracle: `recognized_iut_request_logs=186`; method counts are
  `GET=192`, `POST=0`, `PUT=0`, `PATCH=0`, `DELETE=0`, `OPTIONS=0`.
- Evidence directory:
  `ops/test-results/sprint-ets-61-part2-controlstream-final-2026-07-31/`.
  Durable tracked artifacts are `s-ets-01-03-teamengine-smoke-2026-07-31.xml`,
  `s-ets-01-03-teamengine-container-2026-07-31.txt`,
  `no-mutation-oracle.txt`, and `request-method-counts.txt`.

## Definition of Done

- [x] CP-021, OpenSpec, story, contract, traceability, status, changelog, and
  test-results are reconciled.
- [x] Focused test-first red is captured.
- [x] Corrected focused tests pass.
- [x] Coverage update and audit pass.
- [x] Formatter and full Docker Maven run.
- [x] Local OSH TeamEngine smoke and no-mutation oracle run.
- [x] Raze review completes with no unresolved required fixes.
- [ ] Commit is pushed to Botts `main`.

## Out Of Scope

- Mutation behavior.
- Command Feasibility.
- Part 2 JSON and SWE Common encoding semantic validation.
- Full Command parameter/result validation against ControlStream schemas.
