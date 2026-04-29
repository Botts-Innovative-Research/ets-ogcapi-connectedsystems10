package org.opengis.cite.ogcapiconnectedsystems10;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.testng.annotations.Test;
import org.testng.xml.Parser;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * Tests for REQ-ETS-CLEANUP-005 (live break-Core dependency-skip verification —
 * STRUCTURAL LINT half).
 *
 * <p>
 * Covers: SCENARIO-ETS-CLEANUP-DEPENDENCY-SKIP-LIVE-001 (CRITICAL) — STRUCTURAL
 * invariants only. Behavioral cascading-SKIP semantics are verified by the
 * {@code scripts/sabotage-test.sh} CITE- SC-grade end-to-end script per ADR-010
 * §"Approach (b) bash sabotage script". This unit test is the fast-feedback STRUCTURAL
 * LINT half of ADR-010's defense-in-depth role split (option (a): TestNG XmlSuite parser
 * unit test).
 *
 * <p>
 * Per ADR-010 §"Role boundary": this test asserts that the canonical {@code testng.xml}
 * shipped in the ETS jar declares the SystemFeatures→Core group dependency correctly AND
 * that every SystemFeatures @Test method carries {@code groups = "systemfeatures"} AND
 * every Core @Test method carries {@code groups = "core"}. Catches the regression
 * "someone deleted the &lt;group depends-on&gt; block during a refactor" or "someone
 * forgot {@code groups = "core"} on a new Core @Test" before the slow bash script runs in
 * CI.
 *
 * <p>
 * Pinned to {@code org.testng.xml.Parser} → {@link XmlSuite} API (TestNG 7.x; per
 * ets-common baseline). If TestNG migrates this API in a future major version, the
 * assertion failure messages below cite the package so the next migrator knows where to
 * look.
 *
 * @see <a href=
 * "../../../../../../../../../../../_bmad/adrs/ADR-010-dependency-skip-verification-strategy.md">ADR-010</a>
 */
public class VerifyTestNGSuiteDependency {

	private static final String TESTNG_XML_RESOURCE = "/org/opengis/cite/ogcapiconnectedsystems10/testng.xml";

	private static final String CORE_GROUP = "core";

	private static final String SYSTEMFEATURES_GROUP = "systemfeatures";

	/**
	 * Sprint 4 S-ETS-04-05 / ADR-010 v2 amendment — Subsystems group (FIRST two-level
	 * chain).
	 */
	private static final String SUBSYSTEMS_GROUP = "subsystems";

	/**
	 * Sprint 5 S-ETS-05-05 / ADR-010 v3 amendment — Procedures group (sibling of
	 * Subsystems; depends on SystemFeatures via the now-VERIFIED-LIVE TestNG 7.9.0
	 * transitive cascade).
	 */
	private static final String PROCEDURES_GROUP = "procedures";

	/**
	 * Sprint 5 S-ETS-05-06 / ADR-010 v3 amendment — Deployments group (sibling of
	 * Subsystems + Procedures; depends on SystemFeatures via the same cascade pattern).
	 */
	private static final String DEPLOYMENTS_GROUP = "deployments";

	private static final List<Class<?>> CORE_CLASSES = List.of(
			org.opengis.cite.ogcapiconnectedsystems10.conformance.core.LandingPageTests.class,
			org.opengis.cite.ogcapiconnectedsystems10.conformance.core.ConformanceTests.class,
			org.opengis.cite.ogcapiconnectedsystems10.conformance.core.ResourceShapeTests.class);

	private static final List<Class<?>> SYSTEMFEATURES_CLASSES = List
		.of(org.opengis.cite.ogcapiconnectedsystems10.conformance.systemfeatures.SystemFeaturesTests.class);

	/** Sprint 4 S-ETS-04-05 — Subsystems class set for structural assertions. */
	private static final List<Class<?>> SUBSYSTEMS_CLASSES = List
		.of(org.opengis.cite.ogcapiconnectedsystems10.conformance.subsystems.SubsystemsTests.class);

