# Story S-ETS-19-01: Encoding Mediatype-Write Safety-Gated Checks

> Sprint: ets-19
> Status: GENERATOR COMPLETE - RAZE APPROVE_WITH_CONCERNS
> Priority: P0
> Complexity: M
> Epic: epic-ets-02-part1-classes
> OpenSpec: REQ-ETS-PART1-012, REQ-ETS-PART1-013, REQ-ETS-PART1-010

## User Value

As an OGC API Connected Systems server implementer, I need write-side encoding tests to verify that a mutable server can parse GeoJSON and SensorML request bodies, while the ETS never mutates a shared public IUT by accident.

## Scope

Sprint 19 implements safety-gated checks for:

1. GeoJSON `/req/geojson/mediatype-write`, conditioned on Create/Replace/Delete support.
2. SensorML `/req/sensorml/mediatype-write`, conditioned on Create/Replace/Delete support.
3. Default GeoRobotix smoke no-mutation proof.
4. Positive parse evidence only against a dedicated mutable IUT with explicit mutation opt-in.

The sprint remains PARTIAL for both `REQ-ETS-PART1-012` and `REQ-ETS-PART1-013`.

## Requirements

- REQ-ETS-PART1-012
- REQ-ETS-PART1-013
- REQ-ETS-PART1-010
- SCENARIO-ETS-PART1-012-GEOJSON-MEDIATYPE-WRITE-SAFETY-GATED-001
- SCENARIO-ETS-PART1-013-SENSORML-MEDIATYPE-WRITE-SAFETY-GATED-001
- SCENARIO-ETS-PART1-012-013-MEDIATYPE-WRITE-OPTIONS-READINESS-001
- SCENARIO-ETS-PART1-012-013-MEDIATYPE-WRITE-NO-PUBLIC-MUTATION-001
- SCENARIO-ETS-PART1-012-013-MEDIATYPE-WRITE-PARSE-EVIDENCE-001

## Planning Evidence

- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28, checked 2026-05-07, not stale.
- OGC GeoJSON requirement class source includes `/req/geojson/mediatype-write`.
- OGC SensorML requirement class source includes `/req/sensorml/mediatype-write`.
- OGC GeoJSON clause states `application/geo+json` must be supported in `Content-Type` and parsed according to resource type when Create/Replace/Delete is implemented.
- OGC SensorML clause states `application/sml+json` must be supported in `Content-Type` and parsed according to resource type when Create/Replace/Delete is implemented.
- GeoRobotix `/conformance` currently declares `/conf/create-replace-delete`, `/conf/geojson`, and `/conf/sensorml`.
- GeoRobotix `OPTIONS /systems` and `OPTIONS /systems/0mqcvdnfoca0` return `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS`.
- Public GeoRobotix remains a shared IUT. Sprint 19 must not issue POST, PUT, DELETE, or PATCH in default smoke, even though readiness metadata suggests write capability.
- Raze planning review `.harness/evaluations/sprint-ets-19-plan-adversarial.yaml` returned `GAPS_FOUND` confidence 0.88 for a missing SensorML OpenSpec scenario body.
- Raze gap-fix review `.harness/evaluations/sprint-ets-19-plan-gapfix.yaml` returned `APPROVE` confidence 0.95 after adding `SCENARIO-ETS-PART1-013-SENSORML-MEDIATYPE-WRITE-SAFETY-GATED-001`.

## Implemented Test Surface

1. Reuse Sprint 12 mutation opt-in parameters and public-IUT hard denial.
2. Add GeoJSON mediatype-write checks gated on `/conf/geojson` plus `/conf/create-replace-delete`.
3. Add SensorML mediatype-write checks gated on `/conf/sensorml` plus `/conf/create-replace-delete`.
4. Default public-IUT behavior: SKIP lifecycle parse checks before mutation and prove zero IUT-bound mutation requests.
5. Dedicated mutable-IUT behavior: issue POST or PUT with the exact `Content-Type` under test only after explicit opt-in.
6. Require parse evidence by dereferencing `Location` or canonical id after mutation.
7. Treat status-only mutation success as insufficient.
8. FAIL when an explicitly tested mutable IUT declares the relevant conformance classes but rejects the required `Content-Type`.
9. Preserve best-effort cleanup for created resources.

