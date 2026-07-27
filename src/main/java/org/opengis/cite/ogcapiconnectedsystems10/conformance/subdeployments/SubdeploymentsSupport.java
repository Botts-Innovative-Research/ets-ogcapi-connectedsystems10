package org.opengis.cite.ogcapiconnectedsystems10.conformance.subdeployments;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.TraversalResult;
import org.testng.Reporter;

/**
 * Independent graph, link, and comparison support for released Subdeployment procedures.
 */
final class SubdeploymentsSupport {

	private static final int MAX_NODES = 10_000;

	private static final ObjectMapper JSON = new ObjectMapper();

	private static final String HIERARCHY_REQUIREMENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/subdeployment";

	private SubdeploymentsSupport() {
	}

	static Hierarchy hierarchy(Map<String, List<String>> directChildren) {
		if (directChildren == null) {
			throw new IllegalArgumentException("directChildren must not be null");
		}
		Map<String, Set<String>> normalized = new LinkedHashMap<>();
		Set<String> nodes = new LinkedHashSet<>();
		Map<String, String> parentByChild = new LinkedHashMap<>();
		for (Map.Entry<String, List<String>> entry : directChildren.entrySet()) {
			String parent = requireId(entry.getKey(), HIERARCHY_REQUIREMENT);
			List<String> children = entry.getValue();
			if (children == null) {
				throw new IllegalArgumentException("children must not be null");
			}
			Set<String> unique = new LinkedHashSet<>();
			for (String child : children) {
				String id = requireId(child, HIERARCHY_REQUIREMENT);
				if (!unique.add(id)) {
					ETSAssert.failWithUri(HIERARCHY_REQUIREMENT,
							"hierarchy contains duplicate direct child '" + id + "' for parent '" + parent + "'.");
				}
				String previousParent = parentByChild.putIfAbsent(id, parent);
				if (previousParent != null && !previousParent.equals(parent)) {
					ETSAssert.failWithUri(HIERARCHY_REQUIREMENT, "Deployment '" + id
							+ "' occurs below multiple parents '" + previousParent + "' and '" + parent + "'.");
				}
			}
			normalized.put(parent, Collections.unmodifiableSet(unique));
			nodes.add(parent);
			nodes.addAll(unique);
			if (nodes.size() > MAX_NODES) {
				ETSAssert.failWithUri(HIERARCHY_REQUIREMENT,
						"hierarchy exceeded the " + MAX_NODES + " node safety bound.");
			}
		}
		for (String node : nodes) {
			normalized.putIfAbsent(node, Set.of());
		}
		assertAcyclic(normalized);
		assertNoShortcutEdges(normalized);
		return new Hierarchy(normalized, nodes);
	}

	static Set<String> ids(TraversalResult traversal, URI endpoint, String requirement) {
		if (traversal == null) {
			throw new IllegalArgumentException("traversal must not be null");
		}
		Set<String> ids = new LinkedHashSet<>();
		for (Map<String, Object> item : traversal.items()) {
			Object value = item.get("id");
			if (!(value instanceof String) || ((String) value).isBlank()) {
				ETSAssert.failWithUri(requirement, endpoint + " contains a resource without a non-empty string id.");
			}
			String id = ((String) value).trim();
			if (!ids.add(id)) {
				ETSAssert.failWithUri(requirement, endpoint + " contains duplicate resource id '" + id + "'.");
			}
		}
		return Collections.unmodifiableSet(ids);
	}

	static void assertRecursiveDeployments(Hierarchy hierarchy, Set<String> defaultIds, Set<String> falseIds,
			Set<String> trueIds, String requirement) {
		assertExact(defaultIds, hierarchy.rootNodes(), requirement,
				"default /deployments response does not contain exactly the hierarchy roots");
		assertExact(falseIds, hierarchy.rootNodes(), requirement,
				"recursive=false /deployments response does not contain exactly the hierarchy roots");
		assertExact(trueIds, hierarchy.allNodes(), requirement,
				"recursive=true /deployments response does not contain exactly all hierarchy nodes");
	}

