package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.controlstream;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.PageDocument;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.Part2SchemaValidation;
import org.testng.Reporter;
import org.testng.SkipException;

import io.restassured.response.Response;

/**
 * Exact released ATS support for OGC 23-002 Part 2 Control Streams and Commands.
 */
final class Part2ControlStreamSupport {

	static final String JSON = "application/json";

	static final String GEOJSON = "application/geo+json";

	static final Set<String> JSON_MEDIA = Set.of(JSON);

	static final Set<String> FEATURE_OF_INTEREST_MEDIA = Set.of(GEOJSON, JSON);

	static final String CONTROL_STREAM_COLLECTION_SCHEMA = "controlStreamCollection.json";

	static final String CONTROL_STREAM_SCHEMA = "controlStream.json";

	static final String COMMAND_COLLECTION_SCHEMA = "commandCollection.json";

	static final String COMMAND_SCHEMA = "command.json";

	static final String COMMAND_STATUS_COLLECTION_SCHEMA = "commandStatusCollection.json";

	static final String COMMAND_STATUS_SCHEMA = "commandStatus.json";

	static final String COMMAND_RESULT_COLLECTION_SCHEMA = "commandResultCollection.json";

	static final String COMMAND_RESULT_SCHEMA = "commandResult.json";

	static final String COMMAND_SCHEMA_SCHEMA = "commandSchema.json";

	static final String GEOJSON_FEATURE_COLLECTION_SCHEMA = "https://geojson.org/schema/FeatureCollection.json";

	private static final String SCHEMA_IRI_PREFIX = "https://csapi-compliance.local/schemas/connected-systems-2/json/";

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

	private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory
		.getInstance(SpecVersion.VersionFlag.V202012,
				builder -> builder.schemaMappers(mappers -> mappers
					.mapPrefix("https://csapi-compliance.local/schemas/", "classpath:schemas/")
					.mapPrefix("https://geojson.org/schema/", "classpath:schemas/external/geojson.org/schema/")));

	private Part2ControlStreamSupport() {
	}

	static void validateControlStreamEndpoint(URI endpoint, List<PageDocument> pages, String requirement) {
		validateJsonEndpoint(endpoint, pages, CONTROL_STREAM_COLLECTION_SCHEMA, CONTROL_STREAM_SCHEMA, "ControlStream",
				requirement);
	}

	static void validateCommandEndpoint(URI endpoint, List<PageDocument> pages, String requirement) {
		validateJsonEndpoint(endpoint, pages, COMMAND_COLLECTION_SCHEMA, COMMAND_SCHEMA, "Command", requirement);
	}

	static void validateCommandStatusEndpoint(URI endpoint, List<PageDocument> pages, String requirement) {
		validateJsonEndpoint(endpoint, pages, COMMAND_STATUS_COLLECTION_SCHEMA, COMMAND_STATUS_SCHEMA, "CommandStatus",
				requirement);
	}

	static void validateCommandResultEndpoint(URI endpoint, List<PageDocument> pages, String requirement) {
		validateJsonEndpoint(endpoint, pages, COMMAND_RESULT_COLLECTION_SCHEMA, COMMAND_RESULT_SCHEMA, "CommandResult",
				requirement);
	}

