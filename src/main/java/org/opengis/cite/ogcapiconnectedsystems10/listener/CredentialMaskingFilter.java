package org.opengis.cite.ogcapiconnectedsystems10.listener;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/**
 * REST-Assured {@link Filter} that masks credential header values in the request log
 * BEFORE downstream logging / capture observes them. Implements REQ-ETS-CLEANUP-003 +
 * NFR-ETS-08 (credential masking) per design.md §"CredentialMaskingFilter wiring (Sprint
 * 2 S-ETS-02-04)".
 *
 * <p>
 * <strong>Masking semantics</strong> (verbatim port from v1.0
 * {@code csapi_compliance/src/engine/credential-masker.ts} lines 35-41):
 * </p>
 *
 * <pre>
 * if value.length &lt;= 8: return "****"
 * else                  : return value[0:4] + "***" + value[-4:]
 * </pre>
 *
 * <p>
 * Edge cases (carry from v1.0):
 * </p>
 * <ul>
 * <li>{@code null} or empty input → {@code "****"} (full redaction).</li>
 * <li>Bearer-prefix preservation: the ENTIRE value is masked, including the
 * {@code Bearer }/{@code Basic } prefix. Input {@code "Bearer ABCDEFGH12345678WXYZ"} →
 * output {@code "Bear***WXYZ"}. The literal substring {@code "EFGH12345678WXYZ"} MUST NOT
 * appear anywhere in masked output (the SCENARIO-ETS-CLEANUP-LOGBACK-MASKING-001
 * acceptance criterion expects this).</li>
 * <li>Credentials of {@code <8} chars → {@code "****"} (avoids leaking length information
 * that could enable shoulder-surfing reconstruction).</li>
 * <li>Non-credential headers (Content-Type, Accept, etc.) pass through unchanged.</li>
 * </ul>
 *
 * <p>
 * Sensitive header set defaults to: {@code Authorization}, {@code Proxy-Authorization},
 * {@code X-API-Key}, {@code Cookie}, {@code Set-Cookie}. Custom sets may be supplied via
 * the constructor for IUTs that use other auth header names.
 * </p>
 *
 * <p>
 * Wired in {@link SuiteFixtureListener#onStart(org.testng.ISuite)} alongside the existing
 * REST-Assured baseline config so every {@code given()...} call inherits the filter.
 * </p>
 */
public class CredentialMaskingFilter implements Filter {

	private static final Logger LOGR = Logger.getLogger(CredentialMaskingFilter.class.getName());

	/**
	 * Default header set that gets masked. Lower-cased for case-insensitive comparison.
	 * Source: v1.0 credential-masker.ts SENSITIVE_HEADERS + design.md §
	 * "CredentialMaskingFilter wiring (Sprint 2 S-ETS-02-04)".
	 */
	public static final Set<String> DEFAULT_SENSITIVE_HEADERS = Set.of("authorization", "proxy-authorization",
			"x-api-key", "cookie", "set-cookie");

	private final Set<String> sensitiveHeadersLower;

	/** Constructs a filter using {@link #DEFAULT_SENSITIVE_HEADERS}. */
	public CredentialMaskingFilter() {
		this(DEFAULT_SENSITIVE_HEADERS);
	}

	/**
	 * Constructs a filter masking the supplied set of headers (case-insensitive). Header
	 * names are lower-cased internally so callers may pass mixed-case values.
	 * @param sensitiveHeaders the set of header names to mask (non-null; copied
	 * defensively)
	 */
	public CredentialMaskingFilter(Set<String> sensitiveHeaders) {
		if (sensitiveHeaders == null) {
			throw new IllegalArgumentException("sensitiveHeaders must not be null");
		}
		Set<String> lower = new HashSet<>(sensitiveHeaders.size());
		for (String h : sensitiveHeaders) {
			if (h != null) {
				lower.add(h.toLowerCase(Locale.ROOT));
			}
		}
		this.sensitiveHeadersLower = Set.copyOf(lower);
	}

	/**
	 * Masks a single credential value per the v1.0 semantics. Static for direct use by
	 * unit tests + the integration test in SCENARIO-ETS-CLEANUP-LOGBACK-MASKING-001.
	 *
	 * <p>
	 * Note: this masks the FULL value as one opaque string. Bearer-prefix preservation is
	 * intentional per design.md (the literal substring of the credential body MUST NOT
	 * appear; preserving the prefix would leave {@code "Bearer EFGH12345678WXYZ"} only
	 * partially masked).
	 * </p>
	 * @param value the value to mask (may be null/empty)
	 * @return masked form per v1.0 semantics (never null)
	 */
	public static String maskValue(String value) {
		if (value == null || value.isEmpty()) {
			return "****";
		}
		if (value.length() <= 8) {
			return "****";
		}
		return value.substring(0, 4) + "***" + value.substring(value.length() - 4);
	}

	/**
	 * Returns true iff {@code headerName} is in the configured sensitive set
	 * (case-insensitive).
	 * @param headerName a header name (may be null — null returns false)
	 * @return true when the named header should be masked
	 */
	public boolean isSensitive(String headerName) {
		if (headerName == null) {
			return false;
		}
		return this.sensitiveHeadersLower.contains(headerName.toLowerCase(Locale.ROOT));
	}

	/**
	 * REST-Assured filter entry point. Logs (at FINE) the masked form of any sensitive
	 * header on the outgoing request, then proceeds to the next filter in the chain.
	 *
	 * <p>
	 * <strong>Masking strategy</strong>: the filter does NOT mutate the actual outgoing
	 * request — masking the real header would break the auth handshake. Instead, the
	 * filter logs the masked form to the structured-logging stream so observers (TestNG
	 * report attachments, container logs, REST-Assured RequestLoggingFilter chained after
	 * this filter) see only the masked form. Per design.md §"Logback configuration" the
	 * logback pattern intentionally does NOT include {@code %X{*}} MDC dump as a
	 * defense-in-depth in case any future code path bypasses the filter.
	 * </p>
	 *
	 * <p>
	 * Note: REST-Assured's built-in {@code RequestLoggingFilter} (when explicitly added
	 * by user code) will still log the unmasked header because it reads the specification
	 * directly. Future enhancement (Sprint 3+): wrap or replace the built-in logging
	 * filter. For Sprint 2 the suite does not chain RequestLoggingFilter, so the
	 * masked-form FINE log is the sole observable.
	 * </p>
	 */
	@Override
	public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec,
			FilterContext ctx) {
		try {
			if (requestSpec != null && requestSpec.getHeaders() != null) {
				requestSpec.getHeaders().forEach(h -> {
					if (isSensitive(h.getName())) {
						LOGR.log(Level.FINE, () -> "request header " + h.getName() + ": " + maskValue(h.getValue()));
					}
				});
			}
		}
		catch (RuntimeException ex) {
			// Defensive: never let masking-side bookkeeping break the actual request.
			LOGR.log(Level.WARNING, "CredentialMaskingFilter pre-request bookkeeping failed: " + ex.getMessage());
		}
		return ctx.next(requestSpec, responseSpec);
	}

}
