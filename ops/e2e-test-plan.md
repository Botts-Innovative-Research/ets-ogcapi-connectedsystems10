# E2E Test Plan — OGC API Connected Systems ETS

Last updated: 2026-05-22T19:34Z

## Policy

Every user-directed ETS change must be verified end-to-end before reporting done. For this Java/TestNG TeamEngine ETS, E2E means running the deployed TeamEngine suite against a real IUT with real HTTP protocol exchanges.

## Primary E2E Command

Run from a fresh `/tmp` clone when feasible:

```bash
SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results bash scripts/smoke-test.sh
```

The smoke script builds the Docker image, starts TeamEngine, verifies suite registration, runs the ETS against GeoRobotix by default, archives XML/log artifacts, and exits non-zero if TestNG reports failures or TeamEngine startup has registration errors.

## Accepted IUT Targets

- **Default public target**: GeoRobotix remains the default interoperability smoke target for `scripts/smoke-test.sh`.
- **Accepted local target**: A self-run local OSH instance is sufficient E2E evidence for a sprint when it is a real running OGC API Connected Systems server, TeamEngine reaches it over Docker networking, seed state and credentials are documented, XML/log artifacts are archived, and exact totals are recorded.
- **External-target failures**: If the default public target is unhealthy, document the direct probe evidence and treat that run as an advisory external interoperability check, not as a blocker for a sprint whose accepted E2E IUT is local OSH.

Local OSH command shape:

```bash
SMOKE_DOCKER_NETWORK=field-hub_default \
  SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api \
  SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-local-osh-results \
  bash scripts/smoke-test.sh
```

## Required Gates

- Maven unit/lint verification: `bash scripts/mvn-test-via-docker.sh`
- TeamEngine smoke: `scripts/smoke-test.sh` with `SMOKE_OUTPUT_DIR` outside the worktree for gate runs
- Targeted sabotage/credential scripts when the changed surface touches dependency cascade or auth/logging:
  - `scripts/sabotage-test.sh`
  - `scripts/credential-leak-e2e-test.sh`
  - `scripts/credential-leak-integration-test.sh`

## Reporting

Record exact totals, including skipped tests. Do not claim “all tests pass” when output contains skips. Update `ops/test-results.md`, `ops/status.md`, `ops/changelog.md`, and the relevant OpenSpec/story/traceability files before reporting completion.
