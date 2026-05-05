# E2E Test Plan — OGC API Connected Systems ETS

Last updated: 2026-05-05T16:15Z

## Policy

Every user-directed ETS change must be verified end-to-end before reporting done. For this Java/TestNG TeamEngine ETS, E2E means running the deployed TeamEngine suite against a real IUT with real HTTP protocol exchanges.

## Primary E2E Command

Run from a fresh `/tmp` clone when feasible:

```bash
SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results bash scripts/smoke-test.sh
```

The smoke script builds the Docker image, starts TeamEngine, verifies suite registration, runs the ETS against GeoRobotix by default, archives XML/log artifacts, and exits non-zero if TestNG reports failures or TeamEngine startup has registration errors.

## Required Gates

- Maven unit/lint verification: `bash scripts/mvn-test-via-docker.sh`
- TeamEngine smoke: `scripts/smoke-test.sh` with `SMOKE_OUTPUT_DIR` outside the worktree for gate runs
- Targeted sabotage/credential scripts when the changed surface touches dependency cascade or auth/logging:
  - `scripts/sabotage-test.sh`
  - `scripts/credential-leak-e2e-test.sh`
  - `scripts/credential-leak-integration-test.sh`

## Reporting

Record exact totals, including skipped tests. Do not claim “all tests pass” when output contains skips. Update `ops/test-results.md`, `ops/status.md`, `ops/changelog.md`, and the relevant OpenSpec/story/traceability files before reporting completion.
