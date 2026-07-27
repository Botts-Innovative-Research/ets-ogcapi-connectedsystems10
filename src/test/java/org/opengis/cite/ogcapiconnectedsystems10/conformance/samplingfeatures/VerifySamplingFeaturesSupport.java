package org.opengis.cite.ogcapiconnectedsystems10.conformance.samplingfeatures;

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
 * Focused support checks for the released Sampling Features procedures.
 */
public class VerifySamplingFeaturesSupport {

	private static final String REQUIREMENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sf/collections";

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-COLLECTIONS-001.
	 */
	@Test
	public void selectorUsesExactReleasedFeatureTypeAndMetadata() {
		Map<String, Object> exact = Map.of("id", "samples", "itemType", "feature", "featureType", "sosa:Sample");
		Map<String, Object> extension = Map.of("id", "all_fois", "itemType", "feature", "featureType",
				"featureOfInterest");

		assertEquals(List.of(exact),
				SamplingFeaturesSupport.selectSamplingFeatureCollections(List.of(exact, extension)));
		assertThrows(AssertionError.class, () -> SamplingFeaturesSupport.requireSamplingFeatureCollectionMetadata(
				Map.of("id", "bad", "itemType", "record", "featureType", "sosa:Sample"), REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-RESOURCES-ENDPOINT-001.
	 */
	@Test
	public void releasedSchemaAcceptsValidGeoJsonAndRejectsInvalidContent() {
		Map<String, Object> valid = Map.of("type", "FeatureCollection", "features", List.of(samplingFeature()));
		Map<String, Object> invalid = Map.of("type", "FeatureCollection", "features",
				List.of(samplingFeatureWithoutSampledFeature()));

		assertTrue(SamplingFeaturesSupport.validateSamplingFeatureEndpoint(
				URI.create("https://example.test/api/samplingFeatures"), List.of(page("application/geo+json", valid)),
				REQUIREMENT));
		assertThrows(AssertionError.class,
				() -> SamplingFeaturesSupport.validateSamplingFeatureEndpoint(
						URI.create("https://example.test/api/samplingFeatures"),
						List.of(page("application/geo+json", invalid)), REQUIREMENT));
		assertFalse(SamplingFeaturesSupport.validateSamplingFeatureEndpoint(
				URI.create("https://example.test/api/samplingFeatures"),
				List.of(page("application/json", Map.of("items", List.of()))), REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-CANONICAL-EQUIVALENCE-001.
	 */
	@Test
	public void canonicalLinkUsesExactSamplingFeatureIdentityAndNormalizesCanonicalLinks() {
		URI page = URI.create("https://example.test/api/collections/samples/items");
		URI root = URI.create("https://example.test/api/");
		Map<String, Object> item = samplingFeature();
		Map<String, Object> canonical = samplingFeatureWithoutLinks();

		SamplingFeaturesSupport.CanonicalLink link = SamplingFeaturesSupport.canonicalLink(item, page, root,
				"application/geo+json", REQUIREMENT);
		assertEquals(URI.create("https://example.test/api/samplingFeatures/sf-1?f=geojson"), link.uri());
		assertEquals("application/geo+json", link.mediaType());
		assertEquals(SamplingFeaturesSupport.withoutCanonicalLinks(item),
				SamplingFeaturesSupport.withoutCanonicalLinks(canonical));

		Map<String, Object> wrongPath = samplingFeatureWithCanonical("https://example.test/api/samplingFeatures/sf-2",
				"application/geo+json");
		assertThrows(AssertionError.class, () -> SamplingFeaturesSupport.canonicalLink(wrongPath, page, root,
				"application/geo+json", REQUIREMENT));

		Map<String, Object> crossOrigin = samplingFeatureWithCanonical("https://other.test/api/samplingFeatures/sf-1",
				"application/geo+json");
		assertThrows(AssertionError.class, () -> SamplingFeaturesSupport.canonicalLink(crossOrigin, page, root,
				"application/geo+json", REQUIREMENT));

		Map<String, Object> unsupported = samplingFeatureWithCanonical("https://example.test/api/samplingFeatures/sf-1",
				"text/html");
		assertThrows(SkipException.class, () -> SamplingFeaturesSupport.canonicalLink(unsupported, page, root,
				"application/geo+json", REQUIREMENT));
	}

	private static PageDocument page(String mediaType, Map<String, Object> body) {
		return new PageDocument(URI.create("https://example.test/api/samplingFeatures"), mediaType, body, List.of());
	}

	private static Map<String, Object> samplingFeature() {
		return samplingFeatureWithCanonical("https://example.test/api/samplingFeatures/sf-1?f=geojson",
				"application/geo+json");
	}

	private static Map<String, Object> samplingFeatureWithCanonical(String href, String mediaType) {
		Map<String, Object> feature = new LinkedHashMap<>(samplingFeatureWithoutLinks());
		feature.put("links", List.of(Map.of("rel", "canonical", "href", href, "type", mediaType)));
		return feature;
	}

	private static Map<String, Object> samplingFeatureWithoutLinks() {
		Map<String, Object> feature = new LinkedHashMap<>();
		feature.put("type", "Feature");
		feature.put("id", "sf-1");
		feature.put("geometry", Map.of("type", "Point", "coordinates", List.of(1, 2)));
		feature.put("properties", Map.of("uid", "urn:ogc:sf:1", "name", "Sample One", "featureType", "sosa:Sample",
				"sampledFeature@link", Map.of("href", "https://example.test/features/foi-1")));
		return feature;
	}

	private static Map<String, Object> samplingFeatureWithoutSampledFeature() {
		Map<String, Object> feature = new LinkedHashMap<>(samplingFeatureWithoutLinks());
		feature.put("properties", Map.of("uid", "urn:ogc:sf:1", "name", "Sample One", "featureType", "sosa:Sample"));
		return feature;
	}

}
