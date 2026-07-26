package org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.testng.Reporter;
import org.testng.SkipException;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Read-only support for the two released OGC 23-001 API Common supporting tests.
 */
public final class Part1ApiCommonSupport {

	static final String CONF_CANONICAL_RESOURCES = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/api-common/canonical-resources";

	static final String CONF_COLLECTION_ITEMS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/api-common/collection-items";

	private static final int MAX_PAGES = 10_000;

	private static final Set<String> SUPPORTED_JSON_TYPES = Set.of("application/json", "application/geo+json",
			"application/sml+json");

	private static final String IANA_URN_REGISTRY = "/org/opengis/cite/ogcapiconnectedsystems10/registries/iana-urn-namespaces.json";

	private static final String IANA_URN_SOURCE = "https://www.iana.org/assignments/urn-namespaces/urn-namespaces.xml";

	private static final String IANA_URN_SOURCE_SHA256 = "a2c5f8f6bb1e34ea102211b3eff81131c73f2e27a69f90c9d36b48f2471b9604";

	private static final Set<String> REGISTERED_URN_NAMESPACES = loadRegisteredUrnNamespaces();

	private Part1ApiCommonSupport() {
	}

	/**
	 * Implements released supporting test `/conf/api-common/canonical-resources`.
	 * @param apiRoot normalized API root.
	 * @param resourceType Part 1 resource type path token.
	 * @return all resources, or empty when the endpoint returns 404.
	 */
	public static Optional<List<Map<String, Object>>> canonicalResources(URI apiRoot, String resourceType) {
		return canonicalResourcesDetailed(apiRoot, resourceType).map(TraversalResult::items);
	}

	static Optional<List<Map<String, Object>>> canonicalResources(URI apiRoot, String resourceType,
			Requester requester) {
		return canonicalResourcesDetailed(apiRoot, resourceType, requester).map(TraversalResult::items);
	}

	/**
	 * Retrieves canonical resources and preserves each representation page as evidence.
	 * @param apiRoot normalized API root.
	 * @param resourceType Part 1 resource type path token.
	 * @return traversal evidence, or empty when the endpoint returns 404.
	 */
	public static Optional<TraversalResult> canonicalResourcesDetailed(URI apiRoot, String resourceType) {
		return canonicalResourcesDetailed(apiRoot, resourceType, Part1ApiCommonSupport::get);
	}

	/**
	 * Traverses an arbitrary read-only collection endpoint with the same bounded,
	 * same-origin pagination rules used by the released API Common procedures.
	 * @param endpoint absolute collection endpoint.
	 * @param accept HTTP Accept value.
	 * @param query immutable query parameters for the first page.
	 * @param requirement requirement URI owning the request.
	 * @return traversal evidence, or empty when the endpoint returns HTTP 404.
	 */
	public static Optional<TraversalResult> resourcesAtEndpoint(URI endpoint, String accept, Map<String, String> query,
			String requirement) {
		return resourcesAtEndpoint(endpoint, accept, query, requirement, Set.of());
	}

	/**
	 * Traverses an arbitrary read-only collection endpoint, gating every page by actual
	 * response media type before parsing.
	 * @param endpoint absolute collection endpoint.
	 * @param accept HTTP Accept value.
	 * @param query immutable query parameters for the first page.
	 * @param requirement requirement URI owning the request.
	 * @param supportedMediaTypes allowed actual response media types; an empty set
	 * disables the additional gate.
	 * @return traversal evidence, or empty when the endpoint returns HTTP 404.
	 */
	public static Optional<TraversalResult> resourcesAtEndpoint(URI endpoint, String accept, Map<String, String> query,
			String requirement, Set<String> supportedMediaTypes) {
		if (endpoint == null || !endpoint.isAbsolute()) {
			throw new IllegalArgumentException("endpoint must be an absolute URI");
		}
		if (accept == null || accept.isBlank()) {
			throw new IllegalArgumentException("accept must not be blank");
		}
		if (requirement == null || requirement.isBlank()) {
			throw new IllegalArgumentException("requirement must not be blank");
		}
		Set<String> mediaTypes = normalizedMediaTypes(supportedMediaTypes);
		Map<String, String> parameters = query == null ? Map.of() : Map.copyOf(query);
		Response first = get(endpoint, accept, parameters);
		if (first == null) {
			ETSAssert.failWithUri(requirement, endpoint + " returned no HTTP response.");
		}
		if (first.getStatusCode() == 404) {
			return Optional.empty();
		}
		return Optional
			.of(traverse(endpoint, accept, parameters, Part1ApiCommonSupport::get, first, requirement, mediaTypes));
	}

