package org.opengis.cite.ogcapiconnectedsystems10.validation.sensorml;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Final-image probe for the first-party SensorML adapter and bundled schemas.
 */
public final class SensorMlValidatorRuntimeProbe {

	private SensorMlValidatorRuntimeProbe() {
	}

	/**
	 * Exercises valid and invalid adapter calls on the deployed TeamEngine classpath.
	 * @param args ignored
	 * @throws Exception if fixture parsing fails
	 */
	public static void main(String[] args) throws Exception {
		ObjectMapper json = new ObjectMapper();
		ConnectedSystemsSensorMlValidatorAdapter adapter = new ConnectedSystemsSensorMlValidatorAdapter();

		SensorMlValidationResult valid = adapter.validate(json.readTree("""
				{
				  "type": "PhysicalSystem",
				  "id": "system-1",
				  "uniqueId": "urn:example:system:1",
				  "label": "System",
				  "definition": "sosa:System"
				}
				"""), SensorMlSchema.SYSTEM);
		SensorMlValidationResult invalid = adapter.validate(json.readTree("{\"type\":\"PhysicalSystem\"}"),
				SensorMlSchema.SYSTEM);
		SensorMlValidationResult repeated = adapter.validate(json.readTree("{\"type\":\"PhysicalSystem\"}"),
				SensorMlSchema.SYSTEM);

		if (!valid.valid() || invalid.valid() || invalid.diagnostics().isEmpty()
				|| !invalid.diagnostics().equals(repeated.diagnostics())) {
			throw new IllegalStateException("SensorML deployed-runtime validation probe produced an unexpected result");
		}
		System.out.println("PASS: deployed SensorML adapter executed valid and invalid documents");
	}

}
