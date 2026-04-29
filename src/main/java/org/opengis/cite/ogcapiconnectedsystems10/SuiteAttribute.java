package org.opengis.cite.ogcapiconnectedsystems10;

import java.io.File;
import java.net.URI;

import jakarta.ws.rs.client.Client;

import org.w3c.dom.Document;

/**
 * An enumerated type defining ISuite attributes that may be set to constitute a shared
 * test fixture.
 */
@SuppressWarnings("rawtypes")
public enum SuiteAttribute {

	/**
	 * A client component for interacting with HTTP endpoints.
	 */
	CLIENT("httpClient", Client.class),
	/**
	 * A DOM Document that represents the test subject or metadata about it.
	 */
	TEST_SUBJECT("testSubject", Document.class),
	/**
	 * A File containing the test subject or a description of it.
	 */
	TEST_SUBJ_FILE("testSubjectFile", File.class),
	/**
	 * The raw IUT (Implementation Under Test) URI as supplied via TestNG suite parameter.
	 * Used by REST Assured-based conformance.core.* test classes (Sprint 1+) which reach
	 * the IUT directly rather than via the legacy DOM-parsing TEST_SUBJECT path.
	 */
	IUT("iut", URI.class),

	/**
	 * Optional credential string sent as the {@code Authorization} request header on
	 * every REST Assured-issued request (REQ-ETS-CLEANUP-013 / Sprint 5 GAP-1 wedge fix).
	 * Set by
	 * {@link org.opengis.cite.ogcapiconnectedsystems10.listener.SuiteFixtureListener#processSuiteParameters
	 * SuiteFixtureListener.processSuiteParameters} from the suite-level
	 * {@code auth-credential} parameter; absent when the parameter is unset or empty.
	 */
	AUTH_CREDENTIAL("authCredential", String.class);

	private final Class attrType;

	private final String attrName;

	private SuiteAttribute(String attrName, Class attrType) {
		this.attrName = attrName;
		this.attrType = attrType;
	}

	public Class getType() {
		return attrType;
	}

	public String getName() {
		return attrName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(attrName);
		sb.append('(').append(attrType.getName()).append(')');
		return sb.toString();
	}

}
