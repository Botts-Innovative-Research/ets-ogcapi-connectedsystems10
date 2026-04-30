package org.opengis.cite.ogcapiconnectedsystems10.listener;

import java.io.PrintStream;
import java.util.Locale;
import java.util.Set;

import io.restassured.filter.FilterContext;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/**
 * REST-Assured {@link RequestLoggingFilter} variant that emits a MASKED log line for
 * credential-bearing request headers BEFORE handing the unmodified {@code requestSpec} to
 * {@code ctx.next()} for HTTP transport. The wire therefore carries the ORIGINAL
 * credential while observers (logback, container catalina.out, REST-Assured stream
 * consumers) see only the masked form.
 *
 * <p>
 * <strong>Sprint 6 redesign (S-ETS-06-01) — approach (i) per Sprint 5 meta-Raze
 * recommendation</strong>: the Sprint 3 implementation used a mutate-snapshot-restore
 * try/finally pattern around {@code super.filter()}. Sprint 5 adversarial review (Raze
 * 0.74 GAP-1' + meta-Raze 0.83 META-GAP-1) showed this was structurally broken: the
 * parent {@code RequestLoggingFilter.filter()} calls {@code ctx.next()} INTERNALLY (which
 * is the HTTP transport call), so the masked spec was on the wire before the finally
 * block ran. The 16 wiring-only unit tests (VerifyAuthCredentialPropagation 8 +
 * VerifyMaskingRequestLoggingFilter 8) used a {@code StubFilterContext} returning null
 * and could not detect this.
 * </p>
 *
 * <p>
 * Approach (i) ELIMINATES the mutate/restore dance entirely:
 * </p>
 * <ol>
 * <li>Build a masked log string from a snapshot of {@code requestSpec.getHeaders()} —
 * sensitive header values are passed through {@link CredentialMaskingFilter#maskValue}
 * for log emission only.</li>
 * <li>Emit the masked log line directly to this filter's shadowed {@link PrintStream}
 * field (see field-shadow note below).</li>
 * <li>Call {@code ctx.next(requestSpec, responseSpec)} on the UNMUTATED spec — delegating
 * to the next filter / transport with the original credential intact.</li>
 * </ol>
 *
 * <p>
 * <strong>Why a shadowed {@link PrintStream} field</strong>: REST-Assured 5.5.0 declares
 * {@code RequestLoggingFilter.stream} as {@code private final} with NO {@code
 * getPrintStream()} accessor (verified via Maven Central source jar inspection by
 * Plan-Raze 2026-04-30). To emit our own log line we must capture the same
 * {@link PrintStream} on the constructor and reference it via this class's own field. The
 * {@code super(stream)} call still propagates the field to the parent for any future
 * calls that delegate, but {@code super.filter()} itself is intentionally NOT called by
 * this class — see the bypass note below.
 * </p>
 *
 * <p>
 * <strong>Why bypass {@code super.filter()}</strong>: the parent's {@code filter()}
 * method is exactly two operations in REST-Assured 5.5.0 (verified by Plan-Raze source
 * inspection): {@code RequestPrinter.print(...)} (the unmasked log emission) followed by
 * {@code return ctx.next(...)}. By NOT calling super, we (a) replace the unmasked log
 * emission with our own masked equivalent, and (b) retain the call to {@code ctx.next}
 * via our explicit invocation. The transport semantics are identical; only the logged
 * representation changes. If REST-Assured upgrades past 5.x add behavior between log
 * emission and {@code ctx.next} inside {@code RequestLoggingFilter}, this bypass MUST be
 * re-evaluated (see TODO below).
 * </p>
 *
 * <p>
 * Header set rationale: SUPERSET of
 * {@link CredentialMaskingFilter#DEFAULT_SENSITIVE_HEADERS} — adds {@code Set-Cookie}
 * (response side; the formatter logs response headers too) and
 * {@code Proxy-Authorization} (rare but present in some CITE harness configs). The
 * intersection is intentional defense-in-depth.
 * </p>
 *
 * <p>
 * Wired in {@link SuiteFixtureListener#registerRestAssuredFilters()} alongside
 * {@link CredentialMaskingFilter} (parallel FINE log; defense-in-depth retained per
 * design.md §"Wiring point").
 * </p>
 *
 * <p>
 * <strong>Wire-side proof</strong>: see
 * {@code src/test/java/.../listener/VerifyWireRestoresOriginalCredential.java} — this is
 * the structural test that distinguishes approach (i) correctness from the 16 legacy
 * "wiring-only" tests.
 * </p>
 *
 * <p>
 * TODO(rest-assured-upgrade): if rest-assured.version in pom.xml is bumped past 5.5.x,
 * audit {@code RequestLoggingFilter} source for any new behavior between
 * {@code RequestPrinter.print} and {@code ctx.next} (e.g. retry, response caching,
 * interceptor chaining). The current bypass assumes those two ops are the only ones.
 * </p>
 */
