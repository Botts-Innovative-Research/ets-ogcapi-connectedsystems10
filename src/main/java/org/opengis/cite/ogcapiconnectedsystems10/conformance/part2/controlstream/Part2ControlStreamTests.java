package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.controlstream;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.PageDocument;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.TraversalResult;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.apicommon.Part2ApiCommonTests;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.samplingfeatures.SamplingFeaturesSupport;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Part 2 - Control Streams and Commands conformance tests
 * ({@code /conf/controlstream}; OGC 23-002 Annex A.3).
 */
public class Part2ControlStreamTests {

	static final String GROUP = "part2controlstream";

	static final String CONF_CONTROLSTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/controlstream";

	static final String CONF_PART2_API_COMMON = Part2ApiCommonTests.CONF_PART2_API_COMMON;

	static final String CONF_PART1_SAMPLING_FEATURES = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/sf";

	static final String CONF_PART1_SYSTEM = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/system";

	static final String CONF_PART1_DEPLOYMENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/deployment";

	static final String REQ_CONTROLSTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/controlstream";

	static final String REQ_SF_REF_FROM_CONTROLSTREAM = REQ_CONTROLSTREAM + "/sf-ref-from-controlstream";

	static final String REQ_FOI_REF_FROM_CONTROLSTREAM = REQ_CONTROLSTREAM + "/foi-ref-from-controlstream";

	static final String REQ_CANONICAL_URL = REQ_CONTROLSTREAM + "/canonical-url";

	static final String REQ_RESOURCES_ENDPOINT = REQ_CONTROLSTREAM + "/resources-endpoint";

	static final String REQ_CANONICAL_ENDPOINT = REQ_CONTROLSTREAM + "/canonical-endpoint";

	static final String REQ_REF_FROM_SYSTEM = REQ_CONTROLSTREAM + "/ref-from-system";

	static final String REQ_REF_FROM_DEPLOYMENT = REQ_CONTROLSTREAM + "/ref-from-deployment";

	static final String REQ_COLLECTIONS = REQ_CONTROLSTREAM + "/collections";

	static final String REQ_SCHEMA_OP = REQ_CONTROLSTREAM + "/schema-op";

	static final String REQ_CMD_CANONICAL_URL = REQ_CONTROLSTREAM + "/cmd-canonical-url";

	static final String REQ_CMD_RESOURCES_ENDPOINT = REQ_CONTROLSTREAM + "/cmd-resources-endpoint";

	static final String REQ_CMD_CANONICAL_ENDPOINT = REQ_CONTROLSTREAM + "/cmd-canonical-endpoint";

	static final String REQ_CMD_REF_FROM_CONTROLSTREAM = REQ_CONTROLSTREAM + "/cmd-ref-from-controlstream";

	static final String REQ_CMD_COLLECTIONS = REQ_CONTROLSTREAM + "/cmd-collections";

	static final String REQ_STATUS_RESOURCES_ENDPOINT = REQ_CONTROLSTREAM + "/status-resources-endpoint";

	static final String REQ_COMMAND_STATUS_ENDPOINT = REQ_CONTROLSTREAM + "/command-status-endpoint";

	static final String REQ_RESULT_RESOURCES_ENDPOINT = REQ_CONTROLSTREAM + "/result-resources-endpoint";

	static final String REQ_COMMAND_RESULT_ENDPOINT = REQ_CONTROLSTREAM + "/command-result-endpoint";

	private URI iutUri;

	private URI apiRoot;

