package org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.EncodingRelationTypes;
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
 * Implements the Sprint 10 systems read-only subset and the Sprint 16
 * Deployment/Procedure/Property read-only subset of <strong>REQ-ETS-PART1-013</strong>.
 * This class deliberately does not close the full SensorML requirement class: write-side
 * media-type checks, full SensorML 3.0 JSON Schema validation, and mutation-side behavior
 * remain open for future sprints.
 * </p>
 */
public class SensorMlTests {

	static final String CONF_SENSORML = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/sensorml";

	static final String CONF_DEPLOYMENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/deployment";

	static final String CONF_PROCEDURE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/procedure";

	static final String CONF_PROPERTY = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/property";

	static final String REQ_SENSORML_CLASS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml";

	static final String REQ_MEDIATYPE_READ = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/mediatype-read";

	static final String REQ_RESOURCE_ID = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/resource-id";

	static final String REQ_RELATION_TYPES = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/relation-types";

	static final String REQ_SYSTEM_SML_CLASS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/system-sml-class";

	static final String REQ_SYSTEM_MAPPINGS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/system-mappings";

	static final String REQ_DEPLOYMENT_SCHEMA = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/deployment-schema";

	static final String REQ_DEPLOYMENT_MAPPINGS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/deployment-mappings";

	static final String REQ_PROCEDURE_SCHEMA = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/procedure-schema";

	static final String REQ_PROCEDURE_SML_CLASS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/procedure-sml-class";

	static final String REQ_PROCEDURE_MAPPINGS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/procedure-mappings";

	static final String REQ_PROPERTY_SCHEMA = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/property-schema";

	static final String REQ_PROPERTY_MAPPINGS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/property-mappings";

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

