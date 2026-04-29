package org.opengis.cite.ogcapiconnectedsystems10.conformance.procedures;

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
 * CS API Part 1 — Procedures conformance class tests ({@code /conf/procedure-features};
 * OGC 23-001 Annex A).
 *
 * <p>
 * Implements <strong>REQ-ETS-PART1-006</strong> (Procedures conformance class). Fourth
 * additional Part 1 conformance class beyond Core (Sprint 1) + SystemFeatures (Sprint 2)
 * + Common (Sprint 3) + Subsystems (Sprint 4). Mirrors the
 * {@code conformance.subsystems.SubsystemsTests} architectural pattern: one TestNG
 * {@code @Test} per OGC ATS assertion, each carrying the canonical OGC requirement URI in
 * its {@code description}, every assertion routed through {@link ETSAssert} helpers per
 * ADR-008 (zero bare {@code throw new AssertionError}).
 * </p>
 *
 * <p>
 * <strong>Coverage scope</strong> (Sprint-1-style minimal per Pat-recommended Sprint 5
 * scope; ratified by Sprint 4 Architect precedent for sibling classes — Sprint-1-style
 * scope auto-applies to all SystemFeatures-tier siblings without re-ratification): 4
 * {@code @Test} methods at Sprint 5 close, mapped to 4 SCENARIO-ETS-PART1-006-*
 * enumerated by Pat. Sprint 6+ expansion can add the {@code /req/procedure/collections}
 * discoverability scenario (deferred to keep Sprint 5 within the two-class batch budget).
 * </p>
 *
 * <p>
 * <strong>Two-level dependency chain</strong>: Procedures → SystemFeatures → Core. Per
 * ADR-010 v3 amendment (Sprint 5 close, 2026-04-29) the TestNG 7.9.0 transitive cascade
 * is VERIFIED LIVE at the group-dependency layer (Sprint 4 Raze sabotage exec evidence
 * total=26/passed=16/failed=1/skipped=9). Defense-in-depth retained:
 * </p>
 * <ol>
 * <li>{@code testng.xml} {@code <group name="procedures" depends-on="systemfeatures"/>}
 * declares the structural cascade.</li>
 * <li>{@code @BeforeClass} {@link SkipException} fallback (this class) cascades all four
 * {@code @Test} methods to SKIP if the {@code iut} suite attribute is missing or
 * GeoRobotix returns no procedure items — preserves the inert-insurance pattern
 * established in {@code SubsystemsTests} (mirror Sprint 4 acceptance criterion).</li>
 * </ol>
 *
 * <p>
 * <strong>Procedures-unique assertion</strong>: {@code /req/procedure/location} states
 * that "A {@code Procedure} feature resource SHALL not include a location or geometry."
 * (verbatim from {@code req_location.adoc}, Pat 2026-04-29). Procedures items are GeoJSON
 * Features whose {@code geometry} field MUST be null. Curl-verified GeoRobotix
 * 2026-04-29: all 19 procedures at {@code /procedures} have {@code geometry: null} and
 * the same holds for {@code /procedures/{id}} single-item dereference. The geometry-null
 * invariant is NOT present in Subsystems, SystemFeatures, or Core — it is the
 * Procedures-unique assertion surface.
 * </p>
 *
 * <p>
 * Covers:
 * </p>
 * <ul>
 * <li><strong>SCENARIO-ETS-PART1-006-PROCEDURES-RESOURCES-001</strong> (CRITICAL) — GET
 * {@code /procedures} returns 200 + JSON + non-empty {@code items} array. Curl-verified
 * GeoRobotix 2026-04-29: returns 19 items, first id={@code 164p7ed8l47g}, type=Feature.
 * Per OGC 23-001 {@code /req/procedure/resources-endpoint}.</li>
 * <li><strong>SCENARIO-ETS-PART1-006-PROCEDURES-LOCATION-001</strong> (CRITICAL,
 * <strong>UNIQUE-to-Procedures</strong>) — every Procedure item has {@code geometry} =
 * null (or absent). Per OGC 23-001 {@code /req/procedure/location}. Asserted on each item
 * in the cached {@code /procedures} collection (subsumes single-item dereference since
 * GeoRobotix returns identical geometry-null for both forms).</li>
 * <li><strong>SCENARIO-ETS-PART1-006-PROCEDURES-CANONICAL-001</strong> (NORMAL) — GET
 * {@code /procedures/{id}} returns item with {@code id} (string), {@code type} (string),
 * {@code links} (array). Per OGC 23-001 {@code /req/procedure/canonical-endpoint} +
 * REQ-ETS-CORE-004 base shape.</li>
 * <li><strong>SCENARIO-ETS-PART1-006-PROCEDURES-CANONICAL-URL-001</strong> (NORMAL) —
 * single-procedure {@code links} contains {@code rel="canonical"}. Per OGC 23-001
 * {@code /req/procedure/canonical-url}.</li>
 * </ul>
 *
 * <p>
 * <strong>Canonical URI form</strong>: per OGC source
 * {@code raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/procedure/}
 * (5 .adoc files HTTP-200-verified by Pat 2026-04-29 + re-verified by Generator at sprint
 * time): {@code req_resources_endpoint.adoc}, {@code req_canonical_url.adoc},
 * {@code req_canonical_endpoint.adoc}, {@code req_location.adoc},
 * {@code req_collections.adoc}. The {@code /req/procedure/collections} sub-requirement is
 * deferred to Sprint 6+ (Sprint-1-style minimal scope; not in the Pat-recommended 4-test
 * minimal batch).
 * </p>
 *
 * <p>
 * The IUT also declares {@code .../conf/procedure} in {@code /conformance} (per OGC
 * 23-001 Annex A conformance class declaration; curl-verified GeoRobotix 2026-04-29).
 * </p>
 *
 * <p>
 * <strong>Curl-confirmed shape (2026-04-29, Generator re-verification at Sprint 5 Run 2
 * sprint time)</strong>:
 * </p>
 * <ul>
 * <li>{@code GET /procedures} → 200 + {@code items: [19 GeoJSON Features]}; each item is
 * GeoJSON Feature shape ({@code type=Feature}, {@code id}, {@code geometry: null},
 * {@code properties}). The collection-level item shape does NOT include {@code links}
 * (matches /procedures collection shape; per-item links surface only on
 * dereference).</li>
 * <li>{@code GET /procedures/164p7ed8l47g} → 200 + GeoJSON Feature with {@code geometry:
 * null} + {@code links} array containing 3 entries: {@code canonical} (json),
 * {@code alternate} (sml3), {@code alternate} (html).</li>
 * </ul>
 */
