# CP-024 - Part 2 Advanced Filtering Released ATS Closure

**Status**: Implemented and pushed in Sprint 64
**Date**: 2026-08-01
**Capability**: `ets-ogcapi-connectedsystems`
**Requirement**: `REQ-ETS-PART2-006`
**Story**: `S-ETS-64-01`

## Problem

Sprint 25 implemented a useful declaration-gated Part 2 Advanced Filtering
subset, but the released ATS coverage audit still reports OGC 23-002
`/conf/advanced-filtering` as `0 exact / 13 candidate / 5 unmapped`. The
deployed class has non-ATS declaration/prerequisite helper tests, combines
multiple released procedures into single methods, and omits the released FOI
and CommandStatus procedures.

## Change

Replace the Sprint 25 subset with the eighteen released OGC 23-002 Annex A.6
procedures exactly:

- `/conf/advanced-filtering/datastream-by-phenomenontime`
- `/conf/advanced-filtering/datastream-by-resulttime`
- `/conf/advanced-filtering/datastream-by-obsprop`
- `/conf/advanced-filtering/datastream-by-foi`
- `/conf/advanced-filtering/obs-by-phenomenontime`
- `/conf/advanced-filtering/obs-by-resulttime`
- `/conf/advanced-filtering/obs-by-foi`
- `/conf/advanced-filtering/controlstream-by-issuetime`
- `/conf/advanced-filtering/controlstream-by-exectime`
- `/conf/advanced-filtering/controlstream-by-controlprop`
- `/conf/advanced-filtering/controlstream-by-foi`
- `/conf/advanced-filtering/cmd-by-issuetime`
- `/conf/advanced-filtering/cmd-by-exectime`
- `/conf/advanced-filtering/cmd-by-status`
- `/conf/advanced-filtering/cmd-by-sender`
- `/conf/advanced-filtering/cmd-by-foi`
- `/conf/advanced-filtering/status-by-statuscode`
- `/conf/advanced-filtering/event-by-type`

The deployed TestNG class SHALL expose exactly these eighteen procedures. Each
procedure SHALL gate exact `/conf/advanced-filtering` declaration at runtime,
inherit through Part 2 API Common and Part 1 Advanced Filtering, validate the
referenced Part 2 endpoint shape through the released resource-class helpers,
and stay read-only.

## Verification Plan

- Write failing structural tests for exact method count, released target list,
  direct prerequisites, method-to-target uniqueness, FOI/CommandStatus coverage,
  and no standalone declaration/prerequisite helper methods.
- Run focused Docker Maven against the new structural tests before
  implementation and capture the failing evidence.
- Implement the eighteen released procedures with bounded read-only GET
  traversal and seed-derived predicate checks.
- Promote all eighteen mappings to reviewed exact and regenerate
  `ops/ats-coverage-report.json`.
- Run formatter, focused Maven, coverage update/audit, full Docker Maven, and
  mandatory local OSH TeamEngine smoke with no-mutation evidence.
- Run Raze adversarial review before completion wording.

## Non-Goals

- IUT mutation or seed-resource creation.
- Streaming/SSE event consumption beyond read-only collection endpoints named by
  Annex A.6.
- Correcting IUT data availability by modifying OSH or TeamEngine.
- Broad Part 1 Advanced Filtering changes; Part 1 is an inherited prerequisite
  only.
