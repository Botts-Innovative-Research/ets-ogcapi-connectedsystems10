# Story S-ETS-60-01: Part 2 Datastreams and Observations Released ATS Closure

> Status: Done | Epic: epic-ets-03-part2-classes | Sprint: ets-60 | Last updated: 2026-07-31

## Context

Sprint 60 follows Sprint 59's Part 2 API Common exact closure. The target is
OGC 23-002 Clause 9 and Annex A.2, Requirements Class "Datastreams and
Observations", using official `/req/datastream` and `/conf/datastream`
identifiers.

## Scope

1. Replace the historical Sprint 21 subset with exactly fourteen released
   Datastreams and Observations TestNG procedures.
2. Add the five currently unmapped procedures:
   `/sf-ref-from-datastream`, `/foi-ref-from-datastream`,
   `/ref-from-deployment`, `/collections`, and `/obs-collections`.
3. Remove standalone non-ATS declaration and API Common prerequisite tracer
   methods from the deployed Datastream class.
4. Change `part2datastream` TestNG inheritance to `part2apicommon`.
5. Promote all fourteen released mappings to reviewed exact and regenerate the
   ATS coverage report.
6. Preserve read-only, bounded Datastream and Observation HTTP behavior.

## Acceptance Criteria

- [x] `Part2DatastreamTests` exposes exactly fourteen TestNG `@Test` methods, one
  per released OGC 23-002 `/conf/datastream` target.
- [x] Every deployed Datastream method traces `REQ-ETS-PART2-002` and one canonical
  released requirement URI in its `@Test` description.
- [x] `testng.xml` declares `part2datastream` depends-on `part2apicommon`.
- [x] Runtime setup reads only immutable suite arguments before per-procedure IUT
  access.
- [x] Reviewed coverage reports `2:/conf/datastream` as
  `14 exact / 0 candidate / 0 unmapped`.
- [x] Local OSH E2E is executed and documented honestly, including skips or
  failures caused by local OSH conformance declarations or data availability.
- [x] No IUT-bound mutation requests are emitted.
- [x] Raze bounded-approximation findings are remediated with all-resource,
  exact-collection, canonical-link, endpoint-schema, and all-format schema-op
  semantics.
- [x] Raze conditional-applicability findings are remediated so A.3, A.4, A.8,
  and A.9 skip before nested IUT subresource access unless their conditions are
  declared or evidenced.
- [x] Final Raze follow-up findings are remediated so mixed Sampling Feature
  endpoint evidence cannot false-PASS and FOI `application/json` pages validate
  as GeoJSON FeatureCollections.

## Verification Evidence

- Focused test-first red: `85 tests / 3 failures / 2 errors / 0 skipped`.
- Corrected focused Datastream and suite-dependency verification: `96/0/0/0`.
- Coverage update and audit: `23/0/0/0`; overall coverage is
  `240 total / 107 exact / 2 helper / 107 candidate / 24 unmapped`, and
  `2:/conf/datastream` is `14 exact / 0 candidate / 0 unmapped`.
- Formatter: BUILD SUCCESS.
- Full Docker Maven: `750 tests / 0 failures / 0 errors / 3 skipped`.
- Local OSH TeamEngine smoke: `247 total / 38 passed / 21 failed /
  188 skipped`; Datastream procedures SKIP before IUT access because local OSH
  does not declare Part 2 `/conf/api-common`.
- No-mutation oracle: `recognized_iut_request_logs=189`; request-line method
  count is `GET=194`, zero POST/PUT/PATCH/DELETE.
- Final evidence archive:
  `ops/test-results/sprint-ets-60-part2-datastream-final-raze-2026-07-31/`.
- Raze: initial `GAPS_FOUND 0.94`, FOI/stale-evidence recheck
  `GAPS_FOUND`, conditional-gating recheck `GAPS_FOUND`, and mixed
  Sampling Feature / FOI `application/json` recheck `GAPS_FOUND`; final recheck
  returned `PASS` with high confidence and no required fixes.

## Definition of Done

- [x] CP-020, OpenSpec, story, contract, traceability, status, changelog, and
  test-results are reconciled.
- [x] Focused test-first red is captured.
- [x] Corrected focused tests pass.
- [x] Coverage update and audit pass.
- [x] Formatter and full Docker Maven run.
- [x] Local OSH TeamEngine smoke and no-mutation oracle run.
- [x] Final Raze recheck completes with no required fixes.
- [x] Commit is pushed to Botts `main`.

## Out Of Scope

- Mutation behavior.
- ControlStream and Command behavior.
- Part 2 JSON and SWE Common encoding validation.
- Full Observation result validation against Datastream schemas.
