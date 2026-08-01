# CP-023 - Part 2 System Events Released ATS Closure

**Status**: Implemented with Raze approval; push pending
**Date**: 2026-08-01
**Capability**: `ets-ogcapi-connectedsystems`
**Requirement**: `REQ-ETS-PART2-005`
**Story**: `S-ETS-63-01`

## Problem

Sprint 24 implemented a useful read-only System Events subset, but the released
ATS coverage audit still reports OGC 23-002 `/conf/system-event` as candidate
or unmapped coverage. The deployed class includes declaration/prerequisite
helper tests outside Annex A.5 and deliberately used Clause 12's
`/systems/{sysId}/events` path instead of the released Annex A.43
`/systems/{sysId}/systemEvents` copy text.

## Change

Replace the Sprint 24 subset with the five released OGC 23-002 Part 2 System
Events Annex A.5 procedures exactly:

- `/conf/system-event/canonical-url`
- `/conf/system-event/resources-endpoint`
- `/conf/system-event/canonical-endpoint`
- `/conf/system-event/ref-from-system`
- `/conf/system-event/collections`

The deployed TestNG class SHALL expose exactly these five procedures. Each
procedure SHALL gate exact `/conf/system-event` declaration at runtime, inherit
through the exact Annex A.5 prerequisites, and stay read-only. The
implementation SHALL follow the released Annex procedure text literally while
documenting its copy-text inconsistencies:

- A.40 names the SystemEvent canonical-url requirement, but its purpose and
  collection selector say `ControlStream` and `itemType=ControlStream`.
- A.42 validates canonical `/systemEvents` using a referenced
  `/conf/controlstream/resources-endpoint` test label even though the resource
  type is SystemEvent.
- A.43 validates `{api_root}/systems/{sysId}/systemEvents` even though Clause
  12 Requirement 43 names `{api_root}/systems/{sysId}/events`.

## Verification Plan

- Write failing structural tests for exact method count, direct prerequisites,
  Annex copy-text path selection, `itemType` selectors, and SystemEvent schema
  validation helpers.
- Run focused Docker Maven against the new structural tests before
  implementation and capture the failing evidence.
- Implement the five released procedures with read-only GET traversal and
  bundled `systemEventCollection.json` / `systemEvent.json` validation.
- Promote all five released mappings to reviewed exact and regenerate
  `ops/ats-coverage-report.json`.
- Run formatter, focused Maven, coverage update/audit, full Docker Maven, and
  mandatory local OSH TeamEngine smoke with no-mutation evidence.
- Run Raze adversarial review before completion wording.

## Outcome

Sprint 63 has implemented the five released procedures, promoted all
`/conf/system-event` ATS mappings to reviewed exact, and preserved the Annex
A.5 copy-text behavior literally. Verification evidence is archived under
`ops/test-results/sprint-ets-63-part2-system-event-2026-08-01/`:

- Focused test-first red: formatter normalization followed by missing helper
  compile failures (`focused-red-reproduction.txt`).
- Corrected focused verification: `84/0/0/0` (`focused-corrected.txt`).
- Formatter: BUILD SUCCESS (`formatter.txt`).
- Coverage update: `1/0/0/0` (`coverage-update.txt`); coverage audit:
  `23/0/0/0` (`coverage-audit.txt`).
- Full Docker Maven: `763/0/0/3` (`full-maven.txt`).
- Local OSH TeamEngine smoke: honest non-green `251/36/21/194`; all five
  System Events procedures SKIP before SystemEvent IUT access because
  prerequisite `canonicalSystemsEndpointIsValid` skipped.
- No-mutation oracle: 182 recognized local-OSH IUT request logs, all GET.

## Non-Goals

- Streaming/SSE event consumption.
- System History or GeoRobotix `/conf/system-history` behavior.
- Advanced Filtering `eventType` checks.
- Create/Replace/Delete or Update lifecycle mutation for SystemEvent resources.
- Correcting OGC 23-002 Annex A.5 copy-text inconsistencies by substituting
  unlisted endpoints.
