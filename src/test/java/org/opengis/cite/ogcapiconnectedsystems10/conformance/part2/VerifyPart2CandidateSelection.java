package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Regression coverage for populated Part 2 candidate selection.
 *
 * <p>
 * Traceability: REQ-ETS-PART2-013,
 * SCENARIO-ETS-PART2-013-SIMUAV-PRESEEDED-POPULATED-IUT-001, and
 * SCENARIO-ETS-PART2-013-POPULATED-CANDIDATE-SELECTION-001.
 * </p>
 */
public class VerifyPart2CandidateSelection {

	@Test
	public void objectItemsIgnoresNonObjectCollectionMembers() {
		Map<String, Object> body = Map.of("items",
				List.of(Map.of("id", "ds-empty"), "not-a-resource", Map.of("id", "ds-populated")));

		List<Map<String, Object>> items = Part2CandidateSelection.objectItems(body);

		assertEquals(2, items.size());
		assertEquals("ds-empty", items.get(0).get("id"));
		assertEquals("ds-populated", items.get(1).get("id"));
	}

	@Test
	public void firstParentWithChildSkipsEmptyParents() {
		Map<String, Object> empty = Map.of("id", "cs-empty");
		Map<String, Object> populated = Map.of("id", "cs-populated");
		Map<String, Object> command = Map.of("id", "cmd-1", "controlstream@id", "cs-populated");

		Part2CandidateSelection.ParentChild selected = Part2CandidateSelection
			.firstParentWithChild(List.of(empty, populated), parent -> {
				if ("cs-populated".equals(parent.get("id"))) {
					return command;
				}
				return null;
			});

		assertEquals(populated, selected.parent());
		assertEquals(command, selected.child());
	}

	@Test
	public void firstParentWithChildReturnsNullWhenNoScopedEvidenceExists() {
		Part2CandidateSelection.ParentChild selected = Part2CandidateSelection
			.firstParentWithChild(List.of(Map.of("id", "ds-1"), Map.of("id", "ds-2")), parent -> null);

		assertNull(selected);
	}

}
