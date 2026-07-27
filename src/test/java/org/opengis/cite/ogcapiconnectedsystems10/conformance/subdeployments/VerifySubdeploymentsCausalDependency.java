package org.opengis.cite.ogcapiconnectedsystems10.conformance.subdeployments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.testng.IConfigurationListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestNGListener;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * Causal TestNG dependency experiment using the deployed Subdeployment class.
 */
public class VerifySubdeploymentsCausalDependency {

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	private static final Set<String> PROCEDURES = Set.of("subdeploymentCollectionIsValid",
			"recursiveParameterUsesBooleanValues", "deploymentsRecursiveSearchIsComplete",
			"subdeploymentsRecursiveSearchIsComplete", "recursiveAssociationsIncludeDescendants");

	/**
	 * REQ-ETS-PART1-005; SCENARIO-ETS-PART1-005-RELEASED-DEPENDENCY-CAUSAL-001;
	 * SCENARIO-ETS-PART1-005-RELEASED-DEPENDENCY-ARTIFACT-HYGIENE-001.
	 */
	@Test
	public void oneDeploymentFailureChangesAllFiveMethodsToPreIutSkip() throws Exception {
		try (CountingIut iut = new CountingIut()) {
			iut.start();
			RunEvidence baseline = run(iut.uri(), PassingDeploymentPrerequisite.class,
					this.temporaryFolder.newFolder("baseline").toPath());

			assertTrue(baseline.started().containsAll(PROCEDURES));
			assertEquals(5, baseline.started().stream().filter(PROCEDURES::contains).count());
			assertTrue("passing-prerequisite baseline must reach the IUT", iut.calls() > 0);

			iut.reset();
			RunEvidence sabotage = run(iut.uri(), SabotagedDeploymentPrerequisite.class,
					this.temporaryFolder.newFolder("sabotage").toPath());

			assertTrue(sabotage.failed().contains("sabotagedDeploymentPrerequisite"));
			assertEquals(PROCEDURES, intersection(sabotage.skipped(), PROCEDURES));
			assertTrue(sabotage.skipReasons()
				.stream()
				.anyMatch(reason -> reason.contains("sabotagedDeploymentPrerequisite")));
			assertEquals("sabotage must block before Subdeployment IUT access", 0, iut.calls());
			assertFalse("programmatic TestNG must not write repository-root test-output",
					Files.exists(Path.of("test-output")));
		}
	}

	private static RunEvidence run(URI iut, Class<?> prerequisite, Path outputDirectory) {
		Capture capture = new Capture(iut);
		TestNG testng = new TestNG(false);
		testng.setUseDefaultListeners(false);
		testng.setVerbose(0);
		testng.setOutputDirectory(outputDirectory.toAbsolutePath().toString());
		XmlSuite suite = new XmlSuite();
		suite.setName("Subdeployment causal dependency");
		suite.setParameters(java.util.Map.of("iut", iut.toString()));
		XmlTest test = new XmlTest(suite);
		test.setName("Passing baseline or single-variable sabotage");
		test.setXmlClasses(java.util.List.of(new XmlClass(prerequisite), new XmlClass(SubdeploymentsTests.class)));
		testng.setXmlSuites(java.util.List.of(suite));
		testng.addListener((ITestNGListener) capture);
		testng.run();
		return capture.evidence();
	}

	private static Set<String> intersection(Set<String> actual, Set<String> expected) {
		Set<String> result = new LinkedHashSet<>(actual);
		result.retainAll(expected);
		return result;
	}

	public static class PassingDeploymentPrerequisite {

		@org.testng.annotations.Test(groups = "deployments")
		public void deploymentPrerequisitePasses() {
		}

	}

	public static class SabotagedDeploymentPrerequisite {

		@org.testng.annotations.Test(groups = "deployments")
		public void sabotagedDeploymentPrerequisite() {
			throw new AssertionError("SPRINT51_CAUSAL_DEPLOYMENT_MARKER");
		}

	}

	private static final class Capture implements ISuiteListener, ITestListener, IConfigurationListener {

		private final URI iut;

		private final Set<String> started = new LinkedHashSet<>();

		private final Set<String> failed = new LinkedHashSet<>();

		private final Set<String> skipped = new LinkedHashSet<>();

		private final Set<String> skipReasons = new LinkedHashSet<>();

		private Capture(URI iut) {
			this.iut = iut;
		}

		@Override
		public void onStart(ISuite suite) {
			suite.setAttribute(SuiteAttribute.IUT.getName(), this.iut);
		}

		@Override
		public void onTestStart(ITestResult result) {
			this.started.add(result.getMethod().getMethodName());
		}

		@Override
		public void onTestFailure(ITestResult result) {
			this.failed.add(result.getMethod().getMethodName());
		}

		@Override
		public void onTestSkipped(ITestResult result) {
			this.skipped.add(result.getMethod().getMethodName());
			if (result.getThrowable() != null && result.getThrowable().getMessage() != null) {
				this.skipReasons.add(result.getThrowable().getMessage());
			}
		}

		@Override
		public void onConfigurationSkip(ITestResult result) {
			if (result.getThrowable() != null && result.getThrowable().getMessage() != null) {
				this.skipReasons.add(result.getThrowable().getMessage());
			}
		}

		private RunEvidence evidence() {
			return new RunEvidence(Set.copyOf(this.started), Set.copyOf(this.failed), Set.copyOf(this.skipped),
					Set.copyOf(this.skipReasons));
		}

	}

	private record RunEvidence(Set<String> started, Set<String> failed, Set<String> skipped, Set<String> skipReasons) {
	}

	private static final class CountingIut implements AutoCloseable {

		private final HttpServer server;

		private final AtomicInteger calls = new AtomicInteger();

		private CountingIut() throws IOException {
			this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
			this.server.createContext("/", this::respond);
		}

		private void start() {
			this.server.start();
		}

		private URI uri() {
			return URI.create("http://127.0.0.1:" + this.server.getAddress().getPort() + "/api/");
		}

		private int calls() {
			return this.calls.get();
		}

		private void reset() {
			this.calls.set(0);
		}

		private void respond(HttpExchange exchange) throws IOException {
			this.calls.incrementAndGet();
			exchange.getResponseHeaders().set("Content-Type", "application/json");
			byte[] body = "{}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(500, body.length);
			try (java.io.OutputStream output = exchange.getResponseBody()) {
				output.write(body);
			}
		}

		@Override
		public void close() {
			this.server.stop(0);
		}

	}

}
