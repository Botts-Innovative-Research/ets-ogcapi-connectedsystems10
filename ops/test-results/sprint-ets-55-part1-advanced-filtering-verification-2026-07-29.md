# Sprint ETS-55 Part 1 Advanced Filtering Verification

Date: 2026-07-29
Story: S-ETS-55-01
Requirements: REQ-ETS-PART1-009, REQ-ETS-COVERAGE-001
Released source: `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`

## Implemented Boundary

- Exactly 25 independent TestNG methods implement the 25 released
  `/conf/advanced-filtering` procedures.
- The class directly inherits Part 1 API Common. System and sibling groups do
  not block it.
- `AdvancedFilteringSupport` owns declaration checks, canonical endpoint
  discovery, seed-derived predicates, bounded pagination and relation
  traversal, representation dispatch, JTS intersection, association
  provenance, pairwise combined predicates, and transitive recommendation
  checks.
- Every procedure issues GET only. Known matches cannot pass from empty
  results, and cross-origin relations never receive IUT credentials.

## Verification

- R4 test-first gate: expected `40/4/0/0`, then focused controlled HTTP
  `40/0/0/0` after remediation.
- Precommit full Docker Maven: `594/0/0/3`; the three skips are unchanged historical
  harness fixtures.
- ATS source reproduction: PASS at the pinned clean checkout.
- Coverage: `240/76 exact/2 helper/115 candidate/47 unmapped`;
  `/conf/advanced-filtering` is `25/25 exact`.
- Controlled HTTP executes all 25 positive procedures and key fail-closed
  UID-prefix, keyword, association, combination, pagination, traversal,
  isolation, dependency, and credential branches.
- Superseded candidate `756d729` API Common sabotage, credential, and hygiene
  gates passed. They must be repeated from the new exact candidate.

## TeamEngine E2E

Candidate `756d729828d08b88d43ce8ae0ff5f5dd2e5f13b7`, image
`sha256:e6e7f7c7c853081b37970d69d3742d36baaa5a92d1f39ca46adb796535ec4bf1`,
and all associated runtime and TeamEngine gates are superseded audit evidence
after R4. A new candidate must repeat every exact gate. No OSH or TeamEngine
source or binary was modified.

## Adversarial Review

Initial Raze reported seven required semantic findings. Later rechecks
reopened wrapper identity, prescribed association paths, media/pagination,
combined predicate completeness, keyword boundaries, mapping/traceability,
and exact-build provenance. R3 additionally found Deployment property wrapper
and nested-href shortcuts, malformed-href synthetic identity, mapping
overstatement, ignored evidence, and contract traceability. Requirement-linked
R4 found direct-relation suffix/nested-extension shortcuts and missing
single-System validation after Deployment property dereference. Four
requirement-linked regressions move `40/4/0/0` to `40/0/0/0`;
exact-candidate and fresh Raze gates remain pending.

## Evidence Index

Historical diagnostic evidence is under
`ops/test-results/sprint-ets-55-part1-advanced-filtering-e2e-2026-07-29/`.
Superseded `085a81f` exact candidate and R3 remediation evidence is under
`ops/test-results/sprint-ets-55-part1-advanced-filtering-final-r2-2026-07-29/`
and includes R2/R3 regression evidence plus the superseded `085a81f` exact
artifacts. Superseded `756d729` SHA-prefixed, checksum-manifested evidence and
current R4 remediation logs are under
`ops/test-results/sprint-ets-55-part1-advanced-filtering-final-r3-2026-07-29/`.
