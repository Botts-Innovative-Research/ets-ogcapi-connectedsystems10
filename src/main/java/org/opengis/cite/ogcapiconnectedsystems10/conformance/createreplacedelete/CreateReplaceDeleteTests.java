package org.opengis.cite.ogcapiconnectedsystems10.conformance.createreplacedelete;

import java.net.URI;
import java.util.Map;

import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Released OGC 23-001 Create/Replace/Delete conformance procedures.
 */
public class CreateReplaceDeleteTests {

	static final String GROUP = "createreplacedelete";

	static final String REQ = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/create-replace-delete/";

	private URI apiRoot;

	private String mutationTestsEnabled;

	private String mutationIutPolicy;

	/**
	 * Loads immutable run arguments. Every procedure performs its own declaration,
	 * condition, safety, HTTP, and cleanup work.
	 * @param testContext current TestNG context.
	 */
	@BeforeClass(dependsOnGroups = "part1apicommon")
	public void fetchCreateReplaceDeleteArguments(ITestContext testContext) {
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
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-INHERITED-TRANSACTION-001.
	 */
	@Test(description = "OGC-23-001 " + REQ + "system", groups = GROUP, dependsOnGroups = "part1apicommon")
	public void systemsCreateReplaceDelete() {
		support().systemsCreateReplaceDelete();
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-CASCADE-001.
	 */
	@Test(description = "OGC-23-001 " + REQ + "system-delete-cascade", groups = GROUP,
			dependsOnGroups = "part1apicommon")
	public void systemDeleteCascade() {
		support().systemDeleteCascade();
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-NESTED-CANONICAL-001.
	 */
	@Test(description = "OGC-23-001 " + REQ + "subsystem", groups = GROUP, dependsOnGroups = "part1apicommon")
	public void subsystemsCreate() {
		support().subsystemsCreate();
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-INHERITED-TRANSACTION-001.
	 */
	@Test(description = "OGC-23-001 " + REQ + "deployment", groups = GROUP, dependsOnGroups = "part1apicommon")
	public void deploymentsCreateReplaceDelete() {
		support().deploymentsCreateReplaceDelete();
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-NESTED-CANONICAL-001.
	 */
	@Test(description = "OGC-23-001 " + REQ + "subdeployment", groups = GROUP, dependsOnGroups = "part1apicommon")
	public void subdeploymentsCreate() {
		support().subdeploymentsCreate();
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-INHERITED-TRANSACTION-001.
	 */
	@Test(description = "OGC-23-001 " + REQ + "procedure", groups = GROUP, dependsOnGroups = "part1apicommon")
	public void proceduresCreateReplaceDelete() {
		support().proceduresCreateReplaceDelete();
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-NESTED-CANONICAL-001.
	 */
	@Test(description = "OGC-23-001 " + REQ + "sampling-feature", groups = GROUP, dependsOnGroups = "part1apicommon")
	public void samplingFeaturesCreateReplaceDelete() {
		support().samplingFeaturesCreateReplaceDelete();
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-INHERITED-TRANSACTION-001.
	 */
	@Test(description = "OGC-23-001 " + REQ + "property", groups = GROUP, dependsOnGroups = "part1apicommon")
	public void propertiesCreateReplaceDelete() {
		support().propertiesCreateReplaceDelete();
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-CUSTOM-CREATE-001.
	 */
	@Test(description = "OGC-23-001 " + REQ + "create-in-collection", groups = GROUP,
			dependsOnGroups = "part1apicommon")
	public void resourcesCreateInCustomCollections() {
		support().resourcesCreateInCustomCollections();
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-CUSTOM-REPLACE-001.
	 */
	@Test(description = "OGC-23-001 " + REQ + "replace-in-collection", groups = GROUP,
			dependsOnGroups = "part1apicommon")
	public void resourcesReplaceInCustomCollections() {
		support().resourcesReplaceInCustomCollections();
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-CUSTOM-DELETE-001.
	 */
	@Test(description = "OGC-23-001 " + REQ + "delete-in-collection", groups = GROUP,
			dependsOnGroups = "part1apicommon")
	public void resourcesDeleteInCustomCollections() {
		support().resourcesDeleteInCustomCollections();
	}

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-CUSTOM-URI-LIST-001.
	 */
	@Test(description = "OGC-23-001 " + REQ + "add-to-collection", groups = GROUP, dependsOnGroups = "part1apicommon")
	public void resourcesAddToCustomCollections() {
		support().resourcesAddToCustomCollections();
	}

	private CreateReplaceDeleteSupport support() {
		return new CreateReplaceDeleteSupport(this.apiRoot, this.mutationTestsEnabled, this.mutationIutPolicy);
	}

	private String suiteString(ITestContext testContext, SuiteAttribute attribute) {
		Object value = testContext.getSuite().getAttribute(attribute.getName());
		return value instanceof String ? (String) value : null;
	}

	/**
	 * Compatibility entry point retained for the Sprint 12 regression.
	 * @param phase body phase.
	 * @param systemUid System UID.
	 * @return temporary GeoJSON System.
	 */
	public static Map<String, Object> mutableSystemBody(String phase, String systemUid) {
		return CreateReplaceDeleteSupport.systemBody(phase, systemUid);
	}

	/**
	 * Compatibility entry point retained for the Sprint 12 regression.
	 * @param iutUri IUT API root.
	 * @param base normalized API root string.
	 * @param location Location header.
	 * @return same-origin resource URI.
	 */
	public static String resolveCreatedResourceUri(URI iutUri, String base, String location) {
		return CreateReplaceDeleteSupport.resolveCreatedResourceUri(URI.create(base), location, REQ + "system")
			.toString();
	}

}
