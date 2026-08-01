# CP-028 - Part 2 SWE Common Binary Encoding Released ATS Closure

**Status**: Implemented; Raze approved with low concerns; pending push
**Date**: 2026-08-01
**Capability**: `ets-ogcapi-connectedsystems`
**Requirement**: `REQ-ETS-PART2-012`
**Story**: `S-ETS-68-01`

## Problem

Sprint 31 implemented a useful declaration-gated `/conf/swecommon-binary`
subset, but the released ATS coverage audit still reports OGC 23-002 Annex
A.12 as `0 exact / 7 candidate / 1 unmapped`. The deployed class includes
standalone declaration/prerequisite/resource-gate helper tests, lacks a
released `/conf/swecommon-binary/mediatype-read` procedure, and its
write-media advertisement check can pass from one scoped operation rather than
requiring every advertised scoped POST/PUT operation to include exact
`application/swe+binary`.

## Change

Replace the Sprint 31 subset surface with the eight released Annex A.12
procedures:

- `/conf/swecommon-binary/mediatype-read`
- `/conf/swecommon-binary/mediatype-write`
- `/conf/swecommon-binary/obsschema-schema`
- `/conf/swecommon-binary/obsschema-mapping`
- `/conf/swecommon-binary/observation-encoding`
- `/conf/swecommon-binary/cmdschema-schema`
- `/conf/swecommon-binary/cmdschema-mapping`
- `/conf/swecommon-binary/command-encoding`

Each deployed TestNG method SHALL map to exactly one released ATS target, SHALL
use official OGC 23-002 `/req/swecommon-binary` and `/conf/swecommon-binary`
identifiers, and SHALL remain read-only. Class setup SHALL gate exact
`/conf/swecommon-binary` declaration and the SWE Common 3.0 Binary Encoding
Rules prerequisite before SWE Common Binary resource endpoint access.
Resource-specific procedures SHALL condition on the relevant Part 2 resource
conformance class before endpoint access.

## Verification Plan

- Add structural red tests for exact method count, released target list,
  one-to-one method mapping, no standalone helper tests, and `mediatype-read`.
- Implement the eight methods with read-only GET/API-definition probes, exact
  `application/swe+binary` media checks, bundled wrapper-schema plus reusable
  SWE `recordSchema` validation, and honest SKIPs when concrete
  Observation/Command binary encoding evidence is not safely present.
- Promote all eight mappings to reviewed exact and regenerate
  `ops/ats-coverage-report.json`.
- Run formatter, focused Maven, coverage update/audit, full Docker Maven, and
  mandatory local OSH TeamEngine smoke with no-mutation evidence.
- Run Raze before completion and reconcile all specs/ops artifacts.

## Implementation Status

Generator implementation and local verification are complete, with Raze
recheck approval and push pending. `Part2SweCommonBinaryTests` now exposes exactly eight
released Annex A.12 TestNG methods, removes the Sprint 31 standalone helper
procedures, adds the missing `mediatype-read` procedure, requires
service-desc API-definition GET response-content advertisement plus concrete
retrieval evidence for `mediatype-read`, strengthens `mediatype-write` to
require every advertised scoped POST/PUT operation to include exact
`application/swe+binary`, validates mapping evidence across every retrieved
schema, fails missing or noncanonical IssueTime evidence after Command Schema
retrieval, and keeps all runtime checks read-only.
Coverage is promoted to `2:/conf/swecommon-binary = 8 exact / 0 candidate /
0 unmapped`.

Evidence is archived under
`ops/test-results/sprint-ets-68-part2-swecommon-binary-2026-08-01/`. Initial
Raze review found three high exactness gaps and one cosmetic typo; the gapfix
adds read-advertisement evidence, all-schema mapping checks, mandatory
IssueTime failure, and the typo fix. Post-Raze-fix focused Maven passed
`120/0/0/0`, full Docker Maven completed
`785 tests / 0 failures / 0 errors / 3 skipped`, local OSH TeamEngine smoke
ran the deployed stack and exited honestly non-green at `252/23/20/209`, all
eight Sprint 68 procedures SKIP before SWE Common Binary resource endpoint
access because local OSH lacks
`http://www.opengis.net/spec/SWE/3.0/conf/binary-encoding-rules`,
no-mutation evidence is `GET=130` with zero POST/PUT/PATCH/DELETE, and
TeamEngine 6 runtime immutability verification passed for smoke image
`sha256:47ddcfe1b7143004eba7b9fc88b5173fe1f6b7fa47616459a5d5efde14eee21e`.
Focused Raze recheck returned `APPROVE_WITH_CONCERNS 0.94` with no blocking
implementation, mapping, TestNG wiring, E2E honesty, or no-mutation gaps.

## Non-Goals

- Mutating an IUT to create Observation or Command resources.
- Positive POST/PUT lifecycle behavior for SWE Common Binary write support.
- Changing external SWE Common validator dependencies.
