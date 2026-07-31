package org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.EncodingMediatypeWrite;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.TraversalResult;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonTests;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml.SensorMlSupport.ApiDefinition;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml.SensorMlSupport.ResourceType;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * Released OGC 23-001 Annex A procedures for the SensorML conformance class.
 */
public class SensorMlTests {

	static final String GROUP = "sensorml";

	static final String CONF_SENSORML = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/sensorml";

	static final String REQ_MEDIATYPE_READ = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/mediatype-read";

	static final String REQ_MEDIATYPE_WRITE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/mediatype-write";

	static final String REQ_RELATION_TYPES = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/relation-types";

	static final String REQ_RESOURCE_ID = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/resource-id";

	static final String REQ_FEATURE_ATTRIBUTE_MAPPING = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/feature-attribute-mapping";

	static final String REQ_SYSTEM_SCHEMA = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/system-schema";

	static final String REQ_SYSTEM_CLASS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/system-sml-class";

	static final String REQ_SYSTEM_MAPPINGS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/system-mappings";

	static final String REQ_DEPLOYMENT_SCHEMA = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/deployment-schema";

	static final String REQ_DEPLOYMENT_MAPPINGS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/deployment-mappings";

	static final String REQ_PROCEDURE_SCHEMA = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/procedure-schema";

	static final String REQ_PROCEDURE_CLASS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/procedure-sml-class";

	static final String REQ_PROCEDURE_MAPPINGS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/procedure-mappings";

	static final String REQ_PROPERTY_SCHEMA = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/property-schema";

	static final String REQ_PROPERTY_MAPPINGS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/property-mappings";

	private static final String DATETIME_EVIDENCE_METHOD = "datetimeUsesValidTime";

	private URI apiRoot;

	/**
	 * Loads only the immutable API root after inherited API Common prerequisites.
	 */
	@BeforeClass(dependsOnGroups = "part1apicommon", alwaysRun = true)
	public void fetchSensorMlArguments(ITestContext testContext) {
		skipWhenPrerequisiteUnsatisfied(testContext);
		Object iut = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iut instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		configure((URI) iut);
	}

