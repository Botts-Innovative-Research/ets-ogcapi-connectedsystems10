package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.createreplacedelete;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Regression coverage for S-ETS-26-01 Part 2 Create/Replace/Delete safety gating.
 *
 * <p>
 * Traceability: REQ-ETS-PART2-007, SCENARIO-ETS-PART2-007-CRD-CONFORMANCE-DECLARED-001,
 * SCENARIO-ETS-PART2-007-FEATURES4-PREREQUISITE-001,
 * SCENARIO-ETS-PART2-007-MUTATION-SAFETY-GATE-001,
 * SCENARIO-ETS-PART2-007-OPTIONS-READINESS-READONLY-001, and
 * SCENARIO-ETS-PART2-007-UNAVAILABLE-ENDPOINT-HONESTY-001.
 * </p>
 */
public class VerifyPart2CreateReplaceDeleteTests {

	@Test
	public void officialPart2AndFeatures4IdentifiersAreExposed() {
		String joined = String.join(" ", Part2CreateReplaceDeleteTests.CONF_CREATE_REPLACE_DELETE,
				Part2CreateReplaceDeleteTests.REQ_CREATE_REPLACE_DELETE,
				Part2CreateReplaceDeleteTests.CONF_FEATURES4_CREATE_REPLACE_DELETE,
				Part2CreateReplaceDeleteTests.REQ_DATASTREAM, Part2CreateReplaceDeleteTests.REQ_COMMAND,
				Part2CreateReplaceDeleteTests.REQ_FEASIBILITY, Part2CreateReplaceDeleteTests.REQ_SYSTEM_EVENT);

		assertTrue(joined.contains("ogcapi-connectedsystems-2/1.0/conf/create-replace-delete"));
		assertTrue(joined.contains("ogcapi-connectedsystems-2/1.0/req/create-replace-delete"));
		assertTrue(joined.contains("ogcapi-features-4/1.0/conf/create-replace-delete"));
		assertFalse(joined.contains("ogcapi-connectedsystems-1/1.0/conf/create-replace-delete"));
	}

	@Test
	public void readinessPathsUseNormativeScopedCreateEndpoints() {
		assertEquals("systems/sys-1/datastreams",
				Part2CreateReplaceDeleteTests.systemScopedCollectionPath("sys-1", "datastreams"));
		assertEquals("systems/sys-1/datastreams/ds-1",
				Part2CreateReplaceDeleteTests.systemScopedResourcePath("sys-1", "datastreams", "ds-1"));
		assertEquals("datastreams/ds-1/observations",
				Part2CreateReplaceDeleteTests.datastreamScopedObservationsPath("ds-1"));
		assertEquals("systems/sys-1/controlstreams",
				Part2CreateReplaceDeleteTests.systemScopedCollectionPath("sys-1", "controlstreams"));
		assertFalse("Part 2 CRD CREATE readiness must not use the global /datastreams collection",
				"datastreams".equals(Part2CreateReplaceDeleteTests.systemScopedCollectionPath("sys-1", "datastreams")));
		assertFalse("Part 2 CRD CREATE readiness must not use the global /observations collection",
				"observations".equals(Part2CreateReplaceDeleteTests.datastreamScopedObservationsPath("ds-1")));
		assertFalse("Part 2 CRD CREATE readiness must not use the global /controlstreams collection", "controlstreams"
			.equals(Part2CreateReplaceDeleteTests.systemScopedCollectionPath("sys-1", "controlstreams")));
	}

	@Test
	public void exactConformanceDeclarationIsRequired() {
		Map<String, Object> body = Map.of("conformsTo",
				List.of(Part2CreateReplaceDeleteTests.CONF_CREATE_REPLACE_DELETE));

		assertTrue(Part2CreateReplaceDeleteTests.declaresConformance(body,
				Part2CreateReplaceDeleteTests.CONF_CREATE_REPLACE_DELETE));
		assertFalse(Part2CreateReplaceDeleteTests.declaresConformance(body,
				"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/create-replace-delete"));
		assertFalse(Part2CreateReplaceDeleteTests.declaresConformance(Map.of(),
				Part2CreateReplaceDeleteTests.CONF_CREATE_REPLACE_DELETE));
	}

