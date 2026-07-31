package org.opengis.cite.ogcapiconnectedsystems10.validation.sensorml;

import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

/**
 * Backend-neutral SensorML validation outcome.
 *
 * @param diagnostics immutable, sorted IUT document diagnostics.
 */
public record SensorMlValidationResult(List<String> diagnostics) {

	public SensorMlValidationResult {
		Objects.requireNonNull(diagnostics, "diagnostics");
		if (diagnostics.stream().anyMatch(Objects::isNull)) {
			throw new IllegalStateException("SensorML validator backend returned a null diagnostic.");
		}
		diagnostics = List.copyOf(new TreeSet<>(diagnostics));
	}

	/**
	 * @return true when the document satisfies its target schema.
	 */
	public boolean valid() {
		return this.diagnostics.isEmpty();
	}

}
