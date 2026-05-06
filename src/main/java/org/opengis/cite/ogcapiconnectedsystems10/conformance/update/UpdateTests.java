package org.opengis.cite.ogcapiconnectedsystems10.conformance.update;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.TestRunArg;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.createreplacedelete.CreateReplaceDeleteTests;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Part 1 - Update/PATCH safety-gated systems subset ({@code /conf/update}; OGC
 * 23-001 Annex A).
 */
public class UpdateTests {

	static final String CONF_UPDATE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/update";

	static final String REQ_UPDATE_CLASS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/update";

	static final String REQ_UPDATE_SYSTEM = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/update/system";

	private static final String ENABLED_VALUE = "true";

	private static final String DEDICATED_MUTABLE_IUT_POLICY = "dedicated-mutable-iut";

	private URI iutUri;

	private String base;

	private Response conformanceResponse;

	private Map<String, Object> conformanceBody;

	private String mutationTestsEnabled;

	private String mutationIutPolicy;

	@BeforeClass
	public void fetchUpdateInputs(ITestContext testContext) {
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		this.iutUri = (URI) iutAttr;
		String iutString = this.iutUri.toString();
		this.base = iutString.endsWith("/") ? iutString : iutString + "/";

		this.mutationTestsEnabled = suiteString(testContext, SuiteAttribute.MUTATION_TESTS_ENABLED);
		this.mutationIutPolicy = suiteString(testContext, SuiteAttribute.MUTATION_IUT_POLICY);

		URI conformanceUri = URI.create(this.base + "conformance");
		this.conformanceResponse = given().accept("application/json").when().get(conformanceUri).andReturn();
		this.conformanceBody = parseBody(this.conformanceResponse);
		if (!declaresUpdateConformance()) {
			throw new SkipException(
					CONF_UPDATE + " - IUT does not declare the CS API Update conformance class in /conformance. "
							+ "Undeclared PATCH behavior is not conformance PASS evidence.");
		}
	}

