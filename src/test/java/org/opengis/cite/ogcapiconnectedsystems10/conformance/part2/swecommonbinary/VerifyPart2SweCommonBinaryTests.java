package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.swecommonbinary;

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

import org.junit.Test;

/**
 * Unit checks for the Sprint 68 Part 2 SWE Common Binary released ATS closure.
 */
public class VerifyPart2SweCommonBinaryTests {

	private static final String REQ_BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/swecommon-binary/";

	private static final Set<String> RELEASED_TARGETS = Set.of(REQ_BASE + "mediatype-read",
			REQ_BASE + "mediatype-write", REQ_BASE + "obsschema-schema", REQ_BASE + "obsschema-mapping",
			REQ_BASE + "observation-encoding", REQ_BASE + "cmdschema-schema", REQ_BASE + "cmdschema-mapping",
			REQ_BASE + "command-encoding");

	@Test
	public void releasedSuiteExposesExactlyOneTestMethodPerAnnexA12Target() {
		List<Method> methods = releasedMethods();
		assertEquals(
				"SCENARIO-ETS-PART2-012-RELEASED-PROCEDURES-001: Part 2 SWE Common Binary must expose exactly the eight released Annex A.12 procedures.",
				RELEASED_TARGETS.size(), methods.size());

		Set<String> covered = new HashSet<>();
		for (Method method : methods) {
			org.testng.annotations.Test ann = method.getAnnotation(org.testng.annotations.Test.class);
			List<String> matched = RELEASED_TARGETS.stream()
				.filter(target -> containsCanonicalTarget(ann.description(), target))
				.toList();
			assertEquals("Each Part 2 SWE Common Binary method must cite exactly one released target: "
					+ method.getName() + " -> " + matched, 1, matched.size());
			covered.add(matched.get(0));
		}
		assertEquals("Part 2 SWE Common Binary released targets are not covered exactly once.", RELEASED_TARGETS,
				covered);
	}

	@Test
	public void releasedSuiteContainsNoStandaloneDeclarationPrerequisiteOrConditionProcedures() {
		Set<String> methodNames = releasedMethods().stream().map(Method::getName).collect(Collectors.toSet());

		assertFalse("Declaration is a setup gate, not a released Annex A.12 procedure.",
				methodNames.contains("part2SweCommonBinaryConformanceDeclared"));
		assertFalse("SWE prerequisite visibility is a setup gate, not a released Annex A.12 procedure.",
				methodNames.contains("sweBinaryEncodingRulesPrerequisiteVisibleForFullClosure"));
		assertFalse("Resource condition gates are per-procedure gates, not standalone Annex A.12 procedures.",
				methodNames.contains("sweCommonBinaryResourceConditionGatesAreVisible"));
		assertTrue("Released mediatype-read procedure is missing.", containsTarget(REQ_BASE + "mediatype-read"));
	}

	@Test
	public void everyReleasedMethodTracesSprint68ScenarioAndRequirement() {
		for (Method method : releasedMethods()) {
			org.testng.annotations.Test ann = method.getAnnotation(org.testng.annotations.Test.class);
			String description = ann.description();
			assertTrue(method.getName() + " missing REQ-ETS-PART2-012 trace",
					description.contains("REQ-ETS-PART2-012"));
			assertTrue(method.getName() + " missing Sprint 68 released-procedure scenario trace",
					description.contains("SCENARIO-ETS-PART2-012-RELEASED-PROCEDURES-001"));
			assertTrue(method.getName() + " should carry part2swecommonbinary group",
					Arrays.asList(ann.groups()).contains(Part2SweCommonBinaryTests.GROUP));
		}
	}

