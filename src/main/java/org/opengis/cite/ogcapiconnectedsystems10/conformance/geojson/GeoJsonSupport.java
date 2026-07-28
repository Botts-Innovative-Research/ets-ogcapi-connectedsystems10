package org.opengis.cite.ogcapiconnectedsystems10.conformance.geojson;

import java.net.URI;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.reprezen.kaizen.oasparser.OpenApi3Parser;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;
import com.reprezen.kaizen.oasparser.model3.Operation;
import com.reprezen.kaizen.oasparser.model3.Path;
import com.reprezen.kaizen.oasparser.model3.RequestBody;
import com.reprezen.kaizen.oasparser.model3.Response;
import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.EncodingRelationTypes;

/**
 * Released GeoJSON schema, API-definition, and mapping assertions.
 */
public final class GeoJsonSupport {

	static final String MEDIA_TYPE = "application/geo+json";

	private static final String LOCAL_SCHEMA_PREFIX = "https://csapi-compliance.local/schemas/";

	private static final ObjectMapper JSON = new ObjectMapper();

	private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(
			SpecVersion.VersionFlag.V202012,
			builder -> builder.schemaMappers(mappers -> mappers.mapPrefix(LOCAL_SCHEMA_PREFIX, "classpath:schemas/")
				.mapPrefix("https://geojson.org/schema/", "classpath:schemas/external/geojson.org/schema/")));

	private static final Set<String> SYSTEM_ASSET_TYPES = Set.of("Equipment", "Human", "LivingThing", "Simulation",
			"Process", "Group", "Other");

	private GeoJsonSupport() {
	}

	/**
	 * GeoJSON resource-specific paths, schemas, and conformance declarations.
	 */
	public enum ResourceType {

		SYSTEM("system", "systems", "system.json", "systemCollection.json",
				"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/system"),

		DEPLOYMENT("deployment", "deployments", "deployment.json", "deploymentCollection.json",
				"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/deployment"),

		PROCEDURE("procedure", "procedures", "procedure.json", "procedureCollection.json",
				"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/procedure"),

		SAMPLING_FEATURE("samplingFeature", "samplingFeatures", "samplingFeature.json",
				"samplingFeatureCollection.json", "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/sf");

		private final String relationType;

		private final String collectionPath;

		private final String singleSchema;

		private final String collectionSchema;

		private final String conformanceUri;

		ResourceType(String relationType, String collectionPath, String singleSchema, String collectionSchema,
				String conformanceUri) {
			this.relationType = relationType;
			this.collectionPath = collectionPath;
			this.singleSchema = singleSchema;
			this.collectionSchema = collectionSchema;
			this.conformanceUri = conformanceUri;
		}

		String relationType() {
			return this.relationType;
		}

		String collectionPath() {
			return this.collectionPath;
		}

		String collectionApiPath() {
			return "/" + this.collectionPath;
		}

		String itemApiPath() {
			return collectionApiPath() + "/{id}";
		}

		String singleSchema() {
			return LOCAL_SCHEMA_PREFIX + "connected-systems-1/geojson/" + this.singleSchema;
		}

		String collectionSchema() {
			return LOCAL_SCHEMA_PREFIX + "connected-systems-1/geojson/" + this.collectionSchema;
		}

		String conformanceUri() {
			return this.conformanceUri;
		}

	}

	/**
	 * Parsed API definition with its source URI retained for diagnostics.
	 *
	 * @param source source URI.
	 * @param model parsed OpenAPI model.
	 */
	public record ApiDefinition(URI source, OpenApi3 model) {
	}

	/**
	 * Parses either JSON or YAML OpenAPI 3 content.
	 * @param content API-definition representation.
	 * @param source source URI used to resolve references.
	 * @param requirement requirement owning the assertion.
	 * @return parsed definition.
	 */
	public static ApiDefinition parseApiDefinition(String content, URI source, String requirement) {
		if (content == null || content.isBlank() || source == null || !source.isAbsolute()) {
			ETSAssert.failWithUri(requirement, "API definition content and absolute source URI are required.");
		}
		try {
			URL sourceUrl = source.toURL();
			String cacheKey = Integer.toUnsignedString(content.hashCode(), 16);
			URL parseUrl = new URL(sourceUrl.toExternalForm().replaceFirst("#.*$", "") + "#ets-" + cacheKey);
			OpenApi3 model = new OpenApi3Parser().parse(content, parseUrl, false);
			if (model == null || model.getPaths() == null) {
				ETSAssert.failWithUri(requirement, source + " did not parse as an OpenAPI 3 definition with paths.");
			}
			return new ApiDefinition(source, model);
		}
		catch (Exception ex) {
			ETSAssert.failWithUri(requirement,
					source + " could not be parsed as an OpenAPI 3 JSON/YAML definition: " + ex.getMessage());
			return null;
		}
	}

