package org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.testng.SkipException;

/**
 * Regression coverage for S-ETS-16-01 resource-specific SensorML assertion helpers.
 *
 * <p>
 * Traceability: REQ-ETS-PART1-013,
 * SCENARIO-ETS-PART1-013-SENSORML-DEPLOYMENT-SCHEMA-MAPPING-001,
 * SCENARIO-ETS-PART1-013-SENSORML-PROCEDURE-SCHEMA-MAPPING-001,
 * SCENARIO-ETS-PART1-013-SENSORML-PROPERTY-SCHEMA-MAPPING-001, and
 * SCENARIO-ETS-PART1-013-SENSORML-NON-SYSTEM-FALLBACK-HONESTY-001.
 * </p>
 */
public class VerifySensorMlResourceMappingAssertions {

	@Test
	public void skipsEmptyCollectionWithoutVacuousPass() {
		Map<String, Object> body = Map.of("items", List.of());

		SkipException error = assertThrows(SkipException.class,
				() -> SensorMlTests.firstCollectionItem(body, "/properties", SensorMlTests.REQ_PROPERTY_SCHEMA));

		assertTrue(error.getMessage().contains("empty items array"));
		assertTrue(error.getMessage().contains(SensorMlTests.REQ_PROPERTY_SCHEMA));
	}

	@Test
	public void extractsFirstCollectionItem() {
		Map<String, Object> first = Map.of("type", "Feature", "id", "d-1", "properties",
				Map.of("uid", "urn:test:deployment"));
		Map<String, Object> body = Map.of("items", List.of(first));

		Map<String, Object> extracted = SensorMlTests.firstCollectionItem(body, "/deployments",
				SensorMlTests.REQ_DEPLOYMENT_SCHEMA);

		assertSame(first, extracted);
	}

	@Test
	public void procedureIdentifiersAloneDoNotSatisfySpecificStructure() {
		Map<String, Object> body = Map.of("type", "PhysicalSystem", "id", "p-1", "uniqueId", "urn:test:procedure",
				"identifiers", List.of(Map.of("name", "uid", "value", "urn:test:procedure")));

		assertFalse(SensorMlTests.hasProcedureSpecificStructure(body));
	}

	@Test
	public void procedureNonIdentityStructureSatisfiesSpecificPredicate() {
		assertTrue(SensorMlTests.hasProcedureSpecificStructure(
				Map.of("type", "PhysicalSystem", "inputs", Map.of("video", Map.of("type", "ObservableProperty")))));
		assertTrue(SensorMlTests.hasProcedureSpecificStructure(
				Map.of("type", "SimpleProcess", "capabilities", List.of(Map.of("name", "fieldOfView")))));
	}

	@Test
	public void propertyMappingRequiresPropertyCompatibleTypeAndEvidence() {
		Map<String, Object> property = Map.of("type", "DerivedProperty", "definition", "http://example.test/prop");
		Map<String, Object> genericSystem = Map.of("type", "PhysicalSystem", "definition", "http://example.test/sys");

		assertTrue(SensorMlTests.isPropertyCompatibleType(property));
		assertTrue(SensorMlTests.hasPropertyMappingEvidence(property));
		assertFalse(SensorMlTests.isPropertyCompatibleType(genericSystem));
	}

	@Test
	public void mappingValuesMustBeNonEmpty() {
		assertTrue(SensorMlTests.hasMappingValue(Map.of("deployedSystems", List.of(Map.of("name", "sys"))),
				"deployedSystems"));
		assertFalse(SensorMlTests.hasMappingValue(Map.of("deployedSystems", List.of()), "deployedSystems"));
		assertFalse(SensorMlTests.hasMappingValue(Map.of("definition", ""), "definition"));
	}

}
