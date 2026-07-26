package org.opengis.cite.ogcapiconnectedsystems10.conformance.deployments;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
 * Released OGC 23-001 Annex A procedures for the Deployment conformance class.
 */
public class DeploymentsTests {

	static final String GROUP = "deployments";

	static final String REQ_CANONICAL_URL = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/deployment/canonical-url";

	static final String REQ_RESOURCES_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/deployment/resources-endpoint";

	static final String REQ_CANONICAL_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/deployment/canonical-endpoint";

	static final String REQ_COLLECTIONS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/deployment/collections";

	static final String REQ_REF_FROM_SYSTEM = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/deployment/ref-from-system";

	private static final String DEPLOYMENT_ACCEPT = "application/geo+json, application/sml+json";

	private static final String DATETIME_EVIDENCE_METHOD = "datetimeUsesValidTime";

	private URI apiRoot;

	/**
	 * Loads the immutable API root after inherited API Common prerequisites complete.
	 * @param testContext active TestNG context.
	 */
	@BeforeClass(dependsOnGroups = "part1apicommon", alwaysRun = true)
	public void fetchDeploymentArguments(ITestContext testContext) {
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
	 * REQ-ETS-PART1-004; SCENARIO-ETS-PART1-004-RELEASED-CANONICAL-URL-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_CANONICAL_URL
			+ ": every advertised Deployment resolves to equivalent canonical content", groups = GROUP,
			alwaysRun = true)
	public void everyDeploymentHasCanonicalUrl() {
		List<Map<String, Object>> collections = deploymentCollections(REQ_CANONICAL_URL);
		for (Map<String, Object> collection : collections) {
			Optional<TraversalResult> evidence = Part1ApiCommonSupport.collectionItemsDetailed(this.apiRoot,
					collection);
			if (evidence.isEmpty()) {
				throw new SkipException(REQ_CANONICAL_URL + " - Deployment collection " + collection.get("id")
						+ " has no items representation supported by the API Common helper.");
			}
			for (PageDocument page : evidence.orElseThrow().pages()) {
				for (Map<String, Object> item : page.items()) {
					DeploymentFeaturesSupport.CanonicalLink canonical = DeploymentFeaturesSupport.canonicalLink(item,
							page.source(), this.apiRoot, REQ_CANONICAL_URL);
					String accept = canonical.mediaType().isBlank() ? page.mediaType() : canonical.mediaType();
					Response response = get(canonical.uri(), accept);
					ETSAssert.assertStatus(response, 200, REQ_CANONICAL_URL);
					Map<String, Object> body = parseObject(response, canonical.uri(), REQ_CANONICAL_URL);
					if (!DeploymentFeaturesSupport.withoutCanonicalLinks(item)
						.equals(DeploymentFeaturesSupport.withoutCanonicalLinks(body))) {
						ETSAssert.failWithUri(REQ_CANONICAL_URL, canonical.uri()
								+ " content differs from its collection item after canonical links are removed.");
					}
				}
			}
		}
	}

