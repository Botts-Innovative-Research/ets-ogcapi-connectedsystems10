# Test Results — OGC API Connected Systems ETS

Last updated: 2026-05-07T00:00Z

## Current Sprint Evidence

Sprint ets-17 encoding relation-types read-only Generator:

- OGC source verification:
  - GeoJSON clause: `api/part1/standard/sections/clause_20_requirements_class_geojson_encoding.adoc`, HTTP 200 on 2026-05-06.
  - SensorML clause: `api/part1/standard/sections/clause_21_requirements_class_sensorml_encoding.adoc`, HTTP 200 on 2026-05-06.
  - Both clauses define `/req/*/relation-types` as requiring association links in JSON `links` members to use the association name as `rel`.
- GeoRobotix planning probes:
  - `/systems/0mqcvdnfoca0`: generic `canonical`/`alternate` links plus association links `samplingFeatures` and `datastreams`; association rels match association names.
  - `/deployments/16sp744ch58g`: `links` contains only generic `canonical`/`alternate`; `deployedSystems@link` is under `properties`.
  - `/procedures/164p7ed8l47g`: `links` contains only generic `canonical`/`alternate`.
  - `/samplingFeatures/0mtff3l0oofg`: no `links` member; `hostedProcedure@link` is under `properties`.
  - SensorML system/deployment/procedure bodies observed during planning: no links-member association links.
- Interpretation: Sprint 17 must not count canonical/alternate/pagination links or property-level `@link` objects as relation-types PASS evidence. It should PASS only from links-member association rels valid for the selected encoding and resource type, SKIP when no links-member association exists, and FAIL malformed or wrong-resource association-link rels.
- Raze planning review:
  - `.harness/evaluations/sprint-ets-17-plan-adversarial.yaml`: `GAPS_FOUND` confidence 0.88.
  - Required fix: close the global association-name allowlist false PASS risk by requiring resource-specific GeoJSON and SensorML links-member association allowlists.
  - `.harness/evaluations/sprint-ets-17-plan-gapfix.yaml`: `APPROVE` confidence 0.94, no remaining required fixes.
- Generator implementation:
  - `EncodingRelationTypes` centralizes generic-link filtering and resource-specific links-member association allowlists for GeoJSON and SensorML.
  - `GeoJsonTests` adds `geoJsonLinksMemberAssociationRelsUseResourceSpecificNames`.
  - `SensorMlTests` adds `sensorMlLinksMemberAssociationRelsUseResourceSpecificNames`.
  - `VerifyEncodingRelationTypes` adds 5 focused helper regressions.
- Maven verification:
  - Command: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `133 tests / 0 failures / 0 errors / 3 skipped`
  - Log: `ops/test-results/sprint-ets-17-maven-2026-05-06.log`
- TeamEngine E2E smoke:
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s17-generator bash scripts/smoke-test.sh`
  - Result: `82 total / 55 passed / 0 failed / 27 skipped`
  - Report: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s17-generator/s-ets-01-03-teamengine-smoke-2026-05-06.xml`
  - Log: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s17-generator/s-ets-01-03-teamengine-container-2026-05-06.log`
  - No-mutation oracle: recognized 55 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH entries for `https://api.georobotix.io/ogc/t18/api`.
- Runtime outcome:
  - `geoJsonLinksMemberAssociationRelsUseResourceSpecificNames`: PASS on GeoRobotix selected System links.
  - `sensorMlLinksMemberAssociationRelsUseResourceSpecificNames`: SKIP because the selected SensorML system representation has no top-level links-member association links.
- Raze implementation review:
  - Artifact: `.harness/evaluations/sprint-ets-17-adversarial-implementation.yaml`
  - Verdict: `APPROVE` confidence 0.91.
  - Required fixes: none.

Sprint ets-16 SensorML non-system read-only expansion:

- OGC source verification:
  - Source: `api/part1/standard/requirements/encoding/sensorml/requirements_class_sensorml.adoc`
  - Result: HTTP 200 on 2026-05-06.
  - Listed subrequirements include deployment, procedure, and property SensorML schema/mapping paths.
- GeoRobotix planning probes:
  - `/conformance`: HTTP 200; declares `/conf/sensorml`, `/conf/deployment`, `/conf/procedure`, and `/conf/property`.
  - `GET /deployments?limit=1` with `Accept: application/sml+json`: HTTP 200, `Content-Type: application/json`, top-level `items`.
  - `GET /procedures?limit=1` with `Accept: application/sml+json`: HTTP 200, `Content-Type: application/json`, top-level `items` and `links`.
  - `GET /properties?limit=1` with `Accept: application/sml+json`: HTTP 200, `Content-Type: application/json`, top-level `items`, currently empty.
