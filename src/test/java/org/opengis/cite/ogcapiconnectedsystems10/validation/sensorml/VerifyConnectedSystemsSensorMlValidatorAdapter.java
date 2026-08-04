package org.opengis.cite.ogcapiconnectedsystems10.validation.sensorml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Contract tests for the first-party backend-neutral SensorML adapter.
 */
public class VerifyConnectedSystemsSensorMlValidatorAdapter {

	private static final ObjectMapper JSON = new ObjectMapper();

	/**
	 * REQ-ETS-VALIDATOR-001; SCENARIO-ETS-PART1-013-VALIDATOR-ADAPTER-001.
	 */
	@Test
	public void allEightClosedTargetsValidateReleasedEntryShapes() throws Exception {
		ConnectedSystemsSensorMlValidatorAdapter adapter = new ConnectedSystemsSensorMlValidatorAdapter();

		for (Map.Entry<SensorMlSchema, JsonNode> entry : validCorpus().entrySet()) {
			assertTrue(entry.getKey().name(), adapter.validate(entry.getValue(), entry.getKey()).valid());
		}
	}

	/**
	 * REQ-ETS-VALIDATOR-001; SCENARIO-ETS-VALIDATOR-SENSORML-FIRST-PARTY-HARDENING-001.
	 */
	@Test
	public void validAndInvalidCorporaCoverEveryReleasedEntrySchema() throws Exception {
		EnumSet<SensorMlSchema> releasedTargets = EnumSet.allOf(SensorMlSchema.class);

		assertEquals(releasedTargets, validCorpus().keySet());
		assertEquals(releasedTargets, invalidCorpus().keySet());
	}