	@Test
	public void officialPart2SweCommonBinaryIdentifiersAreExposed() {
		String joined = String.join(" ", Part2SweCommonBinaryTests.CONF_SWE_COMMON_BINARY,
				Part2SweCommonBinaryTests.REQ_SWE_COMMON_BINARY, Part2SweCommonBinaryTests.REQ_MEDIATYPE_READ,
				Part2SweCommonBinaryTests.REQ_MEDIATYPE_WRITE, Part2SweCommonBinaryTests.REQ_OBSSCHEMA_SCHEMA,
				Part2SweCommonBinaryTests.REQ_COMMAND_ENCODING,
				Part2SweCommonBinaryTests.CONF_SWE_BINARY_ENCODING_RULES,
				Part2SweCommonBinaryTests.SWE_BINARY_MEDIA_TYPE);

		assertTrue(joined.contains("ogcapi-connectedsystems-2/1.0/conf/swecommon-binary"));
		assertTrue(joined.contains("ogcapi-connectedsystems-2/1.0/req/swecommon-binary"));
		assertTrue(joined.contains("SWE/3.0/conf/binary-encoding-rules"));
		assertTrue(joined.contains("application/swe+binary"));
		assertFalse(joined.contains("application/vnd.ogc.swe+binary"));
		assertFalse(joined.contains("application/swe+csv"));
		assertFalse(joined.contains("application/swe+text"));
		assertFalse(joined.contains("application/swe+json"));
		assertFalse(joined.contains("ogcapi-connectedsystems-1/1.0/conf/swecommon-binary"));
	}

	@Test
	public void conditionGateMatrixReportsMissingClasses() {
		Map<String, Object> sweBinaryOnly = Map.of("conformsTo",
				List.of(Part2SweCommonBinaryTests.CONF_SWE_COMMON_BINARY));

		List<String> missing = Part2SweCommonBinaryTests.missingConditionClasses(sweBinaryOnly);

		assertEquals(3, missing.size());
		assertTrue(missing
			.contains(Part2SweCommonBinaryTests.missingConditionMessage(Part2SweCommonBinaryTests.CONF_DATASTREAM,
					"Requirements 125-127 Observation Schema and Observation SWE Common Binary")));
		assertTrue(missing
			.contains(Part2SweCommonBinaryTests.missingConditionMessage(Part2SweCommonBinaryTests.CONF_CONTROLSTREAM,
					"Requirements 128-130 Command Schema and Command SWE Common Binary")));
		assertTrue(missing.contains(
				Part2SweCommonBinaryTests.missingConditionMessage(Part2SweCommonBinaryTests.CONF_CREATE_REPLACE_DELETE,
						"Requirement 124 SWE Common Binary mediatype-write")));

		Map<String, Object> complete = Map.of("conformsTo",
				List.of(Part2SweCommonBinaryTests.CONF_SWE_COMMON_BINARY, Part2SweCommonBinaryTests.CONF_DATASTREAM,
						Part2SweCommonBinaryTests.CONF_CONTROLSTREAM,
						Part2SweCommonBinaryTests.CONF_CREATE_REPLACE_DELETE));
		assertTrue(Part2SweCommonBinaryTests.missingConditionClasses(complete).isEmpty());
	}

	@Test
	public void sweCommonBinaryContentTypeRequiresExactMediaType() {
		assertTrue(Part2SweCommonBinaryTests.isExactSweBinaryContentType("application/swe+binary"));
		assertTrue(Part2SweCommonBinaryTests.isExactSweBinaryContentType("application/swe+binary; charset=utf-8"));
		assertFalse(Part2SweCommonBinaryTests.isExactSweBinaryContentType("application/json"));
		assertFalse(Part2SweCommonBinaryTests.isExactSweBinaryContentType("application/vnd.ogc.swe+binary"));
		assertFalse(Part2SweCommonBinaryTests.isExactSweBinaryContentType("application/swe+csv"));
		assertFalse(Part2SweCommonBinaryTests.isExactSweBinaryContentType("application/swe+text"));
		assertFalse(Part2SweCommonBinaryTests.isExactSweBinaryContentType("application/swe+json"));
		assertFalse(Part2SweCommonBinaryTests.isExactSweBinaryContentType("auto"));
		assertFalse(Part2SweCommonBinaryTests.isExactSweBinaryContentType(null));
	}

