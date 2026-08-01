package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.swecommontext;

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
 * Unit checks for the Sprint 67 Part 2 SWE Common Text released ATS closure.
 */
public class VerifyPart2SweCommonTextTests {

	private static final String REQ_BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/swecommon-text/";

	private static final Set<String> RELEASED_TARGETS = Set.of(REQ_BASE + "mediatype-read",
			REQ_BASE + "mediatype-write", REQ_BASE + "obsschema-schema", REQ_BASE + "obsschema-mapping",
			REQ_BASE + "observation-encoding", REQ_BASE + "cmdschema-schema", REQ_BASE + "cmdschema-mapping",
			REQ_BASE + "command-encoding");

	@Test
	public void releasedSuiteExposesExactlyOneTestMethodPerAnnexA11Target() {
		List<Method> methods = releasedMethods();
		assertEquals(
				"SCENARIO-ETS-PART2-011-RELEASED-PROCEDURES-001: Part 2 SWE Common Text must expose exactly the eight released Annex A.11 procedures.",
				RELEASED_TARGETS.size(), methods.size());

		Set<String> covered = new HashSet<>();
		for (Method method : methods) {
			org.testng.annotations.Test ann = method.getAnnotation(org.testng.annotations.Test.class);
			List<String> matched = RELEASED_TARGETS.stream()
				.filter(target -> containsCanonicalTarget(ann.description(), target))
				.toList();
			assertEquals("Each Part 2 SWE Common Text method must cite exactly one released target: " + method.getName()
					+ " -> " + matched, 1, matched.size());
			covered.add(matched.get(0));
		}
		assertEquals("Part 2 SWE Common Text released targets are not covered exactly once.", RELEASED_TARGETS,
				covered);
	}

	@Test
	public void releasedSuiteContainsNoStandaloneDeclarationPrerequisiteOrConditionProcedures() {
		Set<String> methodNames = releasedMethods().stream().map(Method::getName).collect(Collectors.toSet());

		assertFalse("Declaration is a setup gate, not a released Annex A.11 procedure.",
				methodNames.contains("part2SweCommonTextConformanceDeclared"));
		assertFalse("SWE prerequisite visibility is a setup gate, not a released Annex A.11 procedure.",
				methodNames.contains("sweTextEncodingRulesPrerequisiteVisibleForFullClosure"));
		assertFalse("Resource condition gates are per-procedure gates, not standalone Annex A.11 procedures.",
				methodNames.contains("sweCommonTextResourceConditionGatesAreVisible"));
		assertTrue("Released mediatype-read procedure is missing.", containsTarget(REQ_BASE + "mediatype-read"));
	}

	@Test
	public void everyReleasedMethodTracesSprint67ScenarioAndRequirement() {
		for (Method method : releasedMethods()) {
			org.testng.annotations.Test ann = method.getAnnotation(org.testng.annotations.Test.class);
			String description = ann.description();
			assertTrue(method.getName() + " missing REQ-ETS-PART2-011 trace",
					description.contains("REQ-ETS-PART2-011"));
			assertTrue(method.getName() + " missing Sprint 67 released-procedure scenario trace",
					description.contains("SCENARIO-ETS-PART2-011-RELEASED-PROCEDURES-001"));
			assertTrue(method.getName() + " should carry part2swecommontext group",
					Arrays.asList(ann.groups()).contains(Part2SweCommonTextTests.GROUP));
		}
	}

	@Test
	public void officialPart2SweCommonTextIdentifiersAreExposed() {
		String joined = String.join(" ", Part2SweCommonTextTests.CONF_SWE_COMMON_TEXT,
				Part2SweCommonTextTests.REQ_SWE_COMMON_TEXT, Part2SweCommonTextTests.REQ_MEDIATYPE_READ,
				Part2SweCommonTextTests.REQ_MEDIATYPE_WRITE, Part2SweCommonTextTests.REQ_OBSSCHEMA_SCHEMA,
				Part2SweCommonTextTests.REQ_COMMAND_ENCODING, Part2SweCommonTextTests.CONF_SWE_TEXT_ENCODING_RULES,
				Part2SweCommonTextTests.SWE_TEXT_MEDIA_TYPE);

		assertTrue(joined.contains("ogcapi-connectedsystems-2/1.0/conf/swecommon-text"));
		assertTrue(joined.contains("ogcapi-connectedsystems-2/1.0/req/swecommon-text"));
		assertTrue(joined.contains("SWE/3.0/conf/text-encoding-rules"));
		assertTrue(joined.contains("application/swe+text"));
		assertFalse(joined.contains("application/vnd.ogc.swe+text"));
		assertFalse(joined.contains("application/swe+csv"));
		assertFalse(joined.contains("application/swe+binary"));
		assertFalse(joined.contains("application/swe+json"));
		assertFalse(joined.contains("ogcapi-connectedsystems-1/1.0/conf/swecommon-text"));
	}