	/**
	 * REQ-ETS-PART1-004; SCENARIO-ETS-PART1-004-RELEASED-RESOURCES-ENDPOINT-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RESOURCES_ENDPOINT
			+ ": the supplied Deployment resources endpoint returns schema-valid content", groups = GROUP,
			alwaysRun = true)
	public void deploymentResourcesEndpointIsValid() {
		requireApiRoot(REQ_RESOURCES_ENDPOINT);
		validateEndpoint(this.apiRoot.resolve("deployments"), REQ_RESOURCES_ENDPOINT);
	}

	/**
	 * REQ-ETS-PART1-004; SCENARIO-ETS-PART1-004-RELEASED-CANONICAL-ENDPOINT-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_CANONICAL_ENDPOINT
			+ ": the canonical /deployments endpoint satisfies the resources procedure", groups = GROUP,
			alwaysRun = true)
	public void canonicalDeploymentsEndpointIsValid() {
		requireApiRoot(REQ_CANONICAL_ENDPOINT);
		validateEndpoint(this.apiRoot.resolve("deployments"), REQ_CANONICAL_ENDPOINT);
	}

	/**
	 * REQ-ETS-PART1-004; SCENARIO-ETS-PART1-004-RELEASED-COLLECTIONS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_COLLECTIONS
			+ ": advertised Deployment collections use released metadata and schemas", groups = GROUP, alwaysRun = true)
	public void deploymentCollectionsAreValid() {
		List<Map<String, Object>> collections = deploymentCollections(REQ_COLLECTIONS);
		for (Map<String, Object> collection : collections) {
			DeploymentFeaturesSupport.requireDeploymentCollectionMetadata(collection, REQ_COLLECTIONS);
			Optional<TraversalResult> evidence = Part1ApiCommonSupport.collectionItemsDetailed(this.apiRoot, collection,
					DeploymentFeaturesSupport.SUPPORTED_MEDIA_TYPES);
			if (evidence.isEmpty()) {
				throw new SkipException(REQ_COLLECTIONS + " - Deployment collection " + collection.get("id")
						+ " has no GeoJSON or SensorML items representation.");
			}
			URI endpoint = collectionItemsUri(collection);
			if (!DeploymentFeaturesSupport.validateDeploymentEndpoint(endpoint, evidence.orElseThrow().pages(),
					REQ_COLLECTIONS)) {
				throw new SkipException(
						REQ_COLLECTIONS + " - " + endpoint + " returned an unsupported Deployment representation.");
			}
		}
	}

	/**
	 * REQ-ETS-PART1-004; SCENARIO-ETS-PART1-004-RELEASED-REF-FROM-SYSTEM-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_REF_FROM_SYSTEM
			+ ": every System's Deployment endpoint references its owning System", groups = GROUP, alwaysRun = true)
	public void deploymentsReferencedFromSystemsAreValid() {
		requireApiRoot(REQ_REF_FROM_SYSTEM);
		Optional<TraversalResult> systemEvidence = Part1ApiCommonSupport.canonicalResourcesDetailed(this.apiRoot,
				"systems");
		if (systemEvidence.isEmpty()) {
			ETSAssert.failWithUri(REQ_REF_FROM_SYSTEM, this.apiRoot.resolve("systems") + " returned HTTP 404.");
		}
		List<Map<String, Object>> systems = systemEvidence.orElseThrow().items();
		if (systems.isEmpty()) {
			throw new SkipException(REQ_REF_FROM_SYSTEM + " - no canonical System evidence was available.");
		}
		for (Map<String, Object> system : systems) {
			Object idValue = system.get("id");
			if (!(idValue instanceof String) || ((String) idValue).isBlank()) {
				ETSAssert.failWithUri(REQ_REF_FROM_SYSTEM, "canonical Systems response contains a System without id.");
			}
			String systemId = (String) idValue;
			URI endpoint = this.apiRoot.resolve("systems/" + encodePathToken(systemId) + "/deployments");
			Optional<TraversalResult> deployments = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint,
					DEPLOYMENT_ACCEPT, Map.of(), REQ_REF_FROM_SYSTEM, DeploymentFeaturesSupport.SUPPORTED_MEDIA_TYPES);
			if (deployments.isEmpty()) {
				ETSAssert.failWithUri(REQ_REF_FROM_SYSTEM, endpoint + " returned HTTP 404.");
			}
			TraversalResult traversal = deployments.orElseThrow();
			if (!DeploymentFeaturesSupport.validateDeploymentEndpoint(endpoint, traversal.pages(),
					REQ_REF_FROM_SYSTEM)) {
				throw new SkipException(
						REQ_REF_FROM_SYSTEM + " - " + endpoint + " returned an unsupported Deployment representation.");
			}
			for (PageDocument page : traversal.pages()) {
				for (Map<String, Object> deployment : page.items()) {
					if (!DeploymentFeaturesSupport.referencesSystem(deployment, page.mediaType(), systemId,
							this.apiRoot)) {
						ETSAssert.failWithUri(REQ_REF_FROM_SYSTEM, page.source() + " Deployment " + deployment.get("id")
								+ " does not reference owning System " + systemId + ".");
					}
				}
			}
		}
	}

	private void validateEndpoint(URI endpoint, String requirement) {
		requireApiRoot(requirement);
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint, DEPLOYMENT_ACCEPT,
				Map.of(), requirement, DeploymentFeaturesSupport.SUPPORTED_MEDIA_TYPES);
		if (evidence.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " returned HTTP 404.");
		}
		if (!DeploymentFeaturesSupport.validateDeploymentEndpoint(endpoint, evidence.orElseThrow().pages(),
				requirement)) {
			throw new SkipException(
					requirement + " - " + endpoint + " returned an unsupported Deployment representation.");
		}
	}

	private List<Map<String, Object>> deploymentCollections(String requirement) {
		requireApiRoot(requirement);
		URI endpoint = this.apiRoot.resolve("collections");
		Response response = get(endpoint, "application/json");
		ETSAssert.assertStatus(response, 200, requirement);
		Map<String, Object> body = parseObject(response, endpoint, requirement);
		Object advertised = body.get("collections");
		if (!(advertised instanceof List)) {
			ETSAssert.failWithUri(requirement, endpoint + " response is missing a collections array.");
		}
		List<Map<String, Object>> selected = DeploymentFeaturesSupport
			.selectDeploymentCollections((List<?>) advertised);
		if (selected.isEmpty()) {
			ETSAssert.failWithUri(requirement,
					endpoint + " does not advertise a collection with featureType=sosa:Deployment.");
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
			throw new SkipException("Deployment setup skipped before IUT access because prerequisite " + blocker + ".");
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
						+ " Deployment direct procedures will execute, but inherited conformance remains incomplete.",
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

	private static String encodePathToken(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
	}

}
