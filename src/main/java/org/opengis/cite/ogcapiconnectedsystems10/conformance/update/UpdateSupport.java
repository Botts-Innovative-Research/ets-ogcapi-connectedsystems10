package org.opengis.cite.ogcapiconnectedsystems10.conformance.update;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;

import com.reprezen.kaizen.oasparser.model3.Operation;
import com.reprezen.kaizen.oasparser.model3.Path;
import com.reprezen.kaizen.oasparser.model3.RequestBody;
import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.EncodingMediatypeWrite;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.createreplacedelete.CreateReplaceDeleteSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.geojson.GeoJsonSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.geojson.GeoJsonSupport.ApiDefinition;
import org.testng.Reporter;
import org.testng.SkipException;

import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Stateful, per-procedure support for the five released Part 1 Update abstract tests.
 */
public final class UpdateSupport {

	private static final String CONF_BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/";

	private static final String CONF_UPDATE = CONF_BASE + "update";

	private static final String CONF_API_COMMON = CONF_BASE + "api-common";

	static final String CONF_INHERITED_UPDATE = "http://www.opengis.net/spec/ogcapi-4/1.0/conf/update";

	private static final String CONF_GEOJSON = CONF_BASE + "geojson";

	private static final String CONF_SENSORML = CONF_BASE + "sensorml";

	private static final String REQ_BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/update/";

	private static final String GEOJSON = "application/geo+json";

	private static final String SENSORML = "application/sml+json";

	private static final String MERGE_PATCH = "application/merge-patch+json";

	private static final String JSON_PATCH = "application/json-patch+json";

	private static final List<String> PATCH_PREFERENCE = List.of(MERGE_PATCH, JSON_PATCH);

	private static final String ASYNC_TIMEOUT_PROPERTY = "org.opengis.cite.ogcapiconnectedsystems10.update.asyncTimeoutMillis";

	private static final String ASYNC_POLL_PROPERTY = "org.opengis.cite.ogcapiconnectedsystems10.update.asyncPollMillis";

	private static final long DEFAULT_ASYNC_TIMEOUT_MILLIS = 10_000L;

	private static final long DEFAULT_ASYNC_POLL_MILLIS = 100L;

	private static final int MAX_DISCOVERY_PAGES = 100;

	private static final ResourceKind SYSTEM = new ResourceKind("System", "systems", "system", CONF_BASE + "system",
			"sosa:System", false);

	private static final ResourceKind DEPLOYMENT = new ResourceKind("Deployment", "deployments", "deployment",
			CONF_BASE + "deployment", "sosa:Deployment", false);

	private static final ResourceKind PROCEDURE = new ResourceKind("Procedure", "procedures", "procedure",
			CONF_BASE + "procedure", "sosa:Procedure", false);

	private static final ResourceKind SAMPLING_FEATURE = new ResourceKind("Sampling Feature", "samplingFeatures",
			"sampling-feature", CONF_BASE + "sf", "sosa:Sample", false);

	private static final ResourceKind PROPERTY = new ResourceKind("Property", "properties", "property",
			CONF_BASE + "property", "sosa:Property", true);

	private final URI apiRoot;

	private final String mutationTestsEnabled;

	private final String mutationIutPolicy;

	private final long asyncTimeoutMillis;

	private final long asyncPollMillis;

	private final LongSupplier nanoTime;

	/**
	 * Creates isolated support for one independently executable procedure.
	 * @param apiRoot normalized API root.
	 * @param mutationTestsEnabled explicit mutation flag.
	 * @param mutationIutPolicy explicit ownership policy.
	 */
	public UpdateSupport(URI apiRoot, String mutationTestsEnabled, String mutationIutPolicy) {
		this(apiRoot, mutationTestsEnabled, mutationIutPolicy,
				positiveLongProperty(ASYNC_TIMEOUT_PROPERTY, DEFAULT_ASYNC_TIMEOUT_MILLIS),
				positiveLongProperty(ASYNC_POLL_PROPERTY, DEFAULT_ASYNC_POLL_MILLIS));
	}

	UpdateSupport(URI apiRoot, String mutationTestsEnabled, String mutationIutPolicy, long asyncTimeoutMillis,
			long asyncPollMillis) {
		this(apiRoot, mutationTestsEnabled, mutationIutPolicy, asyncTimeoutMillis, asyncPollMillis, System::nanoTime);
	}

	UpdateSupport(URI apiRoot, String mutationTestsEnabled, String mutationIutPolicy, long asyncTimeoutMillis,
			long asyncPollMillis, LongSupplier nanoTime) {
		if (apiRoot == null || !apiRoot.isAbsolute()) {
			throw new IllegalArgumentException("apiRoot must be absolute");
		}
		if (asyncTimeoutMillis <= 0 || asyncPollMillis <= 0) {
			throw new IllegalArgumentException("asynchronous timeout and polling interval must be positive");
		}
		String value = apiRoot.toString();
		this.apiRoot = URI.create(value.endsWith("/") ? value : value + "/");
		this.mutationTestsEnabled = mutationTestsEnabled;
		this.mutationIutPolicy = mutationIutPolicy;
		this.asyncTimeoutMillis = asyncTimeoutMillis;
		this.asyncPollMillis = asyncPollMillis;
		this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
	}

	/**
	 * Executes released procedure {@code /conf/update/system}.
	 */
	public void systemsUpdate() {
		execute(SYSTEM);
	}

	/**
	 * Executes released procedure {@code /conf/update/deployment}.
	 */
	public void deploymentsUpdate() {
		execute(DEPLOYMENT);
	}

	/**
	 * Executes released procedure {@code /conf/update/procedure}.
	 */
	public void proceduresUpdate() {
		execute(PROCEDURE);
	}

	/**
	 * Executes released procedure {@code /conf/update/sampling-feature}.
	 */
	public void samplingFeaturesUpdate() {
		execute(SAMPLING_FEATURE);
	}

	/**
	 * Executes released procedure {@code /conf/update/property}.
	 */
	public void propertiesUpdate() {
		execute(PROPERTY);
	}

