package org.opengis.cite.ogcapiconnectedsystems10.conformance.systemfeatures;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
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
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.PageDocument;
import org.testng.Reporter;

/**
 * Representation-neutral support for the released System conformance procedures.
 */
public final class SystemFeaturesSupport {

	private static final String GEOJSON = "application/geo+json";

	private static final String SENSORML = "application/sml+json";

	private static final String LOCAL_SCHEMA_PREFIX = "https://csapi-compliance.local/schemas/";

	private static final ObjectMapper JSON = new ObjectMapper();

	private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(
			SpecVersion.VersionFlag.V202012,
			builder -> builder.schemaMappers(mappers -> mappers.mapPrefix(LOCAL_SCHEMA_PREFIX, "classpath:schemas/")
				.mapPrefix("https://geojson.org/schema/", "classpath:schemas/external/geojson.org/schema/")));

	private static final Set<String> ALLOWED_SYSTEM_TYPES = Set.of("sosa:Sensor", "sosa:Actuator", "sosa:Sampler",
			"sosa:Platform", "sosa:System", "http://www.w3.org/ns/sosa/Sensor", "http://www.w3.org/ns/sosa/Actuator",
			"http://www.w3.org/ns/sosa/Sampler", "http://www.w3.org/ns/sosa/Platform",
			"http://www.w3.org/ns/sosa/System");

	private SystemFeaturesSupport() {
	}

	static Optional<String> assetType(Map<String, Object> system, String mediaType) {
		if (system == null) {
			return Optional.empty();
		}
		if (GEOJSON.equals(normalizeMediaType(mediaType))) {
			return nestedString(system, "properties", "assetType");
		}
		if (SENSORML.equals(normalizeMediaType(mediaType))) {
			Object classifiers = system.get("classifiers");
			if (classifiers instanceof List) {
				for (Object value : (List<?>) classifiers) {
					if (!(value instanceof Map)) {
						continue;
					}
					Map<?, ?> classifier = (Map<?, ?>) value;
					if (isAssetTypeDefinition(classifier.get("definition")) && classifier.get("value") instanceof String
							&& !((String) classifier.get("value")).isBlank()) {
						return Optional.of((String) classifier.get("value"));
					}
				}
			}
		}
		return string(system.get("assetType"));
	}

	static Optional<String> systemType(Map<String, Object> system, String mediaType) {
		if (system == null) {
			return Optional.empty();
		}
		if (GEOJSON.equals(normalizeMediaType(mediaType))) {
			return nestedString(system, "properties", "featureType");
		}
		if (SENSORML.equals(normalizeMediaType(mediaType))) {
			return string(system.get("definition"));
		}
		return string(system.get("featureType")).or(() -> string(system.get("definition")));
	}

	static Optional<JsonNode> location(Map<String, Object> system, String mediaType) {
		if (system == null) {
			return Optional.empty();
		}
		String normalized = normalizeMediaType(mediaType);
		Object value = GEOJSON.equals(normalized) ? system.get("geometry")
				: SENSORML.equals(normalized) ? system.get("position")
						: system.containsKey("geometry") ? system.get("geometry") : system.get("position");
		if (value == null) {
			return Optional.empty();
		}
		if (SENSORML.equals(normalized) && value instanceof Map) {
			Map<?, ?> position = (Map<?, ?>) value;
			Object type = position.get("type");
			Object coordinates = position.get("position");
			if (("GeoPose".equals(type) || "RelativePose".equals(type)) && coordinates != null) {
				value = coordinates;
			}
		}
		return Optional.of(JSON.valueToTree(value));
	}

	static boolean isVirtualAsset(String assetType) {
		return "Simulation".equals(assetType) || "Process".equals(assetType);
	}

	static boolean isAllowedSystemType(String systemType) {
		return ALLOWED_SYSTEM_TYPES.contains(systemType);
	}

	static List<Map<String, Object>> selectSystemCollections(List<?> advertised) {
		List<Map<String, Object>> selected = new ArrayList<>();
		if (advertised == null) {
			return List.of();
		}
		for (Object value : advertised) {
			if (!(value instanceof Map) || !"sosa:System".equals(((Map<?, ?>) value).get("featureType"))) {
				continue;
			}
			@SuppressWarnings("unchecked")
			Map<String, Object> collection = (Map<String, Object>) value;
			selected.add(collection);
		}
		return List.copyOf(selected);
	}

