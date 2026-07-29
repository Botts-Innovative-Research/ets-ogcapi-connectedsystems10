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

- R3 test-first gate: expected `36/3/0/0`, then focused controlled HTTP
  `36/0/0/0` after remediation.
- Precommit full Docker Maven: `590/0/0/3`; the three skips are unchanged historical
  harness fixtures.
- ATS source reproduction: PASS at the pinned clean checkout.
- Coverage: `240/76 exact/2 helper/115 candidate/47 unmapped`;
  `/conf/advanced-filtering` is `25/25 exact`.
- Controlled HTTP executes all 25 positive procedures and key fail-closed
  UID-prefix, keyword, association, combination, pagination, traversal,
  isolation, dependency, and credential branches.
- API Common sabotage: PASS. API Common setup and all four methods SKIP, then
  Advanced Filtering setup and all 25 methods cascade-SKIP.
- Credential integration and wire E2E: PASS with zero unmasked artifact hits,
  33 masked events, and 33 intact synthetic transmissions.
- Artifact hygiene: PASS with 169 IUT GETs, zero writes, and zero credential
  leaks.

## TeamEngine E2E

Candidate `085a81fdaa8fb2b823dc029532a1e9b41b8cd16c`, image
`sha256:f1a6fed0a0a899a1f09f660ad7dbb947eed91823d6ae94176e06498ae24fc455`,
and all associated runtime and TeamEngine gates are superseded audit evidence
after R3. A new candidate must repeat every exact gate. No OSH or TeamEngine
source or binary was modified.

## Adversarial Review

Initial Raze reported seven required semantic findings. Later rechecks
reopened wrapper identity, prescribed association paths, media/pagination,
combined predicate completeness, keyword boundaries, mapping/traceability,
and exact-build provenance. R3 additionally found Deployment property wrapper
and nested-href shortcuts, malformed-href synthetic identity, mapping
overstatement, ignored evidence, and contract traceability. Requirement-linked
R3 regressions move `36/3/0/0` to `36/0/0/0`; exact-candidate gates and fresh
Raze remain pending.

## Evidence Index

Historical diagnostic evidence is under
`ops/test-results/sprint-ets-55-part1-advanced-filtering-e2e-2026-07-29/`.
Superseded exact candidate and current remediation evidence is under
`ops/test-results/sprint-ets-55-part1-advanced-filtering-final-r2-2026-07-29/`
and includes R2/R3 regression evidence plus the superseded `085a81f` exact
artifacts. A new SHA-prefixed evidence set will be added after commit.
