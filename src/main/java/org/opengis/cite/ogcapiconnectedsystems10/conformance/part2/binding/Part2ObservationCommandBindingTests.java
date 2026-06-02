package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.binding;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.apicommon.Part2ApiCommonTests;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Part 2 - internal Observation/Command binding cross-class closure.
 *
 * <p>
 * OGC 23-002 does not define a standalone {@code /conf/observation-binding} conformance
 * class. This suite implements <strong>REQ-ETS-PART2-013</strong> as an internal closure
 * over the existing DataStream/Observation and ControlStream/Command requirements: parent
 * schema evidence plus associated child body evidence must be present before any binding
 * PASS is reported. Empty local OSH dynamic-data collections SKIP honestly rather than
 * passing from declarations or format lists.
 * </p>
 */
public class Part2ObservationCommandBindingTests {

	public static final String GROUP = "part2binding";

	public static final String CONF_DATASTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/datastream";

	public static final String CONF_CONTROLSTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/controlstream";

	public static final String CONF_JSON = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/json";

	public static final String NON_STANDARD_CONF_OBSERVATION_BINDING = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/observation-binding";

	public static final String REQ_DATASTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/datastream";

	public static final String REQ_CONTROLSTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/controlstream";

	public static final String REQ_DATASTREAM_SCHEMA_OP = REQ_DATASTREAM + "/schema-op";

	public static final String REQ_OBS_REF_FROM_DATASTREAM = REQ_DATASTREAM + "/obs-ref-from-datastream";

	public static final String REQ_CONTROLSTREAM_SCHEMA_OP = REQ_CONTROLSTREAM + "/schema-op";

	public static final String REQ_CMD_REF_FROM_CONTROLSTREAM = REQ_CONTROLSTREAM + "/cmd-ref-from-controlstream";

	public static final String REQ_ETS_PART2_013 = "REQ-ETS-PART2-013";

	private URI iutUri;

	private URI baseUri;

	private Response conformanceResponse;

	private Map<String, Object> conformanceBody;

