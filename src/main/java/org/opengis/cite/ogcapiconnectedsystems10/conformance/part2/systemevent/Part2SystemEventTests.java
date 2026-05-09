package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.systemevent;

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
 * CS API Part 2 - System Events read-only conformance subset ({@code /conf/system-event};
 * OGC 23-002 Annex A).
 */
public class Part2SystemEventTests {

	static final String GROUP = "part2systemevent";

	static final String CONF_SYSTEM_EVENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/system-event";

	static final String CONF_PART2_API_COMMON = Part2ApiCommonTests.CONF_PART2_API_COMMON;

	static final String CONF_SYSTEM = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/system";

	static final String REQ_SYSTEM_EVENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/system-event";

	static final String REQ_CANONICAL_URL = REQ_SYSTEM_EVENT + "/canonical-url";

	static final String REQ_RESOURCES_ENDPOINT = REQ_SYSTEM_EVENT + "/resources-endpoint";

	static final String REQ_CANONICAL_ENDPOINT = REQ_SYSTEM_EVENT + "/canonical-endpoint";

	static final String REQ_REF_FROM_SYSTEM = REQ_SYSTEM_EVENT + "/ref-from-system";

	static final String REQ_COLLECTIONS = REQ_SYSTEM_EVENT + "/collections";

	private URI iutUri;

	private URI baseUri;

	private Response conformanceResponse;

	private Map<String, Object> conformanceBody;

	private Response systemsResponse;

	private Map<String, Object> systemsBody;

