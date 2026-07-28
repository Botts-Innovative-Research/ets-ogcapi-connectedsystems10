package org.opengis.cite.ogcapiconnectedsystems10.conformance.geojson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.geojson.GeoJsonSupport.ApiDefinition;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.geojson.GeoJsonSupport.ResourceType;

/**
 * Unit checks for released GeoJSON API-definition, schema, and mapping behavior.
 */
public class VerifyGeoJsonSupport {

	private static final String REQUIREMENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/test";

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-MEDIATYPE-READ-001.
	 */
	@Test
	public void jsonAndYamlApiDefinitionsAdvertiseRequiredReadOperations() {
		ApiDefinition json = GeoJsonSupport.parseApiDefinition(apiDefinitionJson(),
				URI.create("https://example.test/openapi.json"), REQUIREMENT);
		ApiDefinition yaml = GeoJsonSupport.parseApiDefinition(apiDefinitionYaml(),
				URI.create("https://example.test/openapi.yaml"), REQUIREMENT);

		GeoJsonSupport.assertReadMediaAdvertisements(json, Set.of(ResourceType.SYSTEM), true, REQUIREMENT);
		GeoJsonSupport.assertReadMediaAdvertisements(yaml, Set.of(ResourceType.SYSTEM), true, REQUIREMENT);
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-MEDIATYPE-READ-001.
	 */
	@Test
	public void parseableDefinitionMissingRequiredReadMediaFails() {
		ApiDefinition definition = GeoJsonSupport.parseApiDefinition(
				apiDefinitionJson().replace("application/geo+json", "application/json"),
				URI.create("https://example.test/openapi.json"), REQUIREMENT);

		assertThrows(AssertionError.class, () -> GeoJsonSupport.assertReadMediaAdvertisements(definition,
				Set.of(ResourceType.SYSTEM), true, REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-MEDIATYPE-WRITE-001.
	 */
	@Test
	public void writeAdvertisementRequiresCanonicalPostOrPutRequestContent() {
		ApiDefinition valid = GeoJsonSupport.parseApiDefinition(apiDefinitionJson(),
				URI.create("https://example.test/openapi.json"), REQUIREMENT);
		ApiDefinition invalid = GeoJsonSupport.parseApiDefinition(
				apiDefinitionJson().replace("application/geo+json", "application/json"),
				URI.create("https://example.test/openapi.json"), REQUIREMENT);

		GeoJsonSupport.assertWriteMediaAdvertisement(valid, REQUIREMENT);
		assertThrows(AssertionError.class, () -> GeoJsonSupport.assertWriteMediaAdvertisement(invalid, REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-SCHEMAS-001.
	 */
	@Test
	public void completeReleasedSchemasAcceptValidAndRejectMalformedDocuments() {
		Map<String, Object> feature = systemFeature();
		Map<String, Object> collection = Map.of("type", "FeatureCollection", "features", List.of(feature));

		GeoJsonSupport.validateSingle(feature, ResourceType.SYSTEM, REQUIREMENT, "single");
		GeoJsonSupport.validateCollection(collection, ResourceType.SYSTEM, REQUIREMENT, "collection");
		assertThrows(AssertionError.class, () -> GeoJsonSupport.validateSingle(Map.of("type", "Feature"),
				ResourceType.SYSTEM, REQUIREMENT, "malformed single"));
		assertThrows(AssertionError.class, () -> GeoJsonSupport.validateCollection(Map.of("type", "FeatureCollection"),
				ResourceType.SYSTEM, REQUIREMENT, "malformed collection"));
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-FEATURE-MAPPING-001.
	 */
	@Test
	public void commonFeatureMappingRejectsInvalidUidAndLaterInvalidFeature() {
		GeoJsonSupport.validateCommonFeature(systemFeature(), REQUIREMENT, "feature 1");
		Map<String, Object> invalid = featureWithProperties(
				Map.of("uid", "not a uri", "name", "Invalid", "featureType", "sosa:System"));

		assertThrows(AssertionError.class, () -> List.of(systemFeature(), invalid)
			.forEach(feature -> GeoJsonSupport.validateCommonFeature(feature, REQUIREMENT, "feature")));
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-RESOURCE-MAPPINGS-001.
	 */
	@Test
	public void optionalResourceAssociationsAreNotRequiredButInvalidPresentValuesFail() {
		GeoJsonSupport.validateResourceMappings(systemFeature(), ResourceType.SYSTEM, REQUIREMENT, "system");
		Map<String, Object> invalid = featureWithProperties(Map.of("uid", "urn:example:system:1", "name", "System",
				"featureType", "sosa:System", "validTime", List.of("not-a-time", "2027-01-01T00:00:00Z")));

		assertThrows(AssertionError.class,
				() -> GeoJsonSupport.validateResourceMappings(invalid, ResourceType.SYSTEM, REQUIREMENT, "system"));
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-RELATION-TYPES-001.
	 */
	@Test
	public void relationInspectionCountsOnlyValidResourceSpecificAssociations() {
		Map<String, Object> valid = withLinks(systemFeature(),
				List.of(Map.of("rel", "canonical", "href", "https://example.test/systems/1"),
						Map.of("rel", "subsystems", "href", "https://example.test/systems/1/subsystems")));
		Map<String, Object> generic = withLinks(systemFeature(),
				List.of(Map.of("rel", "canonical", "href", "https://example.test/systems/1")));
		Map<String, Object> invalid = withLinks(systemFeature(),
				List.of(Map.of("rel", "implementingSystems", "href", "https://example.test/systems")));

		assertEquals(1, GeoJsonSupport.validateRelationTypes(valid, ResourceType.SYSTEM, REQUIREMENT, "system"));
		assertEquals(0, GeoJsonSupport.validateRelationTypes(generic, ResourceType.SYSTEM, REQUIREMENT, "system"));
		assertThrows(AssertionError.class,
				() -> GeoJsonSupport.validateRelationTypes(invalid, ResourceType.SYSTEM, REQUIREMENT, "system"));
	}

	private static String apiDefinitionJson() {
		return """
				{
				  "openapi":"3.0.3",
				  "info":{"title":"fixture","version":"1"},
				  "paths":{
				    "/systems":{
				      "get":{"responses":{"200":{"description":"ok","content":{"application/geo+json":{}}}}},
				      "post":{"requestBody":{"content":{"application/geo+json":{"schema":{"type":"object"}}}},
				              "responses":{"201":{"description":"created"}}}
				    },
				    "/collections/{collectionId}/items":{
				      "get":{"responses":{"200":{"description":"ok","content":{"application/geo+json":{}}}}}
				    }
				  }
				}
				""";
	}

	private static String apiDefinitionYaml() {
		return """
				openapi: 3.0.3
				info:
				  title: fixture
				  version: "1"
				paths:
				  /systems:
				    get:
				      responses:
				        "200":
				          description: ok
				          content:
				            application/geo+json: {}
				    post:
				      requestBody:
				        content:
				          application/geo+json:
				            schema:
				              type: object
				      responses:
				        "201":
				          description: created
				  /collections/{collectionId}/items:
				    get:
				      responses:
				        "200":
				          description: ok
				          content:
				            application/geo+json: {}
				""";
	}

	private static Map<String, Object> systemFeature() {
		return featureWithProperties(
				Map.of("uid", "urn:example:system:1", "name", "System", "featureType", "sosa:System"));
	}

	private static Map<String, Object> featureWithProperties(Map<String, Object> properties) {
		Map<String, Object> feature = new LinkedHashMap<>();
		feature.put("type", "Feature");
		feature.put("id", "system-1");
		feature.put("geometry", null);
		feature.put("properties", properties);
		return feature;
	}

	private static Map<String, Object> withLinks(Map<String, Object> feature, List<Map<String, Object>> links) {
		Map<String, Object> linked = new LinkedHashMap<>(feature);
		linked.put("links", links);
		return linked;
	}

}
