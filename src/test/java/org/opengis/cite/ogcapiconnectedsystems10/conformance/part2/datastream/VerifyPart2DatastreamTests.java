package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.datastream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.PageDocument;
import org.testng.SkipException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Unit checks for the Part 2 Datastream released ATS logic.
 */
public class VerifyPart2DatastreamTests {

	private static final String BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/datastream/";

	private static final Set<String> RELEASED_TARGETS = Set.of(BASE + "sf-ref-from-datastream",
			BASE + "foi-ref-from-datastream", BASE + "canonical-url", BASE + "resources-endpoint",
			BASE + "canonical-endpoint", BASE + "ref-from-system", BASE + "ref-from-deployment", BASE + "collections",
			BASE + "schema-op", BASE + "obs-canonical-url", BASE + "obs-resources-endpoint",
			BASE + "obs-canonical-endpoint", BASE + "obs-ref-from-datastream", BASE + "obs-collections");

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-RELEASED-PROCEDURES-001.
	 */
	@org.junit.Test
	public void datastreamClassContainsExactlyTheFourteenReleasedProcedures() {
		List<Method> methods = Arrays.stream(Part2DatastreamTests.class.getDeclaredMethods())
			.filter(method -> method.getAnnotation(org.testng.annotations.Test.class) != null)
			.toList();

		assertEquals(14, methods.size());
		assertEquals(14,
				methods.stream()
					.map(method -> method.getAnnotation(org.testng.annotations.Test.class).description())
					.flatMap(description -> RELEASED_TARGETS.stream().filter(description::contains))
					.distinct()
					.count());
		for (Method method : methods) {
			org.testng.annotations.Test annotation = method.getAnnotation(org.testng.annotations.Test.class);
			assertTrue(method + " must use part2datastream group",
					Arrays.asList(annotation.groups()).contains(Part2DatastreamTests.GROUP));
			assertTrue(method + " must remain independently executable", annotation.alwaysRun());
			assertEquals(method + " must identify exactly one released target", 1,
					RELEASED_TARGETS.stream().filter(annotation.description()::contains).count());
			assertTrue(method + " must trace REQ-ETS-PART2-002",
					annotation.description().contains("REQ-ETS-PART2-002"));
			assertEquals(method + " must not depend on another Part 2 Datastream method", 0,
					annotation.dependsOnMethods().length);
		}
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-DIRECT-PREREQUISITE-001.
	 */
	@org.junit.Test
	public void beforeClassLoadsOnlyImmutableArgumentsAfterPart2ApiCommon() throws Exception {
		Method setup = Part2DatastreamTests.class.getDeclaredMethod("fetchPart2DatastreamInputs",
				org.testng.ITestContext.class);
		org.testng.annotations.BeforeClass annotation = setup.getAnnotation(org.testng.annotations.BeforeClass.class);

		assertTrue(annotation != null);
		assertTrue(annotation.alwaysRun());
		assertTrue(Arrays.asList(annotation.dependsOnGroups()).contains("part2apicommon"));
		assertFalse(Arrays.stream(Part2DatastreamTests.class.getDeclaredFields())
			.anyMatch(field -> field.getName().toLowerCase().contains("response")
					|| field.getName().toLowerCase().contains("body")));
	}

	@org.junit.Test
	public void datastreamShapeRequiresResourceSpecificMembers() {
		assertTrue(Part2DatastreamTests
			.hasDatastreamShape(Map.of("id", "ds-1", "system@id", "sys-1", "outputName", "out", "observedProperties",
					List.of(Map.of("label", "p")), "formats", List.of("application/om+json"), "resultType", "record")));
		assertFalse("A generic JSON object with only id/items must not masquerade as a Datastream.",
				Part2DatastreamTests.hasDatastreamShape(Map.of("id", "ds-1", "items", List.of())));
	}

	@org.junit.Test
	public void observationReferenceRequiresActualDatastreamEvidence() {
		assertTrue(Part2DatastreamTests.observationReferencesDatastream(Map.of("id", "obs-1", "datastream@id", "ds-1"),
				"ds-1"));
		assertTrue(Part2DatastreamTests.observationReferencesDatastream(
				Map.of("id", "obs-1", "links", List.of(Map.of("href", "https://example.test/api/datastreams/ds-1"))),
				"ds-1"));
		assertFalse("Empty or unrelated observations must not PASS obs-ref-from-datastream.",
				Part2DatastreamTests.observationReferencesDatastream(Map.of("id", "obs-1"), "ds-1"));
	}

	@org.junit.Test
	public void itemsOnlyCollectionShapeAllowsEmptyNestedObservations() {
		assertTrue(Part2DatastreamTests.hasItemsOnlyCollectionShape(Map.of("items", List.of())));
		assertFalse(Part2DatastreamTests.hasItemsOnlyCollectionShape(Map.of("links", List.of())));
	}

	@org.junit.Test
	public void observationShapeRequiresObservationSpecificMembers() {
		assertTrue(Part2DatastreamTests.hasObservationShape(Map.of("id", "obs-1", "datastream@id", "ds-1")));
		assertTrue(Part2DatastreamTests.hasObservationShape(Map.of("id", "obs-1", "result", 12.3)));
		assertFalse("A generic JSON object with only an id must not masquerade as an Observation.",
				Part2DatastreamTests.hasObservationShape(Map.of("id", "obs-1")));
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-ASSOCIATION-SUBRESOURCES-001.
	 */
	@org.junit.Test
	public void datastreamAssociationHelpersDetectExactReleasedAssociationEvidence() throws Exception {
		Map<String, Object> resource = Map.of("id", "ds-1", "samplingFeatures@link",
				List.of(Map.of("href", "datastreams/ds-1/samplingFeatures")), "featuresOfInterest@link",
				List.of(Map.of("href", "datastreams/ds-1/featuresOfInterest")), "deployments@link",
				List.of(Map.of("href", "deployments/deploy-1")));

		assertTrue(invokeAssociationHelper(resource, "samplingFeatures"));
		assertTrue(invokeAssociationHelper(resource, "featuresOfInterest"));
		assertTrue(invokeAssociationHelper(resource, "deployments"));
		assertFalse("Unrelated links must not satisfy a released Datastream association.", invokeAssociationHelper(
				Map.of("id", "ds-1", "links", List.of(Map.of("href", "systems/sys-1"))), "featuresOfInterest"));
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-COLLECTION-TAGGING-001.
	 */
	@org.junit.Test
	public void collectionTaggingRecognizesOnlyExactDatastreamAndObservationItemTypes() throws Exception {
		assertTrue(invokeCollectionTagHelper(Map.of("id", "datastreams", "itemType", "DataStream"), "DataStream"));
		assertTrue(invokeCollectionTagHelper(Map.of("id", "observations", "itemType", "Observation"), "Observation"));
		assertFalse("Feature collections are not DataStream collection-tagging evidence.",
				invokeCollectionTagHelper(Map.of("id", "datastreams", "itemType", "feature"), "DataStream"));
		assertFalse("Substring matches must not satisfy itemType.", invokeCollectionTagHelper(
				Map.of("id", "observations", "itemType", "ObservationCollection"), "Observation"));
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-RELEASED-PROCEDURES-001.
	 */
	@org.junit.Test
	public void canonicalSupportRequiresAdvertisedCanonicalLinkAndComparesWithoutThatLink() {
		URI page = URI.create("https://example.test/api/collections/datastreams/items");
		URI root = URI.create("https://example.test/api/");
		Map<String, Object> collectionItem = Map.of("id", "ds-1", "links",
				List.of(Map.of("rel", "canonical", "href", "https://example.test/api/datastreams/ds-1"),
						Map.of("rel", "alternate", "href", "../datastreams/ds-1?f=json")),
				"outputName", "out");
		Map<String, Object> canonicalBody = Map.of("id", "ds-1", "links",
				List.of(Map.of("rel", "alternate", "href", "../datastreams/ds-1?f=json")), "outputName", "out");

		assertEquals(root.resolve("datastreams/ds-1"),
				Part2DatastreamSupport.canonicalUri(collectionItem, page, root, BASE + "canonical-url"));
		assertEquals(Part2DatastreamSupport.withoutCanonicalLinks(collectionItem),
				Part2DatastreamSupport.withoutCanonicalLinks(canonicalBody));
		assertFalse("A synthesized /datastreams/{id} URL must not replace an advertised canonical link.",
				Part2DatastreamSupport.hasCanonicalLink(Map.of("id", "ds-1")));
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-EXACT-MAPPING-001.
	 */
	@org.junit.Test
	public void schemaOpExtractsEveryAdvertisedObservationFormat() {
		Map<String, Object> datastream = Map.of("id", "ds-1", "formats", List.of("application/json",
				Map.of("obsFormat", "application/swe+json"), Map.of("format", "application/swe+binary")));

		assertEquals(List.of("application/json", "application/swe+json", "application/swe+binary"),
				Part2DatastreamSupport.observationFormats(datastream));
		assertTrue("No advertised formats means schema-op is prerequisite-incomplete, not one unparameterized GET.",
				Part2DatastreamSupport.observationFormats(Map.of("id", "ds-1")).isEmpty());
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-COLLECTION-TAGGING-001.
	 */
	@org.junit.Test
	public void exactCollectionSelectionUsesItemTypeNotNameSubstrings() {
		List<Map<String, Object>> advertised = List.of(Map.of("id", "custom-streams", "itemType", "DataStream"),
				Map.of("id", "datastreams", "itemType", "feature"),
				Map.of("id", "observations", "itemType", "Observation"));

		assertEquals(List.of(advertised.get(0)),
				Part2DatastreamSupport.collectionsWithItemType(advertised, "DataStream"));
		assertEquals(List.of(advertised.get(2)),
				Part2DatastreamSupport.collectionsWithItemType(advertised, "Observation"));
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-ASSOCIATION-SUBRESOURCES-001.
	 */
	@org.junit.Test
	public void allResourceIdsRequireEveryApplicableSubresourceEndpoint() {
		List<Map<String, Object>> resources = List.of(Map.of("id", "a"), Map.of("id", "b"));

		assertEquals(List.of("a", "b"), Part2DatastreamSupport.localIds(resources, BASE + "resources-endpoint"));
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-ASSOCIATION-SUBRESOURCES-001.
	 */
	@org.junit.Test
	public void foiGeoJsonAllowsGenericFeatureCollectionWithoutSamplingFeatureMembers() {
		URI source = URI.create("https://example.test/api/datastreams/ds-1/featuresOfInterest");
		Map<String, Object> feature = Map.of("type", "Feature", "id", "foi-1", "geometry",
				Map.of("type", "Point", "coordinates", List.of(1.0, 2.0)), "properties", Map.of("name", "target"));
		Map<String, Object> body = Map.of("type", "FeatureCollection", "features", List.of(feature));

		Part2DatastreamSupport.validateFeatureOfInterestEndpoint(source,
				List.of(new PageDocument(source, Part2DatastreamSupport.GEOJSON, body, List.of(feature))),
				BASE + "foi-ref-from-datastream");
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-ASSOCIATION-SUBRESOURCES-001.
	 */
	@org.junit.Test
	public void foiApplicationJsonMustValidateAsGeoJsonFeatureCollection() {
		URI source = URI.create("https://example.test/api/datastreams/ds-1/featuresOfInterest");
		Map<String, Object> body = Map.of("items", List.of(Map.of("id", "foi-1")));

		assertThrows(AssertionError.class,
				() -> Part2DatastreamSupport.validateFeatureOfInterestEndpoint(source, List
					.of(new PageDocument(source, Part2DatastreamSupport.JSON, body, List.of(Map.of("id", "foi-1")))),
						BASE + "foi-ref-from-datastream"));
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-CONDITION-GATES-001.
	 */
	@org.junit.Test
	public void samplingFeatureConditionSkipsBeforeNestedEndpointAccess() throws Exception {
		try (FixtureServer fixture = new FixtureServer(conformanceJson(Part2DatastreamTests.CONF_DATASTREAM),
				datastreamCollection(null))) {
			Part2DatastreamTests tests = configured(fixture.root());

			SkipException error = assertThrows(SkipException.class,
					tests::datastreamSamplingFeaturesAreAvailableFromDatastream);

			assertTrue(error.getMessage().contains(Part2DatastreamTests.CONF_PART1_SAMPLING_FEATURES));
			assertEquals(0, fixture.datastreamGets.get());
			assertEquals(0, fixture.samplingFeatureNestedGets.get());
		}
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-CONDITION-GATES-001.
	 */
	@org.junit.Test
	public void samplingFeatureMixedUnsupportedMediaSkipsInsteadOfPartialPass() throws Exception {
		try (FixtureServer fixture = new FixtureServer(
				conformanceJson(Part2DatastreamTests.CONF_DATASTREAM,
						Part2DatastreamTests.CONF_PART1_SAMPLING_FEATURES),
				datastreamCollectionWithIds("ds-geo", "ds-json"))) {
			Part2DatastreamTests tests = configured(fixture.root());

			SkipException error = assertThrows(SkipException.class,
					tests::datastreamSamplingFeaturesAreAvailableFromDatastream);

			assertTrue(error.getMessage().contains("unsupported media"));
			assertEquals(2, fixture.samplingFeatureNestedGets.get());
		}
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-CONDITION-GATES-001.
	 */
	@org.junit.Test
	public void systemAndDeploymentConditionsSkipBeforeResourceEndpointAccess() throws Exception {
		try (FixtureServer fixture = new FixtureServer(conformanceJson(Part2DatastreamTests.CONF_DATASTREAM),
				datastreamCollection(null))) {
			Part2DatastreamTests tests = configured(fixture.root());

			assertThrows(SkipException.class, tests::systemScopedDatastreamsReadableWhenSystemReferencePresent);
			assertThrows(SkipException.class, tests::deploymentScopedDatastreamsReadableWhenDeploymentReferencePresent);

			assertEquals(0, fixture.systemGets.get());
			assertEquals(0, fixture.deploymentGets.get());
		}
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-CONDITION-GATES-001.
	 */
	@org.junit.Test
	public void foiConditionSkipsBeforeNestedEndpointAccessWhenAssociationAbsent() throws Exception {
		try (FixtureServer fixture = new FixtureServer(conformanceJson(Part2DatastreamTests.CONF_DATASTREAM),
				datastreamCollection(null))) {
			Part2DatastreamTests tests = configured(fixture.root());

			SkipException error = assertThrows(SkipException.class,
					tests::datastreamFeaturesOfInterestAreAvailableFromDatastream);

			assertTrue(error.getMessage().contains("featuresOfInterest association"));
			assertEquals(1, fixture.datastreamGets.get());
			assertEquals(0, fixture.featureOfInterestNestedGets.get());
		}
	}

	/**
	 * REQ-ETS-PART2-002; SCENARIO-ETS-PART2-002-CONDITION-GATES-001.
	 */
	@org.junit.Test
	public void localFeatureOfInterestConditionRequiresSameOriginOrInlineEvidence() {
		URI root = URI.create("https://example.test/api/");
		Map<String, Object> sameOrigin = Map.of("id", "ds-1", "featureOfInterest@link",
				Map.of("href", "features/foi-1", "rel", "featuresOfInterest"));
		Map<String, Object> inline = Map.of("id", "ds-2", "featuresOfInterest",
				List.of(Map.of("id", "foi-2", "type", "Feature")));
		Map<String, Object> crossOrigin = Map.of("id", "ds-3", "featureOfInterest@link",
				Map.of("href", "https://other.test/api/features/foi-3", "rel", "featuresOfInterest"));

		assertTrue(Part2DatastreamTests.hasLocalFeatureOfInterestCondition(List.of(sameOrigin), root));
		assertTrue(Part2DatastreamTests.hasLocalFeatureOfInterestCondition(List.of(inline), root));
		assertFalse(Part2DatastreamTests.hasLocalFeatureOfInterestCondition(List.of(crossOrigin), root));
		assertFalse(Part2DatastreamTests.hasLocalFeatureOfInterestCondition(List.of(Map.of("id", "ds-4")), root));
	}

	@org.junit.Test
	public void constantsUseOfficialDatastreamIdentifiers() {
		String joined = String.join(" ", Part2DatastreamTests.CONF_DATASTREAM, Part2DatastreamTests.REQ_DATASTREAM,
				Part2DatastreamTests.REQ_OBS_REF_FROM_DATASTREAM);

		assertTrue(joined.contains("/conf/datastream"));
		assertTrue(joined.contains("/req/datastream"));
		assertFalse(joined.contains("dynamic"));
	}

	private static boolean invokeAssociationHelper(Map<String, Object> resource, String association) throws Exception {
		Method method = Part2DatastreamTests.class.getDeclaredMethod("hasAssociationEvidence", Map.class, String.class);
		method.setAccessible(true);
		return (Boolean) method.invoke(null, resource, association);
	}

	private static boolean invokeCollectionTagHelper(Map<String, Object> collection, String itemType) throws Exception {
		Method method = Part2DatastreamTests.class.getDeclaredMethod("isCollectionTagged", Map.class, String.class);
		method.setAccessible(true);
		return (Boolean) method.invoke(null, collection, itemType);
	}

	private static Part2DatastreamTests configured(URI root) {
		Part2DatastreamTests tests = new Part2DatastreamTests();
		tests.configure(root);
		return tests;
	}

	private static String conformanceJson(String... declarations) {
		return "{\"conformsTo\":[\"" + String.join("\",\"", declarations) + "\"]}";
	}

	private static String datastreamCollection(String featureOfInterestHref) {
		return "{\"items\":[" + datastreamItemJson("ds-1", featureOfInterestHref) + "]}";
	}

	private static String datastreamCollectionWithIds(String... ids) {
		StringBuilder body = new StringBuilder("{\"items\":[");
		for (int i = 0; i < ids.length; i++) {
			if (i > 0) {
				body.append(',');
			}
			body.append(datastreamItemJson(ids[i], null));
		}
		return body.append("]}").toString();
	}

	private static String datastreamItemJson(String id, String featureOfInterestHref) {
		String feature = "";
		if (featureOfInterestHref != null) {
			feature = ",\"featureOfInterest@link\":{\"href\":\"" + featureOfInterestHref
					+ "\",\"rel\":\"featuresOfInterest\"}";
		}
		return "{\"id\":\"" + id + "\",\"name\":\"Stream\",\"formats\":[\"application/json\"],"
				+ "\"system@link\":{\"href\":\"https://example.test/api/systems/sys-1\"},"
				+ "\"observedProperties\":null,\"phenomenonTime\":null,\"resultTime\":null,"
				+ "\"resultType\":null,\"live\":false" + feature + "}";
	}

	private static final class FixtureServer implements AutoCloseable {

		private final HttpServer server;

		private final String conformance;

		private final String datastreams;

		private final AtomicInteger datastreamGets = new AtomicInteger();

		private final AtomicInteger samplingFeatureNestedGets = new AtomicInteger();

		private final AtomicInteger featureOfInterestNestedGets = new AtomicInteger();

		private final AtomicInteger systemGets = new AtomicInteger();

		private final AtomicInteger deploymentGets = new AtomicInteger();

		private FixtureServer(String conformance, String datastreams) throws IOException {
			this.conformance = conformance;
			this.datastreams = datastreams;
			this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
			this.server.createContext("/api/conformance", this::handleConformance);
			this.server.createContext("/api/datastreams", this::handleDatastreams);
			this.server.createContext("/api/systems", this::handleSystems);
			this.server.createContext("/api/deployments", this::handleDeployments);
			this.server.start();
		}

		private URI root() {
			return URI.create("http://127.0.0.1:" + this.server.getAddress().getPort() + "/api/");
		}

		private void handleConformance(HttpExchange exchange) throws IOException {
			respondJson(exchange, this.conformance);
		}

		private void handleDatastreams(HttpExchange exchange) throws IOException {
			String path = exchange.getRequestURI().getPath();
			if (path.endsWith("/samplingFeatures")) {
				this.samplingFeatureNestedGets.incrementAndGet();
				String mediaType = path.contains("/ds-geo/") ? Part2DatastreamSupport.GEOJSON
						: Part2DatastreamSupport.JSON;
				respond(exchange, mediaType, "{\"type\":\"FeatureCollection\",\"features\":[]}");
				return;
			}
			if (path.endsWith("/featuresOfInterest")) {
				this.featureOfInterestNestedGets.incrementAndGet();
				respondJson(exchange, "{\"type\":\"FeatureCollection\",\"features\":[]}");
				return;
			}
			this.datastreamGets.incrementAndGet();
			respondJson(exchange, this.datastreams);
		}

		private void handleSystems(HttpExchange exchange) throws IOException {
			this.systemGets.incrementAndGet();
			respondJson(exchange, datastreamCollection(null));
		}

		private void handleDeployments(HttpExchange exchange) throws IOException {
			this.deploymentGets.incrementAndGet();
			respondJson(exchange, datastreamCollection(null));
		}

		private void respondJson(HttpExchange exchange, String body) throws IOException {
			respond(exchange, Part2DatastreamSupport.JSON, body);
		}

		private void respond(HttpExchange exchange, String mediaType, String body) throws IOException {
			byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().set("Content-Type", mediaType);
			exchange.sendResponseHeaders(200, bytes.length);
			exchange.getResponseBody().write(bytes);
			exchange.close();
		}

		@Override
		public void close() {
			this.server.stop(0);
		}

	}

}