	/** Sprint 5 S-ETS-05-05 — Procedures class set for structural assertions. */
	private static final List<Class<?>> PROCEDURES_CLASSES = List
		.of(org.opengis.cite.ogcapiconnectedsystems10.conformance.procedures.ProceduresTests.class);

	/** Sprint 5 S-ETS-05-06 — Deployments class set for structural assertions. */
	private static final List<Class<?>> DEPLOYMENTS_CLASSES = List
		.of(org.opengis.cite.ogcapiconnectedsystems10.conformance.deployments.DeploymentsTests.class);

	private XmlSuite parseShippedSuite() throws Exception {
		try (InputStream in = VerifyTestNGSuiteDependency.class.getResourceAsStream(TESTNG_XML_RESOURCE)) {
			assertNotNull("Canonical testng.xml not on classpath at " + TESTNG_XML_RESOURCE
					+ " — Maven resource filtering may have changed; see ADR-001 SPI registration + ADR-010 §Role boundary",
					in);
			Parser parser = new Parser(in);
			parser.setLoadClasses(false);
			List<XmlSuite> suites = parser.parseToList();
			assertEquals("Expected exactly one <suite> in testng.xml", 1, suites.size());
			return suites.get(0);
		}
	}

	/**
	 * SCENARIO-ETS-CLEANUP-DEPENDENCY-SKIP-LIVE-001 (structural lint half): the canonical
	 * testng.xml SHALL declare the
	 * {@code <group name="systemfeatures" depends-on="core"/>} block inside a
	 * {@code <test>} that hosts BOTH Core and SystemFeatures classes.
	 *
	 * <p>
	 * Without this declaration, TestNG's group-dependency mechanism cannot resolve the
	 * SystemFeatures→Core dependency at runtime → SystemFeatures @Tests would FAIL/ERROR
	 * rather than SKIP when Core fails (the exact regression the bash sabotage script
	 * catches at the behavioral layer).
	 */
	@org.junit.Test
	public void testSystemFeaturesGroupDependsOnCore() throws Exception {
		XmlSuite suite = parseShippedSuite();
		assertFalse("Expected at least one <test> block in testng.xml", suite.getTests().isEmpty());

		boolean foundDependency = false;
		for (XmlTest xt : suite.getTests()) {
			// TestNG 7.x parser flattens <dependencies><group depends-on/> blocks into
			// XmlTest.getXmlDependencyGroups() — a Map<String,String> of group →
			// comma-sep depends-on groups. (XmlGroups.getDependencies() returns
			// List<XmlDependencies> but is NOT populated by the parser in this code
			// path; verified empirically against testng-7.9.0 — see ADR-010 §Risks
			// "TestNG XmlSuite parser API drift".)
			java.util.Map<String, String> deps = xt.getXmlDependencyGroups();
			if (deps != null && deps.containsKey(SYSTEMFEATURES_GROUP)) {
				String dependsOn = deps.get(SYSTEMFEATURES_GROUP);
				assertNotNull("group '" + SYSTEMFEATURES_GROUP + "' has null depends-on attribute", dependsOn);
				assertTrue("group '" + SYSTEMFEATURES_GROUP + "' depends-on '" + dependsOn + "' missing '" + CORE_GROUP
						+ "'", dependsOn.contains(CORE_GROUP));
				foundDependency = true;
				break;
			}
		}
		assertTrue(
				"testng.xml does not declare <group name=\"" + SYSTEMFEATURES_GROUP + "\" depends-on=\"" + CORE_GROUP
						+ "\"/> — see ADR-010 §Role boundary + design.md §SystemFeatures conformance class scope. "
						+ "If the declaration was deliberately moved/restructured, update this test in lockstep.",
				foundDependency);
	}

