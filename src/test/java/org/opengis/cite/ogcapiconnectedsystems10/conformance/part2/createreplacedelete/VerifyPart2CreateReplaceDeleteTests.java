package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.createreplacedelete;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Regression coverage for S-ETS-26-01 Part 2 Create/Replace/Delete safety gating.
 *
 * <p>
 * Traceability: REQ-ETS-PART2-007, SCENARIO-ETS-PART2-007-CRD-CONFORMANCE-DECLARED-001,
 * SCENARIO-ETS-PART2-007-FEATURES4-PREREQUISITE-001,
 * SCENARIO-ETS-PART2-007-MUTATION-SAFETY-GATE-001,
 * SCENARIO-ETS-PART2-007-OPTIONS-READINESS-READONLY-001, and
 * SCENARIO-ETS-PART2-007-UNAVAILABLE-ENDPOINT-HONESTY-001.
 * </p>
 */
public class VerifyPart2CreateReplaceDeleteTests {

	private static final List<String> RELEASED_CHILD_TARGETS = List.of(Part2CreateReplaceDeleteTests.REQ_DATASTREAM,
			Part2CreateReplaceDeleteTests.REQ_DATASTREAM_UPDATE_SCHEMA,
			Part2CreateReplaceDeleteTests.REQ_DATASTREAM_DELETE_CASCADE, Part2CreateReplaceDeleteTests.REQ_OBSERVATION,
			Part2CreateReplaceDeleteTests.REQ_OBSERVATION_SCHEMA, Part2CreateReplaceDeleteTests.REQ_CONTROLSTREAM,
			Part2CreateReplaceDeleteTests.REQ_CONTROLSTREAM_UPDATE_SCHEMA,
			Part2CreateReplaceDeleteTests.REQ_CONTROLSTREAM_DELETE_CASCADE, Part2CreateReplaceDeleteTests.REQ_COMMAND,
			Part2CreateReplaceDeleteTests.REQ_COMMAND_SCHEMA, Part2CreateReplaceDeleteTests.REQ_COMMAND_STATUS,
			Part2CreateReplaceDeleteTests.REQ_COMMAND_RESULT, Part2CreateReplaceDeleteTests.REQ_FEASIBILITY,
			Part2CreateReplaceDeleteTests.REQ_FEASIBILITY_STATUS, Part2CreateReplaceDeleteTests.REQ_FEASIBILITY_RESULT,
			Part2CreateReplaceDeleteTests.REQ_SYSTEM_EVENT);

	@BeforeClass
	public static void verifyFixtureTargetsAreUnique() {
		assertEquals("REQ-ETS-PART2-007 fixture should enumerate all sixteen released Annex A.7 child targets", 16,
				new LinkedHashSet<>(RELEASED_CHILD_TARGETS).size());
	}

	@Test
	public void officialPart2AndFeatures4IdentifiersAreExposed() {
		String joined = String.join(" ", Part2CreateReplaceDeleteTests.CONF_CREATE_REPLACE_DELETE,
				Part2CreateReplaceDeleteTests.REQ_CREATE_REPLACE_DELETE,
				Part2CreateReplaceDeleteTests.CONF_FEATURES4_CREATE_REPLACE_DELETE,
				String.join(" ", RELEASED_CHILD_TARGETS));

		assertTrue(joined.contains("ogcapi-connectedsystems-2/1.0/conf/create-replace-delete"));
		assertTrue(joined.contains("ogcapi-connectedsystems-2/1.0/req/create-replace-delete"));
		assertTrue(joined.contains("ogcapi-features-4/1.0/conf/create-replace-delete"));
		assertFalse(joined.contains("ogcapi-connectedsystems-1/1.0/conf/create-replace-delete"));
	}

