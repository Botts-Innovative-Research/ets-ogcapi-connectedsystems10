package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Regression coverage for S-ETS-27-01 Part 2 Update safety gating.
 *
 * <p>
 * Traceability: REQ-ETS-PART2-008,
 * SCENARIO-ETS-PART2-008-UPDATE-CONFORMANCE-DECLARED-001,
 * SCENARIO-ETS-PART2-008-CRD-FEATURES4-PREREQUISITES-001,
 * SCENARIO-ETS-PART2-008-RESOURCE-CONDITION-GATES-001,
 * SCENARIO-ETS-PART2-008-PATCH-MUTATION-SAFETY-GATE-001,
 * SCENARIO-ETS-PART2-008-OPTIONS-PATCH-READINESS-001,
 * SCENARIO-ETS-PART2-008-UNAVAILABLE-ENDPOINT-HONESTY-001,
 * SCENARIO-ETS-PART2-008-SCHEMA-REJECTION-HONESTY-001, and
 * SCENARIO-ETS-PART2-008-SMOKE-NO-PUBLIC-PATCH-001.
 * </p>
 */
public class VerifyPart2UpdateTests {

	@Test
	public void officialPart2UpdateIdentifiersAreExposed() {
		String joined = String.join(" ", Part2UpdateTests.CONF_UPDATE, Part2UpdateTests.REQ_UPDATE,
				Part2UpdateTests.CONF_CREATE_REPLACE_DELETE, Part2UpdateTests.CONF_FEATURES4_UPDATE,
				Part2UpdateTests.REQ_DATASTREAM, Part2UpdateTests.REQ_COMMAND, Part2UpdateTests.REQ_FEASIBILITY,
				Part2UpdateTests.REQ_SYSTEM_EVENT);

		assertTrue(joined.contains("ogcapi-connectedsystems-2/1.0/conf/update"));
		assertTrue(joined.contains("ogcapi-connectedsystems-2/1.0/req/update"));
		assertTrue(joined.contains("ogcapi-connectedsystems-2/1.0/conf/create-replace-delete"));
		assertTrue(joined.contains("ogcapi-features-4/1.0/conf/update"));
		assertFalse(joined.contains("ogcapi-connectedsystems-1/1.0/conf/update"));
	}

	@Test
	public void conditionGateMatrixReportsMissingClasses() {
		Map<String, Object> updateOnly = Map.of("conformsTo", List.of(Part2UpdateTests.CONF_UPDATE));

		List<String> missing = Part2UpdateTests.missingConditionClasses(updateOnly);

		assertEquals(4, missing.size());
		assertTrue(missing.contains(Part2UpdateTests.missingConditionMessage(Part2UpdateTests.CONF_DATASTREAM,
				"R79-R82 DataStream/Observation Update")));
		assertTrue(missing.contains(Part2UpdateTests.missingConditionMessage(Part2UpdateTests.CONF_CONTROLSTREAM,
				"R83-R88 ControlStream/Command Update")));
		assertTrue(missing.contains(Part2UpdateTests.missingConditionMessage(Part2UpdateTests.CONF_FEASIBILITY,
				"R89-R91 Feasibility Update")));
		assertTrue(missing.contains(Part2UpdateTests.missingConditionMessage(Part2UpdateTests.CONF_SYSTEM_EVENT,
				"R92 SystemEvent Update")));

		Map<String, Object> complete = Map.of("conformsTo",
				List.of(Part2UpdateTests.CONF_UPDATE, Part2UpdateTests.CONF_DATASTREAM,
						Part2UpdateTests.CONF_CONTROLSTREAM, Part2UpdateTests.CONF_FEASIBILITY,
						Part2UpdateTests.CONF_SYSTEM_EVENT));
		assertTrue(Part2UpdateTests.missingConditionClasses(complete).isEmpty());
	}

	@Test
	public void exactConformanceDeclarationIsRequired() {
		Map<String, Object> body = Map.of("conformsTo", List.of(Part2UpdateTests.CONF_UPDATE));

		assertTrue(Part2UpdateTests.declaresConformance(body, Part2UpdateTests.CONF_UPDATE));
		assertFalse(Part2UpdateTests.declaresConformance(body,
				"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/update"));
		assertFalse(Part2UpdateTests.declaresConformance(Map.of(), Part2UpdateTests.CONF_UPDATE));
	}

	@Test
	public void publicGeoRobotixIutIsHardDeniedEvenWhenMutationOptInIsSet() {
		URI georobotix = URI.create("https://api.georobotix.io/ogc/t18/api/");

		assertTrue(Part2UpdateTests.isPublicGeoRobotixIut(georobotix));
		String reason = Part2UpdateTests.mutationGateSkipReason(georobotix, "true", "dedicated-mutable-iut",
				Part2UpdateTests.REQ_UPDATE);

		assertTrue(reason.contains("public GeoRobotix"));
		assertTrue(reason.contains("No PATCH/POST/PUT/DELETE request was issued"));
	}

	@Test
	public void mutationGateRequiresBothExplicitParametersForNonPublicIut() {
		URI local = URI.create("http://field-hub-osh-1:8081/sensorhub/api");

		assertTrue(
				Part2UpdateTests.mutationGateSkipReason(local, "", "dedicated-mutable-iut", Part2UpdateTests.REQ_UPDATE)
					.contains("mutation-tests-enabled"));
		assertTrue(Part2UpdateTests.mutationGateSkipReason(local, "true", "", Part2UpdateTests.REQ_UPDATE)
			.contains("mutation-iut-policy"));
		assertNull(Part2UpdateTests.mutationGateSkipReason(local, "true", "dedicated-mutable-iut",
				Part2UpdateTests.REQ_UPDATE));
	}

	@Test
	public void allowHeaderParsingIsCaseInsensitiveAndCommaDelimited() {
		assertTrue(Part2UpdateTests.allowHeaderContains("GET, HEAD, patch, OPTIONS", "PATCH"));
		assertTrue(Part2UpdateTests.allowHeaderContains("GET,PATCH,DELETE", "patch"));
		assertFalse(Part2UpdateTests.allowHeaderContains("GET, HEAD, OPTIONS", "PATCH"));
		assertFalse(Part2UpdateTests.allowHeaderContains(null, "PATCH"));
	}

	@Test
	public void collectionShapeRequiresItemsArray() {
		assertTrue(Part2UpdateTests.hasItemsOnlyCollectionShape(Map.of("items", List.of())));
		assertFalse(Part2UpdateTests.hasItemsOnlyCollectionShape(Map.of("links", List.of())));
		assertFalse(Part2UpdateTests.hasItemsOnlyCollectionShape(null));
	}

	@Test
	public void missingConditionMessageNamesRequirementGroupAndConformance() {
		String message = Part2UpdateTests.missingConditionMessage(Part2UpdateTests.CONF_SYSTEM_EVENT,
				"R92 SystemEvent Update");

		assertEquals("R92 SystemEvent Update requires " + Part2UpdateTests.CONF_SYSTEM_EVENT, message);
	}

	@Test
	public void groupNameIsStableForTestNgWiring() {
		assertEquals("part2update", Part2UpdateTests.GROUP);
	}

}