	private void execute(ResourceKind kind) {
		String requirement = REQ_BASE + kind.requirement();
		Map<String, Object> conformance = prepare(kind, requirement);
		List<String> mediaTypes = resourceMediaTypes(kind, conformance, requirement);
		ApiDefinition definition = apiDefinition(requirement);
		List<CustomEndpoint> customEndpoints = customEndpoints(kind, requirement);
		executeWithCleanup(requirement, cleanup -> {
			for (String mediaType : mediaTypes) {
				updateOwned(kind, this.apiRoot.resolve(kind.path()), null, mediaType, definition, requirement, cleanup);
			}
			for (CustomEndpoint endpoint : customEndpoints) {
				for (String mediaType : mediaTypes) {
					updateOwned(kind, endpoint.items(), endpoint, mediaType, definition, requirement, cleanup);
				}
			}
		});
	}

	private Map<String, Object> prepare(ResourceKind kind, String requirement) {
		URI endpoint = this.apiRoot.resolve("conformance");
		Response response = get(endpoint, "application/json");
		ETSAssert.assertStatus(response, 200, requirement);
		Map<String, Object> body = parseJsonObject(response, endpoint, requirement);
		requireDeclaration(body, CONF_UPDATE, requirement, "IUT does not declare the Part 1 Update conformance class.");
		requireDeclaration(body, CONF_API_COMMON, requirement,
				"IUT does not declare the direct Part 1 API Common prerequisite.");
		requireDeclaration(body, CONF_INHERITED_UPDATE, requirement,
				"IUT does not declare the exact inherited OGC API Features Part 4 Update class.");
		requireDeclaration(body, kind.condition(), requirement,
				"conditional " + kind.name() + " conformance class is not declared.");
		CreateReplaceDeleteSupport.ensureMutationAllowed(this.apiRoot, this.mutationTestsEnabled,
				this.mutationIutPolicy, requirement);
		return body;
	}

	private void updateOwned(ResourceKind kind, URI createEndpoint, CustomEndpoint custom, String mediaType,
			ApiDefinition definition, String requirement, CleanupStack cleanup) {
		String identity = uid(kind.path());
		Map<String, Object> createBody = CreateReplaceDeleteSupport.generatedBody(kind.path(), mediaType,
				"update-create", identity);
		CreateReplaceDeleteSupport.validateGeneratedBody(createBody, kind.path(), mediaType, requirement);
		CleanupTarget target = new CleanupTarget(kind, mediaType, identity, custom == null ? null : custom.items());
		cleanup.push(kind.name() + " identity " + identity, () -> cleanup(target, requirement));

		Response create;
		target.postDispatched = true;
		try {
			create = EncodingMediatypeWrite.givenWithoutDefaultCharset()
				.accept(mediaType)
				.contentType(mediaType)
				.body(createBody)
				.post(createEndpoint)
				.andReturn();
		}
		catch (RuntimeException ex) {
			throw new SkipException(requirement + " - fixture POST " + createEndpoint
					+ " returned no usable response; Update evidence is inconclusive and no PATCH request was issued.",
					ex);
		}
		if (!List.of(201, 202).contains(create.getStatusCode())) {
			throw new SkipException(requirement + " - fixture POST " + createEndpoint + " returned HTTP "
					+ create.getStatusCode() + "; Update evidence is inconclusive and no PATCH request was issued.");
		}
		target.accepted = true;

		ResourceUris uris = createdUris(kind, createEndpoint, custom, create, target, requirement);
		target.canonical = uris.canonical();
		target.occurrence = uris.occurrence();
		ResourceBaselines baselines = observeBaselines(kind, uris, mediaType, identity, requirement);

		for (String patchMediaType : patchMediaTypes(uris.updateTarget(), definition, requirement)) {
			exercisePatch(kind, uris, mediaType, patchMediaType, baselines, identity, requirement);
			baselines = observeBaselines(kind, uris, mediaType, identity, requirement);
		}
	}

	private ResourceBaselines observeBaselines(ResourceKind kind, ResourceUris uris, String mediaType, String identity,
			String requirement) {
		Observation canonical = observe(kind, uris.canonical(), mediaType, requirement, null);
		assertIdentity(canonical.body(), identity, uris.canonical(), requirement);
		Observation occurrence = null;
		if (uris.occurrence() != null) {
			occurrence = observe(kind, uris.occurrence(), mediaType, requirement, null);
			assertIdentity(occurrence.body(), identity, uris.occurrence(), requirement);
		}
		return new ResourceBaselines(canonical, occurrence);
	}

	private ResourceUris createdUris(ResourceKind kind, URI createEndpoint, CustomEndpoint custom, Response create,
			CleanupTarget target, String requirement) {
		if (create.getStatusCode() == 202) {
			Deadline deadline = new Deadline();
			Optional<URI> canonical = awaitOwned(kind, target.identity, target.mediaType, deadline, requirement);
			if (canonical.isEmpty()) {
				ETSAssert.failWithUri(requirement,
						"accepted POST " + createEndpoint + " did not expose the owned resource before the deadline.");
			}
			URI resource = canonical.orElseThrow();
			URI occurrence = custom == null ? null
					: child(custom.items(), encoded(lastPathSegment(resource, requirement)));
			if (occurrence != null
					&& !awaitAvailable(occurrence, target.mediaType, target.identity, deadline, requirement)) {
				ETSAssert.failWithUri(requirement, "accepted POST " + createEndpoint
						+ " did not expose its collection occurrence before the deadline.");
			}
			return new ResourceUris(resource, occurrence, occurrence == null ? resource : occurrence);
		}

		String location = create.getHeader("Location");
		if (location == null || location.isBlank()) {
			ETSAssert.failWithUri(requirement, "POST " + createEndpoint + " returned HTTP 201 without Location.");
		}
		URI supplied = CreateReplaceDeleteSupport.resolveCreatedResourceUri(this.apiRoot, location, requirement);
		String id = lastPathSegment(supplied, requirement);
		URI canonical = this.apiRoot.resolve(kind.path() + "/" + encoded(id));
		Map<String, Object> canonicalBody = getJson(canonical, target.mediaType, requirement, null);
		assertIdentity(canonicalBody, target.identity, canonical, requirement);
		URI occurrence = custom == null ? null : child(custom.items(), encoded(id));
		if (occurrence != null) {
			Map<String, Object> occurrenceBody = getJson(occurrence, target.mediaType, requirement, null);
			assertIdentity(occurrenceBody, target.identity, occurrence, requirement);
		}
		return new ResourceUris(canonical, occurrence, occurrence == null ? canonical : occurrence);
	}

