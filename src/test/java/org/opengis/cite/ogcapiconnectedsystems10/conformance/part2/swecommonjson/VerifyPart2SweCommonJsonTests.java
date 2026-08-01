package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.swecommonjson;

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
 * Unit checks for the Sprint 66 Part 2 SWE Common JSON released ATS closure.
 */
public class VerifyPart2SweCommonJsonTests {

	private static final String REQ_BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/swecommon-json/";

	private static final Set<String> RELEASED_TARGETS = Set.of(REQ_BASE + "mediatype-read",
			REQ_BASE + "mediatype-write", REQ_BASE + "obsschema-schema", REQ_BASE + "obsschema-mapping",
			REQ_BASE + "observation-encoding", REQ_BASE + "cmdschema-schema", REQ_BASE + "cmdschema-mapping",
			REQ_BASE + "command-encoding");

	@Test
	public void releasedSuiteExposesExactlyOneTestMethodPerAnnexA10Target() {
		List<Method> methods = releasedMethods();
		assertEquals(
				"SCENARIO-ETS-PART2-010-RELEASED-PROCEDURES-001: Part 2 SWE Common JSON must expose exactly the eight released Annex A.10 procedures.",
				RELEASED_TARGETS.size(), methods.size());

		Set<String> covered = new HashSet<>();
		for (Method method : methods) {
			org.testng.annotations.Test ann = method.getAnnotation(org.testng.annotations.Test.class);
			List<String> matched = RELEASED_TARGETS.stream()
				.filter(target -> containsCanonicalTarget(ann.description(), target))
				.toList();
			assertEquals("Each Part 2 SWE Common JSON method must cite exactly one released target: " + method.getName()
					+ " -> " + matched, 1, matched.size());
			covered.add(matched.get(0));
		}
		assertEquals("Part 2 SWE Common JSON released targets are not covered exactly once.", RELEASED_TARGETS,
				covered);
	}

	@Test
	public void releasedSuiteContainsNoStandaloneDeclarationPrerequisiteOrConditionProcedures() {
		Set<String> methodNames = releasedMethods().stream().map(Method::getName).collect(Collectors.toSet());

		assertFalse("Declaration is a setup gate, not a released Annex A.10 procedure.",
				methodNames.contains("part2SweCommonJsonConformanceDeclared"));
		assertFalse("SWE prerequisite visibility is a setup gate, not a released Annex A.10 procedure.",
				methodNames.contains("sweJsonEncodingRulesPrerequisiteVisibleForFullClosure"));
		assertFalse("Resource condition gates are per-procedure gates, not standalone Annex A.10 procedures.",
				methodNames.contains("sweCommonJsonResourceConditionGatesAreVisible"));
		assertTrue("Released mediatype-read procedure is missing.", containsTarget(REQ_BASE + "mediatype-read"));
	}

	@Test
	public void everyReleasedMethodTracesSprint66ScenarioAndRequirement() {
		for (Method method : releasedMethods()) {
			org.testng.annotations.Test ann = method.getAnnotation(org.testng.annotations.Test.class);
			String description = ann.description();
			assertTrue(method.getName() + " missing REQ-ETS-PART2-010 trace",
					description.contains("REQ-ETS-PART2-010"));
			assertTrue(method.getName() + " missing Sprint 66 released-procedure scenario trace",
					description.contains("SCENARIO-ETS-PART2-010-RELEASED-PROCEDURES-001"));
			assertTrue(method.getName() + " should carry part2swecommonjson group",
					Arrays.asList(ann.groups()).contains(Part2SweCommonJsonTests.GROUP));
		}
	}

