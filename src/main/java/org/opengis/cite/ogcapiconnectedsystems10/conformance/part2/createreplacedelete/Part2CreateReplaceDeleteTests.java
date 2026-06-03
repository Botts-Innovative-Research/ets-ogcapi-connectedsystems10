package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.createreplacedelete;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.TestRunArg;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.Part2CandidateSelection;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.apicommon.Part2ApiCommonTests;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Part 2 - Create/Replace/Delete safety-gated conformance subset
 * ({@code /conf/create-replace-delete}; OGC 23-002 Annex A).
 *
 * <p>
 * Implements the first <strong>REQ-ETS-PART2-007</strong> increment: exact declaration,
 * visible OGC API Features Part 4 prerequisite, read-only OPTIONS readiness diagnostics,
 * unavailable-endpoint honesty, and public-IUT mutation hard-denial. Positive DataStream,
 * Observation, ControlStream, Command, Feasibility, and SystemEvent lifecycle mutation
 * paths remain deferred until a dedicated mutable-IUT fixture exists.
 * </p>
 */
public class Part2CreateReplaceDeleteTests {

	static final String GROUP = "part2createreplacedelete";

	static final String CONF_CREATE_REPLACE_DELETE = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/create-replace-delete";

	static final String CONF_FEATURES4_CREATE_REPLACE_DELETE = "http://www.opengis.net/spec/ogcapi-features-4/1.0/conf/create-replace-delete";

	static final String CONF_DATASTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/datastream";

	static final String CONF_CONTROLSTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/controlstream";

	static final String REQ_CREATE_REPLACE_DELETE = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/create-replace-delete";

	static final String REQ_DATASTREAM = REQ_CREATE_REPLACE_DELETE + "/datastream";

	static final String REQ_DATASTREAM_UPDATE_SCHEMA = REQ_CREATE_REPLACE_DELETE + "/datastream-update-schema";

	static final String REQ_DATASTREAM_DELETE_CASCADE = REQ_CREATE_REPLACE_DELETE + "/datastream-delete-cascade";

	static final String REQ_OBSERVATION = REQ_CREATE_REPLACE_DELETE + "/observation";

	static final String REQ_OBSERVATION_SCHEMA = REQ_CREATE_REPLACE_DELETE + "/observation-schema";

	static final String REQ_CONTROLSTREAM = REQ_CREATE_REPLACE_DELETE + "/controlstream";

	static final String REQ_CONTROLSTREAM_UPDATE_SCHEMA = REQ_CREATE_REPLACE_DELETE + "/controlstream-update-schema";

	static final String REQ_CONTROLSTREAM_DELETE_CASCADE = REQ_CREATE_REPLACE_DELETE + "/controlstream-delete-cascade";

	static final String REQ_COMMAND = REQ_CREATE_REPLACE_DELETE + "/command";

	static final String REQ_COMMAND_SCHEMA = REQ_CREATE_REPLACE_DELETE + "/command-schema";

	static final String REQ_COMMAND_STATUS = REQ_CREATE_REPLACE_DELETE + "/command-status";

	static final String REQ_COMMAND_RESULT = REQ_CREATE_REPLACE_DELETE + "/command-result";

	static final String REQ_FEASIBILITY = REQ_CREATE_REPLACE_DELETE + "/feasibility";

	static final String REQ_FEASIBILITY_STATUS = REQ_CREATE_REPLACE_DELETE + "/feasibility-status";

	static final String REQ_FEASIBILITY_RESULT = REQ_CREATE_REPLACE_DELETE + "/feasibility-result";

	static final String REQ_SYSTEM_EVENT = REQ_CREATE_REPLACE_DELETE + "/system-event";

	private static final String ENABLED_VALUE = "true";

	private static final String DEDICATED_MUTABLE_IUT_POLICY = "dedicated-mutable-iut";

	private URI iutUri;

	private URI baseUri;

	private Response conformanceResponse;

	private Map<String, Object> conformanceBody;

	private String mutationTestsEnabled;

	private String mutationIutPolicy;