	private void exercisePatch(ResourceKind kind, ResourceUris uris, String representationMediaType,
			String patchMediaType, ResourceBaselines baselines, String identity, String requirement) {
		String changed = "ETS Update " + patchMediaType + " " + UUID.randomUUID();
		String conflictId = "ets-conflicting-id-" + UUID.randomUUID();
		Object patch = patchDocument(kind, representationMediaType, patchMediaType, changed, conflictId);
		Response response = EncodingMediatypeWrite.givenWithoutDefaultCharset()
			.accept(representationMediaType)
			.contentType(patchMediaType)
			.body(patch)
			.patch(uris.updateTarget())
			.andReturn();
		assertStatusIn(response, List.of(200, 202, 204), requirement, "PATCH " + uris.updateTarget());

		if (response.getStatusCode() == 202) {
			Deadline deadline = new Deadline();
			int[] consecutiveObservations = { 0 };
			boolean observed = pollUntil(deadline, () -> {
				if (updateObserved(kind, uris, representationMediaType, baselines, identity, changed, conflictId,
						requirement, deadline)) {
					consecutiveObservations[0]++;
				}
				else {
					consecutiveObservations[0] = 0;
				}
				return consecutiveObservations[0] >= 2;
			}, requirement, "queued PATCH postconditions for " + uris.updateTarget());
			if (!observed) {
				ETSAssert.failWithUri(requirement,
						"HTTP 202 PATCH postconditions were not jointly observed before the monotonic deadline.");
			}
			return;
		}
		assertCompleteObservation(kind, uris, representationMediaType, baselines, identity, changed, conflictId,
				requirement, null);
		assertCompleteObservation(kind, uris, representationMediaType, baselines, identity, changed, conflictId,
				requirement, null);
	}

	private boolean updateObserved(ResourceKind kind, ResourceUris uris, String mediaType, ResourceBaselines baselines,
			String identity, String changed, String conflictId, String requirement, Deadline deadline) {
		try {
			assertCompleteObservation(kind, uris, mediaType, baselines, identity, changed, conflictId, requirement,
					deadline);
			return !deadline.expired();
		}
		catch (AssertionError ex) {
			return false;
		}
	}

	private void assertCompleteObservation(ResourceKind kind, ResourceUris uris, String mediaType,
			ResourceBaselines baselines, String identity, String changed, String conflictId, String requirement,
			Deadline deadline) {
		assertUpdated(kind, uris.canonical(), mediaType, baselines.canonical(), identity, changed, conflictId,
				requirement, deadline);
		if (uris.occurrence() != null) {
			assertUpdated(kind, uris.occurrence(), mediaType, baselines.occurrence(), identity, changed, conflictId,
					requirement, deadline);
		}
	}

	private void assertUpdated(ResourceKind kind, URI resource, String mediaType, Observation baseline, String identity,
			String changed, String conflictId, String requirement, Deadline deadline) {
		Observation actual = observe(kind, resource, mediaType, requirement, deadline);
		assertIdentity(actual.body(), identity, resource, requirement);
		Object changedValue = nested(actual.body(), kind.descriptionPath(mediaType));
		if (!Objects.equals(changed, changedValue)) {
			ETSAssert.failWithUri(requirement, resource + " did not expose the submitted partial description change.");
		}
		Object sentinel = nested(actual.body(), kind.sentinelPath(mediaType));
		if (!Objects.equals(baseline.sentinel(), sentinel)) {
			ETSAssert.failWithUri(requirement,
					resource + " changed untouched sentinel " + kind.sentinelPath(mediaType) + ".");
		}
		String resourceId = lastPathSegment(resource, requirement);
		Object bodyId = actual.body().get("id");
		if (bodyId != null
				&& (!resourceId.equals(String.valueOf(bodyId)) || conflictId.equals(String.valueOf(bodyId)))) {
			ETSAssert.failWithUri(requirement,
					resource + " did not ignore the conflicting submitted resource identifier.");
		}
	}

	private Observation observe(ResourceKind kind, URI resource, String mediaType, String requirement,
			Deadline deadline) {
		Map<String, Object> body = getJson(resource, mediaType, requirement, deadline);
		Object sentinel = nested(body, kind.sentinelPath(mediaType));
		if (sentinel == null) {
			ETSAssert.failWithUri(requirement,
					resource + " is missing untouched sentinel " + kind.sentinelPath(mediaType) + ".");
		}
		return new Observation(body, sentinel);
	}

	private List<String> patchMediaTypes(URI resource, ApiDefinition definition, String requirement) {
		Response options = EncodingMediatypeWrite.givenWithoutDefaultCharset()
			.accept("*/*")
			.options(resource)
			.andReturn();
		ETSAssert.assertStatus(options, 200, requirement);
		String allow = String.join(",", options.getHeaders().getValues("Allow"));
		if (!headerContainsToken(allow, "PATCH")) {
			ETSAssert.failWithUri(requirement,
					"OPTIONS " + resource + " did not advertise PATCH in Allow; received " + allow + ".");
		}
		Set<String> openApi = openApiPatchMediaTypes(definition, resource);
		String acceptPatch = String.join(",", options.getHeaders().getValues("Accept-Patch"));
		List<String> selected = supportedPatchMediaTypes(acceptPatch, openApi);
		if (selected.isEmpty()) {
			throw new SkipException(requirement + " - " + resource
					+ " advertises PATCH but neither Accept-Patch nor its exact OpenAPI PATCH requestBody.content "
					+ "declares an implemented JSON Merge Patch or JSON Patch document. No PATCH request was issued.");
		}
		return selected;
	}

