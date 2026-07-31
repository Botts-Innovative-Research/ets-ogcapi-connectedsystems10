# Sprint 60 Part 2 Datastream E2E Evidence

Command:

```bash
SMOKE_DOCKER_NETWORK=field-hub_default \
  SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api \
  SMOKE_CONTAINER_NAME=ets-csapi-s60-part2-datastream \
  SMOKE_RUN_LABEL=sprint60-datastream-20260731 \
  SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-sprint-60-part2-datastream-20260731-results \
  bash scripts/smoke-test.sh
```

Result:

- Docker smoke exited nonzero honestly.
- TeamEngine XML summary: `247 total / 38 passed / 21 failed / 188 skipped`.
- Datastream setup and all fourteen Datastream procedures skipped because the
  unmodified local OSH IUT does not declare the Part 2 `/conf/api-common`
  prerequisite.
- No-mutation oracle:
  `recognized_iut_request_logs=189`.
- Strict request-line method count: `GET=194`, zero POST/PUT/PATCH/DELETE.

Artifacts:

- `s-ets-01-03-teamengine-container-2026-07-31.log`
- `s-ets-01-03-teamengine-smoke-2026-07-31.xml`
