package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.apicommon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Unit checks for the Sprint 20 Part 2 API Common helper logic.
 */
public class VerifyPart2ApiCommonTests {

	@org.junit.Test
	public void declaresApiCommonOnlyForExactConformanceUri() {
		Map<String, Object> body = Map.of("conformsTo",
				List.of("http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/datastream",
						Part2ApiCommonTests.CONF_PART2_API_COMMON));

		assertTrue(Part2ApiCommonTests.declaresConformance(body, Part2ApiCommonTests.CONF_PART2_API_COMMON));
		assertFalse(Part2ApiCommonTests.declaresConformance(body,
				"http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/dynamic-data"));
	}

	@org.junit.Test
	public void missingConformsToDoesNotDeclareApiCommon() {
		assertFalse(Part2ApiCommonTests.declaresConformance(Map.of(), Part2ApiCommonTests.CONF_PART2_API_COMMON));
		assertFalse(Part2ApiCommonTests.declaresConformance(null, Part2ApiCommonTests.CONF_PART2_API_COMMON));
	}

	@org.junit.Test
	public void discoversOnlyAdvertisedPart2Collections() {
		Map<String, Object> landing = Map.of("links",
				List.of(Map.of("rel", "datastreams", "href", "datastreams"),
						Map.of("rel", "self", "href", "https://example.test/api/observations"),
						Map.of("rel", "service-desc", "href", "api")));

		List<URI> uris = Part2ApiCommonTests.discoverPart2CollectionUris(landing,
				URI.create("https://example.test/api/"));

		assertEquals(List.of(URI.create("https://example.test/api/datastreams"),
				URI.create("https://example.test/api/observations")), uris);
		assertFalse("The helper must not synthesize /commands when the landing page does not advertise it.",
				uris.contains(URI.create("https://example.test/api/commands")));
	}

	@org.junit.Test
	public void resourceCollectionShapeRequiresItemsAndLinksArrays() {
		assertTrue(Part2ApiCommonTests.hasResourceCollectionShape(Map.of("items", List.of(), "links", List.of())));
		assertFalse(Part2ApiCommonTests.hasResourceCollectionShape(Map.of("items", List.of())));
		assertFalse(Part2ApiCommonTests.hasResourceCollectionShape(Map.of("items", Map.of(), "links", List.of())));
	}

	@org.junit.Test
	public void constantsDoNotUseDynamicDataIdentifiers() {
		String joined = String.join(" ", Part2ApiCommonTests.CONF_PART2_API_COMMON, Part2ApiCommonTests.REQ_API_COMMON,
				Part2ApiCommonTests.REQ_RESOURCES, Part2ApiCommonTests.REQ_RESOURCE_COLLECTION);

		assertFalse(joined.contains("dynamic-data"));
		assertFalse(joined.contains("dynamic"));
	}

}