- SensorML item evidence:
  - `GET /deployments/16sp744ch58g?f=sml3`: HTTP 200, `Content-Type: application/sml+json`, `type=Deployment`, matching `id`, `uniqueId`, and `deployedSystems`.
  - `GET /procedures/164p7ed8l47g?f=sml3`: HTTP 200, `Content-Type: application/sml+json`, `type=PhysicalSystem`, matching `id`, `uniqueId`, and procedure structure.
- Interpretation: Sprint 16 must not count CS API default `items` wrappers or Feature JSON as SensorML PASS evidence. Empty `/properties` is an IUT-state SKIP condition, not PASS. Sampling features are out of scope because upstream SensorML subrequirements list property schema/mapping, not sampling feature schema/mapping.
- Planning review:
  - `.harness/evaluations/sprint-ets-16-plan-adversarial.yaml`: `GAPS_FOUND` confidence 0.86.
  - Required fixes applied: explicit resource conformance-class gating for `/conf/deployment`, `/conf/procedure`, and `/conf/property`; procedure-specific mapping now requires non-identity process/procedure structure, not `identifiers` alone.
  - `.harness/evaluations/sprint-ets-16-plan-gapfix.yaml`: `APPROVE` confidence 0.94, no remaining required fixes.
- Generator implementation:
  - `SensorMlTests` now has 9 read-only @Tests, adding deployment, procedure, and property SensorML schema/mapping checks.
  - Deployment/procedure/property checks gate on matching resource conformance classes before judging SensorML evidence.
  - CS API default `items` wrappers and empty collections SKIP with requirement-cited reasons rather than PASS.
  - Deployment mapping requires explicit SensorML `type=Deployment`, preserved identity, and non-empty `deployedSystems`.
  - Procedure mapping requires explicit SensorML procedure-compatible type, preserved identity, and non-identity process/procedure structure beyond identifiers.
  - Property mapping requires property-compatible SensorML and mapping evidence when a property item exists; GeoRobotix currently has no property items.
  - `VerifySensorMlResourceMappingAssertions` adds 6 focused helper regressions.
- Maven verification:
  - Command: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `128 tests / 0 failures / 0 errors / 3 skipped`
  - Log: `ops/test-results/sprint-ets-16-maven-2026-05-06.log`
- TeamEngine E2E smoke:
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s16-generator bash scripts/smoke-test.sh`
  - Result: `80 total / 54 passed / 0 failed / 26 skipped`
  - Report: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s16-generator/s-ets-01-03-teamengine-smoke-2026-05-06.xml`
  - Log: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s16-generator/s-ets-01-03-teamengine-container-2026-05-06.log`
  - No-mutation oracle: recognized 53 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH entries for `https://api.georobotix.io/ogc/t18/api`.
- Runtime outcome:
  - `deploymentSensorMlHasSchemaAndMapping`: PASS using `application/sml+json` alternate link `https://api.georobotix.io/ogc/t18/api/deployments/16sp744ch58g?f=sml3`.
  - `procedureSensorMlHasSchemaAndMapping`: PASS using `application/sml+json` alternate link `https://api.georobotix.io/ogc/t18/api/procedures/164p7ed8l47g?f=sml3`.
  - `propertySensorMlHasSchemaAndMapping`: SKIP because `/properties` returned an empty collection.
- Raze implementation review:
  - Artifact: `.harness/evaluations/sprint-ets-16-adversarial-implementation.yaml`
  - Verdict: `APPROVE` confidence 0.92.
  - Required fixes: none.

Sprint ets-15 GeoJSON non-system read-only expansion:

- OGC source verification:
  - Source: `api/part1/standard/requirements/encoding/geojson/requirements_class_geojson.adoc`
  - Result: HTTP 200 on 2026-05-06.
  - Listed subrequirements include deployment, procedure, and sampling feature schema/mapping paths.
- GeoRobotix planning probes:
  - `/conformance`: HTTP 200; declares `/conf/geojson`, `/conf/deployment`, `/conf/procedure`, and `/conf/sf`.
  - `GET /deployments?limit=1` with `Accept: application/geo+json`: HTTP 200, `Content-Type: application/json`, top-level `items`.
  - `GET /procedures?limit=1` with `Accept: application/geo+json`: HTTP 200, `Content-Type: application/json`, top-level `items` and `links`.
  - `GET /samplingFeatures?limit=1` with `Accept: application/geo+json`: HTTP 200, `Content-Type: application/json`, top-level `items` and `links`.
