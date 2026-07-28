package org.opengis.cite.ogcapiconnectedsystems10.conformance.propertydefinitions;

import java.net.URI;
import java.util.ArrayList;
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
import org.testng.SkipException;

/**
 * Representation-specific support for the released Property Definitions procedures.
 */
public final class PropertyDefinitionsSupport {

	static final String SENSORML = "application/sml+json";

	static final Set<String> SUPPORTED_MEDIA_TYPES = Set.of(SENSORML);

	private static final String LOCAL_SCHEMA_PREFIX = "https://csapi-compliance.local/schemas/";

	private static final String PROPERTY_COLLECTION_SCHEMA = LOCAL_SCHEMA_PREFIX
			+ "connected-systems-1/sensorml/propertyCollection.json";

	private static final ObjectMapper JSON = new ObjectMapper();

	private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(
			SpecVersion.VersionFlag.V202012,
			builder -> builder.schemaMappers(mappers -> mappers.mapPrefix(LOCAL_SCHEMA_PREFIX, "classpath:schemas/")));

	private PropertyDefinitionsSupport() {
	}

	static List<Map<String, Object>> selectPropertyCollections(List<?> advertised) {
		List<Map<String, Object>> selected = new ArrayList<>();
		if (advertised == null) {
			return List.of();
		}
		for (Object value : advertised) {
			if (!(value instanceof Map) || !"sosa:Property".equals(((Map<?, ?>) value).get("itemType"))) {
				continue;
			}
			@SuppressWarnings("unchecked")
			Map<String, Object> collection = (Map<String, Object>) value;
			selected.add(collection);
		}
		return List.copyOf(selected);
	}

	static void requirePropertyCollectionMetadata(Map<String, Object> collection, String requirement) {
		if (collection == null || !(collection.get("id") instanceof String)
				|| ((String) collection.get("id")).isBlank()) {
			ETSAssert.failWithUri(requirement, "Property collection is missing a non-empty string id.");
		}
		if (!"sosa:Property".equals(collection.get("itemType"))) {
			ETSAssert.failWithUri(requirement,
					"Property collection " + collection.get("id") + " must advertise itemType=sosa:Property.");
		}
	}

	/**
	 * Validates every returned Property collection page against the released SensorML
	 * Property schema.
	 * @param endpoint endpoint under test.
	 * @param pages complete traversal evidence.
	 * @param requirement owning released requirement URI.
	 * @return {@code true} when every page used a supported representation.
	 */
	public static boolean validatePropertyEndpoint(URI endpoint, List<PageDocument> pages, String requirement) {
		if (endpoint == null || !endpoint.isAbsolute()) {
			throw new IllegalArgumentException("endpoint must be an absolute URI");
		}
		if (pages == null || pages.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " traversal produced no representation pages.");
		}
		boolean supported = true;
		for (PageDocument page : pages) {
			if (!SENSORML.equals(normalizeMediaType(page.mediaType()))) {
				Reporter.log(requirement + " - " + page.source() + " returned unsupported media type '"
						+ page.mediaType() + "'; Property validation was not executed.", true);
				supported = false;
				continue;
			}
			validatePropertyCollection(page, requirement);
		}
		return supported;
	}

	static CanonicalLink canonicalLink(Map<String, Object> resource, URI pageSource, URI apiRoot,
			String comparisonMediaType, String requirement) {
		Object links = resource == null ? null : resource.get("links");
		if (!(links instanceof List)) {
			ETSAssert.failWithUri(requirement, pageSource + " Property item is missing a links array.");
		}
		List<CanonicalLink> candidates = new ArrayList<>();
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
				ETSAssert.failWithUri(requirement, pageSource + " Property has a canonical link without an href.");
			}
			try {
				String mediaType = link.get("type") instanceof String ? normalizeMediaType((String) link.get("type"))
						: "";
				CanonicalLink canonical = new CanonicalLink(pageSource.resolve((String) href), mediaType);
				requireCanonicalPropertyPath(canonical.uri(), apiRoot, requirement);
				candidates.add(canonical);
			}
			catch (IllegalArgumentException ex) {
				ETSAssert.failWithUri(requirement,
						pageSource + " Property has an invalid canonical-link href: " + href + ".");
			}
		}
		if (candidates.isEmpty()) {
			ETSAssert.failWithUri(requirement, pageSource + " Property must expose at least one canonical link.");
		}
		String comparable = normalizeMediaType(comparisonMediaType);
		return candidates.stream()
			.filter(candidate -> candidate.mediaType().isBlank() || candidate.mediaType().equals(comparable))
			.findFirst()
			.orElseThrow(() -> new SkipException(requirement + " - " + pageSource
					+ " has canonical links, but none advertises a representation comparable with '" + comparable
					+ "'."));
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

	private static void validatePropertyCollection(PageDocument page, String requirement) {
		try {
			JsonSchema schema = SCHEMA_FACTORY.getSchema(SchemaLocation.of(PROPERTY_COLLECTION_SCHEMA), schemaConfig());
			Set<ValidationMessage> errors = schema.validate(JSON.valueToTree(page.body()));
			if (!errors.isEmpty()) {
				String joined = errors.stream()
					.limit(8)
					.map(ValidationMessage::getMessage)
					.collect(Collectors.joining("; "));
				ETSAssert.failWithUri(requirement,
						page.source() + " failed Property collection schema validation: " + joined);
			}
		}
		catch (RuntimeException ex) {
			ETSAssert.failWithUri(requirement, page.source() + " could not be schema-validated against "
					+ PROPERTY_COLLECTION_SCHEMA + ": " + ex.getMessage());
		}
	}

	private static void requireCanonicalPropertyPath(URI canonical, URI apiRoot, String requirement) {
		if (!sameOrigin(apiRoot, canonical)) {
			ETSAssert.failWithUri(requirement,
					"refusing cross-origin canonical URL from " + apiRoot + " to " + canonical + ".");
		}
		if (canonical.getFragment() != null) {
			ETSAssert.failWithUri(requirement, canonical + " must not contain a fragment.");
		}
		String prefix = apiRoot.resolve("properties/").getRawPath();
		String path = canonical.getRawPath();
		String localId = path != null && path.startsWith(prefix) ? path.substring(prefix.length()) : "";
		if (localId.isEmpty() || localId.contains("/")) {
			ETSAssert.failWithUri(requirement,
					canonical + " is not below the canonical Property path " + prefix + "{id}.");
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

	/**
	 * Canonical Property link target and requested representation.
	 */
	public record CanonicalLink(URI uri, String mediaType) {

		public CanonicalLink {
			if (uri == null || !uri.isAbsolute()) {
				throw new IllegalArgumentException("uri must be absolute");
			}
			mediaType = Optional.ofNullable(mediaType).orElse("");
		}

	}

}