	/**
	 * SCENARIO-ETS-CLEANUP-DEPENDENCY-SKIP-LIVE-001 (structural lint half): the canonical
	 * testng.xml SHALL host BOTH Core and SystemFeatures classes in the SAME
	 * {@code <test>} block (TestNG group dependencies are {@code <test>}-scoped per
	 * TestNG-1.0.dtd semantics; if split across blocks, the dependency map fails to
	 * resolve and a "depends on nonexistent group" error fires).
	 */
	@org.junit.Test
	public void testCoreAndSystemFeaturesInSameTestBlock() throws Exception {
		XmlSuite suite = parseShippedSuite();
		Set<String> coreClassNames = new HashSet<>();
		for (Class<?> c : CORE_CLASSES) {
			coreClassNames.add(c.getName());
		}
		Set<String> systemFeaturesClassNames = new HashSet<>();
		for (Class<?> c : SYSTEMFEATURES_CLASSES) {
			systemFeaturesClassNames.add(c.getName());
		}

		boolean coAlloc = false;
		for (XmlTest xt : suite.getTests()) {
			Set<String> xtClasses = new HashSet<>();
			for (XmlClass xc : xt.getXmlClasses()) {
				xtClasses.add(xc.getName());
			}
			boolean hasAllCore = xtClasses.containsAll(coreClassNames);
			boolean hasAnySystemFeatures = !java.util.Collections.disjoint(xtClasses, systemFeaturesClassNames);
			if (hasAllCore && hasAnySystemFeatures) {
				coAlloc = true;
				break;
			}
		}
		assertTrue("Core (" + coreClassNames + ") and SystemFeatures (" + systemFeaturesClassNames
				+ ") must be declared in the SAME <test> block of testng.xml so TestNG group dependencies "
				+ "resolve within scope. See testng.xml inline comments + ADR-010.", coAlloc);
	}

	/**
	 * SCENARIO-ETS-CLEANUP-DEPENDENCY-SKIP-LIVE-001 (structural lint half): every
	 * Core @Test method SHALL carry {@code groups = "core"} so the
	 * {@code <group depends-on="core"/>} declaration in testng.xml has tagged methods to
	 * resolve against. A Core @Test missing {@code groups = "core"} would silently bypass
	 * the cascading-SKIP mechanism — invisible at the static testng.xml layer, only
	 * catchable here.
	 */
	@org.junit.Test
	public void testEveryCoreTestMethodCarriesCoreGroup() {
		List<String> offenders = new ArrayList<>();
		int totalCore = 0;
		for (Class<?> c : CORE_CLASSES) {
			for (Method m : c.getDeclaredMethods()) {
				Test ann = m.getAnnotation(Test.class);
				if (ann == null) {
					continue;
				}
				totalCore++;
				List<String> groups = java.util.Arrays.asList(ann.groups());
				if (!groups.contains(CORE_GROUP)) {
					offenders.add(c.getSimpleName() + "#" + m.getName() + " (groups=" + groups + ")");
				}
			}
		}
		assertTrue(
				"Expected at least one @Test method in Core conformance classes; found 0 — Maven resource scan failed",
				totalCore > 0);
		assertTrue("Core @Test methods missing groups=\"" + CORE_GROUP + "\": " + offenders, offenders.isEmpty());
	}

	/**
	 * SCENARIO-ETS-CLEANUP-DEPENDENCY-SKIP-LIVE-001 (structural lint half): every
	 * SystemFeatures
	 * @Test method SHALL carry {@code groups = "systemfeatures"} so the
	 * {@code <group name="systemfeatures" depends-on="core"/>} declaration tags those
	 * methods for the cascading-SKIP. A SystemFeatures @Test missing the group annotation
	 * would FAIL/ERROR directly rather than SKIP when Core fails — invisible at the
	 * testng.xml layer.
	 */
	@org.junit.Test
	public void testEverySystemFeaturesTestMethodCarriesSystemFeaturesGroup() {
		List<String> offenders = new ArrayList<>();
		int totalSf = 0;
		for (Class<?> c : SYSTEMFEATURES_CLASSES) {
			for (Method m : c.getDeclaredMethods()) {
				Test ann = m.getAnnotation(Test.class);
				if (ann == null) {
					continue;
				}
				totalSf++;
				List<String> groups = java.util.Arrays.asList(ann.groups());
				if (!groups.contains(SYSTEMFEATURES_GROUP)) {
					offenders.add(c.getSimpleName() + "#" + m.getName() + " (groups=" + groups + ")");
				}
			}
		}
		assertTrue("Expected at least one @Test method in SystemFeatures conformance classes; found 0", totalSf > 0);
		assertTrue("SystemFeatures @Test methods missing groups=\"" + SYSTEMFEATURES_GROUP + "\": " + offenders,
				offenders.isEmpty());
	}