	@Test
	public void officialPart2SweCommonJsonIdentifiersAreExposed() {
		String joined = String.join(" ", Part2SweCommonJsonTests.CONF_SWE_COMMON_JSON,
				Part2SweCommonJsonTests.REQ_SWE_COMMON_JSON, Part2SweCommonJsonTests.REQ_MEDIATYPE_READ,
				Part2SweCommonJsonTests.REQ_MEDIATYPE_WRITE, Part2SweCommonJsonTests.REQ_OBSSCHEMA_SCHEMA,
				Part2SweCommonJsonTests.REQ_COMMAND_ENCODING, Part2SweCommonJsonTests.CONF_SWE_JSON_ENCODING_RULES,
				Part2SweCommonJsonTests.SWE_JSON_MEDIA_TYPE);

		assertTrue(joined.contains("ogcapi-connectedsystems-2/1.0/conf/swecommon-json"));
		assertTrue(joined.contains("ogcapi-connectedsystems-2/1.0/req/swecommon-json"));
		assertTrue(joined.contains("SWE/3.0/conf/json-encoding-rules"));
		assertTrue(joined.contains("application/swe+json"));
		assertFalse(joined.contains("application/vnd.ogc.swe+json"));
		assertFalse(joined.contains("ogcapi-connectedsystems-1/1.0/conf/swecommon-json"));
	}

	@Test
	public void conditionGateMatrixReportsMissingClasses() {
		Map<String, Object> sweJsonOnly = Map.of("conformsTo", List.of(Part2SweCommonJsonTests.CONF_SWE_COMMON_JSON));

		List<String> missing = Part2SweCommonJsonTests.missingConditionClasses(sweJsonOnly);

		assertEquals(3, missing.size());
		assertTrue(missing
			.contains(Part2SweCommonJsonTests.missingConditionMessage(Part2SweCommonJsonTests.CONF_DATASTREAM,
					"Requirements 109-111 Observation Schema and Observation SWE Common JSON")));
		assertTrue(missing
			.contains(Part2SweCommonJsonTests.missingConditionMessage(Part2SweCommonJsonTests.CONF_CONTROLSTREAM,
					"Requirements 112-114 Command Schema and Command SWE Common JSON")));
		assertTrue(missing.contains(
				Part2SweCommonJsonTests.missingConditionMessage(Part2SweCommonJsonTests.CONF_CREATE_REPLACE_DELETE,
						"Requirement 108 SWE Common JSON mediatype-write")));

		Map<String, Object> complete = Map.of("conformsTo",
				List.of(Part2SweCommonJsonTests.CONF_SWE_COMMON_JSON, Part2SweCommonJsonTests.CONF_DATASTREAM,
						Part2SweCommonJsonTests.CONF_CONTROLSTREAM,
						Part2SweCommonJsonTests.CONF_CREATE_REPLACE_DELETE));
		assertTrue(Part2SweCommonJsonTests.missingConditionClasses(complete).isEmpty());
	}

	@Test
	public void sweCommonJsonContentTypeRequiresExactMediaType() {
		assertTrue(Part2SweCommonJsonTests.isExactSweJsonContentType("application/swe+json"));
		assertTrue(Part2SweCommonJsonTests.isExactSweJsonContentType("application/swe+json; charset=utf-8"));
		assertFalse(Part2SweCommonJsonTests.isExactSweJsonContentType("application/json"));
		assertFalse(Part2SweCommonJsonTests.isExactSweJsonContentType("application/vnd.ogc.swe+json"));
		assertFalse(Part2SweCommonJsonTests.isExactSweJsonContentType("auto"));
		assertFalse(Part2SweCommonJsonTests.isExactSweJsonContentType(null));
	}

	@Test
	public void schemaMetadataCanBeJsonButNotAutoOrHtml() {
		assertTrue(Part2SweCommonJsonTests.isJsonCompatibleContentType("application/json"));
		assertTrue(Part2SweCommonJsonTests.isJsonCompatibleContentType("application/swe+json"));
		assertFalse(Part2SweCommonJsonTests.isJsonCompatibleContentType("auto"));
		assertFalse(Part2SweCommonJsonTests.isJsonCompatibleContentType("text/html"));
		assertFalse(Part2SweCommonJsonTests.isJsonCompatibleContentType(null));
	}