	@Test
	public void publicGeoRobotixIutIsHardDeniedEvenWhenMutationOptInIsSet() {
		URI georobotix = URI.create("https://api.georobotix.io/ogc/t18/api/");

		assertTrue(Part2CreateReplaceDeleteTests.isPublicGeoRobotixIut(georobotix));
		String reason = Part2CreateReplaceDeleteTests.mutationGateSkipReason(georobotix, "true",
				"dedicated-mutable-iut", Part2CreateReplaceDeleteTests.REQ_CREATE_REPLACE_DELETE);

		assertTrue(reason.contains("public GeoRobotix"));
		assertTrue(reason.contains("No POST/PUT/DELETE/PATCH request was issued"));
	}

	@Test
	public void mutationGateRequiresBothExplicitParametersForNonPublicIut() {
		URI local = URI.create("http://field-hub-osh-1:8081/sensorhub/api");

		assertTrue(Part2CreateReplaceDeleteTests
			.mutationGateSkipReason(local, "", "dedicated-mutable-iut",
					Part2CreateReplaceDeleteTests.REQ_CREATE_REPLACE_DELETE)
			.contains("mutation-tests-enabled"));
		assertTrue(Part2CreateReplaceDeleteTests
			.mutationGateSkipReason(local, "true", "", Part2CreateReplaceDeleteTests.REQ_CREATE_REPLACE_DELETE)
			.contains("mutation-iut-policy"));
		assertNull(Part2CreateReplaceDeleteTests.mutationGateSkipReason(local, "true", "dedicated-mutable-iut",
				Part2CreateReplaceDeleteTests.REQ_CREATE_REPLACE_DELETE));
	}

	@Test
	public void allowHeaderParsingIsCaseInsensitiveAndCommaDelimited() {
		assertTrue(Part2CreateReplaceDeleteTests.allowHeaderContains("GET, HEAD, post, OPTIONS", "POST"));
		assertTrue(Part2CreateReplaceDeleteTests.allowHeaderContains("GET,PUT,DELETE", "delete"));
		assertFalse(Part2CreateReplaceDeleteTests.allowHeaderContains("GET, HEAD, OPTIONS", "POST"));
		assertFalse(Part2CreateReplaceDeleteTests.allowHeaderContains(null, "POST"));
	}

	@Test
	public void collectionShapeRequiresItemsArray() {
		assertTrue(Part2CreateReplaceDeleteTests.hasItemsOnlyCollectionShape(Map.of("items", List.of())));
		assertFalse(Part2CreateReplaceDeleteTests.hasItemsOnlyCollectionShape(Map.of("links", List.of())));
		assertFalse(Part2CreateReplaceDeleteTests.hasItemsOnlyCollectionShape(null));
	}

	@Test
	public void associatedSystemIdUsesExplicitParentEvidenceOnly() {
		assertEquals("sys-a", Part2CreateReplaceDeleteTests.associatedSystemId(Map.of("system@id", "sys-a")));
		assertEquals("sys-b", Part2CreateReplaceDeleteTests.associatedSystemId(Map.of("systemId", "sys-b")));
		assertEquals("sys-c",
				Part2CreateReplaceDeleteTests.associatedSystemId(Map.of("system", Map.of("id", "sys-c"))));
		assertNull(Part2CreateReplaceDeleteTests.associatedSystemId(Map.of("id", "resource-without-parent")));
		assertNull(Part2CreateReplaceDeleteTests.associatedSystemId(null));
	}

	@Test
	public void groupNameIsStableForTestNgWiring() {
		assertEquals("part2createreplacedelete", Part2CreateReplaceDeleteTests.GROUP);
	}

}
