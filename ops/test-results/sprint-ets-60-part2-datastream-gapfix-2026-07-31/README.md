# Sprint 60 Part 2 Datastream Gap-Fix E2E Evidence

- Date: 2026-07-31
- Command:

```bash
SMOKE_DOCKER_NETWORK=field-hub_default \
  SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api \
  SMOKE_CONTAINER_NAME=ets-csapi-s60-part2-datastream-gapfix \
  SMOKE_RUN_LABEL=sprint60-datastream-gapfix-20260731 \
  SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-sprint-60-part2-datastream-gapfix-20260731-results \
  bash scripts/smoke-test.sh
```

- Result: non-green local OSH baseline; TestNG total=247, passed=38, failed=21, skipped=188.
- Interpretation: Part 2 Datastream tests were skipped because the local OSH IUT does not declare the required Part 2 API Common prerequisite. This preserves Sprint 60 implementation verification but does not certify local OSH Part 2 conformance.
- No-mutation oracle: container log contains 189 recognized IUT request log lines and method count GET=194; no POST, PUT, PATCH, or DELETE requests were observed.
- Artifacts:
  - `s-ets-01-03-teamengine-smoke-2026-07-31.xml`
  - `s-ets-01-03-teamengine-container-2026-07-31.log`
