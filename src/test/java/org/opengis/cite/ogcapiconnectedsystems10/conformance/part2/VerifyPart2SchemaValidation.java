package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.Test;

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

	private Set<ValidationMessage> validateTimePeriod(String json) throws IOException {
		JsonSchema schema = Part2SchemaValidation.getSchema(SCHEMA_FACTORY, TIME_PERIOD_SCHEMA_IRI);
		JsonNode node = JSON.readTree(json);
		return schema.validate(node);
	}

}
