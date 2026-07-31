package org.opengis.cite.ogcapiconnectedsystems10.validation.sensorml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Contract tests for the provisional backend-neutral SensorML adapter.
 */
public class VerifyConnectedSystemsSensorMlValidatorAdapter {

	private static final ObjectMapper JSON = new ObjectMapper();

	/**
	 * REQ-ETS-VALIDATOR-001; SCENARIO-ETS-PART1-013-VALIDATOR-ADAPTER-001.
	 */
	@Test
	public void allEightClosedTargetsValidateReleasedEntryShapes() throws Exception {
		ConnectedSystemsSensorMlValidatorAdapter adapter = new ConnectedSystemsSensorMlValidatorAdapter();
		JsonNode system = JSON.readTree(
				"{\"type\":\"PhysicalSystem\",\"id\":\"system-1\",\"label\":\"System\",\"uniqueId\":\"urn:example:system:1\",\"definition\":\"sosa:System\"}");
		JsonNode deployment = JSON.readTree(
				"{\"type\":\"Deployment\",\"id\":\"deployment-1\",\"label\":\"Deployment\",\"uniqueId\":\"urn:example:deployment:1\",\"definition\":\"sosa:Deployment\"}");
		JsonNode procedure = JSON.readTree(
				"{\"type\":\"SimpleProcess\",\"id\":\"procedure-1\",\"label\":\"Procedure\",\"uniqueId\":\"urn:example:procedure:1\",\"definition\":\"sosa:ObservingProcedure\"}");
		JsonNode property = JSON.readTree(
				"{\"id\":\"property-1\",\"label\":\"Property\",\"uniqueId\":\"urn:example:property:1\",\"baseProperty\":\"https://qudt.org/vocab/quantitykind/Temperature\"}");

		assertTrue(adapter.validate(system, SensorMlSchema.SYSTEM).valid());
		assertTrue(adapter
			.validate(JSON.createObjectNode().set("items", JSON.createArrayNode().add(system)),
					SensorMlSchema.SYSTEM_COLLECTION)
			.valid());
		assertTrue(adapter.validate(deployment, SensorMlSchema.DEPLOYMENT).valid());
		assertTrue(adapter
			.validate(JSON.createObjectNode().set("items", JSON.createArrayNode().add(deployment)),
					SensorMlSchema.DEPLOYMENT_COLLECTION)
			.valid());
		assertTrue(adapter.validate(procedure, SensorMlSchema.PROCEDURE).valid());
		assertTrue(adapter
			.validate(JSON.createObjectNode().set("items", JSON.createArrayNode().add(procedure)),
					SensorMlSchema.PROCEDURE_COLLECTION)
			.valid());
		assertTrue(adapter.validate(property, SensorMlSchema.PROPERTY).valid());
		assertTrue(adapter
			.validate(JSON.createObjectNode().set("items", JSON.createArrayNode().add(property)),
					SensorMlSchema.PROPERTY_COLLECTION)
			.valid());
	}

	/**
	 * REQ-ETS-VALIDATOR-001; SCENARIO-ETS-PART1-013-VALIDATOR-ADAPTER-001.
	 */
	@Test
	public void diagnosticsAreImmutableDeterministicAndBackendNeutral() throws Exception {
		ConnectedSystemsSensorMlValidatorAdapter adapter = new ConnectedSystemsSensorMlValidatorAdapter();
		JsonNode invalid = JSON.readTree("{\"type\":\"PhysicalSystem\",\"uniqueId\":\"not a uri\"}");

		SensorMlValidationResult first = adapter.validate(invalid, SensorMlSchema.SYSTEM);
		SensorMlValidationResult second = adapter.validate(invalid, SensorMlSchema.SYSTEM);

		assertFalse(first.valid());
		assertEquals(first.diagnostics(), second.diagnostics());
		assertEquals(first.diagnostics().stream().sorted().toList(), first.diagnostics());
		assertThrows(UnsupportedOperationException.class, () -> first.diagnostics().add("mutable"));
		assertFalse(ConnectedSystemsSensorMlValidatorAdapter.class.getMethods()[0].getReturnType()
			.getName()
			.startsWith("com.networknt"));
		assertTrue(first.diagnostics().stream().noneMatch(value -> value.contains("ValidationMessage")));
	}

	/**
	 * REQ-ETS-VALIDATOR-001; SCENARIO-ETS-PART1-013-VALIDATOR-ADAPTER-001.
	 */
	@Test
	public void operationalFailuresDoNotBecomeIutDiagnostics() {
		SensorMlValidatorBackend broken = (document, schema) -> {
			throw new IllegalStateException("schema unavailable");
		};
		ConnectedSystemsSensorMlValidatorAdapter adapter = new ConnectedSystemsSensorMlValidatorAdapter(broken);

		IllegalStateException error = assertThrows(IllegalStateException.class,
				() -> adapter.validate(JSON.createObjectNode(), SensorMlSchema.SYSTEM));
		assertTrue(error.getMessage().contains("schema unavailable"));
	}

	/**
	 * REQ-ETS-VALIDATOR-001; SCENARIO-ETS-PART1-013-VALIDATOR-ADAPTER-001.
	 */
	@Test
	public void resultCanonicalizesBackendDiagnostics() {
		SensorMlValidatorBackend backend = (document, schema) -> List.of("z", "a", "z");
		ConnectedSystemsSensorMlValidatorAdapter adapter = new ConnectedSystemsSensorMlValidatorAdapter(backend);

		assertEquals(List.of("a", "z"), adapter.validate(JSON.createObjectNode(), SensorMlSchema.SYSTEM).diagnostics());
	}

}
