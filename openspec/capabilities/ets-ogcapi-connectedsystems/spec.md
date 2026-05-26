# OGC API Connected Systems ETS — Specification

> Version: 1.0 | Status: Active ETS implementation | Last updated: 2026-05-26
>
> **Capability scope**: A Java/TestNG Executable Test Suite for OGC TeamEngine that validates
> conformance against OGC 23-001 (Part 1: Feature Resources) and OGC 23-002 (Part 2: Dynamic Data),
> packaged as the certification-track deliverable for OGC CITE submission. Supersedes the v1.0
> web-app capabilities (`endpoint-discovery`, `conformance-testing`, `dynamic-data-testing`,
> `test-engine`, `request-capture`, `reporting`, `export`, `progress-session`), all of which are
> now `Frozen — v1.0 web app, superseded by ets-ogcapi-connectedsystems`.

## Purpose

This capability defines an OGC-compliant Executable Test Suite (ETS) for the OGC API – Connected Systems standard. The ETS is generated from `org.opengis.cite:ets-archetype-testng:2.7`, runs inside TeamEngine 5.6.x (currently 5.6.1), and produces a per-conformance-class pass/fail/skip verdict against an Implementation Under Test (IUT) supplied as a CS API landing-page URL. The deliverable maps to PRD v2.0 functional requirements FR-ETS-01 through FR-ETS-90.

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
- **Description**: `mvn clean install` SHALL exit 0 on a clean checkout with JDK 17 and Maven 3.9. Two builds from the same commit SHALL produce byte-identical jars excluding `META-INF/` timestamps. CI SHALL verify via a double-build diff job.
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

### Sub-deliverable 3 — Other Part 1 Conformance Classes

> Sprint 2 expands REQ-ETS-PART1-002 (SystemFeatures) from PLACEHOLDER → SPECIFIED.
> The remaining 12 placeholder REQs below establish the certification surface and traceability chain.

