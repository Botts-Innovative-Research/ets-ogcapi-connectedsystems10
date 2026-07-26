package org.opengis.cite.ogcapiconnectedsystems10;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Coverage report builder introduced by S-ETS-45-01.
 */
final class ReleasedAtsCoverage {

	private static final ObjectMapper JSON = new ObjectMapper();

	private ReleasedAtsCoverage() {
	}

	static JsonNode buildReport(JsonNode inventory, JsonNode reviewed, Set<Class<?>> suiteClasses) {
		if (!"1.0".equals(reviewed.path("schemaVersion").asText()) || !reviewed.path("approvedHelpers").isArray()
				|| !reviewed.path("mappings").isArray()) {
			throw new IllegalArgumentException("Reviewed mappings must use schemaVersion 1.0 and array fields");
		}
		Map<String, JsonNode> inventoryByKey = new HashMap<>();
		for (JsonNode test : inventory.path("tests")) {
			String key = key(test.path("part").asInt(), test.path("identifier").asText());
			if (inventoryByKey.put(key, test) != null) {
				throw new IllegalArgumentException("Duplicate inventory test " + key);
			}
		}

		List<TestMethod> testMethods = discoverTestMethods(suiteClasses);
		Set<String> suiteImplementations = testMethods.stream()
			.map(TestMethod::implementation)
			.collect(Collectors.toSet());
		Set<String> approvedHelpers = validateApprovedHelpers(reviewed.path("approvedHelpers"));
		Set<String> claimedHelpers = new HashSet<>();
		Map<String, JsonNode> reviewedByKey = new HashMap<>();
		Map<String, String> implementationClaims = new HashMap<>();
		for (JsonNode mapping : reviewed.path("mappings")) {
			String key = key(mapping.path("part").asInt(), mapping.path("test").asText());
			if (!inventoryByKey.containsKey(key)) {
				throw new IllegalArgumentException("Reviewed mapping references unknown ATS test " + key);
			}
			if (reviewedByKey.put(key, mapping) != null) {
				throw new IllegalArgumentException("Duplicate reviewed mapping " + key);
			}
			String implementation = mapping.path("implementation").asText();
			String previousClaim = implementationClaims.put(implementation, key);
			if (previousClaim != null) {
				throw new IllegalArgumentException(
						implementation + " claims multiple ATS tests: " + previousClaim + " and " + key);
			}
			validateReviewedMapping(inventoryByKey.get(key), mapping, suiteImplementations, approvedHelpers);
			if ("helper".equals(mapping.path("kind").asText())) {
				claimedHelpers.add(implementation);
			}
		}
		if (!claimedHelpers.equals(approvedHelpers)) {
			Set<String> unused = new HashSet<>(approvedHelpers);
			unused.removeAll(claimedHelpers);
			throw new IllegalArgumentException("Approved helper registry contains unused entries " + unused);
		}

		ObjectNode report = JSON.createObjectNode();
		report.put("schemaVersion", "1.0");
		report.put("inventoryCommit", inventory.path("source").path("commit").asText());
		report.put("suiteTestMethodCount", testMethods.size());

		Map<String, Integer> totals = new LinkedHashMap<>();
		totals.put("exact", 0);
		totals.put("helper", 0);
		totals.put("candidate", 0);
		totals.put("unmapped", 0);
		Map<Integer, Map<String, Integer>> partTotals = new LinkedHashMap<>();
		Map<String, Map<String, Integer>> classTotals = new LinkedHashMap<>();
		ArrayNode entries = report.putArray("tests");

		for (JsonNode test : inventory.path("tests")) {
			int part = test.path("part").asInt();
			String identifier = test.path("identifier").asText();
			String target = nullableText(test.get("fullTarget"));
			String inventoryKey = key(part, identifier);
			List<String> candidates = candidateMappings(target, testMethods);
			JsonNode reviewedMapping = reviewedByKey.get(inventoryKey);
			String state;
			if (reviewedMapping != null) {
				state = reviewedMapping.path("kind").asText();
			}
			else if (!candidates.isEmpty()) {
				state = "candidate";
			}
			else {
				state = "unmapped";
			}

			totals.compute(state, (ignored, count) -> count + 1);
			partTotals.computeIfAbsent(part, ignored -> emptyTotals()).compute(state, (ignored, count) -> count + 1);
			String classKey = part + ":" + nullableText(test.get("classIdentifier"));
			classTotals.computeIfAbsent(classKey, ignored -> emptyTotals())
				.compute(state, (ignored, count) -> count + 1);

			ObjectNode entry = entries.addObject();
			entry.put("part", part);
			entry.put("identifier", identifier);
			putNullable(entry, "classIdentifier", nullableText(test.get("classIdentifier")));
			putNullable(entry, "target", nullableText(test.get("target")));
			entry.put("supporting", test.path("supporting").asBoolean());
			entry.put("state", state);
			ArrayNode candidateArray = entry.putArray("candidateMappings");
			candidates.forEach(candidateArray::add);
			if (reviewedMapping == null) {
				entry.putNull("reviewedMapping");
			}
			else {
				entry.set("reviewedMapping", reviewedMapping.deepCopy());
			}
		}

		ObjectNode summary = report.putObject("summary");
		summary.put("total", inventoryByKey.size());
		writeTotals(summary, totals);
		ObjectNode byPart = summary.putObject("byPart");
		partTotals.forEach((part, counts) -> {
			ObjectNode item = byPart.putObject(Integer.toString(part));
			item.put("total", counts.values().stream().mapToInt(Integer::intValue).sum());
			writeTotals(item, counts);
		});
		ObjectNode byClass = summary.putObject("byClass");
		classTotals.forEach((classKey, counts) -> {
			ObjectNode item = byClass.putObject(classKey);
			item.put("total", counts.values().stream().mapToInt(Integer::intValue).sum());
			writeTotals(item, counts);
		});
		return report;
	}

