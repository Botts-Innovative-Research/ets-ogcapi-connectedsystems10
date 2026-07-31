package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.apicommon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonTests;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;

/**
 * Unit checks for Part 2 API Common released ATS logic.
 */
public class VerifyPart2ApiCommonTests {

	private static final String BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/api-common/";

	/**
	 * REQ-ETS-PART2-001; SCENARIO-ETS-PART2-001-RELEASED-PROCEDURE-ISOLATION-001.
	 */
	@org.junit.Test
	public void part2ApiCommonClassContainsExactlyTheTwoReleasedProcedures() {
		List<Method> methods = Arrays.stream(Part2ApiCommonTests.class.getDeclaredMethods())
			.filter(method -> method.getAnnotation(org.testng.annotations.Test.class) != null)
			.toList();
		Set<String> targets = Set.of(BASE + "resources", BASE + "resource-collection");

		assertEquals(2, methods.size());
		assertEquals(2,
				methods.stream()
					.map(method -> method.getAnnotation(org.testng.annotations.Test.class).description())
					.flatMap(description -> targets.stream().filter(description::contains))
					.distinct()
					.count());
		for (Method method : methods) {
			org.testng.annotations.Test annotation = method.getAnnotation(org.testng.annotations.Test.class);
			assertTrue(method + " must use part2apicommon group",
					Arrays.asList(annotation.groups()).contains(Part2ApiCommonTests.GROUP));
			assertTrue(method + " must remain independently executable", annotation.alwaysRun());
			assertEquals(method + " must identify exactly one released target", 1,
					targets.stream().filter(annotation.description()::contains).count());
			assertTrue(method + " must trace REQ-ETS-PART2-001",
					annotation.description().contains("REQ-ETS-PART2-001"));
			assertEquals(method + " must not depend on another Part 2 API Common method", 0,
					annotation.dependsOnMethods().length);
		}
	}

	/**
	 * REQ-ETS-PART2-001; SCENARIO-ETS-PART2-001-RELEASED-PROCEDURE-ISOLATION-001.
	 */
	@org.junit.Test
	public void beforeClassLoadsOnlyImmutableArguments() throws Exception {
		Method setup = Part2ApiCommonTests.class.getDeclaredMethod("fetchPart2ApiCommonInputs", ITestContext.class);
		org.testng.annotations.BeforeClass annotation = setup.getAnnotation(org.testng.annotations.BeforeClass.class);

		assertTrue(annotation != null);
		assertTrue(annotation.alwaysRun());
		assertTrue(Arrays.asList(annotation.dependsOnGroups()).contains("part1apicommon"));
		assertFalse(Arrays.stream(Part2ApiCommonTests.class.getDeclaredFields())
			.anyMatch(field -> field.getName().toLowerCase().contains("response")
					|| field.getName().toLowerCase().contains("body")));
	}

	/**
	 * REQ-ETS-PART2-001; SCENARIO-ETS-PART2-001-RELEASED-DEPENDENCY-CASCADE-001.
	 */
	@org.junit.Test
	public void apiCommonConfigurationBlocksPart2ApiCommonBeforeIutAccess() {
		ITestContext context = contextWithConfiguration("part1apicommon", Part1ApiCommonTests.class,
				"fetchApiCommonArguments");

		assertThrows(SkipException.class, () -> invokePrerequisiteGate(context));
	}

	/**
	 * REQ-ETS-PART2-001; SCENARIO-ETS-PART2-001-RELEASED-DEPENDENCY-CASCADE-001.
	 */
	@org.junit.Test
	public void documentedDatetimeEvidenceLimitationDoesNotBlockPart2ApiCommon() throws Exception {
		invokePrerequisiteGate(contextWithSkippedTest("part1apicommon", "datetimeUsesValidTime",
				new SkipException(Part1ApiCommonTests.DATETIME_EVIDENCE_LIMITATION)));
	}

	/**
	 * REQ-ETS-PART2-001; SCENARIO-ETS-PART2-001-RELEASED-DEPENDENCY-CASCADE-001.
	 */
	@org.junit.Test
	public void unexpectedApiCommonSkipBlocksPart2ApiCommon() {
		ITestContext context = contextWithSkippedTest("part1apicommon", "resourceIdsAreUniqueWithinEachType",
				new SkipException("unexpected API Common evidence gap"));

		assertThrows(SkipException.class, () -> invokePrerequisiteGate(context));
	}

	@org.junit.Test
	public void declaresApiCommonOnlyForExactConformanceUri() {
		Map<String, Object> body = Map.of("conformsTo",
				List.of("http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/datastream",
						Part2ApiCommonTests.CONF_PART2_API_COMMON));

		assertTrue(Part2ApiCommonTests.declaresConformance(body, Part2ApiCommonTests.CONF_PART2_API_COMMON));
		assertFalse(Part2ApiCommonTests.declaresConformance(body,
				"http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/dynamic-data"));
	}

	@org.junit.Test
	public void missingConformsToDoesNotDeclareApiCommon() {
		assertFalse(Part2ApiCommonTests.declaresConformance(Map.of(), Part2ApiCommonTests.CONF_PART2_API_COMMON));
		assertFalse(Part2ApiCommonTests.declaresConformance(null, Part2ApiCommonTests.CONF_PART2_API_COMMON));
	}

