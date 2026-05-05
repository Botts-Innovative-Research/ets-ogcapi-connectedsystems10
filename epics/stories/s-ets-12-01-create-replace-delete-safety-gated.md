# Story S-ETS-12-01: Create/Replace/Delete Safety-Gated Systems Subset

> Sprint: ets-12
> Status: Partial-Implemented
> Priority: P0
> Complexity: M
> Epic: epic-ets-02-part1-classes
> OpenSpec: REQ-ETS-PART1-010

## User Value

As an OGC API Connected Systems server implementer, I need the ETS to start covering declared create/replace/delete support without accidentally mutating a shared or public IUT, so write-operation conformance can advance under an explicit safety contract.

## Scope

Implement a PARTIAL `/conf/create-replace-delete` systems subset with default-safe behavior.

Default TeamEngine smoke against GeoRobotix MUST NOT issue IUT-bound POST, PUT, or DELETE. Mutating lifecycle checks are allowed only when an operator explicitly enables mutation tests for a dedicated mutable IUT.

This story intentionally does not close the full Create/Replace/Delete requirement class. It does not validate deployments, subdeployments, procedures, sampling features, properties, system delete cascade, custom collection propagation, `text/uri-list`, or Update/PATCH.

## Requirements

- REQ-ETS-PART1-010
- SCENARIO-ETS-PART1-010-CRD-CONFORMANCE-DECLARED-001
- SCENARIO-ETS-PART1-010-CRD-MUTATION-SAFETY-GATE-001
- SCENARIO-ETS-PART1-010-CRD-SYSTEMS-OPTIONS-001
- SCENARIO-ETS-PART1-010-CRD-SYSTEM-RESOURCE-OPTIONS-001
- SCENARIO-ETS-PART1-010-CRD-SYSTEM-LIFECYCLE-OPTIN-001
- SCENARIO-ETS-PART1-010-CRD-DEPENDENCY-SMOKE-001
- SCENARIO-ETS-PART1-010-CRD-SMOKE-NO-MUTATION-001

## Planned Test Surface

1. `createReplaceDeleteConformanceDeclared` - `/conformance` declares `/conf/create-replace-delete`, otherwise Create/Replace/Delete tests SKIP with reason.
2. `createReplaceDeleteMutationSafetyGate` - default run records that mutation tests are disabled unless explicit opt-in parameters are present.
3. `systemsCollectionOptionsReadinessPrecondition` - sends `OPTIONS /systems` and records whether the `Allow` header includes POST without issuing POST. This is ETS readiness evidence only, not an OGC lifecycle conformance PASS.
4. `systemResourceOptionsReadinessPrecondition` - selects a seed System id, sends `OPTIONS /systems/{id}`, and records whether `Allow` includes PUT and DELETE without issuing PUT/DELETE. This is ETS readiness evidence only, not an OGC lifecycle conformance PASS.
5. `systemsCreateReplaceDeleteLifecycle` - SKIPs before POST by default. When explicitly enabled for a dedicated mutable IUT, POSTs a new System, PUTs a replacement, DELETEs it, verifies expected status/id behavior, and performs best-effort cleanup.
6. `createReplaceDeleteDependencyCascadeRuntime` - structural/runtime tracer for Create/Replace/Delete -> SystemFeatures -> Core.

## Mutation Safety Contract

Generator must introduce explicit suite parameters before any lifecycle test can issue a mutating request:

| Parameter | Required Value | Purpose |
|-----------|----------------|---------|
| `mutation-tests-enabled` | `true` | Enables mutation-side tests. Missing or any other value forces SKIP before POST/PUT/DELETE. |
| `mutation-iut-policy` | `dedicated-mutable-iut` | Confirms the operator is not targeting a shared public demo endpoint. |

The parameter path is in scope for Generator and must be wired end-to-end:

- `TestRunArg` exposes both parameters.
- `SuiteAttribute` stores both values.
- `SuiteFixtureListener.processSuiteParameters` copies suite parameters onto the TestNG suite.
- `TestNGController` accepts the optional parameters without requiring them.
- The CTL form exposes the parameters only as explicit mutation controls.
- `scripts/smoke-test.sh` may forward `SMOKE_MUTATION_TESTS_ENABLED` and `SMOKE_MUTATION_IUT_POLICY`, but the default environment leaves them unset.

Even when both opt-in parameters are present, the implementation MUST hard-deny mutation against known shared public GeoRobotix URLs, including `https://api.georobotix.io/ogc/t18/api`, before any POST/PUT/DELETE is issued.

The default GeoRobotix smoke target declares `/conf/create-replace-delete`, and `OPTIONS /systems` plus `OPTIONS /systems/{id}` advertise POST, PUT, and DELETE. That is readiness evidence only. It is not permission to mutate GeoRobotix during default smoke.

## No-Mutation Smoke Oracle

Default smoke evidence must distinguish TeamEngine control-plane requests from IUT-bound REST Assured requests. The no-mutation check is:

