package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.controlstream;

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
 * CS API Part 2 - Control Streams and Commands read-only conformance subset
 * ({@code /conf/controlstream}; OGC 23-002 Annex A).
 */
public class Part2ControlStreamTests {

	static final String GROUP = "part2controlstream";

	static final String CONF_CONTROLSTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/controlstream";

	static final String CONF_PART2_API_COMMON = Part2ApiCommonTests.CONF_PART2_API_COMMON;

	static final String REQ_CONTROLSTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/controlstream";

	static final String REQ_CANONICAL_URL = REQ_CONTROLSTREAM + "/canonical-url";

	static final String REQ_RESOURCES_ENDPOINT = REQ_CONTROLSTREAM + "/resources-endpoint";

	static final String REQ_CANONICAL_ENDPOINT = REQ_CONTROLSTREAM + "/canonical-endpoint";

	static final String REQ_REF_FROM_SYSTEM = REQ_CONTROLSTREAM + "/ref-from-system";

	static final String REQ_SCHEMA_OP = REQ_CONTROLSTREAM + "/schema-op";

	static final String REQ_CMD_RESOURCES_ENDPOINT = REQ_CONTROLSTREAM + "/cmd-resources-endpoint";

	static final String REQ_CMD_CANONICAL_ENDPOINT = REQ_CONTROLSTREAM + "/cmd-canonical-endpoint";

	static final String REQ_CMD_REF_FROM_CONTROLSTREAM = REQ_CONTROLSTREAM + "/cmd-ref-from-controlstream";

	private URI iutUri;

	private URI baseUri;

	private Response conformanceResponse;

	private Map<String, Object> conformanceBody;

	private Response controlStreamsResponse;

	private Map<String, Object> controlStreamsBody;

