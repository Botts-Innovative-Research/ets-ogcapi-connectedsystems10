# CP-026 - Part 2 SWE Common JSON Encoding Released ATS Closure

**Status**: Complete - pushed
**Date**: 2026-08-01
**Capability**: `ets-ogcapi-connectedsystems`
**Requirement**: `REQ-ETS-PART2-010`
**Story**: `S-ETS-66-01`

## Problem

Sprint 29 implemented a useful declaration-gated `/conf/swecommon-json` subset,
but the released ATS coverage audit still reports OGC 23-002 Annex A.10 as
`0 exact / 7 candidate / 1 unmapped`. The deployed class includes standalone
declaration/prerequisite/resource-gate helper tests, lacks a released
`/conf/swecommon-json/mediatype-read` procedure, and its write-media
advertisement check can pass from one scoped operation rather than requiring
every advertised scoped POST/PUT operation to include exact
`application/swe+json`.

## Change

Replace the Sprint 29 subset surface with the eight released Annex A.10
procedures:

- `/conf/swecommon-json/mediatype-read`
- `/conf/swecommon-json/mediatype-write`
- `/conf/swecommon-json/obsschema-schema`
- `/conf/swecommon-json/obsschema-mapping`
- `/conf/swecommon-json/observation-encoding`
- `/conf/swecommon-json/cmdschema-schema`
- `/conf/swecommon-json/cmdschema-mapping`
- `/conf/swecommon-json/command-encoding`

Each deployed TestNG method SHALL map to exactly one released ATS target, SHALL
use official OGC 23-002 `/req/swecommon-json` and `/conf/swecommon-json`
identifiers, and SHALL remain read-only. Class setup SHALL gate exact
`/conf/swecommon-json` declaration and the SWE Common 3.0 JSON Encoding Rules
prerequisite before SWE Common JSON resource endpoint access. Resource-specific
procedures SHALL condition on the relevant Part 2 resource conformance class
before endpoint access.

## Verification Plan

- Add structural red tests for exact method count, released target list,
  one-to-one method mapping, no standalone helper tests, and `mediatype-read`.
- Implement the eight methods with read-only GET/API-definition probes, exact
  `application/swe+json` media checks, bundled wrapper-schema plus reusable
  SWE `recordSchema` validation, and honest SKIPs when concrete
  Observation/Command encoding evidence is not safely present.
- Promote all eight mappings to reviewed exact and regenerate
  `ops/ats-coverage-report.json`.
- Run formatter, focused Maven, coverage update/audit, full Docker Maven, and
  mandatory local OSH TeamEngine smoke with no-mutation evidence.
- Run Raze before completion and reconcile all specs/ops artifacts.

## Implementation Status

Implemented on 2026-08-01 with exactly eight Annex A.10 TestNG procedures and
reviewed exact mappings. Formatter passed, focused Docker Maven passed
`114/0/0/0`, coverage audit passed, full Docker Maven passed on retry
`770/0/0/3` after an initial dependency-transfer failure, and mandatory local
OSH TeamEngine E2E exited honestly non-green at `256/27/20/209`. All eight
Sprint 66 methods SKIP before SWE Common JSON resource endpoint access because
local OSH lacks `http://www.opengis.net/spec/SWE/3.0/conf/json-encoding-rules`.
No-mutation evidence shows `GET=144` and zero POST/PUT/PATCH/DELETE. Raze
returned `APPROVE 0.96` with `RAZE-ETS66-DOC-001` closed and no required
fixes. Implementation commit `6e98ac9` is pushed to Botts `main`.

## Non-Goals

- Mutating an IUT to create Observation or Command resources.
- Positive POST/PUT lifecycle behavior for SWE Common JSON write support.
- Closing SWE Common Text or SWE Common Binary encoding classes.
- Changing external SWE Common validator dependencies.
