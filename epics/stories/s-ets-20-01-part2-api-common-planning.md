# Story S-ETS-20-01: Part 2 API Common Planning

> Status: Planned | Epic: epic-ets-03-part2-classes | Sprint: ets-20 | Last updated: 2026-05-07

## Context

Sprint 20 activates the Part 2 track after the Part 1 conformance-class skeleton reached all 14 classes. This is planning only; Generator implementation comes next.

The older frozen web-app story `S09-01` uses stale names such as `dynamic-common` and `dynamic-json`. OGC 23-002 uses `/req/api-common`, `/conf/api-common`, `/req/json`, and `/conf/json`. Sprint 20 adopts the official OGC 23-002 identifiers.

## Scope

Plan `REQ-ETS-PART2-001` for the OGC 23-002 Requirements Class "Common":

1. `/req/api-common/resources`
2. `/req/api-common/resource-collection`
3. Dependency on Part 1 API Common/Core availability before judging Part 2 resource behavior.
4. Declaration-gated Part 2 Common runtime behavior with honest SKIP when `/conf/api-common` is not declared.
5. Read-only resource collection smoke for available Part 2 collection endpoints.

## Planning Evidence

- Architecture freshness: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-07 and not stale.
- Official OGC 23-002 source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 8 "Requirements Class Common".
- OGC source verification:
  - Requirements class identifier: `/req/api-common`.
  - Conformance class identifier: `/conf/api-common`.
  - Prerequisite: `http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/req/api-common`.
  - Normative statements: `/req/api-common/resources` and `/req/api-common/resource-collection`.
- GeoRobotix `/conformance` declares Part 2 `/conf/datastream`, `/conf/controlstream`, `/conf/json`, `/conf/create-replace-delete`, `/conf/system-event`, `/conf/system-history`, and SWE Common encoding classes, but does not currently declare `/conf/api-common`.
- GeoRobotix landing page exposes `datastreams` and `observations` links.
- GeoRobotix collection probes:
  - `GET /datastreams?limit=1`: HTTP 200, JSON body with `items` and `links`.
  - `GET /observations?limit=1`: HTTP 200, JSON body with `items` and `links`.
  - `GET /controlstreams?limit=1`: HTTP 200, JSON body with `items` and `links`.
  - `GET /commands?limit=1`: HTTP 400 in current IUT state; Sprint 20 planning must not assume `/commands` collection availability from landing-page links.

## Planned Generator Requirements

- Add a Part 2 API Common test group and TestNG dependency wiring behind Part 1 Core/Common availability.
- Declare `/conf/api-common` absence as SKIP for Part 2 API Common conformance declaration and resource-judgment tests, not PASS or FAIL.
- Probe only read-only collection endpoints in the first Generator increment.
- Treat `datastreams`, `observations`, and `controlstreams` as useful current-IUT planning evidence, not as universal endpoint requirements for every IUT.
- Do not implement Part 2 JSON, Datastream, ControlStream, SWE Common, CRUD, Update, or mutation behavior in this story.
- Do not promote old `dynamic-*` identifiers into Java `@Test` descriptions.

## Definition of Done

- [x] OpenSpec defines `REQ-ETS-PART2-001` and Sprint 20 scenarios with official OGC 23-002 identifiers.
- [x] Sprint contract exists at `.harness/contracts/sprint-ets-20.yaml`.
- [x] Epic ETS-03 is activated for Part 2 planning.
- [x] Traceability maps `FR-ETS-30` to `S-ETS-20-01`.
- [x] Ops status, changelog, test-results, and planner handoff record Sprint 20 planning evidence.
- [x] Raze reviews Sprint 20 planning changes (`APPROVE_WITH_CONCERNS` 0.92, no required fixes).

## Raze Review

- Artifact: `.harness/evaluations/sprint-ets-20-plan-adversarial.yaml`
- Verdict: `APPROVE_WITH_CONCERNS`
- Confidence: 0.92
- Required fixes: none
- Non-blocking concern: broader Part 2 placeholder taxonomy still says 14 classes and duplicates API Common under the remaining placeholder block. This does not block `S-ETS-20-01`, but should be cleaned before planning later Part 2 decomposition.

## Out Of Scope

- Part 2 JSON encoding (`/req/json`)
- Datastream and Observation conformance closure
- ControlStream and Command conformance closure
- System events/history
- Part 2 Create/Replace/Delete and Update mutation
- SWE Common JSON/Text/Binary encoding
- Full schema validation
