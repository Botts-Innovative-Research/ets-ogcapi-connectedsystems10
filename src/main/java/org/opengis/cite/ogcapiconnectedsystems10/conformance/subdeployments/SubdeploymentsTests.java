package org.opengis.cite.ogcapiconnectedsystems10.conformance.subdeployments;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.TestRunArg;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.deployments.DeploymentFeaturesSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport;
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
 * Released OGC 23-001 Annex A procedures for the Subdeployment conformance class.
 */
public class SubdeploymentsTests {

	static final String GROUP = "subdeployments";

	static final String REQ_COLLECTION = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/subdeployment/collection";

	static final String REQ_RECURSIVE_PARAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/subdeployment/recursive-param";

	static final String REQ_RECURSIVE_DEPLOYMENTS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/subdeployment/recursive-search-deployments";

	static final String REQ_RECURSIVE_SUBDEPLOYMENTS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/subdeployment/recursive-search-subdeployments";

	static final String REQ_RECURSIVE_ASSOC = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/subdeployment/recursive-assoc";

	private static final String DEPLOYMENT_ACCEPT = "application/geo+json, application/sml+json, application/json";

	private static final String JSON_ACCEPT = "application/json, application/geo+json, application/sml+json";

	private static final Set<String> DEPLOYMENT_MEDIA_TYPES = Set.of("application/geo+json", "application/sml+json");

	private static final Set<String> ASSOCIATION_MEDIA_TYPES = Set.of("application/json", "application/geo+json",
			"application/sml+json");

	private static final List<String> ASSOCIATIONS = List.of("deployedSystems", "featuresOfInterest",
			"samplingFeatures", "datastreams", "controlstreams");

	private static final int MAX_HIERARCHY_NODES = 10_000;

	private URI apiRoot;

	private SubdeploymentsSupport.AssociationEvidence associationEvidence = SubdeploymentsSupport
		.associationEvidence(null);

	/**
	 * Loads immutable arguments after inherited Deployment procedures complete.
	 * @param testContext active TestNG context.
	 */
	@BeforeClass(dependsOnGroups = "deployments", alwaysRun = true)
	public void fetchSubdeploymentArguments(ITestContext testContext) {
		skipWhenPrerequisiteUnsatisfied(testContext);
		Object iut = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iut instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		configure((URI) iut);
		String fixture = testContext.getSuite()
			.getXmlSuite()
			.getParameter(TestRunArg.SUBDEPLOYMENT_ASSOCIATION_EVIDENCE.toString());
		if (fixture != null && !fixture.isBlank()) {
			configureAssociationEvidence(fixture);
		}
	}

	void configure(URI iut) {
		if (iut == null || !iut.isAbsolute()) {
			throw new IllegalArgumentException("IUT must be an absolute URI.");
		}
		String value = iut.toString();
		this.apiRoot = URI.create(value.endsWith("/") ? value : value + "/");
	}

	void configureAssociationEvidence(String source) {
		this.associationEvidence = SubdeploymentsSupport.associationEvidence(source);
	}