	/**
	 * Fetches shared read-only inputs once. This class must not issue POST, PUT, PATCH,
	 * or DELETE unless a future lifecycle method has passed the explicit mutation gate.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchPart2CreateReplaceDeleteInputs(ITestContext testContext) {
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		this.iutUri = (URI) iutAttr;
		String iutString = this.iutUri.toString();
		this.baseUri = URI.create(iutString.endsWith("/") ? iutString : iutString + "/");

		this.mutationTestsEnabled = suiteString(testContext, SuiteAttribute.MUTATION_TESTS_ENABLED);
		this.mutationIutPolicy = suiteString(testContext, SuiteAttribute.MUTATION_IUT_POLICY);

		this.conformanceResponse = given().accept("application/json")
			.when()
			.get(this.baseUri.resolve("conformance"))
			.andReturn();
		this.conformanceBody = parseBody(this.conformanceResponse);
	}

	/**
	 * SCENARIO-ETS-PART2-007-CRD-CONFORMANCE-DECLARED-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CREATE_REPLACE_DELETE
			+ ": /conformance declares Part 2 /conf/create-replace-delete before CRD assertions run (REQ-ETS-PART2-007, SCENARIO-ETS-PART2-007-CRD-CONFORMANCE-DECLARED-001)",
			groups = GROUP)
	public void part2CreateReplaceDeleteConformanceDeclared() {
		ETSAssert.assertStatus(this.conformanceResponse, 200, REQ_CREATE_REPLACE_DELETE);
		if (this.conformanceBody == null) {
			ETSAssert.failWithUri(REQ_CREATE_REPLACE_DELETE,
					"/conformance body did not parse as JSON. Content-Type was: "
							+ this.conformanceResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.conformanceBody, "conformsTo", List.class, REQ_CREATE_REPLACE_DELETE);
		if (!declaresConformance(this.conformanceBody, CONF_CREATE_REPLACE_DELETE)) {
			throw new SkipException(CONF_CREATE_REPLACE_DELETE
					+ " - IUT does not declare the CS API Part 2 Create/Replace/Delete conformance class. "
					+ "Undeclared lifecycle behavior is not conformance PASS evidence.");
		}
	}

	/**
	 * SCENARIO-ETS-PART2-007-FEATURES4-PREREQUISITE-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CREATE_REPLACE_DELETE
			+ ": OGC API Features Part 4 Create/Replace/Delete prerequisite is visible before full class closure (REQ-ETS-PART2-007, SCENARIO-ETS-PART2-007-FEATURES4-PREREQUISITE-001)",
			groups = GROUP)
	public void featuresPart4CreateReplaceDeletePrerequisiteVisibleForFullClosure() {
		skipIfCreateReplaceDeleteUndeclared();
		if (!declaresConformance(this.conformanceBody, CONF_FEATURES4_CREATE_REPLACE_DELETE)) {
			throw new SkipException(CONF_FEATURES4_CREATE_REPLACE_DELETE
					+ " - /req/create-replace-delete lists OGC API Features Part 4 Create/Replace/Delete as a prerequisite. Full Part 2 /conf/create-replace-delete closure is prerequisite-incomplete.");
		}
		Reporter.log("IUT declares Part 2 /conf/create-replace-delete and the OGC API Features Part 4 prerequisite.",
				true);
	}

	/**
	 * SCENARIO-ETS-PART2-007-MUTATION-SAFETY-GATE-001.
	 * SCENARIO-ETS-PART2-007-SMOKE-NO-PUBLIC-MUTATION-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CREATE_REPLACE_DELETE
			+ ": mutation lifecycle checks are blocked unless mutation-tests-enabled=true and mutation-iut-policy=dedicated-mutable-iut, with public GeoRobotix hard-denied (REQ-ETS-PART2-007, SCENARIO-ETS-PART2-007-MUTATION-SAFETY-GATE-001, SCENARIO-ETS-PART2-007-SMOKE-NO-PUBLIC-MUTATION-001)",
			groups = GROUP)
	public void part2CreateReplaceDeleteMutationSafetyGate() {
		skipIfCreateReplaceDeleteUndeclared();
		ensureMutationEnabledOrSkip(REQ_CREATE_REPLACE_DELETE);
		Reporter.log(
				"Mutation opt-in accepted for a non-public dedicated mutable IUT; no lifecycle mutation was issued by this safety-gate check.",
				true);
	}

	/**
	 * SCENARIO-ETS-PART2-007-OPTIONS-READINESS-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_DATASTREAM + " and " + REQ_OBSERVATION
			+ ": DataStream and Observation OPTIONS checks are readiness-only and never lifecycle PASS evidence (REQ-ETS-PART2-007, SCENARIO-ETS-PART2-007-OPTIONS-READINESS-READONLY-001)",
			groups = GROUP)
	public void datastreamAndObservationOptionsReadinessIsReadOnly() {
		skipIfCreateReplaceDeleteUndeclared();
		skipIfClassUndeclared(CONF_DATASTREAM,
				"DataStream and Observation CRD readiness requires the Part 2 Datastream class.");
		Map<String, Object> datastream = preferredResourceWithChildCollection("datastreams", "observations",
				REQ_DATASTREAM);
		String datastreamId = resourceId(datastream, REQ_DATASTREAM, "selected DataStream");
		String systemId = associatedSystemId(datastream, REQ_DATASTREAM, "selected DataStream");
		assertOptionsAdvertises(systemScopedCollectionPath(systemId, "datastreams"), "POST", REQ_DATASTREAM);
		assertOptionsAdvertises(systemScopedResourcePath(systemId, "datastreams", datastreamId), "PUT", REQ_DATASTREAM);
		assertOptionsAdvertises(systemScopedResourcePath(systemId, "datastreams", datastreamId), "DELETE",
				REQ_DATASTREAM);
		assertOptionsAdvertises("datastreams/" + datastreamId, "PUT", REQ_DATASTREAM);
		assertOptionsAdvertises("datastreams/" + datastreamId, "DELETE", REQ_DATASTREAM);
		assertItemsCollection(readOnlyGet(datastreamScopedObservationsPath(datastreamId), REQ_OBSERVATION),
				REQ_OBSERVATION, "/" + datastreamScopedObservationsPath(datastreamId));
		assertOptionsAdvertises(datastreamScopedObservationsPath(datastreamId), "POST", REQ_OBSERVATION);
		Reporter.log(
				"OPTIONS readiness recorded for DataStream/Observation endpoints; no POST, PUT, DELETE, or PATCH was issued.",
				true);
	}

	/**
	 * SCENARIO-ETS-PART2-007-OPTIONS-READINESS-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CONTROLSTREAM + " and " + REQ_COMMAND
			+ ": ControlStream and nested Command OPTIONS checks are readiness-only and never lifecycle PASS evidence (REQ-ETS-PART2-007, SCENARIO-ETS-PART2-007-OPTIONS-READINESS-READONLY-001)",
			groups = GROUP)
	public void controlStreamAndNestedCommandOptionsReadinessIsReadOnly() {
		skipIfCreateReplaceDeleteUndeclared();
		skipIfClassUndeclared(CONF_CONTROLSTREAM,
				"ControlStream and Command CRD readiness requires the Part 2 ControlStream class.");
		Map<String, Object> controlStream = preferredResourceWithChildCollection("controlstreams", "commands",
				REQ_CONTROLSTREAM);
		String controlStreamId = resourceId(controlStream, REQ_CONTROLSTREAM, "selected ControlStream");
		String systemId = associatedSystemId(controlStream, REQ_CONTROLSTREAM, "selected ControlStream");
		assertOptionsAdvertises(systemScopedCollectionPath(systemId, "controlstreams"), "POST", REQ_CONTROLSTREAM);
		assertOptionsAdvertises(systemScopedResourcePath(systemId, "controlstreams", controlStreamId), "PUT",
				REQ_CONTROLSTREAM);
		assertOptionsAdvertises(systemScopedResourcePath(systemId, "controlstreams", controlStreamId), "DELETE",
				REQ_CONTROLSTREAM);
		assertOptionsAdvertises("controlstreams/" + controlStreamId, "PUT", REQ_CONTROLSTREAM);
		assertOptionsAdvertises("controlstreams/" + controlStreamId, "DELETE", REQ_CONTROLSTREAM);
		assertItemsCollection(readOnlyGet("controlstreams/" + controlStreamId + "/commands", REQ_COMMAND), REQ_COMMAND,
				"/controlstreams/" + controlStreamId + "/commands");
		assertOptionsAdvertises("controlstreams/" + controlStreamId + "/commands", "POST", REQ_COMMAND);
		Reporter.log(
				"OPTIONS readiness recorded for ControlStream/nested Command endpoints; no POST, PUT, DELETE, or PATCH was issued.",
				true);
	}

	/**
	 * SCENARIO-ETS-PART2-007-UNAVAILABLE-ENDPOINT-HONESTY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COMMAND + ", " + REQ_FEASIBILITY + ", and " + REQ_SYSTEM_EVENT
			+ ": unavailable /commands, /feasibility, /systemEvents, or /systems/{id}/events endpoints are SKIP evidence, not lifecycle PASS evidence (REQ-ETS-PART2-007, SCENARIO-ETS-PART2-007-UNAVAILABLE-ENDPOINT-HONESTY-001)",
			groups = GROUP)
	public void unavailableCommandFeasibilityAndEventEndpointsDoNotBecomeLifecycleEvidence() {
		skipIfCreateReplaceDeleteUndeclared();
		List<String> unavailable = new ArrayList<>();
		collectUnavailableEndpoint("commands", REQ_COMMAND, unavailable);
		collectUnavailableEndpoint("feasibility", REQ_FEASIBILITY, unavailable);
		collectUnavailableEndpoint("systemEvents", REQ_SYSTEM_EVENT, unavailable);
		String systemId = firstResourceIdOrNull("systems");
		if (systemId == null) {
			unavailable.add("GET /systems?limit=1 did not expose a System id for /systems/{id}/events probing");
		}
		else {
			collectUnavailableEndpoint("systems/" + systemId + "/events", REQ_SYSTEM_EVENT, unavailable);
		}
		if (!unavailable.isEmpty()) {
			throw new SkipException(REQ_CREATE_REPLACE_DELETE
					+ " - unavailable or non-JSON endpoints are not Part 2 CRD lifecycle PASS evidence: "
					+ String.join("; ", unavailable));
		}
		Reporter.log(
				"Command, Feasibility, and SystemEvent endpoints exposed HTTP 200 JSON collection evidence before any lifecycle assertion.",
				true);
	}

	/**
	 * SCENARIO-ETS-PART2-007-DATASTREAM-OBSERVATION-LIFECYCLE-OPTIN-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_DATASTREAM + ", " + REQ_OBSERVATION + ", and " + REQ_OBSERVATION_SCHEMA
			+ ": DataStream/Observation positive lifecycle checks require a dedicated mutable IUT and are deferred in this safety-gated increment (REQ-ETS-PART2-007, SCENARIO-ETS-PART2-007-DATASTREAM-OBSERVATION-LIFECYCLE-OPTIN-001)",
			groups = GROUP)
	public void datastreamObservationLifecycleRequiresDedicatedMutableIut() {
		skipIfCreateReplaceDeleteUndeclared();
		ensureMutationEnabledOrSkip(REQ_DATASTREAM);
		throw new SkipException(REQ_DATASTREAM
				+ " - positive DataStream/Observation POST/PUT/DELETE lifecycle coverage is deferred until dedicated mutable-IUT fixtures and cleanup are implemented. No POST/PUT/DELETE request was issued.");
	}

	/**
	 * SCENARIO-ETS-PART2-007-CONTROLSTREAM-COMMAND-LIFECYCLE-OPTIN-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CONTROLSTREAM + ", " + REQ_COMMAND + ", " + REQ_COMMAND_SCHEMA + ", "
			+ REQ_COMMAND_STATUS + ", and " + REQ_COMMAND_RESULT
			+ ": ControlStream/Command positive lifecycle checks require a dedicated mutable IUT and are deferred in this safety-gated increment (REQ-ETS-PART2-007, SCENARIO-ETS-PART2-007-CONTROLSTREAM-COMMAND-LIFECYCLE-OPTIN-001)",
			groups = GROUP)
	public void controlStreamCommandLifecycleRequiresDedicatedMutableIut() {
		skipIfCreateReplaceDeleteUndeclared();
		ensureMutationEnabledOrSkip(REQ_CONTROLSTREAM);
		throw new SkipException(REQ_CONTROLSTREAM
				+ " - positive ControlStream/Command POST/PUT/DELETE lifecycle coverage is deferred until dedicated mutable-IUT fixtures and cleanup are implemented. No POST/PUT/DELETE request was issued.");
	}

	/**
	 * SCENARIO-ETS-PART2-007-FEASIBILITY-SYSTEMEVENT-LIFECYCLE-OPTIN-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_FEASIBILITY + ", " + REQ_FEASIBILITY_STATUS + ", " + REQ_FEASIBILITY_RESULT
			+ ", and " + REQ_SYSTEM_EVENT
			+ ": Feasibility/SystemEvent positive lifecycle checks require a dedicated mutable IUT and are deferred in this safety-gated increment (REQ-ETS-PART2-007, SCENARIO-ETS-PART2-007-FEASIBILITY-SYSTEMEVENT-LIFECYCLE-OPTIN-001)",
			groups = GROUP)
	public void feasibilitySystemEventLifecycleRequiresDedicatedMutableIut() {
		skipIfCreateReplaceDeleteUndeclared();
		ensureMutationEnabledOrSkip(REQ_FEASIBILITY);
		throw new SkipException(REQ_FEASIBILITY
				+ " - positive Feasibility/SystemEvent POST/PUT/DELETE lifecycle coverage is deferred until dedicated mutable-IUT fixtures and cleanup are implemented. No POST/PUT/DELETE request was issued.");
	}

	static boolean declaresConformance(Map<String, Object> body, String conformanceUri) {
		return Part2ApiCommonTests.declaresConformance(body, conformanceUri);
	}

	static boolean isPublicGeoRobotixIut(URI iutUri) {
		if (iutUri == null) {
			return false;
		}
		String host = iutUri.getHost();
		String uri = stripTrailingSlash(iutUri.toString());
		return "api.georobotix.io".equalsIgnoreCase(host)
				|| "https://api.georobotix.io/ogc/t18/api".equalsIgnoreCase(uri);
	}

	static String mutationGateSkipReason(URI iutUri, String mutationTestsEnabled, String mutationIutPolicy,
			String requirementUri) {
		if (isPublicGeoRobotixIut(iutUri)) {
			return requirementUri
					+ " - known shared public GeoRobotix IUT is hard-denied for Part 2 CRD mutation tests. "
					+ "No POST/PUT/DELETE/PATCH request was issued.";
		}
		if (!ENABLED_VALUE.equals(mutationTestsEnabled) || !DEDICATED_MUTABLE_IUT_POLICY.equals(mutationIutPolicy)) {
			return requirementUri + " - IUT-bound mutation lifecycle tests are disabled. Set "
					+ TestRunArg.MUTATION_TESTS_ENABLED + "=true and " + TestRunArg.MUTATION_IUT_POLICY
					+ "=dedicated-mutable-iut to permit POST/PUT/DELETE lifecycle checks. No POST/PUT/DELETE/PATCH request was issued.";
		}
		return null;
	}

	static boolean allowHeaderContains(String allow, String method) {
		if (allow == null || method == null) {
			return false;
		}
		return Arrays.stream(allow.split(","))
			.map(String::trim)
			.map(value -> value.toUpperCase(Locale.ROOT))
			.anyMatch(method.toUpperCase(Locale.ROOT)::equals);
	}

	static boolean hasItemsOnlyCollectionShape(Map<String, Object> body) {
		return body != null && body.get("items") instanceof List;
	}

	static String systemScopedCollectionPath(String systemId, String collectionName) {
		return "systems/" + systemId + "/" + collectionName;
	}

	static String systemScopedResourcePath(String systemId, String collectionName, String resourceId) {
		return systemScopedCollectionPath(systemId, collectionName) + "/" + resourceId;
	}

	static String datastreamScopedObservationsPath(String datastreamId) {
		return "datastreams/" + datastreamId + "/observations";
	}

	static String associatedSystemId(Map<String, Object> resource) {
		if (resource == null) {
			return null;
		}
		String systemId = stringValue(resource.get("system@id"));
		if (systemId != null && !systemId.isBlank()) {
			return systemId;
		}
		systemId = stringValue(resource.get("systemId"));
		if (systemId != null && !systemId.isBlank()) {
			return systemId;
		}
		Object system = resource.get("system");
		if (system instanceof Map) {
			return stringValue(((Map<?, ?>) system).get("id"));
		}
		return null;
	}

	private void skipIfCreateReplaceDeleteUndeclared() {
		if (!declaresConformance(this.conformanceBody, CONF_CREATE_REPLACE_DELETE)) {
			throw new SkipException(CONF_CREATE_REPLACE_DELETE
					+ " - IUT does not declare the CS API Part 2 Create/Replace/Delete conformance class in /conformance.");
		}
	}

	private void skipIfClassUndeclared(String conformanceUri, String reason) {
		if (!declaresConformance(this.conformanceBody, conformanceUri)) {
			throw new SkipException(conformanceUri + " - " + reason);
		}
	}

	private void ensureMutationEnabledOrSkip(String requirementUri) {
		String reason = mutationGateSkipReason(this.iutUri, this.mutationTestsEnabled, this.mutationIutPolicy,
				requirementUri);
		if (reason != null) {
			throw new SkipException(reason);
		}
	}

	private Response readOnlyGet(String path, String requirementUri) {
		Response response = given().accept("application/json")
			.queryParam("limit", 2)
			.when()
			.get(this.baseUri.resolve(path))
			.andReturn();
		if (response.getStatusCode() != 200) {
			throw new SkipException(requirementUri + " - GET /" + path + "?limit=2 returned HTTP "
					+ response.getStatusCode() + "; endpoint availability is not lifecycle PASS evidence.");
		}
		return response;
	}

	private void assertOptionsAdvertises(String path, String method, String requirementUri) {
		Response response = given().accept("*/*").when().options(this.baseUri.resolve(path)).andReturn();
		assertStatusIn(response, List.of(200, 204), requirementUri, "OPTIONS /" + path);
		String allow = response.getHeader("Allow");
		if (!allowHeaderContains(allow, method)) {
			ETSAssert.failWithUri(requirementUri,
					"OPTIONS /" + path + " did not advertise " + method + " readiness; Allow was: " + allow);
		}
	}

	private void collectUnavailableEndpoint(String path, String requirementUri, List<String> unavailable) {
		Response response = given().accept("application/json")
			.queryParam("limit", 1)
			.when()
			.get(this.baseUri.resolve(path))
			.andReturn();
		if (response.getStatusCode() != 200) {
			unavailable.add("GET /" + path + "?limit=1 returned HTTP " + response.getStatusCode());
			return;
		}
		Map<String, Object> body = parseBody(response);
		if (!hasItemsOnlyCollectionShape(body)) {
			unavailable
				.add("GET /" + path + "?limit=1 did not expose JSON items[] collection shape for " + requirementUri);
		}
	}

	private Map<String, Object> firstResource(String collectionPath, String requirementUri) {
		return resources(collectionPath, requirementUri).get(0);
	}

	private Map<String, Object> preferredResourceWithChildCollection(String collectionPath, String childCollection,
			String requirementUri) {
		List<Map<String, Object>> resources = resources(collectionPath, requirementUri);
		Part2CandidateSelection.ParentChild selected = Part2CandidateSelection.firstParentWithChild(resources,
				parent -> {
					String id = stringValue(parent.get("id"));
					if (id == null || id.isBlank()) {
						return null;
					}
					return firstChildItemOrNull(collectionPath + "/" + id + "/" + childCollection);
				});
		return selected == null ? resources.get(0) : selected.parent();
	}

	private List<Map<String, Object>> resources(String collectionPath, String requirementUri) {
		Response response = given().accept("application/json")
			.queryParam("limit", Part2CandidateSelection.CANDIDATE_PAGE_LIMIT)
			.when()
			.get(this.baseUri.resolve(collectionPath))
			.andReturn();
		if (response.getStatusCode() != 200) {
			throw new SkipException(requirementUri + " - /" + collectionPath + " returned HTTP "
					+ response.getStatusCode() + "; no resource is available for read-only OPTIONS probing.");
		}
		List<Map<String, Object>> resources = Part2CandidateSelection.objectItems(parseBody(response));
		if (resources.isEmpty()) {
			throw new SkipException(requirementUri + " - /" + collectionPath
					+ " did not expose a resource item for read-only OPTIONS probing.");
		}
		return resources;
	}

	private Map<String, Object> firstChildItemOrNull(String path) {
		Response response = given().accept("application/json")
			.queryParam("limit", 1)
			.when()
			.get(this.baseUri.resolve(path))
			.andReturn();
		if (response.getStatusCode() != 200) {
			return null;
		}
		Map<String, Object> body = parseBody(response);
		if (!hasItemsOnlyCollectionShape(body)) {
			return null;
		}
		List<Map<String, Object>> childItems = Part2CandidateSelection.objectItems(body);
		return childItems.isEmpty() ? null : childItems.get(0);
	}

	private String resourceId(Map<String, Object> resource, String requirementUri, String label) {
		String id = stringValue(resource.get("id"));
		if (id == null || id.isBlank()) {
			throw new SkipException(
					requirementUri + " - " + label + " did not expose an id for read-only OPTIONS probing.");
		}
		return id;
	}

	private String associatedSystemId(Map<String, Object> resource, String requirementUri, String label) {
		String systemId = associatedSystemId(resource);
		if (systemId == null || systemId.isBlank()) {
			throw new SkipException(requirementUri + " - " + label
					+ " did not expose system@id, systemId, or system.id; the normative system-scoped Create/Replace/Delete endpoint cannot be probed without inventing a parent System.");
		}
		return systemId;
	}

	private String firstResourceIdOrNull(String collectionPath) {
		Response response = given().accept("application/json")
			.queryParam("limit", 1)
			.when()
			.get(this.baseUri.resolve(collectionPath))
			.andReturn();
		if (response.getStatusCode() != 200) {
			return null;
		}
		Map<String, Object> body = parseBody(response);
		List<?> items = items(body);
		if (items.isEmpty() || !(items.get(0) instanceof Map)) {
			return null;
		}
		return stringValue(((Map<?, ?>) items.get(0)).get("id"));
	}

	private Map<String, Object> assertItemsCollection(Response response, String requirementUri, String label) {
		ETSAssert.assertStatus(response, 200, requirementUri);
		Map<String, Object> body = parseBody(response);
		if (!hasItemsOnlyCollectionShape(body)) {
			ETSAssert.failWithUri(requirementUri, label + " did not expose a JSON object with an items[] array.");
		}
		return body;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> parseBody(Response response) {
		try {
			return response.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private List<?> items(Map<String, Object> body) {
		if (body == null || !(body.get("items") instanceof List)) {
			return List.of();
		}
		return (List<?>) body.get("items");
	}

	private String suiteString(ITestContext testContext, SuiteAttribute attr) {
		Object value = testContext.getSuite().getAttribute(attr.getName());
		return value instanceof String ? (String) value : null;
	}

	private static String stripTrailingSlash(String value) {
		return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
	}

	private static String stringValue(Object value) {
		return value instanceof String ? (String) value : null;
	}

	private void assertStatusIn(Response response, List<Integer> expected, String requirementUri, String operation) {
		int actual = response.getStatusCode();
		if (!expected.contains(actual)) {
			ETSAssert.failWithUri(requirementUri,
					operation + " expected HTTP status in " + expected + ", got " + actual);
		}
	}

}