	@Test
	public void conditionGateMatrixReportsMissingClasses() {
		Map<String, Object> sweTextOnly = Map.of("conformsTo", List.of(Part2SweCommonTextTests.CONF_SWE_COMMON_TEXT));

		List<String> missing = Part2SweCommonTextTests.missingConditionClasses(sweTextOnly);

		assertEquals(3, missing.size());
		assertTrue(missing
			.contains(Part2SweCommonTextTests.missingConditionMessage(Part2SweCommonTextTests.CONF_DATASTREAM,
					"Requirements 117-119 Observation Schema and Observation SWE Common Text")));
		assertTrue(missing
			.contains(Part2SweCommonTextTests.missingConditionMessage(Part2SweCommonTextTests.CONF_CONTROLSTREAM,
					"Requirements 120-122 Command Schema and Command SWE Common Text")));
		assertTrue(missing.contains(
				Part2SweCommonTextTests.missingConditionMessage(Part2SweCommonTextTests.CONF_CREATE_REPLACE_DELETE,
						"Requirement 116 SWE Common Text mediatype-write")));

		Map<String, Object> complete = Map.of("conformsTo",
				List.of(Part2SweCommonTextTests.CONF_SWE_COMMON_TEXT, Part2SweCommonTextTests.CONF_DATASTREAM,
						Part2SweCommonTextTests.CONF_CONTROLSTREAM,
						Part2SweCommonTextTests.CONF_CREATE_REPLACE_DELETE));
		assertTrue(Part2SweCommonTextTests.missingConditionClasses(complete).isEmpty());
	}

	@Test
	public void sweCommonTextContentTypeRequiresExactMediaType() {
		assertTrue(Part2SweCommonTextTests.isExactSweTextContentType("application/swe+text"));
		assertTrue(Part2SweCommonTextTests.isExactSweTextContentType("application/swe+text; charset=utf-8"));
		assertFalse(Part2SweCommonTextTests.isExactSweTextContentType("application/json"));
		assertFalse(Part2SweCommonTextTests.isExactSweTextContentType("application/vnd.ogc.swe+text"));
		assertFalse(Part2SweCommonTextTests.isExactSweTextContentType("application/swe+csv"));
		assertFalse(Part2SweCommonTextTests.isExactSweTextContentType("application/swe+binary"));
		assertFalse(Part2SweCommonTextTests.isExactSweTextContentType("application/swe+json"));
		assertFalse(Part2SweCommonTextTests.isExactSweTextContentType("auto"));
		assertFalse(Part2SweCommonTextTests.isExactSweTextContentType(null));
	}

	@Test
	public void schemaMetadataCanBeJsonButNotAutoOrHtml() {
		assertTrue(Part2SweCommonTextTests.isJsonCompatibleContentType("application/json"));
		assertTrue(Part2SweCommonTextTests.isJsonCompatibleContentType("application/swe+json"));
		assertFalse(Part2SweCommonTextTests.isJsonCompatibleContentType("application/swe+text"));
		assertFalse(Part2SweCommonTextTests.isJsonCompatibleContentType("auto"));
		assertFalse(Part2SweCommonTextTests.isJsonCompatibleContentType("text/html"));
		assertFalse(Part2SweCommonTextTests.isJsonCompatibleContentType(null));
	}

	@Test
	public void allAnnexA11SchemaResourcesAreBundledAndLoad() {
		for (String schemaFile : Part2SweCommonTextTests.ANNEX_A11_SCHEMA_FILES) {
			assertTrue("Missing schema resource " + schemaFile,
					Part2SweCommonTextTests.schemaResourceExists(schemaFile));
			assertTrue("Schema did not load through classpath mapper: " + schemaFile,
					Part2SweCommonTextTests.schemaLoads(schemaFile));
			assertTrue(Part2SweCommonTextTests.schemaIri(schemaFile)
				.startsWith("https://csapi-compliance.local/schemas/connected-systems-2/json/"));
		}
	}

