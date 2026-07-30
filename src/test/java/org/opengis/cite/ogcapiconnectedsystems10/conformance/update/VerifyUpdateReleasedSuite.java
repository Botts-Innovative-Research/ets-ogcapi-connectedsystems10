package org.opengis.cite.ogcapiconnectedsystems10.conformance.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;
import org.testng.annotations.BeforeClass;

/**
 * Structural regressions for the released Part 1 Update suite.
 */
public class VerifyUpdateReleasedSuite {

	private static final String REQ_BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/update/";

	private static final Map<String, String> RELEASED = releasedMethods();

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-RELEASED-PROCEDURES-001.
	 */
	@Test
	public void exactlyFiveMethodsMapReleasedProcedures() {
		List<Method> tests = Arrays.stream(UpdateTests.class.getDeclaredMethods())
			.filter(method -> method.isAnnotationPresent(org.testng.annotations.Test.class))
			.toList();

		assertEquals(RELEASED.keySet(), tests.stream().map(Method::getName).collect(Collectors.toSet()));
		assertEquals(5, tests.size());
		for (Method method : tests) {
			org.testng.annotations.Test annotation = method.getAnnotation(org.testng.annotations.Test.class);
			assertTrue(method.getName(), annotation.description().contains(REQ_BASE + RELEASED.get(method.getName())));
			assertEquals(method.getName(), List.of("update"), List.of(annotation.groups()));
			assertEquals(method.getName(), List.of("part1apicommon"), List.of(annotation.dependsOnGroups()));
			assertFalse(method.getName(), annotation.alwaysRun());
			assertEquals(method.getName(), 0, annotation.dependsOnMethods().length);
		}
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-DEPENDENCY-CAUSAL-001.
	 */
	@Test
	public void setupIsImmutableAndCausallyDependsOnlyOnApiCommon() throws Exception {
		Method setup = UpdateTests.class.getDeclaredMethod("fetchUpdateArguments", org.testng.ITestContext.class);
		BeforeClass annotation = setup.getAnnotation(BeforeClass.class);

		assertEquals(List.of("part1apicommon"), List.of(annotation.dependsOnGroups()));
		assertFalse(annotation.alwaysRun());
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-DIRECT-PREREQUISITES-001.
	 */
	@Test
	public void exactReleasedInheritanceConstantsRemainDistinct() {
		assertEquals("http://www.opengis.net/spec/ogcapi-4/1.0/conf/update", UpdateSupport.CONF_INHERITED_UPDATE);
		assertFalse(UpdateSupport.CONF_INHERITED_UPDATE.contains("ogcapi-features-4"));
	}

	private static Map<String, String> releasedMethods() {
		Map<String, String> result = new LinkedHashMap<>();
		result.put("systemsUpdate", "system");
		result.put("deploymentsUpdate", "deployment");
		result.put("proceduresUpdate", "procedure");
		result.put("samplingFeaturesUpdate", "sampling-feature");
		result.put("propertiesUpdate", "property");
		return Map.copyOf(result);
	}

}
