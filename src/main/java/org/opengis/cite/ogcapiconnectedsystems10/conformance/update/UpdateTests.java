package org.opengis.cite.ogcapiconnectedsystems10.conformance.update;

import java.net.URI;

import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Released OGC 23-001 Part 1 Update conformance procedures.
 */
public class UpdateTests {

	static final String GROUP = "update";

	static final String REQ = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/update/";

	private URI apiRoot;

	private String mutationTestsEnabled;

	private String mutationIutPolicy;

	/**
	 * Loads immutable run arguments after the direct released prerequisite.
	 * @param testContext current TestNG context.
	 */
	@BeforeClass(dependsOnGroups = "part1apicommon")
	public void fetchUpdateArguments(ITestContext testContext) {
		Object iut = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iut instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		String value = iut.toString();
		this.apiRoot = URI.create(value.endsWith("/") ? value : value + "/");
		this.mutationTestsEnabled = suiteString(testContext, SuiteAttribute.MUTATION_TESTS_ENABLED);
		this.mutationIutPolicy = suiteString(testContext, SuiteAttribute.MUTATION_IUT_POLICY);
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-001 " + REQ + "system", groups = GROUP, dependsOnGroups = "part1apicommon")
	public void systemsUpdate() {
		support().systemsUpdate();
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-001 " + REQ + "deployment", groups = GROUP, dependsOnGroups = "part1apicommon")
	public void deploymentsUpdate() {
		support().deploymentsUpdate();
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-001 " + REQ + "procedure", groups = GROUP, dependsOnGroups = "part1apicommon")
	public void proceduresUpdate() {
		support().proceduresUpdate();
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-001 " + REQ + "sampling-feature", groups = GROUP, dependsOnGroups = "part1apicommon")
	public void samplingFeaturesUpdate() {
		support().samplingFeaturesUpdate();
	}

	/**
	 * REQ-ETS-PART1-011; SCENARIO-ETS-PART1-011-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-001 " + REQ + "property", groups = GROUP, dependsOnGroups = "part1apicommon")
	public void propertiesUpdate() {
		support().propertiesUpdate();
	}

	private UpdateSupport support() {
		return new UpdateSupport(this.apiRoot, this.mutationTestsEnabled, this.mutationIutPolicy);
	}

	private static String suiteString(ITestContext testContext, SuiteAttribute attribute) {
		Object value = testContext.getSuite().getAttribute(attribute.getName());
		return value instanceof String ? (String) value : null;
	}

}
