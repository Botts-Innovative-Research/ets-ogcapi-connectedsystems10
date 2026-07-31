# S-ETS-58-01: Part 1 SensorML Direct ATS Closure

## Status

IN PROGRESS

## User Instruction

Triggered by the user's instruction to start the next recommended project step.

## Scope

Replace the historical SensorML subset with all fifteen released OGC 23-001
`/conf/sensorml` procedures and introduce the provisional ETS-owned SensorML
validator adapter over pinned released schemas.

- Requirements: `REQ-ETS-PART1-013`, `REQ-ETS-VALIDATOR-001`,
  `REQ-ETS-COVERAGE-001`
- Scenarios:
  - `SCENARIO-ETS-PART1-013-RELEASED-PROCEDURES-001`
  - `SCENARIO-ETS-PART1-013-DIRECT-PREREQUISITES-001`
  - `SCENARIO-ETS-PART1-013-RELEASED-MEDIA-ADVERTISEMENT-001`
  - `SCENARIO-ETS-PART1-013-VALIDATOR-ADAPTER-001`
  - `SCENARIO-ETS-PART1-013-RELEASED-SCHEMAS-001`
  - `SCENARIO-ETS-PART1-013-RELEASED-RESOURCE-ID-001`
  - `SCENARIO-ETS-PART1-013-RELEASED-COMMON-MAPPINGS-001`
  - `SCENARIO-ETS-PART1-013-RELEASED-RESOURCE-MAPPINGS-001`
  - `SCENARIO-ETS-PART1-013-RELEASED-CLASS-COMPATIBILITY-001`
  - `SCENARIO-ETS-PART1-013-RELEASED-RELATION-TYPES-001`
  - `SCENARIO-ETS-PART1-013-RELEASED-MEDIA-GATE-001`
  - `SCENARIO-ETS-PART1-013-RELEASED-PROCEDURE-ISOLATION-001`
  - `SCENARIO-ETS-PART1-013-RELEASED-SCHEMA-PARITY-001`
  - `SCENARIO-ETS-PART1-013-RELEASED-DIRECT-HTTP-COVERAGE-001`
  - `SCENARIO-ETS-PART1-013-RELEASED-E2E-EXECUTION-001`

## Acceptance Criteria

- [x] CP-018, this story, contract, capability scenarios, design,
  architecture, traceability, epic, and operational handoff define the
  increment before code.
- [ ] Exactly fifteen independent TestNG methods map the released procedures.
- [ ] Part 1 API Common is the only direct TestNG prerequisite.
- [ ] API metadata procedures parse JSON/YAML OpenAPI and issue no writes.
- [ ] Four independent schema procedures validate complete single and
  collection SensorML documents through the ETS-owned adapter.
- [ ] The adapter exposes immutable deterministic diagnostics, separates
  operational failures, and has no TestNG or requirement-URI dependency.
- [ ] Every available canonical resource is inspected for exact id, common
  attributes, resource mappings, class compatibility, and association relation
  semantics as applicable.
- [ ] Mapping procedures validate mapped geometry/Pose schema semantics,
  required association target resource/endpoint schemas, credential-bearing
  same-origin resolution, credential-free cross-origin resolution, and the
  released AssetType value forms without accepting unbound CURIEs.
- [ ] Advertised malformed API definitions and default-only response metadata
  fail rather than becoming absent-evidence SKIPs.
- [ ] Actual media is established before parsing and complete pagination is
  bounded, cycle-safe, and same-origin.
- [ ] Eight entry schemas and their transitive graph match the pinned released
  source under resolver-normalized semantic comparison.
- [ ] Requirement-linked negative tests reproduce the historical false-PASS
  paths before implementation and pass afterward.
- [ ] Controlled HTTP covers all fifteen positive procedures and key
  fail-closed or honest-SKIP branches, including malformed metadata, target
  types and collections, cross-origin credential isolation, geometry/Pose,
  unsafe or cyclic pagination, later-page media, omission, and no-evidence
  behavior.
- [ ] Focused and full Docker Maven verification pass.
- [ ] Exact-image TeamEngine executes against unmodified local OSH with zero
  IUT writes and honest SensorML outcomes.
- [ ] Coverage is regenerated and all fifteen mappings are reviewed.
- [ ] Fresh Raze reports no unresolved required findings.

## Baseline

- Released source tag `v1.0.0`, commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`, defines fifteen procedures.
- Current coverage is `0 exact / 0 helper / 12 candidate / 3 unmapped`.
- The current class has thirteen methods, including two non-ATS methods,
  combines multiple procedures, and has a historical System Features
  dependency.
- The released SensorML schema graph is already bundled and source-pinned.
- No reusable public FCU-GIS-Luke SensorML validator module exists; the
  `ets-sensorml30` executable suite jar is not a dependency target.

## Completion Evidence

Pending implementation and gate execution.
