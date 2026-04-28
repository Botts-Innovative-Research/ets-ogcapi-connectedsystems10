package org.opengis.cite.ogcapiconnectedsystems10;

import java.net.URI;
import java.util.Map;

import org.opengis.cite.ogcapiconnectedsystems10.util.ClientUtils;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.w3c.dom.Document;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * A supporting base class that sets up a common test fixture. These configuration methods
 * are invoked before those defined in a subclass.
 *
 * <p>
 * Ported from the archetype's Jersey-1 CommonFixture: {@code request}/{@code response}
 * fields are now typed against Jakarta {@link Invocation}/{@link Response}. Sprint 2+
 * conformance classes will likely use REST Assured logging filters instead (per
 * features10@java17Tomcat10TeamEngine6); this base class is the minimal compile-clean
 * archetype scaffold.
 * </p>
 */
public class CommonFixture {

	/**
	 * Root test suite package (absolute path).
	 */
	public static final String ROOT_PKG_PATH = "/org/opengis/cite/ogcapiconnectedsystems10/";

	/**
	 * HTTP client component (Jakarta JAX-RS Client API).
	 */
	protected Client client;

	/**
	 * The most recently built HTTP GET Invocation.
	 */
	protected Invocation request;

	/**
	 * The most recently received HTTP response.
	 */
	protected Response response;

	/**
	 * Initializes the common test fixture with a client component for interacting with
	 * HTTP endpoints.
	 * @param testContext The test context that contains all the information for a test
	 * run, including suite attributes.
	 */
	@BeforeClass
	public void initCommonFixture(ITestContext testContext) {
		Object obj = testContext.getSuite().getAttribute(SuiteAttribute.CLIENT.getName());
		if (null != obj) {
			this.client = Client.class.cast(obj);
		}
		obj = testContext.getSuite().getAttribute(SuiteAttribute.TEST_SUBJECT.getName());
		if (null == obj) {
			throw new SkipException("Test subject not found in ITestContext.");
		}
	}

	@BeforeMethod
	public void clearMessages() {
		this.request = null;
		this.response = null;
	}

	/**
	 * Obtains the (XML) response entity as a DOM Document. This convenience method wraps
	 * a static method call to facilitate unit testing (Mockito workaround).
	 * @param response A Glassfish Jersey ClientResponse representation of an HTTP
	 * response message.
	 * @param targetURI The target URI from which the entity was retrieved (may be null).
	 * @return A Document representing the entity.
	 *
	 * @see ClientUtils#getResponseEntityAsDocument
	 */
	public Document getResponseEntityAsDocument(org.glassfish.jersey.client.ClientResponse response, String targetURI) {
		return ClientUtils.getResponseEntityAsDocument(response, targetURI);
	}

	/**
	 * Builds an HTTP GET Invocation. This convenience method wraps a static method call
	 * to facilitate unit testing.
	 * @param endpoint A URI indicating the target resource.
	 * @param qryParams A Map containing query parameters (may be null).
	 * @param mediaTypes A list of acceptable media types; if not specified, generic XML
	 * ("application/xml") is preferred.
	 * @return An Invocation that can be invoked to obtain a Response.
	 *
	 * @see ClientUtils#buildGetRequest
	 */
	public Invocation buildGetRequest(URI endpoint, Map<String, String> qryParams, MediaType... mediaTypes) {
		return ClientUtils.buildGetRequest(endpoint, qryParams, mediaTypes);
	}

}
