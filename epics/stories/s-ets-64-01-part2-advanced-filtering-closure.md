# Story S-ETS-64-01: Part 2 Advanced Filtering Released ATS Closure

> Status: Done | Epic: epic-ets-03-part2-classes | Sprint: ets-64 | Last updated: 2026-08-01

## Context

Sprint 64 follows Sprint 63's exact System Events closure. The target is OGC
23-002 Clause 13 and Annex A.6, Requirements Class "Advanced Filtering", using
official `/req/advanced-filtering` and `/conf/advanced-filtering` identifiers.

## Scope

1. Replace the historical Sprint 25 nine-method Advanced Filtering subset with
   exactly eighteen released Annex A.6 TestNG procedures.
2. Remove standalone non-ATS declaration and prerequisite helper methods from
   the deployed Part 2 Advanced Filtering class.
3. Change `part2advancedfiltering` TestNG inheritance to the released
   prerequisites: Part 2 API Common and Part 1 Advanced Filtering.
4. Add exact procedures for DataStream, Observation, ControlStream, Command,
   CommandStatus, and SystemEvent filter targets, including all FOI filters.
5. Promote all eighteen released mappings to reviewed exact and regenerate the
   ATS coverage report.
6. Preserve read-only, bounded query behavior and honest SKIP outcomes when a
   declaring IUT lacks positive seed evidence.

## Acceptance Criteria

- [x] `Part2AdvancedFilteringTests` exposes exactly eighteen TestNG `@Test`
  methods, one per released OGC 23-002 `/conf/advanced-filtering` target.
- [x] Every deployed method traces `REQ-ETS-PART2-006` and exactly one canonical
  released requirement URI in its `@Test` description.
- [x] `testng.xml` declares `part2advancedfiltering` depends-on
  `part2apicommon advancedfiltering`.
- [x] Runtime setup skips before Advanced Filtering IUT access when Part 2 API
  Common or Part 1 Advanced Filtering prerequisite execution failed or skipped.
- [x] Reviewed coverage reports `2:/conf/advanced-filtering` as
  `18 exact / 0 candidate / 0 unmapped`.
- [x] Local OSH E2E is executed and documented honestly, including skips or
  failures caused by local OSH conformance declarations or data availability.
- [x] No IUT-bound mutation requests are emitted.

## Verification Evidence

- Focused red reproduction: `ops/test-results/sprint-ets-64-part2-advanced-filtering-2026-08-01/focused-red-reproduction.txt`.
- Corrected focused Docker Maven: `83 tests / 0 failures / 0 errors / 0 skipped`.
- Raze initial review found `RAZE-ETS64-FALSEPASS-001`; the gapfix removed
  generic SystemEvent `type` fallback from event-type evidence and added a
  `type=SystemEvent` plus actual `definition` regression. Post-gapfix focused
  Docker Maven passed `83/0/0/0`.
- Focused Raze recheck returned `APPROVE 0.97` with
  `RAZE-ETS64-FALSEPASS-001` closed and `required_fixes: []`.
- Coverage update: `1/0/0/0`; coverage audit: `23/0/0/0`.
- Post-gapfix full Docker Maven: `762 tests / 0 failures / 0 errors / 3 skipped`.
- Reviewed coverage: overall `240 total / 153 exact / 2 helper / 77 candidate / 8 unmapped`; Part 2 `130 total / 62 exact / 0 helper / 60 candidate / 8 unmapped`; Part 2 Advanced Filtering `18 exact / 0 candidate / 0 unmapped`.
- Post-gapfix local OSH TeamEngine smoke: `260 total / 36 passed / 21 failed / 203 skipped`. All eighteen Sprint 64 methods SKIP before Advanced Filtering IUT access because inherited Part 1 Advanced Filtering prerequisite `indirectPropertyFiltersAreTransitive` skipped; the failures are existing local OSH SensorML/Deployment/Procedure/Property/Sampling Feature gaps outside Sprint 64.
- No-mutation oracle: `recognized_iut_request_logs=181`; request method counts `GET=186`, zero POST/PUT/PATCH/DELETE.
- TeamEngine 6 runtime immutability passed for post-gapfix smoke image `sha256:fe3c3f03d1ffbc0b5e657a23f3e079c3983116203ec2457e6bbaf83f58f63f28`; local OSH remained clean at `4c87a65`, zero commits ahead, with `/opt/osh` mounted read-only.
- Implementation commit `880c347` is pushed to Botts `main`.

## Definition of Done

- [x] CP-024, OpenSpec, story, contract, traceability, status, changelog, and
  test-results are reconciled.
- [x] Focused test-first red is captured.
- [x] Corrected focused tests pass.
- [x] Coverage update and audit pass.
- [x] Formatter and full Docker Maven run.
- [x] Local OSH TeamEngine smoke and no-mutation oracle run.
- [x] Raze review completes with no unresolved required fixes.
- [x] Commit is pushed to Botts `main`.

## Out Of Scope

- Mutation-created filter seed resources.
- Streaming subscriptions.
- New OSH or TeamEngine source or binary changes.
- Part 2 Create/Replace/Delete, Update, JSON, or SWE Common exact closure.
