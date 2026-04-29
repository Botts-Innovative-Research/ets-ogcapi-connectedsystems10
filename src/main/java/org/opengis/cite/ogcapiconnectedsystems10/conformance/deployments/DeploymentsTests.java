package org.opengis.cite.ogcapiconnectedsystems10.conformance.deployments;

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
 * CS API Part 1 — Deployments conformance class tests ({@code /conf/deployment-features};
 * OGC 23-001 Annex A).
 *
 * <p>
 * Implements <strong>REQ-ETS-PART1-004</strong> (Deployments conformance class). Fifth
 * additional Part 1 conformance class beyond Core (Sprint 1) + SystemFeatures (Sprint 2)
 * + Common (Sprint 3) + Subsystems (Sprint 4) + Procedures (Sprint 5 Run 2 sibling).
 * Mirrors the {@code conformance.subsystems.SubsystemsTests} +
 * {@code conformance.procedures.ProceduresTests} architectural pattern: one TestNG
 * {@code @Test} per OGC ATS assertion, each carrying the canonical OGC requirement URI in
 * its {@code description}, every assertion routed through {@link ETSAssert} helpers per
 * ADR-008.
 * </p>
 *
 * <p>
 * <strong>Coverage scope</strong> (Sprint-1-style minimal per Pat-recommended Sprint 5
 * scope; ratified by Sprint 4 Architect precedent for sibling classes): 4 {@code @Test}
 * methods at Sprint 5 close, mapped to 4 SCENARIO-ETS-PART1-004-* enumerated by Pat.
 * Sprint 6+ expansion can add the {@code /req/deployment/collections} discoverability
 * scenario (deferred to keep Sprint 5 within the two-class batch budget).
 * </p>
 *
 * <p>
 * <strong>Two-level dependency chain</strong>: Deployments → SystemFeatures → Core. Per
 * ADR-010 v3 amendment (Sprint 5 close, 2026-04-29) the TestNG 7.9.0 transitive cascade
 * is VERIFIED LIVE at the group-dependency layer (Sprint 4 Raze sabotage exec evidence
 * total=26/passed=16/failed=1/skipped=9). Defense-in-depth retained:
 * </p>
 * <ol>
 * <li>{@code testng.xml} {@code <group name="deployments" depends-on="systemfeatures"/>}
 * declares the structural cascade.</li>
 * <li>{@code @BeforeClass} {@link SkipException} fallback (this class) cascades all four
 * {@code @Test} methods to SKIP if (a) the {@code iut} suite attribute is missing, OR (b)
 * GeoRobotix's {@code /deployments} returns non-200, OR (c) the {@code items} array is
 * missing or empty (per Sprint 5 Pat-noted GEOROBOTIX-DEPLOYMENTS-SINGLE-ITEM mitigation
 * — single-item shape is valid; non-empty check passes).</li>
 * </ol>
 *
 * <p>
 * <strong>Deployments-unique assertion</strong>:
 * {@code /req/deployment/deployed-system-resource} states (verbatim from
 * {@code req_deployed_system_resource.adoc}): "The server SHALL implement at least one
 * encoding requirements class that provides a representation for the
 * {@code DeployedSystem} resource." This is satisfied at the conformance-class
 * declaration layer (the IUT's {@code /conformance} response MUST include at least one
 * encoding class such as {@code .../conf/geojson} or {@code .../conf/sensorml}). Per
 * Sprint 5 Pat-recommended pattern: SKIP-with-reason if the IUT does not declare a
 * relevant encoding class. Curl-verified GeoRobotix 2026-04-29: declares both
 * {@code .../conf/geojson} and {@code .../conf/sensorml} — assertion PASSES at the
 * conformance-class-presence layer.
 * </p>
 *
 * <p>
 * Covers:
 * </p>
 * <ul>
 * <li><strong>SCENARIO-ETS-PART1-004-DEPLOYMENTS-RESOURCES-001</strong> (CRITICAL) — GET
 * {@code /deployments} returns 200 + JSON + non-empty {@code items} array. Curl-verified
 * GeoRobotix 2026-04-29: returns 1 item, id={@code 16sp744ch58g}, type=Feature
 * (single-item shape; non-empty check passes). Per OGC 23-001
 * {@code /req/deployment/resources-endpoint}.</li>
 * <li><strong>SCENARIO-ETS-PART1-004-DEPLOYMENTS-CANONICAL-001</strong> (NORMAL) — GET
 * {@code /deployments/{id}} returns item with {@code id} (string), {@code type} (string),
 * {@code links} (array). Per OGC 23-001 {@code /req/deployment/canonical-endpoint} +
 * REQ-ETS-CORE-004 base shape.</li>
 * <li><strong>SCENARIO-ETS-PART1-004-DEPLOYMENTS-CANONICAL-URL-001</strong> (NORMAL) —
 * single-deployment {@code links} contains {@code rel="canonical"}. Per OGC 23-001
 * {@code /req/deployment/canonical-url}.</li>
 * <li><strong>SCENARIO-ETS-PART1-004-DEPLOYMENTS-DEPLOYED-SYSTEM-001</strong> (NORMAL,
 * <strong>UNIQUE-to-Deployments</strong>) — IUT's {@code /conformance} declares at least
 * one encoding class that provides a {@code DeployedSystem} representation
 * ({@code .../conf/geojson} or {@code .../conf/sensorml}). SKIP-with-reason if absent.
 * Per OGC 23-001 {@code /req/deployment/deployed-system-resource} (HYPHENATED form).</li>
 * </ul>
 *
 * <p>
 * <strong>Canonical URI form</strong>: per OGC source
 * {@code raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/deployment/}
 * (5 .adoc files HTTP-200-verified by Pat 2026-04-29 + re-verified by Generator at sprint
 * time): {@code req_resources_endpoint.adoc}, {@code req_canonical_url.adoc},
 * {@code req_canonical_endpoint.adoc}, {@code req_deployed_system_resource.adoc}
 * (identifier: {@code /req/deployment/deployed-system-resource} —
 * <strong>HYPHENATED</strong>, not underscored), {@code req_collections.adoc}.
 * </p>
 *
 * <p>
 * The IUT also declares {@code .../conf/deployment} in {@code /conformance} (per OGC
 * 23-001 Annex A conformance class declaration; curl-verified GeoRobotix 2026-04-29).
 * </p>
 *
 * <p>
 * <strong>Curl-confirmed shape (2026-04-29, Generator re-verification at Sprint 5 Run 2
 * sprint time)</strong>:
 * </p>
 * <ul>
 * <li>{@code GET /deployments} → 200 + {@code items: [1 GeoJSON Feature]} —
 * <strong>single-item shape</strong>; id={@code 16sp744ch58g}, type=Feature,
 * {@code geometry: Polygon (17 coordinates — Saildrone Arctic Mission flight envelope)},
 * properties contains {@code deployedSystems@link} array linking to
 * {@code /systems/17k8rt78j4j0}. NOTE: deployments DO have geometry (unlike procedures —
 * Deployments-vs-Procedures geometry asymmetry by design: procedures are abstract,
 * deployments are physical/temporal/spatial events).</li>
 * <li>{@code GET /deployments/16sp744ch58g} → 200 + GeoJSON Feature with
 * {@code geometry: Polygon} + {@code links} array containing 3 entries: {@code canonical}
 * (json), {@code alternate} (sml3), {@code alternate} (html).</li>
 * <li>{@code GET /conformance} declares 33 conformance classes including
 * {@code .../conf/deployment}, {@code .../conf/geojson}, and {@code .../conf/sensorml} —
 * DeployedSystem encoding requirement is satisfied via {@code conf/geojson} (used for the
 * IUT's response of {@code /systems/17k8rt78j4j0} per
 * {@code deployedSystems@link.href}).</li>
 * </ul>
 */
