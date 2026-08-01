package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.advancedfiltering;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.PageDocument;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.TraversalResult;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.apicommon.Part2ApiCommonTests;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.controlstream.Part2ControlStreamSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.datastream.Part2DatastreamSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.systemevent.Part2SystemEventSupport;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;

import io.restassured.response.Response;

/**
 * Read-only released ATS support for OGC 23-002 Part 2 advanced filtering.
 */
final class Part2AdvancedFilteringSupport {

	private static final String JSON = "application/json";

	private static final String GEOJSON_JSON = "application/geo+json, application/json";

	private static final Set<String> JSON_MEDIA = Set.of(JSON);

	private static final Set<String> FOI_MEDIA = Set.of("application/geo+json", JSON);

	private final URI apiRoot;

	private final Map<String, Object> conformanceBody;

	private Map<String, Set<String>> samplingFeatureIdentifiersByLocalId;

	private Map<String, Set<String>> propertyIdentifiersByDefinition;

	private Part2AdvancedFilteringSupport(URI apiRoot, Map<String, Object> conformanceBody) {
		if (apiRoot == null || !apiRoot.isAbsolute()) {
			throw new IllegalArgumentException("apiRoot must be an absolute URI.");
		}
		this.apiRoot = apiRoot;
		this.conformanceBody = conformanceBody == null ? Map.of() : Map.copyOf(conformanceBody);
	}

	static Part2AdvancedFilteringSupport fromIut(URI iut) {
		if (iut == null || !iut.isAbsolute()) {
			throw new IllegalArgumentException("IUT must be an absolute URI.");
		}
		String value = iut.toString();
		URI apiRoot = URI.create(value.endsWith("/") ? value : value + "/");
		Response response = given().accept(JSON).when().get(apiRoot.resolve("conformance")).andReturn();
		ETSAssert.assertStatus(response, 200, Part2AdvancedFilteringTests.REQ_ADVANCED_FILTERING);
		return new Part2AdvancedFilteringSupport(apiRoot, parseObject(response,
				Part2AdvancedFilteringTests.REQ_ADVANCED_FILTERING, apiRoot.resolve("conformance").toString()));
	}

	void datastreamsFilterByPhenomenonTime(String requirement) {
		requireClass(Part2AdvancedFilteringTests.CONF_DATASTREAM, "DataStream filters require Part 2 Datastream.");
		assertTimeFilter(datastreams(), "phenomenonTime", item -> stringValue(item.get("phenomenonTime")), requirement);
	}

	void datastreamsFilterByResultTime(String requirement) {
		requireClass(Part2AdvancedFilteringTests.CONF_DATASTREAM, "DataStream filters require Part 2 Datastream.");
		assertTimeFilter(datastreams(), "resultTime", item -> stringValue(item.get("resultTime")), requirement);
	}

	void datastreamsFilterByObservedProperty(String requirement) {
		requireClass(Part2AdvancedFilteringTests.CONF_DATASTREAM,
				"DataStream observedProperty filters require Part 2 Datastream.");
		assertIdentifierFilter(datastreams(), "observedProperty",
				(item, ignored) -> propertyIdentifiers(item, "observedProperties", requirement), requirement);
	}

	void datastreamsFilterByFeatureOfInterest(String requirement) {
		requireClass(Part2AdvancedFilteringTests.CONF_DATASTREAM, "DataStream foi filters require Part 2 Datastream.");
		assertIdentifierFilter(datastreams(), "foi", (item, ignored) -> datastreamFoiIdentifiers(item, requirement),
				requirement);
	}

	void observationsFilterByPhenomenonTime(String requirement) {
		requireClass(Part2AdvancedFilteringTests.CONF_DATASTREAM,
				"Observation filters require Part 2 Datastreams and Observations.");
		assertTimeFilter(observations(), "phenomenonTime", item -> stringValue(item.get("phenomenonTime")),
				requirement);
	}

	void observationsFilterByResultTime(String requirement) {
		requireClass(Part2AdvancedFilteringTests.CONF_DATASTREAM,
				"Observation filters require Part 2 Datastreams and Observations.");
		assertTimeFilter(observations(), "resultTime", item -> stringValue(item.get("resultTime")), requirement);
		assertLatestObservationResultTime(requirement);
	}

	void observationsFilterByFeatureOfInterest(String requirement) {
		requireClass(Part2AdvancedFilteringTests.CONF_DATASTREAM,
				"Observation foi filters require Part 2 Datastreams and Observations.");
		assertIdentifierFilter(observations(), "foi", (item, ignored) -> observationFoiIdentifiers(item, requirement),
				requirement);
	}

