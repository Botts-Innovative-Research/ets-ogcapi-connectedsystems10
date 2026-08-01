package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.annotations.Test;

/**
 * Unit checks for the Sprint 65 Part 2 JSON Encoding released ATS closure.
 */
public class VerifyPart2JsonTests {

	private static final String REQ_BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/json/";

	private static final Set<String> RELEASED_TARGETS = Set.of(REQ_BASE + "mediatype-read",
			REQ_BASE + "mediatype-write", REQ_BASE + "datastream-schema", REQ_BASE + "obsschema-schema",
			REQ_BASE + "observation-schema", REQ_BASE + "observation-constraints", REQ_BASE + "controlstream-schema",
			REQ_BASE + "commandschema-schema", REQ_BASE + "command-schema", REQ_BASE + "command-constraints",
			REQ_BASE + "commandstatus-schema", REQ_BASE + "commandresult-schema",
			REQ_BASE + "commandresult-constraints", REQ_BASE + "systemevent-schema");

	@org.junit.Test
	public void releasedSuiteExposesExactlyOneTestMethodPerAnnexA9Target() {
		List<Method> methods = releasedMethods();
		assertEquals(
				"SCENARIO-ETS-PART2-009-RELEASED-PROCEDURES-001: Part 2 JSON must expose exactly the fourteen released Annex A.9 procedures.",
				RELEASED_TARGETS.size(), methods.size());

		Set<String> covered = new HashSet<>();
		for (Method method : methods) {
			Test ann = method.getAnnotation(Test.class);
			List<String> matched = RELEASED_TARGETS.stream()
				.filter(target -> containsCanonicalTarget(ann.description(), target))
				.toList();
			assertEquals("Each Part 2 JSON method must cite exactly one released target: " + method.getName() + " -> "
					+ matched, 1, matched.size());
			covered.add(matched.get(0));
		}
		assertEquals("Part 2 JSON released targets are not covered exactly once.", RELEASED_TARGETS, covered);
	}

	@org.junit.Test
	public void releasedSuiteContainsNoStandaloneDeclarationPrerequisiteOrConditionProcedures() {
		Set<String> methodNames = releasedMethods().stream().map(Method::getName).collect(Collectors.toSet());

		assertFalse("Declaration is a setup gate, not a released Annex A.9 procedure.",
				methodNames.contains("part2JsonConformanceDeclared"));
		assertFalse("SWE prerequisite visibility is a setup gate, not a released Annex A.9 procedure.",
				methodNames.contains("sweJsonRecordComponentsPrerequisiteVisibleForFullClosure"));
		assertFalse("Resource condition gates are per-procedure gates, not standalone Annex A.9 procedures.",
				methodNames.contains("jsonResourceConditionGatesAreVisible"));
		assertTrue("Released mediatype-read procedure is missing.", containsTarget(REQ_BASE + "mediatype-read"));
	}

	@org.junit.Test
	public void everyReleasedMethodTracesSprint65ScenarioAndRequirement() {
		for (Method method : releasedMethods()) {
			Test ann = method.getAnnotation(Test.class);
			String description = ann.description();
			assertTrue(method.getName() + " missing REQ-ETS-PART2-009 trace",
					description.contains("REQ-ETS-PART2-009"));
			assertTrue(method.getName() + " missing Sprint 65 released-procedure scenario trace",
					description.contains("SCENARIO-ETS-PART2-009-RELEASED-PROCEDURES-001"));
			assertTrue(method.getName() + " should carry part2json group",
					Arrays.asList(ann.groups()).contains(Part2JsonTests.GROUP));
		}
	}

	@org.junit.Test
	public void officialPart2JsonIdentifiersAreExposed() {
		String joined = String.join(" ", Part2JsonTests.CONF_JSON, Part2JsonTests.CONF_SWE_JSON_RECORD_COMPONENTS,
				Part2JsonTests.REQ_JSON, Part2JsonTests.REQ_MEDIATYPE_READ, Part2JsonTests.REQ_MEDIATYPE_WRITE,
				Part2JsonTests.REQ_DATASTREAM_SCHEMA, Part2JsonTests.REQ_COMMANDRESULT_CONSTRAINTS);

		assertTrue(joined.contains("ogcapi-connectedsystems-2/1.0/conf/json"));
		assertTrue(joined.contains("ogcapi-connectedsystems-2/1.0/req/json"));
		assertTrue(joined.contains("SWE/3.0/conf/json-record-components"));
		assertFalse(joined.contains("ogcapi-connectedsystems-1/1.0/conf/json"));
		assertFalse(joined.contains("req/json-2"));
	}

