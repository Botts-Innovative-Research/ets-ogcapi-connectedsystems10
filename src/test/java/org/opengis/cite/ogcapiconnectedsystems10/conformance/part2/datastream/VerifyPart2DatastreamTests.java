package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.datastream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

/**
 * Unit checks for the Sprint 21 Part 2 Datastream helper logic.
 */
public class VerifyPart2DatastreamTests {

	@org.junit.Test
	public void datastreamShapeRequiresResourceSpecificMembers() {
		assertTrue(Part2DatastreamTests
			.hasDatastreamShape(Map.of("id", "ds-1", "system@id", "sys-1", "outputName", "out", "observedProperties",
					List.of(Map.of("label", "p")), "formats", List.of("application/om+json"), "resultType", "record")));
		assertFalse("A generic JSON object with only id/items must not masquerade as a Datastream.",
				Part2DatastreamTests.hasDatastreamShape(Map.of("id", "ds-1", "items", List.of())));
	}

	@org.junit.Test
	public void observationReferenceRequiresActualDatastreamEvidence() {
		assertTrue(Part2DatastreamTests.observationReferencesDatastream(Map.of("id", "obs-1", "datastream@id", "ds-1"),
				"ds-1"));
		assertTrue(Part2DatastreamTests.observationReferencesDatastream(
				Map.of("id", "obs-1", "links", List.of(Map.of("href", "https://example.test/api/datastreams/ds-1"))),
				"ds-1"));
		assertFalse("Empty or unrelated observations must not PASS obs-ref-from-datastream.",
				Part2DatastreamTests.observationReferencesDatastream(Map.of("id", "obs-1"), "ds-1"));
	}

	@org.junit.Test
	public void itemsOnlyCollectionShapeAllowsEmptyNestedObservations() {
		assertTrue(Part2DatastreamTests.hasItemsOnlyCollectionShape(Map.of("items", List.of())));
		assertFalse(Part2DatastreamTests.hasItemsOnlyCollectionShape(Map.of("links", List.of())));
	}

	@org.junit.Test
	public void observationShapeRequiresObservationSpecificMembers() {
		assertTrue(Part2DatastreamTests.hasObservationShape(Map.of("id", "obs-1", "datastream@id", "ds-1")));
		assertTrue(Part2DatastreamTests.hasObservationShape(Map.of("id", "obs-1", "result", 12.3)));
		assertFalse("A generic JSON object with only an id must not masquerade as an Observation.",
				Part2DatastreamTests.hasObservationShape(Map.of("id", "obs-1")));
	}

	@org.junit.Test
	public void constantsUseOfficialDatastreamIdentifiers() {
		String joined = String.join(" ", Part2DatastreamTests.CONF_DATASTREAM, Part2DatastreamTests.REQ_DATASTREAM,
				Part2DatastreamTests.REQ_OBS_REF_FROM_DATASTREAM);

		assertTrue(joined.contains("/conf/datastream"));
		assertTrue(joined.contains("/req/datastream"));
		assertFalse(joined.contains("dynamic"));
	}

}