	/**
	 * Checks every supported canonical resource GET and the custom collection GET.
	 * @param definition parsed API definition.
	 * @param supported supported canonical resource types.
	 * @param customCollections whether custom collections are advertised.
	 * @param requirement requirement owning the assertion.
	 */
	public static void assertReadMediaAdvertisements(ApiDefinition definition, Set<ResourceType> supported,
			boolean customCollections, String requirement) {
		if (definition == null || definition.model() == null) {
			ETSAssert.failWithUri(requirement, "A parsed API definition is required.");
		}
		for (ResourceType resourceType : supported == null ? Set.<ResourceType>of() : supported) {
			assertResponseMedia(definition, resourceType.collectionApiPath(), requirement);
		}
		if (customCollections) {
			assertResponseMedia(definition, "/collections/{collectionId}/items", requirement);
		}
	}

	/**
	 * Checks for GeoJSON request content on at least one canonical CREATE or REPLACE
	 * operation.
	 * @param definition parsed API definition.
	 * @param requirement requirement owning the assertion.
	 */
	public static void assertWriteMediaAdvertisement(ApiDefinition definition, String requirement) {
		for (ResourceType resourceType : ResourceType.values()) {
			Path collection = matchingPath(definition, resourceType.collectionApiPath());
			if (hasRequestMedia(collection == null ? null : collection.getPost())) {
				return;
			}
			Path item = matchingPath(definition, resourceType.itemApiPath());
			if (hasRequestMedia(item == null ? null : item.getPut())) {
				return;
			}
		}
		ETSAssert.failWithUri(requirement, definition.source()
				+ " does not advertise Content-Type application/geo+json for POST on a canonical collection or PUT on a canonical item.");
	}

	public static void validateSingle(Map<String, Object> document, ResourceType resourceType, String requirement,
			String source) {
		validate(document, resourceType.singleSchema(), resourceType, requirement, source);
	}

	public static void validateCollection(Map<String, Object> document, ResourceType resourceType, String requirement,
			String source) {
		validate(document, resourceType.collectionSchema(), resourceType, requirement, source);
	}

	/**
	 * Validates the common GeoJSON Feature attribute mappings.
	 */
	public static void validateCommonFeature(Map<String, Object> feature, String requirement, String source) {
		Map<String, Object> properties = properties(feature, requirement, source);
		Object uid = properties.get("uid");
		if (!(uid instanceof String) || !isAbsoluteUri((String) uid)) {
			ETSAssert.failWithUri(requirement, source + " properties.uid must be a valid absolute URI.");
		}
		assertOptionalString(properties, "name", requirement, source);
		assertOptionalString(properties, "description", requirement, source);
	}

	/**
	 * Validates resource-specific attribute and embedded-association mappings.
	 */
	public static void validateResourceMappings(Map<String, Object> feature, ResourceType resourceType,
			String requirement, String source) {
		Map<String, Object> properties = properties(feature, requirement, source);
		assertNonBlankString(properties, "featureType", requirement, source);
		if (!isUriOrCurie((String) properties.get("featureType"))) {
			ETSAssert.failWithUri(requirement, source + " properties.featureType must be a URI or CURIE.");
		}
		switch (resourceType) {
			case SYSTEM -> {
				Object assetType = properties.get("assetType");
				if (assetType != null && (!(assetType instanceof String) || !SYSTEM_ASSET_TYPES.contains(assetType))) {
					ETSAssert.failWithUri(requirement,
							source + " properties.assetType is not a released System asset type.");
				}
				assertValidTime(properties, requirement, source);
				assertPointGeometry(feature, requirement, source);
				assertOptionalLink(properties, "systemKind@link", false, requirement, source);
			}
			case DEPLOYMENT -> {
				assertValidTime(properties, requirement, source);
				assertOptionalLink(properties, "platform@link", false, requirement, source);
				assertOptionalLink(properties, "deployedSystems@link", true, requirement, source);
			}
			case PROCEDURE -> assertValidTime(properties, requirement, source);
			case SAMPLING_FEATURE -> {
				assertValidTime(properties, requirement, source);
				assertOptionalLink(properties, "sampledFeature@link", false, requirement, source);
			}
		}
	}

