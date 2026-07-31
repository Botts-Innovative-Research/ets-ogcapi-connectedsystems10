package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.datastream;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.PageDocument;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.TraversalResult;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.apicommon.Part2ApiCommonTests;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.samplingfeatures.SamplingFeaturesSupport;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Part 2 - Datastreams and Observations conformance tests
 * ({@code /conf/datastream}; OGC 23-002 Annex A.2).
 */
public class Part2DatastreamTests {

	static final String GROUP = "part2datastream";

	static final String CONF_DATASTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/datastream";

	static final String CONF_PART2_API_COMMON = Part2ApiCommonTests.CONF_PART2_API_COMMON;

	static final String CONF_PART1_SAMPLING_FEATURES = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/sf";

	static final String CONF_PART1_SYSTEM = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/system";

	static final String CONF_PART1_DEPLOYMENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/deployment";

	static final String REQ_DATASTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/datastream";

	static final String REQ_SF_REF_FROM_DATASTREAM = REQ_DATASTREAM + "/sf-ref-from-datastream";

	static final String REQ_FOI_REF_FROM_DATASTREAM = REQ_DATASTREAM + "/foi-ref-from-datastream";

	static final String REQ_CANONICAL_URL = REQ_DATASTREAM + "/canonical-url";

	static final String REQ_RESOURCES_ENDPOINT = REQ_DATASTREAM + "/resources-endpoint";

	static final String REQ_CANONICAL_ENDPOINT = REQ_DATASTREAM + "/canonical-endpoint";

	static final String REQ_REF_FROM_SYSTEM = REQ_DATASTREAM + "/ref-from-system";

	static final String REQ_REF_FROM_DEPLOYMENT = REQ_DATASTREAM + "/ref-from-deployment";

	static final String REQ_COLLECTIONS = REQ_DATASTREAM + "/collections";

	static final String REQ_SCHEMA_OP = REQ_DATASTREAM + "/schema-op";

	static final String REQ_OBS_CANONICAL_URL = REQ_DATASTREAM + "/obs-canonical-url";

	static final String REQ_OBS_RESOURCES_ENDPOINT = REQ_DATASTREAM + "/obs-resources-endpoint";

	static final String REQ_OBS_CANONICAL_ENDPOINT = REQ_DATASTREAM + "/obs-canonical-endpoint";

	static final String REQ_OBS_REF_FROM_DATASTREAM = REQ_DATASTREAM + "/obs-ref-from-datastream";

	static final String REQ_OBS_COLLECTIONS = REQ_DATASTREAM + "/obs-collections";

	private URI iutUri;

	private URI apiRoot;

