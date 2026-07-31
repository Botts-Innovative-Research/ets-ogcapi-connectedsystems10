package org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Unit checks for the address-pinned API-definition transport.
 */
public class VerifySensorMlHttpFetcher {

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-MEDIA-ADVERTISEMENT-001.
	 */
	@Test
	public void deployedSecurityProbeExercisesExternalTransportControls() throws Exception {
		SensorMlOpenApiSecurityRuntimeProbe.run();
	}

	/**
	 * REQ-ETS-PART1-013; SCENARIO-ETS-PART1-013-RELEASED-MEDIA-ADVERTISEMENT-001.
	 */
	@Test
	public void exactIutOriginIsPinnedOnceBeforeCredentialedDescriptionGraph() throws Exception {
		Map<String, String> authorizations = new ConcurrentHashMap<>();
		HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		server.createContext("/", exchange -> respond(exchange, authorizations));
		server.start();
		try {
			int port = server.getAddress().getPort();
			URI origin = URI.create("http://rebinding.test:" + port + "/");
			AtomicInteger resolutions = new AtomicInteger();
			SensorMlHttpFetcher.AddressResolver resolver = host -> {
				if (resolutions.incrementAndGet() == 1) {
					return new InetAddress[] { InetAddress.getLoopbackAddress() };
				}
				return new InetAddress[] { InetAddress.getByName("203.0.113.10") };
			};
			SensorMlHttpFetcher.PinnedTransport transport = new SensorMlHttpFetcher.PinnedTransport(origin, resolver);

			transport.fetch(origin.resolve("openapi.yaml"), origin, true, "Bearer pinned-secret");
			transport.fetch(origin.resolve("references/system.yaml"), origin, true, "Bearer pinned-secret");

			assertEquals("The exact IUT origin must be resolved only during transport setup.", 1, resolutions.get());
			assertEquals("Bearer pinned-secret", authorizations.get("/openapi.yaml"));
			assertEquals("Bearer pinned-secret", authorizations.get("/references/system.yaml"));
		}
		finally {
			server.stop(0);
		}
	}

	private static void respond(HttpExchange exchange, Map<String, String> authorizations) throws IOException {
		authorizations.put(exchange.getRequestURI().getPath(), exchange.getRequestHeaders().getFirst("Authorization"));
		byte[] body = "openapi: 3.1.0\n".getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type", "application/yaml");
		exchange.sendResponseHeaders(200, body.length);
		exchange.getResponseBody().write(body);
		exchange.close();
	}

}
