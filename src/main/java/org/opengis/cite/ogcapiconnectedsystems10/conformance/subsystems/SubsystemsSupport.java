package org.opengis.cite.ogcapiconnectedsystems10.conformance.subsystems;

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
import java.util.Set;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonSupport.TraversalResult;

/**
 * Independent hierarchy and comparison support for released Subsystem procedures.
 */
final class SubsystemsSupport {

	private static final int MAX_NODES = 10_000;

	private static final String HIERARCHY_REQUIREMENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/subsystem";

	private SubsystemsSupport() {
	}

	static Hierarchy hierarchy(Map<String, List<String>> directChildren) {
		if (directChildren == null) {
			throw new IllegalArgumentException("directChildren must not be null");
		}
		Map<String, Set<String>> normalized = new LinkedHashMap<>();
		Set<String> nodes = new LinkedHashSet<>();
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

	static void assertRecursiveSystems(Hierarchy hierarchy, Set<String> defaultIds, Set<String> falseIds,
			Set<String> trueIds, String requirement) {
		assertExact(defaultIds, hierarchy.rootNodes(), requirement,
				"default /systems response does not contain exactly the hierarchy roots");
		assertExact(falseIds, hierarchy.rootNodes(), requirement,
				"recursive=false /systems response does not contain exactly the hierarchy roots");
		assertExact(trueIds, hierarchy.allNodes(), requirement,
				"recursive=true /systems response does not contain exactly all hierarchy nodes");
	}

	static void assertRecursiveSubsystems(String parent, Hierarchy hierarchy, Set<String> defaultIds,
			Set<String> falseIds, Set<String> trueIds, String requirement) {
		Set<String> direct = hierarchy.directChildren(parent);
		assertExact(defaultIds, direct, requirement,
				"default subsystem response does not contain exactly the direct children of '" + parent + "'");
		assertExact(falseIds, direct, requirement,
				"recursive=false subsystem response does not contain exactly the direct children of '" + parent + "'");
		assertExact(trueIds, hierarchy.descendants(parent), requirement,
				"recursive=true subsystem response does not contain exactly the descendants of '" + parent + "'");
	}

	static URI subsystemsUri(Map<String, Object> parent, URI parentSource, URI apiRoot, String parentId,
			String requirement) {
		Object links = parent == null ? null : parent.get("links");
		if (!(links instanceof List)) {
			ETSAssert.failWithUri(requirement, parentSource + " System resource is missing a links array.");
		}
		List<URI> candidates = new ArrayList<>();
		for (Object value : (List<?>) links) {
			if (!(value instanceof Map)) {
				continue;
			}
			Map<?, ?> link = (Map<?, ?>) value;
			if (!hasRelation(link.get("rel"), "subsystems")) {
				continue;
			}
			Object href = link.get("href");
			if (!(href instanceof String) || ((String) href).isBlank()) {
				ETSAssert.failWithUri(requirement, parentSource + " rel=subsystems link is missing an href.");
			}
			try {
				candidates.add(parentSource.resolve((String) href));
			}
			catch (IllegalArgumentException ex) {
				ETSAssert.failWithUri(requirement,
						parentSource + " rel=subsystems link has an invalid href: " + href + ".");
			}
		}
		if (candidates.size() != 1) {
			ETSAssert.failWithUri(requirement,
					parentSource + " must expose exactly one rel=subsystems link; found " + candidates + ".");
		}
		URI actual = candidates.get(0);
		if (!sameOrigin(apiRoot, actual)) {
			ETSAssert.failWithUri(requirement,
					"refusing cross-origin subsystem URL from " + apiRoot + " to " + actual + ".");
		}
		URI expected = apiRoot.resolve("systems/" + encode(parentId) + "/subsystems");
		if (!expected.equals(actual)) {
			ETSAssert.failWithUri(requirement,
					parentSource + " rel=subsystems target must be " + expected + " but was " + actual + ".");
		}
		return actual;
	}

	static URI systemUri(URI apiRoot, String systemId) {
		return apiRoot.resolve("systems/" + encode(systemId));
	}

	static URI subsystemCollectionUri(URI apiRoot, String parentId) {
		return URI.create(systemUri(apiRoot, parentId) + "/subsystems");
	}

	static URI associationUri(URI apiRoot, String systemId, String association) {
		return URI.create(systemUri(apiRoot, systemId) + "/" + association);
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
			ETSAssert.failWithUri(requirement, "hierarchy contains a blank System id.");
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
			ETSAssert.failWithUri(HIERARCHY_REQUIREMENT, "hierarchy cycle detected among Systems " + cyclic + ".");
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
								"System '" + entry.getKey() + "' reports '" + directChild
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

}
