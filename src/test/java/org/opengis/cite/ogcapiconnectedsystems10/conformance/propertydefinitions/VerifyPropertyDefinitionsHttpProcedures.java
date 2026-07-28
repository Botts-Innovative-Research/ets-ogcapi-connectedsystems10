package org.opengis.cite.ogcapiconnectedsystems10.conformance.propertydefinitions;

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
 * Controlled HTTP checks for all released Property Definitions procedures.
 */
public class VerifyPropertyDefinitionsHttpProcedures {

	/**
	 * REQ-ETS-PART1-008; SCENARIO-ETS-PART1-008-RELEASED-DIRECT-HTTP-COVERAGE-001.
	 */
	@Test
	public void allFourReleasedProceduresExecuteSuccessfulHttpPaths() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.VALID)) {
			server.start();
			PropertyDefinitionsTests tests = new PropertyDefinitionsTests();
			tests.configure(server.apiRoot());

			tests.everyPropertyHasCanonicalUrl();
			tests.propertyResourcesEndpointIsValid();
			tests.canonicalPropertiesEndpointIsValid();
			tests.propertyCollectionsAreValid();

			assertTrue(server.calls("/api/properties") >= 2);
			assertTrue(server.calls("/api/collections") >= 2);
			assertTrue(server.calls("/api/collections/properties/items") >= 2);
			assertTrue(server.calls("/api/properties/property-1") >= 1);
		}
	}

	/**
	 * REQ-ETS-PART1-008; SCENARIO-ETS-PART1-008-RELEASED-COLLECTIONS-001.
	 */
	@Test
	public void missingPropertyCollectionFailsCollectionsAndSkipsCanonicalEvidence() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.NO_COLLECTIONS)) {
			server.start();
			PropertyDefinitionsTests tests = new PropertyDefinitionsTests();
			tests.configure(server.apiRoot());

			assertThrows(SkipException.class, tests::everyPropertyHasCanonicalUrl);
			assertThrows(AssertionError.class, tests::propertyCollectionsAreValid);
		}
	}

	/**
	 * REQ-ETS-PART1-008; SCENARIO-ETS-PART1-008-RELEASED-MEDIA-GATE-001.
	 */
	@Test
	public void unsupportedEndpointMediaSkipsBeforeParsing() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNSUPPORTED_ENDPOINT)) {
			server.start();
			PropertyDefinitionsTests tests = new PropertyDefinitionsTests();
			tests.configure(server.apiRoot());

			assertThrows(SkipException.class, tests::propertyResourcesEndpointIsValid);
			assertThrows(SkipException.class, tests::canonicalPropertiesEndpointIsValid);
			assertEquals(2, server.calls("/api/properties"));
		}
	}

	/**
	 * REQ-ETS-PART1-008; SCENARIO-ETS-PART1-008-RELEASED-SENSORML-SCHEMA-001.
	 */
	@Test
	public void invalidSupportedPropertySchemaFailsEndpointAndCollections() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.INVALID_SCHEMA)) {
			server.start();
			PropertyDefinitionsTests tests = new PropertyDefinitionsTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::propertyResourcesEndpointIsValid);
			assertThrows(AssertionError.class, tests::propertyCollectionsAreValid);
		}
	}

	/**
	 * REQ-ETS-PART1-008; SCENARIO-ETS-PART1-008-RELEASED-CANONICAL-EQUIVALENCE-001.
	 */
	@Test
	public void changedCanonicalContentFails() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.CANONICAL_DIFFERENCE)) {
			server.start();
			PropertyDefinitionsTests tests = new PropertyDefinitionsTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::everyPropertyHasCanonicalUrl);
		}
	}

	/**
	 * REQ-ETS-PART1-008; SCENARIO-ETS-PART1-008-RELEASED-CANONICAL-EQUIVALENCE-001.
	 */
	@Test
	public void laterComparableCanonicalRepresentationIsSelected() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNSUPPORTED_CANONICAL_FIRST)) {
			server.start();
			PropertyDefinitionsTests tests = new PropertyDefinitionsTests();
			tests.configure(server.apiRoot());

			tests.everyPropertyHasCanonicalUrl();
		}
	}

	/**
	 * REQ-ETS-PART1-008; SCENARIO-ETS-PART1-008-RELEASED-CANONICAL-EQUIVALENCE-001.
	 */
	@Test
	public void canonicalOnlyLinksEqualOmittedCanonicalLinks() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.CANONICAL_OMITS_LINKS)) {
			server.start();
			PropertyDefinitionsTests tests = new PropertyDefinitionsTests();
			tests.configure(server.apiRoot());

			tests.everyPropertyHasCanonicalUrl();
		}
	}

	/**
	 * REQ-ETS-PART1-008; SCENARIO-ETS-PART1-008-RELEASED-COLLECTION-COMPLETE-001.
	 */
	@Test
	public void earlierMediaLimitationDoesNotHideLaterCollectionDefect() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.MEDIA_THEN_INVALID_COLLECTION)) {
			server.start();
			PropertyDefinitionsTests tests = new PropertyDefinitionsTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::propertyCollectionsAreValid);
			assertThrows(AssertionError.class, tests::everyPropertyHasCanonicalUrl);
		}
	}

	/**
	 * REQ-ETS-PART1-008; SCENARIO-ETS-PART1-008-RELEASED-CANONICAL-URL-001.
	 */
	@Test
	public void emptyPropertyItemsSkipCanonicalEvidence() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.EMPTY_ITEMS)) {
			server.start();
			PropertyDefinitionsTests tests = new PropertyDefinitionsTests();
			tests.configure(server.apiRoot());

			assertThrows(SkipException.class, tests::everyPropertyHasCanonicalUrl);
		}
	}

	private enum Mode {

		VALID, NO_COLLECTIONS, UNSUPPORTED_ENDPOINT, INVALID_SCHEMA, CANONICAL_DIFFERENCE, UNSUPPORTED_CANONICAL_FIRST,
		CANONICAL_OMITS_LINKS, MEDIA_THEN_INVALID_COLLECTION, EMPTY_ITEMS

	}

	private static final class FixtureServer implements AutoCloseable {

		private final HttpServer server;

		private final Mode mode;

		private final Map<String, AtomicInteger> calls = new ConcurrentHashMap<>();

		private FixtureServer(Mode mode) throws IOException {
			this.mode = mode;
			this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
			this.server.createContext("/api/collections", this::handle);
			this.server.createContext("/api/properties", this::handle);
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
				case "/api/collections/properties/items" -> propertyItems(exchange, false);
				case "/api/collections/unsupported/items" ->
					send(exchange, 200, "application/json", "deliberately-not-json");
				case "/api/collections/invalid/items" -> propertyItems(exchange, true);
				case "/api/properties" -> properties(exchange);
				case "/api/properties/property-1" -> canonicalProperty(exchange);
				default -> send(exchange, 404, "application/json", "{}");
			}
		}

		private void collections(HttpExchange exchange) throws IOException {
			if (this.mode == Mode.NO_COLLECTIONS) {
				send(exchange, 200, "application/json", "{\"collections\":[]}");
				return;
			}
			if (this.mode == Mode.MEDIA_THEN_INVALID_COLLECTION) {
				send(exchange, 200, "application/json", """
						{"collections":[
						  {"id":"unsupported","itemType":"sosa:Property",
						   "links":[{"rel":"items","type":"application/json","href":"%s"}]},
						  {"id":"invalid","itemType":"sosa:Property",
						   "links":[{"rel":"items","type":"application/sml+json","href":"%s"}]}
						]}
						""".formatted(apiRoot().resolve("collections/unsupported/items"),
						apiRoot().resolve("collections/invalid/items")));
				return;
			}
			send(exchange, 200, "application/json", """
					{"collections":[
					  {"id":"properties","itemType":"sosa:Property",
					   "links":[{"rel":"items","type":"application/sml+json","href":"%s"}]}
					]}
					""".formatted(apiRoot().resolve("collections/properties/items")));
		}

		private void properties(HttpExchange exchange) throws IOException {
			if (this.mode == Mode.UNSUPPORTED_ENDPOINT) {
				send(exchange, 200, "application/json", "deliberately-not-json");
				return;
			}
			propertyItems(exchange, this.mode == Mode.INVALID_SCHEMA);
		}

		private void propertyItems(HttpExchange exchange, boolean invalid) throws IOException {
			if (this.mode == Mode.EMPTY_ITEMS) {
				send(exchange, 200, "application/sml+json", "{\"items\":[]}");
				return;
			}
			String property = invalid || this.mode == Mode.INVALID_SCHEMA
					|| this.mode == Mode.MEDIA_THEN_INVALID_COLLECTION ? invalidProperty() : property();
			send(exchange, 200, "application/sml+json", "{\"items\":[" + property + "]}");
		}

		private void canonicalProperty(HttpExchange exchange) throws IOException {
			if (this.mode == Mode.UNSUPPORTED_CANONICAL_FIRST && "html".equals(exchange.getRequestURI().getQuery())) {
				send(exchange, 200, "text/html", "<html><body>Temperature</body></html>");
				return;
			}
			String property = this.mode == Mode.CANONICAL_DIFFERENCE
					? propertyWithoutLinks().replace("\"Temperature\"", "\"Changed Temperature\"")
					: this.mode == Mode.CANONICAL_OMITS_LINKS ? propertyWithoutLinks() : property();
			send(exchange, 200, "application/sml+json", property);
		}

		private String property() {
			if (this.mode == Mode.UNSUPPORTED_CANONICAL_FIRST) {
				return """
						{"uniqueId":"urn:example:property:temperature","label":"Temperature",
						 "baseProperty":"https://qudt.org/vocab/quantitykind/Temperature",
						 "links":[
						   {"rel":"canonical","type":"text/html","href":"%s"},
						   {"rel":"canonical","type":"application/sml+json","href":"%s"}
						 ]}
						""".formatted(apiRoot().resolve("properties/property-1?html"),
						apiRoot().resolve("properties/property-1?sml"));
			}
			return """
					{"uniqueId":"urn:example:property:temperature","label":"Temperature",
					 "baseProperty":"https://qudt.org/vocab/quantitykind/Temperature",
					 "links":[{"rel":"canonical","type":"application/sml+json","href":"%s"}]}
					""".formatted(apiRoot().resolve("properties/property-1?sml"));
		}

		private String propertyWithoutLinks() {
			return """
					{"uniqueId":"urn:example:property:temperature","label":"Temperature",
					 "baseProperty":"https://qudt.org/vocab/quantitykind/Temperature"}
					""";
		}

		private String invalidProperty() {
			return """
					{"uniqueId":"urn:example:property:temperature","label":"Temperature",
					 "links":[{"rel":"canonical","type":"application/sml+json","href":"%s"}]}
					""".formatted(apiRoot().resolve("properties/wrong"));
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
