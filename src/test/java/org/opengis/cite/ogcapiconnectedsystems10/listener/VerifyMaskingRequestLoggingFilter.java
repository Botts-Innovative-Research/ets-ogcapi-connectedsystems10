package org.opengis.cite.ogcapiconnectedsystems10.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.junit.Test;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.RequestSender;

/**
 * Unit tests for {@link MaskingRequestLoggingFilter} per S-ETS-03-02 acceptance criteria.
 *
 * <p>
 * Covers (per design.md §"Sprint 3 hardening" + architect-handoff
 * constraints_for_generator.must items 5-9):
 * </p>
 * <ul>
 * <li>SCENARIO-ETS-CLEANUP-CREDENTIAL-LEAK-INTEGRATION-001 — Bearer 24-char masked in
 * formatter output (literal middle MUST NOT appear in stream output).</li>
 * <li>X-API-Key masked in formatter output.</li>
 * <li>Cookie / Set-Cookie / Proxy-Authorization superset coverage.</li>
 * <li>IUT-side header restoration verified — after {@code filter()} returns, the request
 * spec carries the ORIGINAL credential value (not the masked form).</li>
 * <li>try/finally restoration even when {@code super.filter()} throws.</li>
 * <li>Non-credential headers pass through unchanged.</li>
 * </ul>
 *
 * <p>
 * Reference: design.md §"Sprint 3 hardening: MaskingRequestLoggingFilter wrap pattern
 * (S-ETS-03-02)" lines 531-642; ADR-010 §"Notes / references" cross-reference.
 * </p>
 */
public class VerifyMaskingRequestLoggingFilter {

	private static final String SYNTHETIC_BEARER = "Bearer ABCDEFGH12345678WXYZ";

	private static final String SYNTHETIC_API_KEY = "0123456789ABCDEF";

	/** Builds a real REST-Assured FilterableRequestSpecification (no Mockito needed). */
	private FilterableRequestSpecification buildRequestSpec() {
		// Use the public RequestSpecBuilder + cast to FilterableRequestSpecification.
		// REST-Assured 5.5.0's RequestSpecBuilder.build() returns a RequestSpecification
		// whose runtime impl is RequestSpecificationImpl (which implements
		// FilterableRequestSpecification).
		return (FilterableRequestSpecification) new RequestSpecBuilder().build();
	}

	// ----- isMasked: case-insensitive sensitive-header membership -----

	@Test
	public void isMasked_authorizationHeaderRecognized() {
		MaskingRequestLoggingFilter filter = new MaskingRequestLoggingFilter();
		assertTrue(filter.isMasked("Authorization"));
		assertTrue(filter.isMasked("authorization"));
		assertTrue(filter.isMasked("AUTHORIZATION"));
	}

	@Test
	public void isMasked_supersetOfCredentialMaskingFilterDefaults() {
		MaskingRequestLoggingFilter filter = new MaskingRequestLoggingFilter();
		// Architect-handoff constraints_for_generator.must item 8: header set is SUPERSET
		// of CredentialMaskingFilter.DEFAULT_SENSITIVE_HEADERS — adds Set-Cookie +
		// Proxy-Authorization (the CredentialMaskingFilter already has them in Sprint 2,
		// so the SUPERSET semantics is reduced to "matches all of them").
		for (String h : CredentialMaskingFilter.DEFAULT_SENSITIVE_HEADERS) {
			assertTrue("MaskingRequestLoggingFilter should mask " + h, filter.isMasked(h));
		}
		assertTrue(filter.isMasked("Set-Cookie"));
		assertTrue(filter.isMasked("Proxy-Authorization"));
	}

	@Test
	public void isMasked_nonCredentialHeaderNotMasked() {
		MaskingRequestLoggingFilter filter = new MaskingRequestLoggingFilter();
		assertFalse(filter.isMasked("Content-Type"));
		assertFalse(filter.isMasked("Accept"));
		assertFalse(filter.isMasked(null));
	}

	// ----- IUT-side header restoration (try/finally invariant) -----

	@Test
	public void filter_restoresOriginalAuthorizationHeaderAfterMaskedSuperFilterCall() {
		// Critical S-ETS-03-02 invariant: the IUT MUST receive the unmasked credential.
		// The masked form exists ONLY for the duration of super.filter()'s stream write.
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(baos, true, StandardCharsets.UTF_8);
		MaskingRequestLoggingFilter filter = new MaskingRequestLoggingFilter(
				MaskingRequestLoggingFilter.DEFAULT_HEADERS_TO_MASK, stream);

		FilterableRequestSpecification reqSpec = buildRequestSpec();
		reqSpec.header("Authorization", SYNTHETIC_BEARER);
		reqSpec.header("Content-Type", "application/json");

		// Stub FilterContext that does NOT actually issue an HTTP request; returns null
		// after the super.filter() call has performed its stream write.
		FilterContext ctx = new StubFilterContext();
		try {
			filter.filter(reqSpec, /* responseSpec */ null, ctx);
		}
		catch (RuntimeException expected) {
			// REST-Assured's super.filter() may try to issue the actual request via
			// ctx.next() and fail with a connection error; that's OK — our header
			// restoration must still have run via finally.
		}

		// CRITICAL: post-filter, the spec MUST carry the ORIGINAL Authorization header.
		String restoredAuth = reqSpec.getHeaders().getValue("Authorization");
		assertNotNull("Authorization header must exist post-filter", restoredAuth);
		assertEquals("Authorization MUST be restored to original (unmasked) value", SYNTHETIC_BEARER, restoredAuth);
		// Non-credential headers untouched
		assertEquals("application/json", reqSpec.getHeaders().getValue("Content-Type"));
	}

