# Architecture — OGC API Connected Systems ETS (TeamEngine)

> Version: 2.0.31 | Status: Living Document | Last reconciled: 2026-07-27 (Part 1 Subdeployment complete; final Raze approved)
> **Supersedes v1.0** (preserved verbatim at `_bmad/architecture-v1-frozen.md`).
> v1.0 was web-app-shaped (Next.js + Node + browser UI). v2.0 reflects the user pivot
> 2026-04-27 to a Java/TestNG Executable Test Suite for OGC TeamEngine.
>
> **Authority**: this document binds the Generator (Dana). Where the PRD or capability spec
> conflicts with an ADR or with this file's section, the ADR is authoritative for the decision
> in question and Sam (orchestrator) reconciles back to the PRD/spec at the next planning cycle.

---

## 1. Overview

The deliverable is **`ets-ogcapi-connectedsystems10`** — a Java 17 / Maven 3.9 / TestNG / REST Assured Executable Test Suite that registers through the `com.occamlab.te.spi.jaxrs.TestSuiteController` SPI. TeamEngine 5.6.1 is the verified historical baseline; CP-001/ADR-011/Sprint 41 define migration to an immutable OGC-published TeamEngine 6.0.0 runtime aligned with the `ets-common:17` compile lineage. An OGC API – Connected Systems server implementer points TeamEngine at their CS API landing page through the canonical `iut` run argument; TeamEngine invokes our suite, validates responses against the bundled OGC JSON Schemas, and produces a TestNG XML report TeamEngine renders for the user. Coverage targets actual partial OGC 23-001 Part 1 and OGC 23-002 Part 2 conformance classes plus explicitly scoped project cross-class closures.

This is **not a web application**. It has no browser UI, no REST endpoints we author, no session storage we own. TeamEngine owns the user-facing surface; we provide a jar that TeamEngine loads.

## 2. Deployment topology

The same jar is verified locally and later deployed by OGC:

```
+--------------------------------------+   +---------------------------------+
|  Local development and verification  |   |  OGC validator (production)     |
|                                      |   |                                 |
|  bootstrap validator                 |   |  cite.opengeospatial.org/       |
|  Docker Maven unit/build checks      |   |    teamengine/                  |
|  exact-image runtime verification    |   |  Runs OGC-managed TeamEngine    |
|  TeamEngine E2E against local OSH    |   |  Loads released ETS artifact    |
+--------------------------------------+   +---------------------------------+
                  |                                      |
                  +-- same ETS-owned release artifact ---+
```

**Local (developer)**:
- Until the reusable validator is published, run
  `scripts/bootstrap-swecommon30-validator.sh` before raw host Maven, or use the
  supported `scripts/mvn-test-via-docker.sh` wrapper. Then `mvn clean install`
  produces `target/ets-ogcapi-connectedsystems10-<version>.jar` and
  `target/ets-ogcapi-connectedsystems10-<version>-aio.jar`.
- `docker compose up` uses the repository Dockerfile. The verified historical baseline manually assembles TeamEngine 5.6.1; Sprint 41 replaces it with a digest-pinned OGC-published TeamEngine 6.0.0 runtime containing the ETS jar, justified dependency closure, and CTL resources at supported TeamEngine extension paths. TeamEngine discovers the suite via `META-INF/services/com.occamlab.te.spi.jaxrs.TestSuiteController` (ADR-001/011). Maven, image build, runtime verification, Compose health, suite registration, and the final local OSH TeamEngine E2E gate are archived.
- Developer browses `http://localhost:8081/teamengine/` and runs the Connected Systems suite from the CTL UI.
- The supported local deployment path is the repository **Dockerfile + `docker-compose.yml` + `scripts/smoke-test.sh`**. Any Maven `docker` profile is stale unless it is removed, made a no-op, or delegates to this exact digest-pinned Dockerfile path. It must not define an independent TeamEngine runtime, independent port/startup contract, or broad dependency-copy path.
- The canonical run-argument contract is: required `iut`; optional `auth-credential`, `mutation-tests-enabled`, `mutation-iut-policy`, `mobile-system-id`, and `subdeployment-association-evidence`. The last two arguments respectively supply the released `/conf/system/location-time` prerequisite and an independent Deployment-association ownership oracle; neither enables mutation. TeamEngine UI and documentation may label `iut` as "CS API landing page" for users, but the serialized argument key passed into TestNG must be `iut`. Do not introduce `auth-type` unless Java/TestNG support is explicitly implemented later.

**Verification boundary (ADR-012)**:
- Project-operated hosted CI is not approved and is not part of the architecture.
- Developers run the Docker Maven wrapper, exact-image runtime verifier, and
  TeamEngine E2E locally. Reproducibility checks are local scripted gates.
- Jenkinsfiles are retained only as inert OGC submission/build metadata. They
  are not wired to a project Jenkins service and are not development CI.
- OSH and TeamEngine source code and binaries are external and immutable for this
  project. Supported IUT configuration/test data and additive ETS installation
  at documented TeamEngine extension locations remain permitted.

**OGC validator (production)** — post-beta milestone only:
- `cite.opengeospatial.org/teamengine/` runs the OGC's `teamengine-production` Docker image. Once we publish to Maven Central (REQ-ETS-CITE-001), the OGC's `teamengine-production/pom.xml` adds `<ets-ogcapi-connectedsystems10.version>` and our jar is included in the next image rebuild.
- We do not control the cadence of the OGC's image rebuilds; that is governance velocity (Mary's `TIMELINE-DOMINATED-BY-GOVERNANCE` flag).

## 3. Component model

```
+---------------------------------------------------------------------------------+
|  TeamEngine runtime (TE 5.6.1 verified baseline; TE 6.0.0 Sprint 41 target)     |
|                                                                                 |
|  +-----------------------------+      +-------------------------------+         |
|  | CTL UI (Saxon XSLT)         |----->| TestSuiteController SPI       |         |
|  | renders form from           |      | (Java ServiceLoader scan of   |         |
|  | ogcapi-connectedsystems10-  |      |  META-INF/services/)          |         |
|  | suite.ctl                   |      +---------------+---------------+         |
|  +-----------------------------+                      |                          |
|                                                       v                          |
+-------------------------------------------------------|--------------------------+
                                                        |
                                                        | classloads
                                                        v
+---------------------------------------------------------------------------------+
|  ets-ogcapi-connectedsystems10.jar                                              |
|                                                                                 |
|  +------------------------------------------------------------+                 |
|  |  org.opengis.cite.ogcapiconnectedsystems10.TestNGController|                 |
|  |  (impl com.occamlab.te.spi.jaxrs.TestSuiteController)      |                 |
|  |  - getCode(), getVersion(), getTitle()                     |                 |
|  |  - doTestRun(Document) → delegates to TestNGExecutor       |                 |
|  +-----------+--------------------------------+---------------+                 |
|              |                                |                                 |
|              v                                v                                 |
|  +-----------+-------+        +---------------+-----------------+               |
|  | testng.xml        |        | ets.properties (ets-code, etc.)|               |
|  | (suite descriptor;|        +---------------------------------+               |
|  | tests + listeners)|                                                          |
|  +-----------+-------+                                                          |
|              |                                                                  |
|              v                                                                  |
|  +-----------+-----------------------------------------------+                  |
|  |  conformance.core.* (TestNG @Test classes; Sprint 1)      |                  |
|  |  - LandingPageTests       (REQ-ETS-CORE-002)              |                  |
|  |  - ConformanceTests       (REQ-ETS-CORE-003)              |                  |
|  |  - ResourceShapeTests     (REQ-ETS-CORE-004)              |                  |
|  |                                                            |                  |
|  |  conformance.<class>.* (sprints 2..14; placeholders)      |                  |
|  |  - SystemFeaturesTests, SubsystemsTests, ... (one per     |                  |
|  |    Part 1 conformance class beyond Core)                  |                  |
|  +--------------------------+--------------------------------+                  |
|                             |                                                   |
|                             v                                                   |
|  +-------------+   +--------+--------+   +------------------+   +------------+ |
|  | RestAssured |   | OpenAPI/Schema  |   | SuiteFixture     |   | Listener   | |
|  | (HTTP DSL,  |   | Validator       |   | Listener         |   | + report   | |
|  | response    |   | (Kaizen openapi-|   | (@BeforeSuite —  |   | hooks      | |
|  | capture)    |   | parser + JSON   |   | landing/conform; |   |            | |
|  |             |   | Schema)         |   | shares state via |   |            | |
|  |             |   |                 |   | ITestContext)    |   |            | |
|  +------+------+   +--------+--------+   +--------+---------+   +------------+ |
|         |                   |                     |                              |
|         |                   v                     v                              |
|         |     +-------------+--------+    +-------+----------+                  |
|         |     | src/main/resources/  |    | EtsAssert        |                  |
|         |     |  schemas/ (126 OGC   |    | (fluent          |                  |
|         |     |  JSON Schemas        |    | assertion utils, |                  |
|         |     |  copied from         |    | structured       |                  |
|         |     |  csapi_compliance)   |    | failure msgs     |                  |
|         |     +----------------------+    | with /req/*      |                  |
|         |                                 | URIs)            |                  |
|         |                                 +------------------+                  |
|         v                                                                       |
+---------|---------------------------------------------------------------------+
          |
          | HTTP/HTTPS (auth headers per user input)
          v
   +-------------------------+
   | IUT — CS API server     |
   | e.g. api.georobotix.io  |
   +-------------------------+
```

### Component responsibilities

