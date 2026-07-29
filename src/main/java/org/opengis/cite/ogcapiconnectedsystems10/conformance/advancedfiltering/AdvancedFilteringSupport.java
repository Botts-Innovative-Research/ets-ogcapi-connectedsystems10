package org.opengis.cite.ogcapiconnectedsystems10.conformance.advancedfiltering;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.deployments.DeploymentFeaturesSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.TraversalResult;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.procedures.ProcedureFeaturesSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.propertydefinitions.PropertyDefinitionsSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.samplingfeatures.SamplingFeaturesSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.systemfeatures.SystemFeaturesSupport;
import org.testng.Reporter;
import org.testng.SkipException;

import io.restassured.response.Response;

/**
 * Read-only execution engine for the released OGC 23-001 Advanced Filtering procedures.
 */
public final class AdvancedFilteringSupport {

	static final String CONF_ADVANCED_FILTERING = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/advanced-filtering";

	private static final Set<String> JSON_MEDIA = Set.of("application/json", "application/geo+json",
			"application/sml+json");

	private static final Pattern LOCAL_ID = Pattern.compile("[A-Za-z0-9._~%-]+");

	private static final Pattern WORD = Pattern.compile("[\\p{L}\\p{N}]{4,}");

	private static final ObjectMapper JSON = new ObjectMapper();

	private static final int MAX_REFERENCE_READS = 64;

	private static final Set<String> RESERVED_PROPERTIES = Set.of("id", "type", "geometry", "bbox", "links",
			"properties", "featuretype", "uid", "uniqueid", "name", "label", "description", "definition", "validtime",
			"baseproperty", "objecttype", "position", "location");

	private final URI apiRoot;

	/**
	 * Creates a new independent Advanced Filtering procedure engine.
	 * @param apiRoot normalized absolute API root.
	 */
	public AdvancedFilteringSupport(URI apiRoot) {
		if (apiRoot == null || !apiRoot.isAbsolute()) {
			throw new IllegalArgumentException("apiRoot must be an absolute URI");
		}
		String value = apiRoot.toString();
		this.apiRoot = URI.create(value.endsWith("/") ? value : value + "/");
	}

	/**
	 * Implements `/req/advanced-filtering/id-list-schema`.
	 * @param requirement released target URI.
	 */
	public void idListSchema(String requirement) {
		requireDeclaration(requirement);
		for (String valid : List.of("system-1", "system-1,system-2", "urn:example:system:1",
				"urn:example:system:1,urn:example:system:2", "urn:example:system:*")) {
			if (!isValidIdList(valid)) {
				ETSAssert.failWithUri(requirement, "Expected valid ID_List value was rejected: " + valid);
			}
		}
		for (String invalid : List.of("", ",", "system-1,urn:example:system:1", "urn:example:bad value")) {
			if (isValidIdList(invalid)) {
				ETSAssert.failWithUri(requirement, "Expected invalid ID_List value was accepted: " + invalid);
			}
		}
	}

	/**
	 * Implements `/req/advanced-filtering/resource-by-id`.
	 * @param requirement released target URI.
	 */
	public void resourceById(String requirement) {
		requireDeclaration(requirement);
		Inspection inspection = new Inspection(requirement);
		for (ResourceType type : ResourceType.values()) {
			Optional<TraversalResult> seed = read(type, Map.of(), requirement);
			if (seed.isEmpty()) {
				continue;
			}
			Optional<Map<String, Object>> candidate = seed.get()
				.items()
				.stream()
				.filter(item -> localId(item).isPresent() && uid(item).isPresent())
				.findFirst();
			if (candidate.isEmpty()) {
				inspection.limit(type.path + " has no resource carrying both local ID and UID");
				continue;
			}
			assertIdentifierQuery(type, candidate.get(), localId(candidate.get()).orElseThrow(), requirement);
			assertIdentifierQuery(type, candidate.get(), uid(candidate.get()).orElseThrow(), requirement);
			inspection.exercised();
		}
		inspection.finish();
	}

	/**
	 * Implements `/req/advanced-filtering/resource-by-keyword`.
	 * @param requirement released target URI.
	 */
	public void resourceByKeyword(String requirement) {
		requireDeclaration(requirement);
		Inspection inspection = new Inspection(requirement);
		for (ResourceType type : ResourceType.values()) {
			Optional<TraversalResult> seed = read(type, Map.of(), requirement);
			if (seed.isEmpty()) {
				continue;
			}
			Optional<String> keyword = seed.get()
				.items()
				.stream()
				.map(AdvancedFilteringSupport::keyword)
				.flatMap(Optional::stream)
				.findFirst();
			if (keyword.isEmpty()) {
				inspection.limit(type.path + " has no usable plain-text keyword");
				continue;
			}
			TraversalResult filtered = requiredQuery(type, Map.of("q", keyword.get()), requirement);
			assertNonEmpty(filtered, type, "q", keyword.get(), requirement);
			assertEvery(filtered.items(), item -> containsPlainText(item, keyword.get()), type, "q", keyword.get(),
					requirement);
			inspection.exercised();
		}
		inspection.finish();
	}

	/**
	 * Implements `/rec/advanced-filtering/resource-by-property`.
	 * @param recommendation released recommendation URI.
	 */
	public void resourceByProperty(String recommendation) {
		requireDeclaration(recommendation);
		int exercised = 0;
		for (ResourceType type : ResourceType.values()) {
			Optional<TraversalResult> seed = read(type, Map.of(), recommendation);
			if (seed.isEmpty()) {
				continue;
			}
			Optional<PropertyValue> property = seed.get()
				.items()
				.stream()
				.map(AdvancedFilteringSupport::customProperty)
				.flatMap(Optional::stream)
				.findFirst();
			if (property.isEmpty()) {
				warn(recommendation, type.path + " exposes no scalar custom property suitable for a query");
				continue;
			}
			Optional<TraversalResult> filtered = recommendedQuery(type,
					Map.of(property.get().name, property.get().value), recommendation);
			if (filtered.isEmpty()) {
				continue;
			}
			if (filtered.get().items().isEmpty()) {
				warn(recommendation, type.path + " accepted custom parameter '" + property.get().name
						+ "' but returned no known matching resource");
				continue;
			}
			for (Map<String, Object> item : filtered.get().items()) {
				if (!hasPropertyValue(item, property.get().name, property.get().value)) {
					warn(recommendation,
							type.path + " custom-property result contains a nonmatching resource: " + stableId(item));
				}
			}
			exercised++;
		}
		if (exercised == 0) {
			warn(recommendation, "no canonical endpoint supplied positive custom-property recommendation evidence");
		}
	}

