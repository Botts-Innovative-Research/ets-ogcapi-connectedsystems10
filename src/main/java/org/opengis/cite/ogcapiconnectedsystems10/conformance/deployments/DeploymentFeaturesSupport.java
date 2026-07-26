package org.opengis.cite.ogcapiconnectedsystems10.conformance.deployments;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

/**
 * Representation-specific support for the released Deployment procedures.
 */
public final class DeploymentFeaturesSupport {

	static final String GEOJSON = "application/geo+json";

	static final String SENSORML = "application/sml+json";

	static final Set<String> SUPPORTED_MEDIA_TYPES = Set.of(GEOJSON, SENSORML);

	private static final String LOCAL_SCHEMA_PREFIX = "https://csapi-compliance.local/schemas/";

	private static final ObjectMapper JSON = new ObjectMapper();

	private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(
			SpecVersion.VersionFlag.V202012,
			builder -> builder.schemaMappers(mappers -> mappers.mapPrefix(LOCAL_SCHEMA_PREFIX, "classpath:schemas/")
				.mapPrefix("https://geojson.org/schema/", "classpath:schemas/external/geojson.org/schema/")));

	private DeploymentFeaturesSupport() {
	}

	static List<Map<String, Object>> selectDeploymentCollections(List<?> advertised) {
		List<Map<String, Object>> selected = new ArrayList<>();
		if (advertised == null) {
			return List.of();
		}
		for (Object value : advertised) {
			if (!(value instanceof Map) || !"sosa:Deployment".equals(((Map<?, ?>) value).get("featureType"))) {
				continue;
			}
			@SuppressWarnings("unchecked")
			Map<String, Object> collection = (Map<String, Object>) value;
			selected.add(collection);
		}
		return List.copyOf(selected);
	}

	static void requireDeploymentCollectionMetadata(Map<String, Object> collection, String requirement) {
		if (collection == null || !(collection.get("id") instanceof String)
				|| ((String) collection.get("id")).isBlank()) {
			ETSAssert.failWithUri(requirement, "Deployment collection is missing a non-empty string id.");
		}
		if (!"feature".equals(collection.get("itemType"))) {
			ETSAssert.failWithUri(requirement,
					"Deployment collection " + collection.get("id") + " must advertise itemType=feature.");
		}
		if (!"sosa:Deployment".equals(collection.get("featureType"))) {
			ETSAssert.failWithUri(requirement,
					"Deployment collection " + collection.get("id") + " must advertise featureType=sosa:Deployment.");
		}
	}

	static CanonicalLink canonicalLink(Map<String, Object> resource, URI pageSource, URI apiRoot, String requirement) {
		Object idValue = resource == null ? null : resource.get("id");
		if (!(idValue instanceof String) || ((String) idValue).isBlank()) {
			ETSAssert.failWithUri(requirement, pageSource + " Deployment item is missing a non-empty string id.");
		}
		Object links = resource.get("links");
		if (!(links instanceof List)) {
			ETSAssert.failWithUri(requirement, pageSource + " Deployment item is missing a links array.");
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
				ETSAssert.failWithUri(requirement, pageSource + " Deployment has a canonical link without an href.");
			}
			try {
				String mediaType = link.get("type") instanceof String ? normalizeMediaType((String) link.get("type"))
						: "";
				CanonicalLink canonical = new CanonicalLink(pageSource.resolve((String) href), mediaType);
				if (!sameOrigin(apiRoot, canonical.uri())) {
					ETSAssert.failWithUri(requirement,
							"refusing cross-origin canonical URL from " + apiRoot + " to " + canonical.uri() + ".");
				}
				String encodedId = encodePathToken((String) idValue);
				URI expected = apiRoot.resolve("deployments/" + encodedId);
				if (canonical.uri().getFragment() != null
						|| !expected.getRawPath().equals(canonical.uri().getRawPath())) {
					ETSAssert.failWithUri(requirement,
							canonical.uri() + " is not the canonical Deployment path " + expected.getRawPath() + ".");
				}
				candidates.add(canonical);
			}
			catch (IllegalArgumentException ex) {
				ETSAssert.failWithUri(requirement,
						pageSource + " Deployment has an invalid canonical-link href: " + href + ".");
			}
		}
		if (candidates.isEmpty()) {
			ETSAssert.failWithUri(requirement, pageSource + " Deployment must expose at least one canonical link.");
		}
		return candidates.get(0);
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

