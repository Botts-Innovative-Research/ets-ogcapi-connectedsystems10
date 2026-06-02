# S-ETS-33-01: Local OSH Dynamic-Data Seed Fixtures for Observation/Command Binding

## Status
PLANNING_RAZE_APPROVED. This story plans the populated-IUT follow-up to Sprint 32's read-only `part2binding` implementation. No local OSH dynamic-data fixtures have been applied by this planning story. Mandatory local OSH TeamEngine planning smoke passed on 2026-06-02 with zero IUT-bound write requests. Raze planning review initially found two traceability gaps; both are fixed and focused recheck returned `APPROVE` confidence 0.94.

## User Instruction
Triggered by: "Continue".

Continuation context: after Sprint 32 was completed and pushed, `ops/status.md` identified the next direct spec-first item as documented local OSH dynamic-data seed fixtures plus helper regressions for inline CommandStatus/CommandResult closure.

## Scope
Plan the Generator work needed to turn Sprint 32's honest SKIP state into positive local OSH binding evidence when a dedicated mutable local OSH IUT is explicitly selected.

- Requirement: `REQ-ETS-PART2-013`
- Story: `S-ETS-33-01`
- Prior story: `S-ETS-32-01`
- Primary development IUT: local OSH at `http://field-hub-osh-1:8081/sensorhub/api` on Docker network `field-hub_default`
- Public GeoRobotix role: advisory interoperability probe only, never a mutation target
- Runtime group: existing `part2binding`

## Planning Findings
Authenticated local OSH probes on 2026-06-02 show:

- Existing seed System: `/systems/040g`.
- `/datastreams?limit=5`: HTTP 200 `application/json`, empty `items`.
- `/observations?limit=5`: HTTP 200 `application/json`, empty `items`.
- `/controlstreams?limit=5`: HTTP 200 `application/json`, empty `items`.
- `/systems/040g/datastreams?limit=5`: HTTP 200 `application/json`, empty `items`.
- `/systems/040g/controlstreams?limit=5`: HTTP 200 `application/json`, empty `items`.
- `/commands?limit=5`: HTTP 400 `application/json`, message `Invalid resource name: 'commands'`.
- `OPTIONS` on dynamic-data collection paths returns HTTP 200 and advertises broad write methods.

Planning implication: local OSH is a viable dedicated mutable target, but current read-only evidence cannot produce Observation/Command binding PASS. The next Generator must discover accepted OSH write payload shapes and record created resource ids before any positive closure result is accepted.

## Official API Contract Probe
The Part 2 OpenAPI document at `https://opengeospatial.github.io/ogcapi-connected-systems/api/part2/openapi/openapi-connectedsystems-2.yaml` advertises these relevant POST bodies:

- `/systems/{systemId}/datastreams`: `application/json`, `../schemas/json/dataStream.json`.
- `/datastreams/{dataStreamId}/observations`: `application/json`, `application/swe+json`, `application/swe+csv`, `../schemas/json/observation.json` for JSON.
- `/systems/{systemId}/controlstreams`: `application/json`, `../schemas/json/controlStream.json`.
- `/controlstreams/{controlStreamId}/commands`: `application/json`, `application/swe+csv`, `../schemas/json/command.json` for JSON.
- `/commands/{cmdId}/status`: `application/json`, `../schemas/json/commandStatus.json`.
- `/commands/{cmdId}/result`: `application/json`, `../schemas/json/commandResult.json`.

Caveat: the bundled schema snippets include required fields that are described as readOnly in some cases. Generator must probe OSH acceptance under mutation opt-in before committing exact payloads and must record any accepted request/response differences.

## Planned Seed Fixture Manifest
The planning manifest is `ops/local-osh-dynamic-data-seed-fixtures.json`.

It is intentionally marked `PLANNED_NOT_APPLIED`. It records endpoint order, payload intent, safety gates, and cleanup order, but it does not claim that exact request payloads have been accepted by OSH.

Expected seed order:

