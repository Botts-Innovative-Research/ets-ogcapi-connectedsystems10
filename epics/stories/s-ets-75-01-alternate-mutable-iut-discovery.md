# S-ETS-75-01: Alternate Mutable IUT Discovery

## Status

COMPLETE

## User Instruction

"Continue as best you can. At the same time, start a Discovery agent to
research for any other open source implementation of the OGC CS API, especially
those that claim increased coverage than that of OSH"

## Scope

Research and archive read-only readiness evidence for alternate open-source
OGC API Connected Systems implementations that might unblock the remaining
mutation-bound candidate procedures.

## Requirements

- `REQ-ETS-CLEANUP-026`
- `SCENARIO-ETS-CLEANUP-ALTERNATE-IUT-DISCOVERY-001`
- `REQ-ETS-CLEANUP-023`
- `REQ-ETS-CLEANUP-024`
- `REQ-ETS-CLEANUP-025`

## Acceptance Criteria

- [x] Discovery product brief and handoff are written.
- [x] Read-only readiness probes are archived for public candidate demos.
- [x] Probe evidence records `unsafeMethodsIssued=[]`.
- [x] Probe evidence does not promote any mutation-bound candidate to exact.
- [x] Ops docs summarize whether any public/open-source candidate looks better
  than current local OSH for future dedicated mutable-IUT work.
- [x] Raze reviews the scoped change before completion.

## Non-Goals

- Do not issue mutation methods against public candidate deployments.
- Do not patch third-party implementations.
- Do not use public demos as positive mutation lifecycle evidence.
- Do not close Create/Replace/Delete or Update exact mappings.

## Implementation Notes

- Initial public probes covered `connected-systems-go`, 52North
  `connected-systems-pygeoapi`, and public OpenSensorHub.
- Evidence directory:
  `ops/test-results/sprint-ets-75-alternate-iut-discovery-2026-08-03/`.
- Discovery verdict: `connected-systems-go` is the strongest researched
  alternate open-source candidate for future self-run Part 2
  Create/Replace/Delete lifecycle work, but no candidate currently closes all
  47 mutation-bound procedures or provides Update/PATCH closure.
- Raze returned `APPROVE_WITH_CONCERNS 0.91` with `required_fixes=[]`.
