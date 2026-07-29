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

- Second final-Raze test-first gate: expected `33/6/0/0`, then focused
  controlled HTTP `33/0/0/0` after remediation.
- Precommit full Docker Maven: `587/0/0/3`; the three skips are unchanged historical
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

Candidate `29753ca85c` and image `sha256:709b5f664d8...23aa0` are superseded
audit evidence after the second final-Raze remediation. Exact image,
unmodified-local-OSH TeamEngine, no-mutation, hygiene, sabotage, credential,
runtime, and immutability gates remain pending for the new committed
candidate. No OSH or TeamEngine source or binary is modified.

## Adversarial Review

Initial Raze reported seven required semantic findings. The first recheck
reopened wrapper identity, prescribed association paths, media/pagination,
combined predicate completeness, keyword boundaries, mapping/traceability,
and exact-build provenance. Requirement-linked regressions reproduce the
semantic findings and the precommit implementation is green. Exact-candidate
gates and a fresh final Raze review remain pending.

## Evidence Index

Historical diagnostic evidence is under
`ops/test-results/sprint-ets-55-part1-advanced-filtering-e2e-2026-07-29/`.
Final candidate evidence is under
`ops/test-results/sprint-ets-55-part1-advanced-filtering-final-r2-2026-07-29/`
and currently includes the second red baseline and precommit focused/full
Maven logs. Exact-candidate artifacts will replace the superseded prior
candidate evidence after commit.
