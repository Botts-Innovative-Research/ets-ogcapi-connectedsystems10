package org.opengis.cite.ogcapiconnectedsystems10.conformance.geojson;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.net.URI;
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
import org.opengis.cite.ogcapiconnectedsystems10.conformance.geojson.GeoJsonSupport.ApiDefinition;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.geojson.GeoJsonSupport.ResourceType;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.TraversalResult;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonTests;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * Released OGC 23-001 Annex A procedures for the GeoJSON conformance class.
 */
public class GeoJsonTests {

	static final String GROUP = "geojson";

	static final String CONF_GEOJSON = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/geojson";

	static final String REQ_MEDIATYPE_READ = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/mediatype-read";

	static final String REQ_MEDIATYPE_WRITE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/mediatype-write";

	static final String REQ_RELATION_TYPES = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/relation-types";

	static final String REQ_FEATURE_ATTRIBUTE_MAPPING = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/feature-attribute-mapping";

	static final String REQ_SYSTEM_SCHEMA = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/system-schema";

	static final String REQ_SYSTEM_MAPPINGS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/system-mappings";

	static final String REQ_DEPLOYMENT_SCHEMA = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/deployment-schema";

	static final String REQ_DEPLOYMENT_MAPPINGS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/deployment-mappings";

	static final String REQ_PROCEDURE_SCHEMA = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/procedure-schema";

	static final String REQ_PROCEDURE_MAPPINGS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/procedure-mappings";

	static final String REQ_SF_SCHEMA = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/sf-schema";

	static final String REQ_SF_MAPPINGS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/sf-mappings";

	private static final String DATETIME_EVIDENCE_METHOD = "datetimeUsesValidTime";

	private URI apiRoot;

