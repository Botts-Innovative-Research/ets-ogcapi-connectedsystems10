package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.swecommonbinary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Regression coverage for S-ETS-31-01 Part 2 SWE Common Binary Encoding.
 *
 * <p>
 * Traceability: REQ-ETS-PART2-012,
 * SCENARIO-ETS-PART2-012-SWEBINARY-CONFORMANCE-DECLARED-001,
 * SCENARIO-ETS-PART2-012-SWE-BINARY-ENCODING-RULES-PREREQUISITE-001,
 * SCENARIO-ETS-PART2-012-RESOURCE-CONDITION-GATES-001,
 * SCENARIO-ETS-PART2-012-MEDIATYPE-READ-001,
 * SCENARIO-ETS-PART2-012-SCHEMA-VALIDATION-READONLY-001,
 * SCENARIO-ETS-PART2-012-SCHEMA-MAPPING-TIME-001,
 * SCENARIO-ETS-PART2-012-OBSERVATION-COMMAND-ENCODING-GUARDS-001,
 * SCENARIO-ETS-PART2-012-MEDIATYPE-WRITE-ADVERTISEMENT-001,
 * SCENARIO-ETS-PART2-012-SOURCE-TYPO-HONESTY-001,
 * SCENARIO-ETS-PART2-012-UNAVAILABLE-ENDPOINT-HONESTY-001, and
 * SCENARIO-ETS-PART2-012-SMOKE-NO-PUBLIC-MUTATION-001.
 * </p>
 */
public class VerifyPart2SweCommonBinaryTests {

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
		// REQ-ETS-PART2-012, SCENARIO-ETS-PART2-012-SOURCE-TYPO-HONESTY-001:
		// Annex A.12 PASS evidence is exact application/swe+binary plus BinaryEncoding
		// evidence, not CSV, text, JSON, vendor, or auto/fallback media.
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
		// REQ-ETS-PART2-012, SCENARIO-ETS-PART2-012-UNAVAILABLE-ENDPOINT-HONESTY-001:
		// schema metadata may be JSON, but binary payload media, auto, HTML, and missing
		// content types cannot be converted into schema PASS evidence.
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
	}

	@Test
	public void apiDefinitionWriteAdvertisementRequiresPostOrPutApplicationSweBinaryRequestBody() {
		Map<String, Object> apiDefinition = Map.of("paths",
				Map.of("/datastreams/{datastreamId}/observations", Map.of("post", Map.of("requestBody",
						Map.of("content", Map.of("application/swe+binary", Map.of("schema", Map.of())))))));

		assertTrue(Part2SweCommonBinaryTests.apiDefinitionAdvertisesSweBinaryWrite(apiDefinition));
	}

	@Test
	public void apiDefinitionDoesNotPassFromOptionsJsonFallbackVendorDraftOrUnrelatedPaths() {
		// REQ-ETS-PART2-012, SCENARIO-ETS-PART2-012-SOURCE-TYPO-HONESTY-001,
		// SCENARIO-ETS-PART2-012-UNAVAILABLE-ENDPOINT-HONESTY-001: mediatype-write
		// cannot PASS from OPTIONS, JSON/CSV/text/vendor media, unrelated paths, or
		// subresource evidence.
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

		assertFalse(Part2SweCommonBinaryTests.apiDefinitionAdvertisesSweBinaryWrite(optionsOnly));
		assertFalse(Part2SweCommonBinaryTests.apiDefinitionAdvertisesSweBinaryWrite(jsonOnly));
		assertFalse(Part2SweCommonBinaryTests.apiDefinitionAdvertisesSweBinaryWrite(vendorDraftOnly));
		assertFalse(Part2SweCommonBinaryTests.apiDefinitionAdvertisesSweBinaryWrite(csvOnly));
		assertFalse(Part2SweCommonBinaryTests.apiDefinitionAdvertisesSweBinaryWrite(textOnly));
		assertFalse(Part2SweCommonBinaryTests.apiDefinitionAdvertisesSweBinaryWrite(sweJsonOnly));
		assertFalse(Part2SweCommonBinaryTests.apiDefinitionAdvertisesSweBinaryWrite(unrelatedSweBinary));
		assertFalse(Part2SweCommonBinaryTests.apiDefinitionAdvertisesSweBinaryWrite(commandStatusSubresource));
		assertTrue(Part2SweCommonBinaryTests.isObservationOrCommandResourcePath("/observations"));
		assertTrue(Part2SweCommonBinaryTests.isObservationOrCommandResourcePath("/observations/{obsId}"));
		assertTrue(Part2SweCommonBinaryTests.isObservationOrCommandResourcePath("/controlstreams/{csId}/commands"));
		assertTrue(Part2SweCommonBinaryTests.isObservationOrCommandResourcePath("/commands/{commandId}"));
		assertFalse(Part2SweCommonBinaryTests.isObservationOrCommandResourcePath("/commands/{commandId}/status"));
		assertFalse(Part2SweCommonBinaryTests.isObservationOrCommandResourcePath("/systems/{systemId}/datastreams"));
	}

	@Test
	public void groupNameIsStableForTestNgWiring() {
		assertEquals("part2swecommonbinary", Part2SweCommonBinaryTests.GROUP);
	}

}