	/**
	 * Validates links-member relation names and returns association evidence count.
	 */
	public static int validateRelationTypes(Map<String, Object> feature, ResourceType resourceType, String requirement,
			String source) {
		Object links = feature == null ? null : feature.get("links");
		if (links == null) {
			return 0;
		}
		if (!(links instanceof List<?>)) {
			ETSAssert.failWithUri(requirement, source + " links must be a JSON array.");
		}
		int associations = 0;
		for (Object value : (List<?>) links) {
			if (!(value instanceof Map<?, ?>)) {
				ETSAssert.failWithUri(requirement, source + " links[] entry must be a JSON object.");
			}
			Map<?, ?> link = (Map<?, ?>) value;
			Object relValue = link.get("rel");
			if (!(relValue instanceof String) || ((String) relValue).isBlank()) {
				ETSAssert.failWithUri(requirement, source + " links[] entry has no non-empty rel.");
			}
			String rel = (String) relValue;
			if (EncodingRelationTypes.isGenericRel(rel)) {
				continue;
			}
			if (!EncodingRelationTypes.isAllowedAssociationRel(EncodingRelationTypes.ENCODING_GEOJSON,
					resourceType.relationType(), rel)) {
				ETSAssert.failWithUri(requirement,
						source + " relation '" + rel + "' is not valid for " + resourceType + ".");
			}
			assertLink(link, requirement, source + " links[rel=" + rel + "]");
			associations++;
		}
		return associations;
	}

	private static void assertResponseMedia(ApiDefinition definition, String expectedPath, String requirement) {
		Path path = matchingPath(definition, expectedPath);
		Operation get = path == null ? null : path.getGet();
		boolean advertised = get != null && get.getResponses() != null
				&& get.getResponses()
					.entrySet()
					.stream()
					.filter(entry -> isSuccessResponse(entry.getKey()))
					.map(Map.Entry::getValue)
					.anyMatch(GeoJsonSupport::hasResponseMedia);
		if (!advertised) {
			ETSAssert.failWithUri(requirement,
					definition.source() + " does not advertise application/geo+json for GET " + expectedPath + ".");
		}
	}

	private static Path matchingPath(ApiDefinition definition, String expectedPath) {
		if (definition == null || definition.model() == null || definition.model().getPaths() == null) {
			return null;
		}
		Path exact = definition.model().getPath(expectedPath);
		if (exact != null) {
			return exact;
		}
		String expected = normalizedTemplate(expectedPath);
		return definition.model()
			.getPaths()
			.entrySet()
			.stream()
			.filter(entry -> normalizedTemplate(entry.getKey()).endsWith(expected))
			.map(Map.Entry::getValue)
			.findFirst()
			.orElse(null);
	}

	private static String normalizedTemplate(String path) {
		return path == null ? "" : path.replaceAll("\\{[^/]+}", "{}");
	}

	private static boolean isSuccessResponse(String status) {
		return status != null
				&& (status.matches("2\\d\\d") || "2XX".equalsIgnoreCase(status) || "default".equalsIgnoreCase(status));
	}

	private static boolean hasResponseMedia(Response response) {
		return response != null && response.hasContentMediaType(MEDIA_TYPE);
	}

	private static boolean hasRequestMedia(Operation operation) {
		RequestBody requestBody = operation == null ? null : operation.getRequestBody();
		return requestBody != null && requestBody.hasContentMediaType(MEDIA_TYPE);
	}

