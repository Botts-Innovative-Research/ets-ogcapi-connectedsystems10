# Test Results — OGC API Connected Systems ETS

Last updated: 2026-05-06T15:03Z

## Current Sprint Evidence

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
