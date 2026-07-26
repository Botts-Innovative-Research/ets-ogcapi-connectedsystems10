package org.opengis.cite.ogcapiconnectedsystems10;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import org.junit.Test;

/**
 * Behavioral checks for fresh-only sabotage report selection.
 */
public class VerifyFreshSmokeReport {

	private static final Path HELPER = Path.of("scripts/require-fresh-smoke-report.sh").toAbsolutePath();

	private static final Path CREDENTIAL_GATE = Path.of("scripts/credential-leak-e2e-test.sh").toAbsolutePath();

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-DEPENDENCY-CASCADE-001.
	 */
	@Test
	public void staleReportCannotSatisfyNoReportRun() throws Exception {
		Path directory = Files.createTempDirectory("stale-smoke-report-");
		Path stale = Files.writeString(directory.resolve("s-ets-01-03-teamengine-smoke-stale.xml"), "<stale/>");
		Path marker = Files.writeString(directory.resolve(".run-started"), "");
		Files.setLastModifiedTime(stale, FileTime.fromMillis(1_000));
		Files.setLastModifiedTime(marker, FileTime.fromMillis(2_000));

		CommandResult result = runHelper(directory, marker);

		assertTrue("stale report must be rejected: " + result.output(), result.exitCode() != 0);
	}

	/**
	 * REQ-ETS-PART1-001; SCENARIO-ETS-PART1-001-DEPENDENCY-CASCADE-001.
	 */
	@Test
	public void exactlyOneFreshReportIsSelected() throws Exception {
		Path directory = Files.createTempDirectory("fresh-smoke-report-");
		Path marker = Files.writeString(directory.resolve(".run-started"), "");
		Path fresh = Files.writeString(directory.resolve("s-ets-01-03-teamengine-smoke-fresh.xml"), "<fresh/>");
		Files.setLastModifiedTime(marker, FileTime.fromMillis(2_000));
		Files.setLastModifiedTime(fresh, FileTime.fromMillis(3_000));

		CommandResult result = runHelper(directory, marker);

		assertEquals(result.output(), 0, result.exitCode());
		assertEquals(fresh.toString(), result.output().trim());
	}

	/**
	 * REQ-ETS-CLEANUP-011; SCENARIO-ETS-CLEANUP-CREDENTIAL-LEAK-THREE-FOLD-CLOSE-001.
	 */
	@Test
	public void credentialGateUsesCurrentSmokeOutputAndFreshnessMarker() throws Exception {
		String script = Files.readString(CREDENTIAL_GATE);

		assertTrue(script.contains("SMOKE_RESULTS_DIR=\"${SMOKE_OUTPUT_DIR:-${REPO_ROOT}/ops/test-results}\""));
		assertTrue(script.contains("SMOKE_MARKER=\"${ARCHIVE_DIR}/.smoke-run-started\""));
		assertTrue(script.contains("bash scripts/require-fresh-smoke-report.sh"));
		assertTrue(script.contains("\"$SMOKE_RESULTS_DIR\" \"$SMOKE_MARKER\""));
		assertTrue(script.contains("-newer \"$SMOKE_MARKER\""));
		assertTrue(!script.contains("ls -t ops/test-results/s-ets-01-03-teamengine-smoke-"));
	}

	private static CommandResult runHelper(Path directory, Path marker) throws IOException, InterruptedException {
		Process process = new ProcessBuilder("bash", HELPER.toString(), directory.toString(), marker.toString())
			.redirectErrorStream(true)
			.start();
		String output = new String(process.getInputStream().readAllBytes());
		return new CommandResult(process.waitFor(), output);
	}

	private record CommandResult(int exitCode, String output) {
	}

}