	static List<String> supportedPatchMediaTypes(String acceptPatch, Set<String> openApiMediaTypes) {
		Set<String> advertised = new LinkedHashSet<>();
		if (acceptPatch != null) {
			for (String value : acceptPatch.split(",")) {
				String mediaType = normalizeMediaType(value);
				if (!mediaType.isBlank() && !hasZeroQuality(value)) {
					advertised.add(mediaType);
				}
			}
		}
		if (openApiMediaTypes != null) {
			openApiMediaTypes.stream().map(UpdateSupport::normalizeMediaType).forEach(advertised::add);
		}
		return PATCH_PREFERENCE.stream().filter(advertised::contains).toList();
	}

	private Set<String> openApiPatchMediaTypes(ApiDefinition definition, URI resource) {
		if (definition == null || definition.model() == null || definition.model().getPaths() == null) {
			return Set.of();
		}
		String concrete = relativeApiPath(resource);
		String absolute = resource.getRawPath();
		Set<String> result = new LinkedHashSet<>();
		for (Map.Entry<String, Path> entry : definition.model().getPaths().entrySet()) {
			if (!templateMatches(entry.getKey(), concrete) && !templateMatches(entry.getKey(), absolute)) {
				continue;
			}
			Operation patch = entry.getValue() == null ? null : entry.getValue().getPatch();
			RequestBody requestBody = patch == null ? null : patch.getRequestBody();
			if (requestBody != null && requestBody.getContentMediaTypes() != null) {
				result.addAll(requestBody.getContentMediaTypes().keySet());
			}
		}
		return Set.copyOf(result);
	}

	private ApiDefinition apiDefinition(String requirement) {
		try {
			Map<String, Object> landing = parseJsonObject(get(this.apiRoot, "application/json"), this.apiRoot,
					requirement);
			Object links = landing.get("links");
			if (!(links instanceof List<?>)) {
				return null;
			}
			for (Object value : (List<?>) links) {
				if (!(value instanceof Map<?, ?> link) || !hasRelation(link.get("rel"), "service-desc")) {
					continue;
				}
				String href = string(link.get("href"));
				if (href == null) {
					continue;
				}
				URI source = this.apiRoot.resolve(href);
				String content = serviceDescription(source);
				if (content != null) {
					return GeoJsonSupport.parseApiDefinition(content, source, requirement);
				}
			}
		}
		catch (AssertionError | RuntimeException ex) {
			Reporter.log(requirement + " - API-definition PATCH discovery unavailable: " + ex.getMessage(), true);
		}
		return null;
	}

	private String serviceDescription(URI source) {
		if (sameOrigin(this.apiRoot, source)) {
			Response response = EncodingMediatypeWrite.givenWithoutDefaultCharset()
				.redirects()
				.follow(false)
				.accept("application/vnd.oai.openapi, application/yaml, application/json, */*")
				.get(source)
				.andReturn();
			return response.getStatusCode() == 200 ? response.asString() : null;
		}
		HttpRequest request = HttpRequest.newBuilder(source)
			.timeout(Duration.ofSeconds(30))
			.header("Accept", "application/vnd.oai.openapi, application/yaml, application/json, */*")
			.GET()
			.build();
		try {
			HttpResponse<String> response = HttpClient.newBuilder()
				.followRedirects(HttpClient.Redirect.NORMAL)
				.build()
				.send(request, HttpResponse.BodyHandlers.ofString());
			return response.statusCode() == 200 ? response.body() : null;
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("service-desc retrieval was interrupted", ex);
		}
		catch (IOException ex) {
			return null;
		}
	}

	private List<CustomEndpoint> customEndpoints(ResourceKind kind, String requirement) {
		URI current = this.apiRoot.resolve("collections");
		Set<URI> visited = new HashSet<>();
		Set<URI> endpoints = new LinkedHashSet<>();
		for (int page = 0; current != null; page++) {
			if (page >= MAX_DISCOVERY_PAGES) {
				ETSAssert.failWithUri(requirement,
						"/collections pagination exceeded " + MAX_DISCOVERY_PAGES + " pages.");
			}
			if (!sameOrigin(this.apiRoot, current) || !visited.add(current)) {
				ETSAssert.failWithUri(requirement,
						"/collections pagination is cross-origin or cyclic at " + current + ".");
			}
			Response response = get(current, "application/json");
			if (response.getStatusCode() == 404 && page == 0) {
				return List.of();
			}
			ETSAssert.assertStatus(response, 200, requirement);
			Map<String, Object> body = parseJsonObject(response, current, requirement);
			Object collections = body.get("collections");
			if (!(collections instanceof List<?>)) {
				ETSAssert.failWithUri(requirement, current + " response is missing a collections array.");
			}
			for (Object value : (List<?>) collections) {
				if (!(value instanceof Map<?, ?>)) {
					ETSAssert.failWithUri(requirement, current + " contains a non-object collection entry.");
				}
				@SuppressWarnings("unchecked")
				Map<String, Object> collection = (Map<String, Object>) value;
				if (kind.matches(collection)) {
					endpoints.addAll(itemsLinks(collection, requirement));
				}
			}
			current = nextUri(current, body, requirement);
		}
		return endpoints.stream().map(uri -> new CustomEndpoint(kind, uri)).toList();
	}

	private List<URI> itemsLinks(Map<String, Object> collection, String requirement) {
		Object links = collection.get("links");
		if (!(links instanceof List<?>)) {
			return List.of();
		}
		List<URI> result = new ArrayList<>();
		for (Object value : (List<?>) links) {
			if (!(value instanceof Map<?, ?> link) || !hasRelation(link.get("rel"), "items")) {
				continue;
			}
			String href = string(link.get("href"));
			if (href == null) {
				ETSAssert.failWithUri(requirement, "advertised rel=items link has no non-empty href.");
			}
			URI resolved = this.apiRoot.resolve(href);
			if (!sameOrigin(this.apiRoot, resolved)) {
				ETSAssert.failWithUri(requirement,
						"refusing cross-origin custom collection item endpoint " + resolved + ".");
			}
			result.add(resolved);
		}
		return List.copyOf(result);
	}