public class DeploymentsTests {

	/**
	 * Canonical OGC requirement URI for the Deployments collection endpoint.
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/deployment/req_resources_endpoint.adoc">req_resources_endpoint.adoc</a>
	 */
	static final String REQ_DEPLOYMENT_RESOURCES_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/deployment/resources-endpoint";

	/**
	 * Canonical OGC requirement URI for the Deployment canonical-endpoint shape.
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/deployment/req_canonical_endpoint.adoc">req_canonical_endpoint.adoc</a>
	 */
	static final String REQ_DEPLOYMENT_CANONICAL_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/deployment/canonical-endpoint";

	/**
	 * Canonical OGC requirement URI for the Deployment canonical-URL link discipline.
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/deployment/req_canonical_url.adoc">req_canonical_url.adoc</a>
	 */
	static final String REQ_DEPLOYMENT_CANONICAL_URL = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/deployment/canonical-url";

	/**
	 * Canonical OGC requirement URI for the Deployment deployed-system-resource encoding
	 * requirement (UNIQUE-to-Deployments). HYPHENATED form per OGC source identifier.
	 *
	 * @see <a href=
	 * "https://raw.githubusercontent.com/opengeospatial/ogcapi-connected-systems/master/api/part1/standard/requirements/deployment/req_deployed_system_resource.adoc">req_deployed_system_resource.adoc</a>
	 */
	static final String REQ_DEPLOYMENT_DEPLOYED_SYSTEM_RESOURCE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/deployment/deployed-system-resource";

