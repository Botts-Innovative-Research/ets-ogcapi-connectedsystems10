package org.opengis.cite.ogcapiconnectedsystems10.validation.swecommon;

import java.util.List;
import java.util.Objects;

/**
 * Immutable ETS-owned SWE Common validation outcome.
 *
 * @param diagnostics sorted validation diagnostics; empty when valid
 */
public record SweValidationResult(List<String> diagnostics) {

	public SweValidationResult {
		diagnostics = Objects.requireNonNull(diagnostics, "diagnostics")
			.stream()
			.map(diagnostic -> Objects.requireNonNull(diagnostic, "diagnostic"))
			.sorted()
			.toList();
	}

	/**
	 * @return {@code true} when the component has no validation diagnostics
	 */
	public boolean isValid() {
		return diagnostics.isEmpty();
	}

}
