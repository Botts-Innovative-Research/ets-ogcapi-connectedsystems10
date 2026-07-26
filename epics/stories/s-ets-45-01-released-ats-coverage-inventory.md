# S-ETS-45-01: Released ATS Coverage Inventory

## Status

COMPLETE

## User Instruction

Triggered by the accepted recommendation to build a formal normative Annex A
coverage inventory, then continuously close Part 1 and Part 2 gaps.

## Scope

Establish the released standards as the coverage authority and produce a
reproducible, machine-readable baseline before adding further conformance tests.

- Requirement: `REQ-ETS-COVERAGE-001`
- Scenarios:
  - `SCENARIO-ETS-COVERAGE-RELEASED-SOURCES-001`
  - `SCENARIO-ETS-COVERAGE-EXACT-INVENTORY-001`
  - `SCENARIO-ETS-COVERAGE-COMPILED-MAPPING-001`
  - `SCENARIO-ETS-COVERAGE-FAIL-CLOSED-001`
  - `SCENARIO-ETS-COVERAGE-STATUS-HONESTY-001`

## Acceptance Criteria

- [x] Pin approved OGC 23-001/23-002 source and document hashes separately from
  the newer OpenAPI input pin.
- [x] Extract exactly 13 Part 1 classes and 110 tests, including two supporting
  tests.
- [x] Extract exactly 12 Part 2 classes and 130 tests.
- [x] Commit every identifier, target, class membership, and source provenance
  in a deterministic manifest.
- [x] Map compiled TestNG annotation descriptions to ATS targets without
  treating source-string occurrence as implementation proof.
- [x] Distinguish exact, candidate, helper, and unmapped coverage.
- [x] Fail on duplicate/missing inventory entries, unknown mappings, source
  semantic drift, or an implemented claim without an exact mapping.
- [x] Correct stale class counts and status claims in active planning documents.
- [x] Model current TestNG deployment semantics with unambiguous signatures and
  an explicit approved-helper registry; fail closed on unsupported features.
- [x] Run focused/full Maven and real local OSH TeamEngine E2E.
- [x] Obtain Raze review and close all required findings.

## Baseline

The released inventory contains `240` tests. Compiled annotation discovery
finds `150` target-URI candidates and `90` unmapped tests:

| Part | Released tests | Candidate | Unmapped |
|------|----------------|-----------|----------|
| 1 | 110 | 49 | 61 |
| 2 | 130 | 101 | 29 |

The reviewed mapping file is intentionally empty at this baseline, so exact and
helper counts remain zero. Candidate mappings are review leads, not implemented
claims. The per-test and per-class backlog is
`ops/ats-coverage-report.json`.

## Verification

- Focused Maven: `23/0/0/0`.
- Fresh full Docker Maven: `345/0/0/3`.
- Exact image runtime verifier: PASS for
  `sha256:ad2594ef5f41beadc5f9de59c8caba27d3af1116d3732892fd498860ee23749c`.
- Fresh-clone local OSH TeamEngine E2E: `211/69/0/142`, 135 recognized IUT
  requests, zero writes, and zero startup errors.
- Initial Raze found five gaps. The first recheck closed three and exposed
  incomplete status-variant, constructor-factory, inherited-class, and
  `@Ignore` handling. All findings have executable remediations. Final Raze
  returned `APPROVE`, confidence `0.99`, with no new findings.

## Non-Goals

- Claiming complete Part 1 or Part 2 coverage in this inventory increment.
- Treating the frozen web application's registry as normative.
- Modifying OSH or TeamEngine.
- Adding hosted CI.