	// ===== Sprint 4 S-ETS-04-05 / ADR-010 v2 amendment — Subsystems group =====
	// Two-level dependency chain (Subsystems → SystemFeatures → Core) structural lint.
	// Mirrors the SystemFeatures patterns above; ensures the structural-lint half of
	// ADR-010 defense-in-depth catches Subsystems-side regressions before the slow
	// bash sabotage script runs in CI.

	/**
	 * Sprint 4 S-ETS-04-05 (REQ-ETS-PART1-003): the canonical testng.xml SHALL declare
	 * {@code <group name="subsystems" depends-on="systemfeatures"/>} so the FIRST
	 * two-level dependency chain (Subsystems → SystemFeatures → Core) resolves at
	 * runtime. Without this declaration, Subsystems @Tests would FAIL/ERROR rather than
	 * SKIP when SystemFeatures (transitively, when Core) fails.
	 */
	@org.junit.Test
	public void testSubsystemsGroupDependsOnSystemFeatures() throws Exception {
		XmlSuite suite = parseShippedSuite();
		assertFalse("Expected at least one <test> block in testng.xml", suite.getTests().isEmpty());

		boolean foundDependency = false;
		for (XmlTest xt : suite.getTests()) {
			java.util.Map<String, String> deps = xt.getXmlDependencyGroups();
			if (deps != null && deps.containsKey(SUBSYSTEMS_GROUP)) {
				String dependsOn = deps.get(SUBSYSTEMS_GROUP);
				assertNotNull("group '" + SUBSYSTEMS_GROUP + "' has null depends-on attribute", dependsOn);
				assertTrue("group '" + SUBSYSTEMS_GROUP + "' depends-on '" + dependsOn + "' missing '"
						+ SYSTEMFEATURES_GROUP + "'", dependsOn.contains(SYSTEMFEATURES_GROUP));
				foundDependency = true;
				break;
			}
		}
		assertTrue("testng.xml does not declare <group name=\"" + SUBSYSTEMS_GROUP + "\" depends-on=\""
				+ SYSTEMFEATURES_GROUP + "\"/> — see ADR-010 v2 amendment + design.md §Sprint 4 hardening: "
				+ "Subsystems conformance class scope. The FIRST two-level dependency chain "
				+ "(Subsystems → SystemFeatures → Core) requires this declaration in addition to the "
				+ "Sprint 2 SystemFeatures → Core block.", foundDependency);
	}

	/**
	 * Sprint 4 S-ETS-04-05: every Subsystems @Test method SHALL carry
	 * {@code groups = "subsystems"} so the {@code <group name="subsystems"
	 * depends-on="systemfeatures"/>} declaration in testng.xml has tagged methods to
	 * resolve against. A Subsystems @Test missing the group annotation would FAIL/ERROR
	 * directly rather than cascade-SKIP — invisible at the testng.xml layer.
	 */
	@org.junit.Test
	public void testEverySubsystemsTestMethodCarriesSubsystemsGroup() {
		List<String> offenders = new ArrayList<>();
		int totalSubsystems = 0;
		for (Class<?> c : SUBSYSTEMS_CLASSES) {
			for (Method m : c.getDeclaredMethods()) {
				Test ann = m.getAnnotation(Test.class);
				if (ann == null) {
					continue;
				}
				totalSubsystems++;
				List<String> groups = java.util.Arrays.asList(ann.groups());
				if (!groups.contains(SUBSYSTEMS_GROUP)) {
					offenders.add(c.getSimpleName() + "#" + m.getName() + " (groups=" + groups + ")");
				}
			}
		}
		assertTrue("Expected at least one @Test method in Subsystems conformance classes; found 0",
				totalSubsystems > 0);
		assertTrue("Subsystems @Test methods missing groups=\"" + SUBSYSTEMS_GROUP + "\": " + offenders,
				offenders.isEmpty());
	}