	@org.junit.Test
	public void jsonResourceConditionGateMatrixReportsMissingClasses() {
		Map<String, Object> jsonOnly = Map.of("conformsTo", List.of(Part2JsonTests.CONF_JSON));

		List<String> missing = Part2JsonSupport.missingConditionClasses(jsonOnly);

		assertEquals(3, missing.size());
		assertTrue(missing.contains(Part2JsonSupport.missingConditionMessage(Part2JsonTests.CONF_DATASTREAM,
				"Requirements 95-98 DataStream/Observation JSON")));
		assertTrue(missing.contains(Part2JsonSupport.missingConditionMessage(Part2JsonTests.CONF_CONTROLSTREAM,
				"Requirements 99-105 ControlStream/Command JSON")));
		assertTrue(missing.contains(Part2JsonSupport.missingConditionMessage(Part2JsonTests.CONF_SYSTEM_EVENT,
				"Requirement 106 SystemEvent JSON")));
	}

	@org.junit.Test
	public void exactApplicationJsonContentTypeIsRequiredForJsonResources() {
		assertTrue(Part2JsonSupport.isApplicationJsonContentType("application/json"));
		assertTrue(Part2JsonSupport.isApplicationJsonContentType("application/json; charset=utf-8"));
		assertFalse("Annex A.9 calls for application/json, not OpenAPI vendor JSON.",
				Part2JsonSupport.isApplicationJsonContentType("application/vnd.oai.openapi+json"));
		assertFalse(Part2JsonSupport.isApplicationJsonContentType("auto"));
		assertFalse(Part2JsonSupport.isApplicationJsonContentType("text/html"));
		assertFalse(Part2JsonSupport.isApplicationJsonContentType(null));
	}

	@org.junit.Test
	public void allAnnexA9SchemaResourcesAreBundled() {
		for (String schemaFile : Part2JsonSupport.ANNEX_A9_SCHEMA_FILES) {
			assertTrue("Missing schema resource " + schemaFile, Part2JsonSupport.schemaResourceExists(schemaFile));
			assertTrue(Part2JsonSupport.schemaIri(schemaFile)
				.startsWith("https://csapi-compliance.local/schemas/connected-systems-2/json/"));
		}
	}

	@org.junit.Test
	public void apiDefinitionWriteAdvertisementRequiresPostOrPutApplicationJsonRequestBody() {
		Map<String, Object> apiDefinition = Map.of("paths", Map.of("/systems/{systemId}/datastreams", Map.of("post",
				Map.of("requestBody", Map.of("content", Map.of("application/json", Map.of("schema", Map.of())))))));
		Map<String, List<String>> expected = Map.of("DataStream resources",
				List.of("/datastreams", "/systems/{sysId}/datastreams"));

		assertTrue(Part2JsonSupport.missingJsonWriteAdvertisements(apiDefinition, expected).isEmpty());
	}

	@org.junit.Test
	public void apiDefinitionDoesNotPassFromOptionsOnlyOrOtherJsonMediaTypes() {
		Map<String, List<String>> expected = Map.of("DataStream resources",
				List.of("/datastreams", "/systems/{sysId}/datastreams"));
		Map<String, Object> optionsOnly = Map.of("paths", Map.of("/systems/{systemId}/datastreams",
				Map.of("options", Map.of("requestBody", Map.of("content", Map.of("application/json", Map.of()))))));

		assertFalse(Part2JsonSupport.missingJsonWriteAdvertisements(optionsOnly, expected).isEmpty());

		Map<String, Object> geoJsonOnly = Map.of("paths", Map.of("/systems/{systemId}/datastreams",
				Map.of("post", Map.of("requestBody", Map.of("content", Map.of("application/geo+json", Map.of()))))));
		assertFalse(Part2JsonSupport.missingJsonWriteAdvertisements(geoJsonOnly, expected).isEmpty());

		Map<String, Object> parameterizedJson = Map.of("paths", Map.of("/datastreams/{id}", Map.of("put",
				Map.of("requestBody", Map.of("content", Map.of("application/json; charset=utf-8", Map.of()))))));
		assertFalse(Part2JsonSupport.missingJsonWriteAdvertisements(parameterizedJson, expected).isEmpty());
	}

