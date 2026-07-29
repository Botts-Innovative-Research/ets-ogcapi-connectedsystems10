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

- Adversarial test-first gate: expected `20/7/0/0` failure, then
  `20/0/0/0` after remediation.
- Focused Docker Maven: `102/0/0/0`.
- Full Docker Maven: `574/0/0/3`; the three skips are unchanged historical
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

Candidate commit:
`cf7fa82745615f1c27ec8df57bd1d315db673955`

Exact image:
`sha256:c39a9c35120064e6be41eaf11c677f77566fd0203849dc0e498995e0b63f08ae`

Dockerized TeamEngine executed the full suite against unmodified local OSH at
`http://field-hub-osh-1:8081/sensorhub/api`. The honest result is
`238 total / 40 passed / 7 failed / 191 skipped`. The seven failures match the
established local-IUT baseline method-for-method. All 25 Advanced Filtering
methods were discovered once and SKIP at the missing `/conf/advanced-filtering`
declaration; these are not conformance passes and no filter query was issued.

TeamEngine runtime, deployed SWE Common adapter, coordinate collision,
immutable-base, and confidential-context checks pass. Local OSH remains clean
at `4c87a65c9a967d52af9df476e65d7862c7673a15`, its deployed ConSys bundle
matches that checkout, and `/opt/osh` is read-only. No OSH or TeamEngine source
or binary was modified.

## Adversarial Review

Initial Raze reported seven required semantic findings: incomplete UID-prefix
execution, weak keyword provenance, synthetic association identifiers,
single-combination validation, first-only indirect checks, and silent
traversal truncation. Requirement-linked regressions reproduce all seven and
the implementation closes them. Final Raze recheck is pending.

## Evidence Index

Evidence is under
`ops/test-results/sprint-ets-55-part1-advanced-filtering-e2e-2026-07-29/`.
It includes the red/green adversarial regressions, focused/full Maven logs,
ATS audit, exact local OSH XML/container log, outcome analysis, no-mutation and
hygiene reports, runtime/immutability evidence, API Common sabotage XML/log,
and both credential-gate reports.