	@Test
	public void releasedAnnexA7TargetsHaveOneDeployedMethodEach() {
		// REQ-ETS-PART2-007; SCENARIO-ETS-PART2-007-RELEASED-METHOD-SURFACE-001.
		Map<String, List<String>> methodsByTarget = new HashMap<>();
		List<String> multiTargetMethods = new ArrayList<>();
		int releasedTargetMethods = 0;
		for (Method method : Part2CreateReplaceDeleteTests.class.getDeclaredMethods()) {
			org.testng.annotations.Test annotation = method.getAnnotation(org.testng.annotations.Test.class);
			if (annotation == null || !annotation.enabled()) {
				continue;
			}
			List<String> matchedTargets = RELEASED_CHILD_TARGETS.stream()
				.filter(target -> containsCanonicalTarget(annotation.description(), target))
				.toList();
			if (matchedTargets.size() > 1) {
				multiTargetMethods.add(method.getName() + " -> " + matchedTargets);
			}
			if (matchedTargets.size() == 1) {
				releasedTargetMethods++;
				methodsByTarget.computeIfAbsent(matchedTargets.get(0), ignored -> new ArrayList<>())
					.add(method.getName());
			}
		}

		assertTrue("Foundational Part 2 CRD safety/setup methods must not carry multiple released child targets: "
				+ multiTargetMethods, multiTargetMethods.isEmpty());
		assertEquals("Part 2 CRD SHALL expose one deployed method per released Annex A.7 target", 16,
				releasedTargetMethods);
		for (String target : RELEASED_CHILD_TARGETS) {
			List<String> methods = methodsByTarget.getOrDefault(target, List.of());
			assertEquals("Expected exactly one deployed Part 2 CRD method for " + target + ": " + methods, 1,
					methods.size());
		}
	}

	@Test
	public void readinessPathsUseNormativeScopedCreateEndpoints() {
		assertEquals("systems/sys-1/datastreams",
				Part2CreateReplaceDeleteTests.systemScopedCollectionPath("sys-1", "datastreams"));
		assertEquals("systems/sys-1/datastreams/ds-1",
				Part2CreateReplaceDeleteTests.systemScopedResourcePath("sys-1", "datastreams", "ds-1"));
		assertEquals("datastreams/ds-1/observations",
				Part2CreateReplaceDeleteTests.datastreamScopedObservationsPath("ds-1"));
		assertEquals("systems/sys-1/controlstreams",
				Part2CreateReplaceDeleteTests.systemScopedCollectionPath("sys-1", "controlstreams"));
		assertFalse("Part 2 CRD CREATE readiness must not use the global /datastreams collection",
				"datastreams".equals(Part2CreateReplaceDeleteTests.systemScopedCollectionPath("sys-1", "datastreams")));
		assertFalse("Part 2 CRD CREATE readiness must not use the global /observations collection",
				"observations".equals(Part2CreateReplaceDeleteTests.datastreamScopedObservationsPath("ds-1")));
		assertFalse("Part 2 CRD CREATE readiness must not use the global /controlstreams collection", "controlstreams"
			.equals(Part2CreateReplaceDeleteTests.systemScopedCollectionPath("sys-1", "controlstreams")));
	}

	@Test
	public void exactConformanceDeclarationIsRequired() {
		Map<String, Object> body = Map.of("conformsTo",
				List.of(Part2CreateReplaceDeleteTests.CONF_CREATE_REPLACE_DELETE));

		assertTrue(Part2CreateReplaceDeleteTests.declaresConformance(body,
				Part2CreateReplaceDeleteTests.CONF_CREATE_REPLACE_DELETE));
		assertFalse(Part2CreateReplaceDeleteTests.declaresConformance(body,
				"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/create-replace-delete"));
		assertFalse(Part2CreateReplaceDeleteTests.declaresConformance(Map.of(),
				Part2CreateReplaceDeleteTests.CONF_CREATE_REPLACE_DELETE));
	}

