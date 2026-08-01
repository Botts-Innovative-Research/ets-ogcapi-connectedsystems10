# Story S-ETS-65-01: Part 2 JSON Encoding Released ATS Closure

> Status: Raze approved, pending push | Epic: epic-ets-03-part2-classes | Sprint: ets-65 | Last updated: 2026-08-01

## Context

Sprint 65 follows Sprint 64's exact Part 2 Advanced Filtering closure. The
target is OGC 23-002 Clause 16.1 and Annex A.9, Requirements Class "JSON
Encoding", using official `/req/json` and `/conf/json` identifiers.

## Scope

1. Replace the historical Sprint 28 helper/subset implementation with exactly
   fourteen released Annex A.9 TestNG procedures.
2. Add the missing released `/conf/json/mediatype-read` procedure.
3. Remove standalone declaration, prerequisite, and resource-condition helper
   tests from the deployed Part 2 JSON class.
4. Keep the runtime read-only: GET resource/schema endpoints and inspect API
   definition write-media metadata only.
5. Promote all fourteen released mappings to reviewed exact and regenerate the
   ATS coverage report.
6. Preserve honest SKIP behavior when `/conf/json`, the SWE JSON
   record-components prerequisite, condition classes, endpoints, or positive
   candidate resources are unavailable.

## Acceptance Criteria

- [x] `Part2JsonTests` exposes exactly fourteen TestNG `@Test` methods, one
  per released OGC 23-002 Annex A.9 `/conf/json` target.
- [x] Every deployed method traces `REQ-ETS-PART2-009` and exactly one
  canonical released requirement URI in its `@Test` description.
- [x] `mediatype-read` is no longer unmapped in `ops/ats-coverage-report.json`.
- [x] Reviewed coverage reports `2:/conf/json` as
  `14 exact / 0 candidate / 0 unmapped`.
- [x] Observation, Command, and CommandResult constraint procedures validate
  candidate child JSON values against parent JSON Schema evidence when present,
  and SKIP rather than PASS when safe evidence is absent.
- [x] Local OSH E2E is executed and documented honestly, including skips or
  failures caused by local OSH conformance declarations or data availability.
- [x] No IUT-bound mutation requests are emitted.

## Verification Evidence

- Formatter:
  `formatter-after-raze-wording.txt` exits 0.
- Focused regression after Raze recheck gapfix:
  `focused-after-raze-recheck-fix.txt` reports `88 tests / 0 failures / 0 errors /
  0 skipped`.
- Coverage:
  `coverage-report-update-after-raze-recheck-fix.txt` exits 0,
  `coverage-audit-after-raze-recheck-fix.txt` reports `23/0/0/0`, and
  `coverage-summary-after-raze-recheck-fix.txt` reports overall
  `240 total / 167 exact / 2 helper / 64 candidate / 7 unmapped`, Part 2
  `130 total / 76 exact / 47 candidate / 7 unmapped`, and Part 2 JSON
  `14 exact / 0 candidate / 0 unmapped`.
- Full Docker Maven:
  `full-maven-after-raze-recheck-fix.txt` reports `766 tests / 0 failures / 0 errors /
  3 skipped`.
- Local OSH TeamEngine smoke:
  `local-osh-smoke-after-raze-recheck-fix.txt` exits 1 with honest non-green
  `258 total / 29 passed / 20 failed / 209 skipped`; all fourteen Part 2 JSON
  methods SKIP before JSON resource access because the IUT lacks
  `http://www.opengis.net/spec/SWE/3.0/conf/json-record-components`.
- No mutation:
  `no-mutation-oracle-after-raze-recheck-fix.txt` reports
  `recognized_iut_request_logs=151`; `request-method-counts-after-raze-recheck-fix.txt`
  reports `GET 151`, zero POST/PUT/PATCH/DELETE.
- TeamEngine runtime:
  `teamengine-runtime-immutability-after-raze-recheck-fix.txt` exits 0 for
  smoke image
  `sha256:31e6b4eac77f8455638c94160c788f7156689566711c668ea266194837434637`.
- Raze:
  `sprint-ets-65-adversarial-final-recheck.yaml` reports `APPROVE 0.96` with
  `RAZE-ETS65-RECHECK-FALSEPASS-001` and `RAZE-ETS65-RECHECK-DOC-001` closed
  and `required_fixes: []`.

## Definition of Done

- [x] CP-025, OpenSpec, story, contract, traceability, status, changelog, and
  test-results are reconciled.
- [x] Focused test-first red is captured.
- [x] Corrected focused tests pass.
- [x] Coverage update and audit pass.
- [x] Formatter and full Docker Maven run.
- [x] Local OSH TeamEngine smoke and no-mutation oracle run.
- [x] Raze review completes with no unresolved required fixes.
- [ ] Commit is pushed to Botts `main`.

## Out Of Scope

- Public or default-IUT mutation.
- Positive JSON create/replace lifecycle behavior.
- SWE Common JSON/Text/Binary exact closure.
- New external validator dependency work.
