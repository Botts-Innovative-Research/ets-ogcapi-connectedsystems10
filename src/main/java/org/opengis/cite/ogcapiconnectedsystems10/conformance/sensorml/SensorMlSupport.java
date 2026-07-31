package org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.EncodingRelationTypes;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.geojson.GeoJsonSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.Part2SchemaValidation;
import org.opengis.cite.ogcapiconnectedsystems10.validation.sensorml.ConnectedSystemsSensorMlValidatorAdapter;
import org.opengis.cite.ogcapiconnectedsystems10.validation.sensorml.SensorMlSchema;
import org.opengis.cite.ogcapiconnectedsystems10.validation.sensorml.SensorMlValidationResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

/**
 * Released SensorML schema, API-definition, class, and mapping assertions.
 */
public final class SensorMlSupport {

	static final String MEDIA_TYPE = "application/sml+json";

	private static final String GEOJSON_MEDIA_TYPE = "application/geo+json";

	private static final ObjectMapper JSON = new ObjectMapper();

	private static final ConnectedSystemsSensorMlValidatorAdapter VALIDATOR = new ConnectedSystemsSensorMlValidatorAdapter();

	private static final String PART2_SCHEMA_PREFIX = "https://csapi-compliance.local/schemas/connected-systems-2/json/";

	private static final JsonSchemaFactory PART2_SCHEMA_FACTORY = JsonSchemaFactory
		.getInstance(SpecVersion.VersionFlag.V202012, builder -> builder.schemaMappers(
				mappers -> mappers.mapPrefix("https://csapi-compliance.local/schemas/", "classpath:schemas/")));

	private static final Set<String> SYSTEM_TYPES = Set.of("http://www.w3.org/ns/sosa/Sensor",
			"http://www.w3.org/ns/sosa/Actuator", "http://www.w3.org/ns/sosa/Platform",
			"http://www.w3.org/ns/sosa/Sampler", "http://www.w3.org/ns/sosa/System", "sosa:Sensor", "sosa:Actuator",
			"sosa:Platform", "sosa:Sampler", "sosa:System");

	private static final Set<String> PROCEDURE_TYPES = Set.of("http://www.w3.org/ns/sosa/Procedure",
			"http://www.w3.org/ns/sosa/ObservingProcedure", "http://www.w3.org/ns/sosa/SamplingProcedure",
			"http://www.w3.org/ns/sosa/ActuatingProcedure", "http://www.w3.org/ns/sosa/System",
			"http://www.w3.org/ns/sosa/Sensor", "http://www.w3.org/ns/sosa/Actuator",
			"http://www.w3.org/ns/sosa/Sampler", "http://www.w3.org/ns/sosa/Platform", "sosa:Procedure",
			"sosa:ObservingProcedure", "sosa:SamplingProcedure", "sosa:ActuatingProcedure", "sosa:System",
			"sosa:Sensor", "sosa:Actuator", "sosa:Sampler", "sosa:Platform");

	private static final Set<String> PHYSICAL_PROCEDURE_TYPES = Set.of("http://www.w3.org/ns/sosa/System",
			"http://www.w3.org/ns/sosa/Sensor", "http://www.w3.org/ns/sosa/Actuator",
			"http://www.w3.org/ns/sosa/Sampler", "http://www.w3.org/ns/sosa/Platform", "sosa:System", "sosa:Sensor",
			"sosa:Actuator", "sosa:Sampler", "sosa:Platform");

	private static final Set<String> PHYSICAL_CLASSES = Set.of("PhysicalComponent", "PhysicalSystem");

	private static final Set<String> PROCESS_CLASSES = Set.of("SimpleProcess", "AggregateProcess");

	private static final Set<String> ASSET_TYPES = Set.of("Equipment", "Human", "LivingThing", "Simulation", "Process",
			"Group", "Other");

	private SensorMlSupport() {
	}

	/**
	 * SensorML resource-specific paths, schemas, and conformance declarations.
	 */
	public enum ResourceType {

		SYSTEM("systems", SensorMlSchema.SYSTEM, SensorMlSchema.SYSTEM_COLLECTION,
				"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/system",
				Set.of("subsystems", "samplingFeatures", "deployments", "procedures", "datastreams", "controlstreams")),

		DEPLOYMENT("deployments", SensorMlSchema.DEPLOYMENT, SensorMlSchema.DEPLOYMENT_COLLECTION,
				"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/deployment",
				Set.of("parentDeployment", "subdeployments", "featuresOfInterest", "samplingFeatures", "datastreams",
						"controlstreams")),