	/**
	 * REQ-ETS-VALIDATOR-001; SCENARIO-ETS-VALIDATOR-SENSORML-FIRST-PARTY-HARDENING-001.
	 */
	@Test
	public void allEightClosedTargetsRejectInvalidFixtureCorpusWithDiagnostics() throws Exception {
		ConnectedSystemsSensorMlValidatorAdapter adapter = new ConnectedSystemsSensorMlValidatorAdapter();

		for (Map.Entry<SensorMlSchema, JsonNode> entry : invalidCorpus().entrySet()) {
			SensorMlValidationResult result = adapter.validate(entry.getValue(), entry.getKey());
			assertFalse(entry.getKey().name(), result.valid());
			assertFalse(entry.getKey().name(), result.diagnostics().isEmpty());
			assertEquals(entry.getKey().name(), result.diagnostics().stream().sorted().toList(), result.diagnostics());
			assertTrue(entry.getKey().name(),
					result.diagnostics()
						.stream()
						.noneMatch(VerifyConnectedSystemsSensorMlValidatorAdapter::mentionsBackendType));
		}
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
	 * REQ-ETS-VALIDATOR-001; SCENARIO-ETS-VALIDATOR-SENSORML-FIRST-PARTY-HARDENING-001.
	 */
	@Test
	public void publicAdapterContractDoesNotExposeBackendOrTestNgTypes() {
		for (Method method : ConnectedSystemsSensorMlValidatorAdapter.class.getDeclaredMethods()) {
			assertFalse(method.getName(), mentionsBackendType(method.getReturnType().getName()));
			for (Class<?> parameterType : method.getParameterTypes()) {
				assertFalse(method.getName(), mentionsBackendType(parameterType.getName()));
			}
		}
	}

	/**
	 * REQ-ETS-VALIDATOR-001; SCENARIO-ETS-VALIDATOR-SENSORML-SOURCE-WATCH-001.
	 */
	@Test
	public void executableSensorMlSuiteJarIsNotVisibleAsValidatorDependency() {
		assertThrows(ClassNotFoundException.class, () -> Class.forName("org.opengis.cite.sensorml30.TestNGController"));
		assertThrows(ClassNotFoundException.class,
				() -> Class.forName("org.opengis.cite.sensorml30.BaseJsonSchemaValidatorTest"));
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

	/**
	 * REQ-ETS-VALIDATOR-001; SCENARIO-ETS-VALIDATOR-SENSORML-FIRST-PARTY-HARDENING-001.
	 */
	@Test
	public void nullBackendDiagnosticIsOperationalFailure() {
		SensorMlValidatorBackend backend = (document, schema) -> Arrays.asList("valid diagnostic", null);
		ConnectedSystemsSensorMlValidatorAdapter adapter = new ConnectedSystemsSensorMlValidatorAdapter(backend);

		IllegalStateException error = assertThrows(IllegalStateException.class,
				() -> adapter.validate(JSON.createObjectNode(), SensorMlSchema.SYSTEM));
		assertTrue(error.getMessage().contains("null diagnostic"));
	}

	private static Map<SensorMlSchema, JsonNode> validCorpus() throws Exception {
		JsonNode system = JSON.readTree(
				"{\"type\":\"PhysicalSystem\",\"id\":\"system-1\",\"label\":\"System\",\"uniqueId\":\"urn:example:system:1\",\"definition\":\"sosa:System\"}");
		JsonNode deployment = JSON.readTree(
				"{\"type\":\"Deployment\",\"id\":\"deployment-1\",\"label\":\"Deployment\",\"uniqueId\":\"urn:example:deployment:1\",\"definition\":\"sosa:Deployment\"}");
		JsonNode procedure = JSON.readTree(
				"{\"type\":\"SimpleProcess\",\"id\":\"procedure-1\",\"label\":\"Procedure\",\"uniqueId\":\"urn:example:procedure:1\",\"definition\":\"sosa:ObservingProcedure\"}");
		JsonNode property = JSON.readTree(
				"{\"id\":\"property-1\",\"label\":\"Property\",\"uniqueId\":\"urn:example:property:1\",\"baseProperty\":\"https://qudt.org/vocab/quantitykind/Temperature\"}");
		EnumMap<SensorMlSchema, JsonNode> corpus = new EnumMap<>(SensorMlSchema.class);
		corpus.put(SensorMlSchema.SYSTEM, system);
		corpus.put(SensorMlSchema.SYSTEM_COLLECTION, collectionOf(system));
		corpus.put(SensorMlSchema.DEPLOYMENT, deployment);
		corpus.put(SensorMlSchema.DEPLOYMENT_COLLECTION, collectionOf(deployment));
		corpus.put(SensorMlSchema.PROCEDURE, procedure);
		corpus.put(SensorMlSchema.PROCEDURE_COLLECTION, collectionOf(procedure));
		corpus.put(SensorMlSchema.PROPERTY, property);
		corpus.put(SensorMlSchema.PROPERTY_COLLECTION, collectionOf(property));
		return corpus;
	}

	private static Map<SensorMlSchema, JsonNode> invalidCorpus() throws Exception {
		JsonNode system = JSON.readTree("{\"type\":\"PhysicalSystem\",\"uniqueId\":\"not a uri\"}");
		JsonNode deployment = JSON.readTree("{\"type\":\"Deployment\",\"uniqueId\":\"not a uri\"}");
		JsonNode procedure = JSON.readTree("{\"type\":\"SimpleProcess\",\"uniqueId\":\"not a uri\"}");
		JsonNode property = JSON.readTree("{\"uniqueId\":\"not a uri\"}");
		EnumMap<SensorMlSchema, JsonNode> corpus = new EnumMap<>(SensorMlSchema.class);
		corpus.put(SensorMlSchema.SYSTEM, system);
		corpus.put(SensorMlSchema.SYSTEM_COLLECTION, collectionOf(system));
		corpus.put(SensorMlSchema.DEPLOYMENT, deployment);
		corpus.put(SensorMlSchema.DEPLOYMENT_COLLECTION, collectionOf(deployment));
		corpus.put(SensorMlSchema.PROCEDURE, procedure);
		corpus.put(SensorMlSchema.PROCEDURE_COLLECTION, collectionOf(procedure));
		corpus.put(SensorMlSchema.PROPERTY, property);
		corpus.put(SensorMlSchema.PROPERTY_COLLECTION, collectionOf(property));
		return corpus;
	}

	private static ObjectNode collectionOf(JsonNode item) {
		return JSON.createObjectNode().set("items", JSON.createArrayNode().add(item));
	}

	private static boolean mentionsBackendType(String value) {
		return value.contains("com.networknt") || value.contains("org.testng") || value.contains("org.junit")
				|| value.contains("ValidationMessage");
	}

}