	private URI nextUri(URI current, Map<String, Object> body, String requirement) {
		Object links = body.get("links");
		if (links == null) {
			return null;
		}
		if (!(links instanceof List<?>)) {
			ETSAssert.failWithUri(requirement, current + " links is not an array.");
		}
		Set<URI> next = new LinkedHashSet<>();
		for (Object value : (List<?>) links) {
			if (!(value instanceof Map<?, ?> link) || !hasRelation(link.get("rel"), "next")) {
				continue;
			}
			String href = string(link.get("href"));
			if (href == null) {
				ETSAssert.failWithUri(requirement, current + " next link has no non-empty href.");
			}
			next.add(current.resolve(href));
		}
		if (next.size() > 1) {
			ETSAssert.failWithUri(requirement, current + " advertises multiple distinct next links.");
		}
		return next.stream().findFirst().orElse(null);
	}

	private Optional<URI> awaitOwned(ResourceKind kind, String identity, String mediaType, Deadline deadline,
			String requirement) {
		final URI[] found = new URI[1];
		boolean observed = pollUntil(deadline, () -> {
			found[0] = discoverOwned(kind, identity, mediaType, requirement, deadline).orElse(null);
			return found[0] != null;
		}, requirement, "accepted " + kind.name() + " creation");
		return observed ? Optional.of(found[0]) : Optional.empty();
	}

	private Optional<URI> discoverOwned(ResourceKind kind, String identity, String mediaType, String requirement,
			Deadline deadline) {
		URI endpoint = this.apiRoot.resolve(kind.path());
		return discoverOwnedAt(endpoint, endpoint, kind.name(), identity, mediaType, requirement, deadline);
	}

	private Optional<URI> discoverOwnedOccurrence(ResourceKind kind, URI items, String identity, String mediaType,
			String requirement, Deadline deadline) {
		return discoverOwnedAt(items, items, kind.name() + " custom occurrence", identity, mediaType, requirement,
				deadline);
	}

	private Optional<URI> discoverOwnedAt(URI collection, URI resourceBase, String label, String identity,
			String mediaType, String requirement, Deadline deadline) {
		URI current = collection;
		Set<URI> visited = new HashSet<>();
		for (int page = 0; current != null; page++) {
			if (page >= MAX_DISCOVERY_PAGES || !sameOrigin(this.apiRoot, current) || !visited.add(current)) {
				ETSAssert.failWithUri(requirement,
						label + " ownership discovery exceeded bounds or encountered unsafe pagination.");
			}
			Response response = deadline == null ? get(current, mediaType) : pollingGet(current, mediaType, deadline);
			if (response.getStatusCode() == 404) {
				return Optional.empty();
			}
			ETSAssert.assertStatus(response, 200, requirement);
			Map<String, Object> body = parseJsonObject(response, current, requirement);
			Object items = body.get("items");
			if (!(items instanceof List<?>)) {
				ETSAssert.failWithUri(requirement, current + " response is missing an items array.");
			}
			for (Object value : (List<?>) items) {
				if (value instanceof Map<?, ?> raw) {
					@SuppressWarnings("unchecked")
					Map<String, Object> item = (Map<String, Object>) raw;
					if (identity.equals(resourceIdentity(item))) {
						String id = string(item.get("id"));
						if (id == null) {
							ETSAssert.failWithUri(requirement,
									"owned " + label + " listing entry is missing its local id.");
						}
						return Optional.of(child(resourceBase, encoded(id)));
					}
				}
			}
			current = nextUri(current, body, requirement);
		}
		return Optional.empty();
	}

	private boolean awaitAvailable(URI resource, String mediaType, String identity, Deadline deadline,
			String requirement) {
		return pollUntil(deadline, () -> {
			Response response = pollingGet(resource, mediaType, deadline);
			if (response.getStatusCode() == 404) {
				return false;
			}
			ETSAssert.assertStatus(response, 200, requirement);
			return identity.equals(resourceIdentity(parseJsonObject(response, resource, requirement)));
		}, requirement, "accepted custom collection occurrence " + resource);
	}

	private void cleanup(CleanupTarget target, String requirement) {
		if (!target.postDispatched) {
			return;
		}
		URI[] canonical = { target.canonical };
		URI[] occurrence = { target.occurrence };
		if (canonical[0] == null || target.customItems != null && occurrence[0] == null) {
			Deadline discoveryDeadline = new Deadline();
			pollUntil(discoveryDeadline, () -> {
				if (canonical[0] == null) {
					canonical[0] = discoverOwned(target.kind, target.identity, target.mediaType, requirement,
							discoveryDeadline)
						.orElse(null);
				}
				if (target.customItems != null && occurrence[0] == null) {
					occurrence[0] = discoverOwnedOccurrence(target.kind, target.customItems, target.identity,
							target.mediaType, requirement, discoveryDeadline)
						.orElse(null);
				}
				return canonical[0] != null && (target.customItems == null || occurrence[0] != null);
			}, requirement, "owned fixture cleanup discovery");
		}
		if (canonical[0] == null && occurrence[0] == null) {
			if (!target.accepted) {
				return;
			}
			ETSAssert.failWithUri(requirement, "accepted " + target.kind.name() + " identity " + target.identity
					+ " could not be rediscovered for cleanup.");
		}
		if (canonical[0] == null) {
			canonical[0] = this.apiRoot
				.resolve(target.kind.path() + "/" + encoded(lastPathSegment(occurrence[0], requirement)));
		}
		if (occurrence[0] == null && target.customItems != null) {
			occurrence[0] = child(target.customItems, encoded(lastPathSegment(canonical[0], requirement)));
		}
		List<Throwable> failures = new ArrayList<>();
		if (occurrence[0] != null && !occurrence[0].equals(canonical[0])) {
			tryCleanupDelete(occurrence[0], false, target, requirement, failures);
		}
		tryCleanupDelete(canonical[0], target.kind == SYSTEM, target, requirement, failures);
		if (!failures.isEmpty()) {
			AssertionError aggregate = new AssertionError(
					requirement + " - one or more owned-resource cleanup routes failed.");
			failures.forEach(aggregate::addSuppressed);
			throw aggregate;
		}
	}

