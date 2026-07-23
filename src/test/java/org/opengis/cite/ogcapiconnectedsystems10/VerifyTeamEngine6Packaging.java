package org.opengis.cite.ogcapiconnectedsystems10;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;

/** Structural regression checks for the TeamEngine 6 packaging boundary. */
public class VerifyTeamEngine6Packaging {

	private static String dockerfile;

	private static String compose;

	private static String pom;

	private static String testngXml;

	private static String ctl;

	private static String teamengineConfig;

	private static String readme;

	private static String siteIndex;

	private static String siteHowTo;

	private static String siteChangelog;

	private static String javadocOverview;

	private static String configTestRunProps;

	private static String resourcesTestRunProps;

	private static String smokeScript;

	private static String sweCommonBootstrapScript;

	private static String runtimeVerifier;

	private static String addedJarVerifier;

	private static String addedJarVerifierSelfTest;

	private static String jenkinsBuild;

	private static String jenkinsRelease;

	@BeforeClass
	public static void loadPackagingFiles() throws IOException {
		dockerfile = Files.readString(Path.of("Dockerfile"));
		compose = Files.readString(Path.of("docker-compose.yml"));
		pom = Files.readString(Path.of("pom.xml"));
		testngXml = Files
			.readString(Path.of("src/main/resources/org/opengis/cite/ogcapiconnectedsystems10/testng.xml"));
		ctl = Files.readString(Path.of("src/main/scripts/ctl/ogcapi-connectedsystems10-suite.ctl"));
		teamengineConfig = Files.readString(Path.of("src/main/config/teamengine/config.xml"));
		readme = Files.readString(Path.of("README.adoc"));
		siteIndex = Files.readString(Path.of("src/site/asciidoc/index.adoc"));
		siteHowTo = Files.readString(Path.of("src/site/asciidoc/how-to-run-the-tests.adoc"));
		siteChangelog = Files.readString(Path.of("src/site/asciidoc/changelog.adoc"));
		javadocOverview = Files.readString(Path.of("src/main/javadoc/overview.html"));
		configTestRunProps = Files.readString(Path.of("src/main/config/test-run-props.xml"));
		resourcesTestRunProps = Files.readString(Path.of("src/main/resources/test-run-props.xml"));
		smokeScript = Files.readString(Path.of("scripts/smoke-test.sh"));
		sweCommonBootstrapScript = Files.readString(Path.of("scripts/bootstrap-swecommon30-validator.sh"));
		runtimeVerifier = Files.readString(Path.of("scripts/verify-teamengine6-runtime.sh"));
		addedJarVerifier = Files.readString(Path.of("scripts/verify-added-jar-inventory.sh"));
		addedJarVerifierSelfTest = Files.readString(Path.of("scripts/test-teamengine6-jar-guard.sh"));
		jenkinsBuild = Files.readString(Path.of("jenkinsfiles/build/Jenkinsfile"));
		jenkinsRelease = Files.readString(Path.of("jenkinsfiles/release/Jenkinsfile"));
	}

	/**
	 * REQ-ETS-TEAMENGINE-007; SCENARIO-ETS-TEAMENGINE-TE6-IMAGE-PROVENANCE-001.
	 */
	@Test
	public void runtimeUsesPinnedTeamEngine6Image() {
		assertTrue(dockerfile.contains(
				"FROM ogccite/teamengine-dev@sha256:981b71566d56434576843798ae8072db15be8478eb7dc724b051c2228460f43c"));
		assertTrue(pom.contains("<teamengine.runtime.version>6.0.0</teamengine.runtime.version>"));
		assertTrue(pom.contains(
				"<teamengine.runtime.image.digest>sha256:981b71566d56434576843798ae8072db15be8478eb7dc724b051c2228460f43c</teamengine.runtime.image.digest>"));
	}

