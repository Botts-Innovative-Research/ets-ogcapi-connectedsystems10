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

	private static final String COMMON_GROUP = "common";

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

	/**
	 * Sprint 7 S-ETS-07-02 — SamplingFeatures group (sibling of Subsystems + Procedures +
	 * Deployments; depends on SystemFeatures via the now-VERIFIED-LIVE 3-class cascade —
	 * Sprint 7 S-ETS-07-01 Wedge 1 closed the sabotage-marker javac defect that had
	 * blocked live verification for 2 sprints).
	 */
	private static final String SAMPLINGFEATURES_GROUP = "samplingfeatures";

	/**
	 * Sprint 7 S-ETS-07-03 — PropertyDefinitions group (sibling of all the above; depends
	 * on SystemFeatures).
	 */
	private static final String PROPERTYDEFINITIONS_GROUP = "propertydefinitions";

	/**
	 * Sprint 8 S-ETS-08-02 — Subdeployments group (FIRST three-deep dependency chain in
	 * this ETS: Subdeployments → Deployments → SystemFeatures → Core). depends-on the
	 * {@code deployments} group (NOT {@code systemfeatures} directly); the SystemFeatures
	 * dependency is transitive through Deployments per TestNG 7.9.0 cascade semantics.
	 * ADR-010 v4 amendment (Sprint 8 — Generator close, 2026-04-30) records the 3-deep
	 * chain wiring; sister cascade XML
	 * {@code ops/test-results/sprint-ets-08-cascade-2026-04-30.xml} verifies the cascade
	 * end-to-end (6 sibling classes SKIP when SystemFeatures is sabotaged: 5
	 * SystemFeatures-level direct + 1 Subdeployments transitive via Deployments).
	 */
	private static final String SUBDEPLOYMENTS_GROUP = "subdeployments";

	/**
	 * Sprint 9 S-ETS-09-01 — GeoJSON systems read-only subset group (depends on
	 * SystemFeatures).
	 */
	private static final String GEOJSON_GROUP = "geojson";

	/**
	 * Sprint 10 S-ETS-10-01 — SensorML systems read-only subset group (depends on
	 * SystemFeatures).
	 */
	private static final String SENSORML_GROUP = "sensorml";

	/**
	 * Sprint 11 S-ETS-11-01 — AdvancedFiltering systems/common-resource read-only subset
	 * group (depends on SystemFeatures).
	 */
	private static final String ADVANCEDFILTERING_GROUP = "advancedfiltering";

	/**
	 * Sprint 12 S-ETS-12-01 — CreateReplaceDelete systems safety-gated subset group
	 * (depends on SystemFeatures).
	 */
	private static final String CREATE_REPLACE_DELETE_GROUP = "createreplacedelete";

	/**
	 * Sprint 13 S-ETS-13-01 — Update/PATCH systems safety-gated subset group (depends on
	 * CreateReplaceDelete).
	 */
	private static final String UPDATE_GROUP = "update";

	/**
	 * Sprint 20 S-ETS-20-01 — Part 2 API Common read-only subset group (depends on Core
	 * and Common).
	 */
	private static final String PART2_API_COMMON_GROUP = "part2apicommon";

	/**
	 * Sprint 21 S-ETS-21-01 — Part 2 Datastream read-only subset group (depends on Core
	 * and Common for scoped endpoint checks; full /conf/datastream closure remains
	 * blocked when /conf/api-common is absent).
	 */
	private static final String PART2_DATASTREAM_GROUP = "part2datastream";

	/**
	 * Sprint 22 S-ETS-22-01 — Part 2 ControlStream read-only subset group (depends on
	 * Core and Common for scoped endpoint checks; full /conf/controlstream closure
	 * remains blocked when /conf/api-common is absent).
	 */
	private static final String PART2_CONTROLSTREAM_GROUP = "part2controlstream";

	/**
	 * Sprint 23 S-ETS-23-01 — Part 2 Feasibility safety-gated subset group (depends on
	 * Core and Common; runtime checks keep /req/controlstream prerequisite honesty
	 * visible and default smoke performs no feasibility POST).
	 */
	private static final String PART2_FEASIBILITY_GROUP = "part2feasibility";

	private static final List<Class<?>> CORE_CLASSES = List.of(
			org.opengis.cite.ogcapiconnectedsystems10.conformance.core.LandingPageTests.class,
			org.opengis.cite.ogcapiconnectedsystems10.conformance.core.ConformanceTests.class,
			org.opengis.cite.ogcapiconnectedsystems10.conformance.core.ResourceShapeTests.class);

	private static final List<Class<?>> COMMON_CLASSES = List
		.of(org.opengis.cite.ogcapiconnectedsystems10.conformance.common.CommonTests.class);

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

	/** Sprint 7 S-ETS-07-02 — SamplingFeatures class set for structural assertions. */
	private static final List<Class<?>> SAMPLINGFEATURES_CLASSES = List
		.of(org.opengis.cite.ogcapiconnectedsystems10.conformance.samplingfeatures.SamplingFeaturesTests.class);

	/** Sprint 7 S-ETS-07-03 — PropertyDefinitions class set for structural assertions. */
	private static final List<Class<?>> PROPERTYDEFINITIONS_CLASSES = List
		.of(org.opengis.cite.ogcapiconnectedsystems10.conformance.propertydefinitions.PropertyDefinitionsTests.class);

	/** Sprint 8 S-ETS-08-02 — Subdeployments class set for structural assertions. */
	private static final List<Class<?>> SUBDEPLOYMENTS_CLASSES = List
		.of(org.opengis.cite.ogcapiconnectedsystems10.conformance.subdeployments.SubdeploymentsTests.class);

	/** Sprint 9 S-ETS-09-01 — GeoJSON class set for structural assertions. */
	private static final List<Class<?>> GEOJSON_CLASSES = List
		.of(org.opengis.cite.ogcapiconnectedsystems10.conformance.geojson.GeoJsonTests.class);

	/** Sprint 10 S-ETS-10-01 — SensorML class set for structural assertions. */
	private static final List<Class<?>> SENSORML_CLASSES = List
		.of(org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml.SensorMlTests.class);

	/** Sprint 11 S-ETS-11-01 — AdvancedFiltering class set for structural assertions. */
	private static final List<Class<?>> ADVANCEDFILTERING_CLASSES = List
		.of(org.opengis.cite.ogcapiconnectedsystems10.conformance.advancedfiltering.AdvancedFilteringTests.class);

	/**
	 * Sprint 12 S-ETS-12-01 — CreateReplaceDelete class set for structural assertions.
	 */
	private static final List<Class<?>> CREATE_REPLACE_DELETE_CLASSES = List
		.of(org.opengis.cite.ogcapiconnectedsystems10.conformance.createreplacedelete.CreateReplaceDeleteTests.class);

	/** Sprint 13 S-ETS-13-01 — Update class set for structural assertions. */
	private static final List<Class<?>> UPDATE_CLASSES = List
		.of(org.opengis.cite.ogcapiconnectedsystems10.conformance.update.UpdateTests.class);

	/** Sprint 20 S-ETS-20-01 — Part 2 API Common class set for structural assertions. */
	private static final List<Class<?>> PART2_API_COMMON_CLASSES = List
		.of(org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.apicommon.Part2ApiCommonTests.class);

	/** Sprint 21 S-ETS-21-01 — Part 2 Datastream class set for structural assertions. */
	private static final List<Class<?>> PART2_DATASTREAM_CLASSES = List
		.of(org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.datastream.Part2DatastreamTests.class);

	/**
	 * Sprint 22 S-ETS-22-01 — Part 2 ControlStream class set for structural assertions.
	 */
	private static final List<Class<?>> PART2_CONTROLSTREAM_CLASSES = List
		.of(org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.controlstream.Part2ControlStreamTests.class);

	private static final List<Class<?>> PART2_FEASIBILITY_CLASSES = List
		.of(org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.feasibility.Part2FeasibilityTests.class);

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

	// ===== Sprint 7 S-ETS-07-02 — SamplingFeatures group =====
	// Mirrors the Deployments patterns above; SamplingFeatures is also a SystemFeatures
	// sibling. The 3-class cascade (Subsystems + Procedures + Deployments) was VERIFIED
	// LIVE by Sprint 7 S-ETS-07-01 Wedge 1; SamplingFeatures + PropertyDefinitions extend
	// the cascade to 5 sibling classes at the SystemFeatures level.

	/**
	 * Sprint 7 S-ETS-07-02 (REQ-ETS-PART1-007): the canonical testng.xml SHALL declare
	 * {@code <group name="samplingfeatures" depends-on="systemfeatures"/>} so the
	 * SamplingFeatures conformance class participates in the two-level dependency cascade
	 * (SamplingFeatures → SystemFeatures → Core).
	 */
	@org.junit.Test
	public void testSamplingFeaturesGroupDependsOnSystemFeatures() throws Exception {
		XmlSuite suite = parseShippedSuite();
		assertFalse("Expected at least one <test> block in testng.xml", suite.getTests().isEmpty());

		boolean foundDependency = false;
		for (XmlTest xt : suite.getTests()) {
			java.util.Map<String, String> deps = xt.getXmlDependencyGroups();
			if (deps != null && deps.containsKey(SAMPLINGFEATURES_GROUP)) {
				String dependsOn = deps.get(SAMPLINGFEATURES_GROUP);
				assertNotNull("group '" + SAMPLINGFEATURES_GROUP + "' has null depends-on attribute", dependsOn);
				assertTrue("group '" + SAMPLINGFEATURES_GROUP + "' depends-on '" + dependsOn + "' missing '"
						+ SYSTEMFEATURES_GROUP + "'", dependsOn.contains(SYSTEMFEATURES_GROUP));
				foundDependency = true;
				break;
			}
		}
		assertTrue(
				"testng.xml does not declare <group name=\"" + SAMPLINGFEATURES_GROUP + "\" depends-on=\""
						+ SYSTEMFEATURES_GROUP + "\"/> — see Sprint 7 S-ETS-07-02 + ADR-010 v3 amendment.",
				foundDependency);
	}

	/**
	 * Sprint 7 S-ETS-07-02: every SamplingFeatures @Test method SHALL carry
	 * {@code groups = "samplingfeatures"} so the
	 * {@code <group name="samplingfeatures" depends-on="systemfeatures"/>} declaration
	 * has tagged methods to resolve against.
	 */
	@org.junit.Test
	public void testEverySamplingFeaturesTestMethodCarriesSamplingFeaturesGroup() {
		List<String> offenders = new ArrayList<>();
		int totalSamplingFeatures = 0;
		for (Class<?> c : SAMPLINGFEATURES_CLASSES) {
			for (Method m : c.getDeclaredMethods()) {
				Test ann = m.getAnnotation(Test.class);
				if (ann == null) {
					continue;
				}
				totalSamplingFeatures++;
				List<String> groups = java.util.Arrays.asList(ann.groups());
				if (!groups.contains(SAMPLINGFEATURES_GROUP)) {
					offenders.add(c.getSimpleName() + "#" + m.getName() + " (groups=" + groups + ")");
				}
			}
		}
		assertTrue("Expected at least one @Test method in SamplingFeatures conformance classes; found 0",
				totalSamplingFeatures > 0);
		assertTrue("SamplingFeatures @Test methods missing groups=\"" + SAMPLINGFEATURES_GROUP + "\": " + offenders,
				offenders.isEmpty());
	}

	/**
	 * Sprint 7 S-ETS-07-02: SamplingFeatures classes MUST be co-located in the SAME
	 * {@code <test>} block as SystemFeatures so the two-level group-dependency cascade
	 * can resolve within scope.
	 */
	@org.junit.Test
	public void testSamplingFeaturesCoLocatedWithSystemFeatures() throws Exception {
		XmlSuite suite = parseShippedSuite();
		Set<String> systemFeaturesClassNames = new HashSet<>();
		for (Class<?> c : SYSTEMFEATURES_CLASSES) {
			systemFeaturesClassNames.add(c.getName());
		}
		Set<String> samplingFeaturesClassNames = new HashSet<>();
		for (Class<?> c : SAMPLINGFEATURES_CLASSES) {
			samplingFeaturesClassNames.add(c.getName());
		}

		boolean coAlloc = false;
		for (XmlTest xt : suite.getTests()) {
			Set<String> xtClasses = new HashSet<>();
			for (XmlClass xc : xt.getXmlClasses()) {
				xtClasses.add(xc.getName());
			}
			boolean hasAllSystemFeatures = xtClasses.containsAll(systemFeaturesClassNames);
			boolean hasAnySamplingFeatures = !java.util.Collections.disjoint(xtClasses, samplingFeaturesClassNames);
			if (hasAllSystemFeatures && hasAnySamplingFeatures) {
				coAlloc = true;
				break;
			}
		}
		assertTrue(
				"SystemFeatures (" + systemFeaturesClassNames + ") and SamplingFeatures (" + samplingFeaturesClassNames
						+ ") must be declared in the SAME <test> block of testng.xml so the two-level group dependency "
						+ "(SamplingFeatures → SystemFeatures → Core) resolves within scope. See Sprint 7 S-ETS-07-02.",
				coAlloc);
	}

	// ===== Sprint 7 S-ETS-07-03 — PropertyDefinitions group =====
	// Mirrors the SamplingFeatures patterns above.

	/**
	 * Sprint 7 S-ETS-07-03 (REQ-ETS-PART1-008): the canonical testng.xml SHALL declare
	 * {@code <group name="propertydefinitions" depends-on="systemfeatures"/>} so the
	 * PropertyDefinitions conformance class participates in the two-level dependency
	 * cascade (PropertyDefinitions → SystemFeatures → Core).
	 */
	@org.junit.Test
	public void testPropertyDefinitionsGroupDependsOnSystemFeatures() throws Exception {
		XmlSuite suite = parseShippedSuite();
		assertFalse("Expected at least one <test> block in testng.xml", suite.getTests().isEmpty());

		boolean foundDependency = false;
		for (XmlTest xt : suite.getTests()) {
			java.util.Map<String, String> deps = xt.getXmlDependencyGroups();
			if (deps != null && deps.containsKey(PROPERTYDEFINITIONS_GROUP)) {
				String dependsOn = deps.get(PROPERTYDEFINITIONS_GROUP);
				assertNotNull("group '" + PROPERTYDEFINITIONS_GROUP + "' has null depends-on attribute", dependsOn);
				assertTrue("group '" + PROPERTYDEFINITIONS_GROUP + "' depends-on '" + dependsOn + "' missing '"
						+ SYSTEMFEATURES_GROUP + "'", dependsOn.contains(SYSTEMFEATURES_GROUP));
				foundDependency = true;
				break;
			}
		}
		assertTrue(
				"testng.xml does not declare <group name=\"" + PROPERTYDEFINITIONS_GROUP + "\" depends-on=\""
						+ SYSTEMFEATURES_GROUP + "\"/> — see Sprint 7 S-ETS-07-03 + ADR-010 v3 amendment.",
				foundDependency);
	}

	/**
	 * Sprint 7 S-ETS-07-03: every PropertyDefinitions @Test method SHALL carry
	 * {@code groups = "propertydefinitions"} so the
	 * {@code <group name="propertydefinitions" depends-on="systemfeatures"/>} declaration
	 * has tagged methods to resolve against.
	 */
	@org.junit.Test
	public void testEveryPropertyDefinitionsTestMethodCarriesPropertyDefinitionsGroup() {
		List<String> offenders = new ArrayList<>();
		int totalPropertyDefinitions = 0;
		for (Class<?> c : PROPERTYDEFINITIONS_CLASSES) {
			for (Method m : c.getDeclaredMethods()) {
				Test ann = m.getAnnotation(Test.class);
				if (ann == null) {
					continue;
				}
				totalPropertyDefinitions++;
				List<String> groups = java.util.Arrays.asList(ann.groups());
				if (!groups.contains(PROPERTYDEFINITIONS_GROUP)) {
					offenders.add(c.getSimpleName() + "#" + m.getName() + " (groups=" + groups + ")");
				}
			}
		}
		assertTrue("Expected at least one @Test method in PropertyDefinitions conformance classes; found 0",
				totalPropertyDefinitions > 0);
		assertTrue(
				"PropertyDefinitions @Test methods missing groups=\"" + PROPERTYDEFINITIONS_GROUP + "\": " + offenders,
				offenders.isEmpty());
	}

	/**
	 * Sprint 7 S-ETS-07-03: PropertyDefinitions classes MUST be co-located in the SAME
	 * {@code <test>} block as SystemFeatures so the two-level group-dependency cascade
	 * can resolve within scope.
	 */
	@org.junit.Test
	public void testPropertyDefinitionsCoLocatedWithSystemFeatures() throws Exception {
		XmlSuite suite = parseShippedSuite();
		Set<String> systemFeaturesClassNames = new HashSet<>();
		for (Class<?> c : SYSTEMFEATURES_CLASSES) {
			systemFeaturesClassNames.add(c.getName());
		}
		Set<String> propertyDefinitionsClassNames = new HashSet<>();
		for (Class<?> c : PROPERTYDEFINITIONS_CLASSES) {
			propertyDefinitionsClassNames.add(c.getName());
		}

		boolean coAlloc = false;
		for (XmlTest xt : suite.getTests()) {
			Set<String> xtClasses = new HashSet<>();
			for (XmlClass xc : xt.getXmlClasses()) {
				xtClasses.add(xc.getName());
			}
			boolean hasAllSystemFeatures = xtClasses.containsAll(systemFeaturesClassNames);
			boolean hasAnyPropertyDefinitions = !java.util.Collections.disjoint(xtClasses,
					propertyDefinitionsClassNames);
			if (hasAllSystemFeatures && hasAnyPropertyDefinitions) {
				coAlloc = true;
				break;
			}
		}
		assertTrue("SystemFeatures (" + systemFeaturesClassNames + ") and PropertyDefinitions ("
				+ propertyDefinitionsClassNames
				+ ") must be declared in the SAME <test> block of testng.xml so the two-level group dependency "
				+ "(PropertyDefinitions → SystemFeatures → Core) resolves within scope. See Sprint 7 S-ETS-07-03.",
				coAlloc);
	}

	// ===== Sprint 8 S-ETS-08-02 — Subdeployments group =====
	// Mirrors the patterns above; Subdeployments is the FIRST three-deep dependency chain
	// in this ETS (Subdeployments → Deployments → SystemFeatures → Core). Critical
	// distinction: Subdeployments depends-on="deployments" (NOT "systemfeatures") — the
	// transitive cascade is carried by Deployments' own depends-on="systemfeatures".

	/**
	 * Sprint 8 S-ETS-08-02 (REQ-ETS-PART1-005): the canonical testng.xml SHALL declare
	 * {@code <group name="subdeployments" depends-on="deployments"/>} so the
	 * Subdeployments conformance class participates in the THREE-deep dependency cascade
	 * (Subdeployments → Deployments → SystemFeatures → Core). Critical: depends-on must
	 * be {@code "deployments"} (NOT {@code "systemfeatures"} directly) — the
	 * SystemFeatures dependency is transitive through Deployments.
	 */
	@org.junit.Test
	public void testSubdeploymentsGroupDependsOnDeployments() throws Exception {
		XmlSuite suite = parseShippedSuite();
		assertFalse("Expected at least one <test> block in testng.xml", suite.getTests().isEmpty());

		boolean foundDependency = false;
		for (XmlTest xt : suite.getTests()) {
			java.util.Map<String, String> deps = xt.getXmlDependencyGroups();
			if (deps != null && deps.containsKey(SUBDEPLOYMENTS_GROUP)) {
				String dependsOn = deps.get(SUBDEPLOYMENTS_GROUP);
				assertNotNull("group '" + SUBDEPLOYMENTS_GROUP + "' has null depends-on attribute", dependsOn);
				assertTrue("group '" + SUBDEPLOYMENTS_GROUP + "' depends-on '" + dependsOn + "' missing '"
						+ DEPLOYMENTS_GROUP
						+ "' (3-deep chain Subdeployments → Deployments → SystemFeatures → Core requires Deployments as direct parent, NOT SystemFeatures)",
						dependsOn.contains(DEPLOYMENTS_GROUP));
				foundDependency = true;
				break;
			}
		}
		assertTrue("testng.xml does not declare <group name=\"" + SUBDEPLOYMENTS_GROUP + "\" depends-on=\""
				+ DEPLOYMENTS_GROUP + "\"/> — see Sprint 8 S-ETS-08-02 + ADR-010 v4 amendment. The "
				+ "Subdeployments conformance class requires this declaration to participate in the three-level "
				+ "dependency cascade (Subdeployments → Deployments → SystemFeatures → Core).", foundDependency);
	}

	/**
	 * Sprint 8 S-ETS-08-02: every Subdeployments @Test method SHALL carry
	 * {@code groups = "subdeployments"} so the {@code <group name="subdeployments"
	 * depends-on="deployments"/>} declaration in testng.xml has tagged methods to resolve
	 * against. A Subdeployments @Test missing the group annotation would FAIL/ERROR
	 * directly rather than cascade-SKIP via the 3-deep chain.
	 */
	@org.junit.Test
	public void testEverySubdeploymentsTestMethodCarriesSubdeploymentsGroup() {
		List<String> offenders = new ArrayList<>();
		int totalSubdeployments = 0;
		for (Class<?> c : SUBDEPLOYMENTS_CLASSES) {
			for (Method m : c.getDeclaredMethods()) {
				Test ann = m.getAnnotation(Test.class);
				if (ann == null) {
					continue;
				}
				totalSubdeployments++;
				List<String> groups = java.util.Arrays.asList(ann.groups());
				if (!groups.contains(SUBDEPLOYMENTS_GROUP)) {
					offenders.add(c.getSimpleName() + "#" + m.getName() + " (groups=" + groups + ")");
				}
			}
		}
		assertTrue("Expected at least one @Test method in Subdeployments conformance classes; found 0",
				totalSubdeployments > 0);
		assertTrue("Subdeployments @Test methods missing groups=\"" + SUBDEPLOYMENTS_GROUP + "\": " + offenders,
				offenders.isEmpty());
	}

	/**
	 * Sprint 8 S-ETS-08-02: Subdeployments classes MUST be co-located in the SAME
	 * {@code <test>} block as Deployments so the three-level group-dependency cascade can
	 * resolve within scope. Stronger condition than the SystemFeatures co-location lint:
	 * the 3-deep chain requires the parent (Deployments) to be in the same block, which
	 * ALSO transitively requires Deployments' own parent (SystemFeatures) to be in the
	 * same block (already covered by
	 * {@link #testDeploymentsCoLocatedWithSystemFeatures}).
	 */
	@org.junit.Test
	public void testSubdeploymentsCoLocatedWithDeployments() throws Exception {
		XmlSuite suite = parseShippedSuite();
		Set<String> deploymentsClassNames = new HashSet<>();
		for (Class<?> c : DEPLOYMENTS_CLASSES) {
			deploymentsClassNames.add(c.getName());
		}
		Set<String> subdeploymentsClassNames = new HashSet<>();
		for (Class<?> c : SUBDEPLOYMENTS_CLASSES) {
			subdeploymentsClassNames.add(c.getName());
		}

		boolean coAlloc = false;
		for (XmlTest xt : suite.getTests()) {
			Set<String> xtClasses = new HashSet<>();
			for (XmlClass xc : xt.getXmlClasses()) {
				xtClasses.add(xc.getName());
			}
			boolean hasAllDeployments = xtClasses.containsAll(deploymentsClassNames);
			boolean hasAnySubdeployments = !java.util.Collections.disjoint(xtClasses, subdeploymentsClassNames);
			if (hasAllDeployments && hasAnySubdeployments) {
				coAlloc = true;
				break;
			}
		}
		assertTrue("Deployments (" + deploymentsClassNames + ") and Subdeployments (" + subdeploymentsClassNames
				+ ") must be declared in the SAME <test> block of testng.xml so the three-level group dependency "
				+ "(Subdeployments → Deployments → SystemFeatures → Core) resolves within scope. See Sprint 8 "
				+ "S-ETS-08-02 + ADR-010 v4 amendment.", coAlloc);
	}

	// ===== Sprint 9 S-ETS-09-01 — GeoJSON group =====
	// GeoJSON systems read-only subset depends on SystemFeatures because it validates
	// /systems GeoJSON encoding. This is intentionally a PARTIAL implementation of
	// REQ-ETS-PART1-012.

	/**
	 * Sprint 9 S-ETS-09-01 (REQ-ETS-PART1-012): the canonical testng.xml SHALL declare
	 * {@code <group name="geojson" depends-on="systemfeatures"/>} so GeoJSON tests
	 * cascade-SKIP when the SystemFeatures prerequisite fails.
	 */
	@org.junit.Test
	public void testGeoJsonGroupDependsOnSystemFeatures() throws Exception {
		XmlSuite suite = parseShippedSuite();
		assertFalse("Expected at least one <test> block in testng.xml", suite.getTests().isEmpty());

		boolean foundDependency = false;
		for (XmlTest xt : suite.getTests()) {
			java.util.Map<String, String> deps = xt.getXmlDependencyGroups();
			if (deps != null && deps.containsKey(GEOJSON_GROUP)) {
				String dependsOn = deps.get(GEOJSON_GROUP);
				assertNotNull("group '" + GEOJSON_GROUP + "' has null depends-on attribute", dependsOn);
				assertTrue("group '" + GEOJSON_GROUP + "' depends-on '" + dependsOn + "' missing '"
						+ SYSTEMFEATURES_GROUP + "'", dependsOn.contains(SYSTEMFEATURES_GROUP));
				foundDependency = true;
				break;
			}
		}
		assertTrue("testng.xml does not declare <group name=\"" + GEOJSON_GROUP + "\" depends-on=\""
				+ SYSTEMFEATURES_GROUP + "\"/> — see Sprint 9 S-ETS-09-01. The GeoJSON systems read-only "
				+ "subset requires SystemFeatures as its direct prerequisite.", foundDependency);
	}

	/**
	 * Sprint 9 S-ETS-09-01: every GeoJSON @Test method SHALL carry
	 * {@code groups = "geojson"} so the suite-level dependency declaration has tagged
	 * methods to resolve against.
	 */
	@org.junit.Test
	public void testEveryGeoJsonTestMethodCarriesGeoJsonGroup() {
		List<String> offenders = new ArrayList<>();
		int totalGeoJson = 0;
		for (Class<?> c : GEOJSON_CLASSES) {
			for (Method m : c.getDeclaredMethods()) {
				Test ann = m.getAnnotation(Test.class);
				if (ann == null) {
					continue;
				}
				totalGeoJson++;
				List<String> groups = java.util.Arrays.asList(ann.groups());
				if (!groups.contains(GEOJSON_GROUP)) {
					offenders.add(c.getSimpleName() + "#" + m.getName() + " (groups=" + groups + ")");
				}
			}
		}
		assertTrue("Expected at least one @Test method in GeoJSON conformance classes; found 0", totalGeoJson > 0);
		assertTrue("GeoJSON @Test methods missing groups=\"" + GEOJSON_GROUP + "\": " + offenders, offenders.isEmpty());
	}

	/**
	 * Sprint 9 S-ETS-09-01: GeoJSON classes MUST be co-located in the SAME {@code <test>}
	 * block as SystemFeatures so the group-dependency cascade resolves within TestNG's
	 * test-scoped dependency map.
	 */
	@org.junit.Test
	public void testGeoJsonCoLocatedWithSystemFeatures() throws Exception {
		XmlSuite suite = parseShippedSuite();
		Set<String> systemFeaturesClassNames = new HashSet<>();
		for (Class<?> c : SYSTEMFEATURES_CLASSES) {
			systemFeaturesClassNames.add(c.getName());
		}
		Set<String> geoJsonClassNames = new HashSet<>();
		for (Class<?> c : GEOJSON_CLASSES) {
			geoJsonClassNames.add(c.getName());
		}

		boolean coAlloc = false;
		for (XmlTest xt : suite.getTests()) {
			Set<String> xtClasses = new HashSet<>();
			for (XmlClass xc : xt.getXmlClasses()) {
				xtClasses.add(xc.getName());
			}
			boolean hasAllSystemFeatures = xtClasses.containsAll(systemFeaturesClassNames);
			boolean hasAnyGeoJson = !java.util.Collections.disjoint(xtClasses, geoJsonClassNames);
			if (hasAllSystemFeatures && hasAnyGeoJson) {
				coAlloc = true;
				break;
			}
		}
		assertTrue(
				"SystemFeatures (" + systemFeaturesClassNames + ") and GeoJSON (" + geoJsonClassNames
						+ ") must be declared in the SAME <test> block of testng.xml so the group dependency "
						+ "(GeoJSON → SystemFeatures → Core) resolves within scope. See Sprint 9 S-ETS-09-01.",
				coAlloc);
	}

	// ===== Sprint 10 S-ETS-10-01 — SensorML group =====
	// SensorML systems read-only subset depends on SystemFeatures because it validates
	// system SensorML representations. This is intentionally a PARTIAL implementation of
	// REQ-ETS-PART1-013.

	/**
	 * Sprint 10 S-ETS-10-01 (REQ-ETS-PART1-013): the canonical testng.xml SHALL declare
	 * {@code <group name="sensorml" depends-on="systemfeatures"/>} so SensorML tests
	 * cascade-SKIP when the SystemFeatures prerequisite fails.
	 */
	@org.junit.Test
	public void testSensorMlGroupDependsOnSystemFeatures() throws Exception {
		XmlSuite suite = parseShippedSuite();
		assertFalse("Expected at least one <test> block in testng.xml", suite.getTests().isEmpty());

		boolean foundDependency = false;
		for (XmlTest xt : suite.getTests()) {
			java.util.Map<String, String> deps = xt.getXmlDependencyGroups();
			if (deps != null && deps.containsKey(SENSORML_GROUP)) {
				String dependsOn = deps.get(SENSORML_GROUP);
				assertNotNull("group '" + SENSORML_GROUP + "' has null depends-on attribute", dependsOn);
				assertTrue("group '" + SENSORML_GROUP + "' depends-on '" + dependsOn + "' missing '"
						+ SYSTEMFEATURES_GROUP + "'", dependsOn.contains(SYSTEMFEATURES_GROUP));
				foundDependency = true;
				break;
			}
		}
		assertTrue("testng.xml does not declare <group name=\"" + SENSORML_GROUP + "\" depends-on=\""
				+ SYSTEMFEATURES_GROUP + "\"/> — see Sprint 10 S-ETS-10-01. The SensorML systems read-only "
				+ "subset requires SystemFeatures as its direct prerequisite.", foundDependency);
	}

	/**
	 * Sprint 10 S-ETS-10-01: every SensorML @Test method SHALL carry
	 * {@code groups = "sensorml"} so the suite-level dependency declaration has tagged
	 * methods to resolve against.
	 */
	@org.junit.Test
	public void testEverySensorMlTestMethodCarriesSensorMlGroup() {
		List<String> offenders = new ArrayList<>();
		int totalSensorMl = 0;
		for (Class<?> c : SENSORML_CLASSES) {
			for (Method m : c.getDeclaredMethods()) {
				Test ann = m.getAnnotation(Test.class);
				if (ann == null) {
					continue;
				}
				totalSensorMl++;
				List<String> groups = java.util.Arrays.asList(ann.groups());
				if (!groups.contains(SENSORML_GROUP)) {
					offenders.add(c.getSimpleName() + "#" + m.getName() + " (groups=" + groups + ")");
				}
			}
		}
		assertTrue("Expected at least one @Test method in SensorML conformance classes; found 0", totalSensorMl > 0);
		assertTrue("SensorML @Test methods missing groups=\"" + SENSORML_GROUP + "\": " + offenders,
				offenders.isEmpty());
	}

	/**
	 * Sprint 10 S-ETS-10-01: SensorML classes MUST be co-located in the SAME
	 * {@code <test>} block as SystemFeatures so the group-dependency cascade resolves
	 * within TestNG's test-scoped dependency map.
	 */
	@org.junit.Test
	public void testSensorMlCoLocatedWithSystemFeatures() throws Exception {
		XmlSuite suite = parseShippedSuite();
		Set<String> systemFeaturesClassNames = new HashSet<>();
		for (Class<?> c : SYSTEMFEATURES_CLASSES) {
			systemFeaturesClassNames.add(c.getName());
		}
		Set<String> sensorMlClassNames = new HashSet<>();
		for (Class<?> c : SENSORML_CLASSES) {
			sensorMlClassNames.add(c.getName());
		}

		boolean coAlloc = false;
		for (XmlTest xt : suite.getTests()) {
			Set<String> xtClasses = new HashSet<>();
			for (XmlClass xc : xt.getXmlClasses()) {
				xtClasses.add(xc.getName());
			}
			boolean hasAllSystemFeatures = xtClasses.containsAll(systemFeaturesClassNames);
			boolean hasAnySensorMl = !java.util.Collections.disjoint(xtClasses, sensorMlClassNames);
			if (hasAllSystemFeatures && hasAnySensorMl) {
				coAlloc = true;
				break;
			}
		}
		assertTrue(
				"SystemFeatures (" + systemFeaturesClassNames + ") and SensorML (" + sensorMlClassNames
						+ ") must be declared in the SAME <test> block of testng.xml so the group dependency "
						+ "(SensorML → SystemFeatures → Core) resolves within scope. See Sprint 10 S-ETS-10-01.",
				coAlloc);
	}

	// ===== Sprint 11 S-ETS-11-01 — AdvancedFiltering group =====
	// AdvancedFiltering systems/common-resource read-only subset depends on
	// SystemFeatures
	// because it validates /systems query behavior. This is intentionally a PARTIAL
	// implementation of REQ-ETS-PART1-009.

	/**
	 * Sprint 11 S-ETS-11-01 (REQ-ETS-PART1-009): the canonical testng.xml SHALL declare
	 * {@code <group name="advancedfiltering" depends-on="systemfeatures"/>} so
	 * AdvancedFiltering tests cascade-SKIP when the SystemFeatures prerequisite fails.
	 */
	@org.junit.Test
	public void testAdvancedFilteringGroupDependsOnSystemFeatures() throws Exception {
		XmlSuite suite = parseShippedSuite();
		assertFalse("Expected at least one <test> block in testng.xml", suite.getTests().isEmpty());

		boolean foundDependency = false;
		for (XmlTest xt : suite.getTests()) {
			java.util.Map<String, String> deps = xt.getXmlDependencyGroups();
			if (deps != null && deps.containsKey(ADVANCEDFILTERING_GROUP)) {
				String dependsOn = deps.get(ADVANCEDFILTERING_GROUP);
				assertNotNull("group '" + ADVANCEDFILTERING_GROUP + "' has null depends-on attribute", dependsOn);
				assertTrue("group '" + ADVANCEDFILTERING_GROUP + "' depends-on '" + dependsOn + "' missing '"
						+ SYSTEMFEATURES_GROUP + "'", dependsOn.contains(SYSTEMFEATURES_GROUP));
				foundDependency = true;
				break;
			}
		}
		assertTrue("testng.xml does not declare <group name=\"" + ADVANCEDFILTERING_GROUP + "\" depends-on=\""
				+ SYSTEMFEATURES_GROUP + "\"/> — see Sprint 11 S-ETS-11-01. The AdvancedFiltering "
				+ "systems/common-resource read-only subset requires SystemFeatures as its direct prerequisite.",
				foundDependency);
	}

	/**
	 * Sprint 11 S-ETS-11-01: every AdvancedFiltering @Test method SHALL carry
	 * {@code groups = "advancedfiltering"} so the suite-level dependency declaration has
	 * tagged methods to resolve against.
	 */
	@org.junit.Test
	public void testEveryAdvancedFilteringTestMethodCarriesAdvancedFilteringGroup() {
		List<String> offenders = new ArrayList<>();
		int totalAdvancedFiltering = 0;
		for (Class<?> c : ADVANCEDFILTERING_CLASSES) {
			for (Method m : c.getDeclaredMethods()) {
				Test ann = m.getAnnotation(Test.class);
				if (ann == null) {
					continue;
				}
				totalAdvancedFiltering++;
				List<String> groups = java.util.Arrays.asList(ann.groups());
				if (!groups.contains(ADVANCEDFILTERING_GROUP)) {
					offenders.add(c.getSimpleName() + "#" + m.getName() + " (groups=" + groups + ")");
				}
			}
		}
		assertTrue("Expected at least one @Test method in AdvancedFiltering conformance classes; found 0",
				totalAdvancedFiltering > 0);
		assertTrue("AdvancedFiltering @Test methods missing groups=\"" + ADVANCEDFILTERING_GROUP + "\": " + offenders,
				offenders.isEmpty());
	}

	/**
	 * Sprint 11 S-ETS-11-01: AdvancedFiltering classes MUST be co-located in the SAME
	 * {@code <test>} block as SystemFeatures so the group-dependency cascade resolves
	 * within TestNG's test-scoped dependency map.
	 */
	@org.junit.Test
	public void testAdvancedFilteringCoLocatedWithSystemFeatures() throws Exception {
		XmlSuite suite = parseShippedSuite();
		Set<String> systemFeaturesClassNames = new HashSet<>();
		for (Class<?> c : SYSTEMFEATURES_CLASSES) {
			systemFeaturesClassNames.add(c.getName());
		}
		Set<String> advancedFilteringClassNames = new HashSet<>();
		for (Class<?> c : ADVANCEDFILTERING_CLASSES) {
			advancedFilteringClassNames.add(c.getName());
		}

		boolean coAlloc = false;
		for (XmlTest xt : suite.getTests()) {
			Set<String> xtClasses = new HashSet<>();
			for (XmlClass xc : xt.getXmlClasses()) {
				xtClasses.add(xc.getName());
			}
			boolean hasAllSystemFeatures = xtClasses.containsAll(systemFeaturesClassNames);
			boolean hasAnyAdvancedFiltering = !java.util.Collections.disjoint(xtClasses, advancedFilteringClassNames);
			if (hasAllSystemFeatures && hasAnyAdvancedFiltering) {
				coAlloc = true;
				break;
			}
		}
		assertTrue("SystemFeatures (" + systemFeaturesClassNames + ") and AdvancedFiltering ("
				+ advancedFilteringClassNames
				+ ") must be declared in the SAME <test> block of testng.xml so the group dependency "
				+ "(AdvancedFiltering → SystemFeatures → Core) resolves within scope. See Sprint 11 S-ETS-11-01.",
				coAlloc);
	}

	// ===== Sprint 12 S-ETS-12-01 — CreateReplaceDelete group =====
	// CreateReplaceDelete systems subset depends on SystemFeatures because it validates
	// /systems mutation behavior behind an explicit safety gate. This is intentionally a
	// PARTIAL implementation of REQ-ETS-PART1-010.

	/**
	 * Sprint 12 S-ETS-12-01 (REQ-ETS-PART1-010): the canonical testng.xml SHALL declare
	 * {@code <group name="createreplacedelete" depends-on="systemfeatures"/>} so
	 * CreateReplaceDelete tests cascade-SKIP when the SystemFeatures prerequisite fails.
	 */
	@org.junit.Test
	public void testCreateReplaceDeleteGroupDependsOnSystemFeatures() throws Exception {
		XmlSuite suite = parseShippedSuite();
		assertFalse("Expected at least one <test> block in testng.xml", suite.getTests().isEmpty());

		boolean foundDependency = false;
		for (XmlTest xt : suite.getTests()) {
			java.util.Map<String, String> deps = xt.getXmlDependencyGroups();
			if (deps != null && deps.containsKey(CREATE_REPLACE_DELETE_GROUP)) {
				String dependsOn = deps.get(CREATE_REPLACE_DELETE_GROUP);
				assertNotNull("group '" + CREATE_REPLACE_DELETE_GROUP + "' has null depends-on attribute", dependsOn);
				assertTrue("group '" + CREATE_REPLACE_DELETE_GROUP + "' depends-on '" + dependsOn + "' missing '"
						+ SYSTEMFEATURES_GROUP + "'", dependsOn.contains(SYSTEMFEATURES_GROUP));
				foundDependency = true;
				break;
			}
		}
		assertTrue(
				"testng.xml does not declare <group name=\"" + CREATE_REPLACE_DELETE_GROUP + "\" depends-on=\""
						+ SYSTEMFEATURES_GROUP + "\"/> — see Sprint 12 S-ETS-12-01. The CreateReplaceDelete "
						+ "systems safety-gated subset requires SystemFeatures as its direct prerequisite.",
				foundDependency);
	}

	/**
	 * Sprint 12 S-ETS-12-01: every CreateReplaceDelete @Test method SHALL carry
	 * {@code groups = "createreplacedelete"} so the suite-level dependency declaration
	 * has tagged methods to resolve against.
	 */
	@org.junit.Test
	public void testEveryCreateReplaceDeleteTestMethodCarriesCreateReplaceDeleteGroup() {
		List<String> offenders = new ArrayList<>();
		int totalCreateReplaceDelete = 0;
		for (Class<?> c : CREATE_REPLACE_DELETE_CLASSES) {
			for (Method m : c.getDeclaredMethods()) {
				Test ann = m.getAnnotation(Test.class);
				if (ann == null) {
					continue;
				}
				totalCreateReplaceDelete++;
				List<String> groups = java.util.Arrays.asList(ann.groups());
				if (!groups.contains(CREATE_REPLACE_DELETE_GROUP)) {
					offenders.add(c.getSimpleName() + "#" + m.getName() + " (groups=" + groups + ")");
				}
			}
		}
		assertTrue("Expected at least one @Test method in CreateReplaceDelete conformance classes; found 0",
				totalCreateReplaceDelete > 0);
		assertTrue("CreateReplaceDelete @Test methods missing groups=\"" + CREATE_REPLACE_DELETE_GROUP + "\": "
				+ offenders, offenders.isEmpty());
	}

	/**
	 * Sprint 12 S-ETS-12-01: CreateReplaceDelete classes MUST be co-located in the SAME
	 * {@code <test>} block as SystemFeatures so the group-dependency cascade resolves
	 * within TestNG's test-scoped dependency map.
	 */
	@org.junit.Test
	public void testCreateReplaceDeleteCoLocatedWithSystemFeatures() throws Exception {
		XmlSuite suite = parseShippedSuite();
		Set<String> systemFeaturesClassNames = new HashSet<>();
		for (Class<?> c : SYSTEMFEATURES_CLASSES) {
			systemFeaturesClassNames.add(c.getName());
		}
		Set<String> createReplaceDeleteClassNames = new HashSet<>();
		for (Class<?> c : CREATE_REPLACE_DELETE_CLASSES) {
			createReplaceDeleteClassNames.add(c.getName());
		}

		boolean coAlloc = false;
		for (XmlTest xt : suite.getTests()) {
			Set<String> xtClasses = new HashSet<>();
			for (XmlClass xc : xt.getXmlClasses()) {
				xtClasses.add(xc.getName());
			}
			boolean hasAllSystemFeatures = xtClasses.containsAll(systemFeaturesClassNames);
			boolean hasAnyCreateReplaceDelete = !java.util.Collections.disjoint(xtClasses,
					createReplaceDeleteClassNames);
			if (hasAllSystemFeatures && hasAnyCreateReplaceDelete) {
				coAlloc = true;
				break;
			}
		}
		assertTrue("SystemFeatures (" + systemFeaturesClassNames + ") and CreateReplaceDelete ("
				+ createReplaceDeleteClassNames
				+ ") must be declared in the SAME <test> block of testng.xml so the group dependency "
				+ "(CreateReplaceDelete → SystemFeatures → Core) resolves within scope. See Sprint 12 S-ETS-12-01.",
				coAlloc);
	}

	// ===== Sprint 13 S-ETS-13-01 — Update group =====
	// Update/PATCH systems subset depends on CreateReplaceDelete because OGC Part 1
	// Update requires /req/create-replace-delete. This is intentionally a PARTIAL
	// implementation of REQ-ETS-PART1-011.

	/**
	 * Sprint 13 S-ETS-13-01 (REQ-ETS-PART1-011): the canonical testng.xml SHALL declare
	 * {@code <group name="update" depends-on="createreplacedelete"/>} so Update tests
	 * cascade-SKIP when the CreateReplaceDelete prerequisite fails or skips.
	 */
	@org.junit.Test
	public void testUpdateGroupDependsOnCreateReplaceDelete() throws Exception {
		XmlSuite suite = parseShippedSuite();
		assertFalse("Expected at least one <test> block in testng.xml", suite.getTests().isEmpty());

		boolean foundDependency = false;
		for (XmlTest xt : suite.getTests()) {
			java.util.Map<String, String> deps = xt.getXmlDependencyGroups();
			if (deps != null && deps.containsKey(UPDATE_GROUP)) {
				String dependsOn = deps.get(UPDATE_GROUP);
				assertNotNull("group '" + UPDATE_GROUP + "' has null depends-on attribute", dependsOn);
				assertTrue("group '" + UPDATE_GROUP + "' depends-on '" + dependsOn + "' missing '"
						+ CREATE_REPLACE_DELETE_GROUP + "'", dependsOn.contains(CREATE_REPLACE_DELETE_GROUP));
				foundDependency = true;
				break;
			}
		}
		assertTrue(
				"testng.xml does not declare <group name=\"" + UPDATE_GROUP + "\" depends-on=\""
						+ CREATE_REPLACE_DELETE_GROUP + "\"/> — see Sprint 13 S-ETS-13-01. The Update "
						+ "systems safety-gated subset requires CreateReplaceDelete as its direct prerequisite.",
				foundDependency);
	}

	/**
	 * Sprint 13 S-ETS-13-01: every Update @Test method SHALL carry
	 * {@code groups = "update"} so the suite-level dependency declaration has tagged
	 * methods to resolve against.
	 */
	@org.junit.Test
	public void testEveryUpdateTestMethodCarriesUpdateGroup() {
		List<String> offenders = new ArrayList<>();
		int totalUpdate = 0;
		for (Class<?> c : UPDATE_CLASSES) {
			for (Method m : c.getDeclaredMethods()) {
				Test ann = m.getAnnotation(Test.class);
				if (ann == null) {
					continue;
				}
				totalUpdate++;
				List<String> groups = java.util.Arrays.asList(ann.groups());
				if (!groups.contains(UPDATE_GROUP)) {
					offenders.add(c.getSimpleName() + "#" + m.getName() + " (groups=" + groups + ")");
				}
			}
		}
		assertTrue("Expected at least one @Test method in Update conformance classes; found 0", totalUpdate > 0);
		assertTrue("Update @Test methods missing groups=\"" + UPDATE_GROUP + "\": " + offenders, offenders.isEmpty());
	}

	/**
	 * Sprint 13 S-ETS-13-01: Update classes MUST be co-located in the SAME {@code <test>}
	 * block as CreateReplaceDelete so the group-dependency cascade resolves within
	 * TestNG's test-scoped dependency map.
	 */
	@org.junit.Test
	public void testUpdateCoLocatedWithCreateReplaceDelete() throws Exception {
		XmlSuite suite = parseShippedSuite();
		Set<String> createReplaceDeleteClassNames = new HashSet<>();
		for (Class<?> c : CREATE_REPLACE_DELETE_CLASSES) {
			createReplaceDeleteClassNames.add(c.getName());
		}
		Set<String> updateClassNames = new HashSet<>();
		for (Class<?> c : UPDATE_CLASSES) {
			updateClassNames.add(c.getName());
		}

		boolean coAlloc = false;
		for (XmlTest xt : suite.getTests()) {
			Set<String> xtClasses = new HashSet<>();
			for (XmlClass xc : xt.getXmlClasses()) {
				xtClasses.add(xc.getName());
			}
			boolean hasAllCreateReplaceDelete = xtClasses.containsAll(createReplaceDeleteClassNames);
			boolean hasAnyUpdate = !java.util.Collections.disjoint(xtClasses, updateClassNames);
			if (hasAllCreateReplaceDelete && hasAnyUpdate) {
				coAlloc = true;
				break;
			}
		}
		assertTrue("CreateReplaceDelete (" + createReplaceDeleteClassNames + ") and Update (" + updateClassNames
				+ ") must be declared in the SAME <test> block of testng.xml so the group dependency "
				+ "(Update → CreateReplaceDelete → SystemFeatures → Core) resolves within scope. "
				+ "See Sprint 13 S-ETS-13-01.", coAlloc);
	}

	// ===== Sprint 20 S-ETS-20-01 — Part 2 API Common group =====
	// Part 2 API Common depends on Core and Common because it checks the CS API Part 2
	// API Common subset only after the foundational landing/conformance/common behavior
	// is available. The runtime class remains declaration-gated on /conf/api-common.

	/**
	 * Sprint 20 S-ETS-20-01 (REQ-ETS-PART2-001): the canonical testng.xml SHALL declare
	 * {@code <group name="part2apicommon" depends-on="core common"/>} so Part 2 API
	 * Common tests cascade-SKIP when either foundational prerequisite fails.
	 */
	@org.junit.Test
	public void testPart2ApiCommonGroupDependsOnCoreAndCommon() throws Exception {
		XmlSuite suite = parseShippedSuite();
		assertFalse("Expected at least one <test> block in testng.xml", suite.getTests().isEmpty());

		boolean foundDependency = false;
		for (XmlTest xt : suite.getTests()) {
			java.util.Map<String, String> deps = xt.getXmlDependencyGroups();
			if (deps != null && deps.containsKey(PART2_API_COMMON_GROUP)) {
				String dependsOn = deps.get(PART2_API_COMMON_GROUP);
				assertNotNull("group '" + PART2_API_COMMON_GROUP + "' has null depends-on attribute", dependsOn);
				assertFalse(
						"group '" + PART2_API_COMMON_GROUP + "' depends-on '" + dependsOn
								+ "' uses comma syntax, which TestNG treats as a nonexistent single group at runtime",
						dependsOn.contains(","));
				Set<String> dependencyTokens = dependencyTokens(dependsOn);
				assertTrue("group '" + PART2_API_COMMON_GROUP + "' depends-on '" + dependsOn + "' missing '"
						+ CORE_GROUP + "'", dependencyTokens.contains(CORE_GROUP));
				assertTrue("group '" + PART2_API_COMMON_GROUP + "' depends-on '" + dependsOn + "' missing '"
						+ COMMON_GROUP + "'", dependencyTokens.contains(COMMON_GROUP));
				foundDependency = true;
				break;
			}
		}
		assertTrue("testng.xml does not declare <group name=\"" + PART2_API_COMMON_GROUP
				+ "\" depends-on=\"core common\"/> — see Sprint 20 S-ETS-20-01. The Part 2 API Common subset "
				+ "requires Core and Common as prerequisites.", foundDependency);
	}

	/**
	 * Sprint 20 S-ETS-20-01: every Part 2 API Common @Test method SHALL carry
	 * {@code groups = "part2apicommon"} so the suite-level dependency declaration has
	 * tagged methods to resolve against.
	 */
	@org.junit.Test
	public void testEveryPart2ApiCommonTestMethodCarriesPart2ApiCommonGroup() {
		List<String> offenders = new ArrayList<>();
		int totalPart2ApiCommon = 0;
		for (Class<?> c : PART2_API_COMMON_CLASSES) {
			for (Method m : c.getDeclaredMethods()) {
				Test ann = m.getAnnotation(Test.class);
				if (ann == null) {
					continue;
				}
				totalPart2ApiCommon++;
				List<String> groups = java.util.Arrays.asList(ann.groups());
				if (!groups.contains(PART2_API_COMMON_GROUP)) {
					offenders.add(c.getSimpleName() + "#" + m.getName() + " (groups=" + groups + ")");
				}
			}
		}
		assertTrue("Expected at least one @Test method in Part 2 API Common conformance classes; found 0",
				totalPart2ApiCommon > 0);
		assertTrue("Part 2 API Common @Test methods missing groups=\"" + PART2_API_COMMON_GROUP + "\": " + offenders,
				offenders.isEmpty());
	}

	/**
	 * Sprint 20 S-ETS-20-01: Part 2 API Common classes MUST be co-located in the SAME
	 * {@code <test>} block as Core and Common so both group dependencies resolve within
	 * TestNG's test-scoped dependency map.
	 */
	@org.junit.Test
	public void testPart2ApiCommonCoLocatedWithCoreAndCommon() throws Exception {
		XmlSuite suite = parseShippedSuite();
		Set<String> coreClassNames = new HashSet<>();
		for (Class<?> c : CORE_CLASSES) {
			coreClassNames.add(c.getName());
		}
		Set<String> commonClassNames = new HashSet<>();
		for (Class<?> c : COMMON_CLASSES) {
			commonClassNames.add(c.getName());
		}
		Set<String> part2ApiCommonClassNames = new HashSet<>();
		for (Class<?> c : PART2_API_COMMON_CLASSES) {
			part2ApiCommonClassNames.add(c.getName());
		}

		boolean coAlloc = false;
		for (XmlTest xt : suite.getTests()) {
			Set<String> xtClasses = new HashSet<>();
			for (XmlClass xc : xt.getXmlClasses()) {
				xtClasses.add(xc.getName());
			}
			boolean hasAllCore = xtClasses.containsAll(coreClassNames);
			boolean hasAllCommon = xtClasses.containsAll(commonClassNames);
			boolean hasAnyPart2ApiCommon = !java.util.Collections.disjoint(xtClasses, part2ApiCommonClassNames);
			if (hasAllCore && hasAllCommon && hasAnyPart2ApiCommon) {
				coAlloc = true;
				break;
			}
		}
		assertTrue(
				"Core (" + coreClassNames + "), Common (" + commonClassNames + "), and Part 2 API Common ("
						+ part2ApiCommonClassNames
						+ ") must be declared in the SAME <test> block of testng.xml so the group dependency "
						+ "(Part2ApiCommon → Core + Common) resolves within scope. See Sprint 20 S-ETS-20-01.",
				coAlloc);
	}

	// ===== Sprint 21 S-ETS-21-01 — Part 2 Datastream group =====
	// Datastream depends on Core and Common, not Part2ApiCommon, so scoped endpoint
	// evidence can run when /conf/datastream is declared but /conf/api-common is absent.
	// Runtime checks still make full /conf/datastream closure prerequisite-incomplete.

	/**
	 * Sprint 21 S-ETS-21-01 (REQ-ETS-PART2-002): the canonical testng.xml SHALL declare
	 * {@code <group name="part2datastream" depends-on="core common"/>}.
	 */
	@org.junit.Test
	public void testPart2DatastreamGroupDependsOnCoreAndCommon() throws Exception {
		XmlSuite suite = parseShippedSuite();
		assertFalse("Expected at least one <test> block in testng.xml", suite.getTests().isEmpty());

		boolean foundDependency = false;
		for (XmlTest xt : suite.getTests()) {
			java.util.Map<String, String> deps = xt.getXmlDependencyGroups();
			if (deps != null && deps.containsKey(PART2_DATASTREAM_GROUP)) {
				String dependsOn = deps.get(PART2_DATASTREAM_GROUP);
				assertNotNull("group '" + PART2_DATASTREAM_GROUP + "' has null depends-on attribute", dependsOn);
				assertFalse(
						"group '" + PART2_DATASTREAM_GROUP + "' depends-on '" + dependsOn
								+ "' uses comma syntax, which TestNG treats as a nonexistent single group at runtime",
						dependsOn.contains(","));
				Set<String> dependencyTokens = dependencyTokens(dependsOn);
				assertTrue("group '" + PART2_DATASTREAM_GROUP + "' depends-on '" + dependsOn + "' missing '"
						+ CORE_GROUP + "'", dependencyTokens.contains(CORE_GROUP));
				assertTrue("group '" + PART2_DATASTREAM_GROUP + "' depends-on '" + dependsOn + "' missing '"
						+ COMMON_GROUP + "'", dependencyTokens.contains(COMMON_GROUP));
				assertFalse("group '" + PART2_DATASTREAM_GROUP
						+ "' must not depend on part2apicommon; otherwise GeoRobotix /conf/datastream endpoint checks cascade-SKIP before scoped evidence can run",
						dependencyTokens.contains(PART2_API_COMMON_GROUP));
				foundDependency = true;
				break;
			}
		}
		assertTrue("testng.xml does not declare <group name=\"" + PART2_DATASTREAM_GROUP
				+ "\" depends-on=\"core common\"/> — see Sprint 21 S-ETS-21-01.", foundDependency);
	}

	/**
	 * Sprint 21 S-ETS-21-01: every Part 2 Datastream @Test method SHALL carry
	 * {@code groups = "part2datastream"}.
	 */
	@org.junit.Test
	public void testEveryPart2DatastreamTestMethodCarriesPart2DatastreamGroup() {
		List<String> offenders = new ArrayList<>();
		int totalPart2Datastream = 0;
		for (Class<?> c : PART2_DATASTREAM_CLASSES) {
			for (Method m : c.getDeclaredMethods()) {
				Test ann = m.getAnnotation(Test.class);
				if (ann == null) {
					continue;
				}
				totalPart2Datastream++;
				List<String> groups = java.util.Arrays.asList(ann.groups());
				if (!groups.contains(PART2_DATASTREAM_GROUP)) {
					offenders.add(c.getSimpleName() + "#" + m.getName() + " (groups=" + groups + ")");
				}
			}
		}
		assertTrue("Expected at least one @Test method in Part 2 Datastream conformance classes; found 0",
				totalPart2Datastream > 0);
		assertTrue("Part 2 Datastream @Test methods missing groups=\"" + PART2_DATASTREAM_GROUP + "\": " + offenders,
				offenders.isEmpty());
	}

	/**
	 * Sprint 21 S-ETS-21-01: Part 2 Datastream classes MUST be co-located in the SAME
	 * {@code <test>} block as Core and Common.
	 */
	@org.junit.Test
	public void testPart2DatastreamCoLocatedWithCoreAndCommon() throws Exception {
		XmlSuite suite = parseShippedSuite();
		Set<String> coreClassNames = new HashSet<>();
		for (Class<?> c : CORE_CLASSES) {
			coreClassNames.add(c.getName());
		}
		Set<String> commonClassNames = new HashSet<>();
		for (Class<?> c : COMMON_CLASSES) {
			commonClassNames.add(c.getName());
		}
		Set<String> part2DatastreamClassNames = new HashSet<>();
		for (Class<?> c : PART2_DATASTREAM_CLASSES) {
			part2DatastreamClassNames.add(c.getName());
		}

		boolean coAlloc = false;
		for (XmlTest xt : suite.getTests()) {
			Set<String> xtClasses = new HashSet<>();
			for (XmlClass xc : xt.getXmlClasses()) {
				xtClasses.add(xc.getName());
			}
			boolean hasAllCore = xtClasses.containsAll(coreClassNames);
			boolean hasAllCommon = xtClasses.containsAll(commonClassNames);
			boolean hasAnyPart2Datastream = !java.util.Collections.disjoint(xtClasses, part2DatastreamClassNames);
			if (hasAllCore && hasAllCommon && hasAnyPart2Datastream) {
				coAlloc = true;
				break;
			}
		}
		assertTrue(
				"Core (" + coreClassNames + "), Common (" + commonClassNames + "), and Part 2 Datastream ("
						+ part2DatastreamClassNames
						+ ") must be declared in the SAME <test> block of testng.xml so the group dependency "
						+ "(Part2Datastream → Core + Common) resolves within scope. See Sprint 21 S-ETS-21-01.",
				coAlloc);
	}

	// ===== Sprint 22 S-ETS-22-01 — Part 2 ControlStream group =====
	// ControlStream depends on Core and Common, not Part2ApiCommon, so scoped endpoint
	// evidence can run when /conf/controlstream is declared but /conf/api-common is
	// absent. Runtime checks still make full /conf/controlstream closure
	// prerequisite-incomplete.

	/**
	 * Sprint 22 S-ETS-22-01 (REQ-ETS-PART2-003): the canonical testng.xml SHALL declare
	 * {@code <group name="part2controlstream" depends-on="core common"/>}.
	 */
	@org.junit.Test
	public void testPart2ControlStreamGroupDependsOnCoreAndCommon() throws Exception {
		XmlSuite suite = parseShippedSuite();
		assertFalse("Expected at least one <test> block in testng.xml", suite.getTests().isEmpty());

		boolean foundDependency = false;
		for (XmlTest xt : suite.getTests()) {
			java.util.Map<String, String> deps = xt.getXmlDependencyGroups();
			if (deps != null && deps.containsKey(PART2_CONTROLSTREAM_GROUP)) {
				String dependsOn = deps.get(PART2_CONTROLSTREAM_GROUP);
				assertNotNull("group '" + PART2_CONTROLSTREAM_GROUP + "' has null depends-on attribute", dependsOn);
				assertFalse(
						"group '" + PART2_CONTROLSTREAM_GROUP + "' depends-on '" + dependsOn
								+ "' uses comma syntax, which TestNG treats as a nonexistent single group at runtime",
						dependsOn.contains(","));
				Set<String> dependencyTokens = dependencyTokens(dependsOn);
				assertTrue("group '" + PART2_CONTROLSTREAM_GROUP + "' depends-on '" + dependsOn + "' missing '"
						+ CORE_GROUP + "'", dependencyTokens.contains(CORE_GROUP));
				assertTrue("group '" + PART2_CONTROLSTREAM_GROUP + "' depends-on '" + dependsOn + "' missing '"
						+ COMMON_GROUP + "'", dependencyTokens.contains(COMMON_GROUP));
				assertFalse("group '" + PART2_CONTROLSTREAM_GROUP
						+ "' must not depend on part2apicommon; otherwise GeoRobotix /conf/controlstream endpoint checks cascade-SKIP before scoped evidence can run",
						dependencyTokens.contains(PART2_API_COMMON_GROUP));
				foundDependency = true;
				break;
			}
		}
		assertTrue("testng.xml does not declare <group name=\"" + PART2_CONTROLSTREAM_GROUP
				+ "\" depends-on=\"core common\"/> — see Sprint 22 S-ETS-22-01.", foundDependency);
	}

	/**
	 * Sprint 22 S-ETS-22-01: every Part 2 ControlStream @Test method SHALL carry
	 * {@code groups = "part2controlstream"}.
	 */
	@org.junit.Test
	public void testEveryPart2ControlStreamTestMethodCarriesPart2ControlStreamGroup() {
		List<String> offenders = new ArrayList<>();
		int totalPart2ControlStream = 0;
		for (Class<?> c : PART2_CONTROLSTREAM_CLASSES) {
			for (Method m : c.getDeclaredMethods()) {
				Test ann = m.getAnnotation(Test.class);
				if (ann == null) {
					continue;
				}
				totalPart2ControlStream++;
				List<String> groups = java.util.Arrays.asList(ann.groups());
				if (!groups.contains(PART2_CONTROLSTREAM_GROUP)) {
					offenders.add(c.getSimpleName() + "#" + m.getName() + " (groups=" + groups + ")");
				}
			}
		}
		assertTrue("Expected at least one @Test method in Part 2 ControlStream conformance classes; found 0",
				totalPart2ControlStream > 0);
		assertTrue(
				"Part 2 ControlStream @Test methods missing groups=\"" + PART2_CONTROLSTREAM_GROUP + "\": " + offenders,
				offenders.isEmpty());
	}

	/**
	 * Sprint 22 S-ETS-22-01: Part 2 ControlStream classes MUST be co-located in the SAME
	 * {@code <test>} block as Core and Common.
	 */
	@org.junit.Test
	public void testPart2ControlStreamCoLocatedWithCoreAndCommon() throws Exception {
		XmlSuite suite = parseShippedSuite();
		Set<String> coreClassNames = new HashSet<>();
		for (Class<?> c : CORE_CLASSES) {
			coreClassNames.add(c.getName());
		}
		Set<String> commonClassNames = new HashSet<>();
		for (Class<?> c : COMMON_CLASSES) {
			commonClassNames.add(c.getName());
		}
		Set<String> part2ControlStreamClassNames = new HashSet<>();
		for (Class<?> c : PART2_CONTROLSTREAM_CLASSES) {
			part2ControlStreamClassNames.add(c.getName());
		}

		boolean coAlloc = false;
		for (XmlTest xt : suite.getTests()) {
			Set<String> xtClasses = new HashSet<>();
			for (XmlClass xc : xt.getXmlClasses()) {
				xtClasses.add(xc.getName());
			}
			boolean hasAllCore = xtClasses.containsAll(coreClassNames);
			boolean hasAllCommon = xtClasses.containsAll(commonClassNames);
			boolean hasAnyPart2ControlStream = !java.util.Collections.disjoint(xtClasses, part2ControlStreamClassNames);
			if (hasAllCore && hasAllCommon && hasAnyPart2ControlStream) {
				coAlloc = true;
				break;
			}
		}
		assertTrue(
				"Core (" + coreClassNames + "), Common (" + commonClassNames + "), and Part 2 ControlStream ("
						+ part2ControlStreamClassNames
						+ ") must be declared in the SAME <test> block of testng.xml so the group dependency "
						+ "(Part2ControlStream → Core + Common) resolves within scope. See Sprint 22 S-ETS-22-01.",
				coAlloc);
	}

	// ===== Sprint 23 S-ETS-23-01 — Part 2 Feasibility group =====
	// Feasibility depends on Core and Common, not Part2ControlStream, so the class can
	// declaration-SKIP before public-IUT POST behavior is even reachable. Runtime checks
	// still make /req/controlstream prerequisite closure explicit.

	/**
	 * Sprint 23 S-ETS-23-01 (REQ-ETS-PART2-004): the canonical testng.xml SHALL declare
	 * {@code <group name="part2feasibility" depends-on="core common"/>}.
	 */
	@org.junit.Test
	public void testPart2FeasibilityGroupDependsOnCoreAndCommon() throws Exception {
		XmlSuite suite = parseShippedSuite();
		assertFalse("Expected at least one <test> block in testng.xml", suite.getTests().isEmpty());

		boolean foundDependency = false;
		for (XmlTest xt : suite.getTests()) {
			java.util.Map<String, String> deps = xt.getXmlDependencyGroups();
			if (deps != null && deps.containsKey(PART2_FEASIBILITY_GROUP)) {
				String dependsOn = deps.get(PART2_FEASIBILITY_GROUP);
				assertNotNull("group '" + PART2_FEASIBILITY_GROUP + "' has null depends-on attribute", dependsOn);
				assertFalse(
						"group '" + PART2_FEASIBILITY_GROUP + "' depends-on '" + dependsOn
								+ "' uses comma syntax, which TestNG treats as a nonexistent single group at runtime",
						dependsOn.contains(","));
				Set<String> dependencyTokens = dependencyTokens(dependsOn);
				assertTrue("group '" + PART2_FEASIBILITY_GROUP + "' depends-on '" + dependsOn + "' missing '"
						+ CORE_GROUP + "'", dependencyTokens.contains(CORE_GROUP));
				assertTrue("group '" + PART2_FEASIBILITY_GROUP + "' depends-on '" + dependsOn + "' missing '"
						+ COMMON_GROUP + "'", dependencyTokens.contains(COMMON_GROUP));
				assertFalse("group '" + PART2_FEASIBILITY_GROUP
						+ "' must not depend on part2controlstream; otherwise an undeclared /conf/feasibility class can cascade-SKIP before its own declaration/no-POST guard reports the reason",
						dependencyTokens.contains(PART2_CONTROLSTREAM_GROUP));
				foundDependency = true;
				break;
			}
		}
		assertTrue("testng.xml does not declare <group name=\"" + PART2_FEASIBILITY_GROUP
				+ "\" depends-on=\"core common\"/> — see Sprint 23 S-ETS-23-01.", foundDependency);
	}

	/**
	 * Sprint 23 S-ETS-23-01: every Part 2 Feasibility @Test method SHALL carry
	 * {@code groups = "part2feasibility"}.
	 */
	@org.junit.Test
	public void testEveryPart2FeasibilityTestMethodCarriesPart2FeasibilityGroup() {
		List<String> offenders = new ArrayList<>();
		int totalPart2Feasibility = 0;
		for (Class<?> c : PART2_FEASIBILITY_CLASSES) {
			for (Method m : c.getDeclaredMethods()) {
				Test ann = m.getAnnotation(Test.class);
				if (ann == null) {
					continue;
				}
				totalPart2Feasibility++;
				List<String> groups = java.util.Arrays.asList(ann.groups());
				if (!groups.contains(PART2_FEASIBILITY_GROUP)) {
					offenders.add(c.getSimpleName() + "#" + m.getName() + " (groups=" + groups + ")");
				}
			}
		}
		assertTrue("Expected at least one @Test method in Part 2 Feasibility conformance classes; found 0",
				totalPart2Feasibility > 0);
		assertTrue("Part 2 Feasibility @Test methods missing groups=\"" + PART2_FEASIBILITY_GROUP + "\": " + offenders,
				offenders.isEmpty());
	}

	/**
	 * Sprint 23 S-ETS-23-01: Part 2 Feasibility classes MUST be co-located in the SAME
	 * {@code <test>} block as Core and Common.
	 */
	@org.junit.Test
	public void testPart2FeasibilityCoLocatedWithCoreAndCommon() throws Exception {
		XmlSuite suite = parseShippedSuite();
		Set<String> coreClassNames = new HashSet<>();
		for (Class<?> c : CORE_CLASSES) {
			coreClassNames.add(c.getName());
		}
		Set<String> commonClassNames = new HashSet<>();
		for (Class<?> c : COMMON_CLASSES) {
			commonClassNames.add(c.getName());
		}
		Set<String> part2FeasibilityClassNames = new HashSet<>();
		for (Class<?> c : PART2_FEASIBILITY_CLASSES) {
			part2FeasibilityClassNames.add(c.getName());
		}

		boolean coAlloc = false;
		for (XmlTest xt : suite.getTests()) {
			Set<String> xtClasses = new HashSet<>();
			for (XmlClass xc : xt.getXmlClasses()) {
				xtClasses.add(xc.getName());
			}
			boolean hasAllCore = xtClasses.containsAll(coreClassNames);
			boolean hasAllCommon = xtClasses.containsAll(commonClassNames);
			boolean hasAnyPart2Feasibility = !java.util.Collections.disjoint(xtClasses, part2FeasibilityClassNames);
			if (hasAllCore && hasAllCommon && hasAnyPart2Feasibility) {
				coAlloc = true;
				break;
			}
		}
		assertTrue(
				"Core (" + coreClassNames + "), Common (" + commonClassNames + "), and Part 2 Feasibility ("
						+ part2FeasibilityClassNames
						+ ") must be declared in the SAME <test> block of testng.xml so the group dependency "
						+ "(Part2Feasibility → Core + Common) resolves within scope. See Sprint 23 S-ETS-23-01.",
				coAlloc);
	}

	private Set<String> dependencyTokens(String dependsOn) {
		Set<String> tokens = new HashSet<>();
		if (dependsOn == null) {
			return tokens;
		}
		for (String token : dependsOn.split("\\s+")) {
			if (!token.isBlank()) {
				tokens.add(token);
			}
		}
		return tokens;
	}

}
