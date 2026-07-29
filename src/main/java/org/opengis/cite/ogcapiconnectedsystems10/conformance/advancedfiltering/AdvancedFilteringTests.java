package org.opengis.cite.ogcapiconnectedsystems10.conformance.advancedfiltering;

import java.net.URI;

import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.advancedfiltering.AdvancedFilteringSupport.Relation;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.advancedfiltering.AdvancedFilteringSupport.ResourceType;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part1.apicommon.Part1ApiCommonTests;
import org.testng.IResultMap;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Released OGC 23-001 `/conf/advanced-filtering` conformance procedures.
 */
public class AdvancedFilteringTests {

	static final String GROUP = "advancedfiltering";

	static final String REQ_ID_LIST_SCHEMA = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/id-list-schema";

	static final String REQ_RESOURCE_BY_ID = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/resource-by-id";

	static final String REQ_RESOURCE_BY_KEYWORD = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/resource-by-keyword";

	static final String REC_RESOURCE_BY_PROPERTY = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/rec/advanced-filtering/resource-by-property";

	static final String REQ_FEATURE_BY_GEOM = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/feature-by-geom";

	static final String REQ_SYSTEM_BY_PARENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/system-by-parent";

	static final String REQ_SYSTEM_BY_PROCEDURE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/system-by-procedure";

	static final String REQ_SYSTEM_BY_FOI = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/system-by-foi";

	static final String REQ_SYSTEM_BY_OBSPROP = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/system-by-obsprop";

	static final String REQ_SYSTEM_BY_CONTROLPROP = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/system-by-controlprop";

	static final String REQ_DEPLOYMENT_BY_PARENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/deployment-by-parent";

	static final String REQ_DEPLOYMENT_BY_SYSTEM = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/deployment-by-system";

	static final String REQ_DEPLOYMENT_BY_FOI = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/deployment-by-foi";

	static final String REQ_DEPLOYMENT_BY_OBSPROP = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/deployment-by-obsprop";

	static final String REQ_DEPLOYMENT_BY_CONTROLPROP = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/deployment-by-controlprop";

	static final String REQ_PROCEDURE_BY_OBSPROP = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/procedure-by-obsprop";

	static final String REQ_PROCEDURE_BY_CONTROLPROP = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/procedure-by-controlprop";

	static final String REQ_SF_BY_FOI = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/sf-by-foi";

	static final String REQ_SF_BY_OBSPROP = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/sf-by-obsprop";

	static final String REQ_SF_BY_CONTROLPROP = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/sf-by-controlprop";

	static final String REQ_PROP_BY_BASEPROP = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/prop-by-baseprop";

	static final String REQ_PROP_BY_OBJECT = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/prop-by-object";

	static final String REQ_COMBINED_FILTERS = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/advanced-filtering/combined-filters";

	static final String REC_INDIRECT_PROP = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/rec/advanced-filtering/indirect-prop";

	static final String REC_INDIRECT_FOI = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/rec/advanced-filtering/indirect-foi";

	private static final String DATETIME_EVIDENCE_METHOD = "datetimeUsesValidTime";

	private URI apiRoot;

	/**
	 * Loads only the immutable API root after inherited API Common prerequisites.
	 * @param testContext active TestNG context.
	 */
	@BeforeClass(dependsOnGroups = "part1apicommon", alwaysRun = true)
	public void fetchAdvancedFilteringArguments(ITestContext testContext) {
		skipWhenPrerequisiteUnsatisfied(testContext);
		Object iut = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iut instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		configure((URI) iut);
	}