	/**
	 * REQ-ETS-TEAMENGINE-003, REQ-ETS-TEAMENGINE-007;
	 * SCENARIO-ETS-TEAMENGINE-TE6-BASE-IMMUTABILITY-001.
	 */
	@Test
	public void runtimeDoesNotModifyTeamEngineOwnedFiles() {
		String logicalDockerfile = dockerfile.replace("\\\n", " ");
		assertFalse(dockerfile.contains("USER root"));
		assertFalse(logicalDockerfile.matches(
				"(?s).*(?m:^RUN\\s+[^\\r\\n]*(?:sed|chmod|chown|rm)\\s+[^\\r\\n]*(?:webapps/teamengine|te_base)[^\\r\\n]*$).*"));
		assertFalse(dockerfile.contains("chown -R"));
		assertFalse(dockerfile.contains("teamengine-*.jar"));
		assertTrue(dockerfile.contains("USER tomcat"));
		assertContains(runtimeVerifier, "teamengine_base_inventory");
		assertContains(runtimeVerifier, "%y|%m|%U|%G|%p|%l");
		assertContains(runtimeVerifier, "-type f -print0");
		assertContains(runtimeVerifier, "sha256sum --");
	}

	/**
	 * REQ-ETS-TEAMENGINE-003; SCENARIO-ETS-TEAMENGINE-TE6-DEPENDENCY-INVENTORY-001.
	 */
	@Test
	public void dependencyExclusionsAreExplicit() {
		assertFalse(dockerfile.contains("rm -f target/lib-runtime/teamengine-*.jar"));
		assertFalse(dockerfile.contains("/build/target/lib-runtime/ /usr/local/tomcat"));
		assertFalse(dockerfile.contains("target/lib-runtime-selected/"));
		assertFalse(dockerfile.contains("org.opengis.cite.teamengine:teamengine-resources:6.0.0"));
		assertFalse(dockerfile.contains("teamengine-resources-6.0.0.jar"));
		assertTrue(runtimeVerifier.contains("runtime_jar_inventory"));
		assertTrue(runtimeVerifier.contains("FINAL_IMAGE_ID="));
		assertFalse(runtimeVerifier.contains("[ \"$name\" != \"teamengine-resources-6.0.0.jar\" ]"));
		assertTrue(smokeScript.contains("FINAL_IMAGE_ID="));
		assertTrue(pom.contains("<artifactId>maven-shade-plugin</artifactId>"));
		assertTrue(pom.contains("org.opengis.cite.ogcapiconnectedsystems10.internal.networknt.schema"));
		assertFalse(dockerfile.contains("json-schema-validator-1.5.9.jar"));
		assertFalse(dockerfile.contains("guava-31.0.1-jre.jar"));
		assertFalse(pom.contains("dependency/*teamengine-*.war"));
		assertFalse(pom.contains("<artifactId>teamengine-web</artifactId>"));
		assertFalse(pom.contains("<artifactId>teamengine-console</artifactId>"));
	}

	/**
	 * REQ-ETS-TEAMENGINE-007; SCENARIO-ETS-TEAMENGINE-TE6-DEPENDENCY-INVENTORY-001.
	 */
	@Test
	public void runtimeJarGuardIsFilenameIndependentAndResourceIsolated() {
		assertContains(runtimeVerifier, "runtime_jar_inventory");
		assertContains(runtimeVerifier, "verify_added_jar_inventory");
		assertContains(runtimeVerifier, "teamengine6-functional-path-allowlist.txt");
		assertContains(addedJarVerifier, "pom.properties");
		assertContains(addedJarVerifier, "coordinate_path");
		assertContains(addedJarVerifier, "functional-path collision");
		assertContains(addedJarVerifier, "ALLOWED_COLLISION|");
		assertContains(addedJarVerifier, "unused allowlist entry");
		assertContains(addedJarVerifierSelfTest,
				"ALLOWED_COLLISION|example:application-service|META-INF/services/example.Controller");
		assertContains(addedJarVerifierSelfTest, "ALLOWED_COLLISION|example:application|test-run-props.xml");
		assertContains(addedJarVerifierSelfTest, "actual_output=\"");
		assertContains(addedJarVerifierSelfTest, "if [ \"$actual_output\" != \"$expected_output\" ]");
		assertContains(runtimeVerifier, "test-teamengine6-jar-guard.sh");
		assertContains(pom, "<pattern>jsv-messages</pattern>");
		assertContains(pom, "internal.networknt.schema.i18n.jsv-messages");
		assertContains(pom, "<exclude>jsv-messages*.properties</exclude>");
		assertContains(pom, "<exclude>META-INF/versions/**/module-info.class</exclude>");
	}

