# Story S-ETS-22-01: Part 2 Control Streams & Commands Read-Only Subset

> Status: Partial Implemented | Epic: epic-ets-03-part2-classes | Sprint: ets-22 | Last updated: 2026-05-08

## Context

Sprint 22 continues Part 2 after Sprint 21 Datastreams & Observations. The target is OGC 23-002 Clause 10, Requirements Class "Control Streams & Commands", using official `/req/controlstream` and `/conf/controlstream` identifiers.

## Scope

Implement the first read-only `REQ-ETS-PART2-003` Generator increment:

1. `/conf/controlstream` declaration gate.
2. `GET /controlstreams` ControlStream collection availability.
3. Selected ControlStream item read using the IUT-exposed resource path.
4. `GET /controlstreams/{id}/schema` ControlStream schema sub-resource.
5. `GET /controlstreams/{id}/commands` ControlStream-scoped Command collection endpoint availability.
6. `GET /systems/{systemId}/controlstreams` when a selected ControlStream exposes `system@id`.
7. Honest prerequisite handling for `/req/api-common` without claiming API Common PASS from ControlStream evidence.
8. Explicit alias/global-command honesty: `/controls/{id}` and `/commands` currently return HTTP 400 on GeoRobotix, so Generator must not PASS `/req/controlstream/canonical-url` or global Command endpoint assertions from `/controlstreams/{id}` or nested-command evidence alone.
9. `/req/controlstream/cmd-ref-from-controlstream` only when at least one nested Command item or link gives actual ControlStream association evidence; otherwise SKIP with an empty-IUT-state reason.

## Planning Evidence

- Architecture freshness: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-07 and not stale.
- Official OGC 23-002 source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 10 "Requirements Class Control Streams & Commands".
- OGC source verification:
  - Requirements class identifier: `/req/controlstream`.
  - Conformance class identifier: `/conf/controlstream`.
  - Prerequisite: Requirements Class 1 `/req/api-common`.
  - Selected normative statements for Sprint 22: `/req/controlstream/resources-endpoint`, `/req/controlstream/canonical-endpoint`, `/req/controlstream/ref-from-system`, `/req/controlstream/schema-op`, `/req/controlstream/cmd-resources-endpoint`, `/req/controlstream/cmd-ref-from-controlstream`, and prerequisite/declaration checks around `/req/controlstream/canonical-url`, `/req/controlstream/cmd-canonical-endpoint`, and `/req/api-common`.
- GeoRobotix `/conformance` declares `/conf/controlstream`, but does not declare `/conf/api-common`; scoped endpoint checks may run, but full `/conf/controlstream` closure must remain prerequisite-incomplete until `/req/api-common` is established.
- GeoRobotix probes:
  - `GET /controlstreams?limit=2`: HTTP 200 JSON with `items` and `links`.
  - `GET /controlstreams/0m4qpft9sdag`: HTTP 200 JSON with ControlStream fields.
  - `GET /controlstreams/0m4qpft9sdag/schema`: HTTP 200 JSON with `commandFormat` and `parametersSchema`.
  - `GET /controlstreams/0m4qpft9sdag/commands?limit=2`: HTTP 200 JSON with empty `items`.
  - `GET /systems/0m5ojudgr570/controlstreams?limit=2`: HTTP 200 JSON with ControlStream items.
  - `GET /commands?limit=2`: HTTP 400 HTML.
  - `GET /controls/0m4qpft9sdag`: HTTP 400 HTML.

## Generator Requirements

