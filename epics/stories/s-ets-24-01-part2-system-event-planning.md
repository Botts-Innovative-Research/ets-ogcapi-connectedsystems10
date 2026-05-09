# S-ETS-24-01: Part 2 System Events read-only declaration-gated subset

## Status
Partial Implemented.

## User Instruction
Triggered by: "Continue" after Sprint 23 Generator was implemented and pushed.

## Scope
Plan the first Generator increment for OGC 23-002 Clause 12 Requirements Class "System Events".

- Requirements class: `/req/system-event`
- Conformance class: `/conf/system-event`
- Prerequisites: `/req/api-common` and Part 1 `/req/system`
- Normative statements in scope for planning: `/req/system-event/canonical-url`, `/req/system-event/resources-endpoint`, `/req/system-event/canonical-endpoint`, `/req/system-event/ref-from-system`, and `/req/system-event/collections`

## Planning Evidence
- Official source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 12 "Requirements Class System Events" and Annex A.5.
- Requirement 42 names canonical endpoint `{api_root}/systemEvents`.
- Requirement 43 names system-scoped endpoint `{api_root}/systems/{sysId}/events`.
- Annex A.43 contains a conflicting endpoint string `{api_root}/systems/{sysId}/systemEvents`; Generator must follow the normative requirement text unless a standards-backed correction is documented.
- GeoRobotix `/conformance` declares `/conf/system-event`, `/conf/system-history`, `/conf/datastream`, `/conf/controlstream`, `/conf/json`, `/conf/create-replace-delete`, and SWE Common encodings, but not `/conf/api-common`.
- GeoRobotix `GET /systemEvents?limit=2` returned HTTP 400 HTML: `Invalid resource name: 'systemEvents'`.
- GeoRobotix `GET /systems/0mqcvdnfoca0/events?limit=2` returned HTTP 400 JSON: `Only streaming requests supported on this resource`.
- GeoRobotix `GET /systems/0mqcvdnfoca0/systemEvents?limit=2` returned HTTP 400 HTML: `Invalid resource name: 'systemEvents'`.
- GeoRobotix `GET /collections?limit=100` did not expose a collection with `itemType` equal to `SystemEvent`.

## Generator Requirements
- Add a Part 2 System Events TestNG group that is co-located with Core, Common, and SystemFeatures.
- Gate System Events assertions on exact `/conf/system-event` declaration.
- Keep prerequisite honesty visible: GeoRobotix declares `/conf/system-event` but not `/conf/api-common`, so scoped endpoint checks may run while full closure remains prerequisite-incomplete.
- Use the normative endpoint names from Requirement 42 and 43: `/systemEvents` and `/systems/{sysId}/events`.
- Treat `/systems/{sysId}/systemEvents` as diagnostic evidence only because it appears in Annex A.43 but conflicts with Requirement 43.
- Require actual SystemEvent resource evidence before PASSing canonical URL checks.
- Treat `itemType=SystemEvent` collections as optional unless advertised.
- Do not implement streaming/SSE event consumption in this first read-only subset; streaming behavior is a future System Events expansion.

## Definition of Done
- OpenSpec, traceability, epic, contract, ops status, test-results, known issues, and handoffs reconciled.
- Generator contract clearly blocks false PASS from sibling Part 2 declarations, endpoint alias drift, and empty/generic collection shape.
- Raze reviews planning before Generator starts.
- Planning-only change is committed and pushed before Generator implementation.

## Raze Planning Review
Review `.harness/evaluations/sprint-ets-24-plan-adversarial.yaml` returned `APPROVE` confidence 0.93 with no required fixes. Raze verified official identifiers, prerequisite honesty, the Requirement 43 versus Annex A.43 endpoint conflict guard, GeoRobotix streaming-only HTTP 400 behavior, and absence of stale `/req/systemevents` or `dynamic-*` authoritative naming.

## Generator Implementation
- Added `Part2SystemEventTests` with 6 read-only/default-safe checks for `/conf/system-event`, `/req/api-common` and Part 1 `/req/system` prerequisite visibility, canonical `/systemEvents` endpoint evidence, normative `/systems/{sysId}/events` endpoint evidence, optional `/systemEvents/{id}` resource evidence, and optional `itemType=SystemEvent` collections.
- Added `VerifyPart2SystemEventTests` helper regressions for official identifiers, Requirement 43 endpoint path, resource-shape evidence, `itemType=SystemEvent`, and collection shape.
- Added TestNG `part2systemevent` group wiring and structural dependency regressions.
- Formatter BUILD SUCCESS.
- Maven BUILD SUCCESS: `183 tests / 0 failures / 0 errors / 3 skipped`.
- GeoRobotix TeamEngine smoke: `128 total / 72 passed / 0 failed / 56 skipped`.
- Runtime outcome: `systemEventConformanceDeclared` PASSes because GeoRobotix declares `/conf/system-event`; the other 5 System Events runtime tests SKIP honestly because `/conf/api-common` is absent, `/systemEvents` returns HTTP 400, `/systems/{id}/events` returns streaming-only HTTP 400, no SystemEvent resource evidence is available, and no `itemType=SystemEvent` collection is advertised.
- No-mutation oracle: zero IUT-bound POST/PUT/DELETE/PATCH across 99 recognized IUT request-log entries.
- Raze implementation review `.harness/evaluations/sprint-ets-24-adversarial-implementation.yaml` returned `APPROVE` confidence 0.94 with no required fixes.

## Out of Scope
- Streaming event subscription and long-lived response parsing.
- System History.
- Advanced Filtering event-by-type.
- Part 2 Create/Replace/Delete or Update.
- Part 2 JSON schema closure for `SystemEvent`.
- Positive event-resource body validation unless a stable SystemEvent resource is exposed by a safe IUT.