	/**
	 * REQ-ETS-VALIDATOR-001; SCENARIO-ETS-VALIDATOR-SOURCE-PIN-001,
	 * SCENARIO-ETS-VALIDATOR-DIAGNOSTICS-BOUNDARY-001,
	 * SCENARIO-ETS-VALIDATOR-RUNTIME-EXECUTION-001.
	 */
	@Test
	public void sweCommonValidatorUsesPinnedIsolatedRuntimeBoundary() {
		assertContains(sweCommonBootstrapScript, "https://github.com/opengeospatial/ets-swecommon30.git");
		assertContains(sweCommonBootstrapScript, "3ba75ceabe57cea85f4a8513c59e0f90e386ba96");
		assertContains(sweCommonBootstrapScript, "rev-parse FETCH_HEAD");
		assertContains(sweCommonBootstrapScript, "-pl swecommon30-validator -am");
		assertFalse(sweCommonBootstrapScript.contains("-pl swecommon30-ets"));

		assertContains(pom, "<artifactId>swecommon30-validator</artifactId>");
		assertContains(pom, "<swecommon30.validator.version>0.1-SNAPSHOT</swecommon30.validator.version>");
		assertContains(pom, "<include>org.opengis.cite:swecommon30-validator</include>");
		assertContains(pom, "<groupId>com.networknt</groupId>");
		assertContains(pom, "<groupId>com.fasterxml.jackson.core</groupId>");
		assertContains(pom, "<artifactId>jackson-databind</artifactId>");

		assertContains(dockerfile, "COPY scripts/bootstrap-swecommon30-validator.sh ./scripts/");
		assertContains(dockerfile, "bash scripts/bootstrap-swecommon30-validator.sh");
		assertFalse(dockerfile.contains("swecommon30-validator-0.1-SNAPSHOT.jar"));
		assertContains(jenkinsBuild, "bash scripts/bootstrap-swecommon30-validator.sh");
		assertContains(jenkinsBuild, "jdk 'JDK 17'");
		assertFalse(jenkinsBuild.contains("jdk 'JDK 8'"));

		assertContains(runtimeVerifier, "SweCommonJsonSchemaValidator.class");
		assertContains(runtimeVerifier, "sweCommon.json");
		assertContains(runtimeVerifier, "BaseJsonSchemaValidatorTest.class");
		assertContains(addedJarVerifier, "added jar duplicates a base Maven coordinate family");
		assertContains(runtimeVerifier, "swecommon30-validator-*.jar");
		assertContains(runtimeVerifier, "SweValidatorRuntimeProbe");
		assertContains(runtimeVerifier, "PASS: deployed SWE Common adapter executed valid and invalid components");
	}

	/**
	 * REQ-ETS-TEAMENGINE-004, REQ-ETS-TEAMENGINE-007;
	 * SCENARIO-ETS-TEAMENGINE-TE6-CONFIG-ALIGNMENT-001.
	 */
	@Test
	public void composeInheritsRuntimeConfiguration() {
		assertFalse(compose.contains("JAVA_OPTS:"));
		assertFalse(compose.contains("CATALINA_OPTS:"));
		assertTrue(compose.contains("8081:8080"));
		assertTrue(compose.contains("http://localhost:8080/teamengine/"));
	}

	/**
	 * REQ-ETS-TEAMENGINE-002, REQ-ETS-TEAMENGINE-008;
	 * SCENARIO-ETS-TEAMENGINE-RUN-ARG-CONTRACT-001.
	 */
	@Test
	public void publicRunArgumentContractIsCanonical() {
		assertContains(testngXml, "name=\"iut\"");
		assertContains(testngXml, "name=\"auth-credential\"");
		assertContains(testngXml, "name=\"mutation-tests-enabled\"");
		assertContains(testngXml, "name=\"mutation-iut-policy\"");
		assertDoesNotExposeUnsupportedRunArgs("testng.xml", testngXml);

		assertContains(ctl, "<entry key=\"iut\">");
		assertContains(ctl, "<entry key=\"auth-credential\">");
		assertContains(ctl, "<entry key=\"mutation-tests-enabled\">");
		assertContains(ctl, "<entry key=\"mutation-iut-policy\">");
		assertDoesNotExposeUnsupportedRunArgs("CTL", ctl);

		assertContains(smokeScript, "--data-urlencode \"iut=${IUT_URL}\"");
		assertContains(smokeScript, "--data-urlencode \"auth-credential=${SMOKE_AUTH_CREDENTIAL}\"");
		assertContains(smokeScript, "--data-urlencode \"mutation-tests-enabled=${SMOKE_MUTATION_TESTS_ENABLED}\"");
		assertContains(smokeScript, "--data-urlencode \"mutation-iut-policy=${SMOKE_MUTATION_IUT_POLICY}\"");
		assertContains(smokeScript,
				"EXPECTED_TITLE_FRAGMENT=\"${SMOKE_EXPECTED_TITLE_FRAGMENT:-OGC API - Connected Systems 1.0 Conformance Test Suite}\"");
		assertDoesNotExposeUnsupportedRunArgs("scripts/smoke-test.sh", smokeScript);

		Map<String, String> publicRunArgDocs = Map.of("README.adoc", readme, "site index", siteIndex, "site how-to",
				siteHowTo, "Javadoc overview", javadocOverview, "config test-run-props", configTestRunProps,
				"resources test-run-props", resourcesTestRunProps);
		publicRunArgDocs.forEach((name, content) -> {
			assertContains(name, content, "iut");
			assertContains(name, content, "auth-credential");
			assertContains(name, content, "mutation-tests-enabled");
			assertContains(name, content, "mutation-iut-policy");
			assertDoesNotExposeUnsupportedRunArgs(name, content);
		});
	}