	private static List<TestMethod> discoverTestMethods(Set<Class<?>> suiteClasses) {
		List<TestMethod> methods = new ArrayList<>();
		suiteClasses.stream().sorted(Comparator.comparing(Class::getName)).forEach(type -> {
			Set<String> overridden = new HashSet<>();
			for (Class<?> current = type; current != null
					&& current != Object.class; current = current.getSuperclass()) {
				if (current.getDeclaredAnnotation(org.testng.annotations.Test.class) != null) {
					throw new IllegalArgumentException("Unsupported TestNG class-level @Test on " + current.getName());
				}
				if (current.getDeclaredAnnotation(org.testng.annotations.Ignore.class) != null
						|| current.getPackage().getAnnotation(org.testng.annotations.Ignore.class) != null) {
					throw new IllegalArgumentException(
							"Unsupported TestNG @Ignore on class or package for " + current.getName());
				}
				for (java.lang.reflect.Constructor<?> constructor : current.getDeclaredConstructors()) {
					if (constructor.getAnnotation(org.testng.annotations.Factory.class) != null) {
						throw new IllegalArgumentException(
								"Unsupported TestNG factory constructor " + current.getName());
					}
				}
				for (Method method : current.getDeclaredMethods()) {
					String signature = methodSignature(method);
					if (!overridden.add(signature)) {
						continue;
					}
					if (method.getAnnotation(org.testng.annotations.Factory.class) != null) {
						throw new IllegalArgumentException(
								"Unsupported TestNG factory method " + implementationIdentity(type, method));
					}
					if (method.getAnnotation(org.testng.annotations.Ignore.class) != null) {
						throw new IllegalArgumentException(
								"Unsupported TestNG @Ignore on " + implementationIdentity(type, method));
					}
					org.testng.annotations.Test annotation = method.getAnnotation(org.testng.annotations.Test.class);
					if (annotation == null || !annotation.enabled()) {
						continue;
					}
					if (!annotation.dataProvider().isEmpty() || annotation.invocationCount() != 1) {
						throw new IllegalArgumentException(
								"Unsupported data-driven TestNG method " + implementationIdentity(type, method));
					}
					methods.add(new TestMethod(implementationIdentity(type, method), annotation.description()));
				}
			}
		});
		methods.sort(Comparator.comparing(TestMethod::implementation));
		return methods;
	}

	private static List<String> candidateMappings(String target, List<TestMethod> methods) {
		if (target == null) {
			return List.of();
		}
		return methods.stream()
			.filter(method -> containsCanonicalTarget(method.description(), target))
			.map(TestMethod::implementation)
			.toList();
	}

	private static void validateReviewedMapping(JsonNode test, JsonNode mapping, Set<String> suiteImplementations,
			Set<String> approvedHelpers) {
		validateReviewProvenance(mapping);
		String kind = mapping.path("kind").asText();
		if (!"exact".equals(kind) && !"helper".equals(kind)) {
			throw new IllegalArgumentException("Unknown reviewed mapping kind " + kind);
		}
		boolean supporting = test.path("supporting").asBoolean();
		if (supporting != "helper".equals(kind)) {
			throw new IllegalArgumentException(test.path("identifier").asText()
					+ ": supporting tests require helper mappings; class tests require exact mappings");
		}
		String implementation = mapping.path("implementation").asText();
		Method method = resolveMethod(implementation);
		if ("exact".equals(kind)) {
			org.testng.annotations.Test annotation = method.getAnnotation(org.testng.annotations.Test.class);
			String target = test.path("fullTarget").asText();
			if (annotation == null || !annotation.enabled()
					|| !containsCanonicalTarget(annotation.description(), target)) {
				throw new IllegalArgumentException(implementation + " does not carry canonical target " + target);
			}
			if (!suiteImplementations.contains(implementation)) {
				throw new IllegalArgumentException(implementation + " is not deployed by the TestNG suite");
			}
		}
		else if (!approvedHelpers.contains(implementation)) {
			throw new IllegalArgumentException(implementation + " is not in the approved helper registry");
		}
	}