	static void skipWhenPrerequisiteUnsatisfied(ITestContext testContext) {
		String blocker = resultBlocker(testContext.getFailedConfigurations(), "failed");
		if (blocker == null) {
			blocker = resultBlocker(testContext.getSkippedConfigurations(), "skipped");
		}
		if (blocker == null) {
			blocker = resultBlocker(testContext.getFailedTests(), "failed");
		}
		if (blocker == null) {
			blocker = resultBlocker(testContext.getSkippedTests(), "skipped");
		}
		if (blocker != null) {
			throw new SkipException(
					"Subdeployment setup skipped before IUT access because prerequisite " + blocker + ".");
		}
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-COLLECTION-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_COLLECTION
			+ ": parent Deployments expose exact schema-valid Subdeployment collections", groups = GROUP,
			alwaysRun = true)
	public void subdeploymentCollectionIsValid() {
		HierarchyEvidence evidence = hierarchy(REQ_COLLECTION);
		if (evidence.hierarchy().parents().isEmpty()) {
			throw new SkipException(
					REQ_COLLECTION + " - the IUT exposes no parent Deployment with direct Subdeployment data.");
		}
		for (String parent : evidence.hierarchy().parents()) {
			URI parentUri = SubdeploymentsSupport.deploymentUri(this.apiRoot, parent);
			Map<String, Object> parentBody = deploymentResource(parentUri, REQ_COLLECTION);
			URI endpoint = SubdeploymentsSupport.subdeploymentsUri(parentBody, parentUri, this.apiRoot, parent,
					REQ_COLLECTION);
			TraversalResult collection = requireDeploymentTraversal(endpoint, Map.of(), REQ_COLLECTION);
			validateDeploymentTraversal(endpoint, collection, REQ_COLLECTION);
		}
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-RECURSIVE-PARAM-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RECURSIVE_PARAM
			+ ": recursive accepts the exact boolean values true and false", groups = GROUP, alwaysRun = true)
	public void recursiveParameterUsesBooleanValues() {
		requireConfigured(REQ_RECURSIVE_PARAM);
		URI deployments = this.apiRoot.resolve("deployments");
		ETSAssert.assertStatus(get(deployments, DEPLOYMENT_ACCEPT, Map.of("recursive", "false")), 200,
				REQ_RECURSIVE_PARAM);
		ETSAssert.assertStatus(get(deployments, DEPLOYMENT_ACCEPT, Map.of("recursive", "true")), 200,
				REQ_RECURSIVE_PARAM);
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-RECURSIVE-DEPLOYMENTS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RECURSIVE_DEPLOYMENTS
			+ ": /deployments recursive results match independently discovered hierarchy edges", groups = GROUP,
			alwaysRun = true)
	public void deploymentsRecursiveSearchIsComplete() {
		HierarchyEvidence evidence = hierarchy(REQ_RECURSIVE_DEPLOYMENTS);
		if (evidence.hierarchy().childNodes().isEmpty()) {
			throw new SkipException(REQ_RECURSIVE_DEPLOYMENTS + " - the IUT exposes no Subdeployment hierarchy data.");
		}
		URI deployments = this.apiRoot.resolve("deployments");
		Set<String> defaultIds = deploymentIds(deployments, Map.of(), REQ_RECURSIVE_DEPLOYMENTS);
		Set<String> falseIds = deploymentIds(deployments, Map.of("recursive", "false"), REQ_RECURSIVE_DEPLOYMENTS);
		Set<String> trueIds = deploymentIds(deployments, Map.of("recursive", "true"), REQ_RECURSIVE_DEPLOYMENTS);
		SubdeploymentsSupport.assertRecursiveDeployments(evidence.hierarchy(), defaultIds, falseIds, trueIds,
				REQ_RECURSIVE_DEPLOYMENTS);
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-RECURSIVE-SUBDEPLOYMENTS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RECURSIVE_SUBDEPLOYMENTS
			+ ": nested recursive results distinguish direct and transitive Subdeployments", groups = GROUP,
			alwaysRun = true)
	public void subdeploymentsRecursiveSearchIsComplete() {
		HierarchyEvidence evidence = hierarchy(REQ_RECURSIVE_SUBDEPLOYMENTS);
		String parent = evidence.hierarchy()
			.parents()
			.stream()
			.filter(candidate -> !evidence.hierarchy().transitiveDescendants(candidate).isEmpty())
			.findFirst()
			.orElseThrow(() -> new SkipException(REQ_RECURSIVE_SUBDEPLOYMENTS
					+ " - the IUT exposes no Subdeployment hierarchy with transitive descendants."));
		URI endpoint = SubdeploymentsSupport.subdeploymentCollectionUri(this.apiRoot, parent);
		Set<String> defaultIds = deploymentIds(endpoint, Map.of(), REQ_RECURSIVE_SUBDEPLOYMENTS);
		Set<String> falseIds = deploymentIds(endpoint, Map.of("recursive", "false"), REQ_RECURSIVE_SUBDEPLOYMENTS);
		Set<String> trueIds = deploymentIds(endpoint, Map.of("recursive", "true"), REQ_RECURSIVE_SUBDEPLOYMENTS);
		SubdeploymentsSupport.assertRecursiveSubdeployments(parent, evidence.hierarchy(), defaultIds, falseIds, trueIds,
				REQ_RECURSIVE_SUBDEPLOYMENTS);
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-RECURSIVE-ASSOC-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RECURSIVE_ASSOC
			+ ": Deployment associations include resources from every Subdeployment", groups = GROUP, alwaysRun = true)
	public void recursiveAssociationsIncludeDescendants() {
		HierarchyEvidence evidence = hierarchy(REQ_RECURSIVE_ASSOC);
		if (evidence.hierarchy().parents().isEmpty()) {
			throw new SkipException(
					REQ_RECURSIVE_ASSOC + " - the IUT exposes no parent Deployment with Subdeployments.");
		}
		int advertisedAssociations = 0;
		Set<String> incompleteComparisons = new LinkedHashSet<>();
		for (String parent : evidence.hierarchy().parents()) {
			URI parentSource = SubdeploymentsSupport.deploymentUri(this.apiRoot, parent);
			Map<String, Object> parentResource = deploymentResource(parentSource, REQ_RECURSIVE_ASSOC);
			for (String association : ASSOCIATIONS) {
				if (!SubdeploymentsSupport.associationAdvertised(parentResource, association)) {
					continue;
				}
				advertisedAssociations++;
				Optional<URI> parentEndpoint = SubdeploymentsSupport.associationUri(parentResource, parentSource,
						this.apiRoot, association, REQ_RECURSIVE_ASSOC);
				if (parentEndpoint.isEmpty()) {
					incompleteComparisons.add(parent + "/" + association + " has no safe JSON-compatible link");
					continue;
				}
				Optional<Set<String>> expected = this.associationEvidence.expectedIds(parent, evidence.hierarchy(),
						association);
				if (expected.isEmpty()) {
					incompleteComparisons.add(parent + "/" + association + " has no complete independent evidence");
					continue;
				}
				URI endpoint = parentEndpoint.orElseThrow();
				Set<String> actual = associationIds(endpoint, REQ_RECURSIVE_ASSOC);
				SubdeploymentsSupport.assertIncludes(actual, expected.orElseThrow(), endpoint, association,
						REQ_RECURSIVE_ASSOC);
			}
		}
		if (advertisedAssociations == 0) {
			throw new SkipException(
					REQ_RECURSIVE_ASSOC + " - parent Deployments advertise none of the released association links.");
		}
		if (!incompleteComparisons.isEmpty()) {
			throw new SkipException(
					REQ_RECURSIVE_ASSOC + " - comparison skipped because " + incompleteComparisons + ".");
		}
	}

