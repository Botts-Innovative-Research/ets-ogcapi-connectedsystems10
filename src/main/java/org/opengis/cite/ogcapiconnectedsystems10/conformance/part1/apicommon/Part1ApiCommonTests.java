package org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * Directly owned released OGC 23-001 `/conf/api-common` procedures.
 */
public class Part1ApiCommonTests {

	static final String GROUP = "part1apicommon";

	static final String REQ_RESOURCE_IDS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/api-common/resource-ids";

	static final String REQ_RESOURCE_UIDS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/api-common/resource-uids";

	static final String REC_RESOURCE_UID_TYPES = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/rec/api-common/resource-uids-types";

	static final String REQ_DATETIME = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/api-common/datetime";

	private static final List<String> RESOURCE_TYPES = List.of("systems", "deployments", "procedures",
			"samplingFeatures", "properties");

	private URI apiRoot;

	private final Map<String, List<Map<String, Object>>> resourcesByType = new LinkedHashMap<>();

	private Response collectionsResponse;

	private Map<String, Object> collectionsBody;

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-CANONICAL-RESOURCES-001.
	 * @param testContext current TestNG context.
	 */
	@BeforeClass(dependsOnGroups = { "core", "common" })
	public void fetchApiCommonResources(ITestContext testContext) {
		skipWhenPrerequisiteUnsatisfied(testContext);
		Object iut = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iut instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		String value = iut.toString();
		this.apiRoot = URI.create(value.endsWith("/") ? value : value + "/");
		for (String resourceType : RESOURCE_TYPES) {
			Optional<List<Map<String, Object>>> resources = Part1ApiCommonSupport.canonicalResources(this.apiRoot,
					resourceType);
			resources.ifPresent(items -> this.resourcesByType.put(resourceType, items));
		}
		if (this.resourcesByType.isEmpty()) {
			ETSAssert.failWithUri(REQ_RESOURCE_IDS,
					"IUT does not expose any supported Part 1 canonical resources endpoint.");
		}
		this.collectionsResponse = given().accept("application/json")
			.when()
			.get(this.apiRoot.resolve("collections"))
			.andReturn();
		this.collectionsBody = parseObject(this.collectionsResponse);
	}

	private static void skipWhenPrerequisiteUnsatisfied(ITestContext testContext) {
		String failure = unsatisfiedPrerequisite(testContext.getFailedTests(), "failed");
		if (failure == null) {
			failure = unsatisfiedPrerequisite(testContext.getSkippedTests(), "skipped");
		}
		if (failure != null) {
			throw new SkipException(
					"Part 1 API Common setup skipped before IUT access because prerequisite " + failure + ".");
		}
	}

	private static String unsatisfiedPrerequisite(IResultMap results, String status) {
		if (results == null) {
			return null;
		}
		for (ITestResult result : results.getAllResults()) {
			if (result == null || result.getMethod() == null) {
				continue;
			}
			for (String group : result.getMethod().getGroups()) {
				if ("core".equals(group) || "common".equals(group)) {
					return group + " method " + result.getMethod().getMethodName() + " " + status;
				}
			}
		}
		return null;
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-RESOURCE-IDS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RESOURCE_IDS
			+ ": resource IDs are unique within every supported Part 1 resource type (REQ-ETS-PART1-001, SCENARIO-ETS-PART1-001-RESOURCE-IDS-001)",
			groups = GROUP)
	public void resourceIdsAreUniqueWithinEachType() {
		assertResourceIds(this.resourcesByType);
	}