	static void assertRecursiveSubdeployments(String parent, Hierarchy hierarchy, Set<String> defaultIds,
			Set<String> falseIds, Set<String> trueIds, String requirement) {
		Set<String> direct = hierarchy.directChildren(parent);
		assertExact(defaultIds, direct, requirement,
				"default Subdeployment response does not contain exactly the direct children of '" + parent + "'");
		assertExact(falseIds, direct, requirement,
				"recursive=false Subdeployment response does not contain exactly the direct children of '" + parent
						+ "'");
		assertExact(trueIds, hierarchy.descendants(parent), requirement,
				"recursive=true Subdeployment response does not contain exactly the descendants of '" + parent + "'");
	}

	static URI subdeploymentsUri(Map<String, Object> parent, URI parentSource, URI apiRoot, String parentId,
			String requirement) {
		Object links = parent == null ? null : parent.get("links");
		if (!(links instanceof List)) {
			ETSAssert.failWithUri(requirement, parentSource + " Deployment resource is missing a links array.");
		}
		List<URI> candidates = new ArrayList<>();
		URI expected = subdeploymentCollectionUri(apiRoot, parentId);
		for (Object value : (List<?>) links) {
			if (!(value instanceof Map)) {
				continue;
			}
			Map<?, ?> link = (Map<?, ?>) value;
			if (!hasRelation(link.get("rel"), "subdeployments")) {
				continue;
			}
			URI actual = resolveLink(link, parentSource, apiRoot, "subdeployments", requirement);
			if (!sameHttpTarget(expected, actual)) {
				ETSAssert.failWithUri(requirement,
						parentSource + " rel=subdeployments target must be " + expected + " but was " + actual + ".");
			}
			candidates.add(actual);
		}
		if (candidates.isEmpty()) {
			ETSAssert.failWithUri(requirement, parentSource + " must expose at least one rel=subdeployments link.");
		}
		return candidates.get(0);
	}

	static Optional<URI> associationUri(Map<String, Object> deployment, URI source, URI apiRoot, String relation,
			String requirement) {
		Object links = deployment == null ? null : deployment.get("links");
		if (links == null) {
			return Optional.empty();
		}
		if (!(links instanceof List)) {
			ETSAssert.failWithUri(requirement, source + " Deployment links value is not an array.");
		}
		List<URI> typedCandidates = new ArrayList<>();
		List<URI> untypedCandidates = new ArrayList<>();
		for (Object value : (List<?>) links) {
			if (!(value instanceof Map)) {
				continue;
			}
			Map<?, ?> link = (Map<?, ?>) value;
			if (!hasRelation(link.get("rel"), relation)) {
				continue;
			}
			Optional<URI> candidate = safeAssociationLink(link, source, apiRoot, relation, requirement);
			if (candidate.isEmpty()) {
				continue;
			}
			Object type = link.get("type");
			if (type == null || type instanceof String && ((String) type).isBlank()) {
				untypedCandidates.add(candidate.orElseThrow());
			}
			else if (type instanceof String && isJsonCompatible((String) type)) {
				typedCandidates.add(candidate.orElseThrow());
			}
			else {
				Reporter.log(requirement + " - ignoring rel=" + relation + " occurrence at " + candidate.orElseThrow()
						+ " with unsupported advertised type '" + type + "'.", true);
			}
		}
		return typedCandidates.isEmpty() ? untypedCandidates.stream().findFirst()
				: typedCandidates.stream().findFirst();
	}

	static boolean associationAdvertised(Map<String, Object> deployment, String relation) {
		Object links = deployment == null ? null : deployment.get("links");
		if (!(links instanceof List)) {
			return false;
		}
		for (Object value : (List<?>) links) {
			if (value instanceof Map && hasRelation(((Map<?, ?>) value).get("rel"), relation)) {
				return true;
			}
		}
		return false;
	}