	@org.junit.Test
	public void apiDefinitionWriteAdvertisementMustBeScopedToExpectedResourceEndpoints() {
		Map<String, List<String>> expected = Map.of("Command resources",
				List.of("/controlstreams/{controlStreamId}/commands",
						"/controlstreams/{controlStreamId}/commands/{commandId}"));
		Map<String, Object> unrelatedJsonWrite = Map.of("paths", Map.of("/systems", Map.of("post",
				Map.of("requestBody", Map.of("content", Map.of("application/json", Map.of("schema", Map.of())))))));
		assertEquals(List.of("Command resources (no scoped POST/PUT operation advertised)"),
				Part2JsonSupport.missingJsonWriteAdvertisements(unrelatedJsonWrite, expected));

		Map<String, Object> commandReplace = Map
			.of("paths", Map.of("/controlstreams/{streamId}/commands/{cmdId}", Map.of("put",
					Map.of("requestBody", Map.of("content", Map.of("application/json", Map.of("schema", Map.of())))))));
		assertTrue(Part2JsonSupport.missingJsonWriteAdvertisements(commandReplace, expected).isEmpty());
	}

	@org.junit.Test
	public void apiDefinitionWriteAdvertisementRequiresEveryScopedWriteOperationToAdvertiseJson() {
		Map<String, List<String>> expected = Map.of("DataStream resources",
				List.of("/datastreams", "/datastreams/{datastreamId}"));
		Map<String, Object> mixedOperations = Map.of("paths",
				Map.of("/datastreams",
						Map.of("post",
								Map.of("requestBody",
										Map.of("content", Map.of("application/json", Map.of("schema", Map.of()))))),
						"/datastreams/{id}", Map.of("put", Map.of("requestBody",
								Map.of("content", Map.of("application/xml", Map.of("schema", Map.of())))))));

		assertEquals(List.of("DataStream resources PUT /datastreams/{id}"),
				Part2JsonSupport.missingJsonWriteAdvertisements(mixedOperations, expected));
	}

	@org.junit.Test
	public void schemaConstraintValidationDetectsMismatchedCandidateValues() {
		Map<String, Object> schema = Map.of("type", "object", "required", List.of("temperature"), "properties",
				Map.of("temperature", Map.of("type", "number")));

		List<String> errors = Part2JsonSupport.schemaValidationErrors(Map.of("temperature", "warm"), schema);

		assertFalse("String temperature must not validate against numeric result schema.", errors.isEmpty());
		assertTrue("Numeric temperature should validate cleanly.",
				Part2JsonSupport.schemaValidationErrors(Map.of("temperature", 21.5), schema).isEmpty());
	}

	private static List<Method> releasedMethods() {
		return Arrays.stream(Part2JsonTests.class.getDeclaredMethods())
			.filter(method -> method.getAnnotation(Test.class) != null)
			.toList();
	}

	private static boolean containsTarget(String target) {
		return releasedMethods().stream()
			.map(method -> method.getAnnotation(Test.class).description())
			.anyMatch(description -> containsCanonicalTarget(description, target));
	}

	private static boolean containsCanonicalTarget(String description, String target) {
		int start = description.indexOf(target);
		while (start >= 0) {
			int end = start + target.length();
			boolean leftBoundary = start == 0 || isTargetDelimiter(description.charAt(start - 1));
			boolean rightBoundary = end == description.length() || isTargetDelimiter(description.charAt(end));
			if (leftBoundary && rightBoundary) {
				return true;
			}
			start = description.indexOf(target, start + 1);
		}
		return false;
	}

	private static boolean isTargetDelimiter(char character) {
		return Character.isWhitespace(character) || character == '(' || character == ')' || character == ','
				|| character == ';' || character == ':';
	}

}
