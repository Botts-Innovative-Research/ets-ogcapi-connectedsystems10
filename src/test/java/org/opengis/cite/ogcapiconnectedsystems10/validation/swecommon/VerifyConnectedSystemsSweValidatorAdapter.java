package org.opengis.cite.ogcapiconnectedsystems10.validation.swecommon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.opengis.cite.swecommon30.validation.SweCommonJsonSchemaValidator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Verifies the ETS-owned boundary around the reusable SWE Common validator.
 */
public class VerifyConnectedSystemsSweValidatorAdapter {

	private static final ObjectMapper JSON = new ObjectMapper();

	private final ConnectedSystemsSweValidatorAdapter validator = new ConnectedSystemsSweValidatorAdapter();

	/**
	 * REQ-ETS-VALIDATOR-001; SCENARIO-ETS-VALIDATOR-SWE-COMMON-ADAPTER-001.
	 */
	@Test
	public void acceptsValidCountAndDataRecordComponents() throws Exception {
		SweValidationResult countResult = validator.validateComponent(JSON.readTree("""
				{
				  "type": "Count",
				  "definition": "http://www.opengis.net/def/property/OGC/0/NumberOfPixels",
				  "label": "Row Size",
				  "value": 1024
				}
				"""));
		SweValidationResult recordResult = validator.validateComponent(JSON.readTree("""
				{
				  "type": "DataRecord",
				  "label": "Weather Data Record",
				  "fields": [{
				    "name": "temperature",
				    "type": "Quantity",
				    "definition": "http://example.test/property/air-temperature",
				    "label": "Air Temperature",
				    "uom": { "code": "Cel" }
				  }]
				}
				"""));

		assertTrue(countResult.isValid());
		assertTrue(recordResult.isValid());
		assertTrue(countResult.diagnostics().isEmpty());
	}

	/**
	 * REQ-ETS-VALIDATOR-001; SCENARIO-ETS-VALIDATOR-DIAGNOSTICS-BOUNDARY-001.
	 */
	@Test
	public void returnsSortedImmutableDiagnosticsForInvalidComponent() throws Exception {
		JsonNode invalid = JSON.readTree("{\"type\":\"UnknownComponent\"}");

		SweValidationResult result = validator.validateComponent(invalid);

		assertFalse(result.isValid());
		assertFalse(result.diagnostics().isEmpty());
		List<String> sorted = new ArrayList<>(result.diagnostics());
		sorted.sort(String::compareTo);
		assertEquals(sorted, result.diagnostics());
		assertThrows(UnsupportedOperationException.class, () -> result.diagnostics().add("mutable"));
	}

	/**
	 * REQ-ETS-VALIDATOR-001; SCENARIO-ETS-VALIDATOR-DIAGNOSTICS-BOUNDARY-001.
	 */
	@Test
	public void publicApiDoesNotExposeNetworkntTypes() {
		assertNoNetworkntTypes(ConnectedSystemsSweValidatorAdapter.class.getMethods());
		assertNoNetworkntTypes(SweValidationResult.class.getMethods());
	}

	/**
	 * REQ-ETS-VALIDATOR-001; SCENARIO-ETS-VALIDATOR-DIAGNOSTICS-BOUNDARY-001,
	 * SCENARIO-ETS-VALIDATOR-SWE-COMMON-PARITY-CORPUS-001.
	 */
	@Test
	public void missingUpstreamSchemaIsAnOperationalError() throws Exception {
		ConnectedSystemsSweValidatorAdapter misconfigured = new ConnectedSystemsSweValidatorAdapter(
				new SweCommonJsonSchemaValidator(), "missing-schema.json");

		IllegalStateException error = assertThrows(IllegalStateException.class,
				() -> misconfigured.validateComponent(JSON.readTree("{\"type\":\"Count\"}")));
		assertTrue(error.getMessage().contains("missing-schema.json"));
	}

	private static void assertNoNetworkntTypes(Method[] methods) {
		Arrays.stream(methods).forEach(method -> {
			assertNotNetworknt(method, method.getReturnType());
			Arrays.stream(method.getParameterTypes()).forEach(type -> assertNotNetworknt(method, type));
		});
	}

	private static void assertNotNetworknt(Executable executable, Class<?> type) {
		assertFalse(executable + " exposes external NetworkNT type " + type.getName(),
				type.getName().startsWith("com.networknt."));
	}

}
