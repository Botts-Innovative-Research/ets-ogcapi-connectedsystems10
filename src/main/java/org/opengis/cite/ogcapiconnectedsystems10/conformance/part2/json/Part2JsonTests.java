package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.json;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.TraversalResult;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.Part2CandidateSelection;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.apicommon.Part2ApiCommonTests;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Part 2 - JSON Encoding ({@code /conf/json}; OGC 23-002 Clause 16.1 and Annex
 * A.9).
 */
public class Part2JsonTests {

	static final String GROUP = "part2json";

	static final String CONF_JSON = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/json";

	static final String CONF_SWE_JSON_RECORD_COMPONENTS = "http://www.opengis.net/spec/SWE/3.0/conf/json-record-components";

	static final String CONF_DATASTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/datastream";

	static final String CONF_CONTROLSTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/controlstream";

	static final String CONF_SYSTEM_EVENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/system-event";

	static final String CONF_CREATE_REPLACE_DELETE = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/create-replace-delete";

	static final String CONF_PART1_SYSTEM = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/system";

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

	private URI iutUri;

	private URI baseUri;

	private Response conformanceResponse;

	private Map<String, Object> conformanceBody;

	private Response landingResponse;

	private Map<String, Object> landingBody;

	/**
	 * Fetches shared read-only inputs and skips before JSON resource endpoint access when
	 * the released class or SWE prerequisite is absent.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass(alwaysRun = true)
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
		ETSAssert.assertStatus(this.conformanceResponse, 200, REQ_JSON);
		this.conformanceBody = Part2JsonSupport.parseBody(this.conformanceResponse);
		if (this.conformanceBody == null) {
			ETSAssert.failWithUri(REQ_JSON, "/conformance body did not parse as JSON. Content-Type was: "
					+ this.conformanceResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.conformanceBody, "conformsTo", List.class, REQ_JSON);
		if (!declaresConformance(this.conformanceBody, CONF_JSON)) {
			throw new SkipException(
					CONF_JSON + " - IUT does not declare the CS API Part 2 JSON Encoding conformance class.");
		}
		if (!declaresConformance(this.conformanceBody, CONF_SWE_JSON_RECORD_COMPONENTS)) {
			throw new SkipException(CONF_SWE_JSON_RECORD_COMPONENTS
					+ " - /req/json prerequisite is missing; Sprint 65 JSON procedures skip before JSON resource endpoint access.");
		}

		this.landingResponse = given().accept("application/json").when().get(this.iutUri).andReturn();
		this.landingBody = Part2JsonSupport.parseBody(this.landingResponse);
	}

	@Test(description = "OGC-23-002 " + REQ_MEDIATYPE_READ
			+ ": supported retrieval operations return application/json documents (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-RELEASED-PROCEDURES-001, SCENARIO-ETS-PART2-009-MEDIATYPE-READ-001)",
			groups = GROUP, alwaysRun = true)
	public void jsonMediatypeReadSupportedOnRetrievalOperations() {
		List<String> paths = supportedRetrievalPaths();
		if (paths.isEmpty()) {
			throw new SkipException(REQ_MEDIATYPE_READ
					+ " - no Part 2 resource conformance class is declared, so no supported retrieval operation is available.");
		}
		for (String path : paths) {
			Response response = given().accept("application/json")
				.queryParam("limit", Part2CandidateSelection.CANDIDATE_PAGE_LIMIT)
				.when()
				.get(this.baseUri.resolve(path))
				.andReturn();
			Part2JsonSupport.assertRequiredJsonResponse(response, REQ_MEDIATYPE_READ, "/" + path);
		}
	}

	@Test(description = "OGC-23-002 " + REQ_MEDIATYPE_WRITE
			+ ": application/json write support is advertised in API definition create/replace metadata (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-RELEASED-PROCEDURES-001, SCENARIO-ETS-PART2-009-MEDIATYPE-WRITE-ADVERTISEMENT-001, SCENARIO-ETS-PART2-009-SMOKE-NO-PUBLIC-MUTATION-001)",
			groups = GROUP, alwaysRun = true)
	public void jsonMediatypeWriteAdvertisedByApiDefinitionOnly() {
		skipIfConditionClassUndeclared(CONF_CREATE_REPLACE_DELETE,
				"Requirement 94 applies only when Part 2 Create/Replace/Delete is declared.");
		Map<String, Object> apiDefinition = readJsonApiDefinitionOrFail();
		List<String> missing = Part2JsonSupport.missingJsonWriteAdvertisements(apiDefinition,
				jsonWriteEndpointTemplatesByClass());
		if (!missing.isEmpty()) {
			ETSAssert.failWithUri(REQ_MEDIATYPE_WRITE,
					"API definition does not advertise application/json requestBody content for POST or PUT on supported Part 2 resource endpoints "
							+ missing
							+ ". OPTIONS evidence alone is not mediatype-write PASS evidence; no POST/PUT/PATCH/DELETE request was issued.");
		}
		Reporter.log(
				"API definition advertises application/json requestBody content for supported Part 2 create/replace resource endpoints; no mutation request was issued.",
				true);
	}

	@Test(description = "OGC-23-002 " + REQ_DATASTREAM_SCHEMA
			+ ": DataStream single and collection JSON representations validate against Annex A.9 schemas (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-RELEASED-PROCEDURES-001, SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001)",
			groups = GROUP, alwaysRun = true)
	public void datastreamJsonSchemasValidateAgainstAnnexA9() {
		skipIfConditionClassUndeclared(CONF_DATASTREAM,
				"DataStream JSON schema assertions require the Part 2 Datastream class.");
		List<CollectionEvidence> collections = new ArrayList<>();
		collections.add(requiredJsonCollectionEvidence("datastreams", REQ_DATASTREAM_SCHEMA,
				Part2JsonSupport.DATASTREAM_COLLECTION_SCHEMA, "/datastreams", false));
		collections.addAll(validateNestedSystemCollections("datastreams", Part2JsonSupport.DATASTREAM_COLLECTION_SCHEMA,
				REQ_DATASTREAM_SCHEMA));
		assertFirstResourceAndCanonical("datastreams", REQ_DATASTREAM_SCHEMA, Part2JsonSupport.DATASTREAM_SCHEMA,
				"DataStream", collections);
	}

	@Test(description = "OGC-23-002 " + REQ_OBSSCHEMA_SCHEMA
			+ ": every inspected DataStream observation schema validates as application/json schema metadata (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-RELEASED-PROCEDURES-001, SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001)",
			groups = GROUP, alwaysRun = true)
	public void observationSchemaJsonValidForEveryInspectedDatastream() {
		skipIfConditionClassUndeclared(CONF_DATASTREAM,
				"Observation schema JSON assertions require the Part 2 Datastream class.");
		for (Map<String, Object> datastream : requiredCollectionResources("datastreams", REQ_OBSSCHEMA_SCHEMA,
				Part2JsonSupport.DATASTREAM_COLLECTION_SCHEMA, "/datastreams")) {
			String datastreamId = requireString(datastream, "id", REQ_OBSSCHEMA_SCHEMA);
			Response response = given().accept("application/json")
				.queryParam("obsFormat", "application/json")
				.when()
				.get(this.baseUri.resolve("datastreams/" + encodePathToken(datastreamId) + "/schema"))
				.andReturn();
			String source = "/datastreams/" + datastreamId + "/schema?obsFormat=application/json";
			Part2JsonSupport.assertRequiredJsonResponse(response, REQ_OBSSCHEMA_SCHEMA, source);
			Part2JsonSupport.validateResponseAgainstSchema(response, Part2JsonSupport.OBSERVATION_SCHEMA_JSON_SCHEMA,
					REQ_OBSSCHEMA_SCHEMA, source);
		}
	}

	@Test(description = "OGC-23-002 " + REQ_OBSERVATION_SCHEMA
			+ ": Observation single and collection JSON representations validate against Annex A.9 schemas (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-RELEASED-PROCEDURES-001, SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001)",
			groups = GROUP, alwaysRun = true)
	public void observationJsonSchemasValidateAgainstAnnexA9() {
		skipIfConditionClassUndeclared(CONF_DATASTREAM,
				"Observation JSON schema assertions require the Part 2 Datastream class.");
		List<CollectionEvidence> collections = new ArrayList<>();
		collections.add(requiredJsonCollectionEvidence("observations", REQ_OBSERVATION_SCHEMA,
				Part2JsonSupport.OBSERVATION_COLLECTION_SCHEMA, "/observations", false));
		List<Map<String, Object>> datastreams = requiredCollectionResources("datastreams", REQ_OBSERVATION_SCHEMA,
				Part2JsonSupport.DATASTREAM_COLLECTION_SCHEMA, "/datastreams");
		collections.addAll(validateNestedCollectionsForParents(datastreams, "datastreams", "observations",
				Part2JsonSupport.OBSERVATION_COLLECTION_SCHEMA, REQ_OBSERVATION_SCHEMA));
		assertFirstResourceAndCanonical("observations", REQ_OBSERVATION_SCHEMA, Part2JsonSupport.OBSERVATION_SCHEMA,
				"Observation", collections);
	}

	@Test(description = "OGC-23-002 " + REQ_OBSERVATION_CONSTRAINTS
			+ ": Observation result and parameters validate against parent DataStream schemas (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-RELEASED-PROCEDURES-001, SCENARIO-ETS-PART2-009-OBSERVATION-COMMAND-CONSTRAINTS-001)",
			groups = GROUP, alwaysRun = true)
	public void observationConstraintsValidateAgainstParentSchemaEvidence() {
		skipIfConditionClassUndeclared(CONF_DATASTREAM,
				"Observation dynamic constraints require the Part 2 Datastream class.");
		Part2CandidateSelection.ParentChild evidence = requiredDatastreamObservationEvidence(
				REQ_OBSERVATION_CONSTRAINTS);
		String datastreamId = requireString(evidence.parent(), "id", REQ_OBSERVATION_CONSTRAINTS);
		Map<String, Object> parentSchema = optionalJsonObject(
				"datastreams/" + encodePathToken(datastreamId) + "/schema?obsFormat=application/json",
				REQ_OBSERVATION_CONSTRAINTS, "parent Observation schema");
		Map<String, Object> observation = evidence.child();
		Part2JsonSupport.validateJsonValueAgainstSchema(observation, Part2JsonSupport.OBSERVATION_SCHEMA,
				REQ_OBSERVATION_CONSTRAINTS, "/datastreams/" + datastreamId + "/observations first item");
		boolean validated = false;
		if (observation.containsKey("result") && parentSchema.containsKey("resultSchema")) {
			Part2JsonSupport.assertValueMatchesParentSchema(observation.get("result"), parentSchema.get("resultSchema"),
					REQ_OBSERVATION_CONSTRAINTS, "Observation result");
			validated = true;
		}
		Object parametersSchema = firstPresent(parentSchema, "parametersSchema", "paramsSchema");
		if (observation.containsKey("parameters") && parametersSchema != null) {
			Part2JsonSupport.assertValueMatchesParentSchema(observation.get("parameters"), parametersSchema,
					REQ_OBSERVATION_CONSTRAINTS, "Observation parameters");
			validated = true;
		}
		if (!validated) {
			throw new SkipException(REQ_OBSERVATION_CONSTRAINTS + " - parent DataStream schema and child Observation "
					+ "exist, but no matching resultSchema/parametersSchema evidence is available.");
		}
	}

	@Test(description = "OGC-23-002 " + REQ_CONTROLSTREAM_SCHEMA
			+ ": ControlStream single and collection JSON representations validate against Annex A.9 schemas (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-RELEASED-PROCEDURES-001, SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001)",
			groups = GROUP, alwaysRun = true)
	public void controlStreamJsonSchemasValidateAgainstAnnexA9() {
		skipIfConditionClassUndeclared(CONF_CONTROLSTREAM,
				"ControlStream JSON schema assertions require the Part 2 ControlStream class.");
		List<CollectionEvidence> collections = new ArrayList<>();
		collections.add(requiredJsonCollectionEvidence("controlstreams", REQ_CONTROLSTREAM_SCHEMA,
				Part2JsonSupport.CONTROLSTREAM_COLLECTION_SCHEMA, "/controlstreams", false));
		collections.addAll(validateNestedSystemCollections("controlstreams",
				Part2JsonSupport.CONTROLSTREAM_COLLECTION_SCHEMA, REQ_CONTROLSTREAM_SCHEMA));
		assertFirstResourceAndCanonical("controlstreams", REQ_CONTROLSTREAM_SCHEMA,
				Part2JsonSupport.CONTROLSTREAM_SCHEMA, "ControlStream", collections);
	}

	@Test(description = "OGC-23-002 " + REQ_COMMANDSCHEMA_SCHEMA
			+ ": every inspected ControlStream command schema validates as application/json schema metadata (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-RELEASED-PROCEDURES-001, SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001)",
			groups = GROUP, alwaysRun = true)
	public void commandSchemaJsonValidForEveryInspectedControlStream() {
		skipIfConditionClassUndeclared(CONF_CONTROLSTREAM,
				"Command schema JSON assertions require the Part 2 ControlStream class.");
		for (Map<String, Object> controlStream : requiredCollectionResources("controlstreams", REQ_COMMANDSCHEMA_SCHEMA,
				Part2JsonSupport.CONTROLSTREAM_COLLECTION_SCHEMA, "/controlstreams")) {
			String controlStreamId = requireString(controlStream, "id", REQ_COMMANDSCHEMA_SCHEMA);
			Response response = given().accept("application/json")
				.queryParam("cmdFormat", "application/json")
				.when()
				.get(this.baseUri.resolve("controlstreams/" + encodePathToken(controlStreamId) + "/schema"))
				.andReturn();
			String source = "/controlstreams/" + controlStreamId + "/schema?cmdFormat=application/json";
			Part2JsonSupport.assertRequiredJsonResponse(response, REQ_COMMANDSCHEMA_SCHEMA, source);
			Part2JsonSupport.validateResponseAgainstSchema(response, Part2JsonSupport.COMMAND_SCHEMA_JSON_SCHEMA,
					REQ_COMMANDSCHEMA_SCHEMA, source);
		}
	}

	@Test(description = "OGC-23-002 " + REQ_COMMAND_SCHEMA
			+ ": Command single and collection JSON representations validate against Annex A.9 schemas (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-RELEASED-PROCEDURES-001, SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001)",
			groups = GROUP, alwaysRun = true)
	public void commandJsonSchemasValidateAgainstAnnexA9() {
		skipIfConditionClassUndeclared(CONF_CONTROLSTREAM,
				"Command JSON schema assertions require the Part 2 ControlStream class.");
		CommandEvidence command = firstCommandEvidenceFromCollections(commandCollectionEvidence(REQ_COMMAND_SCHEMA),
				REQ_COMMAND_SCHEMA);
		Part2JsonSupport.validateJsonValueAgainstSchema(command.command(), Part2JsonSupport.COMMAND_SCHEMA,
				REQ_COMMAND_SCHEMA, command.source() + " first item");
		assertSingleResource("commands/" + encodePathToken(command.id()), REQ_COMMAND_SCHEMA,
				Part2JsonSupport.COMMAND_SCHEMA, "/commands/" + command.id());
	}

	@Test(description = "OGC-23-002 " + REQ_COMMAND_CONSTRAINTS
			+ ": Command parameters validate against parent ControlStream schemas (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-RELEASED-PROCEDURES-001, SCENARIO-ETS-PART2-009-OBSERVATION-COMMAND-CONSTRAINTS-001)",
			groups = GROUP, alwaysRun = true)
	public void commandConstraintsValidateAgainstParentSchemaEvidence() {
		skipIfConditionClassUndeclared(CONF_CONTROLSTREAM,
				"Command dynamic constraints require the Part 2 ControlStream class.");
		Part2CandidateSelection.ParentChild evidence = requiredControlStreamCommandEvidence(REQ_COMMAND_CONSTRAINTS);
		String controlStreamId = requireString(evidence.parent(), "id", REQ_COMMAND_CONSTRAINTS);
		Map<String, Object> parentSchema = optionalJsonObject(
				"controlstreams/" + encodePathToken(controlStreamId) + "/schema?cmdFormat=application/json",
				REQ_COMMAND_CONSTRAINTS, "parent Command schema");
		Object parametersSchema = firstPresent(parentSchema, "parametersSchema", "paramsSchema");
		if (parametersSchema == null) {
			throw new SkipException(REQ_COMMAND_CONSTRAINTS + " - ControlStream '" + controlStreamId
					+ "' schema does not expose parametersSchema evidence.");
		}
		Map<String, Object> command = evidence.child();
		Part2JsonSupport.validateJsonValueAgainstSchema(command, Part2JsonSupport.COMMAND_SCHEMA,
				REQ_COMMAND_CONSTRAINTS, "/controlstreams/" + controlStreamId + "/commands first item");
		if (!command.containsKey("parameters")) {
			throw new SkipException(REQ_COMMAND_CONSTRAINTS + " - candidate Command from /controlstreams/"
					+ controlStreamId + "/commands does not contain parameters.");
		}
		Part2JsonSupport.assertValueMatchesParentSchema(command.get("parameters"), parametersSchema,
				REQ_COMMAND_CONSTRAINTS, "Command parameters");
	}

	@Test(description = "OGC-23-002 " + REQ_COMMANDSTATUS_SCHEMA
			+ ": CommandStatus single and collection JSON representations validate against Annex A.9 schemas (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-RELEASED-PROCEDURES-001, SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001)",
			groups = GROUP, alwaysRun = true)
	public void commandStatusJsonSchemasValidateAgainstAnnexA9() {
		skipIfConditionClassUndeclared(CONF_CONTROLSTREAM,
				"CommandStatus JSON schema assertions require the Part 2 ControlStream class.");
		CommandEvidence command = firstCommandEvidence(REQ_COMMANDSTATUS_SCHEMA);
		Map<String, Object> status = firstOptionalCollectionResource(
				"commands/" + encodePathToken(command.id()) + "/status", REQ_COMMANDSTATUS_SCHEMA,
				Part2JsonSupport.COMMANDSTATUS_COLLECTION_SCHEMA, "/commands/" + command.id() + "/status");
		Part2JsonSupport.validateJsonValueAgainstSchema(status, Part2JsonSupport.COMMANDSTATUS_SCHEMA,
				REQ_COMMANDSTATUS_SCHEMA, "/commands/" + command.id() + "/status first item");
		String statusId = requireString(status, "id", REQ_COMMANDSTATUS_SCHEMA);
		assertSingleResource("commands/" + encodePathToken(command.id()) + "/status/" + encodePathToken(statusId),
				REQ_COMMANDSTATUS_SCHEMA, Part2JsonSupport.COMMANDSTATUS_SCHEMA,
				"/commands/" + command.id() + "/status/" + statusId);
	}

	@Test(description = "OGC-23-002 " + REQ_COMMANDRESULT_SCHEMA
			+ ": CommandResult single and collection JSON representations validate against Annex A.9 schemas (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-RELEASED-PROCEDURES-001, SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001)",
			groups = GROUP, alwaysRun = true)
	public void commandResultJsonSchemasValidateAgainstAnnexA9() {
		skipIfConditionClassUndeclared(CONF_CONTROLSTREAM,
				"CommandResult JSON schema assertions require the Part 2 ControlStream class.");
		CommandEvidence command = firstCommandEvidence(REQ_COMMANDRESULT_SCHEMA);
		Map<String, Object> result = firstOptionalCollectionResource(
				"commands/" + encodePathToken(command.id()) + "/result", REQ_COMMANDRESULT_SCHEMA,
				Part2JsonSupport.COMMANDRESULT_COLLECTION_SCHEMA, "/commands/" + command.id() + "/result");
		Part2JsonSupport.validateJsonValueAgainstSchema(result, Part2JsonSupport.COMMANDRESULT_SCHEMA,
				REQ_COMMANDRESULT_SCHEMA, "/commands/" + command.id() + "/result first item");
		String resultId = requireString(result, "id", REQ_COMMANDRESULT_SCHEMA);
		assertSingleResource("commands/" + encodePathToken(command.id()) + "/result/" + encodePathToken(resultId),
				REQ_COMMANDRESULT_SCHEMA, Part2JsonSupport.COMMANDRESULT_SCHEMA,
				"/commands/" + command.id() + "/result/" + resultId);
	}

	@Test(description = "OGC-23-002 " + REQ_COMMANDRESULT_CONSTRAINTS
			+ ": CommandResult inline data validates against parent ControlStream schemas (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-RELEASED-PROCEDURES-001, SCENARIO-ETS-PART2-009-OBSERVATION-COMMAND-CONSTRAINTS-001)",
			groups = GROUP, alwaysRun = true)
	public void commandResultConstraintsValidateAgainstParentSchemaEvidence() {
		skipIfConditionClassUndeclared(CONF_CONTROLSTREAM,
				"CommandResult dynamic constraints require the Part 2 ControlStream class.");
		Part2CandidateSelection.ParentChild evidence = requiredControlStreamCommandEvidence(
				REQ_COMMANDRESULT_CONSTRAINTS);
		String controlStreamId = requireString(evidence.parent(), "id", REQ_COMMANDRESULT_CONSTRAINTS);
		Map<String, Object> parentSchema = optionalJsonObject(
				"controlstreams/" + encodePathToken(controlStreamId) + "/schema?cmdFormat=application/json",
				REQ_COMMANDRESULT_CONSTRAINTS, "parent CommandResult schema");
		Object resultSchema = firstPresent(parentSchema, "resultSchema", "commandResultSchema",
				"feasibilityResultSchema");
		if (resultSchema == null) {
			throw new SkipException(REQ_COMMANDRESULT_CONSTRAINTS + " - ControlStream '" + controlStreamId
					+ "' schema does not expose resultSchema evidence.");
		}
		CommandEvidence command = commandEvidence(evidence.child(), REQ_COMMANDRESULT_CONSTRAINTS,
				"/controlstreams/" + controlStreamId + "/commands");
		Map<String, Object> result = firstOptionalCollectionResource(
				"commands/" + encodePathToken(command.id()) + "/result", REQ_COMMANDRESULT_CONSTRAINTS,
				Part2JsonSupport.COMMANDRESULT_COLLECTION_SCHEMA, "/commands/" + command.id() + "/result");
		Part2JsonSupport.validateJsonValueAgainstSchema(result, Part2JsonSupport.COMMANDRESULT_SCHEMA,
				REQ_COMMANDRESULT_CONSTRAINTS, "/commands/" + command.id() + "/result first item");
		Object value = firstPresent(result, "result", "data");
		if (value == null) {
			throw new SkipException(REQ_COMMANDRESULT_CONSTRAINTS + " - candidate CommandResult from /commands/"
					+ command.id() + "/result does not contain inline result/data.");
		}
		Part2JsonSupport.assertValueMatchesParentSchema(value, resultSchema, REQ_COMMANDRESULT_CONSTRAINTS,
				"CommandResult result");
	}

	@Test(description = "OGC-23-002 " + REQ_SYSTEMEVENT_SCHEMA
			+ ": SystemEvent single and collection JSON representations validate against Annex A.9 schemas (REQ-ETS-PART2-009, SCENARIO-ETS-PART2-009-RELEASED-PROCEDURES-001, SCENARIO-ETS-PART2-009-SCHEMA-VALIDATION-READONLY-001)",
			groups = GROUP, alwaysRun = true)
	public void systemEventJsonSchemasValidateAgainstAnnexA9() {
		skipIfConditionClassUndeclared(CONF_SYSTEM_EVENT,
				"SystemEvent JSON schema assertions require the Part 2 SystemEvent class.");
		List<CollectionEvidence> collections = new ArrayList<>();
		collections.add(requiredJsonCollectionEvidence("systemEvents", REQ_SYSTEMEVENT_SCHEMA,
				Part2JsonSupport.SYSTEMEVENT_COLLECTION_SCHEMA, "/systemEvents", false));
		collections.addAll(validateNestedSystemCollections("events", Part2JsonSupport.SYSTEMEVENT_COLLECTION_SCHEMA,
				REQ_SYSTEMEVENT_SCHEMA));
		assertFirstResourceAndCanonical("systemEvents", REQ_SYSTEMEVENT_SCHEMA, Part2JsonSupport.SYSTEMEVENT_SCHEMA,
				"SystemEvent", collections);
	}

	static boolean declaresConformance(Map<String, Object> body, String conformanceUri) {
		return Part2ApiCommonTests.declaresConformance(body, conformanceUri);
	}

	private void skipIfConditionClassUndeclared(String conformanceClass, String reason) {
		if (!declaresConformance(this.conformanceBody, conformanceClass)) {
			throw new SkipException(
					conformanceClass + " - " + reason + " No JSON Encoding PASS evidence was reported.");
		}
	}

	private List<String> supportedRetrievalPaths() {
		List<String> paths = new ArrayList<>();
		if (declaresConformance(this.conformanceBody, CONF_DATASTREAM)) {
			paths.add("datastreams");
			paths.add("observations");
		}
		if (declaresConformance(this.conformanceBody, CONF_CONTROLSTREAM)) {
			paths.add("controlstreams");
			paths.add("commands");
		}
		if (declaresConformance(this.conformanceBody, CONF_SYSTEM_EVENT)) {
			paths.add("systemEvents");
		}
		return paths;
	}

	private Map<String, List<String>> jsonWriteEndpointTemplatesByClass() {
		Map<String, List<String>> templates = new LinkedHashMap<>();
		if (declaresConformance(this.conformanceBody, CONF_DATASTREAM)) {
			templates.put("DataStream resources", List.of("/datastreams", "/datastreams/{datastreamId}",
					"/systems/{systemId}/datastreams", "/systems/{systemId}/datastreams/{datastreamId}"));
			templates.put("Observation resources",
					List.of("/observations", "/observations/{observationId}",
							"/datastreams/{datastreamId}/observations",
							"/datastreams/{datastreamId}/observations/{observationId}"));
		}
		if (declaresConformance(this.conformanceBody, CONF_CONTROLSTREAM)) {
			templates.put("ControlStream resources", List.of("/controlstreams", "/controlstreams/{controlStreamId}",
					"/systems/{systemId}/controlstreams", "/systems/{systemId}/controlstreams/{controlStreamId}"));
			templates.put("Command resources",
					List.of("/commands", "/commands/{commandId}", "/controlstreams/{controlStreamId}/commands",
							"/controlstreams/{controlStreamId}/commands/{commandId}"));
		}
		if (declaresConformance(this.conformanceBody, CONF_SYSTEM_EVENT)) {
			templates.put("SystemEvent resources", List.of("/systemEvents", "/systemEvents/{systemEventId}",
					"/systems/{systemId}/events", "/systems/{systemId}/events/{systemEventId}"));
		}
		if (templates.isEmpty()) {
			throw new SkipException(REQ_MEDIATYPE_WRITE
					+ " - no Part 2 resource conformance class is declared, so no create/replace resource endpoint is applicable.");
		}
		return templates;
	}

	private void assertFirstResourceAndCanonical(String canonicalCollectionPath, String reqUri, String itemSchema,
			String label, List<CollectionEvidence> collections) {
		ResourceEvidence first = firstResourceEvidence(collections, reqUri, label);
		Part2JsonSupport.validateJsonValueAgainstSchema(first.resource(), itemSchema, reqUri,
				first.source() + " first " + label);
		String id = requireString(first.resource(), "id", reqUri);
		assertSingleResource(canonicalCollectionPath + "/" + encodePathToken(id), reqUri, itemSchema,
				"/" + canonicalCollectionPath + "/" + id);
	}

	private ResourceEvidence firstResourceEvidence(List<CollectionEvidence> collections, String reqUri, String label) {
		for (CollectionEvidence collection : collections) {
			if (!collection.traversal().items().isEmpty()) {
				return new ResourceEvidence(collection.source(), collection.traversal().items().get(0));
			}
		}
		throw new SkipException(reqUri + " - inspected " + label
				+ " collections are schema-valid but empty; no candidate resource is available for single-resource schema-validation PASS.");
	}

	private CollectionEvidence requiredJsonCollectionEvidence(String path, String reqUri, String collectionSchema,
			String source, boolean requireItems) {
		TraversalResult traversal = requiredJsonCollection(path, reqUri, collectionSchema, source, requireItems);
		return new CollectionEvidence(source, traversal);
	}

	private TraversalResult requiredJsonCollection(String path, String reqUri, String collectionSchema, String source,
			boolean requireItems) {
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.resourcesAtEndpoint(this.baseUri.resolve(path),
				"application/json", Map.of("limit", String.valueOf(Part2CandidateSelection.CANDIDATE_PAGE_LIMIT)),
				reqUri, Set.of("application/json"),
				page -> validateCollectionPage(page.body(), collectionSchema, reqUri, page.source().toString()));
		if (evidence.isEmpty()) {
			ETSAssert.failWithUri(reqUri, source + " returned HTTP 404.");
		}
		TraversalResult traversal = evidence.orElseThrow();
		if (requireItems && traversal.items().isEmpty()) {
			throw new SkipException(reqUri + " - " + source
					+ " returned an empty collection; no candidate resource is available for schema-validation PASS.");
		}
		return traversal;
	}

	private void validateCollectionPage(Map<String, Object> body, String collectionSchema, String reqUri,
			String source) {
		if (collectionSchema != null) {
			Part2JsonSupport.validateJsonValueAgainstSchema(body, collectionSchema, reqUri, source);
		}
	}

	private List<CollectionEvidence> validateNestedSystemCollections(String childCollection, String collectionSchema,
			String reqUri) {
		skipIfConditionClassUndeclared(CONF_PART1_SYSTEM,
				"Nested Part 2 JSON endpoint repetition requires the Part 1 System conformance class.");
		Optional<TraversalResult> systems = Part1ApiCommonSupport.canonicalResourcesDetailed(this.baseUri, "systems");
		if (systems.isEmpty()) {
			ETSAssert.failWithUri(reqUri, "/systems returned HTTP 404.");
		}
		return validateNestedCollectionsForParents(systems.orElseThrow().items(), "systems", childCollection,
				collectionSchema, reqUri);
	}

	private List<CollectionEvidence> validateNestedCollectionsForParents(List<Map<String, Object>> parents,
			String parentPath, String childCollection, String childCollectionSchema, String reqUri) {
		if (parents.isEmpty()) {
			throw new SkipException(
					reqUri + " - no " + parentPath + " resources are available for nested JSON collection validation.");
		}
		List<CollectionEvidence> collections = new ArrayList<>();
		for (Map<String, Object> parent : parents) {
			String parentId = requireString(parent, "id", reqUri);
			String path = parentPath + "/" + encodePathToken(parentId) + "/" + childCollection;
			collections.add(requiredJsonCollectionEvidence(path, reqUri, childCollectionSchema, "/" + path, false));
		}
		return collections;
	}

	private void assertSingleResource(String path, String reqUri, String schemaFile, String source) {
		Response response = given().accept("application/json").when().get(this.baseUri.resolve(path)).andReturn();
		Part2JsonSupport.assertRequiredJsonResponse(response, reqUri, source);
		Part2JsonSupport.validateResponseAgainstSchema(response, schemaFile, reqUri, source);
	}

	private List<Map<String, Object>> requiredCollectionResources(String path, String reqUri, String collectionSchema,
			String source) {
		return requiredJsonCollection(path, reqUri, collectionSchema, source, true).items();
	}

	private Part2CandidateSelection.ParentChild requiredDatastreamObservationEvidence(String reqUri) {
		Part2CandidateSelection.ParentChild evidence = firstDatastreamObservationEvidenceOrNull(reqUri);
		if (evidence != null) {
			return evidence;
		}
		throw new SkipException(reqUri
				+ " - no DataStream candidate in the traversed collection evidence exposed parseable scoped Observation evidence.");
	}

	private Part2CandidateSelection.ParentChild firstDatastreamObservationEvidenceOrNull(String reqUri) {
		List<Map<String, Object>> datastreams = requiredCollectionResources("datastreams", reqUri,
				Part2JsonSupport.DATASTREAM_COLLECTION_SCHEMA, "/datastreams");
		return firstParentWithChild(datastreams, "datastreams", "observations",
				Part2JsonSupport.OBSERVATION_COLLECTION_SCHEMA, reqUri);
	}

	private Part2CandidateSelection.ParentChild requiredControlStreamCommandEvidence(String reqUri) {
		Part2CandidateSelection.ParentChild evidence = firstControlStreamCommandEvidenceOrNull(reqUri);
		if (evidence != null) {
			return evidence;
		}
		throw new SkipException(reqUri
				+ " - no ControlStream candidate in the traversed collection evidence exposed parseable scoped Command evidence.");
	}

	private Part2CandidateSelection.ParentChild firstControlStreamCommandEvidenceOrNull(String reqUri) {
		List<Map<String, Object>> controlStreams = requiredCollectionResources("controlstreams", reqUri,
				Part2JsonSupport.CONTROLSTREAM_COLLECTION_SCHEMA, "/controlstreams");
		return firstParentWithChild(controlStreams, "controlstreams", "commands",
				Part2JsonSupport.COMMAND_COLLECTION_SCHEMA, reqUri);
	}

	private Part2CandidateSelection.ParentChild firstParentWithChild(List<Map<String, Object>> parents,
			String parentPath, String childCollection, String childCollectionSchema, String reqUri) {
		return Part2CandidateSelection.firstParentWithChild(parents, parent -> {
			String parentId = stringValue(parent.get("id"));
			if (parentId == null || parentId.isBlank()) {
				return null;
			}
			return firstOptionalCollectionItemOrNull(
					parentPath + "/" + encodePathToken(parentId) + "/" + childCollection, reqUri, childCollectionSchema,
					"/" + parentPath + "/" + parentId + "/" + childCollection);
		});
	}

	private CommandEvidence firstCommandEvidence(String reqUri) {
		return firstCommandEvidenceFromCollections(commandCollectionEvidence(reqUri), reqUri);
	}

	private List<CollectionEvidence> commandCollectionEvidence(String reqUri) {
		List<CollectionEvidence> collections = new ArrayList<>();
		collections.add(requiredJsonCollectionEvidence("commands", reqUri, Part2JsonSupport.COMMAND_COLLECTION_SCHEMA,
				"/commands", false));
		List<Map<String, Object>> controlStreams = requiredCollectionResources("controlstreams", reqUri,
				Part2JsonSupport.CONTROLSTREAM_COLLECTION_SCHEMA, "/controlstreams");
		collections.addAll(validateNestedCollectionsForParents(controlStreams, "controlstreams", "commands",
				Part2JsonSupport.COMMAND_COLLECTION_SCHEMA, reqUri));
		return collections;
	}

	private CommandEvidence firstCommandEvidenceFromCollections(List<CollectionEvidence> collections, String reqUri) {
		ResourceEvidence evidence = firstResourceEvidence(collections, reqUri, "Command");
		return commandEvidence(evidence.resource(), reqUri, evidence.source());
	}

	private CommandEvidence commandEvidence(Map<String, Object> command, String reqUri, String source) {
		return new CommandEvidence(requireString(command, "id", reqUri), command, source);
	}

	private Map<String, Object> firstOptionalCollectionResource(String path, String reqUri, String collectionSchema,
			String source) {
		Map<String, Object> body = optionalCollectionBody(path, reqUri, collectionSchema, source);
		List<Map<String, Object>> items = Part2CandidateSelection.objectItems(body);
		if (items.isEmpty()) {
			throw new SkipException(reqUri + " - " + source
					+ " returned an empty collection; no candidate resource is available for schema-validation PASS.");
		}
		return items.get(0);
	}

	private Map<String, Object> firstOptionalCollectionItemOrNull(String path, String reqUri, String collectionSchema,
			String source) {
		try {
			Map<String, Object> body = optionalCollectionBody(path, reqUri, collectionSchema, source);
			List<Map<String, Object>> items = Part2CandidateSelection.objectItems(body);
			return items.isEmpty() ? null : items.get(0);
		}
		catch (SkipException ex) {
			return null;
		}
	}

	private Map<String, Object> optionalCollectionBody(String path, String reqUri, String collectionSchema,
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
		Map<String, Object> body = Part2JsonSupport.assertRequiredJsonResponse(response, reqUri, source);
		Part2JsonSupport.validateResponseAgainstSchema(response, collectionSchema, reqUri, source);
		return body;
	}

	private Map<String, Object> optionalJsonObject(String path, String reqUri, String label) {
		Response response = given().accept("application/json").when().get(this.baseUri.resolve(path)).andReturn();
		if (response.getStatusCode() != 200) {
			throw new SkipException(reqUri + " - " + label + " at /" + path + " returned HTTP "
					+ response.getStatusCode() + "; no parent schema evidence is available.");
		}
		return Part2JsonSupport.assertRequiredJsonResponse(response, reqUri, "/" + path);
	}

	private Map<String, Object> readJsonApiDefinitionOrFail() {
		if (this.landingResponse.getStatusCode() != 200 || this.landingBody == null) {
			ETSAssert.failWithUri(REQ_MEDIATYPE_WRITE,
					"landing page is not readable JSON, so no service-desc API definition can be inspected.");
		}
		URI serviceDescUri = serviceDescUri();
		if (serviceDescUri == null) {
			ETSAssert.failWithUri(REQ_MEDIATYPE_WRITE,
					"landing page does not expose a rel=service-desc link. service-doc/OPTIONS evidence is not mediatype-write PASS evidence.");
		}
		Response response = given().accept("application/vnd.oai.openapi+json, application/json")
			.when()
			.get(serviceDescUri)
			.andReturn();
		ETSAssert.assertStatus(response, 200, REQ_MEDIATYPE_WRITE);
		if (!Part2JsonSupport.isJsonDocumentContentType(response.getContentType())) {
			ETSAssert.failWithUri(REQ_MEDIATYPE_WRITE, "service-desc API definition returned Content-Type '"
					+ response.getContentType() + "'; expected JSON.");
		}
		Map<String, Object> body = Part2JsonSupport.parseBody(response);
		if (body == null) {
			ETSAssert.failWithUri(REQ_MEDIATYPE_WRITE,
					"service-desc API definition did not parse as JSON; no write media type advertisement PASS was reported.");
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

	private static Object firstPresent(Map<String, Object> body, String... keys) {
		if (body == null) {
			return null;
		}
		for (String key : keys) {
			if (body.containsKey(key)) {
				return body.get(key);
			}
		}
		return null;
	}

	private static String requireString(Map<String, Object> body, String key, String reqUri) {
		ETSAssert.assertJsonObjectHas(body, key, String.class, reqUri);
		return (String) body.get(key);
	}

	private static String stringValue(Object value) {
		return value instanceof String ? (String) value : null;
	}

	private static String encodePathToken(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
	}

	private static List<?> links(Map<String, Object> body) {
		if (body == null || !(body.get("links") instanceof List)) {
			return List.of();
		}
		return (List<?>) body.get("links");
	}

	private record CommandEvidence(String id, Map<String, Object> command, String source) {
	}

	private record CollectionEvidence(String source, TraversalResult traversal) {
	}

	private record ResourceEvidence(String source, Map<String, Object> resource) {
	}

}