	/**
	 * Implements `/req/advanced-filtering/feature-by-geom`.
	 * @param requirement released target URI.
	 */
	public void featureByGeometry(String requirement) {
		requireDeclaration(requirement);
		Inspection inspection = new Inspection(requirement);
		for (ResourceType type : EnumSet.of(ResourceType.SYSTEMS, ResourceType.DEPLOYMENTS,
				ResourceType.SAMPLING_FEATURES)) {
			Optional<TraversalResult> seed = read(type, Map.of(), requirement);
			if (seed.isEmpty()) {
				continue;
			}
			Optional<String> wkt = seed.get()
				.items()
				.stream()
				.map(AdvancedFilteringSupport::geometryQuery)
				.flatMap(Optional::stream)
				.findFirst();
			if (wkt.isEmpty()) {
				inspection.limit(type.path + " has no resource with usable geometry");
				continue;
			}
			TraversalResult filtered = requiredQuery(type, Map.of("geom", wkt.get()), requirement);
			assertNonEmpty(filtered, type, "geom", wkt.get(), requirement);
			assertEvery(filtered.items(), item -> resourceIntersects(item, wkt.get()), type, "geom", wkt.get(),
					requirement);
			inspection.exercised();
		}
		inspection.finish();
	}

	/**
	 * Implements one mandatory association-filter procedure, including local-ID and UID
	 * repetitions.
	 * @param owner returned canonical resource type.
	 * @param parameter query parameter.
	 * @param relation relation oracle.
	 * @param requirement released target URI.
	 */
	public void association(ResourceType owner, String parameter, Relation relation, String requirement) {
		requireDeclaration(requirement);
		Optional<TraversalResult> seed = read(owner, Map.of(), requirement);
		if (seed.isEmpty()) {
			throw new SkipException(requirement + " - canonical " + owner.path + " endpoint is unsupported.");
		}
		Map<String, Object> candidate = null;
		Identifiers selected = null;
		List<String> limitations = new ArrayList<>();
		for (Map<String, Object> item : seed.get().items()) {
			Identifiers identifiers = relationIdentifiers(owner, item, relation, requirement);
			if (identifiers.local.isEmpty() || identifiers.global.isEmpty()) {
				limitations.add(stableId(item) + " lacks " + (identifiers.local.isEmpty() ? "local-ID" : "UID")
						+ " association evidence");
				continue;
			}
			candidate = item;
			selected = identifiers;
			break;
		}
		if (candidate == null) {
			throw new SkipException(requirement + " - no " + owner.path
					+ " resource supplied both local-ID and UID association evidence after complete inspection"
					+ (limitations.isEmpty() ? "." : ": " + String.join(" | ", limitations)));
		}
		assertAssociationQuery(owner, parameter, relation, selected.local.iterator().next(), requirement);
		assertAssociationQuery(owner, parameter, relation, selected.global.iterator().next(), requirement);
	}

	/**
	 * Implements `/req/advanced-filtering/prop-by-object`.
	 * @param requirement released target URI.
	 */
	public void propertyByObjectType(String requirement) {
		requireDeclaration(requirement);
		Optional<TraversalResult> seed = read(ResourceType.PROPERTIES, Map.of(), requirement);
		if (seed.isEmpty()) {
			throw new SkipException(requirement + " - canonical properties endpoint is unsupported.");
		}
		Optional<String> objectType = seed.get()
			.items()
			.stream()
			.map(item -> scalarProperty(item, "objectType"))
			.flatMap(Optional::stream)
			.filter(AdvancedFilteringSupport::isAbsoluteUri)
			.findFirst();
		if (objectType.isEmpty()) {
			throw new SkipException(requirement + " - no Property resource exposes a URI-valued objectType.");
		}
		TraversalResult filtered = requiredQuery(ResourceType.PROPERTIES, Map.of("objectType", objectType.get()),
				requirement);
		validateEndpoint(ResourceType.PROPERTIES, filtered, requirement);
		assertNonEmpty(filtered, ResourceType.PROPERTIES, "objectType", objectType.get(), requirement);
		assertEvery(filtered.items(), item -> hasPropertyValue(item, "objectType", objectType.get()),
				ResourceType.PROPERTIES, "objectType", objectType.get(), requirement);
	}

	/**
	 * Implements `/req/advanced-filtering/combined-filters` with an ID and keyword
	 * conjunction for every supported canonical endpoint.
	 * @param requirement released target URI.
	 */
	public void combinedFilters(String requirement) {
		requireDeclaration(requirement);
		Inspection inspection = new Inspection(requirement);
		for (ResourceType type : ResourceType.values()) {
			Optional<TraversalResult> seed = read(type, Map.of(), requirement);
			if (seed.isEmpty()) {
				continue;
			}
			Optional<Map<String, Object>> candidate = seed.get()
				.items()
				.stream()
				.filter(item -> localId(item).isPresent() && keyword(item).isPresent())
				.findFirst();
			if (candidate.isEmpty()) {
				inspection.limit(type.path + " has no resource carrying both local ID and keyword evidence");
				continue;
			}
			String id = localId(candidate.get()).orElseThrow();
			String q = keyword(candidate.get()).orElseThrow();
			TraversalResult filtered = requiredQuery(type, Map.of("id", id, "q", q), requirement);
			assertNonEmpty(filtered, type, "id+q", id + "," + q, requirement);
			assertEvery(filtered.items(), item -> hasIdentifier(item, id) && containsPlainText(item, q), type, "id+q",
					id + "," + q, requirement);
			inspection.exercised();
		}
		inspection.finish();
	}

