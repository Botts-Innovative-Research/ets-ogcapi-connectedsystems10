# S-ETS-53-01: Part 1 Property Definitions Direct ATS Closure

## Status

COMPLETE

## User Instruction

Triggered by the accepted recommendation to complete released Part 1 gaps
continuously after Sampling Features.

## Scope

Replace the historical Property Definitions approximations with exact
implementations of all four released OGC 23-001 `/conf/property` procedures.

- Requirements: `REQ-ETS-PART1-008`, `REQ-ETS-COVERAGE-001`,
  `REQ-ETS-VALIDATOR-001`
- Scenarios:
  - `SCENARIO-ETS-PART1-008-RELEASED-CANONICAL-URL-001`
  - `SCENARIO-ETS-PART1-008-RELEASED-RESOURCES-ENDPOINT-001`
  - `SCENARIO-ETS-PART1-008-RELEASED-CANONICAL-ENDPOINT-001`
  - `SCENARIO-ETS-PART1-008-RELEASED-COLLECTIONS-001`
  - `SCENARIO-ETS-PART1-008-RELEASED-MEDIA-GATE-001`
  - `SCENARIO-ETS-PART1-008-RELEASED-SENSORML-SCHEMA-001`
  - `SCENARIO-ETS-PART1-008-RELEASED-CANONICAL-EQUIVALENCE-001`
  - `SCENARIO-ETS-PART1-008-RELEASED-COLLECTION-COMPLETE-001`
  - `SCENARIO-ETS-PART1-008-RELEASED-PROCEDURE-ISOLATION-001`
  - `SCENARIO-ETS-PART1-008-RELEASED-DEPENDENCY-CASCADE-001`
  - `SCENARIO-ETS-PART1-008-RELEASED-VALIDATOR-BOUNDARY-001`
  - `SCENARIO-ETS-PART1-008-RELEASED-E2E-EXECUTION-001`
  - `SCENARIO-ETS-PART1-008-RELEASED-DIRECT-HTTP-COVERAGE-001`

## Acceptance Criteria

- [x] CP-013, this story, contract, capability requirement/scenarios, design,
  architecture, traceability, epic, and baseline define the increment before
  code.
- [x] Exactly four deployed methods implement the four released procedures.
- [x] Every procedure retrieves only its own evidence and has no method
  dependency.
- [x] Every page is status/media gated before parsing and traversed with
  bounded same-origin pagination.
- [x] Supported Property endpoint pages pass the released SensorML Property
  collection schema.
- [x] The release's undefined `{sensorml-mediatype}` token is explicitly
  interpreted as `application/sml+json`.
- [x] Resolver-normalized bundled Property schemas prove semantic and
  transitive-reference parity with the pinned release.
- [x] Every advertised `sosa:Property` collection has a non-empty ID and a
  supported items representation or produces an explicit evidence SKIP.
- [x] Every supported collection item has a same-origin canonical Property URL
  and equivalent dereferenced content.
- [x] API Common prerequisite failures cascade before Property IUT access;
  System and unrelated siblings cannot block the class.
- [x] SensorML validation remains behind the ETS-owned replaceable adapter
  boundary, does not import the executable SensorML suite jar, and does not
  claim closure of `/conf/sensorml/property-schema`.
- [x] All four methods have reviewed exact ATS mappings.
- [x] Focused/full Maven, coverage audit, local OSH TeamEngine E2E, controlled
  HTTP, exact-image runtime, dependency, credential, and artifact-hygiene gates
  are archived.
- [x] Local OSH unsupported media and missing Property collection remain
  visible and are not repaired through OSH or TeamEngine changes.
- [x] Raze reports no unresolved required findings.

## Baseline

- Local OSH declares `/conf/property`.
- `/properties` returns HTTP 200 `application/json` with an empty `items`
  array, including when `application/sml+json` is requested.
- `/collections` advertises no collection with `itemType=sosa:Property`.
- The endpoint procedures must SKIP on unsupported actual media, collections
  must FAIL for the missing required collection, and canonical URL must SKIP
  for missing evidence.
- Deployed OSH and TeamEngine source/binaries remain unmodified.

## Completion Evidence

Implementation and verification are complete. Evidence is recorded in
`ops/test-results/sprint-ets-53-part1-property-definitions-verification-2026-07-28.md`.
Raze returned `APPROVE_WITH_CONCERNS` at confidence `0.98`, with two LOW
non-blocking hardening concerns and no required fixes.
