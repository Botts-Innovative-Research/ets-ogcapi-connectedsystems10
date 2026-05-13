package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.advancedfiltering;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

/**
 * Unit checks for the Sprint 25 Part 2 Advanced Filtering helper logic.
 */
public class VerifyPart2AdvancedFilteringTests {

	@org.junit.Test
	public void constantsUseOfficialAdvancedFilteringIdentifiers() {
		String joined = String.join(" ", Part2AdvancedFilteringTests.CONF_ADVANCED_FILTERING,
				Part2AdvancedFilteringTests.REQ_ADVANCED_FILTERING,
				Part2AdvancedFilteringTests.REQ_DATASTREAM_PHENOMENON_TIME,
				Part2AdvancedFilteringTests.REQ_SYSTEM_EVENT_TYPE);

		assertTrue(joined.contains("/conf/advanced-filtering"));
		assertTrue(joined.contains("/req/advanced-filtering"));
		assertFalse(joined.contains("/conf/system-history"));
		assertFalse(joined.contains("/req/system-history"));
		assertFalse(joined.contains("dynamic"));
	}

	@org.junit.Test
	public void systemEventsCanonicalPathUsesCamelCasePathNotAnnexLowercaseTypo() {
		String path = Part2AdvancedFilteringTests.systemEventsCanonicalPath();

		assertTrue(path.equals("systemEvents"));
		assertFalse("Lower-case /systemevents is not used as SystemEvent filter PASS evidence.",
				path.equals("systemevents"));
	}

	@org.junit.Test
	public void timeIntersectionHandlesInstantsAndPeriods() {
		assertTrue(Part2AdvancedFilteringTests.timeIntersects("2026-05-13T10:15:30Z", "2026-05-13T10:15:30Z"));
		assertTrue(Part2AdvancedFilteringTests.timeIntersects("2026-05-13T10:00:00Z/2026-05-13T11:00:00Z",
				"2026-05-13T10:15:30Z"));
		assertTrue(Part2AdvancedFilteringTests.timeIntersects("2026-05-13T10:15:30Z",
				"2026-05-13T10:00:00Z/2026-05-13T11:00:00Z"));
		assertFalse(Part2AdvancedFilteringTests.timeIntersects("2026-05-13T08:00:00Z/2026-05-13T09:00:00Z",
				"2026-05-13T10:00:00Z/2026-05-13T11:00:00Z"));
		assertFalse(Part2AdvancedFilteringTests.timeIntersects("not-a-time", "2026-05-13T10:00:00Z"));
	}

	@org.junit.Test
	public void timeIntersectionRejectsMalformedSubstringEvidence() {
		assertFalse("Malformed strings that merely contain a requested instant are not temporal predicate evidence.",
				Part2AdvancedFilteringTests.timeIntersects("prefix-2026-05-13T10:15:30Z-suffix",
						"2026-05-13T10:15:30Z"));
	}

	@org.junit.Test
	public void observationPhenomenonTimeDoesNotFallbackToResultTime() {
		Map<String, Object> observation = Map.of("resultTime", "2026-05-13T10:15:30Z");

		assertFalse(
				"SCENARIO-ETS-PART2-006-OBSERVATION-FILTERS-READONLY-001: resultTime-only evidence must not satisfy obs-by-phenomenontime.",
				"2026-05-13T10:15:30Z".equals(Part2AdvancedFilteringTests.observationPhenomenonTime(observation)));
	}

	@org.junit.Test
	public void propertyDefinitionRequiresMatchingDefinitionEvidence() {
		Map<String, Object> datastream = Map.of("observedProperties",
				List.of(Map.of("definition", "http://example.test/property/temp", "label", "Temperature")));

		assertTrue(Part2AdvancedFilteringTests.hasPropertyDefinition(datastream, "observedProperties",
				"http://example.test/property/temp"));
		assertFalse("A label-only or unrelated property must not satisfy observedProperty predicate evidence.",
				Part2AdvancedFilteringTests.hasPropertyDefinition(datastream, "observedProperties",
						"http://example.test/property/humidity"));
	}

	@org.junit.Test
	public void commandAndEventPredicatesUseResourceSpecificMembers() {
		assertTrue("COMPLETED".equals(Part2AdvancedFilteringTests.commandStatus(Map.of("currentStatus", "COMPLETED"))));
		assertTrue("COMPLETED".equals(
				Part2AdvancedFilteringTests.commandStatus(Map.of("currentStatus", Map.of("statusCode", "COMPLETED")))));
		assertTrue("sender-1".equals(Part2AdvancedFilteringTests.sender(Map.of("sender", "sender-1"))));
		assertTrue(
				"Calibration".equals(Part2AdvancedFilteringTests.systemEventType(Map.of("eventType", "Calibration"))));
		assertTrue("Calibration".equals(Part2AdvancedFilteringTests.systemEventType(Map.of("type", "Calibration"))));
		assertFalse("A generic JSON object with only id must not satisfy command status evidence.",
				"COMPLETED".equals(Part2AdvancedFilteringTests.commandStatus(Map.of("id", "cmd-1"))));
	}

	@org.junit.Test
	public void itemsOnlyCollectionShapeAllowsEmptyCollectionsButNotPredicatePass() {
		assertTrue(Part2AdvancedFilteringTests.hasItemsOnlyCollectionShape(Map.of("items", List.of())));
		assertFalse(Part2AdvancedFilteringTests.hasItemsOnlyCollectionShape(Map.of("links", List.of())));
	}

}
