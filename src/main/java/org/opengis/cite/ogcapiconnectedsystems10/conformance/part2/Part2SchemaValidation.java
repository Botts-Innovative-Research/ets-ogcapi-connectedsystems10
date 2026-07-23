package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2;

import java.util.stream.Collectors;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.validation.swecommon.ConnectedSystemsSweValidatorAdapter;
import org.opengis.cite.ogcapiconnectedsystems10.validation.swecommon.SweValidationResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaValidatorsConfig;

/**
 * Shared JSON Schema validation settings for CS API Part 2 bundled schemas.
 */
public final class Part2SchemaValidation {

	private static final ConnectedSystemsSweValidatorAdapter SWE_COMMON_VALIDATOR = new ConnectedSystemsSweValidatorAdapter();

	private Part2SchemaValidation() {
	}

	public static JsonSchema getSchema(JsonSchemaFactory factory, String schemaIri) {
		return factory.getSchema(SchemaLocation.of(schemaIri), schemaValidatorsConfig());
	}

	/**
	 * Validates the extracted SWE Common component after its Connected Systems wrapper
	 * has passed local schema validation.
	 * @param wrapperSchema Connected Systems Observation or Command schema document
	 * @return external SWE Common validation result
	 */
	public static SweValidationResult validateSweRecordSchema(JsonNode wrapperSchema) {
		JsonNode recordSchema = wrapperSchema.get("recordSchema");
		if (recordSchema == null || !recordSchema.isObject()) {
			throw new IllegalStateException(
					"Locally validated Connected Systems SWE schema does not contain an object recordSchema");
		}
		return SWE_COMMON_VALIDATOR.validateComponent(recordSchema);
	}

	/**
	 * Reports reusable-validator conformance diagnostics against the active Part 2
	 * requirement. Operational validator failures deliberately propagate as suite errors.
	 * @param wrapperSchema locally validated Connected Systems schema wrapper
	 * @param requirementUri active OGC 23-002 requirement URI
	 * @param source diagnostic label for the retrieved schema
	 */
	public static void assertValidSweRecordSchema(JsonNode wrapperSchema, String requirementUri, String source) {
		SweValidationResult result = validateSweRecordSchema(wrapperSchema);
		if (!result.isValid()) {
			String joined = result.diagnostics().stream().limit(8).collect(Collectors.joining("; "));
			ETSAssert.failWithUri(requirementUri,
					source + " recordSchema failed the reusable SWE Common validator: " + joined);
		}
	}

	static SchemaValidatorsConfig schemaValidatorsConfig() {
		SchemaValidatorsConfig config = new SchemaValidatorsConfig();
		config.setFormatAssertionsEnabled(true);
		return config;
	}

}
