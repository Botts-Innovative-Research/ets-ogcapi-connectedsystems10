package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaValidatorsConfig;

/**
 * Shared JSON Schema validation settings for CS API Part 2 bundled schemas.
 */
public final class Part2SchemaValidation {

	private Part2SchemaValidation() {
	}

	public static JsonSchema getSchema(JsonSchemaFactory factory, String schemaIri) {
		return factory.getSchema(SchemaLocation.of(schemaIri), schemaValidatorsConfig());
	}

	static SchemaValidatorsConfig schemaValidatorsConfig() {
		SchemaValidatorsConfig config = new SchemaValidatorsConfig();
		config.setFormatAssertionsEnabled(true);
		return config;
	}

}