	static void validateFeatureOfInterestEndpoint(URI endpoint, List<PageDocument> pages, String requirement) {
		if (endpoint == null || !endpoint.isAbsolute()) {
			throw new IllegalArgumentException("endpoint must be an absolute URI");
		}
		if (pages == null || pages.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " traversal produced no representation pages.");
		}
		for (PageDocument page : pages) {
			String mediaType = normalizeMediaType(page.mediaType());
			if (GEOJSON.equals(mediaType) || JSON.equals(mediaType)) {
				validateJsonValueAgainstSchema(page.body(), GEOJSON_FEATURE_COLLECTION_SCHEMA, requirement,
						page.source().toString());
				continue;
			}
			ETSAssert.failWithUri(requirement,
					page.source() + " returned unsupported FeatureOfInterest media type '" + page.mediaType() + "'.");
		}
	}

	static void validateControlStreamResource(Map<String, Object> body, String requirement, String source) {
		validateJsonValueAgainstSchema(body, CONTROL_STREAM_SCHEMA, requirement, source);
	}

	static void validateCommandResource(Map<String, Object> body, String requirement, String source) {
		validateJsonValueAgainstSchema(body, COMMAND_SCHEMA, requirement, source);
	}

	static void validateCommandStatusResource(Map<String, Object> body, String requirement, String source) {
		validateJsonValueAgainstSchema(body, COMMAND_STATUS_SCHEMA, requirement, source);
	}

	static void validateCommandResultResource(Map<String, Object> body, String requirement, String source) {
		validateJsonValueAgainstSchema(body, COMMAND_RESULT_SCHEMA, requirement, source);
	}

	static List<Map<String, Object>> collectionsWithItemType(List<Map<String, Object>> collections, String itemType) {
		if (collections == null || itemType == null) {
			return List.of();
		}
		List<Map<String, Object>> selected = new ArrayList<>();
		for (Map<String, Object> collection : collections) {
			if (isCollectionTagged(collection, itemType)) {
				selected.add(collection);
			}
		}
		return List.copyOf(selected);
	}

	static boolean isCollectionTagged(Map<String, Object> collection, String itemType) {
		return collection != null && itemType != null && itemType.equals(collection.get("itemType"));
	}

	static List<String> localIds(List<Map<String, Object>> resources, String requirement) {
		List<String> ids = new ArrayList<>();
		for (Map<String, Object> resource : resources == null ? List.<Map<String, Object>>of() : resources) {
			Object id = resource.get("id");
			if (!(id instanceof String) || ((String) id).isBlank()) {
				ETSAssert.failWithUri(requirement, "Resource item is missing a non-empty local id.");
			}
			ids.add((String) id);
		}
		return List.copyOf(ids);
	}

	static URI canonicalUri(Map<String, Object> resource, URI pageSource, URI apiRoot, String requirement) {
		Object links = resource == null ? null : resource.get("links");
		if (!(links instanceof List)) {
			ETSAssert.failWithUri(requirement, pageSource + " item is missing a links array.");
		}
		Set<URI> candidates = new LinkedHashSet<>();
		for (Object value : (List<?>) links) {
			if (!(value instanceof Map)) {
				continue;
			}
			Map<?, ?> link = (Map<?, ?>) value;
			if (!hasRelation(JSON_MAPPER.valueToTree(link.get("rel")), "canonical")) {
				continue;
			}
			Object href = link.get("href");
			if (!(href instanceof String) || ((String) href).isBlank()) {
				ETSAssert.failWithUri(requirement, pageSource + " item has a canonical link without an href.");
			}
			try {
				URI canonical = pageSource.resolve((String) href);
				if (!sameOrigin(apiRoot, canonical)) {
					ETSAssert.failWithUri(requirement,
							"refusing cross-origin canonical URL from " + apiRoot + " to " + canonical + ".");
				}
				candidates.add(canonical);
			}
			catch (IllegalArgumentException ex) {
				ETSAssert.failWithUri(requirement,
						pageSource + " item has an invalid canonical-link href: " + href + ".");
			}
		}
		if (candidates.size() != 1) {
			ETSAssert.failWithUri(requirement,
					pageSource + " item must expose exactly one distinct canonical URL; found " + candidates + ".");
		}
		return candidates.iterator().next();
	}

	static boolean hasCanonicalLink(Map<String, Object> resource) {
		Object links = resource == null ? null : resource.get("links");
		if (!(links instanceof List)) {
			return false;
		}
		for (Object value : (List<?>) links) {
			if (value instanceof Map
					&& hasRelation(JSON_MAPPER.valueToTree(((Map<?, ?>) value).get("rel")), "canonical")) {
				return true;
			}
		}
		return false;
	}

	static JsonNode withoutCanonicalLinks(Map<String, Object> resource) {
		JsonNode copied = JSON_MAPPER.valueToTree(resource);
		if (!(copied instanceof ObjectNode)) {
			return copied;
		}
		JsonNode links = copied.get("links");
		if (!(links instanceof ArrayNode)) {
			return copied;
		}
		ArrayNode retained = JSON_MAPPER.createArrayNode();
		boolean canonicalRemoved = false;
		for (JsonNode link : links) {
			if (hasRelation(link.get("rel"), "canonical")) {
				canonicalRemoved = true;
			}
			else {
				retained.add(link);
			}
		}
		if (canonicalRemoved && retained.isEmpty()) {
			((ObjectNode) copied).remove("links");
		}
		else {
			((ObjectNode) copied).set("links", retained);
		}
		return copied;
	}

	static List<String> commandFormats(Map<String, Object> controlStream) {
		Object formats = controlStream == null ? null : controlStream.get("formats");
		if (!(formats instanceof List)) {
			return List.of();
		}
		List<String> result = new ArrayList<>();
		for (Object value : (List<?>) formats) {
			Optional<String> format = Optional.empty();
			if (value instanceof String) {
				format = string(value);
			}
			else if (value instanceof Map) {
				Map<?, ?> map = (Map<?, ?>) value;
				format = string(map.get("cmdFormat")).or(() -> string(map.get("commandFormat")))
					.or(() -> string(map.get("format")))
					.or(() -> string(map.get("type")));
			}
			format.ifPresent(result::add);
		}
		return result.stream().distinct().toList();
	}

	static void validateCommandSchema(Response response, String format, String requirement, String source) {
		ETSAssert.assertStatus(response, 200, requirement);
		String mediaType = normalizeMediaType(response.getContentType());
		if (!JSON.equals(mediaType)) {
			throw new SkipException(requirement + " - " + source + " returned unsupported media type '"
					+ response.getContentType() + "'.");
		}
		validateJsonValueAgainstSchema(parseObject(response, URI.create(source), requirement), COMMAND_SCHEMA_SCHEMA,
				requirement, source);
	}

	static Map<String, Object> parseObject(Response response, URI source, String requirement) {
		try {
			Map<String, Object> body = response.jsonPath().getMap("$");
			if (body == null) {
				ETSAssert.failWithUri(requirement, source + " response body is not a JSON object.");
			}
			return body;
		}
		catch (Exception ex) {
			ETSAssert.failWithUri(requirement,
					source + " response body is not parseable as a JSON object: " + ex.getMessage());
			return Map.of();
		}
	}

	static String encodePathToken(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
	}

	private static void validateJsonEndpoint(URI endpoint, List<PageDocument> pages, String collectionSchema,
			String itemSchema, String label, String requirement) {
		if (endpoint == null || !endpoint.isAbsolute()) {
			throw new IllegalArgumentException("endpoint must be an absolute URI");
		}
		if (pages == null || pages.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " traversal produced no representation pages.");
		}
		for (PageDocument page : pages) {
			if (!JSON.equals(normalizeMediaType(page.mediaType()))) {
				Reporter.log(requirement + " - " + page.source() + " returned unsupported media type '"
						+ page.mediaType() + "'; " + label + " JSON validation was not executed.", true);
				throw new SkipException(requirement + " - " + page.source() + " returned unsupported media type '"
						+ page.mediaType() + "'.");
			}
			validateJsonValueAgainstSchema(page.body(), collectionSchema, requirement, page.source().toString());
			for (Map<String, Object> item : page.items()) {
				validateJsonValueAgainstSchema(item, itemSchema, requirement, page.source() + " item");
			}
		}
	}

	private static void validateJsonValueAgainstSchema(Object value, String schemaFile, String requirement,
			String source) {
		try {
			JsonNode node = JSON_MAPPER.valueToTree(value);
			validateJsonNodeAgainstSchema(node, schemaFile, requirement, source);
		}
		catch (IllegalArgumentException ex) {
			ETSAssert.failWithUri(requirement, source + " could not be converted for schema validation against "
					+ schemaFile + ": " + ex.getMessage());
		}
	}

	private static void validateJsonNodeAgainstSchema(JsonNode node, String schemaFile, String requirement,
			String source) {
		try {
			String schemaIri = schemaFile.startsWith("http://") || schemaFile.startsWith("https://") ? schemaFile
					: SCHEMA_IRI_PREFIX + schemaFile;
			JsonSchema schema = Part2SchemaValidation.getSchema(SCHEMA_FACTORY, schemaIri);
			Set<ValidationMessage> errors = schema.validate(node);
			if (!errors.isEmpty()) {
				String joined = errors.stream()
					.limit(8)
					.map(ValidationMessage::getMessage)
					.collect(Collectors.joining("; "));
				ETSAssert.failWithUri(requirement,
						source + " failed schema validation against " + schemaFile + ": " + joined);
			}
		}
		catch (RuntimeException ex) {
			ETSAssert.failWithUri(requirement,
					source + " could not be schema-validated against " + schemaFile + ": " + ex.getMessage());
		}
	}

	private static Optional<String> string(Object value) {
		return value instanceof String && !((String) value).isBlank() ? Optional.of((String) value) : Optional.empty();
	}

	private static boolean hasRelation(JsonNode value, String expected) {
		if (value == null) {
			return false;
		}
		if (value.isTextual()) {
			return expected.equalsIgnoreCase(value.asText());
		}
		if (value.isArray()) {
			for (JsonNode relation : value) {
				if (relation.isTextual() && expected.equalsIgnoreCase(relation.asText())) {
					return true;
				}
			}
		}
		return false;
	}

	private static String normalizeMediaType(String mediaType) {
		if (mediaType == null || mediaType.isBlank()) {
			return "";
		}
		return mediaType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
	}

	private static boolean sameOrigin(URI left, URI right) {
		return left != null && right != null && left.getScheme() != null && right.getScheme() != null
				&& left.getScheme().equalsIgnoreCase(right.getScheme()) && left.getHost() != null
				&& right.getHost() != null && left.getHost().equalsIgnoreCase(right.getHost())
				&& effectivePort(left) == effectivePort(right);
	}

	private static int effectivePort(URI uri) {
		if (uri.getPort() >= 0) {
			return uri.getPort();
		}
		return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
	}

}