		PROCEDURE("procedures", SensorMlSchema.PROCEDURE, SensorMlSchema.PROCEDURE_COLLECTION,
				"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/procedure",
				Set.of("implementingSystems")),

		PROPERTY("properties", SensorMlSchema.PROPERTY, SensorMlSchema.PROPERTY_COLLECTION,
				"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/property", Set.of());

		private final String collectionPath;

		private final SensorMlSchema singleSchema;

		private final SensorMlSchema collectionSchema;

		private final String conformanceUri;

		private final Set<String> associationNames;

		ResourceType(String collectionPath, SensorMlSchema singleSchema, SensorMlSchema collectionSchema,
				String conformanceUri, Set<String> associationNames) {
			this.collectionPath = collectionPath;
			this.singleSchema = singleSchema;
			this.collectionSchema = collectionSchema;
			this.conformanceUri = conformanceUri;
			this.associationNames = associationNames;
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

		SensorMlSchema singleSchema() {
			return this.singleSchema;
		}

		SensorMlSchema collectionSchema() {
			return this.collectionSchema;
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
	public record ApiDefinition(URI source, OpenAPI model, List<String> diagnostics) {

		public ApiDefinition {
			diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
		}
	}

	enum AssociationTargetKind {

		SYSTEM_RESOURCE(true, Set.of("systems"), ResourceType.SYSTEM, GeoJsonSupport.ResourceType.SYSTEM, null),

		PROCEDURE_RESOURCE(true, Set.of("procedures"), ResourceType.PROCEDURE, GeoJsonSupport.ResourceType.PROCEDURE,
				null),

		DEPLOYMENT_RESOURCE(true, Set.of("deployments"), ResourceType.DEPLOYMENT,
				GeoJsonSupport.ResourceType.DEPLOYMENT, null),

		SYSTEMS_ENDPOINT(false, Set.of("systems", "subsystems"), ResourceType.SYSTEM,
				GeoJsonSupport.ResourceType.SYSTEM, null),

		SAMPLING_FEATURES_ENDPOINT(false, Set.of("samplingFeatures"), null,
				GeoJsonSupport.ResourceType.SAMPLING_FEATURE, null),

		DEPLOYMENTS_ENDPOINT(false, Set.of("deployments", "subdeployments"), ResourceType.DEPLOYMENT,
				GeoJsonSupport.ResourceType.DEPLOYMENT, null),

		PROCEDURES_ENDPOINT(false, Set.of("procedures"), ResourceType.PROCEDURE, GeoJsonSupport.ResourceType.PROCEDURE,
				null),

		FEATURES_ENDPOINT(false, Set.of("featuresOfInterest", "samplingFeatures"), null,
				GeoJsonSupport.ResourceType.SAMPLING_FEATURE, null),

		DATASTREAMS_ENDPOINT(false, Set.of("datastreams"), null, null, "dataStreamCollection.json"),

		CONTROLSTREAMS_ENDPOINT(false, Set.of("controlstreams"), null, null, "controlStreamCollection.json");

		private final boolean resource;

		private final Set<String> terminalSegments;

		private final ResourceType sensorMlType;

		private final GeoJsonSupport.ResourceType geoJsonType;

		private final String part2Schema;

		AssociationTargetKind(boolean resource, Set<String> terminalSegments, ResourceType sensorMlType,
				GeoJsonSupport.ResourceType geoJsonType, String part2Schema) {
			this.resource = resource;
			this.terminalSegments = terminalSegments;
			this.sensorMlType = sensorMlType;
			this.geoJsonType = geoJsonType;
			this.part2Schema = part2Schema;
		}

		boolean resource() {
			return this.resource;
		}

	}

	record AssociationTarget(String association, URI target, AssociationTargetKind kind) {
	}

	/**
	 * Validates a dereferenced association against its exact expected representation
	 * type.
	 */
	static void validateAssociationRepresentation(Map<String, Object> document, String mediaType,
			AssociationTarget target, String requirement) {
		if (document == null || target == null || target.kind() == null) {
			ETSAssert.failWithUri(requirement, "Association representation and target metadata are required.");
		}
		AssociationTargetKind kind = target.kind();
		String source = target.target().toString();
		if (MEDIA_TYPE.equals(mediaType) && kind.sensorMlType != null) {
			if (kind.resource) {
				validateResourceMappings(document, kind.sensorMlType, requirement, source);
			}
			else {
				validateSchema(document, kind.sensorMlType.collectionSchema(), kind.sensorMlType, requirement, source);
				validateSensorMlAssociationItems(document, kind.sensorMlType, requirement, source);
			}
			return;
		}
		if (GEOJSON_MEDIA_TYPE.equals(mediaType) && kind.geoJsonType != null) {
			if (kind.resource) {
				GeoJsonSupport.validateSingle(document, kind.geoJsonType, requirement, source);
				GeoJsonSupport.validateResourceMappings(document, kind.geoJsonType, requirement, source);
			}
			else {
				GeoJsonSupport.validateCollection(document, kind.geoJsonType, requirement, source);
				validateGeoJsonAssociationFeatures(document, kind.geoJsonType, requirement, source);
			}
			return;
		}
		if (isJsonMediaType(mediaType) && kind.part2Schema != null) {
			validatePart2AssociationCollection(document, kind.part2Schema, requirement, source);
			return;
		}
		ETSAssert.failWithUri(requirement, target.association() + " association target returned media type '"
				+ mediaType + "' that cannot prove the required " + kind + " representation: " + source);
	}

	/**
	 * Parses either JSON or YAML OpenAPI 3 content.
	 */
	public static ApiDefinition parseApiDefinition(String content, URI source, String requirement) {
		if (content == null || content.isBlank() || source == null || !source.isAbsolute()) {
			ETSAssert.failWithUri(requirement, "API definition content and absolute source URI are required.");
		}
		try {
			ParseOptions options = new ParseOptions();
			options.setResolve(true);
			options.setResolveFully(true);
			options.setSafelyResolveURL(true);
			options.setRemoteRefAllowList(List.of(parserSourceAllowPattern(source, requirement)));
			SwaggerParseResult result = new OpenAPIV3Parser().readContents(content, List.of(), options,
					source.toString());
			OpenAPI model = result.getOpenAPI();
			if (model == null || model.getPaths() == null) {
				ETSAssert.failWithUri(requirement, source + " did not parse as an OpenAPI 3 definition with paths"
						+ parserDiagnostics(result) + ".");
			}
			return new ApiDefinition(source, model, result.getMessages());
		}
		catch (Exception ex) {
			ETSAssert.failWithUri(requirement,
					source + " could not be parsed as an OpenAPI 3 JSON/YAML definition: " + ex.getMessage());
			return null;
		}
	}

	private static String parserSourceAllowPattern(URI source, String requirement) {
		String scheme = source.getScheme() == null ? "" : source.getScheme().toLowerCase(Locale.ROOT);
		String host = source.getHost();
		if (!Set.of("http", "https").contains(scheme) || host == null || host.isBlank() || host.contains(":")
				|| source.getUserInfo() != null) {
			ETSAssert.failWithUri(requirement,
					"API definition source must be an HTTP(S) URI with a supported host and no userinfo: " + source);
		}
		int port = source.getPort() >= 0 ? source.getPort() : "https".equals(scheme) ? 443 : 80;
		return host + ":" + port;
	}

	/**
	 * Checks every supported canonical resource GET and optional custom collection GET.
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
	 * Checks for SensorML request content on a canonical CREATE or REPLACE operation.
	 */
	public static void assertWriteMediaAdvertisement(ApiDefinition definition, String requirement) {
		for (ResourceType resourceType : ResourceType.values()) {
			PathItem collection = matchingPath(definition, resourceType.collectionApiPath());
			if (hasRequestMedia(collection == null ? null : collection.getPost())) {
				return;
			}
			PathItem item = matchingPath(definition, resourceType.itemApiPath());
			if (hasRequestMedia(item == null ? null : item.getPut())) {
				return;
			}
		}
		ETSAssert.failWithUri(requirement, definition.source()
				+ " does not advertise Content-Type application/sml+json for POST on a canonical collection or PUT on a canonical item.");
	}

	/**
	 * Validates one of the eight released schema targets through the adapter boundary.
	 */
	public static void validateSchema(Map<String, Object> document, SensorMlSchema schema, ResourceType resourceType,
			String requirement, String source) {
		SensorMlValidationResult result = VALIDATOR.validate(JSON.valueToTree(document), schema);
		if (!result.valid()) {
			ETSAssert.failWithUri(requirement, source + " failed the released " + resourceType + " SensorML schema: "
					+ String.join("; ", result.diagnostics().stream().limit(8).toList()));
		}
	}

	/**
	 * Validates exact equality between a representation id and canonical URL id.
	 */
	public static void validateResourceId(Map<String, Object> document, String expectedId, String requirement,
			String source) {
		Object id = document == null ? null : document.get("id");
		if (!(id instanceof String) || !id.equals(expectedId)) {
			ETSAssert.failWithUri(requirement,
					source + " id must exactly equal canonical URL id '" + expectedId + "'; found " + id + ".");
		}
	}

	/**
	 * Validates common SensorML Feature attribute mappings.
	 */
	public static void validateCommonFeature(Map<String, Object> document, String requirement, String source) {
		if (document == null) {
			ETSAssert.failWithUri(requirement, source + " must be a JSON object.");
		}
		Object uniqueId = document.get("uniqueId");
		if (uniqueId != null && (!(uniqueId instanceof String) || !isAbsoluteUri((String) uniqueId))) {
			ETSAssert.failWithUri(requirement, source + " uniqueId must be a valid URI when present.");
		}
		assertOptionalString(document, "label", requirement, source);
		assertOptionalString(document, "description", requirement, source);
	}

	/**
	 * Validates the System SensorML class when asset-type evidence is unambiguous.
	 * @return true when a released asset-type/class pairing was inspected.
	 */
	public static boolean validateSystemClass(Map<String, Object> document, String requirement, String source) {
		String smlClass = requiredString(document, "type", requirement, source);
		if (!PHYSICAL_CLASSES.contains(smlClass) && !PROCESS_CLASSES.contains(smlClass)) {
			ETSAssert.failWithUri(requirement, source + " uses unsupported SensorML class '" + smlClass + "'.");
		}
		String assetType = assetType(document, requirement, source);
		if (assetType == null || Set.of("LivingThing", "Group", "Other").contains(assetType)) {
			return false;
		}
		boolean compatible = Set.of("Equipment", "Human").contains(assetType) ? PHYSICAL_CLASSES.contains(smlClass)
				: PROCESS_CLASSES.contains(smlClass);
		if (!compatible) {
			ETSAssert.failWithUri(requirement,
					source + " SensorML class '" + smlClass + "' is incompatible with asset type " + assetType + ".");
		}
		return true;
	}

	/**
	 * Validates Procedure class compatibility and absence of position information.
	 * @return true when procedure-type evidence determined the expected class.
	 */
	public static boolean validateProcedureClass(Map<String, Object> document, String requirement, String source) {
		if (document != null && document.containsKey("position")) {
			ETSAssert.failWithUri(requirement, source + " Procedure must not provide position information.");
		}
		String smlClass = requiredString(document, "type", requirement, source);
		String definition = requiredString(document, "definition", requirement, source);
		if (!PROCEDURE_TYPES.contains(definition)) {
			ETSAssert.failWithUri(requirement, source + " definition is not a released Procedure type URI or CURIE.");
		}
		boolean compatible = PHYSICAL_PROCEDURE_TYPES.contains(definition) ? PHYSICAL_CLASSES.contains(smlClass)
				: PROCESS_CLASSES.contains(smlClass);
		if (!compatible) {
			ETSAssert.failWithUri(requirement,
					source + " SensorML class '" + smlClass + "' is incompatible with " + definition + ".");
		}
		return true;
	}

	/**
	 * Validates resource-specific attribute and embedded-association mappings.
	 */
	public static void validateResourceMappings(Map<String, Object> document, ResourceType resourceType,
			String requirement, String source) {
		validateSchema(document, resourceType.singleSchema(), resourceType, requirement, source);
		switch (resourceType) {
			case SYSTEM -> validateSystemMappings(document, requirement, source);
			case DEPLOYMENT -> validateDeploymentMappings(document, requirement, source);
			case PROCEDURE -> validateProcedureMappings(document, requirement, source);
			case PROPERTY -> validatePropertyMappings(document, requirement, source);
		}
	}

	/**
	 * Resolves and validates every released association target carried by a resource.
	 */
	static List<AssociationTarget> associationTargets(Map<String, Object> document, ResourceType resourceType,
			URI source, URI apiRoot, String requirement) {
		if (document == null || source == null || apiRoot == null) {
			ETSAssert.failWithUri(requirement, "Association validation requires a resource, source URI, and API root.");
		}
		List<AssociationTarget> targets = new ArrayList<>();
		switch (resourceType) {
			case SYSTEM -> {
				addMemberTarget(targets, document, "typeOf", AssociationTargetKind.PROCEDURE_RESOURCE, source, apiRoot,
						requirement);
				addMemberTarget(targets, document, "attachedTo", AssociationTargetKind.SYSTEM_RESOURCE, source, apiRoot,
						requirement);
			}
			case DEPLOYMENT -> {
				addMemberTarget(targets, document, "platform", AssociationTargetKind.SYSTEM_RESOURCE, source, apiRoot,
						requirement);
				Object deployed = document.get("deployedSystems");
				if (deployed instanceof List<?> values) {
					for (Object value : values) {
						if (value instanceof Map<?, ?> deployedSystem
								&& deployedSystem.get("system") instanceof Map<?, ?> link) {
							addTarget(targets, "deployedSystems", link, AssociationTargetKind.SYSTEM_RESOURCE, source,
									apiRoot, requirement);
						}
					}
				}
			}
			case PROCEDURE, PROPERTY -> {
			}
		}
		addLinksMemberTargets(targets, document, resourceType, source, apiRoot, requirement);
		return List.copyOf(targets);
	}

	/**
	 * Validates exact {@code ogc-rel:<association>} links-member vocabulary.
	 * @return number of association links inspected.
	 */
	public static int validateRelationTypes(Map<String, Object> document, ResourceType resourceType, String requirement,
			String source) {
		Object links = document == null ? null : document.get("links");
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
			String rel = requiredString(link, "rel", requirement, source + " links[]");
			if (EncodingRelationTypes.isGenericRel(rel)) {
				assertLink(link, requirement, source + " links[rel=" + rel + "]");
				continue;
			}
			String prefix = "ogc-rel:";
			String association = rel.startsWith(prefix) ? rel.substring(prefix.length()) : "";
			if (!resourceType.associationNames.contains(association)) {
				ETSAssert.failWithUri(requirement,
						source + " relation '" + rel + "' is not valid for " + resourceType + ".");
			}
			assertLink(link, requirement, source + " links[rel=" + rel + "]");
			associations++;
		}
		return associations;
	}

	private static void validateSystemMappings(Map<String, Object> document, String requirement, String source) {
		String definition = requiredString(document, "definition", requirement, source);
		if (!SYSTEM_TYPES.contains(definition)) {
			ETSAssert.failWithUri(requirement, source + " definition is not a released System type URI or CURIE.");
		}
		assetType(document, requirement, source);
		assertValidTime(document, requirement, source);
		assertOptionalLink(document, "typeOf", false, requirement, source);
		assertOptionalLink(document, "attachedTo", false, requirement, source);
	}

	private static void validateDeploymentMappings(Map<String, Object> document, String requirement, String source) {
		String definition = requiredString(document, "definition", requirement, source);
		if (!isUriOrCurie(definition)) {
			ETSAssert.failWithUri(requirement, source + " definition must be a URI or CURIE.");
		}
		assertValidTime(document, requirement, source);
		assertOptionalLink(document, "platform", false, requirement, source);
		Object deployed = document.get("deployedSystems");
		if (deployed != null) {
			if (!(deployed instanceof List<?>)) {
				ETSAssert.failWithUri(requirement, source + " deployedSystems must be a JSON array.");
			}
			for (Object value : (List<?>) deployed) {
				if (!(value instanceof Map<?, ?>)) {
					ETSAssert.failWithUri(requirement, source + " deployedSystems[] must be an object.");
				}
				Object system = ((Map<?, ?>) value).get("system");
				if (!(system instanceof Map<?, ?>)) {
					ETSAssert.failWithUri(requirement, source + " deployedSystems[] must contain a system link.");
				}
				assertLink((Map<?, ?>) system, requirement, source + " deployedSystems[].system");
			}
		}
	}

	private static void validateProcedureMappings(Map<String, Object> document, String requirement, String source) {
		String definition = requiredString(document, "definition", requirement, source);
		if (!PROCEDURE_TYPES.contains(definition)) {
			ETSAssert.failWithUri(requirement, source + " definition is not a released Procedure type URI or CURIE.");
		}
		assertValidTime(document, requirement, source);
	}

	private static void validatePropertyMappings(Map<String, Object> document, String requirement, String source) {
		assertAbsoluteUriMember(document, "baseProperty", true, requirement, source);
		assertAbsoluteUriMember(document, "objectType", false, requirement, source);
		assertAbsoluteUriMember(document, "statistic", false, requirement, source);
	}

	private static String assetType(Map<String, Object> document, String requirement, String source) {
		Object classifiers = document == null ? null : document.get("classifiers");
		if (classifiers == null) {
			return null;
		}
		if (!(classifiers instanceof List<?>)) {
			ETSAssert.failWithUri(requirement, source + " classifiers must be a JSON array.");
		}
		String found = null;
		for (Object value : (List<?>) classifiers) {
			if (!(value instanceof Map<?, ?>)) {
				ETSAssert.failWithUri(requirement, source + " classifiers[] must be a JSON object.");
			}
			Map<?, ?> classifier = (Map<?, ?>) value;
			if (!"cs:AssetType".equals(classifier.get("definition"))) {
				continue;
			}
			String raw = requiredString(classifier, "value", requirement, source + " AssetType classifier");
			String normalized = normalizedAssetType(raw);
			if (!ASSET_TYPES.contains(normalized)) {
				ETSAssert.failWithUri(requirement,
						source + " AssetType classifier value is not from the released vocabulary.");
			}
			if (found != null && !found.equals(normalized)) {
				ETSAssert.failWithUri(requirement, source + " has conflicting cs:AssetType classifiers.");
			}
			found = normalized;
		}
		return found;
	}

	private static void assertResponseMedia(ApiDefinition definition, String expectedPath, String requirement) {
		PathItem path = matchingPath(definition, expectedPath);
		Operation get = path == null ? null : path.getGet();
		boolean advertised = get != null && get.getResponses() != null
				&& get.getResponses()
					.entrySet()
					.stream()
					.filter(entry -> isSuccessResponse(entry.getKey()))
					.map(Map.Entry::getValue)
					.anyMatch(SensorMlSupport::hasResponseMedia);
		if (!advertised) {
			ETSAssert.failWithUri(requirement, definition.source() + " does not advertise application/sml+json for GET "
					+ expectedPath + diagnosticSuffix(definition) + ".");
		}
	}

	private static PathItem matchingPath(ApiDefinition definition, String expectedPath) {
		if (definition == null || definition.model() == null || definition.model().getPaths() == null) {
			return null;
		}
		PathItem exact = definition.model().getPaths().get(expectedPath);
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
		return status != null && (status.matches("2\\d\\d") || "2XX".equalsIgnoreCase(status));
	}

	private static void addMemberTarget(List<AssociationTarget> targets, Map<String, Object> document, String member,
			AssociationTargetKind kind, URI source, URI apiRoot, String requirement) {
		Object value = document.get(member);
		if (value instanceof Map<?, ?> link) {
			addTarget(targets, member, link, kind, source, apiRoot, requirement);
		}
	}

	private static void addLinksMemberTargets(List<AssociationTarget> targets, Map<String, Object> document,
			ResourceType resourceType, URI source, URI apiRoot, String requirement) {
		Object value = document.get("links");
		if (!(value instanceof List<?> links)) {
			return;
		}
		for (Object entry : links) {
			if (!(entry instanceof Map<?, ?> link) || !(link.get("rel") instanceof String rel)
					|| !rel.startsWith("ogc-rel:")) {
				continue;
			}
			String association = rel.substring("ogc-rel:".length());
			AssociationTargetKind kind = associationTargetKind(resourceType, association);
			if (kind != null) {
				addTarget(targets, association, link, kind, source, apiRoot, requirement);
			}
		}
	}

	private static AssociationTargetKind associationTargetKind(ResourceType resourceType, String association) {
		return switch (resourceType) {
			case SYSTEM -> switch (association) {
				case "subsystems" -> AssociationTargetKind.SYSTEMS_ENDPOINT;
				case "samplingFeatures" -> AssociationTargetKind.SAMPLING_FEATURES_ENDPOINT;
				case "deployments" -> AssociationTargetKind.DEPLOYMENTS_ENDPOINT;
				case "procedures" -> AssociationTargetKind.PROCEDURES_ENDPOINT;
				case "datastreams" -> AssociationTargetKind.DATASTREAMS_ENDPOINT;
				case "controlstreams" -> AssociationTargetKind.CONTROLSTREAMS_ENDPOINT;
				default -> null;
			};
			case DEPLOYMENT -> switch (association) {
				case "parentDeployment" -> AssociationTargetKind.DEPLOYMENT_RESOURCE;
				case "subdeployments" -> AssociationTargetKind.DEPLOYMENTS_ENDPOINT;
				case "featuresOfInterest" -> AssociationTargetKind.FEATURES_ENDPOINT;
				case "samplingFeatures" -> AssociationTargetKind.SAMPLING_FEATURES_ENDPOINT;
				case "datastreams" -> AssociationTargetKind.DATASTREAMS_ENDPOINT;
				case "controlstreams" -> AssociationTargetKind.CONTROLSTREAMS_ENDPOINT;
				default -> null;
			};
			case PROCEDURE -> "implementingSystems".equals(association) ? AssociationTargetKind.SYSTEMS_ENDPOINT : null;
			case PROPERTY -> null;
		};
	}

	private static void addTarget(List<AssociationTarget> targets, String association, Map<?, ?> link,
			AssociationTargetKind kind, URI source, URI apiRoot, String requirement) {
		String href = requiredString(link, "href", requirement, association + " association");
		URI target;
		try {
			target = source.resolve(href).normalize();
		}
		catch (IllegalArgumentException ex) {
			ETSAssert.failWithUri(requirement, association + " association has an invalid href '" + href + "'.");
			return;
		}
		validateTargetUri(target, apiRoot, association, kind, requirement);
		targets.add(new AssociationTarget(association, target, kind));
	}

	private static void validateTargetUri(URI target, URI apiRoot, String association, AssociationTargetKind kind,
			String requirement) {
		String scheme = target.getScheme();
		if (!target.isAbsolute() || scheme == null || !Set.of("http", "https").contains(scheme.toLowerCase(Locale.ROOT))
				|| target.getHost() == null || target.getUserInfo() != null || target.getFragment() != null) {
			ETSAssert.failWithUri(requirement, association
					+ " association must resolve to an HTTP(S) URI without userinfo or a fragment: " + target);
		}
		if (!sameOrigin(apiRoot, target)) {
			return;
		}
		String rootPath = apiRoot.normalize().getPath();
		if (!rootPath.endsWith("/")) {
			rootPath += "/";
		}
		String targetPath = target.getPath();
		if (targetPath == null || !targetPath.startsWith(rootPath)
				|| target.getRawPath().toLowerCase(Locale.ROOT).contains("%2f")) {
			ETSAssert.failWithUri(requirement, association + " association target is outside the API root: " + target);
		}
		String relative = targetPath.substring(rootPath.length());
		String[] segments = relative.split("/");
		String terminal = segments.length == 0 ? "" : segments[segments.length - 1];
		boolean valid = kind.resource ? segments.length == 2 && kind.terminalSegments.contains(segments[0])
				: segments.length >= 1 && kind.terminalSegments.contains(terminal);
		if (!valid) {
			ETSAssert.failWithUri(requirement,
					association + " association does not target the required " + kind + " path: " + target);
		}
	}

	private static void validateSensorMlAssociationItems(Map<String, Object> document, ResourceType resourceType,
			String requirement, String source) {
		Object value = document.get("items");
		if (!(value instanceof List<?>)) {
			ETSAssert.failWithUri(requirement, source + " does not contain a SensorML items array.");
		}
		int index = 0;
		for (Object item : (List<?>) value) {
			validateResourceMappings(objectMap(item, requirement, source + " items[" + index + "]"), resourceType,
					requirement, source + " items[" + index + "]");
			index++;
		}
	}

	private static void validateGeoJsonAssociationFeatures(Map<String, Object> document,
			GeoJsonSupport.ResourceType resourceType, String requirement, String source) {
		Object value = document.get("features");
		if (!(value instanceof List<?>)) {
			ETSAssert.failWithUri(requirement, source + " does not contain a GeoJSON features array.");
		}
		int index = 0;
		for (Object feature : (List<?>) value) {
			GeoJsonSupport.validateResourceMappings(
					objectMap(feature, requirement, source + " features[" + index + "]"), resourceType, requirement,
					source + " features[" + index + "]");
			index++;
		}
	}

	private static void validatePart2AssociationCollection(Map<String, Object> document, String schemaFile,
			String requirement, String source) {
		JsonNode node = JSON.valueToTree(document);
		JsonSchema schema = Part2SchemaValidation.getSchema(PART2_SCHEMA_FACTORY, PART2_SCHEMA_PREFIX + schemaFile);
		Set<ValidationMessage> errors = schema.validate(node);
		if (!errors.isEmpty()) {
			String diagnostics = errors.stream()
				.map(ValidationMessage::getMessage)
				.sorted()
				.limit(8)
				.collect(Collectors.joining("; "));
			ETSAssert.failWithUri(requirement,
					source + " failed expected " + schemaFile + " association schema: " + diagnostics);
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> objectMap(Object value, String requirement, String source) {
		if (!(value instanceof Map<?, ?>)) {
			ETSAssert.failWithUri(requirement, source + " must be a JSON object.");
		}
		return (Map<String, Object>) value;
	}

	private static boolean isJsonMediaType(String mediaType) {
		return "application/json".equals(mediaType)
				|| (mediaType != null && mediaType.startsWith("application/") && mediaType.endsWith("+json"));
	}

	private static String normalizedAssetType(String raw) {
		if (ASSET_TYPES.contains(raw)) {
			return raw;
		}
		if (raw != null && raw.startsWith("cs:")) {
			return raw.substring("cs:".length());
		}
		if (!isAbsoluteUri(raw)) {
			return "";
		}
		URI uri = URI.create(raw);
		if (!Set.of("http", "https", "urn").contains(uri.getScheme().toLowerCase(Locale.ROOT))) {
			return "";
		}
		if (uri.getFragment() != null && !uri.getFragment().isBlank()) {
			return uri.getFragment();
		}
		String path = uri.getPath();
		if (path != null && !path.isBlank()) {
			return path.substring(path.lastIndexOf('/') + 1);
		}
		String schemeSpecific = uri.getSchemeSpecificPart();
		return schemeSpecific.substring(schemeSpecific.lastIndexOf(':') + 1);
	}

	private static boolean hasResponseMedia(ApiResponse response) {
		Content content = response == null ? null : response.getContent();
		return content != null && content.containsKey(MEDIA_TYPE);
	}

	private static boolean hasRequestMedia(Operation operation) {
		RequestBody requestBody = operation == null ? null : operation.getRequestBody();
		Content content = requestBody == null ? null : requestBody.getContent();
		return content != null && content.containsKey(MEDIA_TYPE);
	}

	private static String parserDiagnostics(SwaggerParseResult result) {
		List<String> diagnostics = result == null || result.getMessages() == null ? List.of() : result.getMessages();
		return diagnostics.isEmpty() ? "" : ": " + String.join("; ", diagnostics.stream().limit(8).toList());
	}

	private static String diagnosticSuffix(ApiDefinition definition) {
		return definition == null || definition.diagnostics().isEmpty() ? "" : " (parser diagnostics: "
				+ String.join("; ", definition.diagnostics().stream().limit(8).toList()) + ")";
	}

	private static void assertOptionalString(Map<String, Object> document, String name, String requirement,
			String source) {
		Object value = document.get(name);
		if (value != null && !(value instanceof String)) {
			ETSAssert.failWithUri(requirement, source + " " + name + " must be a JSON string.");
		}
	}

	private static void assertValidTime(Map<String, Object> document, String requirement, String source) {
		Object value = document.get("validTime");
		if (value == null) {
			return;
		}
		if (!(value instanceof List<?>) || ((List<?>) value).size() != 2) {
			ETSAssert.failWithUri(requirement, source + " validTime must be a two-bound JSON array when present.");
		}
		for (Object bound : (List<?>) value) {
			if (!(bound instanceof String) || !isIsoDateTime((String) bound)) {
				ETSAssert.failWithUri(requirement, source + " validTime bounds must be ISO 8601 date/time strings.");
			}
		}
	}

	private static void assertOptionalLink(Map<String, Object> document, String name, boolean array, String requirement,
			String source) {
		Object value = document.get(name);
		if (value == null) {
			return;
		}
		if (array) {
			if (!(value instanceof List<?>)) {
				ETSAssert.failWithUri(requirement, source + " " + name + " must be an array of links.");
			}
			for (Object link : (List<?>) value) {
				if (!(link instanceof Map<?, ?>)) {
					ETSAssert.failWithUri(requirement, source + " " + name + " entries must be link objects.");
				}
				assertLink((Map<?, ?>) link, requirement, source + " " + name);
			}
			return;
		}
		if (!(value instanceof Map<?, ?>)) {
			ETSAssert.failWithUri(requirement, source + " " + name + " must be a link object.");
		}
		assertLink((Map<?, ?>) value, requirement, source + " " + name);
	}

	private static void assertLink(Map<?, ?> link, String requirement, String source) {
		Object href = link.get("href");
		if (!(href instanceof String) || !isAbsoluteUri((String) href)) {
			ETSAssert.failWithUri(requirement, source + " must contain an absolute URI href.");
		}
	}

	private static void assertAbsoluteUriMember(Map<String, Object> document, String name, boolean required,
			String requirement, String source) {
		Object value = document == null ? null : document.get(name);
		if (value == null && !required) {
			return;
		}
		if (!(value instanceof String) || !isAbsoluteUri((String) value)) {
			ETSAssert.failWithUri(requirement, source + " " + name + " must be a valid URI.");
		}
	}

	private static String requiredString(Map<?, ?> document, String name, String requirement, String source) {
		Object value = document == null ? null : document.get(name);
		if (!(value instanceof String) || ((String) value).isBlank()) {
			ETSAssert.failWithUri(requirement, source + " " + name + " must be a non-empty JSON string.");
		}
		return (String) value;
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

}