	@org.junit.Test
	public void discoversOnlyAdvertisedPart2Collections() {
		Map<String, Object> landing = Map.of("links",
				List.of(Map.of("rel", "datastreams", "href", "datastreams"),
						Map.of("rel", "self", "href", "https://example.test/api/observations"),
						Map.of("rel", "service-desc", "href", "api")));

		List<URI> uris = Part2ApiCommonTests.discoverPart2CollectionUris(landing,
				URI.create("https://example.test/api/"));

		assertEquals(List.of(URI.create("https://example.test/api/datastreams"),
				URI.create("https://example.test/api/observations")), uris);
		assertFalse("The helper must not synthesize /commands when the landing page does not advertise it.",
				uris.contains(URI.create("https://example.test/api/commands")));
	}

	@org.junit.Test
	public void systemHistoryVendorExtensionIsNotDiscoveredAsOgcPart2Collection() {
		Map<String, Object> landing = Map.of("links", List.of(Map.of("rel", "systemhistory", "href", "systemHistory"),
				Map.of("rel", "system-history", "href", "system-history")));

		List<URI> uris = Part2ApiCommonTests.discoverPart2CollectionUris(landing,
				URI.create("https://example.test/api/"));

		assertTrue(
				"OGC 23-002 Annex A does not define /conf/system-history; vendor extension links must not become Part 2 API Common PASS evidence.",
				uris.isEmpty());
	}

	@org.junit.Test
	public void crossOriginAdvertisedPart2CollectionIsNotDiscovered() {
		Map<String, Object> landing = Map.of("links",
				List.of(Map.of("rel", "datastreams", "href", "https://elsewhere.test/datastreams"),
						Map.of("rel", "observations", "href", "/api/observations")));

		List<URI> uris = Part2ApiCommonTests.discoverPart2CollectionUris(landing,
				URI.create("https://example.test/api/"));

		assertEquals(List.of(URI.create("https://example.test/api/observations")), uris);
	}

	@org.junit.Test
	public void resourceCollectionShapeRequiresItemsAndLinksArrays() {
		assertTrue(Part2ApiCommonTests.hasResourceCollectionShape(Map.of("items", List.of(), "links", List.of())));
		assertFalse(Part2ApiCommonTests.hasResourceCollectionShape(Map.of("items", List.of())));
		assertFalse(Part2ApiCommonTests.hasResourceCollectionShape(Map.of("items", Map.of(), "links", List.of())));
	}

	@org.junit.Test
	public void constantsDoNotUseDynamicDataIdentifiers() {
		String joined = String.join(" ", Part2ApiCommonTests.CONF_PART2_API_COMMON, Part2ApiCommonTests.REQ_API_COMMON,
				Part2ApiCommonTests.REQ_RESOURCES, Part2ApiCommonTests.REQ_RESOURCE_COLLECTION);

		assertFalse(joined.contains("dynamic-data"));
		assertFalse(joined.contains("dynamic"));
	}

	private static void invokePrerequisiteGate(ITestContext context) throws Exception {
		Method method = Part2ApiCommonTests.class.getDeclaredMethod("skipWhenPrerequisiteUnsatisfied",
				ITestContext.class);
		method.setAccessible(true);
		try {
			method.invoke(null, context);
		}
		catch (InvocationTargetException ex) {
			if (ex.getCause() instanceof RuntimeException runtimeException) {
				throw runtimeException;
			}
			if (ex.getCause() instanceof Error error) {
				throw error;
			}
			throw ex;
		}
	}

	private static ITestContext contextWithConfiguration(String group, Class<?> realClass, String methodName) {
		ITestContext context = mock(ITestContext.class);
		IResultMap configurations = mock(IResultMap.class);
		IResultMap empty = mock(IResultMap.class);
		ITestResult result = mock(ITestResult.class);
		ITestNGMethod method = mock(ITestNGMethod.class);
		when(context.getFailedConfigurations()).thenReturn(configurations);
		when(context.getSkippedConfigurations()).thenReturn(empty);
		when(context.getFailedTests()).thenReturn(empty);
		when(context.getSkippedTests()).thenReturn(empty);
		when(configurations.getAllResults()).thenReturn(Set.of(result));
		when(empty.getAllResults()).thenReturn(Collections.emptySet());
		when(result.getMethod()).thenReturn(method);
		when(method.getGroups()).thenReturn(new String[] { group });
		when(method.getRealClass()).thenReturn(realClass);
		when(method.getMethodName()).thenReturn(methodName);
		return context;
	}

	private static ITestContext contextWithSkippedTest(String group, String methodName, Throwable throwable) {
		ITestContext context = mock(ITestContext.class);
		IResultMap empty = mock(IResultMap.class);
		IResultMap skipped = mock(IResultMap.class);
		ITestResult result = mock(ITestResult.class);
		ITestNGMethod method = mock(ITestNGMethod.class);
		when(context.getFailedConfigurations()).thenReturn(empty);
		when(context.getSkippedConfigurations()).thenReturn(empty);
		when(context.getFailedTests()).thenReturn(empty);
		when(context.getSkippedTests()).thenReturn(skipped);
		when(empty.getAllResults()).thenReturn(Collections.emptySet());
		when(skipped.getAllResults()).thenReturn(Set.of(result));
		when(result.getMethod()).thenReturn(method);
		when(result.getThrowable()).thenReturn(throwable);
		when(method.getGroups()).thenReturn(new String[] { group });
		when(method.getMethodName()).thenReturn(methodName);
		return context;
	}

}
