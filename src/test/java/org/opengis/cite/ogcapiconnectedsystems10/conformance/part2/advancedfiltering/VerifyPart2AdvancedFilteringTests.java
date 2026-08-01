package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.advancedfiltering;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.testng.annotations.Test;

/**
 * Unit checks for the Sprint 64 Part 2 Advanced Filtering released ATS closure.
 */
public class VerifyPart2AdvancedFilteringTests {

	private static final String REQ_BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/advanced-filtering/";

	private static final Set<String> RELEASED_TARGETS = Set.of(REQ_BASE + "datastream-by-phenomenontime",
			REQ_BASE + "datastream-by-resulttime", REQ_BASE + "datastream-by-obsprop", REQ_BASE + "datastream-by-foi",
			REQ_BASE + "obs-by-phenomenontime", REQ_BASE + "obs-by-resulttime", REQ_BASE + "obs-by-foi",
			REQ_BASE + "controlstream-by-issuetime", REQ_BASE + "controlstream-by-exectime",
			REQ_BASE + "controlstream-by-controlprop", REQ_BASE + "controlstream-by-foi", REQ_BASE + "cmd-by-issuetime",
			REQ_BASE + "cmd-by-exectime", REQ_BASE + "cmd-by-status", REQ_BASE + "cmd-by-sender",
			REQ_BASE + "cmd-by-foi", REQ_BASE + "status-by-statuscode", REQ_BASE + "event-by-type");

	@org.junit.Test
	public void releasedSuiteExposesExactlyOneTestMethodPerAnnexA6Target() {
		List<Method> methods = releasedMethods();
		assertEquals(
				"SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001: Part 2 Advanced Filtering must expose exactly the eighteen released Annex A.6 procedures.",
				RELEASED_TARGETS.size(), methods.size());

		Set<String> covered = new HashSet<>();
		for (Method method : methods) {
			Test ann = method.getAnnotation(Test.class);
			List<String> matched = RELEASED_TARGETS.stream()
				.filter(target -> containsCanonicalTarget(ann.description(), target))
				.toList();
			assertEquals("Each Part 2 Advanced Filtering method must cite exactly one released target: "
					+ method.getName() + " -> " + matched, 1, matched.size());
			covered.add(matched.get(0));
		}
		assertEquals("Part 2 Advanced Filtering released targets are not covered exactly once.", RELEASED_TARGETS,
				covered);
	}

	@org.junit.Test
	public void releasedSuiteContainsNoStandaloneDeclarationOrPrerequisiteProcedures() {
		Set<String> methodNames = releasedMethods().stream().map(Method::getName).collect(Collectors.toSet());

		assertFalse("Declaration is a runtime gate, not a released Annex A.6 procedure.",
				methodNames.contains("part2AdvancedFilteringConformanceDeclared"));
		assertFalse("Prerequisite visibility is a setup gate, not a released Annex A.6 procedure.",
				methodNames.contains("advancedFilteringPrerequisitesVisibleForFullClosure"));
		assertTrue("Released FOI procedure for DataStream is missing.", containsTarget(REQ_BASE + "datastream-by-foi"));
		assertTrue("Released FOI procedure for Observation is missing.", containsTarget(REQ_BASE + "obs-by-foi"));
		assertTrue("Released FOI procedure for ControlStream is missing.",
				containsTarget(REQ_BASE + "controlstream-by-foi"));
		assertTrue("Released FOI procedure for Command is missing.", containsTarget(REQ_BASE + "cmd-by-foi"));
		assertTrue("Released CommandStatus procedure is missing.", containsTarget(REQ_BASE + "status-by-statuscode"));
	}

	@org.junit.Test
	public void everyReleasedMethodTracesSprint64ScenarioAndRequirement() {
		for (Method method : releasedMethods()) {
			Test ann = method.getAnnotation(Test.class);
			String description = ann.description();
			assertTrue(method.getName() + " missing REQ-ETS-PART2-006 trace",
					description.contains("REQ-ETS-PART2-006"));
			assertTrue(method.getName() + " missing Sprint 64 released-procedure scenario trace",
					description.contains("SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001"));
		}
	}

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
	public void timeIntersectionHandlesInstantsAndPeriods() {
		assertTrue(Part2AdvancedFilteringSupport.timeIntersects("2026-05-13T10:15:30Z", "2026-05-13T10:15:30Z"));
		assertTrue(Part2AdvancedFilteringSupport.timeIntersects("2026-05-13T10:00:00Z/2026-05-13T11:00:00Z",
				"2026-05-13T10:15:30Z"));
		assertTrue(Part2AdvancedFilteringSupport.timeIntersects("2026-05-13T10:15:30Z",
				"2026-05-13T10:00:00Z/2026-05-13T11:00:00Z"));
		assertFalse(Part2AdvancedFilteringSupport.timeIntersects("2026-05-13T08:00:00Z/2026-05-13T09:00:00Z",
				"2026-05-13T10:00:00Z/2026-05-13T11:00:00Z"));
		assertFalse(Part2AdvancedFilteringSupport.timeIntersects("not-a-time", "2026-05-13T10:00:00Z"));
	}

	@org.junit.Test
	public void timeIntersectionRejectsMalformedSubstringEvidence() {
		assertFalse("Malformed strings that merely contain a requested instant are not temporal predicate evidence.",
				Part2AdvancedFilteringSupport.timeIntersects("prefix-2026-05-13T10:15:30Z-suffix",
						"2026-05-13T10:15:30Z"));
	}

	@org.junit.Test
	public void commandAndEventPredicatesUseResourceSpecificMembers() {
		Map<String, Object> commandWithObjectStatus = Map.of("currentStatus", Map.of("statusCode", "COMPLETED"));
		assertTrue(
				"COMPLETED".equals(Part2AdvancedFilteringSupport.commandStatus(Map.of("currentStatus", "COMPLETED"))));
		assertTrue("COMPLETED".equals(Part2AdvancedFilteringSupport.commandStatus(commandWithObjectStatus)));
		assertTrue("Calibration"
			.equals(Part2AdvancedFilteringSupport.systemEventType(Map.of("eventType", "Calibration"))));
		assertTrue("Calibration"
			.equals(Part2AdvancedFilteringSupport.systemEventType(Map.of("definition", "Calibration"))));
		assertTrue("urn:ogc:event:calibration".equals(Part2AdvancedFilteringSupport
			.systemEventType(Map.of("type", "SystemEvent", "definition", "urn:ogc:event:calibration"))));
		assertFalse("Generic SystemEvent resource type is not event-type filter evidence.",
				"SystemEvent".equals(Part2AdvancedFilteringSupport.systemEventType(Map.of("type", "SystemEvent"))));
		assertFalse("A generic JSON object with only id must not satisfy command status evidence.",
				"COMPLETED".equals(Part2AdvancedFilteringSupport.commandStatus(Map.of("id", "cmd-1"))));
	}

	private static List<Method> releasedMethods() {
		return Arrays.stream(Part2AdvancedFilteringTests.class.getDeclaredMethods())
			.filter(method -> method.getAnnotation(Test.class) != null)
			.toList();
	}

	private static boolean containsTarget(String target) {
		return releasedMethods().stream()
			.map(method -> method.getAnnotation(Test.class).description())
			.anyMatch(description -> containsCanonicalTarget(description, target));
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
