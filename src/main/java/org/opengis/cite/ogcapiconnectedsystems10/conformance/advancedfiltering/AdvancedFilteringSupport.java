package org.opengis.cite.ogcapiconnectedsystems10.conformance.advancedfiltering;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
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

	private static final int MAX_REFERENCE_DEPTH = 12;

	private static final Set<String> RESERVED_PROPERTIES = Set.of("id", "type", "geometry", "bbox", "links",
			"properties", "featuretype", "uid", "uniqueid", "name", "label", "description", "definition", "validtime",
			"baseproperty", "objecttype", "position", "location");

	private static final Set<String> REFERENCE_CONTAINER_KEYS = Set.of("features", "items", "members", "relations");

	private final URI apiRoot;

	private Set<String> declarations;

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
		Set<String> declarations = requireDeclaration(requirement);
		Inspection inspection = new Inspection(requirement);
		for (ResourceType type : ResourceType.values()) {
			if (!type.isDeclaredBy(declarations)) {
				continue;
			}
			TraversalResult seed = requiredQuery(type, Map.of(), requirement);
			Optional<Map<String, Object>> candidate = seed.items()
				.stream()
				.filter(item -> localId(item).isPresent() && uid(item).isPresent())
				.findFirst();
			if (candidate.isEmpty()) {
				inspection.limit(type.path + " has no resource carrying both local ID and UID");
				continue;
			}
			assertIdentifierQuery(type, candidate.get(), localId(candidate.get()).orElseThrow(), requirement);
			String candidateUid = uid(candidate.get()).orElseThrow();
			assertIdentifierQuery(type, candidate.get(), candidateUid, requirement);
			Optional<String> prefix = uidPrefix(candidateUid);
			if (prefix.isEmpty()) {
				inspection.limit(type.path + " UID cannot supply a valid non-empty shorter prefix: " + candidateUid);
				continue;
			}
			assertIdentifierQuery(type, candidate.get(), prefix.get() + "*", requirement);
			inspection.exercised();
		}
		inspection.finish();
	}

	/**
	 * Implements `/req/advanced-filtering/resource-by-keyword`.
	 * @param requirement released target URI.
	 */
	public void resourceByKeyword(String requirement) {
		Set<String> declarations = requireDeclaration(requirement);
		Inspection inspection = new Inspection(requirement);
		for (ResourceType type : ResourceType.values()) {
			if (!type.isDeclaredBy(declarations)) {
				continue;
			}
			TraversalResult seed = requiredQuery(type, Map.of(), requirement);
			Optional<String> keyword = seed.items()
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
		Set<String> declarations = requireDeclaration(recommendation);
		int exercised = 0;
		for (ResourceType type : ResourceType.values()) {
			if (!type.isDeclaredBy(declarations)) {
				continue;
			}
			Optional<TraversalResult> seed = read(type, Map.of(), recommendation);
			if (seed.isEmpty()) {
				warn(recommendation, type.path + " is declared but its canonical endpoint returned HTTP 404");
				continue;
			}
			Optional<PropertyValue> property = seed.get()
				.items()
				.stream()
				.map(AdvancedFilteringSupport::customProperties)
				.flatMap(Collection::stream)
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
		Set<String> declarations = requireDeclaration(requirement);
		Inspection inspection = new Inspection(requirement);
		for (ResourceType type : EnumSet.of(ResourceType.SYSTEMS, ResourceType.DEPLOYMENTS,
				ResourceType.SAMPLING_FEATURES)) {
			if (!type.isDeclaredBy(declarations)) {
				continue;
			}
			TraversalResult seed = requiredQuery(type, Map.of(), requirement);
			Optional<String> wkt = seed.items()
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
		Set<String> declarations = requireDeclaration(requirement);
		if (!owner.isDeclaredBy(declarations)) {
			throw new SkipException(
					requirement + " - IUT does not declare the owning resource class " + owner.conformance + ".");
		}
		TraversalResult seed = requiredQuery(owner, Map.of(), requirement);
		Map<String, Object> candidate = null;
		Identifiers selected = null;
		List<String> limitations = new ArrayList<>();
		for (Map<String, Object> item : seed.items()) {
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
		Set<String> declarations = requireDeclaration(requirement);
		if (!ResourceType.PROPERTIES.isDeclaredBy(declarations)) {
			throw new SkipException(requirement + " - IUT does not declare the owning resource class "
					+ ResourceType.PROPERTIES.conformance + ".");
		}
		TraversalResult seed = requiredQuery(ResourceType.PROPERTIES, Map.of(), requirement);
		Optional<String> objectType = seed.items()
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
	 * Implements `/req/advanced-filtering/combined-filters`.
	 * @param requirement released target URI.
	 */
	public void combinedFilters(String requirement) {
		Set<String> declarations = requireDeclaration(requirement);
		Inspection inspection = new Inspection(requirement);
		for (ResourceType type : ResourceType.values()) {
			if (!type.isDeclaredBy(declarations)) {
				continue;
			}
			TraversalResult seed = requiredQuery(type, Map.of(), requirement);
			int combinations = 0;
			Set<String> exercisedQueries = new LinkedHashSet<>();
			for (Map<String, Object> item : seed.items()) {
				List<FilterPredicate> available = combinedPredicates(type, item, requirement);
				for (int left = 0; left < available.size(); left++) {
					for (int right = left + 1; right < available.size(); right++) {
						FilterPredicate first = available.get(left);
						FilterPredicate second = available.get(right);
						if (first.parameter.equals(second.parameter)) {
							continue;
						}
						Map<String, String> query = new LinkedHashMap<>();
						query.put(first.parameter, first.value);
						query.put(second.parameter, second.value);
						String signature = query.toString();
						if (!exercisedQueries.add(signature)) {
							continue;
						}
						TraversalResult filtered = requiredQuery(type, query, requirement);
						validateEndpoint(type, filtered, requirement);
						String label = first.parameter + "+" + second.parameter;
						String values = first.value + "," + second.value;
						assertNonEmpty(filtered, type, label, values, requirement);
						assertEvery(filtered.items(),
								result -> first.matches.test(result) && second.matches.test(result), type, label,
								values, requirement);
						combinations++;
					}
				}
			}
			if (combinations < 2) {
				inspection.limit(type.path + " supplied fewer than two independently evidenced filter combinations");
				continue;
			}
			inspection.exercised();
		}
		inspection.finish();
	}

	/**
	 * Implements the transitive base-property recommendation.
	 * @param recommendation released recommendation URI.
	 */
	public void indirectProperty(String recommendation) {
		Set<String> declarations = requireDeclaration(recommendation);
		if (!ResourceType.PROPERTIES.isDeclaredBy(declarations)) {
			warn(recommendation, "owning resource class " + ResourceType.PROPERTIES.conformance + " is not declared");
			return;
		}
		Optional<TraversalResult> properties = read(ResourceType.PROPERTIES, Map.of(), recommendation);
		if (properties.isEmpty()) {
			warn(recommendation, "canonical properties endpoint is unsupported");
			return;
		}
		int eligible = 0;
		for (Map<String, Object> property : properties.get().items()) {
			Optional<String> propertyId = localId(property);
			Optional<String> baseProperty = scalarProperty(property, "baseProperty");
			if (propertyId.isEmpty() || baseProperty.isEmpty()) {
				warn(recommendation, stableId(property)
						+ " lacks local ID or baseProperty evidence for the indirect-property check");
				continue;
			}
			eligible++;
			for (ResourceType type : ResourceType.values()) {
				if (!type.isDeclaredBy(declarations)) {
					continue;
				}
				String parameter = type == ResourceType.PROPERTIES ? "baseProperty" : "observedProperty";
				Optional<TraversalResult> direct = recommendedQuery(type, Map.of(parameter, propertyId.get()),
						recommendation);
				Optional<TraversalResult> transitive = recommendedQuery(type, Map.of(parameter, baseProperty.get()),
						recommendation);
				if (direct.isPresent() && transitive.isPresent()
						&& !containsAllResources(transitive.get().items(), direct.get().items())) {
					warn(recommendation, type.path + " does not include every direct-property result for "
							+ propertyId.get() + " when queried by its transitive base property");
				}
			}
		}
		if (eligible == 0) {
			warn(recommendation, "no Property carries both local ID and baseProperty evidence");
		}
	}

	/**
	 * Implements the transitive feature-of-interest recommendation.
	 * @param recommendation released recommendation URI.
	 */
	public void indirectFeatureOfInterest(String recommendation) {
		Set<String> declarations = requireDeclaration(recommendation);
		if (!ResourceType.SAMPLING_FEATURES.isDeclaredBy(declarations)) {
			warn(recommendation,
					"owning resource class " + ResourceType.SAMPLING_FEATURES.conformance + " is not declared");
			return;
		}
		Optional<TraversalResult> samplingFeatures = read(ResourceType.SAMPLING_FEATURES, Map.of(), recommendation);
		if (samplingFeatures.isEmpty()) {
			warn(recommendation, "canonical samplingFeatures endpoint is unsupported");
			return;
		}
		int eligible = 0;
		for (Map<String, Object> item : samplingFeatures.get().items()) {
			Identifiers parentIds = relationIdentifiers(ResourceType.SAMPLING_FEATURES, item, Relation.SAMPLE_OF,
					recommendation);
			Identifiers ultimateIds = relationIdentifiers(ResourceType.SAMPLING_FEATURES, item,
					Relation.SAMPLED_FEATURE, recommendation);
			Optional<String> sfId = localId(item);
			if (sfId.isEmpty() || parentIds.all().isEmpty() || ultimateIds.all().isEmpty()) {
				warn(recommendation, stableId(item)
						+ " lacks local ID, sampleOf, or sampledFeature evidence for transitive queries");
				continue;
			}
			eligible++;
			String parentId = parentIds.all().iterator().next();
			String ultimateId = ultimateIds.all().iterator().next();
			Optional<TraversalResult> parentSet = recommendedQuery(ResourceType.SAMPLING_FEATURES,
					Map.of("foi", parentId), recommendation);
			Optional<TraversalResult> ultimateSet = recommendedQuery(ResourceType.SAMPLING_FEATURES,
					Map.of("foi", ultimateId), recommendation);
			if (parentSet.isPresent() && !containsResource(parentSet.get().items(), sfId.get())) {
				warn(recommendation, "samplingFeatures?foi=<parent> omits " + sfId.get());
			}
			if (ultimateSet.isPresent() && !containsResource(ultimateSet.get().items(), sfId.get())) {
				warn(recommendation, "samplingFeatures?foi=<ultimate> omits " + sfId.get());
			}
			for (ResourceType type : List.of(ResourceType.SYSTEMS, ResourceType.DEPLOYMENTS)) {
				if (!type.isDeclaredBy(declarations)) {
					continue;
				}
				Optional<TraversalResult> direct = recommendedQuery(type, Map.of("foi", sfId.get()), recommendation);
				Optional<TraversalResult> parent = recommendedQuery(type, Map.of("foi", parentId), recommendation);
				Optional<TraversalResult> ultimateResult = recommendedQuery(type, Map.of("foi", ultimateId),
						recommendation);
				warnUnlessContains(type, ultimateResult, direct, "ultimate feature", "sampling feature",
						recommendation);
				warnUnlessContains(type, ultimateResult, parent, "ultimate feature", "parent feature", recommendation);
				warnUnlessContains(type, parent, direct, "parent feature", "sampling feature", recommendation);
			}
		}
		if (eligible == 0) {
			warn(recommendation,
					"no Sampling Feature carries local ID, sampleOf, and sampledFeature evidence for transitive queries");
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
		if (resource == null || property == null) {
			return false;
		}
		Object direct = resource.get(property);
		if (isScalar(direct) && expected.equals(String.valueOf(direct))) {
			return true;
		}
		Object nested = asMap(resource.get("properties")).get(property);
		return isScalar(nested) && expected.equals(String.valueOf(nested));
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
		assertEvery(filtered.items(), item -> matchesIdentifier(item, identifier), type, "id", identifier, requirement);
		if (!filtered.items().stream().anyMatch(item -> sameResource(item, candidate))) {
			ETSAssert.failWithUri(requirement, type.path + "?id=" + identifier
					+ " omitted the known matching seed resource " + stableId(candidate) + ".");
		}
	}

	private List<FilterPredicate> combinedPredicates(ResourceType type, Map<String, Object> resource,
			String requirement) {
		List<FilterPredicate> predicates = new ArrayList<>();
		localId(resource)
			.ifPresent(value -> predicates.add(new FilterPredicate("id", value, item -> hasIdentifier(item, value))));
		keyword(resource).ifPresent(
				value -> predicates.add(new FilterPredicate("q", value, item -> containsPlainText(item, value))));
		scalarProperty(resource, "featureType").ifPresent(value -> predicates
			.add(new FilterPredicate("featureType", value, item -> hasPropertyValue(item, "featureType", value))));
		temporalFilter(resource).ifPresent(filter -> predicates
			.add(new FilterPredicate("datetime", filter.parameter, item -> temporalIntersects(item, filter))));
		geometryQuery(resource).ifPresent(
				value -> predicates.add(new FilterPredicate("geom", value, item -> resourceIntersects(item, value))));
		switch (type) {
			case SYSTEMS -> {
				addRelationPredicate(predicates, type, resource, "parent", Relation.PARENT_SYSTEM, requirement);
				addRelationPredicate(predicates, type, resource, "procedure", Relation.PROCEDURE, requirement);
				addRelationPredicate(predicates, type, resource, "foi", Relation.FEATURE_OF_INTEREST, requirement);
				addRelationPredicate(predicates, type, resource, "observedProperty", Relation.OBSERVED_PROPERTY,
						requirement);
				addRelationPredicate(predicates, type, resource, "controlledProperty", Relation.CONTROLLED_PROPERTY,
						requirement);
			}
			case DEPLOYMENTS -> {
				addRelationPredicate(predicates, type, resource, "parent", Relation.PARENT_DEPLOYMENT, requirement);
				addRelationPredicate(predicates, type, resource, "system", Relation.DEPLOYED_SYSTEM, requirement);
				addRelationPredicate(predicates, type, resource, "foi", Relation.FEATURE_OF_INTEREST, requirement);
				addRelationPredicate(predicates, type, resource, "observedProperty", Relation.OBSERVED_PROPERTY,
						requirement);
				addRelationPredicate(predicates, type, resource, "controlledProperty", Relation.CONTROLLED_PROPERTY,
						requirement);
			}
			case PROCEDURES -> {
				addRelationPredicate(predicates, type, resource, "observedProperty", Relation.OBSERVED_PROPERTY,
						requirement);
				addRelationPredicate(predicates, type, resource, "controlledProperty", Relation.CONTROLLED_PROPERTY,
						requirement);
			}
			case SAMPLING_FEATURES -> {
				addRelationPredicate(predicates, type, resource, "foi", Relation.FEATURE_OF_INTEREST, requirement);
				addRelationPredicate(predicates, type, resource, "observedProperty", Relation.OBSERVED_PROPERTY,
						requirement);
				addRelationPredicate(predicates, type, resource, "controlledProperty", Relation.CONTROLLED_PROPERTY,
						requirement);
			}
			case PROPERTIES -> {
				scalarProperty(resource, "objectType").ifPresent(value -> predicates.add(
						new FilterPredicate("objectType", value, item -> hasPropertyValue(item, "objectType", value))));
				addRelationPredicate(predicates, type, resource, "baseProperty", Relation.BASE_PROPERTY, requirement);
			}
		}
		addSupportedCustomPredicates(predicates, type, resource, requirement);
		return List.copyOf(predicates);
	}

	private void addSupportedCustomPredicates(List<FilterPredicate> predicates, ResourceType type,
			Map<String, Object> resource, String requirement) {
		for (PropertyValue selected : customProperties(resource)) {
			Optional<TraversalResult> filtered = recommendedQuery(type, Map.of(selected.name, selected.value),
					requirement);
			if (filtered.isEmpty() || filtered.get().items().isEmpty()
					|| filtered.get()
						.items()
						.stream()
						.anyMatch(item -> !hasPropertyValue(item, selected.name, selected.value))
					|| filtered.get().items().stream().noneMatch(item -> sameResource(item, resource))) {
				continue;
			}
			predicates.add(new FilterPredicate(selected.name, selected.value,
					item -> hasPropertyValue(item, selected.name, selected.value)));
		}
	}

	private void addRelationPredicate(List<FilterPredicate> predicates, ResourceType type, Map<String, Object> resource,
			String parameter, Relation relation, String requirement) {
		Identifiers identifiers = relationIdentifiers(type, resource, relation, requirement);
		Optional<String> selected = identifiers.local.stream()
			.findFirst()
			.or(() -> identifiers.global.stream().findFirst());
		selected.ifPresent(value -> predicates.add(new FilterPredicate(parameter, value,
				item -> relationIdentifiers(type, item, relation, requirement).all().contains(value))));
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

	private Set<String> requireDeclaration(String target) {
		if (this.declarations != null) {
			return this.declarations;
		}
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
		Set<String> declarations = new LinkedHashSet<>();
		for (Object declaration : conformsTo) {
			if (declaration instanceof String value) {
				declarations.add(value);
			}
		}
		this.declarations = Set.copyOf(declarations);
		return this.declarations;
	}

	private Identifiers relationIdentifiers(ResourceType owner, Map<String, Object> resource, Relation relation,
			String requirement) {
		Identifiers result = new Identifiers();
		ReferenceReads reads = new ReferenceReads();
		if (relation.rootEvidenceAllowed(owner)) {
			collectRelation(resource, relation.aliases, relation.hrefIdentity, result, reads, 0, requirement);
		}
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
						if (isReferenceWrapper(item, subresource.aliases)) {
							if (asString(item.get("href")) != null) {
								collectReference(item, subresource.aliases, relation.hrefIdentity, result, reads, 0,
										requirement);
							}
						}
						else {
							addResourceIdentifiers(item, result);
						}
					}
					if (owner == ResourceType.DEPLOYMENTS
							&& (relation == Relation.OBSERVED_PROPERTY || relation == Relation.CONTROLLED_PROPERTY)) {
						collectDeployedSystemProperties(item, relation, result, reads, requirement);
					}
					else {
						collectRelation(item, subresource.aliases, relation.hrefIdentity, result, reads, 0,
								requirement);
					}
				}
			}
		}
		if (relation == Relation.OBSERVED_PROPERTY || relation == Relation.CONTROLLED_PROPERTY) {
			enrichPropertyIdentifiers(result, requirement);
		}
		return result;
	}

	private static boolean isReferenceWrapper(Map<String, Object> item, Set<String> aliases) {
		if (asString(item.get("href")) != null) {
			return true;
		}
		return item.keySet().stream().anyMatch(key -> fieldAliasMatches(key, aliases));
	}

	@SuppressWarnings("unchecked")
	private void collectRelation(Object value, Set<String> aliases, boolean hrefIdentity, Identifiers result,
			ReferenceReads reads, int depth, String requirement) {
		if (value == null) {
			return;
		}
		assertTraversalDepth(depth, requirement);
		if (value instanceof Map<?, ?> raw) {
			Map<String, Object> map = (Map<String, Object>) raw;
			collectDirectRelationFields(map, aliases, hrefIdentity, result, reads, depth, requirement);
			collectDirectRelationFields(asMap(map.get("properties")), aliases, hrefIdentity, result, reads, depth + 1,
					requirement);
			return;
		}
		if (value instanceof Collection<?> collection) {
			for (Object item : collection) {
				collectRelation(item, aliases, hrefIdentity, result, reads, depth + 1, requirement);
			}
		}
	}

	private void collectDirectRelationFields(Map<String, Object> fields, Set<String> aliases, boolean hrefIdentity,
			Identifiers result, ReferenceReads reads, int depth, String requirement) {
		assertTraversalDepth(depth, requirement);
		Object links = fields.get("links");
		if (links instanceof Collection<?> collection) {
			for (Object linkValue : collection) {
				if (!(linkValue instanceof Map<?, ?> link)) {
					continue;
				}
				String rel = asString(link.get("rel"));
				if (rel != null && relationAliasMatches(rel, aliases)) {
					collectReference(link, aliases, hrefIdentity, result, reads, depth + 1, requirement);
				}
			}
		}
		for (Map.Entry<String, Object> entry : fields.entrySet()) {
			if (!"links".equals(entry.getKey()) && fieldAliasMatches(entry.getKey(), aliases)) {
				collectReference(entry.getValue(), aliases, hrefIdentity, result, reads, depth + 1, requirement);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void collectReference(Object value, Set<String> aliases, boolean hrefIdentity, Identifiers result,
			ReferenceReads reads, int depth, String requirement) {
		if (value == null) {
			return;
		}
		assertTraversalDepth(depth, requirement);
		if (value instanceof String string) {
			collectStringReference(string, aliases, hrefIdentity, result, reads, requirement);
			return;
		}
		if (value instanceof Collection<?> collection) {
			for (Object item : collection) {
				collectReference(item, aliases, hrefIdentity, result, reads, depth + 1, requirement);
			}
			return;
		}
		if (!(value instanceof Map<?, ?> raw)) {
			return;
		}
		Map<String, Object> map = (Map<String, Object>) raw;
		String href = asString(map.get("href"));
		if (href != null) {
			resolveReference(href)
				.ifPresent(resolved -> readReference(resolved, aliases, hrefIdentity, result, reads, requirement));
			return;
		}
		addResourceIdentifiers(map, result);
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = normalize(entry.getKey());
			if (REFERENCE_CONTAINER_KEYS.contains(key)
					&& (entry.getValue() instanceof Map<?, ?> || entry.getValue() instanceof Collection<?>)) {
				collectReference(entry.getValue(), aliases, hrefIdentity, result, reads, depth + 1, requirement);
			}
		}
	}

	private void collectStringReference(String value, Set<String> aliases, boolean hrefIdentity, Identifiers result,
			ReferenceReads reads, String requirement) {
		if (!isAbsoluteUri(value)) {
			result.local.add(value);
			return;
		}
		Optional<URI> resolved = resolveReference(value);
		if (resolved.isEmpty()) {
			return;
		}
		URI target = resolved.get();
		if ("http".equalsIgnoreCase(target.getScheme()) || "https".equalsIgnoreCase(target.getScheme())) {
			readReference(target, aliases, hrefIdentity, result, reads, requirement);
		}
		else {
			result.global.add(value);
		}
	}

	private void readReference(URI target, Set<String> aliases, boolean hrefIdentity, Identifiers result,
			ReferenceReads reads, String requirement) {
		if (!sameOrigin(this.apiRoot, target)) {
			if (hrefIdentity) {
				result.global.add(target.toString());
			}
			return;
		}
		if (!reads.enter(target, requirement)) {
			return;
		}
		try {
			Response response = given().accept("application/geo+json, application/sml+json, application/json")
				.when()
				.get(target)
				.andReturn();
			if (response.getStatusCode() != 200) {
				if (hrefIdentity) {
					result.global.add(target.toString());
				}
				return;
			}
			if (!JSON_MEDIA.contains(responseMediaType(response))) {
				if (hrefIdentity) {
					result.global.add(target.toString());
				}
				return;
			}
			Map<String, Object> body = parseObject(response);
			if (body == null) {
				ETSAssert.failWithUri(requirement, target + " returned HTTP 200 but did not contain a JSON object.");
			}
			if (isCollectionDocument(body)) {
				Optional<TraversalResult> traversal = Part1ApiCommonSupport.resourcesAtEndpoint(target,
						"application/geo+json, application/sml+json, application/json", Map.of(), requirement,
						JSON_MEDIA);
				if (traversal.isPresent()) {
					for (Map<String, Object> item : traversal.get().items()) {
						collectResolvedReferenceResource(item, aliases, hrefIdentity, result, reads, requirement);
					}
				}
			}
			else {
				collectResolvedReferenceResource(body, aliases, hrefIdentity, result, reads, requirement);
			}
		}
		finally {
			reads.leave(target);
		}
	}

	private void collectResolvedReferenceResource(Map<String, Object> resource, Set<String> aliases,
			boolean hrefIdentity, Identifiers result, ReferenceReads reads, String requirement) {
		addResourceIdentifiers(resource, result);
		if (hrefIdentity) {
			collectRelation(resource, aliases, true, result, reads, 0, requirement);
		}
	}

	private void collectDeployedSystemProperties(Map<String, Object> deployedSystem, Relation relation,
			Identifiers result, ReferenceReads reads, String requirement) {
		if (isSystemRepresentation(deployedSystem)) {
			collectRelation(deployedSystem, relation.aliases, relation.hrefIdentity, result, reads, 0, requirement);
			return;
		}
		Set<URI> targets = new LinkedHashSet<>();
		collectDirectDeployedSystemTargets(deployedSystem, relation, result, reads, targets, requirement);
		collectDirectDeployedSystemTargets(asMap(deployedSystem.get("properties")), relation, result, reads, targets,
				requirement);
		for (URI target : targets) {
			if (!sameOrigin(this.apiRoot, target) || !reads.enter(target, requirement)) {
				continue;
			}
			try {
				Response response = given().accept("application/geo+json, application/sml+json, application/json")
					.when()
					.get(target)
					.andReturn();
				if (response.getStatusCode() != 200) {
					continue;
				}
				if (!JSON_MEDIA.contains(responseMediaType(response))) {
					continue;
				}
				Map<String, Object> body = parseObject(response);
				if (body == null) {
					ETSAssert.failWithUri(requirement,
							target + " returned HTTP 200 but did not contain a JSON System description.");
				}
				if (isCollectionDocument(body) || !isSystemRepresentation(body)) {
					continue;
				}
				collectRelation(body, relation.aliases, relation.hrefIdentity, result, reads, 0, requirement);
			}
			finally {
				reads.leave(target);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void collectDirectDeployedSystemTargets(Map<String, Object> wrapper, Relation relation, Identifiers result,
			ReferenceReads reads, Set<URI> targets, String requirement) {
		for (Map.Entry<String, Object> entry : wrapper.entrySet()) {
			if (fieldAliasMatches(entry.getKey(), Set.of("system", "deployedsystem"))) {
				collectDirectDeployedSystemTarget(entry.getValue(), relation, result, reads, targets, 0, requirement);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void collectDirectDeployedSystemTarget(Object value, Relation relation, Identifiers result,
			ReferenceReads reads, Set<URI> targets, int depth, String requirement) {
		if (value == null) {
			return;
		}
		assertTraversalDepth(depth, requirement);
		if (value instanceof String href) {
			resolveReference(href).ifPresent(targets::add);
			return;
		}
		if (value instanceof Map<?, ?> raw) {
			Map<String, Object> map = (Map<String, Object>) raw;
			if (isSystemRepresentation(map)) {
				collectRelation(map, relation.aliases, relation.hrefIdentity, result, reads, depth + 1, requirement);
				return;
			}
			String href = asString(map.get("href"));
			if (href != null) {
				resolveReference(href).ifPresent(targets::add);
			}
			return;
		}
		if (value instanceof Collection<?> collection) {
			for (Object item : collection) {
				collectDirectDeployedSystemTarget(item, relation, result, reads, targets, depth + 1, requirement);
			}
		}
	}

	private static boolean isSystemRepresentation(Map<String, Object> resource) {
		String type = normalize(asString(resource.get("type")));
		String featureType = scalarProperty(resource, "featureType").map(AdvancedFilteringSupport::normalize)
			.orElse("");
		return "feature".equals(type) && featureType.endsWith("system")
				|| Set.of("physicalsystem", "system").contains(type);
	}

	private void enrichPropertyIdentifiers(Identifiers identifiers, String requirement) {
		if (!ResourceType.PROPERTIES.isDeclaredBy(requireDeclaration(requirement))) {
			return;
		}
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

	private static Optional<TemporalFilter> temporalFilter(Map<String, Object> resource) {
		Object validTime = validTime(resource);
		if (validTime == null) {
			return Optional.empty();
		}
		Instant sampledAt = Instant.now();
		try {
			if (validTime instanceof String) {
				Instant instant = parseTemporalBound(validTime, sampledAt);
				return Optional.of(new TemporalFilter(instant.toString(), instant, instant, sampledAt));
			}
			TemporalBounds bounds = temporalBounds(validTime, sampledAt);
			if (bounds == null || bounds.begin == null && bounds.end == null) {
				return Optional.empty();
			}
			String begin = bounds.begin == null ? ".." : bounds.begin.toString();
			String end = bounds.end == null ? ".." : bounds.end.toString();
			return Optional.of(new TemporalFilter(begin + "/" + end, bounds.begin, bounds.end, sampledAt));
		}
		catch (IllegalArgumentException ex) {
			return Optional.empty();
		}
	}

	private static boolean temporalIntersects(Map<String, Object> resource, TemporalFilter filter) {
		Object validTime = validTime(resource);
		if (validTime == null) {
			return true;
		}
		try {
			TemporalBounds bounds = temporalBounds(validTime, filter.sampledAt);
			return bounds != null && (bounds.end == null || filter.begin == null || !bounds.end.isBefore(filter.begin))
					&& (filter.end == null || bounds.begin == null || !filter.end.isBefore(bounds.begin));
		}
		catch (IllegalArgumentException ex) {
			return false;
		}
	}

	private static TemporalBounds temporalBounds(Object validTime, Instant sampledAt) {
		if (validTime instanceof String) {
			Instant instant = parseTemporalBound(validTime, sampledAt);
			return new TemporalBounds(instant, instant);
		}
		if (!(validTime instanceof List<?> interval) || interval.size() != 2) {
			throw new IllegalArgumentException("validTime must be an ISO instant or a two-bound interval");
		}
		Instant begin = parseOptionalTemporalBound(interval.get(0), sampledAt);
		Instant end = parseOptionalTemporalBound(interval.get(1), sampledAt);
		if (begin != null && end != null && end.isBefore(begin)) {
			throw new IllegalArgumentException("validTime end precedes its begin");
		}
		return new TemporalBounds(begin, end);
	}

	private static Instant parseOptionalTemporalBound(Object value, Instant sampledAt) {
		return value == null ? null : parseTemporalBound(value, sampledAt);
	}

	private static Instant parseTemporalBound(Object value, Instant sampledAt) {
		if (!(value instanceof String string) || string.isBlank()) {
			throw new IllegalArgumentException("Temporal bound must be an ISO-8601 string or null");
		}
		return "now".equals(string) ? sampledAt : Instant.parse(string);
	}

	private static Object validTime(Map<String, Object> resource) {
		if (resource == null) {
			return null;
		}
		if (resource.containsKey("validTime")) {
			return resource.get("validTime");
		}
		return asMap(resource.get("properties")).get("validTime");
	}

	@SuppressWarnings("unchecked")
	private static boolean resourceIntersects(Map<String, Object> resource, String wkt) {
		Object geometry = resource.get("geometry");
		if (!(geometry instanceof Map<?, ?>)) {
			geometry = resource.get("location");
		}
		return geometry instanceof Map<?, ?> && intersects((Map<String, Object>) geometry, wkt);
	}

	private static List<PropertyValue> customProperties(Map<String, Object> resource) {
		Map<String, PropertyValue> properties = new LinkedHashMap<>();
		collectCustomProperties(asMap(resource.get("properties")), properties);
		collectCustomProperties(resource, properties);
		return List.copyOf(properties.values());
	}

	private static void collectCustomProperties(Map<String, Object> values, Map<String, PropertyValue> properties) {
		for (Map.Entry<String, Object> entry : values.entrySet()) {
			if (RESERVED_PROPERTIES.contains(normalize(entry.getKey()))) {
				continue;
			}
			if (isScalar(entry.getValue())) {
				PropertyValue property = new PropertyValue(entry.getKey(), String.valueOf(entry.getValue()));
				properties.putIfAbsent(normalize(property.name) + "\u0000" + property.value, property);
			}
		}
	}

	private static boolean isScalar(Object value) {
		return value instanceof String || value instanceof Number || value instanceof Boolean;
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

	private static Optional<String> uidPrefix(String value) {
		if (!isAbsoluteUri(value)) {
			return Optional.empty();
		}
		int schemeSeparator = value.indexOf(':');
		if (schemeSeparator < 0 || value.length() <= schemeSeparator + 2) {
			return Optional.empty();
		}
		int end = value.offsetByCodePoints(value.length(), -1);
		String prefix = value.substring(0, end);
		return isAbsoluteUri(prefix) ? Optional.of(prefix) : Optional.empty();
	}

	private static boolean matchesIdentifier(Map<String, Object> resource, String identifier) {
		if (identifier == null || !identifier.endsWith("*")) {
			return hasIdentifier(resource, identifier);
		}
		String prefix = identifier.substring(0, identifier.length() - 1);
		return !prefix.isBlank() && uid(resource).filter(value -> value.startsWith(prefix)).isPresent();
	}

	private static List<String> plainTextStrings(Map<String, Object> resource) {
		List<String> strings = new ArrayList<>();
		collectPlainTextProperties(resource, strings);
		collectPlainTextProperties(asMap(resource == null ? null : resource.get("properties")), strings);
		return strings;
	}

	private static void collectPlainTextProperties(Map<String, Object> values, List<String> strings) {
		for (Map.Entry<String, Object> entry : values.entrySet()) {
			if (Set.of("name", "description", "label").contains(normalize(entry.getKey()))) {
				collectTextValues(entry.getValue(), strings);
			}
		}
	}

	private static void collectTextValues(Object value, List<String> strings) {
		if (value instanceof String string) {
			strings.add(string);
		}
		else if (value instanceof Collection<?> collection) {
			collection.forEach(item -> collectTextValues(item, strings));
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

	private static void assertTraversalDepth(int depth, String requirement) {
		if (depth > MAX_REFERENCE_DEPTH) {
			ETSAssert.failWithUri(requirement,
					"association traversal depth exceeded " + MAX_REFERENCE_DEPTH + " while collecting evidence.");
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
	private static Map<String, Object> parseObject(Response response) {
		try {
			Object value = response.jsonPath().get("$");
			return value instanceof Map<?, ?> ? (Map<String, Object>) value : null;
		}
		catch (RuntimeException ex) {
			return null;
		}
	}

	private static String responseMediaType(Response response) {
		String contentType = response.getContentType();
		return contentType == null || contentType.isBlank() ? ""
				: contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
	}

	private static boolean isCollectionDocument(Map<String, Object> body) {
		if (body.get("items") instanceof List<?> || body.get("features") instanceof List<?>
				|| "FeatureCollection".equals(body.get("type"))) {
			return true;
		}
		Object links = body.get("links");
		return links instanceof Collection<?> collection && collection.stream()
			.filter(Map.class::isInstance)
			.map(Map.class::cast)
			.anyMatch(link -> "next".equals(link.get("rel")));
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> asMap(Object value) {
		return value instanceof Map<?, ?> ? (Map<String, Object>) value : Map.of();
	}

	private Optional<URI> resolveReference(String href) {
		try {
			return Optional.of(this.apiRoot.resolve(URI.create(href)));
		}
		catch (IllegalArgumentException ex) {
			return Optional.empty();
		}
	}

	private static boolean fieldAliasMatches(String value, Set<String> aliases) {
		String normalized = normalize(value);
		if (normalized.endsWith("link")) {
			normalized = normalized.substring(0, normalized.length() - "link".length());
		}
		return aliases.contains(normalized);
	}

	private static boolean relationAliasMatches(String value, Set<String> aliases) {
		if (fieldAliasMatches(value, aliases)) {
			return true;
		}
		try {
			URI relation = URI.create(value);
			if (!relation.isAbsolute()) {
				return false;
			}
			String path = relation.getPath();
			int slash = path == null ? -1 : path.lastIndexOf('/');
			String segment = slash >= 0 ? path.substring(slash + 1) : path;
			return fieldAliasMatches(segment, aliases) || fieldAliasMatches(relation.getFragment(), aliases);
		}
		catch (IllegalArgumentException ex) {
			return false;
		}
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

		SYSTEMS("systems", "application/geo+json, application/sml+json, application/json",
				"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/system"),
		DEPLOYMENTS("deployments", "application/geo+json, application/sml+json, application/json",
				"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/deployment"),
		PROCEDURES("procedures", "application/geo+json, application/sml+json, application/json",
				"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/procedure"),
		SAMPLING_FEATURES("samplingFeatures", "application/geo+json, application/sml+json, application/json",
				"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/sf"),
		PROPERTIES("properties", "application/sml+json, application/json",
				"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/property");

		private final String path;

		private final String accept;

		private final String conformance;

		ResourceType(String path, String accept, String conformance) {
			this.path = path;
			this.accept = accept;
			this.conformance = conformance;
		}

		private boolean isDeclaredBy(Set<String> declarations) {
			return declarations.contains(this.conformance);
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
				return List.of(new Subresource("samplingFeatures", Map.of("recursive", "true"), this.aliases, false,
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

		private boolean rootEvidenceAllowed(ResourceType owner) {
			if (owner == ResourceType.DEPLOYMENTS && this != PARENT_DEPLOYMENT) {
				return false;
			}
			if (owner == ResourceType.SYSTEMS && this == FEATURE_OF_INTEREST) {
				return false;
			}
			return owner != ResourceType.SAMPLING_FEATURES || this != OBSERVED_PROPERTY && this != CONTROLLED_PROPERTY;
		}

	}

	private record PropertyValue(String name, String value) {
	}

	private record FilterPredicate(String parameter, String value, Predicate<Map<String, Object>> matches) {
	}

	private record TemporalFilter(String parameter, Instant begin, Instant end, Instant sampledAt) {
	}

	private record TemporalBounds(Instant begin, Instant end) {
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

		private final Set<URI> active = new LinkedHashSet<>();

		private boolean enter(URI target, String requirement) {
			if (this.active.contains(target)) {
				ETSAssert.failWithUri(requirement, "association reference cycle detected at " + target + ".");
			}
			if (this.visited.contains(target)) {
				return false;
			}
			if (this.visited.size() >= MAX_REFERENCE_READS) {
				ETSAssert.failWithUri(requirement,
						"association reference-read limit of " + MAX_REFERENCE_READS + " exceeded at " + target + ".");
			}
			this.visited.add(target);
			this.active.add(target);
			return true;
		}

		private void leave(URI target) {
			this.active.remove(target);
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