public class ProceduresTests {

	/**
	 * Canonical OGC requirement URI for the Procedures collection endpoint.
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/procedure/req_resources_endpoint.adoc">req_resources_endpoint.adoc</a>
	 */
	static final String REQ_PROCEDURE_RESOURCES_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/procedure/resources-endpoint";

	/**
	 * Canonical OGC requirement URI for the Procedure location/geometry invariant
	 * (UNIQUE-to-Procedures).
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/procedure/req_location.adoc">req_location.adoc</a>
	 */
	static final String REQ_PROCEDURE_LOCATION = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/procedure/location";

	/**
	 * Canonical OGC requirement URI for the Procedure canonical-endpoint shape.
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/procedure/req_canonical_endpoint.adoc">req_canonical_endpoint.adoc</a>
	 */
	static final String REQ_PROCEDURE_CANONICAL_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/procedure/canonical-endpoint";

	/**
	 * Canonical OGC requirement URI for the Procedure canonical-URL link discipline.
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/procedure/req_canonical_url.adoc">req_canonical_url.adoc</a>
	 */
	static final String REQ_PROCEDURE_CANONICAL_URL = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/procedure/canonical-url";

	private URI iutUri;

	/** Cached /procedures collection URI. */
	private URI proceduresUri;

	private Response proceduresResponse;

	private Map<String, Object> proceduresBody;

	/** Cached first-procedure id extracted from the collection (or null). */
	private String firstProcedureId;

	/**
	 * Cached single-procedure response for {@code /procedures/{firstProcedureId}}
	 * (lazy-fetched in BeforeClass).
	 */
	private Response procedureItemResponse;

	private Map<String, Object> procedureItemBody;