	@Test(description = "OGC-23-001 " + REQ_UPDATE_CLASS
			+ ": /conformance declares /conf/update (REQ-ETS-PART1-011, SCENARIO-ETS-PART1-011-UPDATE-CONFORMANCE-DECLARED-001)",
			groups = "update")
	@SuppressWarnings("unchecked")
	public void updateConformanceDeclared() {
		ETSAssert.assertStatus(this.conformanceResponse, 200, REQ_UPDATE_CLASS);
		if (this.conformanceBody == null) {
			ETSAssert.failWithUri(REQ_UPDATE_CLASS, "/conformance body did not parse as JSON. Content-Type was: "
					+ this.conformanceResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.conformanceBody, "conformsTo", List.class, REQ_UPDATE_CLASS);
		List<Object> conformsTo = (List<Object>) this.conformanceBody.get("conformsTo");
		Predicate<Object> isUpdate = CONF_UPDATE::equals;
		ETSAssert.assertJsonArrayContains(conformsTo, isUpdate, CONF_UPDATE, REQ_UPDATE_CLASS);
	}

	@Test(description = "OGC-23-001 " + REQ_UPDATE_CLASS
			+ ": PATCH lifecycle tests are blocked unless mutation-tests-enabled=true and mutation-iut-policy=dedicated-mutable-iut (REQ-ETS-PART1-011, SCENARIO-ETS-PART1-011-UPDATE-MUTATION-SAFETY-GATE-001)",
			groups = "update")
	public void updateMutationSafetyGate() {
		ensureMutationEnabledOrSkip();
	}

	@Test(description = "OGC-23-001 " + REQ_UPDATE_SYSTEM
			+ ": OPTIONS /systems/{systemId} advertises PATCH readiness; readiness-only, not update conformance PASS (REQ-ETS-PART1-011, SCENARIO-ETS-PART1-011-UPDATE-SYSTEM-RESOURCE-OPTIONS-001)",
			groups = "update")
	public void systemResourceOptionsPatchReadinessPrecondition() {
		String systemId = firstSystemId();
		if (systemId == null || systemId.isBlank()) {
			throw new SkipException(REQ_UPDATE_SYSTEM
					+ " - /systems?limit=1 did not provide a system resource for OPTIONS readiness probing.");
		}
		Response response = optionsForSystem(systemId);
		assertOptionsStatus(response, REQ_UPDATE_SYSTEM);
		assertAllowHeaderContains(response, "PATCH", REQ_UPDATE_SYSTEM);
	}

	@Test(description = "OGC-23-001 " + REQ_UPDATE_SYSTEM
			+ ": when explicitly enabled against a dedicated mutable IUT, PATCH a temporary system resource (REQ-ETS-PART1-011, SCENARIO-ETS-PART1-011-UPDATE-SYSTEM-PATCH-LIFECYCLE-OPTIN-001)",
			groups = "update")
	public void systemsPatchLifecycleOptIn() {
		ensureMutationEnabledOrSkip();

		String seedSystemId = firstSystemId();
		if (seedSystemId == null || seedSystemId.isBlank()) {
			throw new SkipException(REQ_UPDATE_SYSTEM
					+ " - /systems?limit=1 did not provide a system resource for PATCH readiness probing.");
		}
		Response optionsResponse = optionsForSystem(seedSystemId);
		if (!allowHeaderContains(optionsResponse, "PATCH")) {
			throw new SkipException(REQ_UPDATE_SYSTEM
					+ " - IUT declares /conf/update, but OPTIONS /systems/{id} does not advertise PATCH. "
					+ "No PATCH request was issued.");
		}

		String systemUid = mutableSystemUid();
		Map<String, Object> createBody = CreateReplaceDeleteTests.mutableSystemBody("update-create", systemUid);
		Response createResponse = given().accept("application/json")
			.contentType("application/json")
			.body(createBody)
			.when()
			.post(URI.create(this.base + "systems"))
			.andReturn();
		assertStatusIn(createResponse, List.of(200, 201, 202), REQ_UPDATE_SYSTEM, "POST /systems");

		String resourceUri = createdResourceUri(createResponse);
		if (resourceUri == null) {
			ETSAssert.failWithUri(REQ_UPDATE_SYSTEM,
					"POST /systems did not expose the temporary resource URI needed for PATCH.");
		}

		try {
			String patchedName = "ETS Sprint 14 Update patch " + Instant.now();
			Map<String, Object> patchBody = Map.of("type", "Feature", "properties",
					Map.of("uid", systemUid, "name", patchedName));
			Response patchResponse = given().accept("application/json")
				.contentType("application/json")
				.body(patchBody)
				.when()
				.patch(URI.create(resourceUri))
				.andReturn();
			assertStatusIn(patchResponse, List.of(200, 204), REQ_UPDATE_SYSTEM, "PATCH " + resourceUri);

			Response getResponse = given().accept("application/json").when().get(URI.create(resourceUri)).andReturn();
			ETSAssert.assertStatus(getResponse, 200, REQ_UPDATE_SYSTEM);
			assertPatchedSystemName(parseBody(getResponse), patchedName);
		}
		finally {
			Response deleteResponse = given().accept("application/json")
				.when()
				.delete(URI.create(resourceUri))
				.andReturn();
			assertStatusIn(deleteResponse, List.of(200, 202, 204), REQ_UPDATE_SYSTEM, "DELETE " + resourceUri);
		}
	}

	@Test(description = "OGC-23-001 " + REQ_UPDATE_CLASS
			+ ": Update group runtime-cascade tracer for Update -> CreateReplaceDelete -> SystemFeatures -> Core (REQ-ETS-PART1-011, SCENARIO-ETS-PART1-011-UPDATE-DEPENDENCY-SMOKE-001)",
			groups = "update")
	public void updateDependencyCascadeRuntime() {
		ETSAssert.assertJsonObjectHas(Map.of("dependencyChain", "update->createreplacedelete->systemfeatures->core"),
				"dependencyChain", String.class, REQ_UPDATE_CLASS);
	}

	private void ensureMutationEnabledOrSkip() {
		if (!ENABLED_VALUE.equals(this.mutationTestsEnabled)
				|| !DEDICATED_MUTABLE_IUT_POLICY.equals(this.mutationIutPolicy)) {
			throw new SkipException(REQ_UPDATE_CLASS + " - IUT-bound PATCH lifecycle tests are disabled. Set "
					+ TestRunArg.MUTATION_TESTS_ENABLED + "=true and " + TestRunArg.MUTATION_IUT_POLICY
					+ "=dedicated-mutable-iut to permit PATCH. No PATCH request was issued.");
		}
		if (isPublicGeoRobotixIut()) {
			throw new SkipException(
					REQ_UPDATE_CLASS + " - known shared public GeoRobotix IUT is hard-denied for PATCH mutation tests. "
							+ "No PATCH request was issued.");
		}
	}

	private boolean isPublicGeoRobotixIut() {
		String host = this.iutUri.getHost();
		String uri = this.iutUri.toString();
		return "api.georobotix.io".equalsIgnoreCase(host)
				|| "https://api.georobotix.io/ogc/t18/api".equalsIgnoreCase(stripTrailingSlash(uri));
	}

	private String stripTrailingSlash(String value) {
		return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
	}

	@SuppressWarnings("unchecked")
	private boolean declaresUpdateConformance() {
		if (this.conformanceBody == null) {
			return false;
		}
		Object conformsToObj = this.conformanceBody.get("conformsTo");
		if (!(conformsToObj instanceof List)) {
			return false;
		}
		return ((List<Object>) conformsToObj).contains(CONF_UPDATE);
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

	private String suiteString(ITestContext testContext, SuiteAttribute attr) {
		Object value = testContext.getSuite().getAttribute(attr.getName());
		return value instanceof String ? (String) value : null;
	}

	private String firstSystemId() {
		Response systemsResponse = given().accept("application/json")
			.queryParam("limit", 1)
			.when()
			.get(URI.create(this.base + "systems"))
			.andReturn();
		ETSAssert.assertStatus(systemsResponse, 200, REQ_UPDATE_SYSTEM);
		Map<String, Object> body = parseBody(systemsResponse);
		Map<String, Object> first = firstItem(body);
		return systemId(first);
	}

	private Response optionsForSystem(String systemId) {
		return given().accept("*/*").when().options(URI.create(this.base + "systems/" + systemId)).andReturn();
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> firstItem(Map<String, Object> body) {
		if (body == null) {
			return null;
		}
		Object itemsObj = body.get("items");
		if (!(itemsObj instanceof List)) {
			return null;
		}
		List<?> items = (List<?>) itemsObj;
		if (items.isEmpty() || !(items.get(0) instanceof Map)) {
			return null;
		}
		return (Map<String, Object>) items.get(0);
	}

	@SuppressWarnings("unchecked")
	private String systemId(Object item) {
		if (!(item instanceof Map)) {
			return null;
		}
		return asString(((Map<String, Object>) item).get("id"));
	}

	private void assertOptionsStatus(Response response, String requirementUri) {
		assertStatusIn(response, List.of(200, 204), requirementUri, "OPTIONS request");
	}

	private void assertAllowHeaderContains(Response response, String method, String requirementUri) {
		if (!allowHeaderContains(response, method)) {
			ETSAssert.failWithUri(requirementUri, "Expected Allow header to advertise " + method
					+ " readiness; Allow was: " + response.getHeader("Allow"));
		}
	}

	private boolean allowHeaderContains(Response response, String method) {
		String allow = response.getHeader("Allow");
		return allow != null && Arrays.stream(allow.split(","))
			.map(String::trim)
			.map(value -> value.toUpperCase(Locale.ROOT))
			.anyMatch(method::equals);
	}

	private void assertStatusIn(Response response, List<Integer> expected, String requirementUri, String operation) {
		int actual = response.getStatusCode();
		if (!expected.contains(actual)) {
			ETSAssert.failWithUri(requirementUri,
					operation + " expected HTTP status in " + expected + ", got " + actual);
		}
	}

	private String mutableSystemUid() {
		return "urn:ets:ogcapi-connectedsystems10:update:" + UUID.randomUUID();
	}

	private String createdResourceUri(Response response) {
		String location = response.getHeader("Location");
		if (location != null && !location.isBlank()) {
			return CreateReplaceDeleteTests.resolveCreatedResourceUri(this.iutUri, this.base, location);
		}
		Map<String, Object> body = parseBody(response);
		String id = systemId(body);
		if (id == null || id.isBlank()) {
			return null;
		}
		return this.base + "systems/" + id;
	}

	private String asString(Object value) {
		return value instanceof String ? (String) value : null;
	}

	static void assertPatchedSystemName(Map<String, Object> systemBody, String expectedName) {
		String actualName = systemName(systemBody);
		if (!expectedName.equals(actualName)) {
			ETSAssert.failWithUri(REQ_UPDATE_SYSTEM,
					"PATCH status was accepted, but GET after PATCH did not expose the expected properties.name. "
							+ "Expected '" + expectedName + "', got '" + actualName + "'.");
		}
	}

	@SuppressWarnings("unchecked")
	static String systemName(Map<String, Object> systemBody) {
		if (systemBody == null) {
			return null;
		}
		Object propertiesObj = systemBody.get("properties");
		if (!(propertiesObj instanceof Map)) {
			return null;
		}
		return ((Map<String, Object>) propertiesObj).get("name") instanceof String
				? (String) ((Map<String, Object>) propertiesObj).get("name") : null;
	}

}
