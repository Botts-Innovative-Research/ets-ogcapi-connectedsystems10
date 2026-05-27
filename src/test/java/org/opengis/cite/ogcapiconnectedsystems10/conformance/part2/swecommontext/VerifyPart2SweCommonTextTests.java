package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.swecommontext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Regression coverage for S-ETS-30-01 Part 2 SWE Common Text Encoding.
 *
 * <p>
 * Traceability: REQ-ETS-PART2-011,
 * SCENARIO-ETS-PART2-011-SWETEXT-CONFORMANCE-DECLARED-001,
 * SCENARIO-ETS-PART2-011-SWE-TEXT-ENCODING-RULES-PREREQUISITE-001,
 * SCENARIO-ETS-PART2-011-RESOURCE-CONDITION-GATES-001,
 * SCENARIO-ETS-PART2-011-MEDIATYPE-READ-001,
 * SCENARIO-ETS-PART2-011-SCHEMA-VALIDATION-READONLY-001,
 * SCENARIO-ETS-PART2-011-SCHEMA-MAPPING-TIME-001,
 * SCENARIO-ETS-PART2-011-OBSERVATION-COMMAND-ENCODING-GUARDS-001,
 * SCENARIO-ETS-PART2-011-MEDIATYPE-WRITE-ADVERTISEMENT-001,
 * SCENARIO-ETS-PART2-011-ANNEX-MEDIATYPE-HONESTY-001,
 * SCENARIO-ETS-PART2-011-UNAVAILABLE-ENDPOINT-HONESTY-001, and
 * SCENARIO-ETS-PART2-011-SMOKE-NO-PUBLIC-MUTATION-001.
 * </p>
 */
public class VerifyPart2SweCommonTextTests {

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
		// REQ-ETS-PART2-011, SCENARIO-ETS-PART2-011-ANNEX-MEDIATYPE-HONESTY-001:
		// Annex A.11 PASS evidence is exact application/swe+text, not CSV, binary,
		// JSON, vendor, or auto/fallback media.
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
		// REQ-ETS-PART2-011, SCENARIO-ETS-PART2-011-UNAVAILABLE-ENDPOINT-HONESTY-001:
		// schema metadata may be JSON, but text payload media, auto, HTML, and missing
		// content types cannot be converted into schema PASS evidence.
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
	}

	@Test
	public void apiDefinitionWriteAdvertisementRequiresPostOrPutApplicationSweTextRequestBody() {
		Map<String, Object> apiDefinition = Map.of("paths", Map.of("/datastreams/{datastreamId}/observations", Map.of(
				"post",
				Map.of("requestBody", Map.of("content", Map.of("application/swe+text", Map.of("schema", Map.of())))))));

		assertTrue(Part2SweCommonTextTests.apiDefinitionAdvertisesSweTextWrite(apiDefinition));
	}

	@Test
	public void apiDefinitionDoesNotPassFromOptionsJsonFallbackVendorDraftOrUnrelatedPaths() {
		// REQ-ETS-PART2-011, SCENARIO-ETS-PART2-011-ANNEX-MEDIATYPE-HONESTY-001,
		// SCENARIO-ETS-PART2-011-UNAVAILABLE-ENDPOINT-HONESTY-001: mediatype-write
		// cannot PASS from OPTIONS, JSON/CSV/binary/vendor media, unrelated paths, or
		// subresource evidence.
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

		assertFalse(Part2SweCommonTextTests.apiDefinitionAdvertisesSweTextWrite(optionsOnly));
		assertFalse(Part2SweCommonTextTests.apiDefinitionAdvertisesSweTextWrite(jsonOnly));
		assertFalse(Part2SweCommonTextTests.apiDefinitionAdvertisesSweTextWrite(vendorDraftOnly));
		assertFalse(Part2SweCommonTextTests.apiDefinitionAdvertisesSweTextWrite(csvOnly));
		assertFalse(Part2SweCommonTextTests.apiDefinitionAdvertisesSweTextWrite(binaryOnly));
		assertFalse(Part2SweCommonTextTests.apiDefinitionAdvertisesSweTextWrite(sweJsonOnly));
		assertFalse(Part2SweCommonTextTests.apiDefinitionAdvertisesSweTextWrite(unrelatedSweText));
		assertFalse(Part2SweCommonTextTests.apiDefinitionAdvertisesSweTextWrite(commandStatusSubresource));
		assertTrue(Part2SweCommonTextTests.isObservationOrCommandResourcePath("/observations"));
		assertTrue(Part2SweCommonTextTests.isObservationOrCommandResourcePath("/observations/{obsId}"));
		assertTrue(Part2SweCommonTextTests.isObservationOrCommandResourcePath("/controlstreams/{csId}/commands"));
		assertTrue(Part2SweCommonTextTests.isObservationOrCommandResourcePath("/commands/{commandId}"));
		assertFalse(Part2SweCommonTextTests.isObservationOrCommandResourcePath("/commands/{commandId}/status"));
		assertFalse(Part2SweCommonTextTests.isObservationOrCommandResourcePath("/systems/{systemId}/datastreams"));
	}

	@Test
	public void groupNameIsStableForTestNgWiring() {
		assertEquals("part2swecommontext", Part2SweCommonTextTests.GROUP);
	}

}