	@Test
	public void allAnnexA10SchemaResourcesAreBundledAndLoad() {
		for (String schemaFile : Part2SweCommonJsonTests.ANNEX_A10_SCHEMA_FILES) {
			assertTrue("Missing schema resource " + schemaFile,
					Part2SweCommonJsonTests.schemaResourceExists(schemaFile));
			assertTrue("Schema did not load through classpath mapper: " + schemaFile,
					Part2SweCommonJsonTests.schemaLoads(schemaFile));
			assertTrue(Part2SweCommonJsonTests.schemaIri(schemaFile)
				.startsWith("https://csapi-compliance.local/schemas/connected-systems-2/json/"));
		}
	}

	@Test
	public void schemaJsonEncodingRequiresEncodingTypeJsonEncoding() {
		assertTrue(Part2SweCommonJsonTests.schemaHasJsonEncoding(Map.of("encoding", Map.of("type", "JSONEncoding"))));
		assertFalse(Part2SweCommonJsonTests.schemaHasJsonEncoding(Map.of("encoding", Map.of("type", "TextEncoding"))));
		assertFalse(Part2SweCommonJsonTests.schemaHasJsonEncoding(Map.of("encoding", "JSONEncoding")));
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

		assertTrue(Part2SweCommonJsonTests.containsTimeComponentWithDefinition(phenomenonTime,
				Part2SweCommonJsonTests.OBSERVATION_TIME_DEFINITIONS));
		assertTrue(Part2SweCommonJsonTests.containsTimeComponentWithDefinition(samplingTime,
				Part2SweCommonJsonTests.OBSERVATION_TIME_DEFINITIONS));
		assertTrue(Part2SweCommonJsonTests.containsTimeComponentWithDefinition(resultTime,
				Part2SweCommonJsonTests.OBSERVATION_TIME_DEFINITIONS));
		assertFalse(Part2SweCommonJsonTests.containsTimeComponentWithDefinition(nonCanonicalPhenomenonTime,
				Part2SweCommonJsonTests.OBSERVATION_TIME_DEFINITIONS));
		assertFalse(Part2SweCommonJsonTests.containsTimeComponentWithDefinition(missingDefinition,
				Part2SweCommonJsonTests.OBSERVATION_TIME_DEFINITIONS));
	}

	@Test
	public void issueTimeEvidenceRequiresCanonicalDefinitionOnTimeComponent() {
		Map<String, Object> issueTime = Map.of("type", "DataRecord", "fields",
				List.of(Map.of("name", "issueTime", "component",
						Map.of("type", "Time", "definition", Part2SweCommonJsonTests.COMMAND_ISSUE_TIME_DEFINITION))));
		Map<String, Object> nonIssueTime = Map.of("type", "DataRecord", "fields", List.of(Map.of("name", "validTime",
				"component", Map.of("type", "Time", "definition", "http://example.test/validTime"))));
		Map<String, Object> namedIssueTimeOnly = Map.of("type", "DataRecord", "fields",
				List.of(Map.of("name", "issueTime", "component", Map.of("type", "Time"))));

		assertTrue(Part2SweCommonJsonTests.containsIssueTimeComponentWithCanonicalDefinition(issueTime));
		assertFalse(Part2SweCommonJsonTests.containsIssueTimeComponentWithCanonicalDefinition(nonIssueTime));
		assertFalse(Part2SweCommonJsonTests.containsIssueTimeComponentWithCanonicalDefinition(namedIssueTimeOnly));
	}

	@Test
	public void apiDefinitionWriteAdvertisementRequiresPostOrPutApplicationSweJsonRequestBody() {
		Map<String, Object> apiDefinition = Map.of("paths", Map.of("/datastreams/{datastreamId}/observations", Map.of(
				"post",
				Map.of("requestBody", Map.of("content", Map.of("application/swe+json", Map.of("schema", Map.of())))))));
		Map<String, List<String>> expected = Map.of("Observation resources",
				List.of("/datastreams/{datastreamId}/observations"));

		assertTrue(Part2SweCommonJsonTests.missingSweJsonWriteAdvertisements(apiDefinition, expected).isEmpty());
	}

