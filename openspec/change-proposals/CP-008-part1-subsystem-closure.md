# Change Proposal: CP-008 Part 1 Subsystem Direct ATS Closure

**Date**: 2026-07-26
**Author**: Codex
**Affects**:

- `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- `openspec/capabilities/ets-ogcapi-connectedsystems/design.md`
- `REQ-ETS-PART1-003`
- `REQ-ETS-COVERAGE-001`

**Status**: Complete; final Raze `APPROVE_WITH_CONCERNS` at confidence `0.99`
with no unresolved required findings

## Motivation

The released OGC 23-001 `/conf/subsystem` class contains five procedures:
collection discovery, boolean recursive-parameter use, recursive search from
the top-level Systems endpoint, recursive search from a parent System's
Subsystems endpoint, and recursive association-resource closure.

The historical four-method implementation tests a different surface. It
requires a non-empty subcollection, checks inherited canonical shape and links,
and asserts a parent link that is not one of the five released Annex A
procedures. Only its collection method is a candidate mapping, and that method
does not execute the released parent-link discovery or media-specific schema
procedure. Sprint 48 replaces the class rather than promoting those
approximations.

## Changed Requirement

### REQ-ETS-PART1-003

The ETS SHALL implement one independently executable TestNG method for each
released `/conf/subsystem` test:

- `/conf/subsystem/collection`;
- `/conf/subsystem/recursive-param`;
- `/conf/subsystem/recursive-search-systems`;
- `/conf/subsystem/recursive-search-subsystems`; and
- `/conf/subsystem/recursive-assoc`.

Hierarchy assertions SHALL derive expected descendants independently by
walking each System's default direct-subsystems endpoint. Traversal SHALL be
bounded, follow pagination safely, reject duplicate IDs and hierarchy cycles,
and retain direct-child and transitive-descendant sets separately. Results from
the endpoint under test SHALL NOT be reused as their own expected recursive
closure.

The collection procedure SHALL retrieve a parent System known to have
subsystems, require exactly one `rel=subsystems` target equal to
`{api_root}/systems/{sysId}/subsystems`, dereference it with HTTP 200, and
validate every page against the released GeoJSON or SensorML System collection
schema selected from the actual response media type. Unsupported media SHALL
warn and SKIP. HTTP status and actual media SHALL be gated before parsing on
every page, the accepted first response SHALL be reused, and collection-schema
validation SHALL accept a first-response SensorML item without requiring a
preliminary GeoJSON response or a local `id` member.
Hierarchy discovery SHALL apply the same status and actual-media gate to every
root `/systems` page and every nested subsystem page before parsing. Recursive
graph discovery MAY require local IDs after that gate.

The recursive-parameter procedure SHALL issue requests containing
`recursive=false` and `recursive=true`, verify the exact boolean query values,
and require successful responses. The two recursive-search procedures SHALL
require positive hierarchy evidence before PASS. Default and
`recursive=false` results SHALL exclude known transitive descendants;
`recursive=true` SHALL include every independently discovered descendant at all
levels.

For every discovered parent System, recursive-association validation SHALL
exercise each implemented Sampling Feature, DataStream, and ControlStream
association endpoint. The parent endpoint SHALL return HTTP 200 and include
every resource ID independently observed through its descendant Systems.
No discovered descendant association resources SHALL produce SKIP rather than
a vacuous PASS.

## Architecture

`SubsystemsTests` retains TestNG group `subsystems` after `systemfeatures`, but
shared setup loads only the normalized API root and evaluates inherited results.
Each released method discovers only the evidence it needs. A no-data outcome in
one method therefore cannot configuration-skip the other four.

`SubsystemsSupport` owns bounded hierarchy discovery, link resolution,
representation traversal, recursive set comparisons, association-resource
closure, and request-query inspection. It reuses the reviewed API Common
pagination model and the System endpoint schema validator instead of copying
homegrown schema logic.

The explicit inherited-result boundary permits only the already documented API
Common datetime no-evidence SKIP and the exact System unsupported-media or
missing-mobile-input SKIPs. Every prerequisite failure, configuration failure,
or other prerequisite SKIP blocks Subsystem IUT access. Allowed inherited SKIPs
remain visible and cannot become positive full-class conformance evidence.

No OSH or TeamEngine source code or binary is modified. The primary local OSH
currently returns unsupported `application/json` for root System collection
traversal, so its expected direct outcomes are one recursive-parameter PASS and
four evidence-honest media-gate SKIPs. A controlled read-only HTTP fixture SHALL
provide a multi-level hierarchy and positive association resources so all five
deployed methods execute successful paths.

## Acceptance Boundary

Sprint 48 closes only when all five procedures have reviewed exact mappings,
focused and full Maven verification pass, TeamEngine executes all five methods
against the unmodified local OSH IUT, a controlled fixture proves every
positive hierarchy and association path, exact-image runtime and dependency
gates remain green, credentials remain protected, and Raze has no unresolved
required findings.
