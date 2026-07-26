package org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.builder.ResponseBuilder;
import io.restassured.response.Response;
import org.junit.Test;
import org.testng.SkipException;

/**
 * Focused checks for REQ-ETS-PART1-001 and the released API Common helpers.
 */
public class VerifyPart1ApiCommonSupport {

	private static final String REQUIREMENT = "http://www.opengis.net/spec/ogcapi-connected-systems-1/1.0/req/api-common/resource-ids";

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-CANONICAL-RESOURCES-001.
	 */
	@Test
	public void canonicalResourcesFollowRelativeNextLinks() {
		URI root = URI.create("https://example.test/api/");
		Map<URI, Response> responses = new LinkedHashMap<>();
		responses.put(root.resolve("systems"), json(200, """
				{"items":[{"id":"one"}],
				 "links":[{"rel":"next","href":"systems?cursor=two"}]}
				"""));
		responses.put(root.resolve("systems?cursor=two"), json(200, """
				{"items":[{"id":"two"}],"links":[]}
				"""));
		List<URI> requests = new ArrayList<>();

		Optional<List<Map<String, Object>>> resources = Part1ApiCommonSupport.canonicalResources(root, "systems",
				(uri, accept, query) -> {
					requests.add(uri);
					return responses.get(uri);
				});

		assertTrue(resources.isPresent());
		assertEquals(List.of("one", "two"), resources.orElseThrow().stream().map(item -> item.get("id")).toList());
		assertEquals(List.of(root.resolve("systems"), root.resolve("systems?cursor=two")), requests);
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-CANONICAL-RESOURCES-001.
	 */
	@Test
	public void canonicalResourcesParseGeoJsonFeatures() {
		URI root = URI.create("https://example.test/api/");
		List<String> accepts = new ArrayList<>();

		Optional<List<Map<String, Object>>> resources = Part1ApiCommonSupport.canonicalResources(root, "systems",
				(uri, accept, query) -> {
					accepts.add(accept);
					return json(200, "application/geo+json", """
							{"type":"FeatureCollection",
							 "features":[{"type":"Feature","id":"system-1",
							 "properties":{"uid":"urn:ogc:def:system:system-1"}}],
							 "links":[]}
							""");
				});

		assertEquals(List.of("system-1"), resources.orElseThrow().stream().map(item -> item.get("id")).toList());
		assertTrue(accepts.get(0).contains("application/geo+json"));
	}

	/**
	 * REQ-ETS-PART1-001, REQ-ETS-PART1-002;
	 * SCENARIO-ETS-PART1-002-RELEASED-RESOURCES-ENDPOINT-001.
	 */
	@Test
	public void detailedTraversalRetainsActualPageEvidenceWithoutDuplicateRequests() {
		URI root = URI.create("https://example.test/api/");
		Map<URI, Response> responses = new LinkedHashMap<>();
		responses.put(root.resolve("systems"), json(200, "application/geo+json", """
				{"type":"FeatureCollection","features":[{"id":"one"}],
				 "links":[{"rel":"next","href":"systems?cursor=two"}]}
				"""));
		responses.put(root.resolve("systems?cursor=two"), json(200, "application/sml+json", """
				{"items":[{"id":"two"}],"links":[]}
				"""));
		List<URI> requests = new ArrayList<>();

		Part1ApiCommonSupport.TraversalResult result = Part1ApiCommonSupport
			.canonicalResourcesDetailed(root, "systems", (uri, accept, query) -> {
				requests.add(uri);
				return responses.get(uri);
			})
			.orElseThrow();

		assertEquals(List.of("one", "two"), result.items().stream().map(item -> item.get("id")).toList());
		assertEquals(List.of("application/geo+json", "application/sml+json"),
				result.pages().stream().map(Part1ApiCommonSupport.PageDocument::mediaType).toList());
		assertEquals(List.of(root.resolve("systems"), root.resolve("systems?cursor=two")),
				result.pages().stream().map(Part1ApiCommonSupport.PageDocument::source).toList());
		assertEquals(requests, result.pages().stream().map(Part1ApiCommonSupport.PageDocument::source).toList());
		assertEquals("FeatureCollection", result.pages().get(0).body().get("type"));
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-CANONICAL-RESOURCES-001.
	 */
	@Test
	public void canonicalResourcesParseSensorMlItems() {
		URI root = URI.create("https://example.test/api/");
		List<String> accepts = new ArrayList<>();

		Optional<List<Map<String, Object>>> resources = Part1ApiCommonSupport.canonicalResources(root, "properties",
				(uri, accept, query) -> {
					accepts.add(accept);
					return json(200, "application/sml+json", """
							{"items":[{"id":"temperature",
							 "uniqueId":"urn:ogc:def:property:OGC::AirTemperature"}],
							 "links":[]}
							""");
				});

		assertEquals("urn:ogc:def:property:OGC::AirTemperature",
				Part1ApiCommonSupport.resourceUid(resources.orElseThrow().get(0)).orElseThrow());
		assertTrue(accepts.get(0).contains("application/sml+json"));
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-PAGINATION-FAIL-CLOSED-001.
	 */
	@Test
	public void paginationCyclesFailClosed() {
		URI root = URI.create("https://example.test/api/");
		Response cycle = json(200, """
				{"items":[],"links":[{"rel":"next","href":"systems"}]}
				""");

		AssertionError error = assertThrows(AssertionError.class,
				() -> Part1ApiCommonSupport.canonicalResources(root, "systems", (uri, accept, query) -> cycle));

		assertTrue(error.getMessage().contains("pagination cycle"));
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-PAGINATION-FAIL-CLOSED-001.
	 */
	@Test
	public void crossOriginPaginationFailsBeforeCredentialBearingFollowUp() {
		URI root = URI.create("https://example.test/api/");
		Response crossOrigin = json(200, """
				{"items":[],"links":[{"rel":"next","href":"https://other.test/api/systems"}]}
				""");
		List<URI> requests = new ArrayList<>();

		AssertionError error = assertThrows(AssertionError.class,
				() -> Part1ApiCommonSupport.canonicalResources(root, "systems", (uri, accept, query) -> {
					requests.add(uri);
					return crossOrigin;
				}));

		assertTrue(error.getMessage().contains("cross-origin"));
		assertEquals(List.of(root.resolve("systems")), requests);
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-CANONICAL-RESOURCES-001.
	 */
	@Test
	public void canonical404MeansUnsupportedResourceType() {
		URI root = URI.create("https://example.test/api/");

		Optional<List<Map<String, Object>>> resources = Part1ApiCommonSupport.canonicalResources(root, "properties",
				(uri, accept, query) -> json(404, "{}"));

		assertTrue(resources.isEmpty());
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-COLLECTION-ITEMS-001.
	 */
	@Test
	public void collectionItemsUseReleasedPathAndAdvertisedMediaType() {
		URI root = URI.create("https://example.test/api/");
		Map<String, Object> collection = Map.of("id", "weather stations", "links", List.of(Map.of("rel", "items",
				"href", "collections/weather%20stations/items", "type", "application/geo+json")));
		List<URI> requests = new ArrayList<>();
		List<String> accepts = new ArrayList<>();

		Optional<List<Map<String, Object>>> items = Part1ApiCommonSupport.collectionItems(root, collection,
				(uri, accept, query) -> {
					requests.add(uri);
					accepts.add(accept);
					return json(200, "application/geo+json", """
							{"type":"FeatureCollection","features":[{"id":"station-1"}],"links":[]}
							""");
				});

		assertTrue(items.isPresent());
		assertEquals(List.of(root.resolve("collections/weather%20stations/items")), requests);
		assertEquals(List.of("application/geo+json"), accepts);
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-COLLECTION-ITEMS-001.
	 */
	@Test
	public void collectionItemsParseSensorMlItems() {
		URI root = URI.create("https://example.test/api/");
		Map<String, Object> collection = Map.of("id", "procedures", "links", List
			.of(Map.of("rel", "items", "href", "collections/procedures/items", "type", "application/sml+json")));

		Optional<List<Map<String, Object>>> items = Part1ApiCommonSupport.collectionItems(root, collection,
				(uri, accept, query) -> json(200, "application/sml+json", """
						{"items":[{"id":"procedure-1","uniqueId":"urn:ogc:def:procedure:example"}],"links":[]}
						"""));

		assertEquals("procedure-1", items.orElseThrow().get(0).get("id"));
	}

	/**
	 * REQ-ETS-PART1-004; SCENARIO-ETS-PART1-004-RELEASED-MEDIA-GATE-001.
	 */
	@Test
	public void restrictedCollectionItemsPreferSupportedAdvertisedMedia() {
		URI root = URI.create("https://example.test/api/");
		for (String supported : List.of("application/geo+json", "application/sml+json")) {
			Map<String, Object> collection = Map.of("id", "deployments", "links", List
				.of(Map.of("rel", "items", "type", "application/json"), Map.of("rel", "items", "type", supported)));
			List<String> accepts = new ArrayList<>();
			String body = "application/geo+json".equals(supported)
					? "{\"type\":\"FeatureCollection\",\"features\":[{\"id\":\"deployment-1\"}],\"links\":[]}"
					: "{\"items\":[{\"id\":\"deployment-1\"}],\"links\":[]}";

			Optional<Part1ApiCommonSupport.TraversalResult> result = Part1ApiCommonSupport.collectionItemsDetailed(root,
					collection, Set.of("application/geo+json", "application/sml+json"), (uri, accept, query) -> {
						accepts.add(accept);
						return json(200, supported, body);
					});

			assertEquals(List.of(supported), accepts);
			assertEquals("deployment-1", result.orElseThrow().items().get(0).get("id"));
		}
	}

	/**
	 * REQ-ETS-PART1-004; SCENARIO-ETS-PART1-004-RELEASED-MEDIA-GATE-001.
	 */
	@Test
	public void restrictedCollectionItemsStillGateActualMediaBeforeParsing() {
		URI root = URI.create("https://example.test/api/");
		Map<String, Object> collection = Map.of("id", "deployments", "links",
				List.of(Map.of("rel", "items", "type", "application/geo+json")));

		assertThrows(SkipException.class,
				() -> Part1ApiCommonSupport.collectionItemsDetailed(root, collection,
						Set.of("application/geo+json", "application/sml+json"),
						(uri, accept, query) -> json(200, "application/json", "not JSON")));
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-COLLECTION-ITEMS-001.
	 */
	@Test
	public void unsupportedCollectionItemsMediaTypeHasNoPositiveEvidence() {
		URI root = URI.create("https://example.test/api/");
		Map<String, Object> collection = Map.of("id", "systems", "links",
				List.of(Map.of("rel", "items", "href", "collections/systems/items", "type", "text/csv")));

		Optional<List<Map<String, Object>>> items = Part1ApiCommonSupport.collectionItems(root, collection,
				(uri, accept, query) -> {
					throw new AssertionError("request must not be sent");
				});

		assertTrue(items.isEmpty());
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-RESOURCE-UIDS-001.
	 */
	@Test
	public void uidExtractionAndAbsoluteUriValidationCoverConnectedSystemsShape() {
		Map<String, Object> nested = Map.of("properties", Map.of("uid", "urn:ogc:def:system:example"));
		Map<String, Object> sensorMl = Map.of("uniqueId", "urn:ogc:def:procedure:example");

		assertEquals("urn:ogc:def:system:example", Part1ApiCommonSupport.resourceUid(nested).orElseThrow());
		assertEquals("urn:ogc:def:procedure:example", Part1ApiCommonSupport.resourceUid(sensorMl).orElseThrow());
		assertTrue(Part1ApiCommonSupport.isValidAbsoluteUri("urn:ogc:def:system:example"));
		assertTrue(Part1ApiCommonSupport.isValidAbsoluteUri("https://example.test/resources/1"));
		assertFalse(Part1ApiCommonSupport.isValidAbsoluteUri("relative/resource"));
		assertFalse(Part1ApiCommonSupport.isValidAbsoluteUri("urn with spaces"));
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-RESOURCE-UIDS-001.
	 */
	@Test
	public void sensorMlUniqueIdHasNormativeUidPrecedence() {
		Map<String, Object> conflicting = Map.of("uniqueId", "urn:ogc:def:procedure:normative", "properties",
				Map.of("uid", "urn:example:geojson"), "uid", "urn:example:extension");

		assertEquals("urn:ogc:def:procedure:normative", Part1ApiCommonSupport.resourceUid(conflicting).orElseThrow());
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-RESOURCE-UID-TYPES-001.
	 */
	@Test
	public void uidRecommendationAcceptsUuidAndKnownRegisteredUrnNamespaces() {
		assertTrue(Part1ApiCommonSupport.isRecommendedUid("urn:uuid:123e4567-e89b-12d3-a456-426614174000"));
		assertTrue(Part1ApiCommonSupport.isRecommendedUid("urn:ogc:def:object:example"));
		assertTrue(Part1ApiCommonSupport.isRecommendedUid("urn:gdr:example"));
		assertTrue(Part1ApiCommonSupport.isRecommendedUid("urn:3gpp:example"));
		assertTrue(Part1ApiCommonSupport.isRecommendedUid("urn:urn-8:example"));
		assertFalse(Part1ApiCommonSupport.isRecommendedUid("urn:uuid:1-1-1-1-1"));
		assertFalse(Part1ApiCommonSupport.isRecommendedUid("urn:ets:local:fixture"));
		assertFalse(Part1ApiCommonSupport.isRecommendedUid("https://example.test/resources/1"));
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-RESOURCE-UID-TYPES-001.
	 */
	@Test
	public void ianaUrnRegistrySnapshotHasExactProvenanceAndCategoryCounts() throws Exception {
		try (var input = getClass()
			.getResourceAsStream("/org/opengis/cite/ogcapiconnectedsystems10/registries/iana-urn-namespaces.json")) {
			assertTrue(input != null);
			JsonNode root = new ObjectMapper().readTree(input);
			assertEquals("1.0", root.path("schemaVersion").asText());
			assertEquals("https://www.iana.org/assignments/urn-namespaces/urn-namespaces.xml",
					root.path("source").asText());
			assertEquals("2026-07-26", root.path("retrievedOn").asText());
			assertEquals("2026-05-28", root.path("registryUpdated").asText());
			assertEquals("a2c5f8f6bb1e34ea102211b3eff81131c73f2e27a69f90c9d36b48f2471b9604",
					root.path("sourceSha256").asText());
			List<String> formal = new ArrayList<>();
			root.path("formalNamespaces").forEach(value -> formal.add(value.asText()));
			List<String> informal = new ArrayList<>();
			root.path("informalNamespaces").forEach(value -> informal.add(value.asText()));
			List<String> namespaces = new ArrayList<>(formal);
			namespaces.addAll(informal);
			assertEquals(96, formal.size());
			assertEquals(8, informal.size());
			assertEquals(104, new HashSet<>(namespaces).size());
		}
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-DATETIME-001.
	 */
	@Test
	public void temporalExtentProducesAllReleasedDatetimeForms() {
		Map<String, Object> collection = Map.of("extent", Map.of("temporal",
				Map.of("interval", List.of(List.of("2026-01-01T00:00:00Z", "2026-01-03T00:00:00Z")))));

		List<Part1ApiCommonSupport.DatetimeQuery> queries = Part1ApiCommonSupport.datetimeQueries(collection);

		assertEquals(
				List.of("2026-01-02T00:00:00Z", "2026-01-01T00:00:00Z/2026-01-03T00:00:00Z", "../2026-01-02T00:00:00Z",
						"2026-01-02T00:00:00Z/.."),
				queries.stream().map(Part1ApiCommonSupport.DatetimeQuery::parameter).toList());
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-DATETIME-001.
	 */
	@Test
	public void validTimeIntersectionHandlesIntervalsInstantsAndTimelessFeatures() {
		Instant requestTime = Instant.parse("2026-01-04T00:00:00Z");
		Part1ApiCommonSupport.DatetimeQuery instantQuery = new Part1ApiCommonSupport.DatetimeQuery(
				"2026-01-02T00:00:00Z", Instant.parse("2026-01-02T00:00:00Z"), Instant.parse("2026-01-02T00:00:00Z"));
		Part1ApiCommonSupport.DatetimeQuery boundedQuery = new Part1ApiCommonSupport.DatetimeQuery(
				"2026-01-01T00:00:00Z/2026-01-03T00:00:00Z", Instant.parse("2026-01-01T00:00:00Z"),
				Instant.parse("2026-01-03T00:00:00Z"));
		Part1ApiCommonSupport.DatetimeQuery openStartQuery = new Part1ApiCommonSupport.DatetimeQuery(
				"../2026-01-02T00:00:00Z", null, Instant.parse("2026-01-02T00:00:00Z"));
		Part1ApiCommonSupport.DatetimeQuery openEndQuery = new Part1ApiCommonSupport.DatetimeQuery(
				"2026-01-02T00:00:00Z/..", Instant.parse("2026-01-02T00:00:00Z"), null);
		Map<String, Object> interval = feature("interval", List.of("2026-01-01T00:00:00Z", "2026-01-03T00:00:00Z"));
		Map<String, Object> instant = feature("instant", "2026-01-02T00:00:00Z");
		Map<String, Object> outside = feature("outside", List.of("2027-01-01T00:00:00Z", "2027-01-03T00:00:00Z"));
		Map<String, Object> timeless = Map.of("id", "timeless", "properties", Map.of());

		assertTrue(Part1ApiCommonSupport.validTimeIntersects(interval, instantQuery, requestTime));
		assertTrue(Part1ApiCommonSupport.validTimeIntersects(instant, boundedQuery, requestTime));
		assertTrue(Part1ApiCommonSupport.validTimeIntersects(interval, openStartQuery, requestTime));
		assertTrue(Part1ApiCommonSupport.validTimeIntersects(interval, openEndQuery, requestTime));
		assertFalse(Part1ApiCommonSupport.validTimeIntersects(outside, instantQuery, requestTime));
		assertTrue(Part1ApiCommonSupport.validTimeIntersects(timeless, instantQuery, requestTime));
		assertTrue(Part1ApiCommonSupport.isTimeless(timeless));
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-DATETIME-001.
	 */
	@Test
	public void nowBoundsUseCapturedRequestTimeWithInclusiveIntersection() {
		Instant requestTime = Instant.parse("2026-01-02T00:00:00Z");
		Map<String, Object> current = feature("current", List.of("2026-01-01T00:00:00Z", "now"));
		Part1ApiCommonSupport.DatetimeQuery atBoundary = new Part1ApiCommonSupport.DatetimeQuery("2026-01-02T00:00:00Z",
				requestTime, requestTime);
		Part1ApiCommonSupport.DatetimeQuery future = new Part1ApiCommonSupport.DatetimeQuery("2026-01-03T00:00:00Z/..",
				Instant.parse("2026-01-03T00:00:00Z"), null);

		assertTrue(Part1ApiCommonSupport.validTimeIntersects(current, atBoundary, requestTime));
		assertFalse(Part1ApiCommonSupport.validTimeIntersects(current, future, requestTime));
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-DATETIME-001.
	 */
	@Test
	public void malformedValidTimeFailsClosed() {
		Map<String, Object> malformed = feature("bad", List.of("not-a-time", "2026-01-03T00:00:00Z"));
		Part1ApiCommonSupport.DatetimeQuery query = new Part1ApiCommonSupport.DatetimeQuery("2026-01-02T00:00:00Z",
				Instant.parse("2026-01-02T00:00:00Z"), Instant.parse("2026-01-02T00:00:00Z"));

		assertThrows(IllegalArgumentException.class, () -> Part1ApiCommonSupport.validTimeIntersects(malformed, query,
				Instant.parse("2026-01-02T00:00:00Z")));
		assertThrows(IllegalArgumentException.class, () -> Part1ApiCommonSupport
			.validTimeIntersects(feature("bad-now", "NOW"), query, Instant.parse("2026-01-02T00:00:00Z")));
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-RESOURCE-IDS-001.
	 */
	@Test
	public void duplicateResourceIdsFailWithinAType() {
		Map<String, List<Map<String, Object>>> resources = Map.of("systems",
				List.of(Map.of("id", "duplicate"), Map.of("id", "duplicate")));

		AssertionError error = assertThrows(AssertionError.class,
				() -> Part1ApiCommonTests.assertResourceIds(resources));

		assertTrue(error.getMessage().contains("duplicate local ID"));
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-RESOURCE-UIDS-001.
	 */
	@Test
	public void duplicateResourceUidsFailAcrossTypes() {
		Map<String, Object> system = Map.of("id", "system-1", "properties", Map.of("uid", "urn:ogc:duplicate"));
		Map<String, Object> procedure = Map.of("id", "procedure-1", "properties", Map.of("uid", "urn:ogc:duplicate"));
		Map<String, List<Map<String, Object>>> resources = Map.of("systems", List.of(system), "procedures",
				List.of(procedure));

		AssertionError error = assertThrows(AssertionError.class,
				() -> Part1ApiCommonTests.assertResourceUids(resources));

		assertTrue(error.getMessage().contains("is used by both"));
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-DATETIME-001.
	 */
	@Test
	public void datetimeValidationRejectsFalsePositivesAndMissingTimelessFeatures() {
		Instant requestTime = Instant.parse("2026-01-02T00:00:01Z");
		Part1ApiCommonSupport.DatetimeQuery query = new Part1ApiCommonSupport.DatetimeQuery("2026-01-02T00:00:00Z",
				Instant.parse("2026-01-02T00:00:00Z"), Instant.parse("2026-01-02T00:00:00Z"));
		Map<String, Object> collection = Map.of("id", "systems");
		Map<String, Object> timeless = Map.of("id", "timeless", "properties", Map.of());
		Map<String, Object> matching = feature("matching", List.of("2026-01-01T00:00:00Z", "2026-01-03T00:00:00Z"));
		Map<String, Object> outside = feature("outside", List.of("2027-01-01T00:00:00Z", "2027-01-03T00:00:00Z"));

		AssertionError falsePositive = assertThrows(AssertionError.class,
				() -> Part1ApiCommonTests.assertFilteredItems(collection, query, requestTime,
						List.of(timeless, matching), List.of(timeless, outside)));
		assertTrue(falsePositive.getMessage().contains("does not intersect"));

		AssertionError missingTimeless = assertThrows(AssertionError.class, () -> Part1ApiCommonTests
			.assertFilteredItems(collection, query, requestTime, List.of(timeless, matching), List.of(matching)));
		assertTrue(missingTimeless.getMessage().contains("omitted timeless feature"));

		Part1ApiCommonTests.assertFilteredItems(collection, query, requestTime, List.of(timeless, matching),
				List.of(timeless, matching));
	}

	private static Map<String, Object> feature(String id, Object validTime) {
		return Map.of("id", id, "properties", Map.of("validTime", validTime));
	}

	private static Response json(int status, String body) {
		return json(status, "application/json", body);
	}

	private static Response json(int status, String contentType, String body) {
		return new ResponseBuilder().setStatusCode(status).setContentType(contentType).setBody(body).build();
	}

}
