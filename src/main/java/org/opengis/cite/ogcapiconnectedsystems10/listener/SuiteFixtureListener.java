package org.opengis.cite.ogcapiconnectedsystems10.listener;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.logging.Level;

import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.TestRunArg;
import org.opengis.cite.ogcapiconnectedsystems10.util.ClientUtils;
import org.opengis.cite.ogcapiconnectedsystems10.util.TestSuiteLogger;
import org.opengis.cite.ogcapiconnectedsystems10.util.URIUtils;
import org.opengis.cite.ogcapiconnectedsystems10.util.XMLUtils;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.w3c.dom.Document;

import jakarta.ws.rs.client.Client;

/**
 * A listener that performs various tasks before and after a test suite is run, usually
 * concerned with maintaining a shared test suite fixture. Since this listener is loaded
 * using the ServiceLoader mechanism, its methods will be called before those of other
 * suite listeners listed in the test suite definition and before any annotated
 * configuration methods.
 *
 * Attributes set on an ISuite instance are not inherited by constituent test group
 * contexts (ITestContext). However, suite attributes are still accessible from lower
 * contexts.
 *
 * @see org.testng.ISuite ISuite interface
 */
public class SuiteFixtureListener implements ISuiteListener {

	@Override
	public void onStart(ISuite suite) {
		processSuiteParameters(suite);
		registerClientComponent(suite);
	}

	@Override
	public void onFinish(ISuite suite) {
		if (null != System.getProperty("deleteSubjectOnFinish")) {
			deleteTempFiles(suite);
			System.getProperties().remove("deleteSubjectOnFinish");
		}
	}

	/**
	 * Processes test suite arguments and sets suite attributes accordingly. The entity
	 * referenced by the {@link TestRunArg#IUT iut} argument is retrieved and written to a
	 * File that is set as the value of the suite attribute
	 * {@link SuiteAttribute#TEST_SUBJ_FILE testSubjectFile}.
	 * @param suite An ISuite object representing a TestNG test suite.
	 */
	void processSuiteParameters(ISuite suite) {
		Map<String, String> params = suite.getXmlSuite().getParameters();
		TestSuiteLogger.log(Level.CONFIG, "Suite parameters\n" + params.toString());
		String iutParam = params.get(TestRunArg.IUT.toString());
		if ((null == iutParam) || iutParam.isEmpty()) {
			throw new IllegalArgumentException("Required test run parameter not found: " + TestRunArg.IUT.toString());
		}
		URI iutRef = URI.create(iutParam.trim());
		// Stash the raw IUT URI on the suite so REST Assured-based test classes (Sprint 1
		// onwards) can read it without re-parsing the XmlSuite parameter map. Coexists
		// with
		// the legacy TEST_SUBJECT/TEST_SUBJ_FILE attributes used by archetype-style
		// tests.
		suite.setAttribute(SuiteAttribute.IUT.getName(), iutRef);
		File entityFile = null;
		try {
			entityFile = URIUtils.dereferenceURI(iutRef);
		}
		catch (IOException iox) {
			// Non-fatal: a CS API IUT may legitimately reject the default Accept header
			// or
			// be temporarily unreachable. Sprint 1 Core tests reach the IUT via REST
			// Assured
			// directly using the `iut` suite attribute set above; the legacy DOM-based
			// TEST_SUBJECT path is not exercised by the new conformance.core.* classes.
			// Log
			// and continue; tests that actually require TEST_SUBJECT will SkipException
			// via
			// CommonFixture.initCommonFixture.
			TestSuiteLogger.log(Level.WARNING, "Failed to dereference IUT URI " + iutRef
					+ " — REST Assured tests in conformance.core.* will still run; legacy DOM tests will SKIP. ("
					+ iox.getMessage() + ")");
			return;
		}
		TestSuiteLogger.log(Level.FINE, String.format("Wrote test subject to file: %s (%d bytes)",
				entityFile.getAbsolutePath(), entityFile.length()));
		suite.setAttribute(SuiteAttribute.TEST_SUBJ_FILE.getName(), entityFile);
		Document iutDoc = null;
		try {
			iutDoc = URIUtils.parseURI(entityFile.toURI());
		}
		catch (Exception x) {
			// JSON / non-XML IUT representations are expected for CS API. Skip DOM
			// population; REST Assured-based tests proceed via the iut attribute.
			TestSuiteLogger.log(Level.WARNING, "Resource retrieved from " + iutRef
					+ " is not parseable as XML (likely JSON for CS API). DOM-based legacy tests will SKIP via CommonFixture. ("
					+ x.getMessage() + ")");
			return;
		}
		suite.setAttribute(SuiteAttribute.TEST_SUBJECT.getName(), iutDoc);
		if (TestSuiteLogger.isLoggable(Level.FINE)) {
			StringBuilder logMsg = new StringBuilder("Parsed resource retrieved from ");
			logMsg.append(iutRef).append("\n");
			logMsg.append(XMLUtils.writeNodeToString(iutDoc));
			TestSuiteLogger.log(Level.FINE, logMsg.toString());
		}
	}

	/**
	 * A client component is added to the suite fixture as the value of the
	 * {@link SuiteAttribute#CLIENT} attribute; it may be subsequently accessed via the
	 * {@link org.testng.ITestContext#getSuite()} method.
	 * @param suite The test suite instance.
	 */
	void registerClientComponent(ISuite suite) {
		Client client = ClientUtils.buildClient();
		if (null != client) {
			suite.setAttribute(SuiteAttribute.CLIENT.getName(), client);
		}
	}

	/**
	 * Deletes temporary files created during the test run if TestSuiteLogger is enabled
	 * at the INFO level or higher (they are left intact at the CONFIG level or lower).
	 * @param suite The test suite.
	 */
	void deleteTempFiles(ISuite suite) {
		if (TestSuiteLogger.isLoggable(Level.CONFIG)) {
			return;
		}
		File testSubjFile = (File) suite.getAttribute(SuiteAttribute.TEST_SUBJ_FILE.getName());
		if (testSubjFile.exists()) {
			testSubjFile.delete();
		}
	}

}
