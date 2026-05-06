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

## Local Mutable-IUT Follow-Up Evidence

Triggered by the 2026-05-05 user question about using a local OpenSensorHub instance, the ETS was exercised against the existing local OSH 2.0-beta2 stack at `http://localhost:8081/sensorhub/api`.

- OSH probe: TeamEngine reached OSH over Docker network `field-hub_default` using IUT URL `http://field-hub-osh-1:8081/sensorhub/api` with explicit mutation parameters set to `mutation-tests-enabled=true` and `mutation-iut-policy=dedicated-mutable-iut`.
- ETS fix from probe: `Location: /systems/{id}` is now resolved relative to the IUT service base, and `systemsCreateReplaceDeleteLifecycle` preserves the created System `uid` during PUT replacement because OSH rejects UID changes as identity changes.
- Unit regression: `VerifyCreateReplaceDeleteLocationResolution` covers absolute, service-relative, IUT-path-relative, and relative `Location` values plus create/replace UID preservation.
- Verification after fix: Docker Maven BUILD SUCCESS, `110 tests / 0 failures / 0 errors / 3 skipped`.
- Local OSH E2E result: `/tmp/ets-csapi-osh-mutable-smoke-r4` reported `69 total / 32 passed / 3 failed / 34 skipped`; `systemsCreateReplaceDeleteLifecycle` PASS with real POST, PUT, and DELETE requests, and OSH logs show resource `/systems/0410` was added, updated, then deleted.
- Overall local OSH smoke is not a full suite PASS yet because the fixture has empty `/procedures`, `/deployments`, and `/samplingFeatures`; a prior run also showed local OSH `proxyBaseUrl` can emit public `https://osh.gis.tw` alternate links for SensorML.
- Cleanup: the temporary lifecycle resource was deleted by the ETS, the manual seed `/systems/040g` was deleted after the run, and `/systems` returned an empty `items` array.

## Local OSH Full-Health Follow-Up

Triggered by the 2026-05-06 user instruction to populate local OSH and rerun full-suite health:

- Updated local OSH `proxyBaseUrl` in `../sar-ops/field-hub/osh/config/config.json` to `http://field-hub-osh-1:8081`, then restarted the OSH container so alternate links resolve inside TeamEngine's Docker network.
- Seeded synthetic resources through the transactional CS API: `/systems/040g`, `/procedures/040g`, `/deployments/040g`, and `/samplingFeatures/040g`. Exact payloads are versioned in `ops/local-osh-seed-fixtures.json`.
- Corrected the System seed to include `properties.featureType = http://www.w3.org/ns/sosa/System`; direct `GET /systems/040g?f=sml3` then returned HTTP 200 with `Content-Type: application/sml+json`.
- TeamEngine full local OSH health smoke: `/tmp/ets-csapi-osh-full-health-r3`, with `SMOKE_DOCKER_NETWORK=field-hub_default`, mutation opt-in enabled, and IUT `http://field-hub-osh-1:8081/sensorhub/api`.
- Result from archived XML and corrected smoke stdout: `69 total / 50 passed / 0 failed / 19 skipped`. No-mutation oracle was intentionally skipped because this was an explicit dedicated mutable-IUT run.
- CRD lifecycle remained PASS with real POST, PUT, DELETE against temporary `/systems/0410`; the seeded long-lived local fixture resources remain in OSH for subsequent health runs.
- Raze local full-health review: `.harness/evaluations/sprint-ets-12-local-osh-full-health-raze.yaml` verdict `GAPS_FOUND` confidence 0.87; required fixes applied by changing `scripts/smoke-test.sh` to print exact totals instead of `${total}/${total}`, and by adding `ops/local-osh-seed-fixtures.json`.

## Out Of Scope

- Unconditional mutation against GeoRobotix or any public shared IUT.
- Deployment, subdeployment, procedure, sampling-feature, and property lifecycle tests.
- System delete cascade behavior.
- Custom collection propagation.
- `text/uri-list` add-to-collection.
- Update/PATCH.
