package org.opengis.cite.ogcapiconnectedsystems10.conformance.core;

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
 * CS API Core — minimal resource-shape conformance tests.
 *
 * <p>
 * Implements <strong>REQ-ETS-CORE-004</strong> (resource base shape). Covers
 * <strong>SCENARIO-ETS-CORE-RESOURCE-SHAPE-001</strong> (NORMAL).
 * </p>
 *
 * <p>
 * <strong>Sprint 1 scope (intentionally minimal):</strong> per design.md <em>"Sprint 1
 * may scope ResourceShapeTests to a single representative resource — likely /api or
 * /conformance itself — and expand to a true crawl in Sprint 2 once Common is
 * implemented."</em> This class therefore exercises:
 * </p>
 * <ol>
 * <li>The api-definition link from the landing page resolves to non-empty content
 * (REQ_OAS30_OAS_IMPL — preserves v1.0 SCENARIO-API-DEF-FALLBACK-001 down-stream
 * verification — test PASSES whether the api-def is service-desc OpenAPI YAML or
 * service-doc HTML).</li>
 * <li>The /conformance resource itself is a JSON object (the simplest representative
 * "resource shape" check on a guaranteed-present resource).</li>
 * </ol>
 *
 * <p>
 * Sprint 2+ will broaden this to a true link-crawl with id/type/links assertions per
 * resource, dispatched via TestNG {@code @DataProvider} per design.md
 * "ResourceShapeTests" responsibilities table.
 * </p>
 *
 * <p>
 * <em>Documented divergence from spec.md REQ-ETS-CORE-004 strict-shape (id/type/links per
 * resource) is captured here as a Sprint-1-narrowing note. Spec Implementation Status
 * reflects this as PARTIAL per ops/changelog reconciliation.</em>
 * </p>
 */
public class ResourceShapeTests {

	/** Canonical OGC requirement URI for OpenAPI 3.0 implementation. */
	static final String REQ_OAS30_OAS_IMPL = "http://www.opengis.net/spec/ogcapi-common-1/1.0/req/oas30/oas-impl";

	/**
	 * Canonical OGC requirement URI for conformance success (re-used for shape check).
	 */
	static final String REQ_CONFORMANCE_SUCCESS = "http://www.opengis.net/spec/ogcapi-common-1/1.0/req/core/conformance-success";

	private URI iutUri;

	private Map<String, Object> landingBody;

	@BeforeClass
	public void fetchLandingPage(ITestContext testContext) {
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		this.iutUri = (URI) iutAttr;
		Response landing = given().accept("application/json").when().get(this.iutUri).andReturn();
		try {
			this.landingBody = landing.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			this.landingBody = null;
		}
	}

	/**
	 * SCENARIO-ETS-CORE-RESOURCE-SHAPE-001 (lite): the api-definition link discovered on
	 * the landing page resolves to HTTP 200 with non-empty content.
	 *
	 * <p>
	 * Preserves v1.0 SCENARIO-API-DEF-FALLBACK-001: the chosen rel is
	 * {@code service-desc} when present, else {@code service-doc}. The structural
	 * assertion is deliberately lax (HTTP 200 + non-empty body) — service-doc returns
	 * HTML, not OpenAPI, so probing an {@code openapi} field would regress on
	 * service-doc-only IUTs.
	 * </p>
	 */
	@Test(description = "OGC-19-072 " + REQ_OAS30_OAS_IMPL
			+ ": api-definition resource resolves to HTTP 200 with non-empty body (REQ-ETS-CORE-004, SCENARIO-ETS-CORE-RESOURCE-SHAPE-001)")
	@SuppressWarnings("unchecked")
	public void apiDefinitionResourceReturnsContent() {
		if (this.landingBody == null) {
			ETSAssert.failWithUri(REQ_OAS30_OAS_IMPL, "landing page body did not parse as JSON");
		}
		ETSAssert.assertJsonObjectHas(this.landingBody, "links", List.class, REQ_OAS30_OAS_IMPL);
		List<Map<String, Object>> linkList = (List<Map<String, Object>>) this.landingBody.get("links");
		Map<String, Object> chosen = pickFirstByRel(linkList, "service-desc");
		if (chosen == null) {
			chosen = pickFirstByRel(linkList, "service-doc");
		}
		if (chosen == null) {
			ETSAssert.failWithUri(REQ_OAS30_OAS_IMPL,
					"neither rel=service-desc nor rel=service-doc present on landing page (api-definition fallback exhausted)");
		}
		Object hrefObj = chosen.get("href");
		if (!(hrefObj instanceof String) || ((String) hrefObj).isEmpty()) {
			ETSAssert.failWithUri(REQ_OAS30_OAS_IMPL, "api-definition link has no usable 'href': " + chosen);
		}
		URI apiDefUri = URI.create((String) hrefObj);
		Response apiDef = given().when().get(apiDefUri).andReturn();
		if (apiDef.getStatusCode() != 200) {
			ETSAssert.failWithUri(REQ_OAS30_OAS_IMPL, "expected HTTP 200 from api-definition (rel='" + chosen.get("rel")
					+ "', href=" + apiDefUri + "), got " + apiDef.getStatusCode());
		}
		String body = apiDef.getBody().asString();
		if (body == null || body.trim().isEmpty()) {
			ETSAssert.failWithUri(REQ_OAS30_OAS_IMPL,
					"api-definition body is empty (rel='" + chosen.get("rel") + "', href=" + apiDefUri + ")");
		}
	}

	/**
	 * SCENARIO-ETS-CORE-RESOURCE-SHAPE-001 (lite): the /conformance resource is a JSON
	 * object (not array, not scalar) — design.md's "single representative resource"
	 * pattern for Sprint 1.
	 */
	@Test(description = "OGC-19-072 " + REQ_CONFORMANCE_SUCCESS
			+ ": /conformance resource is a JSON object (REQ-ETS-CORE-004, SCENARIO-ETS-CORE-RESOURCE-SHAPE-001)")
	public void conformanceResourceShapeIsObject() {
		String iutString = this.iutUri.toString();
		String conformancePath = iutString.endsWith("/") ? iutString + "conformance" : iutString + "/conformance";
		Response resp = given().accept("application/json").when().get(URI.create(conformancePath)).andReturn();
		ETSAssert.assertStatus(resp, 200, REQ_CONFORMANCE_SUCCESS);
		String raw = resp.getBody().asString().trim();
		if (raw.isEmpty() || raw.charAt(0) != '{') {
			ETSAssert.failWithUri(REQ_CONFORMANCE_SUCCESS,
					"expected /conformance body to be a JSON object (start with '{'); got: "
							+ (raw.length() > 60 ? raw.substring(0, 60) + "..." : raw));
		}
	}

	private static Map<String, Object> pickFirstByRel(List<Map<String, Object>> linkList, String rel) {
		for (Map<String, Object> link : linkList) {
			if (link != null && rel.equals(link.get("rel"))) {
				return link;
			}
		}
		return null;
	}

}