## Generator Implementation Evidence

- `EncodingMediatypeWrite` centralizes mutation opt-in, public GeoRobotix hard denial, exact media types, created-resource URI resolution, and dereference/identity parse evidence.
- `GeoJsonTests.geoJsonMediaTypeWriteParsesSystemBodyWhenMutationEnabled` checks `Content-Type: application/geo+json` only after `/conf/create-replace-delete` and explicit dedicated mutable-IUT opt-in.
- `SensorMlTests.sensorMlMediaTypeWriteParsesSystemBodyWhenMutationEnabled` checks `Content-Type: application/sml+json` with the same safety gate and dereference evidence.
- `EncodingMediatypeWrite` disables RestAssured default charset appending for write checks and emits insertion-ordered GeoJSON/SensorML bodies for OSH-compatible positive runs.
- `VerifyEncodingMediatypeWrite` adds 8 unit regressions for public-IUT denial, conformance gating, exact content types, OSH-compatible GeoJSON body evidence, status-only false PASS prevention, and wrong identity rejection.
- Formatter: Docker Maven `spring-javaformat:apply` BUILD SUCCESS on 2026-05-07.
- Maven: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `144 tests / 0 failures / 0 errors / 3 skipped`; log `ops/test-results/sprint-ets-19-maven-r3-2026-05-07.log`.
- GeoRobotix TeamEngine smoke: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s19-generator-r3 bash scripts/smoke-test.sh` reported `89 total / 55 passed / 0 failed / 34 skipped`; report `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s19-generator-r3/s-ets-01-03-teamengine-smoke-2026-05-07.xml`.
- Smoke no-mutation oracle: recognized 69 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH entries for GeoRobotix.
- Runtime skip evidence: both new mediatype-write lifecycle tests SKIP before mutation with messages requiring `mutation-tests-enabled=true` and `mutation-iut-policy=dedicated-mutable-iut`.
- Local OSH mutable-IUT evidence: authenticated TeamEngine smoke r3 against `field-hub-osh-1` reported `89 total / 52 passed / 4 failed / 33 skipped`; both Sprint 19 mediatype-write tests PASSed with exact `application/geo+json` and `application/sml+json`, follow-up GET, and cleanup DELETE request-log evidence. The four failures are SensorML deployment/procedure HTTP 500 responses outside this story.

## Definition of Done

- [x] OpenSpec, story, contract, traceability, planner handoff, status, changelog, known issues, and test-results are reconciled for Sprint 19 planning.
- [x] Raze reviews Sprint 19 planning changes.
- [x] Planning keeps both REQ-ETS-PART1-012 and REQ-ETS-PART1-013 PARTIAL.
- [x] Planning reuses existing mutation safety gates and public-IUT hard denial.
- [x] Planning excludes default public-IUT mutation, Part 2, and full external schema validation.
- [x] Generator implements the safety-gated runtime assertions and helper regressions.
- [x] Formatter, Maven, and TeamEngine smoke gates ran with zero failures.
- [x] Default GeoRobotix smoke produced zero IUT-bound mutation requests.
- [x] Dedicated mutable local OSH run produced positive system-resource GeoJSON and SensorML mediatype-write PASS evidence.
- [x] Raze reviews Sprint 19 implementation changes (`APPROVE_WITH_CONCERNS` 0.90, no required fixes).
- [x] Raze follow-up gapfix approves r3 reconciliation (`APPROVE` 0.94).

## Out Of Scope

- Default mutation against GeoRobotix
- Ungated POST/PUT/DELETE/PATCH
- Part 2 mediatype-write checks
- Full external GeoJSON or SensorML JSON Schema validation
- Closing full GeoJSON or SensorML conformance
