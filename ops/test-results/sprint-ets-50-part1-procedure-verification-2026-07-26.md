# Sprint 50 Part 1 Procedure Verification

Date: 2026-07-26

## Scope

Sprint 50 replaces the historical four-method Procedure approximation with
the five released OGC 23-001 `/conf/procedure` procedures. Procedure depends
directly on Part 1 API Common and preserves the ETS-owned boundary for a future
reusable SensorML validator.

## Coverage

- Released inventory: 240 tests.
- Reviewed coverage: 25 exact, 2 helper, 137 candidate, 76 unmapped.
- `/conf/procedure`: 5 of 5 exact.
- ATS audit self-test, internal consistency, and released-source reproduction
  at `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`: PASS.

## Build And Tests

- Test-first compilation gate: expected 39 missing-symbol errors before
  production implementation.
- Raze gap-fix test-first gate: expected two missing-method compile errors.
- Final focused Procedure and coverage Maven: `116/0/0/0`.
- Full Docker Maven: `451/0/0/3`.
- Controlled HTTP tests execute successful paths for all five procedures and
  fail-closed media, location, type, canonical, and collection cases.

## Exact Runtime

- Image:
  `sha256:6e1beeb598ab4c734f2e2d30e0ecb70d3270af9f9f2d5a1029d1b74259b54d98`.
- TeamEngine 6 provenance, immutable base files, selected dependency payload,
  deployed SWE Common adapter, collision inventory, runtime identity, and
  confidential build-context checks: PASS.

## Primary Local OSH E2E

TeamEngine against the unmodified local OSH reports:

- Total 218, passed 39, failed 5, skipped 174.
- Procedure setup: PASS.
- Procedure location: SKIP, unsupported `application/json`.
- Procedure resources endpoint: SKIP, unsupported `application/json`.
- Procedure canonical endpoint: SKIP, unsupported `application/json`.
- Procedure canonical URL: FAIL, no `featureType=sosa:Procedure` collection.
- Procedure collections: FAIL, no `featureType=sosa:Procedure` collection.
- Artifact hygiene: 120 recognized requests, 115 IUT GETs, zero writes, zero
  credential leaks.

These are genuine IUT conformance outcomes. They are not classified as a
passing conformance gate.

## Failure And Credential Gates

- API Common sabotage: total 218, passed 34, failed 1, skipped 183.
- The one failure is the intentional
  `resourceIdsAreUniqueWithinEachType` failure.
- Procedure setup and all five Procedure methods SKIP before Procedure IUT
  access.
- Sabotage artifact hygiene: PASS with zero writes and zero credential leaks.
- Credential integration: PASS.
- Credential wire E2E: PASS with zero unmasked execution-artifact hits, 38
  masked events, and 38 intact synthetic transmissions.
- Raw synthetic verifier and stub captures remain under `/tmp`; no real
  credential is recorded.

## External Dependency Integrity

- `/home/nh/docker/osh-core` is clean at
  `4c87a65c9a967d52af9df476e65d7862c7673a15` and zero commits ahead.
- The deployed `/opt/osh` mount is read-only.
- TeamEngine is inherited from the approved immutable digest.
- Sprint 50 makes no OSH or TeamEngine source or binary modification.

## Adversarial Review

Initial Raze review reported three required findings:

- unsupported or different-media canonical links could be selected before a
  later comparable representation;
- removing a sole canonical link produced `links: []`, which compared
  differently from an omitted optional `links` member; and
- the TestNG dependency comment still described historical SystemFeatures
  inheritance.

All three are remediated. Focused tests prove media-aware comparable-link
selection, all-target identity validation, no-comparable SKIP behavior, and
canonical-only versus omitted-link equivalence. The dependency comment now
states direct API Common inheritance. Focused Raze recheck returns `PASS` at
confidence `0.99` after 189 seconds, closes all three findings, and reports no
required fixes.
