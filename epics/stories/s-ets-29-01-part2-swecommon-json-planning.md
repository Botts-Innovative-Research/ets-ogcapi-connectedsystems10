# S-ETS-29-01: Part 2 SWE Common JSON Encoding

## Status
SPECIFIED_PLANNED by Sprint 29 planning. Generator implementation has not started.

## User Instruction
Planning triggered by: "Do 1" after Sprint 28 JSON Encoding Generator was implemented, reconciled, and pushed.

## Scope
Plan the first declaration-gated, read-only Generator increment for OGC 23-002 Clause 16.2 Requirements Class "SWE Common JSON Encoding".

- Requirements class: `/req/swecommon-json`
- Conformance class: `/conf/swecommon-json`
- Requirement prerequisite: `http://www.opengis.net/spec/SWE/3.0/req/json-encoding-rules`
- Conformance prerequisite: `http://www.opengis.net/spec/SWE/3.0/conf/json-encoding-rules`
- Media type: `application/swe+json`
- Normative statements in scope for planning: Requirements 107-114, covering mediatype-read, mediatype-write, Observation Schema schema/mapping, Observation encoding, Command Schema schema/mapping, and Command encoding.

## Planning Evidence
- Official source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 16.2 "Requirements Class SWE Common JSON Encoding" and Annex A.10.
- OGC 23-002 names the requirements class `/req/swecommon-json` and conformance class `/conf/swecommon-json`.
- OGC 23-002 lists SWE Common 3.0 JSON Encoding Rules as the prerequisite.
- The normative requirements are:
  - `/req/swecommon-json/mediatype-read`
  - `/req/swecommon-json/mediatype-write`
  - `/req/swecommon-json/obsschema-schema`
  - `/req/swecommon-json/obsschema-mapping`
  - `/req/swecommon-json/observation-encoding`
  - `/req/swecommon-json/cmdschema-schema`
  - `/req/swecommon-json/cmdschema-mapping`
  - `/req/swecommon-json/command-encoding`
- Resource condition gates:
  - Observation Schema, Observation Schema mapping, and Observation encoding require `/conf/datastream`.
  - Command Schema, Command Schema mapping, and Command encoding require `/conf/controlstream`.
  - `mediatype-write` also requires `/conf/create-replace-delete`, but the first increment is non-mutating API-definition evidence only.
- Bundled schema readiness:
  - `src/main/resources/schemas/connected-systems-2/json/observationSchemaSwe.json`
  - `src/main/resources/schemas/connected-systems-2/json/commandSchemaSwe.json`
  - `src/main/resources/schemas/connected-systems-shared/swecommon/schemas/json/*.json`

## GeoRobotix Evidence
- `/conformance` currently declares Part 2 `/conf/swecommon-json`, `/conf/swecommon-text`, `/conf/swecommon-binary`, `/conf/datastream`, `/conf/controlstream`, `/conf/create-replace-delete`, and `/conf/json`.
- `/conformance` does not expose SWE 3.0 `http://www.opengis.net/spec/SWE/3.0/conf/json-encoding-rules`, Part 2 `/conf/api-common`, `/conf/update`, or `/conf/advanced-filtering`.
- Direct read probes on 2026-05-26:
  - `GET /conformance`: HTTP 200.
  - `GET /datastreams?limit=1` with `Accept: application/json`: HTTP 500 `application/json`.
  - `GET /datastreams?limit=1` with `Accept: application/swe+json`: HTTP 500 `application/json`.
  - `GET /observations?limit=1` with `Accept: application/swe+json`: HTTP 500 `application/json`.
  - `GET /controlstreams?limit=1`: HTTP 200 `application/json`, first ID `0m4qpft9sdag`.
  - Selected ControlStream formats include `application/json`, `application/swe+json`, `application/swe+csv`, `application/swe+xml`, and `application/swe+binary`.
  - `GET /controlstreams/0m4qpft9sdag/schema?cmdFormat=application/swe+json`: HTTP 200 with content type reported as `auto`, but the body reports `commandFormat=application/json` and `parametersSchema`; it does not expose the expected `commandFormat=application/swe+json`, `recordSchema`, and `JSONEncoding` evidence.
  - `GET /controlstreams/0m4qpft9sdag/commands?limit=1` with `Accept: application/swe+json`: HTTP 200 JSON with empty `items`.
  - `GET /commands?limit=1` with `Accept: application/swe+json`: HTTP 400 JSON.
  - `GET /systemEvents?limit=1` with `Accept: application/swe+json`: HTTP 400 JSON.

## Local OSH Evidence
- `field-hub-osh-1` is running but unhealthy.
- The current shell has no `SMOKE_AUTH_CREDENTIAL`.
- Unauthenticated `GET http://localhost:8081/sensorhub/api/conformance`: HTTP 401 `text/html;charset=iso-8859-1`.

