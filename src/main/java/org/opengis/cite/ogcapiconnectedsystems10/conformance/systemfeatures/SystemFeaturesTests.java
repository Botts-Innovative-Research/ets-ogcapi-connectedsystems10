package org.opengis.cite.ogcapiconnectedsystems10.conformance.systemfeatures;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonTests;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.PageDocument;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.TraversalResult;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * Released OGC 23-001 Annex A procedures for the System conformance class.
 */
public class SystemFeaturesTests {

	static final String GROUP = "systemfeatures";

	static final String REC_LOCATION = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/rec/system/location";

	static final String REQ_LOCATION_TIME = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/location-time";

	static final String REQ_CANONICAL_URL = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/canonical-url";

	static final String REQ_RESOURCES_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/resources-endpoint";

	static final String REQ_CANONICAL_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/canonical-endpoint";

	static final String REQ_COLLECTIONS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/collections";

	private static final String SYSTEM_ACCEPT = "application/geo+json, application/sml+json, application/json";

	private static final Duration MOBILE_POLL_TIMEOUT = Duration.ofSeconds(30);

	private URI apiRoot;

	private String mobileSystemId;

	/**
	 * Loads immutable run arguments after API Common succeeds.
	 * @param testContext active TestNG context.
	 */
	@BeforeClass(dependsOnGroups = "part1apicommon", alwaysRun = true)
	public void fetchSystemArguments(ITestContext testContext) {
		skipWhenPrerequisiteUnsatisfied(testContext);
		Object iut = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iut instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		Object mobile = testContext.getSuite().getAttribute(SuiteAttribute.MOBILE_SYSTEM_ID.getName());
		configure((URI) iut, mobile instanceof String ? (String) mobile : null);
	}

	void configure(URI iut, String mobileSystemId) {
		this.apiRoot = normalizeApiRoot(iut);
		this.mobileSystemId = mobileSystemId == null || mobileSystemId.isBlank() ? null : mobileSystemId.trim();
	}

