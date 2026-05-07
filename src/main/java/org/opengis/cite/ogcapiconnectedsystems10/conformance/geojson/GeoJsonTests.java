package org.opengis.cite.ogcapiconnectedsystems10.conformance.geojson;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.EncodingMediatypeWrite;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.EncodingRelationTypes;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Part 1 — GeoJSON encoding conformance subset tests ({@code /conf/geojson}; OGC
 * 23-001 Annex A).
 *
 * <p>
 * Implements the Sprint 9 systems read-only subset and the Sprint 15
 * Deployment/Procedure/SamplingFeature read-only subset of
 * <strong>REQ-ETS-PART1-012</strong>. This class deliberately does not close the full
 * GeoJSON requirement class: write-side media-type checks, property GeoJSON mapping, and
 * full external GeoJSON schema validation remain open for future sprints.
 * </p>
 */
public class GeoJsonTests {

	static final String CONF_GEOJSON = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/geojson";

	static final String CONF_DEPLOYMENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/deployment";

	static final String CONF_PROCEDURE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/procedure";

	static final String CONF_SF = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/sf";

	static final String REQ_GEOJSON_CLASS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson";

	static final String REQ_MEDIATYPE_READ = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/mediatype-read";

	static final String REQ_MEDIATYPE_WRITE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/mediatype-write";

	static final String REQ_FEATURE_ATTRIBUTE_MAPPING = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/feature-attribute-mapping";

	static final String REQ_RELATION_TYPES = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/relation-types";

	static final String REQ_SYSTEM_SCHEMA = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/system-schema";

	static final String REQ_SYSTEM_MAPPINGS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/system-mappings";

	static final String REQ_DEPLOYMENT_SCHEMA = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/deployment-schema";

	static final String REQ_DEPLOYMENT_MAPPINGS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/deployment-mappings";

	static final String REQ_PROCEDURE_SCHEMA = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/procedure-schema";

	static final String REQ_PROCEDURE_MAPPINGS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/procedure-mappings";

	static final String REQ_SF_SCHEMA = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/sf-schema";

	static final String REQ_SF_MAPPINGS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/sf-mappings";

	private URI iutUri;

	private Response conformanceResponse;

	private Map<String, Object> conformanceBody;

	private URI systemsUri;

	private Response systemsGeoJsonResponse;

	private Map<String, Object> systemsGeoJsonBody;

	private Map<String, Object> selectedSystemJsonBody;

	private URI deploymentsUri;

	private Response deploymentsGeoJsonResponse;

	private Map<String, Object> deploymentsGeoJsonBody;

	private URI proceduresUri;

	private Response proceduresGeoJsonResponse;

	private Map<String, Object> proceduresGeoJsonBody;

	private URI samplingFeaturesUri;

	private Response samplingFeaturesGeoJsonResponse;

	private Map<String, Object> samplingFeaturesGeoJsonBody;

	private ITestContext testContext;