	void controlstreamsFilterByIssueTime(String requirement) {
		requireClass(Part2AdvancedFilteringTests.CONF_CONTROLSTREAM,
				"ControlStream filters require Part 2 ControlStream.");
		assertTimeFilter(controlstreams(), "issueTime", item -> stringValue(item.get("issueTime")), requirement);
	}

	void controlstreamsFilterByExecutionTime(String requirement) {
		requireClass(Part2AdvancedFilteringTests.CONF_CONTROLSTREAM,
				"ControlStream filters require Part 2 ControlStream.");
		assertTimeFilter(controlstreams(), "executionTime", item -> stringValue(item.get("executionTime")),
				requirement);
	}

	void controlstreamsFilterByControlledProperty(String requirement) {
		requireClass(Part2AdvancedFilteringTests.CONF_CONTROLSTREAM,
				"ControlStream controlledProperty filters require Part 2 ControlStream.");
		assertIdentifierFilter(controlstreams(), "controlledProperty",
				(item, ignored) -> propertyIdentifiers(item, "controlledProperties", requirement), requirement);
	}

	void controlstreamsFilterByFeatureOfInterest(String requirement) {
		requireClass(Part2AdvancedFilteringTests.CONF_CONTROLSTREAM,
				"ControlStream foi filters require Part 2 ControlStream.");
		assertIdentifierFilter(controlstreams(), "foi",
				(item, ignored) -> controlstreamFoiIdentifiers(item, requirement), requirement);
	}

	void commandsFilterByIssueTime(String requirement) {
		requireClass(Part2AdvancedFilteringTests.CONF_CONTROLSTREAM, "Command filters require Part 2 ControlStream.");
		assertTimeFilterAcrossCommandEndpoints("issueTime", item -> stringValue(item.get("issueTime")), requirement);
	}

	void commandsFilterByExecutionTime(String requirement) {
		requireClass(Part2AdvancedFilteringTests.CONF_CONTROLSTREAM, "Command filters require Part 2 ControlStream.");
		assertTimeFilterAcrossCommandEndpoints("executionTime", item -> stringValue(item.get("executionTime")),
				requirement);
	}

	void commandsFilterByStatusCode(String requirement) {
		requireClass(Part2AdvancedFilteringTests.CONF_CONTROLSTREAM, "Command filters require Part 2 ControlStream.");
		assertValueFilterAcrossCommandEndpoints("statusCode", Part2AdvancedFilteringSupport::commandStatus,
				requirement);
	}

	void commandsFilterBySender(String requirement) {
		requireClass(Part2AdvancedFilteringTests.CONF_CONTROLSTREAM, "Command filters require Part 2 ControlStream.");
		assertValueFilterAcrossCommandEndpoints("sender", item -> stringValue(item.get("sender")), requirement);
	}

	void commandsFilterByFeatureOfInterest(String requirement) {
		requireClass(Part2AdvancedFilteringTests.CONF_CONTROLSTREAM, "Command filters require Part 2 ControlStream.");
		assertIdentifierFilterAcrossCommandEndpoints("foi", (item, ignored) -> commandFoiIdentifiers(item, requirement),
				requirement);
	}

	void commandStatusFilterByStatusCode(String requirement) {
		requireClass(Part2AdvancedFilteringTests.CONF_CONTROLSTREAM,
				"CommandStatus filters require Part 2 ControlStream.");
		List<Map<String, Object>> commands = allCommandSeeds(requirement);
		if (commands.isEmpty()) {
			throw new SkipException(
					requirement + " - no Command resources were available to derive /commands/{cmdId}/status probes.");
		}
		boolean exercised = false;
		List<String> limitations = new ArrayList<>();
		for (Map<String, Object> command : commands) {
			String commandId = stringValue(command.get("id"));
			if (commandId == null) {
				limitations.add("Command seed without local id");
				continue;
			}
			ResourceEndpoint endpoint = commandStatuses(commandId);
			TraversalResult seedStatuses = requiredTraversal(endpoint, Map.of(), requirement);
			String requested = firstValue(seedStatuses.items(), item -> stringValue(item.get("statusCode")));
			if (requested == null) {
				requested = commandStatus(command);
			}
			if (requested == null) {
				limitations.add(endpoint.label() + " exposed no statusCode evidence");
				continue;
			}
			exerciseValueFilter(endpoint, "statusCode", requested, item -> stringValue(item.get("statusCode")),
					requirement);
			exercised = true;
		}
		if (!exercised) {
			throw new SkipException(requirement + " - no CommandStatus endpoint exposed usable statusCode evidence: "
					+ String.join("; ", limitations));
		}
	}

