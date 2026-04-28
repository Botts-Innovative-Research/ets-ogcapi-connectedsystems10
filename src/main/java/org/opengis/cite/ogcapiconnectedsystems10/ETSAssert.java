package org.opengis.cite.ogcapiconnectedsystems10;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.opengis.cite.ogcapiconnectedsystems10.util.NamespaceBindings;
import org.opengis.cite.ogcapiconnectedsystems10.util.XMLUtils;
import org.opengis.cite.validation.SchematronValidator;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.testng.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jakarta.ws.rs.core.Response;

/**
 * Provides a set of custom assertion methods.
 */
public class ETSAssert {

	private static final Logger LOGR = Logger.getLogger(ETSAssert.class.getPackage().getName());

	private ETSAssert() {
	}

	/**
	 * Asserts that the qualified name of a DOM Node matches the expected value.
	 * @param node The Node to check.
	 * @param qName A QName object containing a namespace name (URI) and a local part.
	 */
	public static void assertQualifiedName(Node node, QName qName) {
		Assert.assertEquals(node.getLocalName(), qName.getLocalPart(), ErrorMessage.get(ErrorMessageKeys.LOCAL_NAME));
		Assert.assertEquals(node.getNamespaceURI(), qName.getNamespaceURI(),
				ErrorMessage.get(ErrorMessageKeys.NAMESPACE_NAME));
	}

	/**
	 * Asserts that an XPath 1.0 expression holds true for the given evaluation context.
	 * The following standard namespace bindings do not need to be explicitly declared:
	 *
	 * <ul>
	 * <li>ows: {@value org.opengis.cite.ogcapiconnectedsystems10.Namespaces#OWS}</li>
	 * <li>xlink: {@value org.opengis.cite.ogcapiconnectedsystems10.Namespaces#XLINK}</li>
	 * <li>gml: {@value org.opengis.cite.ogcapiconnectedsystems10.Namespaces#GML}</li>
	 * </ul>
	 * @param expr A valid XPath 1.0 expression.
	 * @param context The context node.
	 * @param namespaceBindings A collection of namespace bindings for the XPath
	 * expression, where each entry maps a namespace URI (key) to a prefix (value). It may
	 * be {@code null}.
	 */
	public static void assertXPath(String expr, Node context, Map<String, String> namespaceBindings) {
		if (null == context) {
			throw new NullPointerException("Context node is null.");
		}
		NamespaceBindings bindings = NamespaceBindings.withStandardBindings();
		bindings.addAllBindings(namespaceBindings);
		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(bindings);
		Boolean result;
		try {
			result = (Boolean) xpath.evaluate(expr, context, XPathConstants.BOOLEAN);
		}
		catch (XPathExpressionException xpe) {
			String msg = ErrorMessage.format(ErrorMessageKeys.XPATH_ERROR, expr);
			LOGR.log(Level.WARNING, msg, xpe);
			throw new AssertionError(msg);
		}
		Element elemNode;
		if (Document.class.isInstance(context)) {
			elemNode = Document.class.cast(context).getDocumentElement();
		}
		else {
			elemNode = (Element) context;
		}
		Assert.assertTrue(result, ErrorMessage.format(ErrorMessageKeys.XPATH_RESULT, elemNode.getNodeName(), expr));
	}

	/**
	 * Asserts that an XML resource is schema-valid.
	 * @param validator The Validator to use.
	 * @param source The XML Source to be validated.
	 */
	public static void assertSchemaValid(Validator validator, Source source) {
		ValidationErrorHandler errHandler = new ValidationErrorHandler();
		validator.setErrorHandler(errHandler);
		try {
			validator.validate(source);
		}
		catch (Exception e) {
			throw new AssertionError(ErrorMessage.format(ErrorMessageKeys.XML_ERROR, e.getMessage()));
		}
		Assert.assertFalse(errHandler.errorsDetected(), ErrorMessage.format(ErrorMessageKeys.NOT_SCHEMA_VALID,
				errHandler.getErrorCount(), errHandler.toString()));
	}

