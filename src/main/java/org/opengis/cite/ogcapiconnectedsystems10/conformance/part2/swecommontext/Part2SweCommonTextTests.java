package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.swecommontext;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.Part2SchemaValidation;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.apicommon.Part2ApiCommonTests;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Part 2 - SWE Common Text Encoding read-only conformance subset
 * ({@code /conf/swecommon-text}; OGC 23-002 Clause 16.3 and Annex A.11).
 *
 * <p>
 * Implements the first <strong>REQ-ETS-PART2-011</strong> increment: exact declaration,
 * SWE Common 3.0 Text Encoding Rules prerequisite visibility, resource-class condition
 * gates, read-only {@code application/swe+text} schema/media checks, bundled SWE schema
 * validation, mapping evidence guards, and non-mutating mediatype-write advertisement
 * checks.
 *
 * Traceability also covers SCENARIO-ETS-PART2-011-ANNEX-MEDIATYPE-HONESTY-001 and
 * SCENARIO-ETS-PART2-011-UNAVAILABLE-ENDPOINT-HONESTY-001: exact
 * {@code application/swe+text} evidence is required, and reachable HTTP/schema/media
 * failures remain FAIL or SKIP rather than passing from declaration or sibling media.
 * </p>
 */
public class Part2SweCommonTextTests {

	public static final String GROUP = "part2swecommontext";

	public static final String CONF_SWE_COMMON_TEXT = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/swecommon-text";

	public static final String CONF_SWE_TEXT_ENCODING_RULES = "http://www.opengis.net/spec/SWE/3.0/conf/text-encoding-rules";

	public static final String CONF_DATASTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/datastream";

	public static final String CONF_CONTROLSTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/controlstream";

	public static final String CONF_CREATE_REPLACE_DELETE = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/create-replace-delete";

	public static final String REQ_SWE_COMMON_TEXT = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/swecommon-text";

	public static final String REQ_MEDIATYPE_READ = REQ_SWE_COMMON_TEXT + "/mediatype-read";

	public static final String REQ_MEDIATYPE_WRITE = REQ_SWE_COMMON_TEXT + "/mediatype-write";

	public static final String REQ_OBSSCHEMA_SCHEMA = REQ_SWE_COMMON_TEXT + "/obsschema-schema";

	public static final String REQ_OBSSCHEMA_MAPPING = REQ_SWE_COMMON_TEXT + "/obsschema-mapping";

	public static final String REQ_OBSERVATION_ENCODING = REQ_SWE_COMMON_TEXT + "/observation-encoding";

	public static final String REQ_COMMANDSCHEMA_SCHEMA = REQ_SWE_COMMON_TEXT + "/cmdschema-schema";

	public static final String REQ_COMMANDSCHEMA_MAPPING = REQ_SWE_COMMON_TEXT + "/cmdschema-mapping";

	public static final String REQ_COMMAND_ENCODING = REQ_SWE_COMMON_TEXT + "/command-encoding";

	public static final String SWE_TEXT_MEDIA_TYPE = "application/swe+text";

	public static final String OBSERVATION_SCHEMA_SWE = "observationSchemaSwe.json";

	public static final String COMMAND_SCHEMA_SWE = "commandSchemaSwe.json";

	public static final List<String> ANNEX_A11_SCHEMA_FILES = List.of(OBSERVATION_SCHEMA_SWE, COMMAND_SCHEMA_SWE);

	public static final Set<String> OBSERVATION_TIME_DEFINITIONS = Set.of("http://www.w3.org/ns/sosa/phenomenonTime",
			"http://www.opengis.net/def/property/OGC/0/SamplingTime", "http://www.w3.org/ns/sosa/resultTime");

	public static final String COMMAND_ISSUE_TIME_DEFINITION = "http://www.opengis.net/def/property/OGC/0/IssueTime";

	private static final String DATASTREAM_COLLECTION_SCHEMA = "dataStreamCollection.json";

	private static final String CONTROLSTREAM_COLLECTION_SCHEMA = "controlStreamCollection.json";

	private static final String COMMAND_COLLECTION_SCHEMA = "commandCollection.json";

	private static final String OBSERVATION_COLLECTION_SCHEMA = "observationCollection.json";

	private static final String SCHEMA_RESOURCE_PREFIX = "/schemas/connected-systems-2/json/";

	private static final String SCHEMA_IRI_PREFIX = "https://csapi-compliance.local/schemas/connected-systems-2/json/";

	private static final ObjectMapper JSON = new ObjectMapper();

	private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory
		.getInstance(SpecVersion.VersionFlag.V202012, builder -> builder.schemaMappers(
				mappers -> mappers.mapPrefix("https://csapi-compliance.local/schemas/", "classpath:schemas/")));

	private URI iutUri;

	private URI baseUri;

	private Response conformanceResponse;

	private Map<String, Object> conformanceBody;

	private Response landingResponse;

	private Map<String, Object> landingBody;

