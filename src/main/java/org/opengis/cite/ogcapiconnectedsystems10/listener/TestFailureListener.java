package org.opengis.cite.ogcapiconnectedsystems10.listener;

import java.nio.charset.StandardCharsets;

import org.opengis.cite.ogcapiconnectedsystems10.CommonFixture;
import org.opengis.cite.ogcapiconnectedsystems10.util.XMLUtils;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.w3c.dom.Document;

import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * A listener that augments a test result with diagnostic information in the event that a
 * test method failed. This information will appear in the XML report when the test run is
 * completed.
 *
 * <p>
 * Ported from the archetype's Jersey-1 listener: Invocation/Response replace
 * ClientRequest/ClientResponse. Jakarta's {@link Invocation} does not expose request
 * headers/method/URI directly, so request diagnostics are reduced to a marker string.
 * Rich request/response capture moves to REST Assured logging filters in Sprint 2+
 * conformance classes (per features10@java17Tomcat10TeamEngine6).
 * </p>
 */
public class TestFailureListener extends TestListenerAdapter {

	/**
	 * Sets the "request" and "response" attributes of a test result. The value of these
	 * attributes is a string that contains information about the content of an outgoing
	 * or incoming message: target resource, status code, headers, entity (if present).
	 * The entity is represented as a String with UTF-8 character encoding.
	 * @param result A description of a test result (with a fail verdict).
	 */
	@Override
	public void onTestFailure(ITestResult result) {
		super.onTestFailure(result);
		Object instance = result.getInstance();
		if (CommonFixture.class.isInstance(instance)) {
			CommonFixture fixture = CommonFixture.class.cast(instance);
			result.setAttribute("request", getRequestMessageInfo(fixture.getRequest()));
			result.setAttribute("response", getResponseMessageInfo(fixture.getResponse()));
		}
	}

	/**
	 * Gets diagnostic information about a request invocation. Jakarta's Invocation does
	 * not expose method/headers/URI; for richer capture, Sprint 2+ tests use REST
	 * Assured's request logging filter.
	 * @param req A built Invocation, or null if no request was made.
	 * @return A short string describing the request invocation.
	 */
	String getRequestMessageInfo(Invocation req) {
		if (null == req) {
			return "No request message.";
		}
		return "Invocation: " + req.toString();
	}

	/**
	 * Gets diagnostic information about a response message.
	 * @param rsp An object representing an HTTP response message.
	 * @return A string containing information gleaned from the response message.
	 */
	String getResponseMessageInfo(Response rsp) {
		if (null == rsp) {
			return "No response message.";
		}
		StringBuilder msgInfo = new StringBuilder();
		msgInfo.append("Status: ").append(rsp.getStatus()).append('\n');
		msgInfo.append("Headers: ").append(rsp.getStringHeaders()).append('\n');
		if (rsp.hasEntity()) {
			MediaType mediaType = rsp.getMediaType();
			if (null != mediaType && mediaType.isCompatible(MediaType.APPLICATION_XML_TYPE)) {
				Document doc = rsp.readEntity(Document.class);
				msgInfo.append(XMLUtils.writeNodeToString(doc));
			}
			else {
				byte[] body = rsp.readEntity(byte[].class);
				msgInfo.append(new String(body, StandardCharsets.UTF_8));
			}
			msgInfo.append('\n');
		}
		return msgInfo.toString();
	}

}