	@Test
	public void schemaTextEncodingRequiresEncodingTypeTextEncoding() {
		assertTrue(Part2SweCommonTextTests.schemaHasTextEncoding(Map.of("encoding", Map.of("type", "TextEncoding"))));
		assertFalse(Part2SweCommonTextTests.schemaHasTextEncoding(Map.of("encoding", Map.of("type", "JSONEncoding"))));
		assertFalse(Part2SweCommonTextTests.schemaHasTextEncoding(Map.of("encoding", "TextEncoding")));
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

		assertTrue(Part2SweCommonTextTests.containsTimeComponentWithDefinition(phenomenonTime,
				Part2SweCommonTextTests.OBSERVATION_TIME_DEFINITIONS));
		assertTrue(Part2SweCommonTextTests.containsTimeComponentWithDefinition(samplingTime,
				Part2SweCommonTextTests.OBSERVATION_TIME_DEFINITIONS));
		assertTrue(Part2SweCommonTextTests.containsTimeComponentWithDefinition(resultTime,
				Part2SweCommonTextTests.OBSERVATION_TIME_DEFINITIONS));
		assertFalse(Part2SweCommonTextTests.containsTimeComponentWithDefinition(nonCanonicalPhenomenonTime,
				Part2SweCommonTextTests.OBSERVATION_TIME_DEFINITIONS));
		assertFalse(Part2SweCommonTextTests.containsTimeComponentWithDefinition(missingDefinition,
				Part2SweCommonTextTests.OBSERVATION_TIME_DEFINITIONS));
	}

	@Test
	public void issueTimeEvidenceRequiresCanonicalDefinitionOnTimeComponent() {
		Map<String, Object> issueTime = Map.of("type", "DataRecord", "fields",
				List.of(Map.of("name", "issueTime", "component",
						Map.of("type", "Time", "definition", Part2SweCommonTextTests.COMMAND_ISSUE_TIME_DEFINITION))));
		Map<String, Object> nonIssueTime = Map.of("type", "DataRecord", "fields", List.of(Map.of("name", "validTime",
				"component", Map.of("type", "Time", "definition", "http://example.test/validTime"))));
		Map<String, Object> namedIssueTimeOnly = Map.of("type", "DataRecord", "fields",
				List.of(Map.of("name", "issueTime", "component", Map.of("type", "Time"))));

		assertTrue(Part2SweCommonTextTests.containsIssueTimeComponentWithCanonicalDefinition(issueTime));
		assertFalse(Part2SweCommonTextTests.containsIssueTimeComponentWithCanonicalDefinition(nonIssueTime));
		assertFalse(Part2SweCommonTextTests.containsIssueTimeComponentWithCanonicalDefinition(namedIssueTimeOnly));
		assertFalse(Part2SweCommonTextTests.hasPresentNonCanonicalIssueTimeEvidence(nonIssueTime));
		assertTrue(Part2SweCommonTextTests.hasPresentNonCanonicalIssueTimeEvidence(namedIssueTimeOnly));
	}

	@Test
	public void issueTimeEvidenceFlagsPresentNonCanonicalDefinitions() {
		Map<String, Object> wrongDefinition = Map.of("type", "DataRecord", "fields", List.of(Map.of("name", "issueTime",
				"component", Map.of("type", "Time", "definition", "http://example.test/IssueTime"))));

		assertFalse(Part2SweCommonTextTests.containsIssueTimeComponentWithCanonicalDefinition(wrongDefinition));
		assertTrue(Part2SweCommonTextTests.hasPresentNonCanonicalIssueTimeEvidence(wrongDefinition));
	}

	@Test
	public void apiDefinitionWriteAdvertisementRequiresPostOrPutApplicationSweTextRequestBody() {
		Map<String, Object> apiDefinition = Map.of("paths", Map.of("/datastreams/{datastreamId}/observations", Map.of(
				"post",
				Map.of("requestBody", Map.of("content", Map.of("application/swe+text", Map.of("schema", Map.of())))))));
		Map<String, List<String>> expected = Map.of("Observation resources",
				List.of("/datastreams/{datastreamId}/observations"));

		assertTrue(Part2SweCommonTextTests.missingSweTextWriteAdvertisements(apiDefinition, expected).isEmpty());
	}

