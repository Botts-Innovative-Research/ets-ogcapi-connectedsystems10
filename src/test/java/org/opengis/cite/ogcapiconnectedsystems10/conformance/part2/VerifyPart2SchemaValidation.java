package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.Test;
import org.opengis.cite.ogcapiconnectedsystems10.validation.swecommon.SweValidationResult;

/**
 * Regression coverage for Sprint 37 Part 2 shared schema validation settings.
 *
 * <p>
 * Traceability: REQ-ETS-PART2-013, SCENARIO-ETS-PART2-013-FORMAT-ASSERTION-NOW-001.
 * </p>
 */
public class VerifyPart2SchemaValidation {

	private static final ObjectMapper JSON = new ObjectMapper();

	private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory
		.getInstance(SpecVersion.VersionFlag.V202012, builder -> builder.schemaMappers(
				mappers -> mappers.mapPrefix("https://csapi-compliance.local/schemas/", "classpath:schemas/")));

	private static final String TIME_PERIOD_SCHEMA_IRI = "https://csapi-compliance.local/schemas/connected-systems-shared/common/timePeriod.json";

	private static final String OBSERVATION_SCHEMA_IRI = "https://csapi-compliance.local/schemas/connected-systems-2/json/observationSchemaSwe.json";

	private static final String COMMAND_SCHEMA_IRI = "https://csapi-compliance.local/schemas/connected-systems-2/json/commandSchemaSwe.json";

	private static final String JSON_ENCODING = "{\"type\":\"JSONEncoding\"}";

	private static final String TEXT_ENCODING = """
			{"type":"TextEncoding","tokenSeparator":",","blockSeparator":"\\n"}
			""";

	private static final String BINARY_ENCODING = """
			{
			  "type": "BinaryEncoding",
			  "byteOrder": "bigEndian",
			  "byteEncoding": "raw",
			  "members": [{
			    "type": "Component",
			    "dataType": "http://www.opengis.net/def/dataType/OGC/0/unsignedInt",
			    "ref": "/recordSchema"
			  }]
			}
			""";

	@Test
	public void timePeriodAcceptsLiteralNowWhenFormatAssertionsAreEnabled() throws IOException {
		// REQ-ETS-PART2-013 / SCENARIO-ETS-PART2-013-FORMAT-ASSERTION-NOW-001:
		// "now" must match only the literal branch of timeInstantOrNow, not the
		// date-time branch.
		Set<ValidationMessage> errors = validateTimePeriod("[\"2026-06-03T00:00:00Z\", \"now\"]");

		assertTrue(errors.toString(), errors.isEmpty());
	}

	@Test
	public void timePeriodRejectsMalformedDateTimeWhenFormatAssertionsAreEnabled() throws IOException {
		// REQ-ETS-PART2-013 / SCENARIO-ETS-PART2-013-FORMAT-ASSERTION-NOW-001:
		// malformed date-time strings must not pass as annotation-only format metadata.
		Set<ValidationMessage> errors = validateTimePeriod("[\"not-a-date-time\", \"now\"]");

		assertFalse(errors.isEmpty());
	}

	/**
	 * REQ-ETS-VALIDATOR-001; SCENARIO-ETS-VALIDATOR-SWE-COMMON-DUAL-VALIDATION-001.
	 */
	@Test
	public void extractedRecordSchemaUsesReusableSweCommonValidator() throws IOException {
		JsonNode wrapper = JSON.readTree("""
				{
				  "obsFormat": "application/swe+json",
				  "recordSchema": {
				    "type": "Count",
				    "definition": "http://www.opengis.net/def/property/OGC/0/NumberOfPixels",
				    "label": "Row Size"
				  },
				  "encoding": { "type": "JSONEncoding" }
				}
				""");

		SweValidationResult result = Part2SchemaValidation.validateSweRecordSchema(wrapper);

		assertTrue(result.diagnostics().toString(), result.isValid());
	}