	/**
	 * Loads only the immutable API root after inherited API Common prerequisites.
	 * @param testContext active TestNG context.
	 */
	@BeforeClass(dependsOnGroups = "part1apicommon", alwaysRun = true)
	public void fetchGeoJsonArguments(ITestContext testContext) {
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
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-MEDIATYPE-READ-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_MEDIATYPE_READ
			+ ": API definition advertises application/geo+json for every supported canonical and advertised custom collection GET",
			groups = GROUP, alwaysRun = true)
	public void geoJsonMediaTypeReadIsAdvertised() {
		Set<ResourceType> supported = declaredResourceTypes(REQ_MEDIATYPE_READ);
		ApiDefinition definition = apiDefinition(REQ_MEDIATYPE_READ);
		GeoJsonSupport.assertReadMediaAdvertisements(definition, supported, customCollectionsAdvertised(),
				REQ_MEDIATYPE_READ);
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-MEDIATYPE-WRITE-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_MEDIATYPE_WRITE
			+ ": API definition advertises application/geo+json on at least one canonical CREATE or REPLACE operation",
			groups = GROUP, alwaysRun = true)
	public void geoJsonMediaTypeWriteIsAdvertised() {
		requireGeoJsonDeclaration(REQ_MEDIATYPE_WRITE);
		GeoJsonSupport.assertWriteMediaAdvertisement(apiDefinition(REQ_MEDIATYPE_WRITE), REQ_MEDIATYPE_WRITE);
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-RELATION-TYPES-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RELATION_TYPES
			+ ": every available GeoJSON links-member association uses the resource mapping relation name",
			groups = GROUP, alwaysRun = true)
	public void geoJsonAssociationRelationTypesAreValid() {
		int[] associations = { 0 };
		Inspection inspection = inspectAllTypes(REQ_RELATION_TYPES, (type, feature) -> associations[0] += GeoJsonSupport
			.validateRelationTypes(feature, type, REQ_RELATION_TYPES, type.collectionPath()));
		if (associations[0] == 0) {
			inspection.limitations().add("no links-member association was available after generic links were excluded");
		}
		inspection.finish(REQ_RELATION_TYPES);
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-FEATURE-MAPPING-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_FEATURE_ATTRIBUTE_MAPPING
			+ ": every available GeoJSON feature uses the released uid, name, and description mappings", groups = GROUP,
			alwaysRun = true)
	public void commonFeatureAttributesAreMapped() {
		inspectAllTypes(REQ_FEATURE_ATTRIBUTE_MAPPING, (type, feature) -> GeoJsonSupport.validateCommonFeature(feature,
				REQ_FEATURE_ATTRIBUTE_MAPPING, type.collectionPath()))
			.finish(REQ_FEATURE_ATTRIBUTE_MAPPING);
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-SCHEMAS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_SYSTEM_SCHEMA
			+ ": canonical System single and collection GeoJSON documents validate against released schemas",
			groups = GROUP, alwaysRun = true)
	public void systemGeoJsonSchemasAreValid() {
		validateSchemas(ResourceType.SYSTEM, REQ_SYSTEM_SCHEMA);
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-RESOURCE-MAPPINGS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_SYSTEM_MAPPINGS
			+ ": every available System GeoJSON feature uses released attribute and association mappings",
			groups = GROUP, alwaysRun = true)
	public void systemGeoJsonMappingsAreValid() {
		validateMappings(ResourceType.SYSTEM, REQ_SYSTEM_MAPPINGS);
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-SCHEMAS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_DEPLOYMENT_SCHEMA
			+ ": canonical Deployment single and collection GeoJSON documents validate against released schemas",
			groups = GROUP, alwaysRun = true)
	public void deploymentGeoJsonSchemasAreValid() {
		validateSchemas(ResourceType.DEPLOYMENT, REQ_DEPLOYMENT_SCHEMA);
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-RESOURCE-MAPPINGS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_DEPLOYMENT_MAPPINGS
			+ ": every available Deployment GeoJSON feature uses released attribute and association mappings",
			groups = GROUP, alwaysRun = true)
	public void deploymentGeoJsonMappingsAreValid() {
		validateMappings(ResourceType.DEPLOYMENT, REQ_DEPLOYMENT_MAPPINGS);
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-SCHEMAS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_PROCEDURE_SCHEMA
			+ ": canonical Procedure single and collection GeoJSON documents validate against released schemas",
			groups = GROUP, alwaysRun = true)
	public void procedureGeoJsonSchemasAreValid() {
		validateSchemas(ResourceType.PROCEDURE, REQ_PROCEDURE_SCHEMA);
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-RESOURCE-MAPPINGS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_PROCEDURE_MAPPINGS
			+ ": every available Procedure GeoJSON feature uses released attribute and association mappings",
			groups = GROUP, alwaysRun = true)
	public void procedureGeoJsonMappingsAreValid() {
		validateMappings(ResourceType.PROCEDURE, REQ_PROCEDURE_MAPPINGS);
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-SCHEMAS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_SF_SCHEMA
			+ ": canonical Sampling Feature single and collection GeoJSON documents validate against released schemas",
			groups = GROUP, alwaysRun = true)
	public void samplingFeatureGeoJsonSchemasAreValid() {
		validateSchemas(ResourceType.SAMPLING_FEATURE, REQ_SF_SCHEMA);
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-RESOURCE-MAPPINGS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_SF_MAPPINGS
			+ ": every available Sampling Feature GeoJSON feature uses released attribute and association mappings",
			groups = GROUP, alwaysRun = true)
	public void samplingFeatureGeoJsonMappingsAreValid() {
		validateMappings(ResourceType.SAMPLING_FEATURE, REQ_SF_MAPPINGS);
	}

	private void validateSchemas(ResourceType type, String requirement) {
		requireResourceDeclaration(type, requirement);
		TraversalResult traversal = traverse(type, requirement);
		if (traversal.items().isEmpty()) {
			throw new SkipException(requirement + " - " + type.collectionPath()
					+ " returned no feature from which to request a canonical single resource.");
		}
		for (Part1ApiCommonSupport.PageDocument page : traversal.pages()) {
			GeoJsonSupport.validateCollection(page.body(), type, requirement, page.source().toString());
		}
		Map<String, Object> selected = traversal.items().get(0);
		Object id = selected.get("id");
		if (!(id instanceof String) || ((String) id).isBlank()) {
			ETSAssert.failWithUri(requirement,
					type.collectionPath() + " collection feature has no non-empty id for the single-resource request.");
		}
		URI singleUri = this.apiRoot.resolve(type.collectionPath() + "/"
				+ URLEncoder.encode((String) id, StandardCharsets.UTF_8).replace("+", "%20"));
		Response response = get(singleUri, GeoJsonSupport.MEDIA_TYPE);
		ETSAssert.assertStatus(response, 200, requirement);
		requireGeoJsonMedia(response, singleUri, requirement);
		GeoJsonSupport.validateSingle(parseObject(response, singleUri, requirement), type, requirement,
				singleUri.toString());
	}