	private void tryCleanupDelete(URI resource, boolean cascade, CleanupTarget target, String requirement,
			List<Throwable> failures) {
		try {
			cleanupDelete(resource, cascade, target.identity, target.mediaType, requirement);
		}
		catch (Throwable thrown) {
			failures.add(thrown);
		}
	}

	private void cleanupDelete(URI resource, boolean cascade, String identity, String mediaType, String requirement) {
		requireSameOrigin(resource, requirement);
		Deadline deadline = new Deadline();
		Response current = pollingGet(resource, mediaType, deadline);
		if (current.getStatusCode() == 404) {
			return;
		}
		ETSAssert.assertStatus(current, 200, requirement);
		assertIdentity(parseJsonObject(current, resource, requirement), identity, resource, requirement);
		RequestSpecification request = deadlineRequest(deadline).accept("application/json");
		if (cascade) {
			request.queryParam("cascade", true);
		}
		Response response = request.delete(resource).andReturn();
		assertStatusIn(response, List.of(200, 202, 204, 404), requirement, "cleanup DELETE " + resource);
		if (response.getStatusCode() == 404) {
			return;
		}
		if (!pollUntil(deadline, () -> {
			Response get = pollingGet(resource, mediaType, deadline);
			return get.getStatusCode() == 404;
		}, requirement, "cleanup DELETE " + resource)) {
			ETSAssert.failWithUri(requirement, resource + " remained available after cleanup DELETE.");
		}
	}

	private List<String> resourceMediaTypes(ResourceKind kind, Map<String, Object> conformance, String requirement) {
		List<String> result = new ArrayList<>();
		if (!kind.property() && declares(conformance, CONF_GEOJSON)) {
			result.add(GEOJSON);
		}
		if (kind != SAMPLING_FEATURE && declares(conformance, CONF_SENSORML)) {
			result.add(SENSORML);
		}
		if (result.isEmpty()) {
			throw new SkipException(requirement + " - no applicable declared GeoJSON or SensorML representation is "
					+ "available for " + kind.path() + "; no write request was issued.");
		}
		return List.copyOf(result);
	}

	private Object patchDocument(ResourceKind kind, String representationMediaType, String patchMediaType,
			String changed, String conflictId) {
		if (MERGE_PATCH.equals(patchMediaType)) {
			Map<String, Object> patch = new LinkedHashMap<>();
			patch.put("id", conflictId);
			if (SENSORML.equals(representationMediaType)) {
				patch.put("description", changed);
			}
			else {
				patch.put("properties", Map.of("description", changed));
			}
			return patch;
		}
		return List.of(Map.of("op", "replace", "path", kind.descriptionPath(representationMediaType), "value", changed),
				Map.of("op", "add", "path", "/id", "value", conflictId));
	}

	private Map<String, Object> getJson(URI resource, String mediaType, String requirement, Deadline deadline) {
		Response response = deadline == null ? get(resource, mediaType) : pollingGet(resource, mediaType, deadline);
		ETSAssert.assertStatus(response, 200, requirement);
		return parseJsonObject(response, resource, requirement);
	}

	private Response pollingGet(URI uri, String accept, Deadline deadline) {
		return deadlineRequest(deadline).accept(accept).get(uri).andReturn();
	}

	private RequestSpecification deadlineRequest(Deadline deadline) {
		int timeoutMillis = deadline.requestTimeoutMillis();
		HttpClientConfig httpClient = HttpClientConfig.httpClientConfig()
			.setParam("http.connection.timeout", timeoutMillis)
			.setParam("http.socket.timeout", timeoutMillis)
			.setParam("http.connection-manager.timeout", (long) timeoutMillis);
		RestAssuredConfig config = RestAssuredConfig.config().httpClient(httpClient);
		return EncodingMediatypeWrite.givenWithoutDefaultCharset().config(config);
	}

	private boolean pollUntil(Deadline deadline, BooleanSupplier condition, String requirement, String operation) {
		while (true) {
			failIfInterrupted(requirement, operation);
			if (!deadline.canStartRequest()) {
				return false;
			}
			boolean satisfied;
			try {
				satisfied = condition.getAsBoolean();
			}
			catch (Throwable ex) {
				if (!causedByTimeout(ex)) {
					if (ex instanceof Error) {
						throw (Error) ex;
					}
					if (ex instanceof RuntimeException) {
						throw (RuntimeException) ex;
					}
					throw new IllegalStateException(ex);
				}
				satisfied = false;
			}
			failIfInterrupted(requirement, operation);
			if (deadline.expired()) {
				return false;
			}
			if (satisfied) {
				return true;
			}
			long sleepNanos = deadline.sleepNanos();
			if (sleepNanos <= 0) {
				return false;
			}
			try {
				TimeUnit.NANOSECONDS.sleep(sleepNanos);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				ETSAssert.failWithUri(requirement, "interrupted while waiting for " + operation + ".");
			}
		}
	}

	private static void failIfInterrupted(String requirement, String operation) {
		if (Thread.currentThread().isInterrupted()) {
			ETSAssert.failWithUri(requirement, "interrupted while waiting for " + operation + ".");
		}
	}

	private static boolean causedByTimeout(Throwable thrown) {
		for (Throwable cause = thrown; cause != null; cause = cause.getCause()) {
			if (cause instanceof SocketTimeoutException || cause instanceof AsyncRequestTimeoutException) {
				return true;
			}
		}
		return false;
	}

