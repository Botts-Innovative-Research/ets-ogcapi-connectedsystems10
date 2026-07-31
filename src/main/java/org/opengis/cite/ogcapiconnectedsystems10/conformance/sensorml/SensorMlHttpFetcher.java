package org.opengis.cite.ogcapiconnectedsystems10.conformance.sensorml;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.DnsResolver;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * Bounded no-redirect HTTP transport for advertised OpenAPI documents.
 */
final class SensorMlHttpFetcher {

	static final int MAX_BODY_BYTES = 2 * 1024 * 1024;

	private static final int CONNECT_TIMEOUT_MILLIS = 5_000;

	private static final int REQUEST_TIMEOUT_MILLIS = 15_000;

	private SensorMlHttpFetcher() {
	}

	@FunctionalInterface
	interface AddressResolver {

		InetAddress[] resolve(String host) throws UnknownHostException;

	}

	static final class PinnedTransport {

		private final Origin iutOrigin;

		private final AddressResolver addressResolver;

		private final Map<Origin, InetAddress[]> pins = new HashMap<>();

		PinnedTransport(URI iutOrigin) throws IOException {
			this(iutOrigin, InetAddress::getAllByName);
		}

		PinnedTransport(URI iutOrigin, AddressResolver addressResolver) throws IOException {
			if (addressResolver == null) {
				throw new IOException("API-definition address resolver is required.");
			}
			validateOrigin(iutOrigin);
			this.iutOrigin = Origin.from(iutOrigin);
			this.addressResolver = addressResolver;
			this.pins.put(this.iutOrigin, resolve(iutOrigin, true));
		}

		FetchResult fetch(URI target, URI allowedOrigin, boolean allowRestrictedAddresses, String authorization)
				throws IOException {
			validateTarget(target, allowedOrigin);
			Origin origin = Origin.from(allowedOrigin);
			boolean exactIutOrigin = origin.equals(this.iutOrigin);
			if (allowRestrictedAddresses && !exactIutOrigin) {
				throw new IOException("Restricted API-definition addresses are allowed only for the exact IUT origin.");
			}
			if (authorization != null && !authorization.isBlank() && !exactIutOrigin) {
				throw new IOException("IUT credentials cannot be sent to a cross-origin API definition.");
			}
			InetAddress[] pinned = addresses(origin, allowedOrigin, allowRestrictedAddresses);
			return fetchPinned(target, pinned, authorization);
		}

		private synchronized InetAddress[] addresses(Origin origin, URI allowedOrigin, boolean allowRestrictedAddresses)
				throws IOException {
			InetAddress[] pinned = this.pins.get(origin);
			if (pinned == null) {
				pinned = resolve(allowedOrigin, allowRestrictedAddresses);
				this.pins.put(origin, pinned);
			}
			else if (!allowRestrictedAddresses) {
				rejectRestricted(allowedOrigin.getHost(), pinned);
			}
			return Arrays.copyOf(pinned, pinned.length);
		}

		private InetAddress[] resolve(URI origin, boolean allowRestrictedAddresses) throws IOException {
			InetAddress[] addresses = this.addressResolver.resolve(origin.getHost());
			if (addresses == null || addresses.length == 0) {
				throw new IOException("Advertised API-definition host resolved to no addresses: " + origin.getHost());
			}
			InetAddress[] resolved = Arrays.copyOf(addresses, addresses.length);
			if (!allowRestrictedAddresses) {
				rejectRestricted(origin.getHost(), resolved);
			}
			return resolved;
		}

	}

	private static void rejectRestricted(String host, InetAddress[] addresses) throws IOException {
		for (InetAddress address : addresses) {
			if (isRestricted(address)) {
				throw new IOException("Cross-origin API-definition host resolved to a restricted address: " + host);
			}
		}
	}