	static void assertResourceIds(Map<String, List<Map<String, Object>>> resourcesByType) {
		for (Map.Entry<String, List<Map<String, Object>>> entry : resourcesByType.entrySet()) {
			Set<String> ids = new LinkedHashSet<>();
			for (Map<String, Object> resource : entry.getValue()) {
				String id = requiredId(resource, entry.getKey(), REQ_RESOURCE_IDS);
				if (!ids.add(id)) {
					ETSAssert.failWithUri(REQ_RESOURCE_IDS,
							"duplicate local ID '" + id + "' in canonical resource type " + entry.getKey() + ".");
				}
			}
		}
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-RESOURCE-UIDS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RESOURCE_UIDS
			+ ": resource UIDs are valid absolute URIs and unique across all Part 1 resource types (REQ-ETS-PART1-001, SCENARIO-ETS-PART1-001-RESOURCE-UIDS-001)",
			groups = GROUP)
	public void resourceUidsAreValidAndGloballyUnique() {
		assertResourceUids(this.resourcesByType);
	}

	static void assertResourceUids(Map<String, List<Map<String, Object>>> resourcesByType) {
		Map<String, String> uidOwners = new LinkedHashMap<>();
		for (Map.Entry<String, List<Map<String, Object>>> entry : resourcesByType.entrySet()) {
			for (Map<String, Object> resource : entry.getValue()) {
				String id = requiredId(resource, entry.getKey(), REQ_RESOURCE_UIDS);
				String uid = Part1ApiCommonSupport.resourceUid(resource)
					.orElseGet(() -> failMissingUid(entry.getKey(), id));
				if (!Part1ApiCommonSupport.isValidAbsoluteUri(uid)) {
					ETSAssert.failWithUri(REQ_RESOURCE_UIDS,
							entry.getKey() + "/" + id + " has a UID that is not a valid absolute URI: " + uid + ".");
				}
				String owner = entry.getKey() + "/" + id;
				String prior = uidOwners.putIfAbsent(uid, owner);
				if (prior != null) {
					ETSAssert.failWithUri(REQ_RESOURCE_UIDS,
							"UID '" + uid + "' is used by both " + prior + " and " + owner + ".");
				}
			}
		}
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-RESOURCE-UID-TYPES-001.
	 */
	@Test(description = "OGC-23-001 " + REC_RESOURCE_UID_TYPES
			+ ": resource UID forms are UUID URNs or URNs in the bundled IANA registered-namespace snapshot, with other forms reported as warnings (REQ-ETS-PART1-001, SCENARIO-ETS-PART1-001-RESOURCE-UID-TYPES-001)",
			dependsOnMethods = "resourceUidsAreValidAndGloballyUnique", groups = GROUP)
	public void resourceUidTypesFollowRecommendation() {
		for (Map.Entry<String, List<Map<String, Object>>> entry : this.resourcesByType.entrySet()) {
			for (Map<String, Object> resource : entry.getValue()) {
				String id = requiredId(resource, entry.getKey(), REC_RESOURCE_UID_TYPES);
				String uid = Part1ApiCommonSupport.resourceUid(resource).orElse("");
				if (!Part1ApiCommonSupport.isRecommendedUid(uid)) {
					Reporter.log("[WARNING] " + REC_RESOURCE_UID_TYPES + " - " + entry.getKey() + "/" + id + " UID '"
							+ uid
							+ "' is valid but is not a UUID URN or a URN in the bundled IANA registered-namespace snapshot.",
							true);
				}
			}
		}
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-DATETIME-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_DATETIME
			+ ": datetime filtering uses validTime intersection and retains every timeless feature (REQ-ETS-PART1-001, SCENARIO-ETS-PART1-001-DATETIME-001)",
			groups = GROUP)
	public void datetimeUsesValidTime() {
		ETSAssert.assertStatus(this.collectionsResponse, 200, REQ_DATETIME);
		if (this.collectionsBody == null) {
			ETSAssert.failWithUri(REQ_DATETIME, "/collections response body is not a JSON object.");
		}
		Object advertised = this.collectionsBody.get("collections");
		if (!(advertised instanceof List)) {
			ETSAssert.failWithUri(REQ_DATETIME, "/collections response is missing a collections array.");
		}
		int executedQueries = 0;
		for (Object value : (List<?>) advertised) {
			if (!(value instanceof Map)) {
				ETSAssert.failWithUri(REQ_DATETIME, "/collections array contains a non-object value.");
			}
			@SuppressWarnings("unchecked")
			Map<String, Object> collection = (Map<String, Object>) value;
			List<Part1ApiCommonSupport.DatetimeQuery> queries;
			try {
				queries = Part1ApiCommonSupport.datetimeQueries(collection);
			}
			catch (IllegalArgumentException ex) {
				ETSAssert.failWithUri(REQ_DATETIME,
						"collection " + collection.get("id") + " has an invalid temporal extent: " + ex.getMessage());
				return;
			}
			if (queries.isEmpty()) {
				continue;
			}
			Optional<List<Map<String, Object>>> unfiltered = Part1ApiCommonSupport.collectionItems(this.apiRoot,
					collection);
			if (unfiltered.isEmpty()) {
				continue;
			}
			for (Part1ApiCommonSupport.DatetimeQuery query : queries) {
				Instant requestTime = Instant.now();
				Optional<List<Map<String, Object>>> filtered = Part1ApiCommonSupport.collectionItems(this.apiRoot,
						collection, Map.of("datetime", query.parameter()), Part1ApiCommonSupport::get);
				if (filtered.isEmpty()) {
					continue;
				}
				executedQueries++;
				assertFilteredItems(collection, query, requestTime, unfiltered.orElseThrow(), filtered.orElseThrow());
			}
		}
		if (executedQueries == 0) {
			throw new SkipException(REQ_DATETIME
					+ " - no advertised collection exposes both a usable temporal extent and a supported JSON items media type; no positive datetime-filter evidence is available.");
		}
	}

	static void assertFilteredItems(Map<String, Object> collection, Part1ApiCommonSupport.DatetimeQuery query,
			Instant requestTime, List<Map<String, Object>> unfiltered, List<Map<String, Object>> filtered) {
		String collectionId = String.valueOf(collection.get("id"));
		Set<String> filteredIds = new LinkedHashSet<>();
		for (Map<String, Object> feature : filtered) {
			String id = requiredId(feature, "collection " + collectionId, REQ_DATETIME);
			filteredIds.add(id);
			try {
				if (!Part1ApiCommonSupport.validTimeIntersects(feature, query, requestTime)) {
					ETSAssert.failWithUri(REQ_DATETIME, "collection " + collectionId + " returned feature " + id
							+ " whose validTime does not intersect datetime=" + query.parameter() + ".");
				}
			}
			catch (IllegalArgumentException ex) {
				ETSAssert.failWithUri(REQ_DATETIME,
						"collection " + collectionId + " feature " + id + " has invalid validTime: " + ex.getMessage());
			}
		}
		for (Map<String, Object> feature : unfiltered) {
			if (!Part1ApiCommonSupport.isTimeless(feature)) {
				continue;
			}
			String id = requiredId(feature, "collection " + collectionId, REQ_DATETIME);
			if (!filteredIds.contains(id)) {
				ETSAssert.failWithUri(REQ_DATETIME, "collection " + collectionId + " omitted timeless feature " + id
						+ " from datetime=" + query.parameter() + " result.");
			}
		}
	}

	private static String requiredId(Map<String, Object> resource, String source, String requirement) {
		Object id = resource.get("id");
		if (!(id instanceof String) || ((String) id).isBlank()) {
			ETSAssert.failWithUri(requirement, source + " contains a resource without a non-empty string id.");
		}
		return (String) id;
	}

	private static String failMissingUid(String type, String id) {
		ETSAssert.failWithUri(REQ_RESOURCE_UIDS, type + "/" + id + " does not expose a non-empty UID.");
		return "";
	}

	private static Map<String, Object> parseObject(Response response) {
		try {
			return response.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			return null;
		}
	}

}
