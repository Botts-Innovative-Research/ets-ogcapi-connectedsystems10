package org.opengis.cite.ogcapiconnectedsystems10;

/**
 * An enumerated type defining all recognized test run arguments.
 */
public enum TestRunArg {

	/**
	 * An absolute URI that refers to a representation of the test subject or metadata
	 * about it.
	 */
	IUT,

	/**
	 * Optional credential string to be sent in the {@code Authorization} request header
	 * on every REST Assured-issued request to the IUT (REQ-ETS-CLEANUP-013 / Sprint 5
	 * GAP-1 wedge fix). The value is propagated unmodified through the
	 * {@link org.opengis.cite.ogcapiconnectedsystems10.listener.MaskingRequestLoggingFilter}
	 * (which masks the value in log output but restores the original on the wire).
	 *
	 * <p>
	 * Wire path: bash {@code SMOKE_AUTH_CREDENTIAL} env var → smoke-test.sh
	 * {@code curl --data-urlencode "auth-credential=..."} → TeamEngine REST suite
	 * parameter {@code auth-credential} → {@link TestRunArg#AUTH_CREDENTIAL}
	 * (TestNGController) → suite XML parameter (TestNGExecutor) →
	 * {@link org.opengis.cite.ogcapiconnectedsystems10.listener.SuiteFixtureListener#processSuiteParameters
	 * SuiteFixtureListener.processSuiteParameters} → REST-Assured default request
	 * specification.
	 * </p>
	 *
	 * <p>
	 * Backward-compatible: when absent (or empty string), no Authorization header is
	 * added (Sprint 1-4 smoke against unauthenticated GeoRobotix continues to work).
	 * </p>
	 */
	AUTH_CREDENTIAL;

	@Override
	public String toString() {
		return name().toLowerCase().replace('_', '-');
	}

}
