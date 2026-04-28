package org.opengis.cite.ogcapiconnectedsystems10.conformance.core;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Core — {@code /conformance} endpoint tests.
 *
 * <p>
 * Implements <strong>REQ-ETS-CORE-003</strong> (conformance endpoint assertions). Covers
 * <strong>SCENARIO-ETS-CORE-CONFORMANCE-001</strong> (CRITICAL) — GET /conformance
 * returns 200 + JSON + {@code conformsTo} array, and the URI list is captured into the
 * TestNG suite context for downstream conformance classes.
 * </p>
 *
 * <p>
 * Direct port of v1.0 {@code csapi_compliance/src/engine/registry/common.ts}
 * REQ_CONFORMANCE_ENDPOINT + REQ_CONFORMANCE_CONFORMS_TO. CS API Core conformance
 * declaration URI ({@code .../ogcapi-connectedsystems-1/1.0/conf/core}) is asserted
 * separately to verify the IUT actually advertises CS API Core conformance.
 * </p>
 *
 * <p>
 * Reference: OGC API – Common Part 1 (19-072)
 * {@code /req/landing-page/conformance-success}; OGC API – Connected Systems Part 1
 * (23-001) Annex A {@code /conf/core}.
 * </p>
 */
public class ConformanceTests {

	/** Canonical OGC requirement URI for conformance-success. */
	static final String REQ_CONFORMANCE_SUCCESS = "http://www.opengis.net/spec/ogcapi-common-1/1.0/req/landing-page/conformance-success";

	/** Canonical OGC requirement URI for the CS API Core conformance class. */
	static final String CS_CORE_CONFORMANCE_URI = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/core";

	/**
	 * ISuite attribute name under which the parsed {@code conformsTo} list is stashed for
	 * downstream conformance classes (Sprint 2+ wiring per design.md
	 * {@code conformanceListStashedForDependentSuites}).
	 */
	public static final String CONFORMS_TO_ATTR = "declaredConformanceClasses";

	private URI conformanceUri;

	private Response response;

	private Map<String, Object> body;

	@BeforeClass
	public void fetchConformancePage(ITestContext testContext) {
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		URI iut = (URI) iutAttr;
		String iutString = iut.toString();
		String conformancePath = iutString.endsWith("/") ? iutString + "conformance" : iutString + "/conformance";
		this.conformanceUri = URI.create(conformancePath);
		this.response = given().accept("application/json").when().get(this.conformanceUri).andReturn();
		try {
			this.body = this.response.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			this.body = null;
		}
	}

	/**
	 * SCENARIO-ETS-CORE-CONFORMANCE-001: GET /conformance returns HTTP 200.
	 */
	@Test(description = "OGC-19-072 " + REQ_CONFORMANCE_SUCCESS
			+ ": GET /conformance returns HTTP 200 (REQ-ETS-CORE-003, SCENARIO-ETS-CORE-CONFORMANCE-001)",
			groups = "core")
	public void conformancePageReturnsHttp200() {
		ETSAssert.assertStatus(this.response, 200, REQ_CONFORMANCE_SUCCESS);
	}

	/**
	 * SCENARIO-ETS-CORE-CONFORMANCE-001: /conformance returns parseable JSON.
	 *
	 * <p>
	 * Per the v1.0 known-issue catalog (csapi_compliance/ops/known-issues.md "Overly
	 * Strict Content-Type Check in Discovery", fixed 2026-03-31): real CS API servers may
	 * emit non-standard Content-Type values (e.g. GeoRobotix returns
	 * {@code Content-Type: auto}) while serving valid JSON. PASS if the body is JSON
	 * parseable; the Content-Type header value is informational.
	 * </p>
	 */
	@Test(description = "OGC-19-072 " + REQ_CONFORMANCE_SUCCESS
			+ ": /conformance body is parseable JSON (REQ-ETS-CORE-003, SCENARIO-ETS-CORE-CONFORMANCE-001)",
			groups = "core")
	public void conformancePageReturnsJson() {
		if (this.body == null) {
			ETSAssert.failWithUri(REQ_CONFORMANCE_SUCCESS,
					"/conformance body did not parse as JSON. Content-Type was: " + this.response.getContentType());
		}
	}

	/**
	 * SCENARIO-ETS-CORE-CONFORMANCE-001: body has non-empty {@code conformsTo} array.
	 * Stashes the URI list onto the TestNG ISuite for downstream classes.
	 */
	@Test(description = "OGC-19-072 " + REQ_CONFORMANCE_SUCCESS
			+ ": /conformance body has non-empty conformsTo array, captured into suite context (REQ-ETS-CORE-003, SCENARIO-ETS-CORE-CONFORMANCE-001)",
			dependsOnMethods = "conformancePageReturnsJson", groups = "core")
	@SuppressWarnings("unchecked")
	public void conformancePageHasConformsToArray(ITestContext testContext) {
		if (this.body == null) {
			ETSAssert.failWithUri(REQ_CONFORMANCE_SUCCESS, "/conformance body did not parse as JSON");
		}
		ETSAssert.assertJsonObjectHas(this.body, "conformsTo", List.class, REQ_CONFORMANCE_SUCCESS);
		List<Object> conformsList = (List<Object>) this.body.get("conformsTo");
		if (conformsList.isEmpty()) {
			ETSAssert.failWithUri(REQ_CONFORMANCE_SUCCESS,
					"'conformsTo' array is empty; expected at least one declared conformance class URI");
		}
		ISuite suite = testContext.getSuite();
		suite.setAttribute(CONFORMS_TO_ATTR, conformsList);
	}

	/**
	 * Asserts the IUT declares the CS API Core conformance class. This is the entry-point
	 * gate for the whole suite — without it, downstream conformance classes are
	 * meaningless against this IUT.
	 */
	@Test(description = "OGC-23-001 " + CS_CORE_CONFORMANCE_URI
			+ ": /conformance declares the CS API Core conformance class URI (REQ-ETS-CORE-003)",
			dependsOnMethods = "conformancePageHasConformsToArray", groups = "core")
	@SuppressWarnings("unchecked")
	public void conformancePageDeclaresCsCore() {
		List<Object> conformsList = (List<Object>) this.body.get("conformsTo");
		Predicate<Object> isCsCore = uri -> CS_CORE_CONFORMANCE_URI.equals(uri);
		ETSAssert.assertJsonArrayContains(conformsList, isCsCore, "CS API Core conformance class URI '"
				+ CS_CORE_CONFORMANCE_URI + "' (declared classes: " + conformsList + ")", CS_CORE_CONFORMANCE_URI);
	}

}
