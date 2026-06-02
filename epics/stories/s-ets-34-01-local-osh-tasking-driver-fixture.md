# S-ETS-34-01: Local OSH Tasking Driver Fixture for Command Binding

## Status
VERIFIED_WITH_CONSTRAINTS. The user correctly identified the Sprint 33 blocker: creating a ControlStream manually is not enough for positive Command evidence because OSH waits for the owning tasking receiver to acknowledge the submitted Command. Sprint 34 selected and configured a Sapient tasking-capable local OSH driver fixture and proved that a real protocol peer can complete a CS API Command with terminal CommandStatus evidence. Full `part2binding` PASS closure is still not claimed because the Sapient dynamic stream representations fail broader Part 2 schema suites and the binding runtime still skips when parent schema resources are not returned with JSON content type.

## User Instruction
Triggered by: "OK, so you will need to find and configure an OSH driver that accepts tasks in order to complete the test."

## Scope
Configure a dedicated local OSH tasking fixture for `REQ-ETS-PART2-013` populated Command binding closure.

- Requirement: `REQ-ETS-PART2-013`
- Story: `S-ETS-34-01`
- Prior story: `S-ETS-33-01`
- Primary development IUT: local OSH at `http://field-hub-osh-1:8081/sensorhub/api` on Docker network `field-hub_default`
- Public GeoRobotix role: advisory interoperability probe only, never a mutation target
- Runtime group: existing `part2binding`

## Finding
Sprint 33 proved Observation-side seeding and ControlStream schema creation, but Command creation timed out because the inserted ControlStream had no receiving tasking module. A valid fixture must be owned by a live driver/process that accepts Commands and emits CommandStatus.

Candidate assessment:

- `sensorhub-driver-universalcontroller`: already present in field-hub OSH, but it is a controller input source. It emits controller observations and is not a CS API Command receiver for this fixture.
- `kirby-osh`: has tuner and positioner tasking controls, but the implementation targets Sceptre/Kerby hardware APIs and is not the least-external local fixture.
- `sapient`: has an existing OSH driver, local `osh-test` config, SAPIENT TCP test client, LOOK_AT ControlStreams, and TaskAck-to-CommandStatus E2E evidence. This is the selected Sprint 34 fixture candidate because it can run with a local protocol peer.

## Planned Configuration
Configured field-hub OSH with the Sapient driver module and a locally reachable SAPIENT TCP port:

1. Add `sensorhub-driver-sapient-0.1.0.jar` to the field-hub OSH image library.
2. Add a `com.georobotix.impl.sensor.sapient.Sapient` module to field-hub `config.json` with `serverPort=12000`, TLS disabled, and `taskAckTimeoutMs=30000`.
3. Expose/map TCP `12000` for the local SAPIENT test peer.
4. Rebuild/restart field-hub OSH.
5. Register a LOOK_AT-capable SAPIENT node over the real TCP protocol.
6. Submit a CS API Command to the driver-owned ControlStream and return `TASK_STATUS_COMPLETED` from the local peer.
7. Record the live ControlStream id, Command id, terminal status, protocol acknowledgement, and residual/cleanup state.

## Verification Notes

- Driver selected: `sensorhub-driver-sapient-0.1.0.jar` from `/home/nh/docker/gir/sapient`.
- Field-hub OSH was temporarily run with Sapient `autoStart=true`, TCP port `12000`, and protobuf runtime conflicts removed so only protobuf `4.31.1` remained active.
- Local SAPIENT peer registered node `eeee3401-0000-0000-0000-000000000034`; OSH created ControlStream `02nc9lm4r9p0` named `SAPIENT Gateway - COMMAND_TYPE_LOOK_AT - eeee3401`.
- CS API `POST /controlstreams/02nc9lm4r9p0/commands` returned terminal `COMPLETED` for Command `02nc9lm4r9p01sv2vf80cdtdkmf0`; the protocol peer observed Task `f3ee6568d2fe4fc79ab57efc8b` and returned a terminal TaskAck.
- Evidence artifact: `ops/test-results/sprint-ets-34-local-osh-sapient-command-e2e-2026-06-02.json`.
- A TeamEngine smoke against the Sapient-populated OSH failed `211 total / 82 passed / 28 failed / 101 skipped`; failures were schema-validation failures for the newly visible Sapient DataStream/ControlStream resources, not command-ack timeout. Artifacts: `ops/test-results/sprint-ets-34-local-osh-smoke-failed-2026-06-02.xml`, `ops/test-results/sprint-ets-34-local-osh-container-failed-2026-06-02.log`, and no-mutation `GET=149`, `OPTIONS=14`, writes `0`.
- OSH CS API would not delete the module-owned Sapient streams (`DELETE` returned `404` for stream/system resources and `400 Not implemented` for the Command), so cleanup required resetting only the `field-hub_osh-data` Docker volume and reseeding the static `040g` fixtures.
- Final field-hub state for primary smoke: Sapient remains configured but `autoStart=false` to avoid contaminating default read-only TeamEngine discovery; the clean local OSH smoke passed `211 total / 68 passed / 0 failed / 143 skipped` with no-mutation evidence `GET=133`, `OPTIONS=2`, writes `0`.
- Docker Maven `clean test` passed `288 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-34-maven-clean-test-2026-06-02.log`. An earlier unbounded wrapper attempt was stopped after Maven blocked in an HTTPS dependency download before `clean`.

## Scenario Traceability

- `SCENARIO-ETS-PART2-013-TASKING-DRIVER-FIXTURE-001`: this story selects and configures a live Sapient tasking module so Commands are accepted by a real receiving module rather than timing out on manually inserted ControlStreams.
- `SCENARIO-ETS-PART2-013-COMMAND-PARENT-SCHEMA-001`: positive evidence must include a driver-owned ControlStream schema and associated Command parameters.
- `SCENARIO-ETS-PART2-013-POSITIVE-LOCAL-OSH-CLOSURE-001`: final closure still requires TeamEngine evidence, method counts, ids, and cleanup/residual documentation.
- `SCENARIO-ETS-PART2-013-SEED-MUTATION-SAFETY-001`: public/shared IUT mutation remains forbidden; only the dedicated local OSH target may be mutated.

## Definition of Done

- [x] OpenSpec records the tasking-driver fixture scenario.
- [x] Story created for the user-directed driver configuration follow-up.
- [x] Field-hub OSH is configured with a tasking-capable driver.
- [x] Local protocol peer registers a task-capable system and ControlStream.
- [x] CS API Command POST completes with terminal CommandStatus evidence.
- [x] Fixture evidence is archived under `ops/test-results/`.
- [x] TeamEngine local OSH E2E is rerun and documented.
- [x] Maven verification is rerun and documented.
- [x] Specs, story, traceability, status, changelog, and test results are reconciled.
