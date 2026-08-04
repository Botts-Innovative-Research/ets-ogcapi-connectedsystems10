# CP-041: SensorML First-Party Validator Hardening

## Status

COMPLETE - RAZE APPROVED WITH CONCERNS

## User Instruction

"Do Plan 2. The creator of connected-systems-go has been contacted separately."

Plan 2 is the autonomous SensorML follow-up: harden the first-party SensorML
validator path now that no external reusable SensorML validator is available.

## Problem

The project has confirmed that `opengeospatial/ets-sensorml30` is an official
SensorML ETS source repository, not a reusable validator module. Sprint 58
already introduced the ETS-owned SensorML adapter and closed `/conf/sensorml`,
but the maintained first-party path should have explicit ongoing hardening
coverage: valid and invalid fixture corpus coverage for every closed schema
target, deterministic backend-neutral diagnostic invariants, and watch criteria
for any future upstream `sensorml30-validator` replacement.

## Change

- Add explicit `REQ-ETS-VALIDATOR-001` scenarios for first-party SensorML
  hardening and source-watch criteria.
- Extend SensorML adapter contract tests with complete valid and invalid corpus
  coverage for every `SensorMlSchema` target.
- Verify diagnostics remain immutable, sorted, deterministic, and free of
  backend/TestNG public API leakage.
- Verify executable `ets-sensorml30` suite classes are not visible as a
  validator dependency.
- Archive machine-readable source-watch criteria for future upstream validator
  replacement.

## Non-Goals

- Do not import `ets-sensorml30` or any new SensorML dependency.
- Do not change SensorML TestNG procedure behavior or promote new ATS mappings.
- Do not mutate any IUT.
- Do not modify OSH or TeamEngine source or binaries.
- Do not claim an upstream reusable SensorML module exists.

## Acceptance

- [x] Specs, story, contract, traceability, and ops docs make the ETS-owned
  SensorML backend the maintained first-party path.
- [x] Unit tests cover valid and invalid fixtures for all eight closed
  `SensorMlSchema` targets.
- [x] Unit tests prove backend-neutral deterministic diagnostics and operational
  failure separation, including null backend diagnostics.
- [x] Unit tests prove `ets-sensorml30` executable suite classes are not on the
  validator dependency path.
- [x] Source-watch criteria are archived under `ops/test-results/`.
- [x] Focused Docker Maven, full Docker Maven, local OSH TeamEngine E2E, and
  Raze review are recorded honestly.

## Verification

- Focused Docker Maven:
  `VerifyConnectedSystemsSensorMlValidatorAdapter` PASS `9/0/0/0`.
- Full Docker Maven: BUILD SUCCESS with `792` tests, `0` failures, `0` errors,
  and `3` skips.
- Local OSH TeamEngine smoke: non-green IUT result `275 total / 23 passed / 20
  failed / 232 skipped`; failures are concentrated in SensorML read/schema
  advertising and known local OSH collection metadata gaps.
- Static checks: JSON parse PASS, YAML parse PASS, `git diff --check` PASS,
  active stale-provisional wording scan PASS, and added-content secret scan
  PASS.
- Raze: `APPROVE_WITH_CONCERNS 0.91` with `required_fixes=[]`; the low
  durability recommendation was addressed by archiving the TeamEngine
  container log as tracked `.txt` evidence in the sprint evidence directory.