- Resource-specific mapping evidence visible in fallback payloads:
  - Deployment item: `properties.deployedSystems@link`.
  - Procedure item: `geometry: null` and `properties.featureType`.
  - Sampling Feature item: `properties.hostedProcedure@link` and `properties.radius`.
- Generator implementation:
  - `GeoJsonTests` now adds read-only `/deployments`, `/procedures`, and `/samplingFeatures` GeoJSON schema/mapping checks.
  - CS API default `items` wrappers without GeoJSON `features` SKIP with requirement-cited fallback reasons rather than PASS.
  - Generic Feature shape alone does not close schema/mapping assertions; deployment, procedure, and sampling feature checks require resource-specific predicates.
  - `VerifyGeoJsonResourceMappingAssertions` adds focused helper coverage for fallback SKIP and mapping-value handling.
- Maven verification:
  - Command: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `122 tests / 0 failures / 0 errors / 3 skipped`
  - Log: `ops/test-results/sprint-ets-15-maven-2026-05-06.log`
- TeamEngine E2E smoke:
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s15-generator bash scripts/smoke-test.sh`
  - Result: `77 total / 52 passed / 0 failed / 25 skipped`
  - Report: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s15-generator/s-ets-01-03-teamengine-smoke-2026-05-06.xml`
  - Log: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s15-generator/s-ets-01-03-teamengine-container-2026-05-06.log`
  - No-mutation oracle: recognized 44 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH entries for `https://api.georobotix.io/ogc/t18/api`.
- Interpretation: Sprint 15 does not count CS API default `items` wrappers as GeoJSON FeatureCollection PASS evidence. They SKIP until `features` is observed, and generic Feature shape alone does not close resource-specific schema/mapping assertions.
- Raze planning review:
  - Artifact: `.harness/evaluations/sprint-ets-15-plan-adversarial.yaml`
  - Verdict: `GAPS_FOUND` confidence 0.86.
  - Required fix: add resource-specific predicates before schema/mapping PASS.
- Raze planning gap-fix recheck:
  - Artifact: `.harness/evaluations/sprint-ets-15-plan-gapfix.yaml`
  - Verdict: `APPROVE` confidence 0.93.
  - Required fixes: none remaining.
- Raze implementation review:
  - Artifact: `.harness/evaluations/sprint-ets-15-adversarial-implementation.yaml`
  - Verdict: `APPROVE_WITH_CONCERNS` confidence 0.91.
  - Concern: stale `GeoJsonTests` class javadoc still described non-system GeoJSON as future work.
- Raze implementation gap-fix recheck:
  - Artifact: `.harness/evaluations/sprint-ets-15-adversarial-gapfix.yaml`
  - Verdict: `APPROVE` confidence 0.96.
  - Required fixes: none remaining.

Sprint ets-14 Update positive mutable-IUT hardening:

- Planning probe:
  - Local OSH IUT host URL: `http://localhost:8081/sensorhub/api`
  - Seeded System resource: `/systems/040g`
  - `OPTIONS /systems/040g`: HTTP 200, `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS`; PATCH absent.
  - Simple authenticated `/conformance` curl probe returned HTTP 401 with attempted basic credentials; established TeamEngine smoke credential path remains the authoritative local path.
- Interpretation: local OSH remains a dedicated mutable CRD fixture, but current evidence does not support positive Update/PATCH execution. Sprint 14 Generator must not issue PATCH unless `/conf/update`, `OPTIONS PATCH`, and changed-field verification are all available. If a future probe confirms `/conf/update` while successful `OPTIONS /systems/{id}` still omits PATCH, the readiness assertion should FAIL for `/req/update/system` and lifecycle should SKIP before PATCH.
- Maven verification:
  - Command: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `117 tests / 0 failures / 0 errors / 3 skipped`
  - Log: `ops/test-results/sprint-ets-14-maven-2026-05-06.log`
- TeamEngine E2E smoke:
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s14-generator bash scripts/smoke-test.sh`
  - Result: `74 total / 52 passed / 0 failed / 22 skipped`
  - Report: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s14-generator/s-ets-01-03-teamengine-smoke-2026-05-06.xml`
  - Log: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s14-generator/s-ets-01-03-teamengine-container-2026-05-06.log`
  - No-mutation oracle: recognized 41 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH entries for `https://api.georobotix.io/ogc/t18/api`.
