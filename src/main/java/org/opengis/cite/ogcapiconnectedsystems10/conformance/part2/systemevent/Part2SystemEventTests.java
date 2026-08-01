package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.systemevent;

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
import org.opengis.cite.ogcapiconnectedsystems10.conformance.systemfeatures.SystemFeaturesTests;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Part 2 - System Events conformance tests ({@code /conf/system-event}; OGC 23-002
 * Annex A.5).
 */
public class Part2SystemEventTests {

	static final String GROUP = "part2systemevent";

	static final String CONF_SYSTEM_EVENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/system-event";

	static final String REQ_SYSTEM_EVENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/system-event";

	static final String REQ_CANONICAL_URL = REQ_SYSTEM_EVENT + "/canonical-url";

	static final String REQ_RESOURCES_ENDPOINT = REQ_SYSTEM_EVENT + "/resources-endpoint";

	static final String REQ_CANONICAL_ENDPOINT = REQ_SYSTEM_EVENT + "/canonical-endpoint";

	static final String REQ_REF_FROM_SYSTEM = REQ_SYSTEM_EVENT + "/ref-from-system";

	static final String REQ_COLLECTIONS = REQ_SYSTEM_EVENT + "/collections";

	private URI iutUri;

	private URI apiRoot;

	/**
	 * Loads immutable suite arguments after the released API Common and System
	 * prerequisites.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass(dependsOnGroups = { "part2apicommon", "systemfeatures" }, alwaysRun = true)
	public void fetchPart2SystemEventInputs(ITestContext testContext) {
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
	 * REQ-ETS-PART2-005; SCENARIO-ETS-PART2-005-CANONICAL-CONTROLSTREAM-COLLECTION-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CANONICAL_URL
			+ ": every released A.40 itemType=ControlStream collection item dereferences its advertised canonical URL with equivalent content (REQ-ETS-PART2-005, SCENARIO-ETS-PART2-005-CANONICAL-CONTROLSTREAM-COLLECTION-001)",
			groups = GROUP, alwaysRun = true)
	public void systemEventCanonicalUrlFromControlStreamCollections() {
		requireSystemEventDeclaration(REQ_CANONICAL_URL);
		List<Map<String, Object>> collections = advertisedCollections("ControlStream", REQ_CANONICAL_URL);
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
			Part2ControlStreamSupport.validateControlStreamEndpoint(endpoint, traversal.pages(), REQ_CANONICAL_URL);
			for (PageDocument page : traversal.pages()) {
				for (Map<String, Object> item : page.items()) {
					inspectedItems++;
					URI canonical = Part2SystemEventSupport.canonicalUri(item, page.source(), this.apiRoot,
							REQ_CANONICAL_URL);
					Response response = given().accept(page.mediaType()).when().get(canonical).andReturn();
					ETSAssert.assertStatus(response, 200, REQ_CANONICAL_URL);
					Map<String, Object> canonicalBody = Part2SystemEventSupport.parseObject(response, canonical,
							REQ_CANONICAL_URL);
					JsonNode expected = Part2SystemEventSupport.withoutCanonicalLinks(item);
					JsonNode actual = Part2SystemEventSupport.withoutCanonicalLinks(canonicalBody);
					if (!expected.equals(actual)) {
						ETSAssert.failWithUri(REQ_CANONICAL_URL, canonical
								+ " content differs from its ControlStream collection item after canonical links are removed.");
					}
				}
			}
		}
		skipIfNoSupportedEvidence("ControlStream", supportedCollections, inspectedItems, REQ_CANONICAL_URL);
	}

	/**
	 * REQ-ETS-PART2-005; SCENARIO-ETS-PART2-005-RESOURCE-ENDPOINT-SCHEMA-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_RESOURCES_ENDPOINT
			+ ": the SystemEvent resources endpoint returns schema-valid JSON content (REQ-ETS-PART2-005, SCENARIO-ETS-PART2-005-RESOURCE-ENDPOINT-SCHEMA-001)",
			groups = GROUP, alwaysRun = true)
	public void systemEventResourcesEndpointReadable() {
		requireSystemEventDeclaration(REQ_RESOURCES_ENDPOINT);
		validateSystemEventEndpoint(this.apiRoot.resolve(systemEventsCanonicalPath()), REQ_RESOURCES_ENDPOINT);
	}

	/**
	 * REQ-ETS-PART2-005; SCENARIO-ETS-PART2-005-CANONICAL-ENDPOINT-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CANONICAL_ENDPOINT
			+ ": canonical /systemEvents satisfies the released SystemEvent resources endpoint procedure (REQ-ETS-PART2-005, SCENARIO-ETS-PART2-005-CANONICAL-ENDPOINT-001)",
			groups = GROUP, alwaysRun = true)
	public void systemEventsCanonicalEndpointReadable() {
		requireSystemEventDeclaration(REQ_CANONICAL_ENDPOINT);
		validateSystemEventEndpoint(this.apiRoot.resolve(systemEventsCanonicalPath()), REQ_CANONICAL_ENDPOINT);
	}

	/**
	 * REQ-ETS-PART2-005; SCENARIO-ETS-PART2-005-SYSTEM-REFERENCE-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_REF_FROM_SYSTEM
			+ ": every canonical System exposes the released A.43 /systems/{sysId}/systemEvents endpoint as schema-valid SystemEvent resources (REQ-ETS-PART2-005, SCENARIO-ETS-PART2-005-SYSTEM-REFERENCE-001)",
			groups = GROUP, alwaysRun = true)
	public void systemEventsReferenceFromSystemsUsesReleasedPath() {
		requireSystemEventDeclaration(REQ_REF_FROM_SYSTEM);
		for (String systemId : localIds(canonicalResources("systems", REQ_REF_FROM_SYSTEM), REQ_REF_FROM_SYSTEM)) {
			URI endpoint = this.apiRoot.resolve(releasedSystemScopedSystemEventsPath(systemId));
			validateSystemEventEndpoint(endpoint, REQ_REF_FROM_SYSTEM);
		}
	}

	/**
	 * REQ-ETS-PART2-005; SCENARIO-ETS-PART2-005-SYSTEM-EVENT-COLLECTIONS-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COLLECTIONS
			+ ": every advertised itemType=SystemEvent collection retrieves schema-valid SystemEvent resources (REQ-ETS-PART2-005, SCENARIO-ETS-PART2-005-SYSTEM-EVENT-COLLECTIONS-001)",
			groups = GROUP, alwaysRun = true)
	public void systemEventCollectionsValidateSystemEventSchema() {
		requireSystemEventDeclaration(REQ_COLLECTIONS);
		List<Map<String, Object>> collections = advertisedCollections("SystemEvent", REQ_COLLECTIONS);
		int supportedCollections = 0;
		for (Map<String, Object> collection : collections) {
			Optional<TraversalResult> evidence = Part1ApiCommonSupport.collectionItemsDetailed(this.apiRoot, collection,
					Part2SystemEventSupport.JSON_MEDIA);
			if (evidence.isEmpty()) {
				continue;
			}
			supportedCollections++;
			Part2SystemEventSupport.validateSystemEventEndpoint(collectionItemsUri(collection, REQ_COLLECTIONS),
					evidence.orElseThrow().pages(), REQ_COLLECTIONS);
		}
		if (supportedCollections == 0) {
			throw new SkipException(REQ_COLLECTIONS
					+ " - every advertised SystemEvent collection lacked a supported rel=items application/json link.");
		}
	}

	static boolean declaresConformance(Map<String, Object> body, String conformanceUri) {
		return Part2ApiCommonTests.declaresConformance(body, conformanceUri);
	}

	static String systemEventsCanonicalPath() {
		return "systemEvents";
	}

	static String releasedSystemScopedSystemEventsPath(String systemId) {
		return "systems/" + Part2SystemEventSupport.encodePathToken(systemId) + "/systemEvents";
	}

	static boolean isCollectionWithItemType(Map<String, Object> collection, String itemType) {
		return collection != null && itemType != null && itemType.equals(collection.get("itemType"));
	}

	static boolean hasItemsOnlyCollectionShape(Map<String, Object> body) {
		return body != null && body.get("items") instanceof List;
	}

	private void requireSystemEventDeclaration(String requirement) {
		Response response = given().accept("application/json")
			.when()
			.get(this.apiRoot.resolve("conformance"))
			.andReturn();
		ETSAssert.assertStatus(response, 200, requirement);
		Map<String, Object> parsed = Part2SystemEventSupport.parseObject(response, this.apiRoot.resolve("conformance"),
				requirement);
		ETSAssert.assertJsonObjectHas(parsed, "conformsTo", List.class, requirement);
		if (!declaresConformance(parsed, CONF_SYSTEM_EVENT)) {
			throw new SkipException(CONF_SYSTEM_EVENT
					+ " - IUT does not declare the CS API Part 2 System Events conformance class in /conformance; no SystemEvent mutation or streaming request was issued.");
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
		if ("systems".equals(resourceType) && traversal.items().isEmpty()) {
			throw new SkipException(requirement + " - canonical /systems collection is empty.");
		}
		return traversal;
	}

	private void validateSystemEventEndpoint(URI endpoint, String requirement) {
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint,
				Part2SystemEventSupport.JSON, Map.of(), requirement, Part2SystemEventSupport.JSON_MEDIA);
		if (evidence.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " returned HTTP 404.");
		}
		Part2SystemEventSupport.validateSystemEventEndpoint(endpoint, evidence.orElseThrow().pages(), requirement);
	}

	private List<Map<String, Object>> advertisedCollections(String itemType, String requirement) {
		Response response = given().accept("application/json")
			.when()
			.get(this.apiRoot.resolve("collections"))
			.andReturn();
		ETSAssert.assertStatus(response, 200, requirement);
		Map<String, Object> body = Part2SystemEventSupport.parseObject(response, this.apiRoot.resolve("collections"),
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
		return this.apiRoot.resolve("collections/" + Part2SystemEventSupport.encodePathToken(id) + "/items");
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
					"Part 2 System Events setup skipped before IUT access because prerequisite " + blocker + ".");
		}
	}

	private static String configurationBlocker(IResultMap results, String status) {
		if (results == null) {
			return null;
		}
		for (ITestResult result : results.getAllResults()) {
			if (result != null && result.getMethod() != null && isSystemEventPrerequisite(result)) {
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
			if (result != null && result.getMethod() != null && isSystemEventPrerequisite(result)) {
				return "method " + result.getMethod().getMethodName() + " " + status;
			}
		}
		return null;
	}

	private static boolean isSystemEventPrerequisite(ITestResult result) {
		for (String group : result.getMethod().getGroups()) {
			if ("part2apicommon".equals(group) || "systemfeatures".equals(group)) {
				return true;
			}
		}
		return Part2ApiCommonTests.class.equals(result.getMethod().getRealClass())
				|| SystemFeaturesTests.class.equals(result.getMethod().getRealClass());
	}

}
