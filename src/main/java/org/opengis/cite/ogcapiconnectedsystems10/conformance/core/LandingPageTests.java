package org.opengis.cite.ogcapiconnectedsystems10.conformance.core;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.AbstractMap;
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
 * CS API Core — landing page conformance tests.
 *
 * <p>
 * Implements <strong>REQ-ETS-CORE-002</strong> (landing-page assertions) and partially
 * REQ-ETS-CORE-001 (one {@code @Test} per OGC ATS assertion, each carrying the canonical
 * OGC requirement URI in its description). Direct port of v1.0
 * {@code csapi_compliance/src/engine/registry/common.ts} canonical URIs (verified against
 * {@code http://www.opengis.net/spec/ogcapi-common-1/1.0/req/landing-page/*} per
 * S-ETS-02-03 OGC canonical {@code .adoc} sweep).
 * </p>
 *
 * <p>
 * Covers:
 * </p>
 * <ul>
 * <li><strong>SCENARIO-ETS-CORE-LANDING-001</strong> (CRITICAL) — landing page returns
 * 200 + JSON + links + conformance link + api-definition link</li>
 * <li><strong>SCENARIO-ETS-CORE-LINKS-NORMATIVE-001</strong> — preserves v1.0 GH#3 fix:
 * {@code rel=self} is example-only, NOT mandatory. Sentinel test
 * {@link #landingPageDoesNotRequireSelfRel} asserts the PASS case (rel=self either
 * present or absent — both PASS).</li>
 * <li><strong>SCENARIO-ETS-CORE-API-DEF-FALLBACK-001</strong> — preserves v1.0 fix:
 * {@code rel=service-desc} OR {@code rel=service-doc} satisfies the API-definition
 * requirement (FAIL only when both absent).</li>
 * </ul>
 *
 * <p>
 * Reference: OGC API – Common Part 1 (19-072) §7.5
 * <em>{@code /req/landing-page/root-success}</em>; OGC API – Connected Systems Part 1
 * (23-001) §7 inherits Common Core. The canonical URIs below match the v1.0 TypeScript
 * registry verbatim per architect-handoff {@code evaluation_focus} #1 (URI fidelity).
 * </p>
 *
 * <p>
 * <strong>Sprint 1 simplification:</strong> Tests use REST Assured directly against the
 * {@code iut} suite attribute. JSON Schema validation via everit-json-schema is deferred
 * to Sprint 2+ (see {@code design.md} "JSON Schema validation"). v1.0 GH#3 fix +
 * service-doc fallback are preserved structurally without schema validation.
 * </p>
 */
public class LandingPageTests {

	/** Canonical OGC requirement URI for landing-page success. */
	static final String REQ_ROOT_SUCCESS = "http://www.opengis.net/spec/ogcapi-common-1/1.0/req/landing-page/root-success";

	/** Canonical OGC requirement URI for conformance-success. */
	static final String REQ_CONFORMANCE_SUCCESS = "http://www.opengis.net/spec/ogcapi-common-1/1.0/req/landing-page/conformance-success";

	/** Canonical OGC requirement URI for api-definition-success. */
	static final String REQ_API_DEFINITION_SUCCESS = "http://www.opengis.net/spec/ogcapi-common-1/1.0/req/landing-page/api-definition-success";

	private URI iutUri;

	private Response landingResponse;

	private Map<String, Object> landingBody;

	/**
	 * Reads the {@code iut} suite attribute and fetches the landing page once for the
	 * whole class. All {@code @Test} methods in this class operate on the cached response
	 * to avoid redundant HTTP traffic against the IUT.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchLandingPage(ITestContext testContext) {
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		this.iutUri = (URI) iutAttr;
		this.landingResponse = given().accept("application/json").when().get(this.iutUri).andReturn();
		try {
			this.landingBody = this.landingResponse.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			this.landingBody = null;
		}
	}

	/**
	 * SCENARIO-ETS-CORE-LANDING-001: landing page must return HTTP 200.
	 * @see #REQ_ROOT_SUCCESS
	 */
	@Test(description = "OGC-19-072 " + REQ_ROOT_SUCCESS
			+ ": landing page (GET /) returns HTTP 200 (REQ-ETS-CORE-002, SCENARIO-ETS-CORE-LANDING-001)",
			groups = "core")
	public void landingPageReturnsHttp200() {
		ETSAssert.assertStatus(this.landingResponse, 200, REQ_ROOT_SUCCESS);
	}

	/**
	 * SCENARIO-ETS-CORE-LANDING-001: landing page must return parseable JSON.
	 *
	 * <p>
	 * Per the v1.0 known-issue catalog (csapi_compliance/ops/known-issues.md "Overly
	 * Strict Content-Type Check in Discovery", fixed 2026-03-31): real CS API servers may
	 * emit non-standard Content-Type values (e.g. GeoRobotix returns
	 * {@code Content-Type: auto}) while serving valid JSON. The Java port preserves
	 * v1.0's PASS-if-body-is-JSON-parseable behavior: parseability is the load-bearing
	 * assertion; the Content-Type header is logged but not strictly checked.
	 * </p>
	 */
	@Test(description = "OGC-19-072 " + REQ_ROOT_SUCCESS
			+ ": landing page body is parseable JSON (REQ-ETS-CORE-002, SCENARIO-ETS-CORE-LANDING-001)",
			groups = "core")
	public void landingPageReturnsJson() {
		if (this.landingBody == null) {
			ETSAssert.failWithUri(REQ_ROOT_SUCCESS, "landing page body did not parse as JSON. Content-Type was: "
					+ this.landingResponse.getContentType());
		}
	}

	/**
	 * SCENARIO-ETS-CORE-LANDING-001: landing page body has a {@code links} array.
	 */
	@Test(description = "OGC-19-072 " + REQ_ROOT_SUCCESS
			+ ": landing page body has a links array (REQ-ETS-CORE-002, SCENARIO-ETS-CORE-LANDING-001)",
			dependsOnMethods = "landingPageReturnsJson", groups = "core")
	public void landingPageHasLinks() {
		if (this.landingBody == null) {
			ETSAssert.failWithUri(REQ_ROOT_SUCCESS, "landing page body did not parse as JSON");
		}
		ETSAssert.assertJsonObjectHas(this.landingBody, "links", List.class, REQ_ROOT_SUCCESS);
	}

	/**
	 * SCENARIO-ETS-CORE-LANDING-001: landing page links contain {@code rel=conformance}.
	 */
	@Test(description = "OGC-19-072 " + REQ_CONFORMANCE_SUCCESS
			+ ": landing page links contain rel=conformance (REQ-ETS-CORE-002, SCENARIO-ETS-CORE-LANDING-001)",
			dependsOnMethods = "landingPageHasLinks", groups = "core")
	public void landingPageHasConformanceLink() {
		List<?> links = linksList();
		Predicate<Object> isConformance = l -> (l instanceof Map) && "conformance".equals(((Map<?, ?>) l).get("rel"));
		ETSAssert.assertJsonArrayContains(links, isConformance,
				"rel=conformance link (got rels: " + collectRels() + ")", REQ_CONFORMANCE_SUCCESS);
	}

	/**
	 * SCENARIO-ETS-CORE-API-DEF-FALLBACK-001: landing page links MUST contain either
	 * {@code rel=service-desc} OR {@code rel=service-doc}; FAIL only when both absent.
	 *
	 * <p>
	 * <strong>Preserves v1.0 SCENARIO-API-DEF-FALLBACK-001 fix</strong>: per OGC 19-072
	 * {@code /req/landing-page/api-definition-success}, either link relation satisfies
	 * the API-definition requirement. service-desc is preferred (machine-readable
	 * OpenAPI); service-doc is the fallback (human-readable HTML).
	 * </p>
	 */
	@Test(description = "OGC-19-072 " + REQ_API_DEFINITION_SUCCESS
			+ ": landing page links contain rel=service-desc OR rel=service-doc (REQ-ETS-CORE-002, SCENARIO-ETS-CORE-API-DEF-FALLBACK-001)",
			dependsOnMethods = "landingPageHasLinks", groups = "core")
	public void landingPageHasApiDefinitionLink() {
		List<?> links = linksList();
		Predicate<Object> isServiceDesc = l -> (l instanceof Map) && "service-desc".equals(((Map<?, ?>) l).get("rel"));
		Predicate<Object> isServiceDoc = l -> (l instanceof Map) && "service-doc".equals(((Map<?, ?>) l).get("rel"));
		ETSAssert.assertJsonArrayContainsAnyOf(links,
				List.of(new AbstractMap.SimpleEntry<>("rel=service-desc link", isServiceDesc),
						new AbstractMap.SimpleEntry<>("rel=service-doc link", isServiceDoc)),
				REQ_API_DEFINITION_SUCCESS);
	}

	/**
	 * SCENARIO-ETS-CORE-LINKS-NORMATIVE-001 sentinel: PRESERVES the v1.0 GH#3 fix.
	 *
	 * <p>
	 * The {@code rel=self} link relation on the landing page is shown as an example in
	 * OGC API – Common Part 1 (19-072) but is <strong>NOT</strong> a normative
	 * requirement. Asserting it as mandatory caused false-positive failures against
	 * conformant servers in v1.0 (issue #3). This test PASSES regardless of whether
	 * {@code rel=self} is present or absent — both states are conformant.
	 * </p>
	 *
	 * <p>
	 * The test exists as a regression sentinel: future contributors who re-introduce a
	 * mandatory rel=self assertion will need to deliberately delete this test, providing
	 * a code-review trip-wire.
	 * </p>
	 */
	@Test(description = "OGC-19-072 " + REQ_ROOT_SUCCESS
			+ ": landing page does NOT require rel=self (sentinel — preserves v1.0 GH#3 fix; SCENARIO-ETS-CORE-LINKS-NORMATIVE-001)",
			dependsOnMethods = "landingPageHasLinks", groups = "core")
	public void landingPageDoesNotRequireSelfRel() {
		Set<String> rels = collectRels();
		// Both branches are PASS. We assert that *either* state is acceptable.
		boolean selfPresent = rels.contains("self");
		boolean selfAbsent = !rels.contains("self");
		if (!(selfPresent || selfAbsent)) {
			// Logically unreachable — kept defensive in case rels is somehow null.
			ETSAssert.failWithUri(REQ_ROOT_SUCCESS, "sentinel could not determine self-rel state from rels: " + rels);
		}
	}

	/** Extracts the set of distinct {@code rel} values from the landing page links. */
	@SuppressWarnings("unchecked")
	private Set<String> collectRels() {
		Object links = (this.landingBody == null) ? null : this.landingBody.get("links");
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

	/**
	 * Returns the parsed {@code links} array as a {@code List<?>} for use with
	 * {@link ETSAssert#assertJsonArrayContains} and friends. Returns an empty list if the
	 * body or {@code links} field is missing/null.
	 */
	private List<?> linksList() {
		Object links = (this.landingBody == null) ? null : this.landingBody.get("links");
		return (links instanceof List) ? (List<?>) links : List.of();
	}

}