	private static FetchResult fetchPinned(URI target, InetAddress[] pinned, String authorization) throws IOException {
		DnsResolver resolver = host -> {
			if (!host.equalsIgnoreCase(target.getHost())) {
				throw new UnknownHostException("Unapproved API-definition host: " + host);
			}
			return Arrays.copyOf(pinned, pinned.length);
		};
		RequestConfig requestConfig = RequestConfig.custom()
			.setConnectTimeout(CONNECT_TIMEOUT_MILLIS)
			.setConnectionRequestTimeout(CONNECT_TIMEOUT_MILLIS)
			.setSocketTimeout(REQUEST_TIMEOUT_MILLIS)
			.setRedirectsEnabled(false)
			.build();
		try (CloseableHttpClient client = HttpClients.custom()
			.setDnsResolver(resolver)
			.setDefaultRequestConfig(requestConfig)
			.disableRedirectHandling()
			.build()) {
			HttpGet request = new HttpGet(target);
			request.setHeader("Accept",
					"application/vnd.oai.openapi, application/yaml, application/json, text/yaml, */*");
			if (authorization != null && !authorization.isBlank()) {
				request.setHeader("Authorization", authorization);
			}
			try (CloseableHttpResponse response = client.execute(request)) {
				int status = response.getStatusLine().getStatusCode();
				if (status != 200) {
					throw new IOException("API-definition document returned HTTP " + status + ": " + target);
				}
				HttpEntity entity = response.getEntity();
				if (entity == null) {
					throw new IOException("API-definition document returned an empty body: " + target);
				}
				try (InputStream body = entity.getContent()) {
					byte[] bytes = body.readNBytes(MAX_BODY_BYTES + 1);
					if (bytes.length > MAX_BODY_BYTES) {
						throw new IOException(
								"API-definition document exceeds " + MAX_BODY_BYTES + " decoded bytes: " + target);
					}
					String contentType = entity.getContentType() == null ? "" : entity.getContentType().getValue();
					return new FetchResult(new String(bytes, StandardCharsets.UTF_8), contentType);
				}
			}
		}
	}

	private static void validateOrigin(URI origin) throws IOException {
		if (origin == null || !origin.isAbsolute() || !Set.of("http", "https").contains(normalizedScheme(origin))
				|| origin.getHost() == null || origin.getHost().isBlank() || origin.getUserInfo() != null) {
			throw new IOException("IUT origin must be an absolute HTTP(S) URI with a host and no userinfo.");
		}
	}

	private static void validateTarget(URI target, URI allowedOrigin) throws IOException {
		if (target == null || allowedOrigin == null || !target.isAbsolute()) {
			throw new IOException("Absolute API-definition target and allowed origin are required.");
		}
		String scheme = normalizedScheme(target);
		if (!Set.of("http", "https").contains(scheme) || target.getHost() == null || target.getHost().isBlank()
				|| target.getUserInfo() != null || target.getFragment() != null) {
			throw new IOException(
					"API-definition target must be an HTTP(S) URI with a host, no userinfo, and no fragment: "
							+ target);
		}
		if (!scheme.equals(normalizedScheme(allowedOrigin)) || allowedOrigin.getHost() == null
				|| !target.getHost().equalsIgnoreCase(allowedOrigin.getHost())
				|| effectivePort(target) != effectivePort(allowedOrigin)) {
			throw new IOException("API-definition target must retain the approved exact origin: " + target);
		}
	}

	private static boolean isRestricted(InetAddress address) {
		if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
				|| address.isSiteLocalAddress() || address.isMulticastAddress()) {
			return true;
		}
		byte[] value = address.getAddress();
		if (value.length == 4) {
			return isRestrictedIpv4(value);
		}
		if (value.length == 16) {
			if ((value[0] & 0xfe) == 0xfc) {
				return true;
			}
			if ((value[0] & 0xff) == 0x20 && (value[1] & 0xff) == 0x01 && (value[2] & 0xff) == 0x0d
					&& (value[3] & 0xff) == 0xb8) {
				return true;
			}
			if (isIpv4Mapped(value)) {
				return isRestrictedIpv4(Arrays.copyOfRange(value, 12, 16));
			}
		}
		return false;
	}

	private static boolean isRestrictedIpv4(byte[] value) {
		int first = value[0] & 0xff;
		int second = value[1] & 0xff;
		int third = value[2] & 0xff;
		return first == 0 || first == 10 || first == 127 || first >= 224
				|| first == 100 && second >= 64 && second <= 127 || first == 169 && second == 254
				|| first == 172 && second >= 16 && second <= 31 || first == 192 && second == 168
				|| first == 192 && second == 0 && (third == 0 || third == 2)
				|| first == 198 && (second == 18 || second == 19) || first == 198 && second == 51 && third == 100
				|| first == 203 && second == 0 && third == 113;
	}

	private static boolean isIpv4Mapped(byte[] value) {
		for (int index = 0; index < 10; index++) {
			if (value[index] != 0) {
				return false;
			}
		}
		return (value[10] & 0xff) == 0xff && (value[11] & 0xff) == 0xff;
	}

	private static String normalizedScheme(URI value) {
		return value.getScheme() == null ? "" : value.getScheme().toLowerCase(Locale.ROOT);
	}

	private static int effectivePort(URI value) {
		if (value.getPort() >= 0) {
			return value.getPort();
		}
		return "https".equals(normalizedScheme(value)) ? 443 : 80;
	}

	record FetchResult(String content, String contentType) {
	}

	private record Origin(String scheme, String host, int port) {

		private static Origin from(URI value) {
			return new Origin(normalizedScheme(value), value.getHost().toLowerCase(Locale.ROOT), effectivePort(value));
		}

	}

}