	private static void validate(Map<String, Object> document, String schemaLocation, ResourceType resourceType,
			String requirement, String source) {
		try {
			JsonSchema schema = SCHEMA_FACTORY.getSchema(SchemaLocation.of(schemaLocation), schemaConfig());
			Set<ValidationMessage> errors = schema.validate(JSON.valueToTree(document));
			if (!errors.isEmpty()) {
				String joined = errors.stream()
					.limit(8)
					.map(ValidationMessage::getMessage)
					.collect(Collectors.joining("; "));
				ETSAssert.failWithUri(requirement,
						source + " failed the released " + resourceType + " GeoJSON schema: " + joined);
			}
		}
		catch (AssertionError ex) {
			throw ex;
		}
		catch (RuntimeException ex) {
			ETSAssert.failWithUri(requirement,
					source + " could not be validated against " + schemaLocation + ": " + ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> properties(Map<String, Object> feature, String requirement, String source) {
		Object properties = feature == null ? null : feature.get("properties");
		if (!(properties instanceof Map<?, ?>)) {
			ETSAssert.failWithUri(requirement, source + " must contain a properties object.");
		}
		return (Map<String, Object>) properties;
	}

	private static void assertOptionalString(Map<String, Object> properties, String name, String requirement,
			String source) {
		Object value = properties.get(name);
		if (value != null && !(value instanceof String)) {
			ETSAssert.failWithUri(requirement, source + " properties." + name + " must be a JSON string.");
		}
	}

	private static void assertNonBlankString(Map<String, Object> properties, String name, String requirement,
			String source) {
		Object value = properties.get(name);
		if (!(value instanceof String) || ((String) value).isBlank()) {
			ETSAssert.failWithUri(requirement, source + " properties." + name + " must be a non-empty JSON string.");
		}
	}

	private static void assertValidTime(Map<String, Object> properties, String requirement, String source) {
		Object value = properties.get("validTime");
		if (value == null) {
			return;
		}
		if (!(value instanceof List<?>) || ((List<?>) value).size() != 2) {
			ETSAssert.failWithUri(requirement,
					source + " properties.validTime must be a two-bound JSON array when present.");
		}
		for (Object bound : (List<?>) value) {
			if (!(bound instanceof String) || !isIsoDateTime((String) bound)) {
				ETSAssert.failWithUri(requirement,
						source + " properties.validTime bounds must be ISO 8601 date/time strings.");
			}
		}
	}

	private static void assertPointGeometry(Map<String, Object> feature, String requirement, String source) {
		Object geometry = feature == null ? null : feature.get("geometry");
		if (geometry == null) {
			return;
		}
		if (!(geometry instanceof Map<?, ?>) || !"Point".equals(((Map<?, ?>) geometry).get("type"))) {
			ETSAssert.failWithUri(requirement, source + " System geometry must be a GeoJSON Point when present.");
		}
	}

	private static void assertOptionalLink(Map<String, Object> properties, String name, boolean array,
			String requirement, String source) {
		Object value = properties.get(name);
		if (value == null) {
			return;
		}
		if (array) {
			if (!(value instanceof List<?>)) {
				ETSAssert.failWithUri(requirement, source + " properties." + name + " must be an array of links.");
			}
			for (Object link : (List<?>) value) {
				if (!(link instanceof Map<?, ?>)) {
					ETSAssert.failWithUri(requirement,
							source + " properties." + name + " entries must be link objects.");
				}
				assertLink((Map<?, ?>) link, requirement, source + " properties." + name);
			}
		}
		else {
			if (!(value instanceof Map<?, ?>)) {
				ETSAssert.failWithUri(requirement, source + " properties." + name + " must be a link object.");
			}
			assertLink((Map<?, ?>) value, requirement, source + " properties." + name);
		}
	}

	private static void assertLink(Map<?, ?> link, String requirement, String source) {
		Object href = link.get("href");
		if (!(href instanceof String) || !isAbsoluteUri((String) href)) {
			ETSAssert.failWithUri(requirement, source + " must contain an absolute URI href.");
		}
	}

	private static boolean isAbsoluteUri(String value) {
		try {
			URI uri = URI.create(value);
			return uri.isAbsolute() && uri.getSchemeSpecificPart() != null && !uri.getSchemeSpecificPart().isBlank();
		}
		catch (IllegalArgumentException ex) {
			return false;
		}
	}

	private static boolean isUriOrCurie(String value) {
		if (isAbsoluteUri(value)) {
			return true;
		}
		int delimiter = value == null ? -1 : value.indexOf(':');
		return delimiter > 0 && delimiter < value.length() - 1
				&& value.substring(0, delimiter).matches("[A-Za-z][\\w.-]*")
				&& !value.substring(delimiter + 1).isBlank();
	}

	private static boolean isIsoDateTime(String value) {
		try {
			DateTimeFormatter.ISO_DATE_TIME.parse(value);
			return true;
		}
		catch (DateTimeParseException ex) {
			return false;
		}
	}

	private static SchemaValidatorsConfig schemaConfig() {
		SchemaValidatorsConfig config = new SchemaValidatorsConfig();
		config.setFormatAssertionsEnabled(true);
		return config;
	}

}
