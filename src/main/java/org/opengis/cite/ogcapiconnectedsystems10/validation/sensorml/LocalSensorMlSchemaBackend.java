package org.opengis.cite.ogcapiconnectedsystems10.validation.sensorml;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

/**
 * Local implementation backed by the released bundled JSON Schema closure.
 */
final class LocalSensorMlSchemaBackend implements SensorMlValidatorBackend {

	private static final String LOCAL_SCHEMA_PREFIX = "https://csapi-compliance.local/schemas/";

	private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(
			SpecVersion.VersionFlag.V202012,
			builder -> builder.schemaMappers(mappers -> mappers.mapPrefix(LOCAL_SCHEMA_PREFIX, "classpath:schemas/")));

	@Override
	public List<String> validate(JsonNode document, SensorMlSchema target) {
		JsonSchema schema = SCHEMA_FACTORY.getSchema(SchemaLocation.of(target.location()), schemaConfig());
		Set<ValidationMessage> messages = schema.validate(document);
		return messages.stream().map(ValidationMessage::getMessage).toList();
	}

	private static SchemaValidatorsConfig schemaConfig() {
		SchemaValidatorsConfig config = new SchemaValidatorsConfig();
		config.setFormatAssertionsEnabled(true);
		return config;
	}

}
