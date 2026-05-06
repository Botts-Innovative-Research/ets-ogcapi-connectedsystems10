# Story S-ETS-13-01: Update/PATCH Safety-Gated Systems Subset

> Sprint: ets-13
> Status: Planned
> Priority: P0
> Complexity: M
> Epic: epic-ets-02-part1-classes
> OpenSpec: REQ-ETS-PART1-011

## User Value

As an OGC API Connected Systems server implementer, I need the ETS to cover declared Update/PATCH support without issuing PATCH against shared or public IUTs, so mutation-side conformance can advance under the same explicit safety contract established for Create/Replace/Delete.

## Scope

Implement a PARTIAL `/conf/update` systems subset with default-safe behavior.

Default TeamEngine smoke against GeoRobotix MUST NOT issue IUT-bound PATCH. Positive PATCH behavior is allowed only when an operator explicitly enables mutation tests for a dedicated mutable IUT, the IUT declares `/conf/update`, and the selected resource endpoint advertises PATCH support.

This story intentionally does not close the full Update requirement class. It does not validate deployment, procedure, sampling-feature, or property PATCH; Feature Collections `/collections/{collectionId}/items/{id}` update paths; Part 2 update; optimistic locking; or any PATCH media-type matrix, including JSON Patch, merge patch, and content negotiation beyond the minimal systems subset selected for this sprint.

## Requirements

- REQ-ETS-PART1-011
- SCENARIO-ETS-PART1-011-UPDATE-CONFORMANCE-DECLARED-001
- SCENARIO-ETS-PART1-011-UPDATE-MUTATION-SAFETY-GATE-001
- SCENARIO-ETS-PART1-011-UPDATE-SYSTEM-RESOURCE-OPTIONS-001
- SCENARIO-ETS-PART1-011-UPDATE-SYSTEM-PATCH-LIFECYCLE-OPTIN-001
- SCENARIO-ETS-PART1-011-UPDATE-DEPENDENCY-SMOKE-001
- SCENARIO-ETS-PART1-011-UPDATE-SMOKE-NO-PATCH-001

## Planned Test Surface

1. `updateConformanceDeclared` - `/conformance` declares `/conf/update`, otherwise Update tests SKIP with reason.
2. `updateMutationSafetyGate` - default run records that mutation tests are disabled unless the existing Sprint 12 mutation opt-in parameters are present.
3. `systemResourceOptionsPatchReadinessPrecondition` - selects a seed System id, sends `OPTIONS /systems/{id}`, and records whether `Allow` includes PATCH without issuing PATCH. This is ETS readiness evidence only, not an OGC update conformance PASS.
4. `systemsPatchLifecycleOptIn` - SKIPs before PATCH by default. When explicitly enabled for a dedicated mutable IUT that declares `/conf/update` and advertises PATCH, uses a temporary System resource, issues a minimal PATCH, verifies the update by GET, and performs best-effort cleanup.
5. `updateDependencyCascadeRuntime` - structural/runtime tracer for Update -> CreateReplaceDelete -> SystemFeatures -> Core.

## Mutation Safety Contract

Sprint 13 reuses the Sprint 12 mutation controls:

| Parameter | Required Value | Purpose |
|-----------|----------------|---------|
| `mutation-tests-enabled` | `true` | Enables mutation-side tests. Missing or any other value forces SKIP before PATCH. |
| `mutation-iut-policy` | `dedicated-mutable-iut` | Confirms the operator is not targeting a shared public demo endpoint. |

Even when both opt-in parameters are present, the implementation MUST hard-deny mutation against known shared public GeoRobotix URLs, including `https://api.georobotix.io/ogc/t18/api`, before any PATCH is issued.

The default GeoRobotix smoke target does not currently declare `/conf/update`, and `OPTIONS /systems/{id}` advertises GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS but not PATCH. That means default Update runtime should SKIP-with-reason, not PASS.

## No-Mutation Smoke Oracle