	/**
	 * Sprint 4 S-ETS-04-05: Subsystems classes MUST be co-located in the SAME
	 * {@code <test>} block as Core + SystemFeatures so the two-level group-dependency
	 * cascade can resolve within scope (TestNG group dependencies are
	 * {@code <test>}-scoped per TestNG-1.0.dtd; if Subsystems were in a separate
	 * {@code <test>} block, the {@code depends-on="systemfeatures"} would fail with
	 * "depends on nonexistent group").
	 */
	@org.junit.Test
	public void testSubsystemsCoLocatedWithSystemFeatures() throws Exception {
		XmlSuite suite = parseShippedSuite();
		Set<String> systemFeaturesClassNames = new HashSet<>();
		for (Class<?> c : SYSTEMFEATURES_CLASSES) {
			systemFeaturesClassNames.add(c.getName());
		}
		Set<String> subsystemsClassNames = new HashSet<>();
		for (Class<?> c : SUBSYSTEMS_CLASSES) {
			subsystemsClassNames.add(c.getName());
		}

		boolean coAlloc = false;
		for (XmlTest xt : suite.getTests()) {
			Set<String> xtClasses = new HashSet<>();
			for (XmlClass xc : xt.getXmlClasses()) {
				xtClasses.add(xc.getName());
			}
			boolean hasAllSystemFeatures = xtClasses.containsAll(systemFeaturesClassNames);
			boolean hasAnySubsystems = !java.util.Collections.disjoint(xtClasses, subsystemsClassNames);
			if (hasAllSystemFeatures && hasAnySubsystems) {
				coAlloc = true;
				break;
			}
		}
		assertTrue(
				"SystemFeatures (" + systemFeaturesClassNames + ") and Subsystems (" + subsystemsClassNames
						+ ") must be declared in the SAME <test> block of testng.xml so the two-level group dependency "
						+ "(Subsystems → SystemFeatures → Core) resolves within scope. See ADR-010 v2 amendment.",
				coAlloc);
	}

	// ===== Sprint 5 S-ETS-05-05 / ADR-010 v3 amendment — Procedures group =====
	// Mirrors the Subsystems patterns above; Procedures is a SystemFeatures sibling.
	// Two-level cascade is now VERIFIED LIVE (Sprint 4 Raze sabotage exec); structural
	// lint catches refactor regressions before the slow bash sabotage script runs in CI.

	/**
	 * Sprint 5 S-ETS-05-05 (REQ-ETS-PART1-006): the canonical testng.xml SHALL declare
	 * {@code <group name="procedures" depends-on="systemfeatures"/>} so the Procedures
	 * conformance class participates in the two-level dependency cascade (Procedures →
	 * SystemFeatures → Core).
	 */
	@org.junit.Test
	public void testProceduresGroupDependsOnSystemFeatures() throws Exception {
		XmlSuite suite = parseShippedSuite();
		assertFalse("Expected at least one <test> block in testng.xml", suite.getTests().isEmpty());

		boolean foundDependency = false;
		for (XmlTest xt : suite.getTests()) {
			java.util.Map<String, String> deps = xt.getXmlDependencyGroups();
			if (deps != null && deps.containsKey(PROCEDURES_GROUP)) {
				String dependsOn = deps.get(PROCEDURES_GROUP);
				assertNotNull("group '" + PROCEDURES_GROUP + "' has null depends-on attribute", dependsOn);
				assertTrue("group '" + PROCEDURES_GROUP + "' depends-on '" + dependsOn + "' missing '"
						+ SYSTEMFEATURES_GROUP + "'", dependsOn.contains(SYSTEMFEATURES_GROUP));
				foundDependency = true;
				break;
			}
		}
		assertTrue("testng.xml does not declare <group name=\"" + PROCEDURES_GROUP + "\" depends-on=\""
				+ SYSTEMFEATURES_GROUP + "\"/> — see ADR-010 v3 amendment + Sprint 5 S-ETS-05-05. The Procedures "
				+ "conformance class requires this declaration to participate in the two-level dependency "
				+ "cascade (Procedures → SystemFeatures → Core).", foundDependency);
	}