- Local OSH readiness probe:
  - `/conformance`: HTTP 401 at `http://localhost:8081/sensorhub/api/conformance`.
  - `OPTIONS /systems/040g`: HTTP 200, `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS`; PATCH absent.
  - No local OSH PATCH was issued.
- Raze implementation review:
  - Artifact: `.harness/evaluations/sprint-ets-14-adversarial-implementation.yaml`
  - Verdict: `GAPS_FOUND` confidence 0.84.
  - Required fix: add REQ/SCENARIO trace comments to `VerifyUpdateChangedFieldAssertion`.
- Raze gap-fix recheck:
  - Artifact: `.harness/evaluations/sprint-ets-14-adversarial-gapfix.yaml`
  - Verdict: `APPROVE` confidence 0.94.
  - Required fixes: none remaining.

Sprint ets-13 Update/PATCH safety-gated systems subset:

- Current repo base before implementation: `21c409c`
- Maven verification: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `113 tests / 0 failures / 0 errors / 3 skipped`
  - Log: `ops/test-results/sprint-ets-13-maven-2026-05-06.log`
- TeamEngine E2E smoke:
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results bash scripts/smoke-test.sh`
  - Result: `74 total / 52 passed / 0 failed / 22 skipped`
  - Report: `/tmp/ets-ogcapi-connectedsystems10-smoke-results/s-ets-01-03-teamengine-smoke-2026-05-06.xml`
  - Log: `/tmp/ets-ogcapi-connectedsystems10-smoke-results/s-ets-01-03-teamengine-container-2026-05-06.log`
- No-mutation oracle: integrated smoke oracle recognized 41 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH entries for `https://api.georobotix.io/ogc/t18/api`.
- Update runtime outcome against GeoRobotix: the Update configuration method skips with the missing `/conf/update` reason, and the 5 Update @Tests are skipped in default smoke through the `update -> createreplacedelete` dependency because Create/Replace/Delete's public-IUT mutation safety gate intentionally skips. No PATCH was issued.
- Raze implementation review: `.harness/evaluations/sprint-ets-13-adversarial-implementation.yaml` reported `GAPS_FOUND` 0.86 for documentation/evidence gaps; required fixes applied by updating stale headers/status, archiving the Maven log, and documenting dependency-skip masking.
- Quinn independent Gate 3.5:
  - Gate artifact: `.harness/evaluations/sprint-ets-13-evaluator-gate.yaml`
  - Verdict: `APPROVE_WITH_CONCERNS` confidence 0.91
  - Clone: `/tmp/quinn-sprint-ets-13-cd38223` at `cd3822369f3c9b3d99efb61ea623560ca9516446`
  - Maven command: `bash scripts/mvn-test-via-docker.sh`
  - Maven result: BUILD SUCCESS, `113 tests / 0 failures / 0 errors / 3 skipped`
  - Smoke command: `SMOKE_CONTAINER_NAME=quinn-ets13-gate-smoke SMOKE_IMAGE_TAG=ets-ogcapi-connectedsystems10:quinn-ets13-gate SMOKE_OUTPUT_DIR=/tmp/quinn-ets13-gate-smoke-results bash scripts/smoke-test.sh`
  - Smoke result: `74 total / 52 passed / 0 failed / 22 skipped`
  - Smoke report: `/tmp/quinn-ets13-gate-smoke-results/s-ets-01-03-teamengine-smoke-2026-05-06.xml`
  - Smoke log: `/tmp/quinn-ets13-gate-smoke-results/s-ets-01-03-teamengine-container-2026-05-06.log`
  - No-mutation oracle replay: `recognized_iut_request_logs=41`; zero IUT-bound POST/PUT/DELETE/PATCH.
  - Update runtime outcome: `fetchUpdateInputs` SKIP cites missing `/conf/update`; the 5 Update @Tests SKIP through the `update -> createreplacedelete` dependency because the default CRD mutation safety gate skips lifecycle mutation. No PATCH was issued.
