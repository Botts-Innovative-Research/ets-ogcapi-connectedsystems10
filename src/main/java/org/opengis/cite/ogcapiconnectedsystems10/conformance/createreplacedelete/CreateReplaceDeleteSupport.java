package org.opengis.cite.ogcapiconnectedsystems10.conformance.createreplacedelete;

import java.net.URI;
import java.net.URLEncoder;
import java.net.SocketTimeoutException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
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
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.TestRunArg;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.EncodingMediatypeWrite;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.TraversalResult;
import org.testng.SkipException;

import io.restassured.response.Response;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;

/**
 * Stateful, per-procedure support for the released Create/Replace/Delete ATS.
 */
public final class CreateReplaceDeleteSupport {

	private static final String CONF_BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/";

	private static final String CONF_CREATE_REPLACE_DELETE = CONF_BASE + "create-replace-delete";

	private static final String CONF_API_COMMON = CONF_BASE + "api-common";

	private static final String CONF_INHERITED_CREATE_REPLACE_DELETE = "http://www.opengis.net/spec/ogcapi-4/1.0/conf/create-replace-delete";

	private static final String REQ_BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/create-replace-delete/";

	static final String CONF_GEOJSON = CONF_BASE + "geojson";

	static final String CONF_SENSORML = CONF_BASE + "sensorml";

	private static final String GEOJSON = "application/geo+json";

	private static final String SENSORML = "application/sml+json";

	private static final String ENABLED = "true";

	private static final String DEDICATED_POLICY = "dedicated-mutable-iut";

	private static final String LOCAL_SCHEMA_PREFIX = "https://csapi-compliance.local/schemas/";

	private static final String ASYNC_TIMEOUT_PROPERTY = "org.opengis.cite.ogcapiconnectedsystems10.crd.asyncTimeoutMillis";

	private static final String ASYNC_POLL_PROPERTY = "org.opengis.cite.ogcapiconnectedsystems10.crd.asyncPollMillis";

	private static final long DEFAULT_ASYNC_TIMEOUT_MILLIS = 10_000L;

	private static final long DEFAULT_ASYNC_POLL_MILLIS = 100L;

	private static final ObjectMapper JSON = new ObjectMapper();

	private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(
			SpecVersion.VersionFlag.V202012,
			builder -> builder.schemaMappers(mappers -> mappers.mapPrefix(LOCAL_SCHEMA_PREFIX, "classpath:schemas/")
				.mapPrefix("https://geojson.org/schema/", "classpath:schemas/external/geojson.org/schema/")));

	private static final ResourceKind SYSTEM = new ResourceKind("System", "systems", CONF_BASE + "system",
			"sosa:System", false);

	private static final ResourceKind DEPLOYMENT = new ResourceKind("Deployment", "deployments",
			CONF_BASE + "deployment", "sosa:Deployment", false);

	private static final ResourceKind PROCEDURE = new ResourceKind("Procedure", "procedures", CONF_BASE + "procedure",
			"sosa:Procedure", false);

	private static final ResourceKind SAMPLING_FEATURE = new ResourceKind("Sampling Feature", "samplingFeatures",
			CONF_BASE + "sf", "sosa:Sample", false);

	private static final ResourceKind PROPERTY = new ResourceKind("Property", "properties", CONF_BASE + "property",
			"sosa:Property", true);

	private static final List<ResourceKind> CUSTOM_KINDS = List.of(SYSTEM, PROCEDURE, DEPLOYMENT, SAMPLING_FEATURE,
			PROPERTY);

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
	public CreateReplaceDeleteSupport(URI apiRoot, String mutationTestsEnabled, String mutationIutPolicy) {
		this(apiRoot, mutationTestsEnabled, mutationIutPolicy,
				positiveLongProperty(ASYNC_TIMEOUT_PROPERTY, DEFAULT_ASYNC_TIMEOUT_MILLIS),
				positiveLongProperty(ASYNC_POLL_PROPERTY, DEFAULT_ASYNC_POLL_MILLIS));
	}

	CreateReplaceDeleteSupport(URI apiRoot, String mutationTestsEnabled, String mutationIutPolicy,
			long asyncTimeoutMillis, long asyncPollMillis) {
		this(apiRoot, mutationTestsEnabled, mutationIutPolicy, asyncTimeoutMillis, asyncPollMillis, System::nanoTime);
	}

