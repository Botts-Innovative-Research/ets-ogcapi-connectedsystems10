# S-ETS-57-01: Part 1 Update Direct ATS Closure

## Status

IN PROGRESS

## User Instruction

Triggered by the user's instruction to push the reviewed OSH feasibility audit
and start Sprint 57.

## Scope

Replace the historical System-only Update subset with exact, safety-gated
implementations of all five released OGC 23-001 `/conf/update` procedures.

- Requirements: `REQ-ETS-PART1-011`, `REQ-ETS-COVERAGE-001`
- Scenarios:
  - `SCENARIO-ETS-PART1-011-RELEASED-PROCEDURES-001`
  - `SCENARIO-ETS-PART1-011-DIRECT-PREREQUISITES-001`
  - `SCENARIO-ETS-PART1-011-DEPENDENCY-CAUSAL-001`
  - `SCENARIO-ETS-PART1-011-MUTATION-SAFETY-001`
  - `SCENARIO-ETS-PART1-011-PATCH-NEGOTIATION-001`
  - `SCENARIO-ETS-PART1-011-PARTIAL-UPDATE-001`
  - `SCENARIO-ETS-PART1-011-CUSTOM-COLLECTIONS-001`
  - `SCENARIO-ETS-PART1-011-APPLICABILITY-EXACT-001`
  - `SCENARIO-ETS-PART1-011-ASYNC-DEADLINE-001`
  - `SCENARIO-ETS-PART1-011-CLEANUP-001`
  - `SCENARIO-ETS-PART1-011-FIXTURE-ACQUISITION-001`
  - `SCENARIO-ETS-PART1-011-DIRECT-HTTP-COVERAGE-001`
  - `SCENARIO-ETS-PART1-011-E2E-ISOLATION-001`

## Acceptance Criteria

- [x] CP-017, this story, contract, capability scenarios, design,
  architecture, traceability, and epic define the increment before code.
- [ ] Exactly five independent TestNG methods map the five released
  procedures without `alwaysRun`.
- [ ] API Common is the only direct TestNG prerequisite; a failed prerequisite
  skips all five methods before IUT access.
- [ ] Every procedure checks exact declarations, applicability, and mutation
  ownership before writes.
- [ ] The exact released `ogcapi-4` inheritance URI is required and the
  `ogcapi-features-4` near-match is rejected.
- [ ] Canonical and every advertised applicable custom item endpoint are
  discovered with bounded same-origin pagination and exercised for System,
  Deployment, Procedure, Sampling Feature, and Property.
- [ ] OPTIONS requires HTTP 200 plus PATCH in `Allow`.
- [ ] Every implemented JSON Merge Patch and JSON Patch format advertised by
  `Accept-Patch` or exact OpenAPI metadata is exercised; ordinary resource
  media types are not treated as patch documents.
- [ ] HTTP 200/202/204 are handled, but PASS requires GET evidence that the
  intended field changed, an untouched sentinel and external identity were
  preserved, and a conflicting submitted `id` was ignored.
- [ ] Custom endpoint PASS requires canonical and occurrence propagation in
  two consecutive complete observations using each endpoint's own pre-update
  sentinel baseline.
- [ ] Queued updates use one bounded monotonic deadline for every required
  postcondition, request timeout, and sleep.
- [ ] Owned schema-valid fixtures are cleaned in reverse order after every
  outcome; ambiguous POST polls canonical and custom identity views, and
  destructive cleanup requires current same-origin identity plus disappearance
  proof.
- [ ] Delayed and custom-only ambiguous commits are cleaned, one route failure
  does not suppress remaining safe cleanup, and route failures are aggregated.
- [ ] Fixture acquisition denial or unusable response SKIPs before PATCH rather
  than failing Update conformance.
- [ ] Custom applicability accepts only exact compact/canonical SOSA types,
  and repeated `Allow` fields are combined.
- [ ] Focused/full Maven, coverage, released-source, exact-image runtime,
  dependency, credential, immutable-base, and artifact-hygiene gates complete.
- [ ] Default local OSH TeamEngine E2E records honest Update outcomes and zero
  unauthorized IUT writes.
- [ ] Positive isolated real-IUT PATCH evidence executes before mappings are
  promoted from candidate to reviewed exact.
- [ ] Raze reports no unresolved required findings.

## Baseline

- Released source tag `v1.0.0`, commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`, defines five procedures.
- Inherited Features Part 4 draft tag `part4-1.0.0-draft.1`, commit
  `ea42aa1de6d8cbb53c526f41e1f66c1887fe71d4`, defines PATCH, OPTIONS
  advertisement, partial request bodies, identifier-ignore behavior, and
  HTTP 200/202/204, while explicitly leaving patch encoding open.
- Current coverage is `0 exact / 1 candidate / 4 unmapped`.
- The historical class has five methods but only one guarded System lifecycle
  and an incorrect TestNG dependency on Create/Replace/Delete.
- Unmodified local OSH does not currently provide the exact prerequisite and
  Update declarations needed for positive PATCH E2E.

## Completion Evidence

Candidate `b9143a4` passed focused `93/0/0/0` and full clean Maven
`668/0/0/3`, but Raze `GAPS_FOUND 0.99` supersedes it. Required remediation
covers fixture verdicts, ambiguous POST cleanup, synchronous DELETE
postconditions, immediate ownership revalidation, exact collection types,
stable synchronous custom propagation, and repeated `Allow` fields.

Candidate `cbfa070` passed the remediated focused HTTP suite `18/0/0/0`, the
complete Update suite `24/0/0/0`, and clean Maven `675/0/0/3`. Exact-image,
dependency-sabotage, and both credential gates also passed. Raze
`GAPS_FOUND 0.98` supersedes it because delayed/custom-only ambiguous commits,
independent cleanup-route attempts, and endpoint-specific sentinel baselines
still require test-first closure.

The second remediation reproduced all four added cases at `22/4/0/0`, then
passed controlled HTTP `22/0/0/0`, the complete Update surface `28/0/0/0`, and
clean Docker Maven `679/0/0/3`. Released-source inventory, coverage audit,
audit self-tests, formatting, and TeamEngine jar guard pass precommit.
