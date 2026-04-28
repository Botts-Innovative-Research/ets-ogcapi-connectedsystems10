package org.opengis.cite.ogcapiconnectedsystems10.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

/**
 * Unit tests for {@link CredentialMaskingFilter} per S-ETS-02-04 acceptance criteria.
 *
 * <p>
 * Covers:
 * </p>
 * <ul>
 * <li>SCENARIO-ETS-CLEANUP-LOGBACK-MASKING-001 — masking semantics verbatim from v1.0
 * (first 4 + last 4 chars; full redaction below 8 chars; null/empty → ****).</li>
 * <li>NFR-ETS-08 — credential masker unit tests.</li>
 * </ul>
 *
 * <p>
 * Reference: design.md §"CredentialMaskingFilter wiring (Sprint 2 S-ETS-02-04)" lines
 * 480-503; v1.0 csapi_compliance/src/engine/credential-masker.ts lines 35-41.
 * </p>
 */
public class VerifyCredentialMaskingFilter {

	// ----- maskValue: PASS-path semantics -----

	@Test
	public void maskValue_bearerTwentyFourCharsMaskedToFirst4ThreeStarsLast4() {
		// design.md acceptance criterion: input "Bearer ABCDEFGH12345678WXYZ" →
		// "Bear***WXYZ"
		String input = "Bearer ABCDEFGH12345678WXYZ"; // length 27
		String masked = CredentialMaskingFilter.maskValue(input);
		assertEquals("Bear***WXYZ", masked);
		// Critical leak guard: the literal middle MUST NOT appear in the masked form.
		assertFalse("masked form must not leak the credential middle", masked.contains("EFGH12345678WXYZ"));
		assertFalse("masked form must not leak the credential middle", masked.contains("ABCDEFGH12345678"));
	}

	@Test
	public void maskValue_apiKeySixteenCharsMaskedToFirst4ThreeStarsLast4() {
		String input = "0123456789ABCDEF"; // length 16
		String masked = CredentialMaskingFilter.maskValue(input);
		assertEquals("0123***CDEF", masked);
		assertFalse(masked.contains("456789AB"));
	}

	@Test
	public void maskValue_credentialNineCharsStillMaskedToFirst4ThreeStarsLast4() {
		// Threshold: <=8 → full redaction; >8 → first4+***+last4. 9 chars triggers the
		// partial form.
		String input = "abcdefghi"; // length 9
		String masked = CredentialMaskingFilter.maskValue(input);
		assertEquals("abcd***fghi", masked);
	}

	// ----- maskValue: FULL-redaction edge cases -----

	@Test
	public void maskValue_credentialEightCharsFullyRedacted() {
		// Boundary case: exactly 8 chars triggers full redaction (avoids leaking the
		// length signal that would let an attacker shoulder-surf-reconstruct).
		String input = "12345678"; // length 8
		String masked = CredentialMaskingFilter.maskValue(input);
		assertEquals("****", masked);
	}

	@Test
	public void maskValue_credentialBelowEightCharsFullyRedacted() {
		assertEquals("****", CredentialMaskingFilter.maskValue("short"));
		assertEquals("****", CredentialMaskingFilter.maskValue("a"));
		assertEquals("****", CredentialMaskingFilter.maskValue("1234567"));
	}

	@Test
	public void maskValue_emptyStringFullyRedacted() {
		assertEquals("****", CredentialMaskingFilter.maskValue(""));
	}

	@Test
	public void maskValue_nullFullyRedacted() {
		assertEquals("****", CredentialMaskingFilter.maskValue(null));
	}

	// ----- isSensitive: header-set membership -----

	@Test
	public void isSensitive_authorizationHeaderRecognized() {
		CredentialMaskingFilter filter = new CredentialMaskingFilter();
		assertTrue(filter.isSensitive("Authorization"));
		assertTrue("case-insensitive match", filter.isSensitive("authorization"));
		assertTrue("case-insensitive match", filter.isSensitive("AUTHORIZATION"));
	}

	@Test
	public void isSensitive_apiKeyHeaderRecognized() {
		CredentialMaskingFilter filter = new CredentialMaskingFilter();
		assertTrue(filter.isSensitive("X-API-Key"));
		assertTrue("case-insensitive match", filter.isSensitive("x-api-key"));
	}

	@Test
	public void isSensitive_cookieHeaderRecognized() {
		CredentialMaskingFilter filter = new CredentialMaskingFilter();
		assertTrue(filter.isSensitive("Cookie"));
		assertTrue(filter.isSensitive("Set-Cookie"));
	}

	@Test
	public void isSensitive_proxyAuthorizationHeaderRecognized() {
		CredentialMaskingFilter filter = new CredentialMaskingFilter();
		assertTrue(filter.isSensitive("Proxy-Authorization"));
	}

	@Test
	public void isSensitive_nonCredentialHeadersPassThrough() {
		CredentialMaskingFilter filter = new CredentialMaskingFilter();
		assertFalse(filter.isSensitive("Content-Type"));
		assertFalse(filter.isSensitive("Accept"));
		assertFalse(filter.isSensitive("User-Agent"));
		assertFalse(filter.isSensitive("Cache-Control"));
	}

	@Test
	public void isSensitive_nullHeaderName() {
		CredentialMaskingFilter filter = new CredentialMaskingFilter();
		assertFalse(filter.isSensitive(null));
	}

	// ----- custom sensitive-header set -----

	@Test
	public void customSensitiveSet_overridesDefault() {
		CredentialMaskingFilter filter = new CredentialMaskingFilter(Set.of("X-Custom-Token"));
		assertTrue(filter.isSensitive("X-Custom-Token"));
		assertTrue("case-insensitive", filter.isSensitive("x-custom-token"));
		// Default headers no longer in the set.
		assertFalse(filter.isSensitive("Authorization"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void customSensitiveSet_nullRejected() {
		new CredentialMaskingFilter(null);
	}

}