	private void executeWithCleanup(String requirement, Procedure procedure) {
		CleanupStack cleanup = new CleanupStack(requirement);
		Throwable primary = null;
		try {
			procedure.run(cleanup);
		}
		catch (Throwable thrown) {
			primary = thrown;
		}
		Throwable result = cleanup.close(primary);
		if (result instanceof Error) {
			throw (Error) result;
		}
		if (result instanceof RuntimeException) {
			throw (RuntimeException) result;
		}
		if (result != null) {
			throw new IllegalStateException(result);
		}
	}

	private void requireDeclaration(Map<String, Object> conformance, String declaration, String requirement,
			String reason) {
		if (!declares(conformance, declaration)) {
			throw new SkipException(requirement + " - " + reason + " Missing exact URI " + declaration
					+ ". No write request was issued.");
		}
	}

	private static boolean declares(Map<String, Object> conformance, String declaration) {
		Object conformsTo = conformance.get("conformsTo");
		return conformsTo instanceof List<?> && ((List<?>) conformsTo).contains(declaration);
	}

	private static void assertIdentity(Map<String, Object> body, String expected, URI resource, String requirement) {
		String actual = resourceIdentity(body);
		if (!expected.equals(actual)) {
			ETSAssert.failWithUri(requirement, resource + " exposes external identity '" + actual
					+ "' instead of submitted identity '" + expected + "'.");
		}
	}

	private static String resourceIdentity(Map<String, Object> body) {
		String identity = string(body.get("uniqueId"));
		if (identity != null) {
			return identity;
		}
		Object properties = body.get("properties");
		return properties instanceof Map<?, ?> ? string(((Map<?, ?>) properties).get("uid")) : null;
	}

	@SuppressWarnings("unchecked")
	private static Object nested(Map<String, Object> body, String pointer) {
		Object current = body;
		for (String token : pointer.substring(1).split("/")) {
			if (!(current instanceof Map<?, ?>)) {
				return null;
			}
			current = ((Map<String, Object>) current).get(token);
		}
		return current;
	}

	private static Map<String, Object> parseJsonObject(Response response, URI source, String requirement) {
		String mediaType = normalizeMediaType(response.getContentType());
		if (!"application/json".equals(mediaType)
				&& !(mediaType.startsWith("application/") && mediaType.endsWith("+json"))) {
			ETSAssert.failWithUri(requirement,
					source + " response is not a JSON media type: '" + response.getContentType() + "'.");
		}
		try {
			Map<String, Object> body = response.jsonPath().getMap("$");
			if (body == null) {
				ETSAssert.failWithUri(requirement, source + " response body is not a JSON object.");
			}
			return body;
		}
		catch (RuntimeException ex) {
			ETSAssert.failWithUri(requirement,
					source + " response body is not parseable as a JSON object: " + ex.getMessage() + ".");
			return Map.of();
		}
	}

	private String relativeApiPath(URI resource) {
		String root = this.apiRoot.getRawPath();
		String concrete = resource.getRawPath();
		String normalizedRoot = root == null ? "/" : root;
		if (!normalizedRoot.endsWith("/")) {
			normalizedRoot += "/";
		}
		if (concrete != null && concrete.startsWith(normalizedRoot)) {
			return "/" + concrete.substring(normalizedRoot.length());
		}
		return concrete == null ? "" : concrete;
	}

	private static boolean templateMatches(String template, String concrete) {
		String normalizedTemplate = stripTrailingSlash(template);
		String normalizedConcrete = stripTrailingSlash(concrete);
		String[] expected = normalizedTemplate.split("/");
		String[] actual = normalizedConcrete.split("/");
		if (expected.length != actual.length) {
			return false;
		}
		for (int index = 0; index < expected.length; index++) {
			String part = expected[index];
			if (part.startsWith("{") && part.endsWith("}")) {
				continue;
			}
			if (!part.equals(actual[index])) {
				return false;
			}
		}
		return true;
	}

	private static boolean hasRelation(Object value, String expected) {
		if (value instanceof String) {
			return Arrays.stream(((String) value).split("\\s+")).anyMatch(expected::equals);
		}
		return value instanceof List<?> && ((List<?>) value).contains(expected);
	}

	private static boolean headerContainsToken(String header, String expected) {
		return header != null && Arrays.stream(header.split(","))
			.map(String::trim)
			.anyMatch(value -> expected.equalsIgnoreCase(value));
	}

	private static boolean hasZeroQuality(String value) {
		return Arrays.stream(value.split(";"))
			.map(String::trim)
			.map(part -> part.toLowerCase(Locale.ROOT))
			.anyMatch(part -> part.matches("q=0(?:\\.0*)?"));
	}

	private static String normalizeMediaType(String value) {
		return value == null ? "" : value.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
	}

	private static Response get(URI resource, String accept) {
		return EncodingMediatypeWrite.givenWithoutDefaultCharset().accept(accept).get(resource).andReturn();
	}

	private void requireSameOrigin(URI resource, String requirement) {
		if (!sameOrigin(this.apiRoot, resource)) {
			ETSAssert.failWithUri(requirement, "refusing cleanup outside API origin: " + resource + ".");
		}
	}

	private static boolean sameOrigin(URI left, URI right) {
		return left != null && right != null && left.getScheme() != null && right.getScheme() != null
				&& left.getScheme().equalsIgnoreCase(right.getScheme()) && left.getHost() != null
				&& right.getHost() != null && left.getHost().equalsIgnoreCase(right.getHost())
				&& effectivePort(left) == effectivePort(right);
	}

	private static int effectivePort(URI uri) {
		return uri.getPort() >= 0 ? uri.getPort() : "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
	}

	private static String uid(String scope) {
		return "urn:ets:ogcapi-connectedsystems10:update:" + scope + ":" + UUID.randomUUID();
	}

	private static String lastPathSegment(URI uri, String requirement) {
		String path = uri.getPath();
		int slash = path == null ? -1 : path.lastIndexOf('/');
		String id = slash >= 0 ? path.substring(slash + 1) : path;
		if (id == null || id.isBlank()) {
			ETSAssert.failWithUri(requirement, "resource URI has no local identifier: " + uri + ".");
		}
		return id;
	}

