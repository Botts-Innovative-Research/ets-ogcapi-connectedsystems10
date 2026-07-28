# Sprint ETS-53 Part 1 Property Definitions Verification

Date: 2026-07-28

## Verdict

Implementation and verification are complete. All four released
OGC 23-001 `/conf/property` procedures have reviewed exact mappings. The
primary local OSH run remains honest IUT evidence, not a conformance
certification: one Property procedure fails and three skip because the
unmodified IUT does not expose the required Property collection or SensorML
media.

Raze returned `APPROVE_WITH_CONCERNS` at confidence `0.98`, with no required
fixes. Its two LOW concerns recommend future fail-closed checkout provenance in
the parity script and dedicated Property pagination/later-item fixtures.

## Implemented Procedures

- `/conf/property/canonical-url` maps exactly to
  `PropertyDefinitionsTests#everyPropertyHasCanonicalUrl()`.
- `/conf/property/resources-endpoint` maps exactly to
  `PropertyDefinitionsTests#propertyResourcesEndpointIsValid()`.
- `/conf/property/canonical-endpoint` maps exactly to
  `PropertyDefinitionsTests#canonicalPropertiesEndpointIsValid()`.
- `/conf/property/collections` maps exactly to
  `PropertyDefinitionsTests#propertyCollectionsAreValid()`.

The four methods are independent, use immutable API-root setup, and depend
directly on Part 1 API Common. `PropertyDefinitionsSupport` owns exact
`sosa:Property` selection, SensorML Property schema dispatch, canonical
identity, and canonical-content normalization. The executable
`ets-sensorml30` suite jar is not imported.

## Verification

- Test-first: expected compilation failure with 39 missing production symbols.
- Focused Docker Maven: `95/0/0/0`.
- Full Docker Maven: `525/0/0/3`; the three historical harness skips remain.
- Controlled HTTP: nine Property procedure tests cover all four positive paths
  plus media, schema, metadata, canonical, pagination, completeness, isolation,
  and dependency behavior.
- Released ATS audit: PASS against
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`.
- Coverage: `240 total / 39 exact / 2 helper / 130 candidate / 69 unmapped`;
  `/conf/property` is `4/4 exact`; the deployed suite remains 220 methods.
- Property schema parity: PASS for all three entry schemas and 53 transitive
  schemas, with zero semantic or graph mismatches after reviewed resolver-only
  normalization.
- Exact-image runtime: image
  `sha256:80ca0a313ba903411f23a67efa93df631309ec43699ce5e71bfeadd642dc2bb0`
  passes TeamEngine 6 provenance, SWE Common adapter execution, dependency
  collision, immutable-base, runtime, and confidential-context checks.
- API Common sabotage: Core failure causes API Common setup and tests to skip;
  all four Property methods skip before Property endpoint access.
- Credential integration: PASS with zero unmasked credential hits.
- Credential wire E2E: PASS with zero unmasked artifact hits, 35 masked log
  events, and 35 intact synthetic transmissions to the stub IUT.
- Raze: `APPROVE_WITH_CONCERNS`, confidence `0.98`, no required fixes.

## Primary Local OSH E2E

The exact Sprint 53 source was built from a fresh temporary clone and executed
through Dockerized TeamEngine against the running local OSH instance:

- Full suite: `220 total / 40 passed / 7 failed / 173 skipped`.
- Property setup: PASS despite the documented inherited API Common datetime
  evidence limitation.
- Canonical endpoint: SKIP because actual media is `application/json`.
- Canonical URL: SKIP because no collection advertises
  `itemType=sosa:Property`.
- Collections: FAIL because no exact Property collection is advertised.
- Resources endpoint: SKIP because actual media is `application/json`.

Artifact hygiene recognized 125 request logs, including 120 IUT GETs, with
zero IUT writes and zero credential leaks. The OSH source checkout remains
clean at `4c87a65c9a967d52af9df476e65d7862c7673a15`; `/opt/osh` remains
read-only. No OSH or TeamEngine source or binary was modified.

## Evidence

- `sprint-ets-53-test-first-2026-07-28.log`
- `sprint-ets-53-focused-maven-2026-07-28.log`
- `sprint-ets-53-full-maven-2026-07-28.log`
- `sprint-ets-53-property-schema-parity-2026-07-28.json`
- `sprint-ets-53-ats-coverage-audit-2026-07-28.log`
- `sprint-ets-53-teamengine-runtime-2026-07-28.log`
- `sprint-ets-53-local-osh-teamengine-2026-07-28.xml`
- `sprint-ets-53-local-osh-teamengine-container-2026-07-28.log`
- `sprint-ets-53-local-osh-hygiene-2026-07-28.json`
- `sprint-ets-53-apicommon-sabotage-teamengine-2026-07-28.xml`
- `sprint-ets-53-apicommon-sabotage-verdict-2026-07-28.log`
- `sprint-ets-53-apicommon-sabotage-hygiene-2026-07-28.json`
- `sprint-ets-53-credential-integration-2026-07-28.txt`
- `sprint-ets-53-credential-e2e-2026-07-28.txt`
- `.harness/evaluations/sprint-ets-53-adversarial.yaml`
