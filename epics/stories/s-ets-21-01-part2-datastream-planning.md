# Story S-ETS-21-01: Part 2 Datastreams & Observations Read-Only Subset

> Status: Planned | Epic: epic-ets-03-part2-classes | Sprint: ets-21 | Last updated: 2026-05-07

## Context

Sprint 21 continues Part 2 after Sprint 20 API Common. The target is OGC 23-002 Clause 9, Requirements Class "Datastreams & Observations", using official `/req/datastream` and `/conf/datastream` identifiers.

## Scope

Plan the first read-only `REQ-ETS-PART2-002` Generator increment:

1. `/conf/datastream` declaration gate.
2. `GET /datastreams` and `GET /datastreams/{id}` canonical Datastream access.
3. `GET /datastreams/{id}/schema` Datastream schema sub-resource.
4. `GET /observations` and `GET /datastreams/{id}/observations` Observation collection endpoint access.
5. `GET /systems/{systemId}/datastreams` when a selected Datastream exposes `system@id`.
6. Honest prerequisite handling for `/req/api-common` without claiming API Common PASS from Datastream evidence.
7. `/req/datastream/obs-ref-from-datastream` only when at least one nested Observation item or link gives actual Datastream association evidence; otherwise SKIP with an empty-IUT-state reason.

## Planning Evidence

- Architecture freshness: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-07 and not stale.
- Official OGC 23-002 source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 9 "Requirements Class Datastreams & Observations".
- OGC source verification:
  - Requirements class identifier: `/req/datastream`.
  - Conformance class identifier: `/conf/datastream`.
  - Prerequisite: Requirements Class 1 `/req/api-common`.
  - Selected normative statements for Sprint 21: `/req/datastream/canonical-url`, `/req/datastream/resources-endpoint`, `/req/datastream/canonical-endpoint`, `/req/datastream/ref-from-system`, `/req/datastream/schema-op`, `/req/datastream/obs-canonical-url`, `/req/datastream/obs-resources-endpoint`, `/req/datastream/obs-canonical-endpoint`, and `/req/datastream/obs-ref-from-datastream`.
- GeoRobotix `/conformance` declares `/conf/datastream` and Part 2 encoding classes, but does not declare `/conf/api-common`; scoped endpoint checks may run, but full `/conf/datastream` closure must remain prerequisite-incomplete until `/req/api-common` is established.
- GeoRobotix probes:
  - `GET /datastreams?limit=2`: HTTP 200 JSON with `items` and `links`.
  - `GET /observations?limit=2`: HTTP 200 JSON with `items` and `links`.
  - `GET /datastreams/0mirhn7lo1kg`: HTTP 200 JSON with Datastream fields and an `observations` link.
  - `GET /datastreams/0mirhn7lo1kg/schema`: HTTP 200 JSON with `obsFormat` and `resultSchema`.
  - `GET /datastreams/0mirhn7lo1kg/observations?limit=2`: HTTP 200 JSON with empty `items`; no top-level `links`.
  - `GET /systems/0nar3cl0tk3g/datastreams?limit=1`: HTTP 200 JSON with a Datastream item.

## Generator Requirements

- Add a Part 2 Datastream TestNG group and runtime tests using official OGC 23-002 identifiers only.
- Gate Datastream PASS evidence on `/conf/datastream`.
- Preserve `/req/api-common` prerequisite honesty; do not infer API Common PASS from Datastream endpoint success.
- Do not report full `/conf/datastream` class closure when `/req/api-common` is absent or cannot be established.
- Keep the first increment read-only and bounded to endpoints proven by official Clause 9 and current planning probes.
- Treat empty nested Observation collections as valid endpoint availability evidence when HTTP 200 JSON with `items` is returned.
- Do not PASS `/req/datastream/obs-ref-from-datastream` on an empty nested Observation collection; require actual nested Observation/reference evidence or SKIP.
- Do not require nested Observation collection `links` unless implementing `/req/datastream/obs-collections` specifically.
- Do not implement ControlStream, Command, Part 2 JSON, SWE Common, Create/Replace/Delete, Update, or schema-body validation closure.

## Definition of Done

- [x] OpenSpec defines `REQ-ETS-PART2-002` and Sprint 21 scenarios.
- [x] Sprint contract exists at `.harness/contracts/sprint-ets-21.yaml`.
- [x] Epic ETS-03 maps Datastream planning to `S-ETS-21-01`.
- [x] Traceability maps `FR-ETS-31` to `S-ETS-21-01`.
- [x] Ops status, changelog, test-results, and planner handoff record Sprint 21 planning evidence.
- [x] Raze reviews Sprint 21 planning changes (`GAPS_FOUND` 0.88), required fixes are applied, and gap-fix review approves (`APPROVE` 0.95).
- [ ] Generator implements the planned read-only Datastream subset.
- [ ] Formatter, Maven, and GeoRobotix TeamEngine smoke are run after Generator code changes.

## Out Of Scope

- Mutation behavior.
- ControlStream and Command behavior.
- Part 2 JSON and SWE Common encoding validation.
- Observation result validation against Datastream schemas.
- Full JSON Schema validation.

## Raze Review

- Initial artifact: `.harness/evaluations/sprint-ets-21-plan-adversarial.yaml`
- Initial verdict: `GAPS_FOUND`
- Confidence: 0.88
- Required fixes:
  - Split endpoint availability from `/req/datastream/obs-ref-from-datastream`; empty nested observations can support endpoint availability only.
  - Explicitly block full `/conf/datastream` closure while `/req/api-common` is absent, while allowing scoped endpoint checks to run.
- Gap-fix artifact: `.harness/evaluations/sprint-ets-21-plan-gapfix.yaml`
- Gap-fix verdict: `APPROVE`
- Gap-fix confidence: 0.95
