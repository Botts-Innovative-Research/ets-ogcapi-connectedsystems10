# E2E Test Plan — OGC API Connected Systems ETS

Last updated: 2026-06-01T23:16Z

## Policy

Every user-directed ETS change must be verified end-to-end before reporting done. For this Java/TestNG TeamEngine ETS, E2E means running the deployed TeamEngine suite against a real IUT with real HTTP protocol exchanges.

## Primary E2E Command

Run from a fresh `/tmp` clone when feasible. The primary development IUT is the
self-provisioned local OSH instance on the `field-hub_default` Docker network:

```bash
SMOKE_DOCKER_NETWORK=field-hub_default \
  SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api \
  SMOKE_AUTH_CREDENTIAL="$SMOKE_AUTH_CREDENTIAL" \
  SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-local-osh-results \
  bash scripts/smoke-test.sh
```

The smoke script builds the Docker image, starts TeamEngine, verifies suite registration, runs the ETS against the configured IUT, archives XML/log artifacts, and exits non-zero if TestNG reports failures or TeamEngine startup has registration errors.

## Accepted IUT Targets

- **Primary development target**: A self-run local OSH instance is the default sprint E2E IUT when it is a real running OGC API Connected Systems server, TeamEngine reaches it over Docker networking, seed state and credentials handling are documented, XML/log artifacts are archived, and exact totals are recorded.
- **GeoRobotix public instance**: GeoRobotix is no longer a default or required development target. It may be run only as an explicit advisory interoperability probe when useful, and failures must not block local-OSH-backed development work.
- **Credential handling**: Do not record credential values. Supply local OSH credentials through the environment, typically `SMOKE_AUTH_CREDENTIAL="Basic <base64>"`, derived from the local stack config or a secret store.
- **Seed-state requirement**: For Part 2 dynamic-data work, record whether local OSH has candidate DataStreams, Observations, ControlStreams, Commands, CommandStatus, CommandResult, and SystemEvents. Empty collections are acceptable planning evidence but cannot be counted as positive conformance closure.

Local OSH command shape:

```bash
SMOKE_DOCKER_NETWORK=field-hub_default \
  SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api \
  SMOKE_AUTH_CREDENTIAL="$SMOKE_AUTH_CREDENTIAL" \
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
