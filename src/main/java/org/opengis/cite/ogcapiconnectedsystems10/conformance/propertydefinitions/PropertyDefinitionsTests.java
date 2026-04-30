package org.opengis.cite.ogcapiconnectedsystems10.conformance.propertydefinitions;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Part 1 — Property Definitions conformance class tests ({@code /conf/property};
 * OGC 23-001 Annex A).
 *
 * <p>
 * Implements <strong>REQ-ETS-PART1-008</strong> (Property Definitions conformance class).
 * Seventh additional Part 1 conformance class beyond Core + SystemFeatures + Common +
 * Subsystems + Procedures + Deployments + SamplingFeatures. Mirrors the
 * {@code conformance.samplingfeatures.SamplingFeaturesTests} architectural pattern: one
 * TestNG {@code @Test} per OGC ATS assertion, each carrying the canonical OGC requirement
 * URI in its {@code description}, every assertion routed through {@link ETSAssert}
 * helpers per ADR-008 (zero bare {@code throw new AssertionError}).
 * </p>
 *
 * <p>
 * <strong>Coverage scope</strong> (Sprint-1-style minimal — auto-applies to all
 * SystemFeatures-tier siblings without re-ratification): 4 {@code @Test} methods at
 * Sprint 7 close, mapped to 4 SCENARIO-ETS-PART1-008-* enumerated by Pat
 * (resources-endpoint, canonical-endpoint, canonical-url, dependency-skip cascade runtime
 * tracer). Sprint 8+ expansion can add the {@code /req/property/collections}
 * sub-requirement if/when the IUT exposes property collections.
 * </p>
 *
 * <p>
 * <strong>Two-level dependency chain</strong>: PropertyDefinitions → SystemFeatures →
 * Core. Per ADR-010 v3 amendment + Sprint 7 S-ETS-07-01 Wedge 1 retroactive 3-class
 * cascade live-validation, the TestNG 7.9.0 transitive cascade is VERIFIED LIVE.
 * Defense-in-depth retained:
 * </p>
 * <ol>
 * <li>{@code testng.xml}
 * {@code <group name="propertydefinitions" depends-on="systemfeatures"/>} declares the
 * structural cascade.</li>
 * <li>{@code @BeforeClass} {@link SkipException} fallback (this class) cascades all four
 * {@code @Test} methods to SKIP if (a) the {@code iut} suite attribute is missing, OR (b)
 * GeoRobotix's {@code /properties} returns non-200, OR (c) the {@code items} array is
 * missing (note: empty {@code items} is TOLERATED for property definitions per GeoRobotix
 * observation 2026-04-30 — the IUT may legitimately expose an empty collection if no
 * derived properties are currently defined; the SKIP-with-reason pattern below routes
 * per-item @Tests through SkipException when items.isEmpty()).</li>
 * </ol>
 *
 * <p>
 * <strong>Property-Definitions-unique observation</strong> (curl-verified GeoRobotix
 * 2026-04-30): {@code GET /properties} returns HTTP 200 + {@code items: []} (empty array,
 * no {@code links}). Property Definitions are "Derived properties" — observable
 * quantities defined as reusable metadata. The IUT does declare the {@code /properties}
 * endpoint (per OGC 23-001 {@code /req/property/resources-endpoint}) but does NOT
 * currently populate it. Per Pat's Sprint 7 contract MEDIUM risk
 * "PROPERTY-DEFINITIONS-RESPONSE-SHAPE" mitigation, this class adapts to the IUT shape:
 * the {@code resources-endpoint} @Test PASSes on HTTP 200 + items-array-present
 * (tolerating empty); the {@code canonical-endpoint} + {@code canonical-url} @Tests
 * SKIP-with-reason because no items exist to dereference. This is the Sprint-1-style
 * minimal scope; Sprint 8+ can re-evaluate when GeoRobotix populates the
 * {@code /properties} collection.
 * </p>
 *
 * <p>
 * Covers:
 * </p>
 * <ul>
 * <li><strong>SCENARIO-ETS-PART1-008-PROP-RESOURCES-001</strong> (CRITICAL) — GET
 * {@code /properties} returns 200 + JSON + {@code items} array (may be empty). Per OGC
 * 23-001 {@code /req/property/resources-endpoint}.</li>
 * <li><strong>SCENARIO-ETS-PART1-008-PROP-CANONICAL-001</strong> (CRITICAL) — GET
 * {@code /properties/{id}} returns item with {@code id} (string) and {@code type}
 * (string). Per OGC 23-001 {@code /req/property/canonical-endpoint}. SKIP-with-reason if
 * collection is empty.</li>
 * <li><strong>SCENARIO-ETS-PART1-008-PROP-CANONICAL-URL-001</strong> (CRITICAL) — the
 * canonical property URL {@code /properties/{id}} returns HTTP 200. Per OGC 23-001
 * {@code /req/property/canonical-url}. SKIP-with-reason if collection is empty.</li>
 * <li><strong>SCENARIO-ETS-PART1-008-PROP-DEPENDENCY-SKIP-001</strong> (CRITICAL) —
 * runtime tracer that propertydefinitions group resolves the SystemFeatures dependency
 * chain.</li>
 * </ul>
 *
 * <p>
 * <strong>Canonical URI form</strong>: per OGC source
 * {@code raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/property/}
 * (4 .adoc files HTTP-200-verified by Generator at sprint time — Sprint 7 2026-04-30):
 * {@code req_resources_endpoint.adoc}, {@code req_canonical_url.adoc},
 * {@code req_canonical_endpoint.adoc}, {@code req_collections.adoc}, plus
 * {@code requirements_class_property_definitions.adoc}.
 * </p>
 *
 * <p>
 * <strong>Curl-confirmed shape (2026-04-30, Generator verification at Sprint 7 sprint
 * time)</strong>:
 * </p>
 * <ul>
 * <li>{@code GET /properties} → 200 + {@code items: []} (empty array). Top-level body has
 * only {@code items} (no {@code links}). Per Pat's contract this is consistent with the
 * OGC requirement (the endpoint exists and returns a well-formed response); empty items
 * reflects an IUT-side state, not a conformance gap. SKIP-with-reason pattern applies to
 * per-item @Tests.</li>
 * </ul>
 */
