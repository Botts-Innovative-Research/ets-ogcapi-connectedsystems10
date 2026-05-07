package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.datastream;

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
 * CS API Part 2 - Datastreams and Observations read-only conformance subset
 * ({@code /conf/datastream}; OGC 23-002 Annex A).
 */
public class Part2DatastreamTests {

	static final String GROUP = "part2datastream";

	static final String CONF_DATASTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/datastream";

	static final String CONF_PART2_API_COMMON = Part2ApiCommonTests.CONF_PART2_API_COMMON;

	static final String REQ_DATASTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/datastream";

	static final String REQ_CANONICAL_URL = REQ_DATASTREAM + "/canonical-url";

	static final String REQ_RESOURCES_ENDPOINT = REQ_DATASTREAM + "/resources-endpoint";

	static final String REQ_CANONICAL_ENDPOINT = REQ_DATASTREAM + "/canonical-endpoint";

	static final String REQ_REF_FROM_SYSTEM = REQ_DATASTREAM + "/ref-from-system";

	static final String REQ_SCHEMA_OP = REQ_DATASTREAM + "/schema-op";

	static final String REQ_OBS_CANONICAL_URL = REQ_DATASTREAM + "/obs-canonical-url";

	static final String REQ_OBS_RESOURCES_ENDPOINT = REQ_DATASTREAM + "/obs-resources-endpoint";

	static final String REQ_OBS_CANONICAL_ENDPOINT = REQ_DATASTREAM + "/obs-canonical-endpoint";

	static final String REQ_OBS_REF_FROM_DATASTREAM = REQ_DATASTREAM + "/obs-ref-from-datastream";

	private URI iutUri;

	private URI baseUri;

	private Response conformanceResponse;

	private Map<String, Object> conformanceBody;

	private Response datastreamsResponse;

	private Map<String, Object> datastreamsBody;

