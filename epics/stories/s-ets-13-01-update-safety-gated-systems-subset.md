# Story S-ETS-13-01: Update/PATCH Safety-Gated Systems Subset

> Sprint: ets-13
> Status: PARTIAL-IMPLEMENTED
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

The default GeoRobotix smoke target does not currently declare `/conf/update`, and `OPTIONS /systems/{id}` advertises GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS but not PATCH. That means default Update runtime should SKIP-with-reason, not PASS. In default TestNG smoke, the Update configuration method records the missing `/conf/update` reason, and the five Update @Tests skip through the `update -> createreplacedelete` dependency because Create/Replace/Delete's mutation safety gate intentionally skips public-IUT lifecycle mutation.

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

- [x] `UpdateTests.java` added with the planned @Tests.
- [x] Every Update @Test has `groups = "update"`.
- [x] Every Update @Test `description` includes the OGC requirement URI and `SCENARIO-ETS-PART1-011-*` reference.
- [x] `testng.xml` declares `update` depends on `createreplacedelete`.
- [x] `VerifyTestNGSuiteDependency` adds three Update lint tests: group dependency, method group tagging, and class co-location/order.
- [x] Default smoke emits no IUT-bound PATCH requests from the Update suite.
- [x] Existing no-mutation oracle treats PATCH as a mutation method alongside POST, PUT, and DELETE.
- [x] PATCH lifecycle assertion SKIPs before PATCH when opt-in parameters are absent.
- [x] PATCH lifecycle assertion SKIPs when `/conf/update` is absent or PATCH is not advertised.
- [x] Even when mutation opt-in parameters are present, the implementation hard-denies mutation against public GeoRobotix URLs before PATCH.
- [x] `bash scripts/mvn-test-via-docker.sh` completes with exact totals recorded.
- [x] `scripts/smoke-test.sh` runs with exact totals recorded.
- [x] OpenSpec, story status, traceability, ops status, changelog, and test-results are reconciled after implementation.
- [x] Raze implementation review is run before reporting completion.

## Implementation Notes

Status: PARTIAL-IMPLEMENTED by Sprint 13 Generator on 2026-05-06.

- Added `UpdateTests.java` with five `update` @Tests for conformance declaration gating, mutation safety, `OPTIONS /systems/{id}` PATCH readiness, guarded systems PATCH lifecycle, and dependency tracing.
- Wired `update` after Create/Replace/Delete in `testng.xml` with `<group name="update" depends-on="createreplacedelete"/>`; added three structural lint tests in `VerifyTestNGSuiteDependency`.
- Reused Sprint 12 mutation opt-in attributes and made CreateReplaceDelete helper methods public for shared temporary System creation and created-resource URI resolution.
- Extended the no-mutation oracle and smoke script messaging so default GeoRobotix smoke rejects IUT-bound PATCH alongside POST, PUT, and DELETE.
- Verification: formatter BUILD SUCCESS; `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS with `113 tests / 0 failures / 0 errors / 3 skipped`; archived Maven log at `ops/test-results/sprint-ets-13-maven-2026-05-06.log`.
- E2E verification: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results bash scripts/smoke-test.sh` against GeoRobotix reported `74 total / 52 passed / 0 failed / 22 skipped` and zero IUT-bound POST/PUT/DELETE/PATCH entries across 41 recognized IUT-bound request-log entries.
- Default Update runtime is skip-first because GeoRobotix does not declare `/conf/update` and because Update depends on the default-skipped Create/Replace/Delete mutation safety gate; no PATCH was issued. The smoke XML shows `fetchUpdateInputs` SKIP with the missing `/conf/update` reason, while the five Update @Tests show dependency SKIPs through `createreplacedelete`.
- Raze implementation review: `.harness/evaluations/sprint-ets-13-adversarial-implementation.yaml` reported `GAPS_FOUND` 0.86 for documentation/evidence gaps only; code safety findings were acceptable. The required documentation/evidence fixes were applied in this follow-up.

## Out Of Scope

- Unconditional PATCH against GeoRobotix or any public shared IUT.
- Deployment, procedure, sampling-feature, and property PATCH.
- Feature Collection update paths under `/collections/{collectionId}/items/{id}`.
- Part 2 `/conf/update`.
- Optimistic locking requirements.
- PATCH media-type matrix, including JSON Patch, merge patch, and content negotiation.
