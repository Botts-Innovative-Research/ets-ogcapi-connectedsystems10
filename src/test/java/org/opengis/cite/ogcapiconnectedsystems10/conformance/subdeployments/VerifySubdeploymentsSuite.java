package org.opengis.cite.ogcapiconnectedsystems10.conformance.subdeployments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
 * Structural checks for REQ-ETS-PART1-005 released ATS deployment.
 */
public class VerifySubdeploymentsSuite {

	private static final String BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/subdeployment/";

	/**
	 * REQ-ETS-PART1-005; all Sprint 51 released scenarios.
	 */
	@Test
	public void classContainsExactlyTheFiveIndependentReleasedProcedures() {
		List<Method> methods = Arrays.stream(SubdeploymentsTests.class.getDeclaredMethods())
			.filter(method -> method.getAnnotation(org.testng.annotations.Test.class) != null)
			.toList();
		Set<String> targets = Set.of(BASE + "collection", BASE + "recursive-param",
				BASE + "recursive-search-deployments", BASE + "recursive-search-subdeployments",
				BASE + "recursive-assoc");

		assertEquals(5, methods.size());
		for (Method method : methods) {
			org.testng.annotations.Test annotation = method.getAnnotation(org.testng.annotations.Test.class);
			assertTrue(method + " must use subdeployments group",
					Arrays.asList(annotation.groups()).contains("subdeployments"));
			assertTrue(method + " must remain independently executable", annotation.alwaysRun());
			assertEquals(method + " must identify one released target", 1,
					targets.stream().filter(annotation.description()::contains).count());
			assertEquals(method + " must not depend on another Subdeployment method", 0,
					annotation.dependsOnMethods().length);
		}
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-PROCEDURE-ISOLATION-001.
	 */
	@Test
	public void beforeClassLoadsOnlyArgumentsAndDependsOnDeployment() throws Exception {
		Method setup = SubdeploymentsTests.class.getDeclaredMethod("fetchSubdeploymentArguments", ITestContext.class);
		org.testng.annotations.BeforeClass annotation = setup.getAnnotation(org.testng.annotations.BeforeClass.class);

		assertTrue(annotation != null);
		assertTrue(annotation.alwaysRun());
		assertEquals(List.of("deployments"), Arrays.asList(annotation.dependsOnGroups()));
		assertFalse(Arrays.stream(SubdeploymentsTests.class.getDeclaredFields())
			.anyMatch(field -> field.getName().toLowerCase().contains("response")
					|| field.getName().toLowerCase().contains("body")));
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-PROCEDURE-ISOLATION-001.
	 */
	@Test
	public void unrelatedSiblingResultsAndConfigurationsDoNotBlockSubdeployment() {
		SubdeploymentsTests.skipWhenPrerequisiteUnsatisfied(
				contextWithTestResult(false, "systemfeatures", "systemCollectionsAreValid"));
		SubdeploymentsTests.skipWhenPrerequisiteUnsatisfied(
				contextWithConfigurationResult(false, "procedures", "fetchProcedureArguments"));
	}

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-DEPENDENCY-CASCADE-001.
	 */
	@Test
	public void inheritedDeploymentFailureOrSkipBlocksSubdeployment() {
		for (ITestContext context : List.of(
				contextWithTestResult(false, "deployments", "deploymentCollectionsAreValid"),
				contextWithTestResult(true, "deployments", "deploymentResourcesEndpointIsValid"),
				contextWithTestResult(false, "part1apicommon", "collectionsExposeItemsLinks"),
				contextWithConfigurationResult(false, "common", "fetchCommonArguments"))) {
			assertThrows(SkipException.class, () -> SubdeploymentsTests.skipWhenPrerequisiteUnsatisfied(context));
		}
	}

	private static ITestContext contextWithTestResult(boolean skipped, String group, String methodName) {
		ITestContext context = emptyContext();
		IResultMap results = mock(IResultMap.class);
		ITestResult result = result(group, methodName);
		when(results.getAllResults()).thenReturn(Set.of(result));
		if (skipped) {
			when(context.getSkippedTests()).thenReturn(results);
		}
		else {
			when(context.getFailedTests()).thenReturn(results);
		}
		return context;
	}

	private static ITestContext contextWithConfigurationResult(boolean skipped, String group, String methodName) {
		ITestContext context = emptyContext();
		IResultMap results = mock(IResultMap.class);
		ITestResult result = result(group, methodName);
		when(results.getAllResults()).thenReturn(Set.of(result));
		if (skipped) {
			when(context.getSkippedConfigurations()).thenReturn(results);
		}
		else {
			when(context.getFailedConfigurations()).thenReturn(results);
		}
		return context;
	}

	private static ITestContext emptyContext() {
		ITestContext context = mock(ITestContext.class);
		IResultMap empty = mock(IResultMap.class);
		when(empty.getAllResults()).thenReturn(Collections.emptySet());
		when(context.getFailedTests()).thenReturn(empty);
		when(context.getSkippedTests()).thenReturn(empty);
		when(context.getFailedConfigurations()).thenReturn(empty);
		when(context.getSkippedConfigurations()).thenReturn(empty);
		return context;
	}

	private static ITestResult result(String group, String methodName) {
		ITestResult result = mock(ITestResult.class);
		ITestNGMethod method = mock(ITestNGMethod.class);
		when(result.getMethod()).thenReturn(method);
		when(method.getGroups()).thenReturn(new String[] { group });
		when(method.getMethodName()).thenReturn(methodName);
		return result;
	}

}
