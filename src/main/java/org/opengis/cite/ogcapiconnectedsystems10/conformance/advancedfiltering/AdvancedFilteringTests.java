package org.opengis.cite.ogcapiconnectedsystems10.conformance.advancedfiltering;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Part 1 - AdvancedFiltering conformance subset tests
 * ({@code /conf/advanced-filtering}; OGC 23-001 Annex A).
 *
 * <p>
 * Implements the Sprint 11 systems/common-resource read-only subset of
 * <strong>REQ-ETS-PART1-009</strong>. This class deliberately does not close the full
 * AdvancedFiltering requirement class: full cross-resource association filters, full
 * spatial semantics, combined-filter truth tables, endpoint parity, Part 2 query
 * behavior, and mutation-side classes remain open for future sprints.
 * </p>
 */
public class AdvancedFilteringTests {

	static final String CONF_ADVANCED_FILTERING = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/advanced-filtering";

	static final String REQ_ADVANCED_FILTERING_CLASS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering";

	static final String REQ_ID_LIST_SCHEMA = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/id-list-schema";

	static final String REQ_RESOURCE_BY_ID = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/resource-by-id";

	static final String REQ_RESOURCE_BY_KEYWORD = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/resource-by-keyword";

	static final String REQ_FEATURE_BY_GEOM = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/feature-by-geom";

	private URI iutUri;

	private String base;

	private Response conformanceResponse;

	private Map<String, Object> conformanceBody;

	private Map<String, Object> systemsSeedBody;

	private String selectedSystemId;

	private String selectedKeyword;

