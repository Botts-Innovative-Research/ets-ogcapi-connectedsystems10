# S-ETS-48-01: Part 1 Subsystem Direct ATS Closure

## Status

COMPLETE

## User Instruction

Triggered by the accepted recommendation to complete every released Part 1
gap continuously after System.

## Scope

Replace the historical Subsystems approximations with exact implementations of
all five released OGC 23-001 `/conf/subsystem` procedures.

- Requirements: `REQ-ETS-PART1-003`, `REQ-ETS-COVERAGE-001`
- Scenarios:
  - `SCENARIO-ETS-PART1-003-RELEASED-COLLECTION-001`
  - `SCENARIO-ETS-PART1-003-RELEASED-MEDIA-GATE-001`
  - `SCENARIO-ETS-PART1-003-RELEASED-EXACT-LINK-001`
  - `SCENARIO-ETS-PART1-003-RELEASED-RECURSIVE-PARAM-001`
  - `SCENARIO-ETS-PART1-003-RELEASED-RECURSIVE-SYSTEMS-001`
  - `SCENARIO-ETS-PART1-003-RELEASED-RECURSIVE-SUBSYSTEMS-001`
  - `SCENARIO-ETS-PART1-003-RELEASED-RECURSIVE-ASSOC-001`
  - `SCENARIO-ETS-PART1-003-RELEASED-ASSOCIATION-IMPLEMENTATION-001`
  - `SCENARIO-ETS-PART1-003-RELEASED-HIERARCHY-FAIL-CLOSED-001`
  - `SCENARIO-ETS-PART1-003-RELEASED-PROCEDURE-ISOLATION-001`
  - `SCENARIO-ETS-PART1-003-RELEASED-E2E-EXECUTION-001`
  - `SCENARIO-ETS-PART1-003-RELEASED-DIRECT-HTTP-COVERAGE-001`

## Acceptance Criteria

- [x] CP-008, this story, its contract, capability spec, design, architecture,
  traceability, and operations records define the increment before code.
- [x] The five deployed TestNG methods execute the complete released
  procedures.
- [x] Collection discovery requires the exact parent `rel=subsystems` link,
  rejects duplicate or URI-variant targets, separates HTTP 200 evidence from
  representation parsing, gates every root and nested hierarchy page before
  parsing, accepts first-response SensorML, accepts collection-validation
  SensorML without a non-standard `id`, and performs actual-media System schema
  validation.
- [x] Recursive-parameter requests use only exact boolean values and require
  successful responses without parsing unrelated representation content.
- [x] Expected hierarchy closure is derived independently with bounded,
  pagination-safe, cycle-rejecting direct-edge traversal that rejects shortcut
  overlap.
- [x] Default and `recursive=false` results exclude known transitive
  descendants; `recursive=true` includes all discovered levels.
- [x] Recursive associations cover every discovered parent and each
  independently established Sampling Feature, DataStream, and ControlStream
  implementation.
- [x] Empty hierarchy or descendant-association evidence cannot produce a
  vacuous PASS.
- [x] Shared setup cannot suppress procedures with independent prerequisites.
- [x] The five methods have reviewed exact ATS mappings.
- [x] Focused/full Maven, coverage audit, local OSH TeamEngine E2E, controlled
  positive HTTP coverage, exact-image runtime, dependency, and credential gates
  are archived.
- [x] Raze reports no unresolved required findings.

## Verification

- Raze gap-fix test-first: `12/7/0/0`, reproducing the required false-PASS
  paths before production fixes.
- Raze recheck test-first: `7/3/0/0`, reproducing first-page unsupported,
  missing-media, and SensorML plus later-page media-order defects.
- Final Raze root-media test-first: `10/3/0/0`, reproducing unsupported and
  missing first-root media plus unsupported later-root pagination media.
- Focused Docker Maven: `45/0/0/0`.
- Full Docker Maven: `417/0/0/3`.
- Coverage: `240 total / 15 exact / 2 helper / 144 candidate / 79 unmapped`;
  `/conf/subsystem` is `5/5 exact`.
- Controlled HTTP regressions execute all five successful paths and reject
  independent-association, shortcut-edge, media-ordering, and exact-link
  defects.
- Primary local OSH TeamEngine: `216/39/0/177`, 109 recognized requests, zero
  writes, and zero startup errors. All five methods execute; recursive-param
  passes and four hierarchy-dependent procedures skip because local OSH returns
  unsupported `application/json` for root System collection traversal.
- Exact image
  `sha256:32a43f81b441f3b687b9e83d9d6688016278f4f7a5fec5d8a3c2b174490f285c`
  passes TeamEngine 6 runtime verification.
- SystemFeatures sabotage: `216/37/1/178`; all five Subsystem procedures skip.
- Credential integration and wire E2E pass with zero unmasked artifact hits,
  40 masked log events, and 40 intact synthetic wire transmissions.
- Local OSH checkout remains clean and zero commits ahead of upstream; `/opt/osh`
  is read-only and the ConSys jar manifest matches checkout `4c87a65`.
- Final Raze gap-fix recheck: `APPROVE_WITH_CONCERNS`, confidence `0.99`, with
  every required finding closed. Its only concern is unrelated, undeployed user
  work in a separate `osh-addons` checkout; Sprint 48 did not touch or deploy it.

## Non-Goals

- Modifying or proxying OSH or TeamEngine.
- Treating SKIP as positive conformance evidence.
- Retaining historical canonical-shape or parent-link methods as released
  `/conf/subsystem` mappings.
- Adding hosted CI.
