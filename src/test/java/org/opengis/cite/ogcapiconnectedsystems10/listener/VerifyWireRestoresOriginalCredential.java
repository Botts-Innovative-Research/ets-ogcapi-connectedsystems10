package org.opengis.cite.ogcapiconnectedsystems10.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.RequestSender;

/**
 * Wire-side unit test for {@link MaskingRequestLoggingFilter} per Sprint 6 S-ETS-06-01 /
 * REQ-ETS-CLEANUP-016.
 *
 * <p>
 * <strong>Why this test class is structurally distinct from the 16 "wiring-only" tests in
 * {@link VerifyAuthCredentialPropagation} +
 * {@link VerifyMaskingRequestLoggingFilter}</strong>: those tests use a
 * {@code StubFilterContext} whose {@code next()} returns {@code null} (or throws). Such a
 * stub cannot detect the GAP-1' filter-ordering defect that Sprint 5 adversarial review
 * surfaced — namely, the previous Sprint 3 implementation mutated {@code requestSpec} to
 * the masked form BEFORE delegating to {@code super.filter()} (which calls
 * {@code ctx.next()} internally for HTTP transport). The 16 wiring-only tests only
 * verified the post-filter restoration (after the wire send was already poisoned); they
 * could not see what the wire actually carried.
 * </p>
 *
 * <p>
 * This test uses {@link CapturingFilterContext} which RECORDS the {@code requestSpec}
 * argument passed to {@code ctx.next()} and returns a Mockito mock {@link Response}.
 * Asserting on the captured spec proves what the wire would have carried at the moment of
 * HTTP transport — which is the only structurally-sound proof that approach (i) (no
 * mutation pre-{@code
 * ctx.next}) holds.
 * </p>
 *
 * <p>
 * After Sprint 6's approach (i) implementation, this test PASSES because
 * {@code MaskingRequestLoggingFilter.filter()} no longer mutates the spec — it builds the
 * masked log string from a header snapshot and emits to its shadowed {@link PrintStream}
 * field, then calls {@code ctx.next(requestSpec, responseSpec)} with the unmutated spec.
 * </p>
 *
 * <p>
 * Reference: {@code .harness/evaluations/sprint-ets-05-meta-review.yaml} §META-GAP-1 +
 * {@code .harness/contracts/sprint-ets-06.yaml} SCENARIO-ETS-CLEANUP-WIRE-SIDE-TEST-001 +
 * SCENARIO-ETS-CLEANUP-MASKING-WIRE-TEST-001.
 * </p>
 */
public class VerifyWireRestoresOriginalCredential {

	private static final String SYNTHETIC_BEARER = "Bearer ABCDEFGH12345678WXYZ";

	private static final String SYNTHETIC_API_KEY = "0123456789ABCDEF";

	private static final String LITERAL_LEAK_PROBE = "EFGH12345678WXYZ";

	private static final String MASKED_FORM_PROBE = "Bear***WXYZ";

	/** Builds a real REST-Assured FilterableRequestSpecification (no Mockito needed). */
	private FilterableRequestSpecification buildRequestSpec() {
		return (FilterableRequestSpecification) new RequestSpecBuilder().build();
	}

	// ----- Wire-side proof: ctx.next receives ORIGINAL credential -----

	/**
	 * Primary acceptance test for Sprint 6 / REQ-ETS-CLEANUP-016. Asserts that the
	 * {@code requestSpec} argument passed to {@code ctx.next()} carries the ORIGINAL
	 * (unmasked) {@code Authorization} value — proving the wire would carry the real
	 * credential at HTTP send time.
	 */
	@Test
	public void wireCarriesOriginalAuthorizationCredential() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(baos, true, StandardCharsets.UTF_8);
		MaskingRequestLoggingFilter filter = new MaskingRequestLoggingFilter(
				MaskingRequestLoggingFilter.DEFAULT_HEADERS_TO_MASK, stream);

