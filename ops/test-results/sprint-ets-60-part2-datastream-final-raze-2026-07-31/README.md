# Sprint ETS-60 Part 2 Datastream Final Raze Smoke

Date: 2026-07-31

Command:

```bash
SMOKE_DOCKER_NETWORK=field-hub_default \
  SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api \
  SMOKE_CONTAINER_NAME=ets-csapi-s60-part2-datastream-final-raze \
  SMOKE_RUN_LABEL=sprint60-datastream-final-raze-20260731 \
  SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-sprint-60-part2-datastream-final-raze-20260731-results \
  bash scripts/smoke-test.sh
```

Result:

- TeamEngine completed against the local OSH IUT.
- TestNG summary: 247 total, 38 passed, 21 failed, 188 skipped.
- The remaining failures are outside the Sprint 60 Part 2 Datastream implementation and match the existing local OSH Part 1/SensorML/collection limitations.
- Part 2 Datastream procedures emitted no failures in this run; they were skipped behind `part2apicommon` dependency skips from the local OSH target.

No-mutation oracle:

- Recognized request log entries: 194.
- Recognized local OSH IUT request log entries: 189.
- HTTP method distribution: GET=194.
- Write methods observed: 0.

Artifacts:

- `s-ets-01-03-teamengine-smoke-2026-07-31.xml`
- `s-ets-01-03-teamengine-container-2026-07-31.log`
