package org.opengis.cite.ogcapiconnectedsystems10.validation.sensorml;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Internal substitution boundary for a future external SensorML validator.
 */
@FunctionalInterface
interface SensorMlValidatorBackend {

	List<String> validate(JsonNode document, SensorMlSchema schema);

}