	/**
	 * Fetches shared read-only inputs once. This class intentionally performs no mutation
	 * requests and does not open long-lived streaming event subscriptions.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchPart2SystemEventInputs(ITestContext testContext) {
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

		this.systemsResponse = given().accept("application/json")
			.queryParam("limit", 2)
			.when()
			.get(this.baseUri.resolve("systems"))
			.andReturn();
		this.systemsBody = parseBody(this.systemsResponse);
	}

	/**
	 * SCENARIO-ETS-PART2-005-SYSTEM-EVENT-CONFORMANCE-DECLARED-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_SYSTEM_EVENT
			+ ": /conformance declares /conf/system-event before System Events assertions run (REQ-ETS-PART2-005, SCENARIO-ETS-PART2-005-SYSTEM-EVENT-CONFORMANCE-DECLARED-001)",
			groups = GROUP)
	public void systemEventConformanceDeclared() {
		ETSAssert.assertStatus(this.conformanceResponse, 200, REQ_SYSTEM_EVENT);
		if (this.conformanceBody == null) {
			ETSAssert.failWithUri(REQ_SYSTEM_EVENT, "/conformance body did not parse as JSON. Content-Type was: "
					+ this.conformanceResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.conformanceBody, "conformsTo", List.class, REQ_SYSTEM_EVENT);
		if (!declaresConformance(this.conformanceBody, CONF_SYSTEM_EVENT)) {
			throw new SkipException(
					CONF_SYSTEM_EVENT + " - IUT does not declare the CS API Part 2 System Events conformance class.");
		}
	}

	/**
	 * SCENARIO-ETS-PART2-005-DEPENDENCY-SKIP-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_SYSTEM_EVENT
			+ ": full /conf/system-event closure is prerequisite-incomplete when /conf/api-common or Part 1 /conf/system is absent (REQ-ETS-PART2-005, SCENARIO-ETS-PART2-005-DEPENDENCY-SKIP-001)",
			groups = GROUP)
	public void systemEventPrerequisitesVisibleForFullClosure() {
		skipIfSystemEventUndeclared();
		if (!declaresConformance(this.conformanceBody, CONF_PART2_API_COMMON)) {
			throw new SkipException(CONF_PART2_API_COMMON
					+ " - /req/system-event lists /req/api-common as a prerequisite. Scoped System Events endpoint checks may run, but full /conf/system-event closure is prerequisite-incomplete.");
		}
		if (!declaresConformance(this.conformanceBody, CONF_SYSTEM)) {
			throw new SkipException(CONF_SYSTEM
					+ " - /req/system-event lists Part 1 /req/system as a prerequisite. Full /conf/system-event closure is prerequisite-incomplete.");
		}
		Reporter.log("IUT declares /conf/system-event, /conf/api-common, and Part 1 /conf/system prerequisites.", true);
	}

	/**
	 * SCENARIO-ETS-PART2-005-CANONICAL-ENDPOINT-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CANONICAL_ENDPOINT
			+ ": /systemEvents canonical resources endpoint is readable as JSON when provided (REQ-ETS-PART2-005, SCENARIO-ETS-PART2-005-CANONICAL-ENDPOINT-001)",
			groups = GROUP)
	public void systemEventsCanonicalEndpointReadableWhenAvailable() {
		skipIfSystemEventUndeclared();
		Response response = given().accept("application/json")
			.queryParam("limit", 2)
			.when()
			.get(this.baseUri.resolve("systemEvents"))
			.andReturn();
		if (response.getStatusCode() != 200) {
			throw new SkipException(REQ_CANONICAL_ENDPOINT + " - /systemEvents returned HTTP "
					+ response.getStatusCode() + "; no JSON SystemEvent resources endpoint evidence is available.");
		}
		assertItemsCollection(response, REQ_CANONICAL_ENDPOINT, "/systemEvents");
	}

	/**
	 * SCENARIO-ETS-PART2-005-SYSTEM-REF-ENDPOINT-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_REF_FROM_SYSTEM
			+ ": System-scoped events use normative /systems/{sysId}/events path, not Annex A.43 alias evidence (REQ-ETS-PART2-005, SCENARIO-ETS-PART2-005-SYSTEM-REF-ENDPOINT-001)",
			groups = GROUP)
	public void systemScopedEventsEndpointUsesNormativePath() {
		skipIfSystemEventUndeclared();
		String systemId = requireString(selectedSystem(), "id", REQ_REF_FROM_SYSTEM);
		String path = normativeSystemEventsPath(systemId);
		Response response = given().accept("application/json")
			.queryParam("limit", 2)
			.when()
			.get(this.baseUri.resolve(path))
			.andReturn();
		if (response.getStatusCode() != 200) {
			throw new SkipException(REQ_REF_FROM_SYSTEM + " - /" + path + " returned HTTP " + response.getStatusCode()
					+ "; streaming-only or unsupported responses are not JSON SystemEvent endpoint PASS evidence.");
		}
		assertItemsCollection(response, REQ_REF_FROM_SYSTEM, "/" + path);
	}

	/**
	 * SCENARIO-ETS-PART2-005-SYSTEM-EVENT-RESOURCE-CLOSURE-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CANONICAL_URL
			+ ": actual SystemEvent resource is readable at /systemEvents/{id} before canonical URL PASS (REQ-ETS-PART2-005, SCENARIO-ETS-PART2-005-SYSTEM-EVENT-RESOURCE-CLOSURE-001)",
			groups = GROUP)
	public void systemEventCanonicalResourceReadableWhenAvailable() {
		skipIfSystemEventUndeclared();
		String id = requireString(selectedSystemEventResource(), "id", REQ_CANONICAL_URL);
		Response response = given().accept("application/json")
			.when()
			.get(this.baseUri.resolve("systemEvents/" + id))
			.andReturn();
		ETSAssert.assertStatus(response, 200, REQ_CANONICAL_URL);
		Map<String, Object> body = parseBody(response);
		if (!hasSystemEventResourceShape(body)) {
			ETSAssert.failWithUri(REQ_CANONICAL_URL,
					"/systemEvents/" + id + " did not expose SystemEvent resource evidence.");
		}
		if (!id.equals(body.get("id"))) {
			ETSAssert.failWithUri(REQ_CANONICAL_URL,
					"/systemEvents/" + id + " returned SystemEvent id '" + body.get("id") + "'.");
		}
	}

	/**
	 * SCENARIO-ETS-PART2-005-SYSTEM-EVENT-COLLECTIONS-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COLLECTIONS
			+ ": SystemEvent collections are checked only when advertised with itemType=SystemEvent (REQ-ETS-PART2-005, SCENARIO-ETS-PART2-005-SYSTEM-EVENT-COLLECTIONS-001)",
			groups = GROUP)
	public void systemEventCollectionsCheckedWhenAdvertised() {
		skipIfSystemEventUndeclared();
		Response collections = given().accept("application/json")
			.queryParam("limit", 100)
			.when()
			.get(this.baseUri.resolve("collections"))
			.andReturn();
		if (collections.getStatusCode() != 200) {
			throw new SkipException(REQ_COLLECTIONS + " - /collections returned HTTP " + collections.getStatusCode()
					+ "; no SystemEvent collection metadata is available.");
		}
		Map<String, Object> body = parseBody(collections);
		List<?> items = items(body);
		Object systemEventCollection = items.stream()
			.filter(Part2SystemEventTests::isSystemEventCollection)
			.findFirst()
			.orElse(null);
		if (!(systemEventCollection instanceof Map)) {
			throw new SkipException(
					REQ_COLLECTIONS + " - /collections did not advertise a collection with itemType=SystemEvent.");
		}
		String collectionId = requireString(castMap(systemEventCollection), "id", REQ_COLLECTIONS);
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

	static String normativeSystemEventsPath(String systemId) {
		return "systems/" + systemId + "/events";
	}

	static boolean hasSystemEventResourceShape(Map<String, Object> body) {
		if (body == null || !(body.get("id") instanceof String)) {
			return false;
		}
		return body.containsKey("time") || body.containsKey("eventTime") || body.containsKey("eventType")
				|| body.containsKey("system@id") || body.containsKey("system@link") || body.containsKey("systemUid")
				|| body.get("links") instanceof List;
	}

	static boolean isSystemEventCollection(Object item) {
		return item instanceof Map && "SystemEvent".equals(((Map<?, ?>) item).get("itemType"));
	}

	static boolean hasItemsOnlyCollectionShape(Map<String, Object> body) {
		return body != null && body.get("items") instanceof List;
	}

	private void skipIfSystemEventUndeclared() {
		if (!declaresConformance(this.conformanceBody, CONF_SYSTEM_EVENT)) {
			throw new SkipException(CONF_SYSTEM_EVENT
					+ " - IUT does not declare the CS API Part 2 System Events conformance class in /conformance.");
		}
	}

	private Map<String, Object> selectedSystem() {
		if (this.systemsResponse.getStatusCode() != 200) {
			throw new SkipException(REQ_REF_FROM_SYSTEM + " - /systems returned HTTP "
					+ this.systemsResponse.getStatusCode() + "; no System is available for SystemEvent checks.");
		}
		List<?> systems = items(this.systemsBody);
		if (systems.isEmpty()) {
			throw new SkipException(REQ_REF_FROM_SYSTEM
					+ " - /systems returned an empty collection; no System is available for SystemEvent checks.");
		}
		Object first = systems.get(0);
		if (!(first instanceof Map)) {
			ETSAssert.failWithUri(REQ_REF_FROM_SYSTEM, "/systems first item was not a JSON object: " + first);
		}
		return castMap(first);
	}

	private Map<String, Object> selectedSystemEventResource() {
		Response response = given().accept("application/json")
			.queryParam("limit", 2)
			.when()
			.get(this.baseUri.resolve("systemEvents"))
			.andReturn();
		if (response.getStatusCode() != 200) {
			throw new SkipException(REQ_CANONICAL_URL + " - /systemEvents returned HTTP " + response.getStatusCode()
					+ "; no SystemEvent resource evidence is available.");
		}
		Map<String, Object> body = parseBody(response);
		List<?> systemEvents = items(body);
		if (systemEvents.isEmpty()) {
			throw new SkipException(REQ_CANONICAL_URL
					+ " - /systemEvents returned an empty collection; no SystemEvent resource evidence is available.");
		}
		Object first = systemEvents.get(0);
		if (!(first instanceof Map)) {
			ETSAssert.failWithUri(REQ_CANONICAL_URL, "/systemEvents first item was not a JSON object: " + first);
		}
		Map<String, Object> systemEvent = castMap(first);
		if (!hasSystemEventResourceShape(systemEvent)) {
			ETSAssert.failWithUri(REQ_CANONICAL_URL,
					"/systemEvents first item did not expose SystemEvent resource evidence.");
		}
		return systemEvent;
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
