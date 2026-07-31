package org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml.SensorMlSupport.ResourceType;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Exact-runtime security checks for advertised OpenAPI transport and resolution.
 */
public final class SensorMlOpenApiSecurityRuntimeProbe {

	private static final String CREDENTIAL = "Bearer runtime-probe-secret";

	private SensorMlOpenApiSecurityRuntimeProbe() {
	}

	/**
	 * Exercises external-reference controls from the deployed ETS artifact.
	 */
	public static void run() throws Exception {
		Map<String, String> authorizations = new ConcurrentHashMap<>();
		HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		server.createContext("/", exchange -> respond(exchange, authorizations));
		server.start();
		try {
			int port = server.getAddress().getPort();
			URI origin = URI.create("http://runtime-probe.invalid:" + port + "/");
			AtomicInteger iutResolutions = new AtomicInteger();
			SensorMlHttpFetcher.AddressResolver resolver = host -> {
				if ("runtime-probe.invalid".equals(host)) {
					if (iutResolutions.incrementAndGet() == 1) {
						return new InetAddress[] { InetAddress.getLoopbackAddress() };
					}
					return new InetAddress[] { InetAddress.getByName("203.0.113.10") };
				}
				return new InetAddress[] { InetAddress.getLoopbackAddress() };
			};
			SensorMlHttpFetcher.PinnedTransport transport = new SensorMlHttpFetcher.PinnedTransport(origin, resolver);
			SensorMlHttpFetcher.FetchResult root = transport.fetch(origin.resolve("root.yaml"), origin, true,
					CREDENTIAL);
			var parsed = SensorMlSupport.parseApiDefinition(root.content(), origin.resolve("root.yaml"),
					"runtime-probe", target -> transport.fetch(target, origin, true, CREDENTIAL).content());
			SensorMlSupport.assertReadMediaAdvertisements(parsed, Set.of(ResourceType.SYSTEM), false, "runtime-probe");

			require(iutResolutions.get() == 1, "Exact IUT origin was resolved more than once.");
			require(CREDENTIAL.equals(authorizations.get("/root.yaml")), "Credential was not received by the root.");
			require(CREDENTIAL.equals(authorizations.get("/ref.yaml")),
					"Credential was not received by the reference.");
			expectIOException(() -> transport.fetch(origin.resolve("redirect"), origin, true, CREDENTIAL));
			expectIOException(() -> transport.fetch(origin.resolve("oversize"), origin, true, CREDENTIAL));
			URI privateOrigin = URI.create("http://private-probe.invalid:" + port + "/");
			expectIOException(() -> transport.fetch(privateOrigin.resolve("root.yaml"), privateOrigin, false, null));
			expectIOException(
					() -> transport.fetch(privateOrigin.resolve("root.yaml"), privateOrigin, false, CREDENTIAL));
			verifyDeadlineCancellation();
		}
		finally {
			server.stop(0);
		}
	}

	private static void verifyDeadlineCancellation() throws Exception {
		String definition = """
				openapi: 3.1.0
				info:
				  title: deadline probe
				  version: "1"
				paths:
				  /systems:
				    $ref: references/system.yaml
				""";
		CountDownLatch interrupted = new CountDownLatch(1);
		long started = System.nanoTime();
		try {
			SensorMlSupport.parseApiDefinition(definition, URI.create("https://deadline-probe.invalid/openapi.yaml"),
					"runtime-probe", target -> {
						try {
							Thread.sleep(10_000);
						}
						catch (InterruptedException ex) {
							interrupted.countDown();
							throw ex;
						}
						return "get: {}\n";
					}, Duration.ofMillis(100));
			throw new IllegalStateException("Blocking operation-reference load unexpectedly passed.");
		}
		catch (AssertionError expected) {
			require(Duration.ofNanos(System.nanoTime() - started).compareTo(Duration.ofSeconds(2)) < 0,
					"Operation-reference deadline was not caller-visible.");
			require(interrupted.await(1, TimeUnit.SECONDS),
					"Timed-out operation-reference loader was not interrupted.");
		}
	}

	private static void respond(HttpExchange exchange, Map<String, String> authorizations) throws IOException {
		String path = exchange.getRequestURI().getPath();
		String authorization = exchange.getRequestHeaders().getFirst("Authorization");
		if (authorization != null) {
			authorizations.put(path, authorization);
		}
		if ("/redirect".equals(path)) {
			exchange.getResponseHeaders().set("Location", "/root.yaml");
			exchange.sendResponseHeaders(302, -1);
			exchange.close();
			return;
		}
		byte[] body = switch (path) {
			case "/root.yaml" -> """
					openapi: 3.1.0
					info:
					  title: external runtime probe
					  version: "1"
					paths:
					  /systems:
					    $ref: ref.yaml
					""".getBytes(StandardCharsets.UTF_8);
			case "/ref.yaml" -> """
					get:
					  responses:
					    "200":
					      description: ok
					      content:
					        application/sml+json: {}
					""".getBytes(StandardCharsets.UTF_8);
			case "/oversize" -> new byte[SensorMlHttpFetcher.MAX_BODY_BYTES + 1];
			default -> "not found".getBytes(StandardCharsets.UTF_8);
		};
		int status = Set.of("/root.yaml", "/ref.yaml", "/oversize").contains(path) ? 200 : 404;
		exchange.getResponseHeaders().set("Content-Type", "application/yaml");
		exchange.sendResponseHeaders(status, body.length);
		exchange.getResponseBody().write(body);
		exchange.close();
	}

	private static void expectIOException(IoRunnable action) throws Exception {
		try {
			action.run();
		}
		catch (IOException expected) {
			return;
		}
		throw new IllegalStateException("Unsafe API-definition request unexpectedly passed.");
	}

	private static void require(boolean condition, String message) {
		if (!condition) {
			throw new IllegalStateException(message);
		}
	}

	@FunctionalInterface
	private interface IoRunnable {

		void run() throws Exception;

	}

}