	@Test
	public void apiDefinitionDoesNotPassFromOptionsJsonFallbackVendorDraftOrUnrelatedPaths() {
		Map<String, List<String>> expected = Map.of("Observation resources",
				List.of("/datastreams/{datastreamId}/observations"));
		Map<String, Object> optionsOnly = Map.of("paths", Map.of("/systems/{systemId}/datastreams",
				Map.of("options", Map.of("requestBody", Map.of("content", Map.of("application/swe+text", Map.of()))))));
		Map<String, Object> jsonOnly = Map.of("paths", Map.of("/systems/{systemId}/datastreams",
				Map.of("post", Map.of("requestBody", Map.of("content", Map.of("application/json", Map.of()))))));
		Map<String, Object> vendorDraftOnly = Map.of("paths", Map.of("/systems/{systemId}/datastreams", Map.of("put",
				Map.of("requestBody", Map.of("content", Map.of("application/vnd.ogc.swe+text", Map.of()))))));
		Map<String, Object> csvOnly = Map.of("paths", Map.of("/datastreams/{datastreamId}/observations",
				Map.of("post", Map.of("requestBody", Map.of("content", Map.of("application/swe+csv", Map.of()))))));
		Map<String, Object> binaryOnly = Map.of("paths", Map.of("/datastreams/{datastreamId}/observations",
				Map.of("post", Map.of("requestBody", Map.of("content", Map.of("application/swe+binary", Map.of()))))));
		Map<String, Object> sweJsonOnly = Map.of("paths", Map.of("/datastreams/{datastreamId}/observations",
				Map.of("post", Map.of("requestBody", Map.of("content", Map.of("application/swe+json", Map.of()))))));
		Map<String, Object> unrelatedSweText = Map.of("paths", Map.of("/systems/{systemId}/datastreams",
				Map.of("post", Map.of("requestBody", Map.of("content", Map.of("application/swe+text", Map.of()))))));
		Map<String, Object> commandStatusSubresource = Map.of("paths", Map.of("/commands/{commandId}/status",
				Map.of("put", Map.of("requestBody", Map.of("content", Map.of("application/swe+text", Map.of()))))));

		assertFalse(Part2SweCommonTextTests.missingSweTextWriteAdvertisements(optionsOnly, expected).isEmpty());
		assertFalse(Part2SweCommonTextTests.missingSweTextWriteAdvertisements(jsonOnly, expected).isEmpty());
		assertFalse(Part2SweCommonTextTests.missingSweTextWriteAdvertisements(vendorDraftOnly, expected).isEmpty());
		assertFalse(Part2SweCommonTextTests.missingSweTextWriteAdvertisements(csvOnly, expected).isEmpty());
		assertFalse(Part2SweCommonTextTests.missingSweTextWriteAdvertisements(binaryOnly, expected).isEmpty());
		assertFalse(Part2SweCommonTextTests.missingSweTextWriteAdvertisements(sweJsonOnly, expected).isEmpty());
		assertFalse(Part2SweCommonTextTests.missingSweTextWriteAdvertisements(unrelatedSweText, expected).isEmpty());
		assertFalse(Part2SweCommonTextTests.missingSweTextWriteAdvertisements(commandStatusSubresource, expected)
			.isEmpty());
		assertTrue(Part2SweCommonTextTests.isObservationOrCommandResourcePath("/observations"));
		assertTrue(Part2SweCommonTextTests.isObservationOrCommandResourcePath("/observations/{obsId}"));
		assertTrue(Part2SweCommonTextTests.isObservationOrCommandResourcePath("/controlstreams/{csId}/commands"));
		assertTrue(Part2SweCommonTextTests.isObservationOrCommandResourcePath("/commands/{commandId}"));
		assertFalse(Part2SweCommonTextTests.isObservationOrCommandResourcePath("/commands/{commandId}/status"));
		assertFalse(Part2SweCommonTextTests.isObservationOrCommandResourcePath("/systems/{systemId}/datastreams"));
	}

	@Test
	public void apiDefinitionWriteAdvertisementRequiresEveryScopedWriteOperationToAdvertiseSweText() {
		Map<String, List<String>> expected = Map.of("Observation resources",
				List.of("/observations", "/observations/{observationId}"));
		Map<String, Object> mixedOperations = Map.of("paths",
				Map.of("/observations",
						Map.of("post",
								Map.of("requestBody",
										Map.of("content", Map.of("application/swe+text", Map.of("schema", Map.of()))))),
						"/observations/{id}", Map.of("put", Map.of("requestBody",
								Map.of("content", Map.of("application/json", Map.of("schema", Map.of())))))));

		assertEquals(List.of("Observation resources PUT /observations/{id}"),
				Part2SweCommonTextTests.missingSweTextWriteAdvertisements(mixedOperations, expected));
	}

	@Test
	public void groupNameIsStableForTestNgWiring() {
		assertEquals("part2swecommontext", Part2SweCommonTextTests.GROUP);
	}

	private static List<Method> releasedMethods() {
		return Arrays.stream(Part2SweCommonTextTests.class.getDeclaredMethods())
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
