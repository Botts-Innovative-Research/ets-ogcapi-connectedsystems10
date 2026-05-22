package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.update;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.TestRunArg;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.apicommon.Part2ApiCommonTests;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Part 2 - Update/PATCH safety-gated conformance subset ({@code /conf/update}; OGC
 * 23-002 Annex A).
 *
 * <p>
 * Implements the first <strong>REQ-ETS-PART2-008</strong> increment: exact Update
 * declaration, visible Create/Replace/Delete and OGC API Features Part 4 Update
 * prerequisites, Clause 15 resource condition gates, read-only OPTIONS PATCH readiness,
 * unavailable-endpoint honesty, and public-IUT PATCH hard-denial. Positive PATCH and
 * schema-rejection lifecycles remain deferred until a dedicated mutable-IUT fixture
 * exists.
 * </p>
 */
public class Part2UpdateTests {

	static final String GROUP = "part2update";

	static final String CONF_UPDATE = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/update";

	static final String CONF_CREATE_REPLACE_DELETE = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/create-replace-delete";

	static final String CONF_FEATURES4_UPDATE = "http://www.opengis.net/spec/ogcapi-features-4/1.0/conf/update";

	static final String CONF_DATASTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/datastream";

	static final String CONF_CONTROLSTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/controlstream";

	static final String CONF_FEASIBILITY = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/feasibility";

	static final String CONF_SYSTEM_EVENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/system-event";

	static final String REQ_UPDATE = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/update";

	static final String REQ_DATASTREAM = REQ_UPDATE + "/datastream";

	static final String REQ_DATASTREAM_UPDATE_SCHEMA = REQ_UPDATE + "/datastream-update-schema";

	static final String REQ_OBSERVATION = REQ_UPDATE + "/observation";

	static final String REQ_OBSERVATION_SCHEMA = REQ_UPDATE + "/observation-schema";

	static final String REQ_CONTROLSTREAM = REQ_UPDATE + "/controlstream";

	static final String REQ_CONTROLSTREAM_UPDATE_SCHEMA = REQ_UPDATE + "/controlstream-update-schema";

	static final String REQ_COMMAND = REQ_UPDATE + "/command";

	static final String REQ_COMMAND_SCHEMA = REQ_UPDATE + "/command-schema";

	static final String REQ_COMMAND_STATUS = REQ_UPDATE + "/command-status";

	static final String REQ_COMMAND_RESULT = REQ_UPDATE + "/command-result";

	static final String REQ_FEASIBILITY = REQ_UPDATE + "/feasibility";

	static final String REQ_FEASIBILITY_STATUS = REQ_UPDATE + "/feasibility-status";

	static final String REQ_FEASIBILITY_RESULT = REQ_UPDATE + "/feasibility-result";

	static final String REQ_SYSTEM_EVENT = REQ_UPDATE + "/system-event";

	private static final String ENABLED_VALUE = "true";

	private static final String DEDICATED_MUTABLE_IUT_POLICY = "dedicated-mutable-iut";

	private URI iutUri;

	private URI baseUri;

	private Response conformanceResponse;

	private Map<String, Object> conformanceBody;

	private String mutationTestsEnabled;

	private String mutationIutPolicy;

