# CP-029: Part 2 Binding Populated Command Probe Diagnostics

## Status

IMPLEMENTED - RAZE APPROVED WITH CONCERNS

## Trigger

User instructed: "Use a disposable local OSH mutable IUT and reset/reseed it as needed."

## Motivation

The current populated local OSH workflow can create supported-interface System,
Procedure, Deployment, SamplingFeature, DataStream, Observation, and
ControlStream fixtures. A Sprint 69 rebaseline against the current suite proves
that this is enough for positive Observation binding evidence, but Command
binding still SKIPs because the seeded ControlStream exposes no associated
Command item.

The next practical step is diagnostic, not a false PASS: record the current
supported-interface Command POST and nested Command collection behavior in the
same disposable owned-IUT workflow so future binding work has exact evidence.

## Scope

- Extend the populated seeder fixture manifest with an optional Command probe.
- Record Command POST status or error classification and nested Command
  collection evidence.
- Preserve existing owned loopback target validation, mutation opt-in, and
  primary-state isolation gates.
- Keep provisioning readiness based on the already-required resources and
  Observation evidence; Command probe failure is evidence, not provisioning
  failure.
- Keep `part2binding` runtime PASS criteria unchanged.

## Out of Scope

- Patching OpenSensorHub or TeamEngine.
- Claiming full `REQ-ETS-PART2-013` positive binding closure.
- Turning missing Command evidence into PASS.
- Mutating the primary local OSH or any public IUT.

## Verification

- Python unit regressions for optional Command probe evidence.
- Docker Maven test run.
- Disposable local OSH populated workflow with mutation opt-in, followed by
  cleanup and clean-primary verification.
- Raze review before completion.

Current evidence is archived under
`ops/test-results/sprint-ets-69-mutable-local-osh-2026-08-02/`:

- Python regressions pass `14` tests.
- Final Docker Maven passes `785/0/0/3`.
- Command-probe provisioning passes with `POST=8`, `GET=11` and records
  timeout plus empty nested Command collection diagnostics.
- Populated TeamEngine remains honestly non-green at `252/24/20/208`.
- Clean-primary TeamEngine remains honestly non-green at `252/23/20/209`.
- Cleanup passes and primary state diff is empty.
- Raze focused recheck returns `APPROVE_WITH_CONCERNS 0.96` with
  `RAZE-S69-001` resolved and `required_fixes: []`.
