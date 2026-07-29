package org.opengis.cite.ogcapiconnectedsystems10.conformance.createreplacedelete;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.testng.SkipException;

/**
 * Unit checks for the released Create/Replace/Delete transaction support.
 */
public class VerifyCreateReplaceDeleteSupport {

	private static final String REQUIREMENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/create-replace-delete/system";

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-REPRESENTATION-CLOSURE-001.
	 */
	@Test
	public void submittedContentComparisonAllowsServerManagedMembers() {
		Map<String, Object> submitted = Map.of("type", "Feature", "properties",
				Map.of("uid", "urn:test:system", "name", "submitted"));
		Map<String, Object> received = Map.of("id", "abc", "links", List.of(), "type", "Feature", "properties",
				Map.of("uid", "urn:test:system", "name", "submitted", "serverManaged", true));

		CreateReplaceDeleteSupport.assertSubmittedContent(submitted, received, REQUIREMENT);
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-REPRESENTATION-CLOSURE-001.
	 */
	@Test
	public void submittedContentComparisonFailsOnChangedNestedValue() {
		Map<String, Object> submitted = Map.of("type", "Feature", "properties",
				Map.of("uid", "urn:test:system", "name", "submitted"));
		Map<String, Object> received = Map.of("type", "Feature", "properties",
				Map.of("uid", "urn:test:system", "name", "changed"));

		AssertionError error = assertThrows(AssertionError.class,
				() -> CreateReplaceDeleteSupport.assertSubmittedContent(submitted, received, REQUIREMENT));
		assertTrue(error.getMessage().startsWith(REQUIREMENT));
		assertTrue(error.getMessage().contains("$.properties.name"));
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-MUTATION-SAFETY-001.
	 */
	@Test
	public void mutationGateRejectsMissingOptInAndPublicIut() {
		assertThrows(SkipException.class, () -> CreateReplaceDeleteSupport
			.ensureMutationAllowed(URI.create("http://localhost:8081/api/"), null, null, REQUIREMENT));
		assertThrows(SkipException.class,
				() -> CreateReplaceDeleteSupport.ensureMutationAllowed(
						URI.create("https://api.georobotix.io/ogc/t18/api/"), "true", "dedicated-mutable-iut",
						REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-INHERITED-TRANSACTION-001.
	 */
	@Test
	public void locationResolutionRejectsCrossOriginTargets() {
		URI root = URI.create("http://localhost:8081/sensorhub/api/");

		assertEquals(root.resolve("systems/abc"),
				CreateReplaceDeleteSupport.resolveCreatedResourceUri(root, "/systems/abc", REQUIREMENT));
		AssertionError error = assertThrows(AssertionError.class, () -> CreateReplaceDeleteSupport
			.resolveCreatedResourceUri(root, "https://other.example/systems/abc", REQUIREMENT));
		assertTrue(error.getMessage().startsWith(REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-CLEANUP-001.
	 */
	@Test
	public void cleanupRunsInReverseOrderAndDoesNotHidePrimaryFailure() {
		List<String> order = new ArrayList<>();
		CreateReplaceDeleteSupport.CleanupStack cleanup = new CreateReplaceDeleteSupport.CleanupStack(REQUIREMENT);
		cleanup.push("first", () -> order.add("first"));
		cleanup.push("second", () -> {
			order.add("second");
			throw new IllegalStateException("cleanup failed");
		});
		AssertionError primary = new AssertionError("primary failure");

		Throwable result = cleanup.close(primary);

		assertSame(primary, result);
		assertEquals(List.of("second", "first"), order);
		assertEquals(1, result.getSuppressed().length);
		assertTrue(result.getSuppressed()[0].getMessage().contains("cleanup failed"));
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-CLEANUP-001.
	 */
	@Test
	public void cleanupFailureBecomesVisibleWithoutPrimaryFailure() {
		AtomicInteger calls = new AtomicInteger();
		CreateReplaceDeleteSupport.CleanupStack cleanup = new CreateReplaceDeleteSupport.CleanupStack(REQUIREMENT);
		cleanup.push("owned resource", () -> {
			calls.incrementAndGet();
			throw new IllegalStateException("delete refused");
		});

		Throwable result = cleanup.close(null);

		assertEquals(1, calls.get());
		assertTrue(result instanceof AssertionError);
		assertTrue(result.getMessage().startsWith(REQUIREMENT));
		assertEquals(1, result.getSuppressed().length);
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-REPRESENTATION-CLOSURE-001.
	 */
	@Test
	public void submittedArraysAreComparedInOrder() {
		Map<String, Object> submitted = new LinkedHashMap<>();
		submitted.put("values", List.of(1, 2, 3));

		CreateReplaceDeleteSupport.assertSubmittedContent(submitted, Map.of("values", List.of(1, 2, 3)), REQUIREMENT);
		assertThrows(AssertionError.class, () -> CreateReplaceDeleteSupport.assertSubmittedContent(submitted,
				Map.of("values", List.of(3, 2, 1)), REQUIREMENT));
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-REPRESENTATION-CLOSURE-001.
	 */
	@Test
	public void representationSelectionIncludesEveryApplicableDeclaredEncoding() {
		Map<String, Object> both = Map.of("conformsTo",
				List.of(CreateReplaceDeleteSupport.CONF_GEOJSON, CreateReplaceDeleteSupport.CONF_SENSORML));

		assertEquals(List.of("application/geo+json", "application/sml+json"),
				CreateReplaceDeleteSupport.supportedMediaTypes("systems", both, REQUIREMENT));
		assertEquals(List.of("application/geo+json", "application/sml+json"),
				CreateReplaceDeleteSupport.supportedMediaTypes("deployments", both, REQUIREMENT));
		assertEquals(List.of("application/geo+json", "application/sml+json"),
				CreateReplaceDeleteSupport.supportedMediaTypes("procedures", both, REQUIREMENT));
		assertEquals(List.of("application/geo+json"),
				CreateReplaceDeleteSupport.supportedMediaTypes("samplingFeatures", both, REQUIREMENT));
		assertEquals(List.of("application/sml+json"),
				CreateReplaceDeleteSupport.supportedMediaTypes("properties", both, REQUIREMENT));
	}

}
