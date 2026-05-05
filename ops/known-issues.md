# Known Issues — OGC API Connected Systems ETS

Last updated: 2026-05-05T16:15Z

## Active Issues

- Sprint ets-09 is PARTIAL-IMPLEMENTED only. Full GeoJSON remains open for `mediatype-write`, `relation-types`, deployment/procedure/sampling-feature GeoJSON schema and mappings, and future stricter schema validation.
- GeoRobotix currently declares `/conf/geojson`, but `/systems` with `Accept: application/geo+json` returns `Content-Type: application/json` and a CS API `items` wrapper. Current ETS behavior is SKIP-with-reason for GeoJSON mediatype-read, FeatureCollection, and feature-mapping assertions.
- Quinn + Raze Sprint 9 gates have not yet run as a full independent gate pair after the Generator implementation. Raze implementation/gap-fix reviews were run and the gap-fix review approved.

## Worktree Hygiene

Before the session migration, this repo already had unrelated modified scripts and many `*:Zone.Identifier` files. Do not revert or stage them unless explicitly requested.

Pre-existing modified scripts:

- `scripts/credential-leak-e2e-test.sh`
- `scripts/credential-leak-integration-test.sh`
- `scripts/mvn-test-via-docker.sh`
- `scripts/sabotage-test.sh`
- `scripts/smoke-test.sh`
- `scripts/stub-iut.sh`

## Verification Caveats

- Host Maven is not assumed to exist in WSL2. Use `scripts/mvn-test-via-docker.sh`.
- Gate smoke runs should use `/tmp` clones and `SMOKE_OUTPUT_DIR=/tmp/...` to avoid worktree pollution.
- Do not report skipped tests as pass. Always report totals including skipped counts.
