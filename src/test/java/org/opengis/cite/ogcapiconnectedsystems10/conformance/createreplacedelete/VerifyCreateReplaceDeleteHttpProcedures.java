package org.opengis.cite.ogcapiconnectedsystems10.conformance.createreplacedelete;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.Test;
import org.testng.SkipException;

/**
 * Controlled HTTP execution for the released Create/Replace/Delete procedures.
 */
public class VerifyCreateReplaceDeleteHttpProcedures {

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-DIRECT-HTTP-COVERAGE-001.
	 */
	@Test
	public void allTwelveProceduresExecuteAgainstStatefulHttpApi() throws Exception {
		try (Fixture fixture = new Fixture()) {
			CreateReplaceDeleteSupport support = fixture.support();

			support.systemsCreateReplaceDelete();
			support.systemDeleteCascade();
			support.subsystemsCreate();
			support.deploymentsCreateReplaceDelete();
			support.subdeploymentsCreate();
			support.proceduresCreateReplaceDelete();
			support.samplingFeaturesCreateReplaceDelete();
			support.propertiesCreateReplaceDelete();
			support.resourcesCreateInCustomCollections();
			support.resourcesReplaceInCustomCollections();
			support.resourcesDeleteInCustomCollections();
			support.resourcesAddToCustomCollections();

			assertEquals(0, fixture.liveCanonicalResources());
			assertTrue("OPTIONS count: " + fixture.calls("OPTIONS"), fixture.calls("OPTIONS") >= 22);
			assertTrue("POST count: " + fixture.calls("POST"), fixture.calls("POST") >= 30);
			assertTrue("PUT count: " + fixture.calls("PUT"), fixture.calls("PUT") >= 8);
			assertTrue("DELETE count: " + fixture.calls("DELETE"), fixture.calls("DELETE") >= 30);
			assertTrue("URI-list POST count: " + fixture.uriListPosts(), fixture.uriListPosts() >= 5);
			assertTrue("cascade conflict count: " + fixture.cascadeConflicts(), fixture.cascadeConflicts() >= 2);
			assertEquals("non-System DELETE must omit cascade", 0, fixture.nonSystemCascadeParameters());
			assertTrue(fixture.writeCalls("application/geo+json") > 0);
			assertTrue(fixture.writeCalls("application/sml+json") > 0);
		}
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-MUTATION-SAFETY-001.
	 */
	@Test
	public void missingMutationOptInPerformsNoWrite() throws Exception {
		try (Fixture fixture = new Fixture()) {
			CreateReplaceDeleteSupport support = new CreateReplaceDeleteSupport(fixture.apiRoot(), null, null);

			assertThrows(SkipException.class, support::systemsCreateReplaceDelete);
			assertEquals(0, fixture.writeCalls());
		}
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-DIRECT-PREREQUISITES-001.
	 */
	@Test
	public void featuresPart4NearMatchCannotSatisfyReleasedInheritance() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.nearMatchInheritanceOnly = true;

			assertThrows(SkipException.class, fixture.support()::systemsCreateReplaceDelete);
			assertEquals(0, fixture.writeCalls());
		}
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-INHERITED-TRANSACTION-001.
	 */
	@Test
	public void missingLocationFailsClosed() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.omitLocation = true;

			AssertionError error = assertThrows(AssertionError.class, fixture.support()::systemsCreateReplaceDelete);

			assertTrue(error.getMessage().contains("without Location"));
			assertEquals(0, fixture.liveCanonicalResources());
		}
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-CLEANUP-001.
	 */
	@Test
	public void unrelatedLocationIsNotDeletedAndSubmittedIdentityIsDiscoveredForCleanup() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.seedUnrelatedSystem();
			fixture.wrongLocation = true;

			AssertionError error = assertThrows(AssertionError.class, fixture.support()::systemsCreateReplaceDelete);

