package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.json;

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
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.Part2CandidateSelection;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.Part2SchemaValidation;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.apicommon.Part2ApiCommonTests;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Part 2 - JSON Encoding read-only conformance subset ({@code /conf/json}; OGC
 * 23-002 Clause 16.1 and Annex A.9).
 *
 * <p>
 * Implements the first <strong>REQ-ETS-PART2-009</strong> increment: exact JSON
 * declaration, SWE Common JSON record-components prerequisite visibility, resource-class
 * condition gates, read-only {@code application/json} negotiation, bundled Part 2 JSON
 * Schema validation where candidate resources exist, guarded dynamic-schema evidence, and
 * non-mutating mediatype-write advertisement checks.
 * </p>
 */
public class Part2JsonTests {

	static final String GROUP = "part2json";

	static final String CONF_JSON = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/json";

	static final String CONF_SWE_JSON_RECORD_COMPONENTS = "http://www.opengis.net/spec/SWE/3.0/conf/json-record-components";

	static final String CONF_DATASTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/datastream";

	static final String CONF_CONTROLSTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/controlstream";

	static final String CONF_SYSTEM_EVENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/system-event";

	static final String CONF_CREATE_REPLACE_DELETE = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/create-replace-delete";

	static final String REQ_JSON = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/json";

	static final String REQ_MEDIATYPE_READ = REQ_JSON + "/mediatype-read";

	static final String REQ_MEDIATYPE_WRITE = REQ_JSON + "/mediatype-write";

	static final String REQ_DATASTREAM_SCHEMA = REQ_JSON + "/datastream-schema";

	static final String REQ_OBSSCHEMA_SCHEMA = REQ_JSON + "/obsschema-schema";

	static final String REQ_OBSERVATION_SCHEMA = REQ_JSON + "/observation-schema";

	static final String REQ_OBSERVATION_CONSTRAINTS = REQ_JSON + "/observation-constraints";

	static final String REQ_CONTROLSTREAM_SCHEMA = REQ_JSON + "/controlstream-schema";

	static final String REQ_COMMANDSCHEMA_SCHEMA = REQ_JSON + "/commandschema-schema";

	static final String REQ_COMMAND_SCHEMA = REQ_JSON + "/command-schema";

	static final String REQ_COMMAND_CONSTRAINTS = REQ_JSON + "/command-constraints";

	static final String REQ_COMMANDSTATUS_SCHEMA = REQ_JSON + "/commandstatus-schema";

	static final String REQ_COMMANDRESULT_SCHEMA = REQ_JSON + "/commandresult-schema";

	static final String REQ_COMMANDRESULT_CONSTRAINTS = REQ_JSON + "/commandresult-constraints";

	static final String REQ_SYSTEMEVENT_SCHEMA = REQ_JSON + "/systemevent-schema";

	static final String DATASTREAM_COLLECTION_SCHEMA = "dataStreamCollection.json";

	static final String DATASTREAM_SCHEMA = "dataStream.json";

	static final String OBSERVATION_SCHEMA_JSON_SCHEMA = "observationSchemaJson.json";

	static final String OBSERVATION_COLLECTION_SCHEMA = "observationCollection.json";

	static final String OBSERVATION_SCHEMA = "observation.json";

	static final String CONTROLSTREAM_COLLECTION_SCHEMA = "controlStreamCollection.json";

	static final String CONTROLSTREAM_SCHEMA = "controlStream.json";

	static final String COMMAND_SCHEMA_JSON_SCHEMA = "commandSchemaJson.json";

	static final String COMMAND_COLLECTION_SCHEMA = "commandCollection.json";

	static final String COMMAND_SCHEMA = "command.json";

	static final String COMMANDSTATUS_COLLECTION_SCHEMA = "commandStatusCollection.json";

	static final String COMMANDSTATUS_SCHEMA = "commandStatus.json";

	static final String COMMANDRESULT_COLLECTION_SCHEMA = "commandResultCollection.json";

	static final String COMMANDRESULT_SCHEMA = "commandResult.json";

	static final String SYSTEMEVENT_COLLECTION_SCHEMA = "systemEventCollection.json";

	static final String SYSTEMEVENT_SCHEMA = "systemEvent.json";

