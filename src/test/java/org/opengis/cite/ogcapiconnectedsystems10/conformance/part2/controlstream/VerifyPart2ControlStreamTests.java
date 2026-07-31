package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.controlstream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.SkipException;

/**
 * Unit checks for the Part 2 ControlStream released ATS logic.
 */
public class VerifyPart2ControlStreamTests {

	private static final String BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/controlstream/";

	private static final Set<String> RELEASED_TARGETS = Set.of(BASE + "sf-ref-from-controlstream",
			BASE + "foi-ref-from-controlstream", BASE + "canonical-url", BASE + "resources-endpoint",
			BASE + "canonical-endpoint", BASE + "ref-from-system", BASE + "ref-from-deployment", BASE + "collections",
			BASE + "schema-op", BASE + "cmd-canonical-url", BASE + "cmd-resources-endpoint",
			BASE + "cmd-canonical-endpoint", BASE + "cmd-ref-from-controlstream", BASE + "cmd-collections",
			BASE + "status-resources-endpoint", BASE + "command-status-endpoint", BASE + "result-resources-endpoint",
			BASE + "command-result-endpoint");

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-RELEASED-PROCEDURES-001.
	 */
	@org.junit.Test
	public void controlStreamClassContainsExactlyTheEighteenReleasedProcedures() {
		List<Method> methods = Arrays.stream(Part2ControlStreamTests.class.getDeclaredMethods())
			.filter(method -> method.getAnnotation(org.testng.annotations.Test.class) != null)
			.toList();

		assertEquals(18, methods.size());
		assertEquals(18,
				methods.stream()
					.map(method -> method.getAnnotation(org.testng.annotations.Test.class).description())
					.flatMap(description -> RELEASED_TARGETS.stream().filter(description::contains))
					.distinct()
					.count());
		for (Method method : methods) {
			org.testng.annotations.Test annotation = method.getAnnotation(org.testng.annotations.Test.class);
			assertTrue(method + " must use part2controlstream group",
					Arrays.asList(annotation.groups()).contains(Part2ControlStreamTests.GROUP));
			assertTrue(method + " must remain independently executable", annotation.alwaysRun());
			assertEquals(method + " must identify exactly one released target", 1,
					RELEASED_TARGETS.stream().filter(annotation.description()::contains).count());
			assertTrue(method + " must trace REQ-ETS-PART2-003",
					annotation.description().contains("REQ-ETS-PART2-003"));
			assertEquals(method + " must not depend on another Part 2 ControlStream method", 0,
					annotation.dependsOnMethods().length);
		}
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-DIRECT-PREREQUISITE-001.
	 */
	@org.junit.Test
	public void beforeClassLoadsOnlyImmutableArgumentsAfterPart2ApiCommon() throws Exception {
		Method setup = Part2ControlStreamTests.class.getDeclaredMethod("fetchPart2ControlStreamInputs",
				org.testng.ITestContext.class);
		org.testng.annotations.BeforeClass annotation = setup.getAnnotation(org.testng.annotations.BeforeClass.class);

		assertTrue(annotation != null);
		assertTrue(annotation.alwaysRun());
		assertTrue(Arrays.asList(annotation.dependsOnGroups()).contains("part2apicommon"));
		assertFalse(Arrays.stream(Part2ControlStreamTests.class.getDeclaredFields())
			.anyMatch(field -> field.getName().toLowerCase().contains("response")
					|| field.getName().toLowerCase().contains("body")));
		assertNotNull("Sprint 61 setup must expose a direct configure hook for controlled HTTP regressions.",
				Part2ControlStreamTests.class.getDeclaredMethod("configure", URI.class));
	}

	@org.junit.Test
	public void controlStreamShapeRequiresResourceSpecificMembers() {
		assertTrue(Part2ControlStreamTests.hasControlStreamShape(Map.of("id", "cs-1", "system@link",
				Map.of("href", "systems/sys-1"), "inputName", "cmd", "controlledProperties",
				List.of(Map.of("label", "speed")), "formats", List.of("application/swe+json"), "async", false)));
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
	public void commandShapeRequiresCommandSpecificMembers() throws Exception {
		Method helper = Part2ControlStreamTests.class.getDeclaredMethod("hasCommandShape", Object.class);
		helper.setAccessible(true);
		assertTrue((Boolean) helper.invoke(null, Map.of("id", "cmd-1", "controlstream@id", "cs-1", "issueTime",
				"2026-07-31T00:00:00Z", "parameters", Map.of("setpoint", 12))));
		assertFalse("A generic JSON object with only an id must not masquerade as a Command.",
				(Boolean) helper.invoke(null, Map.of("id", "cmd-1")));
	}

	@org.junit.Test
	public void itemsOnlyCollectionShapeAllowsEmptyNestedCommands() {
		assertTrue(Part2ControlStreamTests.hasItemsOnlyCollectionShape(Map.of("items", List.of())));
		assertFalse(Part2ControlStreamTests.hasItemsOnlyCollectionShape(Map.of("links", List.of())));
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-CANONICAL-LINK-EVIDENCE-001.
	 */
	@org.junit.Test
	public void canonicalSupportRequiresAdvertisedCanonicalLinkAndComparesWithoutThatLink() throws Exception {
		Class<?> support = Class.forName(
				"org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.controlstream.Part2ControlStreamSupport");
		Method canonicalUri = support.getDeclaredMethod("canonicalUri", Map.class, URI.class, URI.class, String.class);
		Method withoutCanonicalLinks = support.getDeclaredMethod("withoutCanonicalLinks", Map.class);
		Method hasCanonicalLink = support.getDeclaredMethod("hasCanonicalLink", Map.class);
		canonicalUri.setAccessible(true);
		withoutCanonicalLinks.setAccessible(true);
		hasCanonicalLink.setAccessible(true);

		URI page = URI.create("https://example.test/api/collections/controlstreams/items");
		URI root = URI.create("https://example.test/api/");
		Map<String, Object> collectionItem = Map.of("id", "cs-1", "links",
				List.of(Map.of("rel", "canonical", "href", "https://example.test/api/controls/cs-1"),
						Map.of("rel", "alternate", "href", "../controlstreams/cs-1?f=json")),
				"inputName", "cmd");
		Map<String, Object> canonicalBody = Map.of("id", "cs-1", "links",
				List.of(Map.of("rel", "alternate", "href", "../controlstreams/cs-1?f=json")), "inputName", "cmd");

		assertEquals(root.resolve("controls/cs-1"),
				canonicalUri.invoke(null, collectionItem, page, root, BASE + "canonical-url"));
		assertEquals(withoutCanonicalLinks.invoke(null, collectionItem),
				withoutCanonicalLinks.invoke(null, canonicalBody));
		assertFalse(
				"A synthesized /controlstreams/{id} or /controls/{id} URL must not replace an advertised canonical link.",
				(Boolean) hasCanonicalLink.invoke(null, Map.of("id", "cs-1")));
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-SCHEMA-OP-FORMATS-001.
	 */
	@org.junit.Test
	@SuppressWarnings("unchecked")
	public void schemaOpExtractsEveryAdvertisedCommandFormat() throws Exception {
		Class<?> support = Class.forName(
				"org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.controlstream.Part2ControlStreamSupport");
		Method commandFormats = support.getDeclaredMethod("commandFormats", Map.class);
		commandFormats.setAccessible(true);
		Map<String, Object> controlStream = Map.of("id", "cs-1", "formats", List.of("application/json",
				Map.of("cmdFormat", "application/swe+json"), Map.of("commandFormat", "application/swe+binary")));

		assertEquals(List.of("application/json", "application/swe+json", "application/swe+binary"),
				(List<String>) commandFormats.invoke(null, controlStream));
		assertTrue("No advertised formats means schema-op is prerequisite-incomplete, not one unparameterized GET.",
				((List<String>) commandFormats.invoke(null, Map.of("id", "cs-1"))).isEmpty());
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-COLLECTION-TAGGING-001.
	 */
	@org.junit.Test
	@SuppressWarnings("unchecked")
	public void exactCollectionSelectionUsesItemTypeNotNameSubstrings() throws Exception {
		Class<?> support = Class.forName(
				"org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.controlstream.Part2ControlStreamSupport");
		Method collectionsWithItemType = support.getDeclaredMethod("collectionsWithItemType", List.class, String.class);
		collectionsWithItemType.setAccessible(true);
		List<Map<String, Object>> advertised = List.of(Map.of("id", "custom-controls", "itemType", "ControlStream"),
				Map.of("id", "controlstreams", "itemType", "feature"), Map.of("id", "commands", "itemType", "Command"));

		assertEquals(List.of(advertised.get(0)),
				(List<Map<String, Object>>) collectionsWithItemType.invoke(null, advertised, "ControlStream"));
		assertEquals(List.of(advertised.get(2)),
				(List<Map<String, Object>>) collectionsWithItemType.invoke(null, advertised, "Command"));
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-ASSOCIATION-SUBRESOURCES-001.
	 */
	@org.junit.Test
	@SuppressWarnings("unchecked")
	public void allResourceIdsRequireEveryApplicableSubresourceEndpoint() throws Exception {
		Class<?> support = Class.forName(
				"org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.controlstream.Part2ControlStreamSupport");
		Method localIds = support.getDeclaredMethod("localIds", List.class, String.class);
		localIds.setAccessible(true);
		List<Map<String, Object>> resources = List.of(Map.of("id", "a"), Map.of("id", "b"));

		assertEquals(List.of("a", "b"), (List<String>) localIds.invoke(null, resources, BASE + "resources-endpoint"));
	}

	/**
	 * REQ-ETS-PART2-003; SCENARIO-ETS-PART2-003-STATUS-RESULT-ENDPOINTS-001.
	 */
	@org.junit.Test
	public void commandStatusAndResultSchemasRejectGenericItems() throws Exception {
		Class<?> support = Class.forName(
				"org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.controlstream.Part2ControlStreamSupport");
		Method validateStatusResource = support.getDeclaredMethod("validateCommandStatusResource", Map.class,
				String.class, String.class);
		Method validateResultResource = support.getDeclaredMethod("validateCommandResultResource", Map.class,
				String.class, String.class);
		validateStatusResource.setAccessible(true);
		validateResultResource.setAccessible(true);

		assertInvocationFails(validateStatusResource,
				Map.of("id", "status-1", "command@id", "cmd-1", "reportTime", "2026-07-31T00:00:00Z"));
		assertInvocationFails(validateResultResource, Map.of("id", "result-1", "command@id", "cmd-1"));
	}

	@org.junit.Test
	public void constantsUseOfficialControlStreamIdentifiers() {
		String joined = String.join(" ", Part2ControlStreamTests.CONF_CONTROLSTREAM,
				Part2ControlStreamTests.REQ_CONTROLSTREAM, Part2ControlStreamTests.REQ_CMD_REF_FROM_CONTROLSTREAM);

		assertTrue(joined.contains("/conf/controlstream"));
		assertTrue(joined.contains("/req/controlstream"));
		assertFalse(joined.contains("dynamic"));
	}

	private static void assertInvocationFails(Method method, Map<String, Object> body) throws Exception {
		InvocationTargetException error = assertThrows(InvocationTargetException.class,
				() -> method.invoke(null, body, BASE + "command-status-endpoint", "fixture"));
		assertTrue(error.getCause() instanceof AssertionError);
	}

}
