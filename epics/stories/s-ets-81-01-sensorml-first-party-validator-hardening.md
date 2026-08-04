# S-ETS-81-01: SensorML First-Party Validator Hardening

## Status

COMPLETE - RAZE APPROVED WITH CONCERNS

## User Instruction

"Do Plan 2. The creator of connected-systems-go has been contacted separately."

## Scope

Harden the maintained ETS-owned SensorML backend now that no reusable external
SensorML validator exists. This sprint adds contract tests and durable
source-watch criteria; it does not change released TestNG procedure behavior or
import a new dependency.

## Requirements

- `REQ-ETS-VALIDATOR-001`
- `SCENARIO-ETS-VALIDATOR-SENSORML-FIRST-PARTY-HARDENING-001`
- `SCENARIO-ETS-VALIDATOR-SENSORML-SOURCE-WATCH-001`

## Acceptance Criteria

- [x] Valid and invalid fixture maps cover every `SensorMlSchema` enum target.
- [x] Every valid fixture validates cleanly through
  `ConnectedSystemsSensorMlValidatorAdapter`.
- [x] Every invalid fixture returns deterministic immutable ETS-owned
  diagnostics and exposes no NetworkNT/TestNG verdict type.
- [x] Backend null-result and null-diagnostic cases remain operational failures.
- [x] The executable `ets-sensorml30` suite classes are not visible as
  validator dependencies.
- [x] Source-watch criteria describe the minimum evidence needed before a future
  upstream reusable SensorML module can replace the first-party backend.
- [x] Focused/full Maven, local OSH TeamEngine E2E, ops reconciliation, and
  Raze review are archived.

## Non-Goals

- Do not mutate public or local IUTs beyond the standard read-only smoke gate.
- Do not add external SensorML dependencies.
- Do not change SensorML ATS mappings or exact counts.
- Do not file CITE tickets or publish artifacts.

## Implementation Notes

Use the existing `ConnectedSystemsSensorMlValidatorAdapter` and
`SensorMlValidationResult` contracts. The hardening work should remain behind
the adapter boundary so a future `sensorml30-validator` can replace only the
backend after parity, diagnostics, dependency-closure, and E2E review.

## Verification

- Focused Docker Maven:
  `VerifyConnectedSystemsSensorMlValidatorAdapter` PASS `9/0/0/0`.
- Full Docker Maven: BUILD SUCCESS with `792/0/0/3`.
- Local OSH TeamEngine smoke: executed against
  `http://field-hub-osh-1:8081/sensorhub/api`; non-green IUT result
  `275 total / 23 passed / 20 failed / 232 skipped`.
- Static checks: JSON parse PASS, YAML parse PASS, `git diff --check` PASS,
  active stale-provisional wording scan PASS, and added-content secret scan
  PASS.
- Raze: `APPROVE_WITH_CONCERNS 0.91` with `required_fixes=[]`; the low
  durability recommendation was addressed by archiving the TeamEngine
  container log as tracked `.txt` evidence in the sprint evidence directory.