	/**
	 * Asserts that an XML resource satisfies all applicable constraints defined for the
	 * specified phase in a Schematron (ISO 19757-3) schema. The "xslt2" query language
	 * binding is supported. Two phase names have special meanings:
	 * <ul>
	 * <li>"#ALL": All patterns are active</li>
	 * <li>"#DEFAULT": The phase identified by the defaultPhase attribute on the schema
	 * element should be used.</li>
	 * </ul>
	 * @param schemaRef A URL that denotes the location of a Schematron schema.
	 * @param xmlSource The XML Source to be validated.
	 * @param activePhase The active phase (pattern set) whose patterns are used for
	 * validation; this is set to "#ALL" if not specified.
	 */
	public static void assertSchematronValid(URL schemaRef, Source xmlSource, String activePhase) {
		String phase = (null == activePhase || activePhase.isEmpty()) ? "#ALL" : activePhase;
		SchematronValidator validator;
		try {
			validator = new SchematronValidator(new StreamSource(schemaRef.toString()), phase);
		}
		catch (Exception e) {
			StringBuilder msg = new StringBuilder("Failed to process Schematron schema at ");
			msg.append(schemaRef).append('\n');
			msg.append(e.getMessage());
			throw new AssertionError(msg);
		}
		DOMResult result = (DOMResult) validator.validate(xmlSource);
		Assert.assertFalse(validator.ruleViolationsDetected(), ErrorMessage.format(ErrorMessageKeys.NOT_SCHEMA_VALID,
				validator.getRuleViolationCount(), XMLUtils.writeNodeToString(result.getNode())));
	}

	// ---------------------------------------------------------------------
	// REST/JSON helpers (REQ-ETS-CLEANUP-001, ADR-008 — Sprint 2 forward-looking)
	//
	// Every helper raises java.lang.AssertionError (NOT TestNG SkipException) with the
	// supplied OGC /req/* URI as the message prefix per REQ-ETS-CORE-001 structured-FAIL
	// discipline. These helpers replace the 21 bare-throw sites in conformance.core.*
	// (Sprint 1 GAP-1 from Quinn s02 + Raze s02) and bind every conformance.* class
	// added in Sprint 2+ (zero `throw new AssertionError` permitted).
	//
	// Helpers operate on REST-Assured io.restassured.response.Response and parsed-JSON
	// Map<String,Object>/List<?> shapes (the same shapes Sprint 1 already produces via
	// `.jsonPath().getMap("$")` / `.getList("links")`). They do NOT take raw JSON
	// strings.
	// ---------------------------------------------------------------------

	/**
	 * Asserts the HTTP response status code matches expected. On failure, raises
	 * AssertionError with prefix "{reqUri} — expected HTTP {expected}, got {actual}".
	 *
	 * <p>
	 * Use for the most common Sprint 1 pattern:
	 * {@code if (resp.getStatusCode() != 200) throw new AssertionError(REQ_X + ...)}.
	 * </p>
	 * @param resp the REST-Assured Response under test (non-null)
	 * @param expected expected HTTP status code
	 * @param reqUri the OGC /req/* URI being asserted (non-null, non-empty)
	 * @throws IllegalArgumentException if {@code resp} or {@code reqUri} is null
	 * @throws AssertionError if the actual status differs from expected
	 */
	public static void assertStatus(io.restassured.response.Response resp, int expected, String reqUri) {
		requireUri(reqUri);
		if (resp == null) {
			throw new IllegalArgumentException("ETSAssert.assertStatus: resp must not be null (reqUri=" + reqUri + ")");
		}
		int actual = resp.getStatusCode();
		if (actual != expected) {
			throw new AssertionError(reqUri + " — expected HTTP " + expected + ", got " + actual);
		}
	}