	private HierarchyEvidence hierarchy(String requirement) {
		requireConfigured(requirement);
		URI deployments = this.apiRoot.resolve("deployments");
		TraversalResult rootsTraversal = requireDeploymentTraversal(deployments, Map.of(), requirement);
		validateDeploymentTraversal(deployments, rootsTraversal, requirement);
		Set<String> roots = SubdeploymentsSupport.ids(rootsTraversal, deployments, requirement);
		Map<String, List<String>> direct = new LinkedHashMap<>();
		Deque<String> pending = new ArrayDeque<>(roots);
		Set<String> processed = new LinkedHashSet<>();
		while (!pending.isEmpty()) {
			String parent = pending.removeFirst();
			if (!processed.add(parent)) {
				continue;
			}
			if (processed.size() > MAX_HIERARCHY_NODES) {
				ETSAssert.failWithUri(requirement,
						"hierarchy discovery exceeded the " + MAX_HIERARCHY_NODES + " node safety bound.");
			}
			URI endpoint = SubdeploymentsSupport.subdeploymentCollectionUri(this.apiRoot, parent);
			Optional<TraversalResult> response = deploymentResourcesAtEndpoint(endpoint, Map.of(), requirement);
			List<String> children = List.of();
			if (response.isPresent()) {
				TraversalResult traversal = response.orElseThrow();
				validateDeploymentTraversal(endpoint, traversal, requirement);
				children = new ArrayList<>(SubdeploymentsSupport.ids(traversal, endpoint, requirement));
			}
			direct.put(parent, List.copyOf(children));
			for (String child : children) {
				if (!processed.contains(child)) {
					pending.addLast(child);
				}
			}
		}
		return new HierarchyEvidence(SubdeploymentsSupport.hierarchy(direct));
	}