	static final List<String> ANNEX_A9_SCHEMA_FILES = List.of(DATASTREAM_SCHEMA, DATASTREAM_COLLECTION_SCHEMA,
			OBSERVATION_SCHEMA_JSON_SCHEMA, OBSERVATION_SCHEMA, OBSERVATION_COLLECTION_SCHEMA, CONTROLSTREAM_SCHEMA,
			CONTROLSTREAM_COLLECTION_SCHEMA, COMMAND_SCHEMA_JSON_SCHEMA, COMMAND_SCHEMA, COMMAND_COLLECTION_SCHEMA,
			COMMANDSTATUS_SCHEMA, COMMANDSTATUS_COLLECTION_SCHEMA, COMMANDRESULT_SCHEMA,
			COMMANDRESULT_COLLECTION_SCHEMA, SYSTEMEVENT_SCHEMA, SYSTEMEVENT_COLLECTION_SCHEMA);

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
	public void fetchPart2JsonInputs(ITestContext testContext) {
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
	 * SCENARIO-ETS-PART2-009-JSON-CONFORMANCE-DECLARED-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_JSON
			+ ": /conformance declares exact Part 2 /conf/json before JSON Encoding assertions run (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-JSON-CONFORMANCE-DECLARED-001)",
			groups = GROUP)
	public void part2JsonConformanceDeclared() {
		ETSAssert.assertStatus(this.conformanceResponse, 200, REQ_JSON);
		if (this.conformanceBody == null) {
			ETSAssert.failWithUri(REQ_JSON, "/conformance body did not parse as JSON. Content-Type was: "
					+ this.conformanceResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.conformanceBody, "conformsTo", List.class, REQ_JSON);
		if (!declaresConformance(this.conformanceBody, CONF_JSON)) {
			throw new SkipException(CONF_JSON
					+ " - IUT does not declare the CS API Part 2 JSON Encoding conformance class. "
					+ "Sibling Common JSON, GeoJSON, SWE Common, or resource-class declarations are not JSON Encoding PASS evidence.");
		}
	}

	/**
	 * SCENARIO-ETS-PART2-009-SWE-PREREQUISITE-VISIBLE-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_JSON
			+ ": SWE Common 3.0 JSON record-components prerequisite is visible before full /conf/json closure (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-SWE-PREREQUISITE-VISIBLE-001)",
			groups = GROUP)
	public void sweJsonRecordComponentsPrerequisiteVisibleForFullClosure() {
		skipIfJsonUndeclared();
		if (!declaresConformance(this.conformanceBody, CONF_SWE_JSON_RECORD_COMPONENTS)) {
			throw new SkipException(CONF_SWE_JSON_RECORD_COMPONENTS
					+ " - /req/json lists SWE Common 3.0 JSON record components as a prerequisite. "
					+ "Scoped JSON resource checks may run, but full /conf/json closure is prerequisite-incomplete.");
		}
		Reporter.log("IUT declares /conf/json and the SWE Common 3.0 JSON record-components prerequisite.", true);
	}

	/**
	 * SCENARIO-ETS-PART2-009-RESOURCE-CONDITION-GATES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_JSON
			+ ": resource-specific JSON assertions are condition-gated on Part 2 Datastream, ControlStream, and SystemEvent classes (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-RESOURCE-CONDITION-GATES-001)",
			groups = GROUP)
	public void jsonResourceConditionGatesAreVisible() {
		skipIfJsonUndeclared();
		List<String> missing = missingConditionClasses(this.conformanceBody);
		if (!missing.isEmpty()) {
			throw new SkipException(REQ_JSON
					+ " - resource-specific JSON assertions are prerequisite-incomplete for missing condition classes: "
					+ String.join("; ", missing));
		}
		Reporter.log(
				"Part 2 JSON resource condition classes are declared for Datastream, ControlStream, and SystemEvent.",
				true);
	}

	/**
	 * SCENARIO-ETS-PART2-009-MEDIATYPE-READ-001.
	 * SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_DATASTREAM_SCHEMA
			+ ": /datastreams JSON collection and candidate DataStream validate against Annex A.9 schemas (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-MEDIATYPE-READ-001, SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001, SCENARIO-ETS-PART2-009-UNAVAILABLE-ENDPOINT-HONESTY-001)",
			groups = GROUP)
	public void datastreamJsonSchemasValidateAgainstAnnexA9() {
		skipIfJsonUndeclared();
		skipIfConditionClassUndeclared(CONF_DATASTREAM,
				"DataStream JSON schema assertions require the Part 2 Datastream class.");
		Map<String, Object> datastream = firstRequiredCollectionResource("datastreams", REQ_DATASTREAM_SCHEMA,
				DATASTREAM_COLLECTION_SCHEMA, "/datastreams");
		validateJsonValueAgainstSchema(datastream, DATASTREAM_SCHEMA, REQ_DATASTREAM_SCHEMA, "/datastreams first item");
	}

	/**
	 * SCENARIO-ETS-PART2-009-MEDIATYPE-READ-001.
	 * SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_OBSSCHEMA_SCHEMA
			+ ": selected Datastream observation schema validates as application/json schema metadata (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-MEDIATYPE-READ-001, SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001)",
			groups = GROUP)
	public void observationSchemaJsonValidWhenDatastreamCandidateAvailable() {
		skipIfJsonUndeclared();
		skipIfConditionClassUndeclared(CONF_DATASTREAM,
				"Observation schema JSON assertions require the Part 2 Datastream class.");
		String datastreamId = requireString(preferredDatastreamResource(REQ_OBSSCHEMA_SCHEMA), "id",
				REQ_OBSSCHEMA_SCHEMA);
		Response response = given().accept("application/json")
			.queryParam("obsFormat", "application/json")
			.when()
			.get(this.baseUri.resolve("datastreams/" + datastreamId + "/schema"))
			.andReturn();
		assertRequiredJsonResponse(response, REQ_OBSSCHEMA_SCHEMA,
				"/datastreams/" + datastreamId + "/schema?obsFormat=application/json");
		validateResponseAgainstSchema(response, OBSERVATION_SCHEMA_JSON_SCHEMA, REQ_OBSSCHEMA_SCHEMA,
				"/datastreams/" + datastreamId + "/schema?obsFormat=application/json");
	}

	/**
	 * SCENARIO-ETS-PART2-009-MEDIATYPE-READ-001.
	 * SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_OBSERVATION_SCHEMA
			+ ": /observations JSON collection and candidate Observation validate against Annex A.9 schemas (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-MEDIATYPE-READ-001, SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001, SCENARIO-ETS-PART2-009-UNAVAILABLE-ENDPOINT-HONESTY-001)",
			groups = GROUP)
	public void observationJsonSchemasValidateAgainstAnnexA9() {
		skipIfJsonUndeclared();
		skipIfConditionClassUndeclared(CONF_DATASTREAM,
				"Observation JSON schema assertions require the Part 2 Datastream class.");
		Map<String, Object> observation = firstRequiredCollectionResource("observations", REQ_OBSERVATION_SCHEMA,
				OBSERVATION_COLLECTION_SCHEMA, "/observations");
		validateJsonValueAgainstSchema(observation, OBSERVATION_SCHEMA, REQ_OBSERVATION_SCHEMA,
				"/observations first item");
	}

	/**
	 * SCENARIO-ETS-PART2-009-OBSERVATION-COMMAND-CONSTRAINTS-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_OBSERVATION_CONSTRAINTS
			+ ": Observation result/parameters constraints require parent Datastream schema and candidate Observation evidence before PASS (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-OBSERVATION-COMMAND-CONSTRAINTS-001)",
			groups = GROUP)
	public void observationConstraintsRequireParentSchemaAndCandidateEvidence() {
		skipIfJsonUndeclared();
		skipIfConditionClassUndeclared(CONF_DATASTREAM,
				"Observation dynamic constraints require the Part 2 Datastream class.");
		Part2CandidateSelection.ParentChild evidence = requiredDatastreamObservationEvidence(
				REQ_OBSERVATION_CONSTRAINTS);
		String datastreamId = requireString(evidence.parent(), "id", REQ_OBSERVATION_CONSTRAINTS);
		Map<String, Object> parentSchema = optionalJsonObject(
				"datastreams/" + datastreamId + "/schema?obsFormat=application/json", REQ_OBSERVATION_CONSTRAINTS,
				"parent Observation schema");
		if (!hasAny(parentSchema, "resultSchema", "parametersSchema", "paramsSchema")) {
			throw new SkipException(REQ_OBSERVATION_CONSTRAINTS + " - Datastream '" + datastreamId
					+ "' schema does not expose resultSchema, parametersSchema, or paramsSchema evidence; no hardcoded Observation constraint PASS was reported.");
		}
		Map<String, Object> observation = evidence.child();
		validateJsonValueAgainstSchema(observation, OBSERVATION_SCHEMA, REQ_OBSERVATION_CONSTRAINTS,
				"/datastreams/" + datastreamId + "/observations first item");
		if (!hasAny(observation, "result", "parameters")) {
			throw new SkipException(REQ_OBSERVATION_CONSTRAINTS + " - candidate Observation from /datastreams/"
					+ datastreamId
					+ "/observations does not contain result or parameters; no Observation dynamic constraint PASS was reported.");
		}
		assertChildHasParentSchemaEvidence(observation, parentSchema, "result", "resultSchema",
				REQ_OBSERVATION_CONSTRAINTS, "Observation result");
		assertChildHasParentSchemaEvidence(observation, parentSchema, "parameters", "parametersSchema",
				REQ_OBSERVATION_CONSTRAINTS, "Observation parameters");
		throw new SkipException(REQ_OBSERVATION_CONSTRAINTS
				+ " - parent Datastream schema and child Observation evidence are present, but semantic validation of Observation result/parameters against the parent schema is deferred; no shape-only PASS was reported.");
	}

	/**
	 * SCENARIO-ETS-PART2-009-MEDIATYPE-READ-001.
	 * SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CONTROLSTREAM_SCHEMA
			+ ": /controlstreams JSON collection and candidate ControlStream validate against Annex A.9 schemas (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-MEDIATYPE-READ-001, SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001, SCENARIO-ETS-PART2-009-UNAVAILABLE-ENDPOINT-HONESTY-001)",
			groups = GROUP)
	public void controlStreamJsonSchemasValidateAgainstAnnexA9() {
		skipIfJsonUndeclared();
		skipIfConditionClassUndeclared(CONF_CONTROLSTREAM,
				"ControlStream JSON schema assertions require the Part 2 ControlStream class.");
		Map<String, Object> controlStream = firstRequiredCollectionResource("controlstreams", REQ_CONTROLSTREAM_SCHEMA,
				CONTROLSTREAM_COLLECTION_SCHEMA, "/controlstreams");
		validateJsonValueAgainstSchema(controlStream, CONTROLSTREAM_SCHEMA, REQ_CONTROLSTREAM_SCHEMA,
				"/controlstreams first item");
	}

	/**
	 * SCENARIO-ETS-PART2-009-MEDIATYPE-READ-001.
	 * SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COMMANDSCHEMA_SCHEMA
			+ ": selected ControlStream command schema validates as application/json schema metadata (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-MEDIATYPE-READ-001, SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001)",
			groups = GROUP)
	public void commandSchemaJsonValidWhenControlStreamCandidateAvailable() {
		skipIfJsonUndeclared();
		skipIfConditionClassUndeclared(CONF_CONTROLSTREAM,
				"Command schema JSON assertions require the Part 2 ControlStream class.");
		String controlStreamId = requireString(preferredControlStreamResource(REQ_COMMANDSCHEMA_SCHEMA), "id",
				REQ_COMMANDSCHEMA_SCHEMA);
		Response response = given().accept("application/json")
			.queryParam("cmdFormat", "application/json")
			.when()
			.get(this.baseUri.resolve("controlstreams/" + controlStreamId + "/schema"))
			.andReturn();
		assertRequiredJsonResponse(response, REQ_COMMANDSCHEMA_SCHEMA,
				"/controlstreams/" + controlStreamId + "/schema?cmdFormat=application/json");
		validateResponseAgainstSchema(response, COMMAND_SCHEMA_JSON_SCHEMA, REQ_COMMANDSCHEMA_SCHEMA,
				"/controlstreams/" + controlStreamId + "/schema?cmdFormat=application/json");
	}

	/**
	 * SCENARIO-ETS-PART2-009-MEDIATYPE-READ-001.
	 * SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COMMAND_SCHEMA
			+ ": Command collection and candidate Command validate against Annex A.9 schemas when candidate evidence exists (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-MEDIATYPE-READ-001, SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001, SCENARIO-ETS-PART2-009-UNAVAILABLE-ENDPOINT-HONESTY-001)",
			groups = GROUP)
	public void commandJsonSchemasValidateWhenCandidateAvailable() {
		skipIfJsonUndeclared();
		skipIfConditionClassUndeclared(CONF_CONTROLSTREAM,
				"Command JSON schema assertions require the Part 2 ControlStream class.");
		CommandEvidence command = firstCommandEvidence(REQ_COMMAND_SCHEMA);
		validateJsonValueAgainstSchema(command.command(), COMMAND_SCHEMA, REQ_COMMAND_SCHEMA,
				command.source() + " first item");
	}

	/**
	 * SCENARIO-ETS-PART2-009-OBSERVATION-COMMAND-CONSTRAINTS-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COMMAND_CONSTRAINTS
			+ ": Command parameters constraints require parent ControlStream schema and candidate Command evidence before PASS (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-OBSERVATION-COMMAND-CONSTRAINTS-001)",
			groups = GROUP)
	public void commandConstraintsRequireParentSchemaAndCandidateEvidence() {
		skipIfJsonUndeclared();
		skipIfConditionClassUndeclared(CONF_CONTROLSTREAM,
				"Command dynamic constraints require the Part 2 ControlStream class.");
		Part2CandidateSelection.ParentChild evidence = requiredControlStreamCommandEvidence(REQ_COMMAND_CONSTRAINTS);
		String controlStreamId = requireString(evidence.parent(), "id", REQ_COMMAND_CONSTRAINTS);
		Map<String, Object> parentSchema = optionalJsonObject(
				"controlstreams/" + controlStreamId + "/schema?cmdFormat=application/json", REQ_COMMAND_CONSTRAINTS,
				"parent Command schema");
		if (!parentSchema.containsKey("parametersSchema")) {
			throw new SkipException(REQ_COMMAND_CONSTRAINTS + " - ControlStream '" + controlStreamId
					+ "' schema does not expose parametersSchema evidence; no hardcoded Command constraint PASS was reported.");
		}
		Map<String, Object> command = evidence.child();
		validateJsonValueAgainstSchema(command, COMMAND_SCHEMA, REQ_COMMAND_CONSTRAINTS,
				"/controlstreams/" + controlStreamId + "/commands first item");
		if (!command.containsKey("parameters")) {
			throw new SkipException(REQ_COMMAND_CONSTRAINTS + " - candidate Command from /controlstreams/"
					+ controlStreamId + "/commands"
					+ " does not contain parameters; no Command parameters constraint PASS was reported.");
		}
		throw new SkipException(REQ_COMMAND_CONSTRAINTS
				+ " - parent ControlStream schema and child Command parameters evidence are present, but semantic validation of Command parameters against the parent schema is deferred; no shape-only PASS was reported.");
	}

	/**
	 * SCENARIO-ETS-PART2-009-MEDIATYPE-READ-001.
	 * SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COMMANDSTATUS_SCHEMA
			+ ": CommandStatus collection and candidate validate against Annex A.9 schemas when candidate evidence exists (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-MEDIATYPE-READ-001, SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001)",
			groups = GROUP)
	public void commandStatusJsonSchemasValidateWhenCandidateAvailable() {
		skipIfJsonUndeclared();
		skipIfConditionClassUndeclared(CONF_CONTROLSTREAM,
				"CommandStatus JSON schema assertions require the Part 2 ControlStream class.");
		CommandEvidence command = firstCommandEvidence(REQ_COMMANDSTATUS_SCHEMA);
		Map<String, Object> status = firstOptionalCollectionResource("commands/" + command.id() + "/status",
				REQ_COMMANDSTATUS_SCHEMA, COMMANDSTATUS_COLLECTION_SCHEMA, "/commands/" + command.id() + "/status");
		validateJsonValueAgainstSchema(status, COMMANDSTATUS_SCHEMA, REQ_COMMANDSTATUS_SCHEMA,
				"/commands/" + command.id() + "/status first item");
	}

	/**
	 * SCENARIO-ETS-PART2-009-MEDIATYPE-READ-001.
	 * SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COMMANDRESULT_SCHEMA
			+ ": CommandResult collection and candidate validate against Annex A.9 schemas when candidate evidence exists (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-MEDIATYPE-READ-001, SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001)",
			groups = GROUP)
	public void commandResultJsonSchemasValidateWhenCandidateAvailable() {
		skipIfJsonUndeclared();
		skipIfConditionClassUndeclared(CONF_CONTROLSTREAM,
				"CommandResult JSON schema assertions require the Part 2 ControlStream class.");
		CommandEvidence command = firstCommandEvidence(REQ_COMMANDRESULT_SCHEMA);
		Map<String, Object> result = firstOptionalCollectionResource("commands/" + command.id() + "/result",
				REQ_COMMANDRESULT_SCHEMA, COMMANDRESULT_COLLECTION_SCHEMA, "/commands/" + command.id() + "/result");
		validateJsonValueAgainstSchema(result, COMMANDRESULT_SCHEMA, REQ_COMMANDRESULT_SCHEMA,
				"/commands/" + command.id() + "/result first item");
	}

	/**
	 * SCENARIO-ETS-PART2-009-OBSERVATION-COMMAND-CONSTRAINTS-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COMMANDRESULT_CONSTRAINTS
			+ ": CommandResult inline data constraints require parent ControlStream schema and candidate CommandResult evidence before PASS (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-OBSERVATION-COMMAND-CONSTRAINTS-001)",
			groups = GROUP)
	public void commandResultConstraintsRequireParentSchemaAndCandidateEvidence() {
		skipIfJsonUndeclared();
		skipIfConditionClassUndeclared(CONF_CONTROLSTREAM,
				"CommandResult dynamic constraints require the Part 2 ControlStream class.");
		Part2CandidateSelection.ParentChild evidence = requiredControlStreamCommandEvidence(
				REQ_COMMANDRESULT_CONSTRAINTS);
		String controlStreamId = requireString(evidence.parent(), "id", REQ_COMMANDRESULT_CONSTRAINTS);
		Map<String, Object> parentSchema = optionalJsonObject(
				"controlstreams/" + controlStreamId + "/schema?cmdFormat=application/json",
				REQ_COMMANDRESULT_CONSTRAINTS, "parent CommandResult schema");
		if (!hasAny(parentSchema, "resultSchema", "feasibilityResultSchema")) {
			throw new SkipException(REQ_COMMANDRESULT_CONSTRAINTS + " - ControlStream '" + controlStreamId
					+ "' schema does not expose resultSchema or feasibilityResultSchema evidence; no hardcoded CommandResult constraint PASS was reported.");
		}
		CommandEvidence command = commandEvidence(evidence.child(), REQ_COMMANDRESULT_CONSTRAINTS,
				"/controlstreams/" + controlStreamId + "/commands");
		Map<String, Object> result = firstOptionalCollectionResource("commands/" + command.id() + "/result",
				REQ_COMMANDRESULT_CONSTRAINTS, COMMANDRESULT_COLLECTION_SCHEMA,
				"/commands/" + command.id() + "/result");
		validateJsonValueAgainstSchema(result, COMMANDRESULT_SCHEMA, REQ_COMMANDRESULT_CONSTRAINTS,
				"/commands/" + command.id() + "/result first item");
		if (!hasAny(result, "result", "data")) {
			throw new SkipException(REQ_COMMANDRESULT_CONSTRAINTS + " - candidate CommandResult from /commands/"
					+ command.id()
					+ "/result does not contain inline result/data; no CommandResult inline constraint PASS was reported.");
		}
		throw new SkipException(REQ_COMMANDRESULT_CONSTRAINTS
				+ " - parent ControlStream schema and child CommandResult inline result/data evidence are present, but semantic validation against the parent schema is deferred; no shape-only PASS was reported.");
	}

	/**
	 * SCENARIO-ETS-PART2-009-MEDIATYPE-READ-001.
	 * SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_SYSTEMEVENT_SCHEMA
			+ ": SystemEvent collection and candidate validate against Annex A.9 schemas when endpoint evidence exists (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-MEDIATYPE-READ-001, SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001, SCENARIO-ETS-PART2-009-UNAVAILABLE-ENDPOINT-HONESTY-001)",
			groups = GROUP)
	public void systemEventJsonSchemasValidateWhenEndpointEvidenceExists() {
		skipIfJsonUndeclared();
		skipIfConditionClassUndeclared(CONF_SYSTEM_EVENT,
				"SystemEvent JSON schema assertions require the Part 2 SystemEvent class.");
		Map<String, Object> systemEvent = firstOptionalCollectionResource("systemEvents", REQ_SYSTEMEVENT_SCHEMA,
				SYSTEMEVENT_COLLECTION_SCHEMA, "/systemEvents");
		validateJsonValueAgainstSchema(systemEvent, SYSTEMEVENT_SCHEMA, REQ_SYSTEMEVENT_SCHEMA,
				"/systemEvents first item");
	}

	/**
	 * SCENARIO-ETS-PART2-009-MEDIATYPE-WRITE-ADVERTISEMENT-001.
	 * SCENARIO-ETS-PART2-009-SMOKE-NO-PUBLIC-MUTATION-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_MEDIATYPE_WRITE
			+ ": JSON write media type support is checked only from non-mutating API definition operation metadata, never OPTIONS alone or public-IUT mutation (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-MEDIATYPE-WRITE-ADVERTISEMENT-001, SCENARIO-ETS-PART2-009-SMOKE-NO-PUBLIC-MUTATION-001)",
			groups = GROUP)
	public void jsonMediatypeWriteAdvertisedByApiDefinitionOnly() {
		skipIfJsonUndeclared();
		skipIfConditionClassUndeclared(CONF_CREATE_REPLACE_DELETE,
				"Requirement 94 applies only when Part 2 Create/Replace/Delete is declared.");
		Map<String, Object> apiDefinition = readJsonApiDefinitionOrSkip();
		if (!apiDefinitionAdvertisesJsonWrite(apiDefinition)) {
			throw new SkipException(REQ_MEDIATYPE_WRITE
					+ " - API definition does not advertise application/json requestBody content for POST or PUT. OPTIONS evidence alone is not mediatype-write PASS evidence; no POST/PUT/PATCH/DELETE request was issued.");
		}
		Reporter.log(
				"API definition advertises application/json requestBody content for a create/replace operation; no mutation request was issued.",
				true);
	}

	static boolean declaresConformance(Map<String, Object> body, String conformanceUri) {
		return Part2ApiCommonTests.declaresConformance(body, conformanceUri);
	}

	static List<String> missingConditionClasses(Map<String, Object> body) {
		List<String> missing = new ArrayList<>();
		if (!declaresConformance(body, CONF_DATASTREAM)) {
			missing.add(missingConditionMessage(CONF_DATASTREAM, "Requirements 95-98 DataStream/Observation JSON"));
		}
		if (!declaresConformance(body, CONF_CONTROLSTREAM)) {
			missing.add(missingConditionMessage(CONF_CONTROLSTREAM, "Requirements 99-105 ControlStream/Command JSON"));
		}
		if (!declaresConformance(body, CONF_SYSTEM_EVENT)) {
			missing.add(missingConditionMessage(CONF_SYSTEM_EVENT, "Requirement 106 SystemEvent JSON"));
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

	static String schemaIri(String schemaFile) {
		return SCHEMA_IRI_PREFIX + schemaFile;
	}

	static boolean schemaResourceExists(String schemaFile) {
		try (var in = Part2JsonTests.class.getResourceAsStream(SCHEMA_RESOURCE_PREFIX + schemaFile)) {
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

	static boolean apiDefinitionAdvertisesJsonWrite(Map<String, Object> apiDefinition) {
		if (apiDefinition == null) {
			return false;
		}
		Set<Map<?, ?>> operations = writeOperations(apiDefinition);
		return operations.stream().anyMatch(Part2JsonTests::requestBodyContainsApplicationJson);
	}

	private static Set<Map<?, ?>> writeOperations(Map<String, Object> apiDefinition) {
		Set<Map<?, ?>> operations = new LinkedHashSet<>();
		Object paths = apiDefinition.get("paths");
		if (!(paths instanceof Map)) {
			return operations;
		}
		for (Object pathItem : ((Map<?, ?>) paths).values()) {
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

	private static boolean requestBodyContainsApplicationJson(Map<?, ?> operation) {
		Object requestBody = operation.get("requestBody");
		if (!(requestBody instanceof Map)) {
			return false;
		}
		Object content = ((Map<?, ?>) requestBody).get("content");
		return content instanceof Map
				&& ((Map<?, ?>) content).keySet().stream().anyMatch(Part2JsonTests::isJsonMediaKey);
	}

	private static boolean isJsonMediaKey(Object key) {
		if (!(key instanceof String)) {
			return false;
		}
		return "application/json".equals(((String) key).trim().toLowerCase(Locale.ROOT));
	}

	private void skipIfJsonUndeclared() {
		if (!declaresConformance(this.conformanceBody, CONF_JSON)) {
			throw new SkipException(CONF_JSON
					+ " - IUT does not declare the CS API Part 2 JSON Encoding conformance class in /conformance.");
		}
	}

	private void skipIfConditionClassUndeclared(String conformanceClass, String reason) {
		if (!declaresConformance(this.conformanceBody, conformanceClass)) {
			throw new SkipException(
					conformanceClass + " - " + reason + " No JSON Encoding PASS evidence was reported.");
		}
	}

	private Map<String, Object> firstRequiredCollectionResource(String path, String reqUri, String collectionSchema,
			String source) {
		return requiredCollectionResources(path, reqUri, collectionSchema, source).get(0);
	}

	private Map<String, Object> preferredDatastreamResource(String reqUri) {
		List<Map<String, Object>> datastreams = requiredCollectionResources("datastreams", reqUri,
				DATASTREAM_COLLECTION_SCHEMA, "/datastreams");
		Part2CandidateSelection.ParentChild evidence = firstParentWithChild(datastreams, "datastreams", "observations",
				reqUri);
		return evidence == null ? datastreams.get(0) : evidence.parent();
	}

	private Map<String, Object> preferredControlStreamResource(String reqUri) {
		List<Map<String, Object>> controlStreams = requiredCollectionResources("controlstreams", reqUri,
				CONTROLSTREAM_COLLECTION_SCHEMA, "/controlstreams");
		Part2CandidateSelection.ParentChild evidence = firstParentWithChild(controlStreams, "controlstreams",
				"commands", reqUri);
		return evidence == null ? controlStreams.get(0) : evidence.parent();
	}

	private Part2CandidateSelection.ParentChild requiredDatastreamObservationEvidence(String reqUri) {
		List<Map<String, Object>> datastreams = requiredCollectionResources("datastreams", reqUri,
				DATASTREAM_COLLECTION_SCHEMA, "/datastreams");
		Part2CandidateSelection.ParentChild evidence = firstParentWithChild(datastreams, "datastreams", "observations",
				reqUri);
		if (evidence != null) {
			return evidence;
		}
		throw new SkipException(reqUri
				+ " - no DataStream candidate on the inspected page exposed parseable scoped Observation evidence; no Observation constraint PASS was reported.");
	}

	private Part2CandidateSelection.ParentChild requiredControlStreamCommandEvidence(String reqUri) {
		List<Map<String, Object>> controlStreams = requiredCollectionResources("controlstreams", reqUri,
				CONTROLSTREAM_COLLECTION_SCHEMA, "/controlstreams");
		Part2CandidateSelection.ParentChild evidence = firstParentWithChild(controlStreams, "controlstreams",
				"commands", reqUri);
		if (evidence != null) {
			return evidence;
		}
		throw new SkipException(reqUri
				+ " - no ControlStream candidate on the inspected page exposed parseable scoped Command evidence; no Command constraint PASS was reported.");
	}

	private Part2CandidateSelection.ParentChild firstParentWithChild(List<Map<String, Object>> parents,
			String parentPath, String childCollection, String reqUri) {
		return Part2CandidateSelection.firstParentWithChild(parents, parent -> {
			String parentId = stringValue(parent.get("id"));
			if (parentId == null || parentId.isBlank()) {
				return null;
			}
			return firstOptionalCollectionItemOrNull(parentPath + "/" + parentId + "/" + childCollection, reqUri,
					"/" + parentPath + "/" + parentId + "/" + childCollection);
		});
	}

	private List<Map<String, Object>> requiredCollectionResources(String path, String reqUri, String collectionSchema,
			String source) {
		Response response = given().accept("application/json")
			.queryParam("limit", Part2CandidateSelection.CANDIDATE_PAGE_LIMIT)
			.when()
			.get(this.baseUri.resolve(path))
			.andReturn();
		Map<String, Object> body = assertRequiredJsonResponse(response, reqUri, source);
		validateResponseAgainstSchema(response, collectionSchema, reqUri, source);
		List<Map<String, Object>> items = Part2CandidateSelection.objectItems(body);
		if (items.isEmpty()) {
			throw new SkipException(reqUri + " - " + source
					+ " returned an empty collection; no candidate resource is available for schema-validation PASS.");
		}
		return items;
	}

	private Map<String, Object> firstOptionalCollectionResource(String path, String reqUri, String collectionSchema,
			String source) {
		Response response = given().accept("application/json")
			.queryParam("limit", 1)
			.when()
			.get(this.baseUri.resolve(path))
			.andReturn();
		if (response.getStatusCode() != 200) {
			throw new SkipException(reqUri + " - " + source + " returned HTTP " + response.getStatusCode()
					+ "; no JSON candidate resource evidence is available.");
		}
		Map<String, Object> body = assertRequiredJsonResponse(response, reqUri, source);
		validateResponseAgainstSchema(response, collectionSchema, reqUri, source);
		List<?> items = items(body);
		if (items.isEmpty()) {
			throw new SkipException(reqUri + " - " + source
					+ " returned an empty collection; no candidate resource is available for schema-validation PASS.");
		}
		Object first = items.get(0);
		if (!(first instanceof Map)) {
			ETSAssert.failWithUri(reqUri, source + " first item was not a JSON object: " + first);
		}
		return castMap(first);
	}

	private CommandEvidence firstCommandEvidence(String reqUri) {
		Part2CandidateSelection.ParentChild evidence = firstControlStreamCommandEvidenceOrNull(reqUri);
		if (evidence != null) {
			String controlStreamId = requireString(evidence.parent(), "id", reqUri);
			return commandEvidence(evidence.child(), reqUri, "/controlstreams/" + controlStreamId + "/commands");
		}

		Response global = given().accept("application/json")
			.queryParam("limit", 1)
			.when()
			.get(this.baseUri.resolve("commands"))
			.andReturn();
		if (global.getStatusCode() != 200) {
			throw new SkipException(reqUri
					+ " - scoped /controlstreams/{id}/commands probes found no candidate and /commands returned HTTP "
					+ global.getStatusCode() + "; no Command candidate resource evidence is available.");
		}
		Map<String, Object> body = assertRequiredJsonResponse(global, reqUri, "/commands");
		validateResponseAgainstSchema(global, COMMAND_COLLECTION_SCHEMA, reqUri, "/commands");
		List<Map<String, Object>> globalItems = Part2CandidateSelection.objectItems(body);
		if (globalItems.isEmpty()) {
			throw new SkipException(reqUri
					+ " - neither scoped /controlstreams/{id}/commands nor /commands exposed a candidate Command resource.");
		}
		return commandEvidence(globalItems.get(0), reqUri, "/commands");
	}

	private Part2CandidateSelection.ParentChild firstControlStreamCommandEvidenceOrNull(String reqUri) {
		List<Map<String, Object>> controlStreams = requiredCollectionResources("controlstreams", reqUri,
				CONTROLSTREAM_COLLECTION_SCHEMA, "/controlstreams");
		return firstParentWithChild(controlStreams, "controlstreams", "commands", reqUri);
	}

	private CommandEvidence commandEvidence(Map<String, Object> command, String reqUri, String source) {
		return new CommandEvidence(requireString(command, "id", reqUri), command, source);
	}

	private Map<String, Object> firstOptionalCollectionItemOrNull(String path, String reqUri, String source) {
		Response response = given().accept("application/json")
			.queryParam("limit", 1)
			.when()
			.get(this.baseUri.resolve(path))
			.andReturn();
		if (response.getStatusCode() != 200 || !isJsonCompatibleContentType(response.getContentType())) {
			return null;
		}
		Map<String, Object> body = parseBody(response);
		if (body == null) {
			return null;
		}
		List<Map<String, Object>> childItems = Part2CandidateSelection.objectItems(body);
		if (childItems.isEmpty()) {
			return null;
		}
		return childItems.get(0);
	}

	private Map<String, Object> optionalJsonObject(String path, String reqUri, String label) {
		Response response = given().accept("application/json").when().get(this.baseUri.resolve(path)).andReturn();
		if (response.getStatusCode() != 200) {
			throw new SkipException(reqUri + " - " + label + " at /" + path + " returned HTTP "
					+ response.getStatusCode() + "; no parent schema evidence is available.");
		}
		return assertRequiredJsonResponse(response, reqUri, "/" + path);
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
					+ "'; expected application/json or another +json media type.");
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
	}

	private static void assertSchemaResourceBundled(String schemaFile, String reqUri) {
		if (!schemaResourceExists(schemaFile)) {
			ETSAssert.failWithUri(reqUri,
					"Bundled Part 2 JSON Schema is missing: " + SCHEMA_RESOURCE_PREFIX + schemaFile);
		}
	}

	private static void assertChildHasParentSchemaEvidence(Map<String, Object> child, Map<String, Object> parentSchema,
			String childMember, String parentMember, String reqUri, String label) {
		if (child.containsKey(childMember) && !parentSchema.containsKey(parentMember)) {
			ETSAssert.failWithUri(reqUri,
					label + " is present but parent schema does not expose " + parentMember + " evidence.");
		}
	}

	private static boolean hasAny(Map<String, Object> body, String... keys) {
		if (body == null) {
			return false;
		}
		for (String key : keys) {
			if (body.containsKey(key)) {
				return true;
			}
		}
		return false;
	}

	private static String requireString(Map<String, Object> body, String key, String reqUri) {
		ETSAssert.assertJsonObjectHas(body, key, String.class, reqUri);
		return (String) body.get(key);
	}

	private static String stringValue(Object value) {
		return value instanceof String ? (String) value : null;
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

	private record CommandEvidence(String id, Map<String, Object> command, String source) {
	}

}
