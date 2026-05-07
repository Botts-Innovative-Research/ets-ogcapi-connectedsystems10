# Known Issues — OGC API Connected Systems ETS

Last updated: 2026-05-07T18:22Z

## Active Issues

- GeoJSON is PARTIAL-IMPLEMENTED only. Sprint 19 adds safety-gated `mediatype-write` coverage with positive system-resource evidence against a local OSH mutable IUT, but full GeoJSON remains open for positive relation-types evidence where non-system resources expose links-member associations, property GeoJSON mapping, non-system mutation-side encoding coverage, and future stricter schema validation.
- SensorML is PARTIAL-IMPLEMENTED only. Sprint 19 adds safety-gated `mediatype-write` coverage with positive system-resource evidence against a local OSH mutable IUT. Current GeoRobotix SensorML bodies have no top-level `links` member, and local OSH deployment/procedure SensorML reads returned HTTP 500, so relation-types/non-system outcomes remain limited. Full SensorML remains open for broader positive relation-types evidence, full SensorML 3.0 JSON Schema validation, non-system mutation-side behavior, and positive property evidence against a populated IUT.
- Part 2 placeholder taxonomy is partly cleaned after Sprint 20. OpenSpec and epic ETS-03 now treat API Common as `REQ-ETS-PART2-001` and the remaining placeholders as `REQ-ETS-PART2-002..014`; older high-level PRD/brief language still says 14 total Part 2 classes, which is correct as total scope but should be read as 1 partially implemented plus 13 remaining.
- Sprint ets-11 Generator implementation is PARTIAL AdvancedFiltering only. GeoRobotix does not currently declare `/conf/advanced-filtering`, so default smoke SKIPs with reason rather than PASS from undeclared query behavior. Full AdvancedFiltering remains open for association filters, full geometry semantics, combined filters, endpoint parity, and any Part 2 query requirements.
- Sprint ets-12 is mutation-safety constrained. GeoRobotix declares `/conf/create-replace-delete` and advertises POST/PUT/DELETE via OPTIONS, but default smoke MUST NOT mutate the public IUT. OPTIONS evidence is readiness only, not lifecycle conformance. Local OSH now proves the guarded System POST/PUT/DELETE lifecycle path, but full Create/Replace/Delete remains PARTIAL until the non-system CRUD and cascade requirements are implemented.
- Local OSH is now a seeded mutable full-smoke health target for the current ETS surface: `proxyBaseUrl` points at `http://field-hub-osh-1:8081`, synthetic system/procedure/deployment/samplingFeature resources exist with payloads versioned in `ops/local-osh-seed-fixtures.json`, and `/tmp/ets-csapi-osh-full-health-r3` reported `69 total / 50 passed / 0 failed / 19 skipped`. It is still not evidence for out-of-scope CRD subrequirements such as non-system CRUD, cascade behavior, `text/uri-list`, or `/conf/update`.
- GeoRobotix currently declares `/conf/geojson`, but `/systems` with `Accept: application/geo+json` returns `Content-Type: application/json` and a CS API `items` wrapper. Current ETS behavior is SKIP-with-reason for GeoJSON mediatype-read, FeatureCollection, and feature-mapping assertions.
- Sprint 9 non-blocking gate concerns remain as cleanup candidates: smoke log archival can lose the container log when Docker cleanup races `docker logs`, and future default-JSON GeoJSON FeatureCollection fallback PASS branches need clearer runtime reporting.

## Worktree Hygiene

Worktree was clean at Sprint 10 planning start. The earlier untracked `*:Zone.Identifier` files were removed, and the six script executable bits were restored.

## Verification Caveats

- Host Maven is not assumed to exist in WSL2. Use `scripts/mvn-test-via-docker.sh`.
- Gate smoke runs should use `/tmp` clones and `SMOKE_OUTPUT_DIR=/tmp/...` to avoid worktree pollution.
- Do not report skipped tests as pass. Always report totals including skipped counts.