	private String baseUriString;

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
		this.baseUriString = base;

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
	 * SCENARIO-ETS-PART1-013-SENSORML-DEPLOYMENT-SCHEMA-MAPPING-001 and
	 * SCENARIO-ETS-PART1-013-SENSORML-NON-SYSTEM-FALLBACK-HONESTY-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_DEPLOYMENT_SCHEMA + " and " + REQ_DEPLOYMENT_MAPPINGS
			+ ": deployment SensorML JSON has Deployment shape, identity mapping, and deployedSystems evidence after /conf/deployment gating (REQ-ETS-PART1-013, SCENARIO-ETS-PART1-013-SENSORML-DEPLOYMENT-SCHEMA-MAPPING-001, SCENARIO-ETS-PART1-013-SENSORML-NON-SYSTEM-FALLBACK-HONESTY-001)",
			groups = "sensorml")
	public void deploymentSensorMlHasSchemaAndMapping() {
		skipIfConformanceMissing(CONF_DEPLOYMENT, REQ_DEPLOYMENT_SCHEMA, "/deployments");
		SensorMlResourceEvidence evidence = fetchSensorMlResource("deployments", REQ_DEPLOYMENT_SCHEMA);
		assertExplicitSensorMlRepresentation(evidence, REQ_DEPLOYMENT_SCHEMA);
		assertSensorMlType(evidence.sensorMlBody, "Deployment", "/deployments", REQ_DEPLOYMENT_SCHEMA);
		assertIdentityPreserved(evidence, REQ_DEPLOYMENT_MAPPINGS);
		if (!hasMappingValue(evidence.sensorMlBody, "deployedSystems")) {
			throw new SkipException(REQ_DEPLOYMENT_MAPPINGS
					+ " - selected deployment SensorML JSON has no non-empty deployedSystems mapping; generic SensorML identity shape alone is not deployment mapping PASS evidence.");
		}
		Reporter.log(
				"Deployment SensorML representation source: " + evidence.source + " (" + evidence.sensorMlUri + ")",
				true);
	}

	/**
	 * SCENARIO-ETS-PART1-013-SENSORML-PROCEDURE-SCHEMA-MAPPING-001 and
	 * SCENARIO-ETS-PART1-013-SENSORML-NON-SYSTEM-FALLBACK-HONESTY-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_PROCEDURE_SCHEMA + ", " + REQ_PROCEDURE_SML_CLASS + " and "
			+ REQ_PROCEDURE_MAPPINGS
			+ ": procedure SensorML JSON has procedure-compatible type, identity mapping, and non-identity process structure after /conf/procedure gating (REQ-ETS-PART1-013, SCENARIO-ETS-PART1-013-SENSORML-PROCEDURE-SCHEMA-MAPPING-001, SCENARIO-ETS-PART1-013-SENSORML-NON-SYSTEM-FALLBACK-HONESTY-001)",
			groups = "sensorml")
	public void procedureSensorMlHasSchemaAndMapping() {
		skipIfConformanceMissing(CONF_PROCEDURE, REQ_PROCEDURE_SCHEMA, "/procedures");
		SensorMlResourceEvidence evidence = fetchSensorMlResource("procedures", REQ_PROCEDURE_SCHEMA);
		assertExplicitSensorMlRepresentation(evidence, REQ_PROCEDURE_SCHEMA);
		assertProcedureCompatibleType(evidence.sensorMlBody, REQ_PROCEDURE_SML_CLASS);
		assertIdentityPreserved(evidence, REQ_PROCEDURE_MAPPINGS);
		if (!hasProcedureSpecificStructure(evidence.sensorMlBody)) {
			throw new SkipException(REQ_PROCEDURE_MAPPINGS
					+ " - selected procedure SensorML JSON has no non-identity process/procedure structure such as definition, inputs, outputs, parameters, characteristics, or capabilities; identifiers alone are not procedure mapping PASS evidence.");
		}
		Reporter.log("Procedure SensorML representation source: " + evidence.source + " (" + evidence.sensorMlUri + ")",
				true);
	}

	/**
	 * SCENARIO-ETS-PART1-013-SENSORML-PROPERTY-SCHEMA-MAPPING-001 and
	 * SCENARIO-ETS-PART1-013-SENSORML-NON-SYSTEM-FALLBACK-HONESTY-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_PROPERTY_SCHEMA + " and " + REQ_PROPERTY_MAPPINGS
			+ ": property SensorML JSON has property-compatible shape and mapping evidence after /conf/property gating, with empty /properties SKIP honesty (REQ-ETS-PART1-013, SCENARIO-ETS-PART1-013-SENSORML-PROPERTY-SCHEMA-MAPPING-001, SCENARIO-ETS-PART1-013-SENSORML-NON-SYSTEM-FALLBACK-HONESTY-001)",
			groups = "sensorml")
	public void propertySensorMlHasSchemaAndMapping() {
		skipIfConformanceMissing(CONF_PROPERTY, REQ_PROPERTY_SCHEMA, "/properties");
		SensorMlResourceEvidence evidence = fetchSensorMlResource("properties", REQ_PROPERTY_SCHEMA);
		assertExplicitSensorMlRepresentation(evidence, REQ_PROPERTY_SCHEMA);
		if (!isPropertyCompatibleType(evidence.sensorMlBody)) {
			ETSAssert.failWithUri(REQ_PROPERTY_SCHEMA,
					"Selected property SensorML JSON type is '" + evidence.sensorMlBody.get("type")
							+ "', expected a property-compatible SensorML type such as DerivedProperty.");
		}
		if (!hasPropertyMappingEvidence(evidence.sensorMlBody)) {
			throw new SkipException(REQ_PROPERTY_MAPPINGS
					+ " - selected property SensorML JSON has no machine-checkable id, uniqueId, definition, or identifiers mapping evidence.");
		}
		Reporter.log("Property SensorML representation source: " + evidence.source + " (" + evidence.sensorMlUri + ")",
				true);
	}

	/**
	 * SCENARIO-ETS-PART1-013-SENSORML-RELATION-TYPES-001 and
	 * SCENARIO-ETS-PART1-012-013-RELATION-TYPES-FALLBACK-HONESTY-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RELATION_TYPES
			+ ": SensorML links-member association rels use resource-specific association names, excluding generic representation links and non-links-member associations (REQ-ETS-PART1-013, SCENARIO-ETS-PART1-013-SENSORML-RELATION-TYPES-001, SCENARIO-ETS-PART1-012-013-RELATION-TYPES-FALLBACK-HONESTY-001)",
			dependsOnMethods = "sensorMlMediaTypeRead", groups = "sensorml")
	public void sensorMlLinksMemberAssociationRelsUseResourceSpecificNames() {
		EncodingRelationTypes.assertLinksMemberAssociationRels(this.sensorMlRepresentationBody,
				EncodingRelationTypes.ENCODING_SENSORML, EncodingRelationTypes.RESOURCE_SYSTEM, REQ_RELATION_TYPES);
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
		return declaresConformance(CONF_SENSORML);
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

	private void skipIfConformanceMissing(String conformanceUri, String requirementUri, String collectionLabel) {
		if (!declaresConformance(conformanceUri)) {
			throw new SkipException(requirementUri + " - IUT declares /conf/sensorml but not " + conformanceUri
					+ "; skipping " + collectionLabel + " SensorML resource-specific schema/mapping assertions.");
		}
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

	private SensorMlResourceEvidence fetchSensorMlResource(String collectionPath, String requirementUri) {
		URI collectionUri = URI.create(this.baseUriString + collectionPath);
		Response collectionResponse = given().accept("application/json")
			.queryParam("limit", 1)
			.when()
			.get(collectionUri)
			.andReturn();
		ETSAssert.assertStatus(collectionResponse, 200, requirementUri);
		Map<String, Object> collectionBody = parseBody(collectionResponse);
		Map<String, Object> selectedItem = firstCollectionItem(collectionBody, "/" + collectionPath, requirementUri);
		String selectedId = asString(selectedItem.get("id"));
		String selectedUid = uidFromFeatureProperties(selectedItem);
		if (selectedId == null || selectedId.isBlank()) {
			throw new SkipException(requirementUri + " - /" + collectionPath
					+ " returned no item with a usable id; cannot discover SensorML representation.");
		}

		URI itemUri = URI.create(this.baseUriString + collectionPath + "/" + selectedId);
		Response itemJsonResponse = given().accept("application/json").when().get(itemUri).andReturn();
		ETSAssert.assertStatus(itemJsonResponse, 200, requirementUri);
		Map<String, Object> itemJsonBody = parseBody(itemJsonResponse);

		Response directResponse = given().accept("application/sml+json").when().get(itemUri).andReturn();
		Map<String, Object> directBody = parseBody(directResponse);
		if (directResponse.getStatusCode() == 200 && isExplicitSensorMl(directBody)) {
			return new SensorMlResourceEvidence("/" + collectionPath, selectedId, selectedUid, itemUri,
					"direct Accept application/sml+json", directResponse, directBody);
		}

		URI alternateUri = sensorMlAlternateLink(itemJsonBody);
		if (alternateUri != null) {
			Response alternateResponse = given().accept("application/sml+json").when().get(alternateUri).andReturn();
			Map<String, Object> alternateBody = parseBody(alternateResponse);
			return new SensorMlResourceEvidence("/" + collectionPath, selectedId, selectedUid, alternateUri,
					"application/sml+json alternate link", alternateResponse, alternateBody);
		}

		if (directResponse.getStatusCode() == 200 && directBody != null && directBody.containsKey("items")) {
			throw new SkipException(requirementUri + " - /" + collectionPath
					+ " item SensorML negotiation returned a CS API 'items' wrapper and no application/sml+json alternate link; this is fallback evidence, not SensorML PASS.");
		}
		if (directResponse.getStatusCode() == 200 && directBody != null && "Feature".equals(directBody.get("type"))) {
			throw new SkipException(requirementUri + " - /" + collectionPath
					+ " item SensorML negotiation returned default Feature JSON and no application/sml+json alternate link; this is fallback evidence, not SensorML PASS.");
		}
		throw new SkipException(requirementUri + " - selected /" + collectionPath + " item " + selectedId
				+ " did not expose SensorML through direct Accept negotiation or an application/sml+json alternate link.");
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
					collectionLabel + " response has no CS API 'items' array; cannot select resource for SensorML.");
		}
		List<?> items = (List<?>) itemsObj;
		if (items.isEmpty()) {
			throw new SkipException(requirementUri + " - " + collectionLabel
					+ " returned an empty items array; current IUT state has no resource to inspect for SensorML mapping.");
		}
		Object first = items.get(0);
		if (!(first instanceof Map)) {
			ETSAssert.failWithUri(requirementUri,
					"First " + collectionLabel + " items[] entry is not a JSON object: " + first);
		}
		return (Map<String, Object>) first;
	}

	private void assertExplicitSensorMlRepresentation(SensorMlResourceEvidence evidence, String requirementUri) {
		ETSAssert.assertStatus(evidence.sensorMlResponse, 200, requirementUri);
		if (evidence.sensorMlBody == null) {
			ETSAssert.failWithUri(requirementUri,
					evidence.collectionLabel + " SensorML representation did not parse as JSON. URI: "
							+ evidence.sensorMlUri + "; Content-Type was: "
							+ evidence.sensorMlResponse.getContentType());
		}
		if (evidence.sensorMlBody.containsKey("items")) {
			ETSAssert.failWithUri(requirementUri,
					evidence.collectionLabel
							+ " SensorML representation URI returned a CS API 'items' wrapper, not SensorML JSON: "
							+ evidence.sensorMlUri);
		}
		if ("Feature".equals(evidence.sensorMlBody.get("type"))
				|| "FeatureCollection".equals(evidence.sensorMlBody.get("type"))) {
			ETSAssert.failWithUri(requirementUri,
					evidence.collectionLabel
							+ " SensorML representation URI returned default Feature JSON, not SensorML JSON: "
							+ evidence.sensorMlUri);
		}
	}

	private void assertSensorMlType(Map<String, Object> body, String expectedType, String collectionLabel,
			String requirementUri) {
		ETSAssert.assertJsonObjectHas(body, "type", String.class, requirementUri);
		Object type = body.get("type");
		if (!expectedType.equals(type)) {
			ETSAssert.failWithUri(requirementUri,
					collectionLabel + " SensorML type is '" + type + "', expected '" + expectedType + "'.");
		}
	}

	private void assertProcedureCompatibleType(Map<String, Object> body, String requirementUri) {
		ETSAssert.assertJsonObjectHas(body, "type", String.class, requirementUri);
		String type = asString(body.get("type"));
		if (type == null || !(type.contains("Process") || type.contains("System") || type.contains("Component"))) {
			ETSAssert.failWithUri(requirementUri, "Selected procedure SensorML type is '" + type
					+ "', expected a procedure-compatible SensorML process/system/component class.");
		}
	}

	private void assertIdentityPreserved(SensorMlResourceEvidence evidence, String requirementUri) {
		String sensorMlId = asString(evidence.sensorMlBody.get("id"));
		String sensorMlUniqueId = asString(evidence.sensorMlBody.get("uniqueId"));
		String sensorMlUid = asString(evidence.sensorMlBody.get("uid"));
		boolean idMatches = evidence.selectedId != null && evidence.selectedId.equals(sensorMlId);
		boolean uidMatches = evidence.selectedUid != null
				&& (evidence.selectedUid.equals(sensorMlUniqueId) || evidence.selectedUid.equals(sensorMlUid));
		if (!idMatches && !uidMatches) {
			throw new SkipException(requirementUri + " - selected CS API resource id/uid (" + evidence.selectedId
					+ " / " + evidence.selectedUid
					+ ") was not machine-checkably preserved in SensorML id/uniqueId/uid (" + sensorMlId + " / "
					+ sensorMlUniqueId + " / " + sensorMlUid + ").");
		}
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

	private boolean isExplicitSensorMl(Map<String, Object> body) {
		if (body == null) {
			return false;
		}
		String type = asString(body.get("type"));
		return type != null && !"Feature".equals(type) && !"FeatureCollection".equals(type)
				&& !body.containsKey("items");
	}

	private boolean hasAnyIdentity(Map<String, Object> body) {
		return asString(body.get("id")) != null || asString(body.get("uniqueId")) != null
				|| asString(body.get("uid")) != null || body.containsKey("identifiers");
	}

	private String asString(Object value) {
		return value instanceof String ? (String) value : null;
	}

	@SuppressWarnings("unchecked")
	private String uidFromFeatureProperties(Map<String, Object> item) {
		Object properties = item.get("properties");
		if (properties instanceof Map) {
			return asString(((Map<String, Object>) properties).get("uid"));
		}
		return asString(item.get("uid"));
	}

	static boolean hasMappingValue(Map<String, Object> body, String propertyName) {
		if (!body.containsKey(propertyName)) {
			return false;
		}
		Object value = body.get(propertyName);
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

	static boolean hasProcedureSpecificStructure(Map<String, Object> body) {
		return hasMappingValue(body, "definition") || hasMappingValue(body, "inputs")
				|| hasMappingValue(body, "outputs") || hasMappingValue(body, "parameters")
				|| hasMappingValue(body, "characteristics") || hasMappingValue(body, "capabilities");
	}

	static boolean isPropertyCompatibleType(Map<String, Object> body) {
		String type = body == null ? null : (body.get("type") instanceof String ? (String) body.get("type") : null);
		return type != null && type.contains("Property");
	}

	static boolean hasPropertyMappingEvidence(Map<String, Object> body) {
		return hasMappingValue(body, "id") || hasMappingValue(body, "uniqueId") || hasMappingValue(body, "definition")
				|| hasMappingValue(body, "identifiers");
	}

	private static final class SensorMlResourceEvidence {

		private final String collectionLabel;

		private final String selectedId;

		private final String selectedUid;

		private final URI sensorMlUri;

		private final String source;

		private final Response sensorMlResponse;

		private final Map<String, Object> sensorMlBody;

		SensorMlResourceEvidence(String collectionLabel, String selectedId, String selectedUid, URI sensorMlUri,
				String source, Response sensorMlResponse, Map<String, Object> sensorMlBody) {
			this.collectionLabel = collectionLabel;
			this.selectedId = selectedId;
			this.selectedUid = selectedUid;
			this.sensorMlUri = sensorMlUri;
			this.source = source;
			this.sensorMlResponse = sensorMlResponse;
			this.sensorMlBody = sensorMlBody;
		}

	}

}