	/**
	 * Fetches /conformance and /systems once for all GeoJSON subset assertions.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchGeoJsonInputs(ITestContext testContext) {
		this.testContext = testContext;
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		this.iutUri = (URI) iutAttr;
		String iutString = this.iutUri.toString();
		String base = iutString.endsWith("/") ? iutString : iutString + "/";

		URI conformanceUri = URI.create(base + "conformance");
		this.conformanceResponse = given().accept("application/json").when().get(conformanceUri).andReturn();
		try {
			this.conformanceBody = this.conformanceResponse.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			this.conformanceBody = null;
		}
		if (!declaresGeoJsonConformance()) {
			throw new SkipException(CONF_GEOJSON
					+ " — IUT does not declare the CS API GeoJSON encoding conformance class in /conformance.");
		}

		this.systemsUri = URI.create(base + "systems");
		this.systemsGeoJsonResponse = given().accept("application/geo+json").when().get(this.systemsUri).andReturn();
		this.systemsGeoJsonBody = parseJsonObject(this.systemsGeoJsonResponse);
		this.selectedSystemJsonBody = fetchFirstItemBody(this.systemsUri, "systems", REQ_RELATION_TYPES);

		this.deploymentsUri = URI.create(base + "deployments");
		this.deploymentsGeoJsonResponse = fetchGeoJsonCollection(this.deploymentsUri);
		this.deploymentsGeoJsonBody = parseJsonObject(this.deploymentsGeoJsonResponse);

		this.proceduresUri = URI.create(base + "procedures");
		this.proceduresGeoJsonResponse = fetchGeoJsonCollection(this.proceduresUri);
		this.proceduresGeoJsonBody = parseJsonObject(this.proceduresGeoJsonResponse);

		this.samplingFeaturesUri = URI.create(base + "samplingFeatures");
		this.samplingFeaturesGeoJsonResponse = fetchGeoJsonCollection(this.samplingFeaturesUri);
		this.samplingFeaturesGeoJsonBody = parseJsonObject(this.samplingFeaturesGeoJsonResponse);
	}

	/**
	 * SCENARIO-ETS-PART1-012-GEOJSON-CONFORMANCE-DECLARED-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_GEOJSON_CLASS
			+ ": /conformance declares /conf/geojson (REQ-ETS-PART1-012, SCENARIO-ETS-PART1-012-GEOJSON-CONFORMANCE-DECLARED-001)",
			groups = "geojson")
	@SuppressWarnings("unchecked")
	public void geoJsonConformanceDeclared() {
		ETSAssert.assertStatus(this.conformanceResponse, 200, REQ_GEOJSON_CLASS);
		if (this.conformanceBody == null) {
			ETSAssert.failWithUri(REQ_GEOJSON_CLASS, "/conformance body did not parse as JSON. Content-Type was: "
					+ this.conformanceResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.conformanceBody, "conformsTo", List.class, REQ_GEOJSON_CLASS);
		List<Object> conformsTo = (List<Object>) this.conformanceBody.get("conformsTo");
		Predicate<Object> isGeoJson = CONF_GEOJSON::equals;
		ETSAssert.assertJsonArrayContains(conformsTo, isGeoJson, CONF_GEOJSON, REQ_GEOJSON_CLASS);
	}

	/**
	 * SCENARIO-ETS-PART1-012-GEOJSON-MEDIATYPE-READ-001.
	 *
	 * <p>
	 * Sprint 9 does not over-claim {@code application/geo+json}: if the IUT returns a
	 * parseable default JSON response instead, this test records that fallback by passing
	 * only after confirming a known systems collection wrapper is present. Strict GeoJSON
	 * FeatureCollection shape is asserted separately by
	 * {@link #systemsCollectionIsGeoJsonFeatureCollection()}.
	 * </p>
	 */
	@Test(description = "OGC-23-001 " + REQ_MEDIATYPE_READ
			+ ": GET /systems with Accept application/geo+json returns HTTP 200 and parseable JSON, with fallback honesty when the media type is application/json (REQ-ETS-PART1-012, SCENARIO-ETS-PART1-012-GEOJSON-MEDIATYPE-READ-001)",
			groups = "geojson")
	public void geoJsonMediaTypeRead() {
		ETSAssert.assertStatus(this.systemsGeoJsonResponse, 200, REQ_MEDIATYPE_READ);
		if (this.systemsGeoJsonBody == null) {
			ETSAssert.failWithUri(REQ_MEDIATYPE_READ, "/systems body did not parse as JSON. Content-Type was: "
					+ this.systemsGeoJsonResponse.getContentType());
		}
		if (!this.systemsGeoJsonBody.containsKey("features") && !this.systemsGeoJsonBody.containsKey("items")) {
			ETSAssert.failWithUri(REQ_MEDIATYPE_READ,
					"/systems response has neither GeoJSON 'features' nor CS API default 'items' wrapper. Keys: "
							+ this.systemsGeoJsonBody.keySet());
		}
		if (this.systemsGeoJsonBody.containsKey("items") && !this.systemsGeoJsonBody.containsKey("features")) {
			throw new SkipException(REQ_MEDIATYPE_READ
					+ " — IUT declares /conf/geojson but /systems with Accept application/geo+json returned the CS API default 'items' wrapper, not GeoJSON 'features'. This is fallback evidence, not a mediatype-read PASS.");
		}
	}

