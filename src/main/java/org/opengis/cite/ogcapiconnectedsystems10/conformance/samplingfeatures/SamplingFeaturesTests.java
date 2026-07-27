package org.opengis.cite.ogcapiconnectedsystems10.conformance.samplingfeatures;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
 * Released OGC 23-001 Annex A procedures for the Sampling Features conformance class.
 */
public class SamplingFeaturesTests {

	static final String GROUP = "samplingfeatures";

	static final String REQ_CANONICAL_URL = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sf/canonical-url";

	static final String REQ_RESOURCES_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sf/resources-endpoint";

	static final String REQ_CANONICAL_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sf/canonical-endpoint";

	static final String REQ_COLLECTIONS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sf/collections";

	static final String REQ_REF_FROM_SYSTEM = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sf/ref-from-system";

	private static final String DATETIME_EVIDENCE_METHOD = "datetimeUsesValidTime";

	private static final String REQ_SYSTEM_LOCATION_TIME = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/location-time";

	private static final String REQ_SYSTEM_RESOURCES_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/resources-endpoint";

	private static final String REQ_SYSTEM_CANONICAL_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/canonical-endpoint";

	private URI apiRoot;

	/**
	 * Loads the immutable API root after inherited System prerequisites complete.
	 * @param testContext active TestNG context.
	 */
	@BeforeClass(dependsOnGroups = "systemfeatures", alwaysRun = true)
	public void fetchSamplingFeaturesArguments(ITestContext testContext) {
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
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-CANONICAL-URL-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_CANONICAL_URL
			+ ": every advertised Sampling Feature resolves to equivalent canonical content", groups = GROUP,
			alwaysRun = true)
	public void everySamplingFeatureHasCanonicalUrl() {
		List<Map<String, Object>> collections = samplingFeatureCollections(REQ_CANONICAL_URL, false);
		if (collections.isEmpty()) {
			throw new SkipException(REQ_CANONICAL_URL + " - no collection advertises featureType=sosa:Sample.");
		}
		List<String> evidenceLimitations = new ArrayList<>();
		for (Map<String, Object> collection : collections) {
			Optional<TraversalResult> evidence;
			try {
				evidence = Part1ApiCommonSupport.collectionItemsDetailed(this.apiRoot, collection,
						SamplingFeaturesSupport.SUPPORTED_MEDIA_TYPES,
						page -> inspectCanonicalPage(page, evidenceLimitations));
			}
			catch (SkipException ex) {
				evidenceLimitations.add(evidenceLimitation(ex));
				continue;
			}
			if (evidence.isEmpty()) {
				evidenceLimitations.add("collection " + collection.get("id") + " has no supported representation");
				continue;
			}
			evidence.orElseThrow();
		}
		skipForEvidenceLimitations(REQ_CANONICAL_URL, evidenceLimitations);
	}

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-RESOURCES-ENDPOINT-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RESOURCES_ENDPOINT
			+ ": the supplied Sampling Feature resources endpoint returns schema-valid content", groups = GROUP,
			alwaysRun = true)
	public void samplingFeaturesResourcesEndpointIsValid() {
		requireApiRoot(REQ_RESOURCES_ENDPOINT);
		validateEndpoint(this.apiRoot.resolve("samplingFeatures"), REQ_RESOURCES_ENDPOINT);
	}

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-CANONICAL-ENDPOINT-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_CANONICAL_ENDPOINT
			+ ": the canonical /samplingFeatures endpoint satisfies the resources procedure", groups = GROUP,
			alwaysRun = true)
	public void canonicalSamplingFeaturesEndpointIsValid() {
		requireApiRoot(REQ_CANONICAL_ENDPOINT);
		validateEndpoint(this.apiRoot.resolve("samplingFeatures"), REQ_CANONICAL_ENDPOINT);
	}

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-COLLECTIONS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_COLLECTIONS
			+ ": advertised Sampling Feature collections use released metadata and schemas", groups = GROUP,
			alwaysRun = true)
	public void samplingFeatureCollectionsAreValid() {
		List<Map<String, Object>> collections = samplingFeatureCollections(REQ_COLLECTIONS, true);
		List<String> evidenceLimitations = new ArrayList<>();
		for (Map<String, Object> collection : collections) {
			SamplingFeaturesSupport.requireSamplingFeatureCollectionMetadata(collection, REQ_COLLECTIONS);
			URI endpoint = collectionItemsUri(collection);
			Optional<TraversalResult> evidence;
			try {
				evidence = Part1ApiCommonSupport.collectionItemsDetailed(this.apiRoot, collection,
						SamplingFeaturesSupport.SUPPORTED_MEDIA_TYPES, page -> SamplingFeaturesSupport
							.validateSamplingFeatureEndpoint(endpoint, List.of(page), REQ_COLLECTIONS));
			}
			catch (SkipException ex) {
				evidenceLimitations.add(evidenceLimitation(ex));
				continue;
			}
			if (evidence.isEmpty()) {
				evidenceLimitations.add("collection " + collection.get("id") + " has no supported GeoJSON evidence");
				continue;
			}
			evidence.orElseThrow();
		}
		skipForEvidenceLimitations(REQ_COLLECTIONS, evidenceLimitations);
	}

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-REF-FROM-SYSTEM-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_REF_FROM_SYSTEM
			+ ": every canonical System exposes a fully traversable samplingFeatures subresource", groups = GROUP,
			alwaysRun = true)
	public void samplingFeaturesAreAvailableFromEverySystem() {
		requireApiRoot(REQ_REF_FROM_SYSTEM);
		Optional<TraversalResult> systems = Part1ApiCommonSupport.canonicalResourcesDetailed(this.apiRoot, "systems");
		if (systems.isEmpty()) {
			ETSAssert.failWithUri(REQ_REF_FROM_SYSTEM, this.apiRoot.resolve("systems") + " returned HTTP 404.");
		}
		List<String> evidenceLimitations = new ArrayList<>();
		for (Map<String, Object> system : systems.orElseThrow().items()) {
			Object id = system.get("id");
			if (!(id instanceof String) || ((String) id).isBlank()) {
				ETSAssert.failWithUri(REQ_REF_FROM_SYSTEM,
						"canonical /systems representation contains an item without a non-empty string id.");
			}
			URI endpoint = this.apiRoot.resolve("systems/" + encodePathToken((String) id) + "/samplingFeatures");
			Optional<TraversalResult> samplingFeatures;
			try {
				samplingFeatures = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint,
						"application/geo+json, application/json", Map.of(), REQ_REF_FROM_SYSTEM,
						Set.of("application/geo+json", "application/json"), page -> SamplingFeaturesSupport
							.validateNestedSamplingFeaturePages(endpoint, List.of(page), REQ_REF_FROM_SYSTEM));
			}
			catch (SkipException ex) {
				evidenceLimitations.add(evidenceLimitation(ex));
				continue;
			}
			if (samplingFeatures.isEmpty()) {
				ETSAssert.failWithUri(REQ_REF_FROM_SYSTEM, endpoint + " returned HTTP 404.");
			}
			samplingFeatures.orElseThrow();
		}
		skipForEvidenceLimitations(REQ_REF_FROM_SYSTEM, evidenceLimitations);
	}

	private void validateEndpoint(URI endpoint, String requirement) {
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint,
				SamplingFeaturesSupport.GEOJSON, Map.of(), requirement, SamplingFeaturesSupport.SUPPORTED_MEDIA_TYPES,
				page -> SamplingFeaturesSupport.validateSamplingFeatureEndpoint(endpoint, List.of(page), requirement));
		if (evidence.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " returned HTTP 404.");
		}
		evidence.orElseThrow();
	}

	private void inspectCanonicalPage(PageDocument page, List<String> evidenceLimitations) {
		for (Map<String, Object> item : page.items()) {
			SamplingFeaturesSupport.CanonicalLink canonical;
			try {
				canonical = SamplingFeaturesSupport.canonicalLink(item, page.source(), this.apiRoot, page.mediaType(),
						REQ_CANONICAL_URL);
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
			if (!SamplingFeaturesSupport.withoutCanonicalLinks(item)
				.equals(SamplingFeaturesSupport.withoutCanonicalLinks(body))) {
				ETSAssert.failWithUri(REQ_CANONICAL_URL, canonical.uri()
						+ " content differs from its collection item after canonical links are removed.");
			}
		}
	}

	private List<Map<String, Object>> samplingFeatureCollections(String requirement, boolean missingIsFailure) {
		requireApiRoot(requirement);
		URI endpoint = this.apiRoot.resolve("collections");
		Response response = get(endpoint, "application/json");
		ETSAssert.assertStatus(response, 200, requirement);
		Map<String, Object> body = parseObject(response, endpoint, requirement);
		Object advertised = body.get("collections");
		if (!(advertised instanceof List)) {
			ETSAssert.failWithUri(requirement, endpoint + " response is missing a collections array.");
		}
		List<Map<String, Object>> selected = SamplingFeaturesSupport
			.selectSamplingFeatureCollections((List<?>) advertised);
		if (selected.isEmpty() && missingIsFailure) {
			ETSAssert.failWithUri(requirement,
					endpoint + " does not advertise a collection with featureType=sosa:Sample.");
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
					"Sampling Features setup skipped before IUT access because prerequisite " + blocker + ".");
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

	private static String testBlocker(IResultMap results, String status, boolean allowEvidenceLimitations) {
		if (results == null) {
			return null;
		}
		for (ITestResult result : results.getAllResults()) {
			if (result == null || result.getMethod() == null || !isInheritedPrerequisite(result)) {
				continue;
			}
			if (allowEvidenceLimitations && isAllowedEvidenceLimitation(result)) {
				Reporter.log(
						"Sampling Features direct procedures will execute despite documented inherited evidence limitation "
								+ result.getMethod().getMethodName() + ".",
						true);
				continue;
			}
			return "method " + result.getMethod().getMethodName() + " " + status;
		}
		return null;
	}

	private static boolean isInheritedPrerequisite(ITestResult result) {
		for (String group : result.getMethod().getGroups()) {
			if ("core".equals(group) || "common".equals(group) || "part1apicommon".equals(group)
					|| "systemfeatures".equals(group)) {
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
				|| className.startsWith("org.opengis.cite.ogcapiconnectedsystems10.conformance.common.")
				|| className.startsWith("org.opengis.cite.ogcapiconnectedsystems10.conformance.systemfeatures.");
	}

	private static boolean isAllowedEvidenceLimitation(ITestResult result) {
		Throwable throwable = result.getThrowable();
		if (!(throwable instanceof SkipException) || throwable.getMessage() == null) {
			return false;
		}
		String method = result.getMethod().getMethodName();
		String reason = throwable.getMessage();
		if (DATETIME_EVIDENCE_METHOD.equals(method)) {
			return Part1ApiCommonTests.DATETIME_EVIDENCE_LIMITATION.equals(reason);
		}
		if ("mobileSystemLocationIsUpdated".equals(method)) {
			return (REQ_SYSTEM_LOCATION_TIME + " - optional mobile-system-id test-run argument was not supplied.")
				.equals(reason);
		}
		if ("systemResourcesEndpointIsValid".equals(method)) {
			return unsupportedSystemMedia(reason, REQ_SYSTEM_RESOURCES_ENDPOINT);
		}
		if ("canonicalSystemsEndpointIsValid".equals(method)) {
			return unsupportedSystemMedia(reason, REQ_SYSTEM_CANONICAL_ENDPOINT);
		}
		return false;
	}

	private static boolean unsupportedSystemMedia(String reason, String requirement) {
		return reason.startsWith(requirement + " - ")
				&& reason.endsWith(" returned a media type unsupported by this testing engine.");
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
