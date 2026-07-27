# S-ETS-52-01: Part 1 Sampling Features Direct ATS Closure

## Status

COMPLETE

## User Instruction

Triggered by the accepted recommendation to complete released Part 1 gaps
continuously after Subdeployment.

## Scope

Replace the historical Sampling Features approximations with exact
implementations of all five released OGC 23-001 `/conf/sf` procedures.

- Requirements: `REQ-ETS-PART1-007`, `REQ-ETS-COVERAGE-001`,
  `REQ-ETS-VALIDATOR-001`
- Scenarios:
  - `SCENARIO-ETS-PART1-007-RELEASED-CANONICAL-URL-001`
  - `SCENARIO-ETS-PART1-007-RELEASED-RESOURCES-ENDPOINT-001`
  - `SCENARIO-ETS-PART1-007-RELEASED-CANONICAL-ENDPOINT-001`
  - `SCENARIO-ETS-PART1-007-RELEASED-COLLECTIONS-001`
  - `SCENARIO-ETS-PART1-007-RELEASED-REF-FROM-SYSTEM-001`
  - `SCENARIO-ETS-PART1-007-RELEASED-MEDIA-GATE-001`
  - `SCENARIO-ETS-PART1-007-RELEASED-CANONICAL-EQUIVALENCE-001`
  - `SCENARIO-ETS-PART1-007-RELEASED-COLLECTION-COMPLETE-001`
  - `SCENARIO-ETS-PART1-007-RELEASED-SYSTEM-COMPLETE-001`
  - `SCENARIO-ETS-PART1-007-RELEASED-PROCEDURE-ISOLATION-001`
  - `SCENARIO-ETS-PART1-007-RELEASED-DEPENDENCY-CASCADE-001`
  - `SCENARIO-ETS-PART1-007-RELEASED-E2E-EXECUTION-001`
  - `SCENARIO-ETS-PART1-007-RELEASED-DIRECT-HTTP-COVERAGE-001`

## Acceptance Criteria

- [x] CP-012, this story, contract, capability requirement/scenarios, design,
  architecture, traceability, epic, and baseline define the increment before
  code.
- [x] Exactly five deployed methods implement the five released procedures.
- [x] Every procedure retrieves only its own evidence and has no method
  dependency.
- [x] Every page is status/media gated before parsing and traversed with
  bounded same-origin pagination.
- [x] Supported Sampling Feature endpoint pages pass the released GeoJSON
  collection schema.
- [x] Every advertised `sosa:Sample` collection has exact metadata and a
  supported items representation or produces an explicit evidence SKIP.
- [x] Every collection item has exact canonical identity and equivalent
  dereferenced content after canonical-link normalization.
- [x] Every canonical System nested Sampling Features endpoint returns HTTP
  200 and is completely paginated.
- [x] System prerequisite failures cascade before Sampling Features IUT access;
  unrelated sibling configurations cannot block the class.
- [x] All five methods have reviewed exact ATS mappings.
- [x] Focused/full Maven, coverage audit, local OSH TeamEngine E2E, controlled
  HTTP, exact-image runtime, dependency, credential, and artifact-hygiene gates
  are archived.
- [x] Local OSH unsupported media and non-standard collection metadata remain
  visible and are not repaired through OSH or TeamEngine changes.
- [x] Focused Raze recheck reports no unresolved required findings.

## Baseline

- Local OSH declares `/conf/sf`.
- `/samplingFeatures` and `/systems/040g/samplingFeatures` return HTTP 200
  `application/json`.
- Negotiating `application/geo+json` still returns `application/json`, so
  released GeoJSON schema procedures must SKIP rather than parse generic JSON.
- `/collections` advertises `all_fois` with
  `featureType=featureOfInterest`, not `sosa:Sample`; the released collections
  requirement therefore has a genuine IUT failure.
- The seeded Sampling Feature has no top-level canonical link.
- Deployed OSH and TeamEngine source/binaries remain unmodified.

## Completion Evidence

- Coverage: `240/35 exact/2 helper/133 candidate/70 unmapped`; `/conf/sf` is
  `5/5 exact`.
- Test-first compile failed as expected; the prerequisite regression failed
  `6/1/0/0`, and the partial-collection regression failed `1/1/0/0` before
  their production corrections.
- Initial Raze returned `GAPS_FOUND` at confidence `0.98`.
  `RAZE-S52-001` and `RAZE-S52-002` are remediated by conditional nested
  GeoJSON validation, page-observer ordering, and narrow aggregate SKIPs.
- Gap-fix red evidence is `13/5/0/0` and `16/3/0/0`.
- Final focused Maven: `49/0/0/0`; full Maven: `506/0/0/3`.
- Final exact image: `sha256:ae3a7b6b...580ff3`; runtime, immutable-base,
  dependency, embedded-validator, and confidential-context gates pass.
- Primary unmodified local OSH TeamEngine: honest `220/40/6/174`. All five
  Sampling Features methods execute as one PASS, one FAIL, and three evidence
  SKIPs.
- Controlled HTTP executes all five positive paths and fail-closed media,
  metadata, schema, canonical, partial-collection, pagination, isolation, and
  dependency cases.
- System sabotage makes all five Sampling Features methods SKIP before their
  IUT access. Primary hygiene records 117 recognized requests, zero writes,
  and zero credential leaks.
- Credential integration and wire E2E pass with zero unmasked artifact hits,
  36 masked events, and 36 intact synthetic transmissions.
- OSH remains clean at `4c87a65`, zero commits ahead, with `/opt/osh`
  read-only.
- Durable summary:
  `ops/test-results/sprint-ets-52-part1-sampling-features-verification-2026-07-27.md`.
- Focused Raze recheck: `APPROVE`, confidence `0.99`, duration 589 seconds;
  both findings closed, no new findings, and no required fixes.