	void systemEventsFilterByEventType(String requirement) {
		requireClass(Part2AdvancedFilteringTests.CONF_SYSTEM_EVENT, "SystemEvent filters require Part 2 SystemEvent.");
		assertValueFilter(systemEvents(), "eventType", Part2AdvancedFilteringSupport::systemEventType, requirement);
	}

	private void assertTimeFilter(ResourceEndpoint endpoint, String queryParam, TimeExtractor extractor,
			String requirement) {
		List<Map<String, Object>> seeds = seedItems(endpoint, requirement);
		String requested = firstValue(seeds, extractor::value);
		if (requested == null) {
			throw new SkipException(requirement + " - " + endpoint.label() + " exposed no " + queryParam
					+ " value usable for a read-only filter probe.");
		}
		exerciseTimeFilter(endpoint, queryParam, requested, extractor, requirement);
	}

	private void assertTimeFilterAcrossCommandEndpoints(String queryParam, TimeExtractor extractor,
			String requirement) {
		boolean exercised = false;
		List<String> limitations = new ArrayList<>();
		for (ResourceEndpoint endpoint : commandEndpoints(requirement)) {
			List<Map<String, Object>> seeds = requiredTraversal(endpoint, Map.of(), requirement).items();
			if (seeds.isEmpty()) {
				limitations.add(endpoint.label() + " returned no Command seeds");
				continue;
			}
			String requested = firstValue(seeds, extractor::value);
			if (requested == null) {
				limitations.add(endpoint.label() + " exposed no " + queryParam + " evidence");
				continue;
			}
			exerciseTimeFilter(endpoint, queryParam, requested, extractor, requirement);
			exercised = true;
		}
		if (!exercised) {
			throw new SkipException(requirement + " - no Command endpoint exposed usable " + queryParam + " evidence: "
					+ String.join("; ", limitations));
		}
	}

	private void exerciseTimeFilter(ResourceEndpoint endpoint, String queryParam, String requested,
			TimeExtractor extractor, String requirement) {
		TraversalResult filtered = requiredTraversal(endpoint, Map.of(queryParam, requested), requirement);
		requireFilteredEvidence(endpoint, queryParam, requested, filtered, requirement);
		for (Map<String, Object> item : filtered.items()) {
			String actual = extractor.value(item);
			if (!timeIntersects(actual, requested)) {
				ETSAssert.failWithUri(requirement, endpoint.label() + "?" + queryParam + "=" + requested
						+ " returned an item whose " + queryParam + " does not intersect the requested time: " + item);
			}
		}
	}

	private void assertValueFilter(ResourceEndpoint endpoint, String queryParam, ValueExtractor extractor,
			String requirement) {
		List<Map<String, Object>> seeds = seedItems(endpoint, requirement);
		String requested = firstValue(seeds, extractor::value);
		if (requested == null) {
			throw new SkipException(requirement + " - " + endpoint.label() + " exposed no " + queryParam
					+ " value usable for a read-only filter probe.");
		}
		exerciseValueFilter(endpoint, queryParam, requested, extractor, requirement);
	}

	private void assertValueFilterAcrossCommandEndpoints(String queryParam, ValueExtractor extractor,
			String requirement) {
		boolean exercised = false;
		List<String> limitations = new ArrayList<>();
		for (ResourceEndpoint endpoint : commandEndpoints(requirement)) {
			List<Map<String, Object>> seeds = requiredTraversal(endpoint, Map.of(), requirement).items();
			if (seeds.isEmpty()) {
				limitations.add(endpoint.label() + " returned no Command seeds");
				continue;
			}
			String requested = firstValue(seeds, extractor::value);
			if (requested == null) {
				limitations.add(endpoint.label() + " exposed no " + queryParam + " evidence");
				continue;
			}
			exerciseValueFilter(endpoint, queryParam, requested, extractor, requirement);
			exercised = true;
		}
		if (!exercised) {
			throw new SkipException(requirement + " - no Command endpoint exposed usable " + queryParam + " evidence: "
					+ String.join("; ", limitations));
		}
	}

