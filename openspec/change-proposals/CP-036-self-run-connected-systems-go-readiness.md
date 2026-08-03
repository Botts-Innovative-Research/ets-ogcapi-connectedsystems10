# CP-036: Self-Run Connected Systems Go Readiness

## Status

COMPLETE

## User Instruction

"Do it" approving the next-step plan to run a controlled self-run
`connected-systems-go` mutable-IUT readiness experiment.

## Problem

Sprint 75 identified `SomethingCreativeStudios/connected-systems-go` as the
strongest alternate open-source Connected Systems implementation for future
mutation-bound exactness work, but that finding was based on source inspection
and read-only public demo probes. Public demos cannot be used for positive
POST/PUT/DELETE lifecycle proof, and the current local OSH target still lacks
the prerequisite declarations and Update/PATCH surface needed to close the
remaining 47 mutation-bound candidate procedures.

## Change

- Run `connected-systems-go` from upstream source as a disposable local IUT
  with isolated PostgreSQL/PostGIS storage.
- Seed only sprint-scoped resources and exercise real HTTP protocol
  lifecycles against the running service.
- Archive source provenance, launch logs, readiness audit output, lifecycle
  request/response evidence, cleanup evidence, and manifest hashes.
- Record whether the experiment improves practical readiness for Part 2
  Create/Replace/Delete work without promoting any candidate mapping to
  reviewed exact.

## Non-Goals

- Do not mutate public candidate deployments.
- Do not patch OSH, TeamEngine, this ETS, or the third-party IUT source to
  make the experiment pass.
- Do not treat source-level routes, CORS methods, or public demo behavior as
  exact ATS closure.
- Do not promote Part 1/Part 2 Create/Replace/Delete or Update candidates to
  exact without a separate ETS implementation sprint and complete TeamEngine
  evidence.

## Acceptance

- Local IUT provenance includes the upstream repository URL and commit.
- The local IUT is launched against disposable storage that can be reset and
  removed after the sprint.
- Readiness audit evidence is archived and records actual issued methods.
- At least one positive POST/GET/PUT/GET/DELETE/GET lifecycle is attempted
  through real HTTP against the self-run IUT.
- Cleanup and isolation evidence demonstrates sprint-created state is removed.
- Ops docs distinguish readiness evidence from exact conformance promotion.

## Result

Sprint 76 ran upstream `connected-systems-go` commit
`7643bb38bc9fa95a50332ed2aa5b1007b56b5028` as a self-run local IUT with
disposable PostGIS storage and archived evidence under
`ops/test-results/sprint-ets-76-connected-systems-go-readiness-2026-08-03/`.
The direct lifecycle probe passed 29 real HTTP steps and demonstrated
POST/GET/PUT/GET/DELETE/GET lifecycles for DataStream, Observation,
ControlStream, and Command. The readiness audit remained non-promotable:
47 candidates, GET plus OPTIONS only, `unsafeMethodsIssued=[]`, zero
declaration/method-ready classes, and zero
prerequisite-declaration-ready classes. TeamEngine E2E against the self-run IUT
executed real HTTP and was honestly non-green at
`275 total / 15 passed / 1 failed / 259 skipped`; the single failure was
missing Part 1 `http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/core`
in the IUT conformance page. No exact mapping is promoted from this readiness
evidence.
