package org.opengis.cite.ogcapiconnectedsystems10.conformance.procedures;

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
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.PageDocument;
import org.testng.SkipException;

/**
 * Focused support checks for the released Procedure procedures.
 */
public class VerifyProcedureFeaturesSupport {

	private static final String REQUIREMENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/procedure/collections";

	private static final Set<String> TYPES = Set.of("ObservingProcedure", "SamplingProcedure", "ActuatingProcedure",
			"Procedure", "Sensor", "Actuator", "Sampler", "Platform", "System");

	/**
	 * REQ-ETS-PART1-006; SCENARIO-ETS-PART1-006-RELEASED-COLLECTION-COMPLETE-001.
	 */
	@Test
	public void selectorUsesExactReleasedFeatureTypeAndMetadata() {
		Map<String, Object> exact = Map.of("id", "procedures", "itemType", "feature", "featureType", "sosa:Procedure");
		Map<String, Object> extension = Map.of("id", "all_procedures", "itemType", "feature", "featureType",
				"procedure");

		assertEquals(List.of(exact), ProcedureFeaturesSupport.selectProcedureCollections(List.of(exact, extension)));
		assertThrows(AssertionError.class, () -> ProcedureFeaturesSupport.requireProcedureCollectionMetadata(
				Map.of("id", "bad", "itemType", "record", "featureType", "sosa:Procedure"), REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-006; SCENARIO-ETS-PART1-006-RELEASED-LOCATION-001.
	 */
	@Test
	public void locationRuleIsRepresentationSpecific() {
		Map<String, Object> validGeoJson = geoProcedure();
		Map<String, Object> nonNullGeometry = new LinkedHashMap<>(validGeoJson);
		nonNullGeometry.put("geometry", Map.of("type", "Point", "coordinates", List.of(1, 2)));
		Map<String, Object> missingGeometry = new LinkedHashMap<>(validGeoJson);
		missingGeometry.remove("geometry");
		Map<String, Object> sensorMlPosition = new LinkedHashMap<>(sensorMlProcedure());
		sensorMlPosition.put("position", Map.of());

		ProcedureFeaturesSupport.requireNoLocation(validGeoJson, "application/geo+json", REQUIREMENT);
		ProcedureFeaturesSupport.requireNoLocation(sensorMlProcedure(), "application/sml+json", REQUIREMENT);
		assertThrows(AssertionError.class,
				() -> ProcedureFeaturesSupport.requireNoLocation(nonNullGeometry, "application/geo+json", REQUIREMENT));
		assertThrows(AssertionError.class,
				() -> ProcedureFeaturesSupport.requireNoLocation(missingGeometry, "application/geo+json", REQUIREMENT));
		assertThrows(AssertionError.class, () -> ProcedureFeaturesSupport.requireNoLocation(sensorMlPosition,
				"application/sml+json", REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-006; SCENARIO-ETS-PART1-006-RELEASED-PROCEDURE-TYPE-001.
	 */
	@Test
	public void allReleasedProcedureTypesAreAcceptedInCurieAndUriForms() {
		for (String localName : TYPES) {
			Map<String, Object> geoJson = geoProcedureWithType("sosa:" + localName);
			Map<String, Object> sensorMl = sensorMlProcedureWithType("http://www.w3.org/ns/sosa/" + localName);
			ProcedureFeaturesSupport.requireReleasedProcedureType(geoJson, "application/geo+json", REQUIREMENT);
			ProcedureFeaturesSupport.requireReleasedProcedureType(sensorMl, "application/sml+json", REQUIREMENT);
		}

		assertThrows(AssertionError.class, () -> ProcedureFeaturesSupport
			.requireReleasedProcedureType(geoProcedureWithType("sosa:Unknown"), "application/geo+json", REQUIREMENT));
		assertThrows(AssertionError.class, () -> ProcedureFeaturesSupport.requireReleasedProcedureType(
				sensorMlProcedureWithType("http://example.test/Unknown"), "application/sml+json", REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-006; SCENARIO-ETS-PART1-006-RELEASED-CANONICAL-EQUIVALENCE-001.
	 */
	@Test
	public void canonicalLinkUsesExactProcedurePathAndNormalizationRemovesOnlyCanonicalLinks() {
		URI page = URI.create("https://example.test/api/collections/procedures/items");
		URI root = URI.create("https://example.test/api/");
		Map<String, Object> item = Map.of("type", "Feature", "id", "procedure-1", "properties", Map.of(), "links",
				List.of(Map.of("rel", "canonical", "href", "../../procedures/procedure-1?f=sml", "type",
						"application/sml+json"),
						Map.of("rel", "canonical", "href", "../../procedures/procedure-1?f=json", "type",
								"application/geo+json"),
						Map.of("rel", "alternate", "href", "../../procedures/procedure-1?f=sml")));
		Map<String, Object> canonical = Map.of("type", "Feature", "id", "procedure-1", "properties", Map.of(), "links",
				List.of(Map.of("rel", "canonical", "href", "https://example.test/api/procedures/procedure-1?f=json"),
						Map.of("rel", "alternate", "href", "../../procedures/procedure-1?f=sml")));

		ProcedureFeaturesSupport.CanonicalLink link = ProcedureFeaturesSupport.canonicalLink(item, page, root,
				"application/geo+json", REQUIREMENT);
		assertEquals(URI.create("https://example.test/api/procedures/procedure-1?f=json"), link.uri());
		assertEquals("application/geo+json", link.mediaType());
		assertEquals(ProcedureFeaturesSupport.withoutCanonicalLinks(item),
				ProcedureFeaturesSupport.withoutCanonicalLinks(canonical));

		Map<String, Object> canonicalOnly = Map.of("id", "procedure-1", "links",
				List.of(Map.of("rel", "canonical", "href", "../../procedures/procedure-1")));
		assertEquals(ProcedureFeaturesSupport.withoutCanonicalLinks(canonicalOnly),
				ProcedureFeaturesSupport.withoutCanonicalLinks(Map.of("id", "procedure-1")));

		Map<String, Object> conflictingPath = Map.of("id", "procedure-1", "links",
				List.of(Map.of("rel", "canonical", "href", "../../procedures/procedure-1"),
						Map.of("rel", "canonical", "href", "../../procedures/procedure-2")));
		assertThrows(AssertionError.class, () -> ProcedureFeaturesSupport.canonicalLink(conflictingPath, page, root,
				"application/geo+json", REQUIREMENT));

		Map<String, Object> noComparableRepresentation = Map.of("id", "procedure-1", "links", List.of(Map.of("rel",
				"canonical", "href", "../../procedures/procedure-1?f=sml", "type", "application/sml+json")));
		assertThrows(SkipException.class, () -> ProcedureFeaturesSupport.canonicalLink(noComparableRepresentation, page,
				root, "application/geo+json", REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-006; SCENARIO-ETS-PART1-006-RELEASED-RESOURCES-ENDPOINT-001.
	 */
	@Test
	public void procedureSchemasAcceptValidRepresentationsAndRejectInvalidContent() {
		Map<String, Object> geoJson = Map.of("type", "FeatureCollection", "features", List.of(geoProcedure()));
		Map<String, Object> sensorMl = Map.of("items", List.of(sensorMlProcedure()));
		Map<String, Object> invalidGeoJson = Map.of("type", "FeatureCollection", "features",
				List.of(geoProcedureWithType("sosa:Unknown")));

		assertTrue(ProcedureFeaturesSupport.validateProcedureEndpoint(URI.create("https://example.test/api/procedures"),
				List.of(page("application/geo+json", geoJson), page("application/sml+json", sensorMl)), REQUIREMENT));
		assertThrows(AssertionError.class,
				() -> ProcedureFeaturesSupport.validateProcedureEndpoint(
						URI.create("https://example.test/api/procedures"),
						List.of(page("application/geo+json", invalidGeoJson)), REQUIREMENT));
		assertFalse(
				ProcedureFeaturesSupport.validateProcedureEndpoint(URI.create("https://example.test/api/procedures"),
						List.of(page("application/json", Map.of("items", List.of()))), REQUIREMENT));
	}

	private static PageDocument page(String mediaType, Map<String, Object> body) {
		return new PageDocument(URI.create("https://example.test/api/procedures"), mediaType, body, List.of());
	}

	private static Map<String, Object> geoProcedure() {
		return geoProcedureWithType("sosa:ObservingProcedure");
	}

	private static Map<String, Object> geoProcedureWithType(String type) {
		Map<String, Object> feature = new LinkedHashMap<>();
		feature.put("type", "Feature");
		feature.put("id", "procedure-geo");
		feature.put("geometry", null);
		feature.put("properties", Map.of("uid", "urn:ogc:procedure:geo", "name", "Procedure Geo", "featureType", type));
		feature.put("links",
				List.of(Map.of("rel", "canonical", "href", "https://example.test/api/procedures/procedure-geo")));
		return feature;
	}

	private static Map<String, Object> sensorMlProcedure() {
		return sensorMlProcedureWithType("sosa:ObservingProcedure");
	}

	private static Map<String, Object> sensorMlProcedureWithType(String type) {
		return Map.of("type", "SimpleProcess", "id", "procedure-sml", "label", "Procedure SML", "uniqueId",
				"urn:ogc:procedure:sml", "definition", type, "links",
				List.of(Map.of("rel", "canonical", "href", "https://example.test/api/procedures/procedure-sml")));
	}

}