	static AssociationEvidence associationEvidence(String source) {
		if (source == null || source.isBlank()) {
			return new AssociationEvidence(Map.of());
		}
		try {
			JsonNode root = JSON.readTree(source);
			if (root == null || !root.isObject()) {
				throw new IllegalArgumentException("association evidence must be a JSON object");
			}
			Map<String, Map<String, Set<String>>> deployments = new LinkedHashMap<>();
			root.fields().forEachRemaining(deployment -> {
				String deploymentId = requireFixtureToken(deployment.getKey(), "Deployment id");
				if (!deployment.getValue().isObject()) {
					throw new IllegalArgumentException(
							"association evidence for Deployment '" + deploymentId + "' must be an object");
				}
				Map<String, Set<String>> relations = new LinkedHashMap<>();
				deployment.getValue().fields().forEachRemaining(relation -> {
					String relationName = requireFixtureToken(relation.getKey(), "relation");
					if (!relation.getValue().isArray()) {
						throw new IllegalArgumentException("association evidence for Deployment '" + deploymentId
								+ "' relation '" + relationName + "' must be an array");
					}
					Set<String> ids = new LinkedHashSet<>();
					for (JsonNode id : relation.getValue()) {
						if (!id.isTextual()) {
							throw new IllegalArgumentException("association evidence IDs must be strings");
						}
						String normalized = requireFixtureToken(id.textValue(), "resource id");
						if (!ids.add(normalized)) {
							throw new IllegalArgumentException("duplicate association evidence ID '" + normalized
									+ "' for Deployment '" + deploymentId + "' relation '" + relationName + "'");
						}
					}
					relations.put(relationName, Collections.unmodifiableSet(ids));
				});
				deployments.put(deploymentId, Collections.unmodifiableMap(relations));
			});
			if (deployments.size() > MAX_NODES) {
				throw new IllegalArgumentException("association evidence exceeded the " + MAX_NODES + " node bound");
			}
			return new AssociationEvidence(Collections.unmodifiableMap(deployments));
		}
		catch (IllegalArgumentException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("association evidence is not valid JSON: " + ex.getMessage(), ex);
		}
	}

	static URI deploymentUri(URI apiRoot, String deploymentId) {
		return apiRoot.resolve("deployments/" + encode(deploymentId));
	}

	static URI subdeploymentCollectionUri(URI apiRoot, String parentId) {
		return URI.create(deploymentUri(apiRoot, parentId) + "/subdeployments");
	}

	static void assertIncludes(Set<String> actual, Set<String> expected, URI endpoint, String relation,
			String requirement) {
		Set<String> missing = new LinkedHashSet<>(expected);
		missing.removeAll(actual);
		if (!missing.isEmpty()) {
			ETSAssert.failWithUri(requirement,
					endpoint + " omits descendant " + relation + " resource ids " + missing + ".");
		}
	}

	private static URI resolveLink(Map<?, ?> link, URI source, URI apiRoot, String relation, String requirement) {
		Object href = link.get("href");
		if (!(href instanceof String) || ((String) href).isBlank()) {
			ETSAssert.failWithUri(requirement, source + " rel=" + relation + " link is missing an href.");
		}
		URI actual;
		try {
			actual = source.resolve((String) href);
		}
		catch (IllegalArgumentException ex) {
			ETSAssert.failWithUri(requirement,
					source + " rel=" + relation + " link has an invalid href: " + href + ".");
			return source;
		}
		if (!sameOrigin(apiRoot, actual)) {
			ETSAssert.failWithUri(requirement,
					"refusing cross-origin " + relation + " URL from " + apiRoot + " to " + actual + ".");
		}
		return actual;
	}

	private static Optional<URI> safeAssociationLink(Map<?, ?> link, URI source, URI apiRoot, String relation,
			String requirement) {
		Object href = link.get("href");
		if (!(href instanceof String) || ((String) href).isBlank()) {
			Reporter.log(requirement + " - ignoring rel=" + relation + " occurrence without a usable href.", true);
			return Optional.empty();
		}
		URI actual;
		try {
			actual = source.resolve((String) href);
		}
		catch (IllegalArgumentException ex) {
			Reporter.log(requirement + " - ignoring rel=" + relation + " occurrence with invalid href '" + href + "'.",
					true);
			return Optional.empty();
		}
		if (!sameOrigin(apiRoot, actual)) {
			Reporter.log(requirement + " - refusing cross-origin rel=" + relation + " candidate " + actual + ".", true);
			return Optional.empty();
		}
		return Optional.of(actual);
	}

