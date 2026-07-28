# S-ETS-54-01: Part 1 GeoJSON Direct ATS Closure

## Status

IN PROGRESS

## User Instruction

Triggered by the user's instruction to execute the recommended next step:
complete released Part 1 `/conf/geojson` and carry forward both Sprint 53 Raze
hardening concerns.

## Scope

Replace the historical GeoJSON approximations with exact implementations of
all twelve released OGC 23-001 `/conf/geojson` procedures.

- Requirements: `REQ-ETS-PART1-012`, `REQ-ETS-COVERAGE-001`
- Scenarios:
  - `SCENARIO-ETS-PART1-012-RELEASED-MEDIATYPE-READ-001`
  - `SCENARIO-ETS-PART1-012-RELEASED-MEDIATYPE-WRITE-001`
  - `SCENARIO-ETS-PART1-012-RELEASED-RELATION-TYPES-001`
  - `SCENARIO-ETS-PART1-012-RELEASED-FEATURE-MAPPING-001`
  - `SCENARIO-ETS-PART1-012-RELEASED-SCHEMAS-001`
  - `SCENARIO-ETS-PART1-012-RELEASED-RESOURCE-MAPPINGS-001`
  - `SCENARIO-ETS-PART1-012-RELEASED-MEDIA-GATE-001`
  - `SCENARIO-ETS-PART1-012-RELEASED-PROCEDURE-ISOLATION-001`
  - `SCENARIO-ETS-PART1-012-RELEASED-DEPENDENCY-CASCADE-001`
  - `SCENARIO-ETS-PART1-012-RELEASED-SCHEMA-PARITY-001`
  - `SCENARIO-ETS-PART1-012-RELEASED-DIRECT-HTTP-COVERAGE-001`
  - `SCENARIO-ETS-PART1-012-RELEASED-E2E-EXECUTION-001`
  - `SCENARIO-ETS-PART1-012-S53-HARDENING-001`

## Acceptance Criteria

- [x] CP-014, this story, contract, capability requirement/scenarios, design,
  architecture, traceability, epic, and baseline define the increment before
  code.
- [ ] Exactly twelve deployed methods implement the twelve released procedures.
- [ ] Every procedure retrieves only its own evidence and has no method
  dependency.
- [ ] API-definition inspection accepts JSON and YAML and never mutates the IUT.
- [ ] Every required canonical and advertised custom-collection GET operation
  advertises `application/geo+json`.
- [ ] At least one canonical POST or PUT operation advertises
  `application/geo+json` request content.
- [ ] Every schema procedure validates complete single and collection
  documents against the released schemas.
- [ ] Every supported page is status/media gated before parsing and traversed
  with bounded same-origin pagination.
- [ ] Common and resource-specific mapping checks process all inspectable
  features without requiring absent optional associations.
- [ ] Relation-types aggregates across all resource types and cannot PASS
  without association relation evidence.
- [ ] API Common prerequisite failures cascade before GeoJSON IUT access;
  System and unrelated siblings cannot block the class.
- [ ] All eight released entry schemas prove pinned-source semantic and
  transitive-reference parity.
- [ ] Property parity provenance fails closed and dedicated Property
  pagination/later-evidence regressions pass.
- [ ] All twelve methods have reviewed exact ATS mappings.
- [ ] Focused/full Maven, coverage audit, local OSH TeamEngine E2E, controlled
  HTTP, exact-image runtime, dependency, credential, and artifact-hygiene gates
  are archived.
- [ ] Local OSH generic JSON fallback remains visible and is not repaired
  through OSH or TeamEngine changes.
- [ ] Raze reports no unresolved required findings.

## Baseline

- Local OSH declares Connected Systems `/conf/geojson`, OGC API Features
  `/conf/geojson`, and the four canonical feature-resource classes.
- Its landing page advertises external OpenAPI 3.1 YAML `service-desc` links.
- Canonical System, Deployment, Procedure, and Sampling Feature collection
  requests with `Accept: application/geo+json` return HTTP 200
  `application/json`, not `application/geo+json`.
- Schema and mapping procedures must therefore SKIP honestly at their
  actual-media gates on the primary IUT.
- Deployed OSH and TeamEngine source/binaries remain unmodified.

## Completion Evidence

Pending implementation and all mandatory gates.