Default smoke evidence must extend the existing Sprint 12 IUT-bound request-log oracle to include PATCH:

1. Parse request-log entries in current `Request: METHOD URI` format or older adjacent `Request method:` and `Request URI:` pair format.
2. Consider only pairs whose URI starts with the IUT base URL.
3. Require at least one recognized IUT-bound request entry, so the oracle cannot pass vacuously.
4. Require zero IUT-bound entries whose method is POST, PUT, DELETE, or PATCH.

The TeamEngine REST control POST that starts a suite run is not part of this oracle.

## Dependency Wiring

Update depends on Create/Replace/Delete:

```xml
<group name="update" depends-on="createreplacedelete"/>
```

`UpdateTests` should be co-located in the same TestNG `<test>` block as Create/Replace/Delete and SystemFeatures, after the Create/Replace/Delete class entry.

## Planning Evidence

- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28, checked 2026-05-06, not stale.
- OGC source: OGC API - Connected Systems Part 1, Requirements Class 11 `/req/update`, Conformance Class A.11 `/conf/update`.
- OGC Part 1 Update prerequisites: `/req/create-replace-delete` and OGC API Features Part 4 `/req/update`.
- OGC Part 1 Update normative statements: `/req/update/system`, `/req/update/deployment`, `/req/update/procedure`, `/req/update/sampling-feature`, `/req/update/property`.
- OGC Part 1 `/req/update/system`: `PATCH` is supported at `{api_root}/systems/{id}` and `id` is the local System identifier.
- OGC Part 1 ATS A.79-A.83 also lists Feature Collection item update paths under `/collections/{collectionId}/items/{id}` for systems, deployments, procedures, sampling features, and properties; these collection item PATCH paths are explicitly deferred from Sprint 13.
- GeoRobotix planning probe on 2026-05-06: `/conformance` does not declare `http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/update`.
- GeoRobotix planning probe on 2026-05-06: `OPTIONS /systems/0mqcvdnfoca0` returns HTTP 200 and `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS`; PATCH is absent.
- Local OSH planning probe on 2026-05-06: unauthenticated `/conformance` returns HTTP 401, and unauthenticated `OPTIONS /systems/040g` returns HTTP 200 with no PATCH in `Allow`.

## Definition of Done

- [ ] `UpdateTests.java` added with the planned @Tests.
- [ ] Every Update @Test has `groups = "update"`.
- [ ] Every Update @Test `description` includes the OGC requirement URI and `SCENARIO-ETS-PART1-011-*` reference.
- [ ] `testng.xml` declares `update` depends on `createreplacedelete`.
- [ ] `VerifyTestNGSuiteDependency` adds three Update lint tests: group dependency, method group tagging, and class co-location/order.
- [ ] Default smoke emits no IUT-bound PATCH requests from the Update suite.
- [ ] Existing no-mutation oracle treats PATCH as a mutation method alongside POST, PUT, and DELETE.
- [ ] PATCH lifecycle assertion SKIPs before PATCH when opt-in parameters are absent.
- [ ] PATCH lifecycle assertion SKIPs when `/conf/update` is absent or PATCH is not advertised.
- [ ] Even when mutation opt-in parameters are present, the implementation hard-denies mutation against public GeoRobotix URLs before PATCH.
- [ ] `bash scripts/mvn-test-via-docker.sh` completes with exact totals recorded.
- [ ] `scripts/smoke-test.sh` runs with exact totals recorded.
- [ ] OpenSpec, story status, traceability, ops status, changelog, and test-results are reconciled after implementation.
- [ ] Raze implementation review is run before reporting completion.

## Out Of Scope

- Unconditional PATCH against GeoRobotix or any public shared IUT.
- Deployment, procedure, sampling-feature, and property PATCH.
- Feature Collection update paths under `/collections/{collectionId}/items/{id}`.
- Part 2 `/conf/update`.
- Optimistic locking requirements.
- PATCH media-type matrix, including JSON Patch, merge patch, and content negotiation.
