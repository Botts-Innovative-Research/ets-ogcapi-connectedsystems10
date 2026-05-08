package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.feasibility;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

/**
 * Unit checks for the Sprint 23 Part 2 Feasibility helper logic.
 */
public class VerifyPart2FeasibilityTests {

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
	public void normativeControlStreamFeasibilityPathUsesSingularControlstream() {
		String path = Part2FeasibilityTests.normativeControlStreamFeasibilityPath("cs-1");

		assertTrue(path.equals("controlstream/cs-1/feasibility"));
		assertFalse("Plural /controlstreams/{id}/feasibility is diagnostic only, not normative PASS evidence.",
				path.startsWith("controlstreams/"));
	}

	@org.junit.Test
	public void feasibilityResourceShapeRequiresResourceSpecificEvidence() {
		assertTrue(Part2FeasibilityTests
			.hasFeasibilityResourceShape(Map.of("id", "feas-1", "status", Map.of("code", "COMPLETED"))));
		assertTrue(Part2FeasibilityTests.hasFeasibilityResourceShape(
				Map.of("id", "feas-1", "controlstream@id", "cs-1", "parameters", Map.of("look", "north"))));
		assertFalse("A generic JSON object with only id/items must not masquerade as a Feasibility resource.",
				Part2FeasibilityTests.hasFeasibilityResourceShape(Map.of("id", "feas-1", "items", List.of())));
	}

	@org.junit.Test
	public void feasibilityCollectionRequiresExactItemType() {
		assertTrue(Part2FeasibilityTests.isFeasibilityCollection(Map.of("id", "c1", "itemType", "Feasibility")));
		assertFalse(Part2FeasibilityTests.isFeasibilityCollection(Map.of("id", "c1", "itemType", "Command")));
		assertFalse(Part2FeasibilityTests.isFeasibilityCollection(Map.of("id", "c1")));
	}

	@org.junit.Test
	public void itemsOnlyCollectionShapeAllowsEmptyFeasibilityCollections() {
		assertTrue(Part2FeasibilityTests.hasItemsOnlyCollectionShape(Map.of("items", List.of())));
		assertFalse(Part2FeasibilityTests.hasItemsOnlyCollectionShape(Map.of("links", List.of())));
	}

}
