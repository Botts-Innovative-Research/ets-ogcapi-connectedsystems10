# OGC API Connected Systems ETS — Specification

> Version: 1.3 | Status: Active ETS implementation | Last updated: 2026-07-31
>
> **Capability scope**: A Java/TestNG Executable Test Suite for OGC TeamEngine that validates
> conformance against OGC 23-001 (Part 1: Feature Resources) and OGC 23-002 (Part 2: Dynamic Data),
> packaged as the certification-track deliverable for OGC CITE submission. Supersedes the v1.0
> web-app capabilities (`endpoint-discovery`, `conformance-testing`, `dynamic-data-testing`,
> `test-engine`, `request-capture`, `reporting`, `export`, `progress-session`), all of which are
> now `Frozen — v1.0 web app, superseded by ets-ogcapi-connectedsystems`.

## Purpose

This capability defines an OGC-compliant Executable Test Suite (ETS) for the OGC API – Connected Systems standard. The ETS is generated from `org.opengis.cite:ets-archetype-testng:2.7`, retains TeamEngine 5.6.1 as verified historical baseline evidence, and uses the immutable OGC-published TeamEngine 6.0.0 runtime implemented under CP-001 and Sprint 41. Maven, image build, startup, registration, exact-image runtime checks, and real local OSH execution are verified. It produces a per-conformance-class pass/fail/skip verdict against an Implementation Under Test (IUT) supplied as a CS API landing-page URL. The deliverable maps to PRD v2.0 functional requirements FR-ETS-01 through FR-ETS-90.

This capability does NOT define web-app endpoints, UI components, REST APIs, or session management — those concerns are owned by TeamEngine and superseded by the v1.0 web-app freeze.

## Functional Requirements

### Sub-deliverable 1 — Maven Archetype Scaffold

#### REQ-ETS-SCAFFOLD-001: Archetype Generation
- **Priority**: MUST
- **Status**: SPECIFIED
- **Description**: The deliverable SHALL be generated from `org.opengis.cite:ets-archetype-testng:2.7` with `groupId=org.opengis.cite`, `artifactId=ets-ogcapi-connectedsystems10`, `ets-code=ogcapi-connectedsystems10`, `ets-title='OGC API - Connected Systems Part 1'`. The generation command and any post-generation modernization SHALL be recorded in `ops/server.md` for reproducibility.
- **Rationale**: OGC convention. Deviating from the archetype produces an ETS that CITE SC reviewers will not recognize structurally.
- **Maps to**: PRD FR-ETS-01, R-PIVOT-01.

#### REQ-ETS-SCAFFOLD-002: JDK 17 + Maven 3.9
- **Priority**: MUST
- **Status**: SPECIFIED
- **Description**: The generated `pom.xml` SHALL declare `maven.compiler.source=17`, `maven.compiler.target=17`, and require Maven 3.9 or higher. Builds SHALL fail (not silently downgrade) on older JDKs/Maven.
- **Rationale**: TeamEngine 5.6.x (currently 5.6.1) is JDK 17. The 2019-vintage archetype defaults to older versions; modernization is mandatory.
- **Maps to**: PRD FR-ETS-02, NFR-ETS-02.

#### REQ-ETS-SCAFFOLD-003: Repo Layout Mirrors `ets-ogcapi-features10`
- **Priority**: MUST
- **Status**: SPECIFIED
- **Description**: The repository layout SHALL include: `src/main/java/org/opengis/cite/ogcapiconnectedsystems10/` (Java sources), `src/main/resources/org/opengis/cite/ogcapiconnectedsystems10/testng.xml` (suite definition), `src/main/resources/schemas/` (OGC JSON Schemas, ported from `csapi_compliance/schemas/`), `src/main/scripts/ctl/ogcapi-connectedsystems10-suite.ctl` (TeamEngine CTL wrapper), `src/site/` (AsciiDoc documentation), `src/test/resources/fixtures/spec-traps/` (ported corpus), `Dockerfile`, `Jenkinsfile`, `docker-compose.yml`, `pom.xml`, `README.adoc`.
- **Rationale**: CITE reviewers expect structural parity with reference ETSs. Divergences require justification.
- **Maps to**: PRD FR-ETS-03, R-PIVOT-02.

#### REQ-ETS-SCAFFOLD-004: Pinned Dependencies
- **Priority**: MUST
- **Status**: SPECIFIED
- **Description**: All dependencies in `pom.xml` SHALL be pinned to specific release versions. No `RELEASE`, `LATEST`, or open-ended ranges. Required dependencies: `org.opengis.cite:ets-common:17`, `org.opengis.cite.teamengine:teamengine-spi`, `org.testng:testng`, `io.rest-assured:rest-assured`, `com.reprezen.kaizen:openapi-parser`, `org.locationtech.jts:jts-core`, `org.locationtech.proj4j:proj4j`, `org.slf4j:slf4j-api`, `ch.qos.logback:logback-classic`.
- **Rationale**: Reproducible builds. CITE SC review may take months; transitive-dependency drift would invalidate the review.
- **Maps to**: PRD FR-ETS-04, NFR-ETS-01.

#### REQ-ETS-SCAFFOLD-005: Reproducible Build
- **Priority**: MUST
- **Status**: SPECIFIED
- **Description**: `mvn clean install` SHALL exit 0 on a clean checkout with JDK 17 and Maven 3.9. Two builds from the same commit SHALL produce byte-identical jars excluding `META-INF/` timestamps. The repository's local verification procedure SHALL verify this with a double-build diff.
- **Rationale**: NFR-ETS-01.
- **Maps to**: PRD FR-ETS-05.

#### REQ-ETS-SCAFFOLD-006: Modernization ADRs
- **Priority**: SHOULD
- **Status**: SPECIFIED
- **Description**: Every dependency-version bump or generated-scaffold modification beyond the archetype defaults SHALL be recorded as an ADR under `_bmad/adrs/`. The ADR SHALL include the original archetype value, the new value, the rationale, and links to relevant CVEs or compatibility issues.
- **Rationale**: The archetype is from 2019; modernization decisions accumulate and need to be auditable for CITE review.
- **Maps to**: PRD FR-ETS-06.

#### REQ-ETS-SCAFFOLD-007: Hosting Topology
- **Priority**: MUST
- **Status**: SPECIFIED
- **Description**: The repository SHALL be hosted at `github.com/<our-org>/ets-ogcapi-connectedsystems10` for the duration of pre-beta development. A draft contribution proposal to OGC SHALL be prepared at the beta milestone (R-PIVOT-12 / REQ-ETS-CITE-003), but the repo SHALL NOT be transferred or mirrored to OGC before then.
- **Rationale**: User decision 2026-04-27.
- **Maps to**: PRD FR-ETS-07.

### Sub-deliverable 2 — CS API Core Conformance Class (Sprint 1 target)

#### REQ-ETS-CORE-001: Test Method Per ATS Assertion
- **Priority**: MUST
- **Status**: SPECIFIED
- **Description**: For each assertion in OGC 23-001 Annex A `/conf/core/`, the ETS SHALL provide at least one TestNG `@Test` method whose `description` attribute starts with the OGC canonical requirement URI (e.g. `OGC-19-072 /req/landing-page/root-success` for landing-page assertions inherited from OGC API Common Part 1, or `OGC-23-001 /req/<class>/<X>` for CS API assertions). The URI form SHALL match the canonical `.adoc` source under `https://raw.githubusercontent.com/opengeospatial/ogcapi-common/master/19-072/requirements/<class>/REQ_<X>.adoc` (or the OGC 23-001 equivalent for CS API requirements). Each `@Test` SHALL produce exactly one of: PASS, FAIL (with structured message), SKIP (with reason).
- **Rationale**: Spec traceability; CITE reviewers map ATS to ETS by URI.
- **Maps to**: PRD FR-ETS-10, SC-2, SC-8.

#### REQ-ETS-CORE-002: Landing-Page Assertions
- **Priority**: MUST
- **Status**: SPECIFIED
- **Description**: The Core suite SHALL assert: (a) `GET /` returns HTTP 200 with `Content-Type` containing `application/json`; (b) the body has `title`, `description`, and `links` (array); (c) `links` contains entries with `rel=conformance` AND (`rel=service-desc` OR `rel=service-doc`) — citation: OGC API Common Part 1 (19-072) `/req/landing-page/root-success`, `/req/landing-page/conformance-success`, `/req/landing-page/api-definition-success` (canonical `.adoc` URIs verified 2026-04-28 per S-ETS-02-03). Absence of BOTH `service-desc` and `service-doc` is the FAIL condition; absence of only one PASSES via fallback. The `rel=self` relation is example-only and SHALL NOT be asserted as mandatory (this preserves the v1.0 GH#3 fix).
- **Rationale**: Preserves the link-relation fix landed in v1.0 sprint user-testing-round-01. Re-introducing a strict `self` requirement would regress against real-world conformant servers.
- **Maps to**: PRD FR-ETS-10. Direct port of v1.0 `REQ-TEST-001` and `REQ-TEST-CITE-002`.

#### REQ-ETS-CORE-003: Conformance Endpoint
- **Priority**: MUST
- **Status**: SPECIFIED
- **Description**: The Core suite SHALL assert `GET /conformance` returns HTTP 200 with a JSON body containing `conformsTo` (array of URI strings). The IUT's declared conformance classes are extracted from this response and used by dependent suites to decide PASS/SKIP.
- **Maps to**: PRD FR-ETS-10.

#### REQ-ETS-CORE-004: Resource Base Shape
- **Priority**: MUST
- **Status**: SPECIFIED
- **Description**: The Core suite SHALL assert that any resource discoverable from the landing-page links includes `id` (string), `type` (string matching the resource kind), and `links` (array of objects with `href`, `rel`, optional `type`, optional `title`).
- **Maps to**: PRD FR-ETS-10. Direct port of v1.0 `REQ-TEST-003`.

### Sub-deliverable 2A — Released ATS Coverage Authority

#### REQ-ETS-COVERAGE-001: Exact Released Annex A Inventory
- **Priority**: MUST
- **Status**: IMPLEMENTED (Sprint 45, S-ETS-45-01; final Raze approved at
  confidence `0.99` with no open findings).
  The released inventory and source reproduction are complete. This status
  applies to the coverage-control capability, not to Part 1 or Part 2 ATS
  completion; those owning requirements remain `PARTIAL_UNREVIEWED_ATS`.
- **Description**: The ETS SHALL maintain a deterministic machine-readable
  inventory of every abstract test in the approved OGC 23-001 and OGC 23-002
  version 1.0 Annex A suites. The reproducible source form SHALL be
  `opengeospatial/ogcapi-connected-systems` tag `v1.0.0`, commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`. The inventory SHALL contain
  exactly 13 Part 1 conformance classes and 110 tests, including the two
  supporting tests, and exactly 12 Part 2 conformance classes and 130 tests.
  The inventory/report pair SHALL record every entry's part, conformance class,
  test identifier, target requirement/recommendation when present, source
  provenance, coverage state, and implementation mapping. Source semantics
  belong to the generated inventory; mutable review state and compiled-suite
  mappings belong to the separately generated coverage report.
- **Coverage policy**: A target URI found in a compiled TestNG description is a
  candidate mapping only. An entry counts as implemented only after its mapping
  is exact, the mapped method implements the complete published test method, and
  the review state is recorded. Supporting tests SHALL map to reviewed reusable
  helpers in the explicit approved-helper registry. Implementation identities
  SHALL include parameter signatures. Discovery SHALL model inherited and
  method-level `enabled` semantics and SHALL fail closed on unsupported
  class-level tests, factories, suite method/package filters, or ambiguous
  overload identities. Unmapped, candidate-only, duplicate, unknown-method,
  source-drift, and unsupported implemented claims SHALL remain visible and
  fail any full-coverage gate.
- **Authority policy**: Approved OGC 23-001/23-002 documents and their release
  source tag are normative. The newer repository commit used by the separately
  pinned OpenAPI input, frozen web-app registries, and IUT declarations SHALL
  NOT add, remove, or rename released ATS classes or tests.
- **Maps to**: PRD SC-2, SC-8; FR-ETS-10..23, FR-ETS-30..42.

#### SCENARIO-ETS-COVERAGE-RELEASED-SOURCES-001 (CRITICAL)
**GIVEN** multiple Connected Systems drafts, repository pins, and implementation declarations exist
**WHEN** the ETS derives its certification coverage surface
**THEN** it uses OGC 23-001/23-002 version 1.0 and source commit `8e03b236...`
**AND** keeps the later OpenAPI pin as a separate non-ATS input.

#### SCENARIO-ETS-COVERAGE-EXACT-INVENTORY-001 (CRITICAL)
**GIVEN** the released source commit
**WHEN** the Annex A extractor runs
**THEN** it emits 13 Part 1 classes with 110 tests and 12 Part 2 classes with 130 tests
**AND** every released identifier appears exactly once within its part.

#### SCENARIO-ETS-COVERAGE-COMPILED-MAPPING-001 (CRITICAL)
**GIVEN** Java annotation descriptions may be composed from constants
**WHEN** the coverage audit discovers existing mappings
**THEN** it reads compiled TestNG metadata
**AND** does not infer absence from raw source-string counts.

#### SCENARIO-ETS-COVERAGE-FAIL-CLOSED-001 (CRITICAL)
**GIVEN** an inventory source drift, duplicate, missing test, unknown method, or unsupported status claim
**WHEN** the coverage gate runs
**THEN** it fails with the affected released identifier
**AND** it rejects ambiguous method identities, arbitrary helpers, or TestNG
execution features the audit does not model
**AND** does not downgrade the condition to a warning.

#### SCENARIO-ETS-COVERAGE-STATUS-HONESTY-001 (CRITICAL)
**GIVEN** a Java class exists or a smoke execution succeeds
**WHEN** project status is reconciled
**THEN** only reviewed exact test mappings count as implemented
**AND** candidate-only or unmapped Annex A tests keep the owning class partial
even when a historical implementation increment for that class is complete.

### Sub-deliverable 3 — Other Part 1 Conformance Classes

> Historical sprint increments are retained in their story artifacts. Active
> requirements below describe the released ATS procedures and supersede earlier
> approximation methods.

#### REQ-ETS-PART1-001: API Common Direct ATS Procedures (Sprint 46)
- **Priority**: MUST
- **Status**: DIRECT_ATS_IMPLEMENTED_FULL_CLASS_PARTIAL (Sprint 46 implements and
  review-maps all 4 directly owned class tests and 2 supporting tests; the five
  inherited external OGC API Features/Common classes remain incomplete)
- **Description**: The ETS SHALL implement all six directly owned released OGC 23-001 Annex A `/conf/api-common` procedures without claiming completion of the five inherited external OGC API Features/Common classes. `Part1ApiCommonTests` SHALL provide one reviewed, enabled TestNG method for each of `/conf/api-common/resource-ids`, `/conf/api-common/resource-uids`, `/conf/api-common/resource-uids-types`, and `/conf/api-common/datetime`. `Part1ApiCommonSupport` SHALL provide separately reviewed parameterized helpers for `/conf/api-common/canonical-resources` and `/conf/api-common/collection-items`. Canonical traversal SHALL probe all five Part 1 canonical resource types (`systems`, `deployments`, `procedures`, `samplingFeatures`, `properties`), negotiate released GeoJSON and SensorML JSON media types, parse both `features` and `items` collection wrappers, include every type returning a conforming collection, and follow `rel=next` links with cycle and page-count guards. Resource IDs SHALL be unique within a type. Resource UIDs SHALL be read from the applicable GeoJSON, SensorML, or extension member, be present, be valid absolute URIs, and be unique across all Part 1 types. UID forms outside the released recommendation SHALL emit a warning, not a failure. Date-time filtering SHALL run instant, bounded, open-start, and open-end interval queries for each advertised collection with a usable temporal extent and supported items media type, validate every returned `validTime` intersects each query, evaluate a `now` bound at captured request time, and verify every unfiltered timeless feature remains included. If no advertised collection has a usable temporal extent, the date-time test SHALL SKIP with an explicit evidence limitation. The existing `conformance.common.CommonTests` remains a partial inherited OGC API Common layer and does not count as a Connected Systems direct ATS implementation or prove full `/conf/api-common` conformance.
- **Dependency**: TestNG group `part1apicommon` SHALL depend on `core common`. Group `systemfeatures` SHALL depend on `part1apicommon`; current Part 1 descendants inherit the released prerequisite transitively.
- **Rationale**: Released `/conf/api-common` is the foundation inherited by every other OGC 23-001 class. Completing it first provides both correct prerequisite semantics and reusable bounded traversal for later Annex A procedures.
- **Maps to**: PRD FR-ETS-11.

#### REQ-ETS-PART1-002: System Direct ATS Procedures (Sprint 47)
- **Priority**: MUST
- **Status**: IMPLEMENTED_RELEASED_ATS (Sprint 47 implements and review-maps
  all 6 released `/conf/system` tests; replacement engineering, E2E, and final
  Raze gates pass)
- **Description**: The ETS SHALL implement exactly the six released OGC 23-001
  Annex A `/conf/system` procedures: `/conf/system/location`,
  `/conf/system/location-time`, `/conf/system/canonical-url`,
  `/conf/system/resources-endpoint`, `/conf/system/canonical-endpoint`, and
  `/conf/system/collections`. Each procedure SHALL have one independently
  executable TestNG method whose description cites its canonical target URI.
  The Sprint 2/3 collection-shape, item-shape, landing-page discovery,
  geometry-presence, and `validTime`-presence methods are superseded historical
  increments and SHALL NOT be treated as active requirements or reviewed ATS
  mappings. Their evidence remains only in the corresponding historical story
  artifacts.
- **Dependency**: Group `systemfeatures` remains ordered after
  `part1apicommon`. Before any System IUT access, its explicit result gate SHALL
  block every Core/Common/API Common failure, configuration failure, and API
  Common SKIP except the exact documented no-temporal-extent SKIP from
  `datetimeUsesValidTime`. That sole evidence limitation remains visible while
  direct System procedures execute; it never becomes PASS or full inherited
  conformance evidence.
- **Rationale**: `/conf/system` is the released prerequisite for descendant
  Part 1 classes. Exact direct procedures and explicit prerequisite-result
  handling replace the narrower historical approximations.
- **Maps to**: PRD FR-ETS-12.

Sprint 47 SHALL replace the historical target-URI approximations with the six
complete procedures in released OGC 23-001 Annex A. Canonical-resource location
inspection SHALL warn, not fail, when a non-`Simulation`/non-`Process` System
lacks GeoJSON `geometry` or SensorML `position`. Location-time SHALL use the
optional `mobile-system-id` run argument, poll for at most 30 seconds, require
two HTTP 200 canonical representations with different GeoJSON geometry or
SensorML positional coordinates, ignore orientation-only changes, and SKIP
when the input is absent. For every collection advertised with
`featureType=sosa:System`, canonical links SHALL be dereferenced and their
content compared with the collection item after removing canonical links from
both documents. One endpoint-parameterized procedure SHALL validate `/systems`
and System collection pages against the bundled GeoJSON or SensorML System
collection schema according to the actual response Content-Type. Referenced
GeoJSON schemas SHALL be pinned complete schemas, not permissive stubs.
Unsupported collections SHALL warn without suppressing later supported
collection evidence, and the procedure SHALL SKIP only when no supported
collection was executed.
The collections procedure SHALL select only exact `featureType=sosa:System`
entries and each selected item SHALL use one of the five released SOSA System
type URI/CURIE pairs. The released Annex A procedure does not independently
assert collection existence or `itemType`; the ETS SHALL not add either as an
extra gate to this exact ATS mapping. Shared setup SHALL load only run
arguments; each direct procedure SHALL retrieve its own network prerequisites.

#### SCENARIO-ETS-PART1-002-RELEASED-SCHEMA-FAIL-CLOSED-001 (CRITICAL)
**GIVEN** a System endpoint reports GeoJSON or SensorML JSON
**WHEN** its collection wrapper, feature, geometry, or System member violates
the selected bundled schema
**THEN** the applicable released procedure SHALL fail
**AND** permissive external-schema placeholders SHALL NOT convert malformed
content into PASS evidence.

#### SCENARIO-ETS-PART1-002-RELEASED-MULTI-COLLECTION-001 (CRITICAL)
**GIVEN** multiple exact `featureType=sosa:System` collections
**WHEN** one collection advertises unsupported media and a later supported
collection is invalid
**THEN** the ETS SHALL continue to the supported collection and report its
failure
**AND** SHALL SKIP only when no supported collection was executed.

#### SCENARIO-ETS-PART1-002-RELEASED-PROCEDURE-ISOLATION-001 (CRITICAL)
**GIVEN** the six released System procedures have different prerequisites
**WHEN** one prerequisite is absent or fails
**THEN** shared setup SHALL NOT suppress unrelated direct procedure results
**AND** the endpoint-parameterized resources procedure SHALL be reused by the
canonical and collection procedures.

#### SCENARIO-ETS-PART1-002-RELEASED-E2E-EXECUTION-001 (CRITICAL)
**GIVEN** an unmodified local OSH omits collection temporal extents
**AND** `/conf/api-common/datetime` reports its documented no-positive-evidence
SKIP
**WHEN** the full TeamEngine suite reaches `/conf/system`
**THEN** the six direct System procedures SHALL execute rather than inherit a
blanket TestNG dependency SKIP
**AND** any API Common failure, configuration failure, or skipped direct
procedure other than that exact datetime evidence limitation SHALL still block
System execution before System IUT access
**AND** the inherited datetime SKIP SHALL remain visible so the run is not
reported as full `/conf/system` conformance.
*Maps to*: REQ-ETS-PART1-001, REQ-ETS-PART1-002.

#### SCENARIO-ETS-PART1-002-RELEASED-DIRECT-HTTP-COVERAGE-001 (CRITICAL)
**GIVEN** a controlled HTTP fixture with schema-valid System collections,
canonical links, and a mobile System whose coordinates change
**WHEN** the six deployed System procedures execute
**THEN** location, location-time, canonical URL, resources endpoint, canonical
endpoint, and collections SHALL each complete their HTTP path
**AND** the regression SHALL assert the expected endpoint exchanges occurred.
*Maps to*: REQ-ETS-PART1-002.

#### REQ-ETS-PART1-003: Subsystem Direct ATS Procedures (Sprint 48)
- **Priority**: MUST
- **Status**: IMPLEMENTED_RELEASED_ATS (Sprint 48 replaces the historical
  four-method approximation with all 5 released `/conf/subsystem` tests;
  focused, full, exact-image runtime, local OSH TeamEngine, controlled HTTP,
  dependency sabotage, and credential gates pass; final Raze has no unresolved
  required findings)
- **Description**: The ETS SHALL implement exactly the five released OGC
  23-001 Annex A `/conf/subsystem` procedures: `/collection`,
  `/recursive-param`, `/recursive-search-systems`,
  `/recursive-search-subsystems`, and `/recursive-assoc`. Each procedure SHALL
  have one independently executable TestNG method whose description cites its
  canonical target URI. The Sprint 4 non-empty collection, inherited canonical
  shape, canonical-link, and parent-link methods are superseded historical
  increments and SHALL NOT be treated as active requirements or reviewed ATS
  mappings.
- **Hierarchy evidence**: Expected recursive closure SHALL be derived
  independently by bounded traversal of default direct-subsystems endpoints.
  Pagination cycles, hierarchy cycles, duplicate IDs, and malformed
  representations SHALL fail closed. A node returned as a direct child while
  also reachable from the same parent by a path of length two or more SHALL be
  rejected as a shortcut edge rather than removed from transitive evidence.
  Default and `recursive=false` responses
  SHALL exclude independently known transitive descendants;
  `recursive=true` SHALL include every independently discovered descendant.
  No positive hierarchy evidence SHALL produce SKIP rather than a vacuous PASS.
- **Collection and associations**: Collection discovery SHALL require the
  parent System's single exact `rel=subsystems` link and resolved URL, with no
  duplicate qualifying link, trailing slash, query, fragment, or cross-origin
  variation. Dereference SHALL require HTTP 200 before representation parsing,
  and actual-media GeoJSON or SensorML System schema validation SHALL follow.
  Unsupported or absent media SHALL warn and SKIP without first attempting JSON
  parsing. Association-type implementation SHALL be established independently
  from its canonical top-level endpoint, not inferred from nested parent
  responses. For every discovered parent, each implemented Sampling Feature,
  DataStream, and ControlStream endpoint SHALL return HTTP 200 and include all
  resource IDs observed through its descendants. No descendant association
  evidence SHALL produce SKIP.
- **Dependency**: Group `subsystems` remains ordered after `systemfeatures`.
  Its explicit result gate SHALL block every inherited failure, configuration
  failure, and unexpected SKIP. It may continue only past the exact documented
  API Common datetime, System unsupported-media, and missing-mobile-input
  evidence limitations. Those inherited SKIPs remain visible and cannot become
  positive full-class conformance evidence.
- **Rationale**: The released class tests recursive graph semantics, not the
  historical canonical-resource shape. Independent hierarchy discovery and
  evidence-sensitive execution are required to prevent self-fulfilling or
  empty-result PASS outcomes.
- **Maps to**: PRD FR-ETS-13.

#### SCENARIO-ETS-PART1-003-RELEASED-COLLECTION-001 (CRITICAL)
**GIVEN** a parent System known to have one or more subsystems
**WHEN** the collection procedure retrieves its canonical resource
**THEN** exactly one `rel=subsystems` link SHALL target
`{api_root}/systems/{sysId}/subsystems`
**AND** dereferencing the link SHALL return HTTP 200
**AND** every page SHALL validate against the GeoJSON or SensorML System schema
selected from its actual Content-Type
**AND** unsupported media SHALL warn and SKIP.

#### SCENARIO-ETS-PART1-003-RELEASED-MEDIA-GATE-001 (CRITICAL)
**GIVEN** hierarchy discovery retrieves the root `/systems` collection or a
nested subsystem collection, or collection validation retrieves a nested
subsystem collection page
**WHEN** the response is received
**THEN** HTTP status and actual Content-Type SHALL be evaluated before parsing
that page
**AND** every pagination page SHALL apply the same gate
**AND** unsupported or missing media SHALL warn and SKIP without parsing
**AND** a supported SensorML response SHALL be accepted from the first request
without requiring a prior GeoJSON representation
**AND** collection validation SHALL NOT require a non-standard `id` member;
recursive hierarchy discovery MAY require local IDs as graph evidence.

#### SCENARIO-ETS-PART1-003-RELEASED-EXACT-LINK-001 (CRITICAL)
**GIVEN** a parent System has subsystem hierarchy evidence
**WHEN** its `rel=subsystems` links are resolved
**THEN** exactly one qualifying link occurrence SHALL exist
**AND** its resolved URI SHALL exactly equal
`{api_root}/systems/{sysId}/subsystems`
**AND** duplicate-identical, trailing-slash, query, fragment, or cross-origin
targets SHALL fail.

#### SCENARIO-ETS-PART1-003-RELEASED-RECURSIVE-PARAM-001 (NORMAL)
**GIVEN** the recursive-parameter procedure
**WHEN** it issues its read-only requests
**THEN** each request SHALL contain `recursive`
**AND** the values SHALL be exactly `false` and `true`
**AND** both responses SHALL be successful
**AND** this procedure SHALL NOT require a parseable or supported response
representation.

#### SCENARIO-ETS-PART1-003-RELEASED-RECURSIVE-SYSTEMS-001 (CRITICAL)
**GIVEN** an independently discovered multi-level System hierarchy
**WHEN** `/systems`, `/systems?recursive=false`, and
`/systems?recursive=true` are retrieved
**THEN** default and false results SHALL exclude all known subsystem IDs
**AND** true results SHALL include every root and descendant at every level.

#### SCENARIO-ETS-PART1-003-RELEASED-RECURSIVE-SUBSYSTEMS-001 (CRITICAL)
**GIVEN** a parent with direct and transitive subsystems
**WHEN** its default, false, and true Subsystems endpoints are retrieved
**THEN** default and false results SHALL include direct children but exclude
known transitive descendants
**AND** true results SHALL include every independently discovered descendant.

#### SCENARIO-ETS-PART1-003-RELEASED-RECURSIVE-ASSOC-001 (CRITICAL)
**GIVEN** one or more parent Systems with subsystems
**WHEN** Sampling Feature, DataStream, or ControlStream resources are
implemented
**AND** implementation is established by the corresponding canonical
top-level endpoint
**THEN** every corresponding parent association endpoint SHALL return HTTP 200
**AND** include every resource ID observed through that parent's descendants
at all levels.

#### SCENARIO-ETS-PART1-003-RELEASED-ASSOCIATION-IMPLEMENTATION-001 (CRITICAL)
**GIVEN** a canonical top-level Sampling Feature, DataStream, or ControlStream
endpoint returns HTTP 200
**WHEN** every nested parent endpoint for that resource type returns HTTP 404
**THEN** recursive-association validation SHALL fail
**AND** SHALL NOT reinterpret the implemented resource type as unsupported.

#### SCENARIO-ETS-PART1-003-RELEASED-HIERARCHY-FAIL-CLOSED-001 (CRITICAL)
**GIVEN** hierarchy and association expectations must be independent
**WHEN** traversal encounters pagination cycles, hierarchy cycles, duplicate
IDs, shortcut edges, malformed collection bodies, or only empty positive
evidence
**THEN** structural defects SHALL fail
**AND** absent positive evidence SHALL SKIP
**AND** a recursive endpoint response SHALL NOT be its own expectation oracle.

#### SCENARIO-ETS-PART1-003-RELEASED-PROCEDURE-ISOLATION-001 (CRITICAL)
**GIVEN** the five released procedures have different evidence prerequisites
**WHEN** one procedure has no hierarchy, media, or association evidence
**THEN** shared setup SHALL NOT suppress unrelated direct procedure results.

#### SCENARIO-ETS-PART1-003-RELEASED-E2E-EXECUTION-001 (CRITICAL)
**GIVEN** the unmodified local OSH returns unsupported `application/json` for
root System collection traversal
**AND** inherited System procedures retain documented evidence SKIPs
**WHEN** the full TeamEngine suite reaches `/conf/subsystem`
**THEN** all five direct methods SHALL execute
**AND** recursive-parameter evidence MAY PASS
**AND** hierarchy-dependent methods SHALL SKIP honestly rather than dependency
SKIP or vacuous PASS
**AND** every inherited failure or unexpected SKIP SHALL still block Subsystem
IUT access.

#### SCENARIO-ETS-PART1-003-RELEASED-DIRECT-HTTP-COVERAGE-001 (CRITICAL)
**GIVEN** a controlled read-only HTTP fixture with a three-level System
hierarchy, schema-valid representations, and descendant Sampling Feature,
DataStream, and ControlStream resources
**WHEN** the five deployed Subsystem procedures execute
**THEN** every successful positive path SHALL complete
**AND** the fixture SHALL record the expected default, false, true, collection,
and association exchanges.

#### REQ-ETS-PART1-004: Deployment Direct ATS Procedures (Sprint 49)
- **Priority**: MUST
- **Status**: IMPLEMENTED_RELEASED_ATS (Sprint 49 replaces the historical
  four-method approximation with all 5 released `/conf/deployment` tests;
  focused, full, exact-image runtime, local OSH TeamEngine, controlled HTTP,
  API Common sabotage, credential freshness, and hygiene gates complete)
- **Description**: The ETS SHALL implement exactly the five released OGC 23-001
  Annex A `/conf/deployment` procedures: `/canonical-url`,
  `/resources-endpoint`, `/canonical-endpoint`, `/collections`, and
  `/ref-from-system`. Each procedure SHALL have one independently executable
  TestNG method whose description cites its canonical target URI. The
  historical non-empty canonical collection, generic item shape, canonical-link
  presence, and encoding-declaration methods are superseded approximations and
  SHALL NOT be reviewed as exact mappings.
- **Collection completeness**: Canonical URL and collections procedures SHALL
  process every collection advertised with `featureType=sosa:Deployment` and
  require at least one such collection. Selected metadata SHALL use
  `itemType=feature`. Missing selected collections SHALL fail rather than
  produce vacuous PASS. An unsupported selected collection SHALL warn and SKIP
  the procedure; one supported collection SHALL NOT conceal another
  unsupported selected collection. When a selected collection advertises
  multiple `rel=items` representations, a GeoJSON or SensorML representation
  supported by the Deployment procedure SHALL be selected in preference to an
  earlier generic JSON representation.
- **Canonical equivalence**: Every selected collection item SHALL contain a
  canonical link resolving on the IUT origin to
  `{api_root}/deployments/{id}`. Multiple canonical relation occurrences and
  representation variants MAY be present, but every occurrence SHALL resolve
  to that canonical resource identity. A representation query MAY remain. The
  first canonical occurrence in document order SHALL be dereferenced
  deterministically. The canonical resource SHALL return HTTP 200 and equal the
  collection item after canonical links are removed from both JSON documents.
- **Endpoint validation**: The parameterized resources procedure SHALL require
  HTTP 200 and validate every page against the released GeoJSON or SensorML
  Deployment collection schema selected from actual response `Content-Type`.
  The canonical endpoint procedure SHALL invoke the same behavior at
  `{api_root}/deployments`. HTTP status and actual media SHALL be gated before
  parsing every page. Unsupported or absent media SHALL warn and SKIP.
- **System reference**: The procedure SHALL retrieve every canonical System
  through the reviewed API Common helper. For every System local ID it SHALL
  require HTTP 200 from `{api_root}/systems/{sysId}/deployments`, follow bounded
  same-origin pagination, validate every page by actual media, and require every
  returned Deployment to contain an explicit representation-specific link to
  that System ID.
- **Dependency and validator boundary**: Deployment SHALL inherit Part 1 API
  Common directly. It SHALL NOT make all five procedures depend on completed
  System ATS outcomes. The defensive setup gate SHALL inspect only Core, Common,
  and Part 1 API Common configuration/test outcomes; unrelated or
  SystemFeatures configuration outcomes SHALL NOT block Deployment. Deployment
  schema dispatch remains behind an ETS-owned support boundary so a future
  reusable SensorML validator can replace local SensorML schema semantics
  without taking ownership of protocol discovery, TestNG verdict policy,
  canonical comparison, pagination, or Connected Systems mappings.
  `ets-sensorml30` SHALL NOT be imported as a library.
- **Historical record**: Sprint 5's four-method approximation and advisory
  GeoRobotix evidence remain archived in
  `epics/stories/s-ets-05-06-deployments-conformance-class.md`; they do not
  establish released ATS completion.
- **Implementation evidence**: Coverage is
  `240 total / 20 exact / 2 helper / 141 candidate / 77 unmapped`, with
  `/conf/deployment` `5/5 exact`. Focused Maven passes `35/0/0/0` for the
  adversarial corrections and `90/0/0/0` for the Deployment gate; full Maven
  is `434/0/0/3`. Exact image
  `sha256:9049b284529b53845403e985fae2b03a9598073724320de2ad2e395006506d47`
  passes runtime and immutable-base verification. Unmodified local OSH
  TeamEngine executes `217/39/3/175`; Deployment reports three genuine FAIL
  and two unsupported-media SKIP outcomes. API Common sabotage reports
  `217/34/1/182` and all five Deployment methods SKIP directly on the injected
  API Common failure before Deployment IUT access. Credential freshness and
  wire gates pass with zero unmasked artifact hits, 39 masked events, and 39
  intact synthetic transmissions. Positive and sabotage hygiene record zero
  IUT writes. Focused final Raze recheck closed the last reconciliation finding
  with `APPROVE` at confidence `0.99` and no unresolved required findings.
- **Maps to**: PRD FR-ETS-14.

#### SCENARIO-ETS-PART1-004-RELEASED-CANONICAL-URL-001 (CRITICAL)
**GIVEN** every advertised `featureType=sosa:Deployment` collection
**WHEN** its items are retrieved through the API Common helper
**THEN** every item SHALL expose a canonical Deployment URL
**AND** dereferencing it SHALL return HTTP 200 equivalent content.

#### SCENARIO-ETS-PART1-004-RELEASED-RESOURCES-ENDPOINT-001 (CRITICAL)
**GIVEN** a Deployment resources endpoint parameter
**WHEN** the released procedure executes
**THEN** every page SHALL return HTTP 200
**AND** actual GeoJSON or SensorML media SHALL select the corresponding released
Deployment collection schema.

#### SCENARIO-ETS-PART1-004-RELEASED-CANONICAL-ENDPOINT-001 (CRITICAL)
**GIVEN** the normalized API root
**WHEN** the canonical-endpoint procedure executes
**THEN** `{api_root}/deployments` SHALL satisfy the complete parameterized
resources-endpoint procedure.

#### SCENARIO-ETS-PART1-004-RELEASED-COLLECTIONS-001 (CRITICAL)
**GIVEN** advertised feature collections
**WHEN** the collections procedure executes
**THEN** at least one SHALL use `itemType=feature` and
`featureType=sosa:Deployment`
**AND** every selected collection page SHALL satisfy the released Deployment
schema for its actual media type.

#### SCENARIO-ETS-PART1-004-RELEASED-REF-FROM-SYSTEM-001 (CRITICAL)
**GIVEN** every canonical System local ID
**WHEN** `{api_root}/systems/{sysId}/deployments` is traversed
**THEN** every page SHALL return HTTP 200 and validate by actual media
**AND** every returned Deployment SHALL explicitly reference `sysId`.

#### SCENARIO-ETS-PART1-004-RELEASED-MEDIA-GATE-001 (CRITICAL)
**GIVEN** a first or later schema-controlled page has absent or unsupported
actual media
**WHEN** Deployment validation reaches that page
**THEN** the ETS SHALL warn and SKIP before representation parsing.
**AND** if collection metadata advertises generic JSON before a supported
GeoJSON or SensorML representation, the supported representation SHALL be
requested instead of producing a false SKIP.

#### SCENARIO-ETS-PART1-004-RELEASED-COLLECTION-COMPLETE-001 (CRITICAL)
**GIVEN** zero or multiple selected Deployment collections
**WHEN** canonical or collection validation executes
**THEN** zero selected collections SHALL fail as missing required evidence
**AND** every selected collection SHALL be processed without first-item,
first-collection, or partial-supported PASS.

#### SCENARIO-ETS-PART1-004-RELEASED-CANONICAL-EQUIVALENCE-001 (CRITICAL)
**GIVEN** canonical and collection representations of one Deployment
**WHEN** canonical relation links are removed from both
**THEN** the remaining JSON documents SHALL be structurally equal
**AND** canonical path, origin, status, or content differences SHALL fail
**AND** multiple canonical links that resolve to the same canonical Deployment
identity SHALL be accepted regardless of representation media, using the first
occurrence for deterministic dereference.

#### SCENARIO-ETS-PART1-004-RELEASED-SYSTEM-REFERENCE-001 (CRITICAL)
**GIVEN** a nested Deployment represented as GeoJSON or SensorML
**WHEN** its owning System ID is evaluated
**THEN** only an explicit representation-specific System link to that exact
local ID SHALL satisfy the procedure
**AND** substring or unrelated-link matches SHALL fail.

#### SCENARIO-ETS-PART1-004-RELEASED-PROCEDURE-ISOLATION-001 (CRITICAL)
**GIVEN** the five procedures have different inputs
**WHEN** one procedure fails or lacks supported media
**THEN** eager shared setup and method dependencies SHALL NOT suppress the
other direct procedures.

#### SCENARIO-ETS-PART1-004-RELEASED-DEPENDENCY-CASCADE-001 (CRITICAL)
**GIVEN** Part 1 API Common fails
**WHEN** Deployment setup starts
**THEN** all five direct methods SHALL SKIP before Deployment IUT access
**AND** the skip reason SHALL identify the direct Core/Common/API Common
prerequisite
**AND** unrelated or SystemFeatures configuration outcomes SHALL NOT restore
the historical all-method System ATS dependency.

#### SCENARIO-ETS-PART1-004-RELEASED-E2E-EXECUTION-001 (CRITICAL)
**GIVEN** the unmodified local OSH advertises no Deployment feature collection,
returns generic `application/json` from `/deployments`, and returns HTTP 400
from `/systems/040g/deployments`
**WHEN** TeamEngine executes `/conf/deployment`
**THEN** all five methods SHALL execute with honest FAIL/SKIP outcomes
**AND** no OSH or TeamEngine change SHALL mask those IUT defects.

#### SCENARIO-ETS-PART1-004-RELEASED-DIRECT-HTTP-COVERAGE-001 (CRITICAL)
**GIVEN** a controlled read-only fixture with GeoJSON and SensorML Deployment
collections, canonical resources, and System-scoped Deployment endpoints
**WHEN** all five deployed procedures execute
**THEN** every successful path SHALL complete
**AND** missing collections, unsupported later pages, canonical differences,
and wrong System references SHALL fail or SKIP as specified.

#### REQ-ETS-PART1-006: Procedure Direct ATS Procedures (Sprint 50)
- **Priority**: MUST
- **Status**: IMPLEMENTED_VERIFIED_RAZE_APPROVED (Sprint 50 replaces the
  historical four-method approximation with all 5 released
  `/conf/procedure` tests)
- **Description**: The ETS SHALL implement exactly the five released OGC 23-001
  Annex A `/conf/procedure` procedures: `/location`, `/canonical-url`,
  `/resources-endpoint`, `/canonical-endpoint`, and `/collections`. Each
  procedure SHALL have one independently executable TestNG method whose
  description cites its canonical target URI. The historical non-empty generic
  collection, representative canonical shape, canonical-link presence, and
  GeoJSON-only location methods are superseded approximations and SHALL NOT be
  reviewed as exact mappings.
- **Location absence**: The location procedure SHALL retrieve every page from
  `{api_root}/procedures`, following bounded same-origin pagination. HTTP status
  and actual media SHALL be gated before parsing each page. Every GeoJSON
  Procedure SHALL have `geometry` set to `null`; every SensorML Procedure SHALL
  omit `position`. Unsupported or absent actual media SHALL warn and SKIP
  before representation parsing.
- **Collection completeness**: Canonical URL and collections procedures SHALL
  process every collection advertised with `featureType=sosa:Procedure` and
  require at least one. Selected metadata SHALL use `itemType=feature`. Missing
  selected collections SHALL fail rather than produce vacuous PASS. When a
  schema-controlled selected collection advertises multiple `rel=items`
  representations, GeoJSON or SensorML SHALL be selected in preference to an
  earlier generic JSON representation.
- **Canonical equivalence**: Every selected collection item SHALL contain a
  canonical link resolving on the IUT origin to
  `{api_root}/procedures/{id}`. Multiple canonical relation occurrences and
  representation variants MAY be present, but every occurrence SHALL resolve
  to that exact Procedure identity. A representation query MAY remain. The
  first occurrence in document order whose advertised media type is absent or
  equals the collection page media type SHALL be dereferenced. If no occurrence
  is representation-comparable, the procedure SHALL warn and SKIP rather than
  report a false conformance failure. The canonical resource SHALL return HTTP
  200 and equal the collection item after canonical links are removed from both
  JSON documents. If canonical-link removal empties the item's `links` array,
  the empty member SHALL be treated as equivalent to an omitted optional
  `links` member in the canonical response.
- **Endpoint validation**: The parameterized resources procedure SHALL require
  HTTP 200 and validate every page against the released GeoJSON or SensorML
  Procedure collection schema selected from actual response `Content-Type`.
  The canonical endpoint procedure SHALL invoke the same behavior at
  `{api_root}/procedures`. Unsupported or absent actual media SHALL warn and
  SKIP before parsing.
- **Procedure type**: For each collection item, the collections procedure SHALL
  read `properties.featureType` from GeoJSON or `definition` from SensorML and
  require one of the nine URI/CURIE values in the released OGC 23-001 Clause 12
  Procedure Types table. Every page SHALL also satisfy the released
  representation-specific Procedure collection schema.
- **Dependency and validator boundary**: Procedure SHALL inherit Part 1 API
  Common directly. The defensive setup gate SHALL inspect only Core, Common,
  and Part 1 API Common configuration/test outcomes; unrelated or
  SystemFeatures outcomes SHALL NOT block Procedure. Procedure schema dispatch
  remains behind an ETS-owned support boundary so a future reusable SensorML
  validator can replace local SensorML schema semantics without taking
  ownership of protocol discovery, TestNG verdict policy, canonical
  comparison, pagination, or Connected Systems mappings. `ets-sensorml30`
  SHALL NOT be imported as a library.
- **Historical record**: Sprint 5's four-method approximation and advisory
  GeoRobotix evidence remain archived in
  `epics/stories/s-ets-05-05-procedures-conformance-class.md`; they do not
  establish released ATS completion.
- **Verification**: Reviewed coverage is `5/5 exact`. Final focused Maven passes
  `116/0/0/0`; full Docker Maven passes `451/0/0/3`; exact-image runtime
  verification passes on
  `sha256:6e1beeb598ab4c734f2e2d30e0ecb70d3270af9f9f2d5a1029d1b74259b54d98`.
  Unmodified-local-OSH TeamEngine executes all five direct methods and reports
  three unsupported-media SKIPs plus two missing-collection FAILs within the
  honest suite total `218/39/5/174`. API Common sabotage makes Procedure setup
  and all five methods SKIP before Procedure IUT access. Credential and
  artifact-hygiene gates pass with zero writes or leaks. The initial Raze
  review found canonical representation selection, optional `links`
  normalization, and stale dependency-comment gaps; all three are remediated
  and covered by focused regressions. Focused Raze recheck passes at confidence
  `0.99` with all three findings closed and no required fixes.
- **Maps to**: PRD FR-ETS-16.

#### SCENARIO-ETS-PART1-006-RELEASED-LOCATION-001 (CRITICAL)
**GIVEN** the canonical Procedure endpoint returns one or more supported
representation pages
**WHEN** the released location procedure executes
**THEN** every GeoJSON item SHALL have `geometry=null`
**AND** every SensorML item SHALL omit `position`
**AND** every pagination page SHALL be processed.

#### SCENARIO-ETS-PART1-006-RELEASED-MEDIA-GATE-001 (CRITICAL)
**GIVEN** a Procedure endpoint returns unsupported or absent actual media
**WHEN** a representation-specific procedure executes
**THEN** it SHALL warn and SKIP before representation parsing
**AND** later pagination pages SHALL apply the same gate.

#### SCENARIO-ETS-PART1-006-RELEASED-CANONICAL-URL-001 (CRITICAL)
**GIVEN** every advertised `featureType=sosa:Procedure` collection
**WHEN** its items are retrieved through the API Common helper
**THEN** every item SHALL expose a canonical Procedure URL
**AND** dereferencing it SHALL return HTTP 200 equivalent content.

#### SCENARIO-ETS-PART1-006-RELEASED-RESOURCES-ENDPOINT-001 (CRITICAL)
**GIVEN** a Procedure resources endpoint parameter
**WHEN** the released procedure executes
**THEN** every page SHALL return HTTP 200
**AND** actual GeoJSON or SensorML media SHALL select the corresponding
released Procedure collection schema.

#### SCENARIO-ETS-PART1-006-RELEASED-CANONICAL-ENDPOINT-001 (CRITICAL)
**GIVEN** the normalized API root
**WHEN** the released canonical endpoint procedure executes
**THEN** it SHALL apply the resources endpoint procedure at
`{api_root}/procedures`.

#### SCENARIO-ETS-PART1-006-RELEASED-COLLECTIONS-001 (CRITICAL)
**GIVEN** the server's advertised collections
**WHEN** the released collections procedure executes
**THEN** at least one collection SHALL have `itemType=feature` and
`featureType=sosa:Procedure`
**AND** every selected page SHALL satisfy its actual-media Procedure schema.

#### SCENARIO-ETS-PART1-006-RELEASED-COLLECTION-COMPLETE-001 (CRITICAL)
**GIVEN** zero, one, or multiple advertised Procedure collections
**WHEN** canonical URL or collections validation executes
**THEN** zero selected collections SHALL fail
**AND** every selected collection and every pagination page SHALL contribute
evidence.

#### SCENARIO-ETS-PART1-006-RELEASED-CANONICAL-EQUIVALENCE-001 (CRITICAL)
**GIVEN** a Procedure item has one or more canonical relation occurrences
**WHEN** canonical URL validation executes
**THEN** every occurrence SHALL resolve to the exact same-origin canonical
Procedure identity
**AND** the first representation-comparable occurrence SHALL return HTTP 200
content equal to the collection item after canonical links are removed from
both
**AND** an item `links` member emptied by that removal SHALL equal an omitted
canonical-response `links` member
**AND** no comparable occurrence SHALL warn and SKIP.

#### SCENARIO-ETS-PART1-006-RELEASED-PROCEDURE-TYPE-001 (CRITICAL)
**GIVEN** GeoJSON or SensorML Procedure collection items
**WHEN** the collections procedure retrieves each reported type
**THEN** GeoJSON SHALL use `properties.featureType`
**AND** SensorML SHALL use `definition`
**AND** every value SHALL be one of the released Clause 12 Procedure type URI
or CURIE values.

#### SCENARIO-ETS-PART1-006-RELEASED-PROCEDURE-ISOLATION-001 (CRITICAL)
**GIVEN** one Procedure procedure lacks evidence or receives unsupported media
**WHEN** the other four methods execute
**THEN** no method dependency or eager shared retrieval SHALL suppress their
independent outcomes.

#### SCENARIO-ETS-PART1-006-RELEASED-DEPENDENCY-CASCADE-001 (CRITICAL)
**GIVEN** a Core, Common, or Part 1 API Common prerequisite fails
**WHEN** Procedure setup starts
**THEN** all five direct methods SHALL SKIP before Procedure IUT access
**AND** unrelated or SystemFeatures outcomes SHALL NOT restore the historical
System ATS dependency.

#### SCENARIO-ETS-PART1-006-RELEASED-E2E-EXECUTION-001 (CRITICAL)
**GIVEN** the unmodified local OSH returns generic `application/json` from
`/procedures` and advertises no Procedure feature collection
**WHEN** TeamEngine executes `/conf/procedure`
**THEN** all five methods SHALL execute with honest FAIL/SKIP outcomes
**AND** no OSH or TeamEngine change SHALL mask those IUT limitations.

#### SCENARIO-ETS-PART1-006-RELEASED-DIRECT-HTTP-COVERAGE-001 (CRITICAL)
**GIVEN** a controlled read-only fixture with GeoJSON and SensorML Procedure
collections and canonical resources
**WHEN** all five deployed procedures execute
**THEN** every successful path SHALL complete
**AND** location, media, type, canonical, and collection defects SHALL fail or
SKIP as specified.

#### REQ-ETS-PART1-007..013: Remaining Per-Class Conformance Suites
- **Priority**: MUST
- **Status**: PARTIAL_UNREVIEWED_ATS for REQ-ETS-PART1-007..013. Historical implementation increments exist, but no released test in these classes has a reviewed exact mapping at the Sprint 45 baseline.
- **Description**: For each remaining OGC 23-001 conformance class (009=`advanced-filtering`, 010=`create-replace-delete`, 011=`update`, 012=`geojson`, 013=`sensorml`), the ETS SHALL provide a TestNG suite class structurally equivalent to Core (REQ-ETS-CORE-001..004), SystemFeatures (REQ-ETS-PART1-002), Common (REQ-ETS-PART1-001), Subsystems (REQ-ETS-PART1-003), Procedures (REQ-ETS-PART1-006), Deployments (REQ-ETS-PART1-004), Sampling Features (REQ-ETS-PART1-007), Property Definitions (REQ-ETS-PART1-008), Subdeployments (REQ-ETS-PART1-005), and GeoJSON (REQ-ETS-PART1-012): one `@Test` per ATS assertion subset selected for the sprint, `description` attribute carries the OGC canonical `.adoc` requirement URI form, suite-level dependency declared via TestNG `dependsOnGroups` if a prerequisite class fails.
- **Rationale**: PRD SC-2 requires Part 1 coverage. Sprint 9 selected a GeoJSON systems read-only subset first because it was lower risk than create-replace-delete mutation coverage and lower schema breadth than SensorML. Sprint 10 continues the low-risk read-only encoding path with SensorML systems before any mutation-side class.
- **Maps to**: PRD FR-ETS-17..23.

### Sub-deliverable 4 — Part 2 Conformance Classes

#### REQ-ETS-PART2-001: Part 2 API Common Conformance Suite
- **Priority**: MUST
- **Status**: IMPLEMENTED_RELEASED_ATS (Sprint 59 CP-019; 2/2 exact mappings)
- **Historical increment**: (Sprint 20 Generator 2026-05-07; story S-ETS-20-01)
- **Closure increment**: (Sprint 59 CP-019; story S-ETS-59-01)
- **Description**: The ETS SHALL provide a TestNG suite class for OGC 23-002 Requirements Class "Common" using official identifiers `/req/api-common`, `/conf/api-common`, `/req/api-common/resources`, and `/req/api-common/resource-collection`. Sprint 59 SHALL close the two released Part 2 API Common ATS procedures exactly. The deployed class SHALL contain one TestNG `@Test` per released procedure, SHALL depend directly on Part 1 API Common through `part2apicommon -> part1apicommon`, SHALL keep `/conf/api-common` declaration honesty inside each procedure, and SHALL SKIP with a precise reason when an IUT does not declare `/conf/api-common`.
- **OGC source verified**: OGC 23-002 official published HTML at `https://docs.ogc.org/is/23-002/23-002.html`, Clause 8 "Requirements Class Common", checked 2026-05-07. The requirements class identifier is `/req/api-common`; conformance class is `/conf/api-common`; prerequisite is `http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/api-common`; normative statements are `/req/api-common/resources` and `/req/api-common/resource-collection`.
- **Planning correction**: Frozen web-app artifacts that mention `dynamic-common` or `dynamic-json` are historical and MUST NOT be used for Java ETS `@Test` descriptions. Sprint 20 adopts OGC 23-002 identifiers.
- **GeoRobotix planning probe**: `/conformance` declares several Part 2 classes (`/conf/datastream`, `/conf/controlstream`, `/conf/json`, `/conf/create-replace-delete`, `/conf/system-event`, `/conf/system-history`, and SWE Common encodings) but does not currently declare `/conf/api-common`. Landing page exposes `datastreams` and `observations` links. `GET /datastreams?limit=1`, `GET /observations?limit=1`, and `GET /controlstreams?limit=1` returned HTTP 200 JSON with `items` and `links`; `GET /commands?limit=1` returned HTTP 400 in current IUT state.
- **Implementation evidence**: `Part2ApiCommonTests` checks exact `/conf/api-common` declaration, discovers only advertised Part 2 collection links, probes those links read-only with `limit=1`, and requires JSON collection objects with `items` and `links`. `VerifyPart2ApiCommonTests` prevents stale `dynamic-*` identifier drift and synthesized `/commands` assumptions. Maven post-Raze rerun reported `152 tests / 0 failures / 0 errors / 3 skipped`; GeoRobotix smoke on 2026-05-07 reported `93 total / 55 passed / 0 failed / 38 skipped`; the Part 2 API Common subset SKIPPED because `/conf/api-common` is not declared.
- **Sprint 59 implementation**: Replaced the historical four-method subset with exactly two released procedures: `/conf/api-common/resources` and `/conf/api-common/resource-collection`. Setup loads only immutable suite arguments and does not fetch the IUT when inherited prerequisites already failed, except that the documented Part 1 API Common datetime evidence limitation is tolerated so independently executable Part 2 API Common procedure evidence remains visible. Collection discovery remains landing-page-advertised, same-origin, bounded, and read-only.
- **Sprint 59 verification evidence**: Focused test-first run reproduced the
  historical gap at `88 tests / 6 failures / 1 error / 0 skipped`. Corrected
  focused verification is `88/0/0/0`; coverage audit is `23/0/0/0`; full
  Docker Maven is `735 tests / 0 failures / 0 errors / 3 skipped`. Coverage is
  now `240 total / 93 exact / 2 helper / 116 candidate / 29 unmapped`, with
  Part 2 API Common `2 exact / 0 candidate / 0 unmapped`. Local OSH TeamEngine
  smoke executed the deployed suite against unmodified local OSH and exited
  honestly non-green at `244 total / 41 passed / 21 failed / 182 skipped`; the
  Part 2 API Common setup passed and both new methods SKIP because local OSH
  does not declare Part 2 `/conf/api-common`. The no-mutation oracle recognized
  194 local-OSH IUT request logs and zero POST/PUT/PATCH/DELETE. Raze returned
  `APPROVE_WITH_CONCERNS 0.95` with no required fixes; its only concern is raw
  Maven stdout archival, while the exact Maven totals are recorded consistently
  in sprint documentation.
- **Maps to**: PRD FR-ETS-30.

##### Acceptance Scenarios for Sprint 59

#### SCENARIO-ETS-PART2-001-RELEASED-RESOURCES-001 (CRITICAL)
**GIVEN** the ETS is evaluating Part 2 API Common
**WHEN** `/conf/api-common/resources` executes
**THEN** it SHALL require the IUT to declare Part 2 `/conf/api-common`
**AND** it SHALL identify at least one advertised same-origin Part 2 resource
collection link before producing PASS evidence.

#### SCENARIO-ETS-PART2-001-RELEASED-RESOURCE-COLLECTION-001 (CRITICAL)
**GIVEN** an IUT exposes Part 2 collection links or endpoints
**WHEN** `/conf/api-common/resource-collection` executes
**THEN** each probed collection response must return HTTP 200 with a JSON object body containing collection members such as `items` and `links`
**AND** the ETS SHALL only issue same-origin read-only GET requests to
advertised collection links.

#### SCENARIO-ETS-PART2-001-RELEASED-PROCEDURE-ISOLATION-001 (CRITICAL)
**GIVEN** the released Part 2 API Common ATS has two procedures
**WHEN** the Java suite is inspected
**THEN** `Part2ApiCommonTests` SHALL expose exactly two TestNG `@Test` methods
**AND** each method SHALL map to exactly one released requirement target.

#### SCENARIO-ETS-PART2-001-RELEASED-DEPENDENCY-CASCADE-001 (CRITICAL)
**GIVEN** Part 2 API Common depends on Part 1 API Common/Core behavior
**WHEN** a prerequisite class fails
**THEN** the Part 2 API Common setup SHALL SKIP before IUT access
**AND** `testng.xml` SHALL declare `part2apicommon` depends on
`part1apicommon`.

#### SCENARIO-ETS-PART2-001-RELEASED-DECLARATION-HONESTY-001 (CRITICAL)
**GIVEN** current GeoRobotix declares Part 2 sibling classes but not `/conf/api-common`
**WHEN** the Part 2 API Common conformance declaration assertion runs
**THEN** it SKIPs with a reason tied to the missing `/conf/api-common`
**AND** it must not claim Part 2 API Common conformance from sibling declarations alone.

##### Historical Acceptance Scenarios for Sprint 20

The following Sprint 20 scenarios documented the first read-only subset and are
superseded by the Sprint 59 released ATS scenarios above for coverage status:
`SCENARIO-ETS-PART2-001-API-COMMON-CONFORMANCE-DECLARED-001`,
`SCENARIO-ETS-PART2-001-RESOURCE-TERMINOLOGY-001`,
`SCENARIO-ETS-PART2-001-RESOURCE-COLLECTION-READONLY-001`,
`SCENARIO-ETS-PART2-001-DEPENDENCY-SKIP-001`, and
`SCENARIO-ETS-PART2-001-GEOROBOTIX-DECLARATION-HONESTY-001`.

#### REQ-ETS-PART2-002: Part 2 Datastreams & Observations Conformance Suite
- **Priority**: MUST
- **Status**: IMPLEMENTED_RELEASED_ATS
- **Historical increment**: (Sprint 21 Generator 2026-05-07; story S-ETS-21-01)
- **Sprint 60 change**: CP-020 / S-ETS-60-01 replaced the historical subset with
  exactly fourteen released OGC 23-002 Annex A.2 procedures, removed standalone
  non-ATS tracer methods, and changed TestNG inheritance to the now-exact Part
  2 API Common group.
- **Description**: The ETS SHALL provide a TestNG suite class for OGC 23-002 Requirements Class "Datastreams & Observations" using official identifiers `/req/datastream` and `/conf/datastream`. Sprint 60 implements exactly the fourteen released Datastream and Observation procedures, gates through the Part 2 API Common prerequisite, follows every applicable canonical resource or exact `itemType` collection, dereferences advertised canonical links, validates endpoint and collection JSON schemas, validates FeatureOfInterest GeoJSON responses including `application/json` pages as generic GeoJSON FeatureCollections, checks every advertised Observation schema format for every DataStream, skips mixed Sampling Feature evidence when any endpoint cannot execute supported-media validation, gates conditional Sampling Feature, FeatureOfInterest, System, and Deployment procedures before nested IUT subresource access, and does not mutate the IUT.
- **OGC source verified**: OGC 23-002 official published HTML at `https://docs.ogc.org/is/23-002/23-002.html`, Clause 9 "Requirements Class Datastreams & Observations", checked 2026-05-07 and rechecked from cached official HTML on 2026-07-31. The requirements class identifier is `/req/datastream`; conformance class is `/conf/datastream`; prerequisite is Requirements Class 1 `/req/api-common`. Normative statements include `/req/datastream/sf-ref-from-datastream`, `/req/datastream/foi-ref-from-datastream`, `/req/datastream/canonical-url`, `/req/datastream/resources-endpoint`, `/req/datastream/canonical-endpoint`, `/req/datastream/ref-from-system`, `/req/datastream/ref-from-deployment`, `/req/datastream/collections`, `/req/datastream/schema-op`, `/req/datastream/obs-canonical-url`, `/req/datastream/obs-resources-endpoint`, `/req/datastream/obs-canonical-endpoint`, `/req/datastream/obs-ref-from-datastream`, and `/req/datastream/obs-collections`.
- **Dependency policy**: Sprint 60 SHALL keep `/req/api-common` prerequisite visibility explicit by wiring `part2datastream` directly to `part2apicommon`. The Datastream setup reads only immutable suite arguments before the inherited prerequisite gate. If the Part 2 API Common prerequisite is absent or skipped, the fourteen Datastream procedures SHALL SKIP before Datastream IUT access; Datastream endpoint behavior must never imply API Common PASS.
- **GeoRobotix planning probe**: `/conformance` declares `/conf/datastream` but not `/conf/api-common`. `GET /datastreams?limit=2`, `GET /observations?limit=2`, `GET /datastreams/{id}`, `GET /datastreams/{id}/schema`, `GET /datastreams/{id}/observations?limit=2`, and `GET /systems/{systemId}/datastreams?limit=1` returned HTTP 200 JSON. The selected Datastream exposes `system@id`, `outputName`, `observedProperties`, `resultType`, `formats`, and an `observations` link. The nested observations response for that Datastream was empty with `items` only, so Generator may count it only as endpoint availability evidence. Any `/req/datastream/obs-ref-from-datastream` assertion must require at least one nested Observation item or link with Datastream reference evidence, or SKIP with a precise empty-IUT-state reason.
- **Implementation evidence**: Sprint 21 first added a scoped read-only subset. Sprint 60 supersedes that subset with `Part2DatastreamTests` exposing exactly fourteen independent released procedure methods, no standalone declaration/prerequisite tracer methods, no response/body cache fields, and direct `part2datastream -> part2apicommon` TestNG wiring. Initial Raze returned `GAPS_FOUND 0.94` for bounded approximation issues; follow-up Raze passes found FOI overvalidation/stale evidence, missing conditional-applicability gates, mixed Sampling Feature false-PASS risk, and FOI `application/json` schema-validation gaps. The final implementation closes those by all-resource traversal, exact collection selection, canonical-link dereference/equality, endpoint schema validation, generic FOI GeoJSON validation, all-format schema-op coverage, per-endpoint unsupported-media SKIPs, and pre-access condition gates. Focused test-first evidence reproduced the historical gap at `85 tests / 3 failures / 2 errors / 0 skipped`; corrected focused verification passed `96/0/0/0`. Formatter passed, the released coverage audit passed `23/0/0/0`, and full Docker Maven completed `750 tests / 0 failures / 0 errors / 3 skipped`.
- **Sprint 60 E2E evidence**: Mandatory local OSH TeamEngine smoke reached the deployed TeamEngine stack and unmodified local OSH IUT, then exited honestly non-green at `247 total / 38 passed / 21 failed / 188 skipped`. The Datastream setup and all fourteen Datastream procedures SKIP because local OSH does not declare the Part 2 `/conf/api-common` prerequisite. The remaining failures are existing local OSH interoperability/conformance gaps outside Datastream. The no-mutation oracle recognized 189 local-OSH IUT request logs; the container log contains 194 request lines, all `GET`, with zero POST, PUT, PATCH, or DELETE. Final evidence is archived under `ops/test-results/sprint-ets-60-part2-datastream-final-raze-2026-07-31/`.
- **Maps to**: PRD FR-ETS-31.

##### Acceptance Scenarios for Sprint 21

#### SCENARIO-ETS-PART2-002-DATASTREAM-CONFORMANCE-DECLARED-001 (CRITICAL)
**GIVEN** the ETS is evaluating OGC 23-002 Datastreams & Observations
**WHEN** it reads `/conformance`
**THEN** `/conf/datastream` is required before producing Datastream conformance PASS evidence
**AND** `/conf/api-common` remains a separate prerequisite judgment, not something inferred from Datastream behavior.

#### SCENARIO-ETS-PART2-002-DATASTREAM-COLLECTION-READONLY-001 (CRITICAL)
**GIVEN** `/req/datastream/resources-endpoint` and `/req/datastream/canonical-endpoint`
**WHEN** the ETS issues `GET {api_root}/datastreams`
**THEN** the response is HTTP 200 JSON with an `items` array
**AND** the test records the canonical requirement URI in its `@Test` description.

#### SCENARIO-ETS-PART2-002-DATASTREAM-ITEM-READONLY-001 (CRITICAL)
**GIVEN** a Datastream identifier selected from the collection
**WHEN** the ETS issues `GET {api_root}/datastreams/{id}`
**THEN** the response is HTTP 200 JSON for the same Datastream resource
**AND** the resource exposes enough Datastream-specific shape to avoid passing on a generic JSON object.

#### SCENARIO-ETS-PART2-002-DATASTREAM-SCHEMA-ENDPOINT-001 (CRITICAL)
**GIVEN** `/req/datastream/schema-op`
**WHEN** the ETS issues `GET {api_root}/datastreams/{id}/schema`
**THEN** the response is HTTP 200 JSON with Datastream observation schema evidence such as `obsFormat` and `resultSchema`.

#### SCENARIO-ETS-PART2-002-OBSERVATION-ENDPOINTS-READONLY-001 (CRITICAL)
**GIVEN** `/req/datastream/obs-canonical-endpoint` and `/req/datastream/obs-resources-endpoint`
**WHEN** the ETS reads the global Observation collection and a Datastream-scoped Observation collection
**THEN** both responses are HTTP 200 JSON objects with an `items` array
**AND** an empty Datastream-scoped Observation collection is not treated as endpoint-availability failure by itself.

#### SCENARIO-ETS-PART2-002-OBSERVATION-REFERENCE-EVIDENCE-001 (CRITICAL)
**GIVEN** `/req/datastream/obs-ref-from-datastream`
**WHEN** the ETS evaluates Datastream-to-Observation reference behavior
**THEN** PASS requires at least one nested Observation item or link with evidence that the Observation is associated to the selected Datastream
**AND** an empty Datastream-scoped Observation collection SKIPs the reference assertion with a precise empty-IUT-state reason.

#### SCENARIO-ETS-PART2-002-SYSTEM-REFERENCE-READONLY-001 (NORMAL)
**GIVEN** `/req/datastream/ref-from-system` and a Datastream resource with `system@id`
**WHEN** the ETS issues `GET {api_root}/systems/{systemId}/datastreams`
**THEN** the response is HTTP 200 JSON with an `items` array
**AND** the selected Datastream is found when the IUT returns it in the current page, otherwise the check remains bounded and non-mutating.

#### SCENARIO-ETS-PART2-002-DEPENDENCY-SKIP-001 (CRITICAL)
**GIVEN** Datastream has prerequisite `/req/api-common`
**WHEN** the prerequisite class cannot be established for the IUT
**THEN** the ETS must not convert Datastream endpoint success into API Common PASS evidence
**AND** it must not report full `/conf/datastream` class closure
**AND** any prerequisite-dependent assertion SKIPs with a precise reason rather than failing downstream noisily.

##### Acceptance Scenarios for Sprint 60

#### SCENARIO-ETS-PART2-002-RELEASED-PROCEDURES-001 (CRITICAL)
**GIVEN** the released OGC 23-002 Annex A.2 `/conf/datastream` inventory
**WHEN** the ETS Datastream class is compiled into the TestNG suite
**THEN** it exposes exactly fourteen TestNG procedures
**AND** each procedure maps to exactly one released `/req/datastream/*` target
**AND** no standalone declaration or prerequisite tracer method is claimed as released ATS coverage.

#### SCENARIO-ETS-PART2-002-DIRECT-PREREQUISITE-001 (CRITICAL)
**GIVEN** `/req/datastream` has prerequisite `/req/api-common`
**WHEN** the canonical TestNG suite declares dependency groups
**THEN** `part2datastream` depends directly on `part2apicommon`
**AND** Datastream setup performs no Datastream IUT requests before the inherited prerequisite gate.

#### SCENARIO-ETS-PART2-002-ASSOCIATION-SUBRESOURCES-001 (CRITICAL)
**GIVEN** the released association procedures for sampling features, features
of interest, systems, deployments, and Observations
**WHEN** the ETS evaluates a selected Datastream with corresponding association evidence
**THEN** the associated resources are checked through bounded read-only
sub-resource endpoints
**AND** absent condition/evidence produces precise SKIP rather than false PASS.

#### SCENARIO-ETS-PART2-002-COLLECTION-TAGGING-001 (CRITICAL)
**GIVEN** the released Datastream and Observation `/collections` procedures
**WHEN** `/collections` advertises selected Datastream or Observation collections
**THEN** Datastream collections are tagged as `itemType=DataStream`
**AND** Observation collections are tagged as `itemType=Observation`
**AND** absence of relevant collection metadata is reported honestly.

#### SCENARIO-ETS-PART2-002-EXACT-MAPPING-001 (CRITICAL)
**GIVEN** the reviewed ATS mapping file and generated coverage report
**WHEN** Sprint 60 coverage gates run
**THEN** `2:/conf/datastream` reports `14 exact / 0 candidate / 0 unmapped`
**AND** the overall exact count increases by fourteen without changing the released inventory.

#### SCENARIO-ETS-PART2-002-SMOKE-NO-MUTATION-001 (CRITICAL)
**GIVEN** the mandatory local OSH TeamEngine E2E gate
**WHEN** Sprint 60 Datastream procedures run against the unmodified local OSH IUT
**THEN** the outcome is documented with concrete pass/fail/skip totals
**AND** IUT-bound request logs contain zero POST, PUT, PATCH, or DELETE.

#### REQ-ETS-PART2-003: Part 2 Control Streams & Commands Conformance Suite
- **Priority**: MUST
- **Status**: IMPLEMENTED_RELEASED_ATS_EXACT (Sprint 61 replaces the Sprint 22
  partial/unreviewed subset)
- **Historical increment**: Sprint 22 Generator 2026-05-08 remains preserved as
  partial evidence only; Sprint 61 supersedes it for reviewed ATS mapping.
- **Description**: The ETS SHALL implement exactly the eighteen released OGC
  23-002 Annex A.3 `/conf/controlstream` procedures:
  `/sf-ref-from-controlstream`, `/foi-ref-from-controlstream`,
  `/canonical-url`, `/resources-endpoint`, `/canonical-endpoint`,
  `/ref-from-system`, `/ref-from-deployment`, `/collections`, `/schema-op`,
  `/cmd-canonical-url`, `/cmd-resources-endpoint`,
  `/cmd-canonical-endpoint`, `/cmd-ref-from-controlstream`,
  `/cmd-collections`, `/status-resources-endpoint`,
  `/command-status-endpoint`, `/result-resources-endpoint`, and
  `/command-result-endpoint`. Each procedure SHALL have one independently
  executable TestNG method whose description cites its canonical target URI and
  `REQ-ETS-PART2-003`. Standalone declaration/prerequisite tracer methods from
  the historical subset SHALL NOT be deployed as reviewed ATS procedures.
- **OGC source verified**: OGC 23-002 official published HTML at
  `https://docs.ogc.org/is/23-002/23-002.html`, Clause 10 and Annex A.3,
  checked 2026-07-31. The requirements class identifier is
  `/req/controlstream`; conformance class is `/conf/controlstream`;
  prerequisite is Requirements Class 1 `/req/api-common`. Annex A.3 also
  specifies nested Command endpoints at `/controlstreams/{csId}/commands`,
  CommandStatus endpoints at `/commands/{cmdId}/status`, and CommandResult
  endpoints at `/commands/{cmdId}/result`.
- **Dependency policy**: `part2controlstream` SHALL depend directly on
  `part2apicommon`. The Sprint 22 scoped-execution exception is superseded now
  that Sprint 59 implemented the Part 2 API Common released ATS. If Part 2 API
  Common is absent, skipped, or failed, ControlStream setup SHALL skip before
  ControlStream IUT access and SHALL NOT convert scoped endpoint evidence into
  full `/conf/controlstream` closure.
- **Read-only scope guard**: Sprint 61 SHALL issue only GET requests. It SHALL
  NOT create Commands, issue feasibility POSTs, mutate ControlStreams, or claim
  semantic Command parameter/result validation beyond the released resource
  endpoint and schema procedures. Empty ControlStream, Command, CommandStatus,
  or CommandResult collections SHALL SKIP resource-specific child checks with
  precise data-availability reasons rather than producing vacuous PASS.
- **Implementation evidence**: Sprint 61 implements exactly eighteen
  `Part2ControlStreamTests` procedures with direct `part2apicommon` inheritance,
  immutable setup, released ControlStream/Command/CommandStatus/CommandResult
  schema validation, exact `itemType` collection traversal, advertised
  canonical-link evidence, all-format `cmdFormat` schema-op checks, and
  condition-gated read-only nested endpoints. Reviewed coverage is
  `2:/conf/controlstream = 18 exact / 0 candidate / 0 unmapped`; overall
  coverage is `240 total / 125 exact / 2 helper / 99 candidate / 14 unmapped`.
  Focused red captured the historical gap at `88/3/6/0`, corrected focused
  verification passed `88/0/0/0`, coverage update passed `1/0/0/0`, full
  coverage audit passed `23/0/0/0`, and full Docker Maven passed
  `758/0/0/3`. Mandatory local OSH TeamEngine reached the real unmodified IUT
  and exited honestly non-green at `254 total / 36 passed / 21 failed /
  197 skipped`; all eighteen ControlStream methods SKIP through
  `part2apicommon` because local OSH does not declare Part 2 `/conf/api-common`.
  The no-mutation oracle recognized 186 IUT request logs; the captured log has
  `GET=192` and zero POST/PUT/PATCH/DELETE/OPTIONS.
- **Maps to**: PRD FR-ETS-32.

##### Acceptance Scenarios for Sprint 61

#### SCENARIO-ETS-PART2-003-RELEASED-PROCEDURES-001 (CRITICAL)
**GIVEN** the reviewed OGC 23-002 Annex A.3 inventory
**WHEN** the deployed ControlStream TestNG class is inspected
**THEN** it SHALL expose exactly eighteen `@Test` methods, one per released
`/conf/controlstream` target
**AND** each method SHALL be independently executable, read-only, grouped as
`part2controlstream`, and mapped to exactly one released requirement URI.

#### SCENARIO-ETS-PART2-003-DIRECT-PREREQUISITE-001 (CRITICAL)
**GIVEN** `/req/controlstream` directly inherits Part 2 `/req/api-common`
**WHEN** `part2apicommon` is absent, skipped, or failed
**THEN** `part2controlstream` SHALL skip before ControlStream IUT access
**AND** scoped endpoint success SHALL NOT imply API Common PASS evidence.

#### SCENARIO-ETS-PART2-003-ASSOCIATION-SUBRESOURCES-001 (CRITICAL)
**GIVEN** ControlStream association procedures for Sampling Features,
Features of Interest, Systems, Deployments, and nested Commands
**WHEN** the applicable prerequisite or association evidence exists
**THEN** the ETS SHALL validate each required nested endpoint using bounded
same-origin traversal and the appropriate JSON or GeoJSON schema
**AND** missing conditions or empty parent resources SHALL SKIP before nested
subresource access rather than producing false PASS or noisy downstream
failures.

#### SCENARIO-ETS-PART2-003-RELEASED-ENDPOINT-SCHEMAS-001 (CRITICAL)
**GIVEN** released ControlStream, Command, CommandStatus, and CommandResult
resources endpoints
**WHEN** the ETS retrieves canonical, nested, or collection item endpoints
**THEN** every supported `application/json` page SHALL validate against the
corresponding bundled collection and item schema
**AND** unsupported media SHALL warn and skip the owning procedure rather than
being parsed as JSON.

#### SCENARIO-ETS-PART2-003-CANONICAL-LINK-EVIDENCE-001 (CRITICAL)
**GIVEN** ControlStream and Command canonical URL procedures
**WHEN** a resource is retrieved through any non-canonical collection endpoint
**THEN** the ETS SHALL require an advertised same-origin `rel=canonical` link,
dereference it, validate the singleton resource schema, and compare content
after removing canonical links
**AND** synthesized `/controlstreams/{id}`, `/controls/{id}`, or
`/commands/{id}` guesses SHALL NOT replace advertised canonical evidence.

#### SCENARIO-ETS-PART2-003-COLLECTION-TAGGING-001 (CRITICAL)
**GIVEN** the released ControlStream and Command `/collections` procedures
**WHEN** `/collections` advertises selected ControlStream or Command
collections
**THEN** ControlStream collections SHALL be tagged as `itemType=ControlStream`
**AND** Command collections SHALL be tagged as `itemType=Command`
**AND** absence of relevant collection metadata SHALL be reported honestly.

#### SCENARIO-ETS-PART2-003-SCHEMA-OP-FORMATS-001 (CRITICAL)
**GIVEN** `/req/controlstream/schema-op`
**WHEN** a ControlStream advertises one or more Command formats
**THEN** the ETS SHALL request `/controlstreams/{id}/schema?cmdFormat={format}`
for every advertised format and validate the response against the bundled
Command Schema representation
**AND** no advertised format SHALL be collapsed to one unparameterized schema
GET.

#### SCENARIO-ETS-PART2-003-STATUS-RESULT-ENDPOINTS-001 (CRITICAL)
**GIVEN** released CommandStatus and CommandResult endpoint procedures
**WHEN** Command resources are available
**THEN** the ETS SHALL validate `/commands/{cmdId}/status` and
`/commands/{cmdId}/result` as CommandStatus and CommandResult resources
endpoints for every applicable Command
**AND** empty Command collections SHALL SKIP those child checks with precise
data-availability reasons.

#### SCENARIO-ETS-PART2-003-EXACT-MAPPING-001 (CRITICAL)
**GIVEN** the reviewed ATS mapping file and generated coverage report
**WHEN** Sprint 61 coverage gates run
**THEN** `2:/conf/controlstream` reports `18 exact / 0 candidate / 0 unmapped`
**AND** the overall exact count increases by eighteen without changing the
released inventory.

#### SCENARIO-ETS-PART2-003-SMOKE-NO-MUTATION-001 (CRITICAL)
**GIVEN** the mandatory local OSH TeamEngine E2E gate
**WHEN** Sprint 61 ControlStream procedures run against the unmodified local
OSH IUT
**THEN** the outcome is documented with concrete pass/fail/skip totals
**AND** IUT-bound request logs contain zero POST, PUT, PATCH, or DELETE.

#### REQ-ETS-PART2-004: Part 2 Command Feasibility Conformance Suite
- **Priority**: MUST.
- **Status**: IMPLEMENTED_RELEASED_ATS_EXACT
- **Historical increment**: Sprint 23 safety subset; Sprint 62 exact replacement.
- **Description**: The ETS provides a TestNG suite for OGC 23-002 Clause 11 Requirements Class "Command Feasibility" using official identifiers `/req/feasibility` and `/conf/feasibility`, with prerequisite `/req/controlstream`. Sprint 62 replaces the historical safety subset with exactly the five released Annex A.4 procedures: `/conf/feasibility/canonical-url`, `/conf/feasibility/ref-from-controlstream`, `/conf/feasibility/status-endpoint`, `/conf/feasibility/result-endpoint`, and `/conf/feasibility/collections`.
- **Scope guard**: Feasibility requests are initiated by creating a Command resource on the feasibility channel. Default Sprint 62 execution SHALL remain read-only and SHALL NOT issue IUT-bound feasibility POST, PUT, PATCH, or DELETE requests. The suite SHALL NOT correct released Annex A.4 copy-text inconsistencies by substituting unlisted endpoints; it SHALL document those inconsistencies and execute the released procedure text literally.
- **Implementation evidence**: `Part2FeasibilityTests` now exposes exactly five TestNG procedures and `part2feasibility` depends directly on `part2controlstream`. Reviewed coverage reports `2:/conf/feasibility` as `5 exact / 0 candidate / 0 unmapped`; focused corrected verification passed `83/0/0/0`; coverage audit passed `23/0/0/0`; full Docker Maven passed with existing skips `760 tests / 0 failures / 0 errors / 3 skipped`. Mandatory local OSH TeamEngine smoke reported honest non-green `252 total / 36 passed / 21 failed / 195 skipped`; all five Feasibility procedures SKIP before Feasibility IUT access because `part2controlstream` setup skipped through the local OSH prerequisite chain. The no-mutation oracle recognized 184 local-OSH IUT request logs and zero writes.
- **Maps to**: PRD FR-ETS-33.

#### SCENARIO-ETS-PART2-004-RELEASED-PROCEDURES-001 (CRITICAL)
**GIVEN** OGC 23-002 Annex A.4 lists exactly five `/conf/feasibility` abstract tests
**WHEN** the deployed Feasibility TestNG class is inspected
**THEN** it SHALL expose exactly five `@Test` methods, one per released procedure
**AND** it SHALL NOT expose historical declaration, dependency, or safety tracer methods as separate deployed tests.

#### SCENARIO-ETS-PART2-004-DIRECT-PREREQUISITE-001 (CRITICAL)
**GIVEN** Annex A.4 lists `/conf/controlstream` as the direct prerequisite
**WHEN** TestNG dependency wiring is inspected
**THEN** `part2feasibility` SHALL depend directly on `part2controlstream`
**AND** setup SHALL skip before Feasibility IUT access if the ControlStream prerequisite failed or skipped.

#### SCENARIO-ETS-PART2-004-ANNEX-COPY-TEXT-001 (CRITICAL)
**GIVEN** Annex A.4 copy text contains Command/Feasibility inconsistencies
**WHEN** implementing exact released ATS coverage
**THEN** the ETS SHALL preserve the released procedure behavior literally
**AND** document that A.35 selects `itemType=Command`, A.36 delegates to the ControlStream Command endpoint procedure, and A.39 selects `itemType=Feasibility` while validating Command schema.

#### SCENARIO-ETS-PART2-004-CANONICAL-COMMAND-COLLECTION-001 (NORMAL)
**GIVEN** `/conf/feasibility/canonical-url` iterates collections advertised with `itemType=Command`
**WHEN** supported collection items are retrieved
**THEN** each item SHALL expose exactly one same-origin canonical link
**AND** dereferencing the canonical link SHALL return schema-valid equivalent Command content after canonical links are removed.

#### SCENARIO-ETS-PART2-004-CONTROLSTREAM-COMMAND-REFERENCE-001 (NORMAL)
**GIVEN** `/conf/feasibility/ref-from-controlstream` retrieves all canonical ControlStream resources
**WHEN** the ETS evaluates each ControlStream
**THEN** it SHALL validate `{api_root}/controlstreams/{dsId}/commands` using the released `/conf/controlstream/cmd-resources-endpoint` behavior
**AND** it SHALL NOT substitute an unlisted singular `/controlstream/{id}/feasibility` PASS condition for the released Annex A.4 method.

#### SCENARIO-ETS-PART2-004-STATUS-RESULT-ENDPOINTS-001 (NORMAL)
**GIVEN** canonical Feasibility resources are available
**WHEN** `/conf/feasibility/status-endpoint` and `/conf/feasibility/result-endpoint` execute
**THEN** every Feasibility resource SHALL expose schema-valid `/feasibility/{cmdId}/status` and `/feasibility/{cmdId}/result` endpoints using the released ControlStream status/result endpoint procedures.

#### SCENARIO-ETS-PART2-004-COLLECTION-TAGGING-001 (NORMAL)
**GIVEN** `/collections` advertises collections with `itemType=Feasibility`
**WHEN** supported collection items are retrieved
**THEN** each page SHALL be validated as a Command resources endpoint according to the released Annex A.4 collections procedure.

#### SCENARIO-ETS-PART2-004-SMOKE-NO-MUTATION-001 (CRITICAL)
**GIVEN** the mandatory local OSH TeamEngine E2E gate
**WHEN** Sprint 62 Feasibility procedures run against the unmodified local OSH IUT
**THEN** the outcome is documented with concrete pass/fail/skip totals
**AND** IUT-bound request logs contain zero POST, PUT, PATCH, or DELETE.

#### REQ-ETS-PART2-005: Part 2 System Events Conformance Suite
- **Priority**: MUST.
- **Status**: IMPLEMENTED_RELEASED_ATS_EXACT
- **Historical increment**: Sprint 24 Generator implemented a useful read-only
  subset. Sprint 63 supersedes it with exact released ATS closure.
- **Description**: The ETS SHALL provide a TestNG suite for OGC 23-002 Clause
  12 and Annex A.5 Requirements Class "System Events" using official
  identifiers `/req/system-event` and `/conf/system-event`, with released
  prerequisites `/req/api-common` and Part 1 `/req/system`. Sprint 63 SHALL
  expose exactly the five released Annex A.5 procedures:
  `/conf/system-event/canonical-url`,
  `/conf/system-event/resources-endpoint`,
  `/conf/system-event/canonical-endpoint`,
  `/conf/system-event/ref-from-system`, and
  `/conf/system-event/collections`.
- **Scope guard**: The exact released ATS closure SHALL NOT implement
  streaming/SSE event consumption, System History, Advanced Filtering
  event-by-type, or mutation classes. It SHALL NOT infer System Events
  conformance from sibling Part 2 declarations. It SHALL follow the released
  Annex A.5 procedure copy text literally while documenting apparent
  inconsistencies: A.40 says `ControlStream` / `itemType=ControlStream`, A.42
  references a ControlStream resources-endpoint test label for the canonical
  `/systemEvents` endpoint, and A.43 uses `/systems/{sysId}/systemEvents`
  while Clause 12 Requirement 43 uses `/systems/{sysId}/events`.
- **Implementation evidence**: Sprint 63 replaces the historical Sprint 24
  subset with exactly five deployed Annex A.5 procedures in
  `Part2SystemEventTests`, direct `part2systemevent -> part2apicommon
  systemfeatures` TestNG inheritance, released SystemEvent
  collection/item schema validation, literal Annex copy-text handling, and
  reviewed exact mappings for all five `/conf/system-event` targets. Focused
  test-first red captured missing helper compile failures after formatter
  normalization; corrected focused verification passed `84/0/0/0`; coverage
  update passed `1/0/0/0`; coverage audit passed `23/0/0/0`; full Docker
  Maven completed `763/0/0/3`. Local OSH TeamEngine E2E is honest non-green at
  `251/36/21/194`; all five System Events methods SKIP before SystemEvent IUT
  access because prerequisite `canonicalSystemsEndpointIsValid` skipped. The
  no-mutation oracle recognized 182 local-OSH IUT request logs, all GET.
- **Maps to**: PRD FR-ETS-34.

#### SCENARIO-ETS-PART2-005-RELEASED-PROCEDURES-001 (CRITICAL)
**GIVEN** OGC 23-002 Annex A.5 lists exactly five `/conf/system-event` tests
**WHEN** the deployed System Events class is inspected
**THEN** it SHALL expose exactly one independent TestNG method for each released
procedure
**AND** it SHALL NOT expose standalone declaration, prerequisite, diagnostic,
or safety-tracer methods outside Annex A.5.

#### SCENARIO-ETS-PART2-005-DIRECT-PREREQUISITES-001 (CRITICAL)
**GIVEN** Annex A.5 lists prerequisites `/conf/api-common` and Part 1 `/conf/system`
**WHEN** `part2systemevent` is wired in `testng.xml`
**THEN** it SHALL depend directly on `part2apicommon` and `systemfeatures`
**AND** runtime setup SHALL SKIP before SystemEvent IUT access when either
prerequisite has failed or skipped.

#### SCENARIO-ETS-PART2-005-ANNEX-COPY-TEXT-001 (CRITICAL)
**GIVEN** Annex A.5 contains copy text that diverges from Clause 12 wording
**WHEN** the ETS maps released procedures
**THEN** it SHALL preserve the released Annex A.5 procedure paths and selectors
**AND** it SHALL document the divergence rather than silently substituting
Clause 12 or historical Sprint 24 endpoints.

#### SCENARIO-ETS-PART2-005-CANONICAL-CONTROLSTREAM-COLLECTION-001 (CRITICAL)
**GIVEN** released test `/conf/system-event/canonical-url` selects
`itemType=ControlStream` collections
**WHEN** those collection items are retrieved
**THEN** every item SHALL expose one same-origin canonical link
**AND** the dereferenced resource SHALL return HTTP 200 with equivalent content
after canonical links are removed.

#### SCENARIO-ETS-PART2-005-RESOURCE-ENDPOINT-SCHEMA-001 (CRITICAL)
**GIVEN** released test `/conf/system-event/resources-endpoint` validates a
SystemEvent resources endpoint
**WHEN** the endpoint returns `application/json`
**THEN** the ETS SHALL validate the collection against
`systemEventCollection.json` and every item against `systemEvent.json`
**AND** unsupported media SHALL SKIP instead of producing shape-only PASS.

#### SCENARIO-ETS-PART2-005-CANONICAL-ENDPOINT-001 (CRITICAL)
**GIVEN** released test `/conf/system-event/canonical-endpoint` identifies
`{api_root}/systemEvents`
**WHEN** the IUT declares `/conf/system-event`
**THEN** the ETS SHALL validate `/systemEvents` as a SystemEvent resources
endpoint
**AND** HTTP 400 or streaming-only responses SHALL NOT produce PASS.

#### SCENARIO-ETS-PART2-005-SYSTEM-REFERENCE-001 (CRITICAL)
**GIVEN** released test `/conf/system-event/ref-from-system` uses
`{api_root}/systems/{sysId}/systemEvents`
**WHEN** canonical System resources are available
**THEN** the ETS SHALL validate each released A.43 nested endpoint as a
SystemEvent resources endpoint
**AND** the historical Sprint 24 `/systems/{sysId}/events` endpoint SHALL NOT
be used for exact released ATS PASS evidence.

#### SCENARIO-ETS-PART2-005-SYSTEM-EVENT-COLLECTIONS-001 (NORMAL)
**GIVEN** `/req/system-event/collections` applies when the server exposes SystemEvent collections
**WHEN** a collection has `itemType` equal to `SystemEvent`
**THEN** the ETS SHALL verify that `/collections/{collectionId}/items` behaves as a System Event resources endpoint
**AND** it SHALL NOT fail an IUT solely because no SystemEvent collection is advertised.

#### SCENARIO-ETS-PART2-005-SMOKE-NO-MUTATION-001 (CRITICAL)
**GIVEN** Sprint 63 System Events procedures are read-only
**WHEN** they run against the unmodified local OSH IUT
**THEN** the outcome SHALL be documented with concrete pass/fail/skip totals
**AND** IUT-bound request logs SHALL contain zero POST, PUT, PATCH, or DELETE.

#### REQ-ETS-PART2-006: Part 2 Advanced Filtering Conformance Suite
- **Priority**: MUST
- **Status**: IMPLEMENTED_RELEASED_ATS_EXACT
- **Historical increments**: Sprint 25 partial subset; Sprint 64 exact released ATS closure.
- **Description**: The ETS SHALL implement the released OGC 23-002 Clause 13 Requirements Class "Advanced Filtering" using official identifiers `/req/advanced-filtering` and `/conf/advanced-filtering`. Sprint 64 supersedes the Sprint 25 subset with exact released ATS closure: one independent read-only procedure for each Annex A.6 target, exact prerequisite inheritance, no false PASS from undeclared query behavior, and no IUT mutation.
- **Rationale**: Advanced Filtering extends previously implemented Part 2 resource classes with query parameters across DataStream, Observation, ControlStream, Command, CommandStatus, and SystemEvent endpoints.
- **Maps to**: PRD FR-ETS-36.
- **Requirements class**: `/req/advanced-filtering`.
- **Conformance class**: `/conf/advanced-filtering`.
- **Prerequisites**: `/req/api-common` and `http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering`.
- **In-scope normative statements for Sprint 64**: `/req/advanced-filtering/datastream-by-phenomenontime`, `/req/advanced-filtering/datastream-by-resulttime`, `/req/advanced-filtering/datastream-by-obsprop`, `/req/advanced-filtering/datastream-by-foi`, `/req/advanced-filtering/obs-by-phenomenontime`, `/req/advanced-filtering/obs-by-resulttime`, `/req/advanced-filtering/obs-by-foi`, `/req/advanced-filtering/controlstream-by-issuetime`, `/req/advanced-filtering/controlstream-by-exectime`, `/req/advanced-filtering/controlstream-by-controlprop`, `/req/advanced-filtering/controlstream-by-foi`, `/req/advanced-filtering/cmd-by-issuetime`, `/req/advanced-filtering/cmd-by-exectime`, `/req/advanced-filtering/cmd-by-status`, `/req/advanced-filtering/cmd-by-sender`, `/req/advanced-filtering/cmd-by-foi`, `/req/advanced-filtering/status-by-statuscode`, and `/req/advanced-filtering/event-by-type`.
- **Planning correction**: OGC 23-002 Annex A does not define `/conf/system-history` or `/req/system-history`; GeoRobotix advertises `/conf/system-history` as a non-standard/vendor extension. The former `REQ-ETS-PART2-006` System History placeholder is retired from the OGC conformance-class backlog.
- **Implementation evidence**: Sprint 64 replaces the Sprint 25 nine-method subset with exactly eighteen independent TestNG methods, one per released Annex A.6 target. `part2advancedfiltering` depends directly on `part2apicommon advancedfiltering`; class setup skips before Advanced Filtering IUT access when either prerequisite chain is incomplete. Runtime checks are read-only and validate DataStream, Observation, ControlStream, Command, CommandStatus, and SystemEvent filtered responses through released Part 2 resource helpers. PASS requires seed-derived predicate evidence; unavailable endpoints, empty positive evidence, undeclared `/conf/advanced-filtering`, or incomplete prerequisites produce SKIP with reason rather than false PASS. `VerifyPart2AdvancedFilteringTests` and `VerifyTestNGSuiteDependency` enforce exact method count, target uniqueness, REQ/SCENARIO traceability, no standalone declaration/prerequisite helper methods, FOI/CommandStatus coverage, and released TestNG dependency wiring.
- **Verification**: Sprint 64 formatter logs pass; focused red first reproduced formatting and historical structural gaps; corrected focused Docker Maven passed `83 tests / 0 failures / 0 errors / 0 skipped`; coverage update passed `1/0/0/0`; coverage audit passed `23/0/0/0`; full Docker Maven passed `762 tests / 0 failures / 0 errors / 3 skipped`. Raze initial review found `RAZE-ETS64-FALSEPASS-001`, a SystemEvent `event-by-type` false-PASS risk from generic `type` evidence; the gapfix removed that fallback, made `eventType` then `definition` the only event-type evidence, and added the requested `type=SystemEvent` plus actual `definition` regression. Post-gapfix focused Docker Maven passed `83/0/0/0`; post-gapfix full Docker Maven passed `762/0/0/3`; focused Raze recheck returned `APPROVE 0.97` with `RAZE-ETS64-FALSEPASS-001` closed and no required fixes. Reviewed ATS coverage is `240 total / 153 exact / 2 helper / 77 candidate / 8 unmapped`; Part 2 is `130 total / 62 exact / 0 helper / 60 candidate / 8 unmapped`; `2:/conf/advanced-filtering` is `18 exact / 0 candidate / 0 unmapped`. Post-gapfix mandatory local OSH TeamEngine smoke reached the unmodified local OSH IUT and exited honestly non-green at `260 total / 36 passed / 21 failed / 203 skipped`; all eighteen Sprint 64 methods SKIP before Advanced Filtering IUT access because inherited Part 1 Advanced Filtering prerequisite `indirectPropertyFiltersAreTransitive` skipped. The 21 failures are existing local OSH SensorML/Deployment/Procedure/Property/Sampling Feature gaps outside Sprint 64. The no-mutation oracle recognized 181 local-OSH IUT request logs; the container log records `GET=186` and zero POST/PUT/PATCH/DELETE. TeamEngine 6 runtime immutability passed for post-gapfix smoke image `sha256:fe3c3f03d1ffbc0b5e657a23f3e079c3983116203ec2457e6bbaf83f58f63f28`; local OSH remained clean at checkout `4c87a65`, zero commits ahead of upstream, with `/opt/osh` mounted read-only and ConSys bundle build `4c87a65`.
- **Acceptance scenarios implemented**:
  - `SCENARIO-ETS-PART2-006-ADVFILTER-CONFORMANCE-DECLARED-001`
  - `SCENARIO-ETS-PART2-006-DEPENDENCY-SKIP-001`
  - `SCENARIO-ETS-PART2-006-DATASTREAM-FILTERS-READONLY-001`
  - `SCENARIO-ETS-PART2-006-OBSERVATION-FILTERS-READONLY-001`
  - `SCENARIO-ETS-PART2-006-CONTROLSTREAM-FILTERS-READONLY-001`
  - `SCENARIO-ETS-PART2-006-COMMAND-FILTERS-READONLY-001`
  - `SCENARIO-ETS-PART2-006-SYSTEM-EVENT-FILTER-READONLY-001`
  - `SCENARIO-ETS-PART2-006-UNDECLARED-FILTER-HONESTY-001`
  - `SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001`
- **GeoRobotix planning probe**: `/conformance` does not declare `/conf/advanced-filtering`, even though selected read-only filter requests currently return mixed behavior: `GET /datastreams?phenomenonTime=...`, `/datastreams?resultTime=...`, `/datastreams?observedProperty=...`, `/controlstreams?issueTime=...`, and `/controlstreams?executionTime=...` returned HTTP 200; `GET /observations?phenomenonTime=...` and `/observations?resultTime=...` returned HTTP 200 with empty `items`; `GET /commands?...` and `/systemEvents?eventType=...` returned HTTP 400; `GET /systems/{id}/events?eventType=...` returned HTTP 400 streaming-only. These probe results are readiness diagnostics only and must not produce Advanced Filtering PASS while `/conf/advanced-filtering` is absent.

##### Acceptance Scenarios for Sprint 64

#### SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001 (CRITICAL)
**GIVEN** the ETS is evaluating OGC 23-002 Annex A.6 Advanced Filtering
**WHEN** the deployed TestNG class is inspected or executed
**THEN** it exposes exactly eighteen released procedures
**AND** each method traces `REQ-ETS-PART2-006` and exactly one canonical `/req/advanced-filtering/*` target
**AND** no standalone declaration or prerequisite tracer method is counted as a released procedure.

#### SCENARIO-ETS-PART2-006-ADVFILTER-CONFORMANCE-DECLARED-001 (CRITICAL)
**GIVEN** the ETS is evaluating OGC 23-002 Advanced Filtering
**WHEN** it reads `/conformance`
**THEN** `/conf/advanced-filtering` is required before producing Part 2 Advanced Filtering PASS evidence
**AND** undeclared filter behavior remains readiness evidence only.

#### SCENARIO-ETS-PART2-006-DEPENDENCY-SKIP-001 (CRITICAL)
**GIVEN** Part 2 Advanced Filtering depends on `/req/api-common` and Part 1 `/req/advanced-filtering`
**WHEN** either prerequisite cannot be established
**THEN** the ETS reports prerequisite-incomplete SKIP behavior instead of full class closure.

#### SCENARIO-ETS-PART2-006-DATASTREAM-FILTERS-READONLY-001 (NORMAL)
**GIVEN** `/req/advanced-filtering/datastream-*` applies
**WHEN** the ETS evaluates bounded `phenomenonTime`, `resultTime`, or `observedProperty` filters
**THEN** it uses read-only GET requests
**AND** every non-empty returned DataStream item must satisfy the requested predicate before PASS.

#### SCENARIO-ETS-PART2-006-OBSERVATION-FILTERS-READONLY-001 (NORMAL)
**GIVEN** `/req/advanced-filtering/obs-*` applies
**WHEN** the ETS evaluates bounded Observation time filters
**THEN** every non-empty returned Observation item must intersect the requested time before PASS
**AND** empty seed-derived filtered collections SKIP with a precise no-predicate-evidence reason.

#### SCENARIO-ETS-PART2-006-CONTROLSTREAM-FILTERS-READONLY-001 (NORMAL)
**GIVEN** `/req/advanced-filtering/controlstream-*` applies
**WHEN** the ETS evaluates bounded `issueTime`, `executionTime`, or `controlledProperty` filters
**THEN** every non-empty returned ControlStream item must satisfy the requested predicate before PASS.

#### SCENARIO-ETS-PART2-006-COMMAND-FILTERS-READONLY-001 (NORMAL)
**GIVEN** `/req/advanced-filtering/cmd-*` applies
**WHEN** the global `/commands` endpoint is available with seed evidence
**THEN** the ETS checks supported read-only Command filters and returned predicates
**AND** SKIPs when `/commands` is unavailable, streaming-only, or lacks seed evidence.

#### SCENARIO-ETS-PART2-006-SYSTEM-EVENT-FILTER-READONLY-001 (NORMAL)
**GIVEN** `/req/advanced-filtering/event-by-type` applies
**WHEN** `/systemEvents` is available with JSON SystemEvent seed evidence
**THEN** the ETS checks `eventType` filtering using the canonical camel-case `/systemEvents` path
**AND** SKIPs when only unavailable or streaming-only event evidence exists.

#### SCENARIO-ETS-PART2-006-UNDECLARED-FILTER-HONESTY-001 (CRITICAL)
**GIVEN** an IUT returns HTTP 200 for some filter query parameters without declaring `/conf/advanced-filtering`
**WHEN** the Advanced Filtering group runs
**THEN** those responses do not produce Advanced Filtering PASS
**AND** sibling Part 2 declarations or the non-standard `/conf/system-history` declaration do not imply Advanced Filtering conformance.

#### REQ-ETS-PART2-007: Part 2 Create/Replace/Delete Conformance Suite
- **Priority**: MUST (eventually); SHALL NOT be scoped into Sprint 1.
- **Status**: RELEASED_ATS_PARTIAL_UNREVIEWED
- **Historical increment**: (Sprint 26 Generator; seeded local OSH E2E accepted after fixture repair; GeoRobotix public smoke currently fails as advisory external evidence)
- **Description**: The ETS SHALL provide a declaration-gated, mutation-safe TestNG suite for OGC 23-002 Clause 14 Requirements Class "Create/Replace/Delete" using official identifiers `/req/create-replace-delete` and `/conf/create-replace-delete`. Sprint 26 implements the first safety-gated subset: exact Part 2 declaration, visible OGC API - Features - Part 4 Create/Replace/Delete prerequisite, read-only OPTIONS readiness diagnostics, unavailable-endpoint honesty, public GeoRobotix mutation hard-denial, and explicit dedicated mutable-IUT opt-in gates before POST, PUT, or DELETE lifecycle checks can run. Positive non-system lifecycle mutation and cascade validation remain deferred.
- **Normative statement set for planning**: Requirements 63-78: `/req/create-replace-delete/datastream`, `/req/create-replace-delete/datastream-update-schema`, `/req/create-replace-delete/datastream-delete-cascade`, `/req/create-replace-delete/observation`, `/req/create-replace-delete/observation-schema`, `/req/create-replace-delete/controlstream`, `/req/create-replace-delete/controlstream-update-schema`, `/req/create-replace-delete/controlstream-delete-cascade`, `/req/create-replace-delete/command`, `/req/create-replace-delete/command-schema`, `/req/create-replace-delete/command-status`, `/req/create-replace-delete/command-result`, `/req/create-replace-delete/feasibility`, `/req/create-replace-delete/feasibility-status`, `/req/create-replace-delete/feasibility-result`, and `/req/create-replace-delete/system-event`.
- **Rationale**: Clause 14 is destructive by nature and delegates lifecycle semantics to OGC API Features Part 4 Create/Replace/Delete at Connected Systems resource endpoints. The ETS must make progress on declaration/prerequisite/readiness checks without producing false PASS from broad OPTIONS headers or mutating GeoRobotix.
- **Implementation evidence**: `Part2CreateReplaceDeleteTests` adds 9 TestNG methods and `VerifyPart2CreateReplaceDeleteTests` adds 9 helper regressions. `testng.xml` declares the `part2createreplacedelete` group with `core common systemfeatures` dependencies, and `VerifyTestNGSuiteDependency` adds structural coverage for dependency, group tagging, and co-location. Raze implementation review found a high endpoint-fidelity gap in the first draft; the gapfix changed DataStream, Observation, and ControlStream OPTIONS readiness probes to OGC 23-002 Clause 14 scoped templates (`/systems/{sysId}/datastreams`, `/datastreams/{dsId}/observations`, and `/systems/{sysId}/controlstreams`) and added path-selection regressions. Focused Raze gapfix recheck returned `APPROVE_WITH_CONCERNS` confidence 0.94 with the scoped-endpoint gap closed. Formatter and Maven completed successfully on 2026-05-22 (`207 tests / 0 failures / 0 errors / 3 skipped`). GeoRobotix TeamEngine smoke reached TestNG but failed `146 total / 27 passed / 5 failed / 114 skipped` because the public IUT returned HTTP 500 for existing SystemFeatures/Datastream/Observation reads. Local OSH fallback initially failed `146 total / 61 passed / 4 failed / 81 skipped` due existing SensorML alternate-resource HTTP 500 responses; the local OSH seedfix added Procedure/Deployment `featureType` metadata and reran TeamEngine with `146 total / 62 passed / 0 failed / 84 skipped`. The new Part 2 Create/Replace/Delete methods were either dependency-skipped on GeoRobotix or passed/skipped honestly on local OSH without issuing Part 2 lifecycle mutation.
- **Maps to**: PRD FR-ETS-37.

#### SCENARIO-ETS-PART2-007-CRD-CONFORMANCE-DECLARED-001 (CRITICAL)
**GIVEN** the IUT exposes `/conformance`
**WHEN** the Part 2 Create/Replace/Delete group runs
**THEN** the ETS detects exact declaration `http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/create-replace-delete`
**AND** SKIPs the group with a precise reason when the declaration is absent.

#### SCENARIO-ETS-PART2-007-FEATURES4-PREREQUISITE-001 (CRITICAL)
**GIVEN** `/req/create-replace-delete` applies
**WHEN** the ETS evaluates full class closure
**THEN** it records whether `http://www.opengis.net/spec/ogcapi-features-4/1.0/conf/create-replace-delete` is declared
**AND** does not report full `/conf/create-replace-delete` closure when the prerequisite is missing.

#### SCENARIO-ETS-PART2-007-MUTATION-SAFETY-GATE-001 (CRITICAL)
**GIVEN** an advisory public GeoRobotix probe is explicitly selected
**WHEN** Create/Replace/Delete tests execute
**THEN** POST, PUT, DELETE, and PATCH requests are blocked before dispatch
**AND** positive lifecycle checks require `mutation-tests-enabled=true` and `mutation-iut-policy=dedicated-mutable-iut`.

#### SCENARIO-ETS-PART2-007-OPTIONS-READINESS-READONLY-001 (NORMAL)
**GIVEN** the IUT declares `/conf/create-replace-delete`
**WHEN** the ETS probes resource endpoints with read-only OPTIONS requests
**THEN** it may record advertised create, replace, and delete methods as readiness diagnostics
**AND** OPTIONS evidence alone SHALL NOT PASS a lifecycle assertion.

#### SCENARIO-ETS-PART2-007-DATASTREAM-OBSERVATION-LIFECYCLE-OPTIN-001 (NORMAL)
**GIVEN** a dedicated mutable IUT is explicitly enabled
**WHEN** the ETS exercises DataStream and Observation Create/Replace/Delete checks
**THEN** it validates lifecycle behavior using OGC API Features Part 4 semantics at the Connected Systems endpoints
**AND** cleans up created resources before test completion.

#### SCENARIO-ETS-PART2-007-CONTROLSTREAM-COMMAND-LIFECYCLE-OPTIN-001 (NORMAL)
**GIVEN** a dedicated mutable IUT is explicitly enabled
**WHEN** the ETS exercises ControlStream, Command, CommandStatus, and CommandResult Create/Replace/Delete checks
**THEN** it validates accepted resource shapes, schema-rejection behavior where applicable, and cleanup
**AND** SKIPs honestly when command endpoints are unavailable.

#### SCENARIO-ETS-PART2-007-FEASIBILITY-SYSTEMEVENT-LIFECYCLE-OPTIN-001 (NORMAL)
**GIVEN** a dedicated mutable IUT is explicitly enabled
**WHEN** the ETS exercises Feasibility, Feasibility status/result, and SystemEvent Create/Replace/Delete checks
**THEN** it validates lifecycle behavior only against JSON resources that are available for the IUT
**AND** SKIPs rather than PASSes when endpoints are absent, invalid resources, or streaming-only.

#### SCENARIO-ETS-PART2-007-UNAVAILABLE-ENDPOINT-HONESTY-001 (CRITICAL)
**GIVEN** the IUT advertises `/conf/create-replace-delete`
**WHEN** `/commands`, `/feasibility`, `/systemEvents`, or `/systems/{sysId}/events` are not readable JSON resource endpoints
**THEN** the ETS does not infer lifecycle PASS from sibling declarations, broad OPTIONS headers, or HTTP 400 responses.

#### SCENARIO-ETS-PART2-007-SMOKE-NO-PUBLIC-MUTATION-001 (CRITICAL)
**GIVEN** TeamEngine smoke runs an advisory public GeoRobotix probe
**WHEN** the smoke run completes
**THEN** request logs contain zero IUT-bound POST, PUT, DELETE, or PATCH requests
**AND** any Create/Replace/Delete assertions that need mutation SKIP before dispatch.

#### REQ-ETS-PART2-008: Part 2 Update Conformance Suite
- **Priority**: MUST (eventually); SHALL NOT be scoped into Sprint 1.
- **Status**: RELEASED_ATS_PARTIAL_UNREVIEWED
- **Historical increment**: (Sprint 27 Generator; positive PATCH lifecycle remains deferred)
- **Description**: The ETS SHALL provide a declaration-gated, mutation-safe TestNG suite for OGC 23-002 Clause 15 Requirements Class "Update" using official identifiers `/req/update` and `/conf/update`. Sprint 27 implements the first safe Generator increment: exact Part 2 Update declaration, visible Part 2 Create/Replace/Delete and OGC API - Features - Part 4 Update prerequisites, Clause 15 condition gates, read-only OPTIONS PATCH readiness diagnostics, unavailable-endpoint honesty, schema-rejection honesty, public GeoRobotix PATCH hard-denial, and explicit dedicated mutable-IUT opt-in gates before PATCH lifecycle checks can run. Positive PATCH lifecycle mutation and concrete schema-rejection PATCH dispatch remain deferred until a dedicated mutable IUT exposes safe fixtures, endpoint PATCH readiness, changed-field GET proof, and cleanup.
- **Normative statement set for planning**: Requirements 79-92: `/req/update/datastream`, `/req/update/datastream-update-schema`, `/req/update/observation`, `/req/update/observation-schema`, `/req/update/controlstream`, `/req/update/controlstream-update-schema`, `/req/update/command`, `/req/update/command-schema`, `/req/update/command-status`, `/req/update/command-result`, `/req/update/feasibility`, `/req/update/feasibility-status`, `/req/update/feasibility-result`, and `/req/update/system-event`.
- **Prerequisites**: Part 2 `/req/create-replace-delete` and `http://www.opengis.net/spec/ogcapi-features-4/1.0/req/update`; the corresponding conformance prerequisites are Part 2 `/conf/create-replace-delete` and `http://www.opengis.net/spec/ogcapi-features-4/1.0/conf/update`.
- **Condition gates**: OGC 23-002 Clause 15 conditions individual Update requirements on the underlying resource classes. Requirements 79-82 SHALL run only when Datastreams & Observations (`/conf/datastream`) applies; Requirements 83-88 SHALL run only when Control Streams & Commands (`/conf/controlstream`) applies; Requirements 89-91 SHALL run only when Command Feasibility (`/conf/feasibility`) applies; Requirement 92 SHALL run only when System Events (`/conf/system-event`) applies. Missing condition classes SHALL produce prerequisite-incomplete SKIP behavior, not PASS from `/conf/update`, endpoint availability, sibling declarations, or OPTIONS.
- **Rationale**: Clause 15 is destructive by nature and delegates PATCH semantics to OGC API Features Part 4 Update at Connected Systems resource endpoints. The ETS must make declaration/prerequisite/readiness progress without producing false PASS from broad OPTIONS headers or mutating GeoRobotix.
- **Planning evidence**: Official OGC 23-002 HTML `https://docs.ogc.org/is/23-002/23-002.html`, Clause 15 and Annex A.8, identifies `/req/update`, `/conf/update`, Requirements 79-92, and Abstract tests A.79-A.92. GeoRobotix `/conformance` declares Part 2 `/conf/create-replace-delete` and Features Part 4 `/conf/create-replace-delete`, but does not declare Part 2 `/conf/update`. GeoRobotix sampled OPTIONS probes for DataStream, Observation, ControlStream, Command, Feasibility, SystemEvent, and system-scoped event endpoints returned HTTP 200 with broad `Allow` headers but no PATCH. Current GeoRobotix read-health probes still return HTTP 500 for `GET /systems/0mqcvdnfoca0`, `GET /datastreams?limit=1`, and `GET /observations?limit=1`; `GET /controlstreams?limit=1` returns HTTP 200 JSON. Local OSH is running and requires Basic auth; unauthenticated `/conformance` returns HTTP 401. Authenticated `/conformance` returns HTTP 200, does not declare Part 2 `/conf/update`, and authenticated `OPTIONS /systems/040g` omits PATCH.
- **Implementation evidence**: `Part2UpdateTests` adds 14 runtime methods plus shared read-only setup for exact declaration, prerequisite visibility, condition-gate visibility, mutation safety, DataStream/Observation OPTIONS PATCH readiness and deferred lifecycle checks, ControlStream/Command OPTIONS PATCH readiness and deferred lifecycle checks, separate Feasibility and SystemEvent OPTIONS PATCH readiness and deferred lifecycle checks, unavailable-endpoint honesty, and schema-rejection honesty. `VerifyPart2UpdateTests` adds 9 helper regressions. `testng.xml` wires `part2update` with `core common systemfeatures` dependencies, and `VerifyTestNGSuiteDependency` verifies group dependency, method tagging, and co-location with prerequisite/condition-gate classes. Formatter returned BUILD SUCCESS. Docker Maven returned BUILD SUCCESS with `219 tests / 0 failures / 0 errors / 3 skipped`. GeoRobotix TeamEngine Generator smoke failed `160 total / 27 passed / 5 failed / 128 skipped` because the public IUT still returns HTTP 500 on existing read paths; all Part 2 Update runtime tests dependency-SKIP because `systemfeatures` does not finish successfully. The archived GeoRobotix log has zero matched PATCH/POST/PUT/DELETE lines and `scripts/no-mutation-oracle.py` recognized 61 IUT-bound request logs with zero IUT-bound write methods. The accepted local OSH TeamEngine E2E gate passed `160 total / 62 passed / 0 failed / 98 skipped`; all 14 Part 2 Update runtime tests SKIP because the local OSH IUT does not declare Part 2 `/conf/update`, and the local OSH container log contains zero PATCH request lines. Existing Part 1 Create/Replace/Delete system POST/PUT/DELETE requests occurred only under explicit dedicated mutable-IUT opt-in.
- **Maps to**: PRD FR-ETS-38.

#### SCENARIO-ETS-PART2-008-UPDATE-CONFORMANCE-DECLARED-001 (CRITICAL)
**GIVEN** the IUT exposes `/conformance`
**WHEN** the Part 2 Update group runs
**THEN** the ETS detects exact declaration `http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/update`
**AND** SKIPs the group with a precise reason when the declaration is absent.

#### SCENARIO-ETS-PART2-008-CRD-FEATURES4-PREREQUISITES-001 (CRITICAL)
**GIVEN** `/req/update` applies
**WHEN** the ETS evaluates full class closure
**THEN** it records whether Part 2 `/conf/create-replace-delete` and OGC API Features Part 4 `/conf/update` are declared
**AND** it does not report full `/conf/update` closure when either prerequisite is missing.

#### SCENARIO-ETS-PART2-008-RESOURCE-CONDITION-GATES-001 (CRITICAL)
**GIVEN** OGC 23-002 Clause 15 conditions Update requirements on underlying Part 2 resource classes
**WHEN** the ETS evaluates Requirements 79-92
**THEN** DataStream and Observation update assertions require `/conf/datastream`
**AND** ControlStream, Command, CommandStatus, and CommandResult update assertions require `/conf/controlstream`
**AND** Feasibility, Feasibility status, and Feasibility result update assertions require `/conf/feasibility`
**AND** SystemEvent update assertions require `/conf/system-event`
**AND** missing condition classes produce prerequisite-incomplete SKIP behavior rather than PASS.

#### SCENARIO-ETS-PART2-008-PATCH-MUTATION-SAFETY-GATE-001 (CRITICAL)
**GIVEN** an advisory public GeoRobotix probe is explicitly selected
**WHEN** Update tests execute
**THEN** PATCH, POST, PUT, and DELETE requests are blocked before dispatch
**AND** positive lifecycle checks require `mutation-tests-enabled=true` and `mutation-iut-policy=dedicated-mutable-iut`.

#### SCENARIO-ETS-PART2-008-OPTIONS-PATCH-READINESS-001 (NORMAL)
**GIVEN** the IUT declares `/conf/update` and a candidate resource endpoint is available
**WHEN** the ETS probes the endpoint with read-only OPTIONS
**THEN** it records whether PATCH is advertised as readiness evidence
**AND** declared `/conf/update` plus successful OPTIONS omitting PATCH FAILs the readiness assertion while lifecycle PATCH SKIPs before dispatch.

#### SCENARIO-ETS-PART2-008-DATASTREAM-OBSERVATION-PATCH-OPTIN-001 (NORMAL)
**GIVEN** a dedicated mutable IUT is explicitly enabled
**WHEN** the ETS exercises DataStream and Observation Update checks
**THEN** it validates PATCH behavior using OGC API Features Part 4 semantics at the Connected Systems endpoints
**AND** verifies the changed field by GET after PATCH before PASS.

#### SCENARIO-ETS-PART2-008-CONTROLSTREAM-COMMAND-PATCH-OPTIN-001 (NORMAL)
**GIVEN** a dedicated mutable IUT is explicitly enabled
**WHEN** the ETS exercises ControlStream, Command, CommandStatus, and CommandResult Update checks
**THEN** it validates PATCH behavior only for available JSON resource endpoints
**AND** SKIPs honestly when command endpoints or candidate resources are unavailable.

#### SCENARIO-ETS-PART2-008-FEASIBILITY-SYSTEMEVENT-PATCH-OPTIN-001 (NORMAL)
**GIVEN** a dedicated mutable IUT is explicitly enabled
**WHEN** the ETS exercises Feasibility, Feasibility status/result, and SystemEvent Update checks
**THEN** it validates PATCH behavior only against available resources
**AND** SKIPs rather than PASSes when endpoints are absent, invalid resources, or streaming-only.

#### SCENARIO-ETS-PART2-008-SCHEMA-REJECTION-HONESTY-001 (NORMAL)
**GIVEN** OGC 23-002 defines schema-rejection requirements for DataStream, Observation, ControlStream, and Command PATCH
**WHEN** the ETS lacks safe mutation opt-in or concrete parent schema evidence
**THEN** it SHALL NOT claim schema-rejection PASS
**AND** it SHALL SKIP with a precise no-safe-evidence reason.

#### SCENARIO-ETS-PART2-008-UNAVAILABLE-ENDPOINT-HONESTY-001 (CRITICAL)
**GIVEN** the IUT advertises `/conf/update`
**WHEN** `/commands`, `/feasibility`, `/systemEvents`, or `/systems/{sysId}/events` are not readable JSON resource endpoints
**THEN** the ETS does not infer Update lifecycle PASS from sibling declarations, broad OPTIONS headers, HTTP 400, HTTP 500, or streaming-only responses.

#### SCENARIO-ETS-PART2-008-SMOKE-NO-PUBLIC-PATCH-001 (CRITICAL)
**GIVEN** TeamEngine smoke runs an advisory public GeoRobotix probe
**WHEN** the smoke run completes
**THEN** request logs contain zero IUT-bound PATCH, POST, PUT, or DELETE requests
**AND** any Update assertions that need mutation SKIP before dispatch.

#### REQ-ETS-PART2-009: Part 2 JSON Encoding
- **Priority**: MUST
- **Status**: IMPLEMENTED_RELEASED_ATS_RAZE_APPROVED_PUSHED (Sprint 65 CP-025;
  14/14 exact mappings; final adversarial recheck approved; implementation
  commit `1acfdfa` pushed to Botts `main`)
- **Historical increments**: Sprint 28 declaration-gated read-only subset;
  Sprint 65 exact released ATS closure supersedes it.
- **Description**: The ETS SHALL implement the released OGC 23-002 Clause
  16.1 Requirements Class "JSON Encoding" using official `/req/json` and
  `/conf/json` identifiers. Sprint 65 supersedes the Sprint 28 subset with
  exact released ATS closure: one independent read-only procedure for each
  Annex A.9 target, exact class/prerequisite gating before JSON resource
  endpoint access, strict `application/json` retrieval evidence, bounded
  all-page schema validation, released nested endpoint repetitions for
  `/systems/{sysId}/datastreams`, `/datastreams/{dsId}/observations`,
  `/systems/{sysId}/controlstreams`, `/controlstreams/{csId}/commands`, and
  `/systems/{sysId}/events`, parent-schema constraint validation when evidence
  exists, scoped non-mutating OpenAPI write-media advertisement checks that
  require every advertised scoped POST/PUT operation to include exact
  `application/json`, and no IUT mutation.
- **In-scope normative statements for Sprint 65**: `/req/json/mediatype-read`, `/req/json/mediatype-write`, `/req/json/datastream-schema`, `/req/json/obsschema-schema`, `/req/json/observation-schema`, `/req/json/observation-constraints`, `/req/json/controlstream-schema`, `/req/json/commandschema-schema`, `/req/json/command-schema`, `/req/json/command-constraints`, `/req/json/commandstatus-schema`, `/req/json/commandresult-schema`, `/req/json/commandresult-constraints`, and `/req/json/systemevent-schema`.
- **Rationale**: PRD SC-3 requires Part 2 coverage. OGC 23-002 Annex A.9
  defines `/conf/json` with fourteen procedures; Sprint 65 closes the formerly
  unmapped `mediatype-read` procedure, removes standalone declaration/
  prerequisite/resource-gate helper tests from the deployed class, and converts
  the remaining candidates to exact mappings without introducing mutation or
  external validator work.
- **Sprint 65 verification evidence**: Focused corrected verification after
  the Raze recheck gapfix is `88/0/0/0`; coverage update and audit pass, with
  coverage now
  `240 total / 167 exact / 2 helper / 64 candidate / 7 unmapped`, Part 2
  `130 total / 76 exact / 0 helper / 47 candidate / 7 unmapped`, and
  `2:/conf/json` at `14 exact / 0 candidate / 0 unmapped`. Full Docker Maven
  completed `766 tests / 0 failures / 0 errors / 3 skipped`. Mandatory local
  OSH TeamEngine E2E reached the deployed stack and unmodified local OSH, then
  exited honestly non-green at `258 total / 29 passed / 20 failed /
  209 skipped`; all Part 2 JSON procedures SKIP before JSON resource access
  because local OSH does not declare the SWE JSON record-components
  prerequisite. No-mutation oracle recognized 151 local-OSH IUT request logs,
  all GET, with zero POST/PUT/PATCH/DELETE. Evidence is archived under
  `ops/test-results/sprint-ets-65-part2-json-2026-08-01/`. Final Raze recheck
  is `APPROVE 0.96` with both previously open findings closed and
  `required_fixes: []`. Implementation commit `1acfdfa` is pushed to Botts
  `main`.
- **Maps to**: PRD FR-ETS-39.

#### SCENARIO-ETS-PART2-009-RELEASED-PROCEDURES-001 (CRITICAL)
**GIVEN** the released OGC 23-002 Annex A.9 inventory for `/conf/json`
**WHEN** Sprint 65 completes
**THEN** the deployed `Part2JsonTests` class exposes exactly fourteen `@Test` procedures, one for each released target
**AND** no standalone declaration, prerequisite, or resource-condition helper test is exposed as an ATS procedure
**AND** `ops/ats-coverage-report.json` reports `2:/conf/json` as `14 exact / 0 candidate / 0 unmapped`.

#### SCENARIO-ETS-PART2-009-JSON-CONFORMANCE-DECLARED-001 (CRITICAL)
**GIVEN** the IUT exposes `/conformance`
**WHEN** the Part 2 JSON Encoding tests run
**THEN** exact `http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/json` declaration is required before `/req/json` assertions can PASS
**AND** sibling declarations such as Common JSON, GeoJSON, SWE Common JSON, or Part 2 resource classes alone cannot satisfy `/conf/json`.

#### SCENARIO-ETS-PART2-009-SWE-PREREQUISITE-VISIBLE-001 (NORMAL)
**GIVEN** OGC 23-002 Clause 16.1 lists SWE Common 3.0 JSON record components as a prerequisite
**WHEN** the Sprint 65 exact JSON procedures run
**THEN** the prerequisite `http://www.opengis.net/spec/SWE/3.0/conf/json-record-components` must be visible before JSON resource endpoint access
**AND** a declaring IUT with a missing prerequisite produces a precise prerequisite-incomplete SKIP rather than scoped PASS evidence.

#### SCENARIO-ETS-PART2-009-RESOURCE-CONDITION-GATES-001 (CRITICAL)
**GIVEN** Annex A.9 applies JSON representation tests to supported Part 2 resource classes
**WHEN** the ETS evaluates Requirements 95-106
**THEN** DataStream, Observation, Observation Schema, and Observation constraint assertions require `/conf/datastream`
**AND** ControlStream, Command Schema, Command, CommandStatus, CommandResult, and their constraint assertions require `/conf/controlstream`
**AND** SystemEvent JSON assertions require `/conf/system-event`
**AND** missing condition classes produce prerequisite-incomplete SKIP behavior rather than PASS from `/conf/json`, endpoint availability, sibling declarations, or collection shape alone.

#### SCENARIO-ETS-PART2-009-MEDIATYPE-READ-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/json`
**WHEN** the ETS requests supported Part 2 resources with `Accept: application/json`
**THEN** each reachable declared resource endpoint must return HTTP 200, an `application/json` content type, and JSON parseable content before mediatype-read PASS
**AND** HTTP 400, HTTP 500, HTML/text error bodies, or empty non-resource evidence cannot PASS.

#### SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001 (CRITICAL)
**GIVEN** bundled Part 2 JSON Schemas exist under `src/main/resources/schemas/connected-systems-2/json/`
**WHEN** candidate JSON resources or collections are retrieved
**THEN** the ETS validates them against the corresponding schemas named in OGC 23-002 Annex A.9, including `dataStream.json`, `dataStreamCollection.json`, `observationSchemaJson.json`, `observation.json`, `observationCollection.json`, `controlStream.json`, `controlStreamCollection.json`, `commandSchemaJson.json`, `command.json`, `commandCollection.json`, `commandStatus.json`, `commandStatusCollection.json`, `commandResult.json`, `commandResultCollection.json`, `systemEvent.json`, and `systemEventCollection.json`
**AND** no schema-validation PASS is reported when the endpoint is unavailable, the collection has no candidate resource, or a schema fixture is missing.

#### SCENARIO-ETS-PART2-009-OBSERVATION-COMMAND-CONSTRAINTS-001 (NORMAL)
**GIVEN** Requirements 98, 102, and 105 require Observation result/parameters, Command parameters, and CommandResult inline data to follow parent DataStream or ControlStream schemas
**WHEN** parent schema evidence and candidate child resources are present
**THEN** the ETS validates the child JSON values against the relevant parent JSON Schema member before PASS
**AND** absent parent schema evidence or absent candidate child resources produce precise no-safe-evidence SKIP behavior
**AND** it SHALL NOT PASS dynamic-schema constraints from collection shape, hardcoded examples, or sibling class declarations.

#### SCENARIO-ETS-PART2-009-MEDIATYPE-WRITE-ADVERTISEMENT-001 (NORMAL)
**GIVEN** Requirement 94 applies only when Create/Replace/Delete is implemented
**WHEN** the ETS checks JSON write-media-type support in the first JSON increment
**THEN** it uses API definition or explicit operation metadata to verify that
every advertised CREATE or REPLACE operation on the supported Part 2 resource
endpoint templates implied by declared resource classes includes exact
`application/json` requestBody content
**AND** unrelated POST/PUT operations, partial scoped write coverage,
OPTIONS alone, or mutation requests are not mediatype-write PASS evidence.

#### SCENARIO-ETS-PART2-009-UNAVAILABLE-ENDPOINT-HONESTY-001 (CRITICAL)
**GIVEN** the current public IUT may declare `/conf/json` while individual resource endpoints are unhealthy or unavailable
**WHEN** Datastream, Observation, Command, CommandStatus, CommandResult, or SystemEvent endpoints return HTTP 400, HTTP 500, streaming-only responses, or empty candidate sets
**THEN** the ETS records FAIL for reachable declared requirements that violate HTTP 200/schema expectations, or SKIP when no candidate/evidence exists
**AND** it never converts those outcomes into PASS from declaration, broad media-type lists, or existing sibling tests.

#### SCENARIO-ETS-PART2-009-SMOKE-NO-PUBLIC-MUTATION-001 (CRITICAL)
**GIVEN** TeamEngine smoke runs against the primary local OSH IUT or any
configured non-dedicated IUT
**WHEN** the Part 2 JSON tests execute
**THEN** request logs contain zero IUT-bound POST, PUT, PATCH, or DELETE requests
**AND** any JSON write-media-type or dynamic-schema behavior requiring mutation SKIPs or relies on non-mutating API-definition evidence only.

#### REQ-ETS-PART2-010: Part 2 SWE Common JSON Encoding
- **Priority**: MUST
- **Status**: RELEASED_ATS_EXACT_RAZE_APPROVED_PUSHED
- **Historical increment**: Sprint 29 Generator implemented the first partial subset. Sprint 66 replaces that surface with exact reviewed Annex A.10 procedures.
- **Description**: The ETS SHALL implement the eight released OGC 23-002 Clause 16.2 SWE Common JSON Encoding procedures using official `/req/swecommon-json` and `/conf/swecommon-json` identifiers. Runtime checks SHALL gate on exact `/conf/swecommon-json` declaration and the SWE Common 3.0 JSON Encoding Rules prerequisite before SWE Common JSON resource endpoint access, condition Observation assertions on declared `/conf/datastream`, condition Command assertions on declared `/conf/controlstream`, verify `application/swe+json` read support only from concrete Observation or Command evidence, validate SWE Common schema resources against bundled `observationSchemaSwe.json` and `commandSchemaSwe.json` plus the reusable SWE Common `recordSchema` adapter, and treat write-media-type support as API-definition evidence only unless a safe dedicated mutable IUT is explicitly enabled in a later sprint.
- **Rationale**: PRD SC-3 requires Part 2 coverage. OGC 23-002 Annex A.10 defines `/conf/swecommon-json` with Requirements 107-114. Sprint 66 supersedes the Sprint 29 helper/subset surface by requiring exactly one deployed TestNG method per released Annex A.10 target, adding the previously unmapped mediatype-read procedure, retaining read-only no-mutation behavior, and promoting only procedures with reviewed exact mapping evidence.
- **Maps to**: PRD FR-ETS-40.

#### SCENARIO-ETS-PART2-010-SWEJSON-CONFORMANCE-DECLARED-001 (CRITICAL)
**GIVEN** the IUT exposes `/conformance`
**WHEN** the SWE Common JSON Encoding tests run
**THEN** exact `http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/swecommon-json` declaration is required before `/req/swecommon-json` assertions can PASS
**AND** sibling declarations such as `/conf/json`, `/conf/swecommon-text`, `/conf/swecommon-binary`, or resource-class declarations alone cannot satisfy `/conf/swecommon-json`.

#### SCENARIO-ETS-PART2-010-SWE-JSON-ENCODING-RULES-PREREQUISITE-001 (NORMAL)
**GIVEN** OGC 23-002 Clause 16.2 lists SWE Common 3.0 JSON Encoding Rules as a prerequisite
**WHEN** the released Annex A.10 procedures execute
**THEN** `http://www.opengis.net/spec/SWE/3.0/conf/json-encoding-rules` must be visible before SWE Common JSON resource endpoint access
**AND** missing prerequisite evidence produces setup-level SKIP behavior rather than PASS from scoped resource evidence.

#### SCENARIO-ETS-PART2-010-RELEASED-PROCEDURES-001 (CRITICAL)
**GIVEN** OGC 23-002 Annex A.10 defines eight SWE Common JSON test targets
**WHEN** `Part2SweCommonJsonTests` is inspected or executed
**THEN** the deployed class exposes exactly one TestNG method for each released target
**AND** declaration, prerequisite, and resource-condition checks remain setup or per-procedure gates rather than standalone released ATS procedures
**AND** `ops/ats-coverage-report.json` can promote the class only when all eight targets are reviewed exact.

#### SCENARIO-ETS-PART2-010-RESOURCE-CONDITION-GATES-001 (CRITICAL)
**GIVEN** Annex A.10 applies SWE Common JSON representation tests to Observation and Command resources
**WHEN** the ETS evaluates Requirements 109-114
**THEN** Observation schema, Observation schema mapping, and Observation encoding assertions require `/conf/datastream`
**AND** Command schema, Command schema mapping, and Command encoding assertions require `/conf/controlstream`
**AND** missing condition classes produce prerequisite-incomplete SKIP behavior rather than PASS from `/conf/swecommon-json`, endpoint availability, sibling declarations, or media-format lists alone.

#### SCENARIO-ETS-PART2-010-MEDIATYPE-READ-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/swecommon-json`
**WHEN** the ETS requests supported Observation or Command endpoints with `Accept: application/swe+json`
**THEN** at least one supported endpoint must advertise and return HTTP 200 with `Content-Type: application/swe+json` and JSON parseable content before mediatype-read PASS
**AND** `application/json`, `auto`, `text/html`, HTTP 400, HTTP 500, empty collections, or format-list-only evidence cannot PASS mediatype-read.

#### SCENARIO-ETS-PART2-010-SCHEMA-VALIDATION-READONLY-001 (CRITICAL)
**GIVEN** bundled schemas `observationSchemaSwe.json`, `commandSchemaSwe.json`, and shared SWE Common JSON component schemas exist under `src/main/resources/schemas/`
**WHEN** candidate Observation Schema or Command Schema resources are retrieved with `obsFormat=application/swe+json` or `cmdFormat=application/swe+json`
**THEN** the ETS validates them against the corresponding bundled schema
**AND** validates that the media-format member is `application/swe+json` and the `encoding` member is a `JSONEncoding` object
**AND** no schema-validation PASS is reported when the endpoint is unavailable, returns a JSON-format schema instead of SWE Common JSON, or a schema fixture is missing.

#### SCENARIO-ETS-PART2-010-SCHEMA-MAPPING-TIME-001 (NORMAL)
**GIVEN** Requirements 110 and 113 define mandatory SWE Common Time mapping evidence
**WHEN** Observation Schema or Command Schema resources are retrieved
**THEN** Observation schema mapping PASS requires at least one `Time` component whose `definition` is exactly one of `http://www.w3.org/ns/sosa/phenomenonTime`, `http://www.opengis.net/def/property/OGC/0/SamplingTime`, or `http://www.w3.org/ns/sosa/resultTime`
**AND** Command schema mapping PASS requires a `Time` component whose `definition` is exactly `http://www.opengis.net/def/property/OGC/0/IssueTime` when issue-time mapping is present
**AND** mapping PASS must come from retrieved `recordSchema` evidence, not hardcoded examples or sibling JSON schema shape.

#### SCENARIO-ETS-PART2-010-OBSERVATION-COMMAND-ENCODING-GUARDS-001 (NORMAL)
**GIVEN** Requirements 111 and 114 require Observation and Command resources to follow parent DataStream or ControlStream schemas using SWE Common JSON encoding rules
**WHEN** parent schema evidence, candidate child resources, or a SWE Common JSON encoding validator are absent
**THEN** the ETS SKIPs with a precise no-safe-evidence reason
**AND** it SHALL NOT PASS Observation or Command encoding from collection shape, empty candidate sets, `application/json` fallback bodies, or hardcoded examples.

#### SCENARIO-ETS-PART2-010-MEDIATYPE-WRITE-ADVERTISEMENT-001 (NORMAL)
**GIVEN** Requirement 108 applies only when Create/Replace/Delete is implemented
**WHEN** the ETS checks SWE Common JSON write-media-type support
**THEN** it uses API definition operation metadata to verify advertised `application/swe+json` support for CREATE or REPLACE operations on Observation or Command resource endpoints only
**AND** every advertised scoped POST or PUT operation matching those endpoint templates must include exact `application/swe+json` requestBody content
**AND** OPTIONS, unrelated POST/PUT paths, partial scoped write coverage, and subresource paths such as Command status alone are not mediatype-write PASS evidence.

#### SCENARIO-ETS-PART2-010-UNAVAILABLE-ENDPOINT-HONESTY-001 (CRITICAL)
**GIVEN** the current public IUT may declare `/conf/swecommon-json` while individual resource endpoints are unhealthy or inconsistent
**WHEN** DataStream, Observation, Command, or ControlStream schema endpoints return HTTP 400, HTTP 500, empty candidate sets, `application/json` fallback schemas, or wrong media members
**THEN** the ETS records FAIL for reachable declared requirements that violate HTTP 200/schema/media expectations, or SKIP when no candidate/evidence exists
**AND** it never converts those outcomes into PASS from declaration, broad media-format lists, or existing sibling tests.

#### SCENARIO-ETS-PART2-010-SMOKE-NO-PUBLIC-MUTATION-001 (CRITICAL)
**GIVEN** TeamEngine smoke runs against the local OSH IUT or any advisory public IUT
**WHEN** the SWE Common JSON tests execute
**THEN** request logs contain zero IUT-bound POST, PUT, PATCH, or DELETE requests
**AND** any write-media-type or encoding behavior requiring mutation SKIPs or relies on non-mutating API-definition evidence only.

#### REQ-ETS-PART2-011: Part 2 SWE Common Text Encoding
- **Priority**: MUST
- **Status**: RELEASED_ATS_EXACT_IMPLEMENTED
- **Historical increment**: Sprint 30 Generator implemented the first partial subset. Sprint 67 replaces that surface with exact reviewed Annex A.11 procedures.
- **Description**: The ETS SHALL implement the eight released OGC 23-002 Clause 16.3 SWE Common Text Encoding procedures using official `/req/swecommon-text` and `/conf/swecommon-text` identifiers. Runtime checks SHALL gate on exact `/conf/swecommon-text` declaration and the SWE Common 3.0 Text Encoding Rules prerequisite before SWE Common Text resource endpoint access, condition Observation assertions on declared `/conf/datastream`, condition Command assertions on declared `/conf/controlstream`, verify `application/swe+text` read support only from concrete Observation or Command evidence, validate SWE Common schema metadata against bundled `observationSchemaSwe.json` and `commandSchemaSwe.json` plus the reusable SWE Common `recordSchema` adapter, and treat write-media-type support as API-definition evidence only unless a safe dedicated mutable IUT is explicitly enabled in a later sprint.
- **Rationale**: PRD SC-3 requires Part 2 coverage. OGC 23-002 Clause 16.3 and Annex A.11 define `/conf/swecommon-text` with Requirements 115-122. Sprint 67 supersedes the Sprint 30 helper/subset surface by requiring exactly one deployed TestNG method per released Annex A.11 target, adding the previously unmapped mediatype-read procedure, retaining read-only no-mutation behavior, and promoting only procedures with reviewed exact mapping evidence. The apparent Annex A.115 API-definition line that mentions `application/swe+binary` remains documented as a source inconsistency and is not SWE Common Text PASS evidence.
- **Maps to**: PRD FR-ETS-41.

#### SCENARIO-ETS-PART2-011-SWETEXT-CONFORMANCE-DECLARED-001 (CRITICAL)
**GIVEN** the IUT exposes `/conformance`
**WHEN** the SWE Common Text Encoding tests run
**THEN** exact `http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/swecommon-text` declaration is required before `/req/swecommon-text` assertions can PASS
**AND** sibling declarations such as `/conf/json`, `/conf/swecommon-json`, `/conf/swecommon-binary`, or resource-class declarations alone cannot satisfy `/conf/swecommon-text`.

#### SCENARIO-ETS-PART2-011-SWE-TEXT-ENCODING-RULES-PREREQUISITE-001 (NORMAL)
**GIVEN** OGC 23-002 Clause 16.3 lists SWE Common 3.0 Text Encoding Rules as a prerequisite
**WHEN** the released Annex A.11 procedures execute
**THEN** `http://www.opengis.net/spec/SWE/3.0/conf/text-encoding-rules` must be visible before SWE Common Text resource endpoint access
**AND** missing prerequisite evidence produces setup-level SKIP behavior rather than PASS from scoped resource evidence.

#### SCENARIO-ETS-PART2-011-RELEASED-PROCEDURES-001 (CRITICAL)
**GIVEN** OGC 23-002 Annex A.11 defines eight SWE Common Text test targets
**WHEN** `Part2SweCommonTextTests` is inspected or executed
**THEN** the deployed class exposes exactly one TestNG method for each released target
**AND** declaration, prerequisite, and resource-condition checks remain setup or per-procedure gates rather than standalone released ATS procedures
**AND** `ops/ats-coverage-report.json` can promote the class only when all eight targets are reviewed exact.

#### SCENARIO-ETS-PART2-011-RESOURCE-CONDITION-GATES-001 (CRITICAL)
**GIVEN** Annex A.11 applies SWE Common Text representation tests to Observation and Command resources
**WHEN** the ETS evaluates Requirements 117-122
**THEN** Observation schema, Observation schema mapping, and Observation encoding assertions require `/conf/datastream`
**AND** Command schema, Command schema mapping, and Command encoding assertions require `/conf/controlstream`
**AND** missing condition classes produce prerequisite-incomplete SKIP behavior rather than PASS from `/conf/swecommon-text`, endpoint availability, sibling declarations, or media-format lists alone.

#### SCENARIO-ETS-PART2-011-MEDIATYPE-READ-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/swecommon-text`
**WHEN** the ETS requests supported Observation or Command endpoints with `Accept: application/swe+text`
**THEN** at least one supported endpoint must advertise and return HTTP 200 with `Content-Type: application/swe+text` before mediatype-read PASS
**AND** `application/json`, `application/swe+csv`, `application/vnd.ogc.swe+text`, `auto`, `text/html`, HTTP 400, HTTP 500, empty collections, or format-list-only evidence cannot PASS mediatype-read.

#### SCENARIO-ETS-PART2-011-SCHEMA-VALIDATION-READONLY-001 (CRITICAL)
**GIVEN** bundled schemas `observationSchemaSwe.json`, `commandSchemaSwe.json`, and shared SWE Common JSON component schemas exist under `src/main/resources/schemas/`
**WHEN** candidate Observation Schema or Command Schema resources are retrieved with `obsFormat=application/swe+text` or `cmdFormat=application/swe+text`
**THEN** the ETS validates the JSON schema metadata against the corresponding bundled schema
**AND** validates that the media-format member is `application/swe+text` and the `encoding` member is a `TextEncoding` object
**AND** no schema-validation PASS is reported when the endpoint is unavailable, returns a JSON-format or CSV-format schema instead of SWE Common Text schema metadata, or a schema fixture is missing.

#### SCENARIO-ETS-PART2-011-SCHEMA-MAPPING-TIME-001 (NORMAL)
**GIVEN** Requirements 118 and 121 defer mandatory field mapping to the SWE Common JSON mapping requirements
**WHEN** Observation Schema or Command Schema resources are retrieved
**THEN** Observation schema mapping PASS requires the same canonical `Time` component definition evidence required by `/req/swecommon-json/obsschema-mapping`
**AND** Command schema mapping PASS requires the same canonical IssueTime definition evidence required by `/req/swecommon-json/cmdschema-mapping`
**AND** mapping PASS must come from retrieved `recordSchema` evidence, not hardcoded examples, sibling JSON schema shape, or field labels alone.

#### SCENARIO-ETS-PART2-011-OBSERVATION-COMMAND-ENCODING-GUARDS-001 (NORMAL)
**GIVEN** Requirements 119 and 122 require Observation and Command resources to follow parent DataStream or ControlStream schemas using SWE Common Text encoding rules
**WHEN** parent schema evidence, candidate child resources, or a SWE Common Text encoding validator are absent
**THEN** the ETS SKIPs with a precise no-safe-evidence reason
**AND** it SHALL NOT PASS Observation or Command text encoding from collection shape, empty candidate sets, `application/json` fallback bodies, CSV media bodies, or hardcoded examples.

#### SCENARIO-ETS-PART2-011-MEDIATYPE-WRITE-ADVERTISEMENT-001 (NORMAL)
**GIVEN** Requirement 116 applies only when Create/Replace/Delete is implemented
**WHEN** the ETS checks SWE Common Text write-media-type support
**THEN** it uses API definition operation metadata to verify advertised `application/swe+text` support for CREATE or REPLACE operations on Observation or Command resource endpoints only
**AND** every advertised scoped POST or PUT operation matching those endpoint templates must include exact `application/swe+text` requestBody content
**AND** OPTIONS, unrelated POST/PUT paths, partial scoped write coverage, `application/swe+csv`, vendor media types, and subresource paths such as Command status alone are not mediatype-write PASS evidence.

#### SCENARIO-ETS-PART2-011-ANNEX-MEDIATYPE-HONESTY-001 (CRITICAL)
**GIVEN** OGC 23-002 Clause 16.3 states the SWE Common Text media type as `application/swe+text`
**WHEN** the ETS evaluates Annex A.11 mediatype-read evidence
**THEN** the ETS uses `application/swe+text` as the normative PASS media type
**AND** the apparent Annex A.115 API-definition line that mentions `application/swe+binary` is treated as a source inconsistency to document, not as SWE Common Text PASS evidence.

#### SCENARIO-ETS-PART2-011-UNAVAILABLE-ENDPOINT-HONESTY-001 (CRITICAL)
**GIVEN** the current public IUT may declare `/conf/swecommon-text` while individual resource endpoints are unhealthy or inconsistent
**WHEN** DataStream, Observation, Command, or ControlStream schema endpoints return HTTP 400, HTTP 500, empty candidate sets, `application/json` fallback schemas, wrong media members, or `application/swe+csv` format evidence
**THEN** the ETS records FAIL for reachable declared requirements that violate HTTP 200/schema/media expectations, or SKIP when no candidate/evidence exists
**AND** it never converts those outcomes into PASS from declaration, broad media-format lists, or existing sibling tests.

#### SCENARIO-ETS-PART2-011-SMOKE-NO-PUBLIC-MUTATION-001 (CRITICAL)
**GIVEN** TeamEngine smoke runs against the primary local OSH IUT or any configured non-dedicated IUT
**WHEN** the SWE Common Text tests execute
**THEN** request logs contain zero IUT-bound POST, PUT, PATCH, or DELETE requests
**AND** any write-media-type or encoding behavior requiring mutation SKIPs or relies on non-mutating API-definition evidence only.

#### REQ-ETS-PART2-012: Part 2 SWE Common Binary Encoding
- **Priority**: MUST (eventually); SHALL NOT be scoped into Sprint 1.
- **Status**: RELEASED_ATS_PARTIAL_UNREVIEWED
- **Historical increment**: (Sprint 31 Generator; public GeoRobotix E2E failed)
- **Description**: The ETS SHALL implement the first declaration-gated, read-only OGC 23-002 Clause 16.4 SWE Common Binary Encoding subset using official `/req/swecommon-binary` and `/conf/swecommon-binary` identifiers. Runtime checks SHALL gate on exact `/conf/swecommon-binary` declaration, keep the SWE Common 3.0 Binary Encoding Rules prerequisite visible, condition Observation assertions on declared `/conf/datastream`, condition Command assertions on declared `/conf/controlstream`, verify `application/swe+binary` read support only from advertised/retrieved Observation or Command evidence, validate SWE Common schema metadata against bundled `observationSchemaSwe.json` and `commandSchemaSwe.json` while requiring `BinaryEncoding`, and treat write-media-type support as API-definition/readiness evidence only unless a safe dedicated mutable IUT is explicitly enabled in a later sprint.
- **Rationale**: PRD SC-3 requires Part 2 coverage. OGC 23-002 Clause 16.4 and Annex A.12 define `/conf/swecommon-binary` with Requirements 123-130. Sprint 31 planning follows the SWE Common JSON/Text guardrails but swaps the media type and schema-encoding assertion to Binary Encoding. Current GeoRobotix declares `/conf/swecommon-binary`, `/conf/datastream`, `/conf/controlstream`, and `/conf/create-replace-delete`, but it does not expose SWE 3.0 `/conf/binary-encoding-rules`; DataStream/Observation binary reads return HTTP 500; reachable Command-side checks fail existing `/controlstreams` schema validation before SWE Common Binary Command Schema PASS evidence; and nested Commands return `application/json` with empty items. The ETS must therefore fail or skip honestly rather than passing from declaration, sibling SWE Common classes, API format lists, JSON fallback schemas, empty binary bodies, vendor preliminary media, or OPTIONS evidence alone.
- **Maps to**: PRD FR-ETS-42.

#### SCENARIO-ETS-PART2-012-SWEBINARY-CONFORMANCE-DECLARED-001 (CRITICAL)
**GIVEN** the IUT exposes `/conformance`
**WHEN** the SWE Common Binary Encoding tests run
**THEN** exact `http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/swecommon-binary` declaration is required before `/req/swecommon-binary` assertions can PASS
**AND** sibling declarations such as `/conf/json`, `/conf/swecommon-json`, `/conf/swecommon-text`, or resource-class declarations alone cannot satisfy `/conf/swecommon-binary`.

#### SCENARIO-ETS-PART2-012-SWE-BINARY-ENCODING-RULES-PREREQUISITE-001 (NORMAL)
**GIVEN** OGC 23-002 Clause 16.4 lists SWE Common 3.0 Binary Encoding Rules as a prerequisite
**WHEN** the ETS reports full `/conf/swecommon-binary` closure
**THEN** `http://www.opengis.net/spec/SWE/3.0/conf/binary-encoding-rules` must be visible or explicitly reported as prerequisite-incomplete
**AND** scoped read-only checks may still run when `/conf/swecommon-binary` and the relevant Part 2 resource class are declared.

#### SCENARIO-ETS-PART2-012-RESOURCE-CONDITION-GATES-001 (CRITICAL)
**GIVEN** Annex A.12 applies SWE Common Binary representation tests to Observation and Command resources
**WHEN** the ETS evaluates Requirements 125-130
**THEN** Observation schema, Observation schema mapping, and Observation encoding assertions require `/conf/datastream`
**AND** Command schema, Command schema mapping, and Command encoding assertions require `/conf/controlstream`
**AND** missing condition classes produce prerequisite-incomplete SKIP behavior rather than PASS from `/conf/swecommon-binary`, endpoint availability, sibling declarations, or media-format lists alone.

#### SCENARIO-ETS-PART2-012-MEDIATYPE-READ-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/swecommon-binary`
**WHEN** the ETS requests supported Observation or Command endpoints with `Accept: application/swe+binary`
**THEN** at least one supported endpoint must advertise and return HTTP 200 with `Content-Type: application/swe+binary` before mediatype-read PASS
**AND** `application/json`, `application/swe+json`, `application/swe+text`, `application/swe+csv`, `application/vnd.ogc.swe+binary`, `auto`, `text/html`, HTTP 400, HTTP 500, empty collections, empty binary bodies, or format-list-only evidence cannot PASS mediatype-read.

#### SCENARIO-ETS-PART2-012-SCHEMA-VALIDATION-READONLY-001 (CRITICAL)
**GIVEN** bundled schemas `observationSchemaSwe.json`, `commandSchemaSwe.json`, and shared SWE Common JSON component schemas exist under `src/main/resources/schemas/`
**WHEN** candidate Observation Schema or Command Schema resources are retrieved with `obsFormat=application/swe+binary` or `cmdFormat=application/swe+binary`
**THEN** the ETS validates the JSON schema metadata against the corresponding bundled schema
**AND** validates that the media-format member is `application/swe+binary` and the `encoding` member is a `BinaryEncoding` object
**AND** no schema-validation PASS is reported when the endpoint is unavailable, returns a JSON-format/Text-format/CSV-format schema instead of SWE Common Binary schema metadata, or a schema fixture is missing.

#### SCENARIO-ETS-PART2-012-SCHEMA-MAPPING-TIME-001 (NORMAL)
**GIVEN** Requirements 126 and 129 defer mandatory field mapping to the SWE Common JSON mapping requirements
**WHEN** Observation Schema or Command Schema resources are retrieved
**THEN** Observation schema mapping PASS requires the same canonical `Time` component definition evidence required by `/req/swecommon-json/obsschema-mapping`
**AND** Command schema mapping PASS requires the same canonical IssueTime definition evidence required by `/req/swecommon-json/cmdschema-mapping`
**AND** mapping PASS must come from retrieved `recordSchema` evidence, not hardcoded examples, sibling JSON schema shape, or field labels alone.

#### SCENARIO-ETS-PART2-012-OBSERVATION-COMMAND-ENCODING-GUARDS-001 (NORMAL)
**GIVEN** Requirements 127 and 130 require Observation and Command resources to follow parent DataStream or ControlStream schemas using SWE Common Binary encoding rules
**WHEN** parent schema evidence, candidate child resources, or a SWE Common Binary encoding validator are absent
**THEN** the ETS SKIPs with a precise no-safe-evidence reason
**AND** it SHALL NOT PASS Observation or Command binary encoding from collection shape, empty candidate sets, `application/json` fallback bodies, text or CSV media bodies, non-empty bytes alone, or hardcoded examples.

#### SCENARIO-ETS-PART2-012-MEDIATYPE-WRITE-ADVERTISEMENT-001 (NORMAL)
**GIVEN** Requirement 124 applies only when Create/Replace/Delete is implemented
**WHEN** the ETS checks SWE Common Binary write-media-type support in the first increment
**THEN** it uses API definition or explicit operation metadata to verify advertised `application/swe+binary` support for CREATE or REPLACE operations on Observation or Command resource endpoints only
**AND** advisory public GeoRobotix probes do not issue POST, PUT, PATCH, or DELETE
**AND** OPTIONS, unrelated POST/PUT paths, `application/vnd.ogc.swe+binary`, JSON/Text/CSV media types, and subresource paths such as Command status alone are readiness evidence, not mediatype-write PASS.

#### SCENARIO-ETS-PART2-012-SOURCE-TYPO-HONESTY-001 (CRITICAL)
**GIVEN** OGC 23-002 Clause 16.4 names SWE Common Binary Encoding and Binary Encoding Rules while retaining stale text that says "SWE Common Text encoding" for the binary media type and ATS A.127/A.130 says "Text encoding rules"
**WHEN** the ETS evaluates SWE Common Binary evidence
**THEN** the ETS treats those strings as source inconsistencies to document
**AND** it requires `application/swe+binary`, `BinaryEncoding`, and Binary Encoding Rules evidence rather than passing from `TextEncoding`, SWE Common Text validators, or the preliminary vendor media type.

#### SCENARIO-ETS-PART2-012-UNAVAILABLE-ENDPOINT-HONESTY-001 (CRITICAL)
**GIVEN** the current public IUT may declare `/conf/swecommon-binary` while individual resource endpoints are unhealthy or inconsistent
**WHEN** DataStream, Observation, Command, or ControlStream schema endpoints return HTTP 400, HTTP 500, empty candidate sets, `application/json` fallback schemas, wrong media members, or text/CSV/JSON format evidence
**THEN** the ETS records FAIL for reachable declared requirements that violate HTTP 200/schema/media expectations, or SKIP when no candidate/evidence exists
**AND** it never converts those outcomes into PASS from declaration, broad media-format lists, non-empty bytes, or existing sibling tests.

#### SCENARIO-ETS-PART2-012-SMOKE-NO-PUBLIC-MUTATION-001 (CRITICAL)
**GIVEN** TeamEngine smoke runs against the public GeoRobotix IUT
**WHEN** the SWE Common Binary tests execute
**THEN** request logs contain zero IUT-bound POST, PUT, PATCH, or DELETE requests
**AND** any write-media-type or encoding behavior requiring mutation SKIPs or relies on non-mutating API-definition evidence only.

#### REQ-ETS-PART2-013: Observation/Command Binding Cross-Class Closure
- **Priority**: MUST.
- **Status**: PARTIAL_IMPLEMENTED; REPRODUCIBLE_POPULATED_IUT_WORKFLOW_IMPLEMENTED (Sprints 32-38 implemented the internal Observation/Command binding closure, local OSH primary E2E target, supported tasking fixtures, parent schema `f=json` request shaping, and ETS candidate-selection hardening. Sprint 40's local OSH source patch is historical audit evidence only and is not an approved implementation path under CP-003/ADR-012. S-ETS-44-01 implements an isolated supported-interface populated-IUT workflow under CP-004. The workflow is verified; full populated-IUT binding conformance remains unclaimed because unmodified OSH fails 28 strict representation checks.)
- **Description**: The ETS SHALL verify the project cross-class dynamic-schema closure that Observation bodies derive from their parent DataStream schema and Command-side bodies derive from their parent ControlStream schema across supported encodings. This is an internal closure requirement derived from OGC 23-002 Part 2 resource/schema requirements and v1.0 GH#7; OGC 23-002 does not define a standalone `/conf/observation-binding` conformance class, so the ETS SHALL NOT advertise or require `/conf/observation-binding` unless a future OGC standard defines it.
- **Scope for first Generator increment**: implement a declaration-gated, local-OSH-backed closure suite that reuses existing Part 2 DataStream, ControlStream, JSON, and SWE Common schema evidence. Positive PASS requires a candidate parent resource, its schema subresource, a candidate child Observation or Command-side resource, and a concrete field/type mapping assertion. Empty collections, missing schema members, unsupported encodings, missing validators, or unavailable endpoints SKIP with precise reasons rather than PASS.
- **Local OSH seed prerequisite**: because authenticated local OSH probes declared relevant Part 2 classes while dynamic collections could be empty, the ETS MUST either create documented fixtures through supported OSH interfaces or keep positive binding checks SKIP with exact empty-IUT-state reasons. Fixtures must record target, credential handling without secret values, resource IDs, payload families, cleanup plan, and TeamEngine totals. Source or binary changes to OSH are prohibited. Historical Sprint 40 patched-IUT results remain audit-only and do not close this requirement.
- **Rationale**: PRD SC-3 requires Part 2 coverage and the historical v1.0 GH#7 risk was a schema coupling bug where Observation body generation could drift from the parent DataStream schema. Sprint 32 also follows the user instruction to abandon GeoRobotix's public instance as a development test target and use a self-provisioned local OSH as the primary E2E IUT.
- **Implementation progress**: Sprint 32 Generator implements `Part2ObservationCommandBindingTests`, helper regressions, and TestNG wiring for group `part2binding`; the first increment is read-only and SKIPs positive closure on empty local OSH collections. Sprints 33-38 add inline CommandStatus/CommandResult regressions, accepted supported-interface seed shapes, Sapient and SimUAV tasking fixtures, parent schema request shaping, stream metadata/format assertions, and populated parent-candidate selection. CP-003 retires Sprint 40's OSH patch path. CP-004/S-ETS-44-01 implements a reproducible ephemeral local OSH process, exact API-created fixtures, source/install/image provenance, owned-resource cleanup, XML-derived provisioning/conformance/gate verdicts, abort-path finalization, normalized primary-state comparison, and a clean-primary rerun. Final evidence is provisioning PASS, populated TestNG `211/91/28/92` FAIL, cleanup PASS, unchanged primary state, and clean-primary `211/69/0/142` PASS. Future positive closure must change the ETS or exercise an unmodified conforming IUT.
- **Maps to**: PRD FR-ETS-43, except retired non-standard FR-ETS-35 System History.

#### SCENARIO-ETS-PART2-013-LOCAL-OSH-PRIMARY-IUT-001 (CRITICAL)
**GIVEN** a user-directed ETS change in Sprint 32 or later
**WHEN** the change needs TeamEngine E2E verification
**THEN** the primary development target is the self-provisioned local OSH instance reachable to the TeamEngine container as `http://field-hub-osh-1:8081/sensorhub/api`
**AND** the sprint evidence records Docker network, credential handling without secret values, seed state, artifacts, exact totals, and whether mutation was enabled.

#### SCENARIO-ETS-PART2-013-GEOROBOTIX-NOT-DEFAULT-001 (CRITICAL)
**GIVEN** GeoRobotix's public instance has repeated external/public-IUT failures and the user has abandoned it as a development target
**WHEN** planning, Generator, or gate agents choose an E2E IUT
**THEN** they SHALL NOT use GeoRobotix as the default or required target
**AND** any GeoRobotix run is explicitly advisory interoperability evidence only.

#### SCENARIO-ETS-PART2-013-EPHEMERAL-POPULATED-IUT-001 (CRITICAL)
**GIVEN** an unmodified local OSH source checkout and installed distribution are available
**WHEN** the populated local E2E workflow starts
**THEN** it SHALL create a separate ephemeral OSH process with isolated state and a versioned configuration
**AND** it SHALL mount the external OSH installation read-only
**AND** it SHALL populate static and dynamic resources only through supported Connected Systems HTTP APIs
**AND** it SHALL record source commit, source cleanliness/ahead state, installation manifest, configuration hash, container identity, network, and fixture ids without recording credentials.

#### SCENARIO-ETS-PART2-013-POPULATED-PROVISIONING-VERDICT-001 (CRITICAL)
**GIVEN** the ephemeral local OSH process is reachable
**WHEN** fixture application completes
**THEN** provisioning readiness SHALL require the expected System, Procedure, Deployment, SamplingFeature, DataStream, Observation, and ControlStream resources plus readable parent schemas and associated Observation body evidence
**AND** provisioning readiness SHALL be reported separately from TeamEngine conformance
**AND** a provisioning PASS SHALL NOT convert any TestNG FAIL or SKIP into PASS.

#### SCENARIO-ETS-PART2-013-POPULATED-EVIDENCE-001 (CRITICAL)
**GIVEN** populated provisioning is ready
**WHEN** TeamEngine executes the Connected Systems suite against the ephemeral IUT
**THEN** the workflow SHALL archive a non-empty TestNG XML report, TeamEngine log, exact totals, failed test names and messages, request method counts, and startup status
**AND** the workflow SHALL exit non-zero if TeamEngine reports one or more failed tests
**AND** known unmodified-IUT representation defects SHALL remain visible rather than being hidden by fixture shaping or weakened validation.

#### SCENARIO-ETS-PART2-013-PRIMARY-STATE-ISOLATION-001 (CRITICAL)
**GIVEN** a populated local OSH run may create mutable test state
**WHEN** the populated attempt finishes or aborts
**THEN** the workflow SHALL remove its ephemeral container and state unless explicit diagnostic retention is selected
**AND** it SHALL verify that the existing primary OSH container and state mount are unchanged
**AND** it SHALL run the clean primary local OSH TeamEngine smoke and record its exact verdict.

#### SCENARIO-ETS-PART2-013-DYNAMIC-SEED-STATE-001 (CRITICAL)
**GIVEN** local OSH currently declares Part 2 dynamic-data conformance classes but has empty DataStream, Observation, and ControlStream collections
**WHEN** Generator implements positive Observation/Command binding checks
**THEN** it SHALL first create or verify documented local dynamic-data seed fixtures, or SKIP positive closure with exact empty-IUT-state reasons
**AND** it SHALL NOT count declarations or empty collections as binding PASS evidence.

#### SCENARIO-ETS-PART2-013-DYNAMIC-SEED-FIXTURES-001 (CRITICAL)
**GIVEN** the Sprint 33 planned local OSH seed manifest is present
**WHEN** Generator attempts populated-IUT Observation/Command binding closure
**THEN** it SHALL create or verify DataStream, Observation, ControlStream, Command, and optional CommandStatus/CommandResult evidence in dependency order
**AND** it SHALL record accepted payload family, resource ids, response statuses, parent schema retrieval, child body evidence, and cleanup results before any positive binding PASS.

#### SCENARIO-ETS-PART2-013-SEED-MUTATION-SAFETY-001 (CRITICAL)
**GIVEN** local OSH dynamic-data seeding would issue POST, PUT, PATCH, or DELETE
**WHEN** mutation tests are not explicitly enabled with policy `dedicated-mutable-iut`
**THEN** seed application SHALL SKIP before mutation
**AND** default planning and smoke runs SHALL record zero IUT-bound write requests.

#### SCENARIO-ETS-PART2-013-OBSERVATION-PARENT-SCHEMA-001 (CRITICAL)
**GIVEN** a candidate DataStream, its schema subresource, and at least one Observation associated with that DataStream
**WHEN** the ETS evaluates Observation binding
**THEN** it verifies the Observation result/parameters body against the parent DataStream schema member semantics available for the negotiated encoding
**AND** it fails mismatched field names, missing required result members, or incompatible primitive types.

#### SCENARIO-ETS-PART2-013-COMMAND-PARENT-SCHEMA-001 (CRITICAL)
**GIVEN** a candidate ControlStream, its schema subresource, and at least one Command-side resource associated with that ControlStream
**WHEN** the ETS evaluates Command binding
**THEN** it verifies Command parameters and any available CommandStatus or CommandResult inline data against the parent ControlStream schema member semantics available for the negotiated encoding
**AND** it fails mismatched field names, missing required members, or incompatible primitive types.

#### SCENARIO-ETS-PART2-013-INLINE-STATUS-RESULT-REGRESSIONS-001 (CRITICAL)
**GIVEN** CommandStatus or CommandResult inline data is present, missing, non-object, or mismatched
**WHEN** the binding helper tests execute
**THEN** they verify missing inline status/result members do not create false failures, non-object inline members SKIP instead of PASS, and concrete missing-field or primitive-type mismatches are reported.

#### SCENARIO-ETS-PART2-013-POSITIVE-LOCAL-OSH-CLOSURE-001 (CRITICAL)
**GIVEN** local OSH has documented dynamic-data seed evidence and cleanup state
**WHEN** TeamEngine runs the `part2binding` group against the local OSH IUT
**THEN** positive Observation and Command binding PASS results require live parent schema evidence and associated child body evidence
**AND** the sprint evidence records TeamEngine totals, request method counts, resource ids, cleanup results, and any residual state.

#### SCENARIO-ETS-PART2-013-TASKING-DRIVER-FIXTURE-001 (CRITICAL)
**GIVEN** local OSH accepts ControlStream creation but Command POSTs time out when no receiving tasking module exists
**WHEN** populated-IUT Command binding closure is attempted
**THEN** the local OSH fixture SHALL include a tasking-capable driver or equivalent module that creates/owns ControlStreams and accepts Commands through real protocol exchange
**AND** positive Command fixture evidence SHALL include the driver/module identity, ControlStream id, Command id, terminal CommandStatus evidence, protocol-side acknowledgement evidence, and cleanup or residual-state documentation
**AND** manually inserted ControlStreams without a receiving tasking module SHALL remain timeout diagnostics, not positive Command binding PASS evidence.

#### SCENARIO-ETS-PART2-013-SIMUAV-TASKING-FIXTURE-001 (CRITICAL)
**GIVEN** OpenSensorHub `osh-addons` SimUAV is configured as a local tasking fixture
**WHEN** the ETS probes the local OSH fixture for Observation/Command binding closure
**THEN** evidence SHALL record SimUAV driver build provenance, module configuration, system id, DataStream ids, ControlStream ids, Observation bodies, schema endpoint bodies, submitted Command body, terminal CommandStatus, inline CommandResult, TeamEngine populated-smoke totals, and cleanup state
**AND** body-valid schema endpoints whose HTTP media type is `auto` SHALL be documented as a local OSH server limitation rather than counted as inspectable JSON schema PASS evidence
**AND** stream collection resources that omit Annex A.9 required metadata such as `live`, `async`, `resultTime`, `issueTime`, or `executionTime` SHALL fail schema-valid fixture closure until the server representation is corrected or an explicitly scoped isolated fixture mode is specified.

#### SCENARIO-ETS-PART2-013-SIMUAV-PRESEEDED-POPULATED-IUT-001 (CRITICAL)
**GIVEN** a dedicated local OSH IUT has SimUAV configured but disabled by default for clean primary smoke
**WHEN** a sprint explicitly opts into the SimUAV populated fixture
**THEN** the preseed step SHALL start or otherwise activate SimUAV, wait for DataStream/Observation and ControlStream resources, submit a waypoint feasibility Command, and verify terminal CommandStatus plus inline or linked CommandResult evidence before TeamEngine smoke
**AND** the fixture evidence SHALL record credentials handling without secret values, module identity, system id, selected parent/child ids, parent schema `f=json` response status/content type, child collection parseability, command request/response bodies, request method counts, cleanup/autostart state, and residual state.

#### SCENARIO-ETS-PART2-013-POPULATED-CANDIDATE-SELECTION-001 (CRITICAL)
**GIVEN** a populated local OSH IUT exposes more than one DataStream or ControlStream
**WHEN** the ETS needs a parent resource for Observation or Command evidence
**THEN** it SHALL prefer a resource whose scoped child collection exposes at least one associated child item over a first-page resource with an empty or unparsable child collection
**AND** it SHALL keep schema validation strict: non-JSON schema media types, HTTP 400 SWE text schema responses, or unparsable child collection bodies SHALL remain failure or skip evidence according to the applicable requirement instead of being masked by fixture selection.

#### SCENARIO-ETS-PART2-013-SCHEMA-JSON-REQUEST-SHAPING-001 (CRITICAL)
**GIVEN** a binding check fetches a DataStream or ControlStream parent schema subresource
**WHEN** the ETS requests `/datastreams/{id}/schema` or `/controlstreams/{id}/schema`
**THEN** it SHALL explicitly request a JSON representation using `f=json`
**AND** it SHALL preserve the strict JSON-compatible media-type gate before counting parent schema evidence as inspectable binding evidence
**AND** it SHALL NOT treat body-valid `Content-Type: auto` schema responses as binding PASS evidence.

#### SCENARIO-ETS-PART2-013-OSH-SCHEMA-MEDIATYPE-001 (CRITICAL)
**GIVEN** local OSH ConSys serves DataStream or ControlStream schema subresources for JSON, SWE Common JSON, SWE Common Text, or SWE Common Binary data/command formats
**WHEN** a non-browser client requests `/datastreams/{id}/schema?obsFormat=...`, `/controlstreams/{id}/schema?commandFormat=...`, or `/controlstreams/{id}/schema?cmdFormat=...` without an explicit `f=json`
**THEN** OSH returns HTTP 200 with a JSON-compatible `Content-Type` for the schema representation rather than `auto`
**AND** the body remains the requested schema representation instead of being replaced with generic fallback content.

#### SCENARIO-ETS-PART2-013-OSH-SWE-TEXT-MEDIATYPE-001 (CRITICAL)
**GIVEN** OGC 23-002 Clause 16.3 requires exact SWE Common Text media type `application/swe+text`
**WHEN** local OSH ConSys advertises, parses, or generates schema evidence for SWE Common Text Observation or Command formats
**THEN** `application/swe+text` is treated as the exact SWE Text media type and returns schema evidence with `TextEncoding`
**AND** legacy `application/swe+csv` compatibility, if supported, SHALL NOT be advertised or counted by the ETS as exact SWE Text conformance evidence.

#### SCENARIO-ETS-PART2-013-OSH-COMMAND-SCHEMA-FORMAT-ALIAS-001 (CRITICAL)
**GIVEN** TeamEngine request paths use `cmdFormat` for ControlStream schema probes and OSH historically used `commandFormat`
**WHEN** local OSH ConSys receives `/controlstreams/{id}/schema?cmdFormat=application/swe+text`, `/controlstreams/{id}/schema?cmdFormat=application/swe+json`, or `/controlstreams/{id}/schema?cmdFormat=application/swe+binary`
**THEN** the request is interpreted equivalently to `commandFormat`
**AND** the response produces the requested Command schema/encoding evidence instead of silently defaulting to JSON.

#### SCENARIO-ETS-PART2-013-POPULATED-CHILD-BODY-JSON-001 (CRITICAL)
**GIVEN** a populated local OSH fixture has DataStreams, ControlStreams, and potentially empty or filtered child Observation/Command collections
**WHEN** a non-streaming client with JSON `Accept` reads `/datastreams/{id}/observations`, `/datastreams/{id}/observations/count`, `/controlstreams/{id}/commands`, `/controlstreams/{id}/commands/count`, `/observations`, or `/commands`
**THEN** HTTP 200 responses use parseable JSON collection/count bodies such as `{"items":[]}` or `{"count":0}`
**AND** `AUTO` response negotiation SHALL NOT select SWE Binary/Text payload framing for JSON collection/count requests without an explicit SWE response format.

#### SCENARIO-ETS-PART2-013-LOCAL-OSH-STREAM-METADATA-001 (CRITICAL)
**GIVEN** local OSH exposes DataStream or ControlStream resources used as populated fixture evidence
**WHEN** the Part 2 JSON or SWE Common schema suites validate stream collection and item representations
**THEN** DataStream JSON includes required `phenomenonTime`, `resultTime`, and `live` members, using JSON `null` for unavailable observation-derived ranges or unknown live state
**AND** ControlStream JSON includes required `issueTime`, `executionTime`, `live`, and `async` members, using JSON `null` for unavailable command-derived ranges or unknown live state and an explicit boolean for `async`
**AND** the ETS does not relax Annex A.9 required-field validation to make non-conforming stream metadata pass.

#### SCENARIO-ETS-PART2-013-FORMAT-ASSERTION-NOW-001 (CRITICAL)
**GIVEN** the bundled Connected Systems schemas use `timeInstantOrNow` as a `oneOf` between `format: date-time` and the literal `"now"`
**WHEN** the ETS validates local OSH stream `validTime` periods containing `"now"`
**THEN** JSON Schema validation asserts `format` keywords so `"now"` matches the literal branch only
**AND** malformed date-time strings remain invalid instead of being accepted as annotation-only format metadata.

#### SCENARIO-ETS-PART2-013-EMPTY-COLLECTION-BODIES-001 (CRITICAL)
**GIVEN** the local OSH ConSys API has no Observation or Command records after a clean reset, or has a parent DataStream/ControlStream whose child collection is empty
**WHEN** TeamEngine or a direct probe retrieves `/observations`, `/observations/count`, `/datastreams/{id}/observations`, `/controlstreams/{id}/commands`, or their count endpoints with a JSON response format
**THEN** each HTTP 200 response includes a parseable JSON body such as an empty `items` array or `{"count":0}`
**AND** HTTP 200 responses with `Content-Type: application/json` and zero body bytes fail clean-smoke and populated binding evidence until the local OSH response path is corrected.

#### SCENARIO-ETS-PART2-013-ENCODING-HONESTY-001 (NORMAL)
**GIVEN** JSON, SWE Common JSON, SWE Common Text, or SWE Common Binary evidence is unavailable or lacks a proven validator
**WHEN** the ETS evaluates cross-class binding for that encoding
**THEN** it SKIPs that encoding-specific binding check with a precise reason
**AND** it does not PASS from broad format lists, non-empty payloads, sibling encoding evidence, declaration alone, or hardcoded examples.

#### SCENARIO-ETS-PART2-013-MUTATION-SAFETY-001 (CRITICAL)
**GIVEN** local OSH is a dedicated mutable development IUT but may be used in read-only or mutation-enabled modes
**WHEN** planning or Generator seeds dynamic data or runs lifecycle checks
**THEN** mutation requires explicit dedicated-mutable-IUT opt-in and cleanup documentation
**AND** read-only planning smoke records zero IUT-bound POST, PUT, PATCH, or DELETE request lines.

### Cross-deliverable domain validator dependencies

#### REQ-ETS-VALIDATOR-001: External SWE Common and SensorML Validator Integration
- **Priority**: MUST before full SWE Common or SensorML validation closure.
- **Status**: IMPLEMENTED. The source-pinned SWE Common adapter and the provisional SensorML adapter are both active behind ETS-owned boundaries. Candidate `a593953d8d79d977649db3077696148e90ffb44a` passes clean Docker Maven `729/0/0/3`, exact-image runtime and security probes, schema parity, credential/sabotage/hygiene gates, and unmodified-local-OSH E2E. Final SensorML Raze approved at `0.99` confidence with no required actions. Future replacement of the provisional SensorML backend by a reproducible FCU/OGC module is an architecture evolution, not an open requirement for the current adapter closure.
- **Description**: The ETS SHALL prefer reusable OGC-owned SWE Common 3.0 and SensorML 3.0 validator modules over long-term homegrown domain-schema validation when those modules are available as reproducible artifacts or source-pinned prebuilds. External validators SHALL be consumed through a thin Connected Systems adapter layer. The adapter MAY delegate pure domain schema validation, but this ETS SHALL retain CS API endpoint discovery, candidate selection, `/conformance` and prerequisite gating, exact media-type evidence, Connected Systems-specific mapping assertions, TestNG pass/fail/skip policy, no-mutation safety, TeamEngine report integration, and TeamEngine 6 runtime packaging discipline.
- **Upstream state on 2026-07-22**: `opengeospatial/ets-swecommon30` PR 10 exposes `org.opengis.cite:swecommon30-validator:0.1-SNAPSHOT` on branch `issue-9-swecommon-validation-module` at commit `3ba75ceabe57cea85f4a8513c59e0f90e386ba96`, but the artifact is not published to Maven Central. No public SensorML validator module attributable to `FCU-GIS-Luke` was found; `opengeospatial/ets-sensorml30` is a public ETS scaffold at commit `d2b2a6308fdf48f113f7c7faed6712dc05e33130`, not a reusable validator dependency.
- **Replacement boundary**: The first SWE implementation SHALL retain local Connected Systems wrapper-schema validation, extract each validated `recordSchema`, and pass that pure SWE component to `swecommon30-validator` through the adapter. Local format, encoding, media-type, mapping, binding, and PASS/SKIP assertions remain authoritative during this dual-validation stage. Local SWE validation SHALL be removed only after the external validator alone proves parity and supports required format assertions plus JSON, Text, and Binary encoding schemas. Sprint 58 SHALL replace minimal SensorML shape checks with full validation through `ConnectedSystemsSensorMlValidatorAdapter`. Until a reusable public FCU/OGC module exists, its provisional backend SHALL use the already pinned bundled SensorML schema graph with Draft 2020-12 format assertions. A later upstream module may replace only that backend after parity and diagnostic review. The ETS SHALL NOT depend directly on another TeamEngine ETS jar such as `ets-swecommon30` or `ets-sensorml30` to obtain domain validation.
- **Rationale**: Reusing OGC-owned domain validators reduces duplicated schema logic and keeps SWE Common/SensorML semantics aligned across CITE suites, but importing another suite or unreviewed dependency closure could reintroduce TeamEngine classloader failures and false PASS behavior.
- **Maps to**: PRD FR-ETS-23, FR-ETS-40, FR-ETS-41, FR-ETS-42, FR-ETS-54, NFR-ETS-11.

#### SCENARIO-ETS-VALIDATOR-EXTERNAL-LIBRARY-BOUNDARY-001 (CRITICAL)
**GIVEN** a reusable SWE Common or SensorML validator module is added to the ETS
**WHEN** the adapter invokes the external validator
**THEN** the external module validates only domain schema semantics
**AND** the Connected Systems ETS retains endpoint discovery, candidate selection, conformance gating, exact media-type checks, TestNG skip/fail/report behavior, no-mutation safety, and TeamEngine packaging decisions.

#### SCENARIO-ETS-VALIDATOR-SWE-COMMON-ADAPTER-001 (CRITICAL)
**GIVEN** `org.opengis.cite:swecommon30-validator` is available as a reproducible artifact or source-pinned prebuild
**WHEN** the ETS first integrates SWE Common component validation
**THEN** a local adapter delegates extracted `recordSchema` objects to the upstream `sweCommon.json` schema
**AND** it converts upstream validation messages into ETS-owned diagnostics consumed by failures citing the active OGC 23-002 requirement URI
**AND** existing SWE JSON/Text/Binary tests preserve their current PASS/SKIP behavior until a later story explicitly changes it with tests and E2E evidence.

#### SCENARIO-ETS-VALIDATOR-SOURCE-PIN-001 (CRITICAL)
**GIVEN** `swecommon30-validator` is not published to an accepted Maven repository
**WHEN** the ETS build prepares the provisional dependency
**THEN** it fetches `opengeospatial/ets-swecommon30` at exactly `3ba75ceabe57cea85f4a8513c59e0f90e386ba96`, verifies the checkout SHA, and builds only the parent plus `swecommon30-validator`
**AND** the build fails closed on a different commit or unavailable source
**AND** it does not build, import, or package the upstream `ets-swecommon30` suite module
**AND** every supported Docker and developer Maven path bootstraps the pinned artifact before resolving the Connected Systems project
**AND** inert OGC Jenkins build definitions use the project Java 17 toolchain
**AND** release publication remains blocked until an accepted repository provides a non-SNAPSHOT reusable validator artifact.

#### SCENARIO-ETS-VALIDATOR-SWE-COMMON-DUAL-VALIDATION-001 (CRITICAL)
**GIVEN** a Connected Systems Observation or Command schema wrapper contains `recordSchema`, format metadata, and `encoding`
**WHEN** a Part 2 SWE Common JSON, Text, or Binary schema assertion runs
**THEN** the local Connected Systems schema validator first validates the complete wrapper
**AND** the adapter independently validates the extracted `recordSchema` against upstream `sweCommon.json`
**AND** local exact media-type, encoding, canonical Time/IssueTime mapping, binding, and PASS/SKIP behavior remains unchanged.

#### SCENARIO-ETS-VALIDATOR-SWE-COMMON-PARITY-CORPUS-001 (CRITICAL)
**GIVEN** complete Observation and Command schema wrappers for SWE Common JSON, Text, and Binary encodings
**WHEN** the provisional dual-validation implementation is verified
**THEN** all six wrappers pass the local Connected Systems wrapper schema and the extracted `recordSchema` passes the reusable validator
**AND** reusable-validator conformance failures cite the active OGC 23-002 requirement URI
**AND** reusable-validator resource or configuration failures propagate as suite errors rather than conformance failures.

#### SCENARIO-ETS-VALIDATOR-SWE-COMMON-UPSTREAM-LIMITS-001 (CRITICAL)
**GIVEN** the current upstream validator API does not enable Draft 2020-12 format assertions and the root of `encodings.json` omits `BinaryEncoding`
**WHEN** the first adapter implementation is released
**THEN** local wrapper, format, and encoding validation remains in place
**AND** no bundled SWE validation resource is removed until the external validator alone passes the agreed valid/invalid parity corpus and covers JSON, Text, and Binary encodings.

#### SCENARIO-ETS-VALIDATOR-DIAGNOSTICS-BOUNDARY-001 (CRITICAL)
**GIVEN** the external validator returns NetworkNT `ValidationMessage` values or encounters an internal resource/configuration error
**WHEN** the adapter handles the result
**THEN** conformance violations become deterministic ETS-owned string diagnostics
**AND** NetworkNT runtime types do not escape the adapter API
**AND** missing schemas or validator configuration failures remain suite errors rather than being misreported as IUT conformance failures.

#### SCENARIO-ETS-VALIDATOR-SENSORML-DISCOVERY-001 (CRITICAL)
**GIVEN** the user expects a SensorML validator library from FCU-GIS-Luke
**WHEN** no public reusable SensorML validator module, branch, artifact coordinate, or API is discoverable
**THEN** the ETS records the dependency as provisional and asks FCU/OGC for the exact source
**AND** it SHALL NOT import `opengeospatial/ets-sensorml30` directly as a dependency while that project remains a TeamEngine ETS scaffold rather than a reusable validator module.

#### SCENARIO-ETS-VALIDATOR-SENSORML-PROVISIONAL-ADAPTER-001 (CRITICAL)
**GIVEN** no reusable public FCU/OGC SensorML validator module is available
**WHEN** the released Connected Systems SensorML procedures require complete domain schema validation
**THEN** the ETS invokes an ETS-owned adapter over the pinned bundled SensorML schema graph
**AND** the adapter accepts a closed schema target and Jackson tree, returns immutable deterministic ETS-owned diagnostics, and exposes no TestNG or requirement-URI policy
**AND** schema resource or configuration failures remain operational errors
**AND** a future reusable validator can replace the provisional backend without changing TestNG procedures or Connected Systems mapping checks.

#### SCENARIO-ETS-VALIDATOR-HOMEGROWN-REPLACEMENT-001 (NORMAL)
**GIVEN** the ETS has local SWE Common schema helpers and minimal SensorML shape checks
**WHEN** an upstream reusable validator reaches the dependency boundary
**THEN** homegrown validation is replaced incrementally behind adapters with parity tests against valid and invalid fixtures
**AND** Connected Systems mapping assertions, relation-type checks, media-type write gates, and Observation/Command parent-child binding evidence remain local to this ETS.

#### SCENARIO-ETS-VALIDATOR-RUNTIME-CLOSURE-001 (CRITICAL)
**GIVEN** an external validator introduces NetworkNT, ITU, Jackson, SLF4J, Jakarta, TestNG, or TeamEngine-related transitive dependencies
**WHEN** the TeamEngine 6 Docker image is built
**THEN** dependency management, exclusions, or shading SHALL prevent duplicate class families and split-version classloader behavior
**AND** local adapter APIs SHALL convert external validator results into ETS-owned diagnostics instead of exposing NetworkNT `ValidationMessage` or other upstream runtime types beyond the adapter boundary
**AND** the runtime verifier SHALL prove the final image adds only justified ETS-owned artifacts without modifying TeamEngine-owned files.

#### SCENARIO-ETS-VALIDATOR-RUNTIME-EXECUTION-001 (CRITICAL)
**GIVEN** the shaded ETS jar is installed in the final TeamEngine 6 image
**WHEN** the runtime verifier exercises the validator adapter using the final image classpath
**THEN** a valid SWE Common component passes and an invalid component returns deterministic diagnostics
**AND** bundled `jar:` schema resolution and relocated NetworkNT execution complete without linkage errors.

#### SCENARIO-ETS-VALIDATOR-E2E-GATE-001 (CRITICAL)
**GIVEN** a future implementation story imports a SWE Common or SensorML validator dependency
**WHEN** Generator reports the implementation complete
**THEN** Docker Maven, the TeamEngine 6 runtime verifier, and the mandatory primary local OSH TeamEngine smoke from a clean `/tmp` clone SHALL be archived with exact totals and no-mutation evidence
**AND** advisory public IUT evidence SHALL NOT substitute for the local OSH gate.

### Sub-deliverable 5 — TeamEngine Integration

#### REQ-ETS-TEAMENGINE-001: SPI Registration
- **Priority**: MUST
- **Status**: IMPLEMENTED (TeamEngine 5.6.1 historical baseline and TeamEngine 6.0.0 SPI/CTL registration plus primary local OSH E2E verified)
- **Description**: The ETS SHALL expose a class implementing the TeamEngine TestNG SPI (e.g. `org.opengis.cite.ogcapiconnectedsystems10.TestNGController` extending `com.occamlab.te.spi.executors.testng.TestNGExecutor` per `ets-common` convention). The SPI registration SHALL be declared via `META-INF/services/com.occamlab.te.spi.jaxrs.TestSuiteController`. The verified baseline is TeamEngine 5.6.1; Sprint 41 SHALL prove discovery and execution on TeamEngine 6.0.0 before the migration is marked implemented.
- **Rationale**: Without SPI registration TeamEngine cannot enumerate the suite.
- **Maps to**: PRD FR-ETS-50.

#### REQ-ETS-TEAMENGINE-002: CTL Wrapper
- **Priority**: MUST
- **Status**: IMPLEMENTED (Sprint 41 policy-guidance remediation aligned CTL, TestNG defaults, smoke forwarding, docs, and sample props; structural verification and the primary local OSH TeamEngine 6 run pass)
- **Description**: A CTL wrapper at `src/main/scripts/ctl/ogcapi-connectedsystems10-suite.ctl` SHALL expose the suite to TeamEngine's CTL UI using the canonical TestNG run-argument contract: required `iut` (CS API landing-page URL), optional `auth-credential`, optional `mutation-tests-enabled`, optional `mutation-iut-policy`, optional `mobile-system-id`, and optional `subdeployment-association-evidence`. Human-facing form labels MAY describe `iut` as the CS API landing page, but the serialized argument key passed to Java/TestNG SHALL be `iut`. The suite SHALL NOT document or emit `iut-url`, `auth-type`, or `ics` as supported TestNG run arguments unless Java/TestNG support is intentionally added in a later requirement.
- **Rationale**: TeamEngine's primary user-facing entry surface is CTL; SPI alone is not enough for the UI. The CTL surface, TestNG defaults, Java run-argument enum, smoke harness, README, Javadoc, site docs, and sample run props must describe the same contract or TeamEngine UI runs can silently drift from automated smoke runs.
- **Maps to**: PRD FR-ETS-51.

#### REQ-ETS-TEAMENGINE-003: Dockerfile
- **Priority**: MUST
- **Status**: IMPLEMENTED - TeamEngine 6 Maven, immutable image, startup, registration, exact-image runtime verification, and primary local OSH E2E are archived.
- **Description**: A `Dockerfile` SHALL build the ETS on JDK 17 with Maven 3.9+ and install the thin ETS jar, required runtime dependency closure, and CTL resources into a runnable TeamEngine webapp. The runtime SHALL use an immutable digest of an OGC-published TeamEngine image compatible with the `ets-common:17` TeamEngine 6.0.0 SPI lineage, and SHALL run as non-root. Any dependency removed because TeamEngine supplies it SHALL be explicitly enumerated and justified by Maven dependency-tree and pinned-image library inventories. The image SHALL build from a clean checkout with no additional host dependencies. TeamEngine 5.6.1 remains the last verified baseline until Sprint 41 produces TeamEngine 6 evidence.
- **Rationale**: CP-001 and ADR-011 replace the manual cross-version TeamEngine 5.6.1 assembly with a reproducible OGC-maintained TeamEngine 6 runtime without converting an unverified diff into implementation evidence.
- **Maps to**: PRD FR-ETS-52, NFR-ETS-11.

#### REQ-ETS-TEAMENGINE-004: docker-compose
- **Priority**: SHOULD
- **Status**: IMPLEMENTED for TeamEngine 6 Compose health/suite metadata and the authoritative Docker smoke path.
- **Description**: A `docker-compose.yml` SHALL bring up the TeamEngine + ETS service at `http://localhost:8081/teamengine/` with port mapping, environment variable injection, and a healthcheck against `/teamengine/`.
- **Maps to**: PRD FR-ETS-53, NFR-ETS-11.

#### REQ-ETS-TEAMENGINE-005: Smoke Test
- **Priority**: MUST
- **Status**: IMPLEMENTED for TeamEngine 6; the smoke builds, registers, and executes the deployed suite against primary local OSH with exact totals and no-mutation enforcement.
- **Description**: A repository smoke-test script (`scripts/smoke-test.sh`) SHALL: (a) build the Docker image, (b) launch the container, (c) wait for healthcheck, (d) execute the suite against the configured IUT, (e) assert the TestNG report is non-empty and contains zero suite-registration errors, and (f) enforce the no-mutation oracle unless the run explicitly opts into a dedicated mutable IUT. Sprint 32 changes the primary development target from GeoRobotix to local OSH; GeoRobotix is advisory only.
- **Maps to**: PRD FR-ETS-54, SC-4.

#### REQ-ETS-TEAMENGINE-006: Local OSH Primary Development Target
- **Priority**: MUST.
- **Status**: IMPLEMENTED (Sprint 42 final gate restored a self-provisioned local OSH target and passed deployed TeamEngine E2E `211/69/0/142` with zero writes).
- **Description**: Development and sprint E2E gates SHALL use a self-provisioned local OpenSensorHub IUT as the primary target. The canonical Docker-network URL is `http://field-hub-osh-1:8081/sensorhub/api` on `field-hub_default`. Credentials SHALL be supplied through the environment and never recorded in repository docs, logs, or artifacts. GeoRobotix SHALL NOT be used as the default development target; it may be run only as an explicit advisory public interoperability probe.
- **Planning evidence**: On 2026-06-01, authenticated local OSH TeamEngine smoke passed `206 total / 65 passed / 0 failed / 141 skipped` with `recognized_iut_request_logs=132` and zero IUT-bound POST/PUT/PATCH/DELETE in read-only mode (`GET=130`, `OPTIONS=2`). Local OSH declared Part 2 `/conf/datastream`, `/conf/controlstream`, `/conf/json`, `/conf/swecommon-json`, `/conf/swecommon-text`, `/conf/swecommon-binary`, `/conf/create-replace-delete`, and `/conf/system-event`, but returned empty DataStream, Observation, and ControlStream collections. Dynamic-data positive closure therefore requires seed fixtures.
- **Maps to**: PRD FR-ETS-54, SC-4, NFR-ETS-11.

#### REQ-ETS-TEAMENGINE-007: TeamEngine Runtime Compatibility and Provenance
- **Priority**: MUST.
- **Status**: IMPLEMENTED. Runtime evidence emits both exact allowed collision tuples and rejects unused allowlist entries. The executable self-test asserts complete sorted multi-tuple output. Both Jenkinsfiles use Java 17, run the source-pin bootstrap, and request only project-declared profiles. Focused Maven `11/0/0/0`, full Maven `312/0/0/3`, exact-image runtime verification, and local OSH `211/69/0/142` pass for image `sha256:829a97414c07dd5763ed302e32b3178d301ca098bc9025f4b1f58b692ddad5f9`; final Raze approved with no required actions.
- **Description**: The selected TeamEngine runtime SHALL be pinned by immutable digest and SHALL have recorded TeamEngine, Tomcat, and JDK versions. The ETS build SHALL NOT modify, patch, replace, delete, or recursively change ownership of TeamEngine-owned files. It MAY only add ETS-owned jars, explicitly justified runtime dependencies, and CTL resources at TeamEngine-supported extension locations. An added runtime dependency SHALL NOT duplicate a base-image Maven coordinate family at another version or provide overlapping functional paths; verification SHALL compare coordinate families and jar contents rather than exact filenames alone. Dockerfile, `docker-compose.yml`, and `scripts/smoke-test.sh` SHALL be the authoritative TeamEngine 6 deployment path and SHALL agree on ports, health endpoint, runtime environment, run-argument forwarding, and artifact installation paths, or document and test any intentional differences. A Maven `docker` profile SHALL NOT define an independent TeamEngine runtime, alternate startup contract, or broad runtime dependency-copy path; if such a profile exists, it SHALL be a no-op or delegate to the same Dockerfile path. Verification SHALL establish required utilities and directories, ownership, effective non-root identity, SPI/CTL suite registration, absence of dependency linkage errors, direct final-image identity, and full TeamEngine execution against the primary local OSH IUT.
- **Rationale**: A matching POM label and plausible base image do not prove that the deployed runtime can load and execute the ETS.
- **Maps to**: PRD FR-ETS-50, FR-ETS-52, FR-ETS-53, FR-ETS-54, NFR-ETS-04, NFR-ETS-11.

#### REQ-ETS-TEAMENGINE-008: Public Package Metadata and Documentation Alignment
- **Priority**: MUST.
- **Status**: IMPLEMENTED (public package metadata/docs, structural tests, TeamEngine 6 runtime path, and primary local OSH E2E verified).
- **Description**: Public conformance-package surfaces SHALL describe the actual OGC API Connected Systems ETS and SHALL remain aligned with the canonical run-argument contract from REQ-ETS-TEAMENGINE-002 and the TeamEngine 6 deployment contract from REQ-ETS-TEAMENGINE-007. The governed surfaces include Maven-derived public metadata (`pom.xml` name/description and `ets.properties` title), Dockerfile labels, `docker-compose.yml` operator-facing comments, CTL title/description/defaults, `src/main/config/teamengine/config.xml`, `scripts/smoke-test.sh` suite-title assertion, `README.adoc`, `src/site/asciidoc/*.adoc`, Javadoc overview, `src/main/config/test-run-props.xml`, and `src/main/resources/test-run-props.xml`. They SHALL state the implemented scope as partial OGC 23-001 Part 1 coverage plus implemented partial OGC 23-002 Part 2 coverage, TeamEngine 6 as the forward runtime, local OSH as the primary development E2E target, and GeoRobotix as advisory only. Archetype placeholders such as XML/W3Schools samples, Class A/Class B, WCAG/XML/RFC boilerplate, or generic "describe scope" text SHALL NOT appear in these public package artifacts.
- **Rationale**: OGC CITE reviewers and TeamEngine users evaluate the shipped package through metadata and docs as well as Java behavior. Stale archetype placeholders can misrepresent suite scope even when runtime code is correct.
- **Maps to**: PRD FR-ETS-50, FR-ETS-51, FR-ETS-52, FR-ETS-53, FR-ETS-54, NFR-ETS-11, NFR-ETS-13.

### Sub-deliverable 5A — Project Scope Boundaries

#### REQ-ETS-SCOPE-001: External Runtime and IUT Immutability
- **Priority**: MUST.
- **Status**: IMPLEMENTED; S-ETS-43-01 Raze recheck approved at confidence 0.99.
- **Description**: This project SHALL NOT modify, patch, fork, publish, or replace OpenSensorHub or TeamEngine source code or binaries. It MAY configure an IUT and create test data through supported product interfaces, and it MAY install ETS-owned jars and CTL resources only at documented TeamEngine extension locations. E2E evidence SHALL identify the unmodified external source/image provenance used for the run.
- **Rationale**: OSH is the implementation under test and TeamEngine is the external execution platform; changing either to satisfy this ETS is outside the approved project scope and compromises independent conformance evidence.
- **Maps to**: ADR-011, ADR-012, REQ-ETS-TEAMENGINE-006, REQ-ETS-TEAMENGINE-007.

#### REQ-ETS-SCOPE-002: No Project-Operated Hosted CI
- **Priority**: MUST.
- **Status**: IMPLEMENTED; S-ETS-43-01 Raze recheck approved at confidence 0.99.
- **Description**: The project SHALL NOT plan, activate, or require GitHub Actions or another project-operated hosted CI service. Authoritative development verification SHALL run locally through the repository's Docker Maven wrapper, exact-image runtime verifier, and TeamEngine E2E procedure. Jenkinsfiles MAY remain only as inert OGC submission/build metadata and SHALL NOT be described as connected project CI.
- **Rationale**: The project will not receive CI approval; retaining activation work creates an unfulfillable gate and misstates scope.
- **Maps to**: ADR-012, REQ-ETS-SCAFFOLD-005, REQ-ETS-TEAMENGINE-005.

#### SCENARIO-ETS-SCOPE-EXTERNAL-SOURCE-IMMUTABILITY-001 (CRITICAL)
**GIVEN** an ETS failure is caused by OSH or TeamEngine behavior
**WHEN** the project plans or implements a response
**THEN** no OSH or TeamEngine source or binary is changed by this project
**AND** the ETS records the external limitation as FAIL, SKIP, known issue, or interoperability evidence.

#### SCENARIO-ETS-SCOPE-TEAMENGINE-ADDITIVE-INSTALL-001 (CRITICAL)
**GIVEN** the ETS is installed into the digest-pinned OGC TeamEngine image
**WHEN** the final image is compared with the base image
**THEN** only ETS-owned jars and CTL resources exist as additions at documented extension locations
**AND** no TeamEngine-owned file is modified, replaced, deleted, or re-owned.

#### SCENARIO-ETS-SCOPE-UNMODIFIED-IUT-PROVENANCE-001 (CRITICAL)
**GIVEN** local OSH is the primary E2E IUT
**WHEN** a gate run is reported
**THEN** the evidence records that the OSH source checkout has no project-authored commits ahead of upstream
**AND** the deployed runtime artifact identifies that unmodified checkout
**AND** supported configuration and fixture operations are distinguished from source or binary modification.

#### SCENARIO-ETS-SCOPE-HOSTED-CI-NONGOAL-001 (CRITICAL)
**GIVEN** the project has no approval for hosted CI
**WHEN** repository verification and planning surfaces are inspected
**THEN** no active or dormant GitHub Actions workflow or activation instruction exists
**AND** local Docker Maven, runtime, and TeamEngine E2E commands remain the documented gates
**AND** Jenkinsfiles are described only as inert OGC submission/build metadata.

### Sub-deliverable 6 — Spec-Trap Fixture Port

#### REQ-ETS-FIXTURES-001: Corpus Port
- **Priority**: MUST
- **Status**: SPECIFIED
- **Description**: The asymmetric `featureType`/`itemType` corpus from `csapi_compliance/tests/fixtures/spec-traps/` (~30-50 cases) SHALL be ported into Java classes implementing `org.testng.annotations.DataProvider`, with one `@DataProvider` method per logical fixture group (e.g. `asymmetricFeatureTypeFixtures`, `halfConformantCollections`, `missingOgc23001Markers`). Each fixture SHALL retain its original case ID and a comment containing the rationale from the TS source.
- **Rationale**: Spec-trap fixtures are unique authored IP, not in OGC ATS verbatim. Losing them in the port regresses test rigor.
- **Maps to**: PRD FR-ETS-60, SC-9.

#### REQ-ETS-FIXTURES-002: Fixture Coverage
- **Priority**: MUST
- **Status**: SPECIFIED
- **Description**: Each Part 1 conformance class with a corresponding spec-trap fixture group SHALL include at least one `@Test` method parameterized via the `@DataProvider`. The mapping (class → fixture group) SHALL match the v1.0 web-app's mapping documented in `csapi_compliance/src/engine/registry/index.ts`.
- **Maps to**: PRD FR-ETS-61.

#### REQ-ETS-FIXTURES-003: Port-Diff Audit
- **Priority**: SHOULD
- **Status**: SPECIFIED
- **Description**: A script `scripts/audit-fixture-port.sh` SHALL list case IDs in TS source vs Java source and flag any case present in TS but not in Java. The local verification gate runs this script; an unexplained drop fails the build.
- **Maps to**: PRD FR-ETS-62.

### Sub-deliverable 7 — CITE Submission

#### REQ-ETS-CITE-001: Maven Central Publish
- **Priority**: MUST (at beta milestone only)
- **Status**: SPECIFIED
- **Description**: At the beta milestone, the artifact `org.opengis.cite:ets-ogcapi-connectedsystems10:<version>` SHALL be published to OSSRH staging and promoted to Maven Central. GPG signing keys are recorded in `ops/server.md`. Pre-beta publishes SHALL be SNAPSHOT only and SHALL NOT promote to Maven Central.
- **Rationale**: OGC convention; CITE reviewers consume the artifact from Maven Central.
- **Maps to**: PRD FR-ETS-70, NFR-ETS-14.

#### REQ-ETS-CITE-002: Three-Implementation Outreach
- **Priority**: MUST
- **Status**: SPECIFIED
- **Description**: At the beta milestone, an outreach package SHALL be produced for OpenSensorHub and `SomethingCreativeStudios/connected-systems-go` requesting beta participation. The package contains: a Docker quickstart (running TeamEngine + ETS locally), a sample TestNG report from GeoRobotix, the OGC CITE governance reference (Policy 08-134r11), and contact info. Outreach status SHALL be tracked in `ops/status.md`.
- **Rationale**: CITE three-implementation rule; candidate pool exists per user gate 2026-04-27 but participation is not yet secured.
- **Maps to**: PRD FR-ETS-71, SC-6.

#### REQ-ETS-CITE-003: CITE SC Submission Ticket
- **Priority**: MUST
- **Status**: SPECIFIED
- **Description**: A CITE SubCommittee submission ticket SHALL be filed at `github.com/opengeospatial/cite/issues` referencing: the Maven Central artifact coordinates, the three-implementation roster with current pass status, the requested beta milestone, and a link to the ETS repository.
- **Maps to**: PRD FR-ETS-72, SC-7.

### Sub-deliverable 8 — Web-App Freeze

#### REQ-ETS-WEBAPP-FREEZE-001: README Reposition + Tag
- **Priority**: MUST
- **Status**: SPECIFIED
- **Description**: The `csapi_compliance` repository's README SHALL be repositioned to describe the v1.0 application as a "developer pre-flight tool, not certification-track," with a prominent link to `ets-ogcapi-connectedsystems10`. The HEAD commit `ab53658` SHALL be tagged `v1.0-frozen`. No further commits to v1.0 functionality (bug fixes excepted) are permitted.
- **Rationale**: User decision 2026-04-27. Prevents the web app from being mistaken for the certification deliverable.
- **Maps to**: PRD FR-ETS-80, R-PIVOT-10.

### Sub-deliverable 9 — Spec-Knowledge Sync

#### REQ-ETS-SYNC-001: TS↔Java URI Diff
- **Priority**: SHOULD
- **Status**: PARTIAL_IMPLEMENTED_REPORT_ONLY (Sprint 39 S-ETS-39-01, 2026-06-03). `scripts/uri-drift-audit.py` self-test passed and current audit is archived at `ops/test-results/sprint-ets-39-uri-schema-drift-audit-2026-06-03.json`: Java URI count `98`, web-app URI count `215`, unallowlisted URI drift detected (`missingInJava=162`, `missingInWebapp=45`), schema bundle parity verified (`126` vs `126`, no missing/extra/hash mismatch), and Java ETS/web-app repository commit plus dirty-count metadata recorded. CI-failing enforcement remains deferred until `ops/uri-coverage-allowlist.txt` is stabilized.
- **Description**: A diff script (`scripts/sync-uri-coverage.sh` or `scripts/uri-drift-audit.py`) SHALL extract every canonical OGC requirement URI from `csapi_compliance/src/engine/registry/*.ts` and from Java ETS source, and SHALL identify URI entries that exist on one side but not the other without an explicit allowlist entry in `ops/uri-coverage-allowlist.txt`. The same harness SHOULD compare the frozen v1.0 schema bundle against the ETS schema resources by relative path and hash. The local verification gate SHALL run this script when either the TS registry, Java ETS, or schema bundle changes once the allowlist is stabilized.
- **Rationale**: Prevents silent drift between the v1.0 web app and the ETS as OGC errata land. Both consume the same JSON Schemas; both should cover the same URI set.
- **Maps to**: PRD FR-ETS-90, R-PIVOT-11.

### Sub-deliverable 10 — Cleanup REQs (Sprint 2 + Sprint 3 carryover formalization)

> Sprint 2 introduced REQ-ETS-CLEANUP-001..004 to track cleanup work as first-class spec items.
> Sprint 3 extends with REQ-ETS-CLEANUP-005..008 for the Sprint 2 carryover items now closing.
> Sprint 4 extends with REQ-ETS-CLEANUP-009..012 for the Sprint 3 carryover items now closing (CI-workflow ESCALATION binary close, image-size v2 chown-layer attack, deeper E2E credential-leak smoke, sabotage-script hermetic execution fixes).

#### REQ-ETS-CLEANUP-005: Live Break-Core Dependency-Skip Verification
- **Priority**: MUST
- **Status**: IMPLEMENTED (pending Quinn+Raze) 2026-04-29 — Generator Run 1: TestNG XmlSuite parser unit test `VerifyTestNGSuiteDependency.java` (4 @Tests, all PASS in mvn test; 49 → 53 surefire) + bash sabotage script `scripts/sabotage-test.sh` (stub-server approach per ADR-010, authored + committed but live execution deferred to next gate run with proper Docker time budget per Sprint 3 mitigation plan). Defense-in-depth role split per ADR-010: structural lint + behavioral verification both shipped.
- **Description**: The dependency boundary SHALL be verified behaviorally, not
  only through annotations and XML structure. When a Core method is sabotaged
  to fail, all six released System methods SHALL report SKIP before System IUT
  access. The separate System-target sabotage mode SHALL inject the current
  `systemLocationsFollowRecommendation()` method and verify descendant
  cascading behavior. The restored suite SHALL retain its evidence-honest
  PASS/SKIP results rather than require every System method to PASS.
- **Maps to**: PRD FR-ETS-24, NFR-ETS-15.

#### REQ-ETS-CLEANUP-006: CredentialMaskingFilter Integration Test + REST-Assured RequestLoggingFilter Wrap
- **Priority**: MUST
- **Status**: SPECIFIED (Sprint 3 target via S-ETS-03-02)
- **Description**: (a) The suite SHALL accept `auth-credential` as a CTL parameter + TestNG suite parameter; the `scripts/smoke-test.sh` SHALL accept `--auth-credential <value>` (or env var `AUTH_CREDENTIAL`) and pass it through to the suite. (b) `MaskingRequestLoggingFilter` (subclass of REST-Assured's built-in `RequestLoggingFilter`) OR equivalent wrap pattern per Architect ratification SHALL intercept REST-Assured's request-log output and apply the existing `CredentialMaskingFilter.maskValue()` semantics BEFORE the log line is emitted. (c) An integration test (executed during smoke OR as a dedicated `scripts/credential-leak-test.sh`) SHALL set `auth-credential=Bearer ABCDEFGH12345678WXYZ`, run the suite, and grep-assert ZERO hits for the literal substring `EFGH12345678WXYZ` in BOTH TestNG report XML attachments AND container logs. The masked form (`Bear***WXYZ` or equivalent) MUST appear at least once (proves filter ran). The dedicated integration gate SHALL invoke the repository's Docker Maven wrapper rather than require host Maven, and SHALL accept the current non-zero targeted test count only when failures, errors, and skips are all zero instead of relying on a stale fixed count. Closes Sprint 2 PARTIAL `no_credential_leak_in_test_logs` + Raze cleanup CONCERN-2.
- **Maps to**: PRD FR-ETS-25 (FR-CAP-006/007 v1.0 carryover), NFR-ETS-08.

#### REQ-ETS-CLEANUP-007: CI Workflow Live at `.github/workflows/build.yml`
- **Priority**: MUST
- **Status**: RETIRED by CP-003 and REQ-ETS-SCOPE-002.
- **Description**: Historical requirement only. The staged GitHub Actions workflow SHALL NOT be activated and its dormant definition and activation instructions SHALL be removed.
- **Maps to**: ADR-012, REQ-ETS-SCOPE-002.

#### REQ-ETS-CLEANUP-008: Docker Image Size Optimization
- **Priority**: SHOULD
- **Status**: PARTIAL (Sprint 3 close: 660MB vs <550MB stretch — ADR-009 illustrative 200-300MB jar-dedupe projection EMPIRICALLY FALSIFIED at S-ETS-03-04; chown-layer 80MB attack identified for Sprint 4); EXTENDED via REQ-ETS-CLEANUP-010 (Sprint 4 v2)
- **Description**: The multi-stage Dockerfile runtime image SHALL be optimized to ≤ 550 MB (Sprint 3 stretch — more permissive than ADR-009 §"Image size target" 450MB soft target). Recommended approach (per Quinn cleanup GAP-1 Option A): TE common-libs ↔ deps-closure dedupe — exclude jars in `target/lib-runtime/` that overlap with `/usr/local/tomcat/lib` (from `teamengine-web-common-libs.zip`); estimated 200-300MB savings → ~363-463MB runtime image. Architect ratifies which approach (a / b / c per Sprint 3 contract `deferred_to_architect`). PARTIAL with rationale acceptable if Generator hits 550-700MB; carryover to Sprint 4 with explicit deferral if >700MB. Smoke 12+6+N PASS preserved post-optimization. **Sprint 3 outcome**: 660MB (3MB savings; only 4 jars / 1.8MB exact-basename overlap on actual TE 5.6.1 + ETS 0.1-SNAPSHOT post-ADR-006 layout). Sprint 4 attacks the dominant 80MB chown layer per REQ-ETS-CLEANUP-010.
- **Maps to**: NFR-ETS-11 (deployment topology), ADR-009.

#### REQ-ETS-CLEANUP-009: CI Workflow ESCALATION (5th-sprint-defer-risk; binary close)
- **Priority**: MUST
- **Status**: SUPERSEDED by CP-003 and REQ-ETS-SCOPE-002.
- **Description**: The historical escalation is closed permanently. Hosted CI is outside project scope; there is no future activation path or recurring user-action blocker.
- **Maps to**: ADR-012, REQ-ETS-SCOPE-002.

#### REQ-ETS-CLEANUP-010: Docker Image-Size v2 Chown-Layer Attack + ADR-009 v2 Amendment
- **Priority**: SHOULD
- **Status**: IMPLEMENTED (Sprint 4 Run 2, S-ETS-04-02; pending Quinn+Raze gate close — image size 663MB → 540MB, -123MB / -18.6%; <600MB target ACHIEVED; smoke 26/26 PASS; zero startup ERROR/SEVERE. Iteration: first build (539MB) had SEVERE on missing /conf chown; fixed by extending post-extract chown set to /conf, /logs, /work, /temp (+1MB negligible).)
- **Description**: The multi-stage Dockerfile SHALL be optimized via Docker buildkit `COPY --chown=tomcat:tomcat` syntax on each `COPY` directive — eliminating the 80MB `RUN chown -R tomcat:tomcat /usr/local/tomcat` layer that Sprint 3 empirical analysis identified as the dominant cost (sprint-ets-03-04-empirical-dedupe-list-2026-04-29.txt). Acceptance: image size <600MB (Sprint 4 PASS target — empirically permissive given multi-jar runtime classloader requirements; Sprint 3 661MB baseline). PARTIAL acceptable at 600-650MB; GAP if >650MB. Smoke 22+M PASS preserved (where M = Subsystems @Test count from S-ETS-04-05). ADR-009 SHALL be amended in-place (Pat hypothesis; Architect ratifies) recording: (a) empirical falsification of the illustrative 200-300MB jar-dedupe projection; (b) chown-layer attack approach + measured delta from Sprint 3 660MB baseline; (c) 80MB-as-dominant-cost identification; (d) Sprint 5+ next-target roadmap (alpine variant per ADR-009 §Alternatives if Sprint 4 chown-attack underperforms). Iterative tier-2 version-overlap dedupe (~7-8MB additional) permitted with smoke verification per excluded version (per ADR-009 §"DO NOT dedupe" runtime-classloader-binding caveat).
- **Maps to**: NFR-ETS-11, ADR-009 (amended).

#### REQ-ETS-CLEANUP-011: Deeper E2E Credential-Leak Smoke at IUT-Auth Layer
- **Priority**: MUST
- **Status**: IMPLEMENTED (Sprint 6 S-ETS-06-01 — finally closes the 2-sprint-old open criterion. Sprint 5 Run 1 wired `SMOKE_AUTH_CREDENTIAL` end-to-end (REQ-ETS-CLEANUP-013) but Sprint 5 gates surfaced GAP-1' (filter ordering defect — wire carried masked form because the Sprint 3 mutate/restore try/finally pattern restored AFTER `super.filter()` had already called `ctx.next()` for HTTP transport). Sprint 6 S-ETS-06-01 implements approach (i): `MaskingRequestLoggingFilter` no longer mutates `requestSpec`; emits masked log line directly to a shadowed `PrintStream` field; calls `ctx.next(requestSpec, responseSpec)` with the unmutated spec. New `VerifyWireRestoresOriginalCredential` (4 @Tests) using `CapturingFilterContext` proves wire-side correctness via BY-VALUE header snapshot at `ctx.next` time. 2 legacy try/finally tests deleted (verified non-existent code under approach (i)); 6 mask-format / isMasked / superset / null-guard tests reclassified as wiring-only with explicit caveat. Surefire 78 → 80 / 0 fails / 0 errors / 3 skipped. Live three-fold cross-check execution remains deferred to Quinn/Raze Sprint 6 gate per established pattern; structural wire-side proof is mvn-verified. Bundled scripts changes: smoke-test.sh container-log capture timing fix (capture before any die() teardown); credential-leak-e2e-test.sh prong-b grep expanded to include `$STUB_LOGFILE`. Sister repo HEAD `c17a534`.)
- **Description**: REQ-ETS-CLEANUP-006 (CredentialMaskingFilter integration test + RequestLoggingFilter wrap) closed at the unit-test integration layer at Sprint 3 (8/8 VerifyMaskingRequestLoggingFilter @Tests + grep mvn output + grep surefire XML for literal credential body, all zero hits) but explicitly deferred the deeper E2E architect-vision: synthetic auth-credential flowing through REST-Assured against an authenticated IUT at smoke time, with grep against ops/test-results/ XML AND container catalina.out for the literal substring (zero hits) AND for the masked form (>=1 hit, proving filter ran rather than dropping the field entirely). Sprint 4 wires `auth-credential` CTL/TestNG suite parameter end-to-end in `scripts/smoke-test.sh` (or new `scripts/credential-leak-e2e-test.sh`). Architect ratifies IUT path: (a) stub IUT in /tmp/ per Sprint 3 sabotage-script pattern (Pat recommends; composable with REQ-ETS-CLEANUP-012 sabotage-script bug fixes; hermetic); (b) pivot to authenticated IUT (lower hermeticity; depends on external IUT availability); (c) extended unit-layer fallback if both stub and alternative IUT prove infeasible. Acceptance: smoke against authenticated IUT (or stub) with `auth-credential=Bearer ABCDEFGH12345678WXYZ`; grep ops/test-results/ XML + container catalina.out returns ZERO hits for `EFGH12345678WXYZ`; grep both surfaces for masked `Bear***WXYZ` returns >=1 hit. Closes design.md §529 deferral text fully.
- **Maps to**: PRD FR-ETS-25, NFR-ETS-08. Closes Sprint 3 PARTIAL `credential_leak_integration_test_green` (deeper E2E) + Quinn cumulative CONCERN-1.

#### REQ-ETS-CLEANUP-012: Sabotage-Script Hermetic-Execution Bug Fixes
- **Priority**: SHOULD
- **Status**: IMPLEMENTED (Sprint 4 Generator Run 1 2026-04-29; pending Quinn+Raze live verification). Both bug fixes applied as separate atomic commits in `ets-ogcapi-connectedsystems10`: HEAD `4f65130` switches the Python ThreadingTCPServer in `scripts/sabotage-test.sh` from `("127.0.0.1", 0)` to `("0.0.0.0", 0)`; HEAD `d954ae9` adds `--add-host=host.docker.internal:host-gateway` to the `docker run` command in `scripts/smoke-test.sh`. STUB-IUT-PORT-LEAK risk verified mitigated by existing PID-based `cleanup_all` trap (kills via `$STUB_PIDFILE` content, not via port). `mvn test` BUILD SUCCESS surefire 61/0/0/3 (unchanged from Sprint 3 baseline). Live hermetic E2E execution deferred to Quinn/Raze gate per QUINN-RAZE-GATE-VERIFICATION-TIME-BUDGET mitigation.
- **Description**: Sprint 3 ADR-010 §"Defense-in-depth role split" landed the bash sabotage script (`scripts/sabotage-test.sh`) with two known bugs preventing hermetic CITE-SC-grade execution: (a) stub server binds to 127.0.0.1 (or default localhost) — should bind to 0.0.0.0 so a Docker container running smoke against `host.docker.internal:<port>` can reach the stub; (b) docker run command lacks `--add-host=host.docker.internal:host-gateway` — Docker on Linux WITHOUT Docker Desktop does NOT auto-resolve `host.docker.internal` (only Docker Desktop's macOS/Windows variants do). Both fixes are mechanical, ~5 LOC each, no architecture decision required. Acceptance: bash sabotage script runs hermetically end-to-end on Linux-without-Docker-Desktop hosts; netstat verification shows stub binding `0.0.0.0:<port>` not `127.0.0.1:<port>`; smoke container reaches stub via `host.docker.internal:<port>`. Live execution evidence archived for audit trail.
- **Maps to**: ADR-010 §"Defense-in-depth role split" (extended to hermetic CITE-SC-grade execution).

> Sprint 5 extends with REQ-ETS-CLEANUP-013..015 for the Sprint 4 carryover items now closing (SMOKE_AUTH_CREDENTIAL wiring wedge fix, SMOKE_OUTPUT_DIR worktree-pollution mitigation v2, sabotage --target flag).

#### REQ-ETS-CLEANUP-013: SMOKE_AUTH_CREDENTIAL End-to-End Wiring (GAP-1 wedge fix)
- **Priority**: MUST
- **Status**: IMPLEMENTED (Sprint 5 Run 1, S-ETS-05-01; pending Quinn+Raze gate close. Three-layer wiring landed: (1) bash — `scripts/smoke-test.sh` reads `SMOKE_AUTH_CREDENTIAL` and adds `--data-urlencode "auth-credential=$SMOKE_AUTH_CREDENTIAL"` to the curl POST when non-empty; (2) Java enums — new `TestRunArg.AUTH_CREDENTIAL` (key `auth-credential`) + new `SuiteAttribute.AUTH_CREDENTIAL` (`authCredential`/String); (3) Java listener — `SuiteFixtureListener.processSuiteParameters` reads the suite param and stashes on the ISuite; `SuiteFixtureListener.onStart` calls new `configureRestAssuredAuthCredential(String)` which sets `RestAssured.requestSpecification` to a `RequestSpecBuilder().addHeader("Authorization", credential).build()` so every subsequent REST-Assured request carries the header through the existing `MaskingRequestLoggingFilter` chain. New unit test `VerifyAuthCredentialPropagation` (8 tests, all PASS) covers TestRunArg key, SuiteAttribute, processSuiteParameters set/no-set/empty branches, and configureRestAssuredAuthCredential set/null/empty branches. Surefire 64 → 72 / 0 fails / 0 errors / 3 skipped. Live three-fold cross-check (smoke + scripts/credential-leak-e2e-test.sh against stub-IUT) still deferred to Quinn/Raze gate per Sprint 5 Run 1 mitigation pattern; structural wiring is mvn-verified.)
- **Implementation notes amended (Sprint 6 S-ETS-06-03 / META-GAP-1 reclassification)**: The 8 `VerifyAuthCredentialPropagation` unit tests verify STRUCTURAL WIRING ONLY — they exercise `TestRunArg` key, `SuiteAttribute`, `processSuiteParameters` branches, and `configureRestAssuredAuthCredential` branches in isolation, but they do NOT exercise wire-side filter ordering. Likewise the 6 retained `VerifyMaskingRequestLoggingFilter` tests (post Sprint 6 S-06-01 audit; 2 try/finally-semantic tests deleted) verify mask-format / `isMasked()` / header-set membership but use a `StubFilterContext` returning null from `ctx.next()` and CANNOT detect filter-ordering defects. **Wire-side credential integrity is proven only by `VerifyWireRestoresOriginalCredential` (REQ-ETS-CLEANUP-016, Sprint 6 S-ETS-06-01)** which uses a `CapturingFilterContext` snapshotting header values BY VALUE at `ctx.next` time. Future readers MUST NOT conflate the wiring-only PASS count with credential safety; the Sprint 5 GAP-1' bug demonstrated that 16 wiring tests can all PASS while the wire is poisoned.
- **Description**: `scripts/smoke-test.sh` SHALL read the `SMOKE_AUTH_CREDENTIAL` environment variable and propagate it as the `auth-credential` TestNG suite parameter via the curl POST `--data-urlencode` call to the TeamEngine `/suite/.../run` endpoint. The Java `SuiteFixtureListener` (or equivalent fixture) SHALL read this TestNG suite parameter and inject it into the REST-Assured `RequestSpec` as an `Authorization` header, flowing through the existing `MaskingRequestLoggingFilter` chain. Acceptance: `scripts/credential-leak-e2e-test.sh` with `SMOKE_AUTH_CREDENTIAL=Bearer ABCDEFGH12345678WXYZ` produces three-fold verdict: (a) ZERO unmasked-credential hits in TestNG XML + container log + smoke log; (b) AT LEAST ONE masked-form (`Bear***WXYZ` or equivalent) hit in log (proves filter ran); (c) AT LEAST ONE unmasked-credential hit in stub-IUT log (proves wire carried the credential). Closes S-ETS-04-03 PARTIAL → IMPLEMENTED.
- **Maps to**: PRD FR-ETS-25, NFR-ETS-08. Closes GAP-1 from Sprint 4 Quinn cumulative APPROVE_WITH_CONCERNS + Raze cumulative APPROVE_WITH_GAPS.

#### REQ-ETS-CLEANUP-014: SMOKE_OUTPUT_DIR Override in smoke-test.sh (Worktree-pollution mitigation v2)
- **Priority**: SHOULD
- **Status**: IMPLEMENTED (Sprint 5 Run 1, S-ETS-05-02; pending Quinn+Raze gate close. ~3 LOC bash: `scripts/smoke-test.sh` ARCHIVE_DIR now reads `${SMOKE_OUTPUT_DIR:-${REPO_ROOT}/ops/test-results}` so when the env var is set the TestNG XML + container log archives go there instead of the worktree. Default behaviour identical to Sprint 1-4 (backward compatible). Bash syntax validated (`bash -n`); grep confirms `SMOKE_OUTPUT_DIR`/`auth-credential`/`SMOKE_AUTH_CREDENTIAL` references all present.)
- **Description**: `scripts/smoke-test.sh` SHALL accept a `SMOKE_OUTPUT_DIR` environment variable. When set, ALL TestNG XML artifact writes SHALL use `${SMOKE_OUTPUT_DIR}/` as the base directory instead of `ops/test-results/`. When unset, behavior defaults to the existing `ops/test-results/` path (backward compatible). Gate briefings for Sprint 5+ SHALL mandate `SMOKE_OUTPUT_DIR=/tmp/<role>-fresh-sprint<N>/test-results/` in all gate smoke invocations to prevent worktree writes.
- **Maps to**: Worktree-pollution mitigation. Closes Sprint 2 systemfeatures gate incident pattern + Sprint 4 Quinn gate recurrence.

#### REQ-ETS-CLEANUP-015: sabotage-test.sh --target=\<class\> Flag
- **Priority**: SHOULD
- **Status**: FULLY-IMPLEMENTED (Sprint 6 S-ETS-06-02 closes the Docker build path; live cascade verification deferred to Sprint 6 gate). Sprint 5 Run 2 S-ETS-05-03 landed structural flag mechanics correctly (--help, --target=foo exit-code, sabotage marker injection, worktree-pollution guard all worked) but the rsync `--exclude='.git/'` stripped `.git` from the temp tree, breaking `Dockerfile COPY .git ./.git`. Sprint 6 S-ETS-06-02 (sister repo HEAD `c17a534`) drops the `.git` exclude (verified sister `.git` = 5.2MB; negligible cost) and updates the cp -a fallback for symmetry. Honest log message: smoke exit code captured; Docker build failure (no TestNG report produced) distinguished from smoke @Test failure (report present). bash -n PASS; --help and --target=foo paths preserved.
- **Description**: `scripts/sabotage-test.sh` SHALL accept a `--target=<class-name>` argument (e.g. `--target=systemfeatures`). When provided, the script SHALL patch the first `@Test` method of the target class in a temporary copy of the source tree (not the user's worktree), recompile, run smoke, archive the TestNG XML cascade evidence, and restore without modifying the original. Acceptance: `bash scripts/sabotage-test.sh --target=systemfeatures` runs end-to-end without manual Java edits; produced XML shows SystemFeatures FAIL + dependents SKIP; original SystemFeaturesTests.java is unmodified after the run.
- **Maps to**: ADR-010 §"Defense-in-depth role split" (behavioral verification at gate). Closes Raze Sprint 4 carryover recommendation.

> Sprint 6 is a WEDGE SPRINT extending with REQ-ETS-CLEANUP-016..017 for the 2 cross-corroborated HIGH gaps (masking filter wire-corruption + sabotage Docker build) + META-GAP-1 (wire-side unit test reclassification). NO new conformance classes in Sprint 6. Sampling + Properties deferred to Sprint 7+.

#### REQ-ETS-CLEANUP-016: MaskingRequestLoggingFilter Wire-Side Correctness (Sprint 6 — GAP-1' fix)
- **Priority**: MUST
- **Status**: IMPLEMENTED (Sprint 6 S-ETS-06-01 — Generator Run 1 close 2026-04-30; sister repo HEAD `c17a534`; live three-fold cross-check deferred to Quinn closure-proof exec at Sprint 6 gate. Approach (i) implemented per meta-Raze + primary-Raze consensus: (1) `MaskingRequestLoggingFilter` adds shadowed `private final PrintStream stream` field (REST-Assured 5.5.0 parent's `stream` is private final with no accessor — Plan-Raze verified via Maven Central source jar); (2) `filter()` builds masked log string from header snapshot using `CredentialMaskingFilter.maskValue` for sensitive header values, emits to shadowed PrintStream, calls `ctx.next(requestSpec, responseSpec)` directly with unmutated spec — wire carries ORIGINAL credential. `super.filter()` no longer called (parent's filter was 2 ops: log + ctx.next; we replace log with masked emission + retain ctx.next). New unit test `VerifyWireRestoresOriginalCredential` (4 @Tests) uses `CapturingFilterContext` that snapshots header values BY VALUE at `ctx.next` call time — critical: a by-reference capture would read post-restoration state and miss the bug, exactly what the legacy 16 wiring-only tests suffered. Mockito Response mock returned to satisfy non-null contract; Mockito present in pom.xml test scope (lines 152-153). 2 legacy try/finally-semantic tests in `VerifyMaskingRequestLoggingFilter` DELETED per Pat's S-06-03 finer-granularity disposition (verified non-existent code under approach (i)); 6 mask-format / isMasked / superset / null-guard tests RETAINED-AND-RECLASSIFIED with explicit "wiring-only — does NOT prove wire-side credential integrity" caveat in class javadoc. ThrowingFilterContext helper deleted (only the deleted #2 used it). Surefire 78 → 80 / 0 fails / 0 errors / 3 skipped. BUILD SUCCESS. TDD evidence: with the legacy filter, `wireCarriesOriginalAuthorizationCredential` FAILed `expected:<Bear[er ABCDEFGH12345678]WXYZ> but was:<Bear[***]WXYZ>`; under approach (i), all 4 wire-side @Tests PASS. Bundled smoke-test.sh + credential-leak-e2e-test.sh fixes also landed — see REQ-ETS-CLEANUP-011 status.)
- **Description**: `MaskingRequestLoggingFilter.filter()` SHALL NOT mutate the `requestSpec` headers before `ctx.next()` (the HTTP send). The filter SHALL: (1) snapshot sensitive header values; (2) build and emit a masked log line DIRECTLY to the configured `PrintStream` (bypassing `super.filter()` for log output); (3) call `ctx.next(requestSpec, responseSpec)` with the ORIGINAL unmutated `requestSpec`. A new unit test `VerifyWireRestoresOriginalCredential` using a `CapturingFilterContext` (NOT `StubFilterContext`) SHALL verify that the `requestSpec` passed to `ctx.next()` carries the ORIGINAL credential value. The 16 existing wiring-only unit tests (VerifyAuthCredentialPropagation 8 + VerifyMaskingRequestLoggingFilter 8) SHALL be reclassified in spec.md and Implementation Notes as "wiring-only — does NOT prove wire-side credential integrity". Acceptance: Quinn live-exec three-fold cross-check (a)+(b)+(c) all PASS; Raze adversarial wire-tap live-exec confirms wire carries unmasked credential; mvn test remains green. NOTE: existing 16 unit tests must CONTINUE to pass (no behavioral regression — the reclassification is documentation-only).
- **Wiring-only caveat for REQ-ETS-CLEANUP-013 (Sprint 5 wiring fix)**: The 8 VerifyAuthCredentialPropagation unit tests from REQ-ETS-CLEANUP-013 verify structural wiring (wiring-only — META-GAP-1 per sprint-ets-05-meta-review.yaml). Wire-side credential integrity is proven only by VerifyWireRestoresOriginalCredential (this REQ).
- **Maps to**: PRD FR-ETS-25, NFR-ETS-08. Closes GAP-1' from Sprint 5 Raze cumulative GAPS_FOUND 0.74 + Quinn cumulative APPROVE_WITH_CONCERNS 0.82. Closes the 2-sprint-old `credential_leak_e2e_full_pass` success criterion (open since Sprint 4 GAP-1 → Sprint 5 GAP-1').

#### REQ-ETS-CLEANUP-017: Sabotage Three-Class Cascade Live-Exec Verified (Sprint 6 — GAP-2 fix; Sprint 7 closure)
- **Priority**: SHOULD
- **Status**: IMPLEMENTED (Sprint 7 S-ETS-07-01 Wedge 1 close 2026-04-30; live 3-class cascade XML produced end-to-end at sister repo `ops/test-results/sprint-ets-07-01-wedge1-sabotage-cascade-2026-04-30.xml` (53KB) — Generator Run 1 cascade verdict: Core 8 PASS, Common 4 PASS, SystemFeatures 1 FAIL + 5 SKIP, Subsystems 4 SKIP, Procedures 4 SKIP, Deployments 4 SKIP; sabotage-test.sh step 5/6 verdict "PASS — two-level cascade verified end-to-end". The Wedge 1 fix changed the sabotage marker injection from bare `throw new AssertionError(...)` (which produced javac unreachable-statement at line 210 per JLS §14.21) to a two-line `if (true)\n\t\t\tthrow new AssertionError(...)` shape that defeats javac reachability analysis AND complies with spring-javaformat-maven-plugin:validate. The two-line shape was discovered necessary at /tmp clone live-exec time when an initial single-line `if (true) throw ...` PASSed javac but FAILed the Dockerfile builder stage 8/8 spring-javaformat:validate step; sister commit `94a4971` records the formatter-aware fix. Cascade XML retroactively validates ADR-010 v3 "forward-extends to Procedures + Deployments" claim at the live-exec layer (v3 amendment was empirical inference; Sprint 7 provides direct evidence). The 2-sprint-old `credential_leak_e2e_full_pass` success criterion was already CLOSED at Sprint 6 wire layer; this REQ closes the cascade-verification companion criterion.
- **Description**: The sabotage script `--target=systemfeatures` SHALL inject
  the current `systemLocationsFollowRecommendation()` method and run end to end
  from a temporary worktree. Core and Common SHALL PASS, at least one System
  method SHALL FAIL, other System methods SHALL retain their evidence-honest
  results, and every direct or transitive TestNG dependency descendant group
  SHALL SKIP. Descendants SHALL be derived from `testng.xml`, and report
  methods SHALL be associated through their `groups` attribute rather than
  package names. The log SHALL distinguish Docker build failure from the
  expected sabotaged-method failure.
- **Maps to**: ADR-010 §"Defense-in-depth role split". Closes GAP-2 from Sprint 5 Raze cumulative GAPS_FOUND 0.74 + Quinn cumulative APPROVE_WITH_CONCERNS 0.82 (cross-corroborated; reclassified from HIGH → MEDIUM per meta-Raze severity calibration). Closes Sprint 6 Raze HIGH GAP-1 + meta-Raze META-GAP-M2.

> Sprint 7 adds REQ-ETS-CLEANUP-018 (Sprint 6 carryover wedge bundle) and REQ-ETS-PART1-007..008 (Sampling Features + Property Definitions — twice-deferred from Sprints 5+6). Stories S-ETS-07-01..03 are Active Sprint 7.

#### REQ-ETS-CLEANUP-018: Sprint 6 Carryover Wedge Bundle (Sprint 7)
- **Priority**: MUST
- **Status**: IMPLEMENTED (Sprint 7 S-ETS-07-01 close 2026-04-30 — Generator Run 1; sister repo HEAD `c17a534 → 38b1f8a` after 5 commits: `a17c6ec` Wedges 1+3+4 initial, `94a4971` Wedge 1 spring-javaformat fix, `c68b803` Wedge 1 cascade XML evidence, `06acd1b` S-07-02+03 SamplingFeatures+PropertyDefinitions, `38b1f8a` Sprint 7 smoke 42/42 evidence; bd6fa9b Wedge 3 bash-x evidence). Wedge 1 (HIGH P0 — sabotage javac fix): two-line `if (true)\n\t\t\tthrow new AssertionError(...)` injection defeats javac reachability analysis (JLS §14.21) AND complies with spring-javaformat-maven-plugin:validate; verified live by 3-class cascade XML at sister `ops/test-results/sprint-ets-07-01-wedge1-sabotage-cascade-2026-04-30.xml`. Wedge 3 (MEDIUM P1 — credential-leak prong-b retarget): glob-safe targeting of `${SMOKE_OUTPUT_DIR}/s-ets-01-03-teamengine-container-*.log` archive (Sprint 6 timing fix output), fallback to `docker logs`; bash -x trace at sister `ops/test-results/sprint-ets-07-01-wedge3-cred-leak-prong-b-bash-x-trace.log` shows prong-b finds masked-form hit in correct archive. Wedge 4 (MEDIUM P1 — sabotage pipefail-unreachable fix): replaced `ls -t ... | head -1` pipeline with glob-safe `for _f in ... do [[ -e $_f ]] && ...` idiom; first attempt at /tmp clone live-exec exercised this path when initial Wedge 1 single-line shape failed spring-javaformat — disambiguation log line "Docker build FAILED (not a sabotage-marker hit)" fired correctly, confirming the fix. Wedge 5 (MEDIUM P1 — design.md doc-lag): added "Sprint 6 redesign: approach (i) — wire-side correctness via no-spec-mutation (S-ETS-06-01) — CANONICAL" subsection BEFORE the old wrap-pattern code, marked the entire historical block "Historical (Sprint 3 baseline — superseded by Sprint 6 approach (i) above)", explicitly invalidated the false try/finally claim per the new Sprint 7 generator_design_md_adr_self_audit success criterion. Wedge 6 (LOW — ADR-010 v3 retroval): natural fall-through; the cascade XML pointer is added to ADR-010 in this sprint. Wedge 2 (HIGH P0 — REQ-017 status honesty) was completed by Pat at planning time + promoted to IMPLEMENTED in this commit after Wedge 1 cascade XML production. mvn surefire 80 → 86/0/0/3 (added 6 lint tests for SF + Property). Smoke 34 → 42/42 against GeoRobotix.
- **Description**: Bundle fix for 6 Sprint 6 gate-identified defects: (1) `scripts/sabotage-test.sh` sabotage-marker injection javac unreachable-statement fix; (2) `scripts/sabotage-test.sh` pipefail-unreachable disambiguation block fix; (3) `scripts/credential-leak-e2e-test.sh` prong-b retarget; (4) `openspec/capabilities/ets-ogcapi-connectedsystems/design.md` §Sprint 3 hardening wrap-pattern doc-lag fix; (5) spec.md REQ-ETS-CLEANUP-017 status-honesty correction (Pat planning) + promotion to IMPLEMENTED (Generator post Wedge 1 close); (6) ADR-010 v3 Sprint 7 live-verification note. Acceptance criteria all met at Sprint 7 close: cascade XML produced, prong-b targeting verified via bash -x, disambiguation block fires under Docker build failure, design.md no longer contains false try/finally claim, REQ-017 status flipped to IMPLEMENTED with cascade XML evidence pointer, ADR-010 v3 retroval note added.
- **Maps to**: meta-Raze sprint-ets-06-meta-review.yaml META-GAP-M1, META-GAP-M2 (HIGH recalibrated). Closes Raze HIGH GAP-1, MEDIUM GAP-3 + Quinn MEDIUM GAP-Q1 from sprint-ets-06-adversarial-cumulative.yaml + sprint-ets-06-evaluator-cumulative.yaml. Implements Sprint 7 contract success criteria: `bash_x_trace_evidence_for_bash_changes`, `generator_design_md_adr_self_audit`, `spec_status_honesty_principle`.
- **Sprint 8 amendment (S-ETS-08-01 Wedge 2 — META-GAP-S7-1 closure)**: the live cascade evidence is no longer 3-class only. Raze's Sprint 7 gate-time sabotage exec from `/tmp/raze-fresh-sprint7/` produced a **5-class** cascade XML (archived per Raze cumulative gate evaluation evidence_artifacts) extending the Sprint 7 Generator's 3-class XML to all 5 SystemFeatures-level sibling classes (Subsystems + Procedures + Deployments + SamplingFeatures + PropertyDefinitions). Sprint 8 retires the prior phrasing ("live 3-class cascade XML produced end-to-end") in favour of "live cascade XML — 3-class at Generator run, 5-class at Raze gate"; the high-water-mark evidence is the Raze gate-time XML. ADR-010 v4 amendment (this sprint) records the Raze gate outcome. The dynamic sibling-enumeration fix landed in Sprint 8 S-ETS-08-01 Wedge 1 ensures the script's stdout VERDICT-summary now matches the actual cascade DAG width without further code edits as Sprint 8+ classes are added (e.g. Subdeployments).

#### REQ-ETS-PART1-007: Sampling Features Conformance Class (`/conf/sf`)
- **Priority**: MUST
- **Status**: IMPLEMENTED_RELEASED_ATS (Sprint 52 replaces the
  historical four-method approximation with all 5 released `/conf/sf` tests;
  implementation and required non-adversarial gates pass).
- **Description**: The ETS SHALL implement exactly the five released OGC
  23-001 Annex A `/conf/sf` procedures: `/canonical-url`,
  `/resources-endpoint`, `/canonical-endpoint`, `/collections`, and
  `/ref-from-system`. Each procedure SHALL have one independently executable
  TestNG method whose description cites its canonical target URI. The
  historical non-empty endpoint check, first-item shape check, path-only
  canonical check, and dependency tracer are superseded approximations and
  SHALL NOT receive exact mappings.
- **Endpoint validation**: Resources-endpoint and canonical-endpoint SHALL
  independently require HTTP 200, traverse bounded same-origin pagination,
  establish actual response media before parsing every page, and validate
  every supported `application/geo+json` page against the bundled released
  Sampling Feature collection schema. HTTP 404 and invalid supported content
  SHALL fail. Unsupported actual media SHALL warn and SKIP.
- **Collections**: The collections procedure SHALL inspect every advertised
  collection whose `featureType` is exactly `sosa:Sample`, require at least one,
  require `itemType=feature` and a non-empty string ID, retrieve each items
  endpoint through the reviewed API Common helper, and validate every supported
  GeoJSON page against the released Sampling Feature collection schema. If any
  selected collection lacks a supported representation, the procedure SHALL
  retain other observed defects, then warn and SKIP rather than pass.
- **Canonical URL**: The canonical-URL procedure SHALL inspect every item in
  every advertised `sosa:Sample` collection with a supported JSON items
  representation. Every item SHALL include a canonical relation resolving on
  the IUT origin to `{api_root}/samplingFeatures/{encodedId}`. A comparable
  occurrence SHALL return HTTP 200 with actual media matching the collection
  item and SHALL contain equal JSON content after canonical links are removed
  from both resources. Missing collections or any unsupported selected
  representation SHALL SKIP as an evidence limitation after all inspectable
  evidence is processed. Missing, unsafe, wrong-target, or content-different
  canonical links SHALL fail.
- **Reference from System**: The system-reference procedure SHALL retrieve
  every canonical System through the reviewed API Common helper. For every
  non-empty System ID it SHALL request
  `{api_root}/systems/{encodedSysId}/samplingFeatures`, require HTTP 200, and
  iterate all pages through bounded same-origin pagination. Every returned
  `application/geo+json` page SHALL pass the released Sampling Feature
  collection schema. Unsupported actual representation media SHALL be
  accumulated at the System boundary so later Systems remain inspectable, then
  warn and SKIP; missing IDs, invalid supported GeoJSON, pagination defects,
  and non-200 responses SHALL fail.
- **Evidence-limitation aggregation**: Expected unsupported-media and
  non-comparable-representation SKIPs SHALL be caught only at the narrow
  collection, item, or System boundary. Every independently inspectable later
  collection, item, and System SHALL still be processed before one aggregate
  SKIP. Assertion failures, unsafe pagination, non-200 responses, invalid
  metadata or schema, and canonical identity or content failures SHALL NOT be
  caught or downgraded.
- **Dependency and validator boundary**: Sampling Features SHALL inherit
  System directly. Setup SHALL inspect only Core, Common, Part 1 API Common,
  and System outcomes; unrelated sibling classes SHALL NOT block Sampling
  Features. The released GeoJSON schema SHALL remain behind an ETS-owned
  `SamplingFeaturesSupport` boundary. OGC 23-001 defines no SensorML Sampling
  Feature representation, so neither a SensorML suite jar nor a SensorML
  library enters this increment. No OSH or TeamEngine source or binary SHALL be
  modified.
- **Historical record**: Sprint 7's four-method approximation, GeoRobotix
  evidence, and historical scenarios remain archived below. They do not
  establish released ATS completion and are superseded by the Sprint 52
  requirement and scenarios.
- **Verification target**: All five procedures SHALL receive reviewed exact
  mappings. Focused and full Maven, pinned-source coverage reproduction,
  exact-image runtime, controlled read-only HTTP, unmodified-local-OSH
  TeamEngine, System dependency, credential, artifact-hygiene, and adversarial
  gates SHALL complete before this status is promoted.
- **Maps to**: PRD FR-ETS-17.

#### SCENARIO-ETS-PART1-007-RELEASED-CANONICAL-URL-001 (CRITICAL)
**GIVEN** one or more `sosa:Sample` collections expose supported JSON items
**WHEN** the released canonical-URL procedure executes
**THEN** every item SHALL expose a same-origin canonical Sampling Feature URL
**AND** dereferenced content SHALL equal the collection item after canonical
links are removed.

#### SCENARIO-ETS-PART1-007-RELEASED-RESOURCES-ENDPOINT-001 (CRITICAL)
**GIVEN** a Sampling Feature resources endpoint
**WHEN** the released parameterized endpoint procedure executes
**THEN** every page SHALL return HTTP 200
**AND** every supported GeoJSON page SHALL pass the released Sampling Feature
collection schema.

#### SCENARIO-ETS-PART1-007-RELEASED-CANONICAL-ENDPOINT-001 (CRITICAL)
**GIVEN** the IUT API root
**WHEN** the released canonical-endpoint procedure evaluates
`{api_root}/samplingFeatures`
**THEN** the endpoint SHALL independently satisfy the released resources
procedure.

#### SCENARIO-ETS-PART1-007-RELEASED-COLLECTIONS-001 (CRITICAL)
**GIVEN** the IUT advertises Feature collections
**WHEN** the released collections procedure executes
**THEN** at least one collection SHALL use `featureType=sosa:Sample`
**AND** every selected collection SHALL use `itemType=feature` and expose
schema-valid supported items.

#### SCENARIO-ETS-PART1-007-RELEASED-REF-FROM-SYSTEM-001 (CRITICAL)
**GIVEN** all canonical System resources
**WHEN** the released system-reference procedure executes
**THEN** every `{api_root}/systems/{sysId}/samplingFeatures` endpoint SHALL
return HTTP 200
**AND** every page SHALL be iterated through bounded same-origin pagination
**AND** every nested GeoJSON page SHALL pass the released Sampling Feature
collection schema.

#### SCENARIO-ETS-PART1-007-RELEASED-MEDIA-GATE-001 (CRITICAL)
**GIVEN** any endpoint page returns a representation unsupported by the testing
engine
**WHEN** a released Sampling Features procedure processes that page
**THEN** actual media SHALL be established before parsing
**AND** the procedure SHALL warn and SKIP rather than pass or parse unsupported
content.

#### SCENARIO-ETS-PART1-007-RELEASED-CANONICAL-EQUIVALENCE-001 (CRITICAL)
**GIVEN** a collection item has canonical relation occurrences
**WHEN** the canonical procedure resolves and dereferences a comparable
occurrence
**THEN** every occurrence SHALL identify the exact same-origin Sampling Feature
path
**AND** any post-normalization content difference SHALL fail.

#### SCENARIO-ETS-PART1-007-RELEASED-COLLECTION-COMPLETE-001 (CRITICAL)
**GIVEN** multiple `sosa:Sample` collections or multiple pages are advertised
**WHEN** canonical and collections procedures execute
**THEN** every selected collection and every page SHALL be processed
**AND** one valid first result SHALL NOT hide a later defect or unsupported
selected representation
**AND** an expected evidence SKIP SHALL NOT hide a later independently
inspectable collection, item, or System defect.

#### SCENARIO-ETS-PART1-007-RELEASED-SYSTEM-COMPLETE-001 (CRITICAL)
**GIVEN** multiple canonical Systems or paginated nested endpoints
**WHEN** the system-reference procedure executes
**THEN** every System and every nested page SHALL be requested
**AND** one valid System SHALL NOT hide a later non-200 or pagination defect.

#### SCENARIO-ETS-PART1-007-RELEASED-PROCEDURE-ISOLATION-001 (CRITICAL)
**GIVEN** one released Sampling Features procedure lacks evidence
**WHEN** another released procedure executes
**THEN** setup SHALL NOT have retrieved procedure-specific IUT responses
**AND** the other procedure SHALL reach its own evidence independently.

#### SCENARIO-ETS-PART1-007-RELEASED-DEPENDENCY-CASCADE-001 (CRITICAL)
**GIVEN** an inherited System prerequisite fails
**WHEN** Sampling Features setup begins
**THEN** setup and all five methods SHALL SKIP before Sampling Features IUT
access
**AND** unrelated sibling outcomes SHALL NOT block Sampling Features
**AND** method-specific, reason-shape no-evidence SKIPs for API Common datetime, optional
mobile-System input, and unsupported System endpoint media SHALL remain visible
but SHALL NOT block the independent direct procedures.

#### SCENARIO-ETS-PART1-007-RELEASED-E2E-EXECUTION-001 (CRITICAL)
**GIVEN** the exact Sprint 52 image and unmodified local OSH IUT
**WHEN** TeamEngine executes the suite
**THEN** all five released methods SHALL appear in TestNG results
**AND** genuine IUT failures and unsupported-representation SKIPs SHALL remain
visible.

#### SCENARIO-ETS-PART1-007-RELEASED-DIRECT-HTTP-COVERAGE-001 (CRITICAL)
**GIVEN** a controlled read-only HTTP fixture satisfies all released
preconditions
**WHEN** each Sampling Features method executes independently
**THEN** all five positive paths SHALL reach their normative endpoints
**AND** media, metadata, canonical, pagination, and completeness defects SHALL
fail or SKIP according to the released procedure.

#### REQ-ETS-PART1-008: Property Definitions Conformance Class (`/conf/property`)
- **Priority**: MUST
- **Status**: IMPLEMENTED_RELEASED_ATS (Sprint 53 replaces the historical
  four-method approximation with all four released `/conf/property`
  procedures. All gates are complete).
- **Description**: The ETS SHALL implement exactly the four released OGC
  23-001 Annex A `/conf/property` procedures: `/canonical-url`,
  `/resources-endpoint`, `/canonical-endpoint`, and `/collections`. Each
  procedure SHALL have one independently executable TestNG method whose
  description cites its canonical target URI. The historical endpoint
  availability check, first-item shape check, path-only dereference check, and
  dependency tracer are superseded approximations and SHALL NOT receive exact
  mappings.
- **Endpoint validation**: Resources-endpoint and canonical-endpoint SHALL
  independently require HTTP 200, traverse bounded same-origin pagination,
  establish actual response media before parsing every page, and validate
  every supported `application/sml+json` page against the bundled released
  SensorML Property collection schema. HTTP 404 and invalid supported content
  SHALL fail. Unsupported actual media SHALL warn and SKIP. The released Annex
  A source's undefined `{sensorml-mediatype}` token SHALL be interpreted as the
  defined `{sensorml-json-mediatype}` value, `application/sml+json`.
- **Collections**: The collections procedure SHALL inspect every advertised
  collection whose `itemType` is exactly `sosa:Property`, require at least one,
  require a non-empty string ID, retrieve each items endpoint through the
  reviewed API Common helper, and validate every supported SensorML page
  against the released Property collection schema. If any selected collection
  lacks a supported representation, the procedure SHALL retain other observed
  defects, then warn and SKIP rather than pass.
- **Canonical URL**: The canonical-URL procedure SHALL inspect every item in
  every advertised `sosa:Property` collection with a supported SensorML items
  representation. Every item SHALL include a canonical relation resolving on
  the IUT origin under `{api_root}/properties/{id}`, with exactly one non-empty
  local-ID path segment. A comparable occurrence SHALL return HTTP 200 and
  SHALL contain equal JSON content after canonical links are removed from both
  resources. Missing collections, empty item evidence, any unsupported
  selected representation, or no comparable canonical representation SHALL
  SKIP after all inspectable evidence is processed. Missing, unsafe,
  wrong-target, or content-different canonical links SHALL fail.
- **Evidence-limitation aggregation**: Expected unsupported-media and
  non-comparable-representation SKIPs SHALL be caught only at the narrow
  collection or item boundary. Every independently inspectable later
  collection and item SHALL still be processed before one aggregate SKIP.
  Assertion failures, unsafe pagination, non-200 responses, invalid metadata
  or schema, and canonical identity or content failures SHALL NOT be caught or
  downgraded.
- **Dependency and validator boundary**: Property Definitions SHALL inherit
  Part 1 API Common directly. Setup SHALL inspect only Core, Common, and Part 1
  API Common outcomes; System and unrelated sibling classes SHALL NOT block
  Property Definitions. The released SensorML Property schema SHALL remain
  behind an ETS-owned `PropertyDefinitionsSupport` boundary. That boundary
  SHALL remain replaceable by FCU-GIS-Luke's future reusable SensorML library
  without exposing external types to TestNG procedures. The executable
  `ets-sensorml30` suite jar SHALL NOT be imported as a library. No OSH or
  TeamEngine source or binary SHALL be modified. The resolver-normalized
  bundled `property.json`, `propertyArray.json`, and
  `propertyCollection.json` graph SHALL prove semantic and transitive-reference
  parity with the pinned released graph; expected `$id` additions and
  equivalent relative-to-absolute local `$ref` rewrites are not semantic
  drift. Reusing this validation SHALL NOT create an exact mapping or closure
  claim for the separate `/conf/sensorml/property-schema` procedure.
- **Historical record**: Sprint 7's four-method approximation and GeoRobotix
  evidence remain archived below. They do not establish released ATS
  completion and are superseded by the Sprint 53 requirement and scenarios.
- **Verification target**: All four procedures SHALL receive reviewed exact
  mappings. Focused and full Maven, pinned-source coverage reproduction,
  exact-image runtime, controlled read-only HTTP, unmodified-local-OSH
  TeamEngine, API Common dependency, credential, artifact-hygiene, and
  adversarial gates SHALL complete before this status is promoted.
- **Implementation evidence**: Exactly four independent deployed methods now
  hold reviewed exact mappings. Focused Maven is `95/0/0/0`; full Maven is
  `525/0/0/3` with three unchanged historical harness skips. Coverage is
  `240/39 exact/2 helper/130 candidate/69 unmapped`, including
  `/conf/property` at `4/4 exact`. Normalized parity passes for three Property
  entry schemas and 53 transitive schemas with zero semantic or graph
  mismatches. Exact-image runtime, dependency sabotage, credential, and
  zero-write/zero-leak hygiene gates pass. Primary unmodified local OSH
  TeamEngine is honestly `220/40/7/173`: all four Property methods appear as
  one missing-collection FAIL and three evidence SKIPs. This evidence does not
  close `/conf/sensorml/property-schema`. Raze returned
  `APPROVE_WITH_CONCERNS` at confidence `0.98`, with no required fixes.
- **Maps to**: PRD FR-ETS-18.

#### SCENARIO-ETS-PART1-008-RELEASED-CANONICAL-URL-001 (CRITICAL)
**GIVEN** one or more `sosa:Property` collections expose supported SensorML
items
**WHEN** the released canonical-URL procedure executes
**THEN** every item SHALL expose a same-origin canonical Property URL
**AND** dereferenced content SHALL equal the collection item after canonical
links are removed.

#### SCENARIO-ETS-PART1-008-RELEASED-RESOURCES-ENDPOINT-001 (CRITICAL)
**GIVEN** a parameterized Property resources endpoint
**WHEN** the released resources-endpoint procedure executes
**THEN** HTTP 200 SHALL be required
**AND** every supported SensorML page SHALL pass the released Property
collection schema.

#### SCENARIO-ETS-PART1-008-RELEASED-CANONICAL-ENDPOINT-001 (CRITICAL)
**GIVEN** the normalized API root
**WHEN** the released canonical-endpoint procedure executes
**THEN** it SHALL independently apply resources-endpoint validation at
`{api_root}/properties`.

#### SCENARIO-ETS-PART1-008-RELEASED-COLLECTIONS-001 (CRITICAL)
**GIVEN** the IUT collection metadata
**WHEN** the released collections procedure executes
**THEN** at least one exact `itemType=sosa:Property` collection with a
non-empty ID SHALL exist
**AND** every supported items page SHALL pass the released SensorML Property
collection schema.

#### SCENARIO-ETS-PART1-008-RELEASED-MEDIA-GATE-001 (CRITICAL)
**GIVEN** any Property endpoint or collection page
**WHEN** its response is received
**THEN** status and actual media SHALL be established before body parsing
**AND** unsupported actual media SHALL warn and SKIP without parsing
**AND** invalid supported content SHALL fail.

#### SCENARIO-ETS-PART1-008-RELEASED-SENSORML-SCHEMA-001 (CRITICAL)
**GIVEN** an `application/sml+json` Property collection page
**WHEN** the Property schema adapter validates it
**THEN** the complete document SHALL satisfy the bundled released
`propertyCollection.json` schema and its transitive SensorML/SWE references
**AND** the resolver-normalized bundled Property graph SHALL prove pinned-source
semantic and transitive-reference parity
**AND** this reuse SHALL NOT be reported as closure of
`/conf/sensorml/property-schema`.

#### SCENARIO-ETS-PART1-008-RELEASED-CANONICAL-EQUIVALENCE-001 (CRITICAL)
**GIVEN** a Property item with one or more canonical relations
**WHEN** canonical candidates are resolved and dereferenced
**THEN** unsafe or non-Property targets SHALL fail
**AND** a comparable SensorML representation SHALL equal the collection item
after canonical links are removed from both documents.

#### SCENARIO-ETS-PART1-008-RELEASED-COLLECTION-COMPLETE-001 (CRITICAL)
**GIVEN** multiple independently inspectable Property collections or items
**WHEN** an earlier candidate has an expected media or comparability limitation
**THEN** every later inspectable candidate SHALL still execute
**AND** any later assertion defect SHALL remain a failure
**AND** only an otherwise clean incomplete traversal SHALL end in aggregate
SKIP.

#### SCENARIO-ETS-PART1-008-RELEASED-PROCEDURE-ISOLATION-001 (CRITICAL)
**GIVEN** any one of the four released Property procedures
**WHEN** it executes
**THEN** it SHALL obtain its own evidence from immutable API-root state
**AND** SHALL NOT depend on another Property TestNG method.

#### SCENARIO-ETS-PART1-008-RELEASED-DEPENDENCY-CASCADE-001 (CRITICAL)
**GIVEN** released inheritance `/conf/property -> /conf/api-common`
**WHEN** API Common fails or skips unexpectedly
**THEN** all four Property procedures SHALL SKIP before Property IUT access
**AND** System or unrelated sibling outcomes SHALL NOT block them.

#### SCENARIO-ETS-PART1-008-RELEASED-VALIDATOR-BOUNDARY-001 (CRITICAL)
**GIVEN** current bundled SensorML Property schema validation
**WHEN** a future reusable SensorML library becomes available
**THEN** only the ETS-owned adapter implementation SHALL require replacement
**AND** TestNG procedures and diagnostics SHALL remain independent of external
validator types
**AND** the executable SensorML ETS suite jar SHALL not become a library
dependency.

#### SCENARIO-ETS-PART1-008-RELEASED-E2E-EXECUTION-001 (CRITICAL)
**GIVEN** the exact Sprint 53 image and unmodified local OSH IUT
**WHEN** TeamEngine executes the full suite
**THEN** all four Property methods SHALL appear in the report
**AND** unsupported media, missing collection evidence, failures, and SKIPs
SHALL remain honestly classified.

#### SCENARIO-ETS-PART1-008-RELEASED-DIRECT-HTTP-COVERAGE-001 (CRITICAL)
**GIVEN** controlled read-only HTTP fixtures for every released procedure
**WHEN** each Property method executes independently
**THEN** all four positive paths SHALL reach their normative endpoints
**AND** media, schema, metadata, canonical, pagination, completeness, and
dependency defects SHALL fail or SKIP according to the released procedure.

> Sprint 8 adds REQ-ETS-CLEANUP-019 (Sprint 7 carryover wedge bundle) and replaces the earlier REQ-ETS-PART1-005 placeholder with the single authoritative Subdeployments requirement below. Stories S-ETS-08-01..02 are Active Sprint 8.

#### REQ-ETS-CLEANUP-019: Sprint 7 Carryover Wedge Bundle (Sprint 8)
- **Priority**: MUST
- **Status**: IMPLEMENTED (Sprint 8 S-ETS-08-01 close 2026-04-30 — Generator Run 1; sister repo HEAD `38b1f8a → <Sprint 8 close>` after this commit). All 6 wedges landed: Wedge 1 (sabotage stdout dynamic 5-class enumeration) — `scripts/sabotage-test.sh` python parser block now extracts sibling buckets dynamically via `re.search(r"conformance\.([a-z][a-z0-9_]*)", sig)`; live-verified end-to-end at `/tmp/dana-fresh-sprint8/` clone (sister `ops/test-results/sprint-ets-08-cascade-2026-04-30.xml` — 6-class cascade including new Subdeployments transitive SKIP). Wedge 2 (spec.md REQ-018 + ADR-010 v4 amendment) — narratives now cite Raze gate-time 5-class XML; "Sprint 8+ will further verify" sentence retired in ADR-010 v4 amendment block. Wedge 3 (project-wide grep audit) — grep archive at INITIAL CLOSE COMMIT TIME at `ops/test-results/sprint-ets-08-01-self-audit-grep.txt` (csapi_compliance); 15 hits adjudicated; 1 stale hit (design.md line 666 item (e)) annotated INVALIDATED retiring deleted-test-scenario reference. Wedge 4 (ops/test-results.md ETS-pointer block) — header block prepended with sister repo GitHub URL. Wedge 5 (spring-javaformat 0.0.43 pin) — explicit pluginManagement entry in sister `pom.xml`; XML 1.0 §2.5 double-dash escape verified at first attempt (initial comment with literal CLI flag was rejected by Maven POM parser; fix preserved formatter version pinning rationale without literal flag). Wedge 6 (`scripts/mvn-test-via-docker.sh`) — wrapper script using `maven:3.9-eclipse-temurin-17` (Debian-based; Alpine variant lacked git breaking buildnumber-maven-plugin); host-side mvn handle for Quinn closes 7-sprint recurring limitation. mvn surefire 86 → 89/0/0/3 (added 3 lint tests for Subdeployments via VerifyTestNGSuiteDependency). Bash -x trace evidence archived for both modified bash artifacts (sister `ops/test-results/sprint-ets-08-01-wedge1-sabotage-bash-x-2026-04-30.log` + `sprint-ets-08-01-wedge6-mvn-via-docker-bash-x-2026-04-30.log`).
- **Description**: Bundle fix for 6 Sprint 7 gate-identified defects and process improvements:
  (1) `scripts/sabotage-test.sh` stdout VERDICT-summary tabulator fix — replace hard-coded 3-class sibling enumeration with dynamic lookup from cascade XML or testng.xml group declarations. Closes Raze GAP-1 (MEDIUM): "human-readable VERDICT-summary enumerates 3 siblings; actual sibling count is 5 post-Sprint 7."
  (2) spec.md REQ-ETS-CLEANUP-018 narrative updated to cite Raze gate-time 5-class XML evidence (not just Generator's 3-class XML); ADR-010 v4 amendment block retiring "Sprint 8+ will further verify the 5-class cascade" sentence (already verified at Sprint 7 Raze gate). Closes META-GAP-S7-1 (LOW-MED): "spec.md REQ-018 + ADR-010 lines 322-324 still cite 3-class as load-bearing when 5-class is already proven."
  (3) Project-wide grep across design.md + all ADR docs + spec.md for `super.filter\|try/finally pattern guarantees` with archived grep output as evidence artifact. Adjudicates design.md lines 666-667 (Raze Q12 judgment call). Closes META-GAP-S7-3 (MEDIUM): "Generator design.md self-audit was section-scoped, not project-wide."
  (4) `ops/test-results.md` (csapi_compliance) ETS-pointer block — prefix note pointing to sister repo `ops/test-results/`. Closes Raze REC-3 / GAP-3 (LOW): "ops/test-results.md stale 13 days — ETS evidence migrated to sister repo."
  (5) spring-javaformat version explicitly pinned in sister `pom.xml`. Closes Quinn W3 (LOW): defense-in-depth against future version drift that could invalidate two-line sabotage marker.
  (6) `scripts/mvn-test-via-docker.sh` wrapper in sister repo. Closes META-GAP-S7-2 / Quinn W1 (RECURRING-MEDIUM): "Quinn cannot run mvn lifecycle outside Docker across ALL 7 ETS sprints." Gives Quinn host-side independent mvn handle for Sprint 8+.
- **Maps to**: meta-Raze sprint-ets-07-meta-review.yaml META-GAP-S7-1, META-GAP-S7-2, META-GAP-S7-3 + sprint-ets-07-adversarial-cumulative.yaml GAP-1, GAP-3 + sprint-ets-07-evaluator-cumulative.yaml W1, W3.

#### REQ-ETS-PART1-005: Subdeployment Direct ATS Procedures (Sprint 51)
- **Priority**: MUST
- **Status**: IMPLEMENTED_RELEASED_ATS (Sprint 51 replaces the
  historical four-method approximation with all 5 released
  `/conf/subdeployment` tests; all four initial Raze oracle and
  dependency-evidence findings plus both follow-up hygiene findings are
  corrected and verified; final Raze reports no required fixes)
- **Description**: The ETS SHALL implement exactly the five released OGC
  23-001 Annex A `/conf/subdeployment` procedures: `/collection`,
  `/recursive-param`, `/recursive-search-deployments`,
  `/recursive-search-subdeployments`, and `/recursive-assoc`. Each procedure
  SHALL have one independently executable TestNG method whose description
  cites its canonical target URI. The historical non-empty collection,
  inherited canonical shape, canonical-link, and dependency-tracer methods are
  superseded approximations and SHALL NOT be reviewed as exact mappings.
- **Hierarchy evidence**: Each procedure that needs hierarchy evidence SHALL
  independently traverse `{api_root}/deployments` and every direct
  `{api_root}/deployments/{id}/subdeployments` endpoint with bounded,
  same-origin pagination. HTTP status and actual GeoJSON or SensorML media SHALL
  be established before parsing every page. Each page SHALL satisfy the
  released Deployment collection schema selected from actual media. Duplicate
  Deployment IDs, cycles, shortcut edges to non-direct descendants, or a
  safety-bound overflow SHALL fail closed.
- **Subcollection**: For every discovered parent with direct children, the
  collection procedure SHALL retrieve the canonical parent Deployment and
  require at least one `rel=subdeployments` occurrence. Every occurrence SHALL
  resolve on the IUT origin to the normalized HTTP target
  `{api_root}/deployments/{encodedParentId}/subdeployments`, without query or
  fragment variants. Normalization SHALL compare case-insensitive scheme and
  host, effective default port, normalized path segments, and percent-encoded
  unreserved path characters; wrong-parent and trailing-path variants SHALL
  still fail. The selected link SHALL return HTTP 200, and all returned pages
  SHALL pass the released Deployment collection schema selected from actual
  media. If no parent has direct children, the procedure SHALL warn and SKIP.
- **Recursive parameter**: The recursive-parameter procedure SHALL issue
  status-only requests carrying the exact values `recursive=false` and
  `recursive=true`. Its verdict SHALL depend on HTTP status, not on parsing or
  representation media.
- **Recursive search from Deployments**: The default and `recursive=false`
  top-level Deployment results SHALL equal the independently discovered root
  ID set. The `recursive=true` result SHALL equal every independently
  discovered Deployment ID. Missing or additional IDs SHALL fail.
- **Recursive search from Subdeployments**: For a parent with direct children,
  default and `recursive=false` results SHALL equal the parent's direct-child
  ID set. The `recursive=true` result SHALL equal all transitive descendant
  IDs. Missing or additional IDs SHALL fail. If no discovered parent has a
  transitive descendant, the procedure SHALL warn and SKIP.
- **Recursive association closure**: For every parent with direct children,
  the procedure SHALL inspect the relations `deployedSystems`,
  `featuresOfInterest`, `samplingFeatures`, `datastreams`, and
  `controlstreams`. For every advertised parent relation, an explicit
  read-only fixture SHALL independently identify the resource IDs owned
  directly by that parent and every descendant. The parent relation's resource
  IDs SHALL include the complete parent-plus-descendant fixture union. Missing
  ownership evidence SHALL warn and SKIP rather than permit a vacuous PASS.
  Association-link selection SHALL inspect every occurrence, refuse to
  dereference cross-origin candidates, prefer same-origin JSON-compatible
  media, and allow a same-origin occurrence without `type` to be negotiated and
  gated by actual response media. An unsupported, unsafe first occurrence SHALL
  NOT hide a usable later occurrence. If no safe comparable occurrence exists,
  the procedure SHALL warn and SKIP; any observed omission SHALL fail.
- **Dependency and validator boundary**: Subdeployment SHALL inherit
  Deployment directly. The defensive setup gate SHALL inspect only Core,
  Common, Part 1 API Common, and Deployment outcomes; unrelated sibling
  classes SHALL NOT block Subdeployment. Deployment collection schema dispatch
  SHALL reuse the ETS-owned `DeploymentFeaturesSupport` validator boundary.
  `ets-sensorml30` SHALL NOT be imported as a library, and no OSH or TeamEngine
  source or binary SHALL be modified.
- **Historical record**: Sprint 8's four-method approximation, advisory
  GeoRobotix evidence, and five historical scenarios remain archived below.
  They do not establish released ATS completion and are superseded by the
  Sprint 51 requirement and scenarios.
- **Verification target**: All five procedures SHALL receive reviewed exact
  mappings. Focused and full Maven, pinned-source coverage reproduction,
  exact-image runtime, controlled read-only HTTP, unmodified-local-OSH
  TeamEngine, a causal single-variable Deployment dependency experiment,
  credential, artifact-hygiene, and adversarial gates SHALL complete before
  this status is promoted. The causal experiment SHALL first run with a passing
  Deployment prerequisite and prove Subdeployment setup and methods are
  runnable, then inject exactly one Deployment failure and prove setup plus all
  five methods change to SKIP before Subdeployment IUT access with the injected
  method reported as blocker. Programmatic TestNG output SHALL be confined to a
  disposable temporary directory that is removed after the experiment; the
  causal test SHALL NOT create repository-root `test-output/` artifacts.
- **Implementation evidence**: All five procedures are independently deployed
  and their exact mappings are restored after correcting Raze findings
  `RAZE-S51-001` through `RAZE-S51-004`. Coverage is
  `240/30 exact/2 helper/136 candidate/72 unmapped`; `/conf/subdeployment` is
  `5/5 exact`. Corrected focused Maven is
  `131/0/0/0`; full Maven is `480/0/0/3`. Exact image
  `sha256:e88aa5f9...b1dca` passes runtime and immutable-base verification.
  Unmodified local OSH TeamEngine is honestly `219/39/5/175`; all five
  Subdeployment methods dependency-SKIP before IUT access while the five
  inherited Deployment/Procedure failures remain visible. The first direct
  Deployment sabotage archive is non-causal because Deployment already fails
  in the baseline; it is retained as historical evidence and SHALL NOT satisfy
  the corrected causal gate. A programmatic TestNG experiment instead proves a
  passing synthetic Deployment baseline reaches the IUT through all five
  methods, while changing only that prerequisite to fail makes setup and all
  five methods SKIP before IUT access with the injected blocker reported.
  Corrected controlled HTTP, deployed TeamEngine, credential,
  zero-write/zero-leak hygiene, ATS-source, and exact-image gates pass.
  Programmatic TestNG reports are confined to JUnit-managed temporary storage;
  focused and full Maven leave repository-root `test-output/` absent. Final
  Raze is `APPROVE_WITH_CONCERNS`, confidence `0.99`, with all six findings
  closed and no required fixes.
- **Maps to**: PRD FR-ETS-15.

#### SCENARIO-ETS-PART1-005-RELEASED-COLLECTION-001 (CRITICAL)
**GIVEN** one or more Deployments have direct Subdeployments
**WHEN** the released collection procedure executes
**THEN** every parent SHALL advertise the exact same-origin Subdeployment
collection
**AND** every returned page SHALL return HTTP 200 and satisfy the released
Deployment collection schema selected from actual media.

#### SCENARIO-ETS-PART1-005-RELEASED-RECURSIVE-PARAM-001 (CRITICAL)
**GIVEN** a Deployment endpoint supports the recursive parameter
**WHEN** the released parameter procedure sends `recursive=false` and
`recursive=true`
**THEN** both exact boolean values SHALL return HTTP 200
**AND** the status-only procedure SHALL NOT require response parsing.

#### SCENARIO-ETS-PART1-005-RELEASED-RECURSIVE-DEPLOYMENTS-001 (CRITICAL)
**GIVEN** an independently discovered Deployment hierarchy
**WHEN** the canonical Deployment endpoint is queried by default, with
`recursive=false`, and with `recursive=true`
**THEN** default and false IDs SHALL equal the root IDs
**AND** true IDs SHALL equal all discovered Deployment IDs.

#### SCENARIO-ETS-PART1-005-RELEASED-RECURSIVE-SUBDEPLOYMENTS-001 (CRITICAL)
**GIVEN** a parent has direct and transitive Subdeployments
**WHEN** its Subdeployment endpoint is queried by default, with
`recursive=false`, and with `recursive=true`
**THEN** default and false IDs SHALL equal its direct children
**AND** true IDs SHALL equal all descendants.

#### SCENARIO-ETS-PART1-005-RELEASED-RECURSIVE-ASSOC-001 (CRITICAL)
**GIVEN** a parent advertises one of the five recursive association relations
**AND** explicit fixture evidence identifies resources owned directly by the
parent and every descendant
**WHEN** the released recursive-association procedure executes
**THEN** the parent's associated-resource IDs SHALL include the complete
parent-plus-descendant fixture union
**AND** any omission SHALL fail.

#### SCENARIO-ETS-PART1-005-RELEASED-ASSOCIATION-ORACLE-001 (CRITICAL)
**GIVEN** a parent has Subdeployments and advertises a recursive association
**WHEN** independent parent-owned or descendant-owned fixture evidence is
absent
**THEN** the procedure SHALL warn and SKIP
**AND** descendant-only comparison SHALL NOT produce a PASS.

#### SCENARIO-ETS-PART1-005-RELEASED-MEDIA-GATE-001 (CRITICAL)
**GIVEN** any hierarchy or recursive-result page has unsupported or absent
actual media
**WHEN** a representation-specific procedure executes
**THEN** it SHALL warn and SKIP before parsing
**AND** every later pagination and child page SHALL apply the same gate.

#### SCENARIO-ETS-PART1-005-RELEASED-LINK-EXACT-001 (CRITICAL)
**GIVEN** a parent advertises one or more `rel=subdeployments` occurrences
**WHEN** link identity is validated
**THEN** every occurrence SHALL resolve to the normalized same-origin parent
Subdeployment target, treating explicit default ports and equivalent
unreserved path encodings as equal
**AND** cross-origin, wrong-parent, query, fragment, or trailing-path variants
SHALL fail.

#### SCENARIO-ETS-PART1-005-RELEASED-HIERARCHY-FAIL-CLOSED-001 (CRITICAL)
**GIVEN** independently discovered Deployment graph evidence
**WHEN** duplicate IDs, cycles, shortcut edges, or safety overflow are observed
**THEN** hierarchy construction SHALL fail before recursive set comparisons
can pass.

#### SCENARIO-ETS-PART1-005-RELEASED-ASSOCIATION-LINK-001 (CRITICAL)
**GIVEN** a parent Deployment advertises multiple occurrences of one
association relation
**WHEN** association evidence is retrieved
**THEN** selection SHALL prefer a same-origin JSON-compatible occurrence over
earlier unsupported or cross-origin occurrences
**AND** an untyped same-origin occurrence SHALL be negotiated and gated by
actual response media
**AND** relation values encoded as a string or list SHALL be recognized.

#### SCENARIO-ETS-PART1-005-RELEASED-PROCEDURE-ISOLATION-001 (CRITICAL)
**GIVEN** one Subdeployment procedure lacks hierarchy, media, or association
evidence
**WHEN** the other four methods execute
**THEN** no method dependency or eager shared retrieval SHALL suppress their
independent outcomes.

#### SCENARIO-ETS-PART1-005-RELEASED-DEPENDENCY-CASCADE-001 (CRITICAL)
**GIVEN** a Core, Common, Part 1 API Common, or Deployment prerequisite fails
**WHEN** Subdeployment setup starts
**THEN** all five direct methods SHALL SKIP before Subdeployment IUT access
**AND** unrelated sibling outcomes SHALL NOT become prerequisites.

#### SCENARIO-ETS-PART1-005-RELEASED-DEPENDENCY-CAUSAL-001 (CRITICAL)
**GIVEN** a controlled TestNG baseline where the Deployment prerequisite passes
and all five Subdeployment methods reach the IUT
**WHEN** exactly one Deployment prerequisite method is changed to fail
**THEN** Subdeployment setup and all five methods SHALL change to SKIP
**AND** the injected method SHALL be the reported blocker
**AND** the sabotage run SHALL issue zero Subdeployment IUT requests.

#### SCENARIO-ETS-PART1-005-RELEASED-DEPENDENCY-ARTIFACT-HYGIENE-001 (CRITICAL)
**GIVEN** the controlled programmatic TestNG baseline/sabotage pair
**WHEN** both synthetic suites execute
**THEN** TestNG reports SHALL be written only beneath a disposable temporary
directory
**AND** the temporary directory SHALL be removed after the test
**AND** repository-root `test-output/` SHALL remain absent.

#### SCENARIO-ETS-PART1-005-RELEASED-E2E-EXECUTION-001 (CRITICAL)
**GIVEN** unmodified local OSH exposes genuine Deployment failures and no
populated Subdeployment hierarchy
**WHEN** TeamEngine executes `/conf/subdeployment`
**THEN** all five methods SHALL retain honest inherited-SKIP outcomes
**AND** no OSH or TeamEngine change SHALL mask the prerequisite defects.

#### SCENARIO-ETS-PART1-005-RELEASED-DIRECT-HTTP-COVERAGE-001 (CRITICAL)
**GIVEN** a controlled read-only fixture with a supported Deployment hierarchy,
exact Subdeployment links, recursive results, and association endpoints
**WHEN** all five deployed procedures execute
**THEN** every successful path SHALL complete
**AND** media, hierarchy, link, recursive-set, and association defects SHALL
fail or SKIP as specified.

> Sprint 39 adds cleanup/sync tooling that can continue while the remaining populated local OSH blockers are outside the ETS safety envelope.

#### REQ-ETS-CLEANUP-020: Artifact Hygiene and Drift Harness (Sprint 39)
- **Priority**: SHOULD
- **Status**: IMPLEMENTED (Sprint 39 S-ETS-39-01, Raze approved 2026-06-03). `scripts/artifact-hygiene.py --self-test` and `scripts/uri-drift-audit.py --self-test` passed; Python compile passed; Docker Maven wrapper passed `294/0/0/3`; clean local OSH TeamEngine E2E passed `211/68/0/143` with zero IUT-bound writes. Archived hygiene reports cover Sprint 38 clean smoke, Sprint 38 populated mutable smoke, and Sprint 39 clean smoke. The Sprint 38 populated read-only gate intentionally fails because the mutable lifecycle artifact contains `POST=3`, `PUT=1`, `DELETE=3`; the report-only mutable summary passes and records zero credential leaks. Raze initially returned `APPROVE_WITH_CONCERNS` with no required fixes; a focused recheck returned `APPROVE` after post-review metadata fixes added explicit secret-input counts, archived clean paths, and Java/web-app repository provenance.
- **Description**: The ETS repository SHALL provide executable report tooling that summarizes TeamEngine TestNG artifacts, request-method counts, IUT-bound write evidence, and credential-scan evidence without weakening conformance assertions. The tooling SHALL also provide a report-first URI/schema drift audit for the frozen v1.0 web app and Java ETS. The artifact-hygiene tool SHALL parse TestNG XML totals, parse smoke container request logs, count IUT-bound HTTP methods by configured IUT prefix, detect IUT-bound POST/PUT/PATCH/DELETE when a read-only smoke is expected, and scan selected artifacts for unmasked Authorization headers or explicitly supplied secret values. The drift tool SHALL extract OGC Connected Systems requirement/conformance URIs from Java ETS source and the v1.0 TypeScript registry, apply an optional allowlist, and compare schema bundle relative paths plus hashes while ignoring non-schema ADS artifacts such as `:Zone.Identifier`.
- **Rationale**: Sprint 37/38 produced large and useful evidence sets while the remaining blockers are OSH behavior limitations. A small durable harness lets future work classify evidence, credential safety, request-method safety, and URI/schema drift quickly without spending a sprint on new conformance assertions.
- **Maps to**: PRD FR-ETS-25, FR-ETS-26, FR-ETS-90; REQ-ETS-SYNC-001.

#### REQ-ETS-CLEANUP-021: Confidential Reference and Build-Context Hygiene (Sprint 41)
- **Priority**: MUST
- **Status**: IMPLEMENTED (2026-07-21: tracked-file, filename-only history, and effective Docker-context checks pass without exposing protected contents; unrelated `f10m.xml` removed)
- **Description**: Confidential OGC-supplied reference material SHALL remain untracked and excluded from the Docker build context without printing its contents during verification. Ignore patterns SHALL be scoped to the documented reference-file locations or names and SHALL be accompanied by tracked-file, history, and build-context checks. Unrelated scratch inputs such as `f10m.xml` SHALL be removed from the worktree or documented as an intentional, non-confidential fixture before Sprint 41 completion.
- **Rationale**: Ignore rules reduce accidental inclusion but do not prove that protected material is absent from Git history or the effective Docker build context; unrelated scratch files also undermine auditable runtime analysis.
- **Maps to**: PRD FR-ETS-25, NFR-ETS-08, CP-001, S-ETS-41-01.

#### REQ-ETS-CLEANUP-022: Session Metrics JSONL Compatibility (Sprint 66 follow-up)
- **Priority**: SHOULD
- **Status**: IMPLEMENTED (2026-08-01: `scripts/session-metrics.py --self-test`
  passes and auto-discovery reads the current Codex rollout JSONL for this
  checkout; Raze approved and implementation/evidence pushed in `6e6d4f3`)
- **Description**: The session metrics extractor SHALL support both legacy
  Claude Code assistant-message JSONL records and current Codex rollout JSONL
  records for the active checkout. Auto-discovery SHALL first preserve the
  historical Claude project lookup, then find Codex logs under
  `~/.codex/sessions` and `~/.codex/archived_sessions` whose metadata `cwd`
  matches the current repository, preferring main-thread rollouts over
  sub-agent rollouts. Codex extraction SHALL read `token_count` records from
  `payload.info.last_token_usage`, split cached input and cache-write tokens
  out of ordinary input tokens, and avoid double-counting cumulative
  `total_token_usage` snapshots.
- **Rationale**: The project metrics process was written for Claude Code but
  the active environment is Codex. Without native Codex JSONL support,
  `ops/metrics.md` cannot record authoritative main-session token totals even
  though the data is available locally.
- **Maps to**: PRD FR-ETS-25, S-ETS-66-02.

> Sprint 11 selects AdvancedFiltering as the next Part 1 increment because it is read-only. The sprint is intentionally declaration-gated and partial: GeoRobotix currently does not declare `/conf/advanced-filtering`, so the default smoke expectation is SKIP-with-reason rather than false PASS. Planning probes show GeoRobotix accepts some query parameters, but undeclared behavior is not conformance evidence.

#### REQ-ETS-PART1-009: AdvancedFiltering Conformance Class (`/conf/advanced-filtering`) (Sprint 11 target)
- **Priority**: MUST
- **Status**: IMPLEMENTED_RELEASED_ATS; 25/25 EXACT; RAZE APPROVED
  (candidate `fce461288e99167bab6f391085493784da42cc58`; controlled
  `48/0/0/0`, focused `53/0/0/0`, full Maven `607/0/0/3`; Raze R8
  `APPROVE 0.99`)
- **Historical increment**: by Sprint 11 Generator and gates (2026-05-05; story S-ETS-11-01; Quinn Gate 3.5 APPROVE_WITH_CONCERNS 0.90; Raze Gate 4 APPROVE_WITH_CONCERNS 0.90). Implemented class `org.opengis.cite.ogcapiconnectedsystems10.conformance.advancedfiltering.AdvancedFilteringTests` with 6 read-only @Tests. Verification: Java formatter via Docker Maven BUILD SUCCESS; Docker Maven `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `98 tests / 0 failures / 0 errors / 3 skipped`; TeamEngine smoke from `/tmp/sprint-ets-11-generator-smoke` with external `SMOKE_OUTPUT_DIR=/tmp/sprint-ets-11-generator-smoke-results` reported `63 total / 48 passed / 0 failed / 15 skipped`. Independent Quinn/Raze gate smoke runs also reported `63 total / 48 passed / 0 failed / 15 skipped`. Current GeoRobotix does not declare `/conf/advanced-filtering`, so all 6 AdvancedFiltering @Tests SKIP with reason and no undeclared query behavior is counted as PASS.
- **OGC source verified**: Upstream `opengeospatial/ogcapi-connected-systems` commit `3fd86c73e744b7e2faaf7f1c17366bfb9ff4cd6f`. Requirement class file exists at `api/part1/standard/requirements/query/requirements_class_advanced_filtering.adoc`; explanatory clause exists at `api/part1/standard/sections/clause_15_requirements_class_advanced_filtering.adoc`. The OpenAPI fragment for `ID_List` exists at `api/part1/openapi/parameters/idListSchema.yaml`. The class identifier is `/req/advanced-filtering`, inherits `/req/api-common`, and lists query-parameter subrequirements for ID lists, common resource keyword/id filters, geometry filters, system/deployment/procedure/sampling-feature/property association filters, and combined filters.
- **Sprint 11 coverage scope**: AdvancedFiltering systems/common-resource read-only subset with 6 @Tests: (1) IUT declares `/conf/advanced-filtering`, otherwise every AdvancedFiltering @Test SKIPs with reason; (2) ID-list schema validator helper accepts homogeneous non-empty local-ID lists and homogeneous non-empty UID lists while rejecting mixed local/UID lists and empty/malformed lists; (3) `/systems?id=<known-id>` returns HTTP 200 and a non-empty result set whose returned items all preserve the selected id when the conformance class is declared and a seed System id was selected; (4) `/systems?q=<known keyword>` returns HTTP 200 and a non-empty result set whose returned items include keyword evidence in `name` or `description` when declared and a seed keyword was selected from a System name/description; (5) `/systems?geom=<WKT>` is exercised with a broad WKT geometry and validated only for HTTP 200 + JSON response shape in this sprint; (6) TestNG dependency wiring and smoke no-regression. The sprint deliberately does not close all 24 listed advanced-filtering subrequirements.
- **ID_List examples for Sprint 11 helper**: Based on upstream `idListSchema.yaml` and clause 15 text, valid examples include `0mqcvdnfoca0`, `0mqcvdnfoca0,0ngu9lvstls0`, `urn:osh:sensor:simweather:0123456879`, `urn:osh:sensor:simweather:0123456879,urn:osh:sensor:simweather:9876543210`, and the resource-by-id UID-prefix query value `urn:osh:sensor:simweather:*`. Invalid examples include an empty value, `,`, `0mqcvdnfoca0,urn:osh:sensor:simweather:0123456879`, and `urn:osh:sensor:bad value`. This is a local schema-helper test only; it does not prove every endpoint's query semantics.
- **Historical dependency wiring**: Sprint 11 depended on SystemFeatures via
  `<group name="advancedfiltering" depends-on="systemfeatures"/>`. Sprint 55
  supersedes that wiring with direct `part1apicommon` inheritance; System and
  sibling groups cannot block the released class.
- **Historical open subrequirements after Sprint 11**: the listed Part 1
  deployment/procedure/sampling-feature/property associations, recursive
  System associations, geometry intersection, combined filters, and
  endpoint-wide common filters are closed by Sprint 55. Part 2 query
  requirements remain separate under `REQ-ETS-PART2-006`.
- **IUT-state policy**: If the IUT does not declare `/conf/advanced-filtering`, every AdvancedFiltering @Test SKIPs with reason. Query parameters that appear to work on GeoRobotix without a declaration are planning evidence only and MUST NOT be reported as conformance PASS.
- **Maps to**: PRD FR-ETS-19.

### Acceptance Scenarios for Sprint 11

#### SCENARIO-ETS-PART1-009-ADVFILTER-CONFORMANCE-DECLARED-001 (CRITICAL)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**WHEN** the AdvancedFiltering suite reads `/conformance`
**THEN** the response contains `/conf/advanced-filtering`
**OR IF** `/conf/advanced-filtering` is absent
**THEN** every AdvancedFiltering @Test SKIPs with reason citing the missing conformance declaration.
*Maps to*: REQ-ETS-PART1-009.

#### SCENARIO-ETS-PART1-009-ADVFILTER-ID-LIST-SCHEMA-001 (CRITICAL)
**GIVEN** Sprint 11 validates the `ID_List` contract locally
**WHEN** `0mqcvdnfoca0`, `0mqcvdnfoca0,0ngu9lvstls0`, `urn:osh:sensor:simweather:0123456879`, `urn:osh:sensor:simweather:0123456879,urn:osh:sensor:simweather:9876543210`, `urn:osh:sensor:simweather:*`, empty values, mixed local/UID values, and malformed URI values are checked
**THEN** homogeneous local-ID and UID lists are accepted
**AND** the UID-prefix wildcard is accepted for resource-by-id query planning
**AND** empty, malformed, and mixed local/UID lists are rejected before a query is issued.
*Maps to*: REQ-ETS-PART1-009.

#### SCENARIO-ETS-PART1-009-ADVFILTER-SYSTEM-ID-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/advanced-filtering`
**WHEN** the suite selects a known System id from a non-empty `/systems` seed response and calls `/systems?id=<id>`
**THEN** the response is HTTP 200 JSON
**AND** the filtered response contains at least one item
**AND** every returned item preserves the selected id
**OR IF** no seed System id can be selected from `/systems`
**THEN** the test SKIPs with reason.
*Maps to*: REQ-ETS-PART1-009.

#### SCENARIO-ETS-PART1-009-ADVFILTER-SYSTEM-KEYWORD-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/advanced-filtering`
**WHEN** the suite selects a keyword from a known System `name` or `description` and calls `/systems?q=<known keyword>`
**THEN** the response is HTTP 200 JSON
**AND** the filtered response contains at least one item
**AND** every returned item includes keyword evidence in human-readable `name` or `description` fields
**OR IF** no seed keyword can be selected from `/systems`
**THEN** the test SKIPs with reason.
*Maps to*: REQ-ETS-PART1-009.

#### SCENARIO-ETS-PART1-009-ADVFILTER-SYSTEM-GEOM-SMOKE-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/advanced-filtering`
**WHEN** the suite calls `/systems?geom=<broad WKT polygon>`
**THEN** the response is HTTP 200 JSON with a valid CS API collection shape
**AND** this sprint records the result as geometry-filter smoke, not full spatial-intersection conformance.
*Maps to*: REQ-ETS-PART1-009.

#### SCENARIO-ETS-PART1-009-ADVFILTER-DEPENDENCY-SMOKE-001 (CRITICAL)
**GIVEN** the SystemFeatures group fails or is sabotaged
**WHEN** the AdvancedFiltering suite attempts to run
**THEN** AdvancedFiltering tests SKIP because `<group name="advancedfiltering" depends-on="systemfeatures"/>` is present
**AND** this dependency behavior is evidenced by structural lint and/or a targeted sabotage/runtime cascade check.
*Maps to*: REQ-ETS-PART1-009.

#### SCENARIO-ETS-PART1-009-ADVFILTER-SMOKE-NO-REGRESSION-001 (CRITICAL)
**GIVEN** Sprint 11 adds 6 AdvancedFiltering @Tests
**WHEN** `scripts/smoke-test.sh` runs from a `/tmp` clone against an explicitly selected advisory GeoRobotix target
**THEN** failed=0
**AND** total PASS+SKIP is at least 63 (Sprint 10 baseline 57 plus 6 AdvancedFiltering @Tests)
**AND** AdvancedFiltering results SKIP-with-reason if `/conf/advanced-filtering` remains absent.
*Maps to*: REQ-ETS-PART1-009.

> Sprint 55 supersedes the six-method Sprint 11 approximation with all 25
> released Annex A procedures. CP-015 records the exact boundary and the
> normative resolution of three obvious released prose defects.

#### Sprint 55 Direct ATS Requirement

The ETS SHALL deploy exactly 25 independent `advancedfiltering` TestNG methods,
one for each released `/conf/advanced-filtering` identifier. Class setup SHALL
retain only the immutable normalized API root after direct Part 1 API Common
prerequisites. Every method SHALL use `alwaysRun`, acquire its own declaration,
seed, query, traversal, and predicate evidence, and issue only GET requests.

Mandatory filter procedures SHALL derive known-matching values from the IUT,
validate every returned item, and fail when a known match yields an empty
filtered set. Missing mandatory seed evidence SHALL produce a precise aggregate
SKIP only after all independently inspectable endpoints or resources have been
processed. Recommendation procedures SHALL remain visible as warnings when
unsupported and SHALL not convert recommendation non-support into a mandatory
conformance failure.

All filtered pages SHALL be status and actual-media gated before parsing,
bounded, cycle-safe, and same-origin. Returned System, Deployment, Procedure,
Sampling Feature, and Property association-filter collections SHALL be
validated through their released representation boundaries. Cross-origin
association targets SHALL never receive IUT credentials.

##### SCENARIO-ETS-PART1-009-RELEASED-ID-LIST-001 (CRITICAL)
**GIVEN** the released `ID_List` procedure
**WHEN** the ETS constructs local-ID, UID, and UID-prefix lists
**THEN** it accepts only non-empty homogeneous comma-separated string values
**AND** rejects malformed, empty, or mixed identifier-type lists.

##### SCENARIO-ETS-PART1-009-RELEASED-COMMON-FILTERS-001 (CRITICAL)
**GIVEN** every canonical Part 1 resource endpoint whose class is declared
**WHEN** the ETS derives `id`, UID, UID-prefix, `q`, or custom-property values
from existing resources and executes each filter
**THEN** every page returns HTTP 200 and every returned item satisfies the
requested predicate
**AND** known matching evidence cannot yield an empty PASS.

##### SCENARIO-ETS-PART1-009-RELEASED-OWNER-APPLICABILITY-001 (CRITICAL)
**GIVEN** `/conf/advanced-filtering` and a subset of canonical Part 1 resource
classes are declared
**WHEN** an endpoint-wide or resource-specific Advanced Filtering procedure
selects applicable canonical endpoints
**THEN** it evaluates only endpoints whose owning resource class is declared
**AND** a declared owner's unavailable canonical endpoint FAILs rather than
being silently omitted
**AND** reachable endpoints for undeclared owners cannot supply PASS evidence.

##### SCENARIO-ETS-PART1-009-RELEASED-UID-PREFIX-001 (CRITICAL)
**GIVEN** a canonical resource has a non-empty UID from which a shorter prefix
can be derived
**WHEN** the ETS submits that prefix with a trailing `*`
**THEN** the known resource is present and every result UID starts with the
unstarred prefix
**AND** empty-prefix probes, non-prefix results, and later-page non-prefix
results cannot PASS.

##### SCENARIO-ETS-PART1-009-RELEASED-KEYWORD-SOURCE-001 (CRITICAL)
**GIVEN** a canonical resource representation
**WHEN** the ETS derives and verifies a `q` predicate
**THEN** keyword evidence comes only from `name`, `description`, or the
SensorML-equivalent `label` at the resource root or immediate GeoJSON
`properties` boundary
**AND** every returned resource, but not necessarily the originally selected
seed, contains the requested keyword
**AND** link metadata, arbitrary extension descendants, association targets,
and unrelated scalar extension properties cannot create a false PASS.

##### SCENARIO-ETS-PART1-009-RELEASED-GEOMETRY-001 (CRITICAL)
**GIVEN** Systems, Deployments, or Sampling Features with usable GeoJSON
geometry
**WHEN** the ETS submits a valid WKT `geom` filter
**THEN** every returned feature has geometry intersecting the parsed filter
geometry according to JTS
**AND** geometry-free features are rejected from the result.

##### SCENARIO-ETS-PART1-009-RELEASED-SYSTEM-ASSOCIATIONS-001 (CRITICAL)
**GIVEN** System parent, Procedure, feature-of-interest, observed-property, or
controlled-property relation evidence
**WHEN** the corresponding filter is executed with local IDs and UIDs
**THEN** every returned System is validated through the System representation
boundary and exposes the requested direct or recursively inherited relation.

##### SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PROVENANCE-001 (CRITICAL)
**GIVEN** a same-origin association wrapper whose own ID, UID, path token, or
canonical href differs from the target representation's local ID and UID
**WHEN** local-ID and UID repetitions are selected
**THEN** the values come from the resolved target representation
**AND** neither wrapper identifiers, the path token, nor the canonical href is
accepted as synthetic identifier evidence after successful resolution
**AND** a malformed href contributes no identifier and is never replaced by
an invented URI.

##### SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PATHS-001 (CRITICAL)
**GIVEN** an association procedure prescribes a deployed-System,
features-of-interest, Datastream, or ControlStream traversal
**WHEN** equivalent-looking identifiers appear only in unrelated root aliases
or extension descendants
**THEN** those shortcuts cannot establish the predicate
**AND** evidence is accepted only from the procedure-specific direct relation
or prescribed subresource and target-description traversal
**AND** direct representation fields use exact recognized association names;
suffix aliases and matching names inside unrelated nested extension objects
cannot seed or validate a predicate
**AND** released GeoJSON `links[].rel` values use exact direct names or exact
`ogc-rel:` compact relation names, GeoJSON Procedure evidence accepts
`systemKind@link`, and SensorML System evidence accepts `attachedTo` and
`typeOf`
**AND** GeoJSON relation links, GeoJSON property links, and SensorML members
are matched against separate exact vocabularies at their prescribed
representation boundaries
**AND** case, punctuation, a trailing `Link` suffix, and broad `parent` or
`procedure` aliases cannot be normalized into released association evidence
**AND** unrelated URI schemes or merely suffix-matching relation values cannot
seed or validate a predicate
**AND** Deployment observed-property and controlled-property evidence ignores
wrapper properties and unrelated nested hrefs, follows only the direct
deployed-System target, and reads properties from the resolved System
description
**AND** a dereferenced deployed-System target must be one single System
representation; a collection or non-System object contributes no property
evidence
**AND** allowed GeoJSON System feature types are `sosa:System`,
`sosa:Sensor`, `sosa:Actuator`, `sosa:Sampler`, and `sosa:Platform` in CURIE
or full-URI form
**AND** allowed SensorML System classes are `PhysicalComponent`,
`PhysicalSystem`, `SimpleProcess`, and `AggregateProcess` with an allowed
System definition
**AND** arbitrary types that merely end with `System` cannot contribute
property evidence.

##### SCENARIO-ETS-PART1-009-RELEASED-REPRESENTATION-SCOPED-RELATIONS-001 (CRITICAL)
**GIVEN** a SensorML System whose generic `links` member contains
`parentSystem` or `ogc-rel:parentSystem`
**WHEN** the ETS derives parent filters or combined-filter predicates
**THEN** those GeoJSON-only relations cannot create SensorML association
evidence
**AND** exact SensorML `attachedTo` remains valid parent evidence
**AND** exact SensorML `typeOf` remains valid Procedure evidence.

##### SCENARIO-ETS-PART1-009-RELEASED-DEPLOYMENT-ASSOCIATIONS-001 (CRITICAL)
**GIVEN** Deployment parent, deployed-System, feature-of-interest,
observed-property, or controlled-property relation evidence
**WHEN** the corresponding filter is executed with local IDs and UIDs
**THEN** every returned Deployment is representation-valid and its traversed
associations establish the requested predicate.

##### SCENARIO-ETS-PART1-009-RELEASED-PROCEDURE-ASSOCIATIONS-001 (CRITICAL)
**GIVEN** Procedure observed-property or controlled-property evidence
**WHEN** the corresponding filter is executed with local IDs and UIDs
**THEN** every returned Procedure is representation-valid and references one
of the requested properties.

##### SCENARIO-ETS-PART1-009-RELEASED-SF-ASSOCIATIONS-001 (CRITICAL)
**GIVEN** Sampling Feature sample-of, Datastream, or ControlStream relations
**WHEN** feature-of-interest, observed-property, or controlled-property
filters are executed
**THEN** every returned Sampling Feature is representation-valid and its
bounded traversed relation graph establishes the requested predicate.

##### SCENARIO-ETS-PART1-009-RELEASED-PROPERTY-FILTERS-001 (CRITICAL)
**GIVEN** Property base-property and object-type evidence
**WHEN** `baseProperty` and `objectType` filters are executed
**THEN** every returned Property is representation-valid and matches the
requested recursive base relation or object type.

##### SCENARIO-ETS-PART1-009-RELEASED-COMBINED-FILTERS-001 (CRITICAL)
**GIVEN** multiple seed-derived filters available for a canonical endpoint
**WHEN** the ETS submits every independently evidenced pairwise combination
**THEN** every returned item satisfies every supplied predicate
**AND** the predicate inventory includes applicable inherited `id`, `q`,
`featureType`, `datetime`, and geometry filters, every applicable mandatory
Advanced Filtering association filter, and any positively supported
custom-property recommendation
**AND** every canonical endpoint exercises at least two distinct combinations
**AND** the procedure cannot PASS from union semantics, one hard-coded
combination, or an empty result
**AND** every filtered collection passes its released media-specific
representation validator before predicate evidence is accepted
**AND** unsupported generic JSON with matching fields cannot PASS.

##### SCENARIO-ETS-PART1-009-RELEASED-INDIRECT-RECOMMENDATIONS-001 (NORMAL)
**GIVEN** base-property or nested feature-of-interest relation evidence
**WHEN** direct and transitive filter result sets are compared
**THEN** transitive sets include their direct descendants
**AND** the released indirect-property procedure uses `observedProperty` for
Systems, Deployments, Procedures, and Sampling Features and `baseProperty` for
Properties, without inventing a controlled-property repetition
**AND** every eligible Property and every eligible Sampling Feature is
evaluated, including resources on later collection pages
**AND** unsupported recommendation behavior emits a visible warning rather
than a mandatory conformance failure.

##### SCENARIO-ETS-PART1-009-RELEASED-MEDIA-PAGINATION-001 (CRITICAL)
**GIVEN** a filtered collection has multiple pages or association links
**WHEN** evidence is traversed
**THEN** every page is status/media gated before parsing
**AND** successfully resolved same-origin association collections contribute
all target representations across pagination
**AND** broken or unsupported-media association targets contribute only their
target URI where the released ATS explicitly permits unresolved target
identity, never wrapper IDs or parsed unsupported content
**AND** pagination and same-origin association traversal reject cycles,
over-limit graphs, cross-origin credential forwarding, and later-page defects.
Depth and reference-read limits SHALL fail explicitly rather than silently
truncate relation evidence.

##### SCENARIO-ETS-PART1-009-RELEASED-PROCEDURE-ISOLATION-001 (CRITICAL)
**GIVEN** one released procedure lacks seed evidence or SKIPs
**WHEN** another procedure runs
**THEN** it independently obtains its own evidence and remains executable
**AND** class setup contains no response, seed, or parsed-body state.

##### SCENARIO-ETS-PART1-009-RELEASED-DEPENDENCY-CASCADE-001 (CRITICAL)
**GIVEN** Advanced Filtering directly inherits Part 1 API Common
**WHEN** API Common has a blocking failure or unexpected SKIP
**THEN** every Advanced Filtering method SKIPs before class-specific IUT access
**AND** System or sibling failures do not block Advanced Filtering.

##### SCENARIO-ETS-PART1-009-RELEASED-DIRECT-HTTP-COVERAGE-001 (CRITICAL)
**GIVEN** a controlled read-only declaring HTTP fixture
**WHEN** all 25 released procedures execute
**THEN** every positive path and key fail-closed branch is exercised
**AND** the fixture records zero POST, PUT, PATCH, or DELETE requests.

##### SCENARIO-ETS-PART1-009-RELEASED-E2E-EXECUTION-001 (CRITICAL)
**GIVEN** the exact Sprint 55 image and unmodified local OSH
**WHEN** Dockerized TeamEngine runs the full suite
**THEN** all 25 Advanced Filtering methods are discovered exactly once
**AND** undeclared-class outcomes remain honest pre-filter SKIPs, not
conformance passes
**AND** this honest deployment evidence is not described as positive
Advanced Filtering conformance by the local OSH IUT
**AND** no OSH or TeamEngine source or binary is modified.

##### Sprint 55 Implementation Evidence

The remediated implementation has 25 independent methods and coverage
`240 total / 76 exact / 2 helper / 115 candidate / 47 unmapped`, with
`/conf/advanced-filtering` at `25/25 exact`. R4 requirement-linked tests
reproduce four semantic findings at `40/4/0/0` and pass after remediation at
`40/0/0/0`. Full precommit Docker Maven passes `594/0/0/3`; every one of the
20 scenario IDs has a literal Java anchor.

R3 returned `GAPS_FOUND 0.98` for Deployment property wrapper shortcuts,
synthetic malformed-href identities, mapping overstatement, evidence
preservation, and contract traceability. Deployment properties now follow
only direct deployed-System targets and read resolved System properties;
wrapper aliases and unrelated nested hrefs cannot establish evidence.
Malformed hrefs contribute no identity or invented URI. Mapping descriptions
and the contract now match demonstrated behavior.

R4 returned `GAPS_FOUND 0.99` because arbitrary suffix/nested aliases could
still establish direct relations and dereferenced Deployment property targets
were not proven to be single Systems. Relation discovery now accepts only
exact recognized root or immediate GeoJSON `properties` fields and explicit
relation-link URIs; recursive reference containers remain bounded. A
dereferenced deployed-System target must be a non-collection System
representation before its properties contribute evidence.

Candidate `060a8aa994d59f0adfa6bfa96fd5fb372b3d6743` passes exact focused
controlled HTTP `40/0/0/0`, full Docker Maven `594/0/0/3`, released-source
audit, runtime verification on image
`sha256:a74b3cc8bfe71df11ef4cc13ef8ceb6c0b32e0cffc184e04f9f115c2f215f07e`,
and the `20/20` scenario trace. Unmodified local OSH TeamEngine is honestly
`238/40/7/191`; all 25 Advanced Filtering methods SKIP at the absent
declaration, so this is deployment evidence rather than positive local-IUT
conformance. API Common sabotage is `238/2/10/226`; credential,
no-mutation, immutability, artifact-hygiene, and source gates pass. Candidate
`756d729` remains superseded audit evidence. Controlled HTTP remains the
positive semantic harness; no OSH or TeamEngine source or binary is modified.
Fresh Raze R5 returned `GAPS_FOUND 0.99` with four required fixes. Canonical
compact `ogc-rel:` values and `systemKind@link` were rejected; the
deployed-System type gate rejected valid released classes and admitted
arbitrary `*System` suffixes; `/prop-by-object` and `/combined-filters`
ignored `validateEndpoint=false`; and the exact keyword mapping overstated
selected-seed inclusion. Candidate `060a8aa` and its exact gates are now
superseded audit evidence. R5 HTTP regressions reproduce `42/3/4/0`; focused
remediation passes `48/0/0/0`, full Docker Maven passes `602/0/0/3`, and the
regenerated coverage report remains `240/76/2/115/47` with Advanced Filtering
`25/25 exact`. Exact candidate `f2a88d54e643f9c91cfcc432f7d7bc403bfab6f0`
passes focused `48/0/0/0`, full Maven `602/0/0/3`, image/runtime,
released-source, unmodified-local-OSH `238/40/7/191`, sabotage
`238/2/10/226`, credential, immutability, and hygiene gates. Fresh Raze R6
returned `GAPS_FOUND 0.99`: the shared relation matcher lowercases,
removes punctuation, strips a trailing `Link`, and retains broad `parent` and
`procedure` aliases. Consequently values such as `parentSystemLink`,
`ogc-rel:parentSystemLink`, and punctuation/case variants can manufacture
association evidence. Candidate `f2a88d5` and its exact gates are superseded;
representation-aware exact matching now gives GeoJSON relation links,
GeoJSON property links, SensorML members, and generic wrappers separate
case-sensitive vocabularies. Trailing-`Link`, punctuation/case, `parent`, and
`procedure` near misses are rejected. R6 controlled HTTP moves from
`46/3/0/0` to `46/0/0/0`; focused Maven passes `51/0/0/0` and full Docker
Maven passes `605/0/0/3`. A new committed candidate, repeated exact gates, and
another fresh Raze review remain. Exact candidate
`b5bc49b2922e0a47b73225c2dabc0422ac7998f4` then passed every repeated
technical and E2E gate, but Raze R7 returned `GAPS_FOUND 0.99`. Link relations
are evaluated before representation dispatch, so a SensorML System can still
reuse GeoJSON `parentSystem` or `ogc-rel:parentSystem` evidence instead of the
released `attachedTo` mapping. Candidate `b5bc49b` is superseded; the
representation-scoped relation scenario reproduces the defect at `48/2/0/0`.
Link relations are now inspected only after GeoJSON dispatch. Controlled HTTP
passes `48/0/0/0`, focused Maven passes `53/0/0/0`, and full Docker Maven
passes `607/0/0/3`. Exact candidate
`fce461288e99167bab6f391085493784da42cc58` and image
`sha256:ed03d1f943da442d8c13bdfc5c140b08c1e9155a57f3f10696925a2a0a402a79`
pass released-source, runtime, unmodified-local-OSH `238/40/7/191`, sabotage
`238/2/10/226`, credential, immutability, and hygiene gates. Fresh Raze R8 is
`APPROVE 0.99`, closes both prior findings, and reports no new findings.

> Sprint 12 starts the mutation-side Part 1 work with Create/Replace/Delete, but it does not permit unguarded writes against the public GeoRobotix smoke target. GeoRobotix declares `/conf/create-replace-delete` and advertises POST/PUT/DELETE via OPTIONS, so default smoke must prove declaration and non-mutating readiness while every lifecycle mutation assertion SKIPs unless an operator explicitly enables mutation tests against a dedicated mutable IUT.

#### REQ-ETS-PART1-010: Create/Replace/Delete Conformance Class (`/conf/create-replace-delete`) (Sprint 12 target)
- **Priority**: MUST
- **Status**: IN_PROGRESS_POSITIVE_MUTATION_E2E_BLOCKED (Sprint 56)
- **Sprint 56 accepted closure**: Replace the historical six-method Systems
  subset with exactly twelve independent methods matching the released
  `v1.0.0` Annex A procedures. Every procedure SHALL check the exact Part 1
  declaration, direct API Common prerequisite, the exact released Annex A
  inherited URI
  `http://www.opengis.net/spec/ogcapi-4/1.0/conf/create-replace-delete`,
  applicable resource-class condition,
  and the explicit dedicated-mutable-IUT gate before writes. A reusable
  transaction helper SHALL verify OPTIONS; POST 201 plus Location or queued
  POST 202; canonical GET content; PUT 200/204 or queued PUT 202 plus
  changed-content GET; DELETE 200/204 or queued DELETE 202; and observed
  deletion postconditions at every prescribed endpoint and for every
  applicable declared representation. HTTP 202 SHALL be treated as accepted
  but not positive lifecycle evidence until the required postcondition is
  observed through bounded, configurable polling. One monotonic deadline SHALL
  govern all required postconditions of one queued operation; deadline checks
  SHALL precede probes, HTTP connect/read timeouts and sleeps SHALL be capped
  to remaining time, interruption SHALL fail visibly, and late success after
  expiry SHALL not count. A queued operation whose
  postcondition is not observed within that bound SHALL SKIP as inconclusive,
  while cleanup still performs bounded identity discovery and reports any
  failure as FAIL rather than allowing the primary SKIP to hide it. The class
  SHALL also verify both
  released System cascade graphs, canonical availability after nested create,
  custom-collection create/replace/root-versus-non-root delete propagation,
  and `text/uri-list` association behavior. Procedures SHALL own and clean
  their resources independently; cleanup failures remain visible. TestNG
  group prerequisites SHALL remain causal: released procedures SHALL not use
  `alwaysRun` to bypass a failed API Common prerequisite. A write response
  location SHALL become a destructive cleanup target only after dereference
  proves the submitted resource identity; absent or incorrect locations SHALL
  fall back to same-origin root discovery by that identity. Deleting a
  verified noncanonical Location SHALL never terminate cleanup: root identity
  discovery SHALL still remove any surviving canonical resource.
- **Released source authority**: OGC 23-001 Annex A at tag `v1.0.0`, commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`. The referenced OGC API Features
  Part 4 draft transaction semantics are reproduced from tag
  `part4-1.0.0-draft.1`, commit
  `ea42aa1de6d8cbb53c526f41e1f66c1887fe71d4`. CP-016 is authoritative for the
  Sprint 56 delta.
- **Sprint 56 implementation evidence**: `CreateReplaceDeleteTests` now exposes
  exactly twelve independent released procedures with causal API Common group
  dependencies and no `alwaysRun`. `CreateReplaceDeleteSupport` implements
  schema-validated GeoJSON/SensorML writes, inherited lifecycle checks,
  identity-safe cleanup, both cascade graphs, nested canonical verification,
  and complete custom-collection propagation checks. Controlled HTTP includes
  prerequisite sabotage and fail-closed Location, cascade, and custom
  collection cases. Candidate `f6e3587` passed exact focused `28/0/0/0`, full
  Maven `630/0/0/3`, released-source, schema-parity, runtime, dependency,
  credential, immutable-base, and hygiene gates, but is superseded after Raze
  found incomplete queued-response semantics and a custom-alias cleanup hole.
  The replacement accepts HTTP 202 only with bounded observed postconditions,
  SKIPs timeout as accepted-but-inconclusive, and continues canonical root
  identity cleanup after alias deletion. Delayed/stalled 202 and alias-leak
  focused CRD passes `35/0/0/0`, including queued/alias/property HTTP
  regressions `18/0/0/0`; full Docker Maven is `637/0/0/3`. Replacement
  candidate `0023d5b492dff8b5dbeff6c201c257f970b8947a` passed exact
  released-source, schema-parity, image/runtime, dependency, credential,
  immutable-base, and artifact-hygiene gates with image
  `sha256:4764227eda6ab91d5895df7bce74d440b0c95842127a0514debd67b857ed0744`.
  Raze returned `GAPS_FOUND 0.99`, superseding that candidate. Seven
  requirement-linked hard-deadline, compound-postcondition,
  cleanup-precedence, interruption, and late URI-list cleanup regressions now
  pass direct HTTP `25/0/0/0`; the initial red run was `25/6/0/0`, with the
  interruption regression tightened during implementation. Exact candidate
  `1a6c5ec30f76e120a0e2cd676f472699141213ca` passes focused `42/0/0/0`,
  full Docker Maven `644/0/0/3`, released-source, schema-parity, image/runtime,
  dependency, credential, immutable-base, and artifact-hygiene gates. Its image
  is
  `sha256:6939ef2ea40ff42328d4ff972b691dd4ba1c6a59855fe913d175b09f9555c1da`
  with `Build-Revision: 1a6c5ec30f`. Raze returned `GAPS_FOUND 0.98`,
  superseding it for unguarded pagination/candidate request boundaries,
  incomplete queued custom replace/delete setup, and discarded queued
  URI-list occurrence Locations. Six requirement-linked regressions reproduce
  those paths. The corrected direct HTTP suite passes `31/0/0/0`, focused
  aggregate passes `48/0/0/0`, and full Docker Maven passes `650/0/0/3`.
  Exact committed candidate
  `8aa92d4da33aeb3b1c545378c0a68cb84a565ccb` repeats those gates and passes
  released-source, schema-parity, image/runtime, local-OSH, dependency,
  credential, immutable-base, and artifact-hygiene verification. Raze
  `GAPS_FOUND 0.97` supersedes it. Identity-safe occurrence cleanup and joint
  compound polling remediation records behavioral red `35/2/0/0`, then passes
  direct HTTP `35/0/0/0`, focused aggregate `52/0/0/0`, and full clean-cache
  Docker Maven `654/0/0/3` precommit. Exact candidate
  `700c697e59eb2a03d3a41a37ec9a745cd1aa3583` repeats those technical and E2E
  gates, but Raze `GAPS_FOUND 0.98` supersedes it because an absolute
  cross-origin HTTP 202 status Location failed before status classification
  and the raw red log was not sealed. The requirement-linked cross-origin
  regression records `1/1/0/0`; parsing before same-origin occurrence
  classification corrects it without external requests. Precommit verification
  passes direct HTTP `36/0/0/0`, focused aggregate `53/0/0/0`, and full
  clean-cache Docker Maven `655/0/0/3`. Exact candidate
  `a2ce5478e25542a766025a2a5fde246fc2d5f8d6` repeats those gates; embeds
  `Build-Revision: a2ce5478e2`; and passes released-source, both parity graphs,
  image/runtime, immutable-base, local-OSH, sabotage, credential, and artifact
  hygiene verification. Its pre-review root 71-file and nested 31-file
  manifests verify; the final 72-file root manifest also seals the review.
  Fresh Raze returns `APPROVE_WITH_CONCERNS 0.99`, closes every prior finding,
  and has no candidate-scoped required fix. Positive mutation E2E remains its
  sole external completion concern.
  The class coverage inventory is
  intentionally `0 exact / 0 helper / 12 candidate / 0 unmapped`.
  Unmodified local OSH reports Part 1 API Common `4 PASS / 1 SKIP`, so causal
  inheritance dependency-SKIPs all twelve procedures before their declaration
  checks and writes, even though it advertises Part 1
  `/conf/create-replace-delete`. OSH also omits the exact Connected Systems API
  Common and
  inherited `ogcapi-4` declarations. Positive real-IUT mutation E2E therefore
  remains open, and this requirement remains
  IN_PROGRESS_POSITIVE_MUTATION_E2E_BLOCKED.
- **Historical increment**: by Sprint 12 Generator (2026-05-05; story S-ETS-12-01). Implemented outcome is declaration, non-mutating method-advertisement readiness, TestNG wiring, explicit mutation opt-in plumbing, public GeoRobotix hard-denial, default-smoke safety, service-relative `Location` handling for OSH-style `/systems/{id}` responses, and a guarded lifecycle path for dedicated mutable IUTs. Full create/replace/delete lifecycle conformance remains OPEN for the overall requirement class because deployment/procedure/sampling-feature/property CRUD, cascade behavior, custom collections, `text/uri-list`, and `/conf/update` remain out of scope.
- **OGC source verified**: Upstream `opengeospatial/ogcapi-connected-systems` commit `3fd86c73e744b7e2faaf7f1c17366bfb9ff4cd6f`. Requirement class file exists at `api/part1/standard/requirements/crud/requirements_class_crd.adoc`; explanatory clause exists at `api/part1/standard/sections/clause_16_requirements_class_create_replace_delete.adoc`. The class identifier is `/req/create-replace-delete`, inherits `/req/api-common` and OGC API Features Part 4 Create/Replace/Delete, and lists subrequirements for systems, system delete cascade, subsystems, deployments, subdeployments, procedures, sampling features, properties, collection propagation, and adding resources to collections by `text/uri-list`.
- **Sprint 12 coverage scope**: Create/Replace/Delete safety-gated systems subset with 6 planned @Tests: (1) IUT declares `/conf/create-replace-delete`; (2) default mutation safety gate is active unless suite parameter `mutation-tests-enabled=true` is supplied together with `mutation-iut-policy=dedicated-mutable-iut`; (3) `OPTIONS /systems` is recorded as an ETS readiness precondition for POST advertisement without issuing POST; (4) `OPTIONS /systems/{id}` is recorded as an ETS readiness precondition for PUT/DELETE advertisement without issuing PUT/DELETE; (5) systems lifecycle create/replace/delete test SKIPs by default with reason and, only when explicitly enabled against a dedicated mutable IUT that is not a known shared public GeoRobotix URL, performs POST/PUT/DELETE with best-effort cleanup; (6) TestNG dependency wiring and smoke no-regression. OPTIONS readiness PASS does not satisfy `/req/create-replace-delete/system`; lifecycle conformance remains SKIP by default until POST/PUT/DELETE run against a dedicated mutable IUT. The sprint deliberately does not close deployment/procedure/sampling-feature/property CRUD, cascade delete semantics, collection propagation, `text/uri-list`, or update/PATCH.
- **Mutation safety policy**: Mutating HTTP methods MUST NOT run during default GeoRobotix smoke even though GeoRobotix currently declares `/conf/create-replace-delete` and advertises `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS` on `/systems` and `/systems/{id}`. Generator MUST introduce explicit opt-in parameters and a hard safety gate before any POST/PUT/DELETE request is issued. The parameter path is in scope end-to-end: `TestRunArg`, `SuiteAttribute`, `SuiteFixtureListener`, optional `TestNGController` validation/acceptance, CTL controls, and optional smoke-script env forwarding (`SMOKE_MUTATION_TESTS_ENABLED`, `SMOKE_MUTATION_IUT_POLICY`). Even when both opt-in parameters are present, the implementation MUST hard-deny mutation against known shared public GeoRobotix URLs, including `https://api.georobotix.io/ogc/t18/api`. Default smoke MUST report lifecycle mutation assertions as SKIP-with-reason, not PASS.
- **No-mutation smoke oracle**: Default smoke no-mutation proof MUST inspect IUT-bound REST Assured request-log entries, not naive process-wide method strings. The oracle parses current `Request: METHOD URI` entries and the older adjacent `Request method:` / `Request URI:` pair format, filters to URIs starting with the IUT base URL, requires at least one recognized IUT-bound request entry, and requires zero POST/PUT/DELETE entries for GeoRobotix. The TeamEngine control-plane POST that starts the suite run is excluded from this oracle because its URI is not IUT-bound.
- **Historical dependency wiring (superseded by Sprint 56)**: Sprint 12
  configured Create/Replace/Delete after SystemFeatures for its Systems-only
  subset. Sprint 56 replaces that relationship: API Common is the sole direct
  TestNG prerequisite, and each released procedure evaluates its own resource
  declaration and evidence.
- **Open subrequirements after Sprint 12**: System delete cascade, subsystem creation, deployment/subdeployment/procedure/sampling-feature/property create/replace/delete, custom collection propagation, adding resources to collections by `text/uri-list`, and all `/conf/update` PATCH behavior remain OPEN unless separately planned.
- **Generator evidence**: Docker Maven `105 tests / 0 failures / 0 errors / 3 skipped`; TeamEngine smoke from `/tmp/sprint-ets-12-generator-smoke-current-r3` against GeoRobotix `69 total / 52 passed / 0 failed / 17 skipped`; CreateReplaceDelete runtime outcome is 4 PASS and 2 SKIP-by-safety-gate; integrated smoke log oracle reported zero IUT-bound POST/PUT/DELETE entries after recognizing 40 IUT-bound request log entries.
- **Local mutable-IUT follow-up evidence**: Local OpenSensorHub 2.0-beta2 at `http://localhost:8081/sensorhub/api`, reached by TeamEngine over Docker network `field-hub_default` as `http://field-hub-osh-1:8081/sensorhub/api`, declares `/conf/create-replace-delete` and permits admin-authenticated transactions. Probe `r4` (`/tmp/ets-csapi-osh-mutable-smoke-r4`) produced real CRD PASS evidence for `systemsCreateReplaceDeleteLifecycle`: POST `/systems`, PUT `/systems/0410`, and DELETE `/systems/0410` all succeeded after the ETS preserved the created System `uid` across replacement and resolved `Location: /systems/0410` against the IUT service base. Follow-up on 2026-05-06 updated the local OSH `proxyBaseUrl` to `http://field-hub-osh-1:8081`, seeded synthetic System/Procedure/Deployment/SamplingFeature resources from `ops/local-osh-seed-fixtures.json`, set the System `featureType` to `http://www.w3.org/ns/sosa/System` so SensorML `?f=sml3` resolves locally, and reran TeamEngine smoke from `/tmp/ets-csapi-osh-full-health-r3`: `69 total / 50 passed / 0 failed / 19 skipped`. Skips remain expected for undeclared or unpopulated out-of-scope surfaces such as AdvancedFiltering, GeoJSON feature-collection fallback, properties, subsystems, and subdeployments. Raze full-health review found and the same turn fixed two false-confidence gaps: smoke stdout now prints exact parsed totals instead of `${total}/${total}`, and the local OSH seed payloads are versioned. The local OSH evidence upgrades the maintained mutable-IUT health target from CRD-only PASS to full smoke failed=0, but the overall REQ remains PARTIAL for non-system CRUD and unimplemented CRD subrequirements.
- **Maps to**: PRD FR-ETS-20.

> Sprint 13 continues the mutation-side Part 1 work with Update/PATCH. It reuses the Sprint 12 mutation-safety contract. GeoRobotix does not currently declare `/conf/update`, and `OPTIONS /systems/{id}` does not advertise PATCH, so default smoke must report Update assertions as SKIP-with-reason and prove that no IUT-bound PATCH was issued.

#### REQ-ETS-PART1-011: Update Conformance Class (`/conf/update`) (Sprint 13)
- **Priority**: MUST
- **Status**: IN_PROGRESS_POSITIVE_MUTATION_E2E_BLOCKED
- **Historical increment**: by Sprint 13 Generator (2026-05-06; story S-ETS-13-01). Implemented declaration-gated `/conf/update`, PATCH mutation safety gate, non-mutating `OPTIONS /systems/{id}` readiness, default lifecycle SKIP-before-PATCH, hard-denial for public GeoRobotix, TestNG dependency on Create/Replace/Delete, and default-smoke no-PATCH evidence. Full Update conformance remains OPEN for deployment/procedure/sampling-feature/property PATCH, Feature Collection update paths, Part 2 update, optimistic locking, and PATCH media-type matrix.
- **OGC source verified**: OGC API - Connected Systems Part 1 Clause 17, Requirements Class "Update" `/req/update`, Conformance Class A.11 `/conf/update`. The upstream requirement class source is `api/part1/standard/requirements/crud/update/requirements_class_update.adoc` at `opengeospatial/ogcapi-connected-systems` commit `3fd86c73e744b7e2faaf7f1c17366bfb9ff4cd6f`; the explanatory clause is `api/part1/standard/sections/clause_17_requirements_class_update.adoc`. The class prerequisite is `/req/create-replace-delete` plus OGC API Features Part 4 `/req/update`. Normative statements are `/req/update/system`, `/req/update/deployment`, `/req/update/procedure`, `/req/update/sampling-feature`, and `/req/update/property`. OGC Part 1 ATS A.79-A.83 also lists Feature Collection item update paths under `/collections/{collectionId}/items/{id}` for systems, deployments, procedures, sampling features, and properties; Sprint 13 explicitly defers those collection item PATCH paths.
- **Sprint 13 coverage scope**: Update safety-gated systems subset with 5 @Tests: (1) IUT declares `/conf/update`, otherwise Update tests SKIP with reason; (2) default mutation safety gate is active unless suite parameter `mutation-tests-enabled=true` is supplied together with `mutation-iut-policy=dedicated-mutable-iut`; (3) `OPTIONS /systems/{id}` is recorded as an ETS readiness precondition for PATCH advertisement without issuing PATCH; (4) systems PATCH lifecycle test SKIPs by default with reason and, only when explicitly enabled against a dedicated mutable IUT that declares `/conf/update`, advertises PATCH, and is not a known shared public GeoRobotix URL, performs PATCH with best-effort cleanup; (5) TestNG dependency wiring and smoke no-regression. OPTIONS readiness PASS does not satisfy `/req/update/system`; lifecycle conformance remains SKIP by default until PATCH runs against a dedicated mutable IUT.
- **Mutation safety policy**: PATCH MUST NOT run during default GeoRobotix smoke. Sprint 13 reuses Sprint 12's mutation opt-in parameters and hard-denial list. Even when both opt-in parameters are present, the implementation MUST hard-deny mutation against known shared public GeoRobotix URLs, including `https://api.georobotix.io/ogc/t18/api`, before any PATCH is issued. Default smoke MUST report lifecycle mutation assertions as SKIP-with-reason, not PASS.
- **No-mutation smoke oracle**: Default smoke no-mutation proof MUST include PATCH as a mutating method. The existing IUT-bound request-log oracle parses current `Request: METHOD URI` entries and older adjacent `Request method:` / `Request URI:` pair format, filters to URIs starting with the IUT base URL, requires at least one recognized IUT-bound request entry, and requires zero POST/PUT/DELETE/PATCH entries for GeoRobotix. The TeamEngine control-plane POST that starts the suite run is excluded from this oracle because its URI is not IUT-bound.
- **Dependency wiring**: Update depends on Create/Replace/Delete via `<group name="update" depends-on="createreplacedelete"/>`. The Sprint 13 systems subset requires the Sprint 12 mutation safety gate and Create/Replace/Delete prerequisite wiring before PATCH behavior can be assessed.
- **Planning probe evidence**: GeoRobotix `/conformance` on 2026-05-06 does not declare `http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/update`. `OPTIONS https://api.georobotix.io/ogc/t18/api/systems/0mqcvdnfoca0` returned HTTP 200 with `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS`; PATCH is absent. Local OSH unauthenticated `/conformance` returned HTTP 401, and unauthenticated `OPTIONS /systems/040g` returned HTTP 200 with no PATCH in `Allow`; authenticated local OSH remains useful as a mutable fixture but does not currently provide positive PATCH evidence.
- **Generator verification evidence**: Docker Maven `bash scripts/mvn-test-via-docker.sh` completed BUILD SUCCESS with `113 tests / 0 failures / 0 errors / 3 skipped`; Maven log archived at `ops/test-results/sprint-ets-13-maven-2026-05-06.log`. TeamEngine default smoke against GeoRobotix reported `74 total / 52 passed / 0 failed / 22 skipped` with 41 recognized IUT-bound request-log entries and zero IUT-bound POST/PUT/DELETE/PATCH entries. Because Update depends on Create/Replace/Delete and the default CRD mutation gate skips, the Update configuration method records missing `/conf/update`, while the five Update @Tests are dependency-skipped through `createreplacedelete`; no PATCH is issued.
- **Sprint 14 hardening evidence**: `S-ETS-14-01` keeps REQ-ETS-PART1-011 PARTIAL and strengthens only the guarded systems PATCH path. Positive PATCH lifecycle evidence now requires a GET after PATCH and an assertion that the intended changed field, initially `properties.name`, changed to the expected value. A PATCH status code alone is not conformance evidence. Missing `OPTIONS Allow: PATCH` follows an explicit verdict matrix: absent `/conf/update`, missing mutation opt-in, public IUT hard-denial, no candidate resource, or inconclusive OPTIONS are SKIP-before-PATCH states; declared `/conf/update` plus successful `OPTIONS /systems/{id}` whose `Allow` omits PATCH FAILs the readiness assertion for `/req/update/system`, while the lifecycle test still SKIPs before PATCH because the precondition failed; declared `/conf/update` plus explicit mutation opt-in plus `Allow: PATCH` may run the guarded lifecycle. Docker Maven reported `117 tests / 0 failures / 0 errors / 3 skipped`; default TeamEngine smoke against GeoRobotix reported `74 total / 52 passed / 0 failed / 22 skipped` and zero IUT-bound POST/PUT/DELETE/PATCH across 41 recognized IUT-bound request-log entries. Local OSH remains a dedicated mutable CRD fixture, but current Generator evidence shows `/conformance` returned HTTP 401 and `OPTIONS /systems/040g` does not advertise PATCH; Sprint 14 does not claim local OSH positive Update support.
- **Open subrequirements after Sprint 13**: Deployment, procedure, sampling-feature, and property PATCH; Feature Collection update paths under `/collections/{collectionId}/items/{id}`; Part 2 `/conf/update`; optimistic locking; and PATCH media-type matrix, including JSON Patch, merge patch, and content negotiation, remain OPEN unless separately planned.
- **Sprint 57 supersession (CP-017)**: Sprint 57 replaces the historical five
  mixed declaration/readiness/lifecycle methods with exactly the five released
  `/conf/update/{system,deployment,procedure,sampling-feature,property}`
  procedures from OGC 23-001 tag `v1.0.0`, commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`. Each procedure owns canonical
  and advertised applicable custom collection endpoints, exact declaration and
  condition checks, patch-document negotiation, a schema-valid temporary
  resource, completed-update proof, and identity-safe cleanup.
- **Sprint 57 inheritance correction**: The released requirements class
  `/req/update` inherits `/req/create-replace-delete`, but released Annex A
  `/conf/update` directly inherits `/conf/api-common` and
  `http://www.opengis.net/spec/ogcapi-4/1.0/conf/update`. Therefore Update's
  direct TestNG dependency is Part 1 API Common, not the sibling
  Create/Replace/Delete group. POST and DELETE remain fixture ownership
  operations. The exact `ogcapi-4` URI is required; the
  `ogcapi-features-4` near-match is not accepted.
- **Sprint 57 inherited operation contract**: The pinned Features Part 4 draft
  tag `part4-1.0.0-draft.1`, commit
  `ea42aa1de6d8cbb53c526f41e1f66c1887fe71d4`, requires PATCH support,
  OPTIONS advertisement, a partial-change document, ignored submitted resource
  identifier, and HTTP 200/202/204 behavior, while explicitly declining to
  mandate one patch encoding. The ETS negotiates implemented JSON Merge Patch
  or JSON Patch from `Accept-Patch` or the exact OpenAPI PATCH request-body
  content and exercises every advertised implemented format. It does not treat `application/json`,
  `application/geo+json`, or `application/sml+json` as patch-document media
  types. No negotiable implemented format is a precise no-evidence SKIP.
- **Sprint 57 positive evidence**: PASS requires a follow-up canonical GET
  proving the intended field changed, an unpatched sentinel and external
  identity remained stable, and the deliberately conflicting submitted `id`
  was ignored. Custom item PATCH additionally requires canonical and occurrence
  propagation in one observation. HTTP 202 uses one monotonic deadline for all
  required postconditions, request timeouts, and sleeps; status alone, late
  success, or accumulated observations do not PASS.
- **Sprint 57 ownership and completion**: Every write remains behind
  `mutation-tests-enabled=true` and
  `mutation-iut-policy=dedicated-mutable-iut`, with known shared public targets
  denied. Cleanup is registered by identity before creation, runs in reverse
  order after every outcome, and requires same-origin identity proof before
  destructive follow-up. Mappings remain candidate until positive real-IUT
  PATCH E2E executes; an honest local OSH prerequisite SKIP is mandatory
  runtime evidence but cannot promote the mappings.
- **Sprint 57 Raze remediation**: Candidate `b9143a4` is superseded by Raze
  `GAPS_FOUND 0.99`. Fixture POST denial or unusable response is inconclusive
  Update evidence and SHALL SKIP before PATCH, while every dispatched POST
  remains an ambiguous ownership event requiring bounded identity
  rediscovery. Cleanup SHALL revalidate current identity immediately before
  DELETE and prove disappearance after every accepted synchronous or queued
  DELETE status. Custom collection applicability accepts only exact compact or
  canonical SOSA types. All repeated `Allow` fields are evaluated. Synchronous
  and queued custom propagation SHALL remain jointly complete for two
  consecutive observations.
- **Sprint 57 implementation evidence**: Candidate `cbfa070` was superseded by
  Raze `GAPS_FOUND 0.98` for delayed/custom-only ambiguous commit cleanup,
  independent route attempts, and endpoint-specific sentinel baselines.
  Candidate `9e839e1` was superseded by Raze `GAPS_FOUND 0.97` for GeoJSON
  `features` discovery, route-failure suppression, and unsafe accepted fixture
  responses. Six final requirement-linked regressions reproduced those defects
  at `28/6/0/0`; controlled HTTP now passes `28/0/0/0`.
- **Sprint 57 exact candidate evidence**: Detached candidate
  `c4b6030b6931863ccda484f2f2d3468cb045d79f` passes Docker Maven
  `685/0/0/3`, released ATS and coverage audits, image/runtime, deployed SWE
  Common adapter, dependency sabotage, credential, TeamEngine immutable-base,
  and artifact-hygiene gates. Exact image
  `sha256:6861fefdab9c3150ffe2c9732af73e6274a011d4e10e2b4c48088a4bb291c6cb`
  runs against unmodified local OSH with populated `244/54/35/155` and clean
  primary `244/40/7/197`. Provisioning and cleanup pass, primary state is
  unchanged, and all 363 IUT requests are GETs. API Common datetime SKIPs,
  causally skipping all five Update procedures before writes. This verifies
  implementation and E2E honesty but is not positive PATCH evidence; all five
  mappings remain candidate.
- **Sprint 57 final Raze remediation**: Candidate `c4b6030` is superseded by
  Raze `GAPS_FOUND 0.98`. Canonical Sampling Feature fixture acquisition SHALL
  own a parent System and POST through the required
  `/systems/{systemId}/samplingFeatures` endpoint; optional root
  `/samplingFeatures` creation cannot gate Update evidence. Reverse cleanup
  SHALL delete the Sampling Feature before its parent System. For an ambiguous
  custom POST, canonical-first visibility SHALL NOT end identity discovery:
  canonical and custom views SHALL continue independently through the shared
  bounded deadline until both are visible or time expires. Safe discovered or
  derived routes SHALL then be cleaned with aggregated failures.
- **Sprint 57 exact replacement evidence**: Detached candidate
  `40cc7039da26a39424f1ffa7626b7b6926a50f0a` reproduces both final-Raze
  defects at `2/2/0/0`, then passes focused `2/0/0/0`, complete Update
  `30/0/0/0`, and full Docker Maven `687/0/0/3`. Released-source, coverage,
  exact-image runtime, deployed SWE Common adapter, dependency sabotage,
  credential, TeamEngine immutable-base, and artifact-hygiene gates pass.
  Exact image
  `sha256:8184ad80b160e0854afcf22d5fa996de835bd886c31930bae150a1bb4cb7ee9d`
  runs against unmodified local OSH with populated `244/54/35/155` and clean
  primary `244/40/7/197`; provisioning and cleanup pass, primary state is
  unchanged, and 363 IUT requests are GETs with zero writes. API Common
  datetime causally skips all five Update procedures before writes. At gate
  capture, fresh Raze had not yet run; positive PATCH lifecycle evidence
  remains pending and all five mappings remain candidate.
- **Sprint 57 fresh Raze**: `APPROVE_WITH_CONCERNS` at high confidence closes
  both prior HIGH findings and identifies no implementation defect. Its sole
  MEDIUM concern is exact-evidence reconciliation; the follow-up binds all
  exact results to `40cc703` while preserving candidate mappings, non-green
  local-OSH totals, and the external positive-PATCH limitation. Focused Raze
  recheck returns `APPROVE_WITH_CONCERNS` at confidence `0.98`, closes the
  MEDIUM concern, and requires no fixes.
- **Maps to**: PRD FR-ETS-21.

### Acceptance Scenarios for Sprint 57

#### SCENARIO-ETS-PART1-011-RELEASED-PROCEDURES-001 (CRITICAL)
**GIVEN** released OGC 23-001 Annex A defines five `/conf/update` procedures
**WHEN** TestNG loads `UpdateTests`
**THEN** exactly five independent methods map System, Deployment, Procedure,
Sampling Feature, and Property
**AND** no declaration, readiness, or dependency tracer method is counted as a
released procedure.
*Maps to*: REQ-ETS-PART1-011, REQ-ETS-COVERAGE-001.

#### SCENARIO-ETS-PART1-011-DIRECT-PREREQUISITES-001 (CRITICAL)
**GIVEN** an Update procedure begins
**WHEN** it reads `/conformance`
**THEN** it requires Part 1 `/conf/update`, direct `/conf/api-common`, exact
`http://www.opengis.net/spec/ogcapi-4/1.0/conf/update`, and its resource
condition
**AND** `ogcapi-features-4` or Create/Replace/Delete declaration alone cannot
satisfy those checks.
*Maps to*: REQ-ETS-PART1-011.

#### SCENARIO-ETS-PART1-011-DEPENDENCY-CAUSAL-001 (CRITICAL)
**GIVEN** the Part 1 API Common TestNG prerequisite fails or unexpectedly skips
**WHEN** the Update group is scheduled
**THEN** all five procedures dependency-SKIP before IUT access
**AND** System or Create/Replace/Delete group outcomes cannot block them.
*Maps to*: REQ-ETS-PART1-011.

#### SCENARIO-ETS-PART1-011-MUTATION-SAFETY-001 (CRITICAL)
**GIVEN** either mutation opt-in value is absent or the IUT is a known shared
public target
**WHEN** any Update procedure starts
**THEN** it SKIPs before POST, PATCH, or DELETE
**AND** reports the denied ownership precondition.
*Maps to*: REQ-ETS-PART1-011.

#### SCENARIO-ETS-PART1-011-PATCH-NEGOTIATION-001 (CRITICAL)
**GIVEN** an owned JSON resource and a successful OPTIONS response
**WHEN** the ETS evaluates Update support
**THEN** `Allow` contains PATCH
**AND** the ETS uses every implemented JSON Merge Patch or JSON Patch format
advertised by `Accept-Patch` or exact OpenAPI PATCH request-body metadata
**OR** it SKIPs precisely when no implemented patch format can be negotiated
**AND** it never guesses an ordinary resource media type.
*Maps to*: REQ-ETS-PART1-011.

#### SCENARIO-ETS-PART1-011-PARTIAL-UPDATE-001 (CRITICAL)
**GIVEN** a schema-valid owned resource with a stable identity and sentinel
**WHEN** PATCH returns HTTP 200, 202, or 204
**THEN** canonical GET evidence proves the requested field changed
**AND** the sentinel and external identity remain unchanged
**AND** a conflicting `id` in the patch document was ignored.
*Maps to*: REQ-ETS-PART1-011.

#### SCENARIO-ETS-PART1-011-CUSTOM-COLLECTIONS-001 (CRITICAL)
**GIVEN** the IUT advertises an applicable non-root collection
**WHEN** bounded cycle-safe same-origin pagination discovers it and the
corresponding procedure patches its owned collection item
**THEN** both the custom item and canonical resource expose the completed
partial update in the same observation
**AND** the completed state remains jointly true for two consecutive complete
observations
**AND** each endpoint preserves the untouched sentinel from its own pre-update
representation.
*Maps to*: REQ-ETS-PART1-011.

#### SCENARIO-ETS-PART1-011-APPLICABILITY-EXACT-001 (CRITICAL)
**GIVEN** a non-root collection advertises an item or feature type
**WHEN** the Update procedure evaluates collection applicability
**THEN** only the exact compact or canonical SOSA type for that procedure is
accepted
**AND** an unrelated namespace sharing the same local-name suffix receives no
write.
*Maps to*: REQ-ETS-PART1-011.

#### SCENARIO-ETS-PART1-011-ASYNC-DEADLINE-001 (CRITICAL)
**GIVEN** PATCH returns HTTP 202
**WHEN** the ETS polls completed-update postconditions
**THEN** one monotonic deadline bounds every required observation, request
timeout, and sleep
**AND** late success or interrupted polling cannot PASS.
*Maps to*: REQ-ETS-PART1-011.

#### SCENARIO-ETS-PART1-011-CLEANUP-001 (CRITICAL)
**GIVEN** a procedure acquires temporary resources
**WHEN** it passes, fails, or becomes inconclusive
**THEN** reverse-order cleanup removes only same-origin identity-verified owned
resources
**AND** identity is revalidated immediately before DELETE
**AND** every accepted DELETE status is followed by bounded disappearance proof
**AND** failure on one safe cleanup route does not suppress attempts on the
remaining safe routes
**AND** all cleanup-route failures are reported after those attempts
**AND** canonical-first visibility does not stop bounded polling for a delayed
custom occurrence
**AND** cleanup failure remains visible and overrides inconclusive SKIP.
*Maps to*: REQ-ETS-PART1-011.

#### SCENARIO-ETS-PART1-011-FIXTURE-ACQUISITION-001 (CRITICAL)
**GIVEN** a procedure has passed declaration, condition, and mutation gates
**WHEN** its owned fixture POST is denied or returns an unusable or ambiguous
response
**THEN** the Update procedure becomes inconclusive and SKIPs before PATCH
**AND** canonical Sampling Feature acquisition first owns a parent System and
uses `/systems/{systemId}/samplingFeatures`, never optional root creation
**AND** reverse cleanup removes the Sampling Feature before its parent System
**AND** every dispatched POST is followed by bounded polling of both canonical
and applicable custom collection identity views
**AND** identity polling reads released GeoJSON `features` collections as well
as JSON or SensorML `items` collections
**AND** failure of one discovery view cannot suppress polling, derivation, or
cleanup through another independently safe view
**AND** delayed canonical-first/custom-later, canonical-only, custom-only, or
jointly visible committed resources are cleaned despite the response failure.
*Maps to*: REQ-ETS-PART1-011.

#### SCENARIO-ETS-PART1-011-DIRECT-HTTP-COVERAGE-001 (CRITICAL)
**GIVEN** a controlled stateful HTTP IUT implements the negotiated operation
**WHEN** the five support procedures execute
**THEN** every canonical and advertised custom path runs through OPTIONS,
owned acquisition, PATCH, completed representation proof, and cleanup
**AND** malformed declarations, formats, responses, and postconditions fail or
skip closed as specified.
*Maps to*: REQ-ETS-PART1-011.

#### SCENARIO-ETS-PART1-011-E2E-ISOLATION-001 (CRITICAL)
**GIVEN** the exact committed candidate runs through Dockerized TeamEngine
against unmodified local OSH
**WHEN** the IUT lacks an inherited prerequisite or Update declaration
**THEN** the result records an honest causal SKIP and zero unauthorized writes
**AND** positive isolated PATCH evidence remains required before exact mapping
promotion.
*Maps to*: REQ-ETS-PART1-011, REQ-ETS-COVERAGE-001.

### Acceptance Scenarios for Sprint 13

#### SCENARIO-ETS-PART1-011-UPDATE-CONFORMANCE-DECLARED-001 (CRITICAL)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**WHEN** the Update suite reads `/conformance`
**THEN** the response contains `/conf/update`
**OR IF** `/conf/update` is absent
**THEN** every Update @Test SKIPs with reason citing the missing conformance declaration.
*Maps to*: REQ-ETS-PART1-011.

#### SCENARIO-ETS-PART1-011-UPDATE-MUTATION-SAFETY-GATE-001 (CRITICAL)
**GIVEN** the suite parameters do not include `mutation-tests-enabled=true` and `mutation-iut-policy=dedicated-mutable-iut`
**WHEN** the Update lifecycle assertion starts
**THEN** it SKIPs before issuing PATCH
**AND** the skip reason names the missing explicit mutation opt-in.
*Maps to*: REQ-ETS-PART1-011.

#### SCENARIO-ETS-PART1-011-UPDATE-SYSTEM-RESOURCE-OPTIONS-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/update` and a System resource id is available
**WHEN** the ETS sends `OPTIONS /systems/{id}`
**THEN** the ETS records whether `Allow` includes PATCH
**AND** does not issue PATCH
**AND** any PASS is reported only as an ETS readiness precondition, not as OGC update lifecycle conformance.
*Maps to*: REQ-ETS-PART1-011.

#### SCENARIO-ETS-PART1-011-UPDATE-SYSTEM-PATCH-LIFECYCLE-OPTIN-001 (CRITICAL)
**GIVEN** `mutation-tests-enabled=true`
**AND** `mutation-iut-policy=dedicated-mutable-iut`
**AND** the IUT is not a known shared public GeoRobotix URL
**AND** the IUT declares `/conf/update`
**AND** `OPTIONS /systems/{id}` advertises PATCH
**WHEN** the systems PATCH lifecycle assertion runs
**THEN** it MAY issue PATCH against a temporary System resource
**AND** verifies the PATCH result by GET
**AND** performs best-effort cleanup.
*Maps to*: REQ-ETS-PART1-011.

#### SCENARIO-ETS-PART1-011-UPDATE-DEPENDENCY-SMOKE-001 (CRITICAL)
**GIVEN** `testng.xml` includes the Update group
**WHEN** TestNG loads the suite
**THEN** `update` depends on `createreplacedelete`
**AND** the Update class is co-located after Create/Replace/Delete in the same TestNG execution block.
*Maps to*: REQ-ETS-PART1-011.

#### SCENARIO-ETS-PART1-011-UPDATE-SMOKE-NO-PATCH-001 (CRITICAL)
**GIVEN** default smoke runs against GeoRobotix
**WHEN** `scripts/smoke-test.sh` validates the TeamEngine log
**THEN** it recognizes at least one IUT-bound REST Assured request entry
**AND** finds zero IUT-bound PATCH entries
**AND** still excludes the TeamEngine control-plane POST.
*Maps to*: REQ-ETS-PART1-011, REQ-ETS-TEAMENGINE-005.

#### SCENARIO-ETS-PART1-011-UPDATE-SYSTEM-PATCH-CHANGED-FIELD-001 (CRITICAL)
**GIVEN** mutation tests are explicitly enabled against a dedicated mutable IUT
**AND** the IUT declares `/conf/update`
**AND** `OPTIONS /systems/{id}` advertises PATCH
**WHEN** the systems PATCH lifecycle assertion issues PATCH against a temporary System resource
**THEN** a follow-up GET returns the temporary System
**AND** the ETS asserts the patched field value, initially `properties.name`, equals the intended new value
**AND** a PATCH status code without changed representation evidence does not PASS the lifecycle assertion.
*Maps to*: REQ-ETS-PART1-011.

#### SCENARIO-ETS-PART1-011-UPDATE-LOCAL-OSH-READINESS-001 (CRITICAL)
**GIVEN** the seeded local OSH mutable fixture is reachable at `http://field-hub-osh-1:8081/sensorhub/api`
**WHEN** Sprint 14 probes `/conformance` and `OPTIONS /systems/040g`
**THEN** the ETS records whether `/conf/update` is declared and whether PATCH is advertised
**AND** if either precondition is absent, no PATCH is issued and the result is recorded as honest readiness SKIP evidence.
*Maps to*: REQ-ETS-PART1-011.

#### SCENARIO-ETS-PART1-011-UPDATE-OPTIONS-PATCH-SKIP-SEMANTICS-001 (CRITICAL)
**GIVEN** mutation tests are explicitly enabled against a non-public IUT
**AND** the IUT declares `/conf/update`
**AND** `OPTIONS /systems/{id}` succeeds but does not advertise PATCH
**WHEN** the systems PATCH lifecycle assertion reaches the readiness check
**THEN** the readiness assertion FAILs for `/req/update/system`
**AND** the lifecycle assertion SKIPs before PATCH because the readiness precondition failed
**AND** no PATCH is issued.
*Maps to*: REQ-ETS-PART1-011.

### Acceptance Scenarios for Sprint 12

#### SCENARIO-ETS-PART1-010-CRD-CONFORMANCE-DECLARED-001 (CRITICAL)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**WHEN** the Create/Replace/Delete suite reads `/conformance`
**THEN** the response contains `/conf/create-replace-delete`
**OR IF** `/conf/create-replace-delete` is absent
**THEN** every Create/Replace/Delete @Test SKIPs with reason citing the missing conformance declaration.
*Maps to*: REQ-ETS-PART1-010.

#### SCENARIO-ETS-PART1-010-CRD-MUTATION-SAFETY-GATE-001 (CRITICAL)
**GIVEN** the suite is running with default smoke parameters
**WHEN** any Create/Replace/Delete lifecycle assertion would issue POST, PUT, or DELETE
**THEN** the assertion SKIPs before issuing the mutating request
**AND** the SKIP reason names the missing explicit mutation opt-in parameter
**AND** default smoke logs contain zero IUT-bound POST, PUT, or DELETE request-log entries, using recognized REST Assured `Request: METHOD URI` or adjacent `Request method:` / `Request URI:` entries filtered to the IUT base URL.
*Maps to*: REQ-ETS-PART1-010.

#### SCENARIO-ETS-PART1-010-CRD-SYSTEMS-OPTIONS-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/create-replace-delete`
**WHEN** the suite sends `OPTIONS /systems`
**THEN** the response is HTTP 200 or 204
**AND** the `Allow` header advertises POST
**AND** the suite does not issue POST in this assertion
**AND** the result is reported as ETS readiness evidence, not as lifecycle conformance for `/req/create-replace-delete/system`.
*Maps to*: REQ-ETS-PART1-010.

#### SCENARIO-ETS-PART1-010-CRD-SYSTEM-RESOURCE-OPTIONS-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/create-replace-delete`
**AND** a seed System id can be selected from `/systems?limit=1`
**WHEN** the suite sends `OPTIONS /systems/{id}`
**THEN** the response is HTTP 200 or 204
**AND** the `Allow` header advertises PUT and DELETE
**AND** the suite does not issue PUT or DELETE in this assertion
**AND** the result is reported as ETS readiness evidence, not as lifecycle conformance for `/req/create-replace-delete/system`.
*Maps to*: REQ-ETS-PART1-010.

#### SCENARIO-ETS-PART1-010-CRD-SYSTEM-LIFECYCLE-OPTIN-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/create-replace-delete`
**WHEN** mutation tests are not explicitly enabled for a dedicated mutable IUT
**THEN** the systems lifecycle create/replace/delete assertion SKIPs with reason before POST
**OR IF** mutation tests are explicitly enabled under the Sprint 12 safety contract
**THEN** the suite first hard-denies known shared public GeoRobotix URLs
**AND IF** the target is a dedicated mutable IUT
**THEN** the suite creates a System with POST, replaces it with PUT, deletes it with DELETE, verifies expected status codes and canonical id behavior, and records best-effort cleanup evidence.
*Maps to*: REQ-ETS-PART1-010.

#### SCENARIO-ETS-PART1-010-CRD-DEPENDENCY-SMOKE-001 (CRITICAL)
**GIVEN** the SystemFeatures group fails or is sabotaged
**WHEN** the Create/Replace/Delete suite attempts to run
**THEN** Create/Replace/Delete tests SKIP because `<group name="createreplacedelete" depends-on="systemfeatures"/>` is present
**AND** this dependency behavior is evidenced by structural lint and/or targeted sabotage/runtime cascade checks.
*Maps to*: REQ-ETS-PART1-010.

#### SCENARIO-ETS-PART1-010-CRD-SMOKE-NO-MUTATION-001 (CRITICAL)
**GIVEN** Sprint 12 adds the Create/Replace/Delete safety-gated systems subset
**WHEN** `scripts/smoke-test.sh` runs from a `/tmp` clone against an explicitly selected advisory GeoRobotix target
**THEN** failed=0
**AND** total PASS+SKIP increases by the number of new Create/Replace/Delete @Tests
**AND** default smoke logs contain zero IUT-bound POST, PUT, or DELETE request-log entries from the Create/Replace/Delete suite, using recognized REST Assured `Request: METHOD URI` or adjacent `Request method:` / `Request URI:` entries filtered to the IUT base URL.
*Maps to*: REQ-ETS-PART1-010.

### Acceptance Scenarios for Sprint 56

#### SCENARIO-ETS-PART1-010-RELEASED-PROCEDURES-001 (CRITICAL)
**GIVEN** released OGC 23-001 Annex A defines twelve
`/conf/create-replace-delete` procedures
**WHEN** the deployed TestNG class is inspected
**THEN** exactly twelve TestNG methods exist
**AND** each method identifies exactly one released target and has no
method-to-method dependency
**AND** no method uses `alwaysRun` to bypass the TestNG group prerequisite.

#### SCENARIO-ETS-PART1-010-DIRECT-PREREQUISITES-001 (CRITICAL)
**GIVEN** the released class directly inherits Part 1 API Common and OGC API
Features Part 4 Create/Replace/Delete
**WHEN** a procedure starts
**THEN** it checks `/conf/api-common`, the exact released Annex A
`http://www.opengis.net/spec/ogcapi-4/1.0/conf/create-replace-delete`
inheritance URI, and its own applicable resource condition
**AND** the draft
`http://www.opengis.net/spec/ogcapi-features-4/1.0/conf/create-replace-delete`
near-match cannot satisfy the released inheritance check
**AND** System or another sibling resource configuration cannot block the
entire class.

#### SCENARIO-ETS-PART1-010-DEPENDENCY-CAUSAL-001 (CRITICAL)
**GIVEN** the `part1apicommon` TestNG group fails
**WHEN** TestNG evaluates the Create/Replace/Delete group dependency
**THEN** all twelve released procedures SKIP before entering procedure code
**AND** the IUT receives no read or write request from those procedures
**AND** a passing prerequisite leaves the twelve sibling methods mutually
independent.

#### SCENARIO-ETS-PART1-010-MUTATION-SAFETY-001 (CRITICAL)
**GIVEN** either explicit mutation parameter is absent, the target is a known
shared public IUT, or the target is the clean primary local OSH
**WHEN** any released procedure reaches its first write
**THEN** it SKIPs before POST, PUT, or DELETE
**AND** default TeamEngine evidence contains zero IUT-bound writes.

#### SCENARIO-ETS-PART1-010-INHERITED-TRANSACTION-001 (CRITICAL)
**GIVEN** an applicable mutable resource endpoint and supported representation
**WHEN** the reusable transaction procedure executes
**THEN** OPTIONS is HTTP 200 and advertises the relevant method
**AND** POST is HTTP 201 with usable Location or HTTP 202, with canonical GET
proving submitted content
**AND** PUT is HTTP 200, 202, or 204, with a later GET proving replacement
content
**AND** DELETE is HTTP 200, 202, or 204, with a later GET proving absence
**AND** a 202 response starts bounded polling configured by ETS runtime
properties and is accepted-but-inconclusive until the required postcondition
is observed
**AND** timeout without a postcondition SKIPs rather than reporting a positive
lifecycle result
**AND** one monotonic deadline covers every postcondition of one queued
operation, checks expiry before each probe, caps HTTP and sleep time to the
remaining bound, rejects late success, and fails visibly on interruption
**AND** generic non-System DELETE requests omit the System-specific `cascade`
query parameter
**AND** status-only or OPTIONS-only evidence cannot PASS the lifecycle.

#### SCENARIO-ETS-PART1-010-REPRESENTATION-CLOSURE-001 (CRITICAL)
**GIVEN** the IUT declares an encoding applicable to a released resource type
**WHEN** its direct or nested transaction procedure runs
**THEN** each applicable declared representation is exercised with its exact
Content-Type and Accept media type
**AND** every ETS-generated request representation validates against the
bundled released resource schema before it can be sent
**AND** server-added identifiers and links do not excuse a changed or missing
submitted field.

#### SCENARIO-ETS-PART1-010-CASCADE-001 (CRITICAL)
**GIVEN** one temporary System has nested resources and another is one of
multiple Systems referenced by a temporary Deployment
**WHEN** each graph is deleted first with `cascade=false` and then with
`cascade=true`
**THEN** a pre-delete Deployment GET proves both System references are present
**AND** the false request returns exactly HTTP 409 and leaves the graph intact
**AND** the true request removes the target System and required nested
resources
**AND** the Deployment graph remains while removing only the deleted System
reference.

#### SCENARIO-ETS-PART1-010-NESTED-CANONICAL-001 (CRITICAL)
**GIVEN** a subsystem, subdeployment, or Sampling Feature is created through
its prescribed parent-scoped endpoint
**WHEN** the server returns its canonical Location
**THEN** the ETS derives the released root canonical endpoint from the returned
local identifier rather than trusting a nested `Location`
**AND** canonical GET is HTTP 200 and preserves submitted content
**AND** replacement and deletion use the released canonical endpoint where
required.

#### SCENARIO-ETS-PART1-010-CUSTOM-CREATE-001 (CRITICAL)
**GIVEN** the IUT advertises an applicable custom collection
**WHEN** a resource is created through `/collections/{colId}/items`
**THEN** the returned custom item and its canonical root URL are both readable
with equivalent submitted content.

#### SCENARIO-ETS-PART1-010-CUSTOM-REPLACE-001 (CRITICAL)
**GIVEN** a temporary resource is present in an advertised applicable custom
collection
**WHEN** it is replaced through `/collections/{colId}/items/{id}`
**THEN** both custom-item and canonical GET prove the replacement content.

#### SCENARIO-ETS-PART1-010-CUSTOM-DELETE-001 (CRITICAL)
**GIVEN** a temporary resource is present in both root and non-root collections
**WHEN** it is deleted from the root
**THEN** both canonical and custom-item URLs become unavailable
**AND WHEN** a separate resource is deleted only from the non-root collection
**THEN** its custom-item URL becomes unavailable while its canonical root
representation remains readable.

#### SCENARIO-ETS-PART1-010-CUSTOM-URI-LIST-001 (CRITICAL)
**GIVEN** same-IUT canonical resources compatible with an advertised custom
collection
**WHEN** their canonical URLs or UIDs are POSTed one-per-line with
`Content-Type: text/uri-list`
**THEN** collection OPTIONS advertises POST, and association POST returns HTTP
201 with a same-origin usable Location or queued HTTP 202
**AND** a 202 response is positive only after bounded polling observes the
returned or computed collection occurrence
**AND** each computed collection-item URL is HTTP 200 and equivalent to the
canonical representation
**AND** identity-safe occurrence cleanup is registered before POST so a
late-materializing accepted association is removed after an inconclusive
timeout.

#### SCENARIO-ETS-PART1-010-CLEANUP-001 (CRITICAL)
**GIVEN** a released procedure creates one or more resources
**WHEN** it passes, fails, or skips after creation
**THEN** cleanup runs in reverse ownership order
**AND** no response Location is registered for destructive cleanup until GET
proves the submitted UID or URI identity
**AND** missing or incorrect Location metadata triggers same-origin root
discovery and cleanup by submitted identity without deleting an unrelated
resource
**AND** cleanup continues with bounded root identity discovery after deleting
a verified alias Location so a surviving canonical resource cannot be hidden
**AND** accepted queued creation is polled by submitted identity during both
positive verification and cleanup
**AND** queued occurrence cleanup SHALL delete a returned or computed
collection item only after a bounded GET proves the submitted identity and
content; mere availability, a mismatched representation, or an unverified
direct `Location` SHALL never authorize DELETE
**AND** cleanup failures override an accepted-but-inconclusive SKIP and are
reported rather than hidden by an earlier outcome.

#### SCENARIO-ETS-PART1-010-ASYNC-DEADLINE-001 (CRITICAL)
**GIVEN** a queued operation and positive timeout and polling properties
**WHEN** a postcondition blocks, appears after expiry, the polling interval
exceeds remaining time, or the waiting thread is interrupted
**THEN** one monotonic deadline caps every HTTP connect/read timeout and sleep
**AND** the deadline is checked at every first-page, pagination-page, and
candidate-resource requester boundary
**AND** a request timeout is never rounded beyond the remaining whole
millisecond and no probe begins when less than one millisecond remains
**AND** late success does not become positive evidence
**AND** interruption fails visibly while preserving interrupt status.

#### SCENARIO-ETS-PART1-010-ASYNC-COMPOUND-001 (CRITICAL)
**GIVEN** a queued cascade or custom-collection mutation has multiple required
postconditions
**WHEN** propagation completes in stages
**THEN** every required canonical, custom, cascade, and surviving-association
postcondition is polled under the same operation deadline
**AND** all postconditions for one mutation SHALL be true in the same polling
observation before positive evidence is reported
**AND** an early custom replacement, disappearance, or cascade state that
reverts before the remaining postconditions become true SHALL not PASS
**AND** a queued custom create used as replace/delete setup awaits and
pre-registers cleanup for its collection occurrence before the later write
**AND** no single early postcondition can cause a false PASS or false FAIL.

#### SCENARIO-ETS-PART1-010-INCONCLUSIVE-CLEANUP-001 (CRITICAL)
**GIVEN** a queued operation times out as accepted-but-inconclusive
**WHEN** owned-resource cleanup also fails
**THEN** the cleanup failure overrides the SKIP and the TestNG outcome is FAIL.

#### SCENARIO-ETS-PART1-010-CUSTOM-URI-LIST-LATE-CLEANUP-001 (CRITICAL)
**GIVEN** a queued `text/uri-list` association is accepted but materializes
after its positive-evidence deadline
**WHEN** procedure cleanup runs
**THEN** pre-registered identity-safe occurrence cleanup polls and removes the
late association
**AND** a supplied HTTP 202 Location inside the target collection-item
namespace is separately verified and cleaned as an occurrence
**AND** cleanup of that returned occurrence requires submitted-content proof
and leaves an existing mismatched direct collection item untouched
**AND** a supplied HTTP 202 Location outside that namespace is treated only as
an asynchronous status URI and is never dereferenced or destructively cleaned
**AND** this status-only treatment includes a syntactically valid absolute
cross-origin Location, which receives no GET or DELETE while the computed
same-origin occurrence is polled
**AND** the canonical resource remains governed by its separate ownership
cleanup.

#### SCENARIO-ETS-PART1-010-DIRECT-HTTP-COVERAGE-001 (CRITICAL)
**GIVEN** a controlled stateful HTTP endpoint implements the released behavior
**WHEN** all twelve direct Java methods execute
**THEN** every positive procedure completes
**AND** focused regressions reject failed prerequisite entry, wrong cascade
status, absent initial cascade associations, missing or unrelated Location,
unchanged replacement content, incorrect collection propagation, generic
non-System cascade parameters, incomplete URI-list responses, queued
POST/PUT/DELETE postconditions, hard deadline/interrupt behavior, compound
propagation, late URI-list materialization, alias-Location canonical leakage,
and hidden cleanup failure.

#### SCENARIO-ETS-PART1-010-E2E-ISOLATION-001 (CRITICAL)
**GIVEN** the exact committed candidate and the Sprint 44 owned isolated local
OSH workflow
**WHEN** mutation-enabled TeamEngine E2E runs
**THEN** all twelve methods deploy and produce honest PASS, FAIL, or SKIP
outcomes
**AND** owned state is cleaned, the primary OSH fingerprint remains unchanged,
and subsequent clean-primary TeamEngine smoke records zero writes without
introducing failures beyond the documented primary-IUT baseline
**AND** prerequisite SKIPs caused by an undeclared exact inherited URI do not
constitute positive lifecycle evidence or complete the released closure.

> Sprint 9 starts the remaining encoding classes with GeoJSON only. This is intentionally narrower than the v1.0 web-app story that paired GeoJSON + SensorML: GeoJSON is read-only, declared by GeoRobotix, and reuses existing Feature/FeatureCollection validation patterns, while SensorML has broader SensorML 3.0 schema inheritance and remains deferred.

#### REQ-ETS-PART1-012: GeoJSON Encoding Conformance Class (`/conf/geojson`) (Sprint 9 target)
- **Priority**: MUST
- **Status**: IMPLEMENTED_RELEASED_ATS (Sprint 54 direct released ATS closure; Raze approved)
- **Historical increment**: (Sprint 9 Generator 2026-05-05; Sprint 15 non-system read-only expansion Generator 2026-05-06; Sprint 17 selected-resource relation-types Generator 2026-05-07; Sprint 18 relation-types breadth Generator 2026-05-07; Sprint 19 mediatype-write safety-gated Generator 2026-05-07; stories S-ETS-09-01, S-ETS-15-01, S-ETS-17-01, S-ETS-18-01, and S-ETS-19-01). Sprint 19 adds safety-gated `/req/geojson/mediatype-write` checks with positive system-resource evidence against a dedicated local OSH mutable IUT. Sprint 18 added independent GeoJSON relation-types checks for selected System, Deployment, Procedure, and Sampling Feature items. Sprint 9 closed the systems read-only subset. Sprint 15 adds deployment/procedure/sampling-feature read-only schema and mapping checks with fallback honesty when an IUT returns default CS API `items` wrappers. Full REQ-ETS-PART1-012 remains open until broader positive relation-types evidence where IUT resources expose association links, property GeoJSON mapping, non-system mutation-side encoding coverage, and full schema-validation closure are implemented.
- **OGC source verified**: Upstream master commit `3fd86c73e744b7e2faaf7f1c17366bfb9ff4cd6f` dated 2026-04-20. Requirement class file exists at `api/part1/standard/requirements/encoding/geojson/requirements_class_geojson.adoc`. The class identifier is `/req/geojson`, inherits `/req/api-common` and OGC API Features 1.0 GeoJSON, and lists 12 subrequirements: `mediatype-read`, `mediatype-write`, `relation-types`, `feature-attribute-mapping`, `system-schema`, `system-mappings`, `deployment-schema`, `deployment-mappings`, `procedure-schema`, `procedure-mappings`, `sf-schema`, and `sf-mappings`.
- **Sprint 9 coverage scope**: Sprint-1-style minimal systems read-only subset with 5 @Tests: (1) IUT declares `/conf/geojson`; (2) `Accept: application/geo+json` or default JSON response for `/systems` returns HTTP 200 + honest media-type/fallback reporting; (3) `/systems` GeoJSON path requires `type="FeatureCollection"` and a `features` array; (4) first system feature carries GeoJSON `Feature` shape with `id`, `type`, `geometry`, and `properties`; (5) TestNG dependency wiring and smoke no-regression.
- **Sprint 15 coverage scope**: `GeoJsonTests` now adds 3 non-system read-only @Tests for `/deployments`, `/procedures`, and `/samplingFeatures` with `Accept: application/geo+json`. Each test requires GeoJSON `FeatureCollection` + `features` before PASS, rejects CS API `items` wrappers as SKIP fallback evidence, and requires resource-specific mapping evidence: deployment `properties.uid` plus non-empty `properties.deployedSystems@link`, procedure `geometry == null` plus `properties.uid` and `properties.featureType`, and sampling feature `properties.uid`, `properties.featureType`, plus non-empty `properties.hostedProcedure@link` or `properties.radius`. `VerifyGeoJsonResourceMappingAssertions` pins helper behavior for fallback SKIP and mapping-value checks.
- **Dependency wiring**: GeoJSON depends on SystemFeatures via `<group name="geojson" depends-on="systemfeatures"/>`. This keeps encoding validation behind the canonical system feature resource availability already implemented in REQ-ETS-PART1-002.
- **Implementation evidence**: Sprint 9: `bash scripts/mvn-test-via-docker.sh` in the sister repo reports BUILD SUCCESS with surefire `Tests run: 92, Failures: 0, Errors: 0, Skipped: 3`. `scripts/smoke-test.sh` from `/tmp/sprint-ets-09-smoke-fix` reports `total=51 passed=42 failed=0 skipped=9`; GeoJSON contributed 2 PASS and 3 SKIP. Sprint 15: formatter BUILD SUCCESS; `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS with `122 tests / 0 failures / 0 errors / 3 skipped`, log `ops/test-results/sprint-ets-15-maven-2026-05-06.log`; TeamEngine smoke `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s15-generator bash scripts/smoke-test.sh` reported `77 total / 52 passed / 0 failed / 25 skipped` with zero IUT-bound POST/PUT/DELETE/PATCH across 44 recognized IUT request-log entries. Sprint 17: formatter BUILD SUCCESS; Docker Maven BUILD SUCCESS with `133 tests / 0 failures / 0 errors / 3 skipped`, log `ops/test-results/sprint-ets-17-maven-2026-05-06.log`; TeamEngine smoke `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s17-generator bash scripts/smoke-test.sh` reported `82 total / 55 passed / 0 failed / 27 skipped`, with GeoJSON relation-types PASS and zero IUT-bound POST/PUT/DELETE/PATCH across 55 recognized IUT request-log entries. Sprint 18: formatter BUILD SUCCESS; Docker Maven BUILD SUCCESS with `136 tests / 0 failures / 0 errors / 3 skipped`, log `ops/test-results/sprint-ets-18-maven-2026-05-07.log`; TeamEngine smoke `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s18-generator bash scripts/smoke-test.sh` reported `87 total / 55 passed / 0 failed / 32 skipped`, with GeoJSON System relation-types PASS, GeoJSON Deployment/Procedure/SamplingFeature breadth checks SKIP independently, and zero IUT-bound POST/PUT/DELETE/PATCH across 69 recognized IUT request-log entries. Sprint 17 Raze implementation review `.harness/evaluations/sprint-ets-17-adversarial-implementation.yaml` returned `APPROVE` confidence 0.91 with no required fixes.
- **IUT-state policy**: If GeoRobotix does not declare `/conf/geojson`, GeoJSON @Tests SKIP-with-reason rather than FAIL. Current GeoRobotix declares `/conf/geojson`, but `GET /systems` with `Accept: application/geo+json` returns `Content-Type: application/json` and a CS API `items` wrapper, not a GeoJSON `FeatureCollection` with `features`. Therefore `systemsCollectionIsGeoJsonFeatureCollection` SKIPs with reason and `systemFeatureHasGeoJsonShapeAndProperties` SKIPs by dependency; this is fallback evidence, not a GeoJSON FeatureCollection PASS.
- **Sprint 17 implemented relation-types scope**: For associations encoded in a JSON `links` member, relation-types checks require `rel` to equal the association name valid for the selected resource type. Generic `canonical`, `alternate`, pagination, collection, service-desc, and service-doc links are not association evidence. Property-level links such as `deployedSystems@link` and `hostedProcedure@link` remain mapping evidence, not `links` member relation-types evidence. `EncodingRelationTypes` uses resource-specific GeoJSON links-member allowlists derived from the OGC association tables: System (`parentSystem`, `subsystems`, `samplingFeatures`, `deployments`, `procedures`, `datastreams`, `controlstreams`); Deployment (`parentDeployment`, `subdeployments`, `featuresOfInterest`, `samplingFeatures`, `datastreams`, `controlstreams`); Procedure (`implementingSystems`); Sampling Feature (`parentSystem`, `sampleOf`, `datastreams`, `controlstreams`). The runtime GeoJSON assertion currently checks a selected System representation.
- **Sprint 18 implemented relation-types breadth scope**: `GeoJsonTests` now has 12 read-only @Tests and evaluates relation-types independently for selected System, Deployment, Procedure, and Sampling Feature representations. Each assertion PASSes, FAILs, or SKIPs independently so the System PASS cannot hide non-system SKIPs. GeoRobotix runtime on 2026-05-07: System PASSed from `samplingFeatures` and `datastreams`; Deployment and Procedure SKIPped because item `links` members contain only generic `canonical`/`alternate` links; Sampling Feature SKIPped because the selected item has no top-level `links` member. Property-level `deployedSystems@link` and `hostedProcedure@link` remain excluded from relation-types PASS evidence.
- **Sprint 19 implemented mediatype-write scope**: `GeoJsonTests` now has 13 @Tests and adds `geoJsonMediaTypeWriteParsesSystemBodyWhenMutationEnabled`. The test checks write-side `Content-Type: application/geo+json` parsing behind the existing Sprint 12 mutation safety gate, requires `/conf/create-replace-delete`, hard-denies public GeoRobotix, and requires follow-up dereference evidence preserving the submitted UID. GeoRobotix declares `/conf/create-replace-delete` and `/conf/geojson`, and OPTIONS advertises POST/PUT/DELETE, but GeoRobotix is a shared public IUT and was not mutated by default smoke. OPTIONS readiness alone is not conformance evidence. Sprint 19 verification: formatter BUILD SUCCESS; Docker Maven BUILD SUCCESS with `144 tests / 0 failures / 0 errors / 3 skipped`, log `ops/test-results/sprint-ets-19-maven-r3-2026-05-07.log`; GeoRobotix TeamEngine smoke r3 reported `89 total / 55 passed / 0 failed / 34 skipped`, with both mediatype-write tests SKIP-before-mutation and zero IUT-bound POST/PUT/DELETE/PATCH across 69 recognized IUT request-log entries. Authenticated local OSH mutable-IUT smoke r3 reported `89 total / 52 passed / 4 failed / 33 skipped`; the GeoJSON mediatype-write test PASSed with exact `Content-Type=application/geo+json`, follow-up GET, and cleanup DELETE evidence. The four local failures were SensorML non-system HTTP 500 responses outside GeoJSON mediatype-write.
- **Sprint 54 implemented released closure**: CP-014 and S-ETS-54-01 replace the historical 13-method approximation with exactly twelve independent methods matching the twelve released Annex A procedures. Media tests inspect JSON or YAML API-definition operation metadata without mutation. Schema tests independently validate complete canonical single and collection documents. Automated manual-inspection tests process every inspectable feature for common mappings, resource mappings, and relation types. Released inheritance changes from historical System to direct API Common; Core and Common remain transitive. Discovery endpoints may parse valid JSON despite inherited local OSH `Content-Type: auto`, but canonical representation gates still require actual `application/geo+json`. Unsupported actual GeoJSON media, empty resource evidence, or unreadable API-definition evidence SKIPs honestly, while parseable missing advertisements, HTTP defects, invalid schemas, unsafe traversal, and incorrect mappings fail. Eight released entry schemas and 20 transitive schemas pass pinned clean-checkout parity. Sprint 54 also hardens Property parity checkout provenance and adds dedicated Property pagination and later-evidence continuation regressions carried from Sprint 53 Raze. Focused Maven is `45/0/0/0`, full Maven is `548/0/0/3`, coverage is `240/51 exact/2 helper/119 candidate/68 unmapped`, and `/conf/geojson` is `12/12 exact`. Exact-image local OSH TeamEngine is honestly `219/40/7/172`; all twelve GeoJSON procedures SKIP at actual-media or API-definition evidence boundaries and no SKIP is counted as conformance. No OSH or TeamEngine source or binary was modified. Initial Raze findings on documentation state, positive-IUT scope, raw red evidence, and baseline attribution are closed; final Raze is `APPROVE` at confidence `0.99` with no required fixes.
- **Maps to**: PRD FR-ETS-22.

Sprint 54 SHALL retain only immutable API-root setup in `GeoJsonTests`.
Every released procedure SHALL retrieve its own evidence, use `alwaysRun`, and
have no method dependency. `GeoJsonSupport` SHALL own OpenAPI JSON/YAML
inspection, exact endpoint selection, status and actual-media gates, bounded
same-origin pagination, schema dispatch, mapping assertions, and relation
tables. A missing or unreadable API definition is an evidence SKIP; a
parseable API definition that omits required GeoJSON media is a failure. No
GeoJSON procedure SHALL issue POST, PUT, PATCH, or DELETE.

The read-media procedure SHALL require `application/geo+json` in successful
GET response content for every canonical System, Deployment, Procedure, and
Sampling Feature endpoint whose resource class is declared by the IUT, and for
the custom collections items path when custom collections are advertised. The
write-media procedure SHALL require `application/geo+json` request content on
at least one canonical POST or PUT create/replace operation.

Each of the four schema procedures SHALL independently request a canonical
collection and one canonical single resource with `Accept:
application/geo+json`, establish status and actual media before parsing, and
validate complete documents against the corresponding released schemas. The
manual-inspection procedures SHALL process every feature available through
bounded canonical collection traversal. They SHALL validate every present
mapped value but SHALL NOT make optional attributes or associations mandatory
merely to manufacture evidence. Relation-types SHALL aggregate all four
resource classes and SKIP only after complete inspection when no association
relation is present.

### Acceptance Scenarios for Sprint 9

#### SCENARIO-ETS-PART1-012-GEOJSON-CONFORMANCE-DECLARED-001 (CRITICAL)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**WHEN** the GeoJSON suite reads `/conformance`
**THEN** the response contains `/conf/geojson`
**OR IF** `/conf/geojson` is absent
**THEN** every GeoJSON @Test SKIPs with reason citing the missing conformance declaration.
*Maps to*: REQ-ETS-PART1-012.

### Acceptance Scenarios for Sprint 18

> Sprint 18 broadens the Sprint 17 relation-types checks across selected resource classes. It remains read-only and partial: generic-only links or absent `links` members SKIP per resource, not PASS.

#### SCENARIO-ETS-PART1-012-GEOJSON-RELATION-TYPES-BREADTH-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/geojson` and the relevant resource conformance class
**WHEN** the GeoJSON suite inspects selected System, Deployment, Procedure, and Sampling Feature item representations
**THEN** each resource type is evaluated independently against its resource-specific links-member association allowlist
**AND** a PASS for one resource type does not satisfy another resource type's relation-types assertion
**AND** generic-only `links` members or absent `links` members SKIP for that resource type with reason
**AND** property-level `@link` objects are not counted as links-member relation-types evidence.
*Maps to*: REQ-ETS-PART1-012.

### Acceptance Scenarios for Sprint 19

> Sprint 19 implements safety-gated write-side encoding checks. It does not permit default mutation against GeoRobotix and does not close the full GeoJSON or SensorML requirement classes.

#### SCENARIO-ETS-PART1-012-GEOJSON-MEDIATYPE-WRITE-SAFETY-GATED-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/geojson` and `/conf/create-replace-delete`
**WHEN** the GeoJSON mediatype-write assertion starts
**THEN** it reuses the existing mutation safety gate
**AND** it SKIPs before POST or PUT unless `mutation-tests-enabled=true` and `mutation-iut-policy=dedicated-mutable-iut`
**AND** known shared public GeoRobotix URLs are hard-denied even if mutation opt-in is supplied.
*Maps to*: REQ-ETS-PART1-012, REQ-ETS-PART1-010.

#### SCENARIO-ETS-PART1-013-SENSORML-MEDIATYPE-WRITE-SAFETY-GATED-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/sensorml` and `/conf/create-replace-delete`
**WHEN** the SensorML mediatype-write assertion starts
**THEN** it reuses the existing mutation safety gate
**AND** it uses exact `Content-Type: application/sml+json` for positive write parsing evidence
**AND** it SKIPs before POST or PUT unless `mutation-tests-enabled=true` and `mutation-iut-policy=dedicated-mutable-iut`
**AND** known shared public GeoRobotix URLs are hard-denied even if mutation opt-in is supplied.
*Maps to*: REQ-ETS-PART1-013, REQ-ETS-PART1-010.

#### SCENARIO-ETS-PART1-012-013-MEDIATYPE-WRITE-OPTIONS-READINESS-001 (CRITICAL)
**GIVEN** a candidate IUT declares Create/Replace/Delete and an encoding conformance class
**WHEN** the ETS sends `OPTIONS` to a candidate collection or resource endpoint
**THEN** the ETS records advertised POST, PUT, or DELETE readiness without issuing mutation
**AND** the readiness result is not reported as mediatype-write conformance.
*Maps to*: REQ-ETS-PART1-012, REQ-ETS-PART1-013, REQ-ETS-PART1-010.

#### SCENARIO-ETS-PART1-012-013-MEDIATYPE-WRITE-NO-PUBLIC-MUTATION-001 (CRITICAL)
**GIVEN** default smoke runs against GeoRobotix
**WHEN** TeamEngine executes the suite
**THEN** the smoke oracle reports zero IUT-bound POST, PUT, DELETE, or PATCH request-log entries
**AND** GeoJSON/SensorML mediatype-write lifecycle assertions do not mutate the public IUT.
*Maps to*: REQ-ETS-PART1-012, REQ-ETS-PART1-013, REQ-ETS-PART1-010, REQ-ETS-TEAMENGINE-005.

#### SCENARIO-ETS-PART1-012-013-MEDIATYPE-WRITE-PARSE-EVIDENCE-001 (CRITICAL)
**GIVEN** mutation tests are explicitly enabled against a dedicated mutable IUT
**WHEN** the ETS submits a request with `Content-Type: application/geo+json` or `Content-Type: application/sml+json`
**THEN** a PASS requires follow-up dereference evidence from `Location` or canonical id showing the IUT parsed and persisted the submitted resource
**AND** a status code alone does not satisfy mediatype-write conformance.
*Maps to*: REQ-ETS-PART1-012, REQ-ETS-PART1-013, REQ-ETS-PART1-010.

#### SCENARIO-ETS-PART1-012-GEOJSON-MEDIATYPE-READ-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/geojson`
**WHEN** the GeoJSON suite requests `/systems` with `Accept: application/geo+json`
**THEN** the response is HTTP 200 with a GeoJSON-compatible JSON payload
**OR** the suite records a fallback to the default JSON representation when the payload is a valid GeoJSON FeatureCollection but the server does not advertise `application/geo+json`.
*Maps to*: REQ-ETS-PART1-012.

#### SCENARIO-ETS-PART1-012-GEOJSON-FEATURECOLLECTION-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/geojson`
**WHEN** the GeoJSON suite reads `/systems`
**THEN** the response body is a GeoJSON FeatureCollection with `type="FeatureCollection"`
**AND** the response body contains a `features` array with at least one feature candidate
**AND** a response that only exposes a CS API `items` collection wrapper does NOT pass this GeoJSON FeatureCollection assertion; it is recorded as default CS API JSON fallback evidence, SKIP, or CONCERN according to observed IUT behavior.
*Maps to*: REQ-ETS-PART1-012.

#### SCENARIO-ETS-PART1-012-GEOJSON-FEATURE-MAPPING-001 (CRITICAL)
**GIVEN** a first system feature was selected
**WHEN** the GeoJSON suite validates the feature representation
**THEN** the feature has `type="Feature"`, an `id`, a `geometry` member that is either a GeoJSON geometry or null, and a `properties` object
**AND** domain attributes such as `validTime` may appear under `properties` per the Sprint 3 SystemFeatures nested-property precedent.
*Maps to*: REQ-ETS-PART1-012.

#### SCENARIO-ETS-PART1-012-GEOJSON-DEPENDENCY-SMOKE-001 (CRITICAL)
**GIVEN** the SystemFeatures group fails or is sabotaged
**WHEN** the GeoJSON suite attempts to run
**THEN** GeoJSON tests SKIP because `<group name="geojson" depends-on="systemfeatures"/>` is present
**AND** `scripts/smoke-test.sh` from a /tmp clone reports failed=0 and total PASS+SKIP at least 51 (Sprint 8 baseline 46 plus 5 GeoJSON @Tests).
*Maps to*: REQ-ETS-PART1-012.

> Sprint 15 expands GeoJSON read-only coverage beyond systems. It targets deployment, procedure, and sampling feature schema/mapping assertions, but it preserves Sprint 9 fallback honesty: an IUT response with the CS API default `items` wrapper and no GeoJSON `features` array is not GeoJSON PASS evidence.

#### SCENARIO-ETS-PART1-012-GEOJSON-DEPLOYMENT-SCHEMA-MAPPING-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/geojson` and `/conf/deployment`
**WHEN** the GeoJSON suite requests `/deployments` with `Accept: application/geo+json`
**THEN** a PASS requires a GeoJSON FeatureCollection with `type="FeatureCollection"` and a `features` array
**AND** the first feature, if present, has `type="Feature"`, an `id`, a `geometry` member that is either a GeoJSON geometry or null, and a `properties` object
**AND** deployment-specific mapping evidence includes `properties.uid` and a deployment association such as `properties.deployedSystems@link`
**AND** a CS API `items` wrapper without `features` SKIPs with fallback evidence rather than PASSing.
*Maps to*: REQ-ETS-PART1-012.

#### SCENARIO-ETS-PART1-012-GEOJSON-PROCEDURE-SCHEMA-MAPPING-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/geojson` and `/conf/procedure`
**WHEN** the GeoJSON suite requests `/procedures` with `Accept: application/geo+json`
**THEN** a PASS requires a GeoJSON FeatureCollection with `type="FeatureCollection"` and a `features` array
**AND** the first feature, if present, has `type="Feature"`, an `id`, a `geometry` member that is either a GeoJSON geometry or null, and a `properties` object
**AND** procedure-specific mapping evidence includes `geometry == null` plus `properties.uid` and `properties.featureType`
**AND** a CS API `items` wrapper without `features` SKIPs with fallback evidence rather than PASSing.
*Maps to*: REQ-ETS-PART1-012.

#### SCENARIO-ETS-PART1-012-GEOJSON-SF-SCHEMA-MAPPING-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/geojson` and `/conf/sf`
**WHEN** the GeoJSON suite requests `/samplingFeatures` with `Accept: application/geo+json`
**THEN** a PASS requires a GeoJSON FeatureCollection with `type="FeatureCollection"` and a `features` array
**AND** the first feature, if present, has `type="Feature"`, an `id`, a `geometry` member that is either a GeoJSON geometry or null, and a `properties` object
**AND** sampling-feature-specific mapping evidence includes `properties.uid`, `properties.featureType`, and an association or attribute such as `properties.hostedProcedure@link` or `properties.radius`
**AND** a CS API `items` wrapper without `features` SKIPs with fallback evidence rather than PASSing.
*Maps to*: REQ-ETS-PART1-012.

#### SCENARIO-ETS-PART1-012-GEOJSON-NON-SYSTEM-FALLBACK-HONESTY-001 (CRITICAL)
**GIVEN** GeoRobotix currently declares `/conf/geojson`
**WHEN** deployment, procedure, or sampling feature collection requests with `Accept: application/geo+json` return `Content-Type: application/json` and top-level `items`
**THEN** the ETS records that as default CS API JSON fallback evidence
**AND** no schema or mapping assertion for GeoJSON FeatureCollection shape PASSes from that `items` wrapper.
*Maps to*: REQ-ETS-PART1-012.

#### SCENARIO-ETS-PART1-012-GEOJSON-SMOKE-NO-MUTATION-001 (CRITICAL)
**GIVEN** Sprint 15 adds GeoJSON non-system read-only checks
**WHEN** `scripts/smoke-test.sh` runs against an explicitly selected advisory GeoRobotix target
**THEN** failed=0
**AND** the smoke log contains zero IUT-bound POST, PUT, DELETE, or PATCH request-log entries.
*Maps to*: REQ-ETS-PART1-012.

> Sprint 17 implements selected-resource read-only relation-types checks shared by the GeoJSON and SensorML encoding classes. It does not implement write media types, mutation behavior, exhaustive resource-class relation-types traversal, or full schema validation.

#### SCENARIO-ETS-PART1-012-GEOJSON-RELATION-TYPES-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/geojson`
**WHEN** the GeoJSON suite inspects a selected resource representation with a JSON `links` member
**THEN** generic links such as `canonical`, `alternate`, pagination, collection, service-desc, and service-doc are ignored for this assertion
**AND** any association encoded in the `links` member has a `rel` value equal to an association name valid for the selected resource type
**AND** a relation type valid only for a different resource type does not satisfy this assertion
**AND** property-level `@link` members are not counted as `links` member relation-types evidence
**OR IF** no association is encoded in the selected representation's `links` member
**THEN** the assertion SKIPs with reason.
*Maps to*: REQ-ETS-PART1-012.

### Acceptance Scenarios for Sprint 54

#### SCENARIO-ETS-PART1-012-RELEASED-MEDIATYPE-READ-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/geojson`
**AND** its landing page advertises a readable JSON or YAML `service-desc`
**WHEN** the released mediatype-read procedure inspects the API definition
**THEN** every required canonical feature-resource GET operation advertises `application/geo+json` in successful response content
**AND** an advertised custom collections items GET operation does likewise
**AND** a parseable omission fails instead of SKIPping.
*Maps to*: REQ-ETS-PART1-012.

#### SCENARIO-ETS-PART1-012-RELEASED-MEDIATYPE-WRITE-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/geojson`
**WHEN** the released mediatype-write procedure inspects the API definition
**THEN** at least one canonical feature-resource POST or PUT operation advertises `application/geo+json` request content
**AND** the procedure issues no mutation request
**AND** OPTIONS-only evidence is not sufficient.
*Maps to*: REQ-ETS-PART1-012.

#### SCENARIO-ETS-PART1-012-RELEASED-RELATION-TYPES-001 (CRITICAL)
**GIVEN** inspectable GeoJSON resource representations
**WHEN** the released relation-types procedure processes their `links` members
**THEN** every association relation is valid for its System, Deployment, Procedure, or Sampling Feature resource type
**AND** generic links are ignored
**AND** all resource types are inspected before a no-association-evidence SKIP
**AND** evidence from one resource type cannot hide an invalid later resource.
*Maps to*: REQ-ETS-PART1-012.

#### SCENARIO-ETS-PART1-012-RELEASED-FEATURE-MAPPING-001 (CRITICAL)
**GIVEN** inspectable GeoJSON feature resources
**WHEN** the released feature-attribute-mapping procedure processes every feature
**THEN** `properties.uid` is a valid URI
**AND** present `properties.name` and `properties.description` values are strings
**AND** no first-item shortcut can hide a later invalid feature.
*Maps to*: REQ-ETS-PART1-012.

#### SCENARIO-ETS-PART1-012-RELEASED-SCHEMAS-001 (CRITICAL)
**GIVEN** the IUT exposes actual `application/geo+json` for a canonical System, Deployment, Procedure, or Sampling Feature collection
**WHEN** the corresponding released schema procedure executes
**THEN** it validates the complete collection document and one complete canonical single-resource document against their released schemas
**AND** each of the four resource types remains an independent procedure.
*Maps to*: REQ-ETS-PART1-012.

#### SCENARIO-ETS-PART1-012-RELEASED-RESOURCE-MAPPINGS-001 (CRITICAL)
**GIVEN** inspectable GeoJSON resource representations
**WHEN** the corresponding released System, Deployment, Procedure, or Sampling Feature mapping procedure executes
**THEN** every present mapped attribute and association is validated against the resource-specific tables
**AND** optional absent members are not made mandatory
**AND** all inspectable features are processed.
*Maps to*: REQ-ETS-PART1-012.

#### SCENARIO-ETS-PART1-012-RELEASED-MEDIA-GATE-001 (CRITICAL)
**GIVEN** a canonical request uses `Accept: application/geo+json`
**WHEN** the IUT returns unsupported actual media
**THEN** the procedure records a reasoned evidence SKIP before parsing
**AND** inherited landing-page, conformance, and collections discovery JSON remains parseable when a known non-standard media value such as `auto` is returned
**AND** HTTP failures, invalid supported content, and unsafe pagination remain failures.
*Maps to*: REQ-ETS-PART1-012.

#### SCENARIO-ETS-PART1-012-RELEASED-PROCEDURE-ISOLATION-001 (CRITICAL)
**GIVEN** one released GeoJSON procedure lacks optional runtime evidence
**WHEN** TestNG executes the class
**THEN** that procedure's SKIP does not suppress any other released procedure
**AND** setup retains no mutable response state.
*Maps to*: REQ-ETS-PART1-012.

#### SCENARIO-ETS-PART1-012-RELEASED-DEPENDENCY-CASCADE-001 (CRITICAL)
**GIVEN** Core, Common, or Part 1 API Common fails
**WHEN** the GeoJSON group is scheduled
**THEN** all twelve direct procedures SKIP before GeoJSON IUT access
**AND** System or an unrelated sibling outcome cannot block GeoJSON.
*Maps to*: REQ-ETS-PART1-012.

#### SCENARIO-ETS-PART1-012-RELEASED-SCHEMA-PARITY-001 (CRITICAL)
**GIVEN** the eight released GeoJSON single and collection entry schemas
**WHEN** exact mappings are promoted
**THEN** the bundled resolver-normalized transitive graph is semantically equal to pinned release commit `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`
**AND** the parity gate fails if the source checkout is dirty or at another commit.
*Maps to*: REQ-ETS-PART1-012, REQ-ETS-COVERAGE-001.

#### SCENARIO-ETS-PART1-012-RELEASED-DIRECT-HTTP-COVERAGE-001 (CRITICAL)
**GIVEN** a controlled read-only HTTP IUT
**WHEN** the twelve released procedures execute
**THEN** positive JSON and YAML API-definition, schema, mapping, relation, pagination, and later-evidence paths execute
**AND** key unsupported-media, omission, invalid-schema, invalid-mapping, unsafe-link, and no-evidence branches remain fail closed or SKIP honest as specified.
*Maps to*: REQ-ETS-PART1-012.

#### SCENARIO-ETS-PART1-012-RELEASED-E2E-EXECUTION-001 (CRITICAL)
**GIVEN** the exact ETS image and the unmodified primary local OSH IUT
**WHEN** Dockerized TeamEngine executes the complete suite
**THEN** all twelve GeoJSON methods are deployed and execute or dependency-SKIP honestly
**AND** no IUT mutation occurs
**AND** no OSH or TeamEngine source or binary modification is used.
*Maps to*: REQ-ETS-PART1-012.

#### SCENARIO-ETS-PART1-012-S53-HARDENING-001 (CRITICAL)
**GIVEN** the two LOW Sprint 53 Raze carryovers
**WHEN** Sprint 54 verification runs
**THEN** Property schema parity rejects dirty or wrong-commit source checkouts
**AND** dedicated Property HTTP tests prove pagination and continuation after later item or collection limitations.
*Maps to*: REQ-ETS-PART1-012, REQ-ETS-PART1-008, REQ-ETS-COVERAGE-001.

> Sprints 10, 16, 17, 18, and 19 are historical approximation increments.
> Sprint 58 replaces their combined method surface with the fifteen released
> procedures and a provisional ETS-owned SensorML validator adapter.

#### REQ-ETS-PART1-013: SensorML Encoding Conformance Class (`/conf/sensorml`)
- **Priority**: MUST
- **Status**: IMPLEMENTED - RELEASED ATS DIRECT; 15/15 reviewed exact mappings.
- **Current increment**: CP-018 and S-ETS-58-01 replace the historical thirteen-method approximation with exactly fifteen direct released procedures. They add complete single/collection schema validation through the provisional ETS-owned SensorML adapter, exact API-definition media advertisement, canonical resource-id, complete common/resource mappings, class compatibility, relation semantics, direct API Common dependency, bounded all-resource traversal, schema parity, controlled HTTP, and unmodified-local-OSH TeamEngine E2E. Exact candidate `a593953d8d79d977649db3077696148e90ffb44a` passes clean Docker Maven `729/0/0/3`, image/runtime/security, dependency, credential, immutability, hygiene, and parity gates. Exact local OSH E2E is honestly `246/41/21/184`: all fifteen methods execute, one passes, fourteen fail on unsupported OSH SensorML collection media or incomplete advertised read-media evidence, and 194 recognized IUT requests contain zero writes. Final Raze is `APPROVED 0.99` with no required fixes.
- **Historical increment**: by Sprint 10 Generator, Sprint 16 Generator, Sprint 17 selected-resource relation-types Generator, Sprint 18 relation-types breadth Generator, and Sprint 19 mediatype-write safety-gated Generator (story S-ETS-10-01 gate-closed 2026-05-05; story S-ETS-16-01 Generator complete and Raze-approved 2026-05-06; story S-ETS-17-01 Generator complete 2026-05-07; story S-ETS-18-01 Generator complete 2026-05-07; story S-ETS-19-01 Generator complete 2026-05-07). Sprint 19 adds safety-gated `/req/sensorml/mediatype-write` checks with positive system-resource evidence against a dedicated local OSH mutable IUT. Sprint 18 added independent SensorML relation-types checks for selected System, Deployment, and Procedure representations. Sprint 10 implemented class `org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml.SensorMlTests` with 6 read-only @Tests. Sprint 16 extends it to 9 read-only @Tests with deployment/procedure/property SensorML schema/mapping checks while keeping the full REQ partial. Sprint 17 extends it to 10 read-only @Tests and adds shared helper coverage. Sprint 18 extends it to 12 read-only @Tests. Sprint 19 extends it to 13 @Tests with mutation-gated mediatype-write parsing evidence. Sprint 19 verification: formatter BUILD SUCCESS; Docker Maven BUILD SUCCESS, `144 tests / 0 failures / 0 errors / 3 skipped`, log `ops/test-results/sprint-ets-19-maven-r3-2026-05-07.log`; GeoRobotix TeamEngine smoke r3 reported `89 total / 55 passed / 0 failed / 34 skipped` with zero IUT-bound POST/PUT/DELETE/PATCH across 69 recognized request-log entries. Local OSH mutable-IUT smoke r3 reported `89 total / 52 passed / 4 failed / 33 skipped`; the SensorML mediatype-write test PASSed with exact `Content-Type=application/sml+json`, follow-up GET, and cleanup DELETE evidence. GeoRobotix runtime used explicit `application/sml+json` alternate links for deployment `https://api.georobotix.io/ogc/t18/api/deployments/16sp744ch58g?f=sml3`, procedure `https://api.georobotix.io/ogc/t18/api/procedures/164p7ed8l47g?f=sml3`, and system `https://api.georobotix.io/ogc/t18/api/systems/0mqcvdnfoca0?f=sml3`; CS API `items` wrappers and default Feature JSON are not counted as SensorML PASS.
- **OGC source verified**: Released tag `v1.0.0`, commit `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`. The class identifier is `/req/sensorml`, inherits `/req/api-common` and SensorML 3.0 JSON requirement classes (`json-simple-process`, `json-physical-system`, `json-deployment`, `json-derived-property`), and lists 15 subrequirements: `mediatype-read`, `mediatype-write`, `relation-types`, `resource-id`, `feature-attribute-mapping`, `system-schema`, `system-sml-class`, `system-mappings`, `deployment-schema`, `deployment-mappings`, `procedure-schema`, `procedure-sml-class`, `procedure-mappings`, `property-schema`, and `property-mappings`.
- **Sprint 10 coverage scope**: SensorML systems read-only subset with 6 @Tests: (1) IUT declares `/conf/sensorml`; (2) a System resource exposes or can be requested as a SensorML JSON representation; (3) the SensorML representation returns HTTP 200 with parseable JSON; (4) the representation has minimal SensorML identity/class shape such as `type` plus identifier/member structure sufficient for a non-schema sanity check; (5) the representation links or maps back to the canonical CS API System id/UID when present; (6) TestNG dependency wiring and smoke no-regression. The Generator MAY use the existing single-system `alternate` link with `type="application/sml+json"` and `?f=sml3` when content negotiation on `Accept: application/sml+json` returns default CS API JSON. Current GeoRobotix verification at planning time: `/conformance` declares `/conf/sensorml`; collection-level `GET /systems` with `Accept: application/sml+json` returns `Content-Type: application/json` with top-level `items`; single-system JSON exposes `alternate` links of type `application/sml+json` to `?f=sml3`.
- **Sprint 16 implemented coverage scope**: SensorML deployment/procedure/property read-only subset. GeoRobotix runtime on 2026-05-06: `/conformance` declares `/conf/sensorml`, `/conf/deployment`, `/conf/procedure`, and `/conf/property`; deployment and procedure SensorML checks PASS through explicit item-level `application/sml+json` alternate links; property SensorML SKIPs honestly because `/properties` currently has an empty `items` array. Each resource check first gates on the matching resource conformance class (`/conf/deployment`, `/conf/procedure`, `/conf/property`) before fetching or judging resource-specific SensorML evidence. Procedure mapping requires non-identity process/procedure structure (`definition`, `inputs`, `outputs`, `parameters`, `characteristics`, or `capabilities`); `identifiers` alone is not enough. Sprint 16 does not claim samplingFeature SensorML coverage because upstream `/req/sensorml` lists property schema/mapping subrequirements, not sampling feature subrequirements.
- **Dependency wiring**: Sprint 58 changes the direct released chain to `<group name="sensorml" depends-on="part1apicommon"/>`. Resource-specific conditions remain inside their owning procedure. The historical System Features dependency is superseded.
- **Released subrequirements after Sprint 58**: all fifteen are direct, independently executable, reviewed exact mappings. The public `opengeospatial/ets-sensorml30` suite jar remains outside the accepted dependency boundary.
- **Sprint 17 implemented relation-types scope**: For associations encoded in a SensorML JSON `links` member, relation-types checks require `rel` to equal the association name valid for the selected resource type. If the selected SensorML representation has no association links in a `links` member, the assertion SKIPs with reason. Generic representation links and property-level mapping links are not relation-types PASS evidence. `EncodingRelationTypes` uses resource-specific links-member allowlists only where the OGC SensorML association table maps that association to `links`: System (`subsystems`, `samplingFeatures`, `deployments`, `procedures`, `datastreams`, `controlstreams`), Deployment (`parentDeployment`, `subdeployments`, `featuresOfInterest`, `samplingFeatures`, `datastreams`, `controlstreams`), and Procedure (`implementingSystems`). SensorML `parentSystem` maps to `attachedTo`, not `links`, and Sampling Features are outside the SensorML conformance class. The runtime SensorML assertion currently checks a selected System representation and SKIPs honestly on GeoRobotix because that representation has no top-level links-member association links.
- **Sprint 18 implemented relation-types breadth scope**: `SensorMlTests` now has 12 read-only @Tests and evaluates relation-types independently for selected System, Deployment, and Procedure SensorML representations. Each assertion evaluates its selected SensorML representation independently and SKIPs when the representation has no links-member association links. GeoRobotix runtime on 2026-05-07 SKIPped all three SensorML relation-types checks because the fetched SensorML system, deployment, and procedure bodies expose no top-level `links` member.
- **Sprint 19 implemented mediatype-write scope**: `SensorMlTests` now has 13 @Tests and adds `sensorMlMediaTypeWriteParsesSystemBodyWhenMutationEnabled`. The test checks write-side `Content-Type: application/sml+json` parsing behind the existing Sprint 12 mutation safety gate, requires `/conf/create-replace-delete`, hard-denies public GeoRobotix, and requires follow-up dereference evidence preserving the submitted UID. GeoRobotix declares `/conf/create-replace-delete` and `/conf/sensorml`, but it is a shared public IUT and was not mutated by default smoke. Local OSH mutable-IUT smoke r3 proved the positive system-resource path. OPTIONS readiness alone is not conformance evidence.
- **IUT-state policy**: If the IUT does not declare `/conf/sensorml`, every SensorML @Test SKIPs with reason. If the IUT declares SensorML but only exposes a SensorML representation through an `alternate` link rather than direct `Accept: application/sml+json` negotiation, the sprint may PASS discovery/fetch checks through the alternate link and MUST record that fallback explicitly. A CS API `items` wrapper alone MUST NOT be counted as SensorML PASS.
- **Maps to**: PRD FR-ETS-23.

### Acceptance Scenarios for Sprint 58

#### SCENARIO-ETS-PART1-013-RELEASED-PROCEDURES-001 (CRITICAL)
**GIVEN** released OGC 23-001 Annex A defines fifteen `/conf/sensorml` tests
**WHEN** the shipped SensorML class is inspected
**THEN** it contains exactly fifteen independently executable TestNG methods, one per released identifier
**AND** no declaration-only, dependency-tracer, combined-schema/mapping, or mutation-lifecycle substitute remains.
*Maps to*: REQ-ETS-PART1-013, REQ-ETS-COVERAGE-001.

#### SCENARIO-ETS-PART1-013-DIRECT-PREREQUISITES-001 (CRITICAL)
**GIVEN** SensorML inherits Part 1 API Common directly
**WHEN** TestNG schedules the class
**THEN** the group depends directly on `part1apicommon`
**AND** a failed Core, Common, or API Common prerequisite skips all fifteen before SensorML-specific IUT access
**AND** the documented API Common datetime evidence limitation alone does not suppress the direct procedures.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-013-RELEASED-MEDIA-ADVERTISEMENT-001 (CRITICAL)
**GIVEN** a JSON or YAML OpenAPI service description
**WHEN** the read and write procedures execute
**THEN** OpenAPI 3.0 and 3.1 documents are parsed, including relative path-item, response, and request-body references resolved from the advertised service-description URI
**AND** resolution SHALL preserve referenced schema graphs instead of fully inlining recursive component schemas, so a cyclic released schema graph remains bounded during TeamEngine execution
**AND** parser warnings do not hide a missing model, unresolved required operation reference, or malformed definition
**AND** an ETS-owned bounded resolver fetches only path-item, response, request-body, and parameter references required by these procedures
**AND** root descriptions and external reference documents are streamed through decoded-body size limits
**AND** external references allow HTTP(S) on only the advertised description's exact origin, reject redirects, `file:`, `classpath:`, userinfo, fragments outside JSON Pointer syntax, unrelated hosts, private-target pivots, cycles, oversize bodies, excessive depth, excessive traversal, excessive unique network reads, and excessive total resolution time
**AND** repeated references to a cached document consume traversal budget but do not consume the unique network-read budget
**AND** read media is advertised on every declared canonical SensorML collection and advertised custom items operation
**AND** write media is advertised on at least one canonical POST or PUT
**AND** only explicit 2xx or `2XX` responses count as successful-response evidence
**AND** an advertised malformed, inaccessible, or unsupported-media service description fails instead of being discarded as no evidence
**AND** same-origin service descriptions and their exact-origin references receive the configured IUT credential when present
**AND** the exact-IUT-origin address set is resolved and pinned during SensorML setup before landing-page retrieval, then reused for every credential-bearing service-description and reference request
**AND** cross-origin service-description retrieval and reference resolution are credential-free, reject redirects and restricted resolved addresses, and pin one validated address set per advertised origin for the complete description graph
**AND** blocking DNS, connect, response, and decoded-body work cannot keep operation-reference resolution active beyond its monotonic global deadline
**AND** the deployed ETS jar contains the isolated OpenAPI parser, model, and runtime support needed to execute the same OpenAPI 3.1 reference path under TeamEngine without adding or replacing a TeamEngine-owned jar
**AND** exact-image runtime verification exercises external reference fetching, address-pin reuse, redirect rejection, decoded-body rejection, credential policy, and deadline cancellation from the deployed artifact
**AND** neither procedure issues a mutation request.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-013-VALIDATOR-ADAPTER-001 (CRITICAL)
**GIVEN** a SensorML document and one of the eight released schema targets
**WHEN** the provisional adapter validates it
**THEN** valid content returns no diagnostics and invalid content returns immutable deterministic ETS-owned diagnostics
**AND** NetworkNT types, TestNG, requirement URIs, and verdict policy do not cross the adapter API
**AND** missing schema or configuration faults remain operational errors.
*Maps to*: REQ-ETS-PART1-013, REQ-ETS-VALIDATOR-001.

#### SCENARIO-ETS-PART1-013-RELEASED-SCHEMAS-001 (CRITICAL)
**GIVEN** actual `application/sml+json` canonical System, Deployment, Procedure, or Property resources
**WHEN** the corresponding schema procedure executes
**THEN** it validates complete collection and selected canonical single-resource documents against the released schemas through the adapter
**AND** the four resource types remain independent procedures.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-013-RELEASED-RESOURCE-ID-001 (CRITICAL)
**GIVEN** canonical SensorML resource representations
**WHEN** resource-id executes
**THEN** every document `id` exactly equals its selected canonical URL identifier
**AND** a mismatch fails instead of SKIPping.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-013-RELEASED-COMMON-MAPPINGS-001 (CRITICAL)
**GIVEN** available canonical SensorML resources
**WHEN** common feature mappings execute
**THEN** every present `uniqueId` is an absolute URI
**AND** present `label` and `description` values are strings
**AND** no first-item shortcut hides a later invalid resource.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-013-RELEASED-RESOURCE-MAPPINGS-001 (CRITICAL)
**GIVEN** inspectable System, Deployment, Procedure, or Property SensorML resources
**WHEN** the corresponding mapping procedure executes
**THEN** every present released attribute and association is validated at its exact JSON member
**AND** mapped GeoJSON geometry and SensorML Pose values validate against the pinned released schema graph
**AND** each mapped association URI is an absolute or source-resolved HTTP(S) URI without userinfo or a fragment
**AND** same-origin targets may use the configured IUT credential while cross-origin targets are dereferenced by a credential-free, non-redirecting client
**AND** every returned association resource or collection validates against the exact expected SensorML, GeoJSON, Datastream, or ControlStream schema
**AND** wrong resource types, wrong collections, credential-bearing cross-origin requests, redirects, and unresolved targets fail
**AND** an AssetType value is an exact released label, an approved `cs:` CURIE, or an absolute URI whose final path or fragment is an exact released label
**AND** unbound CURIE prefixes do not satisfy AssetType
**AND** absent optional members remain optional
**AND** every inspectable resource is processed.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-013-RELEASED-CLASS-COMPATIBILITY-001 (CRITICAL)
**GIVEN** System and Procedure SensorML resources
**WHEN** their SensorML class procedures execute
**THEN** physical versus process classes are compatible with the released asset or procedure semantics
**AND** Procedure descriptions contain no `position` member.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-013-RELEASED-RELATION-TYPES-001 (CRITICAL)
**GIVEN** available SensorML `links` members
**WHEN** relation-types executes across all four resource types
**THEN** every non-generic association uses the exact `ogc-rel:<association>` name allowed by that resource mapping table
**AND** the directly applicable SensorML table spelling `ogc-rel:controlstreams` is required despite the conflicting general registry spelling `ogc-rel:controlStreams`
**AND** all resources are inspected before a no-association-evidence SKIP.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-013-RELEASED-MEDIA-GATE-001 (CRITICAL)
**GIVEN** a canonical request uses `Accept: application/sml+json`
**WHEN** actual media is unsupported
**THEN** the procedure fails before parsing because the requested SensorML representation was not returned
**AND** HTTP failures, invalid supported content, schema failures, and unsafe pagination fail visibly.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-013-RELEASED-PROCEDURE-ISOLATION-001 (CRITICAL)
**GIVEN** one released SensorML procedure lacks optional runtime evidence
**WHEN** the class executes
**THEN** its SKIP does not suppress another released procedure
**AND** setup retains no mutable response state.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-013-RELEASED-SCHEMA-PARITY-001 (CRITICAL)
**GIVEN** the eight bundled SensorML entry schemas
**WHEN** mappings are promoted
**THEN** their resolver-normalized transitive graph is semantically equal to pinned release commit `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`
**AND** a dirty or wrong-commit source checkout fails the gate.
*Maps to*: REQ-ETS-PART1-013, REQ-ETS-COVERAGE-001.

#### SCENARIO-ETS-PART1-013-RELEASED-DIRECT-HTTP-COVERAGE-001 (CRITICAL)
**GIVEN** a controlled read-only HTTP IUT
**WHEN** all fifteen released procedures execute
**THEN** every positive API-definition, schema, id, mapping, class, relation, and pagination path executes
**AND** malformed service descriptions, default-only responses, bad association targets, malformed geometry or Pose, later-page unsupported media, pagination cycles, and cross-origin continuations fail
**AND** omission and no-evidence branches SKIP only where the released procedure is genuinely inapplicable.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-013-RELEASED-E2E-EXECUTION-001 (CRITICAL)
**GIVEN** the exact ETS image and unmodified primary local OSH
**WHEN** Dockerized TeamEngine executes the complete suite
**THEN** all fifteen methods deploy and execute or dependency-SKIP honestly
**AND** no IUT mutation occurs
**AND** no OSH or TeamEngine source or binary modification is used.
*Maps to*: REQ-ETS-PART1-013.

### Acceptance Scenarios for Sprint 10

#### SCENARIO-ETS-PART1-013-SENSORML-CONFORMANCE-DECLARED-001 (CRITICAL)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**WHEN** the SensorML suite reads `/conformance`
**THEN** the response contains `/conf/sensorml`
**OR IF** `/conf/sensorml` is absent
**THEN** every SensorML @Test SKIPs with reason citing the missing conformance declaration.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-013-SENSORML-REPRESENTATION-DISCOVERY-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/sensorml`
**WHEN** the SensorML suite selects a System resource
**THEN** it discovers a SensorML JSON representation either through `Accept: application/sml+json` or through an item-level `alternate` link with `type="application/sml+json"`
**AND** a collection-level CS API `items` wrapper alone does NOT pass this representation-discovery assertion.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-013-SENSORML-MEDIATYPE-READ-001 (CRITICAL)
**GIVEN** a SensorML representation URL was selected
**WHEN** the suite fetches that representation
**THEN** the response is HTTP 200 with parseable JSON
**AND** the suite records whether the representation came from direct media type negotiation or from an explicit `alternate` link fallback
**AND** alternate-link fallback alone is not reported as full SensorML `mediatype-read` closure unless the fetched body proves SensorML JSON support.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-013-SENSORML-SYSTEM-SHAPE-001 (CRITICAL)
**GIVEN** a SensorML system representation was fetched
**WHEN** the suite validates the sprint subset shape
**THEN** the body has a minimal SensorML system identity/class structure, including a string `type` member and an identifier member or UID mapping sufficient to relate the representation to the selected System resource.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-013-SENSORML-SYSTEM-MAPPING-001 (CRITICAL)
**GIVEN** the selected CS API System resource has `id` or `properties.uid`
**WHEN** the SensorML representation is inspected
**THEN** the representation preserves an equivalent system identity through `id`, `uniqueId`, `uid`, or a documented SensorML identifier member
**OR** the assertion SKIPs with reason if the IUT exposes SensorML but omits a machine-checkable identity mapping in the selected resource.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-013-SENSORML-DEPENDENCY-SMOKE-001 (CRITICAL)
**GIVEN** the SystemFeatures group fails or is sabotaged
**WHEN** the SensorML suite attempts to run
**THEN** SensorML tests SKIP because `<group name="sensorml" depends-on="systemfeatures"/>` is present
**AND** `scripts/smoke-test.sh` from a /tmp clone reports failed=0 and total PASS+SKIP at least 57 (Sprint 9 baseline 51 plus 6 SensorML @Tests).
*Maps to*: REQ-ETS-PART1-013.

### Acceptance Scenarios for Sprint 16

#### SCENARIO-ETS-PART1-013-SENSORML-DEPLOYMENT-SCHEMA-MAPPING-001 (CRITICAL)
**GIVEN** Sprint 16 adds SensorML deployment read-only checks
**WHEN** the suite evaluates deployment SensorML coverage
**THEN** it first requires the IUT to declare `/conf/deployment`
**OR IF** `/conf/deployment` is absent
**THEN** the deployment SensorML assertion SKIPs before fetching or judging deployment-specific SensorML evidence
**AND WHEN** the suite selects a deployment resource from `/deployments`
**THEN** it fetches a parseable SensorML JSON deployment representation by direct media negotiation, explicit `alternate` link, or `?f=sml3`
**AND** it requires deployment-specific evidence before PASS: `type=Deployment`, matching `id` or `uniqueId`, and a non-empty deployed systems mapping such as `deployedSystems`
**OR IF** the IUT only returns CS API JSON or omits a machine-checkable deployed-system mapping
**THEN** the assertion SKIPs with reason citing `/req/sensorml/deployment-schema` or `/req/sensorml/deployment-mappings`.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-013-SENSORML-PROCEDURE-SCHEMA-MAPPING-001 (CRITICAL)
**GIVEN** Sprint 16 adds SensorML procedure read-only checks
**WHEN** the suite evaluates procedure SensorML coverage
**THEN** it first requires the IUT to declare `/conf/procedure`
**OR IF** `/conf/procedure` is absent
**THEN** the procedure SensorML assertion SKIPs before fetching or judging procedure-specific SensorML evidence
**AND WHEN** the suite selects a procedure resource from `/procedures`
**THEN** it fetches a parseable SensorML JSON procedure representation by direct media negotiation, explicit `alternate` link, or `?f=sml3`
**AND** it requires procedure-specific evidence before PASS: a SensorML procedure-compatible `type`, matching `id` or `uniqueId`, and at least one non-identity process/procedure structure such as `definition`, `inputs`, `outputs`, `parameters`, `characteristics`, or `capabilities`; `identifiers` alone is not sufficient
**OR IF** the IUT only returns CS API JSON or omits machine-checkable procedure mapping evidence
**THEN** the assertion SKIPs with reason citing `/req/sensorml/procedure-schema`, `/req/sensorml/procedure-sml-class`, or `/req/sensorml/procedure-mappings`.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-013-SENSORML-PROPERTY-SCHEMA-MAPPING-001 (CRITICAL)
**GIVEN** Sprint 16 adds SensorML property read-only checks
**WHEN** the suite evaluates property SensorML coverage
**THEN** it first requires the IUT to declare `/conf/property`
**OR IF** `/conf/property` is absent
**THEN** the property SensorML assertion SKIPs before fetching or judging property-specific SensorML evidence
**AND WHEN** the suite reads `/properties`
**THEN** an empty property collection SKIPs with reason citing current IUT state
**AND WHEN** a property item exists
**THEN** the suite fetches parseable SensorML JSON and requires property-specific evidence before PASS: property-compatible `type` plus identity, definition, or identifier mapping
**AND** empty or default CS API JSON responses never count as property SensorML PASS evidence.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-013-SENSORML-NON-SYSTEM-FALLBACK-HONESTY-001 (CRITICAL)
**GIVEN** Sprint 16 adds non-system SensorML checks
**WHEN** deployment, procedure, or property requests return `Content-Type: application/json` CS API wrappers or default Feature JSON
**THEN** those responses are fallback evidence only
**AND** they MUST NOT satisfy SensorML schema or mapping PASS conditions without an explicit SensorML JSON representation.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-013-SENSORML-SMOKE-NO-MUTATION-001 (CRITICAL)
**GIVEN** Sprint 16 is read-only encoding expansion work
**WHEN** `scripts/smoke-test.sh` runs against an explicitly selected advisory GeoRobotix target
**THEN** the TeamEngine smoke result has `failed=0`
**AND** the no-mutation oracle reports zero IUT-bound POST, PUT, DELETE, or PATCH requests.
*Maps to*: REQ-ETS-PART1-013.

### Acceptance Scenarios for Sprint 17

#### SCENARIO-ETS-PART1-013-SENSORML-RELATION-TYPES-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/sensorml`
**WHEN** the SensorML suite inspects a selected SensorML JSON representation with a `links` member
**THEN** generic representation links are ignored for this assertion
**AND** any association encoded in the `links` member has a `rel` value equal to an association name valid for the selected resource type
**AND** a relation type valid only for a different resource type does not satisfy this assertion
**AND** property-level links or non-links-member associations are not counted as `links` member relation-types evidence
**OR IF** no association is encoded in the selected SensorML representation's `links` member
**THEN** the assertion SKIPs with reason.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-012-013-RELATION-TYPES-SMOKE-NO-MUTATION-001 (CRITICAL)
**GIVEN** Sprint 17 is read-only relation-types work
**WHEN** `scripts/smoke-test.sh` runs against an explicitly selected advisory GeoRobotix target
**THEN** the TeamEngine smoke result has `failed=0`
**AND** the no-mutation oracle reports zero IUT-bound POST, PUT, DELETE, or PATCH requests.
*Maps to*: REQ-ETS-PART1-012, REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-013-SENSORML-RELATION-TYPES-BREADTH-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/sensorml` and exposes selected System, Deployment, or Procedure SensorML JSON representations
**WHEN** the SensorML suite inspects each representation's JSON `links` member
**THEN** each resource type is evaluated independently against its SensorML resource-specific links-member association allowlist
**AND** a System PASS or SKIP does not satisfy Deployment or Procedure relation-types checks
**AND** absent `links` members or generic-only links SKIP for that resource type with reason
**AND** SensorML `parentSystem` is not accepted as links-member evidence because that association maps to `attachedTo`, not `links`.
*Maps to*: REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-012-013-RELATION-TYPES-FALLBACK-HONESTY-001 (CRITICAL)
**GIVEN** Sprint 17 checks relation-types for encoding links
**WHEN** a selected representation contains only generic `canonical`, `alternate`, pagination, collection, service-desc, or service-doc links
**THEN** those links do not satisfy association relation-types requirements
**AND** the assertion SKIPs unless at least one association is encoded in the JSON `links` member.
*Maps to*: REQ-ETS-PART1-012, REQ-ETS-PART1-013.

#### SCENARIO-ETS-PART1-012-013-RELATION-TYPES-BREADTH-NO-MUTATION-001 (CRITICAL)
**GIVEN** Sprint 18 is read-only relation-types breadth work
**WHEN** `scripts/smoke-test.sh` runs against an explicitly selected advisory GeoRobotix target
**THEN** the TeamEngine smoke result has `failed=0`
**AND** the no-mutation oracle reports zero IUT-bound POST, PUT, DELETE, or PATCH requests.
*Maps to*: REQ-ETS-PART1-012, REQ-ETS-PART1-013.

### Acceptance Scenarios for Sprint 8

#### SCENARIO-ETS-CLEANUP-SABOTAGE-STDOUT-5CLASS-001 (CRITICAL)
**GIVEN** `scripts/sabotage-test.sh --target=systemfeatures` is run from a /tmp clone
**WHEN** the cascade XML is produced and the script prints the VERDICT-summary to stdout
**THEN** the stdout VERDICT-summary enumerates ALL sibling classes that received SKIP verdict
**AND** the enumeration includes at minimum: subsystems, procedures, deployments, samplingfeatures, propertydefinitions (5 classes)
**AND** the enumeration is derived dynamically from the cascade XML or testng.xml group declarations, NOT hard-coded
**AND** the script exits 0 (cascade XML produced successfully).
*Maps to*: REQ-ETS-CLEANUP-019, Raze GAP-1 (Sprint 7).

#### SCENARIO-ETS-CLEANUP-SPEC-REQ018-5CLASS-EVIDENCE-001 (CRITICAL)
**GIVEN** spec.md REQ-ETS-CLEANUP-018 narrative and ADR-010 dependency-skip-verification-strategy.md
**WHEN** a reviewer reads the current state of these two documents
**THEN** spec.md REQ-018 narrative cites Raze gate-time 5-class cascade XML as the high-water-mark evidence (not just Generator's 3-class XML)
**AND** ADR-010 no longer contains the sentence "Sprint 8+ sabotage exec will further verify the 5-class cascade" (this has been retired, as the 5-class cascade was verified at Sprint 7 Raze gate)
**AND** ADR-010 contains an explicit v4 amendment block or updated retroval note recording the Sprint 7 Raze gate 5-class outcome.
*Maps to*: REQ-ETS-CLEANUP-019, META-GAP-S7-1.

#### SCENARIO-ETS-CLEANUP-DESIGN-MD-PROJECTWIDE-GREP-001 (CRITICAL)
**GIVEN** Generator has run the project-wide grep for `super.filter|try/finally pattern guarantees` across design.md, all ADR docs, and spec.md
**WHEN** Quinn or Raze reads the archived grep output evidence artifact
**THEN** the grep output file exists (e.g. `ops/test-results/sprint-ets-08-01-self-audit-grep.txt` in sister repo or csapi_compliance)
**AND** every hit line is accounted for: either annotated INVALIDATED, marked historical, or explicitly adjudicated as "non-stale because..."
**AND** design.md lines 666-667 (unit test rules referencing try/finally) are explicitly adjudicated with an annotation at the hit line.
*Maps to*: REQ-ETS-CLEANUP-019, META-GAP-S7-3.

#### SCENARIO-ETS-CLEANUP-TEST-RESULTS-ETS-POINTER-001 (NORMAL)
**GIVEN** `ops/test-results.md` in csapi_compliance repo
**WHEN** a reviewer reads the top of the file
**THEN** the file begins with an ETS-pointer block identifying the sister repo `ets-ogcapi-connectedsystems10/ops/test-results/` as the canonical location for Sprint 1+ ETS test evidence
**AND** the pointer block includes the GitHub URL for the sister repo test-results directory.
*Maps to*: REQ-ETS-CLEANUP-019, Raze GAP-3 (Sprint 7).

#### SCENARIO-ETS-CLEANUP-SPRING-JAVAFORMAT-PINNED-001 (NORMAL)
**GIVEN** `pom.xml` in the sister repo
**WHEN** a reviewer inspects the build plugin configuration
**THEN** `spring-javaformat-maven-plugin` has an explicit version declaration in pluginManagement
**AND** the version matches the currently-used version (verified via `mvn help:effective-pom`)
**AND** a comment references Sprint 7 lesson (two-line `if (true)` sabotage marker shape).
*Maps to*: REQ-ETS-CLEANUP-019, Quinn W3 (Sprint 7).

#### SCENARIO-ETS-CLEANUP-MVN-TEST-VIA-DOCKER-001 (NORMAL)
**GIVEN** `scripts/mvn-test-via-docker.sh` exists in the sister repo
**WHEN** Quinn runs `bash scripts/mvn-test-via-docker.sh` from `/tmp/quinn-fresh-sprint8/`
**THEN** the script exits 0
**AND** Maven surefire output is visible in stdout
**AND** the surefire summary shows the current expected test count (≥89: 86 baseline + 3 subdeployment lint tests) with 0 failures and 0 errors.
*Maps to*: REQ-ETS-CLEANUP-019, META-GAP-S7-2, Quinn recurring mvn host PATH gap.

> The following five Sprint 8 scenarios are historical and superseded by the
> Sprint 51 `SCENARIO-ETS-PART1-005-RELEASED-*` scenarios above. They remain
> only as provenance for the original approximation.

#### SCENARIO-ETS-PART1-005-SUBDEP-RESOURCES-001 (CRITICAL)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**AND** the IUT declares `/conf/subdeployment` in `/conformance`
**WHEN** the Subdeployments suite executes `GET /deployments/{id}/subdeployments`
**THEN** the response is HTTP 200
**AND** the body is parseable JSON containing an `items` array (or equivalent collection wrapper per OGC `/req/subdeployment/collection`)
**AND** the items array is non-empty.
**OR IF** the IUT does NOT declare `/conf/subdeployment`, returns 404, or exposes only empty subdeployments collections
**THEN** all Subdeployments @Tests SKIP with reason citing the missing declaration, 404 response, or empty IUT state.
*Maps to*: REQ-ETS-PART1-005.

#### SCENARIO-ETS-PART1-005-SUBDEP-CANONICAL-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/subdeployment` and `GET /deployments/{id}/subdeployments` returns non-empty items
**WHEN** the Subdeployments suite executes the inherited Deployment canonical endpoint `GET /deployments/{firstId}`
**THEN** the response is HTTP 200
**AND** the endpoint exposes the selected Deployment resource per `/req/deployment/canonical-endpoint`
**AND** the ETS performs structural sanity checks on the returned representation (`id`, `type`, and `links`) without treating `req_canonical_endpoint.adoc` as the sole source for those fields.
*Maps to*: REQ-ETS-PART1-005.

#### SCENARIO-ETS-PART1-005-SUBDEP-CANONICAL-URL-001 (CRITICAL)
**GIVEN** the IUT declares `/conf/subdeployment` and a first subdeployment item exists
**WHEN** the Subdeployments suite checks the canonical URL assertion per inherited `/req/deployment/canonical-url`
**THEN** either the item's `links` array contains a `rel=canonical` link OR the inherited Deployment canonical URL `/deployments/{id}` returns HTTP 200
**AND** absence of `rel=self` is NOT a FAIL (preserves v1.0 GH#3 fix policy).
*Maps to*: REQ-ETS-PART1-005.

#### SCENARIO-ETS-PART1-005-SUBDEP-DEPENDENCY-SKIP-001 (CRITICAL)
**GIVEN** the Deployments group produces at least one FAIL verdict
**WHEN** the Subdeployments suite attempts to run
**THEN** all Subdeployments `@Test` methods emit SKIP with reason citing `dependency deployments not satisfied`
**AND** the testng.xml `<group name="subdeployments" depends-on="deployments"/>` wiring is present
**AND** VerifyTestNGSuiteDependency lint tests for the subdeployments group all pass.
*Maps to*: REQ-ETS-PART1-005, REQ-ETS-CLEANUP-005 (3-deep cascade extension).

#### SCENARIO-ETS-PART1-005-SUBDEP-SMOKE-NO-REGRESSION-001 (CRITICAL)
**GIVEN** the Sprint 8 Generator run is complete (S-ETS-08-01 + S-ETS-08-02 both landed)
**WHEN** `scripts/smoke-test.sh` runs from a /tmp clone against GeoRobotix
**THEN** the script exits 0
**AND** total PASS + SKIP ≥ 46 (42 Sprint 7 baseline + ≥4 new subdeployments @Tests, whether PASS or SKIP-with-reason)
**AND** failed = 0
**AND** no regression in existing 8 conformance classes (core, common, systemfeatures, subsystems, procedures, deployments, samplingfeatures, propertydefinitions).
*Maps to*: REQ-ETS-PART1-005.

## Acceptance Scenarios

### CRITICAL Scenarios (Sprint 1 gating)

#### SCENARIO-ETS-SCAFFOLD-BUILD-001 (CRITICAL)
**GIVEN** a clean checkout of `ets-ogcapi-connectedsystems10` at the Sprint 1 commit
**AND** the host has JDK 17 and Maven 3.9 available
**WHEN** a developer runs `mvn clean install`
**THEN** the command exits 0
**AND** a jar is produced at `target/ets-ogcapi-connectedsystems10-<version>.jar`
**AND** the jar contains `META-INF/services/com.occamlab.te.spi.jaxrs.TestSuiteController`.
*Maps to*: REQ-ETS-SCAFFOLD-001, SCAFFOLD-002, SCAFFOLD-005.

#### SCENARIO-ETS-CORE-LANDING-001 (CRITICAL)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**AND** the Core suite is loaded in TeamEngine
**WHEN** the Core suite executes `landing-page` tests
**THEN** the `@Test` for `OGC-19-072 /req/landing-page/root-success` PASSES (canonical OGC `.adoc` form per S-ETS-02-03 sweep)
**AND** the captured HTTP response shows `Content-Type` containing `application/json`
**AND** the body has `title`, `description`, and `links`
**AND** `links` contains both `rel=conformance` AND (`rel=service-desc` OR `rel=service-doc`).
*Maps to*: REQ-ETS-CORE-002.

#### SCENARIO-ETS-CORE-CONFORMANCE-001 (CRITICAL)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**WHEN** the Core suite executes `GET /conformance`
**THEN** the response is HTTP 200
**AND** the body has `conformsTo` (array of URIs)
**AND** the URI list is captured into TestNG suite context for use by dependent suites.
*Maps to*: REQ-ETS-CORE-003.

#### SCENARIO-ETS-TEAMENGINE-LOAD-001 (CRITICAL)
**GIVEN** the Docker image `ets-ogcapi-connectedsystems10` is built from the current accepted Dockerfile
**WHEN** the container is launched via `docker run -p 8081:8080 ets-ogcapi-connectedsystems10`
**THEN** within 30 seconds `GET http://localhost:8081/teamengine/` returns HTTP 200
**AND** the suite list at `GET http://localhost:8081/teamengine/rest/suites` includes `ogcapi-connectedsystems10`
**AND** the TeamEngine logs show zero `ERROR`-level entries during suite registration.
*Maps to*: REQ-ETS-TEAMENGINE-001, TEAMENGINE-003, NFR-ETS-04.

#### SCENARIO-ETS-TEAMENGINE-TE6-IMAGE-PROVENANCE-001 (CRITICAL)
**GIVEN** Sprint 41 selects an OGC-published TeamEngine 6.0.0 runtime
**WHEN** the runtime reference and image metadata are inspected
**THEN** the Dockerfile uses an immutable digest
**AND** evidence records the corresponding TeamEngine, Tomcat, and JDK versions plus the digest refresh procedure
**AND** a mutable tag alone is not accepted as reproducible runtime provenance.
*Maps to*: REQ-ETS-TEAMENGINE-003, REQ-ETS-TEAMENGINE-007.

#### SCENARIO-ETS-TEAMENGINE-TE6-DEPENDENCY-INVENTORY-001 (CRITICAL)
**GIVEN** Maven resolves the ETS runtime closure and the pinned TeamEngine 6 image supplies engine libraries
**WHEN** the Generator chooses which dependencies to install or exclude
**THEN** archived Maven dependency-tree and image library inventories justify every TeamEngine exclusion
**AND** the implemented exclusion list is explicit rather than an unverified `teamengine-*.jar` wildcard
**AND** no added jar duplicates a base-image Maven coordinate family at another version or supplies overlapping functional paths
**AND** the verifier discovers every added jar from base/final inventories, scans Maven metadata regardless of jar filename, and compares coordinate families across versions
**AND** it intersects non-signature functional paths from each added jar with all base jars and permits only exact coordinate-plus-path entries in a versioned, rationale-bearing allowlist
**AND** runtime evidence emits every accepted coordinate-plus-path tuple and fails if any allowlist entry is unused
**AND** shaded NetworkNT message bundles use an ETS-unique resource path referenced by relocated bytecode, so root `jsv-messages*.properties` paths are not added
**AND** adversarial guard tests prove that a renamed duplicate-coordinate jar and an embedded functional-path collision are rejected
**AND** the guard self-test creates at least two accepted collision tuples and asserts that stdout is exactly the complete sorted expected tuple set
**AND** structural Java coverage proves the behavioral self-test contains that multi-tuple completeness assertion
**AND** startup and suite execution show no classloading, service-loading, or linkage errors.
*Maps to*: REQ-ETS-TEAMENGINE-003, REQ-ETS-TEAMENGINE-007, REQ-ETS-SCAFFOLD-004.

#### SCENARIO-ETS-TEAMENGINE-TE6-BASE-IMMUTABILITY-001 (CRITICAL)
**GIVEN** the runtime stage starts from the pinned OGC TeamEngine 6 image
**WHEN** the ETS image is assembled
**THEN** no TeamEngine-owned file from the base image is modified, patched, replaced, deleted, or recursively re-owned
**AND** build steps only add ETS-owned jars, explicitly inventoried runtime dependencies, and uniquely named CTL resources at supported extension locations
**AND** the final image inherits the base image's TeamEngine startup command and runtime configuration unless an intentional override is separately specified and tested.
*Maps to*: REQ-ETS-TEAMENGINE-003, REQ-ETS-TEAMENGINE-007.

#### SCENARIO-ETS-TEAMENGINE-TE6-RUNTIME-INVARIANTS-001 (CRITICAL)
**GIVEN** the TeamEngine 6 image is built with the ETS installed
**WHEN** the final container is inspected and started
**THEN** required installation paths and utilities exist
**AND** ETS and CTL artifacts have runtime-readable ownership
**AND** the effective runtime UID is non-zero
**AND** `/teamengine/` becomes healthy and the Connected Systems suite is registered through SPI/CTL without startup errors.
*Maps to*: REQ-ETS-TEAMENGINE-001, REQ-ETS-TEAMENGINE-003, REQ-ETS-TEAMENGINE-007, REQ-ETS-CLEANUP-004.

#### SCENARIO-ETS-TEAMENGINE-TE6-CONFIG-ALIGNMENT-001 (CRITICAL)
**GIVEN** Dockerfile, Compose, and smoke-test paths can supply runtime configuration
**WHEN** their ports, health endpoint, `JAVA_OPTS`, `CATALINA_OPTS`, startup command, and artifact paths are compared
**THEN** they are behaviorally aligned
**OR** each intentional difference is specified and exercised by a corresponding gate
**AND** documentation does not claim that inherited image settings remain unchanged when a supported launcher overrides them
**AND** no Maven `docker` profile defines an independent TeamEngine runtime or broad dependency-copy path.
*Maps to*: REQ-ETS-TEAMENGINE-004, REQ-ETS-TEAMENGINE-007.

#### SCENARIO-ETS-TEAMENGINE-RUN-ARG-CONTRACT-001 (CRITICAL)
**GIVEN** the suite can be launched from CTL, TestNG defaults, smoke harnesses, README examples, site docs, Javadoc, and sample test-run-props
**WHEN** those public and executable surfaces describe or serialize run arguments
**THEN** the only supported TestNG argument keys are required `iut` plus optional `auth-credential`, `mutation-tests-enabled`, `mutation-iut-policy`, `mobile-system-id`, and `subdeployment-association-evidence`
**AND** human-facing "CS API landing page" wording maps to serialized key `iut`
**AND** `iut-url`, `auth-type`, and `ics` do not appear as supported serialized run arguments.
*Maps to*: REQ-ETS-TEAMENGINE-002, REQ-ETS-TEAMENGINE-008.

#### SCENARIO-ETS-TEAMENGINE-PUBLIC-METADATA-001 (CRITICAL)
**GIVEN** the TeamEngine conformance package includes Maven-derived suite metadata, Dockerfile labels, Compose operator-facing comments, CTL, TeamEngine config, smoke title assertions, README, site docs, Javadoc, and sample test-run-props
**WHEN** those artifacts are inspected before release or sprint close
**THEN** they describe actual OGC API Connected Systems partial Part 1 and implemented partial Part 2 coverage
**AND** they describe TeamEngine 6 as the forward runtime with local OSH as the primary development E2E target and GeoRobotix as advisory only
**AND** archetype placeholders such as XML/W3Schools examples, Class A/Class B, WCAG/XML boilerplate, or generic "describe scope" text are absent.
*Maps to*: REQ-ETS-TEAMENGINE-008.

#### SCENARIO-ETS-TEAMENGINE-MAVEN-DOCKER-PROFILE-001 (CRITICAL)
**GIVEN** Dockerfile, Compose, and `scripts/smoke-test.sh` are the authoritative TeamEngine 6 deployment contract
**WHEN** the Maven POM is inspected
**THEN** it does not contain an active `docker` profile using Fabric8 Docker Maven plugin behavior
**AND** supported local and inert OGC Jenkins build commands do not request a nonexistent `docker` Maven profile
**AND** every Maven profile explicitly requested by any supported Jenkinsfile is declared by the project model
**AND** every supported Jenkinsfile selects JDK 17 and invokes the source-pin bootstrap before this project is built or released
**AND** it does not copy arbitrary Maven runtime dependencies into a TeamEngine webapp as an alternate deployment path.
*Maps to*: REQ-ETS-TEAMENGINE-007, REQ-ETS-TEAMENGINE-008.

#### SCENARIO-ETS-TEAMENGINE-TE6-LOCAL-OSH-E2E-001 (CRITICAL)
**GIVEN** the TeamEngine 6 image has passed Maven, build, startup, and registration checks
**WHEN** `scripts/smoke-test.sh` executes the deployed suite against `http://field-hub-osh-1:8081/sensorhub/api` on `field-hub_default`
**THEN** the TestNG report is non-empty and exact total/pass/fail/error/skip counts are archived
**AND** container logs contain no suite-registration or linkage errors
**AND** the read-only no-mutation oracle records zero IUT-bound POST, PUT, PATCH, or DELETE requests
**AND** prior TeamEngine 5.6.1 evidence is not substituted for this run.
*Maps to*: REQ-ETS-TEAMENGINE-005, REQ-ETS-TEAMENGINE-006, REQ-ETS-TEAMENGINE-007.

#### SCENARIO-ETS-CORE-SMOKE-001 (CRITICAL)
**GIVEN** the TeamEngine + ETS Docker container is running
**WHEN** `scripts/smoke-test.sh` executes the Core suite against GeoRobotix
**THEN** the script exits 0
**AND** the TestNG XML report is non-empty
**AND** every `@Test` in the Core suite produces PASS or SKIP (no FAIL, no ERROR).
*Maps to*: REQ-ETS-TEAMENGINE-005.

### NORMAL Scenarios

#### SCENARIO-ETS-SCAFFOLD-LAYOUT-001 (NORMAL)
**GIVEN** a clean checkout
**WHEN** a structural-diff checklist compares the repo layout to `opengeospatial/ets-ogcapi-features10`
**THEN** the only divergences are spec-subject-driven (e.g. file basenames mention `connectedsystems10` instead of `features10`).
*Maps to*: REQ-ETS-SCAFFOLD-003, NFR-ETS-15.

#### SCENARIO-ETS-SCAFFOLD-REPRODUCIBLE-001 (NORMAL)
**GIVEN** the same commit checked out twice by the local release gate
**WHEN** `mvn clean install` runs in each checkout
**THEN** the resulting jars are byte-identical excluding `META-INF/` timestamps.
*Maps to*: REQ-ETS-SCAFFOLD-005, NFR-ETS-01.

#### SCENARIO-ETS-CORE-RESOURCE-SHAPE-001 (NORMAL)
**GIVEN** any resource fetched from a landing-page link on the IUT
**WHEN** the Core suite asserts the base resource shape
**THEN** the response body has `id` (string), `type` (string), and `links` (array of objects with `href`, `rel`).
*Maps to*: REQ-ETS-CORE-004.

#### Historical Sprint 2 System Approximation Scenarios (SUPERSEDED)

The following scenario identifiers described the removed four-method
SystemFeatures approximation and raw TestNG dependency behavior:

- `SCENARIO-ETS-PART1-DEPENDENCY-SKIP-001`
- `SCENARIO-ETS-PART1-002-SYSTEMFEATURES-LANDING-001`
- `SCENARIO-ETS-PART1-002-SYSTEMFEATURES-DEPENDENCY-SKIP-001`
- `SCENARIO-ETS-PART1-002-SYSTEMFEATURES-RESOURCE-SHAPE-001`
- `SCENARIO-ETS-PART1-002-SYSTEMFEATURES-LINKS-NORMATIVE-001`

They are non-normative historical records. Sprint 47's
`SCENARIO-ETS-PART1-002-RELEASED-*` scenarios and
`SCENARIO-ETS-PART1-001-DEPENDENCY-CASCADE-001` supersede them. They SHALL NOT
drive implementation, method counts, dependency semantics, coverage mappings,
or conformance claims.

#### SCENARIO-ETS-FIXTURES-PORT-COVERAGE-001 (NORMAL)
**GIVEN** the spec-trap fixture corpus is ported into Java `@DataProvider` methods
**WHEN** `scripts/audit-fixture-port.sh` runs in the local verification gate
**THEN** the script exits 0
**AND** every case ID present in TS source has a matching case ID in Java source.
*Maps to*: REQ-ETS-FIXTURES-001, FIXTURES-003, SC-9.

#### SCENARIO-ETS-CORE-LINKS-NORMATIVE-001 (NORMAL)
**GIVEN** an IUT whose landing page contains `rel=conformance` and `rel=service-desc` but does NOT contain `rel=self`
**WHEN** the Core suite runs the landing-page link-relations assertion
**THEN** the test PASSES (absence of `self` is not a FAIL — example-only per OGC 19-072).
*Maps to*: REQ-ETS-CORE-002. Direct port of v1.0 SCENARIO-LINKS-NORMATIVE-001 (GH#3 fix).

#### SCENARIO-ETS-CORE-API-DEF-FALLBACK-001 (NORMAL)
**GIVEN** an IUT whose landing page contains `rel=service-doc` (HTML) but NOT `rel=service-desc`
**WHEN** the Core suite runs the API-definition assertion
**THEN** the test PASSES via the service-doc fallback.
*Maps to*: REQ-ETS-CORE-002. Direct port of v1.0 SCENARIO-API-DEF-FALLBACK-001.

#### SCENARIO-ETS-CLEANUP-URI-CANONICALIZATION-001 (CRITICAL — Sprint 2)
**GIVEN** the spec.md REQ blocks for REQ-ETS-CORE-002..004 + the Java `static final String REQ_*` constants in `conformance/core/*.java`
**WHEN** S-ETS-02-03 sweep completes
**THEN** every URI in spec.md, traceability.md, Java source, and the Sprint 2 close commit message references the OGC canonical `.adoc` form (e.g. `/req/landing-page/root-success` not `/req/core/root-success`)
**AND** dereferencing any updated URI against the OGC normative document returns HTTP 200 (verified by curl spot-check on at least 3 randomly-chosen URIs).
*Maps to*: REQ-ETS-CORE-001..004 (modified), REQ-ETS-CLEANUP-002. Closes Sprint 1 inherited PARTIAL `uri_mapping_fidelity_preserved`.

#### SCENARIO-ETS-CLEANUP-SMOKE-NO-REGRESSION-001 (CRITICAL — Sprint 2)
**GIVEN** all Sprint 2 cleanup commits have landed (S-ETS-02-02 EtsAssert refactor + S-ETS-02-03 URI sweep + S-ETS-02-05 Dockerfile multi-stage)
**WHEN** `bash scripts/smoke-test.sh` runs end-to-end
**THEN** the script exits 0
**AND** the TestNG XML report shows total = 12 (Core preserved) PASS at minimum (plus N for SystemFeatures once S-ETS-02-06 lands)
**AND** zero startup ERROR/SEVERE in the container log.
*Maps to*: REQ-ETS-TEAMENGINE-005, all Sprint 2 cleanup REQs.

#### SCENARIO-ETS-CLEANUP-ETSASSERT-REFACTOR-001 (NORMAL — Sprint 2)
**GIVEN** the conformance.core.* and conformance.systemfeatures.* test classes at the Sprint 2 close HEAD
**WHEN** `grep -E 'throw new AssertionError|Assert\\.fail' src/main/java/.../conformance/*/*.java` runs
**THEN** the grep returns ZERO hits
**AND** every assertion goes through an `ETSAssert.assert*` or `ETSAssert.failWithUri` helper.
*Maps to*: REQ-ETS-CLEANUP-001, REQ-ETS-CORE-001.

#### SCENARIO-ETS-CLEANUP-LOGBACK-MASKING-001 (NORMAL — Sprint 2)
**GIVEN** smoke-test.sh runs with synthetic CTL parameter `auth-credential=Bearer ABCDEFGH12345678WXYZ`
**WHEN** the TestNG report attachments + container log are produced
**THEN** the literal substring `EFGH12345678WXYZ` (would-be-unmasked credential middle) does NOT appear anywhere in the artifacts
**AND** the masked form (e.g. `Beare...mnop`) DOES appear (proving the filter ran rather than dropping the field entirely).
*Maps to*: REQ-ETS-CLEANUP-003, NFR-ETS-08.

#### SCENARIO-ETS-CLEANUP-DOCKERFILE-MULTISTAGE-001 (NORMAL — Sprint 2)
**GIVEN** a fresh CI-style runner with NO `~/.m2` cache or mount available
**WHEN** `docker build .` runs in the Sprint 2 close working tree
**THEN** the build succeeds
**AND** the resulting image runs as non-root (UID != 0)
**AND** the final image size is ≤ 450MB (target 400MB).
*Maps to*: REQ-ETS-TEAMENGINE-003 (modified), REQ-ETS-CLEANUP-004.

#### SCENARIO-ETS-CLEANUP-CI-WORKFLOW-LIVE-001 (NORMAL — Sprint 2)
**GIVEN** this historical scenario requested a live GitHub Actions workflow
**WHEN** CP-003 is applied
**THEN** the scenario is retired and replaced by `SCENARIO-ETS-SCOPE-HOSTED-CI-NONGOAL-001`.
*Maps to*: REQ-ETS-SCOPE-002.

#### SCENARIO-ETS-CLEANUP-ADR-006-007-001 (NORMAL — Sprint 2)
**GIVEN** the Sprint 2 close HEAD
**WHEN** `ls _bmad/adrs/` runs
**THEN** `ADR-006-jersey-3x-jakarta-port.md` exists with the standard ADR sections (Context, Decision, Status, Consequences, Alternatives Considered) and references the 6 Sprint 1 Jersey port commits by SHA
**AND** `ADR-007-dockerfile-base-image-deviation.md` exists with the same standard sections, includes empirical evidence (Docker Hub tag enumeration + JDK 8 java -version + JDK 17 javap -v), and lists alternatives considered
**AND** ADR-001 contains a cross-reference paragraph pointing to ADR-007.
*Maps to*: REQ-ETS-SCAFFOLD-006.

#### SCENARIO-ETS-CLEANUP-DEPENDENCY-SKIP-LIVE-001 (CRITICAL — Sprint 3)
**GIVEN** the SystemFeatures conformance class is wired with `dependsOnGroups="core"` per Sprint 2 close
**AND** Core's `landingPageReturnsHttp200` @Test is sabotaged (e.g. assertion changed to expect HTTP 999) OR a programmatic TestNG XmlSuite mocks Core failure
**WHEN** the suite runs end-to-end (smoke OR unit-test)
**THEN** Core @Test reports `status="FAIL"`
**AND** all six released System methods report `status="SKIP"` (NOT FAIL, NOT
ERROR) before System IUT access
**AND** the SKIP reason references the unsatisfied Core prerequisite.
*Maps to*: REQ-ETS-CLEANUP-005, REQ-ETS-PART1-002. Closes Quinn s06 CONCERN-1 + Raze s06 CONCERN-1 (both flagged the gap that Sprint 2's static-only dependency-skip verification did not exercise the live cascade).

#### SCENARIO-ETS-CLEANUP-CREDENTIAL-LEAK-INTEGRATION-001 (CRITICAL — Sprint 3)
**GIVEN** the suite at the Sprint 3 close HEAD with `auth-credential` wired as a TestNG suite parameter
**AND** `MaskingRequestLoggingFilter` (or equivalent wrap pattern per Architect) is registered alongside CredentialMaskingFilter
**WHEN** `bash scripts/credential-leak-integration-test.sh` runs through the
repository's Docker Maven wrapper
**THEN** the script exits 0
**AND** Maven reports a non-zero targeted test count with zero failures, errors,
and skips
**AND** the Maven and Surefire outputs contain zero unmasked credential hits
**AND** the targeted assertions prove the masked form is present.
*Maps to*: REQ-ETS-CLEANUP-006, REQ-ETS-CLEANUP-003 (modified). Closes Sprint 2 PARTIAL `no_credential_leak_in_test_logs`.

#### SCENARIO-ETS-CLEANUP-REST-ASSURED-LOGGING-WRAPPED-001 (NORMAL — Sprint 3)
**GIVEN** REST-Assured's built-in `RequestLoggingFilter` is explicitly added to a test class (or unit-test scenario) at the Sprint 3 close
**WHEN** that test sends a request with `Authorization: Bearer ABCDEFGH12345678WXYZ`
**THEN** the request-log line emitted by RequestLoggingFilter shows the masked form (e.g. `Authorization: Bear***WXYZ`) — NOT the unmasked `Bearer ABCDEFGH12345678WXYZ`
**AND** the actual outgoing HTTP request still carries the unmasked Authorization header (auth handshake works).
*Maps to*: REQ-ETS-CLEANUP-006. Closes Raze cleanup CONCERN-2 + design.md §529 Sprint 3 hardening deferral.

#### SCENARIO-ETS-CLEANUP-IMAGE-SIZE-001 (NORMAL — Sprint 3)
**GIVEN** the multi-stage Dockerfile at the Sprint 3 close HEAD with image-size optimization applied (per Architect's ratified approach)
**WHEN** `docker images <smoke-built-image> --format '{{.Size}}'` runs
**THEN** the reported size is < 550 MB (Sprint 3 stretch goal — more permissive than ADR-009's 450MB soft target)
**OR** the reported size is 550-700 MB and the deferral rationale is captured in story Implementation Notes per ADR-009 §"Negative" deferral language
**AND** smoke 12+6+N PASS preserved post-optimization (no regression).
*Maps to*: REQ-ETS-CLEANUP-008, REQ-ETS-CLEANUP-004 (modified).

#### SCENARIO-ETS-CLEANUP-DOC-CLEANUPS-001 (NORMAL — Sprint 3)
**GIVEN** Quinn s06 CONCERN-2 (VerifySystemFeaturesTests reference) + Raze s06 CONCERN-2 (ops/test-results/ convention ambiguity)
**WHEN** S-ETS-03-06 closes
**THEN** EITHER `src/test/java/.../conformance/systemfeatures/VerifySystemFeaturesTests.java` exists with substantive coverage OR the s-ets-02-06 story acceptance criterion line 30 is amended to remove the reference
**AND** Sprint 1 + Sprint 2 + Sprint 3 contract `evaluation_artifacts_required` clauses explicitly state the convention: smoke artifacts archive to `ets-ogcapi-connectedsystems10/ops/test-results/`, NOT `csapi_compliance/ops/test-results/`.
*Maps to*: (no REQ — pure documentation closure).

#### SCENARIO-ETS-PART1-001-COMMON-LANDING-001 (CRITICAL — Sprint 3)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**WHEN** the Common suite executes Common-specific landing-page assertions
**THEN** the response body link discipline matches OGC API Common Part 1 (e.g. `rel=conformance` mandatory; `rel=data` OR `rel=collections` if collections endpoint present)
**AND** Common's @Tests use ETSAssert helpers + canonical `/req/common/<X>` (or canonical-equivalent) URI form
**AND** Common runs in parallel with Core (no `dependsOnGroups` declaration on the `common` group).
*Maps to*: REQ-ETS-PART1-001.

#### SCENARIO-ETS-PART1-001-COMMON-CONFORMANCE-001 (NORMAL — Sprint 3)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**WHEN** the Common suite executes `GET /conformance` with Common-specific assertions
**THEN** `conformsTo` includes Common Part 1's classes
**AND** the @Test description references the canonical OGC `.adoc` URI for `/req/common/conformance` (or equivalent form Generator verified at OGC source).
*Maps to*: REQ-ETS-PART1-001.

#### SCENARIO-ETS-PART1-001-COMMON-COLLECTIONS-001 (NORMAL — Sprint 3)
**GIVEN** the IUT may or may not implement `/collections`
**WHEN** the Common suite executes `GET /collections`
**THEN** if HTTP 200: response body contains a `collections` array (assert per `/req/common/collections`)
**AND** if HTTP 404 OR not implemented: @Test reports `status="SKIP"` with reason "/collections not implemented by IUT" (NOT FAIL).
*Maps to*: REQ-ETS-PART1-001.

#### SCENARIO-ETS-PART1-001-COMMON-CONTENT-NEGOTIATION-001 (NORMAL — Sprint 3)
**GIVEN** the IUT's landing page or any Common endpoint
**WHEN** the Common suite executes `GET /?f=json` and `GET /?f=html`
**THEN** the JSON response has `Content-Type` containing `application/json`
**AND** the HTML response has `Content-Type` containing `text/html`
**OR** if the IUT does not support either format: SKIP-with-reason (NOT FAIL — content-negotiation is a discipline, not all IUTs offer both formats).
*Maps to*: REQ-ETS-PART1-001.

#### SCENARIO-ETS-PART1-001-CANONICAL-RESOURCES-001 (CRITICAL — Sprint 46)
**GIVEN** a Part 1 canonical resource type supported by the IUT
**WHEN** `/conf/api-common/canonical-resources` retrieves that endpoint
**THEN** the request negotiates a supported GeoJSON, SensorML JSON, or
documented JSON extension representation
**AND** every page returns HTTP 200 with the representation-appropriate
`features` or `items` array
**AND** every advertised `next` link is followed exactly once.
*Maps to*: REQ-ETS-PART1-001, REQ-ETS-COVERAGE-001.

#### SCENARIO-ETS-PART1-001-COLLECTION-ITEMS-001 (CRITICAL — Sprint 46)
**GIVEN** an advertised collection with a `rel=items` link whose media type is supported by the ETS
**WHEN** `/conf/api-common/collection-items` executes
**THEN** the released `/collections/{collectionId}/items` endpoint returns HTTP 200
**AND** every paginated item is returned to the invoking test.
*Maps to*: REQ-ETS-PART1-001, REQ-ETS-COVERAGE-001.

#### SCENARIO-ETS-PART1-001-RESOURCE-IDS-001 (CRITICAL — Sprint 46)
**GIVEN** all canonical Part 1 resources retrieved from the IUT
**WHEN** `/conf/api-common/resource-ids` executes
**THEN** every resource has a local ID
**AND** no two resources of the same type have the same ID.
*Maps to*: REQ-ETS-PART1-001.

#### SCENARIO-ETS-PART1-001-RESOURCE-UIDS-001 (CRITICAL — Sprint 46)
**GIVEN** all canonical Part 1 resources retrieved from the IUT
**WHEN** `/conf/api-common/resource-uids` executes
**THEN** every GeoJSON `properties.uid`, SensorML `uniqueId`, or extension
`uid` value is a valid absolute URI
**AND** SensorML `uniqueId` takes precedence over other UID-shaped members,
GeoJSON `properties.uid` takes precedence over a direct extension `uid`, and
direct `uid` is used only as the extension fallback
**AND** no UID occurs more than once across all Part 1 resource types.
*Maps to*: REQ-ETS-PART1-001.

#### SCENARIO-ETS-PART1-001-RESOURCE-UID-TYPES-001 (NORMAL — Sprint 46)
**GIVEN** a valid resource UID
**WHEN** `/conf/api-common/resource-uids-types` evaluates its form
**THEN** canonical 128-bit UUID URNs and namespaces in the bundled, provenance-recorded snapshot
of the IANA Formal and Informal URN Namespaces registries satisfy the recommendation
**AND** every other valid URI form emits a visible warning without failing conformance.
*Maps to*: REQ-ETS-PART1-001.

#### SCENARIO-ETS-PART1-001-DATETIME-001 (CRITICAL — Sprint 46)
**GIVEN** an advertised collection with a usable temporal extent
**WHEN** `/conf/api-common/datetime` requests an instant, bounded interval,
open-start interval, and open-end interval derived from that extent
**THEN** every returned feature with `validTime` intersects each applicable query
**AND** a `validTime` bound equal to `now` is evaluated at captured request time
**AND** every unfiltered feature without `validTime` is present in every filtered result
**AND** no usable temporal extent across all advertised collections yields an explicit SKIP rather than PASS.
*Maps to*: REQ-ETS-PART1-001.

#### SCENARIO-ETS-PART1-001-PAGINATION-FAIL-CLOSED-001 (CRITICAL — Sprint 46)
**GIVEN** a canonical or collection-items response whose pagination repeats a URI or exceeds the configured bound
**WHEN** traversal follows `rel=next`
**THEN** the test fails with the canonical requirement URI and does not loop indefinitely.
*Maps to*: REQ-ETS-PART1-001.

#### SCENARIO-ETS-PART1-001-DEPENDENCY-CASCADE-001 (CRITICAL — Sprint 46)
**GIVEN** Core, inherited OGC API Common, or Part 1 API Common fails
**WHEN** System Features and its descendants are scheduled
**THEN** API Common resource setup checks the completed Core and inherited
OGC API Common results before reading the IUT or issuing any request
**AND** a failed or skipped prerequisite causes the API Common configuration
method to report SKIP rather than FAIL
**AND** the explicit System result gate blocks IUT access after every
prerequisite failure, configuration failure, or API Common SKIP other than the
exact documented no-temporal-extent result from `datetimeUsesValidTime`
**AND** that sole allowed evidence limitation remains SKIP while the six direct
System procedures execute
**AND** descendants remain dependency-SKIP when inherited System conformance is
incomplete
**AND** the failure-path gate accepts exactly one report newer than its
per-run marker from an isolated smoke output directory
**AND** no inherited conformance class reports positive evidence for a skipped
prerequisite.
*Maps to*: REQ-ETS-PART1-001..013.

#### Historical Sprint 3 System Scenarios (SUPERSEDED)

`SCENARIO-ETS-PART1-002-SYSTEMFEATURES-COLLECTIONS-001` and
`SCENARIO-ETS-PART1-002-SYSTEMFEATURES-LOCATION-TIME-001` described the removed
`systemAppearsInCollections` and `systemHasGeometryAndValidTime` approximation
methods. They are non-normative historical records and are superseded by the
Sprint 47 `SCENARIO-ETS-PART1-002-RELEASED-*` scenarios below. They SHALL NOT
drive implementation, coverage mapping, or conformance claims.

#### SCENARIO-ETS-PART1-002-RELEASED-LOCATION-001 (NORMAL — Sprint 47)
**GIVEN** all canonical System resources have been retrieved through the reviewed API Common helper
**WHEN** `/conf/system/location` executes
**THEN** every resource except `Simulation` and `Process` assets is inspected for GeoJSON `geometry` or SensorML `position`
**AND** a missing location emits a warning without failing the recommendation.
*Maps to*: REQ-ETS-PART1-002, `/rec/system/location`.

#### SCENARIO-ETS-PART1-002-RELEASED-LOCATION-TIME-001 (CRITICAL — Sprint 47)
**GIVEN** `mobile-system-id` identifies a mobile System known to move within 30 seconds
**WHEN** `/conf/system/location-time` polls its canonical endpoint
**THEN** both selected responses are HTTP 200
**AND** the second GeoJSON `geometry` or SensorML `position` differs from the first
**AND** absence of the input reports SKIP without positive evidence.
*Maps to*: REQ-ETS-PART1-002, `/req/system/location-time`.

#### SCENARIO-ETS-PART1-002-RELEASED-CANONICAL-URL-001 (CRITICAL — Sprint 47)
**GIVEN** every advertised collection with `featureType=sosa:System`
**WHEN** `/conf/system/canonical-url` retrieves all collection items
**THEN** every item has one unambiguous same-origin canonical URL
**AND** dereferencing it returns HTTP 200
**AND** the returned JSON equals the item after canonical links are removed from both.
*Maps to*: REQ-ETS-PART1-002, `/req/system/canonical-url`.

#### SCENARIO-ETS-PART1-002-RELEASED-RESOURCES-ENDPOINT-001 (CRITICAL — Sprint 47)
**GIVEN** a System resources endpoint URL
**WHEN** `/conf/system/resources-endpoint` retrieves every page
**THEN** every response is HTTP 200
**AND** actual `application/geo+json` pages validate against `geojson/systemCollection.json`
**AND** actual `application/sml+json` pages validate against `sensorml/systemCollection.json`
**AND** an unsupported media type warns and reports SKIP.
*Maps to*: REQ-ETS-PART1-002, `/req/system/resources-endpoint`.

#### SCENARIO-ETS-PART1-002-RELEASED-CANONICAL-ENDPOINT-001 (CRITICAL — Sprint 47)
**GIVEN** the normalized API root
**WHEN** `/conf/system/canonical-endpoint` executes
**THEN** `{api_root}/systems` passes the complete resources-endpoint procedure.
*Maps to*: REQ-ETS-PART1-002, `/req/system/canonical-endpoint`.

#### SCENARIO-ETS-PART1-002-RELEASED-COLLECTIONS-001 (CRITICAL — Sprint 47)
**GIVEN** the advertised `/collections` metadata
**WHEN** `/conf/system/collections` executes
**THEN** every collection with exact `featureType=sosa:System` is selected
**AND** every selected item reports one released SOSA System URI/CURIE
**AND** every returned page passes the schema selected by its actual media type.
**AND** zero selected collections is the released procedure's vacuous loop result,
not an added existence or `itemType` assertion.
*Maps to*: REQ-ETS-PART1-002, `/req/system/collections`.

#### SCENARIO-ETS-CLEANUP-CI-WORKFLOW-ESCALATION-001 (CRITICAL — Sprint 4)
**GIVEN** this historical scenario allowed either activation or formal drop
**WHEN** CP-003 is applied
**THEN** only permanent scope removal is valid
**AND** no future activation path remains.
*Maps to*: REQ-ETS-SCOPE-002.

#### SCENARIO-ETS-CLEANUP-CREDENTIAL-LEAK-E2E-001 (CRITICAL — Sprint 4)
**GIVEN** the suite at the Sprint 4 close HEAD with `auth-credential` wired end-to-end through `scripts/smoke-test.sh` (or dedicated `scripts/credential-leak-e2e-test.sh`)
**AND** authenticated IUT (or stub IUT per Architect's ratification) requires `Authorization: Bearer ABCDEFGH12345678WXYZ`
**WHEN** the E2E smoke runs end-to-end
**THEN** the script exits 0
**AND** `grep -r 'EFGH12345678WXYZ' ets-ogcapi-connectedsystems10/ops/test-results/` returns ZERO hits
**AND** `docker logs <container> 2>&1 | grep 'EFGH12345678WXYZ'` returns ZERO hits
**AND** `grep -rE 'Bear\*\*\*WXYZ' ets-ogcapi-connectedsystems10/ops/test-results/` returns at least one hit (proving filter ran rather than dropping the field).
*Maps to*: REQ-ETS-CLEANUP-006 (modified), REQ-ETS-CLEANUP-011. Closes Sprint 3 PARTIAL `credential_leak_integration_test_green` (deeper E2E) + Quinn cumulative CONCERN-1.

#### SCENARIO-ETS-CLEANUP-IMAGE-SIZE-V2-001 (NORMAL — Sprint 4)
**GIVEN** the multi-stage Dockerfile at the Sprint 4 close HEAD with chown-layer attack applied (every `COPY` directive uses `--chown=tomcat:tomcat`; standalone `RUN chown -R ...` deleted)
**WHEN** `docker build` produces the runtime image AND `docker images <smoke-built-image> --format '{{.Size}}'` runs
**THEN** the reported size is < 600 MB (Sprint 4 PASS target via chown-layer attack)
**OR** the reported size is 600-650 MB and the deferral rationale is captured in story Implementation Notes (PARTIAL acceptable per ADR-009 §"Negative" deferral language)
**AND** smoke 22+M PASS preserved post-optimization (no regression).
*Maps to*: REQ-ETS-CLEANUP-008 (modified), REQ-ETS-CLEANUP-010.

#### SCENARIO-ETS-CLEANUP-ADR-009-V2-001 (NORMAL — Sprint 4)
**GIVEN** the Sprint 4 close HEAD with ADR-009 amended (or new ADR-011 superseding) per Architect's ratification
**WHEN** `cat _bmad/adrs/ADR-009-*.md` runs (or ADR-011 if superseding)
**THEN** the ADR records (a) the empirical falsification of the illustrative 200-300MB jar-dedupe projection (Sprint 3 evidence at sprint-ets-03-04-empirical-dedupe-list-2026-04-29.txt cited);
**AND** (b) the chown-layer attack approach + measured delta from Sprint 3 660MB baseline;
**AND** (c) the 80MB-as-dominant-cost identification;
**AND** (d) the Sprint 5+ next-target roadmap (alpine variant per ADR-009 §Alternatives if Sprint 4 chown-attack underperforms).
*Maps to*: REQ-ETS-CLEANUP-010, REQ-ETS-SCAFFOLD-006.

#### SCENARIO-ETS-CLEANUP-SABOTAGE-SCRIPT-HERMETIC-001 (NORMAL — Sprint 4)
**GIVEN** the bash sabotage script at the Sprint 4 close HEAD with two bug fixes applied (stub bind 0.0.0.0 + docker --add-host=host.docker.internal:host-gateway)
**AND** the host is Linux without Docker Desktop
**WHEN** `bash scripts/sabotage-test.sh` runs end-to-end
**THEN** `netstat -tlnp | grep <stub-port>` shows `0.0.0.0:<port>` (NOT `127.0.0.1:<port>` or `localhost:<port>`)
**AND** the smoke container reaches the stub via `host.docker.internal:<port>` (no resolution failure)
**AND** the script exits 0 with parseable TestNG XML archive at `ops/test-results/sprint-ets-04-04-sabotage-script-hermetic-<date>.xml`
**AND** Sprint 3 one-level cascade-skip behavior preserved (no regression).
*Maps to*: REQ-ETS-CLEANUP-012, ADR-010 (extended).

> **Entire historical Sprint 4 Subsystems scenario block superseded by Sprint
> 48.** The five scenarios through
> `SCENARIO-ETS-PART1-003-SUBSYSTEMS-DEPENDENCY-SKIP-001` describe the removed
> four-method approximation and are retained only as audit history. They SHALL
> NOT be treated as active requirements or released ATS mappings. The active
> requirements are the Sprint 48
> `SCENARIO-ETS-PART1-003-RELEASED-*` scenarios above.

#### SCENARIO-ETS-PART1-003-SUBSYSTEMS-RESOURCES-001 (SUPERSEDED — Sprint 4)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**AND** Core suite has PASSED + SystemFeatures suite has PASSED (no two-level cascade-skip triggered)
**WHEN** the Subsystems suite executes `subsystemsResourcesEndpointReturnsCollection` @Test
**THEN** EITHER `GET /systems/{id}/subsystems` returns 200 + JSON with a non-empty `items` array
**OR** SKIP-with-reason if `/systems/{id}/subsystems` returns 404 (IUT does not implement Subsystems)
**AND** the @Test description references the canonical OGC `.adoc` URI for `/req/subsystem/resources-endpoint`.
*Maps to*: REQ-ETS-PART1-003.

#### SCENARIO-ETS-PART1-003-SUBSYSTEMS-CANONICAL-001 (SUPERSEDED — Sprint 4)
**GIVEN** at least one subsystem id discovered from `/systems/{id}/subsystems`
**WHEN** the Subsystems suite executes `subsystemCanonicalEndpointReturnsBaseShape` @Test
**THEN** `GET /subsystems/{id}` returns 200 + JSON with `id` (string), `type` (string), `links` (array per REQ-ETS-CORE-004 base shape)
**AND** the @Test description references the canonical OGC `.adoc` URI for `/req/subsystem/canonical-endpoint`.
*Maps to*: REQ-ETS-PART1-003.

#### SCENARIO-ETS-PART1-003-SUBSYSTEMS-PARENT-LINK-001 (SUPERSEDED — Sprint 4)
**GIVEN** at least one subsystem item from `/subsystems/{id}` or `/systems/{id}/subsystems`
**WHEN** the Subsystems suite executes `subsystemHasParentSystemLink` @Test
**THEN** the subsystem item's `links` array contains an entry with `rel="system"` (or equivalent per OGC `.adoc`) referencing the parent system URI
**AND** the @Test description references the canonical OGC `.adoc` URI for `/req/subsystem/parent-system-link`.
*Maps to*: REQ-ETS-PART1-003.

#### SCENARIO-ETS-PART1-003-SUBSYSTEMS-CANONICAL-URL-001 (SUPERSEDED — Sprint 4)
**GIVEN** at least one subsystem item from `/subsystems/{id}` or `/systems/{id}/subsystems`
**WHEN** the Subsystems suite executes `subsystemHasCanonicalLink` @Test
**THEN** the subsystem item's `links` array contains an entry with `rel="canonical"` per `/req/subsystem/canonical-url`
**AND** absence of `rel="self"` is NOT FAIL (preserves v1.0 GH#3 fix policy from Core landing page).
*Maps to*: REQ-ETS-PART1-003.

#### SCENARIO-ETS-PART1-003-SUBSYSTEMS-DEPENDENCY-SKIP-001 (SUPERSEDED — Sprint 4)
**GIVEN** the Subsystems conformance class is wired with `dependsOnGroups="systemfeatures"` per Sprint 4 close
**AND** SystemFeatures' tests are sabotaged to FAIL (e.g. extended bash sabotage script targeting SystemFeatures, or VerifyTestNGSuiteDependency.java extension exercising the two-level chain)
**WHEN** the suite runs end-to-end (smoke OR unit-test)
**THEN** SystemFeatures @Tests report `status="FAIL"`
**AND** ALL Subsystems @Tests report `status="SKIP"` (NOT FAIL, NOT ERROR) — TWO-LEVEL cascade verified
**AND** the SKIP reason references the unsatisfied `systemfeatures` group dependency.
*Maps to*: REQ-ETS-PART1-003. Closes architect-handoff `TWO-LEVEL-DEPENDENCY-CASCADE-MAY-NOT-WORK` risk; first multi-level cascade verification in the project.

### Sprint 5 Scenarios

#### SCENARIO-ETS-CLEANUP-CREDENTIAL-LEAK-WIRING-001 (CRITICAL — Sprint 5)
**GIVEN** `SMOKE_AUTH_CREDENTIAL=Bearer ABCDEFGH12345678WXYZ` is set in the environment
**AND** `scripts/stub-iut.sh` is running on 0.0.0.0 on an ephemeral port
**WHEN** `scripts/credential-leak-e2e-test.sh` invokes `scripts/smoke-test.sh` targeting the stub IUT
**THEN** the stub-IUT log shows AT LEAST ONE request with `Authorization: Bearer ABCDEFGH12345678WXYZ`
**AND** the smoke-test.sh passes the credential as `auth-credential` TestNG suite parameter via curl POST.
*Maps to*: REQ-ETS-CLEANUP-013. Closes GAP-1 wiring defect from Sprint 4 cumulative gates.

#### SCENARIO-ETS-CLEANUP-CREDENTIAL-LEAK-THREE-FOLD-001 (CRITICAL — Sprint 5)
**GIVEN** `scripts/credential-leak-e2e-test.sh` runs end-to-end with stub IUT and `SMOKE_AUTH_CREDENTIAL` set
**WHEN** the three-fold cross-check executes
**THEN** prong (a): `grep -r 'EFGH12345678WXYZ' $SMOKE_OUTPUT_DIR` returns ZERO hits
**AND** prong (b): `grep -rE 'Bear\*\*\*WXYZ' $SMOKE_OUTPUT_DIR` returns AT LEAST ONE hit (proves MaskingRequestLoggingFilter ran)
**AND** prong (c): stub-IUT log returns AT LEAST ONE hit for unmasked credential (proves wire carried the credential).
*Maps to*: REQ-ETS-CLEANUP-013, REQ-ETS-CLEANUP-006 (closed). Fully closes design.md §529 deferral.

#### SCENARIO-ETS-CLEANUP-SMOKE-OUTPUT-DIR-001 (NORMAL — Sprint 5)
**GIVEN** `SMOKE_OUTPUT_DIR=/tmp/smoke-test-output` is set
**WHEN** `scripts/smoke-test.sh` runs end-to-end
**THEN** TestNG XML artifacts are written to `/tmp/smoke-test-output/` (not to `ops/test-results/`)
**AND** the user's worktree `ops/test-results/` directory is unmodified.
*Maps to*: REQ-ETS-CLEANUP-014. Closes Sprint 2 + Sprint 4 worktree-pollution incident pattern.

#### SCENARIO-ETS-CLEANUP-SABOTAGE-TARGET-001 (NORMAL — Sprint 5)
**GIVEN** `scripts/sabotage-test.sh --target=systemfeatures` is invoked
**WHEN** the script runs end-to-end
**THEN** the produced TestNG XML shows Core and Common PASS, at least one
SystemFeatures method FAIL, the remaining System methods retain their
evidence-honest results, and every direct or transitive TestNG dependency
descendant group SKIP
**AND** the original SystemFeaturesTests.java file in the worktree is UNMODIFIED after the run.
*Maps to*: REQ-ETS-CLEANUP-015.

#### SCENARIO-ETS-CLEANUP-SUBSYSTEMS-JAVADOC-001 (NORMAL — Sprint 5)
**GIVEN** SubsystemsTests.java at Sprint 5 close HEAD
**WHEN** a reviewer reads the class-level javadoc
**THEN** the javadoc enumerates 6 `.adoc` files (not 5) including `req_subcollection_time.adoc`
**AND** the javadoc clarifies that `req_subcollection_time.adoc` exists but is not enumerated in requirements_class_system_components.adoc.
*Maps to*: REQ-ETS-PART1-003 (minor doc accuracy).

#### SCENARIO-ETS-CLEANUP-ADR-010-V3-001 (NORMAL — Sprint 5)
**GIVEN** `_bmad/adrs/ADR-010.md` at Sprint 5 close HEAD
**WHEN** a reviewer reads the Amendment v3 section
**THEN** the section states that TestNG 7.9.0 transitive cascade is VERIFIED LIVE (not hypothesized)
**AND** cites Raze Sprint 4 sabotage evidence (total=26/passed=16/failed=1/skipped=9)
**AND** does NOT modify the architectural decision text of the original ADR.
*Maps to*: ADR-010 amendment.

> The following five Sprint 5 Procedure scenarios are retained as historical
> planning records. Sprint 50's `RELEASED-*` scenarios under
> `REQ-ETS-PART1-006` supersede them as current acceptance criteria. In
> particular, a non-null Procedure location is now a conformance FAIL, not a
> SKIP, and Procedure inherits API Common directly rather than SystemFeatures.

#### SCENARIO-ETS-PART1-006-PROCEDURES-RESOURCES-001 (CRITICAL — Sprint 5)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**WHEN** the Procedures suite executes `GET /procedures`
**THEN** the response is HTTP 200
**AND** the JSON body contains an `items` array with at least one element
**AND** the assertion cites `OGC-23-001 /req/procedure/resources-endpoint`.
*Maps to*: REQ-ETS-PART1-006.

#### SCENARIO-ETS-PART1-006-PROCEDURES-LOCATION-001 (CRITICAL — Sprint 5)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**WHEN** the Procedures suite fetches `GET /procedures/{id}` for a representative procedure item
**THEN** the `geometry` field of the response is null or absent
**AND** the assertion cites `OGC-23-001 /req/procedure/location`
**OR** the test SKIPs with reason if the IUT returns non-null geometry (IUT conformance gap flagged).
*Maps to*: REQ-ETS-PART1-006.

#### SCENARIO-ETS-PART1-006-PROCEDURES-CANONICAL-001 (NORMAL — Sprint 5)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**WHEN** the Procedures suite fetches `GET /procedures/{id}`
**THEN** the response has `id` (string), `type` (string), and `links` (array) per REQ-ETS-CORE-004 base shape
**AND** the assertion cites `OGC-23-001 /req/procedure/canonical-endpoint`.
*Maps to*: REQ-ETS-PART1-006.

#### SCENARIO-ETS-PART1-006-PROCEDURES-CANONICAL-URL-001 (NORMAL — Sprint 5)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**WHEN** the Procedures suite fetches `GET /procedures/{id}`
**THEN** the `links` array contains at least one entry with `rel="canonical"`
**AND** the assertion cites `OGC-23-001 /req/procedure/canonical-url`.
*Maps to*: REQ-ETS-PART1-006.

#### SCENARIO-ETS-PART1-006-PROCEDURES-DEPENDENCY-SKIP-001 (CRITICAL — Sprint 5)
**GIVEN** SystemFeatures tests are sabotaged to FAIL
**WHEN** the suite runs end-to-end
**THEN** ALL ProceduresTests @Tests report `status="SKIP"` (NOT FAIL, NOT ERROR)
**AND** the SKIP reason references the unsatisfied `systemfeatures` group dependency.
*Maps to*: REQ-ETS-PART1-006. Extends the TWO-LEVEL cascade pattern to Procedures.

#### SCENARIO-ETS-PART1-004-DEPLOYMENTS-RESOURCES-001 (CRITICAL — Sprint 5)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**WHEN** the Deployments suite executes `GET /deployments`
**THEN** the response is HTTP 200
**AND** the JSON body contains an `items` array with at least one element
**AND** the assertion cites `OGC-23-001 /req/deployment/resources-endpoint`.
*Maps to*: REQ-ETS-PART1-004.

#### SCENARIO-ETS-PART1-004-DEPLOYMENTS-CANONICAL-001 (NORMAL — Sprint 5)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**WHEN** the Deployments suite fetches `GET /deployments/{id}`
**THEN** the response has `id` (string), `type` (string), and `links` (array) per REQ-ETS-CORE-004 base shape
**AND** the assertion cites `OGC-23-001 /req/deployment/canonical-endpoint`.
*Maps to*: REQ-ETS-PART1-004.

#### SCENARIO-ETS-PART1-004-DEPLOYMENTS-CANONICAL-URL-001 (NORMAL — Sprint 5)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**WHEN** the Deployments suite fetches `GET /deployments/{id}`
**THEN** the `links` array contains at least one entry with `rel="canonical"`
**AND** the assertion cites `OGC-23-001 /req/deployment/canonical-url`.
*Maps to*: REQ-ETS-PART1-004.

#### SCENARIO-ETS-PART1-004-DEPLOYMENTS-DEPLOYED-SYSTEM-001 (NORMAL — Sprint 5)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**WHEN** the Deployments suite checks for DeployedSystem encoding conformance class in `/conformance`
**THEN** if the IUT declares the class: the test PASSES asserting a DeployedSystem representation exists
**OR** if the IUT does NOT declare the class: the test SKIPs with reason (IUT conformance gap noted)
**AND** the assertion cites `OGC-23-001 /req/deployment/deployed-system-resource`.
*Maps to*: REQ-ETS-PART1-004.

#### SCENARIO-ETS-PART1-004-DEPLOYMENTS-DEPENDENCY-SKIP-001 (CRITICAL — Sprint 5)
**GIVEN** SystemFeatures tests are sabotaged to FAIL
**WHEN** the suite runs end-to-end
**THEN** ALL DeploymentsTests @Tests report `status="SKIP"` (NOT FAIL, NOT ERROR)
**AND** the SKIP reason references the unsatisfied `systemfeatures` group dependency.
*Maps to*: REQ-ETS-PART1-004. Extends the TWO-LEVEL cascade pattern to Deployments.

#### SCENARIO-ETS-CLEANUP-MASKING-WIRE-FIX-001 (CRITICAL — Sprint 6)
**GIVEN** `MaskingRequestLoggingFilter.filter()` has been redesigned per S-ETS-06-01 (approach i: no requestSpec mutation before ctx.next)
**AND** the suite runs `scripts/credential-leak-e2e-test.sh` with `SMOKE_AUTH_CREDENTIAL=Bearer ABCDEFGH12345678WXYZ` against the stub-IUT
**WHEN** the three-fold cross-check executes
**THEN** (a) ZERO unmasked literal hits for `EFGH12345678WXYZ` in TestNG XML + container log + smoke log
**AND** (b) AT LEAST ONE masked-form hit for `Bear***WXYZ` in log output (filter ran — log confirms masking at log time)
**AND** (c) AT LEAST ONE unmasked-credential hit for `Bearer ABCDEFGH12345678WXYZ` in stub-IUT log (wire carried the ORIGINAL credential)
**AND** the filter's own log output confirms the masked form was emitted at log time.
*Maps to*: REQ-ETS-CLEANUP-016. Closes the 2-sprint-old `credential_leak_e2e_full_pass` criterion (open since Sprint 4 GAP-1 → Sprint 5 GAP-1').

#### SCENARIO-ETS-CLEANUP-MASKING-WIRE-TEST-001 (CRITICAL — Sprint 6)
**GIVEN** a `CapturingFilterContext` test harness that records the `requestSpec` passed to `ctx.next()`
**AND** a `MaskingRequestLoggingFilter` instance configured with DEFAULT_HEADERS_TO_MASK
**AND** a request spec carrying `Authorization: Bearer ABCDEFGH12345678WXYZ`
**WHEN** `filter.filter(requestSpec, responseSpec, capturingCtx)` is called
**THEN** the captured spec's `Authorization` header value equals `Bearer ABCDEFGH12345678WXYZ` (the ORIGINAL value)
**AND** the log output (captured PrintStream) contains the masked form `Bear***WXYZ` (proving the filter logged the masked form)
**AND** the captured spec DOES NOT contain `Bear***WXYZ` as the Authorization header value.
*Maps to*: REQ-ETS-CLEANUP-016. This is the wire-side unit test that VerifyMaskingRequestLoggingFilter's StubFilterContext cannot provide.

#### SCENARIO-ETS-CLEANUP-CREDENTIAL-LEAK-THREE-FOLD-CLOSE-001 (CRITICAL — Sprint 6)
**GIVEN** Sprint 6 lands the MaskingRequestLoggingFilter fix (S-ETS-06-01) AND the container-log capture timing fix (bundled)
**WHEN** `scripts/credential-leak-e2e-test.sh` runs from `/tmp/<role>-fresh-sprint6/` with `SMOKE_OUTPUT_DIR=/tmp/<role>-fresh-sprint6/test-results/`
**THEN** the script exits 0 with overall verdict PASS
**AND** the TestNG XML and container log SHALL each be selected from
`SMOKE_OUTPUT_DIR` only when produced after the current run's start marker;
stale worktree or prior-run artifacts SHALL fail the gate
**AND** prong (a): ZERO unmasked literal hits in TestNG XML + container log + smoke log (container log is now captured BEFORE teardown — not vacuously empty)
**AND** prong (b): AT LEAST ONE masked-form `Bear***WXYZ` hit in container log (filter emits masked form during smoke)
**AND** prong (c): AT LEAST ONE unmasked `Bearer ABCDEFGH12345678WXYZ` hit in stub-IUT log (wire carries original credential).
*Maps to*: REQ-ETS-CLEANUP-016, REQ-ETS-CLEANUP-011 (finally IMPLEMENTED after Sprint 4 + Sprint 5 carryover).

#### SCENARIO-ETS-CLEANUP-SABOTAGE-TARGET-DOCKER-FIX-001 (CRITICAL — Sprint 6)
**GIVEN** `scripts/sabotage-test.sh` rsync line has been fixed to include `.git/` in the temp worktree (S-ETS-06-02)
**WHEN** `bash scripts/sabotage-test.sh --target=systemfeatures` runs from `/tmp/<role>-fresh-sprint6/`
**THEN** the Docker build step succeeds (no `COPY .git ./.git: not found` error)
**AND** the smoke run executes against the sabotaged temp tree
**AND** the cascade XML shows Core and Common PASS, at least one
SystemFeatures method FAIL, and all direct or transitive TestNG dependency
descendant groups SKIP
**AND** the script exits 0 with cascade verdict PASS.
*Maps to*: REQ-ETS-CLEANUP-017, REQ-ETS-CLEANUP-015 (promoted from PARTIAL to FULLY-IMPLEMENTED).

#### SCENARIO-ETS-CLEANUP-SABOTAGE-CASCADE-THREE-CLASS-001 (CRITICAL — Sprint 6)
**GIVEN** the sabotage --target=systemfeatures script runs successfully (SCENARIO-ETS-CLEANUP-SABOTAGE-TARGET-DOCKER-FIX-001)
**WHEN** the cascade XML is parsed
**THEN** all Core @Tests (12) show status="PASS"
**AND** all Common @Tests (4) show status="PASS"
**AND** SystemFeatures @Tests show at least 1 FAIL + at least 5 SKIP (within-class cascade)
**AND** ALL Subsystems @Tests (4) show status="SKIP"
**AND** ALL Procedures @Tests (4) show status="SKIP"
**AND** ALL Deployments @Tests (4) show status="SKIP"
**AND** no FAIL appears in Subsystems/Procedures/Deployments (SKIP, not FAIL, is required — a FAIL would indicate a different defect from cascade failure).
*Maps to*: REQ-ETS-CLEANUP-017, ADR-010 v3 "forward-extends to Procedures + Deployments" (live-exec confirmation).

#### SCENARIO-ETS-CLEANUP-WIRE-SIDE-TEST-001 (CRITICAL — Sprint 6)
**GIVEN** a `CapturingFilterContext` class in `src/test/java/` that implements `FilterContext` and records the `requestSpec` passed to `ctx.next()`
**WHEN** `mvn test` runs
**THEN** `VerifyWireRestoresOriginalCredential` test class is present and all its @Test methods PASS
**AND** the test asserts that the captured requestSpec Authorization header equals the ORIGINAL credential (not the masked form)
**AND** the test is identified as a "wire-side test" in its class javadoc (distinct from wiring-only StubFilterContext tests).
*Maps to*: REQ-ETS-CLEANUP-016.

#### SCENARIO-ETS-CLEANUP-WIRING-TEST-RECLASSIFIED-001 (NORMAL — Sprint 6)
**GIVEN** spec.md REQ-ETS-CLEANUP-013 implementation notes and story S-ETS-05-01 Implementation Notes
**WHEN** a developer reads the implementation status
**THEN** the notes explicitly state: "VerifyAuthCredentialPropagation (8 tests) + VerifyMaskingRequestLoggingFilter (8 tests) = 16 unit tests are wiring-only — use StubFilterContext returning null from ctx.next(); they CANNOT detect filter-ordering defects (wire-side ordering is not exercised)"
**AND** the notes reference VerifyWireRestoresOriginalCredential as the wire-side proof test.
*Maps to*: REQ-ETS-CLEANUP-016.

#### SCENARIO-ETS-CLEANUP-SABOTAGE-LOG-HONEST-001 (NORMAL — Sprint 6)
**GIVEN** `scripts/sabotage-test.sh --target=systemfeatures` is running
**WHEN** the Docker build step fails in a broken local environment
**THEN** the log message reads `"Docker build FAILED"` or equivalent rather
than claiming a sabotage hit
**AND** when the Docker build succeeds but smoke exits non-zero due to the
sabotage marker, the log identifies the expected sabotaged System method
failure.
*Maps to*: REQ-ETS-CLEANUP-015 (improved UX).

#### SCENARIO-ETS-CLEANUP-SABOTAGE-JAVAC-FIX-001 (CRITICAL — Sprint 7)
**GIVEN** `scripts/sabotage-test.sh --target=systemfeatures` is run from a /tmp clone at Sprint 7 HEAD
**WHEN** the python injector injects
`if (true) throw new AssertionError("SABOTAGED ...")` as the first statement of
`systemLocationsFollowRecommendation()`
**THEN** Docker build step 8/8 (`mvn clean package`) succeeds without `unreachable statement` compile error
**AND** the smoke run produces a TestNG XML cascade report
**AND** the cascade report shows Core and Common PASS, at least one
SystemFeatures method FAIL, and all direct or transitive TestNG dependency
descendant groups SKIP.
*Maps to*: REQ-ETS-CLEANUP-017 (live acceptance), REQ-ETS-CLEANUP-018.

#### SCENARIO-ETS-CLEANUP-SABOTAGE-PIPEFAIL-FIX-001 (CRITICAL — Sprint 7)
**GIVEN** `scripts/sabotage-test.sh --target=systemfeatures` is run where Docker build fails (e.g. injected compile error path)
**WHEN** the disambiguation block is reached after `SMOKE_EXIT_CODE` capture
**THEN** the script does NOT exit prematurely before the disambiguation log message fires
**AND** the log contains `"Docker build FAILED"` (not a sabotage-marker hit message)
**AND** bash -x trace evidence confirms the disambiguation block at lines ~287-298 is reachable.
*Maps to*: REQ-ETS-CLEANUP-018.

#### SCENARIO-ETS-CLEANUP-CRED-LEAK-PRONG-B-FIX-001 (CRITICAL — Sprint 7)
**GIVEN** `scripts/credential-leak-e2e-test.sh` is run from a /tmp clone with `SMOKE_AUTH_CREDENTIAL='Bearer ABCDEFGH12345678WXYZ'`
**WHEN** the three-fold cross-check executes
**THEN** the script exits 0 (PASS exit code, not FAIL)
**AND** prong (b) finds ≥1 `Bear***WXYZ` hit (in smoke-test.sh's archived container log, not the vacuous post-teardown docker logs output)
**AND** prongs (a) and (c) continue to PASS as in Sprint 6 manual verification.
*Maps to*: REQ-ETS-CLEANUP-018, REQ-ETS-CLEANUP-011 (automated script now matches semantic PASS).

#### SCENARIO-ETS-CLEANUP-REQ017-STATUS-HONESTY-001 (CRITICAL — Sprint 7)
**GIVEN** spec.md REQ-ETS-CLEANUP-017 status text
**WHEN** an agent reads the status before Sprint 7 live-exec completes
**THEN** the status reads `STRUCTURAL-IMPLEMENTED-LIVE-EXEC-FAILED` (not `IMPLEMENTED`)
**AND** the status text cross-references Raze HIGH GAP-1 + meta-Raze META-GAP-M2
**WHEN** Sprint 7 S-ETS-07-01 closes with live cascade XML produced
**THEN** Generator promotes status to `IMPLEMENTED (Sprint 7 S-ETS-07-01)` with cascade XML evidence.
*Maps to*: REQ-ETS-CLEANUP-017, REQ-ETS-CLEANUP-018. spec-anchored-development status-honesty principle.

#### SCENARIO-ETS-CLEANUP-DESIGN-MD-WRAP-PATTERN-001 (NORMAL — Sprint 7)
**GIVEN** `openspec/capabilities/ets-ogcapi-connectedsystems/design.md` §"Sprint 3 hardening" lines ~531-636
**WHEN** a reader reads the section
**THEN** a "Sprint 6 redesign: approach (i)" subsection appears BEFORE the old code block
**AND** the old Java code block is labelled "Historical (Sprint 3 baseline — superseded by Sprint 6 approach (i))"
**AND** the false claim "try/finally pattern guarantees the IUT receives the real credential header even if super.filter() throws" is corrected or removed
**AND** the deleted try/finally unit-test descriptions are marked as historical or removed.
*Maps to*: REQ-ETS-CLEANUP-018. Closes meta-Raze META-GAP-M1.

#### SCENARIO-ETS-CLEANUP-ADR010-V4-OR-RETROVAL-001 (NORMAL — Sprint 7)
**GIVEN** ADR-010 v3 amendment claims "TestNG 7.9.0 transitive cascade VERIFIED LIVE (2026-04-29)" via Sprint 4 2-class chain
**WHEN** Sprint 7 S-ETS-07-01 Wedge 1 produces a 3-class cascade XML
**THEN** ADR-010 receives a "Sprint 7 live-verification note" confirming the 3-class cascade was produced (retroactively validating v3's forward-extends claim)
**AND** the note records the cascade XML archive path and date.
**OR** if Wedge 1 does not close in Sprint 7, ADR-010 receives a v4 amendment noting "3-class live-verification attempt failed in Sprint 6 due to sabotage-marker compile error; Sprint 7 carryover".
*Maps to*: REQ-ETS-CLEANUP-017, REQ-ETS-CLEANUP-018. Closes meta-Raze META-GAP-M3.

#### SCENARIO-ETS-PART1-007-SF-RESOURCES-001 (CRITICAL — Sprint 7)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**AND** the IUT declares `/conf/sf` in its conformance declaration
**WHEN** `SamplingFeaturesTests` executes `GET /samplingFeatures`
**THEN** the response is HTTP 200
**AND** the response body contains a non-empty `features` or `items` array.
*Maps to*: REQ-ETS-PART1-007, OGC requirement `/req/sf/resources-endpoint`.

#### SCENARIO-ETS-PART1-007-SF-CANONICAL-001 (CRITICAL — Sprint 7)
**GIVEN** at least one sampling feature exists in the collection
**WHEN** `SamplingFeaturesTests` retrieves the first sampling feature at `GET /samplingFeatures/{id}`
**THEN** the response is HTTP 200
**AND** the response body contains `id`, `type`, and a `links` array.
*Maps to*: REQ-ETS-PART1-007.

#### SCENARIO-ETS-PART1-007-SF-CANONICAL-URL-001 (CRITICAL — Sprint 7)
**GIVEN** a sampling feature resource at `GET /samplingFeatures/{id}`
**WHEN** the `links` array is inspected
**THEN** at least one link with `rel=canonical` is present
**AND** the href equals `{api_root}/samplingFeatures/{id}`.
*Maps to*: REQ-ETS-PART1-007, OGC requirement `/req/sf/canonical-url`.

#### SCENARIO-ETS-PART1-007-SF-DEPENDENCY-SKIP-001 (CRITICAL — Sprint 7)
**GIVEN** the testng.xml declares `<group name="samplingfeatures" depends-on="systemfeatures"/>`
**WHEN** SystemFeatures group has any FAIL or SKIP
**THEN** all SamplingFeaturesTests `@Test` methods are SKIPped by TestNG
**AND** the @BeforeClass SkipException fallback also fires as belt-and-suspenders defense-in-depth.
*Maps to*: REQ-ETS-PART1-007, ADR-010 v3.

#### SCENARIO-ETS-PART1-007-SF-SMOKE-NO-REGRESSION-001 (CRITICAL — Sprint 7)
**GIVEN** the TeamEngine + ETS Docker container is running post-Sprint 7
**WHEN** `scripts/smoke-test.sh` executes against GeoRobotix
**THEN** existing 34 @Tests (Core + SystemFeatures + Common + Subsystems + Procedures + Deployments) all continue to PASS
**AND** ≥4 new SamplingFeaturesTests @Tests PASS
**AND** total smoke PASS ≥ 38.
*Maps to*: REQ-ETS-PART1-007, REQ-ETS-TEAMENGINE-005.

#### SCENARIO-ETS-PART1-008-PROP-RESOURCES-001 (CRITICAL — Sprint 7)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**WHEN** `PropertyDefinitionsTests` executes `GET /properties`
**THEN** the response is HTTP 200
**AND** the response body contains a non-empty collection of property definitions.
*Maps to*: REQ-ETS-PART1-008, OGC requirement `/req/property/resources-endpoint`.

#### SCENARIO-ETS-PART1-008-PROP-CANONICAL-001 (CRITICAL — Sprint 7)
**GIVEN** at least one property definition exists in the collection
**WHEN** `PropertyDefinitionsTests` retrieves the first property at `GET /properties/{id}`
**THEN** the response is HTTP 200
**AND** the response body contains `id`, `type`, and a `links` array.
*Maps to*: REQ-ETS-PART1-008.

#### SCENARIO-ETS-PART1-008-PROP-CANONICAL-URL-001 (CRITICAL — Sprint 7)
**GIVEN** a property definition resource at `GET /properties/{id}`
**WHEN** the `links` array is inspected
**THEN** at least one link with `rel=canonical` is present
**AND** the href equals `{api_root}/properties/{id}`.
*Maps to*: REQ-ETS-PART1-008, OGC requirement `/req/property/canonical-url`.

#### SCENARIO-ETS-PART1-008-PROP-DEPENDENCY-SKIP-001 (CRITICAL — Sprint 7)
**GIVEN** the testng.xml declares `<group name="propertydefinitions" depends-on="systemfeatures"/>`
**WHEN** SystemFeatures group has any FAIL or SKIP
**THEN** all PropertyDefinitionsTests `@Test` methods are SKIPped by TestNG.
*Maps to*: REQ-ETS-PART1-008, ADR-010 v3.

#### SCENARIO-ETS-PART1-008-PROP-SMOKE-NO-REGRESSION-001 (CRITICAL — Sprint 7)
**GIVEN** the TeamEngine + ETS Docker container is running post-Sprint 7
**WHEN** `scripts/smoke-test.sh` executes against GeoRobotix
**THEN** existing ≥38 @Tests (post S-ETS-07-02) all continue to PASS
**AND** ≥4 new PropertyDefinitionsTests @Tests PASS
**AND** total smoke PASS ≥ 42.
*Maps to*: REQ-ETS-PART1-008, REQ-ETS-TEAMENGINE-005.

#### SCENARIO-ETS-WEBAPP-FREEZE-README-001 (NORMAL)
**GIVEN** the `csapi_compliance` repo at HEAD `ab53658` plus the README reposition commit
**WHEN** a reader opens README.md
**THEN** the first non-trivial paragraph identifies the project as a "developer pre-flight tool, not certification-track"
**AND** the README contains a hyperlink to the new ETS repo
**AND** `git tag --list` includes `v1.0-frozen` pointing at `ab53658`.
*Maps to*: REQ-ETS-WEBAPP-FREEZE-001.

#### SCENARIO-ETS-SYNC-URI-DIFF-001 (NORMAL)
**GIVEN** the v1.0 TS registry and the Java ETS each have a non-empty URI coverage list
**WHEN** `scripts/sync-uri-coverage.sh` runs in the local verification gate
**THEN** the script exits 0 if every URI is mirrored on both sides OR has an entry in `ops/uri-coverage-allowlist.txt`
**AND** the script exits non-zero if any URI is unmirrored without an allowlist entry.
*Maps to*: REQ-ETS-SYNC-001.

#### SCENARIO-ETS-SYNC-URI-SCHEMA-DRIFT-AUDIT-001 (NORMAL -- Sprint 39)
**GIVEN** the frozen v1.0 web-app registry/schema bundle and the Java ETS source/schema bundle are both available
**WHEN** the Sprint 39 drift audit runs
**THEN** it emits machine-readable counts for Java-only, web-app-only, allowlisted, missing-schema, extra-schema, and hash-mismatched schema entries
**AND** it can be switched from report-only mode to failing mode after the allowlist is stabilized.
*Maps to*: REQ-ETS-SYNC-001, REQ-ETS-CLEANUP-020.

#### SCENARIO-ETS-CLEANUP-ARTIFACT-HYGIENE-SUMMARY-001 (NORMAL -- Sprint 39)
**GIVEN** one or more archived TeamEngine TestNG XML reports and smoke container logs
**WHEN** the Sprint 39 artifact hygiene report runs
**THEN** it reports TestNG totals, request-log counts, IUT-bound method counts, write-method counts, and credential-scan counts in JSON
**AND** it exits non-zero when configured read-only IUT-bound writes or credential leaks are found.
*Maps to*: REQ-ETS-CLEANUP-020, REQ-ETS-TEAMENGINE-006.

#### SCENARIO-ETS-CLEANUP-ARTIFACT-CREDENTIAL-SCAN-001 (CRITICAL -- Sprint 39)
**GIVEN** smoke artifacts include masked Authorization headers and may include configured local OSH credential values
**WHEN** the Sprint 39 artifact hygiene report scans those files
**THEN** masked Authorization headers are accepted
**AND** unmasked Authorization headers or explicitly supplied secret values are counted as leaks without printing the secret value.
*Maps to*: REQ-ETS-CLEANUP-020, PRD FR-ETS-25.

#### SCENARIO-ETS-CLEANUP-CONFIDENTIAL-BUILD-CONTEXT-001 (CRITICAL -- Sprint 41)
**GIVEN** locally supplied OGC reference files may exist beside the repository
**WHEN** Sprint 41 runs tracked-file, history, and effective Docker build-context hygiene checks
**THEN** no protected reference file is tracked or sent in the Docker build context
**AND** the check reports only filenames/counts needed for audit and never prints protected contents
**AND** unrelated `f10m.xml` scratch material is absent unless an explicit safe-fixture purpose is specified.
*Maps to*: REQ-ETS-CLEANUP-021.

#### SCENARIO-ETS-CLEANUP-CODEX-SESSION-METRICS-001 (NORMAL -- Sprint 66 follow-up)
**GIVEN** the current checkout is running under Codex and has rollout JSONL
records under `~/.codex/sessions` or `~/.codex/archived_sessions`
**WHEN** `python3 scripts/session-metrics.py` runs without an explicit path
**THEN** it selects the newest main-thread Codex rollout whose metadata `cwd`
matches the checkout
**AND** it reports input, output, cache-write, and cache-read totals from
`payload.info.last_token_usage` token-count records without treating
cumulative `total_token_usage` snapshots as separate API calls
**AND** `python3 scripts/session-metrics.py --self-test` verifies both Claude
and Codex JSONL parsing.
*Maps to*: REQ-ETS-CLEANUP-022.

## Implementation Status (2026-04-28)

**Status**: Sprint 1 / S-ETS-01-01 ✅ PASS at `Botts-Innovative-Research/ets-ogcapi-connectedsystems10` HEAD `1323884` (29 commits). Quinn (Gate 3.5) APPROVE_WITH_GAPS 0.88; Raze (Gate 4) GAPS_FOUND 0.84 — both gates' 3 doc gaps closed same-turn 2026-04-28T16:30Z. S-ETS-01-02 (CS API Core conformance class) and S-ETS-01-03 (TeamEngine Docker smoke) are the remaining stories in Sprint 1 contract `.harness/contracts/sprint-ets-01.yaml`.

### What's Built (Sprint ets-01 / S-ETS-01-01)

**Sub-deliverable 1 — Maven Archetype Scaffold** (REQ-ETS-SCAFFOLD-001..007, Implemented):
- REQ-ETS-SCAFFOLD-001: Archetype generated from `org.opengis.cite:ets-archetype-testng:2.7` with ADR-003 coordinates (artifactId `ets-ogcapi-connectedsystems10`, ets-code `ogcapi-connectedsystems10`, package `org.opengis.cite.ogcapiconnectedsystems10`). Generation command recorded in new repo's `ops/server.md`.
- REQ-ETS-SCAFFOLD-002: `<maven.compiler.source/target/release>17</>` set; Maven 3.9 enforced via inherited ets-common:17 maven-enforcer config.
- REQ-ETS-SCAFFOLD-003: Repo layout matches features10 archetype-flat structure. **PARTIAL caveat**: features10's `java17Tomcat10TeamEngine6` branch refactored to `listener/`+`conformance/` subpackages — that refactor is deferred to S-ETS-01-02 when real Core test classes need the subpackages.
- REQ-ETS-SCAFFOLD-004: All deps pinned (no `RELEASE`/`LATEST`). ets-common:17 manages testng, rest-assured, openapi-parser, jts-core, proj4j, jts-io-common, slf4j-api, schema-utils. logback-classic 1.5.18 explicit (not in ets-common's depMgmt).
- REQ-ETS-SCAFFOLD-005: Reproducible build verified. sha256 `fe1c90c54537facf73ddd5172deec4b866e0071eae78834606bf92b229746385` — verified across 7 independent builds (Quinn 3 + Raze 4) including two fresh-clone builds in `/tmp/`. ADR-004 C-5 plumbing: `<project.build.outputTimestamp>2026-04-27T00:00:00Z</>` + manifest `Build-Time` override.
- REQ-ETS-SCAFFOLD-006: 5 ADRs at `_bmad/adrs/ADR-001..005` cover SPI registration, schema bundling, package naming, archetype modernization checklist, cross-repo relationship. 16 of 28 modernization commits cite ADR rows; 12 are legitimate non-ADR work (archetype baseline, SCM rewrite, formatting, Jersey/Jakarta port — Raze CONCERN-1 suggests an optional ADR-006 for the Jersey port; deferred to Sprint 2).
- REQ-ETS-SCAFFOLD-007: Repo lives at `Botts-Innovative-Research/ets-ogcapi-connectedsystems10` per ADR-005 "our org first" gate.

**Sub-deliverable 2 — JSON Schema Bundle** (REQ-ETS-FIXTURES-001 admin-deferred; ADR-002 verbatim copy live):
- 126 JSON Schemas under `src/main/resources/schemas/` byte-identical to `csapi_compliance@ab53658/schemas/` (`diff -r` empty, verified by Quinn + Raze).
- pom.xml `<connected-systems-yaml.sha>3fd86c73e744b7e2faaf7f1c17366bfb9ff4cd6f</>` per ADR-002 mandate (commit `1323884`). Schema-provenance audit trail in new repo's `ops/server.md`.

**Sub-deliverable 3 — CS API Core conformance class** (REQ-ETS-CORE-001..004, Implemented S-ETS-01-02):
- REQ-ETS-CORE-001: TestNG suite-fixture plumbing live in `CommonFixture` + `listener.SuiteFixtureListener` (commit `b6a9c12` in new repo). REST-Assured request/response capture wired via `getRequest()`/`getResponse()`; IUT URL stash via SuiteAttribute enum.
- REQ-ETS-CORE-002: `LandingPageTests` (`conformance.core.LandingPageTests` in new repo, commit `990c850`) — 6 @Test methods. **v1.0 GH#3 fix preserved** via sentinel @Test `landingPageDoesNotRequireSelfRel` (LandingPageTests:204, asserts both presence and absence of `rel=self` are PASS — Raze independently verified the assertion logic). **API-definition fallback preserved** via `landingPageHasApiDefinitionLink` (LandingPageTests:179, PASSES on `service-desc` OR `service-doc`, FAILS only when both absent — Raze verified). All 6 PASS against GeoRobotix.
- REQ-ETS-CORE-003: `ConformanceTests` (commit `ea59436`) — 4 @Test methods asserting GET /conformance HTTP 200 + JSON + non-empty `conformsTo` array + explicit declaration of `http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/core`. All 4 PASS against GeoRobotix.
- REQ-ETS-CORE-004: `ResourceShapeTests` Sprint-1-minimal (commit `b249aa1` + URI fix `1fdfe07`) — 2 @Test methods: api-definition link resolves to non-empty content + /conformance body shape is JSON object. Full id/type/links crawl deferred to Sprint 2 per design.md "single representative resource" pattern. **Note**: a copy-paste URI typo (`ogcapi-common-2/0.0/req/oas30/oas-impl` — Common Part 2, OGC 20-024) caught by Raze GAP-3 was corrected to `ogcapi-common-1/1.0/req/oas30/oas-impl` (Common Part 1, OGC 19-072 — the standard Sprint 1 actually targets) in commit `1fdfe07`.

**Sub-deliverable 5 — TeamEngine Integration** (REQ-ETS-TEAMENGINE-001..008; TeamEngine 5.6.1 retained as historical baseline, TeamEngine 6 replacement implemented and final-Raze approved under S-ETS-41-01):
- REQ-ETS-TEAMENGINE-001: META-INF/services SPI registration file (58 bytes, single-line FQCN `org.opengis.cite.ogcapiconnectedsystems10.TestNGController`, no whitespace, no extension) — verified by Quinn s01 + Raze s01/s02 + S-ETS-01-03 smoke runtime.
- REQ-ETS-TEAMENGINE-002: CTL wrapper at `src/main/scripts/ctl/ogcapi-connectedsystems10-suite.ctl` from archetype. **CTL Saxon namespace verified clean** (architect-handoff S-ETS-01-03 CONCERNS pitfall #3 — silent failure mode): `xmlns:tng="java:org.opengis.cite.ogcapiconnectedsystems10.TestNGController"` is the canonical run-together ADR-003 form, no `cs10` typo. Runtime corroboration: 12/12 PASS via SPI-routed smoke confirms TeamEngine successfully loaded the CTL. Sprint 41 policy-guidance remediation updates the contract to canonical args `iut`, `auth-credential`, `mutation-tests-enabled`, and `mutation-iut-policy` across CTL, TestNG defaults, smoke forwarding, README, site docs, Javadoc, and sample props. Focused structural verification passed `9/0/0/0`; full Docker Maven passed `303/0/0/3`; readiness Compose and advisory GeoRobotix runs verified TeamEngine 6 suite registration with the corrected full title.
- REQ-ETS-TEAMENGINE-003: **IMPLEMENTED FOR TEAMENGINE 6**. The manual TeamEngine 5.6.1/JDK 17 path remains historical baseline evidence. Sprint 41 verifies the immutable TeamEngine 6.0.0 digest, isolated shaded dependencies/resources, filename-independent added-jar coordinate/content checks, byte-for-byte base immutability, image build, non-root startup, SPI/CTL registration, and primary local OSH execution. Fresh full Docker Maven passed `312/0/0/3`; exact image `sha256:829a97414c07dd5763ed302e32b3178d301ca098bc9025f4b1f58b692ddad5f9` passed runtime and local OSH E2E `211/69/0/142`; final Raze approved.
- REQ-ETS-TEAMENGINE-004: **IMPLEMENTED FOR TEAMENGINE 6**. `docker-compose.yml` retains the canonical `8081:8080` mapping and runtime healthcheck while using the digest-pinned repository Dockerfile. Sprint 41 readiness verified Compose build, health, and suite metadata; `ops/server.md` documents host-port fallback and amd64 emulation requirements.
- REQ-ETS-TEAMENGINE-005: **IMPLEMENTED FOR TEAMENGINE 6**. `scripts/smoke-test.sh` builds and launches the exact image, verifies suite metadata, runs a non-empty zero-failure TestNG report, enforces the no-mutation oracle, and scans startup errors. The 2026-07-22 primary local OSH run passed `211/69/0/142`, recognized 135 IUT requests, and recorded zero writes and zero startup errors.
- REQ-ETS-TEAMENGINE-007: **IMPLEMENTED** after inert OGC release-definition and exact multi-tuple gapfix on 2026-07-22. The replacement payload adds only the shaded ETS jar and CTL resources. Generic inventory proves one added jar, no duplicate base or TeamEngine coordinate, emits both exact allowlisted TeamEngine convention paths, and rejects unused entries; its self-test asserts a complete exact two-tuple set. Both Jenkinsfiles pass Java 17/bootstrap/declared-profile checks and remain unconnected metadata. Exact image `sha256:829a97414c07dd5763ed302e32b3178d301ca098bc9025f4b1f58b692ddad5f9` passed runtime verification and primary local OSH with `211/69/0/142`, 135 recognized requests, zero writes, and zero startup errors. Final Raze approved at `0.99` confidence with no required actions.
- REQ-ETS-TEAMENGINE-008: **IMPLEMENTED** by Sprint 41 closure. Public package metadata/docs, Maven-derived suite metadata, canonical run arguments, structural verification, Compose metadata, and primary local OSH TeamEngine E2E are verified without archetype placeholders.

**Sprint 1 contract success_criteria walk after S-ETS-01-03**: **9/9 PASS** (per Dana's S-ETS-01-03 generator report) — all 5 critical scenarios PASS (SCAFFOLD-BUILD-001, CORE-LANDING-001, CORE-CONFORMANCE-001, TEAMENGINE-LOAD-001, CORE-SMOKE-001), all 5 normal scenarios PASS (SCAFFOLD-LAYOUT-001, SCAFFOLD-REPRODUCIBLE-001, CORE-RESOURCE-SHAPE-001, CORE-LINKS-NORMATIVE-001, CORE-API-DEF-FALLBACK-001). **Sprint 1 functionally complete pending Quinn+Raze gate close on S-ETS-01-03.**

**Sub-deliverable 3 (cont.) — Common conformance class** (REQ-ETS-PART1-001, historical increment complete; released ATS partial/unreviewed):
- REQ-ETS-PART1-001: `conformance.common.CommonTests` 4 @Test methods (Sprint-1-style minimal-then-expand per architect-handoff item 17 — distinct surface from Core to avoid duplication) all PASS against GeoRobotix at HEAD commit `c56df10` (new repo). Smoke total = 22/22 (12 Core + 6 SystemFeatures + 4 Common). 2 commits at new repo: `f384509` (CommonTests + testng.xml single-block consolidation extension), `c56df10` (live smoke evidence + nested-properties fix in S-ETS-03-05). Common is INDEPENDENT of Core (no dependsOnGroups declaration on the common group); runs in parallel. URI canonical form: `/req/json/{definition,content}` (Common Part 1 JSON encoding class), `/req/landing-page/conformance-success` (reused at Common-class layer to assert `ogcapi-common-1/1.0/conf/core` IS declared in `/conformance` body), `/req/collections/collections-list-success` (Common Part 2). All 4 .adoc URLs HTTP-200-verified at `raw.githubusercontent.com/opengeospatial/ogcapi-common/master/{19-072,collections}/requirements/`. ETSAssert helpers throughout; zero new bare-throw sites. GeoRobotix curl evidence: `/conformance` declares `ogcapi-common-1/1.0/conf/core` AND `ogcapi-common-2/0.0/conf/collections`; `/collections` returns 200 with `id="all_systems"` entry; `?f=json` returns JSON; `?f=html` returns 400 (acceptable per content-negotiation discipline — IUT explicitly handles parameter). Full curl evidence + URI mapping archived in `epics/stories/s-ets-03-07-common-conformance-class.md` Implementation Notes.

**Sub-deliverable 3 (cont.) — SystemFeatures conformance class**
(REQ-ETS-PART1-002, released ATS implemented; final Raze approved):
- Sprint 47 replaces the historical six-method approximation with six exact
  released `/conf/system` procedures. The direct tests independently retrieve
  their prerequisites and cover warning-only canonical location inspection,
  explicit moving-System location polling, every-item canonical dereference and
  normalized content comparison, endpoint-parameterized resources validation,
  canonical `/systems` validation, and every exact System collection's released
  type/schema checks. Actual GeoJSON/SensorML media types select their bundled
  collection schemas. Four pinned complete `geojson.org` dependencies replace
  permissive placeholders and carry source URL/date/SHA-256 provenance.
  Unsupported collections cannot hide later supported failures; SensorML
  orientation-only changes cannot prove movement; missing `mobile-system-id`
  remains an evidence-honest SKIP.
- Reviewed coverage is `6/6 exact` for `/conf/system` and
  `10 exact / 2 helper / 145 candidate / 83 unmapped` overall. Focused Maven is
  `46/0/0/0`, the released-ATS audit is `23/0/0/0`, full Docker Maven is
  `395/0/0/3`, and exact image
  `sha256:101e20653097fea9891ff5fbe1f4c160ae163ca97338cf63cfb5980dd958cf6e`
  passes runtime verification. Primary local OSH TeamEngine is
  `215/38/0/177`, with 105 recognized GET requests, zero writes, and zero
  startup errors. All six System methods execute: three PASS and three retain
  explicit unsupported-media or missing-input SKIPs. The controlled HTTP
  fixture proves successful positive paths for all six methods. Dependency
  sabotage and both credential gates pass.
- Historical Sprint 2/3 implementation and advisory GeoRobotix evidence remain
  archived in `epics/stories/s-ets-02-06-systemfeatures-conformance-class.md`;
  they no longer establish released ATS completion.

**Sub-deliverable 3 (cont.) - Subsystem conformance class**
(REQ-ETS-PART1-003, released ATS implemented; final Raze approved):
- Sprint 48 replaces the historical four-method canonical-shape/link
  approximation with the five released `/conf/subsystem` procedures.
  Independent bounded direct-edge discovery rejects duplicate IDs, pagination
  and hierarchy cycles, and shortcut overlap. Collection discovery requires
  exactly one exact `rel=subsystems` URI and validates successful supported
  GeoJSON or SensorML pages through the existing System schemas. Recursive
  boolean requests are status-only. Association implementation is established
  independently at the top-level resource endpoint before every parent and
  descendant endpoint is required.
- Reviewed coverage is `5/5 exact` for `/conf/subsystem` and
  `15 exact / 2 helper / 144 candidate / 79 unmapped` overall. The Raze
  gap-fix gate reproduced seven expected failures; its first recheck exposed
  three additional nested first/later-page media failures at `7/3/0/0`; final
  review exposed three root first/later-page failures at `10/3/0/0`. Focused
  Maven is `45/0/0/0` and full Docker Maven is `417/0/0/3`. Exact image
  `sha256:32a43f81b441f3b687b9e83d9d6688016278f4f7a5fec5d8a3c2b174490f285c`
  passes runtime verification. Primary local OSH TeamEngine is
  `216/39/0/177`, with 109 recognized requests, zero writes, and zero startup
  errors. All five methods execute: recursive-param passes and the four
  hierarchy procedures skip because the root System collection uses unsupported
  `application/json`.
  Controlled HTTP regressions execute every successful path. SystemFeatures
  sabotage makes all five Subsystem methods skip, and both credential gates
  pass. Final Raze is `APPROVE_WITH_CONCERNS` at confidence `0.99`, with every
  required finding closed.
- The local OSH checkout is clean and zero commits ahead of upstream, `/opt/osh`
  is mounted read-only, and the deployed ConSys jar manifest matches checkout
  `4c87a65`. No OSH or TeamEngine source or binary was modified.
- Historical Sprint 4 implementation evidence remains audit history only and
  no longer establishes released ATS completion.

**Sub-deliverable 8 — Web-App Freeze**: REQ-ETS-WEBAPP-FREEZE-001 ✅ closed (commit `44c279e`, tag `v1.0-frozen` at `ab53658`). README.adoc reverse cross-link in new repo closes ADR-005 "both directions" requirement.

### Deviations from Spec
- **Java root package, artifactId, ets-code, CTL filename, ets-common version, TeamEngine version**: spec text was reconciled to ADR-003/ADR-004/ADR-001 authority on 2026-04-28T14:42Z (commit `19003b1`). Spec now matches what Generator implemented.
- **Layout refactor closed in S-ETS-01-02**: archetype-flat layout retained through S-ETS-01-01; refactored to `conformance.core.*` + `listener.*` subpackages in S-ETS-01-02 commit `2dc4414`. Closes Quinn+Raze CONCERN-3 from S-ETS-01-01 gate close.
- **Kaizen openapi-parser declared but not consumed in Sprint 1**: per architect-handoff `surfaced_risks_pat_missed.OPENAPI-PARSER-NOT-USED-IN-SPRINT-1`, Sprint 1 Core uses everit-json-schema (transitive via ets-common:17) directly. Kaizen is on the dep list for Sprint 2+ when richer Part 1 classes need OpenAPI-driven validation.
- **GitHub Actions workflow path retired**: CP-003 removes the dormant workflow and all activation instructions because hosted CI is outside project scope. Historical deferral evidence remains audit-only.
- **Bare `throw new AssertionError(...)` instead of `EtsAssert` helper** (architect-handoff `must` constraint #9): 21 call sites across the 3 Core test classes use bare `throw new AssertionError(URI + " — message")` rather than an `EtsAssert.failWithUri(...)` helper. **Intent met** (every FAIL message includes the canonical `/req/*` URI as required); **form violated** (no helper used). The existing `ETSAssert.java` is XML/Schematron-only and Dana didn't extend it. Tracked as Quinn GAP-1 / Raze GAP-1 (both s02). **Sprint 2 cleanup**: extend `ETSAssert` with a `failWithUri(String message, String uri)` overload and refactor the 21 call sites mechanically.
- **URI form drift between v1.0 TS, Java port, and OGC canonical** (Quinn GAP-2 / Raze GAP-2 in s02 reports): Java cites `/req/core/root-success`; v1.0 TS uses `/req/ogcapi-common/landing-page`; OGC's normative .adoc canonical (verified by Raze upstream-fetch 2026-04-17) is `/req/landing-page/root-success`. Three different forms all citing the same correct normative text, but a CITE SC reviewer dereferencing the @Test description URIs against the OGC normative document will get a 404. **Source is upstream of S-ETS-01-02** (spec.md text already used the `/req/core/<X>-success` form when Dana implemented). **Sprint 2 cleanup**: amend spec.md + traceability.md + Java @Test descriptions to the OGC canonical `.adoc` URI form; ~30-40 sites across both repos.

### Deferred
- REQ-ETS-TEAMENGINE-002..005 (Dockerfile, docker-compose, smoke-test.sh, container-load verification) → S-ETS-01-03 (final Sprint 1 story).
- REQ-ETS-PART1-001..013 (per-class detail beyond Core) — drafted as placeholders; per-assertion FRs and SCENARIOs to be expanded in sprints 2..N.
- REQ-ETS-PART2-002 (Datastreams & Observations) — implemented released ATS in Sprint 60; `2:/conf/datastream` is `14 exact / 0 candidate / 0 unmapped`.
- REQ-ETS-PART2-003 (Control Streams & Commands) — implemented released ATS in Sprint 61; `2:/conf/controlstream` is `18 exact / 0 candidate / 0 unmapped`.
- REQ-ETS-PART2-004 (Command Feasibility) — implemented released ATS exact in Sprint 62; `2:/conf/feasibility` is `5 exact / 0 candidate / 0 unmapped`.
- REQ-ETS-PART2-005 (System Events) — implemented released ATS exact in Sprint
  63; `2:/conf/system-event` is `5 exact / 0 candidate / 0 unmapped`.
- REQ-ETS-PART2-006: partially implemented by Sprint 25 Advanced Filtering Generator.
- REQ-ETS-PART2-007 (Part 2 Create/Replace/Delete) - partially implemented by Sprint 26 Generator; seeded local OSH E2E is accepted after fixture repair, while GeoRobotix public smoke remains advisory and currently fails with public-IUT HTTP 500 responses outside the new Part 2 CRD tests.
- REQ-ETS-PART2-008 (Part 2 Update) - partially implemented by Sprint 27 Generator; positive PATCH lifecycle and concrete schema-rejection dispatch remain deferred.
- REQ-ETS-PART2-009 (Part 2 JSON Encoding) - Sprint 65 CP-025 supersedes the
  Sprint 28 subset with 14/14 exact released Annex A.9 mappings, scoped
  all-operation JSON write-advertisement checks, and honest E2E SKIP behavior
  when a declaring IUT lacks the SWE JSON record-components prerequisite or
  candidate resource evidence.
- REQ-ETS-PART2-010 (Part 2 SWE Common JSON Encoding) - Sprint 66 supersedes Sprint 29 with exact reviewed released ATS closure and final Raze approval: eight Annex A.10 procedures, no standalone helper procedures, exact `application/swe+json` read evidence, bundled wrapper plus reusable SWE `recordSchema` validation, canonical Time/IssueTime mapping evidence, honest Observation/Command encoding SKIPs without proven data-value validator evidence, and non-mutating API-definition write checks requiring every advertised scoped POST/PUT operation to include exact `application/swe+json`. Coverage is `8 exact / 0 candidate / 0 unmapped`; focused Docker Maven passed `114/0/0/0`; full Docker Maven retry passed `770/0/0/3` after an initial dependency-transfer failure; mandatory local OSH TeamEngine E2E is honestly non-green at `256 total / 27 passed / 20 failed / 209 skipped`, with all eight Sprint 66 methods SKIPping before SWE Common JSON resource endpoint access because local OSH lacks `http://www.opengis.net/spec/SWE/3.0/conf/json-encoding-rules`; no-mutation evidence is `GET=144`, zero POST/PUT/PATCH/DELETE; final Raze is `APPROVE 0.96` with no required fixes; implementation commit `6e98ac9` is pushed.
- REQ-ETS-PART2-011 (Part 2 SWE Common Text Encoding) - Sprint 67 supersedes Sprint 30 with exact reviewed released ATS closure: eight Annex A.11 procedures, no standalone helper procedures, exact `application/swe+text` read evidence, bundled wrapper plus reusable SWE `recordSchema` validation, canonical Time/IssueTime mapping evidence, present-noncanonical IssueTime mapping failure, honest Observation/Command encoding SKIPs without proven data-value validator evidence, and non-mutating API-definition write checks requiring every advertised scoped POST/PUT operation to include exact `application/swe+text`. Coverage is `8 exact / 0 candidate / 0 unmapped`; post-Raze-fix focused Docker Maven passed `115/0/0/0`; full Docker Maven completed `775/0/0/3`; mandatory local OSH TeamEngine E2E is honestly non-green at `254 total / 25 passed / 20 failed / 209 skipped`, with all eight Sprint 67 methods SKIPping before SWE Common Text resource endpoint access because local OSH lacks `http://www.opengis.net/spec/SWE/3.0/conf/text-encoding-rules`; no-mutation evidence is `GET=137`, zero POST/PUT/PATCH/DELETE; final Raze recheck is `APPROVE 0.96` with no required fixes; implementation commit `5f0a3f6` is pushed.
- REQ-ETS-PART2-012 (Part 2 SWE Common Binary Encoding) - partially implemented by Sprint 31 Generator. `Part2SweCommonBinaryTests` implements exact `/conf/swecommon-binary` declaration gating, SWE Common 3.0 `/conf/binary-encoding-rules` prerequisite visibility, `/conf/datastream`, `/conf/controlstream`, and `/conf/create-replace-delete` condition gates, exact `application/swe+binary` read checks, bundled `observationSchemaSwe.json`/`commandSchemaSwe.json` metadata validation with `BinaryEncoding`, canonical Time/IssueTime mapping evidence, Observation/Command encoding guards, and non-mutating API-definition mediatype-write checks. Maven verification succeeded (`272 tests / 0 failures / 0 errors / 3 skipped`). Mandatory GeoRobotix Generator smoke failed (`206 total / 35 passed / 34 failed / 137 skipped`); the new SWE Common Binary group produced 3 PASS, 6 FAIL, and 2 SKIP, with no public-IUT mutation (`GET 99`, `POST/PUT/PATCH/DELETE 0`).
- REQ-ETS-PART2-013 (Observation/Command binding cross-class closure) - partially implemented by Sprint 32 Generator as an internal project closure item, not a standalone OGC `/conf/observation-binding` class. `Part2ObservationCommandBindingTests` and helper regressions are implemented with group `part2binding`, no default fixture seeding, and GET-only positive closure guards. Sprints 33-38 add inline status/result regressions, local OSH seed/tasking fixture evidence, parent schema `f=json` request shaping, stream metadata and format assertions, and populated parent-candidate selection. Sprint 40's external OSH patch is retained as historical audit evidence only and is not an approved implementation path under CP-003/ADR-012. Full positive populated-IUT binding closure remains unclaimed and must proceed through ETS changes or an unmodified IUT.
- REQ-ETS-VALIDATOR-001 (External SWE Common and SensorML Validator Integration) - IMPLEMENTED. The SWE Common source-pinned adapter remains final-Raze approved, and Sprint 58 completes the provisional SensorML adapter over the pinned released schema graph. The backend-neutral SensorML API returns immutable deterministic ETS-owned diagnostics, keeps operational failures separate, validates all eight entry schemas, and is replaceable by a future reproducible FCU/OGC module without changing TestNG procedures or Connected Systems mappings. Exact candidate `a593953d8d79d977649db3077696148e90ffb44a` passes full Maven `729/0/0/3`, 8-entry/63-transitive schema parity, exact-image adapter and external-fetch security probes, unmodified-local-OSH E2E with zero writes, and final Raze `APPROVED 0.99`.
- REQ-ETS-TEAMENGINE-006 (Local OSH Primary Development Target) - specified by Sprint 32 planning. Development E2E uses the self-provisioned local OSH IUT on `field-hub_default`; GeoRobotix is advisory-only and no longer the default development target.
- REQ-ETS-FIXTURES-001..003 (spec-trap port from `csapi_compliance/tests/fixtures/spec-traps/`) → epic-ets-06 parallel sprint after Sprint 1 closes.
- REQ-ETS-CITE-001..003 — calendar-bound, not sprint-bound. Beta milestone gates these.
- REQ-ETS-SYNC-001 — local audit-script work, expected after Part 1 is feature-complete enough to make the diff meaningful.
- HTTP request/response capture (full REST Assured logging-filter pattern) → Sprint 2.
- Auth credential masking + `logback.xml` (architect-handoff `should` #3 — never log Authorization/X-API-Key) → Sprint 2 (no auth path exercised in Sprint 1; GeoRobotix is open).
- JaCoCo ≥80% coverage instrumentation → Sprint 2.

### Gate verdicts (audit trail)
- **Gate 3.5 (Quinn / Evaluator) for S-ETS-01-01**: APPROVE_WITH_GAPS confidence 0.88. Report at `.harness/evaluations/sprint-ets-01-evaluator.yaml`. 3 gaps + 4 concerns — all gaps closed same-turn 2026-04-28T16:30Z.
- **Gate 4 (Raze / Adversarial) for S-ETS-01-01**: GAPS_FOUND confidence 0.84. Report at `.harness/evaluations/sprint-ets-01-adversarial.yaml`. 3 gaps + 3 concerns — same 3 gaps Quinn caught (cross-corroborating). All closed same-turn.
- **Gate 3.5 (Quinn / Evaluator) for S-ETS-01-02**: APPROVE_WITH_GAPS confidence 0.85. Report at `.harness/evaluations/sprint-ets-01-evaluator-s02.yaml`. 3 gaps + 4 concerns. GAP-3 (spec.md reconcile pending) closed by this commit; GAP-1 (EtsAssert) + GAP-2 (URI form drift) deferred to Sprint 2 cleanup with explicit notes above.
- **Gate 4 (Raze / Adversarial) for S-ETS-01-02**: GAPS_FOUND confidence 0.82. Report at `.harness/evaluations/sprint-ets-01-adversarial-s02.yaml`. 3 gaps + 3 concerns — same 3 gaps Quinn caught (cross-corroborating, 2nd consecutive sprint). GAP-3 (Common Part 2 → Part 1 URI typo in `ResourceShapeTests`) closed by new repo commit `1fdfe07`. CONCERN-1 (Dana's reported sha256 `c4a80294...` was at HEAD `b249aa1`; canonical Sprint-1-close hash at `ea2c91f` is `b1ffdc8eee...` per Raze independent verification — buildnumber-maven-plugin embeds commit SHA in manifest, so per-commit hash variance is expected metadata-only) — narrative clarified in ops/status.md and ops/changelog.md this turn. CONCERN-3 (logback.xml + CredentialMaskingFilter) Sprint 2 scope.
