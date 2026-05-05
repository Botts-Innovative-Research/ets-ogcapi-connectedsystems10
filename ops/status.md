# Operational Status — OGC API Connected Systems ETS

Last updated: 2026-05-05T17:41Z

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
- `.harness/handoffs/planner-handoff.yaml`
- `.harness/contracts/sprint-ets-10.yaml`

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

- ETS HEAD at Sprint 10 planning start: `8af9f70`
- Latest csapi docs handoff commit before migration: `1568f36`
- Latest implemented story: `S-ETS-09-01`
- Current sprint status: Sprint ets-10 PLANNED; SensorML systems read-only subset selected

## Sprint ets-09 Evidence

GeoJSON systems read-only subset:

- `GeoJsonTests.java` added with 5 read-only @Tests
- `testng.xml` wires `<group name="geojson" depends-on="systemfeatures"/>`
- VerifyTestNGSuiteDependency adds 3 GeoJSON lint tests
- Full REQ-ETS-PART1-012 remains open for `mediatype-write`, `relation-types`, and non-system GeoJSON schema/mapping subrequirements

Verification:

- Generator: `bash scripts/mvn-test-via-docker.sh` — BUILD SUCCESS, `92 tests / 0 failures / 0 errors / 3 skipped`
- Generator TeamEngine smoke from `/tmp/sprint-ets-09-smoke-fix` — `51 total / 42 passed / 0 failed / 9 skipped`
- Quinn independent Maven from `/tmp/quinn-sprint-ets-09` — BUILD SUCCESS, `92 tests / 0 failures / 0 errors / 3 skipped`
- Quinn independent TeamEngine smoke with unique container name — `51 total / 42 passed / 0 failed / 9 skipped`
- Raze independent Maven from `/tmp/raze-sprint-ets-09-review` — BUILD SUCCESS, `92 tests / 0 failures / 0 errors / 3 skipped`
- Raze independent TeamEngine smoke — `51 total / 42 passed / 0 failed / 9 skipped`
- GeoJSON runtime — 2 PASS + 3 SKIP; current GeoRobotix `items` JSON is not counted as GeoJSON PASS

Gate Results:

- `.harness/evaluations/sprint-ets-09-adversarial-implementation.yaml` — GAPS_FOUND 0.86 on mediatype-read overclaim
- `.harness/evaluations/sprint-ets-09-adversarial-gapfix.yaml` — APPROVE 0.94 after `b4a97de`
- `.harness/evaluations/sprint-ets-09-evaluator-gate.yaml` — Quinn APPROVE_WITH_CONCERNS 0.90; no blockers
- `.harness/evaluations/sprint-ets-09-adversarial-gate.yaml` — Raze APPROVE_WITH_CONCERNS 0.88; no required fixes

## Next Action

1. Run Generator for Sprint ets-10 story `S-ETS-10-01`.
2. Implement `SensorMlTests.java` as a PARTIAL read-only subset for REQ-ETS-PART1-013: conformance declaration, system SensorML representation discovery, media-type read or alternate-link fallback, minimal system shape, identity mapping sanity check, and dependency tracer.
3. Add TestNG wiring and 3 SensorML dependency lint tests.
4. Verify with `bash scripts/mvn-test-via-docker.sh` and TeamEngine smoke from a /tmp clone with `SMOKE_OUTPUT_DIR` outside the worktree.
5. Keep all SensorML write-side, relation-type, non-system schema/mapping, full SensorML 3.0 schema validation, AdvancedFiltering query/filtering, create-replace-delete, Update, and Part 2 work out of Sprint 10.

## Dirty Worktree Notes

The repo already had unrelated modified scripts before this migration:

- `scripts/credential-leak-e2e-test.sh`
- `scripts/credential-leak-integration-test.sh`
- `scripts/mvn-test-via-docker.sh`
- `scripts/sabotage-test.sh`
- `scripts/smoke-test.sh`
- `scripts/stub-iut.sh`

The previously untracked `*:Zone.Identifier` files were removed on 2026-05-05 after explicit user instruction. Worktree was clean at Sprint 10 planning start.