1. Parse request-log entries in current `Request: METHOD URI` format or older adjacent `Request method:` and `Request URI:` pair format.
2. Consider only pairs whose URI starts with the IUT base URL.
3. Require at least one recognized IUT-bound request entry, so the oracle cannot pass vacuously.
4. Require zero IUT-bound entries whose method is POST, PUT, or DELETE.

The TeamEngine REST control POST that starts a suite run is not part of this oracle.

## Dependency Wiring

Create/Replace/Delete depends on SystemFeatures:

```xml
<group name="createreplacedelete" depends-on="systemfeatures"/>
```

`CreateReplaceDeleteTests` should be co-located in the same TestNG `<test>` block as SystemFeatures.

## Planning Evidence

- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28, checked 2026-05-05, not stale.
- OGC upstream source: `opengeospatial/ogcapi-connected-systems` commit `3fd86c73e744b7e2faaf7f1c17366bfb9ff4cd6f`.
- Requirement class source: `api/part1/standard/requirements/crud/requirements_class_crd.adoc`.
- Requirement class identifier: `/req/create-replace-delete`.
- Clause source: `api/part1/standard/sections/clause_16_requirements_class_create_replace_delete.adoc`.
- Listed subrequirements include systems, system-delete-cascade, subsystem, deployment, subdeployment, procedure, sampling-feature, property, create-in-collection, replace-in-collection, delete-in-collection, and add-to-collection.
- GeoRobotix planning probe: `/conformance` declares `/conf/create-replace-delete`.
- GeoRobotix planning probe: `OPTIONS /systems` returns HTTP 200 and `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS`.
- GeoRobotix planning probe: `OPTIONS /systems/0mqcvdnfoca0` returns HTTP 200 and `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS`.

## Definition of Done

- [x] `CreateReplaceDeleteTests.java` added with the six planned @Tests.
- [x] Every Create/Replace/Delete @Test has `groups = "createreplacedelete"`.
- [x] Every Create/Replace/Delete @Test `description` includes the OGC requirement URI and `SCENARIO-ETS-PART1-010-*` reference.
- [x] `testng.xml` declares `createreplacedelete` depends on `systemfeatures`.
- [x] `VerifyTestNGSuiteDependency` adds three Create/Replace/Delete lint tests: group dependency, method group tagging, and class co-location.
- [x] `TestRunArg`, `SuiteAttribute`, and suite parameter propagation support `mutation-tests-enabled` and `mutation-iut-policy`.
- [x] Default smoke emits no IUT-bound POST/PUT/DELETE requests from the Create/Replace/Delete suite, using `Request: METHOD URI` and adjacent `Request method:` + `Request URI:` log parsing.
- [x] Lifecycle mutation assertion SKIPs before POST when opt-in parameters are absent.
- [x] Mutation opt-in is wired end-to-end through `TestRunArg`, `SuiteAttribute`, `SuiteFixtureListener`, `TestNGController`, CTL, and optional smoke env forwarding.
- [x] Even when mutation opt-in parameters are present, the implementation hard-denies mutation against public GeoRobotix URLs before POST/PUT/DELETE.
- [x] `bash scripts/mvn-test-via-docker.sh` completes with exact totals recorded.
- [x] `scripts/smoke-test.sh` runs from a `/tmp` clone with `SMOKE_OUTPUT_DIR` outside the worktree and exact totals recorded.
- [x] OpenSpec, story status, traceability, ops status, changelog, and test-results are reconciled after implementation.
- [x] Raze implementation review is run before reporting completion.

## Generator Evidence

- Maven: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `105 tests / 0 failures / 0 errors / 3 skipped`.
- TeamEngine smoke: `/tmp/sprint-ets-12-generator-smoke-current-r3`, command `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s12-generator-r3 bash scripts/smoke-test.sh`, result `69 total / 52 passed / 0 failed / 17 skipped`.
- CreateReplaceDelete runtime outcome against GeoRobotix: 4 PASS (`conformance`, dependency tracer, collection OPTIONS readiness, resource OPTIONS readiness) and 2 SKIP (`mutation safety gate`, lifecycle opt-in).
- Smoke log oracle: zero IUT-bound POST/PUT/DELETE entries for `https://api.georobotix.io/ogc/t18/api`; integrated smoke oracle recognized 40 IUT-bound request log entries.
- Raze implementation review: `.harness/evaluations/sprint-ets-12-adversarial-implementation.yaml` verdict `GAPS_FOUND` confidence 0.88; same-turn fixes applied for the no-mutation oracle, stale status strings, and Allow header token parsing.
- Raze gap-fix review: `.harness/evaluations/sprint-ets-12-adversarial-gapfix.yaml` verdict `APPROVE_WITH_CONCERNS` confidence 0.91; no required fixes remain.

## Out Of Scope

- Unconditional mutation against GeoRobotix or any public shared IUT.
- Deployment, subdeployment, procedure, sampling-feature, and property lifecycle tests.
- System delete cascade behavior.
- Custom collection propagation.
- `text/uri-list` add-to-collection.
- Update/PATCH.
