# Operational Status — OGC API Connected Systems ETS

Last updated: 2026-05-05T16:15Z

## Fresh-Session Entry Point

Start future sessions in:

```bash
cd /home/nh/docker/gir/ets-ogcapi-connectedsystems10
```

Read these first:

- `AGENTS.md`
- `ops/SESSION-HANDOFF-2026-05-05-ETS-REPO-MIGRATION.md`
- `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- `_bmad/traceability.md`
- `.harness/handoffs/generator-handoff.yaml`
- `.harness/contracts/sprint-ets-09.yaml`

## Current State

The active project has moved from the frozen `csapi_compliance` web app repo into this Java/TestNG TeamEngine ETS repo.

Migrated context now lives here:

- `.harness/`
- `openspec/`
- `_bmad/`
- `epics/`
- selected `ops/*.md`
- `AGENTS.md`
- `scripts/orchestrate.py`
- `scripts/session-metrics.py`

Existing ETS evidence in `ops/test-results/` and `ops/server.md` was preserved.

## Current Code State

- ETS HEAD: `b4a97de`
- Latest csapi docs handoff commit before migration: `1568f36`
- Latest implemented story: `S-ETS-09-01`
- Current sprint status: Sprint ets-09 PARTIAL-IMPLEMENTED, pending full Quinn + Raze gate pair

## Sprint ets-09 Evidence

GeoJSON systems read-only subset:

- `GeoJsonTests.java` added with 5 read-only @Tests
- `testng.xml` wires `<group name="geojson" depends-on="systemfeatures"/>`
- VerifyTestNGSuiteDependency adds 3 GeoJSON lint tests
- Full REQ-ETS-PART1-012 remains open for `mediatype-write`, `relation-types`, and non-system GeoJSON schema/mapping subrequirements

Verification:

- `bash scripts/mvn-test-via-docker.sh` — BUILD SUCCESS, `92 tests / 0 failures / 0 errors / 3 skipped`
- TeamEngine smoke from `/tmp/sprint-ets-09-smoke-fix` — `51 total / 42 passed / 0 failed / 9 skipped`
- GeoJSON runtime — 2 PASS + 3 SKIP; current GeoRobotix `items` JSON is not counted as GeoJSON PASS

Raze:

- `.harness/evaluations/sprint-ets-09-adversarial-implementation.yaml` — GAPS_FOUND 0.86 on mediatype-read overclaim
- `.harness/evaluations/sprint-ets-09-adversarial-gapfix.yaml` — APPROVE 0.94 after `b4a97de`

## Next Action

1. Commit this migration in the ETS repo, staging only migrated context files.
2. Restart the session in `/home/nh/docker/gir/ets-ogcapi-connectedsystems10`.
3. Run independent Quinn + Raze Sprint 9 gates from this repo.

## Dirty Worktree Notes

The repo already had unrelated modified scripts before this migration:

- `scripts/credential-leak-e2e-test.sh`
- `scripts/credential-leak-integration-test.sh`
- `scripts/mvn-test-via-docker.sh`
- `scripts/sabotage-test.sh`
- `scripts/smoke-test.sh`
- `scripts/stub-iut.sh`

There are also many `*:Zone.Identifier` files. Do not stage or revert them unless explicitly requested.
