package org.opengis.cite.ogcapiconnectedsystems10.conformance.propertydefinitions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.PageDocument;
import org.testng.SkipException;

/**
 * Focused support checks for the released Property Definitions procedures.
 */
public class VerifyPropertyDefinitionsSupport {

	private static final String REQUIREMENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/property/collections";

	/**
	 * REQ-ETS-PART1-008; SCENARIO-ETS-PART1-008-RELEASED-COLLECTIONS-001.
	 */
	@Test
	public void selectorUsesExactReleasedItemTypeAndMetadata() {
		Map<String, Object> exact = Map.of("id", "properties", "itemType", "sosa:Property");
		Map<String, Object> wrongMember = Map.of("id", "properties-feature", "itemType", "feature", "featureType",
				"sosa:Property");

		assertEquals(List.of(exact), PropertyDefinitionsSupport.selectPropertyCollections(List.of(exact, wrongMember)));
		assertThrows(AssertionError.class, () -> PropertyDefinitionsSupport
			.requirePropertyCollectionMetadata(Map.of("id", "", "itemType", "sosa:Property"), REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-008; SCENARIO-ETS-PART1-008-RELEASED-SENSORML-SCHEMA-001.
	 */
	@Test
	public void releasedSchemaAcceptsValidSensorMlAndRejectsInvalidContent() {
		Map<String, Object> valid = Map.of("items", List.of(property()));
		Map<String, Object> invalid = Map.of("items", List.of(propertyWithoutBaseProperty()));

		assertTrue(
				PropertyDefinitionsSupport.validatePropertyEndpoint(URI.create("https://example.test/api/properties"),
						List.of(page("application/sml+json", valid)), REQUIREMENT));
		assertThrows(AssertionError.class,
				() -> PropertyDefinitionsSupport.validatePropertyEndpoint(
						URI.create("https://example.test/api/properties"),
						List.of(page("application/sml+json", invalid)), REQUIREMENT));
		assertFalse(
				PropertyDefinitionsSupport.validatePropertyEndpoint(URI.create("https://example.test/api/properties"),
						List.of(page("application/json", Map.of("items", List.of()))), REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-008; SCENARIO-ETS-PART1-008-RELEASED-CANONICAL-EQUIVALENCE-001.
	 */
	@Test
	public void canonicalLinkUsesPropertyPathAndNormalizesCanonicalLinks() {
		URI page = URI.create("https://example.test/api/collections/properties/items");
		URI root = URI.create("https://example.test/api/");
		Map<String, Object> item = property();
		Map<String, Object> canonical = propertyWithoutLinks();

		PropertyDefinitionsSupport.CanonicalLink link = PropertyDefinitionsSupport.canonicalLink(item, page, root,
				"application/sml+json", REQUIREMENT);
		assertEquals(URI.create("https://example.test/api/properties/property-1?f=sml"), link.uri());
		assertEquals("application/sml+json", link.mediaType());
		assertEquals(PropertyDefinitionsSupport.withoutCanonicalLinks(item),
				PropertyDefinitionsSupport.withoutCanonicalLinks(canonical));

		Map<String, Object> wrongPath = propertyWithCanonical("https://example.test/api/procedures/property-1",
				"application/sml+json");
		assertThrows(AssertionError.class, () -> PropertyDefinitionsSupport.canonicalLink(wrongPath, page, root,
				"application/sml+json", REQUIREMENT));

		Map<String, Object> nestedPath = propertyWithCanonical("https://example.test/api/properties/a/b",
				"application/sml+json");
		assertThrows(AssertionError.class, () -> PropertyDefinitionsSupport.canonicalLink(nestedPath, page, root,
				"application/sml+json", REQUIREMENT));

		Map<String, Object> crossOrigin = propertyWithCanonical("https://other.test/api/properties/property-1",
				"application/sml+json");
		assertThrows(AssertionError.class, () -> PropertyDefinitionsSupport.canonicalLink(crossOrigin, page, root,
				"application/sml+json", REQUIREMENT));

		Map<String, Object> unsupported = propertyWithCanonical("https://example.test/api/properties/property-1",
				"text/html");
		assertThrows(SkipException.class, () -> PropertyDefinitionsSupport.canonicalLink(unsupported, page, root,
				"application/sml+json", REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-008; SCENARIO-ETS-PART1-008-RELEASED-VALIDATOR-BOUNDARY-001.
	 */
	@Test
	public void publicAdapterSurfaceDoesNotExposeValidatorImplementationTypes() {
		for (java.lang.reflect.Method method : PropertyDefinitionsSupport.class.getDeclaredMethods()) {
			if (!java.lang.reflect.Modifier.isPublic(method.getModifiers())) {
				continue;
			}
			assertFalse(method.getReturnType().getName().startsWith("com.networknt."));
			for (Class<?> parameter : method.getParameterTypes()) {
				assertFalse(parameter.getName().startsWith("com.networknt."));
			}
		}
	}

	private static PageDocument page(String mediaType, Map<String, Object> body) {
		return new PageDocument(URI.create("https://example.test/api/properties"), mediaType, body,
				SuppressWarningsHelper.items(body));
	}

	private static Map<String, Object> property() {
		return propertyWithCanonical("https://example.test/api/properties/property-1?f=sml", "application/sml+json");
	}

	private static Map<String, Object> propertyWithCanonical(String href, String mediaType) {
		Map<String, Object> property = new LinkedHashMap<>();
		property.put("uniqueId", "urn:example:property:temperature");
		property.put("label", "Temperature");
		property.put("baseProperty", "https://qudt.org/vocab/quantitykind/Temperature");
		property.put("links", List.of(Map.of("rel", "canonical", "type", mediaType, "href", href)));
		return property;
	}

	private static Map<String, Object> propertyWithoutLinks() {
		Map<String, Object> property = new LinkedHashMap<>(property());
		property.remove("links");
		return property;
	}

	private static Map<String, Object> propertyWithoutBaseProperty() {
		Map<String, Object> property = new LinkedHashMap<>(property());
		property.remove("baseProperty");
		return property;
	}

	private static final class SuppressWarningsHelper {

		private SuppressWarningsHelper() {
		}

		@SuppressWarnings("unchecked")
		private static List<Map<String, Object>> items(Map<String, Object> body) {
			Object items = body.get("items");
			return items instanceof List ? (List<Map<String, Object>>) items : List.of();
		}

	}

}
