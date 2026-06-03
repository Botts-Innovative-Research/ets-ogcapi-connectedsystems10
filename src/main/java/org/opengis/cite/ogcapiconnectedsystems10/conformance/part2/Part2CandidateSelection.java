package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Shared candidate-selection helpers for populated Part 2 resources.
 */
public final class Part2CandidateSelection {

	public static final int CANDIDATE_PAGE_LIMIT = 25;

	private Part2CandidateSelection() {
	}

	/**
	 * Returns JSON object members from a CS API collection body.
	 * @param body parsed JSON collection body.
	 * @return object-valued entries from {@code items[]}; empty when absent.
	 */
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> objectItems(Map<String, Object> body) {
		if (body == null || !(body.get("items") instanceof List)) {
			return List.of();
		}
		List<Map<String, Object>> objects = new ArrayList<>();
		for (Object item : (List<?>) body.get("items")) {
			if (item instanceof Map) {
				objects.add((Map<String, Object>) item);
			}
		}
		return objects;
	}

	/**
	 * Finds the first parent whose scoped child lookup returns evidence.
	 * <p>
	 * REQ-ETS-PART2-013 / SCENARIO-ETS-PART2-013-POPULATED-CANDIDATE-SELECTION-001: a
	 * populated fixture should not be missed merely because the first DataStream or
	 * ControlStream on the page has an empty child collection.
	 * </p>
	 * @param parents parent resource candidates.
	 * @param childLookup returns the first associated child for a parent, or
	 * {@code null}.
	 * @return selected parent/child pair, or {@code null} when no parent has child
	 * evidence.
	 */
	public static ParentChild firstParentWithChild(List<Map<String, Object>> parents,
			Function<Map<String, Object>, Map<String, Object>> childLookup) {
		for (Map<String, Object> parent : parents) {
			Map<String, Object> child = childLookup.apply(parent);
			if (child != null) {
				return new ParentChild(parent, child);
			}
		}
		return null;
	}

	public record ParentChild(Map<String, Object> parent, Map<String, Object> child) {
	}

}
