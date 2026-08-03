# CP-034: Prerequisite Declaration Field Clarity

## Status

IMPLEMENTED_E2E_NON_GREEN_RAZE_APPROVED_WITH_CONCERNS_PUSHED

## User Instruction

"Continue"

## Problem

Sprint 73 added prerequisite-aware mutation-readiness fields. Raze approved the
change with one LOW concern: the field name
`declarationMethodAndPrerequisiteReadiness` can be read as proof that inherited
TestNG prerequisite groups passed, even though the audit intentionally checks
only `/conformance` declarations and `OPTIONS` method advertisement.

## Change

- Add explicit prerequisite-declaration readiness field names to the read-only
  mutation-readiness JSON.
- Preserve the Sprint 73 field names as compatibility aliases for existing
  consumers.
- Update tests, specs, stories, operations docs, and evidence summaries to use
  the explicit declaration-only terminology.
- Keep exact promotion false for every mutation-bound candidate.

## Non-Goals

- Do not issue POST, PUT, PATCH, or DELETE from the audit.
- Do not inspect or infer TestNG prerequisite PASS/SKIP status inside the audit.
- Do not patch OSH or TeamEngine.
- Do not promote mutation-bound candidates to reviewed exact mappings.

## Acceptance

- The audit emits class-level
  `declarationMethodAndPrerequisiteDeclarationReadiness` and root
  `classesWithDeclarationMethodAndPrerequisiteDeclarationReadiness`.
- The older Sprint 73 fields remain present and equal to the new explicit
  fields.
- Focused tests assert both names and the declaration-only policy.
- Direct and disposable local OSH evidence archive the explicit field names
  with no unsafe audit methods and no exact promotion.

## Evidence

- Python compile: PASS.
- Python unit tests: `21/0/0/0`.
- Formatter: BUILD SUCCESS.
- Docker Maven: `787/0/0/3`.
- Direct local OSH audit: `47` remaining candidates, `GET=1`, `OPTIONS=25`,
  `unsafeMethodsIssued=[]`, one declaration/method-ready class, zero
  prerequisite-declaration-ready classes, and old/new alias equality.
- Disposable local OSH E2E run:
  `sprint-ets-74-fields-20260803T042511Z`; provisioning PASS, cleanup PASS,
  primary-state isolation PASS, mutation readiness `GET=1`, `OPTIONS=27`,
  `unsafeMethodsIssued=[]`, old/new alias equality, and full TeamEngine smoke
  still non-green on the existing local OSH twenty-failure baseline.
- Evidence directory:
  `ops/test-results/sprint-ets-74-prerequisite-declaration-fields-2026-08-03/`.
- Raze review:
  `.harness/evaluations/sprint-ets-74-adversarial.yaml` returned
  `APPROVE_WITH_CONCERNS 0.94` with `required_fixes: []`.
- Post-review hygiene: the LOW pycompile-log exit-status concern was addressed
  by regenerating `pycompile.log` with `pycompile:0` and refreshing
  `repo-evidence-manifest.sha256`.
- Implementation/evidence commit `b265dc6` is pushed to Botts `main`.
