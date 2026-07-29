package org.opengis.cite.ogcapiconnectedsystems10.conformance.advancedfiltering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonTests;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.systemfeatures.SystemFeaturesTests;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;

/**
 * Structural checks for the 25 released Advanced Filtering procedures.
 */
public class VerifyAdvancedFilteringReleasedSuite {

	private static final String REQ_BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/";

	private static final String REC_BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/rec/advanced-filtering/";

	/**
	 * REQ-ETS-PART1-009; all Sprint 55 released-procedure scenarios.
	 */
	@Test
	public void advancedFilteringClassContainsExactlyTheTwentyFiveReleasedProcedures() {
		List<Method> methods = Arrays.stream(AdvancedFilteringTests.class.getDeclaredMethods())
			.filter(method -> method.getAnnotation(org.testng.annotations.Test.class) != null)
			.toList();
		Set<String> targets = Set.of(REQ_BASE + "id-list-schema", REQ_BASE + "resource-by-id",
				REQ_BASE + "resource-by-keyword", REC_BASE + "resource-by-property", REQ_BASE + "feature-by-geom",
				REQ_BASE + "system-by-parent", REQ_BASE + "system-by-procedure", REQ_BASE + "system-by-foi",
				REQ_BASE + "system-by-obsprop", REQ_BASE + "system-by-controlprop", REQ_BASE + "deployment-by-parent",
				REQ_BASE + "deployment-by-system", REQ_BASE + "deployment-by-foi", REQ_BASE + "deployment-by-obsprop",
				REQ_BASE + "deployment-by-controlprop", REQ_BASE + "procedure-by-obsprop",
				REQ_BASE + "procedure-by-controlprop", REQ_BASE + "sf-by-foi", REQ_BASE + "sf-by-obsprop",
				REQ_BASE + "sf-by-controlprop", REQ_BASE + "prop-by-baseprop", REQ_BASE + "prop-by-object",
				REQ_BASE + "combined-filters", REC_BASE + "indirect-prop", REC_BASE + "indirect-foi");

		assertEquals(25, methods.size());
		assertEquals(25,
				methods.stream()
					.map(method -> method.getAnnotation(org.testng.annotations.Test.class).description())
					.flatMap(description -> targets.stream().filter(description::contains))
					.distinct()
					.count());
		for (Method method : methods) {
			org.testng.annotations.Test annotation = method.getAnnotation(org.testng.annotations.Test.class);
			assertTrue(method + " must use advancedfiltering group",
					Arrays.asList(annotation.groups()).contains("advancedfiltering"));
			assertTrue(method + " must remain independently executable", annotation.alwaysRun());
			assertEquals(method + " must identify exactly one released target", 1,
					targets.stream().filter(annotation.description()::contains).count());
			assertEquals(method + " must not depend on another Advanced Filtering method", 0,
					annotation.dependsOnMethods().length);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-PROCEDURE-ISOLATION-001.
	 */
	@Test
	public void beforeClassLoadsOnlyImmutableArguments() throws Exception {
		Method setup = AdvancedFilteringTests.class.getDeclaredMethod("fetchAdvancedFilteringArguments",
				ITestContext.class);
		org.testng.annotations.BeforeClass annotation = setup.getAnnotation(org.testng.annotations.BeforeClass.class);

		assertTrue(annotation != null);
		assertTrue(annotation.alwaysRun());
		assertTrue(Arrays.asList(annotation.dependsOnGroups()).contains("part1apicommon"));
		assertFalse(Arrays.stream(AdvancedFilteringTests.class.getDeclaredFields())
			.anyMatch(field -> field.getName().toLowerCase().contains("response")
					|| field.getName().toLowerCase().contains("body")
					|| field.getName().toLowerCase().contains("seed")));
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-DEPENDENCY-CASCADE-001.
	 */
	@Test
	public void systemFeaturesConfigurationDoesNotBlockAdvancedFiltering() throws Exception {
		invokePrerequisiteGate(
				contextWithConfiguration("systemfeatures", SystemFeaturesTests.class, "fetchSystemArguments"));
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-DEPENDENCY-CASCADE-001.
	 */
	@Test
	public void apiCommonConfigurationBlocksAdvancedFiltering() {
		ITestContext context = contextWithConfiguration("part1apicommon", Part1ApiCommonTests.class,
				"fetchApiCommonArguments");

		assertThrows(SkipException.class, () -> invokePrerequisiteGate(context));
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-E2E-EXECUTION-001.
	 */
	@Test
	public void documentedDatetimeEvidenceLimitationDoesNotBlockAdvancedFiltering() throws Exception {
		invokePrerequisiteGate(contextWithSkippedTest("part1apicommon", "datetimeUsesValidTime",
				new SkipException(Part1ApiCommonTests.DATETIME_EVIDENCE_LIMITATION)));
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-DEPENDENCY-CASCADE-001.
	 */
	@Test
	public void unexpectedApiCommonSkipBlocksAdvancedFiltering() {
		ITestContext context = contextWithSkippedTest("part1apicommon", "canonicalResourcesHaveUid",
				new SkipException("unexpected API Common evidence gap"));

		assertThrows(SkipException.class, () -> invokePrerequisiteGate(context));
	}

	private static void invokePrerequisiteGate(ITestContext context) throws Exception {
		Method method = AdvancedFilteringTests.class.getDeclaredMethod("skipWhenPrerequisiteUnsatisfied",
				ITestContext.class);
		method.setAccessible(true);
		try {
			method.invoke(null, context);
		}
		catch (InvocationTargetException ex) {
			if (ex.getCause() instanceof RuntimeException) {
				throw (RuntimeException) ex.getCause();
			}
			if (ex.getCause() instanceof Error) {
				throw (Error) ex.getCause();
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
