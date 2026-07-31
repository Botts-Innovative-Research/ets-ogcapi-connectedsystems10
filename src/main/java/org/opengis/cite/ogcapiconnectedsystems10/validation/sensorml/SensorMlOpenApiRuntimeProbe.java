package org.opengis.cite.ogcapiconnectedsystems10.validation.sensorml;

import java.net.URI;
import java.util.Set;

import org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml.SensorMlSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml.SensorMlSupport.ResourceType;

/**
 * Verifies that the deployed jar can execute its isolated OpenAPI 3.1 parser.
 */
public final class SensorMlOpenApiRuntimeProbe {

	private SensorMlOpenApiRuntimeProbe() {
	}

	/**
	 * Executes an OpenAPI 3.1 bounded-reference parse from the runtime classpath.
	 * @param args ignored.
	 */
	public static void main(String[] args) {
		StringBuilder parameters = new StringBuilder();
		for (int index = 0; index < 80; index++) {
			parameters.append("        - $ref: '#/components/parameters/id'\n");
		}
		String definition = """
				openapi: 3.1.0
				info:
				  title: runtime probe
				  version: "1"
				components:
				  parameters:
				    id:
				      name: id
				      in: query
				      schema:
				        type: string
				  schemas:
				    Node:
				      type: object
				      properties:
				        next:
				          $ref: '#/components/schemas/Node'
				paths:
				  /systems:
				    get:
				      parameters:
				%s
				      responses:
				        "200":
				          description: ok
				          content:
				            application/sml+json:
				              schema:
				                $ref: '#/components/schemas/Node'
				""".formatted(parameters);
		URI source = URI.create("https://runtime-probe.invalid/openapi.yaml");
		var parsed = SensorMlSupport.parseApiDefinition(definition, source, "runtime-probe");
		SensorMlSupport.assertReadMediaAdvertisements(parsed, Set.of(ResourceType.SYSTEM), false, "runtime-probe");
		if (parsed.model().getPaths().get("/systems").getGet().getParameters().size() != 80) {
			throw new IllegalStateException("OpenAPI runtime probe lost cached parameter references.");
		}
		System.out.println("PASS: deployed SensorML OpenAPI 3.1 parser executed");
	}

}