	/**
	 * Loads immutable suite arguments after the released Part 2 API Common prerequisite.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass(dependsOnGroups = "part2apicommon", alwaysRun = true)
	public void fetchPart2DatastreamInputs(ITestContext testContext) {
		skipWhenPrerequisiteUnsatisfied(testContext);
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		configure((URI) iutAttr);
	}

	void configure(URI iut) {
		if (iut == null || !iut.isAbsolute()) {
			throw new IllegalArgumentException("IUT must be an absolute URI.");
		}
		this.iutUri = iut;
		String iutString = this.iutUri.toString();
		this.apiRoot = URI.create(iutString.endsWith("/") ? iutString : iutString + "/");
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-ASSOCIATION-SUBRESOURCES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_SF_REF_FROM_DATASTREAM
			+ ": every Datastream exposes a readable SamplingFeature sub-resource endpoint (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-ASSOCIATION-SUBRESOURCES-001)",
			groups = GROUP, alwaysRun = true)
	public void datastreamSamplingFeaturesAreAvailableFromDatastream() {
		Map<String, Object> conformance = requireDatastreamDeclaration(REQ_SF_REF_FROM_DATASTREAM);
		skipIfConditionClassUndeclared(conformance, CONF_PART1_SAMPLING_FEATURES, REQ_SF_REF_FROM_DATASTREAM,
				"Requirement 3 applies only when the Part 1 Sampling Features conformance class is declared.");
		validateSamplingFeatureSubresources("samplingFeatures", REQ_SF_REF_FROM_DATASTREAM);
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-ASSOCIATION-SUBRESOURCES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_FOI_REF_FROM_DATASTREAM
			+ ": every Datastream exposes a readable FeatureOfInterest sub-resource endpoint (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-ASSOCIATION-SUBRESOURCES-001)",
			groups = GROUP, alwaysRun = true)
	public void datastreamFeaturesOfInterestAreAvailableFromDatastream() {
		requireDatastreamDeclaration(REQ_FOI_REF_FROM_DATASTREAM);
		TraversalResult datastreams = datastreams(REQ_FOI_REF_FROM_DATASTREAM);
		skipUnlessFeatureOfInterestCondition(datastreams, REQ_FOI_REF_FROM_DATASTREAM);
		validateFeatureOfInterestSubresources(datastreams, REQ_FOI_REF_FROM_DATASTREAM);
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CANONICAL_URL
			+ ": every Datastream collection item dereferences its advertised canonical URL with equivalent content (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-RELEASED-PROCEDURES-001)",
			groups = GROUP, alwaysRun = true)
	public void datastreamCanonicalResourceReadable() {
		requireDatastreamDeclaration(REQ_CANONICAL_URL);
		validateCanonicalLinksForCollections("DataStream", false, REQ_CANONICAL_URL);
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_RESOURCES_ENDPOINT
			+ ": DataStream resource endpoint returns schema-valid content (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-RELEASED-PROCEDURES-001)",
			groups = GROUP, alwaysRun = true)
	public void datastreamsCollectionReadable() {
		requireDatastreamDeclaration(REQ_RESOURCES_ENDPOINT);
		validateDatastreamEndpoint(this.apiRoot.resolve("datastreams"), REQ_RESOURCES_ENDPOINT);
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CANONICAL_ENDPOINT
			+ ": canonical /datastreams endpoint satisfies the DataStream resource endpoint procedure (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-RELEASED-PROCEDURES-001)",
			groups = GROUP, alwaysRun = true)
	public void datastreamsCanonicalEndpointExposesDatastreamItems() {
		requireDatastreamDeclaration(REQ_CANONICAL_ENDPOINT);
		validateDatastreamEndpoint(this.apiRoot.resolve("datastreams"), REQ_CANONICAL_ENDPOINT);
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-ASSOCIATION-SUBRESOURCES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_REF_FROM_SYSTEM
			+ ": every System exposes a schema-valid Datastream sub-resource endpoint (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-ASSOCIATION-SUBRESOURCES-001)",
			groups = GROUP, alwaysRun = true)
	public void systemScopedDatastreamsReadableWhenSystemReferencePresent() {
		Map<String, Object> conformance = requireDatastreamDeclaration(REQ_REF_FROM_SYSTEM);
		skipIfConditionClassUndeclared(conformance, CONF_PART1_SYSTEM, REQ_REF_FROM_SYSTEM,
				"Requirement 8 applies only when the Part 1 System conformance class is declared.");
		for (String systemId : localIdsForCanonicalResources("systems", REQ_REF_FROM_SYSTEM)) {
			validateDatastreamEndpoint(this.apiRoot.resolve("systems/" + encode(systemId) + "/datastreams"),
					REQ_REF_FROM_SYSTEM);
		}
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-ASSOCIATION-SUBRESOURCES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_REF_FROM_DEPLOYMENT
			+ ": every Deployment exposes a schema-valid Datastream sub-resource endpoint (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-ASSOCIATION-SUBRESOURCES-001)",
			groups = GROUP, alwaysRun = true)
	public void deploymentScopedDatastreamsReadableWhenDeploymentReferencePresent() {
		Map<String, Object> conformance = requireDatastreamDeclaration(REQ_REF_FROM_DEPLOYMENT);
		skipIfConditionClassUndeclared(conformance, CONF_PART1_DEPLOYMENT, REQ_REF_FROM_DEPLOYMENT,
				"Requirement 9 applies only when the Part 1 Deployment conformance class is declared.");
		for (String deploymentId : localIdsForCanonicalResources("deployments", REQ_REF_FROM_DEPLOYMENT)) {
			validateDatastreamEndpoint(this.apiRoot.resolve("deployments/" + encode(deploymentId) + "/datastreams"),
					REQ_REF_FROM_DEPLOYMENT);
		}
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-COLLECTION-TAGGING-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COLLECTIONS
			+ ": every advertised DataStream collection retrieves schema-valid items (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-COLLECTION-TAGGING-001)",
			groups = GROUP, alwaysRun = true)
	public void datastreamCollectionsAreTaggedWithItemType() {
		requireDatastreamDeclaration(REQ_COLLECTIONS);
		validateAdvertisedCollections("DataStream", false, REQ_COLLECTIONS);
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_SCHEMA_OP
			+ ": every Datastream exposes /schema for every advertised Observation format (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-RELEASED-PROCEDURES-001)",
			groups = GROUP, alwaysRun = true)
	public void datastreamSchemaReadable() {
		requireDatastreamDeclaration(REQ_SCHEMA_OP);
		int checked = 0;
		for (Map<String, Object> datastream : datastreams(REQ_SCHEMA_OP).items()) {
			String id = requireString(datastream, "id", REQ_SCHEMA_OP);
			List<String> formats = Part2DatastreamSupport.observationFormats(datastream);
			if (formats.isEmpty()) {
				ETSAssert.failWithUri(REQ_SCHEMA_OP,
						"Datastream '" + id + "' does not list supported Observation formats.");
			}
			for (String format : formats) {
				Response response = given().accept("application/json")
					.queryParam("obsFormat", format)
					.when()
					.get(this.apiRoot.resolve("datastreams/" + encode(id) + "/schema"))
					.andReturn();
				Part2DatastreamSupport.validateObservationSchema(response, format, REQ_SCHEMA_OP,
						"/datastreams/" + id + "/schema?obsFormat=" + format);
				checked++;
			}
		}
		if (checked == 0) {
			throw new SkipException(REQ_SCHEMA_OP + " - no Datastream resource was available for schema-op checks.");
		}
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_OBS_CANONICAL_URL
			+ ": every Observation collection item dereferences its advertised canonical URL with equivalent content (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-RELEASED-PROCEDURES-001)",
			groups = GROUP, alwaysRun = true)
	public void observationCanonicalResourceReadableWhenCollectionPopulated() {
		requireDatastreamDeclaration(REQ_OBS_CANONICAL_URL);
		validateCanonicalLinksForCollections("Observation", true, REQ_OBS_CANONICAL_URL);
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_OBS_RESOURCES_ENDPOINT
			+ ": Observation resource endpoint returns schema-valid content (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-RELEASED-PROCEDURES-001)",
			groups = GROUP, alwaysRun = true)
	public void observationResourcesEndpointReadable() {
		requireDatastreamDeclaration(REQ_OBS_RESOURCES_ENDPOINT);
		validateObservationEndpoint(this.apiRoot.resolve("observations"), REQ_OBS_RESOURCES_ENDPOINT);
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_OBS_CANONICAL_ENDPOINT
			+ ": canonical /observations endpoint satisfies the Observation resource endpoint procedure (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-RELEASED-PROCEDURES-001)",
			groups = GROUP, alwaysRun = true)
	public void observationsCanonicalEndpointReadable() {
		requireDatastreamDeclaration(REQ_OBS_CANONICAL_ENDPOINT);
		validateObservationEndpoint(this.apiRoot.resolve("observations"), REQ_OBS_CANONICAL_ENDPOINT);
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-ASSOCIATION-SUBRESOURCES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_OBS_REF_FROM_DATASTREAM
			+ ": every Datastream exposes a schema-valid Observation sub-resource endpoint (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-ASSOCIATION-SUBRESOURCES-001)",
			groups = GROUP, alwaysRun = true)
	public void observationsReferenceSelectedDatastreamWhenNestedCollectionPopulated() {
		requireDatastreamDeclaration(REQ_OBS_REF_FROM_DATASTREAM);
		for (String datastreamId : datastreamIds(REQ_OBS_REF_FROM_DATASTREAM)) {
			validateObservationEndpoint(this.apiRoot.resolve("datastreams/" + encode(datastreamId) + "/observations"),
					REQ_OBS_REF_FROM_DATASTREAM);
		}
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-COLLECTION-TAGGING-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_OBS_COLLECTIONS
			+ ": every advertised Observation collection retrieves schema-valid items (REQ-ETS-PART2-002, SCENARIO-ETS-PART2-002-COLLECTION-TAGGING-001)",
			groups = GROUP, alwaysRun = true)
	public void observationCollectionsAreTaggedWithItemType() {
		requireDatastreamDeclaration(REQ_OBS_COLLECTIONS);
		validateAdvertisedCollections("Observation", true, REQ_OBS_COLLECTIONS);
	}

	static boolean declaresConformance(Map<String, Object> body, String conformanceUri) {
		return Part2ApiCommonTests.declaresConformance(body, conformanceUri);
	}

	static boolean hasDatastreamShape(Map<String, Object> body) {
		if (body == null || !(body.get("id") instanceof String)) {
			return false;
		}
		boolean hasSystem = body.containsKey("system@id") || body.containsKey("system@link");
		return hasSystem && body.containsKey("outputName") && body.get("observedProperties") instanceof List
				&& body.get("formats") instanceof List && body.containsKey("resultType");
	}

	static boolean hasAssociationEvidence(Map<String, Object> resource, String association) {
		if (resource == null || association == null || association.isBlank()) {
			return false;
		}
		for (String member : associationMemberNames(association)) {
			if (resource.containsKey(member)) {
				return true;
			}
		}
		Set<String> tokens = associationTokens(association);
		if (linkMatchesAnyToken(resource.get("links"), tokens) || directMemberMatchesAnyToken(resource, tokens)) {
			return true;
		}
		for (String member : associationMemberNames(association)) {
			if (linkMatchesAnyToken(resource.get(member), tokens)) {
				return true;
			}
		}
		return false;
	}

	static boolean hasLocalFeatureOfInterestCondition(List<Map<String, Object>> datastreams, URI apiRoot) {
		if (datastreams == null || apiRoot == null) {
			return false;
		}
		for (Map<String, Object> datastream : datastreams) {
			if (hasLocalAssociationEvidence(datastream, "featuresOfInterest", apiRoot)
					|| hasLocalAssociationEvidence(datastream, "featureOfInterest", apiRoot)) {
				return true;
			}
		}
		return false;
	}

	static boolean hasLocalAssociationEvidence(Map<String, Object> resource, String association, URI apiRoot) {
		if (resource == null || association == null || association.isBlank() || apiRoot == null) {
			return false;
		}
		Set<String> tokens = associationTokens(association);
		if (localLinkMatchesAnyToken(resource.get("links"), tokens, apiRoot)) {
			return true;
		}
		for (String member : associationMemberNames(association)) {
			Object value = resource.get(member);
			if (localLinkMatchesAnyToken(value, tokens, apiRoot)) {
				return true;
			}
			if (isEmbeddedAssociationMember(member, association) && containsInlineDescription(value)) {
				return true;
			}
		}
		return false;
	}

	static boolean isCollectionTagged(Map<String, Object> collection, String itemType) {
		return Part2DatastreamSupport.isCollectionTagged(collection, itemType);
	}

	static boolean observationReferencesDatastream(Object observation, String datastreamId) {
		if (!(observation instanceof Map) || datastreamId == null || datastreamId.isBlank()) {
			return false;
		}
		Map<?, ?> obs = (Map<?, ?>) observation;
		if (datastreamId.equals(stringValue(obs.get("datastream@id")))
				|| datastreamId.equals(stringValue(obs.get("datastreamId")))) {
			return true;
		}
		Object datastream = obs.get("datastream");
		if (datastream instanceof Map && datastreamId.equals(stringValue(((Map<?, ?>) datastream).get("id")))) {
			return true;
		}
		return linksContainDatastream(obs.get("links"), datastreamId)
				|| linksContainDatastream(obs.get("datastream@link"), datastreamId);
	}

	static boolean hasItemsOnlyCollectionShape(Map<String, Object> body) {
		return body != null && body.get("items") instanceof List;
	}

	static boolean hasObservationShape(Object body) {
		if (!(body instanceof Map)) {
			return false;
		}
		Map<?, ?> observation = (Map<?, ?>) body;
		return observation.get("id") instanceof String
				&& (observation.containsKey("datastream@id") || observation.containsKey("datastreamId")
						|| observation.containsKey("datastream") || observation.containsKey("phenomenonTime")
						|| observation.containsKey("resultTime") || observation.containsKey("result"));
	}

	private Map<String, Object> requireDatastreamDeclaration(String requirement) {
		Response response = given().accept("application/json")
			.when()
			.get(this.apiRoot.resolve("conformance"))
			.andReturn();
		ETSAssert.assertStatus(response, 200, requirement);
		Map<String, Object> parsed = parseBody(response);
		if (parsed == null) {
			ETSAssert.failWithUri(requirement,
					"/conformance body did not parse as JSON. Content-Type was: " + response.getContentType());
		}
		ETSAssert.assertJsonObjectHas(parsed, "conformsTo", List.class, requirement);
		if (!declaresConformance(parsed, CONF_DATASTREAM)) {
			throw new SkipException(CONF_DATASTREAM
					+ " - IUT does not declare the CS API Part 2 Datastreams and Observations conformance class in /conformance.");
		}
		return parsed;
	}

	private static void skipIfConditionClassUndeclared(Map<String, Object> conformance, String conformanceClass,
			String requirement, String reason) {
		if (!declaresConformance(conformance, conformanceClass)) {
			throw new SkipException(requirement + " - " + reason + " Missing exact URI " + conformanceClass + ".");
		}
	}

	private TraversalResult datastreams(String requirement) {
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.canonicalResourcesDetailed(this.apiRoot,
				"datastreams");
		if (evidence.isEmpty()) {
			ETSAssert.failWithUri(requirement, this.apiRoot.resolve("datastreams") + " returned HTTP 404.");
		}
		TraversalResult traversal = evidence.orElseThrow();
		Part2DatastreamSupport.validateDatastreamEndpoint(this.apiRoot.resolve("datastreams"), traversal.pages(),
				requirement);
		return traversal;
	}

	private List<String> datastreamIds(String requirement) {
		List<String> ids = Part2DatastreamSupport.localIds(datastreams(requirement).items(), requirement);
		if (ids.isEmpty()) {
			throw new SkipException(requirement + " - no Datastream resources are available for per-resource checks.");
		}
		return ids;
	}

	private List<String> localIdsForCanonicalResources(String resourceType, String requirement) {
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.canonicalResourcesDetailed(this.apiRoot,
				resourceType);
		if (evidence.isEmpty()) {
			ETSAssert.failWithUri(requirement, this.apiRoot.resolve(resourceType) + " returned HTTP 404.");
		}
		List<String> ids = Part2DatastreamSupport.localIds(evidence.orElseThrow().items(), requirement);
		if (ids.isEmpty()) {
			throw new SkipException(
					requirement + " - no " + resourceType + " resources are available for per-resource checks.");
		}
		return ids;
	}

	private void validateSamplingFeatureSubresources(String childPath, String requirement) {
		List<URI> unsupported = new ArrayList<>();
		for (String datastreamId : datastreamIds(requirement)) {
			URI endpoint = this.apiRoot.resolve("datastreams/" + encode(datastreamId) + "/" + childPath);
			Optional<TraversalResult> evidence = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint,
					"application/geo+json, application/json", Map.of(), requirement);
			if (evidence.isEmpty()) {
				ETSAssert.failWithUri(requirement, endpoint + " returned HTTP 404.");
			}
			if (!SamplingFeaturesSupport.validateSamplingFeatureEndpoint(endpoint, evidence.orElseThrow().pages(),
					requirement)) {
				unsupported.add(endpoint);
			}
		}
		if (!unsupported.isEmpty()) {
			throw new SkipException(requirement + " - " + childPath
					+ " endpoint validation could not execute for unsupported media at " + unsupported + ".");
		}
	}

	private void skipUnlessFeatureOfInterestCondition(TraversalResult datastreams, String requirement) {
		if (!hasLocalFeatureOfInterestCondition(datastreams.items(), this.apiRoot)) {
			throw new SkipException(requirement
					+ " - Requirement 4 conditions are not evidenced: no Datastream representation advertises a locally hosted featuresOfInterest association.");
		}
	}

	private void validateFeatureOfInterestSubresources(TraversalResult datastreams, String requirement) {
		int checked = 0;
		List<String> ids = Part2DatastreamSupport.localIds(datastreams.items(), requirement);
		if (ids.isEmpty()) {
			throw new SkipException(
					requirement + " - no Datastream resources are available for FeatureOfInterest checks.");
		}
		for (String datastreamId : ids) {
			URI endpoint = this.apiRoot.resolve("datastreams/" + encode(datastreamId) + "/featuresOfInterest");
			Optional<TraversalResult> evidence = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint,
					"application/geo+json, application/json", Map.of(), requirement,
					Part2DatastreamSupport.FEATURE_OF_INTEREST_MEDIA);
			if (evidence.isEmpty()) {
				ETSAssert.failWithUri(requirement, endpoint + " returned HTTP 404.");
			}
			Part2DatastreamSupport.validateFeatureOfInterestEndpoint(endpoint, evidence.orElseThrow().pages(),
					requirement);
			checked++;
		}
		if (checked == 0) {
			throw new SkipException(
					requirement + " - no Datastream resources are available for FeatureOfInterest checks.");
		}
	}

	private void validateDatastreamEndpoint(URI endpoint, String requirement) {
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint,
				Part2DatastreamSupport.JSON, Map.of(), requirement, Part2DatastreamSupport.JSON_MEDIA);
		if (evidence.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " returned HTTP 404.");
		}
		Part2DatastreamSupport.validateDatastreamEndpoint(endpoint, evidence.orElseThrow().pages(), requirement);
	}

	private void validateObservationEndpoint(URI endpoint, String requirement) {
		Optional<TraversalResult> evidence = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint,
				Part2DatastreamSupport.JSON, Map.of(), requirement, Part2DatastreamSupport.JSON_MEDIA);
		if (evidence.isEmpty()) {
			ETSAssert.failWithUri(requirement, endpoint + " returned HTTP 404.");
		}
		Part2DatastreamSupport.validateObservationEndpoint(endpoint, evidence.orElseThrow().pages(), requirement);
	}

	private void validateAdvertisedCollections(String itemType, boolean observation, String requirement) {
		List<Map<String, Object>> collections = advertisedCollections(itemType, requirement);
		int supportedCollections = 0;
		for (Map<String, Object> collection : collections) {
			Optional<TraversalResult> evidence = Part1ApiCommonSupport.collectionItemsDetailed(this.apiRoot, collection,
					Part2DatastreamSupport.JSON_MEDIA);
			if (evidence.isEmpty()) {
				continue;
			}
			supportedCollections++;
			URI endpoint = collectionItemsUri(collection);
			if (observation) {
				Part2DatastreamSupport.validateObservationEndpoint(endpoint, evidence.orElseThrow().pages(),
						requirement);
			}
			else {
				Part2DatastreamSupport.validateDatastreamEndpoint(endpoint, evidence.orElseThrow().pages(),
						requirement);
			}
		}
		if (supportedCollections == 0) {
			throw new SkipException(requirement + " - every advertised " + itemType
					+ " collection lacked a supported rel=items application/json link.");
		}
	}

	private void validateCanonicalLinksForCollections(String itemType, boolean observation, String requirement) {
		List<Map<String, Object>> collections = advertisedCollections(itemType, requirement);
		int supportedCollections = 0;
		for (Map<String, Object> collection : collections) {
			Optional<TraversalResult> evidence = Part1ApiCommonSupport.collectionItemsDetailed(this.apiRoot, collection,
					Part2DatastreamSupport.JSON_MEDIA);
			if (evidence.isEmpty()) {
				continue;
			}
			supportedCollections++;
			TraversalResult traversal = evidence.orElseThrow();
			URI endpoint = collectionItemsUri(collection);
			if (observation) {
				Part2DatastreamSupport.validateObservationEndpoint(endpoint, traversal.pages(), requirement);
			}
			else {
				Part2DatastreamSupport.validateDatastreamEndpoint(endpoint, traversal.pages(), requirement);
			}
			for (PageDocument page : traversal.pages()) {
				for (Map<String, Object> item : page.items()) {
					URI canonical = Part2DatastreamSupport.canonicalUri(item, page.source(), this.apiRoot, requirement);
					Response response = given().accept(page.mediaType()).when().get(canonical).andReturn();
					ETSAssert.assertStatus(response, 200, requirement);
					Map<String, Object> canonicalBody = Part2DatastreamSupport.parseObject(response, canonical,
							requirement);
					if (observation) {
						validateObservationSingleton(canonicalBody, requirement, canonical);
					}
					else {
						validateDatastreamSingleton(canonicalBody, requirement, canonical);
					}
					JsonNode expected = Part2DatastreamSupport.withoutCanonicalLinks(item);
					JsonNode actual = Part2DatastreamSupport.withoutCanonicalLinks(canonicalBody);
					if (!expected.equals(actual)) {
						ETSAssert.failWithUri(requirement, canonical
								+ " content differs from its collection item after canonical links are removed.");
					}
				}
			}
		}
		if (supportedCollections == 0) {
			throw new SkipException(requirement + " - every advertised " + itemType
					+ " collection lacked a supported rel=items application/json link.");
		}
	}

	private List<Map<String, Object>> advertisedCollections(String itemType, String requirement) {
		Response response = given().accept("application/json")
			.when()
			.get(this.apiRoot.resolve("collections"))
			.andReturn();
		ETSAssert.assertStatus(response, 200, requirement);
		Map<String, Object> body = Part2DatastreamSupport.parseObject(response, this.apiRoot.resolve("collections"),
				requirement);
		Object advertised = body.get("collections");
		if (!(advertised instanceof List)) {
			ETSAssert.failWithUri(requirement, "/collections response is missing a collections array.");
		}
		List<Map<String, Object>> typed = new ArrayList<>();
		for (Object value : (List<?>) advertised) {
			if (value instanceof Map) {
				typed.add(castMap(value));
			}
		}
		List<Map<String, Object>> selected = Part2DatastreamSupport.collectionsWithItemType(typed, itemType);
		if (selected.isEmpty()) {
			throw new SkipException(requirement + " - /collections does not advertise itemType=" + itemType + ".");
		}
		return selected;
	}

	private void validateDatastreamSingleton(Map<String, Object> body, String requirement, URI source) {
		Part2DatastreamSupport.validateDatastreamResource(body, requirement, source.toString());
	}

	private void validateObservationSingleton(Map<String, Object> body, String requirement, URI source) {
		Part2DatastreamSupport.validateObservationResource(body, requirement, source.toString());
	}

	private URI collectionItemsUri(Map<String, Object> collection) {
		String id = requireString(collection, "id", REQ_COLLECTIONS);
		String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8).replace("+", "%20");
		return this.apiRoot.resolve("collections/" + encodedId + "/items");
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> castMap(Object value) {
		return (Map<String, Object>) value;
	}

	private static String requireString(Map<String, Object> parsed, String key, String reqUri) {
		ETSAssert.assertJsonObjectHas(parsed, key, String.class, reqUri);
		return (String) parsed.get(key);
	}

	private static String encode(String value) {
		return Part2DatastreamSupport.encodePathToken(value);
	}

	private static boolean linksContainDatastream(Object links, String datastreamId) {
		if (links instanceof Map) {
			return linkReferencesDatastream((Map<?, ?>) links, datastreamId);
		}
		if (!(links instanceof List)) {
			return false;
		}
		for (Object link : (List<?>) links) {
			if (link instanceof Map && linkReferencesDatastream((Map<?, ?>) link, datastreamId)) {
				return true;
			}
		}
		return false;
	}

	private static boolean linkReferencesDatastream(Map<?, ?> link, String datastreamId) {
		String href = stringValue(link.get("href"));
		return href != null && href.contains("/datastreams/" + datastreamId);
	}

	private static boolean linkMatchesAnyToken(Object links, Set<String> tokens) {
		if (links instanceof Map) {
			return linkMatchesAnyToken((Map<?, ?>) links, tokens);
		}
		if (!(links instanceof List)) {
			return false;
		}
		for (Object link : (List<?>) links) {
			if (link instanceof Map && linkMatchesAnyToken((Map<?, ?>) link, tokens)) {
				return true;
			}
		}
		return false;
	}

	private static boolean linkMatchesAnyToken(Map<?, ?> link, Set<String> tokens) {
		String href = lower(stringValue(link.get("href")));
		String rel = lower(stringValue(link.get("rel")));
		String title = lower(stringValue(link.get("title")));
		return tokens.stream().anyMatch(token -> href.contains(token) || rel.contains(token) || title.contains(token));
	}

	private static boolean localLinkMatchesAnyToken(Object links, Set<String> tokens, URI apiRoot) {
		if (links instanceof Map) {
			return localLinkMatchesAnyToken((Map<?, ?>) links, tokens, apiRoot);
		}
		if (!(links instanceof List)) {
			return false;
		}
		for (Object link : (List<?>) links) {
			if (link instanceof Map && localLinkMatchesAnyToken((Map<?, ?>) link, tokens, apiRoot)) {
				return true;
			}
		}
		return false;
	}

	private static boolean localLinkMatchesAnyToken(Map<?, ?> link, Set<String> tokens, URI apiRoot) {
		String href = stringValue(link.get("href"));
		if (href == null || !linkMatchesAnyToken(link, tokens)) {
			return false;
		}
		try {
			URI resolved = apiRoot.resolve(href);
			return sameOrigin(apiRoot, resolved);
		}
		catch (IllegalArgumentException ex) {
			return false;
		}
	}

	private static boolean sameOrigin(URI expected, URI actual) {
		return expected.getScheme() != null && actual.getScheme() != null
				&& expected.getScheme().equalsIgnoreCase(actual.getScheme()) && expected.getHost() != null
				&& actual.getHost() != null && expected.getHost().equalsIgnoreCase(actual.getHost())
				&& effectivePort(expected) == effectivePort(actual);
	}

	private static int effectivePort(URI uri) {
		if (uri.getPort() >= 0) {
			return uri.getPort();
		}
		if ("https".equalsIgnoreCase(uri.getScheme())) {
			return 443;
		}
		if ("http".equalsIgnoreCase(uri.getScheme())) {
			return 80;
		}
		return -1;
	}

	private static boolean directMemberMatchesAnyToken(Map<String, Object> resource, Set<String> tokens) {
		for (Map.Entry<String, Object> entry : resource.entrySet()) {
			String key = lower(entry.getKey());
			if (tokens.stream().anyMatch(key::contains)) {
				return true;
			}
			Object value = entry.getValue();
			if (value instanceof String && tokens.stream().anyMatch(lower((String) value)::contains)) {
				return true;
			}
		}
		return false;
	}

	private static Set<String> associationTokens(String association) {
		return switch (association) {
			case "samplingFeatures" ->
				Set.of("samplingfeatures", "sampling-features", "sampling_features", "samples", "sf");
			case "featuresOfInterest", "featureOfInterest" ->
				Set.of("featuresofinterest", "featureofinterest", "features-of-interest", "feature-of-interest",
						"features_of_interest", "feature_of_interest", "fois", "foi");
			case "deployments" -> Set.of("deployments", "deployment");
			default -> Set.of(association.toLowerCase(Locale.ROOT));
		};
	}

	private static List<String> associationMemberNames(String association) {
		return switch (association) {
			case "samplingFeatures" ->
				List.of("samplingFeatures", "samplingFeatures@id", "samplingFeaturesId", "samplingFeatures@link",
						"samplingFeature", "samplingFeature@id", "samplingFeatureId", "samplingFeature@link");
			case "featuresOfInterest",
					"featureOfInterest" ->
				List.of("featuresOfInterest", "featuresOfInterest@id", "featuresOfInterestId",
						"featuresOfInterest@link", "featureOfInterest", "featureOfInterest@id", "featureOfInterestId",
						"featureOfInterest@link");
			default -> List.of(association, association + "@id", association + "Id", association + "@link");
		};
	}

	private static boolean isEmbeddedAssociationMember(String member, String association) {
		return associationMemberNames(association).contains(member) && !member.endsWith("@link")
				&& !member.endsWith("@id") && !member.endsWith("Id");
	}

	private static boolean containsInlineDescription(Object value) {
		if (value instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) value;
			return map.containsKey("id") && !map.containsKey("href");
		}
		if (!(value instanceof List)) {
			return false;
		}
		for (Object item : (List<?>) value) {
			if (containsInlineDescription(item)) {
				return true;
			}
		}
		return false;
	}

	private static String lower(String value) {
		return value == null ? "" : value.toLowerCase(Locale.ROOT);
	}

	private static String stringValue(Object value) {
		return value instanceof String ? (String) value : null;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> parseBody(Response response) {
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

	private static void skipWhenPrerequisiteUnsatisfied(ITestContext testContext) {
		String blocker = configurationBlocker(testContext.getFailedConfigurations(), "failed");
		if (blocker == null) {
			blocker = configurationBlocker(testContext.getSkippedConfigurations(), "skipped");
		}
		if (blocker == null) {
			blocker = testBlocker(testContext.getFailedTests(), "failed");
		}
		if (blocker == null) {
			blocker = testBlocker(testContext.getSkippedTests(), "skipped");
		}
		if (blocker != null) {
			throw new SkipException(
					"Part 2 Datastream setup skipped before IUT access because prerequisite " + blocker + ".");
		}
	}

	private static String configurationBlocker(IResultMap results, String status) {
		if (results == null) {
			return null;
		}
		for (ITestResult result : results.getAllResults()) {
			if (result != null && result.getMethod() != null && isPart2ApiCommonPrerequisite(result)) {
				return "configuration " + result.getMethod().getMethodName() + " " + status;
			}
		}
		return null;
	}

	private static String testBlocker(IResultMap results, String status) {
		if (results == null) {
			return null;
		}
		for (ITestResult result : results.getAllResults()) {
			if (result != null && result.getMethod() != null && isPart2ApiCommonPrerequisite(result)) {
				return "method " + result.getMethod().getMethodName() + " " + status;
			}
		}
		return null;
	}

	private static boolean isPart2ApiCommonPrerequisite(ITestResult result) {
		for (String group : result.getMethod().getGroups()) {
			if ("part2apicommon".equals(group)) {
				return true;
			}
		}
		return Part2ApiCommonTests.class.equals(result.getMethod().getRealClass());
	}

}
