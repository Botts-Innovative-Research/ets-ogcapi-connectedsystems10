package org.opengis.cite.ogcapiconnectedsystems10.conformance;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.opengis.cite.ogcapiconnectedsystems10.TestRunArg;
import org.testng.ITestContext;
import org.testng.SkipException;

import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Shared safety and evidence helpers for encoding mediatype-write checks.
 */
public final class EncodingMediatypeWrite {

	public static final String CONF_CREATE_REPLACE_DELETE = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/create-replace-delete";

	public static final String GEOJSON_CONTENT_TYPE = "application/geo+json";

	public static final String SENSORML_CONTENT_TYPE = "application/sml+json";

	private static final String ENABLED_VALUE = "true";

	private static final String DEDICATED_MUTABLE_IUT_POLICY = "dedicated-mutable-iut";

	private EncodingMediatypeWrite() {
	}

	public static void ensureMutationEnabledOrSkip(ITestContext testContext, URI iutUri, String requirementUri) {
		String mutationTestsEnabled = suiteString(testContext, SuiteAttribute.MUTATION_TESTS_ENABLED);
		String mutationIutPolicy = suiteString(testContext, SuiteAttribute.MUTATION_IUT_POLICY);
		if (!ENABLED_VALUE.equals(mutationTestsEnabled) || !DEDICATED_MUTABLE_IUT_POLICY.equals(mutationIutPolicy)) {
			throw new SkipException(requirementUri + " - encoding mediatype-write lifecycle tests are disabled. Set "
					+ TestRunArg.MUTATION_TESTS_ENABLED + "=true and " + TestRunArg.MUTATION_IUT_POLICY
					+ "=dedicated-mutable-iut to permit POST/PUT parsing checks. No POST/PUT/DELETE request was issued.");
		}
		if (isPublicGeoRobotixIut(iutUri)) {
			throw new SkipException(requirementUri
					+ " - known shared public GeoRobotix IUT is hard-denied for encoding mediatype-write mutation tests. No POST/PUT/DELETE request was issued.");
		}
	}

	public static void skipIfConformanceMissing(Map<String, Object> conformanceBody, String conformanceClass,
			String requirementUri) {
		if (!declaresConformance(conformanceBody, conformanceClass)) {
			throw new SkipException(requirementUri + " - IUT does not declare " + conformanceClass
					+ "; write-side encoding behavior is not conformance PASS evidence.");
		}
	}

	public static boolean declaresConformance(Map<String, Object> conformanceBody, String conformanceClass) {
		if (conformanceBody == null) {
			return false;
		}
		Object conformsToObj = conformanceBody.get("conformsTo");
		return conformsToObj instanceof List && ((List<?>) conformsToObj).contains(conformanceClass);
	}

	public static boolean isPublicGeoRobotixIut(URI iutUri) {
		String host = iutUri.getHost();
		String uri = iutUri.toString();
		return "api.georobotix.io".equalsIgnoreCase(host)
				|| "https://api.georobotix.io/ogc/t18/api".equalsIgnoreCase(stripTrailingSlash(uri));
	}

	public static Map<String, Object> geoJsonSystemBody(String phase, String uid) {
		Map<String, Object> geometry = new LinkedHashMap<>();
		geometry.put("type", "Point");
		geometry.put("coordinates", List.of(-77.0365, 38.8977));

		Map<String, Object> properties = new LinkedHashMap<>();
		properties.put("uid", uid);
		properties.put("featureType", "http://www.w3.org/ns/sosa/System");
		properties.put("name", "ETS mediatype-write GeoJSON " + phase + " " + Instant.now());
		properties.put("description", "Temporary system resource created by the ETS GeoJSON mediatype-write test.");

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("type", "Feature");
		body.put("geometry", geometry);
		body.put("properties", properties);
		return body;
	}

