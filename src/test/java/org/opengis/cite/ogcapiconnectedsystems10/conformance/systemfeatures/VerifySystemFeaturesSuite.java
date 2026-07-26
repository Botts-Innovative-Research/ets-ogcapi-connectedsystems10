package org.opengis.cite.ogcapiconnectedsystems10.conformance.systemfeatures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonTests;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;

/**
 * Structural checks for REQ-ETS-PART1-002 released ATS deployment.
 */
public class VerifySystemFeaturesSuite {

	private static final String BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/";

	/**
	 * REQ-ETS-PART1-002; all Sprint 47 released scenarios.
	 */
	@Test
	public void systemClassContainsExactlyTheSixReleasedProcedures() {
		List<Method> methods = Arrays.stream(SystemFeaturesTests.class.getDeclaredMethods())
			.filter(method -> method.getAnnotation(org.testng.annotations.Test.class) != null)
			.toList();
		Set<String> targets = Set.of(BASE + "rec/system/location", BASE + "req/system/location-time",
				BASE + "req/system/canonical-url", BASE + "req/system/resources-endpoint",
				BASE + "req/system/canonical-endpoint", BASE + "req/system/collections");

		assertEquals(6, methods.size());
		for (Method method : methods) {
			org.testng.annotations.Test annotation = method.getAnnotation(org.testng.annotations.Test.class);
			assertTrue(method + " must use systemfeatures group",
					Arrays.asList(annotation.groups()).contains("systemfeatures"));
			assertTrue(method + " must execute after the documented API Common datetime evidence limitation",
					annotation.alwaysRun());
			assertEquals(method + " must identify one released target", 1,
					targets.stream().filter(annotation.description()::contains).count());
		}
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-LOCATION-TIME-001.
	 */
	@Test
	public void missingMobileSystemInputSkipsLocationTime() {
		assertThrows(SkipException.class, () -> new SystemFeaturesTests().mobileSystemLocationIsUpdated());
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-PROCEDURE-ISOLATION-001.
	 */
	@Test
	public void beforeClassLoadsArgumentsWithoutFetchingNetworkEvidence() throws Exception {
		Method setup = SystemFeaturesTests.class.getDeclaredMethod("fetchSystemArguments",
				org.testng.ITestContext.class);
		org.testng.annotations.BeforeClass annotation = setup.getAnnotation(org.testng.annotations.BeforeClass.class);
		assertTrue(annotation != null);
		assertTrue(annotation.alwaysRun());
		assertTrue(Arrays.stream(SystemFeaturesTests.class.getDeclaredFields())
			.noneMatch(field -> field.getName().equals("canonicalSystems")));
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-E2E-EXECUTION-001.
	 */
	@Test
	public void exactDatetimeEvidenceLimitationDoesNotBlockSystemProcedures() {
		ITestContext context = contextWithApiCommonResult(true, "datetimeUsesValidTime",
				new SkipException(Part1ApiCommonTests.DATETIME_EVIDENCE_LIMITATION));

		SystemFeaturesTests.skipWhenPrerequisiteUnsatisfied(context);
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-E2E-EXECUTION-001.
	 */
	@Test
	public void anyOtherApiCommonSkipBlocksSystemProcedures() {
		ITestContext context = contextWithApiCommonResult(true, "resourceIdsAreUniqueWithinEachType",
				new SkipException("configuration unavailable"));

		assertThrows(SkipException.class, () -> SystemFeaturesTests.skipWhenPrerequisiteUnsatisfied(context));
	}

	/**
	 * REQ-ETS-PART1-002; SCENARIO-ETS-PART1-002-RELEASED-E2E-EXECUTION-001.
	 */
	@Test
	public void apiCommonFailureBlocksSystemProcedures() {
		ITestContext context = contextWithApiCommonResult(false, "resourceIdsAreUniqueWithinEachType",
				new AssertionError("duplicate ID"));

		assertThrows(SkipException.class, () -> SystemFeaturesTests.skipWhenPrerequisiteUnsatisfied(context));
	}

	private static ITestContext contextWithApiCommonResult(boolean skipped, String methodName, Throwable throwable) {
		ITestContext context = mock(ITestContext.class);
		IResultMap failed = mock(IResultMap.class);
		IResultMap skippedResults = mock(IResultMap.class);
		IResultMap failedConfigurations = mock(IResultMap.class);
		IResultMap skippedConfigurations = mock(IResultMap.class);
		ITestResult result = mock(ITestResult.class);
		ITestNGMethod method = mock(ITestNGMethod.class);
		when(context.getFailedTests()).thenReturn(failed);
		when(context.getSkippedTests()).thenReturn(skippedResults);
		when(context.getFailedConfigurations()).thenReturn(failedConfigurations);
		when(context.getSkippedConfigurations()).thenReturn(skippedConfigurations);
		when(failed.getAllResults()).thenReturn(skipped ? Collections.emptySet() : Set.of(result));
		when(skippedResults.getAllResults()).thenReturn(skipped ? Set.of(result) : Collections.emptySet());
		when(failedConfigurations.getAllResults()).thenReturn(Collections.emptySet());
		when(skippedConfigurations.getAllResults()).thenReturn(Collections.emptySet());
		when(result.getMethod()).thenReturn(method);
		when(result.getThrowable()).thenReturn(throwable);
		when(method.getGroups()).thenReturn(new String[] { "part1apicommon" });
		when(method.getMethodName()).thenReturn(methodName);
		return context;
	}

}
