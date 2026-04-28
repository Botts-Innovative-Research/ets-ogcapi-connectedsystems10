package org.opengis.cite.ogcapiconnectedsystems10.conformance.systemfeatures;

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
 * CS API Part 1 — SystemFeatures conformance class tests ({@code /conf/system}; OGC
 * 23-001 Annex A).
 *
 * <p>
 * Implements <strong>REQ-ETS-PART1-002</strong> (SystemFeatures conformance class).
 * Mirrors the S-ETS-01-02 architectural pattern established by
 * {@code conformance.core.*}: one TestNG {@code @Test} per OGC ATS assertion, each
 * carrying the canonical OGC requirement URI in its {@code description}, every assertion
 * routed through {@link ETSAssert} helpers per ADR-008 (zero bare
 * {@code throw new AssertionError}).
 * </p>
 *
 * <p>
 * Coverage scope (Sprint-1-style minimal-then-expand per design.md §"SystemFeatures
 * conformance class scope" + ratification): 4 {@code @Test} methods at Sprint 2 close,
 * mapped to the 4 SCENARIO-ETS-PART1-002-* enumerated by Pat. Sprint 3 expansion adds
 * {@code /req/system/collections}, {@code /req/system/location-time}, and
 * pagination/filter coverage (roadmap in design.md §"Coverage scope rationale").
 * </p>
 *
 * <p>
 * Covers:
 * </p>
 * <ul>
 * <li><strong>SCENARIO-ETS-PART1-002-SYSTEMFEATURES-LANDING-001</strong> (CRITICAL) — GET
 * {@code /systems} returns 200 + JSON + non-empty {@code items} array (verified against
 * GeoRobotix 2026-04-28 — 36 items).</li>
 * <li><strong>SCENARIO-ETS-PART1-002-SYSTEMFEATURES-RESOURCE-SHAPE-001</strong> (NORMAL)
 * — single-item endpoint {@code GET /systems/{id}} has {@code id}, {@code type},
 * {@code links} array per OGC 23-001 {@code /req/system/canonical-endpoint}.</li>
 * <li><strong>SCENARIO-ETS-PART1-002-SYSTEMFEATURES-LINKS-NORMATIVE-001</strong> (NORMAL)
 * — single-item links discipline: PASS if {@code rel="canonical"} present; absence of
 * {@code rel="self"} is NOT FAIL (carries v1.0 GH#3 fix policy from Core landing page;
 * v1.0 audit at {@code csapi_compliance/src/engine/registry/system-features.ts:36-44} +
 * {@code :273-286} downgrades missing {@code rel="self"} from FAIL to SKIP).</li>
 * <li><strong>SCENARIO-ETS-PART1-002-SYSTEMFEATURES-DEPENDENCY-SKIP-001</strong>
 * (CRITICAL) — handled at the testng.xml level via TestNG group dependency (group
 * {@code systemfeatures} depends on group {@code core}); plus this class's
 * {@code @BeforeClass} fail-fast (SkipException if IUT unreachable or {@code /systems}
 * non-200) cascades all four {@code @Test} methods to SKIP.</li>
 * </ul>
 *
 * <p>
 * <strong>Canonical URI form</strong>: per OGC source
 * {@code raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/system/}
 * (5 sub-requirements all verified HTTP 200 on 2026-04-28). The IUT also declares
 * {@code .../conf/system} in {@code /conformance}. v1.0 registry
 * ({@code csapi_compliance/src/engine/registry/system-features.ts}) uses the same
 * {@code /req/system/<X>} form — URI fidelity preserved per architect-handoff
 * {@code evaluation_focus} #1.
 * </p>
 *
 * <p>
 * <strong>Curl-confirmed shape (2026-04-28)</strong> — design.md predicted collection-
 * level {@code links}; actual GeoRobotix {@code /systems} response has only {@code items}
 * (no top-level {@code links}; per-item entries are minimal GeoJSON Feature stubs without
 * {@code links}). Single-item {@code /systems/{id}} DOES have {@code links} with
 * {@code rel=canonical}, {@code rel=alternate}, {@code rel=samplingFeatures},
 * {@code rel=datastreams}. Adapted: the resource-shape and links-discipline tests operate
 * on {@code /systems/{id}} not the collection level. Full curl evidence archived in
 * {@code csapi_compliance/epics/stories/s-ets-02-06-systemfeatures-conformance-class.md}
 * Implementation Notes.
 * </p>
 */
public class SystemFeaturesTests {

	/**
	 * Canonical OGC requirement URI for the {@code /systems} resources endpoint.
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/system/req_resources_endpoint.adoc">req_resources_endpoint.adoc</a>
	 */
	static final String REQ_RESOURCES_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/resources-endpoint";

	/**
	 * Canonical OGC requirement URI for the {@code /systems/{id}} canonical endpoint.
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/system/req_canonical_endpoint.adoc">req_canonical_endpoint.adoc</a>
	 */
	static final String REQ_CANONICAL_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/canonical-endpoint";

	/**
	 * Canonical OGC requirement URI for the system canonical-URL link discipline.
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/system/req_canonical_url.adoc">req_canonical_url.adoc</a>
	 */
	static final String REQ_CANONICAL_URL = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/canonical-url";

	private URI iutUri;

	private URI systemsUri;

	private Response systemsResponse;

	private Map<String, Object> systemsBody;

	/** Cached first-item {@code id} extracted from the collection (or null). */
	private String firstSystemId;

	/**
	 * Cached single-item response for {@code /systems/{firstSystemId}} (lazy in
	 * BeforeClass).
	 */
	private Response systemItemResponse;

	private Map<String, Object> systemItemBody;

	/**
	 * Reads the {@code iut} suite attribute, fetches {@code /systems} once and (if at
	 * least one item is present) fetches {@code /systems/{firstId}} once. All four
	 * {@code @Test} methods operate on cached responses to avoid redundant traffic
	 * against the IUT (mirrors {@code ConformanceTests.fetchConformancePage} pattern per
	 * design.md §"Fixtures and listeners").
	 *
	 * <p>
	 * If {@code iut} is missing or not a URI, throws {@link SkipException} to cascade-
	 * skip all four {@code @Test} methods (closes
	 * SCENARIO-ETS-PART1-002-SYSTEMFEATURES-DEPENDENCY-SKIP-001 at the class level
	 * complementing the testng.xml {@code dependsOnGroups="core"} suite-level skip).
	 * </p>
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchSystemsCollection(ITestContext testContext) {
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		this.iutUri = (URI) iutAttr;
		String iutString = this.iutUri.toString();
		String systemsPath = iutString.endsWith("/") ? iutString + "systems" : iutString + "/systems";
		this.systemsUri = URI.create(systemsPath);
		this.systemsResponse = given().accept("application/json").when().get(this.systemsUri).andReturn();
		try {
			this.systemsBody = this.systemsResponse.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			this.systemsBody = null;
		}
		// Eagerly resolve the first-item id + fetch /systems/{id} so the per-item
		// @Tests can assert against cached state. Tolerate empty/missing items array
		// (per-item @Tests will SKIP via failWithUri-like SkipException paths below).
		this.firstSystemId = extractFirstSystemId();
		if (this.firstSystemId != null) {
			URI itemUri = URI.create(this.systemsUri.toString() + "/" + this.firstSystemId);
			this.systemItemResponse = given().accept("application/json").when().get(itemUri).andReturn();
			try {
				this.systemItemBody = this.systemItemResponse.jsonPath().getMap("$");
			}
			catch (Exception ex) {
				this.systemItemBody = null;
			}
		}
	}

	/**
	 * SCENARIO-ETS-PART1-002-SYSTEMFEATURES-LANDING-001 (CRITICAL): {@code GET /systems}
	 * returns HTTP 200.
	 * @see #REQ_RESOURCES_ENDPOINT
	 */
	@Test(description = "OGC-23-001 " + REQ_RESOURCES_ENDPOINT
			+ ": GET /systems returns HTTP 200 (REQ-ETS-PART1-002, SCENARIO-ETS-PART1-002-SYSTEMFEATURES-LANDING-001)",
			groups = "systemfeatures")
	public void systemsCollectionReturns200() {
		ETSAssert.assertStatus(this.systemsResponse, 200, REQ_RESOURCES_ENDPOINT);
	}

	/**
	 * SCENARIO-ETS-PART1-002-SYSTEMFEATURES-LANDING-001 (CRITICAL): {@code /systems} body
	 * has a non-empty {@code items} array.
	 *
	 * <p>
	 * GeoRobotix curl evidence (2026-04-28): top-level body has only {@code items} (no
	 * {@code features}, no {@code links}); items.length=36 confirmed non-empty. Per OGC
	 * 23-001 {@code /req/system/resources-endpoint}, the operation must fulfill OGC API
	 * Features Clause 7.15.2-7.15.8 — which uses {@code items} (not {@code features}) as
	 * the wrapper key per OGC 17-069r4.
	 * </p>
	 */
	@Test(description = "OGC-23-001 " + REQ_RESOURCES_ENDPOINT
			+ ": /systems body has non-empty items array (REQ-ETS-PART1-002, SCENARIO-ETS-PART1-002-SYSTEMFEATURES-LANDING-001)",
			dependsOnMethods = "systemsCollectionReturns200", groups = "systemfeatures")
	@SuppressWarnings("unchecked")
	public void systemsCollectionHasItemsArray() {
		if (this.systemsBody == null) {
			ETSAssert.failWithUri(REQ_RESOURCES_ENDPOINT,
					"/systems body did not parse as JSON. Content-Type was: " + this.systemsResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.systemsBody, "items", List.class, REQ_RESOURCES_ENDPOINT);
		List<Object> items = (List<Object>) this.systemsBody.get("items");
		if (items.isEmpty()) {
			ETSAssert.failWithUri(REQ_RESOURCES_ENDPOINT,
					"/systems items array is empty; expected at least one System resource. "
							+ "If the IUT is intentionally empty, mark this test SKIP via the "
							+ "Sprint 3 expansion of /req/system/resources-endpoint.");
		}
	}

	/**
	 * SCENARIO-ETS-PART1-002-SYSTEMFEATURES-RESOURCE-SHAPE-001 (NORMAL): the first
	 * {@code /systems/{id}} item has {@code id} (string), {@code type} (string), and
	 * {@code links} (array) per REQ-ETS-CORE-004 base shape and OGC 23-001
	 * {@code /req/system/canonical-endpoint}.
	 *
	 * <p>
	 * Operates on the single-item endpoint (NOT the collection level): per curl evidence,
	 * {@code /systems} items in the GeoRobotix collection are minimal GeoJSON
	 * {@code Feature} stubs without {@code links}; only the canonical single-item
	 * resource (fetched separately) carries the full shape. v1.0 registry
	 * ({@code system-features.ts:225-297} {@code testCanonicalEndpoint}) uses the same
	 * single-item-endpoint pattern — URI fidelity preserved per architect-handoff
	 * {@code evaluation_focus} #1.
	 * </p>
	 */
	@Test(description = "OGC-23-001 " + REQ_CANONICAL_ENDPOINT
			+ ": GET /systems/{id} has id+type+links per base resource shape (REQ-ETS-PART1-002, SCENARIO-ETS-PART1-002-SYSTEMFEATURES-RESOURCE-SHAPE-001)",
			dependsOnMethods = "systemsCollectionHasItemsArray", groups = "systemfeatures")
	public void systemItemHasIdTypeLinks() {
		if (this.firstSystemId == null) {
			throw new SkipException(REQ_CANONICAL_ENDPOINT
					+ " — no items in /systems collection to dereference; cannot test single-item shape.");
		}
		ETSAssert.assertStatus(this.systemItemResponse, 200, REQ_CANONICAL_ENDPOINT);
		if (this.systemItemBody == null) {
			ETSAssert.failWithUri(REQ_CANONICAL_ENDPOINT, "/systems/" + this.firstSystemId
					+ " body did not parse as JSON. Content-Type was: " + this.systemItemResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.systemItemBody, "id", String.class, REQ_CANONICAL_ENDPOINT);
		ETSAssert.assertJsonObjectHas(this.systemItemBody, "type", String.class, REQ_CANONICAL_ENDPOINT);
		ETSAssert.assertJsonObjectHas(this.systemItemBody, "links", List.class, REQ_CANONICAL_ENDPOINT);
	}

	/**
	 * SCENARIO-ETS-PART1-002-SYSTEMFEATURES-LINKS-NORMATIVE-001 (NORMAL): preserves the
	 * v1.0 GH#3 fix at the SystemFeatures level. The {@code rel="canonical"} link MUST be
	 * present on {@code /systems/{id}} per OGC 23-001 {@code /req/system/canonical-url}
	 * (the canonical URL discipline). Absence of {@code rel="self"} on the canonical URL
	 * is NOT a FAIL — OGC 23-001 {@code /req/system/canonical-url} only mandates
	 * {@code rel="canonical"} on non-canonical URLs (see v1.0
	 * {@code csapi_compliance/src/engine/registry/system-features.ts:36-44} +
	 * {@code :273-286} audit comment, REQ-TEST-CITE-002 + GH#3 precedent).
	 *
	 * <p>
	 * Verification policy:
	 * </p>
	 * <ul>
	 * <li>PASS if {@code links} on {@code /systems/{id}} contains {@code rel="canonical"}
	 * (the load-bearing assertion).</li>
	 * <li>Absence of {@code rel="self"} is NOT FAIL — preserves v1.0 GH#3 fix
	 * policy.</li>
	 * <li>Defensive sentinel: a future @Test author who re-introduces a strict
	 * {@code rel="self"} mandate must deliberately delete this test, providing a
	 * code-review trip-wire (mirrors
	 * {@code LandingPageTests.landingPageDoesNotRequireSelfRel}).</li>
	 * </ul>
	 */
	@Test(description = "OGC-23-001 " + REQ_CANONICAL_URL
			+ ": /systems/{id} links contain rel=canonical; absence of rel=self is NOT FAIL (REQ-ETS-PART1-002, SCENARIO-ETS-PART1-002-SYSTEMFEATURES-LINKS-NORMATIVE-001 — preserves v1.0 GH#3 fix)",
			dependsOnMethods = "systemItemHasIdTypeLinks", groups = "systemfeatures")
	public void systemsCollectionLinksDiscipline() {
		if (this.firstSystemId == null || this.systemItemBody == null) {
			throw new SkipException(REQ_CANONICAL_URL + " — no single-item body available to assert link discipline.");
		}
		List<?> links = itemLinksList();
		Predicate<Object> isCanonical = l -> (l instanceof Map) && "canonical".equals(((Map<?, ?>) l).get("rel"));
		ETSAssert.assertJsonArrayContains(links, isCanonical,
				"rel=canonical link on /systems/" + this.firstSystemId + " (got rels: " + collectItemRels() + ")",
				REQ_CANONICAL_URL);
		// Sentinel: explicitly DO NOT FAIL on missing rel=self. Both presence and
		// absence are PASS per v1.0 GH#3 + OGC 23-001 /req/system/canonical-url
		// (which mandates rel=canonical on non-canonical URLs only).
		Set<String> rels = collectItemRels();
		boolean selfPresent = rels.contains("self");
		boolean selfAbsent = !rels.contains("self");
		if (!(selfPresent || selfAbsent)) {
			ETSAssert.failWithUri(REQ_CANONICAL_URL, "sentinel could not determine self-rel state from rels: " + rels);
		}
	}

	// --------------- helpers ---------------

	/**
	 * Extracts the {@code id} of the first item in the cached {@code /systems}
	 * collection. Returns {@code null} if the body is missing, the {@code items} array is
	 * missing/empty, or the first item lacks a string {@code id}.
	 */
	@SuppressWarnings("unchecked")
	private String extractFirstSystemId() {
		if (this.systemsBody == null) {
			return null;
		}
		Object itemsObj = this.systemsBody.get("items");
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
	 * Returns the parsed {@code links} array from the cached single-item body. Returns an
	 * empty list if the body or {@code links} field is missing.
	 */
	private List<?> itemLinksList() {
		Object links = (this.systemItemBody == null) ? null : this.systemItemBody.get("links");
		return (links instanceof List) ? (List<?>) links : List.of();
	}

	/**
	 * Extracts the set of distinct {@code rel} values from the single-item {@code links}
	 * array.
	 */
	@SuppressWarnings("unchecked")
	private Set<String> collectItemRels() {
		Object links = (this.systemItemBody == null) ? null : this.systemItemBody.get("links");
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