	private void exerciseValueFilter(ResourceEndpoint endpoint, String queryParam, String requested,
			ValueExtractor extractor, String requirement) {
		TraversalResult filtered = requiredTraversal(endpoint, Map.of(queryParam, requested), requirement);
		requireFilteredEvidence(endpoint, queryParam, requested, filtered, requirement);
		for (Map<String, Object> item : filtered.items()) {
			if (!requested.equals(extractor.value(item))) {
				ETSAssert.failWithUri(requirement, endpoint.label() + "?" + queryParam + "=" + requested
						+ " returned an item without matching predicate evidence: " + item);
			}
		}
	}

	private void assertIdentifierFilter(ResourceEndpoint endpoint, String queryParam, IdentifierExtractor extractor,
			String requirement) {
		List<Map<String, Object>> seeds = seedItems(endpoint, requirement);
		Set<String> identifiers = firstIdentifierSet(seeds, extractor, requirement);
		if (identifiers.isEmpty()) {
			throw new SkipException(requirement + " - " + endpoint.label() + " exposed no " + queryParam
					+ " identifier evidence usable for a read-only filter probe.");
		}
		exerciseIdentifierProbes(endpoint, queryParam, identifiers, extractor, requirement);
	}

	private void assertIdentifierFilterAcrossCommandEndpoints(String queryParam, IdentifierExtractor extractor,
			String requirement) {
		boolean exercised = false;
		List<String> limitations = new ArrayList<>();
		for (ResourceEndpoint endpoint : commandEndpoints(requirement)) {
			List<Map<String, Object>> seeds = requiredTraversal(endpoint, Map.of(), requirement).items();
			if (seeds.isEmpty()) {
				limitations.add(endpoint.label() + " returned no Command seeds");
				continue;
			}
			Set<String> identifiers = firstIdentifierSet(seeds, extractor, requirement);
			if (identifiers.isEmpty()) {
				limitations.add(endpoint.label() + " exposed no " + queryParam + " identifier evidence");
				continue;
			}
			exerciseIdentifierProbes(endpoint, queryParam, identifiers, extractor, requirement);
			exercised = true;
		}
		if (!exercised) {
			throw new SkipException(requirement + " - no Command endpoint exposed usable " + queryParam
					+ " identifier evidence: " + String.join("; ", limitations));
		}
	}

	private void exerciseIdentifierProbes(ResourceEndpoint endpoint, String queryParam, Set<String> identifiers,
			IdentifierExtractor extractor, String requirement) {
		for (String requested : selectedProbeValues(identifiers)) {
			TraversalResult filtered = requiredTraversal(endpoint, Map.of(queryParam, requested), requirement);
			requireFilteredEvidence(endpoint, queryParam, requested, filtered, requirement);
			for (Map<String, Object> item : filtered.items()) {
				if (!extractor.identifiers(item, requirement).contains(requested)) {
					ETSAssert.failWithUri(requirement, endpoint.label() + "?" + queryParam + "=" + requested
							+ " returned an item without matching identifier evidence: " + item);
				}
			}
		}
	}

	private void assertLatestObservationResultTime(String requirement) {
		List<Map<String, Object>> seeds = seedItems(observations(), requirement);
		String latest = latestInstantString(seeds, item -> stringValue(item.get("resultTime")));
		if (latest == null) {
			Reporter.log(requirement
					+ " - resultTime=latest was not exercised because Observation seeds exposed no parseable resultTime values.",
					true);
			return;
		}
		TraversalResult filtered = requiredTraversal(observations(), Map.of("resultTime", "latest"), requirement);
		requireFilteredEvidence(observations(), "resultTime", "latest", filtered, requirement);
		for (Map<String, Object> item : filtered.items()) {
			String actual = stringValue(item.get("resultTime"));
			if (actual == null || !actual.equals(latest)) {
				ETSAssert.failWithUri(requirement,
						"/observations?resultTime=latest returned an Observation whose resultTime is not the latest seed resultTime "
								+ latest + ": " + item);
			}
		}
	}

	private List<Map<String, Object>> allCommandSeeds(String requirement) {
		List<Map<String, Object>> commands = new ArrayList<>();
		for (ResourceEndpoint endpoint : commandEndpoints(requirement)) {
			commands.addAll(requiredTraversal(endpoint, Map.of(), requirement).items());
		}
		return List.copyOf(commands);
	}

	private List<ResourceEndpoint> commandEndpoints(String requirement) {
		List<ResourceEndpoint> endpoints = new ArrayList<>();
		endpoints.add(commands());
		TraversalResult controlStreams = requiredTraversal(controlstreams(), Map.of(), requirement);
		for (Map<String, Object> controlStream : controlStreams.items()) {
			String id = stringValue(controlStream.get("id"));
			if (id != null) {
				endpoints.add(controlstreamCommands(id));
			}
		}
		return List.copyOf(endpoints);
	}

