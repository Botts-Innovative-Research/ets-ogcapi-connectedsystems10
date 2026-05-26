package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.swecommonjson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Regression coverage for S-ETS-29-01 Part 2 SWE Common JSON Encoding.
 *
 * <p>
 * Traceability: REQ-ETS-PART2-010,
 * SCENARIO-ETS-PART2-010-SWEJSON-CONFORMANCE-DECLARED-001,
 * SCENARIO-ETS-PART2-010-SWE-JSON-ENCODING-RULES-PREREQUISITE-001,
 * SCENARIO-ETS-PART2-010-RESOURCE-CONDITION-GATES-001,
 * SCENARIO-ETS-PART2-010-MEDIATYPE-READ-001,
 * SCENARIO-ETS-PART2-010-SCHEMA-VALIDATION-READONLY-001,
 * SCENARIO-ETS-PART2-010-SCHEMA-MAPPING-TIME-001,
 * SCENARIO-ETS-PART2-010-OBSERVATION-COMMAND-ENCODING-GUARDS-001,
 * SCENARIO-ETS-PART2-010-MEDIATYPE-WRITE-ADVERTISEMENT-001, and
 * SCENARIO-ETS-PART2-010-SMOKE-NO-PUBLIC-MUTATION-001.
 * </p>
 */
public class VerifyPart2SweCommonJsonTests {

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

		assertTrue(Part2SweCommonJsonTests.apiDefinitionAdvertisesSweJsonWrite(apiDefinition));
	}

	@Test
	public void apiDefinitionDoesNotPassFromOptionsJsonFallbackVendorDraftOrUnrelatedPaths() {
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

		assertFalse(Part2SweCommonJsonTests.apiDefinitionAdvertisesSweJsonWrite(optionsOnly));
		assertFalse(Part2SweCommonJsonTests.apiDefinitionAdvertisesSweJsonWrite(jsonOnly));
		assertFalse(Part2SweCommonJsonTests.apiDefinitionAdvertisesSweJsonWrite(vendorDraftOnly));
		assertFalse(Part2SweCommonJsonTests.apiDefinitionAdvertisesSweJsonWrite(unrelatedSweJson));
		assertFalse(Part2SweCommonJsonTests.apiDefinitionAdvertisesSweJsonWrite(commandStatusSubresource));
		assertTrue(Part2SweCommonJsonTests.isObservationOrCommandResourcePath("/observations"));
		assertTrue(Part2SweCommonJsonTests.isObservationOrCommandResourcePath("/observations/{obsId}"));
		assertTrue(Part2SweCommonJsonTests.isObservationOrCommandResourcePath("/controlstreams/{csId}/commands"));
		assertTrue(Part2SweCommonJsonTests.isObservationOrCommandResourcePath("/commands/{commandId}"));
		assertFalse(Part2SweCommonJsonTests.isObservationOrCommandResourcePath("/commands/{commandId}/status"));
		assertFalse(Part2SweCommonJsonTests.isObservationOrCommandResourcePath("/systems/{systemId}/datastreams"));
	}

	@Test
	public void groupNameIsStableForTestNgWiring() {
		assertEquals("part2swecommonjson", Part2SweCommonJsonTests.GROUP);
	}

}
