package org.opengis.cite.ogcapiconnectedsystems10.conformance.advancedfiltering;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Focused unit checks for Advanced Filtering predicate helpers.
 */
public class VerifyAdvancedFilteringSupport {

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-ID-LIST-001.
	 */
	@Test
	public void idListRejectsEmptyMixedAndMalformedValues() {
		assertTrue(AdvancedFilteringSupport.isValidIdList("system-1,system-2"));
		assertTrue(AdvancedFilteringSupport.isValidIdList("urn:example:system:1,urn:example:system:2"));
		assertTrue(AdvancedFilteringSupport.isValidIdList("urn:example:system:*"));
		assertFalse(AdvancedFilteringSupport.isValidIdList("*"));
		assertFalse(AdvancedFilteringSupport.isValidIdList(""));
		assertFalse(AdvancedFilteringSupport.isValidIdList(","));
		assertFalse(AdvancedFilteringSupport.isValidIdList("system-1,urn:example:system:1"));
		assertFalse(AdvancedFilteringSupport.isValidIdList("urn:example:bad value"));
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-COMMON-FILTERS-001.
	 */
	@Test
	public void resourcePredicatesReadGeoJsonAndSensorMlShapes() {
		Map<String, Object> geoJson = Map.of("id", "system-1", "properties",
				Map.of("uid", "urn:example:system:1", "name", "Weather Station", "customCode", "alpha"));
		Map<String, Object> sensorMl = Map.of("id", "property-1", "uniqueId", "urn:example:property:1", "label",
				"Air Temperature", "customCode", "alpha");

		assertTrue(AdvancedFilteringSupport.hasIdentifier(geoJson, "system-1"));
		assertTrue(AdvancedFilteringSupport.hasIdentifier(geoJson, "urn:example:system:1"));
		assertTrue(AdvancedFilteringSupport.containsPlainText(geoJson, "weather"));
		assertTrue(AdvancedFilteringSupport.hasPropertyValue(geoJson, "customCode", "alpha"));
		assertTrue(AdvancedFilteringSupport.hasIdentifier(sensorMl, "urn:example:property:1"));
		assertTrue(AdvancedFilteringSupport.containsPlainText(sensorMl, "temperature"));
		assertTrue(AdvancedFilteringSupport.hasPropertyValue(sensorMl, "customCode", "alpha"));
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-KEYWORD-SOURCE-001.
	 */
	@Test
	public void keywordPredicateIgnoresUnrelatedScalarExtensions() {
		Map<String, Object> unrelated = Map.of("id", "system-1", "properties",
				Map.of("uid", "urn:example:system:1", "customCode", "weather"));
		Map<String, Object> nestedLabels = Map.of("id", "system-2", "properties",
				Map.of("extensions", Map.of("label", "Weather Extension")), "links",
				List.of(Map.of("rel", "alternate", "label", "Weather Link")));
		Map<String, Object> described = Map.of("id", "system-2", "properties",
				Map.of("description", "Weather observations"));

		assertFalse(AdvancedFilteringSupport.containsPlainText(unrelated, "weather"));
		assertFalse(AdvancedFilteringSupport.containsPlainText(nestedLabels, "weather"));
		assertTrue(AdvancedFilteringSupport.containsPlainText(described, "weather"));
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-COMBINED-FILTERS-001.
	 */
	@Test
	public void setContainmentIsBasedOnStableResourceIdentifiers() {
		List<Map<String, Object>> subset = List.of(Map.of("id", "system-1"));
		List<Map<String, Object>> superset = List.of(Map.of("id", "system-2"), Map.of("id", "system-1"));

		assertTrue(AdvancedFilteringSupport.containsAllResources(superset, subset));
		assertFalse(AdvancedFilteringSupport.containsAllResources(subset, superset));
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-GEOMETRY-001.
	 */
	@Test
	public void geometryPredicateUsesIntersectionNotCollectionShape() {
		Map<String, Object> point = Map.of("type", "Point", "coordinates", List.of(-77.0, 38.0));

		assertTrue(AdvancedFilteringSupport.intersects(point, "POLYGON((-78 37,-76 37,-76 39,-78 39,-78 37))"));
		assertFalse(AdvancedFilteringSupport.intersects(point, "POLYGON((0 0,1 0,1 1,0 1,0 0))"));
	}

}