	/**
	 * Validates every returned Deployment collection page against its released schema.
	 * @param endpoint endpoint under test.
	 * @param pages complete traversal evidence.
	 * @param requirement owning released requirement URI.
	 * @return {@code true} when every page used a supported representation.
	 */
	public static boolean validateDeploymentEndpoint(URI endpoint, List<PageDocument> pages, String requirement) {
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
				case GEOJSON -> schema = LOCAL_SCHEMA_PREFIX + "connected-systems-1/geojson/deploymentCollection.json";
				case SENSORML ->
					schema = LOCAL_SCHEMA_PREFIX + "connected-systems-1/sensorml/deploymentCollection.json";
				default -> {
					Reporter.log(requirement + " - " + page.source() + " returned unsupported media type '"
							+ page.mediaType() + "'; Deployment validation was not executed.", true);
					supported = false;
					continue;
				}
			}
			validateDeploymentCollection(page, schema, requirement);
		}
		return supported;
	}

	static boolean referencesSystem(Map<String, Object> deployment, String mediaType, String systemId, URI apiRoot) {
		if (deployment == null || systemId == null || systemId.isBlank() || apiRoot == null) {
			return false;
		}
		List<String> hrefs = new ArrayList<>();
		String normalized = normalizeMediaType(mediaType);
		if (GEOJSON.equals(normalized)) {
			Object properties = deployment.get("properties");
			if (properties instanceof Map) {
				collectHrefs(((Map<?, ?>) properties).get("deployedSystems@link"), hrefs, false);
			}
		}
		else if (SENSORML.equals(normalized)) {
			collectHrefs(deployment.get("deployedSystems"), hrefs, true);
		}
		URI expected = apiRoot.resolve("systems/" + encodePathToken(systemId));
		for (String href : hrefs) {
			try {
				URI actual = apiRoot.resolve(href);
				if (actual.getFragment() == null && sameOrigin(apiRoot, actual)
						&& expected.getRawPath().equals(actual.getRawPath())) {
					return true;
				}
			}
			catch (IllegalArgumentException ex) {
				// Invalid links cannot establish the required owning-System reference.
			}
		}
		return false;
	}

	private static void collectHrefs(Object values, List<String> hrefs, boolean nestedSystem) {
		if (!(values instanceof List)) {
			return;
		}
		for (Object value : (List<?>) values) {
			if (!(value instanceof Map)) {
				continue;
			}
			Map<?, ?> link = (Map<?, ?>) value;
			if (nestedSystem && link.get("system") instanceof Map) {
				link = (Map<?, ?>) link.get("system");
			}
			if (link.get("href") instanceof String && !((String) link.get("href")).isBlank()) {
				hrefs.add((String) link.get("href"));
			}
		}
	}

	private static void validateDeploymentCollection(PageDocument page, String schema, String requirement) {
		try {
			JsonSchema jsonSchema = SCHEMA_FACTORY.getSchema(SchemaLocation.of(schema), schemaConfig());
			Set<ValidationMessage> errors = jsonSchema.validate(JSON.valueToTree(page.body()));
			if (!errors.isEmpty()) {
				String joined = errors.stream()
					.limit(8)
					.map(ValidationMessage::getMessage)
					.collect(Collectors.joining("; "));
				ETSAssert.failWithUri(requirement,
						page.source() + " failed Deployment collection schema validation: " + joined);
			}
		}
		catch (RuntimeException ex) {
			ETSAssert.failWithUri(requirement,
					page.source() + " could not be schema-validated against " + schema + ": " + ex.getMessage());
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

	private static String encodePathToken(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
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
	 * Canonical Deployment link target and requested representation.
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