	/**
	 * Implements the transitive base-property recommendation.
	 * @param recommendation released recommendation URI.
	 */
	public void indirectProperty(String recommendation) {
		requireDeclaration(recommendation);
		Optional<TraversalResult> properties = read(ResourceType.PROPERTIES, Map.of(), recommendation);
		if (properties.isEmpty()) {
			warn(recommendation, "canonical properties endpoint is unsupported");
			return;
		}
		Optional<Map<String, Object>> candidate = properties.get()
			.items()
			.stream()
			.filter(item -> localId(item).isPresent() && scalarProperty(item, "baseProperty").isPresent())
			.findFirst();
		if (candidate.isEmpty()) {
			warn(recommendation, "no Property carries both local ID and baseProperty evidence");
			return;
		}
		String propertyId = localId(candidate.get()).orElseThrow();
		String baseProperty = scalarProperty(candidate.get(), "baseProperty").orElseThrow();
		for (ResourceType type : ResourceType.values()) {
			String parameter = type == ResourceType.PROPERTIES ? "baseProperty" : "observedProperty";
			Optional<TraversalResult> direct = recommendedQuery(type, Map.of(parameter, propertyId), recommendation);
			Optional<TraversalResult> transitive = recommendedQuery(type, Map.of(parameter, baseProperty),
					recommendation);
			if (direct.isPresent() && transitive.isPresent()
					&& !containsAllResources(transitive.get().items(), direct.get().items())) {
				warn(recommendation, type.path + " does not include every direct-property result when queried by "
						+ "the transitive base property");
			}
		}
	}

	/**
	 * Implements the transitive feature-of-interest recommendation.
	 * @param recommendation released recommendation URI.
	 */
	public void indirectFeatureOfInterest(String recommendation) {
		requireDeclaration(recommendation);
		Optional<TraversalResult> samplingFeatures = read(ResourceType.SAMPLING_FEATURES, Map.of(), recommendation);
		if (samplingFeatures.isEmpty()) {
			warn(recommendation, "canonical samplingFeatures endpoint is unsupported");
			return;
		}
		Map<String, Object> candidate = null;
		Identifiers parents = null;
		Identifiers ultimate = null;
		for (Map<String, Object> item : samplingFeatures.get().items()) {
			Identifiers parentIds = relationIdentifiers(ResourceType.SAMPLING_FEATURES, item, Relation.SAMPLE_OF,
					recommendation);
			Identifiers ultimateIds = relationIdentifiers(ResourceType.SAMPLING_FEATURES, item,
					Relation.SAMPLED_FEATURE, recommendation);
			if (localId(item).isPresent() && !parentIds.all().isEmpty() && !ultimateIds.all().isEmpty()) {
				candidate = item;
				parents = parentIds;
				ultimate = ultimateIds;
				break;
			}
		}
		if (candidate == null) {
			warn(recommendation,
					"no Sampling Feature carries local ID, sampleOf, and sampledFeature evidence for transitive queries");
			return;
		}
		String sfId = localId(candidate).orElseThrow();
		String parentId = parents.all().iterator().next();
		String ultimateId = ultimate.all().iterator().next();
		Optional<TraversalResult> parentSet = recommendedQuery(ResourceType.SAMPLING_FEATURES, Map.of("foi", parentId),
				recommendation);
		Optional<TraversalResult> ultimateSet = recommendedQuery(ResourceType.SAMPLING_FEATURES,
				Map.of("foi", ultimateId), recommendation);
		if (parentSet.isPresent() && !containsResource(parentSet.get().items(), sfId)) {
			warn(recommendation, "samplingFeatures?foi=<parent> omits " + sfId);
		}
		if (ultimateSet.isPresent() && !containsResource(ultimateSet.get().items(), sfId)) {
			warn(recommendation, "samplingFeatures?foi=<ultimate> omits " + sfId);
		}
		for (ResourceType type : List.of(ResourceType.SYSTEMS, ResourceType.DEPLOYMENTS)) {
			Optional<TraversalResult> direct = recommendedQuery(type, Map.of("foi", sfId), recommendation);
			Optional<TraversalResult> parent = recommendedQuery(type, Map.of("foi", parentId), recommendation);
			Optional<TraversalResult> ultimateResult = recommendedQuery(type, Map.of("foi", ultimateId),
					recommendation);
			warnUnlessContains(type, ultimateResult, direct, "ultimate feature", "sampling feature", recommendation);
			warnUnlessContains(type, ultimateResult, parent, "ultimate feature", "parent feature", recommendation);
			warnUnlessContains(type, parent, direct, "parent feature", "sampling feature", recommendation);
		}
	}

	/**
	 * Validates the OpenAPI ID_List lexical constraints used by released procedures.
	 * @param value serialized query value.
	 * @return true when the list is non-empty, homogeneous, and lexically valid.
	 */
	static boolean isValidIdList(String value) {
		if (value == null || value.isBlank()) {
			return false;
		}
		String[] tokens = value.split(",", -1);
		Boolean uriList = null;
		for (String token : tokens) {
			if (token.isBlank() || !token.equals(token.trim())) {
				return false;
			}
			boolean uri = isUriPattern(token);
			if (!uri && !LOCAL_ID.matcher(token).matches()) {
				return false;
			}
			if (uriList != null && uriList != uri) {
				return false;
			}
			uriList = uri;
		}
		return true;
	}

	static boolean hasIdentifier(Map<String, Object> resource, String identifier) {
		return identifier != null && identifiers(resource).contains(identifier);
	}

	static boolean containsPlainText(Map<String, Object> resource, String keyword) {
		if (resource == null || keyword == null || keyword.isBlank()) {
			return false;
		}
		String normalized = keyword.toLowerCase(Locale.ROOT);
		return plainTextStrings(resource).stream()
			.anyMatch(value -> value.toLowerCase(Locale.ROOT).contains(normalized));
	}

	static boolean hasPropertyValue(Map<String, Object> resource, String property, String expected) {
		return scalarProperty(resource, property).filter(expected::equals).isPresent();
	}

