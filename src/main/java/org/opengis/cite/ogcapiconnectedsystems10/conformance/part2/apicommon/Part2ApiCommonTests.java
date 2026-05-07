package org.opengis.cite.ogcapiconnectedsystems10.conformance.part2.apicommon;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.opengis.cite.ogcapiconnectedsystems10.SuiteAttribute;
import org.testng.ITestContext;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * CS API Part 2 - API Common conformance subset tests ({@code /conf/api-common}; OGC
 * 23-002 Annex A).
 *
 * <p>
 * Implements the Sprint 20 read-only, declaration-gated subset of
 * <strong>REQ-ETS-PART2-001</strong>. This class intentionally does not close the full
 * Part 2 surface: JSON payload classes, stream semantics, write behavior, and full
 * endpoint parity remain open for later Part 2 sprints.
 * </p>
 */
public class Part2ApiCommonTests {

	static final String GROUP = "part2apicommon";

	static final String CONF_PART2_API_COMMON = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/api-common";

	static final String REQ_API_COMMON = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/api-common";

	static final String REQ_RESOURCES = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/api-common/resources";

	static final String REQ_RESOURCE_COLLECTION = "http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/req/api-common/resource-collection";

	private static final Set<String> PART2_COLLECTION_TOKENS = Set.of("datastreams", "observations", "controlstreams",
			"commands", "systemevents", "systemhistory");

	private URI iutUri;

	private URI baseUri;

	private Response landingResponse;

	private Map<String, Object> landingBody;

	private Response conformanceResponse;

	private Map<String, Object> conformanceBody;

	/**
	 * Fetches the read-only landing page and /conformance documents once. Collection
	 * probes run only after the IUT declares /conf/api-common.
	 * @param testContext TestNG test context.
	 */
	@BeforeClass
	public void fetchPart2ApiCommonInputs(ITestContext testContext) {
		Object iutAttr = testContext.getSuite().getAttribute(SuiteAttribute.IUT.getName());
		if (!(iutAttr instanceof URI)) {
			throw new SkipException("Suite attribute '" + SuiteAttribute.IUT.getName() + "' is missing or not a URI.");
		}
		this.iutUri = (URI) iutAttr;
		String iutString = this.iutUri.toString();
		this.baseUri = URI.create(iutString.endsWith("/") ? iutString : iutString + "/");

		this.landingResponse = given().accept("application/json").when().get(this.iutUri).andReturn();
		this.landingBody = parseBody(this.landingResponse);

		this.conformanceResponse = given().accept("application/json")
			.when()
			.get(this.baseUri.resolve("conformance"))
			.andReturn();
		this.conformanceBody = parseBody(this.conformanceResponse);
	}

	/**
	 * SCENARIO-ETS-PART2-001-API-COMMON-CONFORMANCE-DECLARED-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_API_COMMON
			+ ": /conformance declares /conf/api-common before Part 2 API Common assertions run (REQ-ETS-PART2-001, SCENARIO-ETS-PART2-001-API-COMMON-CONFORMANCE-DECLARED-001)",
			groups = GROUP)
	@SuppressWarnings("unchecked")
	public void part2ApiCommonConformanceDeclared() {
		ETSAssert.assertStatus(this.conformanceResponse, 200, REQ_API_COMMON);
		if (this.conformanceBody == null) {
			ETSAssert.failWithUri(REQ_API_COMMON, "/conformance body did not parse as JSON. Content-Type was: "
					+ this.conformanceResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.conformanceBody, "conformsTo", List.class, REQ_API_COMMON);
		List<Object> conformsTo = (List<Object>) this.conformanceBody.get("conformsTo");
		Predicate<Object> isPart2ApiCommon = CONF_PART2_API_COMMON::equals;
		if (!conformsTo.stream().anyMatch(isPart2ApiCommon)) {
			throw new SkipException(CONF_PART2_API_COMMON
					+ " - IUT does not declare the CS API Part 2 API Common conformance class in /conformance. "
					+ "Undeclared Part 2 API Common behavior is not conformance PASS evidence.");
		}
	}

	/**
	 * SCENARIO-ETS-PART2-001-RESOURCE-TERMINOLOGY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_RESOURCES
			+ ": landing-page Part 2 collection links use Connected Systems resource collection terminology (REQ-ETS-PART2-001, SCENARIO-ETS-PART2-001-RESOURCE-TERMINOLOGY-001)",
			groups = GROUP)
	public void part2ApiCommonResourceTerminology() {
		skipIfPart2ApiCommonUndeclared();
		ETSAssert.assertStatus(this.landingResponse, 200, REQ_RESOURCES);
		if (this.landingBody == null) {
			ETSAssert.failWithUri(REQ_RESOURCES, "landing page body did not parse as JSON. Content-Type was: "
					+ this.landingResponse.getContentType());
		}
		ETSAssert.assertJsonObjectHas(this.landingBody, "links", List.class, REQ_RESOURCES);
		List<URI> collectionUris = discoverPart2CollectionUris(this.landingBody, this.baseUri);
		if (collectionUris.isEmpty()) {
			throw new SkipException(REQ_RESOURCES
					+ " - landing page did not advertise any discoverable Part 2 resource collection links.");
		}
	}

	/**
	 * SCENARIO-ETS-PART2-001-RESOURCE-COLLECTION-READONLY-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_RESOURCE_COLLECTION
			+ ": discoverable Part 2 resource collections are readable JSON objects with items and links arrays (REQ-ETS-PART2-001, SCENARIO-ETS-PART2-001-RESOURCE-COLLECTION-READONLY-001)",
			groups = GROUP)
	public void part2ApiCommonResourceCollectionsReadable() {
		skipIfPart2ApiCommonUndeclared();
		if (this.landingBody == null) {
			ETSAssert.failWithUri(REQ_RESOURCE_COLLECTION, "landing page body did not parse as JSON. Content-Type was: "
					+ this.landingResponse.getContentType());
		}
		List<URI> collectionUris = discoverPart2CollectionUris(this.landingBody, this.baseUri);
		if (collectionUris.isEmpty()) {
			throw new SkipException(REQ_RESOURCE_COLLECTION
					+ " - landing page did not advertise any discoverable Part 2 resource collection links.");
		}
		for (URI collectionUri : collectionUris) {
			Response response = given().accept("application/json")
				.queryParam("limit", 1)
				.when()
				.get(collectionUri)
				.andReturn();
			ETSAssert.assertStatus(response, 200, REQ_RESOURCE_COLLECTION);
			Map<String, Object> body = parseBody(response);
			if (body == null) {
				ETSAssert.failWithUri(REQ_RESOURCE_COLLECTION,
						collectionUri + " body did not parse as JSON. Content-Type was: " + response.getContentType());
			}
			assertResourceCollectionShape(body, collectionUri.toString());
		}
	}

	/**
	 * SCENARIO-ETS-PART2-001-DEPENDENCY-SKIP-001.
	 */
	@Test(description = "OGC-23-002 " + REQ_API_COMMON
			+ ": Part 2 API Common tests are dependency-scoped to Core and Common and skip when /conf/api-common is undeclared (REQ-ETS-PART2-001, SCENARIO-ETS-PART2-001-DEPENDENCY-SKIP-001)",
			groups = GROUP)
	public void part2ApiCommonDependencyRuntime() {
		skipIfPart2ApiCommonUndeclared();
		Reporter.log("Part 2 API Common group reached runtime after Core/Common prerequisites resolved.", true);
	}

