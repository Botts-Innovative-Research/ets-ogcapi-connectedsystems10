package org.opengis.cite.ogcapiconnectedsystems10.conformance.subsystems;

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
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.TraversalResult;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonTests;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.systemfeatures.SystemFeaturesSupport;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * Released OGC 23-001 Annex A procedures for the Subsystem conformance class.
 */
public class SubsystemsTests {

	static final String GROUP = "subsystems";

	static final String REQ_COLLECTION = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/subsystem/collection";

	static final String REQ_RECURSIVE_PARAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/subsystem/recursive-param";

	static final String REQ_RECURSIVE_SYSTEMS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/subsystem/recursive-search-systems";

	static final String REQ_RECURSIVE_SUBSYSTEMS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/subsystem/recursive-search-subsystems";

	static final String REQ_RECURSIVE_ASSOC = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/subsystem/recursive-assoc";

	private static final String REQ_SYSTEM_LOCATION_TIME = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/location-time";

	private static final String REQ_SYSTEM_RESOURCES_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/resources-endpoint";

	private static final String REQ_SYSTEM_CANONICAL_ENDPOINT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/canonical-endpoint";

	private static final String SYSTEM_ACCEPT = "application/geo+json, application/sml+json, application/json";

	private static final String JSON_ACCEPT = "application/json, application/geo+json, application/sml+json";

	private static final Set<String> SYSTEM_MEDIA_TYPES = Set.of("application/geo+json", "application/sml+json");

	private static final int MAX_HIERARCHY_NODES = 10_000;

	private static final List<String> ASSOCIATIONS = List.of("samplingFeatures", "datastreams", "controlstreams");

	private URI apiRoot;

	/**
	 * Loads immutable arguments after inherited System procedures complete.
	 * @param testContext active TestNG context.
	 */
	@BeforeClass(dependsOnGroups = "systemfeatures", alwaysRun = true)
	public void fetchSubsystemArguments(ITestContext testContext) {
		skipWhenPrerequisiteUnsatisfied(testContext);
		Object iut = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iut instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		configure((URI) iut);
	}

