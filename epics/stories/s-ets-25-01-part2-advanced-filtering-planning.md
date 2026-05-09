# S-ETS-25-01: Part 2 Advanced Filtering read-only declaration-gated subset

## Status
Planned; Raze-approved; committed and pushed.

## User Instruction
Triggered by: "continue" after Sprint 24 Generator was implemented, reconciled, and pushed.

## Scope
Plan the first Generator increment for OGC 23-002 Clause 13 Requirements Class "Advanced Filtering".

- Requirements class: `/req/advanced-filtering`
- Conformance class: `/conf/advanced-filtering`
- Prerequisites: `/req/api-common` and Part 1 `/req/advanced-filtering`
- Normative statements in scope for planning: Requirements 45-62, covering DataStream, Observation, ControlStream, Command, CommandStatus, and SystemEvent filter query parameters

## Planning Correction
The previous Part 2 placeholder backlog included `/conf/system-history` as `REQ-ETS-PART2-006`. OGC 23-002 Annex A does not define a System History conformance class or requirements class. GeoRobotix advertises `/conf/system-history`, but Sprint 25 treats that as a non-standard/vendor extension and retires the placeholder from the OGC conformance-class backlog.

## Planning Evidence
- Official source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 13 "Requirements Class Advanced Filtering" and Annex A.6.
- OGC 23-002 names the requirements class `/req/advanced-filtering` and conformance class `/conf/advanced-filtering`.
- The class prerequisites are `/req/api-common` and Part 1 `/req/advanced-filtering`.
- Normative requirements 45-62 define query filters for:
  - DataStreams: `phenomenonTime`, `resultTime`, `observedProperty`, `foi`.
  - Observations: `phenomenonTime`, `resultTime`, `foi`.
  - ControlStreams: `issueTime`, `executionTime`, `controlledProperty`, `foi`.
  - Commands: `issueTime`, `executionTime`, `statusCode`, `sender`, `foi`.
  - CommandStatus: `statusCode`.
  - SystemEvents: `eventType`.
- GeoRobotix `/conformance` does not declare `/conf/advanced-filtering`.
- GeoRobotix `GET /datastreams?phenomenonTime=2026-04-20T00:00:00Z&limit=2` returned HTTP 200 JSON with `items`.
- GeoRobotix `GET /datastreams?resultTime=2026-04-20T00:00:00Z&limit=2` returned HTTP 200 JSON with `items`.
- GeoRobotix `GET /datastreams?observedProperty=http%3A%2F%2Fsensorml.com%2Font%2Fisa%2Fproperty%2FLink_Loss&limit=2` returned HTTP 200 JSON with `items`.
- GeoRobotix `GET /observations?phenomenonTime=2026-04-20T00:00:00Z&limit=2` and `GET /observations?resultTime=2026-04-20T00:00:00Z&limit=2` returned HTTP 200 JSON with empty `items`.
- GeoRobotix `GET /controlstreams?issueTime=2026-04-20T00:00:00Z&limit=2` and `GET /controlstreams?executionTime=2026-04-20T00:00:00Z&limit=2` returned HTTP 200 JSON with `items`.
- GeoRobotix `GET /commands?issueTime=...`, `GET /commands?statusCode=...`, and `GET /commands?sender=...` returned HTTP 400 `Invalid resource name: 'commands'`.
- GeoRobotix `GET /systemEvents?eventType=...` returned HTTP 400 `Invalid resource name: 'systemEvents'`.
- GeoRobotix `GET /systems/0mqcvdnfoca0/events?eventType=...` returned HTTP 400 `Only streaming requests supported on this resource`.

## Generator Requirements
- Add a Part 2 Advanced Filtering TestNG group that is co-located with Core, Common, Part 1 AdvancedFiltering, Part 2 Datastream, Part 2 ControlStream, and Part 2 SystemEvent as needed by the first subset.
- Gate all Part 2 Advanced Filtering assertions on exact `/conf/advanced-filtering` declaration.
- Preserve prerequisite honesty: `/req/api-common` and Part 1 `/req/advanced-filtering` are prerequisites for the full class.
- Do not PASS any Advanced Filtering assertion from successful undeclared query behavior alone.
- Prefer bounded read-only GET requests with `limit=2`.
- For filter assertions that return a non-empty collection, verify each returned resource actually satisfies the requested filter predicate before PASS.
- For empty result collections, treat endpoint/readability as insufficient for predicate PASS unless the assertion is explicitly scoped to "supported and empty" behavior with a precise SKIP reason.
- For Command and SystemEvent filters, SKIP when the underlying endpoint is unavailable or streaming-only; do not fail GeoRobotix solely because it does not declare `/conf/advanced-filtering`.
- Do not implement mutation, POST-created seed resources, long-lived streaming/SSE consumption, or full recursive FOI graph traversal in the first subset.

## Definition of Done
- OpenSpec, traceability, epic, contract, ops status, test-results, known issues, and handoffs reconciled.
- The stale `/conf/system-history` placeholder is retired from current OGC 23-002 scope.
- Generator contract blocks false PASS from undeclared filter behavior, empty collections, endpoint availability alone, and sibling Part 2 declarations.
- Raze reviews planning before Generator starts.
- Raze planning review `.harness/evaluations/sprint-ets-25-plan-adversarial.yaml` returned `APPROVE` confidence 0.96 after closing the stale `REQ-ETS-PART2-014` acceptance-reference gap.
- Planning-only change is committed and pushed before Generator implementation: `2f4a6de Plan Sprint 25 Advanced Filtering`.

## Out of Scope
- Full FOI recursive graph traversal.
- SystemEvent streaming/SSE consumption.
- Mutation or seed-resource creation.
- Command positive filter closure when `/commands` is unavailable.
- Part 2 Create/Replace/Delete, Update, JSON, and SWE Common encoding classes.
