package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.systemevent;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
 * Exact released ATS support for OGC 23-002 Part 2 System Events.
 */
public final class Part2SystemEventSupport {

	public static final String JSON = "application/json";

	public static final Set<String> JSON_MEDIA = Set.of(JSON);

	static final String SYSTEM_EVENT_COLLECTION_SCHEMA = "systemEventCollection.json";

	static final String SYSTEM_EVENT_SCHEMA = "systemEvent.json";

	private static final String SCHEMA_IRI_PREFIX = "https://csapi-compliance.local/schemas/connected-systems-2/json/";

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

	private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory
		.getInstance(SpecVersion.VersionFlag.V202012, builder -> builder.schemaMappers(
				mappers -> mappers.mapPrefix("https://csapi-compliance.local/schemas/", "classpath:schemas/")));

	private Part2SystemEventSupport() {
	}

	public static void validateSystemEventEndpoint(URI endpoint, List<PageDocument> pages, String requirement) {
		if (endpoint == null || !endpoint.isAbsolute()) {
			throw new IllegalArgumentException("endpoint must be an absolute URI");
		}
		if (pages == null || pages.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " traversal produced no representation pages.");
		}
		for (PageDocument page : pages) {
			if (!JSON.equals(normalizeMediaType(page.mediaType()))) {
				Reporter.log(requirement + " - " + page.source() + " returned unsupported media type '"
						+ page.mediaType() + "'; SystemEvent JSON validation was not executed.", true);
				throw new SkipException(requirement + " - " + page.source() + " returned unsupported media type '"
						+ page.mediaType() + "'.");
			}
			validateJsonValueAgainstSchema(page.body(), SYSTEM_EVENT_COLLECTION_SCHEMA, requirement,
					page.source().toString());
			for (Map<String, Object> item : page.items()) {
				validateSystemEventResource(item, requirement, page.source() + " item");
			}
		}
	}

	static void validateSystemEventResource(Map<String, Object> body, String requirement, String source) {
		validateJsonValueAgainstSchema(body, SYSTEM_EVENT_SCHEMA, requirement, source);
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

	public static Map<String, Object> parseObject(Response response, URI source, String requirement) {
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

	public static String encodePathToken(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
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
			JsonSchema schema = Part2SchemaValidation.getSchema(SCHEMA_FACTORY, SCHEMA_IRI_PREFIX + schemaFile);
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
