package org.opengis.cite.ogcapiconnectedsystems10.conformance.geojson;

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
 * Controlled HTTP coverage for all twelve released GeoJSON procedures.
 */
public class VerifyGeoJsonHttpProcedures {

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-DIRECT-HTTP-COVERAGE-001.
	 */
	@Test
	public void allTwelveReleasedProceduresExecuteSuccessfulReadOnlyPaths() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.VALID)) {
			server.start();
			GeoJsonTests tests = configured(server);

			tests.geoJsonMediaTypeReadIsAdvertised();
			tests.geoJsonMediaTypeWriteIsAdvertised();
			tests.geoJsonAssociationRelationTypesAreValid();
			tests.commonFeatureAttributesAreMapped();
			tests.systemGeoJsonSchemasAreValid();
			tests.systemGeoJsonMappingsAreValid();
			tests.deploymentGeoJsonSchemasAreValid();
			tests.deploymentGeoJsonMappingsAreValid();
			tests.procedureGeoJsonSchemasAreValid();
			tests.procedureGeoJsonMappingsAreValid();
			tests.samplingFeatureGeoJsonSchemasAreValid();
			tests.samplingFeatureGeoJsonMappingsAreValid();

			assertEquals(0, server.nonGetCalls());
			assertTrue(server.calls("/api/openapi.yaml") >= 2);
			assertTrue(server.calls("/api/systems/system-1") >= 1);
			assertTrue(server.calls("/api/deployments/deployment-1") >= 1);
			assertTrue(server.calls("/api/procedures/procedure-1") >= 1);
			assertTrue(server.calls("/api/samplingFeatures/samplingFeature-1") >= 1);
		}
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-MEDIA-GATE-001.
	 */
	@Test
	public void actualJsonFallbackSkipsBeforeGeoJsonSchemaParsing() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.JSON_MEDIA)) {
			server.start();
			GeoJsonTests tests = configured(server);

			SkipException error = assertThrows(SkipException.class, tests::systemGeoJsonSchemasAreValid);

			assertTrue(error.getMessage().contains("unsupported media type 'application/json'"));
			assertEquals(0, server.calls("/api/systems/system-1"));
		}
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-MEDIATYPE-READ-001.
	 */
	@Test
	public void parseableDefinitionMissingReadAdvertisementFails() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.MISSING_READ_MEDIA)) {
			server.start();
			GeoJsonTests tests = configured(server);

			assertThrows(AssertionError.class, tests::geoJsonMediaTypeReadIsAdvertised);
		}
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-FEATURE-MAPPING-001;
	 * SCENARIO-ETS-PART1-012-RELEASED-DIRECT-HTTP-COVERAGE-001.
	 */
	@Test
	public void invalidFeatureOnLaterPaginationPageCannotBeHidden() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.LATER_INVALID_MAPPING)) {
			server.start();
			GeoJsonTests tests = configured(server);

			assertThrows(AssertionError.class, tests::commonFeatureAttributesAreMapped);
			assertTrue(server.calls("/api/systems?page=2") >= 1);
		}
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-SCHEMAS-001;
	 * SCENARIO-ETS-PART1-012-RELEASED-DIRECT-HTTP-COVERAGE-001.
	 */
	@Test
	public void invalidCollectionDocumentFailsReleasedSchemaValidation() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.INVALID_SCHEMA)) {
			server.start();
			GeoJsonTests tests = configured(server);

			assertThrows(AssertionError.class, tests::systemGeoJsonSchemasAreValid);
			assertEquals(0, server.calls("/api/systems/system-1"));
		}
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-DIRECT-HTTP-COVERAGE-001.
	 */
	@Test
	public void crossOriginPaginationFailsBeforeFollowingUnsafeLink() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.CROSS_ORIGIN_PAGINATION)) {
			server.start();
			GeoJsonTests tests = configured(server);

			AssertionError error = assertThrows(AssertionError.class, tests::commonFeatureAttributesAreMapped);

			assertTrue(error.getMessage().contains("cross-origin pagination"));
		}
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-RELEASED-RELATION-TYPES-001.
	 */
	@Test
	public void noAssociationEvidenceProducesAggregateSkip() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.NO_ASSOCIATIONS)) {
			server.start();
			GeoJsonTests tests = configured(server);

			SkipException error = assertThrows(SkipException.class, tests::geoJsonAssociationRelationTypesAreValid);

			assertTrue(error.getMessage().contains("no links-member association"));
		}
	}

	/**
	 * REQ-ETS-PART1-012; SCENARIO-ETS-PART1-012-S53-HARDENING-001.
	 */
	@Test
	public void crossOriginServiceDescriptionDoesNotReceiveIutCredential() throws Exception {
		try (ExternalDefinitionServer external = new ExternalDefinitionServer();
				FixtureServer server = new FixtureServer(Mode.CROSS_ORIGIN_DEFINITION)) {
			external.start();
			server.setExternalDefinition(external.definitionUri());
			server.start();
			RequestSpecification original = RestAssured.requestSpecification;
			RestAssured.requestSpecification = new RequestSpecBuilder()
				.addHeader("Authorization", "Bearer synthetic-secret")
				.build();
			try {
				configured(server).geoJsonMediaTypeReadIsAdvertised();
			}
			finally {
				RestAssured.requestSpecification = original;
			}

			assertNull(external.authorization());
			assertEquals(0, server.nonGetCalls());
		}
	}

	private static GeoJsonTests configured(FixtureServer server) {
		GeoJsonTests tests = new GeoJsonTests();
		tests.configure(server.apiRoot());
		return tests;
	}

	private enum Mode {

		VALID, JSON_MEDIA, MISSING_READ_MEDIA, LATER_INVALID_MAPPING, INVALID_SCHEMA, CROSS_ORIGIN_PAGINATION,
		NO_ASSOCIATIONS, CROSS_ORIGIN_DEFINITION

	}

	private static final class FixtureServer implements AutoCloseable {

		private final HttpServer server;

		private final Mode mode;

		private final Map<String, AtomicInteger> calls = new ConcurrentHashMap<>();

		private final AtomicInteger nonGetCalls = new AtomicInteger();

		private URI externalDefinition;

		private FixtureServer(Mode mode) throws IOException {
			this.mode = mode;
			this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
			this.server.createContext("/api/", this::handle);
		}

		private void setExternalDefinition(URI externalDefinition) {
			this.externalDefinition = externalDefinition;
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

		private int nonGetCalls() {
			return this.nonGetCalls.get();
		}

		private void handle(HttpExchange exchange) throws IOException {
			if (!"GET".equals(exchange.getRequestMethod())) {
				this.nonGetCalls.incrementAndGet();
			}
			String path = exchange.getRequestURI().getPath();
			this.calls.computeIfAbsent(path, ignored -> new AtomicInteger()).incrementAndGet();
			if (exchange.getRequestURI().getRawQuery() != null) {
				this.calls
					.computeIfAbsent(path + "?" + exchange.getRequestURI().getRawQuery(),
							ignored -> new AtomicInteger())
					.incrementAndGet();
			}
			switch (path) {
				case "/api/" -> landing(exchange);
				case "/api/conformance" -> conformance(exchange);
				case "/api/collections" -> collections(exchange);
				case "/api/openapi.yaml" -> send(exchange, 200, "application/yaml", openApiDefinition());
				case "/api/systems" -> collection(exchange, "system");
				case "/api/deployments" -> collection(exchange, "deployment");
				case "/api/procedures" -> collection(exchange, "procedure");
				case "/api/samplingFeatures" -> collection(exchange, "samplingFeature");
				case "/api/systems/system-1" -> single(exchange, "system");
				case "/api/deployments/deployment-1" -> single(exchange, "deployment");
				case "/api/procedures/procedure-1" -> single(exchange, "procedure");
				case "/api/samplingFeatures/samplingFeature-1" -> single(exchange, "samplingFeature");
				default -> send(exchange, 404, "application/json", "{}");
			}
		}

		private void landing(HttpExchange exchange) throws IOException {
			URI definition = this.mode == Mode.CROSS_ORIGIN_DEFINITION ? this.externalDefinition
					: apiRoot().resolve("openapi.yaml");
			send(exchange, 200, "application/json",
					"{\"links\":[{\"rel\":\"service-desc\",\"type\":\"application/vnd.oai.openapi\"," + "\"href\":\""
							+ definition + "\"}]}");
		}

		private void conformance(HttpExchange exchange) throws IOException {
			send(exchange, 200, "application/json", """
					{"conformsTo":[
					  "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/geojson",
					  "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/system",
					  "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/deployment",
					  "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/procedure",
					  "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/sf"
					]}
					""");
		}

		private void collections(HttpExchange exchange) throws IOException {
			send(exchange, 200, "application/json", """
					{"collections":[{"id":"custom","itemType":"feature",
					  "links":[{"rel":"items","type":"application/geo+json","href":"%s"}]}]}
					""".formatted(apiRoot().resolve("collections/custom/items")));
		}

		private void collection(HttpExchange exchange, String type) throws IOException {
			String contentType = this.mode == Mode.JSON_MEDIA ? "application/json" : "application/geo+json";
			if (this.mode == Mode.INVALID_SCHEMA && "system".equals(type)) {
				send(exchange, 200, contentType,
						"{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\"}]}");
				return;
			}
			if (this.mode == Mode.CROSS_ORIGIN_PAGINATION && "system".equals(type)) {
				send(exchange, 200, contentType, "{\"type\":\"FeatureCollection\",\"features\":[" + feature(type)
						+ "],\"links\":[{\"rel\":\"next\",\"href\":\"https://other.test/api/systems\"}]}");
				return;
			}
			if (this.mode == Mode.LATER_INVALID_MAPPING && "system".equals(type)) {
				if ("page=2".equals(exchange.getRequestURI().getRawQuery())) {
					send(exchange, 200, contentType,
							"{\"type\":\"FeatureCollection\",\"features\":[" + invalidSystemFeature() + "]}");
				}
				else {
					send(exchange, 200, contentType,
							"{\"type\":\"FeatureCollection\",\"features\":[" + feature(type)
									+ "],\"links\":[{\"rel\":\"next\",\"href\":\"" + apiRoot().resolve("systems?page=2")
									+ "\"}]}");
				}
				return;
			}
			send(exchange, 200, contentType, "{\"type\":\"FeatureCollection\",\"features\":[" + feature(type) + "]}");
		}

		private void single(HttpExchange exchange, String type) throws IOException {
			String contentType = this.mode == Mode.JSON_MEDIA ? "application/json" : "application/geo+json";
			send(exchange, 200, contentType, feature(type));
		}

		private String feature(String type) {
			boolean associations = this.mode != Mode.NO_ASSOCIATIONS;
			return switch (type) {
				case "system" -> """
						{"type":"Feature","id":"system-1",
						 "geometry":{"type":"Point","coordinates":[1,2]},
						 "properties":{"uid":"urn:example:system:1","name":"System",
						   "featureType":"sosa:System","assetType":"Equipment",
						   "validTime":["2026-01-01T00:00:00Z","2027-01-01T00:00:00Z"]},
						 "links":[%s]}
						""".formatted(link(associations ? "subsystems" : "canonical", "systems"));
				case "deployment" -> """
						{"type":"Feature","id":"deployment-1",
						 "geometry":{"type":"Point","coordinates":[1,2]},
						 "properties":{"uid":"urn:example:deployment:1","name":"Deployment",
						   "featureType":"sosa:Deployment",
						   "validTime":["2026-01-01T00:00:00Z","2027-01-01T00:00:00Z"]},
						 "links":[%s]}
						""".formatted(link(associations ? "subdeployments" : "canonical", "deployments"));
				case "procedure" -> """
						{"type":"Feature","id":"procedure-1","geometry":null,
						 "properties":{"uid":"urn:example:procedure:1","name":"Procedure",
						   "featureType":"sosa:Procedure"},
						 "links":[%s]}
						""".formatted(link(associations ? "implementingSystems" : "canonical", "systems"));
				case "samplingFeature" -> """
						{"type":"Feature","id":"samplingFeature-1",
						 "geometry":{"type":"Point","coordinates":[1,2]},
						 "properties":{"uid":"urn:example:sampling-feature:1","name":"Sampling Feature",
						   "featureType":"http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint",
						   "validTime":["2026-01-01T00:00:00Z","2027-01-01T00:00:00Z"],
						   "sampledFeature@link":{"href":"%s"}},
						 "links":[%s]}
						""".formatted(apiRoot().resolve("features/1"),
						link(associations ? "sampleOf" : "canonical", "samplingFeatures"));
				default -> throw new IllegalArgumentException(type);
			};
		}

		private String invalidSystemFeature() {
			return feature("system").replace("urn:example:system:1", "not a uri");
		}

		private String link(String rel, String target) {
			return "{\"rel\":\"" + rel + "\",\"href\":\"" + apiRoot().resolve(target) + "\"}";
		}

		private String openApiDefinition() {
			String media = this.mode == Mode.MISSING_READ_MEDIA ? "application/json" : "application/geo+json";
			return openApiDefinition(media);
		}

		private static String openApiDefinition(String media) {
			return """
					openapi: 3.0.3
					info:
					  title: GeoJSON fixture
					  version: "1"
					paths:
					  /systems:
					    get:
					      responses:
					        "200":
					          description: ok
					          content:
					            %s: {}
					    post:
					      requestBody:
					        content:
					          application/geo+json:
					            schema:
					              type: object
					      responses:
					        "201":
					          description: created
					  /deployments:
					    get:
					      responses:
					        "200":
					          description: ok
					          content:
					            application/geo+json: {}
					  /procedures:
					    get:
					      responses:
					        "200":
					          description: ok
					          content:
					            application/geo+json: {}
					  /samplingFeatures:
					    get:
					      responses:
					        "200":
					          description: ok
					          content:
					            application/geo+json: {}
					  /collections/{collectionId}/items:
					    get:
					      responses:
					        "200":
					          description: ok
					          content:
					            application/geo+json: {}
					""".formatted(media);
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

	private static final class ExternalDefinitionServer implements AutoCloseable {

		private final HttpServer server;

		private final AtomicReference<String> authorization = new AtomicReference<>();

		private ExternalDefinitionServer() throws IOException {
			this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
			this.server.createContext("/openapi.yaml", this::handle);
		}

		private void start() {
			this.server.start();
		}

		private URI definitionUri() {
			return URI.create("http://127.0.0.1:" + this.server.getAddress().getPort() + "/openapi.yaml");
		}

		private String authorization() {
			return this.authorization.get();
		}

		private void handle(HttpExchange exchange) throws IOException {
			this.authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
			FixtureServer.send(exchange, 200, "application/yaml",
					FixtureServer.openApiDefinition("application/geo+json"));
		}

		@Override
		public void close() {
			this.server.stop(0);
		}

	}

}