	/**
	 * Fetches shared read-only inputs once.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchPart2DatastreamInputs(ITestContext testContext) {
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

		this.datastreamsResponse = given().accept("application/json")
			.queryParam("limit", 2)
			.when()
			.get(this.baseUri.resolve("datastreams"))
			.andReturn();
		this.datastreamsBody = parseBody(this.datastreamsResponse);
	}

	/**
	 * SCENARIO-ETS-PART2-002-DATASTREAM-CONFORMANCE-DECLARED-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_DATASTREAM
			+ ": /conformance declares /conf/datastream before Datastream assertions run (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-DATASTREAM-CONFORMANCE-DECLARED-001)",
			groups = GROUP)
	public void datastreamConformanceDeclared() {
		ETSAssert.assertStatus(this.conformanceResponse, 200, REQ_DATASTREAM);
		if (this.conformanceBody == null) {
			ETSAssert.failWithUri(REQ_DATASTREAM, "/conformance body did not parse as JSON. Content-Type was: "
					+ this.conformanceResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.conformanceBody, "conformsTo", List.class, REQ_DATASTREAM);
		if (!declaresConformance(this.conformanceBody, CONF_DATASTREAM)) {
			throw new SkipException(CONF_DATASTREAM
					+ " - IUT does not declare the CS API Part 2 Datastreams & Observations conformance class.");
		}
	}

	/**
	 * SCENARIO-ETS-PART2-002-DEPENDENCY-SKIP-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_DATASTREAM
			+ ": full /conf/datastream closure is prerequisite-incomplete when /conf/api-common is absent (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-DEPENDENCY-SKIP-001)",
			groups = GROUP)
	public void datastreamApiCommonPrerequisiteVisibleForFullClosure() {
		skipIfDatastreamUndeclared();
		if (!declaresConformance(this.conformanceBody, CONF_PART2_API_COMMON)) {
			throw new SkipException(CONF_PART2_API_COMMON
					+ " - /req/datastream lists /req/api-common as a prerequisite. Scoped Datastream endpoint checks may run, but full /conf/datastream closure is prerequisite-incomplete.");
		}
		Reporter.log("IUT declares both /conf/datastream and /conf/api-common; full-class prerequisite is visible.",
				true);
	}

	/**
	 * SCENARIO-ETS-PART2-002-DATASTREAM-COLLECTION-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_RESOURCES_ENDPOINT
			+ ": /datastreams is a readable JSON resource collection (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-DATASTREAM-COLLECTION-READONLY-001)",
			groups = GROUP)
	public void datastreamsCollectionReadable() {
		skipIfDatastreamUndeclared();
		ETSAssert.assertStatus(this.datastreamsResponse, 200, REQ_RESOURCES_ENDPOINT);
		if (this.datastreamsBody == null) {
			ETSAssert.failWithUri(REQ_RESOURCES_ENDPOINT, "/datastreams body did not parse as JSON. Content-Type was: "
					+ this.datastreamsResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.datastreamsBody, "items", List.class, REQ_RESOURCES_ENDPOINT);
	}

	/**
	 * SCENARIO-ETS-PART2-002-DATASTREAM-COLLECTION-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CANONICAL_ENDPOINT
			+ ": /datastreams canonical endpoint exposes Datastream resources (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-DATASTREAM-COLLECTION-READONLY-001)",
			groups = GROUP)
	public void datastreamsCanonicalEndpointExposesDatastreamItems() {
		skipIfDatastreamUndeclared();
		Map<String, Object> datastream = selectedDatastream();
		assertDatastreamShape(datastream, REQ_CANONICAL_ENDPOINT);
	}

	/**
	 * SCENARIO-ETS-PART2-002-DATASTREAM-ITEM-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CANONICAL_URL
			+ ": selected Datastream is accessible through /datastreams/{id} canonical URL (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-DATASTREAM-ITEM-READONLY-001)",
			groups = GROUP)
	public void datastreamCanonicalResourceReadable() {
		skipIfDatastreamUndeclared();
		Map<String, Object> selected = selectedDatastream();
		String id = requireString(selected, "id", REQ_CANONICAL_URL);
		Response response = given().accept("application/json")
			.when()
			.get(this.baseUri.resolve("datastreams/" + id))
			.andReturn();
		ETSAssert.assertStatus(response, 200, REQ_CANONICAL_URL);
		Map<String, Object> body = parseBody(response);
		if (body == null) {
			ETSAssert.failWithUri(REQ_CANONICAL_URL, "/datastreams/" + id
					+ " body did not parse as JSON. Content-Type was: " + response.getContentType());
		}
		ETSAssert.assertJsonObjectHas(body, "id", String.class, REQ_CANONICAL_URL);
		if (!id.equals(body.get("id"))) {
			ETSAssert.failWithUri(REQ_CANONICAL_URL,
					"/datastreams/" + id + " returned Datastream id '" + body.get("id") + "'.");
		}
		assertDatastreamShape(body, REQ_CANONICAL_URL);
	}

	/**
	 * SCENARIO-ETS-PART2-002-DATASTREAM-SCHEMA-ENDPOINT-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_SCHEMA_OP
			+ ": selected Datastream exposes a readable /schema sub-resource (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-DATASTREAM-SCHEMA-ENDPOINT-001)",
			groups = GROUP)
	public void datastreamSchemaReadable() {
		skipIfDatastreamUndeclared();
		String id = requireString(selectedDatastream(), "id", REQ_SCHEMA_OP);
		Response response = given().accept("application/json")
			.when()
			.get(this.baseUri.resolve("datastreams/" + id + "/schema"))
			.andReturn();
		ETSAssert.assertStatus(response, 200, REQ_SCHEMA_OP);
		Map<String, Object> body = parseBody(response);
		if (body == null) {
			ETSAssert.failWithUri(REQ_SCHEMA_OP, "/datastreams/" + id
					+ "/schema body did not parse as JSON. Content-Type was: " + response.getContentType());
		}
		ETSAssert.assertJsonObjectHas(body, "obsFormat", String.class, REQ_SCHEMA_OP);
		ETSAssert.assertJsonObjectHas(body, "resultSchema", Map.class, REQ_SCHEMA_OP);
	}

	/**
	 * SCENARIO-ETS-PART2-002-OBSERVATION-ENDPOINTS-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_OBS_RESOURCES_ENDPOINT
			+ ": /observations and /datastreams/{id}/observations are readable JSON endpoint collections (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-OBSERVATION-ENDPOINTS-READONLY-001)",
			groups = GROUP)
	public void observationEndpointsReadable() {
		skipIfDatastreamUndeclared();
		Response observations = given().accept("application/json")
			.queryParam("limit", 2)
			.when()
			.get(this.baseUri.resolve("observations"))
			.andReturn();
		assertItemsCollection(observations, REQ_OBS_RESOURCES_ENDPOINT, "/observations");

		String id = requireString(selectedDatastream(), "id", REQ_OBS_RESOURCES_ENDPOINT);
		Response nested = given().accept("application/json")
			.queryParam("limit", 2)
			.when()
			.get(this.baseUri.resolve("datastreams/" + id + "/observations"))
			.andReturn();
		assertItemsCollection(nested, REQ_OBS_RESOURCES_ENDPOINT, "/datastreams/" + id + "/observations");
	}

	/**
	 * SCENARIO-ETS-PART2-002-OBSERVATION-ENDPOINTS-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_OBS_CANONICAL_ENDPOINT
			+ ": /observations canonical endpoint is readable using the Observation resources endpoint test (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-OBSERVATION-ENDPOINTS-READONLY-001)",
			groups = GROUP)
	public void observationsCanonicalEndpointReadable() {
		skipIfDatastreamUndeclared();
		Response response = given().accept("application/json")
			.queryParam("limit", 1)
			.when()
			.get(this.baseUri.resolve("observations"))
			.andReturn();
		assertItemsCollection(response, REQ_OBS_CANONICAL_ENDPOINT, "/observations");
	}

	/**
	 * SCENARIO-ETS-PART2-002-OBSERVATION-ENDPOINTS-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_OBS_CANONICAL_URL
			+ ": selected Observation is accessible through /observations/{id} canonical URL when the collection is populated (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-OBSERVATION-ENDPOINTS-READONLY-001)",
			groups = GROUP)
	public void observationCanonicalResourceReadableWhenCollectionPopulated() {
		skipIfDatastreamUndeclared();
		Response collection = given().accept("application/json")
			.queryParam("limit", 1)
			.when()
			.get(this.baseUri.resolve("observations"))
			.andReturn();
		Map<String, Object> collectionBody = assertItemsCollection(collection, REQ_OBS_CANONICAL_URL, "/observations");
		List<?> observations = items(collectionBody);
		if (observations.isEmpty()) {
			throw new SkipException(REQ_OBS_CANONICAL_URL
					+ " - /observations returned an empty collection; no Observation item is available for canonical URL checks.");
		}
		Object first = observations.get(0);
		if (!(first instanceof Map)) {
			ETSAssert.failWithUri(REQ_OBS_CANONICAL_URL, "/observations first item was not a JSON object: " + first);
		}
		String id = requireString(castMap(first), "id", REQ_OBS_CANONICAL_URL);
		Response response = given().accept("application/json")
			.when()
			.get(this.baseUri.resolve("observations/" + id))
			.andReturn();
		ETSAssert.assertStatus(response, 200, REQ_OBS_CANONICAL_URL);
		Map<String, Object> body = parseBody(response);
		if (body == null) {
			ETSAssert.failWithUri(REQ_OBS_CANONICAL_URL, "/observations/" + id
					+ " body did not parse as JSON. Content-Type was: " + response.getContentType());
		}
		if (!id.equals(body.get("id"))) {
			ETSAssert.failWithUri(REQ_OBS_CANONICAL_URL,
					"/observations/" + id + " returned Observation id '" + body.get("id") + "'.");
		}
		if (!hasObservationShape(body)) {
			ETSAssert.failWithUri(REQ_OBS_CANONICAL_URL,
					"Observation resource did not expose expected Observation-specific members: id and at least one of datastream@id, datastreamId, datastream, phenomenonTime, resultTime, or result.");
		}
	}

	/**
	 * SCENARIO-ETS-PART2-002-OBSERVATION-REFERENCE-EVIDENCE-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_OBS_REF_FROM_DATASTREAM
			+ ": Datastream-scoped Observation resources provide reference evidence when populated (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-OBSERVATION-REFERENCE-EVIDENCE-001)",
			groups = GROUP)
	public void observationsReferenceSelectedDatastreamWhenNestedCollectionPopulated() {
		skipIfDatastreamUndeclared();
		String id = requireString(selectedDatastream(), "id", REQ_OBS_REF_FROM_DATASTREAM);
		Response nested = given().accept("application/json")
			.queryParam("limit", 10)
			.when()
			.get(this.baseUri.resolve("datastreams/" + id + "/observations"))
			.andReturn();
		Map<String, Object> body = assertItemsCollection(nested, REQ_OBS_REF_FROM_DATASTREAM,
				"/datastreams/" + id + "/observations");
		List<?> items = items(body);
		if (items.isEmpty()) {
			throw new SkipException(REQ_OBS_REF_FROM_DATASTREAM + " - Datastream-scoped Observation collection for '"
					+ id + "' is empty; endpoint availability is not Observation reference PASS evidence.");
		}
		if (!items.stream().anyMatch(item -> observationReferencesDatastream(item, id))) {
			ETSAssert.failWithUri(REQ_OBS_REF_FROM_DATASTREAM, "/datastreams/" + id
					+ "/observations returned Observation items, but none contained datastream@id, datastreamId, datastream.id, or datastream link evidence for the selected Datastream.");
		}
	}

	/**
	 * SCENARIO-ETS-PART2-002-SYSTEM-REFERENCE-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_REF_FROM_SYSTEM
			+ ": selected Datastream's parent System exposes a Datastream sub-resource collection (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-SYSTEM-REFERENCE-READONLY-001)",
			groups = GROUP)
	public void systemScopedDatastreamsReadableWhenSystemReferencePresent() {
		skipIfDatastreamUndeclared();
		Map<String, Object> selected = selectedDatastream();
		String datastreamId = requireString(selected, "id", REQ_REF_FROM_SYSTEM);
		String systemId = stringValue(selected.get("system@id"));
		if (systemId == null || systemId.isBlank()) {
			throw new SkipException(REQ_REF_FROM_SYSTEM
					+ " - selected Datastream does not expose system@id for a bounded System sub-resource check.");
		}
		Response response = given().accept("application/json")
			.queryParam("limit", 100)
			.when()
			.get(this.baseUri.resolve("systems/" + systemId + "/datastreams"))
			.andReturn();
		Map<String, Object> body = assertItemsCollection(response, REQ_REF_FROM_SYSTEM,
				"/systems/" + systemId + "/datastreams");
		List<?> pageItems = items(body);
		if (!pageItems.isEmpty() && pageItems.stream().noneMatch(item -> datastreamId.equals(idOf(item)))) {
			throw new SkipException(REQ_REF_FROM_SYSTEM + " - /systems/" + systemId
					+ "/datastreams is readable, but the selected Datastream '" + datastreamId
					+ "' is not present in the bounded page returned by the IUT.");
		}
	}

	static boolean declaresConformance(Map<String, Object> body, String conformanceUri) {
		return Part2ApiCommonTests.declaresConformance(body, conformanceUri);
	}

	static boolean hasDatastreamShape(Map<String, Object> body) {
		if (body == null || !(body.get("id") instanceof String)) {
			return false;
		}
		boolean hasSystem = body.containsKey("system@id") || body.containsKey("system@link");
		return hasSystem && body.containsKey("outputName") && body.get("observedProperties") instanceof List
				&& body.get("formats") instanceof List && body.containsKey("resultType");
	}

	static boolean observationReferencesDatastream(Object observation, String datastreamId) {
		if (!(observation instanceof Map) || datastreamId == null || datastreamId.isBlank()) {
			return false;
		}
		Map<?, ?> obs = (Map<?, ?>) observation;
		if (datastreamId.equals(stringValue(obs.get("datastream@id")))
				|| datastreamId.equals(stringValue(obs.get("datastreamId")))) {
			return true;
		}
		Object datastream = obs.get("datastream");
		if (datastream instanceof Map && datastreamId.equals(stringValue(((Map<?, ?>) datastream).get("id")))) {
			return true;
		}
		return linksContainDatastream(obs.get("links"), datastreamId)
				|| linksContainDatastream(obs.get("datastream@link"), datastreamId);
	}

	static boolean hasItemsOnlyCollectionShape(Map<String, Object> body) {
		return body != null && body.get("items") instanceof List;
	}

	static boolean hasObservationShape(Map<String, Object> body) {
		return body != null && body.get("id") instanceof String
				&& (body.containsKey("datastream@id") || body.containsKey("datastreamId")
						|| body.containsKey("datastream") || body.containsKey("phenomenonTime")
						|| body.containsKey("resultTime") || body.containsKey("result"));
	}

	private void skipIfDatastreamUndeclared() {
		if (!declaresConformance(this.conformanceBody, CONF_DATASTREAM)) {
			throw new SkipException(CONF_DATASTREAM
					+ " - IUT does not declare the CS API Part 2 Datastreams & Observations conformance class in /conformance.");
		}
	}

	private Map<String, Object> selectedDatastream() {
		if (this.datastreamsBody == null) {
			ETSAssert.failWithUri(REQ_RESOURCES_ENDPOINT, "/datastreams body did not parse as JSON. Content-Type was: "
					+ this.datastreamsResponse.getContentType());
		}
		List<?> datastreams = items(this.datastreamsBody);
		if (datastreams.isEmpty()) {
			throw new SkipException(REQ_RESOURCES_ENDPOINT
					+ " - /datastreams returned an empty collection; no Datastream item is available for canonical read-only checks.");
		}
		Object first = datastreams.get(0);
		if (!(first instanceof Map)) {
			ETSAssert.failWithUri(REQ_RESOURCES_ENDPOINT, "/datastreams first item was not a JSON object: " + first);
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> datastream = (Map<String, Object>) first;
		return datastream;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> castMap(Object value) {
		return (Map<String, Object>) value;
	}

	private static List<?> items(Map<String, Object> body) {
		if (body == null || !(body.get("items") instanceof List)) {
			return List.of();
		}
		return (List<?>) body.get("items");
	}

	private static void assertDatastreamShape(Map<String, Object> body, String reqUri) {
		if (!hasDatastreamShape(body)) {
			ETSAssert.failWithUri(reqUri,
					"Datastream resource did not expose expected Datastream-specific members: id, system@id or system@link, outputName, observedProperties[], formats[], and resultType.");
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

	private static boolean linksContainDatastream(Object links, String datastreamId) {
		if (links instanceof Map) {
			return linkReferencesDatastream((Map<?, ?>) links, datastreamId);
		}
		if (!(links instanceof List)) {
			return false;
		}
		for (Object link : (List<?>) links) {
			if (link instanceof Map && linkReferencesDatastream((Map<?, ?>) link, datastreamId)) {
				return true;
			}
		}
		return false;
	}

	private static boolean linkReferencesDatastream(Map<?, ?> link, String datastreamId) {
		String href = stringValue(link.get("href"));
		return href != null && href.contains("/datastreams/" + datastreamId);
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
