# S-ETS-56-01: Part 1 Create/Replace/Delete Direct ATS Closure

## Status

IN PROGRESS

## User Instruction

Triggered by the user's instruction to continue the accepted released-ATS
completion sequence after Sprint 55.

## Scope

Replace the historical Systems-only Create/Replace/Delete subset with exact,
safety-gated implementations of all twelve released OGC 23-001
`/conf/create-replace-delete` procedures.

- Requirements: `REQ-ETS-PART1-010`, `REQ-ETS-COVERAGE-001`
- Scenarios:
  - `SCENARIO-ETS-PART1-010-RELEASED-PROCEDURES-001`
  - `SCENARIO-ETS-PART1-010-DIRECT-PREREQUISITES-001`
  - `SCENARIO-ETS-PART1-010-DEPENDENCY-CAUSAL-001`
  - `SCENARIO-ETS-PART1-010-MUTATION-SAFETY-001`
  - `SCENARIO-ETS-PART1-010-INHERITED-TRANSACTION-001`
  - `SCENARIO-ETS-PART1-010-REPRESENTATION-CLOSURE-001`
  - `SCENARIO-ETS-PART1-010-CASCADE-001`
  - `SCENARIO-ETS-PART1-010-NESTED-CANONICAL-001`
  - `SCENARIO-ETS-PART1-010-CUSTOM-CREATE-001`
  - `SCENARIO-ETS-PART1-010-CUSTOM-REPLACE-001`
  - `SCENARIO-ETS-PART1-010-CUSTOM-DELETE-001`
  - `SCENARIO-ETS-PART1-010-CUSTOM-URI-LIST-001`
  - `SCENARIO-ETS-PART1-010-CLEANUP-001`
  - `SCENARIO-ETS-PART1-010-DIRECT-HTTP-COVERAGE-001`
  - `SCENARIO-ETS-PART1-010-E2E-ISOLATION-001`

## Acceptance Criteria

- [x] CP-016, this story, contract, capability scenarios, design,
  architecture, traceability, and epic define the increment before code.
- [x] Exactly twelve independent TestNG methods map the twelve released
  procedures without `alwaysRun`.
- [x] A failed API Common TestNG prerequisite skips all twelve methods before
  IUT access while passing prerequisites leave sibling methods independent.
- [x] Every method checks its own declarations, condition, and mutation gate
  before writes.
- [x] The exact released Annex A `ogcapi-4` inheritance URI is required; the
  `ogcapi-features-4` near-match is rejected.
- [x] API Common is the only direct local TestNG prerequisite.
- [x] The generic transaction helper accepts immediate and queued
  POST/PUT/DELETE responses, but reports positive lifecycle evidence only after
  bounded, configurable polling observes the required postcondition.
- [x] Applicable declared resource representations are exercised without
  importing another executable ETS jar.
- [x] Every generated request fixture passes bundled released schema
  validation before write.
- [x] Both released System cascade graphs are tested with exact 409 and
  postcondition semantics, including proof that both Deployment references
  existed before deletion.
- [x] Subsystem, subdeployment, and Sampling Feature nested creation is
  verified through derived root canonical resources even when Location is
  nested.
- [x] Custom-collection create, replace, root/non-root delete propagation, and
  `text/uri-list` behavior cover every advertised applicable resource type,
  both custom and canonical URLs, OPTIONS, HTTP 201, and Location dereference.
- [x] No-evidence custom-collection outcomes SKIP; malformed or incorrect
  advertised behavior fails.
- [x] Owned resources are cleaned in reverse order after pass and failure, and
  cleanup failures remain visible.
- [x] Cleanup dereferences and identity-verifies returned Location before
  destructive use; missing or unrelated Location falls back to root discovery
  by identity without deleting unrelated resources; deleting a verified alias
  is followed by root identity discovery.
- [x] Generic non-System DELETE omits the System-specific cascade parameter.
- [ ] Focused/full Maven, coverage audit, exact-image runtime, dependency,
  credential, immutable-base, and artifact-hygiene gates complete.
- [ ] Default primary local OSH TeamEngine E2E has zero writes.
- [ ] Owned isolated local OSH TeamEngine E2E executes mutation paths, records
  honest conformance outcomes, cleans up, proves primary state unchanged, and
  is followed by clean-primary smoke.
- [ ] All twelve mappings are reviewed exact only after positive mutation E2E
  executes; until then they remain candidate.
- [ ] Raze reports no unresolved required findings.

## Baseline

- Released source commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2` defines twelve procedures.
- The inherited Features Part 4 draft source is tag
  `part4-1.0.0-draft.1`, commit
  `ea42aa1de6d8cbb53c526f41e1f66c1887fe71d4`.
- Current class coverage is `0 exact / 1 candidate / 11 unmapped`.
- The historical class has six methods and only one guarded System lifecycle.
- The isolated Sprint 44 workflow already provides owned OSH state, explicit
  mutation gates, cleanup, primary-state fingerprints, and clean-primary
  verification.

## Completion Evidence

Candidate `f6e3587` is superseded after Raze found incomplete queued-response
semantics and an alias-Location cleanup gap. It passed focused `28/0/0/0`,
full Docker Maven `630/0/0/3`, released
source, schema parity, image/runtime, dependency, credential, immutable-base,
and artifact-hygiene gates. All twelve procedures execute through the
controlled HTTP harness, including schema-valid fixtures, identity-safe
cleanup, cascade preconditions, nested canonical URLs, and custom-collection
negative cases. Coverage is honestly `0 exact / 0 helper / 12 candidate / 0
unmapped` for this class.

Exact-candidate local OSH E2E is populated `244/54/35/155` and clean primary
`244/40/7/197`. Provisioning and cleanup pass, primary state is unchanged, and
TeamEngine logs contain zero IUT writes. Part 1 API Common is `4 PASS / 1
SKIP`; causal inheritance therefore makes all twelve procedures dependency-
SKIP before CRD declaration checks. OSH additionally omits the exact Connected
Systems API Common and inherited `ogcapi-4` declarations. This is correct
fail-closed behavior, not positive mutation E2E. Corrective focused CRD passes
`35/0/0/0`, including delayed/stalled 202, alias-leak, and deployment-property
regressions at `18/0/0/0`; full Docker Maven is `637/0/0/3`. Replacement exact
gates and fresh Raze remain pending; the story stays IN PROGRESS and mappings
remain candidate.
