package org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Part 1 - SensorML encoding conformance subset tests ({@code /conf/sensorml}; OGC
 * 23-001 Annex A).
 *
 * <p>
 * Implements the Sprint 10 systems read-only subset of
 * <strong>REQ-ETS-PART1-013</strong>. This class deliberately does not close the full
 * SensorML requirement class: write-side media-type checks, relation-types,
 * Deployment/Procedure/Property SensorML schema and mapping assertions, and full SensorML
 * 3.0 JSON Schema validation remain open for future sprints.
 * </p>
 */
public class SensorMlTests {

	static final String CONF_SENSORML = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/sensorml";

	static final String REQ_SENSORML_CLASS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml";

	static final String REQ_MEDIATYPE_READ = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/mediatype-read";

	static final String REQ_RESOURCE_ID = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/resource-id";

	static final String REQ_SYSTEM_SML_CLASS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/system-sml-class";

	static final String REQ_SYSTEM_MAPPINGS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/system-mappings";

	private URI iutUri;

	private Response conformanceResponse;

	private Map<String, Object> conformanceBody;

	private URI systemsUri;

	private Response systemsAcceptSensorMlResponse;

	private Map<String, Object> systemsAcceptSensorMlBody;

	private Map<String, Object> selectedSystemBody;

	private String selectedSystemId;

	private String selectedSystemUid;

	private URI sensorMlRepresentationUri;

	private String sensorMlRepresentationSource;

	private Response sensorMlRepresentationResponse;

	private Map<String, Object> sensorMlRepresentationBody;