	/**
	 * Loads immutable suite arguments after the released Part 2 API Common prerequisite.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass(dependsOnGroups = "part2apicommon", alwaysRun = true)
	public void fetchPart2ControlStreamInputs(ITestContext testContext) {
		skipWhenPrerequisiteUnsatisfied(testContext);
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		configure((URI) iutAttr);
	}

	void configure(URI iut) {
		if (iut == null || !iut.isAbsolute()) {
			throw new IllegalArgumentException("IUT must be an absolute URI.");
		}
		this.iutUri = iut;
		String iutString = this.iutUri.toString();
		this.apiRoot = URI.create(iutString.endsWith("/") ? iutString : iutString + "/");
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-ASSOCIATION-SUBRESOURCES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_SF_REF_FROM_CONTROLSTREAM
			+ ": every ControlStream exposes a readable SamplingFeature sub-resource endpoint (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-ASSOCIATION-SUBRESOURCES-001)",
			groups = GROUP, alwaysRun = true)
	public void controlStreamSamplingFeaturesAreAvailableFromControlStream() {
		Map<String, Object> conformance = requireControlStreamDeclaration(REQ_SF_REF_FROM_CONTROLSTREAM);
		skipIfConditionClassUndeclared(conformance, CONF_PART1_SAMPLING_FEATURES, REQ_SF_REF_FROM_CONTROLSTREAM,
				"Requirement 17 applies only when the Part 1 Sampling Features conformance class is declared.");
		validateSamplingFeatureSubresources("samplingFeatures", REQ_SF_REF_FROM_CONTROLSTREAM);
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-ASSOCIATION-SUBRESOURCES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_FOI_REF_FROM_CONTROLSTREAM
			+ ": every ControlStream exposes a readable FeatureOfInterest sub-resource endpoint when local FOI associations are evidenced (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-ASSOCIATION-SUBRESOURCES-001)",
			groups = GROUP, alwaysRun = true)
	public void controlStreamFeaturesOfInterestAreAvailableFromControlStream() {
		requireControlStreamDeclaration(REQ_FOI_REF_FROM_CONTROLSTREAM);
		TraversalResult controlStreams = controlStreams(REQ_FOI_REF_FROM_CONTROLSTREAM);
		skipUnlessFeatureOfInterestCondition(controlStreams, REQ_FOI_REF_FROM_CONTROLSTREAM);
		validateFeatureOfInterestSubresources(controlStreams, REQ_FOI_REF_FROM_CONTROLSTREAM);
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-CANONICAL-LINK-EVIDENCE-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CANONICAL_URL
			+ ": every ControlStream collection item dereferences its advertised canonical URL with equivalent content (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-CANONICAL-LINK-EVIDENCE-001)",
			groups = GROUP, alwaysRun = true)
	public void controlStreamCanonicalResourceReadable() {
		requireControlStreamDeclaration(REQ_CANONICAL_URL);
		validateCanonicalLinksForCollections("ControlStream", false, REQ_CANONICAL_URL);
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-RELEASED-ENDPOINT-SCHEMAS-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_RESOURCES_ENDPOINT
			+ ": ControlStream resource endpoint returns schema-valid content (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-RELEASED-ENDPOINT-SCHEMAS-001)",
			groups = GROUP, alwaysRun = true)
	public void controlStreamsCollectionReadable() {
		requireControlStreamDeclaration(REQ_RESOURCES_ENDPOINT);
		validateControlStreamEndpoint(this.apiRoot.resolve("controlstreams"), REQ_RESOURCES_ENDPOINT);
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-RELEASED-ENDPOINT-SCHEMAS-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CANONICAL_ENDPOINT
			+ ": canonical /controlstreams endpoint satisfies the ControlStream resource endpoint procedure (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-RELEASED-ENDPOINT-SCHEMAS-001)",
			groups = GROUP, alwaysRun = true)
	public void controlStreamsCanonicalEndpointExposesControlStreamItems() {
		requireControlStreamDeclaration(REQ_CANONICAL_ENDPOINT);
		validateControlStreamEndpoint(this.apiRoot.resolve("controlstreams"), REQ_CANONICAL_ENDPOINT);
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-ASSOCIATION-SUBRESOURCES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_REF_FROM_SYSTEM
			+ ": every System exposes a schema-valid ControlStream sub-resource endpoint (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-ASSOCIATION-SUBRESOURCES-001)",
			groups = GROUP, alwaysRun = true)
	public void systemScopedControlStreamsReadableWhenSystemReferencePresent() {
		Map<String, Object> conformance = requireControlStreamDeclaration(REQ_REF_FROM_SYSTEM);
		skipIfConditionClassUndeclared(conformance, CONF_PART1_SYSTEM, REQ_REF_FROM_SYSTEM,
				"Requirement 22 applies only when the Part 1 System conformance class is declared.");
		for (String systemId : localIdsForCanonicalResources("systems", REQ_REF_FROM_SYSTEM)) {
			validateControlStreamEndpoint(this.apiRoot.resolve("systems/" + encode(systemId) + "/controlstreams"),
					REQ_REF_FROM_SYSTEM);
		}
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-ASSOCIATION-SUBRESOURCES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_REF_FROM_DEPLOYMENT
			+ ": every applicable Deployment exposes a schema-valid ControlStream sub-resource endpoint (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-ASSOCIATION-SUBRESOURCES-001)",
			groups = GROUP, alwaysRun = true)
	public void deploymentScopedControlStreamsReadableWhenDeploymentReferencePresent() {
		Map<String, Object> conformance = requireControlStreamDeclaration(REQ_REF_FROM_DEPLOYMENT);
		skipIfConditionClassUndeclared(conformance, CONF_PART1_DEPLOYMENT, REQ_REF_FROM_DEPLOYMENT,
				"Requirement 23 applies only when the Part 1 Deployment conformance class is declared.");
		for (String deploymentId : localIdsForCanonicalResourcesWithAssociation("deployments", "controlstreams",
				REQ_REF_FROM_DEPLOYMENT)) {
			validateControlStreamEndpoint(
					this.apiRoot.resolve("deployments/" + encode(deploymentId) + "/controlstreams"),
					REQ_REF_FROM_DEPLOYMENT);
		}
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-COLLECTION-TAGGING-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COLLECTIONS
			+ ": every advertised ControlStream collection retrieves schema-valid items (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-COLLECTION-TAGGING-001)",
			groups = GROUP, alwaysRun = true)
	public void controlStreamCollectionsAreTaggedWithItemType() {
		requireControlStreamDeclaration(REQ_COLLECTIONS);
		validateAdvertisedCollections("ControlStream", ResourceKind.CONTROL_STREAM, REQ_COLLECTIONS);
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-SCHEMA-OP-FORMATS-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_SCHEMA_OP
			+ ": every ControlStream exposes /schema for every advertised Command format (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-SCHEMA-OP-FORMATS-001)",
			groups = GROUP, alwaysRun = true)
	public void controlStreamSchemaReadable() {
		requireControlStreamDeclaration(REQ_SCHEMA_OP);
		int checked = 0;
		for (Map<String, Object> controlStream : controlStreams(REQ_SCHEMA_OP).items()) {
			String id = requireString(controlStream, "id", REQ_SCHEMA_OP);
			List<String> formats = Part2ControlStreamSupport.commandFormats(controlStream);
			if (formats.isEmpty()) {
				ETSAssert.failWithUri(REQ_SCHEMA_OP,
						"ControlStream '" + id + "' does not list supported Command formats.");
			}
			for (String format : formats) {
				Response response = given().accept("application/json")
					.queryParam("cmdFormat", format)
					.when()
					.get(this.apiRoot.resolve("controlstreams/" + encode(id) + "/schema"))
					.andReturn();
				Part2ControlStreamSupport.validateCommandSchema(response, format, REQ_SCHEMA_OP,
						"/controlstreams/" + id + "/schema?cmdFormat=" + format);
				checked++;
			}
		}
		if (checked == 0) {
			throw new SkipException(REQ_SCHEMA_OP + " - no ControlStream resource was available for schema-op checks.");
		}
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-CANONICAL-LINK-EVIDENCE-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CMD_CANONICAL_URL
			+ ": every Command collection item dereferences its advertised canonical URL with equivalent content (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-CANONICAL-LINK-EVIDENCE-001)",
			groups = GROUP, alwaysRun = true)
	public void commandCanonicalResourceReadableWhenCollectionPopulated() {
		requireControlStreamDeclaration(REQ_CMD_CANONICAL_URL);
		validateCanonicalLinksForCollections("Command", true, REQ_CMD_CANONICAL_URL);
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-RELEASED-ENDPOINT-SCHEMAS-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CMD_RESOURCES_ENDPOINT
			+ ": Command resource endpoints nested under ControlStreams return schema-valid content (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-RELEASED-ENDPOINT-SCHEMAS-001)",
			groups = GROUP, alwaysRun = true)
	public void controlStreamScopedCommandsReadable() {
		requireControlStreamDeclaration(REQ_CMD_RESOURCES_ENDPOINT);
		for (String controlStreamId : controlStreamIds(REQ_CMD_RESOURCES_ENDPOINT)) {
			validateCommandEndpoint(this.apiRoot.resolve("controlstreams/" + encode(controlStreamId) + "/commands"),
					REQ_CMD_RESOURCES_ENDPOINT);
		}
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-RELEASED-ENDPOINT-SCHEMAS-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CMD_CANONICAL_ENDPOINT
			+ ": canonical /commands endpoint satisfies the Command resource endpoint procedure (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-RELEASED-ENDPOINT-SCHEMAS-001)",
			groups = GROUP, alwaysRun = true)
	public void commandsCanonicalEndpointReadable() {
		requireControlStreamDeclaration(REQ_CMD_CANONICAL_ENDPOINT);
		validateCommandEndpoint(this.apiRoot.resolve("commands"), REQ_CMD_CANONICAL_ENDPOINT);
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-ASSOCIATION-SUBRESOURCES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CMD_REF_FROM_CONTROLSTREAM
			+ ": every ControlStream exposes schema-valid Command sub-resource endpoints with parent reference evidence when populated (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-ASSOCIATION-SUBRESOURCES-001)",
			groups = GROUP, alwaysRun = true)
	public void commandsReferenceSelectedControlStreamWhenNestedCollectionPopulated() {
		requireControlStreamDeclaration(REQ_CMD_REF_FROM_CONTROLSTREAM);
		int commandItems = 0;
		for (String controlStreamId : controlStreamIds(REQ_CMD_REF_FROM_CONTROLSTREAM)) {
			URI endpoint = this.apiRoot.resolve("controlstreams/" + encode(controlStreamId) + "/commands");
			TraversalResult traversal = validateCommandEndpointWithEvidence(endpoint, REQ_CMD_REF_FROM_CONTROLSTREAM);
			for (Map<String, Object> command : traversal.items()) {
				commandItems++;
				if (!commandReferencesControlStream(command, controlStreamId)) {
					ETSAssert.failWithUri(REQ_CMD_REF_FROM_CONTROLSTREAM,
							endpoint + " returned Command '" + command.get("id")
									+ "' without reference evidence for ControlStream " + controlStreamId + ".");
				}
			}
		}
		if (commandItems == 0) {
			throw new SkipException(REQ_CMD_REF_FROM_CONTROLSTREAM
					+ " - ControlStream-scoped Command endpoints are empty; no Command reference evidence is available.");
		}
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-COLLECTION-TAGGING-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CMD_COLLECTIONS
			+ ": every advertised Command collection retrieves schema-valid items (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-COLLECTION-TAGGING-001)",
			groups = GROUP, alwaysRun = true)
	public void commandCollectionsAreTaggedWithItemType() {
		requireControlStreamDeclaration(REQ_CMD_COLLECTIONS);
		validateAdvertisedCollections("Command", ResourceKind.COMMAND, REQ_CMD_COLLECTIONS);
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-STATUS-RESULT-ENDPOINTS-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_STATUS_RESOURCES_ENDPOINT
			+ ": CommandStatus resources endpoint returns schema-valid content (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-STATUS-RESULT-ENDPOINTS-001)",
			groups = GROUP, alwaysRun = true)
	public void commandStatusResourcesEndpointReadable() {
		requireControlStreamDeclaration(REQ_STATUS_RESOURCES_ENDPOINT);
		String commandId = commandIds(REQ_STATUS_RESOURCES_ENDPOINT).get(0);
		validateCommandStatusEndpoint(this.apiRoot.resolve("commands/" + encode(commandId) + "/status"),
				REQ_STATUS_RESOURCES_ENDPOINT);
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-STATUS-RESULT-ENDPOINTS-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COMMAND_STATUS_ENDPOINT
			+ ": every Command exposes a schema-valid CommandStatus endpoint (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-STATUS-RESULT-ENDPOINTS-001)",
			groups = GROUP, alwaysRun = true)
	public void commandStatusEndpointReadableForEveryCommand() {
		requireControlStreamDeclaration(REQ_COMMAND_STATUS_ENDPOINT);
		for (String commandId : commandIds(REQ_COMMAND_STATUS_ENDPOINT)) {
			URI endpoint = this.apiRoot.resolve("commands/" + encode(commandId) + "/status");
			TraversalResult traversal = validateCommandStatusEndpointWithEvidence(endpoint,
					REQ_COMMAND_STATUS_ENDPOINT);
			for (Map<String, Object> status : traversal.items()) {
				if (!commandId.equals(stringValue(status.get("command@id")))) {
					ETSAssert.failWithUri(REQ_COMMAND_STATUS_ENDPOINT, endpoint + " returned CommandStatus '"
							+ status.get("id") + "' without command@id reference to " + commandId + ".");
				}
			}
		}
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-STATUS-RESULT-ENDPOINTS-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_RESULT_RESOURCES_ENDPOINT
			+ ": CommandResult resources endpoint returns schema-valid content (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-STATUS-RESULT-ENDPOINTS-001)",
			groups = GROUP, alwaysRun = true)
	public void commandResultResourcesEndpointReadable() {
		requireControlStreamDeclaration(REQ_RESULT_RESOURCES_ENDPOINT);
		String commandId = commandIds(REQ_RESULT_RESOURCES_ENDPOINT).get(0);
		validateCommandResultEndpoint(this.apiRoot.resolve("commands/" + encode(commandId) + "/result"),
				REQ_RESULT_RESOURCES_ENDPOINT);
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-STATUS-RESULT-ENDPOINTS-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COMMAND_RESULT_ENDPOINT
			+ ": every Command exposes a schema-valid CommandResult endpoint (REQ-ETS-PART2-003, SCENARIO-ETS-PART2-003-STATUS-RESULT-ENDPOINTS-001)",
			groups = GROUP, alwaysRun = true)
	public void commandResultEndpointReadableForEveryCommand() {
		requireControlStreamDeclaration(REQ_COMMAND_RESULT_ENDPOINT);
		for (String commandId : commandIds(REQ_COMMAND_RESULT_ENDPOINT)) {
			URI endpoint = this.apiRoot.resolve("commands/" + encode(commandId) + "/result");
			TraversalResult traversal = validateCommandResultEndpointWithEvidence(endpoint,
					REQ_COMMAND_RESULT_ENDPOINT);
			for (Map<String, Object> result : traversal.items()) {
				if (!commandId.equals(stringValue(result.get("command@id")))) {
					ETSAssert.failWithUri(REQ_COMMAND_RESULT_ENDPOINT, endpoint + " returned CommandResult '"
							+ result.get("id") + "' without command@id reference to " + commandId + ".");
				}
			}
		}
	}

	static boolean declaresConformance(Map<String, Object> body, String conformanceUri) {
		return Part2ApiCommonTests.declaresConformance(body, conformanceUri);
	}

	static boolean hasControlStreamShape(Map<String, Object> body) {
		if (body == null || !(body.get("id") instanceof String)) {
			return false;
		}
		boolean hasSystem = body.containsKey("system@id") || body.containsKey("system@link");
		return hasSystem && body.containsKey("inputName") && body.get("controlledProperties") instanceof List
				&& body.get("formats") instanceof List && body.containsKey("async");
	}

	static boolean hasCommandShape(Object body) {
		if (!(body instanceof Map)) {
			return false;
		}
		Map<?, ?> command = (Map<?, ?>) body;
		return command.get("id") instanceof String && (command.containsKey("controlstream@id")
				|| command.containsKey("controlStream@id") || command.containsKey("controlstream")
				|| command.containsKey("controlStream") || command.containsKey("issueTime")
				|| command.containsKey("parameters") || command.containsKey("currentStatus"));
	}

	static boolean commandReferencesControlStream(Object command, String controlStreamId) {
		if (!(command instanceof Map) || controlStreamId == null || controlStreamId.isBlank()) {
			return false;
		}
		Map<?, ?> cmd = (Map<?, ?>) command;
		if (controlStreamId.equals(stringValue(cmd.get("controlstream@id")))
				|| controlStreamId.equals(stringValue(cmd.get("controlStream@id")))
				|| controlStreamId.equals(stringValue(cmd.get("controlstreamId")))
				|| controlStreamId.equals(stringValue(cmd.get("controlStreamId")))) {
			return true;
		}
		Object controlStream = cmd.get("controlstream");
		if (!(controlStream instanceof Map)) {
			controlStream = cmd.get("controlStream");
		}
		if (controlStream instanceof Map
				&& controlStreamId.equals(stringValue(((Map<?, ?>) controlStream).get("id")))) {
			return true;
		}
		return linksContainControlStream(cmd.get("links"), controlStreamId)
				|| linksContainControlStream(cmd.get("controlstream@link"), controlStreamId)
				|| linksContainControlStream(cmd.get("controlStream@link"), controlStreamId);
	}

	static boolean hasItemsOnlyCollectionShape(Map<String, Object> body) {
		return body != null && body.get("items") instanceof List;
	}

	static boolean isCollectionTagged(Map<String, Object> collection, String itemType) {
		return Part2ControlStreamSupport.isCollectionTagged(collection, itemType);
	}

	private Map<String, Object> requireControlStreamDeclaration(String requirement) {
		Response response = given().accept("application/json")
			.when()
			.get(this.apiRoot.resolve("conformance"))
			.andReturn();
		ETSAssert.assertStatus(response, 200, requirement);
		Map<String, Object> parsed = parseBody(response);
		if (parsed == null) {
			ETSAssert.failWithUri(requirement,
					"/conformance body did not parse as JSON. Content-Type was: " + response.getContentType());
		}
		ETSAssert.assertJsonObjectHas(parsed, "conformsTo", List.class, requirement);
		if (!declaresConformance(parsed, CONF_CONTROLSTREAM)) {
			throw new SkipException(CONF_CONTROLSTREAM
					+ " - IUT does not declare the CS API Part 2 Control Streams and Commands conformance class in /conformance.");
		}
		return parsed;
	}

	private static void skipIfConditionClassUndeclared(Map<String, Object> conformance, String conformanceClass,
			String requirement, String reason) {
		if (!declaresConformance(conformance, conformanceClass)) {
			throw new SkipException(requirement + " - " + reason + " Missing exact URI " + conformanceClass + ".");
		}
	}

	private TraversalResult controlStreams(String requirement) {
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.canonicalResourcesDetailed(this.apiRoot,
				"controlstreams");
		if (evidence.isEmpty()) {
			ETSAssert.failWithUri(requirement, this.apiRoot.resolve("controlstreams") + " returned HTTP 404.");
		}
		TraversalResult traversal = evidence.orElseThrow();
		Part2ControlStreamSupport.validateControlStreamEndpoint(this.apiRoot.resolve("controlstreams"),
				traversal.pages(), requirement);
		return traversal;
	}

	private List<String> controlStreamIds(String requirement) {
		List<String> ids = Part2ControlStreamSupport.localIds(controlStreams(requirement).items(), requirement);
		if (ids.isEmpty()) {
			throw new SkipException(
					requirement + " - no ControlStream resources are available for per-resource checks.");
		}
		return ids;
	}

	private Optional<TraversalResult> commandsMaybe(String requirement) {
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.canonicalResourcesDetailed(this.apiRoot, "commands");
		if (evidence.isEmpty()) {
			return Optional.empty();
		}
		TraversalResult traversal = evidence.orElseThrow();
		Part2ControlStreamSupport.validateCommandEndpoint(this.apiRoot.resolve("commands"), traversal.pages(),
				requirement);
		return Optional.of(traversal);
	}

	private List<String> commandIds(String requirement) {
		Optional<TraversalResult> evidence = commandsMaybe(requirement);
		if (evidence.isEmpty()) {
			throw new SkipException(requirement
					+ " - canonical /commands endpoint is unavailable; no Command resources are available for child endpoint checks.");
		}
		List<String> ids = Part2ControlStreamSupport.localIds(evidence.orElseThrow().items(), requirement);
		if (ids.isEmpty()) {
			throw new SkipException(requirement + " - no Command resources are available for child endpoint checks.");
		}
		return ids;
	}

	private List<String> localIdsForCanonicalResources(String resourceType, String requirement) {
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.canonicalResourcesDetailed(this.apiRoot,
				resourceType);
		if (evidence.isEmpty()) {
			ETSAssert.failWithUri(requirement, this.apiRoot.resolve(resourceType) + " returned HTTP 404.");
		}
		List<String> ids = Part2ControlStreamSupport.localIds(evidence.orElseThrow().items(), requirement);
		if (ids.isEmpty()) {
			throw new SkipException(
					requirement + " - no " + resourceType + " resources are available for per-resource checks.");
		}
		return ids;
	}

	private List<String> localIdsForCanonicalResourcesWithAssociation(String resourceType, String association,
			String requirement) {
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.canonicalResourcesDetailed(this.apiRoot,
				resourceType);
		if (evidence.isEmpty()) {
			ETSAssert.failWithUri(requirement, this.apiRoot.resolve(resourceType) + " returned HTTP 404.");
		}
		List<Map<String, Object>> resources = evidence.orElseThrow().items();
		List<Map<String, Object>> applicable = new ArrayList<>();
		for (Map<String, Object> resource : resources) {
			if (hasLocalAssociationEvidence(resource, association, this.apiRoot)) {
				applicable.add(resource);
			}
		}
		if (applicable.isEmpty()) {
			throw new SkipException(requirement + " - " + resourceType
					+ " resources did not advertise a locally hosted " + association + " association.");
		}
		return Part2ControlStreamSupport.localIds(applicable, requirement);
	}

	private void validateSamplingFeatureSubresources(String childPath, String requirement) {
		List<URI> unsupported = new ArrayList<>();
		for (String controlStreamId : controlStreamIds(requirement)) {
			URI endpoint = this.apiRoot.resolve("controlstreams/" + encode(controlStreamId) + "/" + childPath);
			Optional<TraversalResult> evidence = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint,
					"application/geo+json, application/json", Map.of(), requirement);
			if (evidence.isEmpty()) {
				ETSAssert.failWithUri(requirement, endpoint + " returned HTTP 404.");
			}
			if (!SamplingFeaturesSupport.validateSamplingFeatureEndpoint(endpoint, evidence.orElseThrow().pages(),
					requirement)) {
				unsupported.add(endpoint);
			}
		}
		if (!unsupported.isEmpty()) {
			throw new SkipException(requirement + " - " + childPath
					+ " endpoint validation could not execute for unsupported media at " + unsupported + ".");
		}
	}

	private void skipUnlessFeatureOfInterestCondition(TraversalResult controlStreams, String requirement) {
		if (!hasLocalFeatureOfInterestCondition(controlStreams.items(), this.apiRoot)) {
			throw new SkipException(requirement
					+ " - Requirement 18 conditions are not evidenced: no ControlStream representation advertises a locally hosted featuresOfInterest association.");
		}
	}

	private void validateFeatureOfInterestSubresources(TraversalResult controlStreams, String requirement) {
		int checked = 0;
		List<String> ids = Part2ControlStreamSupport.localIds(controlStreams.items(), requirement);
		if (ids.isEmpty()) {
			throw new SkipException(
					requirement + " - no ControlStream resources are available for FeatureOfInterest checks.");
		}
		for (String controlStreamId : ids) {
			URI endpoint = this.apiRoot.resolve("controlstreams/" + encode(controlStreamId) + "/featuresOfInterest");
			Optional<TraversalResult> evidence = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint,
					"application/geo+json, application/json", Map.of(), requirement,
					Part2ControlStreamSupport.FEATURE_OF_INTEREST_MEDIA);
			if (evidence.isEmpty()) {
				ETSAssert.failWithUri(requirement, endpoint + " returned HTTP 404.");
			}
			Part2ControlStreamSupport.validateFeatureOfInterestEndpoint(endpoint, evidence.orElseThrow().pages(),
					requirement);
			checked++;
		}
		if (checked == 0) {
			throw new SkipException(
					requirement + " - no ControlStream resources are available for FeatureOfInterest checks.");
		}
	}

	private void validateControlStreamEndpoint(URI endpoint, String requirement) {
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint,
				Part2ControlStreamSupport.JSON, Map.of(), requirement, Part2ControlStreamSupport.JSON_MEDIA);
		if (evidence.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " returned HTTP 404.");
		}
		Part2ControlStreamSupport.validateControlStreamEndpoint(endpoint, evidence.orElseThrow().pages(), requirement);
	}

	private void validateCommandEndpoint(URI endpoint, String requirement) {
		validateCommandEndpointWithEvidence(endpoint, requirement);
	}

	private TraversalResult validateCommandEndpointWithEvidence(URI endpoint, String requirement) {
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint,
				Part2ControlStreamSupport.JSON, Map.of(), requirement, Part2ControlStreamSupport.JSON_MEDIA);
		if (evidence.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " returned HTTP 404.");
		}
		TraversalResult traversal = evidence.orElseThrow();
		Part2ControlStreamSupport.validateCommandEndpoint(endpoint, traversal.pages(), requirement);
		return traversal;
	}

	private void validateCommandStatusEndpoint(URI endpoint, String requirement) {
		validateCommandStatusEndpointWithEvidence(endpoint, requirement);
	}

	private TraversalResult validateCommandStatusEndpointWithEvidence(URI endpoint, String requirement) {
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint,
				Part2ControlStreamSupport.JSON, Map.of(), requirement, Part2ControlStreamSupport.JSON_MEDIA);
		if (evidence.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " returned HTTP 404.");
		}
		TraversalResult traversal = evidence.orElseThrow();
		Part2ControlStreamSupport.validateCommandStatusEndpoint(endpoint, traversal.pages(), requirement);
		return traversal;
	}

	private void validateCommandResultEndpoint(URI endpoint, String requirement) {
		validateCommandResultEndpointWithEvidence(endpoint, requirement);
	}

	private TraversalResult validateCommandResultEndpointWithEvidence(URI endpoint, String requirement) {
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint,
				Part2ControlStreamSupport.JSON, Map.of(), requirement, Part2ControlStreamSupport.JSON_MEDIA);
		if (evidence.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " returned HTTP 404.");
		}
		TraversalResult traversal = evidence.orElseThrow();
		Part2ControlStreamSupport.validateCommandResultEndpoint(endpoint, traversal.pages(), requirement);
		return traversal;
	}

	private void validateAdvertisedCollections(String itemType, ResourceKind kind, String requirement) {
		List<Map<String, Object>> collections = advertisedCollections(itemType, requirement);
		int supportedCollections = 0;
		for (Map<String, Object> collection : collections) {
			Optional<TraversalResult> evidence = Part1ApiCommonSupport.collectionItemsDetailed(this.apiRoot, collection,
					Part2ControlStreamSupport.JSON_MEDIA);
			if (evidence.isEmpty()) {
				continue;
			}
			supportedCollections++;
			URI endpoint = collectionItemsUri(collection, requirement);
			validateTraversalForKind(endpoint, evidence.orElseThrow().pages(), kind, requirement);
		}
		if (supportedCollections == 0) {
			throw new SkipException(requirement + " - every advertised " + itemType
					+ " collection lacked a supported rel=items application/json link.");
		}
	}

	private void validateCanonicalLinksForCollections(String itemType, boolean command, String requirement) {
		List<Map<String, Object>> collections = advertisedCollections(itemType, requirement);
		int supportedCollections = 0;
		int inspectedItems = 0;
		for (Map<String, Object> collection : collections) {
			Optional<TraversalResult> evidence = Part1ApiCommonSupport.collectionItemsDetailed(this.apiRoot, collection,
					Part2ControlStreamSupport.JSON_MEDIA);
			if (evidence.isEmpty()) {
				continue;
			}
			supportedCollections++;
			TraversalResult traversal = evidence.orElseThrow();
			URI endpoint = collectionItemsUri(collection, requirement);
			validateTraversalForKind(endpoint, traversal.pages(),
					command ? ResourceKind.COMMAND : ResourceKind.CONTROL_STREAM, requirement);
			for (PageDocument page : traversal.pages()) {
				for (Map<String, Object> item : page.items()) {
					inspectedItems++;
					URI canonical = Part2ControlStreamSupport.canonicalUri(item, page.source(), this.apiRoot,
							requirement);
					Response response = given().accept(page.mediaType()).when().get(canonical).andReturn();
					ETSAssert.assertStatus(response, 200, requirement);
					Map<String, Object> canonicalBody = Part2ControlStreamSupport.parseObject(response, canonical,
							requirement);
					if (command) {
						validateCommandSingleton(canonicalBody, requirement, canonical);
					}
					else {
						validateControlStreamSingleton(canonicalBody, requirement, canonical);
					}
					JsonNode expected = Part2ControlStreamSupport.withoutCanonicalLinks(item);
					JsonNode actual = Part2ControlStreamSupport.withoutCanonicalLinks(canonicalBody);
					if (!expected.equals(actual)) {
						ETSAssert.failWithUri(requirement, canonical
								+ " content differs from its collection item after canonical links are removed.");
					}
				}
			}
		}
		if (supportedCollections == 0) {
			throw new SkipException(requirement + " - every advertised " + itemType
					+ " collection lacked a supported rel=items application/json link.");
		}
		if (inspectedItems == 0) {
			throw new SkipException(requirement + " - supported " + itemType
					+ " collections were empty; no canonical resource evidence is available.");
		}
	}

	private void validateTraversalForKind(URI endpoint, List<PageDocument> pages, ResourceKind kind,
			String requirement) {
		switch (kind) {
			case CONTROL_STREAM ->
				Part2ControlStreamSupport.validateControlStreamEndpoint(endpoint, pages, requirement);
			case COMMAND -> Part2ControlStreamSupport.validateCommandEndpoint(endpoint, pages, requirement);
			case COMMAND_STATUS ->
				Part2ControlStreamSupport.validateCommandStatusEndpoint(endpoint, pages, requirement);
			case COMMAND_RESULT ->
				Part2ControlStreamSupport.validateCommandResultEndpoint(endpoint, pages, requirement);
		}
	}

	private List<Map<String, Object>> advertisedCollections(String itemType, String requirement) {
		Response response = given().accept("application/json")
			.when()
			.get(this.apiRoot.resolve("collections"))
			.andReturn();
		ETSAssert.assertStatus(response, 200, requirement);
		Map<String, Object> body = Part2ControlStreamSupport.parseObject(response, this.apiRoot.resolve("collections"),
				requirement);
		Object advertised = body.get("collections");
		if (!(advertised instanceof List)) {
			ETSAssert.failWithUri(requirement, "/collections response is missing a collections array.");
		}
		List<Map<String, Object>> typed = new ArrayList<>();
		for (Object value : (List<?>) advertised) {
			if (value instanceof Map) {
				typed.add(castMap(value));
			}
		}
		List<Map<String, Object>> selected = Part2ControlStreamSupport.collectionsWithItemType(typed, itemType);
		if (selected.isEmpty()) {
			throw new SkipException(requirement + " - /collections does not advertise itemType=" + itemType + ".");
		}
		return selected;
	}

	private void validateControlStreamSingleton(Map<String, Object> body, String requirement, URI source) {
		Part2ControlStreamSupport.validateControlStreamResource(body, requirement, source.toString());
	}

	private void validateCommandSingleton(Map<String, Object> body, String requirement, URI source) {
		Part2ControlStreamSupport.validateCommandResource(body, requirement, source.toString());
	}

	private URI collectionItemsUri(Map<String, Object> collection, String requirement) {
		String id = requireString(collection, "id", requirement);
		String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8).replace("+", "%20");
		return this.apiRoot.resolve("collections/" + encodedId + "/items");
	}

	static boolean hasLocalFeatureOfInterestCondition(List<Map<String, Object>> controlStreams, URI apiRoot) {
		if (controlStreams == null || apiRoot == null) {
			return false;
		}
		for (Map<String, Object> controlStream : controlStreams) {
			if (hasLocalAssociationEvidence(controlStream, "featuresOfInterest", apiRoot)
					|| hasLocalAssociationEvidence(controlStream, "featureOfInterest", apiRoot)) {
				return true;
			}
		}
		return false;
	}

	static boolean hasLocalAssociationEvidence(Map<String, Object> resource, String association, URI apiRoot) {
		if (resource == null || association == null || association.isBlank() || apiRoot == null) {
			return false;
		}
		Set<String> tokens = associationTokens(association);
		if (localLinkMatchesAnyToken(resource.get("links"), tokens, apiRoot)) {
			return true;
		}
		for (String member : associationMemberNames(association)) {
			Object value = resource.get(member);
			if (localLinkMatchesAnyToken(value, tokens, apiRoot)) {
				return true;
			}
			if (isEmbeddedAssociationMember(member, association) && containsInlineDescription(value)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> castMap(Object value) {
		return (Map<String, Object>) value;
	}

	private static String requireString(Map<String, Object> parsed, String key, String reqUri) {
		ETSAssert.assertJsonObjectHas(parsed, key, String.class, reqUri);
		return (String) parsed.get(key);
	}

	private static String encode(String value) {
		return Part2ControlStreamSupport.encodePathToken(value);
	}

	private static boolean linksContainControlStream(Object links, String controlStreamId) {
		if (links instanceof Map) {
			return linkReferencesControlStream((Map<?, ?>) links, controlStreamId);
		}
		if (!(links instanceof List)) {
			return false;
		}
		for (Object link : (List<?>) links) {
			if (link instanceof Map && linkReferencesControlStream((Map<?, ?>) link, controlStreamId)) {
				return true;
			}
		}
		return false;
	}

	private static boolean linkReferencesControlStream(Map<?, ?> link, String controlStreamId) {
		String href = stringValue(link.get("href"));
		return href != null && (href.contains("/controlstreams/" + controlStreamId)
				|| href.contains("/controls/" + controlStreamId));
	}

	private static boolean linkMatchesAnyToken(Object links, Set<String> tokens) {
		if (links instanceof Map) {
			return linkMatchesAnyToken((Map<?, ?>) links, tokens);
		}
		if (!(links instanceof List)) {
			return false;
		}
		for (Object link : (List<?>) links) {
			if (link instanceof Map && linkMatchesAnyToken((Map<?, ?>) link, tokens)) {
				return true;
			}
		}
		return false;
	}

	private static boolean linkMatchesAnyToken(Map<?, ?> link, Set<String> tokens) {
		String href = lower(stringValue(link.get("href")));
		String rel = lower(stringValue(link.get("rel")));
		String title = lower(stringValue(link.get("title")));
		return tokens.stream().anyMatch(token -> href.contains(token) || rel.contains(token) || title.contains(token));
	}

	private static boolean localLinkMatchesAnyToken(Object links, Set<String> tokens, URI apiRoot) {
		if (links instanceof Map) {
			return localLinkMatchesAnyToken((Map<?, ?>) links, tokens, apiRoot);
		}
		if (!(links instanceof List)) {
			return false;
		}
		for (Object link : (List<?>) links) {
			if (link instanceof Map && localLinkMatchesAnyToken((Map<?, ?>) link, tokens, apiRoot)) {
				return true;
			}
		}
		return false;
	}

	private static boolean localLinkMatchesAnyToken(Map<?, ?> link, Set<String> tokens, URI apiRoot) {
		String href = stringValue(link.get("href"));
		if (href == null || !linkMatchesAnyToken(link, tokens)) {
			return false;
		}
		try {
			URI resolved = apiRoot.resolve(href);
			return sameOrigin(apiRoot, resolved);
		}
		catch (IllegalArgumentException ex) {
			return false;
		}
	}

	private static boolean sameOrigin(URI expected, URI actual) {
		return expected.getScheme() != null && actual.getScheme() != null
				&& expected.getScheme().equalsIgnoreCase(actual.getScheme()) && expected.getHost() != null
				&& actual.getHost() != null && expected.getHost().equalsIgnoreCase(actual.getHost())
				&& effectivePort(expected) == effectivePort(actual);
	}

	private static int effectivePort(URI uri) {
		if (uri.getPort() >= 0) {
			return uri.getPort();
		}
		if ("https".equalsIgnoreCase(uri.getScheme())) {
			return 443;
		}
		if ("http".equalsIgnoreCase(uri.getScheme())) {
			return 80;
		}
		return -1;
	}

	private static Set<String> associationTokens(String association) {
		return switch (association) {
			case "samplingFeatures" ->
				Set.of("samplingfeatures", "sampling-features", "sampling_features", "samples", "sf");
			case "featuresOfInterest", "featureOfInterest" ->
				Set.of("featuresofinterest", "featureofinterest", "features-of-interest", "feature-of-interest",
						"features_of_interest", "feature_of_interest", "fois", "foi");
			case "controlstreams", "controlStreams" ->
				Set.of("controlstreams", "control-streams", "control_streams", "controls");
			case "deployments" -> Set.of("deployments", "deployment");
			default -> Set.of(association.toLowerCase(Locale.ROOT));
		};
	}

	private static List<String> associationMemberNames(String association) {
		return switch (association) {
			case "samplingFeatures" ->
				List.of("samplingFeatures", "samplingFeatures@id", "samplingFeaturesId", "samplingFeatures@link",
						"samplingFeature", "samplingFeature@id", "samplingFeatureId", "samplingFeature@link");
			case "featuresOfInterest",
					"featureOfInterest" ->
				List.of("featuresOfInterest", "featuresOfInterest@id", "featuresOfInterestId",
						"featuresOfInterest@link", "featureOfInterest", "featureOfInterest@id", "featureOfInterestId",
						"featureOfInterest@link");
			case "controlstreams", "controlStreams" ->
				List.of("controlstreams", "controlstreams@id", "controlstreamsId", "controlstreams@link",
						"controlStreams", "controlStreams@id", "controlStreamsId", "controlStreams@link");
			default -> List.of(association, association + "@id", association + "Id", association + "@link");
		};
	}

	private static boolean isEmbeddedAssociationMember(String member, String association) {
		return associationMemberNames(association).contains(member) && !member.endsWith("@link")
				&& !member.endsWith("@id") && !member.endsWith("Id");
	}

	private static boolean containsInlineDescription(Object value) {
		if (value instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) value;
			return map.containsKey("id") && !map.containsKey("href");
		}
		if (!(value instanceof List)) {
			return false;
		}
		for (Object item : (List<?>) value) {
			if (containsInlineDescription(item)) {
				return true;
			}
		}
		return false;
	}

	private static String lower(String value) {
		return value == null ? "" : value.toLowerCase(Locale.ROOT);
	}

	private static String stringValue(Object value) {
		return value instanceof String ? (String) value : null;
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

	private static void skipWhenPrerequisiteUnsatisfied(ITestContext testContext) {
		String blocker = configurationBlocker(testContext.getFailedConfigurations(), "failed");
		if (blocker == null) {
			blocker = configurationBlocker(testContext.getSkippedConfigurations(), "skipped");
		}
		if (blocker == null) {
			blocker = testBlocker(testContext.getFailedTests(), "failed");
		}
		if (blocker == null) {
			blocker = testBlocker(testContext.getSkippedTests(), "skipped");
		}
		if (blocker != null) {
			throw new SkipException(
					"Part 2 ControlStream setup skipped before IUT access because prerequisite " + blocker + ".");
		}
	}

	private static String configurationBlocker(IResultMap results, String status) {
		if (results == null) {
			return null;
		}
		for (ITestResult result : results.getAllResults()) {
			if (result != null && result.getMethod() != null && isPart2ApiCommonPrerequisite(result)) {
				return "configuration " + result.getMethod().getMethodName() + " " + status;
			}
		}
		return null;
	}

	private static String testBlocker(IResultMap results, String status) {
		if (results == null) {
			return null;
		}
		for (ITestResult result : results.getAllResults()) {
			if (result != null && result.getMethod() != null && isPart2ApiCommonPrerequisite(result)) {
				return "method " + result.getMethod().getMethodName() + " " + status;
			}
		}
		return null;
	}

	private static boolean isPart2ApiCommonPrerequisite(ITestResult result) {
		for (String group : result.getMethod().getGroups()) {
			if ("part2apicommon".equals(group)) {
				return true;
			}
		}
		return Part2ApiCommonTests.class.equals(result.getMethod().getRealClass());
	}

	private enum ResourceKind {

		CONTROL_STREAM, COMMAND, COMMAND_STATUS, COMMAND_RESULT

	}

}
