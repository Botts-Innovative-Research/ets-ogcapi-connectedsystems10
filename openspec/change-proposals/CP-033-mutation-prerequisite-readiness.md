# CP-033: Mutation Prerequisite Readiness Audit

## Status

IMPLEMENTED_RAZE_APPROVED_WITH_CONCERNS_PUSHED

## User Instruction

"Continue"

## Problem

Sprint 72 made the remaining mutation-bound candidate backlog visible through a
read-only `/conformance` and `OPTIONS` audit. That audit can still overstate
near-term readiness when an IUT declares the direct mutation class and advertises
methods but omits inherited prerequisite conformance declarations that the
released ATS requires before lifecycle methods can run.

The current local OSH evidence is the motivating case: Part 1
Create/Replace/Delete can look declaration/method-ready from direct
declarations and broad `Allow` headers, while the TestNG prerequisite chain still
SKIPs before writes because API Common datetime evidence and exact inherited
`ogcapi-4` declarations are absent.

## Change

- Add prerequisite-declaration readiness fields to the mutation-readiness audit.
- Keep the Sprint 72 read-only contract: no POST, PUT, PATCH, or DELETE.
- Preserve candidate mapping status and `exactPromotionReady=false`.
- Report missing inherited prerequisite declarations separately from missing
  direct class declarations, missing condition declarations, missing advertised
  methods, and missing positive lifecycle proof.
- Archive refreshed direct and disposable local OSH evidence showing the
  prerequisite-declaration readiness tier.

## Non-Goals

- Do not patch OSH or TeamEngine.
- Do not issue lifecycle mutation requests from the audit.
- Do not promote any Part 1 or Part 2 mutation candidate to reviewed exact.
- Do not replace the mandatory TeamEngine E2E gate with audit evidence.

## Acceptance

- `scripts/mutation-readiness-audit.py` emits
  `missingPrerequisiteConformance`, `prerequisiteConformancePresent`, and
  `declarationMethodAndPrerequisiteReadiness` for every audited mutation class.
- The root JSON includes
  `classesWithDeclarationMethodAndPrerequisiteReadiness`.
- Focused tests cover an IUT that is direct declaration/method-ready but
  prerequisite-incomplete.
- Direct local OSH and disposable local OSH evidence remain read-only for
  TeamEngine smoke traffic and keep exact promotion false.

## Evidence

- Python unit tests: `21/0/0/0`.
- Docker Maven: `787/0/0/3`.
- Direct local OSH audit: `GET=1`, `OPTIONS=25`, unsafe `[]`, direct
  declaration/method-ready `["1:/conf/create-replace-delete"]`,
  prerequisite-declaration-ready `[]`.
- Disposable local OSH audit: `GET=1`, `OPTIONS=27`, unsafe `[]`,
  prerequisite-declaration-ready `[]`.
- TeamEngine E2E remains honestly non-green on the known local OSH baseline:
  populated `275/24/20/231`, clean-primary `275/23/20/232`.
- Raze returned `APPROVE_WITH_CONCERNS 0.94` with `required_fixes: []`; the
  only LOW concern is that prerequisite readiness is declaration-only, not
  prerequisite TestNG execution proof.
- Implementation and evidence commit `d110ccd` is pushed to Botts `main`.
