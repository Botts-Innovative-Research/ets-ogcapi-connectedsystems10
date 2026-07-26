package org.opengis.cite.ogcapiconnectedsystems10.conformance.subsystems;

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
 * Controlled HTTP checks for direct released Subsystem procedures.
 */
public class VerifySubsystemsHttpProcedures {

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-DIRECT-HTTP-COVERAGE-001.
	 */
	@Test
	public void allFiveReleasedProceduresExecuteSuccessfulHttpPaths() throws Exception {
		try (FixtureServer server = new FixtureServer()) {
			server.start();
			SubsystemsTests tests = new SubsystemsTests();
			tests.configure(server.apiRoot());

			tests.subsystemCollectionIsValid();
			tests.recursiveParameterUsesBooleanValues();
			tests.systemsRecursiveSearchIsComplete();
			tests.subsystemsRecursiveSearchIsComplete();
			tests.nestedAssociationsAreIncluded();

			assertTrue(server.calls("/api/systems/root") >= 1);
			assertTrue(server.calls("/api/systems/root/subsystems") >= 4);
			assertTrue(server.calls("/api/systems?recursive=false") >= 2);
			assertTrue(server.calls("/api/systems?recursive=true") >= 2);
			assertTrue(server.calls("/api/systems/root/subsystems?recursive=true") >= 1);
			assertTrue(server.calls("/api/systems/root/samplingFeatures") >= 1);
			assertTrue(server.calls("/api/systems/root/datastreams") >= 1);
			assertTrue(server.calls("/api/systems/root/controlstreams") >= 1);
			assertTrue(server.calls("/api/samplingFeatures") >= 1);
			assertTrue(server.calls("/api/datastreams") >= 1);
			assertTrue(server.calls("/api/controlstreams") >= 1);
		}
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-RECURSIVE-PARAM-001.
	 */
	@Test
	public void recursiveParameterChecksStatusWithoutParsingRepresentations() throws Exception {
		try (FixtureServer server = new FixtureServer(FixtureMode.RECURSIVE_PARAMETER_TEXT)) {
			server.start();
			SubsystemsTests tests = configured(server);

			tests.recursiveParameterUsesBooleanValues();

			assertTrue(server.calls("/api/systems?recursive=false") == 1);
			assertTrue(server.calls("/api/systems?recursive=true") == 1);
		}
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-MEDIA-GATE-001.
	 */
	@Test
	public void unsupportedOrMissingCollectionMediaSkipsBeforeParsing() throws Exception {
		for (FixtureMode mode : new FixtureMode[] { FixtureMode.UNSUPPORTED_SUBSYSTEM_MEDIA,
				FixtureMode.MISSING_SUBSYSTEM_MEDIA }) {
			try (FixtureServer server = new FixtureServer(mode)) {
				server.start();
				SubsystemsTests tests = configured(server);

				assertThrows(SkipException.class, tests::subsystemCollectionIsValid);
				assertTrue(server.calls("/api/systems/root/subsystems") == 1);
			}
		}
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-MEDIA-GATE-001.
	 */
	@Test
	public void sensorMlSubsystemCollectionUsesFirstResponseWithoutLocalId() throws Exception {
		try (FixtureServer server = new FixtureServer(FixtureMode.SENSORML_SUBSYSTEM_MEDIA)) {
			server.start();

			configured(server).subsystemCollectionIsValid();

			assertTrue(server.calls("/api/systems/root/subsystems") == 1);
		}
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-MEDIA-GATE-001.
	 */
	@Test
	public void unsupportedPaginationMediaSkipsBeforeParsingSecondPage() throws Exception {
		try (FixtureServer server = new FixtureServer(FixtureMode.UNSUPPORTED_SUBSYSTEM_NEXT_MEDIA)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::subsystemCollectionIsValid);

			assertTrue(server.calls("/api/systems/root/subsystems") == 1);
			assertTrue(server.calls("/api/subsystem-page-2") == 1);
		}
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-MEDIA-GATE-001.
	 */
	@Test
	public void unsupportedRootMediaSkipsBeforeParsing() throws Exception {
		try (FixtureServer server = new FixtureServer(FixtureMode.UNSUPPORTED_ROOT_MEDIA)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::systemsRecursiveSearchIsComplete);

			assertTrue(server.calls("/api/systems") == 1);
			assertTrue(server.calls("/api/systems/root/subsystems") == 0);
		}
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-MEDIA-GATE-001.
	 */
	@Test
	public void missingRootMediaSkipsBeforeParsing() throws Exception {
		try (FixtureServer server = new FixtureServer(FixtureMode.MISSING_ROOT_MEDIA)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::systemsRecursiveSearchIsComplete);

			assertTrue(server.calls("/api/systems") == 1);
			assertTrue(server.calls("/api/systems/root/subsystems") == 0);
		}
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-MEDIA-GATE-001.
	 */
	@Test
	public void unsupportedRootPaginationMediaSkipsBeforeParsingSecondPage() throws Exception {
		try (FixtureServer server = new FixtureServer(FixtureMode.UNSUPPORTED_ROOT_NEXT_MEDIA)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::systemsRecursiveSearchIsComplete);

			assertTrue(server.calls("/api/systems") == 1);
			assertTrue(server.calls("/api/system-page-2") == 1);
			assertTrue(server.calls("/api/systems/root/subsystems") == 0);
		}
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-ASSOCIATION-IMPLEMENTATION-001.
	 */
	@Test
	public void topLevelImplementationPreventsAllParent404Skip() throws Exception {
		try (FixtureServer server = new FixtureServer(FixtureMode.ASSOCIATION_TOP_LEVEL_ONLY)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::nestedAssociationsAreIncluded);
		}
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-HIERARCHY-FAIL-CLOSED-001.
	 */
	@Test
	public void shortcutHierarchyFailsInsteadOfSkipping() throws Exception {
		try (FixtureServer server = new FixtureServer(FixtureMode.SHORTCUT_HIERARCHY)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::subsystemsRecursiveSearchIsComplete);
		}
	}

	private static SubsystemsTests configured(FixtureServer server) {
		SubsystemsTests tests = new SubsystemsTests();
		tests.configure(server.apiRoot());
		return tests;
	}

	private enum FixtureMode {

		SUCCESS,

		RECURSIVE_PARAMETER_TEXT,

		UNSUPPORTED_SUBSYSTEM_MEDIA,

		MISSING_SUBSYSTEM_MEDIA,

		SENSORML_SUBSYSTEM_MEDIA,

		UNSUPPORTED_SUBSYSTEM_NEXT_MEDIA,

		UNSUPPORTED_ROOT_MEDIA,

		MISSING_ROOT_MEDIA,

		UNSUPPORTED_ROOT_NEXT_MEDIA,

		ASSOCIATION_TOP_LEVEL_ONLY,

		SHORTCUT_HIERARCHY

	}

	private static final class FixtureServer implements AutoCloseable {

		private final HttpServer server;

		private final Map<String, AtomicInteger> calls = new ConcurrentHashMap<>();

		private final FixtureMode mode;

		private FixtureServer() throws IOException {
			this(FixtureMode.SUCCESS);
		}

		private FixtureServer(FixtureMode mode) throws IOException {
			this.mode = mode;
			this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
			this.server.createContext("/api", this::handle);
		}

		private void start() {
			this.server.start();
		}

		private URI apiRoot() {
			return URI.create("http://127.0.0.1:" + this.server.getAddress().getPort() + "/api/");
		}

		private int calls(String target) {
			AtomicInteger count = this.calls.get(target);
			return count == null ? 0 : count.get();
		}

		private void handle(HttpExchange exchange) throws IOException {
			String path = exchange.getRequestURI().getPath();
			String query = exchange.getRequestURI().getRawQuery();
			String target = query == null ? path : path + "?" + query;
			this.calls.computeIfAbsent(target, ignored -> new AtomicInteger()).incrementAndGet();

			if ("/api/systems".equals(path)) {
				if (this.mode == FixtureMode.RECURSIVE_PARAMETER_TEXT && query != null) {
					send(exchange, 200, "text/plain", "successful");
					return;
				}
				if (query == null && this.mode == FixtureMode.UNSUPPORTED_ROOT_MEDIA) {
					send(exchange, 200, "text/xml", "<systems/>");
					return;
				}
				if (query == null && this.mode == FixtureMode.MISSING_ROOT_MEDIA) {
					send(exchange, 200, null, "{\"items\":");
					return;
				}
				if (query == null && this.mode == FixtureMode.UNSUPPORTED_ROOT_NEXT_MEDIA) {
					sendGeoJson(exchange, paginatedSystems("root", apiRoot().resolve("system-page-2")));
					return;
				}
				if ("recursive=true".equals(query)) {
					sendGeoJson(exchange, systems("root", "child", "grandchild"));
				}
				else {
					sendGeoJson(exchange, systems("root"));
				}
				return;
			}
			if ("/api/system-page-2".equals(path)) {
				send(exchange, 200, "text/xml", "<systems/>");
				return;
			}
			if ("/api/systems/root".equals(path)) {
				sendGeoJson(exchange, system("root", true));
				return;
			}
			if ("/api/systems/root/subsystems".equals(path)) {
				if (query == null) {
					if (this.mode == FixtureMode.UNSUPPORTED_SUBSYSTEM_MEDIA) {
						send(exchange, 200, "text/xml", "<systems/>");
						return;
					}
					if (this.mode == FixtureMode.MISSING_SUBSYSTEM_MEDIA) {
						send(exchange, 200, null, "{\"items\":[]}");
						return;
					}
					if (this.mode == FixtureMode.SENSORML_SUBSYSTEM_MEDIA) {
						send(exchange, 200, "application/sml+json", sensorMlSystems());
						return;
					}
					if (this.mode == FixtureMode.UNSUPPORTED_SUBSYSTEM_NEXT_MEDIA) {
						sendGeoJson(exchange, paginatedSystems("child", apiRoot().resolve("subsystem-page-2")));
						return;
					}
				}
				sendGeoJson(exchange, "recursive=true".equals(query) || this.mode == FixtureMode.SHORTCUT_HIERARCHY
						? systems("child", "grandchild") : systems("child"));
				return;
			}
			if ("/api/subsystem-page-2".equals(path)) {
				send(exchange, 200, "text/xml", "<systems/>");
				return;
			}
			if ("/api/systems/child/subsystems".equals(path)) {
				sendGeoJson(exchange, systems("grandchild"));
				return;
			}
			if ("/api/systems/grandchild/subsystems".equals(path)) {
				sendGeoJson(exchange, systems());
				return;
			}
			for (String association : new String[] { "samplingFeatures", "datastreams", "controlstreams" }) {
				if (("/api/" + association).equals(path)) {
					if (this.mode != FixtureMode.ASSOCIATION_TOP_LEVEL_ONLY || "samplingFeatures".equals(association)) {
						sendJson(exchange, items());
					}
					else {
						send(exchange, 404, "application/json", "{}");
					}
					return;
				}
			}
			for (String association : new String[] { "samplingFeatures", "datastreams", "controlstreams" }) {
				if (this.mode == FixtureMode.ASSOCIATION_TOP_LEVEL_ONLY) {
					continue;
				}
				if (("/api/systems/root/" + association).equals(path)) {
					sendJson(exchange, items(association + "-root", association + "-child", association + "-grand"));
					return;
				}
				if (("/api/systems/child/" + association).equals(path)) {
					sendJson(exchange, items(association + "-child", association + "-grand"));
					return;
				}
				if (("/api/systems/grandchild/" + association).equals(path)) {
					sendJson(exchange, items(association + "-grand"));
					return;
				}
			}
			send(exchange, 404, "application/json", "{}");
		}

		private String systems(String... ids) {
			StringBuilder features = new StringBuilder();
			for (String id : ids) {
				if (features.length() > 0) {
					features.append(',');
				}
				features.append(system(id, false));
			}
			return """
					{"type":"FeatureCollection","features":[%s],
					 "links":[{"rel":"self","type":"application/geo+json","href":"%s"}]}
					""".formatted(features, apiRoot().resolve("systems"));
		}

		private String system(String id, boolean subsystemLink) {
			String links = """
					[{"rel":"canonical","type":"application/geo+json","href":"%s"}%s]
					""".formatted(apiRoot().resolve("systems/" + id), subsystemLink ? """
					,{"rel":"subsystems","type":"application/geo+json","href":"%s"}
					""".formatted(apiRoot().resolve("systems/" + id + "/subsystems")) : "");
			return """
					{"type":"Feature","id":"%s","geometry":null,
					 "properties":{"uid":"urn:ogc:system:%s","name":"%s","featureType":"sosa:System"},
					 "links":%s}
					""".formatted(id, id, id, links);
		}

		private static String items(String... ids) {
			StringBuilder values = new StringBuilder();
			for (String id : ids) {
				if (values.length() > 0) {
					values.append(',');
				}
				values.append("{\"id\":\"").append(id).append("\"}");
			}
			return "{\"items\":[" + values + "],\"links\":[]}";
		}

		private static String sensorMlSystems() {
			return """
					{"items":[{"type":"PhysicalSystem","label":"Child",
					 "uniqueId":"urn:ogc:system:child",
					 "definition":"http://www.w3.org/ns/sosa/System"}]}
					""";
		}

		private String paginatedSystems(String id, URI next) {
			return """
					{"type":"FeatureCollection","features":[%s],
					 "links":[{"rel":"next","type":"application/geo+json","href":"%s"}]}
					""".formatted(system(id, false), next);
		}

		private static void sendGeoJson(HttpExchange exchange, String body) throws IOException {
			send(exchange, 200, "application/geo+json", body);
		}

		private static void sendJson(HttpExchange exchange, String body) throws IOException {
			send(exchange, 200, "application/json", body);
		}

		private static void send(HttpExchange exchange, int status, String contentType, String body)
				throws IOException {
			byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
			if (contentType != null) {
				exchange.getResponseHeaders().set("Content-Type", contentType);
			}
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
