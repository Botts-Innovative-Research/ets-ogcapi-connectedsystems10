package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.feasibility;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

/**
 * Unit checks for the Sprint 62 Part 2 Feasibility exact released ATS closure.
 */
public class VerifyPart2FeasibilityTests {

	private static final Set<String> RELEASED_METHODS = Set.of("feasibilityCanonicalUrlFromCommandCollections",
			"feasibilityReferenceFromControlStreamUsesReleasedCommandEndpoint",
			"feasibilityStatusEndpointReadableForEveryFeasibility",
			"feasibilityResultEndpointReadableForEveryFeasibility", "feasibilityCollectionsValidateCommandSchema");

	@org.junit.Test
	public void constantsUseOfficialFeasibilityIdentifiers() {
		String joined = String.join(" ", Part2FeasibilityTests.CONF_FEASIBILITY, Part2FeasibilityTests.REQ_FEASIBILITY,
				Part2FeasibilityTests.REQ_REF_FROM_CONTROLSTREAM);

		assertTrue(joined.contains("/conf/feasibility"));
		assertTrue(joined.contains("/req/feasibility"));
		assertFalse(joined.contains("commandfeasibility"));
		assertFalse(joined.contains("dynamic"));
	}

	@org.junit.Test
	public void deployedClassContainsExactlyFiveReleasedAtsMethods() {
		Set<String> actual = new LinkedHashSet<>();
		for (Method method : Part2FeasibilityTests.class.getDeclaredMethods()) {
			if (method.getAnnotation(Test.class) != null) {
				actual.add(method.getName());
			}
		}

		assertEquals("REQ-ETS-PART2-004; SCENARIO-ETS-PART2-004-RELEASED-PROCEDURES-001", RELEASED_METHODS, actual);
	}

	@org.junit.Test
	public void releasedMethodDescriptionsTraceExactTargets() {
		for (Method method : Part2FeasibilityTests.class.getDeclaredMethods()) {
			Test test = method.getAnnotation(Test.class);
			if (test == null) {
				continue;
			}
			String description = test.description();
			assertTrue(method.getName() + " missing REQ-ETS-PART2-004 trace",
					description.contains("REQ-ETS-PART2-004"));
			assertTrue(method.getName() + " missing /req/feasibility URI", description.contains("/req/feasibility/"));
			assertTrue(method.getName() + " missing scenario trace", description.contains("SCENARIO-ETS-PART2-004-"));
			assertTrue(method.getName() + " should be alwaysRun so declaration/prerequisite skips stay visible",
					test.alwaysRun());
			assertTrue(method.getName() + " should carry part2feasibility group",
					Arrays.asList(test.groups()).contains(Part2FeasibilityTests.GROUP));
		}
	}

	@org.junit.Test
	public void releasedRefFromControlStreamUsesAnnexCommandEndpointPath() {
		String path = Part2FeasibilityTests.releasedControlStreamCommandPath("cs-1");

		assertEquals("controlstreams/cs-1/commands", path);
		assertFalse("Sprint 62 implements released Annex A.4 literally, not the older singular feasibility subset.",
				path.startsWith("controlstream/"));
		assertFalse("Sprint 62 shall not substitute a feasibility endpoint for the released command endpoint.",
				path.endsWith("/feasibility"));
	}

	@org.junit.Test
	public void canonicalUrlReleasedProcedureSelectsCommandCollections() {
		assertTrue(
				Part2FeasibilityTests.isCollectionWithItemType(Map.of("id", "c1", "itemType", "Command"), "Command"));
		assertFalse("A.35 released text selects itemType=Command, not itemType=Feasibility.", Part2FeasibilityTests
			.isCollectionWithItemType(Map.of("id", "c1", "itemType", "Feasibility"), "Command"));
	}

	@org.junit.Test
	public void feasibilityCollectionRequiresExactItemType() {
		assertTrue(Part2FeasibilityTests.isCollectionWithItemType(Map.of("id", "c1", "itemType", "Feasibility"),
				"Feasibility"));
		assertFalse(Part2FeasibilityTests.isCollectionWithItemType(Map.of("id", "c1", "itemType", "Command"),
				"Feasibility"));
		assertFalse(Part2FeasibilityTests.isCollectionWithItemType(Map.of("id", "c1"), "Feasibility"));
	}

	@org.junit.Test
	public void itemsOnlyCollectionShapeAllowsEmptyFeasibilityCollections() {
		assertTrue(Part2FeasibilityTests.hasItemsOnlyCollectionShape(Map.of("items", List.of())));
		assertFalse(Part2FeasibilityTests.hasItemsOnlyCollectionShape(Map.of("links", List.of())));
	}

}
