package org.opengis.cite.ogcapiconnectedsystems10.validation.sensorml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml.SensorMlSupport;
import org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml.SensorMlSupport.ResourceType;

/**
 * Verifies that the deployed jar can execute its isolated OpenAPI 3.1 parser.
 */
public final class SensorMlOpenApiRuntimeProbe {

	private SensorMlOpenApiRuntimeProbe() {
	}

	/**
	 * Executes an OpenAPI 3.1 relative-reference parse from the runtime classpath.
	 * @param args ignored.
	 * @throws Exception if the loopback reference fixture cannot execute.
	 */
	public static void main(String[] args) throws Exception {
		InetAddress loopback = InetAddress.getByName("127.0.0.1");
		try (ServerSocket server = new ServerSocket(0, 1, loopback)) {
			AtomicReference<Throwable> serverFailure = new AtomicReference<>();
			Thread responder = new Thread(() -> servePathReference(server, serverFailure),
					"sensorml-openapi-runtime-probe");
			responder.setDaemon(true);
			responder.start();

			String definition = """
					openapi: 3.1.0
					info:
					  title: runtime probe
					  version: "1"
					paths:
					  /systems:
					    $ref: refs/systems.yaml
					""";
			URI source = URI.create("http://127.0.0.1:" + server.getLocalPort() + "/openapi.yaml");
			var parsed = SensorMlSupport.parseApiDefinition(definition, source, "runtime-probe");
			SensorMlSupport.assertReadMediaAdvertisements(parsed, Set.of(ResourceType.SYSTEM), false, "runtime-probe");

			responder.join(10_000);
			if (responder.isAlive()) {
				throw new IllegalStateException("OpenAPI runtime probe did not request its relative path reference.");
			}
			if (serverFailure.get() != null) {
				throw new IllegalStateException("OpenAPI runtime reference fixture failed.", serverFailure.get());
			}
		}
		System.out.println("PASS: deployed SensorML OpenAPI 3.1 parser executed");
	}

	private static void servePathReference(ServerSocket server, AtomicReference<Throwable> failure) {
		try (Socket socket = server.accept();
				BufferedReader input = new BufferedReader(
						new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1));
				OutputStream output = socket.getOutputStream()) {
			String requestLine = input.readLine();
			for (String line = input.readLine(); line != null && !line.isEmpty(); line = input.readLine()) {
				// Consume request headers.
			}
			if (requestLine == null || !requestLine.startsWith("GET /refs/systems.yaml ")) {
				throw new IOException("Unexpected OpenAPI reference request: " + requestLine);
			}
			byte[] body = """
					get:
					  responses:
					    "200":
					      description: ok
					      content:
					        application/sml+json: {}
					""".getBytes(StandardCharsets.UTF_8);
			byte[] headers = ("HTTP/1.1 200 OK\r\nContent-Type: application/yaml\r\nContent-Length: " + body.length
					+ "\r\nConnection: close\r\n\r\n")
				.getBytes(StandardCharsets.ISO_8859_1);
			output.write(headers);
			output.write(body);
			output.flush();
		}
		catch (Throwable ex) {
			failure.set(ex);
		}
	}

}
