package org.opengis.cite.ogcapiconnectedsystems10.conformance.samplingfeatures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.Test;
import org.testng.SkipException;

/**
 * Controlled HTTP checks for all released Sampling Features procedures.
 */
public class VerifySamplingFeaturesHttpProcedures {

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-DIRECT-HTTP-COVERAGE-001.
	 */
	@Test
	public void allFiveReleasedProceduresExecuteSuccessfulHttpPaths() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.VALID)) {
			server.start();
			SamplingFeaturesTests tests = new SamplingFeaturesTests();
			tests.configure(server.apiRoot());

			tests.everySamplingFeatureHasCanonicalUrl();
			tests.samplingFeaturesResourcesEndpointIsValid();
			tests.canonicalSamplingFeaturesEndpointIsValid();
			tests.samplingFeatureCollectionsAreValid();
			tests.samplingFeaturesAreAvailableFromEverySystem();

			assertTrue(server.calls("/api/samplingFeatures") >= 2);
			assertTrue(server.calls("/api/collections") >= 2);
			assertTrue(server.calls("/api/collections/samples/items") >= 2);
			assertEquals(1, server.calls("/api/samplingFeatures/sf-1"));
			assertEquals(1, server.calls("/api/systems"));
			assertEquals(1, server.calls("/api/systems/sys-1/samplingFeatures"));
			assertEquals(1, server.calls("/api/systems/sys-1/samplingFeatures-page-2"));
			assertEquals(1, server.calls("/api/systems/sys-2/samplingFeatures"));
		}
	}

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-COLLECTION-COMPLETE-001.
	 */
	@Test
	public void missingSamplingFeatureCollectionsCannotPass() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.NO_COLLECTIONS)) {
			server.start();
			SamplingFeaturesTests tests = new SamplingFeaturesTests();
			tests.configure(server.apiRoot());

			assertThrows(SkipException.class, tests::everySamplingFeatureHasCanonicalUrl);
			assertThrows(AssertionError.class, tests::samplingFeatureCollectionsAreValid);
		}
	}

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-MEDIA-GATE-001.
	 */
	@Test
	public void unsupportedEndpointMediaSkipsBeforeParsing() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNSUPPORTED_ENDPOINT)) {
			server.start();
			SamplingFeaturesTests tests = new SamplingFeaturesTests();
			tests.configure(server.apiRoot());

			assertThrows(SkipException.class, tests::samplingFeaturesResourcesEndpointIsValid);
			assertEquals(1, server.calls("/api/samplingFeatures"));
		}
	}

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-RESOURCES-ENDPOINT-001.
	 */
	@Test
	public void invalidSupportedGeoJsonFailsEndpointAndCollectionProcedures() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.INVALID_SCHEMA)) {
			server.start();
			SamplingFeaturesTests tests = new SamplingFeaturesTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::samplingFeaturesResourcesEndpointIsValid);
			assertThrows(AssertionError.class, tests::samplingFeatureCollectionsAreValid);
		}
	}

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-CANONICAL-EQUIVALENCE-001.
	 */
	@Test
	public void wrongCanonicalPathAndChangedContentFail() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.WRONG_CANONICAL_PATH)) {
			server.start();
			SamplingFeaturesTests tests = new SamplingFeaturesTests();
			tests.configure(server.apiRoot());
			assertThrows(AssertionError.class, tests::everySamplingFeatureHasCanonicalUrl);
		}
		try (FixtureServer server = new FixtureServer(Mode.CANONICAL_DIFFERENCE)) {
			server.start();
			SamplingFeaturesTests tests = new SamplingFeaturesTests();
			tests.configure(server.apiRoot());
			assertThrows(AssertionError.class, tests::everySamplingFeatureHasCanonicalUrl);
		}
	}

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-SYSTEM-COMPLETE-001.
	 */
	@Test
	public void laterSystemFailureIsNotHidden() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.SECOND_SYSTEM_FAILS)) {
			server.start();
			SamplingFeaturesTests tests = new SamplingFeaturesTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::samplingFeaturesAreAvailableFromEverySystem);
			assertEquals(1, server.calls("/api/systems/sys-1/samplingFeatures-page-2"));
			assertEquals(1, server.calls("/api/systems/sys-2/samplingFeatures"));
		}
	}

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-COLLECTION-COMPLETE-001.
	 */
	@Test
	public void unsupportedCollectionRepresentationsSkipRatherThanPass() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNSUPPORTED_COLLECTION)) {
			server.start();
			SamplingFeaturesTests tests = new SamplingFeaturesTests();
			tests.configure(server.apiRoot());

			assertThrows(SkipException.class, tests::everySamplingFeatureHasCanonicalUrl);
			assertThrows(SkipException.class, tests::samplingFeatureCollectionsAreValid);
		}
	}

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-COLLECTION-COMPLETE-001.
	 */
	@Test
	public void partiallyUnsupportedCollectionsCannotProduceCanonicalPass() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.PARTIALLY_UNSUPPORTED_COLLECTION)) {
			server.start();
			SamplingFeaturesTests tests = new SamplingFeaturesTests();
			tests.configure(server.apiRoot());

			assertThrows(SkipException.class, tests::everySamplingFeatureHasCanonicalUrl);
			assertThrows(SkipException.class, tests::samplingFeatureCollectionsAreValid);
			assertTrue(server.calls("/api/collections/samples/items") > 0);
			assertEquals(0, server.calls("/api/collections/unsupported-samples/items"));
		}
	}

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-REF-FROM-SYSTEM-001.
	 */
	@Test
	public void nestedGeoJsonSchemaIsAppliedToLaterPagesAndSystems() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.INVALID_NESTED_LATER_PAGE)) {
			server.start();
			SamplingFeaturesTests tests = new SamplingFeaturesTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::samplingFeaturesAreAvailableFromEverySystem);
			assertEquals(1, server.calls("/api/systems/sys-1/samplingFeatures-page-2"));
		}
		try (FixtureServer server = new FixtureServer(Mode.INVALID_NESTED_LATER_SYSTEM)) {
			server.start();
			SamplingFeaturesTests tests = new SamplingFeaturesTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::samplingFeaturesAreAvailableFromEverySystem);
			assertEquals(1, server.calls("/api/systems/sys-2/samplingFeatures"));
		}
	}

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-COLLECTION-COMPLETE-001.
	 */
	@Test
	public void actualMediaLimitationDoesNotHideLaterCollectionDefects() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.MEDIA_THEN_INVALID_COLLECTION)) {
			server.start();
			SamplingFeaturesTests tests = new SamplingFeaturesTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::everySamplingFeatureHasCanonicalUrl);
			assertEquals(1, server.calls("/api/collections/samples/items"));
		}
		try (FixtureServer server = new FixtureServer(Mode.MEDIA_THEN_INVALID_COLLECTION)) {
			server.start();
			SamplingFeaturesTests tests = new SamplingFeaturesTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::samplingFeatureCollectionsAreValid);
			assertEquals(1, server.calls("/api/collections/samples/items"));
		}
	}

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-CANONICAL-EQUIVALENCE-001.
	 */
	@Test
	public void canonicalMediaLimitationDoesNotHideLaterItemDefect() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.CANONICAL_MEDIA_THEN_ITEM_DEFECT)) {
			server.start();
			SamplingFeaturesTests tests = new SamplingFeaturesTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::everySamplingFeatureHasCanonicalUrl);
		}
	}

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-SYSTEM-COMPLETE-001.
	 */
	@Test
	public void systemMediaLimitationDoesNotHideLaterSystemFailure() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.SYSTEM_MEDIA_THEN_FAILURE)) {
			server.start();
			SamplingFeaturesTests tests = new SamplingFeaturesTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::samplingFeaturesAreAvailableFromEverySystem);
			assertEquals(1, server.calls("/api/systems/sys-2/samplingFeatures"));
		}
	}

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-COLLECTION-COMPLETE-001.
	 */
	@Test
	public void actualMediaLimitationsAggregateAfterLaterValidEvidence() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.MEDIA_THEN_VALID_COLLECTION)) {
			server.start();
			SamplingFeaturesTests tests = new SamplingFeaturesTests();
			tests.configure(server.apiRoot());

			assertThrows(SkipException.class, tests::everySamplingFeatureHasCanonicalUrl);
			assertEquals(1, server.calls("/api/collections/samples/items"));
		}
		try (FixtureServer server = new FixtureServer(Mode.MEDIA_THEN_VALID_COLLECTION)) {
			server.start();
			SamplingFeaturesTests tests = new SamplingFeaturesTests();
			tests.configure(server.apiRoot());

			assertThrows(SkipException.class, tests::samplingFeatureCollectionsAreValid);
			assertEquals(1, server.calls("/api/collections/samples/items"));
		}
	}

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-COLLECTION-COMPLETE-001.
	 */
	@Test
	public void unsupportedLaterPageCannotHideEarlierCanonicalDefect() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.INVALID_PAGE_THEN_MEDIA)) {
			server.start();
			SamplingFeaturesTests tests = new SamplingFeaturesTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::everySamplingFeatureHasCanonicalUrl);
		}
	}

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-COLLECTION-COMPLETE-001.
	 */
	@Test
	public void unsupportedLaterPageCannotHideEarlierCollectionSchemaDefect() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.INVALID_PAGE_THEN_MEDIA)) {
			server.start();
			SamplingFeaturesTests tests = new SamplingFeaturesTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::samplingFeatureCollectionsAreValid);
		}
	}

	/**
	 * REQ-ETS-PART1-007; SCENARIO-ETS-PART1-007-RELEASED-REF-FROM-SYSTEM-001.
	 */
	@Test
	public void unsupportedLaterNestedPageCannotHideEarlierSchemaDefect() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.INVALID_PAGE_THEN_MEDIA)) {
			server.start();
			SamplingFeaturesTests tests = new SamplingFeaturesTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::samplingFeaturesAreAvailableFromEverySystem);
		}
	}

	private enum Mode {

		VALID, NO_COLLECTIONS, UNSUPPORTED_ENDPOINT, INVALID_SCHEMA, WRONG_CANONICAL_PATH, CANONICAL_DIFFERENCE,
		SECOND_SYSTEM_FAILS, UNSUPPORTED_COLLECTION, PARTIALLY_UNSUPPORTED_COLLECTION, INVALID_NESTED_LATER_PAGE,
		INVALID_NESTED_LATER_SYSTEM, MEDIA_THEN_INVALID_COLLECTION, CANONICAL_MEDIA_THEN_ITEM_DEFECT,
		SYSTEM_MEDIA_THEN_FAILURE, MEDIA_THEN_VALID_COLLECTION, INVALID_PAGE_THEN_MEDIA

	}

	private static final class FixtureServer implements AutoCloseable {

		private final HttpServer server;

		private final Mode mode;

		private final Map<String, AtomicInteger> calls = new ConcurrentHashMap<>();

		private FixtureServer(Mode mode) throws IOException {
			this.mode = mode;
			this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
			this.server.createContext("/api/collections", this::handle);
			this.server.createContext("/api/samplingFeatures", this::handle);
			this.server.createContext("/api/systems", this::handle);
		}

		private void start() {
			this.server.start();
		}

		private URI apiRoot() {
			return URI.create("http://127.0.0.1:" + this.server.getAddress().getPort() + "/api/");
		}

		private int calls(String path) {
			AtomicInteger count = this.calls.get(path);
			return count == null ? 0 : count.get();
		}

		private void handle(HttpExchange exchange) throws IOException {
			String path = exchange.getRequestURI().getPath();
			this.calls.computeIfAbsent(path, ignored -> new AtomicInteger()).incrementAndGet();
			switch (path) {
				case "/api/collections" -> collections(exchange);
				case "/api/collections/samples/items", "/api/collections/unsupported-actual/items",
						"/api/collections/unsupported-actual/items-page-2" ->
					collectionItems(exchange);
				case "/api/samplingFeatures" -> samplingFeatures(exchange);
				case "/api/samplingFeatures/sf-1", "/api/samplingFeatures/sf-2" -> canonicalSamplingFeature(exchange);
				case "/api/systems" ->
					send(exchange, 200, "application/json", "{\"items\":[{\"id\":\"sys-1\"},{\"id\":\"sys-2\"}]}");
				case "/api/systems/sys-1/samplingFeatures" -> firstSystemSamplingFeatures(exchange);
				case "/api/systems/sys-1/samplingFeatures-page-2" -> secondSystemPage(exchange);
				case "/api/systems/sys-2/samplingFeatures" -> {
					if (this.mode == Mode.SECOND_SYSTEM_FAILS || this.mode == Mode.SYSTEM_MEDIA_THEN_FAILURE) {
						send(exchange, 500, "application/json", "{}");
					}
					else if (this.mode == Mode.INVALID_NESTED_LATER_SYSTEM) {
						send(exchange, 200, "application/geo+json", geoCollection(invalidSamplingFeature()));
					}
					else {
						send(exchange, 200, "application/geo+json", geoCollection(samplingFeature()));
					}
				}
				default -> send(exchange, 404, "application/json", "{}");
			}
		}

		private void collections(HttpExchange exchange) throws IOException {
			if (this.mode == Mode.NO_COLLECTIONS) {
				send(exchange, 200, "application/json", "{\"collections\":[]}");
				return;
			}
			if (this.mode == Mode.PARTIALLY_UNSUPPORTED_COLLECTION) {
				send(exchange, 200, "application/json", """
						{"collections":[
						  {"id":"samples","itemType":"feature","featureType":"sosa:Sample",
						   "links":[{"rel":"items","type":"application/geo+json","href":"%s"}]},
						  {"id":"unsupported-samples","itemType":"feature","featureType":"sosa:Sample",
						   "links":[{"rel":"items","type":"application/json","href":"%s"}]}
						]}
						""".formatted(apiRoot().resolve("collections/samples/items"),
						apiRoot().resolve("collections/unsupported-samples/items")));
				return;
			}
			if (this.mode == Mode.MEDIA_THEN_INVALID_COLLECTION || this.mode == Mode.MEDIA_THEN_VALID_COLLECTION
					|| this.mode == Mode.INVALID_PAGE_THEN_MEDIA) {
				if (this.mode == Mode.INVALID_PAGE_THEN_MEDIA) {
					send(exchange, 200, "application/json", """
							{"collections":[
							  {"id":"unsupported-actual","itemType":"feature","featureType":"sosa:Sample",
							   "links":[{"rel":"items","type":"application/geo+json","href":"%s"}]}
							]}
							""".formatted(apiRoot().resolve("collections/unsupported-actual/items")));
					return;
				}
				send(exchange, 200, "application/json", """
						{"collections":[
						  {"id":"unsupported-actual","itemType":"feature","featureType":"sosa:Sample",
						   "links":[{"rel":"items","type":"application/geo+json","href":"%s"}]},
						  {"id":"samples","itemType":"feature","featureType":"sosa:Sample",
						   "links":[{"rel":"items","type":"application/geo+json","href":"%s"}]}
						]}
						""".formatted(apiRoot().resolve("collections/unsupported-actual/items"),
						apiRoot().resolve("collections/samples/items")));
				return;
			}
			String mediaType = this.mode == Mode.UNSUPPORTED_COLLECTION ? "application/json" : "application/geo+json";
			send(exchange, 200, "application/json", """
					{"collections":[
					  {"id":"samples","itemType":"feature","featureType":"sosa:Sample",
					   "links":[{"rel":"items","type":"%s","href":"%s"}]}
					]}
					""".formatted(mediaType, apiRoot().resolve("collections/samples/items")));
		}

		private void collectionItems(HttpExchange exchange) throws IOException {
			String path = exchange.getRequestURI().getPath();
			if ("/api/collections/unsupported-actual/items".equals(path)) {
				String feature = this.mode == Mode.INVALID_PAGE_THEN_MEDIA ? invalidSamplingFeatureWithWrongCanonical()
						: "";
				send(exchange, 200, "application/geo+json",
						"{\"type\":\"FeatureCollection\",\"features\":[" + feature
								+ "],\"links\":[{\"rel\":\"next\",\"href\":\""
								+ apiRoot().resolve("collections/unsupported-actual/items-page-2") + "\"}]}");
				return;
			}
			if ("/api/collections/unsupported-actual/items-page-2".equals(path)) {
				send(exchange, 200, "application/json", "{\"items\":[]}");
				return;
			}
			if (this.mode == Mode.UNSUPPORTED_COLLECTION) {
				send(exchange, 200, "application/json", "{\"items\":[]}");
				return;
			}
			String feature = this.mode == Mode.INVALID_SCHEMA || this.mode == Mode.MEDIA_THEN_INVALID_COLLECTION
					? invalidSamplingFeatureWithWrongCanonical() : this.mode == Mode.CANONICAL_MEDIA_THEN_ITEM_DEFECT
							? canonicalMediaThenItemDefectCollection() : samplingFeature();
			if (this.mode == Mode.CANONICAL_MEDIA_THEN_ITEM_DEFECT) {
				send(exchange, 200, "application/geo+json", feature);
				return;
			}
			send(exchange, 200, "application/geo+json", geoCollection(feature));
		}

		private void samplingFeatures(HttpExchange exchange) throws IOException {
			if (this.mode == Mode.UNSUPPORTED_ENDPOINT) {
				send(exchange, 200, "application/json", "not-json-and-must-not-be-parsed");
				return;
			}
			String feature = this.mode == Mode.INVALID_SCHEMA ? invalidSamplingFeature() : samplingFeature();
			send(exchange, 200, "application/geo+json", geoCollection(feature));
		}

		private void canonicalSamplingFeature(HttpExchange exchange) throws IOException {
			String feature = canonicalSamplingFeature();
			if (this.mode == Mode.CANONICAL_DIFFERENCE) {
				feature = feature.replace("\"Sample One\"", "\"Changed Sample\"");
			}
			String mediaType = this.mode == Mode.CANONICAL_MEDIA_THEN_ITEM_DEFECT
					&& exchange.getRequestURI().getPath().endsWith("/sf-1") ? "application/json"
							: "application/geo+json";
			send(exchange, 200, mediaType, feature);
		}

		private void firstSystemSamplingFeatures(HttpExchange exchange) throws IOException {
			if (this.mode == Mode.SYSTEM_MEDIA_THEN_FAILURE) {
				send(exchange, 200, "text/plain", "unsupported");
				return;
			}
			String feature = this.mode == Mode.INVALID_PAGE_THEN_MEDIA ? invalidSamplingFeature() : samplingFeature();
			send(exchange, 200, "application/geo+json",
					"{\"type\":\"FeatureCollection\",\"features\":[" + feature
							+ "],\"links\":[{\"rel\":\"next\",\"href\":\""
							+ apiRoot().resolve("systems/sys-1/samplingFeatures-page-2") + "\"}]}");
		}

		private void secondSystemPage(HttpExchange exchange) throws IOException {
			if (this.mode == Mode.INVALID_PAGE_THEN_MEDIA) {
				send(exchange, 200, "text/plain", "unsupported");
				return;
			}
			String feature = this.mode == Mode.INVALID_NESTED_LATER_PAGE ? invalidSamplingFeature() : samplingFeature();
			send(exchange, 200, "application/geo+json", geoCollection(feature));
		}

		private String geoCollection(String feature) {
			return "{\"type\":\"FeatureCollection\",\"features\":[" + feature + "]}";
		}

		private String samplingFeature() {
			String canonical = this.mode == Mode.WRONG_CANONICAL_PATH
					? apiRoot().resolve("samplingFeatures/sf-2").toString()
					: apiRoot().resolve("samplingFeatures/sf-1?f=geojson").toString();
			return """
					{"type":"Feature","id":"sf-1","geometry":{"type":"Point","coordinates":[1,2]},
					 "properties":{"uid":"urn:ogc:sf:1","name":"Sample One","featureType":"sosa:Sample",
					   "sampledFeature@link":{"href":"https://example.test/features/foi-1"}},
					 "links":[{"rel":"canonical","type":"application/geo+json","href":"%s"}]}
					""".formatted(canonical);
		}

		private String canonicalSamplingFeature() {
			return """
					{"type":"Feature","id":"sf-1","geometry":{"type":"Point","coordinates":[1,2]},
					 "properties":{"uid":"urn:ogc:sf:1","name":"Sample One","featureType":"sosa:Sample",
					   "sampledFeature@link":{"href":"https://example.test/features/foi-1"}}}
					""";
		}

		private String invalidSamplingFeature() {
			return """
					{"type":"Feature","id":"sf-1","geometry":{"type":"Point","coordinates":[1,2]},
					 "properties":{"uid":"urn:ogc:sf:1","name":"Sample One","featureType":"sosa:Sample"}}
					""";
		}

		private String invalidSamplingFeatureWithWrongCanonical() {
			return """
					{"type":"Feature","id":"sf-1","geometry":{"type":"Point","coordinates":[1,2]},
					 "properties":{"uid":"urn:ogc:sf:1","name":"Sample One","featureType":"sosa:Sample"},
					 "links":[{"rel":"canonical","type":"application/geo+json","href":"%s"}]}
					""".formatted(apiRoot().resolve("samplingFeatures/wrong"));
		}

		private String canonicalMediaThenItemDefectCollection() {
			String first = samplingFeature();
			String second = samplingFeature().replace("\"id\":\"sf-1\"", "\"id\":\"sf-2\"")
				.replace(apiRoot().resolve("samplingFeatures/sf-1?f=geojson").toString(),
						apiRoot().resolve("samplingFeatures/wrong").toString());
			return "{\"type\":\"FeatureCollection\",\"features\":[" + first + "," + second + "]}";
		}

		private static void send(HttpExchange exchange, int status, String contentType, String body)
				throws IOException {
			byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().set("Content-Type", contentType);
			exchange.sendResponseHeaders(status, bytes.length);
			try (OutputStream output = exchange.getResponseBody()) {
				output.write(bytes);
			}
		}

		@Override
		public void close() {
			this.server.stop(0);
		}

	}

}
