package org.opengis.cite.ogcapiconnectedsystems10.validation.sensorml;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Stable ETS boundary around the provisional SensorML validator backend.
 *
 * <p>
 * The public contract intentionally exposes neither NetworkNT nor TestNG types so a
 * future external SensorML library can replace the local backend without changing
 * conformance procedures.
 * </p>
 */
public final class ConnectedSystemsSensorMlValidatorAdapter {

	private final SensorMlValidatorBackend backend;

	/**
	 * Creates an adapter backed by the released schemas bundled with this ETS.
	 */
	public ConnectedSystemsSensorMlValidatorAdapter() {
		this(new LocalSensorMlSchemaBackend());
	}

	ConnectedSystemsSensorMlValidatorAdapter(SensorMlValidatorBackend backend) {
		this.backend = Objects.requireNonNull(backend, "backend");
	}

	/**
	 * Validates a document against one of the eight closed SensorML targets.
	 * @param document parsed JSON document.
	 * @param schema closed schema target.
	 * @return immutable backend-neutral validation result.
	 */
	public SensorMlValidationResult validate(JsonNode document, SensorMlSchema schema) {
		Objects.requireNonNull(document, "document");
		Objects.requireNonNull(schema, "schema");
		List<String> diagnostics = this.backend.validate(document, schema);
		if (diagnostics == null) {
			throw new IllegalStateException("SensorML validator backend returned no result.");
		}
		return new SensorMlValidationResult(diagnostics);
	}

}