	@Test
	public void schemaMetadataCanBeJsonButNotAutoOrHtml() {
		assertTrue(Part2SweCommonBinaryTests.isJsonCompatibleContentType("application/json"));
		assertTrue(Part2SweCommonBinaryTests.isJsonCompatibleContentType("application/swe+json"));
		assertFalse(Part2SweCommonBinaryTests.isJsonCompatibleContentType("application/swe+binary"));
		assertFalse(Part2SweCommonBinaryTests.isJsonCompatibleContentType("auto"));
		assertFalse(Part2SweCommonBinaryTests.isJsonCompatibleContentType("text/html"));
		assertFalse(Part2SweCommonBinaryTests.isJsonCompatibleContentType(null));
	}

	@Test
	public void allAnnexA12SchemaResourcesAreBundledAndLoad() {
		for (String schemaFile : Part2SweCommonBinaryTests.ANNEX_A12_SCHEMA_FILES) {
			assertTrue("Missing schema resource " + schemaFile,
					Part2SweCommonBinaryTests.schemaResourceExists(schemaFile));
			assertTrue("Schema did not load through classpath mapper: " + schemaFile,
					Part2SweCommonBinaryTests.schemaLoads(schemaFile));
			assertTrue(Part2SweCommonBinaryTests.schemaIri(schemaFile)
				.startsWith("https://csapi-compliance.local/schemas/connected-systems-2/json/"));
		}
	}

	@Test
	public void schemaBinaryEncodingRequiresEncodingTypeBinaryEncoding() {
		assertTrue(Part2SweCommonBinaryTests
			.schemaHasBinaryEncoding(Map.of("encoding", Map.of("type", "BinaryEncoding"))));
		assertFalse(
				Part2SweCommonBinaryTests.schemaHasBinaryEncoding(Map.of("encoding", Map.of("type", "JSONEncoding"))));
		assertFalse(
				Part2SweCommonBinaryTests.schemaHasBinaryEncoding(Map.of("encoding", Map.of("type", "TextEncoding"))));
		assertFalse(Part2SweCommonBinaryTests.schemaHasBinaryEncoding(Map.of("encoding", "BinaryEncoding")));
	}

	@Test
	public void timeMappingEvidenceRequiresTimeComponentAndCanonicalDefinition() {
		Map<String, Object> phenomenonTime = Map.of("type", "DataRecord", "fields",
				List.of(Map.of("name", "phenomenonTime", "component",
						Map.of("type", "Time", "definition", "http://www.w3.org/ns/sosa/phenomenonTime"))));
		Map<String, Object> samplingTime = Map
			.of("type", "DataRecord", "fields", List.of(Map.of("name", "samplingTime", "component",
					Map.of("type", "Time", "definition", "http://www.opengis.net/def/property/OGC/0/SamplingTime"))));
		Map<String, Object> resultTime = Map.of("type", "DataRecord", "fields", List.of(Map.of("name", "resultTime",
				"component", Map.of("type", "Time", "definition", "http://www.w3.org/ns/sosa/resultTime"))));
		Map<String, Object> nonCanonicalPhenomenonTime = Map.of("type", "DataRecord", "fields",
				List.of(Map.of("name", "phenomenonTime", "component",
						Map.of("type", "Time", "definition", "http://example.test/phenomenonTime"))));
		Map<String, Object> missingDefinition = Map.of("type", "DataRecord", "fields",
				List.of(Map.of("name", "phenomenonTime", "component", Map.of("type", "Time"))));

		assertTrue(Part2SweCommonBinaryTests.containsTimeComponentWithDefinition(phenomenonTime,
				Part2SweCommonBinaryTests.OBSERVATION_TIME_DEFINITIONS));
		assertTrue(Part2SweCommonBinaryTests.containsTimeComponentWithDefinition(samplingTime,
				Part2SweCommonBinaryTests.OBSERVATION_TIME_DEFINITIONS));
		assertTrue(Part2SweCommonBinaryTests.containsTimeComponentWithDefinition(resultTime,
				Part2SweCommonBinaryTests.OBSERVATION_TIME_DEFINITIONS));
		assertFalse(Part2SweCommonBinaryTests.containsTimeComponentWithDefinition(nonCanonicalPhenomenonTime,
				Part2SweCommonBinaryTests.OBSERVATION_TIME_DEFINITIONS));
		assertFalse(Part2SweCommonBinaryTests.containsTimeComponentWithDefinition(missingDefinition,
				Part2SweCommonBinaryTests.OBSERVATION_TIME_DEFINITIONS));
	}

