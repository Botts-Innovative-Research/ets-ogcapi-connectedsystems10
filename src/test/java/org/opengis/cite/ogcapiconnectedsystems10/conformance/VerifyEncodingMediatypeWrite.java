package org.opengis.cite.ogcapiconnectedsystems10.conformance;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.testng.SkipException;

/**
 * Regression coverage for S-ETS-19-01 mediatype-write safety behavior.
 *
 * <p>
 * Traceability: REQ-ETS-PART1-012, REQ-ETS-PART1-013, REQ-ETS-PART1-010,
 * SCENARIO-ETS-PART1-012-GEOJSON-MEDIATYPE-WRITE-SAFETY-GATED-001,
 * SCENARIO-ETS-PART1-013-SENSORML-MEDIATYPE-WRITE-SAFETY-GATED-001, and
 * SCENARIO-ETS-PART1-012-013-MEDIATYPE-WRITE-PARSE-EVIDENCE-001.
 * </p>
 */
public class VerifyEncodingMediatypeWrite {

	private static final String REQ = "http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/geojson/mediatype-write";

	@Test
	public void publicGeoRobotixIutIsHardDenied() {
		assertTrue(EncodingMediatypeWrite.isPublicGeoRobotixIut(URI.create("https://api.georobotix.io/ogc/t18/api")));
		assertTrue(EncodingMediatypeWrite.isPublicGeoRobotixIut(URI.create("https://api.georobotix.io/ogc/t18/api/")));
		assertFalse(
				EncodingMediatypeWrite.isPublicGeoRobotixIut(URI.create("http://field-hub-osh-1:8081/sensorhub/api")));
	}

	@Test
	public void missingEncodingConformanceSkipsBeforeMutationEvidence() {
		Map<String, Object> conformance = Map.of("conformsTo",
				java.util.List.of(EncodingMediatypeWrite.CONF_CREATE_REPLACE_DELETE));

		SkipException error = assertThrows(SkipException.class,
				() -> EncodingMediatypeWrite.skipIfConformanceMissing(conformance,
						"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/sensorml", REQ));

		assertTrue(error.getMessage().contains("does not declare"));
	}

	@Test
	public void dereferenceEvidenceAcceptsGeoJsonPropertiesUid() {
		EncodingMediatypeWrite.assertDereferencedResourcePreservesUid(
				Map.of("type", "Feature", "properties", Map.of("uid", "urn:test:1")), "urn:test:1", REQ);
	}

	@Test
	public void dereferenceEvidenceAcceptsSensorMlUniqueId() {
		EncodingMediatypeWrite.assertDereferencedResourcePreservesUid(
				Map.of("type", "PhysicalSystem", "uniqueId", "urn:test:2"), "urn:test:2", REQ);
	}

	@Test
	public void statusOnlyBodyCannotCreateParseEvidencePass() {
		AssertionError error = assertThrows(AssertionError.class,
				() -> EncodingMediatypeWrite.assertDereferencedResourcePreservesUid(null, "urn:test:3", REQ));

		assertTrue(error.getMessage().contains("status-only write response"));
	}

	@Test
	public void wrongDereferencedIdentityFailsParseEvidence() {
		AssertionError error = assertThrows(AssertionError.class,
				() -> EncodingMediatypeWrite.assertDereferencedResourcePreservesUid(
						Map.of("type", "Feature", "properties", Map.of("uid", "urn:test:other")), "urn:test:3", REQ));

		assertTrue(error.getMessage().contains("did not preserve submitted"));
	}

	@Test
	public void exactContentTypesAreExposedForRuntimeChecks() {
		org.junit.Assert.assertEquals("application/geo+json", EncodingMediatypeWrite.GEOJSON_CONTENT_TYPE);
		org.junit.Assert.assertEquals("application/sml+json", EncodingMediatypeWrite.SENSORML_CONTENT_TYPE);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void geoJsonSystemBodyCarriesOshCompatibleSystemFeatureEvidence() {
		Map<String, Object> body = EncodingMediatypeWrite.geoJsonSystemBody("create", "urn:test:geojson");
		Map<String, Object> geometry = (Map<String, Object>) body.get("geometry");
		Map<String, Object> properties = (Map<String, Object>) body.get("properties");

		org.junit.Assert.assertEquals("Point", geometry.get("type"));
		assertTrue(geometry.get("coordinates") instanceof List);
		org.junit.Assert.assertEquals("urn:test:geojson", properties.get("uid"));
		org.junit.Assert.assertEquals("http://www.w3.org/ns/sosa/System", properties.get("featureType"));
	}

}
