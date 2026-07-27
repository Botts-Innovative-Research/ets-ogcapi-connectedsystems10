# S-ETS-51-01: Part 1 Subdeployment Direct ATS Closure

## Status

COMPLETE

## User Instruction

Triggered by the accepted recommendation to complete released Part 1 gaps
continuously after Procedure.

## Scope

Replace the historical Subdeployment approximations with exact implementations
of all five released OGC 23-001 `/conf/subdeployment` procedures.

- Requirements: `REQ-ETS-PART1-005`, `REQ-ETS-COVERAGE-001`,
  `REQ-ETS-VALIDATOR-001`
- Scenarios:
  - `SCENARIO-ETS-PART1-005-RELEASED-COLLECTION-001`
  - `SCENARIO-ETS-PART1-005-RELEASED-RECURSIVE-PARAM-001`
  - `SCENARIO-ETS-PART1-005-RELEASED-RECURSIVE-DEPLOYMENTS-001`
  - `SCENARIO-ETS-PART1-005-RELEASED-RECURSIVE-SUBDEPLOYMENTS-001`
  - `SCENARIO-ETS-PART1-005-RELEASED-RECURSIVE-ASSOC-001`
  - `SCENARIO-ETS-PART1-005-RELEASED-MEDIA-GATE-001`
  - `SCENARIO-ETS-PART1-005-RELEASED-LINK-EXACT-001`
  - `SCENARIO-ETS-PART1-005-RELEASED-HIERARCHY-FAIL-CLOSED-001`
  - `SCENARIO-ETS-PART1-005-RELEASED-ASSOCIATION-LINK-001`
  - `SCENARIO-ETS-PART1-005-RELEASED-ASSOCIATION-ORACLE-001`
  - `SCENARIO-ETS-PART1-005-RELEASED-PROCEDURE-ISOLATION-001`
  - `SCENARIO-ETS-PART1-005-RELEASED-DEPENDENCY-CASCADE-001`
  - `SCENARIO-ETS-PART1-005-RELEASED-DEPENDENCY-CAUSAL-001`
  - `SCENARIO-ETS-PART1-005-RELEASED-DEPENDENCY-ARTIFACT-HYGIENE-001`
  - `SCENARIO-ETS-PART1-005-RELEASED-E2E-EXECUTION-001`
  - `SCENARIO-ETS-PART1-005-RELEASED-DIRECT-HTTP-COVERAGE-001`

## Acceptance Criteria

- [x] CP-011, this story, contract, capability requirement/scenarios, design,
  architecture, traceability, epic, baseline, and operations records define
  the increment before code.
- [x] Exactly five deployed methods implement the five released procedures.
- [x] Hierarchy discovery processes every root and direct-child page with a
  bounded same-origin traversal.
- [x] Hierarchy evidence gates status and actual media before parsing and uses
  released Deployment collection schemas.
- [x] Duplicate IDs, cycles, shortcut edges, and safety-bound violations fail.
- [x] Every parent with children exposes exact same-origin
  `rel=subdeployments` target occurrences.
- [x] Collection responses return HTTP 200 and pass the Deployment collection
  schema selected from actual media.
- [x] Recursive parameter sends exact boolean `true` and `false` values.
- [x] Default/false root results equal roots; true results equal all nodes.
- [x] Default/false child results equal direct children; true results equal all
  descendants.
- [x] Every advertised recursive association includes resources independently
  observed from all descendants.
- [x] Each procedure retrieves only its own prerequisites and has no method
  dependency.
- [x] Deployment prerequisite failures cascade before Subdeployment IUT access;
  unrelated sibling configurations cannot block Subdeployment.
- [x] All five methods have reviewed exact ATS mappings.
- [x] Focused/full Maven, coverage audit, local OSH TeamEngine E2E, controlled
  positive HTTP coverage, exact-image runtime, dependency, credential, and
  artifact-hygiene gates are archived.
- [x] Local OSH inherited Deployment and no-hierarchy outcomes remain visible
  and are not repaired through OSH or TeamEngine changes.
- [x] Explicit fixture evidence covers parent-owned and descendant-owned
  recursive association resources; absent evidence SKIPs.
- [x] Normalized Subdeployment target identity accepts equivalent default-port
  and unreserved-encoding forms while rejecting path/query/fragment defects.
- [x] Association selection prefers a usable same-origin JSON occurrence over
  earlier unsupported or cross-origin occurrences.
- [x] A causal controlled TestNG baseline/sabotage pair proves one injected
  Deployment failure changes setup and all five methods to pre-IUT SKIP.
- [x] The causal TestNG experiment confines reports to disposable temporary
  storage and leaves no repository-root `test-output/`.
- [x] Raze reports no unresolved required findings.

## Baseline

- `/deployments`, including `recursive=false` and `recursive=true`, returns
  HTTP 200 `application/json` with one root Deployment `040g`.
- `/deployments/040g/subdeployments`, including both recursive values, returns
  HTTP 200 `application/json` with an empty `items` array.
- `/deployments/040g` has no `rel=subdeployments` link.
- Existing TeamEngine dependency-skips all four historical Subdeployment
  methods because the released parent Deployment group has genuine failures.
- Deployed `/home/nh/docker/osh-core` is clean at `4c87a65`, zero commits ahead,
  three behind, and mounted read-only.

## Completion Evidence

- Coverage: `240/30 exact/2 helper/136 candidate/72 unmapped`;
  `/conf/subdeployment` is `5/5 exact`.
- Corrected focused Maven: `131/0/0/0`; full Maven: `480/0/0/3`.
- Final exact image: `sha256:e88aa5f9...b1dca`; runtime and immutable-base
  PASS.
- Primary local OSH TeamEngine: honest `219/39/5/175`; all five
  Subdeployment methods inherit SKIP before IUT access.
- Programmatic TestNG baseline reaches the IUT through all five methods;
  changing only one synthetic Deployment prerequisite to FAIL changes setup
  and all five methods to pre-IUT SKIP with zero IUT access.
- The earlier direct Deployment sabotage is retained only as historical,
  non-causal evidence because the baseline IUT already fails Deployment.
- Controlled HTTP, credential, and zero-write/zero-leak hygiene gates PASS.
- Durable summary:
  `ops/test-results/sprint-ets-51-part1-subdeployment-verification-2026-07-27.md`.
- Initial Raze: `GAPS_FOUND`, confidence `0.99`, four required fixes.
- All four findings are corrected and independently regression-tested; focused
  Raze recheck closed them but found repository-output and stale-record gaps.
- TestNG output now uses JUnit-managed temporary storage; corrected focused
  `131/0/0/0`, full `480/0/0/3`, and final TeamEngine `219/39/5/175` leave no
  repository-root `test-output/`.
- An independently committed clean source snapshot passes `480/0/0/3` and
  remains Git-clean with repository-root `test-output/` absent.
- Final Raze: `APPROVE_WITH_CONCERNS`, confidence `0.99`; all six findings are
  closed and no required fixes remain.
