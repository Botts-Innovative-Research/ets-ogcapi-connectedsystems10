# Sprint 51 Part 1 Subdeployment Verification

Date: 2026-07-27

## Scope

Sprint 51 replaces the historical four-method Subdeployment approximation with
the five released OGC 23-001 `/conf/subdeployment` procedures. No OSH or
TeamEngine source or binary was modified.

## Coverage

- Released source: `v1.0.0` commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`.
- ATS inventory reproduction: PASS.
- Coverage: `240 total / 30 exact / 2 helper / 136 candidate / 72 unmapped`.
- `/conf/subdeployment`: `5/5 exact`.
- Deployed TestNG methods: `219`.

## Maven And HTTP

- Test-first compile: expected FAIL with 45 missing-symbol errors.
- Corrected focused Maven: `131 total / 0 failed / 0 errors / 0 skipped`.
- Corrected full Maven: `480 total / 0 failed / 0 errors / 3 skipped`; BUILD
  SUCCESS.
- Controlled HTTP tests execute successful paths for all five procedures and
  media, pagination, schema, exact-link, hierarchy, recursive-set,
  association, sibling-isolation, and dependency-cascade defects.

## Runtime

- Final exact image:
  `sha256:e88aa5f94ce98d474b66168fbe95ccd6ef574511bce910142acb38a5484b1dca`.
- TeamEngine 6 provenance, embedded SWE Common adapter execution, dependency
  collision, immutable-base, and confidential-context checks: PASS.
- Primary unmodified local OSH TeamEngine:
  `219 total / 39 passed / 5 failed / 175 skipped`.
- All five Subdeployment methods SKIP before IUT access because the inherited
  Deployment group has genuine failures.
- The five visible failures are existing local OSH conformance outcomes: three
  Deployment failures and two Procedure failures. They are not converted to
  PASS or repaired outside ETS scope.
- Primary artifact hygiene: 118 recognized requests, 113 IUT GETs, zero IUT
  writes, and zero credential leaks.

## Dependency And Credential Gates

- A controlled programmatic TestNG baseline reaches the IUT through setup and
  all five methods while a synthetic Deployment prerequisite passes. Changing
  only that prerequisite to FAIL makes setup and all five methods SKIP before
  IUT access, with the injected method reported as blocker.
- The direct local-OSH Deployment sabotage is retained as historical
  non-causal evidence only: Deployment already fails in the baseline, so its
  observed SKIPs cannot establish causality.
- Generic SystemFeatures sabotage also passes its current dynamic-descendant
  oracle. Deployment and Procedure are correctly not descendants after their
  Sprint 49/50 API Common rewiring, so this is supporting rather than direct
  Sprint 51 cascade evidence.
- Credential integration: PASS with zero literal synthetic-credential hits.
- Credential wire E2E: PASS with zero unmasked artifact hits, 37 masked log
  events, and 37 intact synthetic transmissions observed only by the stub IUT.

## Raze Corrections

- `RAZE-S51-001`: the association oracle now requires independent fixture
  evidence for resources owned directly by the parent and by every descendant;
  a known missing parent-owned ID fails.
- `RAZE-S51-002`: the causal programmatic TestNG baseline/sabotage pair replaces
  the historical non-causal direct sabotage claim.
- `RAZE-S51-003`: target identity now normalizes effective default ports and
  percent-encoded unreserved path characters while rejecting path, query, and
  fragment defects.
- `RAZE-S51-004`: association selection examines every occurrence, refuses
  cross-origin candidates, and chooses a same-origin JSON-compatible or
  negotiable untyped candidate.
- `RAZE-S51-GF-001`: both programmatic TestNG runs now write beneath
  JUnit-managed temporary directories; corrected focused and full Maven leave
  repository-root `test-output/` absent.
- `RAZE-S51-GF-002`: all canonical records now cite the corrected Maven totals,
  final image, causal proof, and historical non-causal classification.

## External Immutability

- `/home/nh/docker/osh-core` is clean at
  `4c87a65c9a967d52af9df476e65d7862c7673a15`, zero commits ahead and three
  behind upstream.
- The running OSH container mounts `/opt/osh` read-only.
- TeamEngine base-file identity is verified by the exact-image runtime gate.

## Evidence

- `sprint-ets-51-test-first-2026-07-27.log`
- `sprint-ets-51-focused-final-2026-07-27.log`
- `sprint-ets-51-full-maven-2026-07-27.log`
- `sprint-ets-51-ats-audit-self-test-2026-07-27.log`
- `sprint-ets-51-ats-source-reproduction-2026-07-27.log`
- `sprint-ets-51-coverage-update-2026-07-27.log`
- `sprint-ets-51-teamengine6-runtime-2026-07-27.log`
- `sprint-ets-51-local-osh-teamengine-2026-07-27.xml`
- `sprint-ets-51-local-osh-teamengine-container-2026-07-27.log`
- `sprint-ets-51-primary-hygiene-2026-07-27.json`
- `sprint-ets-51-deployment-sabotage-teamengine-2026-07-27.xml`
- `sprint-ets-51-deployment-sabotage-verdict-2026-07-27.log`
- `sprint-ets-51-deployment-sabotage-hygiene-2026-07-27.json`
- `sprint-ets-51-credential-integration-2026-07-27.log`
- `sprint-ets-51-credential-e2e-2026-07-27.txt`
- `sprint-ets-51-raze-gapfix-test-first-2026-07-27.log`
- `sprint-ets-51-raze-gapfix-focused-final-2026-07-27.log`
- `sprint-ets-51-raze-gapfix-full-maven-2026-07-27.log`
- `sprint-ets-51-raze-gapfix-coverage-update-2026-07-27.log`
- `sprint-ets-51-raze-gapfix-ats-source-reproduction-2026-07-27.log`
- `sprint-ets-51-raze-gapfix-teamengine-runtime-2026-07-27.log`
- `sprint-ets-51-raze-gapfix-local-osh-teamengine-2026-07-27.xml`
- `sprint-ets-51-raze-gapfix-local-osh-container-2026-07-27.log`
- `sprint-ets-51-raze-gapfix-primary-hygiene-2026-07-27.json`
- `sprint-ets-51-raze-gapfix-credential-integration-2026-07-27.log`
- `sprint-ets-51-raze-gapfix-credential-e2e-2026-07-27.txt`
- `sprint-ets-51-raze-hygiene-focused-final-2026-07-27.log`
- `sprint-ets-51-raze-hygiene-full-maven-2026-07-27.log`
- `sprint-ets-51-final-teamengine-runtime-2026-07-27.log`
- `sprint-ets-51-final-local-osh-teamengine-2026-07-27.xml`
- `sprint-ets-51-final-local-osh-container-2026-07-27.log`
- `sprint-ets-51-final-primary-hygiene-2026-07-27.json`
- `sprint-ets-51-clean-snapshot-full-maven-2026-07-27.log`
- `sprint-ets-51-clean-snapshot-hygiene-2026-07-27.md`
- `.harness/evaluations/sprint-ets-51-adversarial-final.yaml`

Final Raze returns `APPROVE_WITH_CONCERNS`, confidence `0.99`; all six findings
are closed and no required fixes remain.
