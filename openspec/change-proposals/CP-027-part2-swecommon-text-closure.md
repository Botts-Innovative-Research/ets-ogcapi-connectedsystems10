# CP-027 - Part 2 SWE Common Text Encoding Released ATS Closure

**Status**: Raze approved; push pending
**Date**: 2026-08-01
**Capability**: `ets-ogcapi-connectedsystems`
**Requirement**: `REQ-ETS-PART2-011`
**Story**: `S-ETS-67-01`

## Problem

Sprint 30 implemented a useful declaration-gated `/conf/swecommon-text`
subset, but the released ATS coverage audit still reports OGC 23-002 Annex
A.11 as `0 exact / 7 candidate / 1 unmapped`. The deployed class includes
standalone declaration/prerequisite/resource-gate helper tests, lacks a
released `/conf/swecommon-text/mediatype-read` procedure, and its write-media
advertisement check can pass from one scoped operation rather than requiring
every advertised scoped POST/PUT operation to include exact
`application/swe+text`.

## Change

Replace the Sprint 30 subset surface with the eight released Annex A.11
procedures:

- `/conf/swecommon-text/mediatype-read`
- `/conf/swecommon-text/mediatype-write`
- `/conf/swecommon-text/obsschema-schema`
- `/conf/swecommon-text/obsschema-mapping`
- `/conf/swecommon-text/observation-encoding`
- `/conf/swecommon-text/cmdschema-schema`
- `/conf/swecommon-text/cmdschema-mapping`
- `/conf/swecommon-text/command-encoding`

Each deployed TestNG method SHALL map to exactly one released ATS target, SHALL
use official OGC 23-002 `/req/swecommon-text` and `/conf/swecommon-text`
identifiers, and SHALL remain read-only. Class setup SHALL gate exact
`/conf/swecommon-text` declaration and the SWE Common 3.0 Text Encoding Rules
prerequisite before SWE Common Text resource endpoint access. Resource-specific
procedures SHALL condition on the relevant Part 2 resource conformance class
before endpoint access.

## Verification Plan

- Add structural red tests for exact method count, released target list,
  one-to-one method mapping, no standalone helper tests, and `mediatype-read`.
- Implement the eight methods with read-only GET/API-definition probes, exact
  `application/swe+text` media checks, bundled wrapper-schema plus reusable SWE
  `recordSchema` validation, and honest SKIPs when concrete
  Observation/Command text encoding evidence is not safely present.
- Promote all eight mappings to reviewed exact and regenerate
  `ops/ats-coverage-report.json`.
- Run formatter, focused Maven, coverage update/audit, full Docker Maven, and
  mandatory local OSH TeamEngine smoke with no-mutation evidence.
- Run Raze before completion and reconcile all specs/ops artifacts.

## Implementation Status

Generator implementation and local verification are complete, with Raze
approved and commit/push still pending. `Part2SweCommonTextTests` now exposes
exactly eight released Annex A.11 TestNG methods, removes the Sprint 30
standalone helper procedures, adds the missing `mediatype-read` procedure,
strengthens `mediatype-write` to require every advertised scoped POST/PUT
operation to include exact `application/swe+text`, fails present but
noncanonical IssueTime mapping evidence, and keeps all runtime checks
read-only.
Coverage is promoted to `2:/conf/swecommon-text = 8 exact / 0 candidate /
0 unmapped`.

Evidence is archived under
`ops/test-results/sprint-ets-67-part2-swecommon-text-2026-08-01/`. Focused
Maven passed `115/0/0/0`, full Docker Maven completed
`775 tests / 0 failures / 0 errors / 3 skipped`, local OSH TeamEngine smoke
ran the deployed stack and exited honestly non-green at `254/25/20/209`, all
eight Sprint 67 procedures SKIP before SWE Common Text resource endpoint access
because local OSH lacks
`http://www.opengis.net/spec/SWE/3.0/conf/text-encoding-rules`, no-mutation
evidence is `GET=137` with zero POST/PUT/PATCH/DELETE, and TeamEngine 6
runtime immutability verification passed for smoke image
`sha256:84423839aa6f4e5209b679a46ddf6a7cfbb3cbc3eb737ecce25d4e9d65167b0c`.
Initial Raze found `RAZE-ETS67-FALSESKIP-001` and `RAZE-ETS67-DOC-001`; the
focused recheck returned `APPROVE 0.96` with both findings closed and
`required_fixes: []`.

## Non-Goals

- Mutating an IUT to create Observation or Command resources.
- Positive POST/PUT lifecycle behavior for SWE Common Text write support.
- Closing SWE Common Binary encoding.
- Changing external SWE Common validator dependencies.
