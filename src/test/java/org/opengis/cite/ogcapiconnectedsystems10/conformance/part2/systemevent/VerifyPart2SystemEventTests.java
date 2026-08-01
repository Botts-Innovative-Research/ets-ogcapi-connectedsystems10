package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.systemevent;

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
 * Unit checks for the Sprint 63 Part 2 System Events exact released ATS closure.
 */
public class VerifyPart2SystemEventTests {

	private static final Set<String> RELEASED_METHODS = Set.of("systemEventCanonicalUrlFromControlStreamCollections",
			"systemEventResourcesEndpointReadable", "systemEventsCanonicalEndpointReadable",
			"systemEventsReferenceFromSystemsUsesReleasedPath", "systemEventCollectionsValidateSystemEventSchema");

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
	public void deployedClassContainsExactlyFiveReleasedAtsMethods() {
		Set<String> actual = new LinkedHashSet<>();
		for (Method method : Part2SystemEventTests.class.getDeclaredMethods()) {
			if (method.getAnnotation(Test.class) != null) {
				actual.add(method.getName());
			}
		}

		assertEquals("REQ-ETS-PART2-005; SCENARIO-ETS-PART2-005-RELEASED-PROCEDURES-001", RELEASED_METHODS, actual);
	}

	@org.junit.Test
	public void releasedMethodDescriptionsTraceExactTargets() {
		for (Method method : Part2SystemEventTests.class.getDeclaredMethods()) {
			Test test = method.getAnnotation(Test.class);
			if (test == null) {
				continue;
			}
			String description = test.description();
			assertTrue(method.getName() + " missing REQ-ETS-PART2-005 trace",
					description.contains("REQ-ETS-PART2-005"));
			assertTrue(method.getName() + " missing /req/system-event URI", description.contains("/req/system-event/"));
			assertTrue(method.getName() + " missing scenario trace", description.contains("SCENARIO-ETS-PART2-005-"));
			assertTrue(method.getName() + " should be alwaysRun so declaration/prerequisite skips stay visible",
					test.alwaysRun());
			assertTrue(method.getName() + " should carry part2systemevent group",
					Arrays.asList(test.groups()).contains(Part2SystemEventTests.GROUP));
		}
	}

	@org.junit.Test
	public void releasedRefFromSystemUsesAnnexA43SystemEventsPath() {
		String path = Part2SystemEventTests.releasedSystemScopedSystemEventsPath("sys-1");

		assertEquals("systems/sys-1/systemEvents", path);
		assertFalse("Sprint 63 implements released Annex A.5 literally, not the older Sprint 24 /events path.",
				path.endsWith("/events"));
	}

	@org.junit.Test
	public void canonicalUrlReleasedProcedureSelectsControlStreamCollections() {
		assertTrue(Part2SystemEventTests.isCollectionWithItemType(Map.of("id", "c1", "itemType", "ControlStream"),
				"ControlStream"));
		assertFalse("A.40 released text selects itemType=ControlStream, not itemType=SystemEvent.",
				Part2SystemEventTests.isCollectionWithItemType(Map.of("id", "c1", "itemType", "SystemEvent"),
						"ControlStream"));
	}

	@org.junit.Test
	public void systemEventCollectionRequiresExactItemType() {
		assertTrue(Part2SystemEventTests.isCollectionWithItemType(Map.of("id", "c1", "itemType", "SystemEvent"),
				"SystemEvent"));
		assertFalse(Part2SystemEventTests.isCollectionWithItemType(Map.of("id", "c1", "itemType", "SystemHistory"),
				"SystemEvent"));
		assertFalse(Part2SystemEventTests.isCollectionWithItemType(Map.of("id", "c1"), "SystemEvent"));
	}

	@org.junit.Test
	public void itemsOnlyCollectionShapeAllowsEmptySystemEventCollections() {
		assertTrue(Part2SystemEventTests.hasItemsOnlyCollectionShape(Map.of("items", List.of())));
		assertFalse(Part2SystemEventTests.hasItemsOnlyCollectionShape(Map.of("links", List.of())));
	}

	@org.junit.Test
	public void systemEventValidationUsesReleasedJsonSchemas() {
		assertEquals("systemEventCollection.json", Part2SystemEventSupport.SYSTEM_EVENT_COLLECTION_SCHEMA);
		assertEquals("systemEvent.json", Part2SystemEventSupport.SYSTEM_EVENT_SCHEMA);
	}

}