	public static Map<String, Object> sensorMlSystemBody(String phase, String uid) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("type", "PhysicalSystem");
		body.put("uniqueId", uid);
		body.put("label", "ETS mediatype-write SensorML " + phase + " " + Instant.now());
		body.put("description", "Temporary system resource created by the ETS SensorML mediatype-write test.");
		return body;
	}

	public static String mutableSystemUid(String encodingName) {
		return "urn:ets:ogcapi-connectedsystems10:" + encodingName + "-mediatype-write:" + UUID.randomUUID();
	}

	public static RequestSpecification givenWithoutDefaultCharset() {
		return RestAssured.given()
			.config(RestAssured.config()
				.encoderConfig(
						EncoderConfig.encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false)));
	}

	public static String createdResourceUri(Response response, URI iutUri, String base) {
		String location = response.getHeader("Location");
		if (location != null && !location.isBlank()) {
			return resolveResourceUri(iutUri, base, location);
		}
		Map<String, Object> body = parseBody(response);
		String id = systemId(body);
		if (id == null || id.isBlank()) {
			return null;
		}
		return base + "systems/" + id;
	}

	public static String resolveResourceUri(URI iutUri, String base, String location) {
		URI locationUri = URI.create(location);
		if (locationUri.isAbsolute()) {
			return locationUri.toString();
		}
		if (location.startsWith("/")) {
			String iutPath = iutUri.getPath();
			boolean alreadyIncludesIutPath = iutPath != null && !iutPath.isBlank() && !"/".equals(iutPath)
					&& (location.equals(iutPath) || location.startsWith(iutPath + "/"));
			if (!alreadyIncludesIutPath) {
				return base + location.substring(1);
			}
			return iutUri.resolve(location).toString();
		}
		return URI.create(base).resolve(location).toString();
	}

	public static void assertStatusIn(Response response, List<Integer> expected, String requirementUri,
			String operation) {
		int actual = response.getStatusCode();
		if (!expected.contains(actual)) {
			ETSAssert.failWithUri(requirementUri,
					operation + " expected HTTP status in " + expected + ", got " + actual);
		}
	}

	public static void assertDereferencedResourcePreservesUid(Map<String, Object> body, String expectedUid,
			String requirementUri) {
		if (body == null) {
			ETSAssert.failWithUri(requirementUri,
					"Follow-up dereference did not return parseable JSON; status-only write response is not mediatype-write PASS evidence.");
		}
		String directUid = asString(body.get("uid"));
		String uniqueId = asString(body.get("uniqueId"));
		String propertiesUid = propertiesUid(body);
		if (!expectedUid.equals(directUid) && !expectedUid.equals(uniqueId) && !expectedUid.equals(propertiesUid)) {
			ETSAssert.failWithUri(requirementUri,
					"Follow-up dereference did not preserve submitted uid/uniqueId " + expectedUid + "; observed uid="
							+ directUid + ", uniqueId=" + uniqueId + ", properties.uid=" + propertiesUid + ".");
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> parseBody(Response response) {
		try {
			return response.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			return null;
		}
	}

	private static String suiteString(ITestContext testContext, SuiteAttribute attr) {
		Object value = testContext.getSuite().getAttribute(attr.getName());
		return value instanceof String ? (String) value : null;
	}

	private static String stripTrailingSlash(String value) {
		return value != null && value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
	}

	@SuppressWarnings("unchecked")
	private static String systemId(Map<String, Object> body) {
		if (body == null) {
			return null;
		}
		String directId = asString(body.get("id"));
		if (directId != null) {
			return directId;
		}
		Object propertiesObj = body.get("properties");
		if (propertiesObj instanceof Map) {
			return asString(((Map<String, Object>) propertiesObj).get("id"));
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static String propertiesUid(Map<String, Object> body) {
		Object propertiesObj = body.get("properties");
		if (!(propertiesObj instanceof Map)) {
			return null;
		}
		return asString(((Map<String, Object>) propertiesObj).get("uid"));
	}

	private static String asString(Object value) {
		return value instanceof String ? (String) value : null;
	}

}
