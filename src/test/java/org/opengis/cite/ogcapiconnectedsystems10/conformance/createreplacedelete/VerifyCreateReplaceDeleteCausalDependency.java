package org.opengis.cite.ogcapiconnectedsystems10.conformance.createreplacedelete;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * Causal TestNG dependency experiment using the deployed mutation class.
 */
public class VerifyCreateReplaceDeleteCausalDependency {

	private static final Set<String> PROCEDURES = Set.of("systemsCreateReplaceDelete", "systemDeleteCascade",
			"subsystemsCreate", "deploymentsCreateReplaceDelete", "subdeploymentsCreate",
			"proceduresCreateReplaceDelete", "samplingFeaturesCreateReplaceDelete", "propertiesCreateReplaceDelete",
			"resourcesCreateInCustomCollections", "resourcesReplaceInCustomCollections",
			"resourcesDeleteInCustomCollections", "resourcesAddToCustomCollections");

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	/**
	 * REQ-ETS-PART1-010; SCENARIO-ETS-PART1-010-DEPENDENCY-CAUSAL-001.
	 */
	@Test
	public void failedApiCommonPrerequisiteSkipsAllProceduresBeforeIutAccess() throws Exception {
		try (CountingIut iut = new CountingIut()) {
			iut.start();
			Path output = this.temporaryFolder.newFolder("sabotage").toPath();
			RunEvidence evidence = run(iut.uri(), output);

			assertTrue(evidence.failed().contains("sabotagedApiCommonPrerequisite"));
			assertEquals(PROCEDURES, intersection(evidence.skipped(), PROCEDURES));
			assertEquals("sabotage must block before Create/Replace/Delete IUT access", 0, iut.calls());
			assertFalse("programmatic TestNG must not write repository-root test-output",
					Files.exists(Path.of("test-output")));
		}
	}

	private static RunEvidence run(URI iut, Path outputDirectory) {
		Capture capture = new Capture(iut);
		TestNG testng = new TestNG(false);
		testng.setUseDefaultListeners(false);
		testng.setVerbose(0);
		testng.setOutputDirectory(outputDirectory.toAbsolutePath().toString());
		XmlSuite suite = new XmlSuite();
		suite.setName("Create/Replace/Delete causal dependency");
		suite.setParameters(java.util.Map.of("iut", iut.toString()));
		XmlTest test = new XmlTest(suite);
		test.setName("API Common sabotage");
		test.setXmlClasses(List.of(new XmlClass(SabotagedApiCommonPrerequisite.class),
				new XmlClass(CreateReplaceDeleteTests.class)));
		testng.setXmlSuites(List.of(suite));
		testng.addListener((ITestNGListener) capture);
		testng.run();
		return capture.evidence();
	}

	private static Set<String> intersection(Set<String> actual, Set<String> expected) {
		Set<String> result = new LinkedHashSet<>(actual);
		result.retainAll(expected);
		return result;
	}

	public static class SabotagedApiCommonPrerequisite {

		@org.testng.annotations.Test(groups = "part1apicommon")
		public void sabotagedApiCommonPrerequisite() {
			throw new AssertionError("SPRINT56_CAUSAL_API_COMMON_MARKER");
		}

	}

	private static final class Capture implements ISuiteListener, ITestListener {

		private final URI iut;

		private final Set<String> failed = new LinkedHashSet<>();

		private final Set<String> skipped = new LinkedHashSet<>();

		private Capture(URI iut) {
			this.iut = iut;
		}

		@Override
		public void onStart(ISuite suite) {
			suite.setAttribute(SuiteAttribute.IUT.getName(), this.iut);
			suite.setAttribute(SuiteAttribute.MUTATION_TESTS_ENABLED.getName(), "true");
			suite.setAttribute(SuiteAttribute.MUTATION_IUT_POLICY.getName(), "dedicated-mutable-iut");
		}

		@Override
		public void onTestFailure(ITestResult result) {
			this.failed.add(result.getMethod().getMethodName());
		}

		@Override
		public void onTestSkipped(ITestResult result) {
			this.skipped.add(result.getMethod().getMethodName());
		}

		private RunEvidence evidence() {
			return new RunEvidence(Set.copyOf(this.failed), Set.copyOf(this.skipped));
		}

	}

	private record RunEvidence(Set<String> failed, Set<String> skipped) {
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

		private void respond(HttpExchange exchange) throws IOException {
			this.calls.incrementAndGet();
			exchange.sendResponseHeaders(500, -1);
			exchange.close();
		}

		@Override
		public void close() {
			this.server.stop(0);
		}

	}

}