	@Test
	public void publicGeoRobotixIutIsHardDeniedEvenWhenMutationOptInIsSet() {
		URI georobotix = URI.create("https://api.georobotix.io/ogc/t18/api/");

		assertTrue(Part2CreateReplaceDeleteTests.isPublicGeoRobotixIut(georobotix));
		String reason = Part2CreateReplaceDeleteTests.mutationGateSkipReason(georobotix, "true",
				"dedicated-mutable-iut", Part2CreateReplaceDeleteTests.REQ_CREATE_REPLACE_DELETE);

		assertTrue(reason.contains("public GeoRobotix"));
		assertTrue(reason.contains("No POST/PUT/DELETE/PATCH request was issued"));
	}

	@Test
	public void mutationGateRequiresBothExplicitParametersForNonPublicIut() {
		URI local = URI.create("http://field-hub-osh-1:8081/sensorhub/api");

		assertTrue(Part2CreateReplaceDeleteTests
			.mutationGateSkipReason(local, "", "dedicated-mutable-iut",
					Part2CreateReplaceDeleteTests.REQ_CREATE_REPLACE_DELETE)
			.contains("mutation-tests-enabled"));
		assertTrue(Part2CreateReplaceDeleteTests
			.mutationGateSkipReason(local, "true", "", Part2CreateReplaceDeleteTests.REQ_CREATE_REPLACE_DELETE)
			.contains("mutation-iut-policy"));
		assertNull(Part2CreateReplaceDeleteTests.mutationGateSkipReason(local, "true", "dedicated-mutable-iut",
				Part2CreateReplaceDeleteTests.REQ_CREATE_REPLACE_DELETE));
	}

	@Test
	public void allowHeaderParsingIsCaseInsensitiveAndCommaDelimited() {
		assertTrue(Part2CreateReplaceDeleteTests.allowHeaderContains("GET, HEAD, post, OPTIONS", "POST"));
		assertTrue(Part2CreateReplaceDeleteTests.allowHeaderContains("GET,PUT,DELETE", "delete"));
		assertFalse(Part2CreateReplaceDeleteTests.allowHeaderContains("GET, HEAD, OPTIONS", "POST"));
		assertFalse(Part2CreateReplaceDeleteTests.allowHeaderContains(null, "POST"));
	}

	@Test
	public void collectionShapeRequiresItemsArray() {
		assertTrue(Part2CreateReplaceDeleteTests.hasItemsOnlyCollectionShape(Map.of("items", List.of())));
		assertFalse(Part2CreateReplaceDeleteTests.hasItemsOnlyCollectionShape(Map.of("links", List.of())));
		assertFalse(Part2CreateReplaceDeleteTests.hasItemsOnlyCollectionShape(null));
	}

	@Test
	public void associatedSystemIdUsesExplicitParentEvidenceOnly() {
		assertEquals("sys-a", Part2CreateReplaceDeleteTests.associatedSystemId(Map.of("system@id", "sys-a")));
		assertEquals("sys-b", Part2CreateReplaceDeleteTests.associatedSystemId(Map.of("systemId", "sys-b")));
		assertEquals("sys-c",
				Part2CreateReplaceDeleteTests.associatedSystemId(Map.of("system", Map.of("id", "sys-c"))));
		assertNull(Part2CreateReplaceDeleteTests.associatedSystemId(Map.of("id", "resource-without-parent")));
		assertNull(Part2CreateReplaceDeleteTests.associatedSystemId(null));
	}

	@Test
	public void groupNameIsStableForTestNgWiring() {
		assertEquals("part2createreplacedelete", Part2CreateReplaceDeleteTests.GROUP);
	}

	private static boolean containsCanonicalTarget(String description, String target) {
		int start = description.indexOf(target);
		while (start >= 0) {
			int end = start + target.length();
			boolean leftBoundary = start == 0 || isTargetDelimiter(description.charAt(start - 1));
			boolean rightBoundary = end == description.length() || isTargetDelimiter(description.charAt(end));
			if (leftBoundary && rightBoundary) {
				return true;
			}
			start = description.indexOf(target, start + 1);
		}
		return false;
	}

	private static boolean isTargetDelimiter(char character) {
		return Character.isWhitespace(character) || character == '(' || character == ')' || character == ','
				|| character == ';' || character == ':';
	}

}
