# Story: S-ETS-08-02

**Epic**: epic-ets-02-part1-classes
**Priority**: P0
**Estimated Complexity**: M

## Description

Implement the Subdeployments `/conf/subdeployment` conformance class as a new TestNG suite
class in the ETS. Subdeployments completes the 3-deep cascade chain:
  Subdeployments → Deployments → SystemFeatures → Core
This is the strongest dependency-cascade extension remaining in Sprint 8. It is the natural
successor to Sprint 4's 2-deep Subsystems→SystemFeatures→Core precedent, extended to 3 levels.

The class follows the established mechanical pattern:
- New `SubdeploymentsTests.java` (4 @Tests, Sprint-1-style minimal)
- Depends on Deployments via testng.xml group dependency wiring
- VerifyTestNGSuiteDependency extended with 3 new lint tests for the subdeployments group
- SKIP-with-reason via @BeforeClass SkipException if parent Deployments group fails

## Acceptance Criteria

- SCENARIO-ETS-PART1-005-SUBDEP-RESOURCES-001 (CRITICAL)
- SCENARIO-ETS-PART1-005-SUBDEP-CANONICAL-001 (CRITICAL)
- SCENARIO-ETS-PART1-005-SUBDEP-CANONICAL-URL-001 (CRITICAL)
- SCENARIO-ETS-PART1-005-SUBDEP-DEPENDENCY-SKIP-001 (CRITICAL)
- SCENARIO-ETS-PART1-005-SUBDEP-SMOKE-NO-REGRESSION-001 (CRITICAL)

## Spec References

- REQ-ETS-PART1-005 (Subdeployments `/conf/subdeployment` — PLACEHOLDER → SPECIFIED → IMPLEMENTED)

## Technical Notes

### OGC Requirement Structure

Sprint 8 implementation corrected the planning narrative: the IUT and OGC source identify the conformance class as `/conf/subdeployment` (singular), while the OGC requirements class filename uses a plural label. The implemented ETS honors the singular identifier.
Generator MUST verify the canonical requirement URI form via OGC `.adoc` source HTTP-200 fetch
before writing any @Test description attributes (continuing the URI-canonicalization discipline
from S-ETS-02-03 and all subsequent classes).

Expected OGC requirement directory:
`raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/subdeployment/`

Verified OGC requirement files:
- `requirements_class_subdeployments.adoc` → class identifier `/req/subdeployment`, inheriting `/req/deployment`
- `req_subcollection.adoc` → `/deployments/{parentId}/subdeployments`
- `req_recursive_param.adoc`
- `req_recursive_search_deployments.adoc`
- `req_recursive_search_subdeployments.adoc`

No `/req/subdeployment/parent-deployment-link`, `/req/subdeployment/canonical-endpoint`, or `/req/subdeployment/canonical-url` file exists. Canonical endpoint exposure and canonical URL assertions are inherited from `/req/deployment` and operate on `/deployments/{id}`. Any `id`, `type`, and `links` checks are ETS structural sanity checks on the returned resource representation, not fields spelled out directly by `req_canonical_endpoint.adoc`.

Note: OGC directory naming may be `subdeployment/` (singular, no `-s`). Generator must verify.

### GeoRobotix IUT Verification

Generator MUST curl-verify IUT endpoints at sprint time before writing assertions.
GeoRobotix at `https://api.georobotix.io/ogc/t18/api` has 1 deployment (`16sp744ch58g`).

Expected verification steps:
1. `curl -s https://api.georobotix.io/ogc/t18/api/deployments/16sp744ch58g/subdeployments` — check HTTP status + body
2. `curl -s https://api.georobotix.io/ogc/t18/api/conformance` — verify `/conf/subdeployment` declared

