# S-ETS-55-01: Part 1 Advanced Filtering Direct ATS Closure

## Status

IN PROGRESS - final Raze recheck reopened semantic and exact-build provenance
gates.

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
- [x] UID-prefix queries derive a non-empty shorter prefix, retain the known
  match, and reject non-prefix results on every page.
- [ ] Keyword evidence is limited to resource-boundary `name`, `description`,
  or SensorML `label`; links and nested extensions cannot satisfy `q`.
- [x] Recommendation procedures emit visible warnings without converting
  recommendation non-support into conformance failure.
- [x] Geometry predicates use parsed JTS intersection, not response-shape
  smoke.
- [ ] Every association procedure verifies local-ID and UID forms against
  procedure-specific relation evidence.
- [ ] Same-origin resolved association targets supply their representation ID
  and UID; wrapper IDs, path tokens, and hrefs are not synthetic substitutes.
- [ ] Procedure-specific deployed-System, features-of-interest, Datastream, and
  ControlStream paths cannot be replaced by root aliases.
- [ ] Combined filters enumerate every applicable inherited, mandatory
  class-specific, and positively supported custom predicate, then prove every
  evidenced pair uses logical AND.
- [x] Indirect property and feature-of-interest recommendations evaluate
  transitive result-set inclusion for every eligible resource.
- [x] Recursive association traversal fails explicitly on depth, cycle, or
  reference-read limit exhaustion.
- [ ] Every collection and association page is status/media gated before
  parsing and traversed through bounded same-origin pagination.
- [x] Known matching seeds cannot yield a vacuous empty-result PASS.
- [x] Cross-origin association targets never receive IUT credentials.
- [ ] All 25 methods have accurate reviewed exact ATS mappings.
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
- The post-Raze regression red is `20/7/0/0`; remediation is `20/0/0/0`.
  Focused Maven is `102/0/0/0`; full Docker Maven is `574/0/0/3`.
- Exact image
  `sha256:c39a9c35120064e6be41eaf11c677f77566fd0203849dc0e498995e0b63f08ae`
  was built from `cf7fa82745615f1c27ec8df57bd1d315db673955`.
- Unmodified local OSH TeamEngine is honestly `238/40/7/191`. The seven
  failures match the established baseline. All 25 Advanced Filtering methods
  execute exactly once and SKIP at the missing-declaration boundary.
- Controlled HTTP, API Common sabotage, runtime immutability, no-mutation,
  credential integration/wire, and artifact-hygiene gates pass. Primary E2E
  records 169 IUT GETs, zero writes, and zero leaks.
- OSH remains clean at `4c87a65`; `/opt/osh` is read-only. No OSH or
  TeamEngine source or binary was modified, and no hosted CI was added.
- Initial Raze found seven semantic gaps and the first remediation closed four
  fully and three only partially. Final Raze recheck `GAPS_FOUND 0.99` reopened
  association provenance/path specificity, reference media/pagination,
  combined-predicate completeness, keyword provenance, mapping/scenario
  traceability, exact-build provenance, and honest positive-IUT wording.