| Component | FQCN / location | Responsibility |
|---|---|---|
| **TestNGController** | `org.opengis.cite.ogcapiconnectedsystems10.TestNGController` | TeamEngine SPI entry point. Implements `TestSuiteController`. Delegates execution to `TestNGExecutor` (from `teamengine-spi`). 1:1 port of `ets-ogcapi-features10`'s controller (ADR-001). |
| **testng.xml** | `src/main/resources/org/opengis/cite/ogcapiconnectedsystems10/testng.xml` | TestNG suite descriptor. Declares `<test name="Core">` with `<package>` entries for `conformance.core.*` plus listener block. Sprint 1 ships Core only; `<test>` blocks for sprints 2-N. |
| **SuitePreconditions** | `org.opengis.cite.ogcapiconnectedsystems10.conformance.SuitePreconditions` | TestNG class run first via `<classes>` in testng.xml. Validates `iut` parameter is present and reachable. Pattern from features10. |
| **SuiteFixtureListener** | `org.opengis.cite.ogcapiconnectedsystems10.listener.SuiteFixtureListener` | Implements `ISuiteListener.onStart`. Performs landing-page fetch + `/conformance` fetch, stashes results into `ISuite.getAttribute()` so all suites can read declared conformance classes. Equivalent of v1.0's two-step discovery flow. |
| **TestRunListener / TestFailureListener / LoggingTestListener** | `org.opengis.cite.ogcapiconnectedsystems10.listener.*` | Standard ets-common pattern; mirror features10 listener set verbatim in Sprint 1 (minimal logging) and refine in Sprint 2. |
| **CoreTests (multiple classes)** | `conformance.core.LandingPageTests`, `ConformanceTests`, `ResourceShapeTests` | Sprint 1 P0. Each `@Test` method's `description` attribute starts with the OGC requirement URI (e.g. `OGC-23-001 /req/core/landing-page`) per REQ-ETS-CORE-001. |
| **OpenApi3Loader** | `conformance.openapi3.OpenApi3Loader` | Sprint 2+ helper that loads the OGC OpenAPI YAML via Kaizen `openapi-parser`. Sprint 1's Core class does not need this — Core asserts response shape via `everit-json-schema` (transitive from ets-common) against the bundled JSON Schemas at `src/main/resources/schemas/connected-systems-1/landing-page.json` etc. |
| **EtsAssert** | `org.opengis.cite.ogcapiconnectedsystems10.util.EtsAssert` | Fluent assertion utilities. Wraps Hamcrest matchers + structured failure messages that include the OGC `/req/*` URI. Mirrors `org.opengis.cite.ogcapifeatures10.EtsAssert` pattern. |
| **Schemas (resource bundle)** | `src/main/resources/schemas/{connected-systems-1, connected-systems-2, connected-systems-shared, external, fallback}/*.json` | 126 JSON Schema files copied verbatim from `csapi_compliance/schemas/` (ADR-002). Loaded at @BeforeSuite into a Kaizen schema registry; used by Schema Validator at @Test time. |
| **CTL wrapper** | `src/main/scripts/ctl/ogcapi-connectedsystems10-suite.ctl` | XSLT 2.0 CTL package that exposes the suite to TeamEngine's CTL UI. Calls `tng:new($outputDir)` then `tng:doTestRun(...)`. Canonical argument keys: `iut` (required), `auth-credential` (optional), `mutation-tests-enabled` (optional), `mutation-iut-policy` (optional). UI labels may describe `iut` as the CS API landing page. `auth-type` is not supported by the current Java run-argument contract. |
| **SPI registration file** | `src/main/resources/META-INF/services/com.occamlab.te.spi.jaxrs.TestSuiteController` | Single line: `org.opengis.cite.ogcapiconnectedsystems10.TestNGController`. Discovered by TeamEngine's classloader scan (ADR-001). |
| **ets.properties** | `src/main/resources/org/opengis/cite/ogcapiconnectedsystems10/ets.properties` | Maven-substituted properties: `ets-title`, `ets-version`, `ets-code` (`ogcapi-connectedsystems10`). |

## 4. Build & dependencies

Per ADR-004:

- **JDK**: 17 (mandatory, build fails on older)
- **Maven**: 3.9+ (enforced by maven-enforcer)
- **Parent POM**: `org.opengis.cite:ets-common:17` (release tag — NOT 18-SNAPSHOT)
  - Resolves TeamEngine 6.0.0 SPI artifacts via `<dependencyManagement>`
  - Pulls Jersey 3.1.8, Jackson 2.18.0, JTS 1.19, proj4j 1.1.3, etc.
- **Direct dependencies** (no `<version>` — versions inherited from ets-common):
  - `org.opengis.cite.teamengine:teamengine-spi`
  - `org.testng:testng`
  - `io.rest-assured:rest-assured`
  - `com.reprezen.kaizen:openapi-parser`
  - `org.locationtech.jts:jts-core`, `org.locationtech.proj4j:proj4j`, `org.locationtech.jts.io:jts-io-common`
  - `org.slf4j:slf4j-api`, `ch.qos.logback:logback-classic` (see §6 Logging)
- **Build plugins**: maven-compiler-plugin 3.13.0, maven-surefire-plugin 3.5.x, maven-assembly-plugin (AIO jar with `mainClass=...TestNGController`), maven-jar-plugin
- **Reproducibility**: `<project.build.outputTimestamp>2026-04-27T00:00:00Z</project.build.outputTimestamp>` (ADR-004 group C-5)

`mvn clean install` produces:
- `target/ets-ogcapi-connectedsystems10-<version>.jar` (thin)
- `target/ets-ogcapi-connectedsystems10-<version>-aio.jar` (all-in-one with deps; for CLI use)
- `target/ets-ogcapi-connectedsystems10-<version>-sources.jar` (per OGC convention)

### TeamEngine 6 deployment contract

For Sprint 41 and later, the authoritative runtime path is the digest-pinned TeamEngine 6 Dockerfile, repository Compose file, and `scripts/smoke-test.sh`. Generator must keep these three artifacts aligned on:
- base image digest and recorded TeamEngine/Tomcat/JDK versions
- installed ETS jar/resource/CTL paths
- startup command, exposed port, health endpoint, non-root runtime identity
- `iut`, `auth-credential`, `mutation-tests-enabled`, and `mutation-iut-policy` argument forwarding

The Maven `docker` profile is not an independent deployment surface. It must be removed, made a no-op with an explicit pointer to Dockerfile/Compose/smoke-test, or delegated to the same Dockerfile build. It must not copy arbitrary Maven runtime dependencies into TeamEngine; dependency payload must remain selected and justified.

## 5. Test runtime model

A test execution flows through these stages inside TeamEngine:

1. **Suite registration** (TeamEngine startup, ~30 sec — NFR-ETS-04). Tomcat starts → web-app classloader scans `WEB-INF/lib/*.jar` for `META-INF/services/com.occamlab.te.spi.jaxrs.TestSuiteController` files → instantiates each FQCN found. `TestNGController` constructor loads `ets.properties` and locates `testng.xml` on the classpath. `getCode()` returns `ogcapi-connectedsystems10`; TeamEngine renders the suite in the CTL list.
2. **CTL form** (user flow). User clicks "Connected Systems API 1.0" → CTL Saxon engine renders the form from `ogcapi-connectedsystems10-suite.ctl` → user enters the CS API landing page and optional supported controls → form POST → CTL function calls `tng:new($outputDir)` (constructs `TestNGController`) and `tng:doTestRun($controller, $testRunArgs)`. The form may label the landing-page field as `iut-url` or "CS API landing page" in visible text, but the XML argument key passed to Java/TestNG must be `iut`.
3. **TestNG launch**. `TestNGController.doTestRun(Document testRunArgs)` validates the args, builds a `Map<String,String>` of suite parameters (the `iut` URL is mandatory; `auth-credential`, `mutation-tests-enabled`, and `mutation-iut-policy` are optional), and invokes `TestNGExecutor.execute(testRunArgs)`. `TestNGExecutor` (from teamengine-spi) wires up TestNG: parses `testng.xml`, registers listeners, runs. `auth-type` is not part of the contract unless code is changed in a later story.
4. **@BeforeSuite phase**. `SuiteFixtureListener.onStart(ISuite)` reads the `iut` parameter, fetches the landing page, fetches `/conformance`, stashes both into `ISuite.setAttribute(...)`. `SuitePreconditions` (a TestNG class run first via `<classes>` block in testng.xml) re-validates these are present.
5. **Per-suite execution** (Sprint 1 = Core only). TestNG runs `<test name="Core">` packages in declared order. For each `@Test` method:
   - REST Assured constructs the HTTP request from the IUT base URL + relative path
   - Auth header (if configured) is applied
   - Request is sent; full request/response captured by REST Assured's `RequestLoggingFilter` and `ResponseLoggingFilter` (these are written into the TestNG report attachment per FR-ETS-25)
   - Response body validated against the relevant JSON Schema via Kaizen / everit-json-schema
   - `EtsAssert.assertXxx(...)` produces structured failure messages that include the OGC `/req/*` URI in case of FAIL
6. **Dependency-skip semantics** (FR-ETS-24, sprints 2+). If Core's `@Test` produces a FAIL, downstream conformance class suites that `dependsOnGroups("core")` (TestNG's native dependency mechanism) auto-skip with reason `dependency /conf/core not satisfied`. Sprint 1's Core suite is dependency-free.
7. **Report generation**. TestNG's `XmlReporter` writes `target/testng-results.xml`. TeamEngine's executor wraps this into the user-visible HTML report at `/teamengine/results/<run-id>/`.

### Concurrency model

Sprint 1 ships sequential test execution within Core (TestNG default — `parallel="false"`). Concurrency within a class can be enabled via `<test parallel="methods" thread-count="5">` in testng.xml in a future sprint. Across-class ordering uses TestNG `<test>` elements in declared order; cross-class dependency uses `dependsOnGroups`.

## 6. Quality, assertions, and logging

### Assertions

`EtsAssert` wraps Hamcrest matchers and produces failure messages that **always include**:
- The OGC requirement URI (e.g. `/req/core/landing-page`)
- The IUT base URL
- The HTTP request method + path
- A truncated response excerpt (≤500 chars)