	static Optional<TraversalResult> canonicalResourcesDetailed(URI apiRoot, String resourceType, Requester requester) {
		requireApiRoot(apiRoot);
		if (resourceType == null || resourceType.isBlank() || resourceType.contains("/")
				|| resourceType.contains("..")) {
			throw new IllegalArgumentException("resourceType must be a non-empty path token");
		}
		URI endpoint = apiRoot.resolve(resourceType);
		String accept = canonicalAccept(resourceType);
		Response first = requester.get(endpoint, accept, Map.of());
		if (first == null) {
			ETSAssert.failWithUri(CONF_CANONICAL_RESOURCES, endpoint + " returned no HTTP response.");
		}
		if (first.getStatusCode() == 404) {
			Reporter.log(CONF_CANONICAL_RESOURCES + " - IUT does not support resource type " + resourceType
					+ " (canonical endpoint returned HTTP 404).", true);
			return Optional.empty();
		}
		return Optional.of(traverse(endpoint, accept, Map.of(), requester, first, CONF_CANONICAL_RESOURCES, Set.of()));
	}

	/**
	 * Implements released supporting test `/conf/api-common/collection-items`.
	 * @param apiRoot normalized API root.
	 * @param collection advertised collection metadata.
	 * @return all items, or empty when no supported items media type is advertised.
	 */
	public static Optional<List<Map<String, Object>>> collectionItems(URI apiRoot, Map<String, Object> collection) {
		return collectionItemsDetailed(apiRoot, collection).map(TraversalResult::items);
	}

	static Optional<List<Map<String, Object>>> collectionItems(URI apiRoot, Map<String, Object> collection,
			Requester requester) {
		return collectionItems(apiRoot, collection, Map.of(), requester);
	}

	static Optional<List<Map<String, Object>>> collectionItems(URI apiRoot, Map<String, Object> collection,
			Map<String, String> query, Requester requester) {
		return collectionItemsDetailed(apiRoot, collection, query, requester).map(TraversalResult::items);
	}

	/**
	 * Retrieves advertised collection items and preserves each representation page as
	 * evidence.
	 * @param apiRoot normalized API root.
	 * @param collection advertised collection metadata.
	 * @return traversal evidence, or empty when no supported items media type is
	 * advertised.
	 */
	public static Optional<TraversalResult> collectionItemsDetailed(URI apiRoot, Map<String, Object> collection) {
		return collectionItemsDetailed(apiRoot, collection, Map.of(), Part1ApiCommonSupport::get);
	}

	static Optional<TraversalResult> collectionItemsDetailed(URI apiRoot, Map<String, Object> collection,
			Requester requester) {
		return collectionItemsDetailed(apiRoot, collection, Map.of(), requester);
	}

