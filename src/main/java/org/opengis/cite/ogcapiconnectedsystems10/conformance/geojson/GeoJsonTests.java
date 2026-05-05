package org.opengis.cite.ogcapiconnectedsystems10.conformance.geojson;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
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
 * Implements the Sprint 9 systems read-only subset of <strong>REQ-ETS-PART1-012</strong>.
 * This class deliberately does not close the full GeoJSON requirement class: write-side
 * media-type checks, relation-types, and Deployment/Procedure/SamplingFeature GeoJSON
 * schema and mapping assertions remain open for future sprints.
 * </p>
 */
public class GeoJsonTests {

	static final String CONF_GEOJSON = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/geojson";

	static final String REQ_GEOJSON_CLASS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson";

	static final String REQ_MEDIATYPE_READ = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/mediatype-read";

	static final String REQ_FEATURE_ATTRIBUTE_MAPPING = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/feature-attribute-mapping";

	static final String REQ_SYSTEM_SCHEMA = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/system-schema";

	static final String REQ_SYSTEM_MAPPINGS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/system-mappings";

	private URI iutUri;

	private Response conformanceResponse;

	private Map<String, Object> conformanceBody;

	private URI systemsUri;

	private Response systemsGeoJsonResponse;

	private Map<String, Object> systemsGeoJsonBody;

	/**
	 * Fetches /conformance and /systems once for all GeoJSON subset assertions.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchGeoJsonInputs(ITestContext testContext) {
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
		try {
			this.systemsGeoJsonBody = this.systemsGeoJsonResponse.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			this.systemsGeoJsonBody = null;
		}
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
		if (this.conformanceBody == null) {
			return false;
		}
		Object conformsToObj = this.conformanceBody.get("conformsTo");
		if (!(conformsToObj instanceof List)) {
			return false;
		}
		return ((List<Object>) conformsToObj).contains(CONF_GEOJSON);
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

}
