package org.opengis.cite.ogcapiconnectedsystems10.conformance.subsystems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * Focused graph checks for the five released Subsystem procedures.
 */
public class VerifySubsystemsSupport {

	private static final String REQUIREMENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/subsystem/recursive-search-subsystems";

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-HIERARCHY-FAIL-CLOSED-001.
	 */
	@Test
	public void hierarchySeparatesDirectAndTransitiveDescendants() {
		SubsystemsSupport.Hierarchy hierarchy = SubsystemsSupport
			.hierarchy(Map.of("root", List.of("child"), "child", List.of("grandchild"), "grandchild", List.of()));

		assertEquals(Set.of("child"), hierarchy.directChildren("root"));
		assertEquals(Set.of("child", "grandchild"), hierarchy.descendants("root"));
		assertEquals(Set.of("grandchild"), hierarchy.transitiveDescendants("root"));
		assertEquals(Set.of("root", "child", "grandchild"), hierarchy.allNodes());
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-HIERARCHY-FAIL-CLOSED-001.
	 */
	@Test
	public void hierarchyRejectsCyclesAndDuplicateDirectIds() {
		assertThrows(AssertionError.class,
				() -> SubsystemsSupport.hierarchy(Map.of("a", List.of("b"), "b", List.of("a"))));
		assertThrows(AssertionError.class,
				() -> SubsystemsSupport.hierarchy(Map.of("a", List.of("b", "b"), "b", List.of())));
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-HIERARCHY-FAIL-CLOSED-001.
	 */
	@Test
	public void hierarchyRejectsDirectEdgesThatAlsoHaveTransitivePaths() {
		assertThrows(AssertionError.class, () -> SubsystemsSupport.hierarchy(Map.of("root",
				List.of("child", "grandchild"), "child", List.of("grandchild"), "grandchild", List.of())));
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-RECURSIVE-SYSTEMS-001.
	 */
	@Test
	public void systemsComparisonRejectsDescendantsFromDefaultAndMissingRecursiveIds() {
		SubsystemsSupport.Hierarchy hierarchy = hierarchy();
		SubsystemsSupport.assertRecursiveSystems(hierarchy, Set.of("root"), Set.of("root"),
				Set.of("root", "child", "grandchild"), REQUIREMENT);

		assertThrows(AssertionError.class, () -> SubsystemsSupport.assertRecursiveSystems(hierarchy,
				Set.of("root", "child"), Set.of("root"), Set.of("root", "child", "grandchild"), REQUIREMENT));
		assertThrows(AssertionError.class, () -> SubsystemsSupport.assertRecursiveSystems(hierarchy, Set.of("root"),
				Set.of("root"), Set.of("root", "child"), REQUIREMENT));
		assertThrows(AssertionError.class, () -> SubsystemsSupport.assertRecursiveSystems(hierarchy, Set.of("root"),
				Set.of("root", "unexpected"), Set.of("root", "child", "grandchild"), REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-RECURSIVE-SUBSYSTEMS-001.
	 */
	@Test
	public void subsystemComparisonUsesIndependentDirectAndTransitiveSets() {
		SubsystemsSupport.Hierarchy hierarchy = hierarchy();
		SubsystemsSupport.assertRecursiveSubsystems("root", hierarchy, Set.of("child"), Set.of("child"),
				Set.of("child", "grandchild"), REQUIREMENT);

		assertThrows(AssertionError.class, () -> SubsystemsSupport.assertRecursiveSubsystems("root", hierarchy,
				Set.of("child", "grandchild"), Set.of("child"), Set.of("child", "grandchild"), REQUIREMENT));
		assertThrows(AssertionError.class, () -> SubsystemsSupport.assertRecursiveSubsystems("root", hierarchy,
				Set.of("child"), Set.of("child"), Set.of("child", "grandchild", "unexpected"), REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-COLLECTION-001.
	 */
	@Test
	public void subsystemLinkMustBeUniqueAndMatchNormativeTarget() {
		URI apiRoot = URI.create("https://example.test/api/");
		String expected = apiRoot.resolve("systems/root/subsystems").toString();
		Map<String, Object> valid = Map.of("links", List.of(Map.of("rel", "subsystems", "href", expected)));
		Map<String, Object> ambiguous = Map.of("links", List.of(Map.of("rel", "subsystems", "href", expected),
				Map.of("rel", "subsystems", "href", "../other")));
		Map<String, Object> duplicate = Map.of("links",
				List.of(Map.of("rel", "subsystems", "href", expected), Map.of("rel", "subsystems", "href", expected)));

		assertEquals(URI.create("https://example.test/api/systems/root/subsystems"),
				SubsystemsSupport.subsystemsUri(valid, apiRoot.resolve("systems/root"), apiRoot, "root", REQUIREMENT));
		assertThrows(AssertionError.class, () -> SubsystemsSupport.subsystemsUri(ambiguous,
				apiRoot.resolve("systems/root"), apiRoot, "root", REQUIREMENT));
		assertThrows(AssertionError.class, () -> SubsystemsSupport.subsystemsUri(duplicate,
				apiRoot.resolve("systems/root"), apiRoot, "root", REQUIREMENT));
		for (String variant : List.of(expected + "/", expected + "?recursive=false", expected + "#collection",
				"https://other.test/api/systems/root/subsystems")) {
			Map<String, Object> invalid = Map.of("links", List.of(Map.of("rel", "subsystems", "href", variant)));
			assertThrows(AssertionError.class, () -> SubsystemsSupport.subsystemsUri(invalid,
					apiRoot.resolve("systems/root"), apiRoot, "root", REQUIREMENT));
		}
	}

	private static SubsystemsSupport.Hierarchy hierarchy() {
		return SubsystemsSupport
			.hierarchy(Map.of("root", List.of("child"), "child", List.of("grandchild"), "grandchild", List.of()));
	}

}