	static boolean containsAllResources(List<Map<String, Object>> superset, List<Map<String, Object>> subset) {
		Set<String> available = stableIds(superset);
		Set<String> required = stableIds(subset);
		return required.size() == subset.size() && available.containsAll(required);
	}

	static boolean intersects(Map<String, Object> geometry, String wkt) {
		if (geometry == null || wkt == null || wkt.isBlank()) {
			return false;
		}
		try {
			Geometry feature = new GeoJsonReader().read(JSON.writeValueAsString(geometry));
			Geometry query = new WKTReader().read(wkt);
			return feature != null && query != null && feature.intersects(query);
		}
		catch (JsonProcessingException | ParseException | RuntimeException ex) {
			return false;
		}
	}

	private void assertIdentifierQuery(ResourceType type, Map<String, Object> candidate, String identifier,
			String requirement) {
		TraversalResult filtered = requiredQuery(type, Map.of("id", identifier), requirement);
		assertNonEmpty(filtered, type, "id", identifier, requirement);
		assertEvery(filtered.items(), item -> hasIdentifier(item, identifier), type, "id", identifier, requirement);
		if (!filtered.items().stream().anyMatch(item -> sameResource(item, candidate))) {
			ETSAssert.failWithUri(requirement, type.path + "?id=" + identifier
					+ " omitted the known matching seed resource " + stableId(candidate) + ".");
		}
	}

	private void assertAssociationQuery(ResourceType owner, String parameter, Relation relation, String identifier,
			String requirement) {
		TraversalResult filtered = requiredQuery(owner, Map.of(parameter, identifier), requirement);
		if (!validateEndpoint(owner, filtered, requirement)) {
			throw new SkipException(requirement + " - " + owner.path
					+ " returned an unsupported representation for the released resources-endpoint validation.");
		}
		assertNonEmpty(filtered, owner, parameter, identifier, requirement);
		for (Map<String, Object> item : filtered.items()) {
			Identifiers actual = relationIdentifiers(owner, item, relation, requirement);
			if (!actual.all().contains(identifier)) {
				ETSAssert.failWithUri(requirement,
						owner.path + "?" + parameter + "=" + identifier + " returned nonmatching resource "
								+ stableId(item) + "; collected association identifiers: " + actual.all());
			}
		}
	}

	private TraversalResult requiredQuery(ResourceType type, Map<String, String> query, String requirement) {
		Optional<TraversalResult> result = read(type, query, requirement);
		if (result.isEmpty()) {
			ETSAssert.failWithUri(requirement,
					type.path + " returned HTTP 404 for a query against a previously supported canonical endpoint.");
		}
		return result.orElseThrow();
	}

	private Optional<TraversalResult> recommendedQuery(ResourceType type, Map<String, String> query,
			String recommendation) {
		Response probe = given().accept(type.accept).queryParams(query).when().get(endpoint(type)).andReturn();
		if (probe.getStatusCode() == 404 && query.isEmpty()) {
			return Optional.empty();
		}
		if (probe.getStatusCode() != 200) {
			warn(recommendation,
					type.path + " recommendation query " + query + " returned HTTP " + probe.getStatusCode());
			return Optional.empty();
		}
		try {
			return read(type, query, recommendation);
		}
		catch (AssertionError | RuntimeException ex) {
			warn(recommendation,
					type.path + " recommendation query " + query + " could not be evaluated: " + message(ex));
			return Optional.empty();
		}
	}

	private Optional<TraversalResult> read(ResourceType type, Map<String, String> query, String requirement) {
		return Part1ApiCommonSupport.resourcesAtEndpoint(endpoint(type), type.accept, query, requirement, JSON_MEDIA);
	}

	private URI endpoint(ResourceType type) {
		return this.apiRoot.resolve(type.path);
	}

	private void requireDeclaration(String target) {
		Response response = given().accept("application/json")
			.when()
			.get(this.apiRoot.resolve("conformance"))
			.andReturn();
		ETSAssert.assertStatus(response, 200, target);
		Map<String, Object> body = parseObject(response);
		if (body == null || !(body.get("conformsTo") instanceof List<?> conformsTo)
				|| !conformsTo.contains(CONF_ADVANCED_FILTERING)) {
			throw new SkipException(target + " - IUT does not declare " + CONF_ADVANCED_FILTERING
					+ "; undeclared filter behavior is not conformance PASS evidence.");
		}
	}