	@Test
	public void issueTimeEvidenceRequiresCanonicalDefinitionOnTimeComponent() {
		Map<String, Object> issueTime = Map
			.of("type", "DataRecord", "fields", List.of(Map.of("name", "issueTime", "component",
					Map.of("type", "Time", "definition", Part2SweCommonBinaryTests.COMMAND_ISSUE_TIME_DEFINITION))));
		Map<String, Object> nonIssueTime = Map.of("type", "DataRecord", "fields", List.of(Map.of("name", "validTime",
				"component", Map.of("type", "Time", "definition", "http://example.test/validTime"))));
		Map<String, Object> namedIssueTimeOnly = Map.of("type", "DataRecord", "fields",
				List.of(Map.of("name", "issueTime", "component", Map.of("type", "Time"))));

		assertTrue(Part2SweCommonBinaryTests.containsIssueTimeComponentWithCanonicalDefinition(issueTime));
		assertFalse(Part2SweCommonBinaryTests.containsIssueTimeComponentWithCanonicalDefinition(nonIssueTime));
		assertFalse(Part2SweCommonBinaryTests.containsIssueTimeComponentWithCanonicalDefinition(namedIssueTimeOnly));
		assertFalse(Part2SweCommonBinaryTests.hasPresentNonCanonicalIssueTimeEvidence(nonIssueTime));
		assertTrue(Part2SweCommonBinaryTests.hasPresentNonCanonicalIssueTimeEvidence(namedIssueTimeOnly));
	}

	@Test
	public void issueTimeEvidenceFlagsPresentNonCanonicalDefinitions() {
		Map<String, Object> wrongDefinition = Map.of("type", "DataRecord", "fields", List.of(Map.of("name", "issueTime",
				"component", Map.of("type", "Time", "definition", "http://example.test/IssueTime"))));

		assertFalse(Part2SweCommonBinaryTests.containsIssueTimeComponentWithCanonicalDefinition(wrongDefinition));
		assertTrue(Part2SweCommonBinaryTests.hasPresentNonCanonicalIssueTimeEvidence(wrongDefinition));
	}

	@Test
	public void commandSchemaIssueTimeMappingFailsWhenCanonicalIssueTimeIsAbsent() {
		// REQ-ETS-PART2-012,
		// SCENARIO-ETS-PART2-012-SCHEMA-MAPPING-TIME-001: retrieved Command
		// Schema evidence without canonical IssueTime fails instead of SKIPping.
		Map<String, Object> commandSchema = Map.of("recordSchema", Map.of("type", "DataRecord", "fields", List.of(Map
			.of("name", "command", "component", Map.of("type", "Text", "definition", "http://example.test/command")))));

		try {
			Part2SweCommonBinaryTests.assertCommandSchemaIssueTimeMappings(List.of(commandSchema),
					Part2SweCommonBinaryTests.REQ_COMMANDSCHEMA_MAPPING);
		}
		catch (AssertionError expected) {
			assertTrue(expected.getMessage().contains(Part2SweCommonBinaryTests.REQ_COMMANDSCHEMA_MAPPING));
			assertTrue(expected.getMessage().contains("IssueTime"));
			return;
		}
		throw new AssertionError("Missing canonical IssueTime mapping evidence should fail, not SKIP or PASS.");
	}

