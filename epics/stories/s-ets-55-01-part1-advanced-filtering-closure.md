# S-ETS-55-01: Part 1 Advanced Filtering Direct ATS Closure

## Status

REMEDIATION IN PROGRESS - final Raze R2 reopened declaration applicability,
association-target identity, complete combined-predicate inventory, evidence
provenance, and scenario traceability.

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
  - `SCENARIO-ETS-PART1-009-RELEASED-OWNER-APPLICABILITY-001`
  - `SCENARIO-ETS-PART1-009-RELEASED-UID-PREFIX-001`
  - `SCENARIO-ETS-PART1-009-RELEASED-KEYWORD-SOURCE-001`
  - `SCENARIO-ETS-PART1-009-RELEASED-GEOMETRY-001`
  - `SCENARIO-ETS-PART1-009-RELEASED-SYSTEM-ASSOCIATIONS-001`
  - `SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PROVENANCE-001`
  - `SCENARIO-ETS-PART1-009-RELEASED-ASSOCIATION-PATHS-001`
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
- [x] Exactly 25 deployed methods implement the 25 released procedures.
- [x] Every procedure retrieves only its own evidence and has no method
  dependency.
- [x] Released direct inheritance is API Common; System and siblings cannot
  block the class.
- [x] Common ID and keyword predicates cover every declared canonical Part 1
  endpoint, including local IDs, UIDs, and UID prefixes.
- [x] Endpoint applicability follows owner-class declarations; a declared
  unavailable endpoint fails and an undeclared reachable endpoint is not
  queried.
- [x] UID-prefix queries derive a non-empty shorter prefix, retain the known
  match, and reject non-prefix results on every page.
- [x] Keyword evidence is limited to resource-boundary `name`, `description`,
  or SensorML `label`; links and nested extensions cannot satisfy `q`.
- [x] Recommendation procedures emit visible warnings without converting
  recommendation non-support into conformance failure.
- [x] Geometry predicates use parsed JTS intersection, not response-shape
  smoke.
- [x] Every association procedure verifies local-ID and UID forms against
  procedure-specific relation evidence.
- [x] Same-origin resolved association targets supply their representation ID
  and UID; wrapper IDs, path tokens, and hrefs are not synthetic substitutes.
- [x] Procedure-specific deployed-System, features-of-interest, Datastream, and
  ControlStream paths cannot be replaced by root aliases.
- [x] Combined filters enumerate every applicable inherited, mandatory
  class-specific, and positively supported custom predicate, then prove every
  evidenced pair uses logical AND.
- [x] Indirect property and feature-of-interest recommendations evaluate
  transitive result-set inclusion for every eligible resource.
- [x] Recursive association traversal fails explicitly on depth, cycle, or
  reference-read limit exhaustion.
- [x] Every collection and association page is status/media gated before
  parsing and traversed through bounded same-origin pagination.
- [x] Known matching seeds cannot yield a vacuous empty-result PASS.
- [x] Cross-origin association targets never receive IUT credentials.
- [x] All 25 methods have accurate reviewed exact ATS mappings.
- [ ] Focused/full Maven, coverage audit, local OSH TeamEngine E2E, controlled
  HTTP, exact-image runtime, dependency, credential, and artifact-hygiene gates
  are archived from the final exact committed candidate.
- [x] Local OSH's undeclared class remains visible and is not repaired through
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

- Exactly 25 methods have reviewed exact mappings. Coverage is
  `240/76 exact/2 helper/115 candidate/47 unmapped`; Advanced Filtering is
  `25/25 exact`.
- The second final-Raze regression baseline is `33/6/0/0`; remediation and
  focused controlled HTTP are `33/0/0/0`. Precommit full Docker Maven is
  `587/0/0/3`, and scenario trace is `20/20` with no missing Java anchors.
- Candidate `29753ca85c` and image `sha256:709b5f664d8...23aa0` are
  superseded audit evidence. Exact-image, unmodified-local-OSH TeamEngine,
  sabotage, credential, hygiene, immutability, and runtime gates must be rerun
  from the new committed candidate.
- No OSH or TeamEngine source or binary is modified, and no hosted CI is
  added.
- Initial Raze found seven semantic gaps. Final Raze recheck
  `GAPS_FOUND 0.99` identified association provenance/path specificity,
  reference media/pagination, combined-predicate completeness, keyword
  provenance, mapping/scenario traceability, exact-build provenance, and
  honest positive-IUT wording. Those findings now have requirement-linked
  regressions, implementation fixes, and reconciled mappings. Exact-candidate
  gates and a fresh final Raze review remain pending.
