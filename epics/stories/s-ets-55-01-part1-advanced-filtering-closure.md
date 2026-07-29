# S-ETS-55-01: Part 1 Advanced Filtering Direct ATS Closure

## Status

IN PROGRESS

## User Instruction

Triggered by the user's instruction to continue the accepted released-ATS
completion sequence after Sprint 54.

## Scope

Replace the historical Advanced Filtering subset with exact implementations of
all 25 released OGC 23-001 `/conf/advanced-filtering` procedures.

- Requirements: `REQ-ETS-PART1-009`, `REQ-ETS-COVERAGE-001`
- Scenarios:
  - `SCENARIO-ETS-PART1-009-RELEASED-ID-LIST-001`
  - `SCENARIO-ETS-PART1-009-RELEASED-COMMON-FILTERS-001`
  - `SCENARIO-ETS-PART1-009-RELEASED-GEOMETRY-001`
  - `SCENARIO-ETS-PART1-009-RELEASED-SYSTEM-ASSOCIATIONS-001`
  - `SCENARIO-ETS-PART1-009-RELEASED-DEPLOYMENT-ASSOCIATIONS-001`
  - `SCENARIO-ETS-PART1-009-RELEASED-PROCEDURE-ASSOCIATIONS-001`
  - `SCENARIO-ETS-PART1-009-RELEASED-SF-ASSOCIATIONS-001`
  - `SCENARIO-ETS-PART1-009-RELEASED-PROPERTY-FILTERS-001`
  - `SCENARIO-ETS-PART1-009-RELEASED-COMBINED-FILTERS-001`
  - `SCENARIO-ETS-PART1-009-RELEASED-INDIRECT-RECOMMENDATIONS-001`
  - `SCENARIO-ETS-PART1-009-RELEASED-MEDIA-PAGINATION-001`
  - `SCENARIO-ETS-PART1-009-RELEASED-PROCEDURE-ISOLATION-001`
  - `SCENARIO-ETS-PART1-009-RELEASED-DEPENDENCY-CASCADE-001`
  - `SCENARIO-ETS-PART1-009-RELEASED-DIRECT-HTTP-COVERAGE-001`
  - `SCENARIO-ETS-PART1-009-RELEASED-E2E-EXECUTION-001`

## Acceptance Criteria

- [x] CP-015, this story, contract, capability scenarios, design,
  architecture, traceability, epic, and baseline define the increment before
  code.
- [ ] Exactly 25 deployed methods implement the 25 released procedures.
- [ ] Every procedure retrieves only its own evidence and has no method
  dependency.
- [ ] Released direct inheritance is API Common; System and siblings cannot
  block the class.
- [ ] Common ID and keyword predicates cover every declared canonical Part 1
  endpoint, including local IDs, UIDs, and UID prefixes.
- [ ] UID-prefix queries derive a non-empty shorter prefix, retain the known
  match, and reject non-prefix results on every page.
- [ ] Keyword evidence is limited to `name`, `description`, or SensorML
  `label`; unrelated scalar extensions cannot satisfy `q`.
- [ ] Recommendation procedures emit visible warnings without converting
  recommendation non-support into conformance failure.
- [ ] Geometry predicates use parsed JTS intersection, not response-shape
  smoke.
- [ ] Every association procedure verifies local-ID and UID forms against
  procedure-specific relation evidence.
- [ ] Same-origin resolved association targets supply their representation ID
  and UID; path tokens and hrefs are not synthetic substitutes.
- [ ] Combined filters prove logical AND semantics for at least two distinct,
  independently evidenced combinations on every inspectable canonical
  endpoint.
- [ ] Indirect property and feature-of-interest recommendations evaluate
  transitive result-set inclusion for every eligible resource.
- [ ] Recursive association traversal fails explicitly on depth, cycle, or
  reference-read limit exhaustion.
- [ ] Every page is status/media gated before parsing and traversed through
  bounded same-origin pagination.
- [ ] Known matching seeds cannot yield a vacuous empty-result PASS.
- [ ] Cross-origin association targets never receive IUT credentials.
- [ ] All 25 methods have reviewed exact ATS mappings.
- [ ] Focused/full Maven, coverage audit, local OSH TeamEngine E2E, controlled
  HTTP, exact-image runtime, dependency, credential, and artifact-hygiene gates
  are archived.
- [ ] Local OSH's undeclared class remains visible and is not repaired through
  OSH or TeamEngine changes.
- [ ] Raze reports no unresolved required findings.

## Baseline

- Released source commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2` defines 25 procedures.
- Current coverage is `0 exact / 4 candidate / 21 unmapped` for this class.
- The historical class has six methods and depends on System.
- Unmodified local OSH does not declare Part 1 `/conf/advanced-filtering`.
- Every deployed procedure is therefore expected to SKIP at its declaration
  boundary in primary E2E; controlled HTTP supplies positive procedure
  evidence.

## Completion Evidence

Pending implementation and all mandatory gates.
