# S-ETS-31-01: Part 2 SWE Common Binary Encoding

## Status
PLANNED by Sprint 31 planning. Generator implementation is pending.

## User Instruction
Planning triggered by: "Do next logical work item".

## Scope
Plan the first declaration-gated, read-only Generator increment for OGC 23-002 Clause 16.4 Requirements Class "SWE Common Binary Encoding".

- Requirements class: `/req/swecommon-binary`
- Conformance class: `/conf/swecommon-binary`
- Requirement prerequisite: `http://www.opengis.net/spec/SWE/3.0/req/binary-encoding-rules`
- Conformance prerequisite: `http://www.opengis.net/spec/SWE/3.0/conf/binary-encoding-rules`
- Media type: `application/swe+binary`
- Normative statements in scope for planning: Requirements 123-130, covering mediatype-read, mediatype-write, Observation Schema schema/mapping, Observation binary encoding, Command Schema schema/mapping, and Command binary encoding.

## Planning Evidence
- Official source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 16.4 "Requirements Class SWE Common Binary Encoding" and Annex A.12.
- OGC 23-002 names the requirements class `/req/swecommon-binary` and conformance class `/conf/swecommon-binary`.
- OGC 23-002 lists SWE Common 3.0 Binary Encoding Rules as the prerequisite.
- The normative requirements are:
  - `/req/swecommon-binary/mediatype-read`
  - `/req/swecommon-binary/mediatype-write`
  - `/req/swecommon-binary/obsschema-schema`
  - `/req/swecommon-binary/obsschema-mapping`
  - `/req/swecommon-binary/observation-encoding`
  - `/req/swecommon-binary/cmdschema-schema`
  - `/req/swecommon-binary/cmdschema-mapping`
  - `/req/swecommon-binary/command-encoding`
- Resource condition gates:
  - Observation Schema, Observation Schema mapping, and Observation encoding require `/conf/datastream`.
  - Command Schema, Command Schema mapping, and Command encoding require `/conf/controlstream`.
  - `mediatype-write` also requires `/conf/create-replace-delete`, but the first increment is non-mutating API-definition evidence only.
- Source inconsistency notes:
  - Clause 16.4's media-type note still says "SWE Common Text encoding" while discussing the binary media type.
  - Annex A.127 and A.130 say to validate binary Observation/Command responses using a validator implementing Text encoding rules. Generator must document this as source inconsistency and must not pass SWE Common Binary from `TextEncoding`, a text validator, or `application/swe+text`.
- Bundled schema readiness:
  - `src/main/resources/schemas/connected-systems-2/json/observationSchemaSwe.json`
  - `src/main/resources/schemas/connected-systems-2/json/commandSchemaSwe.json`
  - `src/main/resources/schemas/connected-systems-shared/swecommon/schemas/json/*.json`

## GeoRobotix Evidence
- Raw planning probe transcript: `ops/test-results/sprint-ets-31-plan-georobotix-swebinary-probes-2026-05-27.txt`.
- `/conformance` currently declares Part 2 `/conf/swecommon-binary`, `/conf/swecommon-text`, `/conf/swecommon-json`, `/conf/datastream`, `/conf/controlstream`, `/conf/create-replace-delete`, and `/conf/json`.
- `/conformance` does not expose SWE 3.0 `http://www.opengis.net/spec/SWE/3.0/conf/binary-encoding-rules`, `http://www.opengis.net/spec/SWE/3.0/conf/text-encoding-rules`, or `http://www.opengis.net/spec/SWE/3.0/conf/json-encoding-rules`.
- `/conformance` does not expose Part 2 `/conf/api-common`, `/conf/update`, or `/conf/advanced-filtering`.
- Direct read-only probes on 2026-05-27:
  - `GET /conformance`: HTTP 200.
  - `GET /datastreams?limit=1` with `Accept: application/json`: HTTP 500 `application/json`.
  - `GET /datastreams?limit=1` with `Accept: application/swe+binary`: HTTP 500 with no content body.
  - `GET /observations?limit=1` with `Accept: application/swe+binary`: HTTP 500 with no content body.
  - `GET /controlstreams?limit=1`: HTTP 200 `application/json`, first ID `0m4qpft9sdag`.
  - Selected ControlStream formats include `application/json`, `application/swe+json`, `application/swe+csv`, `application/swe+xml`, and `application/swe+binary`.
  - `GET /controlstreams/0m4qpft9sdag/schema?cmdFormat=application/swe+binary`: HTTP 200, but the body reports `commandFormat=application/json` and `parametersSchema`; it does not expose expected `commandFormat=application/swe+binary`, `recordSchema`, and `BinaryEncoding` evidence.
  - `GET /controlstreams/0m4qpft9sdag/commands?limit=1` with `Accept: application/swe+binary`: HTTP 200 `application/json` with empty `items`.
  - `GET /commands?limit=1` with `Accept: application/swe+binary`: HTTP 400.

