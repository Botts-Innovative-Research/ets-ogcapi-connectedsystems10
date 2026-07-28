package org.opengis.cite.ogcapiconnectedsystems10.conformance.propertydefinitions;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.PageDocument;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.TraversalResult;
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
 * Released OGC 23-001 Annex A procedures for the Property Definitions class.
 */
public class PropertyDefinitionsTests {

	static final String GROUP = "propertydefinitions";

	static final String REQ_CANONICAL_URL = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/property/canonical-url";

	static final String REQ_RESOURCES_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/property/resources-endpoint";

	static final String REQ_CANONICAL_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/property/canonical-endpoint";

	static final String REQ_COLLECTIONS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/property/collections";

	private static final String DATETIME_EVIDENCE_METHOD = "datetimeUsesValidTime";

	private URI apiRoot;

	/**
	 * Loads the immutable API root after inherited API Common prerequisites complete.
	 * @param testContext active TestNG context.
	 */
	@BeforeClass(dependsOnGroups = "part1apicommon", alwaysRun = true)
	public void fetchPropertyArguments(ITestContext testContext) {
		skipWhenPrerequisiteUnsatisfied(testContext);
		Object iut = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iut instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		configure((URI) iut);
	}

	void configure(URI iut) {
		this.apiRoot = normalizeApiRoot(iut);
	}

