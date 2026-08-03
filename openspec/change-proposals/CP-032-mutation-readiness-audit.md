# CP-032: Mutation Candidate Readiness Audit

## Status

IMPLEMENTED_RAZE_APPROVED

## Trigger

User instructed: "Continue" after Sprint 71 was pushed.

## Motivation

The released ATS inventory is now zero-unmapped, but 47 procedures remain
candidate because they require destructive Create/Replace/Delete or Update
lifecycle evidence. The current local OSH evidence shows these candidates are
blocked by missing declarations, missing PATCH/write readiness, missing
condition classes, or missing positive lifecycle proof.

The practical next increment is a durable read-only audit that records these
readiness blockers directly from a real IUT without issuing mutation requests
or overclaiming exact conformance.

## Scope

- Add a standalone `scripts/mutation-readiness-audit.py` tool.
- Audit the four remaining mutation-bound classes:
  Part 1 Create/Replace/Delete, Part 1 Update, Part 2
  Create/Replace/Delete, and Part 2 Update.
- Fetch `/conformance` and issue only `OPTIONS` readiness probes.
- Report class-level candidate counts, missing declarations, missing condition
  classes, missing advertised methods, and positive lifecycle evidence still
  required before exact promotion.
- Record only whether credentials were supplied, never the credential value.
- Archive the audit from the disposable local OSH populated workflow after
  seeding and before TeamEngine smoke.
- Use a generated Docker DNS-safe network alias for the disposable OSH IUT URL
  so long legal run ids do not make TeamEngine resolve an overlong container
  hostname.

## Out of Scope

- Issuing POST, PUT, PATCH, or DELETE from the audit.
- Promoting any mutation-bound mapping to reviewed exact.
- Treating OPTIONS as lifecycle conformance evidence.
- Patching OSH or TeamEngine.

## Verification

- Python unit tests for the auditor and populated-workflow integration.
- Java formatter and Maven verification for unchanged conformance suite health.
- Direct local OSH audit evidence with unsafe methods empty.
- Disposable local OSH or primary local OSH TeamEngine E2E evidence, honestly
  reporting existing non-green IUT baseline failures.
- Raze review before completion.

## Verification Results

- Python compile and unit tests pass; `scripts.test_mutation_readiness_audit`
  and `scripts.test_local_osh_populated_workflow` now cover 20 tests.
- Formatter BUILD SUCCESS and full Docker Maven BUILD SUCCESS with
  `787 tests / 0 failures / 0 errors / 3 skipped`.
- Direct local OSH audit records all 47 remaining candidate procedures,
  `GET=1`, `OPTIONS=25`, `unsafeMethodsIssued=[]`, and
  `exactPromotionReady=false`.
- Initial disposable E2E with a long run id exposed
  `UnknownHostException` for an overlong generated OSH container hostname; the
  workflow now passes a short generated network alias to Docker and TeamEngine.
- Final disposable E2E run
  `sprint-ets-72-readiness-r2-20260803T015933Z` reached the populated IUT via
  `http://ets-csapi-osh-435115cf728c162b:8081/sensorhub/api`, archived
  mutation-readiness JSON, removed the isolated OSH state, and verified the
  primary OSH state was unchanged.
- Full TeamEngine remains honestly non-green on the existing local OSH
  twenty-failure baseline: populated `275/24/20/231`, clean-primary
  `275/23/20/232`.
- Initial Raze review returned `GAPS_FOUND 0.93` because nested Sprint 72
  `.log` files were ignored by `.gitignore`. The gapfix adds a scoped ignore
  exception for this evidence directory, makes the referenced logs
  commit-reachable, verifies the copied run `artifact-manifest.sha256`, and
  adds `repo-evidence-manifest.sha256` for the repository evidence subset.
- Focused Raze recheck
  `.harness/evaluations/sprint-ets-72-adversarial-recheck.yaml` returned
  `APPROVE 0.97` with `required_fixes: []`.
