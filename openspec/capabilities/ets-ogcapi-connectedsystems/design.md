# Design — OGC API Connected Systems ETS

**Architect**: Architect Agent (Alex)
**Date**: 2026-04-27
**Spec Reference**: [`spec.md`](./spec.md) v1.0
**Status**: Approved (Sprint 1)
**Authoritative ADRs**: ADR-001, ADR-002, ADR-003, ADR-004, ADR-005 (in `_bmad/adrs/`)

> Sprint 57 addition (2026-07-30): CP-017 replaces the historical Update
> declaration/readiness/System subset with exactly five released procedures.
> `UpdateTests` retains immutable run arguments and depends directly on Part 1
> API Common. `UpdateSupport` owns exact declaration and condition gates,
> procedure-owned fixture acquisition, OPTIONS/OpenAPI PATCH negotiation,
> bounded paginated custom discovery, canonical and advertised custom item execution, completed partial-update assertions,
> bounded HTTP 202 polling, and reverse-order identity-safe cleanup. It accepts
> JSON Merge Patch and JSON Patch documents, never guesses a resource
> representation media type as a patch format, and requires changed-field,
> preserved-sentinel, stable-external-identity, and ignored-conflicting-id
> evidence. Custom updates prove canonical and occurrence propagation together.
> Fixture POST/DELETE reuse the Sprint 56 ownership and schema contract without
> making Create/Replace/Delete a direct TestNG dependency. Positive writes use
> owned isolated IUT state; OSH and TeamEngine remain immutable, and no
> executable conformance-suite jar or hosted CI is introduced.
> Raze `GAPS_FOUND 0.99` supersedes candidate `b9143a4`. Fixture POST denial
> or unusable response is an inconclusive Update SKIP, not Update failure, but
> every dispatched POST triggers bounded identity rediscovery because an
> ambiguous response may still have committed. Collection applicability uses
> exact compact/canonical SOSA types. Repeated `Allow` fields are combined.
> Synchronous and queued custom propagation require two consecutive joint
> observations. Cleanup revalidates identity immediately before DELETE and
> proves disappearance after every accepted DELETE status.

> Sprint 41 supersession note (2026-07-21): the TeamEngine 5.5/5.6.x
> deployment guidance in the Sprint 1 and Sprint 2 sections below is historical
> baseline context only. The forward runtime contract is ADR-011 plus
> REQ-ETS-TEAMENGINE-007/008: Dockerfile, Compose, and `scripts/smoke-test.sh`
> target a digest-pinned OGC TeamEngine 6.0.0 image, with partial Part 1 and
> Part 2 Connected Systems coverage, canonical run arguments, and local OSH as
> the primary development E2E target.

> Sprint 56 addition (reconciled 2026-07-30): CP-016 replaces the historical
> Create/Replace/Delete subset with twelve direct released procedures.
> `CreateReplaceDeleteSupport` centralizes inherited Features Part 4
> transaction semantics and cleanup, while each TestNG method owns its
> declarations, applicability, resources, and mutation gate. Positive writes
> run only on the owned isolated local OSH workflow; the primary IUT remains
> read-only and external OSH/TeamEngine artifacts remain immutable.
> The support requires the exact released Annex A `ogcapi-4` inheritance URI,
> validates every generated GeoJSON/SensorML write fixture against the bundled
> released schema, and derives root canonical endpoints after nested creates
> rather than treating a nested `Location` as canonical evidence. TestNG group
> dependency remains causal: the procedures do not use `alwaysRun`. Cleanup
> treats a returned Location as destructive only after a GET proves the
> submitted identity; missing or incorrect metadata falls back to root
> discovery by that identity, and deleting a verified alias is followed by
> canonical root identity discovery. HTTP 202 POST, PUT, DELETE, and URI-list
> association responses start bounded polling; they remain
> accepted-but-inconclusive until the submitted content, replacement content,
> deletion, or collection occurrence is observed. Timeout and polling interval
> are deployment-configurable JVM properties. Each queued response creates one
> monotonic deadline shared by all of that operation's required postconditions;
> expiry is checked before probes, HTTP connect/read timeouts and sleeps are
> capped to remaining time, and interruption fails visibly. Compound cascade
> and custom-collection propagation uses the same deadline. URI-list occurrence
> cleanup is registered before POST to catch late materialization, and cleanup
> failure overrides an inconclusive SKIP. Generic resource deletion omits
> `cascade`, while
> explicit System graph checks own `cascade=false|true`. Custom-collection
> checks prove both custom and canonical representations, and URI-list
> association requires OPTIONS, HTTP 201 plus Location or queued HTTP 202, and
> observed dereference evidence.
> Exact candidate `1a6c5ec30f76e120a0e2cd676f472699141213ca` passes focused
> `42/0/0/0`, full Docker Maven `644/0/0/3`, exact-image/runtime, dependency,
> credential, immutability, and hygiene gates, but Raze `GAPS_FOUND 0.98`
> supersedes it. The remediation injects the monotonic clock for deterministic
> deadline tests, refuses first-page, pagination, and candidate requests
> without a whole-millisecond budget, pre-registers and awaits queued custom
> setup occurrences before replace/delete, and treats only direct target
> collection item Locations as queued URI-list occurrences. Status Locations
> are never dereferenced or deleted. Exact candidate
> `8aa92d4da33aeb3b1c545378c0a68cb84a565ccb` passes direct HTTP `31/0/0/0`,
> focused aggregate `48/0/0/0`, full Docker Maven `650/0/0/3`, and all
> released-source, parity, exact-image/runtime, local-OSH, sabotage,
> credential, immutability, and hygiene gates. Raze `GAPS_FOUND 0.97`
> supersedes it: cleanup could delete a mismatched direct occurrence Location,
> and sequential compound polling could accept transient states. The
> replacement stores expected submitted content on each occurrence cleanup
> target and authorizes DELETE only after bounded matching GET. One polling
> iteration evaluates every required postcondition for a compound queued
> mutation, and positive evidence requires two consecutive observations with
> all conditions true together. Behavioral red was `35/2/0/0`; corrected
> direct HTTP is `35/0/0/0`, focused aggregate is `52/0/0/0`, and full
> clean-cache Docker Maven is `654/0/0/3` precommit. A clean replacement exact
> candidate and fresh Raze recheck are pending. Raze `GAPS_FOUND 0.98` on
> exact candidate `700c697`: its technical gates and prior remediations pass,
> but HTTP 202 handling must parse and classify an absolute cross-origin status
> Location without issuing a request or failing the computed-occurrence path.
> The requirement-linked red is `1/1/0/0`. Parsing the queued Location before
> same-origin occurrence classification closes the defect; only a same-origin
> direct child can become an occurrence. Precommit verification is direct HTTP
> `36/0/0/0`, focused `53/0/0/0`, and full Maven `655/0/0/3`. Exact candidate
> `a2ce5478e25542a766025a2a5fde246fc2d5f8d6` repeats those gates and passes
> all exact source, parity, image/runtime, local-OSH, sabotage, credential,
> immutable-base, and hygiene checks. Fresh Raze returns
> `APPROVE_WITH_CONCERNS 0.99`, closes every prior finding, and requires no
> candidate fix. Its concern is solely the external positive-mutation blocker.
> Positive real-IUT mutation evidence remains blocked by unmodified local OSH
> prerequisites.

## Overview

This design translates capability spec REQ-ETS-* into a concrete Java/TestNG component layout for the new repo `ets-ogcapi-connectedsystems10` (per ADR-003 — note: PRD §FR-ETS-01 and capability spec §REQ-ETS-SCAFFOLD-001 reference the older `ets-ogcapi-connectedsystems-1` artifactId; the ADR-003 form is authoritative and Sam will reconcile the spec strings at the next planning cycle).

The Sprint 1 deliverable is the smallest end-to-end vertical slice that proves the architecture works:

1. Archetype-generated Maven project, JDK 17 modernized, builds green (S-ETS-01-01).
2. CS API Core conformance class implemented with one TestNG `@Test` per ATS assertion (S-ETS-01-02).
3. Historical Sprint 1 baseline: a TeamEngine 5.6.x Docker container loaded the ETS jar via SPI registration and ran Core against `https://api.georobotix.io/ogc/t18/api` (S-ETS-01-03). Sprint 41 supersedes this as the forward runtime with TeamEngine 6.0.0, while retaining the old evidence only as baseline history.

The capability extends mechanically across sprints 2..N: each remaining Part 1 conformance class adds one `conformance.<class>` package and one `<test>` block in `testng.xml`. The wiring is fixed in Sprint 1.

## Architecture overview

```
                 +-------------------------+
                 |  TeamEngine 6.0.0       |
                 |  (Tomcat 10, JDK 17)    |
                 |                         |
                 |  CTL UI (XSLT/Saxon)    |
                 |    |                    |
                 |    v                    |
                 |  ServiceLoader scans    |
                 |    META-INF/services/   |
                 |    *.TestSuiteController|
                 +----------+--------------+
                            |
                            | classloads
                            v
   +---------------------------------------------------------+
   |  ets-ogcapi-connectedsystems10.jar                      |
   |                                                         |
   |  TestNGController (impl TestSuiteController)            |
   |       |                                                 |
   |       v                                                 |
   |  testng.xml -> <test name="Core">                       |
   |       |                                                 |
   |       v                                                 |
   |  conformance.SuitePreconditions (validates iut param)   |
   |       |                                                 |
   |       v                                                 |
   |  conformance.core.LandingPageTests       <-+            |
   |  conformance.core.ConformanceTests       <-+- Sprint 1  |
   |  conformance.core.ResourceShapeTests     <-+            |
   |       |                                                 |
   |       v                                                 |
   |  RestAssured -> HTTP -> IUT                             |
   |  Kaizen / everit-json-schema -> validates response      |
   |  EtsAssert -> structured FAIL msgs w/ /req/* URIs       |
   +---------------------------------------------------------+
                            |
                            | TestNG XML report
                            v
              +-------------+--------------+
              | TeamEngine HTML report     |
              | (user-visible result)      |
              +----------------------------+
```

## Class structure

### Sprint 1 deliverable

| Class | Package | Implements REQs | Sprint |
|---|---|---|---|
| `TestNGController` | `org.opengis.cite.ogcapiconnectedsystems10` | REQ-ETS-TEAMENGINE-001 | 1 |
| `CommandLineArguments` | `org.opengis.cite.ogcapiconnectedsystems10` | (CLI usage; per features10) | 1 |
| `TestRunArg` (enum) | `org.opengis.cite.ogcapiconnectedsystems10` | REQ-ETS-TEAMENGINE-002 (CTL → TestNG param mapping) | 1 |
| `EtsAssert` | `org.opengis.cite.ogcapiconnectedsystems10.util` | REQ-ETS-CORE-001 (structured FAIL msg discipline) | 1 |
| `SuitePreconditions` | `org.opengis.cite.ogcapiconnectedsystems10.conformance` | REQ-ETS-CORE-002 (validates iut reachable) | 1 |
| `SuiteFixtureListener` | `org.opengis.cite.ogcapiconnectedsystems10.listener` | REQ-ETS-CORE-002, -003 (fetches landing + conformance) | 1 |
| `TestRunListener` | `org.opengis.cite.ogcapiconnectedsystems10.listener` | (per features10 stub) | 1 |
| `LoggingTestListener` | `org.opengis.cite.ogcapiconnectedsystems10.listener` | (slf4j logging hook) | 1 |
| `LandingPageTests` | `org.opengis.cite.ogcapiconnectedsystems10.conformance.core` | REQ-ETS-CORE-002 | 1 |
| `ConformanceTests` | `org.opengis.cite.ogcapiconnectedsystems10.conformance.core` | REQ-ETS-CORE-003 | 1 |
| `ResourceShapeTests` | `org.opengis.cite.ogcapiconnectedsystems10.conformance.core` | REQ-ETS-CORE-004 | 1 |

### Sprints 2..N skeleton (placeholders)

| Future class | Package | REQ |
|---|---|---|
| `CommonTests` | `conformance.common` | REQ-ETS-PART1-001 |
| `SystemFeaturesTests` | `conformance.systemfeatures` | REQ-ETS-PART1-002 |
| `SubsystemsTests` | `conformance.subsystems` | REQ-ETS-PART1-003 |
| `DeploymentsTests` | `conformance.deployments` | REQ-ETS-PART1-004 |
| `SubdeploymentsTests` | `conformance.subdeployments` | REQ-ETS-PART1-005 |
| `ProceduresTests` | `conformance.procedures` | REQ-ETS-PART1-006 |
| `SamplingFeaturesTests` | `conformance.samplingfeatures` | REQ-ETS-PART1-007 |
| `PropertyDefinitionsTests` | `conformance.propertydefinitions` | REQ-ETS-PART1-008 |
| `AdvancedFilteringTests` | `conformance.advancedfiltering` | REQ-ETS-PART1-009 |
| `CreateReplaceDeleteTests` | `conformance.createreplacedelete` | REQ-ETS-PART1-010 |
| `UpdateTests` | `conformance.update` | REQ-ETS-PART1-011 |
| `GeoJsonTests` | `conformance.geojson` | REQ-ETS-PART1-012 |
| `SensorMlTests` | `conformance.sensorml` | REQ-ETS-PART1-013 |

(Part 2 classes — REQ-ETS-PART2-001..013 after the Sprint 25 taxonomy correction — are structurally analogous when their sprint cluster runs.)

## Per-suite-class responsibilities (Sprint 1)

### `LandingPageTests` (REQ-ETS-CORE-002)

`@Test` methods (description = `OGC-23-001 /req/core/landing-page-...` per REQ-ETS-CORE-001):

