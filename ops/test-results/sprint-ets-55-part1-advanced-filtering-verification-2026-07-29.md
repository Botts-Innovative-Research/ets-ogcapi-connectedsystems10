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
- R5 HTTP test-first gate: expected `42/3/4/0`, then focused controlled HTTP
  and support tests `48/0/0/0` after remediation.
- Precommit full Docker Maven: `602/0/0/3`; the three skips are unchanged
  historical harness fixtures.
- ATS source reproduction: PASS at the pinned clean checkout.
- Coverage: `240/76 exact/2 helper/115 candidate/47 unmapped`;
  `/conf/advanced-filtering` is `25/25 exact`.
- Controlled HTTP executes all 25 positive procedures and key fail-closed
  UID-prefix, keyword, association, combination, pagination, traversal,
  isolation, dependency, and credential branches.
- Candidate `060a8aa` runtime, source, API Common sabotage, credential,
  immutability, and hygiene gates passed before Raze R5 superseded it.

## TeamEngine E2E

Candidate `060a8aa994d59f0adfa6bfa96fd5fb372b3d6743`, image
`sha256:a74b3cc8bfe71df11ef4cc13ef8ceb6c0b32e0cffc184e04f9f115c2f215f07e`,
passed exact runtime and TeamEngine gates. Unmodified local OSH was honestly
`238/40/7/191`; the seven established IUT failures remain visible and all 25
Advanced Filtering methods SKIP at the absent declaration. API Common
sabotage is `238/2/10/226`; all 25 methods cascade-SKIP. Hygiene records 174
recognized GETs, 169 IUT GETs, zero writes, and zero leaks. Credential wire
E2E recorded zero unmasked artifact hits, 33 masked events, and 33 intact
transmissions. Raze R5 superseded this candidate; every exact gate must repeat
from the R5 remediation commit. No OSH or TeamEngine source or binary was
modified.

## Adversarial Review

Initial Raze reported seven required semantic findings. Later rechecks
reopened wrapper identity, prescribed association paths, media/pagination,
combined predicate completeness, keyword boundaries, mapping/traceability,
and exact-build provenance. R3 additionally found Deployment property wrapper
and nested-href shortcuts, malformed-href synthetic identity, mapping
overstatement, ignored evidence, and contract traceability. Requirement-linked
R4 found direct-relation suffix/nested-extension shortcuts and missing
single-System validation after Deployment property dereference. Four
requirement-linked regressions move `40/4/0/0` to `40/0/0/0`. Every exact
candidate gate passed on `060a8aa`, but Raze R5 returned `GAPS_FOUND 0.99`
for canonical relation vocabulary, System typing, ignored representation
validation, and a mapping overclaim. R5 HTTP regressions reproduce
`42/3/4/0`; remediation passes focused `48/0/0/0` and full Maven
`602/0/0/3`. New exact-candidate and fresh Raze gates remain.

## Evidence Index

Historical diagnostic evidence is under
`ops/test-results/sprint-ets-55-part1-advanced-filtering-e2e-2026-07-29/`.
Superseded `085a81f` exact candidate and R3 remediation evidence is under
`ops/test-results/sprint-ets-55-part1-advanced-filtering-final-r2-2026-07-29/`
and includes R2/R3 regression evidence plus the superseded `085a81f` exact
artifacts. Superseded `756d729` SHA-prefixed, checksum-manifested evidence and
current R4 remediation logs are under
`ops/test-results/sprint-ets-55-part1-advanced-filtering-final-r3-2026-07-29/`.
Superseded exact `060a8aa` SHA-prefixed, checksum-manifested evidence and R5
remediation logs are under
`ops/test-results/sprint-ets-55-part1-advanced-filtering-final-r5-2026-07-29/`.
