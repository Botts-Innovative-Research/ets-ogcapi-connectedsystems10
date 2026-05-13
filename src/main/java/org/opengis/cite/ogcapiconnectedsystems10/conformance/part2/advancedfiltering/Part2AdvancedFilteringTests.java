package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.advancedfiltering;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.apicommon.Part2ApiCommonTests;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Part 2 - Advanced Filtering read-only conformance subset
 * ({@code /conf/advanced-filtering}; OGC 23-002 Annex A.6).
 */
public class Part2AdvancedFilteringTests {

	static final String GROUP = "part2advancedfiltering";

	static final String CONF_ADVANCED_FILTERING = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/advanced-filtering";

	static final String CONF_PART2_API_COMMON = Part2ApiCommonTests.CONF_PART2_API_COMMON;

	static final String CONF_PART1_ADVANCED_FILTERING = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/advanced-filtering";

	static final String CONF_DATASTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/datastream";

	static final String CONF_CONTROLSTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/controlstream";

	static final String CONF_SYSTEM_EVENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/system-event";

	static final String REQ_ADVANCED_FILTERING = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/advanced-filtering";

	static final String REQ_DATASTREAM_PHENOMENON_TIME = REQ_ADVANCED_FILTERING + "/datastream-by-phenomenontime";

	static final String REQ_DATASTREAM_RESULT_TIME = REQ_ADVANCED_FILTERING + "/datastream-by-resulttime";

	static final String REQ_DATASTREAM_OBS_PROP = REQ_ADVANCED_FILTERING + "/datastream-by-obsprop";

	static final String REQ_OBS_PHENOMENON_TIME = REQ_ADVANCED_FILTERING + "/obs-by-phenomenontime";

	static final String REQ_OBS_RESULT_TIME = REQ_ADVANCED_FILTERING + "/obs-by-resulttime";

	static final String REQ_CONTROLSTREAM_ISSUE_TIME = REQ_ADVANCED_FILTERING + "/controlstream-by-issuetime";

	static final String REQ_CONTROLSTREAM_EXEC_TIME = REQ_ADVANCED_FILTERING + "/controlstream-by-exectime";

	static final String REQ_CONTROLSTREAM_CONTROL_PROP = REQ_ADVANCED_FILTERING + "/controlstream-by-controlprop";

	static final String REQ_COMMAND_ISSUE_TIME = REQ_ADVANCED_FILTERING + "/cmd-by-issuetime";

	static final String REQ_COMMAND_EXEC_TIME = REQ_ADVANCED_FILTERING + "/cmd-by-exectime";

	static final String REQ_COMMAND_STATUS = REQ_ADVANCED_FILTERING + "/cmd-by-status";

	static final String REQ_COMMAND_SENDER = REQ_ADVANCED_FILTERING + "/cmd-by-sender";

	static final String REQ_SYSTEM_EVENT_TYPE = REQ_ADVANCED_FILTERING + "/event-by-type";

	private URI iutUri;

	private URI baseUri;

	private Response conformanceResponse;

	private Map<String, Object> conformanceBody;