public class MaskingRequestLoggingFilter extends RequestLoggingFilter {

	/**
	 * Default header set masked by this filter — superset of
	 * {@link CredentialMaskingFilter#DEFAULT_SENSITIVE_HEADERS} (preserves all members,
	 * adds nothing new since CredentialMaskingFilter already covers Set-Cookie + Proxy-
	 * Authorization). Lower-cased for case-insensitive comparison.
	 */
	public static final Set<String> DEFAULT_HEADERS_TO_MASK = Set.of("authorization", "proxy-authorization",
			"x-api-key", "cookie", "set-cookie");

	private final Set<String> headersToMaskLower;

	/**
	 * Shadowed {@link PrintStream} — REST-Assured's {@code RequestLoggingFilter.stream}
	 * is {@code private final} with no accessor (5.5.0). We capture the same stream here
	 * so the masked log line can be emitted directly without calling super.filter(). See
	 * class javadoc for rationale.
	 */
	private final PrintStream stream;

	/**
	 * Constructs a filter using {@link #DEFAULT_HEADERS_TO_MASK} and {@link System#out}.
	 */
	public MaskingRequestLoggingFilter() {
		this(DEFAULT_HEADERS_TO_MASK, System.out);
	}

	/**
	 * Constructs a filter masking the supplied set of headers (case-insensitive) and
	 * writing the masked log output to {@code stream}.
	 * @param headersToMask the set of header names to mask (non-null)
	 * @param stream the PrintStream the masked log line is emitted to (non-null in
	 * practice; the parent class also receives it)
	 */
	public MaskingRequestLoggingFilter(Set<String> headersToMask, PrintStream stream) {
		super(stream);
		if (headersToMask == null) {
			throw new IllegalArgumentException("headersToMask must not be null");
		}
		java.util.HashSet<String> lower = new java.util.HashSet<>(headersToMask.size());
		for (String h : headersToMask) {
			if (h != null) {
				lower.add(h.toLowerCase(Locale.ROOT));
			}
		}
		this.headersToMaskLower = Set.copyOf(lower);
		this.stream = stream;
	}

	/**
	 * Returns true iff {@code headerName} is in the configured mask set
	 * (case-insensitive).
	 */
	public boolean isMasked(String headerName) {
		if (headerName == null) {
			return false;
		}
		return this.headersToMaskLower.contains(headerName.toLowerCase(Locale.ROOT));
	}

	/**
	 * REST-Assured filter entry point (Sprint 6 approach (i) implementation).
	 *
	 * <p>
	 * Builds a masked log line from a snapshot of the request's headers (sensitive header
	 * VALUES replaced with their masked form via
	 * {@link CredentialMaskingFilter#maskValue}; non-sensitive headers passed through
	 * unchanged), emits to the shadowed {@link PrintStream} field, then delegates to
	 * {@code ctx.next(requestSpec, responseSpec)} with the UNMUTATED requestSpec. The
	 * wire therefore carries the original credential. No try/finally restoration is
	 * required because no mutation occurs.
	 * </p>
	 *
	 * <p>
	 * Log format is intentionally minimal: HTTP method + URL on the first line, then one
	 * indented {@code Header=Value} line per header. This is sufficient for prong (b) of
	 * the credential-leak three-fold cross-check ({@code Bear***WXYZ} masked-form grep).
	 * Full REST-Assured payload formatter parity is NOT a goal; correctness (wire carries
	 * original credential) takes precedence over log aesthetic parity.
	 * </p>
	 */
	@Override
	public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec,
			FilterContext ctx) {
		if (requestSpec != null && this.stream != null) {
			StringBuilder logLine = new StringBuilder("Request: ");
			try {
				String method = requestSpec.getMethod();
				String uri = requestSpec.getURI();
				logLine.append(method != null ? method : "?").append(' ').append(uri != null ? uri : "?");
				if (requestSpec.getHeaders() != null) {
					requestSpec.getHeaders().forEach(h -> {
						String name = h.getName();
						String value = h.getValue();
						String display = isMasked(name) ? CredentialMaskingFilter.maskValue(value) : value;
						logLine.append("\n\t").append(name).append('=').append(display);
					});
				}
				this.stream.println(logLine);
			}
			catch (RuntimeException ex) {
				// Defensive: never let a logging-side failure break the actual request.
				// Mirror CredentialMaskingFilter's defensive log-and-continue posture.
				this.stream.println("[MaskingRequestLoggingFilter: log-emission failed: " + ex.getMessage() + "]");
			}
		}
		// Delegate to the next filter / transport with the UNMUTATED requestSpec — the
		// wire carries the ORIGINAL credential. This is the load-bearing invariant per
		// Sprint 6 / REQ-ETS-CLEANUP-016.
		return ctx.next(requestSpec, responseSpec);
	}

}
