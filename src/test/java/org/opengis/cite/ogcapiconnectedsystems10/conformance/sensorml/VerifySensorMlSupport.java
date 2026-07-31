package org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml.SensorMlSupport.ApiDefinition;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml.SensorMlSupport.ResourceType;

/**
 * Unit checks for released SensorML API-definition and mapping semantics.
 */
public class VerifySensorMlSupport {

	private static final String REQUIREMENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/test";

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-MEDIA-ADVERTISEMENT-001.
	 */
	@Test
	public void jsonAndYamlDefinitionsAdvertiseReadAndWriteMedia() {
		ApiDefinition json = SensorMlSupport.parseApiDefinition(apiDefinitionJson(),
				URI.create("https://example.test/openapi.json"), REQUIREMENT);
		ApiDefinition yaml = SensorMlSupport.parseApiDefinition(apiDefinitionYaml(),
				URI.create("https://example.test/openapi.yaml"), REQUIREMENT);

		SensorMlSupport.assertReadMediaAdvertisements(json, Set.of(ResourceType.SYSTEM), true, REQUIREMENT);
		SensorMlSupport.assertReadMediaAdvertisements(yaml, Set.of(ResourceType.SYSTEM), true, REQUIREMENT);
		SensorMlSupport.assertWriteMediaAdvertisement(json, REQUIREMENT);
		SensorMlSupport.assertWriteMediaAdvertisement(yaml, REQUIREMENT);
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-MEDIA-ADVERTISEMENT-001.
	 */
	@Test
	public void parseableDefinitionMissingSensorMlMediaFails() {
		ApiDefinition definition = SensorMlSupport.parseApiDefinition(
				apiDefinitionJson().replace("application/sml+json", "application/json"),
				URI.create("https://example.test/openapi.json"), REQUIREMENT);

		assertThrows(AssertionError.class, () -> SensorMlSupport.assertReadMediaAdvertisements(definition,
				Set.of(ResourceType.SYSTEM), true, REQUIREMENT));
		assertThrows(AssertionError.class,
				() -> SensorMlSupport.assertWriteMediaAdvertisement(definition, REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-MEDIA-ADVERTISEMENT-001.
	 */
	@Test
	public void defaultResponseDoesNotProveSuccessfulGetMedia() {
		ApiDefinition definition = SensorMlSupport.parseApiDefinition(
				apiDefinitionJson().replace("\"200\"", "\"default\""), URI.create("https://example.test/openapi.json"),
				REQUIREMENT);

		assertThrows(AssertionError.class, () -> SensorMlSupport.assertReadMediaAdvertisements(definition,
				Set.of(ResourceType.SYSTEM), true, REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-MEDIA-ADVERTISEMENT-001.
	 */
	@Test
	public void externalReferenceCannotReachUnrelatedPrivateAddress() {
		String definition = """
				openapi: 3.1.0
				info:
				  title: unsafe reference
				  version: "1"
				paths:
				  /systems:
				    $ref: http://127.0.0.1:9/private.yaml
				""";

		ApiDefinition parsed = SensorMlSupport.parseApiDefinition(definition,
				URI.create("https://example.test/openapi.yaml"), REQUIREMENT);
		assertTrue(parsed.diagnostics().toString().contains("IP is restricted"));
		assertThrows(AssertionError.class, () -> SensorMlSupport.assertReadMediaAdvertisements(parsed,
				Set.of(ResourceType.SYSTEM), false, REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-RESOURCE-ID-001.
	 */
	@Test
	public void canonicalIdRequiresExactEquality() {
		SensorMlSupport.validateResourceId(system(), "system-1", REQUIREMENT, "system");

		assertThrows(AssertionError.class,
				() -> SensorMlSupport.validateResourceId(system(), "different", REQUIREMENT, "system"));
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-COMMON-MAPPINGS-001.
	 */
	@Test
	public void commonMappingsValidateEveryPresentValue() {
		SensorMlSupport.validateCommonFeature(system(), REQUIREMENT, "system");
		Map<String, Object> invalid = new LinkedHashMap<>(system());
		invalid.put("uniqueId", "not a uri");

		assertThrows(AssertionError.class, () -> SensorMlSupport.validateCommonFeature(invalid, REQUIREMENT, "system"));
		Map<String, Object> withoutOptionalUniqueId = new LinkedHashMap<>(property());
		withoutOptionalUniqueId.remove("uniqueId");
		SensorMlSupport.validateCommonFeature(withoutOptionalUniqueId, REQUIREMENT, "property");
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-CLASS-COMPATIBILITY-001.
	 */
	@Test
	public void systemClassCompatibilityUsesAssetTypeWithoutAmbiguousPass() {
		assertTrue(SensorMlSupport.validateSystemClass(system(), REQUIREMENT, "system"));
		Map<String, Object> incompatible = new LinkedHashMap<>(system());
		incompatible.put("type", "SimpleProcess");
		assertThrows(AssertionError.class,
				() -> SensorMlSupport.validateSystemClass(incompatible, REQUIREMENT, "system"));

		Map<String, Object> ambiguous = new LinkedHashMap<>(system());
		ambiguous.remove("classifiers");
		assertFalse(SensorMlSupport.validateSystemClass(ambiguous, REQUIREMENT, "system"));
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-RESOURCE-MAPPINGS-001.
	 */
	@Test
	public void assetTypeAcceptsBoundFormsAndRejectsUnboundCurie() {
		Map<String, Object> bound = new LinkedHashMap<>(system());
		bound.put("classifiers", List.of(Map.of("definition", "cs:AssetType", "value", "cs:Equipment")));
		assertTrue(SensorMlSupport.validateSystemClass(bound, REQUIREMENT, "system"));

		Map<String, Object> absolute = new LinkedHashMap<>(system());
		absolute.put("classifiers",
				List.of(Map.of("definition", "cs:AssetType", "value", "https://example.test/assets#Equipment")));
		assertTrue(SensorMlSupport.validateSystemClass(absolute, REQUIREMENT, "system"));

		Map<String, Object> unbound = new LinkedHashMap<>(system());
		unbound.put("classifiers", List.of(Map.of("definition", "cs:AssetType", "value", "bogus:Equipment")));
		assertThrows(AssertionError.class, () -> SensorMlSupport.validateSystemClass(unbound, REQUIREMENT, "system"));
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-CLASS-COMPATIBILITY-001.
	 */
	@Test
	public void procedureClassUsesProcedureTypeAndRejectsPosition() {
		assertTrue(SensorMlSupport.validateProcedureClass(procedure(), REQUIREMENT, "procedure"));
		Map<String, Object> wrongClass = new LinkedHashMap<>(procedure());
		wrongClass.put("type", "PhysicalSystem");
		assertThrows(AssertionError.class,
				() -> SensorMlSupport.validateProcedureClass(wrongClass, REQUIREMENT, "procedure"));

		Map<String, Object> positioned = new LinkedHashMap<>(procedure());
		positioned.put("position", Map.of());
		assertThrows(AssertionError.class,
				() -> SensorMlSupport.validateProcedureClass(positioned, REQUIREMENT, "procedure"));
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-RESOURCE-MAPPINGS-001.
	 */
	@Test
	public void resourceMappingsKeepOptionalMembersOptionalAndRejectMalformedPresentValues() {
		SensorMlSupport.validateResourceMappings(system(), ResourceType.SYSTEM, REQUIREMENT, "system");
		SensorMlSupport.validateResourceMappings(deployment(), ResourceType.DEPLOYMENT, REQUIREMENT, "deployment");
		SensorMlSupport.validateResourceMappings(procedure(), ResourceType.PROCEDURE, REQUIREMENT, "procedure");
		SensorMlSupport.validateResourceMappings(property(), ResourceType.PROPERTY, REQUIREMENT, "property");

		Map<String, Object> invalidDeployment = new LinkedHashMap<>(deployment());
		invalidDeployment.put("deployedSystems", List.of(Map.of("name", "missing system link")));
		assertThrows(AssertionError.class, () -> SensorMlSupport.validateResourceMappings(invalidDeployment,
				ResourceType.DEPLOYMENT, REQUIREMENT, "deployment"));

		Map<String, Object> invalidProperty = new LinkedHashMap<>(property());
		invalidProperty.put("baseProperty", "not a uri");
		assertThrows(AssertionError.class, () -> SensorMlSupport.validateResourceMappings(invalidProperty,
				ResourceType.PROPERTY, REQUIREMENT, "property"));

		Map<String, Object> malformedPose = new LinkedHashMap<>(system());
		malformedPose.put("position", Map.of("type", "GeoPose"));
		assertThrows(AssertionError.class, () -> SensorMlSupport.validateResourceMappings(malformedPose,
				ResourceType.SYSTEM, REQUIREMENT, "system"));

		Map<String, Object> malformedGeometry = new LinkedHashMap<>(deployment());
		malformedGeometry.put("location", Map.of("type", "Point"));
		assertThrows(AssertionError.class, () -> SensorMlSupport.validateResourceMappings(malformedGeometry,
				ResourceType.DEPLOYMENT, REQUIREMENT, "deployment"));
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-RESOURCE-MAPPINGS-001.
	 */
	@Test
	public void associationTargetsResolveRequiredKindsAndPermitDistributedResources() {
		Map<String, Object> linked = withLinks(system(),
				List.of(Map.of("rel", "ogc-rel:subsystems", "href", "/api/systems/system-1/subsystems")));
		linked.put("typeOf", Map.of("href", "/api/procedures/procedure-1"));
		linked.put("attachedTo", Map.of("href", "/api/systems/parent-1"));

		List<SensorMlSupport.AssociationTarget> targets = SensorMlSupport.associationTargets(linked,
				ResourceType.SYSTEM, URI.create("https://example.test/api/systems/system-1"),
				URI.create("https://example.test/api/"), REQUIREMENT);
		assertEquals(3, targets.size());

		Map<String, Object> wrongType = new LinkedHashMap<>(system());
		wrongType.put("typeOf", Map.of("href", "/api/systems/not-a-procedure"));
		assertThrows(AssertionError.class,
				() -> SensorMlSupport.associationTargets(wrongType, ResourceType.SYSTEM,
						URI.create("https://example.test/api/systems/system-1"),
						URI.create("https://example.test/api/"), REQUIREMENT));

		Map<String, Object> distributed = new LinkedHashMap<>(system());
		distributed.put("attachedTo", Map.of("href", "https://other.test/distributed/parent-1"));
		List<SensorMlSupport.AssociationTarget> distributedTargets = SensorMlSupport.associationTargets(distributed,
				ResourceType.SYSTEM, URI.create("https://example.test/api/systems/system-1"),
				URI.create("https://example.test/api/"), REQUIREMENT);
		assertEquals(1, distributedTargets.size());

		Map<String, Object> unsafe = new LinkedHashMap<>(system());
		unsafe.put("attachedTo", Map.of("href", "ftp://other.test/distributed/parent-1"));
		assertThrows(AssertionError.class,
				() -> SensorMlSupport.associationTargets(unsafe, ResourceType.SYSTEM,
						URI.create("https://example.test/api/systems/system-1"),
						URI.create("https://example.test/api/"), REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-RELATION-TYPES-001.
	 */
	@Test
	public void relationsRequireExactOgcRelPrefixedVocabulary() {
		Map<String, Object> valid = withLinks(system(),
				List.of(Map.of("rel", "canonical", "href", "https://example.test/systems/system-1"),
						Map.of("rel", "ogc-rel:subsystems", "href", "https://example.test/systems")));
		Map<String, Object> unprefixed = withLinks(system(),
				List.of(Map.of("rel", "subsystems", "href", "https://example.test/systems")));
		Map<String, Object> wrongResource = withLinks(system(),
				List.of(Map.of("rel", "ogc-rel:implementingSystems", "href", "https://example.test/systems")));
		Map<String, Object> wrongCase = withLinks(system(),
				List.of(Map.of("rel", "ogc-rel:controlStreams", "href", "https://example.test/controlstreams")));

		assertEquals(1, SensorMlSupport.validateRelationTypes(valid, ResourceType.SYSTEM, REQUIREMENT, "system"));
		assertThrows(AssertionError.class,
				() -> SensorMlSupport.validateRelationTypes(unprefixed, ResourceType.SYSTEM, REQUIREMENT, "system"));
		assertThrows(AssertionError.class,
				() -> SensorMlSupport.validateRelationTypes(wrongResource, ResourceType.SYSTEM, REQUIREMENT, "system"));
		assertThrows(AssertionError.class,
				() -> SensorMlSupport.validateRelationTypes(wrongCase, ResourceType.SYSTEM, REQUIREMENT, "system"));
	}

	private static String apiDefinitionJson() {
		return """
				{
				  "openapi":"3.0.3",
				  "info":{"title":"fixture","version":"1"},
				  "paths":{
				    "/systems":{
				      "get":{"responses":{"200":{"description":"ok","content":{"application/sml+json":{}}}}},
				      "post":{"requestBody":{"content":{"application/sml+json":{"schema":{"type":"object"}}}},
				              "responses":{"201":{"description":"created"}}}
				    },
				    "/collections/{collectionId}/items":{
				      "get":{"responses":{"200":{"description":"ok","content":{"application/sml+json":{}}}}}
				    }
				  }
				}
				""";
	}

	private static String apiDefinitionYaml() {
		return """
				openapi: 3.0.3
				info:
				  title: fixture
				  version: "1"
				paths:
				  /systems:
				    get:
				      responses:
				        "200":
				          description: ok
				          content:
				            application/sml+json: {}
				    post:
				      requestBody:
				        content:
				          application/sml+json:
				            schema:
				              type: object
				      responses:
				        "201":
				          description: created
				  /collections/{collectionId}/items:
				    get:
				      responses:
				        "200":
				          description: ok
				          content:
				            application/sml+json: {}
				""";
	}

	private static Map<String, Object> system() {
		Map<String, Object> value = new LinkedHashMap<>();
		value.put("type", "PhysicalSystem");
		value.put("id", "system-1");
		value.put("uniqueId", "urn:example:system:1");
		value.put("label", "System");
		value.put("definition", "sosa:System");
		value.put("classifiers",
				List.of(Map.of("label", "Asset type", "definition", "cs:AssetType", "value", "Equipment")));
		return value;
	}

	private static Map<String, Object> deployment() {
		Map<String, Object> value = new LinkedHashMap<>();
		value.put("type", "Deployment");
		value.put("id", "deployment-1");
		value.put("uniqueId", "urn:example:deployment:1");
		value.put("label", "Deployment");
		value.put("definition", "sosa:Deployment");
		value.put("deployedSystems",
				List.of(Map.of("name", "sensor", "system", Map.of("href", "https://example.test/systems/system-1"))));
		return value;
	}

	private static Map<String, Object> procedure() {
		Map<String, Object> value = new LinkedHashMap<>();
		value.put("type", "SimpleProcess");
		value.put("id", "procedure-1");
		value.put("uniqueId", "urn:example:procedure:1");
		value.put("label", "Procedure");
		value.put("definition", "sosa:ObservingProcedure");
		return value;
	}

	private static Map<String, Object> property() {
		Map<String, Object> value = new LinkedHashMap<>();
		value.put("id", "property-1");
		value.put("uniqueId", "urn:example:property:1");
		value.put("label", "Property");
		value.put("baseProperty", "https://qudt.org/vocab/quantitykind/Temperature");
		return value;
	}

	private static Map<String, Object> withLinks(Map<String, Object> resource, List<Map<String, Object>> links) {
		Map<String, Object> linked = new LinkedHashMap<>(resource);
		linked.put("links", links);
		return linked;
	}

}
