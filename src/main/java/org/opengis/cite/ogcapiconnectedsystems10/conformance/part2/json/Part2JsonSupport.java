package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.json;

import java.io.IOException;
import java.util.ArrayList;
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
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.Part2SchemaValidation;

import io.restassured.response.Response;

/**
 * Shared helpers for OGC 23-002 Part 2 JSON Encoding.
 */
final class Part2JsonSupport {

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

	private Part2JsonSupport() {
	}

	static List<String> missingConditionClasses(Map<String, Object> body) {
		List<String> missing = new ArrayList<>();
		if (!Part2JsonTests.declaresConformance(body, Part2JsonTests.CONF_DATASTREAM)) {
			missing.add(missingConditionMessage(Part2JsonTests.CONF_DATASTREAM,
					"Requirements 95-98 DataStream/Observation JSON"));
		}
		if (!Part2JsonTests.declaresConformance(body, Part2JsonTests.CONF_CONTROLSTREAM)) {
			missing.add(missingConditionMessage(Part2JsonTests.CONF_CONTROLSTREAM,
					"Requirements 99-105 ControlStream/Command JSON"));
		}
		if (!Part2JsonTests.declaresConformance(body, Part2JsonTests.CONF_SYSTEM_EVENT)) {
			missing.add(missingConditionMessage(Part2JsonTests.CONF_SYSTEM_EVENT, "Requirement 106 SystemEvent JSON"));
		}
		return missing;
	}

	static String missingConditionMessage(String conformanceClass, String requirementGroup) {
		return requirementGroup + " requires " + conformanceClass;
	}

