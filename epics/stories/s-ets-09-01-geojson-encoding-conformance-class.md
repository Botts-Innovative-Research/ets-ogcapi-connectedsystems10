# Story: S-ETS-09-01

**Epic**: epic-ets-02-part1-classes
**Priority**: P0
**Estimated Complexity**: M

## Description

Implement a GeoJSON systems read-only subset as the first Sprint 9 Part 1 class. This story intentionally does not close the full GeoJSON requirement class; SensorML remains deferred because its SensorML 3.0 schema inheritance is broader, and create-replace-delete remains deferred because it involves mutation-side risk.

The class follows the established Part 1 pattern:
- New `GeoJsonTests.java` with 5 read-only @Tests
- Suite-level dependency on SystemFeatures via `testng.xml`
- VerifyTestNGSuiteDependency extended with 3 lint tests for the `geojson` group
- SKIP-with-reason when `/conf/geojson` is not declared
- No `mediatype-write`, POST, PUT, PATCH, or DELETE assertions in Sprint 9

## Acceptance Criteria

- SCENARIO-ETS-PART1-012-GEOJSON-CONFORMANCE-DECLARED-001 (CRITICAL)
- SCENARIO-ETS-PART1-012-GEOJSON-MEDIATYPE-READ-001 (CRITICAL)
- SCENARIO-ETS-PART1-012-GEOJSON-FEATURECOLLECTION-001 (CRITICAL)
- SCENARIO-ETS-PART1-012-GEOJSON-FEATURE-MAPPING-001 (CRITICAL)
- SCENARIO-ETS-PART1-012-GEOJSON-DEPENDENCY-SMOKE-001 (CRITICAL)

## Spec References

- REQ-ETS-PART1-012 (GeoJSON Encoding Conformance Class `/conf/geojson` — SPECIFIED → target PARTIAL-IMPLEMENTED for systems read-only subset)

## Technical Notes

### OGC Requirement Structure

Planner verified upstream source on 2026-05-05:

- Repository: `opengeospatial/ogcapi-connected-systems`
- Branch: `master`
- Commit: `3fd86c73e744b7e2faaf7f1c17366bfb9ff4cd6f`
- Path: `api/part1/standard/requirements/encoding/geojson/requirements_class_geojson.adoc`
- Class identifier: `/req/geojson`

The class file lists these subrequirements:

- `/req/geojson/mediatype-read`
- `/req/geojson/mediatype-write`
- `/req/geojson/relation-types`
- `/req/geojson/feature-attribute-mapping`
- `/req/geojson/system-schema`
- `/req/geojson/system-mappings`
- `/req/geojson/deployment-schema`
- `/req/geojson/deployment-mappings`
- `/req/geojson/procedure-schema`
- `/req/geojson/procedure-mappings`
- `/req/geojson/sf-schema`
- `/req/geojson/sf-mappings`

Sprint 9 covers a systems read-only subset. `mediatype-write` remains OPEN and is conditional on the create-replace-delete requirement class being implemented or explicitly selected. Deployment, Procedure, and Sampling Feature GeoJSON schema/mapping subrequirements also remain OPEN.

### Expected @Tests

1. `geojsonConformanceDeclared` — `/conformance` declares `/conf/geojson`, otherwise the suite SKIPs with reason.
2. `geojsonMediaTypeRead` — `GET /systems` with `Accept: application/geo+json` returns HTTP 200 and GeoJSON-compatible JSON, or records an explicit default-JSON fallback when the representation is valid GeoJSON but media-type negotiation is not advertised.
3. `systemsCollectionIsGeoJsonFeatureCollection` — `/systems` GeoJSON response requires `type="FeatureCollection"` and a `features` array. A CS API `items` wrapper alone is not a passing GeoJSON FeatureCollection assertion; record it as default CS API JSON fallback evidence, SKIP, or CONCERN according to observed IUT behavior.
4. `systemFeatureHasGeoJsonShapeAndProperties` — selected feature has `type="Feature"`, `id`, `geometry` as GeoJSON geometry or null, and `properties`.
5. `geojsonDependencyCascadeRuntime` — runtime tracer or structural assertion proving `geojson` depends on `systemfeatures`.

