# CP-019 — Part 2 API Common Released ATS Closure

**Status**: Implemented and gate-closed
**Date**: 2026-07-31
**Capability**: `ets-ogcapi-connectedsystems`
**Requirement**: `REQ-ETS-PART2-001`
**Story**: `S-ETS-59-01`

## Problem

Sprint 20 implemented a useful read-only Part 2 API Common subset, but the
released ATS coverage audit still treats both OGC 23-002 `/conf/api-common`
procedures as candidate mappings. The class also carries historical non-ATS
declaration/dependency tracer methods and a TestNG dependency on `core common`
instead of the released prerequisite, Part 1 API Common.

## Change

Close the two released OGC 23-002 Part 2 API Common Annex A procedures exactly:

- `/conf/api-common/resources`
- `/conf/api-common/resource-collection`

The implementation keeps the existing read-only behavior, but the deployed
TestNG class exposes exactly the two released procedure methods. Runtime
declaration checks remain prerequisite gates inside each procedure rather than
standalone claimed ATS methods. TestNG wiring will depend on `part1apicommon`
so inherited Part 1 API Common behavior is causal and visible.

## Verification

- Focused preimplementation red: `88 tests / 6 failures / 1 error / 0 skipped`.
- Corrected focused Part 2 API Common and suite-dependency verification:
  `88/0/0/0`.
- Released ATS coverage audit: `23/0/0/0`; Part 2 API Common is
  `2 exact / 0 candidate / 0 unmapped`.
- Full Docker Maven: `735 tests / 0 failures / 0 errors / 3 skipped`.
- Local OSH TeamEngine smoke: `244 total / 41 passed / 21 failed /
  182 skipped`; both Part 2 API Common procedures SKIP honestly because local
  OSH does not declare `/conf/api-common`.
- No-mutation oracle: 194 recognized local-OSH IUT request logs and zero
  POST/PUT/PATCH/DELETE.
- Raze: `APPROVE_WITH_CONCERNS 0.95`, no required fixes. The only LOW concern
  is that raw Maven stdout logs are not archived beside the E2E artifacts; the
  exact Maven totals are recorded consistently in the sprint documents.
- Evidence directory:
  `ops/test-results/sprint-ets-59-part2-api-common-2026-07-31/`.

## Acceptance

- `Part2ApiCommonTests` has exactly two TestNG `@Test` methods, one for each
  released Part 2 API Common ATS target.
- Both methods carry `REQ-ETS-PART2-001` and their matching
  `SCENARIO-ETS-PART2-001-RELEASED-*` identifiers in comments/descriptions.
- The `part2apicommon` group depends on `part1apicommon`.
- Setup reads only immutable suite arguments before per-procedure execution and
  skips before IUT access when inherited prerequisites fail, except for the
  documented Part 1 API Common datetime evidence limitation.
- Collection discovery remains advertised-link-only, same-origin, bounded, and
  read-only.
- `reviewed-ats-mappings.json` maps both released tests as exact mappings, and
  `ops/ats-coverage-report.json` is regenerated.
- Focused unit/structural tests, full Docker Maven, released-ATS coverage audit,
  local OSH TeamEngine smoke, and Raze review are recorded.

## Non-Goals

- Do not implement or claim Datastream, ControlStream, Feasibility, System
  Events, JSON, SWE Common, Create/Replace/Delete, Update, or Binding closure.
- Do not mutate the IUT.
- Do not introduce a new validator dependency or TeamEngine runtime path.
