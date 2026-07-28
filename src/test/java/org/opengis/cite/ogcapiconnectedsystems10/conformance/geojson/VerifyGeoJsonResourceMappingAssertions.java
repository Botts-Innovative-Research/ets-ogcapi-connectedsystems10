package org.opengis.cite.ogcapiconnectedsystems10.conformance.geojson;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.geojson.GeoJsonSupport.ResourceType;

/**
 * Resource-specific regressions for the released GeoJSON mapping procedures.
 */
public class VerifyGeoJsonResourceMappingAssertions {

	private static final String REQUIREMENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/test";

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-MEDIA-GATE-001.
	 */
	@Test
	public void collectionSchemaRejectsHistoricalItemsFallback() {
		Map<String, Object> body = Map.of("items", List.of(Map.of("uid", "urn:example:deployment:1")));

		AssertionError error = assertThrows(AssertionError.class,
				() -> GeoJsonSupport.validateCollection(body, ResourceType.DEPLOYMENT, REQUIREMENT, "/deployments"));

		assertTrue(error.getMessage().contains(REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-RESOURCE-MAPPINGS-001.
	 */
	@Test
	public void optionalDeploymentAssociationsMayBeAbsent() {
		GeoJsonSupport.validateResourceMappings(deploymentFeature(), ResourceType.DEPLOYMENT, REQUIREMENT,
				"/deployments");
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-RESOURCE-MAPPINGS-001.
	 */
	@Test
	public void malformedPresentDeploymentAssociationFails() {
		Map<String, Object> feature = deploymentFeature();
		@SuppressWarnings("unchecked")
		Map<String, Object> properties = new LinkedHashMap<>((Map<String, Object>) feature.get("properties"));
		properties.put("deployedSystems@link", List.of(Map.of("href", "relative/system")));
		feature.put("properties", properties);

		assertThrows(AssertionError.class, () -> GeoJsonSupport.validateResourceMappings(feature,
				ResourceType.DEPLOYMENT, REQUIREMENT, "/deployments"));
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-SCHEMAS-001.
	 */
	@Test
	public void procedureSchemaRejectsMissingGeometryMember() {
		Map<String, Object> feature = Map.of("type", "Feature", "id", "p-1", "properties",
				Map.of("uid", "urn:example:procedure:1", "name", "Procedure", "featureType", "sosa:Procedure"));

		assertThrows(AssertionError.class,
				() -> GeoJsonSupport.validateSingle(feature, ResourceType.PROCEDURE, REQUIREMENT, "/procedures/p-1"));
	}

	private static Map<String, Object> deploymentFeature() {
		Map<String, Object> feature = new LinkedHashMap<>();
		feature.put("type", "Feature");
		feature.put("id", "d-1");
		feature.put("geometry", Map.of("type", "Point", "coordinates", List.of(1, 2)));
		feature.put("properties", Map.of("uid", "urn:example:deployment:1", "name", "Deployment", "featureType",
				"sosa:Deployment", "validTime", List.of("2026-01-01T00:00:00Z", "2027-01-01T00:00:00Z")));
		return feature;
	}

}
