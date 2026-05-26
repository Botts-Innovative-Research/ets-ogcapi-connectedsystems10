package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Regression coverage for S-ETS-28-01 Part 2 JSON Encoding.
 *
 * <p>
 * Traceability: REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-JSON-CONFORMANCE-DECLARED-001,
 * SCENARIO-ETS-PART2-009-SWE-PREREQUISITE-VISIBLE-001,
 * SCENARIO-ETS-PART2-009-RESOURCE-CONDITION-GATES-001,
 * SCENARIO-ETS-PART2-009-MEDIATYPE-READ-001,
 * SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001,
 * SCENARIO-ETS-PART2-009-OBSERVATION-COMMAND-CONSTRAINTS-001,
 * SCENARIO-ETS-PART2-009-MEDIATYPE-WRITE-ADVERTISEMENT-001, and
 * SCENARIO-ETS-PART2-009-SMOKE-NO-PUBLIC-MUTATION-001.
 * </p>
 */
public class VerifyPart2JsonTests {

	@Test
	public void officialPart2JsonIdentifiersAreExposed() {
		String joined = String.join(" ", Part2JsonTests.CONF_JSON, Part2JsonTests.REQ_JSON,
				Part2JsonTests.REQ_MEDIATYPE_READ, Part2JsonTests.REQ_MEDIATYPE_WRITE,
				Part2JsonTests.REQ_DATASTREAM_SCHEMA, Part2JsonTests.REQ_COMMANDRESULT_CONSTRAINTS,
				Part2JsonTests.CONF_SWE_JSON_RECORD_COMPONENTS);

		assertTrue(joined.contains("ogcapi-connectedsystems-2/1.0/conf/json"));
		assertTrue(joined.contains("ogcapi-connectedsystems-2/1.0/req/json"));
		assertTrue(joined.contains("SWE/3.0/conf/json-record-components"));
		assertFalse(joined.contains("ogcapi-connectedsystems-1/1.0/conf/json"));
		assertFalse(joined.contains("req/json-2"));
	}

	@Test
	public void conditionGateMatrixReportsMissingClasses() {
		Map<String, Object> jsonOnly = Map.of("conformsTo", List.of(Part2JsonTests.CONF_JSON));

		List<String> missing = Part2JsonTests.missingConditionClasses(jsonOnly);

		assertEquals(3, missing.size());
		assertTrue(missing.contains(Part2JsonTests.missingConditionMessage(Part2JsonTests.CONF_DATASTREAM,
				"Requirements 95-98 DataStream/Observation JSON")));
		assertTrue(missing.contains(Part2JsonTests.missingConditionMessage(Part2JsonTests.CONF_CONTROLSTREAM,
				"Requirements 99-105 ControlStream/Command JSON")));
		assertTrue(missing.contains(Part2JsonTests.missingConditionMessage(Part2JsonTests.CONF_SYSTEM_EVENT,
				"Requirement 106 SystemEvent JSON")));

		Map<String, Object> complete = Map.of("conformsTo", List.of(Part2JsonTests.CONF_JSON,
				Part2JsonTests.CONF_DATASTREAM, Part2JsonTests.CONF_CONTROLSTREAM, Part2JsonTests.CONF_SYSTEM_EVENT));
		assertTrue(Part2JsonTests.missingConditionClasses(complete).isEmpty());
	}

	@Test
	public void jsonCompatibleContentTypeRequiresJsonMediaType() {
		assertTrue(Part2JsonTests.isJsonCompatibleContentType("application/json"));
		assertTrue(Part2JsonTests.isJsonCompatibleContentType("application/json; charset=utf-8"));
		assertTrue(Part2JsonTests.isJsonCompatibleContentType("application/vnd.oai.openapi+json"));
		assertFalse(Part2JsonTests.isJsonCompatibleContentType("auto"));
		assertFalse(Part2JsonTests.isJsonCompatibleContentType("text/html"));
		assertFalse(Part2JsonTests.isJsonCompatibleContentType(null));
	}

	@Test
	public void allAnnexA9SchemaResourcesAreBundled() {
		for (String schemaFile : Part2JsonTests.ANNEX_A9_SCHEMA_FILES) {
			assertTrue("Missing schema resource " + schemaFile, Part2JsonTests.schemaResourceExists(schemaFile));
			assertTrue(Part2JsonTests.schemaIri(schemaFile)
				.startsWith("https://csapi-compliance.local/schemas/connected-systems-2/json/"));
		}
	}

	@Test
	public void annexA9SchemasLoadThroughClasspathMapper() {
		for (String schemaFile : Part2JsonTests.ANNEX_A9_SCHEMA_FILES) {
			assertTrue("Schema did not load through classpath mapper: " + schemaFile,
					Part2JsonTests.schemaLoads(schemaFile));
		}
	}

	@Test
	public void apiDefinitionWriteAdvertisementRequiresPostOrPutApplicationJsonRequestBody() {
		Map<String, Object> apiDefinition = Map.of("paths", Map.of("/systems/{systemId}/datastreams", Map.of("post",
				Map.of("requestBody", Map.of("content", Map.of("application/json", Map.of("schema", Map.of())))))));

		assertTrue(Part2JsonTests.apiDefinitionAdvertisesJsonWrite(apiDefinition));
	}

	@Test
	public void apiDefinitionDoesNotPassFromOptionsOnlyOrOtherJsonMediaTypes() {
		Map<String, Object> optionsOnly = Map.of("paths", Map.of("/systems/{systemId}/datastreams",
				Map.of("options", Map.of("requestBody", Map.of("content", Map.of("application/json", Map.of()))))));

		assertFalse(Part2JsonTests.apiDefinitionAdvertisesJsonWrite(optionsOnly));

		Map<String, Object> geoJsonOnly = Map.of("paths", Map.of("/systems/{systemId}/datastreams",
				Map.of("post", Map.of("requestBody", Map.of("content", Map.of("application/geo+json", Map.of()))))));
		assertFalse(Part2JsonTests.apiDefinitionAdvertisesJsonWrite(geoJsonOnly));

		Map<String, Object> parameterizedJson = Map.of("paths", Map.of("/datastreams/{id}", Map.of("put",
				Map.of("requestBody", Map.of("content", Map.of("application/json; charset=utf-8", Map.of()))))));
		assertFalse(Part2JsonTests.apiDefinitionAdvertisesJsonWrite(parameterizedJson));
	}

	@Test
	public void groupNameIsStableForTestNgWiring() {
		assertEquals("part2json", Part2JsonTests.GROUP);
	}

}