	/**
	 * REQ-ETS-TEAMENGINE-008; SCENARIO-ETS-TEAMENGINE-PUBLIC-METADATA-001.
	 */
	@Test
	public void publicMetadataDoesNotContainArchetypePlaceholders() {
		Map<String, String> publicMetadata = Map.ofEntries(Map.entry("POM", pom), Map.entry("Dockerfile", dockerfile),
				Map.entry("docker-compose.yml", compose), Map.entry("CTL", ctl),
				Map.entry("TeamEngine config", teamengineConfig), Map.entry("README.adoc", readme),
				Map.entry("site index", siteIndex), Map.entry("site how-to", siteHowTo),
				Map.entry("site changelog", siteChangelog), Map.entry("Javadoc overview", javadocOverview),
				Map.entry("config test-run-props", configTestRunProps),
				Map.entry("resources test-run-props", resourcesTestRunProps));
		publicMetadata.forEach((name, content) -> {
			assertContains(name, content, "OGC API - Connected Systems");
			assertContains(name, content, "Part 1");
			assertContains(name, content, "Part 2");
			assertFalse(name + " still contains archetype Class A placeholder", content.contains("Class A"));
			assertFalse(name + " still contains archetype Class B placeholder", content.contains("Class B"));
			assertFalse(name + " still contains W3Schools sample IUT", content.toLowerCase().contains("w3schools"));
			assertFalse(name + " still contains XML Base placeholder", content.contains("XML Base"));
			assertFalse(name + " still contains WCAG placeholder", content.contains("WCAG"));
			assertFalse(name + " still contains generic scope placeholder",
					content.toLowerCase().contains("describe the scope"));
		});
	}

	/**
	 * REQ-ETS-TEAMENGINE-007, REQ-ETS-TEAMENGINE-008;
	 * SCENARIO-ETS-TEAMENGINE-MAVEN-DOCKER-PROFILE-001.
	 */
	@Test
	public void mavenDockerProfileDoesNotDefineIndependentRuntime() {
		assertFalse(pom.contains("<id>docker</id>"));
		assertFalse(pom.contains("<artifactId>docker-maven-plugin</artifactId>"));
		assertFalse(pom.contains("<goal>copy-dependencies</goal>"));
		assertFalse(compose.contains("docker.teamengine.version"));
		Map<String, String> jenkinsfiles = Map.of("build", jenkinsBuild, "release", jenkinsRelease);
		Pattern profileArgument = Pattern.compile("(?:^|\\s)-P([^\\s'\"]+)");
		jenkinsfiles.forEach((name, content) -> {
			assertContains(name, content, "jdk 'JDK 17'");
			assertContains(name, content, "bash scripts/bootstrap-swecommon30-validator.sh");
			assertFalse(name + " requests the removed docker profile", content.contains("docker"));
			Matcher matcher = profileArgument.matcher(content);
			while (matcher.find()) {
				for (String profile : matcher.group(1).split(",")) {
					assertContains(name + " requests an undeclared Maven profile", pom, "<id>" + profile + "</id>");
				}
			}
		});
	}

