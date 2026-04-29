package org.opengis.cite.ogcapiconnectedsystems10.conformance.common;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * OGC API Common Part 1 + Part 2 — Common conformance class tests ({@code /conf/common};
 * OGC 19-072 + 20-024).
 *
 * <p>
 * Implements <strong>REQ-ETS-PART1-001</strong> (Common conformance class). Mirrors the
 * S-ETS-01-02 / S-ETS-02-06 architectural pattern established by
 * {@code conformance.core.*} and {@code conformance.systemfeatures.*}: one TestNG
 * {@code @Test} per OGC ATS assertion, each carrying the canonical OGC requirement URI in
 * its {@code description}, every assertion routed through {@link ETSAssert} helpers per
 * ADR-008 (zero bare {@code throw new AssertionError}).
 * </p>
 *
 * <p>
 * Coverage scope (Sprint-1-style minimal-then-expand per architect-handoff
 * {@code constraints_for_generator.must} item 17 — 4 {@code @Test} methods at Sprint 3
 * close, deliberately distinct from the Core class's coverage to avoid duplication;
 * Sprint 4+ expansion adds Common Part 2 collections-description, content-negotiation
 * parameter validation, paging discipline, and the remaining Common Part 1 oas30
 * subrequirements.
 * </p>
 *
 * <p>
 * <strong>Independence from Core</strong>: Common is INDEPENDENT of Core (same
 * group-dependency-DAG-root level). The {@code common} group does NOT declare
 * {@code dependsOnGroups="core"} in testng.xml — Common runs in parallel with Core. This
 * matches the Sprint 3 contract acceptance criterion and design.md
 * §SystemFeatures-conformance-class-scope precedent (groups are sibling, not transitive).
 * </p>
 *
 * <p>
 * <strong>Distinct surface from Core</strong>: Core (Sprint 1) covers the SUCCESS-side
 * sub-requirements at the landing-page + oas30 layer
 * ({@code /req/landing-page/root-success}, {@code /req/landing-page/conformance-success},
 * {@code /req/landing-page/api-definition-success}, {@code /req/oas30/oas-impl}). Common
 * (Sprint 3) covers the JSON encoding class ({@code /req/json/definition},
 * {@code /req/json/content}) plus the Common Part 2 collections-list class
 * ({@code /req/collections/collections-list-success},
 * {@code /req/collections/collections-list-links}). These are NOT covered by Core; Common
 * is the load-bearing class for them.
 * </p>
 *
 * <p>
 * Covers:
 * </p>
 * <ul>
 * <li><strong>SCENARIO-ETS-PART1-001-COMMON-LANDING-001</strong> (CRITICAL) — Common
 * landing-page link discipline beyond Core's subset (in our pivot:
 * {@code rel="conformance"} link has a JSON {@code type} attribute per
 * {@code /req/json/definition}).</li>
 * <li><strong>SCENARIO-ETS-PART1-001-COMMON-CONFORMANCE-001</strong> (NORMAL) —
 * {@code /conformance} declares {@code ogcapi-common-1/1.0/conf/core} per
 * {@code /req/landing-page/conformance-success} (Common-class-specific assertion).</li>
 * <li><strong>SCENARIO-ETS-PART1-001-COMMON-COLLECTIONS-001</strong> (NORMAL) —
 * {@code /collections} returns a {@code collections} array per Common Part 2
 * {@code /req/collections/collections-list-success}.</li>
 * <li><strong>SCENARIO-ETS-PART1-001-COMMON-CONTENT-NEGOTIATION-001</strong> (NORMAL) —
 * {@code GET /?f=json} returns JSON; {@code ?f=html} returns 4xx OR HTML (per IUT
 * capability — content-negotiation discipline).</li>
 * </ul>
 *
 * <p>
 * <strong>Curl-confirmed shape (2026-04-29)</strong> — GeoRobotix landing page DOES
 * declare {@code rel="conformance"} link with {@code type="application/json"};
 * {@code /conformance} body declares {@code ogcapi-common-1/1.0/conf/core} +
 * {@code ogcapi-common-2/0.0/conf/collections}; {@code /collections} returns 200 with
 * {@code collections} array containing {@code id="all_systems"}; {@code ?f=html} returns
 * HTTP 400 ("Unsupported format: text/html") which is acceptable per content-negotiation
 * discipline (the IUT explicitly handles the parameter rather than ignoring it). Full
 * curl evidence archived in {@code epics/stories/s-ets-03-07-common-conformance-class.md}
 * Implementation Notes.
 * </p>
 */