	private void skipIfPart2ApiCommonUndeclared() {
		if (!declaresConformance(this.conformanceBody, CONF_PART2_API_COMMON)) {
			throw new SkipException(CONF_PART2_API_COMMON
					+ " - IUT does not declare the CS API Part 2 API Common conformance class in /conformance.");
		}
	}

	static boolean declaresConformance(Map<String, Object> body, String conformanceUri) {
		if (body == null || conformanceUri == null) {
			return false;
		}
		Object conformsTo = body.get("conformsTo");
		if (!(conformsTo instanceof List)) {
			return false;
		}
		for (Object entry : (List<?>) conformsTo) {
			if (conformanceUri.equals(entry)) {
				return true;
			}
		}
		return false;
	}

	static List<URI> discoverPart2CollectionUris(Map<String, Object> landingBody, URI baseUri) {
		if (landingBody == null || baseUri == null) {
			return List.of();
		}
		Object links = landingBody.get("links");
		if (!(links instanceof List)) {
			return List.of();
		}
		Set<URI> discovered = new LinkedHashSet<>();
		for (Object link : (List<?>) links) {
			if (!(link instanceof Map)) {
				continue;
			}
			Map<?, ?> linkMap = (Map<?, ?>) link;
			Object href = linkMap.get("href");
			if (!(href instanceof String) || ((String) href).isBlank()) {
				continue;
			}
			String rel = asLowerString(linkMap.get("rel"));
			String path = pathToken((String) href);
			if (PART2_COLLECTION_TOKENS.contains(rel) || PART2_COLLECTION_TOKENS.contains(path)) {
				discovered.add(baseUri.resolve((String) href));
			}
		}
		return new ArrayList<>(discovered);
	}

	static boolean hasResourceCollectionShape(Map<String, Object> body) {
		return body != null && body.get("items") instanceof List && body.get("links") instanceof List;
	}

	static void assertResourceCollectionShape(Map<String, Object> body, String source) {
		if (!hasResourceCollectionShape(body)) {
			ETSAssert.failWithUri(REQ_RESOURCE_COLLECTION,
					source + " did not expose a CS API resource collection JSON object with items[] and links[].");
		}
	}

	private static String pathToken(String href) {
		try {
			String path = URI.create(href).getPath();
			if (path == null || path.isBlank()) {
				return "";
			}
			String[] parts = path.toLowerCase(Locale.ROOT).split("/");
			for (int i = parts.length - 1; i >= 0; i--) {
				if (!parts[i].isBlank()) {
					return parts[i];
				}
			}
			return "";
		}
		catch (IllegalArgumentException ex) {
			return "";
		}
	}

	private static String asLowerString(Object value) {
		return value instanceof String ? ((String) value).toLowerCase(Locale.ROOT) : "";
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> parseBody(Response response) {
		if (response == null || response.getBody() == null) {
			return null;
		}
		try {
			return response.jsonPath().getMap("$");
		}
		catch (Exception ex) {
			return null;
		}
	}

}
