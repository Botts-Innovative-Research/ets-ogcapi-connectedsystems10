package org.opengis.cite.ogcapiconnectedsystems10.conformance.deployments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.PageDocument;

/**
 * Focused support checks for the released Deployment procedures.
 */
public class VerifyDeploymentFeaturesSupport {

	private static final String REQUIREMENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/deployment/collections";

	/**
	 * REQ-ETS-PART1-004; SCENARIO-ETS-PART1-004-RELEASED-COLLECTION-COMPLETE-001.
	 */
	@Test
	public void selectorUsesExactReleasedFeatureTypeAndMetadata() {
		Map<String, Object> exact = Map.of("id", "deployments", "itemType", "feature", "featureType",
				"sosa:Deployment");
		Map<String, Object> extension = Map.of("id", "all_deployments", "itemType", "feature", "featureType",
				"deployment");

		assertEquals(List.of(exact), DeploymentFeaturesSupport.selectDeploymentCollections(List.of(exact, extension)));
		assertThrows(AssertionError.class, () -> DeploymentFeaturesSupport.requireDeploymentCollectionMetadata(
				Map.of("id", "bad", "itemType", "record", "featureType", "sosa:Deployment"), REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-004; SCENARIO-ETS-PART1-004-RELEASED-CANONICAL-EQUIVALENCE-001.
	 */
	@Test
	public void canonicalLinkUsesExactDeploymentPathAndNormalizationRemovesOnlyCanonicalLinks() {
		URI page = URI.create("https://example.test/api/collections/deployments/items");
		URI root = URI.create("https://example.test/api/");
		Map<String, Object> item = Map.of("id", "deployment-1", "links",
				List.of(Map.of("rel", "canonical", "href", "../../deployments/deployment-1?f=json", "type",
						"application/geo+json"),
						Map.of("rel", "alternate", "href", "../../deployments/deployment-1?f=sml")));
		Map<String, Object> canonical = Map.of("id", "deployment-1", "links",
				List.of(Map.of("rel", "canonical", "href", "https://example.test/api/deployments/deployment-1?f=json"),
						Map.of("rel", "alternate", "href", "../../deployments/deployment-1?f=sml")));

		DeploymentFeaturesSupport.CanonicalLink link = DeploymentFeaturesSupport.canonicalLink(item, page, root,
				REQUIREMENT);
		assertEquals(URI.create("https://example.test/api/deployments/deployment-1?f=json"), link.uri());
		assertEquals("application/geo+json", link.mediaType());
		assertEquals(DeploymentFeaturesSupport.withoutCanonicalLinks(item),
				DeploymentFeaturesSupport.withoutCanonicalLinks(canonical));

		Map<String, Object> wrongPath = Map.of("id", "deployment-1", "links",
				List.of(Map.of("rel", "canonical", "href", "../../deployment/deployment-1")));
		assertThrows(AssertionError.class,
				() -> DeploymentFeaturesSupport.canonicalLink(wrongPath, page, root, REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-004; SCENARIO-ETS-PART1-004-RELEASED-CANONICAL-EQUIVALENCE-001.
	 */
	@Test
	public void canonicalRepresentationVariantsShareOneResourceIdentity() {
		URI page = URI.create("https://example.test/api/collections/deployments/items");
		URI root = URI.create("https://example.test/api/");
		Map<String, Object> variantLinks = Map.of("id", "deployment-1", "links",
				List.of(Map.of("rel", "canonical", "href", "../../deployments/deployment-1?f=json", "type",
						"application/geo+json"),
						Map.of("rel", "canonical", "href", "../../deployments/deployment-1?f=sml", "type",
								"application/sml+json"),
						Map.of("rel", "canonical", "href", "../../deployments/deployment-1?f=json", "type",
								"application/geo+json")));

		DeploymentFeaturesSupport.CanonicalLink selected = DeploymentFeaturesSupport.canonicalLink(variantLinks, page,
				root, REQUIREMENT);

		assertEquals(URI.create("https://example.test/api/deployments/deployment-1?f=json"), selected.uri());
		assertEquals("application/geo+json", selected.mediaType());

		Map<String, Object> conflictingPath = Map.of("id", "deployment-1", "links",
				List.of(Map.of("rel", "canonical", "href", "../../deployments/deployment-1"),
						Map.of("rel", "canonical", "href", "../../deployments/deployment-2")));
		assertThrows(AssertionError.class,
				() -> DeploymentFeaturesSupport.canonicalLink(conflictingPath, page, root, REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-004; SCENARIO-ETS-PART1-004-RELEASED-RESOURCES-ENDPOINT-001.
	 */
	@Test
	public void deploymentSchemasAcceptValidGeoJsonAndSensorMlAndRejectInvalidContent() {
		Map<String, Object> collectionLink = Map.of("rel", "self", "href", "https://example.test/api/deployments");
		Map<String, Object> geoJson = Map.of("type", "FeatureCollection", "features", List.of(geoDeployment()), "links",
				List.of(collectionLink));
		Map<String, Object> sensorMl = Map.of("items", List.of(sensorMlDeployment()), "links", List.of(collectionLink));
		Map<String, Object> invalidGeoJson = Map.of("type", "FeatureCollection", "features", List
			.of(Map.of("type", "Feature", "id", "deployment-1", "geometry", nullMapValue(), "properties",
					Map.of("uid", "urn:ogc:deployment:1", "name", "Deployment 1", "featureType", "sosa:Deployment"))));

		assertTrue(DeploymentFeaturesSupport.validateDeploymentEndpoint(
				URI.create("https://example.test/api/deployments"),
				List.of(page("application/geo+json", geoJson), page("application/sml+json", sensorMl)), REQUIREMENT));
		assertThrows(AssertionError.class,
				() -> DeploymentFeaturesSupport.validateDeploymentEndpoint(
						URI.create("https://example.test/api/deployments"),
						List.of(page("application/geo+json", invalidGeoJson)), REQUIREMENT));
		assertFalse(
				DeploymentFeaturesSupport.validateDeploymentEndpoint(URI.create("https://example.test/api/deployments"),
						List.of(page("application/json", Map.of("items", List.of()))), REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-004; SCENARIO-ETS-PART1-004-RELEASED-SYSTEM-REFERENCE-001.
	 */
	@Test
	public void systemReferenceMatchingIsRepresentationSpecificAndExact() {
		URI root = URI.create("https://example.test/api/");
		Map<String, Object> geoJson = geoDeployment();
		Map<String, Object> sensorMl = sensorMlDeployment();

		assertTrue(DeploymentFeaturesSupport.referencesSystem(geoJson, "application/geo+json", "system-1", root));
		assertTrue(DeploymentFeaturesSupport.referencesSystem(sensorMl, "application/sml+json", "system-1", root));
		assertFalse(DeploymentFeaturesSupport.referencesSystem(geoJson, "application/geo+json", "system", root));
		assertFalse(DeploymentFeaturesSupport.referencesSystem(sensorMl, "application/sml+json", "system-10", root));
		assertFalse(DeploymentFeaturesSupport.referencesSystem(
				Map.of("links", List.of(Map.of("href", root.resolve("systems/system-1").toString()))),
				"application/geo+json", "system-1", root));
	}

	private static PageDocument page(String mediaType, Map<String, Object> body) {
		return new PageDocument(URI.create("https://example.test/api/deployments"), mediaType, body, List.of());
	}

	private static Map<String, Object> geoDeployment() {
		Map<String, Object> feature = new LinkedHashMap<>();
		feature.put("type", "Feature");
		feature.put("id", "deployment-1");
		feature.put("geometry", null);
		feature.put("properties",
				Map.of("uid", "urn:ogc:deployment:1", "name", "Deployment 1", "featureType", "sosa:Deployment",
						"validTime", List.of("2026-01-01T00:00:00Z", "2026-12-31T00:00:00Z"), "deployedSystems@link",
						List.of(Map.of("href", "https://example.test/api/systems/system-1"))));
		feature.put("links",
				List.of(Map.of("rel", "canonical", "href", "https://example.test/api/deployments/deployment-1")));
		return feature;
	}

	private static Map<String, Object> sensorMlDeployment() {
		return Map.of("type", "Deployment", "id", "deployment-1", "label", "Deployment 1", "uniqueId",
				"urn:ogc:deployment:1", "definition", "sosa:Deployment", "deployedSystems",
				List.of(Map.of("name", "sensor", "system",
						Map.of("href", "https://example.test/api/systems/system-1"))),
				"links",
				List.of(Map.of("rel", "canonical", "href", "https://example.test/api/deployments/deployment-1")));
	}

	private static Object nullMapValue() {
		return new Object();
	}

}
