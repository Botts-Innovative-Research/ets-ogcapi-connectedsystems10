# S-ETS-49-01: Part 1 Deployment Direct ATS Closure

## Status

COMPLETE

## User Instruction

Triggered by the accepted recommendation to complete released Part 1 gaps
continuously after Subsystem.

## Scope

Replace the historical Deployment approximations with exact implementations of
all five released OGC 23-001 `/conf/deployment` procedures.

- Requirements: `REQ-ETS-PART1-004`, `REQ-ETS-COVERAGE-001`,
  `REQ-ETS-VALIDATOR-001`
- Scenarios:
  - `SCENARIO-ETS-PART1-004-RELEASED-CANONICAL-URL-001`
  - `SCENARIO-ETS-PART1-004-RELEASED-RESOURCES-ENDPOINT-001`
  - `SCENARIO-ETS-PART1-004-RELEASED-CANONICAL-ENDPOINT-001`
  - `SCENARIO-ETS-PART1-004-RELEASED-COLLECTIONS-001`
  - `SCENARIO-ETS-PART1-004-RELEASED-REF-FROM-SYSTEM-001`
  - `SCENARIO-ETS-PART1-004-RELEASED-MEDIA-GATE-001`
  - `SCENARIO-ETS-PART1-004-RELEASED-COLLECTION-COMPLETE-001`
  - `SCENARIO-ETS-PART1-004-RELEASED-CANONICAL-EQUIVALENCE-001`
  - `SCENARIO-ETS-PART1-004-RELEASED-SYSTEM-REFERENCE-001`
  - `SCENARIO-ETS-PART1-004-RELEASED-PROCEDURE-ISOLATION-001`
  - `SCENARIO-ETS-PART1-004-RELEASED-DEPENDENCY-CASCADE-001`
  - `SCENARIO-ETS-PART1-004-RELEASED-E2E-EXECUTION-001`
  - `SCENARIO-ETS-PART1-004-RELEASED-DIRECT-HTTP-COVERAGE-001`

## Acceptance Criteria

- [x] CP-009, this story, contract, capability spec, design, architecture,
  traceability, epic, and operations records define the increment before code.
- [x] Exactly five deployed methods implement the five released procedures.
- [x] Canonical URL processing covers every item in every selected Deployment
  collection, accepts same-identity canonical representation variants, and
  compares canonical content after removing canonical links.
- [x] Resources, canonical, collection, and nested System endpoints gate every
  page by HTTP status and actual media before parsing.
- [x] Restricted collection retrieval prefers advertised GeoJSON/SensorML over
  an earlier generic JSON items link.
- [x] GeoJSON and SensorML pages use the released Deployment collection schemas.
- [x] No advertised Deployment collection cannot produce vacuous PASS.
- [x] Every System nested endpoint is required and every returned Deployment
  explicitly links to that System ID.
- [x] Shared setup cannot suppress an independently executable procedure.
- [x] API Common dependency failure cascades without restoring the historical
  all-Deployment dependency on System ATS outcomes; setup ignores unrelated
  and SystemFeatures configuration results.
- [x] All five methods have reviewed exact ATS mappings.
- [x] Focused/full Maven, coverage audit, local OSH TeamEngine E2E, controlled
  positive HTTP coverage, exact-image runtime, dependency, and credential gates
  are archived.
- [x] The local OSH's genuine missing-collection, unsupported-media, and nested
  HTTP 400 outcomes remain FAIL/SKIP evidence; they are not masked or repaired.
- [x] Raze reports no unresolved required findings.
- [x] Credential E2E consumes only current-run TestNG/container artifacts from
  `SMOKE_OUTPUT_DIR`; stale worktree evidence cannot satisfy the gate.

## Baseline

- `/collections`: HTTP 200 `application/json`; no collection advertises
  `featureType=sosa:Deployment`.
- `/deployments`: HTTP 200 `application/json`, unsupported by the released
  Deployment schema procedure.
- `/systems/040g/deployments`: HTTP 400,
  `Invalid resource name: 'deployments'`.
- Deployed `/home/nh/docker/osh-core`: clean at `4c87a65`.

## Completion Evidence

- Coverage: `240 total / 20 exact / 2 helper / 141 candidate / 77 unmapped`;
  `/conf/deployment` is `5/5 exact`.
- Focused Maven: remediation `35/0/0/0`; Deployment gate `90/0/0/0`.
- Full Maven: `434/0/0/3`.
- Exact image:
  `sha256:9049b284529b53845403e985fae2b03a9598073724320de2ad2e395006506d47`;
  runtime and immutable-base verification PASS.
- Primary local OSH TeamEngine: `217/39/3/175`. Deployment contributes three
  genuine FAIL and two unsupported-media SKIP outcomes.
- API Common sabotage: `217/34/1/182`; all five Deployment methods SKIP
  directly on `resourceIdsAreUniqueWithinEachType` before Deployment IUT access.
- Credential integration and wire gates PASS with zero unmasked artifact hits,
  39 masked events, and 39 intact synthetic transmissions.
- Positive and sabotage hygiene PASS with zero IUT writes.
- Final Raze found one documentation reconciliation gap after closing every
  behavioral finding. The focused documentation recheck closed
  `RAZE-S49-FINAL-001` with `APPROVE`, confidence `0.99`, and no remaining
  required findings.
