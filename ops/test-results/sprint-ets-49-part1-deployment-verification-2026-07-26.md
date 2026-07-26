# Sprint 49 Part 1 Deployment Verification

Date: 2026-07-26

## Scope

Sprint 49 replaces the historical four-method Deployment approximation with
the five released OGC 23-001 `/conf/deployment` procedures. Deployment depends
directly on Part 1 API Common and preserves an ETS-owned boundary for future
SensorML validator replacement.

## Coverage

- Released inventory: 240 tests.
- Reviewed coverage: 20 exact, 2 helper, 141 candidate, 77 unmapped.
- `/conf/deployment`: 5 of 5 exact.
- ATS audit self-test and released-source reproduction at
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`: PASS.

## Build And Tests

- Raze remediation focused Maven: `35/0/0/0`.
- Deployment focused Maven: `90/0/0/0`.
- Full Docker Maven: `434/0/0/3`.
- Controlled HTTP tests execute successful paths for all five procedures and
  fail-closed media, canonical, collection, and System-reference cases.

## Exact Runtime

- Image:
  `sha256:9049b284529b53845403e985fae2b03a9598073724320de2ad2e395006506d47`.
- TeamEngine 6 provenance, immutable base files, selected dependency payload,
  deployed SWE Common adapter, collision inventory, runtime identity, and
  confidential build-context checks: PASS.

## Primary Local OSH E2E

TeamEngine against the unmodified local OSH reports:

- Total 217, passed 39, failed 3, skipped 175.
- Deployment canonical endpoint: SKIP, unsupported `application/json`.
- Deployment resources endpoint: SKIP, unsupported `application/json`.
- Deployment collections: FAIL, no `featureType=sosa:Deployment` collection.
- Deployment canonical URL: FAIL, no required Deployment collection evidence.
- Deployment System reference: FAIL, nested endpoint HTTP 400.
- Artifact hygiene: 117 recognized requests, 112 IUT GETs, zero writes, zero
  credential leaks.

These are genuine IUT conformance outcomes. They are not classified as a
passing conformance gate.

## Failure And Credential Gates

- API Common sabotage: total 217, passed 34, failed 1, skipped 182.
- The one failure is the intentional
  `resourceIdsAreUniqueWithinEachType` failure.
- All five Deployment methods SKIP directly on that API Common failure before
  Deployment IUT access.
- Sabotage hygiene: 100 recognized requests, 95 IUT GETs, zero writes.
- Credential integration: PASS.
- Credential wire E2E: PASS with exactly one fresh TestNG XML and container log
  selected after the current-run marker; zero unmasked execution-artifact hits,
  39 masked events, and 39 intact synthetic transmissions.
- Raw synthetic verifier and stub captures remain under `/tmp`; no real
  credential is recorded.

## External Dependency Integrity

- `/home/nh/docker/osh-core` is clean at
  `4c87a65c9a967d52af9df476e65d7862c7673a15` and zero commits ahead.
- The deployed `/opt/osh` mount is read-only.
- The deployed ConSys jar reports `Bundle-BuildNumber: 4c87a65`.
- TeamEngine is inherited from the approved immutable digest.
- Sprint 49 makes no OSH or TeamEngine source or binary modification.

## Adversarial Review

- Initial Raze review identified behavioral, credential-provenance, and
  reconciliation gaps.
- Corrected focused Maven and all full-stack gates above close the behavioral
  and credential findings.
- The final focused documentation recheck closed `RAZE-S49-FINAL-001` with
  `APPROVE`, confidence `0.99`, duration 46 seconds, and no remaining required
  findings.