	void configure(URI iut) {
		if (iut == null || !iut.isAbsolute()) {
			throw new IllegalArgumentException("IUT must be an absolute URI.");
		}
		String value = iut.toString();
		this.apiRoot = URI.create(value.endsWith("/") ? value : value + "/");
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-MEDIA-ADVERTISEMENT-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_MEDIATYPE_READ
			+ ": API definition advertises application/sml+json for every supported canonical and advertised custom collection GET",
			groups = GROUP, alwaysRun = true)
	public void sensorMlMediaTypeReadIsAdvertised() {
		Set<ResourceType> supported = declaredResourceTypes(REQ_MEDIATYPE_READ);
		SensorMlSupport.assertReadMediaAdvertisements(apiDefinition(REQ_MEDIATYPE_READ), supported,
				customCollectionsAdvertised(), REQ_MEDIATYPE_READ);
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-MEDIA-ADVERTISEMENT-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_MEDIATYPE_WRITE
			+ ": API definition advertises application/sml+json on at least one canonical CREATE or REPLACE operation",
			groups = GROUP, alwaysRun = true)
	public void sensorMlMediaTypeWriteIsAdvertised() {
		List<String> declarations = conformanceDeclarations(REQ_MEDIATYPE_WRITE);
		requireSensorMlDeclaration(declarations, REQ_MEDIATYPE_WRITE);
		if (!declarations.contains(EncodingMediatypeWrite.CONF_CREATE_REPLACE_DELETE)) {
			throw new SkipException(REQ_MEDIATYPE_WRITE + " - IUT does not declare "
					+ EncodingMediatypeWrite.CONF_CREATE_REPLACE_DELETE + ".");
		}
		SensorMlSupport.assertWriteMediaAdvertisement(apiDefinition(REQ_MEDIATYPE_WRITE), REQ_MEDIATYPE_WRITE);
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-RELATION-TYPES-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RELATION_TYPES
			+ ": every available SensorML links-member association uses exact ogc-rel-prefixed resource vocabulary",
			groups = GROUP, alwaysRun = true)
	public void sensorMlAssociationRelationTypesAreValid() {
		int[] associations = { 0 };
		Inspection inspection = inspectAllTypes(REQ_RELATION_TYPES,
				(type, document) -> associations[0] += SensorMlSupport.validateRelationTypes(document, type,
						REQ_RELATION_TYPES, type.collectionPath()));
		if (associations[0] == 0) {
			inspection.limitations().add("no links-member association was available after generic links were excluded");
		}
		inspection.finish(REQ_RELATION_TYPES);
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-RESOURCE-ID-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RESOURCE_ID
			+ ": every available canonical SensorML representation id exactly equals its URL id", groups = GROUP,
			alwaysRun = true)
	public void sensorMlResourceIdsMatchCanonicalUrls() {
		Set<ResourceType> supported = declaredResourceTypes(REQ_RESOURCE_ID);
		Inspection inspection = new Inspection();
		for (ResourceType type : supported) {
			try {
				TraversalResult traversal = traverse(type, REQ_RESOURCE_ID);
				if (traversal.items().isEmpty()) {
					inspection.limitations().add(type.collectionPath() + " returned no resource");
					continue;
				}
				for (Map<String, Object> item : traversal.items()) {
					String id = requiredItemId(item, type, REQ_RESOURCE_ID);
					URI source = singleResourceUri(type, id);
					Map<String, Object> single = sensorMlObject(
							getExpected200(source, SensorMlSupport.MEDIA_TYPE, REQ_RESOURCE_ID), source,
							REQ_RESOURCE_ID);
					SensorMlSupport.validateResourceId(single, id, REQ_RESOURCE_ID, source.toString());
					inspection.inspected++;
				}
			}
			catch (SkipException ex) {
				inspection.limitations().add(message(ex));
			}
		}
		if (inspection.inspected == 0) {
			inspection.limitations().add("no canonical SensorML single-resource representation was available");
		}
		inspection.finish(REQ_RESOURCE_ID);
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-COMMON-MAPPINGS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_FEATURE_ATTRIBUTE_MAPPING
			+ ": every available SensorML resource uses released uniqueId, label, and description mappings",
			groups = GROUP, alwaysRun = true)
	public void sensorMlCommonFeatureAttributesAreMapped() {
		inspectAllTypes(REQ_FEATURE_ATTRIBUTE_MAPPING, (type, document) -> SensorMlSupport
			.validateCommonFeature(document, REQ_FEATURE_ATTRIBUTE_MAPPING, type.collectionPath()))
			.finish(REQ_FEATURE_ATTRIBUTE_MAPPING);
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-SCHEMAS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_SYSTEM_SCHEMA
			+ ": canonical System single and collection SensorML documents validate against released schemas",
			groups = GROUP, alwaysRun = true)
	public void systemSensorMlSchemasAreValid() {
		validateSchemas(ResourceType.SYSTEM, REQ_SYSTEM_SCHEMA);
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-CLASS-COMPATIBILITY-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_SYSTEM_CLASS
			+ ": every available System SensorML class is compatible with unambiguous asset-type evidence",
			groups = GROUP, alwaysRun = true)
	public void systemSensorMlClassesAreCompatible() {
		requireResourceDeclaration(ResourceType.SYSTEM, REQ_SYSTEM_CLASS);
		TraversalResult traversal = traverse(ResourceType.SYSTEM, REQ_SYSTEM_CLASS);
		if (traversal.items().isEmpty()) {
			throw new SkipException(REQ_SYSTEM_CLASS + " - systems returned no resource to inspect.");
		}
		List<String> limitations = new ArrayList<>();
		for (Map<String, Object> system : traversal.items()) {
			if (!SensorMlSupport.validateSystemClass(system, REQ_SYSTEM_CLASS, ResourceType.SYSTEM.collectionPath())) {
				limitations.add("System " + system.get("id") + " has no unambiguous released asset-type evidence");
			}
		}
		if (!limitations.isEmpty()) {
			throw new SkipException(REQ_SYSTEM_CLASS + " - incomplete class-compatibility evidence: "
					+ String.join(" | ", limitations));
		}
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-RESOURCE-MAPPINGS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_SYSTEM_MAPPINGS
			+ ": every available System SensorML document uses released attribute and association mappings",
			groups = GROUP, alwaysRun = true)
	public void systemSensorMlMappingsAreValid() {
		validateMappings(ResourceType.SYSTEM, REQ_SYSTEM_MAPPINGS);
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-SCHEMAS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_DEPLOYMENT_SCHEMA
			+ ": canonical Deployment single and collection SensorML documents validate against released schemas",
			groups = GROUP, alwaysRun = true)
	public void deploymentSensorMlSchemasAreValid() {
		validateSchemas(ResourceType.DEPLOYMENT, REQ_DEPLOYMENT_SCHEMA);
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-RESOURCE-MAPPINGS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_DEPLOYMENT_MAPPINGS
			+ ": every available Deployment SensorML document uses released attribute and association mappings",
			groups = GROUP, alwaysRun = true)
	public void deploymentSensorMlMappingsAreValid() {
		validateMappings(ResourceType.DEPLOYMENT, REQ_DEPLOYMENT_MAPPINGS);
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-SCHEMAS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_PROCEDURE_SCHEMA
			+ ": canonical Procedure single and collection SensorML documents validate against released schemas",
			groups = GROUP, alwaysRun = true)
	public void procedureSensorMlSchemasAreValid() {
		validateSchemas(ResourceType.PROCEDURE, REQ_PROCEDURE_SCHEMA);
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-CLASS-COMPATIBILITY-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_PROCEDURE_CLASS
			+ ": every available Procedure uses a compatible SensorML class and omits position", groups = GROUP,
			alwaysRun = true)
	public void procedureSensorMlClassesAreCompatible() {
		requireResourceDeclaration(ResourceType.PROCEDURE, REQ_PROCEDURE_CLASS);
		TraversalResult traversal = traverse(ResourceType.PROCEDURE, REQ_PROCEDURE_CLASS);
		if (traversal.items().isEmpty()) {
			throw new SkipException(REQ_PROCEDURE_CLASS + " - procedures returned no resource to inspect.");
		}
		for (Map<String, Object> procedure : traversal.items()) {
			SensorMlSupport.validateProcedureClass(procedure, REQ_PROCEDURE_CLASS,
					ResourceType.PROCEDURE.collectionPath());
		}
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-RESOURCE-MAPPINGS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_PROCEDURE_MAPPINGS
			+ ": every available Procedure SensorML document uses released attribute and association mappings",
			groups = GROUP, alwaysRun = true)
	public void procedureSensorMlMappingsAreValid() {
		validateMappings(ResourceType.PROCEDURE, REQ_PROCEDURE_MAPPINGS);
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-SCHEMAS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_PROPERTY_SCHEMA
			+ ": canonical Property single and collection SensorML documents validate against released schemas",
			groups = GROUP, alwaysRun = true)
	public void propertySensorMlSchemasAreValid() {
		validateSchemas(ResourceType.PROPERTY, REQ_PROPERTY_SCHEMA);
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-RESOURCE-MAPPINGS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_PROPERTY_MAPPINGS
			+ ": every available Property SensorML document uses released URI mappings", groups = GROUP,
			alwaysRun = true)
	public void propertySensorMlMappingsAreValid() {
		validateMappings(ResourceType.PROPERTY, REQ_PROPERTY_MAPPINGS);
	}

