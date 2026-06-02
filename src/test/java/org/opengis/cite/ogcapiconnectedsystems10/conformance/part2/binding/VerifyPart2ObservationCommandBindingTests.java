package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.testng.SkipException;

/**
 * Regression coverage for S-ETS-32-01 Part 2 Observation/Command binding closure.
 *
 * <p>
 * Traceability: REQ-ETS-PART2-013, SCENARIO-ETS-PART2-013-DYNAMIC-SEED-STATE-001,
 * SCENARIO-ETS-PART2-013-OBSERVATION-PARENT-SCHEMA-001,
 * SCENARIO-ETS-PART2-013-COMMAND-PARENT-SCHEMA-001,
 * SCENARIO-ETS-PART2-013-ENCODING-HONESTY-001, and
 * SCENARIO-ETS-PART2-013-MUTATION-SAFETY-001, and
 * SCENARIO-ETS-PART2-013-INLINE-STATUS-RESULT-REGRESSIONS-001.
 * </p>
 */
public class VerifyPart2ObservationCommandBindingTests {

	private static final String REQ_URI = Part2ObservationCommandBindingTests.REQ_ETS_PART2_013;

	@Test
	public void internalBindingIdentifiersDoNotAdvertiseStandaloneOgcConformanceClass() {
		String joined = String.join(" ", Part2ObservationCommandBindingTests.GROUP,
				Part2ObservationCommandBindingTests.REQ_DATASTREAM,
				Part2ObservationCommandBindingTests.REQ_CONTROLSTREAM,
				Part2ObservationCommandBindingTests.REQ_DATASTREAM_SCHEMA_OP,
				Part2ObservationCommandBindingTests.REQ_CONTROLSTREAM_SCHEMA_OP);

		assertEquals("part2binding", Part2ObservationCommandBindingTests.GROUP);
		assertTrue(joined.contains("ogcapi-connectedsystems-2/1.0/req/datastream"));
		assertTrue(joined.contains("ogcapi-connectedsystems-2/1.0/req/controlstream"));
		assertFalse(joined.contains("/conf/observation-binding"));
		assertTrue(Part2ObservationCommandBindingTests.isNonStandardObservationBindingConformance(
				Part2ObservationCommandBindingTests.NON_STANDARD_CONF_OBSERVATION_BINDING));
	}

	/**
	 * REQ-ETS-PART2-013. SCENARIO-ETS-PART2-013-SCHEMA-JSON-REQUEST-SHAPING-001.
	 */
	@Test
	public void parentSchemaRequestsAddJsonFormatWithoutLooseningMediaTypeGate() {
		assertEquals("datastreams/abc/schema?f=json",
				Part2ObservationCommandBindingTests.schemaJsonRequestPath("datastreams/abc/schema"));
		assertEquals("controlstreams/abc/schema?commandFormat=application/json&f=json",
				Part2ObservationCommandBindingTests
					.schemaJsonRequestPath("controlstreams/abc/schema?commandFormat=application/json"));
		assertEquals("datastreams/abc/schema?f=json",
				Part2ObservationCommandBindingTests.schemaJsonRequestPath("datastreams/abc/schema?f=json"));
		assertEquals("datastreams/abc/schema?format=json",
				Part2ObservationCommandBindingTests.schemaJsonRequestPath("datastreams/abc/schema?format=json"));
	}

	@Test
	public void jsonSchemaRequiredFieldsMatchChildBody() {
		Map<String, Object> schema = Map.of("resultSchema", Map.of("type", "object", "required", List.of("temperature"),
				"properties", Map.of("temperature", Map.of("type", "number"), "quality", Map.of("type", "string"))));
		Map<String, Object> child = Map.of("temperature", 21.5d, "quality", "good");

		assertTrue(Part2ObservationCommandBindingTests
			.bindingMismatches(child, schema, List.of("resultSchema"), "Observation result")
			.isEmpty());
	}