	/**
	 * Fetches /conformance and read-only seed data once for all AdvancedFiltering subset
	 * assertions.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchAdvancedFilteringInputs(ITestContext testContext) {
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		this.iutUri = (URI) iutAttr;
		String iutString = this.iutUri.toString();
		this.base = iutString.endsWith("/") ? iutString : iutString + "/";

		URI conformanceUri = URI.create(this.base + "conformance");
		this.conformanceResponse = given().accept("application/json").when().get(conformanceUri).andReturn();
		this.conformanceBody = parseBody(this.conformanceResponse);
		if (!declaresAdvancedFilteringConformance()) {
			throw new SkipException(CONF_ADVANCED_FILTERING
					+ " - IUT does not declare the CS API AdvancedFiltering conformance class in /conformance. "
					+ "Undeclared query-parameter behavior is not conformance PASS evidence.");
		}

		Response systemsSeedResponse = given().accept("application/json")
			.queryParam("limit", 1)
			.when()
			.get(URI.create(this.base + "systems"))
			.andReturn();
		ETSAssert.assertStatus(systemsSeedResponse, 200, REQ_ADVANCED_FILTERING_CLASS);
		this.systemsSeedBody = parseBody(systemsSeedResponse);
		this.selectedSystemId = systemId(firstItem(this.systemsSeedBody));
		this.selectedKeyword = keywordFromSystem(firstItem(this.systemsSeedBody));
	}

	/**
	 * SCENARIO-ETS-PART1-009-ADVFILTER-CONFORMANCE-DECLARED-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_ADVANCED_FILTERING_CLASS
			+ ": /conformance declares /conf/advanced-filtering (REQ-ETS-PART1-009, SCENARIO-ETS-PART1-009-ADVFILTER-CONFORMANCE-DECLARED-001)",
			groups = "advancedfiltering")
	@SuppressWarnings("unchecked")
	public void advancedFilteringConformanceDeclared() {
		ETSAssert.assertStatus(this.conformanceResponse, 200, REQ_ADVANCED_FILTERING_CLASS);
		if (this.conformanceBody == null) {
			ETSAssert.failWithUri(REQ_ADVANCED_FILTERING_CLASS,
					"/conformance body did not parse as JSON. Content-Type was: "
							+ this.conformanceResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.conformanceBody, "conformsTo", List.class, REQ_ADVANCED_FILTERING_CLASS);
		List<Object> conformsTo = (List<Object>) this.conformanceBody.get("conformsTo");
		Predicate<Object> isAdvancedFiltering = CONF_ADVANCED_FILTERING::equals;
		ETSAssert.assertJsonArrayContains(conformsTo, isAdvancedFiltering, CONF_ADVANCED_FILTERING,
				REQ_ADVANCED_FILTERING_CLASS);
	}

	/**
	 * SCENARIO-ETS-PART1-009-ADVFILTER-ID-LIST-SCHEMA-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_ID_LIST_SCHEMA
			+ ": local ID_List helper accepts homogeneous local-ID or UID lists and rejects empty, malformed, or mixed lists (REQ-ETS-PART1-009, SCENARIO-ETS-PART1-009-ADVFILTER-ID-LIST-SCHEMA-001)",
			groups = "advancedfiltering")
	public void advancedFilteringIdListSchema() {
		assertIdListValid("0mqcvdnfoca0");
		assertIdListValid("0mqcvdnfoca0,0ngu9lvstls0");
		assertIdListValid("urn:osh:sensor:simweather:0123456879");
		assertIdListValid("urn:osh:sensor:simweather:0123456879,urn:osh:sensor:simweather:9876543210");
		assertIdListValid("urn:osh:sensor:simweather:*");
		assertIdListInvalid("");
		assertIdListInvalid(",");
		assertIdListInvalid("0mqcvdnfoca0,urn:osh:sensor:simweather:0123456879");
		assertIdListInvalid("urn:osh:sensor:bad value");
	}

	/**
	 * SCENARIO-ETS-PART1-009-ADVFILTER-SYSTEM-ID-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RESOURCE_BY_ID
			+ ": /systems?id=<seed-id> returns non-empty results and every result preserves the seed id (REQ-ETS-PART1-009, SCENARIO-ETS-PART1-009-ADVFILTER-SYSTEM-ID-001)",
			groups = "advancedfiltering")
	public void systemsFilterById() {
		if (this.selectedSystemId == null || this.selectedSystemId.isBlank()) {
			throw new SkipException(
					REQ_RESOURCE_BY_ID + " - /systems seed response did not include a System with a usable id.");
		}
		Response filteredResponse = given().accept("application/json")
			.queryParam("id", this.selectedSystemId)
			.when()
			.get(URI.create(this.base + "systems"))
			.andReturn();
		ETSAssert.assertStatus(filteredResponse, 200, REQ_RESOURCE_BY_ID);
		Map<String, Object> body = parseBody(filteredResponse);
		if (body == null) {
			ETSAssert.failWithUri(REQ_RESOURCE_BY_ID, "/systems?id=" + this.selectedSystemId
					+ " body did not parse as JSON. Content-Type was: " + filteredResponse.getContentType());
		}
		List<Object> items = itemsArray(body, REQ_RESOURCE_BY_ID);
		if (items.isEmpty()) {
			ETSAssert.failWithUri(REQ_RESOURCE_BY_ID, "/systems?id=" + this.selectedSystemId
					+ " returned an empty items array after the suite selected that id from a non-empty /systems seed response.");
		}
		for (Object item : items) {
			String itemId = systemId(item);
			if (!this.selectedSystemId.equals(itemId)) {
				ETSAssert.failWithUri(REQ_RESOURCE_BY_ID,
						"/systems?id=" + this.selectedSystemId + " returned item with id '" + itemId
								+ "'. Only resources assigned the requested id belong in the result set.");
			}
		}
	}

	/**
	 * SCENARIO-ETS-PART1-009-ADVFILTER-SYSTEM-KEYWORD-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RESOURCE_BY_KEYWORD
			+ ": /systems?q=<seed-keyword> returns non-empty results with keyword evidence in name or description (REQ-ETS-PART1-009, SCENARIO-ETS-PART1-009-ADVFILTER-SYSTEM-KEYWORD-001)",
			groups = "advancedfiltering")
	public void systemsFilterByKeyword() {
		if (this.selectedKeyword == null || this.selectedKeyword.isBlank()) {
			throw new SkipException(REQ_RESOURCE_BY_KEYWORD
					+ " - /systems seed response did not include a System name or description with a usable keyword.");
		}
		Response filteredResponse = given().accept("application/json")
			.queryParam("q", this.selectedKeyword)
			.when()
			.get(URI.create(this.base + "systems"))
			.andReturn();
		ETSAssert.assertStatus(filteredResponse, 200, REQ_RESOURCE_BY_KEYWORD);
		Map<String, Object> body = parseBody(filteredResponse);
		if (body == null) {
			ETSAssert.failWithUri(REQ_RESOURCE_BY_KEYWORD, "/systems?q=" + this.selectedKeyword
					+ " body did not parse as JSON. Content-Type was: " + filteredResponse.getContentType());
		}
		List<Object> items = itemsArray(body, REQ_RESOURCE_BY_KEYWORD);
		if (items.isEmpty()) {
			ETSAssert.failWithUri(REQ_RESOURCE_BY_KEYWORD, "/systems?q=" + this.selectedKeyword
					+ " returned an empty items array after the suite selected that keyword from a non-empty /systems seed response.");
		}
		for (Object item : items) {
			if (!hasKeywordEvidence(item, this.selectedKeyword)) {
				ETSAssert.failWithUri(REQ_RESOURCE_BY_KEYWORD, "/systems?q=" + this.selectedKeyword
						+ " returned an item without the keyword in name or description: " + item);
			}
		}
	}

	/**
	 * SCENARIO-ETS-PART1-009-ADVFILTER-SYSTEM-GEOM-SMOKE-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_FEATURE_BY_GEOM
			+ ": /systems?geom=<broad-WKT-polygon> returns HTTP 200 JSON collection shape; this is smoke, not full spatial-intersection conformance (REQ-ETS-PART1-009, SCENARIO-ETS-PART1-009-ADVFILTER-SYSTEM-GEOM-SMOKE-001)",
			groups = "advancedfiltering")
	public void systemsFilterByGeomSmoke() {
		Response geomResponse = given().accept("application/json")
			.queryParam("geom", "POLYGON((-180 -90,180 -90,180 90,-180 90,-180 -90))")
			.when()
			.get(URI.create(this.base + "systems"))
			.andReturn();
		ETSAssert.assertStatus(geomResponse, 200, REQ_FEATURE_BY_GEOM);
		Map<String, Object> body = parseBody(geomResponse);
		if (body == null) {
			ETSAssert.failWithUri(REQ_FEATURE_BY_GEOM,
					"/systems?geom=<broad WKT polygon> body did not parse as JSON. Content-Type was: "
							+ geomResponse.getContentType());
		}
		if (!body.containsKey("items") && !body.containsKey("features")) {
			ETSAssert.failWithUri(REQ_FEATURE_BY_GEOM,
					"/systems?geom=<broad WKT polygon> returned JSON without CS API 'items' or GeoJSON 'features'. Keys: "
							+ body.keySet());
		}
	}

	/**
	 * SCENARIO-ETS-PART1-009-ADVFILTER-DEPENDENCY-SMOKE-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_ADVANCED_FILTERING_CLASS
			+ ": AdvancedFiltering group runtime-cascade tracer for AdvancedFiltering -> SystemFeatures -> Core (REQ-ETS-PART1-009, SCENARIO-ETS-PART1-009-ADVFILTER-DEPENDENCY-SMOKE-001)",
			groups = "advancedfiltering")
	public void advancedFilteringDependencyCascadeRuntime() {
		ETSAssert.assertJsonObjectHas(Map.of("dependencyChain", "advancedfiltering->systemfeatures->core"),
				"dependencyChain", String.class, REQ_ADVANCED_FILTERING_CLASS);
	}

	@SuppressWarnings("unchecked")
	private boolean declaresAdvancedFilteringConformance() {
		if (this.conformanceBody == null) {
			return false;
		}
		Object conformsToObj = this.conformanceBody.get("conformsTo");
		if (!(conformsToObj instanceof List)) {
			return false;
		}
		return ((List<Object>) conformsToObj).contains(CONF_ADVANCED_FILTERING);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> parseBody(Response response) {
		try {
			return response.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> firstItem(Map<String, Object> body) {
		if (body == null) {
			return null;
		}
		Object itemsObj = body.get("items");
		if (!(itemsObj instanceof List)) {
			return null;
		}
		List<?> items = (List<?>) itemsObj;
		if (items.isEmpty() || !(items.get(0) instanceof Map)) {
			return null;
		}
		return (Map<String, Object>) items.get(0);
	}

	@SuppressWarnings("unchecked")
	private List<Object> itemsArray(Map<String, Object> body, String requirementUri) {
		ETSAssert.assertJsonObjectHas(body, "items", List.class, requirementUri);
		return (List<Object>) body.get("items");
	}

	@SuppressWarnings("unchecked")
	private String systemId(Object item) {
		if (!(item instanceof Map)) {
			return null;
		}
		return asString(((Map<String, Object>) item).get("id"));
	}

	@SuppressWarnings("unchecked")
	private String keywordFromSystem(Map<String, Object> system) {
		if (system == null) {
			return null;
		}
		String text = firstNonBlank(asString(system.get("name")), asString(system.get("description")));
		Object properties = system.get("properties");
		if (text == null && properties instanceof Map) {
			Map<String, Object> props = (Map<String, Object>) properties;
			text = firstNonBlank(asString(props.get("name")), asString(props.get("description")));
		}
		return firstKeyword(text);
	}

	@SuppressWarnings("unchecked")
	private boolean hasKeywordEvidence(Object item, String keyword) {
		if (!(item instanceof Map) || keyword == null) {
			return false;
		}
		Map<String, Object> obj = (Map<String, Object>) item;
		String text = joinText(asString(obj.get("name")), asString(obj.get("description")));
		Object properties = obj.get("properties");
		if (properties instanceof Map) {
			Map<String, Object> props = (Map<String, Object>) properties;
			text = joinText(text, asString(props.get("name")), asString(props.get("description")));
		}
		return text.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
	}

	private boolean isValidIdList(String value) {
		if (value == null || value.isBlank()) {
			return false;
		}
		String[] tokens = value.split(",", -1);
		boolean seenUid = false;
		boolean seenLocalId = false;
		for (String token : tokens) {
			if (token.isBlank()) {
				return false;
			}
			boolean uid = isUidToken(token);
			if (!uid && token.contains(":")) {
				return false;
			}
			seenUid = seenUid || uid;
			seenLocalId = seenLocalId || !uid;
			if (seenUid && seenLocalId) {
				return false;
			}
		}
		return true;
	}

	private boolean isUidToken(String token) {
		if (token.endsWith("*")) {
			token = token.substring(0, token.length() - 1);
		}
		try {
			URI uri = URI.create(token);
			return uri.getScheme() != null && token.indexOf(' ') < 0;
		}
		catch (IllegalArgumentException ex) {
			return false;
		}
	}

	private void assertIdListValid(String value) {
		if (!isValidIdList(value)) {
			ETSAssert.failWithUri(REQ_ID_LIST_SCHEMA, "Expected ID_List value to be valid: '" + value + "'.");
		}
	}

	private void assertIdListInvalid(String value) {
		if (isValidIdList(value)) {
			ETSAssert.failWithUri(REQ_ID_LIST_SCHEMA, "Expected ID_List value to be invalid: '" + value + "'.");
		}
	}

	private String firstKeyword(String text) {
		if (text == null) {
			return null;
		}
		String[] words = text.split("[^A-Za-z0-9]+");
		for (String word : words) {
			if (word.length() >= 4) {
				return word;
			}
		}
		return null;
	}

	private String firstNonBlank(String... values) {
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		return null;
	}

	private String joinText(String... values) {
		StringBuilder builder = new StringBuilder();
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				if (builder.length() > 0) {
					builder.append(' ');
				}
				builder.append(value);
			}
		}
		return builder.toString();
	}

	private String asString(Object value) {
		return value instanceof String ? (String) value : null;
	}

}
