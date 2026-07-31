package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.feasibility;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.PageDocument;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.TraversalResult;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.apicommon.Part2ApiCommonTests;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.controlstream.Part2ControlStreamSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.controlstream.Part2ControlStreamTests;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Part 2 - Command Feasibility conformance tests ({@code /conf/feasibility}; OGC
 * 23-002 Annex A.4).
 */
public class Part2FeasibilityTests {

	static final String GROUP = "part2feasibility";

	static final String CONF_FEASIBILITY = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/feasibility";

	static final String REQ_FEASIBILITY = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/feasibility";

	static final String REQ_CANONICAL_URL = REQ_FEASIBILITY + "/canonical-url";

	static final String REQ_REF_FROM_CONTROLSTREAM = REQ_FEASIBILITY + "/ref-from-controlstream";

	static final String REQ_STATUS_ENDPOINT = REQ_FEASIBILITY + "/status-endpoint";

	static final String REQ_RESULT_ENDPOINT = REQ_FEASIBILITY + "/result-endpoint";

	static final String REQ_COLLECTIONS = REQ_FEASIBILITY + "/collections";

	private URI iutUri;

	private URI apiRoot;

	/**
	 * Loads immutable suite arguments after the released ControlStream prerequisite.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass(dependsOnGroups = "part2controlstream", alwaysRun = true)
	public void fetchPart2FeasibilityInputs(ITestContext testContext) {
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
		this.iutUri = iut;
		String iutString = this.iutUri.toString();
		this.apiRoot = URI.create(iutString.endsWith("/") ? iutString : iutString + "/");
	}

	/**
	 * REQ-ETS-PART2-004; SCENARIO-ETS-PART2-004-CANONICAL-COMMAND-COLLECTION-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CANONICAL_URL
			+ ": every released A.35 itemType=Command collection item dereferences its advertised canonical URL with equivalent Command content (REQ-ETS-PART2-004, SCENARIO-ETS-PART2-004-CANONICAL-COMMAND-COLLECTION-001)",
			groups = GROUP, alwaysRun = true)
	public void feasibilityCanonicalUrlFromCommandCollections() {
		requireFeasibilityDeclaration(REQ_CANONICAL_URL);
		List<Map<String, Object>> collections = advertisedCollections("Command", REQ_CANONICAL_URL);
		int supportedCollections = 0;
		int inspectedItems = 0;
		for (Map<String, Object> collection : collections) {
			Optional<TraversalResult> evidence = Part1ApiCommonSupport.collectionItemsDetailed(this.apiRoot, collection,
					Part2ControlStreamSupport.JSON_MEDIA);
			if (evidence.isEmpty()) {
				continue;
			}
			supportedCollections++;
			TraversalResult traversal = evidence.orElseThrow();
			URI endpoint = collectionItemsUri(collection, REQ_CANONICAL_URL);
			Part2ControlStreamSupport.validateCommandEndpoint(endpoint, traversal.pages(), REQ_CANONICAL_URL);
			for (PageDocument page : traversal.pages()) {
				for (Map<String, Object> item : page.items()) {
					inspectedItems++;
					URI canonical = Part2ControlStreamSupport.canonicalUri(item, page.source(), this.apiRoot,
							REQ_CANONICAL_URL);
					Response response = given().accept(page.mediaType()).when().get(canonical).andReturn();
					ETSAssert.assertStatus(response, 200, REQ_CANONICAL_URL);
					Map<String, Object> canonicalBody = Part2ControlStreamSupport.parseObject(response, canonical,
							REQ_CANONICAL_URL);
					Part2ControlStreamSupport.validateCommandResource(canonicalBody, REQ_CANONICAL_URL,
							canonical.toString());
					JsonNode expected = Part2ControlStreamSupport.withoutCanonicalLinks(item);
					JsonNode actual = Part2ControlStreamSupport.withoutCanonicalLinks(canonicalBody);
					if (!expected.equals(actual)) {
						ETSAssert.failWithUri(REQ_CANONICAL_URL, canonical
								+ " content differs from its Command collection item after canonical links are removed.");
					}
				}
			}
		}
		skipIfNoSupportedEvidence("Command", supportedCollections, inspectedItems, REQ_CANONICAL_URL);
	}

	/**
	 * REQ-ETS-PART2-004; SCENARIO-ETS-PART2-004-CONTROLSTREAM-COMMAND-REFERENCE-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_REF_FROM_CONTROLSTREAM
			+ ": every canonical ControlStream exposes the released A.36 /controlstreams/{dsId}/commands endpoint as schema-valid Command resources (REQ-ETS-PART2-004, SCENARIO-ETS-PART2-004-CONTROLSTREAM-COMMAND-REFERENCE-001)",
			groups = GROUP, alwaysRun = true)
	public void feasibilityReferenceFromControlStreamUsesReleasedCommandEndpoint() {
		requireFeasibilityDeclaration(REQ_REF_FROM_CONTROLSTREAM);
		for (String controlStreamId : localIds(canonicalResources("controlstreams", REQ_REF_FROM_CONTROLSTREAM),
				REQ_REF_FROM_CONTROLSTREAM)) {
			URI endpoint = this.apiRoot.resolve(releasedControlStreamCommandPath(controlStreamId));
			validateCommandEndpoint(endpoint, REQ_REF_FROM_CONTROLSTREAM);
		}
	}

	/**
	 * REQ-ETS-PART2-004; SCENARIO-ETS-PART2-004-STATUS-RESULT-ENDPOINTS-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_STATUS_ENDPOINT
			+ ": every canonical Feasibility resource exposes a schema-valid /feasibility/{cmdId}/status endpoint (REQ-ETS-PART2-004, SCENARIO-ETS-PART2-004-STATUS-RESULT-ENDPOINTS-001)",
			groups = GROUP, alwaysRun = true)
	public void feasibilityStatusEndpointReadableForEveryFeasibility() {
		requireFeasibilityDeclaration(REQ_STATUS_ENDPOINT);
		for (String feasibilityId : localIds(feasibilityResources(REQ_STATUS_ENDPOINT), REQ_STATUS_ENDPOINT)) {
			URI endpoint = this.apiRoot
				.resolve("feasibility/" + Part2ControlStreamSupport.encodePathToken(feasibilityId) + "/status");
			validateCommandStatusEndpoint(endpoint, REQ_STATUS_ENDPOINT);
		}
	}

	/**
	 * REQ-ETS-PART2-004; SCENARIO-ETS-PART2-004-STATUS-RESULT-ENDPOINTS-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_RESULT_ENDPOINT
			+ ": every canonical Feasibility resource exposes a schema-valid /feasibility/{cmdId}/result endpoint (REQ-ETS-PART2-004, SCENARIO-ETS-PART2-004-STATUS-RESULT-ENDPOINTS-001)",
			groups = GROUP, alwaysRun = true)
	public void feasibilityResultEndpointReadableForEveryFeasibility() {
		requireFeasibilityDeclaration(REQ_RESULT_ENDPOINT);
		for (String feasibilityId : localIds(feasibilityResources(REQ_RESULT_ENDPOINT), REQ_RESULT_ENDPOINT)) {
			URI endpoint = this.apiRoot
				.resolve("feasibility/" + Part2ControlStreamSupport.encodePathToken(feasibilityId) + "/result");
			validateCommandResultEndpoint(endpoint, REQ_RESULT_ENDPOINT);
		}
	}

	/**
	 * REQ-ETS-PART2-004; SCENARIO-ETS-PART2-004-COLLECTION-TAGGING-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COLLECTIONS
			+ ": every advertised itemType=Feasibility collection retrieves schema-valid Command resources (REQ-ETS-PART2-004, SCENARIO-ETS-PART2-004-COLLECTION-TAGGING-001)",
			groups = GROUP, alwaysRun = true)
	public void feasibilityCollectionsValidateCommandSchema() {
		requireFeasibilityDeclaration(REQ_COLLECTIONS);
		List<Map<String, Object>> collections = advertisedCollections("Feasibility", REQ_COLLECTIONS);
		int supportedCollections = 0;
		for (Map<String, Object> collection : collections) {
			Optional<TraversalResult> evidence = Part1ApiCommonSupport.collectionItemsDetailed(this.apiRoot, collection,
					Part2ControlStreamSupport.JSON_MEDIA);
			if (evidence.isEmpty()) {
				continue;
			}
			supportedCollections++;
			Part2ControlStreamSupport.validateCommandEndpoint(collectionItemsUri(collection, REQ_COLLECTIONS),
					evidence.orElseThrow().pages(), REQ_COLLECTIONS);
		}
		if (supportedCollections == 0) {
			throw new SkipException(REQ_COLLECTIONS
					+ " - every advertised Feasibility collection lacked a supported rel=items application/json link.");
		}
	}

	static boolean declaresConformance(Map<String, Object> body, String conformanceUri) {
		return Part2ApiCommonTests.declaresConformance(body, conformanceUri);
	}

	static String releasedControlStreamCommandPath(String controlStreamId) {
		return "controlstreams/" + Part2ControlStreamSupport.encodePathToken(controlStreamId) + "/commands";
	}

	static boolean isCollectionWithItemType(Map<String, Object> collection, String itemType) {
		return collection != null && itemType != null && itemType.equals(collection.get("itemType"));
	}

	static boolean hasItemsOnlyCollectionShape(Map<String, Object> body) {
		return body != null && body.get("items") instanceof List;
	}

	private void requireFeasibilityDeclaration(String requirement) {
		Response response = given().accept("application/json")
			.when()
			.get(this.apiRoot.resolve("conformance"))
			.andReturn();
		ETSAssert.assertStatus(response, 200, requirement);
		Map<String, Object> parsed = Part2ControlStreamSupport.parseObject(response,
				this.apiRoot.resolve("conformance"), requirement);
		ETSAssert.assertJsonObjectHas(parsed, "conformsTo", List.class, requirement);
		if (!declaresConformance(parsed, CONF_FEASIBILITY)) {
			throw new SkipException(CONF_FEASIBILITY
					+ " - IUT does not declare the CS API Part 2 Command Feasibility conformance class in /conformance; no feasibility mutation request was issued.");
		}
	}

	private TraversalResult canonicalResources(String resourceType, String requirement) {
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.canonicalResourcesDetailed(this.apiRoot,
				resourceType);
		if (evidence.isEmpty()) {
			throw new SkipException(requirement + " - canonical /" + resourceType
					+ " endpoint is unavailable; no released procedure evidence is available.");
		}
		TraversalResult traversal = evidence.orElseThrow();
		if ("controlstreams".equals(resourceType)) {
			Part2ControlStreamSupport.validateControlStreamEndpoint(this.apiRoot.resolve("controlstreams"),
					traversal.pages(), requirement);
		}
		return traversal;
	}

	private TraversalResult feasibilityResources(String requirement) {
		TraversalResult traversal = canonicalResources("feasibility", requirement);
		Part2ControlStreamSupport.validateCommandEndpoint(this.apiRoot.resolve("feasibility"), traversal.pages(),
				requirement);
		return traversal;
	}

	private void validateCommandEndpoint(URI endpoint, String requirement) {
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint,
				Part2ControlStreamSupport.JSON, Map.of(), requirement, Part2ControlStreamSupport.JSON_MEDIA);
		if (evidence.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " returned HTTP 404.");
		}
		Part2ControlStreamSupport.validateCommandEndpoint(endpoint, evidence.orElseThrow().pages(), requirement);
	}

	private void validateCommandStatusEndpoint(URI endpoint, String requirement) {
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint,
				Part2ControlStreamSupport.JSON, Map.of(), requirement, Part2ControlStreamSupport.JSON_MEDIA);
		if (evidence.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " returned HTTP 404.");
		}
		Part2ControlStreamSupport.validateCommandStatusEndpoint(endpoint, evidence.orElseThrow().pages(), requirement);
	}

	private void validateCommandResultEndpoint(URI endpoint, String requirement) {
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint,
				Part2ControlStreamSupport.JSON, Map.of(), requirement, Part2ControlStreamSupport.JSON_MEDIA);
		if (evidence.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " returned HTTP 404.");
		}
		Part2ControlStreamSupport.validateCommandResultEndpoint(endpoint, evidence.orElseThrow().pages(), requirement);
	}

	private List<Map<String, Object>> advertisedCollections(String itemType, String requirement) {
		Response response = given().accept("application/json")
			.when()
			.get(this.apiRoot.resolve("collections"))
			.andReturn();
		ETSAssert.assertStatus(response, 200, requirement);
		Map<String, Object> body = Part2ControlStreamSupport.parseObject(response, this.apiRoot.resolve("collections"),
				requirement);
		Object advertised = body.get("collections");
		if (!(advertised instanceof List)) {
			ETSAssert.failWithUri(requirement, "/collections response is missing a collections array.");
		}
		List<Map<String, Object>> typed = new ArrayList<>();
		for (Object value : (List<?>) advertised) {
			if (value instanceof Map) {
				typed.add(castMap(value));
			}
		}
		List<Map<String, Object>> selected = typed.stream()
			.filter(collection -> isCollectionWithItemType(collection, itemType))
			.toList();
		if (selected.isEmpty()) {
			throw new SkipException(requirement + " - /collections does not advertise itemType=" + itemType + ".");
		}
		return selected;
	}

	private URI collectionItemsUri(Map<String, Object> collection, String requirement) {
		String id = requireString(collection, "id", requirement);
		return this.apiRoot.resolve("collections/" + Part2ControlStreamSupport.encodePathToken(id) + "/items");
	}

	private static List<String> localIds(TraversalResult traversal, String requirement) {
		List<String> ids = new ArrayList<>();
		for (Map<String, Object> item : traversal.items()) {
			ids.add(requireString(item, "id", requirement));
		}
		if (ids.isEmpty()) {
			throw new SkipException(requirement + " - canonical resource collection is empty.");
		}
		return List.copyOf(ids);
	}

	private static String requireString(Map<String, Object> parsed, String key, String requirement) {
		ETSAssert.assertJsonObjectHas(parsed, key, String.class, requirement);
		return (String) parsed.get(key);
	}

	private static void skipIfNoSupportedEvidence(String itemType, int supportedCollections, int inspectedItems,
			String requirement) {
		if (supportedCollections == 0) {
			throw new SkipException(requirement + " - every advertised " + itemType
					+ " collection lacked a supported rel=items application/json link.");
		}
		if (inspectedItems == 0) {
			throw new SkipException(requirement + " - supported " + itemType
					+ " collections were empty; no canonical resource evidence is available.");
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> castMap(Object value) {
		return (Map<String, Object>) value;
	}

	private static void skipWhenPrerequisiteUnsatisfied(ITestContext testContext) {
		String blocker = configurationBlocker(testContext.getFailedConfigurations(), "failed");
		if (blocker == null) {
			blocker = configurationBlocker(testContext.getSkippedConfigurations(), "skipped");
		}
		if (blocker == null) {
			blocker = testBlocker(testContext.getFailedTests(), "failed");
		}
		if (blocker == null) {
			blocker = testBlocker(testContext.getSkippedTests(), "skipped");
		}
		if (blocker != null) {
			throw new SkipException(
					"Part 2 Feasibility setup skipped before IUT access because prerequisite " + blocker + ".");
		}
	}

	private static String configurationBlocker(IResultMap results, String status) {
		if (results == null) {
			return null;
		}
		for (ITestResult result : results.getAllResults()) {
			if (result != null && result.getMethod() != null && isControlStreamPrerequisite(result)) {
				return "configuration " + result.getMethod().getMethodName() + " " + status;
			}
		}
		return null;
	}

	private static String testBlocker(IResultMap results, String status) {
		if (results == null) {
			return null;
		}
		for (ITestResult result : results.getAllResults()) {
			if (result != null && result.getMethod() != null && isControlStreamPrerequisite(result)) {
				return "method " + result.getMethod().getMethodName() + " " + status;
			}
		}
		return null;
	}

	private static boolean isControlStreamPrerequisite(ITestResult result) {
		for (String group : result.getMethod().getGroups()) {
			if ("part2controlstream".equals(group)) {
				return true;
			}
		}
		return Part2ControlStreamTests.class.equals(result.getMethod().getRealClass());
	}

}
