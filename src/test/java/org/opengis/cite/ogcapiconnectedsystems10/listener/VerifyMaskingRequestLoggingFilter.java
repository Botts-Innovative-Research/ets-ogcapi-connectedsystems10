package org.opengis.cite.ogcapiconnectedsystems10.listener;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.RequestSender;

/**
 * <strong>WIRING-ONLY unit tests</strong> for {@link MaskingRequestLoggingFilter}'s
 * static / configuration surface (REQ-ETS-CLEANUP-013, originally per S-ETS-03-02).
 *
 * <p>
 * <strong>Scope caveat (Sprint 6 S-ETS-06-01 + S-ETS-06-03 reclassification per
 * META-GAP-1)</strong>: the tests in this class use a {@code StubFilterContext} that
 * returns {@code null} from {@code ctx.next()} and CANNOT prove wire-side credential
 * integrity. They cover only:
 * </p>
 * <ul>
 * <li>{@link MaskingRequestLoggingFilter#isMasked} case-insensitive header-set
 * membership.</li>
 * <li>Mask format on the stream output ({@code Bear***WXYZ} / {@code 0123***CDEF}
 * present; literal credential middle absent).</li>
 * <li>Constructor null-argument guard.</li>
 * <li>Default header set is the SUPERSET of
 * {@link CredentialMaskingFilter#DEFAULT_SENSITIVE_HEADERS}.</li>
 * </ul>
 *
 * <p>
 * <strong>Wire-side proof lives elsewhere</strong>: see
 * {@link VerifyWireRestoresOriginalCredential} (Sprint 6 / REQ-ETS-CLEANUP-016) which
 * uses a {@code CapturingFilterContext} (records the spec passed to {@code ctx.next()}
 * and snapshots header values BY VALUE at call time) — that test is the structural proof
 * that the wire carries the ORIGINAL credential, not the masked form. The 8-test
 * "wiring-only" PASS metric SHOULD NOT be conflated with credential safety; refer to
 * VerifyWireRestoresOriginalCredential for the latter.
 * </p>
 *
 * <p>
 * <strong>Sprint 6 deletions</strong>: two legacy tests
 * ({@code filter_restoresOriginalAuthorizationHeaderAfterMaskedSuperFilterCall} and
 * {@code filter_restoresOriginalApiKeyAndCookieEvenWhenSuperFilterThrows}) verified
 * try/finally semantics around the Sprint 3 mutate/restore implementation. Sprint 6
 * approach (i) (no mutation; bypass {@code super.filter()}) eliminates that try/finally
 * codepath entirely, so the tests would test non-existent code. They are DELETED rather
 * than retained as decorative passes (Plan-Raze 2026-04-30 finer-granularity disposition:
 * "partial-delete is healthier than preserving tests for non-existent code"). The
 * wire-side correctness invariant they purported to prove now lives in
 * VerifyWireRestoresOriginalCredential. Likewise the {@code ThrowingFilterContext} helper
 * (only used by the deleted #2) is removed.
 * </p>
 *
 * <p>
 * Reference: design.md §"Sprint 3 hardening: MaskingRequestLoggingFilter wrap pattern
 * (S-ETS-03-02)" + Sprint 6 S-ETS-06-01 implementation notes (Sprint 3 wrap pattern
 * superseded by approach (i)) + ADR-010 §"Notes / references".
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
		// Header set is SUPERSET of CredentialMaskingFilter.DEFAULT_SENSITIVE_HEADERS —
		// adds Set-Cookie + Proxy-Authorization (the CredentialMaskingFilter already has
		// them in Sprint 2, so the SUPERSET semantics is reduced to "matches all of
		// them").
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

	// ----- Stream output mask format (wiring-only — does NOT prove wire-side
	// credential integrity; see VerifyWireRestoresOriginalCredential) -----

	@Test
	public void filter_streamOutputContainsMaskedFormNotLiteralCredential() {
		// SCENARIO-ETS-CLEANUP-CREDENTIAL-LEAK-INTEGRATION-001 unit-level proxy:
		// the ByteArrayOutputStream that the filter writes to MUST contain the masked
		// form (Bear***WXYZ) and MUST NOT contain the literal credential body
		// (EFGH12345678WXYZ). Sprint 6 approach (i): the filter emits the masked log
		// line directly (no super.filter()); this test verifies the format remains
		// correct. WIRING-ONLY caveat: this does NOT prove what the wire carries —
		// see VerifyWireRestoresOriginalCredential for that.
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
			// approach (i) calls ctx.next which returns null from StubFilterContext —
			// no exception expected, but caught for symmetry with the apiKey test.
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
		// Wiring-only — same caveat as above.
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

	// ----- Constructor invariants -----

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
	 * Stub that returns null from ctx.next(...). Sprint 6 approach (i) calls ctx.next
	 * directly (rather than going through super.filter), so the test still uses this stub
	 * to short-circuit the transport call without opening a socket. NOTE: this is
	 * "wiring-only" coverage — see {@link VerifyWireRestoresOriginalCredential} for the
	 * wire-side proof using a CapturingFilterContext.
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

}