	@Test
	public void bindingMismatchesReportMissingRequiredParentSchemaField() {
		Map<String, Object> schema = Map.of("parametersSchema", Map.of("type", "object", "required", List.of("mode"),
				"properties", Map.of("mode", Map.of("type", "string"), "duration", Map.of("type", "integer"))));
		Map<String, Object> child = Map.of("duration", 10);

		List<String> mismatches = Part2ObservationCommandBindingTests.bindingMismatches(child, schema,
				List.of("parametersSchema"), "Command parameters");

		assertEquals(1, mismatches.size());
		assertTrue(mismatches.get(0).contains("missing required parent-schema field 'mode'"));
	}

	@Test
	public void bindingMismatchesReportPrimitiveTypeMismatch() {
		Map<String, Object> schema = Map.of("resultSchema", Map.of("type", "object", "required", List.of("count"),
				"properties", Map.of("count", Map.of("type", "integer"))));
		Map<String, Object> child = Map.of("count", "ten");

		List<String> mismatches = Part2ObservationCommandBindingTests.bindingMismatches(child, schema,
				List.of("resultSchema"), "Observation result");

		assertEquals(1, mismatches.size());
		assertTrue(mismatches.get(0).contains("incompatible with parent-schema type 'integer'"));
	}

	@Test
	public void bindingMismatchesDoNotPassWhenNoConcreteFieldOverlaps() {
		Map<String, Object> schema = Map.of("resultSchema",
				Map.of("type", "object", "properties", Map.of("temperature", Map.of("type", "number"))));
		Map<String, Object> child = Map.of("humidity", 55);

		List<String> mismatches = Part2ObservationCommandBindingTests.bindingMismatches(child, schema,
				List.of("resultSchema"), "Observation result");

		assertEquals(1, mismatches.size());
		assertTrue(mismatches.get(0).contains("does not contain any field named by the parent schema"));
	}

	@Test
	public void sweRecordFieldsAreInspectableBindingEvidence() {
		Map<String, Object> schema = Map.of("recordSchema",
				Map.of("type", "DataRecord", "fields",
						List.of(Map.of("name", "phenomenonTime", "component", Map.of("type", "Time")),
								Map.of("name", "temperature", "component", Map.of("type", "Quantity")))));

		List<Part2ObservationCommandBindingTests.BindingField> fields = Part2ObservationCommandBindingTests
			.schemaFields(schema, List.of("recordSchema"));

		assertEquals(2, fields.size());
		assertEquals("phenomenonTime", fields.get(0).name());
		assertEquals("Time", fields.get(0).type());
		assertTrue(fields.get(0).required());
		assertEquals("temperature", fields.get(1).name());
	}

	@Test
	public void primitiveTypeCompatibilityCoversJsonAndSweTypes() {
		assertTrue(Part2ObservationCommandBindingTests.isPrimitiveTypeCompatible("number", 12.3d));
		assertTrue(Part2ObservationCommandBindingTests.isPrimitiveTypeCompatible("Quantity", 12.3d));
		assertTrue(Part2ObservationCommandBindingTests.isPrimitiveTypeCompatible("integer", 12));
		assertFalse(Part2ObservationCommandBindingTests.isPrimitiveTypeCompatible("integer", 12.25d));
		assertTrue(Part2ObservationCommandBindingTests.isPrimitiveTypeCompatible("Time", "2026-06-01T00:00:00Z"));
		assertFalse(Part2ObservationCommandBindingTests.isPrimitiveTypeCompatible("boolean", "true"));
	}

	/**
	 * SCENARIO-ETS-PART2-013-INLINE-STATUS-RESULT-REGRESSIONS-001.
	 */
	@Test
	public void missingInlineCommandStatusAndResultMembersDoNotBlockParametersBinding() {
		Map<String, Object> schema = Map.of("statusSchema",
				Map.of("type", "object", "properties", Map.of("state", Map.of("type", "string"))), "resultSchema",
				Map.of("type", "object", "properties", Map.of("accepted", Map.of("type", "boolean"))));
		Map<String, Object> command = Map.of("parameters", Map.of("mode", "auto"));

		Part2ObservationCommandBindingTests.assertAvailableCommandInlineDataMatchesParentSchema(command, schema,
				REQ_URI);
	}