	@Test
	public void filter_restoresOriginalApiKeyAndCookieEvenWhenSuperFilterThrows() {
		// Sub-case: even if the underlying super.filter() throws, finally must restore
		// originals. Use a FilterContext that throws to simulate a transport failure.
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(baos, true, StandardCharsets.UTF_8);
		MaskingRequestLoggingFilter filter = new MaskingRequestLoggingFilter(
				MaskingRequestLoggingFilter.DEFAULT_HEADERS_TO_MASK, stream);

		FilterableRequestSpecification reqSpec = buildRequestSpec();
		reqSpec.header("X-API-Key", SYNTHETIC_API_KEY);
		reqSpec.header("Cookie", "sessionId=abcdefghijklmnop");

		FilterContext ctx = new ThrowingFilterContext();
		try {
			filter.filter(reqSpec, null, ctx);
		}
		catch (RuntimeException expected) {
			// expected — ThrowingFilterContext throws
		}

		assertEquals(SYNTHETIC_API_KEY, reqSpec.getHeaders().getValue("X-API-Key"));
		assertEquals("sessionId=abcdefghijklmnop", reqSpec.getHeaders().getValue("Cookie"));
	}

	@Test
	public void filter_streamOutputContainsMaskedFormNotLiteralCredential() {
		// SCENARIO-ETS-CLEANUP-CREDENTIAL-LEAK-INTEGRATION-001 unit-level proxy:
		// the ByteArrayOutputStream that super.filter() writes to MUST contain the
		// masked form (Bear***WXYZ) and MUST NOT contain the literal credential body
		// (EFGH12345678WXYZ).
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(baos, true, StandardCharsets.UTF_8);
		MaskingRequestLoggingFilter filter = new MaskingRequestLoggingFilter(
				MaskingRequestLoggingFilter.DEFAULT_HEADERS_TO_MASK, stream);

		FilterableRequestSpecification reqSpec = buildRequestSpec();
		reqSpec.header("Authorization", SYNTHETIC_BEARER);

		FilterContext ctx = new StubFilterContext();
		try {
			filter.filter(reqSpec, null, ctx);
		}
		catch (RuntimeException expected) {
			// per other tests
		}

		String output = baos.toString(StandardCharsets.UTF_8);
		// CRITICAL leak guard — literal middle MUST NOT appear in formatter output.
		assertFalse("Stream output MUST NOT contain literal credential middle 'EFGH12345678WXYZ' (leak): " + output,
				output.contains("EFGH12345678WXYZ"));
		assertFalse("Stream output MUST NOT contain literal credential middle 'ABCDEFGH12345678' (leak)",
				output.contains("ABCDEFGH12345678"));
		// And the masked form MUST appear (proves filter ran rather than dropping the
		// header).
		assertTrue("Stream output MUST contain masked form 'Bear***WXYZ' (proves filter ran): " + output,
				output.contains("Bear***WXYZ"));
	}

	@Test
	public void filter_apiKeyMaskedInStreamOutput() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(baos, true, StandardCharsets.UTF_8);
		MaskingRequestLoggingFilter filter = new MaskingRequestLoggingFilter(
				MaskingRequestLoggingFilter.DEFAULT_HEADERS_TO_MASK, stream);

		FilterableRequestSpecification reqSpec = buildRequestSpec();
		reqSpec.header("X-API-Key", SYNTHETIC_API_KEY);

		FilterContext ctx = new StubFilterContext();
		try {
			filter.filter(reqSpec, null, ctx);
		}
		catch (RuntimeException expected) {
		}

		String output = baos.toString(StandardCharsets.UTF_8);
		assertFalse("Stream output MUST NOT contain literal API key middle '456789AB': " + output,
				output.contains("456789AB"));
		assertTrue("Stream output MUST contain masked form '0123***CDEF' (proves filter ran): " + output,
				output.contains("0123***CDEF"));
	}

	@Test
	public void constructor_nullHeaderSetThrows() {
		try {
			new MaskingRequestLoggingFilter(null, System.out);
			throw new AssertionError("expected IllegalArgumentException for null headers");
		}
		catch (IllegalArgumentException expected) {
			// pass
		}
	}

	// ----- Stub FilterContext that does NOT actually issue a request -----

	/**
	 * Stub that returns null from ctx.next(...) — REST-Assured's RequestLoggingFilter's
	 * super.filter() invokes ctx.next() to chain to the next filter / send the request.
	 * Here we short-circuit so the unit test does not actually open a socket.
	 */
	private static final class StubFilterContext implements FilterContext {

		@Override
		public Response next(FilterableRequestSpecification req, FilterableResponseSpecification resp) {
			return null;
		}

		@Override
		public <T> T getValue(String name) {
			return null;
		}

		@Override
		public boolean hasValue(String name) {
			return false;
		}

		@Override
		public boolean hasValue(String name, Object value) {
			return false;
		}

		@Override
		public void setValue(String name, Object value) {
			// no-op
		}

		@Override
		public Response send(RequestSender requestSender) {
			return null;
		}

	}

	private static final class ThrowingFilterContext implements FilterContext {

		@Override
		public Response next(FilterableRequestSpecification req, FilterableResponseSpecification resp) {
			throw new RuntimeException("simulated transport failure for try/finally test");
		}

		@Override
		public <T> T getValue(String name) {
			return null;
		}

		@Override
		public boolean hasValue(String name) {
			return false;
		}

		@Override
		public boolean hasValue(String name, Object value) {
			return false;
		}

		@Override
		public void setValue(String name, Object value) {
		}

		@Override
		public Response send(RequestSender requestSender) {
			throw new RuntimeException("send not implemented in test stub");
		}

	}

}
