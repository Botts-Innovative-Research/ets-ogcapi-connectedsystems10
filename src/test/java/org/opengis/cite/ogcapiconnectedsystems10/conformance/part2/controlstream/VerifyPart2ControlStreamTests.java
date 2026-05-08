package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.controlstream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

/**
 * Unit checks for the Sprint 22 Part 2 ControlStream helper logic.
 */
public class VerifyPart2ControlStreamTests {

	@org.junit.Test
	public void controlStreamShapeRequiresResourceSpecificMembers() {
		assertTrue(Part2ControlStreamTests.hasControlStreamShape(
				Map.of("id", "cs-1", "system@id", "sys-1", "inputName", "cmd", "controlledProperties",
						List.of(Map.of("label", "speed")), "formats", List.of("application/swe+json"))));
		assertFalse("A generic JSON object with only id/items must not masquerade as a ControlStream.",
				Part2ControlStreamTests.hasControlStreamShape(Map.of("id", "cs-1", "items", List.of())));
	}

	@org.junit.Test
	public void commandReferenceRequiresActualControlStreamEvidence() {
		assertTrue(Part2ControlStreamTests
			.commandReferencesControlStream(Map.of("id", "cmd-1", "controlstream@id", "cs-1"), "cs-1"));
		assertTrue(Part2ControlStreamTests.commandReferencesControlStream(
				Map.of("id", "cmd-1", "links", List.of(Map.of("href", "https://example.test/api/controlstreams/cs-1"))),
				"cs-1"));
		assertTrue(Part2ControlStreamTests.commandReferencesControlStream(
				Map.of("id", "cmd-1", "links", List.of(Map.of("href", "https://example.test/api/controls/cs-1"))),
				"cs-1"));
		assertFalse("Empty or unrelated commands must not PASS cmd-ref-from-controlstream.",
				Part2ControlStreamTests.commandReferencesControlStream(Map.of("id", "cmd-1"), "cs-1"));
	}

	@org.junit.Test
	public void itemsOnlyCollectionShapeAllowsEmptyNestedCommands() {
		assertTrue(Part2ControlStreamTests.hasItemsOnlyCollectionShape(Map.of("items", List.of())));
		assertFalse(Part2ControlStreamTests.hasItemsOnlyCollectionShape(Map.of("links", List.of())));
	}

	@org.junit.Test
	public void constantsUseOfficialControlStreamIdentifiers() {
		String joined = String.join(" ", Part2ControlStreamTests.CONF_CONTROLSTREAM,
				Part2ControlStreamTests.REQ_CONTROLSTREAM, Part2ControlStreamTests.REQ_CMD_REF_FROM_CONTROLSTREAM);

		assertTrue(joined.contains("/conf/controlstream"));
		assertTrue(joined.contains("/req/controlstream"));
		assertFalse(joined.contains("dynamic"));
	}

}
