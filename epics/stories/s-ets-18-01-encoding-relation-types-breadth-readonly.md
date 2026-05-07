# Story S-ETS-18-01: Encoding Relation-Types Breadth Read-Only Checks

> Sprint: ets-18
> Status: PLANNED - RAZE-APPROVED
> Priority: P0
> Complexity: S
> Epic: epic-ets-02-part1-classes
> OpenSpec: REQ-ETS-PART1-012, REQ-ETS-PART1-013

## User Value

As an OGC API Connected Systems server implementer, I need relation-types checks to cover the available GeoJSON and SensorML resource classes individually, so a server cannot pass an encoding relation-types subset only because one System item has valid links.

## Scope

Sprint 18 broadens Sprint 17 from selected System-only runtime assertions to selected-resource assertions for each relevant encoding/resource pair:

1. GeoJSON System, Deployment, Procedure, and Sampling Feature representations.
2. SensorML System, Deployment, and Procedure representations.

The sprint remains PARTIAL for both `REQ-ETS-PART1-012` and `REQ-ETS-PART1-013`.

## Requirements

- REQ-ETS-PART1-012
- REQ-ETS-PART1-013
- SCENARIO-ETS-PART1-012-GEOJSON-RELATION-TYPES-001
- SCENARIO-ETS-PART1-013-SENSORML-RELATION-TYPES-001
- SCENARIO-ETS-PART1-012-GEOJSON-RELATION-TYPES-BREADTH-001
- SCENARIO-ETS-PART1-013-SENSORML-RELATION-TYPES-BREADTH-001
- SCENARIO-ETS-PART1-012-013-RELATION-TYPES-FALLBACK-HONESTY-001
- SCENARIO-ETS-PART1-012-013-RELATION-TYPES-BREADTH-NO-MUTATION-001

## Planning Evidence

- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28, checked 2026-05-07, not stale.
- OGC GeoJSON source: `api/part1/standard/sections/clause_20_requirements_class_geojson_encoding.adoc`, fetched HTTP 200 on 2026-05-07.
- OGC SensorML source: `api/part1/standard/sections/clause_21_requirements_class_sensorml_encoding.adoc`, fetched HTTP 200 on 2026-05-07.
- Both clauses keep the same relation-types rule: for associations encoded in a JSON `links` member, the link relation type must be the association name.
- GeoRobotix `/systems/0mqcvdnfoca0` default JSON contains links-member association rels `samplingFeatures` and `datastreams`; this remains positive GeoJSON System evidence.
- GeoRobotix `/deployments/16sp744ch58g` default JSON contains only generic `canonical` and `alternate` links; `deployedSystems@link` remains under `properties`, not `links`.
- GeoRobotix `/procedures/164p7ed8l47g` default JSON contains only generic `canonical` and `alternate` links.
- GeoRobotix `/samplingFeatures/0mtff3l0oofg` default JSON has no `links` member; `hostedProcedure@link` remains under `properties`, not `links`.
- GeoRobotix SensorML system, deployment, and procedure bodies observed on 2026-05-07 have no top-level `links` member, so those breadth checks should SKIP honestly on the default IUT until such links exist.
- Raze planning review `.harness/evaluations/sprint-ets-18-plan-adversarial.yaml` returned `APPROVE` confidence 0.92 with no required fixes.

## Planned Test Surface

1. Reuse or extend `EncodingRelationTypes`; keep encoding/resource-specific allowlists.
2. Add individual read-only relation-types checks for GeoJSON Deployment, Procedure, and Sampling Feature, in addition to the existing System check.
3. Add individual read-only relation-types checks for SensorML Deployment and Procedure, in addition to the existing System check.
4. Gate each non-system check on the corresponding resource conformance class and selected item availability.
5. PASS only when every detected links-member association rel is valid for that exact encoding/resource pair.
6. SKIP when the selected representation has no `links` member or only generic links after exclusions.
7. FAIL when a links-member entry has missing/blank `rel` or a non-generic rel not valid for the selected encoding/resource pair.
8. Do not traverse or count property-level `@link` objects such as `deployedSystems@link` or `hostedProcedure@link`.
9. Keep default GeoRobotix smoke non-mutating.

## Definition of Done

- [x] OpenSpec, story, contract, traceability, planner handoff, status, changelog, known issues, and test-results are reconciled for Sprint 18 planning.
- [x] Raze reviews Sprint 18 planning changes.
- [x] Planning keeps both REQ-ETS-PART1-012 and REQ-ETS-PART1-013 PARTIAL.
- [x] Planning requires resource-specific relation-types checks per resource class, not a single aggregate assertion that can hide SKIPs.
- [x] Planning excludes mediatype-write, mutation, full schema validation, and Part 2.

## Out Of Scope

- `/req/geojson/mediatype-write`
- `/req/sensorml/mediatype-write`
- Full GeoJSON or SensorML JSON Schema validation
- Property-level `@link` mapping checks beyond existing mapping assertions
- Create/Replace/Delete or Update mutation behavior
- Part 2 work