	private void validateMappings(ResourceType type, String requirement) {
		requireResourceDeclaration(type, requirement);
		TraversalResult traversal = traverse(type, requirement);
		if (traversal.items().isEmpty()) {
			throw new SkipException(requirement + " - " + type.collectionPath() + " returned no feature to inspect.");
		}
		for (Map<String, Object> feature : traversal.items()) {
			GeoJsonSupport.validateResourceMappings(feature, type, requirement, type.collectionPath());
			GeoJsonSupport.validateRelationTypes(feature, type, requirement, type.collectionPath());
		}
	}

	private Inspection inspectAllTypes(String requirement, BiConsumer<ResourceType, Map<String, Object>> consumer) {
		Set<ResourceType> supported = declaredResourceTypes(requirement);
		Inspection inspection = new Inspection();
		for (ResourceType type : supported) {
			try {
				TraversalResult traversal = traverse(type, requirement);
				if (traversal.items().isEmpty()) {
					inspection.limitations().add(type.collectionPath() + " returned no feature");
					continue;
				}
				for (Map<String, Object> feature : traversal.items()) {
					consumer.accept(type, feature);
					inspection.inspected++;
				}
			}
			catch (SkipException ex) {
				inspection.limitations().add(message(ex));
			}
		}
		if (inspection.inspected == 0) {
			inspection.limitations().add("no supported GeoJSON feature representation was available");
		}
		return inspection;
	}