	void configure(URI iut) {
		if (iut == null || !iut.isAbsolute()) {
			throw new IllegalArgumentException("IUT must be an absolute URI.");
		}
		String value = iut.toString();
		this.apiRoot = URI.create(value.endsWith("/") ? value : value + "/");
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-ID-LIST-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_ID_LIST_SCHEMA
			+ ": query parameters of type ID List use valid homogeneous comma-separated values", groups = GROUP,
			alwaysRun = true)
	public void idListSchemaIsValid() {
		support().idListSchema(REQ_ID_LIST_SCHEMA);
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-COMMON-FILTERS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RESOURCE_BY_ID
			+ ": every supported canonical endpoint filters by local-ID lists and UID lists", groups = GROUP,
			alwaysRun = true)
	public void canonicalResourcesFilterById() {
		support().resourceById(REQ_RESOURCE_BY_ID);
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-COMMON-FILTERS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_RESOURCE_BY_KEYWORD
			+ ": every supported canonical endpoint returns only resources containing the selected keyword",
			groups = GROUP, alwaysRun = true)
	public void canonicalResourcesFilterByKeyword() {
		support().resourceByKeyword(REQ_RESOURCE_BY_KEYWORD);
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-COMMON-FILTERS-001.
	 */
	@Test(description = "OGC-23-001 " + REC_RESOURCE_BY_PROPERTY
			+ ": assess custom scalar property filtering on every supported canonical endpoint", groups = GROUP,
			alwaysRun = true)
	public void canonicalResourcesFilterByProperty() {
		support().resourceByProperty(REC_RESOURCE_BY_PROPERTY);
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-GEOMETRY-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_FEATURE_BY_GEOM
			+ ": systems, deployments, and samplingFeatures geometry results intersect the WKT query", groups = GROUP,
			alwaysRun = true)
	public void featuresFilterByGeometry() {
		support().featureByGeometry(REQ_FEATURE_BY_GEOM);
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-SYSTEM-ASSOCIATIONS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_SYSTEM_BY_PARENT
			+ ": System parent filters match parent System local IDs and UIDs", groups = GROUP, alwaysRun = true)
	public void systemsFilterByParent() {
		support().association(ResourceType.SYSTEMS, "parent", Relation.PARENT_SYSTEM, REQ_SYSTEM_BY_PARENT);
	}

	@Test(description = "OGC-23-001 " + REQ_SYSTEM_BY_PROCEDURE
			+ ": System procedure filters match Procedure local IDs and UIDs", groups = GROUP, alwaysRun = true)
	public void systemsFilterByProcedure() {
		support().association(ResourceType.SYSTEMS, "procedure", Relation.PROCEDURE, REQ_SYSTEM_BY_PROCEDURE);
	}

	@Test(description = "OGC-23-001 " + REQ_SYSTEM_BY_FOI
			+ ": System feature-of-interest filters follow recursive sampling-feature associations", groups = GROUP,
			alwaysRun = true)
	public void systemsFilterByFeatureOfInterest() {
		support().association(ResourceType.SYSTEMS, "foi", Relation.FEATURE_OF_INTEREST, REQ_SYSTEM_BY_FOI);
	}

	@Test(description = "OGC-23-001 " + REQ_SYSTEM_BY_OBSPROP
			+ ": System observedProperty filters include recursively nested subsystem properties", groups = GROUP,
			alwaysRun = true)
	public void systemsFilterByObservedProperty() {
		support().association(ResourceType.SYSTEMS, "observedProperty", Relation.OBSERVED_PROPERTY,
				REQ_SYSTEM_BY_OBSPROP);
	}

	@Test(description = "OGC-23-001 " + REQ_SYSTEM_BY_CONTROLPROP
			+ ": System controlledProperty filters include recursively nested subsystem properties", groups = GROUP,
			alwaysRun = true)
	public void systemsFilterByControlledProperty() {
		support().association(ResourceType.SYSTEMS, "controlledProperty", Relation.CONTROLLED_PROPERTY,
				REQ_SYSTEM_BY_CONTROLPROP);
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-DEPLOYMENT-ASSOCIATIONS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_DEPLOYMENT_BY_PARENT
			+ ": Deployment parent filters match parent Deployment local IDs and UIDs", groups = GROUP,
			alwaysRun = true)
	public void deploymentsFilterByParent() {
		support().association(ResourceType.DEPLOYMENTS, "parent", Relation.PARENT_DEPLOYMENT, REQ_DEPLOYMENT_BY_PARENT);
	}

	@Test(description = "OGC-23-001 " + REQ_DEPLOYMENT_BY_SYSTEM
			+ ": Deployment system filters match recursively deployed System local IDs and UIDs", groups = GROUP,
			alwaysRun = true)
	public void deploymentsFilterBySystem() {
		support().association(ResourceType.DEPLOYMENTS, "system", Relation.DEPLOYED_SYSTEM, REQ_DEPLOYMENT_BY_SYSTEM);
	}

	@Test(description = "OGC-23-001 " + REQ_DEPLOYMENT_BY_FOI
			+ ": Deployment feature-of-interest filters match associated Feature local IDs and UIDs", groups = GROUP,
			alwaysRun = true)
	public void deploymentsFilterByFeatureOfInterest() {
		support().association(ResourceType.DEPLOYMENTS, "foi", Relation.FEATURE_OF_INTEREST, REQ_DEPLOYMENT_BY_FOI);
	}

	@Test(description = "OGC-23-001 " + REQ_DEPLOYMENT_BY_OBSPROP
			+ ": Deployment observedProperty filters follow deployed Systems", groups = GROUP, alwaysRun = true)
	public void deploymentsFilterByObservedProperty() {
		support().association(ResourceType.DEPLOYMENTS, "observedProperty", Relation.OBSERVED_PROPERTY,
				REQ_DEPLOYMENT_BY_OBSPROP);
	}

	@Test(description = "OGC-23-001 " + REQ_DEPLOYMENT_BY_CONTROLPROP
			+ ": Deployment controlledProperty filters follow deployed Systems", groups = GROUP, alwaysRun = true)
	public void deploymentsFilterByControlledProperty() {
		support().association(ResourceType.DEPLOYMENTS, "controlledProperty", Relation.CONTROLLED_PROPERTY,
				REQ_DEPLOYMENT_BY_CONTROLPROP);
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-PROCEDURE-ASSOCIATIONS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_PROCEDURE_BY_OBSPROP
			+ ": Procedure observedProperty filters match referenced Property local IDs and UIDs", groups = GROUP,
			alwaysRun = true)
	public void proceduresFilterByObservedProperty() {
		support().association(ResourceType.PROCEDURES, "observedProperty", Relation.OBSERVED_PROPERTY,
				REQ_PROCEDURE_BY_OBSPROP);
	}

	@Test(description = "OGC-23-001 " + REQ_PROCEDURE_BY_CONTROLPROP
			+ ": Procedure controlledProperty filters match referenced Property local IDs and UIDs", groups = GROUP,
			alwaysRun = true)
	public void proceduresFilterByControlledProperty() {
		support().association(ResourceType.PROCEDURES, "controlledProperty", Relation.CONTROLLED_PROPERTY,
				REQ_PROCEDURE_BY_CONTROLPROP);
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-SF-ASSOCIATIONS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_SF_BY_FOI
			+ ": Sampling Feature foi filters follow recursive sampleOf associations", groups = GROUP, alwaysRun = true)
	public void samplingFeaturesFilterByFeatureOfInterest() {
		support().association(ResourceType.SAMPLING_FEATURES, "foi", Relation.FEATURE_OF_INTEREST, REQ_SF_BY_FOI);
	}

	@Test(description = "OGC-23-001 " + REQ_SF_BY_OBSPROP
			+ ": Sampling Feature observedProperty filters follow datastream associations", groups = GROUP,
			alwaysRun = true)
	public void samplingFeaturesFilterByObservedProperty() {
		support().association(ResourceType.SAMPLING_FEATURES, "observedProperty", Relation.OBSERVED_PROPERTY,
				REQ_SF_BY_OBSPROP);
	}

	@Test(description = "OGC-23-001 " + REQ_SF_BY_CONTROLPROP
			+ ": Sampling Feature controlledProperty filters follow controlstream associations", groups = GROUP,
			alwaysRun = true)
	public void samplingFeaturesFilterByControlledProperty() {
		support().association(ResourceType.SAMPLING_FEATURES, "controlledProperty", Relation.CONTROLLED_PROPERTY,
				REQ_SF_BY_CONTROLPROP);
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-PROPERTY-FILTERS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_PROP_BY_BASEPROP
			+ ": Property baseProperty filters follow recursive base-property associations", groups = GROUP,
			alwaysRun = true)
	public void propertiesFilterByBaseProperty() {
		support().association(ResourceType.PROPERTIES, "baseProperty", Relation.BASE_PROPERTY, REQ_PROP_BY_BASEPROP);
	}

	@Test(description = "OGC-23-001 " + REQ_PROP_BY_OBJECT
			+ ": Property objectType results contain only the selected object URI", groups = GROUP, alwaysRun = true)
	public void propertiesFilterByObjectType() {
		support().propertyByObjectType(REQ_PROP_BY_OBJECT);
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-COMBINED-FILTERS-001.
	 */
	@Test(description = "OGC-23-001 " + REQ_COMBINED_FILTERS
			+ ": combined canonical-resource filters apply logical AND", groups = GROUP, alwaysRun = true)
	public void canonicalResourcesCombineFilters() {
		support().combinedFilters(REQ_COMBINED_FILTERS);
	}

	/**
	 * REQ-ETS-PART1-009; released transitive recommendations.
	 */
	@Test(description = "OGC-23-001 " + REC_INDIRECT_PROP
			+ ": assess transitive baseProperty filtering across canonical endpoints", groups = GROUP, alwaysRun = true)
	public void indirectPropertyFiltersAreTransitive() {
		support().indirectProperty(REC_INDIRECT_PROP);
	}

	@Test(description = "OGC-23-001 " + REC_INDIRECT_FOI + ": assess transitive sampledFeature and sampleOf filtering",
			groups = GROUP, alwaysRun = true)
	public void indirectFeatureOfInterestFiltersAreTransitive() {
		support().indirectFeatureOfInterest(REC_INDIRECT_FOI);
	}

	private AdvancedFilteringSupport support() {
		if (this.apiRoot == null) {
			throw new IllegalStateException("Advanced Filtering API root was not configured.");
		}
		return new AdvancedFilteringSupport(this.apiRoot);
	}

	private static void skipWhenPrerequisiteUnsatisfied(ITestContext testContext) {
		String blocker = configurationBlocker(testContext.getFailedConfigurations(), "failed");
		if (blocker == null) {
			blocker = configurationBlocker(testContext.getSkippedConfigurations(), "skipped");
		}
		if (blocker == null) {
			blocker = testBlocker(testContext.getFailedTests(), "failed", false);
		}
		if (blocker == null) {
			blocker = testBlocker(testContext.getSkippedTests(), "skipped", true);
		}
		if (blocker != null) {
			throw new SkipException(
					"Advanced Filtering setup skipped before IUT access because prerequisite " + blocker + ".");
		}
	}

	private static String configurationBlocker(IResultMap results, String status) {
		if (results == null) {
			return null;
		}
		for (ITestResult result : results.getAllResults()) {
			if (result != null && result.getMethod() != null && isInheritedPrerequisite(result)) {
				return "configuration " + result.getMethod().getMethodName() + " " + status;
			}
		}
		return null;
	}

	private static String testBlocker(IResultMap results, String status, boolean allowDatetimeEvidenceLimitation) {
		if (results == null) {
			return null;
		}
		for (ITestResult result : results.getAllResults()) {
			if (result == null || result.getMethod() == null || !isInheritedPrerequisite(result)) {
				continue;
			}
			if (allowDatetimeEvidenceLimitation && DATETIME_EVIDENCE_METHOD.equals(result.getMethod().getMethodName())
					&& result.getThrowable() instanceof SkipException
					&& Part1ApiCommonTests.DATETIME_EVIDENCE_LIMITATION.equals(result.getThrowable().getMessage())) {
				Reporter.log(Part1ApiCommonTests.DATETIME_EVIDENCE_LIMITATION
						+ " Advanced Filtering direct procedures will execute, but inherited conformance remains incomplete.",
						true);
				continue;
			}
			return "method " + result.getMethod().getMethodName() + " " + status;
		}
		return null;
	}

	private static boolean isInheritedPrerequisite(ITestResult result) {
		for (String group : result.getMethod().getGroups()) {
			if ("core".equals(group) || "common".equals(group) || "part1apicommon".equals(group)) {
				return true;
			}
		}
		Class<?> realClass = result.getMethod().getRealClass();
		if (realClass == null) {
			return false;
		}
		String className = realClass.getName();
		return realClass == Part1ApiCommonTests.class
				|| className.startsWith("org.opengis.cite.ogcapiconnectedsystems10.conformance.core.")
				|| className.startsWith("org.opengis.cite.ogcapiconnectedsystems10.conformance.common.");
	}

}