	/**
	 * Fetches shared read-only inputs once. This class intentionally never issues POST,
	 * PUT, PATCH, or DELETE.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchPart2SweCommonTextInputs(ITestContext testContext) {
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		this.iutUri = (URI) iutAttr;
		String iutString = this.iutUri.toString();
		this.baseUri = URI.create(iutString.endsWith("/") ? iutString : iutString + "/");

		this.conformanceResponse = given().accept("application/json")
			.when()
			.get(this.baseUri.resolve("conformance"))
			.andReturn();
		this.conformanceBody = parseBody(this.conformanceResponse);

		this.landingResponse = given().accept("application/json").when().get(this.iutUri).andReturn();
		this.landingBody = parseBody(this.landingResponse);
	}

	/**
	 * SCENARIO-ETS-PART2-011-SWETEXT-CONFORMANCE-DECLARED-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_SWE_COMMON_TEXT
			+ ": /conformance declares exact Part 2 /conf/swecommon-text before SWE Common Text assertions run (REQ-ETS-PART2-011, SCENARIO-ETS-PART2-011-SWETEXT-CONFORMANCE-DECLARED-001)",
			groups = GROUP)
	public void part2SweCommonTextConformanceDeclared() {
		ETSAssert.assertStatus(this.conformanceResponse, 200, REQ_SWE_COMMON_TEXT);
		if (this.conformanceBody == null) {
			ETSAssert.failWithUri(REQ_SWE_COMMON_TEXT, "/conformance body did not parse as JSON. Content-Type was: "
					+ this.conformanceResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.conformanceBody, "conformsTo", List.class, REQ_SWE_COMMON_TEXT);
		if (!declaresConformance(this.conformanceBody, CONF_SWE_COMMON_TEXT)) {
			throw new SkipException(CONF_SWE_COMMON_TEXT
					+ " - IUT does not declare the CS API Part 2 SWE Common Text Encoding conformance class. "
					+ "Sibling /conf/json, /conf/swecommon-json, /conf/swecommon-binary, or resource-class declarations are not PASS evidence.");
		}
	}

	/**
	 * SCENARIO-ETS-PART2-011-SWE-TEXT-ENCODING-RULES-PREREQUISITE-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_SWE_COMMON_TEXT
			+ ": SWE Common 3.0 Text Encoding Rules prerequisite is visible before full /conf/swecommon-text closure (REQ-ETS-PART2-011, SCENARIO-ETS-PART2-011-SWE-TEXT-ENCODING-RULES-PREREQUISITE-001)",
			groups = GROUP)
	public void sweTextEncodingRulesPrerequisiteVisibleForFullClosure() {
		skipIfSweCommonTextUndeclared();
		if (!declaresConformance(this.conformanceBody, CONF_SWE_TEXT_ENCODING_RULES)) {
			throw new SkipException(CONF_SWE_TEXT_ENCODING_RULES
					+ " - /req/swecommon-text lists SWE Common 3.0 Text Encoding Rules as a prerequisite. "
					+ "Scoped SWE Common Text resource checks may run, but full /conf/swecommon-text closure is prerequisite-incomplete.");
		}
		Reporter.log("IUT declares /conf/swecommon-text and the SWE Common 3.0 Text Encoding Rules prerequisite.",
				true);
	}

	/**
	 * SCENARIO-ETS-PART2-011-RESOURCE-CONDITION-GATES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_SWE_COMMON_TEXT
			+ ": SWE Common Text assertions are condition-gated on Part 2 Datastream, ControlStream, and Create/Replace/Delete classes (REQ-ETS-PART2-011, SCENARIO-ETS-PART2-011-RESOURCE-CONDITION-GATES-001)",
			groups = GROUP)
	public void sweCommonTextResourceConditionGatesAreVisible() {
		skipIfSweCommonTextUndeclared();
		List<String> missing = missingConditionClasses(this.conformanceBody);
		if (!missing.isEmpty()) {
			throw new SkipException(REQ_SWE_COMMON_TEXT
					+ " - SWE Common Text assertions are prerequisite-incomplete for missing condition classes: "
					+ String.join("; ", missing));
		}
		Reporter.log("Part 2 SWE Common Text condition classes are declared for Datastream, ControlStream, and CRD.",
				true);
	}

	/**
	 * SCENARIO-ETS-PART2-011-SCHEMA-VALIDATION-READONLY-001.
	 * SCENARIO-ETS-PART2-011-MEDIATYPE-READ-001.
	 * SCENARIO-ETS-PART2-011-ANNEX-MEDIATYPE-HONESTY-001.
	 * SCENARIO-ETS-PART2-011-UNAVAILABLE-ENDPOINT-HONESTY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_OBSSCHEMA_SCHEMA
			+ ": selected Datastream Observation Schema validates as SWE Common Text metadata with TextEncoding (REQ-ETS-PART2-011, SCENARIO-ETS-PART2-011-SCHEMA-VALIDATION-READONLY-001, SCENARIO-ETS-PART2-011-MEDIATYPE-READ-001)",
			groups = GROUP)
	public void observationSchemaSweTextValidWhenDatastreamCandidateAvailable() {
		Map<String, Object> schema = observationSweSchema(REQ_OBSSCHEMA_SCHEMA);
		validateJsonValueAgainstSchema(schema, OBSERVATION_SCHEMA_SWE, REQ_OBSSCHEMA_SCHEMA,
				"Observation Schema for obsFormat=application/swe+text");
		assertMediaMember(schema, "obsFormat", REQ_OBSSCHEMA_SCHEMA, "Observation Schema");
		assertRecordSchemaObject(schema, REQ_OBSSCHEMA_SCHEMA, "Observation Schema");
		assertTextEncoding(schema, REQ_OBSSCHEMA_SCHEMA, "Observation Schema");
	}

	/**
	 * SCENARIO-ETS-PART2-011-SCHEMA-MAPPING-TIME-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_OBSSCHEMA_MAPPING
			+ ": Observation Schema mapping requires canonical Time definition evidence from retrieved SWE Common recordSchema (REQ-ETS-PART2-011, SCENARIO-ETS-PART2-011-SCHEMA-MAPPING-TIME-001)",
			groups = GROUP)
	public void observationSchemaSweMappingRequiresTimeComponentEvidence() {
		Map<String, Object> schema = observationSweSchema(REQ_OBSSCHEMA_MAPPING);
		Object recordSchema = assertRecordSchemaObject(schema, REQ_OBSSCHEMA_MAPPING, "Observation Schema");
		if (!containsTimeComponentWithDefinition(recordSchema, OBSERVATION_TIME_DEFINITIONS)) {
			ETSAssert.failWithUri(REQ_OBSSCHEMA_MAPPING,
					"Observation Schema recordSchema does not expose a Time component with one of the canonical phenomenonTime, SamplingTime, or resultTime definition URIs.");
		}
	}

	/**
	 * SCENARIO-ETS-PART2-011-OBSERVATION-COMMAND-ENCODING-GUARDS-001.
	 * SCENARIO-ETS-PART2-011-MEDIATYPE-READ-001.
	 * SCENARIO-ETS-PART2-011-ANNEX-MEDIATYPE-HONESTY-001.
	 * SCENARIO-ETS-PART2-011-UNAVAILABLE-ENDPOINT-HONESTY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_OBSERVATION_ENCODING
			+ ": Observation SWE Common Text encoding requires parent schema, candidate Observation, and encoding-validator evidence before PASS (REQ-ETS-PART2-011, SCENARIO-ETS-PART2-011-OBSERVATION-COMMAND-ENCODING-GUARDS-001, SCENARIO-ETS-PART2-011-MEDIATYPE-READ-001)",
			groups = GROUP)
	public void observationSweTextEncodingRequiresParentSchemaAndCandidateEvidence() {
		Map<String, Object> schema = observationSweSchema(REQ_OBSERVATION_ENCODING);
		assertRecordSchemaObject(schema, REQ_OBSERVATION_ENCODING, "Observation Schema");
		assertTextEncoding(schema, REQ_OBSERVATION_ENCODING, "Observation Schema");
		SweCandidate observation = firstObservationEvidence(REQ_OBSERVATION_ENCODING);
		assertExactSweTextContentType(observation.response(), REQ_OBSERVATION_ENCODING, observation.source());
		if (observation.body().isBlank()) {
			throw new SkipException(REQ_OBSERVATION_ENCODING
					+ " - candidate Observation body is empty; no SWE Common Text encoding PASS was reported.");
		}
		throw new SkipException(REQ_OBSERVATION_ENCODING
				+ " - parent Observation Schema and candidate Observation evidence are present, but semantic validation against SWE Common Text encoding rules is deferred until a dedicated encoding validator is available; no shape-only PASS was reported.");
	}

	/**
	 * SCENARIO-ETS-PART2-011-SCHEMA-VALIDATION-READONLY-001.
	 * SCENARIO-ETS-PART2-011-MEDIATYPE-READ-001.
	 * SCENARIO-ETS-PART2-011-ANNEX-MEDIATYPE-HONESTY-001.
	 * SCENARIO-ETS-PART2-011-UNAVAILABLE-ENDPOINT-HONESTY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COMMANDSCHEMA_SCHEMA
			+ ": selected ControlStream Command Schema validates as SWE Common Text metadata with TextEncoding (REQ-ETS-PART2-011, SCENARIO-ETS-PART2-011-SCHEMA-VALIDATION-READONLY-001, SCENARIO-ETS-PART2-011-MEDIATYPE-READ-001)",
			groups = GROUP)
	public void commandSchemaSweTextValidWhenControlStreamCandidateAvailable() {
		Map<String, Object> schema = commandSweSchema(REQ_COMMANDSCHEMA_SCHEMA);
		validateJsonValueAgainstSchema(schema, COMMAND_SCHEMA_SWE, REQ_COMMANDSCHEMA_SCHEMA,
				"Command Schema for cmdFormat=application/swe+text");
		assertMediaMember(schema, "commandFormat", REQ_COMMANDSCHEMA_SCHEMA, "Command Schema");
		assertRecordSchemaObject(schema, REQ_COMMANDSCHEMA_SCHEMA, "Command Schema");
		assertTextEncoding(schema, REQ_COMMANDSCHEMA_SCHEMA, "Command Schema");
	}

	/**
	 * SCENARIO-ETS-PART2-011-SCHEMA-MAPPING-TIME-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COMMANDSCHEMA_MAPPING
			+ ": Command Schema mapping requires canonical IssueTime Time component evidence when issue-time mapping is present (REQ-ETS-PART2-011, SCENARIO-ETS-PART2-011-SCHEMA-MAPPING-TIME-001)",
			groups = GROUP)
	public void commandSchemaSweMappingRequiresIssueTimeEvidenceWhenPresent() {
		Map<String, Object> schema = commandSweSchema(REQ_COMMANDSCHEMA_MAPPING);
		Object recordSchema = assertRecordSchemaObject(schema, REQ_COMMANDSCHEMA_MAPPING, "Command Schema");
		if (!containsIssueTimeComponentWithCanonicalDefinition(recordSchema)) {
			throw new SkipException(REQ_COMMANDSCHEMA_MAPPING
					+ " - retrieved Command Schema does not expose a Time component with canonical IssueTime definition evidence; no command issue-time mapping PASS was reported.");
		}
	}

	/**
	 * SCENARIO-ETS-PART2-011-OBSERVATION-COMMAND-ENCODING-GUARDS-001.
	 * SCENARIO-ETS-PART2-011-MEDIATYPE-READ-001.
	 * SCENARIO-ETS-PART2-011-ANNEX-MEDIATYPE-HONESTY-001.
	 * SCENARIO-ETS-PART2-011-UNAVAILABLE-ENDPOINT-HONESTY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COMMAND_ENCODING
			+ ": Command SWE Common Text encoding requires parent schema, candidate Command, and encoding-validator evidence before PASS (REQ-ETS-PART2-011, SCENARIO-ETS-PART2-011-OBSERVATION-COMMAND-ENCODING-GUARDS-001, SCENARIO-ETS-PART2-011-MEDIATYPE-READ-001)",
			groups = GROUP)
	public void commandSweTextEncodingRequiresParentSchemaAndCandidateEvidence() {
		Map<String, Object> schema = commandSweSchema(REQ_COMMAND_ENCODING);
		assertRecordSchemaObject(schema, REQ_COMMAND_ENCODING, "Command Schema");
		assertTextEncoding(schema, REQ_COMMAND_ENCODING, "Command Schema");
		SweCandidate command = firstCommandEvidence(REQ_COMMAND_ENCODING);
		assertExactSweTextContentType(command.response(), REQ_COMMAND_ENCODING, command.source());
		if (command.body().isBlank()) {
			throw new SkipException(REQ_COMMAND_ENCODING
					+ " - candidate Command body is empty; no SWE Common Text encoding PASS was reported.");
		}
		throw new SkipException(REQ_COMMAND_ENCODING
				+ " - parent Command Schema and candidate Command evidence are present, but semantic validation against SWE Common Text encoding rules is deferred until a dedicated encoding validator is available; no shape-only PASS was reported.");
	}

	/**
	 * SCENARIO-ETS-PART2-011-MEDIATYPE-WRITE-ADVERTISEMENT-001.
	 * SCENARIO-ETS-PART2-011-SMOKE-NO-PUBLIC-MUTATION-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_MEDIATYPE_WRITE
			+ ": SWE Common Text write media type support is checked only from non-mutating API definition operation metadata, never OPTIONS alone or public-IUT mutation (REQ-ETS-PART2-011, SCENARIO-ETS-PART2-011-MEDIATYPE-WRITE-ADVERTISEMENT-001, SCENARIO-ETS-PART2-011-SMOKE-NO-PUBLIC-MUTATION-001)",
			groups = GROUP)
	public void sweCommonTextMediatypeWriteAdvertisedByApiDefinitionOnly() {
		skipIfSweCommonTextUndeclared();
		skipIfConditionClassUndeclared(CONF_CREATE_REPLACE_DELETE,
				"Requirement 116 applies only when Part 2 Create/Replace/Delete is declared.");
		Map<String, Object> apiDefinition = readJsonApiDefinitionOrSkip();
		if (!apiDefinitionAdvertisesSweTextWrite(apiDefinition)) {
			throw new SkipException(REQ_MEDIATYPE_WRITE
					+ " - API definition does not advertise application/swe+text requestBody content for POST or PUT. OPTIONS evidence alone is not mediatype-write PASS evidence; no POST/PUT/PATCH/DELETE request was issued.");
		}
		Reporter.log(
				"API definition advertises application/swe+text requestBody content for a create/replace operation; no mutation request was issued.",
				true);
	}

	static boolean declaresConformance(Map<String, Object> body, String conformanceUri) {
		return Part2ApiCommonTests.declaresConformance(body, conformanceUri);
	}

	static List<String> missingConditionClasses(Map<String, Object> body) {
		List<String> missing = new ArrayList<>();
		if (!declaresConformance(body, CONF_DATASTREAM)) {
			missing.add(missingConditionMessage(CONF_DATASTREAM,
					"Requirements 117-119 Observation Schema and Observation SWE Common Text"));
		}
		if (!declaresConformance(body, CONF_CONTROLSTREAM)) {
			missing.add(missingConditionMessage(CONF_CONTROLSTREAM,
					"Requirements 120-122 Command Schema and Command SWE Common Text"));
		}
		if (!declaresConformance(body, CONF_CREATE_REPLACE_DELETE)) {
			missing.add(missingConditionMessage(CONF_CREATE_REPLACE_DELETE,
					"Requirement 116 SWE Common Text mediatype-write"));
		}
		return missing;
	}

	static String missingConditionMessage(String conformanceClass, String requirementGroup) {
		return requirementGroup + " requires " + conformanceClass;
	}

	static boolean isJsonCompatibleContentType(String contentType) {
		if (contentType == null || contentType.isBlank()) {
			return false;
		}
		String mediaType = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
		return "application/json".equals(mediaType) || mediaType.endsWith("+json");
	}

	static boolean isExactSweTextContentType(String contentType) {
		if (contentType == null || contentType.isBlank()) {
			return false;
		}
		String mediaType = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
		return SWE_TEXT_MEDIA_TYPE.equals(mediaType);
	}

	static String schemaIri(String schemaFile) {
		return SCHEMA_IRI_PREFIX + schemaFile;
	}

	static boolean schemaResourceExists(String schemaFile) {
		try (var in = Part2SweCommonTextTests.class.getResourceAsStream(SCHEMA_RESOURCE_PREFIX + schemaFile)) {
			return in != null;
		}
		catch (IOException ex) {
			return false;
		}
	}

	static boolean schemaLoads(String schemaFile) {
		try {
			Part2SchemaValidation.getSchema(SCHEMA_FACTORY, schemaIri(schemaFile));
			return true;
		}
		catch (RuntimeException ex) {
			return false;
		}
	}

	static boolean apiDefinitionAdvertisesSweTextWrite(Map<String, Object> apiDefinition) {
		if (apiDefinition == null) {
			return false;
		}
		Set<Map<?, ?>> operations = writeOperations(apiDefinition);
		return operations.stream().anyMatch(Part2SweCommonTextTests::requestBodyContainsApplicationSweText);
	}

	static boolean schemaHasTextEncoding(Map<String, Object> schema) {
		if (schema == null || !(schema.get("encoding") instanceof Map)) {
			return false;
		}
		Object type = ((Map<?, ?>) schema.get("encoding")).get("type");
		return "TextEncoding".equals(type);
	}

	static boolean containsTimeComponentWithDefinition(Object value, Set<String> canonicalDefinitions) {
		return containsTimeComponentWithDefinition(value, canonicalDefinitions, false);
	}

	static boolean containsIssueTimeComponentWithCanonicalDefinition(Object value) {
		return containsTimeComponentWithDefinition(value, Set.of(COMMAND_ISSUE_TIME_DEFINITION));
	}

	private Map<String, Object> observationSweSchema(String reqUri) {
		skipIfSweCommonTextUndeclared();
		skipIfConditionClassUndeclared(CONF_DATASTREAM,
				"Observation-side SWE Common Text assertions require the Part 2 Datastream class.");
		String datastreamId = requireString(
				firstRequiredCollectionResource("datastreams", reqUri, DATASTREAM_COLLECTION_SCHEMA, "/datastreams"),
				"id", reqUri);
		return requiredJsonObject("datastreams/" + datastreamId + "/schema?obsFormat=application/swe+text", reqUri,
				"/datastreams/" + datastreamId + "/schema?obsFormat=application/swe+text");
	}

	private Map<String, Object> commandSweSchema(String reqUri) {
		skipIfSweCommonTextUndeclared();
		skipIfConditionClassUndeclared(CONF_CONTROLSTREAM,
				"Command-side SWE Common Text assertions require the Part 2 ControlStream class.");
		String controlStreamId = requireString(firstRequiredCollectionResource("controlstreams", reqUri,
				CONTROLSTREAM_COLLECTION_SCHEMA, "/controlstreams"), "id", reqUri);
		return requiredJsonObject("controlstreams/" + controlStreamId + "/schema?cmdFormat=application/swe+text",
				reqUri, "/controlstreams/" + controlStreamId + "/schema?cmdFormat=application/swe+text");
	}

	private SweCandidate firstObservationEvidence(String reqUri) {
		Map<String, Object> datastream = firstRequiredCollectionResource("datastreams", reqUri,
				DATASTREAM_COLLECTION_SCHEMA, "/datastreams");
		String datastreamId = requireString(datastream, "id", reqUri);
		SweCandidate nested = firstOptionalSweTextCandidate("datastreams/" + datastreamId + "/observations?limit=1",
				reqUri, "/datastreams/" + datastreamId + "/observations");
		if (nested != null) {
			return nested;
		}
		SweCandidate global = firstOptionalSweTextCandidate("observations?limit=1", reqUri, "/observations");
		if (global != null) {
			return global;
		}
		throw new SkipException(reqUri + " - neither /datastreams/" + datastreamId
				+ "/observations nor /observations exposed a candidate Observation resource for application/swe+text.");
	}

	private SweCandidate firstCommandEvidence(String reqUri) {
		Map<String, Object> controlStream = firstRequiredCollectionResource("controlstreams", reqUri,
				CONTROLSTREAM_COLLECTION_SCHEMA, "/controlstreams");
		String controlStreamId = requireString(controlStream, "id", reqUri);
		SweCandidate nested = firstOptionalSweTextCandidate("controlstreams/" + controlStreamId + "/commands?limit=1",
				reqUri, "/controlstreams/" + controlStreamId + "/commands");
		if (nested != null) {
			return nested;
		}
		SweCandidate global = firstOptionalSweTextCandidate("commands?limit=1", reqUri, "/commands");
		if (global != null) {
			return global;
		}
		throw new SkipException(reqUri + " - neither /controlstreams/" + controlStreamId
				+ "/commands nor /commands exposed a candidate Command resource for application/swe+text.");
	}

	private SweCandidate firstOptionalSweTextCandidate(String pathWithQuery, String reqUri, String source) {
		Response response = given().accept(SWE_TEXT_MEDIA_TYPE)
			.when()
			.get(this.baseUri.resolve(pathWithQuery))
			.andReturn();
		if (response.getStatusCode() != 200) {
			return null;
		}
		assertExactSweTextContentType(response, reqUri, source);
		String body = response.getBody() == null ? "" : response.getBody().asString();
		return new SweCandidate(body, response, source);
	}

	private Map<String, Object> firstRequiredCollectionResource(String path, String reqUri, String collectionSchema,
			String source) {
		Response response = given().accept("application/json")
			.queryParam("limit", 1)
			.when()
			.get(this.baseUri.resolve(path))
			.andReturn();
		Map<String, Object> body = assertRequiredJsonResponse(response, reqUri, source);
		validateResponseAgainstSchema(response, collectionSchema, reqUri, source);
		List<?> items = items(body);
		if (items.isEmpty()) {
			throw new SkipException(reqUri + " - " + source
					+ " returned an empty collection; no candidate resource is available for SWE Common Text PASS.");
		}
		Object first = items.get(0);
		if (!(first instanceof Map)) {
			ETSAssert.failWithUri(reqUri, source + " first item was not a JSON object: " + first);
		}
		return castMap(first);
	}

	private Map<String, Object> requiredJsonObject(String pathWithQuery, String reqUri, String source) {
		Response response = given().accept("application/json")
			.when()
			.get(this.baseUri.resolve(pathWithQuery))
			.andReturn();
		return assertRequiredJsonResponse(response, reqUri, source);
	}

	private Map<String, Object> readJsonApiDefinitionOrSkip() {
		if (this.landingResponse.getStatusCode() != 200 || this.landingBody == null) {
			throw new SkipException(REQ_MEDIATYPE_WRITE
					+ " - landing page is not readable JSON, so no service-desc API definition can be inspected.");
		}
		URI serviceDescUri = serviceDescUri();
		if (serviceDescUri == null) {
			throw new SkipException(REQ_MEDIATYPE_WRITE
					+ " - landing page does not expose a rel=service-desc link. service-doc/OPTIONS evidence is not mediatype-write PASS evidence.");
		}
		Response response = given().accept("application/vnd.oai.openapi+json, application/json")
			.when()
			.get(serviceDescUri)
			.andReturn();
		if (response.getStatusCode() != 200) {
			throw new SkipException(REQ_MEDIATYPE_WRITE + " - service-desc API definition returned HTTP "
					+ response.getStatusCode() + "; no write media type advertisement PASS was reported.");
		}
		Map<String, Object> body = parseBody(response);
		if (body == null) {
			throw new SkipException(REQ_MEDIATYPE_WRITE
					+ " - service-desc API definition did not parse as JSON; no write media type advertisement PASS was reported.");
		}
		return body;
	}

	private URI serviceDescUri() {
		for (Object link : links(this.landingBody)) {
			if (!(link instanceof Map)) {
				continue;
			}
			Map<?, ?> linkMap = (Map<?, ?>) link;
			if (!"service-desc".equals(linkMap.get("rel"))) {
				continue;
			}
			Object href = linkMap.get("href");
			if (href instanceof String && !((String) href).isBlank()) {
				return this.baseUri.resolve((String) href);
			}
		}
		return null;
	}

	private void skipIfSweCommonTextUndeclared() {
		if (!declaresConformance(this.conformanceBody, CONF_SWE_COMMON_TEXT)) {
			throw new SkipException(CONF_SWE_COMMON_TEXT
					+ " - IUT does not declare the CS API Part 2 SWE Common Text Encoding conformance class in /conformance.");
		}
	}

	private void skipIfConditionClassUndeclared(String conformanceClass, String reason) {
		if (!declaresConformance(this.conformanceBody, conformanceClass)) {
			throw new SkipException(
					conformanceClass + " - " + reason + " No SWE Common Text Encoding PASS evidence was reported.");
		}
	}

	private Map<String, Object> assertRequiredJsonResponse(Response response, String reqUri, String source) {
		ETSAssert.assertStatus(response, 200, reqUri);
		assertJsonContentType(response, reqUri, source);
		Map<String, Object> body = parseBody(response);
		if (body == null) {
			ETSAssert.failWithUri(reqUri,
					source + " body did not parse as JSON. Content-Type was: " + response.getContentType());
		}
		return body;
	}

	private static void assertJsonContentType(Response response, String reqUri, String source) {
		String contentType = response.getContentType();
		if (!isJsonCompatibleContentType(contentType)) {
			ETSAssert.failWithUri(reqUri, source + " returned Content-Type '" + contentType
					+ "'; expected application/json or another +json media type for schema metadata.");
		}
	}

	private static void assertExactSweTextContentType(Response response, String reqUri, String source) {
		String contentType = response.getContentType();
		if (!isExactSweTextContentType(contentType)) {
			ETSAssert.failWithUri(reqUri, source + " returned Content-Type '" + contentType
					+ "'; expected exact application/swe+text for SWE Common Text resource encoding.");
		}
	}

	private static void validateResponseAgainstSchema(Response response, String schemaFile, String reqUri,
			String source) {
		validateJsonTextAgainstSchema(response.getBody().asString(), schemaFile, reqUri, source);
	}

	private static void validateJsonValueAgainstSchema(Object value, String schemaFile, String reqUri, String source) {
		try {
			JsonNode node = JSON.valueToTree(value);
			validateJsonNodeAgainstSchema(node, schemaFile, reqUri, source);
		}
		catch (IllegalArgumentException ex) {
			ETSAssert.failWithUri(reqUri, source + " could not be converted for schema validation against " + schemaFile
					+ ": " + ex.getMessage());
		}
	}

	private static void validateJsonTextAgainstSchema(String jsonText, String schemaFile, String reqUri,
			String source) {
		try {
			JsonNode node = JSON.readTree(jsonText);
			validateJsonNodeAgainstSchema(node, schemaFile, reqUri, source);
		}
		catch (IOException ex) {
			ETSAssert.failWithUri(reqUri, source + " did not parse as JSON for schema validation against " + schemaFile
					+ ": " + ex.getMessage());
		}
	}

	private static void validateJsonNodeAgainstSchema(JsonNode node, String schemaFile, String reqUri, String source) {
		assertSchemaResourceBundled(schemaFile, reqUri);
		try {
			JsonSchema schema = Part2SchemaValidation.getSchema(SCHEMA_FACTORY, schemaIri(schemaFile));
			Set<ValidationMessage> errors = schema.validate(node);
			if (!errors.isEmpty()) {
				String joined = errors.stream()
					.limit(8)
					.map(ValidationMessage::getMessage)
					.collect(Collectors.joining("; "));
				ETSAssert.failWithUri(reqUri,
						source + " failed schema validation against " + schemaFile + ": " + joined);
			}
		}
		catch (RuntimeException ex) {
			ETSAssert.failWithUri(reqUri,
					source + " could not be schema-validated against " + schemaFile + ": " + ex.getMessage());
		}
		validateSweCommonRecordSchema(node, schemaFile, reqUri, source);
	}

	private static void validateSweCommonRecordSchema(JsonNode node, String schemaFile, String reqUri, String source) {
		if (!OBSERVATION_SCHEMA_SWE.equals(schemaFile) && !COMMAND_SCHEMA_SWE.equals(schemaFile)) {
			return;
		}
		Part2SchemaValidation.assertValidSweRecordSchema(node, reqUri, source);
	}

	private static void assertSchemaResourceBundled(String schemaFile, String reqUri) {
		if (!schemaResourceExists(schemaFile)) {
			ETSAssert.failWithUri(reqUri,
					"Bundled Part 2 SWE Common Text Schema is missing: " + SCHEMA_RESOURCE_PREFIX + schemaFile);
		}
	}

	private static void assertMediaMember(Map<String, Object> schema, String member, String reqUri, String label) {
		Object value = schema.get(member);
		if (!SWE_TEXT_MEDIA_TYPE.equals(value)) {
			ETSAssert.failWithUri(reqUri,
					label + " member '" + member + "' was '" + value + "'; expected application/swe+text.");
		}
	}

	private static Object assertRecordSchemaObject(Map<String, Object> schema, String reqUri, String label) {
		Object recordSchema = schema.get("recordSchema");
		if (!(recordSchema instanceof Map)) {
			ETSAssert.failWithUri(reqUri,
					label + " does not expose a SWE Common recordSchema object required for mapping checks.");
		}
		return recordSchema;
	}

	private static void assertTextEncoding(Map<String, Object> schema, String reqUri, String label) {
		if (!schemaHasTextEncoding(schema)) {
			ETSAssert.failWithUri(reqUri,
					label + " does not expose encoding.type=TextEncoding for application/swe+text.");
		}
	}

	private static Set<Map<?, ?>> writeOperations(Map<String, Object> apiDefinition) {
		Set<Map<?, ?>> operations = new LinkedHashSet<>();
		Object paths = apiDefinition.get("paths");
		if (!(paths instanceof Map)) {
			return operations;
		}
		for (Map.Entry<?, ?> pathEntry : ((Map<?, ?>) paths).entrySet()) {
			if (!isObservationOrCommandResourcePath(pathEntry.getKey())) {
				continue;
			}
			Object pathItem = pathEntry.getValue();
			if (!(pathItem instanceof Map)) {
				continue;
			}
			Map<?, ?> pathMap = (Map<?, ?>) pathItem;
			for (String method : List.of("post", "put")) {
				Object operation = pathMap.get(method);
				if (operation instanceof Map) {
					operations.add((Map<?, ?>) operation);
				}
			}
		}
		return operations;
	}

	static boolean isObservationOrCommandResourcePath(Object path) {
		if (!(path instanceof String) || ((String) path).isBlank()) {
			return false;
		}
		List<String> segments = List.of(((String) path).toLowerCase(Locale.ROOT).split("/"))
			.stream()
			.filter(segment -> !segment.isBlank())
			.toList();
		return isCollectionOrItemPath(segments, "observations") || isCollectionOrItemPath(segments, "commands");
	}

	private static boolean isCollectionOrItemPath(List<String> segments, String collectionName) {
		int index = segments.lastIndexOf(collectionName);
		if (index < 0) {
			return false;
		}
		int remaining = segments.size() - index - 1;
		return remaining == 0 || (remaining == 1 && isTemplateSegment(segments.get(index + 1)));
	}

	private static boolean isTemplateSegment(String segment) {
		return segment.startsWith("{") && segment.endsWith("}") && segment.length() > 2;
	}

	private static boolean requestBodyContainsApplicationSweText(Map<?, ?> operation) {
		Object requestBody = operation.get("requestBody");
		if (!(requestBody instanceof Map)) {
			return false;
		}
		Object content = ((Map<?, ?>) requestBody).get("content");
		return content instanceof Map
				&& ((Map<?, ?>) content).keySet().stream().anyMatch(Part2SweCommonTextTests::isSweTextMediaKey);
	}

	private static boolean isSweTextMediaKey(Object key) {
		if (!(key instanceof String)) {
			return false;
		}
		return SWE_TEXT_MEDIA_TYPE.equals(((String) key).trim().toLowerCase(Locale.ROOT));
	}

	@SuppressWarnings("unchecked")
	private static boolean containsTimeComponentWithDefinition(Object value, Set<String> canonicalDefinitions,
			boolean insideTimeComponent) {
		if (value instanceof Map) {
			Map<Object, Object> map = (Map<Object, Object>) value;
			boolean timeComponent = insideTimeComponent || "Time".equals(map.get("type"));
			Object definition = map.get("definition");
			if (timeComponent && definition instanceof String && canonicalDefinitions.contains(definition)) {
				return true;
			}
			for (Object child : map.values()) {
				if (containsTimeComponentWithDefinition(child, canonicalDefinitions, timeComponent)) {
					return true;
				}
			}
		}
		else if (value instanceof Iterable) {
			for (Object child : (Iterable<?>) value) {
				if (containsTimeComponentWithDefinition(child, canonicalDefinitions, insideTimeComponent)) {
					return true;
				}
			}
		}
		return false;
	}

	private static String requireString(Map<String, Object> body, String key, String reqUri) {
		ETSAssert.assertJsonObjectHas(body, key, String.class, reqUri);
		return (String) body.get(key);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> castMap(Object value) {
		return (Map<String, Object>) value;
	}

	private static List<?> items(Map<String, Object> body) {
		if (body == null || !(body.get("items") instanceof List)) {
			return List.of();
		}
		return (List<?>) body.get("items");
	}

	private static List<?> links(Map<String, Object> body) {
		if (body == null || !(body.get("links") instanceof List)) {
			return List.of();
		}
		return (List<?>) body.get("links");
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> parseBody(Response response) {
		if (response == null || response.getBody() == null) {
			return null;
		}
		try {
			return response.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			return null;
		}
	}

	private record SweCandidate(String body, Response response, String source) {
	}

}
