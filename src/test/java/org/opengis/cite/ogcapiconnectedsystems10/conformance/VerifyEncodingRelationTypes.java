package org.opengis.cite.ogcapiconnectedsystems10.conformance;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.testng.SkipException;

/**
 * Regression coverage for S-ETS-17-01 and S-ETS-18-01 relation-types helper behavior.
 *
 * <p>
 * Traceability: REQ-ETS-PART1-012, REQ-ETS-PART1-013,
 * SCENARIO-ETS-PART1-012-GEOJSON-RELATION-TYPES-001,
 * SCENARIO-ETS-PART1-013-SENSORML-RELATION-TYPES-001,
 * SCENARIO-ETS-PART1-012-GEOJSON-RELATION-TYPES-BREADTH-001,
 * SCENARIO-ETS-PART1-013-SENSORML-RELATION-TYPES-BREADTH-001, and
 * SCENARIO-ETS-PART1-012-013-RELATION-TYPES-FALLBACK-HONESTY-001.
 * </p>
 */
public class VerifyEncodingRelationTypes {

	private static final String REQ = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/relation-types";

	@Test
	public void geoJsonSystemAssociationRelPassesWhenResourceSpecific() {
		Map<String, Object> representation = Map.of("links", List.of(Map.of("rel", "canonical", "href", "/systems/1"),
				Map.of("rel", "samplingFeatures", "href", "/systems/1/samplingFeatures")));

		EncodingRelationTypes.assertLinksMemberAssociationRels(representation, EncodingRelationTypes.ENCODING_GEOJSON,
				EncodingRelationTypes.RESOURCE_SYSTEM, REQ);
	}

	@Test
	public void genericLinksOnlySkipWithoutVacuousPass() {
		Map<String, Object> representation = Map.of("links", List.of(Map.of("rel", "canonical", "href", "/systems/1"),
				Map.of("rel", "alternate", "href", "/systems/1?f=html")));

		SkipException error = assertThrows(SkipException.class,
				() -> EncodingRelationTypes.assertLinksMemberAssociationRels(representation,
						EncodingRelationTypes.ENCODING_GEOJSON, EncodingRelationTypes.RESOURCE_SYSTEM, REQ));

		assertTrue(error.getMessage().contains("no links-member association links"));
	}

	@Test
	public void wrongResourceAssociationRelFails() {
		Map<String, Object> representation = Map.of("links",
				List.of(Map.of("rel", "samplingFeatures", "href", "/procedures/1/samplingFeatures")));

		AssertionError error = assertThrows(AssertionError.class,
				() -> EncodingRelationTypes.assertLinksMemberAssociationRels(representation,
						EncodingRelationTypes.ENCODING_GEOJSON, EncodingRelationTypes.RESOURCE_PROCEDURE, REQ));

		assertTrue(error.getMessage().contains("samplingFeatures"));
		assertTrue(error.getMessage().contains(EncodingRelationTypes.RESOURCE_PROCEDURE));
	}

	@Test
	public void missingRelFailsRatherThanSkipping() {
		Map<String, Object> representation = Map.of("links", List.of(Map.of("href", "/systems/1/samplingFeatures")));

		AssertionError error = assertThrows(AssertionError.class,
				() -> EncodingRelationTypes.assertLinksMemberAssociationRels(representation,
						EncodingRelationTypes.ENCODING_GEOJSON, EncodingRelationTypes.RESOURCE_SYSTEM, REQ));

		assertTrue(error.getMessage().contains("no non-empty rel"));
	}

	@Test
	public void sensorMlSystemRejectsGeoJsonOnlyParentSystemRel() {
		Map<String, Object> representation = Map.of("links",
				List.of(Map.of("rel", "parentSystem", "href", "/systems/parent")));

		AssertionError error = assertThrows(AssertionError.class,
				() -> EncodingRelationTypes.assertLinksMemberAssociationRels(representation,
						EncodingRelationTypes.ENCODING_SENSORML, EncodingRelationTypes.RESOURCE_SYSTEM, REQ));

		assertTrue(error.getMessage().contains("parentSystem"));
		assertTrue(error.getMessage().contains(EncodingRelationTypes.ENCODING_SENSORML));
	}

	@Test
	public void systemPassDoesNotMaskDeploymentGenericOnlySkip() {
		Map<String, Object> system = Map.of("links",
				List.of(Map.of("rel", "samplingFeatures", "href", "/systems/1/samplingFeatures")));
		Map<String, Object> deployment = Map.of("links", List.of(Map.of("rel", "canonical", "href", "/deployments/1"),
				Map.of("rel", "alternate", "href", "/deployments/1?f=html")));

		EncodingRelationTypes.assertLinksMemberAssociationRels(system, EncodingRelationTypes.ENCODING_GEOJSON,
				EncodingRelationTypes.RESOURCE_SYSTEM, REQ);
		SkipException error = assertThrows(SkipException.class,
				() -> EncodingRelationTypes.assertLinksMemberAssociationRels(deployment,
						EncodingRelationTypes.ENCODING_GEOJSON, EncodingRelationTypes.RESOURCE_DEPLOYMENT, REQ));

		assertTrue(error.getMessage().contains(EncodingRelationTypes.RESOURCE_DEPLOYMENT));
		assertTrue(error.getMessage().contains("no links-member association links"));
	}

	@Test
	public void propertyLevelLinkDoesNotCreateLinksMemberEvidence() {
		Map<String, Object> representation = Map.of("properties",
				Map.of("deployedSystems@link", List.of(Map.of("href", "/systems/1"))));

		SkipException error = assertThrows(SkipException.class,
				() -> EncodingRelationTypes.assertLinksMemberAssociationRels(representation,
						EncodingRelationTypes.ENCODING_GEOJSON, EncodingRelationTypes.RESOURCE_DEPLOYMENT, REQ));

		assertTrue(error.getMessage().contains("no JSON links member"));
	}

	@Test
	public void sensorMlDeploymentAssociationRelPassesWhenResourceSpecific() {
		Map<String, Object> representation = Map.of("links",
				List.of(Map.of("rel", "parentDeployment", "href", "/deployments/parent")));

		EncodingRelationTypes.assertLinksMemberAssociationRels(representation, EncodingRelationTypes.ENCODING_SENSORML,
				EncodingRelationTypes.RESOURCE_DEPLOYMENT, REQ);
	}

}