		FilterableRequestSpecification reqSpec = buildRequestSpec();
		reqSpec.header("Authorization", SYNTHETIC_BEARER);
		reqSpec.header("Content-Type", "application/json");

		CapturingFilterContext ctx = new CapturingFilterContext();
		Response result = filter.filter(reqSpec, /* responseSpec */ null, ctx);

		// Filter must have called ctx.next exactly once
		assertEquals("ctx.next must be called exactly once per filter invocation", 1, ctx.getCallCount());
		FilterableRequestSpecification capturedSpec = ctx.getCapturedRequestSpec();
		assertNotNull("ctx.next must receive a non-null requestSpec", capturedSpec);

		// The CRITICAL invariant: the spec at ctx.next time carried the ORIGINAL
		// credential — not the masked form. We use the by-VALUE snapshot taken inside
		// ctx.next (NOT the by-reference capturedSpec, which the legacy try/finally
		// implementation would restore after ctx.next returned, masking the bug).
		// If this fails, GAP-1' is NOT closed and the wire would transmit Bear***WXYZ
		// to the IUT (which would break the auth handshake).
		String wireValue = ctx.getCapturedHeaderAtNextCall("Authorization");
		assertEquals("Wire MUST carry ORIGINAL credential at ctx.next call site (not masked form)", SYNTHETIC_BEARER,
				wireValue);

		// Non-credential headers untouched on the wire
		assertEquals("application/json", ctx.getCapturedHeaderAtNextCall("Content-Type"));