	/**
	 * REQ-ETS-PART1-008; SCENARIO-ETS-PART1-008-RELEASED-CANONICAL-URL-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_CANONICAL_URL
			+ ": every advertised Property resolves to equivalent canonical content", groups = GROUP, alwaysRun = true)
	public void everyPropertyHasCanonicalUrl() {
		List<Map<String, Object>> collections = propertyCollections(REQ_CANONICAL_URL, false);
		if (collections.isEmpty()) {
			throw new SkipException(REQ_CANONICAL_URL + " - no collection advertises itemType=sosa:Property.");
		}
		List<String> evidenceLimitations = new ArrayList<>();
		int[] inspectedItems = { 0 };
		for (Map<String, Object> collection : collections) {
			Optional<TraversalResult> evidence;
			try {
				evidence = Part1ApiCommonSupport.collectionItemsDetailed(this.apiRoot, collection,
						PropertyDefinitionsSupport.SUPPORTED_MEDIA_TYPES,
						page -> inspectCanonicalPage(page, evidenceLimitations, inspectedItems));
			}
			catch (SkipException ex) {
				evidenceLimitations.add(evidenceLimitation(ex));
				continue;
			}
			if (evidence.isEmpty()) {
				evidenceLimitations.add("collection " + collection.get("id") + " has no supported SensorML evidence");
			}
		}
		if (inspectedItems[0] == 0) {
			evidenceLimitations.add("no Property items were available for canonical comparison");
		}
		skipForEvidenceLimitations(REQ_CANONICAL_URL, evidenceLimitations);
	}

	/**
	 * REQ-ETS-PART1-008; SCENARIO-ETS-PART1-008-RELEASED-RESOURCES-ENDPOINT-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RESOURCES_ENDPOINT
			+ ": the supplied Property resources endpoint returns schema-valid content", groups = GROUP,
			alwaysRun = true)
	public void propertyResourcesEndpointIsValid() {
		requireApiRoot(REQ_RESOURCES_ENDPOINT);
		validateEndpoint(this.apiRoot.resolve("properties"), REQ_RESOURCES_ENDPOINT);
	}

	/**
	 * REQ-ETS-PART1-008; SCENARIO-ETS-PART1-008-RELEASED-CANONICAL-ENDPOINT-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_CANONICAL_ENDPOINT
			+ ": the canonical /properties endpoint satisfies the resources procedure", groups = GROUP,
			alwaysRun = true)
	public void canonicalPropertiesEndpointIsValid() {
		requireApiRoot(REQ_CANONICAL_ENDPOINT);
		validateEndpoint(this.apiRoot.resolve("properties"), REQ_CANONICAL_ENDPOINT);
	}

	/**
	 * REQ-ETS-PART1-008; SCENARIO-ETS-PART1-008-RELEASED-COLLECTIONS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_COLLECTIONS
			+ ": advertised Property collections use released metadata and schemas", groups = GROUP, alwaysRun = true)
	public void propertyCollectionsAreValid() {
		List<Map<String, Object>> collections = propertyCollections(REQ_COLLECTIONS, true);
		List<String> evidenceLimitations = new ArrayList<>();
		for (Map<String, Object> collection : collections) {
			PropertyDefinitionsSupport.requirePropertyCollectionMetadata(collection, REQ_COLLECTIONS);
			URI endpoint = collectionItemsUri(collection);
			Optional<TraversalResult> evidence;
			try {
				evidence = Part1ApiCommonSupport.collectionItemsDetailed(this.apiRoot, collection,
						PropertyDefinitionsSupport.SUPPORTED_MEDIA_TYPES, page -> PropertyDefinitionsSupport
							.validatePropertyEndpoint(endpoint, List.of(page), REQ_COLLECTIONS));
			}
			catch (SkipException ex) {
				evidenceLimitations.add(evidenceLimitation(ex));
				continue;
			}
			if (evidence.isEmpty()) {
				evidenceLimitations.add("collection " + collection.get("id") + " has no supported SensorML evidence");
			}
		}
		skipForEvidenceLimitations(REQ_COLLECTIONS, evidenceLimitations);
	}

	private void validateEndpoint(URI endpoint, String requirement) {
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint,
				PropertyDefinitionsSupport.SENSORML, Map.of(), requirement,
				PropertyDefinitionsSupport.SUPPORTED_MEDIA_TYPES,
				page -> PropertyDefinitionsSupport.validatePropertyEndpoint(endpoint, List.of(page), requirement));
		if (evidence.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " returned HTTP 404.");
		}
	}

	private void inspectCanonicalPage(PageDocument page, List<String> evidenceLimitations, int[] inspectedItems) {
		for (Map<String, Object> item : page.items()) {
			inspectedItems[0]++;
			PropertyDefinitionsSupport.CanonicalLink canonical;
			try {
				canonical = PropertyDefinitionsSupport.canonicalLink(item, page.source(), this.apiRoot,
						page.mediaType(), REQ_CANONICAL_URL);
			}
			catch (SkipException ex) {
				evidenceLimitations.add(evidenceLimitation(ex));
				continue;
			}
			String accept = canonical.mediaType().isBlank() ? page.mediaType() : canonical.mediaType();
			Response response = get(canonical.uri(), accept);
			ETSAssert.assertStatus(response, 200, REQ_CANONICAL_URL);
			String actualMediaType = normalizeMediaType(response.getContentType());
			if (!normalizeMediaType(page.mediaType()).equals(actualMediaType)) {
				evidenceLimitations.add(canonical.uri() + " returned media type '" + response.getContentType()
						+ "', which is not comparable with collection media '" + page.mediaType() + "'");
				continue;
			}
			Map<String, Object> body = parseObject(response, canonical.uri(), REQ_CANONICAL_URL);
			if (!PropertyDefinitionsSupport.withoutCanonicalLinks(item)
				.equals(PropertyDefinitionsSupport.withoutCanonicalLinks(body))) {
				ETSAssert.failWithUri(REQ_CANONICAL_URL, canonical.uri()
						+ " content differs from its collection item after canonical links are removed.");
			}
		}
	}

	private List<Map<String, Object>> propertyCollections(String requirement, boolean missingIsFailure) {
		requireApiRoot(requirement);
		URI endpoint = this.apiRoot.resolve("collections");
		Response response = get(endpoint, "application/json");
		ETSAssert.assertStatus(response, 200, requirement);
		Map<String, Object> body = parseObject(response, endpoint, requirement);
		Object advertised = body.get("collections");
		if (!(advertised instanceof List)) {
			ETSAssert.failWithUri(requirement, endpoint + " response is missing a collections array.");
		}
		List<Map<String, Object>> selected = PropertyDefinitionsSupport.selectPropertyCollections((List<?>) advertised);
		if (selected.isEmpty() && missingIsFailure) {
			ETSAssert.failWithUri(requirement,
					endpoint + " does not advertise a collection with itemType=sosa:Property.");
		}
		return selected;
	}

	private URI collectionItemsUri(Map<String, Object> collection) {
		return this.apiRoot.resolve("collections/" + encodePathToken((String) collection.get("id")) + "/items");
	}

	private void requireApiRoot(String requirement) {
		if (this.apiRoot == null) {
			throw new SkipException(requirement + " - API root fixture is unavailable.");
		}
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
					"Property Definitions setup skipped before IUT access because prerequisite " + blocker + ".");
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
			if (allowDatetimeEvidenceLimitation && isDatetimeEvidenceLimitation(result)) {
				Reporter.log(Part1ApiCommonTests.DATETIME_EVIDENCE_LIMITATION
						+ " Property Definitions direct procedures will execute, but inherited conformance remains incomplete.",
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

	private static boolean isDatetimeEvidenceLimitation(ITestResult result) {
		Throwable throwable = result.getThrowable();
		return DATETIME_EVIDENCE_METHOD.equals(result.getMethod().getMethodName()) && throwable instanceof SkipException
				&& Part1ApiCommonTests.DATETIME_EVIDENCE_LIMITATION.equals(throwable.getMessage());
	}

	private static String evidenceLimitation(SkipException exception) {
		return exception.getMessage() == null || exception.getMessage().isBlank() ? exception.getClass().getSimpleName()
				: exception.getMessage();
	}

	private static void skipForEvidenceLimitations(String requirement, List<String> limitations) {
		if (!limitations.isEmpty()) {
			throw new SkipException(requirement + " - incomplete evidence after inspecting all independent candidates: "
					+ String.join(" | ", limitations));
		}
	}

	private static Response get(URI uri, String accept) {
		return given().accept(accept).when().get(uri).andReturn();
	}

	private static Map<String, Object> parseObject(Response response, URI source, String requirement) {
		try {
			Map<String, Object> body = response.jsonPath().getMap("$");
			if (body == null) {
				ETSAssert.failWithUri(requirement, source + " response body is not a JSON object.");
			}
			return body;
		}
		catch (Exception ex) {
			ETSAssert.failWithUri(requirement,
					source + " response body is not parseable as a JSON object: " + ex.getMessage());
			return Map.of();
		}
	}

	private static URI normalizeApiRoot(URI iut) {
		if (iut == null || !iut.isAbsolute()) {
			throw new IllegalArgumentException("IUT must be an absolute URI.");
		}
		String value = iut.toString();
		return URI.create(value.endsWith("/") ? value : value + "/");
	}

	private static String normalizeMediaType(String mediaType) {
		if (mediaType == null) {
			return "";
		}
		return mediaType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
	}

	private static String encodePathToken(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
	}

}