	static JsonNode withoutCanonicalLinks(Map<String, Object> resource) {
		JsonNode copied = JSON.valueToTree(resource);
		if (!(copied instanceof ObjectNode)) {
			return copied;
		}
		JsonNode links = copied.get("links");
		if (!(links instanceof ArrayNode)) {
			return copied;
		}
		ArrayNode retained = JSON.createArrayNode();
		for (JsonNode link : links) {
			if (!hasRelation(link.get("rel"), "canonical")) {
				retained.add(link);
			}
		}
		((ObjectNode) copied).set("links", retained);
		return copied;
	}

	static URI canonicalUri(Map<String, Object> resource, URI pageSource, URI apiRoot, String requirement) {
		Object links = resource == null ? null : resource.get("links");
		if (!(links instanceof List)) {
			ETSAssert.failWithUri(requirement, pageSource + " item is missing a links array.");
		}
		Set<URI> candidates = new HashSet<>();
		for (Object value : (List<?>) links) {
			if (!(value instanceof Map)) {
				continue;
			}
			Map<?, ?> link = (Map<?, ?>) value;
			if (!hasRelation(JSON.valueToTree(link.get("rel")), "canonical")) {
				continue;
			}
			Object href = link.get("href");
			if (!(href instanceof String) || ((String) href).isBlank()) {
				ETSAssert.failWithUri(requirement, pageSource + " item has a canonical link without an href.");
			}
			try {
				candidates.add(pageSource.resolve((String) href));
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
		URI canonical = candidates.iterator().next();
		if (!sameOrigin(apiRoot, canonical)) {
			ETSAssert.failWithUri(requirement,
					"refusing cross-origin canonical URL from " + apiRoot + " to " + canonical + ".");
		}
		return canonical;
	}

	/**
	 * Executes the released parameterized resources-endpoint validation procedure.
	 * @param endpoint endpoint URL under test.
	 * @param pages complete retrieved page evidence.
	 * @param requirement owning released requirement URI.
	 * @return {@code true} when every page used a supported media type and validated;
	 * {@code false} when the endpoint media type is unsupported.
	 */
	public static boolean validateSystemEndpoint(URI endpoint, List<PageDocument> pages, String requirement) {
		if (endpoint == null || !endpoint.isAbsolute()) {
			throw new IllegalArgumentException("endpoint must be an absolute URI");
		}
		if (pages == null || pages.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " traversal produced no representation pages.");
		}
		boolean supported = true;
		for (PageDocument page : pages) {
			String schema;
			switch (normalizeMediaType(page.mediaType())) {
				case GEOJSON -> schema = LOCAL_SCHEMA_PREFIX + "connected-systems-1/geojson/systemCollection.json";
				case SENSORML -> schema = LOCAL_SCHEMA_PREFIX + "connected-systems-1/sensorml/systemCollection.json";
				default -> {
					Reporter.log(
							requirement + " - " + page.source() + " returned unsupported media type '"
									+ page.mediaType() + "'; endpoint procedure not executed for this representation.",
							true);
					supported = false;
					continue;
				}
			}
			validateSystemCollection(page, schema, requirement);
		}
		return supported;
	}

	private static void validateSystemCollection(PageDocument page, String schema, String requirement) {
		try {
			JsonSchema jsonSchema = SCHEMA_FACTORY.getSchema(SchemaLocation.of(schema), schemaConfig());
			Set<ValidationMessage> errors = jsonSchema.validate(JSON.valueToTree(page.body()));
			if (!errors.isEmpty()) {
				String joined = errors.stream()
					.limit(8)
					.map(ValidationMessage::getMessage)
					.collect(Collectors.joining("; "));
				ETSAssert.failWithUri(requirement,
						page.source() + " failed System collection schema validation: " + joined);
			}
		}
		catch (RuntimeException ex) {
			ETSAssert.failWithUri(requirement,
					page.source() + " could not be schema-validated against " + schema + ": " + ex.getMessage());
		}
	}

	private static Optional<String> nestedString(Map<String, Object> value, String parent, String child) {
		Object nested = value.get(parent);
		return nested instanceof Map ? string(((Map<?, ?>) nested).get(child)) : Optional.empty();
	}

	private static Optional<String> string(Object value) {
		return value instanceof String && !((String) value).isBlank() ? Optional.of((String) value) : Optional.empty();
	}

	private static boolean isAssetTypeDefinition(Object definition) {
		if (!(definition instanceof String)) {
			return false;
		}
		String normalized = ((String) definition).toLowerCase(Locale.ROOT);
		return "cs:assettype".equals(normalized) || normalized.endsWith("/assettype")
				|| normalized.endsWith("#assettype");
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
		if (mediaType == null) {
			return "";
		}
		return mediaType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
	}

	private static SchemaValidatorsConfig schemaConfig() {
		SchemaValidatorsConfig config = new SchemaValidatorsConfig();
		config.setFormatAssertionsEnabled(true);
		return config;
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
