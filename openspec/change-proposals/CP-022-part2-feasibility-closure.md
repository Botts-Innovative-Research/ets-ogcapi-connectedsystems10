# CP-022 - Part 2 Command Feasibility Released ATS Closure

**Status**: Implemented in Sprint 62
**Date**: 2026-07-31
**Capability**: `ets-ogcapi-connectedsystems`
**Requirement**: `REQ-ETS-PART2-004`
**Story**: `S-ETS-62-01`

## Problem

Sprint 23 implemented a useful safety-gated Command Feasibility subset, but the
released ATS coverage audit still reports OGC 23-002 `/conf/feasibility` as
candidate coverage. The deployed class includes seven helper-oriented methods
instead of the five released Annex A.4 procedures, and its TestNG group still
depends on `core common` rather than the now-exact `/conf/controlstream`
prerequisite closed in Sprint 61.

## Change

Replace the Sprint 23 subset with the five released OGC 23-002 Part 2 Command
Feasibility Annex A.4 procedures exactly:

- `/conf/feasibility/canonical-url`
- `/conf/feasibility/ref-from-controlstream`
- `/conf/feasibility/status-endpoint`
- `/conf/feasibility/result-endpoint`
- `/conf/feasibility/collections`

The deployed TestNG class SHALL expose exactly these five procedures. Each
procedure SHALL gate exact `/conf/feasibility` declaration at runtime, inherit
through the exact `/conf/controlstream` group, and stay read-only. The
implementation SHALL follow the released Annex procedure text literally while
documenting its copy-text inconsistencies:

- A.35 names the Feasibility canonical-url requirement, but its purpose and
  collection selector say `Command` and `itemType=Command`.
- A.36 names Feasibility `ref-from-controlstream`, but its method delegates to
  the ControlStream command endpoint procedure at
  `{api_root}/controlstreams/{dsId}/commands`.
- A.39 selects `itemType=Feasibility` collections but validates them with the
  released Command schema.

## Verification Plan

- Focused test-first red captured after formatting: the new exact helper tests
  failed compilation on missing released-path/itemType helpers.
- Corrected focused verification passed `83/0/0/0`.
- Released ATS coverage update passed `1/0/0/0`; coverage audit passed
  `23/0/0/0`; `2:/conf/feasibility` is
  `5 exact / 0 candidate / 0 unmapped`.
- Formatter passed. Full Docker Maven passed with existing skips:
  `760 tests / 0 failures / 0 errors / 3 skipped`.
- Mandatory local OSH TeamEngine smoke produced honest non-green evidence:
  `252 total / 36 passed / 21 failed / 195 skipped`; all five Feasibility
  procedures SKIP before Feasibility IUT access because the local OSH
  prerequisite chain skips `part2controlstream`.
- No-mutation oracle passed with `recognized_iut_request_logs=184` and
  `GET=184`, zero POST/PUT/PATCH/DELETE.
- Raze adversarial focused recheck returned `APPROVE 0.96` with no unresolved
  required fixes.

## Non-Goals

- IUT mutation or feasibility Command creation.
- Correcting OGC 23-002 Annex A.4 copy-text inconsistencies by substituting
  unlisted endpoints.
- OSH or TeamEngine source/binary changes.
- Semantic feasibility-analysis validation beyond the released Command,
  CommandStatus, and CommandResult schema procedures.