	static boolean isApplicationJsonContentType(String contentType) {
		if (contentType == null || contentType.isBlank()) {
			return false;
		}
		return "application/json".equals(contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT));
	}

	static boolean isJsonDocumentContentType(String contentType) {
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
		try (var in = Part2JsonSupport.class.getResourceAsStream(SCHEMA_RESOURCE_PREFIX + schemaFile)) {
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

	static List<String> missingJsonWriteAdvertisements(Map<String, Object> apiDefinition,
			Map<String, List<String>> endpointTemplatesByLabel) {
		List<String> missing = new ArrayList<>();
		if (endpointTemplatesByLabel == null || endpointTemplatesByLabel.isEmpty()) {
			return missing;
		}
		Object paths = apiDefinition == null ? null : apiDefinition.get("paths");
		if (!(paths instanceof Map)) {
			for (String label : endpointTemplatesByLabel.keySet()) {
				missing.add(label + " (API definition paths missing)");
			}
			return missing;
		}
		for (Map.Entry<String, List<String>> expected : endpointTemplatesByLabel.entrySet()) {
			missing.addAll(missingJsonWriteOperations((Map<?, ?>) paths, expected.getKey(), expected.getValue()));
		}
		return missing;
	}

	static Map<String, Object> assertRequiredJsonResponse(Response response, String reqUri, String source) {
		ETSAssert.assertStatus(response, 200, reqUri);
		assertApplicationJsonContentType(response, reqUri, source);
		Map<String, Object> body = parseBody(response);
		if (body == null) {
			ETSAssert.failWithUri(reqUri,
					source + " body did not parse as JSON. Content-Type was: " + response.getContentType());
		}
		return body;
	}

	static Map<String, Object> parseBody(Response response) {
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

	static void validateResponseAgainstSchema(Response response, String schemaFile, String reqUri, String source) {
		validateJsonTextAgainstSchema(response.getBody().asString(), schemaFile, reqUri, source);
	}

	static void validateJsonValueAgainstSchema(Object value, String schemaFile, String reqUri, String source) {
		try {
			JsonNode node = JSON.valueToTree(value);
			validateJsonNodeAgainstSchema(node, schemaFile, reqUri, source);
		}
		catch (IllegalArgumentException ex) {
			ETSAssert.failWithUri(reqUri, source + " could not be converted for schema validation against " + schemaFile
					+ ": " + ex.getMessage());
		}
	}

	static List<String> schemaValidationErrors(Object value, Object schemaObject) {
		try {
			JsonNode valueNode = JSON.valueToTree(value);
			JsonNode schemaNode = JSON.valueToTree(schemaObject);
			JsonSchema schema = SCHEMA_FACTORY.getSchema(schemaNode);
			return schema.validate(valueNode).stream().map(ValidationMessage::getMessage).collect(Collectors.toList());
		}
		catch (RuntimeException ex) {
			return List.of(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
		}
	}

	static void assertValueMatchesParentSchema(Object value, Object schemaObject, String reqUri, String source) {
		List<String> errors = schemaValidationErrors(value, schemaObject);
		if (!errors.isEmpty()) {
			ETSAssert.failWithUri(reqUri, source + " does not validate against parent JSON Schema: "
					+ errors.stream().limit(8).collect(Collectors.joining("; ")));
		}
	}

	@SuppressWarnings("unchecked")
	static Map<String, Object> castMap(Object value) {
		return (Map<String, Object>) value;
	}

	private static List<String> missingJsonWriteOperations(Map<?, ?> paths, String label,
			List<String> endpointTemplates) {
		List<String> missing = new ArrayList<>();
		if (endpointTemplates == null || endpointTemplates.isEmpty()) {
			return missing;
		}
		boolean operationSeen = false;
		for (Map.Entry<?, ?> pathEntry : paths.entrySet()) {
			Object apiPath = pathEntry.getKey();
			if (!(apiPath instanceof String) || !matchesAnyTemplate((String) apiPath, endpointTemplates)) {
				continue;
			}
			if (!(pathEntry.getValue() instanceof Map)) {
				continue;
			}
			Map<?, ?> pathMap = (Map<?, ?>) pathEntry.getValue();
			for (String method : List.of("post", "put")) {
				Object operation = pathMap.get(method);
				if (operation == null) {
					continue;
				}
				operationSeen = true;
				if (!(operation instanceof Map) || !requestBodyContainsApplicationJson((Map<?, ?>) operation)) {
					missing.add(label + " " + method.toUpperCase(Locale.ROOT) + " " + apiPath);
				}
			}
		}
		if (!operationSeen) {
			missing.add(label + " (no scoped POST/PUT operation advertised)");
		}
		return missing;
	}

	private static boolean matchesAnyTemplate(String apiPath, List<String> endpointTemplates) {
		return endpointTemplates.stream().anyMatch(template -> pathMatchesTemplate(apiPath, template));
	}

	private static boolean pathMatchesTemplate(String apiPath, String template) {
		List<String> actual = pathSegments(apiPath);
		List<String> expected = pathSegments(template);
		if (actual.size() < expected.size()) {
			return false;
		}
		int offset = actual.size() - expected.size();
		for (int i = 0; i < expected.size(); i++) {
			String expectedSegment = expected.get(i);
			String actualSegment = actual.get(i + offset);
			if (!isTemplateVariable(expectedSegment) && !expectedSegment.equals(actualSegment)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isTemplateVariable(String segment) {
		return segment.startsWith("{") && segment.endsWith("}") && segment.length() > 2;
	}

	private static List<String> pathSegments(String path) {
		if (path == null || path.isBlank()) {
			return List.of();
		}
		String normalized = path.split("\\?", 2)[0].split("#", 2)[0];
		while (normalized.startsWith("/")) {
			normalized = normalized.substring(1);
		}
		while (normalized.endsWith("/")) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		if (normalized.isBlank()) {
			return List.of();
		}
		return List.of(normalized.split("/"));
	}

	private static boolean requestBodyContainsApplicationJson(Map<?, ?> operation) {
		Object requestBody = operation.get("requestBody");
		if (!(requestBody instanceof Map)) {
			return false;
		}
		Object content = ((Map<?, ?>) requestBody).get("content");
		return content instanceof Map
				&& ((Map<?, ?>) content).keySet().stream().anyMatch(Part2JsonSupport::isJsonMediaKey);
	}

	private static boolean isJsonMediaKey(Object key) {
		return key instanceof String && "application/json".equals(((String) key).trim().toLowerCase(Locale.ROOT));
	}

	private static void assertApplicationJsonContentType(Response response, String reqUri, String source) {
		String contentType = response.getContentType();
		if (!isApplicationJsonContentType(contentType)) {
			ETSAssert.failWithUri(reqUri,
					source + " returned Content-Type '" + contentType + "'; expected application/json.");
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

}
