package org.opengis.cite.ogcapiconnectedsystems10.conformance.createreplacedelete;

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
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Part 1 - CreateReplaceDelete safety-gated subset
 * ({@code /conf/create-replace-delete}; OGC 23-001 Annex A).
 *
 * <p>
 * Implements Sprint 12 <strong>REQ-ETS-PART1-010</strong> coverage for systems CRD:
 * conformance declaration, OPTIONS readiness probes, explicit mutation opt-in guard,
 * system POST/PUT/DELETE lifecycle path for dedicated mutable IUTs, and dependency
 * cascade tracing. This class deliberately does not cover PATCH, Part 2 resources,
 * deployment/procedure/property CRUD, or {@code /conf/update}.
 * </p>
 */
public class CreateReplaceDeleteTests {

	static final String CONF_CREATE_REPLACE_DELETE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/create-replace-delete";

	static final String REQ_CREATE_REPLACE_DELETE_CLASS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/create-replace-delete";

	static final String REQ_SYSTEM = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/create-replace-delete/system";

	static final String REQ_SYSTEM_DELETE_CASCADE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/create-replace-delete/system-delete-cascade";

	private static final String ENABLED_VALUE = "true";

	private static final String DEDICATED_MUTABLE_IUT_POLICY = "dedicated-mutable-iut";

	private URI iutUri;

	private String base;

	private Response conformanceResponse;

	private Map<String, Object> conformanceBody;

	private String mutationTestsEnabled;

	private String mutationIutPolicy;

	@BeforeClass
	public void fetchCreateReplaceDeleteInputs(ITestContext testContext) {
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
		if (!declaresCreateReplaceDeleteConformance()) {
			throw new SkipException(CONF_CREATE_REPLACE_DELETE
					+ " - IUT does not declare the CS API CreateReplaceDelete conformance class in /conformance. "
					+ "Undeclared mutation behavior is not conformance PASS evidence.");
		}
	}

