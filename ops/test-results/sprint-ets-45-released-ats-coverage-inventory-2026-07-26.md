# Sprint ETS-45 Released ATS Coverage Inventory Verification

Date: 2026-07-26

## Authority

- Standards: OGC 23-001 and OGC 23-002, version 1.0
- Source tag: `v1.0.0`
- Source commit: `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`
- Part 1 PDF SHA-256:
  `c444bff07193daf8ce880077b1d728127868b48c056fe35278129e04d439f9e4`
- Part 2 PDF SHA-256:
  `78531c637053890dd501bb153a0046261b9c03fa064d0888a39e2b0dc383d154`

## Inventory Result

| Part | Classes | Tests | Supporting | Semantic SHA-256 |
|------|---------|-------|------------|-----------------|
| 1 | 13 | 110 | 2 | `25f2aa8f2f8218593d5371193ae7d5859105f6615cf6c42111c8af052eb9e418` |
| 2 | 12 | 130 | 0 | `5d57d3824a5e5d415b5de79b2eb82fbd06aea325340014d2a6c99c4ff18cd5af` |

The source audit self-test passes. Reproduction from the exact source checkout
matches the committed manifest.

## Coverage Baseline

| Part | Released | Exact | Helper | Candidate | Unmapped |
|------|----------|-------|--------|-----------|----------|
| 1 | 110 | 0 | 0 | 49 | 61 |
| 2 | 130 | 0 | 0 | 101 | 29 |
| Total | 240 | 0 | 0 | 150 | 90 |

The suite contains 211 compiled TestNG test methods. URI matches are candidates
only. The empty reviewed mapping baseline deliberately prevents historical
partial implementations from being promoted without a method-by-method review.

## Test-First Evidence

Before the inventory, reviewed mapping file, report, and audit script existed,
the focused JUnit run failed as expected:

`5 tests / 5 failures / 0 errors / 0 skipped`

Raw Surefire XML:
`ops/test-results/sprint-ets-45-ats-inventory-test-first-2026-07-26.xml`

## Corrected Focused Gate

- Audit self-test: PASS
- Exact released-source reproduction: PASS
- `VerifyReleasedAtsCoverage`: `23 tests / 0 failures / 0 errors / 0 skipped`

## Full Gates

- Full Docker Maven: PASS,
  `345 tests / 0 failures / 0 errors / 3 skipped`
- Exact candidate image:
  `sha256:ad2594ef5f41beadc5f9de59c8caba27d3af1116d3732892fd498860ee23749c`
- Real local OSH TeamEngine E2E: PASS,
  `211 total / 69 passed / 0 failed / 142 skipped`
- Real protocol evidence: `135` recognized IUT requests, zero
  POST/PUT/PATCH/DELETE requests, and zero startup ERROR/SEVERE entries
- Exact-image runtime verifier: PASS for deployed SWE Common adapter execution,
  dependency parity, reviewed collision inventory, byte-for-byte TeamEngine
  base immutability, runtime invariants, and confidential context hygiene

Artifacts:

- `ops/test-results/sprint-ets-45-full-maven-2026-07-26.log`
- `ops/test-results/sprint-ets-45-local-osh-teamengine-2026-07-26.xml`
- `ops/test-results/sprint-ets-45-local-osh-teamengine-container-2026-07-26.log`
- `ops/test-results/sprint-ets-45-final-focused-maven-2026-07-26.xml`
- `ops/test-results/sprint-ets-45-final-full-maven-2026-07-26.log`
- `ops/test-results/sprint-ets-45-final-local-osh-smoke-stdout-2026-07-26.log`
- `ops/test-results/sprint-ets-45-final-local-osh-teamengine-2026-07-26.xml`
- `ops/test-results/sprint-ets-45-final-local-osh-teamengine-container-2026-07-26.log`
- `ops/test-results/sprint-ets-45-final-runtime-verifier-2026-07-26.log`
- `ops/test-results/sprint-ets-45-final-raze2-gapfix-test-first-2026-07-26.xml`
- `ops/test-results/sprint-ets-45-final-raze2-gapfix-focused-2026-07-26.xml`
- `ops/test-results/sprint-ets-45-final-raze2-gapfix-full-maven-2026-07-26.log`
- `ops/test-results/sprint-ets-45-final-raze2-gapfix-local-osh-smoke-stdout-2026-07-26.log`
- `ops/test-results/sprint-ets-45-final-raze2-gapfix-local-osh-teamengine-2026-07-26.xml`
- `ops/test-results/sprint-ets-45-final-raze2-gapfix-local-osh-teamengine-container-2026-07-26.log`
- `ops/test-results/sprint-ets-45-final-raze2-gapfix-runtime-verifier-2026-07-26.log`

## Adversarial Review

- Initial Raze: `GAPS_FOUND`, confidence `0.99`, duration 937 seconds.
- First recheck: `GAPS_FOUND`, confidence `0.99`, duration 426 seconds.
- Findings `RAZE-S45-001` through `RAZE-S45-005` now have executable
  remediations and fresh full-gate evidence.
- Final recheck: `APPROVE`, confidence `0.99`, duration 334 seconds; all
  findings closed and no new findings.