	void configure(URI iut) {
		if (iut == null || !iut.isAbsolute()) {
			throw new IllegalArgumentException("IUT must be an absolute URI.");
		}
		String value = iut.toString();
		this.apiRoot = URI.create(value.endsWith("/") ? value : value + "/");
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
			throw new SkipException("Subsystem setup skipped before IUT access because prerequisite " + blocker + ".");
		}
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-COLLECTION-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_COLLECTION
			+ ": parent Systems expose schema-valid canonical subsystem collections", groups = GROUP, alwaysRun = true)
	public void subsystemCollectionIsValid() {
		requireConfigured(REQ_COLLECTION);
		URI systems = this.apiRoot.resolve("systems");
		Set<String> roots = SubsystemsSupport.ids(requireSystemTraversal(systems, Map.of(), REQ_COLLECTION), systems,
				REQ_COLLECTION);
		for (String parent : roots) {
			URI endpoint = SubsystemsSupport.subsystemCollectionUri(this.apiRoot, parent);
			Optional<TraversalResult> response = systemResourcesAtEndpoint(endpoint, Map.of(), REQ_COLLECTION);
			if (response.isEmpty() || response.orElseThrow().items().isEmpty()) {
				continue;
			}
			URI parentUri = SubsystemsSupport.systemUri(this.apiRoot, parent);
			Response parentResponse = get(parentUri, SYSTEM_ACCEPT);
			ETSAssert.assertStatus(parentResponse, 200, REQ_COLLECTION);
			requireSupportedSystemMediaType(parentResponse, parentUri, REQ_COLLECTION);
			Map<String, Object> parentBody = parseObject(parentResponse, parentUri, REQ_COLLECTION);
			SubsystemsSupport.subsystemsUri(parentBody, parentUri, this.apiRoot, parent, REQ_COLLECTION);
			TraversalResult collection = response.orElseThrow();
			if (!SystemFeaturesSupport.validateSystemEndpoint(endpoint, collection.pages(), REQ_COLLECTION)) {
				throw new SkipException(REQ_COLLECTION + " - " + endpoint
						+ " returned a media type unsupported by this testing engine.");
			}
			return;
		}
		throw new SkipException(REQ_COLLECTION + " - the IUT exposes no parent System with direct subsystem data.");
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-RECURSIVE-PARAM-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RECURSIVE_PARAM
			+ ": recursive accepts the exact boolean values true and false", groups = GROUP, alwaysRun = true)
	public void recursiveParameterUsesBooleanValues() {
		URI systems = this.apiRoot.resolve("systems");
		requireConfigured(REQ_RECURSIVE_PARAM);
		ETSAssert.assertStatus(get(systems, SYSTEM_ACCEPT, Map.of("recursive", "false")), 200, REQ_RECURSIVE_PARAM);
		ETSAssert.assertStatus(get(systems, SYSTEM_ACCEPT, Map.of("recursive", "true")), 200, REQ_RECURSIVE_PARAM);
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-RECURSIVE-SYSTEMS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RECURSIVE_SYSTEMS
			+ ": /systems recursive results match independently discovered hierarchy edges", groups = GROUP,
			alwaysRun = true)
	public void systemsRecursiveSearchIsComplete() {
		HierarchyEvidence evidence = hierarchy(REQ_RECURSIVE_SYSTEMS);
		if (evidence.hierarchy().childNodes().isEmpty()) {
			throw new SkipException(REQ_RECURSIVE_SYSTEMS + " - the IUT exposes no subsystem hierarchy data.");
		}
		URI systems = this.apiRoot.resolve("systems");
		Set<String> defaultIds = SubsystemsSupport.ids(
				requireTraversal(systems, Map.of(), SYSTEM_ACCEPT, REQ_RECURSIVE_SYSTEMS), systems,
				REQ_RECURSIVE_SYSTEMS);
		Set<String> falseIds = SubsystemsSupport.ids(
				requireTraversal(systems, Map.of("recursive", "false"), SYSTEM_ACCEPT, REQ_RECURSIVE_SYSTEMS), systems,
				REQ_RECURSIVE_SYSTEMS);
		Set<String> trueIds = SubsystemsSupport.ids(
				requireTraversal(systems, Map.of("recursive", "true"), SYSTEM_ACCEPT, REQ_RECURSIVE_SYSTEMS), systems,
				REQ_RECURSIVE_SYSTEMS);
		SubsystemsSupport.assertRecursiveSystems(evidence.hierarchy(), defaultIds, falseIds, trueIds,
				REQ_RECURSIVE_SYSTEMS);
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-RECURSIVE-SUBSYSTEMS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RECURSIVE_SUBSYSTEMS
			+ ": nested recursive results distinguish direct and transitive descendants", groups = GROUP,
			alwaysRun = true)
	public void subsystemsRecursiveSearchIsComplete() {
		HierarchyEvidence evidence = hierarchy(REQ_RECURSIVE_SUBSYSTEMS);
		String parent = evidence.hierarchy()
			.parents()
			.stream()
			.filter(candidate -> !evidence.hierarchy().transitiveDescendants(candidate).isEmpty())
			.findFirst()
			.orElseThrow(() -> new SkipException(
					REQ_RECURSIVE_SUBSYSTEMS + " - the IUT exposes no hierarchy with transitive descendants."));
		URI endpoint = SubsystemsSupport.subsystemCollectionUri(this.apiRoot, parent);
		Set<String> defaultIds = SubsystemsSupport.ids(
				requireTraversal(endpoint, Map.of(), SYSTEM_ACCEPT, REQ_RECURSIVE_SUBSYSTEMS), endpoint,
				REQ_RECURSIVE_SUBSYSTEMS);
		Set<String> falseIds = SubsystemsSupport.ids(
				requireTraversal(endpoint, Map.of("recursive", "false"), SYSTEM_ACCEPT, REQ_RECURSIVE_SUBSYSTEMS),
				endpoint, REQ_RECURSIVE_SUBSYSTEMS);
		Set<String> trueIds = SubsystemsSupport.ids(
				requireTraversal(endpoint, Map.of("recursive", "true"), SYSTEM_ACCEPT, REQ_RECURSIVE_SUBSYSTEMS),
				endpoint, REQ_RECURSIVE_SUBSYSTEMS);
		SubsystemsSupport.assertRecursiveSubsystems(parent, evidence.hierarchy(), defaultIds, falseIds, trueIds,
				REQ_RECURSIVE_SUBSYSTEMS);
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-RECURSIVE-ASSOC-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RECURSIVE_ASSOC
			+ ": parent associations include resources from every descendant", groups = GROUP, alwaysRun = true)
	public void nestedAssociationsAreIncluded() {
		HierarchyEvidence evidence = hierarchy(REQ_RECURSIVE_ASSOC);
		if (evidence.hierarchy().parents().isEmpty()) {
			throw new SkipException(REQ_RECURSIVE_ASSOC + " - the IUT exposes no parent System with subsystems.");
		}
		int supportedTypes = 0;
		int descendantResources = 0;
		for (String association : ASSOCIATIONS) {
			if (!associationImplemented(association)) {
				continue;
			}
			supportedTypes++;
			descendantResources += associations(evidence.hierarchy(), association);
		}
		if (supportedTypes == 0) {
			throw new SkipException(
					REQ_RECURSIVE_ASSOC + " - the IUT exposes none of the released association resource types.");
		}
		if (descendantResources == 0) {
			throw new SkipException(
					REQ_RECURSIVE_ASSOC + " - descendant Systems expose no association resources to compare.");
		}
	}

	private boolean associationImplemented(String association) {
		URI endpoint = this.apiRoot.resolve(association);
		Response response = get(endpoint, JSON_ACCEPT);
		if (response.getStatusCode() == 404) {
			return false;
		}
		ETSAssert.assertStatus(response, 200, REQ_RECURSIVE_ASSOC);
		return true;
	}

	private int associations(SubsystemsSupport.Hierarchy hierarchy, String association) {
		Map<String, TraversalResult> parentResponses = new LinkedHashMap<>();
		for (String parent : hierarchy.parents()) {
			URI endpoint = SubsystemsSupport.associationUri(this.apiRoot, parent, association);
			parentResponses.put(parent, requireTraversal(endpoint, Map.of(), JSON_ACCEPT, REQ_RECURSIVE_ASSOC));
		}

		int descendantResources = 0;
		for (Map.Entry<String, TraversalResult> parentResponse : parentResponses.entrySet()) {
			Set<String> expected = new LinkedHashSet<>();
			for (String descendant : hierarchy.descendants(parentResponse.getKey())) {
				URI descendantEndpoint = SubsystemsSupport.associationUri(this.apiRoot, descendant, association);
				TraversalResult descendantResponse = requireTraversal(descendantEndpoint, Map.of(), JSON_ACCEPT,
						REQ_RECURSIVE_ASSOC);
				Set<String> ids = SubsystemsSupport.ids(descendantResponse, descendantEndpoint, REQ_RECURSIVE_ASSOC);
				expected.addAll(ids);
				descendantResources += ids.size();
			}
			URI parentEndpoint = SubsystemsSupport.associationUri(this.apiRoot, parentResponse.getKey(), association);
			Set<String> actual = SubsystemsSupport.ids(parentResponse.getValue(), parentEndpoint, REQ_RECURSIVE_ASSOC);
			Set<String> missing = new LinkedHashSet<>(expected);
			missing.removeAll(actual);
			if (!missing.isEmpty()) {
				ETSAssert.failWithUri(REQ_RECURSIVE_ASSOC,
						parentEndpoint + " omits descendant " + association + " resource ids " + missing + ".");
			}
		}
		return descendantResources;
	}

	private HierarchyEvidence hierarchy(String requirement) {
		requireConfigured(requirement);
		URI systems = this.apiRoot.resolve("systems");
		TraversalResult rootsTraversal = requireSystemTraversal(systems, Map.of(), requirement);
		Set<String> roots = SubsystemsSupport.ids(rootsTraversal, systems, requirement);
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
			URI endpoint = SubsystemsSupport.subsystemCollectionUri(this.apiRoot, parent);
			Optional<TraversalResult> response = systemResourcesAtEndpoint(endpoint, Map.of(), requirement);
			List<String> children = response.isEmpty() ? List.of()
					: new ArrayList<>(SubsystemsSupport.ids(response.orElseThrow(), endpoint, requirement));
			direct.put(parent, List.copyOf(children));
			for (String child : children) {
				if (!processed.contains(child)) {
					pending.addLast(child);
				}
			}
		}
		return new HierarchyEvidence(SubsystemsSupport.hierarchy(direct));
	}

	private TraversalResult requireTraversal(URI endpoint, Map<String, String> query, String accept,
			String requirement) {
		requireConfigured(requirement);
		Optional<TraversalResult> traversal = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint, accept, query,
				requirement);
		if (traversal.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " returned HTTP 404.");
		}
		return traversal.orElseThrow();
	}

	private TraversalResult requireSystemTraversal(URI endpoint, Map<String, String> query, String requirement) {
		Optional<TraversalResult> traversal = systemResourcesAtEndpoint(endpoint, query, requirement);
		if (traversal.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " returned HTTP 404.");
		}
		return traversal.orElseThrow();
	}

	private Optional<TraversalResult> systemResourcesAtEndpoint(URI endpoint, Map<String, String> query,
			String requirement) {
		requireConfigured(requirement);
		return Part1ApiCommonSupport.resourcesAtEndpoint(endpoint, SYSTEM_ACCEPT, query, requirement,
				SYSTEM_MEDIA_TYPES);
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

	private static void requireSupportedSystemMediaType(Response response, URI source, String requirement) {
		String contentType = response.getContentType();
		String mediaType = contentType == null ? "" : contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
		if (SYSTEM_MEDIA_TYPES.contains(mediaType)) {
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

	private static String testBlocker(IResultMap results, String status, boolean allowEvidenceLimitations) {
		if (results == null) {
			return null;
		}
		for (ITestResult result : results.getAllResults()) {
			if (result == null || result.getMethod() == null || !isInheritedPrerequisite(result)) {
				continue;
			}
			if (allowEvidenceLimitations && isAllowedEvidenceLimitation(result)) {
				Reporter
					.log("Subsystem direct procedures will execute despite documented inherited evidence limitation "
							+ result.getMethod().getMethodName() + ".", true);
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
		return false;
	}

	private static boolean isAllowedEvidenceLimitation(ITestResult result) {
		Throwable throwable = result.getThrowable();
		if (!(throwable instanceof SkipException) || throwable.getMessage() == null) {
			return false;
		}
		String method = result.getMethod().getMethodName();
		String reason = throwable.getMessage();
		if ("datetimeUsesValidTime".equals(method)) {
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

	private record HierarchyEvidence(SubsystemsSupport.Hierarchy hierarchy) {
	}

}
