# Story S-ETS-11-01: AdvancedFiltering Read-Only Subset

> Sprint: ets-11
> Status: Planned
> Priority: P0
> Complexity: M
> Epic: epic-ets-02-part1-classes
> OpenSpec: REQ-ETS-PART1-009

## User Value

As an OGC API Connected Systems server implementer, I need the ETS to exercise declared AdvancedFiltering support without mutating server state, so query/filter conformance can advance before create-replace-delete and update classes are attempted.

## Scope

Implement a declaration-gated AdvancedFiltering systems/common-resource read-only subset for `/conf/advanced-filtering`.

This story intentionally does not close the full AdvancedFiltering requirement class. It does not validate every resource type, every association filter, full geometry intersection semantics, combined-filter truth tables, or Part 2 query behavior.

## Requirements

- REQ-ETS-PART1-009
- SCENARIO-ETS-PART1-009-ADVFILTER-CONFORMANCE-DECLARED-001
- SCENARIO-ETS-PART1-009-ADVFILTER-ID-LIST-SCHEMA-001
- SCENARIO-ETS-PART1-009-ADVFILTER-SYSTEM-ID-001
- SCENARIO-ETS-PART1-009-ADVFILTER-SYSTEM-KEYWORD-001
- SCENARIO-ETS-PART1-009-ADVFILTER-SYSTEM-GEOM-SMOKE-001
- SCENARIO-ETS-PART1-009-ADVFILTER-DEPENDENCY-SMOKE-001
- SCENARIO-ETS-PART1-009-ADVFILTER-SMOKE-NO-REGRESSION-001

## Planned Test Surface

1. `advancedFilteringConformanceDeclared` - `/conformance` declares `/conf/advanced-filtering`, otherwise AdvancedFiltering tests SKIP with reason.
2. `advancedFilteringIdListSchema` - local helper validates the explicit ID_List examples below: homogeneous local-ID lists and UID lists are valid, UID-prefix `*` is valid for resource-by-id query planning, mixed local/UID lists and empty/malformed values are invalid.
3. `systemsFilterById` - selects a known System id from a non-empty `/systems` seed response and verifies `/systems?id=<id>` returns HTTP 200 JSON with at least one item and every item preserves the selected id. If no seed id can be selected, SKIP with reason; an empty filtered response after selecting a seed id is FAIL.
4. `systemsFilterByKeyword` - selects a keyword from a System `name` or `description` and verifies `/systems?q=<keyword>` returns HTTP 200 JSON with at least one item and every item has keyword evidence in `name` or `description`. If no seed keyword can be selected, SKIP with reason; an empty filtered response after selecting a seed keyword is FAIL.
5. `systemsFilterByGeomSmoke` - calls `/systems?geom=<broad WKT polygon>` and validates HTTP 200 JSON collection shape only; this is not full spatial-intersection conformance.
6. `advancedFilteringDependencyCascadeRuntime` - structural lint and/or runtime tracer for AdvancedFiltering -> SystemFeatures -> Core. Default smoke total is recorded separately as no-regression evidence.

## ID_List Helper Examples

Sprint 11 uses the upstream clause 15 `ID_List` wording and `api/part1/openapi/parameters/idListSchema.yaml`, which define a non-empty list of local IDs or UIDs and prohibit mixing ID types in one request.

| Value | Expected | Reason |
|-------|----------|--------|
| `0mqcvdnfoca0` | valid | one local resource ID |
| `0mqcvdnfoca0,0ngu9lvstls0` | valid | homogeneous local resource IDs |
| `urn:osh:sensor:simweather:0123456879` | valid | one UID URI |
| `urn:osh:sensor:simweather:0123456879,urn:osh:sensor:simweather:9876543210` | valid | homogeneous UID URIs |
| `urn:osh:sensor:simweather:*` | valid | resource-by-id UID-prefix query value |
| `` | invalid | empty list |
| `,` | invalid | no non-empty items |
| `0mqcvdnfoca0,urn:osh:sensor:simweather:0123456879` | invalid | mixed local ID and UID |
| `urn:osh:sensor:bad value` | invalid | malformed URI value |

This is a local schema-helper test only. It does not prove full query semantics at every endpoint.

## Dependency Wiring

AdvancedFiltering depends on SystemFeatures:

```xml
<group name="advancedfiltering" depends-on="systemfeatures"/>
```

`AdvancedFilteringTests` should be co-located in the same TestNG `<test>` block as SystemFeatures.

## Planning Evidence

- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28, checked 2026-05-05, not stale.
- OGC upstream source: `opengeospatial/ogcapi-connected-systems` commit `3fd86c73e744b7e2faaf7f1c17366bfb9ff4cd6f`.
- Requirement class source: `api/part1/standard/requirements/query/requirements_class_advanced_filtering.adoc`.
- Requirement class identifier: `/req/advanced-filtering`.
- Listed subrequirements include ID list schema, resource-by-id, resource-by-keyword, feature-by-geom, system/deployment/procedure/sampling-feature/property association filters, and combined filters.
- GeoRobotix planning probe: `/conformance` does not currently declare `/conf/advanced-filtering`.
- GeoRobotix planning probes: `/systems?uid=...`, `/systems?q=Weather`, and `/systems?select=id` return HTTP 200 JSON, but undeclared query behavior is not conformance PASS evidence.
- GeoRobotix planning probe: `/systems?bbox=-1,-1,1,1&limit=1` returns HTTP 200 with empty items; read-only query behavior is available for smoke-style checks.

## Definition of Done

- [ ] `AdvancedFilteringTests.java` added with the six planned read-only @Tests.
- [ ] Every AdvancedFiltering @Test has `groups = "advancedfiltering"`.
- [ ] Every AdvancedFiltering @Test `description` includes the OGC requirement URI and `SCENARIO-ETS-PART1-009-*` reference.
- [ ] `testng.xml` declares `advancedfiltering` depends on `systemfeatures`.
- [ ] `VerifyTestNGSuiteDependency` adds three AdvancedFiltering lint tests: group dependency, method group tagging, and class co-location.
- [ ] No POST/PUT/PATCH/DELETE calls are introduced.
- [ ] ID and keyword filter tests fail, not pass, on empty filtered results after selecting seed data.
- [ ] Default smoke no-regression totals are documented separately from dependency cascade evidence.
- [ ] `bash scripts/mvn-test-via-docker.sh` passes.
- [ ] `scripts/smoke-test.sh` passes from a /tmp clone with `SMOKE_OUTPUT_DIR` outside the worktree.
- [ ] Smoke total is at least 63 with failed=0.
- [ ] OpenSpec, story status, traceability, ops status, changelog, and test-results are reconciled after implementation.
- [ ] Raze implementation review is run before reporting completion.

## Out Of Scope

- Create/replace/delete and update behavior.
- Part 2 AdvancedFiltering.
- Full cross-resource association filter semantics.
- Full WKT/spatial intersection conformance.
- Combined-filter truth-table validation.
- Endpoint parity across every CS API resource endpoint.
