package org.opengis.cite.ogcapiconnectedsystems10.validation.swecommon;

import java.util.Objects;
import java.util.Set;

import org.opengis.cite.swecommon30.validation.SweCommonJsonSchemaValidator;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;

/**
 * Adapts the reusable SWE Common validator to an ETS-owned result boundary.
 */
public final class ConnectedSystemsSweValidatorAdapter {

	private static final String DEFAULT_SCHEMA = "sweCommon.json";

	private final SweCommonJsonSchemaValidator delegate;

	private final String schemaName;

	/** Creates an adapter for the upstream SWE Common root component schema. */
	public ConnectedSystemsSweValidatorAdapter() {
		this(new SweCommonJsonSchemaValidator(), DEFAULT_SCHEMA);
	}

	ConnectedSystemsSweValidatorAdapter(SweCommonJsonSchemaValidator delegate, String schemaName) {
		this.delegate = Objects.requireNonNull(delegate, "delegate");
		this.schemaName = Objects.requireNonNull(schemaName, "schemaName");
	}

	/**
	 * Validates one SWE Common component without exposing validator-library types.
	 * @param component SWE Common JSON component
	 * @return immutable validation outcome
	 * @throws IllegalStateException if the validator cannot load or execute its schema
	 */
	public SweValidationResult validateComponent(JsonNode component) {
		Objects.requireNonNull(component, "component");
		try {
			Set<ValidationMessage> errors = delegate.validate(component, schemaName);
			return new SweValidationResult(errors.stream().map(ValidationMessage::getMessage).toList());
		}
		catch (RuntimeException ex) {
			throw new IllegalStateException("SWE Common validator failed operationally for schema " + schemaName, ex);
		}
	}

}
