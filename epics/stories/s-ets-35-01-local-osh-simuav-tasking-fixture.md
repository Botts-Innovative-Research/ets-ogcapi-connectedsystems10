# S-ETS-35-01 — Local OSH SimUAV Tasking Fixture Probe

Status: VERIFIED_WITH_SCHEMA_BLOCKER

## User Instruction

"Do it" after the `osh-addons` driver scan, interpreted as configuring the best candidate driver to provide a local tasking fixture with dynamic streams and inspectable schema evidence.

## Scope

- Build OpenSensorHub `osh-addons` `sensorhub-driver-simuav`.
- Configure field-hub local OSH with SimUAV as an isolated tasking fixture.
- Verify that SimUAV exposes dynamic DataStreams, ControlStreams, schema bodies, Observations, accepted Commands, terminal CommandStatus, and inline CommandResult evidence.
- Run TeamEngine against the populated fixture and record exact totals honestly.
- Restore the primary local OSH smoke state after the isolated fixture run.

## Requirement Traceability

- `REQ-ETS-PART2-013`
- `REQ-ETS-TEAMENGINE-006`
- `SCENARIO-ETS-PART2-013-SIMUAV-TASKING-FIXTURE-001`
- `SCENARIO-ETS-PART2-013-TASKING-DRIVER-FIXTURE-001`
- `SCENARIO-ETS-PART2-013-COMMAND-PARENT-SCHEMA-001`
- `SCENARIO-ETS-PART2-013-POSITIVE-LOCAL-OSH-CLOSURE-001`

## Implementation Evidence

- Built `sensorhub-driver-simuav-1.0.0-bundle.jar` from `/tmp/osh-addons-scan` with Gradle task `:sensorhub-driver-simuav:osgi`, using local `/home/nh/docker/gir/osh-core`.
- Added `sensorhub-driver-simuav-1.0.0-bundle.jar` and `sensorhub-datamodel-uxs-1.0.0.jar` to `/home/nh/docker/gir/sar-ops/field-hub/osh/lib`.
- Configured field-hub OSH module `simuav-driver` with `moduleClass=org.sensorhub.impl.sensor.simuav.SimUavDriver`.
- Rebuilt/recreated only the field-hub `osh` service for the isolated probe.

## Probe Evidence

- Artifact: `ops/test-results/sprint-ets-35-local-osh-simuav-command-e2e-2026-06-02.json`.
- SimUAV system id: `02kargmsuc2g`.
- DataStreams: `03la3nu3m47g` (`platform_pos`) and `026a5nuan1s0` (`platform_state`).
- ControlStreams: `03jtrf5qmkng` (`mission_planning`), `03lbn3mgpspg` (`vehicle_control`), and `02481pm3g53g` (`waypoint_feasibility`).
- Command: `02481pm3g53g1m6uvj80cc85rppg`.
- Terminal status: `COMPLETED`.
- Inline result data: `reachable=true`, `time_to_waypoint=493292.0`, `battery_remaining=59.0`.

## E2E Verification

- SimUAV-populated TeamEngine smoke: FAIL, `211 total / 84 passed / 28 failed / 99 skipped`.
- Populated-smoke artifacts:
  - `ops/test-results/sprint-ets-35-simuav-local-osh-smoke-failed-2026-06-02.xml`
  - `ops/test-results/sprint-ets-35-simuav-local-osh-container-failed-2026-06-02.log`
  - `ops/test-results/sprint-ets-35-simuav-local-osh-smoke-failed-no-mutation-2026-06-02.txt`
- Populated-smoke no-mutation evidence: `GET=150`, `OPTIONS=14`, writes `0`.
- Clean primary local OSH smoke after cleanup: PASS, `211 total / 68 passed / 0 failed / 143 skipped`.
- Clean-smoke artifacts:
  - `ops/test-results/sprint-ets-35-clean-local-osh-smoke-2026-06-02.xml`
  - `ops/test-results/sprint-ets-35-clean-local-osh-container-2026-06-02.log`
  - `ops/test-results/sprint-ets-35-clean-local-osh-no-mutation-2026-06-02.txt`
- Raze review: `APPROVE_WITH_CONCERNS`, confidence `0.90`, no required fixes; artifact `.harness/evaluations/sprint-ets-35-adversarial-implementation.yaml`.

## Outcome

SimUAV is a better isolated local tasking fixture than Sapient for CommandResult evidence because it needs no external TCP peer and returns terminal `COMPLETED` status with inline result data. It still does not complete schema-valid populated-IUT closure because local OSH 2.0-beta2 omits Annex A.9-required stream metadata (`live`, `async`, and some time ranges) and returns schema resources with `Content-Type: auto` despite JSON bodies.

The primary local OSH state was restored by setting SimUAV and Sapient `autoStart=false`, resetting only `field-hub_osh-data`, reseeding static `040g` fixtures, and rerunning clean TeamEngine smoke successfully.