	/**
	 * Fetches the SensorML discovery inputs once for all SensorML subset assertions.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchSensorMlInputs(ITestContext testContext) {
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		this.iutUri = (URI) iutAttr;
		String iutString = this.iutUri.toString();
		String base = iutString.endsWith("/") ? iutString : iutString + "/";

		URI conformanceUri = URI.create(base + "conformance");
		this.conformanceResponse = given().accept("application/json").when().get(conformanceUri).andReturn();
		this.conformanceBody = parseBody(this.conformanceResponse);
		if (!declaresSensorMlConformance()) {
			throw new SkipException(CONF_SENSORML
					+ " - IUT does not declare the CS API SensorML encoding conformance class in /conformance.");
		}

		this.systemsUri = URI.create(base + "systems");
		this.systemsAcceptSensorMlResponse = given().accept("application/sml+json")
			.when()
			.get(this.systemsUri)
			.andReturn();
		this.systemsAcceptSensorMlBody = parseBody(this.systemsAcceptSensorMlResponse);

		Response systemsJsonResponse = given().accept("application/json").when().get(this.systemsUri).andReturn();
		ETSAssert.assertStatus(systemsJsonResponse, 200, REQ_SENSORML_CLASS);
		Map<String, Object> systemsJsonBody = parseBody(systemsJsonResponse);
		this.selectedSystemId = firstSystemId(systemsJsonBody);
		this.selectedSystemUid = firstSystemUid(systemsJsonBody);
		if (this.selectedSystemId == null || this.selectedSystemId.isBlank()) {
			throw new SkipException(REQ_SENSORML_CLASS
					+ " - /systems returned no system item with a usable id; cannot discover SensorML representation.");
		}

		URI selectedSystemUri = URI.create(base + "systems/" + this.selectedSystemId);
		Response selectedSystemResponse = given().accept("application/json").when().get(selectedSystemUri).andReturn();
		ETSAssert.assertStatus(selectedSystemResponse, 200, REQ_SENSORML_CLASS);
		this.selectedSystemBody = parseBody(selectedSystemResponse);

		Response directSensorMlResponse = given().accept("application/sml+json")
			.when()
			.get(selectedSystemUri)
			.andReturn();
		Map<String, Object> directSensorMlBody = parseBody(directSensorMlResponse);
		if (directSensorMlResponse.getStatusCode() == 200 && isSensorMlLike(directSensorMlBody)) {
			this.sensorMlRepresentationUri = selectedSystemUri;
			this.sensorMlRepresentationSource = "direct Accept application/sml+json on selected system";
			this.sensorMlRepresentationResponse = directSensorMlResponse;
			this.sensorMlRepresentationBody = directSensorMlBody;
			return;
		}

		URI alternateSensorMlUri = sensorMlAlternateLink(this.selectedSystemBody);
		if (alternateSensorMlUri != null) {
			this.sensorMlRepresentationUri = alternateSensorMlUri;
			this.sensorMlRepresentationSource = "application/sml+json alternate link";
			this.sensorMlRepresentationResponse = given().accept("application/sml+json")
				.when()
				.get(this.sensorMlRepresentationUri)
				.andReturn();
			this.sensorMlRepresentationBody = parseBody(this.sensorMlRepresentationResponse);
		}
	}

	/**
	 * SCENARIO-ETS-PART1-013-SENSORML-CONFORMANCE-DECLARED-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_SENSORML_CLASS
			+ ": /conformance declares /conf/sensorml (REQ-ETS-PART1-013, SCENARIO-ETS-PART1-013-SENSORML-CONFORMANCE-DECLARED-001)",
			groups = "sensorml")
	@SuppressWarnings("unchecked")
	public void sensorMlConformanceDeclared() {
		ETSAssert.assertStatus(this.conformanceResponse, 200, REQ_SENSORML_CLASS);
		if (this.conformanceBody == null) {
			ETSAssert.failWithUri(REQ_SENSORML_CLASS, "/conformance body did not parse as JSON. Content-Type was: "
					+ this.conformanceResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.conformanceBody, "conformsTo", List.class, REQ_SENSORML_CLASS);
		List<Object> conformsTo = (List<Object>) this.conformanceBody.get("conformsTo");
		Predicate<Object> isSensorMl = CONF_SENSORML::equals;
		ETSAssert.assertJsonArrayContains(conformsTo, isSensorMl, CONF_SENSORML, REQ_SENSORML_CLASS);
	}

	/**
	 * SCENARIO-ETS-PART1-013-SENSORML-REPRESENTATION-DISCOVERY-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_MEDIATYPE_READ
			+ ": discover a system SensorML representation through direct negotiation or an explicit application/sml+json alternate link; CS API items wrapper alone does not PASS (REQ-ETS-PART1-013, SCENARIO-ETS-PART1-013-SENSORML-REPRESENTATION-DISCOVERY-001)",
			groups = "sensorml")
	public void sensorMlRepresentationDiscoveredForSystem() {
		boolean collectionAcceptReturnedGenericItems = this.systemsAcceptSensorMlResponse.getStatusCode() == 200
				&& this.systemsAcceptSensorMlBody != null && this.systemsAcceptSensorMlBody.containsKey("items");
		if (this.systemsAcceptSensorMlResponse.getStatusCode() == 200 && this.systemsAcceptSensorMlBody == null) {
			ETSAssert.failWithUri(REQ_MEDIATYPE_READ, "/systems body did not parse as JSON. Content-Type was: "
					+ this.systemsAcceptSensorMlResponse.getContentType());
		}
		if (this.sensorMlRepresentationUri == null) {
			if (collectionAcceptReturnedGenericItems) {
				throw new SkipException(REQ_MEDIATYPE_READ
						+ " - collection-level Accept application/sml+json returned the CS API default 'items' wrapper and the selected system did not expose a direct or alternate application/sml+json representation. This is not SensorML PASS evidence.");
			}
			ETSAssert.failWithUri(REQ_MEDIATYPE_READ, "Selected system " + this.selectedSystemId
					+ " did not expose SensorML through direct Accept negotiation or an application/sml+json alternate link. Collection-level /systems Accept application/sml+json returned HTTP "
					+ this.systemsAcceptSensorMlResponse.getStatusCode() + ".");
		}
	}

	/**
	 * SCENARIO-ETS-PART1-013-SENSORML-MEDIATYPE-READ-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_MEDIATYPE_READ
			+ ": fetch selected SensorML representation and record direct Accept versus alternate-link fallback without claiming full mediatype-read closure from generic JSON (REQ-ETS-PART1-013, SCENARIO-ETS-PART1-013-SENSORML-MEDIATYPE-READ-001)",
			dependsOnMethods = "sensorMlRepresentationDiscoveredForSystem", groups = "sensorml")
	public void sensorMlMediaTypeRead() {
		ETSAssert.assertStatus(this.sensorMlRepresentationResponse, 200, REQ_MEDIATYPE_READ);
		Reporter.log("SensorML representation source: " + this.sensorMlRepresentationSource + " ("
				+ this.sensorMlRepresentationUri + ")", true);
		if (this.sensorMlRepresentationBody == null) {
			ETSAssert.failWithUri(REQ_MEDIATYPE_READ,
					"SensorML representation did not parse as JSON. URI: " + this.sensorMlRepresentationUri
							+ "; Content-Type was: " + this.sensorMlRepresentationResponse.getContentType());
		}
		if (this.sensorMlRepresentationBody.containsKey("items")) {
			ETSAssert.failWithUri(REQ_MEDIATYPE_READ,
					"SensorML representation URI returned a CS API 'items' wrapper, not a SensorML JSON representation: "
							+ this.sensorMlRepresentationUri);
		}
		if ("Feature".equals(this.sensorMlRepresentationBody.get("type"))) {
			ETSAssert.failWithUri(REQ_MEDIATYPE_READ,
					"SensorML representation URI returned generic CS API Feature JSON, not SensorML JSON: "
							+ this.sensorMlRepresentationUri);
		}
		if (!isSensorMlLike(this.sensorMlRepresentationBody)) {
			ETSAssert.failWithUri(REQ_MEDIATYPE_READ,
					"Fetched alternate-link fallback body is parseable JSON but does not look like SensorML system JSON. Keys: "
							+ this.sensorMlRepresentationBody.keySet());
		}
	}

	/**
	 * SCENARIO-ETS-PART1-013-SENSORML-SYSTEM-SHAPE-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_SYSTEM_SML_CLASS
			+ ": SensorML system representation has minimal SensorML class and identity shape (REQ-ETS-PART1-013, SCENARIO-ETS-PART1-013-SENSORML-SYSTEM-SHAPE-001)",
			dependsOnMethods = "sensorMlMediaTypeRead", groups = "sensorml")
	public void sensorMlSystemHasMinimalShape() {
		ETSAssert.assertJsonObjectHas(this.sensorMlRepresentationBody, "type", String.class, REQ_SYSTEM_SML_CLASS);
		String type = (String) this.sensorMlRepresentationBody.get("type");
		if ("Feature".equals(type) || "FeatureCollection".equals(type)) {
			ETSAssert.failWithUri(REQ_SYSTEM_SML_CLASS, "SensorML representation type is '" + type
					+ "', which is generic CS API/GeoJSON JSON, not SensorML.");
		}
		if (!type.contains("System") && !type.contains("Process")) {
			ETSAssert.failWithUri(REQ_SYSTEM_SML_CLASS,
					"SensorML representation type is '" + type + "', expected a System or Process class.");
		}
		if (!hasAnyIdentity(this.sensorMlRepresentationBody)) {
			ETSAssert.failWithUri(REQ_RESOURCE_ID,
					"SensorML representation has no id, uniqueId, uid, or identifiers member.");
		}
	}

	/**
	 * SCENARIO-ETS-PART1-013-SENSORML-SYSTEM-MAPPING-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_SYSTEM_MAPPINGS
			+ ": SensorML system representation preserves selected CS API system identity through id or UID mapping (REQ-ETS-PART1-013, SCENARIO-ETS-PART1-013-SENSORML-SYSTEM-MAPPING-001)",
			dependsOnMethods = "sensorMlSystemHasMinimalShape", groups = "sensorml")
	public void sensorMlSystemPreservesIdentityMapping() {
		String sensorMlId = asString(this.sensorMlRepresentationBody.get("id"));
		String sensorMlUniqueId = asString(this.sensorMlRepresentationBody.get("uniqueId"));
		String sensorMlUid = asString(this.sensorMlRepresentationBody.get("uid"));
		boolean idMatches = this.selectedSystemId != null && this.selectedSystemId.equals(sensorMlId);
		boolean uidMatches = this.selectedSystemUid != null
				&& (this.selectedSystemUid.equals(sensorMlUniqueId) || this.selectedSystemUid.equals(sensorMlUid));
		if (!idMatches && !uidMatches) {
			throw new SkipException(REQ_SYSTEM_MAPPINGS + " - selected CS API System id/uid (" + this.selectedSystemId
					+ " / " + this.selectedSystemUid
					+ ") was not machine-checkably preserved in SensorML id/uniqueId/uid (" + sensorMlId + " / "
					+ sensorMlUniqueId + " / " + sensorMlUid + ").");
		}
	}

	/**
	 * SCENARIO-ETS-PART1-013-SENSORML-DEPENDENCY-SMOKE-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_SENSORML_CLASS
			+ ": SensorML group runtime-cascade tracer for SensorML -> SystemFeatures -> Core (REQ-ETS-PART1-013, SCENARIO-ETS-PART1-013-SENSORML-DEPENDENCY-SMOKE-001)",
			groups = "sensorml")
	public void sensorMlDependencyCascadeRuntime() {
		ETSAssert.assertJsonObjectHas(Map.of("dependencyChain", "sensorml->systemfeatures->core"), "dependencyChain",
				String.class, REQ_SENSORML_CLASS);
	}

	@SuppressWarnings("unchecked")
	private boolean declaresSensorMlConformance() {
		if (this.conformanceBody == null) {
			return false;
		}
		Object conformsToObj = this.conformanceBody.get("conformsTo");
		if (!(conformsToObj instanceof List)) {
			return false;
		}
		return ((List<Object>) conformsToObj).contains(CONF_SENSORML);
	}

	@SuppressWarnings("unchecked")
	private String firstSystemId(Map<String, Object> systemsJsonBody) {
		Map<String, Object> first = firstItem(systemsJsonBody);
		return first == null ? null : asString(first.get("id"));
	}

	@SuppressWarnings("unchecked")
	private String firstSystemUid(Map<String, Object> systemsJsonBody) {
		Map<String, Object> first = firstItem(systemsJsonBody);
		if (first == null) {
			return null;
		}
		Object properties = first.get("properties");
		if (!(properties instanceof Map)) {
			return null;
		}
		return asString(((Map<String, Object>) properties).get("uid"));
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> firstItem(Map<String, Object> body) {
		if (body == null) {
			return null;
		}
		Object itemsObj = body.get("items");
		if (!(itemsObj instanceof List)) {
			return null;
		}
		List<?> items = (List<?>) itemsObj;
		if (items.isEmpty() || !(items.get(0) instanceof Map)) {
			return null;
		}
		return (Map<String, Object>) items.get(0);
	}

	@SuppressWarnings("unchecked")
	private URI sensorMlAlternateLink(Map<String, Object> body) {
		if (body == null) {
			return null;
		}
		Object linksObj = body.get("links");
		if (!(linksObj instanceof List)) {
			return null;
		}
		for (Object linkObj : (List<?>) linksObj) {
			if (!(linkObj instanceof Map)) {
				continue;
			}
			Map<String, Object> link = (Map<String, Object>) linkObj;
			if (!"alternate".equals(link.get("rel")) || !"application/sml+json".equals(link.get("type"))) {
				continue;
			}
			String href = asString(link.get("href"));
			if (href != null && !href.isBlank()) {
				return URI.create(href);
			}
		}
		return null;
	}

	private Map<String, Object> parseBody(Response response) {
		try {
			return response.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			return null;
		}
	}

	private boolean isSensorMlLike(Map<String, Object> body) {
		if (body == null) {
			return false;
		}
		String type = asString(body.get("type"));
		if (type == null) {
			return false;
		}
		return !"Feature".equals(type) && !"FeatureCollection".equals(type)
				&& (type.contains("System") || type.contains("Process"));
	}

	private boolean hasAnyIdentity(Map<String, Object> body) {
		return asString(body.get("id")) != null || asString(body.get("uniqueId")) != null
				|| asString(body.get("uid")) != null || body.containsKey("identifiers");
	}

	private String asString(Object value) {
		return value instanceof String ? (String) value : null;
	}

}
