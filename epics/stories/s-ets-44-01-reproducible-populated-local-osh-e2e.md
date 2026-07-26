# S-ETS-44-01: Reproducible Populated Local OSH E2E

## Status

COMPLETE

## User Instruction

Triggered by: "Make it so" after confirming that supported local OSH
configuration and API-created fixtures are in scope.

## Scope

Create an ETS-owned, reproducible populated local OSH E2E workflow without
modifying OSH or TeamEngine source code or binaries.

- Requirements:
  - `REQ-ETS-PART2-013`
  - `REQ-ETS-TEAMENGINE-006`
  - `REQ-ETS-SCOPE-001`
- Scenarios:
  - `SCENARIO-ETS-PART2-013-EPHEMERAL-POPULATED-IUT-001`
  - `SCENARIO-ETS-PART2-013-POPULATED-PROVISIONING-VERDICT-001`
  - `SCENARIO-ETS-PART2-013-POPULATED-EVIDENCE-001`
  - `SCENARIO-ETS-PART2-013-PRIMARY-STATE-ISOLATION-001`

## Acceptance Criteria

- [x] CP-004 and this story define the workflow before implementation.
- [x] An exact fixture manifest defines static System/Procedure/Deployment/
  SamplingFeature resources and dynamic DataStream/Observation/ControlStream
  resources accepted by the current public OSH API.
- [x] A hard-gated seeder refuses mutation without both
  `mutation-tests-enabled=true` and
  `mutation-iut-policy=dedicated-mutable-iut`.
- [x] The seeder rejects public/non-local targets and records no credential
  values.
- [x] The orchestrator mounts the external OSH installation read-only, records
  source/distribution/config provenance, uses isolated state, and leaves the
  existing primary OSH container/state untouched.
- [x] TeamEngine executes against the populated IUT and archives a non-empty
  XML report and container log.
- [x] Provisioning readiness and TestNG conformance are reported separately;
  TestNG failures remain failures and make the workflow non-zero.
- [x] Ephemeral OSH state is removed unless an explicit diagnostic-retention
  option is supplied.
- [x] The clean primary local OSH smoke runs after the populated attempt and
  remains authoritative.
- [x] Focused/full Maven, exact-image runtime verification, populated E2E, and
  clean-primary E2E results are archived.
- [x] Raze reviews the completed workflow and all required findings are closed.

## Implementation Evidence

- Behavioral safety suite: `12/0/0/0`.
- Focused Maven: `9/0/0/0`.
- Full Docker Maven: `322/0/0/3`.
- Exact TeamEngine runtime image
  `sha256:cc8c9d711e57ed50d2ed08cdef01cb1236052e775ff27ad016185672e9de8169`
  passed provenance, immutability, dependency, adapter-execution, and
  collision-policy verification.
- Provisioning passed with seven accepted POSTs and ten verification GETs.
- Populated TeamEngine completed `211/91/28/92`. Its TestNG conformance verdict
  is `FAIL`; all 28 failures remain visible and the workflow exited non-zero.
- Owned-container cleanup passed, ephemeral state was removed, the normalized
  primary fingerprint had no changed fields, and clean-primary TeamEngine
  passed `211/69/0/142` with zero writes.
- Complete evidence is archived under
  `ops/test-results/sprint-ets-44-final-e2e-2026-07-25/`.
- Initial Raze review returned `GAPS_FOUND` at confidence `0.99`. The
  implementation addressed all ten required findings. Focused adversarial
  recheck returned `APPROVE` at confidence `0.99` with no new findings.

## Discovery Evidence

Before implementation, a manually isolated OSH 2.0.1 process from clean checkout
`4c87a65c9a967d52af9df476e65d7862c7673a15` accepted four static fixtures and
API-created DataStream, Observation, and ControlStream resources. TeamEngine
executed `211` tests and returned `86 passed / 28 failed / 97 skipped`.

All 28 failures were honest OSH representation failures:

- DataStream collection items omitted required `live`.
- ControlStream collection items omitted required `issueTime`,
  `executionTime`, `live`, and `async`.

Supplying those read-only fields in create payloads did not make OSH serialize
them. The ETS must preserve these failures; neither fixtures nor schemas may be
weakened to create a passing result.

## Non-Goals

- Modifying OSH or TeamEngine source code or binaries.
- Installing a project-built OSH driver or patch.
- Treating fixture readiness as conformance PASS.
- Weakening Annex A.9, SWE Common, media-type, or binding assertions.
- Mutating GeoRobotix or any shared/public IUT.