	private Identifiers relationIdentifiers(ResourceType owner, Map<String, Object> resource, Relation relation,
			String requirement) {
		Identifiers result = new Identifiers();
		ReferenceReads reads = new ReferenceReads();
		collectRelation(resource, relation.aliases, relation.hrefIdentity, result, reads, 0, requirement);
		Optional<String> ownerId = localId(resource);
		if (ownerId.isPresent()) {
			for (Subresource subresource : relation.subresources(owner)) {
				URI endpoint = this.apiRoot.resolve(owner.path + "/" + encode(ownerId.get()) + "/" + subresource.path);
				Optional<TraversalResult> nested = Part1ApiCommonSupport.resourcesAtEndpoint(endpoint,
						subresource.accept, subresource.query, requirement, JSON_MEDIA);
				if (nested.isEmpty()) {
					continue;
				}
				for (Map<String, Object> item : nested.get().items()) {
					if (subresource.includeItems) {
						addResourceIdentifiers(item, result);
					}
					collectRelation(item, subresource.aliases, relation.hrefIdentity, result, reads, 0, requirement);
					if (owner == ResourceType.DEPLOYMENTS
							&& (relation == Relation.OBSERVED_PROPERTY || relation == Relation.CONTROLLED_PROPERTY)) {
						collectDeployedSystemProperties(item, relation, result, reads, requirement);
					}
				}
			}
		}
		if (relation == Relation.OBSERVED_PROPERTY || relation == Relation.CONTROLLED_PROPERTY) {
			enrichPropertyIdentifiers(result, requirement);
		}
		if (relation == Relation.BASE_PROPERTY || relation == Relation.SAMPLE_OF || relation == Relation.SAMPLED_FEATURE
				|| relation == Relation.FEATURE_OF_INTEREST) {
			followRecursiveRelations(result, relation, reads, requirement);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private void collectRelation(Object value, Set<String> aliases, boolean hrefIdentity, Identifiers result,
			ReferenceReads reads, int depth, String requirement) {
		if (value == null || depth > 12) {
			return;
		}
		if (value instanceof Map<?, ?> raw) {
			Map<String, Object> map = (Map<String, Object>) raw;
			Object links = map.get("links");
			if (links instanceof Collection<?> collection) {
				for (Object linkValue : collection) {
					if (!(linkValue instanceof Map<?, ?> link)) {
						continue;
					}
					String rel = asString(link.get("rel"));
					if (rel != null && aliasMatches(rel, aliases)) {
						collectReference(link, hrefIdentity, result, reads, depth + 1, requirement);
					}
				}
			}
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				if ("links".equals(entry.getKey())) {
					continue;
				}
				if (aliasMatches(entry.getKey(), aliases)) {
					collectReference(entry.getValue(), hrefIdentity, result, reads, depth + 1, requirement);
				}
				else if (entry.getValue() instanceof Map<?, ?> || entry.getValue() instanceof Collection<?>) {
					collectRelation(entry.getValue(), aliases, hrefIdentity, result, reads, depth + 1, requirement);
				}
			}
			return;
		}
		if (value instanceof Collection<?> collection) {
			for (Object item : collection) {
				collectRelation(item, aliases, hrefIdentity, result, reads, depth + 1, requirement);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void collectReference(Object value, boolean hrefIdentity, Identifiers result, ReferenceReads reads,
			int depth, String requirement) {
		if (value == null || depth > 12) {
			return;
		}
		if (value instanceof String string) {
			addSemanticIdentifier(string, result);
			return;
		}
		if (value instanceof Collection<?> collection) {
			for (Object item : collection) {
				collectReference(item, hrefIdentity, result, reads, depth + 1, requirement);
			}
			return;
		}
		if (!(value instanceof Map<?, ?> raw)) {
			return;
		}
		Map<String, Object> map = (Map<String, Object>) raw;
		addResourceIdentifiers(map, result);
		String href = asString(map.get("href"));
		if (href != null) {
			URI resolved = resolve(href);
			lastPathToken(resolved).ifPresent(result.local::add);
			if (hrefIdentity) {
				result.global.add(resolved.toString());
			}
			readReference(resolved, result, reads, requirement);
		}
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = normalize(entry.getKey());
			if (!"href".equals(entry.getKey()) && (entry.getValue() instanceof Map<?, ?>
					|| entry.getValue() instanceof Collection<?>
					|| Set.of("id", "uid", "uniqueid", "definition", "baseproperty", "objecttype").contains(key))) {
				collectReference(entry.getValue(), hrefIdentity, result, reads, depth + 1, requirement);
			}
		}
	}

	private void readReference(URI target, Identifiers result, ReferenceReads reads, String requirement) {
		if (!sameOrigin(this.apiRoot, target) || !reads.visit(target)) {
			return;
		}
		Response response = given().accept("application/geo+json, application/sml+json, application/json")
			.when()
			.get(target)
			.andReturn();
		if (response.getStatusCode() != 200) {
			return;
		}
		Map<String, Object> body = parseObject(response);
		if (body == null) {
			ETSAssert.failWithUri(requirement, target + " returned HTTP 200 but did not contain a JSON object.");
		}
		List<Map<String, Object>> items = collectionItems(body);
		if (items.isEmpty()) {
			addResourceIdentifiers(body, result);
		}
		else {
			items.forEach(item -> addResourceIdentifiers(item, result));
		}
	}

	private void collectDeployedSystemProperties(Map<String, Object> deployedSystem, Relation relation,
			Identifiers result, ReferenceReads reads, String requirement) {
		Set<URI> targets = new LinkedHashSet<>();
		collectRelationTargets(deployedSystem, Set.of("system", "deployedsystem"), targets, 0);
		for (URI target : targets) {
			if (!sameOrigin(this.apiRoot, target) || !reads.visit(target)) {
				continue;
			}
			Response response = given().accept("application/geo+json, application/sml+json, application/json")
				.when()
				.get(target)
				.andReturn();
			if (response.getStatusCode() != 200) {
				continue;
			}
			Map<String, Object> body = parseObject(response);
			if (body == null) {
				ETSAssert.failWithUri(requirement,
						target + " returned HTTP 200 but did not contain a JSON System description.");
			}
			collectRelation(body, relation.aliases, relation.hrefIdentity, result, reads, 0, requirement);
		}
	}

	@SuppressWarnings("unchecked")
	private void collectRelationTargets(Object value, Set<String> aliases, Set<URI> targets, int depth) {
		if (value == null || depth > 12) {
			return;
		}
		if (value instanceof Map<?, ?> raw) {
			Map<String, Object> map = (Map<String, Object>) raw;
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				if (aliasMatches(entry.getKey(), aliases)) {
					collectHrefs(entry.getValue(), targets, depth + 1);
				}
				else if (entry.getValue() instanceof Map<?, ?> || entry.getValue() instanceof Collection<?>) {
					collectRelationTargets(entry.getValue(), aliases, targets, depth + 1);
				}
			}
		}
		else if (value instanceof Collection<?> collection) {
			for (Object item : collection) {
				collectRelationTargets(item, aliases, targets, depth + 1);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void collectHrefs(Object value, Set<URI> targets, int depth) {
		if (value == null || depth > 12) {
			return;
		}
		if (value instanceof Map<?, ?> raw) {
			Map<String, Object> map = (Map<String, Object>) raw;
			String href = asString(map.get("href"));
			if (href != null) {
				targets.add(resolve(href));
			}
			for (Object nested : map.values()) {
				collectHrefs(nested, targets, depth + 1);
			}
		}
		else if (value instanceof Collection<?> collection) {
			for (Object item : collection) {
				collectHrefs(item, targets, depth + 1);
			}
		}
	}

	private void enrichPropertyIdentifiers(Identifiers identifiers, String requirement) {
		Optional<TraversalResult> properties = read(ResourceType.PROPERTIES, Map.of(), requirement);
		if (properties.isEmpty()) {
			return;
		}
		for (Map<String, Object> property : properties.get().items()) {
			Optional<String> propertyUid = uid(property);
			if (propertyUid.isPresent() && identifiers.global.contains(propertyUid.get())) {
				addResourceIdentifiers(property, identifiers);
			}
		}
	}

	private void followRecursiveRelations(Identifiers identifiers, Relation relation, ReferenceReads reads,
			String requirement) {
		Set<URI> followed = new LinkedHashSet<>();
		while (followed.size() < MAX_REFERENCE_READS) {
			Optional<URI> next = identifiers.global.stream()
				.filter(AdvancedFilteringSupport::isAbsoluteUri)
				.map(URI::create)
				.filter(target -> sameOrigin(this.apiRoot, target))
				.filter(target -> !followed.contains(target))
				.findFirst();
			if (next.isEmpty()) {
				return;
			}
			URI target = next.get();
			followed.add(target);
			Response response = given().accept("application/geo+json, application/sml+json, application/json")
				.when()
				.get(target)
				.andReturn();
			if (response.getStatusCode() != 200) {
				continue;
			}
			Map<String, Object> body = parseObject(response);
			if (body == null) {
				ETSAssert.failWithUri(requirement,
						target + " returned HTTP 200 but did not contain a recursive association JSON object.");
			}
			addResourceIdentifiers(body, identifiers);
			collectRelation(body, relation.aliases, relation.hrefIdentity, identifiers, reads, 0, requirement);
		}
	}

	private boolean validateEndpoint(ResourceType type, TraversalResult result, String requirement) {
		return switch (type) {
			case SYSTEMS -> SystemFeaturesSupport.validateSystemEndpoint(endpoint(type), result.pages(), requirement);
			case DEPLOYMENTS ->
				DeploymentFeaturesSupport.validateDeploymentEndpoint(endpoint(type), result.pages(), requirement);
			case PROCEDURES ->
				ProcedureFeaturesSupport.validateProcedureEndpoint(endpoint(type), result.pages(), requirement);
			case SAMPLING_FEATURES ->
				SamplingFeaturesSupport.validateSamplingFeatureEndpoint(endpoint(type), result.pages(), requirement);
			case PROPERTIES ->
				PropertyDefinitionsSupport.validatePropertyEndpoint(endpoint(type), result.pages(), requirement);
		};
	}

	private static void assertNonEmpty(TraversalResult result, ResourceType type, String parameter, String value,
			String requirement) {
		if (result.items().isEmpty()) {
			ETSAssert.failWithUri(requirement, type.path + "?" + parameter + "=" + value
					+ " returned an empty collection for a value selected from known matching seed evidence.");
		}
	}

	private static void assertEvery(List<Map<String, Object>> items, Predicate<Map<String, Object>> predicate,
			ResourceType type, String parameter, String value, String requirement) {
		for (Map<String, Object> item : items) {
			if (!predicate.test(item)) {
				ETSAssert.failWithUri(requirement, type.path + "?" + parameter + "=" + value
						+ " returned a resource that does not satisfy the filter: " + stableId(item));
			}
		}
	}

	private static Optional<String> geometryQuery(Map<String, Object> resource) {
		Object geometry = resource.get("geometry");
		if (!(geometry instanceof Map<?, ?>)) {
			geometry = resource.get("location");
		}
		if (!(geometry instanceof Map<?, ?> map)) {
			return Optional.empty();
		}
		try {
			Geometry parsed = new GeoJsonReader().read(JSON.writeValueAsString(map));
			if (parsed == null || parsed.isEmpty()) {
				return Optional.empty();
			}
			double scale = Math.max(parsed.getEnvelopeInternal().getWidth(), parsed.getEnvelopeInternal().getHeight());
			double padding = Math.max(scale * 0.01, 0.000001);
			return Optional.of(parsed.getEnvelope().buffer(padding).toText());
		}
		catch (JsonProcessingException | ParseException | RuntimeException ex) {
			return Optional.empty();
		}
	}

	@SuppressWarnings("unchecked")
	private static boolean resourceIntersects(Map<String, Object> resource, String wkt) {
		Object geometry = resource.get("geometry");
		if (!(geometry instanceof Map<?, ?>)) {
			geometry = resource.get("location");
		}
		return geometry instanceof Map<?, ?> && intersects((Map<String, Object>) geometry, wkt);
	}

	private static Optional<PropertyValue> customProperty(Map<String, Object> resource) {
		Optional<PropertyValue> nested = customPropertyInMap(asMap(resource.get("properties")));
		return nested.isPresent() ? nested : customPropertyInMap(resource);
	}

	private static Optional<PropertyValue> customPropertyInMap(Map<String, Object> values) {
		for (Map.Entry<String, Object> entry : values.entrySet()) {
			if (RESERVED_PROPERTIES.contains(normalize(entry.getKey()))) {
				continue;
			}
			if (entry.getValue() instanceof String || entry.getValue() instanceof Number
					|| entry.getValue() instanceof Boolean) {
				return Optional.of(new PropertyValue(entry.getKey(), String.valueOf(entry.getValue())));
			}
		}
		return Optional.empty();
	}

	private static Optional<String> scalarProperty(Map<String, Object> resource, String property) {
		if (resource == null || property == null) {
			return Optional.empty();
		}
		Object direct = resource.get(property);
		if (direct instanceof String || direct instanceof Number || direct instanceof Boolean) {
			return Optional.of(String.valueOf(direct));
		}
		Object nested = resource.get("properties");
		if (nested instanceof Map<?, ?> map) {
			Object value = map.get(property);
			if (value instanceof String || value instanceof Number || value instanceof Boolean) {
				return Optional.of(String.valueOf(value));
			}
		}
		return Optional.empty();
	}

	private static Optional<String> keyword(Map<String, Object> resource) {
		for (String value : plainTextStrings(resource)) {
			java.util.regex.Matcher matcher = WORD.matcher(value);
			if (matcher.find()) {
				return Optional.of(matcher.group());
			}
		}
		return Optional.empty();
	}

	private static List<String> plainTextStrings(Object value) {
		List<String> strings = new ArrayList<>();
		collectPlainTextStrings(value, strings, 0);
		return strings;
	}

	private static void collectPlainTextStrings(Object value, List<String> strings, int depth) {
		if (value == null || depth > 12) {
			return;
		}
		if (value instanceof String string) {
			strings.add(string);
		}
		else if (value instanceof Map<?, ?> map) {
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				String key = normalize(String.valueOf(entry.getKey()));
				if (!Set
					.of("id", "uid", "uniqueid", "type", "featuretype", "definition", "href", "rel", "geometry", "bbox",
							"validtime", "baseproperty", "objecttype")
					.contains(key)) {
					collectPlainTextStrings(entry.getValue(), strings, depth + 1);
				}
			}
		}
		else if (value instanceof Collection<?> collection) {
			collection.forEach(item -> collectPlainTextStrings(item, strings, depth + 1));
		}
	}

	private static Set<String> identifiers(Map<String, Object> resource) {
		Identifiers result = new Identifiers();
		addResourceIdentifiers(resource, result);
		return result.all();
	}

	private static void addResourceIdentifiers(Map<String, Object> resource, Identifiers result) {
		localId(resource).ifPresent(result.local::add);
		uid(resource).ifPresent(result.global::add);
	}

	private static Optional<String> localId(Map<String, Object> resource) {
		return stringValue(resource == null ? null : resource.get("id"));
	}

	private static Optional<String> uid(Map<String, Object> resource) {
		if (resource == null) {
			return Optional.empty();
		}
		Optional<String> uniqueId = stringValue(resource.get("uniqueId"));
		if (uniqueId.isPresent()) {
			return uniqueId;
		}
		Optional<String> direct = stringValue(resource.get("uid"));
		if (direct.isPresent()) {
			return direct;
		}
		return stringValue(asMap(resource.get("properties")).get("uid"));
	}

	private static void addSemanticIdentifier(String value, Identifiers result) {
		if (value == null || value.isBlank()) {
			return;
		}
		if (isAbsoluteUri(value)) {
			result.global.add(value);
			lastPathToken(URI.create(value)).ifPresent(result.local::add);
		}
		else {
			result.local.add(value);
		}
	}

	private static Set<String> stableIds(List<Map<String, Object>> resources) {
		Set<String> ids = new LinkedHashSet<>();
		for (Map<String, Object> resource : resources) {
			String id = stableId(resource);
			if (!id.startsWith("<unidentified")) {
				ids.add(id);
			}
		}
		return ids;
	}

	private static String stableId(Map<String, Object> resource) {
		return localId(resource).or(() -> uid(resource)).orElse("<unidentified-resource>");
	}

	private static boolean sameResource(Map<String, Object> left, Map<String, Object> right) {
		Set<String> leftIds = identifiers(left);
		return !leftIds.isEmpty() && !Collections.disjoint(leftIds, identifiers(right));
	}

	private static boolean containsResource(List<Map<String, Object>> resources, String identifier) {
		return resources.stream().anyMatch(item -> hasIdentifier(item, identifier));
	}

	private static void warnUnlessContains(ResourceType type, Optional<TraversalResult> superset,
			Optional<TraversalResult> subset, String supersetName, String subsetName, String recommendation) {
		if (superset.isPresent() && subset.isPresent()
				&& !containsAllResources(superset.get().items(), subset.get().items())) {
			warn(recommendation,
					type.path + " " + supersetName + " result does not contain every " + subsetName + " result");
		}
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> collectionItems(Map<String, Object> body) {
		Object value = body.get("items");
		if (!(value instanceof List<?>)) {
			value = body.get("features");
		}
		if (!(value instanceof List<?> list)) {
			return List.of();
		}
		List<Map<String, Object>> items = new ArrayList<>();
		for (Object item : list) {
			if (item instanceof Map<?, ?> map) {
				items.add((Map<String, Object>) map);
			}
		}
		return List.copyOf(items);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> parseObject(Response response) {
		try {
			Object value = response.jsonPath().get("$");
			return value instanceof Map<?, ?> ? (Map<String, Object>) value : null;
		}
		catch (RuntimeException ex) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> asMap(Object value) {
		return value instanceof Map<?, ?> ? (Map<String, Object>) value : Map.of();
	}

	private URI resolve(String href) {
		try {
			return this.apiRoot.resolve(URI.create(href));
		}
		catch (IllegalArgumentException ex) {
			return URI.create("urn:invalid-reference:" + Integer.toHexString(href.hashCode()));
		}
	}

	private static Optional<String> lastPathToken(URI uri) {
		if (uri == null || uri.getPath() == null || uri.getPath().isBlank()) {
			return Optional.empty();
		}
		String path = uri.getPath();
		int end = path.endsWith("/") ? path.length() - 1 : path.length();
		int slash = path.lastIndexOf('/', end - 1);
		return end > slash + 1 ? Optional.of(path.substring(slash + 1, end)) : Optional.empty();
	}

	private static boolean aliasMatches(String value, Set<String> aliases) {
		String normalized = normalize(value);
		if (normalized.endsWith("link")) {
			normalized = normalized.substring(0, normalized.length() - "link".length());
		}
		String candidate = normalized;
		return aliases.stream().anyMatch(alias -> candidate.equals(alias) || candidate.endsWith(alias));
	}

	private static String normalize(String value) {
		return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
	}

	private static boolean isUriPattern(String token) {
		String value = token.endsWith("*") ? token.substring(0, token.length() - 1) : token;
		return isAbsoluteUri(value);
	}

	private static boolean isAbsoluteUri(String value) {
		if (value == null || value.isBlank() || value.indexOf(' ') >= 0) {
			return false;
		}
		try {
			return URI.create(value).isAbsolute();
		}
		catch (IllegalArgumentException ex) {
			return false;
		}
	}

	private static boolean sameOrigin(URI left, URI right) {
		return left != null && right != null && left.getScheme() != null && right.getScheme() != null
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

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
	}

	private static Optional<String> stringValue(Object value) {
		return value instanceof String string && !string.isBlank() ? Optional.of(string) : Optional.empty();
	}

	private static String asString(Object value) {
		return value instanceof String string && !string.isBlank() ? string : null;
	}

	private static String message(Throwable throwable) {
		return throwable.getMessage() == null || throwable.getMessage().isBlank() ? throwable.getClass().getSimpleName()
				: throwable.getMessage();
	}

	private static void warn(String recommendation, String message) {
		Reporter.log("WARNING " + recommendation + " - " + message + ".", true);
	}

	/**
	 * Canonical Part 1 resource types used by released Advanced Filtering tests.
	 */
	public enum ResourceType {

		SYSTEMS("systems", "application/geo+json, application/sml+json, application/json"),
		DEPLOYMENTS("deployments", "application/geo+json, application/sml+json, application/json"),
		PROCEDURES("procedures", "application/geo+json, application/sml+json, application/json"),
		SAMPLING_FEATURES("samplingFeatures", "application/geo+json, application/sml+json, application/json"),
		PROPERTIES("properties", "application/sml+json, application/json");

		private final String path;

		private final String accept;

		ResourceType(String path, String accept) {
			this.path = path;
			this.accept = accept;
		}

	}

	/**
	 * Association or semantic-property oracles used by released procedures.
	 */
	public enum Relation {

		PARENT_SYSTEM(Set.of("parent", "parentsystem"), false), PROCEDURE(Set.of("procedure", "typeof"), false),
		FEATURE_OF_INTEREST(Set.of("foi", "featureofinterest", "featuresofinterest", "sampleof", "sampledfeature"),
				true),
		OBSERVED_PROPERTY(Set.of("observedproperty", "observedproperties", "output", "outputs"), true),
		CONTROLLED_PROPERTY(Set.of("controlledproperty", "controlledproperties", "input", "inputs"), true),
		PARENT_DEPLOYMENT(Set.of("parent", "parentdeployment"), false),
		DEPLOYED_SYSTEM(Set.of("system", "deployedsystem", "deployedsystems"), false),
		BASE_PROPERTY(Set.of("baseproperty"), true), SAMPLE_OF(Set.of("sampleof"), true),
		SAMPLED_FEATURE(Set.of("sampledfeature"), true);

		private final Set<String> aliases;

		private final boolean hrefIdentity;

		Relation(Set<String> aliases, boolean hrefIdentity) {
			this.aliases = aliases;
			this.hrefIdentity = hrefIdentity;
		}

		private List<Subresource> subresources(ResourceType owner) {
			if (this == FEATURE_OF_INTEREST && owner == ResourceType.SYSTEMS) {
				return List.of(new Subresource("samplingFeatures", Map.of("recursive", "true"), this.aliases, true,
						"application/geo+json, application/json"));
			}
			if (this == FEATURE_OF_INTEREST && owner == ResourceType.DEPLOYMENTS) {
				return List.of(new Subresource("featuresOfInterest", Map.of(), this.aliases, true,
						"application/geo+json, application/json"));
			}
			if ((this == OBSERVED_PROPERTY || this == CONTROLLED_PROPERTY) && owner == ResourceType.SYSTEMS) {
				return List.of(new Subresource("subsystems", Map.of("recursive", "true"), this.aliases, false,
						"application/geo+json, application/sml+json, application/json"));
			}
			if (this == DEPLOYED_SYSTEM && owner == ResourceType.DEPLOYMENTS) {
				return List.of(new Subresource("deployedSystems", Map.of("recursive", "true"), this.aliases, true,
						"application/geo+json, application/sml+json, application/json"));
			}
			if ((this == OBSERVED_PROPERTY || this == CONTROLLED_PROPERTY) && owner == ResourceType.DEPLOYMENTS) {
				return List.of(new Subresource("deployedSystems", Map.of("recursive", "true"), this.aliases, false,
						"application/geo+json, application/sml+json, application/json"));
			}
			if (this == OBSERVED_PROPERTY && owner == ResourceType.SAMPLING_FEATURES) {
				return List.of(new Subresource("datastreams", Map.of(), this.aliases, false, "application/json"));
			}
			if (this == CONTROLLED_PROPERTY && owner == ResourceType.SAMPLING_FEATURES) {
				return List.of(new Subresource("controlstreams", Map.of(), this.aliases, false, "application/json"));
			}
			return List.of();
		}

	}

	private record PropertyValue(String name, String value) {
	}

	private record Subresource(String path, Map<String, String> query, Set<String> aliases, boolean includeItems,
			String accept) {
	}

	private static final class Identifiers {

		private final Set<String> local = new LinkedHashSet<>();

		private final Set<String> global = new LinkedHashSet<>();

		private Set<String> all() {
			Set<String> all = new LinkedHashSet<>(this.local);
			all.addAll(this.global);
			return Collections.unmodifiableSet(all);
		}

	}

	private static final class ReferenceReads {

		private final Set<URI> visited = new LinkedHashSet<>();

		private boolean visit(URI target) {
			return this.visited.size() < MAX_REFERENCE_READS && this.visited.add(target);
		}

	}

	private static final class Inspection {

		private final String requirement;

		private final List<String> limitations = new ArrayList<>();

		private int exercised;

		private Inspection(String requirement) {
			this.requirement = requirement;
		}

		private void exercised() {
			this.exercised++;
		}

		private void limit(String limitation) {
			this.limitations.add(limitation);
		}

		private void finish() {
			if (this.exercised == 0 || !this.limitations.isEmpty()) {
				String detail = this.limitations.isEmpty() ? "no supported endpoint supplied positive evidence"
						: String.join(" | ", this.limitations);
				throw new SkipException(this.requirement + " - incomplete evidence after inspecting all independent "
						+ "canonical endpoints: " + detail + ".");
			}
		}

	}

}
