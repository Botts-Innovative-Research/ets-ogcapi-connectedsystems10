# S-ETS-31-01: Part 2 SWE Common Binary Encoding

## Status
PARTIAL_IMPLEMENTED by Sprint 31 Generator. Mandatory public GeoRobotix Generator E2E failed; full positive `/conf/swecommon-binary` closure remains open.

## User Instruction
Planning triggered by: "Do next logical work item".

Generator verification triggered by: "Continue with verify the SWE Binary implementation".

## Scope
Implement the first declaration-gated, read-only Generator increment for OGC 23-002 Clause 16.4 Requirements Class "SWE Common Binary Encoding".

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

## Generator Implementation
- Added `src/main/java/org/opengis/cite/ogcapiconnectedsystems10/conformance/part2/swecommonbinary/Part2SweCommonBinaryTests.java`.
- Runtime group: `part2swecommonbinary`.
- Runtime checks cover:
  - Exact Part 2 `/conf/swecommon-binary` declaration.
  - SWE Common 3.0 `/conf/binary-encoding-rules` prerequisite visibility as a prerequisite-incomplete SKIP, not PASS.
  - `/conf/datastream`, `/conf/controlstream`, and `/conf/create-replace-delete` condition gates.
  - Observation Schema retrieval with `obsFormat=application/swe+binary`, bundled `observationSchemaSwe.json` validation, `obsFormat=application/swe+binary`, `recordSchema`, and `encoding.type=BinaryEncoding`.
  - Command Schema retrieval with `cmdFormat=application/swe+binary`, bundled `commandSchemaSwe.json` validation, `commandFormat=application/swe+binary`, `recordSchema`, and `encoding.type=BinaryEncoding`.
  - Canonical Time and IssueTime mapping evidence reused from SWE Common JSON mapping guards.
  - Observation/Command `Accept: application/swe+binary` resource guards without JSON/Text/CSV fallback parsing and without semantic PASS until a SWE Binary validator and candidate evidence exist.
  - Non-mutating `mediatype-write` API-definition advertisement evidence scoped to Observation and Command collection/item request bodies only.
- Added `VerifyPart2SweCommonBinaryTests` with 11 helper regressions for official identifiers, condition gates, exact content type handling, bundled schemas, `BinaryEncoding`, canonical Time/IssueTime mapping, write-advertisement scoping, negative media/path cases, and group naming.
- Updated `testng.xml` to wire `Part2SweCommonBinaryTests` with group dependency `part2swecommonbinary` -> `core common`.
- Updated `VerifyTestNGSuiteDependency` with `part2swecommonbinary` dependency, method group, and co-location structural lint.

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
- Formatter: Docker Maven `mvn -B spring-javaformat:apply` returned BUILD SUCCESS.
- Focused Maven: Docker Maven `mvn -B test -Dtest=VerifyPart2SweCommonBinaryTests,VerifyTestNGSuiteDependency` returned BUILD SUCCESS with `84 tests / 0 failures / 0 errors / 0 skipped`.
- Full Maven: `bash scripts/mvn-test-via-docker.sh` returned BUILD SUCCESS with `272 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-31-full-maven-2026-05-27.log`.
- Mandatory TeamEngine Generator E2E against GeoRobotix: `SMOKE_CONTAINER_NAME=ets-csapi-s31-swebinary-generator-georobotix SMOKE_OUTPUT_DIR=/tmp/sprint-ets-31-swebinary-generator-georobotix-results bash scripts/smoke-test.sh`.
- Generator TeamEngine E2E result: FAILED, `206 total / 35 passed / 34 failed / 137 skipped`.
- Generator TeamEngine artifacts:
  - `ops/test-results/sprint-ets-31-generator-georobotix-smoke-failed-2026-05-27.xml`
  - `ops/test-results/sprint-ets-31-generator-georobotix-smoke-container-failed-2026-05-27.log`
  - `ops/test-results/sprint-ets-31-generator-georobotix-smoke-console-failed-2026-05-27.log`
- New SWE Common Binary group outcome: 3 PASS, 6 FAIL, and 2 SKIP.
  - PASS: setup, exact `/conf/swecommon-binary` declaration, and resource condition-gate visibility.
  - SKIP: missing SWE Common 3.0 `/conf/binary-encoding-rules` prerequisite and no parseable exact API-definition `application/swe+binary` write advertisement.
  - FAIL: Observation-side HTTP 500 responses and Command-side `/controlstreams` schema validation failure before SWE Common Binary command evidence.
- Public-IUT mutation check: `scripts/no-mutation-oracle.py` recognized 99 IUT request logs; explicit counts were `GET 99`, `POST 0`, `PUT 0`, `PATCH 0`, `DELETE 0`.
- Raze implementation review: `.harness/evaluations/sprint-ets-31-adversarial-implementation.yaml` returned `APPROVE_WITH_CONCERNS` confidence 0.91. The required bookkeeping fix `RAZE-ETS31-IMPL-CONCERN-001` was closed by post-review reconciliation; no code-level required fixes were found.

## Definition of Done
- [x] OpenSpec splits `REQ-ETS-PART2-012` out for Part 2 SWE Common Binary and keeps remaining observation-binding placeholder at `REQ-ETS-PART2-013`.
- [x] Story and sprint contract capture official OGC identifiers, prerequisite, media type, and Requirements 123-130.
- [x] Planning captures resource condition gates for `/conf/datastream`, `/conf/controlstream`, and `/conf/create-replace-delete`.
- [x] Planning captures current GeoRobotix declaration and SWE Common Binary read-health state with raw probe transcript artifact.
- [x] Planning explicitly blocks false PASS from declaration alone, sibling declarations, vendor preliminary binary media, text/CSV/JSON format lists, source text-rule typos, non-empty bytes alone, empty candidate sets, unavailable endpoints, JSON fallback schemas, OPTIONS-only write evidence, and public-IUT mutation.
- [x] Planning TeamEngine E2E evidence is captured and documented honestly.
- [x] Public GeoRobotix mutation check records no POST/PUT/PATCH/DELETE request lines.
- [x] Raze reviews planning before Generator starts.
- [x] Planning-only change is committed and pushed before Generator implementation.
- [x] Runtime tests reference `REQ-ETS-PART2-012` and `SCENARIO-ETS-PART2-012-*` in comments.
- [x] Runtime suite implements exact declaration gating, condition gates, SWE Binary media checks, bundled schema metadata validation, mapping guards, encoding guards, and non-mutating write advertisement checks.
- [x] Helper regressions and TestNG structural lint cover the new group.
- [x] Formatter and Maven verification completed successfully.
- [x] Mandatory public GeoRobotix TeamEngine E2E executed and failed honestly as public-IUT evidence.
- [x] Raze reviews Generator implementation before completion is reported.
- [ ] Generator change is committed and pushed.

## Out of Scope
- Public GeoRobotix mutation.
- Positive SWE Common Binary write lifecycle behavior.
- Observation-binding cross-class closure beyond the SWE Common Binary schema/mapping evidence guards.
- Full Observation/Command payload semantic validation unless a Generator increment supplies a proven SWE Common Binary encoding validator and candidate resource evidence.