	static Optional<TraversalResult> collectionItemsDetailed(URI apiRoot, Map<String, Object> collection,
			Map<String, String> query, Requester requester) {
		requireApiRoot(apiRoot);
		if (collection == null) {
			throw new IllegalArgumentException("collection must not be null");
		}
		Object idValue = collection.get("id");
		if (!(idValue instanceof String) || ((String) idValue).isBlank()) {
			ETSAssert.failWithUri(CONF_COLLECTION_ITEMS, "advertised collection is missing a non-empty string id.");
		}
		String id = (String) idValue;
		Optional<String> mediaType = supportedItemsMediaType(collection);
		if (mediaType.isEmpty()) {
			Reporter.log(CONF_COLLECTION_ITEMS + " - collection " + id
					+ " has no rel=items link with a JSON media type supported by this ETS; collection skipped.", true);
			return Optional.empty();
		}
		String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8).replace("+", "%20");
		URI endpoint = apiRoot.resolve("collections/" + encodedId + "/items");
		return Optional.of(traverse(endpoint, mediaType.orElseThrow(), query == null ? Map.of() : query, requester,
				null, CONF_COLLECTION_ITEMS, Set.of()));
	}

	static Optional<String> resourceUid(Map<String, Object> resource) {
		if (resource == null) {
			return Optional.empty();
		}
		Object uniqueId = resource.get("uniqueId");
		if (uniqueId instanceof String && !((String) uniqueId).isBlank()) {
			return Optional.of((String) uniqueId);
		}
		Object properties = resource.get("properties");
		if (properties instanceof Map) {
			Object nested = ((Map<?, ?>) properties).get("uid");
			if (nested instanceof String && !((String) nested).isBlank()) {
				return Optional.of((String) nested);
			}
		}
		Object direct = resource.get("uid");
		if (direct instanceof String && !((String) direct).isBlank()) {
			return Optional.of((String) direct);
		}
		return Optional.empty();
	}

	static boolean isValidAbsoluteUri(String value) {
		if (value == null || value.isBlank()) {
			return false;
		}
		try {
			URI uri = URI.create(value);
			return uri.isAbsolute() && uri.getSchemeSpecificPart() != null && !uri.getSchemeSpecificPart().isBlank();
		}
		catch (IllegalArgumentException ex) {
			return false;
		}
	}

	static boolean isRecommendedUid(String value) {
		if (!isValidAbsoluteUri(value)) {
			return false;
		}
		URI uri = URI.create(value);
		if (!"urn".equalsIgnoreCase(uri.getScheme())) {
			return false;
		}
		String[] parts = uri.getSchemeSpecificPart().split(":", 2);
		if (parts.length != 2) {
			return false;
		}
		String namespace = parts[0].toLowerCase(Locale.ROOT);
		if (!REGISTERED_URN_NAMESPACES.contains(namespace)) {
			return false;
		}
		if ("uuid".equals(namespace)) {
			try {
				UUID uuid = UUID.fromString(parts[1]);
				if (parts[1].length() != 36 || !uuid.toString().equalsIgnoreCase(parts[1])) {
					return false;
				}
			}
			catch (IllegalArgumentException ex) {
				return false;
			}
		}
		return true;
	}

	static List<DatetimeQuery> datetimeQueries(Map<String, Object> collection) {
		for (List<?> interval : temporalIntervals(collection)) {
			if (interval.size() != 2) {
				throw new IllegalArgumentException("Temporal interval must contain exactly two bounds");
			}
			Instant begin = parseOptionalInstant(interval.get(0));
			Instant end = parseOptionalInstant(interval.get(1));
			if (begin == null && end == null) {
				continue;
			}
			if (begin == null) {
				begin = end.minusSeconds(1);
			}
			if (end == null) {
				end = begin.plusSeconds(1);
			}
			if (end.isBefore(begin)) {
				throw new IllegalArgumentException("Temporal interval end precedes its begin");
			}
			Duration duration = Duration.between(begin, end);
			Instant anchor = begin.plus(duration.dividedBy(2));
			return List.of(new DatetimeQuery(anchor.toString(), anchor, anchor),
					new DatetimeQuery(begin + "/" + end, begin, end), new DatetimeQuery("../" + anchor, null, anchor),
					new DatetimeQuery(anchor + "/..", anchor, null));
		}
		return List.of();
	}

	static boolean isTimeless(Map<String, Object> feature) {
		return validTime(feature) == null;
	}

	static boolean validTimeIntersects(Map<String, Object> feature, DatetimeQuery query, Instant requestTime) {
		if (query == null || requestTime == null) {
			throw new IllegalArgumentException("query and requestTime must not be null");
		}
		Object validTime = validTime(feature);
		if (validTime == null) {
			return true;
		}
		Instant begin;
		Instant end;
		if (validTime instanceof String) {
			begin = parseValidTimeBound(validTime, requestTime);
			end = begin;
		}
		else {
			if (!(validTime instanceof List) || ((List<?>) validTime).size() != 2) {
				throw new IllegalArgumentException("validTime must be an ISO instant or a two-bound interval");
			}
			List<?> interval = (List<?>) validTime;
			begin = parseOptionalValidTimeBound(interval.get(0), requestTime);
			end = parseOptionalValidTimeBound(interval.get(1), requestTime);
		}
		if (begin != null && end != null && end.isBefore(begin)) {
			throw new IllegalArgumentException("validTime end precedes its begin");
		}
		return (end == null || query.begin() == null || !end.isBefore(query.begin()))
				&& (query.end() == null || begin == null || !query.end().isBefore(begin));
	}

	private static TraversalResult traverse(URI initial, String accept, Map<String, String> initialQuery,
			Requester requester, Response firstResponse, String requirement, Set<String> supportedMediaTypes) {
		if (requester == null) {
			throw new IllegalArgumentException("requester must not be null");
		}
		List<Map<String, Object>> items = new ArrayList<>();
		List<PageDocument> pages = new ArrayList<>();
		Set<URI> visited = new LinkedHashSet<>();
		URI current = initial;
		Map<String, String> query = initialQuery;
		Response response = firstResponse;

		while (current != null) {
			if (visited.size() >= MAX_PAGES) {
				ETSAssert.failWithUri(requirement, "pagination exceeded the " + MAX_PAGES + " page safety bound.");
			}
			if (!visited.add(current)) {
				ETSAssert.failWithUri(requirement, "pagination cycle detected at " + current + ".");
			}
			if (response == null) {
				response = requester.get(current, accept, query);
			}
			if (response == null) {
				ETSAssert.failWithUri(requirement, current + " returned no HTTP response.");
			}
			ETSAssert.assertStatus(response, 200, requirement);
			requireSupportedMediaType(response, current, requirement, supportedMediaTypes);
			Map<String, Object> body = parseObject(response, current, requirement);
			List<?> pageItems = pageItems(response, body, current, requirement);
			List<Map<String, Object>> typedPageItems = new ArrayList<>();
			for (Object item : pageItems) {
				if (!(item instanceof Map)) {
					ETSAssert.failWithUri(requirement,
							current + " representation collection array contains a non-object value.");
				}
				@SuppressWarnings("unchecked")
				Map<String, Object> typed = (Map<String, Object>) item;
				items.add(typed);
				typedPageItems.add(typed);
			}
			pages.add(new PageDocument(current, responseMediaType(response), body, typedPageItems));
			URI next = nextUri(current, body, requirement);
			if (next != null && !sameOrigin(initial, next)) {
				ETSAssert.failWithUri(requirement,
						"refusing cross-origin pagination from " + initial + " to " + next + ".");
			}
			current = next;
			query = Map.of();
			response = null;
		}
		return new TraversalResult(items, pages);
	}

	private static Set<String> normalizedMediaTypes(Set<String> supportedMediaTypes) {
		if (supportedMediaTypes == null || supportedMediaTypes.isEmpty()) {
			return Set.of();
		}
		Set<String> normalized = new LinkedHashSet<>();
		for (String mediaType : supportedMediaTypes) {
			if (mediaType == null || mediaType.isBlank()) {
				throw new IllegalArgumentException("supportedMediaTypes must not contain blank values");
			}
			normalized.add(mediaType.trim().toLowerCase(Locale.ROOT));
		}
		return Collections.unmodifiableSet(normalized);
	}

	private static void requireSupportedMediaType(Response response, URI source, String requirement,
			Set<String> supportedMediaTypes) {
		if (supportedMediaTypes.isEmpty()) {
			return;
		}
		String mediaType = responseMediaType(response);
		if (supportedMediaTypes.contains(mediaType)) {
			return;
		}
		String detail = mediaType.isEmpty() ? "no Content-Type" : "unsupported media type '" + mediaType + "'";
		Reporter.log(requirement + " - " + source + " returned " + detail + "; representation parsing skipped.", true);
		throw new SkipException(
				requirement + " - " + source + " returned " + detail + "; representation parsing skipped.");
	}

	private static List<?> pageItems(Response response, Map<String, Object> body, URI source, String requirement) {
		String mediaType = responseMediaType(response);
		String member;
		if ("application/geo+json".equals(mediaType)) {
			member = "features";
		}
		else if ("application/sml+json".equals(mediaType)) {
			member = "items";
		}
		else if ("application/json".equals(mediaType)
				|| mediaType.startsWith("application/") && mediaType.endsWith("+json")) {
			member = body.get("items") instanceof List ? "items" : "features";
		}
		else {
			ETSAssert.failWithUri(requirement,
					source + " returned unsupported collection media type '" + mediaType + "'.");
			return List.of();
		}
		Object values = body.get(member);
		if (!(values instanceof List)) {
			ETSAssert.failWithUri(requirement,
					source + " " + mediaType + " response is missing a " + member + " array.");
		}
		return (List<?>) values;
	}

	private static String responseMediaType(Response response) {
		String contentType = response.getContentType();
		if (contentType == null || contentType.isBlank()) {
			return "";
		}
		return contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
	}

	private static URI nextUri(URI current, Map<String, Object> body, String requirement) {
		Object links = body.get("links");
		if (links == null) {
			return null;
		}
		if (!(links instanceof List)) {
			ETSAssert.failWithUri(requirement, current + " links value is not an array.");
		}
		Set<URI> nextUris = new HashSet<>();
		for (Object value : (List<?>) links) {
			if (!(value instanceof Map)) {
				continue;
			}
			Map<?, ?> link = (Map<?, ?>) value;
			if (!hasRelation(link.get("rel"), "next")) {
				continue;
			}
			Object href = link.get("href");
			if (!(href instanceof String) || ((String) href).isBlank()) {
				ETSAssert.failWithUri(requirement, current + " next link is missing a non-empty href.");
			}
			try {
				nextUris.add(current.resolve((String) href));
			}
			catch (IllegalArgumentException ex) {
				ETSAssert.failWithUri(requirement, current + " next link has an invalid href: " + href + ".");
			}
		}
		if (nextUris.size() > 1) {
			ETSAssert.failWithUri(requirement, current + " advertises multiple distinct next links: " + nextUris + ".");
		}
		return nextUris.stream().findFirst().orElse(null);
	}

	private static Optional<String> supportedItemsMediaType(Map<String, Object> collection) {
		Object links = collection.get("links");
		if (!(links instanceof List)) {
			return Optional.empty();
		}
		for (Object value : (List<?>) links) {
			if (!(value instanceof Map)) {
				continue;
			}
			Map<?, ?> link = (Map<?, ?>) value;
			if (!hasRelation(link.get("rel"), "items")) {
				continue;
			}
			Object type = link.get("type");
			if (!(type instanceof String)) {
				continue;
			}
			String normalized = ((String) type).split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
			if (SUPPORTED_JSON_TYPES.contains(normalized)
					|| normalized.startsWith("application/") && normalized.endsWith("+json")) {
				return Optional.of(normalized);
			}
		}
		return Optional.empty();
	}

	private static String canonicalAccept(String resourceType) {
		return switch (resourceType) {
			case "systems", "deployments", "procedures" ->
				"application/geo+json, application/sml+json, application/json";
			case "samplingFeatures" -> "application/geo+json, application/json";
			case "properties" -> "application/sml+json, application/json";
			default -> "application/json";
		};
	}

	private static boolean hasRelation(Object value, String expected) {
		if (value instanceof String) {
			return expected.equalsIgnoreCase((String) value);
		}
		if (value instanceof List) {
			return ((List<?>) value).stream()
				.anyMatch(relation -> relation instanceof String && expected.equalsIgnoreCase((String) relation));
		}
		return false;
	}

	private static Map<String, Object> parseObject(Response response, URI source, String requirement) {
		try {
			Map<String, Object> body = response.jsonPath().getMap("$");
			if (body == null) {
				ETSAssert.failWithUri(requirement, source + " response body is not a JSON object.");
			}
			return body;
		}
		catch (Exception ex) {
			ETSAssert.failWithUri(requirement,
					source + " response body is not parseable as a JSON object: " + ex.getMessage());
			return Map.of();
		}
	}

	private static List<List<?>> temporalIntervals(Map<String, Object> collection) {
		if (collection == null || !(collection.get("extent") instanceof Map)) {
			return List.of();
		}
		Object temporal = ((Map<?, ?>) collection.get("extent")).get("temporal");
		Object intervals = temporal instanceof Map ? ((Map<?, ?>) temporal).get("interval") : temporal;
		if (!(intervals instanceof List)) {
			return List.of();
		}
		List<?> values = (List<?>) intervals;
		if (values.size() == 2 && values.stream().noneMatch(List.class::isInstance)) {
			return List.of(values);
		}
		List<List<?>> result = new ArrayList<>();
		for (Object value : values) {
			if (value instanceof List) {
				result.add((List<?>) value);
			}
		}
		return result;
	}

	private static Object validTime(Map<String, Object> feature) {
		if (feature == null) {
			return null;
		}
		if (feature.containsKey("validTime")) {
			return feature.get("validTime");
		}
		Object properties = feature.get("properties");
		if (properties instanceof Map) {
			return ((Map<?, ?>) properties).get("validTime");
		}
		return null;
	}

	private static Instant parseOptionalInstant(Object value) {
		if (value == null) {
			return null;
		}
		return parseInstant(value);
	}

	private static Instant parseOptionalValidTimeBound(Object value, Instant requestTime) {
		if (value == null) {
			return null;
		}
		return parseValidTimeBound(value, requestTime);
	}

	private static Instant parseValidTimeBound(Object value, Instant requestTime) {
		if ("now".equals(value)) {
			return requestTime;
		}
		return parseInstant(value);
	}

	private static Instant parseInstant(Object value) {
		if (!(value instanceof String) || ((String) value).isBlank()) {
			throw new IllegalArgumentException("Temporal bound must be an ISO-8601 string or null");
		}
		try {
			return Instant.parse((String) value);
		}
		catch (DateTimeParseException ex) {
			throw new IllegalArgumentException("Invalid ISO-8601 temporal bound: " + value, ex);
		}
	}

	private static void requireApiRoot(URI apiRoot) {
		if (apiRoot == null || !apiRoot.isAbsolute() || !apiRoot.toString().endsWith("/")) {
			throw new IllegalArgumentException("apiRoot must be an absolute URI ending with '/'");
		}
	}

	private static boolean sameOrigin(URI left, URI right) {
		return left.getScheme().equalsIgnoreCase(right.getScheme()) && left.getHost() != null && right.getHost() != null
				&& left.getHost().equalsIgnoreCase(right.getHost()) && effectivePort(left) == effectivePort(right);
	}

	private static int effectivePort(URI uri) {
		if (uri.getPort() >= 0) {
			return uri.getPort();
		}
		return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
	}

	private static Set<String> loadRegisteredUrnNamespaces() {
		try (var input = Part1ApiCommonSupport.class.getResourceAsStream(IANA_URN_REGISTRY)) {
			if (input == null) {
				throw new IllegalStateException("Missing bundled IANA URN namespace registry " + IANA_URN_REGISTRY);
			}
			JsonNode root = new ObjectMapper().readTree(input);
			if (!"1.0".equals(root.path("schemaVersion").asText())
					|| !IANA_URN_SOURCE.equals(root.path("source").asText())
					|| !"2026-07-26".equals(root.path("retrievedOn").asText())
					|| !"2026-05-28".equals(root.path("registryUpdated").asText())
					|| !IANA_URN_SOURCE_SHA256.equals(root.path("sourceSha256").asText())
					|| !root.path("formalNamespaces").isArray() || !root.path("informalNamespaces").isArray()) {
				throw new IllegalStateException("Invalid bundled IANA URN namespace registry metadata");
			}
			Set<String> formal = new LinkedHashSet<>();
			root.path("formalNamespaces").forEach(value -> formal.add(value.asText()));
			Set<String> informal = new LinkedHashSet<>();
			root.path("informalNamespaces").forEach(value -> informal.add(value.asText()));
			Set<String> namespaces = new LinkedHashSet<>(formal);
			namespaces.addAll(informal);
			if (formal.size() != 96 || informal.size() != 8 || namespaces.size() != 104 || !namespaces.contains("uuid")
					|| !namespaces.contains("ogc") || !namespaces.contains("urn-8")) {
				throw new IllegalStateException("Bundled IANA URN namespace registry is incomplete");
			}
			return Set.copyOf(namespaces);
		}
		catch (Exception ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	static Response get(URI uri, String accept, Map<String, String> query) {
		RequestSpecification request = given().accept(accept);
		query.forEach(request::queryParam);
		return request.when().get(uri).andReturn();
	}

	@FunctionalInterface
	interface Requester {

		Response get(URI uri, String accept, Map<String, String> query);

	}

	record DatetimeQuery(String parameter, Instant begin, Instant end) {

		DatetimeQuery {
			if (parameter == null || parameter.isBlank() || begin == null && end == null) {
				throw new IllegalArgumentException("datetime query requires a parameter and at least one bound");
			}
			if (begin != null && end != null && end.isBefore(begin)) {
				throw new IllegalArgumentException("datetime query end precedes its begin");
			}
		}

	}

	/**
	 * Immutable evidence for one retrieved collection page.
	 *
	 * @param source effective page URI.
	 * @param mediaType normalized response media type.
	 * @param body parsed JSON object.
	 * @param items typed items extracted from the representation.
	 */
	public record PageDocument(URI source, String mediaType, Map<String, Object> body,
			List<Map<String, Object>> items) {

		public PageDocument {
			if (source == null || mediaType == null || body == null || items == null) {
				throw new IllegalArgumentException("page evidence values must not be null");
			}
			body = Collections.unmodifiableMap(new LinkedHashMap<>(body));
			items = List.copyOf(items);
		}

	}

	/**
	 * Immutable aggregate for a complete paginated traversal.
	 *
	 * @param items all items in traversal order.
	 * @param pages all retrieved pages in traversal order.
	 */
	public record TraversalResult(List<Map<String, Object>> items, List<PageDocument> pages) {

		public TraversalResult {
			if (items == null || pages == null) {
				throw new IllegalArgumentException("traversal evidence values must not be null");
			}
			items = List.copyOf(items);
			pages = List.copyOf(pages);
		}

	}

}
