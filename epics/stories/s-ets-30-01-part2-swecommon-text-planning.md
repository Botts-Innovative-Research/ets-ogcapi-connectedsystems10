# S-ETS-30-01: Part 2 SWE Common Text Encoding

## Status
PLANNED by Sprint 30 planning. Generator implementation is pending.

## User Instruction
Planning triggered by: "Start the spec-first planning".

## Scope
Plan the first declaration-gated, read-only Generator increment for OGC 23-002 Clause 16.3 Requirements Class "SWE Common Text Encoding".

- Requirements class: `/req/swecommon-text`
- Conformance class: `/conf/swecommon-text`
- Requirement prerequisite: `http://www.opengis.net/spec/SWE/3.0/req/text-encoding-rules`
- Conformance prerequisite: `http://www.opengis.net/spec/SWE/3.0/conf/text-encoding-rules`
- Media type: `application/swe+text`
- Normative statements in scope for planning: Requirements 115-122, covering mediatype-read, mediatype-write, Observation Schema schema/mapping, Observation text encoding, Command Schema schema/mapping, and Command text encoding.

## Planning Evidence
- Official source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 16.3 "Requirements Class SWE Common Text Encoding" and Annex A.11.
- OGC 23-002 names the requirements class `/req/swecommon-text` and conformance class `/conf/swecommon-text`.
- OGC 23-002 lists SWE Common 3.0 Text Encoding Rules as the prerequisite.
- The normative requirements are:
  - `/req/swecommon-text/mediatype-read`
  - `/req/swecommon-text/mediatype-write`
  - `/req/swecommon-text/obsschema-schema`
  - `/req/swecommon-text/obsschema-mapping`
  - `/req/swecommon-text/observation-encoding`
  - `/req/swecommon-text/cmdschema-schema`
  - `/req/swecommon-text/cmdschema-mapping`
  - `/req/swecommon-text/command-encoding`
- Resource condition gates:
  - Observation Schema, Observation Schema mapping, and Observation encoding require `/conf/datastream`.
  - Command Schema, Command Schema mapping, and Command encoding require `/conf/controlstream`.
  - `mediatype-write` also requires `/conf/create-replace-delete`, but the first increment is non-mutating API-definition evidence only.
- Annex A.115 note: the mediatype-read API-definition bullet currently mentions `application/swe+binary`, while Clause 16.3 and the remaining A.115 steps use `application/swe+text`. Generator must document this as source inconsistency and must not use binary advertisement as SWE Common Text PASS evidence.
- Bundled schema readiness:
  - `src/main/resources/schemas/connected-systems-2/json/observationSchemaSwe.json`
  - `src/main/resources/schemas/connected-systems-2/json/commandSchemaSwe.json`
  - `src/main/resources/schemas/connected-systems-shared/swecommon/schemas/json/*.json`

## GeoRobotix Evidence
- Raw planning probe transcript: `ops/test-results/sprint-ets-30-plan-georobotix-swetext-probes-2026-05-26.txt`.
- `/conformance` currently declares Part 2 `/conf/swecommon-text`, `/conf/swecommon-json`, `/conf/swecommon-binary`, `/conf/datastream`, `/conf/controlstream`, `/conf/create-replace-delete`, and `/conf/json`.
- `/conformance` does not expose SWE 3.0 `http://www.opengis.net/spec/SWE/3.0/conf/text-encoding-rules`, `http://www.opengis.net/spec/SWE/3.0/conf/json-encoding-rules`, or `http://www.opengis.net/spec/SWE/3.0/conf/binary-encoding-rules`.
- `/conformance` does not expose Part 2 `/conf/api-common`, `/conf/update`, or `/conf/advanced-filtering`.
- Direct read-only probes on 2026-05-26:
  - `GET /conformance`: HTTP 200.
  - `GET /datastreams?limit=1` with `Accept: application/json`: HTTP 500 `application/json`.
  - `GET /datastreams?limit=1` with `Accept: application/swe+text`: HTTP 500.
  - `GET /observations?limit=1` with `Accept: application/swe+text`: HTTP 500.
  - `GET /controlstreams?limit=1`: HTTP 200 `application/json`, first ID `0m4qpft9sdag`.
  - Selected ControlStream formats include `application/json`, `application/swe+json`, `application/swe+csv`, `application/swe+xml`, and `application/swe+binary`; it did not list `application/swe+text`.
  - `GET /controlstreams/0m4qpft9sdag/schema?cmdFormat=application/swe+text`: HTTP 200, but the body reports `commandFormat=application/json` and `parametersSchema`; it does not expose expected `commandFormat=application/swe+text`, `recordSchema`, and `TextEncoding` evidence.
  - `GET /controlstreams/0m4qpft9sdag/commands?limit=1` with `Accept: application/swe+text`: HTTP 200 `application/json` with empty `items`.
  - `GET /commands?limit=1` with `Accept: application/swe+text`: HTTP 400.

