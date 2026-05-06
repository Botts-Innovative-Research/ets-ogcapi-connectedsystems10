package org.opengis.cite.ogcapiconnectedsystems10.conformance.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;

import java.util.Map;

import org.junit.Test;

/**
 * Regression coverage for S-ETS-14-01 status-only PATCH false-positive prevention.
 *
 * <p>
 * Traceability: REQ-ETS-PART1-011,
 * SCENARIO-ETS-PART1-011-UPDATE-SYSTEM-PATCH-CHANGED-FIELD-001, and
 * SCENARIO-ETS-PART1-011-UPDATE-OPTIONS-PATCH-SKIP-SEMANTICS-001.
 * </p>
 */
public class VerifyUpdateChangedFieldAssertion {

	@Test
	public void extractsNestedSystemName() {
		Map<String, Object> system = Map.of("type", "Feature", "properties", Map.of("name", "patched"));

		assertEquals("patched", UpdateTests.systemName(system));
	}

	@Test
	public void acceptsExpectedPatchedSystemName() {
		Map<String, Object> system = Map.of("type", "Feature", "properties", Map.of("name", "patched"));

		UpdateTests.assertPatchedSystemName(system, "patched");
	}

	@Test
	public void rejectsMissingPatchedSystemName() {
		Map<String, Object> system = Map.of("type", "Feature", "properties", Map.of("uid", "urn:test"));

		AssertionError error = assertThrows(AssertionError.class,
				() -> UpdateTests.assertPatchedSystemName(system, "patched"));

		assertTrue(error.getMessage().contains(UpdateTests.REQ_UPDATE_SYSTEM));
		assertTrue(error.getMessage().contains("did not expose the expected properties.name"));
	}

	@Test
	public void rejectsUnchangedSystemName() {
		Map<String, Object> system = Map.of("type", "Feature", "properties", Map.of("name", "before"));

		AssertionError error = assertThrows(AssertionError.class,
				() -> UpdateTests.assertPatchedSystemName(system, "after"));

		assertTrue(error.getMessage().contains("Expected 'after', got 'before'"));
	}

}
