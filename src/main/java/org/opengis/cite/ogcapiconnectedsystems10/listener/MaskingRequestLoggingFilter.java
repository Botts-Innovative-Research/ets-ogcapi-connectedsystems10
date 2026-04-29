package org.opengis.cite.ogcapiconnectedsystems10.listener;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.restassured.filter.FilterContext;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/**
 * REST-Assured {@link RequestLoggingFilter} variant that masks credential-bearing headers
 * BEFORE they reach the underlying log stream, then restores the originals BEFORE the
 * actual HTTP request is sent to the IUT.
 *
 * <p>
 * Closes the unmasked side-channel that the parallel {@link CredentialMaskingFilter}
 * cannot. Sprint 3 hardening per S-ETS-03-02; design.md §"Sprint 3 hardening:
 * MaskingRequestLoggingFilter wrap pattern (S-ETS-03-02)".
 * </p>
 *
 * <p>
 * <strong>Pattern</strong>: subclass + try/finally header swap (architect-ratified
 * "option (a)" per design.md). Subclassing inherits all of REST-Assured's payload
 * formatting (200+ LOC); only the header-emission step is intercepted via the swap. The
 * {@code try/finally} guarantees the IUT receives the real credential header even if
 * {@code super.filter()} throws — masked headers exist only during the formatter's read.
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
	 * Constructs a filter using {@link #DEFAULT_HEADERS_TO_MASK} and {@link System#out}.
	 */
	public MaskingRequestLoggingFilter() {
		this(DEFAULT_HEADERS_TO_MASK, System.out);
	}

	/**
	 * Constructs a filter masking the supplied set of headers (case-insensitive) and
	 * writing the formatted log output to {@code stream}.
	 * @param headersToMask the set of header names to mask (non-null)
	 * @param stream the PrintStream the underlying RequestLoggingFilter writes to
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
	 * REST-Assured filter entry point. Snapshot original sensitive header values, replace
	 * with masked equivalents for the duration of {@code super.filter()} (which writes to
	 * the configured stream), then restore originals so the actual HTTP request still
	 * carries the unmasked credentials to the IUT.
	 */
	@Override
	public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec,
			FilterContext ctx) {
		Map<String, String> originals = new HashMap<>();
		try {
			if (requestSpec != null && requestSpec.getHeaders() != null) {
				// Snapshot first (avoid concurrent modification while iterating)
				java.util.List<String> sensitiveNames = new java.util.ArrayList<>();
				requestSpec.getHeaders().forEach(h -> {
					if (isMasked(h.getName())) {
						sensitiveNames.add(h.getName());
					}
				});
				for (String name : sensitiveNames) {
					String value = requestSpec.getHeaders().getValue(name);
					if (value != null) {
						originals.put(name, value);
						requestSpec.removeHeader(name);
						requestSpec.header(name, CredentialMaskingFilter.maskValue(value));
					}
				}
			}
			return super.filter(requestSpec, responseSpec, ctx);
		}
		finally {
			// Restore originals — IUT MUST receive the real credentials (auth handshake
			// would fail otherwise). Runs even if super.filter() throws.
			if (requestSpec != null) {
				for (Map.Entry<String, String> entry : originals.entrySet()) {
					requestSpec.removeHeader(entry.getKey());
					requestSpec.header(entry.getKey(), entry.getValue());
				}
			}
		}
	}

}
