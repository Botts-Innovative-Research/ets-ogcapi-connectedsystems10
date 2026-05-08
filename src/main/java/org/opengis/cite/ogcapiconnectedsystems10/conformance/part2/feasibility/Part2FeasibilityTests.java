package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.feasibility;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.apicommon.Part2ApiCommonTests;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Part 2 - Command Feasibility safety-gated conformance subset
 * ({@code /conf/feasibility}; OGC 23-002 Annex A).
 */
public class Part2FeasibilityTests {

	static final String GROUP = "part2feasibility";

	static final String CONF_FEASIBILITY = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/feasibility";

	static final String CONF_CONTROLSTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/controlstream";

	static final String REQ_FEASIBILITY = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/feasibility";

	static final String REQ_CANONICAL_URL = REQ_FEASIBILITY + "/canonical-url";

	static final String REQ_REF_FROM_CONTROLSTREAM = REQ_FEASIBILITY + "/ref-from-controlstream";

	static final String REQ_STATUS_ENDPOINT = REQ_FEASIBILITY + "/status-endpoint";

	static final String REQ_RESULT_ENDPOINT = REQ_FEASIBILITY + "/result-endpoint";

	static final String REQ_COLLECTIONS = REQ_FEASIBILITY + "/collections";

	private URI iutUri;

	private URI baseUri;

	private Response conformanceResponse;

	private Map<String, Object> conformanceBody;

	private Response controlStreamsResponse;

	private Map<String, Object> controlStreamsBody;