public class PropertyDefinitionsTests {

	/**
	 * Canonical OGC requirement URI for the Property Definitions collection endpoint.
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/property/req_resources_endpoint.adoc">req_resources_endpoint.adoc</a>
	 */
	static final String REQ_PROPERTY_RESOURCES_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/property/resources-endpoint";

	/**
	 * Canonical OGC requirement URI for the Property Definition canonical-endpoint shape.
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/property/req_canonical_endpoint.adoc">req_canonical_endpoint.adoc</a>
	 */
	static final String REQ_PROPERTY_CANONICAL_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/property/canonical-endpoint";

	/**
	 * Canonical OGC requirement URI for the Property Definition canonical-URL link
	 * discipline.
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/property/req_canonical_url.adoc">req_canonical_url.adoc</a>
	 */
	static final String REQ_PROPERTY_CANONICAL_URL = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/property/canonical-url";

	private URI iutUri;

	private URI propertiesUri;

	private Response propertiesResponse;

	private Map<String, Object> propertiesBody;

	/** Cached first-property id extracted from the collection (or null if empty). */
	private String firstPropertyId;

	/**
	 * Cached single-property response for {@code /properties/{firstPropertyId}}
	 * (lazy-fetched in BeforeClass; null if collection empty).
	 */
	private Response propertyItemResponse;

	private Map<String, Object> propertyItemBody;