	private void validateSchemas(ResourceType type, String requirement) {
		requireResourceDeclaration(type, requirement);
		TraversalResult traversal = traverse(type, requirement);
		for (Part1ApiCommonSupport.PageDocument page : traversal.pages()) {
			SensorMlSupport.validateSchema(page.body(), type.collectionSchema(), type, requirement,
					page.source().toString());
		}
		if (traversal.items().isEmpty()) {
			throw new SkipException(requirement + " - " + type.collectionPath()
					+ " returned no resource from which to request a canonical single resource.");
		}
		String id = requiredItemId(traversal.items().get(0), type, requirement);
		URI source = singleResourceUri(type, id);
		Map<String, Object> single = sensorMlObject(getExpected200(source, SensorMlSupport.MEDIA_TYPE, requirement),
				source, requirement);
		SensorMlSupport.validateSchema(single, type.singleSchema(), type, requirement, source.toString());
	}

	private void validateMappings(ResourceType type, String requirement) {
		requireResourceDeclaration(type, requirement);
		TraversalResult traversal = traverse(type, requirement);
		if (traversal.items().isEmpty()) {
			throw new SkipException(requirement + " - " + type.collectionPath() + " returned no resource to inspect.");
		}
		for (Map<String, Object> document : traversal.items()) {
			SensorMlSupport.validateResourceMappings(document, type, requirement, type.collectionPath());
			SensorMlSupport.validateRelationTypes(document, type, requirement, type.collectionPath());
			String id = requiredItemId(document, type, requirement);
			URI source = singleResourceUri(type, id);
			for (SensorMlSupport.AssociationTarget target : SensorMlSupport.associationTargets(document, type, source,
					this.apiRoot, requirement)) {
				verifyAssociationTarget(target, requirement);
			}
		}
	}