	private static void assertExact(Set<String> actual, Set<String> expected, String requirement, String message) {
		Set<String> missing = new LinkedHashSet<>(expected);
		missing.removeAll(actual);
		Set<String> unexpected = new LinkedHashSet<>(actual);
		unexpected.removeAll(expected);
		if (!missing.isEmpty() || !unexpected.isEmpty()) {
			ETSAssert.failWithUri(requirement, message + "; missing " + missing + ", unexpected " + unexpected + ".");
		}
	}

	private static String requireId(String value, String requirement) {
		if (value == null || value.isBlank()) {
			ETSAssert.failWithUri(requirement, "hierarchy contains a blank Deployment id.");
		}
		return value.trim();
	}

	private static String requireFixtureToken(String value, String label) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(label + " must not be blank");
		}
		return value.trim();
	}

	private static void assertAcyclic(Map<String, Set<String>> directChildren) {
		Map<String, Integer> incoming = new LinkedHashMap<>();
		directChildren.keySet().forEach(node -> incoming.put(node, 0));
		directChildren.values()
			.forEach(children -> children.forEach(child -> incoming.compute(child, (ignored, count) -> count + 1)));
		Deque<String> pending = new ArrayDeque<>();
		incoming.forEach((node, count) -> {
			if (count == 0) {
				pending.addLast(node);
			}
		});
		int visited = 0;
		while (!pending.isEmpty()) {
			String node = pending.removeFirst();
			visited++;
			for (String child : directChildren.getOrDefault(node, Set.of())) {
				int remaining = incoming.computeIfPresent(child, (ignored, count) -> count - 1);
				if (remaining == 0) {
					pending.addLast(child);
				}
			}
		}
		if (visited != incoming.size()) {
			Set<String> cyclic = new LinkedHashSet<>();
			incoming.forEach((node, count) -> {
				if (count > 0) {
					cyclic.add(node);
				}
			});
			ETSAssert.failWithUri(HIERARCHY_REQUIREMENT, "hierarchy cycle detected among Deployments " + cyclic + ".");
		}
	}

	private static void assertNoShortcutEdges(Map<String, Set<String>> directChildren) {
		for (Map.Entry<String, Set<String>> entry : directChildren.entrySet()) {
			for (String directChild : entry.getValue()) {
				Deque<String> pending = new ArrayDeque<>(entry.getValue());
				pending.remove(directChild);
				Set<String> visited = new LinkedHashSet<>();
				while (!pending.isEmpty()) {
					String candidate = pending.removeFirst();
					if (directChild.equals(candidate)) {
						ETSAssert.failWithUri(HIERARCHY_REQUIREMENT,
								"Deployment '" + entry.getKey() + "' reports '" + directChild
										+ "' as a direct child although it is also reachable through a longer path.");
					}
					if (visited.add(candidate)) {
						pending.addAll(directChildren.getOrDefault(candidate, Set.of()));
					}
				}
			}
		}
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

	private static String encode(String id) {
		return URLEncoder.encode(requireId(id, HIERARCHY_REQUIREMENT), StandardCharsets.UTF_8).replace("+", "%20");
	}

	private static boolean sameOrigin(URI left, URI right) {
		return left != null && right != null && left.getScheme() != null && right.getScheme() != null
				&& left.getScheme().equalsIgnoreCase(right.getScheme()) && left.getHost() != null
				&& right.getHost() != null && left.getHost().equalsIgnoreCase(right.getHost())
				&& effectivePort(left) == effectivePort(right);
	}

	private static boolean sameHttpTarget(URI expected, URI actual) {
		return sameOrigin(expected, actual) && Objects.equals(expected.getRawUserInfo(), actual.getRawUserInfo())
				&& normalizedPath(expected).equals(normalizedPath(actual)) && actual.getRawQuery() == null
				&& actual.getRawFragment() == null;
	}

	private static String normalizedPath(URI uri) {
		String rawPath = uri.normalize().getRawPath();
		if (rawPath == null) {
			return "";
		}
		StringBuilder result = new StringBuilder(rawPath.length());
		for (int index = 0; index < rawPath.length(); index++) {
			char current = rawPath.charAt(index);
			if (current == '%' && index + 2 < rawPath.length()) {
				int high = Character.digit(rawPath.charAt(index + 1), 16);
				int low = Character.digit(rawPath.charAt(index + 2), 16);
				if (high >= 0 && low >= 0) {
					char decoded = (char) ((high << 4) + low);
					if (isUnreserved(decoded)) {
						result.append(decoded);
					}
					else {
						result.append('%')
							.append(Character.toUpperCase(rawPath.charAt(index + 1)))
							.append(Character.toUpperCase(rawPath.charAt(index + 2)));
					}
					index += 2;
					continue;
				}
			}
			result.append(current);
		}
		return result.toString();
	}

	private static boolean isUnreserved(char value) {
		return value >= 'a' && value <= 'z' || value >= 'A' && value <= 'Z' || value >= '0' && value <= '9'
				|| value == '-' || value == '.' || value == '_' || value == '~';
	}

	private static boolean isJsonCompatible(String type) {
		String normalized = type.split(";", 2)[0].trim().toLowerCase(java.util.Locale.ROOT);
		return "application/json".equals(normalized)
				|| normalized.startsWith("application/") && normalized.endsWith("+json");
	}

	private static int effectivePort(URI uri) {
		if (uri.getPort() >= 0) {
			return uri.getPort();
		}
		return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
	}

	static final class Hierarchy {

		private final Map<String, Set<String>> directChildren;

		private final Set<String> allNodes;

		private final Set<String> childNodes;

		private Hierarchy(Map<String, Set<String>> directChildren, Set<String> allNodes) {
			this.directChildren = Collections.unmodifiableMap(new LinkedHashMap<>(directChildren));
			this.allNodes = Collections.unmodifiableSet(new LinkedHashSet<>(allNodes));
			Set<String> children = new LinkedHashSet<>();
			directChildren.values().forEach(children::addAll);
			this.childNodes = Collections.unmodifiableSet(children);
		}

		Set<String> directChildren(String parent) {
			return this.directChildren.getOrDefault(parent, Set.of());
		}

		Set<String> descendants(String parent) {
			Set<String> descendants = new LinkedHashSet<>();
			Deque<String> pending = new ArrayDeque<>(directChildren(parent));
			while (!pending.isEmpty()) {
				String child = pending.removeFirst();
				if (descendants.add(child)) {
					pending.addAll(directChildren(child));
				}
			}
			return Collections.unmodifiableSet(descendants);
		}

		Set<String> transitiveDescendants(String parent) {
			Set<String> descendants = new LinkedHashSet<>(descendants(parent));
			descendants.removeAll(directChildren(parent));
			return Collections.unmodifiableSet(descendants);
		}

		Set<String> allNodes() {
			return this.allNodes;
		}

		Set<String> childNodes() {
			return this.childNodes;
		}

		Set<String> rootNodes() {
			Set<String> roots = new LinkedHashSet<>(this.allNodes);
			roots.removeAll(this.childNodes);
			return Collections.unmodifiableSet(roots);
		}

		Set<String> parents() {
			Set<String> parents = new LinkedHashSet<>();
			this.directChildren.forEach((parent, children) -> {
				if (!children.isEmpty()) {
					parents.add(parent);
				}
			});
			return Collections.unmodifiableSet(parents);
		}

	}

	static final class AssociationEvidence {

		private final Map<String, Map<String, Set<String>>> deployments;

		private AssociationEvidence(Map<String, Map<String, Set<String>>> deployments) {
			this.deployments = deployments;
		}

		Optional<Set<String>> expectedIds(String parent, Hierarchy hierarchy, String relation) {
			Set<String> deploymentsToInspect = new LinkedHashSet<>();
			deploymentsToInspect.add(parent);
			deploymentsToInspect.addAll(hierarchy.descendants(parent));
			Set<String> expected = new LinkedHashSet<>();
			for (String deployment : deploymentsToInspect) {
				Map<String, Set<String>> relations = this.deployments.get(deployment);
				if (relations == null || !relations.containsKey(relation)) {
					return Optional.empty();
				}
				expected.addAll(relations.get(relation));
			}
			return Optional.of(Collections.unmodifiableSet(expected));
		}

	}

}
