package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.apicommon;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonTests;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Part 2 - API Common conformance subset tests ({@code /conf/api-common}; OGC
 * 23-002 Annex A).
 *
 * <p>
 * Implements the Sprint 20 read-only, declaration-gated subset of
 * <strong>REQ-ETS-PART2-001</strong>. This class intentionally does not close the full
 * Part 2 surface: JSON payload classes, stream semantics, write behavior, and full
 * endpoint parity remain open for later Part 2 sprints.
 * </p>
 */
public class Part2ApiCommonTests {

	static final String GROUP = "part2apicommon";

	public static final String CONF_PART2_API_COMMON = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/api-common";

	public static final String REQ_API_COMMON = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/api-common";

	static final String REQ_RESOURCES = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/api-common/resources";

	static final String REQ_RESOURCE_COLLECTION = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/api-common/resource-collection";

	private static final String DATETIME_EVIDENCE_METHOD = "datetimeUsesValidTime";

	private static final Set<String> PART2_COLLECTION_TOKENS = Set.of("datastreams", "observations", "controlstreams",
			"commands", "systemevents");

	private URI apiRoot;

	/**
	 * Loads only immutable suite arguments after inherited API Common prerequisites.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass(dependsOnGroups = "part1apicommon", alwaysRun = true)
	public void fetchPart2ApiCommonInputs(ITestContext testContext) {
		skipWhenPrerequisiteUnsatisfied(testContext);
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		configure((URI) iutAttr);
	}

	void configure(URI iut) {
		if (iut == null || !iut.isAbsolute()) {
			throw new IllegalArgumentException("IUT must be an absolute URI.");
		}
		String iutString = iut.toString();
		this.apiRoot = URI.create(iutString.endsWith("/") ? iutString : iutString + "/");
	}

	/**
	 * REQ-ETS-PART2-001; SCENARIO-ETS-PART2-001-RELEASED-RESOURCES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_RESOURCES
			+ ": landing page advertises same-origin Part 2 resource collection links (REQ-ETS-PART2-001, SCENARIO-ETS-PART2-001-RELEASED-RESOURCES-001)",
			groups = GROUP, alwaysRun = true)
	public void part2ApiCommonResourcesAreDiscoverable() {
		requirePart2ApiCommonDeclaration(REQ_RESOURCES);
		Response landingResponse = given().accept("application/json").when().get(this.apiRoot).andReturn();
		ETSAssert.assertStatus(landingResponse, 200, REQ_RESOURCES);
		Map<String, Object> landingBody = parseBody(landingResponse);
		if (landingBody == null) {
			ETSAssert.failWithUri(REQ_RESOURCES,
					"landing page body did not parse as JSON. Content-Type was: " + landingResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(landingBody, "links", List.class, REQ_RESOURCES);
		List<URI> collectionUris = discoverPart2CollectionUris(landingBody, this.apiRoot);
		if (collectionUris.isEmpty()) {
			throw new SkipException(REQ_RESOURCES
					+ " - landing page did not advertise any same-origin Part 2 resource collection links.");
		}
		Reporter.log("Discovered Part 2 resource collection links: " + collectionUris, true);
	}

	/**
	 * REQ-ETS-PART2-001; SCENARIO-ETS-PART2-001-RELEASED-RESOURCE-COLLECTION-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_RESOURCE_COLLECTION
			+ ": advertised Part 2 resource collections are readable JSON objects with items and links arrays (REQ-ETS-PART2-001, SCENARIO-ETS-PART2-001-RELEASED-RESOURCE-COLLECTION-001)",
			groups = GROUP, alwaysRun = true)
	public void part2ApiCommonResourceCollectionsAreReadable() {
		requirePart2ApiCommonDeclaration(REQ_RESOURCE_COLLECTION);
		Response landingResponse = given().accept("application/json").when().get(this.apiRoot).andReturn();
		ETSAssert.assertStatus(landingResponse, 200, REQ_RESOURCE_COLLECTION);
		Map<String, Object> landingBody = parseBody(landingResponse);
		if (landingBody == null) {
			ETSAssert.failWithUri(REQ_RESOURCE_COLLECTION,
					"landing page body did not parse as JSON. Content-Type was: " + landingResponse.getContentType());
		}
		List<URI> collectionUris = discoverPart2CollectionUris(landingBody, this.apiRoot);
		if (collectionUris.isEmpty()) {
			throw new SkipException(REQ_RESOURCE_COLLECTION
					+ " - landing page did not advertise any same-origin Part 2 resource collection links.");
		}
		for (URI collectionUri : collectionUris) {
			Response response = given().accept("application/json")
				.queryParam("limit", 1)
				.when()
				.get(collectionUri)
				.andReturn();
			ETSAssert.assertStatus(response, 200, REQ_RESOURCE_COLLECTION);
			Map<String, Object> body = parseBody(response);
			if (body == null) {
				ETSAssert.failWithUri(REQ_RESOURCE_COLLECTION,
						collectionUri + " body did not parse as JSON. Content-Type was: " + response.getContentType());
			}
			assertResourceCollectionShape(body, collectionUri.toString());
		}
	}

	private void requirePart2ApiCommonDeclaration(String requirement) {
		Response conformanceResponse = given().accept("application/json")
			.when()
			.get(this.apiRoot.resolve("conformance"))
			.andReturn();
		ETSAssert.assertStatus(conformanceResponse, 200, requirement);
		Map<String, Object> conformanceBody = parseBody(conformanceResponse);
		if (conformanceBody == null) {
			ETSAssert.failWithUri(requirement, "/conformance body did not parse as JSON. Content-Type was: "
					+ conformanceResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(conformanceBody, "conformsTo", List.class, requirement);
		if (!declaresConformance(conformanceBody, CONF_PART2_API_COMMON)) {
			throw new SkipException(CONF_PART2_API_COMMON
					+ " - IUT does not declare the CS API Part 2 API Common conformance class in /conformance. "
					+ "Undeclared Part 2 API Common behavior is not conformance PASS evidence.");
		}
	}

	public static boolean declaresConformance(Map<String, Object> body, String conformanceUri) {
		if (body == null || conformanceUri == null) {
			return false;
		}
		Object conformsTo = body.get("conformsTo");
		if (!(conformsTo instanceof List)) {
			return false;
		}
		for (Object entry : (List<?>) conformsTo) {
			if (conformanceUri.equals(entry)) {
				return true;
			}
		}
		return false;
	}

	static List<URI> discoverPart2CollectionUris(Map<String, Object> landingBody, URI baseUri) {
		if (landingBody == null || baseUri == null) {
			return List.of();
		}
		Object links = landingBody.get("links");
		if (!(links instanceof List)) {
			return List.of();
		}
		Set<URI> discovered = new LinkedHashSet<>();
		for (Object link : (List<?>) links) {
			if (!(link instanceof Map)) {
				continue;
			}
			Map<?, ?> linkMap = (Map<?, ?>) link;
			Object href = linkMap.get("href");
			if (!(href instanceof String) || ((String) href).isBlank()) {
				continue;
			}
			String rel = asLowerString(linkMap.get("rel"));
			String path = pathToken((String) href);
			if (PART2_COLLECTION_TOKENS.contains(rel) || PART2_COLLECTION_TOKENS.contains(path)) {
				URI resolved = baseUri.resolve((String) href);
				if (isSameOrigin(baseUri, resolved)) {
					discovered.add(resolved);
				}
			}
		}
		return new ArrayList<>(discovered);
	}

	static boolean hasResourceCollectionShape(Map<String, Object> body) {
		return body != null && body.get("items") instanceof List && body.get("links") instanceof List;
	}

	static void assertResourceCollectionShape(Map<String, Object> body, String source) {
		if (!hasResourceCollectionShape(body)) {
			ETSAssert.failWithUri(REQ_RESOURCE_COLLECTION,
					source + " did not expose a CS API resource collection JSON object with items[] and links[].");
		}
	}

	private static String pathToken(String href) {
		try {
			String path = URI.create(href).getPath();
			if (path == null || path.isBlank()) {
				return "";
			}
			String[] parts = path.toLowerCase(Locale.ROOT).split("/");
			for (int i = parts.length - 1; i >= 0; i--) {
				if (!parts[i].isBlank()) {
					return parts[i];
				}
			}
			return "";
		}
		catch (IllegalArgumentException ex) {
			return "";
		}
	}

	private static String asLowerString(Object value) {
		return value instanceof String ? ((String) value).toLowerCase(Locale.ROOT) : "";
	}

	private static boolean isSameOrigin(URI expected, URI actual) {
		return expected != null && actual != null && expected.getScheme() != null && actual.getScheme() != null
				&& expected.getHost() != null && actual.getHost() != null
				&& expected.getScheme().equalsIgnoreCase(actual.getScheme())
				&& expected.getHost().equalsIgnoreCase(actual.getHost())
				&& effectivePort(expected) == effectivePort(actual);
	}

	private static int effectivePort(URI uri) {
		if (uri.getPort() >= 0) {
			return uri.getPort();
		}
		return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
	}

	private static void skipWhenPrerequisiteUnsatisfied(ITestContext testContext) {
		String blocker = configurationBlocker(testContext.getFailedConfigurations(), "failed");
		if (blocker == null) {
			blocker = configurationBlocker(testContext.getSkippedConfigurations(), "skipped");
		}
		if (blocker == null) {
			blocker = testBlocker(testContext.getFailedTests(), "failed", false);
		}
		if (blocker == null) {
			blocker = testBlocker(testContext.getSkippedTests(), "skipped", true);
		}
		if (blocker != null) {
			throw new SkipException(
					"Part 2 API Common setup skipped before IUT access because prerequisite " + blocker + ".");
		}
	}

	private static String configurationBlocker(IResultMap results, String status) {
		if (results == null) {
			return null;
		}
		for (ITestResult result : results.getAllResults()) {
			if (result != null && result.getMethod() != null && isInheritedPrerequisite(result)) {
				return "configuration " + result.getMethod().getMethodName() + " " + status;
			}
		}
		return null;
	}

	private static String testBlocker(IResultMap results, String status, boolean allowDatetimeEvidenceLimitation) {
		if (results == null) {
			return null;
		}
		for (ITestResult result : results.getAllResults()) {
			if (result == null || result.getMethod() == null || !isInheritedPrerequisite(result)) {
				continue;
			}
			if (allowDatetimeEvidenceLimitation && DATETIME_EVIDENCE_METHOD.equals(result.getMethod().getMethodName())
					&& result.getThrowable() instanceof SkipException
					&& Part1ApiCommonTests.DATETIME_EVIDENCE_LIMITATION.equals(result.getThrowable().getMessage())) {
				Reporter.log(Part1ApiCommonTests.DATETIME_EVIDENCE_LIMITATION
						+ " Part 2 API Common direct procedures will execute, but inherited conformance remains incomplete.",
						true);
				continue;
			}
			return "method " + result.getMethod().getMethodName() + " " + status;
		}
		return null;
	}

	private static boolean isInheritedPrerequisite(ITestResult result) {
		for (String group : result.getMethod().getGroups()) {
			if ("core".equals(group) || "common".equals(group) || "part1apicommon".equals(group)) {
				return true;
			}
		}
		Class<?> realClass = result.getMethod().getRealClass();
		if (realClass == null) {
			return false;
		}
		String className = realClass.getName();
		return realClass == Part1ApiCommonTests.class
				|| className.startsWith("org.opengis.cite.ogcapiconnectedsystems10.conformance.core.")
				|| className.startsWith("org.opengis.cite.ogcapiconnectedsystems10.conformance.common.");
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> parseBody(Response response) {
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
