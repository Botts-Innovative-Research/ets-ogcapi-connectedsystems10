# Sprint ETS-60 Part 2 Datastream FOI Fix E2E Evidence

Date: 2026-07-31

Command:

```bash
SMOKE_DOCKER_NETWORK=field-hub_default \
  SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api \
  SMOKE_CONTAINER_NAME=ets-csapi-s60-part2-datastream-foi-fix \
  SMOKE_RUN_LABEL=sprint60-datastream-foi-fix-20260731 \
  SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-sprint-60-part2-datastream-foi-fix-20260731-results \
  bash scripts/smoke-test.sh
```

Result:

- TestNG report: `total=247 passed=38 failed=21 skipped=188`
- The run is non-green because the local OSH IUT still lacks several conformance classes/endpoints required by the full ETS gate.
- No-mutation oracle over the TeamEngine container log: `recognized_iut_request_logs=189`, `recognized_request_logs=194`, `GET=194`, `writes=0`.
- Archived artifacts:
  - `s-ets-01-03-teamengine-smoke-2026-07-31.xml`
  - `s-ets-01-03-teamengine-container-2026-07-31.log`
