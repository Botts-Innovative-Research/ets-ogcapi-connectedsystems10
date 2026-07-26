package org.opengis.cite.ogcapiconnectedsystems10;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Structural contract for S-ETS-44-01.
 *
 * <p>
 * REQ-ETS-PART2-013; SCENARIO-ETS-PART2-013-EPHEMERAL-POPULATED-IUT-001,
 * SCENARIO-ETS-PART2-013-POPULATED-PROVISIONING-VERDICT-001,
 * SCENARIO-ETS-PART2-013-POPULATED-EVIDENCE-001, and
 * SCENARIO-ETS-PART2-013-PRIMARY-STATE-ISOLATION-001.
 */
public class VerifyLocalOshPopulatedE2e {

	private static final Path FIXTURES = Path.of("ops/local-osh-populated-fixtures.json");

	private static final Path SEEDER = Path.of("scripts/local-osh-populated-fixture.py");

	private static final Path ENTRYPOINT = Path.of("scripts/local-osh-populated-e2e.sh");

	private static final Path ORCHESTRATOR = Path.of("scripts/local_osh_populated_e2e.py");

	private static final Path BEHAVIOR_TEST = Path.of("scripts/test_local_osh_populated_workflow.py");

	private static final Path SMOKE = Path.of("scripts/smoke-test.sh");

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void fixtureManifestDefinesExactSupportedApiResources() throws IOException {
		assertTrue("missing exact populated fixture manifest", Files.isRegularFile(FIXTURES));
		JsonNode root = mapper.readTree(FIXTURES.toFile());
		assertEquals(4, root.path("staticFixtures").size());
		assertEquals("/systems/{systemId}/datastreams", root.at("/dynamicFixtures/dataStream/collection").asText());
		assertEquals("application/om+json", root.at("/dynamicFixtures/observation/mediaType").asText());
		assertEquals("/systems/{systemId}/controlstreams",
				root.at("/dynamicFixtures/controlStream/collection").asText());
		assertEquals("temperature", root.at("/dynamicFixtures/dataStream/payload/outputName").asText());
		assertEquals("setpoint", root.at("/dynamicFixtures/controlStream/payload/inputName").asText());
	}

	@Test
	public void seederEnforcesDedicatedLocalMutationGate() throws IOException {
		String seeder = read(SEEDER);
		assertContains(seeder, "SMOKE_MUTATION_TESTS_ENABLED");
		assertContains(seeder, "SMOKE_MUTATION_IUT_POLICY");
		assertContains(seeder, "dedicated-mutable-iut");
		assertContains(seeder, "assert_local_target");
		assertContains(seeder, "validate_owned_target");
		assertContains(seeder, "ownership-evidence");
		assertContains(seeder, "Docker container identity or ownership labels do not match");
		assertContains(seeder, "127.0.0.1");
		assertContains(seeder, "localhost");
		assertFalse(seeder.contains("api.georobotix.io"));
	}

	@Test
	public void seederSeparatesReadinessEvidenceFromCredentials() throws IOException {
		String seeder = read(SEEDER);
		assertContains(seeder, "\"provisioningReady\"");
		assertContains(seeder, "\"resourceIds\"");
		assertContains(seeder, "\"requestMethodCounts\"");
		assertContains(seeder, "\"schemaEvidence\"");
		assertContains(seeder, "\"observationEvidence\"");
		assertContains(seeder, "\"credentialSupplied\"");
		assertFalse(seeder.contains("\"credentialValue\""));
		assertFalse(seeder.contains("\"authCredential\""));
	}