			assertTrue(error.getMessage().contains("instead of submitted identity"));
			assertTrue(fixture.hasResource("/api/systems/unrelated"));
			assertEquals(1, fixture.liveCanonicalResources());
		}
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-REPRESENTATION-CLOSURE-001.
	 */
	@Test
	public void ignoredReplacementFailsAndOwnedResourceIsCleaned() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.ignorePut = true;

			assertThrows(AssertionError.class, fixture.support()::systemsCreateReplaceDelete);
			assertEquals(0, fixture.liveCanonicalResources());
			assertTrue(fixture.calls("DELETE") >= 1);
		}
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-CASCADE-001.
	 */
	@Test
	public void wrongCascadeConflictStatusFailsAndCleansGraph() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.wrongConflictStatus = true;

			assertThrows(AssertionError.class, fixture.support()::systemDeleteCascade);
			assertEquals(0, fixture.liveCanonicalResources());
		}
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-CASCADE-001.
	 */
	@Test
	public void missingInitialDeploymentAssociationFailsBeforeCascadeDelete() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.omitAssociationOnPreDeleteGet = true;

			AssertionError error = assertThrows(AssertionError.class, fixture.support()::systemDeleteCascade);

			assertTrue(error.getMessage().contains("pre-delete Deployment"));
			assertEquals(0, fixture.liveCanonicalResources());
		}
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-CUSTOM-DELETE-001.
	 */
	@Test
	public void customRootDeleteMustRemoveCollectionOccurrence() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.retainAliasesOnCanonicalDelete = true;

			assertThrows(AssertionError.class, fixture.support()::resourcesDeleteInCustomCollections);
		}
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-CUSTOM-URI-LIST-001.
	 */
	@Test
	public void uriListAssociationRequiresLocationAndCleansOwnedResources() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.omitUriListLocation = true;

			AssertionError error = assertThrows(AssertionError.class,
					fixture.support()::resourcesAddToCustomCollections);

			assertTrue(error.getMessage().contains("without Location"));
			assertEquals(0, fixture.liveCanonicalResources());
		}
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-NESTED-CANONICAL-001.
	 */
	@Test
	public void nestedLocationCannotSubstituteForRootCanonicalResource() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.nestedLocationWithoutCanonical = true;

			assertThrows(AssertionError.class, fixture.support()::subsystemsCreate);
			assertEquals(0, fixture.liveCanonicalResources());
		}
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-INHERITED-TRANSACTION-001.
	 */
	@Test
	public void queuedCreateReplaceDeleteWaitForObservedPostconditions() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.queuedWrites = true;

			fixture.support(2_000L, 10L).systemsCreateReplaceDelete();

			assertTrue(fixture.queuedPosts.get() >= 2);
			assertTrue(fixture.queuedPuts.get() >= 2);
			assertTrue(fixture.queuedDeletes.get() >= 2);
			assertEquals(0, fixture.liveCanonicalResources());
		}
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-CUSTOM-URI-LIST-001.
	 */
	@Test
	public void queuedUriListAssociationWaitsForComputedOccurrence() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.queuedWrites = true;

			fixture.support(2_000L, 10L).resourcesAddToCustomCollections();

			assertTrue(fixture.queuedUriListPosts.get() >= 5);
			assertEquals(0, fixture.liveCanonicalResources());
		}
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-INHERITED-TRANSACTION-001.
	 */
	@Test
	public void queuedCreateWithoutObservedPostconditionSkipsAndCleans() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.stallQueuedCreate = true;

			SkipException error = assertThrows(SkipException.class,
					fixture.support(80L, 10L)::systemsCreateReplaceDelete);

			assertTrue(error.getMessage().contains("accepted-but-inconclusive"));
			assertEquals(0, fixture.liveCanonicalResources());
		}
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-INHERITED-TRANSACTION-001.
	 */
	@Test
	public void queuedReplaceWithoutObservedPostconditionSkipsAndCleans() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.stallQueuedPut = true;

			SkipException error = assertThrows(SkipException.class,
					fixture.support(80L, 10L)::systemsCreateReplaceDelete);

			assertTrue(error.getMessage().contains("accepted-but-inconclusive"));
			assertEquals(0, fixture.liveCanonicalResources());
		}
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-INHERITED-TRANSACTION-001.
	 */
	@Test
	public void queuedDeleteWithoutObservedPostconditionSkipsAndCleanupRetries() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.stallFirstQueuedDelete = true;

			SkipException error = assertThrows(SkipException.class,
					fixture.support(80L, 10L)::systemsCreateReplaceDelete);

			assertTrue(error.getMessage().contains("accepted-but-inconclusive"));
			assertEquals(0, fixture.liveCanonicalResources());
			assertTrue(fixture.calls("DELETE") >= 2);
		}
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-CLEANUP-001.
	 */
	@Test
	public void verifiedCustomAliasCleanupStillRemovesCanonicalResource() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.customAliasLocation = true;
			fixture.corruptCustomAliasAfterFirstGet = true;

			assertThrows(AssertionError.class, fixture.support()::resourcesCreateInCustomCollections);

			assertEquals(0, fixture.liveCanonicalResources());
			assertTrue(fixture.aliasDeletes.get() >= 1);
		}
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-INHERITED-TRANSACTION-001.
	 */
	@Test
	public void asynchronousPollingPropertiesRejectInvalidValues() throws Exception {
		String timeoutProperty = "org.opengis.cite.ogcapiconnectedsystems10.crd.asyncTimeoutMillis";
		String pollProperty = "org.opengis.cite.ogcapiconnectedsystems10.crd.asyncPollMillis";
		String previousTimeout = System.getProperty(timeoutProperty);
		String previousPoll = System.getProperty(pollProperty);
		try (Fixture fixture = new Fixture()) {
			System.setProperty(timeoutProperty, "0");
			IllegalArgumentException timeoutError = assertThrows(IllegalArgumentException.class,
					() -> new CreateReplaceDeleteSupport(fixture.apiRoot(), "true", "dedicated-mutable-iut"));
			assertTrue(timeoutError.getMessage().contains(timeoutProperty));

			System.clearProperty(timeoutProperty);
			System.setProperty(pollProperty, "not-a-number");
			IllegalArgumentException pollError = assertThrows(IllegalArgumentException.class,
					() -> new CreateReplaceDeleteSupport(fixture.apiRoot(), "true", "dedicated-mutable-iut"));
			assertTrue(pollError.getMessage().contains(pollProperty));
		}
		finally {
			restoreProperty(timeoutProperty, previousTimeout);
			restoreProperty(pollProperty, previousPoll);
		}
	}

	private static void restoreProperty(String name, String value) {
		if (value == null) {
			System.clearProperty(name);
		}
		else {
			System.setProperty(name, value);
		}
	}

	private static final class Fixture implements AutoCloseable {

		private static final ObjectMapper JSON = new ObjectMapper();

		private static final String CONF_BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/";

		private final HttpServer server;

		private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

		private final Map<String, Resource> resources = new ConcurrentHashMap<>();

		private final Map<String, String> aliases = new ConcurrentHashMap<>();

		private final Map<String, Resource> deletedResources = new ConcurrentHashMap<>();

		private final Map<String, AtomicInteger> methods = new ConcurrentHashMap<>();

		private final Map<String, AtomicInteger> writeMediaTypes = new ConcurrentHashMap<>();

		private final AtomicInteger sequence = new AtomicInteger();

		private final AtomicInteger uriListPosts = new AtomicInteger();

		private final AtomicInteger cascadeConflicts = new AtomicInteger();

		private final AtomicInteger nonSystemCascadeParameters = new AtomicInteger();

		private final AtomicInteger deploymentGets = new AtomicInteger();

		private final AtomicInteger queuedPosts = new AtomicInteger();

		private final AtomicInteger queuedPuts = new AtomicInteger();

		private final AtomicInteger queuedDeletes = new AtomicInteger();

		private final AtomicInteger queuedUriListPosts = new AtomicInteger();

		private final AtomicInteger stalledDeleteResponses = new AtomicInteger();

		private final AtomicInteger customAliasGets = new AtomicInteger();

		private final AtomicInteger aliasDeletes = new AtomicInteger();

		private volatile boolean omitLocation;

		private volatile boolean omitUriListLocation;

		private volatile boolean wrongLocation;

		private volatile boolean ignorePut;

		private volatile boolean wrongConflictStatus;

		private volatile boolean retainAliasesOnCanonicalDelete;

		private volatile boolean nearMatchInheritanceOnly;

		private volatile boolean nestedLocationWithoutCanonical;

		private volatile boolean omitAssociationOnPreDeleteGet;

		private volatile boolean queuedWrites;

		private volatile boolean stallQueuedCreate;

		private volatile boolean stallQueuedPut;

		private volatile boolean stallFirstQueuedDelete;

		private volatile boolean customAliasLocation;

		private volatile boolean corruptCustomAliasAfterFirstGet;

		Fixture() throws IOException {
			this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
			this.server.createContext("/api", this::handle);
			this.server.start();
		}

		URI apiRoot() {
			return URI.create("http://127.0.0.1:" + this.server.getAddress().getPort() + "/api/");
		}

		CreateReplaceDeleteSupport support() {
			return new CreateReplaceDeleteSupport(apiRoot(), "true", "dedicated-mutable-iut");
		}

		CreateReplaceDeleteSupport support(long asyncTimeoutMillis, long asyncPollMillis) {
			return new CreateReplaceDeleteSupport(apiRoot(), "true", "dedicated-mutable-iut", asyncTimeoutMillis,
					asyncPollMillis);
		}

		int calls(String method) {
			AtomicInteger count = this.methods.get(method);
			return count == null ? 0 : count.get();
		}

		int writeCalls() {
			return calls("POST") + calls("PUT") + calls("DELETE");
		}

		int writeCalls(String mediaType) {
			AtomicInteger count = this.writeMediaTypes.get(mediaType);
			return count == null ? 0 : count.get();
		}

		int uriListPosts() {
			return this.uriListPosts.get();
		}

		int cascadeConflicts() {
			return this.cascadeConflicts.get();
		}

		int nonSystemCascadeParameters() {
			return this.nonSystemCascadeParameters.get();
		}

		int liveCanonicalResources() {
			return this.resources.size();
		}

		boolean hasResource(String path) {
			return this.resources.containsKey(path);
		}

		void seedUnrelatedSystem() {
			Map<String, Object> body = new LinkedHashMap<>(
					CreateReplaceDeleteSupport.systemBody("unrelated", "urn:ets:unrelated"));
			body.put("id", "unrelated");
			this.resources.put("/api/systems/unrelated", new Resource("systems", body, null));
		}

		@Override
		public void close() {
			this.server.stop(0);
			this.scheduler.shutdownNow();
		}

		private void handle(HttpExchange exchange) throws IOException {
			String method = exchange.getRequestMethod();
			this.methods.computeIfAbsent(method, ignored -> new AtomicInteger()).incrementAndGet();
			String path = exchange.getRequestURI().getPath();
			try {
				if ("/api/conformance".equals(path)) {
					json(exchange, 200, conformance());
					return;
				}
				if ("/api/collections".equals(path)) {
					json(exchange, 200, collections());
					return;
				}
				if ("OPTIONS".equals(method)) {
					exchange.getResponseHeaders().set("Allow", "GET, POST, PUT, DELETE, OPTIONS");
					respond(exchange, 200, "");
					return;
				}
				if ("POST".equals(method)) {
					post(exchange, path);
					return;
				}
				if ("GET".equals(method)) {
					get(exchange, path);
					return;
				}
				if ("PUT".equals(method)) {
					put(exchange, path);
					return;
				}
				if ("DELETE".equals(method)) {
					delete(exchange, path);
					return;
				}
				respond(exchange, 405, "");
			}
			catch (RuntimeException ex) {
				json(exchange, 500, Map.of("error", ex.toString()));
			}
		}

		private void post(HttpExchange exchange, String path) throws IOException {
			String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
			if (contentType != null && contentType.startsWith("text/uri-list")) {
				countWriteMediaType(contentType);
				this.uriListPosts.incrementAndGet();
				String firstAlias = null;
				Map<String, String> pendingAliases = new LinkedHashMap<>();
				for (String line : requestText(exchange).lines().toList()) {
					if (line.isBlank()) {
						continue;
					}
					String canonical = URI.create(line).getPath();
					String alias = path + "/" + last(canonical);
					pendingAliases.put(alias, canonical);
					if (firstAlias == null) {
						firstAlias = alias;
					}
				}
				if (firstAlias == null) {
					json(exchange, 400, Map.of("error", "empty URI list"));
					return;
				}
				if (this.queuedWrites) {
					this.queuedUriListPosts.incrementAndGet();
					this.scheduler.schedule(() -> this.aliases.putAll(pendingAliases), 40L, TimeUnit.MILLISECONDS);
					json(exchange, 202, Map.of());
					return;
				}
				this.aliases.putAll(pendingAliases);
				if (!this.omitUriListLocation) {
					exchange.getResponseHeaders().set("Location", firstAlias);
				}
				json(exchange, 201, Map.of("id", last(firstAlias)));
				return;
			}

			countWriteMediaType(contentType);
			Map<String, Object> body = requestJson(exchange);
			String id = "r" + this.sequence.incrementAndGet();
			String root = rootFor(path, body);
			String canonical = "/api/" + root + "/" + id;
			String parent = parentFor(path);
			boolean nestedOnly = this.nestedLocationWithoutCanonical && parent != null;
			String stored = nestedOnly ? path + "/" + id : canonical;
			Runnable materialize = () -> {
				this.resources.put(stored, new Resource(root, bodyWithId(body, id), parent));
				if (path.startsWith("/api/collections/")) {
					this.aliases.put(path + "/" + id, canonical);
				}
			};
			if (this.stallQueuedCreate) {
				this.queuedPosts.incrementAndGet();
				json(exchange, 202, Map.of());
				return;
			}
			if (this.queuedWrites) {
				this.queuedPosts.incrementAndGet();
				this.scheduler.schedule(materialize, 40L, TimeUnit.MILLISECONDS);
				json(exchange, 202, Map.of());
				return;
			}
			materialize.run();
			if (this.omitLocation) {
				json(exchange, 201, Map.of());
				return;
			}
			exchange.getResponseHeaders()
				.set("Location",
						this.wrongLocation ? "/api/systems/unrelated"
								: this.customAliasLocation && path.startsWith("/api/collections/") ? path + "/" + id
										: nestedOnly ? stored : canonical);
			json(exchange, 201, Map.of("id", id));
		}

		private void get(HttpExchange exchange, String path) throws IOException {
			if (path.matches("/api/(systems|deployments|procedures|samplingFeatures|properties)")) {
				collection(exchange, path.substring("/api/".length()));
				return;
			}
			String canonical = this.aliases.getOrDefault(path, path);
			Resource resource = this.resources.get(canonical);
			if (resource == null && this.retainAliasesOnCanonicalDelete && this.aliases.containsKey(path)) {
				resource = this.deletedResources.get(canonical);
			}
			if (resource == null) {
				respond(exchange, 404, "");
				return;
			}
			Map<String, Object> responseBody = resource.body();
			if (this.omitAssociationOnPreDeleteGet && "deployments".equals(resource.root())
					&& this.deploymentGets.incrementAndGet() > 1) {
				responseBody = deepCopy(responseBody);
				removeFirstDeploymentReference(responseBody);
			}
			if (this.corruptCustomAliasAfterFirstGet && path.startsWith("/api/collections/")
					&& this.customAliasGets.incrementAndGet() > 1) {
				responseBody = deepCopy(responseBody);
				corruptSubmittedContent(responseBody);
			}
			json(exchange, 200, responseBody);
		}

		private void put(HttpExchange exchange, String path) throws IOException {
			countWriteMediaType(exchange.getRequestHeaders().getFirst("Content-Type"));
			String canonical = this.aliases.getOrDefault(path, path);
			Resource current = this.resources.get(canonical);
			if (current == null) {
				respond(exchange, 404, "");
				return;
			}
			Map<String, Object> replacement = requestJson(exchange);
			if (this.stallQueuedPut) {
				this.queuedPuts.incrementAndGet();
				respond(exchange, 202, "");
				return;
			}
			if (this.queuedWrites) {
				this.queuedPuts.incrementAndGet();
				this.scheduler.schedule(
						() -> this.resources.put(canonical, new Resource(current.root(),
								bodyWithId(replacement, last(canonical)), current.parent())),
						40L, TimeUnit.MILLISECONDS);
				respond(exchange, 202, "");
				return;
			}
			if (!this.ignorePut) {
				this.resources.put(canonical,
						new Resource(current.root(), bodyWithId(replacement, last(canonical)), current.parent()));
			}
			respond(exchange, 204, "");
		}

		private void delete(HttpExchange exchange, String path) throws IOException {
			if (this.aliases.containsKey(path)) {
				this.aliasDeletes.incrementAndGet();
				if (this.queuedWrites) {
					this.queuedDeletes.incrementAndGet();
					this.scheduler.schedule(() -> this.aliases.remove(path), 40L, TimeUnit.MILLISECONDS);
					respond(exchange, 202, "");
					return;
				}
				this.aliases.remove(path);
				respond(exchange, 204, "");
				return;
			}
			Resource resource = this.resources.get(path);
			if (resource == null) {
				respond(exchange, 404, "");
				return;
			}
			boolean cascade = "true".equals(query(exchange, "cascade"));
			if (!"systems".equals(resource.root()) && query(exchange, "cascade") != null) {
				this.nonSystemCascadeParameters.incrementAndGet();
			}
			if ("systems".equals(resource.root()) && !cascade && hasSystemDependency(path, resource)) {
				this.cascadeConflicts.incrementAndGet();
				respond(exchange, this.wrongConflictStatus ? 400 : 409, "");
				return;
			}
			if (this.stallFirstQueuedDelete && this.stalledDeleteResponses.getAndIncrement() == 0) {
				this.queuedDeletes.incrementAndGet();
				respond(exchange, 202, "");
				return;
			}
			if (this.queuedWrites) {
				this.queuedDeletes.incrementAndGet();
				this.scheduler.schedule(() -> removeResource(path, resource, cascade), 40L, TimeUnit.MILLISECONDS);
				respond(exchange, 202, "");
				return;
			}
			removeResource(path, resource, cascade);
			respond(exchange, 204, "");
		}

		private void removeResource(String path, Resource resource, boolean cascade) {
			if ("systems".equals(resource.root()) && cascade) {
				removeSystemDependencies(path, uid(resource.body()));
			}
			if (this.retainAliasesOnCanonicalDelete) {
				this.deletedResources.put(path, resource);
			}
			this.resources.remove(path);
			if (!this.retainAliasesOnCanonicalDelete) {
				this.aliases.entrySet().removeIf(entry -> path.equals(entry.getValue()));
			}
		}

		@SuppressWarnings("unchecked")
		private void corruptSubmittedContent(Map<String, Object> body) {
			Object properties = body.get("properties");
			if (properties instanceof Map) {
				((Map<String, Object>) properties).put("name", "corrupted after initial alias verification");
				return;
			}
			body.put("label", "corrupted after initial alias verification");
		}

		private void countWriteMediaType(String contentType) {
			if (contentType == null) {
				return;
			}
			String mediaType = contentType.split(";", 2)[0];
			this.writeMediaTypes.computeIfAbsent(mediaType, ignored -> new AtomicInteger()).incrementAndGet();
		}

		private boolean hasSystemDependency(String systemPath, Resource system) {
			String systemUid = uid(system.body());
			return this.resources.values()
				.stream()
				.anyMatch(resource -> systemPath.equals(resource.parent())
						|| "deployments".equals(resource.root()) && contains(resource.body(), systemUid));
		}

		private void removeSystemDependencies(String systemPath, String systemUid) {
			List<String> children = this.resources.entrySet()
				.stream()
				.filter(entry -> systemPath.equals(entry.getValue().parent()))
				.map(Map.Entry::getKey)
				.toList();
			children.forEach(this.resources::remove);
			for (Map.Entry<String, Resource> entry : new ArrayList<>(this.resources.entrySet())) {
				Resource resource = entry.getValue();
				if ("deployments".equals(resource.root()) && contains(resource.body(), systemUid)) {
					Map<String, Object> updated = deepCopy(resource.body());
					removeDeploymentReference(updated, systemUid);
					this.resources.put(entry.getKey(), new Resource(resource.root(), updated, resource.parent()));
				}
			}
		}

		@SuppressWarnings("unchecked")
		private void removeDeploymentReference(Map<String, Object> deployment, String systemUid) {
			Object deployed = deployment.get("deployedSystems");
			if (deployed instanceof List) {
				List<Object> retained = new ArrayList<>();
				for (Object item : (List<Object>) deployed) {
					if (!contains(item, systemUid)) {
						retained.add(item);
					}
				}
				deployment.put("deployedSystems", retained);
			}
			Object properties = deployment.get("properties");
			if (properties instanceof Map) {
				Map<String, Object> deploymentProperties = (Map<String, Object>) properties;
				Object links = deploymentProperties.get("deployedSystems@link");
				if (links instanceof List) {
					deploymentProperties.put("deployedSystems@link",
							((List<Object>) links).stream().filter(item -> !contains(item, systemUid)).toList());
				}
			}
		}

		@SuppressWarnings("unchecked")
		private void removeFirstDeploymentReference(Map<String, Object> deployment) {
			Object deployed = deployment.get("deployedSystems");
			if (deployed instanceof List && !((List<?>) deployed).isEmpty()) {
				deployment.put("deployedSystems",
						new ArrayList<>(((List<Object>) deployed).subList(1, ((List<?>) deployed).size())));
			}
			Object properties = deployment.get("properties");
			if (properties instanceof Map) {
				Map<String, Object> deploymentProperties = (Map<String, Object>) properties;
				Object links = deploymentProperties.get("deployedSystems@link");
				if (links instanceof List && !((List<?>) links).isEmpty()) {
					deploymentProperties.put("deployedSystems@link",
							new ArrayList<>(((List<Object>) links).subList(1, ((List<?>) links).size())));
				}
			}
		}

		private String conformance() throws IOException {
			List<String> values = new ArrayList<>();
			values.add(CONF_BASE + "create-replace-delete");
			values.add(CONF_BASE + "api-common");
			if (!this.nearMatchInheritanceOnly) {
				values.add("http://www.opengis.net/spec/ogcapi-4/1.0/conf/create-replace-delete");
			}
			values.add("http://www.opengis.net/spec/ogcapi-features-4/1.0/conf/create-replace-delete");
			values.add(CONF_BASE + "system");
			values.add(CONF_BASE + "subsystem");
			values.add(CONF_BASE + "deployment");
			values.add(CONF_BASE + "subdeployment");
			values.add(CONF_BASE + "procedure");
			values.add(CONF_BASE + "sf");
			values.add(CONF_BASE + "property");
			values.add(CONF_BASE + "geojson");
			values.add(CONF_BASE + "sensorml");
			return JSON.writeValueAsString(Map.of("conformsTo", values));
		}

		private String collections() throws IOException {
			List<Map<String, Object>> values = List.of(
					Map.of("id", "systems", "itemType", "feature", "featureType", "sosa:System"),
					Map.of("id", "custom_systems", "itemType", "feature", "featureType", "sosa:System"),
					Map.of("id", "custom_procedures", "itemType", "feature", "featureType", "sosa:Procedure"),
					Map.of("id", "custom_deployments", "itemType", "feature", "featureType", "sosa:Deployment"),
					Map.of("id", "custom_sampling", "itemType", "feature", "featureType", "sosa:Sample"),
					Map.of("id", "custom_properties", "itemType", "sosa:Property"));
			return JSON.writeValueAsString(Map.of("collections", values));
		}

		private void collection(HttpExchange exchange, String root) throws IOException {
			List<Map<String, Object>> values = this.resources.entrySet()
				.stream()
				.filter(entry -> entry.getKey().matches("/api/" + root + "/[^/]+"))
				.map(entry -> deepCopy(entry.getValue().body()))
				.toList();
			String accept = exchange.getRequestHeaders().getFirst("Accept");
			if (accept != null && accept.contains("application/geo+json")) {
				json(exchange, 200, Map.of("type", "FeatureCollection", "features", values), "application/geo+json");
			}
			else if (accept != null && accept.contains("application/sml+json")) {
				json(exchange, 200, Map.of("items", values), "application/sml+json");
			}
			else {
				json(exchange, 200, Map.of("items", values));
			}
		}

		private String rootFor(String path, Map<String, Object> body) {
			if (path.matches("/api/systems/[^/]+/subsystems")) {
				return "systems";
			}
			if (path.matches("/api/deployments/[^/]+/subdeployments")) {
				return "deployments";
			}
			if (path.matches("/api/systems/[^/]+/samplingFeatures")) {
				return "samplingFeatures";
			}
			if (path.startsWith("/api/collections/")) {
				String collection = path.split("/")[3];
				if (collection.contains("system")) {
					return "systems";
				}
				if (collection.contains("procedure")) {
					return "procedures";
				}
				if (collection.contains("deployment")) {
					return "deployments";
				}
				if (collection.contains("sampling")) {
					return "samplingFeatures";
				}
				return "properties";
			}
			String[] segments = path.split("/");
			return segments.length > 2 ? segments[2] : "";
		}

		private String parentFor(String path) {
			if (path.matches("/api/systems/[^/]+/(subsystems|samplingFeatures)")) {
				return path.substring(0, path.lastIndexOf('/'));
			}
			if (path.matches("/api/deployments/[^/]+/subdeployments")) {
				return path.substring(0, path.lastIndexOf('/'));
			}
			return null;
		}

		private String uid(Map<String, Object> body) {
			Object direct = body.get("uniqueId");
			if (direct instanceof String) {
				return (String) direct;
			}
			Object properties = body.get("properties");
			return properties instanceof Map && ((Map<?, ?>) properties).get("uid") instanceof String
					? (String) ((Map<?, ?>) properties).get("uid") : "";
		}

		private boolean contains(Object value, String expected) {
			if (value instanceof Map) {
				return ((Map<?, ?>) value).values().stream().anyMatch(item -> contains(item, expected));
			}
			if (value instanceof List) {
				return ((List<?>) value).stream().anyMatch(item -> contains(item, expected));
			}
			return expected.equals(value);
		}

		private String query(HttpExchange exchange, String name) {
			String query = exchange.getRequestURI().getRawQuery();
			if (query == null) {
				return null;
			}
			for (String parameter : query.split("&")) {
				String[] parts = parameter.split("=", 2);
				if (name.equals(parts[0])) {
					return parts.length == 2 ? parts[1] : "";
				}
			}
			return null;
		}

		private Map<String, Object> requestJson(HttpExchange exchange) throws IOException {
			return JSON.readValue(requestText(exchange), new TypeReference<Map<String, Object>>() {
			});
		}

		private String requestText(HttpExchange exchange) throws IOException {
			return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		}

		private Map<String, Object> deepCopy(Map<String, Object> value) {
			return JSON.convertValue(value, new TypeReference<Map<String, Object>>() {
			});
		}

		private Map<String, Object> bodyWithId(Map<String, Object> value, String id) {
			Map<String, Object> result = deepCopy(value);
			result.put("id", id);
			return result;
		}

		private String last(String path) {
			return path.substring(path.lastIndexOf('/') + 1);
		}

		private void json(HttpExchange exchange, int status, Object value) throws IOException {
			json(exchange, status, value, "application/json");
		}

		private void json(HttpExchange exchange, int status, Object value, String contentType) throws IOException {
			exchange.getResponseHeaders().set("Content-Type", contentType);
			respond(exchange, status, value instanceof String ? (String) value : JSON.writeValueAsString(value));
		}

		private void respond(HttpExchange exchange, int status, String body) throws IOException {
			byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
			if (status == 204) {
				exchange.sendResponseHeaders(status, -1);
			}
			else {
				exchange.sendResponseHeaders(status, bytes.length);
				exchange.getResponseBody().write(bytes);
			}
			exchange.close();
		}

		private record Resource(String root, Map<String, Object> body, String parent) {
		}

	}

}
