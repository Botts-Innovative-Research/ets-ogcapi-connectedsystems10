package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.advancedfiltering;

import java.net.URI;

import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.apicommon.Part2ApiCommonTests;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Released OGC 23-002 Part 2 `/conf/advanced-filtering` conformance procedures.
 */
public class Part2AdvancedFilteringTests {

	static final String GROUP = "part2advancedfiltering";

	static final String CONF_ADVANCED_FILTERING = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/advanced-filtering";

	static final String CONF_PART2_API_COMMON = Part2ApiCommonTests.CONF_PART2_API_COMMON;

	static final String CONF_PART1_ADVANCED_FILTERING = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/advanced-filtering";

	static final String CONF_DATASTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/datastream";

	static final String CONF_CONTROLSTREAM = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/controlstream";

	static final String CONF_SYSTEM_EVENT = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/system-event";

	static final String REQ_ADVANCED_FILTERING = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/advanced-filtering";

	static final String REQ_DATASTREAM_PHENOMENON_TIME = REQ_ADVANCED_FILTERING + "/datastream-by-phenomenontime";

	static final String REQ_DATASTREAM_RESULT_TIME = REQ_ADVANCED_FILTERING + "/datastream-by-resulttime";

	static final String REQ_DATASTREAM_OBS_PROP = REQ_ADVANCED_FILTERING + "/datastream-by-obsprop";

	static final String REQ_DATASTREAM_FOI = REQ_ADVANCED_FILTERING + "/datastream-by-foi";

	static final String REQ_OBS_PHENOMENON_TIME = REQ_ADVANCED_FILTERING + "/obs-by-phenomenontime";

	static final String REQ_OBS_RESULT_TIME = REQ_ADVANCED_FILTERING + "/obs-by-resulttime";

	static final String REQ_OBS_FOI = REQ_ADVANCED_FILTERING + "/obs-by-foi";

	static final String REQ_CONTROLSTREAM_ISSUE_TIME = REQ_ADVANCED_FILTERING + "/controlstream-by-issuetime";

	static final String REQ_CONTROLSTREAM_EXEC_TIME = REQ_ADVANCED_FILTERING + "/controlstream-by-exectime";

	static final String REQ_CONTROLSTREAM_CONTROL_PROP = REQ_ADVANCED_FILTERING + "/controlstream-by-controlprop";

	static final String REQ_CONTROLSTREAM_FOI = REQ_ADVANCED_FILTERING + "/controlstream-by-foi";

	static final String REQ_COMMAND_ISSUE_TIME = REQ_ADVANCED_FILTERING + "/cmd-by-issuetime";

	static final String REQ_COMMAND_EXEC_TIME = REQ_ADVANCED_FILTERING + "/cmd-by-exectime";

	static final String REQ_COMMAND_STATUS = REQ_ADVANCED_FILTERING + "/cmd-by-status";

	static final String REQ_COMMAND_SENDER = REQ_ADVANCED_FILTERING + "/cmd-by-sender";

	static final String REQ_COMMAND_FOI = REQ_ADVANCED_FILTERING + "/cmd-by-foi";

	static final String REQ_STATUS_STATUS_CODE = REQ_ADVANCED_FILTERING + "/status-by-statuscode";

	static final String REQ_SYSTEM_EVENT_TYPE = REQ_ADVANCED_FILTERING + "/event-by-type";

	static final String RELEASED_SCENARIO = "SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001";

	private Part2AdvancedFilteringSupport support;

