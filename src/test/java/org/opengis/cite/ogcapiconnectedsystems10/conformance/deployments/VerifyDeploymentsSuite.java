package org.opengis.cite.ogcapiconnectedsystems10.conformance.deployments;

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
 * Structural checks for the five released Deployment procedures.
 */
public class VerifyDeploymentsSuite {

	private static final String BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/deployment/";

	/**
	 * REQ-ETS-PART1-004; all Sprint 49 released scenarios.
	 */
	@Test
	public void deploymentClassContainsExactlyTheFiveReleasedProcedures() {
		List<Method> methods = Arrays.stream(DeploymentsTests.class.getDeclaredMethods())
			.filter(method -> method.getAnnotation(org.testng.annotations.Test.class) != null)
			.toList();
		Set<String> targets = Set.of(BASE + "canonical-url", BASE + "resources-endpoint", BASE + "canonical-endpoint",
				BASE + "collections", BASE + "ref-from-system");

		assertEquals(5, methods.size());
		for (Method method : methods) {
			org.testng.annotations.Test annotation = method.getAnnotation(org.testng.annotations.Test.class);
			assertTrue(method + " must use deployments group",
					Arrays.asList(annotation.groups()).contains("deployments"));
			assertTrue(method + " must execute independently after documented API Common evidence limitations",
					annotation.alwaysRun());
			assertEquals(method + " must identify one released target", 1,
					targets.stream().filter(annotation.description()::contains).count());
			assertEquals(method + " must not depend on another Deployment method", 0,
					annotation.dependsOnMethods().length);
		}
	}

	/**
	 * REQ-ETS-PART1-004; SCENARIO-ETS-PART1-004-RELEASED-PROCEDURE-ISOLATION-001.
	 */
	@Test
	public void beforeClassLoadsOnlyImmutableArguments() throws Exception {
		Method setup = DeploymentsTests.class.getDeclaredMethod("fetchDeploymentArguments",
				org.testng.ITestContext.class);
		org.testng.annotations.BeforeClass annotation = setup.getAnnotation(org.testng.annotations.BeforeClass.class);

		assertTrue(annotation != null);
		assertTrue(annotation.alwaysRun());
		assertTrue(Arrays.asList(annotation.dependsOnGroups()).contains("part1apicommon"));
		assertFalse(Arrays.stream(DeploymentsTests.class.getDeclaredFields())
			.anyMatch(field -> field.getName().toLowerCase().contains("response")
					|| field.getName().toLowerCase().contains("body")));
	}

	/**
	 * REQ-ETS-PART1-004; SCENARIO-ETS-PART1-004-RELEASED-DEPENDENCY-CASCADE-001.
	 */
	@Test
	public void systemFeaturesConfigurationDoesNotBlockDeployment() throws Exception {
		invokePrerequisiteGate(
				contextWithConfiguration("systemfeatures", SystemFeaturesTests.class, "fetchSystemArguments"));
	}

	/**
	 * REQ-ETS-PART1-004; SCENARIO-ETS-PART1-004-RELEASED-DEPENDENCY-CASCADE-001.
	 */
	@Test
	public void apiCommonConfigurationStillBlocksDeployment() {
		ITestContext context = contextWithConfiguration("part1apicommon", Part1ApiCommonTests.class,
				"fetchApiCommonArguments");

		assertThrows(SkipException.class, () -> invokePrerequisiteGate(context));
	}

	private static void invokePrerequisiteGate(ITestContext context) throws Exception {
		Method method = DeploymentsTests.class.getDeclaredMethod("skipWhenPrerequisiteUnsatisfied", ITestContext.class);
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

}
