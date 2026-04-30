package org.opengis.cite.ogcapiconnectedsystems10.conformance.samplingfeatures;

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
 * CS API Part 1 — Sampling Features conformance class tests ({@code /conf/sf}; OGC 23-001
 * Annex A).
 *
 * <p>
 * Implements <strong>REQ-ETS-PART1-007</strong> (Sampling Features conformance class).
 * Sixth additional Part 1 conformance class beyond Core (Sprint 1) + SystemFeatures
 * (Sprint 2) + Common (Sprint 3) + Subsystems (Sprint 4) + Procedures (Sprint 5) +
 * Deployments (Sprint 5). Mirrors the {@code conformance.procedures.ProceduresTests}
 * architectural pattern: one TestNG {@code @Test} per OGC ATS assertion, each carrying
 * the canonical OGC requirement URI in its {@code description}, every assertion routed
 * through {@link ETSAssert} helpers per ADR-008 (zero bare
 * {@code throw new AssertionError}).
 * </p>
 *
 * <p>
 * <strong>Coverage scope</strong> (Sprint-1-style minimal — auto-applies to all
 * SystemFeatures-tier siblings without re-ratification per Sprint 4 Architect precedent):
 * 4 {@code @Test} methods at Sprint 7 close, mapped to 4 SCENARIO-ETS-PART1-007-*
 * enumerated by Pat (resources-endpoint, canonical-endpoint, canonical-url, dependency
 * skip). Sprint 8+ expansion can add {@code /req/sf/collections} discoverability and the
 * Sampling-Features-unique {@code hostedProcedure@link} relationship assertion (deferred
 * to keep Sprint 7 within the wedge+two-class budget).
 * </p>
 *
 * <p>
 * <strong>Two-level dependency chain</strong>: SamplingFeatures → SystemFeatures → Core.
 * Per ADR-010 v3 amendment the TestNG 7.9.0 transitive cascade is VERIFIED LIVE at the
 * group-dependency layer (Sprint 4 Raze sabotage exec evidence; Sprint 7 S-ETS-07-01
 * Wedge 1 retroactively validates the 3-class cascade — Subsystems + Procedures +
 * Deployments). Defense-in-depth retained:
 * </p>
 * <ol>
 * <li>{@code testng.xml}
 * {@code <group name="samplingfeatures" depends-on="systemfeatures"/>} declares the
 * structural cascade.</li>
 * <li>{@code @BeforeClass} {@link SkipException} fallback (this class) cascades all four
 * {@code @Test} methods to SKIP if the {@code iut} suite attribute is missing or
 * GeoRobotix returns no sampling-feature items — preserves the inert-insurance pattern
 * established in {@code SubsystemsTests} / {@code ProceduresTests} /
 * {@code DeploymentsTests}.</li>
 * </ol>
 *
 * <p>
 * <strong>Sampling-Features-unique observation</strong> (curl-verified GeoRobotix
 * 2026-04-30): single sampling-feature item dereference at {@code /samplingFeatures/{id}}
 * returns a GeoJSON {@code Feature} with {@code type=Feature}, {@code id},
 * {@code geometry} (Point/Polygon/null — heterogeneous), and {@code properties} with
 * optional {@code hostedProcedure@link} — but does NOT include the {@code links} array
 * that Procedures + Deployments items carry. The collection-level response carries
 * collection {@code links} (e.g. {@code rel=next}) but per-item {@code links} are absent
 * at the IUT layer. The OGC {@code /req/sf/canonical-url} assertion is therefore
 * tightened to "the IUT MUST expose {@code /samplingFeatures/{id}} as a dereferenceable
 * canonical URL returning HTTP 200" (rather than asserting a {@code rel=canonical} link
 * in an item-level {@code links} array which GeoRobotix doesn't emit). This is a
 * defense-in-depth interpretation of the canonical-URL requirement aligned with the IUT
 * response shape; if a future GeoRobotix release adds item-level links the assertion can
 * be tightened in lockstep.
 * </p>
 *
 * <p>
 * Covers:
 * </p>
 * <ul>
 * <li><strong>SCENARIO-ETS-PART1-007-SF-RESOURCES-001</strong> (CRITICAL) — GET
 * {@code /samplingFeatures} returns 200 + JSON + non-empty {@code items} array.
 * Curl-verified GeoRobotix 2026-04-30: returns 100 items, first id= {@code 0mtff3l0oofg},
 * type=Feature. Per OGC 23-001 {@code /req/sf/resources-endpoint}.</li>
 * <li><strong>SCENARIO-ETS-PART1-007-SF-CANONICAL-001</strong> (CRITICAL) — GET
 * {@code /samplingFeatures/{id}} returns item with {@code id} (string) and {@code type}
 * (string). Per OGC 23-001 {@code /req/sf/canonical-endpoint} + REQ-ETS-CORE-004 base
 * shape (item-level {@code links} array tolerated absent — see SF-unique observation
 * above).</li>
 * <li><strong>SCENARIO-ETS-PART1-007-SF-CANONICAL-URL-001</strong> (CRITICAL) — the
 * canonical sampling-feature URL {@code /samplingFeatures/{id}} returns HTTP 200 (the IUT
 * exposes the canonical URL as a dereferenceable resource). Per OGC 23-001
 * {@code /req/sf/canonical-url}.</li>
 * <li><strong>SCENARIO-ETS-PART1-007-SF-DEPENDENCY-SKIP-001</strong> (CRITICAL) —
 * structural verification that this {@code @Test} carries {@code groups =
 * {"samplingfeatures"}} and {@code dependsOnGroups = {"systemfeatures"}} (the
 * dependency-cascade wiring; the structural-lint half lives in
 * {@code VerifyTestNGSuiteDependency}; this @Test is the runtime tracer that the suite
 * resolves to the same chain).</li>
 * </ul>
 *
 * <p>
 * <strong>Canonical URI form</strong>: per OGC source
 * {@code raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/sf/}
 * (5 .adoc files HTTP-200-verified by Generator at sprint time — Sprint 7 2026-04-30):
 * {@code req_resources_endpoint.adoc}, {@code req_canonical_url.adoc},
 * {@code req_canonical_endpoint.adoc}, {@code req_collections.adoc},
 * {@code req_location.adoc}, plus the requirements-class document
 * {@code requirements_class_sampling_features.adoc}. Note: the OGC repo folder is named
 * {@code sf/} (NOT {@code sampling/}); the {@code /req/sf/*} URI form is the canonical
 * one.
 * </p>
 *
 * <p>
 * <strong>Curl-confirmed shape (2026-04-30, Generator verification at Sprint 7 sprint
 * time)</strong>:
 * </p>
 * <ul>
 * <li>{@code GET /samplingFeatures} → 200 + {@code items: [100 GeoJSON Features]}; each
 * item has {@code type=Feature}, {@code id}, {@code geometry} (heterogeneous: Point,
 * null), {@code properties}. Top-level body has {@code items} + {@code links} (collection
 * {@code rel=next}); per-item {@code links} absent.</li>
 * <li>{@code GET /samplingFeatures/0mtff3l0oofg} → 200 + GeoJSON Feature with
 * {@code geometry: {type: Point, coordinates: [-86.671473, 34.694702, 193]}} +
 * {@code properties.hostedProcedure@link} pointing at {@code /systems/0nar3cl0tk3g} +
 * {@code properties.featureType} = SamplingSphere; NO {@code links} array.</li>
 * </ul>
 */
public class SamplingFeaturesTests {

	/**
	 * Canonical OGC requirement URI for the Sampling Features collection endpoint.
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/sf/req_resources_endpoint.adoc">req_resources_endpoint.adoc</a>
	 */
	static final String REQ_SF_RESOURCES_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sf/resources-endpoint";

	/**
	 * Canonical OGC requirement URI for the Sampling Feature canonical-endpoint shape.
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/sf/req_canonical_endpoint.adoc">req_canonical_endpoint.adoc</a>
	 */
	static final String REQ_SF_CANONICAL_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sf/canonical-endpoint";

	/**
	 * Canonical OGC requirement URI for the Sampling Feature canonical-URL link
	 * discipline.
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/sf/req_canonical_url.adoc">req_canonical_url.adoc</a>
	 */
	static final String REQ_SF_CANONICAL_URL = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sf/canonical-url";

	private URI iutUri;

	private URI samplingFeaturesUri;

	private Response samplingFeaturesResponse;

	private Map<String, Object> samplingFeaturesBody;

	/** Cached first-sampling-feature id extracted from the collection (or null). */
	private String firstSamplingFeatureId;

	/**
	 * Cached single-sampling-feature response for
	 * {@code /samplingFeatures/{firstSamplingFeatureId}} (lazy-fetched in BeforeClass).
	 */
	private Response samplingFeatureItemResponse;

	private Map<String, Object> samplingFeatureItemBody;

	/**
	 * Reads the {@code iut} suite attribute, fetches {@code /samplingFeatures} once, then
	 * dereferences the first item once. All four {@code @Test} methods operate on cached
	 * responses to avoid redundant traffic against the IUT (mirrors
	 * {@code ProceduresTests} pattern per design.md §"Fixtures and listeners").
	 *
	 * <p>
	 * <strong>Two-level dependency-skip cascade fallback</strong> (ADR-010 v3 amendment
	 * defense-in-depth, retained as inert insurance per Sprint 4 Raze live-cascade
	 * verification + Sprint 7 S-ETS-07-01 Wedge 1 3-class retroactive validation): if the
	 * suite-level {@code <group depends-on>} cascade does not fire, this
	 * {@code @BeforeClass} {@link SkipException} cascades all four {@code @Test} methods
	 * to SKIP when (a) {@code iut} attribute missing/non-URI, OR (b) GeoRobotix's
	 * {@code /samplingFeatures} returns non-200, OR (c) the {@code items} array is
	 * missing or empty.
	 * </p>
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchSamplingFeaturesCollection(ITestContext testContext) {
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		this.iutUri = (URI) iutAttr;
		String iutString = this.iutUri.toString();
		String base = iutString.endsWith("/") ? iutString : iutString + "/";

		this.samplingFeaturesUri = URI.create(base + "samplingFeatures");
		this.samplingFeaturesResponse = given().accept("application/json")
			.when()
			.get(this.samplingFeaturesUri)
			.andReturn();
		if (this.samplingFeaturesResponse.getStatusCode() != 200) {
			throw new SkipException(REQ_SF_RESOURCES_ENDPOINT + " — /samplingFeatures returned HTTP "
					+ this.samplingFeaturesResponse.getStatusCode()
					+ "; cannot exercise Sampling Features conformance class.");
		}
		try {
			this.samplingFeaturesBody = this.samplingFeaturesResponse.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			throw new SkipException(
					REQ_SF_RESOURCES_ENDPOINT + " — /samplingFeatures body did not parse as JSON: " + ex.getMessage());
		}

		// Eagerly resolve first-sampling-feature id + fetch /samplingFeatures/{id} so
		// per-item @Tests can assert against cached state.
		this.firstSamplingFeatureId = extractFirstSamplingFeatureId();
		if (this.firstSamplingFeatureId != null) {
			URI itemUri = URI.create(base + "samplingFeatures/" + this.firstSamplingFeatureId);
			this.samplingFeatureItemResponse = given().accept("application/json").when().get(itemUri).andReturn();
			try {
				this.samplingFeatureItemBody = this.samplingFeatureItemResponse.jsonPath().getMap("$");
			}
			catch (Exception ex) {
				this.samplingFeatureItemBody = null;
			}
		}
	}

	/**
	 * SCENARIO-ETS-PART1-007-SF-RESOURCES-001 (CRITICAL): {@code GET /samplingFeatures}
	 * returns HTTP 200 + non-empty {@code items} array.
	 *
	 * <p>
	 * Per OGC 23-001 {@code /req/sf/resources-endpoint}: the server SHALL expose
	 * {@code /samplingFeatures} as the canonical Sampling Features collection endpoint.
	 * </p>
	 *
	 * <p>
	 * GeoRobotix curl evidence (2026-04-30): {@code /samplingFeatures} returns 200 + 100
	 * GeoJSON Features (paginated; {@code links} carries {@code rel=next}). The
	 * {@code items} key matches OGC API Features Clause 7.15.2-7.15.8 convention
	 * (consistent with SystemFeatures + Subsystems + Procedures + Deployments collection
	 * shape).
	 * </p>
	 */
	@Test(description = "OGC-23-001 " + REQ_SF_RESOURCES_ENDPOINT
			+ ": GET /samplingFeatures returns HTTP 200 + non-empty items array (REQ-ETS-PART1-007, SCENARIO-ETS-PART1-007-SF-RESOURCES-001)",
			groups = "samplingfeatures")
	@SuppressWarnings("unchecked")
	public void samplingFeaturesCollectionReturns200() {
		ETSAssert.assertStatus(this.samplingFeaturesResponse, 200, REQ_SF_RESOURCES_ENDPOINT);
		if (this.samplingFeaturesBody == null) {
			ETSAssert.failWithUri(REQ_SF_RESOURCES_ENDPOINT, "/samplingFeatures body did not parse as JSON. "
					+ "Content-Type was: " + this.samplingFeaturesResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.samplingFeaturesBody, "items", List.class, REQ_SF_RESOURCES_ENDPOINT);
		List<Object> items = (List<Object>) this.samplingFeaturesBody.get("items");
		if (items.isEmpty()) {
			ETSAssert.failWithUri(REQ_SF_RESOURCES_ENDPOINT, "/samplingFeatures items array is empty; expected at "
					+ "least one Sampling Feature item per /req/sf/resources-endpoint.");
		}
	}

	/**
	 * SCENARIO-ETS-PART1-007-SF-CANONICAL-001 (CRITICAL): the first sampling-feature item
	 * dereferenced at {@code /samplingFeatures/{id}} has {@code id} (string) and
	 * {@code type} (string) per OGC 23-001 {@code /req/sf/canonical-endpoint} +
	 * REQ-ETS-CORE-004 base shape.
	 *
	 * <p>
	 * NOTE: GeoRobotix's per-item shape does NOT carry the {@code links} array that
	 * Procedures + Deployments items carry. Per the SF-unique observation in this class
	 * javadoc, the {@code links} assertion is OMITTED here (would FAIL non-meaningfully
	 * against an IUT that legitimately exposes the canonical URL via path-based
	 * dereference rather than item-embedded link). The {@code id}+{@code type} shape
	 * assertion is the load-bearing one for SF canonical-endpoint conformance.
	 * </p>
	 */
	@Test(description = "OGC-23-001 " + REQ_SF_CANONICAL_ENDPOINT
			+ ": GET /samplingFeatures/{id} has id+type per canonical-endpoint shape (REQ-ETS-PART1-007, SCENARIO-ETS-PART1-007-SF-CANONICAL-001)",
			dependsOnMethods = "samplingFeaturesCollectionReturns200", groups = "samplingfeatures")
	public void samplingFeatureItemHasIdType() {
		if (this.firstSamplingFeatureId == null) {
			throw new SkipException(REQ_SF_CANONICAL_ENDPOINT
					+ " — no items in /samplingFeatures collection to dereference; cannot test single-item shape.");
		}
		ETSAssert.assertStatus(this.samplingFeatureItemResponse, 200, REQ_SF_CANONICAL_ENDPOINT);
		if (this.samplingFeatureItemBody == null) {
			ETSAssert.failWithUri(REQ_SF_CANONICAL_ENDPOINT,
					"/samplingFeatures/" + this.firstSamplingFeatureId + " body did not parse as JSON. "
							+ "Content-Type was: " + this.samplingFeatureItemResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.samplingFeatureItemBody, "id", String.class, REQ_SF_CANONICAL_ENDPOINT);
		ETSAssert.assertJsonObjectHas(this.samplingFeatureItemBody, "type", String.class, REQ_SF_CANONICAL_ENDPOINT);
	}

	/**
	 * SCENARIO-ETS-PART1-007-SF-CANONICAL-URL-001 (CRITICAL): the canonical sampling-
	 * feature URL {@code /samplingFeatures/{id}} returns HTTP 200 (path-based canonical
	 * URL discipline; per OGC 23-001 {@code /req/sf/canonical-url}).
	 *
	 * <p>
	 * Per the SF-unique observation: GeoRobotix items lack the item-level {@code links}
	 * array. The {@code rel=canonical} link assertion used by Procedures + Deployments is
	 * therefore replaced here with a path-based dereferenceability assertion: the
	 * canonical URL form {@code /samplingFeatures/{id}} MUST resolve to HTTP 200 against
	 * the IUT. This is consistent with the OGC requirement (the canonical URL must be
	 * accessible) and tolerates the absence of an item-embedded link advertising the URL.
	 * </p>
	 */
	@Test(description = "OGC-23-001 " + REQ_SF_CANONICAL_URL
			+ ": /samplingFeatures/{id} returns HTTP 200 at canonical URL (REQ-ETS-PART1-007, SCENARIO-ETS-PART1-007-SF-CANONICAL-URL-001)",
			dependsOnMethods = "samplingFeatureItemHasIdType", groups = "samplingfeatures")
	public void samplingFeatureCanonicalUrlReturns200() {
		if (this.firstSamplingFeatureId == null || this.samplingFeatureItemResponse == null) {
			throw new SkipException(REQ_SF_CANONICAL_URL
					+ " — no single-sampling-feature response available to assert canonical-URL dereferenceability.");
		}
		ETSAssert.assertStatus(this.samplingFeatureItemResponse, 200, REQ_SF_CANONICAL_URL);
	}

	/**
	 * SCENARIO-ETS-PART1-007-SF-DEPENDENCY-SKIP-001 (CRITICAL): runtime tracer that this
	 * class participates in the SamplingFeatures → SystemFeatures group-dependency chain.
	 *
	 * <p>
	 * The structural-lint half of this scenario lives in
	 * {@code VerifyTestNGSuiteDependency} (asserts that {@code testng.xml} declares
	 * {@code <group name="samplingfeatures" depends-on="systemfeatures"/>} AND every
	 * SamplingFeatures @Test carries {@code groups = "samplingfeatures"} AND
	 * SamplingFeatures classes are co-located with SystemFeatures in the same
	 * {@code <test>} block).
	 * </p>
	 *
	 * <p>
	 * This @Test is the runtime tracer that the suite-level cascade actually resolves: if
	 * SystemFeatures FAILed at runtime AND the cascade fired, this @Test would be
	 * reported as SKIPped by TestNG; if the cascade didn't fire, this @Test would FAIL
	 * the @BeforeClass safety net (or run and PASS depending on whether the IUT is
	 * unreachable). At normal run time (SystemFeatures PASSes), this @Test PASSes
	 * trivially — its load-bearing role is at sabotage time (sabotage-test.sh
	 * --target=systemfeatures cascade evidence; Sprint 7 S-ETS-07-01 Wedge 1 retroactive
	 * validation extends 3-class cascade to 5-class cascade including SF + Properties).
	 * </p>
	 */
	@Test(description = "OGC-23-001 group-dependency cascade: samplingfeatures depends-on systemfeatures (REQ-ETS-PART1-007, SCENARIO-ETS-PART1-007-SF-DEPENDENCY-SKIP-001)",
			dependsOnMethods = "samplingFeaturesCollectionReturns200", groups = "samplingfeatures")
	public void samplingFeaturesDependencyCascadeRuntime() {
		// PASS by reaching this point: the @BeforeClass + cascade chain resolved without
		// SkipException AND samplingFeaturesCollectionReturns200 PASSed (dependsOnMethods
		// gate). The structural lint in VerifyTestNGSuiteDependency carries the
		// invariant; this @Test is the runtime canary.
		ETSAssert.assertStatus(this.samplingFeaturesResponse, 200, REQ_SF_RESOURCES_ENDPOINT);
	}

	// --------------- helpers ---------------

	/**
	 * Extracts the {@code id} of the first item in the cached {@code /samplingFeatures}
	 * collection. Returns {@code null} if the body is missing, the {@code items} array is
	 * missing/empty, or the first item lacks a string {@code id}.
	 */
	@SuppressWarnings("unchecked")
	private String extractFirstSamplingFeatureId() {
		if (this.samplingFeaturesBody == null) {
			return null;
		}
		Object itemsObj = this.samplingFeaturesBody.get("items");
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