		// Mock Response from CapturingFilterContext should be returned by the filter
		assertNotNull("filter() must return the Response from ctx.next", result);
	}

	/**
	 * Supplementary wire-side proof: X-API-Key + Cookie also carried unmutated to the
	 * wire (multi-header coverage of the masking set).
	 */
	@Test
	public void wireCarriesOriginalApiKeyAndCookie() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(baos, true, StandardCharsets.UTF_8);
		MaskingRequestLoggingFilter filter = new MaskingRequestLoggingFilter(
				MaskingRequestLoggingFilter.DEFAULT_HEADERS_TO_MASK, stream);

		FilterableRequestSpecification reqSpec = buildRequestSpec();
		reqSpec.header("X-API-Key", SYNTHETIC_API_KEY);
		reqSpec.header("Cookie", "sessionId=abcdefghijklmnop");

		CapturingFilterContext ctx = new CapturingFilterContext();
		filter.filter(reqSpec, null, ctx);

		// Use BY-VALUE snapshots — see CapturingFilterContext field javadoc for why
		// by-reference reads after the filter returns are unsafe under the legacy
		// try/finally implementation (and equivalent under approach (i)).
		assertEquals("Wire MUST carry original X-API-Key", SYNTHETIC_API_KEY,
				ctx.getCapturedHeaderAtNextCall("X-API-Key"));
		assertEquals("Wire MUST carry original Cookie", "sessionId=abcdefghijklmnop",
				ctx.getCapturedHeaderAtNextCall("Cookie"));
	}

	/**
	 * Companion structural assertion: with approach (i) the filter NO LONGER mutates the
	 * spec at any point — the post-{@code filter()} spec is identical to the
	 * pre-{@code filter()} spec. This subsumes the legacy try/finally restoration
	 * invariant (which is now vacuous because nothing is ever mutated).
	 */
	@Test
	public void filterDoesNotMutateRequestSpec() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(baos, true, StandardCharsets.UTF_8);
		MaskingRequestLoggingFilter filter = new MaskingRequestLoggingFilter(
				MaskingRequestLoggingFilter.DEFAULT_HEADERS_TO_MASK, stream);

		FilterableRequestSpecification reqSpec = buildRequestSpec();
		reqSpec.header("Authorization", SYNTHETIC_BEARER);
		reqSpec.header("Content-Type", "application/json");

		CapturingFilterContext ctx = new CapturingFilterContext();
		filter.filter(reqSpec, null, ctx);

		// The original reqSpec reference must still carry original values post-filter.
		assertEquals("post-filter Authorization must remain original", SYNTHETIC_BEARER,
				reqSpec.getHeaders().getValue("Authorization"));
		assertEquals("application/json", reqSpec.getHeaders().getValue("Content-Type"));
	}

	/**
	 * Verifies the masked-form log line IS emitted to the configured PrintStream — so
	 * downstream observers (logback, container catalina.out) see the masked form. This is
	 * prong (b) of the three-fold cross-check at the unit-test layer.
	 */
	@Test
	public void streamOutputContainsMaskedFormNotLiteralCredential() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(baos, true, StandardCharsets.UTF_8);
		MaskingRequestLoggingFilter filter = new MaskingRequestLoggingFilter(
				MaskingRequestLoggingFilter.DEFAULT_HEADERS_TO_MASK, stream);

		FilterableRequestSpecification reqSpec = buildRequestSpec();
		reqSpec.header("Authorization", SYNTHETIC_BEARER);

		CapturingFilterContext ctx = new CapturingFilterContext();
		filter.filter(reqSpec, null, ctx);

		String output = baos.toString(StandardCharsets.UTF_8);
		assertFalse("Stream output MUST NOT contain literal credential middle (leak): " + output,
				output.contains(LITERAL_LEAK_PROBE));
		assertTrue("Stream output MUST contain masked form '" + MASKED_FORM_PROBE
				+ "' (proves filter emitted masked log line): " + output, output.contains(MASKED_FORM_PROBE));
	}

	// ----- CapturingFilterContext: records requestSpec + returns mock Response -----

	/**
	 * FilterContext that records the {@code requestSpec} argument passed to
	 * {@code ctx.next()} (rather than {@code StubFilterContext} which discards it and
	 * returns null). The captured spec is the wire-side artifact: whatever the filter
	 * passes to {@code ctx.next} is what subsequent filters / the transport layer would
	 * see when the HTTP request is sent.
	 */
	private static final class CapturingFilterContext implements FilterContext {

		private FilterableRequestSpecification capturedRequestSpec;

		private FilterableResponseSpecification capturedResponseSpec;

		/**
		 * Snapshot of header values AT the moment ctx.next() is called. This is the
		 * critical wire-side artifact: we MUST capture by-value here, not by-reference,
		 * because the caller may mutate the spec back to its original state via a finally
		 * block AFTER ctx.next() returns. A by-reference capture would read the
		 * post-restoration state at assertion time and incorrectly conclude the wire was
		 * unaffected. The Sprint 5 GAP-1' bug specifically exploits this: pre-fix code
		 * mutated then restored, so any test that did not snapshot during ctx.next was
		 * decorative.
		 */
		private final Map<String, String> capturedHeadersAtNextCall = new HashMap<>();

		private int callCount = 0;

		@Override
		public Response next(FilterableRequestSpecification req, FilterableResponseSpecification resp) {
			this.capturedRequestSpec = req;
			this.capturedResponseSpec = resp;
			this.callCount++;
			// SNAPSHOT by-value (NOT by-reference) — see field javadoc.
			if (req != null && req.getHeaders() != null) {
				req.getHeaders().forEach(h -> this.capturedHeadersAtNextCall.put(h.getName(), h.getValue()));
			}
			// Mockito mock — Mockito is in pom.xml test scope (verified for Sprint 6).
			// Returning a non-null mock so the caller's `return ctx.next(...)` path
			// produces a non-null Response (avoids NPE in any downstream consumer).
			return mock(Response.class);
		}

		FilterableRequestSpecification getCapturedRequestSpec() {
			return capturedRequestSpec;
		}

		@SuppressWarnings("unused")
		FilterableResponseSpecification getCapturedResponseSpec() {
			return capturedResponseSpec;
		}

		String getCapturedHeaderAtNextCall(String name) {
			return this.capturedHeadersAtNextCall.get(name);
		}

		int getCallCount() {
			return callCount;
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
			return mock(Response.class);
		}

	}

}