If GeoRobotix returns HTTP 404 for `/deployments/{id}/subdeployments` OR does not declare
`/conf/subdeployment` in conformance, ALL subdeployments @Tests MUST SKIP-with-reason
(same pattern as PropertyDefinitions' empty-items handling in Sprint 7). IUT-state-honest SKIP
is the correct behavior; this is NOT a test failure.

### Conformance Class Structure

Class file: `src/main/java/.../conformance/subdeployments/SubdeploymentsTests.java`
Package: `org.opengis.cite.ogcapiconnectedsystems10.conformance.subdeployments`

TestNG wiring in testng.xml:
```xml
<group name="subdeployments" depends-on="deployments"/>
```
(Note: depends-on="deployments", NOT "systemfeatures" — this creates the 3-deep chain:
subdeployments→deployments→systemfeatures→core)

@Test annotations use the project-standard suite-level dependency pattern:
```java
@Test(groups = {"subdeployments"})
```
The direct group dependency is declared once in `testng.xml`, and TestNG emits `depends-on-groups="deployments"` at runtime.

### Expected @Tests (4, Sprint-1-style minimal)

Following the Subsystems/Procedures/Deployments/SamplingFeatures pattern exactly:

1. `subdeploymentCollectionReturns200` — GET /deployments/{id}/subdeployments → HTTP 200 + non-empty items OR
   SKIP-with-reason if IUT returns 404 or /conf/subdeployment not declared
   Maps to: `/req/subdeployment/collection`

2. `subdeploymentItemHasIdType` — GET /deployments/{id} → inherited Deployment canonical endpoint exposure plus structural sanity checks on id/type/links
   Maps to: inherited `/req/deployment/canonical-endpoint`
   SKIP-with-reason if collection empty or IUT doesn't serve subdeployments

3. `subdeploymentCanonicalUrlReturns200` — /deployments/{id} links contain rel=canonical or the inherited Deployment canonical URL is directly dereferenceable
   Maps to: inherited `/req/deployment/canonical-url`

4. `subdeploymentsDependencyCascadeRuntime` — runtime tracer (analogous to Sprint 7 SF + Property
   dependency-cascade-runtime tests); verifies the 3-deep chain is wired at TestNG runtime level.
   No HTTP call; pure TestNG group dependency assertion.
   (Alternative: combine cascade-runtime with a parent-deployment-link check if OGC requires it,
   analogous to Subsystems' `subsystemHasParentSystemLink`)

**Parent-deployment-link (@Test candidate) — resolved during implementation**:
The expected parent-deployment-link requirement does not exist in the OGC source. No 5th @Test was added.

### VerifyTestNGSuiteDependency Extension

Following Sprint 7 precedent (SamplingFeatures + PropertyDefinitions lint tests), extend
`VerifyTestNGSuiteDependency.java` with 3 new lint tests:
1. `testSubdeploymentsGroupDependsOnDeployments` — verifies testng.xml `<group name="subdeployments" depends-on="deployments"/>`
2. `testEverySubdeploymentsTestMethodCarriesSubdeploymentsGroup` — grep SubdeploymentsTests.java for `@Test` without `groups="subdeployments"`
3. `testSubdeploymentsCoLocatedWithDeployments` — structural co-location check

### Cascade Chain Proof

This story completes the 3-deep cascade chain:
  Subdeployments → Deployments → SystemFeatures → Core

The cascade should produce this pattern when SystemFeatures is sabotaged:
  Core: PASS
  Common: PASS
  SystemFeatures: 1 FAIL + N SKIP
  Subsystems: SKIP (depends-on systemfeatures)
  Procedures: SKIP (depends-on systemfeatures)
  Deployments: SKIP (depends-on systemfeatures)
  SamplingFeatures: SKIP (depends-on systemfeatures)
  PropertyDefinitions: SKIP (depends-on systemfeatures)
  Subdeployments: SKIP (depends-on deployments which depends-on systemfeatures)

AND when Deployments is sabotaged (NEW proof point for Subdeployments):
  Core: PASS
  Common: PASS
  SystemFeatures: PASS (6 @Tests)
  Subsystems: PASS (depends-on systemfeatures — unaffected)
  Procedures: PASS (depends-on systemfeatures — unaffected)
  Deployments: 1 FAIL + N SKIP
  SamplingFeatures: PASS (depends-on systemfeatures — unaffected)
  PropertyDefinitions: PASS (depends-on systemfeatures — unaffected)
  Subdeployments: SKIP (directly depends-on deployments)

Quinn: verify the SystemFeatures-sabotage cascade from the mvn-test-via-docker.sh output (6-class SKIP count expected).
Raze: adversarial code inspection of SubdeploymentsTests.java (groups wiring, @BeforeClass SkipException, OGC URI form).

### Smoke Baseline Update

Sprint 7 smoke baseline: 42/42 PASS (40 PASS + 2 SKIP-with-reason for empty PropertyDefinitions items).
Sprint 8 target after this story (if GeoRobotix serves subdeployments): ≥46 (@Tests + up to 4 SKIP-with-reason
if GeoRobotix doesn't declare /conf/subdeployment in conformance).
If IUT does NOT serve subdeployments → all 4 new @Tests SKIP-with-reason → smoke ≥42 PASS + 4 additional SKIPs.
Both outcomes are acceptable; the key is failed=0.

### Relationship to Existing Code

Sister repo already has:
- `conformance/deployments/DeploymentsTests.java` (Sprint 5, the parent class)
- `conformance/subsystems/SubsystemsTests.java` (Sprint 4, the sibling pattern)

SubdeploymentsTests.java should structurally mirror SubsystemsTests.java, substituting:
- "subsystem" → "subdeployment"
- `depends-on="systemfeatures"` → `depends-on="deployments"`
- `/systems/{id}/subsystems` → `/deployments/{id}/subdeployments`
- `/subsystems/{id}` → inherited Deployment canonical endpoint `/deployments/{id}` (no standalone `/subdeployments/{id}` assertion in Sprint 8)
- `rel="system"` parent link → not applicable; OGC source has no parent-deployment-link requirement

## Dependencies

- S-ETS-08-01 must land first (wedge bundle including mvn-test-via-docker.sh for Quinn verification)

## Definition of Done

- [x] `SubdeploymentsTests.java` exists at `src/main/java/.../conformance/subdeployments/`
- [x] ≥4 @Test methods carry `groups={"subdeployments"}`; `testng.xml` supplies the suite-level `depends-on="deployments"` group dependency
- [x] @BeforeClass SkipException fallback present (honoring IUT-state-honest SKIP policy)
- [x] OGC `/req/subdeployment/` canonical URIs HTTP-200-verified at implementation time; cited in @Test description attributes
- [x] testng.xml extended: `<group name="subdeployments" depends-on="deployments"/>` + SubdeploymentsTests class in single-block consolidation
- [x] VerifyTestNGSuiteDependency extended with 3 new lint tests for subdeployments group
- [x] mvn clean test passes (89 surefire: 0 failures, 0 errors, 3 skipped) via `scripts/mvn-test-via-docker.sh`
- [x] smoke-test.sh from /tmp clone: failed=0; PASS+SKIP sum 46; new subdeployments @Tests appear as SKIP-with-reason
- [x] REQ-ETS-PART1-005 status updated PLACEHOLDER → IMPLEMENTED in spec.md
- [x] traceability.md REQ-ETS-PART1-005 row updated
- [x] No regression in existing 8 conformance classes (core, common, systemfeatures, subsystems, procedures, deployments, samplingfeatures, propertydefinitions)

## Implementation Notes

Sprint 8 Generator implemented this story in the sister repo at commit `b349edf` after wedge commit `fcff76b`.
GeoRobotix declared `/conf/subdeployment` and returned HTTP 200 with empty `items: []` at `/deployments/16sp744ch58g/subdeployments`, so all 4 new Subdeployments @Tests SKIP-with-reason under the IUT-state-honest policy. Quinn reran smoke on 2026-05-04 and observed `total=46 passed=40 failed=0 skipped=6`; Raze reran smoke after Docker contention cleared and observed the same. Quinn and Raze both verified the 3-deep chain via TestNG output: Subdeployments depends on Deployments, Deployments depends on SystemFeatures, and SystemFeatures depends on Core.
