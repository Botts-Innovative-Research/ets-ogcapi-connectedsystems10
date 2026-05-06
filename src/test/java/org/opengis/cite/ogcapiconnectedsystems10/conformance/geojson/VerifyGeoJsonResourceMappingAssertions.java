package org.opengis.cite.ogcapiconnectedsystems10.conformance.geojson;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.testng.SkipException;

/**
 * Regression coverage for S-ETS-15-01 resource-specific GeoJSON assertion helpers.
 *
 * <p>
 * Traceability: REQ-ETS-PART1-012,
 * SCENARIO-ETS-PART1-012-GEOJSON-DEPLOYMENT-SCHEMA-MAPPING-001,
 * SCENARIO-ETS-PART1-012-GEOJSON-PROCEDURE-SCHEMA-MAPPING-001,
 * SCENARIO-ETS-PART1-012-GEOJSON-SF-SCHEMA-MAPPING-001, and
 * SCENARIO-ETS-PART1-012-GEOJSON-NON-SYSTEM-FALLBACK-HONESTY-001.
 * </p>
 */
public class VerifyGeoJsonResourceMappingAssertions {

	@Test
	public void skipsItemsWrapperWithoutFeatures() {
		Map<String, Object> body = Map.of("items", List.of(Map.of("uid", "d-1")));

		SkipException error = assertThrows(SkipException.class,
				() -> GeoJsonTests.firstGeoJsonFeature(body, "/deployments", GeoJsonTests.REQ_DEPLOYMENT_SCHEMA));

		assertTrue(error.getMessage().contains("items"));
		assertTrue(error.getMessage().contains("not GeoJSON 'features'"));
	}

	@Test
	public void extractsFirstGeoJsonFeature() {
		Map<String, Object> first = Map.of("type", "Feature", "id", "d-1", "geometry", Map.of("type", "Point"),
				"properties", Map.of("uid", "d-1"));
		Map<String, Object> body = Map.of("type", "FeatureCollection", "features", List.of(first));

		Map<String, Object> extracted = GeoJsonTests.firstGeoJsonFeature(body, "/deployments",
				GeoJsonTests.REQ_DEPLOYMENT_SCHEMA);

		assertSame(first, extracted);
	}

	@Test
	public void rejectsGenericFeatureWithoutMappingValue() {
		Map<String, Object> properties = Map.of("uid", "d-1");

		assertFalse(GeoJsonTests.hasMappingValue(properties, "deployedSystems@link"));
	}

	@Test
	public void acceptsNonEmptyMappingValues() {
		assertTrue(GeoJsonTests.hasMappingValue(Map.of("deployedSystems@link", List.of(Map.of("href", "/systems/1"))),
				"deployedSystems@link"));
		assertTrue(GeoJsonTests.hasMappingValue(Map.of("radius", 12), "radius"));
		assertFalse(GeoJsonTests.hasMappingValue(Map.of("hostedProcedure@link", List.of()), "hostedProcedure@link"));
	}

	@Test
	public void rejectsFeatureShapeWithoutGeometryMember() {
		Map<String, Object> feature = Map.of("type", "Feature", "id", "p-1", "properties",
				Map.of("uid", "p-1", "featureType", "procedure"));

		AssertionError error = assertThrows(AssertionError.class, () -> GeoJsonTests.assertGeoJsonFeatureShape(feature,
				"/procedures", GeoJsonTests.REQ_PROCEDURE_SCHEMA));

		assertTrue(error.getMessage().contains(GeoJsonTests.REQ_PROCEDURE_SCHEMA));
		assertTrue(error.getMessage().contains("geometry"));
	}

}
