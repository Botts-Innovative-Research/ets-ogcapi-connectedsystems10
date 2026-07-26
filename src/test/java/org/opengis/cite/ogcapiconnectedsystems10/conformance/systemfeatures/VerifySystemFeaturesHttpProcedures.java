package org.opengis.cite.ogcapiconnectedsystems10.conformance.systemfeatures;

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

/**
 * Controlled HTTP checks for direct released System procedures.
 */
public class VerifySystemFeaturesHttpProcedures {

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-DIRECT-HTTP-COVERAGE-001.
	 */
	@Test
	public void allSixReleasedProceduresExecuteSuccessfulHttpPaths() throws Exception {
		try (FixtureServer server = new FixtureServer(true)) {
			server.start();
			SystemFeaturesTests tests = new SystemFeaturesTests();
			tests.configure(server.apiRoot(), "mobile");

			tests.systemLocationsFollowRecommendation();
			tests.mobileSystemLocationIsUpdated();
			tests.everySystemHasCanonicalUrl();
			tests.systemResourcesEndpointIsValid();
			tests.canonicalSystemsEndpointIsValid();
			tests.systemCollectionsAreValid();

			assertTrue(server.calls("/api/systems") >= 3);
			assertTrue(server.calls("/api/systems/mobile") >= 2);
			assertTrue(server.calls("/api/systems/system-1") >= 1);
			assertTrue(server.calls("/api/collections") >= 2);
			assertTrue(server.calls("/api/collections/systems/items") >= 2);
		}
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-MULTI-COLLECTION-001.
	 */
	@Test
	public void unsupportedCollectionDoesNotHideLaterInvalidSupportedCollection() throws Exception {
		try (FixtureServer server = new FixtureServer(false)) {
			server.start();
			SystemFeaturesTests tests = new SystemFeaturesTests();
			tests.configure(server.apiRoot(), null);

			assertThrows(AssertionError.class, tests::systemCollectionsAreValid);
		}
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-MULTI-COLLECTION-001.
	 */
	@Test
	public void unsupportedCollectionDoesNotHideLaterCanonicalFailure() throws Exception {
		try (FixtureServer server = new FixtureServer(false)) {
			server.start();
			SystemFeaturesTests tests = new SystemFeaturesTests();
			tests.configure(server.apiRoot(), null);

			assertThrows(AssertionError.class, tests::everySystemHasCanonicalUrl);
		}
	}

	private static final class FixtureServer implements AutoCloseable {

		private final HttpServer server;

		private final boolean valid;

		private final Map<String, AtomicInteger> calls = new ConcurrentHashMap<>();

		private FixtureServer(boolean valid) throws IOException {
			this.valid = valid;
			this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
			this.server.createContext("/api/collections", this::handle);
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
			if (this.valid) {
				handleValid(exchange, path);
				return;
			}
			if ("/api/collections".equals(path)) {
				send(exchange, 200, "application/json", """
						{"collections":[
						  {"id":"unsupported","featureType":"sosa:System","links":[
						    {"rel":"items","type":"application/xml"}
						  ]},
						  {"id":"supported","featureType":"sosa:System","links":[
						    {"rel":"items","type":"application/geo+json"}
						  ]}
						]}
						""");
				return;
			}
			if ("/api/collections/supported/items".equals(path)) {
				send(exchange, 200, "application/geo+json", """
						{"type":"FeatureCollection","features":[
						  {"type":"Feature","id":"system-1","geometry":null,
						   "properties":{"uid":"urn:ogc:system:1","featureType":"sosa:Procedure"},
						   "links":[]}
						]}
						""");
				return;
			}
			send(exchange, 404, "application/json", "{}");
		}

		private void handleValid(HttpExchange exchange, String path) throws IOException {
			if ("/api/collections".equals(path)) {
				send(exchange, 200, "application/json", """
						{"collections":[{
						  "id":"systems",
						  "featureType":"sosa:System",
						  "links":[{"rel":"items","type":"application/geo+json","href":"%s"}]
						}]}
						""".formatted(apiRoot().resolve("collections/systems/items")));
				return;
			}
			if ("/api/systems".equals(path) || "/api/collections/systems/items".equals(path)) {
				send(exchange, 200, "application/geo+json", featureCollection());
				return;
			}
			if ("/api/systems/system-1".equals(path)) {
				send(exchange, 200, "application/geo+json", systemFeature());
				return;
			}
			if ("/api/systems/mobile".equals(path)) {
				int call = calls(path);
				double longitude = call == 1 ? -77.0 : -76.5;
				send(exchange, 200, "application/geo+json", """
						{"type":"Feature","id":"mobile","geometry":{"type":"Point","coordinates":[%s,38.0]},
						 "properties":{"uid":"urn:ogc:system:mobile","name":"Mobile","featureType":"sosa:System"},
						 "links":[]}
						""".formatted(longitude));
				return;
			}
			send(exchange, 404, "application/json", "{}");
		}

		private String featureCollection() {
			return """
					{"type":"FeatureCollection","features":[%s],
					 "links":[{"rel":"self","type":"application/geo+json","href":"%s"}]}
					""".formatted(systemFeature(), apiRoot().resolve("systems"));
		}

		private String systemFeature() {
			return """
					{"type":"Feature","id":"system-1","geometry":{"type":"Point","coordinates":[-77.0,38.0]},
					 "properties":{"uid":"urn:ogc:system:1","name":"System 1","featureType":"sosa:System"},
					 "links":[{"rel":"canonical","type":"application/geo+json","href":"%s"}]}
					""".formatted(apiRoot().resolve("systems/system-1"));
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
