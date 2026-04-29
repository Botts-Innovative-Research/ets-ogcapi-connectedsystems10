package org.opengis.cite.ogcapiconnectedsystems10.conformance.subsystems;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Part 1 — Subsystems conformance class tests ({@code /conf/subsystem}; OGC 23-001
 * Annex A).
 *
 * <p>
 * Implements <strong>REQ-ETS-PART1-003</strong> (Subsystems conformance class). Third
 * additional Part 1 conformance class beyond Core (Sprint 1) + SystemFeatures (Sprint 2)
 * + Common (Sprint 3). Mirrors the {@code conformance.systemfeatures.SystemFeaturesTests}
 * architectural pattern: one TestNG {@code @Test} per OGC ATS assertion, each carrying
 * the canonical OGC requirement URI in its {@code description}, every assertion routed
 * through {@link ETSAssert} helpers per ADR-008 (zero bare
 * {@code throw new AssertionError}).
 * </p>
 *
 * <p>
 * <strong>Coverage scope</strong> (Sprint-1-style minimal per
 * {@code openspec/capabilities/ets-ogcapi-connectedsystems/design.md} §"Sprint 4
 * hardening: Subsystems conformance class scope" + Architect ratification 2026-04-29): 4
 * {@code @Test} methods at Sprint 4 close, mapped to 4 SCENARIO-ETS-PART1-003-*
 * enumerated by Pat. Sprint 5+ expansion adds {@code /req/subsystem/recursive-*}
 * requirements (recursive-param + recursive-search-systems + recursive-search-subsystems)
 * plus the {@code /rec/subsystem/collection-datetime} recommendation.
 * </p>
 *
 * <p>
 * <strong>Two-level dependency chain</strong> (FIRST in this ETS): Subsystems →
 * SystemFeatures → Core. Per ADR-010 v2 amendment (Architect 2026-04-29)
 * defense-in-depth:
 * </p>
 * <ol>
 * <li>{@code testng.xml} {@code <group name="subsystems" depends-on="systemfeatures"/>}
 * declares the structural cascade (SystemFeatures → Core is already wired Sprint 2;
 * adding Subsystems → SystemFeatures yields the two-level chain).</li>
 * <li>{@code @BeforeClass} {@link SkipException} fallback (this class) cascades all four
 * {@code @Test} methods to SKIP if (a) the {@code iut} suite attribute is missing, OR (b)
 * GeoRobotix returns no system with non-empty {@code subsystems} array — the latter
 * preserves Sprint 4 acceptance criterion that 4-@Test cascade-SKIPs gracefully when the
 * IUT does not expose subsystems (per design.md §"Sprint 4 hardening: Subsystems
 * conformance class scope" §What NOT to ship in Sprint 4).</li>
 * </ol>
 *
 * <p>
 * Covers:
 * </p>
 * <ul>
 * <li><strong>SCENARIO-ETS-PART1-003-SUBSYSTEMS-RESOURCES-001</strong> (CRITICAL) — GET
 * {@code /systems/{parentId}/subsystems} returns 200 + JSON + non-empty {@code items}
 * array (curl-verified against GeoRobotix 2026-04-29 — system {@code 0n3rtpmuihc0}
 * returns 12 subsystems; most other systems return empty). Per OGC 23-001
 * {@code /req/subsystem/collection}.</li>
 * <li><strong>SCENARIO-ETS-PART1-003-SUBSYSTEMS-CANONICAL-001</strong> (NORMAL) —
 * canonical-endpoint discipline inherited via OGC 23-001
 * {@code /req/subsystem requirements_class_system_components.adoc} {@code inherit::
 * /req/system}. Subsystems ARE Systems and dereference at the canonical
 * {@code /systems/{id}} endpoint with the standard id+type+links shape.</li>
 * <li><strong>SCENARIO-ETS-PART1-003-SUBSYSTEMS-CANONICAL-URL-001</strong> (NORMAL) —
 * single-subsystem links contain {@code rel="canonical"} per OGC 23-001
 * {@code /req/system/canonical-url} (inherited).</li>
 * <li><strong>SCENARIO-ETS-PART1-003-SUBSYSTEMS-PARENT-LINK-001</strong> (CRITICAL,
 * UNIQUE-to-Subsystems) — single-subsystem links contain {@code rel="parent"} pointing
 * back at the parent system. Curl-verified GeoRobotix 2026-04-29: subsystem
 * {@code 0nar3cl0tk3g} carries {@code {"rel":"parent","title":"Parent
 * system","href":".../systems/0n3rtpmuihc0?f=geojson"}}. The architectural invariant is:
 * a subsystem MUST link back to its parent. While the OGC source repo's
 * {@code /req/subsystem/} folder does NOT define a standalone {@code parent-system-link}
 * requirement (verified 2026-04-29: subsystem reqs are {@code collection},
 * {@code recursive-param}, {@code recursive-search-systems},
 * {@code recursive-search-subsystems}, {@code subcollection-time}), the parent link is
 * implied by the architectural composition rules in OGC 23-001 §System Components and is
 * the load-bearing semantic distinction between a System and a Subsystem in the graph. We
 * assert under the canonical class URI {@code /req/subsystem} (the requirements class
 * itself) since no per-link sub-requirement exists.</li>
 * </ul>
 *
 * <p>
 * <strong>Canonical URI form</strong>: per OGC source
 * {@code raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/subsystem/}
 * (6 .adoc files present in the GitHub directory listing as of 2026-04-29 — corrected
 * Sprint 5 S-ETS-05-04 from prior 5-file enumeration per Raze CONCERN-1):
 * {@code requirements_class_system_components.adoc}, {@code req_subcollection.adoc},
 * {@code req_subcollection_time.adoc}, {@code req_recursive_param.adoc},
 * {@code req_recursive_search_systems.adoc},
 * {@code req_recursive_search_subsystems.adoc}.
 * </p>
 *
 * <p>
 * <strong>NOTE</strong> on {@code req_subcollection_time.adoc}: the file exists in the
 * GitHub directory listing but is <em>not</em> enumerated in
 * {@code requirements_class_system_components.adoc}'s {@code requirement::} list (the
 * .adoc that defines the Subsystems requirements class membership). It represents a
 * separate, optional sub-requirement (subcollection time-extent filter) that is
 * <em>not</em> asserted by this class at Sprint 4 minimal scope. Deferred to Sprint 5+
 * recursive-* expansion. Distinct from {@code req_subcollection.adoc} (which IS in the
 * requirements class and IS asserted via the SUBSYSTEMS-RESOURCES SCENARIO).
 * </p>
 *
 * <p>
 * The IUT also declares {@code .../conf/subsystem} in {@code /conformance} (per OGC
 * 23-001 Annex A conformance class declaration).
 * </p>
 *
 * <p>
 * <strong>Curl-confirmed shape (2026-04-29)</strong>:
 * </p>
 * <ul>
 * <li>{@code GET /systems/0n3rtpmuihc0/subsystems} → 200 + {@code items: [12 GeoJSON
 * Features]}; each item is GeoJSON Feature shape ({@code type}, {@code id},
 * {@code geometry}, {@code properties}); {@code links} array NOT present at collection
 * level (matches SystemFeatures collection shape per S-ETS-02-06 evidence).</li>
 * <li>{@code GET /systems/0n3rtpmuihc0/subsystems/0nar3cl0tk3g} → 200 + GeoJSON Feature
 * with {@code links} array containing 6 entries: {@code canonical}, {@code alternate}
 * (sml3), {@code alternate} (html), <strong>{@code parent}</strong>,
 * {@code samplingFeatures}, {@code datastreams}. The {@code parent} link's {@code href}
 * resolves to {@code .../systems/0n3rtpmuihc0?f=geojson} — i.e., back to the parent
 * system.</li>
 * </ul>
 */
public class SubsystemsTests {

	/**
	 * Canonical OGC requirement URI for the Subsystems collection endpoint.
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/subsystem/req_subcollection.adoc">req_subcollection.adoc</a>
	 */
	static final String REQ_SUBSYSTEM_COLLECTION = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/subsystem/collection";

	/**
	 * Canonical OGC requirement URI for the Subsystems requirements class itself (used
	 * for assertions that have no standalone sub-requirement, e.g. the architectural
	 * parent-link invariant).
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/subsystem/requirements_class_system_components.adoc">requirements_class_system_components.adoc</a>
	 */
	static final String REQ_SUBSYSTEM_CLASS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/subsystem";

	/**
	 * Canonical OGC requirement URI for the System canonical endpoint (inherited by
	 * Subsystems per {@code requirements_class_system_components.adoc}
	 * {@code inherit:: /req/system}).
	 */
	static final String REQ_CANONICAL_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/canonical-endpoint";

	/**
	 * Canonical OGC requirement URI for the System canonical-URL link discipline
	 * (inherited).
	 */
	static final String REQ_CANONICAL_URL = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/canonical-url";

	private URI iutUri;

	/** Cached parent-system id chosen during {@link #fetchSubsystemsCollection}. */
	private String parentSystemId;

	/** Cached subsystems collection URI {@code /systems/{parentId}/subsystems}. */
	private URI subsystemsUri;

	private Response subsystemsResponse;

	private Map<String, Object> subsystemsBody;

	/** Cached first-subsystem id extracted from the collection (or null). */
	private String firstSubsystemId;

	/**
	 * Cached single-subsystem response for
	 * {@code /systems/{parentId}/subsystems/{firstSubsystemId}} (lazy in BeforeClass).
	 */
	private Response subsystemItemResponse;

	private Map<String, Object> subsystemItemBody;

	/**
	 * Reads the {@code iut} suite attribute, walks {@code /systems} to find a system with
	 * a non-empty {@code subsystems} collection, then fetches that collection + the first
	 * subsystem item once. All four {@code @Test} methods operate on cached responses to
	 * avoid redundant traffic against the IUT (mirrors
	 * {@link org.opengis.cite.ogcapiconnectedsystems10.conformance.systemfeatures.SystemFeaturesTests}
	 * pattern per design.md §"Fixtures and listeners").
	 *
	 * <p>
	 * <strong>Two-level dependency-skip cascade fallback</strong> (ADR-010 v2 amendment
	 * defense-in-depth): if the suite-level {@code <group depends-on>} cascade does not
	 * fire (e.g. TestNG 7.9.0 transitive cascade behavior is not what we expect), this
	 * {@code @BeforeClass} {@link SkipException} cascades all four {@code @Test} methods
	 * to SKIP when (a) {@code iut} attribute missing/non-URI, OR (b) no system in the IUT
	 * has subsystems, OR (c) the chosen system's {@code /subsystems} returns non-200.
	 * </p>
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchSubsystemsCollection(ITestContext testContext) {
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		this.iutUri = (URI) iutAttr;
		String iutString = this.iutUri.toString();
		String base = iutString.endsWith("/") ? iutString : iutString + "/";

		// Step 1 — list /systems and pick one with a non-empty /subsystems collection.
		// GeoRobotix curl evidence (2026-04-29): most systems have empty subsystems;
		// system 0n3rtpmuihc0 has 12. Walk the first 15 (matching curl evidence span)
		// to balance coverage with traffic cost. If none have subsystems, SKIP-with-
		// reason cascades all 4 @Tests (per Sprint 4 acceptance criterion #2).
		Response systemsResp = given().accept("application/json").when().get(URI.create(base + "systems")).andReturn();
		if (systemsResp.getStatusCode() != 200) {
			throw new SkipException(REQ_SUBSYSTEM_COLLECTION + " — /systems returned HTTP "
					+ systemsResp.getStatusCode() + "; cannot enumerate parent-system candidates.");
		}
		Map<String, Object> systemsBody;
		try {
			systemsBody = systemsResp.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			throw new SkipException(
					REQ_SUBSYSTEM_COLLECTION + " — /systems body did not parse as JSON: " + ex.getMessage());
		}
		Object itemsObj = (systemsBody == null) ? null : systemsBody.get("items");
		if (!(itemsObj instanceof List)) {
			throw new SkipException(
					REQ_SUBSYSTEM_COLLECTION + " — /systems body has no 'items' array; cannot pick parent system.");
		}
		@SuppressWarnings("unchecked")
		List<Object> systems = (List<Object>) itemsObj;
		if (systems.isEmpty()) {
			throw new SkipException(
					REQ_SUBSYSTEM_COLLECTION + " — /systems items array is empty; no parent-system candidates.");
		}

		// Probe up to 15 systems for one with non-empty subsystems
		String chosenParent = null;
		Response chosenSubsResp = null;
		Map<String, Object> chosenSubsBody = null;
		int probeLimit = Math.min(systems.size(), 15);
		for (int i = 0; i < probeLimit; i++) {
			Object sys = systems.get(i);
			if (!(sys instanceof Map)) {
				continue;
			}
			Object id = ((Map<?, ?>) sys).get("id");
			if (!(id instanceof String)) {
				continue;
			}
			String sysId = (String) id;
			URI probeUri = URI.create(base + "systems/" + sysId + "/subsystems");
			Response probeResp = given().accept("application/json").when().get(probeUri).andReturn();
			if (probeResp.getStatusCode() != 200) {
				continue;
			}
			Map<String, Object> probeBody;
			try {
				probeBody = probeResp.jsonPath().getMap("$");
			}
			catch (Exception ex) {
				continue;
			}
			Object probeItems = (probeBody == null) ? null : probeBody.get("items");
			if (probeItems instanceof List && !((List<?>) probeItems).isEmpty()) {
				chosenParent = sysId;
				chosenSubsResp = probeResp;
				chosenSubsBody = probeBody;
				break;
			}
		}

		if (chosenParent == null) {
			throw new SkipException(REQ_SUBSYSTEM_COLLECTION + " — probed " + probeLimit
					+ " parent systems; none have non-empty /subsystems collections. "
					+ "IUT does not expose subsystems data for this test run; per Sprint 4 design.md "
					+ "§'Sprint 4 hardening: Subsystems conformance class scope' acceptance criterion, "
					+ "all 4 Subsystems @Tests SKIP-with-reason rather than FAIL (subsystems are an "
					+ "OPTIONAL System composition; absence is a valid IUT state).");
		}

		this.parentSystemId = chosenParent;
		this.subsystemsUri = URI.create(base + "systems/" + chosenParent + "/subsystems");
		this.subsystemsResponse = chosenSubsResp;
		this.subsystemsBody = chosenSubsBody;

		// Eagerly resolve first-subsystem id + fetch /systems/{parent}/subsystems/{id}
		// so per-item @Tests can assert against cached state.
		this.firstSubsystemId = extractFirstSubsystemId();
		if (this.firstSubsystemId != null) {
			URI itemUri = URI.create(this.subsystemsUri.toString() + "/" + this.firstSubsystemId);
			this.subsystemItemResponse = given().accept("application/json").when().get(itemUri).andReturn();
			try {
				this.subsystemItemBody = this.subsystemItemResponse.jsonPath().getMap("$");
			}
			catch (Exception ex) {
				this.subsystemItemBody = null;
			}
		}
	}

	/**
	 * SCENARIO-ETS-PART1-003-SUBSYSTEMS-RESOURCES-001 (CRITICAL): {@code GET
	 * /systems/{parentId}/subsystems} returns HTTP 200 + non-empty {@code items} array.
	 *
	 * <p>
	 * Per OGC 23-001 {@code /req/subsystem/collection}: "The server SHALL expose a
	 * {system-resources-endpoint} at path {api_root}/systems/{parentId}/subsystems."
	 * </p>
	 *
	 * <p>
	 * GeoRobotix curl evidence (2026-04-29): system {@code 0n3rtpmuihc0}'s
	 * {@code /subsystems} endpoint returns 200 + 12 GeoJSON Features. The {@code items}
	 * key (NOT {@code features}) matches OGC API Features Clause 7.15.2-7.15.8
	 * convention.
	 * </p>
	 */
	@Test(description = "OGC-23-001 " + REQ_SUBSYSTEM_COLLECTION
			+ ": GET /systems/{parentId}/subsystems returns HTTP 200 + non-empty items array (REQ-ETS-PART1-003, SCENARIO-ETS-PART1-003-SUBSYSTEMS-RESOURCES-001)",
			groups = "subsystems")
	@SuppressWarnings("unchecked")
	public void subsystemsCollectionReturns200() {
		ETSAssert.assertStatus(this.subsystemsResponse, 200, REQ_SUBSYSTEM_COLLECTION);
		if (this.subsystemsBody == null) {
			ETSAssert.failWithUri(REQ_SUBSYSTEM_COLLECTION,
					"/systems/" + this.parentSystemId + "/subsystems body did not parse as JSON. Content-Type was: "
							+ this.subsystemsResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.subsystemsBody, "items", List.class, REQ_SUBSYSTEM_COLLECTION);
		List<Object> items = (List<Object>) this.subsystemsBody.get("items");
		if (items.isEmpty()) {
			ETSAssert.failWithUri(REQ_SUBSYSTEM_COLLECTION,
					"/systems/" + this.parentSystemId + "/subsystems items array is empty; expected at least one "
							+ "Subsystem (BeforeClass selected this parent BECAUSE it had non-empty subsystems — a "
							+ "concurrent IUT mutation may have emptied it between probe and assertion).");
		}
	}

	/**
	 * SCENARIO-ETS-PART1-003-SUBSYSTEMS-CANONICAL-001 (NORMAL): the first subsystem item
	 * dereferenced at {@code /systems/{parentId}/subsystems/{id}} has {@code id}
	 * (string), {@code type} (string), and {@code links} (array) per OGC 23-001
	 * {@code /req/system/canonical-endpoint} (inherited via
	 * {@code /req/subsystem inherit:: /req/system}).
	 *
	 * <p>
	 * Subsystems ARE Systems (per OGC 23-001 §System Components composition rules) and
	 * therefore inherit the canonical-endpoint shape. This test exercises the inheritance
	 * at the subsystems collection level; the same shape would also be reachable via the
	 * top-level {@code /systems/{id}} endpoint (subsystems are dereferenceable BOTH ways
	 * — curl-verified 2026-04-29).
	 * </p>
	 */
	@Test(description = "OGC-23-001 " + REQ_CANONICAL_ENDPOINT
			+ ": GET /systems/{parentId}/subsystems/{id} has id+type+links per inherited canonical-endpoint shape (REQ-ETS-PART1-003, SCENARIO-ETS-PART1-003-SUBSYSTEMS-CANONICAL-001)",
			dependsOnMethods = "subsystemsCollectionReturns200", groups = "subsystems")
	public void subsystemItemHasIdTypeLinks() {
		if (this.firstSubsystemId == null) {
			throw new SkipException(REQ_CANONICAL_ENDPOINT
					+ " — no items in /subsystems collection to dereference; cannot test single-item shape.");
		}
		ETSAssert.assertStatus(this.subsystemItemResponse, 200, REQ_CANONICAL_ENDPOINT);
		if (this.subsystemItemBody == null) {
			ETSAssert.failWithUri(REQ_CANONICAL_ENDPOINT,
					"/systems/" + this.parentSystemId + "/subsystems/" + this.firstSubsystemId
							+ " body did not parse as JSON. Content-Type was: "
							+ this.subsystemItemResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.subsystemItemBody, "id", String.class, REQ_CANONICAL_ENDPOINT);
		ETSAssert.assertJsonObjectHas(this.subsystemItemBody, "type", String.class, REQ_CANONICAL_ENDPOINT);
		ETSAssert.assertJsonObjectHas(this.subsystemItemBody, "links", List.class, REQ_CANONICAL_ENDPOINT);
	}

	/**
	 * SCENARIO-ETS-PART1-003-SUBSYSTEMS-CANONICAL-URL-001 (NORMAL): single-subsystem
	 * {@code links} contains {@code rel="canonical"} per OGC 23-001
	 * {@code /req/system/canonical-url} (inherited).
	 *
	 * <p>
	 * Same v1.0 GH#3 fix policy as
	 * {@link org.opengis.cite.ogcapiconnectedsystems10.conformance.systemfeatures.SystemFeaturesTests#systemsCollectionLinksDiscipline()}:
	 * {@code rel="canonical"} is the load-bearing assertion; absence of
	 * {@code rel="self"} is NOT a FAIL.
	 * </p>
	 */
	@Test(description = "OGC-23-001 " + REQ_CANONICAL_URL
			+ ": /systems/{parentId}/subsystems/{id} links contain rel=canonical (REQ-ETS-PART1-003, SCENARIO-ETS-PART1-003-SUBSYSTEMS-CANONICAL-URL-001 — preserves v1.0 GH#3 fix)",
			dependsOnMethods = "subsystemItemHasIdTypeLinks", groups = "subsystems")
	public void subsystemItemHasCanonicalLink() {
		if (this.firstSubsystemId == null || this.subsystemItemBody == null) {
			throw new SkipException(
					REQ_CANONICAL_URL + " — no single-subsystem body available to assert link discipline.");
		}
		List<?> links = itemLinksList();
		Predicate<Object> isCanonical = l -> (l instanceof Map) && "canonical".equals(((Map<?, ?>) l).get("rel"));
		ETSAssert.assertJsonArrayContains(links, isCanonical, "rel=canonical link on /systems/" + this.parentSystemId
				+ "/subsystems/" + this.firstSubsystemId + " (got rels: " + collectItemRels() + ")", REQ_CANONICAL_URL);
	}

	/**
	 * SCENARIO-ETS-PART1-003-SUBSYSTEMS-PARENT-LINK-001 (CRITICAL,
	 * <strong>UNIQUE-to-Subsystems architectural invariant</strong>): single-subsystem
	 * {@code links} contains {@code rel="parent"} pointing back at the parent system.
	 *
	 * <p>
	 * GeoRobotix curl evidence (2026-04-29): subsystem {@code 0nar3cl0tk3g} carries
	 * {@code {"rel":"parent","title":"Parent
	 * system","href":".../systems/0n3rtpmuihc0?f=geojson"}}. The href substring
	 * {@code /systems/0n3rtpmuihc0} matches the parent-system id (architectural
	 * composition invariant: a subsystem's parent link MUST resolve back to the same
	 * parent under whose {@code /subsystems} collection the subsystem was discovered).
	 * </p>
	 *
	 * <p>
	 * Asserted under {@link #REQ_SUBSYSTEM_CLASS} (the requirements class itself) since
	 * the OGC source repo's {@code /req/subsystem/} folder does not define a standalone
	 * per-link sub-requirement (verified 2026-04-29). The parent link is implied by OGC
	 * 23-001 §System Components composition rules and is the load-bearing semantic
	 * distinction between a System and a Subsystem in the resource graph.
	 * </p>
	 */
	@Test(description = "OGC-23-001 " + REQ_SUBSYSTEM_CLASS
			+ ": /systems/{parentId}/subsystems/{id} links contain rel=parent referencing parent system (REQ-ETS-PART1-003, SCENARIO-ETS-PART1-003-SUBSYSTEMS-PARENT-LINK-001 — UNIQUE-to-Subsystems architectural invariant)",
			dependsOnMethods = "subsystemItemHasIdTypeLinks", groups = "subsystems")
	public void subsystemHasParentSystemLink() {
		if (this.firstSubsystemId == null || this.subsystemItemBody == null) {
			throw new SkipException(
					REQ_SUBSYSTEM_CLASS + " — no single-subsystem body available to assert parent-link discipline.");
		}
		List<?> links = itemLinksList();
		// Parent-link discipline (curl-verified 2026-04-29): rel=parent + href contains
		// "/systems/{parentSystemId}" (with optional ?f=... format param).
		// Accept BOTH forms (some IUTs may emit absolute URLs, others relative; some may
		// emit query string, others not).
		String parentMatch = "/systems/" + this.parentSystemId;
		Predicate<Object> isParentLink = l -> {
			if (!(l instanceof Map)) {
				return false;
			}
			Map<?, ?> link = (Map<?, ?>) l;
			Object rel = link.get("rel");
			Object href = link.get("href");
			if (!"parent".equals(rel)) {
				return false;
			}
			return (href instanceof String) && ((String) href).contains(parentMatch);
		};
		ETSAssert.assertJsonArrayContains(links, isParentLink,
				"rel=parent link with href containing '" + parentMatch + "' on /systems/" + this.parentSystemId
						+ "/subsystems/" + this.firstSubsystemId + " (got rels: " + collectItemRels()
						+ "; parent system MUST link back per OGC 23-001 §System Components composition)",
				REQ_SUBSYSTEM_CLASS);
	}

	// --------------- helpers ---------------

	/**
	 * Extracts the {@code id} of the first item in the cached {@code /subsystems}
	 * collection. Returns {@code null} if the body is missing, the {@code items} array is
	 * missing/empty, or the first item lacks a string {@code id}.
	 */
	@SuppressWarnings("unchecked")
	private String extractFirstSubsystemId() {
		if (this.subsystemsBody == null) {
			return null;
		}
		Object itemsObj = this.subsystemsBody.get("items");
		if (!(itemsObj instanceof List)) {
			return null;
		}
		List<Object> items = (List<Object>) itemsObj;
		if (items.isEmpty()) {
			return null;
		}
		Object firstItem = items.get(0);
		if (!(firstItem instanceof Map)) {
			return null;
		}
		Object id = ((Map<?, ?>) firstItem).get("id");
		return (id instanceof String) ? (String) id : null;
	}

	/**
	 * Returns the parsed {@code links} array from the cached single-subsystem body.
	 * Returns an empty list if the body or {@code links} field is missing.
	 */
	private List<?> itemLinksList() {
		Object links = (this.subsystemItemBody == null) ? null : this.subsystemItemBody.get("links");
		return (links instanceof List) ? (List<?>) links : List.of();
	}

	/**
	 * Extracts the set of distinct {@code rel} values from the single-subsystem
	 * {@code links} array.
	 */
	@SuppressWarnings("unchecked")
	private Set<String> collectItemRels() {
		Object links = (this.subsystemItemBody == null) ? null : this.subsystemItemBody.get("links");
		if (!(links instanceof List)) {
			return Set.of();
		}
		List<Map<String, Object>> linkList = (List<Map<String, Object>>) links;
		return linkList.stream()
			.map(l -> l == null ? null : l.get("rel"))
			.filter(r -> r instanceof String)
			.map(r -> (String) r)
			.collect(Collectors.toSet());
	}

}