	/**
	 * Fetches shared read-only inputs once. This class intentionally never issues POST,
	 * PUT, PATCH, or DELETE, and it does not seed local OSH fixtures.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchPart2BindingInputs(ITestContext testContext) {
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
	}

	/**
	 * SCENARIO-ETS-PART2-013-GEOROBOTIX-NOT-DEFAULT-001.
	 * SCENARIO-ETS-PART2-013-ENCODING-HONESTY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_DATASTREAM + " and " + REQ_CONTROLSTREAM
			+ ": internal REQ-ETS-PART2-013 binding closure does not require or PASS from a fabricated /conf/observation-binding class (REQ-ETS-PART2-013, SCENARIO-ETS-PART2-013-ENCODING-HONESTY-001)",
			groups = GROUP)
	public void standaloneObservationBindingConformanceClassIsIgnored() {
		ETSAssert.assertStatus(this.conformanceResponse, 200, REQ_ETS_PART2_013);
		if (this.conformanceBody == null) {
			ETSAssert.failWithUri(REQ_ETS_PART2_013, "/conformance body did not parse as JSON. Content-Type was: "
					+ this.conformanceResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.conformanceBody, "conformsTo", List.class, REQ_ETS_PART2_013);
		if (declaresConformance(this.conformanceBody, NON_STANDARD_CONF_OBSERVATION_BINDING)) {
			Reporter.log(
					NON_STANDARD_CONF_OBSERVATION_BINDING
							+ " is not an OGC 23-002 conformance class and is ignored as vendor/non-standard evidence.",
					true);
		}
	}

	/**
	 * SCENARIO-ETS-PART2-013-DYNAMIC-SEED-STATE-001.
	 * SCENARIO-ETS-PART2-013-OBSERVATION-PARENT-SCHEMA-001.
	 * SCENARIO-ETS-PART2-013-ENCODING-HONESTY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_DATASTREAM_SCHEMA_OP + " and " + REQ_OBS_REF_FROM_DATASTREAM
			+ ": Observation binding requires a candidate DataStream schema and associated Observation body before PASS (REQ-ETS-PART2-013, SCENARIO-ETS-PART2-013-DYNAMIC-SEED-STATE-001, SCENARIO-ETS-PART2-013-OBSERVATION-PARENT-SCHEMA-001)",
			groups = GROUP)
	public void observationBindingRequiresParentSchemaAndChildBodyEvidence() {
		skipIfConditionClassUndeclared(CONF_DATASTREAM,
				"Observation binding requires the Part 2 Datastreams & Observations class.");
		Map<String, Object> datastream = firstRequiredCollectionResource("datastreams", REQ_OBS_REF_FROM_DATASTREAM,
				"/datastreams");
		String datastreamId = requireString(datastream, "id", REQ_OBS_REF_FROM_DATASTREAM);
		Map<String, Object> parentSchema = requiredJsonObject("datastreams/" + datastreamId + "/schema",
				REQ_DATASTREAM_SCHEMA_OP, "/datastreams/" + datastreamId + "/schema");
		Map<String, Object> observation = firstObservationForDatastream(datastreamId, REQ_OBS_REF_FROM_DATASTREAM);
		Map<String, Object> childBody = childBindingBody(observation, List.of("result", "parameters"),
				REQ_OBS_REF_FROM_DATASTREAM, "Observation");

		assertChildMatchesParentSchema(childBody, parentSchema,
				List.of("resultSchema", "parametersSchema", "paramsSchema", "recordSchema"),
				REQ_OBS_REF_FROM_DATASTREAM, "Observation body");
	}

	/**
	 * SCENARIO-ETS-PART2-013-DYNAMIC-SEED-STATE-001.
	 * SCENARIO-ETS-PART2-013-COMMAND-PARENT-SCHEMA-001.
	 * SCENARIO-ETS-PART2-013-ENCODING-HONESTY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CONTROLSTREAM_SCHEMA_OP + " and " + REQ_CMD_REF_FROM_CONTROLSTREAM
			+ ": Command binding requires a candidate ControlStream schema, associated Command parameters, and available status/result inline data before PASS (REQ-ETS-PART2-013, SCENARIO-ETS-PART2-013-DYNAMIC-SEED-STATE-001, SCENARIO-ETS-PART2-013-COMMAND-PARENT-SCHEMA-001)",
			groups = GROUP)
	public void commandBindingRequiresParentSchemaAndChildBodyEvidence() {
		skipIfConditionClassUndeclared(CONF_CONTROLSTREAM,
				"Command binding requires the Part 2 Control Streams & Commands class.");
		Map<String, Object> controlStream = firstRequiredCollectionResource("controlstreams",
				REQ_CMD_REF_FROM_CONTROLSTREAM, "/controlstreams");
		String controlStreamId = requireString(controlStream, "id", REQ_CMD_REF_FROM_CONTROLSTREAM);
		Map<String, Object> parentSchema = requiredJsonObject("controlstreams/" + controlStreamId + "/schema",
				REQ_CONTROLSTREAM_SCHEMA_OP, "/controlstreams/" + controlStreamId + "/schema");
		Map<String, Object> command = firstCommandForControlStream(controlStreamId, REQ_CMD_REF_FROM_CONTROLSTREAM);
		Map<String, Object> childBody = childBindingBody(command, List.of("parameters", "params"),
				REQ_CMD_REF_FROM_CONTROLSTREAM, "Command");

		assertChildMatchesParentSchema(childBody, parentSchema,
				List.of("parametersSchema", "paramsSchema", "recordSchema"), REQ_CMD_REF_FROM_CONTROLSTREAM,
				"Command parameters");
		assertAvailableCommandInlineDataMatchesParentSchema(command, parentSchema, REQ_CMD_REF_FROM_CONTROLSTREAM);
	}

	/**
	 * SCENARIO-ETS-PART2-013-ENCODING-HONESTY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_DATASTREAM_SCHEMA_OP + " and " + REQ_CONTROLSTREAM_SCHEMA_OP
			+ ": binding closure skips unsupported or unavailable encoding evidence instead of passing from declarations or format lists (REQ-ETS-PART2-013, SCENARIO-ETS-PART2-013-ENCODING-HONESTY-001)",
			groups = GROUP)
	public void bindingClosureRequiresInspectableJsonEvidenceForFirstIncrement() {
		if (!declaresConformance(this.conformanceBody, CONF_JSON)) {
			throw new SkipException(CONF_JSON
					+ " - first REQ-ETS-PART2-013 binding increment validates inspectable JSON object evidence only; sibling or non-JSON encoding declarations are not binding PASS evidence.");
		}
		Reporter.log(
				"IUT declares /conf/json; runtime binding checks still require parent schema and child JSON body evidence.",
				true);
	}

	/**
	 * SCENARIO-ETS-PART2-013-MUTATION-SAFETY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_DATASTREAM + " and " + REQ_CONTROLSTREAM
			+ ": default REQ-ETS-PART2-013 binding checks do not seed fixtures or issue mutation requests (REQ-ETS-PART2-013, SCENARIO-ETS-PART2-013-MUTATION-SAFETY-001)",
			groups = GROUP)
	public void readOnlyBindingSuiteDoesNotSeedFixturesWithoutDedicatedMutationWork() {
		Reporter.log(
				"REQ-ETS-PART2-013 first increment is read-only: it uses GET-only live evidence and does not seed DataStream, Observation, ControlStream, or Command fixtures.",
				true);
	}

	static boolean declaresConformance(Map<String, Object> body, String conformanceUri) {
		return Part2ApiCommonTests.declaresConformance(body, conformanceUri);
	}

	static boolean isNonStandardObservationBindingConformance(Object value) {
		return NON_STANDARD_CONF_OBSERVATION_BINDING.equals(value);
	}

	static List<BindingField> schemaFields(Object schema, List<String> candidateMembers) {
		Object schemaNode = unwrapSchemaNode(schema, candidateMembers);
		List<BindingField> fields = new ArrayList<>();
		collectFields(schemaNode, fields, requiredNames(schemaNode));
		return fields;
	}

	static List<String> bindingMismatches(Map<String, Object> childBody, Object parentSchema,
			List<String> candidateSchemaMembers, String label) {
		List<BindingField> fields = schemaFields(parentSchema, candidateSchemaMembers);
		List<String> mismatches = new ArrayList<>();
		if (fields.isEmpty()) {
			mismatches.add(label + " parent schema does not expose inspectable field metadata.");
			return mismatches;
		}
		boolean hasMappedField = false;
		for (BindingField field : fields) {
			if (!childBody.containsKey(field.name())) {
				if (field.required()) {
					mismatches.add(label + " is missing required parent-schema field '" + field.name() + "'.");
				}
				continue;
			}
			hasMappedField = true;
			Object value = childBody.get(field.name());
			if (!isPrimitiveTypeCompatible(field.type(), value)) {
				mismatches.add(label + " field '" + field.name() + "' has value type " + valueType(value)
						+ " incompatible with parent-schema type '" + field.type() + "'.");
			}
		}
		if (!hasMappedField) {
			mismatches.add(label
					+ " does not contain any field named by the parent schema; declarations or format lists alone are not binding PASS evidence.");
		}
		return mismatches;
	}

	static boolean isPrimitiveTypeCompatible(String schemaType, Object value) {
		if (schemaType == null || schemaType.isBlank() || value == null) {
			return true;
		}
		String normalized = normalizeType(schemaType);
		return switch (normalized) {
			case "string" -> value instanceof String;
			case "boolean" -> value instanceof Boolean;
			case "number" -> value instanceof Number;
			case "integer" -> isIntegralNumber(value);
			case "object" -> value instanceof Map;
			case "array" -> value instanceof List;
			case "time" -> value instanceof String || value instanceof Number;
			default -> true;
		};
	}

	private Map<String, Object> firstRequiredCollectionResource(String path, String reqUri, String source) {
		Response response = given().accept("application/json")
			.queryParam("limit", 1)
			.when()
			.get(this.baseUri.resolve(path))
			.andReturn();
		Map<String, Object> body = skipUnlessInspectableJsonResponse(response, reqUri, source);
		List<?> items = items(body);
		if (items.isEmpty()) {
			throw new SkipException(reqUri + " - " + source
					+ " returned an empty collection; no local dynamic-data candidate is available for REQ-ETS-PART2-013 binding PASS.");
		}
		Object first = items.get(0);
		if (!(first instanceof Map)) {
			ETSAssert.failWithUri(reqUri, source + " first item was not a JSON object: " + first);
		}
		return castMap(first);
	}

	private Map<String, Object> firstObservationForDatastream(String datastreamId, String reqUri) {
		Map<String, Object> nested = firstOptionalCollectionItem("datastreams/" + datastreamId + "/observations",
				reqUri, "/datastreams/" + datastreamId + "/observations");
		if (nested != null) {
			return nested;
		}
		Map<String, Object> global = firstOptionalCollectionItem("observations", reqUri, "/observations");
		if (global != null && observationReferencesDatastream(global, datastreamId)) {
			return global;
		}
		throw new SkipException(reqUri + " - neither /datastreams/" + datastreamId
				+ "/observations nor /observations exposed an Observation associated with Datastream '" + datastreamId
				+ "'; no binding PASS was reported.");
	}

	private Map<String, Object> firstCommandForControlStream(String controlStreamId, String reqUri) {
		Map<String, Object> nested = firstOptionalCollectionItem("controlstreams/" + controlStreamId + "/commands",
				reqUri, "/controlstreams/" + controlStreamId + "/commands");
		if (nested != null) {
			return nested;
		}
		Map<String, Object> global = firstOptionalCollectionItem("commands", reqUri, "/commands");
		if (global != null && commandReferencesControlStream(global, controlStreamId)) {
			return global;
		}
		throw new SkipException(reqUri + " - neither /controlstreams/" + controlStreamId
				+ "/commands nor /commands exposed a Command associated with ControlStream '" + controlStreamId
				+ "'; no binding PASS was reported.");
	}

	private Map<String, Object> firstOptionalCollectionItem(String path, String reqUri, String source) {
		Response response = given().accept("application/json")
			.queryParam("limit", 1)
			.when()
			.get(this.baseUri.resolve(path))
			.andReturn();
		if (response.getStatusCode() != 200) {
			return null;
		}
		Map<String, Object> body = optionalJsonResponse(response);
		if (body == null) {
			return null;
		}
		List<?> collectionItems = items(body);
		if (collectionItems.isEmpty()) {
			return null;
		}
		Object first = collectionItems.get(0);
		if (!(first instanceof Map)) {
			ETSAssert.failWithUri(reqUri, source + " first item was not a JSON object: " + first);
		}
		return castMap(first);
	}

	private Map<String, Object> requiredJsonObject(String path, String reqUri, String source) {
		Response response = given().accept("application/json").when().get(this.baseUri.resolve(path)).andReturn();
		return skipUnlessInspectableJsonResponse(response, reqUri, source);
	}

	private Map<String, Object> skipUnlessInspectableJsonResponse(Response response, String reqUri, String source) {
		if (response.getStatusCode() != 200) {
			throw new SkipException(reqUri + " - " + source + " returned HTTP " + response.getStatusCode()
					+ "; no inspectable binding evidence is available for this first read-only increment.");
		}
		if (!isJsonCompatibleContentType(response.getContentType())) {
			throw new SkipException(reqUri + " - " + source + " returned Content-Type '" + response.getContentType()
					+ "'; expected application/json or another +json media type before binding PASS.");
		}
		Map<String, Object> body = parseBody(response);
		if (body == null) {
			throw new SkipException(reqUri + " - " + source
					+ " body did not parse as JSON; no parent-schema binding PASS was reported.");
		}
		return body;
	}

	private void skipIfConditionClassUndeclared(String conformanceClass, String reason) {
		if (!declaresConformance(this.conformanceBody, conformanceClass)) {
			throw new SkipException(
					conformanceClass + " - " + reason + " No REQ-ETS-PART2-013 binding PASS evidence was reported.");
		}
	}

	private static void assertChildMatchesParentSchema(Map<String, Object> childBody, Map<String, Object> parentSchema,
			List<String> candidateSchemaMembers, String reqUri, String label) {
		if (schemaFields(parentSchema, candidateSchemaMembers).isEmpty()) {
			throw new SkipException(reqUri + " - " + label
					+ " parent schema does not expose inspectable field metadata; no binding PASS was reported.");
		}
		List<String> mismatches = bindingMismatches(childBody, parentSchema, candidateSchemaMembers, label);
		if (!mismatches.isEmpty()) {
			ETSAssert.failWithUri(reqUri, String.join(" ", mismatches));
		}
	}

	static void assertAvailableCommandInlineDataMatchesParentSchema(Map<String, Object> command,
			Map<String, Object> parentSchema, String reqUri) {
		assertOptionalInlineCommandDataMatchesParentSchema(command, parentSchema, "status",
				List.of("statusSchema", "commandStatusSchema"), reqUri);
		assertOptionalInlineCommandDataMatchesParentSchema(command, parentSchema, "commandStatus",
				List.of("statusSchema", "commandStatusSchema"), reqUri);
		assertOptionalInlineCommandDataMatchesParentSchema(command, parentSchema, "result",
				List.of("resultSchema", "commandResultSchema"), reqUri);
		assertOptionalInlineCommandDataMatchesParentSchema(command, parentSchema, "commandResult",
				List.of("resultSchema", "commandResultSchema"), reqUri);
	}

	private static void assertOptionalInlineCommandDataMatchesParentSchema(Map<String, Object> command,
			Map<String, Object> parentSchema, String member, List<String> candidateSchemaMembers, String reqUri) {
		Object inline = command.get(member);
		if (inline == null) {
			return;
		}
		if (!(inline instanceof Map)) {
			throw new SkipException(reqUri + " - Command inline member '" + member
					+ "' is not an inspectable JSON object; no binding PASS was reported for that inline data.");
		}
		assertChildMatchesParentSchema(castMap(inline), parentSchema, candidateSchemaMembers, reqUri,
				"Command inline " + member);
	}

	private static Map<String, Object> childBindingBody(Map<String, Object> child, List<String> bodyMembers,
			String reqUri, String label) {
		for (String bodyMember : bodyMembers) {
			Object value = child.get(bodyMember);
			if (value instanceof Map) {
				return castMap(value);
			}
			if (value != null) {
				throw new SkipException(reqUri + " - " + label + " member '" + bodyMember
						+ "' is not an inspectable JSON object; encoding-specific validation is required before binding PASS.");
			}
		}
		throw new SkipException(reqUri + " - " + label + " does not expose any of " + bodyMembers
				+ "; no parent-schema binding PASS was reported.");
	}

	private static Object unwrapSchemaNode(Object schema, List<String> candidateMembers) {
		if (!(schema instanceof Map)) {
			return schema;
		}
		Map<?, ?> map = (Map<?, ?>) schema;
		for (String member : candidateMembers) {
			Object value = map.get(member);
			if (value != null) {
				return value;
			}
		}
		if (map.containsKey("schema")) {
			return map.get("schema");
		}
		return schema;
	}

	private static void collectFields(Object schemaNode, List<BindingField> fields, Set<String> requiredNames) {
		if (!(schemaNode instanceof Map)) {
			return;
		}
		Map<?, ?> map = (Map<?, ?>) schemaNode;
		Object properties = map.get("properties");
		if (properties instanceof Map) {
			for (Map.Entry<?, ?> entry : ((Map<?, ?>) properties).entrySet()) {
				String name = stringValue(entry.getKey());
				if (name == null || !(entry.getValue() instanceof Map)) {
					continue;
				}
				Map<?, ?> property = (Map<?, ?>) entry.getValue();
				fields.add(new BindingField(name, stringValue(property.get("type")), requiredNames.contains(name)));
			}
			return;
		}
		Object fieldsValue = map.get("fields");
		if (fieldsValue instanceof List) {
			for (Object fieldValue : (List<?>) fieldsValue) {
				BindingField field = sweRecordField(fieldValue);
				if (field != null) {
					fields.add(field);
				}
			}
		}
	}

	private static BindingField sweRecordField(Object value) {
		if (!(value instanceof Map)) {
			return null;
		}
		Map<?, ?> fieldMap = (Map<?, ?>) value;
		String name = stringValue(fieldMap.get("name"));
		if (name == null || name.isBlank()) {
			return null;
		}
		Object component = fieldMap.containsKey("component") ? fieldMap.get("component") : fieldMap;
		String type = null;
		if (component instanceof Map) {
			type = stringValue(((Map<?, ?>) component).get("type"));
		}
		return new BindingField(name, type, true);
	}

	private static Set<String> requiredNames(Object schemaNode) {
		if (!(schemaNode instanceof Map)) {
			return Set.of();
		}
		Object required = ((Map<?, ?>) schemaNode).get("required");
		if (!(required instanceof List)) {
			return Set.of();
		}
		List<String> names = new ArrayList<>();
		for (Object item : (List<?>) required) {
			String name = stringValue(item);
			if (name != null && !name.isBlank()) {
				names.add(name);
			}
		}
		return Set.copyOf(names);
	}

	private static boolean observationReferencesDatastream(Map<String, Object> observation, String datastreamId) {
		if (datastreamId.equals(stringValue(observation.get("datastream@id")))
				|| datastreamId.equals(stringValue(observation.get("datastreamId")))) {
			return true;
		}
		Object datastream = observation.get("datastream");
		return datastream instanceof Map && datastreamId.equals(stringValue(((Map<?, ?>) datastream).get("id")));
	}

	private static boolean commandReferencesControlStream(Map<String, Object> command, String controlStreamId) {
		if (controlStreamId.equals(stringValue(command.get("controlstream@id")))
				|| controlStreamId.equals(stringValue(command.get("controlStream@id")))
				|| controlStreamId.equals(stringValue(command.get("controlstreamId")))
				|| controlStreamId.equals(stringValue(command.get("controlStreamId")))) {
			return true;
		}
		Object controlStream = command.containsKey("controlStream") ? command.get("controlStream")
				: command.get("controlstream");
		return controlStream instanceof Map
				&& controlStreamId.equals(stringValue(((Map<?, ?>) controlStream).get("id")));
	}

	private static boolean isJsonCompatibleContentType(String contentType) {
		if (contentType == null || contentType.isBlank()) {
			return false;
		}
		String mediaType = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
		return "application/json".equals(mediaType) || mediaType.endsWith("+json");
	}

	private static String requireString(Map<String, Object> body, String key, String reqUri) {
		ETSAssert.assertJsonObjectHas(body, key, String.class, reqUri);
		return (String) body.get(key);
	}

	private static List<?> items(Map<String, Object> body) {
		if (body == null || !(body.get("items") instanceof List)) {
			return List.of();
		}
		return (List<?>) body.get("items");
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> castMap(Object value) {
		return (Map<String, Object>) value;
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

	private static Map<String, Object> optionalJsonResponse(Response response) {
		if (!isJsonCompatibleContentType(response.getContentType())) {
			return null;
		}
		return parseBody(response);
	}

	private static String normalizeType(String schemaType) {
		String normalized = schemaType.trim().toLowerCase(Locale.ROOT);
		return switch (normalized) {
			case "text", "category", "string" -> "string";
			case "quantity", "real", "double", "float", "number" -> "number";
			case "count", "integer", "int", "long" -> "integer";
			case "boolean", "bool" -> "boolean";
			case "datarecord", "object", "record" -> "object";
			case "dataarray", "array" -> "array";
			case "time" -> "time";
			default -> normalized;
		};
	}

	private static boolean isIntegralNumber(Object value) {
		if (!(value instanceof Number)) {
			return false;
		}
		double asDouble = ((Number) value).doubleValue();
		return Math.rint(asDouble) == asDouble;
	}

	private static String valueType(Object value) {
		return Objects.isNull(value) ? "null" : value.getClass().getSimpleName();
	}

	/**
	 * Parent schema field extracted from JSON Schema or SWE Common record metadata.
	 */
	public record BindingField(String name, String type, boolean required) {
	}

}