	/**
	 * Loads immutable read-only inputs only after inherited prerequisite groups settle.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass(dependsOnGroups = { "part2apicommon", "advancedfiltering" }, alwaysRun = true)
	public void fetchPart2AdvancedFilteringInputs(ITestContext testContext) {
		Part2AdvancedFilteringSupport.skipWhenPrerequisiteUnsatisfied(testContext);
		Object iut = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iut instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		this.support = Part2AdvancedFilteringSupport.fromIut((URI) iut);
	}

	/**
	 * REQ-ETS-PART2-006; SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_DATASTREAM_PHENOMENON_TIME
			+ ": DataStream phenomenonTime filters return only intersecting DataStreams (REQ-ETS-PART2-006, "
			+ RELEASED_SCENARIO + ")", groups = GROUP, alwaysRun = true)
	public void datastreamsFilterByPhenomenonTime() {
		support().datastreamsFilterByPhenomenonTime(REQ_DATASTREAM_PHENOMENON_TIME);
	}

	/**
	 * REQ-ETS-PART2-006; SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_DATASTREAM_RESULT_TIME
			+ ": DataStream resultTime filters return only intersecting DataStreams (REQ-ETS-PART2-006, "
			+ RELEASED_SCENARIO + ")", groups = GROUP, alwaysRun = true)
	public void datastreamsFilterByResultTime() {
		support().datastreamsFilterByResultTime(REQ_DATASTREAM_RESULT_TIME);
	}

	/**
	 * REQ-ETS-PART2-006; SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_DATASTREAM_OBS_PROP
			+ ": DataStream observedProperty filters match local or URI property identifiers (REQ-ETS-PART2-006, "
			+ RELEASED_SCENARIO + ")", groups = GROUP, alwaysRun = true)
	public void datastreamsFilterByObservedProperty() {
		support().datastreamsFilterByObservedProperty(REQ_DATASTREAM_OBS_PROP);
	}

	/**
	 * REQ-ETS-PART2-006; SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_DATASTREAM_FOI
			+ ": DataStream foi filters match sampling or ultimate feature identifiers (REQ-ETS-PART2-006, "
			+ RELEASED_SCENARIO + ")", groups = GROUP, alwaysRun = true)
	public void datastreamsFilterByFeatureOfInterest() {
		support().datastreamsFilterByFeatureOfInterest(REQ_DATASTREAM_FOI);
	}

	/**
	 * REQ-ETS-PART2-006; SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_OBS_PHENOMENON_TIME
			+ ": Observation phenomenonTime filters return only intersecting Observations (REQ-ETS-PART2-006, "
			+ RELEASED_SCENARIO + ")", groups = GROUP, alwaysRun = true)
	public void observationsFilterByPhenomenonTime() {
		support().observationsFilterByPhenomenonTime(REQ_OBS_PHENOMENON_TIME);
	}

	/**
	 * REQ-ETS-PART2-006; SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_OBS_RESULT_TIME
			+ ": Observation resultTime filters return matching datetime and latest results (REQ-ETS-PART2-006, "
			+ RELEASED_SCENARIO + ")", groups = GROUP, alwaysRun = true)
	public void observationsFilterByResultTime() {
		support().observationsFilterByResultTime(REQ_OBS_RESULT_TIME);
	}

	/**
	 * REQ-ETS-PART2-006; SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_OBS_FOI
			+ ": Observation foi filters match sampling feature identifiers (REQ-ETS-PART2-006, " + RELEASED_SCENARIO
			+ ")", groups = GROUP, alwaysRun = true)
	public void observationsFilterByFeatureOfInterest() {
		support().observationsFilterByFeatureOfInterest(REQ_OBS_FOI);
	}

	/**
	 * REQ-ETS-PART2-006; SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CONTROLSTREAM_ISSUE_TIME
			+ ": ControlStream issueTime filters return only intersecting ControlStreams (REQ-ETS-PART2-006, "
			+ RELEASED_SCENARIO + ")", groups = GROUP, alwaysRun = true)
	public void controlstreamsFilterByIssueTime() {
		support().controlstreamsFilterByIssueTime(REQ_CONTROLSTREAM_ISSUE_TIME);
	}

	/**
	 * REQ-ETS-PART2-006; SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CONTROLSTREAM_EXEC_TIME
			+ ": ControlStream executionTime filters return only intersecting ControlStreams (REQ-ETS-PART2-006, "
			+ RELEASED_SCENARIO + ")", groups = GROUP, alwaysRun = true)
	public void controlstreamsFilterByExecutionTime() {
		support().controlstreamsFilterByExecutionTime(REQ_CONTROLSTREAM_EXEC_TIME);
	}

	/**
	 * REQ-ETS-PART2-006; SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CONTROLSTREAM_CONTROL_PROP
			+ ": ControlStream controlledProperty filters match local or URI property identifiers (REQ-ETS-PART2-006, "
			+ RELEASED_SCENARIO + ")", groups = GROUP, alwaysRun = true)
	public void controlstreamsFilterByControlledProperty() {
		support().controlstreamsFilterByControlledProperty(REQ_CONTROLSTREAM_CONTROL_PROP);
	}

	/**
	 * REQ-ETS-PART2-006; SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_CONTROLSTREAM_FOI
			+ ": ControlStream foi filters match sampling or ultimate feature identifiers (REQ-ETS-PART2-006, "
			+ RELEASED_SCENARIO + ")", groups = GROUP, alwaysRun = true)
	public void controlstreamsFilterByFeatureOfInterest() {
		support().controlstreamsFilterByFeatureOfInterest(REQ_CONTROLSTREAM_FOI);
	}

	/**
	 * REQ-ETS-PART2-006; SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COMMAND_ISSUE_TIME
			+ ": Command issueTime filters return only matching Commands at root and nested endpoints (REQ-ETS-PART2-006, "
			+ RELEASED_SCENARIO + ")", groups = GROUP, alwaysRun = true)
	public void commandsFilterByIssueTime() {
		support().commandsFilterByIssueTime(REQ_COMMAND_ISSUE_TIME);
	}

	/**
	 * REQ-ETS-PART2-006; SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COMMAND_EXEC_TIME
			+ ": Command executionTime filters return only matching Commands at root and nested endpoints (REQ-ETS-PART2-006, "
			+ RELEASED_SCENARIO + ")", groups = GROUP, alwaysRun = true)
	public void commandsFilterByExecutionTime() {
		support().commandsFilterByExecutionTime(REQ_COMMAND_EXEC_TIME);
	}

	/**
	 * REQ-ETS-PART2-006; SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COMMAND_STATUS
			+ ": Command statusCode filters return only matching Commands at root and nested endpoints (REQ-ETS-PART2-006, "
			+ RELEASED_SCENARIO + ")", groups = GROUP, alwaysRun = true)
	public void commandsFilterByStatusCode() {
		support().commandsFilterByStatusCode(REQ_COMMAND_STATUS);
	}

	/**
	 * REQ-ETS-PART2-006; SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COMMAND_SENDER
			+ ": Command sender filters return only matching Commands at root and nested endpoints (REQ-ETS-PART2-006, "
			+ RELEASED_SCENARIO + ")", groups = GROUP, alwaysRun = true)
	public void commandsFilterBySender() {
		support().commandsFilterBySender(REQ_COMMAND_SENDER);
	}

	/**
	 * REQ-ETS-PART2-006; SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_COMMAND_FOI
			+ ": Command foi filters return only matching Commands at root and nested endpoints (REQ-ETS-PART2-006, "
			+ RELEASED_SCENARIO + ")", groups = GROUP, alwaysRun = true)
	public void commandsFilterByFeatureOfInterest() {
		support().commandsFilterByFeatureOfInterest(REQ_COMMAND_FOI);
	}

	/**
	 * REQ-ETS-PART2-006; SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_STATUS_STATUS_CODE
			+ ": Command status endpoints filter statusCode values for every Command (REQ-ETS-PART2-006, "
			+ RELEASED_SCENARIO + ")", groups = GROUP, alwaysRun = true)
	public void commandStatusFilterByStatusCode() {
		support().commandStatusFilterByStatusCode(REQ_STATUS_STATUS_CODE);
	}

	/**
	 * REQ-ETS-PART2-006; SCENARIO-ETS-PART2-006-RELEASED-PROCEDURES-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_SYSTEM_EVENT_TYPE
			+ ": SystemEvent eventType filters return only matching SystemEvents (REQ-ETS-PART2-006, "
			+ RELEASED_SCENARIO + ")", groups = GROUP, alwaysRun = true)
	public void systemEventsFilterByEventType() {
		support().systemEventsFilterByEventType(REQ_SYSTEM_EVENT_TYPE);
	}

	private Part2AdvancedFilteringSupport support() {
		if (this.support == null) {
			throw new IllegalStateException("Part 2 Advanced Filtering support was not configured.");
		}
		return this.support;
	}

}