	@Test
	public void apiDefinitionDoesNotPassFromOptionsJsonFallbackVendorDraftOrUnrelatedPaths() {
		Map<String, List<String>> expected = Map.of("Observation resources",
				List.of("/datastreams/{datastreamId}/observations"));
		Map<String, Object> optionsOnly = Map.of("paths", Map.of("/systems/{systemId}/datastreams",
				Map.of("options", Map.of("requestBody", Map.of("content", Map.of("application/swe+json", Map.of()))))));
		Map<String, Object> jsonOnly = Map.of("paths", Map.of("/systems/{systemId}/datastreams",
				Map.of("post", Map.of("requestBody", Map.of("content", Map.of("application/json", Map.of()))))));
		Map<String, Object> vendorDraftOnly = Map.of("paths", Map.of("/systems/{systemId}/datastreams", Map.of("put",
				Map.of("requestBody", Map.of("content", Map.of("application/vnd.ogc.swe+json", Map.of()))))));
		Map<String, Object> unrelatedSweJson = Map.of("paths", Map.of("/systems/{systemId}/datastreams",
				Map.of("post", Map.of("requestBody", Map.of("content", Map.of("application/swe+json", Map.of()))))));
		Map<String, Object> commandStatusSubresource = Map.of("paths", Map.of("/commands/{commandId}/status",
				Map.of("put", Map.of("requestBody", Map.of("content", Map.of("application/swe+json", Map.of()))))));

		assertFalse(Part2SweCommonJsonTests.missingSweJsonWriteAdvertisements(optionsOnly, expected).isEmpty());
		assertFalse(Part2SweCommonJsonTests.missingSweJsonWriteAdvertisements(jsonOnly, expected).isEmpty());
		assertFalse(Part2SweCommonJsonTests.missingSweJsonWriteAdvertisements(vendorDraftOnly, expected).isEmpty());
		assertFalse(Part2SweCommonJsonTests.missingSweJsonWriteAdvertisements(unrelatedSweJson, expected).isEmpty());
		assertFalse(Part2SweCommonJsonTests.missingSweJsonWriteAdvertisements(commandStatusSubresource, expected)
			.isEmpty());
		assertTrue(Part2SweCommonJsonTests.isObservationOrCommandResourcePath("/observations"));
		assertTrue(Part2SweCommonJsonTests.isObservationOrCommandResourcePath("/observations/{obsId}"));
		assertTrue(Part2SweCommonJsonTests.isObservationOrCommandResourcePath("/controlstreams/{csId}/commands"));
		assertTrue(Part2SweCommonJsonTests.isObservationOrCommandResourcePath("/commands/{commandId}"));
		assertFalse(Part2SweCommonJsonTests.isObservationOrCommandResourcePath("/commands/{commandId}/status"));
		assertFalse(Part2SweCommonJsonTests.isObservationOrCommandResourcePath("/systems/{systemId}/datastreams"));
	}

	@Test
	public void apiDefinitionWriteAdvertisementRequiresEveryScopedWriteOperationToAdvertiseSweJson() {
		Map<String, List<String>> expected = Map.of("Observation resources",
				List.of("/observations", "/observations/{observationId}"));
		Map<String, Object> mixedOperations = Map.of("paths",
				Map.of("/observations",
						Map.of("post",
								Map.of("requestBody",
										Map.of("content", Map.of("application/swe+json", Map.of("schema", Map.of()))))),
						"/observations/{id}", Map.of("put", Map.of("requestBody",
								Map.of("content", Map.of("application/json", Map.of("schema", Map.of())))))));

		assertEquals(List.of("Observation resources PUT /observations/{id}"),
				Part2SweCommonJsonTests.missingSweJsonWriteAdvertisements(mixedOperations, expected));
	}

	@Test
	public void groupNameIsStableForTestNgWiring() {
		assertEquals("part2swecommonjson", Part2SweCommonJsonTests.GROUP);
	}

	private static List<Method> releasedMethods() {
		return Arrays.stream(Part2SweCommonJsonTests.class.getDeclaredMethods())
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