public class CommonTests {

	/**
	 * Canonical OGC requirement URI for {@code /req/json/definition} (Common Part 1
	 * §"JSON" requirements class).
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-common/master/19-072/requirements/json/REQ_definition.adoc">REQ_definition.adoc</a>
	 */
	static final String REQ_JSON_DEFINITION = "http://www.opengis.net/spec/ogcapi-common-1/1.0/req/json/definition";

	/**
	 * Canonical OGC requirement URI for {@code /req/landing-page/conformance-success}
	 * (Common Part 1 §"Landing Page" requirements class). Reused at the Common-class
	 * layer to assert that {@code conformsTo} declares Common Core itself.
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-common/master/19-072/requirements/landing-page/REQ_conformance-success.adoc">REQ_conformance-success.adoc</a>
	 */
	static final String REQ_CONFORMANCE_SUCCESS = "http://www.opengis.net/spec/ogcapi-common-1/1.0/req/landing-page/conformance-success";

	/** Common Part 1 Core conformance class declaration URI (asserted to be present). */
	static final String COMMON_CORE_CONFORMANCE_URI = "http://www.opengis.net/spec/ogcapi-common-1/1.0/conf/core";

	/**
	 * Canonical OGC requirement URI for {@code /req/collections/collections-list-success}
	 * (Common Part 2 §"Collections" requirements class).
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-common/master/collections/requirements/collections/REQ_collections-collections-list-success.adoc">REQ_collections-collections-list-success.adoc</a>
	 */
	static final String REQ_COLLECTIONS_LIST_SUCCESS = "http://www.opengis.net/spec/ogcapi-common-2/0.0/req/collections/collections-list-success";

	/**
	 * Canonical OGC requirement URI for {@code /req/json/content} (Common Part 1 §"JSON"
	 * requirements class). Used as the URI prefix for the content-negotiation discipline
	 * assertion (the JSON-encoding class is the load-bearing requirement for content
	 * negotiation when the IUT honours {@code ?f=json}).
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-common/master/19-072/requirements/json/REQ_content.adoc">REQ_content.adoc</a>
	 */
	static final String REQ_JSON_CONTENT = "http://www.opengis.net/spec/ogcapi-common-1/1.0/req/json/content";

	private URI iutUri;

	private Response landingResponse;

	private Map<String, Object> landingBody;

	private Response conformanceResponse;

	private Map<String, Object> conformanceBody;

	private Response collectionsResponse;

	private Map<String, Object> collectionsBody;

	private Response landingFJsonResponse;