| @Test method | Asserts |
|---|---|
| `landingPageReturns200` | GET `/` → status 200; Content-Type contains `application/json` |
| `landingPageHasTitle` | body has string `title` |
| `landingPageHasDescription` | body has string `description` |
| `landingPageHasLinks` | body has array `links` |
| `landingPageLinksContainConformance` | `links[].rel` includes `conformance` |
| `landingPageLinksContainApiDefinition` | `links[].rel` includes `service-desc` OR `service-doc` (fallback fix preserved per SCENARIO-ETS-CORE-API-DEF-FALLBACK-001) |
| `landingPageDoesNotRequireSelfRel` | sentinel test: PASSES whether `self` is present or absent (preserves v1.0 GH#3 fix per SCENARIO-ETS-CORE-LINKS-NORMATIVE-001) |

### `ConformanceTests` (REQ-ETS-CORE-003)

| @Test method | Asserts |
|---|---|
| `conformanceEndpointReturns200` | GET `/conformance` → status 200 |
| `conformanceBodyHasConformsTo` | body has array `conformsTo` |
| `conformanceConformsToEntriesAreUris` | each `conformsTo[i]` is a string parseable as a URI |
| `conformanceListStashedForDependentSuites` | `ISuite.getAttribute("declaredConformanceClasses")` is set non-null after `@BeforeSuite` runs |

### `ResourceShapeTests` (REQ-ETS-CORE-004)

`@DataProvider` returns one row per "linked resource discoverable from landing page". For each:

| @Test method | Asserts |
|---|---|
| `resourceHasIdField` | response body has string `id` |
| `resourceHasTypeField` | response body has string `type` |
| `resourceHasLinksArray` | response body has array `links` with at least one entry having `href` and `rel` |

(Sprint 1 may scope `ResourceShapeTests` to a single representative resource — likely `/api` or `/conformance` itself — and expand to a true crawl in Sprint 2 once Common is implemented.)

## Interface contracts

### TeamEngine SPI hook (ADR-001)

```
File: src/main/resources/META-INF/services/com.occamlab.te.spi.jaxrs.TestSuiteController
Body (single line):
  org.opengis.cite.ogcapiconnectedsystems10.TestNGController

Class: org.opengis.cite.ogcapiconnectedsystems10.TestNGController
  implements com.occamlab.te.spi.jaxrs.TestSuiteController
  Methods (1:1 port from features10):
    String getCode()        → ets-code property = "ogcapi-connectedsystems10"
    String getVersion()     → project version from ets.properties
    String getTitle()       → ets-title property
    Source doTestRun(Document testRunArgs) throws Exception
```

### TestNG suite parameters (REQ-ETS-TEAMENGINE-002)

```
testng.xml <suite> declares parameters:
  iut                      (required, the CS API landing-page URL)
  auth-credential          (optional, opaque Authorization header value)
  mutation-tests-enabled   (optional, true only for a dedicated mutable IUT)
  mutation-iut-policy      (optional, documents the mutable-IUT policy acknowledgement)
CTL form populates these from user input at the CTL-form layer.
TestRunArg enum values map XML attribute keys to parameter strings.
The CTL UI may label `iut` as the CS API landing page, but serialized TestNG
parameters use `iut`, not `iut-url`. `ics` and `auth-type` are unsupported
unless a later requirement adds Java/TestNG support.
```

### Public package metadata (REQ-ETS-TEAMENGINE-008)

TeamEngine derives the suite title exposed through `TestNGController#getTitle()`
from `src/main/resources/org/opengis/cite/ogcapiconnectedsystems10/ets.properties`,
which maps `ets-title` to `${project.name}`. The Maven `pom.xml` name and
description are therefore part of the public conformance-package metadata, not
just build metadata. They must use the same current scope language as the CTL,
TeamEngine config, README, site docs, Javadoc, sample props, and smoke title
assertion: OGC API - Connected Systems 1.0 with partial Part 1 and Part 2
coverage, TeamEngine 6 forward runtime, local OSH primary E2E, and GeoRobotix
advisory-only status.

### REST Assured request lifecycle (per @Test method)

```
RequestSpecification req = RestAssured
  .given()
  .baseUri(suite.getAttribute("iut"))
  .filter(new RequestLoggingFilter(LogDetail.ALL))   // -> TestNG attachment
  .filter(new ResponseLoggingFilter(LogDetail.ALL))  // -> TestNG attachment
  .filter(new CredentialMaskingFilter(...))          // strips Authorization header from logs
  ;
if (auth.isPresent()) req = applyAuth(req, auth);

Response resp = req.get(relativePath);
EtsAssert.assertStatus(resp, 200, "/req/core/landing-page");
EtsAssert.assertJsonHas(resp, "$.title", "/req/core/landing-page");
...
```

### JSON Schema validation

```
@BeforeSuite (in SuiteFixtureListener):
  load com.reprezen.kaizen.OpenAPIParser  -- but for Sprint 1 we DO NOT use openapi-parser
  load schemas from classpath: src/main/resources/schemas/connected-systems-1/*.json
  via everit-json-schema's SchemaLoader builder
  cache validators in ISuite attributes by schema name

Per @Test:
  Schema landingSchema = (Schema) suite.getAttribute("schema:landing-page");
  landingSchema.validate(new JSONObject(resp.body().asString()));
  // ValidationException → EtsAssert.fail with /req/* URI + violation list
```

(Kaizen `openapi-parser` is on the dep list but Sprint 1's Core suite uses everit-json-schema directly — a transitive dep of ets-common — because the OGC OpenAPI YAML for CS API is not yet stable enough to drive operation-level validation; see Architecture §11 risk #2. Sprint 2+ will revisit.)

### External domain validator boundary

S-ETS-42-01 adds a provisional boundary for reusable SWE Common 3.0 and SensorML 3.0 validators. These libraries are domain validators, not TeamEngine execution owners.

Proposed local shape:

```
Part 1/Part 2 @Test
  -> Connected Systems discovery / candidate selection / media-type gate
  -> ConnectedSystems*ValidatorAdapter
  -> external reusable validator module
  -> ETSAssert failure or TestNG skip/fail decision remains local
```

`ConnectedSystemsSweValidatorAdapter` is the first candidate because `opengeospatial/ets-swecommon30` PR 10 exposes `org.opengis.cite:swecommon30-validator:0.1-SNAPSHOT` with `SweCommonJsonSchemaValidator`. It shall delegate pure SWE Common schema validation only. The current ETS logic remains responsible for `/conf/swecommon-json`, `/conf/swecommon-text`, and `/conf/swecommon-binary` gating; exact `application/swe+json`, `application/swe+text`, and `application/swe+binary` evidence; Observation/Command schema endpoint selection; Time/IssueTime mapping; write-advertisement safety; and no-mutation policy.

The first integration uses a dual-validation flow because the current upstream API
is narrower than the Connected Systems wrapper schemas:

```
Observation/Command schema document
  -> local Connected Systems wrapper-schema validation
  -> extract recordSchema
  -> ConnectedSystemsSweValidatorAdapter.validateComponent(JsonNode)
  -> SweCommonJsonSchemaValidator.validate(node, "sweCommon.json")
  -> ETS-owned SweValidationResult
  -> local ETSAssert failure with the active OGC 23-002 requirement URI
```

`ConnectedSystemsSweValidatorAdapter` returns only immutable, sorted string
diagnostics. It does not expose `ValidationMessage`, call `ETSAssert`, throw
`SkipException`, or own TestNG reporting. Validation-message results describe IUT
violations; missing bundled schemas and validator configuration failures propagate
as suite errors.

Until a published artifact exists, the build may use the CP-002 source-pinned
prebuild at commit `3ba75ceabe57cea85f4a8513c59e0f90e386ba96`.
That path verifies the Git checkout and builds only the parent and
`swecommon30-validator` module. The POM excludes the validator's older NetworkNT
and Jackson transitives, keeps the ETS-managed NetworkNT 1.5.9/Jackson 2.18.0
closure, and includes the validator class/resources in the slim shaded ETS jar.
The final TeamEngine image adds no separate validator, NetworkNT, or ITU jar;
the immutable TeamEngine base's existing library inventory remains unchanged.

The current upstream `validate` method creates a default Draft 2020-12 NetworkNT
schema without enabling format assertions. Its `encodings.json` defines
`BinaryEncoding`, but the root `oneOf` does not select it. Consequently the first
integration validates `recordSchema` with `sweCommon.json` while retaining local
wrapper, format, and encoding validation. Removal of local SWE resources is a
later change gated by external-only fixture parity and complete JSON/Text/Binary
encoding support. During dual validation, the regression corpus covers complete
Observation and Command wrappers for each of JSON, Text, and Binary through both
local wrapper validation and the extracted component adapter. The final-image
verifier also invokes the adapter with valid and invalid components so shaded
schema-resource lookup and relocated NetworkNT execute on TeamEngine's actual
classpath.

`ConnectedSystemsSensorMlValidatorAdapter` is deferred until FCU/OGC provide a reusable SensorML validator module. As of 2026-07-22, no public SensorML library is visible under `FCU-GIS-Luke`, and `opengeospatial/ets-sensorml30` is an ETS scaffold rather than a reusable module. The ETS must not import another TeamEngine ETS jar to obtain SensorML validation.

Replacement is incremental. First add adapter parity tests for current valid/invalid schema fixtures and dual-validate extracted `recordSchema` objects without changing existing PASS/SKIP behavior. Only after external-only parity, format assertions, and complete encoding support may local SWE validation be removed. SensorML full JSON Schema validation replaces the current minimal shape heuristics only after a reusable SensorML module exists. Connected Systems mapping assertions, relation-type checks, parent-child Observation/Command binding evidence, TestNG dependency wiring, and TeamEngine reporting remain in this ETS.

Any implementation that adds validator dependencies must extend the TeamEngine 6 runtime verifier to catch duplicate NetworkNT, ITU, Jackson, SLF4J, Jakarta, TestNG, or TeamEngine jar families and must preserve the selected-payload rule from REQ-ETS-TEAMENGINE-007. The adapter must translate external validator return types such as NetworkNT `ValidationMessage` into ETS-owned diagnostics before test classes see them, so shaded/relocated runtime types do not leak into the conformance-test API.

The added-jar guard's executable self-test must construct at least two accepted
coordinate/path collisions, capture stdout, and compare its complete sorted
`ALLOWED_COLLISION|coordinate|path` set with an exact expected set. Structural
packaging tests must require that behavioral assertion rather than accepting
implementation string literals alone. They also inspect every supported
Jenkinsfile: each must select JDK 17, invoke the source-pin bootstrap, and request
only profile IDs declared by this project's Maven model.

### Auth handling

The current supported credential input is the optional opaque `auth-credential`
suite parameter, applied as the outbound `Authorization` header value. The suite
does not currently accept `auth-type`; bearer/basic/API-key mode selection must
not be documented as supported until a later Java/TestNG change implements it.

Credentials are passed via TestNG suite parameters, kept in request-scope values
only, and **never** logged. The `CredentialMaskingFilter` (custom REST Assured
`Filter`) redacts `Authorization`, `X-API-Key`, and any header named in a
class-level `Set<String>` to `***MASKED***` in the request/response logging
output.

## Test data and fixtures

- **Bundled OGC JSON Schemas**: `src/main/resources/schemas/{connected-systems-1, connected-systems-2, connected-systems-shared, external, fallback}/*.json` — 126 files, copied verbatim from `csapi_compliance/schemas/` per ADR-002. Copying happens manually at S-ETS-01-01 scaffold time; provenance recorded in `ops/server.md`.
- **External domain validators** (planned): reusable SWE Common and SensorML validator modules may replace bundled domain-schema copies only through the adapter and parity-test path specified by `REQ-ETS-VALIDATOR-001`.
- **Sample IUT data** (sprints 2+): `src/main/resources/data/` for shipped sample SensorML / SWE Common payloads (pattern from features10).
- **Spec-trap fixture corpus** (sprints 2+, epic-ets-06): `src/test/resources/fixtures/spec-traps/` for the asymmetric featureType/itemType corpus (~30-50 cases ported as Java `@DataProvider` inputs). NOT in Sprint 1 scope but Sprint 1 must NOT erase the requirement.

## Implementation phasing (per-sprint readiness)

### Sprint 1 (active)

Stories scoped: S-ETS-01-01, -02, -03. See readiness verdicts in §"Implementation Readiness Check" below.

### Sprint 2 (next, post-Sprint-1 success)

Suggested stories:
- S-ETS-02-01: implement `CommonTests` (REQ-ETS-PART1-001) — link relations, content negotiation, OpenAPI Common conformance
- S-ETS-02-02: implement `SystemFeaturesTests` (REQ-ETS-PART1-002) — system collection assertions
- S-ETS-02-03: implement TestNG `dependsOnGroups` wiring across Core → Common → SystemFeatures

### Sprints 3-7

Remaining 11 Part 1 conformance classes (one or two per sprint, depending on assertion count). epic-ets-06 (spec-trap fixture port) runs in parallel as a separate epic.

### Sprint 8+

Part 2 (REQ-ETS-PART2-*) and CITE-submission process work (REQ-ETS-CITE-*).

## Implementation Readiness Check (Sprint 1)

Per the architect role contract, each Sprint 1 story gets a verdict. Verdicts are based on whether ADR-001..005 + this design provide enough specification for a stateless Generator to write the code without ambiguous decisions.

### S-ETS-01-01 — "Generate archetype, modernize to JDK 17, first green build"

**Verdict: PASS**

Rationale:
- Maven coordinates and Java root package fully specified (ADR-003).
- Archetype modernization checklist is exhaustive (ADR-004) — Generator follows the 25-item Group A/B/C/D list. Each delta becomes one ADR row referenced from REQ-ETS-SCAFFOLD-006.
- Reproducibility mechanism (`<project.build.outputTimestamp>`) is concrete (ADR-004 group C-5). The local release gate verifies it via SCENARIO-ETS-SCAFFOLD-REPRODUCIBLE-001.
- Repository layout is fully specified (Architecture §3, ADR-001).
- Schema source is pinned (ADR-002).
- Cross-repo relationship is documented (ADR-005); Generator does NOT need to do anything cross-repo in Sprint 1 except note the schema provenance in `ops/server.md`.

Constraints for Generator:
- MUST: Use `org.opengis.cite:ets-common:17` as parent (not 14, not 18-SNAPSHOT). PRD says `:14` — that's stale; ADR-004 supersedes.
- MUST: Use `org.opengis.cite.ogcapiconnectedsystems10` as Java root package. PRD/spec say `org.opengis.cite.ogcapi.cs10` — ADR-003 supersedes.
- MUST: Use `ets-ogcapi-connectedsystems10` as artifactId. PRD/spec say `ets-ogcapi-connectedsystems-1` — ADR-003 supersedes.
- MUST: Tag each modernization delta with an ADR row referenced from a row in this sprint's commit log per REQ-ETS-SCAFFOLD-006.

Caveat for Generator (NOT a CONCERNS — handled): the ADR-003 / ADR-004 deviation from PRD strings is captured in the ADRs' Consequences sections; Generator references those when CITE-style auditors (or Quinn) ask why the strings differ.

### S-ETS-01-02 — "Implement CS API Core conformance class end-to-end against GeoRobotix"

**Verdict: PASS**

Rationale:
- Three test classes specified (LandingPageTests, ConformanceTests, ResourceShapeTests) with concrete @Test method names and assertion contracts (this design §"Per-suite-class responsibilities").
- v1.0 GH#3 fix and API-def fallback explicitly preserved at the SCENARIO level (SCENARIO-ETS-CORE-LINKS-NORMATIVE-001 + -API-DEF-FALLBACK-001) and at the design-class level (`landingPageDoesNotRequireSelfRel`, `landingPageLinksContainApiDefinition`).
- Assertion failure-message format specified (Architecture §6, EtsAssert pattern with `/req/* URI` always present).
- HTTP/auth/credential-masking lifecycle documented (this design §"REST Assured request lifecycle", §"Auth handling").
- JSON Schema validation pathway specified (this design §"JSON Schema validation") with the explicit Sprint-1 caveat that `everit-json-schema` is the validator and Kaizen's `openapi-parser` is deferred to Sprint 2+ (a known, deliberate scope split).

Constraints for Generator:
- MUST: every `@Test` method's `description` attribute starts with `OGC-23-001 /req/core/...`.
- MUST: SCENARIO-ETS-CORE-LINKS-NORMATIVE-001 must pass — `rel=self` is example-only.
- MUST: SCENARIO-ETS-CORE-API-DEF-FALLBACK-001 must pass — `service-desc` OR `service-doc` is acceptable.
- MUST NOT: add a `@Test` for `rel=self` mandatory; if anti-regression coverage is desired, the test should ASSERT THE PASS CASE (the `landingPageDoesNotRequireSelfRel` sentinel above).
- MUST: use `EtsAssert` with structured FAIL messages including the `/req/*` URI; do not throw bare TestNG `AssertionError`s.

### S-ETS-01-03 — Historical TeamEngine 5.5/5.6.x GeoRobotix smoke

This section preserves Sprint 1 decision context. It is not the active
deployment contract after Sprint 41; use ADR-011, `_bmad/architecture.md`,
REQ-ETS-TEAMENGINE-007/008, `Dockerfile`, `docker-compose.yml`, and
`scripts/smoke-test.sh` for forward TeamEngine 6 work.

**Verdict: CONCERNS** (proceed, with caveats)

Rationale:
- The SPI registration mechanism is concretely specified (ADR-001) and verified against features10's master branch.
- Historical Sprint 1 Dockerfile content used the then-current TeamEngine 5.x baseline (`FROM ogccite/teamengine-production:5.6.1` plus ETS jar copy into TeamEngine). This is retained only as baseline context and is superseded by ADR-011's digest-pinned TeamEngine 6 Dockerfile path.
- The smoke-test script contract is specified (REQ-ETS-TEAMENGINE-005, SCENARIO-ETS-CORE-SMOKE-001).

Concerns the Generator must handle and Quinn must verify:

1. **Historical TeamEngine 5.6.1 base image availability**: The capability spec and PRD referenced TeamEngine 5.5; the actual `ogccite/teamengine-production` master pom pinned 5.6.1. This concern is retained only to explain Sprint 1 baseline evidence. New runtime work MUST NOT use this image path as the forward contract.

2. **`META-INF/services/` filename literalness**: ADR-001 specifies the file path exactly. A common Generator failure mode is to create `META-INF/services/com.occamlab.te.spi.jaxrs.TestSuiteController.txt` or split into multiple files. The file name MUST be the bare interface FQCN with no extension. Quinn check: `unzip -l target/*.jar | grep META-INF/services/` — exactly one matching entry.

3. **CTL wrapper Saxon namespace declaration**: ADR-001 specifies `xmlns:tng="java:org.opengis.cite.ogcapiconnectedsystems10.TestNGController"`. A typo in the package name silently makes the CTL form a no-op (Saxon throws at runtime, not at CTL parse time). Quinn check: actually click "Start" on the CTL form in the smoke-test container and verify the TestNG report is non-empty.

4. **Smoke test as Sprint 1's E2E gate**: `scripts/smoke-test.sh` must produce a non-empty TestNG XML report from a container-launched suite run against the selected real IUT. The E2E mandate applies: archived TestNG XML is the evidence. Quinn verifies the local artifact; Raze verifies it came from the actual smoke run.

Constraints for Generator:
- MUST: smoke test is **scripts/smoke-test.sh** (bash) so local operators receive direct container failure output.
- MUST: smoke test waits for TeamEngine HTTP healthcheck before invoking the suite.
- MUST: smoke test produces an exit code: 0 only if TestNG report is non-empty AND zero suite-registration ERRORs in TeamEngine container logs.
- MUST: archive the TestNG report outside the worktree for gate execution and summarize it in `ops/test-results.md`.

## Security Considerations

This is a server-side test suite; the IUT-facing surface is HTTP-out, not HTTP-in. SSRF is not a concern (we don't accept user input that becomes outbound URLs without operator awareness — the operator IS the user typing the IUT URL into TeamEngine). However:

- **Credential masking** in logs and reports: REQ-ETS-FR-25, NFR-ETS-08. Pattern: `CredentialMaskingFilter` for REST Assured + logback `<pattern>` excluding configured headers. **Tests for this exist** at the unit-test level (NOT shipped in Sprint 1's first commit; defer to Sprint 1 cleanup if time permits).
- **No persistent secrets in the jar**. Auth credentials are TestNG suite parameters (in-memory, scoped to one test run). The jar contains no API keys, no test-fixtures with real credentials.
- **JSON Schema validation must reject unknown-protocol URIs**: a malicious IUT response could reference `file://` or `jar:` URIs in `links[].href`. The schema validator's URI-format check + EtsAssert verifying `https?://` schemes prevents this from becoming a vector. (This is a hardening for a future sprint, not Sprint 1 critical.)

## Performance Considerations

NFR-ETS-04: TeamEngine + ETS jar registers within 30 sec of container start.
NFR-ETS-05: full Part 1 suite completes in <10 min against a responsive IUT.

Sprint 1 (Core only, ~12 @Test methods) is well within NFR-ETS-05; performance is not a Sprint 1 risk. Sprints 2+ should add JaCoCo and capture local gate durations to track regression.

## Implementation Constraints (additional, beyond Sprint 1 stories)

The Generator MUST:
1. Apply ADR-004 modernization checklist Group A-D items as **separate atomic commits** so each is git-bisect-friendly.
2. Use ets-common:17 (release tag), not master.
3. Use the ADR-003 naming for all coordinates and packages.
4. Cite the relevant ADR ID in any commit message that touches scaffolding (e.g. `S-ETS-01-01: pom.xml parent → ets-common:17 (ADR-004 A-1)`).
5. Run `mvn clean install` and capture the output; log the build success or failure to `ops/test-results.md` per CLAUDE.md step 5.
6. Run the smoke test in S-ETS-01-03; capture the TestNG report; archive it.

The Generator MUST NOT:
1. Add a non-ets-common-managed transitive dependency without an ADR.
2. Override an ets-common-managed dep version (everit-json-schema, jackson, jersey, jts) without an ADR explaining why ets-common's pin is wrong.
3. Implement any Part 2 functionality (REQ-ETS-PART2-*).
4. Implement spec-trap fixtures (REQ-ETS-FIXTURES-*) — this is epic-ets-06's scope, parallel sprint.
5. Modify `csapi_compliance/` repo files. The freeze applies. README reposition (REQ-ETS-WEBAPP-FREEZE-001) is a separate epic.

## Testing Strategy

- **Unit tests** (Sprint 1): `src/test/java/...` covers `EtsAssert` formatting, `CredentialMaskingFilter` behavior, `SuiteFixtureListener` parameter parsing. Mockito for HTTP boundary; no live IUT in unit tests.
- **Integration tests** (Sprint 1): the smoke test IS the integration test — TeamEngine + ETS + GeoRobotix end-to-end. No separate integration-test layer needed for Sprint 1.
- **Reproducible-build local gate** (NFR-ETS-01): clean checkout, `mvn install`, save jar, clean checkout again, `mvn install`, and diff jars excluding `META-INF` timestamps. Empty diff is the pass condition.
- **Cross-platform evidence** (NFR-ETS-06): release-candidate checks may be run manually where environments are available. No hosted CI workflow is planned or required under ADR-012.

## Open Items for Future Sprints (NOT Sprint 1)

- Detailed REQ-* per Part 1 class beyond Core (PLACEHOLDER status in spec).
- All of REQ-ETS-PART2-*.
- REQ-ETS-FIXTURES-* (epic-ets-06).
- REQ-ETS-CITE-* (calendar-bound).
- REQ-ETS-WEBAPP-FREEZE-001 (separate quick-win sprint).
- REQ-ETS-SYNC-001 must be implemented as a local/manual schema-sync check; hosted CI activation is outside scope.

## ADR Cross-References

| Decision | Authority |
|---|---|
| TeamEngine SPI registration mechanics | ADR-001 (with ADR-007 cross-reference for Dockerfile-side reality) |
| Schema bundling | ADR-002 |
| Java package + Maven coordinates | ADR-003 |
| Archetype modernization checklist | ADR-004 (extended via ADR-006 Group F retro-row) |
| Cross-repo relationship | ADR-005 |
| Jersey 1.x → Jakarta EE 9 / Jersey 3.x port | ADR-006 (Sprint 2 retro) |
| Historical Dockerfile base image deviation (`tomcat:8.5-jre17`) | ADR-007 (Sprint 2 retro; superseded for forward runtime by ADR-011) |
| EtsAssert REST/JSON helper API surface | ADR-008 (Sprint 2 forward-looking) |
| Multi-stage Dockerfile pattern | ADR-009 (Sprint 2 forward-looking) |
| Logging stack (slf4j + logback) | Architecture §6 + this design.md §"CredentialMaskingFilter wiring" (Sprint 2) |

## Sprint 2 Ratifications (2026-04-28)

The following sections were added by Architect (Alex) at Sprint 2 ets-02 to formalize decisions Pat (Planner) deferred. They bind the Sprint 2 Generator (Dana) and every conformance.* class added in Sprint 2+.

### EtsAssert helper API (Sprint 2 S-ETS-02-02)

Full specification at **ADR-008**. Summary for design.md readers:

- 5 new static helpers added to `org.opengis.cite.ogcapiconnectedsystems10.ETSAssert`:
  - `assertStatus(Response resp, int expected, String reqUri)` — covers ~7 of 21 Sprint-1 sites.
  - `assertJsonObjectHas(Map<String,Object> body, String key, Class<?> type, String reqUri)` — covers ~5 sites.
  - `assertJsonArrayContains(List<?> array, Predicate<Object> pred, String desc, String reqUri)` — covers ~5 sites.
  - `assertJsonArrayContainsAnyOf(List<?> array, List<Map.Entry<String, Predicate<Object>>> alternatives, String reqUri)` — covers the OR-fallback patterns (~2 sites: `service-desc OR service-doc`; `rel=collection AND/OR rel=items`).
  - `failWithUri(String reqUri, String message)` — universal escape hatch (~2 sites: sentinels, custom multi-step assertions).
- Every helper raises `java.lang.AssertionError` (not TestNG `SkipException`) with the OGC `/req/*` URI as the message prefix.
- Every helper has at least one PASS-path + one FAIL-path unit test under `src/test/java/.../VerifyETSAssert.java`.
- **Constraint binding Sprint 2+**: zero `throw new AssertionError(...)` permitted in `conformance.*` subpackages; Quinn enforces via `grep -E 'throw new AssertionError|Assert\.fail' src/main/java/.../conformance/`. See ADR-008 §"Constraints" for the full list.
- Refactor discipline (S-ETS-02-02): one commit per test class (3 commits — LandingPageTests, ConformanceTests, ResourceShapeTests); smoke-test 12/12 PASS verified at every commit boundary.

Refactoring examples for the 21 Sprint-1 sites are in ADR-008 §"Examples drawn from actual Sprint 1 sites".

### Historical Dockerfile multi-stage build (Sprint 2 S-ETS-02-05)

This ADR-009 summary is historical. ADR-011 supersedes the forward runtime with
the digest-pinned OGC TeamEngine 6 Dockerfile path; do not reintroduce the
Tomcat 8.5/manual TeamEngine 5.6.1 assembly or Maven-profile dependency-copy
runtime.

Full specification at **ADR-009**. Summary for design.md readers:

- Historical two-stage Dockerfile: `eclipse-temurin:17-jdk-jammy` build stage + `tomcat:8.5-jre17` runtime stage (preserving ADR-007's then-current runtime base choice and the 3 secondary patches). ADR-011 supersedes this for forward TeamEngine 6 work.
- Build stage uses BuildKit `--mount=type=cache,target=/root/.m2` to amortize Maven dep download across `docker build` invocations.
- Layer ordering optimized for cache: pom.xml + `dependency:go-offline` BEFORE source COPY; rare-changing layers (TE WAR download, JAXB jars) BEFORE per-commit layers (`COPY --from=builder`).
- Runtime image runs as non-root `USER tomcat` (REQ-ETS-CLEANUP-004 mandate); `chown -R tomcat:tomcat /usr/local/tomcat` before USER switch.
- Image size target: ≤ 450MB (vs Sprint 1 single-stage ~600MB); soft target 400MB.
- `scripts/smoke-test.sh` simplifies post-multi-stage: drops the host-`mvn -B clean package` and `mvn dependency:copy-dependencies` steps (now handled inside `docker build`); only `docker build .` is needed at smoke time. Eliminates Quinn s03 / Raze s03 host-`~/.m2` brittleness.

The ADR explicitly REJECTED options (b) (pre-staged target/lib-runtime split-only) and (c) (pom.xml profile bakes deps closure) — both fail to eliminate the host-Maven dependency.

### SystemFeatures conformance class scope (Sprint 2 S-ETS-02-06)

> **Entire section superseded by Sprint 47.** Everything through the next
> top-level design heading records the historical Sprint 2/3 implementation
> sequence only. The four-method table, cached setup, raw dependency behavior,
> and expansion names are not active design requirements or mappings. The
> released six-procedure design under "Sprint 47: released Part 1 System direct
> procedures" is authoritative.

**Architect ratifies: Sprint-1-style minimal-then-expand. 4 @Test methods at Sprint 2 close, full-coverage expansion deferred to Sprint 3.**

Pat enumerated 4 SCENARIOs in REQ-ETS-PART1-002 (now SPECIFIED in spec.md). Architect maps these to 4 @Test methods, mirroring the LandingPageTests/ConformanceTests pattern:

| @Test method | Asserts | Scenario closed |
|---|---|---|
| `systemsCollectionReturns200` | `GET /systems` → status 200; Content-Type contains `application/json` | SCENARIO-ETS-PART1-002-SYSTEMFEATURES-LANDING-001 (CRITICAL) |
| `systemsCollectionHasItemsArray` | body has array `items` (or `features` if CS API server uses GeoJSON wrapper); array is non-empty (Generator MUST curl-verify before writing assertion) | SCENARIO-ETS-PART1-002-SYSTEMFEATURES-LANDING-001 (CRITICAL) |
| `systemItemHasIdTypeLinks` | for the first item in the collection: has string `id`, string `type` (matching `System` or the IUT's discriminator), array `links` per REQ-ETS-CORE-004 base shape | SCENARIO-ETS-PART1-002-SYSTEMFEATURES-RESOURCE-SHAPE-001 (NORMAL) |
| `systemsCollectionLinksDiscipline` | collection-level `links` array contains `rel=collection` AND/OR `rel=items` per OGC Common; absence of `rel=self` is NOT FAIL (carries v1.0 GH#3 fix policy from Core landing page) | SCENARIO-ETS-PART1-002-SYSTEMFEATURES-LINKS-NORMATIVE-001 (NORMAL) |

The following direct `dependsOnGroups="core"` wiring records the historical
Sprint 2 design. Sprint 46 supersedes it with
`systemfeatures -> part1apicommon -> core common`, preserving Core failure
cascade while adding the released API Common and inherited Common
prerequisites:

```xml
<test name="SystemFeatures">
  <packages>
    <package name="org.opengis.cite.ogcapiconnectedsystems10.conformance.systemfeatures"/>
  </packages>
  <groups>
    <dependencies>
      <group name="systemfeatures" depends-on="core"/>
    </dependencies>
  </groups>
</test>
```

The current `dependsOnGroups` semantics auto-skip API Common and then every
`@Test` in `conformance.systemfeatures.*` if Core or Common produces FAIL.
Sprint 46 verifies that current transitive chain with the live sabotage gate.

#### Subpackage layout

`org.opengis.cite.ogcapiconnectedsystems10.conformance.systemfeatures.SystemFeaturesTests` — single class for Sprint 2. Mirrors the 1:1 LandingPageTests/ConformanceTests/ResourceShapeTests pattern from `conformance.core.*`. If Sprint 3+ expansion grows the @Test count beyond ~10, split into `SystemFeaturesCollectionTests` + `SystemFeaturesItemTests` (deferred to Sprint 3 per below).

#### Fixtures and listeners

No new fixtures or listeners needed for Sprint 2. The existing `SuiteFixtureListener` (which fetches landing page + `/conformance` per ADR-001) supplies the IUT base URL via `SuiteAttribute.IUT`. SystemFeaturesTests reads `iutUri` the same way Core's classes do.

`@BeforeClass` in `SystemFeaturesTests` performs the `GET /systems` once and caches the response shape into a class-level field (so the 4 @Tests don't redundantly hit the IUT). Pattern mirrors `ConformanceTests.fetchConformancePage()`.

#### Historical coverage scope rationale (Sprint-1-style narrowing)

> **Superseded by Sprint 47.** This subsection records the Sprint 2/3
> implementation sequence only. Its approximation method names and deferred
> expansion list are not active design requirements, mappings, or future
> roadmap. The released six-procedure design under "Sprint 47: released Part 1
> System direct procedures" is authoritative.

Pat recommended Sprint-1-style narrowing for risk control on the first pattern extension. Architect concurs because:

1. **The architectural pattern is being extended for the first time**. Sprint 2 proves the extension works mechanically. Minimizing the per-class surface area maximizes the signal-to-noise of "did the pattern extend?" vs "did we get the assertion logic right?"
2. **The 4 chosen SCENARIOs cover the foundational shape** (collection landing, items array, item shape, links discipline). The remaining ~8-12 ATS items in OGC 23-001 Annex A `/conf/system-features/` (canonical-url, location-time, collections, write operations, advanced filtering interactions) layer on top — once the foundation is proven, expansion is mechanical.
3. **Beta gate doesn't require full per-class coverage**. CITE SC review approves on the basis of "the test class exists, runs, and produces deterministic verdicts" — depth comes during the 6-12 month beta period via passing-IUT outreach.
4. **GeoRobotix's `/systems` collection shape is unknown until Generator curls it**. Acceptance criterion #1 mandates the curl-first approach; if `/systems` returns an unexpected shape (e.g. paginated wrapper, GeoJSON FeatureCollection), 4 @Tests adapt cleanly while 12-15 would force structural choices we'd regret.

The historical Sprint 3 expansion list was:

- `systemCanonicalUrlReturns200` — REQ-ETS-PART1-002 / `/req/system/canonical-url`
- `systemHasGeometryAndValidTime` (NORMAL — `MAY` priority) — REQ-ETS-PART1-002 / `/req/system/location-time`
- `systemAppearsInCollections` — REQ-ETS-PART1-002 / `/req/system/collections`
- `systemFeaturesPagination` — pagination correctness if `/systems` returns `next` link
- Plus ~4 more covering filter-by-property and filter-by-time interactions

Architect estimates Sprint 3 SystemFeatures expansion at ~4 hours Generator time (mechanical extensions).

#### What NOT to ship in Sprint 2

- **Spec-trap fixture port**: the `asymmetric-feature-type/` fixture group from `csapi_compliance/tests/fixtures/spec-traps/` is REQ-ETS-FIXTURES-* / epic-ets-06 scope. Generator MUST NOT port it inline as part of S-ETS-02-06; the SCENARIO references it only as future-ready context.
- **Write-operation coverage** (POST / PUT / DELETE on `/systems`): REQ-ETS-PART1-010 (`create-replace-delete`) scope; deferred to Sprint 4+.
- **Cross-IUT testing**: GeoRobotix is the canonical Sprint 2 IUT. Multi-IUT smoke is REQ-ETS-CITE-002 (three-implementation outreach) at beta.

### CredentialMaskingFilter wiring (Sprint 2 S-ETS-02-04)

Architect rules **NO separate ADR** for CredentialMaskingFilter. Justification: the implementation is wire-the-OGC-pattern-verbatim (REST-Assured `Filter` SPI is well-trodden; logback `<pattern>` masking is a 5-line config; v1.0 `csapi_compliance/src/engine/credential-masker.ts` provides the masking semantics verbatim). The decision surface is too small for an ADR — design.md inline is sufficient. The audit-trail weight Pat flagged is captured by (a) NFR-ETS-08 in the PRD already mandating credential masking, (b) the credential-leak integration test required by S-ETS-02-04 acceptance criteria, (c) the SCENARIO-ETS-CLEANUP-LOGBACK-MASKING-001 / NFR-ETS-08 spec entry.

#### Class location and pattern

`org.opengis.cite.ogcapiconnectedsystems10.listener.CredentialMaskingFilter` — `listener/` subpackage parallels the existing `ReusableEntityFilter` (which is also a REST-Assured `Filter`). Implements `io.restassured.filter.Filter`; constructor takes `Set<String>` of header names to mask (defaults to `Authorization`, `X-API-Key`, `Cookie`, `Set-Cookie`, `Proxy-Authorization` per v1.0 reference).

#### Masking semantics (verbatim port from v1.0)

Read `csapi_compliance/src/engine/credential-masker.ts` lines 35-41:

```
if value.length <= 8: return "****"
else: return value[0:4] + "***" + value[-4:]
```

Java port preserves the same semantics:

```java
public static String maskValue(String value) {
    if (value == null || value.isEmpty()) return "****";
    if (value.length() <= 8) return "****";
    return value.substring(0, 4) + "***" + value.substring(value.length() - 4);
}
```

Edge cases (carry from v1.0):
- Bearer-prefix preservation: input `"Bearer ABCDEFGH12345678WXYZ"` → output `"Bear***WXYZ"` (mask the entire credential value INCLUDING the Bearer prefix; the SCENARIO-ETS-CLEANUP-LOGBACK-MASKING-001 acceptance criterion expects this — the literal substring `EFGH12345678WXYZ` must NOT appear, and a recognizable masked form like `Bear...WXYZ` MUST appear).
- Empty string: returns `"****"`.
- Credentials < 8 chars: full redaction `"****"` (avoids leaking length information that could enable shoulder-surfing reconstruction).
- Non-credential headers (Content-Type, Accept, etc.): pass through unchanged (the filter only intervenes on the configured header set).

#### Wiring point

Register the filter in `SuiteFixtureListener.onStart()` alongside the existing REST-Assured baseline config. Generator updates the REST-Assured `RestAssured.filters(...)` global registration to include the new filter ONCE per suite execution.

#### Logback configuration

`src/main/resources/logback.xml`:

```xml
<configuration>
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <!-- Pattern excludes %X{Authorization} and %X{X-API-Key} from MDC output -->
      <pattern>%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>
  <root level="INFO">
    <appender-ref ref="STDOUT"/>
  </root>
  <logger name="io.restassured" level="DEBUG"/>
  <logger name="org.opengis.cite.ogcapiconnectedsystems10" level="DEBUG"/>
</configuration>
```

**Implementation reality** (reconciled 2026-04-28T22:50Z post-Raze CONCERN-1 on Sprint 2 cleanup gate): the `CredentialMaskingFilter` does NOT mutate the outgoing REST-Assured request/response payloads (mutating Authorization headers in-flight would break authenticated IUT calls). Instead, it observes via REST-Assured's `Filter` SPI and emits a **parallel FINE-level masked log entry** alongside REST-Assured's built-in `RequestLoggingFilter` output. The filter's masking applies only to the parallel log entry; REST-Assured's own request/response logger (if attached) emits unmasked headers as a side effect. **Defense-in-depth**: logback's pattern intentionally omits `%X{*}` MDC dump, and the architect's `should` constraint #3 directs operators to attach the masking filter, NOT REST-Assured's `RequestLoggingFilter`, in any production-like configuration. **Sprint 3 hardening**: wrap REST-Assured's `RequestLoggingFilter` with a masking variant so the unmasked side channel is closed (see "Sprint 3 hardening: MaskingRequestLoggingFilter wrap pattern" below).

#### Sprint 3 hardening: MaskingRequestLoggingFilter wrap pattern (S-ETS-03-02)

##### Sprint 6 redesign: approach (i) — wire-side correctness via no-spec-mutation (S-ETS-06-01) — CANONICAL

**Sprint 6 update (2026-04-30)**: The Sprint 3 subclass-based wrap pattern documented below was diagnosed as DEFECTIVE by Sprint 5 Raze adversarial review (GAP-1'): `super.filter()` internally calls `ctx.next()` (the actual HTTP send) WHILE the temporary masked-header swap is in effect, so the wire request carries the **masked** credential — not the original. The `try/finally` restoration block runs AFTER `ctx.next()` returns and so cannot affect the request that is already on the wire. The IUT therefore receives the masked credential string and rejects every authenticated request as 401. (This is also why the Sprint 5 GAP-2 sabotage `.git`-exclude masked the latent javac defect: the live cascade could never run.)

**Approach (i) — now canonical (ratified by meta-Raze + primary Raze + Quinn at Sprint 6 close)**:

`MaskingRequestLoggingFilter.filter()` SHALL NOT call `super.filter()` and SHALL NOT mutate `requestSpec` headers. Instead:

1. **Snapshot** the current values of credential-bearing headers READ-ONLY via `requestSpec.getHeaders().getValue(name)`.
2. **Build the masked log line** in a `StringBuilder`, substituting `CredentialMaskingFilter.maskValue(value)` for each captured value.
3. **Emit** the masked log line directly to a shadowed `private final PrintStream stream` field on the filter (REST-Assured 5.5.0's parent `RequestLoggingFilter` declares `stream` as `private final` with no accessor, so the subclass cannot reach the parent's stream — Plan-Raze verified via Maven Central source-jar inspection; the shadowed field is the documented escape).
4. **Call `ctx.next(requestSpec, responseSpec)` directly** with the **unmutated** `requestSpec` — the wire carries the **ORIGINAL** credential.
5. **`super.filter()` is never invoked.** No header mutation. No `try/finally`. No restoration step (because nothing was mutated).

The Sprint 6 unit test `VerifyWireRestoresOriginalCredential` (4 @Tests; sister repo `src/test/java/.../listener/VerifyWireRestoresOriginalCredential.java`) uses a `CapturingFilterContext` that snapshots header values **BY VALUE** at `ctx.next` call time. The legacy 16 wiring-only tests (`VerifyAuthCredentialPropagation` 8 + `VerifyMaskingRequestLoggingFilter` 8) used `StubFilterContext` which captured by reference — they read post-restoration state and could not see the bug. With the Sprint 5 filter, `wireCarriesOriginalAuthorizationCredential` FAILed `expected:<Bear[er ABCDEFGH12345678]WXYZ> but was:<Bear[***]WXYZ>`. Under approach (i), all 4 wire-side @Tests PASS.

**`super.filter()` is no longer called**, so the `try/finally` "restoration" pattern documented further below is **historical** — the false claim *"the try/finally pattern guarantees the IUT receives the real credential header even if super.filter() throws"* (item #4 in the historical list below) is **incorrect**: under the Sprint 3 design, `super.filter()` itself emitted the request to the wire while the masked header was in place, so the try/finally could only restore the spec for any subsequent filters in the chain, not for the network round-trip already issued.

**Sprint 7 doc-lag close (Wedge 5, REQ-ETS-CLEANUP-018)**: this subsection was added to close meta-Raze META-GAP-M1 (Sprint 6 missed self-audit — design.md §Sprint 3 hardening still described the OLD wrap pattern as canonical after the Sprint 6 redesign landed).

**Cross-references**:

- ADR-010 v3 amendment (Sprint 5 close) — independently documents the dependency-skip cascade strategy under approach (i).
- REQ-ETS-CLEANUP-016 (spec.md) — the Sprint 6 wire-side correctness REQ; status IMPLEMENTED at Sprint 6 close + closure-proof verified at Sprint 6 gate.
- REQ-ETS-CLEANUP-011 (spec.md) — the Sprint 4 credential-leak E2E REQ; auto-PASS for the script three-fold under approach (i) once Wedge 3 closes (Sprint 7 S-ETS-07-01).

##### Historical (Sprint 3 baseline — superseded by Sprint 6 approach (i) above)

> The remainder of this subsection (architect ratification, code listing, "why subclass" rationale, and the original integration-test rules) describes the Sprint 3 baseline pattern. It is RETAINED as historical context for the v1.0 → v1.1 evolution. **DO NOT use the code listing below as the canonical implementation reference** — the canonical filter is the Sprint 6 approach (i) variant in the sister repo at `src/main/java/.../listener/MaskingRequestLoggingFilter.java` (HEAD `c17a534+` post Sprint 7).

**Architect ratifies: subclass-based wrap (Pat's option (a)) — NO separate ADR (precedent: CredentialMaskingFilter NO-ADR ruling).** Justification: the wrap pattern uses REST-Assured 5.5.0's public Filter SPI (well-trodden); the reusable masking semantics already live in `CredentialMaskingFilter.maskValue(...)` (Sprint 2 verbatim port from v1.0); the wrap is a 30-50 LOC subclass override. Decision surface is too small for a standalone ADR. The audit weight is carried by (a) NFR-ETS-08 + SCENARIO-ETS-CLEANUP-LOGBACK-MASKING-001 (already in spec), (b) the credential-leak integration test now mandated by S-ETS-03-02 acceptance criteria (no longer deferred), (c) ADR-010 §"Notes / references" (which cross-references this design.md section as the canonical wrap pattern reference).

> **Sprint 5 GAP-1' supersession**: this Sprint 3 ratification was retroactively invalidated by Sprint 5 Raze adversarial review — see "Sprint 6 redesign: approach (i)" subsection above. The historical ratification is preserved here for archaeological accuracy; the canonical pattern is approach (i).

**Class location and pattern (Sprint 3 baseline — historical; superseded)**:

`org.opengis.cite.ogcapiconnectedsystems10.listener.MaskingRequestLoggingFilter` — sibling of `CredentialMaskingFilter` in the same `listener/` subpackage. Sprint 3 baseline: Extends REST-Assured's `io.restassured.filter.log.RequestLoggingFilter`. **Sprint 6 reality**: the class still extends `RequestLoggingFilter` (for the constructor signature and any consumer code that does `instanceof`), but `filter()` no longer calls `super.filter()` — see approach (i) above for the canonical implementation.

```java
package org.opengis.cite.ogcapiconnectedsystems10.listener;

import io.restassured.filter.FilterContext;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.io.PrintStream;
import java.util.Set;

/**
 * REST-Assured RequestLoggingFilter variant that masks credential-bearing headers
 * before they reach the underlying log stream.
 *
 * Closes the unmasked side-channel that the parallel CredentialMaskingFilter cannot.
 * Sprint 3 hardening per S-ETS-03-02; design.md §"Sprint 3 hardening: MaskingRequestLoggingFilter
 * wrap pattern (S-ETS-03-02)".
 */
public class MaskingRequestLoggingFilter extends RequestLoggingFilter {

    private final Set<String> headersToMask;

    public MaskingRequestLoggingFilter(Set<String> headersToMask, PrintStream stream) {
        super(stream);
        this.headersToMask = Set.copyOf(headersToMask);
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {
        // Snapshot original header values, replace with masked equivalents for the
        // duration of the super.filter() call (which writes to the configured stream),
        // then restore originals so the actual HTTP request still carries the unmasked
        // credentials to the IUT.
        var originals = new java.util.HashMap<String, String>();
        for (String name : headersToMask) {
            String value = requestSpec.getHeaders().getValue(name);
            if (value != null) {
                originals.put(name, value);
                requestSpec.removeHeader(name);
                requestSpec.header(name, CredentialMaskingFilter.maskValue(value));
            }
        }
        try {
            return super.filter(requestSpec, responseSpec, ctx);
        } finally {
            // Restore originals — IUT MUST receive the real credentials.
            for (var entry : originals.entrySet()) {
                requestSpec.removeHeader(entry.getKey());
                requestSpec.header(entry.getKey(), entry.getValue());
            }
        }
    }
}
```

**Why subclass + temporary header swap (not chained-filter, not full-replacement)**:

1. **Subclass preserves all built-in formatting.** `RequestLoggingFilter` has 200+ LOC of payload-pretty-printing, multipart handling, query-string formatting, etc. Subclassing inherits all of it; only the header-emission step is intercepted via header swap.
2. **Chained-filter-with-registration-order (Pat's option (b)) is fragile.** It depends on REST-Assured invoking filters in registration order (which it does, currently — `io.restassured.internal.filter.FilterContextImpl`) but a future REST-Assured release could reorder filters via SPI annotations. Subclass-based composition is contractually stable.
3. **Replace-entirely (Pat's option (c)) is overkill.** Re-implementing 200+ LOC of formatting code for ~10 lines of masking gain creates a maintenance burden — every REST-Assured upgrade requires re-syncing the formatter. The 30-50 LOC subclass is the minimal touch.
4. **[INVALIDATED — Sprint 5 GAP-1' / Sprint 7 Wedge 5]** ~~**Header swap (vs payload mutation) is restorable.** The `try/finally` pattern guarantees the IUT receives the real credential header even if `super.filter()` throws; the masked headers exist only during the formatter's read.~~ **Sprint 5 Raze diagnosis (META-GAP)**: `super.filter()` internally calls `ctx.next()` (the actual HTTP send) while the masked header swap is in effect, so the try/finally restoration runs AFTER the wire request was already issued — the IUT receives the masked credential, not the original. This rationale was used to ratify the wrap pattern but is fundamentally incorrect. See approach (i) at the top of this section for the canonical Sprint 6+ implementation that does not call `super.filter()` and therefore needs no restoration.

**Wiring point**:

In `SuiteFixtureListener.onStart()`, REPLACE the bare `new RequestLoggingFilter(LogDetail.ALL)` registration (currently at design.md §171 "REST Assured request lifecycle") with the masking variant:

```java
// Before (Sprint 2):
RestAssured.filters(
    new RequestLoggingFilter(LogDetail.ALL),  // <-- unmasked side channel
    new CredentialMaskingFilter(Set.of("Authorization", "X-API-Key", "Cookie"))
);

// After (Sprint 3):
RestAssured.filters(
    new MaskingRequestLoggingFilter(
        Set.of("Authorization", "X-API-Key", "Cookie", "Set-Cookie", "Proxy-Authorization"),
        System.out
    ),
    new CredentialMaskingFilter(Set.of("Authorization", "X-API-Key", "Cookie"))  // parallel FINE log; defense-in-depth retained
);
```

The `CredentialMaskingFilter` registration is RETAINED as defense-in-depth (parallel FINE-level log is still useful for forensic review). Both filters operate independently; both must be registered.

**Header set rationale**:

The MaskingRequestLoggingFilter's mask set is a SUPERSET of CredentialMaskingFilter's: adds `Set-Cookie` (response side; the formatter logs response headers too) and `Proxy-Authorization` (rare but present in some CITE harness configs). The intersection is intentional — both filters mask Authorization/X-API-Key/Cookie because they are the highest-priority credentials and a defense-in-depth approach masks them at every observation point.

**Unit + integration test rules (per S-ETS-03-02 acceptance criteria)**:

- Unit tests in `src/test/java/.../listener/VerifyMaskingRequestLoggingFilter.java`: cover (a) Bearer 24-char masked in formatter output, (b) X-API-Key 16-char masked, (c) Set-Cookie response header masked in response logging, (d) IUT-side header restoration verified via `requestSpec.getHeaders().getValue()` after `filter()` returns, ~~(e) try/finally restoration even when `super.filter()` throws (mock RuntimeException)~~.

  **[INVALIDATED — Sprint 8 S-ETS-08-01 Wedge 3 (project-wide self-audit grep, META-GAP-S7-3 closure)]**: item (e) describes a deleted test scenario. Under approach (i) (Sprint 6 S-ETS-06-01 — see canonical block at top of this section, lines ~535-552), `MaskingRequestLoggingFilter.filter()` no longer calls `super.filter()` and no longer mutates `requestSpec`, so there is no try/finally restoration to test. The 2 legacy try/finally-semantic tests in `VerifyMaskingRequestLoggingFilter` were DELETED per Pat's Sprint 6 S-06-03 finer-granularity disposition (verified non-existent code under approach (i)); the `ThrowingFilterContext` helper used only by those tests was also deleted. The remaining unit tests in `VerifyMaskingRequestLoggingFilter` are reclassified as "wiring-only — does NOT prove wire-side credential integrity" (see spec.md REQ-ETS-CLEANUP-014 Implementation Notes). Wire-side proof now lives in `VerifyWireRestoresOriginalCredential` via `CapturingFilterContext` (BY-VALUE header snapshot at `ctx.next` time). This S-ETS-03-02 acceptance-criterion bullet is preserved here for audit history; item (e) is retired.
- Integration test (`scripts/verify-credential-leak.sh`): smoke-test.sh with synthetic `auth-credential=Bearer ABCDEFGH12345678WXYZ`; grep TestNG XML attachments + container logs + REST-Assured stdout for the literal `EFGH12345678WXYZ` (zero hits required); assert masked form `Bear...WXYZ` IS present (proving filter ran rather than dropping the header). This integration test was DEFERRED in Sprint 2 (Quinn cleanup CONCERN-1) and is now mandated by S-ETS-03-02.

**Risks**:

- **REST-Assured 5.6+ API drift.** `RequestLoggingFilter` constructor signature could change. Mitigation: lock REST-Assured version in pom.xml; the masking variant is a thin subclass that's easy to re-sync.
- **Header set drift.** New credential header names (e.g. `X-Auth-Token` from a future IUT) won't be masked unless added to the Set. Mitigation: integration test runs with a representative credential set per IUT; failures surface unmasked headers.
- **PrintStream choice.** `System.out` is the conventional REST-Assured target; some test runners may redirect it. Mitigation: SuiteFixtureListener configures the stream explicitly; tests can inject a `ByteArrayOutputStream` for assertion.

#### Unit + integration test rules (per S-ETS-02-04 acceptance criteria)

- Unit tests in `src/test/java/.../listener/VerifyCredentialMaskingFilter.java`: cover (a) Bearer 24-char masked correctly, (b) API key 16-char masked correctly, (c) credential < 8 chars fully redacted, (d) non-credential header pass-through.
- Integration test: smoke-test.sh with synthetic `auth-credential=Bearer ABCDEFGH12345678WXYZ`; grep TestNG XML attachments + container logs for the literal `EFGH12345678WXYZ` (zero hits required); also grep for the masked form `Bear...WXYZ` (must be present, proving filter ran rather than dropping the field entirely).

### ADR-001 cross-reference amendment

ADR-001 §Consequences ("**Positive**" bullet 2) originally claimed: "TeamEngine 5.6.1 production Docker image (`opengeospatial/teamengine-docker/teamengine-production` master, `teamengine.version=5.6.1`) loads the resulting jar without modification." Per ADR-007 §Context, this claim is empirically false for our JDK 17 ETS jar (production image runs JDK 8).

Architect choses **option (i) — lightweight footnote amendment** (not full ADR-001 rewrite, not new ADR-001v2). The amendment adds a one-line cross-reference to ADR-007 in ADR-001's Consequences section, leaving the rest of ADR-001's content (which is correct about the SPI registration mechanics) untouched. Generator (Dana) applies the amendment as part of S-ETS-02-01 acceptance criterion #7.

Rationale for option (i) over (ii) full rewrite: ADR-001 is correct about the SPI registration mechanics (META-INF/services file, TestNGController class, ets.properties, testng.xml, CTL wrapper — all verified at runtime in S-ETS-01-03 smoke). Only the one parenthetical remark about "production Docker image loads it without modification" is wrong. A footnote is the lightest touch that preserves the historical record.

## Sprint 4 Ratifications (2026-04-29)

### Sprint 4 hardening: credential-leak E2E via stub IUT (S-ETS-04-03)

**Architect ratifies: option (a) stub IUT in `/tmp/`** — REJECTS option (b) authenticated IUT pivot (sacrifices hermeticity; CITE SC reviewers cannot reproduce without IUT credentials) and option (c) extended unit-layer fallback (already shipped in Sprint 3 `VerifyMaskingRequestLoggingFilter` unit tests; insufficient as E2E evidence per Quinn cumulative CONCERN-3 / Raze cumulative CONCERN-1 deeper-E2E gap).

Justification:

1. **Composability with S-ETS-04-04 sabotage-script bug fixes**. The Sprint 3 stub-server pattern (per ADR-010 §Decision option b) already exists in bash form at `scripts/verify-dependency-skip.sh`. S-ETS-04-04 fixes the known sabotage-script bugs (Pat enumerated; mostly mechanical). Extending the same stub-server to also echo the inbound `Authorization` header in a 401 response gives a single hermetic primitive that powers BOTH the dependency-skip verification AND the credential-leak verification — minimum new code.
2. **Hermeticity preserved**. `/tmp/` stub IUT has no network egress, no real credentials, no IUT-vendor coordination. The synthetic credential `Bearer ABCDEFGH12345678WXYZ` is the same Sprint 2 + Sprint 3 unit-test fixture; reusing it gives the integration test trivial reproducibility.
3. **The masking gap is a SIDE-CHANNEL gap, not an IUT-vendor-specific gap**. CredentialMaskingFilter + MaskingRequestLoggingFilter both operate against the outbound REST-Assured request lifecycle independent of which IUT receives the call. A stub IUT that simply records "yes, I received an Authorization header; here's what I saw verbatim" gives sufficient E2E coverage to assert the masking pipeline did its job without leaking the credential into TestNG XML attachments / container logs / REST-Assured stdout.

Reject (b): pivoting to an authenticated IUT (e.g. GeoRobotix with a leased credential) would (i) introduce vendor-coordination latency, (ii) leak a real credential into the project's test corpus (CITE SC submission risk), (iii) fail closed if IUT is offline. Reject (c): unit-layer tests don't exercise REST-Assured's actual request emission pipeline; insufficient as the deferred-from-Sprint-3 E2E evidence.

#### Stub IUT extension pattern

`scripts/stub-iut.sh` (NEW; or extend the Sprint 3 stub-server inline within `scripts/verify-credential-leak.sh`):

```bash
#!/usr/bin/env bash
# Sprint 4 stub IUT for credential-leak E2E verification.
# Echoes the inbound Authorization header back in the 401 response body so
# downstream test logic can assert "what the stub received" vs "what the logs
# / TestNG attachments captured" — proving the masking pipeline worked.
#
# Per design.md §"Sprint 4 hardening: credential-leak E2E via stub IUT (S-ETS-04-03)".

set -euo pipefail
PORT="${1:-0}"  # 0 = ephemeral; bind script writes resolved port to /tmp/stub-iut-port

python3 - <<'PYEOF' &
import http.server
import socketserver
import sys
import os

class StubIUT(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        # Capture incoming Authorization header verbatim for echo-back.
        auth = self.headers.get("Authorization", "")
        body = f'{{"received_authorization": "{auth}"}}\n'.encode()
        self.send_response(401)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)
    def log_message(self, format, *args):
        pass  # suppress access log

sock = socketserver.TCPServer(("127.0.0.1", 0), StubIUT)
port = sock.server_address[1]
with open("/tmp/stub-iut-port", "w") as f:
    f.write(str(port))
sock.serve_forever()
PYEOF

# Caller reads /tmp/stub-iut-port to discover the bound port; stub keeps running until killed.
echo "Stub IUT started on port $(cat /tmp/stub-iut-port)" >&2
```

#### E2E test flow

`scripts/verify-credential-leak.sh` (already mandated by S-ETS-03-02; Sprint 4 S-ETS-04-03 strengthens with stub-IUT integration):

1. Launch `scripts/stub-iut.sh` in background (binds ephemeral port; writes `/tmp/stub-iut-port`).
2. Run `scripts/smoke-test.sh` against the stub IUT with `-DiutUri=http://127.0.0.1:$(cat /tmp/stub-iut-port)` and synthetic credential `-Dauth-credential="Bearer ABCDEFGH12345678WXYZ"`.
3. Smoke completes (TestNG suite executes; Core landing-page assertion fails because stub returns 401 — expected; the test goal is the masking pipeline, not the assertion outcome).
4. Grep for the literal credential substring `EFGH12345678WXYZ` in:
   - `target/testng-results.xml` (zero hits required)
   - The container's stdout log (zero hits required) — fetched via `docker logs <container_id> > /tmp/container-log.txt`
   - REST-Assured's request-emission stdout if separately captured (zero hits required)
5. Grep for the masked form `Bear***WXYZ` in the same logs/attachments — at least one hit required (proves the masking filter ran rather than silently dropping the header).
6. **Cross-check via stub-IUT echo**: parse the stub IUT's 401 response body (preserved in the smoke run's TestNG attachments). Assert that the `received_authorization` field contains the FULL UNMASKED credential `Bearer ABCDEFGH12345678WXYZ` — proving REST-Assured restored the original header before HTTP transmission per the Sprint 3 try/finally pattern (so the IUT receives the credential as the user intended).
7. Tear down stub IUT (`kill $stub_pid`); cleanup `/tmp/stub-iut-port`.

This three-fold cross-check (logs masked + stub received unmasked + masked form present in logs) is the strongest possible hermetic evidence for the credential-masking pipeline.

#### Composability with S-ETS-04-04 sabotage-script fixes

S-ETS-04-04 fixes the Sprint 3 sabotage-script bugs (per Pat's enumeration: stub-server kill on script abort, port-collision retry, jar-restoration `trap` ordering). The fixes apply to `scripts/verify-dependency-skip.sh` AND propagate to the new `scripts/verify-credential-leak.sh` AND `scripts/stub-iut.sh`:

- The `trap cleanup EXIT` block extends to kill the stub-IUT process AND remove `/tmp/stub-iut-port`.
- The ephemeral-port allocation pattern (Python `socket.bind(('', 0))`) is the same in both scripts.
- The `ops/test-results/` archival pattern carries over (stub IUT logs archived per Sprint 4 close).

S-ETS-04-04 SHOULD ship BEFORE S-ETS-04-03 so the credential-leak script inherits the fixed primitives; Pat's deferred_to_generator sequencing already reflects this.

#### Acceptance criterion (S-ETS-04-03)

The Sprint 4 contract's `success_criteria.credential_leak_e2e_test_green` is satisfied when:

- `scripts/verify-credential-leak.sh` exits zero.
- `ops/test-results/sprint-ets-04-credential-leak-evidence.txt` archives: (i) the synthetic credential used, (ii) the stub-IUT received-authorization echo (full unmasked), (iii) the grep results from logs/attachments (zero unmasked + at least one masked), (iv) the cross-check verdict.
- The script runs as a required local verification target. Hosted workflow activation is prohibited by ADR-012.

### Sprint 4 hardening: Subsystems conformance class scope (S-ETS-04-05)

> **Entire section superseded by Sprint 48.** Everything through the next
> top-level design heading records the historical Sprint 4 implementation only.
> The four-method table, cached setup, and parent-link assertion are not active
> design requirements or released ATS mappings. The five-procedure design under
> "Sprint 48: released Part 1 Subsystem direct procedures" is authoritative.

**Architect ratifies: Sprint-1-style minimal — 4 @Test methods at Sprint 4 close** (parallel to SystemFeatures Sprint 2 §"SystemFeatures conformance class scope" + Common Sprint 3 baseline). Full per-class expansion deferred to Sprint 5+ when sibling classes (Procedures, Sampling, Properties, Deployments) are batched.

Pat enumerated 5 SCENARIOs in REQ-ETS-PART1-003 (now SPECIFIED in spec.md). Architect maps these to 4 @Test methods + 1 testng.xml-level wiring concern (the dependency-skip SCENARIO is `<dependencies>` config, not a method):

| @Test method | Asserts | SCENARIO closed |
|---|---|---|
| `subsystemsResourcesEndpointReturnsCollection` | `GET /systems/{id}/subsystems` → status 200; body has array `items` (or equivalent — Generator MUST curl-verify GeoRobotix's actual shape FIRST per acceptance criterion #1); SKIP-with-reason if 404 (IUT does not implement Subsystems) | SCENARIO-ETS-PART1-003-SUBSYSTEMS-RESOURCES-001 (CRITICAL) |
| `subsystemCanonicalEndpointReturnsBaseShape` | for the first subsystem item: has string `id`, string `type`, array `links` per REQ-ETS-CORE-004 base shape | SCENARIO-ETS-PART1-003-SUBSYSTEMS-CANONICAL-001 (NORMAL) |
| `subsystemHasParentSystemLink` | subsystem item's `links` array contains an entry with `rel="system"` (or equivalent OGC-defined relation referencing the parent system); this is the **UNIQUE-TO-SUBSYSTEMS** assertion — the architectural invariant that distinguishes subsystems from sibling collection types | SCENARIO-ETS-PART1-003-SUBSYSTEMS-PARENT-LINK-001 (NORMAL) |
| `subsystemHasCanonicalLink` | subsystem item's `links` array contains `rel="canonical"` (absence of `rel="self"` is NOT FAIL — preserves v1.0 GH#3 fix policy from Core landing page) | SCENARIO-ETS-PART1-003-SUBSYSTEMS-CANONICAL-URL-001 (NORMAL) |

The `dependsOnGroups="systemfeatures"` wiring (SCENARIO-ETS-PART1-003-SUBSYSTEMS-DEPENDENCY-SKIP-001 — CRITICAL) is a **testng.xml change**, not a @Test method — handled per ADR-010 v2 amendment (defense-in-depth: `<group depends-on>` extension in testng.xml + `@BeforeSuite` SkipException fallback in `SubsystemsTests`).

#### Subpackage layout

`org.opengis.cite.ogcapiconnectedsystems10.conformance.subsystems.SubsystemsTests` — single class for Sprint 4. Mirrors the Sprint 2 SystemFeaturesTests pattern (1:1 class:conformance-class structure). If Sprint 5+ expansion grows the @Test count beyond ~10, split into `SubsystemsCollectionTests` + `SubsystemsItemTests` (deferred per the SystemFeatures-pattern precedent at design.md §437 line 439).

#### Fixtures and listeners

No new fixtures or listeners needed for Sprint 4. The existing `SuiteFixtureListener` supplies `iutUri`. Subsystems' `@BeforeClass` performs `GET /systems` ONCE to extract a sample system `id`, then `GET /systems/{id}/subsystems` ONCE to cache the response shape — pattern mirrors `SystemFeaturesTests.fetchSystemsCollection()`.

If the `@BeforeSuite` SkipException fallback (per ADR-010 v2 amendment) activates, SuiteFixtureListener may need a small extension to populate `core.failed` / `systemfeatures.failed` SuiteAttribute keys via `ITestListener.onTestFailure` — Generator implements ONLY IF runtime verification shows TestNG transitive cascade does not work without it.

#### Coverage scope rationale (Sprint-1-style narrowing — third extension)

Pat recommended Sprint-1-style minimal for risk control on the third pattern extension AND first two-level dependency chain. Architect concurs because:

1. **First two-level dependency chain compounds risk surface.** Sprint 4 introduces TWO new architectural firsts simultaneously: (i) the third conformance-class extension, (ii) the first multi-level group-dependency chain. Minimizing per-class @Test count concentrates Generator + gate verification effort on the dependency-cascade verification (the riskier of the two firsts).
2. **The 4 chosen SCENARIOs cover the foundational shape** AND the unique-to-Subsystems `parent-system-link` assertion. The remaining ~3-5 ATS items in OGC 23-001 Annex A `/conf/subsystem/` (canonical-url depth, location-time geometry, cross-system queries, write operations, advanced filtering interactions) layer on top — once the foundation + two-level cascade are proven, expansion is mechanical AND batches cleanly with sibling classes.
3. **Historical increment boundary.** Sprint 4 accepted a minimal subset so the
   dependency mechanism could be evaluated independently. ADR-013 supersedes
   any interpretation that class existence is enough for certification
   completeness: unreconciled Annex A tests keep the class partial.
4. **GeoRobotix's `/systems/{id}/subsystems` shape is unknown until Generator curls it** (acceptance criterion #1 mandates curl-first). 4 @Tests adapt cleanly to whatever GeoRobotix returns; 12-15 would force structural choices we'd regret OR force a SKIP-with-reason cascade that breaks the demonstration of the multi-level dependency mechanism.
5. **GEOROBOTIX-SUBSYSTEMS-SHAPE-MISMATCH risk** (Pat surfaced; medium severity). If GeoRobotix returns 404 on `/systems/{id}/subsystems`, the entire Subsystems class SKIP-with-reasons (acceptable Sprint 4 outcome — the testng.xml two-level dependency wiring is still verified via the sabotage exec, which doesn't require IUT 200s). 4 @Tests narrow the scope of "what to SKIP gracefully if IUT doesn't implement Subsystems".

Sprint 5+ expansion targets (mechanical extensions, batched with Procedures/Sampling/Properties/Deployments siblings):

- `subsystemCanonicalUrlReturns200` — REQ-ETS-PART1-003 / `/req/subsystem/canonical-url` deeper assertion
- `subsystemHasGeometryAndValidTime` (NORMAL — `MAY` priority) — `/req/subsystem/location-time` if present in OGC 23-001 Annex A
- `subsystemAppearsInCollections` — cross-system query (parent-system-link inverse direction)
- Plus ~2-3 more covering filter-by-property and filter-by-time interactions

Architect estimates Sprint 5 Subsystems-expansion-bundled-with-Procedures/Sampling at ~6-8 hours Generator time (mechanical extensions across 3-4 sibling classes sharing the SystemFeatures dependency baseline).

#### What NOT to ship in Sprint 4

- **Subsystems write operations** (POST / PUT / DELETE on `/systems/{id}/subsystems`): REQ-ETS-PART1-010 (`create-replace-delete`) scope; deferred to Sprint 6+ per epic-ets-02 placeholder repositioning.
- **Cross-system query depth**: `GET /systems?subsystem.id=X` filtering not in Sprint 4 scope; covered by REQ-ETS-PART1-009 (`advanced-filtering`) when that class lands.
- **Subdeployments coverage**: REQ-ETS-PART1-005 (`subdeployments`) is a related-but-distinct OGC 23-001 conformance class; deferred to Sprint 5+ batching.
- **Common conformance class expansion** (4 → 8 @Tests per Quinn cumulative CONCERN-2): per-Pat-Sprint-4-conformance-class-pick rationale, this is "by-design minimal-then-expand" — explicit deferral to Sprint 5+ when user prioritizes batching with sibling classes.

### Sprint 44: reproducible populated local OSH E2E

CP-004 and S-ETS-44-01 add a supplemental populated-IUT workflow without
changing the primary clean-smoke contract.

The workflow has four layers:

1. **External provenance check**: require a clean OSH checkout with no commits
   ahead of its configured upstream; record its commit and a deterministic
   installed-file manifest. The install is mounted read-only.
2. **Ephemeral IUT lifecycle**: derive a no-secret runtime configuration from
   `ops/local-osh-gate-config.json`, start a uniquely named OSH process on
   `field-hub_default`, use a Docker-assigned loopback host port for fixture
   application, and isolate all database/module state under a temporary
   directory.
3. **Supported-interface seeding**: a versioned manifest supplies exact static
   and dynamic payloads. The seeder hard-requires explicit dedicated mutable-IUT
   controls, rejects public/non-local targets, posts resources in dependency
   order, and records resource ids, response status/media type, parent schemas,
   associated Observation evidence, and request method counts.
4. **Two-verdict execution**: TeamEngine runs against the Docker-network IUT
   URL. The summary records `provisioningReady` independently from exact TestNG
   totals. TestNG failures remain non-zero. Cleanup removes the ephemeral IUT,
   verifies the primary container/state identity, and runs the authoritative
   clean primary smoke.

The populated target is acceptable E2E evidence when provisioning succeeds and
TeamEngine produces a real report. It is not a passing conformance target unless
the report itself contains zero failures. This distinction lets the project
increase exercised coverage while preserving IUT defects as evidence.

The discovery run against unmodified OSH 2.0.1 produced
`211 total / 86 passed / 28 failed / 97 skipped`. All failures came from missing
required DataStream/ControlStream metadata. The implementation SHALL NOT add a
response proxy, modify OSH, or relax Annex A.9 to change that result.

The completed implementation uses per-run container names and labels, exact
owned-container IDs, an ownership-evidence capability for the loopback seeder,
strict TestNG XML parsing, distinct conformance/infrastructure/overall verdicts,
an always-run finalizer, observable cleanup, and a normalized primary-container
and state fingerprint. It validates canonical OSH upstream/source/build
metadata, an installed-file manifest, and a digest-pinned runtime image.

The final fresh-clone run provisioned all seven resource families, then returned
populated TestNG `211/91/28/92` with conformance `FAIL`. Cleanup passed, primary
state was unchanged, and clean-primary TestNG passed `211/69/0/142`. This closes
the reproducible populated-IUT workflow, not the wider positive binding
conformance requirement.

### Sprint 45: released ATS inventory and coverage gate

ADR-013 and CP-005 separate three inputs that were previously conflated:

1. the approved 23-001/23-002 Annex A suites, which define certification
   coverage;
2. the later OpenAPI repository pin, which supplies API/schema input for its
   documented purpose;
3. IUT declarations and the frozen web application, which provide
   interoperability evidence but cannot redefine the ATS.

The inventory extractor reads the exact `v1.0.0` release source commit and emits
a deterministic semantic manifest. The manifest keys every test by standard
part plus identifier, retains target-less supporting tests, and records class
ordering and target requirements/recommendations. Semantic hashes avoid binding
the gate to generated HTML element IDs while official PDF hashes retain
document provenance.

Coverage auditing operates on compiled TestNG annotations because Java source
descriptions are commonly assembled from constants. Matching an annotation
description to a target URI yields only a candidate. A separate reviewed
mapping is required before the inventory can call an ATS test implemented.
Target-less supporting tests map to named reusable helper methods.

The gate fails closed for released-source drift, duplicate or missing entries,
unknown classes/methods, a mapping whose annotation omits the canonical target,
or an implemented status without an exact reviewed mapping. Method identities
include parameter signatures. Discovery walks inherited methods, ignores
method-level disabled tests, and rejects suite filters, factories, class-level
TestNG tests, and other execution features until the audit models them exactly.
Supporting mappings must resolve to the explicit approved-helper registry.
Summary counts are derived from per-test entries. Consequently, a successful
Maven or TeamEngine run proves regression health but cannot promote an
incomplete conformance class. Historical story completion remains chronology;
every released class stays partial until every owning test is exact and its
supporting helpers are reviewed.

### Sprint 46: released Part 1 API Common direct procedures

The historical `CommonTests` class remains responsible for selected inherited
OGC API Common behavior. It is not renamed or reused as a Connected Systems
mapping because released OGC 23-001 defines a distinct `/conf/api-common`
class.

Sprint 46 adds:

```text
core -----------+
                +--> part1apicommon --> systemfeatures --> existing descendants
common ---------+

Part1ApiCommonTests
  |-- resourceIdsAreUniqueWithinEachType()
  |-- resourceUidsAreValidAndGloballyUnique()
  |-- resourceUidTypesFollowRecommendation()
  `-- datetimeUsesValidTime()
          |
          v
Part1ApiCommonSupport
  |-- canonicalResources(apiRoot, resourceType)
  `-- collectionItems(apiRoot, collection)
```

`Part1ApiCommonSupport` performs read-only JSON retrieval with a finite page
limit and visited-URI set. Canonical requests negotiate the released
representation set for each resource type: GeoJSON, SensorML JSON, or the JSON
extension where applicable. Each page must return HTTP 200 and the collection
member required by its actual media type: GeoJSON `features`, SensorML JSON
`items`, or either recognized wrapper for an extension JSON type. A malformed,
cyclic, cross-origin, or over-limit traversal fails closed. Canonical-resource
support probes the five resource types fixed by OGC 23-001; HTTP 404 means that
resource type is not supported by the IUT, while other non-200 responses fail.
UID extraction gives the normative SensorML `uniqueId` first priority, then
GeoJSON `properties.uid`, and finally a direct `uid` extension fallback.

The API Common `@BeforeClass` configuration explicitly depends on the `core`
and `common` groups in addition to the suite's group dependency. TestNG orders
the configuration after those groups but does not reliably suppress a
configuration method when only part of a prerequisite group fails. The setup
therefore inspects the completed failed and skipped test results for both
prerequisite groups and throws `SkipException` before reading the IUT suite
attribute or issuing a request. The failure-path gate requires the
configuration itself, all four API Common tests, and System Features to report
SKIP after Core sabotage. The sabotage run writes to a unique smoke output
directory and accepts exactly one XML report newer than a marker created
immediately before smoke starts, preventing pre-existing evidence from
satisfying a no-report run.

The date-time method reads collection metadata from `/collections`. Collections
without a usable temporal extent do not produce positive evidence. For each
eligible collection, the test derives instant, bounded, open-start, and
open-end query forms from the extent. It retrieves unfiltered items once and
filtered items for every form, rejects returned `validTime` values that do not
intersect the query, resolves a `now` bound using the timestamp captured
immediately before that request, and compares feature IDs to prove timeless
features were retained in every filtered result. The method skips only when no
query can execute against an eligible collection.

UID recommendation evaluation uses a deterministic JSON snapshot of the IANA
Formal and Informal URN Namespaces registries. The resource records the source
URL, registry update date, retrieval date, and source XML SHA-256; runtime
initialization checks those metadata and both expected namespace counts before
using the snapshot. Valid URI forms outside UUID URNs and that snapshot remain
warnings, because the released procedure expresses this check as a
recommendation.

These six reviewed mappings cover only the four directly owned class tests and
two supporting tests in the OGC 23-001 inventory. The released class also
inherits five external OGC API Features/Common conformance classes. Those
external suites remain partial, so Sprint 46 does not claim full
`/conf/api-common` conformance.

The official `ets-ogcapi-features10` implementation was inspected at commit
`a314c1e6a9278b14ab9a2ed865cfe36d202f0125` for the referenced temporal-extent
selection model. Its `fc-time-response` method still contains a response
assertion TODO, so this ETS implements the normative inclusion checks directly
instead of importing an incomplete implementation.

### Sprint 47: released Part 1 System direct procedures

Sprint 47 keeps the existing `systemfeatures` group and replaces its six
historical methods with one method for each released `/conf/system` test.

```text
Part1ApiCommonSupport
  |-- canonicalResourcesDetailed()
  `-- collectionItemsDetailed()
          |
          v
SystemFeaturesTests
  |-- systemLocationsFollowRecommendation()
  |-- mobileSystemLocationIsUpdated()
  |-- everySystemHasCanonicalUrl()
  |-- systemResourcesEndpointIsValid()
  |-- canonicalSystemsEndpointIsValid()
  `-- systemCollectionsAreValid()
          |
          v
SystemFeaturesSupport
  representation extraction + canonical normalization
  + endpoint-parameterized validation + fail-closed bundled schemas
```

The detailed API Common results preserve the existing reviewed helper
signatures while exposing immutable page documents containing source URI,
actual response media type, parsed body, and page items. Descendants therefore
validate the exact traversed response without duplicate HTTP requests.

System media behavior is closed over the two released representations.
GeoJSON location/type use `geometry` and `properties.featureType`; SensorML JSON
uses `position` and `definition`. GeoJSON pages validate against
`connected-systems-1/geojson/systemCollection.json`; SensorML pages validate
against `connected-systems-1/sensorml/systemCollection.json`. Unsupported media
warns and is accumulated as unsupported evidence. Collection loops continue,
and SKIP occurs only when no supported collection was executed. Every
`geojson.org` reference resolves to a pinned complete schema; permissive
resolution stubs are not accepted.

The resources-endpoint procedure is endpoint-parameterized and owns HTTP 200,
actual-media selection, and every-page schema validation. The canonical
endpoint invokes it for `{api_root}/systems`; collection procedures invoke it
for their traversed item pages. Test-class setup loads only run arguments so a
network failure in one released procedure does not suppress unrelated results.

Location-time has a real external prerequisite: a known moving System. The
optional `mobile-system-id` TeamEngine argument identifies that resource. The
test polls the canonical endpoint once per second for at most 30 seconds and
passes only after observing changed GeoJSON geometry or SensorML positional
coordinates. SensorML orientation and reference-frame metadata are excluded
from the movement comparison. Missing input yields immediate SKIP. This does
not modify or seed OSH and does not reinterpret a static local fixture as
mobile evidence.

The local OSH target does not advertise collection temporal extents, so API
Common datetime reports its specified no-positive-evidence SKIP. Raw TestNG
group dependency semantics would propagate that evidence limitation to every
System method even though their HTTP prerequisites are independently
available. Sprint 47 therefore keeps the declared
`part1apicommon -> systemfeatures` ordering but makes System tests
`alwaysRun`. System setup inspects completed prerequisite results before any
System IUT access: any Core/Common/API Common failure, configuration failure,
or API Common skip other than the exact datetime evidence limitation produces
SKIP; the datetime-only limitation is logged and the six direct procedures
execute. The inherited SKIP remains in the report, so this does not create a
full `/conf/system` conformance claim.

Focused HTTP coverage executes the same six deployed methods against a
controlled server that records endpoint exchanges, returns complete
schema-valid System collections and canonical resources, and changes only the
mobile System's positional coordinates. This supplements, but does not replace,
TeamEngine execution against the unmodified local OSH.

### Sprint 48: released Part 1 Subsystem direct procedures

Sprint 48 retains the `subsystems` TestNG group but replaces its four
historical shape/link methods with the five released Annex A procedures.

```text
SubsystemsTests
  |-- subsystemCollectionIsValid()
  |-- recursiveParameterUsesBooleanValues()
  |-- systemsRecursiveSearchIsComplete()
  |-- subsystemsRecursiveSearchIsComplete()
  `-- nestedAssociationsAreIncluded()
          |
          v
SubsystemsSupport
  bounded page traversal + direct-edge hierarchy discovery
  + cycle/duplicate rejection + recursive set assertions
  + association-resource closure
          |
          +--> Part1ApiCommonSupport traversal model
          `--> SystemFeaturesSupport endpoint schema validation
```

Hierarchy expectations are never derived from the `recursive=true` response
under test. The support layer starts with top-level Systems, follows each
default direct-subsystems endpoint, and recursively records direct edges.
Traversal has fixed page and node limits, rejects repeated page URLs, rejects
cycles in the parent-child graph, rejects a shortcut edge when a reported direct
child is also reachable through a path of length two or more, and requires
unique non-empty resource IDs. It retains direct children separately from
transitive descendants so default, false, and true response sets can be
evaluated without conflating levels. Cycle detection is iterative so the
10,000-node bound cannot exhaust the Java call stack first.

The collection procedure probes bounded nested collections to identify a parent
with actual children without requiring returned collection items to expose
local IDs. It then retrieves the parent resource and resolves exactly one
`rel=subsystems` link. The resolved target must exactly equal the already
probed `{api_root}/systems/{sysId}/subsystems` endpoint;
duplicate-identical links, trailing slashes, queries, fragments, and
cross-origin variants fail. Every traversal page checks HTTP status and actual
Content-Type before representation parsing. The accepted first response is
reused rather than fetched again. Supported responses use bounded
representation traversal, and `SystemFeaturesSupport` validates each returned
page according to its actual GeoJSON or SensorML JSON media type. Unsupported
or absent media warns and SKIPs without attempting JSON parsing, including on
later pages. First-response SensorML collections do not need a preliminary
GeoJSON response or an `id` member merely to execute this collection procedure.
Recursive hierarchy discovery applies the same per-page media gate to the root
`/systems` traversal and every nested subsystem traversal before its separate
local-ID extraction.

Recursive-parameter validation issues exact `recursive=false` and
`recursive=true` requests, checks the effective request query, and requires only
HTTP success; it does not parse response representations outside that released
procedure. Recursive Systems and Subsystems checks require at least one
independently discovered descendant, and nested-Subsystem checks require at
least one transitive descendant. Missing hierarchy evidence is SKIP, not PASS.

Association closure is evaluated for every discovered parent and for each
implemented Sampling Feature, DataStream, and ControlStream resource type.
Implementation evidence comes from the corresponding canonical top-level
endpoint before any nested endpoint is evaluated. A top-level HTTP 200 means
every applicable parent and descendant endpoint must return HTTP 200; nested
404 responses cannot redefine the type as unsupported. The parent endpoint must
include every ID observed through descendant endpoints. If no descendant
association resources exist, the procedure SKIPs rather than claiming
conformance from empty-set containment.

Subsystem setup loads only the API root and inspects completed prerequisites.
It allows direct execution past only the exact inherited no-evidence outcomes
already documented by API Common and System. Any failure, configuration
failure, or unexpected inherited SKIP blocks all Subsystem IUT access. The
allowed inherited SKIPs remain visible, so direct procedure execution does not
become a full inherited-conformance claim.

The unmodified local OSH target currently serves the root System collection as
unsupported `application/json` for these released hierarchy procedures.
TeamEngine must still execute all five methods: the recursive boolean request
procedure can pass, while hierarchy-dependent procedures SKIP at the root media
gate. A controlled multi-level HTTP fixture supplies positive GeoJSON/SensorML
collection and nested-association evidence for all five deployed paths.

### Sprint 49: released Part 1 Deployment direct procedures

Sprint 49 replaces the historical eager four-method Deployment approximation
with the five released Annex A procedures.

```text
DeploymentsTests
  |-- everyDeploymentHasCanonicalUrl()
  |-- deploymentResourcesEndpointIsValid()
  |-- canonicalDeploymentsEndpointIsValid()
  |-- deploymentCollectionsAreValid()
  `-- deploymentsReferencedFromSystemsAreValid()
          |
          v
DeploymentFeaturesSupport
  collection selection + canonical equivalence
  + Deployment schema dispatch + exact System-link matching
          |
          +--> Part1ApiCommonSupport bounded traversal
          `--> future ConnectedSystemsSensorMlValidatorAdapter
```

Class setup normalizes only the API root after checking Part 1 API Common
results. Every procedure retrieves its own evidence and uses `alwaysRun`
without method dependencies. The group dependency becomes
`part1apicommon -> deployments`; a completed System ATS is not an inherited
prerequisite for all Deployment procedures. The `ref-from-system` method
retrieves Systems exactly as its released procedure specifies.

Both collection procedures enumerate every exact
`featureType=sosa:Deployment` collection and require at least one. The
collections procedure also requires `itemType=feature`. No selected collection
can pass vacuously or be hidden by another supported collection. API Common
resolves the advertised items endpoint and bounds same-origin pagination. Its
restricted overload selects from the intersection of advertised item-link
media and Deployment-supported GeoJSON/SensorML media, so an earlier generic
JSON link cannot cause a false SKIP. Schema-controlled paths add an
actual-media gate on every page before parsing.

Canonical comparison requires at least one canonical relation. Every canonical
occurrence must resolve to the IUT origin and path
`{api_root}/deployments/{id}`; representation query variants and duplicate
occurrences are allowed by the released procedure. The first occurrence in
document order is dereferenced deterministically with HTTP 200. Jackson trees
are compared after removing all canonical relation links from both top-level
`links` arrays. Query parameters used for representation selection are
retained; fragments and path aliases fail.

Resources endpoint validation dispatches actual `application/geo+json` and
`application/sml+json` pages to the bundled released Deployment collection
schemas. This support class is intentionally the replacement seam described by
`REQ-ETS-VALIDATOR-001`: Connected Systems protocol and mapping behavior remain
local, while a future reusable FCU/OGC SensorML validator will replace only the
SensorML schema backend. The SWE Common component adapter is not misapplied to a
complete SensorML Deployment document, and the ETS suite jar
`ets-sensorml30` is not imported.

System-reference validation retrieves every canonical System, requires each
normative nested endpoint, follows all pages, validates each page by actual
media, and accepts only explicit GeoJSON `deployedSystems@link` or SensorML
`deployedSystems[].system` hrefs resolving to the exact owning System path.
Unrelated links and substring ID matches fail.

The unmodified local OSH baseline is expected to produce real conformance
failures: no `sosa:Deployment` collection, unsupported generic media from
`/deployments`, and HTTP 400 from `/systems/040g/deployments`. Mandatory
TeamEngine E2E preserves those outcomes. Controlled read-only HTTP coverage
provides the positive oracle for all five deployed procedures.

The defensive `@BeforeClass` result scan is narrower than the shared TestNG
context: only Core, Common, and Part 1 API Common test/configuration results are
eligible blockers. SystemFeatures and sibling configuration results are ignored
because they are not inherited by `/conf/deployment`. Credential wire gates
select exactly one TestNG XML and container log produced after a per-run marker
from `SMOKE_OUTPUT_DIR`; stale worktree artifacts cannot satisfy the gate.

### Sprint 50: released Part 1 Procedure direct procedures

Sprint 50 replaces the historical eager four-method Procedure approximation
with the five released Annex A procedures.

```text
ProceduresTests
  |-- procedureLocationIsAbsent()
  |-- everyProcedureHasCanonicalUrl()
  |-- procedureResourcesEndpointIsValid()
  |-- canonicalProceduresEndpointIsValid()
  `-- procedureCollectionsAreValid()
          |
          v
ProcedureFeaturesSupport
  location + collection metadata + Procedure types
  + canonical equivalence + Procedure schema dispatch
          |
          +--> Part1ApiCommonSupport bounded traversal
          `--> future ConnectedSystemsSensorMlValidatorAdapter
```

Class setup normalizes only the API root after checking Core, Common, and Part 1
API Common results. Every procedure retrieves its own evidence, uses
`alwaysRun`, and has no method dependency. The group dependency becomes
`part1apicommon -> procedures`; completed System ATS outcomes are not inherited
by the released Procedure class.

The location procedure traverses `{api_root}/procedures` directly with a
GeoJSON/SensorML Accept value and a strict actual-media gate on every page. It
checks every GeoJSON `geometry` value for JSON null and rejects any SensorML
item containing `position`. Unsupported or missing actual media logs the
released requirement and SKIPs before parsing.

Canonical URL and collections procedures enumerate every exact
`featureType=sosa:Procedure` collection and require at least one. The
collections procedure also requires `itemType=feature`. API Common resolves
advertised items endpoints and bounds same-origin pagination. Schema-controlled
collection retrieval uses the restricted media overload so advertised GeoJSON
or SensorML is selected ahead of an earlier generic JSON link.

Canonical comparison requires at least one canonical relation. Every canonical
occurrence must resolve to the IUT origin and path
`{api_root}/procedures/{id}`; representation query variants and duplicate
occurrences are allowed only for that exact identity. After all targets are
validated, the first occurrence with no advertised media type or one matching
the collection page media type is dereferenced. No comparable occurrence
produces a warning and SKIP. Jackson trees are compared after removing all
canonical relation links from both top-level `links` arrays; a `links` member
emptied by that removal is dropped so it equals an omitted optional member.

Resources endpoint validation dispatches actual `application/geo+json` and
`application/sml+json` pages to the bundled released Procedure collection
schemas. The canonical endpoint invokes the same behavior at
`{api_root}/procedures`.

The collections procedure extracts Procedure type from
`properties.featureType` for GeoJSON and `definition` for SensorML. The value
must match one of the nine URI/CURIE pairs in OGC 23-001 Clause 12. Every page
also passes representation-specific Procedure collection schema validation.

`ProcedureFeaturesSupport` is the validator replacement boundary from
`REQ-ETS-VALIDATOR-001`. Connected Systems protocol and mapping behavior remain
local. A future reusable FCU/OGC SensorML validator can replace SensorML schema
semantics behind this boundary; the suite jar `ets-sensorml30` is not imported.

The unmodified local OSH baseline returns generic `application/json` from
`/procedures` and advertises no `sosa:Procedure` collection. Mandatory
TeamEngine E2E must execute all five methods and preserve the resulting
unsupported-media SKIPs and missing-collection FAILs. Controlled read-only HTTP
coverage provides positive GeoJSON/SensorML evidence for all successful paths
and adversarial location, type, media, canonical, and collection cases.

Sprint 50 verification confirms this design. Reviewed coverage is `5/5 exact`;
final focused Maven is `116/0/0/0`, full Maven is `451/0/0/3`, and the exact
image is
`sha256:6e1beeb598ab4c734f2e2d30e0ecb70d3270af9f9f2d5a1029d1b74259b54d98`.
Local OSH TeamEngine is honestly `218/39/5/174`: Procedure setup passes, the
three direct endpoint methods SKIP on unsupported `application/json`, and both
collection-dependent methods FAIL because no exact `sosa:Procedure`
collection is advertised. API Common sabotage proves setup plus all five
methods SKIP before Procedure access. Runtime, credential, and artifact
hygiene gates pass without modifying OSH or TeamEngine. Initial adversarial
review exposed unsupported-first canonical selection, canonical-only versus
omitted `links` normalization, and stale dependency-comment gaps. Media-aware
selection, optional-member normalization, and focused regressions now close
those gaps. Focused Raze recheck passes at confidence `0.99`, closes all three
findings, and reports no required fixes.

### Sprint 51: released Part 1 Subdeployment graph procedures

Sprint 51 replaces the historical four-method Subdeployment approximation with
the five released Annex A procedures.

```text
SubdeploymentsTests
  |-- subdeploymentCollectionIsValid()
  |-- recursiveParameterUsesBooleanValues()
  |-- deploymentsRecursiveSearchIsComplete()
  |-- subdeploymentsRecursiveSearchIsComplete()
  `-- recursiveAssociationsIncludeDescendants()
          |
          v
SubdeploymentsSupport
  fail-closed Deployment graph + exact link identity
  + recursive set equality + association closure
          |
          +--> Part1ApiCommonSupport bounded traversal
          `--> DeploymentFeaturesSupport schema boundary
```

Class setup normalizes only the API root after checking Core, Common, Part 1
API Common, and Deployment outcomes. Every procedure retrieves its own
prerequisites, uses `alwaysRun`, and has no method dependency. The released
group chain remains
`Core/Common -> Part 1 API Common -> Deployment -> Subdeployment`; unrelated
siblings cannot block Subdeployment.

`SubdeploymentsSupport` discovers the Deployment graph independently from the
top-level `/deployments` endpoint and every direct
`/deployments/{id}/subdeployments` endpoint. Traversal is same-origin and
bounded. Each page must establish status and actual GeoJSON or SensorML media
before parsing and must pass the released Deployment collection schema.
Duplicate IDs, cycles, shortcut edges, and safety overflow fail before the
graph can be used as conformance evidence.

The collection procedure examines every parent proven to have direct children.
It retrieves the canonical parent, requires every `rel=subdeployments`
occurrence to identify the normalized same-origin parent path without query or
fragment, including equivalent default-port and unreserved percent-encoding
forms. It dereferences a valid occurrence and schema-validates every returned
page. No parent with children warns and SKIPs.

The recursive-parameter procedure is intentionally status-only. It sends the
exact boolean values `false` and `true` and requires HTTP 200 without imposing
representation parsing on a requirement that specifies parameter support.

Recursive root search compares exact sets: default and false equal graph roots,
while true equals all graph nodes. Recursive child search compares default and
false with direct children and true with all transitive descendants. The child
procedure warns and SKIPs when the IUT has no transitive hierarchy.

Recursive association validation evaluates `deployedSystems`,
`featuresOfInterest`, `samplingFeatures`, `datastreams`, and `controlstreams`.
For each advertised parent relation, immutable run-argument fixture evidence
independently identifies resources owned directly by that parent and by every
descendant. The procedure examines every link occurrence, ignores unsafe or
unsupported earlier candidates, and selects a same-origin JSON-compatible or
untyped negotiable occurrence. The parent endpoint's ID set must include the
complete parent-plus-descendant fixture union. Missing ownership evidence or
no safe comparable occurrence warns and SKIPs; observed omissions fail. Child
association responses are never used to manufacture the expected oracle.

Deployment collection schemas remain behind the existing
`DeploymentFeaturesSupport` validator boundary. A future reusable SensorML
library may replace SensorML schema semantics there. The executable
`ets-sensorml30` suite jar is not imported because it owns a separate
TeamEngine/TestNG lifecycle rather than a reusable validation API.

The unmodified local OSH baseline has one root Deployment, no children, no
`rel=subdeployments` link, generic `application/json` Deployment collections,
and genuine failures in the inherited Deployment group. Mandatory TeamEngine
E2E therefore preserves five dependency SKIPs before Subdeployment access.
Controlled read-only HTTP coverage is the positive oracle for all five
successful paths and the media, graph, link, recursive-set, and association
fail-closed branches. OSH and TeamEngine source and binaries remain unchanged.

Implementation reconciliation confirms all five methods are deployed and
reviewed exact. Corrected focused Maven is `131/0/0/0`; full Maven is
`480/0/0/3`. Exact image `sha256:e88aa5f9...b1dca` passes immutable TeamEngine
and embedded-validator checks. Primary local OSH is honestly `219/39/5/175`.
A programmatic TestNG baseline/sabotage pair proves causality: all five methods
reach the IUT while the synthetic Deployment prerequisite passes, then setup
and all five methods SKIP before IUT access when that single prerequisite is
changed to fail. The earlier direct local-OSH sabotage is retained only as
historical non-causal evidence. Corrected controlled HTTP, credential,
ATS-source, and zero-write/zero-leak hygiene gates pass. Focused adversarial
recheck closed the initial four findings and exposed repository-root TestNG
output plus stale records. The records are reconciled, and programmatic TestNG
now uses JUnit-managed temporary directories; focused and full Maven leave no
`test-output/`. Final Raze returns `APPROVE_WITH_CONCERNS`, confidence `0.99`,
with all six findings closed and no required fixes.

### Sprint 52: released Part 1 Sampling Features procedures

Sprint 52 replaces the historical four-method Sampling Features approximation
with all five released Annex A procedures.

```text
SamplingFeaturesTests
  |-- everySamplingFeatureHasCanonicalUrl()
  |-- samplingFeaturesResourcesEndpointIsValid()
  |-- canonicalSamplingFeaturesEndpointIsValid()
  |-- samplingFeatureCollectionsAreValid()
  `-- samplingFeaturesAreAvailableFromEverySystem()
          |
          v
SamplingFeaturesSupport
  exact collection metadata + canonical identity/equivalence
  + GeoJSON Sampling Feature schema boundary
          |
          `--> Part1ApiCommonSupport bounded traversal
```

Class setup normalizes only the API root after checking Core, Common, Part 1
API Common, and System outcomes. Every procedure retrieves its own evidence,
uses `alwaysRun`, and has no method dependency. The released group chain is
`Core/Common -> Part 1 API Common -> System -> Sampling Features`; unrelated
siblings cannot block Sampling Features.

The defensive prerequisite gate fails closed for inherited failures,
configuration failures, and unexpected SKIPs. It permits only method-specific,
reason-shape documented
no-evidence SKIPs already established by the System boundary: API Common
datetime without an eligible temporal collection, absent optional
`mobile-system-id`, and unsupported actual media at the two System endpoint
schema procedures. Those limitations remain logged and keep inherited
conformance incomplete while the independent Sampling Features procedures
collect their own evidence.

Resources and canonical endpoint procedures independently traverse
`/samplingFeatures`, require HTTP 200, gate actual media before parsing, and
validate every supported GeoJSON page against the bundled released Sampling
Feature collection schema. Generic `application/json` and other unsupported
representations warn and SKIP. The canonical endpoint does not reuse another
TestNG result; it independently executes the parameterized procedure.

Collection discovery requests `/collections`, selects every exact
`featureType=sosa:Sample` entry, requires `itemType=feature` and a non-empty ID,
and retrieves each items endpoint through the reviewed API Common helper.
Every supported page is GeoJSON-schema validated. No matching collection
fails. If any matching collection has no supported items representation, all
inspectable collections are still processed before the procedure SKIPs instead
of creating a partial-evidence PASS.

Canonical URL validation processes every item from every matching collection
whose JSON representation is supported. Every canonical occurrence must
resolve on the IUT origin to the encoded
`/samplingFeatures/{id}` resource identity. A representation comparable with
the collection page is dereferenced, must return HTTP 200 with matching actual
media, and must equal the collection item after canonical links are removed
from both JSON documents. An empty `links` member after removal is normalized
to omission. No matching collections or any unsupported selected collection
SKIPs after all inspectable evidence is processed; observed missing, unsafe,
wrong-target, or different canonical content fails.

System reference validation obtains every canonical System through the
reviewed API Common helper, then independently traverses
`/systems/{sysId}/samplingFeatures` for every ID. Every nested page must return
HTTP 200, use supported JSON media, and follow bounded same-origin pagination.
Every nested GeoJSON page also passes the released Sampling Feature collection
schema. Expected unsupported-media evidence is retained per System while later
Systems continue; one valid or unsupported System cannot hide a later failure.

Canonical collection traversal, per-item canonical dereference, and nested
System traversal catch only expected `SkipException` evidence limitations at
their narrow independent boundaries. They continue later collections, items,
and Systems, then throw one aggregate SKIP after all inspectable evidence has
been processed. Assertions, non-200 responses, unsafe pagination, invalid
schema or metadata, and canonical identity or content defects escape
immediately as failures.

Sampling Features have only a GeoJSON representation in released OGC 23-001.
The schema remains behind the ETS-owned `SamplingFeaturesSupport` boundary; no
SensorML suite jar or SensorML library enters this path. Primary TeamEngine E2E
uses unmodified local OSH. Its generic JSON media and
`featureType=featureOfInterest` collection metadata remain visible as honest
SKIP/FAIL evidence. Controlled read-only HTTP coverage supplies the positive
oracle for all five methods. OSH and TeamEngine source and binaries remain
unchanged.

Initial Raze returned `GAPS_FOUND` at confidence `0.98`. Its two required
findings identified omitted conditional GeoJSON validation in nested System
pages and early expected media SKIPs that could hide later defects.
`Part1ApiCommonSupport` now exposes reviewed page-observer overloads. The
observer runs after safe parsing of each supported page and before pagination
advances, so an earlier supported-page defect cannot be erased by a later
evidence SKIP. Sampling Features endpoint, collection, canonical, and nested
System procedures use that ordering.

Remediated implementation verification closes all non-adversarial gates. Reviewed
coverage is `5/5 exact` for `/conf/sf` and
`35 exact / 2 helper / 133 candidate / 70 unmapped` overall. Test-first
evidence includes the expected initial compile failure, the real prerequisite
execution gap at `6/1/0/0`, and a partial-collection false-PASS regression at
`1/1/0/0`. Raze gap-fix test-first runs fail `13/5/0/0` and `16/3/0/0`.
Final focused Maven is `49/0/0/0`; full Docker Maven is
`506/0/0/3`. Exact image
`sha256:ae3a7b6b17d98c328ca7dff95afa05fbfecf6a2f1ebe313a75c7429ae2580ff3`
passes TeamEngine runtime, immutable-base, dependency, deployed SWE Common
adapter, and confidential-context verification.

Primary unmodified local OSH TeamEngine is honestly `220/40/6/174`. The five
Sampling Features methods all execute: System-reference passes, collections
fails because no collection advertises exact `sosa:Sample`, and the canonical
URL plus two endpoint-schema procedures SKIP for missing collection or
unsupported actual-media evidence. System sabotage makes all five procedures
SKIP before Sampling Features IUT access. Artifact hygiene records 117
recognized IUT requests, zero writes, and zero credential leaks. Credential
integration and wire E2E pass with zero unmasked artifact hits, 36 masked
events, and 36 intact synthetic transmissions. Focused adversarial recheck
returns `APPROVE` at confidence `0.99`; both findings are closed, with no new
findings and no required fixes.

### Sprint 53: released Part 1 Property Definitions procedures

Sprint 53 replaces the historical four-method Property Definitions
approximation with the four released Annex A procedures.

```text
PropertyDefinitionsTests
  |-- everyPropertyHasCanonicalUrl()
  |-- propertyResourcesEndpointIsValid()
  |-- canonicalPropertiesEndpointIsValid()
  `-- propertyCollectionsAreValid()
          |
          v
PropertyDefinitionsSupport
  exact sosa:Property collection selection
  + SensorML Property collection schema adapter
  + canonical identity/equivalence
          |
          `--> Part1ApiCommonSupport bounded traversal
```

Class setup normalizes only the API root after checking Core, Common, and Part
1 API Common outcomes. Every procedure retrieves its own evidence, uses
`alwaysRun`, and has no method dependency. The released group chain is
`Core/Common -> Part 1 API Common -> Property Definitions`; System and
unrelated siblings cannot block Property Definitions.

Resources and canonical endpoint procedures independently traverse their
parameterized endpoint or `/properties`, require HTTP 200, gate actual media
before parsing, and validate every supported `application/sml+json` page
against the bundled released Property collection schema. Generic
`application/json` and other unsupported representations warn and SKIP. The
canonical endpoint does not reuse another TestNG result; it independently
executes the parameterized procedure.

The Annex source uses `{sensorml-mediatype}` without defining that token. The
release does define `{sensorml-json-mediatype}` as `application/sml+json`;
Sprint 53 records and tests that as the only supported Property media
interpretation.

Collection discovery requests `/collections`, selects every exact
`itemType=sosa:Property` entry, requires at least one and a non-empty ID, and
retrieves each items endpoint through the reviewed API Common helper. Every
supported page is SensorML-schema validated. If a selected collection lacks a
supported representation, later collections remain inspectable and an
otherwise clean incomplete run ends in aggregate SKIP.

Canonical URL validation processes every item from every matching collection
whose SensorML representation is supported. Every canonical occurrence must
resolve on the IUT origin below `/properties/` with exactly one non-empty local
ID segment. A comparable representation is dereferenced, must return HTTP 200,
and must equal the collection item after canonical links are removed from both
JSON documents. An empty `links` member after removal is normalized to
omission. Missing collections, empty item evidence, unsupported selected
collections, or no comparable occurrence SKIP only after all later inspectable
evidence has been processed. Missing, unsafe, wrong-target, or different
canonical content fails.

Canonical collection traversal and per-item canonical dereference catch only
expected `SkipException` evidence limitations at narrow independent
boundaries. Assertions, non-200 responses, unsafe pagination, invalid schema
or metadata, and canonical identity or content defects escape immediately as
failures. An aggregate SKIP is emitted only after no later failure is found.

`PropertyDefinitionsSupport` is the ETS-owned replaceable SensorML validator
adapter. Current implementation uses the bundled released
`propertyCollection.json` graph. FCU-GIS-Luke's future reusable SensorML
library can replace the adapter internals after source and diagnostic parity
review; TestNG procedures remain unchanged. The executable
`ets-sensorml30` suite jar is not imported as a library.

The bundled Property schemas are resolver-normalized: `$id` values support
local resolution and relative release `$ref` values are rewritten to
equivalent absolute local URIs. A pinned-release parity gate normalizes those
resolver-only differences and compares all three Property entry schemas plus
their transitive reference graph before exact status is assigned. This adapter
reuse implements schema steps inside `/conf/property`; the reviewed mapping
inventory must not assign it to the distinct
`/conf/sensorml/property-schema` procedure.

Primary TeamEngine E2E uses unmodified local OSH. Its `/properties` endpoint
returns HTTP 200 generic `application/json` with an empty `items` array, and
`/collections` advertises no `sosa:Property` collection. These remain honest
endpoint/canonical evidence SKIPs and a collections failure. Controlled
read-only HTTP coverage supplies the positive oracle for all four methods. OSH
and TeamEngine source and binaries remain unchanged; hosted CI remains out of
scope.

Implementation conforms to this design. All four procedures have reviewed
exact mappings; focused Maven is `95/0/0/0`, full Maven is `525/0/0/3`, and
coverage is `240/39 exact/2 helper/130 candidate/69 unmapped`.
Resolver-normalized parity covers three entry schemas and 53 transitive
schemas without mismatch. The exact image passes TeamEngine and validator
runtime checks. Unmodified local OSH TeamEngine is honestly `220/40/7/173`,
with one Property collections failure and three evidence SKIPs. Dependency,
credential, controlled-HTTP, and zero-write/zero-leak hygiene gates pass.
No exact mapping is assigned to `/conf/sensorml/property-schema`. Raze returns
`APPROVE_WITH_CONCERNS` at confidence `0.98`, with no required fixes.

### Sprint 54: released Part 1 GeoJSON procedures

Sprint 54 replaces the historical thirteen-method GeoJSON approximation with
the twelve released Annex A procedures.

```text
GeoJsonTests (12 independent procedures)
          |
          v
GeoJsonSupport
  OpenAPI JSON/YAML operation inspection
  + canonical resource selection and media gates
  + released single/collection schema dispatch
  + common/resource mapping inspection
  + resource-specific relation tables
          |
          `--> Part1ApiCommonSupport bounded traversal
```

Class setup normalizes only the API root after checking Core, Common, and Part
1 API Common outcomes. Every procedure retrieves its own evidence, uses
`alwaysRun`, and has no method dependency. The released group chain is
`Core/Common -> Part 1 API Common -> GeoJSON`; System and unrelated siblings
cannot block the encoding class.

The read- and write-media procedures discover `rel=service-desc` from the
landing page and parse either JSON or YAML OpenAPI. Missing, inaccessible, or
unparseable service-description evidence SKIPs. Once a definition parses,
missing required operation metadata is a conformance failure. Read media
checks successful GET response content on canonical endpoints for every
declared feature-resource class and the custom collections items path when the
IUT advertises custom collections. Write media checks request content on at
least one canonical POST or PUT operation. Neither procedure issues a
mutation, and OPTIONS evidence is not accepted.

Each resource schema procedure independently requests its canonical collection
and one canonical item with `Accept: application/geo+json`. HTTP status and
actual media are established before parsing. The complete collection and
single-resource documents are validated against the released System,
Deployment, Procedure, or Sampling Feature schemas. Canonical item selection
comes from safely parsed collection evidence; an empty collection or
unsupported actual media SKIPs without creating a false PASS.

Manual-inspection procedures automate the released mapping tables over every
inspectable feature from bounded canonical collection traversal. The common
feature procedure validates URI-valued `properties.uid` and string-valued
optional `name` and `description`. Resource procedures validate each present
attribute and association at its defined GeoJSON location and type. Optional
members remain optional. The relation-types procedure aggregates all four
resource classes, rejects any association relation not valid for that type,
ignores generic links, and emits a no-evidence SKIP only after complete
inspection.

Expected unsupported-media or empty-resource limitations are retained at the
narrow resource boundary while later resource types continue. Assertion
failures, non-200 responses, unsafe pagination, invalid supported content,
wrong schema, and mapping or relation defects are not caught or downgraded.

The eight released single and collection schema entries plus their transitive
references pass resolver-normalized semantic parity against pinned release
commit `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`. The parity tool verifies that
the source checkout is at that exact commit and clean. The same fail-closed
source-provenance check is added to the Property parity tool. Dedicated
Property controlled-HTTP regressions cover pagination and continuation after
later collection or item evidence limitations.

Primary TeamEngine E2E uses unmodified local OSH. It declares GeoJSON and
advertises external OpenAPI 3.1 YAML, but canonical feature collections return
generic `application/json` under a GeoJSON Accept header. Those schema and
manual-inspection procedures remain honest media SKIPs. Controlled read-only
HTTP coverage provides positive JSON/YAML API-definition, schema, mapping,
relation, pagination, and continuation evidence. No OSH or TeamEngine source
or binary change and no hosted CI are permitted.

The implemented boundary follows this design. Discovery parsing tolerates
valid JSON bodies from landing, conformance, and collections endpoints when
local OSH supplies the known nonstandard `Content-Type: auto`; that exception
does not apply to canonical GeoJSON representations. Exactly twelve methods
are deployed and all twelve have reviewed exact mappings. Coverage is
`240/51 exact/2 helper/119 candidate/68 unmapped`; `/conf/geojson` is `12/12
exact`. Focused Maven is `45/0/0/0`, and full Maven is `548/0/0/3`.

Exact image
`sha256:9277fe99e6cf4bacbee9b839ab6890e789ebeaac1e6a8de6eecd052494245c19`
passes runtime, dependency, credential, and immutable-base gates. Primary
unmodified local OSH TeamEngine is honestly `219/40/7/172`; the twelve
GeoJSON procedures are evidence SKIPs, not conformance passes. Controlled HTTP
executes every positive procedure and key fail-closed branch. API Common
sabotage skips all twelve methods before GeoJSON IUT access. Credential and
artifact-hygiene gates record zero writes and zero leaks. Final Raze review is
`APPROVE` at confidence `0.99`; all four initial findings are closed and no
required fixes remain.

### Sprint 55: released Part 1 Advanced Filtering procedures

Sprint 55 replaces the historical six-method subset with the 25 released Annex
A procedures.

```text
AdvancedFilteringTests (25 independent procedures)
          |
          v
AdvancedFilteringSupport
  declaration and canonical endpoint selection
  + seed-derived ID/UID/keyword/property queries
  + bounded filtered traversal and representation validation
  + JTS WKT/GeoJSON geometry intersection
  + association graph and combined predicate evaluation
  + transitive recommendation set comparisons
          |
          `--> Part1ApiCommonSupport bounded traversal
```

Class setup normalizes only the API root after direct Core, Common, and Part 1
API Common prerequisite inspection. Each procedure uses `alwaysRun`, has no
method dependency, and reacquires its own declaration and filter evidence.

Mandatory query values come from resources visible at the same IUT. Common
filters inspect each canonical Part 1 endpoint whose resource class is
declared. Local-ID and UID lists are tested separately; ID coverage includes a
UID-prefix query. Keyword values come from human-readable name or description
members. A generated known match cannot produce an empty PASS.

Geometry filtering requests GeoJSON Systems, Deployments, and Sampling
Features with usable geometry. WKT query values and GeoJSON response
geometries are parsed by JTS, and every returned feature must intersect the
filter geometry.

Association filters derive both local and URI identifiers from actual
relations. Returned collection pages are validated through the matching
released representation support before each resource's parent, procedure,
feature-of-interest, observed-property, controlled-property, deployed-System,
Datastream, ControlStream, base-property, or object-type evidence is checked.
Pagination and association traversal are bounded, cycle-safe, and same-origin.
Cross-origin relation targets are used only as permitted identifier evidence
and never receive the IUT credential.

The evidence router distinguishes direct representation relations from
procedure-prescribed subresources. Deployment System/FOI/property predicates
must traverse `deployedSystems` or `featuresOfInterest`; Sampling Feature
property predicates must traverse Datastream or ControlStream subresources.
Equivalent root aliases and extension descendants do not substitute for those
paths. A same-origin link wrapper contributes no ID or UID after resolution;
the resolved target representation controls. Association collections use the
same actual-media and bounded-pagination gate as canonical collections.

Combined filtering inventories applicable inherited `id`, `q`, `featureType`,
`datetime`, and geometry predicates, mandatory class-specific association
predicates, and positively supported custom-property predicates. It executes
every independently evidenced pair and requires logical AND semantics. The indirect
property and feature-of-interest recommendations compare complete direct and
transitive result sets. Custom-property and indirect recommendation
non-support is logged as a warning rather than treated as a requirement
failure.

Expected missing seed evidence is retained at narrow endpoint/resource
boundaries while later independent evidence is processed. Assertion failures,
non-200 responses, invalid supported representations, unsafe traversal, empty
known-match responses, predicate errors, and later-page defects escape as
failures.

The implementation follows the target requirement where released ATS prose has
obvious editorial substitutions: parent Deployment instead of parent System,
`system` instead of `foi` on the deployment UID repetition, and
`samplingFeatures` instead of a repeated `systems` endpoint in indirect
property checks. Normative recursive subsystem behavior is not coupled to a
non-standard literal `/components` path.

Primary TeamEngine E2E uses unmodified local OSH. Because the IUT does not
declare Part 1 `/conf/advanced-filtering`, all 25 methods must deploy and SKIP
before filter-specific access. Controlled HTTP supplies all positive
procedures and fail-closed cases. No OSH or TeamEngine source or binary change
and no hosted CI are permitted.

Final-Raze remediation discards resolved wrapper identifiers, rejects root
alias shortcuts, gates and paginates association targets, limits keyword
labels to resource boundaries, and includes inherited, mandatory, temporal,
feature-type, and positively supported custom predicates in combined checks.
R4 constrains direct association discovery to exact recognized root or
immediate GeoJSON `properties` fields and explicit relation-link URIs.
Dereferenced Deployment property targets must be single System
representations. Its red baseline is `40/4/0/0`; exact candidate `060a8aa`
passes focused controlled HTTP `40/0/0/0`, full Maven `594/0/0/3`, exact-image
runtime, unmodified-local-OSH, sabotage, credential, immutability, hygiene,
source, and `20/20` scenario-trace gates. Candidate `756d729` remains
superseded audit evidence. Raze R5 found four additional relation-vocabulary,
System-type, representation-validation, and mapping gaps, superseding
`060a8aa`. R5 HTTP regressions reproduce `42/3/4/0`; media-aware canonical
remediation passes focused `48/0/0/0`, full Maven `602/0/0/3`, and regenerated
coverage `240/76/2/115/47`. Exact candidate `f2a88d5` passes focused
`48/0/0/0`, full Maven `602/0/0/3`, image/runtime, released-source,
unmodified-local-OSH `238/40/7/191`, sabotage `238/2/10/226`, credential,
immutability, and hygiene gates. Raze R6 supersedes that candidate after
finding that the shared relation matcher still strips a generic `Link` suffix,
normalizes case/punctuation, and admits broad `parent`/`procedure` aliases.
The replacement design gives GeoJSON relation links, GeoJSON property links,
and SensorML root members separate exact vocabularies; no lossy normalization
is permitted. R6 regression-first verification moves controlled HTTP from
`46/3/0/0` to `46/0/0/0`; focused Maven passes `51/0/0/0` and full Docker
Maven passes `605/0/0/3`. A new committed candidate must repeat every exact
gate. Candidate `b5bc49b` completed those gates, but Raze R7 found that
`links[].rel` is still evaluated before representation dispatch. The
replacement must inspect link relations only for GeoJSON and must derive
SensorML System parent and Procedure associations only from exact
`attachedTo` and `typeOf` members. Regression-first verification moves
controlled HTTP from `48/2/0/0` to `48/0/0/0`; focused Maven passes
`53/0/0/0` and full Docker Maven passes `607/0/0/3`. A new exact committed
candidate must repeat every runtime and E2E gate. Candidate
`fce461288e99167bab6f391085493784da42cc58` completed that cycle with exact
image
`sha256:ed03d1f943da442d8c13bdfc5c140b08c1e9155a57f3f10696925a2a0a402a79`,
honest unmodified-local-OSH `238/40/7/191`, sabotage `238/2/10/226`, zero IUT
writes or credential leaks, and immutable external dependencies. Fresh Raze
R8 is `APPROVE 0.99` with both prior findings closed and no new findings.

## Status

**Approved for Sprint 1 + Sprint 2 + Sprint 3 + Sprint 4 ratifications**. Generator (Dana) may begin S-ETS-04-* work in Pat's recommended dependency order (S-ETS-04-04 → -01 → -03 → -02 → -05) per Sprint 4 contract `deferred_to_generator` block. Architect's 3 deferred decisions + 2 surfaced suggestions are now resolved; ADR-009 v2 amendment + ADR-010 v2 amendment + this Sprint 4 Ratifications section's stub-IUT credential-leak design + Subsystems coverage scope cover them.

The Sprint 1 + Sprint 2 + Sprint 3 ratifications above remain canonical. The S-ETS-01-03 CONCERNS verdict from Sprint 1 remains closed retroactively by ADR-007.
