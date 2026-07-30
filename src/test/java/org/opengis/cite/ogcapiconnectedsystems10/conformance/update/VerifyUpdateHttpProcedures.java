package org.opengis.cite.ogcapiconnectedsystems10.conformance.update;

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
 * Controlled HTTP execution for the five released Update procedures.
 */
public class VerifyUpdateHttpProcedures {

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-DIRECT-HTTP-COVERAGE-001.
	 */
	@Test
	public void allFiveProceduresUpdateCanonicalAndPaginatedCustomResources() throws Exception {
		try (Fixture fixture = new Fixture()) {
			UpdateSupport support = fixture.support();

			support.systemsUpdate();
			support.deploymentsUpdate();
			support.proceduresUpdate();
			support.samplingFeaturesUpdate();
			support.propertiesUpdate();

			assertEquals(0, fixture.liveResources());
			assertTrue(fixture.calls("PATCH") >= 32);
			assertTrue(fixture.calls("OPTIONS") >= 16);
			assertTrue(fixture.collectionPageTwoCalls.get() >= 5);
			assertTrue(fixture.mergePatchCalls.get() > 0);
			assertTrue(fixture.jsonPatchCalls.get() > 0);
			assertEquals(0, fixture.acceptedConflictingIds.get());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-MUTATION-SAFETY-001.
	 */
	@Test
	public void missingMutationOptInPerformsNoWrite() throws Exception {
		try (Fixture fixture = new Fixture()) {
			UpdateSupport support = new UpdateSupport(fixture.apiRoot(), null, null);

			assertThrows(SkipException.class, support::systemsUpdate);
			assertEquals(0, fixture.writeCalls());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-DIRECT-PREREQUISITES-001.
	 */
	@Test
	public void inheritedUpdateNearMatchPerformsNoWrite() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.nearMatchInheritanceOnly = true;

			assertThrows(SkipException.class, fixture.support()::systemsUpdate);
			assertEquals(0, fixture.writeCalls());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-PATCH-NEGOTIATION-001.
	 */
	@Test
	public void exactOpenApiPatchContentCanSupplyPatchFormat() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.acceptPatch = null;
			fixture.openApiPatch = true;

			fixture.support().systemsUpdate();

			assertTrue(fixture.calls("PATCH") > 0);
			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-PATCH-NEGOTIATION-001.
	 */
	@Test
	public void noAdvertisedPatchDocumentSkipsBeforePatchAndCleans() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.acceptPatch = "application/json, application/geo+json";

			SkipException error = assertThrows(SkipException.class, fixture.support()::systemsUpdate);

			assertTrue(error.getMessage().contains("No PATCH request was issued"));
			assertEquals(0, fixture.calls("PATCH"));
			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-PARTIAL-UPDATE-001.
	 */
	@Test
	public void ignoredPatchCannotPassOnStatusAlone() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.ignorePatch = true;

			assertThrows(AssertionError.class, fixture.support()::systemsUpdate);
			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-PARTIAL-UPDATE-001.
	 */
	@Test
	public void acceptedConflictingIdentifierFailsAndCleans() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.acceptConflictingId = true;

			assertThrows(AssertionError.class, fixture.support()::systemsUpdate);
			assertTrue(fixture.acceptedConflictingIds.get() > 0);
			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-ASYNC-DEADLINE-001.
	 */
	@Test
	public void queuedPatchWaitsForJointPostconditions() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.queuedPatch = true;

			fixture.support(2_000L, 10L).systemsUpdate();

			assertTrue(fixture.queuedPatchCalls.get() > 0);
			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-CUSTOM-COLLECTIONS-001.
	 */
	@Test
	public void customPatchWithoutCanonicalPropagationFailsAndCleans() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.ignoreCustomPatch = true;

			assertThrows(AssertionError.class, fixture.support()::systemsUpdate);
			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-ASYNC-DEADLINE-001.
	 */
	@Test
	public void queuedPatchWithoutPostconditionFailsAtDeadlineAndCleans() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.queuedPatch = true;
			fixture.ignorePatch = true;

			assertThrows(AssertionError.class, () -> fixture.support(120L, 10L).systemsUpdate());
			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-PARTIAL-UPDATE-001.
	 */
	@Test
	public void http200StillRequiresObservablePostconditions() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.patchStatus = 200;
			fixture.ignorePatch = true;

			assertThrows(AssertionError.class, fixture.support()::systemsUpdate);
			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-FIXTURE-ACQUISITION-001.
	 */
	@Test
	public void deniedFixturePostSkipsBeforePatch() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.denyCreate = true;

			assertThrows(SkipException.class, fixture.support()::systemsUpdate);
			assertEquals(0, fixture.calls("PATCH"));
			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-FIXTURE-ACQUISITION-001.
	 */
	@Test
	public void ambiguousFixturePostResponseStillCleansCommittedResource() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.ambiguousCreateResponse = true;

			assertThrows(SkipException.class, fixture.support()::systemsUpdate);
			assertEquals(0, fixture.calls("PATCH"));
			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-CLEANUP-001.
	 */
	@Test
	public void ignoredHttp204DeleteFailsCleanupProof() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.ignoreDelete = true;

			assertThrows(AssertionError.class, fixture.support()::systemsUpdate);
			assertTrue(fixture.liveResources() > 0);
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-CLEANUP-001.
	 */
	@Test
	public void changedIdentityImmediatelyBeforeCleanupPreventsDelete() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.changeIdentityBeforeCleanup = true;

			assertThrows(AssertionError.class, fixture.support()::systemsUpdate);
			assertEquals(0, fixture.changedIdentityDeleteCalls.get());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-APPLICABILITY-EXACT-001.
	 */
	@Test
	public void unrelatedTypeWithSystemSuffixReceivesNoCustomWrite() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.unrelatedSystemType = true;

			fixture.support().systemsUpdate();

			assertEquals(4, fixture.calls("PATCH"));
			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-CUSTOM-COLLECTIONS-001.
	 */
	@Test
	public void transientSynchronousCustomPropagationCannotPass() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.revertAfterCustomRead = true;

			assertThrows(AssertionError.class, fixture.support()::systemsUpdate);
			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-PATCH-NEGOTIATION-001.
	 */
	@Test
	public void repeatedAllowFieldsAreCombinedForPatchDiscovery() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.repeatedAllow = true;

			fixture.support().systemsUpdate();

			assertTrue(fixture.calls("PATCH") > 0);
			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-FIXTURE-ACQUISITION-001.
	 */
	@Test
	public void delayedAmbiguousFixtureCommitIsDiscoveredAndCleaned() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.delayedAmbiguousCreate = true;

			assertThrows(SkipException.class, fixture.support()::systemsUpdate);
			TimeUnit.MILLISECONDS.sleep(100);
			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-FIXTURE-ACQUISITION-001.
	 */
	@Test
	public void customOnlyAmbiguousFixtureCommitIsDiscoveredAndCleaned() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.ambiguousCustomOnlyCreate = true;

			assertThrows(SkipException.class, fixture.support()::systemsUpdate);
			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-CLEANUP-001.
	 */
	@Test
	public void occurrenceCleanupFailureDoesNotSuppressCanonicalCleanup() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.ignoreOccurrenceDelete = true;

			assertThrows(AssertionError.class, fixture.support()::systemsUpdate);
			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-CUSTOM-COLLECTIONS-001.
	 */
	@Test
	public void occurrenceUsesItsOwnSentinelBaseline() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.distinctOccurrenceSentinel = true;

			fixture.support().systemsUpdate();

			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-FIXTURE-ACQUISITION-001.
	 */
	@Test
	public void geoJsonFeaturesCollectionSupportsAmbiguousRediscovery() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.geoJsonFeatureCollections = true;
			fixture.ambiguousCreateResponse = true;

			assertThrows(SkipException.class, fixture.support()::systemsUpdate);
			assertEquals(0, fixture.calls("PATCH"));
			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-CLEANUP-001.
	 */
	@Test
	public void canonicalDiscoveryFailureDoesNotSuppressCustomCleanup() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.ambiguousCustomOnlyCreate = true;
			fixture.failCanonicalDiscovery = true;

			assertThrows(AssertionError.class, fixture.support()::systemsUpdate);
			assertEquals(fixture.resourcePaths(), 0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-CLEANUP-001.
	 */
	@Test
	public void customDiscoveryFailureDoesNotSuppressCanonicalCleanup() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.ambiguousCustomCreateResponse = true;
			fixture.failCustomDiscovery = true;

			assertThrows(AssertionError.class, fixture.support()::systemsUpdate);
			assertEquals(fixture.resourcePaths(), 0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-FIXTURE-ACQUISITION-001.
	 */
	@Test
	public void accepted202WithoutOwnedResourceSkipsBeforePatch() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.acceptedCreateWithoutCommit = true;

			assertThrows(SkipException.class, fixture.support()::systemsUpdate);
			assertEquals(0, fixture.calls("PATCH"));
			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-FIXTURE-ACQUISITION-001.
	 */
	@Test
	public void http201WithoutLocationSkipsBeforePatchAndCleans() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.missingCreateLocation = true;

			assertThrows(SkipException.class, fixture.support()::systemsUpdate);
			assertEquals(0, fixture.calls("PATCH"));
			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-FIXTURE-ACQUISITION-001.
	 */
	@Test
	public void crossOriginCreateLocationSkipsBeforePatchAndCleans() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.crossOriginCreateLocation = true;

			assertThrows(SkipException.class, fixture.support()::systemsUpdate);
			assertEquals(0, fixture.calls("PATCH"));
			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-FIXTURE-ACQUISITION-001.
	 */
	@Test
	public void samplingFeatureFixtureUsesRequiredSystemScopedCreation() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.rejectRootSamplingFeatureCreate = true;

			try {
				fixture.support().samplingFeaturesUpdate();
			}
			catch (SkipException ex) {
				throw new AssertionError("required System-scoped Sampling Feature creation was not used", ex);
			}

			assertEquals(0, fixture.rootSamplingFeatureCreateCalls.get());
			assertTrue(fixture.systemScopedSamplingFeatureCreateCalls.get() > 0);
			assertEquals(0, fixture.liveResources());
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-CLEANUP-001;
	 * SCENARIO-ETS-PART1-011-FIXTURE-ACQUISITION-001.
	 */
	@Test
	public void canonicalFirstCustomDelayedAmbiguousCommitIsDiscoveredAndCleaned() throws Exception {
		try (Fixture fixture = new Fixture()) {
			fixture.canonicalFirstCustomDelayedCreate = true;

			assertThrows(SkipException.class, fixture.support()::systemsUpdate);
			TimeUnit.MILLISECONDS.sleep(100);
			assertEquals(fixture.resourcePaths(), 0, fixture.liveResources());
		}
	}

	private static final class Fixture implements AutoCloseable {

		private static final ObjectMapper JSON = new ObjectMapper();

		private static final List<String> KINDS = List.of("systems", "deployments", "procedures", "samplingFeatures",
				"properties");

		private final HttpServer server;

		private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

		private final Map<String, Map<String, Object>> resources = new ConcurrentHashMap<>();

		private final Map<String, String> aliases = new ConcurrentHashMap<>();

		private final Map<String, Map<String, Object>> originalResources = new ConcurrentHashMap<>();

		private final Map<String, AtomicInteger> methodCalls = new ConcurrentHashMap<>();

		private final AtomicInteger ids = new AtomicInteger();

		private final AtomicInteger collectionPageTwoCalls = new AtomicInteger();

		private final AtomicInteger mergePatchCalls = new AtomicInteger();

		private final AtomicInteger jsonPatchCalls = new AtomicInteger();

		private final AtomicInteger queuedPatchCalls = new AtomicInteger();

		private final AtomicInteger acceptedConflictingIds = new AtomicInteger();

		private final AtomicInteger finalPatchReads = new AtomicInteger();

		private final AtomicInteger changedIdentityDeleteCalls = new AtomicInteger();

		private final AtomicInteger rootSamplingFeatureCreateCalls = new AtomicInteger();

		private final AtomicInteger systemScopedSamplingFeatureCreateCalls = new AtomicInteger();

		private volatile String acceptPatch = "application/merge-patch+json, application/json-patch+json";

		private volatile boolean openApiPatch;

		private volatile boolean nearMatchInheritanceOnly;

		private volatile boolean ignorePatch;

		private volatile boolean ignoreCustomPatch;

		private volatile boolean acceptConflictingId;

		private volatile boolean queuedPatch;

		private volatile boolean denyCreate;

		private volatile boolean ambiguousCreateResponse;

		private volatile boolean ignoreDelete;

		private volatile boolean changeIdentityBeforeCleanup;

		private volatile boolean unrelatedSystemType;

		private volatile boolean revertAfterCustomRead;

		private volatile boolean repeatedAllow;

		private volatile boolean delayedAmbiguousCreate;

		private volatile boolean ambiguousCustomOnlyCreate;

		private volatile boolean ambiguousCustomCreateResponse;

		private volatile boolean ignoreOccurrenceDelete;

		private volatile boolean distinctOccurrenceSentinel;

		private volatile boolean geoJsonFeatureCollections;

		private volatile boolean failCanonicalDiscovery;

		private volatile boolean failCustomDiscovery;

		private volatile boolean acceptedCreateWithoutCommit;

		private volatile boolean missingCreateLocation;

		private volatile boolean crossOriginCreateLocation;

		private volatile boolean rejectRootSamplingFeatureCreate;

		private volatile boolean canonicalFirstCustomDelayedCreate;

		private volatile boolean cleanupIdentityChanged;

		private volatile String changedIdentityCanonical;

		private volatile int patchStatus = 204;

		private Fixture() throws IOException {
			this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
			this.server.createContext("/api", this::handle);
			this.server.setExecutor(Executors.newCachedThreadPool());
			this.server.start();
		}

		private URI apiRoot() {
			return URI.create("http://127.0.0.1:" + this.server.getAddress().getPort() + "/api/");
		}

		private UpdateSupport support() {
			return support(1_000L, 10L);
		}

		private UpdateSupport support(long timeout, long poll) {
			return new UpdateSupport(apiRoot(), "true", "dedicated-mutable-iut", timeout, poll);
		}

		private int calls(String method) {
			return this.methodCalls.getOrDefault(method, new AtomicInteger()).get();
		}

		private int writeCalls() {
			return calls("POST") + calls("PATCH") + calls("DELETE");
		}

		private int liveResources() {
			return this.resources.size();
		}

		private String resourcePaths() {
			return this.resources.keySet().toString();
		}

		private void handle(HttpExchange exchange) throws IOException {
			String method = exchange.getRequestMethod();
			String path = exchange.getRequestURI().getPath();
			this.methodCalls.computeIfAbsent(method, ignored -> new AtomicInteger()).incrementAndGet();
			try {
				if ("GET".equals(method) && "/api/".equals(path)) {
					landing(exchange);
				}
				else if ("GET".equals(method) && "/api/conformance".equals(path)) {
					conformance(exchange);
				}
				else if ("GET".equals(method) && "/api/openapi".equals(path)) {
					openApi(exchange);
				}
				else if ("GET".equals(method) && "/api/collections".equals(path)) {
					collections(exchange);
				}
				else if ("POST".equals(method) && collectionKind(path) != null) {
					create(exchange, path);
				}
				else if ("GET".equals(method) && isCanonicalCollection(path)) {
					canonicalCollection(exchange, path);
				}
				else if ("GET".equals(method) && isCustomCollection(path)) {
					customCollection(exchange, path);
				}
				else if ("GET".equals(method) && canonicalPath(path) != null) {
					resource(exchange, path);
				}
				else if ("OPTIONS".equals(method) && canonicalPath(path) != null) {
					options(exchange);
				}
				else if ("PATCH".equals(method) && canonicalPath(path) != null) {
					patch(exchange, path);
				}
				else if ("DELETE".equals(method) && canonicalPath(path) != null) {
					delete(exchange, path);
				}
				else {
					send(exchange, 404, "application/json", Map.of("error", "not found"));
				}
			}
			catch (RuntimeException ex) {
				send(exchange, 500, "application/json", Map.of("error", ex.getMessage()));
			}
		}

		private void landing(HttpExchange exchange) throws IOException {
			List<Map<String, Object>> links = this.openApiPatch ? List
				.of(Map.of("rel", "service-desc", "href", "/api/openapi", "type", "application/vnd.oai.openapi+json"))
					: List.of();
			send(exchange, 200, "application/json", Map.of("links", links));
		}

		private void conformance(HttpExchange exchange) throws IOException {
			List<String> declarations = new ArrayList<>(
					List.of("http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/update",
							"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/api-common",
							"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/system",
							"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/deployment",
							"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/procedure",
							"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/sf",
							"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/property",
							"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/geojson",
							"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/sensorml"));
			declarations
				.add(this.nearMatchInheritanceOnly ? "http://www.opengis.net/spec/ogcapi-features-4/1.0/conf/update"
						: "http://www.opengis.net/spec/ogcapi-4/1.0/conf/update");
			send(exchange, 200, "application/json", Map.of("conformsTo", declarations));
		}

		private void openApi(HttpExchange exchange) throws IOException {
			Map<String, Object> content = Map.of("application/merge-patch+json", Map.of(),
					"application/json-patch+json", Map.of());
			Map<String, Object> patch = Map.of("requestBody", Map.of("content", content), "responses",
					Map.of("204", Map.of("description", "updated")));
			Map<String, Object> paths = new LinkedHashMap<>();
			for (String kind : KINDS) {
				paths.put("/" + kind + "/{resourceId}", Map.of("patch", patch));
			}
			paths.put("/collections/{collectionId}/items/{resourceId}", Map.of("patch", patch));
			send(exchange, 200, "application/vnd.oai.openapi+json",
					Map.of("openapi", "3.0.3", "info", Map.of("title", "fixture", "version", "1"), "paths", paths));
		}

		private void collections(HttpExchange exchange) throws IOException {
			boolean second = "page=2".equals(exchange.getRequestURI().getQuery());
			if (second) {
				this.collectionPageTwoCalls.incrementAndGet();
			}
			List<String> kinds = second ? KINDS.subList(3, 5) : KINDS.subList(0, 3);
			List<Map<String, Object>> values = kinds.stream().map(this::collection).toList();
			Map<String, Object> body = new LinkedHashMap<>();
			body.put("collections", values);
			if (!second) {
				body.put("links", List.of(Map.of("rel", "next", "href", "/api/collections?page=2")));
			}
			send(exchange, 200, "application/json", body);
		}

		private Map<String, Object> collection(String kind) {
			Map<String, Object> result = new LinkedHashMap<>();
			result.put("id", "custom-" + kind);
			if ("properties".equals(kind)) {
				result.put("itemType", "sosa:Property");
			}
			else {
				result.put("itemType", "feature");
				result.put("featureType", switch (kind) {
					case "systems" -> this.unrelatedSystemType ? "https://example.test/vocab/System" : "sosa:System";
					case "deployments" -> "sosa:Deployment";
					case "procedures" -> "sosa:Procedure";
					default -> "sosa:Sample";
				});
			}
			result.put("links", List.of(Map.of("rel", "items", "href", "/api/collections/custom-" + kind + "/items")));
			return result;
		}

		private void create(HttpExchange exchange, String path) throws IOException {
			if ("/api/samplingFeatures".equals(path)) {
				this.rootSamplingFeatureCreateCalls.incrementAndGet();
				if (this.rejectRootSamplingFeatureCreate) {
					send(exchange, 405, "application/json", Map.of("error", "root creation is not supported"));
					return;
				}
			}
			if (path.matches("/api/systems/[^/]+/samplingFeatures")) {
				this.systemScopedSamplingFeatureCreateCalls.incrementAndGet();
			}
			if (this.denyCreate) {
				send(exchange, 403, "application/json", Map.of("error", "fixture creation denied"));
				return;
			}
			if (this.acceptedCreateWithoutCommit) {
				send(exchange, 202, "application/json", Map.of());
				return;
			}
			String kind = collectionKind(path);
			String id = Integer.toString(this.ids.incrementAndGet());
			String canonical = "/api/" + kind + "/" + id;
			Map<String, Object> body = objectBody(exchange);
			body.put("id", id);
			if (this.canonicalFirstCustomDelayedCreate && path.startsWith("/api/collections/")) {
				String occurrence = path + "/" + id;
				this.resources.put(canonical, body);
				this.originalResources.put(canonical, copy(body));
				this.scheduler.schedule(() -> {
					this.resources.put(occurrence, copy(body));
					this.originalResources.put(occurrence, copy(body));
				}, 25, TimeUnit.MILLISECONDS);
				send(exchange, 500, "application/json", Map.of("error", "delayed custom propagation"));
				return;
			}
			if (this.ambiguousCustomOnlyCreate && path.startsWith("/api/collections/")) {
				String occurrence = path + "/" + id;
				this.resources.put(occurrence, body);
				this.originalResources.put(occurrence, copy(body));
				send(exchange, 500, "application/json", Map.of("error", "custom response unavailable"));
				return;
			}
			if (this.delayedAmbiguousCreate && !path.startsWith("/api/collections/")) {
				this.scheduler.schedule(() -> {
					this.resources.put(canonical, body);
					this.originalResources.put(canonical, copy(body));
				}, 25, TimeUnit.MILLISECONDS);
				send(exchange, 500, "application/json", Map.of("error", "delayed response unavailable"));
				return;
			}
			this.resources.put(canonical, body);
			this.originalResources.put(canonical, copy(body));
			String location = canonical;
			if (path.startsWith("/api/collections/")) {
				location = path + "/" + id;
				this.aliases.put(location, canonical);
			}
			if (this.ambiguousCreateResponse
					|| this.ambiguousCustomCreateResponse && path.startsWith("/api/collections/")) {
				send(exchange, 500, "application/json", Map.of("error", "response unavailable"));
				return;
			}
			if (this.crossOriginCreateLocation) {
				exchange.getResponseHeaders().set("Location", "https://other.test/api/" + kind + "/" + id);
			}
			else if (!this.missingCreateLocation) {
				exchange.getResponseHeaders().set("Location", location);
			}
			send(exchange, 201, "application/json", Map.of("id", id));
		}

		private void canonicalCollection(HttpExchange exchange, String path) throws IOException {
			if (this.failCanonicalDiscovery) {
				send(exchange, 500, "application/json", Map.of("error", "canonical discovery unavailable"));
				return;
			}
			List<Map<String, Object>> items = this.resources.entrySet()
				.stream()
				.filter(entry -> entry.getKey().startsWith(path + "/"))
				.map(entry -> copy(entry.getValue()))
				.toList();
			String member = this.geoJsonFeatureCollections ? "features" : "items";
			String contentType = this.geoJsonFeatureCollections ? "application/geo+json" : "application/json";
			send(exchange, 200, contentType, Map.of(member, items));
		}

		private void customCollection(HttpExchange exchange, String path) throws IOException {
			if (this.failCustomDiscovery) {
				send(exchange, 500, "application/json", Map.of("error", "custom discovery unavailable"));
				return;
			}
			List<Map<String, Object>> items = this.resources.entrySet()
				.stream()
				.filter(entry -> entry.getKey().startsWith(path + "/"))
				.map(entry -> copy(entry.getValue()))
				.toList();
			String member = this.geoJsonFeatureCollections ? "features" : "items";
			String contentType = this.geoJsonFeatureCollections ? "application/geo+json" : "application/json";
			send(exchange, 200, contentType, Map.of(member, items));
		}

		private void resource(HttpExchange exchange, String path) throws IOException {
			String canonical = canonicalPath(path);
			Map<String, Object> body = this.resources.get(canonical);
			if (body == null) {
				send(exchange, 404, "application/json", Map.of("error", "not found"));
				return;
			}
			if (this.changeIdentityBeforeCleanup && !this.cleanupIdentityChanged && calls("PATCH") >= 8
					&& this.finalPatchReads.incrementAndGet() > 5) {
				body.put("uniqueId", "urn:changed-before-cleanup");
				Object properties = body.get("properties");
				if (properties instanceof Map<?, ?> values) {
					@SuppressWarnings("unchecked")
					Map<String, Object> mutable = (Map<String, Object>) values;
					mutable.put("uid", "urn:changed-before-cleanup");
				}
				this.cleanupIdentityChanged = true;
				this.changedIdentityCanonical = canonical;
			}
			Map<String, Object> response = copy(body);
			if (this.distinctOccurrenceSentinel && path.startsWith("/api/collections/")) {
				if (response.containsKey("properties")) {
					@SuppressWarnings("unchecked")
					Map<String, Object> properties = (Map<String, Object>) response.get("properties");
					properties.put("name", "Custom occurrence sentinel");
				}
				else {
					response.put("label", "Custom occurrence sentinel");
				}
			}
			send(exchange, 200, mediaType(response), response);
			if (this.revertAfterCustomRead && path.startsWith("/api/collections/") && this.jsonPatchCalls.get() > 0) {
				this.resources.put(canonical, copy(this.originalResources.get(canonical)));
			}
		}

		private void options(HttpExchange exchange) throws IOException {
			if (this.repeatedAllow) {
				exchange.getResponseHeaders().add("Allow", "GET");
				exchange.getResponseHeaders().add("Allow", "PATCH, DELETE, OPTIONS");
			}
			else {
				exchange.getResponseHeaders().set("Allow", "GET, PATCH, DELETE, OPTIONS");
			}
			if (this.acceptPatch != null) {
				exchange.getResponseHeaders().set("Accept-Patch", this.acceptPatch);
			}
			send(exchange, 200, "application/json", Map.of());
		}

		private void patch(HttpExchange exchange, String path) throws IOException {
			String canonical = canonicalPath(path);
			Map<String, Object> current = this.resources.get(canonical);
			if (current == null) {
				send(exchange, 404, "application/json", Map.of("error", "not found"));
				return;
			}
			String type = exchange.getRequestHeaders().getFirst("Content-Type");
			Object patch = JSON.readValue(exchange.getRequestBody(), Object.class);
			Runnable mutation = () -> {
				if (!this.ignorePatch && !(this.ignoreCustomPatch && path.startsWith("/api/collections/"))) {
					applyPatch(current, type, patch);
				}
			};
			if (this.queuedPatch) {
				this.queuedPatchCalls.incrementAndGet();
				this.scheduler.schedule(mutation, 25, TimeUnit.MILLISECONDS);
				send(exchange, 202, "application/json", Map.of());
			}
			else {
				mutation.run();
				send(exchange, this.patchStatus, this.patchStatus == 200 ? mediaType(current) : null,
						this.patchStatus == 200 ? current : null);
			}
		}

		private void applyPatch(Map<String, Object> current, String contentType, Object patch) {
			if (contentType.startsWith("application/merge-patch+json")) {
				this.mergePatchCalls.incrementAndGet();
				@SuppressWarnings("unchecked")
				Map<String, Object> merge = (Map<String, Object>) patch;
				applyId(current, merge.get("id"));
				if (merge.containsKey("properties")) {
					@SuppressWarnings("unchecked")
					Map<String, Object> properties = (Map<String, Object>) current.get("properties");
					@SuppressWarnings("unchecked")
					Map<String, Object> submitted = (Map<String, Object>) merge.get("properties");
					properties.put("description", submitted.get("description"));
				}
				else {
					current.put("description", merge.get("description"));
				}
				return;
			}
			this.jsonPatchCalls.incrementAndGet();
			for (Object value : (List<?>) patch) {
				@SuppressWarnings("unchecked")
				Map<String, Object> operation = (Map<String, Object>) value;
				if ("/id".equals(operation.get("path"))) {
					applyId(current, operation.get("value"));
				}
				else if ("/properties/description".equals(operation.get("path"))) {
					@SuppressWarnings("unchecked")
					Map<String, Object> properties = (Map<String, Object>) current.get("properties");
					properties.put("description", operation.get("value"));
				}
				else if ("/description".equals(operation.get("path"))) {
					current.put("description", operation.get("value"));
				}
			}
		}

		private void applyId(Map<String, Object> current, Object submitted) {
			if (this.acceptConflictingId) {
				current.put("id", submitted);
				this.acceptedConflictingIds.incrementAndGet();
			}
		}

		private void delete(HttpExchange exchange, String path) throws IOException {
			if (canonicalPath(path) != null && canonicalPath(path).equals(this.changedIdentityCanonical)) {
				this.changedIdentityDeleteCalls.incrementAndGet();
			}
			if (this.ignoreDelete) {
				send(exchange, 204, null, null);
				return;
			}
			if (this.ignoreOccurrenceDelete && path.startsWith("/api/collections/")) {
				send(exchange, 204, null, null);
				return;
			}
			if (this.aliases.containsKey(path)) {
				this.aliases.remove(path);
			}
			else {
				this.resources.remove(path);
				this.originalResources.remove(path);
				this.aliases.entrySet().removeIf(entry -> path.equals(entry.getValue()));
			}
			send(exchange, 204, null, null);
		}

		private String collectionKind(String path) {
			if (path.matches("/api/systems/[^/]+/samplingFeatures")) {
				return "samplingFeatures";
			}
			for (String kind : KINDS) {
				if (("/api/" + kind).equals(path) || ("/api/collections/custom-" + kind + "/items").equals(path)) {
					return kind;
				}
			}
			return null;
		}

		private boolean isCanonicalCollection(String path) {
			return KINDS.stream().anyMatch(kind -> ("/api/" + kind).equals(path));
		}

		private boolean isCustomCollection(String path) {
			return KINDS.stream().anyMatch(kind -> ("/api/collections/custom-" + kind + "/items").equals(path));
		}

		private String canonicalPath(String path) {
			String alias = this.aliases.get(path);
			if (alias != null) {
				return alias;
			}
			if (this.resources.containsKey(path)) {
				return path;
			}
			for (String kind : KINDS) {
				if (path.matches("/api/" + kind + "/[^/]+")) {
					return path;
				}
			}
			return null;
		}

		private Map<String, Object> objectBody(HttpExchange exchange) throws IOException {
			return JSON.readValue(exchange.getRequestBody(), new TypeReference<Map<String, Object>>() {
			});
		}

		private static Map<String, Object> copy(Map<String, Object> body) {
			return JSON.convertValue(body, new TypeReference<Map<String, Object>>() {
			});
		}

		private static String mediaType(Map<String, Object> body) {
			return body.containsKey("properties") ? "application/geo+json" : "application/sml+json";
		}

		private static void send(HttpExchange exchange, int status, String contentType, Object body)
				throws IOException {
			byte[] bytes = body == null ? new byte[0] : JSON.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
			if (contentType != null) {
				exchange.getResponseHeaders().set("Content-Type", contentType);
			}
			exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
			if (bytes.length > 0) {
				exchange.getResponseBody().write(bytes);
			}
			exchange.close();
		}

		@Override
		public void close() {
			this.server.stop(0);
			this.scheduler.shutdownNow();
		}

	}

}