- Raze review of Quinn artifact: `.harness/evaluations/sprint-ets-13-quinn-gate-raze-review.yaml` `APPROVE` confidence 0.89 after correcting the Quinn artifact file list.
- Raze independent Gate 4:
  - Gate artifact: `.harness/evaluations/sprint-ets-13-adversarial-gate.yaml`
  - Verdict: `APPROVE_WITH_CONCERNS` confidence 0.90
  - Clone: `/tmp/raze-sprint-ets-13-gate` at `cd38223`
  - Maven command: `bash scripts/mvn-test-via-docker.sh`
  - Maven result: BUILD SUCCESS, `113 tests / 0 failures / 0 errors / 3 skipped`
  - No-mutation oracle self-test: PASS
  - Smoke command: `SMOKE_CONTAINER_NAME=raze-ets-csapi-smoke-s13-gate SMOKE_IMAGE_TAG=ets-ogcapi-connectedsystems10:raze-s13-gate SMOKE_OUTPUT_DIR=/tmp/raze-sprint-ets-13-gate-smoke-results SMOKE_RUN_TIMEOUT_S=900 bash scripts/smoke-test.sh`
  - Smoke result: `74 total / 52 passed / 0 failed / 22 skipped`
  - Smoke report: `/tmp/raze-sprint-ets-13-gate-smoke-results/s-ets-01-03-teamengine-smoke-2026-05-06.xml`
  - Smoke log: `/tmp/raze-sprint-ets-13-gate-smoke-results/s-ets-01-03-teamengine-container-2026-05-06.log`
  - No-mutation oracle replay: `recognized_iut_request_logs=41`; zero IUT-bound POST/PUT/DELETE/PATCH.
  - Required fixes: none. Low follow-up: decide `OPTIONS Allow: PATCH` readiness semantics before using Sprint 13 as a positive mutable-IUT conformance gate.
- Scope note: this is PARTIAL for REQ-ETS-PART1-011. Deployment/procedure/sampling-feature/property PATCH, Feature Collection update paths, Part 2 update, optimistic locking, and PATCH media-type matrix remain open.

Sprint ets-12 Create/Replace/Delete safety-gated systems subset:

- Current repo base before implementation: `7427c3c`
- Maven verification: `bash scripts/mvn-test-via-docker.sh`
  - Original Generator result: BUILD SUCCESS, `105 tests / 0 failures / 0 errors / 3 skipped`
  - Local OSH follow-up result after Location/UID regressions: BUILD SUCCESS, `110 tests / 0 failures / 0 errors / 3 skipped`
- TeamEngine E2E smoke:
  - Copy: `/tmp/sprint-ets-12-generator-smoke-current-r3`
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s12-generator-r3 bash scripts/smoke-test.sh`
  - Result: `69 total / 52 passed / 0 failed / 17 skipped`
  - Report: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s12-generator-r3/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
  - Log: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s12-generator-r3/s-ets-01-03-teamengine-container-2026-05-05.log`
- CreateReplaceDelete runtime outcome: 4 PASS and 2 SKIP against GeoRobotix. PASS: declaration, dependency tracer, `OPTIONS /systems`, `OPTIONS /systems/{id}`. SKIP: mutation safety gate and lifecycle opt-in because default smoke does not set mutation parameters.
- No-mutation oracle: integrated smoke oracle recognized 40 IUT-bound request log entries and reported zero IUT-bound POST/PUT/DELETE entries for `https://api.georobotix.io/ogc/t18/api`.
- Raze gap-fix review: `.harness/evaluations/sprint-ets-12-adversarial-gapfix.yaml` `APPROVE_WITH_CONCERNS` 0.91, with no required fixes remaining. Residual low concern: smoke stdout was not archived separately, but the oracle result is reproducible from the r3 container log.
- Local OSH mutable-IUT probe:
  - IUT: local OpenSensorHub 2.0-beta2 at `http://localhost:8081/sensorhub/api`; TeamEngine reached it as `http://field-hub-osh-1:8081/sensorhub/api` on Docker network `field-hub_default`.
  - Command shape: `SMOKE_DOCKER_NETWORK=field-hub_default`, `SMOKE_MUTATION_TESTS_ENABLED=true`, `SMOKE_MUTATION_IUT_POLICY=dedicated-mutable-iut`, output `/tmp/ets-csapi-osh-mutable-smoke-r4`.
  - Result: `69 total / 32 passed / 3 failed / 34 skipped`; not a full-suite PASS.
  - CRD lifecycle outcome: `systemsCreateReplaceDeleteLifecycle` PASS. OSH log evidence shows POST added `/systems/0410`, PUT updated `/systems/0410`, and DELETE removed `/systems/0410`.
  - Remaining local OSH failures: empty `/procedures`, `/deployments`, and `/samplingFeatures`; prior probe also exposed public `https://osh.gis.tw` SensorML alternate links from OSH `proxyBaseUrl`.
  - Cleanup: manual seed `/systems/040g` deleted after the run; `/systems` returned `{"items":[]}`.