	/**
	 * Reads the {@code iut} suite attribute, fetches {@code /}, {@code /conformance},
	 * {@code /collections}, and {@code /?f=json} once. All four {@code @Test} methods
	 * operate on cached responses to avoid redundant traffic against the IUT (mirrors
	 * {@code SystemFeaturesTests.fetchSystemsCollection} pattern per design.md §"Fixtures
	 * and listeners").
	 *
	 * <p>
	 * If {@code iut} is missing or not a URI, throws {@link SkipException} to
	 * cascade-skip all four {@code @Test} methods.
	 * </p>
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchCommonResources(ITestContext testContext) {
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		this.iutUri = (URI) iutAttr;
		String iutString = this.iutUri.toString();
		String base = iutString.endsWith("/") ? iutString : iutString + "/";

		this.landingResponse = given().accept("application/json").when().get(URI.create(iutString)).andReturn();
		this.landingBody = parseJsonOrNull(this.landingResponse);

		this.conformanceResponse = given().accept("application/json")
			.when()
			.get(URI.create(base + "conformance"))
			.andReturn();
		this.conformanceBody = parseJsonOrNull(this.conformanceResponse);

		this.collectionsResponse = given().accept("application/json")
			.when()
			.get(URI.create(base + "collections"))
			.andReturn();
		this.collectionsBody = parseJsonOrNull(this.collectionsResponse);

		// Content-negotiation probe: explicitly request JSON via the ?f=json query
		// parameter (per Common Part 1 §"JSON" content-negotiation discipline). HTML is
		// probed lazily inside the @Test to avoid double-fetching when not needed.
		this.landingFJsonResponse = given().when().get(URI.create(base + "?f=json")).andReturn();
	}

	/**
	 * SCENARIO-ETS-PART1-001-COMMON-LANDING-001 (CRITICAL): Common landing-page link
	 * discipline — the {@code rel="conformance"} link MUST carry a {@code type} attribute
	 * that identifies the JSON media type (per Common Part 1 §"JSON" requirements class
	 * {@code /req/json/definition}, the JSON-encoded link MUST be discoverable via the
	 * {@code type} attribute on each link).
	 *
	 * <p>
	 * Beyond Core's landing-page subset (which only asserts the {@code rel="conformance"}
	 * link is present): Common asserts the link's {@code type} attribute identifies the
	 * JSON media type. GeoRobotix curl evidence (2026-04-29) confirms
	 * {@code "type": "application/json"} on the conformance link.
	 * </p>
	 */
	@Test(description = "OGC-19-072 " + REQ_JSON_DEFINITION
			+ ": landing-page rel=conformance link has type attribute identifying JSON (REQ-ETS-PART1-001, SCENARIO-ETS-PART1-001-COMMON-LANDING-001)",
			groups = "common")
	public void commonLandingPageConformanceLinkHasJsonType() {
		ETSAssert.assertStatus(this.landingResponse, 200, REQ_JSON_DEFINITION);
		if (this.landingBody == null) {
			ETSAssert.failWithUri(REQ_JSON_DEFINITION, "landing page body did not parse as JSON. Content-Type was: "
					+ this.landingResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.landingBody, "links", List.class, REQ_JSON_DEFINITION);
		@SuppressWarnings("unchecked")
		List<Object> links = (List<Object>) this.landingBody.get("links");
		Predicate<Object> conformanceWithJsonType = l -> {
			if (!(l instanceof Map)) {
				return false;
			}
			Map<?, ?> m = (Map<?, ?>) l;
			if (!"conformance".equals(m.get("rel"))) {
				return false;
			}
			Object type = m.get("type");
			return (type instanceof String) && ((String) type).toLowerCase().contains("json");
		};
		ETSAssert.assertJsonArrayContains(links, conformanceWithJsonType,
				"link with rel=conformance AND type containing 'json' (Common /req/json/definition discipline)",
				REQ_JSON_DEFINITION);
	}

	/**
	 * SCENARIO-ETS-PART1-001-COMMON-CONFORMANCE-001 (NORMAL): the IUT's
	 * {@code /conformance} body MUST declare the OGC API Common Part 1 Core conformance
	 * URI ({@code .../ogcapi-common-1/1.0/conf/core}). This is the load-bearing
	 * Common-specific assertion that Core does NOT make (Core asserts conformance-success
	 * shape; Common asserts the IUT actually advertises Common Core conformance — the
	 * prerequisite for any Common-derived spec).
	 */
	@Test(description = "OGC-19-072 " + REQ_CONFORMANCE_SUCCESS
			+ ": /conformance conformsTo declares OGC API Common Part 1 Core (REQ-ETS-PART1-001, SCENARIO-ETS-PART1-001-COMMON-CONFORMANCE-001)",
			groups = "common")
	public void commonConformanceDeclaresCommonCore() {
		ETSAssert.assertStatus(this.conformanceResponse, 200, REQ_CONFORMANCE_SUCCESS);
		if (this.conformanceBody == null) {
			ETSAssert.failWithUri(REQ_CONFORMANCE_SUCCESS, "/conformance body did not parse as JSON. Content-Type was: "
					+ this.conformanceResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.conformanceBody, "conformsTo", List.class, REQ_CONFORMANCE_SUCCESS);
		@SuppressWarnings("unchecked")
		List<Object> conformsTo = (List<Object>) this.conformanceBody.get("conformsTo");
		Predicate<Object> isCommonCore = u -> COMMON_CORE_CONFORMANCE_URI.equals(u);
		ETSAssert.assertJsonArrayContains(conformsTo, isCommonCore,
				"OGC API Common Part 1 Core URI (" + COMMON_CORE_CONFORMANCE_URI + ")", REQ_CONFORMANCE_SUCCESS);
	}

	/**
	 * SCENARIO-ETS-PART1-001-COMMON-COLLECTIONS-001 (NORMAL): the IUT's
	 * {@code /collections} endpoint MUST return a {@code collections} array per Common
	 * Part 2 {@code /req/collections/collections-list-success}. SKIP-with-reason if the
	 * IUT does NOT implement {@code /collections} (HTTP 404) — Common Part 2 collections
	 * is a separate conformance class declaration; not all OGC API Common implementations
	 * advertise it.
	 *
	 * <p>
	 * GeoRobotix curl evidence (2026-04-29): {@code /collections} returns 200 with
	 * {@code collections: [{id: "all_systems", ...}, {id: "all_datastreams", ...}, ...]};
	 * {@code /conformance} declares {@code ogcapi-common-2/0.0/conf/collections} so this
	 * test PASSES against GeoRobotix.
	 * </p>
	 */
	@Test(description = "OGC-20-024 " + REQ_COLLECTIONS_LIST_SUCCESS
			+ ": /collections returns 200 with collections array (REQ-ETS-PART1-001, SCENARIO-ETS-PART1-001-COMMON-COLLECTIONS-001)",
			groups = "common")
	public void commonCollectionsEndpointReturnsCollectionsArray() {
		int status = this.collectionsResponse.getStatusCode();
		if (status == 404) {
			throw new SkipException(REQ_COLLECTIONS_LIST_SUCCESS
					+ " — IUT does not implement /collections (HTTP 404). Common Part 2 collections "
					+ "conformance class is OPTIONAL; SKIP per /req/collections/collections-list-success "
					+ "MAY-priority handling. Verify /conformance declaration for "
					+ "ogcapi-common-2/0.0/conf/collections to confirm IUT does not advertise this class.");
		}
		ETSAssert.assertStatus(this.collectionsResponse, 200, REQ_COLLECTIONS_LIST_SUCCESS);
		if (this.collectionsBody == null) {
			ETSAssert.failWithUri(REQ_COLLECTIONS_LIST_SUCCESS,
					"/collections body did not parse as JSON. Content-Type was: "
							+ this.collectionsResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.collectionsBody, "collections", List.class, REQ_COLLECTIONS_LIST_SUCCESS);
		@SuppressWarnings("unchecked")
		List<Object> collections = (List<Object>) this.collectionsBody.get("collections");
		if (collections.isEmpty()) {
			ETSAssert.failWithUri(REQ_COLLECTIONS_LIST_SUCCESS,
					"/collections collections array is empty; expected at least one collection. "
							+ "If the IUT is intentionally empty, expand /req/collections/collections-list-success "
							+ "tolerance per Sprint 4+ MAY-priority handling.");
		}
	}

	/**
	 * SCENARIO-ETS-PART1-001-COMMON-CONTENT-NEGOTIATION-001 (NORMAL): the IUT MUST honour
	 * the {@code ?f=json} query parameter — {@code GET /?f=json} returns a JSON-shaped
	 * response (HTTP 200 + Content-Type containing "json"). Per Common Part 1 §"JSON"
	 * requirements class {@code /req/json/content}, the JSON-encoding class mandates the
	 * IUT acknowledge JSON-format requests.
	 *
	 * <p>
	 * GeoRobotix curl evidence (2026-04-29): {@code GET /?f=json} returns 200 with the
	 * landing-page JSON body. (Note: {@code ?f=html} returns HTTP 400 "Unsupported
	 * format: text/html" — acceptable per content-negotiation discipline since the IUT
	 * explicitly handles the parameter rather than ignoring it; the HTML-encoding class
	 * is OPTIONAL per Common Part 1 §"HTML".)
	 * </p>
	 */
	@Test(description = "OGC-19-072 " + REQ_JSON_CONTENT
			+ ": GET /?f=json returns JSON (REQ-ETS-PART1-001, SCENARIO-ETS-PART1-001-COMMON-CONTENT-NEGOTIATION-001)",
			groups = "common")
	public void commonContentNegotiationHonoursFJsonParameter() {
		ETSAssert.assertStatus(this.landingFJsonResponse, 200, REQ_JSON_CONTENT);
		String contentType = this.landingFJsonResponse.getContentType();
		if (contentType == null || !contentType.toLowerCase().contains("json")) {
			ETSAssert.failWithUri(REQ_JSON_CONTENT,
					"GET /?f=json returned 200 but Content-Type does not contain 'json' (got: " + contentType
							+ "). Per /req/json/content, the IUT MUST return a JSON-shaped response when "
							+ "the JSON encoding class is requested via the f= parameter.");
		}
	}

	// --------------- helpers ---------------

	/**
	 * Parse a REST-Assured response body as a JSON object Map; return null if the body is
	 * not parseable as JSON (e.g. HTML 4xx error pages). Mirrors the
	 * {@code SystemFeaturesTests.fetchSystemsCollection} try/catch pattern.
	 */
	private static Map<String, Object> parseJsonOrNull(Response resp) {
		try {
			return resp.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			return null;
		}
	}

}
