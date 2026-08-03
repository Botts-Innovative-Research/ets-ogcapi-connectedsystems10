# S-ETS-76-01: Self-Run Connected Systems Go Readiness

## Status

COMPLETE

## User Instruction

"Do it" approving Sprint 76: stand up a self-run disposable
`connected-systems-go` mutable IUT, seed/probe it as feasible, archive
readiness and lifecycle evidence, reconcile specs and ops, run Raze, and push.

## Scope

Run the upstream `SomethingCreativeStudios/connected-systems-go` implementation
locally as a disposable mutable IUT and determine whether it is practically
usable for the remaining mutation-bound ETS work.

## Requirements

- `REQ-ETS-CLEANUP-027`
- `SCENARIO-ETS-CLEANUP-SELF-RUN-ALT-IUT-READINESS-001`
- `REQ-ETS-CLEANUP-026`

## Acceptance Criteria

- [x] Upstream source provenance is recorded.
- [x] Local disposable storage/runtime launch evidence is archived.
- [x] Read-only mutation-readiness audit evidence is archived.
- [x] Positive real-HTTP lifecycle proof is attempted for a scoped Part 2
  Create/Replace/Delete path.
- [x] Cleanup/isolation evidence is archived.
- [x] No mutation-bound candidate mappings are promoted to reviewed exact.
- [x] Raze reviews the scoped change before completion.

## Non-Goals

- Do not mutate public candidate deployments.
- Do not patch third-party IUT source to make the experiment pass.
- Do not close Create/Replace/Delete or Update exact mappings in this story.
- Do not rely on CORS method lists as `Allow` evidence.

## Implementation Notes

- Evidence directory:
  `ops/test-results/sprint-ets-76-connected-systems-go-readiness-2026-08-03/`.
- Source provenance:
  `SomethingCreativeStudios/connected-systems-go` commit
  `7643bb38bc9fa95a50332ed2aa5b1007b56b5028`; local image id
  `sha256:2aab27501798d2d7403bbcb71c55078a638cc0f7eafb1bc92efe7dc982f21a78`.
- Direct readiness audit:
  `47` remaining candidate procedures, `GET=1`, `OPTIONS=25`,
  `unsafeMethodsIssued=[]`, no declaration/method-ready classes, no
  prerequisite-declaration-ready classes, no exact-promotion-ready classes.
- Lifecycle-ID readiness audit:
  `GET=1`, `OPTIONS=29`, `unsafeMethodsIssued=[]`, no exact-promotion-ready
  classes.
- Direct lifecycle probe:
  PASS, `29` real HTTP steps, method counts `GET=15`, `POST=5`, `PUT=4`,
  `DELETE=5`, with positive DataStream, Observation, ControlStream, and Command
  POST/GET/PUT/GET/DELETE/GET evidence.
- Cleanup:
  direct lifecycle database counts returned to zero before teardown; both
  disposable Docker networks and all Sprint 76 containers were removed.
- TeamEngine E2E:
  NON_GREEN_HONEST, `275 total / 15 passed / 1 failed / 259 skipped`,
  no-mutation oracle `recognized_iut_request_logs=16`,
  `no_mutation_oracle_exit=0`. The failure is
  `conformancePageDeclaresCsCore` because the IUT omits Part 1 `/conf/core`.
- Raze:
  initial static review returned `GAPS_FOUND 0.88` for three bookkeeping gaps:
  commit reachability, Raze-pending status reconciliation, and manifest
  self-coverage scope. The status and manifest findings are addressed in this
  reconciliation; commit reachability is closed by pushed commit `a0f2803`.
- Outcome:
  `connected-systems-go` is a useful local Part 2 CRD route/lifecycle
  diagnostic, but it is not currently a conforming exact-promotion IUT for the
  remaining mutation-bound ETS candidates.
