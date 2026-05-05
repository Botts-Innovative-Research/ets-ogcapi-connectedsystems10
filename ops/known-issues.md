# Known Issues — OGC API Connected Systems ETS

Last updated: 2026-05-05T19:32Z

## Active Issues

- Sprint ets-09 is PARTIAL-IMPLEMENTED only. Full GeoJSON remains open for `mediatype-write`, `relation-types`, deployment/procedure/sampling-feature GeoJSON schema and mappings, and future stricter schema validation.
- Sprint ets-10 Generator implementation is PARTIAL SensorML only. Full SensorML remains open for `mediatype-write`, `relation-types`, deployment/procedure/property SensorML schema and mappings, and full SensorML 3.0 JSON Schema validation.
- Sprint ets-11 planning targets a PARTIAL AdvancedFiltering subset only. GeoRobotix does not currently declare `/conf/advanced-filtering`, so default smoke should SKIP-with-reason rather than PASS from undeclared query behavior.
- GeoRobotix currently declares `/conf/geojson`, but `/systems` with `Accept: application/geo+json` returns `Content-Type: application/json` and a CS API `items` wrapper. Current ETS behavior is SKIP-with-reason for GeoJSON mediatype-read, FeatureCollection, and feature-mapping assertions.
- Sprint 9 non-blocking gate concerns remain as cleanup candidates: smoke log archival can lose the container log when Docker cleanup races `docker logs`, and future default-JSON GeoJSON FeatureCollection fallback PASS branches need clearer runtime reporting.

## Worktree Hygiene

Worktree was clean at Sprint 10 planning start. The earlier untracked `*:Zone.Identifier` files were removed, and the six script executable bits were restored.

## Verification Caveats

- Host Maven is not assumed to exist in WSL2. Use `scripts/mvn-test-via-docker.sh`.
- Gate smoke runs should use `/tmp` clones and `SMOKE_OUTPUT_DIR=/tmp/...` to avoid worktree pollution.
- Do not report skipped tests as pass. Always report totals including skipped counts.