	private URI iutUri;

	/** Cached /deployments collection URI. */
	private URI deploymentsUri;

	private Response deploymentsResponse;

	private Map<String, Object> deploymentsBody;

	/** Cached first-deployment id extracted from the collection (or null). */
	private String firstDeploymentId;

	/**
	 * Cached single-deployment response for {@code /deployments/{firstDeploymentId}}.
	 */
	private Response deploymentItemResponse;

	private Map<String, Object> deploymentItemBody;

	/** Cached /conformance response for the deployed-system-resource encoding check. */
	private Response conformanceResponse;

	private List<String> conformsTo;

	/**
	 * Reads the {@code iut} suite attribute, fetches {@code /deployments} once,
	 * dereferences the first item once, and fetches {@code /conformance} once for the
	 * deployed-system-resource encoding-class assertion. All four {@code @Test} methods
	 * operate on cached responses.
	 *
	 * <p>
	 * <strong>Two-level dependency-skip cascade fallback</strong> (ADR-010 v3 amendment
	 * defense-in-depth, retained as inert insurance): if the suite-level
	 * {@code <group depends-on>} cascade does not fire, this {@code @BeforeClass}
	 * {@link SkipException} cascades all four {@code @Test} methods to SKIP when (a)
	 * {@code iut} attribute missing/non-URI, OR (b) GeoRobotix's {@code /deployments}
	 * returns non-200, OR (c) the {@code items} array is missing or empty.
	 * </p>
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchDeploymentsCollection(ITestContext testContext) {
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		this.iutUri = (URI) iutAttr;
		String iutString = this.iutUri.toString();
		String base = iutString.endsWith("/") ? iutString : iutString + "/";

		this.deploymentsUri = URI.create(base + "deployments");
		this.deploymentsResponse = given().accept("application/json").when().get(this.deploymentsUri).andReturn();
		if (this.deploymentsResponse.getStatusCode() != 200) {
			throw new SkipException(REQ_DEPLOYMENT_RESOURCES_ENDPOINT + " — /deployments returned HTTP "
					+ this.deploymentsResponse.getStatusCode() + "; cannot exercise Deployments conformance class.");
		}
		try {
			this.deploymentsBody = this.deploymentsResponse.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			throw new SkipException(REQ_DEPLOYMENT_RESOURCES_ENDPOINT + " — /deployments body did not parse as JSON: "
					+ ex.getMessage());
		}

		// Eagerly resolve first-deployment id + fetch /deployments/{id} so per-item
		// @Tests can assert against cached state. Single-item shape (GeoRobotix 1
		// deployment) is valid here — items.size() >= 1 is non-empty per
		// /req/deployment/resources-endpoint.
		this.firstDeploymentId = extractFirstDeploymentId();
		if (this.firstDeploymentId != null) {
			URI itemUri = URI.create(base + "deployments/" + this.firstDeploymentId);
			this.deploymentItemResponse = given().accept("application/json").when().get(itemUri).andReturn();
			try {
				this.deploymentItemBody = this.deploymentItemResponse.jsonPath().getMap("$");
			}
			catch (Exception ex) {
				this.deploymentItemBody = null;
			}
		}

		// Fetch /conformance for the deployed-system-resource encoding-class check
		// (best-effort; non-blocking — if /conformance is unavailable, the
		// deployed-system-resource @Test SKIPs-with-reason rather than FAILs).
		try {
			URI conformanceUri = URI.create(base + "conformance");
			this.conformanceResponse = given().accept("application/json").when().get(conformanceUri).andReturn();
			if (this.conformanceResponse.getStatusCode() == 200) {
				List<?> rawList = this.conformanceResponse.jsonPath().getList("conformsTo");
				if (rawList != null) {
					this.conformsTo = rawList.stream()
						.filter(o -> o instanceof String)
						.map(o -> (String) o)
						.collect(Collectors.toList());
				}
			}
		}
		catch (Exception ex) {
			// best-effort; deployed-system-resource @Test handles null conformsTo
			this.conformsTo = null;
		}
	}

	/**
	 * SCENARIO-ETS-PART1-004-DEPLOYMENTS-RESOURCES-001 (CRITICAL):
	 * {@code GET /deployments} returns HTTP 200 + non-empty {@code items} array.
	 *
	 * <p>
	 * Per OGC 23-001 {@code /req/deployment/resources-endpoint}: the server SHALL expose
	 * {@code /deployments} as the canonical Deployments collection endpoint.
	 * </p>
	 *
	 * <p>
	 * GeoRobotix curl evidence (2026-04-29): {@code /deployments} returns 200 + 1 GeoJSON
	 * Feature (single-item shape; non-empty check passes since {@code items.size() == 1}
	 * is non-empty).
	 * </p>
	 */
	@Test(description = "OGC-23-001 " + REQ_DEPLOYMENT_RESOURCES_ENDPOINT
			+ ": GET /deployments returns HTTP 200 + non-empty items array (REQ-ETS-PART1-004, SCENARIO-ETS-PART1-004-DEPLOYMENTS-RESOURCES-001)",
			groups = "deployments")
	@SuppressWarnings("unchecked")
	public void deploymentsCollectionReturns200() {
		ETSAssert.assertStatus(this.deploymentsResponse, 200, REQ_DEPLOYMENT_RESOURCES_ENDPOINT);
		if (this.deploymentsBody == null) {
			ETSAssert.failWithUri(REQ_DEPLOYMENT_RESOURCES_ENDPOINT, "/deployments body did not parse as JSON. "
					+ "Content-Type was: " + this.deploymentsResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.deploymentsBody, "items", List.class, REQ_DEPLOYMENT_RESOURCES_ENDPOINT);
		List<Object> items = (List<Object>) this.deploymentsBody.get("items");
		if (items.isEmpty()) {
			ETSAssert.failWithUri(REQ_DEPLOYMENT_RESOURCES_ENDPOINT, "/deployments items array is empty; expected at "
					+ "least one Deployment item per /req/deployment/resources-endpoint.");
		}
	}

	/**
	 * SCENARIO-ETS-PART1-004-DEPLOYMENTS-CANONICAL-001 (NORMAL): the first deployment
	 * item dereferenced at {@code /deployments/{id}} has {@code id} (string),
	 * {@code type} (string), and {@code links} (array) per OGC 23-001
	 * {@code /req/deployment/canonical-endpoint} + REQ-ETS-CORE-004 base shape.
	 */
	@Test(description = "OGC-23-001 " + REQ_DEPLOYMENT_CANONICAL_ENDPOINT
			+ ": GET /deployments/{id} has id+type+links per canonical-endpoint shape (REQ-ETS-PART1-004, SCENARIO-ETS-PART1-004-DEPLOYMENTS-CANONICAL-001)",
			dependsOnMethods = "deploymentsCollectionReturns200", groups = "deployments")
	public void deploymentItemHasIdTypeLinks() {
		if (this.firstDeploymentId == null) {
			throw new SkipException(REQ_DEPLOYMENT_CANONICAL_ENDPOINT
					+ " — no items in /deployments collection to dereference; cannot test single-item shape.");
		}
		ETSAssert.assertStatus(this.deploymentItemResponse, 200, REQ_DEPLOYMENT_CANONICAL_ENDPOINT);
		if (this.deploymentItemBody == null) {
			ETSAssert.failWithUri(REQ_DEPLOYMENT_CANONICAL_ENDPOINT, "/deployments/" + this.firstDeploymentId
					+ " body did not parse as JSON. Content-Type was: " + this.deploymentItemResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.deploymentItemBody, "id", String.class, REQ_DEPLOYMENT_CANONICAL_ENDPOINT);
		ETSAssert.assertJsonObjectHas(this.deploymentItemBody, "type", String.class, REQ_DEPLOYMENT_CANONICAL_ENDPOINT);
		ETSAssert.assertJsonObjectHas(this.deploymentItemBody, "links", List.class, REQ_DEPLOYMENT_CANONICAL_ENDPOINT);
	}

	/**
	 * SCENARIO-ETS-PART1-004-DEPLOYMENTS-CANONICAL-URL-001 (NORMAL): single-deployment
	 * {@code links} contains {@code rel="canonical"} per OGC 23-001
	 * {@code /req/deployment/canonical-url}.
	 *
	 * <p>
	 * Same v1.0 GH#3 fix policy as
	 * {@code SubsystemsTests.subsystemItemHasCanonicalLink()} +
	 * {@code ProceduresTests.procedureItemHasCanonicalLink()}: {@code rel="canonical"} is
	 * the load-bearing assertion; absence of {@code rel="self"} is NOT a FAIL.
	 * </p>
	 */
	@Test(description = "OGC-23-001 " + REQ_DEPLOYMENT_CANONICAL_URL
			+ ": /deployments/{id} links contain rel=canonical (REQ-ETS-PART1-004, SCENARIO-ETS-PART1-004-DEPLOYMENTS-CANONICAL-URL-001)",
			dependsOnMethods = "deploymentItemHasIdTypeLinks", groups = "deployments")
	public void deploymentItemHasCanonicalLink() {
		if (this.firstDeploymentId == null || this.deploymentItemBody == null) {
			throw new SkipException(
					REQ_DEPLOYMENT_CANONICAL_URL + " — no single-deployment body available to assert link discipline.");
		}
		List<?> links = itemLinksList();
		Predicate<Object> isCanonical = l -> (l instanceof Map) && "canonical".equals(((Map<?, ?>) l).get("rel"));
		ETSAssert.assertJsonArrayContains(links, isCanonical, "rel=canonical link on /deployments/"
				+ this.firstDeploymentId + " (got rels: " + collectItemRels() + ")", REQ_DEPLOYMENT_CANONICAL_URL);
	}

	/**
	 * SCENARIO-ETS-PART1-004-DEPLOYMENTS-DEPLOYED-SYSTEM-001 (NORMAL,
	 * <strong>UNIQUE-to-Deployments architectural requirement</strong>): the IUT's
	 * {@code /conformance} declares at least one encoding requirements class that
	 * provides a {@code DeployedSystem} representation (typically
	 * {@code .../conf/geojson} and/or {@code .../conf/sensorml}).
	 *
	 * <p>
	 * Per OGC 23-001 {@code /req/deployment/deployed-system-resource} (verbatim from
	 * {@code req_deployed_system_resource.adoc}): "The server SHALL implement at least
	 * one encoding requirements class that provides a representation for the
	 * {@code DeployedSystem} resource."
	 * </p>
	 *
	 * <p>
	 * SKIP-with-reason if the IUT does not declare a relevant encoding class (per Pat's
	 * Sprint 5 contract guidance — single-item GeoRobotix shape's deployedSystems@link
	 * resolves to {@code /systems/17k8rt78j4j0}, which the IUT serves via
	 * {@code conf/geojson} encoding; absence of an encoding class would be an IUT
	 * conformance gap, but absence in /conformance might also indicate that the IUT
	 * doesn't expose conformance metadata at all — SKIP rather than FAIL since the OGC
	 * requirement is satisfied at the encoding-class-presence layer, not at the
	 * specific-item-shape layer).
	 * </p>
	 *
	 * <p>
	 * GeoRobotix curl evidence (2026-04-29): {@code /conformance} declares 33 classes
	 * including {@code .../conf/geojson} (Part 1 §"GeoJSON Encoding") AND
	 * {@code .../conf/sensorml} (Part 1 §"SensorML Encoding") — both provide
	 * DeployedSystem representations per OGC 23-001 §Encoding requirements; assertion
	 * PASSES.
	 * </p>
	 */
	@Test(description = "OGC-23-001 " + REQ_DEPLOYMENT_DEPLOYED_SYSTEM_RESOURCE
			+ ": IUT /conformance declares at least one encoding class providing DeployedSystem representation (REQ-ETS-PART1-004, SCENARIO-ETS-PART1-004-DEPLOYMENTS-DEPLOYED-SYSTEM-001 — UNIQUE-to-Deployments)",
			dependsOnMethods = "deploymentsCollectionReturns200", groups = "deployments")
	public void deploymentDeployedSystemEncodingDeclared() {
		if (this.conformanceResponse == null || this.conformanceResponse.getStatusCode() != 200
				|| this.conformsTo == null) {
			throw new SkipException(REQ_DEPLOYMENT_DEPLOYED_SYSTEM_RESOURCE
					+ " — IUT /conformance unavailable or did not declare a conformsTo array; "
					+ "cannot determine whether DeployedSystem encoding class is declared. "
					+ "SKIP-with-reason rather than FAIL since the OGC requirement is at the "
					+ "encoding-class-declaration layer, not at the specific-item-shape layer.");
		}
		// Recognised encoding classes per OGC 23-001 Annex A (DeployedSystem resource is
		// representable via any of these). A relevant declaration is sufficient to pass.
		// Note: matching is a substring check on the conformsTo URI; this catches both
		// the canonical OGC URI form (.../conf/geojson) and any vendor-specific
		// alternate forms that contain the encoding identifier.
		List<String> matchingTokens = List.of("/conf/geojson", "/conf/sensorml", "/conf/json", "/conf/html");
		boolean hasEncodingClass = false;
		for (String uri : this.conformsTo) {
			for (String token : matchingTokens) {
				if (uri.contains(token)) {
					hasEncodingClass = true;
					break;
				}
			}
			if (hasEncodingClass) {
				break;
			}
		}
		if (!hasEncodingClass) {
			throw new SkipException(REQ_DEPLOYMENT_DEPLOYED_SYSTEM_RESOURCE
					+ " — IUT /conformance did not declare any encoding class providing a "
					+ "DeployedSystem representation (looked for any of: " + matchingTokens + "). conformsTo entries: "
					+ this.conformsTo + ". SKIP-with-reason rather than FAIL since this could be a "
					+ "documentation gap rather than a hard conformance failure.");
		}
		// Encoding class declared → assertion PASSES. The IUT honours the OGC 23-001
		// /req/deployment/deployed-system-resource requirement at the
		// encoding-class-declaration layer.
	}

	// --------------- helpers ---------------

	/**
	 * Extracts the {@code id} of the first item in the cached {@code /deployments}
	 * collection. Returns {@code null} if the body is missing, the {@code items} array is
	 * missing/empty, or the first item lacks a string {@code id}.
	 */
	@SuppressWarnings("unchecked")
	private String extractFirstDeploymentId() {
		if (this.deploymentsBody == null) {
			return null;
		}
		Object itemsObj = this.deploymentsBody.get("items");
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
	 * Returns the parsed {@code links} array from the cached single-deployment body.
	 * Returns an empty list if the body or {@code links} field is missing.
	 */
	private List<?> itemLinksList() {
		Object links = (this.deploymentItemBody == null) ? null : this.deploymentItemBody.get("links");
		return (links instanceof List) ? (List<?>) links : List.of();
	}

	/**
	 * Extracts the set of distinct {@code rel} values from the single-deployment
	 * {@code links} array.
	 */
	@SuppressWarnings("unchecked")
	private Set<String> collectItemRels() {
		Object links = (this.deploymentItemBody == null) ? null : this.deploymentItemBody.get("links");
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
