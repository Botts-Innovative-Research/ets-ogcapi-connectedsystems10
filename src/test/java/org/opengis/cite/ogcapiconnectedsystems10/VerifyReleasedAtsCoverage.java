package org.opengis.cite.ogcapiconnectedsystems10;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Structural gate for REQ-ETS-COVERAGE-001.
 */
public class VerifyReleasedAtsCoverage {

	private static final Path INVENTORY = Path.of("src/main/resources/org/opengis/cite/ogcapiconnectedsystems10/ats",
			"released-ats-inventory.json");

	private static final Path REVIEWED_MAPPINGS = Path
		.of("src/main/resources/org/opengis/cite/ogcapiconnectedsystems10/ats", "reviewed-ats-mappings.json");

	private static final Path COVERAGE_REPORT = Path.of("ops/ats-coverage-report.json");

	private static final ObjectMapper JSON = new ObjectMapper();

	private static final Map<String, String> REQUIREMENT_CLASSES = Map.ofEntries(
			Map.entry("REQ-ETS-PART1-001", "1:/conf/api-common"), Map.entry("REQ-ETS-PART1-002", "1:/conf/system"),
			Map.entry("REQ-ETS-PART1-003", "1:/conf/subsystem"), Map.entry("REQ-ETS-PART1-004", "1:/conf/deployment"),
			Map.entry("REQ-ETS-PART1-005", "1:/conf/subdeployment"),
			Map.entry("REQ-ETS-PART1-006", "1:/conf/procedure"), Map.entry("REQ-ETS-PART1-007", "1:/conf/sf"),
			Map.entry("REQ-ETS-PART1-008", "1:/conf/property"),
			Map.entry("REQ-ETS-PART1-009", "1:/conf/advanced-filtering"),
			Map.entry("REQ-ETS-PART1-010", "1:/conf/create-replace-delete"),
			Map.entry("REQ-ETS-PART1-011", "1:/conf/update"), Map.entry("REQ-ETS-PART1-012", "1:/conf/geojson"),
			Map.entry("REQ-ETS-PART1-013", "1:/conf/sensorml"), Map.entry("REQ-ETS-PART2-001", "2:/conf/api-common"),
			Map.entry("REQ-ETS-PART2-002", "2:/conf/datastream"),
			Map.entry("REQ-ETS-PART2-003", "2:/conf/controlstream"),
			Map.entry("REQ-ETS-PART2-004", "2:/conf/feasibility"),
			Map.entry("REQ-ETS-PART2-005", "2:/conf/system-event"),
			Map.entry("REQ-ETS-PART2-006", "2:/conf/advanced-filtering"),
			Map.entry("REQ-ETS-PART2-007", "2:/conf/create-replace-delete"),
			Map.entry("REQ-ETS-PART2-008", "2:/conf/update"), Map.entry("REQ-ETS-PART2-009", "2:/conf/json"),
			Map.entry("REQ-ETS-PART2-010", "2:/conf/swecommon-json"),
			Map.entry("REQ-ETS-PART2-011", "2:/conf/swecommon-text"),
			Map.entry("REQ-ETS-PART2-012", "2:/conf/swecommon-binary"));

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-RELEASED-SOURCES-001.
	 */
	@Test
	public void releasedInventoryPinsApprovedSource() throws IOException {
		assertTrue("Released ATS inventory is missing", Files.isRegularFile(INVENTORY));
		JsonNode root = JSON.readTree(INVENTORY.toFile());
		assertEquals("1.0", root.path("schemaVersion").asText());
		assertEquals("v1.0.0", root.path("source").path("tag").asText());
		assertEquals("8e03b236a049849f2ccc24b4fd9fdce5ff69bed2", root.path("source").path("commit").asText());
		assertEquals("https://github.com/opengeospatial/ogcapi-connected-systems.git",
				root.path("source").path("repository").asText());
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-EXACT-INVENTORY-001.
	 */
	@Test
	public void releasedInventoryHasExactSemanticCounts() throws IOException {
		assertTrue("Released ATS inventory is missing", Files.isRegularFile(INVENTORY));
		JsonNode root = JSON.readTree(INVENTORY.toFile());
		assertPartCounts(root, 1, 13, 110, 2);
		assertPartCounts(root, 2, 12, 130, 0);

		Set<String> classKeys = new HashSet<>();
		for (JsonNode item : root.path("classes")) {
			assertTrue("Duplicate class key", classKeys.add(item.path("part").asInt() + ":" + item.path("identifier")));
		}
		Set<String> testKeys = new HashSet<>();
		for (JsonNode item : root.path("tests")) {
			assertTrue("Duplicate test key", testKeys.add(item.path("part").asInt() + ":" + item.path("identifier")));
		}
		assertEquals(25, classKeys.size());
		assertEquals(240, testKeys.size());
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-COMPILED-MAPPING-001,
	 * SCENARIO-ETS-COVERAGE-STATUS-HONESTY-001.
	 */
	@Test
	public void coverageReportMatchesCompiledTestNgMetadata() throws Exception {
		assertTrue("Reviewed ATS mapping file is missing", Files.isRegularFile(REVIEWED_MAPPINGS));

		JsonNode inventory = JSON.readTree(INVENTORY.toFile());
		JsonNode reviewed = JSON.readTree(REVIEWED_MAPPINGS.toFile());
		JsonNode actual = ReleasedAtsCoverage.buildReport(inventory, reviewed, loadSuiteClasses());
		if (Boolean.getBoolean("ats.coverage.report.update")) {
			JSON.writerWithDefaultPrettyPrinter().writeValue(COVERAGE_REPORT.toFile(), actual);
			return;
		}
		assertTrue("ATS coverage report is missing", Files.isRegularFile(COVERAGE_REPORT));
		JsonNode expected = JSON.readTree(COVERAGE_REPORT.toFile());
		assertEquals("Committed ATS coverage report is stale; run the documented update command", expected, actual);
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-FAIL-CLOSED-001.
	 */
	@Test
	public void reviewedMappingsResolveToKnownMethods() throws Exception {
		assertTrue("Reviewed ATS mapping file is missing", Files.isRegularFile(REVIEWED_MAPPINGS));
		JsonNode inventory = JSON.readTree(INVENTORY.toFile());
		JsonNode reviewed = JSON.readTree(REVIEWED_MAPPINGS.toFile());
		JsonNode report = ReleasedAtsCoverage.buildReport(inventory, reviewed, loadSuiteClasses());
		int reviewedCount = report.path("summary").path("exact").asInt()
				+ report.path("summary").path("helper").asInt();
		assertEquals(reviewed.path("mappings").size(), reviewedCount);
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-FAIL-CLOSED-001.
	 */
	@Test
	public void duplicateInventoryTestFailsClosed() {
		ObjectNode inventory = inventoryWithOneClassTest();
		inventory.withArray("tests").add(inventory.path("tests").get(0).deepCopy());
		IllegalArgumentException error = org.junit.Assert.assertThrows(IllegalArgumentException.class,
				() -> ReleasedAtsCoverage.buildReport(inventory, emptyReviewedMappings(), Set.of()));
		assertTrue(error.getMessage(), error.getMessage().contains("Duplicate inventory test"));
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-FAIL-CLOSED-001.
	 */
	@Test
	public void unknownReviewedAtsTestFailsClosed() {
		ObjectNode reviewed = emptyReviewedMappings();
		reviewed.withArray("mappings")
			.addObject()
			.put("part", 1)
			.put("test", "/conf/system/not-released")
			.put("kind", "exact")
			.put("implementation", UndeployedFixture.class.getName() + "#canonicalTest");
		IllegalArgumentException error = org.junit.Assert.assertThrows(IllegalArgumentException.class,
				() -> ReleasedAtsCoverage.buildReport(inventoryWithOneClassTest(), reviewed, Set.of()));
		assertTrue(error.getMessage(), error.getMessage().contains("unknown ATS test"));
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-FAIL-CLOSED-001.
	 */
	@Test
	public void exactMappingOutsideDeployedSuiteFailsClosed() {
		ObjectNode reviewed = emptyReviewedMappings();
		reviewed.withArray("mappings").add(reviewedMapping("/conf/system/canonical-test", "canonicalTest"));
		IllegalArgumentException error = org.junit.Assert.assertThrows(IllegalArgumentException.class,
				() -> ReleasedAtsCoverage.buildReport(inventoryWithOneClassTest(), reviewed, Set.of()));
		assertTrue(error.getMessage(), error.getMessage().contains("not deployed by the TestNG suite"));
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-FAIL-CLOSED-001.
	 */
	@Test
	public void exactMappingRequiresReviewProvenance() {
		ObjectNode reviewed = emptyReviewedMappings();
		reviewed.withArray("mappings")
			.addObject()
			.put("part", 1)
			.put("test", "/conf/system/canonical-test")
			.put("kind", "exact")
			.put("implementation", UndeployedFixture.class.getName() + "#canonicalTest");
		IllegalArgumentException error = org.junit.Assert.assertThrows(IllegalArgumentException.class,
				() -> ReleasedAtsCoverage.buildReport(inventoryWithOneClassTest(), reviewed,
						Set.of(UndeployedFixture.class)));
		assertTrue(error.getMessage(), error.getMessage().contains("review provenance"));
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-FAIL-CLOSED-001.
	 */
	@Test
	public void implementationCannotClaimTwoReleasedTests() {
		ObjectNode inventory = inventoryWithOneClassTest();
		inventory.withArray("tests")
			.addObject()
			.put("part", 1)
			.put("identifier", "/conf/system/second-test")
			.put("classIdentifier", "/conf/system")
			.put("target", "/req/system/canonical-test")
			.put("fullTarget", "http://www.opengis.net/spec/ogcapi-connected-systems-1/1.0/req/system/canonical-test")
			.put("supporting", false);
		ObjectNode reviewed = emptyReviewedMappings();
		reviewed.withArray("mappings").add(reviewedMapping("/conf/system/canonical-test", "canonicalTest"));
		reviewed.withArray("mappings").add(reviewedMapping("/conf/system/second-test", "canonicalTest"));
		IllegalArgumentException error = org.junit.Assert.assertThrows(IllegalArgumentException.class,
				() -> ReleasedAtsCoverage.buildReport(inventory, reviewed, Set.of(UndeployedFixture.class)));
		assertTrue(error.getMessage(), error.getMessage().contains("claims multiple ATS tests"));
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-COMPILED-MAPPING-001.
	 */
	@Test
	public void targetPrefixIsNotACandidateMapping() {
		ObjectNode inventory = inventoryWithOneClassTest();
		ObjectNode test = (ObjectNode) inventory.withArray("tests").get(0);
		test.put("target", "/req/system/canonical");
		test.put("fullTarget", "http://www.opengis.net/spec/ogcapi-connected-systems-1/1.0/req/system/canonical");
		JsonNode report = ReleasedAtsCoverage.buildReport(inventory, emptyReviewedMappings(),
				Set.of(UndeployedFixture.class));
		assertEquals(0, report.path("summary").path("candidate").asInt());
		assertEquals(1, report.path("summary").path("unmapped").asInt());
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-COMPILED-MAPPING-001.
	 */
	@Test
	public void disabledTestIsNotADeployedCandidate() {
		JsonNode report = ReleasedAtsCoverage.buildReport(inventoryWithOneClassTest(), emptyReviewedMappings(),
				Set.of(DisabledFixture.class));
		assertEquals(0, report.path("summary").path("candidate").asInt());
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-COMPILED-MAPPING-001.
	 */
	@Test
	public void inheritedTestUsesUnambiguousSuiteClassIdentity() {
		JsonNode report = ReleasedAtsCoverage.buildReport(inventoryWithOneClassTest(), emptyReviewedMappings(),
				Set.of(InheritedFixture.class));
		JsonNode candidates = report.path("tests").get(0).path("candidateMappings");
		assertEquals(1, candidates.size());
		assertEquals(InheritedFixture.class.getName() + "#canonicalTest()", candidates.get(0).asText());
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-COMPILED-MAPPING-001.
	 */
	@Test
	public void overloadedTestsHaveDistinctSignatureIdentities() {
		JsonNode report = ReleasedAtsCoverage.buildReport(inventoryWithOneClassTest(), emptyReviewedMappings(),
				Set.of(OverloadedFixture.class));
		Set<String> candidates = new HashSet<>();
		report.path("tests").get(0).path("candidateMappings").forEach(node -> candidates.add(node.asText()));
		assertEquals(Set.of(OverloadedFixture.class.getName() + "#canonicalTest()",
				OverloadedFixture.class.getName() + "#canonicalTest(java.lang.String)"), candidates);
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-FAIL-CLOSED-001.
	 */
	@Test
	public void classLevelTestAnnotationFailsClosed() {
		IllegalArgumentException error = org.junit.Assert.assertThrows(IllegalArgumentException.class,
				() -> ReleasedAtsCoverage.buildReport(inventoryWithOneClassTest(), emptyReviewedMappings(),
						Set.of(ClassLevelFixture.class)));
		assertTrue(error.getMessage(), error.getMessage().contains("class-level"));
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-FAIL-CLOSED-001.
	 */
	@Test
	public void inheritedClassLevelTestAnnotationFailsClosed() {
		IllegalArgumentException error = org.junit.Assert.assertThrows(IllegalArgumentException.class,
				() -> ReleasedAtsCoverage.buildReport(inventoryWithOneClassTest(), emptyReviewedMappings(),
						Set.of(InheritedClassLevelFixture.class)));
		assertTrue(error.getMessage(), error.getMessage().contains("class-level"));
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-FAIL-CLOSED-001.
	 */
	@Test
	public void testNgFactoryFailsClosed() {
		IllegalArgumentException error = org.junit.Assert.assertThrows(IllegalArgumentException.class,
				() -> ReleasedAtsCoverage.buildReport(inventoryWithOneClassTest(), emptyReviewedMappings(),
						Set.of(FactoryFixture.class)));
		assertTrue(error.getMessage(), error.getMessage().contains("factory"));
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-FAIL-CLOSED-001.
	 */
	@Test
	public void testNgConstructorFactoryFailsClosed() {
		IllegalArgumentException error = org.junit.Assert.assertThrows(IllegalArgumentException.class,
				() -> ReleasedAtsCoverage.buildReport(inventoryWithOneClassTest(), emptyReviewedMappings(),
						Set.of(FactoryConstructorFixture.class)));
		assertTrue(error.getMessage(), error.getMessage().contains("factory"));
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-FAIL-CLOSED-001.
	 */
	@Test
	public void ignoredMethodFailsClosed() {
		IllegalArgumentException error = org.junit.Assert.assertThrows(IllegalArgumentException.class,
				() -> ReleasedAtsCoverage.buildReport(inventoryWithOneClassTest(), emptyReviewedMappings(),
						Set.of(IgnoredMethodFixture.class)));
		assertTrue(error.getMessage(), error.getMessage().contains("@Ignore"));
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-FAIL-CLOSED-001.
	 */
	@Test
	public void ignoredClassFailsClosed() {
		IllegalArgumentException error = org.junit.Assert.assertThrows(IllegalArgumentException.class,
				() -> ReleasedAtsCoverage.buildReport(inventoryWithOneClassTest(), emptyReviewedMappings(),
						Set.of(IgnoredClassFixture.class)));
		assertTrue(error.getMessage(), error.getMessage().contains("@Ignore"));
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-FAIL-CLOSED-001.
	 */
	@Test
	public void ignoredPackageFailsClosed() {
		IllegalArgumentException error = org.junit.Assert.assertThrows(IllegalArgumentException.class,
				() -> ReleasedAtsCoverage.buildReport(inventoryWithOneClassTest(), emptyReviewedMappings(), Set
					.of(org.opengis.cite.ogcapiconnectedsystems10.coveragefixtures.ignoredpackage.IgnoredPackageFixture.class)));
		assertTrue(error.getMessage(), error.getMessage().contains("@Ignore"));
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-FAIL-CLOSED-001.
	 */
	@Test
	public void arbitrarySupportingHelperFailsClosed() {
		ObjectNode inventory = inventoryWithSupportingTest();
		ObjectNode reviewed = emptyReviewedMappings();
		ObjectNode mapping = reviewed.withArray("mappings").addObject();
		mapping.put("part", 1);
		mapping.put("test", "/conf/api-common/supporting-test");
		mapping.put("kind", "helper");
		mapping.put("implementation", HelperFixture.class.getName() + "#helper()");
		addReviewProvenance(mapping);
		IllegalArgumentException error = org.junit.Assert.assertThrows(IllegalArgumentException.class,
				() -> ReleasedAtsCoverage.buildReport(inventory, reviewed, Set.of()));
		assertTrue(error.getMessage(), error.getMessage().contains("approved helper"));
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-FAIL-CLOSED-001.
	 */
	@Test
	public void suiteMethodFilterFailsClosed() throws IOException {
		Path suite = Files.createTempFile("ats-filtered-suite", ".xml");
		try {
			Files.writeString(suite,
					"<suite name=\"fixture\"><test name=\"filtered\"><groups><run><include name=\"core\"/></run></groups>"
							+ "<classes><class name=\"" + UndeployedFixture.class.getName()
							+ "\"/></classes></test></suite>");
			IllegalArgumentException error = org.junit.Assert.assertThrows(IllegalArgumentException.class,
					() -> loadSuiteClasses(suite));
			assertTrue(error.getMessage(), error.getMessage().contains("filters"));
		}
		finally {
			Files.deleteIfExists(suite);
		}
	}

	/**
	 * REQ-ETS-COVERAGE-001; SCENARIO-ETS-COVERAGE-STATUS-HONESTY-001.
	 */
	@Test
	public void activeSpecCannotClaimUnreviewedClassImplemented() throws IOException {
		JsonNode report = JSON.readTree(COVERAGE_REPORT.toFile());
		JsonNode byClass = report.path("summary").path("byClass");
		Pattern header = Pattern.compile("^#### (REQ-ETS-PART[12]-\\d{3}):");
		String activeRequirement = null;
		for (String line : Files.readAllLines(Path.of("openspec/capabilities/ets-ogcapi-connectedsystems/spec.md"))) {
			Matcher matcher = header.matcher(line);
			if (matcher.find()) {
				activeRequirement = matcher.group(1);
			}
			else if (activeRequirement != null && line.startsWith("- **Status**:")) {
				String status = line.substring(line.indexOf(':') + 1).trim();
				String classKey = REQUIREMENT_CLASSES.get(activeRequirement);
				if (classKey != null && status.toUpperCase(java.util.Locale.ROOT).contains("IMPLEMENTED")) {
					JsonNode totals = byClass.path(classKey);
					int reviewed = totals.path("exact").asInt() + totals.path("helper").asInt();
					assertEquals(activeRequirement + " cannot claim implementation without complete reviewed coverage",
							totals.path("total").asInt(), reviewed);
				}
				activeRequirement = null;
			}
		}
	}

	private static void assertPartCounts(JsonNode root, int part, int classes, int tests, int supporting) {
		JsonNode metadata = null;
		for (JsonNode candidate : root.path("source").path("parts")) {
			if (candidate.path("part").asInt() == part) {
				metadata = candidate;
				break;
			}
		}
		assertNotNull("Missing source metadata for Part " + part, metadata);
		assertEquals(classes, metadata.path("classCount").asInt());
		assertEquals(tests, metadata.path("testCount").asInt());
		assertEquals(supporting, metadata.path("supportingTestCount").asInt());

		int actualClasses = 0;
		int actualTests = 0;
		int actualSupporting = 0;
		for (JsonNode item : root.path("classes")) {
			if (item.path("part").asInt() == part) {
				actualClasses++;
			}
		}
		for (JsonNode item : root.path("tests")) {
			if (item.path("part").asInt() == part) {
				actualTests++;
				if (item.path("supporting").asBoolean()) {
					actualSupporting++;
				}
			}
		}
		assertEquals(classes, actualClasses);
		assertEquals(tests, actualTests);
		assertEquals(supporting, actualSupporting);
	}

	private static Set<Class<?>> loadSuiteClasses() throws Exception {
		return loadSuiteClasses(Path.of("src/main/resources/org/opengis/cite/ogcapiconnectedsystems10/testng.xml"));
	}

	private static Set<Class<?>> loadSuiteClasses(Path suite) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		Document document = factory.newDocumentBuilder().parse(suite.toFile());
		if (document.getElementsByTagName("run").getLength() > 0
				|| document.getElementsByTagName("methods").getLength() > 0
				|| document.getElementsByTagName("packages").getLength() > 0
				|| document.getElementsByTagName("method-selectors").getLength() > 0) {
			throw new IllegalArgumentException("TestNG suite filters or package selectors are not supported");
		}
		NodeList classes = document.getElementsByTagName("class");
		Set<Class<?>> result = new HashSet<>();
		for (int i = 0; i < classes.getLength(); i++) {
			String name = classes.item(i).getAttributes().getNamedItem("name").getNodeValue();
			result.add(Class.forName(name));
		}
		assertFalse("No TestNG suite classes found", result.isEmpty());
		return result;
	}

	private static ObjectNode inventoryWithOneClassTest() {
		ObjectNode inventory = JSON.createObjectNode();
		inventory.putObject("source").put("commit", "fixture");
		ArrayNode tests = inventory.putArray("tests");
		tests.addObject()
			.put("part", 1)
			.put("identifier", "/conf/system/canonical-test")
			.put("classIdentifier", "/conf/system")
			.put("target", "/req/system/canonical-test")
			.put("fullTarget", "http://www.opengis.net/spec/ogcapi-connected-systems-1/1.0/req/system/canonical-test")
			.put("supporting", false);
		return inventory;
	}

	private static ObjectNode emptyReviewedMappings() {
		ObjectNode reviewed = JSON.createObjectNode();
		reviewed.put("schemaVersion", "1.0");
		reviewed.putArray("approvedHelpers");
		reviewed.putArray("mappings");
		return reviewed;
	}

	private static ObjectNode reviewedMapping(String test, String method) {
		ObjectNode mapping = JSON.createObjectNode();
		mapping.put("part", 1);
		mapping.put("test", test);
		mapping.put("kind", "exact");
		mapping.put("implementation", UndeployedFixture.class.getName() + "#" + method + "()");
		addReviewProvenance(mapping);
		return mapping;
	}

	private static void addReviewProvenance(ObjectNode mapping) {
		mapping.put("reviewedBy", "fixture-reviewer");
		mapping.put("reviewedOn", "2026-07-26");
		mapping.put("evidence", "Fixture verifies the complete abstract test method.");
	}

	private static ObjectNode inventoryWithSupportingTest() {
		ObjectNode inventory = JSON.createObjectNode();
		inventory.putObject("source").put("commit", "fixture");
		inventory.putArray("tests")
			.addObject()
			.put("part", 1)
			.put("identifier", "/conf/api-common/supporting-test")
			.putNull("classIdentifier")
			.putNull("target")
			.putNull("fullTarget")
			.put("supporting", true);
		return inventory;
	}

	private static class BaseFixture {

		@org.testng.annotations.Test(
				description = "http://www.opengis.net/spec/ogcapi-connected-systems-1/1.0/req/system/canonical-test")
		void canonicalTest() {
		}

	}

	private static final class UndeployedFixture extends BaseFixture {

	}

	private static final class InheritedFixture extends BaseFixture {

	}

	private static final class DisabledFixture {

		@org.testng.annotations.Test(enabled = false,
				description = "http://www.opengis.net/spec/ogcapi-connected-systems-1/1.0/req/system/canonical-test")
		void canonicalTest() {
		}

	}

	private static final class OverloadedFixture {

		@org.testng.annotations.Test(
				description = "http://www.opengis.net/spec/ogcapi-connected-systems-1/1.0/req/system/canonical-test")
		void canonicalTest() {
		}

		@org.testng.annotations.Test(
				description = "http://www.opengis.net/spec/ogcapi-connected-systems-1/1.0/req/system/canonical-test")
		void canonicalTest(String value) {
		}

	}

	@org.testng.annotations.Test
	private static class ClassLevelFixture {

		void canonicalTest() {
		}

	}

	private static final class InheritedClassLevelFixture extends ClassLevelFixture {

	}

	private static final class FactoryFixture {

		@org.testng.annotations.Factory
		Object[] create() {
			return new Object[0];
		}

	}

	private static final class FactoryConstructorFixture {

		@org.testng.annotations.Factory
		FactoryConstructorFixture() {
		}

	}

	private static final class IgnoredMethodFixture {

		@org.testng.annotations.Ignore
		@org.testng.annotations.Test(
				description = "http://www.opengis.net/spec/ogcapi-connected-systems-1/1.0/req/system/canonical-test")
		void canonicalTest() {
		}

	}

	@org.testng.annotations.Ignore
	private static final class IgnoredClassFixture {

		@org.testng.annotations.Test(
				description = "http://www.opengis.net/spec/ogcapi-connected-systems-1/1.0/req/system/canonical-test")
		void canonicalTest() {
		}

	}

	private static final class HelperFixture {

		static void helper() {
		}

	}

}
