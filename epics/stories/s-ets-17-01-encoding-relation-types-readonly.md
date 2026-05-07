# Story S-ETS-17-01: Encoding Relation-Types Read-Only Link Checks

> Sprint: ets-17
> Status: GENERATOR COMPLETE - RAZE-APPROVED
> Priority: P0
> Complexity: S
> Epic: epic-ets-02-part1-classes
> OpenSpec: REQ-ETS-PART1-012, REQ-ETS-PART1-013

## User Value

As an OGC API Connected Systems server implementer, I need the ETS to verify that association links in GeoJSON and SensorML JSON use relation types that match the association names, without over-claiming from generic links or property-level link objects.

## Scope

Sprint 17 adds read-only checks for:

1. `/req/geojson/relation-types`
2. `/req/sensorml/relation-types`

The sprint remains PARTIAL for both `REQ-ETS-PART1-012` and `REQ-ETS-PART1-013`.

## Requirements

- REQ-ETS-PART1-012
- REQ-ETS-PART1-013
- SCENARIO-ETS-PART1-012-GEOJSON-RELATION-TYPES-001
- SCENARIO-ETS-PART1-013-SENSORML-RELATION-TYPES-001
- SCENARIO-ETS-PART1-012-013-RELATION-TYPES-FALLBACK-HONESTY-001
- SCENARIO-ETS-PART1-012-013-RELATION-TYPES-SMOKE-NO-MUTATION-001

## Planning Evidence

- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28, checked 2026-05-06, not stale.
- OGC GeoJSON source: `api/part1/standard/sections/clause_20_requirements_class_geojson_encoding.adoc`, fetched HTTP 200 on 2026-05-06.
- OGC SensorML source: `api/part1/standard/sections/clause_21_requirements_class_sensorml_encoding.adoc`, fetched HTTP 200 on 2026-05-06.
- Both clauses define the same relation-types rule: for associations encoded in a JSON `links` member, the link relation type must be the association name.
- GeoRobotix `/systems/0mqcvdnfoca0` default JSON contains `links` with generic `canonical`/`alternate` links and association links `samplingFeatures` and `datastreams`.
- GeoRobotix `/deployments/16sp744ch58g` default JSON contains only generic `canonical` and `alternate` links in `links`; `deployedSystems@link` is under `properties`, not the `links` member.
- GeoRobotix `/procedures/164p7ed8l47g` default JSON contains only generic `canonical` and `alternate` links in `links`.
- GeoRobotix `/samplingFeatures/0mtff3l0oofg` has no `links` member; `hostedProcedure@link` is under `properties`, not the `links` member.
- GeoRobotix SensorML system/deployment/procedure representations observed during planning do not expose association links in a top-level `links` member, so SensorML relation-types may SKIP honestly on this IUT until such links exist.
- Raze planning review `.harness/evaluations/sprint-ets-17-plan-adversarial.yaml` returned `GAPS_FOUND` confidence 0.88 for a global allowlist false PASS risk.
- Raze gap-fix review `.harness/evaluations/sprint-ets-17-plan-gapfix.yaml` returned `APPROVE` confidence 0.94 after planning was tightened with resource-specific GeoJSON/SensorML links-member association allowlists and wrong-resource rel rejection.

## Implemented Test Surface

1. Added read-only relation-types checks for GeoJSON and SensorML encoding suites.
2. GeoJSON relation-types checks are gated through the existing GeoJSON/SystemFeatures setup and fetch a selected `/systems/{id}` representation before judging resource links.
3. SensorML relation-types checks are gated on `/conf/sensorml` and explicit SensorML representation availability before judging SensorML links.
4. Treat `canonical`, `alternate`, pagination, collection, service-description, and service-document links as non-association links.
5. PASS only when every detected association encoded in a JSON `links` member uses a relation type valid for the selected encoding and resource type, using resource-specific OGC association tables:
   - GeoJSON System links-member associations: `parentSystem`, `subsystems`, `samplingFeatures`, `deployments`, `procedures`, `datastreams`, `controlstreams`.
   - Deployment links-member associations: `parentDeployment`, `subdeployments`, `featuresOfInterest`, `samplingFeatures`, `datastreams`, `controlstreams`.
   - Procedure links-member associations: `implementingSystems`.
   - GeoJSON Sampling Feature links-member associations: `parentSystem`, `sampleOf`, `datastreams`, `controlstreams`.
   - SensorML System links-member associations exclude `parentSystem` because that association maps to `attachedTo`, not `links`; SensorML has no Sampling Feature representation in this conformance class.
   - Property resources have no planned links-member association allowlist in Sprint 17.
6. SKIP when the selected representation has no association encoded in the `links` member.
7. FAIL when a links-member association URL is present but its `rel` is missing, generic, not the association name, or valid only for another resource type.
8. Do not apply this rule to property-level `@link` members such as `deployedSystems@link` or `hostedProcedure@link`; those belong to mapping assertions, not links-member relation-types.
9. Keep default GeoRobotix smoke non-mutating.

## Generator Implementation Evidence

- Added `EncodingRelationTypes` shared helper with resource-specific GeoJSON and SensorML links-member association allowlists.
- Added `geoJsonLinksMemberAssociationRelsUseResourceSpecificNames`; GeoRobotix PASSes from `/systems/0mqcvdnfoca0` association rels `samplingFeatures` and `datastreams`.
- Added `sensorMlLinksMemberAssociationRelsUseResourceSpecificNames`; GeoRobotix SKIPs honestly because the selected SensorML system representation has no top-level links-member association links.
- Added `VerifyEncodingRelationTypes` with 5 unit tests for resource-specific PASS, generic-only SKIP, wrong-resource FAIL, missing-rel FAIL, and SensorML rejection of GeoJSON-only `parentSystem`.
- Formatter: Docker Maven `spring-javaformat:apply` BUILD SUCCESS.
- Maven: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `133 tests / 0 failures / 0 errors / 3 skipped`, log `ops/test-results/sprint-ets-17-maven-2026-05-06.log`.
- TeamEngine smoke: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s17-generator bash scripts/smoke-test.sh`, `82 total / 55 passed / 0 failed / 27 skipped`.
- Smoke no-mutation oracle: 55 recognized IUT-bound request-log entries and zero IUT-bound POST/PUT/DELETE/PATCH entries.
- Raze implementation review `.harness/evaluations/sprint-ets-17-adversarial-implementation.yaml` returned `APPROVE` confidence 0.91 with no required fixes.

## Definition of Done

- [x] OpenSpec, story, contract, traceability, planner handoff, status, changelog, and test-results are reconciled for Sprint 17 planning.
- [x] Raze reviews Sprint 17 planning changes.
- [x] Generator implements clear PASS/SKIP/FAIL semantics for association links.
- [x] Implementation uses resource-specific links-member association allowlists, not a single global allowlist.
- [x] Implementation keeps both REQ-ETS-PART1-012 and REQ-ETS-PART1-013 PARTIAL.
- [x] Implementation excludes mediatype-write, mutation, full schema validation, and Part 2.
- [x] Raze reviews Sprint 17 implementation changes.

## Out Of Scope

- `/req/geojson/mediatype-write`
- `/req/sensorml/mediatype-write`
- Full GeoJSON or SensorML JSON Schema validation
- Property-level `@link` mapping checks beyond existing mapping assertions
- Create/Replace/Delete or Update mutation behavior
- Part 2 work