- Local OSH full-health run after fixture seeding:
  - OSH config: `proxyBaseUrl` updated to `http://field-hub-osh-1:8081` in `../sar-ops/field-hub/osh/config/config.json`, then OSH restarted.
  - Seed state: synthetic `/systems/040g`, `/procedures/040g`, `/deployments/040g`, and `/samplingFeatures/040g` exist. Exact payloads are versioned in `ops/local-osh-seed-fixtures.json`. System seed uses `featureType=http://www.w3.org/ns/sosa/System`; direct `/systems/040g?f=sml3` returned HTTP 200 and `Content-Type: application/sml+json`.
  - Command: `SMOKE_CONTAINER_NAME=ets-csapi-osh-full-health-r3 SMOKE_DOCKER_NETWORK=field-hub_default SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api SMOKE_MUTATION_TESTS_ENABLED=true SMOKE_MUTATION_IUT_POLICY=dedicated-mutable-iut SMOKE_OUTPUT_DIR=/tmp/ets-csapi-osh-full-health-r3 bash scripts/smoke-test.sh`
  - Result: SMOKE PASS, `69 total / 50 passed / 0 failed / 19 skipped`.
  - Smoke stdout now prints exact parsed totals: `SMOKE PASS: total=69 passed=50 failed=0 skipped=19 ...`.
  - Report: `/tmp/ets-csapi-osh-full-health-r3/s-ets-01-03-teamengine-smoke-2026-05-06.xml`
  - Log: `/tmp/ets-csapi-osh-full-health-r3/s-ets-01-03-teamengine-container-2026-05-06.log`
  - CRD lifecycle: real POST, PUT, and DELETE against temporary `/systems/0410`; no-mutation oracle skipped by design because the run explicitly enabled dedicated mutable-IUT mutation tests.
- Raze local OSH full-health review: `.harness/evaluations/sprint-ets-12-local-osh-full-health-raze.yaml` `GAPS_FOUND` 0.87. Required fixes applied: `scripts/smoke-test.sh` now prints exact totals (`total/passed/failed/skipped`) instead of `${total}/${total}`, and `ops/local-osh-seed-fixtures.json` versions the fixture payloads.
- Scope note: this is PARTIAL for REQ-ETS-PART1-010. Positive System CRD lifecycle evidence now exists against local OSH, but deployment/procedure/sampling-feature/property CRUD, system delete cascade, collection propagation, `text/uri-list`, `/conf/update`, PATCH, and Part 2 are still out of scope.

Sprint ets-11 AdvancedFiltering read-only subset:

- Current repo base before implementation: `5cdcdf4`
- Maven verification: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `98 tests / 0 failures / 0 errors / 3 skipped`
- TeamEngine E2E smoke:
  - Clone/copy: `/tmp/sprint-ets-11-generator-smoke`
  - Command: `SMOKE_CONTAINER_NAME=sprint-ets-11-generator-smoke SMOKE_OUTPUT_DIR=/tmp/sprint-ets-11-generator-smoke-results bash scripts/smoke-test.sh`
  - Result: `63 total / 48 passed / 0 failed / 15 skipped`
  - Report: `/tmp/sprint-ets-11-generator-smoke-results/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
  - Log: `/tmp/sprint-ets-11-generator-smoke-results/s-ets-01-03-teamengine-container-2026-05-05.log`
- AdvancedFiltering outcome: 6 AdvancedFiltering @Tests SKIP with reason because current GeoRobotix does not declare `/conf/advanced-filtering`.
- Scope note: this is PARTIAL for REQ-ETS-PART1-009; mutation behavior, Part 2, full cross-resource association filters, full geometry intersection semantics, combined-filter truth tables, and endpoint parity remain open.
- Quinn independent Gate 3.5:
  - Maven command: `bash scripts/mvn-test-via-docker.sh`
  - Maven clone: `/tmp/quinn-sprint-ets-11-gate`
  - Maven result: BUILD SUCCESS, `98 tests / 0 failures / 0 errors / 3 skipped`
  - Maven log: `/tmp/quinn-ets-csapi-mvn-s11.log`
  - Maven note: first worktree invocation exited BUILD FAILURE after report XML totals `98/0/0/3` due surefire fork ClassNotFoundException for `VerifyMaskingRequestLoggingFilter`; the later `/tmp` clone rerun succeeded with persisted log evidence and the same totals.
  - Smoke clone: `/tmp/quinn-sprint-ets-11-gate`
  - Smoke command: `SMOKE_CONTAINER_NAME=quinn-ets-csapi-smoke-s11 SMOKE_OUTPUT_DIR=/tmp/quinn-ets-csapi-smoke-s11-results bash scripts/smoke-test.sh`
  - Smoke result: `63 total / 48 passed / 0 failed / 15 skipped`
  - AdvancedFiltering runtime outcome: 6 SKIP, 0 PASS, 0 FAIL, with missing `/conf/advanced-filtering` reason
  - Report: `/tmp/quinn-ets-csapi-smoke-s11-results/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
  - Gate artifact: `.harness/evaluations/sprint-ets-11-evaluator-gate.yaml`
