package org.opengis.cite.ogcapiconnectedsystems10.conformance.advancedfiltering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.Test;
import org.testng.SkipException;

/**
 * Controlled HTTP coverage for all 25 released Advanced Filtering procedures.
 */
public class VerifyAdvancedFilteringHttpProcedures {

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-DIRECT-HTTP-COVERAGE-001.
	 */
	@Test
	public void allTwentyFiveReleasedProceduresExecuteSuccessfulReadOnlyPaths() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.VALID)) {
			server.start();
			AdvancedFilteringTests tests = configured(server);

			tests.idListSchemaIsValid();
			tests.canonicalResourcesFilterById();
			tests.canonicalResourcesFilterByKeyword();
			tests.canonicalResourcesFilterByProperty();
			tests.featuresFilterByGeometry();
			tests.systemsFilterByParent();
			tests.systemsFilterByProcedure();
			tests.systemsFilterByFeatureOfInterest();
			tests.systemsFilterByObservedProperty();
			tests.systemsFilterByControlledProperty();
			tests.deploymentsFilterByParent();
			tests.deploymentsFilterBySystem();
			tests.deploymentsFilterByFeatureOfInterest();
			tests.deploymentsFilterByObservedProperty();
			tests.deploymentsFilterByControlledProperty();
			tests.proceduresFilterByObservedProperty();
			tests.proceduresFilterByControlledProperty();
			tests.samplingFeaturesFilterByFeatureOfInterest();
			tests.samplingFeaturesFilterByObservedProperty();
			tests.samplingFeaturesFilterByControlledProperty();
			tests.propertiesFilterByBaseProperty();
			tests.propertiesFilterByObjectType();
			tests.canonicalResourcesCombineFilters();
			tests.indirectPropertyFiltersAreTransitive();
			tests.indirectFeatureOfInterestFiltersAreTransitive();

			assertEquals(0, server.nonGetCalls());
			assertTrue(server.calls("/api/conformance") >= 25);
			assertTrue(server.calls("/api/systems") > 0);
			assertTrue(server.calls("/api/deployments") > 0);
			assertTrue(server.calls("/api/procedures") > 0);
			assertTrue(server.calls("/api/samplingFeatures") > 0);
			assertTrue(server.calls("/api/properties") > 0);
			for (String endpoint : new String[] { "/api/systems", "/api/deployments", "/api/procedures",
					"/api/samplingFeatures", "/api/properties" }) {
				assertTrue(endpoint + " did not receive a UID-prefix query",
						server.callsWithValue(endpoint, "id", value -> value.endsWith("*")) > 0);
			}
			assertTrue(server.callsWithKeys("/api/systems", "q", "featureType") > 0);
			assertTrue(server.callsWithKeys("/api/systems", "id", "customCode") > 0);
			assertTrue(server.callsWithKeys("/api/deployments", "datetime", "system") > 0);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-E2E-EXECUTION-001.
	 */
	@Test
	public void undeclaredClassSkipsBeforeCanonicalResourceAccess() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNDECLARED)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::canonicalResourcesFilterById);
			assertEquals(0, server.calls("/api/systems"));
			assertEquals(0, server.nonGetCalls());
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-COMMON-FILTERS-001.
	 */
	@Test
	public void emptyKnownMatchIdResultFailsClosed() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.EMPTY_ID_RESULT)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::canonicalResourcesFilterById);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-MEDIA-PAGINATION-001.
	 */
	@Test
	public void nonmatchingResourceOnLaterPageCannotBeHidden() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.LATER_WRONG_ID)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::canonicalResourcesFilterById);
			assertTrue(server.calls("/api/systems?page=2") > 0);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-UID-PREFIX-001.
	 */
	@Test
	public void nonmatchingUidPrefixOnLaterPageCannotBeHidden() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.LATER_WRONG_UID_PREFIX)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::canonicalResourcesFilterById);
			assertTrue(server.calls("/api/systems?page=uid-prefix-2") > 0);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-COMBINED-FILTERS-001.
	 */
	@Test
	public void unionBehaviorCannotPassTheCombinedFilterProcedure() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNION_COMBINED)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::canonicalResourcesCombineFilters);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-COMBINED-FILTERS-001.
	 */
	@Test
	public void aSecondAvailableCombinationCannotBeIgnored() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNION_SECOND_COMBINATION)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::canonicalResourcesCombineFilters);
			assertTrue(server.callsWithKeys("/api/systems", "q", "geom") > 0);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-COMBINED-FILTERS-001.
	 */
	@Test
	public void inheritedFeatureTypeKeywordCombinationCannotBeIgnored() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNION_FEATURE_TYPE_KEYWORD)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::canonicalResourcesCombineFilters);
			assertTrue(server.callsWithKeys("/api/systems", "featureType", "q") > 0);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-COMBINED-FILTERS-001.
	 */
	@Test
	public void inheritedDatetimeAssociationCombinationCannotBeIgnored() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNION_DATETIME_SYSTEM)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::canonicalResourcesCombineFilters);
			assertTrue(server.callsWithKeys("/api/deployments", "datetime", "system") > 0);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-COMBINED-FILTERS-001.
	 */
	@Test
	public void supportedCustomPropertyCombinationCannotBeIgnored() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNION_CUSTOM_PROPERTY)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::canonicalResourcesCombineFilters);
			assertTrue(server.callsWithKeys("/api/systems", "id", "customCode") > 0);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-COMBINED-FILTERS-001.
	 */
	@Test
	public void everySupportedCustomPropertyCombinationIsEnumerated() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNION_ADDITIONAL_CUSTOM_PROPERTY)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::canonicalResourcesCombineFilters);
			assertTrue(server.callsWithKeys("/api/systems", "id", "secondaryCode") > 0);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-COMBINED-FILTERS-001.
	 */
	@Test
	public void predicatesFromLaterResourcesAreEnumerated() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNION_LATER_RESOURCE_PREDICATE)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::canonicalResourcesCombineFilters);
			assertTrue(server.callsWithKeys("/api/systems", "id", "laterCode") > 0);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-GEOMETRY-001.
	 */
	@Test
	public void nonintersectingGeometryCannotPass() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.WRONG_GEOMETRY)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::featuresFilterByGeometry);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-MEDIA-PAGINATION-001.
	 */
	@Test
	public void crossOriginAssociationDoesNotReceiveIutCredential() throws Exception {
		try (ExternalServer external = new ExternalServer();
				FixtureServer server = new FixtureServer(Mode.CROSS_ORIGIN_ASSOCIATION)) {
			external.start();
			server.setExternalTarget(external.target());
			server.start();
			RequestSpecification original = RestAssured.requestSpecification;
			RestAssured.requestSpecification = new RequestSpecBuilder()
				.addHeader("Authorization", "Bearer synthetic-secret")
				.build();
			try {
				assertThrows(SkipException.class, configured(server)::systemsFilterByParent);
			}
			finally {
				RestAssured.requestSpecification = original;
			}

			assertNull(external.authorization());
			assertEquals(0, external.calls());
			assertEquals(0, server.nonGetCalls());
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-PROCEDURE-ISOLATION-001.
	 */
	@Test
	public void missingAssociationEvidenceDoesNotBlockIndependentIdProcedure() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.NO_ASSOCIATIONS)) {
			server.start();
			AdvancedFilteringTests tests = configured(server);

			assertThrows(SkipException.class, tests::systemsFilterByParent);
			tests.canonicalResourcesFilterById();
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PROVENANCE-001.
	 */
	@Test
	public void resolvedAssociationUsesRepresentationIdentifiersNotHrefTokens() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.RESOLVED_TARGET_IDENTIFIERS)) {
			server.start();

			configured(server).systemsFilterByParent();

			assertTrue(server.callsWithValue("/api/systems", "parent", "parent-real"::equals) > 0);
			assertTrue(server.callsWithValue("/api/systems", "parent", "urn:example:system:parent-real"::equals) > 0);
			assertEquals(0, server.callsWithValue("/api/systems", "parent", "alias-parent"::equals));
			assertEquals(0, server.callsWithValue("/api/systems", "parent", "wrapper-parent"::equals));
			assertEquals(0,
					server.callsWithValue("/api/systems", "parent", "urn:example:system:wrapper-parent"::equals));
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-SYSTEM-ASSOCIATIONS-001;
	 * SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PROVENANCE-001.
	 */
	@Test
	public void systemFoiUsesSampleOfTargetsNotSamplingFeatureIdentifiers() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.CONTRADICTORY_SYSTEM_FOI_WRAPPER)) {
			server.start();

			configured(server).systemsFilterByFeatureOfInterest();

			assertTrue(server.callsWithValue("/api/systems", "foi", "foi-real"::equals) > 0);
			assertTrue(server.callsWithValue("/api/systems", "foi", "urn:example:foi:real"::equals) > 0);
			assertEquals(0, server.callsWithValue("/api/systems", "foi", "sf-wrapper"::equals));
			assertEquals(0, server.callsWithValue("/api/systems", "foi", "urn:example:sf:wrapper"::equals));
			assertEquals(0, server.callsWithValue("/api/systems", "foi", "foi-wrapper"::equals));
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-DEPLOYMENT-ASSOCIATIONS-001;
	 * SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PROVENANCE-001.
	 */
	@Test
	public void deploymentSystemUsesResolvedTargetsNotAssociationWrapperIdentifiers() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.CONTRADICTORY_DEPLOYED_SYSTEM_WRAPPER)) {
			server.start();

			configured(server).deploymentsFilterBySystem();

			assertTrue(server.callsWithValue("/api/deployments", "system", "system-real"::equals) > 0);
			assertTrue(server.callsWithValue("/api/deployments", "system", "urn:example:system:real"::equals) > 0);
			assertEquals(0, server.callsWithValue("/api/deployments", "system", "deployment-wrapper"::equals));
			assertEquals(0, server.callsWithValue("/api/deployments", "system", "system-wrapper"::equals));
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PATHS-001;
	 * SCENARIO-ETS-PART1-009-RELEASED-DEPLOYMENT-ASSOCIATIONS-001.
	 */
	@Test
	public void deploymentPropertiesIgnoreAssociationWrapperAliases() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.DEPLOYED_PROPERTY_WRAPPER_SHORTCUT)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::deploymentsFilterByObservedProperty);
			assertEquals(0, server.callsWithValue("/api/deployments", "observedProperty", "property-wrapper"::equals));
			assertEquals(0, server.callsWithValue("/api/deployments", "observedProperty",
					"urn:example:property:wrapper"::equals));
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PATHS-001;
	 * SCENARIO-ETS-PART1-009-RELEASED-DEPLOYMENT-ASSOCIATIONS-001.
	 */
	@Test
	public void deploymentPropertiesIgnoreUnrelatedNestedSystemHrefs() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.DEPLOYED_PROPERTY_NESTED_HREF_SHORTCUT)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::deploymentsFilterByObservedProperty);
			assertTrue(server.calls("/api/systems/deployed-clean") > 0);
			assertEquals(0, server.calls("/api/systems/deployed-bogus"));
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PROVENANCE-001.
	 */
	@Test
	public void malformedAssociationHrefCannotBecomeSyntheticIdentity() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.MALFORMED_ASSOCIATION_HREF)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::systemsFilterByObservedProperty);

			assertEquals(0, server.callsWithValue("/api/systems", "observedProperty", "property-local"::equals));
			assertEquals(0, server.callsWithValue("/api/systems", "observedProperty",
					value -> value.startsWith("urn:invalid-reference:")));
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PATHS-001.
	 */
	@Test
	public void directRelationsRejectUnrelatedSuffixAliases() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNRELATED_SUFFIX_ALIAS)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::systemsFilterByParent);
			assertEquals(0, server.callsWithValue("/api/systems", "parent", "parent-shortcut"::equals));
			assertEquals(0,
					server.callsWithValue("/api/systems", "parent", "urn:example:system:parent-shortcut"::equals));
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PATHS-001.
	 */
	@Test
	public void directRelationsRejectNestedExtensionAliases() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.NESTED_EXTENSION_ALIAS)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::systemsFilterByParent);
			assertEquals(0, server.callsWithValue("/api/systems", "parent", "parent-shortcut"::equals));
			assertEquals(0,
					server.callsWithValue("/api/systems", "parent", "urn:example:system:parent-shortcut"::equals));
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PATHS-001;
	 * SCENARIO-ETS-PART1-009-RELEASED-DEPLOYMENT-ASSOCIATIONS-001.
	 */
	@Test
	public void deploymentPropertiesRejectNonSystemTarget() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.DEPLOYED_PROPERTY_NON_SYSTEM_TARGET)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::deploymentsFilterByObservedProperty);
			assertTrue(server.calls("/api/systems/deployed-not-system") > 0);
			assertEquals(0, server.callsWithValue("/api/deployments", "observedProperty", "property-target"::equals));
			assertEquals(0, server.callsWithValue("/api/deployments", "observedProperty",
					"urn:example:property:target"::equals));
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PATHS-001;
	 * SCENARIO-ETS-PART1-009-RELEASED-DEPLOYMENT-ASSOCIATIONS-001.
	 */
	@Test
	public void deploymentPropertiesRejectSystemCollectionTarget() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.DEPLOYED_PROPERTY_COLLECTION_TARGET)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::deploymentsFilterByObservedProperty);
			assertTrue(server.calls("/api/systems/deployed-collection") > 0);
			assertEquals(0, server.callsWithValue("/api/deployments", "observedProperty", "property-target"::equals));
			assertEquals(0, server.callsWithValue("/api/deployments", "observedProperty",
					"urn:example:property:target"::equals));
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PATHS-001.
	 */
	@Test
	public void releasedOgcRelCompactRelationIsAccepted() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.CANONICAL_OGC_RELATION)) {
			server.start();
			AdvancedFilteringTests tests = configured(server);

			tests.systemsFilterByParent();
			tests.deploymentsFilterByParent();
			tests.samplingFeaturesFilterByFeatureOfInterest();

			assertTrue(server.callsWithValue("/api/systems", "parent", "parent-1"::equals) > 0);
			assertTrue(server.callsWithValue("/api/systems", "parent", "urn:example:system:parent"::equals) > 0);
			assertTrue(server.callsWithValue("/api/deployments", "parent", "deployment-parent"::equals) > 0);
			assertTrue(server.callsWithValue("/api/samplingFeatures", "foi", "foi-1"::equals) > 0);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PATHS-001.
	 */
	@Test
	public void unrelatedCompactRelationSchemeIsRejected() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNRELATED_RELATION_SCHEME)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::systemsFilterByParent);
			assertEquals(0, server.callsWithValue("/api/systems", "parent", "parent-1"::equals));
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PATHS-001.
	 */
	@Test
	public void trailingLinkSuffixCannotCreateFieldAssociationEvidence() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.FIELD_LINK_SUFFIX_ALIASES)) {
			server.start();
			AdvancedFilteringTests tests = configured(server);

			assertThrows(SkipException.class, tests::systemsFilterByParent);
			assertThrows(SkipException.class, tests::systemsFilterByProcedure);
			assertEquals(0, server.callsWithValue("/api/systems", "parent", "parent-1"::equals));
			assertEquals(0, server.callsWithValue("/api/systems", "procedure", "procedure-1"::equals));
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PATHS-001.
	 */
	@Test
	public void ogcRelCasePunctuationAndSuffixNearMissesAreRejected() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.OGC_REL_NEAR_MISSES)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::systemsFilterByParent);
			assertEquals(0, server.callsWithValue("/api/systems", "parent", "parent-1"::equals));
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PATHS-001.
	 */
	@Test
	public void broadParentAndProcedureAliasesAreRejected() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.BROAD_RELATION_ALIASES)) {
			server.start();
			AdvancedFilteringTests tests = configured(server);

			assertThrows(SkipException.class, tests::systemsFilterByParent);
			assertThrows(SkipException.class, tests::systemsFilterByProcedure);
			assertEquals(0, server.callsWithValue("/api/systems", "parent", "parent-1"::equals));
			assertEquals(0, server.callsWithValue("/api/systems", "procedure", "procedure-1"::equals));
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PATHS-001.
	 */
	@Test
	public void releasedGeoJsonSystemKindLinkIsAccepted() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.GEOJSON_SYSTEM_KIND_LINK)) {
			server.start();

			configured(server).systemsFilterByProcedure();

			assertTrue(server.callsWithValue("/api/systems", "procedure", "procedure-1"::equals) > 0);
			assertTrue(server.callsWithValue("/api/systems", "procedure", "urn:example:procedure:1"::equals) > 0);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PATHS-001.
	 */
	@Test
	public void releasedSensorMlAssociationFieldsAreAccepted() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.SENSORML_SYSTEM_ASSOCIATIONS)) {
			server.start();
			AdvancedFilteringTests tests = configured(server);

			tests.systemsFilterByParent();
			tests.systemsFilterByProcedure();

			assertTrue(server.callsWithValue("/api/systems", "parent", "parent-1"::equals) > 0);
			assertTrue(server.callsWithValue("/api/systems", "procedure", "procedure-1"::equals) > 0);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PATHS-001.
	 */
	@Test
	public void everyReleasedSystemTargetTypeContributesPropertyEvidence() throws Exception {
		String[] targetTypes = { "sosa:System", "sosa:Sensor", "sosa:Actuator", "sosa:Sampler", "sosa:Platform",
				"http://www.w3.org/ns/sosa/System", "http://www.w3.org/ns/sosa/Sensor",
				"http://www.w3.org/ns/sosa/Actuator", "http://www.w3.org/ns/sosa/Sampler",
				"http://www.w3.org/ns/sosa/Platform", "PhysicalComponent", "PhysicalSystem", "SimpleProcess",
				"AggregateProcess" };
		for (String targetType : targetTypes) {
			try (FixtureServer server = new FixtureServer(Mode.DEPLOYED_PROPERTY_TYPED_SYSTEM_TARGET, targetType)) {
				server.start();

				configured(server).deploymentsFilterByObservedProperty();

				assertTrue(targetType,
						server.callsWithValue("/api/deployments", "observedProperty", "property-target"::equals) > 0);
			}
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PATHS-001.
	 */
	@Test
	public void arbitrarySystemSuffixTargetIsRejected() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.DEPLOYED_PROPERTY_SUFFIX_SYSTEM_TARGET)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::deploymentsFilterByObservedProperty);
			assertEquals(0, server.callsWithValue("/api/deployments", "observedProperty", "property-target"::equals));
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-PROPERTY-FILTERS-001.
	 */
	@Test
	public void propertyObjectTypeRejectsUnsupportedRepresentation() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNSUPPORTED_PROPERTY_FILTER_MEDIA)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::propertiesFilterByObjectType);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-COMBINED-FILTERS-001.
	 */
	@Test
	public void combinedFiltersRejectUnsupportedRepresentation() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.UNSUPPORTED_COMBINED_FILTER_MEDIA)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::canonicalResourcesCombineFilters);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PATHS-001.
	 */
	@Test
	public void rootAliasesCannotReplacePrescribedAssociationSubresources() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.ROOT_ASSOCIATION_SHORTCUTS)) {
			server.start();
			AdvancedFilteringTests tests = configured(server);

			assertThrows(SkipException.class, tests::deploymentsFilterBySystem);
			assertThrows(SkipException.class, tests::deploymentsFilterByFeatureOfInterest);
			assertThrows(SkipException.class, tests::deploymentsFilterByObservedProperty);
			assertThrows(SkipException.class, tests::deploymentsFilterByControlledProperty);
			assertThrows(SkipException.class, tests::samplingFeaturesFilterByObservedProperty);
			assertThrows(SkipException.class, tests::samplingFeaturesFilterByControlledProperty);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PROVENANCE-001.
	 */
	@Test
	public void brokenAssociationCannotFallBackToWrapperIdentifiers() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.BROKEN_ASSOCIATION)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::systemsFilterByParent);
			assertEquals(0, server.callsWithValue("/api/systems", "parent", "wrapper-parent"::equals));
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-MEDIA-PAGINATION-001.
	 */
	@Test
	public void unsupportedAssociationMediaCannotBeParsedAsIdentityEvidence() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.WRONG_ASSOCIATION_MEDIA)) {
			server.start();

			assertThrows(SkipException.class, configured(server)::systemsFilterByParent);
			assertTrue(server.calls("/api/systems/wrong-media-parent") > 0);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-MEDIA-PAGINATION-001.
	 */
	@Test
	public void associationCollectionsTraverseEveryPage() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.PAGINATED_ASSOCIATION)) {
			server.start();

			configured(server).systemsFilterByParent();

			assertTrue(server.calls("/api/systems/parent-collection?page=2") > 0);
			assertTrue(server.callsWithValue("/api/systems", "parent", "parent-real"::equals) > 0);
			assertTrue(server.callsWithValue("/api/systems", "parent", "urn:example:system:parent-real"::equals) > 0);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-INDIRECT-RECOMMENDATIONS-001.
	 */
	@Test
	public void indirectRecommendationsInspectEveryEligiblePagedResource() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.LATER_INDIRECT_RESOURCES)) {
			server.start();
			AdvancedFilteringTests tests = configured(server);

			tests.indirectPropertyFiltersAreTransitive();
			tests.indirectFeatureOfInterestFiltersAreTransitive();

			assertTrue(server.callsWithValue("/api/systems", "observedProperty", "property-2"::equals) > 0);
			assertTrue(server.callsWithValue("/api/systems", "foi", "sf-2"::equals) > 0);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-MEDIA-PAGINATION-001.
	 */
	@Test
	public void overDepthAssociationGraphFailsExplicitly() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.OVER_DEPTH_RELATION)) {
			server.start();

			AssertionError error = assertThrows(AssertionError.class, configured(server)::systemsFilterByParent);
			assertTrue(error.getMessage().contains("traversal depth"));
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-MEDIA-PAGINATION-001.
	 */
	@Test
	public void cyclicAssociationGraphFailsExplicitly() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.CYCLIC_RELATION)) {
			server.start();

			AssertionError error = assertThrows(AssertionError.class,
					configured(server)::samplingFeaturesFilterByFeatureOfInterest);
			assertTrue(error.getMessage().contains("cycle"));
			assertTrue(server.calls("/api/features/cycle-a") > 0);
			assertTrue(server.calls("/api/features/cycle-b") > 0);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-MEDIA-PAGINATION-001.
	 */
	@Test
	public void overLimitAssociationGraphFailsExplicitly() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.OVER_LIMIT_RELATION)) {
			server.start();

			AssertionError error = assertThrows(AssertionError.class,
					configured(server)::samplingFeaturesFilterByFeatureOfInterest);
			assertTrue(error.getMessage().contains("reference-read limit"));
			assertTrue(server.calls("/api/features/chain-63") > 0);
			assertEquals(0, server.calls("/api/features/chain-64"));
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-OWNER-APPLICABILITY-001.
	 */
	@Test
	public void declaredCanonicalEndpointCannotBeSilentlyIgnoredWhenUnavailable() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.DECLARED_SYSTEM_404)) {
			server.start();

			assertThrows(AssertionError.class, configured(server)::canonicalResourcesFilterById);
			assertTrue(server.calls("/api/systems") > 0);
		}
	}

	/**
	 * REQ-ETS-PART1-009; SCENARIO-ETS-PART1-009-RELEASED-OWNER-APPLICABILITY-001.
	 */
	@Test
	public void reachableUndeclaredCanonicalEndpointsAreNotApplicable() throws Exception {
		try (FixtureServer server = new FixtureServer(Mode.ONLY_SYSTEM_DECLARED)) {
			server.start();

			configured(server).canonicalResourcesFilterById();

			assertTrue(server.calls("/api/systems") > 0);
			assertEquals(0, server.calls("/api/deployments"));
			assertEquals(0, server.calls("/api/procedures"));
			assertEquals(0, server.calls("/api/samplingFeatures"));
			assertEquals(0, server.calls("/api/properties"));
		}
	}

	private static AdvancedFilteringTests configured(FixtureServer server) {
		AdvancedFilteringTests tests = new AdvancedFilteringTests();
		tests.configure(server.apiRoot());
		return tests;
	}

	private enum Mode {

		VALID, UNDECLARED, EMPTY_ID_RESULT, LATER_WRONG_ID, LATER_WRONG_UID_PREFIX, UNION_COMBINED,
		UNION_SECOND_COMBINATION, UNION_FEATURE_TYPE_KEYWORD, UNION_DATETIME_SYSTEM, UNION_CUSTOM_PROPERTY,
		UNION_ADDITIONAL_CUSTOM_PROPERTY, UNION_LATER_RESOURCE_PREDICATE, WRONG_GEOMETRY, CROSS_ORIGIN_ASSOCIATION,
		NO_ASSOCIATIONS, RESOLVED_TARGET_IDENTIFIERS, CONTRADICTORY_SYSTEM_FOI_WRAPPER,
		CONTRADICTORY_DEPLOYED_SYSTEM_WRAPPER, DEPLOYED_PROPERTY_WRAPPER_SHORTCUT,
		DEPLOYED_PROPERTY_NESTED_HREF_SHORTCUT, DEPLOYED_PROPERTY_NON_SYSTEM_TARGET,
		DEPLOYED_PROPERTY_COLLECTION_TARGET, MALFORMED_ASSOCIATION_HREF, UNRELATED_SUFFIX_ALIAS, NESTED_EXTENSION_ALIAS,
		CANONICAL_OGC_RELATION, GEOJSON_SYSTEM_KIND_LINK, SENSORML_SYSTEM_ASSOCIATIONS, UNRELATED_RELATION_SCHEME,
		FIELD_LINK_SUFFIX_ALIASES, OGC_REL_NEAR_MISSES, BROAD_RELATION_ALIASES, DEPLOYED_PROPERTY_TYPED_SYSTEM_TARGET,
		DEPLOYED_PROPERTY_SUFFIX_SYSTEM_TARGET, UNSUPPORTED_PROPERTY_FILTER_MEDIA, UNSUPPORTED_COMBINED_FILTER_MEDIA,
		ROOT_ASSOCIATION_SHORTCUTS, BROKEN_ASSOCIATION, WRONG_ASSOCIATION_MEDIA, PAGINATED_ASSOCIATION,
		LATER_INDIRECT_RESOURCES, OVER_DEPTH_RELATION, CYCLIC_RELATION, OVER_LIMIT_RELATION, DECLARED_SYSTEM_404,
		ONLY_SYSTEM_DECLARED

	}

	private static final class FixtureServer implements AutoCloseable {

		private final HttpServer server;

		private final Mode mode;

		private final String deployedSystemType;

		private final Map<String, AtomicInteger> calls = new ConcurrentHashMap<>();

		private final AtomicInteger nonGetCalls = new AtomicInteger();

		private URI externalTarget;

		private FixtureServer(Mode mode) throws IOException {
			this(mode, null);
		}

		private FixtureServer(Mode mode, String deployedSystemType) throws IOException {
			this.mode = mode;
			this.deployedSystemType = deployedSystemType;
			this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
			this.server.createContext("/api/", this::handle);
		}

		private void start() {
			this.server.start();
		}

		private void setExternalTarget(URI externalTarget) {
			this.externalTarget = externalTarget;
		}

		private URI apiRoot() {
			return URI.create("http://127.0.0.1:" + this.server.getAddress().getPort() + "/api/");
		}

		private int calls(String requestTarget) {
			AtomicInteger count = this.calls.get(requestTarget);
			return count == null ? 0 : count.get();
		}

		private int callsWithValue(String path, String key, java.util.function.Predicate<String> predicate) {
			return this.calls.entrySet()
				.stream()
				.filter(entry -> entry.getKey().startsWith(path + "?"))
				.filter(entry -> {
					String value = queryValue(entry.getKey().substring(entry.getKey().indexOf('?') + 1), key);
					return value != null && predicate.test(value);
				})
				.mapToInt(entry -> entry.getValue().get())
				.sum();
		}

		private int callsWithKeys(String path, String... keys) {
			return this.calls.entrySet()
				.stream()
				.filter(entry -> entry.getKey().startsWith(path + "?"))
				.filter(entry -> {
					String query = entry.getKey().substring(entry.getKey().indexOf('?') + 1);
					for (String key : keys) {
						if (queryValue(query, key) == null) {
							return false;
						}
					}
					return true;
				})
				.mapToInt(entry -> entry.getValue().get())
				.sum();
		}

		private int nonGetCalls() {
			return this.nonGetCalls.get();
		}

		private void handle(HttpExchange exchange) throws IOException {
			if (!"GET".equals(exchange.getRequestMethod())) {
				this.nonGetCalls.incrementAndGet();
			}
			String path = exchange.getRequestURI().getPath();
			this.calls.computeIfAbsent(path, ignored -> new AtomicInteger()).incrementAndGet();
			String query = exchange.getRequestURI().getRawQuery();
			if (query != null) {
				this.calls.computeIfAbsent(path + "?" + query, ignored -> new AtomicInteger()).incrementAndGet();
			}
			switch (path) {
				case "/api/conformance" -> conformance(exchange);
				case "/api/systems" -> canonical(exchange, "systems", query);
				case "/api/deployments" -> canonical(exchange, "deployments", query);
				case "/api/procedures" -> canonical(exchange, "procedures", query);
				case "/api/samplingFeatures" -> canonical(exchange, "samplingFeatures", query);
				case "/api/properties" -> canonical(exchange, "properties", query);
				case "/api/systems/system-1/subsystems" -> canonical(exchange, "systems", query);
				case "/api/systems/system-1/samplingFeatures" -> systemSamplingFeatures(exchange, query);
				case "/api/deployments/deployment-1/deployedSystems" ->
					prescribedSubresource(exchange, "systems", query);
				case "/api/deployments/deployment-1/featuresOfInterest" ->
					prescribedSubresource(exchange, "samplingFeatures", query);
				case "/api/samplingFeatures/sf-1/datastreams" -> prescribedStream(exchange, true);
				case "/api/samplingFeatures/sf-1/controlstreams" -> prescribedStream(exchange, false);
				case "/api/systems/parent-1" -> single(exchange, "systems", parentSystem());
				case "/api/systems/alias-parent" -> single(exchange, "systems", resolvedParentSystem());
				case "/api/systems/broken-parent" -> send(exchange, 404, "application/json", "{}");
				case "/api/systems/wrong-media-parent" -> send(exchange, 200, "text/plain", resolvedParentSystem());
				case "/api/systems/parent-collection" -> associationCollection(exchange, query);
				case "/api/deployments/deployment-parent" -> single(exchange, "deployments", parentDeployment());
				case "/api/systems/deployed-real" -> single(exchange, "systems", deployedSystemTarget());
				case "/api/systems/deployed-clean" -> single(exchange, "systems", deployedSystemWithoutProperties());
				case "/api/systems/deployed-bogus" -> single(exchange, "systems", deployedSystemWithBogusProperties());
				case "/api/systems/deployed-not-system" -> single(exchange, "systems", nonSystemWithTargetProperties());
				case "/api/systems/deployed-collection" ->
					sendCollection(exchange, "systems", "[" + deployedSystemWithTargetProperties() + "]", null);
				case "/api/systems/deployed-typed" -> typedDeployedSystem(exchange);
				case "/api/systems/deployed-suffix" ->
					send(exchange, 200, "application/geo+json", suffixSystemWithTargetProperties());
				case "/api/procedures/procedure-1" -> single(exchange, "procedures", procedure());
				case "/api/features/foi-1" -> single(exchange, "samplingFeatures", featureOfInterest());
				case "/api/features/ultimate-required" ->
					single(exchange, "samplingFeatures", requiredUltimateFeature());
				case "/api/features/foi-real" -> single(exchange, "samplingFeatures", resolvedFeatureOfInterest());
				case "/api/properties/base-1" -> single(exchange, "properties", baseProperty("base-1"));
				case "/api/properties/base-2" -> single(exchange, "properties", baseProperty("base-2"));
				default -> {
					if (path.startsWith("/api/features/cycle-")) {
						relationTarget(exchange, path, true);
					}
					else if (path.startsWith("/api/features/chain-")) {
						relationTarget(exchange, path, false);
					}
					else {
						send(exchange, 404, "application/json", "{}");
					}
				}
			}
		}

		private void conformance(HttpExchange exchange) throws IOException {
			String declaration = this.mode == Mode.UNDECLARED ? ""
					: ",\"" + AdvancedFilteringSupport.CONF_ADVANCED_FILTERING + "\"";
			String ownerDeclarations;
			if (this.mode == Mode.ONLY_SYSTEM_DECLARED || this.mode == Mode.DECLARED_SYSTEM_404
					|| this.mode == Mode.UNDECLARED) {
				ownerDeclarations = ",\"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/system\"";
			}
			else {
				ownerDeclarations = """
						,"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/system"
						,"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/deployment"
						,"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/procedure"
						,"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/sf"
						,"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/property"
						""".replaceAll("\\s+", "");
			}
			send(exchange, 200, "application/json",
					"{\"conformsTo\":[\"http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/api-common\""
							+ declaration + ownerDeclarations + "]}");
		}

		private void canonical(HttpExchange exchange, String type, String query) throws IOException {
			String id = queryValue(query, "id");
			if (this.mode == Mode.DECLARED_SYSTEM_404 && "systems".equals(type)) {
				send(exchange, 404, "application/json", "{}");
				return;
			}
			if (this.mode == Mode.SENSORML_SYSTEM_ASSOCIATIONS && "systems".equals(type)) {
				send(exchange, 200, "application/sml+json", "{\"items\":[" + sensorMlSystem() + "]}");
				return;
			}
			if (this.mode == Mode.UNSUPPORTED_PROPERTY_FILTER_MEDIA && "properties".equals(type)) {
				sendGenericCollection(exchange, "[" + property() + "]");
				return;
			}
			if (this.mode == Mode.UNSUPPORTED_COMBINED_FILTER_MEDIA && "systems".equals(type)) {
				sendGenericCollection(exchange, "[" + system() + "]");
				return;
			}
			if (this.mode == Mode.EMPTY_ID_RESULT && "systems".equals(type) && query != null && id != null) {
				sendCollection(exchange, type, "[]", null);
				return;
			}
			if (this.mode == Mode.LATER_WRONG_ID && "systems".equals(type) && query != null && id != null) {
				sendCollection(exchange, type, "[" + system() + "]", apiRoot().resolve("systems?page=2"));
				return;
			}
			if (this.mode == Mode.LATER_WRONG_ID && "systems".equals(type) && "page=2".equals(query)) {
				sendCollection(exchange, type, "[" + otherSystem() + "]", null);
				return;
			}
			if (this.mode == Mode.LATER_WRONG_UID_PREFIX && "systems".equals(type) && id != null && id.endsWith("*")) {
				sendCollection(exchange, type, "[" + system() + "]", apiRoot().resolve("systems?page=uid-prefix-2"));
				return;
			}
			if (this.mode == Mode.LATER_WRONG_UID_PREFIX && "systems".equals(type)
					&& "page=uid-prefix-2".equals(query)) {
				sendCollection(exchange, type, "[" + nonmatchingPrefixSystem() + "]", null);
				return;
			}
			if (this.mode == Mode.UNION_COMBINED && "systems".equals(type) && query != null && query.contains("id=")
					&& query.contains("q=")) {
				sendCollection(exchange, type, "[" + system() + "," + otherSystem() + "]", null);
				return;
			}
			if (this.mode == Mode.UNION_SECOND_COMBINATION && "systems".equals(type) && queryValue(query, "q") != null
					&& queryValue(query, "geom") != null) {
				sendCollection(exchange, type, "[" + system() + "," + otherSystem() + "]", null);
				return;
			}
			if (this.mode == Mode.UNION_FEATURE_TYPE_KEYWORD && "systems".equals(type)
					&& queryValue(query, "featureType") != null && queryValue(query, "q") != null) {
				sendCollection(exchange, type, "[" + system() + "," + otherSystem() + "]", null);
				return;
			}
			if (this.mode == Mode.UNION_DATETIME_SYSTEM && "deployments".equals(type)
					&& queryValue(query, "datetime") != null && queryValue(query, "system") != null) {
				sendCollection(exchange, type, "[" + deployment() + "," + otherDeployment() + "]", null);
				return;
			}
			if (this.mode == Mode.UNION_CUSTOM_PROPERTY && "systems".equals(type) && queryValue(query, "id") != null
					&& queryValue(query, "customCode") != null) {
				sendCollection(exchange, type, "[" + system() + "," + otherSystem() + "]", null);
				return;
			}
			if (this.mode == Mode.UNION_ADDITIONAL_CUSTOM_PROPERTY && "systems".equals(type)
					&& queryValue(query, "id") != null && queryValue(query, "secondaryCode") != null) {
				sendCollection(exchange, type, "[" + system() + "," + otherSystem() + "]", null);
				return;
			}
			if (this.mode == Mode.UNION_LATER_RESOURCE_PREDICATE && "systems".equals(type) && query == null) {
				sendCollection(exchange, type, "[" + system() + "," + laterCombinedSystem() + "]", null);
				return;
			}
			if (this.mode == Mode.UNION_LATER_RESOURCE_PREDICATE && "systems".equals(type)
					&& queryValue(query, "id") != null && queryValue(query, "laterCode") != null) {
				sendCollection(exchange, type, "[" + laterCombinedSystem() + "," + system() + "]", null);
				return;
			}
			if (this.mode == Mode.UNION_LATER_RESOURCE_PREDICATE && "systems".equals(type)
					&& (queryValue(query, "laterCode") != null || "system-2".equals(queryValue(query, "id"))
							|| "Later".equals(queryValue(query, "q")))) {
				sendCollection(exchange, type, "[" + laterCombinedSystem() + "]", null);
				return;
			}
			if (this.mode == Mode.CONTRADICTORY_SYSTEM_FOI_WRAPPER && "systems".equals(type)
					&& queryValue(query, "foi") != null
					&& !Set.of("foi-real", "urn:example:foi:real").contains(queryValue(query, "foi"))) {
				sendCollection(exchange, type, "[]", null);
				return;
			}
			if (this.mode == Mode.CONTRADICTORY_DEPLOYED_SYSTEM_WRAPPER && "deployments".equals(type)
					&& queryValue(query, "system") != null
					&& !Set.of("system-real", "urn:example:system:real").contains(queryValue(query, "system"))) {
				sendCollection(exchange, type, "[]", null);
				return;
			}
			if (this.mode == Mode.WRONG_GEOMETRY && "systems".equals(type) && query != null
					&& query.startsWith("geom=")) {
				sendCollection(exchange, type, "[" + otherSystem() + "]", null);
				return;
			}
			if (this.mode == Mode.RESOLVED_TARGET_IDENTIFIERS && "systems".equals(type)
					&& queryValue(query, "parent") != null
					&& !Set.of("parent-real", "urn:example:system:parent-real").contains(queryValue(query, "parent"))) {
				sendCollection(exchange, type, "[]", null);
				return;
			}
			if (this.mode == Mode.PAGINATED_ASSOCIATION && "systems".equals(type) && queryValue(query, "parent") != null
					&& !Set.of("parent-real", "urn:example:system:parent-real").contains(queryValue(query, "parent"))) {
				sendCollection(exchange, type, "[]", null);
				return;
			}
			if (this.mode == Mode.LATER_INDIRECT_RESOURCES && query == null
					&& ("properties".equals(type) || "samplingFeatures".equals(type))) {
				String page = "properties".equals(type) ? "property-2" : "sf-2";
				String item = "properties".equals(type) ? property() : samplingFeature();
				sendCollection(exchange, type, "[" + item + "]", apiRoot().resolve(type + "?page=" + page));
				return;
			}
			if (this.mode == Mode.LATER_INDIRECT_RESOURCES && "page=property-2".equals(query)
					&& "properties".equals(type)) {
				sendCollection(exchange, type, "[" + property2() + "]", null);
				return;
			}
			if (this.mode == Mode.LATER_INDIRECT_RESOURCES && "page=sf-2".equals(query)
					&& "samplingFeatures".equals(type)) {
				sendCollection(exchange, type, "[" + samplingFeature2() + "]", null);
				return;
			}
			String item = switch (type) {
				case "systems" -> system();
				case "deployments" -> deployment();
				case "procedures" -> procedure();
				case "samplingFeatures" -> samplingFeature();
				case "properties" -> property();
				default -> throw new IllegalArgumentException(type);
			};
			sendCollection(exchange, type, "[" + item + "]", null);
		}

		private void systemSamplingFeatures(HttpExchange exchange, String query) throws IOException {
			if (this.mode == Mode.CONTRADICTORY_SYSTEM_FOI_WRAPPER) {
				sendCollection(exchange, "samplingFeatures", "[" + samplingFeatureWrapper() + "]", null);
				return;
			}
			canonical(exchange, "samplingFeatures", query);
		}

		private void sendCollection(HttpExchange exchange, String type, String items, URI next) throws IOException {
			if ("properties".equals(type)) {
				send(exchange, 200, "application/sml+json", "{\"items\":" + items + links(next) + "}");
			}
			else {
				send(exchange, 200, "application/geo+json",
						"{\"type\":\"FeatureCollection\",\"features\":" + items + links(next) + "}");
			}
		}

		private void sendGenericCollection(HttpExchange exchange, String items) throws IOException {
			send(exchange, 200, "application/json", "{\"items\":" + items + "}");
		}

		private String links(URI next) {
			return next == null ? ""
					: ",\"links\":[{\"rel\":\"next\",\"type\":\"application/geo+json\",\"href\":\"" + next + "\"}]";
		}

		private void single(HttpExchange exchange, String type, String body) throws IOException {
			send(exchange, 200, "properties".equals(type) ? "application/sml+json" : "application/geo+json", body);
		}

		private void stream(HttpExchange exchange, boolean observed) throws IOException {
			String key = observed ? "observedProperties" : "controlledProperties";
			String value = observed ? "urn:example:property:observed" : "urn:example:property:controlled";
			send(exchange, 200, "application/json", "{\"items\":[{\"id\":\"stream-1\",\"" + key
					+ "\":[{\"id\":\"property-1\",\"uid\":\"" + value + "\"}]}]}");
		}

		private void prescribedSubresource(HttpExchange exchange, String type, String query) throws IOException {
			if (this.mode == Mode.ROOT_ASSOCIATION_SHORTCUTS) {
				send(exchange, 404, "application/json", "{}");
				return;
			}
			if (this.mode == Mode.CONTRADICTORY_DEPLOYED_SYSTEM_WRAPPER && "systems".equals(type)) {
				sendCollection(exchange, type, "[" + deployedSystemWrapper() + "]", null);
				return;
			}
			if (this.mode == Mode.DEPLOYED_PROPERTY_WRAPPER_SHORTCUT && "systems".equals(type)) {
				sendCollection(exchange, type, "[" + deployedPropertyWrapperShortcut() + "]", null);
				return;
			}
			if (this.mode == Mode.DEPLOYED_PROPERTY_NESTED_HREF_SHORTCUT && "systems".equals(type)) {
				sendCollection(exchange, type, "[" + deployedPropertyNestedHrefShortcut() + "]", null);
				return;
			}
			if (this.mode == Mode.DEPLOYED_PROPERTY_NON_SYSTEM_TARGET && "systems".equals(type)) {
				sendCollection(exchange, type, "[" + deployedPropertyTarget("deployed-not-system") + "]", null);
				return;
			}
			if (this.mode == Mode.DEPLOYED_PROPERTY_COLLECTION_TARGET && "systems".equals(type)) {
				sendCollection(exchange, type, "[" + deployedPropertyTarget("deployed-collection") + "]", null);
				return;
			}
			if (this.mode == Mode.DEPLOYED_PROPERTY_TYPED_SYSTEM_TARGET && "systems".equals(type)) {
				sendCollection(exchange, type, "[" + deployedPropertyTarget("deployed-typed") + "]", null);
				return;
			}
			if (this.mode == Mode.DEPLOYED_PROPERTY_SUFFIX_SYSTEM_TARGET && "systems".equals(type)) {
				sendCollection(exchange, type, "[" + deployedPropertyTarget("deployed-suffix") + "]", null);
				return;
			}
			canonical(exchange, type, query);
		}

		private void prescribedStream(HttpExchange exchange, boolean observed) throws IOException {
			if (this.mode == Mode.ROOT_ASSOCIATION_SHORTCUTS) {
				send(exchange, 404, "application/json", "{}");
				return;
			}
			stream(exchange, observed);
		}

		private void associationCollection(HttpExchange exchange, String query) throws IOException {
			if (this.mode != Mode.PAGINATED_ASSOCIATION) {
				send(exchange, 404, "application/json", "{}");
				return;
			}
			if ("page=2".equals(query)) {
				sendCollection(exchange, "systems", "[" + resolvedParentSystem() + "]", null);
				return;
			}
			sendCollection(exchange, "systems", "[]", apiRoot().resolve("systems/parent-collection?page=2"));
		}

		private String system() {
			if (this.mode == Mode.CANONICAL_OGC_RELATION) {
				return systemWithOgcRelation();
			}
			if (this.mode == Mode.GEOJSON_SYSTEM_KIND_LINK) {
				return systemWithKindLink();
			}
			if (this.mode == Mode.UNRELATED_RELATION_SCHEME) {
				return systemWithUnrelatedRelationScheme();
			}
			if (this.mode == Mode.FIELD_LINK_SUFFIX_ALIASES) {
				return systemWithFieldLinkSuffixAliases();
			}
			if (this.mode == Mode.OGC_REL_NEAR_MISSES) {
				return systemWithOgcRelationNearMisses();
			}
			if (this.mode == Mode.BROAD_RELATION_ALIASES) {
				return systemWithBroadRelationAliases();
			}
			String propertyAssociations = this.mode == Mode.NO_ASSOCIATIONS ? "" : systemPropertyAssociations();
			String parentLink = this.mode == Mode.NO_ASSOCIATIONS ? "" : systemParentLink();
			String additionalCustom = this.mode == Mode.UNION_ADDITIONAL_CUSTOM_PROPERTY ? ",\"secondaryCode\":\"beta\""
					: "";
			return """
					{"type":"Feature","id":"system-1","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:System","uid":"urn:example:system:1",
					 "name":"Weather Station","customCode":"alpha"%s%s},
					 "links":[{"rel":"canonical","type":"application/geo+json","href":"%s"}%s]}
					""".formatted(additionalCustom, propertyAssociations, apiRoot().resolve("systems/system-1"),
					parentLink);
		}

		private String systemWithOgcRelation() {
			return """
					{"type":"Feature","id":"system-1","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:System","uid":"urn:example:system:1",
					 "name":"Weather Station","customCode":"alpha"},
					 "links":[
					  {"rel":"canonical","type":"application/geo+json","href":"%s"},
					  {"rel":"ogc-rel:parentSystem","type":"application/geo+json","href":"%s"}]}
					""".formatted(apiRoot().resolve("systems/system-1"), apiRoot().resolve("systems/parent-1"));
		}

		private String systemWithKindLink() {
			return """
					{"type":"Feature","id":"system-1","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:System","uid":"urn:example:system:1",
					 "name":"Weather Station","customCode":"alpha",
					 "systemKind@link":{"href":"%s"}},
					 "links":[{"rel":"canonical","type":"application/geo+json","href":"%s"}]}
					""".formatted(apiRoot().resolve("procedures/procedure-1"), apiRoot().resolve("systems/system-1"));
		}

		private String systemWithUnrelatedRelationScheme() {
			return """
					{"type":"Feature","id":"system-1","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:System","uid":"urn:example:system:1",
					 "name":"Weather Station","customCode":"alpha"},
					 "links":[
					  {"rel":"canonical","type":"application/geo+json","href":"%s"},
					  {"rel":"custom:parentSystem","type":"application/geo+json","href":"%s"}]}
						""".formatted(apiRoot().resolve("systems/system-1"), apiRoot().resolve("systems/parent-1"));
		}

		private String systemWithFieldLinkSuffixAliases() {
			return """
					{"type":"Feature","id":"system-1","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:System","uid":"urn:example:system:1",
					 "name":"Weather Station",
					 "parentSystemLink":{"href":"%s"},
					 "systemKindLink":{"href":"%s"}},
					 "links":[{"rel":"canonical","type":"application/geo+json","href":"%s"}]}
					""".formatted(apiRoot().resolve("systems/parent-1"), apiRoot().resolve("procedures/procedure-1"),
					apiRoot().resolve("systems/system-1"));
		}

		private String systemWithOgcRelationNearMisses() {
			return """
					{"type":"Feature","id":"system-1","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:System","uid":"urn:example:system:1",
					 "name":"Weather Station"},
					 "links":[
					  {"rel":"canonical","type":"application/geo+json","href":"%s"},
					  {"rel":"ogc-rel:parentSystemLink","type":"application/geo+json","href":"%s"},
					  {"rel":"ogc-rel:ParentSystem","type":"application/geo+json","href":"%s"},
					  {"rel":"ogc-rel:parent-system","type":"application/geo+json","href":"%s"}]}
					""".formatted(apiRoot().resolve("systems/system-1"), apiRoot().resolve("systems/parent-1"),
					apiRoot().resolve("systems/parent-1"), apiRoot().resolve("systems/parent-1"));
		}

		private String systemWithBroadRelationAliases() {
			return """
					{"type":"Feature","id":"system-1","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:System","uid":"urn:example:system:1",
					 "name":"Weather Station",
					 "parent":{"href":"%s"},
					 "procedure":{"href":"%s"}},
					 "links":[{"rel":"canonical","type":"application/geo+json","href":"%s"}]}
					""".formatted(apiRoot().resolve("systems/parent-1"), apiRoot().resolve("procedures/procedure-1"),
					apiRoot().resolve("systems/system-1"));
		}

		private String sensorMlSystem() {
			return """
					{"type":"PhysicalSystem","id":"system-1","definition":"sosa:System",
					 "uniqueId":"urn:example:system:1","label":"Weather Station",
					 "attachedTo":{"href":"%s"},"typeOf":{"href":"%s"}}
					""".formatted(apiRoot().resolve("systems/parent-1"), apiRoot().resolve("procedures/procedure-1"));
		}

		private String systemPropertyAssociations() {
			if (this.mode == Mode.MALFORMED_ASSOCIATION_HREF) {
				return """
						,"systemKind@link":{"id":"procedure-1"},
						 "observedProperties":[{"id":"property-local"},{"href":"%%%"}],
						 "controlledProperties":[{"id":"property-local"}]
						""";
			}
			if (this.mode == Mode.UNRELATED_SUFFIX_ALIAS) {
				return """
						,"unrelatedParent":{"id":"parent-shortcut",
						 "uid":"urn:example:system:parent-shortcut"}
						""";
			}
			if (this.mode == Mode.NESTED_EXTENSION_ALIAS) {
				return """
						,"metadata":{"parentSystem":{"id":"parent-shortcut",
						 "uid":"urn:example:system:parent-shortcut"}}
						""";
			}
			if (this.mode == Mode.OVER_DEPTH_RELATION) {
				return "";
			}
			return """
					,"systemKind@link":{"id":"procedure-1","uid":"urn:example:procedure:1","href":"%s"},
					 "observedProperties":[{"id":"property-1","uid":"urn:example:property:observed"}],
					 "controlledProperties":[{"id":"property-1","uid":"urn:example:property:controlled"}]
					""".formatted(apiRoot().resolve("procedures/procedure-1"));
		}

		private String systemParentLink() {
			if (this.mode == Mode.RESOLVED_TARGET_IDENTIFIERS) {
				return ",{\"rel\":\"parentSystem\",\"id\":\"wrapper-parent\","
						+ "\"uid\":\"urn:example:system:wrapper-parent\",\"href\":\""
						+ apiRoot().resolve("systems/alias-parent") + "\"}";
			}
			if (this.mode == Mode.BROKEN_ASSOCIATION) {
				return ",{\"rel\":\"parentSystem\",\"id\":\"wrapper-parent\","
						+ "\"uid\":\"urn:example:system:wrapper-parent\",\"href\":\""
						+ apiRoot().resolve("systems/broken-parent") + "\"}";
			}
			if (this.mode == Mode.WRONG_ASSOCIATION_MEDIA) {
				return ",{\"rel\":\"parentSystem\",\"id\":\"wrapper-parent\","
						+ "\"uid\":\"urn:example:system:wrapper-parent\",\"href\":\""
						+ apiRoot().resolve("systems/wrong-media-parent") + "\"}";
			}
			if (this.mode == Mode.PAGINATED_ASSOCIATION) {
				return ",{\"rel\":\"parentSystem\",\"href\":\"" + apiRoot().resolve("systems/parent-collection")
						+ "\"}";
			}
			if (this.mode == Mode.MALFORMED_ASSOCIATION_HREF || this.mode == Mode.UNRELATED_SUFFIX_ALIAS
					|| this.mode == Mode.NESTED_EXTENSION_ALIAS) {
				return "";
			}
			if (this.mode == Mode.OVER_DEPTH_RELATION) {
				String relation = "{\"id\":\"parent-1\",\"uid\":\"urn:example:system:parent\"}";
				for (int depth = 0; depth < 14; depth++) {
					relation = "{\"items\":[" + relation + "]}";
				}
				return ",{\"rel\":\"parentSystem\",\"items\":[" + relation + "]}";
			}
			URI parent = this.mode == Mode.CROSS_ORIGIN_ASSOCIATION ? this.externalTarget
					: apiRoot().resolve("systems/parent-1");
			return """
					,{"rel":"parentSystem","id":"parent-1","uid":"urn:example:system:parent","href":"%s"}
					""".formatted(parent);
		}

		private String parentSystem() {
			return """
					{"type":"Feature","id":"parent-1","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:System","uid":"urn:example:system:parent","name":"Parent System"}}
					""";
		}

		private String resolvedParentSystem() {
			return """
					{"type":"Feature","id":"parent-real","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:System","uid":"urn:example:system:parent-real",
					 "name":"Resolved Parent"}}
					""";
		}

		private String otherSystem() {
			return """
					{"type":"Feature","id":"system-2","geometry":{"type":"Point","coordinates":[0,0]},
					 "properties":{"featureType":"sosa:System","uid":"urn:example:system:2","name":"Other Sensor"}}
					""";
		}

		private String laterCombinedSystem() {
			return """
					{"type":"Feature","id":"system-2","geometry":{"type":"Point","coordinates":[-76,39]},
					 "properties":{"featureType":"sosa:System","uid":"urn:example:system:2","name":"Later Sensor",
					 "laterCode":"gamma"}}
					""";
		}

		private String nonmatchingPrefixSystem() {
			return """
					{"type":"Feature","id":"system-outside-prefix","geometry":{"type":"Point","coordinates":[0,0]},
					 "properties":{"featureType":"sosa:System","uid":"urn:other:system:2","name":"Other Sensor"}}
					""";
		}

		private String deployment() {
			if (this.mode == Mode.CANONICAL_OGC_RELATION) {
				return deploymentWithOgcRelation();
			}
			return """
					{"type":"Feature","id":"deployment-1","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:Deployment","uid":"urn:example:deployment:1",
					 "name":"Field Deployment","validTime":["2026-01-01T00:00:00Z","2027-01-01T00:00:00Z"],
					 "customCode":"alpha",
					 "deployedSystems":[{"id":"system-1","uid":"urn:example:system:1"}],
					 "featuresOfInterest":[{"id":"foi-1","uid":"urn:example:foi:1"}],
					 "observedProperties":[{"id":"property-1","uid":"urn:example:property:observed"}],
					 "controlledProperties":[{"id":"property-1","uid":"urn:example:property:controlled"}]},
					 "links":[{"rel":"parentDeployment","id":"deployment-parent",
					  "uid":"urn:example:deployment:parent","href":"%s"}]}
					""".formatted(apiRoot().resolve("deployments/deployment-parent"));
		}

		private String deploymentWithOgcRelation() {
			return """
					{"type":"Feature","id":"deployment-1","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:Deployment","uid":"urn:example:deployment:1",
					 "name":"Field Deployment",
					 "validTime":["2026-01-01T00:00:00Z","2027-01-01T00:00:00Z"]},
					 "links":[{"rel":"ogc-rel:parentDeployment","type":"application/geo+json","href":"%s"}]}
					""".formatted(apiRoot().resolve("deployments/deployment-parent"));
		}

		private String parentDeployment() {
			return """
					{"type":"Feature","id":"deployment-parent","geometry":null,
					 "properties":{"featureType":"sosa:Deployment",
					 "uid":"urn:example:deployment:parent","name":"Parent Deployment"}}
					""";
		}

		private String otherDeployment() {
			return """
					{"type":"Feature","id":"deployment-2","geometry":{"type":"Point","coordinates":[0,0]},
					 "properties":{"featureType":"sosa:Deployment","uid":"urn:example:deployment:2",
					 "name":"Other Deployment","validTime":["2030-01-01T00:00:00Z","2031-01-01T00:00:00Z"],
					 "deployedSystems":[{"id":"system-2","uid":"urn:example:system:2"}]}}
					""";
		}

		private String procedure() {
			return """
					{"type":"Feature","id":"procedure-1","geometry":null,
					 "properties":{"featureType":"sosa:ObservingProcedure","uid":"urn:example:procedure:1",
					 "name":"Weather Procedure","customCode":"alpha",
					 "observedProperties":[{"id":"property-1","uid":"urn:example:property:observed"}],
					 "controlledProperties":[{"id":"property-1","uid":"urn:example:property:controlled"}]}}
					""";
		}

		private String samplingFeature() {
			if (this.mode == Mode.CANONICAL_OGC_RELATION) {
				return samplingFeatureWithOgcRelation();
			}
			if (this.mode == Mode.CYCLIC_RELATION || this.mode == Mode.OVER_LIMIT_RELATION) {
				String target = this.mode == Mode.CYCLIC_RELATION ? "features/cycle-a" : "features/chain-0";
				return """
						{"type":"Feature","id":"sf-1","geometry":{"type":"Point","coordinates":[-77,38]},
						 "properties":{"featureType":"sosa:Sample","uid":"urn:example:sf:1","name":"Weather Sample"},
						 "links":[{"rel":"sampleOf","href":"%s"}]}
						""".formatted(apiRoot().resolve(target));
			}
			return """
					{"type":"Feature","id":"sf-1","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:Sample","uid":"urn:example:sf:1","name":"Weather Sample",
					 "customCode":"alpha",
					 "sampledFeature@link":{"href":"%s","id":"foi-1","uid":"urn:example:foi:1"},
					 "observedProperties":[{"id":"property-1","uid":"urn:example:property:observed"}],
					 "controlledProperties":[{"id":"property-1","uid":"urn:example:property:controlled"}]},
					 "links":[{"rel":"sampleOf","href":"%s","id":"parent-sf",
					  "uid":"urn:example:sf:parent"}]}
						""".formatted(apiRoot().resolve("features/foi-1"), apiRoot().resolve("features/foi-1"));
		}

		private String samplingFeature2() {
			return """
					{"type":"Feature","id":"sf-2","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:Sample","uid":"urn:example:sf:2","name":"Second Weather Sample",
					 "sampledFeature@link":{"href":"%s","id":"foi-2","uid":"urn:example:foi:2"}},
					 "links":[{"rel":"sampleOf","href":"%s","id":"parent-sf-2",
					  "uid":"urn:example:sf:parent-2"}]}
					""".formatted(apiRoot().resolve("features/foi-1"), apiRoot().resolve("features/foi-1"));
		}

		private String samplingFeatureWithOgcRelation() {
			return """
					{"type":"Feature","id":"sf-1","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:Sample","uid":"urn:example:sf:1",
					 "name":"Weather Sample",
					 "sampledFeature@link":{"href":"%s"}},
					 "links":[{"rel":"ogc-rel:sampleOf","type":"application/geo+json","href":"%s"}]}
					""".formatted(apiRoot().resolve("features/ultimate-required"), apiRoot().resolve("features/foi-1"));
		}

		private String samplingFeatureWrapper() {
			return """
					{"type":"Feature","id":"sf-wrapper","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:Sample","uid":"urn:example:sf:wrapper"},
					 "links":[{"rel":"sampleOf","id":"foi-wrapper",
					  "uid":"urn:example:foi:wrapper","href":"%s"}]}
					""".formatted(apiRoot().resolve("features/foi-real"));
		}

		private String deployedSystemWrapper() {
			return """
					{"id":"deployment-wrapper","uid":"urn:example:deployment:wrapper",
					 "system":{"id":"system-wrapper","uid":"urn:example:system:wrapper","href":"%s"}}
					""".formatted(apiRoot().resolve("systems/deployed-real"));
		}

		private String deployedPropertyWrapperShortcut() {
			return """
					{"id":"deployment-wrapper","uid":"urn:example:deployment:wrapper",
					 "observedProperties":[{"id":"property-wrapper","uid":"urn:example:property:wrapper"}],
					 "controlledProperties":[{"id":"property-wrapper","uid":"urn:example:property:wrapper"}],
					 "system":{"href":"%s"}}
					""".formatted(apiRoot().resolve("systems/deployed-clean"));
		}

		private String deployedPropertyNestedHrefShortcut() {
			return """
					{"id":"deployment-wrapper","uid":"urn:example:deployment:wrapper",
					 "system":{"href":"%s","metadata":{"href":"%s"}}}
					""".formatted(apiRoot().resolve("systems/deployed-clean"),
					apiRoot().resolve("systems/deployed-bogus"));
		}

		private String deployedPropertyTarget(String target) {
			return """
					{"id":"deployment-wrapper","uid":"urn:example:deployment:wrapper",
					 "system":{"href":"%s"}}
					""".formatted(apiRoot().resolve("systems/" + target));
		}

		private String deployedSystemWithoutProperties() {
			return """
					{"type":"Feature","id":"deployed-clean","geometry":null,
					 "properties":{"featureType":"sosa:System","uid":"urn:example:system:clean",
					 "name":"Clean Deployed System"}}
					""";
		}

		private String deployedSystemWithBogusProperties() {
			return """
					{"type":"Feature","id":"deployed-bogus","geometry":null,
					 "properties":{"featureType":"sosa:System","uid":"urn:example:system:bogus",
					 "name":"Unrelated Deployed System",
					 "observedProperties":[{"id":"property-nested"}],
					 "controlledProperties":[{"id":"property-nested"}]}}
					""";
		}

		private String deployedSystemWithTargetProperties() {
			return """
					{"type":"Feature","id":"deployed-target","geometry":null,
					 "properties":{"featureType":"sosa:System","uid":"urn:example:system:target",
					 "name":"Target Deployed System",
					 "observedProperties":[{"id":"property-target",
					  "uid":"urn:example:property:target"}]}}
					""";
		}

		private void typedDeployedSystem(HttpExchange exchange) throws IOException {
			boolean sensorMl = Set.of("PhysicalComponent", "PhysicalSystem", "SimpleProcess", "AggregateProcess")
				.contains(this.deployedSystemType);
			send(exchange, 200, sensorMl ? "application/sml+json" : "application/geo+json",
					typedSystemWithTargetProperties(sensorMl));
		}

		private String typedSystemWithTargetProperties(boolean sensorMl) {
			if (sensorMl) {
				return """
						{"type":"%s","id":"deployed-target","definition":"sosa:System",
						 "uniqueId":"urn:example:system:target","label":"Target Deployed System",
						 "outputs":[{"id":"property-target",
						  "uid":"urn:example:property:target"}]}
						""".formatted(this.deployedSystemType);
			}
			return """
					{"type":"Feature","id":"deployed-target","geometry":null,
					 "properties":{"featureType":"%s","uid":"urn:example:system:target",
					 "name":"Target Deployed System",
					 "observedProperties":[{"id":"property-target",
					  "uid":"urn:example:property:target"}]}}
					""".formatted(this.deployedSystemType);
		}

		private String suffixSystemWithTargetProperties() {
			return """
					{"type":"Feature","id":"deployed-suffix","geometry":null,
					 "properties":{"featureType":"custom:NotSystem","uid":"urn:example:system:suffix",
					 "name":"Suffix Impostor",
					 "observedProperties":[{"id":"property-target",
					  "uid":"urn:example:property:target"}]}}
					""";
		}

		private String nonSystemWithTargetProperties() {
			return """
					{"type":"Feature","id":"not-system","geometry":null,
					 "properties":{"featureType":"sosa:Deployment","uid":"urn:example:deployment:not-system",
					 "name":"Not a System",
					 "observedProperties":[{"id":"property-target",
					  "uid":"urn:example:property:target"}]}}
					""";
		}

		private String property() {
			return """
					{"id":"property-1","uniqueId":"urn:example:property:1","label":"Weather Property",
					 "baseProperty":"%s","objectType":"urn:example:type:System",
					 "customCode":"alpha",
					 "links":[{"rel":"canonical","type":"application/sml+json","href":"%s"}]}
					""".formatted(apiRoot().resolve("properties/base-1"), apiRoot().resolve("properties/property-1"));
		}

		private String property2() {
			return """
					{"id":"property-2","uniqueId":"urn:example:property:2","label":"Second Weather Property",
					 "baseProperty":"%s","objectType":"urn:example:type:System"}
					""".formatted(apiRoot().resolve("properties/base-2"));
		}

		private String featureOfInterest() {
			return """
					{"type":"Feature","id":"foi-1","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:FeatureOfInterest","uid":"urn:example:foi:1",
					 "name":"Weather Feature"}}
					""";
		}

		private String requiredUltimateFeature() {
			return """
					{"type":"Feature","id":"ultimate-required","geometry":null,
					 "properties":{"featureType":"sosa:FeatureOfInterest",
					 "uid":"urn:example:foi:ultimate-required",
					 "name":"Required Ultimate Feature"}}
					""";
		}

		private String resolvedFeatureOfInterest() {
			return """
					{"type":"Feature","id":"foi-real","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:FeatureOfInterest","uid":"urn:example:foi:real",
					 "name":"Resolved Feature"}}
					""";
		}

		private String deployedSystemTarget() {
			return """
					{"type":"Feature","id":"system-real","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:System","uid":"urn:example:system:real",
					 "name":"Resolved Deployed System"}}
					""";
		}

		private String baseProperty(String id) {
			return """
					{"id":"%s","uniqueId":"%s","label":"Base Weather Property",
					 "objectType":"urn:example:type:System"}
					""".formatted(id, apiRoot().resolve("properties/" + id));
		}

		private void relationTarget(HttpExchange exchange, String path, boolean cycle) throws IOException {
			String token = path.substring(path.lastIndexOf('/') + 1);
			String next;
			if (cycle) {
				next = token.endsWith("a") ? "features/cycle-b" : "features/cycle-a";
			}
			else {
				int index = Integer.parseInt(token.substring("chain-".length()));
				next = "features/chain-" + (index + 1);
			}
			String body = """
					{"type":"Feature","id":"%s","geometry":{"type":"Point","coordinates":[-77,38]},
					 "properties":{"featureType":"sosa:Sample","uid":"urn:example:%s","name":"Relation Target"},
					 "links":[{"rel":"sampleOf","href":"%s"}]}
					""".formatted(token, token, apiRoot().resolve(next));
			single(exchange, "samplingFeatures", body);
		}

		private static String queryValue(String query, String key) {
			if (query == null) {
				return null;
			}
			for (String pair : query.split("&")) {
				String[] parts = pair.split("=", 2);
				if (URLDecoder.decode(parts[0], StandardCharsets.UTF_8).equals(key)) {
					return parts.length == 2 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
				}
			}
			return null;
		}

		private static void send(HttpExchange exchange, int status, String contentType, String body)
				throws IOException {
			byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().set("Content-Type", contentType);
			exchange.sendResponseHeaders(status, bytes.length);
			try (OutputStream output = exchange.getResponseBody()) {
				output.write(bytes);
			}
		}

		@Override
		public void close() {
			this.server.stop(0);
		}

	}

	private static final class ExternalServer implements AutoCloseable {

		private final HttpServer server;

		private final AtomicReference<String> authorization = new AtomicReference<>();

		private final AtomicInteger calls = new AtomicInteger();

		private ExternalServer() throws IOException {
			this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
			this.server.createContext("/target", this::handle);
		}

		private void start() {
			this.server.start();
		}

		private URI target() {
			return URI.create("http://127.0.0.1:" + this.server.getAddress().getPort() + "/target");
		}

		private String authorization() {
			return this.authorization.get();
		}

		private int calls() {
			return this.calls.get();
		}

		private void handle(HttpExchange exchange) throws IOException {
			this.calls.incrementAndGet();
			this.authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
			FixtureServer.send(exchange, 200, "application/json", "{\"id\":\"external\"}");
		}

		@Override
		public void close() {
			this.server.stop(0);
		}

	}

}