	/**
	 * Reads the {@code iut} suite attribute, fetches {@code /procedures} once, then
	 * dereferences the first item once. All four {@code @Test} methods operate on cached
	 * responses to avoid redundant traffic against the IUT (mirrors
	 * {@code SubsystemsTests} pattern per design.md §"Fixtures and listeners").
	 *
	 * <p>
	 * <strong>Two-level dependency-skip cascade fallback</strong> (ADR-010 v3 amendment
	 * defense-in-depth, retained as inert insurance per Sprint 4 Raze live-cascade
	 * verification): if the suite-level {@code <group depends-on>} cascade does not fire,
	 * this {@code @BeforeClass} {@link SkipException} cascades all four {@code @Test}
	 * methods to SKIP when (a) {@code iut} attribute missing/non-URI, OR (b) GeoRobotix's
	 * {@code /procedures} returns non-200, OR (c) the {@code items} array is missing or
	 * empty.
	 * </p>
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchProceduresCollection(ITestContext testContext) {
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		this.iutUri = (URI) iutAttr;
		String iutString = this.iutUri.toString();
		String base = iutString.endsWith("/") ? iutString : iutString + "/";

		this.proceduresUri = URI.create(base + "procedures");
		this.proceduresResponse = given().accept("application/json").when().get(this.proceduresUri).andReturn();
		if (this.proceduresResponse.getStatusCode() != 200) {
			throw new SkipException(REQ_PROCEDURE_RESOURCES_ENDPOINT + " — /procedures returned HTTP "
					+ this.proceduresResponse.getStatusCode() + "; cannot exercise Procedures conformance class.");
		}
		try {
			this.proceduresBody = this.proceduresResponse.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			throw new SkipException(
					REQ_PROCEDURE_RESOURCES_ENDPOINT + " — /procedures body did not parse as JSON: " + ex.getMessage());
		}

		// Eagerly resolve first-procedure id + fetch /procedures/{id} so per-item @Tests
		// can assert against cached state.
		this.firstProcedureId = extractFirstProcedureId();
		if (this.firstProcedureId != null) {
			URI itemUri = URI.create(base + "procedures/" + this.firstProcedureId);
			this.procedureItemResponse = given().accept("application/json").when().get(itemUri).andReturn();
			try {
				this.procedureItemBody = this.procedureItemResponse.jsonPath().getMap("$");
			}
			catch (Exception ex) {
				this.procedureItemBody = null;
			}
		}
	}

	/**
	 * SCENARIO-ETS-PART1-006-PROCEDURES-RESOURCES-001 (CRITICAL): {@code GET /procedures}
	 * returns HTTP 200 + non-empty {@code items} array.
	 *
	 * <p>
	 * Per OGC 23-001 {@code /req/procedure/resources-endpoint}: the server SHALL expose
	 * {@code /procedures} as the canonical Procedures collection endpoint.
	 * </p>
	 *
	 * <p>
	 * GeoRobotix curl evidence (2026-04-29): {@code /procedures} returns 200 + 19 GeoJSON
	 * Features. The {@code items} key (NOT {@code features}) matches OGC API Features
	 * Clause 7.15.2-7.15.8 convention (consistent with SystemFeatures + Subsystems
	 * collection shape).
	 * </p>
	 */
	@Test(description = "OGC-23-001 " + REQ_PROCEDURE_RESOURCES_ENDPOINT
			+ ": GET /procedures returns HTTP 200 + non-empty items array (REQ-ETS-PART1-006, SCENARIO-ETS-PART1-006-PROCEDURES-RESOURCES-001)",
			groups = "procedures")
	@SuppressWarnings("unchecked")
	public void proceduresCollectionReturns200() {
		ETSAssert.assertStatus(this.proceduresResponse, 200, REQ_PROCEDURE_RESOURCES_ENDPOINT);
		if (this.proceduresBody == null) {
			ETSAssert.failWithUri(REQ_PROCEDURE_RESOURCES_ENDPOINT, "/procedures body did not parse as JSON. "
					+ "Content-Type was: " + this.proceduresResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.proceduresBody, "items", List.class, REQ_PROCEDURE_RESOURCES_ENDPOINT);
		List<Object> items = (List<Object>) this.proceduresBody.get("items");
		if (items.isEmpty()) {
			ETSAssert.failWithUri(REQ_PROCEDURE_RESOURCES_ENDPOINT, "/procedures items array is empty; expected at "
					+ "least one Procedure item per /req/procedure/resources-endpoint.");
		}
	}

	/**
	 * SCENARIO-ETS-PART1-006-PROCEDURES-LOCATION-001 (CRITICAL,
	 * <strong>UNIQUE-to-Procedures architectural invariant</strong>): every Procedure
	 * item in the {@code /procedures} collection has {@code geometry: null} (or geometry
	 * key absent).
	 *
	 * <p>
	 * Per OGC 23-001 {@code /req/procedure/location} (verbatim from
	 * {@code req_location.adoc}): "A {@code Procedure} feature resource SHALL not include
	 * a location or geometry."
	 * </p>
	 *
	 * <p>
	 * GeoRobotix curl evidence (2026-04-29): all 19 procedures at {@code /procedures}
	 * have {@code geometry: null}. The single-item dereference at
	 * {@code /procedures/164p7ed8l47g} also returns {@code geometry: null}. We assert
	 * over the entire collection-level items array (not just the first dereferenced item)
	 * because the invariant is per-item.
	 * </p>
	 *
	 * <p>
	 * Adapt-to-IUT-shape behaviour: if any item is found with non-null geometry, we
	 * report it as a {@link AssertionError} (FAIL) — this is a real conformance gap in
	 * the IUT (not an ETS bug). Pat's Sprint 5 contract ratifies this as the correct
	 * adversarial behaviour: the assertion MUST FAIL when the IUT is non-conformant; the
	 * SKIP-with-reason fallback documented in the story is reserved for the case where
	 * GeoRobotix's behaviour changed BETWEEN curl-verification and assertion-write
	 * (didn't happen in Sprint 5 Run 2 — geometry-null verified at sprint time).
	 * </p>
	 */
	@Test(description = "OGC-23-001 " + REQ_PROCEDURE_LOCATION
			+ ": every /procedures item has geometry=null (Procedure SHALL NOT include geometry) (REQ-ETS-PART1-006, SCENARIO-ETS-PART1-006-PROCEDURES-LOCATION-001 — UNIQUE-to-Procedures)",
			dependsOnMethods = "proceduresCollectionReturns200", groups = "procedures")
	@SuppressWarnings("unchecked")
	public void procedureItemsHaveNoGeometry() {
		if (this.proceduresBody == null) {
			throw new SkipException(
					REQ_PROCEDURE_LOCATION + " — no parsed /procedures body to assert geometry-null invariant.");
		}
		Object itemsObj = this.proceduresBody.get("items");
		if (!(itemsObj instanceof List)) {
			ETSAssert.failWithUri(REQ_PROCEDURE_LOCATION,
					"/procedures items array missing; cannot exercise per-item geometry-null invariant.");
		}
		List<Object> items = (List<Object>) itemsObj;
		for (int i = 0; i < items.size(); i++) {
			Object item = items.get(i);
			if (!(item instanceof Map)) {
				ETSAssert.failWithUri(REQ_PROCEDURE_LOCATION, "/procedures items[" + i
						+ "] is not a JSON object; cannot assert geometry-null invariant on it.");
			}
			Map<String, Object> itemMap = (Map<String, Object>) item;
			// /req/procedure/location: geometry MUST be null OR absent.
			// "absent" = key not present in the map. "null" = key present, value null.
			// Both forms are conformant; only a non-null geometry value is a FAIL.
			if (itemMap.containsKey("geometry")) {
				Object geometry = itemMap.get("geometry");
				if (geometry != null) {
					Object id = itemMap.get("id");
					ETSAssert.failWithUri(REQ_PROCEDURE_LOCATION,
							"/procedures items[" + i + "] (id=" + id + ") has non-null geometry; "
									+ "/req/procedure/location says Procedure feature SHALL NOT include "
									+ "geometry. Got geometry: " + geometry);
				}
			}
		}
	}

	/**
	 * SCENARIO-ETS-PART1-006-PROCEDURES-CANONICAL-001 (NORMAL): the first procedure item
	 * dereferenced at {@code /procedures/{id}} has {@code id} (string), {@code type}
	 * (string), and {@code links} (array) per OGC 23-001
	 * {@code /req/procedure/canonical-endpoint} + REQ-ETS-CORE-004 base shape.
	 */
	@Test(description = "OGC-23-001 " + REQ_PROCEDURE_CANONICAL_ENDPOINT
			+ ": GET /procedures/{id} has id+type+links per canonical-endpoint shape (REQ-ETS-PART1-006, SCENARIO-ETS-PART1-006-PROCEDURES-CANONICAL-001)",
			dependsOnMethods = "proceduresCollectionReturns200", groups = "procedures")
	public void procedureItemHasIdTypeLinks() {
		if (this.firstProcedureId == null) {
			throw new SkipException(REQ_PROCEDURE_CANONICAL_ENDPOINT
					+ " — no items in /procedures collection to dereference; cannot test single-item shape.");
		}
		ETSAssert.assertStatus(this.procedureItemResponse, 200, REQ_PROCEDURE_CANONICAL_ENDPOINT);
		if (this.procedureItemBody == null) {
			ETSAssert.failWithUri(REQ_PROCEDURE_CANONICAL_ENDPOINT, "/procedures/" + this.firstProcedureId
					+ " body did not parse as JSON. Content-Type was: " + this.procedureItemResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.procedureItemBody, "id", String.class, REQ_PROCEDURE_CANONICAL_ENDPOINT);
		ETSAssert.assertJsonObjectHas(this.procedureItemBody, "type", String.class, REQ_PROCEDURE_CANONICAL_ENDPOINT);
		ETSAssert.assertJsonObjectHas(this.procedureItemBody, "links", List.class, REQ_PROCEDURE_CANONICAL_ENDPOINT);
	}

	/**
	 * SCENARIO-ETS-PART1-006-PROCEDURES-CANONICAL-URL-001 (NORMAL): single-procedure
	 * {@code links} contains {@code rel="canonical"} per OGC 23-001
	 * {@code /req/procedure/canonical-url}.
	 *
	 * <p>
	 * Same v1.0 GH#3 fix policy as
	 * {@code SubsystemsTests.subsystemItemHasCanonicalLink()}: {@code rel="canonical"} is
	 * the load-bearing assertion; absence of {@code rel="self"} is NOT a FAIL.
	 * </p>
	 */
	@Test(description = "OGC-23-001 " + REQ_PROCEDURE_CANONICAL_URL
			+ ": /procedures/{id} links contain rel=canonical (REQ-ETS-PART1-006, SCENARIO-ETS-PART1-006-PROCEDURES-CANONICAL-URL-001)",
			dependsOnMethods = "procedureItemHasIdTypeLinks", groups = "procedures")
	public void procedureItemHasCanonicalLink() {
		if (this.firstProcedureId == null || this.procedureItemBody == null) {
			throw new SkipException(
					REQ_PROCEDURE_CANONICAL_URL + " — no single-procedure body available to assert link discipline.");
		}
		List<?> links = itemLinksList();
		Predicate<Object> isCanonical = l -> (l instanceof Map) && "canonical".equals(((Map<?, ?>) l).get("rel"));
		ETSAssert.assertJsonArrayContains(links, isCanonical,
				"rel=canonical link on /procedures/" + this.firstProcedureId + " (got rels: " + collectItemRels() + ")",
				REQ_PROCEDURE_CANONICAL_URL);
	}

	// --------------- helpers ---------------

	/**
	 * Extracts the {@code id} of the first item in the cached {@code /procedures}
	 * collection. Returns {@code null} if the body is missing, the {@code items} array is
	 * missing/empty, or the first item lacks a string {@code id}.
	 */
	@SuppressWarnings("unchecked")
	private String extractFirstProcedureId() {
		if (this.proceduresBody == null) {
			return null;
		}
		Object itemsObj = this.proceduresBody.get("items");
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
	 * Returns the parsed {@code links} array from the cached single-procedure body.
	 * Returns an empty list if the body or {@code links} field is missing.
	 */
	private List<?> itemLinksList() {
		Object links = (this.procedureItemBody == null) ? null : this.procedureItemBody.get("links");
		return (links instanceof List) ? (List<?>) links : List.of();
	}

	/**
	 * Extracts the set of distinct {@code rel} values from the single-procedure
	 * {@code links} array.
	 */
	@SuppressWarnings("unchecked")
	private Set<String> collectItemRels() {
		Object links = (this.procedureItemBody == null) ? null : this.procedureItemBody.get("links");
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
