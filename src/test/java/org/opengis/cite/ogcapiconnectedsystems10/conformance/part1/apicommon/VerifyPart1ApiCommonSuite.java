package org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.xml.Parser;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * Structural checks for REQ-ETS-PART1-001 dependency and deployment semantics.
 */
public class VerifyPart1ApiCommonSuite {

	private static final String SUITE = "/org/opengis/cite/ogcapiconnectedsystems10/testng.xml";

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-DEPENDENCY-CASCADE-001.
	 */
	@Test
	public void part1ApiCommonIsDeployedWithReleasedDependencyChain() throws Exception {
		XmlSuite suite = parseSuite();
		boolean deployed = false;
		boolean dependencies = false;
		for (XmlTest test : suite.getTests()) {
			deployed |= test.getXmlClasses()
				.stream()
				.anyMatch(xmlClass -> Part1ApiCommonTests.class.getName().equals(xmlClass.getName()));
			Map<String, String> groups = test.getXmlDependencyGroups();
			dependencies |= containsGroups(groups.get("part1apicommon"), "core", "common")
					&& containsGroups(groups.get("systemfeatures"), "part1apicommon");
		}
		assertTrue("Part1ApiCommonTests must be deployed by testng.xml", deployed);
		assertTrue("Expected core common -> part1apicommon -> systemfeatures dependency chain", dependencies);
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-DEPENDENCY-CASCADE-001.
	 */
	@Test
	public void apiCommonSetupWaitsForReleasedPrerequisites() throws Exception {
		Method setup = Part1ApiCommonTests.class.getDeclaredMethod("fetchApiCommonResources",
				org.testng.ITestContext.class);
		BeforeClass annotation = setup.getAnnotation(BeforeClass.class);

		assertTrue("fetchApiCommonResources must remain a TestNG BeforeClass configuration", annotation != null);
		assertTrue("API Common setup must wait for both Core and inherited OGC API Common",
				Arrays.asList(annotation.dependsOnGroups()).containsAll(List.of("core", "common")));
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-DEPENDENCY-CASCADE-001.
	 */
	@Test
	public void apiCommonSetupSkipsBeforeReadingIutAfterFailedPrerequisite() {
		ITestContext context = contextWithUnsatisfiedPrerequisite("core", false);

		assertThrows(SkipException.class, () -> new Part1ApiCommonTests().fetchApiCommonResources(context));
		verify(context, never()).getSuite();
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-DEPENDENCY-CASCADE-001.
	 */
	@Test
	public void apiCommonSetupSkipsBeforeReadingIutAfterSkippedPrerequisite() {
		ITestContext context = contextWithUnsatisfiedPrerequisite("common", true);

		assertThrows(SkipException.class, () -> new Part1ApiCommonTests().fetchApiCommonResources(context));
		verify(context, never()).getSuite();
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-DEPENDENCY-CASCADE-001.
	 */
	@Test
	public void everyPart1ApiCommonTestUsesItsGroupAndCanonicalTarget() {
		List<Method> methods = Arrays.stream(Part1ApiCommonTests.class.getDeclaredMethods())
			.filter(method -> method.getAnnotation(org.testng.annotations.Test.class) != null)
			.toList();

		assertEquals("Released API Common has exactly four class tests", 4, methods.size());
		for (Method method : methods) {
			org.testng.annotations.Test annotation = method.getAnnotation(org.testng.annotations.Test.class);
			assertTrue(method + " must use part1apicommon group",
					Arrays.asList(annotation.groups()).contains("part1apicommon"));
			assertTrue(method + " must carry the released canonical target",
					annotation.description().contains("http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/")
							&& (annotation.description().contains("/req/api-common/")
									|| annotation.description().contains("/rec/api-common/")));
		}
	}

	private static boolean containsGroups(String dependency, String... expected) {
		if (dependency == null) {
			return false;
		}
		List<String> actual = Arrays.asList(dependency.trim().split("\\s+"));
		return actual.containsAll(Arrays.asList(expected));
	}

	private static ITestContext contextWithUnsatisfiedPrerequisite(String group, boolean skipped) {
		ITestContext context = mock(ITestContext.class);
		IResultMap failedResults = mock(IResultMap.class);
		IResultMap skippedResults = mock(IResultMap.class);
		ITestResult result = mock(ITestResult.class);
		ITestNGMethod method = mock(ITestNGMethod.class);
		when(context.getFailedTests()).thenReturn(failedResults);
		when(context.getSkippedTests()).thenReturn(skippedResults);
		when(failedResults.getAllResults()).thenReturn(skipped ? Collections.emptySet() : Set.of(result));
		when(skippedResults.getAllResults()).thenReturn(skipped ? Set.of(result) : Collections.emptySet());
		when(result.getMethod()).thenReturn(method);
		when(method.getGroups()).thenReturn(new String[] { group });
		return context;
	}

	private static XmlSuite parseSuite() throws Exception {
		try (InputStream input = VerifyPart1ApiCommonSuite.class.getResourceAsStream(SUITE)) {
			assertTrue("Missing " + SUITE, input != null);
			List<XmlSuite> suites = new Parser(input).parseToList();
			assertEquals(1, suites.size());
			return suites.get(0);
		}
	}

}