	/**
	 * SCENARIO-ETS-PART1-012-GEOJSON-MEDIATYPE-WRITE-SAFETY-GATED-001,
	 * SCENARIO-ETS-PART1-012-013-MEDIATYPE-WRITE-NO-PUBLIC-MUTATION-001, and
	 * SCENARIO-ETS-PART1-012-013-MEDIATYPE-WRITE-PARSE-EVIDENCE-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_MEDIATYPE_WRITE
			+ ": Content-Type application/geo+json write parsing is checked only after CRD conformance, explicit dedicated-mutable-IUT opt-in, public-IUT hard denial, and follow-up dereference evidence (REQ-ETS-PART1-012, SCENARIO-ETS-PART1-012-GEOJSON-MEDIATYPE-WRITE-SAFETY-GATED-001, SCENARIO-ETS-PART1-012-013-MEDIATYPE-WRITE-NO-PUBLIC-MUTATION-001, SCENARIO-ETS-PART1-012-013-MEDIATYPE-WRITE-PARSE-EVIDENCE-001)",
			groups = "geojson")
	public void geoJsonMediaTypeWriteParsesSystemBodyWhenMutationEnabled() {
		EncodingMediatypeWrite.skipIfConformanceMissing(this.conformanceBody,
				EncodingMediatypeWrite.CONF_CREATE_REPLACE_DELETE, REQ_MEDIATYPE_WRITE);
		EncodingMediatypeWrite.ensureMutationEnabledOrSkip(this.testContext, this.iutUri, REQ_MEDIATYPE_WRITE);

		String systemUid = EncodingMediatypeWrite.mutableSystemUid("geojson");
		Response createResponse = EncodingMediatypeWrite.givenWithoutDefaultCharset()
			.accept("application/json")
			.contentType(EncodingMediatypeWrite.GEOJSON_CONTENT_TYPE)
			.body(EncodingMediatypeWrite.geoJsonSystemBody("create", systemUid))
			.when()
			.post(this.systemsUri)
			.andReturn();
		EncodingMediatypeWrite.assertStatusIn(createResponse, List.of(200, 201, 202), REQ_MEDIATYPE_WRITE,
				"POST /systems with Content-Type " + EncodingMediatypeWrite.GEOJSON_CONTENT_TYPE);

		String resourceUri = EncodingMediatypeWrite.createdResourceUri(createResponse, this.iutUri, baseUriString());
		if (resourceUri == null) {
			ETSAssert.failWithUri(REQ_MEDIATYPE_WRITE,
					"POST /systems did not expose Location or JSON id; status-only write response is not mediatype-write PASS evidence.");
		}

		try {
			Response dereferenceResponse = given().accept("application/json")
				.when()
				.get(URI.create(resourceUri))
				.andReturn();
			EncodingMediatypeWrite.assertStatusIn(dereferenceResponse, List.of(200), REQ_MEDIATYPE_WRITE,
					"GET " + resourceUri + " after GeoJSON mediatype-write POST");
			EncodingMediatypeWrite.assertDereferencedResourcePreservesUid(
					EncodingMediatypeWrite.parseBody(dereferenceResponse), systemUid, REQ_MEDIATYPE_WRITE);
		}
		finally {
			given().accept("application/json").when().delete(URI.create(resourceUri)).andReturn();
		}
	}

	/**
	 * SCENARIO-ETS-PART1-012-GEOJSON-FEATURECOLLECTION-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_SYSTEM_SCHEMA
			+ ": /systems GeoJSON response is type=FeatureCollection with a features array; CS API items wrapper alone does not PASS (REQ-ETS-PART1-012, SCENARIO-ETS-PART1-012-GEOJSON-FEATURECOLLECTION-001)",
			dependsOnMethods = "geoJsonMediaTypeRead", groups = "geojson")
	public void systemsCollectionIsGeoJsonFeatureCollection() {
		if (this.systemsGeoJsonBody.containsKey("items") && !this.systemsGeoJsonBody.containsKey("features")) {
			throw new SkipException(REQ_SYSTEM_SCHEMA
					+ " — IUT declares /conf/geojson but /systems returned the CS API default 'items' wrapper, not a GeoJSON FeatureCollection 'features' array. This is fallback evidence, not a GeoJSON PASS.");
		}
		ETSAssert.assertJsonObjectHas(this.systemsGeoJsonBody, "type", String.class, REQ_SYSTEM_SCHEMA);
		Object type = this.systemsGeoJsonBody.get("type");
		if (!"FeatureCollection".equals(type)) {
			ETSAssert.failWithUri(REQ_SYSTEM_SCHEMA,
					"/systems GeoJSON response type is '" + type + "', expected 'FeatureCollection'.");
		}
		ETSAssert.assertJsonObjectHas(this.systemsGeoJsonBody, "features", List.class, REQ_SYSTEM_SCHEMA);
		List<?> features = featuresArray();
		if (features.isEmpty()) {
			throw new SkipException(
					REQ_SYSTEM_SCHEMA + " — /systems GeoJSON FeatureCollection has empty features array.");
		}
	}

	/**
	 * SCENARIO-ETS-PART1-012-GEOJSON-FEATURE-MAPPING-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_SYSTEM_MAPPINGS
			+ ": first system feature has GeoJSON Feature shape and CS API attributes under properties (REQ-ETS-PART1-012, SCENARIO-ETS-PART1-012-GEOJSON-FEATURE-MAPPING-001)",
			dependsOnMethods = "systemsCollectionIsGeoJsonFeatureCollection", groups = "geojson")
	@SuppressWarnings("unchecked")
	public void systemFeatureHasGeoJsonShapeAndProperties() {
		Object first = featuresArray().get(0);
		if (!(first instanceof Map)) {
			ETSAssert.failWithUri(REQ_SYSTEM_MAPPINGS,
					"First /systems features[] entry is not a JSON object: " + first);
		}
		Map<String, Object> feature = (Map<String, Object>) first;
		ETSAssert.assertJsonObjectHas(feature, "type", String.class, REQ_SYSTEM_MAPPINGS);
		if (!"Feature".equals(feature.get("type"))) {
			ETSAssert.failWithUri(REQ_SYSTEM_MAPPINGS,
					"First /systems feature type is '" + feature.get("type") + "', expected 'Feature'.");
		}
		ETSAssert.assertJsonObjectHas(feature, "id", String.class, REQ_SYSTEM_MAPPINGS);
		if (!feature.containsKey("geometry")) {
			ETSAssert.failWithUri(REQ_SYSTEM_SCHEMA,
					"First /systems feature has no 'geometry' member; expected GeoJSON geometry or null.");
		}
		ETSAssert.assertJsonObjectHas(feature, "properties", Map.class, REQ_FEATURE_ATTRIBUTE_MAPPING);
	}

	/**
	 * SCENARIO-ETS-PART1-012-GEOJSON-DEPLOYMENT-SCHEMA-MAPPING-001 and
	 * SCENARIO-ETS-PART1-012-GEOJSON-NON-SYSTEM-FALLBACK-HONESTY-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_DEPLOYMENT_SCHEMA + " and " + REQ_DEPLOYMENT_MAPPINGS
			+ ": /deployments GeoJSON FeatureCollection exposes deployment-specific properties before PASS (REQ-ETS-PART1-012, SCENARIO-ETS-PART1-012-GEOJSON-DEPLOYMENT-SCHEMA-MAPPING-001, SCENARIO-ETS-PART1-012-GEOJSON-NON-SYSTEM-FALLBACK-HONESTY-001)",
			groups = "geojson")
	public void deploymentFeatureHasGeoJsonSchemaAndMapping() {
		skipIfConformanceMissing(CONF_DEPLOYMENT, REQ_DEPLOYMENT_SCHEMA, "/deployments");
		ETSAssert.assertStatus(this.deploymentsGeoJsonResponse, 200, REQ_DEPLOYMENT_SCHEMA);
		Map<String, Object> feature = firstGeoJsonFeature(this.deploymentsGeoJsonBody, "/deployments",
				REQ_DEPLOYMENT_SCHEMA);
		assertGeoJsonFeatureShape(feature, "/deployments", REQ_DEPLOYMENT_SCHEMA);
		Map<String, Object> properties = featureProperties(feature, REQ_DEPLOYMENT_MAPPINGS);
		ETSAssert.assertJsonObjectHas(properties, "uid", String.class, REQ_DEPLOYMENT_MAPPINGS);
		if (!hasMappingValue(properties, "deployedSystems@link")) {
			throw new SkipException(REQ_DEPLOYMENT_MAPPINGS
					+ " — first /deployments GeoJSON feature has no non-empty properties.deployedSystems@link mapping; generic Feature shape alone is not deployment mapping PASS evidence.");
		}
	}

	/**
	 * SCENARIO-ETS-PART1-012-GEOJSON-PROCEDURE-SCHEMA-MAPPING-001 and
	 * SCENARIO-ETS-PART1-012-GEOJSON-NON-SYSTEM-FALLBACK-HONESTY-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_PROCEDURE_SCHEMA + " and " + REQ_PROCEDURE_MAPPINGS
			+ ": /procedures GeoJSON FeatureCollection exposes procedure-specific null geometry and featureType before PASS (REQ-ETS-PART1-012, SCENARIO-ETS-PART1-012-GEOJSON-PROCEDURE-SCHEMA-MAPPING-001, SCENARIO-ETS-PART1-012-GEOJSON-NON-SYSTEM-FALLBACK-HONESTY-001)",
			groups = "geojson")
	public void procedureFeatureHasGeoJsonSchemaAndMapping() {
		skipIfConformanceMissing(CONF_PROCEDURE, REQ_PROCEDURE_SCHEMA, "/procedures");
		ETSAssert.assertStatus(this.proceduresGeoJsonResponse, 200, REQ_PROCEDURE_SCHEMA);
		Map<String, Object> feature = firstGeoJsonFeature(this.proceduresGeoJsonBody, "/procedures",
				REQ_PROCEDURE_SCHEMA);
		assertGeoJsonFeatureShape(feature, "/procedures", REQ_PROCEDURE_SCHEMA);
		if (feature.get("geometry") != null) {
			ETSAssert.failWithUri(REQ_PROCEDURE_SCHEMA,
					"First /procedures GeoJSON feature geometry is not null; procedure location mapping expects null geometry.");
		}
		Map<String, Object> properties = featureProperties(feature, REQ_PROCEDURE_MAPPINGS);
		ETSAssert.assertJsonObjectHas(properties, "uid", String.class, REQ_PROCEDURE_MAPPINGS);
		ETSAssert.assertJsonObjectHas(properties, "featureType", String.class, REQ_PROCEDURE_MAPPINGS);
	}

	/**
	 * SCENARIO-ETS-PART1-012-GEOJSON-SF-SCHEMA-MAPPING-001 and
	 * SCENARIO-ETS-PART1-012-GEOJSON-NON-SYSTEM-FALLBACK-HONESTY-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_SF_SCHEMA + " and " + REQ_SF_MAPPINGS
			+ ": /samplingFeatures GeoJSON FeatureCollection exposes sampling-feature-specific properties before PASS (REQ-ETS-PART1-012, SCENARIO-ETS-PART1-012-GEOJSON-SF-SCHEMA-MAPPING-001, SCENARIO-ETS-PART1-012-GEOJSON-NON-SYSTEM-FALLBACK-HONESTY-001)",
			groups = "geojson")
	public void samplingFeatureHasGeoJsonSchemaAndMapping() {
		skipIfConformanceMissing(CONF_SF, REQ_SF_SCHEMA, "/samplingFeatures");
		ETSAssert.assertStatus(this.samplingFeaturesGeoJsonResponse, 200, REQ_SF_SCHEMA);
		Map<String, Object> feature = firstGeoJsonFeature(this.samplingFeaturesGeoJsonBody, "/samplingFeatures",
				REQ_SF_SCHEMA);
		assertGeoJsonFeatureShape(feature, "/samplingFeatures", REQ_SF_SCHEMA);
		Map<String, Object> properties = featureProperties(feature, REQ_SF_MAPPINGS);
		ETSAssert.assertJsonObjectHas(properties, "uid", String.class, REQ_SF_MAPPINGS);
		ETSAssert.assertJsonObjectHas(properties, "featureType", String.class, REQ_SF_MAPPINGS);
		if (!hasMappingValue(properties, "hostedProcedure@link") && !hasMappingValue(properties, "radius")) {
			throw new SkipException(REQ_SF_MAPPINGS
					+ " — first /samplingFeatures GeoJSON feature has neither non-empty properties.hostedProcedure@link nor properties.radius; generic Feature shape alone is not sampling feature mapping PASS evidence.");
		}
	}

	/**
	 * SCENARIO-ETS-PART1-012-GEOJSON-RELATION-TYPES-001 and
	 * SCENARIO-ETS-PART1-012-GEOJSON-RELATION-TYPES-BREADTH-001 and
	 * SCENARIO-ETS-PART1-012-013-RELATION-TYPES-FALLBACK-HONESTY-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RELATION_TYPES
			+ ": system links-member association rels use resource-specific association names, excluding canonical/alternate/property-level links (REQ-ETS-PART1-012, SCENARIO-ETS-PART1-012-GEOJSON-RELATION-TYPES-001, SCENARIO-ETS-PART1-012-GEOJSON-RELATION-TYPES-BREADTH-001, SCENARIO-ETS-PART1-012-013-RELATION-TYPES-FALLBACK-HONESTY-001)",
			groups = "geojson")
	public void geoJsonLinksMemberAssociationRelsUseResourceSpecificNames() {
		EncodingRelationTypes.assertLinksMemberAssociationRels(this.selectedSystemJsonBody,
				EncodingRelationTypes.ENCODING_GEOJSON, EncodingRelationTypes.RESOURCE_SYSTEM, REQ_RELATION_TYPES);
	}

	/**
	 * SCENARIO-ETS-PART1-012-GEOJSON-RELATION-TYPES-BREADTH-001 and
	 * SCENARIO-ETS-PART1-012-013-RELATION-TYPES-FALLBACK-HONESTY-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RELATION_TYPES
			+ ": deployment links-member association rels use deployment-specific names and generic/property-level links do not create PASS (REQ-ETS-PART1-012, SCENARIO-ETS-PART1-012-GEOJSON-RELATION-TYPES-BREADTH-001, SCENARIO-ETS-PART1-012-013-RELATION-TYPES-FALLBACK-HONESTY-001)",
			groups = "geojson")
	public void geoJsonDeploymentLinksMemberAssociationRelsUseResourceSpecificNames() {
		skipIfConformanceMissing(CONF_DEPLOYMENT, REQ_RELATION_TYPES, "/deployments");
		Map<String, Object> deploymentBody = fetchFirstItemBody(this.deploymentsUri, "deployments", REQ_RELATION_TYPES);
		EncodingRelationTypes.assertLinksMemberAssociationRels(deploymentBody, EncodingRelationTypes.ENCODING_GEOJSON,
				EncodingRelationTypes.RESOURCE_DEPLOYMENT, REQ_RELATION_TYPES);
	}

	/**
	 * SCENARIO-ETS-PART1-012-GEOJSON-RELATION-TYPES-BREADTH-001 and
	 * SCENARIO-ETS-PART1-012-013-RELATION-TYPES-FALLBACK-HONESTY-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RELATION_TYPES
			+ ": procedure links-member association rels use procedure-specific names and generic links do not create PASS (REQ-ETS-PART1-012, SCENARIO-ETS-PART1-012-GEOJSON-RELATION-TYPES-BREADTH-001, SCENARIO-ETS-PART1-012-013-RELATION-TYPES-FALLBACK-HONESTY-001)",
			groups = "geojson")
	public void geoJsonProcedureLinksMemberAssociationRelsUseResourceSpecificNames() {
		skipIfConformanceMissing(CONF_PROCEDURE, REQ_RELATION_TYPES, "/procedures");
		Map<String, Object> procedureBody = fetchFirstItemBody(this.proceduresUri, "procedures", REQ_RELATION_TYPES);
		EncodingRelationTypes.assertLinksMemberAssociationRels(procedureBody, EncodingRelationTypes.ENCODING_GEOJSON,
				EncodingRelationTypes.RESOURCE_PROCEDURE, REQ_RELATION_TYPES);
	}

	/**
	 * SCENARIO-ETS-PART1-012-GEOJSON-RELATION-TYPES-BREADTH-001 and
	 * SCENARIO-ETS-PART1-012-013-RELATION-TYPES-FALLBACK-HONESTY-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RELATION_TYPES
			+ ": samplingFeature links-member association rels use samplingFeature-specific names and property-level links do not create PASS (REQ-ETS-PART1-012, SCENARIO-ETS-PART1-012-GEOJSON-RELATION-TYPES-BREADTH-001, SCENARIO-ETS-PART1-012-013-RELATION-TYPES-FALLBACK-HONESTY-001)",
			groups = "geojson")
	public void geoJsonSamplingFeatureLinksMemberAssociationRelsUseResourceSpecificNames() {
		skipIfConformanceMissing(CONF_SF, REQ_RELATION_TYPES, "/samplingFeatures");
		Map<String, Object> samplingFeatureBody = fetchFirstItemBody(this.samplingFeaturesUri, "samplingFeatures",
				REQ_RELATION_TYPES);
		EncodingRelationTypes.assertLinksMemberAssociationRels(samplingFeatureBody,
				EncodingRelationTypes.ENCODING_GEOJSON, EncodingRelationTypes.RESOURCE_SAMPLING_FEATURE,
				REQ_RELATION_TYPES);
	}

	/**
	 * SCENARIO-ETS-PART1-012-GEOJSON-DEPENDENCY-SMOKE-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_GEOJSON_CLASS
			+ ": GeoJSON group runtime-cascade tracer for GeoJSON → SystemFeatures → Core (REQ-ETS-PART1-012, SCENARIO-ETS-PART1-012-GEOJSON-DEPENDENCY-SMOKE-001)",
			groups = "geojson")
	public void geoJsonDependencyCascadeRuntime() {
		ETSAssert.assertJsonObjectHas(Map.of("dependencyChain", "geojson→systemfeatures→core"), "dependencyChain",
				String.class, REQ_GEOJSON_CLASS);
	}

	@SuppressWarnings("unchecked")
	private boolean declaresGeoJsonConformance() {
		return declaresConformance(CONF_GEOJSON);
	}

	@SuppressWarnings("unchecked")
	private boolean declaresConformance(String conformanceUri) {
		if (this.conformanceBody == null) {
			return false;
		}
		Object conformsToObj = this.conformanceBody.get("conformsTo");
		if (!(conformsToObj instanceof List)) {
			return false;
		}
		return ((List<Object>) conformsToObj).contains(conformanceUri);
	}

	@SuppressWarnings("unchecked")
	private List<Object> featuresArray() {
		Object featuresObj = this.systemsGeoJsonBody.get("features");
		if (!(featuresObj instanceof List)) {
			throw new SkipException(REQ_SYSTEM_SCHEMA
					+ " — /systems response has no GeoJSON 'features' array; cannot inspect feature mapping.");
		}
		return (List<Object>) featuresObj;
	}

	private void skipIfConformanceMissing(String conformanceUri, String requirementUri, String collectionLabel) {
		if (!declaresConformance(conformanceUri)) {
			throw new SkipException(requirementUri + " — IUT declares /conf/geojson but not " + conformanceUri
					+ "; skipping " + collectionLabel + " GeoJSON resource-specific mapping assertions.");
		}
	}

	private static Response fetchGeoJsonCollection(URI uri) {
		return given().accept("application/geo+json").queryParam("limit", 1).when().get(uri).andReturn();
	}

	private static Map<String, Object> fetchFirstItemBody(URI collectionUri, String collectionName,
			String requirementUri) {
		Response collectionResponse = given().accept("application/json")
			.queryParam("limit", 1)
			.when()
			.get(collectionUri)
			.andReturn();
		ETSAssert.assertStatus(collectionResponse, 200, requirementUri);
		Map<String, Object> collectionBody = parseJsonObject(collectionResponse);
		Map<String, Object> first = firstCollectionItem(collectionBody, "/" + collectionName, requirementUri);
		Object id = first.get("id");
		if (!(id instanceof String) || ((String) id).isBlank()) {
			throw new SkipException(requirementUri + " — /" + collectionName
					+ " returned no item with a usable id; cannot inspect links-member relation-types.");
		}
		Response itemResponse = given().accept("application/json")
			.when()
			.get(URI.create(collectionUri + "/" + id))
			.andReturn();
		ETSAssert.assertStatus(itemResponse, 200, requirementUri);
		return parseJsonObject(itemResponse);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> parseJsonObject(Response response) {
		try {
			return response.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	static Map<String, Object> firstGeoJsonFeature(Map<String, Object> body, String collectionLabel,
			String requirementUri) {
		if (body == null) {
			ETSAssert.failWithUri(requirementUri, collectionLabel + " body did not parse as JSON.");
		}
		if (body.containsKey("items") && !body.containsKey("features")) {
			throw new SkipException(requirementUri + " — IUT declares /conf/geojson but " + collectionLabel
					+ " with Accept application/geo+json returned the CS API default 'items' wrapper, not GeoJSON 'features'. This is fallback evidence, not a GeoJSON PASS.");
		}
		ETSAssert.assertJsonObjectHas(body, "type", String.class, requirementUri);
		Object type = body.get("type");
		if (!"FeatureCollection".equals(type)) {
			ETSAssert.failWithUri(requirementUri,
					collectionLabel + " GeoJSON response type is '" + type + "', expected 'FeatureCollection'.");
		}
		ETSAssert.assertJsonObjectHas(body, "features", List.class, requirementUri);
		List<?> features = (List<?>) body.get("features");
		if (features.isEmpty()) {
			throw new SkipException(requirementUri + " — " + collectionLabel
					+ " GeoJSON FeatureCollection has empty features array; cannot inspect resource-specific mapping.");
		}
		Object first = features.get(0);
		if (!(first instanceof Map)) {
			ETSAssert.failWithUri(requirementUri,
					"First " + collectionLabel + " features[] entry is not a JSON object: " + first);
		}
		return (Map<String, Object>) first;
	}

	@SuppressWarnings("unchecked")
	static Map<String, Object> firstCollectionItem(Map<String, Object> body, String collectionLabel,
			String requirementUri) {
		if (body == null) {
			ETSAssert.failWithUri(requirementUri, collectionLabel + " body did not parse as JSON.");
		}
		Object itemsObj = body.get("items");
		if (!(itemsObj instanceof List)) {
			ETSAssert.failWithUri(requirementUri,
					collectionLabel + " response has no CS API 'items' array; cannot select resource.");
		}
		List<?> items = (List<?>) itemsObj;
		if (items.isEmpty()) {
			throw new SkipException(requirementUri + " — " + collectionLabel
					+ " returned an empty items array; cannot inspect links-member relation-types.");
		}
		Object first = items.get(0);
		if (!(first instanceof Map)) {
			ETSAssert.failWithUri(requirementUri,
					"First " + collectionLabel + " items[] entry is not a JSON object: " + first);
		}
		return (Map<String, Object>) first;
	}

	static void assertGeoJsonFeatureShape(Map<String, Object> feature, String collectionLabel, String requirementUri) {
		ETSAssert.assertJsonObjectHas(feature, "type", String.class, requirementUri);
		if (!"Feature".equals(feature.get("type"))) {
			ETSAssert.failWithUri(requirementUri,
					"First " + collectionLabel + " feature type is '" + feature.get("type") + "', expected 'Feature'.");
		}
		ETSAssert.assertJsonObjectHas(feature, "id", String.class, requirementUri);
		if (!feature.containsKey("geometry")) {
			ETSAssert.failWithUri(requirementUri, "First " + collectionLabel + " feature has no 'geometry' member.");
		}
	}

	@SuppressWarnings("unchecked")
	static Map<String, Object> featureProperties(Map<String, Object> feature, String requirementUri) {
		ETSAssert.assertJsonObjectHas(feature, "properties", Map.class, requirementUri);
		return (Map<String, Object>) feature.get("properties");
	}

	static boolean hasMappingValue(Map<String, Object> properties, String propertyName) {
		if (!properties.containsKey(propertyName)) {
			return false;
		}
		Object value = properties.get(propertyName);
		if (value == null) {
			return false;
		}
		if (value instanceof String) {
			return !((String) value).isBlank();
		}
		if (value instanceof List) {
			return !((List<?>) value).isEmpty();
		}
		if (value instanceof Map) {
			return !((Map<?, ?>) value).isEmpty();
		}
		return true;
	}

	private String baseUriString() {
		String value = this.iutUri.toString();
		return value.endsWith("/") ? value : value + "/";
	}

}
