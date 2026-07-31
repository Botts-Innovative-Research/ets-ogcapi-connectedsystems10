# Story S-ETS-59-01: Part 2 API Common Released ATS Closure

> Status: DONE | Epic: epic-ets-03-part2-classes | Sprint: ets-59 | Last updated: 2026-07-31

## Context

The repository has completed the Part 1 exact-closure run through SensorML.
The next smallest coverage item is OGC 23-002 Part 2 API Common: two released
procedures, both already present as candidate mappings from the historical
Sprint 20 subset.

## Scope

Close `REQ-ETS-PART2-001` by review-mapping exactly:

1. `/conf/api-common/resources`
2. `/conf/api-common/resource-collection`

The work updates the historical Sprint 20 implementation so the deployed class
contains only released ATS procedure methods, depends directly on Part 1 API
Common, keeps declaration honesty inside each method, and preserves read-only
collection probing.

## Requirements

- Use official OGC 23-002 `v1.0.0` identifiers already pinned by
  `released-ats-inventory.json`.
- Keep `/conf/api-common` absence as an honest SKIP, not PASS.
- Probe only collection links advertised by the landing page.
- Keep probes same-origin with the IUT and read-only (`GET` plus `limit=1`).
- Do not use historical `dynamic-*` identifiers or synthesized `/commands`
  assumptions.
- Treat the documented Part 1 API Common datetime evidence limitation as
  inherited incomplete evidence that does not prevent independently executable
  Part 2 API Common procedures from running.

## Definition of Done

- [x] OpenSpec, design, traceability, status, changelog, metrics, and test
      results reflect Sprint 59 Part 2 API Common closure.
- [x] Focused tests fail against the historical four-method class and pass
      after implementation.
- [x] The TestNG group dependency is `part2apicommon -> part1apicommon`.
- [x] `reviewed-ats-mappings.json` and `ops/ats-coverage-report.json` show
      Part 2 API Common as `2 exact / 0 candidate / 0 unmapped`.
- [x] Full Docker Maven and the documented local OSH TeamEngine smoke are run.
- [x] Raze reviews the non-trivial change before completion.

## Implementation Notes

Implemented in `Part2ApiCommonTests` with exactly two released procedure
methods. Focused red reproduced the historical gap at `88 tests / 6 failures /
1 error / 0 skipped`; corrected focused verification passed `88/0/0/0`.
Coverage audit passed `23/0/0/0` and reports Part 2 API Common
`2 exact / 0 candidate / 0 unmapped`. Full Docker Maven passed
`735/0/0/3`. Local OSH TeamEngine smoke ran against the real unmodified IUT and
exited honestly non-green at `244/41/21/182`; both Part 2 API Common methods
SKIP because local OSH does not declare `/conf/api-common`. No-mutation oracle
recognized 194 IUT request logs and zero writes. Evidence:
`ops/test-results/sprint-ets-59-part2-api-common-2026-07-31/`.
Raze returned `APPROVE_WITH_CONCERNS 0.95` with no required fixes; the sole LOW
concern is raw Maven stdout archival, while totals are recorded in this story,
the contract, status, changelog, test-results, spec, and traceability.
