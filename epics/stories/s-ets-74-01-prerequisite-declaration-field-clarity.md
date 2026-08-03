# S-ETS-74-01: Prerequisite Declaration Field Clarity

## Status

IMPLEMENTED_E2E_NON_GREEN_RAZE_APPROVED_WITH_CONCERNS_PUSHED

## User Instruction

"Continue"

## Scope

Clarify Sprint 73 mutation-readiness JSON semantics by adding explicit
prerequisite-declaration readiness field names while preserving compatibility
aliases.

## Requirements

- `REQ-ETS-CLEANUP-025`
- `SCENARIO-ETS-CLEANUP-MUTATION-PREREQUISITE-DECLARATION-FIELDS-001`
- `REQ-ETS-CLEANUP-024`
- `REQ-ETS-CLEANUP-023`

## Acceptance Criteria

- [x] OpenSpec records the Sprint 74 field-clarity requirement.
- [x] The audit emits explicit prerequisite-declaration readiness fields at
  class and root levels.
- [x] Sprint 73 field names remain present as compatibility aliases.
- [x] Tests prove old and new readiness fields are equal and declaration-only.
- [x] The audit remains read-only and serializes no credential values.
- [x] No mutation-bound candidate mapping is promoted to reviewed exact.
- [x] Direct local OSH and disposable local OSH evidence are archived.
- [x] Raze reviews the scoped change before completion.

## Non-Goals

- Do not add TestNG prerequisite execution parsing to the audit.
- Do not issue mutation methods from the audit.
- Do not modify OSH or TeamEngine.
- Do not close positive CRD or Update lifecycle conformance.

## Implementation Notes

- `scripts/mutation-readiness-audit.py` now emits
  `declarationMethodAndPrerequisiteDeclarationReadiness`,
  `classesWithDeclarationMethodAndPrerequisiteDeclarationReadiness`, and
  `prerequisiteDeclarationReadinessPolicy`.
- Sprint 73 names remain compatibility aliases:
  `declarationMethodAndPrerequisiteReadiness`,
  `classesWithDeclarationMethodAndPrerequisiteReadiness`, and
  `prerequisiteReadinessPolicy`.
- `scripts/test_mutation_readiness_audit.py` asserts old/new equality,
  declaration-only policy text, read-only behavior, no credential-value
  serialization, and no exact promotion.
- Evidence directory:
  `ops/test-results/sprint-ets-74-prerequisite-declaration-fields-2026-08-03/`.
- Raze returned `APPROVE_WITH_CONCERNS 0.94` with `required_fixes: []`.
- The LOW pycompile-log exit-status concern was addressed by regenerating
  `pycompile.log` with `pycompile:0` and refreshing the repository evidence
  manifest.
- Implementation/evidence commit `b265dc6` is pushed to Botts `main`.