	/**
	 * Sprint 5 S-ETS-05-05: every Procedures @Test method SHALL carry
	 * {@code groups = "procedures"} so the {@code <group name="procedures"
	 * depends-on="systemfeatures"/>} declaration in testng.xml has tagged methods to
	 * resolve against. A Procedures @Test missing the group annotation would FAIL/ERROR
	 * directly rather than cascade-SKIP.
	 */
	@org.junit.Test
	public void testEveryProceduresTestMethodCarriesProceduresGroup() {
		List<String> offenders = new ArrayList<>();
		int totalProcedures = 0;
		for (Class<?> c : PROCEDURES_CLASSES) {
			for (Method m : c.getDeclaredMethods()) {
				Test ann = m.getAnnotation(Test.class);
				if (ann == null) {
					continue;
				}
				totalProcedures++;
				List<String> groups = java.util.Arrays.asList(ann.groups());
				if (!groups.contains(PROCEDURES_GROUP)) {
					offenders.add(c.getSimpleName() + "#" + m.getName() + " (groups=" + groups + ")");
				}
			}
		}
		assertTrue("Expected at least one @Test method in Procedures conformance classes; found 0",
				totalProcedures > 0);
		assertTrue("Procedures @Test methods missing groups=\"" + PROCEDURES_GROUP + "\": " + offenders,
				offenders.isEmpty());
	}

	/**
	 * Sprint 5 S-ETS-05-05: Procedures classes MUST be co-located in the SAME
	 * {@code <test>} block as SystemFeatures so the two-level group-dependency cascade
	 * can resolve within scope (TestNG group dependencies are {@code <test>}-scoped per
	 * TestNG-1.0.dtd; if Procedures were in a separate block, the
	 * {@code depends-on="systemfeatures"} would fail with "depends on nonexistent
	 * group").
	 */
	@org.junit.Test
	public void testProceduresCoLocatedWithSystemFeatures() throws Exception {
		XmlSuite suite = parseShippedSuite();
		Set<String> systemFeaturesClassNames = new HashSet<>();
		for (Class<?> c : SYSTEMFEATURES_CLASSES) {
			systemFeaturesClassNames.add(c.getName());
		}
		Set<String> proceduresClassNames = new HashSet<>();
		for (Class<?> c : PROCEDURES_CLASSES) {
			proceduresClassNames.add(c.getName());
		}

		boolean coAlloc = false;
		for (XmlTest xt : suite.getTests()) {
			Set<String> xtClasses = new HashSet<>();
			for (XmlClass xc : xt.getXmlClasses()) {
				xtClasses.add(xc.getName());
			}
			boolean hasAllSystemFeatures = xtClasses.containsAll(systemFeaturesClassNames);
			boolean hasAnyProcedures = !java.util.Collections.disjoint(xtClasses, proceduresClassNames);
			if (hasAllSystemFeatures && hasAnyProcedures) {
				coAlloc = true;
				break;
			}
		}
		assertTrue(
				"SystemFeatures (" + systemFeaturesClassNames + ") and Procedures (" + proceduresClassNames
						+ ") must be declared in the SAME <test> block of testng.xml so the two-level group dependency "
						+ "(Procedures → SystemFeatures → Core) resolves within scope. See ADR-010 v3 amendment.",
				coAlloc);
	}

	// ===== Sprint 5 S-ETS-05-06 / ADR-010 v3 amendment — Deployments group =====
	// Mirrors the Procedures patterns above; Deployments is also a SystemFeatures
	// sibling.

