package org.opengis.cite.ogcapiconnectedsystems10.validation.swecommon;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Final-image probe for shaded schema resources and relocated validator execution.
 */
public final class SweValidatorRuntimeProbe {

	private SweValidatorRuntimeProbe() {
	}

	/**
	 * Exercises valid and invalid adapter calls on the deployed TeamEngine classpath.
	 * @param args ignored
	 * @throws Exception if fixture parsing fails
	 */
	public static void main(String[] args) throws Exception {
		ObjectMapper json = new ObjectMapper();
		ConnectedSystemsSweValidatorAdapter adapter = new ConnectedSystemsSweValidatorAdapter();

		SweValidationResult valid = adapter.validateComponent(json.readTree("""
				{
				  "type": "Count",
				  "definition": "http://www.opengis.net/def/property/OGC/0/NumberOfPixels",
				  "label": "Row Size"
				}
				"""));
		SweValidationResult invalid = adapter.validateComponent(json.readTree("{\"type\":\"UnknownComponent\"}"));
		SweValidationResult repeated = adapter.validateComponent(json.readTree("{\"type\":\"UnknownComponent\"}"));

		if (!valid.isValid() || invalid.isValid() || invalid.diagnostics().isEmpty()
				|| !invalid.diagnostics().equals(repeated.diagnostics())) {
			throw new IllegalStateException(
					"SWE Common deployed-runtime validation probe produced an unexpected result");
		}
		System.out.println("PASS: deployed SWE Common adapter executed valid and invalid components");
	}

}
