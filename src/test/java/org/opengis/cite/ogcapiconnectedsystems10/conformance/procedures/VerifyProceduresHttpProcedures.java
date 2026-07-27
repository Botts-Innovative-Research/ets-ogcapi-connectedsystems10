package org.opengis.cite.ogcapiconnectedsystems10.conformance.procedures;

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
 * Controlled HTTP checks for all released Procedure procedures.
 */
public class VerifyProceduresHttpProcedures {

	/**
	 * REQ-ETS-PART1-006; SCENARIO-ETS-PART1-006-RELEASED-DIRECT-HTTP-COVERAGE-001.
	 */
	@Test
	public void allFiveReleasedProceduresExecuteSuccessfulHttpPaths() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.VALID)) {
			server.start();
			ProceduresTests tests = new ProceduresTests();
			tests.configure(server.apiRoot());

			tests.procedureLocationIsAbsent();
			tests.everyProcedureHasCanonicalUrl();
			tests.procedureResourcesEndpointIsValid();
			tests.canonicalProceduresEndpointIsValid();
			tests.procedureCollectionsAreValid();

			assertTrue(server.calls("/api/procedures") >= 3);
			assertTrue(server.calls("/api/collections") >= 2);
			assertTrue(server.calls("/api/collections/procedures-geo/items") >= 2);
			assertTrue(server.calls("/api/collections/procedures-sml/items") >= 2);
			assertTrue(server.calls("/api/procedures/procedure-geo") >= 1);
			assertTrue(server.calls("/api/procedures/procedure-sml") >= 1);
		}
	}

	/**
	 * REQ-ETS-PART1-006; SCENARIO-ETS-PART1-006-RELEASED-COLLECTION-COMPLETE-001.
	 */
	@Test
	public void missingProcedureCollectionsFailCanonicalAndCollectionProcedures() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.NO_COLLECTIONS)) {
			server.start();
			ProceduresTests tests = new ProceduresTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::everyProcedureHasCanonicalUrl);
			assertThrows(AssertionError.class, tests::procedureCollectionsAreValid);
		}
	}

	/**
	 * REQ-ETS-PART1-006; SCENARIO-ETS-PART1-006-RELEASED-MEDIA-GATE-001.
	 */
	@Test
	public void unsupportedLaterPageSkipsBeforeParsing() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNSUPPORTED_LATER_PAGE)) {
			server.start();
			ProceduresTests tests = new ProceduresTests();
			tests.configure(server.apiRoot());

			assertThrows(SkipException.class, tests::procedureLocationIsAbsent);
			assertThrows(SkipException.class, tests::procedureResourcesEndpointIsValid);
			assertEquals(2, server.calls("/api/procedures"));
			assertEquals(2, server.calls("/api/procedures-page-2"));
		}
	}

	/**
	 * REQ-ETS-PART1-006; SCENARIO-ETS-PART1-006-RELEASED-LOCATION-001.
	 */
	@Test
	public void representedLocationFailsForBothReleasedMediaTypes() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.NON_NULL_GEOJSON_LOCATION)) {
			server.start();
			ProceduresTests tests = new ProceduresTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::procedureLocationIsAbsent);
		}
		try (FixtureServer server = new FixtureServer(Mode.SENSORML_POSITION)) {
			server.start();
			ProceduresTests tests = new ProceduresTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::procedureLocationIsAbsent);
		}
	}

	/**
	 * REQ-ETS-PART1-006; SCENARIO-ETS-PART1-006-RELEASED-PROCEDURE-TYPE-001.
	 */
	@Test
	public void unrecognizedProcedureTypeFailsCollectionProcedure() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.INVALID_TYPE)) {
			server.start();
			ProceduresTests tests = new ProceduresTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::procedureCollectionsAreValid);
		}
	}

	/**
	 * REQ-ETS-PART1-006; SCENARIO-ETS-PART1-006-RELEASED-CANONICAL-EQUIVALENCE-001.
	 */
	@Test
	public void changedCanonicalContentFails() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.CANONICAL_DIFFERENCE)) {
			server.start();
			ProceduresTests tests = new ProceduresTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::everyProcedureHasCanonicalUrl);
		}
	}

	/**
	 * REQ-ETS-PART1-006; SCENARIO-ETS-PART1-006-RELEASED-CANONICAL-EQUIVALENCE-001.
	 */
	@Test
	public void laterComparableCanonicalRepresentationIsSelected() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNSUPPORTED_CANONICAL_FIRST)) {
			server.start();
			ProceduresTests tests = new ProceduresTests();
			tests.configure(server.apiRoot());

			tests.everyProcedureHasCanonicalUrl();
		}
	}

	/**
	 * REQ-ETS-PART1-006; SCENARIO-ETS-PART1-006-RELEASED-CANONICAL-EQUIVALENCE-001.
	 */
	@Test
	public void canonicalOnlyLinksEqualOmittedCanonicalLinks() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.CANONICAL_OMITS_LINKS)) {
			server.start();
			ProceduresTests tests = new ProceduresTests();
			tests.configure(server.apiRoot());

			tests.everyProcedureHasCanonicalUrl();
		}
	}

	private enum Mode {

		VALID, NO_COLLECTIONS, UNSUPPORTED_LATER_PAGE, NON_NULL_GEOJSON_LOCATION, SENSORML_POSITION, INVALID_TYPE,
		CANONICAL_DIFFERENCE, UNSUPPORTED_CANONICAL_FIRST, CANONICAL_OMITS_LINKS

	}

	private static final class FixtureServer implements AutoCloseable {

		private final HttpServer server;

		private final Mode mode;

		private final Map<String, AtomicInteger> calls = new ConcurrentHashMap<>();

		private FixtureServer(Mode mode) throws IOException {
			this.mode = mode;
			this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
			this.server.createContext("/api/collections", this::handle);
			this.server.createContext("/api/procedures", this::handle);
			this.server.createContext("/api/procedures-page-2", this::handle);
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
				case "/api/collections/procedures-geo/items" ->
					send(exchange, 200, "application/geo+json", geoCollection(geoProcedure()));
				case "/api/collections/procedures-sml/items" ->
					send(exchange, 200, "application/sml+json", sensorMlCollection(sensorMlProcedure()));
				case "/api/procedures" -> procedures(exchange);
				case "/api/procedures-page-2" ->
					send(exchange, 200, "application/json", "this is deliberately not JSON");
				case "/api/procedures/procedure-geo" -> {
					if (this.mode == Mode.UNSUPPORTED_CANONICAL_FIRST
							&& "f=html".equals(exchange.getRequestURI().getQuery())) {
						send(exchange, 200, "text/html", "<html><body>Procedure Geo</body></html>");
					}
					else {
						String body = this.mode == Mode.CANONICAL_DIFFERENCE
								? geoProcedure().replace("\"Procedure Geo\"", "\"Changed Procedure\"")
								: this.mode == Mode.CANONICAL_OMITS_LINKS ? geoProcedureWithoutLinks() : geoProcedure();
						send(exchange, 200, "application/geo+json", body);
					}
				}
				case "/api/procedures/procedure-sml" ->
					send(exchange, 200, "application/sml+json", sensorMlProcedure());
				default -> send(exchange, 404, "application/json", "{}");
			}
		}

		private void collections(HttpExchange exchange) throws IOException {
			if (this.mode == Mode.NO_COLLECTIONS) {
				send(exchange, 200, "application/json", "{\"collections\":[]}");
				return;
			}
			send(exchange, 200, "application/json", """
					{"collections":[
					  {"id":"procedures-geo","itemType":"feature","featureType":"sosa:Procedure",
					   "links":[{"rel":"items","type":"application/geo+json","href":"%s"}]},
					  {"id":"procedures-sml","itemType":"feature","featureType":"sosa:Procedure",
					   "links":[{"rel":"items","type":"application/sml+json","href":"%s"}]}
					]}
					""".formatted(apiRoot().resolve("collections/procedures-geo/items"),
					apiRoot().resolve("collections/procedures-sml/items")));
		}

		private void procedures(HttpExchange exchange) throws IOException {
			if (this.mode == Mode.UNSUPPORTED_LATER_PAGE) {
				send(exchange, 200, "application/geo+json", geoCollectionWithNext(geoProcedure()));
				return;
			}
			if (this.mode == Mode.SENSORML_POSITION) {
				send(exchange, 200, "application/sml+json", sensorMlCollection(sensorMlProcedureWithPosition()));
				return;
			}
			send(exchange, 200, "application/geo+json", geoCollection(geoProcedure()));
		}

		private String geoCollection(String procedure) {
			return "{\"type\":\"FeatureCollection\",\"features\":[" + procedure
					+ "],\"links\":[{\"rel\":\"self\",\"href\":\"" + apiRoot().resolve("procedures") + "\"}]}";
		}

		private String geoCollectionWithNext(String procedure) {
			return "{\"type\":\"FeatureCollection\",\"features\":[" + procedure
					+ "],\"links\":[{\"rel\":\"next\",\"href\":\"" + apiRoot().resolve("procedures-page-2") + "\"}]}";
		}

		private String sensorMlCollection(String procedure) {
			return "{\"items\":[" + procedure + "],\"links\":[{\"rel\":\"self\",\"href\":\""
					+ apiRoot().resolve("procedures") + "\"}]}";
		}

		private String geoProcedure() {
			String geometry = this.mode == Mode.NON_NULL_GEOJSON_LOCATION ? "{\"type\":\"Point\",\"coordinates\":[1,2]}"
					: "null";
			String procedureType = this.mode == Mode.INVALID_TYPE ? "sosa:Unknown" : "sosa:ObservingProcedure";
			if (this.mode == Mode.UNSUPPORTED_CANONICAL_FIRST) {
				return """
						{"type":"Feature","id":"procedure-geo","geometry":%s,
						 "properties":{"uid":"urn:ogc:procedure:geo","name":"Procedure Geo","featureType":"%s"},
						 "links":[
						   {"rel":"canonical","type":"text/html","href":"%s"},
						   {"rel":"canonical","type":"application/geo+json","href":"%s"}
						 ]}
						""".formatted(geometry, procedureType, apiRoot().resolve("procedures/procedure-geo?f=html"),
						apiRoot().resolve("procedures/procedure-geo?f=json"));
			}
			return """
					{"type":"Feature","id":"procedure-geo","geometry":%s,
					 "properties":{"uid":"urn:ogc:procedure:geo","name":"Procedure Geo","featureType":"%s"},
					 "links":[{"rel":"canonical","type":"application/geo+json","href":"%s"}]}
					""".formatted(geometry, procedureType, apiRoot().resolve("procedures/procedure-geo?f=json"));
		}

		private String geoProcedureWithoutLinks() {
			return """
					{"type":"Feature","id":"procedure-geo","geometry":null,
					 "properties":{"uid":"urn:ogc:procedure:geo","name":"Procedure Geo",
						 "featureType":"sosa:ObservingProcedure"}}
					""";
		}

		private String sensorMlProcedure() {
			return """
					{"type":"SimpleProcess","id":"procedure-sml","label":"Procedure SML",
					 "uniqueId":"urn:ogc:procedure:sml","definition":"sosa:ObservingProcedure",
					 "links":[{"rel":"canonical","type":"application/sml+json","href":"%s"}]}
					""".formatted(apiRoot().resolve("procedures/procedure-sml?f=sml"));
		}

		private String sensorMlProcedureWithPosition() {
			return sensorMlProcedure().replace("\"links\":", "\"position\":{},\"links\":");
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