	/**
	 * Fetches shared read-only inputs once. This class must not issue PATCH, POST, PUT,
	 * or DELETE unless a future lifecycle method has passed the explicit mutation gate.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchPart2UpdateInputs(ITestContext testContext) {
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		this.iutUri = (URI) iutAttr;
		String iutString = this.iutUri.toString();
		this.baseUri = URI.create(iutString.endsWith("/") ? iutString : iutString + "/");

		this.mutationTestsEnabled = suiteString(testContext, SuiteAttribute.MUTATION_TESTS_ENABLED);
		this.mutationIutPolicy = suiteString(testContext, SuiteAttribute.MUTATION_IUT_POLICY);

		this.conformanceResponse = given().accept("application/json")
			.when()
			.get(this.baseUri.resolve("conformance"))
			.andReturn();
		this.conformanceBody = parseBody(this.conformanceResponse);
	}

	/**
	 * SCENARIO-ETS-PART2-008-UPDATE-CONFORMANCE-DECLARED-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_UPDATE
			+ ": /conformance declares Part 2 /conf/update before Update assertions run (REQ-ETS-PART2-008, SCENARIO-ETS-PART2-008-UPDATE-CONFORMANCE-DECLARED-001)",
			groups = GROUP)
	public void part2UpdateConformanceDeclared() {
		ETSAssert.assertStatus(this.conformanceResponse, 200, REQ_UPDATE);
		if (this.conformanceBody == null) {
			ETSAssert.failWithUri(REQ_UPDATE, "/conformance body did not parse as JSON. Content-Type was: "
					+ this.conformanceResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.conformanceBody, "conformsTo", List.class, REQ_UPDATE);
		if (!declaresConformance(this.conformanceBody, CONF_UPDATE)) {
			throw new SkipException(CONF_UPDATE
					+ " - IUT does not declare the CS API Part 2 Update conformance class. Undeclared PATCH behavior is not conformance PASS evidence.");
		}
	}

	/**
	 * SCENARIO-ETS-PART2-008-CRD-FEATURES4-PREREQUISITES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_UPDATE
			+ ": Part 2 Create/Replace/Delete and OGC API Features Part 4 Update prerequisites are visible before full class closure (REQ-ETS-PART2-008, SCENARIO-ETS-PART2-008-CRD-FEATURES4-PREREQUISITES-001)",
			groups = GROUP)
	public void part2UpdatePrerequisitesVisibleForFullClosure() {
		skipIfUpdateUndeclared();
		List<String> missing = new ArrayList<>();
		if (!declaresConformance(this.conformanceBody, CONF_CREATE_REPLACE_DELETE)) {
			missing.add(CONF_CREATE_REPLACE_DELETE);
		}
		if (!declaresConformance(this.conformanceBody, CONF_FEATURES4_UPDATE)) {
			missing.add(CONF_FEATURES4_UPDATE);
		}
		if (!missing.isEmpty()) {
			throw new SkipException(
					REQ_UPDATE + " - full Part 2 /conf/update closure is prerequisite-incomplete; missing: "
							+ String.join(", ", missing));
		}
		Reporter.log("IUT declares Part 2 /conf/update and its Create/Replace/Delete + Features Part 4 prerequisites.",
				true);
	}

	/**
	 * SCENARIO-ETS-PART2-008-RESOURCE-CONDITION-GATES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_UPDATE
			+ ": Clause 15 condition classes gate R79-R92 before any Update PASS evidence (REQ-ETS-PART2-008, SCENARIO-ETS-PART2-008-RESOURCE-CONDITION-GATES-001)",
			groups = GROUP)
	public void clause15ResourceConditionGatesAreVisible() {
		skipIfUpdateUndeclared();
		List<String> missing = missingConditionClasses(this.conformanceBody);
		if (!missing.isEmpty()) {
			throw new SkipException(REQ_UPDATE
					+ " - Clause 15 condition classes are missing, so affected R79-R92 assertions are prerequisite-incomplete: "
					+ String.join("; ", missing));
		}
		Reporter.log("Clause 15 Update condition classes are declared for R79-R92.", true);
	}

	/**
	 * SCENARIO-ETS-PART2-008-PATCH-MUTATION-SAFETY-GATE-001.
	 * SCENARIO-ETS-PART2-008-SMOKE-NO-PUBLIC-PATCH-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_UPDATE
			+ ": PATCH lifecycle checks are blocked unless mutation-tests-enabled=true and mutation-iut-policy=dedicated-mutable-iut, with public GeoRobotix hard-denied (REQ-ETS-PART2-008, SCENARIO-ETS-PART2-008-PATCH-MUTATION-SAFETY-GATE-001, SCENARIO-ETS-PART2-008-SMOKE-NO-PUBLIC-PATCH-001)",
			groups = GROUP)
	public void part2UpdateMutationSafetyGate() {
		skipIfUpdateUndeclared();
		ensureMutationEnabledOrSkip(REQ_UPDATE);
		Reporter.log(
				"Mutation opt-in accepted for a non-public dedicated mutable IUT; no PATCH lifecycle mutation was issued by this safety-gate check.",
				true);
	}

	/**
	 * SCENARIO-ETS-PART2-008-OPTIONS-PATCH-READINESS-001.
	 * SCENARIO-ETS-PART2-008-DATASTREAM-OBSERVATION-PATCH-OPTIN-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_DATASTREAM + ", " + REQ_OBSERVATION
			+ ": DataStream and Observation OPTIONS PATCH readiness is checked only after /conf/update and /conf/datastream gates (REQ-ETS-PART2-008, SCENARIO-ETS-PART2-008-OPTIONS-PATCH-READINESS-001, SCENARIO-ETS-PART2-008-DATASTREAM-OBSERVATION-PATCH-OPTIN-001)",
			groups = GROUP)
	public void datastreamObservationPatchReadinessIsConditionGated() {
		skipIfUpdateUndeclared();
		skipIfConditionClassUndeclared(CONF_DATASTREAM,
				"Requirements 79-82 require the Datastreams & Observations class before DataStream/Observation Update PASS evidence.");
		Map<String, Object> datastream = firstResource("datastreams", REQ_DATASTREAM);
		String datastreamId = resourceId(datastream, REQ_DATASTREAM, "selected DataStream");
		assertOptionsPatchReadiness("datastreams/" + datastreamId, REQ_DATASTREAM);

		Map<String, Object> observation = firstResource("observations", REQ_OBSERVATION);
		String observationId = resourceId(observation, REQ_OBSERVATION, "selected Observation");
		assertOptionsPatchReadiness("observations/" + observationId, REQ_OBSERVATION);
		Reporter.log("PATCH readiness recorded for DataStream/Observation endpoints; no PATCH was issued.", true);
	}

	/**
	 * SCENARIO-ETS-PART2-008-OPTIONS-PATCH-READINESS-001.
	 * SCENARIO-ETS-PART2-008-CONTROLSTREAM-COMMAND-PATCH-OPTIN-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CONTROLSTREAM + ", " + REQ_COMMAND + ", " + REQ_COMMAND_STATUS + ", and "
			+ REQ_COMMAND_RESULT
			+ ": ControlStream/Command OPTIONS PATCH readiness is checked only after /conf/update and /conf/controlstream gates (REQ-ETS-PART2-008, SCENARIO-ETS-PART2-008-OPTIONS-PATCH-READINESS-001, SCENARIO-ETS-PART2-008-CONTROLSTREAM-COMMAND-PATCH-OPTIN-001)",
			groups = GROUP)
	public void controlStreamCommandPatchReadinessIsConditionGated() {
		skipIfUpdateUndeclared();
		skipIfConditionClassUndeclared(CONF_CONTROLSTREAM,
				"Requirements 83-88 require the Control Streams & Commands class before ControlStream/Command Update PASS evidence.");
		Map<String, Object> controlStream = firstResource("controlstreams", REQ_CONTROLSTREAM);
		String controlStreamId = resourceId(controlStream, REQ_CONTROLSTREAM, "selected ControlStream");
		assertOptionsPatchReadiness("controlstreams/" + controlStreamId, REQ_CONTROLSTREAM);

		Map<String, Object> command = firstResource("commands", REQ_COMMAND);
		String commandId = resourceId(command, REQ_COMMAND, "selected Command");
		assertOptionsPatchReadiness("commands/" + commandId, REQ_COMMAND);
		Reporter.log("PATCH readiness recorded for ControlStream/Command endpoints; no PATCH was issued.", true);
	}

	/**
	 * SCENARIO-ETS-PART2-008-OPTIONS-PATCH-READINESS-001.
	 * SCENARIO-ETS-PART2-008-FEASIBILITY-SYSTEMEVENT-PATCH-OPTIN-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_FEASIBILITY + ", " + REQ_FEASIBILITY_STATUS + ", and "
			+ REQ_FEASIBILITY_RESULT
			+ ": Feasibility OPTIONS PATCH readiness is checked only after /conf/update and /conf/feasibility gates (REQ-ETS-PART2-008, SCENARIO-ETS-PART2-008-OPTIONS-PATCH-READINESS-001, SCENARIO-ETS-PART2-008-FEASIBILITY-SYSTEMEVENT-PATCH-OPTIN-001)",
			groups = GROUP)
	public void feasibilityPatchReadinessIsConditionGated() {
		skipIfUpdateUndeclared();
		skipIfConditionClassUndeclared(CONF_FEASIBILITY,
				"Requirements 89-91 require the Command Feasibility class before Feasibility Update PASS evidence.");

		Map<String, Object> feasibility = firstResource("feasibility", REQ_FEASIBILITY);
		String feasibilityId = resourceId(feasibility, REQ_FEASIBILITY, "selected Feasibility resource");
		assertOptionsPatchReadiness("feasibility/" + feasibilityId, REQ_FEASIBILITY);
		Reporter.log("PATCH readiness recorded for Feasibility endpoint; no PATCH was issued.", true);
	}

	/**
	 * SCENARIO-ETS-PART2-008-OPTIONS-PATCH-READINESS-001.
	 * SCENARIO-ETS-PART2-008-FEASIBILITY-SYSTEMEVENT-PATCH-OPTIN-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_SYSTEM_EVENT
			+ ": SystemEvent OPTIONS PATCH readiness is checked only after /conf/update and /conf/system-event gates (REQ-ETS-PART2-008, SCENARIO-ETS-PART2-008-OPTIONS-PATCH-READINESS-001, SCENARIO-ETS-PART2-008-FEASIBILITY-SYSTEMEVENT-PATCH-OPTIN-001)",
			groups = GROUP)
	public void systemEventPatchReadinessIsConditionGated() {
		skipIfUpdateUndeclared();
		skipIfConditionClassUndeclared(CONF_SYSTEM_EVENT,
				"Requirement 92 requires the System Events class before SystemEvent Update PASS evidence.");

		Map<String, Object> systemEvent = firstResource("systemEvents", REQ_SYSTEM_EVENT);
		String systemEventId = resourceId(systemEvent, REQ_SYSTEM_EVENT, "selected SystemEvent");
		assertOptionsPatchReadiness("systemEvents/" + systemEventId, REQ_SYSTEM_EVENT);
		Reporter.log("PATCH readiness recorded for SystemEvent endpoint; no PATCH was issued.", true);
	}

	/**
	 * SCENARIO-ETS-PART2-008-UNAVAILABLE-ENDPOINT-HONESTY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COMMAND + ", " + REQ_FEASIBILITY + ", and " + REQ_SYSTEM_EVENT
			+ ": unavailable, HTTP 500, HTTP 400, or streaming-only endpoints are SKIP evidence, not Update lifecycle PASS evidence (REQ-ETS-PART2-008, SCENARIO-ETS-PART2-008-UNAVAILABLE-ENDPOINT-HONESTY-001)",
			groups = GROUP)
	public void unavailableEndpointsDoNotBecomeUpdateLifecycleEvidence() {
		skipIfUpdateUndeclared();
		skipIfConditionClassUndeclared(CONF_CONTROLSTREAM,
				"Requirements 85-88 require /conf/controlstream before Command endpoint honesty can support Part 2 Update evidence.");
		skipIfConditionClassUndeclared(CONF_FEASIBILITY,
				"Requirements 89-91 require /conf/feasibility before Feasibility endpoint honesty can support Part 2 Update evidence.");
		skipIfConditionClassUndeclared(CONF_SYSTEM_EVENT,
				"Requirement 92 requires /conf/system-event before SystemEvent endpoint honesty can support Part 2 Update evidence.");
		List<String> unavailable = new ArrayList<>();
		collectUnavailableEndpoint("commands", REQ_COMMAND, unavailable);
		collectUnavailableEndpoint("feasibility", REQ_FEASIBILITY, unavailable);
		collectUnavailableEndpoint("systemEvents", REQ_SYSTEM_EVENT, unavailable);
		String systemId = firstResourceIdOrNull("systems");
		if (systemId == null) {
			unavailable.add("GET /systems?limit=1 did not expose a System id for /systems/{id}/events probing");
		}
		else {
			collectUnavailableEndpoint("systems/" + systemId + "/events", REQ_SYSTEM_EVENT, unavailable);
		}
		if (!unavailable.isEmpty()) {
			throw new SkipException(
					REQ_UPDATE + " - unavailable or non-JSON endpoints are not Part 2 Update lifecycle PASS evidence: "
							+ String.join("; ", unavailable));
		}
		Reporter.log(
				"Command, Feasibility, and SystemEvent endpoints exposed HTTP 200 JSON collection evidence before any Update lifecycle assertion.",
				true);
	}

	/**
	 * SCENARIO-ETS-PART2-008-SCHEMA-REJECTION-HONESTY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_DATASTREAM_UPDATE_SCHEMA + ", " + REQ_OBSERVATION_SCHEMA + ", "
			+ REQ_CONTROLSTREAM_UPDATE_SCHEMA + ", and " + REQ_COMMAND_SCHEMA
			+ ": schema-rejection PATCH checks require safe mutation opt-in and concrete parent schema evidence (REQ-ETS-PART2-008, SCENARIO-ETS-PART2-008-SCHEMA-REJECTION-HONESTY-001)",
			groups = GROUP)
	public void schemaRejectionPatchChecksRequireSafeEvidence() {
		skipIfUpdateUndeclared();
		skipIfConditionClassUndeclared(CONF_DATASTREAM,
				"Requirements 80 and 82 require /conf/datastream before DataStream/Observation update-schema rejection evidence.");
		skipIfConditionClassUndeclared(CONF_CONTROLSTREAM,
				"Requirements 84 and 86 require /conf/controlstream before ControlStream/Command update-schema rejection evidence.");
		ensureMutationEnabledOrSkip(REQ_DATASTREAM_UPDATE_SCHEMA);
		throw new SkipException(REQ_DATASTREAM_UPDATE_SCHEMA
				+ " - schema-rejection PATCH checks are deferred until a dedicated mutable IUT, endpoint PATCH readiness, and concrete parent schema evidence are available. No PATCH request was issued.");
	}

	/**
	 * SCENARIO-ETS-PART2-008-DATASTREAM-OBSERVATION-PATCH-OPTIN-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_DATASTREAM + ", " + REQ_OBSERVATION
			+ ": DataStream/Observation positive PATCH checks require a dedicated mutable IUT and changed-field GET proof (REQ-ETS-PART2-008, SCENARIO-ETS-PART2-008-DATASTREAM-OBSERVATION-PATCH-OPTIN-001)",
			groups = GROUP)
	public void datastreamObservationPatchLifecycleRequiresDedicatedMutableIut() {
		skipIfUpdateUndeclared();
		skipIfConditionClassUndeclared(CONF_DATASTREAM,
				"Requirements 79-82 require /conf/datastream before DataStream/Observation PATCH lifecycle checks.");
		ensureMutationEnabledOrSkip(REQ_DATASTREAM);
		throw new SkipException(REQ_DATASTREAM
				+ " - positive DataStream/Observation PATCH lifecycle coverage is deferred until dedicated mutable-IUT fixtures, endpoint PATCH readiness, changed-field GET proof, and cleanup are implemented. No PATCH request was issued.");
	}

	/**
	 * SCENARIO-ETS-PART2-008-CONTROLSTREAM-COMMAND-PATCH-OPTIN-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CONTROLSTREAM + ", " + REQ_COMMAND + ", " + REQ_COMMAND_STATUS + ", and "
			+ REQ_COMMAND_RESULT
			+ ": ControlStream/Command positive PATCH checks require a dedicated mutable IUT and changed-field GET proof (REQ-ETS-PART2-008, SCENARIO-ETS-PART2-008-CONTROLSTREAM-COMMAND-PATCH-OPTIN-001)",
			groups = GROUP)
	public void controlStreamCommandPatchLifecycleRequiresDedicatedMutableIut() {
		skipIfUpdateUndeclared();
		skipIfConditionClassUndeclared(CONF_CONTROLSTREAM,
				"Requirements 83-88 require /conf/controlstream before ControlStream/Command PATCH lifecycle checks.");
		ensureMutationEnabledOrSkip(REQ_CONTROLSTREAM);
		throw new SkipException(REQ_CONTROLSTREAM
				+ " - positive ControlStream/Command PATCH lifecycle coverage is deferred until dedicated mutable-IUT fixtures, endpoint PATCH readiness, changed-field GET proof, and cleanup are implemented. No PATCH request was issued.");
	}

	/**
	 * SCENARIO-ETS-PART2-008-FEASIBILITY-SYSTEMEVENT-PATCH-OPTIN-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_FEASIBILITY + ", " + REQ_FEASIBILITY_STATUS + ", and "
			+ REQ_FEASIBILITY_RESULT
			+ ": Feasibility positive PATCH checks require a dedicated mutable IUT and changed-field GET proof (REQ-ETS-PART2-008, SCENARIO-ETS-PART2-008-FEASIBILITY-SYSTEMEVENT-PATCH-OPTIN-001)",
			groups = GROUP)
	public void feasibilityPatchLifecycleRequiresDedicatedMutableIut() {
		skipIfUpdateUndeclared();
		skipIfConditionClassUndeclared(CONF_FEASIBILITY,
				"Requirements 89-91 require /conf/feasibility before Feasibility PATCH lifecycle checks.");
		ensureMutationEnabledOrSkip(REQ_FEASIBILITY);
		throw new SkipException(REQ_FEASIBILITY
				+ " - positive Feasibility PATCH lifecycle coverage is deferred until dedicated mutable-IUT fixtures, endpoint PATCH readiness, changed-field GET proof, and cleanup are implemented. No PATCH request was issued.");
	}

	/**
	 * SCENARIO-ETS-PART2-008-FEASIBILITY-SYSTEMEVENT-PATCH-OPTIN-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_SYSTEM_EVENT
			+ ": SystemEvent positive PATCH checks require a dedicated mutable IUT and changed-field GET proof (REQ-ETS-PART2-008, SCENARIO-ETS-PART2-008-FEASIBILITY-SYSTEMEVENT-PATCH-OPTIN-001)",
			groups = GROUP)
	public void systemEventPatchLifecycleRequiresDedicatedMutableIut() {
		skipIfUpdateUndeclared();
		skipIfConditionClassUndeclared(CONF_SYSTEM_EVENT,
				"Requirement 92 requires /conf/system-event before SystemEvent PATCH lifecycle checks.");
		ensureMutationEnabledOrSkip(REQ_SYSTEM_EVENT);
		throw new SkipException(REQ_SYSTEM_EVENT
				+ " - positive SystemEvent PATCH lifecycle coverage is deferred until dedicated mutable-IUT fixtures, endpoint PATCH readiness, changed-field GET proof, and cleanup are implemented. No PATCH request was issued.");
	}

	static boolean declaresConformance(Map<String, Object> body, String conformanceUri) {
		return Part2ApiCommonTests.declaresConformance(body, conformanceUri);
	}

	static boolean isPublicGeoRobotixIut(URI iutUri) {
		if (iutUri == null) {
			return false;
		}
		String host = iutUri.getHost();
		String uri = stripTrailingSlash(iutUri.toString());
		return "api.georobotix.io".equalsIgnoreCase(host)
				|| "https://api.georobotix.io/ogc/t18/api".equalsIgnoreCase(uri);
	}

	static String mutationGateSkipReason(URI iutUri, String mutationTestsEnabled, String mutationIutPolicy,
			String requirementUri) {
		if (isPublicGeoRobotixIut(iutUri)) {
			return requirementUri
					+ " - known shared public GeoRobotix IUT is hard-denied for Part 2 Update PATCH tests. "
					+ "No PATCH/POST/PUT/DELETE request was issued.";
		}
		if (!ENABLED_VALUE.equals(mutationTestsEnabled) || !DEDICATED_MUTABLE_IUT_POLICY.equals(mutationIutPolicy)) {
			return requirementUri + " - IUT-bound PATCH lifecycle tests are disabled. Set "
					+ TestRunArg.MUTATION_TESTS_ENABLED + "=true and " + TestRunArg.MUTATION_IUT_POLICY
					+ "=dedicated-mutable-iut to permit PATCH lifecycle checks. No PATCH/POST/PUT/DELETE request was issued.";
		}
		return null;
	}

	static boolean allowHeaderContains(String allow, String method) {
		if (allow == null || method == null) {
			return false;
		}
		return Arrays.stream(allow.split(","))
			.map(String::trim)
			.map(value -> value.toUpperCase(Locale.ROOT))
			.anyMatch(method.toUpperCase(Locale.ROOT)::equals);
	}

	static boolean hasItemsOnlyCollectionShape(Map<String, Object> body) {
		return body != null && body.get("items") instanceof List;
	}

	static List<String> missingConditionClasses(Map<String, Object> conformanceBody) {
		List<String> missing = new ArrayList<>();
		addMissingCondition(missing, conformanceBody, CONF_DATASTREAM, "R79-R82 DataStream/Observation Update");
		addMissingCondition(missing, conformanceBody, CONF_CONTROLSTREAM, "R83-R88 ControlStream/Command Update");
		addMissingCondition(missing, conformanceBody, CONF_FEASIBILITY, "R89-R91 Feasibility Update");
		addMissingCondition(missing, conformanceBody, CONF_SYSTEM_EVENT, "R92 SystemEvent Update");
		return missing;
	}

	private static void addMissingCondition(List<String> missing, Map<String, Object> conformanceBody,
			String conformanceUri, String label) {
		if (!declaresConformance(conformanceBody, conformanceUri)) {
			missing.add(missingConditionMessage(conformanceUri, label));
		}
	}

	static String missingConditionMessage(String conformanceUri, String label) {
		return label + " requires " + conformanceUri;
	}

	private void skipIfUpdateUndeclared() {
		if (!declaresConformance(this.conformanceBody, CONF_UPDATE)) {
			throw new SkipException(CONF_UPDATE
					+ " - IUT does not declare the CS API Part 2 Update conformance class in /conformance.");
		}
	}

	private void skipIfConditionClassUndeclared(String conformanceUri, String reason) {
		if (!declaresConformance(this.conformanceBody, conformanceUri)) {
			throw new SkipException(conformanceUri + " - " + reason);
		}
	}

	private void ensureMutationEnabledOrSkip(String requirementUri) {
		String reason = mutationGateSkipReason(this.iutUri, this.mutationTestsEnabled, this.mutationIutPolicy,
				requirementUri);
		if (reason != null) {
			throw new SkipException(reason);
		}
	}

	private void assertOptionsPatchReadiness(String path, String requirementUri) {
		Response response = given().accept("*/*").when().options(this.baseUri.resolve(path)).andReturn();
		int statusCode = response.getStatusCode();
		if (statusCode != 200 && statusCode != 204) {
			throw new SkipException(requirementUri + " - OPTIONS /" + path + " returned HTTP " + statusCode
					+ "; endpoint PATCH readiness is inconclusive and no PATCH was issued.");
		}
		String allow = response.getHeader("Allow");
		if (!allowHeaderContains(allow, "PATCH")) {
			ETSAssert.failWithUri(requirementUri,
					"OPTIONS /" + path + " succeeded but did not advertise PATCH readiness; Allow was: " + allow
							+ ". Lifecycle PATCH was not issued.");
		}
	}

	private void collectUnavailableEndpoint(String path, String requirementUri, List<String> unavailable) {
		Response response = given().accept("application/json")
			.queryParam("limit", 1)
			.when()
			.get(this.baseUri.resolve(path))
			.andReturn();
		if (response.getStatusCode() != 200) {
			unavailable.add("GET /" + path + "?limit=1 returned HTTP " + response.getStatusCode());
			return;
		}
		Map<String, Object> body = parseBody(response);
		if (!hasItemsOnlyCollectionShape(body)) {
			unavailable
				.add("GET /" + path + "?limit=1 did not expose JSON items[] collection shape for " + requirementUri);
		}
	}

	private Map<String, Object> firstResource(String collectionPath, String requirementUri) {
		Response response = given().accept("application/json")
			.queryParam("limit", 1)
			.when()
			.get(this.baseUri.resolve(collectionPath))
			.andReturn();
		if (response.getStatusCode() != 200) {
			throw new SkipException(requirementUri + " - /" + collectionPath + "?limit=1 returned HTTP "
					+ response.getStatusCode() + "; no resource is available for read-only PATCH readiness probing.");
		}
		Map<String, Object> body = parseBody(response);
		List<?> items = items(body);
		if (items.isEmpty() || !(items.get(0) instanceof Map)) {
			throw new SkipException(requirementUri + " - /" + collectionPath
					+ "?limit=1 did not expose a resource item for read-only PATCH readiness probing.");
		}
		@SuppressWarnings("unchecked")
		Map<String, Object> resource = (Map<String, Object>) items.get(0);
		return resource;
	}

	private String resourceId(Map<String, Object> resource, String requirementUri, String label) {
		String id = stringValue(resource.get("id"));
		if (id == null || id.isBlank()) {
			throw new SkipException(
					requirementUri + " - " + label + " did not expose an id for read-only PATCH readiness probing.");
		}
		return id;
	}

	private String firstResourceIdOrNull(String collectionPath) {
		Response response = given().accept("application/json")
			.queryParam("limit", 1)
			.when()
			.get(this.baseUri.resolve(collectionPath))
			.andReturn();
		if (response.getStatusCode() != 200) {
			return null;
		}
		Map<String, Object> body = parseBody(response);
		List<?> items = items(body);
		if (items.isEmpty() || !(items.get(0) instanceof Map)) {
			return null;
		}
		return stringValue(((Map<?, ?>) items.get(0)).get("id"));
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> parseBody(Response response) {
		try {
			return response.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private List<?> items(Map<String, Object> body) {
		if (body == null || !(body.get("items") instanceof List)) {
			return List.of();
		}
		return (List<?>) body.get("items");
	}

	private String suiteString(ITestContext testContext, SuiteAttribute attr) {
		Object value = testContext.getSuite().getAttribute(attr.getName());
		return value instanceof String ? (String) value : null;
	}

	private static String stripTrailingSlash(String value) {
		return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
	}

	private static String stringValue(Object value) {
		return value instanceof String ? (String) value : null;
	}

}
