package org.opengis.cite.ogcapiconnectedsystems10.conformance.subsystems;

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
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;

/**
 * Structural checks for REQ-ETS-PART1-003 released ATS deployment.
 */
public class VerifySubsystemsSuite {

	private static final String BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/subsystem/";

	/**
	 * REQ-ETS-PART1-003; all Sprint 48 released scenarios.
	 */
	@Test
	public void subsystemClassContainsExactlyTheFiveReleasedProcedures() {
		List<Method> methods = Arrays.stream(SubsystemsTests.class.getDeclaredMethods())
			.filter(method -> method.getAnnotation(org.testng.annotations.Test.class) != null)
			.toList();
		Set<String> targets = Set.of(BASE + "collection", BASE + "recursive-param", BASE + "recursive-search-systems",
				BASE + "recursive-search-subsystems", BASE + "recursive-assoc");

		assertEquals(5, methods.size());
		for (Method method : methods) {
			org.testng.annotations.Test annotation = method.getAnnotation(org.testng.annotations.Test.class);
			assertTrue(method + " must use subsystems group",
					Arrays.asList(annotation.groups()).contains("subsystems"));
			assertTrue(method + " must execute after documented inherited evidence limitations",
					annotation.alwaysRun());
			assertEquals(method + " must identify one released target", 1,
					targets.stream().filter(annotation.description()::contains).count());
		}
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-PROCEDURE-ISOLATION-001.
	 */
	@Test
	public void beforeClassLoadsOnlyArgumentsAndRunsAfterSystem() throws Exception {
		Method setup = SubsystemsTests.class.getDeclaredMethod("fetchSubsystemArguments", ITestContext.class);
		org.testng.annotations.BeforeClass annotation = setup.getAnnotation(org.testng.annotations.BeforeClass.class);

		assertTrue(annotation != null);
		assertTrue(annotation.alwaysRun());
		assertTrue(Arrays.asList(annotation.dependsOnGroups()).contains("systemfeatures"));
		assertTrue(Arrays.stream(SubsystemsTests.class.getDeclaredFields())
			.noneMatch(field -> field.getName().toLowerCase().contains("response")));
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-E2E-EXECUTION-001.
	 */
	@Test
	public void documentedSystemEvidenceLimitationsDoNotBlockDirectProcedures() {
		for (ITestContext context : List.of(contextWithResult(true, "systemfeatures", "mobileSystemLocationIsUpdated",
				new SkipException("http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/location-time"
						+ " - optional mobile-system-id test-run argument was not supplied.")),
				contextWithResult(true, "systemfeatures", "systemResourcesEndpointIsValid", new SkipException(
						"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/resources-endpoint"
								+ " - https://example.test/api/systems returned a media type unsupported by this testing engine.")),
				contextWithResult(true, "systemfeatures", "canonicalSystemsEndpointIsValid", new SkipException(
						"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/system/canonical-endpoint"
								+ " - https://example.test/api/systems returned a media type unsupported by this testing engine.")))) {
			SubsystemsTests.skipWhenPrerequisiteUnsatisfied(context);
		}
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-E2E-EXECUTION-001.
	 */
	@Test
	public void unexpectedSystemSkipBlocksSubsystemProcedures() {
		ITestContext context = contextWithResult(true, "systemfeatures", "systemCollectionsAreValid",
				new SkipException("no System collection evidence"));

		assertThrows(SkipException.class, () -> SubsystemsTests.skipWhenPrerequisiteUnsatisfied(context));
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-E2E-EXECUTION-001.
	 */
	@Test
	public void inheritedFailureBlocksSubsystemProcedures() {
		ITestContext context = contextWithResult(false, "systemfeatures", "systemCollectionsAreValid",
				new AssertionError("invalid System collection"));

		assertThrows(SkipException.class, () -> SubsystemsTests.skipWhenPrerequisiteUnsatisfied(context));
	}

	/**
	 * REQ-ETS-PART1-003; SCENARIO-ETS-PART1-003-RELEASED-E2E-EXECUTION-001.
	 */
	@Test
	public void configurationFailureBlocksSubsystemProcedures() {
		ITestContext context = mock(ITestContext.class);
		IResultMap failedConfigurations = mock(IResultMap.class);
		IResultMap empty = mock(IResultMap.class);
		ITestResult result = mock(ITestResult.class);
		ITestNGMethod method = mock(ITestNGMethod.class);
		when(context.getFailedConfigurations()).thenReturn(failedConfigurations);
		when(context.getSkippedConfigurations()).thenReturn(empty);
		when(context.getFailedTests()).thenReturn(empty);
		when(context.getSkippedTests()).thenReturn(empty);
		when(failedConfigurations.getAllResults()).thenReturn(Set.of(result));
		when(empty.getAllResults()).thenReturn(Collections.emptySet());
		when(result.getMethod()).thenReturn(method);
		when(method.getMethodName()).thenReturn("fetchSystemArguments");

		assertThrows(SkipException.class, () -> SubsystemsTests.skipWhenPrerequisiteUnsatisfied(context));
	}

	private static ITestContext contextWithResult(boolean skipped, String group, String methodName,
			Throwable throwable) {
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
		when(method.getGroups()).thenReturn(new String[] { group });
		when(method.getMethodName()).thenReturn(methodName);
		return context;
	}

}
