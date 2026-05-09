# Epic ETS-03: CS API Part 2 Conformance Classes

> Status: Active — Sprint 25 plans the Part 2 Advanced Filtering read-only declaration-gated subset and corrects the stale System History placeholder. | Last updated: 2026-05-09

## Goal
Implement TestNG suite classes for the OGC 23-002 conformance classes (Dynamic Data: datastreams, observations, control streams, commands, system events, advanced filtering, mutation classes, JSON, and SWE Common formats). Sprint 25 corrects the earlier backlog error that treated GeoRobotix's `/conf/system-history` declaration as an OGC 23-002 conformance class; OGC 23-002 Annex A does not define `/conf/system-history`.

## Dependencies
- Depends on: `epic-ets-02-part1-classes` (Part 2 references Part 1 resources, e.g. systems own datastreams; also reuses base test infrastructure)
- Blocks: `epic-ets-05-cite-submission` (Part 2 must be feature-complete for the beta submission to cover the full standard)

## Stories

| ID | Story | Status | OpenSpec Refs |
|----|-------|--------|---------------|
| S-ETS-20-01 | Implement `/conf/api-common` (Part 2) read-only declaration-gated subset using official OGC 23-002 identifiers | Partial Implemented | REQ-ETS-PART2-001 |
| S-ETS-03-01 | (placeholder) Implement `/conf/api-common` (Part 2) suite | Superseded by S-ETS-20-01 planning | REQ-ETS-PART2-001 |
| S-ETS-21-01 | Implement `/conf/datastream` (Part 2 Datastreams & Observations) read-only declaration-gated subset using official OGC 23-002 identifiers | Partial Implemented | REQ-ETS-PART2-002 |
| S-ETS-03-02 | (placeholder) Implement `/conf/datastream` suite | Superseded by S-ETS-21-01 planning | REQ-ETS-PART2-002 |
| S-ETS-22-01 | Implement `/conf/controlstream` (Part 2 Control Streams & Commands) read-only declaration-gated subset using official OGC 23-002 identifiers | Partial Implemented | REQ-ETS-PART2-003 |
| S-ETS-03-03 | (placeholder) Implement `/conf/controlstream` suite | Superseded by S-ETS-22-01 planning | REQ-ETS-PART2-003 |
| S-ETS-23-01 | Implement `/conf/feasibility` (Part 2 Command Feasibility) safety-gated declaration subset using official OGC 23-002 identifiers | Partial Implemented | REQ-ETS-PART2-004 |
| S-ETS-03-04 | (placeholder) Implement `/conf/feasibility` suite | Superseded by S-ETS-23-01 planning | REQ-ETS-PART2-004 |
| S-ETS-24-01 | Implement `/conf/system-event` (Part 2 System Events) read-only declaration-gated subset using official OGC 23-002 identifiers | Partial Implemented | REQ-ETS-PART2-005 |
| S-ETS-03-05 | (placeholder) Implement `/conf/system-event` suite | Superseded by S-ETS-24-01 planning | REQ-ETS-PART2-005 |
| S-ETS-03-06 | (placeholder) Implement `/conf/system-history` suite | Retired — not defined by OGC 23-002 Annex A | retired |
| S-ETS-25-01 | Implement `/conf/advanced-filtering` (Part 2) read-only declaration-gated subset using official OGC 23-002 identifiers | Planned | REQ-ETS-PART2-006 |
| S-ETS-03-07 | (placeholder) Implement `/conf/advanced-filtering` (Part 2) suite | Superseded by S-ETS-25-01 planning | REQ-ETS-PART2-006 |
| S-ETS-03-08 | (placeholder) Implement `/conf/create-replace-delete` (Part 2) suite | Deferred | REQ-ETS-PART2-007 |
| S-ETS-03-09 | (placeholder) Implement `/conf/update` (Part 2) suite | Deferred | REQ-ETS-PART2-008 |
| S-ETS-03-10 | (placeholder) Implement `/conf/json` (Part 2) suite | Deferred | REQ-ETS-PART2-009 |
| S-ETS-03-11 | (placeholder) Implement `/conf/swecommon-json` suite | Deferred | REQ-ETS-PART2-010 |
| S-ETS-03-12 | (placeholder) Implement `/conf/swecommon-text` suite | Deferred | REQ-ETS-PART2-011 |
| S-ETS-03-13 | (placeholder) Implement `/conf/swecommon-binary` suite | Deferred | REQ-ETS-PART2-012 |
| S-ETS-03-14 | (placeholder) Implement `/conf/observation-binding` cross-class (Observation body schema derives from Datastream schema, per v1.0 GH#7) | Deferred | REQ-ETS-PART2-013 |

## Acceptance Criteria
- [ ] All OGC 23-002 Part 2 conformance classes have at least one `@Test` per ATS assertion
- [ ] Cross-class dynamic schema coupling (REQ-ETS-PART2-013) is enforced — Observation/Command bodies validate against parent-resource schema
- [ ] Same dependency-aware skip semantics as Part 1
- [ ] Same HTTP capture, schema validation, credential masking semantics as Part 1
- [ ] Part 2 suite pass rate ≥95% against GeoRobotix demo server for IUT-declared classes

## Notes
- Sprint 1 explicitly **excluded** Part 2 work per user decision 2026-04-27.
- Sprint 20 activates the Part 2 track with a read-only, declaration-gated Part 2 API Common subset before Part 2 JSON or resource-specific classes.
- Sprint 21 partially implements the first read-only Datastreams & Observations subset. OGC 23-002 Clause 9 names the class `/req/datastream` with conformance `/conf/datastream` and prerequisite `/req/api-common`; full closure remains prerequisite-incomplete on GeoRobotix because `/conf/api-common` is absent.
- Sprint 22 partially implements the first read-only Control Streams & Commands subset. OGC 23-002 Clause 10 names the class `/req/controlstream` with conformance `/conf/controlstream` and prerequisite `/req/api-common`; GeoRobotix currently serves scoped `/controlstreams` endpoints but returns HTTP 400 for `/commands` and `/controls/{id}`, so the runtime tests SKIP those global/canonical assertions instead of producing false PASS.
- Sprint 23 partially implements Command Feasibility. OGC 23-002 Clause 11 names the class `/req/feasibility` with conformance `/conf/feasibility` and prerequisite `/req/controlstream`; GeoRobotix currently does not declare `/conf/feasibility`, so the 7 Feasibility runtime tests SKIP before any feasibility POST and the smoke no-mutation oracle reports zero IUT-bound POST/PUT/DELETE/PATCH.
- Sprint 24 partially implements System Events. OGC 23-002 Clause 12 names the class `/req/system-event` with conformance `/conf/system-event`, prerequisites `/req/api-common` and Part 1 `/req/system`, canonical endpoint `/systemEvents`, and system-scoped endpoint `/systems/{sysId}/events`. GeoRobotix declares `/conf/system-event`, so the declaration test PASSes, but the other 5 System Events runtime tests SKIP because `/conf/api-common` is absent, `/systemEvents` returns HTTP 400, `/systems/{id}/events` returns streaming-only HTTP 400, no SystemEvent resource evidence is available, and no `itemType=SystemEvent` collection is advertised.
- Sprint 25 plans Advanced Filtering. OGC 23-002 Clause 13 names the class `/req/advanced-filtering` with conformance `/conf/advanced-filtering`, prerequisites `/req/api-common` and Part 1 `/req/advanced-filtering`, and Requirements 45-62 for DataStream, Observation, ControlStream, Command, CommandStatus, and SystemEvent filters. GeoRobotix does not declare `/conf/advanced-filtering`, so successful read-only filter probes must remain diagnostics and must not produce conformance PASS.
- Sprint 25 correction: OGC 23-002 Annex A does not define `/conf/system-history`; the prior placeholder is retired from the OGC conformance-class backlog.
- Historical web-app Part 2 story IDs and `dynamic-*` names are not authoritative for the Java ETS; use official OGC 23-002 identifiers.