	/**
	 * Fetches shared read-only inputs once. Filter probes are delayed until each test has
	 * confirmed exact {@code /conf/advanced-filtering} declaration.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchPart2AdvancedFilteringInputs(ITestContext testContext) {
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		this.iutUri = (URI) iutAttr;
		String iutString = this.iutUri.toString();
		this.baseUri = URI.create(iutString.endsWith("/") ? iutString : iutString + "/");

		this.conformanceResponse = given().accept("application/json")
			.when()
			.get(this.baseUri.resolve("conformance"))
			.andReturn();
		this.conformanceBody = parseBody(this.conformanceResponse);
	}

	/**
	 * SCENARIO-ETS-PART2-006-ADVFILTER-CONFORMANCE-DECLARED-001.
	 * SCENARIO-ETS-PART2-006-UNDECLARED-FILTER-HONESTY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_ADVANCED_FILTERING
			+ ": /conformance declares /conf/advanced-filtering before Part 2 Advanced Filtering assertions run (REQ-ETS-PART2-006, SCENARIO-ETS-PART2-006-ADVFILTER-CONFORMANCE-DECLARED-001, SCENARIO-ETS-PART2-006-UNDECLARED-FILTER-HONESTY-001)",
			groups = GROUP)
	public void part2AdvancedFilteringConformanceDeclared() {
		ETSAssert.assertStatus(this.conformanceResponse, 200, REQ_ADVANCED_FILTERING);
		if (this.conformanceBody == null) {
			ETSAssert.failWithUri(REQ_ADVANCED_FILTERING, "/conformance body did not parse as JSON. Content-Type was: "
					+ this.conformanceResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.conformanceBody, "conformsTo", List.class, REQ_ADVANCED_FILTERING);
		if (!declaresConformance(this.conformanceBody, CONF_ADVANCED_FILTERING)) {
			throw new SkipException(CONF_ADVANCED_FILTERING
					+ " - IUT does not declare the CS API Part 2 Advanced Filtering conformance class. "
					+ "Undeclared filter query behavior is readiness evidence only, not conformance PASS evidence.");
		}
	}

	/**
	 * SCENARIO-ETS-PART2-006-DEPENDENCY-SKIP-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_ADVANCED_FILTERING
			+ ": full /conf/advanced-filtering closure is prerequisite-incomplete when /conf/api-common or Part 1 /conf/advanced-filtering is absent (REQ-ETS-PART2-006, SCENARIO-ETS-PART2-006-DEPENDENCY-SKIP-001)",
			groups = GROUP)
	public void advancedFilteringPrerequisitesVisibleForFullClosure() {
		skipIfAdvancedFilteringUndeclared();
		if (!declaresConformance(this.conformanceBody, CONF_PART2_API_COMMON)) {
			throw new SkipException(CONF_PART2_API_COMMON
					+ " - /req/advanced-filtering lists /req/api-common as a prerequisite. Filter-specific checks may run, but full /conf/advanced-filtering closure is prerequisite-incomplete.");
		}
		if (!declaresConformance(this.conformanceBody, CONF_PART1_ADVANCED_FILTERING)) {
			throw new SkipException(CONF_PART1_ADVANCED_FILTERING
					+ " - Part 2 /req/advanced-filtering lists Part 1 /req/advanced-filtering as a prerequisite. Full /conf/advanced-filtering closure is prerequisite-incomplete.");
		}
		Reporter.log("IUT declares Part 2 /conf/advanced-filtering and its visible prerequisite classes.", true);
	}

	/**
	 * SCENARIO-ETS-PART2-006-DATASTREAM-FILTERS-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_DATASTREAM_PHENOMENON_TIME + " and " + REQ_DATASTREAM_RESULT_TIME
			+ ": DataStream time filters are read-only and returned resources intersect the requested time value (REQ-ETS-PART2-006, SCENARIO-ETS-PART2-006-DATASTREAM-FILTERS-READONLY-001)",
			groups = GROUP)
	public void datastreamTimeFiltersVerifyReturnedPredicates() {
		skipIfAdvancedFilteringUndeclared();
		skipIfClassUndeclared(CONF_DATASTREAM, "Datastream time filters require the Part 2 Datastream class.");
		List<?> seeds = fetchItems("datastreams", REQ_DATASTREAM_PHENOMENON_TIME);
		assertTimeFilterFromSeed("datastreams", "phenomenonTime", seeds, REQ_DATASTREAM_PHENOMENON_TIME);
		assertTimeFilterFromSeed("datastreams", "resultTime", seeds, REQ_DATASTREAM_RESULT_TIME);
	}

	/**
	 * SCENARIO-ETS-PART2-006-DATASTREAM-FILTERS-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_DATASTREAM_OBS_PROP
			+ ": DataStream observedProperty filter returns only DataStreams with matching observed property evidence (REQ-ETS-PART2-006, SCENARIO-ETS-PART2-006-DATASTREAM-FILTERS-READONLY-001)",
			groups = GROUP)
	public void datastreamObservedPropertyFilterVerifiesReturnedPredicates() {
		skipIfAdvancedFilteringUndeclared();
		skipIfClassUndeclared(CONF_DATASTREAM,
				"DataStream observedProperty filter requires the Part 2 Datastream class.");
		List<?> seeds = fetchItems("datastreams", REQ_DATASTREAM_OBS_PROP);
		String propertyDefinition = firstPropertyDefinition(seeds, "observedProperties");
		if (propertyDefinition == null) {
			throw new SkipException(REQ_DATASTREAM_OBS_PROP
					+ " - /datastreams seed page did not expose an observedProperties[].definition value usable for a read-only filter probe.");
		}
		Response response = filteredGet("datastreams", "observedProperty", propertyDefinition);
		List<?> filtered = nonEmptyFilteredItems(response, REQ_DATASTREAM_OBS_PROP,
				"/datastreams?observedProperty=" + propertyDefinition);
		for (Object item : filtered) {
			if (!hasPropertyDefinition(item, "observedProperties", propertyDefinition)) {
				ETSAssert.failWithUri(REQ_DATASTREAM_OBS_PROP, "/datastreams?observedProperty=" + propertyDefinition
						+ " returned a DataStream without that observedProperties definition: " + item);
			}
		}
	}

	/**
	 * SCENARIO-ETS-PART2-006-OBSERVATION-FILTERS-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_OBS_PHENOMENON_TIME + " and " + REQ_OBS_RESULT_TIME
			+ ": Observation time filters are read-only and returned Observations intersect the requested time value (REQ-ETS-PART2-006, SCENARIO-ETS-PART2-006-OBSERVATION-FILTERS-READONLY-001)",
			groups = GROUP)
	public void observationTimeFiltersVerifyReturnedPredicates() {
		skipIfAdvancedFilteringUndeclared();
		skipIfClassUndeclared(CONF_DATASTREAM,
				"Observation filters require the Part 2 Datastreams and Observations class.");
		List<?> seeds = fetchItems("observations", REQ_OBS_RESULT_TIME);
		assertObservationPhenomenonTimeFilterFromSeed(seeds);
		assertTimeFilterFromSeed("observations", "resultTime", seeds, REQ_OBS_RESULT_TIME);
	}

	/**
	 * SCENARIO-ETS-PART2-006-CONTROLSTREAM-FILTERS-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CONTROLSTREAM_ISSUE_TIME + " and " + REQ_CONTROLSTREAM_EXEC_TIME
			+ ": ControlStream time filters are read-only and returned resources intersect the requested time value (REQ-ETS-PART2-006, SCENARIO-ETS-PART2-006-CONTROLSTREAM-FILTERS-READONLY-001)",
			groups = GROUP)
	public void controlStreamTimeFiltersVerifyReturnedPredicates() {
		skipIfAdvancedFilteringUndeclared();
		skipIfClassUndeclared(CONF_CONTROLSTREAM, "ControlStream time filters require the Part 2 ControlStream class.");
		List<?> seeds = fetchItems("controlstreams", REQ_CONTROLSTREAM_ISSUE_TIME);
		assertTimeFilterFromSeed("controlstreams", "issueTime", seeds, REQ_CONTROLSTREAM_ISSUE_TIME);
		assertTimeFilterFromSeed("controlstreams", "executionTime", seeds, REQ_CONTROLSTREAM_EXEC_TIME);
	}

	/**
	 * SCENARIO-ETS-PART2-006-CONTROLSTREAM-FILTERS-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CONTROLSTREAM_CONTROL_PROP
			+ ": ControlStream controlledProperty filter returns only ControlStreams with matching controlled property evidence (REQ-ETS-PART2-006, SCENARIO-ETS-PART2-006-CONTROLSTREAM-FILTERS-READONLY-001)",
			groups = GROUP)
	public void controlStreamControlledPropertyFilterVerifiesReturnedPredicates() {
		skipIfAdvancedFilteringUndeclared();
		skipIfClassUndeclared(CONF_CONTROLSTREAM,
				"ControlStream controlledProperty filter requires the Part 2 ControlStream class.");
		List<?> seeds = fetchItems("controlstreams", REQ_CONTROLSTREAM_CONTROL_PROP);
		String propertyDefinition = firstPropertyDefinition(seeds, "controlledProperties");
		if (propertyDefinition == null) {
			throw new SkipException(REQ_CONTROLSTREAM_CONTROL_PROP
					+ " - /controlstreams seed page did not expose a controlledProperties[].definition value usable for a read-only filter probe.");
		}
		Response response = filteredGet("controlstreams", "controlledProperty", propertyDefinition);
		List<?> filtered = nonEmptyFilteredItems(response, REQ_CONTROLSTREAM_CONTROL_PROP,
				"/controlstreams?controlledProperty=" + propertyDefinition);
		for (Object item : filtered) {
			if (!hasPropertyDefinition(item, "controlledProperties", propertyDefinition)) {
				ETSAssert.failWithUri(REQ_CONTROLSTREAM_CONTROL_PROP,
						"/controlstreams?controlledProperty=" + propertyDefinition
								+ " returned a ControlStream without that controlledProperties definition: " + item);
			}
		}
	}

	/**
	 * SCENARIO-ETS-PART2-006-COMMAND-FILTERS-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COMMAND_ISSUE_TIME + ", " + REQ_COMMAND_EXEC_TIME + ", "
			+ REQ_COMMAND_STATUS + ", and " + REQ_COMMAND_SENDER
			+ ": Command filters are read-only and checked only when /commands is available with seed evidence (REQ-ETS-PART2-006, SCENARIO-ETS-PART2-006-COMMAND-FILTERS-READONLY-001)",
			groups = GROUP)
	public void commandFiltersVerifyReturnedPredicatesWhenEndpointAvailable() {
		skipIfAdvancedFilteringUndeclared();
		skipIfClassUndeclared(CONF_CONTROLSTREAM, "Command filters require the Part 2 ControlStream class.");
		Response seedResponse = given().accept("application/json")
			.queryParam("limit", 10)
			.when()
			.get(this.baseUri.resolve("commands"))
			.andReturn();
		if (seedResponse.getStatusCode() != 200) {
			throw new SkipException(REQ_COMMAND_ISSUE_TIME + " - /commands returned HTTP "
					+ seedResponse.getStatusCode() + "; unavailable Command endpoints are not filter PASS evidence.");
		}
		List<?> seeds = items(assertItemsCollection(seedResponse, REQ_COMMAND_ISSUE_TIME, "/commands"));
		if (seeds.isEmpty()) {
			throw new SkipException(REQ_COMMAND_ISSUE_TIME
					+ " - /commands returned an empty collection; no Command seed is available for predicate-filter checks.");
		}
		boolean exercised = false;
		exercised |= assertOptionalTimeFilterFromSeed("commands", "issueTime", seeds, REQ_COMMAND_ISSUE_TIME);
		exercised |= assertOptionalTimeFilterFromSeed("commands", "executionTime", seeds, REQ_COMMAND_EXEC_TIME);
		exercised |= assertOptionalValueFilterFromSeed("commands", "statusCode",
				Part2AdvancedFilteringTests::commandStatus, seeds, REQ_COMMAND_STATUS);
		exercised |= assertOptionalValueFilterFromSeed("commands", "sender", Part2AdvancedFilteringTests::sender, seeds,
				REQ_COMMAND_SENDER);
		if (!exercised) {
			throw new SkipException(REQ_COMMAND_ISSUE_TIME
					+ " - /commands seed page did not expose issueTime, executionTime, currentStatus, or sender values usable for read-only Command filter probes.");
		}
	}

	/**
	 * SCENARIO-ETS-PART2-006-SYSTEM-EVENT-FILTER-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_SYSTEM_EVENT_TYPE
			+ ": SystemEvent eventType filter is checked only when JSON SystemEvent endpoints are available with event type evidence (REQ-ETS-PART2-006, SCENARIO-ETS-PART2-006-SYSTEM-EVENT-FILTER-READONLY-001)",
			groups = GROUP)
	public void systemEventTypeFilterVerifiesReturnedPredicatesWhenEndpointAvailable() {
		skipIfAdvancedFilteringUndeclared();
		skipIfClassUndeclared(CONF_SYSTEM_EVENT, "SystemEvent filters require the Part 2 System Events class.");
		Response seedResponse = given().accept("application/json")
			.queryParam("limit", 10)
			.when()
			.get(this.baseUri.resolve(systemEventsCanonicalPath()))
			.andReturn();
		if (seedResponse.getStatusCode() != 200) {
			throw new SkipException(REQ_SYSTEM_EVENT_TYPE + " - /" + systemEventsCanonicalPath() + " returned HTTP "
					+ seedResponse.getStatusCode()
					+ "; unavailable or streaming-only SystemEvent endpoints are not eventType filter PASS evidence.");
		}
		List<?> seeds = items(
				assertItemsCollection(seedResponse, REQ_SYSTEM_EVENT_TYPE, "/" + systemEventsCanonicalPath()));
		String type = firstMappedValue(seeds, Part2AdvancedFilteringTests::systemEventType);
		if (type == null) {
			throw new SkipException(REQ_SYSTEM_EVENT_TYPE
					+ " - SystemEvent seed page did not expose eventType or type evidence usable for a read-only filter probe.");
		}
		Response response = filteredGet(systemEventsCanonicalPath(), "eventType", type);
		List<?> filtered = nonEmptyFilteredItems(response, REQ_SYSTEM_EVENT_TYPE,
				"/" + systemEventsCanonicalPath() + "?eventType=" + type);
		for (Object item : filtered) {
			if (!type.equals(systemEventType(item))) {
				ETSAssert.failWithUri(REQ_SYSTEM_EVENT_TYPE, "/" + systemEventsCanonicalPath() + "?eventType=" + type
						+ " returned a SystemEvent without matching event type evidence: " + item);
			}
		}
	}

	static boolean declaresConformance(Map<String, Object> body, String conformanceUri) {
		return Part2ApiCommonTests.declaresConformance(body, conformanceUri);
	}

	static String systemEventsCanonicalPath() {
		return "systemEvents";
	}

	static boolean hasItemsOnlyCollectionShape(Map<String, Object> body) {
		return body != null && body.get("items") instanceof List;
	}

	static boolean hasPropertyDefinition(Object item, String arrayName, String expectedDefinition) {
		if (!(item instanceof Map) || expectedDefinition == null || expectedDefinition.isBlank()) {
			return false;
		}
		Object properties = ((Map<?, ?>) item).get(arrayName);
		if (!(properties instanceof List)) {
			return false;
		}
		for (Object property : (List<?>) properties) {
			if (property instanceof Map
					&& expectedDefinition.equals(stringValue(((Map<?, ?>) property).get("definition")))) {
				return true;
			}
		}
		return false;
	}

	static boolean timeIntersects(String candidate, String requested) {
		if (candidate == null || requested == null || candidate.isBlank() || requested.isBlank()) {
			return false;
		}
		TimeRange candidateRange = TimeRange.parse(candidate);
		TimeRange requestedRange = TimeRange.parse(requested);
		if (candidateRange == null || requestedRange == null) {
			return false;
		}
		return !candidateRange.end.isBefore(requestedRange.start) && !requestedRange.end.isBefore(candidateRange.start);
	}

	static String commandStatus(Object command) {
		if (!(command instanceof Map)) {
			return null;
		}
		Object status = ((Map<?, ?>) command).get("currentStatus");
		if (status instanceof String) {
			return (String) status;
		}
		if (status instanceof Map) {
			return stringValue(((Map<?, ?>) status).get("statusCode"));
		}
		return null;
	}

	static String sender(Object command) {
		return command instanceof Map ? stringValue(((Map<?, ?>) command).get("sender")) : null;
	}

	static String systemEventType(Object event) {
		if (!(event instanceof Map)) {
			return null;
		}
		Map<?, ?> map = (Map<?, ?>) event;
		String eventType = stringValue(map.get("eventType"));
		return eventType != null ? eventType : stringValue(map.get("type"));
	}

	private void skipIfAdvancedFilteringUndeclared() {
		if (!declaresConformance(this.conformanceBody, CONF_ADVANCED_FILTERING)) {
			throw new SkipException(CONF_ADVANCED_FILTERING
					+ " - IUT does not declare the CS API Part 2 Advanced Filtering conformance class in /conformance.");
		}
	}

	private void skipIfClassUndeclared(String conformanceUri, String reason) {
		if (!declaresConformance(this.conformanceBody, conformanceUri)) {
			throw new SkipException(conformanceUri + " - " + reason);
		}
	}

	private List<?> fetchItems(String path, String reqUri) {
		Response response = given().accept("application/json")
			.queryParam("limit", 10)
			.when()
			.get(this.baseUri.resolve(path))
			.andReturn();
		return items(assertItemsCollection(response, reqUri, "/" + path));
	}

	private void assertTimeFilterFromSeed(String path, String propertyName, List<?> seeds, String reqUri) {
		if (!assertOptionalTimeFilterFromSeed(path, propertyName, seeds, reqUri)) {
			throw new SkipException(reqUri + " - /" + path + " seed page did not expose " + propertyName
					+ " values usable for a read-only filter probe.");
		}
	}

	private boolean assertOptionalTimeFilterFromSeed(String path, String propertyName, List<?> seeds, String reqUri) {
		String requestedTime = firstTimeValue(seeds, propertyName);
		if (requestedTime == null) {
			return false;
		}
		Response response = filteredGet(path, propertyName, requestedTime);
		List<?> filtered = nonEmptyFilteredItems(response, reqUri,
				"/" + path + "?" + propertyName + "=" + requestedTime);
		for (Object item : filtered) {
			String actualTime = timeValue(item, propertyName);
			if (!timeIntersects(actualTime, requestedTime)) {
				ETSAssert.failWithUri(reqUri,
						"/" + path + "?" + propertyName + "=" + requestedTime + " returned an item whose "
								+ propertyName + " does not intersect the requested time: " + item);
			}
		}
		return true;
	}

	private void assertObservationPhenomenonTimeFilterFromSeed(List<?> seeds) {
		String requestedTime = firstObservationPhenomenonTime(seeds);
		if (requestedTime == null) {
			throw new SkipException(REQ_OBS_PHENOMENON_TIME
					+ " - /observations seed page did not expose phenomenonTime values usable for a read-only filter probe.");
		}
		Response response = filteredGet("observations", "phenomenonTime", requestedTime);
		List<?> filtered = nonEmptyFilteredItems(response, REQ_OBS_PHENOMENON_TIME,
				"/observations?phenomenonTime=" + requestedTime);
		for (Object item : filtered) {
			String actualTime = observationPhenomenonTime(item);
			if (!timeIntersects(actualTime, requestedTime)) {
				ETSAssert.failWithUri(REQ_OBS_PHENOMENON_TIME, "/observations?phenomenonTime=" + requestedTime
						+ " returned an Observation whose phenomenonTime does not intersect the requested time: "
						+ item);
			}
		}
	}

	private boolean assertOptionalValueFilterFromSeed(String path, String queryParam, ValueExtractor extractor,
			List<?> seeds, String reqUri) {
		String requested = firstMappedValue(seeds, extractor);
		if (requested == null) {
			return false;
		}
		Response response = filteredGet(path, queryParam, requested);
		List<?> filtered = nonEmptyFilteredItems(response, reqUri, "/" + path + "?" + queryParam + "=" + requested);
		for (Object item : filtered) {
			if (!requested.equals(extractor.value(item))) {
				ETSAssert.failWithUri(reqUri, "/" + path + "?" + queryParam + "=" + requested
						+ " returned an item without matching predicate evidence: " + item);
			}
		}
		return true;
	}

	private Response filteredGet(String path, String queryParam, String value) {
		return given().accept("application/json")
			.queryParam(queryParam, value)
			.queryParam("limit", 2)
			.when()
			.get(this.baseUri.resolve(path))
			.andReturn();
	}

	private static List<?> nonEmptyFilteredItems(Response response, String reqUri, String source) {
		List<?> filtered = items(assertItemsCollection(response, reqUri, source));
		if (filtered.isEmpty()) {
			throw new SkipException(reqUri + " - " + source
					+ " returned an empty items[] array for a seed-derived filter value; empty collections are not predicate PASS evidence.");
		}
		return filtered;
	}

	private static String firstTimeValue(List<?> items, String propertyName) {
		for (Object item : items) {
			String value = timeValue(item, propertyName);
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		return null;
	}

	private static String firstObservationPhenomenonTime(List<?> items) {
		for (Object item : items) {
			String value = observationPhenomenonTime(item);
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		return null;
	}

	private static String firstPropertyDefinition(List<?> items, String arrayName) {
		for (Object item : items) {
			if (!(item instanceof Map)) {
				continue;
			}
			Object properties = ((Map<?, ?>) item).get(arrayName);
			if (!(properties instanceof List)) {
				continue;
			}
			for (Object property : (List<?>) properties) {
				if (property instanceof Map) {
					String definition = stringValue(((Map<?, ?>) property).get("definition"));
					if (definition != null && !definition.isBlank()) {
						return definition;
					}
				}
			}
		}
		return null;
	}

	private static String firstMappedValue(List<?> items, ValueExtractor extractor) {
		for (Object item : items) {
			String value = extractor.value(item);
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		return null;
	}

	private static String timeValue(Object item, String propertyName) {
		return item instanceof Map ? stringValue(((Map<?, ?>) item).get(propertyName)) : null;
	}

	static String observationPhenomenonTime(Object item) {
		if (!(item instanceof Map)) {
			return null;
		}
		Map<?, ?> map = (Map<?, ?>) item;
		return stringValue(map.get("phenomenonTime"));
	}

	private static Map<String, Object> assertItemsCollection(Response response, String reqUri, String source) {
		ETSAssert.assertStatus(response, 200, reqUri);
		Map<String, Object> body = parseBody(response);
		if (body == null) {
			ETSAssert.failWithUri(reqUri,
					source + " body did not parse as JSON. Content-Type was: " + response.getContentType());
		}
		if (!hasItemsOnlyCollectionShape(body)) {
			ETSAssert.failWithUri(reqUri, source + " did not expose a JSON object with an items[] array.");
		}
		return body;
	}

	private static List<?> items(Map<String, Object> body) {
		if (body == null || !(body.get("items") instanceof List)) {
			return List.of();
		}
		return (List<?>) body.get("items");
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> parseBody(Response response) {
		if (response == null || response.getBody() == null) {
			return null;
		}
		try {
			return response.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			return null;
		}
	}

	private static String stringValue(Object value) {
		return value instanceof String ? (String) value : null;
	}

	private interface ValueExtractor {

		String value(Object item);

	}

	private static final class TimeRange {

		private final Instant start;

		private final Instant end;

		private TimeRange(Instant start, Instant end) {
			this.start = start;
			this.end = end;
		}

		private static TimeRange parse(String value) {
			if (value.contains("/")) {
				String[] parts = value.split("/", -1);
				if (parts.length != 2) {
					return null;
				}
				Instant start = parseEndpoint(parts[0], Instant.MIN);
				Instant end = parseEndpoint(parts[1], Instant.MAX);
				return start != null && end != null ? new TimeRange(start, end) : null;
			}
			Instant instant = parseEndpoint(value, null);
			return instant != null ? new TimeRange(instant, instant) : null;
		}

		private static Instant parseEndpoint(String value, Instant openValue) {
			if (value == null || value.isBlank() || "..".equals(value)) {
				return openValue;
			}
			try {
				return Instant.parse(value);
			}
			catch (DateTimeParseException ex) {
				return null;
			}
		}

	}

}