	/**
	 * Sprint 5 S-ETS-05-06 (REQ-ETS-PART1-004): the canonical testng.xml SHALL declare
	 * {@code <group name="deployments" depends-on="systemfeatures"/>} so the Deployments
	 * conformance class participates in the two-level dependency cascade (Deployments →
	 * SystemFeatures → Core).
	 */
	@org.junit.Test
	public void testDeploymentsGroupDependsOnSystemFeatures() throws Exception {
		XmlSuite suite = parseShippedSuite();
		assertFalse("Expected at least one <test> block in testng.xml", suite.getTests().isEmpty());

		boolean foundDependency = false;
		for (XmlTest xt : suite.getTests()) {
			java.util.Map<String, String> deps = xt.getXmlDependencyGroups();
			if (deps != null && deps.containsKey(DEPLOYMENTS_GROUP)) {
				String dependsOn = deps.get(DEPLOYMENTS_GROUP);
				assertNotNull("group '" + DEPLOYMENTS_GROUP + "' has null depends-on attribute", dependsOn);
				assertTrue("group '" + DEPLOYMENTS_GROUP + "' depends-on '" + dependsOn + "' missing '"
						+ SYSTEMFEATURES_GROUP + "'", dependsOn.contains(SYSTEMFEATURES_GROUP));
				foundDependency = true;
				break;
			}
		}
		assertTrue(
				"testng.xml does not declare <group name=\"" + DEPLOYMENTS_GROUP + "\" depends-on=\""
						+ SYSTEMFEATURES_GROUP + "\"/> — see ADR-010 v3 amendment + Sprint 5 S-ETS-05-06.",
				foundDependency);
	}

	/**
	 * Sprint 5 S-ETS-05-06: every Deployments @Test method SHALL carry
	 * {@code groups = "deployments"} so the {@code <group name="deployments"
	 * depends-on="systemfeatures"/>} declaration in testng.xml has tagged methods to
	 * resolve against.
	 */
	@org.junit.Test
	public void testEveryDeploymentsTestMethodCarriesDeploymentsGroup() {
		List<String> offenders = new ArrayList<>();
		int totalDeployments = 0;
		for (Class<?> c : DEPLOYMENTS_CLASSES) {
			for (Method m : c.getDeclaredMethods()) {
				Test ann = m.getAnnotation(Test.class);
				if (ann == null) {
					continue;
				}
				totalDeployments++;
				List<String> groups = java.util.Arrays.asList(ann.groups());
				if (!groups.contains(DEPLOYMENTS_GROUP)) {
					offenders.add(c.getSimpleName() + "#" + m.getName() + " (groups=" + groups + ")");
				}
			}
		}
		assertTrue("Expected at least one @Test method in Deployments conformance classes; found 0",
				totalDeployments > 0);
		assertTrue("Deployments @Test methods missing groups=\"" + DEPLOYMENTS_GROUP + "\": " + offenders,
				offenders.isEmpty());
	}

	/**
	 * Sprint 5 S-ETS-05-06: Deployments classes MUST be co-located in the SAME
	 * {@code <test>} block as SystemFeatures so the two-level group-dependency cascade
	 * can resolve within scope.
	 */
	@org.junit.Test
	public void testDeploymentsCoLocatedWithSystemFeatures() throws Exception {
		XmlSuite suite = parseShippedSuite();
		Set<String> systemFeaturesClassNames = new HashSet<>();
		for (Class<?> c : SYSTEMFEATURES_CLASSES) {
			systemFeaturesClassNames.add(c.getName());
		}
		Set<String> deploymentsClassNames = new HashSet<>();
		for (Class<?> c : DEPLOYMENTS_CLASSES) {
			deploymentsClassNames.add(c.getName());
		}

		boolean coAlloc = false;
		for (XmlTest xt : suite.getTests()) {
			Set<String> xtClasses = new HashSet<>();
			for (XmlClass xc : xt.getXmlClasses()) {
				xtClasses.add(xc.getName());
			}
			boolean hasAllSystemFeatures = xtClasses.containsAll(systemFeaturesClassNames);
			boolean hasAnyDeployments = !java.util.Collections.disjoint(xtClasses, deploymentsClassNames);
			if (hasAllSystemFeatures && hasAnyDeployments) {
				coAlloc = true;
				break;
			}
		}
		assertTrue(
				"SystemFeatures (" + systemFeaturesClassNames + ") and Deployments (" + deploymentsClassNames
						+ ") must be declared in the SAME <test> block of testng.xml so the two-level group dependency "
						+ "(Deployments → SystemFeatures → Core) resolves within scope. See ADR-010 v3 amendment.",
				coAlloc);
	}

}