	@Test
	public void orchestratorKeepsExternalOshAndPrimaryStateImmutable() throws IOException {
		String orchestrator = read(ORCHESTRATOR);
		assertContains(orchestrator, "LOCAL_OSH_SOURCE");
		assertContains(orchestrator, "LOCAL_OSH_INSTALL");
		assertContains(orchestrator, "EXPECTED_OSH_REMOTE");
		assertContains(orchestrator, "parse_manifest_build_number");
		assertContains(orchestrator, "DEFAULT_OSH_IMAGE");
		assertContains(orchestrator, "installed-files.sha256");
		assertContains(orchestrator, "normalized_primary_fingerprint");
		assertContains(orchestrator, "primary-state-{label}");
		assertContains(orchestrator, "capture_primary(\"before\")");
		assertContains(orchestrator, "capture_primary(\"after\")");
		assertContains(orchestrator, "primary-state-diff.txt");
		assertContains(orchestrator, "PRIMARY_OSH_CONTAINER");
	}

	@Test
	public void orchestratorPreservesConformanceFailureAndAlwaysCleans() throws IOException {
		String orchestrator = read(ORCHESTRATOR);
		assertContains(orchestrator, "parse_testng_report");
		assertContains(orchestrator, "workflow_gate_verdict");
		assertContains(orchestrator, "cleanup_owned_resources");
		assertContains(orchestrator, "verify_owned_container");
		assertContains(orchestrator, "self.finalize()");
		assertContains(orchestrator, "testng-results");
		assertContains(orchestrator, "\"conformanceVerdict\"");
		assertContains(orchestrator, "\"overallWorkflowVerdict\"");
		assertContains(orchestrator, "artifact-manifest.sha256");
	}

	@Test
	public void orchestratorRunsCleanPrimaryAfterPopulatedAttempt() throws IOException {
		String orchestrator = read(ORCHESTRATOR);
		assertContains(orchestrator, "CLEAN_PRIMARY_IUT_URL");
		assertContains(orchestrator, "clean-primary-results");
		assertContains(orchestrator, "run_clean_primary");
		assertContains(orchestrator, "\"cleanPrimaryConformanceVerdict\"");
		assertContains(orchestrator, "\"cleanPrimaryGateVerdict\"");
	}

	@Test
	public void behavioralSuiteCoversOwnershipDriftAbortAndVerdictBranches() throws IOException {
		String behaviorTest = read(BEHAVIOR_TEST);
		assertContains(behaviorTest, "test_cleanup_refuses_unowned_container_and_never_removes_it");
		assertContains(behaviorTest, "test_every_started_abort_phase_runs_all_finalizers");
		assertContains(behaviorTest, "test_cleanup_failure_does_not_skip_remaining_finalizers");
		assertContains(behaviorTest,
				"test_primary_fingerprint_detects_identity_image_config_state_network_and_mount_drift");
		assertContains(behaviorTest, "test_testng_verdict_comes_only_from_xml_and_extracts_exact_failures");
		assertContains(behaviorTest, "test_rejects_non_loopback_credentialed_https_wrong_path_and_wrong_port");
		assertContains(behaviorTest, "test_rejects_primary_or_unrelated_loopback_container");
	}

	@Test
	public void shellEntrypointDelegatesWithoutOwningCleanupLogic() throws IOException {
		String entrypoint = read(ENTRYPOINT);
		assertContains(entrypoint, "exec python3");
		assertContains(entrypoint, "local_osh_populated_e2e.py");
		assertFalse(entrypoint.contains("docker rm"));
	}

	@Test
	public void labeledTeamEngineSmokeRefusesUnownedNameCleanup() throws IOException {
		String smoke = read(SMOKE);
		assertContains(smoke, "SMOKE_RUN_LABEL");
		assertContains(smoke, "org.opengeospatial.ets.csapi.run-id");
		assertContains(smoke, "refusing to remove unowned container name");
		assertContains(smoke, "docker rm -f \"$container_id\"");
	}

	private static String read(Path path) throws IOException {
		assertTrue("missing " + path, Files.isRegularFile(path));
		return Files.readString(path);
	}

	private static void assertContains(String content, String expected) {
		assertTrue("missing required contract fragment: " + expected, content.contains(expected));
	}

}