#### REQ-ETS-PART1-001: Common Conformance Class (Sprint 3 target)
- **Priority**: MUST
- **Status**: IMPLEMENTED (Sprint 3, S-ETS-03-07; pending Quinn+Raze gate close — 4 @Tests PASS against GeoRobotix at smoke commit `c56df10`)
- **Description**: For each assertion in OGC 23-001 Annex A `/conf/common/`, the ETS SHALL provide at least one TestNG `@Test` method whose `description` attribute starts with the OGC canonical `.adoc` requirement URI form. Generator MUST verify the canonical form via OGC `.adoc` source HTTP-200 fetch BEFORE writing assertions (continuing the S-ETS-02-03 / S-ETS-02-06 URI-canonicalization discipline; the form may be `/req/common/<X>` or may follow Common Part 1's existing `/req/landing-page/<X>` / `/req/oas30/<X>` / `/req/json/<X>` etc subdirectory pattern depending on what the OGC 19-072 + 23-001 Annex A actually specify). Expected sub-requirements: (a) Common landing-page link discipline beyond Core's subset (e.g. `rel=conformance` mandatory; `rel=data` or `rel=collections` if collections endpoint exists); (b) Common conformance enumeration (`conformsTo` includes Common's classes); (c) `/collections` endpoint shape per `/req/common/collections` (SKIP-with-reason if IUT returns 404 — GeoRobotix may not implement); (d) content-negotiation discipline via `f=json` / `f=html` query parameter per `/req/common/content-negotiation`. The Common class lives at `org.opengis.cite.ogcapiconnectedsystems10.conformance.common.CommonTests` per design.md placeholder. Common is INDEPENDENT of Core — same DAG-root level — and runs in parallel (no `dependsOnGroups` declaration on the `common` group). Coverage scope at Sprint 3 close: Sprint-1-style minimal (4 @Tests covering 4 highest-priority assertions per Architect ratification — see design.md Sprint 3 ratifications); Sprint 4+ expansion adds 1-3 remaining ATS items + parameter validation + paging discipline.
- **Rationale**: Common is foundational — every remaining 11 Part 1 class (Subsystems, Procedures, Sampling, Properties, Deployments, AdvancedFiltering, CRUD, Update, Subdeployments, GeoJSON, SensorML) inherits from Common's base assertions. Highest dependency-leverage of any single class; landing Common in Sprint 3 unlocks Sprint 4+ remaining classes cleanly.
- **Maps to**: PRD FR-ETS-11.

#### REQ-ETS-PART1-002: SystemFeatures Conformance Class (Sprint 2 target — extended Sprint 3)
- **Priority**: MUST
- **Status**: IMPLEMENTED (Sprint 2 close, S-ETS-02-06, Quinn 0.96 + Raze 0.92; Sprint 3 extended with /req/system/collections + /req/system/location-time via S-ETS-03-05 — 6 @Tests total now PASS against GeoRobotix at smoke commit `c56df10`, pending Quinn+Raze)
- **Description**: For each assertion in OGC 23-001 Annex A `/conf/system/`, the ETS SHALL provide at least one TestNG `@Test` method whose `description` attribute starts with the OGC canonical `.adoc` requirement URI form `/req/system/<assertion>` (e.g. `OGC-23-001 .../req/system/resources-endpoint`). **URI form reconciled 2026-04-28T23:35Z**: design.md text and Sprint 2 contract used `/conf/system-features/` and `/req/system-features/<X>`; OGC `.adoc` canonical source uses `/conf/system` (singular, no `-features` suffix) and `/req/system/<X>`. The 5 sub-requirement `.adoc` files at `raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/system/` (HTTP-200-verified by Generator at S-ETS-02-06): `req_resources_endpoint.adoc`, `req_canonical_url.adoc`, `req_canonical_endpoint.adoc`, `req_collections.adoc`, `req_location_time.adoc`. The IUT (GeoRobotix) also declares `/conf/system` in `/conformance` — same form. v1.0 registry `csapi_compliance/src/engine/registry/system-features.ts` uses `/req/system/<X>`. Same drift class as S-ETS-02-03's `/req/core/*` → `/req/landing-page/*` correction. The class lives at `org.opengis.cite.ogcapiconnectedsystems10.conformance.systemfeatures.SystemFeaturesTests` per design.md placeholder. Required behaviors: (a) `GET /systems` returns HTTP 200 with JSON body containing a non-empty `items` array (per OGC API – Features clause 7.15.2-7.15.8 inherited via `/req/system/resources-endpoint`); (b) `GET /systems/{id}` returns the canonical single-item shape — `id` (string), `type` (string), `links` (array per REQ-ETS-CORE-004 base shape) — per `/req/system/canonical-endpoint`; (c) `/systems/{id}` `links` array contains `rel="canonical"` per `/req/system/canonical-url` — absence of `rel="self"` is NOT FAIL (carries v1.0 GH#3 fix policy from Core landing page; v1.0 audit at `system-features.ts:36-44`); (d) the SystemFeatures class declares TestNG suite-level dependency on Core via group dependency wiring (`<dependencies><group name="systemfeatures" depends-on="core"/>`) so SystemFeatures @Tests SKIP gracefully if Core FAILs. Coverage scope: Sprint-1-style minimal (4 @Tests) at Sprint 2 close per Architect ratification (design.md §"SystemFeatures conformance class scope"); Sprint 3 expansion adds `/req/system/collections` + `/req/system/location-time` + pagination/filter coverage.
- **Rationale**: SystemFeatures is the foundational CS API collection — every other CS API endpoint exposes `/systems` collections, so the patterns established here (collection shape, item shape, dependency-skip wiring) propagate to Subsystems, Procedures, Sampling, Properties, Deployments. GeoRobotix serves a non-empty `/systems` collection (36 items confirmed at S-ETS-02-06 curl-verification 2026-04-28T23:30Z, Implementation Notes archive in `epics/stories/s-ets-02-06-systemfeatures-conformance-class.md`).
- **Maps to**: PRD FR-ETS-12.

#### REQ-ETS-PART1-003: Subsystems Conformance Class (Sprint 4 target)

> Sprint 5 Run 1 doc-only amendment (S-ETS-05-04 item A): SubsystemsTests.java class-level javadoc enumeration of OGC `.adoc` files corrected from 5 → 6 (added `req_subcollection_time.adoc` per Raze CONCERN-1) and clarified that the subcollection_time .adoc exists in the GitHub directory but is NOT enumerated in `requirements_class_system_components.adoc`'s `requirement::` list (deferred to Sprint 5+ recursive-* expansion). No behaviour change; status remains IMPLEMENTED.

- **Priority**: MUST
- **Status**: IMPLEMENTED (Sprint 4 Run 2, S-ETS-04-05; pending Quinn+Raze gate close — 4 @Tests PASS against GeoRobotix at smoke commit `2dc44d1`; canonical URI `/req/subsystem/collection` curl-verified at OGC repo; `subsystemHasParentSystemLink` UNIQUE-to-Subsystems invariant verified against subsystem `0nar3cl0tk3g` rel=parent link)
- **Description**: For each assertion in OGC 23-001 Annex A `/conf/subsystem/`, the ETS SHALL provide at least one TestNG `@Test` method whose `description` attribute starts with the OGC canonical `.adoc` requirement URI form `/req/subsystem/<assertion>` (e.g. `OGC-23-001 .../req/subsystem/resources-endpoint`). Generator MUST verify the canonical form via OGC `.adoc` source HTTP-200 fetch BEFORE writing assertions (continuing the S-ETS-02-03 / S-ETS-02-06 / S-ETS-03-07 URI-canonicalization discipline). Expected sub-requirements (~4-5 per OGC 23-001 Annex A `/conf/subsystem/`): (a) `/req/subsystem/resources-endpoint` — `GET /systems/{id}/subsystems` returns HTTP 200 + non-empty `items` array (if implemented by IUT; SKIP-with-reason if 404); (b) `/req/subsystem/canonical-endpoint` — `GET /subsystems/{id}` returns canonical single-item shape (id, type, links per REQ-ETS-CORE-004 base shape); (c) `/req/subsystem/canonical-url` — subsystem links contain `rel="canonical"` (absence of `rel="self"` is NOT FAIL — preserves v1.0 GH#3 fix policy from Core landing page); (d) `/req/subsystem/parent-system-link` — subsystem links contain `rel="system"` (or equivalent) referencing the parent system. The Subsystems class lives at `org.opengis.cite.ogcapiconnectedsystems10.conformance.subsystems.SubsystemsTests` per design.md placeholder. Subsystems DEPENDS ON SystemFeatures via TestNG group dependency wiring (`<dependencies><group name="subsystems" depends-on="systemfeatures"/>`) — FIRST two-level dependency chain in the project (Subsystems→SystemFeatures→Core). Coverage scope at Sprint 4 close: Sprint-1-style minimal (4 @Tests covering 4 highest-priority assertions per Architect ratification — see design.md Sprint 4 ratifications); Sprint 5+ expansion adds remaining ATS items.
- **Rationale**: Subsystems is the FIRST class to exercise a TWO-LEVEL group dependency chain (Subsystems→SystemFeatures→Core). Sprint 3 proved one-level (SystemFeatures→Core) live cascading-skip via S-ETS-03-01 sabotage exec. Subsystems extends to two levels — critical proof point before Sprint 5+ scales to remaining 10 Part 1 classes (most depend on either SystemFeatures or SystemFeatures+Common). Procedures/Sampling/Properties/Deployments are siblings of Subsystems (also depend on SystemFeatures); once Subsystems proves the two-level pattern, Sprint 5+ can BATCH 2-3 of these classes per sprint with confidence. GeoRobotix readily exercises Subsystems via `/systems/{id}/subsystems` (Generator MUST curl-verify BEFORE writing assertions).
- **Maps to**: PRD FR-ETS-13.

#### REQ-ETS-PART1-004: Deployments Conformance Class (Sprint 5 target)
- **Priority**: MUST
- **Status**: IMPLEMENTED (Sprint 5 Run 2, S-ETS-05-06; sister repo HEAD `c25e44a` 2026-04-29; pending Quinn+Raze gate close. New class `org.opengis.cite.ogcapiconnectedsystems10.conformance.deployments.DeploymentsTests` (4 @Tests covering /req/deployment/{resources-endpoint,canonical-endpoint,canonical-url,deployed-system-resource}); testng.xml extended with `<group name="deployments" depends-on="systemfeatures"/>` + DeploymentsTests added to single-block consolidation; VerifyTestNGSuiteDependency extended with 3 new structural lint tests for Deployments group/co-location/method-tagging. UNIQUE-to-Deployments: deployed-system-resource encoding-class assertion checks IUT /conformance for at least one matching encoding URI (conf/geojson|sensorml|json|html); SKIP-with-reason if absent. GeoRobotix Generator-time verification: /deployments returns 1 item (id=16sp744ch58g, type=Feature, geometry=Polygon — Saildrone Arctic Mission); /conformance declares conf/geojson + conf/sensorml — assertion PASSES. Smoke target post-Run-2: 26+4=30 PASS (mvn 78/0/0/3 BUILD SUCCESS). Live smoke deferred to Quinn/Raze gate.)
- **Description**: For each assertion in OGC 23-001 Annex A `/conf/deployment-features/`, the ETS SHALL provide at least one TestNG `@Test` method whose `description` attribute starts with the OGC canonical `.adoc` requirement URI form `/req/deployment/<assertion>`. The 5 sub-requirement `.adoc` files at `raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/deployment/` (HTTP-200-verified by Pat 2026-04-29): `req_resources_endpoint.adoc`, `req_canonical_url.adoc`, `req_canonical_endpoint.adoc`, `req_deployed_system_resource.adoc` (identifier: `/req/deployment/deployed-system-resource` — HYPHENATED), `req_collections.adoc`. The class lives at `org.opengis.cite.ogcapiconnectedsystems10.conformance.deployments.DeploymentsTests`. Deployments DEPENDS ON SystemFeatures via `<group name="deployments" depends-on="systemfeatures"/>`. Coverage scope at Sprint 5: Sprint-1-style minimal (4-5 @Tests): (a) GET /deployments 200 + non-empty items; (b) GET /deployments/{id} canonical shape; (c) rel=canonical link; (d) /req/deployment/deployed-system-resource — SKIP-with-reason if IUT doesn't declare DeployedSystem encoding conformance class. GeoRobotix serves 1 deployment (single-item shape is valid; non-empty check passes). Generator MUST re-verify at sprint time.
- **Rationale**: Deployments is a SystemFeatures sibling (depends on SystemFeatures, not Core directly). With the two-level cascade proven at Sprint 4, Sprint 5 mechanically extends to Deployments using the identical pattern. GeoRobotix confirms the /deployments endpoint exists (1 item).
- **Maps to**: PRD FR-ETS-14.

#### REQ-ETS-PART1-006: Procedures Conformance Class (Sprint 5 target)
- **Priority**: MUST
- **Status**: IMPLEMENTED (Sprint 5 Run 2, S-ETS-05-05; sister repo HEAD `c25e44a` 2026-04-29; pending Quinn+Raze gate close. New class `org.opengis.cite.ogcapiconnectedsystems10.conformance.procedures.ProceduresTests` (4 @Tests covering /req/procedure/{resources-endpoint,location,canonical-endpoint,canonical-url}); testng.xml extended with `<group name="procedures" depends-on="systemfeatures"/>` + ProceduresTests added to single-block consolidation; VerifyTestNGSuiteDependency extended with 3 new structural lint tests for Procedures. UNIQUE-to-Procedures geometry-null invariant per /req/procedure/location verbatim ("A Procedure feature resource SHALL not include a location or geometry") — Generator-time GeoRobotix re-verification: ALL 19 procedures at /procedures have geometry: null; assertion implemented as-written (no SKIP-with-reason fallback needed since invariant holds at IUT level). Smoke target post-Run-2: 26+4=30 PASS contribution (mvn 78/0/0/3 BUILD SUCCESS). Live smoke deferred to Quinn/Raze gate.)
- **Description**: For each assertion in OGC 23-001 Annex A `/conf/procedure-features/`, the ETS SHALL provide at least one TestNG `@Test` method whose `description` attribute starts with the OGC canonical `.adoc` requirement URI form `/req/procedure/<assertion>`. The 5 sub-requirement `.adoc` files at `raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/procedure/` (HTTP-200-verified by Pat 2026-04-29): `req_resources_endpoint.adoc`, `req_canonical_url.adoc`, `req_canonical_endpoint.adoc`, `req_location.adoc` (identifier: `/req/procedure/location` — Procedure SHALL NOT include geometry), `req_collections.adoc`. The class lives at `org.opengis.cite.ogcapiconnectedsystems10.conformance.procedures.ProceduresTests`. Procedures DEPENDS ON SystemFeatures via `<group name="procedures" depends-on="systemfeatures"/>`. Coverage scope at Sprint 5: Sprint-1-style minimal (4-5 @Tests): (a) GET /procedures 200 + non-empty items; (b) GET /procedures/{id} canonical shape; (c) rel=canonical link; (d) `/req/procedure/location` — geometry=null invariant (UNIQUE to Procedures — not in Subsystems or SystemFeatures). Generator MUST curl-verify geometry value BEFORE writing assertion; if IUT returns non-null geometry, use SKIP-with-reason. GeoRobotix serves 19 procedures. Generator MUST re-verify at sprint time.
- **Rationale**: Procedures is a SystemFeatures sibling. Sprint 5 two-class batch (with Deployments) uses the now-proven cascade pattern. The geometry=null invariant is Procedures-unique and represents new assertion surface not present in prior classes.
- **Maps to**: PRD FR-ETS-16.

#### REQ-ETS-PART1-007..013: Remaining Per-Class Conformance Suites
- **Priority**: MUST
- **Status**: REQ-ETS-PART1-011 is PARTIAL-IMPLEMENTED in Sprint 13/14 below; REQ-ETS-PART1-007..008 are IMPLEMENTED in Sprint 7, REQ-ETS-PART1-009 is PARTIAL-IMPLEMENTED in Sprint 11 below, REQ-ETS-PART1-010 is PARTIAL-IMPLEMENTED in Sprint 12 below, REQ-ETS-PART1-012 is PARTIAL-IMPLEMENTED in Sprint 9/15/17/18/19 below, and REQ-ETS-PART1-013 is PARTIAL-IMPLEMENTED in Sprint 10/16/17/18/19 below.
- **Description**: For each remaining OGC 23-001 conformance class (009=`advanced-filtering`, 010=`create-replace-delete`, 011=`update`, 012=`geojson`, 013=`sensorml`), the ETS SHALL provide a TestNG suite class structurally equivalent to Core (REQ-ETS-CORE-001..004), SystemFeatures (REQ-ETS-PART1-002), Common (REQ-ETS-PART1-001), Subsystems (REQ-ETS-PART1-003), Procedures (REQ-ETS-PART1-006), Deployments (REQ-ETS-PART1-004), Sampling Features (REQ-ETS-PART1-007), Property Definitions (REQ-ETS-PART1-008), Subdeployments (REQ-ETS-PART1-005), and GeoJSON (REQ-ETS-PART1-012): one `@Test` per ATS assertion subset selected for the sprint, `description` attribute carries the OGC canonical `.adoc` requirement URI form, suite-level dependency declared via TestNG `dependsOnGroups` if a prerequisite class fails.
- **Rationale**: PRD SC-2 requires Part 1 coverage. Sprint 9 selected a GeoJSON systems read-only subset first because it was lower risk than create-replace-delete mutation coverage and lower schema breadth than SensorML. Sprint 10 continues the low-risk read-only encoding path with SensorML systems before any mutation-side class.
- **Maps to**: PRD FR-ETS-17..23.

### Sub-deliverable 4 — Part 2 Conformance Classes

#### REQ-ETS-PART2-001: Part 2 API Common Conformance Suite
- **Priority**: MUST
- **Status**: PARTIAL_IMPLEMENTED (Sprint 20 Generator 2026-05-07; story S-ETS-20-01)
- **Description**: The ETS SHALL provide a TestNG suite class for OGC 23-002 Requirements Class "Common" using official identifiers `/req/api-common`, `/conf/api-common`, `/req/api-common/resources`, and `/req/api-common/resource-collection`. The implemented Sprint 20 subset is read-only, depends on Part 1 API Common/Core availability via TestNG group `part2apicommon`, and SKIPs with a precise reason when an IUT does not declare `/conf/api-common`.
- **OGC source verified**: OGC 23-002 official published HTML at `https://docs.ogc.org/is/23-002/23-002.html`, Clause 8 "Requirements Class Common", checked 2026-05-07. The requirements class identifier is `/req/api-common`; conformance class is `/conf/api-common`; prerequisite is `http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/api-common`; normative statements are `/req/api-common/resources` and `/req/api-common/resource-collection`.
- **Planning correction**: Frozen web-app artifacts that mention `dynamic-common` or `dynamic-json` are historical and MUST NOT be used for Java ETS `@Test` descriptions. Sprint 20 adopts OGC 23-002 identifiers.
- **GeoRobotix planning probe**: `/conformance` declares several Part 2 classes (`/conf/datastream`, `/conf/controlstream`, `/conf/json`, `/conf/create-replace-delete`, `/conf/system-event`, `/conf/system-history`, and SWE Common encodings) but does not currently declare `/conf/api-common`. Landing page exposes `datastreams` and `observations` links. `GET /datastreams?limit=1`, `GET /observations?limit=1`, and `GET /controlstreams?limit=1` returned HTTP 200 JSON with `items` and `links`; `GET /commands?limit=1` returned HTTP 400 in current IUT state.
- **Implementation evidence**: `Part2ApiCommonTests` checks exact `/conf/api-common` declaration, discovers only advertised Part 2 collection links, probes those links read-only with `limit=1`, and requires JSON collection objects with `items` and `links`. `VerifyPart2ApiCommonTests` prevents stale `dynamic-*` identifier drift and synthesized `/commands` assumptions. Maven post-Raze rerun reported `152 tests / 0 failures / 0 errors / 3 skipped`; GeoRobotix smoke on 2026-05-07 reported `93 total / 55 passed / 0 failed / 38 skipped`; the Part 2 API Common subset SKIPPED because `/conf/api-common` is not declared.
- **Maps to**: PRD FR-ETS-30.

##### Acceptance Scenarios for Sprint 20

#### SCENARIO-ETS-PART2-001-API-COMMON-CONFORMANCE-DECLARED-001 (CRITICAL)
**GIVEN** the ETS is evaluating Part 2 API Common
**WHEN** the suite reads `/conformance`
**THEN** `/conf/api-common` is required for Part 2 API Common PASS evidence
**AND** sibling Part 2 classes such as `/conf/datastream` or `/conf/json` do not imply `/conf/api-common`.

#### SCENARIO-ETS-PART2-001-RESOURCE-TERMINOLOGY-001 (CRITICAL)
**GIVEN** OGC 23-002 `/req/api-common/resources`
**WHEN** Generator implements Part 2 API Common checks
**THEN** it interprets OGC API Features "feature" requirements using Part 2 "resource" terminology
**AND** it records the canonical requirement URI in each `@Test` description.

#### SCENARIO-ETS-PART2-001-RESOURCE-COLLECTION-READONLY-001 (CRITICAL)
**GIVEN** an IUT exposes Part 2 collection links or endpoints
**WHEN** Generator performs the first read-only Part 2 collection checks
**THEN** each probed collection response must return HTTP 200 with a JSON object body containing collection members such as `items` and `links`
**AND** endpoints not advertised or returning non-200 are not assumed available.

#### SCENARIO-ETS-PART2-001-DEPENDENCY-SKIP-001 (CRITICAL)
**GIVEN** Part 2 API Common depends on Part 1 API Common/Core behavior
**WHEN** a prerequisite class fails
**THEN** the Part 2 API Common group SKIPs by dependency rather than producing noisy downstream failures.

#### SCENARIO-ETS-PART2-001-GEOROBOTIX-DECLARATION-HONESTY-001 (CRITICAL)
**GIVEN** current GeoRobotix declares Part 2 sibling classes but not `/conf/api-common`
**WHEN** the Part 2 API Common conformance declaration assertion runs
**THEN** it SKIPs with a reason tied to the missing `/conf/api-common`
**AND** it must not claim Part 2 API Common conformance from sibling declarations alone.

#### REQ-ETS-PART2-002: Part 2 Datastreams & Observations Conformance Suite
- **Priority**: MUST
- **Status**: PARTIAL_IMPLEMENTED (Sprint 21 Generator 2026-05-07; story S-ETS-21-01)
- **Description**: The ETS SHALL provide a TestNG suite class for OGC 23-002 Requirements Class "Datastreams & Observations" using official identifiers `/req/datastream` and `/conf/datastream`. Sprint 21 is the first read-only, declaration-gated subset and SHALL cover canonical Datastream and Observation endpoint availability, Datastream item canonical access, Datastream schema sub-resources, selected System-scoped Datastream sub-resource access, and Datastream-scoped Observation sub-resource access without mutating the IUT.
- **OGC source verified**: OGC 23-002 official published HTML at `https://docs.ogc.org/is/23-002/23-002.html`, Clause 9 "Requirements Class Datastreams & Observations", checked 2026-05-07. The requirements class identifier is `/req/datastream`; conformance class is `/conf/datastream`; prerequisite is Requirements Class 1 `/req/api-common`. Normative statements include `/req/datastream/canonical-url`, `/req/datastream/resources-endpoint`, `/req/datastream/canonical-endpoint`, `/req/datastream/ref-from-system`, `/req/datastream/collections`, `/req/datastream/schema-op`, `/req/datastream/obs-canonical-url`, `/req/datastream/obs-resources-endpoint`, `/req/datastream/obs-canonical-endpoint`, `/req/datastream/obs-ref-from-datastream`, and `/req/datastream/obs-collections`.
- **Dependency policy**: Sprint 21 SHALL keep `/req/api-common` prerequisite visibility explicit. Because GeoRobotix declares `/conf/datastream` but not `/conf/api-common`, Generator MAY evaluate clearly scoped Datastream endpoint assertions when `/conf/datastream` is declared, but SHALL NOT report full `/conf/datastream` class closure while the `/req/api-common` prerequisite is absent or cannot be established. The missing prerequisite must remain a visible SKIP/prerequisite-incomplete outcome, and Datastream evidence SHALL NOT imply API Common PASS.
- **GeoRobotix planning probe**: `/conformance` declares `/conf/datastream` but not `/conf/api-common`. `GET /datastreams?limit=2`, `GET /observations?limit=2`, `GET /datastreams/{id}`, `GET /datastreams/{id}/schema`, `GET /datastreams/{id}/observations?limit=2`, and `GET /systems/{systemId}/datastreams?limit=1` returned HTTP 200 JSON. The selected Datastream exposes `system@id`, `outputName`, `observedProperties`, `resultType`, `formats`, and an `observations` link. The nested observations response for that Datastream was empty with `items` only, so Generator may count it only as endpoint availability evidence. Any `/req/datastream/obs-ref-from-datastream` assertion must require at least one nested Observation item or link with Datastream reference evidence, or SKIP with a precise empty-IUT-state reason.
- **Implementation evidence**: Sprint 21 adds `Part2DatastreamTests` and `part2datastream` TestNG group coverage for `/conf/datastream`, `/datastreams`, `/datastreams/{id}`, `/datastreams/{id}/schema`, `/observations`, `/observations/{id}`, `/datastreams/{id}/observations`, and bounded `/systems/{systemId}/datastreams`. The runtime prerequisite check SKIPs full closure when `/conf/api-common` is absent, while scoped read-only endpoint evidence can run under `/conf/datastream`. Formatter passed; Maven via Docker passed with `160 tests / 0 failures / 0 errors / 3 skipped`; GeoRobotix TeamEngine smoke passed with `104 total / 64 passed / 0 failed / 40 skipped` and zero IUT-bound mutating requests across 82 recognized IUT request-log entries.
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

#### REQ-ETS-PART2-003: Part 2 Control Streams & Commands Conformance Suite
- **Priority**: MUST
- **Status**: PARTIAL_IMPLEMENTED (Sprint 22 Generator 2026-05-08; story S-ETS-22-01)
- **Description**: The ETS provides a TestNG suite class for OGC 23-002 Requirements Class "Control Streams & Commands" using official identifiers `/req/controlstream` and `/conf/controlstream`. Sprint 22 implements the first read-only, declaration-gated subset covering ControlStream endpoint availability, ControlStream resource shape, ControlStream schema sub-resources, selected System-scoped ControlStream sub-resource access, and ControlStream-scoped Command endpoint availability without mutating the IUT.
- **OGC source verified**: OGC 23-002 official published HTML at `https://docs.ogc.org/is/23-002/23-002.html`, Clause 10 "Requirements Class Control Streams & Commands", checked 2026-05-07. The requirements class identifier is `/req/controlstream`; conformance class is `/conf/controlstream`; prerequisite is Requirements Class 1 `/req/api-common`. Normative statements include `/req/controlstream/sf-ref-from-controlstream`, `/req/controlstream/foi-ref-from-controlstream`, `/req/controlstream/canonical-url`, `/req/controlstream/resources-endpoint`, `/req/controlstream/canonical-endpoint`, `/req/controlstream/ref-from-system`, `/req/controlstream/ref-from-deployment`, `/req/controlstream/collections`, `/req/controlstream/schema-op`, `/req/controlstream/cmd-canonical-url`, `/req/controlstream/cmd-resources-endpoint`, `/req/controlstream/cmd-canonical-endpoint`, `/req/controlstream/cmd-ref-from-controlstream`, `/req/controlstream/cmd-collections`, `/req/controlstream/status-resources-endpoint`, `/req/controlstream/command-status-endpoint`, `/req/controlstream/result-resources-endpoint`, and `/req/controlstream/command-result-endpoint`.
- **Dependency policy**: Sprint 22 SHALL keep `/req/api-common` prerequisite visibility explicit. Because GeoRobotix declares `/conf/controlstream` but not `/conf/api-common`, Generator MAY evaluate clearly scoped ControlStream endpoint assertions when `/conf/controlstream` is declared, but SHALL NOT report full `/conf/controlstream` class closure while the `/req/api-common` prerequisite is absent or cannot be established. The missing prerequisite must remain a visible SKIP/prerequisite-incomplete outcome, and ControlStream evidence SHALL NOT imply API Common PASS.
- **GeoRobotix planning probe**: `/conformance` declares `/conf/controlstream` but not `/conf/api-common`. `GET /controlstreams?limit=2`, `GET /controlstreams/{id}`, `GET /controlstreams/{id}/schema`, `GET /controlstreams/{id}/commands?limit=2`, and `GET /systems/{systemId}/controlstreams?limit=2` returned HTTP 200 JSON for selected read-only probes. The selected ControlStream exposes `system@id`, `inputName`, `controlledProperties`, and `formats`; its schema exposes `commandFormat` and `parametersSchema`. The nested commands response for that ControlStream was empty with `items` only. `GET /commands?limit=2` and `GET /controls/{id}` returned HTTP 400 on the current GeoRobotix IUT, so Sprint 22 Generator must not PASS global Command endpoint or `/controls/{id}` canonical URL assertions from `/controlstreams/{id}` alias evidence.
- **Scope guard**: Sprint 22 is a partial read-only subset. It SHALL NOT implement Command creation, feasibility POST, command status/result dereferencing, SWE Common encoding validation, Part 2 Create/Replace/Delete, Part 2 Update, or full command-body validation against the ControlStream schema. Empty ControlStream-scoped Command collections are endpoint evidence only, not `/req/controlstream/cmd-ref-from-controlstream` PASS evidence.
- **Implementation evidence**: `Part2ControlStreamTests` implements declaration-gated read-only checks for `/conformance`, `/controlstreams`, `/controlstreams/{id}`, `/controlstreams/{id}/schema`, `/controlstreams/{id}/commands`, `/commands` availability when provided, `/controls/{id}` canonical URL evidence when provided, nested Command reference evidence when populated, and bounded `/systems/{systemId}/controlstreams`. `VerifyPart2ControlStreamTests` and `VerifyTestNGSuiteDependency` add helper and TestNG structural regressions. Maven via Docker reported `167 tests / 0 failures / 0 errors / 3 skipped`; GeoRobotix TeamEngine smoke reported `115 total / 71 passed / 0 failed / 44 skipped` with zero IUT-bound POST/PUT/DELETE/PATCH entries. On GeoRobotix, seven ControlStream tests PASS and four SKIP honestly for missing `/conf/api-common`, `/controls/{id}` HTTP 400, `/commands` HTTP 400, and empty nested Command reference evidence.
- **Maps to**: PRD FR-ETS-32.

##### Acceptance Scenarios for Sprint 22

#### SCENARIO-ETS-PART2-003-CONTROLSTREAM-CONFORMANCE-DECLARED-001 (CRITICAL)
**GIVEN** the ETS is evaluating OGC 23-002 Control Streams & Commands
**WHEN** it reads `/conformance`
**THEN** `/conf/controlstream` is required before producing ControlStream conformance PASS evidence
**AND** `/conf/api-common` remains a separate prerequisite judgment, not something inferred from ControlStream behavior.

#### SCENARIO-ETS-PART2-003-CONTROLSTREAM-COLLECTION-READONLY-001 (CRITICAL)
**GIVEN** `/req/controlstream/resources-endpoint` and `/req/controlstream/canonical-endpoint`
**WHEN** the ETS issues `GET {api_root}/controlstreams`
**THEN** the response is HTTP 200 JSON with an `items` array
**AND** the test records the canonical requirement URI in its `@Test` description.

#### SCENARIO-ETS-PART2-003-CONTROLSTREAM-ITEM-READONLY-001 (CRITICAL)
**GIVEN** a ControlStream identifier selected from the collection
**WHEN** the ETS reads an available ControlStream resource
**THEN** the response is HTTP 200 JSON for the same ControlStream resource
**AND** the resource exposes enough ControlStream-specific shape to avoid passing on a generic JSON object.

#### SCENARIO-ETS-PART2-003-CONTROLSTREAM-SCHEMA-ENDPOINT-001 (CRITICAL)
**GIVEN** `/req/controlstream/schema-op`
**WHEN** the ETS issues `GET {api_root}/controlstreams/{id}/schema`
**THEN** the response is HTTP 200 JSON with ControlStream command schema evidence such as `commandFormat` and `parametersSchema`.

#### SCENARIO-ETS-PART2-003-COMMAND-ENDPOINTS-READONLY-001 (CRITICAL)
**GIVEN** `/req/controlstream/cmd-resources-endpoint` and `/req/controlstream/cmd-ref-from-controlstream`
**WHEN** the ETS reads Command endpoints in the first Sprint 22 subset
**THEN** the ControlStream-scoped Command collection response is HTTP 200 JSON with an `items` array
**AND** an empty ControlStream-scoped Command collection is not treated as endpoint-availability failure by itself
**AND** `/commands` returning non-200 is not converted into PASS evidence.

#### SCENARIO-ETS-PART2-003-COMMAND-REFERENCE-EVIDENCE-001 (CRITICAL)
**GIVEN** `/req/controlstream/cmd-ref-from-controlstream`
**WHEN** the ETS evaluates ControlStream-to-Command reference behavior
**THEN** PASS requires at least one nested Command item or link with evidence that the Command is associated to the selected ControlStream
**AND** an empty ControlStream-scoped Command collection SKIPs the reference assertion with a precise empty-IUT-state reason.

#### SCENARIO-ETS-PART2-003-SYSTEM-REFERENCE-READONLY-001 (NORMAL)
**GIVEN** `/req/controlstream/ref-from-system` and a ControlStream resource with `system@id`
**WHEN** the ETS issues `GET {api_root}/systems/{systemId}/controlstreams`
**THEN** the response is HTTP 200 JSON with an `items` array
**AND** the selected ControlStream is found when the IUT returns it in the current page, otherwise the check remains bounded and non-mutating.

#### SCENARIO-ETS-PART2-003-CANONICAL-URL-ALIAS-HONESTY-001 (CRITICAL)
**GIVEN** OGC 23-002 `/req/controlstream/canonical-url` cites canonical ControlStream URL form `{api_root}/controls/{id}`
**WHEN** the IUT serves `/controlstreams/{id}` but `GET /controls/{id}` returns non-200
**THEN** the ETS must not PASS `/req/controlstream/canonical-url` from `/controlstreams/{id}` alias evidence alone.

#### SCENARIO-ETS-PART2-003-DEPENDENCY-SKIP-001 (CRITICAL)
**GIVEN** ControlStream has prerequisite `/req/api-common`
**WHEN** the prerequisite class cannot be established for the IUT
**THEN** the ETS must not convert ControlStream endpoint success into API Common PASS evidence
**AND** it must not report full `/conf/controlstream` class closure
**AND** any prerequisite-dependent assertion SKIPs with a precise reason rather than failing downstream noisily.

#### REQ-ETS-PART2-004: Part 2 Command Feasibility Conformance Suite
- **Priority**: MUST.
- **Status**: PARTIAL_IMPLEMENTED (Sprint 23 Generator)
- **Description**: The ETS SHALL provide a TestNG suite for OGC 23-002 Clause 11 Requirements Class "Command Feasibility" using official identifiers `/req/feasibility` and `/conf/feasibility`, with prerequisite `/req/controlstream`. Sprint 23 targets a safety-gated Generator increment: exact conformance declaration detection, prerequisite honesty, feasibility endpoint/resource discovery, canonical URL/status/result/collection checks where evidence exists, and explicit non-mutating behavior against the default public smoke IUT.
- **Scope guard**: Feasibility requests are initiated by creating a Command resource on the feasibility channel. Therefore, any IUT-bound feasibility POST is outside default public-smoke behavior and SHALL require an explicit safe/mutable-IUT opt-in before execution. When `/conf/feasibility` is absent, the suite SHALL SKIP before any feasibility POST. The suite SHALL NOT create regular Commands, exercise unrelated mutation classes, or infer feasibility conformance from `/conf/controlstream` alone.
- **Maps to**: PRD FR-ETS-33.

#### SCENARIO-ETS-PART2-004-FEASIBILITY-CONFORMANCE-DECLARED-001 (CRITICAL)
**GIVEN** OGC 23-002 `/req/feasibility` maps to conformance class `/conf/feasibility`
**WHEN** the IUT conformance document does not declare `/conf/feasibility`
**THEN** the ETS SHALL SKIP Command Feasibility conformance assertions with a precise reason
**AND** it SHALL NOT issue IUT-bound feasibility POST requests.

#### SCENARIO-ETS-PART2-004-DEPENDENCY-SKIP-001 (CRITICAL)
**GIVEN** Command Feasibility has prerequisite `/req/controlstream`
**WHEN** the prerequisite class cannot be established for the IUT
**THEN** the ETS SHALL NOT convert ControlStream endpoint success into full `/conf/feasibility` closure
**AND** prerequisite-dependent assertions SHALL SKIP with a precise reason.

#### SCENARIO-ETS-PART2-004-FEASIBILITY-ENDPOINT-SAFETY-001 (CRITICAL)
**GIVEN** OGC 23-002 `/req/feasibility/ref-from-controlstream` identifies `{api_root}/controlstream/{csId}/feasibility`
**WHEN** Generator implements endpoint checks
**THEN** read-only GET probes MAY verify endpoint availability only after `/conf/feasibility` is declared
**AND** the plural `/controlstreams/{csId}/feasibility` form SHALL be treated as diagnostic alias evidence only, not sufficient PASS evidence for the normative singular path
**AND** IUT-bound feasibility creation POSTs SHALL require an explicit safe/mutable-IUT opt-in.

#### SCENARIO-ETS-PART2-004-FEASIBILITY-RESOURCE-CLOSURE-001 (NORMAL)
**GIVEN** a real Feasibility resource is available through declared `/conf/feasibility` evidence
**WHEN** the ETS evaluates `/req/feasibility/canonical-url`, `/req/feasibility/status-endpoint`, and `/req/feasibility/result-endpoint`
**THEN** it SHALL require actual Feasibility resource evidence before PASS
**AND** it SHALL SKIP when the IUT exposes no Feasibility resources.

#### SCENARIO-ETS-PART2-004-FEASIBILITY-COLLECTIONS-001 (NORMAL)
**GIVEN** `/req/feasibility/collections` is optional unless the server exposes Feasibility collections
**WHEN** a collection has `itemType` equal to `Feasibility`
**THEN** the ETS SHALL verify that `/collections/{collectionId}/items` behaves as a Command resources endpoint
**AND** it SHALL NOT fail an IUT solely because no Feasibility collection is advertised.

#### REQ-ETS-PART2-005: Part 2 System Events Conformance Suite
- **Priority**: MUST.
- **Status**: PARTIAL_IMPLEMENTED (Sprint 24 Generator).
- **Description**: The ETS SHALL provide a TestNG suite for OGC 23-002 Clause 12 Requirements Class "System Events" using official identifiers `/req/system-event` and `/conf/system-event`, with prerequisites `/req/api-common` and Part 1 `/req/system`. Sprint 24 targets a read-only Generator increment: exact conformance declaration detection, prerequisite honesty, canonical `/systemEvents` endpoint checks, system-scoped `/systems/{sysId}/events` endpoint checks, optional SystemEvent resource/canonical evidence, and optional `itemType=SystemEvent` collection checks.
- **Scope guard**: The first System Events increment SHALL NOT implement streaming/SSE event consumption, System History, Advanced Filtering event-by-type, Part 2 JSON schema closure, or mutation classes. It SHALL NOT infer System Events conformance from sibling Part 2 declarations, and SHALL NOT treat Annex A.43's conflicting `/systems/{sysId}/systemEvents` endpoint string as Requirement 43 PASS evidence without a standards-backed rationale.
- **Implementation evidence**: `Part2SystemEventTests` adds six read-only/default-safe checks for exact `/conf/system-event` declaration, prerequisite visibility, canonical `/systemEvents` endpoint evidence, normative `/systems/{sysId}/events` evidence, actual SystemEvent resource/canonical evidence, and optional `itemType=SystemEvent` collections. `VerifyPart2SystemEventTests` prevents stale `/req/systemevents`/`dynamic-*` identifier drift and Annex A.43 alias drift. Maven reported `183 tests / 0 failures / 0 errors / 3 skipped`; GeoRobotix smoke reported `128 total / 72 passed / 0 failed / 56 skipped`; System Events had 1 PASS for declaration and 5 SKIPs for missing `/conf/api-common`, `/systemEvents` HTTP 400, `/systems/{id}/events` streaming-only HTTP 400, no resource evidence, and no advertised `itemType=SystemEvent`.
- **Maps to**: PRD FR-ETS-34.

#### SCENARIO-ETS-PART2-005-SYSTEM-EVENT-CONFORMANCE-DECLARED-001 (CRITICAL)
**GIVEN** OGC 23-002 `/req/system-event` maps to conformance class `/conf/system-event`
**WHEN** the IUT conformance document does not declare `/conf/system-event`
**THEN** the ETS SHALL SKIP System Events conformance assertions with a precise reason
**AND** it SHALL NOT infer System Events support from sibling Part 2 declarations.

#### SCENARIO-ETS-PART2-005-DEPENDENCY-SKIP-001 (CRITICAL)
**GIVEN** System Events has prerequisites `/req/api-common` and Part 1 `/req/system`
**WHEN** the prerequisite classes cannot be established for the IUT
**THEN** the ETS SHALL keep scoped endpoint evidence separate from full `/conf/system-event` closure
**AND** prerequisite-dependent assertions SHALL SKIP with a precise reason.

#### SCENARIO-ETS-PART2-005-CANONICAL-ENDPOINT-001 (CRITICAL)
**GIVEN** OGC 23-002 `/req/system-event/canonical-endpoint` identifies `{api_root}/systemEvents`
**WHEN** the IUT declares `/conf/system-event`
**THEN** the ETS SHALL verify the canonical SystemEvent resources endpoint using read-only GET
**AND** HTTP 400 or non-resource streaming-only responses SHALL NOT produce PASS.

#### SCENARIO-ETS-PART2-005-SYSTEM-REF-ENDPOINT-001 (CRITICAL)
**GIVEN** OGC 23-002 Requirement 43 identifies `{api_root}/systems/{sysId}/events`
**WHEN** the ETS checks System-scoped SystemEvent resources
**THEN** it SHALL use `/systems/{sysId}/events` as the normative endpoint
**AND** `/systems/{sysId}/systemEvents` SHALL be diagnostic alias evidence only unless a standards-backed correction is documented.

#### SCENARIO-ETS-PART2-005-SYSTEM-EVENT-RESOURCE-CLOSURE-001 (NORMAL)
**GIVEN** a real SystemEvent resource is available through declared `/conf/system-event` evidence
**WHEN** the ETS evaluates `/req/system-event/canonical-url`
**THEN** it SHALL require actual SystemEvent resource evidence before PASS
**AND** it SHALL SKIP when the IUT exposes no SystemEvent resources.

#### SCENARIO-ETS-PART2-005-SYSTEM-EVENT-COLLECTIONS-001 (NORMAL)
**GIVEN** `/req/system-event/collections` applies when the server exposes SystemEvent collections
**WHEN** a collection has `itemType` equal to `SystemEvent`
**THEN** the ETS SHALL verify that `/collections/{collectionId}/items` behaves as a System Event resources endpoint
**AND** it SHALL NOT fail an IUT solely because no SystemEvent collection is advertised.

#### REQ-ETS-PART2-006: Part 2 Advanced Filtering Conformance Suite
- **Priority**: MUST
- **Status**: PARTIAL_IMPLEMENTED (Sprint 25 Generator 2026-05-13; story S-ETS-25-01)
- **Description**: The ETS SHALL implement a read-only, declaration-gated subset of OGC 23-002 Clause 13 Requirements Class "Advanced Filtering" using official identifiers `/req/advanced-filtering` and `/conf/advanced-filtering`. The Sprint 25 Generator increment preserves prerequisite honesty for `/req/api-common` and Part 1 `/req/advanced-filtering`, does not infer conformance from successful undeclared query behavior, and avoids mutation.
- **Rationale**: Advanced Filtering extends previously implemented Part 2 resource classes with query parameters across DataStream, Observation, ControlStream, Command, CommandStatus, and SystemEvent endpoints.
- **Maps to**: PRD FR-ETS-36.
- **Requirements class**: `/req/advanced-filtering`.
- **Conformance class**: `/conf/advanced-filtering`.
- **Prerequisites**: `/req/api-common` and `http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering`.
- **In-scope normative statements for Sprint 25 Generator**: `/req/advanced-filtering/datastream-by-phenomenontime`, `/req/advanced-filtering/datastream-by-resulttime`, `/req/advanced-filtering/datastream-by-obsprop`, `/req/advanced-filtering/obs-by-phenomenontime`, `/req/advanced-filtering/obs-by-resulttime`, `/req/advanced-filtering/controlstream-by-issuetime`, `/req/advanced-filtering/controlstream-by-exectime`, `/req/advanced-filtering/controlstream-by-controlprop`, `/req/advanced-filtering/cmd-by-issuetime`, `/req/advanced-filtering/cmd-by-exectime`, `/req/advanced-filtering/cmd-by-status`, `/req/advanced-filtering/cmd-by-sender`, and `/req/advanced-filtering/event-by-type`.
- **Planning correction**: OGC 23-002 Annex A does not define `/conf/system-history` or `/req/system-history`; GeoRobotix advertises `/conf/system-history` as a non-standard/vendor extension. The former `REQ-ETS-PART2-006` System History placeholder is retired from the OGC conformance-class backlog.
- **Implementation evidence**: `Part2AdvancedFilteringTests` adds the `part2advancedfiltering` TestNG group with 9 read-only runtime checks for exact `/conf/advanced-filtering` declaration, visible `/req/api-common` and Part 1 `/req/advanced-filtering` prerequisites, DataStream time filters, DataStream `observedProperty`, Observation time filters, ControlStream time filters, ControlStream `controlledProperty`, Command filters when `/commands` is available, and SystemEvent `eventType` when `/systemEvents` is available. Filter PASS requires non-empty returned resources that satisfy the requested predicate; empty seed-derived filter responses SKIP with reason instead of PASS. Raze gap `RAZE-ETS25-IMPL-GAP-001` was fixed by requiring `/req/advanced-filtering/obs-by-phenomenontime` to seed and validate only `phenomenonTime`, with no `resultTime` fallback. The related low concern was fixed by making `timeIntersects` parse both time values instead of accepting substrings. `VerifyPart2AdvancedFilteringTests` adds 8 helper regressions for official identifiers, canonical `systemEvents` casing, parsed time intersection, malformed substring rejection, strict Observation `phenomenonTime`, property matching, command/event predicate extraction, and collection shape. `VerifyTestNGSuiteDependency` wires structural coverage for the new group. Sprint 25 also removes the stale `systemhistory` vendor-extension token from Part 2 API Common collection discovery.
- **Verification**: Docker formatter `spring-javaformat:apply` BUILD SUCCESS. Docker Maven wrapper BUILD SUCCESS with `195 tests / 0 failures / 0 errors / 3 skipped`. GeoRobotix TeamEngine smoke reported `137 total / 72 passed / 0 failed / 65 skipped` and zero IUT-bound POST/PUT/DELETE/PATCH across 100 recognized request-log entries. The 9 Part 2 Advanced Filtering runtime tests all SKIP on GeoRobotix because `/conf/advanced-filtering` is not declared.
- **Remaining scope**: full FOI recursive filters, CommandStatus filters, `/req/advanced-filtering/*-by-foi` closure, broader combined filter behavior, positive Command/SystemEvent coverage against a declaring IUT with available endpoints, and streaming/SSE event filter coverage remain deferred.
- **Acceptance scenarios implemented**:
  - `SCENARIO-ETS-PART2-006-ADVFILTER-CONFORMANCE-DECLARED-001`
  - `SCENARIO-ETS-PART2-006-DEPENDENCY-SKIP-001`
  - `SCENARIO-ETS-PART2-006-DATASTREAM-FILTERS-READONLY-001`
  - `SCENARIO-ETS-PART2-006-OBSERVATION-FILTERS-READONLY-001`
  - `SCENARIO-ETS-PART2-006-CONTROLSTREAM-FILTERS-READONLY-001`
  - `SCENARIO-ETS-PART2-006-COMMAND-FILTERS-READONLY-001`
  - `SCENARIO-ETS-PART2-006-SYSTEM-EVENT-FILTER-READONLY-001`
  - `SCENARIO-ETS-PART2-006-UNDECLARED-FILTER-HONESTY-001`
- **GeoRobotix planning probe**: `/conformance` does not declare `/conf/advanced-filtering`, even though selected read-only filter requests currently return mixed behavior: `GET /datastreams?phenomenonTime=...`, `/datastreams?resultTime=...`, `/datastreams?observedProperty=...`, `/controlstreams?issueTime=...`, and `/controlstreams?executionTime=...` returned HTTP 200; `GET /observations?phenomenonTime=...` and `/observations?resultTime=...` returned HTTP 200 with empty `items`; `GET /commands?...` and `/systemEvents?eventType=...` returned HTTP 400; `GET /systems/{id}/events?eventType=...` returned HTTP 400 streaming-only. These probe results are readiness diagnostics only and must not produce Advanced Filtering PASS while `/conf/advanced-filtering` is absent.

##### Acceptance Scenarios for Sprint 25

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
- **Status**: PARTIAL_IMPLEMENTED (Sprint 26 Generator; seeded local OSH E2E accepted after fixture repair; GeoRobotix public smoke currently fails as advisory external evidence)
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
**GIVEN** the default smoke target is the public GeoRobotix IUT
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
**GIVEN** TeamEngine smoke runs against GeoRobotix
**WHEN** the smoke run completes
**THEN** request logs contain zero IUT-bound POST, PUT, DELETE, or PATCH requests
**AND** any Create/Replace/Delete assertions that need mutation SKIP before dispatch.

#### REQ-ETS-PART2-008: Part 2 Update Conformance Suite
- **Priority**: MUST (eventually); SHALL NOT be scoped into Sprint 1.
- **Status**: PARTIAL_IMPLEMENTED (Sprint 27 Generator; positive PATCH lifecycle remains deferred)
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
**GIVEN** the default smoke target is the public GeoRobotix IUT
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
**GIVEN** TeamEngine smoke runs against GeoRobotix
**WHEN** the smoke run completes
**THEN** request logs contain zero IUT-bound PATCH, POST, PUT, or DELETE requests
**AND** any Update assertions that need mutation SKIP before dispatch.

#### REQ-ETS-PART2-009: Part 2 JSON Encoding
- **Priority**: MUST
- **Status**: PARTIAL_IMPLEMENTED (Sprint 28 Generator)
- **Description**: The ETS SHALL implement the first declaration-gated, read-only OGC 23-002 Clause 16.1 JSON Encoding subset using official `/req/json` and `/conf/json` identifiers. Runtime checks SHALL gate on exact `/conf/json` declaration, keep the SWE Common 3.0 JSON record components prerequisite visible, condition resource-specific assertions on the underlying declared Part 2 resource classes, validate read responses against bundled Part 2 JSON Schemas where candidate resources are available, and treat write-media-type support as API-definition/readiness evidence only unless a safe dedicated mutable IUT is explicitly enabled in a later sprint.
- **Rationale**: PRD SC-3 requires Part 2 coverage. OGC 23-002 Annex A.9 defines `/conf/json` with Requirements 93-106. Sprint 28 adds `Part2JsonTests`, TestNG `part2json` wiring, and helper regressions for the first read-only JSON Encoding subset. Current GeoRobotix declares `/conf/json`, `/conf/datastream`, `/conf/controlstream`, `/conf/system-event`, `/conf/create-replace-delete`, and SWE Common encoding classes, but still returns HTTP 500 for declared Datastream/Observation reads and exposes ControlStream JSON that fails the bundled `controlStreamCollection.json` schema. The ETS therefore fails or skips honestly instead of reporting false PASS from declaration alone.
- **Maps to**: PRD FR-ETS-39.

#### SCENARIO-ETS-PART2-009-JSON-CONFORMANCE-DECLARED-001 (CRITICAL)
**GIVEN** the IUT exposes `/conformance`
**WHEN** the Part 2 JSON Encoding tests run
**THEN** exact `http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/json` declaration is required before `/req/json` assertions can PASS
**AND** sibling declarations such as Common JSON, GeoJSON, SWE Common JSON, or Part 2 resource classes alone cannot satisfy `/conf/json`.

#### SCENARIO-ETS-PART2-009-SWE-PREREQUISITE-VISIBLE-001 (NORMAL)
**GIVEN** OGC 23-002 Clause 16.1 lists SWE Common 3.0 JSON record components as a prerequisite
**WHEN** the ETS reports full `/conf/json` closure
**THEN** the prerequisite `http://www.opengis.net/spec/SWE/3.0/conf/json-record-components` must be visible or explicitly reported as prerequisite-incomplete
**AND** scoped JSON representation checks may still run when `/conf/json` and the relevant Part 2 resource class are declared.

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
**THEN** each reachable declared resource endpoint must return HTTP 200, an `application/json` compatible content type, and JSON parseable content before mediatype-read PASS
**AND** HTTP 400, HTTP 500, HTML/text error bodies, or empty non-resource evidence cannot PASS.

#### SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001 (CRITICAL)
**GIVEN** bundled Part 2 JSON Schemas exist under `src/main/resources/schemas/connected-systems-2/json/`
**WHEN** candidate JSON resources or collections are retrieved
**THEN** the ETS validates them against the corresponding schemas named in OGC 23-002 Annex A.9, including `dataStream.json`, `dataStreamCollection.json`, `observationSchemaJson.json`, `observation.json`, `observationCollection.json`, `controlStream.json`, `controlStreamCollection.json`, `commandSchemaJson.json`, `command.json`, `commandCollection.json`, `commandStatus.json`, `commandStatusCollection.json`, `commandResult.json`, `commandResultCollection.json`, `systemEvent.json`, and `systemEventCollection.json`
**AND** no schema-validation PASS is reported when the endpoint is unavailable, the collection has no candidate resource, or a schema fixture is missing.

#### SCENARIO-ETS-PART2-009-OBSERVATION-COMMAND-CONSTRAINTS-001 (NORMAL)
**GIVEN** Requirements 98, 102, and 105 require Observation result/parameters, Command parameters, and CommandResult inline data to follow parent DataStream or ControlStream schemas
**WHEN** parent schema evidence or candidate child resources are absent
**THEN** the ETS SKIPs constraint checks with a precise no-safe-evidence reason
**AND** it SHALL NOT PASS dynamic-schema constraints from collection shape, hardcoded examples, or sibling class declarations.

#### SCENARIO-ETS-PART2-009-MEDIATYPE-WRITE-ADVERTISEMENT-001 (NORMAL)
**GIVEN** Requirement 94 applies only when Create/Replace/Delete is implemented
**WHEN** the ETS checks JSON write-media-type support in the first JSON increment
**THEN** it uses API definition or explicit operation metadata to verify advertised `application/json` support for CREATE or REPLACE operations
**AND** default public GeoRobotix smoke does not issue POST, PUT, PATCH, or DELETE
**AND** OPTIONS alone is readiness evidence, not mediatype-write PASS.

#### SCENARIO-ETS-PART2-009-UNAVAILABLE-ENDPOINT-HONESTY-001 (CRITICAL)
**GIVEN** the current public IUT may declare `/conf/json` while individual resource endpoints are unhealthy or unavailable
**WHEN** Datastream, Observation, Command, CommandStatus, CommandResult, or SystemEvent endpoints return HTTP 400, HTTP 500, streaming-only responses, or empty candidate sets
**THEN** the ETS records FAIL for reachable declared requirements that violate HTTP 200/schema expectations, or SKIP when no candidate/evidence exists
**AND** it never converts those outcomes into PASS from declaration, broad media-type lists, or existing sibling tests.

#### SCENARIO-ETS-PART2-009-SMOKE-NO-PUBLIC-MUTATION-001 (CRITICAL)
**GIVEN** TeamEngine smoke runs against the public GeoRobotix IUT
**WHEN** the Part 2 JSON tests execute
**THEN** request logs contain zero IUT-bound POST, PUT, PATCH, or DELETE requests
**AND** any JSON write-media-type or dynamic-schema behavior requiring mutation SKIPs or relies on non-mutating API-definition evidence only.

#### REQ-ETS-PART2-010: Part 2 SWE Common JSON Encoding
- **Priority**: MUST
- **Status**: PARTIAL_IMPLEMENTED (Sprint 29 Generator; public GeoRobotix E2E failed)
- **Description**: The ETS SHALL implement the first declaration-gated, read-only OGC 23-002 Clause 16.2 SWE Common JSON Encoding subset using official `/req/swecommon-json` and `/conf/swecommon-json` identifiers. Runtime checks SHALL gate on exact `/conf/swecommon-json` declaration, keep the SWE Common 3.0 JSON Encoding Rules prerequisite visible, condition Observation assertions on declared `/conf/datastream`, condition Command assertions on declared `/conf/controlstream`, verify `application/swe+json` read support only from advertised/retrieved Observation or Command evidence, validate SWE Common schema resources against bundled `observationSchemaSwe.json` and `commandSchemaSwe.json`, and treat write-media-type support as API-definition/readiness evidence only unless a safe dedicated mutable IUT is explicitly enabled in a later sprint.
- **Rationale**: PRD SC-3 requires Part 2 coverage. OGC 23-002 Annex A.10 defines `/conf/swecommon-json` with Requirements 107-114. Sprint 29 implements the first read-only SWE Common JSON subset after Sprint 28 JSON Encoding through `Part2SweCommonJsonTests`, TestNG `part2swecommonjson` wiring, helper regressions, exact `application/swe+json` media checks, bundled SWE schema validation, and non-mutating API-definition write-media checks. Current GeoRobotix declares `/conf/swecommon-json`, `/conf/datastream`, `/conf/controlstream`, and `/conf/create-replace-delete`, but still returns HTTP 500 for DataStream/Observation read paths and fails reachable ControlStream schema preconditions before SWE Common Command Schema PASS evidence. The ETS therefore fails or skips honestly rather than passing from declaration, sibling classes, API format lists, or OPTIONS evidence alone.
- **Maps to**: PRD FR-ETS-40.

#### SCENARIO-ETS-PART2-010-SWEJSON-CONFORMANCE-DECLARED-001 (CRITICAL)
**GIVEN** the IUT exposes `/conformance`
**WHEN** the SWE Common JSON Encoding tests run
**THEN** exact `http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/swecommon-json` declaration is required before `/req/swecommon-json` assertions can PASS
**AND** sibling declarations such as `/conf/json`, `/conf/swecommon-text`, `/conf/swecommon-binary`, or resource-class declarations alone cannot satisfy `/conf/swecommon-json`.

#### SCENARIO-ETS-PART2-010-SWE-JSON-ENCODING-RULES-PREREQUISITE-001 (NORMAL)
**GIVEN** OGC 23-002 Clause 16.2 lists SWE Common 3.0 JSON Encoding Rules as a prerequisite
**WHEN** the ETS reports full `/conf/swecommon-json` closure
**THEN** `http://www.opengis.net/spec/SWE/3.0/conf/json-encoding-rules` must be visible or explicitly reported as prerequisite-incomplete
**AND** scoped read-only checks may still run when `/conf/swecommon-json` and the relevant Part 2 resource class are declared.

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
**WHEN** the ETS checks SWE Common JSON write-media-type support in the first increment
**THEN** it uses API definition or explicit operation metadata to verify advertised `application/swe+json` support for CREATE or REPLACE operations on Observation or Command resource endpoints only
**AND** default public GeoRobotix smoke does not issue POST, PUT, PATCH, or DELETE
**AND** OPTIONS, unrelated POST/PUT paths, and subresource paths such as Command status alone are readiness evidence, not mediatype-write PASS.

#### SCENARIO-ETS-PART2-010-UNAVAILABLE-ENDPOINT-HONESTY-001 (CRITICAL)
**GIVEN** the current public IUT may declare `/conf/swecommon-json` while individual resource endpoints are unhealthy or inconsistent
**WHEN** DataStream, Observation, Command, or ControlStream schema endpoints return HTTP 400, HTTP 500, empty candidate sets, `application/json` fallback schemas, or wrong media members
**THEN** the ETS records FAIL for reachable declared requirements that violate HTTP 200/schema/media expectations, or SKIP when no candidate/evidence exists
**AND** it never converts those outcomes into PASS from declaration, broad media-format lists, or existing sibling tests.

#### SCENARIO-ETS-PART2-010-SMOKE-NO-PUBLIC-MUTATION-001 (CRITICAL)
**GIVEN** TeamEngine smoke runs against the public GeoRobotix IUT
**WHEN** the SWE Common JSON tests execute
**THEN** request logs contain zero IUT-bound POST, PUT, PATCH, or DELETE requests
**AND** any write-media-type or encoding behavior requiring mutation SKIPs or relies on non-mutating API-definition evidence only.

#### REQ-ETS-PART2-011: Part 2 SWE Common Text Encoding
- **Priority**: MUST
- **Status**: SPECIFIED (Sprint 30 planning; Generator pending)
- **Description**: The ETS SHALL implement the first declaration-gated, read-only OGC 23-002 Clause 16.3 SWE Common Text Encoding subset using official `/req/swecommon-text` and `/conf/swecommon-text` identifiers. Runtime checks SHALL gate on exact `/conf/swecommon-text` declaration, keep the SWE Common 3.0 Text Encoding Rules prerequisite visible, condition Observation assertions on declared `/conf/datastream`, condition Command assertions on declared `/conf/controlstream`, verify `application/swe+text` read support only from advertised/retrieved Observation or Command evidence, validate SWE Common schema metadata against bundled `observationSchemaSwe.json` and `commandSchemaSwe.json` while requiring `TextEncoding`, and treat write-media-type support as API-definition/readiness evidence only unless a safe dedicated mutable IUT is explicitly enabled in a later sprint.
- **Rationale**: PRD SC-3 requires Part 2 coverage. OGC 23-002 Clause 16.3 and Annex A.11 define `/conf/swecommon-text` with Requirements 115-122. Sprint 30 planning follows Sprint 29's SWE Common JSON guardrails but swaps the media type and schema-encoding assertion to Text Encoding. Current GeoRobotix declares `/conf/swecommon-text`, `/conf/datastream`, `/conf/controlstream`, and `/conf/create-replace-delete`, but it does not expose SWE 3.0 `/conf/text-encoding-rules`; DataStream/Observation text reads return HTTP 500; the selected ControlStream advertises `application/swe+csv` rather than `application/swe+text`; and `cmdFormat=application/swe+text` returns JSON-format schema evidence with `commandFormat=application/json` and no `TextEncoding`. The ETS must therefore fail or skip honestly rather than passing from declaration, sibling SWE Common classes, API format lists, JSON fallback schemas, or OPTIONS evidence alone.
- **Maps to**: PRD FR-ETS-41.

#### SCENARIO-ETS-PART2-011-SWETEXT-CONFORMANCE-DECLARED-001 (CRITICAL)
**GIVEN** the IUT exposes `/conformance`
**WHEN** the SWE Common Text Encoding tests run
**THEN** exact `http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/swecommon-text` declaration is required before `/req/swecommon-text` assertions can PASS
**AND** sibling declarations such as `/conf/json`, `/conf/swecommon-json`, `/conf/swecommon-binary`, or resource-class declarations alone cannot satisfy `/conf/swecommon-text`.

#### SCENARIO-ETS-PART2-011-SWE-TEXT-ENCODING-RULES-PREREQUISITE-001 (NORMAL)
**GIVEN** OGC 23-002 Clause 16.3 lists SWE Common 3.0 Text Encoding Rules as a prerequisite
**WHEN** the ETS reports full `/conf/swecommon-text` closure
**THEN** `http://www.opengis.net/spec/SWE/3.0/conf/text-encoding-rules` must be visible or explicitly reported as prerequisite-incomplete
**AND** scoped read-only checks may still run when `/conf/swecommon-text` and the relevant Part 2 resource class are declared.

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
**WHEN** the ETS checks SWE Common Text write-media-type support in the first increment
**THEN** it uses API definition or explicit operation metadata to verify advertised `application/swe+text` support for CREATE or REPLACE operations on Observation or Command resource endpoints only
**AND** default public GeoRobotix smoke does not issue POST, PUT, PATCH, or DELETE
**AND** OPTIONS, unrelated POST/PUT paths, `application/swe+csv`, vendor media types, and subresource paths such as Command status alone are readiness evidence, not mediatype-write PASS.

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
**GIVEN** TeamEngine smoke runs against the public GeoRobotix IUT
**WHEN** the SWE Common Text tests execute
**THEN** request logs contain zero IUT-bound POST, PUT, PATCH, or DELETE requests
**AND** any write-media-type or encoding behavior requiring mutation SKIPs or relies on non-mutating API-definition evidence only.

#### REQ-ETS-PART2-012..013: Remaining Part 2 Conformance Suites
- **Priority**: MUST (eventually); SHALL NOT be scoped into Sprint 1.
- **Status**: PLACEHOLDER (remaining Part 2 work after Sprint 30 SWE Common Text planning)
- **Description**: For each of the remaining 2 OGC 23-002 conformance classes or cross-class closures (`swecommon-binary`, `observation-binding`), the ETS SHALL provide a TestNG suite class structurally equivalent to Part 1 classes. Per-assertion REQ-* IDs deferred to future sprint planning.
- **Rationale**: PRD SC-3 requires Part 2 coverage. User gate locks Sprint 1 to Part 1 only.
- **Maps to**: PRD FR-ETS-42..43, except retired non-standard FR-ETS-35 System History.

### Sub-deliverable 5 — TeamEngine Integration

#### REQ-ETS-TEAMENGINE-001: SPI Registration
- **Priority**: MUST
- **Status**: SPECIFIED
- **Description**: The ETS SHALL expose a class implementing the TeamEngine TestNG SPI (e.g. `org.opengis.cite.ogcapiconnectedsystems10.TestNGController` extending `com.occamlab.te.spi.executors.testng.TestNGExecutor` per `ets-common` convention). The SPI registration SHALL be declared via `META-INF/services/com.occamlab.te.spi.jaxrs.TestSuiteController` so TeamEngine 5.6.x (currently 5.6.1) discovers the suite at startup.
- **Rationale**: Without SPI registration TeamEngine cannot enumerate the suite.
- **Maps to**: PRD FR-ETS-50.

#### REQ-ETS-TEAMENGINE-002: CTL Wrapper
- **Priority**: MUST
- **Status**: SPECIFIED
- **Description**: A CTL wrapper at `src/main/scripts/ctl/ogcapi-connectedsystems10-suite.ctl` SHALL expose the suite to TeamEngine's CTL UI, accepting `iut-url` (CS API landing-page URL, required), `auth-type` (one of `none`, `bearer`, `apikey`, `basic`, optional, default `none`), and `auth-credential` (string, optional). The CTL wrapper passes these as TestNG suite parameters.
- **Rationale**: TeamEngine 5.6.x (currently 5.6.1)'s primary entry surface is CTL; SPI alone is not enough for the user-visible UI.
- **Maps to**: PRD FR-ETS-51.

#### REQ-ETS-TEAMENGINE-003: Dockerfile
- **Priority**: MUST
- **Status**: SPECIFIED
- **Description**: A `Dockerfile` SHALL produce a runnable TeamEngine 5.6.1 webapp on a JDK 17 base image with the built ETS jar staged under `/usr/local/tomcat/webapps/teamengine/WEB-INF/lib/`. The image SHALL build via `docker build -t ets-ogcapi-connectedsystems10 .` from a clean checkout with no additional host dependencies. **Original REQ wording (`extends ogccite/teamengine-production:5.6.1`) reconciled 2026-04-28T19:55Z** per Quinn s03 GAP-1 + Raze s03 CONCERN-1: the `:5.6.1` tag does not exist on Docker Hub (only `:latest` and `:1.0-SNAPSHOT`), and the production image runs JDK 8 (incompatible with the JDK 17 ETS jar — `UnsupportedClassVersionError class file version 61.0`). Implemented resolution per S-ETS-01-03 commit `d910808`: assemble TE 5.6.1 manually on `tomcat:8.5-jre17` by downloading `teamengine-web-5.6.1.war` + `teamengine-web-5.6.1-common-libs.zip` + `teamengine-console-5.6.1-base.zip` from Maven Central + 3 secondary patches. Identical TE 5.6.1 behavior + JDK 17 runtime; identical 12/12 PASS against GeoRobotix. Full audit trail at new repo `ops/server.md` "Docker smoke test" section. **ADR-007 (Dockerfile base image deviation) is a Sprint 2 follow-up** — Quinn s03 GAP-1 identifies the missing ADR-tracked decision; deferred per Quinn's recommendation.
- **Maps to**: PRD FR-ETS-52, NFR-ETS-11.

#### REQ-ETS-TEAMENGINE-004: docker-compose
- **Priority**: SHOULD
- **Status**: SPECIFIED
- **Description**: A `docker-compose.yml` SHALL bring up the TeamEngine + ETS service at `http://localhost:8081/teamengine/` with port mapping, environment variable injection, and a healthcheck against `/teamengine/`.
- **Maps to**: PRD FR-ETS-53, NFR-ETS-11.

#### REQ-ETS-TEAMENGINE-005: Smoke Test
- **Priority**: MUST
- **Status**: SPECIFIED
- **Description**: A repository smoke-test script (`scripts/smoke-test.sh`) SHALL: (a) build the Docker image, (b) launch the container, (c) wait for healthcheck, (d) execute the Core suite against `https://api.georobotix.io/ogc/t18/api`, (e) assert the TestNG report is non-empty and contains zero suite-registration errors. Used as Sprint 1's E2E acceptance criterion.
- **Maps to**: PRD FR-ETS-54, SC-4.

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
- **Description**: A script `scripts/audit-fixture-port.sh` SHALL list case IDs in TS source vs Java source and flag any case present in TS but not in Java. CI runs this script; presence of an unexplained drop fails the build.
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
- **Status**: SPECIFIED
- **Description**: A diff script (`scripts/sync-uri-coverage.sh`) SHALL extract every canonical OGC requirement URI from `csapi_compliance/src/engine/registry/*.ts` and from Java `@Test` `description` attributes in the new ETS, and SHALL fail if any URI exists in TS but not in Java (or vice versa) without an explicit allowlist entry in `ops/uri-coverage-allowlist.txt`. CI SHALL run this script on every commit affecting either the TS registry or the Java ETS.
- **Rationale**: Prevents silent drift between the v1.0 web app and the ETS as OGC errata land. Both consume the same JSON Schemas; both should cover the same URI set.
- **Maps to**: PRD FR-ETS-90, R-PIVOT-11.

### Sub-deliverable 10 — Cleanup REQs (Sprint 2 + Sprint 3 carryover formalization)

> Sprint 2 introduced REQ-ETS-CLEANUP-001..004 to track cleanup work as first-class spec items.
> Sprint 3 extends with REQ-ETS-CLEANUP-005..008 for the Sprint 2 carryover items now closing.
> Sprint 4 extends with REQ-ETS-CLEANUP-009..012 for the Sprint 3 carryover items now closing (CI-workflow ESCALATION binary close, image-size v2 chown-layer attack, deeper E2E credential-leak smoke, sabotage-script hermetic execution fixes).

#### REQ-ETS-CLEANUP-005: Live Break-Core Dependency-Skip Verification
- **Priority**: MUST
- **Status**: IMPLEMENTED (pending Quinn+Raze) 2026-04-29 — Generator Run 1: TestNG XmlSuite parser unit test `VerifyTestNGSuiteDependency.java` (4 @Tests, all PASS in mvn test; 49 → 53 surefire) + bash sabotage script `scripts/sabotage-test.sh` (stub-server approach per ADR-010, authored + committed but live execution deferred to next gate run with proper Docker time budget per Sprint 3 mitigation plan). Defense-in-depth role split per ADR-010: structural lint + behavioral verification both shipped.
- **Description**: The dependency-skip wiring (TestNG `dependsOnGroups` declaration in `testng.xml`) SHALL be verified at runtime via cascading-SKIP behavior under a FAILing Core test, NOT just at static layers (source `groups` annotations + testng.xml declaration + smoke XML attribute). Verification approach: TestNG programmatic-API unit test (`VerifyTestNGSuiteDependency.java`) OR bash sabotage script (`scripts/sabotage-test.sh`) OR both per Architect ratification. Acceptance: when Core's `landingPageReturnsHttp200` is sabotaged to fail, all 4 SystemFeatures @Tests report `status="SKIP"` (NOT FAIL/ERROR); when Core is restored, all PASS. Closes Quinn s06 CONCERN-1 + Raze s06 CONCERN-1.
- **Maps to**: PRD FR-ETS-24, NFR-ETS-15.

#### REQ-ETS-CLEANUP-006: CredentialMaskingFilter Integration Test + REST-Assured RequestLoggingFilter Wrap
- **Priority**: MUST
- **Status**: SPECIFIED (Sprint 3 target via S-ETS-03-02)
- **Description**: (a) The suite SHALL accept `auth-credential` as a CTL parameter + TestNG suite parameter; the `scripts/smoke-test.sh` SHALL accept `--auth-credential <value>` (or env var `AUTH_CREDENTIAL`) and pass it through to the suite. (b) `MaskingRequestLoggingFilter` (subclass of REST-Assured's built-in `RequestLoggingFilter`) OR equivalent wrap pattern per Architect ratification SHALL intercept REST-Assured's request-log output and apply the existing `CredentialMaskingFilter.maskValue()` semantics BEFORE the log line is emitted. (c) An integration test (executed during smoke OR as a dedicated `scripts/credential-leak-test.sh`) SHALL set `auth-credential=Bearer ABCDEFGH12345678WXYZ`, run the suite, and grep-assert ZERO hits for the literal substring `EFGH12345678WXYZ` in BOTH TestNG report XML attachments AND container logs. The masked form (`Bear***WXYZ` or equivalent) MUST appear at least once (proves filter ran). Closes Sprint 2 PARTIAL `no_credential_leak_in_test_logs` + Raze cleanup CONCERN-2.
- **Maps to**: PRD FR-ETS-25 (FR-CAP-006/007 v1.0 carryover), NFR-ETS-08.

#### REQ-ETS-CLEANUP-007: CI Workflow Live at `.github/workflows/build.yml`
- **Priority**: MUST
- **Status**: SPECIFIED (Sprint 3 target via S-ETS-03-03; USER ACTION required: `gh auth refresh -s workflow`)
- **Description**: The CI workflow staged at `ci/github-workflows-build.yml` SHALL be moved to `.github/workflows/build.yml` so GitHub Actions runs it on push. Acceptance: at least one `workflow_run` exists with `conclusion=success` on a Sprint 3 commit; the run URL is captured in `ops/test-results/sprint-ets-03-ci-workflow-live-<date>.txt`. Pre-condition: orchestrator runs `gh auth refresh -s workflow` (token scope `workflow` is required to push to `.github/workflows/`). If pre-condition not met at sprint start, story DEFERRED-WITH-RATIONALE; carryover to Sprint 4 with 4th-sprint-defer-escalation flag.
- **Maps to**: PRD FR-ETS-05 (CI plumbing), NFR-ETS-02.

#### REQ-ETS-CLEANUP-008: Docker Image Size Optimization
- **Priority**: SHOULD
- **Status**: PARTIAL (Sprint 3 close: 660MB vs <550MB stretch — ADR-009 illustrative 200-300MB jar-dedupe projection EMPIRICALLY FALSIFIED at S-ETS-03-04; chown-layer 80MB attack identified for Sprint 4); EXTENDED via REQ-ETS-CLEANUP-010 (Sprint 4 v2)
- **Description**: The multi-stage Dockerfile runtime image SHALL be optimized to ≤ 550 MB (Sprint 3 stretch — more permissive than ADR-009 §"Image size target" 450MB soft target). Recommended approach (per Quinn cleanup GAP-1 Option A): TE common-libs ↔ deps-closure dedupe — exclude jars in `target/lib-runtime/` that overlap with `/usr/local/tomcat/lib` (from `teamengine-web-common-libs.zip`); estimated 200-300MB savings → ~363-463MB runtime image. Architect ratifies which approach (a / b / c per Sprint 3 contract `deferred_to_architect`). PARTIAL with rationale acceptable if Generator hits 550-700MB; carryover to Sprint 4 with explicit deferral if >700MB. Smoke 12+6+N PASS preserved post-optimization. **Sprint 3 outcome**: 660MB (3MB savings; only 4 jars / 1.8MB exact-basename overlap on actual TE 5.6.1 + ETS 0.1-SNAPSHOT post-ADR-006 layout). Sprint 4 attacks the dominant 80MB chown layer per REQ-ETS-CLEANUP-010.
- **Maps to**: NFR-ETS-11 (deployment topology), ADR-009.

#### REQ-ETS-CLEANUP-009: CI Workflow ESCALATION (5th-sprint-defer-risk; binary close)
- **Priority**: MUST
- **Status**: IMPLEMENTED via PATH B / formal-drop (Sprint 4 Generator Run 1 2026-04-29; pending Quinn+Raze gate). `gh auth status` at run start showed token scopes `'gist', 'read:org', 'repo'` — `workflow` ABSENT; `ci/README.md` documents 2 future-activation paths; `ops/test-results/sprint-ets-04-01-ci-workflow-path-b-2026-04-29.txt` archived; `ops/status.md` updated with "Perpetual Environmental Blockers (DROPPED from sprint cadence)" section listing `ci_workflow_live`. Binary close achieved; future sprints will not re-litigate.
- **Description**: REQ-ETS-CLEANUP-007 (CI workflow live at `.github/workflows/build.yml`) was DEFERRED-WITH-RATIONALE for 4 consecutive sprints (S-ETS-01-01..03 + S-ETS-02-05 + S-ETS-03-03). Raze cumulative APPROVE_WITH_CONCERNS verdict explicitly flagged as ESCALATION TERRITORY. Sprint 4 success_criterion `ci_workflow_live_or_formally_dropped` SHALL resolve to TRUE via either: (a) user grants `workflow` scope (`gh auth refresh -s workflow`) before Generator run AND Generator executes the `git mv` + workflow_dispatch verification (~30 min); OR (b) `ci_workflow_live` is FORMALLY DROPPED from sprint cadence with explicit "perpetual environmental blocker" deferral note in `ops/status.md` (Raze recommendation). No more 4-sprint-style "we'll try again" deferrals — Sprint 4 is the binary close. Pat documents alternative path for the user to adopt at any point post-Sprint-4: manual GitHub UI move via web (~5 min user-time; bypasses gh-cli-scope blocker entirely).
- **Maps to**: NFR-ETS-02. Closes Raze cumulative CONCERN-3 + 4-sprint-defer pattern.

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
- **Description**: After the rsync `.git` include fix in `scripts/sabotage-test.sh` (S-ETS-06-02) AND the spring-javaformat-aware sabotage marker injection fix in S-ETS-07-01 Wedge 1, the sabotage script `--target=systemfeatures` SHALL run end-to-end at gate time producing a cascade XML showing: Core+Common all PASS; SystemFeatures 1×FAIL + Nx SKIP; Subsystems+Procedures+Deployments all SKIP. This closes the ADR-010 v3 "forward-extends to Procedures + Deployments" claim at the live-exec layer. The sabotage log message SHALL correctly distinguish Docker build failure from smoke @Test failure (Sprint 6 honesty fix; verified live in Sprint 7 Generator Run 1 first attempt).
- **Maps to**: ADR-010 §"Defense-in-depth role split". Closes GAP-2 from Sprint 5 Raze cumulative GAPS_FOUND 0.74 + Quinn cumulative APPROVE_WITH_CONCERNS 0.82 (cross-corroborated; reclassified from HIGH → MEDIUM per meta-Raze severity calibration). Closes Sprint 6 Raze HIGH GAP-1 + meta-Raze META-GAP-M2.

> Sprint 7 adds REQ-ETS-CLEANUP-018 (Sprint 6 carryover wedge bundle) and REQ-ETS-PART1-007..008 (Sampling Features + Property Definitions — twice-deferred from Sprints 5+6). Stories S-ETS-07-01..03 are Active Sprint 7.

#### REQ-ETS-CLEANUP-018: Sprint 6 Carryover Wedge Bundle (Sprint 7)
- **Priority**: MUST
- **Status**: IMPLEMENTED (Sprint 7 S-ETS-07-01 close 2026-04-30 — Generator Run 1; sister repo HEAD `c17a534 → 38b1f8a` after 5 commits: `a17c6ec` Wedges 1+3+4 initial, `94a4971` Wedge 1 spring-javaformat fix, `c68b803` Wedge 1 cascade XML evidence, `06acd1b` S-07-02+03 SamplingFeatures+PropertyDefinitions, `38b1f8a` Sprint 7 smoke 42/42 evidence; bd6fa9b Wedge 3 bash-x evidence). Wedge 1 (HIGH P0 — sabotage javac fix): two-line `if (true)\n\t\t\tthrow new AssertionError(...)` injection defeats javac reachability analysis (JLS §14.21) AND complies with spring-javaformat-maven-plugin:validate; verified live by 3-class cascade XML at sister `ops/test-results/sprint-ets-07-01-wedge1-sabotage-cascade-2026-04-30.xml`. Wedge 3 (MEDIUM P1 — credential-leak prong-b retarget): glob-safe targeting of `${SMOKE_OUTPUT_DIR}/s-ets-01-03-teamengine-container-*.log` archive (Sprint 6 timing fix output), fallback to `docker logs`; bash -x trace at sister `ops/test-results/sprint-ets-07-01-wedge3-cred-leak-prong-b-bash-x-trace.log` shows prong-b finds masked-form hit in correct archive. Wedge 4 (MEDIUM P1 — sabotage pipefail-unreachable fix): replaced `ls -t ... | head -1` pipeline with glob-safe `for _f in ... do [[ -e $_f ]] && ...` idiom; first attempt at /tmp clone live-exec exercised this path when initial Wedge 1 single-line shape failed spring-javaformat — disambiguation log line "Docker build FAILED (not a sabotage-marker hit)" fired correctly, confirming the fix. Wedge 5 (MEDIUM P1 — design.md doc-lag): added "Sprint 6 redesign: approach (i) — wire-side correctness via no-spec-mutation (S-ETS-06-01) — CANONICAL" subsection BEFORE the old wrap-pattern code, marked the entire historical block "Historical (Sprint 3 baseline — superseded by Sprint 6 approach (i) above)", explicitly invalidated the false try/finally claim per the new Sprint 7 generator_design_md_adr_self_audit success criterion. Wedge 6 (LOW — ADR-010 v3 retroval): natural fall-through; the cascade XML pointer is added to ADR-010 in this sprint. Wedge 2 (HIGH P0 — REQ-017 status honesty) was completed by Pat at planning time + promoted to IMPLEMENTED in this commit after Wedge 1 cascade XML production. mvn surefire 80 → 86/0/0/3 (added 6 lint tests for SF + Property). Smoke 34 → 42/42 against GeoRobotix.
- **Description**: Bundle fix for 6 Sprint 6 gate-identified defects: (1) `scripts/sabotage-test.sh` sabotage-marker injection javac unreachable-statement fix; (2) `scripts/sabotage-test.sh` pipefail-unreachable disambiguation block fix; (3) `scripts/credential-leak-e2e-test.sh` prong-b retarget; (4) `openspec/capabilities/ets-ogcapi-connectedsystems/design.md` §Sprint 3 hardening wrap-pattern doc-lag fix; (5) spec.md REQ-ETS-CLEANUP-017 status-honesty correction (Pat planning) + promotion to IMPLEMENTED (Generator post Wedge 1 close); (6) ADR-010 v3 Sprint 7 live-verification note. Acceptance criteria all met at Sprint 7 close: cascade XML produced, prong-b targeting verified via bash -x, disambiguation block fires under Docker build failure, design.md no longer contains false try/finally claim, REQ-017 status flipped to IMPLEMENTED with cascade XML evidence pointer, ADR-010 v3 retroval note added.
- **Maps to**: meta-Raze sprint-ets-06-meta-review.yaml META-GAP-M1, META-GAP-M2 (HIGH recalibrated). Closes Raze HIGH GAP-1, MEDIUM GAP-3 + Quinn MEDIUM GAP-Q1 from sprint-ets-06-adversarial-cumulative.yaml + sprint-ets-06-evaluator-cumulative.yaml. Implements Sprint 7 contract success criteria: `bash_x_trace_evidence_for_bash_changes`, `generator_design_md_adr_self_audit`, `spec_status_honesty_principle`.
- **Sprint 8 amendment (S-ETS-08-01 Wedge 2 — META-GAP-S7-1 closure)**: the live cascade evidence is no longer 3-class only. Raze's Sprint 7 gate-time sabotage exec from `/tmp/raze-fresh-sprint7/` produced a **5-class** cascade XML (archived per Raze cumulative gate evaluation evidence_artifacts) extending the Sprint 7 Generator's 3-class XML to all 5 SystemFeatures-level sibling classes (Subsystems + Procedures + Deployments + SamplingFeatures + PropertyDefinitions). Sprint 8 retires the prior phrasing ("live 3-class cascade XML produced end-to-end") in favour of "live cascade XML — 3-class at Generator run, 5-class at Raze gate"; the high-water-mark evidence is the Raze gate-time XML. ADR-010 v4 amendment (this sprint) records the Raze gate outcome. The dynamic sibling-enumeration fix landed in Sprint 8 S-ETS-08-01 Wedge 1 ensures the script's stdout VERDICT-summary now matches the actual cascade DAG width without further code edits as Sprint 8+ classes are added (e.g. Subdeployments).

#### REQ-ETS-PART1-007: Sampling Features Conformance Class (`/conf/sf`)
- **Priority**: SHOULD
- **Status**: IMPLEMENTED (Sprint 7 S-ETS-07-02 close 2026-04-30 — Generator Run 1; sister commit `06acd1b`). New class `src/main/java/.../conformance/samplingfeatures/SamplingFeaturesTests.java` with 4 @Tests all PASS against GeoRobotix (verified at /tmp clone smoke 42/42; sister `ops/test-results/sprint-ets-07-smoke-42-tests-2026-04-30.xml`): `samplingFeaturesCollectionReturns200` (HTTP 200 + non-empty items, 100 items), `samplingFeatureItemHasIdType` (canonical-endpoint shape), `samplingFeatureCanonicalUrlReturns200` (path-based dereferenceability — adapted to GeoRobotix shape which lacks per-item `links` array), `samplingFeaturesDependencyCascadeRuntime` (runtime tracer). All @Tests carry `groups="samplingfeatures"`. testng.xml extended: `<group name="samplingfeatures" depends-on="systemfeatures"/>` + `SamplingFeaturesTests` class entry in single-block consolidation (now 5 sibling classes depend on SystemFeatures). VerifyTestNGSuiteDependency extended with 3 lint tests: `testSamplingFeaturesGroupDependsOnSystemFeatures`, `testEverySamplingFeaturesTestMethodCarriesSamplingFeaturesGroup`, `testSamplingFeaturesCoLocatedWithSystemFeatures`. mvn 80 → 83 lint tests; smoke 38 confirmed.
- **OGC requirement prefix**: `/req/sf/` (HTTP 200 verified at raw.githubusercontent.com 2026-04-30 by Generator: `req_resources_endpoint.adoc`, `req_canonical_endpoint.adoc`, `req_canonical_url.adoc`. NOTE: OGC repo folder is `sf/` not `sampling/` — Pat's planning-time guidance was correct on URI form; folder naming clarified by Generator).
- **SF-unique implementation note**: GeoRobotix per-item shape (`/samplingFeatures/{id}`) does NOT include the `links` array that Procedures + Deployments items carry. Per defense-in-depth, the canonical-URL @Test asserts HTTP 200 at the path-based canonical URL form rather than `rel=canonical` link search; if a future GeoRobotix release adds item-level links the assertion can be tightened in lockstep.
- **Maps to**: PRD FR-ETS-17; twice-deferred from Sprint 5 (wedge-deferred) + Sprint 6 (wedge sprint, excluded).

#### REQ-ETS-PART1-008: Property Definitions Conformance Class (`/conf/property`)
- **Priority**: SHOULD
- **Status**: IMPLEMENTED (Sprint 7 S-ETS-07-03 close 2026-04-30 — Generator Run 1; sister commit `06acd1b`). New class `src/main/java/.../conformance/propertydefinitions/PropertyDefinitionsTests.java` with 4 @Tests: `propertiesCollectionReturns200` PASSes (HTTP 200 + items array present); `propertyItemHasIdType` and `propertyCanonicalUrlReturns200` SKIP-with-reason against current GeoRobotix `/properties` (returns empty `items: []` per IUT state — endpoint declared but no derived properties currently populated; per Pat MEDIUM risk PROPERTY-DEFINITIONS-RESPONSE-SHAPE mitigation); `propertyDefinitionsDependencyCascadeRuntime` PASSes (runtime tracer). 2 PASS + 2 SKIP-with-reason in smoke (verified at /tmp clone smoke 42/42). All @Tests carry `groups="propertydefinitions"`. testng.xml extended: `<group name="propertydefinitions" depends-on="systemfeatures"/>` + `PropertyDefinitionsTests` class entry in single-block consolidation. VerifyTestNGSuiteDependency extended with 3 lint tests: `testPropertyDefinitionsGroupDependsOnSystemFeatures`, `testEveryPropertyDefinitionsTestMethodCarriesPropertyDefinitionsGroup`, `testPropertyDefinitionsCoLocatedWithSystemFeatures`. mvn 83 → 86 lint tests; smoke 38 → 42 confirmed.
- **OGC requirement prefix**: `/req/property/` (HTTP 200 verified at raw.githubusercontent.com 2026-04-30 by Generator: `req_resources_endpoint.adoc`, `req_canonical_endpoint.adoc`, `req_canonical_url.adoc`).
- **Property-Definitions-unique implementation note**: GeoRobotix `/properties` returns HTTP 200 + `items: []` (empty). Per defense-in-depth + Pat MEDIUM risk mitigation, per-item @Tests SKIP-with-reason rather than FAIL when items empty (the OGC requirement is at the endpoint-existence + response-shape layer; population is IUT-state-dependent). If GeoRobotix populates the collection in the future, no code changes required — the same @Tests will exercise the cached single-property body.
- **Maps to**: PRD FR-ETS-18; twice-deferred from Sprint 5 + Sprint 6 (see REQ-ETS-PART1-007 rationale).

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

#### REQ-ETS-PART1-005: Subdeployments Conformance Class (`/conf/subdeployment`) (Sprint 8 target)
- **Priority**: MUST
- **Status**: IMPLEMENTED (Sprint 8 S-ETS-08-02 close 2026-04-30 — Generator Run 1). New class `src/main/java/.../conformance/subdeployments/SubdeploymentsTests.java` with 4 @Tests all SKIP-with-reason against GeoRobotix at sprint time (per IUT-state-honest SKIP policy — GeoRobotix curl-verified 2026-04-30T20:24Z: `/deployments/16sp744ch58g/subdeployments` returns HTTP 200 + empty `items: []`; all 4 @Tests SKIP via @BeforeClass cascade since "non-empty items" is part of `/req/subdeployment/collection` discipline and no per-item assertions can run on an empty collection). Smoke 42 → 46 (40 PASS + 6 SKIP — 4 new Subdeployments + 2 PropertyDefinitions empty-collection precedent). FIRST three-deep dependency chain in this ETS: `<group name="subdeployments" depends-on="deployments"/>` creates Subdeployments → Deployments → SystemFeatures → Core. Live cascade verified: sister `ops/test-results/sprint-ets-08-cascade-2026-04-30.xml` (76KB) — 6-class cascade (5 SystemFeatures-level direct + 1 Subdeployments transitive via Deployments), produced from `/tmp/dana-fresh-sprint8/` clone with sabotage `--target=systemfeatures`. testng.xml extended with subdeployments group dependency + SubdeploymentsTests class entry. VerifyTestNGSuiteDependency extended with 3 new lint tests: `testSubdeploymentsGroupDependsOnDeployments`, `testEverySubdeploymentsTestMethodCarriesSubdeploymentsGroup`, `testSubdeploymentsCoLocatedWithDeployments`. mvn 86 → 89 (BUILD SUCCESS, Failures: 0, Errors: 0, Skipped: 3). Sister commit at S-ETS-08-02 close.
- **Priority status correction**: original Pat planning narrative referenced `/conf/subdeployments` (plural) for the conformance class identifier. Generator curl-verified that GeoRobotix declares `/conf/subdeployment` (singular) per OGC 23-001 Annex A; OGC source repo also uses singular `/req/subdeployment/` directory naming with class identifier `/req/subdeployment` (singular) declared in `requirements_class_subdeployments.adoc`. Both forms appear in OGC sources at different abstraction layers (plural class file name; singular identifier path). The IUT and OGC source agree on the singular identifier; Generator implementation honors the singular form for all OGC URIs.
- **OGC requirement structure** (Generator HTTP-200-verified 2026-04-30T20:24Z): 5 .adoc files at `raw.githubusercontent.com/.../requirements/subdeployment/` — `requirements_class_subdeployments.adoc` (declares `inherit:: /req/deployment` — Subdeployments inherit Deployment resource exposure and canonical URL discipline from /req/deployment), `req_subcollection.adoc` (path `/deployments/{parentId}/subdeployments`), `req_recursive_param.adoc`, `req_recursive_search_deployments.adoc`, `req_recursive_search_subdeployments.adoc`. NOTE: there is NO `/req/subdeployment/parent-deployment-link` — Subdeployments do NOT have the equivalent of Subsystems' parent-system-link uniqueness; the inheritance from /req/deployment is the architectural composition mechanism. The 4 @Tests therefore use `/req/subdeployment/collection` (collection presence) + `/req/deployment/canonical-endpoint` (inherited Deployment resource endpoint exposure at `/deployments/{id}`) + `/req/deployment/canonical-url` (inherited canonical URL at `/deployments/{id}`) + the `/req/subdeployment` class URI (3-deep cascade runtime tracer). Any `id`, `type`, and `links` checks are ETS structural sanity checks for the returned resource representation and are not attributed solely to `req_canonical_endpoint.adoc`.
- **GeoRobotix IUT state at sprint time**: 1 deployment exists (`16sp744ch58g`); `/deployments/16sp744ch58g/subdeployments` returns HTTP 200 + empty `items` array. IUT declares `/conf/subdeployment` in `/conformance`. Future GeoRobotix release that populates the subdeployments collection automatically promotes 4 @Tests from SKIP to PASS without code changes — the @BeforeClass probe-loop scans up to 15 parent deployments looking for non-empty subdeployments and only SKIPs when no parent has non-empty children. Sprint 8 IUT-state-honest SKIP outcome is the contract-anticipated PASS-with-caveat per Pat planning (SUBDEPLOYMENTS-IUT-STATE-UNKNOWN risk MEDIUM mitigation).
- **Description**: For `/conf/subdeployment`, the ETS SHALL provide at least one TestNG `@Test` method whose `description` attribute starts with the OGC canonical `.adoc` requirement URI form `/req/subdeployment/<assertion>`. Generator MUST verify canonical URI form via OGC `.adoc` source HTTP-200 fetch before writing assertions. Generator verified the OGC source directory at `raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/subdeployment/`; it contains `requirements_class_subdeployments.adoc`, `req_subcollection.adoc`, `req_recursive_param.adoc`, `req_recursive_search_deployments.adoc`, and `req_recursive_search_subdeployments.adoc`, with Subdeployments inheriting Deployment canonical endpoint and canonical URL discipline from `/req/deployment`. The class lives at `org.opengis.cite.ogcapiconnectedsystems10.conformance.subdeployments.SubdeploymentsTests`. Subdeployments DEPENDS ON Deployments via `<group name="subdeployments" depends-on="deployments"/>` — this creates the 3-deep cascade chain Subdeployments→Deployments→SystemFeatures→Core. Coverage scope Sprint 8: Sprint-1-style minimal (4 @Tests per pattern): (a) GET /deployments/{id}/subdeployments HTTP 200 + non-empty items; (b) inherited Deployment canonical endpoint exposure at `/deployments/{id}` plus ETS structural sanity checks on the returned resource; (c) inherited Deployment canonical URL at `/deployments/{id}`; (d) 3-deep cascade runtime tracer. If GeoRobotix does not declare `/conf/subdeployment` in conformance OR returns 404 for `/deployments/{id}/subdeployments`, all @Tests SKIP-with-reason (IUT-state-honest per sprint policy).
- **Rationale**: Subdeployments completes the deepest dependency chain in Part 1. Deployments (S-ETS-05-06, Sprint 5 IMPLEMENTED) is the parent. Subdeployments→Deployments→SystemFeatures→Core is the same structural depth as the Subsystems→SystemFeatures→Core chain proven at Sprint 4, extended one level. Completing this chain proves the n-level cascade pattern scales to 3 levels.
- **Maps to**: PRD FR-ETS-15.

> Sprint 11 selects AdvancedFiltering as the next Part 1 increment because it is read-only. The sprint is intentionally declaration-gated and partial: GeoRobotix currently does not declare `/conf/advanced-filtering`, so the default smoke expectation is SKIP-with-reason rather than false PASS. Planning probes show GeoRobotix accepts some query parameters, but undeclared behavior is not conformance evidence.

#### REQ-ETS-PART1-009: AdvancedFiltering Conformance Class (`/conf/advanced-filtering`) (Sprint 11 target)
- **Priority**: MUST
- **Status**: PARTIAL-IMPLEMENTED by Sprint 11 Generator and gates (2026-05-05; story S-ETS-11-01; Quinn Gate 3.5 APPROVE_WITH_CONCERNS 0.90; Raze Gate 4 APPROVE_WITH_CONCERNS 0.90). Implemented class `org.opengis.cite.ogcapiconnectedsystems10.conformance.advancedfiltering.AdvancedFilteringTests` with 6 read-only @Tests. Verification: Java formatter via Docker Maven BUILD SUCCESS; Docker Maven `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `98 tests / 0 failures / 0 errors / 3 skipped`; TeamEngine smoke from `/tmp/sprint-ets-11-generator-smoke` with external `SMOKE_OUTPUT_DIR=/tmp/sprint-ets-11-generator-smoke-results` reported `63 total / 48 passed / 0 failed / 15 skipped`. Independent Quinn/Raze gate smoke runs also reported `63 total / 48 passed / 0 failed / 15 skipped`. Current GeoRobotix does not declare `/conf/advanced-filtering`, so all 6 AdvancedFiltering @Tests SKIP with reason and no undeclared query behavior is counted as PASS.
- **OGC source verified**: Upstream `opengeospatial/ogcapi-connected-systems` commit `3fd86c73e744b7e2faaf7f1c17366bfb9ff4cd6f`. Requirement class file exists at `api/part1/standard/requirements/query/requirements_class_advanced_filtering.adoc`; explanatory clause exists at `api/part1/standard/sections/clause_15_requirements_class_advanced_filtering.adoc`. The OpenAPI fragment for `ID_List` exists at `api/part1/openapi/parameters/idListSchema.yaml`. The class identifier is `/req/advanced-filtering`, inherits `/req/api-common`, and lists query-parameter subrequirements for ID lists, common resource keyword/id filters, geometry filters, system/deployment/procedure/sampling-feature/property association filters, and combined filters.
- **Sprint 11 coverage scope**: AdvancedFiltering systems/common-resource read-only subset with 6 @Tests: (1) IUT declares `/conf/advanced-filtering`, otherwise every AdvancedFiltering @Test SKIPs with reason; (2) ID-list schema validator helper accepts homogeneous non-empty local-ID lists and homogeneous non-empty UID lists while rejecting mixed local/UID lists and empty/malformed lists; (3) `/systems?id=<known-id>` returns HTTP 200 and a non-empty result set whose returned items all preserve the selected id when the conformance class is declared and a seed System id was selected; (4) `/systems?q=<known keyword>` returns HTTP 200 and a non-empty result set whose returned items include keyword evidence in `name` or `description` when declared and a seed keyword was selected from a System name/description; (5) `/systems?geom=<WKT>` is exercised with a broad WKT geometry and validated only for HTTP 200 + JSON response shape in this sprint; (6) TestNG dependency wiring and smoke no-regression. The sprint deliberately does not close all 24 listed advanced-filtering subrequirements.
- **ID_List examples for Sprint 11 helper**: Based on upstream `idListSchema.yaml` and clause 15 text, valid examples include `0mqcvdnfoca0`, `0mqcvdnfoca0,0ngu9lvstls0`, `urn:osh:sensor:simweather:0123456879`, `urn:osh:sensor:simweather:0123456879,urn:osh:sensor:simweather:9876543210`, and the resource-by-id UID-prefix query value `urn:osh:sensor:simweather:*`. Invalid examples include an empty value, `,`, `0mqcvdnfoca0,urn:osh:sensor:simweather:0123456879`, and `urn:osh:sensor:bad value`. This is a local schema-helper test only; it does not prove every endpoint's query semantics.
- **Dependency wiring**: AdvancedFiltering depends on SystemFeatures via `<group name="advancedfiltering" depends-on="systemfeatures"/>`; the planned tests exercise System resources first and must cascade-SKIP when SystemFeatures fails.
- **Open subrequirements after Sprint 11**: Deployment/procedure/sampling-feature/property association filters, system-by-parent/procedure/foi/observedProperty/controlledProperty semantic result validation, full geometry intersection correctness, combined filter truth-table validation, collection-wide all-resource endpoint parity, and any Part 2 query requirements remain OPEN unless separately planned.
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
**WHEN** `scripts/smoke-test.sh` runs from a `/tmp` clone against the default GeoRobotix target
**THEN** failed=0
**AND** total PASS+SKIP is at least 63 (Sprint 10 baseline 57 plus 6 AdvancedFiltering @Tests)
**AND** AdvancedFiltering results SKIP-with-reason if `/conf/advanced-filtering` remains absent.
*Maps to*: REQ-ETS-PART1-009.

> Sprint 12 starts the mutation-side Part 1 work with Create/Replace/Delete, but it does not permit unguarded writes against the public GeoRobotix smoke target. GeoRobotix declares `/conf/create-replace-delete` and advertises POST/PUT/DELETE via OPTIONS, so default smoke must prove declaration and non-mutating readiness while every lifecycle mutation assertion SKIPs unless an operator explicitly enables mutation tests against a dedicated mutable IUT.

#### REQ-ETS-PART1-010: Create/Replace/Delete Conformance Class (`/conf/create-replace-delete`) (Sprint 12 target)
- **Priority**: MUST
- **Status**: PARTIAL-IMPLEMENTED by Sprint 12 Generator (2026-05-05; story S-ETS-12-01). Implemented outcome is declaration, non-mutating method-advertisement readiness, TestNG wiring, explicit mutation opt-in plumbing, public GeoRobotix hard-denial, default-smoke safety, service-relative `Location` handling for OSH-style `/systems/{id}` responses, and a guarded lifecycle path for dedicated mutable IUTs. Full create/replace/delete lifecycle conformance remains OPEN for the overall requirement class because deployment/procedure/sampling-feature/property CRUD, cascade behavior, custom collections, `text/uri-list`, and `/conf/update` remain out of scope.
- **OGC source verified**: Upstream `opengeospatial/ogcapi-connected-systems` commit `3fd86c73e744b7e2faaf7f1c17366bfb9ff4cd6f`. Requirement class file exists at `api/part1/standard/requirements/crud/requirements_class_crd.adoc`; explanatory clause exists at `api/part1/standard/sections/clause_16_requirements_class_create_replace_delete.adoc`. The class identifier is `/req/create-replace-delete`, inherits `/req/api-common` and OGC API Features Part 4 Create/Replace/Delete, and lists subrequirements for systems, system delete cascade, subsystems, deployments, subdeployments, procedures, sampling features, properties, collection propagation, and adding resources to collections by `text/uri-list`.
- **Sprint 12 coverage scope**: Create/Replace/Delete safety-gated systems subset with 6 planned @Tests: (1) IUT declares `/conf/create-replace-delete`; (2) default mutation safety gate is active unless suite parameter `mutation-tests-enabled=true` is supplied together with `mutation-iut-policy=dedicated-mutable-iut`; (3) `OPTIONS /systems` is recorded as an ETS readiness precondition for POST advertisement without issuing POST; (4) `OPTIONS /systems/{id}` is recorded as an ETS readiness precondition for PUT/DELETE advertisement without issuing PUT/DELETE; (5) systems lifecycle create/replace/delete test SKIPs by default with reason and, only when explicitly enabled against a dedicated mutable IUT that is not a known shared public GeoRobotix URL, performs POST/PUT/DELETE with best-effort cleanup; (6) TestNG dependency wiring and smoke no-regression. OPTIONS readiness PASS does not satisfy `/req/create-replace-delete/system`; lifecycle conformance remains SKIP by default until POST/PUT/DELETE run against a dedicated mutable IUT. The sprint deliberately does not close deployment/procedure/sampling-feature/property CRUD, cascade delete semantics, collection propagation, `text/uri-list`, or update/PATCH.
- **Mutation safety policy**: Mutating HTTP methods MUST NOT run during default GeoRobotix smoke even though GeoRobotix currently declares `/conf/create-replace-delete` and advertises `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS` on `/systems` and `/systems/{id}`. Generator MUST introduce explicit opt-in parameters and a hard safety gate before any POST/PUT/DELETE request is issued. The parameter path is in scope end-to-end: `TestRunArg`, `SuiteAttribute`, `SuiteFixtureListener`, optional `TestNGController` validation/acceptance, CTL controls, and optional smoke-script env forwarding (`SMOKE_MUTATION_TESTS_ENABLED`, `SMOKE_MUTATION_IUT_POLICY`). Even when both opt-in parameters are present, the implementation MUST hard-deny mutation against known shared public GeoRobotix URLs, including `https://api.georobotix.io/ogc/t18/api`. Default smoke MUST report lifecycle mutation assertions as SKIP-with-reason, not PASS.
- **No-mutation smoke oracle**: Default smoke no-mutation proof MUST inspect IUT-bound REST Assured request-log entries, not naive process-wide method strings. The oracle parses current `Request: METHOD URI` entries and the older adjacent `Request method:` / `Request URI:` pair format, filters to URIs starting with the IUT base URL, requires at least one recognized IUT-bound request entry, and requires zero POST/PUT/DELETE entries for GeoRobotix. The TeamEngine control-plane POST that starts the suite run is excluded from this oracle because its URI is not IUT-bound.
- **Dependency wiring**: Create/Replace/Delete depends on SystemFeatures via `<group name="createreplacedelete" depends-on="systemfeatures"/>`; the Sprint 12 systems subset requires canonical System resource availability before method-advertisement or lifecycle checks.
- **Open subrequirements after Sprint 12**: System delete cascade, subsystem creation, deployment/subdeployment/procedure/sampling-feature/property create/replace/delete, custom collection propagation, adding resources to collections by `text/uri-list`, and all `/conf/update` PATCH behavior remain OPEN unless separately planned.
- **Generator evidence**: Docker Maven `105 tests / 0 failures / 0 errors / 3 skipped`; TeamEngine smoke from `/tmp/sprint-ets-12-generator-smoke-current-r3` against GeoRobotix `69 total / 52 passed / 0 failed / 17 skipped`; CreateReplaceDelete runtime outcome is 4 PASS and 2 SKIP-by-safety-gate; integrated smoke log oracle reported zero IUT-bound POST/PUT/DELETE entries after recognizing 40 IUT-bound request log entries.
- **Local mutable-IUT follow-up evidence**: Local OpenSensorHub 2.0-beta2 at `http://localhost:8081/sensorhub/api`, reached by TeamEngine over Docker network `field-hub_default` as `http://field-hub-osh-1:8081/sensorhub/api`, declares `/conf/create-replace-delete` and permits admin-authenticated transactions. Probe `r4` (`/tmp/ets-csapi-osh-mutable-smoke-r4`) produced real CRD PASS evidence for `systemsCreateReplaceDeleteLifecycle`: POST `/systems`, PUT `/systems/0410`, and DELETE `/systems/0410` all succeeded after the ETS preserved the created System `uid` across replacement and resolved `Location: /systems/0410` against the IUT service base. Follow-up on 2026-05-06 updated the local OSH `proxyBaseUrl` to `http://field-hub-osh-1:8081`, seeded synthetic System/Procedure/Deployment/SamplingFeature resources from `ops/local-osh-seed-fixtures.json`, set the System `featureType` to `http://www.w3.org/ns/sosa/System` so SensorML `?f=sml3` resolves locally, and reran TeamEngine smoke from `/tmp/ets-csapi-osh-full-health-r3`: `69 total / 50 passed / 0 failed / 19 skipped`. Skips remain expected for undeclared or unpopulated out-of-scope surfaces such as AdvancedFiltering, GeoJSON feature-collection fallback, properties, subsystems, and subdeployments. Raze full-health review found and the same turn fixed two false-confidence gaps: smoke stdout now prints exact parsed totals instead of `${total}/${total}`, and the local OSH seed payloads are versioned. The local OSH evidence upgrades the maintained mutable-IUT health target from CRD-only PASS to full smoke failed=0, but the overall REQ remains PARTIAL for non-system CRUD and unimplemented CRD subrequirements.
- **Maps to**: PRD FR-ETS-20.

> Sprint 13 continues the mutation-side Part 1 work with Update/PATCH. It reuses the Sprint 12 mutation-safety contract. GeoRobotix does not currently declare `/conf/update`, and `OPTIONS /systems/{id}` does not advertise PATCH, so default smoke must report Update assertions as SKIP-with-reason and prove that no IUT-bound PATCH was issued.

#### REQ-ETS-PART1-011: Update Conformance Class (`/conf/update`) (Sprint 13)
- **Priority**: MUST
- **Status**: PARTIAL-IMPLEMENTED by Sprint 13 Generator (2026-05-06; story S-ETS-13-01). Implemented declaration-gated `/conf/update`, PATCH mutation safety gate, non-mutating `OPTIONS /systems/{id}` readiness, default lifecycle SKIP-before-PATCH, hard-denial for public GeoRobotix, TestNG dependency on Create/Replace/Delete, and default-smoke no-PATCH evidence. Full Update conformance remains OPEN for deployment/procedure/sampling-feature/property PATCH, Feature Collection update paths, Part 2 update, optimistic locking, and PATCH media-type matrix.
- **OGC source verified**: OGC API - Connected Systems Part 1 Clause 17, Requirements Class "Update" `/req/update`, Conformance Class A.11 `/conf/update`. The upstream requirement class source is `api/part1/standard/requirements/crud/update/requirements_class_update.adoc` at `opengeospatial/ogcapi-connected-systems` commit `3fd86c73e744b7e2faaf7f1c17366bfb9ff4cd6f`; the explanatory clause is `api/part1/standard/sections/clause_17_requirements_class_update.adoc`. The class prerequisite is `/req/create-replace-delete` plus OGC API Features Part 4 `/req/update`. Normative statements are `/req/update/system`, `/req/update/deployment`, `/req/update/procedure`, `/req/update/sampling-feature`, and `/req/update/property`. OGC Part 1 ATS A.79-A.83 also lists Feature Collection item update paths under `/collections/{collectionId}/items/{id}` for systems, deployments, procedures, sampling features, and properties; Sprint 13 explicitly defers those collection item PATCH paths.
- **Sprint 13 coverage scope**: Update safety-gated systems subset with 5 @Tests: (1) IUT declares `/conf/update`, otherwise Update tests SKIP with reason; (2) default mutation safety gate is active unless suite parameter `mutation-tests-enabled=true` is supplied together with `mutation-iut-policy=dedicated-mutable-iut`; (3) `OPTIONS /systems/{id}` is recorded as an ETS readiness precondition for PATCH advertisement without issuing PATCH; (4) systems PATCH lifecycle test SKIPs by default with reason and, only when explicitly enabled against a dedicated mutable IUT that declares `/conf/update`, advertises PATCH, and is not a known shared public GeoRobotix URL, performs PATCH with best-effort cleanup; (5) TestNG dependency wiring and smoke no-regression. OPTIONS readiness PASS does not satisfy `/req/update/system`; lifecycle conformance remains SKIP by default until PATCH runs against a dedicated mutable IUT.
- **Mutation safety policy**: PATCH MUST NOT run during default GeoRobotix smoke. Sprint 13 reuses Sprint 12's mutation opt-in parameters and hard-denial list. Even when both opt-in parameters are present, the implementation MUST hard-deny mutation against known shared public GeoRobotix URLs, including `https://api.georobotix.io/ogc/t18/api`, before any PATCH is issued. Default smoke MUST report lifecycle mutation assertions as SKIP-with-reason, not PASS.
- **No-mutation smoke oracle**: Default smoke no-mutation proof MUST include PATCH as a mutating method. The existing IUT-bound request-log oracle parses current `Request: METHOD URI` entries and older adjacent `Request method:` / `Request URI:` pair format, filters to URIs starting with the IUT base URL, requires at least one recognized IUT-bound request entry, and requires zero POST/PUT/DELETE/PATCH entries for GeoRobotix. The TeamEngine control-plane POST that starts the suite run is excluded from this oracle because its URI is not IUT-bound.
- **Dependency wiring**: Update depends on Create/Replace/Delete via `<group name="update" depends-on="createreplacedelete"/>`. The Sprint 13 systems subset requires the Sprint 12 mutation safety gate and Create/Replace/Delete prerequisite wiring before PATCH behavior can be assessed.
- **Planning probe evidence**: GeoRobotix `/conformance` on 2026-05-06 does not declare `http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/update`. `OPTIONS https://api.georobotix.io/ogc/t18/api/systems/0mqcvdnfoca0` returned HTTP 200 with `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS`; PATCH is absent. Local OSH unauthenticated `/conformance` returned HTTP 401, and unauthenticated `OPTIONS /systems/040g` returned HTTP 200 with no PATCH in `Allow`; authenticated local OSH remains useful as a mutable fixture but does not currently provide positive PATCH evidence.
- **Generator verification evidence**: Docker Maven `bash scripts/mvn-test-via-docker.sh` completed BUILD SUCCESS with `113 tests / 0 failures / 0 errors / 3 skipped`; Maven log archived at `ops/test-results/sprint-ets-13-maven-2026-05-06.log`. TeamEngine default smoke against GeoRobotix reported `74 total / 52 passed / 0 failed / 22 skipped` with 41 recognized IUT-bound request-log entries and zero IUT-bound POST/PUT/DELETE/PATCH entries. Because Update depends on Create/Replace/Delete and the default CRD mutation gate skips, the Update configuration method records missing `/conf/update`, while the five Update @Tests are dependency-skipped through `createreplacedelete`; no PATCH is issued.
- **Sprint 14 hardening evidence**: `S-ETS-14-01` keeps REQ-ETS-PART1-011 PARTIAL and strengthens only the guarded systems PATCH path. Positive PATCH lifecycle evidence now requires a GET after PATCH and an assertion that the intended changed field, initially `properties.name`, changed to the expected value. A PATCH status code alone is not conformance evidence. Missing `OPTIONS Allow: PATCH` follows an explicit verdict matrix: absent `/conf/update`, missing mutation opt-in, public IUT hard-denial, no candidate resource, or inconclusive OPTIONS are SKIP-before-PATCH states; declared `/conf/update` plus successful `OPTIONS /systems/{id}` whose `Allow` omits PATCH FAILs the readiness assertion for `/req/update/system`, while the lifecycle test still SKIPs before PATCH because the precondition failed; declared `/conf/update` plus explicit mutation opt-in plus `Allow: PATCH` may run the guarded lifecycle. Docker Maven reported `117 tests / 0 failures / 0 errors / 3 skipped`; default TeamEngine smoke against GeoRobotix reported `74 total / 52 passed / 0 failed / 22 skipped` and zero IUT-bound POST/PUT/DELETE/PATCH across 41 recognized IUT-bound request-log entries. Local OSH remains a dedicated mutable CRD fixture, but current Generator evidence shows `/conformance` returned HTTP 401 and `OPTIONS /systems/040g` does not advertise PATCH; Sprint 14 does not claim local OSH positive Update support.
- **Open subrequirements after Sprint 13**: Deployment, procedure, sampling-feature, and property PATCH; Feature Collection update paths under `/collections/{collectionId}/items/{id}`; Part 2 `/conf/update`; optimistic locking; and PATCH media-type matrix, including JSON Patch, merge patch, and content negotiation, remain OPEN unless separately planned.
- **Maps to**: PRD FR-ETS-21.

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
**WHEN** `scripts/smoke-test.sh` runs from a `/tmp` clone against the default GeoRobotix target
**THEN** failed=0
**AND** total PASS+SKIP increases by the number of new Create/Replace/Delete @Tests
**AND** default smoke logs contain zero IUT-bound POST, PUT, or DELETE request-log entries from the Create/Replace/Delete suite, using recognized REST Assured `Request: METHOD URI` or adjacent `Request method:` / `Request URI:` entries filtered to the IUT base URL.
*Maps to*: REQ-ETS-PART1-010.

> Sprint 9 starts the remaining encoding classes with GeoJSON only. This is intentionally narrower than the v1.0 web-app story that paired GeoJSON + SensorML: GeoJSON is read-only, declared by GeoRobotix, and reuses existing Feature/FeatureCollection validation patterns, while SensorML has broader SensorML 3.0 schema inheritance and remains deferred.

#### REQ-ETS-PART1-012: GeoJSON Encoding Conformance Class (`/conf/geojson`) (Sprint 9 target)
- **Priority**: MUST
- **Status**: PARTIAL-IMPLEMENTED (Sprint 9 Generator 2026-05-05; Sprint 15 non-system read-only expansion Generator 2026-05-06; Sprint 17 selected-resource relation-types Generator 2026-05-07; Sprint 18 relation-types breadth Generator 2026-05-07; Sprint 19 mediatype-write safety-gated Generator 2026-05-07; stories S-ETS-09-01, S-ETS-15-01, S-ETS-17-01, S-ETS-18-01, and S-ETS-19-01). Sprint 19 adds safety-gated `/req/geojson/mediatype-write` checks with positive system-resource evidence against a dedicated local OSH mutable IUT. Sprint 18 added independent GeoJSON relation-types checks for selected System, Deployment, Procedure, and Sampling Feature items. Sprint 9 closed the systems read-only subset. Sprint 15 adds deployment/procedure/sampling-feature read-only schema and mapping checks with fallback honesty when an IUT returns default CS API `items` wrappers. Full REQ-ETS-PART1-012 remains open until broader positive relation-types evidence where IUT resources expose association links, property GeoJSON mapping, non-system mutation-side encoding coverage, and full schema-validation closure are implemented.
- **OGC source verified**: Upstream master commit `3fd86c73e744b7e2faaf7f1c17366bfb9ff4cd6f` dated 2026-04-20. Requirement class file exists at `api/part1/standard/requirements/encoding/geojson/requirements_class_geojson.adoc`. The class identifier is `/req/geojson`, inherits `/req/api-common` and OGC API Features 1.0 GeoJSON, and lists 12 subrequirements: `mediatype-read`, `mediatype-write`, `relation-types`, `feature-attribute-mapping`, `system-schema`, `system-mappings`, `deployment-schema`, `deployment-mappings`, `procedure-schema`, `procedure-mappings`, `sf-schema`, and `sf-mappings`.
- **Sprint 9 coverage scope**: Sprint-1-style minimal systems read-only subset with 5 @Tests: (1) IUT declares `/conf/geojson`; (2) `Accept: application/geo+json` or default JSON response for `/systems` returns HTTP 200 + honest media-type/fallback reporting; (3) `/systems` GeoJSON path requires `type="FeatureCollection"` and a `features` array; (4) first system feature carries GeoJSON `Feature` shape with `id`, `type`, `geometry`, and `properties`; (5) TestNG dependency wiring and smoke no-regression.
- **Sprint 15 coverage scope**: `GeoJsonTests` now adds 3 non-system read-only @Tests for `/deployments`, `/procedures`, and `/samplingFeatures` with `Accept: application/geo+json`. Each test requires GeoJSON `FeatureCollection` + `features` before PASS, rejects CS API `items` wrappers as SKIP fallback evidence, and requires resource-specific mapping evidence: deployment `properties.uid` plus non-empty `properties.deployedSystems@link`, procedure `geometry == null` plus `properties.uid` and `properties.featureType`, and sampling feature `properties.uid`, `properties.featureType`, plus non-empty `properties.hostedProcedure@link` or `properties.radius`. `VerifyGeoJsonResourceMappingAssertions` pins helper behavior for fallback SKIP and mapping-value checks.
- **Dependency wiring**: GeoJSON depends on SystemFeatures via `<group name="geojson" depends-on="systemfeatures"/>`. This keeps encoding validation behind the canonical system feature resource availability already implemented in REQ-ETS-PART1-002.
- **Implementation evidence**: Sprint 9: `bash scripts/mvn-test-via-docker.sh` in the sister repo reports BUILD SUCCESS with surefire `Tests run: 92, Failures: 0, Errors: 0, Skipped: 3`. `scripts/smoke-test.sh` from `/tmp/sprint-ets-09-smoke-fix` reports `total=51 passed=42 failed=0 skipped=9`; GeoJSON contributed 2 PASS and 3 SKIP. Sprint 15: formatter BUILD SUCCESS; `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS with `122 tests / 0 failures / 0 errors / 3 skipped`, log `ops/test-results/sprint-ets-15-maven-2026-05-06.log`; TeamEngine smoke `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s15-generator bash scripts/smoke-test.sh` reported `77 total / 52 passed / 0 failed / 25 skipped` with zero IUT-bound POST/PUT/DELETE/PATCH across 44 recognized IUT request-log entries. Sprint 17: formatter BUILD SUCCESS; Docker Maven BUILD SUCCESS with `133 tests / 0 failures / 0 errors / 3 skipped`, log `ops/test-results/sprint-ets-17-maven-2026-05-06.log`; TeamEngine smoke `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s17-generator bash scripts/smoke-test.sh` reported `82 total / 55 passed / 0 failed / 27 skipped`, with GeoJSON relation-types PASS and zero IUT-bound POST/PUT/DELETE/PATCH across 55 recognized IUT request-log entries. Sprint 18: formatter BUILD SUCCESS; Docker Maven BUILD SUCCESS with `136 tests / 0 failures / 0 errors / 3 skipped`, log `ops/test-results/sprint-ets-18-maven-2026-05-07.log`; TeamEngine smoke `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s18-generator bash scripts/smoke-test.sh` reported `87 total / 55 passed / 0 failed / 32 skipped`, with GeoJSON System relation-types PASS, GeoJSON Deployment/Procedure/SamplingFeature breadth checks SKIP independently, and zero IUT-bound POST/PUT/DELETE/PATCH across 69 recognized IUT request-log entries. Sprint 17 Raze implementation review `.harness/evaluations/sprint-ets-17-adversarial-implementation.yaml` returned `APPROVE` confidence 0.91 with no required fixes.
- **IUT-state policy**: If GeoRobotix does not declare `/conf/geojson`, GeoJSON @Tests SKIP-with-reason rather than FAIL. Current GeoRobotix declares `/conf/geojson`, but `GET /systems` with `Accept: application/geo+json` returns `Content-Type: application/json` and a CS API `items` wrapper, not a GeoJSON `FeatureCollection` with `features`. Therefore `systemsCollectionIsGeoJsonFeatureCollection` SKIPs with reason and `systemFeatureHasGeoJsonShapeAndProperties` SKIPs by dependency; this is fallback evidence, not a GeoJSON FeatureCollection PASS.
- **Sprint 17 implemented relation-types scope**: For associations encoded in a JSON `links` member, relation-types checks require `rel` to equal the association name valid for the selected resource type. Generic `canonical`, `alternate`, pagination, collection, service-desc, and service-doc links are not association evidence. Property-level links such as `deployedSystems@link` and `hostedProcedure@link` remain mapping evidence, not `links` member relation-types evidence. `EncodingRelationTypes` uses resource-specific GeoJSON links-member allowlists derived from the OGC association tables: System (`parentSystem`, `subsystems`, `samplingFeatures`, `deployments`, `procedures`, `datastreams`, `controlstreams`); Deployment (`parentDeployment`, `subdeployments`, `featuresOfInterest`, `samplingFeatures`, `datastreams`, `controlstreams`); Procedure (`implementingSystems`); Sampling Feature (`parentSystem`, `sampleOf`, `datastreams`, `controlstreams`). The runtime GeoJSON assertion currently checks a selected System representation.
- **Sprint 18 implemented relation-types breadth scope**: `GeoJsonTests` now has 12 read-only @Tests and evaluates relation-types independently for selected System, Deployment, Procedure, and Sampling Feature representations. Each assertion PASSes, FAILs, or SKIPs independently so the System PASS cannot hide non-system SKIPs. GeoRobotix runtime on 2026-05-07: System PASSed from `samplingFeatures` and `datastreams`; Deployment and Procedure SKIPped because item `links` members contain only generic `canonical`/`alternate` links; Sampling Feature SKIPped because the selected item has no top-level `links` member. Property-level `deployedSystems@link` and `hostedProcedure@link` remain excluded from relation-types PASS evidence.
- **Sprint 19 implemented mediatype-write scope**: `GeoJsonTests` now has 13 @Tests and adds `geoJsonMediaTypeWriteParsesSystemBodyWhenMutationEnabled`. The test checks write-side `Content-Type: application/geo+json` parsing behind the existing Sprint 12 mutation safety gate, requires `/conf/create-replace-delete`, hard-denies public GeoRobotix, and requires follow-up dereference evidence preserving the submitted UID. GeoRobotix declares `/conf/create-replace-delete` and `/conf/geojson`, and OPTIONS advertises POST/PUT/DELETE, but GeoRobotix is a shared public IUT and was not mutated by default smoke. OPTIONS readiness alone is not conformance evidence. Sprint 19 verification: formatter BUILD SUCCESS; Docker Maven BUILD SUCCESS with `144 tests / 0 failures / 0 errors / 3 skipped`, log `ops/test-results/sprint-ets-19-maven-r3-2026-05-07.log`; GeoRobotix TeamEngine smoke r3 reported `89 total / 55 passed / 0 failed / 34 skipped`, with both mediatype-write tests SKIP-before-mutation and zero IUT-bound POST/PUT/DELETE/PATCH across 69 recognized IUT request-log entries. Authenticated local OSH mutable-IUT smoke r3 reported `89 total / 52 passed / 4 failed / 33 skipped`; the GeoJSON mediatype-write test PASSed with exact `Content-Type=application/geo+json`, follow-up GET, and cleanup DELETE evidence. The four local failures were SensorML non-system HTTP 500 responses outside GeoJSON mediatype-write.
- **Maps to**: PRD FR-ETS-22.

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
**WHEN** `scripts/smoke-test.sh` runs against the default GeoRobotix target
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

> Sprint 10 targets SensorML as another read-only encoding increment. The sprint is intentionally PARTIAL for the SensorML requirement class: it proves conformance declaration, discovers a SensorML alternate representation for an existing System resource, fetches that representation, and checks a minimal SensorML system shape. It does not close write media type behavior, relation types, deployments/procedures/properties SensorML schema or mapping assertions, or full JSON Schema validation. Alternate-link fallback is evidence for this sprint subset only unless the fetched body proves SensorML JSON support and the fallback is documented in runtime output.

#### REQ-ETS-PART1-013: SensorML Encoding Conformance Class (`/conf/sensorml`) (Sprint 10 + Sprint 16 + Sprint 17 target)
- **Priority**: MUST
- **Status**: PARTIAL-IMPLEMENTED by Sprint 10 Generator, Sprint 16 Generator, Sprint 17 selected-resource relation-types Generator, Sprint 18 relation-types breadth Generator, and Sprint 19 mediatype-write safety-gated Generator (story S-ETS-10-01 gate-closed 2026-05-05; story S-ETS-16-01 Generator complete and Raze-approved 2026-05-06; story S-ETS-17-01 Generator complete 2026-05-07; story S-ETS-18-01 Generator complete 2026-05-07; story S-ETS-19-01 Generator complete 2026-05-07). Sprint 19 adds safety-gated `/req/sensorml/mediatype-write` checks with positive system-resource evidence against a dedicated local OSH mutable IUT. Sprint 18 added independent SensorML relation-types checks for selected System, Deployment, and Procedure representations. Sprint 10 implemented class `org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml.SensorMlTests` with 6 read-only @Tests. Sprint 16 extends it to 9 read-only @Tests with deployment/procedure/property SensorML schema/mapping checks while keeping the full REQ partial. Sprint 17 extends it to 10 read-only @Tests and adds shared helper coverage. Sprint 18 extends it to 12 read-only @Tests. Sprint 19 extends it to 13 @Tests with mutation-gated mediatype-write parsing evidence. Sprint 19 verification: formatter BUILD SUCCESS; Docker Maven BUILD SUCCESS, `144 tests / 0 failures / 0 errors / 3 skipped`, log `ops/test-results/sprint-ets-19-maven-r3-2026-05-07.log`; GeoRobotix TeamEngine smoke r3 reported `89 total / 55 passed / 0 failed / 34 skipped` with zero IUT-bound POST/PUT/DELETE/PATCH across 69 recognized request-log entries. Local OSH mutable-IUT smoke r3 reported `89 total / 52 passed / 4 failed / 33 skipped`; the SensorML mediatype-write test PASSed with exact `Content-Type=application/sml+json`, follow-up GET, and cleanup DELETE evidence. GeoRobotix runtime used explicit `application/sml+json` alternate links for deployment `https://api.georobotix.io/ogc/t18/api/deployments/16sp744ch58g?f=sml3`, procedure `https://api.georobotix.io/ogc/t18/api/procedures/164p7ed8l47g?f=sml3`, and system `https://api.georobotix.io/ogc/t18/api/systems/0mqcvdnfoca0?f=sml3`; CS API `items` wrappers and default Feature JSON are not counted as SensorML PASS.
- **OGC source verified**: Upstream `opengeospatial/ogcapi-connected-systems` commit `3fd86c73e744b7e2faaf7f1c17366bfb9ff4cd6f`. Requirement class file exists at `api/part1/standard/requirements/encoding/sensorml/requirements_class_sensorml.adoc`. The class identifier is `/req/sensorml`, inherits `/req/api-common` and SensorML 3.0 JSON requirement classes (`json-simple-process`, `json-physical-system`, `json-deployment`, `json-derived-property`), and lists 15 subrequirements: `mediatype-read`, `mediatype-write`, `relation-types`, `resource-id`, `feature-attribute-mapping`, `system-schema`, `system-sml-class`, `system-mappings`, `deployment-schema`, `deployment-mappings`, `procedure-schema`, `procedure-sml-class`, `procedure-mappings`, `property-schema`, and `property-mappings`.
- **Sprint 10 coverage scope**: SensorML systems read-only subset with 6 @Tests: (1) IUT declares `/conf/sensorml`; (2) a System resource exposes or can be requested as a SensorML JSON representation; (3) the SensorML representation returns HTTP 200 with parseable JSON; (4) the representation has minimal SensorML identity/class shape such as `type` plus identifier/member structure sufficient for a non-schema sanity check; (5) the representation links or maps back to the canonical CS API System id/UID when present; (6) TestNG dependency wiring and smoke no-regression. The Generator MAY use the existing single-system `alternate` link with `type="application/sml+json"` and `?f=sml3` when content negotiation on `Accept: application/sml+json` returns default CS API JSON. Current GeoRobotix verification at planning time: `/conformance` declares `/conf/sensorml`; collection-level `GET /systems` with `Accept: application/sml+json` returns `Content-Type: application/json` with top-level `items`; single-system JSON exposes `alternate` links of type `application/sml+json` to `?f=sml3`.
- **Sprint 16 implemented coverage scope**: SensorML deployment/procedure/property read-only subset. GeoRobotix runtime on 2026-05-06: `/conformance` declares `/conf/sensorml`, `/conf/deployment`, `/conf/procedure`, and `/conf/property`; deployment and procedure SensorML checks PASS through explicit item-level `application/sml+json` alternate links; property SensorML SKIPs honestly because `/properties` currently has an empty `items` array. Each resource check first gates on the matching resource conformance class (`/conf/deployment`, `/conf/procedure`, `/conf/property`) before fetching or judging resource-specific SensorML evidence. Procedure mapping requires non-identity process/procedure structure (`definition`, `inputs`, `outputs`, `parameters`, `characteristics`, or `capabilities`); `identifiers` alone is not enough. Sprint 16 does not claim samplingFeature SensorML coverage because upstream `/req/sensorml` lists property schema/mapping subrequirements, not sampling feature subrequirements.
- **Dependency wiring**: SensorML depends on SystemFeatures via `<group name="sensorml" depends-on="systemfeatures"/>`. SensorML system encoding assertions are meaningful only after the canonical SystemFeatures resource layer is available.
- **Open subrequirements after Sprint 19 Generator**: broader positive relation-types evidence where SensorML resources expose links-member associations, full SensorML 3.0 JSON Schema validation, non-system mutation-side encoding behavior, and positive property SensorML evidence against a populated IUT remain OPEN unless separately planned.
- **Sprint 17 implemented relation-types scope**: For associations encoded in a SensorML JSON `links` member, relation-types checks require `rel` to equal the association name valid for the selected resource type. If the selected SensorML representation has no association links in a `links` member, the assertion SKIPs with reason. Generic representation links and property-level mapping links are not relation-types PASS evidence. `EncodingRelationTypes` uses resource-specific links-member allowlists only where the OGC SensorML association table maps that association to `links`: System (`subsystems`, `samplingFeatures`, `deployments`, `procedures`, `datastreams`, `controlstreams`), Deployment (`parentDeployment`, `subdeployments`, `featuresOfInterest`, `samplingFeatures`, `datastreams`, `controlstreams`), and Procedure (`implementingSystems`). SensorML `parentSystem` maps to `attachedTo`, not `links`, and Sampling Features are outside the SensorML conformance class. The runtime SensorML assertion currently checks a selected System representation and SKIPs honestly on GeoRobotix because that representation has no top-level links-member association links.
- **Sprint 18 implemented relation-types breadth scope**: `SensorMlTests` now has 12 read-only @Tests and evaluates relation-types independently for selected System, Deployment, and Procedure SensorML representations. Each assertion evaluates its selected SensorML representation independently and SKIPs when the representation has no links-member association links. GeoRobotix runtime on 2026-05-07 SKIPped all three SensorML relation-types checks because the fetched SensorML system, deployment, and procedure bodies expose no top-level `links` member.
- **Sprint 19 implemented mediatype-write scope**: `SensorMlTests` now has 13 @Tests and adds `sensorMlMediaTypeWriteParsesSystemBodyWhenMutationEnabled`. The test checks write-side `Content-Type: application/sml+json` parsing behind the existing Sprint 12 mutation safety gate, requires `/conf/create-replace-delete`, hard-denies public GeoRobotix, and requires follow-up dereference evidence preserving the submitted UID. GeoRobotix declares `/conf/create-replace-delete` and `/conf/sensorml`, but it is a shared public IUT and was not mutated by default smoke. Local OSH mutable-IUT smoke r3 proved the positive system-resource path. OPTIONS readiness alone is not conformance evidence.
- **IUT-state policy**: If the IUT does not declare `/conf/sensorml`, every SensorML @Test SKIPs with reason. If the IUT declares SensorML but only exposes a SensorML representation through an `alternate` link rather than direct `Accept: application/sml+json` negotiation, the sprint may PASS discovery/fetch checks through the alternate link and MUST record that fallback explicitly. A CS API `items` wrapper alone MUST NOT be counted as SensorML PASS.
- **Maps to**: PRD FR-ETS-23.

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
**WHEN** `scripts/smoke-test.sh` runs against the default GeoRobotix target
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
**WHEN** `scripts/smoke-test.sh` runs against the default GeoRobotix target
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
**WHEN** `scripts/smoke-test.sh` runs against the default GeoRobotix target
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
**GIVEN** the Docker image `ets-ogcapi-connectedsystems10` is built from the Sprint 1 Dockerfile
**WHEN** the container is launched via `docker run -p 8081:8080 ets-ogcapi-connectedsystems10`
**THEN** within 30 seconds `GET http://localhost:8081/teamengine/` returns HTTP 200
**AND** the suite list at `GET http://localhost:8081/teamengine/rest/suites` includes `ogcapi-connectedsystems10`
**AND** the TeamEngine logs show zero `ERROR`-level entries during suite registration.
*Maps to*: REQ-ETS-TEAMENGINE-001, TEAMENGINE-003, NFR-ETS-04.

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
**GIVEN** the same commit checked out twice in CI
**WHEN** `mvn clean install` runs in each checkout
**THEN** the resulting jars are byte-identical excluding `META-INF/` timestamps.
*Maps to*: REQ-ETS-SCAFFOLD-005, NFR-ETS-01.

#### SCENARIO-ETS-CORE-RESOURCE-SHAPE-001 (NORMAL)
**GIVEN** any resource fetched from a landing-page link on the IUT
**WHEN** the Core suite asserts the base resource shape
**THEN** the response body has `id` (string), `type` (string), and `links` (array of objects with `href`, `rel`).
*Maps to*: REQ-ETS-CORE-004.

#### SCENARIO-ETS-PART1-DEPENDENCY-SKIP-001 (NORMAL)
**GIVEN** the Core suite produces at least one FAIL verdict for a target IUT
**WHEN** the System Features suite (`/conf/system-features`) attempts to run
**THEN** all `@Test` methods in System Features emit SKIP with reason `dependency /conf/core not satisfied`.
*Maps to*: REQ-ETS-PART1-001..013, PRD FR-ETS-24.

#### SCENARIO-ETS-FIXTURES-PORT-COVERAGE-001 (NORMAL)
**GIVEN** the spec-trap fixture corpus is ported into Java `@DataProvider` methods
**WHEN** `scripts/audit-fixture-port.sh` runs in CI
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

#### SCENARIO-ETS-PART1-002-SYSTEMFEATURES-LANDING-001 (CRITICAL — Sprint 2)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**AND** Core suite has PASSED (no dependency-skip triggered)
**WHEN** the SystemFeatures suite executes `GET /systems`
**THEN** the response is HTTP 200
**AND** the body is parseable JSON containing an `items` array (the CS API uses the `items` wrapper key per OGC API – Features clause 7.15.2-7.15.8 inherited via `/req/system/resources-endpoint`)
**AND** the `items` array is non-empty (S-ETS-02-06 curl-verification confirmed 36 items).
*Maps to*: REQ-ETS-PART1-002 (`/req/system/resources-endpoint`).

#### SCENARIO-ETS-PART1-002-SYSTEMFEATURES-DEPENDENCY-SKIP-001 (CRITICAL — Sprint 2)
**GIVEN** the Core suite produces at least one FAIL verdict for a target IUT
**WHEN** the SystemFeatures suite (`/conf/system`) attempts to run
**THEN** all `@Test` methods in SystemFeatures emit SKIP with reason referencing the unsatisfied `core` group dependency (TestNG group-dependency wiring `<dependencies><group name="systemfeatures" depends-on="core"/>` in `testng.xml`)
**AND** no assertion in SystemFeatures is reported as FAIL or ERROR.
*Maps to*: REQ-ETS-PART1-002. Closes SCENARIO-ETS-PART1-DEPENDENCY-SKIP-001 against SystemFeatures specifically. Live verification deferred to Quinn/Raze gate (would require modifying GeoRobotix or pointing IUT at a 500-server); static verification at S-ETS-02-06 confirmed via TestNG XML output `depends-on-groups="core"` attribute on each of the 4 SystemFeatures @Tests.

#### SCENARIO-ETS-PART1-002-SYSTEMFEATURES-RESOURCE-SHAPE-001 (NORMAL — Sprint 2)
**GIVEN** the first item in the `/systems` collection has been dereferenced via `GET /systems/{id}`
**WHEN** the SystemFeatures suite asserts the canonical-endpoint single-item shape
**THEN** the item has `id` (string), `type` (string), and `links` (array of objects with `href`, `rel`).
*Note*: Operates on the **single-item endpoint** `/systems/{id}` per `/req/system/canonical-endpoint`, NOT the collection level. S-ETS-02-06 curl-verification proved that GeoRobotix `/systems` collection items are minimal GeoJSON Feature stubs without `links`; only the single-item canonical endpoint carries the load-bearing `links` array. v1.0 registry `system-features.ts:225-297` `testCanonicalEndpoint` uses the same single-item-endpoint pattern.
*Maps to*: REQ-ETS-PART1-002 (`/req/system/canonical-endpoint`), REQ-ETS-CORE-004.

#### SCENARIO-ETS-PART1-002-SYSTEMFEATURES-LINKS-NORMATIVE-001 (NORMAL — Sprint 2)
**GIVEN** the single-item `/systems/{id}` response on the IUT
**WHEN** the SystemFeatures suite runs the links-discipline assertion
**THEN** the `links` array contains an entry with `rel=canonical` (the load-bearing assertion per OGC 23-001 `/req/system/canonical-url` — the canonical URL discipline)
**AND** absence of `rel=self` is NOT a FAIL (consistent with the v1.0 GH#3 fix policy applied at the Core landing page; v1.0 audit at `csapi_compliance/src/engine/registry/system-features.ts:36-44` + `:273-286` documents that OGC 23-001 `/req/system/canonical-url` mandates `rel="canonical"` only on **non-canonical** URLs and does NOT require `rel="self"` on `/systems/{id}`).
*Note*: Adapted from design.md text (collection-level `rel=collection`/`rel=items`) per S-ETS-02-06 curl-verification: GeoRobotix `/systems` has only `items` (no collection-level `links`); the load-bearing link discipline lives on `/systems/{id}`.
*Maps to*: REQ-ETS-PART1-002 (`/req/system/canonical-url`), REQ-ETS-CORE-002 (link-discipline policy carryover).

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
**GIVEN** the Sprint 2 close HEAD on the new repo
**WHEN** a developer inspects the GitHub Actions tab
**THEN** at least one `workflow_run` exists for `.github/workflows/build.yml` triggered by a Sprint 2 push commit
**AND** the workflow_run status is SUCCESS
**OR** the absence is documented in ops/status.md as a deferred-with-rationale carryover (gh OAuth scope still missing).
*Maps to*: REQ-ETS-SCAFFOLD-005, NFR-ETS-02.

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
**AND** all 4 SystemFeatures @Tests report `status="SKIP"` (NOT FAIL, NOT ERROR)
**AND** the SKIP reason references the unsatisfied `core` group dependency.
*Maps to*: REQ-ETS-CLEANUP-005, REQ-ETS-PART1-002. Closes Quinn s06 CONCERN-1 + Raze s06 CONCERN-1 (both flagged the gap that Sprint 2's static-only dependency-skip verification did not exercise the live cascade).

#### SCENARIO-ETS-CLEANUP-CREDENTIAL-LEAK-INTEGRATION-001 (CRITICAL — Sprint 3)
**GIVEN** the suite at the Sprint 3 close HEAD with `auth-credential` wired as a TestNG suite parameter
**AND** `MaskingRequestLoggingFilter` (or equivalent wrap pattern per Architect) is registered alongside CredentialMaskingFilter
**WHEN** `bash scripts/smoke-test.sh --auth-credential "Bearer ABCDEFGH12345678WXYZ"` runs end-to-end against GeoRobotix
**THEN** the script exits 0
**AND** `grep -r 'EFGH12345678WXYZ' ets-ogcapi-connectedsystems10/ops/test-results/` returns ZERO hits (no leak in TestNG XML attachments)
**AND** `grep -r 'EFGH12345678WXYZ' <container-log-location>` returns ZERO hits (no leak in container logs)
**AND** `grep -r 'Bear\*\*\*WXYZ\|Bear.*\*\*\*WXYZ' ets-ogcapi-connectedsystems10/ops/test-results/` returns at least one hit (proving filter ran rather than dropping the field).
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

#### SCENARIO-ETS-PART1-002-SYSTEMFEATURES-COLLECTIONS-001 (CRITICAL — Sprint 3)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**AND** Core suite has PASSED (no dependency-skip triggered)
**WHEN** the SystemFeatures expansion @Test `systemAppearsInCollections` runs
**THEN** EITHER `GET /collections` returns 200 + JSON with a `collections` array containing an entry for `systems` (id, title, or canonical IUT path matches)
**OR** the IUT's landing page contains a link with `rel="collection"` (or equivalent) referencing `/systems` (fallback discovery)
**OR** SKIP-with-reason if neither path is available (the IUT may surface `/systems` differently than OGC 23-001 §`/req/system/collections` standardizes).
*Maps to*: REQ-ETS-PART1-002 (modified per Sprint 3 expansion).

#### SCENARIO-ETS-PART1-002-SYSTEMFEATURES-LOCATION-TIME-001 (NORMAL — Sprint 3)
**GIVEN** the first item in the `/systems` collection on the IUT
**WHEN** the SystemFeatures expansion @Test `systemHasGeometryAndValidTime` runs
**THEN** the item has `geometry` field (GeoJSON Geometry or null) AND/OR `properties.validTime` (string/array per OGC 23-001 §`/req/system/location-time`)
**OR** if neither is present: SKIP-with-reason (MAY priority per v1.0 audit at `csapi_compliance/src/engine/registry/system-features.ts`; absence is NOT FAIL).
*Maps to*: REQ-ETS-PART1-002 (modified per Sprint 3 expansion).

#### SCENARIO-ETS-CLEANUP-CI-WORKFLOW-ESCALATION-001 (CRITICAL — Sprint 4)
**GIVEN** the gh OAuth token has been a 4-consecutive-sprint user-action blocker (S-ETS-01-01..03 + S-ETS-02-05 + S-ETS-03-03)
**WHEN** Sprint 4 closes
**THEN** EITHER GitHub Actions UI shows at least one workflow_run on a Sprint 4 commit with `conclusion=success` (Path A — user granted scope, Generator landed `git mv`)
**OR** `ops/status.md` documents `ci_workflow_live` as DROPPED from sprint cadence with explicit "perpetual environmental blocker" rationale + alternative path note (Path B — formal drop per Raze recommendation).
*Maps to*: REQ-ETS-CLEANUP-007 (modified), REQ-ETS-CLEANUP-009. Closes Raze cumulative CONCERN-3 + 4-sprint-defer pattern (binary outcome — no more 4-sprint-style retries).

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

#### SCENARIO-ETS-PART1-003-SUBSYSTEMS-RESOURCES-001 (CRITICAL — Sprint 4)
**GIVEN** the IUT is `https://api.georobotix.io/ogc/t18/api`
**AND** Core suite has PASSED + SystemFeatures suite has PASSED (no two-level cascade-skip triggered)
**WHEN** the Subsystems suite executes `subsystemsResourcesEndpointReturnsCollection` @Test
**THEN** EITHER `GET /systems/{id}/subsystems` returns 200 + JSON with a non-empty `items` array
**OR** SKIP-with-reason if `/systems/{id}/subsystems` returns 404 (IUT does not implement Subsystems)
**AND** the @Test description references the canonical OGC `.adoc` URI for `/req/subsystem/resources-endpoint`.
*Maps to*: REQ-ETS-PART1-003.

#### SCENARIO-ETS-PART1-003-SUBSYSTEMS-CANONICAL-001 (NORMAL — Sprint 4)
**GIVEN** at least one subsystem id discovered from `/systems/{id}/subsystems`
**WHEN** the Subsystems suite executes `subsystemCanonicalEndpointReturnsBaseShape` @Test
**THEN** `GET /subsystems/{id}` returns 200 + JSON with `id` (string), `type` (string), `links` (array per REQ-ETS-CORE-004 base shape)
**AND** the @Test description references the canonical OGC `.adoc` URI for `/req/subsystem/canonical-endpoint`.
*Maps to*: REQ-ETS-PART1-003.

#### SCENARIO-ETS-PART1-003-SUBSYSTEMS-PARENT-LINK-001 (NORMAL — Sprint 4)
**GIVEN** at least one subsystem item from `/subsystems/{id}` or `/systems/{id}/subsystems`
**WHEN** the Subsystems suite executes `subsystemHasParentSystemLink` @Test
**THEN** the subsystem item's `links` array contains an entry with `rel="system"` (or equivalent per OGC `.adoc`) referencing the parent system URI
**AND** the @Test description references the canonical OGC `.adoc` URI for `/req/subsystem/parent-system-link`.
*Maps to*: REQ-ETS-PART1-003.

#### SCENARIO-ETS-PART1-003-SUBSYSTEMS-CANONICAL-URL-001 (NORMAL — Sprint 4)
**GIVEN** at least one subsystem item from `/subsystems/{id}` or `/systems/{id}/subsystems`
**WHEN** the Subsystems suite executes `subsystemHasCanonicalLink` @Test
**THEN** the subsystem item's `links` array contains an entry with `rel="canonical"` per `/req/subsystem/canonical-url`
**AND** absence of `rel="self"` is NOT FAIL (preserves v1.0 GH#3 fix policy from Core landing page).
*Maps to*: REQ-ETS-PART1-003.

#### SCENARIO-ETS-PART1-003-SUBSYSTEMS-DEPENDENCY-SKIP-001 (CRITICAL — Sprint 4)
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
**THEN** the produced TestNG XML shows SystemFeatures @Tests FAIL (1) + SKIP (5) + Subsystems SKIP (4) + Procedures SKIP (P) + Deployments SKIP (D); Core + Common PASS
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
**AND** prong (a): ZERO unmasked literal hits in TestNG XML + container log + smoke log (container log is now captured BEFORE teardown — not vacuously empty)
**AND** prong (b): AT LEAST ONE masked-form `Bear***WXYZ` hit in container log (filter emits masked form during smoke)
**AND** prong (c): AT LEAST ONE unmasked `Bearer ABCDEFGH12345678WXYZ` hit in stub-IUT log (wire carries original credential).
*Maps to*: REQ-ETS-CLEANUP-016, REQ-ETS-CLEANUP-011 (finally IMPLEMENTED after Sprint 4 + Sprint 5 carryover).

#### SCENARIO-ETS-CLEANUP-SABOTAGE-TARGET-DOCKER-FIX-001 (CRITICAL — Sprint 6)
**GIVEN** `scripts/sabotage-test.sh` rsync line has been fixed to include `.git/` in the temp worktree (S-ETS-06-02)
**WHEN** `bash scripts/sabotage-test.sh --target=systemfeatures` runs from `/tmp/<role>-fresh-sprint6/`
**THEN** the Docker build step succeeds (no `COPY .git ./.git: not found` error)
**AND** the smoke run executes against the sabotaged temp tree
**AND** the cascade XML shows Core+Common PASS, SystemFeatures 1×FAIL+Nx SKIP, Subsystems+Procedures+Deployments all SKIP
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
**WHEN** the Docker build step fails (if it were to fail, e.g. in CI with a broken environment)
**THEN** the log message reads `"Docker build FAILED"` or equivalent (NOT `"smoke exited non-zero (EXPECTED — SystemFeatures FAIL on first @Test)"`)
**AND** when the Docker build succeeds but smoke exits non-zero due to the sabotage marker @Test FAIL, the log message reads `"smoke exited non-zero (EXPECTED — SystemFeatures FAIL on first @Test)"`.
*Maps to*: REQ-ETS-CLEANUP-015 (improved UX).

#### SCENARIO-ETS-CLEANUP-SABOTAGE-JAVAC-FIX-001 (CRITICAL — Sprint 7)
**GIVEN** `scripts/sabotage-test.sh --target=systemfeatures` is run from a /tmp clone at Sprint 7 HEAD
**WHEN** the python injector injects `if (true) throw new AssertionError("SABOTAGED ...")` as the first statement of `systemsCollectionReturns200()`
**THEN** Docker build step 8/8 (`mvn clean package`) succeeds without `unreachable statement` compile error
**AND** the smoke run produces a TestNG XML cascade report
**AND** the cascade report shows Core+Common all PASS, SystemFeatures 1×FAIL + Nx SKIP, Subsystems+Procedures+Deployments all SKIP.
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
**WHEN** `scripts/sync-uri-coverage.sh` runs in CI
**THEN** the script exits 0 if every URI is mirrored on both sides OR has an entry in `ops/uri-coverage-allowlist.txt`
**AND** the script exits non-zero if any URI is unmirrored without an allowlist entry.
*Maps to*: REQ-ETS-SYNC-001.

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

**Sub-deliverable 5 — TeamEngine Integration** (REQ-ETS-TEAMENGINE-001..005, Implemented S-ETS-01-03):
- REQ-ETS-TEAMENGINE-001: META-INF/services SPI registration file (58 bytes, single-line FQCN `org.opengis.cite.ogcapiconnectedsystems10.TestNGController`, no whitespace, no extension) — verified by Quinn s01 + Raze s01/s02 + S-ETS-01-03 smoke runtime.
- REQ-ETS-TEAMENGINE-002: CTL wrapper at `src/main/scripts/ctl/ogcapi-connectedsystems10-suite.ctl` from archetype. **CTL Saxon namespace verified clean** (architect-handoff S-ETS-01-03 CONCERNS pitfall #3 — silent failure mode): `xmlns:tng="java:org.opengis.cite.ogcapiconnectedsystems10.TestNGController"` is the canonical run-together ADR-003 form, no `cs10` typo. Runtime corroboration: 12/12 PASS via SPI-routed smoke confirms TeamEngine successfully loaded the CTL.
- REQ-ETS-TEAMENGINE-003: `Dockerfile` at repo root produces TE 5.6.1 webapp + ETS jar (commit `d910808`). **🚨 IMPLEMENTATION DEVIATION FROM SPEC TEXT (REQ wording amendment proposed for next planning cycle)**: spec said `FROM ogccite/teamengine-production:5.6.1`. Dana discovered (a) that tag doesn't exist on Docker Hub (only `:latest` and `:1.0-SNAPSHOT`), and (b) the production image runs JDK 8 (`JAVA_VERSION=8u212`), incompatible with our JDK 17 ETS jar (`UnsupportedClassVersionError class file version 61.0`). Implemented resolution: assemble TE 5.6.1 manually on `tomcat:8.5-jre17` by downloading `teamengine-web-5.6.1.war` + `teamengine-web-5.6.1-common-libs.zip` + `teamengine-console-5.6.1-base.zip` from Maven Central + 3 secondary patches (VirtualWebappLoader strip, JAXB jars in shared `lib/`, full `mvn dependency:copy-dependencies` deps closure with `teamengine-*-6.0.0.jar` filtered out). Identical TE 5.6.1 behavior + JDK 17 runtime; identical assertion outcomes (12/12 PASS) on the same IUT against GeoRobotix. Audit trail at new repo `ops/server.md` "Docker smoke test" section. **Proposed amended REQ wording**: "...SHALL produce a TeamEngine 5.6.1 webapp on a JDK 17 base image" (preserves Sprint 1 semantics; acknowledges JDK 17 toolchain reality + the missing `:5.6.1` tag fact).
- REQ-ETS-TEAMENGINE-004: `docker-compose.yml` at repo root with `8081:8080` port mapping + 60s start-period healthcheck against `http://localhost:8080/teamengine` (commit `d831da1`). Canonical port 8081 committed; for dev environments where 8081 is in use (e.g. WSL2 running other containers), use `docker run -p 8082:8080` for testing. Dev-environment caveat documented at new repo `ops/server.md`.
- REQ-ETS-TEAMENGINE-005: `scripts/smoke-test.sh` end-to-end (commit `91308f7`). Bash, idempotent, exits 0 only on non-empty TestNG report + zero ERROR-level container logs during suite registration. End-to-end ~10s wall-clock (image cached); first run with TE image pull adds 5-10 min. Archived artifacts at `ops/test-results/s-ets-01-03-teamengine-{smoke,container}-2026-04-28.{xml,log}`.

**Sprint 1 contract success_criteria walk after S-ETS-01-03**: **9/9 PASS** (per Dana's S-ETS-01-03 generator report) — all 5 critical scenarios PASS (SCAFFOLD-BUILD-001, CORE-LANDING-001, CORE-CONFORMANCE-001, TEAMENGINE-LOAD-001, CORE-SMOKE-001), all 5 normal scenarios PASS (SCAFFOLD-LAYOUT-001, SCAFFOLD-REPRODUCIBLE-001, CORE-RESOURCE-SHAPE-001, CORE-LINKS-NORMATIVE-001, CORE-API-DEF-FALLBACK-001). **Sprint 1 functionally complete pending Quinn+Raze gate close on S-ETS-01-03.**

**Sub-deliverable 3 (cont.) — Common conformance class** (REQ-ETS-PART1-001, Implemented S-ETS-03-07, pending Quinn+Raze):
- REQ-ETS-PART1-001: `conformance.common.CommonTests` 4 @Test methods (Sprint-1-style minimal-then-expand per architect-handoff item 17 — distinct surface from Core to avoid duplication) all PASS against GeoRobotix at HEAD commit `c56df10` (new repo). Smoke total = 22/22 (12 Core + 6 SystemFeatures + 4 Common). 2 commits at new repo: `f384509` (CommonTests + testng.xml single-block consolidation extension), `c56df10` (live smoke evidence + nested-properties fix in S-ETS-03-05). Common is INDEPENDENT of Core (no dependsOnGroups declaration on the common group); runs in parallel. URI canonical form: `/req/json/{definition,content}` (Common Part 1 JSON encoding class), `/req/landing-page/conformance-success` (reused at Common-class layer to assert `ogcapi-common-1/1.0/conf/core` IS declared in `/conformance` body), `/req/collections/collections-list-success` (Common Part 2). All 4 .adoc URLs HTTP-200-verified at `raw.githubusercontent.com/opengeospatial/ogcapi-common/master/{19-072,collections}/requirements/`. ETSAssert helpers throughout; zero new bare-throw sites. GeoRobotix curl evidence: `/conformance` declares `ogcapi-common-1/1.0/conf/core` AND `ogcapi-common-2/0.0/conf/collections`; `/collections` returns 200 with `id="all_systems"` entry; `?f=json` returns JSON; `?f=html` returns 400 (acceptable per content-negotiation discipline — IUT explicitly handles parameter). Full curl evidence + URI mapping archived in `epics/stories/s-ets-03-07-common-conformance-class.md` Implementation Notes.

**Sub-deliverable 3 (cont.) — SystemFeatures conformance class** (REQ-ETS-PART1-002, Implemented S-ETS-02-06 + extended S-ETS-03-05, pending Quinn+Raze):
- REQ-ETS-PART1-002: `conformance.systemfeatures.SystemFeaturesTests` Sprint 2 4 @Test methods + Sprint 3 2 new @Test methods = 6 @Tests total (5/5 v1.0 SystemFeatures URI coverage achieved, was 3/5 at Sprint 2 close) all PASS against GeoRobotix at HEAD commit `c56df10` (new repo). Smoke total = 22/22 (12 Core + 6 SystemFeatures + 4 Common). Sprint 3 expansion commits: `bfa0e6b` (2 new @Tests for /req/system/collections + /req/system/location-time), `c56df10` (nested-properties fix for GeoJSON Feature shape — items have validTime under `properties` not top-level). New URI canonical forms: `/req/system/collections` (defense-in-depth: PASS via /collections OR landing-page rel=systems link — GeoRobotix has both), `/req/system/location-time` (MAY-priority: SKIP rather than FAIL if both validTime and geometry absent; PASS against GeoRobotix because items carry `properties.validTime`). Sprint 2 close artifact (16/16): 4 commits at new repo: `9847544` (SystemFeaturesTests + Core `groups = "core"` annotations), `d99665d`+`02796dd` (testng.xml dependency wiring; consolidated to single `<test>` block after empirical TestNG group-scope discovery), `3bd7fc6` (smoke artifact archive `ops/test-results/sprint-ets-02-systemfeatures-georobotix-smoke-2026-04-28.xml`). Reproducible build verified (sha256 `b51577cfb48535c6322cfc117514bd501e4d180b6c1435f8628b56d31a7a000a` byte-identical across two consecutive `mvn clean install -DskipTests`). All 4 SCENARIO-ETS-PART1-002-* satisfied: LANDING-001 + RESOURCE-SHAPE-001 + LINKS-NORMATIVE-001 PASS at runtime; DEPENDENCY-SKIP-001 PASS via TestNG XML output `depends-on-groups="core"` recorded on each of the 4 SystemFeatures @Tests (live break-Core verification deferred to Quinn/Raze gate). v1.0 GH#3 fix preserved at SystemFeatures level. URI form `/req/system/<X>` per OGC `.adoc` canonical (5 sub-requirement `.adoc` URLs HTTP-200-verified at `raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/system/`). Adapted from design.md table — collection-level GeoRobotix `/systems` has only `items` (no `links`); per-item entries are minimal stubs without `links`; the load-bearing `links` array lives on `/systems/{id}` (single-item canonical endpoint); `systemItemHasIdTypeLinks` and `systemsCollectionLinksDiscipline` operate on the single-item endpoint per `/req/system/canonical-endpoint` + `/req/system/canonical-url`. Full curl evidence + URI-form pivot rationale archived in `epics/stories/s-ets-02-06-systemfeatures-conformance-class.md` Implementation Notes.

**Sub-deliverable 8 — Web-App Freeze**: REQ-ETS-WEBAPP-FREEZE-001 ✅ closed (commit `44c279e`, tag `v1.0-frozen` at `ab53658`). README.adoc reverse cross-link in new repo closes ADR-005 "both directions" requirement.

### Deviations from Spec
- **Java root package, artifactId, ets-code, CTL filename, ets-common version, TeamEngine version**: spec text was reconciled to ADR-003/ADR-004/ADR-001 authority on 2026-04-28T14:42Z (commit `19003b1`). Spec now matches what Generator implemented.
- **Layout refactor closed in S-ETS-01-02**: archetype-flat layout retained through S-ETS-01-01; refactored to `conformance.core.*` + `listener.*` subpackages in S-ETS-01-02 commit `2dc4414`. Closes Quinn+Raze CONCERN-3 from S-ETS-01-01 gate close.
- **Kaizen openapi-parser declared but not consumed in Sprint 1**: per architect-handoff `surfaced_risks_pat_missed.OPENAPI-PARSER-NOT-USED-IN-SPRINT-1`, Sprint 1 Core uses everit-json-schema (transitive via ets-common:17) directly. Kaizen is on the dep list for Sprint 2+ when richer Part 1 classes need OpenAPI-driven validation.
- **GitHub Actions workflow staged at `ci/github-workflows-build.yml` not `.github/workflows/build.yml`**: gh OAuth token at commit time lacked `workflow` scope. One-line fix: `gh auth refresh -s workflow` then `git mv`. Tracked as Raze CONCERN-2 (S-ETS-01-01 + S-ETS-01-02).
- **Bare `throw new AssertionError(...)` instead of `EtsAssert` helper** (architect-handoff `must` constraint #9): 21 call sites across the 3 Core test classes use bare `throw new AssertionError(URI + " — message")` rather than an `EtsAssert.failWithUri(...)` helper. **Intent met** (every FAIL message includes the canonical `/req/*` URI as required); **form violated** (no helper used). The existing `ETSAssert.java` is XML/Schematron-only and Dana didn't extend it. Tracked as Quinn GAP-1 / Raze GAP-1 (both s02). **Sprint 2 cleanup**: extend `ETSAssert` with a `failWithUri(String message, String uri)` overload and refactor the 21 call sites mechanically.
- **URI form drift between v1.0 TS, Java port, and OGC canonical** (Quinn GAP-2 / Raze GAP-2 in s02 reports): Java cites `/req/core/root-success`; v1.0 TS uses `/req/ogcapi-common/landing-page`; OGC's normative .adoc canonical (verified by Raze upstream-fetch 2026-04-17) is `/req/landing-page/root-success`. Three different forms all citing the same correct normative text, but a CITE SC reviewer dereferencing the @Test description URIs against the OGC normative document will get a 404. **Source is upstream of S-ETS-01-02** (spec.md text already used the `/req/core/<X>-success` form when Dana implemented). **Sprint 2 cleanup**: amend spec.md + traceability.md + Java @Test descriptions to the OGC canonical `.adoc` URI form; ~30-40 sites across both repos.

### Deferred
- REQ-ETS-TEAMENGINE-002..005 (Dockerfile, docker-compose, smoke-test.sh, container-load verification) → S-ETS-01-03 (final Sprint 1 story).
- REQ-ETS-PART1-001..013 (per-class detail beyond Core) — drafted as placeholders; per-assertion FRs and SCENARIOs to be expanded in sprints 2..N.
- REQ-ETS-PART2-002 (Datastreams & Observations) — partially implemented in Sprint 21 read-only subset.
- REQ-ETS-PART2-003 (Control Streams & Commands) — partially implemented in Sprint 22 read-only subset.
- REQ-ETS-PART2-004 (Command Feasibility) — partially implemented in Sprint 23 safety-gated subset.
- REQ-ETS-PART2-005: partially implemented by Sprint 24 System Events Generator.
- REQ-ETS-PART2-006: partially implemented by Sprint 25 Advanced Filtering Generator.
- REQ-ETS-PART2-007 (Part 2 Create/Replace/Delete) - partially implemented by Sprint 26 Generator; seeded local OSH E2E is accepted after fixture repair, while GeoRobotix public smoke remains advisory and currently fails with public-IUT HTTP 500 responses outside the new Part 2 CRD tests.
- REQ-ETS-PART2-008 (Part 2 Update) - partially implemented by Sprint 27 Generator; positive PATCH lifecycle and concrete schema-rejection dispatch remain deferred.
- REQ-ETS-PART2-009 (Part 2 JSON Encoding) - partially implemented by Sprint 28 Generator; full positive schema closure remains dependent on a healthy declaring IUT with valid DataStream, Observation, ControlStream, Command, CommandStatus, CommandResult, SystemEvent, SWE Common record-component, and mediatype-write evidence.
- REQ-ETS-PART2-010 (Part 2 SWE Common JSON Encoding) - partially implemented by Sprint 29 Generator; full positive closure remains dependent on a healthy declaring IUT with SWE 3.0 JSON Encoding Rules visibility, valid DataStream/Observation SWE JSON reads, valid ControlStream/Command SWE Common JSON schema evidence, candidate Observation/Command resources, and non-mutating mediatype-write evidence. Mandatory GeoRobotix Generator smoke failed (`186 total / 31 passed / 22 failed / 133 skipped`) with zero matched public-IUT write requests.
- REQ-ETS-PART2-011 (Part 2 SWE Common Text Encoding) - specified by Sprint 30 planning; Generator pending. Planning verified OGC 23-002 Clause 16.3 identifiers, the SWE Common 3.0 Text Encoding Rules prerequisite, exact `application/swe+text` media type, Requirements 115-122, resource condition gates, `TextEncoding` schema evidence requirements, non-mutating mediatype-write evidence, and false-PASS guards for CSV/binary/JSON fallback evidence. Mandatory GeoRobotix planning smoke failed (`186 total / 31 passed / 22 failed / 133 skipped`) with zero matched public-IUT write requests.
- REQ-ETS-PART2-012..013 (remaining Part 2 classes/cross-class closures) - deferred after Sprint 30 SWE Common Text planning.
- REQ-ETS-FIXTURES-001..003 (spec-trap port from `csapi_compliance/tests/fixtures/spec-traps/`) → epic-ets-06 parallel sprint after Sprint 1 closes.
- REQ-ETS-CITE-001..003 — calendar-bound, not sprint-bound. Beta milestone gates these.
- REQ-ETS-SYNC-001 — CI script work, expected after Part 1 is feature-complete enough to make the diff meaningful.
- HTTP request/response capture (full REST Assured logging-filter pattern) → Sprint 2.
- Auth credential masking + `logback.xml` (architect-handoff `should` #3 — never log Authorization/X-API-Key) → Sprint 2 (no auth path exercised in Sprint 1; GeoRobotix is open).
- JaCoCo ≥80% coverage instrumentation → Sprint 2.

### Gate verdicts (audit trail)
- **Gate 3.5 (Quinn / Evaluator) for S-ETS-01-01**: APPROVE_WITH_GAPS confidence 0.88. Report at `.harness/evaluations/sprint-ets-01-evaluator.yaml`. 3 gaps + 4 concerns — all gaps closed same-turn 2026-04-28T16:30Z.
- **Gate 4 (Raze / Adversarial) for S-ETS-01-01**: GAPS_FOUND confidence 0.84. Report at `.harness/evaluations/sprint-ets-01-adversarial.yaml`. 3 gaps + 3 concerns — same 3 gaps Quinn caught (cross-corroborating). All closed same-turn.
- **Gate 3.5 (Quinn / Evaluator) for S-ETS-01-02**: APPROVE_WITH_GAPS confidence 0.85. Report at `.harness/evaluations/sprint-ets-01-evaluator-s02.yaml`. 3 gaps + 4 concerns. GAP-3 (spec.md reconcile pending) closed by this commit; GAP-1 (EtsAssert) + GAP-2 (URI form drift) deferred to Sprint 2 cleanup with explicit notes above.
- **Gate 4 (Raze / Adversarial) for S-ETS-01-02**: GAPS_FOUND confidence 0.82. Report at `.harness/evaluations/sprint-ets-01-adversarial-s02.yaml`. 3 gaps + 3 concerns — same 3 gaps Quinn caught (cross-corroborating, 2nd consecutive sprint). GAP-3 (Common Part 2 → Part 1 URI typo in `ResourceShapeTests`) closed by new repo commit `1fdfe07`. CONCERN-1 (Dana's reported sha256 `c4a80294...` was at HEAD `b249aa1`; canonical Sprint-1-close hash at `ea2c91f` is `b1ffdc8eee...` per Raze independent verification — buildnumber-maven-plugin embeds commit SHA in manifest, so per-commit hash variance is expected metadata-only) — narrative clarified in ops/status.md and ops/changelog.md this turn. CONCERN-3 (logback.xml + CredentialMaskingFilter) Sprint 2 scope.