	CreateReplaceDeleteSupport(URI apiRoot, String mutationTestsEnabled, String mutationIutPolicy,
			long asyncTimeoutMillis, long asyncPollMillis, LongSupplier nanoTime) {
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
	 * Executes abstract test A.67.
	 */
	public void systemsCreateReplaceDelete() {
		String requirement = REQ_BASE + "system";
		Map<String, Object> conformance = prepare(requirement, SYSTEM.condition());
		executeWithCleanup(requirement,
				cleanup -> transactions(SYSTEM, this.apiRoot.resolve("systems"), conformance, requirement, cleanup));
	}

	/**
	 * Executes abstract test A.68.
	 */
	public void systemDeleteCascade() {
		String requirement = REQ_BASE + "system-delete-cascade";
		Map<String, Object> conformance = prepare(requirement, SYSTEM.condition());
		executeWithCleanup(requirement, cleanup -> {
			String systemMediaType = preferredMediaType(SYSTEM, conformance, requirement);
			String deploymentMediaType = preferredMediaType(DEPLOYMENT, conformance, requirement);
			String parentUid = uid("cascade-parent");
			OwnedResource parent = canonicalOwned(
					createOwned(this.apiRoot.resolve("systems"), body(SYSTEM, systemMediaType, "parent", parentUid),
							systemMediaType, SYSTEM, requirement, cleanup, true),
					SYSTEM, requirement, cleanup);
			OwnedResource child = canonicalOwned(createOwned(childCollection(parent.uri(), "subsystems"),
					body(SYSTEM, systemMediaType, "child", uid("cascade-child")), systemMediaType, SYSTEM, requirement,
					cleanup, true), SYSTEM, requirement, cleanup);

			assertDeleteConflict(parent.uri(), requirement);
			assertAvailable(parent.uri(), requirement);
			assertAvailable(child.uri(), requirement);
			AsyncDeadline parentDelete = delete(parent, true, requirement);
			if (parentDelete == null) {
				assertGone(child.uri(), requirement);
			}
			else {
				awaitCompoundOrSkip(parentDelete, requirement, "queued cascade DELETE of " + parent.uri(),
						() -> gone(parent.uri(), parentDelete, requirement),
						() -> gone(child.uri(), parentDelete, requirement));
			}

			String targetUid = uid("cascade-target");
			String survivorUid = uid("cascade-survivor");
			OwnedResource target = canonicalOwned(
					createOwned(this.apiRoot.resolve("systems"), body(SYSTEM, systemMediaType, "target", targetUid),
							systemMediaType, SYSTEM, requirement, cleanup, true),
					SYSTEM, requirement, cleanup);
			OwnedResource survivor = canonicalOwned(
					createOwned(this.apiRoot.resolve("systems"), body(SYSTEM, systemMediaType, "survivor", survivorUid),
							systemMediaType, SYSTEM, requirement, cleanup, true),
					SYSTEM, requirement, cleanup);
			OwnedResource deployment = canonicalOwned(createOwned(this.apiRoot.resolve("deployments"),
					body(DEPLOYMENT, deploymentMediaType, "association", uid("cascade-deployment"),
							List.of(targetUid, survivorUid)),
					deploymentMediaType, DEPLOYMENT, requirement, cleanup, false), DEPLOYMENT, requirement, cleanup);

			Map<String, Object> initialDeployment = getJson(deployment.uri(), deploymentMediaType, 200, requirement);
			assertContainsAssociation(initialDeployment, targetUid, target.uri(), "target", requirement);
			assertContainsAssociation(initialDeployment, survivorUid, survivor.uri(), "survivor", requirement);
			assertDeleteConflict(target.uri(), requirement);
			assertAvailable(target.uri(), requirement);
			assertAvailable(survivor.uri(), requirement);
			assertAvailable(deployment.uri(), requirement);
			AsyncDeadline targetDelete = delete(target, true, requirement);
			if (targetDelete == null) {
				assertDeploymentAfterCascade(deployment.uri(), deploymentMediaType, targetUid, target.uri(),
						survivorUid, survivor.uri(), requirement);
				assertAvailable(survivor.uri(), requirement);
			}
			else {
				awaitCascadePostconditionsOrSkip(deployment.uri(), deploymentMediaType, targetUid, target.uri(),
						survivorUid, survivor.uri(), targetDelete, requirement);
			}
		});
	}

	/**
	 * Executes abstract test A.69.
	 */
	public void subsystemsCreate() {
		String requirement = REQ_BASE + "subsystem";
		Map<String, Object> conformance = prepare(requirement, CONF_BASE + "subsystem");
		executeWithCleanup(requirement, cleanup -> {
			String parentMediaType = preferredMediaType(SYSTEM, conformance, requirement);
			OwnedResource parent = canonicalOwned(createOwned(this.apiRoot.resolve("systems"),
					body(SYSTEM, parentMediaType, "subsystem-parent", uid("subsystem-parent")), parentMediaType, SYSTEM,
					requirement, cleanup, true), SYSTEM, requirement, cleanup);
			for (String mediaType : supportedMediaTypes(SYSTEM.path(), conformance, requirement)) {
				Map<String, Object> child = body(SYSTEM, mediaType, "subsystem", uid("subsystem"));
				createOnly(childCollection(parent.uri(), "subsystems"), child, mediaType, SYSTEM, requirement, cleanup,
						true);
			}
		});
	}

	/**
	 * Executes abstract test A.70.
	 */
	public void deploymentsCreateReplaceDelete() {
		String requirement = REQ_BASE + "deployment";
		Map<String, Object> conformance = prepare(requirement, DEPLOYMENT.condition());
		executeWithCleanup(requirement, cleanup -> transactions(DEPLOYMENT, this.apiRoot.resolve("deployments"),
				conformance, requirement, cleanup));
	}

	/**
	 * Executes abstract test A.71.
	 */
	public void subdeploymentsCreate() {
		String requirement = REQ_BASE + "subdeployment";
		Map<String, Object> conformance = prepare(requirement, CONF_BASE + "subdeployment");
		executeWithCleanup(requirement, cleanup -> {
			String parentMediaType = preferredMediaType(DEPLOYMENT, conformance, requirement);
			OwnedResource parent = canonicalOwned(
					createOwned(this.apiRoot.resolve("deployments"),
							body(DEPLOYMENT, parentMediaType, "subdeployment-parent", uid("subdeployment-parent")),
							parentMediaType, DEPLOYMENT, requirement, cleanup, false),
					DEPLOYMENT, requirement, cleanup);
			for (String mediaType : supportedMediaTypes(DEPLOYMENT.path(), conformance, requirement)) {
				Map<String, Object> child = body(DEPLOYMENT, mediaType, "subdeployment", uid("subdeployment"));
				createOnly(childCollection(parent.uri(), "subdeployments"), child, mediaType, DEPLOYMENT, requirement,
						cleanup, false);
			}
		});
	}

	/**
	 * Executes abstract test A.72.
	 */
	public void proceduresCreateReplaceDelete() {
		String requirement = REQ_BASE + "procedure";
		Map<String, Object> conformance = prepare(requirement, PROCEDURE.condition());
		executeWithCleanup(requirement, cleanup -> transactions(PROCEDURE, this.apiRoot.resolve("procedures"),
				conformance, requirement, cleanup));
	}

	/**
	 * Executes abstract test A.73.
	 */
	public void samplingFeaturesCreateReplaceDelete() {
		String requirement = REQ_BASE + "sampling-feature";
		Map<String, Object> conformance = prepare(requirement, SAMPLING_FEATURE.condition());
		executeWithCleanup(requirement, cleanup -> {
			String parentMediaType = preferredMediaType(SYSTEM, conformance, requirement);
			OwnedResource parent = canonicalOwned(createOwned(this.apiRoot.resolve("systems"),
					body(SYSTEM, parentMediaType, "sampling-parent", uid("sampling-parent")), parentMediaType, SYSTEM,
					requirement, cleanup, true), SYSTEM, requirement, cleanup);
			for (String mediaType : supportedMediaTypes(SAMPLING_FEATURE.path(), conformance, requirement)) {
				String samplingUid = uid("sampling-feature");
				transaction(SAMPLING_FEATURE, childCollection(parent.uri(), "samplingFeatures"),
						body(SAMPLING_FEATURE, mediaType, "create", samplingUid),
						identity -> body(SAMPLING_FEATURE, mediaType, "replace", identity), mediaType, requirement,
						cleanup);
			}
		});
	}

	/**
	 * Executes abstract test A.74.
	 */
	public void propertiesCreateReplaceDelete() {
		String requirement = REQ_BASE + "property";
		Map<String, Object> conformance = prepare(requirement, PROPERTY.condition());
		executeWithCleanup(requirement, cleanup -> transactions(PROPERTY, this.apiRoot.resolve("properties"),
				conformance, requirement, cleanup));
	}

	/**
	 * Executes abstract test A.75.
	 */
	public void resourcesCreateInCustomCollections() {
		String requirement = REQ_BASE + "create-in-collection";
		List<CustomCollection> collections = prepareCustomCollections(requirement);
		executeWithCleanup(requirement, cleanup -> {
			for (CustomCollection collection : collections) {
				for (String mediaType : collection.mediaTypes()) {
					String resourceUid = uid("custom-create-" + collection.kind().path());
					Map<String, Object> body = body(collection.kind(), mediaType, "create", resourceUid);
					OwnedResource created = createOwned(collection.itemsUri(), body, mediaType, collection.kind(),
							requirement, cleanup, collection.kind() == SYSTEM);
					String id = lastPathSegment(created.uri(), requirement);
					URI collectionItem = childCollection(collection.itemsUri(), encoded(id));
					URI canonical = canonicalUri(collection.kind(), created.uri(), requirement);
					cleanup.push("canonical custom-created resource " + canonical,
							() -> cleanupDelete(canonical, collection.kind() == SYSTEM, requirement));
					registerAndVerifyOccurrence(collectionItem, canonical, mediaType, body, created, requirement,
							cleanup);
				}
			}
		});
	}

	/**
	 * Executes abstract test A.76.
	 */
	public void resourcesReplaceInCustomCollections() {
		String requirement = REQ_BASE + "replace-in-collection";
		List<CustomCollection> collections = prepareCustomCollections(requirement);
		executeWithCleanup(requirement, cleanup -> {
			for (CustomCollection collection : collections) {
				for (String mediaType : collection.mediaTypes()) {
					String resourceUid = uid("custom-replace-" + collection.kind().path());
					Map<String, Object> create = body(collection.kind(), mediaType, "create", resourceUid);
					OwnedResource created = createOwned(collection.itemsUri(), create, mediaType, collection.kind(),
							requirement, cleanup, collection.kind() == SYSTEM);
					String id = lastPathSegment(created.uri(), requirement);
					URI collectionItem = childCollection(collection.itemsUri(), encoded(id));
					URI canonical = canonicalUri(collection.kind(), created.uri(), requirement);
					cleanup.push("canonical custom-created resource " + canonical,
							() -> cleanupDelete(canonical, collection.kind() == SYSTEM, requirement));
					registerAndVerifyOccurrence(collectionItem, canonical, mediaType, create, created, requirement,
							cleanup);
					Map<String, Object> replacement = body(collection.kind(), mediaType, "replace", resourceUid);
					assertOptions(collectionItem, List.of("PUT"), requirement);
					Response put = request(mediaType, replacement, collection.kind(), requirement).put(collectionItem)
						.andReturn();
					assertStatusIn(put, List.of(200, 202, 204), requirement, "PUT " + collectionItem);
					if (put.getStatusCode() == 202) {
						AsyncDeadline deadline = new AsyncDeadline();
						awaitCompoundOrSkip(deadline, requirement, "queued PUT propagation for " + collectionItem,
								() -> submittedContentAvailable(collectionItem, mediaType, replacement, deadline,
										requirement),
								() -> submittedContentAvailable(canonical, mediaType, replacement, deadline,
										requirement));
					}
					else {
						assertSubmittedContent(replacement, getJson(collectionItem, mediaType, 200, requirement),
								requirement);
						assertSubmittedContent(replacement, getJson(canonical, mediaType, 200, requirement),
								requirement);
					}
				}
			}
		});
	}

	/**
	 * Executes abstract test A.77.
	 */
	public void resourcesDeleteInCustomCollections() {
		String requirement = REQ_BASE + "delete-in-collection";
		List<CustomCollection> collections = prepareCustomCollections(requirement);
		executeWithCleanup(requirement, cleanup -> {
			for (CustomCollection collection : collections) {
				for (String mediaType : collection.mediaTypes()) {
					Map<String, Object> rootDeleteBody = body(collection.kind(), mediaType, "root-delete",
							uid("custom-root-delete"));
					OwnedResource rootDelete = createOwned(collection.itemsUri(), rootDeleteBody, mediaType,
							collection.kind(), requirement, cleanup, collection.kind() == SYSTEM);
					String rootDeleteId = lastPathSegment(rootDelete.uri(), requirement);
					URI rootDeleteItem = childCollection(collection.itemsUri(), encoded(rootDeleteId));
					URI rootDeleteCanonical = canonicalUri(collection.kind(), rootDelete.uri(), requirement);
					cleanup.push("canonical custom-created resource " + rootDeleteCanonical,
							() -> cleanupDelete(rootDeleteCanonical, collection.kind() == SYSTEM, requirement));
					registerAndVerifyOccurrence(rootDeleteItem, rootDeleteCanonical, mediaType, rootDeleteBody,
							rootDelete, requirement, cleanup);
					AsyncDeadline rootDeleteDeadline = delete(
							new OwnedResource(rootDeleteCanonical, collection.kind() == SYSTEM, null),
							collection.kind() == SYSTEM, requirement);
					if (rootDeleteDeadline == null) {
						assertGone(rootDeleteItem, requirement);
					}
					else {
						awaitCompoundOrSkip(rootDeleteDeadline, requirement,
								"queued custom root DELETE propagation to " + rootDeleteItem,
								() -> gone(rootDeleteCanonical, rootDeleteDeadline, requirement),
								() -> gone(rootDeleteItem, rootDeleteDeadline, requirement));
					}

					Map<String, Object> occurrenceBody = body(collection.kind(), mediaType, "occurrence-delete",
							uid("custom-occurrence-delete"));
					OwnedResource occurrence = createOwned(collection.itemsUri(), occurrenceBody, mediaType,
							collection.kind(), requirement, cleanup, collection.kind() == SYSTEM);
					String occurrenceId = lastPathSegment(occurrence.uri(), requirement);
					URI collectionItem = childCollection(collection.itemsUri(), encoded(occurrenceId));
					URI canonical = canonicalUri(collection.kind(), occurrence.uri(), requirement);
					cleanup.push("canonical custom-created resource " + canonical,
							() -> cleanupDelete(canonical, collection.kind() == SYSTEM, requirement));
					registerAndVerifyOccurrence(collectionItem, canonical, mediaType, occurrenceBody, occurrence,
							requirement, cleanup);
					assertOptions(collectionItem, List.of("DELETE"), requirement);
					Response delete = EncodingMediatypeWrite.givenWithoutDefaultCharset()
						.accept(mediaType)
						.delete(collectionItem)
						.andReturn();
					assertStatusIn(delete, List.of(200, 202, 204), requirement, "DELETE " + collectionItem);
					if (delete.getStatusCode() == 202) {
						AsyncDeadline deadline = new AsyncDeadline();
						awaitCompoundOrSkip(deadline, requirement, "queued DELETE " + collectionItem,
								() -> gone(collectionItem, deadline, requirement),
								() -> available(canonical, deadline, requirement));
					}
					else {
						assertGone(collectionItem, requirement);
						assertAvailable(canonical, requirement);
					}
				}
			}
		});
	}

	/**
	 * Executes abstract test A.78.
	 */
	public void resourcesAddToCustomCollections() {
		String requirement = REQ_BASE + "add-to-collection";
		List<CustomCollection> collections = prepareCustomCollections(requirement);
		executeWithCleanup(requirement, cleanup -> {
			for (CustomCollection collection : collections) {
				for (String mediaType : collection.mediaTypes()) {
					Map<String, Object> body = body(collection.kind(), mediaType, "add", uid("custom-add"));
					OwnedResource canonical = canonicalOwned(
							createOwned(this.apiRoot.resolve(collection.kind().path()), body, mediaType,
									collection.kind(), requirement, cleanup, collection.kind() == SYSTEM),
							collection.kind(), requirement, cleanup);
					assertOptions(collection.itemsUri(), List.of("POST"), requirement);
					String id = lastPathSegment(canonical.uri(), requirement);
					URI collectionItem = childCollection(collection.itemsUri(), encoded(id));
					Map<String, Object> expected = getJson(canonical.uri(), mediaType, 200, requirement);
					OccurrenceCleanupTarget occurrence = new OccurrenceCleanupTarget(collectionItem, false, mediaType,
							expected);
					cleanup.push("custom collection occurrence " + collectionItem,
							() -> cleanupOccurrence(occurrence, requirement));
					Response add = EncodingMediatypeWrite.givenWithoutDefaultCharset()
						.accept("application/json")
						.contentType("text/uri-list")
						.body(canonical.uri() + "\n")
						.post(collection.itemsUri())
						.andReturn();
					assertStatusIn(add, List.of(201, 202), requirement, "POST text/uri-list " + collection.itemsUri());
					String location = add.getHeader("Location");
					if (add.getStatusCode() == 201 && (location == null || location.isBlank())) {
						ETSAssert.failWithUri(requirement,
								"POST text/uri-list " + collection.itemsUri() + " returned HTTP 201 without Location.");
					}
					URI returnedItem = collectionItem;
					if (location != null && !location.isBlank()) {
						URI suppliedLocation = add.getStatusCode() == 202
								? resolveLocationReference(this.apiRoot, location, requirement)
								: resolveCreatedResourceUri(this.apiRoot, location, requirement);
						if (isDirectCollectionItem(collection.itemsUri(), suppliedLocation)) {
							returnedItem = suppliedLocation;
						}
						else if (add.getStatusCode() == 201) {
							ETSAssert.failWithUri(requirement, "POST text/uri-list " + collection.itemsUri()
									+ " returned HTTP 201 with a Location outside the target collection item namespace.");
						}
					}
					OccurrenceCleanupTarget returnedOccurrence = null;
					if (!returnedItem.equals(collectionItem)) {
						returnedOccurrence = new OccurrenceCleanupTarget(returnedItem, false, mediaType, expected);
						OccurrenceCleanupTarget cleanupTarget = returnedOccurrence;
						cleanup.push("returned custom collection occurrence " + returnedItem,
								() -> cleanupOccurrence(cleanupTarget, requirement));
					}
					if (add.getStatusCode() == 202) {
						AsyncDeadline deadline = new AsyncDeadline();
						OccurrenceCleanupTarget returnedTarget = returnedOccurrence;
						awaitCompoundOrSkip(deadline, requirement, "queued POST text/uri-list " + collection.itemsUri(),
								() -> submittedContentAvailable(collectionItem, mediaType, expected, deadline,
										requirement),
								() -> returnedTarget == null || submittedContentAvailable(returnedTarget.uri, mediaType,
										expected, deadline, requirement));
						occurrence.verified = true;
						if (returnedOccurrence != null) {
							returnedOccurrence.verified = true;
						}
					}
					else {
						Map<String, Object> returned = getJson(returnedItem, mediaType, 200, requirement);
						assertSubmittedContent(expected, returned, requirement);
						Map<String, Object> actual = getJson(collectionItem, mediaType, 200, requirement);
						assertSubmittedContent(expected, actual, requirement);
						occurrence.verified = true;
						if (returnedOccurrence != null) {
							returnedOccurrence.verified = true;
						}
					}
				}
			}
		});
	}

	private Map<String, Object> prepare(String requirement, String condition) {
		Response response = EncodingMediatypeWrite.givenWithoutDefaultCharset()
			.accept("application/json")
			.get(this.apiRoot.resolve("conformance"))
			.andReturn();
		ETSAssert.assertStatus(response, 200, requirement);
		Map<String, Object> body = parseBody(response, requirement, "GET /conformance");
		requireDeclaration(body, CONF_CREATE_REPLACE_DELETE, requirement,
				"IUT does not declare the Part 1 Create/Replace/Delete conformance class.");
		requireDeclaration(body, CONF_API_COMMON, requirement,
				"IUT does not declare the direct Part 1 API Common prerequisite.");
		requireDeclaration(body, CONF_INHERITED_CREATE_REPLACE_DELETE, requirement,
				"IUT does not declare the inherited OGC API Features Part 4 prerequisite.");
		requireDeclaration(body, condition, requirement,
				"conditional resource conformance class is not declared; this procedure is not applicable.");
		ensureMutationAllowed(this.apiRoot, this.mutationTestsEnabled, this.mutationIutPolicy, requirement);
		return body;
	}

	private List<CustomCollection> prepareCustomCollections(String requirement) {
		Map<String, Object> conformance = prepare(requirement, CONF_API_COMMON);
		Response response = EncodingMediatypeWrite.givenWithoutDefaultCharset()
			.accept("application/json")
			.get(this.apiRoot.resolve("collections"))
			.andReturn();
		ETSAssert.assertStatus(response, 200, requirement);
		Map<String, Object> body = parseBody(response, requirement, "GET /collections");
		Object advertised = body.get("collections");
		if (!(advertised instanceof List)) {
			ETSAssert.failWithUri(requirement, "/collections is missing its collections array.");
		}
		List<CustomCollection> result = new ArrayList<>();
		for (Object value : (List<?>) advertised) {
			if (!(value instanceof Map)) {
				ETSAssert.failWithUri(requirement, "/collections contains a non-object collection entry.");
			}
			@SuppressWarnings("unchecked")
			Map<String, Object> collection = (Map<String, Object>) value;
			String id = string(collection.get("id"));
			if (id == null) {
				ETSAssert.failWithUri(requirement, "advertised collection is missing a non-empty string id.");
			}
			for (ResourceKind kind : CUSTOM_KINDS) {
				if (declares(conformance, kind.condition()) && kind.matches(collection)) {
					result.add(new CustomCollection(kind, id,
							this.apiRoot.resolve("collections/" + encoded(id) + "/items"),
							supportedMediaTypes(kind.path(), conformance, requirement)));
				}
			}
		}
		if (result.isEmpty()) {
			throw new SkipException(requirement
					+ " - the IUT advertises no non-root collection for a declared System, Procedure, Deployment, Sampling Feature, or Property type; no custom-collection evidence exists.");
		}
		return List.copyOf(result);
	}

	private void transactions(ResourceKind kind, URI collection, Map<String, Object> conformance, String requirement,
			CleanupStack cleanup) {
		for (String mediaType : supportedMediaTypes(kind.path(), conformance, requirement)) {
			String identity = uid(kind.path());
			transaction(kind, collection, body(kind, mediaType, "create", identity),
					stableIdentity -> body(kind, mediaType, "replace", stableIdentity), mediaType, requirement,
					cleanup);
		}
	}

	private void transaction(ResourceKind kind, URI collection, Map<String, Object> createBody,
			BodyFactory replacementFactory, String mediaType, String requirement, CleanupStack cleanup) {
		assertOptions(collection, List.of("POST"), requirement);
		OwnedResource resource = canonicalOwned(
				createOwned(collection, createBody, mediaType, kind, requirement, cleanup, kind == SYSTEM), kind,
				requirement, cleanup);
		verifySubmittedContent(resource.uri(), mediaType, createBody, resource.deadline(), requirement,
				"queued canonical creation at " + resource.uri());

		assertOptions(resource.uri(), List.of("PUT", "DELETE"), requirement);
		String identity = resourceIdentity(createBody, requirement);
		Map<String, Object> replacement = replacementFactory.create(identity);
		Response replace = request(mediaType, replacement, kind, requirement).put(resource.uri()).andReturn();
		assertStatusIn(replace, List.of(200, 202, 204), requirement, "PUT " + resource.uri());

		if (replace.getStatusCode() == 202) {
			awaitSubmittedContentOrSkip(resource.uri(), mediaType, replacement, new AsyncDeadline(), requirement,
					"queued PUT " + resource.uri());
		}
		else {
			Map<String, Object> replaced = getJson(resource.uri(), mediaType, 200, requirement);
			assertSubmittedContent(replacement, replaced, requirement);
		}
		if (Objects.equals(createBody, replacement)) {
			ETSAssert.failWithUri(requirement, "replacement fixture did not change representation content.");
		}

		delete(resource, false, requirement);
	}

	private OwnedResource createOnly(URI collection, Map<String, Object> body, String mediaType, ResourceKind kind,
			String requirement, CleanupStack cleanup, boolean cascadeCleanup) {
		assertOptions(collection, List.of("POST"), requirement);
		OwnedResource created = canonicalOwned(
				createOwned(collection, body, mediaType, kind, requirement, cleanup, cascadeCleanup), kind, requirement,
				cleanup);
		verifySubmittedContent(created.uri(), mediaType, body, created.deadline(), requirement,
				"queued canonical creation at " + created.uri());
		return created;
	}

	private OwnedResource createOwned(URI collection, Map<String, Object> body, String mediaType, ResourceKind kind,
			String requirement, CleanupStack cleanup, boolean cascadeCleanup) {
		io.restassured.specification.RequestSpecification create = request(mediaType, body, kind, requirement);
		String identity = resourceIdentity(body, requirement);
		OwnedCleanupTarget cleanupTarget = new OwnedCleanupTarget(kind, mediaType, identity, cascadeCleanup);
		cleanup.push("created " + kind.name() + " identity " + identity,
				() -> cleanupOwned(cleanupTarget, requirement));
		Response response = create.post(collection).andReturn();
		assertStatusIn(response, List.of(201, 202), requirement, "POST " + collection);
		String location = response.getHeader("Location");
		if (response.getStatusCode() == 201 && (location == null || location.isBlank())) {
			ETSAssert.failWithUri(requirement, "POST " + collection + " returned HTTP 201 without Location.");
		}
		if (response.getStatusCode() == 202) {
			cleanupTarget.queued = true;
			AsyncDeadline deadline = new AsyncDeadline();
			Optional<URI> completed = awaitCreatedResource(cleanupTarget, body, deadline, requirement);
			if (completed.isEmpty()) {
				throw inconclusive(requirement, "POST " + collection);
			}
			URI resourceUri = completed.get();
			cleanupTarget.setVerifiedUri(resourceUri);
			return new OwnedResource(resourceUri, cascadeCleanup, deadline);
		}
		URI resourceUri = resolveCreatedResourceUri(this.apiRoot, location, requirement);
		Map<String, Object> returned = getJson(resourceUri, mediaType, 200, requirement);
		assertResourceIdentity(returned, identity, resourceUri, requirement);
		assertSubmittedContent(body, returned, requirement);
		cleanupTarget.setVerifiedUri(resourceUri);
		return new OwnedResource(resourceUri, cascadeCleanup, null);
	}

	private void registerAndVerifyOccurrence(URI occurrence, URI canonical, String mediaType,
			Map<String, Object> submitted, OwnedResource created, String requirement, CleanupStack cleanup) {
		OccurrenceCleanupTarget target = new OccurrenceCleanupTarget(occurrence, false, mediaType, submitted);
		cleanup.push("custom collection occurrence " + occurrence, () -> cleanupOccurrence(target, requirement));
		target.verified = occurrence.equals(created.uri());
		if (created.deadline() == null) {
			assertSubmittedContent(submitted, getJson(occurrence, mediaType, 200, requirement), requirement);
			assertSubmittedContent(submitted, getJson(canonical, mediaType, 200, requirement), requirement);
		}
		else {
			AsyncDeadline deadline = created.deadline();
			awaitCompoundOrSkip(deadline, requirement, "queued custom-collection creation at " + occurrence,
					() -> submittedContentAvailable(occurrence, mediaType, submitted, deadline, requirement),
					() -> submittedContentAvailable(canonical, mediaType, submitted, deadline, requirement));
		}
		target.verified = true;
	}

	private io.restassured.specification.RequestSpecification request(String mediaType, Map<String, Object> body,
			ResourceKind kind, String requirement) {
		validateGeneratedBody(body, kind.path(), mediaType, requirement);
		return EncodingMediatypeWrite.givenWithoutDefaultCharset().accept(mediaType).contentType(mediaType).body(body);
	}

	public static void validateGeneratedBody(Map<String, Object> body, String resourcePath, String mediaType,
			String requirement) {
		String schema = generatedBodySchema(resourcePath, mediaType);
		if (schema == null) {
			ETSAssert.failWithUri(requirement,
					"no released schema is mapped for generated " + resourcePath + " body with " + mediaType + ".");
		}
		try {
			JsonSchema jsonSchema = SCHEMA_FACTORY.getSchema(SchemaLocation.of(schema), schemaConfig());
			Set<ValidationMessage> errors = jsonSchema.validate(JSON.valueToTree(body));
			if (!errors.isEmpty()) {
				String joined = errors.stream()
					.limit(8)
					.map(ValidationMessage::getMessage)
					.collect(Collectors.joining("; "));
				ETSAssert.failWithUri(requirement,
						"generated " + resourcePath + " write body failed the released schema: " + joined);
			}
		}
		catch (RuntimeException ex) {
			ETSAssert.failWithUri(requirement,
					"generated " + resourcePath + " write body could not be validated against the released schema "
							+ schema + ": " + ex.getMessage() + ".");
		}
	}

	private static String generatedBodySchema(String resourcePath, String mediaType) {
		if (GEOJSON.equals(mediaType)
				&& Set.of("systems", "deployments", "procedures", "samplingFeatures").contains(resourcePath)) {
			String name = "samplingFeatures".equals(resourcePath) ? "samplingFeature" : singular(resourcePath);
			return LOCAL_SCHEMA_PREFIX + "connected-systems-1/geojson/" + name + ".json";
		}
		if (SENSORML.equals(mediaType)
				&& Set.of("systems", "deployments", "procedures", "properties").contains(resourcePath)) {
			return LOCAL_SCHEMA_PREFIX + "connected-systems-1/sensorml/" + singular(resourcePath) + ".json";
		}
		return null;
	}

	private static SchemaValidatorsConfig schemaConfig() {
		SchemaValidatorsConfig config = new SchemaValidatorsConfig();
		config.setFormatAssertionsEnabled(true);
		return config;
	}

	private void assertOptions(URI uri, List<String> expectedMethods, String requirement) {
		Response response = EncodingMediatypeWrite.givenWithoutDefaultCharset().accept("*/*").options(uri).andReturn();
		ETSAssert.assertStatus(response, 200, requirement);
		String allow = response.getHeader("Allow");
		List<String> advertised = allow == null ? List.of()
				: Arrays.stream(allow.split(","))
					.map(String::trim)
					.map(value -> value.toUpperCase(Locale.ROOT))
					.toList();
		for (String method : expectedMethods) {
			if (!advertised.contains(method)) {
				ETSAssert.failWithUri(requirement,
						"OPTIONS " + uri + " must advertise " + method + " in Allow; received " + allow + ".");
			}
		}
	}

	private void assertDeleteConflict(URI system, String requirement) {
		Response response = EncodingMediatypeWrite.givenWithoutDefaultCharset()
			.accept("application/json")
			.queryParam("cascade", false)
			.delete(system)
			.andReturn();
		ETSAssert.assertStatus(response, 409, requirement);
	}

	private AsyncDeadline delete(OwnedResource resource, boolean cascade, String requirement) {
		RequestSpecification request = EncodingMediatypeWrite.givenWithoutDefaultCharset().accept("application/json");
		if (cascade) {
			request.queryParam("cascade", true);
		}
		Response response = request.delete(resource.uri()).andReturn();
		assertStatusIn(response, List.of(200, 202, 204), requirement, "DELETE " + resource.uri());
		if (response.getStatusCode() == 202) {
			AsyncDeadline deadline = new AsyncDeadline();
			awaitGoneOrSkip(resource.uri(), deadline, requirement, "queued DELETE " + resource.uri());
			return deadline;
		}
		assertGone(resource.uri(), requirement);
		return null;
	}

	private void cleanupDelete(URI resource, boolean cascade, String requirement) {
		RequestSpecification request = EncodingMediatypeWrite.givenWithoutDefaultCharset().accept("application/json");
		if (cascade) {
			request.queryParam("cascade", true);
		}
		Response response = request.delete(resource).andReturn();
		assertStatusIn(response, List.of(200, 202, 204, 404), requirement, "cleanup DELETE " + resource);
		AsyncDeadline deadline = new AsyncDeadline(cleanupTimeoutMillis(), cleanupPollMillis());
		if (response.getStatusCode() == 202 && !awaitGone(resource, deadline, requirement)) {
			ETSAssert.failWithUri(requirement,
					resource + " remained available after the bounded asynchronous cleanup DELETE.");
		}
	}

	private void cleanupOccurrence(OccurrenceCleanupTarget target, String requirement) {
		if (!target.verified) {
			AsyncDeadline deadline = new AsyncDeadline(cleanupTimeoutMillis(), cleanupPollMillis());
			if (!pollUntil(deadline, () -> submittedContentAvailable(target.uri, target.mediaType, target.submitted,
					deadline, requirement), requirement, "late custom-collection occurrence " + target.uri)) {
				return;
			}
		}
		cleanupDelete(target.uri, target.cascade, requirement);
	}

	private void cleanupOwned(OwnedCleanupTarget target, String requirement) {
		if (target.verifiedUri != null) {
			cleanupDelete(target.verifiedUri, target.cascade, requirement);
		}
		Optional<URI> discovered = target.queued && target.verifiedUri == null
				? awaitOwnedDiscovery(target, requirement) : discoverOwned(target, requirement);
		if (discovered.isPresent() && !discovered.get().equals(target.verifiedUri)) {
			cleanupDelete(discovered.get(), target.cascade, requirement);
		}
	}

	private Optional<URI> discoverOwned(OwnedCleanupTarget target, String requirement) {
		return discoverOwned(target, null, requirement);
	}

	private Optional<URI> discoverOwned(OwnedCleanupTarget target, AsyncDeadline deadline, String requirement) {
		URI rootCollection = this.apiRoot.resolve(target.kind.path());
		Optional<TraversalResult> traversal = deadline == null
				? Part1ApiCommonSupport.resourcesAtEndpoint(rootCollection, target.mediaType, Map.of(), requirement)
				: Part1ApiCommonSupport.resourcesAtEndpoint(rootCollection, target.mediaType, Map.of(), requirement,
						Set.of(), ignored -> {
						}, (uri, accept, query) -> pollingGet(uri, accept, query, deadline));
		if (traversal.isEmpty()) {
			return Optional.empty();
		}
		List<URI> matches = new ArrayList<>();
		for (Map<String, Object> item : traversal.get().items()) {
			if (!target.identity.equals(optionalResourceIdentity(item))) {
				continue;
			}
			String id = string(item.get("id"));
			if (id == null) {
				ETSAssert.failWithUri(requirement, "cleanup discovery found " + target.kind.name() + " identity "
						+ target.identity + " without a local id; refusing an unverified DELETE.");
			}
			URI candidate = this.apiRoot.resolve(target.kind.path() + "/" + encoded(id));
			Response response = deadline == null ? get(candidate, target.mediaType)
					: pollingGet(candidate, target.mediaType, Map.of(), deadline);
			if (response.getStatusCode() == 404) {
				continue;
			}
			ETSAssert.assertStatus(response, 200, requirement);
			Map<String, Object> representation = parseBody(response, requirement, "GET " + candidate);
			if (target.identity.equals(optionalResourceIdentity(representation))) {
				matches.add(candidate);
			}
		}
		if (matches.size() > 1) {
			ETSAssert.failWithUri(requirement,
					"cleanup discovery found multiple " + target.kind.name() + " resources with submitted identity "
							+ target.identity + "; refusing ambiguous DELETE operations.");
		}
		return matches.stream().findFirst();
	}

	private Optional<URI> awaitOwnedDiscovery(OwnedCleanupTarget target, String requirement) {
		URI[] result = new URI[1];
		AsyncDeadline deadline = new AsyncDeadline(cleanupTimeoutMillis(), cleanupPollMillis());
		pollUntil(deadline, () -> {
			Optional<URI> discovered = discoverOwned(target, deadline, requirement);
			result[0] = discovered.orElse(null);
			return result[0] != null;
		}, requirement, "identity discovery for " + target.identity);
		return Optional.ofNullable(result[0]);
	}

	private Optional<URI> awaitCreatedResource(OwnedCleanupTarget target, Map<String, Object> submitted,
			AsyncDeadline deadline, String requirement) {
		URI[] result = new URI[1];
		pollUntil(deadline, () -> {
			Optional<URI> discovered = discoverOwned(target, deadline, requirement);
			if (discovered.isPresent() && resourceMatches(discovered.get(), target.mediaType, target.identity,
					submitted, deadline, requirement)) {
				result[0] = discovered.get();
				return true;
			}
			return false;
		}, requirement, "queued creation of " + target.identity);
		return Optional.ofNullable(result[0]);
	}

	private boolean resourceMatches(URI resource, String mediaType, String identity, Map<String, Object> submitted,
			AsyncDeadline deadline, String requirement) {
		Response response = pollingGet(resource, mediaType, Map.of(), deadline);
		if (response.getStatusCode() == 404) {
			return false;
		}
		ETSAssert.assertStatus(response, 200, requirement);
		Map<String, Object> body = parseBody(response, requirement, "GET " + resource);
		return identity.equals(optionalResourceIdentity(body)) && submittedContentMatches(submitted, body);
	}

	private void awaitSubmittedContentOrSkip(URI resource, String mediaType, Map<String, Object> submitted,
			AsyncDeadline deadline, String requirement, String operation) {
		boolean completed = pollUntil(deadline,
				() -> submittedContentAvailable(resource, mediaType, submitted, deadline, requirement), requirement,
				operation);
		if (!completed) {
			throw inconclusive(requirement, operation);
		}
	}

	private void verifySubmittedContent(URI resource, String mediaType, Map<String, Object> submitted,
			AsyncDeadline deadline, String requirement, String operation) {
		if (deadline == null) {
			assertSubmittedContent(submitted, getJson(resource, mediaType, 200, requirement), requirement);
		}
		else {
			awaitSubmittedContentOrSkip(resource, mediaType, submitted, deadline, requirement, operation);
		}
	}

	private void awaitGoneOrSkip(URI resource, AsyncDeadline deadline, String requirement, String operation) {
		if (!awaitGone(resource, deadline, requirement)) {
			throw inconclusive(requirement, operation);
		}
	}

	private boolean awaitGone(URI resource, AsyncDeadline deadline, String requirement) {
		return pollUntil(deadline, () -> gone(resource, deadline, requirement), requirement, "deletion of " + resource);
	}

	private void awaitAvailableOrSkip(URI resource, AsyncDeadline deadline, String requirement, String operation) {
		if (!pollUntil(deadline, () -> available(resource, deadline, requirement), requirement, operation)) {
			throw inconclusive(requirement, operation);
		}
	}

	private boolean available(URI resource, AsyncDeadline deadline, String requirement) {
		Response response = pollingGet(resource, "application/json", Map.of(), deadline);
		if (response.getStatusCode() == 404) {
			return false;
		}
		ETSAssert.assertStatus(response, 200, requirement);
		return true;
	}

	private boolean gone(URI resource, AsyncDeadline deadline, String requirement) {
		Response response = pollingGet(resource, "application/json", Map.of(), deadline);
		if (response.getStatusCode() == 404) {
			return true;
		}
		ETSAssert.assertStatus(response, 200, requirement);
		return false;
	}

	private boolean submittedContentAvailable(URI resource, String mediaType, Map<String, Object> submitted,
			AsyncDeadline deadline, String requirement) {
		Response response = pollingGet(resource, mediaType, Map.of(), deadline);
		if (response.getStatusCode() == 404) {
			return false;
		}
		ETSAssert.assertStatus(response, 200, requirement);
		return submittedContentMatches(submitted, parseBody(response, requirement, "GET " + resource));
	}

	private void awaitCascadePostconditionsOrSkip(URI deployment, String deploymentMediaType, String deletedUid,
			URI deleted, String survivorUid, URI survivor, AsyncDeadline deadline, String requirement) {
		awaitCompoundOrSkip(deadline, requirement, "queued cascade graph propagation for " + deleted,
				() -> gone(deleted, deadline, requirement), () -> {
					Response deploymentResponse = pollingGet(deployment, deploymentMediaType, Map.of(), deadline);
					if (deploymentResponse.getStatusCode() != 200) {
						return false;
					}
					Map<String, Object> body = parseBody(deploymentResponse, requirement, "GET " + deployment);
					return !containsString(body, deletedUid) && !containsString(body, deleted)
							&& (containsString(body, survivorUid) || containsString(body, survivor));
				}, () -> available(survivor, deadline, requirement));
	}

	private void awaitCompoundOrSkip(AsyncDeadline deadline, String requirement, String operation,
			BooleanSupplier... conditions) {
		boolean[] previousComplete = { false };
		boolean complete = pollUntil(deadline, () -> {
			boolean currentComplete = true;
			for (BooleanSupplier condition : conditions) {
				if (!deadline.canStartRequest()) {
					return false;
				}
				currentComplete = condition.getAsBoolean() && currentComplete;
			}
			boolean stable = currentComplete && previousComplete[0];
			previousComplete[0] = currentComplete;
			return stable;
		}, requirement, operation);
		if (!complete) {
			throw inconclusive(requirement, operation);
		}
	}

	private void assertDeploymentAfterCascade(URI deployment, String deploymentMediaType, String deletedUid,
			URI deleted, String survivorUid, URI survivor, String requirement) {
		Map<String, Object> remaining = getJson(deployment, deploymentMediaType, 200, requirement);
		if (containsString(remaining, deletedUid) || containsString(remaining, deleted)) {
			ETSAssert.failWithUri(requirement,
					"surviving Deployment still references the cascade-deleted System " + deleted + ".");
		}
		if (!containsString(remaining, survivorUid) && !containsString(remaining, survivor)) {
			ETSAssert.failWithUri(requirement,
					"surviving Deployment lost the unrelated System association " + survivor + ".");
		}
	}

	private boolean pollUntil(AsyncDeadline deadline, BooleanSupplier condition, String requirement, String operation) {
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
			if (!deadline.canStartRequest()) {
				return false;
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

	private Response pollingGet(URI uri, String accept, Map<String, String> query, AsyncDeadline deadline) {
		int timeoutMillis = deadline.requestTimeoutMillis();
		HttpClientConfig httpClient = HttpClientConfig.httpClientConfig()
			.setParam("http.connection.timeout", timeoutMillis)
			.setParam("http.socket.timeout", timeoutMillis)
			.setParam("http.connection-manager.timeout", (long) timeoutMillis);
		RestAssuredConfig config = RestAssuredConfig.config().httpClient(httpClient);
		RequestSpecification request = EncodingMediatypeWrite.givenWithoutDefaultCharset()
			.config(config)
			.accept(accept);
		query.forEach(request::queryParam);
		return request.get(uri).andReturn();
	}

	private static Response get(URI uri, String accept) {
		return EncodingMediatypeWrite.givenWithoutDefaultCharset().accept(accept).get(uri).andReturn();
	}

	private static boolean causedByTimeout(Throwable thrown) {
		for (Throwable cause = thrown; cause != null; cause = cause.getCause()) {
			if (cause instanceof SocketTimeoutException || cause.getClass().getSimpleName().contains("Timeout")) {
				return true;
			}
		}
		return false;
	}

	private static void failIfInterrupted(String requirement, String operation) {
		if (Thread.currentThread().isInterrupted()) {
			ETSAssert.failWithUri(requirement, "interrupted while waiting for " + operation + ".");
		}
	}

	private long cleanupTimeoutMillis() {
		return this.asyncTimeoutMillis > Long.MAX_VALUE / 2L ? Long.MAX_VALUE : this.asyncTimeoutMillis * 2L;
	}

	private long cleanupPollMillis() {
		return Math.min(this.asyncPollMillis, 25L);
	}

	private static SkipException inconclusive(String requirement, String operation) {
		return new SkipException(requirement + " - " + operation
				+ " returned HTTP 202, but its required postcondition was not observed within the configured timeout; the operation is accepted-but-inconclusive, not positive lifecycle evidence.");
	}

	private void assertAvailable(URI resource, String requirement) {
		Response response = EncodingMediatypeWrite.givenWithoutDefaultCharset()
			.accept("application/json")
			.get(resource)
			.andReturn();
		ETSAssert.assertStatus(response, 200, requirement);
	}

	private void assertGone(URI resource, String requirement) {
		Response response = EncodingMediatypeWrite.givenWithoutDefaultCharset()
			.accept("application/json")
			.get(resource)
			.andReturn();
		ETSAssert.assertStatus(response, 404, requirement);
	}

	private static void assertContainsAssociation(Map<String, Object> deployment, String uid, URI resource,
			String label, String requirement) {
		if (!containsString(deployment, uid) && !containsString(deployment, resource)) {
			ETSAssert.failWithUri(requirement, "pre-delete Deployment does not reference the " + label + " System "
					+ resource + "; cascade behavior cannot be assessed.");
		}
	}

	private Map<String, Object> getJson(URI resource, String mediaType, int status, String requirement) {
		Response response = EncodingMediatypeWrite.givenWithoutDefaultCharset()
			.accept(mediaType)
			.get(resource)
			.andReturn();
		ETSAssert.assertStatus(response, status, requirement);
		return parseBody(response, requirement, "GET " + resource);
	}

	private Map<String, Object> parseBody(Response response, String requirement, String operation) {
		try {
			Map<String, Object> body = response.jsonPath().getMap("$");
			if (body == null) {
				ETSAssert.failWithUri(requirement, operation + " did not return a JSON object.");
			}
			return body;
		}
		catch (RuntimeException ex) {
			ETSAssert.failWithUri(requirement, operation + " did not return parseable JSON: " + ex.getMessage() + ".");
			return Map.of();
		}
	}

	private void requireDeclaration(Map<String, Object> body, String conformanceClass, String requirement,
			String reason) {
		if (!declares(body, conformanceClass)) {
			throw new SkipException(requirement + " - " + reason + " Missing exact URI " + conformanceClass
					+ ". No POST, PUT, or DELETE request was issued.");
		}
	}

	private static boolean declares(Map<String, Object> body, String conformanceClass) {
		Object value = body.get("conformsTo");
		return value instanceof List && ((List<?>) value).contains(conformanceClass);
	}

	static List<String> supportedMediaTypes(String resourcePath, Map<String, Object> conformance, String requirement) {
		List<String> mediaTypes = new ArrayList<>();
		boolean featureResource = "systems".equals(resourcePath) || "deployments".equals(resourcePath)
				|| "procedures".equals(resourcePath) || "samplingFeatures".equals(resourcePath);
		boolean sensorMlResource = "systems".equals(resourcePath) || "deployments".equals(resourcePath)
				|| "procedures".equals(resourcePath) || "properties".equals(resourcePath);
		if (featureResource && declares(conformance, CONF_GEOJSON)) {
			mediaTypes.add(GEOJSON);
		}
		if (sensorMlResource && declares(conformance, CONF_SENSORML)) {
			mediaTypes.add(SENSORML);
		}
		if (mediaTypes.isEmpty()) {
			throw new SkipException(requirement + " - no applicable declared GeoJSON or SensorML representation is "
					+ "available for " + resourcePath + "; no write request was issued.");
		}
		return List.copyOf(mediaTypes);
	}

	private static String preferredMediaType(ResourceKind kind, Map<String, Object> conformance, String requirement) {
		List<String> supported = supportedMediaTypes(kind.path(), conformance, requirement);
		if (kind == DEPLOYMENT && supported.contains(SENSORML)) {
			return SENSORML;
		}
		return supported.get(0);
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

	/**
	 * Enforces the explicit ownership gate before any write.
	 * @param apiRoot API root.
	 * @param enabled mutation flag.
	 * @param policy ownership policy.
	 * @param requirement owning requirement.
	 */
	public static void ensureMutationAllowed(URI apiRoot, String enabled, String policy, String requirement) {
		if (!ENABLED.equals(enabled) || !DEDICATED_POLICY.equals(policy)) {
			throw new SkipException(requirement + " - mutation procedures are disabled. Set "
					+ TestRunArg.MUTATION_TESTS_ENABLED + "=true and " + TestRunArg.MUTATION_IUT_POLICY + "="
					+ DEDICATED_POLICY + ". No POST, PUT, or DELETE request was issued.");
		}
		String host = apiRoot.getHost();
		if ("api.georobotix.io".equalsIgnoreCase(host)) {
			throw new SkipException(requirement
					+ " - the known shared public GeoRobotix IUT is hard-denied for mutation. No POST, PUT, or DELETE request was issued.");
		}
	}

	/**
	 * Resolves a Location value and rejects credential-bearing cross-origin follow-up
	 * requests.
	 * @param apiRoot normalized API root.
	 * @param location Location value.
	 * @param requirement owning requirement.
	 * @return same-origin absolute URI.
	 */
	public static URI resolveCreatedResourceUri(URI apiRoot, String location, String requirement) {
		URI resolved = resolveLocationReference(apiRoot, location, requirement);
		if (!sameOrigin(apiRoot, resolved)) {
			ETSAssert.failWithUri(requirement,
					"refusing cross-origin Location from " + apiRoot + " to " + resolved + ".");
		}
		return resolved;
	}

	private static URI resolveLocationReference(URI apiRoot, String location, String requirement) {
		URI supplied;
		try {
			supplied = URI.create(location);
		}
		catch (IllegalArgumentException ex) {
			ETSAssert.failWithUri(requirement, "POST returned an invalid Location value: " + location + ".");
			return apiRoot;
		}
		URI resolved;
		if (supplied.isAbsolute()) {
			resolved = supplied;
		}
		else if (location.startsWith("/")) {
			String rootPath = apiRoot.getPath();
			boolean containsRoot = rootPath != null && !rootPath.isBlank() && !"/".equals(rootPath)
					&& (location.equals(stripTrailingSlash(rootPath))
							|| location.startsWith(stripTrailingSlash(rootPath) + "/"));
			resolved = containsRoot ? apiRoot.resolve(location) : apiRoot.resolve(location.substring(1));
		}
		else {
			resolved = apiRoot.resolve(location);
		}
		return resolved;
	}

	private static boolean isDirectCollectionItem(URI itemsUri, URI candidate) {
		if (!sameOrigin(itemsUri, candidate)) {
			return false;
		}
		String itemsPath = stripTrailingSlash(itemsUri.getRawPath());
		String candidatePath = candidate.getRawPath();
		if (candidatePath == null || !candidatePath.startsWith(itemsPath + "/")) {
			return false;
		}
		String relative = candidatePath.substring(itemsPath.length() + 1);
		return !relative.isBlank() && !relative.contains("/");
	}

	/**
	 * Compares submitted JSON recursively while allowing server-managed extra members.
	 * @param submitted submitted representation.
	 * @param received dereferenced representation.
	 * @param requirement owning requirement.
	 */
	static void assertSubmittedContent(Object submitted, Object received, String requirement) {
		assertSubmittedContent(submitted, received, "$", requirement);
	}

	private static boolean submittedContentMatches(Object submitted, Object received) {
		if (submitted instanceof Map) {
			if (!(received instanceof Map)) {
				return false;
			}
			Map<?, ?> receivedMap = (Map<?, ?>) received;
			return ((Map<?, ?>) submitted).entrySet()
				.stream()
				.allMatch(entry -> receivedMap.containsKey(entry.getKey())
						&& submittedContentMatches(entry.getValue(), receivedMap.get(entry.getKey())));
		}
		if (submitted instanceof List) {
			if (!(received instanceof List)) {
				return false;
			}
			List<?> expected = (List<?>) submitted;
			List<?> actual = (List<?>) received;
			if (expected.size() != actual.size()) {
				return false;
			}
			for (int index = 0; index < expected.size(); index++) {
				if (!submittedContentMatches(expected.get(index), actual.get(index))) {
					return false;
				}
			}
			return true;
		}
		return submitted instanceof Number && received instanceof Number
				? new BigDecimal(submitted.toString()).compareTo(new BigDecimal(received.toString())) == 0
				: Objects.equals(submitted, received);
	}

	private static void assertSubmittedContent(Object submitted, Object received, String path, String requirement) {
		if (submitted instanceof Map) {
			if (!(received instanceof Map)) {
				ETSAssert.failWithUri(requirement, path + " must remain a JSON object.");
			}
			Map<?, ?> receivedMap = (Map<?, ?>) received;
			for (Map.Entry<?, ?> entry : ((Map<?, ?>) submitted).entrySet()) {
				String key = String.valueOf(entry.getKey());
				if (!receivedMap.containsKey(key)) {
					ETSAssert.failWithUri(requirement, path + "." + key + " is missing from the returned resource.");
				}
				assertSubmittedContent(entry.getValue(), receivedMap.get(key), path + "." + key, requirement);
			}
			return;
		}
		if (submitted instanceof List) {
			if (!(received instanceof List)) {
				ETSAssert.failWithUri(requirement, path + " must remain a JSON array.");
			}
			List<?> expected = (List<?>) submitted;
			List<?> actual = (List<?>) received;
			if (expected.size() != actual.size()) {
				ETSAssert.failWithUri(requirement,
						path + " array size changed from " + expected.size() + " to " + actual.size() + ".");
			}
			for (int index = 0; index < expected.size(); index++) {
				assertSubmittedContent(expected.get(index), actual.get(index), path + "[" + index + "]", requirement);
			}
			return;
		}
		boolean equal = submitted instanceof Number && received instanceof Number
				? new BigDecimal(submitted.toString()).compareTo(new BigDecimal(received.toString())) == 0
				: Objects.equals(submitted, received);
		if (!equal) {
			ETSAssert.failWithUri(requirement,
					path + " changed from " + String.valueOf(submitted) + " to " + String.valueOf(received) + ".");
		}
	}

	/**
	 * Builds a valid temporary GeoJSON System.
	 * @param phase phase marker.
	 * @param uid stable UID.
	 * @return representation.
	 */
	public static Map<String, Object> systemBody(String phase, String uid) {
		Map<String, Object> properties = new LinkedHashMap<>();
		properties.put("uid", uid);
		properties.put("featureType", "http://www.w3.org/ns/sosa/System");
		properties.put("name", "ETS Create/Replace/Delete System " + phase);
		properties.put("description", "Temporary System owned by the ETS at " + Instant.now() + ".");
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("type", "Feature");
		body.put("geometry", Map.of("type", "Point", "coordinates", List.of(-77.0365, 38.8977)));
		body.put("properties", properties);
		return body;
	}

	/**
	 * Builds a schema-valid temporary representation for a released root resource.
	 * @param resourcePath canonical collection path.
	 * @param mediaType declared write representation.
	 * @param phase phase marker.
	 * @param uid stable external identity.
	 * @return mutable JSON representation.
	 */
	public static Map<String, Object> generatedBody(String resourcePath, String mediaType, String phase, String uid) {
		ResourceKind kind = switch (resourcePath) {
			case "systems" -> SYSTEM;
			case "deployments" -> DEPLOYMENT;
			case "procedures" -> PROCEDURE;
			case "samplingFeatures" -> SAMPLING_FEATURE;
			case "properties" -> PROPERTY;
			default -> throw new IllegalArgumentException("unsupported canonical resource path: " + resourcePath);
		};
		return body(kind, mediaType, phase, uid);
	}

	private static Map<String, Object> sensorMlSystemBody(String phase, String uid) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("type", "PhysicalSystem");
		body.put("definition", "http://www.w3.org/ns/sosa/System");
		body.put("uniqueId", uid);
		body.put("label", "ETS Create/Replace/Delete System " + phase);
		body.put("description", "Temporary System owned by the ETS.");
		return body;
	}

	private static Map<String, Object> sensorMlDeploymentBody(String phase, String uid, List<String> systemUids) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("type", "Deployment");
		body.put("definition", "http://www.w3.org/ns/sosa/Deployment");
		body.put("uniqueId", uid);
		body.put("label", "ETS Create/Replace/Delete Deployment " + phase);
		body.put("description", "Temporary Deployment owned by the ETS.");
		body.put("validTime", List.of("2026-01-01T00:00:00Z", "2027-01-01T00:00:00Z"));
		if (!systemUids.isEmpty()) {
			List<Map<String, Object>> deployed = new ArrayList<>();
			for (int index = 0; index < systemUids.size(); index++) {
				deployed.add(Map.of("name", "system-" + index, "system", Map.of("href", systemUids.get(index))));
			}
			body.put("deployedSystems", deployed);
		}
		return body;
	}

	private static Map<String, Object> geoJsonDeploymentBody(String phase, String uid, List<String> systemUids) {
		Map<String, Object> properties = new LinkedHashMap<>();
		properties.put("uid", uid);
		properties.put("featureType", "http://www.w3.org/ns/sosa/Deployment");
		properties.put("name", "ETS Create/Replace/Delete Deployment " + phase);
		properties.put("description", "Temporary Deployment owned by the ETS.");
		properties.put("validTime", List.of("2026-01-01T00:00:00Z", "2027-01-01T00:00:00Z"));
		if (!systemUids.isEmpty()) {
			properties.put("deployedSystems@link",
					systemUids.stream().map(systemUid -> Map.of("href", systemUid, "uid", systemUid)).toList());
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("type", "Feature");
		body.put("geometry", null);
		body.put("properties", properties);
		return body;
	}

	private static Map<String, Object> geoJsonProcedureBody(String phase, String uid) {
		Map<String, Object> properties = new LinkedHashMap<>();
		properties.put("uid", uid);
		properties.put("featureType", "http://www.w3.org/ns/sosa/Procedure");
		properties.put("name", "ETS Create/Replace/Delete Procedure " + phase);
		properties.put("description", "Temporary Procedure owned by the ETS.");
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("type", "Feature");
		body.put("geometry", null);
		body.put("properties", properties);
		return body;
	}

	private static Map<String, Object> sensorMlProcedureBody(String phase, String uid) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("type", "SimpleProcess");
		body.put("definition", "http://www.w3.org/ns/sosa/Procedure");
		body.put("uniqueId", uid);
		body.put("label", "ETS Create/Replace/Delete Procedure " + phase);
		body.put("description", "Temporary Procedure owned by the ETS.");
		return body;
	}

	private static Map<String, Object> samplingFeatureBody(String phase, String uid) {
		Map<String, Object> properties = new LinkedHashMap<>();
		properties.put("uid", uid);
		properties.put("featureType", "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint");
		properties.put("name", "ETS Create/Replace/Delete Sampling Feature " + phase);
		properties.put("description", "Temporary Sampling Feature owned by the ETS.");
		properties.put("sampledFeature@link",
				Map.of("href", "http://sweetontology.net/realm/Atmosphere", "title", "Ambient Air"));
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("type", "Feature");
		body.put("geometry", Map.of("type", "Point", "coordinates", List.of(-77.037, 38.898)));
		body.put("properties", properties);
		return body;
	}

	private static Map<String, Object> propertyBody(String phase, String uid) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("uniqueId", uid);
		body.put("label", "ETS Create/Replace/Delete Property " + phase);
		body.put("description", "Temporary Property owned by the ETS.");
		body.put("baseProperty", "https://qudt.org/vocab/quantitykind/Temperature");
		return body;
	}

	private static Map<String, Object> body(ResourceKind kind, String mediaType, String phase, String uid) {
		return body(kind, mediaType, phase, uid, List.of());
	}

	private static Map<String, Object> body(ResourceKind kind, String mediaType, String phase, String uid,
			List<String> systemUids) {
		if (kind == SYSTEM) {
			return SENSORML.equals(mediaType) ? sensorMlSystemBody(phase, uid) : systemBody(phase, uid);
		}
		if (kind == DEPLOYMENT) {
			return SENSORML.equals(mediaType) ? sensorMlDeploymentBody(phase, uid, systemUids)
					: geoJsonDeploymentBody(phase, uid, systemUids);
		}
		if (kind == PROCEDURE) {
			return SENSORML.equals(mediaType) ? sensorMlProcedureBody(phase, uid) : geoJsonProcedureBody(phase, uid);
		}
		if (kind == SAMPLING_FEATURE) {
			return samplingFeatureBody(phase, uid);
		}
		return propertyBody(phase, uid);
	}

	private static String resourceIdentity(Map<String, Object> body, String requirement) {
		String identity = optionalResourceIdentity(body);
		if (identity != null) {
			return identity;
		}
		ETSAssert.failWithUri(requirement, "submitted representation does not expose uid or uniqueId.");
		return "";
	}

	private static String optionalResourceIdentity(Map<String, Object> body) {
		String direct = string(body.get("uniqueId"));
		if (direct != null) {
			return direct;
		}
		Object properties = body.get("properties");
		return properties instanceof Map ? string(((Map<?, ?>) properties).get("uid")) : null;
	}

	private static void assertResourceIdentity(Map<String, Object> body, String expected, URI resource,
			String requirement) {
		String actual = optionalResourceIdentity(body);
		if (!expected.equals(actual)) {
			ETSAssert.failWithUri(requirement, "POST Location " + resource + " resolved to identity " + actual
					+ " instead of submitted identity " + expected + "; refusing destructive cleanup at that URI.");
		}
	}

	private OwnedResource canonicalOwned(OwnedResource created, ResourceKind kind, String requirement,
			CleanupStack cleanup) {
		URI canonical = canonicalUri(kind, created.uri(), requirement);
		if (!canonical.equals(created.uri())) {
			cleanup.push("canonical created resource " + canonical,
					() -> cleanupDelete(canonical, created.cascadeCleanup(), requirement));
		}
		return new OwnedResource(canonical, created.cascadeCleanup(), created.deadline());
	}

	private URI canonicalUri(ResourceKind kind, URI location, String requirement) {
		String id = lastPathSegment(location, requirement);
		return this.apiRoot.resolve(kind.path() + "/" + encoded(id));
	}

	private static String singular(String resourcePath) {
		if ("properties".equals(resourcePath)) {
			return "property";
		}
		return resourcePath.substring(0, resourcePath.length() - 1);
	}

	private static URI childCollection(URI parent, String childName) {
		return URI.create(stripTrailingSlash(parent.toString()) + "/" + childName);
	}

	private static boolean containsString(Object value, Object expected) {
		if (value instanceof Map) {
			return ((Map<?, ?>) value).values().stream().anyMatch(item -> containsString(item, expected));
		}
		if (value instanceof List) {
			return ((List<?>) value).stream().anyMatch(item -> containsString(item, expected));
		}
		return expected != null && Objects.equals(String.valueOf(expected), value);
	}

	private static String uid(String scope) {
		return "urn:ets:ogcapi-connectedsystems10:crd:" + scope + ":" + UUID.randomUUID();
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

	private static String encoded(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
	}

	private static String string(Object value) {
		return value instanceof String && !((String) value).isBlank() ? (String) value : null;
	}

	private static String stripTrailingSlash(String value) {
		return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
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

	private static boolean sameOrigin(URI left, URI right) {
		return left.getScheme() != null && right.getScheme() != null
				&& left.getScheme().equalsIgnoreCase(right.getScheme()) && left.getHost() != null
				&& right.getHost() != null && left.getHost().equalsIgnoreCase(right.getHost())
				&& effectivePort(left) == effectivePort(right);
	}

	private static int effectivePort(URI uri) {
		if (uri.getPort() >= 0) {
			return uri.getPort();
		}
		return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
	}

	private static void assertStatusIn(Response response, List<Integer> expected, String requirement,
			String operation) {
		if (!expected.contains(response.getStatusCode())) {
			ETSAssert.failWithUri(requirement,
					operation + " expected HTTP status in " + expected + ", got " + response.getStatusCode() + ".");
		}
	}

	/**
	 * Reverse-order cleanup aggregator.
	 */
	static final class CleanupStack {

		private final String requirement;

		private final Deque<CleanupAction> actions = new ArrayDeque<>();

		CleanupStack(String requirement) {
			this.requirement = requirement;
		}

		void push(String label, ThrowingAction action) {
			this.actions.push(new CleanupAction(label, action));
		}

		Throwable close(Throwable primary) {
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
							+ " - accepted operation was inconclusive and owned-resource cleanup failed.");
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

	private record ResourceKind(String name, String path, String condition, String featureType, boolean property) {

		boolean matches(Map<String, Object> collection) {
			return this.property ? this.featureType.equals(collection.get("itemType"))
					: "feature".equals(collection.get("itemType"))
							&& this.featureType.equals(collection.get("featureType"));
		}

	}

	private record CustomCollection(ResourceKind kind, String id, URI itemsUri, List<String> mediaTypes) {
	}

	private record OwnedResource(URI uri, boolean cascadeCleanup, AsyncDeadline deadline) {
	}

	private final class AsyncDeadline {

		private static final long MINIMUM_REQUEST_SLICE_MILLIS = 250L;

		private final long startedNanos = CreateReplaceDeleteSupport.this.nanoTime.getAsLong();

		private final long timeoutNanos;

		private final long pollNanos;

		private AsyncDeadline() {
			this(CreateReplaceDeleteSupport.this.asyncTimeoutMillis, CreateReplaceDeleteSupport.this.asyncPollMillis);
		}

		private AsyncDeadline(long timeoutMillis, long pollMillis) {
			this.timeoutNanos = TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
			this.pollNanos = TimeUnit.MILLISECONDS.toNanos(pollMillis);
		}

		private long remainingNanos() {
			return this.timeoutNanos - (CreateReplaceDeleteSupport.this.nanoTime.getAsLong() - this.startedNanos);
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

	private static final class OwnedCleanupTarget {

		private final ResourceKind kind;

		private final String mediaType;

		private final String identity;

		private final boolean cascade;

		private URI verifiedUri;

		private boolean queued;

		private OwnedCleanupTarget(ResourceKind kind, String mediaType, String identity, boolean cascade) {
			this.kind = kind;
			this.mediaType = mediaType;
			this.identity = identity;
			this.cascade = cascade;
		}

		private void setVerifiedUri(URI verifiedUri) {
			this.verifiedUri = verifiedUri;
		}

	}

	private static final class OccurrenceCleanupTarget {

		private final URI uri;

		private final boolean cascade;

		private final String mediaType;

		private final Map<String, Object> submitted;

		private boolean verified;

		private OccurrenceCleanupTarget(URI uri, boolean cascade, String mediaType, Map<String, Object> submitted) {
			this.uri = uri;
			this.cascade = cascade;
			this.mediaType = mediaType;
			this.submitted = submitted;
		}

	}

	private record CleanupAction(String label, ThrowingAction action) {
	}

	@FunctionalInterface
	private interface Procedure {

		void run(CleanupStack cleanup);

	}

	@FunctionalInterface
	private interface BodyFactory {

		Map<String, Object> create(String identity);

	}

	@FunctionalInterface
	interface ThrowingAction {

		void run() throws Throwable;

	}

}