	@Test
	public void observationSchemaMappingChecksEveryRetrievedSchema() {
		// REQ-ETS-PART2-012,
		// SCENARIO-ETS-PART2-012-SCHEMA-MAPPING-TIME-001: a later retrieved
		// DataStream schema cannot be hidden by a valid first schema.
		Map<String, Object> validFirst = Map.of("recordSchema",
				Map.of("type", "DataRecord", "fields", List.of(Map.of("name", "phenomenonTime", "component",
						Map.of("type", "Time", "definition", "http://www.w3.org/ns/sosa/phenomenonTime")))));
		Map<String, Object> invalidSecond = Map.of("recordSchema",
				Map.of("type", "DataRecord", "fields", List.of(Map.of("name", "result", "component",
						Map.of("type", "Quantity", "definition", "http://example.test/result")))));

		try {
			Part2SweCommonBinaryTests.assertObservationSchemaTimeMappings(List.of(validFirst, invalidSecond),
					Part2SweCommonBinaryTests.REQ_OBSSCHEMA_MAPPING);
		}
		catch (AssertionError expected) {
			assertTrue(expected.getMessage().contains(Part2SweCommonBinaryTests.REQ_OBSSCHEMA_MAPPING));
			assertTrue(expected.getMessage().contains("Observation Schema[1]"));
			return;
		}
		throw new AssertionError("Second retrieved Observation Schema with invalid mapping should fail.");
	}

	@Test
	public void commandSchemaMappingChecksEveryRetrievedSchema() {
		// REQ-ETS-PART2-012,
		// SCENARIO-ETS-PART2-012-SCHEMA-MAPPING-TIME-001: a later retrieved
		// ControlStream schema cannot be hidden by a valid first schema.
		Map<String, Object> validFirst = Map
			.of("recordSchema", Map.of("type", "DataRecord", "fields", List.of(Map.of("name", "issueTime", "component",
					Map.of("type", "Time", "definition", Part2SweCommonBinaryTests.COMMAND_ISSUE_TIME_DEFINITION)))));
		Map<String, Object> invalidSecond = Map.of("recordSchema",
				Map.of("type", "DataRecord", "fields", List.of(Map.of("name", "issueTime", "component",
						Map.of("type", "Time", "definition", "http://example.test/IssueTime")))));

		try {
			Part2SweCommonBinaryTests.assertCommandSchemaIssueTimeMappings(List.of(validFirst, invalidSecond),
					Part2SweCommonBinaryTests.REQ_COMMANDSCHEMA_MAPPING);
		}
		catch (AssertionError expected) {
			assertTrue(expected.getMessage().contains(Part2SweCommonBinaryTests.REQ_COMMANDSCHEMA_MAPPING));
			assertTrue(expected.getMessage().contains("Command Schema[1]"));
			return;
		}
		throw new AssertionError("Second retrieved Command Schema with invalid IssueTime mapping should fail.");
	}

	@Test
	public void apiDefinitionReadAdvertisementRequiresGetResponseSweBinaryContent() {
		// REQ-ETS-PART2-012, SCENARIO-ETS-PART2-012-MEDIATYPE-READ-001:
		// mediatype-read requires OpenAPI GET response advertisement evidence.
		Map<String, Object> apiDefinition = Map.of("paths", Map.of("/datastreams/{datastreamId}/observations", Map.of(
				"get",
				Map.of("responses", Map.of("200", Map.of("content", Map.of("application/swe+binary", Map.of())))))));

		assertTrue(Part2SweCommonBinaryTests.apiDefinitionAdvertisesSweBinaryRead(apiDefinition,
				"/datastreams/{datastreamId}/observations"));
	}

