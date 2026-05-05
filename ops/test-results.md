# Test Results — OGC API Connected Systems ETS

Last updated: 2026-05-05T20:28Z

## Current Sprint Evidence

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

Persistent ETS evidence lives in this repository under `ops/test-results/`. Recent Sprint 11, Sprint 10, and Sprint 9 smoke artifacts currently live under `/tmp/...` gate directories, including `/tmp/sprint-ets-11-generator-smoke-results/`, `/tmp/quinn-sprint-ets-10-smoke-results/`, and `/tmp/raze-sprint-ets-10-smoke-results/`, because the gate runs intentionally avoided polluting the worktree.

## Historical Evidence

Earlier Sprint 1–8 runtime artifacts already present in `ops/test-results/` include TeamEngine smoke XMLs, container logs, sabotage cascade XMLs, bash traces, and surefire output.