	/**
	 * REQ-ETS-TEAMENGINE-007; SCENARIO-ETS-TEAMENGINE-TE6-BASE-IMMUTABILITY-001.
	 */
	@Test
	public void ctlInstallationRejectsBasePathCollisions() {
		assertTrue(dockerfile.contains("test ! -e /usr/local/tomcat/te_base/scripts/ogcapi-connectedsystems10"));
		assertFalse(dockerfile.contains("unzip -q -o"));
		assertTrue(dockerfile.contains("test -d /tmp/ets-ctl/ogcapi-connectedsystems10"));
	}

	/**
	 * REQ-ETS-SCOPE-002; SCENARIO-ETS-SCOPE-HOSTED-CI-NONGOAL-001.
	 */
	@Test
	public void repositoryDoesNotDefineHostedCiWorkflows() throws IOException {
		Path ciDirectory = Path.of("ci");
		if (Files.isDirectory(ciDirectory)) {
			try (var entries = Files.walk(ciDirectory)) {
				assertFalse(entries.anyMatch(Files::isRegularFile));
			}
		}
		Path workflowDirectory = Path.of(".github/workflows");
		if (Files.isDirectory(workflowDirectory)) {
			try (var entries = Files.walk(workflowDirectory)) {
				assertFalse(entries.anyMatch(Files::isRegularFile));
			}
		}

		Map<String, String> activePlanningSurfaces = Map.ofEntries(Map.entry("Dockerfile", dockerfile),
				Map.entry("product brief", Files.readString(Path.of("_bmad/product-brief.md"))),
				Map.entry("ADR-009", Files.readString(Path.of("_bmad/adrs/ADR-009-multi-stage-dockerfile.md"))),
				Map.entry("ADR-010",
						Files.readString(Path.of("_bmad/adrs/ADR-010-dependency-skip-verification-strategy.md"))),
				Map.entry("Sprint 2 story",
						Files.readString(Path.of("epics/stories/s-ets-02-05-dockerfile-cleanup.md"))),
				Map.entry("Sprint 1 scaffold story",
						Files.readString(Path.of("epics/stories/s-ets-01-01-archetype-jdk17-build.md"))),
				Map.entry("Sprint 1 runtime story",
						Files.readString(Path.of("epics/stories/s-ets-01-03-teamengine-docker-smoke.md"))),
				Map.entry("Sprint 3 story", Files.readString(Path.of("epics/stories/s-ets-03-03-ci-workflow-live.md"))),
				Map.entry("Sprint 4 story",
						Files.readString(Path.of("epics/stories/s-ets-04-01-ci-workflow-escalation.md"))),
				Map.entry("operational status", Files.readString(Path.of("ops/status.md"))),
				Map.entry("planner handoff", Files.readString(Path.of(".harness/handoffs/planner-handoff.yaml"))),
				Map.entry("generator handoff", Files.readString(Path.of(".harness/handoffs/generator-handoff.yaml"))));
		Pattern activationInstruction = Pattern.compile(
				"(?i)(gh auth refresh -s workflow|git mv ci/github-workflows-build\\.yml|gh workflow run|workflow_dispatch run|test suite already ran in CI|GitHub Actions build matrix|runs this script as .*workflow)");
		activePlanningSurfaces
			.forEach((name, content) -> assertFalse(name + " retains a hosted-CI activation instruction",
					activationInstruction.matcher(content).find()));
	}

	/**
	 * REQ-ETS-CLEANUP-021; SCENARIO-ETS-CLEANUP-CONFIDENTIAL-BUILD-CONTEXT-001.
	 */
	@Test
	public void dockerfileProvidesEffectiveContextAuditStage() {
		assertTrue(dockerfile.contains("AS build-context-audit"));
		assertTrue(dockerfile.contains("COPY . /context"));
	}

	private static void assertContains(String content, String expected) {
		assertTrue(content.contains(expected));
	}

	private static void assertContains(String name, String content, String expected) {
		assertTrue(name + " does not contain expected text: " + expected, content.contains(expected));
	}

	private static void assertDoesNotExposeUnsupportedRunArgs(String name, String content) {
		assertFalse(name + " exposes iut-url", content.contains("iut-url"));
		assertFalse(name + " exposes auth-type", content.contains("auth-type"));
		assertFalse(name + " exposes ics entry", content.contains("key=\"ics\""));
		assertFalse(name + " exposes ics parameter", content.contains("name=\"ics\""));
		assertFalse(name + " documents ics as code", content.contains("`ics`"));
	}

}