1. Create or verify DataStream under `/systems/040g/datastreams`.
2. Create or verify Observation under `/datastreams/{dataStreamId}/observations`.
3. Create or verify ControlStream under `/systems/040g/controlstreams`.
4. Create or verify Command under `/controlstreams/{controlStreamId}/commands`.
5. Create or verify optional CommandStatus under `/commands/{cmdId}/status`.
6. Create or verify optional CommandResult under `/commands/{cmdId}/result`.

Cleanup must run in reverse dependency order where OSH exposes DELETE. If OSH cannot delete a created resource, Generator must record residual state and must not call the run clean.

## Helper Regression Scope
Sprint 32 Raze approved the implementation with a non-blocking concern: helper regression depth for inline CommandStatus/CommandResult data.

Sprint 33 Generator should add focused unit regressions around the existing command-side inline data behavior:

- Missing inline `status`, `commandStatus`, `result`, or `commandResult` members do not block Command parameter binding PASS.
- Present inline status/result members that are not JSON objects SKIP instead of PASS.
- Present inline status/result objects with no concrete parent-schema overlap do not PASS.
- Present inline status/result objects with missing required schema fields report mismatches.
- Present inline status/result objects with primitive type mismatches report mismatches.

Implementation style is intentionally not fixed by planning. Generator may expose a package-private helper or extract a small static helper if needed to test this behavior without broad refactoring.

## Mutation Safety
Generator must not seed fixtures unless all of these are true:

- The target is local OSH or another dedicated non-public mutable IUT.
- Mutation tests are explicitly enabled.
- The mutation IUT policy is explicitly `dedicated-mutable-iut`.
- Credential values remain out of repository docs and logs.
- Before/after seed-state probes are archived.
- Created ids, payload family, response status, cleanup result, TeamEngine totals, and no-mutation/read-only comparison evidence are archived.

Default planning and smoke runs remain read-only and must keep IUT-bound `POST`, `PUT`, `PATCH`, and `DELETE` counts at zero.

## Scenario Traceability

- `SCENARIO-ETS-PART2-013-DYNAMIC-SEED-FIXTURES-001`: planned manifest `ops/local-osh-dynamic-data-seed-fixtures.json` records the intended DataStream, Observation, ControlStream, Command, CommandStatus, and CommandResult dependency order plus required evidence fields.
- `SCENARIO-ETS-PART2-013-SEED-MUTATION-SAFETY-001`: manifest and story require explicit `dedicated-mutable-iut` opt-in, forbid public/shared IUT mutation, and keep default planning smoke read-only.
- `SCENARIO-ETS-PART2-013-INLINE-STATUS-RESULT-REGRESSIONS-001`: story and contract carry Sprint 32 Raze's helper-regression concern for inline `status`, `commandStatus`, `result`, and `commandResult` skip/fail behavior.
- `SCENARIO-ETS-PART2-013-POSITIVE-LOCAL-OSH-CLOSURE-001`: planning E2E artifacts establish the current empty-IUT baseline and require future Generator evidence to include live parent schema, child body evidence, resource ids, cleanup results, TeamEngine totals, and request method counts.

## Definition of Done
- [x] Contract created at `.harness/contracts/sprint-ets-33.yaml`.
- [x] Story created at `epics/stories/s-ets-33-01-local-osh-dynamic-data-seed-fixtures-planning.md`.
- [x] OpenSpec adds Sprint 33 fixture and helper-regression scenarios under `REQ-ETS-PART2-013`.
- [x] Traceability and epic ETS-03 map `S-ETS-33-01`.
- [x] Planning probe artifact is archived.
- [x] Planned fixture manifest is created and marked not applied.
- [x] Mandatory local OSH TeamEngine planning E2E is complete: `211 total / 68 passed / 0 failed / 143 skipped`.
- [x] Planning no-mutation evidence is archived: `GET=133`, `OPTIONS=2`, `POST/PUT/PATCH/DELETE=0`.
- [x] Raze planning review is complete: `.harness/evaluations/sprint-ets-33-plan-adversarial.yaml`, final `APPROVE` confidence 0.94.
- [x] Post-Raze reconciliation is complete.
- [ ] Planning changes are committed and pushed.