	@Test
	public void apiDefinitionReadAdvertisementRejectsRequestBodyPostVendorAndUnrelatedPaths() {
		// REQ-ETS-PART2-012, SCENARIO-ETS-PART2-012-MEDIATYPE-READ-001:
		// request bodies, write operations, vendor media, and unrelated paths are not
		// read-advertisement PASS evidence.
		Map<String, Object> requestBodyOnly = Map.of("paths", Map.of("/observations",
				Map.of("get", Map.of("requestBody", Map.of("content", Map.of("application/swe+binary", Map.of()))))));
		Map<String, Object> postOnly = Map.of("paths", Map.of("/observations", Map.of("post",
				Map.of("responses", Map.of("200", Map.of("content", Map.of("application/swe+binary", Map.of())))))));
		Map<String, Object> vendorDraftOnly = Map.of("paths", Map.of("/observations", Map.of("get", Map.of("responses",
				Map.of("200", Map.of("content", Map.of("application/vnd.ogc.swe+binary", Map.of())))))));
		Map<String, Object> unrelated = Map.of("paths", Map.of("/systems", Map.of("get",
				Map.of("responses", Map.of("200", Map.of("content", Map.of("application/swe+binary", Map.of())))))));

		assertFalse(Part2SweCommonBinaryTests.apiDefinitionAdvertisesSweBinaryRead(requestBodyOnly, "/observations"));
		assertFalse(Part2SweCommonBinaryTests.apiDefinitionAdvertisesSweBinaryRead(postOnly, "/observations"));
		assertFalse(Part2SweCommonBinaryTests.apiDefinitionAdvertisesSweBinaryRead(vendorDraftOnly, "/observations"));
		assertFalse(Part2SweCommonBinaryTests.apiDefinitionAdvertisesSweBinaryRead(unrelated, "/observations"));
	}

	@Test
	public void apiDefinitionWriteAdvertisementRequiresPostOrPutApplicationSweBinaryRequestBody() {
		Map<String, Object> apiDefinition = Map.of("paths",
				Map.of("/datastreams/{datastreamId}/observations", Map.of("post", Map.of("requestBody",
						Map.of("content", Map.of("application/swe+binary", Map.of("schema", Map.of())))))));
		Map<String, List<String>> expected = Map.of("Observation resources",
				List.of("/datastreams/{datastreamId}/observations"));

		assertTrue(Part2SweCommonBinaryTests.missingSweBinaryWriteAdvertisements(apiDefinition, expected).isEmpty());
	}