	/**
	 * Fetches shared read-only inputs once.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchPart2ControlStreamInputs(ITestContext testContext) {
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
	 * SCENARIO-ETS-PART2-003-CONTROLSTREAM-CONFORMANCE-DECLARED-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CONTROLSTREAM
			+ ": /conformance declares /conf/controlstream before ControlStream assertions run (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-CONTROLSTREAM-CONFORMANCE-DECLARED-001)",
			groups = GROUP)
	public void controlStreamConformanceDeclared() {
		ETSAssert.assertStatus(this.conformanceResponse, 200, REQ_CONTROLSTREAM);
		if (this.conformanceBody == null) {
			ETSAssert.failWithUri(REQ_CONTROLSTREAM, "/conformance body did not parse as JSON. Content-Type was: "
					+ this.conformanceResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.conformanceBody, "conformsTo", List.class, REQ_CONTROLSTREAM);
		if (!declaresConformance(this.conformanceBody, CONF_CONTROLSTREAM)) {
			throw new SkipException(CONF_CONTROLSTREAM
					+ " - IUT does not declare the CS API Part 2 Control Streams and Commands conformance class.");
		}
	}

	/**
	 * SCENARIO-ETS-PART2-003-DEPENDENCY-SKIP-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CONTROLSTREAM
			+ ": full /conf/controlstream closure is prerequisite-incomplete when /conf/api-common is absent (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-DEPENDENCY-SKIP-001)",
			groups = GROUP)
	public void controlStreamApiCommonPrerequisiteVisibleForFullClosure() {
		skipIfControlStreamUndeclared();
		if (!declaresConformance(this.conformanceBody, CONF_PART2_API_COMMON)) {
			throw new SkipException(CONF_PART2_API_COMMON
					+ " - /req/controlstream lists /req/api-common as a prerequisite. Scoped ControlStream endpoint checks may run, but full /conf/controlstream closure is prerequisite-incomplete.");
		}
		Reporter.log("IUT declares both /conf/controlstream and /conf/api-common; full-class prerequisite is visible.",
				true);
	}

	/**
	 * SCENARIO-ETS-PART2-003-CONTROLSTREAM-COLLECTION-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_RESOURCES_ENDPOINT
			+ ": /controlstreams is a readable JSON resource collection (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-CONTROLSTREAM-COLLECTION-READONLY-001)",
			groups = GROUP)
	public void controlStreamsCollectionReadable() {
		skipIfControlStreamUndeclared();
		ETSAssert.assertStatus(this.controlStreamsResponse, 200, REQ_RESOURCES_ENDPOINT);
		if (this.controlStreamsBody == null) {
			ETSAssert.failWithUri(REQ_RESOURCES_ENDPOINT,
					"/controlstreams body did not parse as JSON. Content-Type was: "
							+ this.controlStreamsResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.controlStreamsBody, "items", List.class, REQ_RESOURCES_ENDPOINT);
	}

	/**
	 * SCENARIO-ETS-PART2-003-CONTROLSTREAM-COLLECTION-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CANONICAL_ENDPOINT
			+ ": /controlstreams endpoint exposes ControlStream resources (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-CONTROLSTREAM-COLLECTION-READONLY-001)",
			groups = GROUP)
	public void controlStreamsEndpointExposesControlStreamItems() {
		skipIfControlStreamUndeclared();
		Map<String, Object> controlStream = selectedControlStream();
		assertControlStreamShape(controlStream, REQ_CANONICAL_ENDPOINT);
	}

	/**
	 * SCENARIO-ETS-PART2-003-CONTROLSTREAM-ITEM-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CANONICAL_ENDPOINT
			+ ": selected ControlStream is readable through observed /controlstreams/{id} endpoint (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-CONTROLSTREAM-ITEM-READONLY-001)",
			groups = GROUP)
	public void controlStreamResourceReadableViaObservedEndpoint() {
		skipIfControlStreamUndeclared();
		Map<String, Object> selected = selectedControlStream();
		String id = requireString(selected, "id", REQ_CANONICAL_ENDPOINT);
		Response response = given().accept("application/json")
			.when()
			.get(this.baseUri.resolve("controlstreams/" + id))
			.andReturn();
		ETSAssert.assertStatus(response, 200, REQ_CANONICAL_ENDPOINT);
		Map<String, Object> body = parseBody(response);
		if (body == null) {
			ETSAssert.failWithUri(REQ_CANONICAL_ENDPOINT, "/controlstreams/" + id
					+ " body did not parse as JSON. Content-Type was: " + response.getContentType());
		}
		ETSAssert.assertJsonObjectHas(body, "id", String.class, REQ_CANONICAL_ENDPOINT);
		if (!id.equals(body.get("id"))) {
			ETSAssert.failWithUri(REQ_CANONICAL_ENDPOINT,
					"/controlstreams/" + id + " returned ControlStream id '" + body.get("id") + "'.");
		}
		assertControlStreamShape(body, REQ_CANONICAL_ENDPOINT);
	}

	/**
	 * SCENARIO-ETS-PART2-003-CONTROLSTREAM-CANONICAL-URL-EVIDENCE-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CANONICAL_URL
			+ ": /controls/{id} canonical URL is verified only when that canonical path is readable (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-CONTROLSTREAM-CANONICAL-URL-EVIDENCE-001)",
			groups = GROUP)
	public void controlStreamCanonicalUrlReadableWhenControlsPathAvailable() {
		skipIfControlStreamUndeclared();
		Map<String, Object> selected = selectedControlStream();
		String id = requireString(selected, "id", REQ_CANONICAL_URL);
		Response response = given().accept("application/json")
			.when()
			.get(this.baseUri.resolve("controls/" + id))
			.andReturn();
		if (response.getStatusCode() != 200) {
			throw new SkipException(REQ_CANONICAL_URL + " - /controls/" + id + " returned HTTP "
					+ response.getStatusCode()
					+ "; /controlstreams/{id} alias evidence is not used to PASS the canonical URL requirement.");
		}
		Map<String, Object> body = parseBody(response);
		if (body == null) {
			ETSAssert.failWithUri(REQ_CANONICAL_URL,
					"/controls/" + id + " body did not parse as JSON. Content-Type was: " + response.getContentType());
		}
		if (!id.equals(body.get("id"))) {
			ETSAssert.failWithUri(REQ_CANONICAL_URL,
					"/controls/" + id + " returned ControlStream id '" + body.get("id") + "'.");
		}
		assertControlStreamShape(body, REQ_CANONICAL_URL);
	}

	/**
	 * SCENARIO-ETS-PART2-003-CONTROLSTREAM-SCHEMA-ENDPOINT-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_SCHEMA_OP
			+ ": selected ControlStream exposes a readable /schema sub-resource (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-CONTROLSTREAM-SCHEMA-ENDPOINT-001)",
			groups = GROUP)
	public void controlStreamSchemaReadable() {
		skipIfControlStreamUndeclared();
		String id = requireString(selectedControlStream(), "id", REQ_SCHEMA_OP);
		Response response = given().accept("application/json")
			.when()
			.get(this.baseUri.resolve("controlstreams/" + id + "/schema"))
			.andReturn();
		ETSAssert.assertStatus(response, 200, REQ_SCHEMA_OP);
		Map<String, Object> body = parseBody(response);
		if (body == null) {
			ETSAssert.failWithUri(REQ_SCHEMA_OP, "/controlstreams/" + id
					+ "/schema body did not parse as JSON. Content-Type was: " + response.getContentType());
		}
		ETSAssert.assertJsonObjectHas(body, "commandFormat", String.class, REQ_SCHEMA_OP);
		ETSAssert.assertJsonObjectHas(body, "parametersSchema", Map.class, REQ_SCHEMA_OP);
	}

	/**
	 * SCENARIO-ETS-PART2-003-COMMAND-ENDPOINTS-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CMD_RESOURCES_ENDPOINT
			+ ": /controlstreams/{id}/commands is a readable JSON endpoint collection (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-COMMAND-ENDPOINTS-READONLY-001)",
			groups = GROUP)
	public void controlStreamScopedCommandsReadable() {
		skipIfControlStreamUndeclared();
		String id = requireString(selectedControlStream(), "id", REQ_CMD_RESOURCES_ENDPOINT);
		Response nested = given().accept("application/json")
			.queryParam("limit", 2)
			.when()
			.get(this.baseUri.resolve("controlstreams/" + id + "/commands"))
			.andReturn();
		assertItemsCollection(nested, REQ_CMD_RESOURCES_ENDPOINT, "/controlstreams/" + id + "/commands");
	}

	/**
	 * SCENARIO-ETS-PART2-003-COMMAND-ENDPOINTS-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CMD_CANONICAL_ENDPOINT
			+ ": /commands canonical endpoint is readable when provided by the IUT (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-COMMAND-ENDPOINTS-READONLY-001)",
			groups = GROUP)
	public void globalCommandsEndpointReadableWhenAvailable() {
		skipIfControlStreamUndeclared();
		Response response = given().accept("application/json")
			.queryParam("limit", 2)
			.when()
			.get(this.baseUri.resolve("commands"))
			.andReturn();
		if (response.getStatusCode() != 200) {
			throw new SkipException(REQ_CMD_CANONICAL_ENDPOINT + " - /commands returned HTTP "
					+ response.getStatusCode() + "; cannot PASS the global Commands canonical endpoint requirement.");
		}
		assertItemsCollection(response, REQ_CMD_CANONICAL_ENDPOINT, "/commands");
	}

	/**
	 * SCENARIO-ETS-PART2-003-COMMAND-REFERENCE-EVIDENCE-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CMD_REF_FROM_CONTROLSTREAM
			+ ": ControlStream-scoped Command resources provide reference evidence when populated (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-COMMAND-REFERENCE-EVIDENCE-001)",
			groups = GROUP)
	public void commandsReferenceSelectedControlStreamWhenNestedCollectionPopulated() {
		skipIfControlStreamUndeclared();
		String id = requireString(selectedControlStream(), "id", REQ_CMD_REF_FROM_CONTROLSTREAM);
		Response nested = given().accept("application/json")
			.queryParam("limit", 10)
			.when()
			.get(this.baseUri.resolve("controlstreams/" + id + "/commands"))
			.andReturn();
		Map<String, Object> body = assertItemsCollection(nested, REQ_CMD_REF_FROM_CONTROLSTREAM,
				"/controlstreams/" + id + "/commands");
		List<?> items = items(body);
		if (items.isEmpty()) {
			throw new SkipException(REQ_CMD_REF_FROM_CONTROLSTREAM + " - ControlStream-scoped Command collection for '"
					+ id + "' is empty; endpoint availability is not Command reference PASS evidence.");
		}
		if (!items.stream().anyMatch(item -> commandReferencesControlStream(item, id))) {
			ETSAssert.failWithUri(REQ_CMD_REF_FROM_CONTROLSTREAM, "/controlstreams/" + id
					+ "/commands returned Command items, but none contained controlstream@id, controlStreamId, controlStream.id, or controlstream link evidence for the selected ControlStream.");
		}
	}

	/**
	 * SCENARIO-ETS-PART2-003-SYSTEM-REFERENCE-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_REF_FROM_SYSTEM
			+ ": selected ControlStream's parent System exposes a ControlStream sub-resource collection (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-SYSTEM-REFERENCE-READONLY-001)",
			groups = GROUP)
	public void systemScopedControlStreamsReadableWhenSystemReferencePresent() {
		skipIfControlStreamUndeclared();
		Map<String, Object> selected = selectedControlStream();
		String controlStreamId = requireString(selected, "id", REQ_REF_FROM_SYSTEM);
		String systemId = stringValue(selected.get("system@id"));
		if (systemId == null || systemId.isBlank()) {
			throw new SkipException(REQ_REF_FROM_SYSTEM
					+ " - selected ControlStream does not expose system@id for a bounded System sub-resource check.");
		}
		Response response = given().accept("application/json")
			.queryParam("limit", 100)
			.when()
			.get(this.baseUri.resolve("systems/" + systemId + "/controlstreams"))
			.andReturn();
		Map<String, Object> body = assertItemsCollection(response, REQ_REF_FROM_SYSTEM,
				"/systems/" + systemId + "/controlstreams");
		List<?> pageItems = items(body);
		if (!pageItems.isEmpty() && pageItems.stream().noneMatch(item -> controlStreamId.equals(idOf(item)))) {
			throw new SkipException(REQ_REF_FROM_SYSTEM + " - /systems/" + systemId
					+ "/controlstreams is readable, but the selected ControlStream '" + controlStreamId
					+ "' is not present in the bounded page returned by the IUT.");
		}
	}

	static boolean declaresConformance(Map<String, Object> body, String conformanceUri) {
		return Part2ApiCommonTests.declaresConformance(body, conformanceUri);
	}

	static boolean hasControlStreamShape(Map<String, Object> body) {
		if (body == null || !(body.get("id") instanceof String)) {
			return false;
		}
		boolean hasSystem = body.containsKey("system@id") || body.containsKey("system@link");
		return hasSystem && body.containsKey("inputName") && body.get("controlledProperties") instanceof List
				&& body.get("formats") instanceof List;
	}

	static boolean commandReferencesControlStream(Object command, String controlStreamId) {
		if (!(command instanceof Map) || controlStreamId == null || controlStreamId.isBlank()) {
			return false;
		}
		Map<?, ?> cmd = (Map<?, ?>) command;
		if (controlStreamId.equals(stringValue(cmd.get("controlstream@id")))
				|| controlStreamId.equals(stringValue(cmd.get("controlStream@id")))
				|| controlStreamId.equals(stringValue(cmd.get("controlstreamId")))
				|| controlStreamId.equals(stringValue(cmd.get("controlStreamId")))) {
			return true;
		}
		Object controlStream = cmd.containsKey("controlStream") ? cmd.get("controlStream") : cmd.get("controlstream");
		if (controlStream instanceof Map
				&& controlStreamId.equals(stringValue(((Map<?, ?>) controlStream).get("id")))) {
			return true;
		}
		return linksContainControlStream(cmd.get("links"), controlStreamId)
				|| linksContainControlStream(cmd.get("controlstream@link"), controlStreamId)
				|| linksContainControlStream(cmd.get("controlStream@link"), controlStreamId);
	}

	static boolean hasItemsOnlyCollectionShape(Map<String, Object> body) {
		return body != null && body.get("items") instanceof List;
	}

	private void skipIfControlStreamUndeclared() {
		if (!declaresConformance(this.conformanceBody, CONF_CONTROLSTREAM)) {
			throw new SkipException(CONF_CONTROLSTREAM
					+ " - IUT does not declare the CS API Part 2 Control Streams and Commands conformance class in /conformance.");
		}
	}

	private Map<String, Object> selectedControlStream() {
		if (this.controlStreamsBody == null) {
			ETSAssert.failWithUri(REQ_RESOURCES_ENDPOINT,
					"/controlstreams body did not parse as JSON. Content-Type was: "
							+ this.controlStreamsResponse.getContentType());
		}
		List<?> controlStreams = items(this.controlStreamsBody);
		if (controlStreams.isEmpty()) {
			throw new SkipException(REQ_RESOURCES_ENDPOINT
					+ " - /controlstreams returned an empty collection; no ControlStream item is available for canonical read-only checks.");
		}
		Object first = controlStreams.get(0);
		if (!(first instanceof Map)) {
			ETSAssert.failWithUri(REQ_RESOURCES_ENDPOINT, "/controlstreams first item was not a JSON object: " + first);
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> controlStream = (Map<String, Object>) first;
		return controlStream;
	}

	private static List<?> items(Map<String, Object> body) {
		if (body == null || !(body.get("items") instanceof List)) {
			return List.of();
		}
		return (List<?>) body.get("items");
	}

	private static void assertControlStreamShape(Map<String, Object> body, String reqUri) {
		if (!hasControlStreamShape(body)) {
			ETSAssert.failWithUri(reqUri,
					"ControlStream resource did not expose expected ControlStream-specific members: id, system@id or system@link, inputName, controlledProperties[], and formats[].");
		}
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

	private static String idOf(Object item) {
		if (!(item instanceof Map)) {
			return null;
		}
		return stringValue(((Map<?, ?>) item).get("id"));
	}

	private static boolean linksContainControlStream(Object links, String controlStreamId) {
		if (links instanceof Map) {
			return linkReferencesControlStream((Map<?, ?>) links, controlStreamId);
		}
		if (!(links instanceof List)) {
			return false;
		}
		for (Object link : (List<?>) links) {
			if (link instanceof Map && linkReferencesControlStream((Map<?, ?>) link, controlStreamId)) {
				return true;
			}
		}
		return false;
	}

	private static boolean linkReferencesControlStream(Map<?, ?> link, String controlStreamId) {
		String href = stringValue(link.get("href"));
		return href != null && (href.contains("/controlstreams/" + controlStreamId)
				|| href.contains("/controls/" + controlStreamId));
	}

	private static String stringValue(Object value) {
		return value instanceof String ? (String) value : null;
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
