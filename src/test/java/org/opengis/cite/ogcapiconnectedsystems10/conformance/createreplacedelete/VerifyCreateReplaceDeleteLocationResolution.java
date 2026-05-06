package org.opengis.cite.ogcapiconnectedsystems10.conformance.createreplacedelete;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.Map;

import org.junit.Test;

/**
 * Regression coverage for S-ETS-12-01 local mutable-IUT probing.
 */
public class VerifyCreateReplaceDeleteLocationResolution {

	private static final URI OSH_IUT = URI.create("http://localhost:8081/sensorhub/api");

	private static final String OSH_BASE = "http://localhost:8081/sensorhub/api/";

	@Test
	public void resolvesAbsoluteLocationUnchanged() {
		String location = "http://localhost:8081/sensorhub/api/systems/040g";

		assertEquals(location, CreateReplaceDeleteTests.resolveCreatedResourceUri(OSH_IUT, OSH_BASE, location));
	}

	@Test
	public void resolvesServiceRelativeLocationAgainstIutBase() {
		assertEquals("http://localhost:8081/sensorhub/api/systems/040g",
				CreateReplaceDeleteTests.resolveCreatedResourceUri(OSH_IUT, OSH_BASE, "/systems/040g"));
	}

	@Test
	public void resolvesPathThatAlreadyIncludesIutPathAgainstOrigin() {
		assertEquals("http://localhost:8081/sensorhub/api/systems/040g",
				CreateReplaceDeleteTests.resolveCreatedResourceUri(OSH_IUT, OSH_BASE, "/sensorhub/api/systems/040g"));
	}

	@Test
	public void resolvesRelativeLocationAgainstIutBase() {
		assertEquals("http://localhost:8081/sensorhub/api/systems/040g",
				CreateReplaceDeleteTests.resolveCreatedResourceUri(OSH_IUT, OSH_BASE, "systems/040g"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void replacementBodyPreservesCreatedSystemUid() {
		String uid = "urn:ets:ogcapi-connectedsystems10:crd:test";

		Map<String, Object> createProperties = (Map<String, Object>) CreateReplaceDeleteTests
			.mutableSystemBody("create", uid)
			.get("properties");
		Map<String, Object> replaceProperties = (Map<String, Object>) CreateReplaceDeleteTests
			.mutableSystemBody("replace", uid)
			.get("properties");

		assertEquals(uid, createProperties.get("uid"));
		assertEquals(uid, replaceProperties.get("uid"));
	}

}
