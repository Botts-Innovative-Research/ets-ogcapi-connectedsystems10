package org.opengis.cite.ogcapiconnectedsystems10.conformance.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * Focused regressions for Update patch-format negotiation.
 */
public class VerifyUpdateSupport {

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-PATCH-NEGOTIATION-001.
	 */
	@Test
	public void acceptPatchSelectsOnlyImplementedPatchDocuments() {
		List<String> selected = UpdateSupport.supportedPatchMediaTypes(
				"application/json, application/merge-patch+json; charset=utf-8, application/json-patch+json", Set.of());

		assertEquals(List.of("application/merge-patch+json", "application/json-patch+json"), selected);
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-PATCH-NEGOTIATION-001.
	 */
	@Test
	public void openApiMetadataComplementsAcceptPatchWithoutDuplicates() {
		List<String> selected = UpdateSupport.supportedPatchMediaTypes("application/merge-patch+json",
				Set.of("application/json-patch+json", "application/merge-patch+json"));

		assertEquals(List.of("application/merge-patch+json", "application/json-patch+json"), selected);
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-PATCH-NEGOTIATION-001.
	 */
	@Test
	public void ordinaryResourceMediaTypesNeverBecomePatchDocuments() {
		List<String> selected = UpdateSupport.supportedPatchMediaTypes(
				"application/json, application/geo+json, application/sml+json",
				Set.of("application/json", "application/geo+json", "application/sml+json"));

		assertTrue(selected.isEmpty());
		assertFalse(selected.contains("application/json"));
	}

}