## Local OSH Evidence
- `field-hub-osh-1` is running but unhealthy.
- The current shell has no `SMOKE_AUTH_CREDENTIAL`.
- Unauthenticated `GET http://localhost:8081/sensorhub/api/conformance`: HTTP 401 `text/html;charset=iso-8859-1`.

## Generator Requirements
- Add a Part 2 SWE Common Text TestNG group with official OGC 23-002 identifiers only.
- Gate all assertions on exact Part 2 `/conf/swecommon-text` declaration.
- Keep SWE Common 3.0 `/conf/text-encoding-rules` prerequisite visible; do not claim full `/conf/swecommon-text` closure when the prerequisite is absent.
- Condition Observation-side assertions on `/conf/datastream` and Command-side assertions on `/conf/controlstream`.
- Implement read-only mediatype-read checks using `Accept: application/swe+text`; require API-definition or operation evidence plus HTTP 200 and exact `application/swe+text` content type before PASS.
- Do not use `application/swe+csv`, `application/swe+binary`, `application/swe+json`, `application/json`, or preliminary vendor media type evidence as SWE Common Text PASS evidence.
- Validate retrieved Observation Schema and Command Schema metadata against bundled `observationSchemaSwe.json` and `commandSchemaSwe.json`.
- Require retrieved schema evidence to show `obsFormat` or `commandFormat` equal to `application/swe+text`, `recordSchema`, and `encoding` as `TextEncoding` before PASS.
- For mapping checks, reuse the canonical Time and IssueTime definition evidence guards from the SWE Common JSON implementation because Annex A.11 delegates mapping checks to the SWE Common JSON mapping tests.
- For Observation and Command encoding checks, require parent schema evidence, candidate child resources, and an actual SWE Common Text encoding validator before PASS. If the validator or candidates are absent, SKIP with a precise reason.
- Check `mediatype-write` only through non-mutating API-definition or operation metadata on Observation or Command resource endpoints in this first increment. Do not issue public GeoRobotix POST, PUT, PATCH, or DELETE.
- OPTIONS evidence alone must not PASS `mediatype-write`.
- Add tests/comments referencing `REQ-ETS-PART2-011` and `SCENARIO-ETS-PART2-011-*` IDs.

## Verification
- Direct source verification used official OGC 23-002 HTML for Clause 16.3 and Annex A.11.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-05-09; checked 2026-05-26 and not stale.
- Live IUT probes were non-mutating: `/conformance`, bounded GETs, and schema/media read checks only.
- Mandatory TeamEngine planning E2E ran from a `/tmp` clone with `SMOKE_CONTAINER_NAME=ets-csapi-s30-swetext-plan-georobotix SMOKE_OUTPUT_DIR=/tmp/sprint-ets-30-swetext-plan-georobotix-results bash scripts/smoke-test.sh`.
- Planning TeamEngine E2E result: FAILED, `186 total / 31 passed / 22 failed / 133 skipped`.
- Planning TeamEngine artifacts: `ops/test-results/sprint-ets-30-plan-georobotix-smoke-failed-2026-05-26.xml` and `ops/test-results/sprint-ets-30-plan-georobotix-smoke-container-failed-2026-05-26.log`.
- Planning E2E interpretation: this is a captured failed public-IUT check, not a passing E2E gate. Failures are inherited from current public GeoRobotix read-path and schema-validation state; no Sprint 30 SWE Common Text runtime tests exist yet.
- Public-IUT mutation check: `scripts/no-mutation-oracle.py` recognized 83 IUT request logs; explicit container-log grep found 83 GeoRobotix GET request lines and zero matched GeoRobotix POST/PUT/PATCH/DELETE request lines.

## Definition of Done
- [x] OpenSpec splits `REQ-ETS-PART2-011` out for Part 2 SWE Common Text and keeps remaining placeholders at `REQ-ETS-PART2-012..013`.
- [x] Story and sprint contract capture official OGC identifiers, prerequisite, media type, and Requirements 115-122.
- [x] Planning captures resource condition gates for `/conf/datastream`, `/conf/controlstream`, and `/conf/create-replace-delete`.
- [x] Planning captures current GeoRobotix declaration and SWE Common Text read-health state with raw probe transcript artifact.
- [x] Planning captures current local OSH unauthenticated readiness limits.
- [x] Planning explicitly blocks false PASS from declaration alone, sibling declarations, `application/swe+csv` format lists, binary/media typo evidence, empty candidate sets, unavailable endpoints, JSON fallback schemas, OPTIONS-only write evidence, and public-IUT mutation.
- [x] Planning TeamEngine E2E evidence is captured and documented honestly.
- [x] Public GeoRobotix mutation check records no POST/PUT/PATCH/DELETE request lines.
- [x] Raze reviews planning before Generator starts.
- [ ] Planning-only change is committed and pushed before Generator implementation.

## Out of Scope
- Public GeoRobotix mutation.
- Positive SWE Common Text write lifecycle behavior.
- SWE Common Binary encoding class.
- Observation-binding cross-class closure beyond the SWE Common Text schema/mapping evidence guards.
- Full Observation/Command payload semantic validation unless a Generator increment supplies a proven SWE Common Text encoding validator and candidate resource evidence.