	/**
	 * SCENARIO-ETS-PART2-013-INLINE-STATUS-RESULT-REGRESSIONS-001.
	 */
	@Test
	public void nonObjectInlineCommandStatusSkipsInsteadOfPassing() {
		Map<String, Object> schema = Map.of("statusSchema",
				Map.of("type", "object", "properties", Map.of("state", Map.of("type", "string"))));
		Map<String, Object> command = Map.of("status", "accepted");

		SkipException skip = expectSkipException(() -> Part2ObservationCommandBindingTests
			.assertAvailableCommandInlineDataMatchesParentSchema(command, schema, REQ_URI));

		assertTrue(skip.getMessage().contains("is not an inspectable JSON object"));
	}

	/**
	 * SCENARIO-ETS-PART2-013-INLINE-STATUS-RESULT-REGRESSIONS-001.
	 */
	@Test
	public void inlineCommandStatusWithoutParentSchemaOverlapDoesNotPass() {
		Map<String, Object> schema = Map.of("statusSchema",
				Map.of("type", "object", "properties", Map.of("state", Map.of("type", "string"))));
		Map<String, Object> command = Map.of("status", Map.of("unexpected", "accepted"));

		AssertionError error = expectAssertionError(() -> Part2ObservationCommandBindingTests
			.assertAvailableCommandInlineDataMatchesParentSchema(command, schema, REQ_URI));

		assertTrue(error.getMessage().contains("does not contain any field named by the parent schema"));
	}

	/**
	 * SCENARIO-ETS-PART2-013-INLINE-STATUS-RESULT-REGRESSIONS-001.
	 */
	@Test
	public void inlineCommandStatusMissingRequiredParentFieldDoesNotPass() {
		Map<String, Object> schema = Map.of("statusSchema", Map.of("type", "object", "required", List.of("state"),
				"properties", Map.of("state", Map.of("type", "string"), "message", Map.of("type", "string"))));
		Map<String, Object> command = Map.of("commandStatus", Map.of("message", "queued"));

		AssertionError error = expectAssertionError(() -> Part2ObservationCommandBindingTests
			.assertAvailableCommandInlineDataMatchesParentSchema(command, schema, REQ_URI));

		assertTrue(error.getMessage().contains("missing required parent-schema field 'state'"));
	}

	/**
	 * SCENARIO-ETS-PART2-013-INLINE-STATUS-RESULT-REGRESSIONS-001.
	 */
	@Test
	public void inlineCommandResultPrimitiveTypeMismatchDoesNotPass() {
		Map<String, Object> schema = Map.of("resultSchema",
				Map.of("type", "object", "properties", Map.of("accepted", Map.of("type", "boolean"))));
		Map<String, Object> command = Map.of("commandResult", Map.of("accepted", "true"));

		AssertionError error = expectAssertionError(() -> Part2ObservationCommandBindingTests
			.assertAvailableCommandInlineDataMatchesParentSchema(command, schema, REQ_URI));

		assertTrue(error.getMessage().contains("incompatible with parent-schema type 'boolean'"));
	}

	/**
	 * SCENARIO-ETS-PART2-013-INLINE-STATUS-RESULT-REGRESSIONS-001.
	 */
	@Test
	public void inlineResultAliasPrimitiveTypeMismatchDoesNotPass() {
		Map<String, Object> schema = Map.of("resultSchema",
				Map.of("type", "object", "properties", Map.of("accepted", Map.of("type", "boolean"))));
		Map<String, Object> command = Map.of("result", Map.of("accepted", "true"));

		AssertionError error = expectAssertionError(() -> Part2ObservationCommandBindingTests
			.assertAvailableCommandInlineDataMatchesParentSchema(command, schema, REQ_URI));

		assertTrue(error.getMessage().contains("incompatible with parent-schema type 'boolean'"));
	}

	private static AssertionError expectAssertionError(Runnable action) {
		try {
			action.run();
		}
		catch (AssertionError error) {
			return error;
		}
		throw new AssertionError("Expected AssertionError.");
	}

	private static SkipException expectSkipException(Runnable action) {
		try {
			action.run();
		}
		catch (SkipException skip) {
			return skip;
		}
		throw new AssertionError("Expected SkipException.");
	}

}
