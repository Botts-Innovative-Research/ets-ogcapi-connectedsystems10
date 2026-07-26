package org.opengis.cite.ogcapiconnectedsystems10.conformance.systemfeatures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport;

/**
 * Focused behavior checks for the six released System procedures.
 */
public class VerifySystemFeaturesSupport {

	private static final String REQUIREMENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/collections";

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-LOCATION-001.
	 */
	@Test
	public void representationMappingsExtractLocationAssetAndSystemType() {
		Map<String, Object> geoJson = Map.of("geometry", Map.of("type", "Point", "coordinates", List.of(1, 2)),
				"properties", Map.of("assetType", "Equipment", "featureType", "sosa:Sensor"));
		Map<String, Object> sensorMl = Map.of("position", Map.of("type", "Point", "coordinates", List.of(3, 4)),
				"definition", "http://www.w3.org/ns/sosa/Platform", "classifiers",
				List.of(Map.of("definition", "cs:AssetType", "value", "Process")));

		assertEquals("Equipment", SystemFeaturesSupport.assetType(geoJson, "application/geo+json").orElseThrow());
		assertEquals("sosa:Sensor", SystemFeaturesSupport.systemType(geoJson, "application/geo+json").orElseThrow());
		assertEquals("Point",
				SystemFeaturesSupport.location(geoJson, "application/geo+json").orElseThrow().path("type").asText());
		assertEquals("Process", SystemFeaturesSupport.assetType(sensorMl, "application/sml+json").orElseThrow());
		assertEquals("http://www.w3.org/ns/sosa/Platform",
				SystemFeaturesSupport.systemType(sensorMl, "application/sml+json").orElseThrow());
		assertEquals(3,
				SystemFeaturesSupport.location(sensorMl, "application/sml+json")
					.orElseThrow()
					.path("coordinates")
					.get(0)
					.asInt());
		assertTrue(SystemFeaturesSupport.isVirtualAsset("Simulation"));
		assertTrue(SystemFeaturesSupport.isVirtualAsset("Process"));
		assertFalse(SystemFeaturesSupport.isVirtualAsset("Equipment"));
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-LOCATION-TIME-001.
	 */
	@Test
	public void sensorMlPoseMovementUsesCoordinatesAndIgnoresOrientation() {
		Map<String, Object> position = Map.of("lat", 38.0, "lon", -77.0, "h", 120.0);
		Map<String, Object> first = Map.of("position", Map.of("type", "GeoPose", "position", position, "angles",
				Map.of("yaw", 0.0, "pitch", 0.0, "roll", 0.0)));
		Map<String, Object> orientationOnly = Map.of("position", Map.of("type", "GeoPose", "position", position,
				"angles", Map.of("yaw", 90.0, "pitch", 0.0, "roll", 0.0)));
		Map<String, Object> moved = Map.of("position",
				Map.of("type", "GeoPose", "position", Map.of("lat", 38.1, "lon", -77.0, "h", 120.0), "angles",
						Map.of("yaw", 90.0, "pitch", 0.0, "roll", 0.0)));

		assertEquals(SystemFeaturesSupport.location(first, "application/sml+json"),
				SystemFeaturesSupport.location(orientationOnly, "application/sml+json"));
		assertFalse(SystemFeaturesSupport.location(first, "application/sml+json")
			.equals(SystemFeaturesSupport.location(moved, "application/sml+json")));
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-CANONICAL-URL-001.
	 */
	@Test
	public void canonicalComparisonRemovesOnlyCanonicalLinks() {
		Map<String, Object> collectionItem = Map.of("id", "system-1", "links",
				List.of(Map.of("rel", "canonical", "href", "/api/systems/system-1"),
						Map.of("rel", "alternate", "href", "/api/systems/system-1?f=sml")));
		Map<String, Object> canonical = Map.of("id", "system-1", "links",
				List.of(Map.of("rel", "canonical", "href", "https://example.test/api/systems/system-1"),
						Map.of("rel", "alternate", "href", "/api/systems/system-1?f=sml")));

		assertEquals(SystemFeaturesSupport.withoutCanonicalLinks(collectionItem),
				SystemFeaturesSupport.withoutCanonicalLinks(canonical));
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-CANONICAL-URL-001.
	 */
	@Test
	public void canonicalUrlMustBeUniqueAndSameOrigin() {
		URI page = URI.create("https://example.test/api/collections/systems/items");
		URI root = URI.create("https://example.test/api/");
		Map<String, Object> valid = Map.of("links",
				List.of(Map.of("rel", "canonical", "href", "../../systems/system-1")));
		Map<String, Object> ambiguous = Map.of("links",
				List.of(Map.of("rel", "canonical", "href", "../../systems/system-1"),
						Map.of("rel", "canonical", "href", "../../systems/system-2")));
		Map<String, Object> crossOrigin = Map.of("links",
				List.of(Map.of("rel", "canonical", "href", "https://other.test/systems/system-1")));

		assertEquals(URI.create("https://example.test/api/systems/system-1"),
				SystemFeaturesSupport.canonicalUri(valid, page, root, REQUIREMENT));
		assertThrows(AssertionError.class,
				() -> SystemFeaturesSupport.canonicalUri(ambiguous, page, root, REQUIREMENT));
		assertThrows(AssertionError.class,
				() -> SystemFeaturesSupport.canonicalUri(crossOrigin, page, root, REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-COLLECTIONS-001.
	 */
	@Test
	public void releasedSystemTypeSetAcceptsOnlyPublishedUriAndCurieValues() {
		assertTrue(SystemFeaturesSupport.isAllowedSystemType("sosa:Sensor"));
		assertTrue(SystemFeaturesSupport.isAllowedSystemType("http://www.w3.org/ns/sosa/System"));
		assertFalse(SystemFeaturesSupport.isAllowedSystemType("Sensor"));
		assertFalse(SystemFeaturesSupport.isAllowedSystemType("sosa:Procedure"));
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-COLLECTIONS-001.
	 */
	@Test
	public void collectionSelectorUsesExactReleasedFeatureTypeAndAllowsEmptyResult() {
		Map<String, Object> exact = Map.of("id", "systems", "featureType", "sosa:System");
		Map<String, Object> oshExtension = Map.of("id", "all_systems", "featureType", "system");

		assertEquals(List.of(exact), SystemFeaturesSupport.selectSystemCollections(List.of(exact, oshExtension)));
		assertTrue(SystemFeaturesSupport.selectSystemCollections(List.of(oshExtension)).isEmpty());
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-RESOURCES-ENDPOINT-001.
	 */
	@Test
	public void systemCollectionSchemaAcceptsValidGeoJsonAndRejectsInvalidContent() {
		Map<String, Object> valid = Map.of("type", "FeatureCollection", "features", List
			.of(geoFeature(Map.of("uid", "urn:ogc:system:1", "name", "System 1", "featureType", "sosa:System"))));
		Map<String, Object> invalid = Map.of("type", "FeatureCollection", "features",
				List.of(geoFeature(Map.of("name", "System 1", "featureType", "not-a-system-type"))));

		assertTrue(SystemFeaturesSupport.validateSystemEndpoint(URI.create("https://example.test/api/systems"),
				List.of(page("application/geo+json", valid)), REQUIREMENT));
		assertThrows(AssertionError.class,
				() -> SystemFeaturesSupport.validateSystemEndpoint(URI.create("https://example.test/api/systems"),
						List.of(page("application/geo+json", invalid)), REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-SCHEMA-FAIL-CLOSED-001.
	 */
	@Test
	public void completeGeoJsonSchemasRejectMalformedWrapperFeatureAndGeometry() {
		Map<String, Object> missingWrapperMembers = Map.of();
		Map<String, Object> missingFeatureGeometry = Map.of("type", "FeatureCollection", "features", List.of(Map
			.of("type", "Feature", "properties", Map.of("uid", "urn:ogc:system:1", "featureType", "sosa:System"))));
		Map<String, Object> invalidPoint = Map.of("type", "FeatureCollection", "features",
				List.of(geoFeatureWithGeometry(Map.of("type", "Point", "coordinates", List.of("not-a-number", -77.0)),
						Map.of("uid", "urn:ogc:system:1", "featureType", "sosa:System"))));

		for (Map<String, Object> malformed : List.of(missingWrapperMembers, missingFeatureGeometry, invalidPoint)) {
			assertThrows(AssertionError.class,
					() -> SystemFeaturesSupport.validateSystemEndpoint(URI.create("https://example.test/api/systems"),
							List.of(page("application/geo+json", malformed)), REQUIREMENT));
		}
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-SCHEMA-FAIL-CLOSED-001.
	 */
	@Test
	public void sensorMlSystemCollectionSchemaHasPositiveAndNegativeCoverage() {
		Map<String, Object> validSystem = Map.of("type", "PhysicalSystem", "label", "System 1", "uniqueId",
				"urn:ogc:system:1", "definition", "http://www.w3.org/ns/sosa/System");
		Map<String, Object> valid = Map.of("items", List.of(validSystem));
		Map<String, Object> invalid = Map.of("items",
				List.of(Map.of("type", "PhysicalSystem", "definition", "http://www.w3.org/ns/sosa/System")));

		assertTrue(SystemFeaturesSupport.validateSystemEndpoint(URI.create("https://example.test/api/systems"),
				List.of(page("application/sml+json", valid)), REQUIREMENT));
		assertThrows(AssertionError.class,
				() -> SystemFeaturesSupport.validateSystemEndpoint(URI.create("https://example.test/api/systems"),
						List.of(page("application/sml+json", invalid)), REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-SCHEMA-FAIL-CLOSED-001.
	 * @throws Exception if a bundled schema or digest algorithm is unavailable.
	 */
	@Test
	public void vendoredGeoJsonSchemasMatchRecordedProvenance() throws Exception {
		String prefix = "/schemas/external/geojson.org/schema/";
		JsonNode provenance = new ObjectMapper().readTree(resource(prefix + "provenance.json"));
		JsonNode files = provenance.path("files");
		Set<String> names = new HashSet<>();
		files.fieldNames().forEachRemaining(names::add);
		assertEquals(Set.of("Feature.json", "FeatureCollection.json", "Geometry.json", "Point.json"),
				Set.copyOf(names));
		Iterator<Map.Entry<String, JsonNode>> entries = files.fields();
		while (entries.hasNext()) {
			Map.Entry<String, JsonNode> entry = entries.next();
			assertEquals(entry.getValue().asText(), sha256(resource(prefix + entry.getKey())));
		}
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-RESOURCES-ENDPOINT-001.
	 */
	@Test
	public void parameterizedEndpointReportsUnsupportedMediaWithoutPositiveEvidence() {
		assertFalse(SystemFeaturesSupport.validateSystemEndpoint(URI.create("https://example.test/api/systems"),
				List.of(page("application/json", Map.of("items", List.of()))), REQUIREMENT));
	}

	private static Part1ApiCommonSupport.PageDocument page(String mediaType, Map<String, Object> body) {
		return new Part1ApiCommonSupport.PageDocument(URI.create("https://example.test/api/systems"), mediaType, body,
				List.of());
	}

	private static Map<String, Object> geoFeature(Map<String, Object> properties) {
		return geoFeatureWithGeometry(null, properties);
	}

	private static Map<String, Object> geoFeatureWithGeometry(Object geometry, Map<String, Object> properties) {
		Map<String, Object> feature = new LinkedHashMap<>();
		feature.put("type", "Feature");
		feature.put("id", "system-1");
		feature.put("geometry", geometry);
		feature.put("properties", properties);
		return feature;
	}

	private static InputStream resource(String path) {
		InputStream stream = VerifySystemFeaturesSupport.class.getResourceAsStream(path);
		if (stream == null) {
			throw new AssertionError("Missing classpath resource " + path);
		}
		return stream;
	}

	private static String sha256(InputStream stream) throws IOException, NoSuchAlgorithmException {
		try (InputStream input = stream) {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(input.readAllBytes()));
		}
	}

}
