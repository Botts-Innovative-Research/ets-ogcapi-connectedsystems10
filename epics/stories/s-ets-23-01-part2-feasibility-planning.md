# S-ETS-23-01: Part 2 Command Feasibility safety-gated subset

## Status
Planned.

## User Instruction
Triggered by: "Do it" after Sprint 22 Generator handoff identified `REQ-ETS-PART2-004` `/conf/feasibility` as the next Sprint item.

## Scope
Implement a safety-gated Generator increment for OGC 23-002 Clause 11 Requirements Class "Command Feasibility".

- Requirements class: `/req/feasibility`
- Conformance class: `/conf/feasibility`
- Prerequisite: `/req/controlstream`
- Normative statements in scope for planning: `/req/feasibility/canonical-url`, `/req/feasibility/ref-from-controlstream`, `/req/feasibility/status-endpoint`, `/req/feasibility/result-endpoint`, and `/req/feasibility/collections`

## Planning Evidence
- Official source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 11 "Requirements Class Command Feasibility".
- The standard states a feasibility request is initiated by creating a `Command` resource on the feasibility channel; therefore public default smoke must not issue feasibility POSTs.
- GeoRobotix `/conformance` currently does not declare `/conf/feasibility`.
- GeoRobotix `GET /feasibility?limit=2` returned HTTP 400 JSON: `Invalid resource name: 'feasibility'`.
- GeoRobotix `GET /controlstreams/0m4qpft9sdag/feasibility?limit=2` returned HTTP 400 JSON: `Invalid resource name: 'feasibility'`.
- GeoRobotix `GET /controlstream/0m4qpft9sdag/feasibility?limit=2` returned HTTP 400 JSON: `Invalid resource name: 'controlstream'`.
- GeoRobotix `GET /collections?limit=100` did not show a collection with `itemType` equal to `Feasibility`.
- Normative endpoint guard: `/req/feasibility/ref-from-controlstream` uses singular `{api_root}/controlstream/{csId}/feasibility`. The plural `/controlstreams/{id}/feasibility` probe is diagnostic only and must not produce PASS by itself.

## Generator Requirements
- Add a Part 2 Feasibility TestNG group that is co-located after Core/Common/ControlStream dependencies.
- Gate all Command Feasibility conformance assertions on exact `/conf/feasibility` declaration.
- Preserve `/req/controlstream` prerequisite honesty; do not report full `/conf/feasibility` closure when the prerequisite cannot be established.
- In default public-smoke mode, SKIP before any IUT-bound feasibility POST.
- If a future safe/mutable-IUT opt-in is enabled, only then may the suite create a feasibility request, and it must remain distinct from regular Command creation.
- Treat Feasibility collections as optional; only assert collection behavior when a collection advertises `itemType: Feasibility`.
- Require actual Feasibility resource evidence before PASSing canonical URL, status endpoint, or result endpoint assertions.
- Require the normative singular `{api_root}/controlstream/{csId}/feasibility` endpoint, or a documented standards-backed rationale, before PASSing `/req/feasibility/ref-from-controlstream`.

## Definition of Done
- OpenSpec, traceability, epic, contract, ops status, test-results, known issues, and handoffs reconciled.
- Tests include comments referencing `REQ-ETS-PART2-004` and relevant `SCENARIO-ETS-PART2-004-*` IDs.
- Maven test suite passes without hiding failures.
- TeamEngine smoke against GeoRobotix shows `/conf/feasibility` SKIPs honestly and zero IUT-bound POST/PUT/DELETE/PATCH.
- Raze reviews implementation before completion is reported.

## Out of Scope
- Regular Command creation.
- Default public-IUT feasibility POST.
- Part 2 JSON, SWE Common encodings, Create/Replace/Delete, Update, System Events, and System History.
- Full feasibility result schema validation unless a real safe/mutable IUT provides stable result evidence.

## Raze Planning Review
Initial review `.harness/evaluations/sprint-ets-23-plan-adversarial.yaml` returned `GAPS_FOUND` confidence 0.89 for endpoint-alias false-PASS risk and stale status metadata. Gap-fix review `.harness/evaluations/sprint-ets-23-plan-gapfix.yaml` returned `APPROVE` confidence 0.96 with no required fixes.