- Add a Part 2 ControlStream TestNG group and runtime tests using official OGC 23-002 identifiers only.
- Gate scoped ControlStream PASS evidence on `/conf/controlstream`.
- Preserve `/req/api-common` prerequisite honesty; do not infer API Common PASS from ControlStream endpoint success.
- Do not report full `/conf/controlstream` class closure when `/req/api-common` is absent or cannot be established.
- Keep the first increment read-only and bounded to endpoints proven by official Clause 10 and current planning probes.
- Treat empty ControlStream-scoped Command collections as valid endpoint availability evidence when HTTP 200 JSON with `items` is returned.
- Do not PASS `/req/controlstream/cmd-ref-from-controlstream` on an empty nested Command collection; require actual nested Command/reference evidence or SKIP.
- Do not PASS `/req/controlstream/canonical-url` using `/controlstreams/{id}` alias evidence when the official `/controls/{id}` path returns non-200.
- Do not PASS `/req/controlstream/cmd-canonical-endpoint` when `/commands` returns non-200.
- Do not implement Command creation, feasibility POST, command status/result endpoint closure, ControlStream mutation, Part 2 JSON, SWE Common, Create/Replace/Delete, Update, or command-body schema validation closure.

## Definition of Done

- [x] OpenSpec defines `REQ-ETS-PART2-003` and Sprint 22 scenarios.
- [x] Sprint contract exists at `.harness/contracts/sprint-ets-22.yaml`.
- [x] Epic ETS-03 maps ControlStream planning to `S-ETS-22-01`.
- [x] Traceability maps `FR-ETS-32` to `S-ETS-22-01`.
- [x] Ops status, changelog, test-results, known issues, and planner handoff record Sprint 22 planning evidence.
- [x] Raze reviews Sprint 22 planning changes (`APPROVE` 0.93; no required fixes).
- [x] Generator implements the planned read-only ControlStream subset.
- [x] Formatter, Maven, and GeoRobotix TeamEngine smoke are run after Generator code changes.

## Generator Evidence

- Implementation: `Part2ControlStreamTests` adds declaration-gated checks for `/conf/controlstream`, `/controlstreams`, `/controlstreams/{id}`, `/controlstreams/{id}/schema`, `/controlstreams/{id}/commands`, `/commands` when available, `/controls/{id}` when available, populated nested Command reference evidence, and bounded `/systems/{systemId}/controlstreams`.
- Structural regressions: `VerifyPart2ControlStreamTests` prevents generic JSON and empty nested Command collections from becoming false PASS evidence; `VerifyTestNGSuiteDependency` keeps `part2controlstream` co-located with Core/Common and independent of `part2apicommon` cascade.
- Maven: `bash scripts/mvn-test-via-docker.sh` reported `167 tests / 0 failures / 0 errors / 3 skipped`.
- TeamEngine smoke: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s22 bash scripts/smoke-test.sh` reported `115 total / 71 passed / 0 failed / 44 skipped`.
- GeoRobotix runtime outcome: seven ControlStream tests PASS; four SKIP honestly for missing `/conf/api-common`, `/controls/{id}` HTTP 400, `/commands` HTTP 400, and empty nested Command reference evidence.
- Smoke artifacts: `ops/test-results/sprint-ets-22-smoke-2026-05-08.xml` and `ops/test-results/sprint-ets-22-smoke-container-2026-05-08.log`.

## Out Of Scope

- Mutation behavior.
- Command creation or write-side command body validation.
- Command feasibility.
- Command status/result endpoint closure.
- Part 2 JSON and SWE Common encoding validation.
- Full JSON Schema validation.

## Raze Review

- Artifact: `.harness/evaluations/sprint-ets-22-plan-adversarial.yaml`
- Verdict: `APPROVE`
- Confidence: 0.93
- Required fixes: none
- Implementation artifact: `.harness/evaluations/sprint-ets-22-adversarial-implementation.yaml`
- Implementation verdict: `GAPS_FOUND` confidence 0.91 for docs/evidence gaps only
- Implementation gap status: planner handoff superseded and Maven evidence archived at `ops/test-results/sprint-ets-22-maven-2026-05-08.log`
- Gap-fix artifact: `.harness/evaluations/sprint-ets-22-adversarial-gapfix.yaml`
- Gap-fix verdict: `APPROVE` confidence 0.95 with no required fixes
