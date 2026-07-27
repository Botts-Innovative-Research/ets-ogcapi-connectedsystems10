package org.opengis.cite.ogcapiconnectedsystems10.conformance.subdeployments;

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
 * Controlled HTTP checks for all released Subdeployment procedures.
 */
public class VerifySubdeploymentsHttpProcedures {

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-DIRECT-HTTP-COVERAGE-001.
	 */
	@Test
	public void allFiveReleasedProceduresExecuteSuccessfulHttpPaths() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.SUCCESS)) {
			server.start();
			SubdeploymentsTests tests = configured(server);

			tests.subdeploymentCollectionIsValid();
			tests.recursiveParameterUsesBooleanValues();
			tests.deploymentsRecursiveSearchIsComplete();
			tests.subdeploymentsRecursiveSearchIsComplete();
			tests.recursiveAssociationsIncludeDescendants();

			assertTrue(server.calls("/api/deployments/root") >= 1);
			assertTrue(server.calls("/api/deployments/root/subdeployments") >= 4);
			assertTrue(server.calls("/api/deployments?recursive=false") >= 2);
			assertTrue(server.calls("/api/deployments?recursive=true") >= 2);
			assertTrue(server.calls("/api/deployments/root/subdeployments?recursive=true") >= 1);
			for (String relation : FixtureServer.ASSOCIATIONS) {
				assertTrue(server.calls("/api/deployments/root/" + relation) >= 1);
				assertTrue(server.calls("/api/deployments/child/" + relation) >= 1);
				assertTrue(server.calls("/api/deployments/grandchild/" + relation) == 0);
			}
		}
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-RECURSIVE-PARAM-001.
	 */
	@Test
	public void recursiveParameterChecksStatusWithoutParsingRepresentations() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.RECURSIVE_PARAMETER_TEXT)) {
			server.start();

			configured(server).recursiveParameterUsesBooleanValues();

			assertTrue(server.calls("/api/deployments?recursive=false") == 1);
			assertTrue(server.calls("/api/deployments?recursive=true") == 1);
		}
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-MEDIA-GATE-001.
	 */
	@Test
	public void unsupportedOrMissingChildCollectionMediaSkipsBeforeParsing() throws Exception {
		for (Mode mode : new Mode[] { Mode.UNSUPPORTED_CHILD_MEDIA, Mode.MISSING_CHILD_MEDIA }) {
			try (FixtureServer server = new FixtureServer(mode)) {
				server.start();

				assertThrows(SkipException.class, configured(server)::subdeploymentCollectionIsValid);
				assertTrue(server.calls("/api/deployments/root/subdeployments") == 1);
			}
		}
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-MEDIA-GATE-001.
	 */
	@Test
	public void sensorMlSubdeploymentCollectionUsesReleasedDeploymentSchema() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.SENSORML_CHILD_MEDIA)) {
			server.start();

			configured(server).subdeploymentCollectionIsValid();

			assertTrue(server.calls("/api/deployments/root/subdeployments") >= 2);
		}
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-MEDIA-GATE-001.
	 */
	@Test
	public void unsupportedChildPaginationMediaSkipsBeforeParsingSecondPage() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNSUPPORTED_CHILD_NEXT_MEDIA)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::subdeploymentCollectionIsValid);
			assertTrue(server.calls("/api/deployment-page-2") == 1);
		}
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-MEDIA-GATE-001.
	 */
	@Test
	public void unsupportedRootAndLaterRootMediaSkipBeforeHierarchyUse() throws Exception {
		for (Mode mode : new Mode[] { Mode.UNSUPPORTED_ROOT_MEDIA, Mode.MISSING_ROOT_MEDIA,
				Mode.UNSUPPORTED_ROOT_NEXT_MEDIA }) {
			try (FixtureServer server = new FixtureServer(mode)) {
				server.start();

				assertThrows(SkipException.class, configured(server)::deploymentsRecursiveSearchIsComplete);
				assertTrue(server.calls("/api/deployments/root/subdeployments") == 0);
			}
		}
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-LINK-EXACT-001.
	 */
	@Test
	public void wrongSubdeploymentLinkFailsCollectionProcedure() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.WRONG_SUBDEPLOYMENT_LINK)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::subdeploymentCollectionIsValid);
		}
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-HIERARCHY-FAIL-CLOSED-001.
	 */
	@Test
	public void shortcutHierarchyFailsInsteadOfPassingRecursiveSearch() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.SHORTCUT_HIERARCHY)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::subdeploymentsRecursiveSearchIsComplete);
		}
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-RECURSIVE-ASSOC-001.
	 */
	@Test
	public void missingDescendantAssociationIdFails() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.MISSING_DESCENDANT_ASSOCIATION)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::recursiveAssociationsIncludeDescendants);
		}
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-RECURSIVE-ASSOC-001.
	 */
	@Test
	public void missingParentOwnedAssociationIdFails() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.MISSING_PARENT_ASSOCIATION)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::recursiveAssociationsIncludeDescendants);
		}
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-ASSOCIATION-ORACLE-001.
	 */
	@Test
	public void missingIndependentAssociationEvidenceSkips() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.NO_ASSOCIATION_EVIDENCE)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::recursiveAssociationsIncludeDescendants);
		}
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-ASSOCIATION-LINK-001.
	 */
	@Test
	public void laterUsableAssociationLinkWinsOverUnsafeAndUnsupportedOccurrences() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.PREFERRED_ASSOCIATION_LINK)) {
			server.start();

			configured(server).recursiveAssociationsIncludeDescendants();
			assertTrue(server.calls("/api/deployments/root/deployedSystems") == 1);
			assertTrue(server.calls("/api/deployments/root/deployedSystems.html") == 0);
		}
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-ASSOCIATION-LINK-001.
	 */
	@Test
	public void noSafeComparableAssociationLinkSkips() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.NO_SAFE_ASSOCIATION_LINK)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::recursiveAssociationsIncludeDescendants);
		}
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-LINK-EXACT-001.
	 */
	@Test
	public void equivalentEncodedSubdeploymentTargetExecutes() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.EQUIVALENT_SUBDEPLOYMENT_LINK)) {
			server.start();

			configured(server).subdeploymentCollectionIsValid();
			assertTrue(server.calls("/api/deployments/root/subdeployments") >= 1);
		}
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-RECURSIVE-ASSOC-001.
	 */
	@Test
	public void absentAssociationLinksSkipWithReason() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.NO_ASSOCIATION_LINKS)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::recursiveAssociationsIncludeDescendants);
		}
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-PROCEDURE-ISOLATION-001.
	 */
	@Test
	public void noHierarchySkipsEvidenceDependentMethodsButNotRecursiveParameter() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.NO_HIERARCHY)) {
			server.start();
			SubdeploymentsTests tests = configured(server);

			tests.recursiveParameterUsesBooleanValues();
			assertThrows(SkipException.class, tests::subdeploymentCollectionIsValid);
			assertThrows(SkipException.class, tests::deploymentsRecursiveSearchIsComplete);
			assertThrows(SkipException.class, tests::subdeploymentsRecursiveSearchIsComplete);
			assertThrows(SkipException.class, tests::recursiveAssociationsIncludeDescendants);
		}
	}

	private static SubdeploymentsTests configured(FixtureServer server) {
		SubdeploymentsTests tests = new SubdeploymentsTests();
		tests.configure(server.apiRoot());
		if (server.mode != Mode.NO_ASSOCIATION_EVIDENCE) {
			tests.configureAssociationEvidence(FixtureServer.associationEvidence());
		}
		return tests;
	}

	private enum Mode {

		SUCCESS,

		RECURSIVE_PARAMETER_TEXT,

		UNSUPPORTED_CHILD_MEDIA,

		MISSING_CHILD_MEDIA,

		SENSORML_CHILD_MEDIA,

		UNSUPPORTED_CHILD_NEXT_MEDIA,

		UNSUPPORTED_ROOT_MEDIA,

		MISSING_ROOT_MEDIA,

		UNSUPPORTED_ROOT_NEXT_MEDIA,

		WRONG_SUBDEPLOYMENT_LINK,

		SHORTCUT_HIERARCHY,

		MISSING_DESCENDANT_ASSOCIATION,

		MISSING_PARENT_ASSOCIATION,

		NO_ASSOCIATION_EVIDENCE,

		PREFERRED_ASSOCIATION_LINK,

		NO_SAFE_ASSOCIATION_LINK,

		EQUIVALENT_SUBDEPLOYMENT_LINK,

		NO_ASSOCIATION_LINKS,

		NO_HIERARCHY

	}

	private static final class FixtureServer implements AutoCloseable {

		private static final String[] ASSOCIATIONS = { "deployedSystems", "featuresOfInterest", "samplingFeatures",
				"datastreams", "controlstreams" };

		private final HttpServer server;

		private final Map<String, AtomicInteger> calls = new ConcurrentHashMap<>();

		private final Mode mode;

		private FixtureServer(Mode mode) throws IOException {
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

			if ("/api/deployments".equals(path)) {
				handleDeployments(exchange, query);
				return;
			}
			if ("/api/root-page-2".equals(path) || "/api/deployment-page-2".equals(path)) {
				send(exchange, 200, "text/xml", "<deployments/>");
				return;
			}
			if (path.matches("/api/deployments/[^/]+$")) {
				String id = path.substring(path.lastIndexOf('/') + 1);
				sendGeoJson(exchange, deployment(id, !"grandchild".equals(id)));
				return;
			}
			if (path.matches("/api/deployments/[^/]+/subdeployments")) {
				handleSubdeployments(exchange, path, query);
				return;
			}
			for (String relation : ASSOCIATIONS) {
				String suffix = "/" + relation;
				if (path.startsWith("/api/deployments/") && path.endsWith(suffix)) {
					String id = path.substring("/api/deployments/".length(), path.length() - suffix.length());
					handleAssociation(exchange, id, relation);
					return;
				}
			}
			send(exchange, 404, "application/json", "{}");
		}

		private void handleDeployments(HttpExchange exchange, String query) throws IOException {
			if (this.mode == Mode.RECURSIVE_PARAMETER_TEXT && query != null) {
				send(exchange, 200, "text/plain", "successful");
				return;
			}
			if (query == null && this.mode == Mode.UNSUPPORTED_ROOT_MEDIA) {
				send(exchange, 200, "text/xml", "<deployments/>");
				return;
			}
			if (query == null && this.mode == Mode.MISSING_ROOT_MEDIA) {
				send(exchange, 200, null, "{\"features\":");
				return;
			}
			if (query == null && this.mode == Mode.UNSUPPORTED_ROOT_NEXT_MEDIA) {
				sendGeoJson(exchange, deploymentCollectionWithNext(apiRoot().resolve("root-page-2"), "root"));
				return;
			}
			if ("recursive=true".equals(query) && this.mode != Mode.NO_HIERARCHY) {
				sendGeoJson(exchange, deploymentCollection("root", "child", "grandchild"));
				return;
			}
			sendGeoJson(exchange, deploymentCollection("root"));
		}

		private void handleSubdeployments(HttpExchange exchange, String path, String query) throws IOException {
			String id = path.split("/")[3];
			if ("root".equals(id) && query == null && this.mode == Mode.UNSUPPORTED_CHILD_MEDIA) {
				send(exchange, 200, "text/xml", "<deployments/>");
				return;
			}
			if ("root".equals(id) && query == null && this.mode == Mode.MISSING_CHILD_MEDIA) {
				send(exchange, 200, null, "{\"features\":[]}");
				return;
			}
			if ("root".equals(id) && query == null && this.mode == Mode.SENSORML_CHILD_MEDIA) {
				send(exchange, 200, "application/sml+json", sensorMlDeploymentCollection("child"));
				return;
			}
			if ("root".equals(id) && query == null && this.mode == Mode.UNSUPPORTED_CHILD_NEXT_MEDIA) {
				sendGeoJson(exchange, deploymentCollectionWithNext(apiRoot().resolve("deployment-page-2"), "child"));
				return;
			}
			if ("root".equals(id) && this.mode == Mode.NO_HIERARCHY) {
				sendGeoJson(exchange, deploymentCollection());
				return;
			}
			if ("root".equals(id)) {
				boolean recursive = "recursive=true".equals(query) || this.mode == Mode.SHORTCUT_HIERARCHY;
				sendGeoJson(exchange,
						recursive ? deploymentCollection("child", "grandchild") : deploymentCollection("child"));
				return;
			}
			if ("child".equals(id)) {
				sendGeoJson(exchange, deploymentCollection("grandchild"));
				return;
			}
			sendGeoJson(exchange, deploymentCollection());
		}

		private void handleAssociation(HttpExchange exchange, String id, String relation) throws IOException {
			if ("root".equals(id)) {
				if (this.mode == Mode.MISSING_DESCENDANT_ASSOCIATION && "datastreams".equals(relation)) {
					sendJson(exchange, items(relation + "-root", relation + "-child"));
					return;
				}
				if (this.mode == Mode.MISSING_PARENT_ASSOCIATION && "datastreams".equals(relation)) {
					sendJson(exchange, items(relation + "-child", relation + "-grandchild"));
					return;
				}
				sendJson(exchange, items(relation + "-root", relation + "-child", relation + "-grandchild"));
				return;
			}
			if ("child".equals(id)) {
				sendJson(exchange, items(relation + "-child", relation + "-grandchild"));
				return;
			}
			sendJson(exchange, items(relation + "-grandchild"));
		}

		private String deploymentCollection(String... ids) {
			StringBuilder features = new StringBuilder();
			for (String id : ids) {
				if (features.length() > 0) {
					features.append(',');
				}
				features.append(deployment(id, false));
			}
			return """
					{"type":"FeatureCollection","features":[%s],
					 "links":[{"rel":"self","type":"application/geo+json","href":"%s"}]}
					""".formatted(features, apiRoot().resolve("deployments"));
		}

		private String deploymentCollectionWithNext(URI next, String... ids) {
			String collection = deploymentCollection(ids);
			return collection.replace("]}",
					",{\"rel\":\"next\",\"type\":\"application/geo+json\",\"href\":\"" + next + "\"}]}");
		}

		private String deployment(String id, boolean subdeploymentLink) {
			StringBuilder links = new StringBuilder();
			links.append("{\"rel\":\"canonical\",\"type\":\"application/geo+json\",\"href\":\"")
				.append(apiRoot().resolve("deployments/" + id))
				.append("\"}");
			if (subdeploymentLink) {
				String href = apiRoot().resolve("deployments/" + id + "/subdeployments").toString();
				if (this.mode == Mode.WRONG_SUBDEPLOYMENT_LINK && "root".equals(id)) {
					href += "?recursive=false";
				}
				if (this.mode == Mode.EQUIVALENT_SUBDEPLOYMENT_LINK && "root".equals(id)) {
					href = href.replace("/root/", "/%72oot/");
				}
				links.append(",{\"rel\":\"subdeployments\",\"type\":\"application/geo+json\",\"href\":\"")
					.append(href)
					.append("\"}");
			}
			if (this.mode != Mode.NO_ASSOCIATION_LINKS) {
				for (String relation : ASSOCIATIONS) {
					String href = apiRoot().resolve("deployments/" + id + "/" + relation).toString();
					if ("root".equals(id) && "deployedSystems".equals(relation)
							&& (this.mode == Mode.PREFERRED_ASSOCIATION_LINK
									|| this.mode == Mode.NO_SAFE_ASSOCIATION_LINK)) {
						links.append(",{\"rel\":\"deployedSystems\",\"type\":\"application/json\",")
							.append("\"href\":\"https://other.test/api/deployments/root/deployedSystems\"}");
						links.append(",{\"rel\":\"deployedSystems\",\"type\":\"text/html\",\"href\":\"")
							.append(apiRoot().resolve("deployments/root/deployedSystems.html"))
							.append("\"}");
						if (this.mode == Mode.NO_SAFE_ASSOCIATION_LINK) {
							continue;
						}
					}
					links.append(",{\"rel\":\"")
						.append(relation)
						.append("\"")
						.append(",\"type\":\"application/json\",\"href\":\"")
						.append(href)
						.append("\"}");
				}
			}
			return """
					{"type":"Feature","id":"%s","geometry":null,
					 "properties":{"uid":"urn:ogc:deployment:%s","name":"Deployment %s",
					   "featureType":"sosa:Deployment",
					   "validTime":["2026-01-01T00:00:00Z","2026-12-31T00:00:00Z"]},
					 "links":[%s]}
					""".formatted(id, id, id, links);
		}

		private String sensorMlDeploymentCollection(String id) {
			return """
					{"items":[{"type":"Deployment","id":"%s","label":"Deployment %s",
					 "uniqueId":"urn:ogc:deployment:%s","definition":"sosa:Deployment"}],
					 "links":[{"rel":"self","type":"application/sml+json","href":"%s"}]}
					""".formatted(id, id, id, apiRoot().resolve("deployments"));
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

		private static String associationEvidence() {
			StringBuilder json = new StringBuilder("{");
			for (String deployment : new String[] { "root", "child", "grandchild" }) {
				if (json.length() > 1) {
					json.append(',');
				}
				json.append('"').append(deployment).append("\":{");
				for (int i = 0; i < ASSOCIATIONS.length; i++) {
					if (i > 0) {
						json.append(',');
					}
					String relation = ASSOCIATIONS[i];
					json.append('"')
						.append(relation)
						.append("\":[\"")
						.append(relation)
						.append('-')
						.append(deployment)
						.append("\"]");
				}
				json.append('}');
			}
			return json.append('}').toString();
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
