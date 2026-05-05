# Test Results — OGC API Connected Systems ETS

Last updated: 2026-05-05T16:15Z

## Current Sprint Evidence

Sprint ets-09 GeoJSON systems read-only subset:

- Sister/current repo HEAD: `b4a97de`
- Maven verification: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `92 tests / 0 failures / 0 errors / 3 skipped`
- TeamEngine E2E smoke:
  - Clone: `/tmp/sprint-ets-09-smoke-fix`
  - Command: `SMOKE_OUTPUT_DIR=/tmp/sprint-ets-09-smoke-fix-results bash scripts/smoke-test.sh`
  - Result: `51 total / 42 passed / 0 failed / 9 skipped`
  - Report: `/tmp/sprint-ets-09-smoke-fix-results/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
  - Log: `/tmp/sprint-ets-09-smoke-fix-results/s-ets-01-03-teamengine-container-2026-05-05.log`

GeoJSON outcome: 2 PASS + 3 SKIP. GeoRobotix declares `/conf/geojson`, but `/systems` with `Accept: application/geo+json` returns `Content-Type: application/json` and top-level `items`; the ETS does not count that as mediatype-read, FeatureCollection, or mapping PASS.

## Artifact Location

Persistent ETS evidence lives in this repository under `ops/test-results/`. Recent Sprint 9 smoke artifacts currently live under `/tmp/sprint-ets-09-smoke-fix-results/` because the gate run intentionally avoided polluting the worktree.

## Historical Evidence

Earlier Sprint 1–8 runtime artifacts already present in `ops/test-results/` include TeamEngine smoke XMLs, container logs, sabotage cascade XMLs, bash traces, and surefire output.