	private static Set<String> validateApprovedHelpers(JsonNode helpers) {
		Set<String> approved = new HashSet<>();
		for (JsonNode helper : helpers) {
			validateReviewProvenance(helper);
			String implementation = helper.path("implementation").asText();
			if (!approved.add(implementation)) {
				throw new IllegalArgumentException("Duplicate approved helper " + implementation);
			}
			Method method = resolveMethod(implementation);
			if (method.getAnnotation(org.testng.annotations.Test.class) != null
					|| method.getAnnotation(org.testng.annotations.Factory.class) != null) {
				throw new IllegalArgumentException(
						"Approved helper cannot be a TestNG test or factory " + implementation);
			}
		}
		return approved;
	}

	private static void validateReviewProvenance(JsonNode mapping) {
		String reviewedBy = mapping.path("reviewedBy").asText().trim();
		String reviewedOn = mapping.path("reviewedOn").asText().trim();
		String evidence = mapping.path("evidence").asText().trim();
		if (reviewedBy.isEmpty() || reviewedOn.isEmpty() || evidence.isEmpty()) {
			throw new IllegalArgumentException("Reviewed mapping is missing review provenance");
		}
		try {
			LocalDate.parse(reviewedOn);
		}
		catch (DateTimeParseException error) {
			throw new IllegalArgumentException("Reviewed mapping has invalid reviewedOn date " + reviewedOn, error);
		}
	}

	private static boolean containsCanonicalTarget(String description, String target) {
		int start = description.indexOf(target);
		while (start >= 0) {
			int end = start + target.length();
			boolean leftBoundary = start == 0 || isTargetDelimiter(description.charAt(start - 1));
			boolean rightBoundary = end == description.length() || isTargetDelimiter(description.charAt(end));
			if (leftBoundary && rightBoundary) {
				return true;
			}
			start = description.indexOf(target, start + 1);
		}
		return false;
	}

	private static boolean isTargetDelimiter(char character) {
		return Character.isWhitespace(character) || character == '(' || character == ')' || character == ','
				|| character == ';' || character == ':';
	}

	private static Method resolveMethod(String implementation) {
		int separator = implementation.lastIndexOf('#');
		if (separator <= 0 || !implementation.endsWith(")") || implementation.indexOf('(', separator) < 0) {
			throw new IllegalArgumentException(
					"Implementation mapping must use fully.qualified.Class#method(parameter.Types): " + implementation);
		}
		String className = implementation.substring(0, separator);
		try {
			Class<?> type = Class.forName(className);
			Method match = null;
			Set<String> overridden = new HashSet<>();
			for (Class<?> current = type; current != null
					&& current != Object.class; current = current.getSuperclass()) {
				for (Method method : current.getDeclaredMethods()) {
					if (!overridden.add(methodSignature(method))) {
						continue;
					}
					if (implementationIdentity(type, method).equals(implementation)) {
						if (match != null) {
							throw new IllegalArgumentException("Ambiguous implementation mapping " + implementation);
						}
						match = method;
					}
				}
			}
			if (match == null) {
				throw new IllegalArgumentException("Unknown implementation mapping " + implementation);
			}
			return match;
		}
		catch (ClassNotFoundException error) {
			throw new IllegalArgumentException("Unknown implementation mapping " + implementation, error);
		}
	}

	private static String implementationIdentity(Class<?> suiteType, Method method) {
		return suiteType.getName() + "#" + methodSignature(method);
	}

	private static String methodSignature(Method method) {
		return method.getName() + "("
				+ Arrays.stream(method.getParameterTypes()).map(Class::getTypeName).collect(Collectors.joining(","))
				+ ")";
	}

	private static Map<String, Integer> emptyTotals() {
		Map<String, Integer> totals = new LinkedHashMap<>();
		totals.put("exact", 0);
		totals.put("helper", 0);
		totals.put("candidate", 0);
		totals.put("unmapped", 0);
		return totals;
	}

	private static void writeTotals(ObjectNode node, Map<String, Integer> totals) {
		totals.forEach(node::put);
	}

	private static String key(int part, String identifier) {
		return part + ":" + identifier;
	}

	private static String nullableText(JsonNode node) {
		return node == null || node.isNull() ? null : node.asText();
	}

	private static void putNullable(ObjectNode node, String name, String value) {
		if (value == null) {
			node.putNull(name);
		}
		else {
			node.put(name, value);
		}
	}

	private record TestMethod(String implementation, String description) {
	}

}
