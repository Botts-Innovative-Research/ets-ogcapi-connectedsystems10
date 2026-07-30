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
  - `SCENARIO-ETS-PART1-010-ASYNC-DEADLINE-001`
  - `SCENARIO-ETS-PART1-010-ASYNC-COMPOUND-001`
  - `SCENARIO-ETS-PART1-010-INCONCLUSIVE-CLEANUP-001`
  - `SCENARIO-ETS-PART1-010-CUSTOM-URI-LIST-LATE-CLEANUP-001`
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
- [x] Each queued operation uses one monotonic deadline for every compound
  postcondition, HTTP timeout, capped sleep, late-success rejection, and
  interruption check.
- [x] Every first-page, pagination-page, and candidate request refuses expired
  or sub-millisecond budgets without upward timeout rounding.
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
- [x] Cleanup failure overrides accepted-but-inconclusive SKIP, and URI-list
  occurrence cleanup is registered before POST to remove late materialization.
- [x] Queued custom replace/delete setup awaits and cleanup-registers its
  custom occurrence before the later write.
- [x] A queued URI-list `Location` inside the target item namespace is
  verified and cleaned, while an asynchronous status `Location` is never
  dereferenced or deleted.
- [x] Generic non-System DELETE omits the System-specific cascade parameter.
- [ ] Focused/full Maven, coverage audit, exact-image runtime, dependency,
  credential, immutable-base, and artifact-hygiene gates complete.
- [x] Default primary local OSH TeamEngine E2E has zero writes.
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

Candidate
`0023d5b492dff8b5dbeff6c201c257f970b8947a` passes exact focused
`35/0/0/0`, full Docker Maven `637/0/0/3`, released-source, schema-parity,
image/runtime, dependency, credential, immutable-base, and artifact-hygiene
gates. Its image is
`sha256:4764227eda6ab91d5895df7bce74d440b0c95842127a0514debd67b857ed0744`
with manifest `Build-Revision: 0023d5b492`, but is superseded after Raze
returned `GAPS_FOUND 0.99`.

Exact-candidate local OSH E2E is populated `244/54/35/155` and clean primary
`244/40/7/197`. Provisioning and cleanup pass, primary state is unchanged, and
TeamEngine logs contain 365 IUT GETs and zero IUT writes. The IUT advertises
Part 1 `/conf/create-replace-delete`, but Part 1 API Common is `4 PASS / 1
SKIP`; causal inheritance therefore makes all twelve procedures dependency-
SKIP before CRD declaration checks. OSH additionally omits the exact Connected
Systems API Common and inherited `ogcapi-4` declarations; its
`ogcapi-features-4` declaration is only a near-match. This is correct
fail-closed behavior, not positive mutation E2E. The story stays IN PROGRESS
and mappings remain candidate.

Raze review of `0023d5b` returned `GAPS_FOUND 0.99`: polling was not a hard
wall-clock bound, cleanup failure could be hidden by an inconclusive SKIP,
compound queued postconditions were incomplete, URI-list cleanup was
registered too late, and CP-016 retained stale synchronous status semantics.
The remediation adds seven requirement-linked regressions. The initial red run
reported `25/6/0/0`; the interruption regression was tightened during
implementation. Exact candidate
`1a6c5ec30f76e120a0e2cd676f472699141213ca` passes focused `42/0/0/0`,
including direct controlled HTTP `25/0/0/0`, and full Docker Maven
`644/0/0/3`. Released-source, schema-parity, exact-image, runtime,
immutable-base, dependency sabotage, credential, and artifact-hygiene gates
pass. Its image is
`sha256:6939ef2ea40ff42328d4ff972b691dd4ba1c6a59855fe913d175b09f9555c1da`
with `Build-Revision: 1a6c5ec30f`.

The exact local OSH outcomes remain populated `244/54/35/155` and clean primary
`244/40/7/197`; provisioning and cleanup pass, primary state is unchanged, and
365 IUT requests are GETs with zero writes. Core sabotage is
`244/2/10/232`, with all twelve substantive CRD procedures causally skipped.
Credential and hygiene gates report zero leaks. These gates verify the
candidate implementation but do not supply positive real-IUT mutation
evidence, so the story and all twelve mappings remain IN PROGRESS/candidate.
Raze nevertheless returned `GAPS_FOUND 0.98`: API Common pagination and
candidate request boundaries could overrun the deadline, queued custom
replace/delete setup did not await or cleanup-register its occurrence, and a
distinct queued URI-list occurrence `Location` was discarded. Candidate
`1a6c5ec` is therefore superseded; its checksum-manifested exact evidence
remains historical audit material.

The next remediation adds six requirement-linked controlled regressions:
deadline expiry between pages, a sub-millisecond no-request boundary, delayed
custom replace and delete setup propagation, distinct queued occurrence
`Location` verification/cleanup, and asynchronous status `Location` non-use.
Test-first verification first failed compilation at the injected-clock
constructor, then reproduced `30/4/0/0`. A first green attempt exposed and was
aborted for an unbounded no-request-budget loop. The corrected direct HTTP
suite passes `31/0/0/0`, focused aggregate passes `48/0/0/0`, and full Docker
Maven passes `650/0/0/3` precommit. The
implementation floors timeout conversion, refuses every request boundary
without a whole-millisecond budget, awaits and pre-registers custom setup
occurrences, and classifies queued URI-list Locations by the direct target
item namespace. A new committed candidate must repeat every exact technical,
runtime, E2E, credential, immutability, hygiene, and Raze gate.

These controlled gates do not supply positive real-IUT mutation evidence, so
the story and all twelve mappings remain IN PROGRESS/candidate.