	private List<Map<String, Object>> seedItems(ResourceEndpoint endpoint, String requirement) {
		List<Map<String, Object>> seeds = requiredTraversal(endpoint, Map.of(), requirement).items();
		if (seeds.isEmpty()) {
			throw new SkipException(requirement + " - " + endpoint.label()
					+ " returned no resources; no seed-derived filter probe can be constructed.");
		}
		return seeds;
	}

	private TraversalResult requiredTraversal(ResourceEndpoint endpoint, Map<String, String> query,
			String requirement) {
		Optional<TraversalResult> result = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint.uri(), endpoint.accept(),
				query, requirement, endpoint.supportedMediaTypes());
		if (result.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint.label() + " returned HTTP 404.");
		}
		endpoint.validator().validate(endpoint.uri(), result.get().pages(), requirement);
		return result.get();
	}

	private void requireFilteredEvidence(ResourceEndpoint endpoint, String queryParam, String requested,
			TraversalResult filtered, String requirement) {
		if (filtered.items().isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint.label() + "?" + queryParam + "=" + requested
					+ " returned no resources for a seed-derived filter value.");
		}
	}

	private Set<String> propertyIdentifiers(Map<String, Object> item, String arrayName, String requirement) {
		Set<String> identifiers = new LinkedHashSet<>();
		Object properties = item.get(arrayName);
		if (properties instanceof List) {
			for (Object property : (List<?>) properties) {
				if (property instanceof Map) {
					Map<?, ?> map = (Map<?, ?>) property;
					addIdentifier(identifiers, map.get("id"));
					addIdentifier(identifiers, map.get("uid"));
					addIdentifier(identifiers, map.get("uniqueId"));
					String definition = stringValue(map.get("definition"));
					addIdentifier(identifiers, definition);
					identifiers.addAll(propertyIdentifiersForDefinition(definition, requirement));
				}
				else {
					addIdentifier(identifiers, property);
				}
			}
		}
		return identifiers;
	}

	private Set<String> datastreamFoiIdentifiers(Map<String, Object> item, String requirement) {
		Set<String> identifiers = streamFoiIdentifiers(item, requirement);
		String id = stringValue(item.get("id"));
		if (id != null) {
			collectSamplingFeatureSubresource("datastreams", id, identifiers, requirement,
					Part2DatastreamSupport::validateFeatureOfInterestEndpoint);
		}
		return identifiers;
	}

	private Set<String> controlstreamFoiIdentifiers(Map<String, Object> item, String requirement) {
		Set<String> identifiers = streamFoiIdentifiers(item, requirement);
		String id = stringValue(item.get("id"));
		if (id != null) {
			collectSamplingFeatureSubresource("controlstreams", id, identifiers, requirement,
					Part2ControlStreamSupport::validateFeatureOfInterestEndpoint);
		}
		return identifiers;
	}

	private Set<String> streamFoiIdentifiers(Map<String, Object> item, String requirement) {
		Set<String> identifiers = new LinkedHashSet<>();
		collectReferenceValue(item.get("samplingFeature@id"), identifiers);
		collectReferenceValue(item.get("samplingFeature@link"), identifiers);
		collectReferenceValue(item.get("featureOfInterest@link"), identifiers);
		collectReferenceValue(item.get("samplingFeature"), identifiers);
		collectReferenceValue(item.get("featureOfInterest"), identifiers);
		expandSamplingFeatureLocals(identifiers, requirement);
		return identifiers;
	}

	private Set<String> observationFoiIdentifiers(Map<String, Object> item, String requirement) {
		Set<String> identifiers = new LinkedHashSet<>();
		collectReferenceValue(item.get("samplingFeature@id"), identifiers);
		collectReferenceValue(item.get("samplingFeature"), identifiers);
		collectReferenceValue(item.get("featureOfInterest"), identifiers);
		expandSamplingFeatureLocals(identifiers, requirement);
		return identifiers;
	}

	private Set<String> commandFoiIdentifiers(Map<String, Object> item, String requirement) {
		Set<String> identifiers = new LinkedHashSet<>();
		collectReferenceValue(item.get("samplingFeature@id"), identifiers);
		collectReferenceValue(item.get("samplingFeature"), identifiers);
		collectReferenceValue(item.get("featureOfInterest"), identifiers);
		expandSamplingFeatureLocals(identifiers, requirement);
		return identifiers;
	}

	private void collectSamplingFeatureSubresource(String parentPath, String parentId, Set<String> identifiers,
			String requirement, EndpointValidator validator) {
		ResourceEndpoint endpoint = new ResourceEndpoint(
				this.apiRoot.resolve(parentPath + "/" + encode(parentId) + "/samplingFeatures"),
				"/" + parentPath + "/" + parentId + "/samplingFeatures", GEOJSON_JSON, FOI_MEDIA, validator);
		Optional<TraversalResult> result = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint.uri(), endpoint.accept(),
				Map.of(), requirement, endpoint.supportedMediaTypes());
		if (result.isEmpty()) {
			return;
		}
		validator.validate(endpoint.uri(), result.get().pages(), requirement);
		for (Map<String, Object> feature : result.get().items()) {
			collectFeatureIdentifiers(feature, identifiers);
		}
	}

	private void collectFeatureIdentifiers(Map<String, Object> feature, Set<String> identifiers) {
		addIdentifier(identifiers, feature.get("id"));
		Object properties = feature.get("properties");
		if (properties instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) properties;
			addIdentifier(identifiers, map.get("uid"));
			addIdentifier(identifiers, map.get("uniqueId"));
		}
		collectReferenceValue(feature.get("sampleOf"), identifiers);
		collectReferenceValue(feature.get("sampledFeature"), identifiers);
		collectReferenceValue(feature.get("links"), identifiers);
	}

	private void expandSamplingFeatureLocals(Set<String> identifiers, String requirement) {
		List<String> locals = identifiers.stream().filter(value -> !isGlobalIdentifier(value)).toList();
		for (String local : locals) {
			identifiers.addAll(samplingFeatureIdentifiersForLocal(local, requirement));
		}
	}

	private Set<String> samplingFeatureIdentifiersForLocal(String localId, String requirement) {
		if (localId == null || localId.isBlank()) {
			return Set.of();
		}
		if (this.samplingFeatureIdentifiersByLocalId == null) {
			this.samplingFeatureIdentifiersByLocalId = loadSamplingFeatureIdentifiers(requirement);
		}
		return this.samplingFeatureIdentifiersByLocalId.getOrDefault(localId, Set.of(localId));
	}

	private Map<String, Set<String>> loadSamplingFeatureIdentifiers(String requirement) {
		Optional<TraversalResult> result = Part1ApiCommonSupport.resourcesAtEndpoint(
				this.apiRoot.resolve("samplingFeatures"), GEOJSON_JSON, Map.of(), requirement, FOI_MEDIA);
		if (result.isEmpty()) {
			return Map.of();
		}
		Map<String, Set<String>> byLocal = new LinkedHashMap<>();
		for (Map<String, Object> feature : result.get().items()) {
			String local = stringValue(feature.get("id"));
			if (local == null) {
				continue;
			}
			Set<String> identifiers = new LinkedHashSet<>();
			collectFeatureIdentifiers(feature, identifiers);
			identifiers.add(local);
			byLocal.put(local, Set.copyOf(identifiers));
		}
		return Map.copyOf(byLocal);
	}

	private Set<String> propertyIdentifiersForDefinition(String definition, String requirement) {
		if (definition == null || definition.isBlank()) {
			return Set.of();
		}
		if (this.propertyIdentifiersByDefinition == null) {
			this.propertyIdentifiersByDefinition = loadPropertyIdentifiers(requirement);
		}
		return this.propertyIdentifiersByDefinition.getOrDefault(definition, Set.of());
	}

	private Map<String, Set<String>> loadPropertyIdentifiers(String requirement) {
		Optional<TraversalResult> result = Part1ApiCommonSupport.resourcesAtEndpoint(this.apiRoot.resolve("properties"),
				"application/sml+json, application/json", Map.of(), requirement, Set.of("application/sml+json", JSON));
		if (result.isEmpty()) {
			return Map.of();
		}
		Map<String, Set<String>> byDefinition = new LinkedHashMap<>();
		for (Map<String, Object> property : result.get().items()) {
			String definition = stringValue(property.get("definition"));
			if (definition == null) {
				continue;
			}
			Set<String> identifiers = new LinkedHashSet<>();
			addIdentifier(identifiers, property.get("id"));
			addIdentifier(identifiers, property.get("uid"));
			addIdentifier(identifiers, property.get("uniqueId"));
			addIdentifier(identifiers, definition);
			byDefinition.put(definition, Set.copyOf(identifiers));
		}
		return Map.copyOf(byDefinition);
	}

	private ResourceEndpoint datastreams() {
		return new ResourceEndpoint(this.apiRoot.resolve("datastreams"), "/datastreams", JSON, JSON_MEDIA,
				Part2DatastreamSupport::validateDatastreamEndpoint);
	}

	private ResourceEndpoint observations() {
		return new ResourceEndpoint(this.apiRoot.resolve("observations"), "/observations", JSON, JSON_MEDIA,
				Part2DatastreamSupport::validateObservationEndpoint);
	}

	private ResourceEndpoint controlstreams() {
		return new ResourceEndpoint(this.apiRoot.resolve("controlstreams"), "/controlstreams", JSON, JSON_MEDIA,
				Part2ControlStreamSupport::validateControlStreamEndpoint);
	}

	private ResourceEndpoint commands() {
		return new ResourceEndpoint(this.apiRoot.resolve("commands"), "/commands", JSON, JSON_MEDIA,
				Part2ControlStreamSupport::validateCommandEndpoint);
	}

	private ResourceEndpoint controlstreamCommands(String controlstreamId) {
		return new ResourceEndpoint(this.apiRoot.resolve("controlstreams/" + encode(controlstreamId) + "/commands"),
				"/controlstreams/" + controlstreamId + "/commands", JSON, JSON_MEDIA,
				Part2ControlStreamSupport::validateCommandEndpoint);
	}

	private ResourceEndpoint commandStatuses(String commandId) {
		return new ResourceEndpoint(this.apiRoot.resolve("commands/" + encode(commandId) + "/status"),
				"/commands/" + commandId + "/status", JSON, JSON_MEDIA,
				Part2ControlStreamSupport::validateCommandStatusEndpoint);
	}

	private ResourceEndpoint systemEvents() {
		return new ResourceEndpoint(this.apiRoot.resolve("systemEvents"), "/systemEvents", JSON, JSON_MEDIA,
				Part2SystemEventSupport::validateSystemEventEndpoint);
	}

	private void requireClass(String conformanceUri, String reason) {
		if (!declares(Part2AdvancedFilteringTests.CONF_ADVANCED_FILTERING)) {
			throw new SkipException(Part2AdvancedFilteringTests.CONF_ADVANCED_FILTERING
					+ " - IUT does not declare the CS API Part 2 Advanced Filtering conformance class.");
		}
		if (!declares(Part2AdvancedFilteringTests.CONF_PART2_API_COMMON)) {
			throw new SkipException(Part2AdvancedFilteringTests.CONF_PART2_API_COMMON
					+ " - /req/advanced-filtering lists /req/api-common as a prerequisite.");
		}
		if (!declares(Part2AdvancedFilteringTests.CONF_PART1_ADVANCED_FILTERING)) {
			throw new SkipException(Part2AdvancedFilteringTests.CONF_PART1_ADVANCED_FILTERING
					+ " - Part 2 /req/advanced-filtering lists Part 1 /req/advanced-filtering as a prerequisite.");
		}
		if (!declares(conformanceUri)) {
			throw new SkipException(conformanceUri + " - " + reason);
		}
	}

	private boolean declares(String conformanceUri) {
		return Part2ApiCommonTests.declaresConformance(this.conformanceBody, conformanceUri);
	}

	static void skipWhenPrerequisiteUnsatisfied(ITestContext testContext) {
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
					"Part 2 Advanced Filtering setup skipped before IUT access because prerequisite " + blocker + ".");
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

	private static String testBlocker(IResultMap results, String status) {
		if (results == null) {
			return null;
		}
		for (ITestResult result : results.getAllResults()) {
			if (result != null && result.getMethod() != null && isInheritedPrerequisite(result)) {
				return "method " + result.getMethod().getMethodName() + " " + status;
			}
		}
		return null;
	}

	private static boolean isInheritedPrerequisite(ITestResult result) {
		for (String group : result.getMethod().getGroups()) {
			if ("part2apicommon".equals(group) || "advancedfiltering".equals(group)) {
				return true;
			}
		}
		Class<?> realClass = result.getMethod().getRealClass();
		if (realClass == null) {
			return false;
		}
		String className = realClass.getName();
		return realClass == Part2ApiCommonTests.class
				|| className.startsWith("org.opengis.cite.ogcapiconnectedsystems10.conformance.advancedfiltering.");
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
		return !candidateRange.end().isBefore(requestedRange.start())
				&& !requestedRange.end().isBefore(candidateRange.start());
	}

	static String commandStatus(Map<String, Object> command) {
		Object status = command.get("currentStatus");
		if (status instanceof String) {
			return (String) status;
		}
		if (status instanceof Map) {
			return stringValue(((Map<?, ?>) status).get("statusCode"));
		}
		return null;
	}

	static String systemEventType(Map<String, Object> event) {
		String eventType = stringValue(event.get("eventType"));
		if (eventType != null) {
			return eventType;
		}
		return stringValue(event.get("definition"));
	}

	private static Set<String> firstIdentifierSet(List<Map<String, Object>> items, IdentifierExtractor extractor,
			String requirement) {
		for (Map<String, Object> item : items) {
			Set<String> identifiers = extractor.identifiers(item, requirement);
			if (!identifiers.isEmpty()) {
				return identifiers;
			}
		}
		return Set.of();
	}

	private static List<String> selectedProbeValues(Set<String> identifiers) {
		List<String> selected = new ArrayList<>();
		identifiers.stream().filter(value -> !isGlobalIdentifier(value)).findFirst().ifPresent(selected::add);
		identifiers.stream()
			.filter(Part2AdvancedFilteringSupport::isGlobalIdentifier)
			.filter(value -> !selected.contains(value))
			.findFirst()
			.ifPresent(selected::add);
		if (selected.isEmpty() && !identifiers.isEmpty()) {
			selected.add(identifiers.iterator().next());
		}
		return List.copyOf(selected);
	}

	private static boolean isGlobalIdentifier(String value) {
		if (value == null) {
			return false;
		}
		String lower = value.toLowerCase(Locale.ROOT);
		return lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("urn:")
				|| lower.startsWith("tag:") || lower.startsWith("uuid:");
	}

	private static void collectReferenceValue(Object value, Set<String> identifiers) {
		if (value instanceof String) {
			addIdentifier(identifiers, value);
			return;
		}
		if (value instanceof List) {
			for (Object child : (List<?>) value) {
				collectReferenceValue(child, identifiers);
			}
			return;
		}
		if (!(value instanceof Map)) {
			return;
		}
		Map<?, ?> map = (Map<?, ?>) value;
		addIdentifier(identifiers, map.get("id"));
		addIdentifier(identifiers, map.get("uid"));
		addIdentifier(identifiers, map.get("uniqueId"));
		addIdentifier(identifiers, map.get("definition"));
		addIdentifier(identifiers, map.get("href"));
		Object properties = map.get("properties");
		if (properties instanceof Map) {
			Map<?, ?> propertyMap = (Map<?, ?>) properties;
			addIdentifier(identifiers, propertyMap.get("uid"));
			addIdentifier(identifiers, propertyMap.get("uniqueId"));
		}
		collectReferenceValue(map.get("sampleOf"), identifiers);
		collectReferenceValue(map.get("sampledFeature"), identifiers);
	}

	private static void addIdentifier(Set<String> identifiers, Object value) {
		if (value instanceof String && !((String) value).isBlank()) {
			identifiers.add((String) value);
		}
	}

	private static String firstValue(List<Map<String, Object>> items, ValueExtractor extractor) {
		for (Map<String, Object> item : items) {
			String value = extractor.value(item);
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		return null;
	}

	private static String latestInstantString(List<Map<String, Object>> items, ValueExtractor extractor) {
		Instant latest = null;
		String latestString = null;
		for (Map<String, Object> item : items) {
			String value = extractor.value(item);
			if (value == null) {
				continue;
			}
			try {
				Instant instant = Instant.parse(value);
				if (latest == null || instant.isAfter(latest)) {
					latest = instant;
					latestString = value;
				}
			}
			catch (DateTimeParseException ex) {
				// Ignore non-instant seed values for the optional latest probe.
			}
		}
		return latestString;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> parseObject(Response response, String requirement, String source) {
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

	private static String stringValue(Object value) {
		return value instanceof String && !((String) value).isBlank() ? (String) value : null;
	}

	private static String encode(String value) {
		return Part2ControlStreamSupport.encodePathToken(value);
	}

	private record ResourceEndpoint(URI uri, String label, String accept, Set<String> supportedMediaTypes,
			EndpointValidator validator) {
	}

	@FunctionalInterface
	private interface EndpointValidator {

		void validate(URI endpoint, List<PageDocument> pages, String requirement);

	}

	@FunctionalInterface
	private interface TimeExtractor {

		String value(Map<String, Object> item);

	}

	@FunctionalInterface
	private interface ValueExtractor {

		String value(Map<String, Object> item);

	}

	@FunctionalInterface
	private interface IdentifierExtractor {

		Set<String> identifiers(Map<String, Object> item, String requirement);

	}

	private record TimeRange(Instant start, Instant end) {

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