	private Set<String> deploymentIds(URI endpoint, Map<String, String> query, String requirement) {
		TraversalResult traversal = requireDeploymentTraversal(endpoint, query, requirement);
		validateDeploymentTraversal(endpoint, traversal, requirement);
		return SubdeploymentsSupport.ids(traversal, endpoint, requirement);
	}

	private Set<String> associationIds(URI endpoint, String requirement) {
		Optional<TraversalResult> response = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint, JSON_ACCEPT, Map.of(),
				requirement, ASSOCIATION_MEDIA_TYPES);
		if (response.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " was advertised but returned HTTP 404.");
		}
		return SubdeploymentsSupport.ids(response.orElseThrow(), endpoint, requirement);
	}

	private Map<String, Object> deploymentResource(URI endpoint, String requirement) {
		Response response = get(endpoint, DEPLOYMENT_ACCEPT);
		ETSAssert.assertStatus(response, 200, requirement);
		requireSupportedDeploymentMediaType(response, endpoint, requirement);
		return parseObject(response, endpoint, requirement);
	}

	private TraversalResult requireDeploymentTraversal(URI endpoint, Map<String, String> query, String requirement) {
		Optional<TraversalResult> traversal = deploymentResourcesAtEndpoint(endpoint, query, requirement);
		if (traversal.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " returned HTTP 404.");
		}
		return traversal.orElseThrow();
	}

	private Optional<TraversalResult> deploymentResourcesAtEndpoint(URI endpoint, Map<String, String> query,
			String requirement) {
		requireConfigured(requirement);
		return Part1ApiCommonSupport.resourcesAtEndpoint(endpoint, DEPLOYMENT_ACCEPT, query, requirement,
				DEPLOYMENT_MEDIA_TYPES);
	}

	private static void validateDeploymentTraversal(URI endpoint, TraversalResult traversal, String requirement) {
		if (!DeploymentFeaturesSupport.validateDeploymentEndpoint(endpoint, traversal.pages(), requirement)) {
			throw new SkipException(
					requirement + " - " + endpoint + " returned a media type unsupported by this testing engine.");
		}
	}

	private void requireConfigured(String requirement) {
		if (this.apiRoot == null) {
			throw new SkipException(requirement + " - API root fixture is unavailable.");
		}
	}

	private static Response get(URI endpoint, String accept) {
		return get(endpoint, accept, Map.of());
	}

	private static Response get(URI endpoint, String accept, Map<String, String> query) {
		return given().accept(accept).queryParams(query).when().get(endpoint).andReturn();
	}

	private static void requireSupportedDeploymentMediaType(Response response, URI source, String requirement) {
		String contentType = response.getContentType();
		String mediaType = contentType == null ? "" : contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
		if (DEPLOYMENT_MEDIA_TYPES.contains(mediaType)) {
			return;
		}
		String detail = mediaType.isEmpty() ? "no Content-Type" : "unsupported media type '" + mediaType + "'";
		Reporter.log(requirement + " - " + source + " returned " + detail + "; representation parsing skipped.", true);
		throw new SkipException(
				requirement + " - " + source + " returned " + detail + "; representation parsing skipped.");
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

	private static String resultBlocker(IResultMap results, String status) {
		if (results == null) {
			return null;
		}
		for (ITestResult result : results.getAllResults()) {
			if (result == null || result.getMethod() == null || !isInheritedPrerequisite(result)) {
				continue;
			}
			return "method " + result.getMethod().getMethodName() + " " + status;
		}
		return null;
	}

	private static boolean isInheritedPrerequisite(ITestResult result) {
		String[] groups = result.getMethod().getGroups();
		if (groups != null) {
			for (String group : groups) {
				if ("core".equals(group) || "common".equals(group) || "part1apicommon".equals(group)
						|| "deployments".equals(group)) {
					return true;
				}
			}
		}
		Class<?> realClass = result.getMethod().getRealClass();
		if (realClass == null || realClass.getPackageName() == null) {
			return false;
		}
		String packageName = realClass.getPackageName();
		return packageName.contains(".conformance.core") || packageName.contains(".conformance.common")
				|| packageName.contains(".conformance.part1.apicommon")
				|| packageName.contains(".conformance.deployments");
	}

	private record HierarchyEvidence(SubdeploymentsSupport.Hierarchy hierarchy) {
	}

}