	/**
	 * Reads the {@code iut} suite attribute, fetches {@code /properties} once, then
	 * dereferences the first item if present. All four {@code @Test} methods operate on
	 * cached responses to avoid redundant traffic against the IUT.
	 *
	 * <p>
	 * <strong>Two-level dependency-skip cascade fallback</strong> (ADR-010 v3 amendment
	 * defense-in-depth, retained as inert insurance per Sprint 4 Raze live-cascade
	 * verification + Sprint 7 S-ETS-07-01 Wedge 1 retroactive 3-class validation): if the
	 * suite-level {@code <group depends-on>} cascade does not fire, this
	 * {@code @BeforeClass} {@link SkipException} cascades all four {@code @Test} methods
	 * to SKIP when (a) {@code iut} attribute missing/non-URI, OR (b) GeoRobotix's
	 * {@code /properties} returns non-200, OR (c) the body did not parse as JSON. NOTE:
	 * empty {@code items} is TOLERATED — the {@code resources-endpoint} @Test PASSes on
	 * 200 + items-array-present; per-item @Tests SKIP-with-reason when items empty.
	 * </p>
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchPropertiesCollection(ITestContext testContext) {
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		this.iutUri = (URI) iutAttr;
		String iutString = this.iutUri.toString();
		String base = iutString.endsWith("/") ? iutString : iutString + "/";

		this.propertiesUri = URI.create(base + "properties");
		this.propertiesResponse = given().accept("application/json").when().get(this.propertiesUri).andReturn();
		if (this.propertiesResponse.getStatusCode() != 200) {
			throw new SkipException(REQ_PROPERTY_RESOURCES_ENDPOINT + " — /properties returned HTTP "
					+ this.propertiesResponse.getStatusCode()
					+ "; cannot exercise Property Definitions conformance class.");
		}
		try {
			this.propertiesBody = this.propertiesResponse.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			throw new SkipException(
					REQ_PROPERTY_RESOURCES_ENDPOINT + " — /properties body did not parse as JSON: " + ex.getMessage());
		}

		// Eagerly resolve first-property id + fetch /properties/{id} so per-item @Tests
		// can assert against cached state. Tolerates empty items (firstPropertyId stays
		// null; per-item @Tests SKIP-with-reason).
		this.firstPropertyId = extractFirstPropertyId();
		if (this.firstPropertyId != null) {
			URI itemUri = URI.create(base + "properties/" + this.firstPropertyId);
			this.propertyItemResponse = given().accept("application/json").when().get(itemUri).andReturn();
			try {
				this.propertyItemBody = this.propertyItemResponse.jsonPath().getMap("$");
			}
			catch (Exception ex) {
				this.propertyItemBody = null;
			}
		}
	}

	/**
	 * SCENARIO-ETS-PART1-008-PROP-RESOURCES-001 (CRITICAL): {@code GET /properties}
	 * returns HTTP 200 + JSON body containing an {@code items} array (possibly empty).
	 *
	 * <p>
	 * Per OGC 23-001 {@code /req/property/resources-endpoint}: the server SHALL expose
	 * {@code /properties} as the canonical Property Definitions collection endpoint.
	 * </p>
	 *
	 * <p>
	 * GeoRobotix curl evidence (2026-04-30): {@code /properties} returns 200 +
	 * {@code items: []} (empty array). Empty items is TOLERATED — the OGC requirement is
	 * at the endpoint-existence + response-shape layer, not the items-non-empty layer
	 * (cf. {@code SubsystemsTests} which also tolerates empty collection per IUT state).
	 * The per-item @Tests SKIP-with-reason when no items exist.
	 * </p>
	 */
	@Test(description = "OGC-23-001 " + REQ_PROPERTY_RESOURCES_ENDPOINT
			+ ": GET /properties returns HTTP 200 + items array (may be empty per IUT state) (REQ-ETS-PART1-008, SCENARIO-ETS-PART1-008-PROP-RESOURCES-001)",
			groups = "propertydefinitions")
	public void propertiesCollectionReturns200() {
		ETSAssert.assertStatus(this.propertiesResponse, 200, REQ_PROPERTY_RESOURCES_ENDPOINT);
		if (this.propertiesBody == null) {
			ETSAssert.failWithUri(REQ_PROPERTY_RESOURCES_ENDPOINT, "/properties body did not parse as JSON. "
					+ "Content-Type was: " + this.propertiesResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.propertiesBody, "items", List.class, REQ_PROPERTY_RESOURCES_ENDPOINT);
		// NOTE: empty items is tolerated per /req/property/resources-endpoint
		// (endpoint-existence is the load-bearing assertion; population is IUT-state
		// dependent). Per-item @Tests SKIP-with-reason when items empty.
	}

	/**
	 * SCENARIO-ETS-PART1-008-PROP-CANONICAL-001 (CRITICAL): the first property item
	 * dereferenced at {@code /properties/{id}} has {@code id} (string) and {@code type}
	 * (string) per OGC 23-001 {@code /req/property/canonical-endpoint} + REQ-ETS-CORE-004
	 * base shape.
	 *
	 * <p>
	 * SKIP-with-reason if collection is empty (no items to dereference). Empty collection
	 * is TOLERATED per Pat's Sprint 7 MEDIUM risk PROPERTY-DEFINITIONS-RESPONSE-SHAPE
	 * mitigation (curl-verified GeoRobotix 2026-04-30 returns empty items).
	 * </p>
	 */
	@Test(description = "OGC-23-001 " + REQ_PROPERTY_CANONICAL_ENDPOINT
			+ ": GET /properties/{id} has id+type per canonical-endpoint shape (REQ-ETS-PART1-008, SCENARIO-ETS-PART1-008-PROP-CANONICAL-001)",
			dependsOnMethods = "propertiesCollectionReturns200", groups = "propertydefinitions")
	public void propertyItemHasIdType() {
		if (this.firstPropertyId == null) {
			throw new SkipException(REQ_PROPERTY_CANONICAL_ENDPOINT
					+ " — /properties items array is empty; no items to dereference. SKIP-with-reason "
					+ "(empty collection is IUT-state, not a conformance gap; per Sprint 7 contract MEDIUM risk "
					+ "PROPERTY-DEFINITIONS-RESPONSE-SHAPE mitigation).");
		}
		ETSAssert.assertStatus(this.propertyItemResponse, 200, REQ_PROPERTY_CANONICAL_ENDPOINT);
		if (this.propertyItemBody == null) {
			ETSAssert.failWithUri(REQ_PROPERTY_CANONICAL_ENDPOINT,
					"/properties/" + this.firstPropertyId + " body did not parse as JSON. " + "Content-Type was: "
							+ this.propertyItemResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.propertyItemBody, "id", String.class, REQ_PROPERTY_CANONICAL_ENDPOINT);
		ETSAssert.assertJsonObjectHas(this.propertyItemBody, "type", String.class, REQ_PROPERTY_CANONICAL_ENDPOINT);
	}

	/**
	 * SCENARIO-ETS-PART1-008-PROP-CANONICAL-URL-001 (CRITICAL): the canonical property
	 * URL {@code /properties/{id}} returns HTTP 200 (path-based canonical URL
	 * discipline). Per OGC 23-001 {@code /req/property/canonical-url}.
	 *
	 * <p>
	 * SKIP-with-reason if collection is empty (no items to dereference) — same
	 * IUT-state-tolerance pattern as the canonical-endpoint @Test above.
	 * </p>
	 */
	@Test(description = "OGC-23-001 " + REQ_PROPERTY_CANONICAL_URL
			+ ": /properties/{id} returns HTTP 200 at canonical URL (REQ-ETS-PART1-008, SCENARIO-ETS-PART1-008-PROP-CANONICAL-URL-001)",
			dependsOnMethods = "propertyItemHasIdType", groups = "propertydefinitions")
	public void propertyCanonicalUrlReturns200() {
		if (this.firstPropertyId == null || this.propertyItemResponse == null) {
			throw new SkipException(REQ_PROPERTY_CANONICAL_URL
					+ " — no single-property response available to assert canonical-URL dereferenceability "
					+ "(/properties items array empty per IUT state).");
		}
		ETSAssert.assertStatus(this.propertyItemResponse, 200, REQ_PROPERTY_CANONICAL_URL);
	}

	/**
	 * SCENARIO-ETS-PART1-008-PROP-DEPENDENCY-SKIP-001 (CRITICAL): runtime tracer that
	 * this class participates in the PropertyDefinitions → SystemFeatures
	 * group-dependency chain.
	 *
	 * <p>
	 * The structural-lint half lives in {@code VerifyTestNGSuiteDependency} (asserts that
	 * {@code testng.xml} declares
	 * {@code <group name="propertydefinitions" depends-on="systemfeatures"/>}, every
	 * PropertyDefinitions @Test carries {@code groups = "propertydefinitions"}, and
	 * PropertyDefinitions classes are co-located with SystemFeatures in the same
	 * {@code <test>} block). This @Test is the runtime canary: at sabotage time
	 * (sabotage-test.sh --target=systemfeatures) it would be reported as SKIPped by
	 * TestNG when the cascade fires; at normal run time it PASSes trivially.
	 * </p>
	 */
	@Test(description = "OGC-23-001 group-dependency cascade: propertydefinitions depends-on systemfeatures (REQ-ETS-PART1-008, SCENARIO-ETS-PART1-008-PROP-DEPENDENCY-SKIP-001)",
			dependsOnMethods = "propertiesCollectionReturns200", groups = "propertydefinitions")
	public void propertyDefinitionsDependencyCascadeRuntime() {
		// PASS by reaching this point: cascade chain resolved without SkipException AND
		// propertiesCollectionReturns200 PASSed (dependsOnMethods gate). Structural lint
		// in VerifyTestNGSuiteDependency carries the structural invariant.
		ETSAssert.assertStatus(this.propertiesResponse, 200, REQ_PROPERTY_RESOURCES_ENDPOINT);
	}

	// --------------- helpers ---------------

	/**
	 * Extracts the {@code id} of the first item in the cached {@code /properties}
	 * collection. Returns {@code null} if the body is missing, the {@code items} array is
	 * missing or empty, or the first item lacks a string {@code id}.
	 */
	@SuppressWarnings("unchecked")
	private String extractFirstPropertyId() {
		if (this.propertiesBody == null) {
			return null;
		}
		Object itemsObj = this.propertiesBody.get("items");
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

}