## Generator Requirements
- Add a Part 2 SWE Common Binary TestNG group with official OGC 23-002 identifiers only.
- Gate all assertions on exact Part 2 `/conf/swecommon-binary` declaration.
- Keep SWE Common 3.0 `/conf/binary-encoding-rules` prerequisite visible; do not claim full `/conf/swecommon-binary` closure when the prerequisite is absent.
- Condition Observation-side assertions on `/conf/datastream` and Command-side assertions on `/conf/controlstream`.
- Implement read-only mediatype-read checks using `Accept: application/swe+binary`; require API-definition or operation evidence plus HTTP 200 and exact `application/swe+binary` content type before PASS.
- Do not use `application/vnd.ogc.swe+binary`, `application/swe+text`, `application/swe+csv`, `application/swe+json`, `application/json`, empty binary bodies, or format-list-only evidence as SWE Common Binary PASS evidence.
- Validate retrieved Observation Schema and Command Schema metadata against bundled `observationSchemaSwe.json` and `commandSchemaSwe.json`.
- Require retrieved schema evidence to show `obsFormat` or `commandFormat` equal to `application/swe+binary`, `recordSchema`, and `encoding` as `BinaryEncoding` before PASS.
- For mapping checks, reuse the canonical Time and IssueTime definition evidence guards from the SWE Common JSON implementation because Annex A.12 delegates mapping checks to the SWE Common JSON mapping tests.
- For Observation and Command encoding checks, require parent schema evidence, candidate child resources, exact binary media type, non-empty body, and an actual SWE Common Binary encoding validator before PASS. If the validator or candidates are absent, SKIP with a precise reason.
- Check `mediatype-write` only through non-mutating API-definition or operation metadata on Observation or Command resource endpoints in this first increment. Do not issue public GeoRobotix POST, PUT, PATCH, or DELETE.
- OPTIONS evidence alone must not PASS `mediatype-write`.
- Add tests/comments referencing `REQ-ETS-PART2-012` and `SCENARIO-ETS-PART2-012-*` IDs.

## Verification
- Direct source verification used official OGC 23-002 HTML for Clause 16.4 and Annex A.12.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-05-09; checked 2026-05-27 and not stale.
- Live IUT probes were non-mutating: `/conformance`, bounded GETs, and schema/media read checks only.
- Mandatory TeamEngine planning E2E: GeoRobotix smoke failed `196 total / 33 passed / 28 failed / 135 skipped`.
- Planning smoke command: `SMOKE_CONTAINER_NAME=ets-csapi-s31-swebinary-plan-georobotix SMOKE_OUTPUT_DIR=/tmp/sprint-ets-31-swebinary-plan-georobotix-results bash scripts/smoke-test.sh`.
- Planning smoke artifacts:
  - `ops/test-results/sprint-ets-31-plan-georobotix-smoke-failed-2026-05-27.xml`
  - `ops/test-results/sprint-ets-31-plan-georobotix-smoke-container-failed-2026-05-27.log`
  - `ops/test-results/sprint-ets-31-plan-georobotix-smoke-console-failed-2026-05-27.log`
- Public-IUT mutation check: `scripts/no-mutation-oracle.py` recognized 91 IUT request logs; explicit counts are archived in `ops/test-results/sprint-ets-31-plan-georobotix-no-mutation-2026-05-27.txt` as `GET=91`, `POST=0`, `PUT=0`, `PATCH=0`, `DELETE=0`.
- Raze planning review: `.harness/evaluations/sprint-ets-31-plan-adversarial.yaml` returned `APPROVE_WITH_CONCERNS` confidence 0.93. The required low bookkeeping fix `RAZE-ETS31-PLAN-CONCERN-001` was closed by post-review reconciliation.

## Definition of Done
- [x] OpenSpec splits `REQ-ETS-PART2-012` out for Part 2 SWE Common Binary and keeps remaining observation-binding placeholder at `REQ-ETS-PART2-013`.
- [x] Story and sprint contract capture official OGC identifiers, prerequisite, media type, and Requirements 123-130.
- [x] Planning captures resource condition gates for `/conf/datastream`, `/conf/controlstream`, and `/conf/create-replace-delete`.
- [x] Planning captures current GeoRobotix declaration and SWE Common Binary read-health state with raw probe transcript artifact.
- [x] Planning explicitly blocks false PASS from declaration alone, sibling declarations, vendor preliminary binary media, text/CSV/JSON format lists, source text-rule typos, non-empty bytes alone, empty candidate sets, unavailable endpoints, JSON fallback schemas, OPTIONS-only write evidence, and public-IUT mutation.
- [x] Planning TeamEngine E2E evidence is captured and documented honestly.
- [x] Public GeoRobotix mutation check records no POST/PUT/PATCH/DELETE request lines.
- [x] Raze reviews planning before Generator starts.
- [ ] Planning-only change is committed and pushed before Generator implementation.

## Out of Scope
- Public GeoRobotix mutation.
- Positive SWE Common Binary write lifecycle behavior.
- Observation-binding cross-class closure beyond the SWE Common Binary schema/mapping evidence guards.
- Full Observation/Command payload semantic validation unless a Generator increment supplies a proven SWE Common Binary encoding validator and candidate resource evidence.
