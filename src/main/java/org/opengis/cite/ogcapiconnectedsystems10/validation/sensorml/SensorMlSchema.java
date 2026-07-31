package org.opengis.cite.ogcapiconnectedsystems10.validation.sensorml;

/**
 * Closed set of released Connected Systems SensorML entry schemas.
 */
public enum SensorMlSchema {

	SYSTEM("system.json"),

	SYSTEM_COLLECTION("systemCollection.json"),

	DEPLOYMENT("deployment.json"),

	DEPLOYMENT_COLLECTION("deploymentCollection.json"),

	PROCEDURE("procedure.json"),

	PROCEDURE_COLLECTION("procedureCollection.json"),

	PROPERTY("property.json"),

	PROPERTY_COLLECTION("propertyCollection.json");

	private static final String BASE = "https://csapi-compliance.local/schemas/connected-systems-1/sensorml/";

	private final String location;

	SensorMlSchema(String fileName) {
		this.location = BASE + fileName;
	}

	String location() {
		return this.location;
	}

}
