# S-ETS-69-01: Part 2 Binding Populated Command Probe Diagnostics

## Status

DONE

## User Instruction

"Use a disposable local OSH mutable IUT and reset/reseed it as needed."

Future planning preference captured from the same instruction: when asking for
approval or alternatives, number the plan options so the user can approve by
number.

## Scope

Rebaseline the existing disposable populated local OSH workflow against the
current full suite, then add optional Command probe diagnostics to the supported
interface seeder. This story does not promote full `REQ-ETS-PART2-013`
Observation/Command binding closure unless TeamEngine independently observes
both parent schema and associated child body evidence.

## Requirements

- `REQ-ETS-PART2-013`
- `SCENARIO-ETS-PART2-013-EPHEMERAL-POPULATED-IUT-001`
- `SCENARIO-ETS-PART2-013-POPULATED-PROVISIONING-VERDICT-001`
- `SCENARIO-ETS-PART2-013-POPULATED-EVIDENCE-001`
- `SCENARIO-ETS-PART2-013-PRIMARY-STATE-ISOLATION-001`
- `SCENARIO-ETS-PART2-013-POPULATED-COMMAND-PROBE-DIAGNOSTICS-001`

## Baseline Evidence

Sprint 69 baseline disposable run:

- Evidence directory: `ops/test-results/sprint-ets-69-mutable-local-osh-2026-08-02/`
- Run id: `ets69-20260802T035352Z`
- Provisioning: PASS; supported HTTP API created System, Procedure, Deployment,
  SamplingFeature, DataStream, Observation, and ControlStream fixtures.
- Provisioning request counts: `POST=7`, `GET=10`.
- Populated TeamEngine: honest non-green `252 total / 24 passed / 20 failed /
  208 skipped`.
- `part2binding` outcome: Observation binding PASSes from seeded DataStream
  schema plus Observation body; Command binding SKIPs because no inspected
  ControlStream exposes associated Command evidence.
- Populated TeamEngine request counts: `GET=134`, zero writes.
- Cleanup: PASS; primary state diff is empty.
- Clean-primary TeamEngine: honest non-green `252 total / 23 passed / 20
  failed / 209 skipped`; request counts `GET=130`, zero writes.

Post-change disposable run with optional Command probe:

- Run id: `ets69cmdprobe-20260802T042211Z`
- Provisioning: PASS with `POST=8`, `GET=11`.
- Command probe evidence: `POST /controlstreams/040g/commands` timed out,
  `GET /controlstreams/040g/commands?limit=10` returned
  `application/json` with `itemCount=0`, and no Command id was discovered.
- Populated TeamEngine: honest non-green `252 total / 24 passed / 20 failed /
  208 skipped`; `part2binding` Observation binding PASSes and Command binding
  SKIPs for missing associated Command evidence.
- Populated TeamEngine request counts: `GET=134`, zero writes.
- Cleanup: PASS; primary state diff is empty.
- Clean-primary TeamEngine: honest non-green `252 total / 23 passed / 20
  failed / 209 skipped`; request counts `GET=130`, zero writes.
- Full Docker Maven final: `785 tests / 0 failures / 0 errors / 3 skipped`.
- Python workflow regressions: `14 tests` passed.
- Raze gapfix: baseline and command-probe SHA/provenance manifests are
  archived under `baseline-manifests/` and `command-probe-manifests/`, and the
  copied run summaries now reference those repository paths.
- Raze focused recheck: `APPROVE_WITH_CONCERNS 0.96`; `RAZE-S69-001` is
  resolved and `required_fixes: []`.

## Acceptance Criteria

- [x] Baseline disposable local OSH evidence is archived with provisioning,
  TeamEngine, cleanup, and clean-primary results.
- [x] OpenSpec records the optional Command probe diagnostics requirement.
- [x] The fixture manifest supports an optional Command probe body.
- [x] The seeder records Command POST and nested Command collection diagnostics
  without making provisioning fail solely because Command evidence is absent.
- [x] Python unit regressions cover successful and failed optional Command
  probes.
- [x] Docker Maven and disposable populated local OSH E2E are rerun after the
  harness change.
- [x] Raze reviews the scoped change before completion.
- [x] Specs, story, traceability, status, changelog, test-results, and metrics
  are reconciled.

## Non-Goals

- Do not patch OSH or TeamEngine.
- Do not mutate the primary local OSH.
- Do not claim full positive Command binding closure without TeamEngine-visible
  associated Command body evidence.
