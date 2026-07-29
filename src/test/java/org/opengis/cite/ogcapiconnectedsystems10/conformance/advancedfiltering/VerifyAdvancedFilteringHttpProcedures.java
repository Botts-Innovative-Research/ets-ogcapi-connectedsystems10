package org.opengis.cite.ogcapiconnectedsystems10.conformance.advancedfiltering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.Test;
import org.testng.SkipException;

/**
 * Controlled HTTP coverage for all 25 released Advanced Filtering procedures.
 */
public class VerifyAdvancedFilteringHttpProcedures {

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-DIRECT-HTTP-COVERAGE-001.
	 */
	@Test
	public void allTwentyFiveReleasedProceduresExecuteSuccessfulReadOnlyPaths() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.VALID)) {
			server.start();
			AdvancedFilteringTests tests = configured(server);

			tests.idListSchemaIsValid();
			tests.canonicalResourcesFilterById();
			tests.canonicalResourcesFilterByKeyword();
			tests.canonicalResourcesFilterByProperty();
			tests.featuresFilterByGeometry();
			tests.systemsFilterByParent();
			tests.systemsFilterByProcedure();
			tests.systemsFilterByFeatureOfInterest();
			tests.systemsFilterByObservedProperty();
			tests.systemsFilterByControlledProperty();
			tests.deploymentsFilterByParent();
			tests.deploymentsFilterBySystem();
			tests.deploymentsFilterByFeatureOfInterest();
			tests.deploymentsFilterByObservedProperty();
			tests.deploymentsFilterByControlledProperty();
			tests.proceduresFilterByObservedProperty();
			tests.proceduresFilterByControlledProperty();
			tests.samplingFeaturesFilterByFeatureOfInterest();
			tests.samplingFeaturesFilterByObservedProperty();
			tests.samplingFeaturesFilterByControlledProperty();
			tests.propertiesFilterByBaseProperty();
			tests.propertiesFilterByObjectType();
			tests.canonicalResourcesCombineFilters();
			tests.indirectPropertyFiltersAreTransitive();
			tests.indirectFeatureOfInterestFiltersAreTransitive();

			assertEquals(0, server.nonGetCalls());
			assertTrue(server.calls("/api/conformance") >= 25);
			assertTrue(server.calls("/api/systems") > 0);
			assertTrue(server.calls("/api/deployments") > 0);
			assertTrue(server.calls("/api/procedures") > 0);
			assertTrue(server.calls("/api/samplingFeatures") > 0);
			assertTrue(server.calls("/api/properties") > 0);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-DECLARATION-GATE-001.
	 */
	@Test
	public void undeclaredClassSkipsBeforeCanonicalResourceAccess() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNDECLARED)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::canonicalResourcesFilterById);
			assertEquals(0, server.calls("/api/systems"));
			assertEquals(0, server.nonGetCalls());
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-KNOWN-MATCH-001.
	 */
	@Test
	public void emptyKnownMatchIdResultFailsClosed() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.EMPTY_ID_RESULT)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::canonicalResourcesFilterById);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-PAGINATION-001.
	 */
	@Test
	public void nonmatchingResourceOnLaterPageCannotBeHidden() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.LATER_WRONG_ID)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::canonicalResourcesFilterById);
			assertTrue(server.calls("/api/systems?page=2") > 0);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-COMBINED-FILTERS-001.
	 */
	@Test
	public void unionBehaviorCannotPassTheCombinedFilterProcedure() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNION_COMBINED)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::canonicalResourcesCombineFilters);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-GEOMETRY-001.
	 */
	@Test
	public void nonintersectingGeometryCannotPass() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.WRONG_GEOMETRY)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::featuresFilterByGeometry);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-CREDENTIAL-BOUNDARY-001.
	 */
	@Test
	public void crossOriginAssociationDoesNotReceiveIutCredential() throws Exception {
		try (ExternalServer external = new ExternalServer();
				FixtureServer server = new FixtureServer(Mode.CROSS_ORIGIN_ASSOCIATION)) {
			external.start();
			server.setExternalTarget(external.target());
			server.start();
			RequestSpecification original = RestAssured.requestSpecification;
			RestAssured.requestSpecification = new RequestSpecBuilder()
				.addHeader("Authorization", "Bearer synthetic-secret")
				.build();
			try {
				configured(server).systemsFilterByParent();
			}
			finally {
				RestAssured.requestSpecification = original;
			}

			assertNull(external.authorization());
			assertEquals(0, external.calls());
			assertEquals(0, server.nonGetCalls());
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-PROCEDURE-ISOLATION-001.
	 */
	@Test
	public void missingAssociationEvidenceDoesNotBlockIndependentIdProcedure() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.NO_ASSOCIATIONS)) {
			server.start();
			AdvancedFilteringTests tests = configured(server);

			assertThrows(SkipException.class, tests::systemsFilterByParent);
			tests.canonicalResourcesFilterById();
		}
	}

	private static AdvancedFilteringTests configured(FixtureServer server) {
		AdvancedFilteringTests tests = new AdvancedFilteringTests();
		tests.configure(server.apiRoot());
		return tests;
	}

	private enum Mode {

		VALID, UNDECLARED, EMPTY_ID_RESULT, LATER_WRONG_ID, UNION_COMBINED, WRONG_GEOMETRY, CROSS_ORIGIN_ASSOCIATION,
		NO_ASSOCIATIONS

	}

	private static final class FixtureServer implements AutoCloseable {

		private final HttpServer server;

		private final Mode mode;

		private final Map<String, AtomicInteger> calls = new ConcurrentHashMap<>();

		private final AtomicInteger nonGetCalls = new AtomicInteger();

		private URI externalTarget;

		private FixtureServer(Mode mode) throws IOException {
			this.mode = mode;
			this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
			this.server.createContext("/api/", this::handle);
		}

		private void start() {
			this.server.start();
		}

		private void setExternalTarget(URI externalTarget) {
			this.externalTarget = externalTarget;
		}

		private URI apiRoot() {
			return URI.create("http://127.0.0.1:" + this.server.getAddress().getPort() + "/api/");
		}

		private int calls(String requestTarget) {
			AtomicInteger count = this.calls.get(requestTarget);
			return count == null ? 0 : count.get();
		}

		private int nonGetCalls() {
			return this.nonGetCalls.get();
		}

		private void handle(HttpExchange exchange) throws IOException {
			if (!"GET".equals(exchange.getRequestMethod())) {
				this.nonGetCalls.incrementAndGet();
			}
			String path = exchange.getRequestURI().getPath();
			this.calls.computeIfAbsent(path, ignored -> new AtomicInteger()).incrementAndGet();
			String query = exchange.getRequestURI().getRawQuery();
			if (query != null) {
				this.calls.computeIfAbsent(path + "?" + query, ignored -> new AtomicInteger()).incrementAndGet();
			}
			switch (path) {
				case "/api/conformance" -> conformance(exchange);
				case "/api/systems" -> canonical(exchange, "systems", query);
				case "/api/deployments" -> canonical(exchange, "deployments", query);
				case "/api/procedures" -> canonical(exchange, "procedures", query);
				case "/api/samplingFeatures" -> canonical(exchange, "samplingFeatures", query);
				case "/api/properties" -> canonical(exchange, "properties", query);
				case "/api/systems/system-1/subsystems" -> canonical(exchange, "systems", query);
				case "/api/systems/system-1/samplingFeatures" -> canonical(exchange, "samplingFeatures", query);
				case "/api/deployments/deployment-1/deployedSystems" -> canonical(exchange, "systems", query);
				case "/api/deployments/deployment-1/featuresOfInterest" ->
					canonical(exchange, "samplingFeatures", query);
				case "/api/samplingFeatures/sf-1/datastreams" -> stream(exchange, true);
				case "/api/samplingFeatures/sf-1/controlstreams" -> stream(exchange, false);
				case "/api/systems/parent-1" -> single(exchange, "systems", parentSystem());
				case "/api/procedures/procedure-1" -> single(exchange, "procedures", procedure());
				case "/api/features/foi-1" -> single(exchange, "samplingFeatures", samplingFeature());
				default -> send(exchange, 404, "application/json", "{}");
			}
		}

		private void conformance(HttpExchange exchange) throws IOException {
			String declaration = this.mode == Mode.UNDECLARED ? ""
					: ",\"" + AdvancedFilteringSupport.CONF_ADVANCED_FILTERING + "\"";
			send(exchange, 200, "application/json",
					"{\"conformsTo\":[\"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/api-common\""
							+ declaration + "]}");
		}

		private void canonical(HttpExchange exchange, String type, String query) throws IOException {
			if (this.mode == Mode.EMPTY_ID_RESULT && "systems".equals(type) && query != null
					&& query.startsWith("id=")) {
				sendCollection(exchange, type, "[]", null);
				return;
			}
			if (this.mode == Mode.LATER_WRONG_ID && "systems".equals(type) && query != null
					&& query.startsWith("id=")) {
				sendCollection(exchange, type, "[" + system() + "]", apiRoot().resolve("systems?page=2"));
				return;
			}
			if (this.mode == Mode.LATER_WRONG_ID && "systems".equals(type) && "page=2".equals(query)) {
				sendCollection(exchange, type, "[" + otherSystem() + "]", null);
				return;
			}
			if (this.mode == Mode.UNION_COMBINED && "systems".equals(type) && query != null && query.contains("id=")
					&& query.contains("q=")) {
				sendCollection(exchange, type, "[" + system() + "," + otherSystem() + "]", null);
				return;
			}
			if (this.mode == Mode.WRONG_GEOMETRY && "systems".equals(type) && query != null
					&& query.startsWith("geom=")) {
				sendCollection(exchange, type, "[" + otherSystem() + "]", null);
				return;
			}
			String item = switch (type) {
				case "systems" -> system();
				case "deployments" -> deployment();
				case "procedures" -> procedure();
				case "samplingFeatures" -> samplingFeature();
				case "properties" -> property();
				default -> throw new IllegalArgumentException(type);
			};
			sendCollection(exchange, type, "[" + item + "]", null);
		}

		private void sendCollection(HttpExchange exchange, String type, String items, URI next) throws IOException {
			if ("properties".equals(type)) {
				send(exchange, 200, "application/sml+json", "{\"items\":" + items + links(next) + "}");
			}
			else {
				send(exchange, 200, "application/geo+json",
						"{\"type\":\"FeatureCollection\",\"features\":" + items + links(next) + "}");
			}
		}

		private String links(URI next) {
			return next == null ? ""
					: ",\"links\":[{\"rel\":\"next\",\"type\":\"application/geo+json\",\"href\":\"" + next + "\"}]";
		}

		private void single(HttpExchange exchange, String type, String body) throws IOException {
			send(exchange, 200, "properties".equals(type) ? "application/sml+json" : "application/geo+json", body);
		}

		private void stream(HttpExchange exchange, boolean observed) throws IOException {
			String key = observed ? "observedProperties" : "controlledProperties";
			String value = observed ? "urn:example:property:observed" : "urn:example:property:controlled";
			send(exchange, 200, "application/json", "{\"items\":[{\"id\":\"stream-1\",\"" + key
					+ "\":[{\"id\":\"property-1\",\"uid\":\"" + value + "\"}]}]}");
		}

		private String system() {
			String associations = this.mode == Mode.NO_ASSOCIATIONS ? "" : systemAssociations();
			return """
					{"type":"Feature","id":"system-1","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:System","uid":"urn:example:system:1",
					 "name":"Weather Station","customCode":"alpha"%s},
					 "links":[{"rel":"canonical","type":"application/geo+json","href":"%s"}]}
					""".formatted(associations, apiRoot().resolve("systems/system-1"));
		}

		private String systemAssociations() {
			URI parent = this.mode == Mode.CROSS_ORIGIN_ASSOCIATION ? this.externalTarget
					: apiRoot().resolve("systems/parent-1");
			return """
					,"parentSystem":{"id":"parent-1","uid":"urn:example:system:parent","href":"%s"},
					 "procedure":{"id":"procedure-1","uid":"urn:example:procedure:1","href":"%s"},
					 "sampleOf":{"id":"foi-1","uid":"urn:example:foi:1","href":"%s"},
					 "observedProperties":[{"id":"property-1","uid":"urn:example:property:observed"}],
					 "controlledProperties":[{"id":"property-1","uid":"urn:example:property:controlled"}]
					""".formatted(parent, apiRoot().resolve("procedures/procedure-1"),
					apiRoot().resolve("features/foi-1"));
		}

		private String parentSystem() {
			return """
					{"type":"Feature","id":"parent-1","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:System","uid":"urn:example:system:parent","name":"Parent System"}}
					""";
		}

		private String otherSystem() {
			return """
					{"type":"Feature","id":"system-2","geometry":{"type":"Point","coordinates":[0,0]},
					 "properties":{"featureType":"sosa:System","uid":"urn:example:system:2","name":"Other Sensor"}}
					""";
		}

		private String deployment() {
			return """
					{"type":"Feature","id":"deployment-1","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:Deployment","uid":"urn:example:deployment:1",
					 "name":"Field Deployment","validTime":["2026-01-01T00:00:00Z","2027-01-01T00:00:00Z"],
					 "customCode":"alpha",
					 "parentDeployment":{"id":"deployment-parent","uid":"urn:example:deployment:parent"},
					 "deployedSystems":[{"id":"system-1","uid":"urn:example:system:1"}],
					 "featuresOfInterest":[{"id":"foi-1","uid":"urn:example:foi:1"}],
					 "observedProperties":[{"id":"property-1","uid":"urn:example:property:observed"}],
					 "controlledProperties":[{"id":"property-1","uid":"urn:example:property:controlled"}]}}
					""";
		}

		private String procedure() {
			return """
					{"type":"Feature","id":"procedure-1","geometry":null,
					 "properties":{"featureType":"sosa:ObservingProcedure","uid":"urn:example:procedure:1",
					 "name":"Weather Procedure","customCode":"alpha",
					 "observedProperties":[{"id":"property-1","uid":"urn:example:property:observed"}],
					 "controlledProperties":[{"id":"property-1","uid":"urn:example:property:controlled"}]}}
					""";
		}

		private String samplingFeature() {
			return """
					{"type":"Feature","id":"sf-1","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:Sample","uid":"urn:example:sf:1","name":"Weather Sample",
					 "customCode":"alpha",
					 "sampledFeature@link":{"href":"%s","id":"foi-1","uid":"urn:example:foi:1"},
					 "sampleOf":{"href":"%s","id":"parent-sf","uid":"urn:example:sf:parent"},
					 "observedProperties":[{"id":"property-1","uid":"urn:example:property:observed"}],
					 "controlledProperties":[{"id":"property-1","uid":"urn:example:property:controlled"}]}}
					""".formatted(apiRoot().resolve("features/foi-1"), apiRoot().resolve("features/foi-1"));
		}

		private String property() {
			return """
					{"id":"property-1","uniqueId":"urn:example:property:1","label":"Weather Property",
					 "baseProperty":"%s","objectType":"urn:example:type:System",
					 "customCode":"alpha",
					 "links":[{"rel":"canonical","type":"application/sml+json","href":"%s"}]}
					""".formatted(apiRoot().resolve("properties/base-1"), apiRoot().resolve("properties/property-1"));
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

	private static final class ExternalServer implements AutoCloseable {

		private final HttpServer server;

		private final AtomicReference<String> authorization = new AtomicReference<>();

		private final AtomicInteger calls = new AtomicInteger();

		private ExternalServer() throws IOException {
			this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
			this.server.createContext("/target", this::handle);
		}

		private void start() {
			this.server.start();
		}

		private URI target() {
			return URI.create("http://127.0.0.1:" + this.server.getAddress().getPort() + "/target");
		}

		private String authorization() {
			return this.authorization.get();
		}

		private int calls() {
			return this.calls.get();
		}

		private void handle(HttpExchange exchange) throws IOException {
			this.calls.incrementAndGet();
			this.authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
			FixtureServer.send(exchange, 200, "application/json", "{\"id\":\"external\"}");
		}

		@Override
		public void close() {
			this.server.stop(0);
		}

	}

}
