# Session Handoff — ETS Repo Migration

Date: 2026-05-05T16:15Z
New working directory: `/home/nh/docker/gir/ets-ogcapi-connectedsystems10`

## Why This Exists

The active work has moved from the frozen `csapi_compliance` web app repo to the Java/TestNG ETS repo. Future sessions should start here so code, specs, harness artifacts, evidence, and commits live in one repository.

## Current State

- ETS HEAD: `b4a97de` (`Tighten GeoJSON mediatype fallback`)
- Last csapi handoff/docs commit before migration: `1568f36`
- Sprint ets-09 status: PARTIAL-IMPLEMENTED, pending independent Quinn + Raze gate run
- Latest verification:
  - `bash scripts/mvn-test-via-docker.sh` — BUILD SUCCESS, `92/0/0/3`
  - `/tmp/sprint-ets-09-smoke-fix` smoke — `51 total / 42 passed / 0 failed / 9 skipped`
  - GeoJSON runtime: 2 PASS + 3 SKIP; CS API `items` is not counted as GeoJSON PASS

## Migrated Context

- `.harness/`
- `openspec/`
- `_bmad/`
- `epics/`
- `ops/status.md`
- `ops/changelog.md`
- `ops/metrics.md`
- `ops/known-issues.md`
- `ops/e2e-test-plan.md`
- `ops/test-results.md`
- `AGENTS.md`
- `scripts/orchestrate.py`
- `scripts/session-metrics.py`

Existing ETS runtime artifacts in `ops/test-results/` and `ops/server.md` were preserved.

## Known Dirty State

Before migration, this repo already had unrelated modified scripts:

- `scripts/credential-leak-e2e-test.sh`
- `scripts/credential-leak-integration-test.sh`
- `scripts/mvn-test-via-docker.sh`
- `scripts/sabotage-test.sh`
- `scripts/smoke-test.sh`
- `scripts/stub-iut.sh`

It also has many `*:Zone.Identifier` files. Do not treat those as migration edits unless explicitly asked.

## Recommended Next Step

1. Commit this migration in the ETS repo, staging only migrated context files.
2. Start the next session in `/home/nh/docker/gir/ets-ogcapi-connectedsystems10`.
3. Run Quinn + Raze Sprint 9 gates from this repo using the migrated `.harness/`, `_bmad/`, `openspec/`, and `epics/` context.