	/**
	 * Fetches shared read-only inputs once. This class intentionally performs no POST,
	 * PUT, PATCH, or DELETE requests.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchPart2FeasibilityInputs(ITestContext testContext) {
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		this.iutUri = (URI) iutAttr;
		String iutString = this.iutUri.toString();
		this.baseUri = URI.create(iutString.endsWith("/") ? iutString : iutString + "/");

		this.conformanceResponse = given().accept("application/json")
			.when()
			.get(this.baseUri.resolve("conformance"))
			.andReturn();
		this.conformanceBody = parseBody(this.conformanceResponse);

		this.controlStreamsResponse = given().accept("application/json")
			.queryParam("limit", 2)
			.when()
			.get(this.baseUri.resolve("controlstreams"))
			.andReturn();
		this.controlStreamsBody = parseBody(this.controlStreamsResponse);
	}

	/**
	 * SCENARIO-ETS-PART2-004-FEASIBILITY-CONFORMANCE-DECLARED-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_FEASIBILITY
			+ ": /conformance declares /conf/feasibility before Command Feasibility assertions run (REQ-ETS-PART2-004, SCENARIO-ETS-PART2-004-FEASIBILITY-CONFORMANCE-DECLARED-001)",
			groups = GROUP)
	public void feasibilityConformanceDeclared() {
		ETSAssert.assertStatus(this.conformanceResponse, 200, REQ_FEASIBILITY);
		if (this.conformanceBody == null) {
			ETSAssert.failWithUri(REQ_FEASIBILITY, "/conformance body did not parse as JSON. Content-Type was: "
					+ this.conformanceResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.conformanceBody, "conformsTo", List.class, REQ_FEASIBILITY);
		if (!declaresConformance(this.conformanceBody, CONF_FEASIBILITY)) {
			throw new SkipException(CONF_FEASIBILITY
					+ " - IUT does not declare the CS API Part 2 Command Feasibility conformance class; no feasibility POST was issued.");
		}
	}

	/**
	 * SCENARIO-ETS-PART2-004-DEPENDENCY-SKIP-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_FEASIBILITY
			+ ": full /conf/feasibility closure is prerequisite-incomplete when /conf/controlstream is absent (REQ-ETS-PART2-004, SCENARIO-ETS-PART2-004-DEPENDENCY-SKIP-001)",
			groups = GROUP)
	public void feasibilityControlStreamPrerequisiteVisibleForFullClosure() {
		skipIfFeasibilityUndeclared();
		if (!declaresConformance(this.conformanceBody, CONF_CONTROLSTREAM)) {
			throw new SkipException(CONF_CONTROLSTREAM
					+ " - /req/feasibility lists /req/controlstream as a prerequisite. Feasibility endpoint checks cannot establish full /conf/feasibility closure without it.");
		}
		Reporter.log("IUT declares both /conf/feasibility and /conf/controlstream; full-class prerequisite is visible.",
				true);
	}

	/**
	 * SCENARIO-ETS-PART2-004-FEASIBILITY-ENDPOINT-SAFETY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_REF_FROM_CONTROLSTREAM
			+ ": ControlStream-scoped Feasibility endpoint uses the normative singular /controlstream/{csId}/feasibility path without POSTing (REQ-ETS-PART2-004, SCENARIO-ETS-PART2-004-FEASIBILITY-ENDPOINT-SAFETY-001)",
			groups = GROUP)
	public void controlStreamScopedFeasibilityEndpointUsesNormativeSingularPath() {
		skipIfFeasibilityUndeclared();
		String controlStreamId = requireString(selectedControlStream(), "id", REQ_REF_FROM_CONTROLSTREAM);
		String path = normativeControlStreamFeasibilityPath(controlStreamId);
		Response response = given().accept("application/json")
			.queryParam("limit", 2)
			.when()
			.get(this.baseUri.resolve(path))
			.andReturn();
		ETSAssert.assertStatus(response, 200, REQ_REF_FROM_CONTROLSTREAM);
		Map<String, Object> body = parseBody(response);
		if (!hasItemsOnlyCollectionShape(body)) {
			ETSAssert.failWithUri(REQ_REF_FROM_CONTROLSTREAM,
					"/" + path + " did not expose a JSON object with an items[] array.");
		}
	}

	/**
	 * SCENARIO-ETS-PART2-004-FEASIBILITY-RESOURCE-CLOSURE-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CANONICAL_URL
			+ ": actual Feasibility resource is readable at /feasibility/{id} before canonical URL PASS (REQ-ETS-PART2-004, SCENARIO-ETS-PART2-004-FEASIBILITY-RESOURCE-CLOSURE-001)",
			groups = GROUP)
	public void feasibilityCanonicalResourceReadableWhenAvailable() {
		skipIfFeasibilityUndeclared();
		String id = requireString(selectedFeasibilityResource(), "id", REQ_CANONICAL_URL);
		Response response = given().accept("application/json")
			.when()
			.get(this.baseUri.resolve("feasibility/" + id))
			.andReturn();
		ETSAssert.assertStatus(response, 200, REQ_CANONICAL_URL);
		Map<String, Object> body = parseBody(response);
		if (!hasFeasibilityResourceShape(body)) {
			ETSAssert.failWithUri(REQ_CANONICAL_URL,
					"/feasibility/" + id + " did not expose Feasibility resource evidence.");
		}
		if (!id.equals(body.get("id"))) {
			ETSAssert.failWithUri(REQ_CANONICAL_URL,
					"/feasibility/" + id + " returned Feasibility id '" + body.get("id") + "'.");
		}
	}

	/**
	 * SCENARIO-ETS-PART2-004-FEASIBILITY-RESOURCE-CLOSURE-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_STATUS_ENDPOINT
			+ ": actual Feasibility resource exposes a readable /status endpoint when resource evidence exists (REQ-ETS-PART2-004, SCENARIO-ETS-PART2-004-FEASIBILITY-RESOURCE-CLOSURE-001)",
			groups = GROUP)
	public void feasibilityStatusEndpointReadableWhenResourceAvailable() {
		skipIfFeasibilityUndeclared();
		String id = requireString(selectedFeasibilityResource(), "id", REQ_STATUS_ENDPOINT);
		Response response = given().accept("application/json")
			.queryParam("limit", 2)
			.when()
			.get(this.baseUri.resolve("feasibility/" + id + "/status"))
			.andReturn();
		assertItemsCollection(response, REQ_STATUS_ENDPOINT, "/feasibility/" + id + "/status");
	}

	/**
	 * SCENARIO-ETS-PART2-004-FEASIBILITY-RESOURCE-CLOSURE-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_RESULT_ENDPOINT
			+ ": actual Feasibility resource exposes a readable /result endpoint when resource evidence exists (REQ-ETS-PART2-004, SCENARIO-ETS-PART2-004-FEASIBILITY-RESOURCE-CLOSURE-001)",
			groups = GROUP)
	public void feasibilityResultEndpointReadableWhenResourceAvailable() {
		skipIfFeasibilityUndeclared();
		String id = requireString(selectedFeasibilityResource(), "id", REQ_RESULT_ENDPOINT);
		Response response = given().accept("application/json")
			.queryParam("limit", 2)
			.when()
			.get(this.baseUri.resolve("feasibility/" + id + "/result"))
			.andReturn();
		assertItemsCollection(response, REQ_RESULT_ENDPOINT, "/feasibility/" + id + "/result");
	}

	/**
	 * SCENARIO-ETS-PART2-004-FEASIBILITY-COLLECTIONS-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COLLECTIONS
			+ ": Feasibility collections are checked only when advertised with itemType=Feasibility (REQ-ETS-PART2-004, SCENARIO-ETS-PART2-004-FEASIBILITY-COLLECTIONS-001)",
			groups = GROUP)
	public void feasibilityCollectionsCheckedWhenAdvertised() {
		skipIfFeasibilityUndeclared();
		Response collections = given().accept("application/json")
			.queryParam("limit", 100)
			.when()
			.get(this.baseUri.resolve("collections"))
			.andReturn();
		if (collections.getStatusCode() != 200) {
			throw new SkipException(REQ_COLLECTIONS + " - /collections returned HTTP " + collections.getStatusCode()
					+ "; no Feasibility collection metadata is available.");
		}
		Map<String, Object> body = parseBody(collections);
		List<?> items = items(body);
		Object feasibilityCollection = items.stream()
			.filter(Part2FeasibilityTests::isFeasibilityCollection)
			.findFirst()
			.orElse(null);
		if (!(feasibilityCollection instanceof Map)) {
			throw new SkipException(
					REQ_COLLECTIONS + " - /collections did not advertise a collection with itemType=Feasibility.");
		}
		String collectionId = requireString(castMap(feasibilityCollection), "id", REQ_COLLECTIONS);
		Response collectionItems = given().accept("application/json")
			.queryParam("limit", 2)
			.when()
			.get(this.baseUri.resolve("collections/" + collectionId + "/items"))
			.andReturn();
		assertItemsCollection(collectionItems, REQ_COLLECTIONS, "/collections/" + collectionId + "/items");
	}

	static boolean declaresConformance(Map<String, Object> body, String conformanceUri) {
		return Part2ApiCommonTests.declaresConformance(body, conformanceUri);
	}

	static String normativeControlStreamFeasibilityPath(String controlStreamId) {
		return "controlstream/" + controlStreamId + "/feasibility";
	}

	static boolean hasFeasibilityResourceShape(Map<String, Object> body) {
		if (body == null || !(body.get("id") instanceof String)) {
			return false;
		}
		return body.containsKey("params") || body.containsKey("parameters") || body.containsKey("status")
				|| body.containsKey("controlstream@id") || body.containsKey("controlStream@id")
				|| body.containsKey("controlstream@link") || body.containsKey("controlStream@link")
				|| body.get("links") instanceof List;
	}

	static boolean isFeasibilityCollection(Object item) {
		return item instanceof Map && "Feasibility".equals(((Map<?, ?>) item).get("itemType"));
	}

	static boolean hasItemsOnlyCollectionShape(Map<String, Object> body) {
		return body != null && body.get("items") instanceof List;
	}

	private void skipIfFeasibilityUndeclared() {
		if (!declaresConformance(this.conformanceBody, CONF_FEASIBILITY)) {
			throw new SkipException(CONF_FEASIBILITY
					+ " - IUT does not declare the CS API Part 2 Command Feasibility conformance class in /conformance; no feasibility POST was issued.");
		}
	}

	private Map<String, Object> selectedControlStream() {
		if (this.controlStreamsResponse.getStatusCode() != 200) {
			throw new SkipException(REQ_REF_FROM_CONTROLSTREAM + " - /controlstreams returned HTTP "
					+ this.controlStreamsResponse.getStatusCode()
					+ "; no ControlStream is available for Feasibility endpoint checks.");
		}
		List<?> controlStreams = items(this.controlStreamsBody);
		if (controlStreams.isEmpty()) {
			throw new SkipException(REQ_REF_FROM_CONTROLSTREAM
					+ " - /controlstreams returned an empty collection; no ControlStream is available for Feasibility endpoint checks.");
		}
		Object first = controlStreams.get(0);
		if (!(first instanceof Map)) {
			ETSAssert.failWithUri(REQ_REF_FROM_CONTROLSTREAM,
					"/controlstreams first item was not a JSON object: " + first);
		}
		return castMap(first);
	}

	private Map<String, Object> selectedFeasibilityResource() {
		Response response = given().accept("application/json")
			.queryParam("limit", 2)
			.when()
			.get(this.baseUri.resolve("feasibility"))
			.andReturn();
		if (response.getStatusCode() != 200) {
			throw new SkipException(REQ_CANONICAL_URL + " - /feasibility returned HTTP " + response.getStatusCode()
					+ "; no Feasibility resource evidence is available.");
		}
		Map<String, Object> body = parseBody(response);
		List<?> feasibilityResources = items(body);
		if (feasibilityResources.isEmpty()) {
			throw new SkipException(REQ_CANONICAL_URL
					+ " - /feasibility returned an empty collection; no Feasibility resource evidence is available.");
		}
		Object first = feasibilityResources.get(0);
		if (!(first instanceof Map)) {
			ETSAssert.failWithUri(REQ_CANONICAL_URL, "/feasibility first item was not a JSON object: " + first);
		}
		Map<String, Object> feasibility = castMap(first);
		if (!hasFeasibilityResourceShape(feasibility)) {
			ETSAssert.failWithUri(REQ_CANONICAL_URL,
					"/feasibility first item did not expose Feasibility resource evidence.");
		}
		return feasibility;
	}

	private static List<?> items(Map<String, Object> body) {
		if (body == null || !(body.get("items") instanceof List)) {
			return List.of();
		}
		return (List<?>) body.get("items");
	}

	private static String requireString(Map<String, Object> body, String key, String reqUri) {
		ETSAssert.assertJsonObjectHas(body, key, String.class, reqUri);
		return (String) body.get(key);
	}

	private static Map<String, Object> assertItemsCollection(Response response, String reqUri, String source) {
		ETSAssert.assertStatus(response, 200, reqUri);
		Map<String, Object> body = parseBody(response);
		if (body == null) {
			ETSAssert.failWithUri(reqUri,
					source + " body did not parse as JSON. Content-Type was: " + response.getContentType());
		}
		if (!hasItemsOnlyCollectionShape(body)) {
			ETSAssert.failWithUri(reqUri, source + " did not expose a JSON object with an items[] array.");
		}
		return body;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> castMap(Object value) {
		return (Map<String, Object>) value;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> parseBody(Response response) {
		if (response == null || response.getBody() == null) {
			return null;
		}
		try {
			return response.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			return null;
		}
	}

}
