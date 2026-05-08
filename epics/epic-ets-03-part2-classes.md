# Epic ETS-03: CS API Part 2 Conformance Classes

> Status: Active — Sprint 23 plans the Part 2 Command Feasibility safety-gated subset after Sprint 22 Control Streams & Commands. | Last updated: 2026-05-08

## Goal
Implement TestNG suite classes for all 14 OGC 23-002 conformance classes (Dynamic Data: datastreams, observations, control streams, commands, system events, system history, SWE Common formats, plus Part 2 Common/JSON/CRUD/Update/advanced-filtering). Owns sub-deliverable 3 of the new ETS capability.

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
| S-ETS-23-01 | Implement `/conf/feasibility` (Part 2 Command Feasibility) safety-gated declaration subset using official OGC 23-002 identifiers | Planned | REQ-ETS-PART2-004 |
| S-ETS-03-04 | (placeholder) Implement `/conf/feasibility` suite | Superseded by S-ETS-23-01 planning | REQ-ETS-PART2-004 |
| S-ETS-03-05 | (placeholder) Implement `/conf/system-event` suite | Deferred | REQ-ETS-PART2-005 |
| S-ETS-03-06 | (placeholder) Implement `/conf/system-history` suite | Deferred | REQ-ETS-PART2-006 |
| S-ETS-03-07 | (placeholder) Implement `/conf/advanced-filtering` (Part 2) suite | Deferred | REQ-ETS-PART2-007 |
| S-ETS-03-08 | (placeholder) Implement `/conf/create-replace-delete` (Part 2) suite | Deferred | REQ-ETS-PART2-008 |
| S-ETS-03-09 | (placeholder) Implement `/conf/update` (Part 2) suite | Deferred | REQ-ETS-PART2-009 |
| S-ETS-03-10 | (placeholder) Implement `/conf/json` (Part 2) suite | Deferred | REQ-ETS-PART2-010 |
| S-ETS-03-11 | (placeholder) Implement `/conf/swecommon-json` suite | Deferred | REQ-ETS-PART2-011 |
| S-ETS-03-12 | (placeholder) Implement `/conf/swecommon-text` suite | Deferred | REQ-ETS-PART2-012 |
| S-ETS-03-13 | (placeholder) Implement `/conf/swecommon-binary` suite | Deferred | REQ-ETS-PART2-013 |
| S-ETS-03-14 | (placeholder) Implement `/conf/observation-binding` cross-class (Observation body schema derives from Datastream schema, per v1.0 GH#7) | Deferred | REQ-ETS-PART2-014 |

## Acceptance Criteria
- [ ] All 14 Part 2 conformance classes have at least one `@Test` per ATS assertion
- [ ] Cross-class dynamic schema coupling (REQ-ETS-PART2-014) is enforced — Observation/Command bodies validate against parent-resource schema
- [ ] Same dependency-aware skip semantics as Part 1
- [ ] Same HTTP capture, schema validation, credential masking semantics as Part 1
- [ ] Part 2 suite pass rate ≥95% against GeoRobotix demo server for IUT-declared classes

## Notes
- Sprint 1 explicitly **excluded** Part 2 work per user decision 2026-04-27.
- Sprint 20 activates the Part 2 track with a read-only, declaration-gated Part 2 API Common subset before Part 2 JSON or resource-specific classes.
- Sprint 21 partially implements the first read-only Datastreams & Observations subset. OGC 23-002 Clause 9 names the class `/req/datastream` with conformance `/conf/datastream` and prerequisite `/req/api-common`; full closure remains prerequisite-incomplete on GeoRobotix because `/conf/api-common` is absent.
- Sprint 22 partially implements the first read-only Control Streams & Commands subset. OGC 23-002 Clause 10 names the class `/req/controlstream` with conformance `/conf/controlstream` and prerequisite `/req/api-common`; GeoRobotix currently serves scoped `/controlstreams` endpoints but returns HTTP 400 for `/commands` and `/controls/{id}`, so the runtime tests SKIP those global/canonical assertions instead of producing false PASS.
- Sprint 23 plans Command Feasibility. OGC 23-002 Clause 11 names the class `/req/feasibility` with conformance `/conf/feasibility` and prerequisite `/req/controlstream`; GeoRobotix currently does not declare `/conf/feasibility` and returns HTTP 400 for probed feasibility URLs, so Generator must declaration-SKIP by default and must not issue public-IUT feasibility POSTs.
- Historical web-app Part 2 story IDs and `dynamic-*` names are not authoritative for the Java ETS; use official OGC 23-002 identifiers.
