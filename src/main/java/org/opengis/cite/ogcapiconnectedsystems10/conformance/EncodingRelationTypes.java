package org.opengis.cite.ogcapiconnectedsystems10.conformance;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opengis.cite.ogcapiconnectedsystems10.ETSAssert;
import org.testng.SkipException;

/**
 * Shared assertions for GeoJSON/SensorML {@code relation-types} requirements.
 */
public final class EncodingRelationTypes {

	public static final String ENCODING_GEOJSON = "geojson";

	public static final String ENCODING_SENSORML = "sensorml";

	public static final String RESOURCE_SYSTEM = "system";

	public static final String RESOURCE_DEPLOYMENT = "deployment";

	public static final String RESOURCE_PROCEDURE = "procedure";

	public static final String RESOURCE_SAMPLING_FEATURE = "samplingFeature";

	private static final Set<String> GENERIC_RELATIONS = Set.of("self", "canonical", "alternate", "next", "prev",
			"first", "last", "collection", "service-desc", "service-doc");

	private static final Map<String, Map<String, Set<String>>> ALLOWED_ASSOCIATION_RELS = Map.of(ENCODING_GEOJSON,
			Map.of(RESOURCE_SYSTEM,
					Set.of("parentSystem", "subsystems", "samplingFeatures", "deployments", "procedures", "datastreams",
							"controlstreams"),
					RESOURCE_DEPLOYMENT,
					Set.of("parentDeployment", "subdeployments", "featuresOfInterest", "samplingFeatures",
							"datastreams", "controlstreams"),
					RESOURCE_PROCEDURE, Set.of("implementingSystems"), RESOURCE_SAMPLING_FEATURE,
					Set.of("parentSystem", "sampleOf", "datastreams", "controlstreams")),
			ENCODING_SENSORML,
			Map.of(RESOURCE_SYSTEM,
					Set.of("subsystems", "samplingFeatures", "deployments", "procedures", "datastreams",
							"controlstreams"),
					RESOURCE_DEPLOYMENT, Set.of("parentDeployment", "subdeployments", "featuresOfInterest",
							"samplingFeatures", "datastreams", "controlstreams"),
					RESOURCE_PROCEDURE, Set.of("implementingSystems")));

	private EncodingRelationTypes() {
	}

	/**
	 * Asserts that links-member association rels are valid for the selected encoding and
	 * resource type.
	 * @param representation JSON representation body.
	 * @param encoding one of {@link #ENCODING_GEOJSON} or {@link #ENCODING_SENSORML}.
	 * @param resourceType selected resource type.
	 * @param requirementUri OGC requirement URI for assertion messages.
	 */
	@SuppressWarnings("unchecked")
	public static void assertLinksMemberAssociationRels(Map<String, Object> representation, String encoding,
			String resourceType, String requirementUri) {
		Set<String> allowedRels = allowedAssociationRels(encoding, resourceType);
		Object linksObj = representation == null ? null : representation.get("links");
		if (!(linksObj instanceof List)) {
			throw new SkipException(requirementUri + " - selected " + resourceType
					+ " representation has no JSON links member with associations to inspect.");
		}
		boolean foundAssociationLink = false;
		for (Object linkObj : (List<?>) linksObj) {
			if (!(linkObj instanceof Map)) {
				continue;
			}
			Map<String, Object> link = (Map<String, Object>) linkObj;
			Object relObj = link.get("rel");
			String rel = relObj instanceof String ? (String) relObj : null;
			if (rel == null || rel.isBlank()) {
				ETSAssert.failWithUri(requirementUri, "A links[] entry has no non-empty rel: " + link);
			}
			if (GENERIC_RELATIONS.contains(rel)) {
				continue;
			}
			if (!allowedRels.contains(rel)) {
				ETSAssert.failWithUri(requirementUri, "Link relation '" + rel + "' is not a valid " + encoding + " "
						+ resourceType + " links-member association relation. Link: " + link);
			}
			foundAssociationLink = true;
		}
		if (!foundAssociationLink) {
			throw new SkipException(requirementUri + " - selected " + resourceType
					+ " representation has no links-member association links after generic links were excluded.");
		}
	}

	public static boolean isAllowedAssociationRel(String encoding, String resourceType, String rel) {
		return allowedAssociationRels(encoding, resourceType).contains(rel);
	}

	public static boolean isGenericRel(String rel) {
		return GENERIC_RELATIONS.contains(rel);
	}

	private static Set<String> allowedAssociationRels(String encoding, String resourceType) {
		Map<String, Set<String>> byResource = ALLOWED_ASSOCIATION_RELS.get(encoding);
		if (byResource == null || !byResource.containsKey(resourceType)) {
			throw new IllegalArgumentException("No relation-types allowlist for " + encoding + " " + resourceType);
		}
		return byResource.get(resourceType);
	}

}
