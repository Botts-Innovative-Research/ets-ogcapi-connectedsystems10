package org.opengis.cite.ogcapiconnectedsystems10;

import java.io.IOException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class VerifyETSAssert {

	private static final String WADL_NS = "http://wadl.dev.java.net/2009/02";

	private static DocumentBuilder docBuilder;

	private static SchemaFactory factory;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public VerifyETSAssert() {
	}

	@BeforeClass
	public static void setUpClass() throws ParserConfigurationException {
		factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		docBuilder = dbf.newDocumentBuilder();
	}

	@Test
	public void validateUsingSchemaHints_expect2Errors() throws SAXException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("2 schema validation error(s) detected");
		URL url = this.getClass().getResource("/Gamma.xml");
		Schema schema = factory.newSchema();
		Validator validator = schema.newValidator();
		ETSAssert.assertSchemaValid(validator, new StreamSource(url.toString()));
	}

	@Test
	public void assertXPathWithNamespaceBindings() throws SAXException, IOException {
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream("/capabilities-simple.xml"));
		Map<String, String> nsBindings = new HashMap<String, String>();
		nsBindings.put(WADL_NS, "ns1");
		String xpath = "//ns1:resources";
		ETSAssert.assertXPath(xpath, doc, nsBindings);
	}

	@Test
	public void assertXPath_expectFalse() throws SAXException, IOException {
		thrown.expect(AssertionError.class);
		thrown.expectMessage("Unexpected result evaluating XPath expression");
		Document doc = docBuilder.parse(this.getClass().getResourceAsStream("/capabilities-simple.xml"));
		// using built-in namespace binding
		String xpath = "//ows:OperationsMetadata/ows:Constraint[@name='XMLEncoding']/ows:DefaultValue = 'TRUE'";
		ETSAssert.assertXPath(xpath, doc, null);
	}

	// ------------------------------------------------------------------
	// REST/JSON helper tests (REQ-ETS-CLEANUP-001, ADR-008)
	//
	// Covers: SCENARIO-ETS-CLEANUP-ETSASSERT-REFACTOR-001 — every helper has at least
	// one PASS-path + one FAIL-path test. Each helper raises AssertionError with the
	// /req/* URI as the message prefix.
	// ------------------------------------------------------------------

	private static final String REQ_TEST_URI = "http://www.opengis.net/spec/ogcapi-common-1/1.0/req/landing-page/root-success";

	// ----- assertStatus -----

	@Test
	public void assertStatus_passWhenStatusMatches() {
		io.restassured.response.Response resp = Mockito.mock(io.restassured.response.Response.class);
		Mockito.when(resp.getStatusCode()).thenReturn(200);
		ETSAssert.assertStatus(resp, 200, REQ_TEST_URI);
	}

	@Test
	public void assertStatus_failWhenStatusDiffers() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage(REQ_TEST_URI);
		thrown.expectMessage("expected HTTP 200");
		thrown.expectMessage("got 404");
		io.restassured.response.Response resp = Mockito.mock(io.restassured.response.Response.class);
		Mockito.when(resp.getStatusCode()).thenReturn(404);
		ETSAssert.assertStatus(resp, 200, REQ_TEST_URI);
	}

	// ----- assertJsonObjectHas -----

	@Test
	public void assertJsonObjectHas_passWhenKeyHasExpectedType() {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("title", "GeoRobotix CS API");
		body.put("links", List.of());
		ETSAssert.assertJsonObjectHas(body, "title", String.class, REQ_TEST_URI);
		ETSAssert.assertJsonObjectHas(body, "links", List.class, REQ_TEST_URI);
		// Object.class accepts any non-null value.
		ETSAssert.assertJsonObjectHas(body, "title", Object.class, REQ_TEST_URI);
	}

	@Test
	public void assertJsonObjectHas_failWhenKeyMissing() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage(REQ_TEST_URI);
		thrown.expectMessage("expected key 'links'");
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("title", "GeoRobotix");
		ETSAssert.assertJsonObjectHas(body, "links", List.class, REQ_TEST_URI);
	}

	@Test
	public void assertJsonObjectHas_failWhenTypeMismatch() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage(REQ_TEST_URI);
		thrown.expectMessage("expected key 'links' of type List");
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("links", "not-a-list");
		ETSAssert.assertJsonObjectHas(body, "links", List.class, REQ_TEST_URI);
	}

	// ----- assertJsonArrayContains -----

	@Test
	public void assertJsonArrayContains_passWhenPredicateMatches() {
		Map<String, Object> link1 = Map.of("rel", "conformance", "href", "/conformance");
		Map<String, Object> link2 = Map.of("rel", "service-desc", "href", "/api");
		List<?> links = List.of(link1, link2);
		Predicate<Object> isConformance = l -> "conformance".equals(((Map<?, ?>) l).get("rel"));
		ETSAssert.assertJsonArrayContains(links, isConformance, "rel=conformance link", REQ_TEST_URI);
	}

	@Test
	public void assertJsonArrayContains_failWhenNoMatch() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage(REQ_TEST_URI);
		thrown.expectMessage("rel=conformance link");
		List<?> links = List.of(Map.of("rel", "self", "href", "/"));
		Predicate<Object> isConformance = l -> "conformance".equals(((Map<?, ?>) l).get("rel"));
		ETSAssert.assertJsonArrayContains(links, isConformance, "rel=conformance link", REQ_TEST_URI);
	}

	// ----- assertJsonArrayContainsAnyOf -----

	@Test
	public void assertJsonArrayContainsAnyOf_passWhenAtLeastOneMatches() {
		List<?> links = List.of(Map.of("rel", "service-doc", "href", "/api.html"));
		Predicate<Object> isServiceDesc = l -> "service-desc".equals(((Map<?, ?>) l).get("rel"));
		Predicate<Object> isServiceDoc = l -> "service-doc".equals(((Map<?, ?>) l).get("rel"));
		ETSAssert.assertJsonArrayContainsAnyOf(links,
				List.of(new AbstractMap.SimpleEntry<>("service-desc", isServiceDesc),
						new AbstractMap.SimpleEntry<>("service-doc", isServiceDoc)),
				REQ_TEST_URI);
	}

	@Test
	public void assertJsonArrayContainsAnyOf_failWhenNoneMatches() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage(REQ_TEST_URI);
		thrown.expectMessage("service-desc");
		thrown.expectMessage("service-doc");
		List<?> links = List.of(Map.of("rel", "self", "href", "/"));
		Predicate<Object> isServiceDesc = l -> "service-desc".equals(((Map<?, ?>) l).get("rel"));
		Predicate<Object> isServiceDoc = l -> "service-doc".equals(((Map<?, ?>) l).get("rel"));
		ETSAssert.assertJsonArrayContainsAnyOf(links,
				List.of(new AbstractMap.SimpleEntry<>("service-desc", isServiceDesc),
						new AbstractMap.SimpleEntry<>("service-doc", isServiceDoc)),
				REQ_TEST_URI);
	}

	// ----- failWithUri -----

	@Test
	public void failWithUri_alwaysThrowsWithUriPrefix() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage(REQ_TEST_URI);
		thrown.expectMessage("custom failure context");
		ETSAssert.failWithUri(REQ_TEST_URI, "custom failure context");
	}

	@Test
	public void failWithUri_handlesNullMessage() {
		thrown.expect(AssertionError.class);
		thrown.expectMessage(REQ_TEST_URI);
		ETSAssert.failWithUri(REQ_TEST_URI, null);
	}

	// ----- programming-error guards -----

	@Test
	public void assertStatus_nullUriRaisesIllegalArgument() {
		thrown.expect(IllegalArgumentException.class);
		io.restassured.response.Response resp = Mockito.mock(io.restassured.response.Response.class);
		ETSAssert.assertStatus(resp, 200, null);
	}

}
