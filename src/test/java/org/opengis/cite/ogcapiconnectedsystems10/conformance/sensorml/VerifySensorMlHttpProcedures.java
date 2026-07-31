package org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml;

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

import org.junit.Test;
import org.testng.SkipException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

/**
 * Controlled HTTP coverage for all fifteen released SensorML procedures.
 */
public class VerifySensorMlHttpProcedures {

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-DIRECT-HTTP-COVERAGE-001.
	 */
	@Test
	public void allFifteenReleasedProceduresExecuteSuccessfulReadOnlyPaths() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.VALID)) {
			server.start();
			SensorMlTests tests = configured(server);

			tests.sensorMlMediaTypeReadIsAdvertised();
			tests.sensorMlMediaTypeWriteIsAdvertised();
			tests.sensorMlAssociationRelationTypesAreValid();
			tests.sensorMlResourceIdsMatchCanonicalUrls();
			tests.sensorMlCommonFeatureAttributesAreMapped();
			tests.systemSensorMlSchemasAreValid();
			tests.systemSensorMlClassesAreCompatible();
			tests.systemSensorMlMappingsAreValid();
			tests.deploymentSensorMlSchemasAreValid();
			tests.deploymentSensorMlMappingsAreValid();
			tests.procedureSensorMlSchemasAreValid();
			tests.procedureSensorMlClassesAreCompatible();
			tests.procedureSensorMlMappingsAreValid();
			tests.propertySensorMlSchemasAreValid();
			tests.propertySensorMlMappingsAreValid();

			assertEquals(0, server.nonGetCalls());
			assertTrue(server.calls("/api/openapi.json") >= 2);
			assertTrue(server.calls("/api/systems/system-1") >= 2);
			assertTrue(server.calls("/api/deployments/deployment-1") >= 2);
			assertTrue(server.calls("/api/procedures/procedure-1") >= 2);
			assertTrue(server.calls("/api/properties/property-1") >= 2);
		}
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-MEDIA-GATE-001.
	 */
	@Test
	public void genericJsonFailsBeforeSensorMlSchemaParsing() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.JSON_MEDIA)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::systemSensorMlSchemasAreValid);
			assertEquals(0, server.calls("/api/systems/system-1"));
		}
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-COMMON-MAPPINGS-001.
	 */
	@Test
	public void invalidLaterPageResourceCannotBeHidden() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.LATER_INVALID_MAPPING)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::sensorMlCommonFeatureAttributesAreMapped);
			assertTrue(server.calls("/api/systems?page=2") >= 1);
		}
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-RESOURCE-ID-001.
	 */
	@Test
	public void canonicalIdMismatchFails() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.WRONG_ID)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::sensorMlResourceIdsMatchCanonicalUrls);
		}
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-RELATION-TYPES-001.
	 */
	@Test
	public void unprefixedAssociationRelationFails() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.WRONG_RELATION)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::sensorMlAssociationRelationTypesAreValid);
		}
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-SCHEMAS-001.
	 */
	@Test
	public void invalidCollectionDocumentFailsReleasedSchema() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.INVALID_SCHEMA)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::systemSensorMlSchemasAreValid);
			assertEquals(0, server.calls("/api/systems/system-1"));
		}
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-MEDIA-ADVERTISEMENT-001.
	 */
	@Test
	public void malformedAdvertisedServiceDescriptionFails() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.MALFORMED_DEFINITION)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::sensorMlMediaTypeReadIsAdvertised);
		}
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-RESOURCE-MAPPINGS-001.
	 */
	@Test
	public void wrongAssociationTargetAndMalformedPoseFailMappings() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.BAD_ASSOCIATION_TARGET)) {
			server.start();
			assertThrows(AssertionError.class, configured(server)::systemSensorMlMappingsAreValid);
		}
		try (FixtureServer server = new FixtureServer(Mode.MALFORMED_POSITION)) {
			server.start();
			assertThrows(AssertionError.class, configured(server)::systemSensorMlMappingsAreValid);
		}
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-RESOURCE-MAPPINGS-001.
	 */
	@Test
	public void distributedAssociationUsesTypedSchemaWithoutForwardingCredential() throws Exception {
		RequestSpecification original = RestAssured.requestSpecification;
		try (FixtureServer distributed = new FixtureServer(Mode.VALID)) {
			distributed.start();
			try (FixtureServer primary = new FixtureServer(Mode.VALID, distributed.apiRoot().resolve("distributed"))) {
				primary.start();
				RestAssured.requestSpecification = new RequestSpecBuilder()
					.addHeader("Authorization", "Bearer sensor-ml-secret")
					.build();

				configured(primary).systemSensorMlMappingsAreValid();

				assertEquals(null, distributed.authorization("/api/distributed"));
				assertTrue(distributed.calls("/api/distributed") >= 1);
			}
		}
		finally {
			RestAssured.requestSpecification = original;
		}
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-RESOURCE-MAPPINGS-001.
	 */
	@Test
	public void wrongAssociationResourceAndCollectionSchemasFail() throws Exception {
		try (FixtureServer distributed = new FixtureServer(Mode.WRONG_ASSOCIATION_COLLECTION)) {
			distributed.start();
			try (FixtureServer primary = new FixtureServer(Mode.VALID, distributed.apiRoot().resolve("distributed"))) {
				primary.start();
				assertThrows(AssertionError.class, configured(primary)::systemSensorMlMappingsAreValid);
			}
		}
		try (FixtureServer distributed = new FixtureServer(Mode.WRONG_ASSOCIATION_RESOURCE)) {
			distributed.start();
			try (FixtureServer primary = new FixtureServer(Mode.VALID,
					distributed.apiRoot().resolve("distributed-resource"))) {
				primary.start();
				assertThrows(AssertionError.class, configured(primary)::deploymentSensorMlMappingsAreValid);
			}
		}
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-DIRECT-HTTP-COVERAGE-001.
	 */
	@Test
	public void unsafeCycleAndLaterUnsupportedPaginationFail() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.CROSS_ORIGIN_NEXT)) {
			server.start();
			assertThrows(AssertionError.class, configured(server)::sensorMlCommonFeatureAttributesAreMapped);
		}
		try (FixtureServer server = new FixtureServer(Mode.PAGINATION_CYCLE)) {
			server.start();
			assertThrows(AssertionError.class, configured(server)::sensorMlCommonFeatureAttributesAreMapped);
		}
		try (FixtureServer server = new FixtureServer(Mode.LATER_UNSUPPORTED_MEDIA)) {
			server.start();
			assertThrows(AssertionError.class, configured(server)::sensorMlCommonFeatureAttributesAreMapped);
		}
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-RELATION-TYPES-001.
	 */
	@Test
	public void absentAssociationEvidenceSkipsOnlyAfterInspection() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.NO_ASSOCIATIONS)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::sensorMlAssociationRelationTypesAreValid);
			assertTrue(server.calls("/api/systems") >= 1);
			assertTrue(server.calls("/api/deployments") >= 1);
			assertTrue(server.calls("/api/procedures") >= 1);
			assertTrue(server.calls("/api/properties") >= 1);
		}
	}

	private static SensorMlTests configured(FixtureServer server) {
		SensorMlTests tests = new SensorMlTests();
		tests.configure(server.apiRoot());
		return tests;
	}

	private enum Mode {

		VALID, JSON_MEDIA, LATER_INVALID_MAPPING, WRONG_ID, WRONG_RELATION, INVALID_SCHEMA, MALFORMED_DEFINITION,
		BAD_ASSOCIATION_TARGET, MALFORMED_POSITION, CROSS_ORIGIN_NEXT, PAGINATION_CYCLE, LATER_UNSUPPORTED_MEDIA,
		NO_ASSOCIATIONS, WRONG_ASSOCIATION_RESOURCE, WRONG_ASSOCIATION_COLLECTION

	}

	private static final class FixtureServer implements AutoCloseable {

		private final HttpServer server;

		private final Mode mode;

		private final URI associationTarget;

		private final Map<String, AtomicInteger> calls = new ConcurrentHashMap<>();

		private final Map<String, String> authorizations = new ConcurrentHashMap<>();

		private final AtomicInteger nonGetCalls = new AtomicInteger();

		private FixtureServer(Mode mode) throws IOException {
			this(mode, null);
		}

		private FixtureServer(Mode mode, URI associationTarget) throws IOException {
			this.mode = mode;
			this.associationTarget = associationTarget;
			this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
			this.server.createContext("/api/", this::handle);
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

		private String authorization(String path) {
			return this.authorizations.get(path);
		}

		private void handle(HttpExchange exchange) throws IOException {
			if (!"GET".equals(exchange.getRequestMethod())) {
				this.nonGetCalls.incrementAndGet();
			}
			String path = exchange.getRequestURI().getPath();
			this.calls.computeIfAbsent(path, ignored -> new AtomicInteger()).incrementAndGet();
			String authorization = exchange.getRequestHeaders().getFirst("Authorization");
			if (authorization != null) {
				this.authorizations.put(path, authorization);
			}
			if (exchange.getRequestURI().getRawQuery() != null) {
				this.calls
					.computeIfAbsent(path + "?" + exchange.getRequestURI().getRawQuery(),
							ignored -> new AtomicInteger())
					.incrementAndGet();
			}
			switch (path) {
				case "/api/" -> landing(exchange);
				case "/api/conformance" -> conformance(exchange);
				case "/api/collections" -> send(exchange, 200, "application/json", "{\"collections\":[]}");
				case "/api/openapi.json" -> send(exchange, 200, "application/json",
						this.mode == Mode.MALFORMED_DEFINITION ? "{not-json" : openApiDefinition());
				case "/api/systems" -> collection(exchange, "system");
				case "/api/deployments" -> collection(exchange, "deployment");
				case "/api/procedures" -> collection(exchange, "procedure");
				case "/api/properties" -> collection(exchange, "property");
				case "/api/systems/system-1" -> single(exchange, "system");
				case "/api/deployments/deployment-1" -> single(exchange, "deployment");
				case "/api/procedures/procedure-1" -> single(exchange, "procedure");
				case "/api/properties/property-1" -> single(exchange, "property");
				case "/api/distributed" -> associationCollection(exchange);
				case "/api/distributed-resource" -> associationResource(exchange);
				default -> send(exchange, 404, "application/json", "{}");
			}
		}

		private void associationCollection(HttpExchange exchange) throws IOException {
			String type = this.mode == Mode.WRONG_ASSOCIATION_COLLECTION ? "procedure" : "system";
			send(exchange, 200, "application/sml+json", "{\"items\":[" + resource(type) + "]}");
		}

		private void associationResource(HttpExchange exchange) throws IOException {
			String type = this.mode == Mode.WRONG_ASSOCIATION_RESOURCE ? "procedure" : "system";
			send(exchange, 200, "application/sml+json", resource(type));
		}

		private void landing(HttpExchange exchange) throws IOException {
			send(exchange, 200, "application/json",
					"{\"links\":[{\"rel\":\"service-desc\",\"type\":\"application/vnd.oai.openapi\"," + "\"href\":\""
							+ apiRoot().resolve("openapi.json") + "\"}]}");
		}

		private void conformance(HttpExchange exchange) throws IOException {
			send(exchange, 200, "application/json", """
					{"conformsTo":[
					  "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/sensorml",
					  "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/system",
					  "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/deployment",
					  "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/procedure",
					  "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/property",
					  "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/create-replace-delete"
					]}
					""");
		}

		private void collection(HttpExchange exchange, String type) throws IOException {
			String media = this.mode == Mode.JSON_MEDIA ? "application/json" : "application/sml+json";
			if (this.mode == Mode.INVALID_SCHEMA && "system".equals(type)) {
				send(exchange, 200, media, "{\"wrong\":[]}");
				return;
			}
			if (this.mode == Mode.LATER_INVALID_MAPPING && "system".equals(type)) {
				if ("page=2".equals(exchange.getRequestURI().getRawQuery())) {
					send(exchange, 200, media,
							"{\"items\":[" + resource(type).replace("urn:example:system:1", "not a uri") + "]}");
				}
				else {
					send(exchange, 200, media,
							"{\"items\":[" + resource(type) + "],\"links\":[{\"rel\":\"next\",\"href\":\""
									+ apiRoot().resolve("systems?page=2") + "\"}]}");
				}
				return;
			}
			if ("system".equals(type) && (this.mode == Mode.CROSS_ORIGIN_NEXT || this.mode == Mode.PAGINATION_CYCLE
					|| this.mode == Mode.LATER_UNSUPPORTED_MEDIA)) {
				if ("page=2".equals(exchange.getRequestURI().getRawQuery())) {
					send(exchange, 200,
							this.mode == Mode.LATER_UNSUPPORTED_MEDIA ? "application/json" : "application/sml+json",
							"{\"items\":[" + resource(type) + "]}");
				}
				else {
					String href = this.mode == Mode.CROSS_ORIGIN_NEXT ? "https://other.test/api/systems?page=2"
							: this.mode == Mode.PAGINATION_CYCLE ? apiRoot().resolve("systems").toString()
									: apiRoot().resolve("systems?page=2").toString();
					send(exchange, 200, media, "{\"items\":[" + resource(type)
							+ "],\"links\":[{\"rel\":\"next\",\"href\":\"" + href + "\"}]}");
				}
				return;
			}
			send(exchange, 200, media, "{\"items\":[" + resource(type) + "]}");
		}

		private void single(HttpExchange exchange, String type) throws IOException {
			String media = this.mode == Mode.JSON_MEDIA ? "application/json" : "application/sml+json";
			String body = resource(type);
			if (this.mode == Mode.WRONG_ID && "system".equals(type)) {
				body = body.replace("\"id\":\"system-1\"", "\"id\":\"different\"");
			}
			send(exchange, 200, media, body);
		}

		private String resource(String type) {
			return switch (type) {
				case "system" -> systemResource();
				case "deployment" -> """
						{"type":"Deployment","id":"deployment-1","uniqueId":"urn:example:deployment:1",
						 "label":"Deployment","definition":"sosa:Deployment",
						 "deployedSystems":[{"name":"sensor","system":{"href":"%s"}}]}
						""".formatted(this.associationTarget == null ? apiRoot().resolve("systems/system-1")
						: this.associationTarget);
				case "procedure" -> """
						{"type":"SimpleProcess","id":"procedure-1","uniqueId":"urn:example:procedure:1",
						 "label":"Procedure","definition":"sosa:ObservingProcedure"}
						""";
				case "property" -> """
						{"id":"property-1","uniqueId":"urn:example:property:1","label":"Property",
						 "baseProperty":"https://qudt.org/vocab/quantitykind/Temperature"}
						""";
				default -> throw new IllegalArgumentException(type);
			};
		}

		private String systemResource() {
			String position = this.mode == Mode.MALFORMED_POSITION ? ",\"position\":{\"type\":\"GeoPose\"}" : "";
			String links = this.mode == Mode.NO_ASSOCIATIONS ? "" : """
					,"links":[{"rel":"%s","href":"%s"}]
					""".formatted(this.mode == Mode.WRONG_RELATION ? "subsystems" : "ogc-rel:subsystems",
					this.mode == Mode.BAD_ASSOCIATION_TARGET ? apiRoot().resolve("properties")
							: this.associationTarget == null ? apiRoot().resolve("systems") : this.associationTarget);
			return """
					{"type":"PhysicalSystem","id":"system-1","uniqueId":"urn:example:system:1",
					 "label":"System","definition":"sosa:System",
					 "classifiers":[{"definition":"cs:AssetType","label":"Asset Type","value":"Equipment"}]%s%s}
					""".formatted(position, links);
		}

		private static String openApiDefinition() {
			return """
					{
					  "openapi":"3.0.3",
					  "info":{"title":"SensorML fixture","version":"1"},
					  "paths":{
					    "/systems":{
					      "get":{"responses":{"200":{"description":"ok","content":{"application/sml+json":{}}}}},
					      "post":{"requestBody":{"content":{"application/sml+json":{"schema":{"type":"object"}}}},
					              "responses":{"201":{"description":"created"}}}
					    },
					    "/deployments":{"get":{"responses":{"200":{"description":"ok","content":{"application/sml+json":{}}}}}},
					    "/procedures":{"get":{"responses":{"200":{"description":"ok","content":{"application/sml+json":{}}}}}},
					    "/properties":{"get":{"responses":{"200":{"description":"ok","content":{"application/sml+json":{}}}}}}
					  }
					}
					""";
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