	@Test
	public void apiDefinitionDoesNotPassFromOptionsJsonFallbackVendorDraftOrUnrelatedPaths() {
		Map<String, List<String>> expected = Map.of("Observation resources",
				List.of("/datastreams/{datastreamId}/observations"));
		Map<String, Object> optionsOnly = Map.of("paths", Map.of("/systems/{systemId}/datastreams", Map.of("options",
				Map.of("requestBody", Map.of("content", Map.of("application/swe+binary", Map.of()))))));
		Map<String, Object> jsonOnly = Map.of("paths", Map.of("/systems/{systemId}/datastreams",
				Map.of("post", Map.of("requestBody", Map.of("content", Map.of("application/json", Map.of()))))));
		Map<String, Object> vendorDraftOnly = Map.of("paths", Map.of("/systems/{systemId}/datastreams", Map.of("put",
				Map.of("requestBody", Map.of("content", Map.of("application/vnd.ogc.swe+binary", Map.of()))))));
		Map<String, Object> csvOnly = Map.of("paths", Map.of("/datastreams/{datastreamId}/observations",
				Map.of("post", Map.of("requestBody", Map.of("content", Map.of("application/swe+csv", Map.of()))))));
		Map<String, Object> textOnly = Map.of("paths", Map.of("/datastreams/{datastreamId}/observations",
				Map.of("post", Map.of("requestBody", Map.of("content", Map.of("application/swe+text", Map.of()))))));
		Map<String, Object> sweJsonOnly = Map.of("paths", Map.of("/datastreams/{datastreamId}/observations",
				Map.of("post", Map.of("requestBody", Map.of("content", Map.of("application/swe+json", Map.of()))))));
		Map<String, Object> unrelatedSweBinary = Map.of("paths", Map.of("/systems/{systemId}/datastreams",
				Map.of("post", Map.of("requestBody", Map.of("content", Map.of("application/swe+binary", Map.of()))))));
		Map<String, Object> commandStatusSubresource = Map.of("paths", Map.of("/commands/{commandId}/status",
				Map.of("put", Map.of("requestBody", Map.of("content", Map.of("application/swe+binary", Map.of()))))));

		assertFalse(Part2SweCommonBinaryTests.missingSweBinaryWriteAdvertisements(optionsOnly, expected).isEmpty());
		assertFalse(Part2SweCommonBinaryTests.missingSweBinaryWriteAdvertisements(jsonOnly, expected).isEmpty());
		assertFalse(Part2SweCommonBinaryTests.missingSweBinaryWriteAdvertisements(vendorDraftOnly, expected).isEmpty());
		assertFalse(Part2SweCommonBinaryTests.missingSweBinaryWriteAdvertisements(csvOnly, expected).isEmpty());
		assertFalse(Part2SweCommonBinaryTests.missingSweBinaryWriteAdvertisements(textOnly, expected).isEmpty());
		assertFalse(Part2SweCommonBinaryTests.missingSweBinaryWriteAdvertisements(sweJsonOnly, expected).isEmpty());
		assertFalse(
				Part2SweCommonBinaryTests.missingSweBinaryWriteAdvertisements(unrelatedSweBinary, expected).isEmpty());
		assertFalse(Part2SweCommonBinaryTests.missingSweBinaryWriteAdvertisements(commandStatusSubresource, expected)
			.isEmpty());
		assertTrue(Part2SweCommonBinaryTests.isObservationOrCommandResourcePath("/observations"));
		assertTrue(Part2SweCommonBinaryTests.isObservationOrCommandResourcePath("/observations/{obsId}"));
		assertTrue(Part2SweCommonBinaryTests.isObservationOrCommandResourcePath("/controlstreams/{csId}/commands"));
		assertTrue(Part2SweCommonBinaryTests.isObservationOrCommandResourcePath("/commands/{commandId}"));
		assertFalse(Part2SweCommonBinaryTests.isObservationOrCommandResourcePath("/commands/{commandId}/status"));
		assertFalse(Part2SweCommonBinaryTests.isObservationOrCommandResourcePath("/systems/{systemId}/datastreams"));
	}

	@Test
	public void apiDefinitionWriteAdvertisementRequiresEveryScopedWriteOperationToAdvertiseSweBinary() {
		Map<String, List<String>> expected = Map.of("Observation resources",
				List.of("/observations", "/observations/{observationId}"));
		Map<String, Object> mixedOperations = Map.of("paths",
				Map.of("/observations", Map.of("post",
						Map.of("requestBody",
								Map.of("content", Map.of("application/swe+binary", Map.of("schema", Map.of()))))),
						"/observations/{id}", Map.of("put", Map.of("requestBody",
								Map.of("content", Map.of("application/json", Map.of("schema", Map.of())))))));

		assertEquals(List.of("Observation resources PUT /observations/{id}"),
				Part2SweCommonBinaryTests.missingSweBinaryWriteAdvertisements(mixedOperations, expected));
	}

	@Test
	public void groupNameIsStableForTestNgWiring() {
		assertEquals("part2swecommonbinary", Part2SweCommonBinaryTests.GROUP);
	}

	private static List<Method> releasedMethods() {
		return Arrays.stream(Part2SweCommonBinaryTests.class.getDeclaredMethods())
			.filter(method -> method.getAnnotation(org.testng.annotations.Test.class) != null)
			.toList();
	}

	private static boolean containsTarget(String target) {
		return releasedMethods().stream()
			.map(method -> method.getAnnotation(org.testng.annotations.Test.class).description())
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
