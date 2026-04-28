package org.opengis.cite.ogcapiconnectedsystems10;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.testng.ITestContext;
import org.testng.annotations.BeforeTest;

/**
 * Checks that suite-level preconditions are satisfied before any conformance class
 * executes. If any of these methods fails, the {@code <test>} block fails configuration
 * and dependent {@code @Test} methods are skipped.
 *
 * <p>
 * Note: the original archetype used {@code @BeforeSuite}, but TestNG 7+ rejects native
 * parameter injection on suite-scope configuration methods. {@code @BeforeTest} provides
 * the same effect (runs before any {@code @Test} in this {@code <test>} block) and
 * supports {@code ITestContext} injection. The
 * {@link org.opengis.cite.ogcapiconnectedsystems10.listener.SuiteFixtureListener
 * SuiteFixtureListener} runs at true suite scope (via the listener SPI) and is the
 * primary place where the {@code iut} parameter is parsed.
 * </p>
 */
public class SuitePreconditions {

	private static final Logger LOGR = Logger.getLogger(SuitePreconditions.class.getName());

	/**
	 * Verifies that an IUT URI was supplied via the {@code iut} TestNG suite parameter.
	 * Sprint 1 Core (REST Assured-based) tests rely on the {@link SuiteAttribute#IUT}
	 * attribute set by
	 * {@link org.opengis.cite.ogcapiconnectedsystems10.listener.SuiteFixtureListener
	 * SuiteFixtureListener#processSuiteParameters}. The legacy
	 * {@link SuiteAttribute#TEST_SUBJ_FILE TEST_SUBJ_FILE} attribute is no longer
	 * required (CS API representations are JSON, not XML, so DOM parsing is intentionally
	 * skipped — see SuiteFixtureListener).
	 * @param testContext Information about the (pending) test run.
	 */
	@BeforeTest
	public void verifyIutSet(ITestContext testContext) {
		Object iut = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (null == iut || !SuiteAttribute.IUT.getType().isInstance(iut)) {
			String msg = String.format(
					"Required suite attribute '%s' is missing or not a %s. Did the 'iut' TestNG suite parameter get set?",
					SuiteAttribute.IUT.getName(), SuiteAttribute.IUT.getType().getName());
			LOGR.log(Level.SEVERE, msg);
			throw new AssertionError(msg);
		}
	}

}