	private static URI child(URI parent, String child) {
		return URI.create(stripTrailingSlash(parent.toString()) + "/" + child);
	}

	private static String encoded(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
	}

	private static String stripTrailingSlash(String value) {
		return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
	}

	private static String string(Object value) {
		return value instanceof String && !((String) value).isBlank() ? (String) value : null;
	}

	private static long positiveLongProperty(String name, long defaultValue) {
		String configured = System.getProperty(name);
		if (configured == null || configured.isBlank()) {
			return defaultValue;
		}
		try {
			long value = Long.parseLong(configured);
			if (value > 0) {
				return value;
			}
		}
		catch (NumberFormatException ex) {
			// Fall through to the deterministic configuration error.
		}
		throw new IllegalArgumentException(name + " must be a positive integer");
	}

	private static void assertStatusIn(Response response, List<Integer> expected, String requirement,
			String operation) {
		if (!expected.contains(response.getStatusCode())) {
			ETSAssert.failWithUri(requirement,
					operation + " expected HTTP status in " + expected + ", got " + response.getStatusCode() + ".");
		}
	}

	private record ResourceKind(String name, String path, String requirement, String condition, String featureType,
			boolean property) {

		boolean matches(Map<String, Object> collection) {
			String itemType = string(collection.get("itemType"));
			String advertisedType = this.property ? itemType : string(collection.get("featureType"));
			return (this.property || "feature".equalsIgnoreCase(itemType))
					&& typeMatches(advertisedType, this.featureType);
		}

		String descriptionPath(String mediaType) {
			return SENSORML.equals(mediaType) ? "/description" : "/properties/description";
		}

		String sentinelPath(String mediaType) {
			return SENSORML.equals(mediaType) ? "/label" : "/properties/name";
		}

		private static boolean typeMatches(String actual, String expected) {
			if (actual == null) {
				return false;
			}
			String local = expected.substring(expected.indexOf(':') + 1);
			return expected.equals(actual) || ("http://www.w3.org/ns/sosa/" + local).equals(actual);
		}

	}

	private record CustomEndpoint(ResourceKind kind, URI items) {
	}

	private record ResourceUris(URI canonical, URI occurrence, URI updateTarget) {
	}

	private record ResourceBaselines(Observation canonical, Observation occurrence) {
	}

	private record Observation(Map<String, Object> body, Object sentinel) {
	}

	private static final class CleanupTarget {

		private final ResourceKind kind;

		private final String mediaType;

		private final String identity;

		private final URI customItems;

		private boolean accepted;

		private boolean postDispatched;

		private URI canonical;

		private URI occurrence;

		private CleanupTarget(ResourceKind kind, String mediaType, String identity, URI customItems) {
			this.kind = kind;
			this.mediaType = mediaType;
			this.identity = identity;
			this.customItems = customItems;
		}

	}

	private static final class CleanupStack {

		private final String requirement;

		private final Deque<CleanupAction> actions = new ArrayDeque<>();

		private CleanupStack(String requirement) {
			this.requirement = requirement;
		}

		private void push(String label, ThrowingAction action) {
			this.actions.push(new CleanupAction(label, action));
		}

		private Throwable close(Throwable primary) {
			List<AssertionError> failures = new ArrayList<>();
			while (!this.actions.isEmpty()) {
				CleanupAction cleanup = this.actions.pop();
				try {
					cleanup.action().run();
				}
				catch (Throwable thrown) {
					AssertionError failure = new AssertionError(
							this.requirement + " - cleanup failed for " + cleanup.label() + ": " + thrown.getMessage());
					failure.initCause(thrown);
					failures.add(failure);
				}
			}
			if (primary != null) {
				if (primary instanceof SkipException && !failures.isEmpty()) {
					AssertionError aggregate = new AssertionError(this.requirement
							+ " - accepted update evidence was inconclusive and owned-resource cleanup failed.");
					aggregate.addSuppressed(primary);
					failures.forEach(aggregate::addSuppressed);
					return aggregate;
				}
				failures.forEach(primary::addSuppressed);
				return primary;
			}
			if (failures.isEmpty()) {
				return null;
			}
			AssertionError aggregate = new AssertionError(
					this.requirement + " - one or more owned-resource cleanup operations failed.");
			failures.forEach(aggregate::addSuppressed);
			return aggregate;
		}

	}

	private record CleanupAction(String label, ThrowingAction action) {
	}

	@FunctionalInterface
	private interface ThrowingAction {

		void run() throws Exception;

	}

	@FunctionalInterface
	private interface Procedure {

		void run(CleanupStack cleanup);

	}

	private final class Deadline {

		private static final long MINIMUM_REQUEST_SLICE_MILLIS = 250L;

		private final long startedNanos = UpdateSupport.this.nanoTime.getAsLong();

		private final long timeoutNanos = TimeUnit.MILLISECONDS.toNanos(UpdateSupport.this.asyncTimeoutMillis);

		private final long pollNanos = TimeUnit.MILLISECONDS.toNanos(UpdateSupport.this.asyncPollMillis);

		private long remainingNanos() {
			return this.timeoutNanos - (UpdateSupport.this.nanoTime.getAsLong() - this.startedNanos);
		}

		private boolean canStartRequest() {
			return TimeUnit.NANOSECONDS.toMillis(remainingNanos()) > 0L;
		}

		private boolean expired() {
			return remainingNanos() <= 0L;
		}

		private long sleepNanos() {
			return Math.min(remainingNanos(), this.pollNanos);
		}

		private int requestTimeoutMillis() {
			long remainingMillis = TimeUnit.NANOSECONDS.toMillis(remainingNanos());
			if (remainingMillis <= 0L) {
				throw new AsyncRequestTimeoutException();
			}
			long slice = Math.max(TimeUnit.NANOSECONDS.toMillis(this.pollNanos), MINIMUM_REQUEST_SLICE_MILLIS);
			return (int) Math.min(Integer.MAX_VALUE, Math.min(remainingMillis, slice));
		}

	}

	private static final class AsyncRequestTimeoutException extends RuntimeException {

	}

}
