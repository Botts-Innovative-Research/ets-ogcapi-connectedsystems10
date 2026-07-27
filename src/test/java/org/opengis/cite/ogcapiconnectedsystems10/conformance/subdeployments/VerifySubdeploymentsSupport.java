package org.opengis.cite.ogcapiconnectedsystems10.conformance.subdeployments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;

/**
 * Focused graph and link checks for the five released Subdeployment procedures.
 */
public class VerifySubdeploymentsSupport {

	private static final String REQUIREMENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/subdeployment/recursive-search-subdeployments";

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-HIERARCHY-FAIL-CLOSED-001.
	 */
	@Test
	public void hierarchySeparatesRootsDirectChildrenAndDescendants() {
		SubdeploymentsSupport.Hierarchy hierarchy = hierarchy();

		assertEquals(Set.of("root"), hierarchy.rootNodes());
		assertEquals(Set.of("child"), hierarchy.directChildren("root"));
		assertEquals(Set.of("child", "grandchild"), hierarchy.descendants("root"));
		assertEquals(Set.of("grandchild"), hierarchy.transitiveDescendants("root"));
		assertEquals(Set.of("root", "child", "grandchild"), hierarchy.allNodes());
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-HIERARCHY-FAIL-CLOSED-001.
	 */
	@Test
	public void hierarchyRejectsCyclesDuplicateIdsAndShortcutEdges() {
		assertThrows(AssertionError.class,
				() -> SubdeploymentsSupport.hierarchy(Map.of("a", List.of("b"), "b", List.of("a"))));
		assertThrows(AssertionError.class,
				() -> SubdeploymentsSupport.hierarchy(Map.of("a", List.of("b", "b"), "b", List.of())));
		assertThrows(AssertionError.class, () -> SubdeploymentsSupport
			.hierarchy(Map.of("a", List.of("shared"), "b", List.of("shared"), "shared", List.of())));
		assertThrows(AssertionError.class, () -> SubdeploymentsSupport.hierarchy(Map.of("root",
				List.of("child", "grandchild"), "child", List.of("grandchild"), "grandchild", List.of())));
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-RECURSIVE-DEPLOYMENTS-001.
	 */
	@Test
	public void deploymentComparisonUsesExactRootAndAllNodeSets() {
		SubdeploymentsSupport.Hierarchy hierarchy = hierarchy();
		SubdeploymentsSupport.assertRecursiveDeployments(hierarchy, Set.of("root"), Set.of("root"),
				Set.of("root", "child", "grandchild"), REQUIREMENT);

		assertThrows(AssertionError.class, () -> SubdeploymentsSupport.assertRecursiveDeployments(hierarchy,
				Set.of("root", "child"), Set.of("root"), Set.of("root", "child", "grandchild"), REQUIREMENT));
		assertThrows(AssertionError.class, () -> SubdeploymentsSupport.assertRecursiveDeployments(hierarchy,
				Set.of("root"), Set.of("root"), Set.of("root", "child"), REQUIREMENT));
		assertThrows(AssertionError.class, () -> SubdeploymentsSupport.assertRecursiveDeployments(hierarchy,
				Set.of("root"), Set.of("root", "unexpected"), Set.of("root", "child", "grandchild"), REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-RECURSIVE-SUBDEPLOYMENTS-001.
	 */
	@Test
	public void subdeploymentComparisonUsesExactDirectAndDescendantSets() {
		SubdeploymentsSupport.Hierarchy hierarchy = hierarchy();
		SubdeploymentsSupport.assertRecursiveSubdeployments("root", hierarchy, Set.of("child"), Set.of("child"),
				Set.of("child", "grandchild"), REQUIREMENT);

		assertThrows(AssertionError.class, () -> SubdeploymentsSupport.assertRecursiveSubdeployments("root", hierarchy,
				Set.of("child", "grandchild"), Set.of("child"), Set.of("child", "grandchild"), REQUIREMENT));
		assertThrows(AssertionError.class, () -> SubdeploymentsSupport.assertRecursiveSubdeployments("root", hierarchy,
				Set.of("child"), Set.of("child"), Set.of("child", "grandchild", "unexpected"), REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-LINK-EXACT-001.
	 */
	@Test
	public void everySubdeploymentLinkOccurrenceMustMatchNormativeTarget() {
		URI apiRoot = URI.create("https://example.test/api/");
		URI parent = apiRoot.resolve("deployments/root");
		String expected = apiRoot.resolve("deployments/root/subdeployments").toString();
		Map<String, Object> valid = Map.of("links", List.of(Map.of("rel", "subdeployments", "href", expected),
				Map.of("rel", List.of("alternate", "subdeployments"), "href", expected)));

		assertEquals(URI.create(expected),
				SubdeploymentsSupport.subdeploymentsUri(valid, parent, apiRoot, "root", REQUIREMENT));

		for (String variant : List.of(expected + "/", expected + "?recursive=false", expected + "#collection",
				apiRoot.resolve("deployments/other/subdeployments").toString(),
				"https://other.test/api/deployments/root/subdeployments")) {
			Map<String, Object> invalid = Map.of("links", List.of(Map.of("rel", "subdeployments", "href", expected),
					Map.of("rel", "subdeployments", "href", variant)));
			assertThrows(AssertionError.class,
					() -> SubdeploymentsSupport.subdeploymentsUri(invalid, parent, apiRoot, "root", REQUIREMENT));
		}
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-ASSOCIATION-LINK-001.
	 */
	@Test
	public void normalizedTargetAcceptsDefaultPortAndUnreservedEncoding() {
		URI apiRoot = URI.create("http://example.test/api/");
		URI parent = apiRoot.resolve("deployments/root");
		Map<String, Object> valid = Map.of("links", List.of(Map.of("rel", "subdeployments", "href",
				"http://EXAMPLE.test:80/api/deployments/%72oot/subdeployments")));

		assertEquals(URI.create("http://EXAMPLE.test:80/api/deployments/%72oot/subdeployments"),
				SubdeploymentsSupport.subdeploymentsUri(valid, parent, apiRoot, "root", REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-ASSOCIATION-LINK-001.
	 */
	@Test
	public void associationLinksPreferUsableSameOriginJsonOccurrence() {
		URI apiRoot = URI.create("https://example.test/api/");
		URI source = apiRoot.resolve("deployments/root");
		URI expected = apiRoot.resolve("deployments/root/systems");
		Map<String, Object> valid = Map.of("links",
				List.of(Map.of("rel", "deployedSystems", "type", "application/json", "href",
						"https://other.test/api/deployments/root/systems"),
						Map.of("rel", "deployedSystems", "type", "text/html", "href",
								apiRoot.resolve("deployments/root/systems.html").toString()),
						Map.of("rel", List.of("alternate", "deployedSystems"), "type", "application/json", "href",
								expected.toString())));

		assertEquals(Optional.of(expected),
				SubdeploymentsSupport.associationUri(valid, source, apiRoot, "deployedSystems", REQUIREMENT));
		assertEquals(Optional.empty(),
				SubdeploymentsSupport.associationUri(valid, source, apiRoot, "datastreams", REQUIREMENT));

		Map<String, Object> unusable = Map.of("links",
				List.of(Map.of("rel", "deployedSystems", "type", "application/json", "href",
						"https://other.test/api/deployments/root/systems"),
						Map.of("rel", "deployedSystems", "type", "text/html", "href",
								apiRoot.resolve("deployments/root/systems.html").toString())));
		assertEquals(Optional.empty(),
				SubdeploymentsSupport.associationUri(unusable, source, apiRoot, "deployedSystems", REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-ASSOCIATION-ORACLE-001.
	 */
	@Test
	public void associationFixtureBuildsParentPlusDescendantUnion() {
		String fixture = """
				{
				  "root":{"deployedSystems":["root-system"]},
				  "child":{"deployedSystems":["child-system"]},
				  "grandchild":{"deployedSystems":["grandchild-system"]}
				}
				""";
		SubdeploymentsSupport.AssociationEvidence evidence = SubdeploymentsSupport.associationEvidence(fixture);

		assertEquals(Set.of("root-system", "child-system", "grandchild-system"),
				evidence.expectedIds("root", hierarchy(), "deployedSystems").orElseThrow());
		assertEquals(Optional.empty(), evidence.expectedIds("root", hierarchy(), "datastreams"));
		assertThrows(IllegalArgumentException.class, () -> SubdeploymentsSupport
			.associationEvidence("{\"root\":{\"deployedSystems\":[\"same\",\"same\"]}}"));
	}

	private static SubdeploymentsSupport.Hierarchy hierarchy() {
		return SubdeploymentsSupport
			.hierarchy(Map.of("root", List.of("child"), "child", List.of("grandchild"), "grandchild", List.of()));
	}

}
