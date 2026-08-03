# S-ETS-78-01: Alternate IUT Discovery Follow-up

## Status

COMPLETE_RAZE_APPROVED

## User Instruction

"Continue alternate IUT discovery"

## Scope

Refresh the alternate open-source IUT discovery inventory after the Sprint 76
self-run `connected-systems-go` evidence and Sprint 77 upstream gap package.
The sprint is documentation and evidence only: public probes remain read-only,
and no candidate mapping is promoted.

## Requirements

- `REQ-ETS-CLEANUP-029`
- `SCENARIO-ETS-CLEANUP-ALT-IUT-DISCOVERY-FOLLOWUP-001`
- `REQ-ETS-CLEANUP-026`
- `REQ-ETS-CLEANUP-027`
- `REQ-ETS-CLEANUP-028`

## Acceptance Criteria

- [x] Raw official-registry, GitHub, source-clone, and public-readiness probe
  evidence is archived.
- [x] A compact candidate summary identifies each candidate's current
  disposition.
- [x] Newly discovered direct CS API server claims are classified without
  overstating runnable implementation evidence.
- [x] Public probes are GET/OPTIONS only and report no unsafe methods.
- [x] Product brief, handoff, ops docs, traceability, and OpenSpec are
  reconciled.
- [x] Lightweight verification and Raze review are recorded.

## Non-Goals

- Do not mutate public IUTs.
- Do not run TeamEngine, Maven, or Docker deployment work for this discovery
  sprint.
- Do not alter third-party source.
- Do not file outreach or GitHub issues.
- Do not promote mutation-bound candidate mappings.

## Implementation Notes

- Evidence directory:
  `ops/test-results/sprint-ets-78-alternate-iut-discovery-followup-2026-08-03/`.
- Compact summary:
  `candidate-summary.json`.
- 52North `connected-systems-pygeoapi` source HEAD:
  `18c1ce803fcdb2de3aac9d227ab814306d8a718f`.
- DGIWG `glaux-server` source HEAD:
  `1ba41159d1465797f1fceab129486197eb80aadf`.
- Public readiness probes for 52North and public `connected-systems-go` are
  GET/OPTIONS only, report `unsafeMethodsIssued=[]`, and do not promote any
  mutation-bound candidate.
- Lightweight verification:
  JSON parse PASS, YAML parse PASS, Markdown artifact presence PASS, public
  probe policy PASS, ignored-log check PASS, evidence manifest PASS, and
  `git diff --check` PASS.
- Raze review:
  initial review returned `GAPS_FOUND 0.90`; all four required fixes were
  applied and focused recheck returned `APPROVE 0.94` with
  `required_fixes=[]`.
