package org.opengis.cite.ogcapiconnectedsystems10.conformance.deployments;

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
 * Controlled HTTP checks for all released Deployment procedures.
 */
public class VerifyDeploymentsHttpProcedures {

	/**
	 * REQ-ETS-PART1-004; SCENARIO-ETS-PART1-004-RELEASED-DIRECT-HTTP-COVERAGE-001.
	 */
	@Test
	public void allFiveReleasedProceduresExecuteSuccessfulHttpPaths() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.VALID)) {
			server.start();
			DeploymentsTests tests = new DeploymentsTests();
			tests.configure(server.apiRoot());

			tests.everyDeploymentHasCanonicalUrl();
			tests.deploymentResourcesEndpointIsValid();
			tests.canonicalDeploymentsEndpointIsValid();
			tests.deploymentCollectionsAreValid();
			tests.deploymentsReferencedFromSystemsAreValid();

			assertTrue(server.calls("/api/collections") >= 2);
			assertTrue(server.calls("/api/collections/deployments-geo/items") >= 2);
			assertTrue(server.calls("/api/collections/deployments-sml/items") >= 2);
			assertTrue(server.calls("/api/deployments") >= 2);
			assertTrue(server.calls("/api/deployments/deployment-geo") >= 1);
			assertTrue(server.calls("/api/deployments/deployment-sml") >= 1);
			assertEquals(1, server.calls("/api/systems"));
			assertEquals(1, server.calls("/api/systems/system-1/deployments"));
		}
	}

	/**
	 * REQ-ETS-PART1-004; SCENARIO-ETS-PART1-004-RELEASED-COLLECTION-COMPLETE-001.
	 */
	@Test
	public void missingDeploymentCollectionsFailCanonicalAndCollectionProcedures() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.NO_COLLECTIONS)) {
			server.start();
			DeploymentsTests tests = new DeploymentsTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::everyDeploymentHasCanonicalUrl);
			assertThrows(AssertionError.class, tests::deploymentCollectionsAreValid);
		}
	}

	/**
	 * REQ-ETS-PART1-004; SCENARIO-ETS-PART1-004-RELEASED-MEDIA-GATE-001.
	 */
	@Test
	public void unsupportedLaterPageSkipsBeforeParsing() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNSUPPORTED_LATER_PAGE)) {
			server.start();
			DeploymentsTests tests = new DeploymentsTests();
			tests.configure(server.apiRoot());

			assertThrows(SkipException.class, tests::deploymentResourcesEndpointIsValid);
			assertEquals(1, server.calls("/api/deployments"));
			assertEquals(1, server.calls("/api/deployments-page-2"));
		}
	}

	/**
	 * REQ-ETS-PART1-004; SCENARIO-ETS-PART1-004-RELEASED-CANONICAL-EQUIVALENCE-001.
	 */
	@Test
	public void changedCanonicalContentFails() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.CANONICAL_DIFFERENCE)) {
			server.start();
			DeploymentsTests tests = new DeploymentsTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::everyDeploymentHasCanonicalUrl);
		}
	}

	/**
	 * REQ-ETS-PART1-004; SCENARIO-ETS-PART1-004-RELEASED-SYSTEM-REFERENCE-001.
	 */
	@Test
	public void wrongNestedSystemReferenceFails() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.WRONG_SYSTEM_REFERENCE)) {
			server.start();
			DeploymentsTests tests = new DeploymentsTests();
			tests.configure(server.apiRoot());

			assertThrows(AssertionError.class, tests::deploymentsReferencedFromSystemsAreValid);
		}
	}

	private enum Mode {

		VALID, NO_COLLECTIONS, UNSUPPORTED_LATER_PAGE, CANONICAL_DIFFERENCE, WRONG_SYSTEM_REFERENCE

	}

	private static final class FixtureServer implements AutoCloseable {

		private final HttpServer server;

		private final Mode mode;

		private final Map<String, AtomicInteger> calls = new ConcurrentHashMap<>();

		private FixtureServer(Mode mode) throws IOException {
			this.mode = mode;
			this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
			this.server.createContext("/api/collections", this::handle);
			this.server.createContext("/api/deployments", this::handle);
			this.server.createContext("/api/deployments-page-2", this::handle);
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
				case "/api/collections/deployments-geo/items" ->
					send(exchange, 200, "application/geo+json", geoCollection(geoDeployment()));
				case "/api/collections/deployments-sml/items" ->
					send(exchange, 200, "application/sml+json", sensorMlCollection());
				case "/api/deployments" -> deployments(exchange);
				case "/api/deployments-page-2" ->
					send(exchange, 200, "application/json", "this is deliberately not JSON");
				case "/api/deployments/deployment-geo" -> {
					String body = this.mode == Mode.CANONICAL_DIFFERENCE
							? geoDeployment().replace("\"Deployment Geo\"", "\"Changed Deployment\"") : geoDeployment();
					send(exchange, 200, "application/geo+json", body);
				}
				case "/api/deployments/deployment-sml" ->
					send(exchange, 200, "application/sml+json", sensorMlDeployment("system-1"));
				case "/api/systems" ->
					send(exchange, 200, "application/json", "{\"items\":[{\"id\":\"system-1\"}],\"links\":[]}");
				case "/api/systems/system-1/deployments" -> send(exchange, 200, "application/geo+json", geoCollection(
						geoDeploymentForSystem(this.mode == Mode.WRONG_SYSTEM_REFERENCE ? "system-10" : "system-1")));
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
					  {"id":"deployments-geo","itemType":"feature","featureType":"sosa:Deployment",
					   "links":[{"rel":"items","type":"application/geo+json","href":"%s"}]},
					  {"id":"deployments-sml","itemType":"feature","featureType":"sosa:Deployment",
					   "links":[{"rel":"items","type":"application/sml+json","href":"%s"}]}
					]}
					""".formatted(apiRoot().resolve("collections/deployments-geo/items"),
					apiRoot().resolve("collections/deployments-sml/items")));
		}

		private void deployments(HttpExchange exchange) throws IOException {
			if (this.mode == Mode.UNSUPPORTED_LATER_PAGE) {
				send(exchange, 200, "application/geo+json", geoCollectionWithNext(geoDeployment()));
				return;
			}
			send(exchange, 200, "application/geo+json", geoCollection(geoDeployment()));
		}

		private String geoCollection(String deployment) {
			return "{\"type\":\"FeatureCollection\",\"features\":[" + deployment
					+ "],\"links\":[{\"rel\":\"self\",\"href\":\"" + apiRoot().resolve("deployments") + "\"}]}";
		}

		private String geoCollectionWithNext(String deployment) {
			return "{\"type\":\"FeatureCollection\",\"features\":[" + deployment
					+ "],\"links\":[{\"rel\":\"next\",\"href\":\"" + apiRoot().resolve("deployments-page-2") + "\"}]}";
		}

		private String sensorMlCollection() {
			return "{\"items\":[" + sensorMlDeployment("system-1") + "],\"links\":[{\"rel\":\"self\",\"href\":\""
					+ apiRoot().resolve("deployments") + "\"}]}";
		}

		private String geoDeployment() {
			return geoDeploymentForSystem("system-1");
		}

		private String geoDeploymentForSystem(String systemId) {
			return """
					{"type":"Feature","id":"deployment-geo","geometry":null,
					 "properties":{"uid":"urn:ogc:deployment:geo","name":"Deployment Geo",
					   "featureType":"sosa:Deployment",
					   "validTime":["2026-01-01T00:00:00Z","2026-12-31T00:00:00Z"],
					   "deployedSystems@link":[{"href":"%s"}]},
					 "links":[{"rel":"canonical","type":"application/geo+json","href":"%s"}]}
					""".formatted(apiRoot().resolve("systems/" + systemId),
					apiRoot().resolve("deployments/deployment-geo?f=json"));
		}

		private String sensorMlDeployment(String systemId) {
			return """
					{"type":"Deployment","id":"deployment-sml","label":"Deployment SML",
					 "uniqueId":"urn:ogc:deployment:sml","definition":"sosa:Deployment",
					 "deployedSystems":[{"name":"sensor","system":{"href":"%s"}}],
					 "links":[{"rel":"canonical","type":"application/sml+json","href":"%s"}]}
					""".formatted(apiRoot().resolve("systems/" + systemId),
					apiRoot().resolve("deployments/deployment-sml?f=sml"));
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
