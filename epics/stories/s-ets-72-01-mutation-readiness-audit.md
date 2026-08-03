# S-ETS-72-01: Mutation Candidate Readiness Audit

## Status

COMPLETE

## User Instruction

"Continue"

## Scope

Add durable, read-only readiness evidence for the 47 remaining mutation-bound
candidate procedures. This sprint does not claim exact Create/Replace/Delete or
Update lifecycle conformance.

## Requirements

- `REQ-ETS-CLEANUP-023`
- `SCENARIO-ETS-CLEANUP-MUTATION-READINESS-AUDIT-001`
- `REQ-ETS-PART1-010`
- `REQ-ETS-PART1-011`
- `REQ-ETS-PART2-007`
- `REQ-ETS-PART2-008`

## Acceptance Criteria

- [x] OpenSpec records the Sprint 72 mutation-readiness audit requirement.
- [x] The audit reports all four remaining mutation-bound candidate classes and
  all 47 candidate procedures.
- [x] The audit issues only GET and OPTIONS and records unsafe methods as an
  empty list.
- [x] Credential values are not serialized into audit evidence.
- [x] The disposable local OSH populated workflow archives audit JSON after
  provisioning and before TeamEngine smoke.
- [x] Python tests, formatter, Maven, direct local OSH audit, and E2E evidence
  are archived.
- [x] Raze reviews the scoped change before completion. Initial Raze returned
  `GAPS_FOUND 0.93` for ignored nested `.log` evidence; the logs are now
  commit-reachable and `repo-evidence-manifest.sha256` verifies. Focused
  recheck returned `APPROVE 0.97` with `required_fixes: []`.
- [x] Specs, story, traceability, status, changelog, test-results, and metrics
  are reconciled.

## Non-Goals

- Do not issue POST, PUT, PATCH, or DELETE from the audit.
- Do not promote candidate mappings to reviewed exact.
- Do not patch OSH or TeamEngine.
- Do not treat OPTIONS advertisement as lifecycle conformance.

## Implementation Notes

- Added `scripts/mutation-readiness-audit.py` and focused Python regressions.
- Wired the audit into `scripts/local_osh_populated_e2e.py` after provisioning
  and before TeamEngine smoke.
- Fixed the populated workflow to use a generated Docker DNS-safe network
  alias for the TeamEngine IUT URL after the first Sprint 72 long-run-id E2E
  attempt exposed an `UnknownHostException` from an overlong container name.
- Direct local OSH readiness audit: 47 candidates, `GET=1`, `OPTIONS=25`,
  `unsafeMethodsIssued=[]`, and no exact promotions.
- Disposable populated OSH run
  `sprint-ets-72-readiness-r2-20260803T015933Z`: readiness audit `GET=1`,
  `OPTIONS=27`, `unsafeMethodsIssued=[]`; populated TeamEngine
  `275/24/20/231`; clean-primary TeamEngine `275/23/20/232`; cleanup PASS;
  primary-state isolation PASS.
- Raze initial review `.harness/evaluations/sprint-ets-72-adversarial.yaml`
  returned `GAPS_FOUND 0.93` with one required evidence-packaging fix:
  nested `.log` files referenced by the run manifest were ignored. The
  `.gitignore` exception is now scoped to this Sprint 72 evidence directory,
  the copied run `artifact-manifest.sha256` verifies in place, and
  `repo-evidence-manifest.sha256` covers the repository evidence subset.
- Raze focused recheck
  `.harness/evaluations/sprint-ets-72-adversarial-recheck.yaml` returned
  `APPROVE 0.97` with `required_fixes: []`.
- The E2E result remains honestly non-green on the existing local OSH
  twenty-failure baseline. This sprint does not promote any mutation-bound
  candidate mapping to reviewed exact.
