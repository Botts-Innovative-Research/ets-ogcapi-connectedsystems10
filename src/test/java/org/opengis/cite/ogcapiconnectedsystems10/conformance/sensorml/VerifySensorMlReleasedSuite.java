package org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml;

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
 * Structural checks for the fifteen released SensorML procedures.
 */
public class VerifySensorMlReleasedSuite {

	private static final String BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/sensorml/";

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-PROCEDURES-001.
	 */
	@Test
	public void sensorMlClassContainsExactlyTheFifteenReleasedProcedures() {
		List<Method> methods = Arrays.stream(SensorMlTests.class.getDeclaredMethods())
			.filter(method -> method.getAnnotation(org.testng.annotations.Test.class) != null)
			.toList();
		Set<String> targets = Set.of(BASE + "mediatype-read", BASE + "mediatype-write", BASE + "relation-types",
				BASE + "resource-id", BASE + "feature-attribute-mapping", BASE + "system-schema",
				BASE + "system-sml-class", BASE + "system-mappings", BASE + "deployment-schema",
				BASE + "deployment-mappings", BASE + "procedure-schema", BASE + "procedure-sml-class",
				BASE + "procedure-mappings", BASE + "property-schema", BASE + "property-mappings");

		assertEquals(15, methods.size());
		assertEquals(15,
				methods.stream()
					.map(method -> method.getAnnotation(org.testng.annotations.Test.class).description())
					.flatMap(description -> targets.stream().filter(description::contains))
					.distinct()
					.count());
		for (Method method : methods) {
			org.testng.annotations.Test annotation = method.getAnnotation(org.testng.annotations.Test.class);
			assertTrue(method + " must use sensorml group",
					Arrays.asList(annotation.groups()).contains(SensorMlTests.GROUP));
			assertTrue(method + " must remain independently executable", annotation.alwaysRun());
			assertEquals(method + " must identify exactly one released target", 1,
					targets.stream().filter(annotation.description()::contains).count());
			assertEquals(method + " must not depend on another SensorML method", 0,
					annotation.dependsOnMethods().length);
		}
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-PROCEDURE-ISOLATION-001.
	 */
	@Test
	public void beforeClassLoadsOnlyImmutableArguments() throws Exception {
		Method setup = SensorMlTests.class.getDeclaredMethod("fetchSensorMlArguments", ITestContext.class);
		org.testng.annotations.BeforeClass annotation = setup.getAnnotation(org.testng.annotations.BeforeClass.class);

		assertTrue(annotation != null);
		assertTrue(annotation.alwaysRun());
		assertTrue(Arrays.asList(annotation.dependsOnGroups()).contains("part1apicommon"));
		assertFalse(Arrays.stream(SensorMlTests.class.getDeclaredFields())
			.anyMatch(field -> field.getName().toLowerCase().contains("response")
					|| field.getName().toLowerCase().contains("body")));
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-DIRECT-PREREQUISITES-001.
	 */
	@Test
	public void systemFeaturesConfigurationDoesNotBlockSensorMl() throws Exception {
		invokePrerequisiteGate(
				contextWithConfiguration("systemfeatures", SystemFeaturesTests.class, "fetchSystemArguments"));
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-DIRECT-PREREQUISITES-001.
	 */
	@Test
	public void apiCommonConfigurationBlocksSensorMl() {
		ITestContext context = contextWithConfiguration("part1apicommon", Part1ApiCommonTests.class,
				"fetchApiCommonArguments");

		assertThrows(SkipException.class, () -> invokePrerequisiteGate(context));
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-DIRECT-PREREQUISITES-001.
	 */
	@Test
	public void documentedDatetimeEvidenceLimitationDoesNotBlockSensorMl() throws Exception {
		invokePrerequisiteGate(contextWithSkippedTest("part1apicommon", "datetimeUsesValidTime",
				new SkipException(Part1ApiCommonTests.DATETIME_EVIDENCE_LIMITATION)));
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-DIRECT-PREREQUISITES-001.
	 */
	@Test
	public void unexpectedApiCommonSkipBlocksSensorMl() {
		ITestContext context = contextWithSkippedTest("part1apicommon", "canonicalResourcesHaveUid",
				new SkipException("unexpected API Common evidence gap"));

		assertThrows(SkipException.class, () -> invokePrerequisiteGate(context));
	}

	private static void invokePrerequisiteGate(ITestContext context) throws Exception {
		Method method = SensorMlTests.class.getDeclaredMethod("skipWhenPrerequisiteUnsatisfied", ITestContext.class);
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
