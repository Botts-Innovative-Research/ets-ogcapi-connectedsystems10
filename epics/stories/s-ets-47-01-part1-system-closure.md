# S-ETS-47-01: Part 1 System Direct ATS Closure

## Status

CLOSED

## User Instruction

Triggered by the accepted recommendation to complete every released Part 1
gap continuously after API Common.

## Scope

Replace the historical System approximations with exact implementations of all
six released OGC 23-001 `/conf/system` procedures.

- Requirements: `REQ-ETS-PART1-002`, `REQ-ETS-COVERAGE-001`
- Scenarios:
  - `SCENARIO-ETS-PART1-002-RELEASED-LOCATION-001`
  - `SCENARIO-ETS-PART1-002-RELEASED-LOCATION-TIME-001`
  - `SCENARIO-ETS-PART1-002-RELEASED-CANONICAL-URL-001`
  - `SCENARIO-ETS-PART1-002-RELEASED-RESOURCES-ENDPOINT-001`
  - `SCENARIO-ETS-PART1-002-RELEASED-CANONICAL-ENDPOINT-001`
  - `SCENARIO-ETS-PART1-002-RELEASED-COLLECTIONS-001`
  - `SCENARIO-ETS-PART1-002-RELEASED-SCHEMA-FAIL-CLOSED-001`
  - `SCENARIO-ETS-PART1-002-RELEASED-MULTI-COLLECTION-001`
  - `SCENARIO-ETS-PART1-002-RELEASED-PROCEDURE-ISOLATION-001`
  - `SCENARIO-ETS-PART1-002-RELEASED-E2E-EXECUTION-001`
  - `SCENARIO-ETS-PART1-002-RELEASED-DIRECT-HTTP-COVERAGE-001`

## Acceptance Criteria

- [x] CP-007, this story, its contract, capability spec, design, architecture,
  traceability, and operations records define the increment before code.
- [x] The six deployed TestNG methods execute the complete released procedures.
- [x] Missing nonvirtual System locations produce warnings only.
- [x] Location-time uses an explicit optional moving-System input and reports
  SKIP when it is absent.
- [x] Canonical links are dereferenced and compared after canonical-link
  normalization for every advertised System collection item.
- [x] `/systems` and every System collection-items page are validated against
  the bundled schema selected by actual response media type; pinned complete
  GeoJSON schemas reject malformed wrappers, features, and geometries.
- [x] Every exact `featureType=sosa:System` collection is selected; the ETS does
  not add existence or `itemType` assertions absent from the released procedure,
  and unsupported collections do not hide later supported failures.
- [x] Every selected collection item reports one of the released System type
  URI/CURIEs.
- [x] SensorML movement compares positional coordinates, not orientation-only
  changes.
- [x] The resources-endpoint procedure is endpoint-parameterized and reused by
  canonical and collection procedures.
- [x] Each direct procedure retrieves only its own prerequisites; shared setup
  cannot suppress an unrelated released result.
- [x] The existing API Common reviewed helper signatures remain valid while
  detailed page evidence is exposed without duplicate retrieval.
- [x] The six methods have reviewed exact ATS mappings.
- [x] Focused/full Maven, coverage audit, local OSH TeamEngine E2E, exact-image
  runtime, dependency, and credential gates are archived.
- [x] Raze reports no unresolved required findings.

## Non-Goals

- Modifying or proxying OSH or TeamEngine.
- Treating SKIP as positive conformance evidence.
- Claiming the separate GeoJSON or SensorML conformance classes complete.
- Adding hosted CI.
