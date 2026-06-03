# S-ETS-39-01: Artifact Hygiene and URI/Schema Drift Harness

## Status
IMPLEMENTED_RAZE_APPROVED_WITH_CONCERNS

## User Instruction
Triggered by: "Do them"

## Scope
Add durable report-first tooling that can proceed while the remaining populated local OSH blockers are outside the ETS safety envelope.

- Requirements:
  - `REQ-ETS-CLEANUP-020`
  - `REQ-ETS-SYNC-001`
- Scenarios:
  - `SCENARIO-ETS-CLEANUP-ARTIFACT-HYGIENE-SUMMARY-001`
  - `SCENARIO-ETS-CLEANUP-ARTIFACT-CREDENTIAL-SCAN-001`
  - `SCENARIO-ETS-SYNC-URI-SCHEMA-DRIFT-AUDIT-001`
- Target tools:
  - `scripts/artifact-hygiene.py`
  - `scripts/uri-drift-audit.py`

## Acceptance Criteria

- [x] OpenSpec, story, contract, epic, and traceability artifacts define the Sprint 39 report-first scope before implementation.
- [x] Artifact hygiene tooling parses TestNG XML totals, smoke request-log method counts, IUT-bound write counts, and credential-scan counts.
- [x] Credential scanning accepts masked Authorization entries and flags unmasked Authorization headers or explicitly supplied secret values without printing the secret value.
- [x] Drift tooling extracts OGC Connected Systems URIs from Java ETS source and the frozen v1.0 TypeScript registry, applies an optional allowlist, and reports Java-only/web-app-only URI counts.
- [x] Drift tooling compares schema bundle relative paths and hashes while ignoring non-schema `:Zone.Identifier` artifacts.
- [x] Self-tests reference `REQ-ETS-CLEANUP-020`, `REQ-ETS-SYNC-001`, and the Sprint 39 scenarios in comments or docstrings.
- [x] Tools are run against Sprint 38 archived artifacts and current schema/source state, with JSON results archived under `ops/test-results/`.
- [x] Docker Maven verification and mandatory clean local OSH TeamEngine E2E are run.
- [x] Raze reviews non-trivial changes before completion.

## Non-Goals

- Do not weaken strict schema media-type, SWE Common Text, Annex A.9, or populated binding assertions.
- Do not enable CI-failing URI drift enforcement until the allowlist is stabilized.
- Do not scan or print local OSH credential values directly in archived artifacts.
- Do not mutate public or shared IUTs.

## Verification Evidence

- Self-tests:
  - `python3 scripts/artifact-hygiene.py --self-test`: PASS.
  - `python3 scripts/uri-drift-audit.py --self-test`: PASS.
  - `python3 -m py_compile scripts/artifact-hygiene.py scripts/uri-drift-audit.py`: PASS.
- Sprint 38 clean artifact hygiene:
  - Artifact: `ops/test-results/sprint-ets-39-artifact-hygiene-s38-clean-2026-06-03.json`.
  - Result: PASS; TestNG `211/68/0/143`; IUT methods `GET=133`, `OPTIONS=2`, writes `0`; credential leaks `0`.
- Sprint 38 populated artifact hygiene:
  - Read-only gate artifact: `ops/test-results/sprint-ets-39-artifact-hygiene-s38-populated-2026-06-03.json`.
  - Result: FAIL by policy because the populated mutable TeamEngine artifact contains IUT-bound lifecycle writes (`POST=3`, `PUT=1`, `DELETE=3`).
  - Report-only mutable artifact: `ops/test-results/sprint-ets-39-artifact-hygiene-s38-populated-mutable-2026-06-03.json`.
  - Result: PASS; TestNG `211/83/29/99`; IUT methods `GET=248`, `OPTIONS=12`, `POST=3`, `PUT=1`, `DELETE=3`; credential leaks `0`.
- URI/schema drift audit:
  - Artifact: `ops/test-results/sprint-ets-39-uri-schema-drift-audit-2026-06-03.json`.
  - Result: PASS in report-only mode with drift detected; Java URI count `98`, web-app URI count `215`, missing-in-Java `162`, missing-in-webapp `45`.
  - Schema bundle parity: Java `126`, web app `126`, missing/extra/hash mismatch `0`.
- Maven:
  - Artifact: `ops/test-results/sprint-ets-39-maven-test-via-docker-2026-06-03.txt`.
  - Result: PASS; `294 tests / 0 failures / 0 errors / 3 skipped`.
- Clean local OSH E2E:
  - Report: `ops/test-results/sprint-ets-39-clean-local-osh-smoke-2026-06-03.xml`.
  - Container log: `ops/test-results/sprint-ets-39-clean-local-osh-container-2026-06-03.log`.
  - Hygiene summary: `ops/test-results/sprint-ets-39-artifact-hygiene-clean-local-osh-2026-06-03.json`.
  - Result: PASS; `211 total / 68 passed / 0 failed / 143 skipped`; IUT methods `GET=133`, `OPTIONS=2`, writes `0`; credential leaks `0`.
- Raze:
  - Artifact: `.harness/evaluations/sprint-ets-39-adversarial-implementation.yaml`.
  - Result: `APPROVE_WITH_CONCERNS`, confidence `0.90`, no required fixes.
  - Post-review concern handling: regenerated the clean hygiene JSON against archived `ops/test-results/` paths, added non-sensitive explicit-secret input counts to hygiene JSON, and added Java ETS/web-app repository commit plus dirty-count metadata to the drift JSON.
  - Focused recheck: `.harness/evaluations/sprint-ets-39-adversarial-recheck.yaml` returned `APPROVE`, confidence `0.94`, no required fixes, and confirmed concerns 002-004 closed while concern 001 remains accepted as report-only.
