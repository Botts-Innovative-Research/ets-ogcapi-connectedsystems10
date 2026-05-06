# Story S-ETS-15-01: GeoJSON Non-System Read-Only Expansion

> Sprint: ets-15
> Status: IMPLEMENTED
> Priority: P0
> Complexity: M
> Epic: epic-ets-02-part1-classes
> OpenSpec: REQ-ETS-PART1-012

## User Value

As an OGC API Connected Systems server implementer, I need GeoJSON encoding checks to cover deployment, procedure, and sampling feature resources, while the ETS stays honest when an IUT declares `/conf/geojson` but returns the default CS API `items` representation instead of GeoJSON `features`.

## Scope

Extend the Sprint 9 `/conf/geojson` systems subset. This sprint does not add a new Part 1 conformance class. It adds read-only non-system GeoJSON schema and mapping coverage for:

1. `/deployments`
2. `/procedures`
3. `/samplingFeatures`

The sprint remains PARTIAL for `REQ-ETS-PART1-012`.

## Requirements

- REQ-ETS-PART1-012
- SCENARIO-ETS-PART1-012-GEOJSON-DEPLOYMENT-SCHEMA-MAPPING-001
- SCENARIO-ETS-PART1-012-GEOJSON-PROCEDURE-SCHEMA-MAPPING-001
- SCENARIO-ETS-PART1-012-GEOJSON-SF-SCHEMA-MAPPING-001
- SCENARIO-ETS-PART1-012-GEOJSON-NON-SYSTEM-FALLBACK-HONESTY-001
- SCENARIO-ETS-PART1-012-GEOJSON-SMOKE-NO-MUTATION-001

## Planning Evidence

- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28, checked 2026-05-06, not stale.
- OGC upstream source: `api/part1/standard/requirements/encoding/geojson/requirements_class_geojson.adoc`, fetched HTTP 200 on 2026-05-06.
- Requirement class identifier: `/req/geojson`.
- Relevant subrequirements: `/req/geojson/deployment-schema`, `/req/geojson/deployment-mappings`, `/req/geojson/procedure-schema`, `/req/geojson/procedure-mappings`, `/req/geojson/sf-schema`, and `/req/geojson/sf-mappings`.
- GeoRobotix `/conformance` returned HTTP 200 and declares `/conf/geojson`, `/conf/deployment`, `/conf/procedure`, and `/conf/sf`.
- GeoRobotix `GET /deployments?limit=1` with `Accept: application/geo+json` returned HTTP 200, `Content-Type: application/json`, and top-level `items`.
- GeoRobotix `GET /procedures?limit=1` with `Accept: application/geo+json` returned HTTP 200, `Content-Type: application/json`, and top-level `items` plus `links`.
- GeoRobotix `GET /samplingFeatures?limit=1` with `Accept: application/geo+json` returned HTTP 200, `Content-Type: application/json`, and top-level `items` plus `links`.
- Raze planning review: `.harness/evaluations/sprint-ets-15-plan-adversarial.yaml` returned `GAPS_FOUND` confidence 0.86 because generic Feature shape was not enough for resource-specific schema/mapping claims.
- Raze planning gap-fix recheck: `.harness/evaluations/sprint-ets-15-plan-gapfix.yaml` returned `APPROVE` confidence 0.93 after the plan added deployment, procedure, and sampling-feature-specific predicates.

## Planned Test Surface

1. Add read-only checks that request the three non-system collections with `Accept: application/geo+json`.
2. For each collection, require GeoJSON `FeatureCollection` shape with `features` before any schema/mapping PASS is possible.
3. If the response is a CS API default `items` wrapper without `features`, SKIP with a requirement-cited fallback reason rather than PASS.
4. When a `features` array is present, validate the first feature has `type="Feature"`, `id`, `geometry` present and either a GeoJSON geometry or null, and `properties`.
5. Add at least one resource-specific schema/mapping predicate per resource type before claiming PASS:
   - Deployment: `properties.uid` and a deployment association such as `properties.deployedSystems@link`.
   - Procedure: `geometry == null` and `properties.uid` plus `properties.featureType`.
   - Sampling Feature: `properties.uid`, `properties.featureType`, and a sampling-feature-specific association or attribute such as `properties.hostedProcedure@link` or `properties.radius`.
6. Preserve the existing `geojson -> systemfeatures -> core` dependency wiring.
7. Keep default GeoRobotix smoke non-mutating.

## Implementation Notes

- Added three read-only GeoJSON @Tests in `GeoJsonTests` for `/deployments`, `/procedures`, and `/samplingFeatures`.
- Each non-system path requires GeoJSON `FeatureCollection` + `features` before PASS and SKIPs CS API default `items` wrappers with requirement-cited fallback reasons.
- Resource-specific predicates prevent generic Feature shape from closing schema/mapping claims:
  - Deployment: `properties.uid` and non-empty `properties.deployedSystems@link`.
  - Procedure: `geometry == null`, `properties.uid`, and `properties.featureType`.
  - Sampling Feature: `properties.uid`, `properties.featureType`, and non-empty `properties.hostedProcedure@link` or `properties.radius`.
- Added `VerifyGeoJsonResourceMappingAssertions` regression coverage for fallback SKIP and mapping-value helper behavior.
- Maven verification: `bash scripts/mvn-test-via-docker.sh`, BUILD SUCCESS, `122 tests / 0 failures / 0 errors / 3 skipped`; log `ops/test-results/sprint-ets-15-maven-2026-05-06.log`.
- TeamEngine smoke: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s15-generator bash scripts/smoke-test.sh`, `77 total / 52 passed / 0 failed / 25 skipped`; report `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s15-generator/s-ets-01-03-teamengine-smoke-2026-05-06.xml`.
- No-mutation oracle: smoke recognized 44 IUT-bound request-log entries and found zero IUT-bound POST/PUT/DELETE/PATCH entries against GeoRobotix.

## Definition of Done

- [x] `GeoJsonTests` covers deployment, procedure, and sampling feature read-only GeoJSON schema/mapping paths.
- [x] New tests include comments or descriptions referencing `REQ-ETS-PART1-012` and the matching `SCENARIO-*` IDs.
- [x] CS API `items` wrappers without GeoJSON `features` do not count as PASS evidence.
- [x] Generic GeoJSON Feature shape alone does not close deployment/procedure/sampling-feature schema and mapping predicates.
- [x] No POST, PUT, PATCH, or DELETE is issued by default smoke.
- [x] Docker Maven and TeamEngine smoke are run and recorded.
- [x] OpenSpec, story, traceability, status, changelog, and test-results are reconciled.
- [x] Raze reviews Sprint 15 planning changes.
- [x] Raze reviews implementation changes before completion.

## Out Of Scope

- `/req/geojson/mediatype-write`
- `/req/geojson/relation-types`
- Property definition GeoJSON mapping
- Full external GeoJSON JSON Schema validation
- SensorML work
- Part 2 work
- Any mutation request