	/**
	 * Asserts a parsed JSON object body has the named key with a value of the expected
	 * Java type. On failure, raises AssertionError with prefix "{reqUri} — expected key
	 * '{key}' of type {type.getSimpleName()}; got {actual}".
	 *
	 * <p>
	 * Type may be {@code String.class}, {@code Number.class}, {@code Boolean.class},
	 * {@code List.class}, {@code Map.class}, or {@code Object.class}
	 * ({@code Object.class} accepts any non-null value — used for "key must be present"
	 * checks where the type is not load-bearing).
	 * </p>
	 * @param body parsed JSON body as a Map (non-null)
	 * @param key the JSON key to look up (non-null, non-empty)
	 * @param type expected Java type of the value (non-null; use Object.class for "any
	 * non-null")
	 * @param reqUri the OGC /req/* URI being asserted
	 * @throws IllegalArgumentException if any required argument is null
	 * @throws AssertionError if the key is missing or the value type does not match
	 */
	public static void assertJsonObjectHas(Map<String, Object> body, String key, Class<?> type, String reqUri) {
		requireUri(reqUri);
		if (body == null) {
			throw new AssertionError(reqUri + " — expected JSON object body but body was null (key='" + key + "')");
		}
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("ETSAssert.assertJsonObjectHas: key must be non-null/non-empty");
		}
		if (type == null) {
			throw new IllegalArgumentException("ETSAssert.assertJsonObjectHas: type must be non-null");
		}
		if (!body.containsKey(key)) {
			throw new AssertionError(reqUri + " — expected key '" + key + "' of type " + type.getSimpleName()
					+ " in JSON body; key is missing. Available keys: " + body.keySet());
		}
		Object value = body.get(key);
		if (value == null) {
			throw new AssertionError(reqUri + " — expected key '" + key + "' of type " + type.getSimpleName()
					+ " in JSON body; value is null");
		}
		if (!type.isInstance(value)) {
			throw new AssertionError(reqUri + " — expected key '" + key + "' of type " + type.getSimpleName()
					+ " in JSON body; got " + value.getClass().getSimpleName() + " (value=" + value + ")");
		}
	}

	/**
	 * Asserts a parsed JSON array contains at least one element matching the predicate.
	 * On failure, raises AssertionError with prefix "{reqUri} — array did not contain any
	 * element matching: {desc}".
	 *
	 * <p>
	 * Use for "links contains rel=conformance" / "conformsTo contains URI X" assertions.
	 * </p>
	 * @param array parsed JSON array (non-null; empty array is a failure)
	 * @param pred predicate to test each element (non-null)
	 * @param desc human-readable description of what the predicate is looking for (e.g.
	 * "rel=conformance link"); used in the FAIL message
	 * @param reqUri the OGC /req/* URI being asserted
	 * @throws IllegalArgumentException if {@code pred}, {@code desc}, or {@code reqUri}
	 * is null
	 * @throws AssertionError if no element matches the predicate
	 */
	public static void assertJsonArrayContains(List<?> array, Predicate<Object> pred, String desc, String reqUri) {
		requireUri(reqUri);
		if (pred == null) {
			throw new IllegalArgumentException("ETSAssert.assertJsonArrayContains: pred must not be null");
		}
		if (desc == null) {
			throw new IllegalArgumentException("ETSAssert.assertJsonArrayContains: desc must not be null");
		}
		if (array == null) {
			throw new AssertionError(
					reqUri + " — array did not contain any element matching: " + desc + " (array was null)");
		}
		for (Object element : array) {
			if (pred.test(element)) {
				return;
			}
		}
		throw new AssertionError(reqUri + " — array did not contain any element matching: " + desc + " (array size="
				+ array.size() + ")");
	}

	/**
	 * Asserts a parsed JSON array contains at least one element matching at least ONE of
	 * a list of acceptable predicates. Used for OR-fallback patterns (e.g. service-desc
	 * OR service-doc; rel=collection OR rel=items). On failure, raises AssertionError
	 * with prefix "{reqUri} — array did not contain any element matching ANY of:
	 * {descs}".
	 * @param array parsed JSON array (non-null)
	 * @param alternatives list of (description, predicate) pairs; the array must match at
	 * least one of these alternatives
	 * @param reqUri the OGC /req/* URI being asserted
	 * @throws IllegalArgumentException if {@code alternatives} is null/empty or
	 * {@code reqUri} is null
	 * @throws AssertionError if no element matches any of the alternatives
	 */
	public static void assertJsonArrayContainsAnyOf(List<?> array,
			List<Map.Entry<String, Predicate<Object>>> alternatives, String reqUri) {
		requireUri(reqUri);
		if (alternatives == null || alternatives.isEmpty()) {
			throw new IllegalArgumentException(
					"ETSAssert.assertJsonArrayContainsAnyOf: alternatives must be non-null and non-empty");
		}
		String descs = alternatives.stream().map(Map.Entry::getKey).collect(Collectors.joining(", "));
		if (array == null) {
			throw new AssertionError(
					reqUri + " — array did not contain any element matching ANY of: " + descs + " (array was null)");
		}
		for (Map.Entry<String, Predicate<Object>> alt : alternatives) {
			Predicate<Object> pred = alt.getValue();
			if (pred == null) {
				continue;
			}
			for (Object element : array) {
				if (pred.test(element)) {
					return;
				}
			}
		}
		throw new AssertionError(reqUri + " — array did not contain any element matching ANY of: " + descs
				+ " (array size=" + array.size() + ")");
	}

	/**
	 * Universal escape hatch: raise AssertionError with the standard URI-prefixed format,
	 * for use when the assertion logic is too custom for the type-specific helpers above.
	 * Equivalent to {@code throw new AssertionError(reqUri + " — " + message)}.
	 * @param reqUri the OGC /req/* URI being asserted
	 * @param message free-form failure message (the URI is prepended; do not include it
	 * in {@code message})
	 * @throws IllegalArgumentException if {@code reqUri} is null
	 * @throws AssertionError always (this method never returns normally)
	 */
	public static void failWithUri(String reqUri, String message) {
		requireUri(reqUri);
		String msg = (message == null) ? "" : message;
		throw new AssertionError(reqUri + " — " + msg);
	}

	/**
	 * Internal guard: every REST/JSON helper requires a non-null /req/* URI. A null URI
	 * is a programming error (the structured-FAIL discipline requires it), not a test
	 * failure, so this raises IllegalArgumentException not AssertionError.
	 */
	private static void requireUri(String reqUri) {
		if (reqUri == null) {
			throw new IllegalArgumentException(
					"ETSAssert: reqUri must not be null (REQ-ETS-CORE-001 structured-FAIL discipline).");
		}
	}

	/**
	 * Asserts that the given XML entity contains the expected number of descendant
	 * elements having the specified name.
	 * @param xmlEntity A Document representing an XML entity.
	 * @param elementName The qualified name of the element.
	 * @param expectedCount The expected number of occurrences.
	 */
	public static void assertDescendantElementCount(Document xmlEntity, QName elementName, int expectedCount) {
		NodeList features = xmlEntity.getElementsByTagNameNS(elementName.getNamespaceURI(), elementName.getLocalPart());
		Assert.assertEquals(features.getLength(), expectedCount,
				String.format("Unexpected number of %s descendant elements.", elementName));
	}

	/**
	 * Asserts that the given response message contains an OGC exception report. The
	 * message body must contain an XML document that has a document element with the
	 * following properties:
	 *
	 * <ul>
	 * <li>[local name] = "ExceptionReport"</li>
	 * <li>[namespace name] = "http://www.opengis.net/ows/2.0"</li>
	 * </ul>
	 * @param rsp A Jakarta Response object representing an HTTP response message.
	 * @param exceptionCode The expected OGC exception code.
	 * @param locator A case-insensitive string value expected to occur in the locator
	 * attribute (e.g. a parameter name); the attribute value will be ignored if the
	 * argument is null or empty.
	 */
	public static void assertExceptionReport(Response rsp, String exceptionCode, String locator) {
		Assert.assertEquals(rsp.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
				ErrorMessage.get(ErrorMessageKeys.UNEXPECTED_STATUS));
		Document doc = rsp.readEntity(Document.class);
		String expr = String.format("//ows:Exception[@exceptionCode = '%s']", exceptionCode);
		NodeList nodeList = null;
		try {
			nodeList = XMLUtils.evaluateXPath(doc, expr, null);
		}
		catch (XPathExpressionException xpe) {
			// won't happen
		}
		Assert.assertTrue(nodeList.getLength() > 0, "Exception not found in response: " + expr);
		if (null != locator && !locator.isEmpty()) {
			Element exception = (Element) nodeList.item(0);
			String locatorValue = exception.getAttribute("locator").toLowerCase();
			Assert.assertTrue(locatorValue.contains(locator.toLowerCase()),
					String.format("Expected locator attribute to contain '%s']", locator));
		}
	}

}