	static void skipWhenPrerequisiteUnsatisfied(ITestContext testContext) {
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
			throw new SkipException("System setup skipped before IUT access because prerequisite " + blocker + ".");
		}
	}

	private static String configurationBlocker(IResultMap results, String status) {
		if (results == null) {
			return null;
		}
		for (ITestResult result : results.getAllResults()) {
			if (result != null && result.getMethod() != null) {
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
						+ " System direct procedures will execute, but inherited conformance remains incomplete.",
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
		return false;
	}

	private static boolean isDatetimeEvidenceLimitation(ITestResult result) {
		Throwable throwable = result.getThrowable();
		return "datetimeUsesValidTime".equals(result.getMethod().getMethodName()) && throwable instanceof SkipException
				&& Part1ApiCommonTests.DATETIME_EVIDENCE_LIMITATION.equals(throwable.getMessage());
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-LOCATION-001.
	 */
	@Test(description = "OGC-23-001 " + REC_LOCATION + ": every physical System should include a location",
			groups = GROUP, alwaysRun = true)
	public void systemLocationsFollowRecommendation() {
		TraversalResult canonicalSystems = canonicalSystems(REC_LOCATION);
		for (PageDocument page : canonicalSystems.pages()) {
			for (Map<String, Object> system : page.items()) {
				String assetType = SystemFeaturesSupport.assetType(system, page.mediaType()).orElse("");
				if (!SystemFeaturesSupport.isVirtualAsset(assetType)
						&& SystemFeaturesSupport.location(system, page.mediaType()).isEmpty()) {
					Reporter.log(REC_LOCATION + " - " + page.source()
							+ " contains a non-Simulation/Process System without location information.", true);
				}
			}
		}
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-LOCATION-TIME-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_LOCATION_TIME + ": a known moving System changes location over time",
			groups = GROUP, alwaysRun = true)
	public void mobileSystemLocationIsUpdated() {
		if (this.mobileSystemId == null || this.mobileSystemId.isBlank()) {
			throw new SkipException(
					REQ_LOCATION_TIME + " - optional mobile-system-id test-run argument was not supplied.");
		}
		if (this.apiRoot == null) {
			throw new SkipException(REQ_LOCATION_TIME + " - API root fixture is unavailable.");
		}
		String encodedId = URLEncoder.encode(this.mobileSystemId, StandardCharsets.UTF_8).replace("+", "%20");
		URI mobileUri = this.apiRoot.resolve("systems/" + encodedId);
		Response first = get(mobileUri, SYSTEM_ACCEPT);
		ETSAssert.assertStatus(first, 200, REQ_LOCATION_TIME);
		Map<String, Object> firstBody = parseObject(first, mobileUri, REQ_LOCATION_TIME);
		JsonNode initialLocation = SystemFeaturesSupport.location(firstBody, responseMediaType(first)).orElseGet(() -> {
			ETSAssert.failWithUri(REQ_LOCATION_TIME,
					mobileUri + " did not expose a location for the known moving System.");
			return null;
		});

		Instant deadline = Instant.now().plus(MOBILE_POLL_TIMEOUT);
		while (Instant.now().isBefore(deadline)) {
			sleepOneSecond();
			Response current = get(mobileUri, SYSTEM_ACCEPT);
			ETSAssert.assertStatus(current, 200, REQ_LOCATION_TIME);
			Map<String, Object> currentBody = parseObject(current, mobileUri, REQ_LOCATION_TIME);
			Optional<JsonNode> currentLocation = SystemFeaturesSupport.location(currentBody,
					responseMediaType(current));
			if (currentLocation.isPresent() && !initialLocation.equals(currentLocation.orElseThrow())) {
				return;
			}
		}
		ETSAssert.failWithUri(REQ_LOCATION_TIME,
				mobileUri + " location did not change within " + MOBILE_POLL_TIMEOUT.toSeconds() + " seconds.");
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-CANONICAL-URL-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_CANONICAL_URL
			+ ": every System collection item resolves to equivalent canonical content", groups = GROUP,
			alwaysRun = true)
	public void everySystemHasCanonicalUrl() {
		List<Map<String, Object>> collections = systemCollections(REQ_CANONICAL_URL);
		int supportedCollections = 0;
		for (Map<String, Object> collection : collections) {
			Optional<TraversalResult> evidence = Part1ApiCommonSupport.collectionItemsDetailed(this.apiRoot,
					collection);
			if (evidence.isEmpty()) {
				continue;
			}
			supportedCollections++;
			TraversalResult traversal = evidence.orElseThrow();
			for (PageDocument page : traversal.pages()) {
				for (Map<String, Object> item : page.items()) {
					URI canonical = SystemFeaturesSupport.canonicalUri(item, page.source(), this.apiRoot,
							REQ_CANONICAL_URL);
					Response response = get(canonical, page.mediaType());
					ETSAssert.assertStatus(response, 200, REQ_CANONICAL_URL);
					Map<String, Object> canonicalBody = parseObject(response, canonical, REQ_CANONICAL_URL);
					if (!SystemFeaturesSupport.withoutCanonicalLinks(item)
						.equals(SystemFeaturesSupport.withoutCanonicalLinks(canonicalBody))) {
						ETSAssert.failWithUri(REQ_CANONICAL_URL, canonical
								+ " content differs from its collection item after canonical links are removed.");
					}
				}
			}
		}
		skipWhenEverySelectedCollectionIsUnsupported(collections, supportedCollections, REQ_CANONICAL_URL);
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-RESOURCES-ENDPOINT-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RESOURCES_ENDPOINT
			+ ": the System resources endpoint returns schema-valid content", groups = GROUP, alwaysRun = true)
	public void systemResourcesEndpointIsValid() {
		TraversalResult traversal = canonicalSystems(REQ_RESOURCES_ENDPOINT);
		URI endpoint = this.apiRoot.resolve("systems");
		requireSupportedEndpoint(endpoint, traversal, REQ_RESOURCES_ENDPOINT);
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-CANONICAL-ENDPOINT-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_CANONICAL_ENDPOINT
			+ ": the canonical /systems endpoint satisfies the released procedure", groups = GROUP, alwaysRun = true)
	public void canonicalSystemsEndpointIsValid() {
		TraversalResult traversal = canonicalSystems(REQ_CANONICAL_ENDPOINT);
		URI endpoint = this.apiRoot.resolve("systems");
		requireSupportedEndpoint(endpoint, traversal, REQ_CANONICAL_ENDPOINT);
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-COLLECTIONS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_COLLECTIONS
			+ ": advertised System collections use released types and schemas", groups = GROUP, alwaysRun = true)
	public void systemCollectionsAreValid() {
		List<Map<String, Object>> collections = systemCollections(REQ_COLLECTIONS);
		int supportedCollections = 0;
		for (Map<String, Object> collection : collections) {
			Optional<TraversalResult> evidence = Part1ApiCommonSupport.collectionItemsDetailed(this.apiRoot,
					collection);
			if (evidence.isEmpty()) {
				continue;
			}
			TraversalResult traversal = evidence.orElseThrow();
			URI endpoint = collectionItemsUri(collection);
			if (!SystemFeaturesSupport.validateSystemEndpoint(endpoint, traversal.pages(), REQ_COLLECTIONS)) {
				continue;
			}
			supportedCollections++;
			for (PageDocument page : traversal.pages()) {
				for (Map<String, Object> item : page.items()) {
					String systemType = SystemFeaturesSupport.systemType(item, page.mediaType()).orElseGet(() -> {
						ETSAssert.failWithUri(REQ_COLLECTIONS,
								page.source() + " System item is missing its representation-specific type.");
						return "";
					});
					if (!SystemFeaturesSupport.isAllowedSystemType(systemType)) {
						ETSAssert.failWithUri(REQ_COLLECTIONS,
								page.source() + " System item has unsupported type '" + systemType + "'.");
					}
				}
			}
		}
		skipWhenEverySelectedCollectionIsUnsupported(collections, supportedCollections, REQ_COLLECTIONS);
	}

	private TraversalResult canonicalSystems(String requirement) {
		if (this.apiRoot == null) {
			throw new SkipException(requirement + " - API root fixture is unavailable.");
		}
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.canonicalResourcesDetailed(this.apiRoot, "systems");
		if (evidence.isEmpty()) {
			ETSAssert.failWithUri(requirement, this.apiRoot.resolve("systems") + " returned HTTP 404.");
		}
		return evidence.orElseThrow();
	}

	private List<Map<String, Object>> systemCollections(String requirement) {
		if (this.apiRoot == null) {
			throw new SkipException(requirement + " - API root fixture is unavailable.");
		}
		Response collectionsResponse = get(this.apiRoot.resolve("collections"), "application/json");
		ETSAssert.assertStatus(collectionsResponse, 200, requirement);
		Map<String, Object> body = parseObject(collectionsResponse, this.apiRoot.resolve("collections"), requirement);
		Object advertised = body.get("collections");
		if (!(advertised instanceof List)) {
			ETSAssert.failWithUri(requirement, "/collections response is missing a collections array.");
		}
		return SystemFeaturesSupport.selectSystemCollections((List<?>) advertised);
	}

	private void requireSupportedEndpoint(URI endpoint, TraversalResult traversal, String requirement) {
		if (!SystemFeaturesSupport.validateSystemEndpoint(endpoint, traversal.pages(), requirement)) {
			throw new SkipException(
					requirement + " - " + endpoint + " returned a media type unsupported by this testing engine.");
		}
	}

	private URI collectionItemsUri(Map<String, Object> collection) {
		String id = String.valueOf(collection.get("id"));
		String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8).replace("+", "%20");
		return this.apiRoot.resolve("collections/" + encodedId + "/items");
	}

	private static void skipWhenEverySelectedCollectionIsUnsupported(List<Map<String, Object>> collections,
			int supportedCollections, String requirement) {
		if (!collections.isEmpty() && supportedCollections == 0) {
			throw new SkipException(requirement + " - every selected System collection advertised unsupported media.");
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

	private static String responseMediaType(Response response) {
		String contentType = response.getContentType();
		if (contentType == null || contentType.isBlank()) {
			return "";
		}
		return contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
	}

	private static URI normalizeApiRoot(URI iut) {
		if (iut == null || !iut.isAbsolute()) {
			throw new IllegalArgumentException("IUT must be an absolute URI.");
		}
		String value = iut.toString();
		return URI.create(value.endsWith("/") ? value : value + "/");
	}

	private static void sleepOneSecond() {
		try {
			Thread.sleep(1_000);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while polling a mobile System location.", ex);
		}
	}

}
