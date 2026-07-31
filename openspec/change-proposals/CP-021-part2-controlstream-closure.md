# CP-021 - Part 2 Control Streams and Commands Released ATS Closure

**Status**: Implemented in Sprint 61
**Date**: 2026-07-31
**Capability**: `ets-ogcapi-connectedsystems`
**Requirement**: `REQ-ETS-PART2-003`
**Story**: `S-ETS-61-01`

## Problem

Sprint 22 implemented a useful read-only Control Streams and Commands subset,
but the released ATS coverage audit still reports the OGC 23-002
`/conf/controlstream` class as `0 exact / 8 candidate / 10 unmapped`. The
class also retains standalone non-ATS tracer procedures and TestNG wiring that
allows scoped endpoint evidence to run without the now-exact Part 2 API Common
prerequisite.

## Change

Close all eighteen released OGC 23-002 Part 2 Control Streams and Commands
Annex A procedures exactly:

- `/conf/controlstream/sf-ref-from-controlstream`
- `/conf/controlstream/foi-ref-from-controlstream`
- `/conf/controlstream/canonical-url`
- `/conf/controlstream/resources-endpoint`
- `/conf/controlstream/canonical-endpoint`
- `/conf/controlstream/ref-from-system`
- `/conf/controlstream/ref-from-deployment`
- `/conf/controlstream/collections`
- `/conf/controlstream/schema-op`
- `/conf/controlstream/cmd-canonical-url`
- `/conf/controlstream/cmd-resources-endpoint`
- `/conf/controlstream/cmd-canonical-endpoint`
- `/conf/controlstream/cmd-ref-from-controlstream`
- `/conf/controlstream/cmd-collections`
- `/conf/controlstream/status-resources-endpoint`
- `/conf/controlstream/command-status-endpoint`
- `/conf/controlstream/result-resources-endpoint`
- `/conf/controlstream/command-result-endpoint`

The deployed TestNG class SHALL expose exactly these eighteen procedures. Each
procedure SHALL gate exact `/conf/controlstream` declaration at runtime and
SHALL stay read-only. The `part2controlstream` group SHALL inherit through
`part2apicommon` now that Sprint 59 closed the Part 2 API Common released ATS.

## Verification Plan

- Focused test-first red showing the historical Sprint 22 subset is not exact.
- Corrected focused verification for ControlStream semantics and TestNG
  dependency structure.
- Released ATS coverage update and audit with `2:/conf/controlstream` at
  `18 exact / 0 candidate / 0 unmapped`.
- Formatter and full Docker Maven verification.
- Mandatory local OSH TeamEngine smoke with concrete pass/fail/skip totals and
  no-mutation evidence.
- Raze adversarial review before completion.

## Outcome

Sprint 61 implemented the change as specified. `Part2ControlStreamTests` now
contains exactly eighteen released procedures, `part2controlstream` inherits
directly from `part2apicommon`, reviewed coverage reports
`18 exact / 0 candidate / 0 unmapped`, full Docker Maven passed
`758/0/0/3`, and the mandatory local OSH smoke reached the real IUT at
`254/36/21/197` with all Sprint 61 methods prerequisite-SKIPping because local
OSH does not declare Part 2 `/conf/api-common`. No IUT-bound writes were
observed.

## Non-Goals

- IUT mutation.
- Command creation or feasibility POST behavior.
- Importing or modifying OSH or TeamEngine source/binaries.
- Semantic Command parameter/result validation beyond the released
  ControlStream and Command resource endpoint/schema procedures.
