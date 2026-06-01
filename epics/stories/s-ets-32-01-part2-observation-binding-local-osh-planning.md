# S-ETS-32-01: Observation/Command Binding Closure with Local OSH Primary E2E

## Status
SPECIFIED_PLANNED. Raze planning review approved after focused gapfix. Generator implementation remains pending.

## User Instruction
Planning triggered by: "Let's abandon GeoRobotix's public instance as a test target. Let's use a local, self-provisioned OSH as the primary test target for development. Continue with the spec-first planning."

Continuation triggered by: "continue sprint 32 spec-first planning".

## Scope
Plan the next Part 2 cross-class closure and the development E2E target policy.

- Requirement: `REQ-ETS-PART2-013`
- Story: `S-ETS-32-01`
- E2E policy requirement: `REQ-ETS-TEAMENGINE-006`
- Primary development IUT: local OSH at `http://field-hub-osh-1:8081/sensorhub/api` on Docker network `field-hub_default`
- Public GeoRobotix role: advisory interoperability probe only, not a default gate
- Runtime implementation: out of scope for this planning story

## OGC Source Verification
Official source: `https://docs.ogc.org/is/23-002/23-002.html`, verified on 2026-06-01.

OGC 23-002 lists the Part 2 requirements classes as Common, Datastreams & Observations, Control Streams & Commands, Command Feasibility, System Events, Advanced Filtering, Create/Replace/Delete, Update, JSON Encoding, SWE Common JSON Encoding, SWE Common Text Encoding, and SWE Common Binary Encoding. It does not define a standalone `/conf/observation-binding` conformance class.

Planning conclusion: `REQ-ETS-PART2-013` is an internal project closure tying existing DataStream/Observation and ControlStream/Command schema requirements together. The ETS must not require, advertise, or PASS a fabricated `/conf/observation-binding` class.

The source basis for the closure is:

- DataStreams are containers for Observations.
- Observations are grouped into DataStreams and carry results structured by a schema.
- ControlStreams are containers for Commands.
- Commands carry parameters structured by a schema.

## Planned Runtime Behavior
Generator should add a declaration-gated cross-class binding suite, probably group `part2binding`, that reuses existing Part 2 evidence instead of inventing a new OGC conformance class.

Observation-side positive checks require:

- `/conf/datastream` declared.
- A candidate DataStream.
- The DataStream schema subresource or equivalent parent-schema evidence.
- A candidate Observation associated with that DataStream.
- Concrete field/type mapping evidence between parent schema members and the Observation result/parameters body for the negotiated encoding.

Command-side positive checks require:

- `/conf/controlstream` declared.
- A candidate ControlStream.
- The ControlStream schema subresource or equivalent parent-schema evidence.
- A candidate Command-side body associated with that ControlStream.
- Concrete field/type mapping evidence between parent schema members and Command parameters, and any available CommandStatus or CommandResult inline data.

## False-PASS Guardrails
Generator must not PASS from:

- Declaration alone.
- Empty DataStream, Observation, ControlStream, Command, CommandStatus, or CommandResult collections.
- Broad format lists without retrieved schema and child body evidence.
- Sibling encoding evidence.
- Hardcoded examples.
- OPTIONS-only evidence.
- A non-standard `/conf/observation-binding` declaration.
- GeoRobotix advisory public probes.

If local OSH remains empty, positive binding assertions must SKIP with exact empty-IUT-state reasons.

## Local OSH Evidence
Local OSH planning probes are archived at `ops/test-results/sprint-ets-32-plan-local-osh-probes-2026-06-01.txt`.

Current local OSH state:

- `/conformance`: HTTP 200.
- Declares Part 2 `/conf/datastream`, `/conf/controlstream`, `/conf/json`, `/conf/swecommon-json`, `/conf/swecommon-text`, `/conf/swecommon-binary`, `/conf/create-replace-delete`, and `/conf/system-event`.
- Also declares non-standard `/conf/system-history`, which remains vendor evidence only.
- `/datastreams?limit=1`: HTTP 200 `application/json`, empty `items`.
- `/observations?limit=1`: HTTP 200 `application/json`, empty `items`.
- `/controlstreams?limit=1`: HTTP 200 `application/json`, empty `items`.
- `/commands?limit=1`: HTTP 400 `application/json`.
- `/systemEvents?limit=1`: HTTP 400 `application/json`.

Implication: positive Observation/Command binding closure requires documented dynamic-data seed fixtures before Generator can produce PASS evidence. Without fixtures, Generator must SKIP positive binding checks honestly.

## Planning E2E
Command shape, with credential value intentionally omitted:

```bash
SMOKE_AUTH_CREDENTIAL=<derived, not logged> \
SMOKE_CONTAINER_NAME=ets-csapi-s32-plan-local-osh \
SMOKE_TARGET=local-osh \
SMOKE_DOCKER_NETWORK=field-hub_default \
SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api \
SMOKE_OUTPUT_DIR=/tmp/sprint-ets-32-local-osh-plan-2026-06-01-results \
bash scripts/smoke-test.sh
```

Result: PASS, `206 total / 65 passed / 0 failed / 141 skipped`.

Artifacts:

- `ops/test-results/sprint-ets-32-plan-local-osh-smoke-2026-06-01.xml`
- `ops/test-results/sprint-ets-32-plan-local-osh-container-2026-06-01.log`
- `ops/test-results/sprint-ets-32-plan-local-osh-no-mutation-2026-06-01.txt`

No-mutation evidence:

- `recognized_iut_request_logs=132`
- `GET=130`
- `OPTIONS=2`
- `POST=0`
- `PUT=0`
- `PATCH=0`
- `DELETE=0`

## Definition of Done
- [x] OpenSpec specifies `REQ-ETS-PART2-013` as internal Observation/Command binding closure.
- [x] OpenSpec adds `REQ-ETS-TEAMENGINE-006` for local OSH as the primary development target.
- [x] PRD no longer treats GeoRobotix public instance as the development smoke gate.
- [x] `AGENTS.md`, `ops/e2e-test-plan.md`, and `scripts/smoke-test.sh` default E2E to local OSH.
- [x] Contract and story document the no-standalone-`/conf/observation-binding` conclusion.
- [x] Local OSH probe and TeamEngine smoke artifacts are archived.
- [x] Read-only planning smoke records zero IUT-bound POST/PUT/PATCH/DELETE requests.
- [x] Empty local OSH dynamic-data state is documented as a Generator seed/skip constraint.
- [x] Raze planning review is complete.
- [x] Post-Raze reconciliation is complete.
- [ ] Planning changes are committed and pushed.

## Out of Scope
- Runtime `part2binding` TestNG implementation.
- Dynamic-data fixture seeding.
- Mutation-enabled local OSH lifecycle checks.
- Any requirement to run GeoRobotix as a development gate.
- Any fabricated OGC `/conf/observation-binding` conformance class.
