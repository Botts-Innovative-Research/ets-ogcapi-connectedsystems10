# S-ETS-58-01: Part 1 SensorML Direct ATS Closure

## Status

DONE

## User Instruction

Triggered by the user's instruction to start the next recommended project step.

## Scope

Replace the historical SensorML subset with all fifteen released OGC 23-001
`/conf/sensorml` procedures and introduce the first-party ETS-owned SensorML
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
- [x] Exactly fifteen independent TestNG methods map the released procedures.
- [x] Part 1 API Common is the only direct TestNG prerequisite.
- [x] API metadata procedures parse OpenAPI 3.0/3.1 JSON/YAML, resolve relative
  operation references from the advertised URI, preserve bounded recursive
  schema reference graphs, enforce scheme/origin/redirect/cycle/depth/traversal/
  unique-read/body/time bounds, stream root descriptions, pin the IUT origin
  before landing-page access, pin one validated public address set for each
  cross-origin graph, forward credentials only to exact-IUT-origin descriptions
  and references, interrupt blocking loads at the global deadline, and issue no
  writes.
- [x] The final TeamEngine image executes the isolated OpenAPI 3.1 parser
  closure and external-fetch security probes from the ETS jar without adding
  or replacing TeamEngine-owned jars.
- [x] Four independent schema procedures validate complete single and
  collection SensorML documents through the ETS-owned adapter.
- [x] The adapter exposes immutable deterministic diagnostics, separates
  operational failures, and has no TestNG or requirement-URI dependency.
- [x] Every available canonical resource is inspected for exact id, common
  attributes, resource mappings, class compatibility, and association relation
  semantics as applicable.
- [x] Mapping procedures validate mapped geometry/Pose schema semantics,
  required association target resource/endpoint schemas, credential-bearing
  same-origin resolution, credential-free cross-origin resolution, and the
  released AssetType value forms without accepting unbound CURIEs.
- [x] Advertised malformed API definitions and default-only response metadata
  fail rather than becoming absent-evidence SKIPs.
- [x] Actual media is established before parsing and complete pagination is
  bounded, cycle-safe, and same-origin.
- [x] Eight entry schemas and their transitive graph match the pinned released
  source under resolver-normalized semantic comparison.
- [x] Requirement-linked negative tests reproduce the historical false-PASS
  paths before implementation and pass afterward.
- [x] Controlled HTTP covers all fifteen positive procedures and key
  fail-closed or honest-SKIP branches, including malformed metadata, target
  types and collections, cross-origin credential isolation, geometry/Pose,
  unsafe or cyclic pagination, later-page media, omission, and no-evidence
  behavior.
- [x] Focused and full Docker Maven verification pass.
- [x] Exact-image TeamEngine executes against unmodified local OSH with zero
  IUT writes and honest SensorML outcomes.
- [x] Coverage is regenerated and all fifteen mappings are reviewed.
- [x] Fresh Raze reports no unresolved required findings.

## Baseline

- Released source tag `v1.0.0`, commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`, defines fifteen procedures.
- The Sprint 58 preimplementation baseline was
  `0 exact / 0 helper / 12 candidate / 3 unmapped`.
- The preimplementation class had thirteen methods, including two non-ATS
  methods, combined multiple procedures, and had a historical System Features
  dependency. The completed replacement has fifteen independent released
  methods and `15 exact / 0 candidate / 0 unmapped`.
- The released SensorML schema graph is already bundled and source-pinned.
- No reusable public FCU-GIS-Luke SensorML validator module exists; the
  `ets-sensorml30` executable suite jar is not a dependency target.

## Completion Evidence

- Exact source candidate:
  `a593953d8d79d977649db3077696148e90ffb44a`.
- Exact E2E image:
  `sha256:c0227ab3ef9d67a27d8d22a119979eda7615df10bbbc43c9e50a52daffdff093`.
- Focused SensorML verification: `37/0/0/0`; clean Docker Maven:
  `729/0/0/3`.
- Released schema parity: 8 entry and 63 transitive schemas with zero graph or
  semantic mismatch.
- Coverage: SensorML `15 exact / 0 candidate / 0 unmapped`; overall
  `240 total / 91 exact / 2 helper / 118 candidate / 29 unmapped`.
- Exact unmodified-local-OSH TeamEngine: `246/41/21/184`. All fifteen
  SensorML procedures executed; one passed and fourteen failed honestly on
  unsupported OSH SensorML collection media or incomplete advertised OpenAPI
  read-media evidence.
- No-mutation oracle: 194 recognized IUT requests, zero writes. Artifact
  hygiene and both credential gates passed.
- Dependency sabotage: `246/2/10/234`; all fifteen SensorML procedures
  dependency-skipped after Core failure.
- Exact-image runtime passed both validator adapters, OpenAPI 3.1 and
  external-fetch security probes, dependency parity, base immutability, and
  confidential-history hygiene.
- Final implementation Raze: `APPROVED`, confidence `0.99`. Reconciliation
  Raze's two chronology findings are closed by focused recheck
  `APPROVED 0.99`; no required fixes remain.
- Evidence:
  `ops/test-results/sprint-ets-58-part1-sensorml-final-a593953-2026-07-31/`.