### Dependency Wiring

TestNG wiring in `testng.xml`:

```xml
<group name="geojson" depends-on="systemfeatures"/>
```

All @Tests use the project-standard suite-level dependency pattern:

```java
@Test(groups = {"geojson"})
```

### VerifyTestNGSuiteDependency Extension

Add 3 lint tests:

1. `testGeoJsonGroupDependsOnSystemFeatures`
2. `testEveryGeoJsonTestMethodCarriesGeoJsonGroup`
3. `testGeoJsonCoLocatedWithSystemFeatures`

### Relationship to Existing Code

Use the sister repo's existing conformance-class patterns:

- `conformance.systemfeatures.SystemFeaturesTests`
- `conformance.samplingfeatures.SamplingFeaturesTests`
- `conformance.propertydefinitions.PropertyDefinitionsTests`
- `conformance.subdeployments.SubdeploymentsTests`

The frozen v1.0 web app has useful prior logic in `src/engine/registry/geojson.ts`, but it is not authoritative for canonical OGC URI form. Generator must treat the upstream `.adoc` source and current Java ETS patterns as authoritative.

## Dependencies

- Sprint 8 close commit `fb01e40` in `csapi_compliance`
- Sister ETS repo Sprint 8 close state with smoke baseline 46 total / 40 PASS / 0 FAIL / 6 SKIP
- SystemFeatures group remains the direct prerequisite for GeoJSON

## Definition of Done

- [x] `GeoJsonTests.java` exists at `src/main/java/.../conformance/geojson/`
- [x] 5 @Test methods carry `groups={"geojson"}`
- [x] `testng.xml` includes `<group name="geojson" depends-on="systemfeatures"/>` and class entry in the single-block consolidation
- [x] VerifyTestNGSuiteDependency has 3 GeoJSON lint tests
- [x] No write-operation assertions are implemented in Sprint 9; `/req/geojson/mediatype-write` remains OPEN and tied to future create-replace-delete scope
- [x] OGC GeoJSON requirement class source is cited in @Test descriptions or constants
- [x] `mvn clean test` passes via `scripts/mvn-test-via-docker.sh`
- [x] `scripts/smoke-test.sh` from a /tmp clone reports failed=0 and PASS+SKIP total at least 51
- [x] REQ-ETS-PART1-012 status updated SPECIFIED → PARTIAL-IMPLEMENTED only after live smoke
- [x] traceability.md REQ-ETS-PART1-012 row updated after implementation, with remaining GeoJSON subrequirements still open

## Implementation Notes

Implemented in sister repo commits `28f4ddf` and `b4a97de` on 2026-05-05.

- Added `GeoJsonTests.java` with 5 @Tests for `/conf/geojson`, `/req/geojson/mediatype-read`, `/req/geojson/system-schema`, `/req/geojson/system-mappings`, and dependency tracing.
- Wired `geojson` into `testng.xml` with `depends-on="systemfeatures"` and added 3 structural lint tests in `VerifyTestNGSuiteDependency`.
- Verification: `bash scripts/mvn-test-via-docker.sh` reports BUILD SUCCESS, surefire `Tests run: 92, Failures: 0, Errors: 0, Skipped: 3`.
- E2E verification: `/tmp/sprint-ets-09-smoke-fix` running `SMOKE_OUTPUT_DIR=/tmp/sprint-ets-09-smoke-fix-results bash scripts/smoke-test.sh` reports `total=51 passed=42 failed=0 skipped=9`.
- GeoRobotix declares `/conf/geojson`, but `/systems` with `Accept: application/geo+json` returns `Content-Type: application/json` and top-level `items`. The mediatype-read, FeatureCollection, and feature-mapping assertions therefore SKIP; `items` is not counted as GeoJSON PASS.