Pattern (ports v1.0's `failureReason` discipline):
```
FAIL: /req/core/api-definition — IUT https://api.georobotix.io/ogc/t18/api landing-page
response had no `service-desc` AND no `service-doc` link relation. At least one is required
per OGC API Common Part 1 §7.4 (/req/core/api-definition). Excerpt: {"links":[{"rel":"self",...}]}.
```

### v1.0 fixes preserved (load-bearing)

Two assertion behaviors from v1.0 must port verbatim — the Generator must NOT regress them:

- **`rel=self` is example-only, not mandatory** (v1.0 GH#3 fix; SCENARIO-ETS-CORE-LINKS-NORMATIVE-001). The Core landing-page test must NOT fail when `self` is absent. Cite OGC API Common Part 1 — `self` appears as a sample value, not in a `/req/*` clause.
- **`service-desc` OR `service-doc` is the API-definition fallback** (v1.0 SCENARIO-API-DEF-FALLBACK-001; SCENARIO-ETS-CORE-API-DEF-FALLBACK-001). PASS when either is present; FAIL only when both are absent.

### Logging

- **slf4j-api** facade (per ADR-004 dep list).
- **logback-classic** binding (slf4j → SLF4J → logback). Pat's PRD NFR-ETS-10 calls for slf4j+logback; ets-common does not bind a backend, so we declare logback explicitly.
- Logback configured via `src/main/resources/logback.xml` to: emit JSON-structured logs (logback-jackson encoder), default level INFO, NEVER log Authorization/api-key headers (configurable maskList), append to STDOUT only (TeamEngine captures container logs).
- Credential masking: REST Assured logging filters configured to redact `Authorization`, `X-API-Key`, and any header in `auth-mask-headers` to `***MASKED***`. Pattern equivalent to v1.0's `CredentialMasker`.

`ets-common`'s default is `java.util.logging` via `TestSuiteLogger`. We add slf4j+logback on top because: (a) PRD NFR-ETS-10 specifies it, (b) it gives structured logs that TeamEngine's container-orchestration consumers can parse, (c) RestAssured's logging is already slf4j-aware. **No conflict** with ets-common — both can coexist; `TestSuiteLogger` continues to be used by ets-common-supplied utilities, our code uses slf4j.

## 7. Spec-trap fixtures port plan (high-level)

The asymmetric `featureType`/`itemType` corpus from `csapi_compliance/tests/fixtures/spec-traps/` (~30-50 cases) ports as follows. Generator owns the detail; Architect sets the contract:

- **Location**: `src/test/resources/fixtures/spec-traps/<group-name>/*.json` (mirrors PRD §4 resolution; the v1.0 layout structure is preserved).
- **Loading**: a `org.opengis.cite.ogcapiconnectedsystems10.fixtures.SpecTrapFixtures` Java class reads the JSON files at @DataProvider time. Jackson deserializes them into typed POJOs that TestNG passes to `@Test(dataProvider=...)` methods.
- **Case ID retention**: each fixture file has a top-level `caseId` field; the Java loader exposes it; failed @Test failure messages include `caseId` so a CITE reviewer can trace the fixture back to its v1.0 origin.
- **Audit script**: `scripts/audit-fixture-port.sh` (REQ-ETS-FIXTURES-003) compares case-ID lists in TS source (`csapi_compliance/tests/fixtures/spec-traps/`) vs Java source (`src/test/resources/fixtures/spec-traps/`) and fails the local gate on unexplained drops.

This is **not a Sprint 1 deliverable** (out_of_scope per Pat's contract). Sprint 1 must NOT delete the requirement; it must reference the corpus existence in `epic-ets-06-fixture-port.md`.

## 8. Cross-repo integration

Per ADR-005:

- The frozen v1.0 repo (`csapi_compliance`) and the new ETS repo (`ets-ogcapi-connectedsystems10`) are **siblings**, not parent/child.
- No git submodule, no symlink, no shared package. Each is independently buildable.
- `csapi_compliance` README links to the new ETS as the certification deliverable; new ETS `README.adoc` links back to v1.0 as the dev pre-flight tool.
- `csapi_compliance@ab53658` is tagged `v1.0-frozen`. Schemas were copied verbatim into the new ETS at that point (ADR-002).
- URI-coverage diff (REQ-ETS-SYNC-001) is a local audit script in the new ETS that clones `csapi_compliance@v1.0-frozen` into the workspace; deferred to post-Sprint-1.

## 9. CITE submission pipeline

This is governance, not code, but the architecture must acknowledge the calendar (Mary's `TIMELINE-DOMINATED-BY-GOVERNANCE`):

```
Sprint 1: scaffold + Core green vs GeoRobotix         [code: 1-2 sprints]
Sprints 2-7: remaining Part 1 conformance classes     [code: 6-12 weeks]
Sprint 8+: Part 2 conformance classes                 [code: 6-12 weeks]
Beta gate: REQ-ETS-CITE-001..003                       [governance: weeks]
  - Maven Central publish via OSSRH staging
  - Outreach to OpenSensorHub + connected-systems-go
  - File CITE SC ticket
6-12 months in beta: gather 3 passing IUTs            [governance: quarters]
CITE SC review → TC vote → official release           [governance: months]
```

Total calendar from Sprint 1 to official release: **9-21 months**. Code-complete is a fraction of that.

## 10. Constraints from OGC

The Generator (Dana) MUST respect these or CITE SC review will reject the ETS:

1. **No non-Java test runtime**. No shell-out to Node.js, Python, Go. Tests run in the JVM.
2. **Use ets-common's idioms**. `EtsAssert`, listener naming, package layout, ets.properties, testng.xml location — all per the features10 reference. Innovation is permitted in the test logic, not in the framework wiring.
3. **No new transitive dep without an ADR**. Adding a dependency that ets-common doesn't already manage requires an ADR justifying it (RAML in ADR-004 group B).
4. **Maven Central publish is a release-only action**. SNAPSHOTs go to OSSRH staging; never promote SNAPSHOTs to Maven Central (REQ-ETS-CITE-001).
5. **Reproducible builds**. `<project.build.outputTimestamp>` is set; the local release gate verifies double-build byte-identical jars.
6. **README is .adoc, not .md**. AsciiDoc per OGC convention. Top-level files: `README.adoc`, `LICENSE.txt`, `pom.xml`, `Jenkinsfile` (stub), `Dockerfile`, `docker-compose.yml`.
7. **Respect the v1.0 GH#3 fix and API-def fallback**. See §6 Quality. Regressing these is a release-blocker.
8. **Public metadata is part of the conformance package**. CTL, `src/main/config/teamengine/config.xml`, site AsciiDoc, Javadoc overview, README, and sample test-run-props files must describe the actual OGC API Connected Systems Part 1/Part 2 partial coverage, TeamEngine 6 status, local OSH primary E2E target, and real run arguments. Archetype placeholders such as XML examples, Class A/Class B, WCAG/XML citations, or W3Schools default IUTs are not acceptable in a CITE-facing package.

## 11. Open architectural risks (residual)

1. **`teamengine-spi` 5.6 vs 6.0 SPI/runtime alignment** (residual after ADR-001/011). The SPI interface has been stable across 5.x, but TE 6.0.0 brings Tomcat 10.1, Jakarta/Jersey behavior, and classloader differences. Mitigation: stay on `ets-common:17`, use the digest-pinned TeamEngine 6 Dockerfile path, inspect linkage errors, and archive full TeamEngine execution against the primary local OSH IUT before marking Sprint 41 implemented.
2. **OGC OpenAPI YAML structure for CS API is not yet finalized** (Mary's `SCHEMAS-MAY-DRIFT`). Sprint 1 sidesteps by validating Core responses directly against bundled JSON Schemas (Kaizen-loaded) rather than via the OpenAPI YAML. Sprint 2+ Part 1 classes that depend on the OpenAPI structure (e.g. operation-parameter validation) will need to revisit when SWG settles.
3. **Spec-trap fixture port fidelity** (Mary's `SPEC-TRAP-FIXTURES-UNIQUE-IP`). Mitigation: REQ-ETS-FIXTURES-003 audit script enforces 1:1 case-ID mapping. Generator's epic-ets-06 work must not silently drop cases.
4. **TestNG dependency-graph correctness** (sprints 2+). The 13 released Part
   1 conformance classes have a dependency DAG defined by their normative
   prerequisites. The frozen v1.0 registry is an advisory comparison only.
   Translating the released DAG into TestNG `<groups>` +
   `dependsOnGroups` is mechanical but error-prone. Mitigation: structural
   tests verify the released dependency mapping under REQ-ETS-COVERAGE-001.
5. **Reproducible builds on Windows**. `<project.build.outputTimestamp>` works on Windows, but git's autocrlf can introduce line-ending differences in resource files inside the jar. Mitigation: `.gitattributes` enforces LF for all `.json`, `.xml`, `.ctl`, `.properties` files at scaffold time.
6. **Logback + ets-common's `java.util.logging`**. Both run in the JVM. If a CITE reviewer expects only ets-common's logging idiom, our slf4j+logback addition is justified by NFR-ETS-10 but is a deviation from the features10 baseline. Mitigation: documented in §6 above.
7. **Public docs and TeamEngine metadata drift**. The ETS generated from the TestNG archetype has several public surfaces that can drift independently: CTL, config.xml, README, site AsciiDoc, Javadoc overview, and sample test-run-props. Mitigation: treat these as conformance package artifacts, not marketing docs; keep them aligned with the canonical run-argument contract and actual partial Part 1/Part 2 coverage.

## 12. Implementation phasing

| Sprint | Stories | Output |
|---|---|---|
| **Sprint 1 (current)** | S-ETS-01-01, -02, -03 | Archetype scaffold + JDK 17 modernized + Core suite + TeamEngine Docker smoke green vs GeoRobotix |
| Sprint 2 | TBD per Pat | 2-3 of the remaining 13 Part 1 classes (likely `common`, `system-features`, `subsystems` — top of the dependency DAG) |
| Sprints 3-6 | TBD | Remaining Part 1 classes; spec-trap fixture port (epic-ets-06 in parallel) |
| Sprint 7 | TBD | URI-coverage local audit (REQ-ETS-SYNC-001); README repositions; v1.0-frozen tag |
| Sprint 8+ | TBD | Part 2 conformance classes (per OGC 23-002) |
| Beta milestone (calendar) | non-sprint | Maven Central publish; outreach; CITE SC ticket |

## 13. ADR index

| ID | Title | Status |
|---|---|---|
| ADR-001 | TeamEngine SPI Registration Pattern | Accepted (Sprint 2 cross-ref to ADR-007 added) |
| ADR-002 | JSON Schema Bundling Mechanism | Accepted |
| ADR-003 | Java Package Naming and Maven Coordinates | Accepted |
| ADR-004 | ets-archetype-testng:2.7 Modernization Checklist | Accepted (extended via ADR-006 Group F retro-row) |
| ADR-005 | Cross-Repo Relationship with the Frozen v1.0 Web App | Accepted |
| ADR-006 | Jersey 1.x → Jakarta EE 9 / Jersey 3.x Port (Archetype Util Layer) | Accepted (post-hoc, Sprint 2) |
| ADR-007 | Dockerfile Base Image Deviation: `tomcat:8.5-jre17` + Manual TE 5.6.1 Assembly | Superseded for forward runtime by ADR-011; historical baseline retained |
| ADR-008 | EtsAssert REST/JSON Helper API Surface | Accepted (forward-looking, Sprint 2) |
| ADR-009 | Multi-Stage Dockerfile Pattern | Partially superseded by ADR-011; builder/non-root/minimal-copy principles retained; stale Maven docker profile must not remain an alternate broad-copy runtime path |
| ADR-010 | Dependency-Skip Verification Strategy: Bash Sabotage (Canonical) + TestNG Unit Test (Fast-Feedback Supplement) | Accepted (forward-looking, Sprint 3) |
| ADR-011 | OGC-Published TeamEngine 6 Runtime Image | Accepted and implemented for the forward runtime; final local OSH TeamEngine 6 E2E archived on 2026-07-22 |
| ADR-012 | External Dependency Immutability and No Hosted CI | Accepted; OSH/TeamEngine patches and project-hosted CI are out of scope |
| ADR-013 | Released OGC 23-001/23-002 ATS Source of Truth | Accepted; released tag and semantic inventory govern coverage |

## 14. Architecture v2.0.1 — Sprint 2 ratifications (2026-04-28)

This section appends to v2.0 (which remains the canonical baseline). Sprint 2 ets-02 ratified 4 deferred decisions and 2 surfaced questions. Cross-references to original architecture sections are included for navigation.

### 14.1 ADR-006 — Jersey 1.x → Jakarta EE 9 / Jersey 3.x port (post-hoc)

Cross-references **§4 Build & dependencies** (Jersey 3.1.8 transitive via ets-common:17 → teamengine-spi → jersey-core 3.1.8). Sprint 1 archetype-supplied util layer (8 source files: ClientUtils, URIUtils, ReusableEntityFilter, CommonFixture, TestFailureListener, ETSAssert, SuiteAttribute, SuiteFixtureListener + VerifySuiteFixtureListener test) was ported from `com.sun.jersey.api.client.*` (Jersey 1.x) to `org.glassfish.jersey.*` and from `javax.ws.rs.*` to `jakarta.ws.rs.*` per the `features10@java17Tomcat10TeamEngine6` reference branch. ADR-004 is amended with a "Group F" cross-reference; ADR-006 is the canonical record. Closes Raze s01 CONCERN-1.

### 14.2 ADR-007 — Dockerfile base image deviation: `tomcat:8.5-jre17` (post-hoc)

Cross-references **§2 Deployment topology** (Dockerfile section). The original architecture text at §2 said the local Docker context "spins TeamEngine + jar" via the production-docker image; ADR-007 now governs the Sprint 1 Dockerfile reality: the `:5.6.1` Docker Hub tag does not exist, and the production image runs JDK 8 — incompatible with our JDK 17 ETS jar and Jakarta EE 9 imports. The Sprint 1 Dockerfile assembles TE 5.6.1 manually on `tomcat:8.5-jre17` via Maven Central artifacts + 3 secondary patches (VirtualWebappLoader strip, JAXB shared-lib jars, deps-closure with TE 6.0.0 jars filtered). ADR-001 §Consequences amended with cross-reference. REQ-ETS-TEAMENGINE-003 spec wording reconciled. Closes Quinn s03 GAP-1 + Raze s03 CONCERN-1.

### 14.3 ADR-008 — EtsAssert REST/JSON helper API surface (forward-looking)

Cross-references **§6 Quality, assertions, and logging** (EtsAssert pattern). 5 new static helpers added to `org.opengis.cite.ogcapiconnectedsystems10.ETSAssert`: `assertStatus`, `assertJsonObjectHas`, `assertJsonArrayContains`, `assertJsonArrayContainsAnyOf`, `failWithUri`. Every helper carries the OGC `/req/*` URI. S-ETS-02-02 refactors the 21 Sprint-1 bare-throw sites to use these helpers (3 commits, smoke-test verified between each). Sprint 2+ binding constraint: zero `throw new AssertionError(...)` permitted in `conformance.*` subpackages.

### 14.4 ADR-009 — Multi-stage Dockerfile pattern (forward-looking)

Cross-references **§2 Deployment topology** (Dockerfile section) and ADR-007. Sprint 2 S-ETS-02-05 rewrites the Dockerfile as: Stage 1 = `eclipse-temurin:17-jdk-jammy` + Maven 3.9.9 + BuildKit cache mount for `~/.m2`; Stage 2 = `tomcat:8.5-jre17` + ADR-007's secondary patches + non-root `USER tomcat`. `scripts/smoke-test.sh` simplifies (drops host-mvn dependency). Image size target ≤ 450MB (vs Sprint 1 ~600MB). Eliminates Quinn s03 / Raze s03 host-`~/.m2` brittleness.

### 14.5 CredentialMaskingFilter (Sprint 2 S-ETS-02-04, no separate ADR)

Cross-references **§6 Quality, assertions, and logging** ("Credential masking" subsection). Implementation rules captured inline in `openspec/capabilities/ets-ogcapi-connectedsystems/design.md` §"CredentialMaskingFilter wiring (Sprint 2 S-ETS-02-04)". Class at `org.opengis.cite.ogcapiconnectedsystems10.listener.CredentialMaskingFilter`; masking semantics ported verbatim from `csapi_compliance/src/engine/credential-masker.ts` (first 4 + last 4 chars; full redaction below 8 chars). Logback `<pattern>` excludes MDC dump as defense-in-depth. Architect ruled NO separate ADR because (a) implementation is wire-the-OGC-pattern-verbatim, (b) NFR-ETS-08 + SCENARIO-ETS-CLEANUP-LOGBACK-MASKING-001 carry the audit weight already.

### 14.6 SystemFeatures conformance class (Sprint 2 S-ETS-02-06)

**Historical ratification, superseded by §25 and §26.** Cross-references
**§3 Component model** ("conformance.<class>.* (sprints 2..14; placeholders)"
entry). The Sprint 2 four-method minimal increment and its Sprint 3 expansion
roadmap are retained only as implementation history. They are not active
architecture requirements or released ATS mappings. Sprint 47's six exact
procedures and explicit prerequisite-result boundary are authoritative.

### 14.7 ADR-001 cross-reference amendment

Cross-references **§13 ADR index**. ADR-001 §Consequences "Positive" bullet 2 amended with a lightweight footnote pointing to ADR-007. Architect chose option (i) (footnote, not full rewrite, not v2 supersede) because ADR-001's SPI registration mechanics are correct as written; only the production-image-without-modification parenthetical was wrong. Lightest touch preserves audit-trail continuity.

## 15. Architecture v2.0.2 — Sprint 3 ratifications (2026-04-29)

This section appends to v2.0.1 (which remains the canonical baseline). Sprint 3 ets-03 ratified 3 deferred decisions and 1 surfaced question. Cross-references to original architecture sections + Sprint 2 §14 are included for navigation.

### 15.1 ADR-010 — Dependency-skip verification strategy (forward-looking)

Cross-references **§14.6 SystemFeatures conformance class** (Sprint 2) — closes the deferred CRITICAL acceptance criterion #7 (live break-Core verification). Sprint 3 S-ETS-03-01 implements BOTH: (a) `scripts/verify-dependency-skip.sh` (bash sabotage with stub-server preferred over testng.xml mutation; canonical CITE-SC-grade artifact archived to `ops/test-results/sprint-ets-03-dependency-skip-evidence.xml`); (b) `VerifyDependencySkipWiring.java` TestNG unit test (structural lint via `org.testng.xml.Parser` over `XmlSuite` API; fast-feedback in `mvn test`). Defense-in-depth: structural lint catches refactor regressions; bash script catches semantic regressions. Sets precedent for Common (S-ETS-03-07) and Subsystems (Sprint 4) dependency wiring extensions. Worktree-pollution constraint embedded: sabotage operates on `/tmp/` clones OR built Docker image only — NEVER against the user's worktree at `~/docker/gir/ets-ogcapi-connectedsystems10/`. Closes Quinn s06 CONCERN-1 + Raze s06 CONCERN-1.

### 15.2 ADR-009 Sprint 3 amendment — Image-size optimization via TE common-libs ↔ deps-closure dedupe

Cross-references **§14.4 ADR-009** (Sprint 2). Sprint 2 shipped at ~570MB (missed 450MB target); ADR-009 §Negative bullet 4 explicitly anticipated Sprint 3 carryover. Sprint 3 S-ETS-03-04 EXTENDS Stage 1 of the Dockerfile with a `dedupe` RUN step that removes jars from `target/lib-runtime/` already provided by `teamengine-web-common-libs.zip` (extracted to `/usr/local/tomcat/lib/`). Generator MUST derive the exclusion list EMPIRICALLY (illustrative table in ADR-009 amendment is NOT the authoritative list). Acceptance: ≤ 450 MB reported via `docker images --format '{{.Size}}'`; smoke 12/12+ PASS preserved. Rejected: distroless (Sprint 5+; deferred per original §Alternatives) and alpine refinement (50-100MB savings insufficient vs dedupe's 200-300MB). Closes Quinn cleanup GAP-1 + Raze cleanup CONCERN-2.

### 15.3 design.md §"Sprint 3 hardening: MaskingRequestLoggingFilter wrap pattern (S-ETS-03-02)" — REST-Assured wrap

Cross-references **§14.5 CredentialMaskingFilter** (Sprint 2; design.md §"CredentialMaskingFilter wiring"). Sprint 3 S-ETS-03-02 closes the unmasked side-channel Sprint 2 left open: a new `org.opengis.cite.ogcapiconnectedsystems10.listener.MaskingRequestLoggingFilter` extends REST-Assured 5.5.0's `RequestLoggingFilter`, swaps masked headers in / restores originals out via try/finally (so the IUT still receives real credentials). Architect ratifies subclass pattern (Pat's option (a)) — rejects chained-filter (fragile to REST-Assured filter-order changes) and full-replacement (overkill — re-implements 200+ LOC of formatting). **NO separate ADR** (precedent: CredentialMaskingFilter NO-ADR ruling §14.5); design.md amendment is sufficient. The CredentialMaskingFilter is RETAINED in parallel for defense-in-depth FINE-level forensic logging. Credential-leak integration test (`scripts/verify-credential-leak.sh`; previously deferred per Quinn cleanup CONCERN-1) is now mandated by S-ETS-03-02.

### 15.4 Surfaced question resolution — REST-Assured wrap ADR vs design.md amendment

**Resolved: design.md amendment** (NOT a new ADR). Justification: (a) the wrap uses REST-Assured's well-trodden public Filter SPI; (b) the masking semantics already exist in `CredentialMaskingFilter.maskValue(...)` (Sprint 2); (c) the wrap is 30-50 LOC subclass — decision surface too small for an ADR per the Sprint 2 §14.5 NO-ADR-for-CredentialMaskingFilter precedent; (d) audit weight is carried by NFR-ETS-08 + SCENARIO-ETS-CLEANUP-LOGBACK-MASKING-001 + the now-mandated credential-leak integration test. ADR-010 §"Notes / references" cross-references the design.md section to keep traceability.

### 15.5 Architecture-level guidance for Generator

- **Worktree-pollution constraint**: ALL Sprint 3 work (Generator + Quinn + Raze) operates against `/tmp/` clones or archived artifacts; NEVER against `~/docker/gir/ets-ogcapi-connectedsystems10/`. Sprint 2 SystemFeatures gate-run polluted that worktree; Sprint 3 contract embeds this constraint at `worktree_pollution_constraint`.
- **ADR cardinality**: 10 ADRs is approaching the threshold where an `_bmad/adrs/INDEX.md` navigation aid would help (Pat surfaced this risk). Architect defers the index to Sprint 4 — ADR-010 is not yet over the threshold (10 vs Pat's hypothetical 11+ trigger).
- **Historical Generator batching guidance**: Sprint 3 grouped security, the then-proposed CI path, and Dockerfile work. CP-003/ADR-012 retires the CI portion; this note is chronology only.

## 16. Architecture v2.0.3 — Sprint 4 ratifications (2026-04-29)

This section appends to v2.0.2 (which remains the canonical baseline). Sprint 4 ets-04 ratified 3 deferred decisions and 2 surfaced suggestions. Cross-references to original sections + Sprint 2 §14 + Sprint 3 §15 are included for navigation.

### 16.1 ADR-009 v2 amendment — chown-layer attack + Sprint 3 illustrative-table falsification

Cross-references **§15.2 ADR-009 Sprint 3 amendment** + **§14.4 ADR-009** (Sprint 2). Sprint 3 S-ETS-03-04 EMPIRICALLY FALSIFIED the 200-300MB jar-dedupe projection (only 4 jars / ~1.8MB exact-basename overlap on actual TE 5.6.1 + ETS 0.1-SNAPSHOT post-ADR-006 layout per `~/docker/gir/ets-ogcapi-connectedsystems10/ops/test-results/sprint-ets-03-04-empirical-dedupe-list-2026-04-29.txt`). Sprint 4 S-ETS-04-02 EXTENDS Stage 2 of the Dockerfile with **`COPY --chown=tomcat:tomcat`** on every COPY directive + per-RUN-step ownership for TE WAR/console extraction, ELIMINATING the standalone `RUN ... && chown -R tomcat:tomcat /usr/local/tomcat` ~80MB layer. Target image size <600MB (Sprint 4 PASS — empirically permissive; PARTIAL acceptable at 600-650MB given multi-jar runtime classloader requirements). Architect picks **in-place ADR-009 amendment** (Pat's option (a)) over new ADR-011 superseding (option (b)) per §14.5 NO-ADR-for-CredentialMaskingFilter + §14.7 ADR-001 cross-reference precedent. Sprint 5+ alpine-variant roadmap documented (50-100MB additional savings if Sprint 4 underperforms; trigger: user-prioritized size reduction). Closes Sprint 3 carryover empirical-falsification gap.

### 16.2 ADR-010 v2 amendment — Two-level dependency-skip cascade (defense-in-depth)

Cross-references **§15.1 ADR-010** (Sprint 3) + **§14.6 SystemFeatures conformance class** (Sprint 2). Sprint 4 S-ETS-04-05 introduces the FIRST two-level group-dependency chain (Subsystems→SystemFeatures→Core). Architect ratifies **option (c) BOTH defense-in-depth**: (a) testng.xml `<group name="subsystems" depends-on="systemfeatures"/>` extension (mechanical; mirrors Sprint 2 SystemFeatures pattern; both per-`<test>` and consolidated-suite-level forms documented — Generator picks based on TestNG 7.9.0 runtime cascade behavior); (b) `@BeforeSuite` SkipException fallback in `SubsystemsTests` (~10 LOC checking `core.failed` / `systemfeatures.failed` SuiteAttribute keys; activates ONLY IF runtime verification shows TestNG transitive cascade does not work). Generator MUST runtime-verify via extended bash sabotage (Core sabotage → assert SystemFeatures AND Subsystems both `status="SKIP"`); archive to `ops/test-results/sprint-ets-04-two-level-dependency-skip-evidence.xml`. `VerifyDependencySkipWiring` unit test (Sprint 3 baseline) extended with Subsystems structural assertions (~10 LOC). Closes Pat's TWO-LEVEL-DEPENDENCY-CASCADE-MAY-NOT-WORK risk pre-emptively.

### 16.3 design.md §"Sprint 4 hardening: credential-leak E2E via stub IUT (S-ETS-04-03)" — stub IUT pattern

Cross-references **§15.3 MaskingRequestLoggingFilter wrap pattern** (Sprint 3) + **§14.5 CredentialMaskingFilter** (Sprint 2). Architect ratifies **option (a) stub IUT in /tmp/** — REJECTS (b) authenticated IUT pivot (sacrifices hermeticity; CITE SC reproduction friction) and (c) extended unit-layer fallback (already shipped in Sprint 3; insufficient as deeper-E2E evidence). New `scripts/stub-iut.sh` (extends Sprint 3 sabotage-server pattern; echoes inbound Authorization header verbatim in 401 response body for cross-check). New `scripts/verify-credential-leak.sh` (composes with S-ETS-04-04 sabotage-script bug fixes — same ephemeral-port + `trap cleanup EXIT` primitives) executes three-fold cross-check: (i) grep `EFGH12345678WXYZ` in TestNG XML + container logs + REST-Assured stdout (zero hits required); (ii) grep `Bear***WXYZ` masked form (≥1 hit required); (iii) parse stub-IUT echo and assert it received the FULL UNMASKED credential (proves try/finally restoration per §15.3). **NO separate ADR** (precedent: §14.5 + §15.4 NO-ADR-for-CredentialMaskingFilter ruling). Closes Quinn cumulative CONCERN-3 / Raze cumulative CONCERN-1 deeper-E2E gap.

### 16.4 design.md §"Sprint 4 hardening: Subsystems conformance class scope (S-ETS-04-05)" — Sprint-1-style minimal

**Historical ratification, superseded by §27.** The four-method shape/link
surface below is retained only as Sprint 4 audit history. It is not an active
architecture requirement or released ATS mapping.

Cross-references **§14.6 SystemFeatures conformance class scope** (Sprint 2). Architect ratifies **Sprint-1-style minimal (4 @Tests)** parallel to SystemFeatures pattern: `subsystemsResourcesEndpointReturnsCollection` (CRITICAL) + `subsystemCanonicalEndpointReturnsBaseShape` + `subsystemHasParentSystemLink` (UNIQUE-TO-SUBSYSTEMS — the architectural invariant distinguishing subsystems from sibling collection types) + `subsystemHasCanonicalLink`. The `dependsOnGroups="systemfeatures"` wiring (CRITICAL SCENARIO-ETS-PART1-003-SUBSYSTEMS-DEPENDENCY-SKIP-001) is testng.xml + `@BeforeSuite` defense-in-depth per §16.2 above. Sprint 5+ expansion targets ~3-5 additional ATS items (canonical-url depth, location-time, cross-system queries) — mechanical extensions BATCHED with Procedures/Sampling/Properties/Deployments siblings. Coverage scope rationale: third pattern extension AND first multi-level dependency chain compound risk surface; minimal per-class @Test count concentrates Generator + gate verification effort on the cascade verification.

### 16.5 Surfaced question resolution — Architect chown-scratch rebuild

**Resolved: SKIPPED per autonomous-loop mitigation pattern**. Pat's surfaced suggestion (a) recommended an optional 5-min Architect scratch rebuild verifying chown-layer attack materializes the predicted ~80MB savings BEFORE Generator commits. Architect skipped per orchestrator-imposed budget constraint (no docker / no live network ops in 25-min wall-clock window). Sprint 3 empirical evidence at `~/docker/gir/ets-ogcapi-connectedsystems10/ops/test-results/sprint-ets-03-04-empirical-dedupe-list-2026-04-29.txt` (chown-layer 80MB identification) + Generator runtime verification mandate in S-ETS-04-02 acceptance criteria are sufficient pre-commit signal. PARTIAL outcome (600-650MB) is acceptable per Sprint 4 contract `success_criteria.image_size_under_600mb` PARTIAL band; tier-2 version-overlap dedupe + alpine-roadmap fallbacks documented in ADR-009 v2 amendment.

### 16.6 Architecture-level guidance for Generator (Sprint 4)

- **Worktree-pollution constraint** (preserved from §15.5): ALL Sprint 4 work (Generator + Quinn + Raze) operates against `/tmp/` clones or archived artifacts; NEVER against `~/docker/gir/ets-ogcapi-connectedsystems10/`.
- **Historical Generator sequencing**: Sprint 4 included a proposed CI activation step. CP-003/ADR-012 permanently retire that step; the remaining security, Docker, and conformance work is unaffected.
- **ADR cardinality**: 10 ADRs at Sprint 3 close; Sprint 4 adds 0 new ADRs (in-place v2 amendments to ADR-009 + ADR-010 per Architect ratification). Pat's hypothetical `_bmad/adrs/INDEX.md` trigger remains 11+ ADRs; defer to Sprint 5+.
- **Two-level cascade verification is BLOCKING** for S-ETS-04-05 close. If TestNG `<group depends-on>` cascade does NOT work, Generator MUST activate the `@BeforeSuite` fallback (no Architect re-cycle required; pattern is pre-ratified per §16.2). Document the resolution path in S-ETS-04-05 Implementation Notes.
- **Stub-IUT script reuse**: `scripts/stub-iut.sh` is a NEW shared primitive serving BOTH S-ETS-04-03 (credential-leak; echoes Authorization header) AND potentially S-ETS-04-05 sub-tests if GeoRobotix returns 404 on `/systems/{id}/subsystems` (synthetic Subsystems response for assertion verification — defer this extension to Sprint 5+ unless GeoRobotix 404 surfaces in Generator curl-verification).

## 17. Last reconciled

**2026-07-22** — External SWE Common and SensorML validator architecture appended (§21). Adapter-first dependency boundary, homegrown-validation replacement path, upstream uncertainty, and runtime-closure gates reconciled. Re-reconcile required if >30 days stale per AGENTS.md.

## 18. Architecture v2.0.4 — Sprint 25 Part 2 taxonomy correction (2026-05-09)

Sprint 25 planning corrected the Part 2 scope model after re-checking OGC 23-002 Annex A. The standard defines the Part 2 Advanced Filtering conformance class as `/conf/advanced-filtering` with requirements class `/req/advanced-filtering`; it does not define `/conf/system-history` or `/req/system-history`.

Architectural consequence: the ETS shall not add an OGC 23-002 System History TestNG group. GeoRobotix's `/conf/system-history` declaration is treated as non-standard/vendor extension evidence only. The active Part 2 backlog now tracks the OGC 23-002 conformance classes plus explicitly scoped project cross-class closures, not the stale v1.0 web-app count.

**2026-05-09** — Sprint 25 taxonomy correction appended (§18). v2.0 sections 1-13 unchanged except the overview sentence now avoids the stale "14 Part 2 conformance classes" count. Re-reconcile required if >30 days stale per AGENTS.md.

## 19. Architecture v2.0.6 — TeamEngine 6 runtime migration planning (2026-07-20)

Sprint 41 replaces the forward runtime decision, not the historical evidence. ADR-007 remains the record of why TeamEngine 5.6.1 was manually assembled on Tomcat 8.5 and why that baseline was accepted. ADR-011 supersedes that runtime for future implementation and supersedes only ADR-009's Tomcat 8.5 stage; ADR-009's multi-stage builder, reproducibility, minimal-copy, and non-root principles remain binding.

The target runtime is an immutable digest of the OGC-published TeamEngine 6.0.0 development image. The digest must be accompanied by recorded TeamEngine, Tomcat, and JDK versions and a refresh procedure. Mutable tags are discovery aliases, not release provenance.

The runtime dependency boundary is empirical. Generator must compare the Maven runtime dependency tree with the pinned image library inventory before excluding TeamEngine artifacts. The architectural target is an explicit exclusion list, preferably expressed through Maven scope or exclusions where practical. A broad `teamengine-*.jar` deletion is not accepted without evidence that every matched artifact is supplied compatibly by the image.

Dockerfile, Compose, and smoke harness form the supported deployment contract. Their port, health endpoint, startup command, environment, install paths, effective runtime identity, selected dependency payload, and run-argument forwarding must align or have documented, tested differences. The Maven Docker profile is not a fourth runtime surface; it must be removed, made a no-op, or delegated to the same Dockerfile path.

At planning time on 2026-07-20, the migration remained planned until Maven verification, image build and filesystem checks, non-root startup, health, SPI/CTL registration, linkage-error inspection, and full TeamEngine execution against the primary local OSH IUT were archived. The 2026-07-21 readiness pass archived the non-IUT TeamEngine 6 gates, and the 2026-07-22 final local OSH run archived the remaining deployed execution gate. Sprint 40 TeamEngine 5.6.1 results remain historical baseline evidence and are not used for Sprint 41 closure.

**2026-07-20** — Architecture freshness reconciled after the required greater-than-30-day warning. CP-001, ADR-011, and S-ETS-41-01 establish the new decision and verification boundary.

## 20. Architecture v2.0.7 — Policy-guidance and Raze alignment (2026-07-21)

This section appends to v2.0.6 and incorporates the policy-guidance/Raze findings from `.harness/evaluations/teamengine-policy-guidance-adversarial-2026-07-21.yaml`. It does not promote Sprint 41 to implemented; it tightens the implementation contract for Generator.

### 20.1 Canonical run-argument contract

The suite has one canonical run-argument contract:
- required `iut`
- optional `auth-credential`
- optional `mutation-tests-enabled`
- optional `mutation-iut-policy`
- optional `mobile-system-id`
- optional `subdeployment-association-evidence`

CTL, TestNG XML, Java enum/docs, smoke harness docs, README, Javadoc, site docs, and sample test-run-props must converge on those keys. Human-facing UI text may describe `iut` as the "CS API landing page"; that label must not leak into serialized TestNG argument names as `iut-url`. `auth-type` must not appear in public docs, CTL, sample run props, or testng defaults unless a later code change implements and verifies that argument.

### 20.2 TeamEngine 6 deployment authority

Dockerfile + `docker-compose.yml` + `scripts/smoke-test.sh` are the authoritative TeamEngine 6 deployment contract. They define the runtime image digest, install paths, dependency payload, startup/health behavior, non-root identity, and run-argument forwarding.

The Maven `docker` profile is stale if it performs an independent TeamEngine assembly or copies Maven dependencies broadly. Generator must remove it, make it a no-op/delegation to the authoritative Dockerfile path, or otherwise prove it cannot bypass the selected-payload policy. No broad dependency-copy path is allowed for TeamEngine 6.

### 20.3 Public metadata and documentation as package artifacts

Public TeamEngine metadata and docs are part of the conformance package. Generator must remove archetype placeholders from:
- CTL suite title/description/defaults
- `src/main/config/teamengine/config.xml`
- `src/site/asciidoc/*.adoc`
- Javadoc overview
- `README.adoc`
- `src/main/config/test-run-props.xml`
- `src/main/resources/test-run-props.xml`

These surfaces must describe the actual OGC API Connected Systems ETS: partial OGC 23-001 Part 1 coverage, partial OGC 23-002 Part 2 coverage where implemented, TeamEngine 6 forward-runtime status, local OSH as the primary development E2E target, GeoRobotix as advisory only, and the real run arguments from §20.1.

### 20.4 Sprint 41 implementation status gate

Documentation cleanup and metadata alignment were necessary but insufficient at the 2026-07-21 readiness checkpoint. The 2026-07-22 local OSH TeamEngine 6 run supplied the missing E2E evidence (`211/69/0/142`, zero writes, zero startup errors). A later final Raze review reopened Sprint 41 because the first candidate added a duplicate TeamEngine resources coordinate family. Versions 2.0.12 through 2.0.15 remove that family and close the generic inventory, resource isolation, inert OGC release-definition, and exact multi-tuple findings. Version 2.0.16 records final Raze `APPROVE` at `0.99` confidence and Sprint 41 completion.

### 20.5 Generator constraints from Raze findings

- Fix RAZE-POLICY-001 through one canonical run-argument contract; do not add `auth-type` support by documentation alone.
- Fix RAZE-POLICY-002 by retiring or delegating the Maven `docker` profile; do not allow fabric8/dependency-copy behavior to define a second runtime path.
- Fix RAZE-POLICY-003 and RAZE-POLICY-004 by replacing archetype placeholders and stale README claims with actual Part 1/Part 2 partial coverage and TeamEngine 6 status.
- Preserve Sprint 41 status honesty from RAZE-POLICY-005: no "implemented" or "final" claims until local OSH TeamEngine 6 E2E is archived.
- Carry RAZE-POLICY-006 and RAZE-POLICY-007 forward in the implementation handoff: this Architect turn was explicitly barred from editing ops or older generator handoff state, but those stale handoff details must not guide implementation.

**2026-07-21** — v2.0.7 reconciles the architecture with Raze policy findings and hands concrete constraints to Generator. Re-reconcile required if implementation diverges from this contract.

**2026-07-21** — v2.0.8 records the Sprint 41 readiness pass: Maven, image
build, runtime verifier, Compose health, and suite metadata are verified. Version
2.0.11 records the 2026-07-22 primary local OSH E2E pass that cleared the final
Sprint 41 gate.

## 21. Architecture v2.0.9-v2.0.16 - External domain validator integration (2026-07-22)

This section records S-ETS-42-01, S-ETS-42-02, and
`REQ-ETS-VALIDATOR-001`. Version 2.0.9 established the provisional decision;
version 2.0.10 reconciles the first SWE Common adapter implementation; version
2.0.11 records the first primary local OSH E2E run; version 2.0.12 records the
duplicate-family gapfix; version 2.0.13 records the generic jar-guard gapfix;
version 2.0.14 records the first metadata gapfix; version 2.0.15 closes its
inert OGC release-definition and executable multi-tuple findings; version 2.0.16 records final
Raze approval and story closure.

### 21.1 Upstream state

SWE Common has a plausible reusable module in `opengeospatial/ets-swecommon30` PR 10. The correct target is `org.opengis.cite:swecommon30-validator:0.1-SNAPSHOT` from branch `issue-9-swecommon-validation-module`, observed at `3ba75ceabe57cea85f4a8513c59e0f90e386ba96` on 2026-07-22. It is not published to Maven Central yet.

SensorML remains uncertain. The GitHub user `FCU-GIS-Luke` exists but exposes no public repositories, and no public SensorML validator module was found under that username. `opengeospatial/ets-sensorml30` exists at `d2b2a6308fdf48f113f7c7faed6712dc05e33130`, but it is a TeamEngine ETS scaffold, not a reusable validator module. Do not import it directly as a dependency.

### 21.2 Adapter boundary

External validators validate domain schema semantics only. This ETS keeps ownership of:

- CS API resource discovery and candidate selection
- `/conformance` declaration and prerequisite gating
- exact media-type checks for `application/sml+json`, `application/swe+json`, `application/swe+text`, and `application/swe+binary`
- Connected Systems mapping assertions and relation-type checks
- Observation/Command parent-child binding evidence
- TestNG pass/fail/skip policy and TeamEngine reporting
- no-mutation and public-IUT hard-denial policy

The implemented local boundary is
`validation.swecommon.ConnectedSystemsSweValidatorAdapter`. It delegates a
`JsonNode` to upstream `SweCommonJsonSchemaValidator` with `sweCommon.json`, then
converts NetworkNT messages into the immutable, sorted, ETS-owned
`SweValidationResult`. TestNG, `ETSAssert`, requirement URIs, and skip/fail policy
do not enter the adapter. Upstream resource/configuration exceptions propagate as
suite `IllegalStateException` values rather than IUT conformance diagnostics.
`ConnectedSystemsSensorMlValidatorAdapter` remains deferred until a real reusable
SensorML module exists.

### 21.3 Replacement path

The first replacement increment is implemented as dual validation:

- Adapter tests cover valid Count/DataRecord components, invalid components,
  immutable deterministic diagnostics, API type isolation, and missing-schema
  operational failure.
- `Part2SchemaValidation` extracts `recordSchema` only after the complete
  Connected Systems Observation/Command wrapper passes local validation.
- The six JSON/Text/Binary Observation/Command schema assertion paths invoke the
  reusable validator for that extracted component and retain their active OGC
  23-002 requirement URI for any conformance failure.
- Keep Observation/Command encoded-body semantic checks skipped until an external validator explicitly supports those encoding rules and real parent/child evidence exists.
- Replace minimal SensorML shape checks in `SensorMlTests` with full SensorML 3.0 validation only after FCU/OGC provide reusable module coordinates or upstream splits `sensorml30-validator` from the SensorML ETS.
- Retain Connected Systems-specific mapping, relation-types, media-type write, and binding checks locally.

### 21.4 Runtime closure

Validator dependencies stay inside the ETS runtime closure and do not modify
TeamEngine-owned files. Until publication, supported builds run
`scripts/bootstrap-swecommon30-validator.sh`, verify exact commit
`3ba75ceabe57cea85f4a8513c59e0f90e386ba96`, and build only the upstream parent
plus validator module. The POM excludes upstream NetworkNT 1.5.4 and Jackson
2.17.2, uses the ETS-managed NetworkNT 1.5.9/Jackson closure, and shades the
validator class/resources with relocated NetworkNT/ITU packages into the slim
ETS jar. The final image contains no standalone validator jar. The runtime
verifier compares duplicate-prone jar-family inventories and inspects the shaded
class/resource payload.

The Dockerfile, Docker Maven wrapper, GitHub workflow template, and Jenkins build
job invoke the same bootstrap before resolving this project. A release build must
not ship with the provisional `0.1-SNAPSHOT` dependency; Maven publication is
blocked until the reusable upstream module has an accepted non-SNAPSHOT
repository coordinate.

The upstream API still creates its default Draft 2020-12 validator without
format assertions, and `encodings.json` does not select BinaryEncoding from its
root. Local wrapper, format, JSON/Text/Binary encoding, media-type, mapping, and
binding checks therefore remain. Removing bundled SWE validation is expressly
deferred until external-only parity and complete encoding support exist.

### 21.5 Verification and gate closure

Docker Maven passes `311/0/0/3`; focused adapter/parity verification passes
`19/0/0/0`. Final Raze then found that the first closure image added TeamEngine
resources 6.0.0 beside the immutable base's 6.0.0-RC2 jar, duplicating 89 of 89
functional paths. Version 2.0.12 removes that GA jar and selected-dependency
payload, requires coordinate-family multiset parity, and emits exact image IDs
directly from smoke and runtime verification. Replacement image
`sha256:b52f4897c553f5d3e37caf62fa14765a774b17f943243be3d99c5d89eec5dcb3`
passes the deployed valid/invalid adapter probe, coordinate-aware dependency
parity, immutable-base checks, and primary local OSH E2E `211/69/0/142` with
135 recognized requests and zero writes. The subsequent Raze recheck found that
coordinate scanning still trusted jar filenames and that the shaded ETS jar
collided with base NetworkNT message-bundle paths. S-ETS-41-01 and S-ETS-42-02
therefore remain in progress. `REQ-ETS-VALIDATOR-001` also remains partial
because SensorML is deferred pending a reusable FCU/OGC module.

### 21.6 Replacement recheck gap

Runtime verification must inventory all added jars without trusting filenames,
scan every embedded Maven coordinate, and intersect functional paths against all
base jars with only a narrow, rationale-bearing coordinate-plus-path allowlist.
The shaded NetworkNT `jsv-messages*.properties` bundles must move to an
ETS-unique path, and relocated bytecode must reference that path. A new image and
fresh Maven, runtime, local OSH, and Raze evidence are required. Image
`sha256:b52f4897c553f5d3e37caf62fa14765a774b17f943243be3d99c5d89eec5dcb3`
is historical replacement evidence. Version 2.0.13 implements this generic
contract in image
`sha256:9a34fd4abda872637635271b3f17a977ec3b0c0928fc70b83b0980d20e98f50e`,
but its Raze recheck requires explicit accepted-tuple output, unused-allowlist
rejection, Jenkins profile cleanup, and final chronology reconciliation.
Version 2.0.14 closes those findings: exact image
`sha256:05a592e0f09de6dfb18f3c01457c7f2dcdcdb635d16ff672485130c32b9b988d`
emits both exact accepted coordinate/path tuples, rejects unused entries, and
passes fresh Maven `312/0/0/3`, adapter execution, and local OSH E2E
`211/69/0/142`. Its Raze recheck found that release Jenkins still requested
undeclared profiles and used JDK 8 without the validator bootstrap, while the
guard self-test did not assert complete multi-tuple stdout. Version 2.0.15
closes those findings: both Jenkinsfiles use Java 17, invoke the source-pin
bootstrap, and request only project-declared profiles; the self-test asserts an
exact sorted two-tuple result. Exact image
`sha256:829a97414c07dd5763ed302e32b3178d301ca098bc9025f4b1f58b692ddad5f9`
passes fresh Maven `312/0/0/3`, runtime verification, adapter execution, and
local OSH E2E `211/69/0/142`. It is ready for final Raze.

Final Raze recheck
`.harness/evaluations/sprint-ets-42-final-raze-gapfix-adversarial-recheck-2026-07-22.yaml`
returned `APPROVE` at `0.99` confidence with no required actions. S-ETS-41-01
and the SWE Common S-ETS-42-02 increment are complete. The overall
`REQ-ETS-VALIDATOR-001` remains partial because SensorML integration is deferred
until FCU/OGC provides a reusable module.

**2026-07-22** — v2.0.9 documents the external SWE Common/SensorML validator architecture and the plan to replace local schema validation through adapters once upstream artifacts are reproducible.

**2026-07-22** — v2.0.10 reconciles the source-pinned SWE Common adapter,
dual-validation call sites, shaded runtime closure, verification evidence, and
the initially blocked primary local OSH gate.

**2026-07-22** - v2.0.11 closes the SWE Common implementation gate with primary
local OSH TeamEngine evidence `211/69/0/142`, exact-image runtime verification,
and zero-write evidence across 135 recognized IUT requests.

**2026-07-22** - v2.0.12 reopens that closure after final Raze found the
cross-version TeamEngine resources duplicate, removes the added GA jar, adds
coordinate-aware parity and direct image-ID evidence, and records the passing
replacement image pending final Raze recheck.

**2026-07-22** - v2.0.13 records the replacement recheck gap: generic added-jar
metadata/content enforcement, isolated message bundles, and fresh full gates are
required before closure; it then records the verified generic jar-guard candidate.

**2026-07-22** - v2.0.14 records explicit accepted collision tuples,
unused-allowlist rejection, build-Jenkins profile cleanup, and exact-image
Maven/runtime/local-OSH evidence; final Raze then identified release-Jenkins and
executable tuple-output coverage gaps.

**2026-07-22** - v2.0.15 records Java 17/bootstrap/declared-profile coverage for
both Jenkinsfiles, exact behavioral multi-tuple assertions, and fresh passing
Maven/runtime/local-OSH evidence pending final Raze.

**2026-07-22** - v2.0.16 records final Raze `APPROVE` at `0.99` confidence,
closes S-ETS-41-01 and the SWE Common S-ETS-42-02 increment, and preserves the
overall validator requirement as partial for deferred SensorML integration.

## 22. Architecture v2.0.18 - Populated local OSH evidence (2026-07-23)

CP-004/S-ETS-44-01 implement populated local OSH as a supplemental, ephemeral
E2E target. The clean `field-hub-osh-1` instance remains the authoritative
primary development gate.

### 22.1 Boundary

- OSH source and installed binaries are external and immutable.
- The workflow may derive runtime configuration and create fixtures through the
  public Connected Systems API.
- The external OSH install is mounted read-only into a separate container.
- The primary OSH state directory is never mounted by the populated workflow.
- TeamEngine remains the immutable OGC base plus ETS extension artifacts.

### 22.2 Execution topology

```text
clean OSH checkout/install (read-only)
             |
             v
ephemeral OSH container + isolated state
             |
             +-- loopback random port --> ETS fixture seeder
             |
             +-- field-hub_default -----> TeamEngine 6 + Connected Systems ETS
                                                   |
                                                   v
                                      TestNG XML/log/exact verdict

cleanup ephemeral OSH/state
             |
             v
clean field-hub-osh-1 TeamEngine smoke
```

### 22.3 Verdict model

The workflow publishes two independent results:

- **Provisioning readiness**: exact fixtures were accepted and required
  resources/schema/body evidence is observable.
- **Conformance verdict**: exact TeamEngine/TestNG totals.

Provisioning readiness may PASS while conformance FAILS. The process remains
non-zero for any TeamEngine failure. This prevents the infrastructure gate from
masking an IUT defect.

### 22.4 Current unmodified-IUT evidence

The planning probe created four static resources plus one DataStream,
Observation, and ControlStream through supported HTTP APIs. TeamEngine executed
all 211 methods and returned `86 passed / 28 failed / 97 skipped`. All 28
failures were strict Annex A.9/SWE paths reached by the populated resources:
DataStream items omitted `live`; ControlStream items omitted `issueTime`,
`executionTime`, `live`, and `async`. Those defects remain IUT evidence.

### 22.5 Implemented ownership and failure model

The production workflow generates unique per-run OSH and TeamEngine names,
labels every owned container, captures exact container IDs, and removes only
IDs whose labels, names, and mounts still match the ownership record. Existing
names are refused, including the primary OSH. The seeder accepts only the
Docker-published loopback port proven by an orchestrator-generated ownership
record for the isolated container and state source.

Every started attempt enters one finalizer that independently attempts owned
cleanup, normalized primary identity/state comparison, clean-primary TeamEngine,
and summary generation. Cleanup or finalization failures cannot mask the
originating phase and force an overall non-zero verdict. TestNG conformance is
derived only from validated XML; infrastructure and overall workflow verdicts
are separate.

The final fresh-clone evidence uses OSH source
`4c87a65c9a967d52af9df476e65d7862c7673a15`, installed ConSys build
`4c87a65`, pinned runtime image reference
`maven:3.9-eclipse-temurin-17@sha256:1ed5d1f54416b706707b4f3238f63a20bb06aab27c6d240090a2bb9ad895ed45`,
and TeamEngine image
`sha256:cc8c9d711e57ed50d2ed08cdef01cb1236052e775ff27ad016185672e9de8169`.
Provisioning passed; populated TestNG failed `211/91/28/92`; cleanup and primary
isolation passed; clean-primary TestNG passed `211/69/0/142`. Therefore the
reproducible E2E architecture is implemented while positive binding conformance
remains open.

Focused Raze recheck approved this workflow at confidence `0.99`; all ten
initial safety/evidence findings are closed and no new findings remain.

## 23. Architecture v2.0.20 - Released ATS coverage authority (2026-07-26)

ADR-013 establishes approved OGC 23-001 and OGC 23-002 version 1.0 as the
certification source of truth. Their reproducible source form is
`opengeospatial/ogcapi-connected-systems` tag `v1.0.0`, commit
`8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`.

The coverage subsystem has two layers:

```text
released Annex A source
        |
        v
deterministic semantic inventory
  part + class + test + target
        |
        v
compiled TestNG annotation audit ---- reviewed exact/helper mapping
        |                                      |
        +-------------- coverage report <------+
                               |
                               v
                  specs, traceability, backlog
```

The source extractor must retain all 240 released abstract tests: Part 1 has 13
classes, 108 class tests, and two target-less supporting tests; Part 2 has 12
classes and 130 tests. The manifest is keyed by part and identifier because
class/test paths such as `/conf/api-common` occur in both standards.

Compiled annotation inspection is required because TestNG descriptions are
assembled from Java constants. A canonical target match is classified as a
candidate mapping. It becomes implemented only after an exact method or helper
mapping is recorded and reviewed against the complete published test method.

The newer Connected Systems commit `3fd86c73...` remains the separately
documented OpenAPI input pin. It is not an ATS authority. Later drafts, IUT
declarations, and frozen web-app registries cannot alter the released coverage
surface.

## 24. Architecture v2.0.21 - Part 1 API Common direct procedures (2026-07-26)

Released OGC 23-001 `/conf/api-common` is a distinct ETS component from the
existing inherited OGC API Common tests:

```text
conformance.common.CommonTests
  inherited OGC API Common assertions
                 |
                 +---+
                     v
core ----------> part1apicommon ----------> systemfeatures
                     |                            |
                     v                            v
          Part1ApiCommonSupport          current Part 1 DAG
```

The `part1apicommon` group depends on both `core` and `common`.
`systemfeatures` depends on `part1apicommon`, making the released foundation a
transitive prerequisite for all current System Features descendants without
duplicating dependency declarations.

`Part1ApiCommonSupport` owns bounded, read-only pagination for canonical
resources and collection items. It is intentionally independent of encoding
validators: SWE Common and SensorML adapter responsibilities remain behind the
external validator boundaries defined in Sprint 42. The support layer may
issue GET requests only and must fail on pagination cycles, excessive pages,
cross-origin next links, non-200 supported endpoints, or malformed JSON
collection bodies. It negotiates the representation set allowed for each
canonical resource type and parses GeoJSON `features`, SensorML JSON `items`,
and recognized extension JSON wrappers according to the actual response media
type.

Date-time conformance is evidence-sensitive. Collections lacking temporal
extent are ineligible rather than passing. Every eligible collection is queried
with instant, bounded, open-start, and open-end forms. Returned features with
`validTime` must intersect each query, `now` is resolved at the captured request
time, and every timeless feature found by the unfiltered traversal must remain
in every filtered result. Zero executed queries yields SKIP.

This component closes the four directly owned tests and two supporting tests in
the Connected Systems inventory only. Full `/conf/api-common` conformance
remains partial until the five inherited external OGC API Features/Common
classes are completed and reviewed.

## 25. Architecture v2.0.22 - Part 1 System direct procedures (2026-07-26)

System Features consumes API Common's bounded traversal as a public read-only
service. The original helper signatures remain the reviewed supporting-test
entry points. Detailed variants return each parsed page together with its
source URI and actual response media type, allowing class-specific schema
validation without repeating network requests.

`SystemFeaturesTests` contains exactly the six released `/conf/system`
procedures. `SystemFeaturesSupport` owns GeoJSON/SensorML extraction, the five
allowed SOSA System URI/CURIE pairs, canonical-link normalization, same-origin
canonical URL resolution, and one endpoint-parameterized NetworkNT validation
procedure against the bundled Part 1 System collection schemas. The GeoJSON
dependency closure is a pinned complete copy of the schemas published at
`geojson.org/schema`; permissive resolution stubs are prohibited.

Class setup loads only immutable run arguments. Each released test retrieves
its own prerequisite resources, so a canonical `/systems` failure cannot
configuration-skip collection-only procedures or an absent-input
location-time result. Collection procedures accumulate unsupported-media
warnings, continue through later collections, and SKIP only when no supported
collection was executed.

Location is warning-only because its target is a recommendation. Location-time
is never inferred from static shape. It requires optional `mobile-system-id`,
polls a canonical System GET at one-second intervals for at most 30 seconds,
and passes only after an actual GeoJSON geometry or SensorML positional
coordinate change. Orientation-only or reference-frame metadata changes do not
prove movement. Missing input is SKIP. No OSH or TeamEngine source or binary
changes are part of this component.

## 26. Architecture v2.0.23 - Inherited evidence-limitation isolation (2026-07-26)

The unmodified local OSH hard-codes `/collections` without temporal extents.
Consequently `/conf/api-common/datetime` reports the specified
no-positive-evidence SKIP. TestNG's raw group dependency semantics treat that
single evidence limitation like a failed prerequisite and dependency-SKIP all
System methods, preventing E2E execution of Sprint 47 code.

The `part1apicommon -> systemfeatures` dependency remains declared to preserve
ordering. System configuration and tests run with TestNG `alwaysRun` semantics,
then apply an explicit result gate before System IUT access. Any prerequisite
test or configuration failure blocks System. Any skipped API Common procedure
other than `datetimeUsesValidTime` blocks System. The sole allowed continuation
is that method's documented no-temporal-extent `SkipException`; it remains
visible in the report and therefore prevents any full inherited-conformance
claim. This isolates procedure execution from evidence availability without
converting SKIP into PASS.

All six deployed System methods also execute through a controlled direct HTTP
regression. That fixture proves successful location, positional movement,
canonical dereference/content comparison, resources validation, canonical
endpoint validation, and collection validation while asserting the expected
wire exchanges. Local OSH TeamEngine remains the mandatory primary E2E target.

## 27. Architecture v2.0.25 - Part 1 Subsystem graph procedures (2026-07-26)

`SubsystemsTests` replaces its historical four shape/link assertions with the
five released `/conf/subsystem` procedures. Shared setup only normalizes the API
root and evaluates inherited results. Each test discovers its own collection,
hierarchy, or association evidence so a no-data result cannot suppress
unrelated procedures.

`SubsystemsSupport` is a read-only graph and collection service. It uses bounded
pagination, unique local IDs, explicit direct-parent edges, node/page limits,
iterative cycle rejection, and shortcut-edge rejection when a direct child is
also reachable by a path of length two or more. Expected recursive closure
comes only from walking default direct-subsystems endpoints; the
`recursive=true` response under test is never used as its own oracle. Direct
children and transitive descendants remain separate evidence sets.

The collection procedure uses a bounded, media-gated subcollection probe to
identify a parent with actual children without requiring those returned
representations to expose local IDs. It then resolves exactly one parent
`rel=subsystems` link occurrence and requires its resolved target to exactly
match the already-probed normative nested endpoint. Duplicate-identical links,
trailing slashes, queries, fragments, and cross-origin variants fail. HTTP 200
and actual Content-Type are checked on every traversal page before parsing;
unsupported or absent media warns and SKIPs. The accepted first response is
reused rather than fetched again. Supported GeoJSON/SensorML collections reuse
`SystemFeaturesSupport` schema validation, including schema-valid SensorML
items without an `id` member for collection validation. Recursive hierarchy
discovery applies the same per-page gate to root `/systems` and every nested
subsystem traversal before extracting required local graph IDs. The
recursive-parameter procedure checks only exact boolean requests and HTTP
success. Recursive graph procedures require positive hierarchy evidence and
compare default, false, and true result sets against the independently
discovered graph. Missing evidence SKIPs instead of passing vacuously.

Association closure covers every discovered parent and each implemented
Sampling Feature, DataStream, and ControlStream endpoint. Implementation is
established independently by the canonical top-level endpoint. Once that
endpoint returns HTTP 200, every applicable nested parent and descendant
endpoint must return HTTP 200; all-parent 404 cannot become an unsupported-type
SKIP. Parent results must contain every resource ID observed through descendant
endpoints. Empty descendant association sets are evidence limitations, not
PASS.

The `systemfeatures -> subsystems` TestNG ordering remains. Subsystem methods
run with an explicit prerequisite-result gate that permits only the exact
already documented API Common datetime, System unsupported-media, and
missing-mobile-input SKIPs. All failures, configuration failures, and other
SKIPs block before Subsystem IUT access. Allowed inherited SKIPs remain visible
and prevent a full inherited-conformance claim.

Primary E2E remains Dockerized TeamEngine against unmodified local OSH. Its
root System collection currently uses unsupported `application/json`, so the
expected evidence is one boolean-request PASS and four hierarchy-procedure
media-gate SKIPs. A controlled read-only multi-level fixture must execute all
five positive paths, including all three association resource types. This
architecture adds no OSH or TeamEngine source or binary changes.

## 28. Architecture v2.0.26 - Part 1 Deployment procedures (2026-07-26)

`DeploymentsTests` replaces four eager historical approximations with the five
released `/conf/deployment` procedures. Setup retains only the normalized API
root. Each method independently discovers collections, canonical resources, or
Systems so one missing input cannot configuration-skip unrelated procedures.

The dependency changes from `systemfeatures -> deployments` to the class's
released inheritance boundary, `part1apicommon -> deployments`. API Common
failures and unexpected SKIPs block before IUT access. Its exact documented
datetime no-evidence SKIP remains visible while direct Deployment procedures
execute. System resources are retrieved inside `ref-from-system`, as required,
instead of making all five procedures depend on unrelated System ATS outcomes.

`DeploymentFeaturesSupport` owns collection selection, canonical link/path
resolution, canonical-content normalization, representation-specific System
link matching, and media-to-schema dispatch. `Part1ApiCommonSupport` remains the
single bounded, same-origin pagination implementation and gains an
actual-media-gated collection-items entry point for schema-controlled callers.
Every selected collection and every page is processed; zero selected
Deployment collections fail rather than pass vacuously.

The schema backend uses the released bundled GeoJSON/SensorML Deployment
collection schemas behind the support boundary. This preserves the external
validator architecture: a future `ConnectedSystemsSensorMlValidatorAdapter`
replaces only SensorML schema semantics after FCU/OGC provides a reusable
module. TestNG policy, endpoint discovery, canonical equivalence, pagination,
and Connected Systems mappings stay local. The SWE Common component adapter
does not validate whole SensorML Deployment documents, and no executable
SensorML ETS jar enters the runtime closure.

Primary TeamEngine E2E executes against unmodified local OSH and intentionally
retains its observed failures: no `sosa:Deployment` collection, unsupported
generic `/deployments` media, and HTTP 400 for the nested System endpoint.
Controlled read-only HTTP coverage proves all five successful procedures and
the key fail-closed branches. No OSH or TeamEngine source or binary change is
part of this architecture.

## 29. Architecture v2.0.27 - Part 1 Procedure procedures (2026-07-26)

`ProceduresTests` replaces four eager historical approximations with the five
released `/conf/procedure` procedures. Setup retains only the normalized API
root. Each method independently retrieves canonical Procedure pages or
advertised Procedure collections, preventing one missing evidence source from
configuration-skipping unrelated procedures.

The dependency changes from `systemfeatures -> procedures` to the released
inheritance boundary, `part1apicommon -> procedures`. Core, Common, and API
Common failures or unexpected SKIPs block before IUT access. The documented API
Common datetime no-evidence SKIP remains visible while direct Procedure
procedures execute. SystemFeatures and sibling outcomes cannot block Procedure.

`ProcedureFeaturesSupport` owns collection selection, collection metadata,
Procedure type extraction, media-specific location absence, canonical
link/path resolution, canonical-content normalization, and media-to-schema
dispatch. `Part1ApiCommonSupport` remains the single bounded, same-origin
pagination implementation. Schema-controlled collection paths use its
restricted media entry point so GeoJSON or SensorML is selected ahead of an
earlier generic JSON link.

The location procedure traverses all canonical `/procedures` pages and gates
actual media before parsing. GeoJSON requires `geometry` to be JSON null;
SensorML forbids the `position` member. The collections procedure requires
`itemType=feature`, `featureType=sosa:Procedure`, one of the nine released
Procedure type URI/CURIE values, and the matching released Procedure collection
schema on every page.

Canonical comparison requires every canonical relation occurrence to resolve
on the IUT origin to `{api_root}/procedures/{id}`. Duplicate occurrences and
representation queries are allowed only for that exact identity. The first
occurrence with no advertised media type or one matching the collection page
media type is dereferenced; no comparable occurrence warns and SKIPs. Content
is compared after canonical links are removed from both resources, and a
`links` member emptied by that removal is normalized to omitted.

The schema backend uses bundled released GeoJSON/SensorML Procedure collection
schemas behind the support boundary. A future
`ConnectedSystemsSensorMlValidatorAdapter` replaces only SensorML schema
semantics after a reusable FCU/OGC module exists. TestNG policy, endpoint
discovery, pagination, canonical equivalence, and Connected Systems mapping
remain local. No executable SensorML ETS jar enters the runtime closure.

Primary TeamEngine E2E executes against unmodified local OSH and intentionally
retains unsupported generic `/procedures` media and missing
`sosa:Procedure`-collection outcomes. Controlled read-only HTTP coverage proves
all five successful procedures and fail-closed location, media, type,
canonical, and collection branches. No OSH or TeamEngine source or binary
change is part of this architecture.

Implementation reconciliation confirms all five methods are deployed and
reviewed exact. The local OSH result is `218/39/5/174`, with the expected three
Procedure media SKIPs and two collection failures. API Common sabotage skips
Procedure setup and all five methods before Procedure IUT access. The exact
image passes immutable TeamEngine runtime and deployed SWE Common adapter
checks; no external source or binary was changed.

## 30. Architecture v2.0.31 - Part 1 Subdeployment graph procedures (2026-07-27)

`SubdeploymentsTests` replaces four eager historical approximations with the
five released `/conf/subdeployment` procedures. Setup retains only the
normalized API root. Each method independently discovers the hierarchy,
recursive result, or association evidence it needs, so missing evidence in one
procedure cannot configuration-skip another.

The released dependency remains `deployments -> subdeployments`. The defensive
setup gate recognizes only Core, Common, Part 1 API Common, and Deployment
prerequisite outcomes. Unrelated sibling groups cannot become implicit
prerequisites. Deployment sabotage must skip setup and all five methods before
Subdeployment IUT access.

`SubdeploymentsSupport` owns a bounded, same-origin Deployment graph built from
the root collection plus every direct Subdeployment endpoint. Every hierarchy
page is status/media gated before parsing and validated through
`DeploymentFeaturesSupport`. Duplicate identities, cycles, non-direct shortcut
edges, and safety overflow fail closed.

The collection procedure requires exact same-origin
`/deployments/{id}/subdeployments` relation targets and schema-valid collection
pages. Recursive search uses exact ID-set equality: root default/false equals
roots, root true equals all nodes, child default/false equals direct children,
and child true equals all descendants.

Recursive association closure uses immutable run-argument fixture evidence for
resources owned directly by the parent and by every descendant for each of the
five released association relations. It examines every occurrence, refuses
cross-origin candidates, and selects a same-origin JSON-compatible or untyped
negotiable link. The parent endpoint must include the complete fixture union.
Missing fixture evidence or no safe comparable link SKIPs with warning; an
observed omission fails. Descendant endpoint responses do not become the
expected oracle.

Deployment schema validation remains an ETS-owned adapter boundary. A future
reusable SensorML module may replace schema semantics behind that boundary, but
the `ets-sensorml30` executable suite jar does not enter this runtime closure.
Protocol traversal, graph semantics, TestNG verdict policy, and Connected
Systems mappings remain local.

Primary TeamEngine E2E executes against unmodified local OSH and preserves
inherited Deployment failures as five Subdeployment dependency SKIPs.
Controlled read-only HTTP coverage supplies positive graph and association
evidence. No OSH or TeamEngine source or binary change is permitted.

Implementation reconciliation confirms all five methods are independently
deployed and reviewed exact. Primary local OSH TeamEngine is honestly
`219/39/5/175`; the inherited Deployment failures remain visible and all five
Subdeployment methods SKIP before IUT access. A controlled programmatic TestNG
experiment establishes causality with a passing Deployment baseline and a
single-variable failing prerequisite; the earlier direct local-OSH sabotage is
historical non-causal evidence only. Corrected focused Maven is `131/0/0/0`,
full Maven is `480/0/0/3`, and exact image
`sha256:e88aa5f9...b1dca` passes runtime, controlled HTTP, credential,
provenance, and zero-write/zero-leak hygiene gates. Focused adversarial recheck
closed the initial four findings and identified repository-root TestNG output
plus stale evidence. TestNG now writes only to JUnit-managed temporary
directories, focused/full Maven leave `test-output/` absent, and records use
the corrected causal proof. Final Raze returns `APPROVE_WITH_CONCERNS`,
confidence `0.99`, with all six findings closed and no required fixes.
