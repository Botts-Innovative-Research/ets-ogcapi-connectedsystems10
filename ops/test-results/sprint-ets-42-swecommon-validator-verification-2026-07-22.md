# Sprint ETS-42 SWE Common Validator Verification

Date: 2026-07-22

## Scope

Verification for S-ETS-42-02, the provisional source-pinned
`swecommon30-validator` adapter and dual-validation integration.

## Results

- Formatter: PASS, Dockerized `spring-javaformat:apply` BUILD SUCCESS.
- Source bootstrap: PASS. The script fetched and verified
  `3ba75ceabe57cea85f4a8513c59e0f90e386ba96`, then built only the upstream
  parent and `swecommon30-validator` module.
- Focused Maven: PASS, 19 tests / 0 failures / 0 errors / 0 skipped for the
  adapter, shared Part 2 schema helper, and packaging boundary.
- The focused parity corpus passes complete Observation and Command wrappers for
  JSON, Text, and Binary through local wrapper validation and reusable
  `recordSchema` validation. It also verifies active requirement-URI diagnostics
  and operational-error propagation.
- Full Docker Maven: PASS, 311 tests / 0 failures / 0 errors / 3 skipped.
- Exact E2E Docker image: PASS, `ets-ogcapi-connectedsystems10:smoke`, image ID
  `sha256:5e0a557ade09d87a8372dcec9a4bbc867ee91dbded54d269235ba2683e932424`.
- TeamEngine 6 runtime verifier: PASS. The final image preserves the pinned
  base, embeds the upstream validator class and `sweCommon.json` resources in
  the slim shaded ETS jar, relocates NetworkNT, excludes the upstream main-tree
  test class, adds no standalone validator/runtime-family jar, and leaves
  TeamEngine-owned files unchanged. The verifier executed valid and invalid
  adapter calls on the final image classpath, proving shaded `jar:` schema
  resolution and relocated NetworkNT execution.
- Mandatory primary local OSH smoke: PASS from a fresh `/tmp` clone synchronized
  to the final worktree, `211 total / 69 passed / 0 failed / 142 skipped`.
  The no-mutation oracle recognized 135 IUT request logs and found zero
  POST/PUT/PATCH/DELETE requests; startup scanning found zero errors.
- Local target: OpenSensorHub 2.0.1 build `4c87a65`, container
  `field-hub-osh-1` on `field-hub_default`, with no-secret configuration
  `ops/local-osh-gate-config.json` and static fixtures from
  `ops/local-osh-seed-fixtures.json`.
- Advisory GeoRobotix deployed run: FAIL as expected for the public IUT,
  211 total / 38 passed / 34 failed / 139 skipped. These totals exactly match
  the Sprint 41 baseline, so the adapter introduced no observed PASS/SKIP drift.
- Advisory runtime diagnostics: no `NoClassDefFoundError`,
  `ClassNotFoundException`, `NoSuchMethodError`, `LinkageError`, adapter
  operational error, or reusable-validator diagnostic appeared in the report
  or container log. Because those advisory schema paths failed before adapter
  invocation, this is regression evidence only; the final-image execution probe
  is the positive adapter runtime evidence.
- Advisory no-mutation oracle: PASS, `recognized_iut_request_logs=272`, with no
  IUT-bound POST, PUT, PATCH, or DELETE requests.

## Commands

```bash
bash scripts/mvn-test-via-docker.sh \
  -Dtest=VerifyConnectedSystemsSweValidatorAdapter,VerifyPart2SchemaValidation,VerifyTeamEngine6Packaging

bash scripts/mvn-test-via-docker.sh

TEAMENGINE_FINAL_IMAGE=ets-ogcapi-connectedsystems10:smoke \
  bash scripts/verify-teamengine6-runtime.sh

SMOKE_DOCKER_NETWORK=field-hub_default \
SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api \
SMOKE_OUTPUT_DIR=/tmp/ets-s42-local-osh-final.KmCuZNuf/results-r4 \
  bash scripts/smoke-test.sh

SMOKE_TARGET=georobotix \
SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-s42-e2e.g0HK46Lh/georobotix-results \
  bash scripts/smoke-test.sh
```

## Raw Artifacts

- `ops/test-results/sprint-ets-42-swecommon-formatter-2026-07-22.txt`
- `ops/test-results/sprint-ets-42-swecommon-bootstrap-2026-07-22.txt`
- `ops/test-results/sprint-ets-42-swecommon-focused-maven-2026-07-22.txt`
- `ops/test-results/sprint-ets-42-swecommon-full-maven-2026-07-22.txt`
- `ops/test-results/sprint-ets-42-swecommon-docker-build-2026-07-22.txt`
- `ops/test-results/sprint-ets-42-swecommon-final-image-provenance-2026-07-22.txt`
- `ops/test-results/sprint-ets-42-swecommon-runtime-verifier-2026-07-22.txt`
- `ops/test-results/sprint-ets-42-swecommon-runtime-verifier-final-2026-07-22.txt`
- `ops/test-results/sprint-ets-42-swecommon-local-osh-e2e-final-2026-07-22.txt`
- `ops/test-results/sprint-ets-42-swecommon-local-osh-final-2026-07-22.xml`
- `ops/test-results/sprint-ets-42-swecommon-local-osh-final-container-2026-07-22.log`
- `ops/test-results/sprint-ets-42-swecommon-local-osh-final-no-mutation-2026-07-22.txt`
- `ops/test-results/sprint-ets-42-swecommon-advisory-georobotix-smoke-2026-07-22.xml`
- `ops/test-results/sprint-ets-42-swecommon-advisory-georobotix-container-2026-07-22.log`

## Gate Status

Maven, six-wrapper parity, packaging, exact-image adapter execution, deployed
registration, primary local OSH execution, and no-mutation evidence are
verified. The earlier Raze recheck returned `PASS_WITH_EXTERNAL_BLOCKER`,
confidence `0.99`; its sole external blocker is now cleared. S-ETS-42-02 is
complete under `SCENARIO-ETS-VALIDATOR-E2E-GATE-001`.