	private Inspection inspectAllTypes(String requirement, BiConsumer<ResourceType, Map<String, Object>> consumer) {
		Set<ResourceType> supported = declaredResourceTypes(requirement);
		Inspection inspection = new Inspection();
		for (ResourceType type : supported) {
			try {
				TraversalResult traversal = traverse(type, requirement);
				if (traversal.items().isEmpty()) {
					inspection.limitations().add(type.collectionPath() + " returned no resource");
					continue;
				}
				for (Map<String, Object> document : traversal.items()) {
					consumer.accept(type, document);
					inspection.inspected++;
				}
			}
			catch (SkipException ex) {
				inspection.limitations().add(message(ex));
			}
		}
		if (inspection.inspected == 0) {
			inspection.limitations().add("no supported SensorML resource representation was available");
		}
		return inspection;
	}

	private TraversalResult traverse(ResourceType type, String requirement) {
		URI endpoint = this.apiRoot.resolve(type.collectionPath());
		Optional<TraversalResult> result;
		try {
			result = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint, SensorMlSupport.MEDIA_TYPE, Map.of(),
					requirement, Set.of(SensorMlSupport.MEDIA_TYPE));
		}
		catch (SkipException ex) {
			ETSAssert.failWithUri(requirement, message(ex));
			throw new IllegalStateException("unreachable", ex);
		}
		if (result.isEmpty()) {
			throw new SkipException(requirement + " - " + endpoint + " returned HTTP 404.");
		}
		return result.orElseThrow();
	}

	private Set<ResourceType> declaredResourceTypes(String requirement) {
		List<String> declarations = conformanceDeclarations(requirement);
		requireSensorMlDeclaration(declarations, requirement);
		EnumSet<ResourceType> supported = EnumSet.noneOf(ResourceType.class);
		for (ResourceType type : ResourceType.values()) {
			if (declarations.contains(type.conformanceUri())) {
				supported.add(type);
			}
		}
		if (supported.isEmpty()) {
			throw new SkipException(
					requirement + " - IUT declares SensorML but no canonical SensorML resource conformance class.");
		}
		return supported;
	}

	private void requireResourceDeclaration(ResourceType type, String requirement) {
		List<String> declarations = conformanceDeclarations(requirement);
		requireSensorMlDeclaration(declarations, requirement);
		if (!declarations.contains(type.conformanceUri())) {
			throw new SkipException(requirement + " - IUT does not declare " + type.conformanceUri() + ".");
		}
	}

	private static void requireSensorMlDeclaration(List<String> declarations, String requirement) {
		if (!declarations.contains(CONF_SENSORML)) {
			throw new SkipException(requirement + " - IUT does not declare " + CONF_SENSORML + ".");
		}
	}

	@SuppressWarnings("unchecked")
	private List<String> conformanceDeclarations(String requirement) {
		requireApiRoot(requirement);
		URI endpoint = this.apiRoot.resolve("conformance");
		Response response = get(endpoint, "application/json");
		ETSAssert.assertStatus(response, 200, requirement);
		Map<String, Object> body = parseDiscoveryObject(response, endpoint, requirement);
		Object value = body.get("conformsTo");
		if (!(value instanceof List<?>)) {
			ETSAssert.failWithUri(requirement, endpoint + " response is missing a conformsTo array.");
		}
		for (Object declaration : (List<?>) value) {
			if (!(declaration instanceof String)) {
				ETSAssert.failWithUri(requirement, endpoint + " conformsTo contains a non-string value.");
			}
		}
		return (List<String>) value;
	}

	private ApiDefinition apiDefinition(String requirement) {
		List<String> declarations = conformanceDeclarations(requirement);
		requireSensorMlDeclaration(declarations, requirement);
		URI landingUri = this.apiRoot;
		Map<String, Object> landing = parseDiscoveryObject(getExpected200(landingUri, "application/json", requirement),
				landingUri, requirement);
		Object links = landing.get("links");
		if (!(links instanceof List<?>)) {
			throw new SkipException(requirement + " - landing page has no links array containing rel=service-desc.");
		}
		boolean advertised = false;
		for (Object value : (List<?>) links) {
			if (!(value instanceof Map<?, ?> link) || !"service-desc".equals(link.get("rel"))) {
				continue;
			}
			advertised = true;
			Object href = link.get("href");
			if (!(href instanceof String) || ((String) href).isBlank()) {
				ETSAssert.failWithUri(requirement, "An advertised service-desc link has no non-empty href.");
			}
			URI source;
			try {
				source = landingUri.resolve((String) href);
			}
			catch (IllegalArgumentException ex) {
				ETSAssert.failWithUri(requirement, "An advertised service-desc href is invalid: " + href);
				throw new IllegalStateException("unreachable", ex);
			}
			String content = serviceDescription(source, requirement);
			ApiDefinition definition = SensorMlSupport.parseApiDefinition(content, source, requirement);
			if (!definition.model().getPaths().keySet().stream().anyMatch(path -> path.contains("/systems"))) {
				ETSAssert.failWithUri(requirement, source + " does not describe Part 1 canonical resources.");
			}
			return definition;
		}
		if (!advertised) {
			throw new SkipException(requirement + " - landing page has no rel=service-desc API definition.");
		}
		throw new IllegalStateException("Advertised service-desc was not evaluated.");
	}

	private String serviceDescription(URI source, String requirement) {
		if (sameOrigin(this.apiRoot, source)) {
			Response response = given().redirects()
				.follow(false)
				.accept("application/vnd.oai.openapi, application/yaml, application/json, */*")
				.when()
				.get(source)
				.andReturn();
			if (response.getStatusCode() != 200) {
				ETSAssert.failWithUri(requirement,
						source + " advertised service description returned HTTP " + response.getStatusCode() + ".");
			}
			requireApiDefinitionMedia(response.getContentType(), source, requirement);
			return response.asString();
		}
		HttpRequest request = HttpRequest.newBuilder(source)
			.timeout(Duration.ofSeconds(30))
			.header("Accept", "application/vnd.oai.openapi, application/yaml, application/json, */*")
			.GET()
			.build();
		try {
			HttpResponse<String> response = HttpClient.newBuilder()
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build()
				.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				ETSAssert.failWithUri(requirement, "Cross-origin advertised service description " + source
						+ " returned HTTP " + response.statusCode() + ".");
			}
			requireApiDefinitionMedia(response.headers().firstValue("Content-Type").orElse(""), source, requirement);
			return response.body();
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			ETSAssert.failWithUri(requirement,
					"Cross-origin advertised service description " + source + " retrieval was interrupted.");
			throw new IllegalStateException("unreachable", ex);
		}
		catch (IOException ex) {
			ETSAssert.failWithUri(requirement, "Cross-origin advertised service description " + source
					+ " could not be retrieved: " + ex.getMessage());
			throw new IllegalStateException("unreachable", ex);
		}
	}

	private void verifyAssociationTarget(SensorMlSupport.AssociationTarget target, String requirement) {
		Map<String, Object> body;
		String mediaType;
		if (sameOrigin(this.apiRoot, target.target())) {
			Response response = given().redirects()
				.follow(false)
				.accept("application/sml+json, application/geo+json, application/json")
				.when()
				.get(target.target())
				.andReturn();
			ETSAssert.assertStatus(response, 200, requirement);
			mediaType = normalizeMediaType(response.getContentType());
			body = parseJsonObject(response, target.target(), requirement, true);
		}
		else {
			HttpRequest request = HttpRequest.newBuilder(target.target())
				.timeout(Duration.ofSeconds(30))
				.header("Accept", "application/sml+json, application/geo+json, application/json")
				.GET()
				.build();
			try {
				HttpResponse<String> response = HttpClient.newBuilder()
					.followRedirects(HttpClient.Redirect.NEVER)
					.build()
					.send(request, HttpResponse.BodyHandlers.ofString());
				if (response.statusCode() != 200) {
					ETSAssert.failWithUri(requirement, "Cross-origin " + target.association() + " association target "
							+ target.target() + " returned HTTP " + response.statusCode() + ".");
				}
				mediaType = normalizeMediaType(response.headers().firstValue("Content-Type").orElse(""));
				body = parseJsonText(response.body(), target.target(), requirement);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				ETSAssert.failWithUri(requirement,
						"Cross-origin association retrieval was interrupted: " + target.target());
				throw new IllegalStateException("unreachable", ex);
			}
			catch (IOException ex) {
				ETSAssert.failWithUri(requirement,
						"Cross-origin association could not be retrieved: " + target.target() + ": " + ex.getMessage());
				throw new IllegalStateException("unreachable", ex);
			}
		}
		SensorMlSupport.validateAssociationRepresentation(body, mediaType, target, requirement);
		if (target.kind().resource()) {
			String path = target.target().getPath();
			String expectedId = URLDecoder.decode(path.substring(path.lastIndexOf('/') + 1), StandardCharsets.UTF_8);
			SensorMlSupport.validateResourceId(body, expectedId, requirement, target.target().toString());
		}
	}

	private boolean customCollectionsAdvertised() {
		URI endpoint = this.apiRoot.resolve("collections");
		Response response = get(endpoint, "application/json");
		if (response.getStatusCode() == 404) {
			return false;
		}
		ETSAssert.assertStatus(response, 200, REQ_MEDIATYPE_READ);
		Object collections = parseDiscoveryObject(response, endpoint, REQ_MEDIATYPE_READ).get("collections");
		if (!(collections instanceof List<?>)) {
			ETSAssert.failWithUri(REQ_MEDIATYPE_READ, endpoint + " response is missing a collections array.");
		}
		return !((List<?>) collections).isEmpty();
	}

	private String requiredItemId(Map<String, Object> item, ResourceType type, String requirement) {
		Object id = item.get("id");
		if (!(id instanceof String) || ((String) id).isBlank()) {
			ETSAssert.failWithUri(requirement,
					type.collectionPath() + " collection resource has no non-empty id for canonical dereference.");
		}
		return (String) id;
	}

	private URI singleResourceUri(ResourceType type, String id) {
		return this.apiRoot
			.resolve(type.collectionPath() + "/" + URLEncoder.encode(id, StandardCharsets.UTF_8).replace("+", "%20"));
	}

	private void requireApiRoot(String requirement) {
		if (this.apiRoot == null) {
			throw new SkipException(requirement + " - API root fixture is unavailable.");
		}
	}

	private static Response get(URI uri, String accept) {
		return given().accept(accept).when().get(uri).andReturn();
	}

	private static Response getExpected200(URI uri, String accept, String requirement) {
		Response response = get(uri, accept);
		ETSAssert.assertStatus(response, 200, requirement);
		return response;
	}

	private static Map<String, Object> sensorMlObject(Response response, URI source, String requirement) {
		String mediaType = normalizeMediaType(response.getContentType());
		if (!SensorMlSupport.MEDIA_TYPE.equals(mediaType)) {
			ETSAssert.failWithUri(requirement,
					source + " returned " + (mediaType.isBlank() ? "no Content-Type" : "media type '" + mediaType + "'")
							+ " instead of application/sml+json.");
		}
		return parseJsonObject(response, source, requirement, true);
	}

	private static Map<String, Object> parseDiscoveryObject(Response response, URI source, String requirement) {
		return parseJsonObject(response, source, requirement, false);
	}

	private static Map<String, Object> parseJsonObject(Response response, URI source, String requirement,
			boolean requireJsonMedia) {
		try {
			String mediaType = normalizeMediaType(response.getContentType());
			if (requireJsonMedia && !"application/json".equals(mediaType)
					&& !(mediaType.startsWith("application/") && mediaType.endsWith("+json"))) {
				ETSAssert.failWithUri(requirement,
						source + " response is not a JSON media type: '" + response.getContentType() + "'.");
			}
			return parseJsonText(response.asString(), source, requirement);
		}
		catch (Exception ex) {
			ETSAssert.failWithUri(requirement,
					source + " response body is not parseable as a JSON object: " + ex.getMessage());
			return Map.of();
		}
	}

	private static Map<String, Object> parseJsonText(String content, URI source, String requirement) {
		try {
			Map<String, Object> body = JsonPath.from(content).getMap("$");
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

	private static String normalizeMediaType(String mediaType) {
		return mediaType == null ? "" : mediaType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
	}

	private static void requireApiDefinitionMedia(String contentType, URI source, String requirement) {
		String mediaType = normalizeMediaType(contentType);
		if (!Set
			.of("application/json", "application/yaml", "text/yaml", "application/vnd.oai.openapi",
					"application/openapi+json", "application/openapi+yaml")
			.contains(mediaType) && !(mediaType.startsWith("application/") && mediaType.endsWith("+json"))
				&& !(mediaType.startsWith("application/") && mediaType.endsWith("+yaml"))) {
			ETSAssert.failWithUri(requirement,
					source + " returned unsupported API-definition media type '" + contentType + "'.");
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

	private static String message(Throwable throwable) {
		return throwable.getMessage() == null || throwable.getMessage().isBlank() ? throwable.getClass().getSimpleName()
				: throwable.getMessage();
	}

	private static void skipWhenPrerequisiteUnsatisfied(ITestContext testContext) {
		String blocker = configurationBlocker(testContext.getFailedConfigurations(), "failed");
		if (blocker == null) {
			blocker = configurationBlocker(testContext.getSkippedConfigurations(), "skipped");
		}
		if (blocker == null) {
			blocker = testBlocker(testContext.getFailedTests(), "failed", false);
		}
		if (blocker == null) {
			blocker = testBlocker(testContext.getSkippedTests(), "skipped", true);
		}
		if (blocker != null) {
			throw new SkipException("SensorML setup skipped before IUT access because prerequisite " + blocker + ".");
		}
	}

	private static String configurationBlocker(IResultMap results, String status) {
		if (results == null) {
			return null;
		}
		for (ITestResult result : results.getAllResults()) {
			if (result != null && result.getMethod() != null && isInheritedPrerequisite(result)) {
				return "configuration " + result.getMethod().getMethodName() + " " + status;
			}
		}
		return null;
	}

	private static String testBlocker(IResultMap results, String status, boolean allowDatetimeEvidenceLimitation) {
		if (results == null) {
			return null;
		}
		for (ITestResult result : results.getAllResults()) {
			if (result == null || result.getMethod() == null || !isInheritedPrerequisite(result)) {
				continue;
			}
			if (allowDatetimeEvidenceLimitation && DATETIME_EVIDENCE_METHOD.equals(result.getMethod().getMethodName())
					&& result.getThrowable() instanceof SkipException
					&& Part1ApiCommonTests.DATETIME_EVIDENCE_LIMITATION.equals(result.getThrowable().getMessage())) {
				Reporter.log(Part1ApiCommonTests.DATETIME_EVIDENCE_LIMITATION
						+ " SensorML direct procedures will execute, but inherited conformance remains incomplete.",
						true);
				continue;
			}
			return "method " + result.getMethod().getMethodName() + " " + status;
		}
		return null;
	}

	private static boolean isInheritedPrerequisite(ITestResult result) {
		for (String group : result.getMethod().getGroups()) {
			if ("core".equals(group) || "common".equals(group) || "part1apicommon".equals(group)) {
				return true;
			}
		}
		Class<?> realClass = result.getMethod().getRealClass();
		if (realClass == null) {
			return false;
		}
		String className = realClass.getName();
		return realClass == Part1ApiCommonTests.class
				|| className.startsWith("org.opengis.cite.ogcapiconnectedsystems10.conformance.core.")
				|| className.startsWith("org.opengis.cite.ogcapiconnectedsystems10.conformance.common.");
	}

	private static final class Inspection {

		private final List<String> limitations = new ArrayList<>();

		private int inspected;

		List<String> limitations() {
			return this.limitations;
		}

		void finish(String requirement) {
			if (!this.limitations.isEmpty()) {
				throw new SkipException(requirement + " - incomplete evidence after inspecting all independent "
						+ "resource types: " + String.join(" | ", this.limitations));
			}
		}

	}

}