	/**
	 * REQ-ETS-VALIDATOR-001; SCENARIO-ETS-VALIDATOR-SWE-COMMON-PARITY-CORPUS-001.
	 */
	@Test
	public void allSixSweWrappersPassLocalAndReusableValidation() throws IOException {
		List<WrapperCase> cases = List.of(
				new WrapperCase("JSON Observation", OBSERVATION_SCHEMA_IRI, "obsFormat", "application/swe+json",
						JSON_ENCODING),
				new WrapperCase("JSON Command", COMMAND_SCHEMA_IRI, "commandFormat", "application/swe+json",
						JSON_ENCODING),
				new WrapperCase("Text Observation", OBSERVATION_SCHEMA_IRI, "obsFormat", "application/swe+text",
						TEXT_ENCODING),
				new WrapperCase("Text Command", COMMAND_SCHEMA_IRI, "commandFormat", "application/swe+text",
						TEXT_ENCODING),
				new WrapperCase("Binary Observation", OBSERVATION_SCHEMA_IRI, "obsFormat", "application/swe+binary",
						BINARY_ENCODING),
				new WrapperCase("Binary Command", COMMAND_SCHEMA_IRI, "commandFormat", "application/swe+binary",
						BINARY_ENCODING));

		for (WrapperCase fixture : cases) {
			JsonNode wrapper = wrapper(fixture);
			Set<ValidationMessage> localErrors = Part2SchemaValidation.getSchema(SCHEMA_FACTORY, fixture.schemaIri())
				.validate(wrapper);
			assertTrue(fixture.label() + " local validation errors: " + localErrors, localErrors.isEmpty());

			SweValidationResult reusableResult = Part2SchemaValidation.validateSweRecordSchema(wrapper);
			assertTrue(fixture.label() + " reusable validation errors: " + reusableResult.diagnostics(),
					reusableResult.isValid());
		}
	}

	/**
	 * REQ-ETS-VALIDATOR-001; SCENARIO-ETS-VALIDATOR-SWE-COMMON-PARITY-CORPUS-001.
	 */
	@Test
	public void reusableFailureIncludesActiveRequirementUri() throws IOException {
		String requirementUri = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/swecommon-json/obsschema-schema";
		JsonNode wrapper = wrapper(
				new WrapperCase("invalid", OBSERVATION_SCHEMA_IRI, "obsFormat", "application/swe+json", JSON_ENCODING));
		((com.fasterxml.jackson.databind.node.ObjectNode) wrapper).set("recordSchema",
				JSON.readTree("{\"type\":\"UnknownComponent\"}"));

		AssertionError error = assertThrows(AssertionError.class,
				() -> Part2SchemaValidation.assertValidSweRecordSchema(wrapper, requirementUri, "Observation Schema"));

		assertTrue(error.getMessage(), error.getMessage().startsWith(requirementUri));
		assertTrue(error.getMessage(), error.getMessage().contains("reusable SWE Common validator"));
	}

	private Set<ValidationMessage> validateTimePeriod(String json) throws IOException {
		JsonSchema schema = Part2SchemaValidation.getSchema(SCHEMA_FACTORY, TIME_PERIOD_SCHEMA_IRI);
		JsonNode node = JSON.readTree(json);
		return schema.validate(node);
	}

	private JsonNode wrapper(WrapperCase fixture) throws IOException {
		com.fasterxml.jackson.databind.node.ObjectNode wrapper = JSON.createObjectNode();
		wrapper.put(fixture.formatMember(), fixture.mediaType());
		wrapper.set("recordSchema", JSON.readTree("""
				{
				  "type": "Count",
				  "definition": "http://www.opengis.net/def/property/OGC/0/NumberOfPixels",
				  "label": "Row Size"
				}
				"""));
		wrapper.set("encoding", JSON.readTree(fixture.encodingJson()));
		return wrapper;
	}

	private record WrapperCase(String label, String schemaIri, String formatMember, String mediaType,
			String encodingJson) {
	}

}