- Raze independent Gate 4:
  - Maven result: BUILD SUCCESS, `98 tests / 0 failures / 0 errors / 3 skipped`
  - AdvancedFiltering lint evidence: `testAdvancedFilteringGroupDependsOnSystemFeatures`, `testEveryAdvancedFilteringTestMethodCarriesAdvancedFilteringGroup`, and `testAdvancedFilteringCoLocatedWithSystemFeatures` present in `VerifyTestNGSuiteDependency` surefire XML
  - Smoke command: `SMOKE_CONTAINER_NAME=raze-ets-csapi-smoke-s11 SMOKE_OUTPUT_DIR=/tmp/raze-sprint-ets-11-smoke-results bash scripts/smoke-test.sh`
  - Smoke result: `63 total / 48 passed / 0 failed / 15 skipped`
  - AdvancedFiltering runtime outcome: 6 SKIP, 0 PASS, 0 FAIL, with missing `/conf/advanced-filtering` reason
  - Report: `/tmp/raze-sprint-ets-11-smoke-results/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
  - Gate artifact: `.harness/evaluations/sprint-ets-11-adversarial-gate.yaml`

Sprint ets-10 SensorML systems read-only subset:

- Current repo base before implementation: `e7ba5f1`
- Maven verification: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `95 tests / 0 failures / 0 errors / 3 skipped`
- TeamEngine E2E smoke:
  - Clone: `/tmp/sprint-ets-10-generator-smoke-git-r2`
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-sprint10-smoke-results-git-r2 bash scripts/smoke-test.sh`
  - Result: `57 total / 48 passed / 0 failed / 9 skipped`
  - Report: `/tmp/ets-ogcapi-connectedsystems10-sprint10-smoke-results-git-r2/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
  - Log: `/tmp/ets-ogcapi-connectedsystems10-sprint10-smoke-results-git-r2/s-ets-01-03-teamengine-container-2026-05-05.log`
- SensorML outcome: 6 SensorML @Tests PASS. Runtime report records `SensorML representation source: application/sml+json alternate link (https://api.georobotix.io/ogc/t18/api/systems/0mqcvdnfoca0?f=sml3)`.
- Scope note: this is PARTIAL for REQ-ETS-PART1-013; write media type, relation types, non-system schema/mapping, and full SensorML 3.0 JSON Schema validation remain open.
- Raze implementation review: `.harness/evaluations/sprint-ets-10-adversarial-implementation.yaml` found two gaps; both were fixed.
- Raze gap-fix review: `.harness/evaluations/sprint-ets-10-adversarial-gapfix.yaml` APPROVE 0.93, no final blockers.
- Quinn independent gate:
  - Maven clone: `/tmp/quinn-sprint-ets-10`
  - Maven result: BUILD SUCCESS, `95 tests / 0 failures / 0 errors / 3 skipped`
  - SensorML lint evidence: `testSensorMlGroupDependsOnSystemFeatures`, `testEverySensorMlTestMethodCarriesSensorMlGroup`, and `testSensorMlCoLocatedWithSystemFeatures` present in `VerifyTestNGSuiteDependency` surefire XML
  - Smoke command: `SMOKE_CONTAINER_NAME=quinn-ets-csapi-smoke-s10 SMOKE_OUTPUT_DIR=/tmp/quinn-sprint-ets-10-smoke-results bash scripts/smoke-test.sh`
  - Smoke result: `57 total / 48 passed / 0 failed / 9 skipped`
  - Report: `/tmp/quinn-sprint-ets-10-smoke-results/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
  - Gate artifact: `.harness/evaluations/sprint-ets-10-evaluator-gate.yaml`
- Raze independent Gate 4:
  - Maven clone: `/tmp/raze-sprint-ets-10`
  - Maven result: BUILD SUCCESS, `95 tests / 0 failures / 0 errors / 3 skipped`
  - Smoke command: `SMOKE_CONTAINER_NAME=raze-ets-csapi-smoke-s10 SMOKE_OUTPUT_DIR=/tmp/raze-sprint-ets-10-smoke-results bash scripts/smoke-test.sh`
  - Smoke result: `57 total / 48 passed / 0 failed / 9 skipped`
  - SensorML outcome: 6 PASS, 0 failed, 0 skipped
  - Report: `/tmp/raze-sprint-ets-10-smoke-results/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
  - Gate artifact: `.harness/evaluations/sprint-ets-10-adversarial-gate.yaml`

Sprint ets-09 GeoJSON systems read-only subset:

- Current repo HEAD: `880b391`
- Implementation commits: `28f4ddf` and `b4a97de`
- Maven verification: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `92 tests / 0 failures / 0 errors / 3 skipped`
- TeamEngine E2E smoke:
  - Clone: `/tmp/sprint-ets-09-smoke-fix`
  - Command: `SMOKE_OUTPUT_DIR=/tmp/sprint-ets-09-smoke-fix-results bash scripts/smoke-test.sh`
  - Result: `51 total / 42 passed / 0 failed / 9 skipped`
  - Report: `/tmp/sprint-ets-09-smoke-fix-results/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
  - Log: `/tmp/sprint-ets-09-smoke-fix-results/s-ets-01-03-teamengine-container-2026-05-05.log`
- Quinn independent gate:
  - Maven clone: `/tmp/quinn-sprint-ets-09`
  - Maven result: BUILD SUCCESS, `92 tests / 0 failures / 0 errors / 3 skipped`
  - Smoke command: `SMOKE_CONTAINER_NAME=quinn-ets-csapi-smoke-2 SMOKE_OUTPUT_DIR=/tmp/quinn-sprint-ets-09-smoke-results-2 SMOKE_RUN_TIMEOUT_S=1200 bash scripts/smoke-test.sh`
  - Smoke result: `51 total / 42 passed / 0 failed / 9 skipped`
  - Report: `/tmp/quinn-sprint-ets-09-smoke-results-2/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
  - Gate artifact: `.harness/evaluations/sprint-ets-09-evaluator-gate.yaml`
- Raze independent gate:
  - Maven clone: `/tmp/raze-sprint-ets-09-review`
  - Maven result: BUILD SUCCESS, `92 tests / 0 failures / 0 errors / 3 skipped`
  - Smoke command: `SMOKE_OUTPUT_DIR=/tmp/raze-sprint-ets-09-smoke-results bash scripts/smoke-test.sh`
  - Smoke result: `51 total / 42 passed / 0 failed / 9 skipped`
  - Report: `/tmp/raze-sprint-ets-09-smoke-results/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
  - Gate artifact: `.harness/evaluations/sprint-ets-09-adversarial-gate.yaml`

GeoJSON outcome: 2 PASS + 3 SKIP. GeoRobotix declares `/conf/geojson`, but `/systems` with `Accept: application/geo+json` returns `Content-Type: application/json` and top-level `items`; the ETS does not count that as mediatype-read, FeatureCollection, or mapping PASS.

Gate verdicts: Quinn APPROVE_WITH_CONCERNS 0.90 and Raze APPROVE_WITH_CONCERNS 0.88. No blockers or required fixes were found.

## Artifact Location

Persistent ETS evidence lives in this repository under `ops/test-results/`. Recent Sprint 12, Sprint 11, Sprint 10, and Sprint 9 smoke artifacts currently live under `/tmp/...` gate directories, including `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s12-generator-r3/`, `/tmp/sprint-ets-11-generator-smoke-results/`, `/tmp/quinn-sprint-ets-10-smoke-results/`, and `/tmp/raze-sprint-ets-10-smoke-results/`, because the gate runs intentionally avoided polluting the worktree.

## Historical Evidence

Earlier Sprint 1–8 runtime artifacts already present in `ops/test-results/` include TeamEngine smoke XMLs, container logs, sabotage cascade XMLs, bash traces, and surefire output.