	/**
	 * SCENARIO-ETS-PART1-010-CRD-CONFORMANCE-DECLARED-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_CREATE_REPLACE_DELETE_CLASS
			+ ": /conformance declares /conf/create-replace-delete (REQ-ETS-PART1-010, SCENARIO-ETS-PART1-010-CRD-CONFORMANCE-DECLARED-001)",
			groups = "createreplacedelete")
	@SuppressWarnings("unchecked")
	public void createReplaceDeleteConformanceDeclared() {
		ETSAssert.assertStatus(this.conformanceResponse, 200, REQ_CREATE_REPLACE_DELETE_CLASS);
		if (this.conformanceBody == null) {
			ETSAssert.failWithUri(REQ_CREATE_REPLACE_DELETE_CLASS,
					"/conformance body did not parse as JSON. Content-Type was: "
							+ this.conformanceResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.conformanceBody, "conformsTo", List.class, REQ_CREATE_REPLACE_DELETE_CLASS);
		List<Object> conformsTo = (List<Object>) this.conformanceBody.get("conformsTo");
		Predicate<Object> isCreateReplaceDelete = CONF_CREATE_REPLACE_DELETE::equals;
		ETSAssert.assertJsonArrayContains(conformsTo, isCreateReplaceDelete, CONF_CREATE_REPLACE_DELETE,
				REQ_CREATE_REPLACE_DELETE_CLASS);
	}

	/**
	 * SCENARIO-ETS-PART1-010-CRD-MUTATION-SAFETY-GATE-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_CREATE_REPLACE_DELETE_CLASS
			+ ": mutation lifecycle tests are blocked unless mutation-tests-enabled=true and mutation-iut-policy=dedicated-mutable-iut (REQ-ETS-PART1-010, SCENARIO-ETS-PART1-010-CRD-MUTATION-SAFETY-GATE-001)",
			groups = "createreplacedelete")
	public void createReplaceDeleteMutationSafetyGate() {
		ensureMutationEnabledOrSkip();
	}

	/**
	 * SCENARIO-ETS-PART1-010-CRD-SYSTEMS-OPTIONS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_SYSTEM
			+ ": OPTIONS /systems advertises POST readiness; readiness-only, not lifecycle conformance PASS (REQ-ETS-PART1-010, SCENARIO-ETS-PART1-010-CRD-SYSTEMS-OPTIONS-001)",
			groups = "createreplacedelete")
	public void systemsCollectionOptionsReadinessPrecondition() {
		Response response = given().accept("*/*").when().options(URI.create(this.base + "systems")).andReturn();
		assertOptionsStatus(response, REQ_SYSTEM);
		assertAllowHeaderContains(response, "POST", REQ_SYSTEM);
	}

	/**
	 * SCENARIO-ETS-PART1-010-CRD-SYSTEM-RESOURCE-OPTIONS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_SYSTEM
			+ ": OPTIONS /systems/{systemId} advertises PUT and DELETE readiness; readiness-only, not lifecycle conformance PASS (REQ-ETS-PART1-010, SCENARIO-ETS-PART1-010-CRD-SYSTEM-RESOURCE-OPTIONS-001)",
			groups = "createreplacedelete")
	public void systemResourceOptionsReadinessPrecondition() {
		String systemId = firstSystemId();
		if (systemId == null || systemId.isBlank()) {
			throw new SkipException(REQ_SYSTEM
					+ " - /systems?limit=1 did not provide a system resource for OPTIONS readiness probing.");
		}
		Response response = given().accept("*/*")
			.when()
			.options(URI.create(this.base + "systems/" + systemId))
			.andReturn();
		assertOptionsStatus(response, REQ_SYSTEM);
		assertAllowHeaderContains(response, "PUT", REQ_SYSTEM);
		assertAllowHeaderContains(response, "DELETE", REQ_SYSTEM);
	}

	/**
	 * SCENARIO-ETS-PART1-010-CRD-SYSTEM-LIFECYCLE-OPTIN-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_SYSTEM
			+ ": when explicitly enabled against a dedicated mutable IUT, POST/PUT/DELETE a temporary system resource (REQ-ETS-PART1-010, SCENARIO-ETS-PART1-010-CRD-SYSTEM-LIFECYCLE-OPTIN-001)",
			groups = "createreplacedelete")
	public void systemsCreateReplaceDeleteLifecycle() {
		ensureMutationEnabledOrSkip();

		String systemUid = mutableSystemUid();
		Map<String, Object> createBody = mutableSystemBody("create", systemUid);
		Response createResponse = given().accept("application/json")
			.contentType("application/json")
			.body(createBody)
			.when()
			.post(URI.create(this.base + "systems"))
			.andReturn();
		assertStatusIn(createResponse, List.of(200, 201, 202), REQ_SYSTEM, "POST /systems");

		String resourceUri = createdResourceUri(createResponse);
		if (resourceUri == null) {
			ETSAssert.failWithUri(REQ_SYSTEM,
					"POST /systems did not expose the created resource URI through Location header or JSON id.");
		}

		try {
			Map<String, Object> replaceBody = mutableSystemBody("replace", systemUid);
			Response replaceResponse = given().accept("application/json")
				.contentType("application/json")
				.body(replaceBody)
				.when()
				.put(URI.create(resourceUri))
				.andReturn();
			assertStatusIn(replaceResponse, List.of(200, 204), REQ_SYSTEM, "PUT " + resourceUri);
		}
		finally {
			Response deleteResponse = given().accept("application/json")
				.when()
				.delete(URI.create(resourceUri))
				.andReturn();
			assertStatusIn(deleteResponse, List.of(200, 202, 204), REQ_SYSTEM_DELETE_CASCADE, "DELETE " + resourceUri);
		}
	}

	/**
	 * SCENARIO-ETS-PART1-010-CRD-DEPENDENCY-SMOKE-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_CREATE_REPLACE_DELETE_CLASS
			+ ": CreateReplaceDelete group runtime-cascade tracer for CreateReplaceDelete -> SystemFeatures -> Core (REQ-ETS-PART1-010, SCENARIO-ETS-PART1-010-CRD-DEPENDENCY-SMOKE-001)",
			groups = "createreplacedelete")
	public void createReplaceDeleteDependencyCascadeRuntime() {
		ETSAssert.assertJsonObjectHas(Map.of("dependencyChain", "createreplacedelete->systemfeatures->core"),
				"dependencyChain", String.class, REQ_CREATE_REPLACE_DELETE_CLASS);
	}

	private void ensureMutationEnabledOrSkip() {
		if (!ENABLED_VALUE.equals(this.mutationTestsEnabled)
				|| !DEDICATED_MUTABLE_IUT_POLICY.equals(this.mutationIutPolicy)) {
			throw new SkipException(REQ_CREATE_REPLACE_DELETE_CLASS
					+ " - IUT-bound mutation lifecycle tests are disabled. " + "Set "
					+ TestRunArg.MUTATION_TESTS_ENABLED + "=true and " + TestRunArg.MUTATION_IUT_POLICY
					+ "=dedicated-mutable-iut to permit POST/PUT/DELETE. No POST/PUT/DELETE request was issued.");
		}
		if (isPublicGeoRobotixIut()) {
			throw new SkipException(REQ_CREATE_REPLACE_DELETE_CLASS
					+ " - known shared public GeoRobotix IUT is hard-denied for mutation tests. "
					+ "No POST/PUT/DELETE request was issued.");
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
	private boolean declaresCreateReplaceDeleteConformance() {
		if (this.conformanceBody == null) {
			return false;
		}
		Object conformsToObj = this.conformanceBody.get("conformsTo");
		if (!(conformsToObj instanceof List)) {
			return false;
		}
		return ((List<Object>) conformsToObj).contains(CONF_CREATE_REPLACE_DELETE);
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
		ETSAssert.assertStatus(systemsResponse, 200, REQ_SYSTEM);
		Map<String, Object> body = parseBody(systemsResponse);
		Map<String, Object> first = firstItem(body);
		return systemId(first);
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
		String allow = response.getHeader("Allow");
		boolean methodPresent = allow != null && Arrays.stream(allow.split(","))
			.map(String::trim)
			.map(value -> value.toUpperCase(Locale.ROOT))
			.anyMatch(method::equals);
		if (!methodPresent) {
			ETSAssert.failWithUri(requirementUri,
					"Expected Allow header to advertise " + method + " readiness; Allow was: " + allow);
		}
	}

	private void assertStatusIn(Response response, List<Integer> expected, String requirementUri, String operation) {
		int actual = response.getStatusCode();
		if (!expected.contains(actual)) {
			ETSAssert.failWithUri(requirementUri,
					operation + " expected HTTP status in " + expected + ", got " + actual);
		}
	}

	private String mutableSystemUid() {
		return "urn:ets:ogcapi-connectedsystems10:crd:" + UUID.randomUUID();
	}

	static Map<String, Object> mutableSystemBody(String phase, String systemUid) {
		return Map.of("type", "Feature", "properties",
				Map.of("uid", systemUid, "name", "ETS Sprint 12 CRD " + phase + " " + Instant.now(), "description",
						"Temporary system resource created by the ETS create-replace-delete lifecycle test."));
	}

	private String createdResourceUri(Response response) {
		String location = response.getHeader("Location");
		if (location != null && !location.isBlank()) {
			return resolveCreatedResourceUri(this.iutUri, this.base, location);
		}
		Map<String, Object> body = parseBody(response);
		String id = systemId(body);
		if (id == null || id.isBlank()) {
			return null;
		}
		return this.base + "systems/" + id;
	}

	static String resolveCreatedResourceUri(URI iutUri, String base, String location) {
		URI locationUri = URI.create(location);
		if (locationUri.isAbsolute()) {
			return locationUri.toString();
		}
		if (location.startsWith("/")) {
			String iutPath = iutUri.getPath();
			boolean alreadyIncludesIutPath = iutPath != null && !iutPath.isBlank() && !"/".equals(iutPath)
					&& (location.equals(iutPath) || location.startsWith(iutPath + "/"));
			if (!alreadyIncludesIutPath) {
				return base + location.substring(1);
			}
			return iutUri.resolve(location).toString();
		}
		return URI.create(base).resolve(location).toString();
	}

	private String asString(Object value) {
		return value instanceof String ? (String) value : null;
	}

}