	private TraversalResult traverse(ResourceType type, String requirement) {
		URI endpoint = this.apiRoot.resolve(type.collectionPath());
		Optional<TraversalResult> result = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint,
				GeoJsonSupport.MEDIA_TYPE, Map.of(), requirement, Set.of(GeoJsonSupport.MEDIA_TYPE));
		if (result.isEmpty()) {
			throw new SkipException(requirement + " - " + endpoint + " returned HTTP 404.");
		}
		return result.orElseThrow();
	}

	private Set<ResourceType> declaredResourceTypes(String requirement) {
		List<String> declarations = conformanceDeclarations(requirement);
		if (!declarations.contains(CONF_GEOJSON)) {
			throw new SkipException(requirement + " - IUT does not declare " + CONF_GEOJSON + ".");
		}
		EnumSet<ResourceType> supported = EnumSet.noneOf(ResourceType.class);
		for (ResourceType type : ResourceType.values()) {
			if (declarations.contains(type.conformanceUri())) {
				supported.add(type);
			}
		}
		if (supported.isEmpty()) {
			throw new SkipException(
					requirement + " - IUT declares GeoJSON but no canonical GeoJSON resource conformance class.");
		}
		return supported;
	}

	private void requireGeoJsonDeclaration(String requirement) {
		if (!conformanceDeclarations(requirement).contains(CONF_GEOJSON)) {
			throw new SkipException(requirement + " - IUT does not declare " + CONF_GEOJSON + ".");
		}
	}

	private void requireResourceDeclaration(ResourceType type, String requirement) {
		List<String> declarations = conformanceDeclarations(requirement);
		if (!declarations.contains(CONF_GEOJSON)) {
			throw new SkipException(requirement + " - IUT does not declare " + CONF_GEOJSON + ".");
		}
		if (!declarations.contains(type.conformanceUri())) {
			throw new SkipException(requirement + " - IUT does not declare " + type.conformanceUri() + ".");
		}
	}

	@SuppressWarnings("unchecked")
	private List<String> conformanceDeclarations(String requirement) {
		requireApiRoot(requirement);
		URI endpoint = this.apiRoot.resolve("conformance");
		Response response = get(endpoint, "application/json");
		ETSAssert.assertStatus(response, 200, requirement);
		Map<String, Object> body = parseObject(response, endpoint, requirement);
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
		requireGeoJsonDeclaration(requirement);
		URI landingUri = this.apiRoot;
		Map<String, Object> landing = parseObject(getExpected200(landingUri, "application/json", requirement),
				landingUri, requirement);
		Object links = landing.get("links");
		if (!(links instanceof List<?>)) {
			throw new SkipException(requirement + " - landing page has no links array containing rel=service-desc.");
		}
		List<String> limitations = new ArrayList<>();
		for (Object value : (List<?>) links) {
			if (!(value instanceof Map<?, ?> link) || !"service-desc".equals(link.get("rel"))) {
				continue;
			}
			Object href = link.get("href");
			if (!(href instanceof String) || ((String) href).isBlank()) {
				limitations.add("service-desc link has no non-empty href");
				continue;
			}
			URI source;
			try {
				source = landingUri.resolve((String) href);
			}
			catch (IllegalArgumentException ex) {
				limitations.add("service-desc href is invalid: " + href);
				continue;
			}
			try {
				String content = serviceDescription(source, requirement);
				ApiDefinition definition = GeoJsonSupport.parseApiDefinition(content, source, requirement);
				if (definition.model().getPaths().keySet().stream().anyMatch(path -> path.contains("/systems"))) {
					return definition;
				}
				limitations.add(source + " does not describe Part 1 canonical resources");
			}
			catch (SkipException | AssertionError ex) {
				limitations.add(message(ex));
			}
		}
		throw new SkipException(
				requirement + " - no usable Part 1 service-desc API definition: " + String.join(" | ", limitations));
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
				throw new SkipException(
						requirement + " - " + source + " returned HTTP " + response.getStatusCode() + ".");
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
				throw new SkipException(
						requirement + " - cross-origin " + source + " returned HTTP " + response.statusCode() + ".");
			}
			requireApiDefinitionMedia(response.headers().firstValue("Content-Type").orElse(""), source, requirement);
			return response.body();
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new SkipException(requirement + " - cross-origin " + source + " retrieval was interrupted.");
		}
		catch (IOException ex) {
			throw new SkipException(
					requirement + " - cross-origin " + source + " could not be retrieved: " + ex.getMessage());
		}
	}

	private boolean customCollectionsAdvertised() {
		URI endpoint = this.apiRoot.resolve("collections");
		Response response = get(endpoint, "application/json");
		if (response.getStatusCode() == 404) {
			return false;
		}
		ETSAssert.assertStatus(response, 200, REQ_MEDIATYPE_READ);
		Object collections = parseObject(response, endpoint, REQ_MEDIATYPE_READ).get("collections");
		if (!(collections instanceof List<?>)) {
			ETSAssert.failWithUri(REQ_MEDIATYPE_READ, endpoint + " response is missing a collections array.");
		}
		return !((List<?>) collections).isEmpty();
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

	private static void requireGeoJsonMedia(Response response, URI source, String requirement) {
		String mediaType = normalizeMediaType(response.getContentType());
		if (!GeoJsonSupport.MEDIA_TYPE.equals(mediaType)) {
			throw new SkipException(requirement + " - " + source + " returned "
					+ (mediaType.isBlank() ? "no Content-Type" : "unsupported media type '" + mediaType + "'")
					+ "; representation parsing skipped.");
		}
	}

	private static Map<String, Object> parseObject(Response response, URI source, String requirement) {
		try {
			String mediaType = normalizeMediaType(response.getContentType());
			if (!"application/json".equals(mediaType)
					&& !(mediaType.startsWith("application/") && mediaType.endsWith("+json"))) {
				ETSAssert.failWithUri(requirement,
						source + " response is not a JSON media type: '" + response.getContentType() + "'.");
			}
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
			throw new SkipException(requirement + " - " + source + " returned unsupported API-definition media type '"
					+ contentType + "'.");
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
			throw new SkipException("GeoJSON setup skipped before IUT access because prerequisite " + blocker + ".");
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
						+ " GeoJSON direct procedures will execute, but inherited conformance remains incomplete.",
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
