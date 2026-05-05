# Story S-ETS-10-01: SensorML Systems Read-Only Subset

> Sprint: ets-10  
> Status: Planned  
> Priority: P0  
> Complexity: M  
> Epic: epic-ets-02-part1-classes  
> OpenSpec: REQ-ETS-PART1-013

## User Value

As an OGC API Connected Systems server implementer, I need the ETS to exercise the declared SensorML encoding class without performing write operations, so that read-only SensorML representation support is tested before the suite expands into mutation-side or full-schema coverage.

## Scope

Implement a SensorML systems read-only subset for `/conf/sensorml`.

This story intentionally does not close the full SensorML requirement class. `mediatype-write`, `relation-types`, deployment/procedure/property SensorML schema and mapping assertions, full SensorML 3.0 JSON Schema validation, and mutation-side behavior remain open.

## Requirements

- REQ-ETS-PART1-013
- SCENARIO-ETS-PART1-013-SENSORML-CONFORMANCE-DECLARED-001
- SCENARIO-ETS-PART1-013-SENSORML-REPRESENTATION-DISCOVERY-001
- SCENARIO-ETS-PART1-013-SENSORML-MEDIATYPE-READ-001
- SCENARIO-ETS-PART1-013-SENSORML-SYSTEM-SHAPE-001
- SCENARIO-ETS-PART1-013-SENSORML-SYSTEM-MAPPING-001
- SCENARIO-ETS-PART1-013-SENSORML-DEPENDENCY-SMOKE-001

## Planned Test Surface

1. `sensorMlConformanceDeclared` — `/conformance` declares `/conf/sensorml`, otherwise SensorML tests SKIP with reason.
2. `sensorMlRepresentationDiscoveredForSystem` — select a System resource and discover SensorML JSON either through direct `Accept: application/sml+json` behavior or an item-level `alternate` link with `type="application/sml+json"`.
3. `sensorMlMediaTypeRead` — fetch the selected SensorML representation and assert HTTP 200 + parseable JSON while recording direct negotiation vs alternate-link fallback. Alternate-link fallback must not be reported as full `mediatype-read` closure unless the fetched body proves SensorML JSON support.
4. `sensorMlSystemHasMinimalShape` — assert minimal SensorML system shape, including a string `type` and a plausible identifier/member structure.
5. `sensorMlSystemPreservesIdentityMapping` — compare selected CS API System `id` or `properties.uid` with SensorML `id`, `uniqueId`, `uid`, or documented SensorML identifier member; SKIP with reason if no machine-checkable mapping exists.
6. `sensorMlDependencyCascadeRuntime` — runtime tracer for SensorML -> SystemFeatures -> Core.

## Dependency Wiring

SensorML depends on SystemFeatures:

```xml
<group name="sensorml" depends-on="systemfeatures"/>
```

`SensorMlTests` should be co-located in the same TestNG `<test>` block as SystemFeatures and GeoJSON.

## Planning Evidence

- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28, checked 2026-05-05, not stale.
- OGC upstream source: `opengeospatial/ogcapi-connected-systems` commit `3fd86c73e744b7e2faaf7f1c17366bfb9ff4cd6f`.
- SensorML class source: `api/part1/standard/requirements/encoding/sensorml/requirements_class_sensorml.adoc`.
- Requirement class identifier: `/req/sensorml`.
- Listed subrequirements: `mediatype-read`, `mediatype-write`, `relation-types`, `resource-id`, `feature-attribute-mapping`, `system-schema`, `system-sml-class`, `system-mappings`, `deployment-schema`, `deployment-mappings`, `procedure-schema`, `procedure-sml-class`, `procedure-mappings`, `property-schema`, and `property-mappings`.
- GeoRobotix planning probe: `/conformance` declares `/conf/sensorml`.
- GeoRobotix planning probe: collection-level `GET /systems` with `Accept: application/sml+json` returns `Content-Type: application/json` and top-level `items`; that alone is not SensorML PASS.
- GeoRobotix planning probe: single-system JSON exposes `alternate` links of type `application/sml+json` to `?f=sml3`.

## Definition of Done

- [ ] `SensorMlTests.java` added with the six planned read-only @Tests.
- [ ] Every SensorML @Test has `groups = "sensorml"`.
- [ ] Every SensorML @Test `description` includes the OGC requirement URI and `SCENARIO-ETS-PART1-013-*` reference.
- [ ] `testng.xml` declares `sensorml` depends on `systemfeatures`.
- [ ] `VerifyTestNGSuiteDependency` adds three SensorML lint tests: group dependency, method group tagging, and class co-location.
- [ ] No POST/PUT/PATCH/DELETE calls are introduced in SensorML tests.
- [ ] `bash scripts/mvn-test-via-docker.sh` passes.
- [ ] `scripts/smoke-test.sh` passes from a /tmp clone with `SMOKE_OUTPUT_DIR` outside the worktree.
- [ ] Smoke total is at least 57 with failed=0.
- [ ] OpenSpec, story status, traceability, ops status, changelog, and test-results are reconciled after implementation.
- [ ] Quinn + Raze gates are run independently after implementation.

## Out Of Scope

- SensorML `mediatype-write`.
- SensorML `relation-types`.
- Deployment, Procedure, and Property SensorML schema/mapping assertions.
- Full SensorML 3.0 JSON Schema validation.
- AdvancedFiltering query/filtering behavior, create-replace-delete, Update, and Part 2.