## Generator Requirements
- Add a Part 2 SWE Common JSON TestNG group with official OGC 23-002 identifiers only.
- Gate all assertions on exact Part 2 `/conf/swecommon-json` declaration.
- Keep SWE Common 3.0 `/conf/json-encoding-rules` prerequisite visible; do not claim full `/conf/swecommon-json` closure when the prerequisite is absent.
- Condition Observation-side assertions on `/conf/datastream` and Command-side assertions on `/conf/controlstream`.
- Implement read-only mediatype-read checks using `Accept: application/swe+json`; require API-definition or operation evidence plus HTTP 200, exact `application/swe+json` content type, and parseable JSON before PASS.
- Validate retrieved Observation Schema and Command Schema resources against bundled `observationSchemaSwe.json` and `commandSchemaSwe.json`.
- Require retrieved schema evidence to show `obsFormat` or `commandFormat` equal to `application/swe+json`, `recordSchema`, and `encoding` as `JSONEncoding` before PASS.
- For mapping checks, require Time component evidence from `recordSchema`; do not PASS from hardcoded examples, sibling JSON schemas, or media format lists.
- For Observation and Command encoding checks, require parent schema evidence, candidate child resources, and an actual SWE Common JSON encoding validator before PASS. If the validator or candidates are absent, SKIP with a precise reason.
- Check `mediatype-write` only through non-mutating API-definition or operation metadata in this first increment. Do not issue public GeoRobotix POST, PUT, PATCH, or DELETE.
- OPTIONS evidence alone must not PASS `mediatype-write`.
- Add tests/comments referencing `REQ-ETS-PART2-010` and `SCENARIO-ETS-PART2-010-*` IDs.

## Planning Verification
- Direct source verification used official OGC 23-002 HTML for Clause 16.2 and Annex A.10.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-05-09; checked 2026-05-26 and not stale.
- Live IUT probes were non-mutating: `/conformance`, bounded GETs, and schema/media read checks only.
- Mandatory TeamEngine planning E2E ran from a `/tmp` clone with `SMOKE_CONTAINER_NAME=ets-csapi-s29-swejson-plan-georobotix SMOKE_OUTPUT_DIR=/tmp/sprint-ets-29-swejson-plan-georobotix-results bash scripts/smoke-test.sh`.
- Planning TeamEngine E2E result: FAILED, `176 total / 29 passed / 16 failed / 131 skipped`.
- Planning TeamEngine artifacts: `ops/test-results/sprint-ets-29-plan-georobotix-smoke-failed-2026-05-26.xml` and `ops/test-results/sprint-ets-29-plan-georobotix-smoke-container-failed-2026-05-26.log`.
- Planning E2E interpretation: this is a captured failed public-IUT check, not a passing E2E gate. Failures are inherited from current public GeoRobotix read-path and schema-validation state; no Sprint 29 SWE Common JSON runtime tests exist yet.
- Public-IUT mutation check: `scripts/no-mutation-oracle.py` was inconclusive for this log format, but explicit container-log grep found 75 GeoRobotix GET request lines and zero matched GeoRobotix POST/PUT/PATCH/DELETE request lines.
- Raze planning review `.harness/evaluations/sprint-ets-29-plan-adversarial.yaml` returned `APPROVE_WITH_CONCERNS` confidence 0.93 with no required fixes. Low non-blocking concerns: raw direct probe transcripts are summarized rather than archived, and Generator should reproduce or archive any probe bodies used for PASS/SKIP behavior.

## Definition of Done
- [x] OpenSpec splits `REQ-ETS-PART2-010` out for Part 2 SWE Common JSON and keeps remaining placeholders at `REQ-ETS-PART2-011..013`.
- [x] Story and sprint contract capture official OGC identifiers, prerequisite, media type, and Requirements 107-114.
- [x] Planning captures resource condition gates for `/conf/datastream`, `/conf/controlstream`, and `/conf/create-replace-delete`.
- [x] Planning captures current GeoRobotix declaration and SWE Common JSON read-health state.
- [x] Planning captures current local OSH unauthenticated readiness limits.
- [x] Planning explicitly blocks false PASS from declaration alone, sibling declarations, media-format lists, empty candidate sets, unavailable endpoints, JSON fallback schemas, OPTIONS-only write evidence, and public-IUT mutation.
- [x] Planning TeamEngine E2E evidence is captured and documented honestly.
- [x] Public GeoRobotix mutation check records no POST/PUT/PATCH/DELETE request lines.
- [x] Raze reviews planning before Generator starts.
- [x] Planning-only change is committed and pushed before Generator implementation (`690dbd3 Plan Sprint 29 Part 2 SWE Common JSON`, `be7f1a6..690dbd3 main -> main`).

## Out of Scope
- Runtime implementation in this planning step.
- Public GeoRobotix mutation.
- Positive SWE Common JSON write lifecycle behavior.
- SWE Common Text and SWE Common Binary encoding classes.
- Full Observation/Command payload semantic validation unless a Generator increment supplies a proven SWE Common JSON encoding validator and candidate resource evidence.
- Observation-binding cross-class closure beyond the SWE Common JSON schema/mapping evidence guards.
