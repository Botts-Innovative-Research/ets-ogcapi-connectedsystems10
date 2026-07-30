package org.opengis.cite.ogcapiconnectedsystems10.conformance.createreplacedelete;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.testng.ITestContext;

/**
 * Structural checks for the twelve released Create/Replace/Delete procedures.
 */
public class VerifyCreateReplaceDeleteReleasedSuite {

	private static final String REQ_BASE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/create-replace-delete/";

	private static final Set<String> TARGETS = Set.of(REQ_BASE + "system", REQ_BASE + "system-delete-cascade",
			REQ_BASE + "subsystem", REQ_BASE + "deployment", REQ_BASE + "subdeployment", REQ_BASE + "procedure",
			REQ_BASE + "sampling-feature", REQ_BASE + "property", REQ_BASE + "create-in-collection",
			REQ_BASE + "replace-in-collection", REQ_BASE + "delete-in-collection", REQ_BASE + "add-to-collection");

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-RELEASED-PROCEDURES-001.
	 */
	@Test
	public void classContainsExactlyTheTwelveReleasedProcedures() {
		List<Method> methods = Arrays.stream(CreateReplaceDeleteTests.class.getDeclaredMethods())
			.filter(method -> method.getAnnotation(org.testng.annotations.Test.class) != null)
			.toList();

		assertEquals(12, methods.size());
		assertEquals(12,
				methods.stream()
					.map(method -> method.getAnnotation(org.testng.annotations.Test.class).description())
					.flatMap(description -> TARGETS.stream().filter(description::endsWith))
					.distinct()
					.count());
		for (Method method : methods) {
			org.testng.annotations.Test annotation = method.getAnnotation(org.testng.annotations.Test.class);
			assertTrue(method + " must use createreplacedelete group",
					Arrays.asList(annotation.groups()).contains("createreplacedelete"));
			assertFalse(method + " must preserve the causal group prerequisite", annotation.alwaysRun());
			assertEquals(method + " must identify exactly one released target", 1,
					TARGETS.stream().filter(annotation.description()::endsWith).count());
			assertEquals(method + " must not depend on another Create/Replace/Delete method", 0,
					annotation.dependsOnMethods().length);
			assertEquals(method + " must depend directly on API Common", List.of("part1apicommon"),
					Arrays.asList(annotation.dependsOnGroups()));
		}
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-DIRECT-PREREQUISITES-001.
	 */
	@Test
	public void beforeClassLoadsOnlyImmutableArguments() throws Exception {
		Method setup = CreateReplaceDeleteTests.class.getDeclaredMethod("fetchCreateReplaceDeleteArguments",
				ITestContext.class);
		org.testng.annotations.BeforeClass annotation = setup.getAnnotation(org.testng.annotations.BeforeClass.class);

		assertNotNull(annotation);
		assertFalse(annotation.alwaysRun());
		assertTrue(Arrays.asList(annotation.dependsOnGroups()).contains("part1apicommon"));
		assertFalse(Arrays.stream(CreateReplaceDeleteTests.class.getDeclaredFields())
			.anyMatch(field -> field.getName().toLowerCase().contains("response")
					|| field.getName().toLowerCase().contains("body")));
	}

}
