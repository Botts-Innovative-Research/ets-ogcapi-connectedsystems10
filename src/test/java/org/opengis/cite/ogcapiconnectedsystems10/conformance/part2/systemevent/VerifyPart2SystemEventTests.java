package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.systemevent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

/**
 * Unit checks for the Sprint 24 Part 2 System Events helper logic.
 */
public class VerifyPart2SystemEventTests {

	@org.junit.Test
	public void constantsUseOfficialSystemEventIdentifiers() {
		String joined = String.join(" ", Part2SystemEventTests.CONF_SYSTEM_EVENT,
				Part2SystemEventTests.REQ_SYSTEM_EVENT, Part2SystemEventTests.REQ_REF_FROM_SYSTEM);

		assertTrue(joined.contains("/conf/system-event"));
		assertTrue(joined.contains("/req/system-event"));
		assertFalse(joined.contains("/conf/systemevents"));
		assertFalse(joined.contains("/req/systemevents"));
		assertFalse(joined.contains("dynamic"));
	}

	@org.junit.Test
	public void normativeSystemEventsPathUsesEventsPathFromRequirement43() {
		String path = Part2SystemEventTests.normativeSystemEventsPath("sys-1");

		assertTrue(path.equals("systems/sys-1/events"));
		assertFalse("Annex A.43 /systems/{id}/systemEvents is diagnostic only, not Requirement 43 PASS evidence.",
				path.endsWith("/systemEvents"));
	}

	@org.junit.Test
	public void systemEventResourceShapeRequiresResourceSpecificEvidence() {
		assertTrue(Part2SystemEventTests.hasSystemEventResourceShape(
				Map.of("id", "event-1", "time", "2026-05-08T21:00:00Z", "eventType", "status")));
		assertTrue(Part2SystemEventTests
			.hasSystemEventResourceShape(Map.of("id", "event-1", "system@id", "sys-1", "links", List.of())));
		assertFalse("A generic JSON object with only id/items must not masquerade as a SystemEvent resource.",
				Part2SystemEventTests.hasSystemEventResourceShape(Map.of("id", "event-1", "items", List.of())));
	}

	@org.junit.Test
	public void systemEventCollectionRequiresExactItemType() {
		assertTrue(Part2SystemEventTests.isSystemEventCollection(Map.of("id", "c1", "itemType", "SystemEvent")));
		assertFalse(Part2SystemEventTests.isSystemEventCollection(Map.of("id", "c1", "itemType", "SystemHistory")));
		assertFalse(Part2SystemEventTests.isSystemEventCollection(Map.of("id", "c1")));
	}

	@org.junit.Test
	public void itemsOnlyCollectionShapeAllowsEmptySystemEventCollections() {
		assertTrue(Part2SystemEventTests.hasItemsOnlyCollectionShape(Map.of("items", List.of())));
		assertFalse(Part2SystemEventTests.hasItemsOnlyCollectionShape(Map.of("links", List.of())));
	}

}
