# S-ETS-30-01: Part 2 SWE Common Text Encoding

## Status
PARTIAL_IMPLEMENTED by Sprint 30 Generator. Mandatory public GeoRobotix Generator E2E failed; full positive `/conf/swecommon-text` closure remains open.

## User Instruction
Planning triggered by: "Start the spec-first planning".

Generator triggered by: "Do it".

## Scope
Implement the first declaration-gated, read-only Generator increment for OGC 23-002 Clause 16.3 Requirements Class "SWE Common Text Encoding".

- Requirements class: `/req/swecommon-text`
- Conformance class: `/conf/swecommon-text`
- Requirement prerequisite: `http://www.opengis.net/spec/SWE/3.0/req/text-encoding-rules`
- Conformance prerequisite: `http://www.opengis.net/spec/SWE/3.0/conf/text-encoding-rules`
- Media type: `application/swe+text`
- Normative statements in scope: Requirements 115-122, covering mediatype-read, mediatype-write, Observation Schema schema/mapping, Observation text encoding, Command Schema schema/mapping, and Command text encoding.

## Planning Evidence
- Official source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 16.3 "Requirements Class SWE Common Text Encoding" and Annex A.11.
- OGC 23-002 names the requirements class `/req/swecommon-text` and conformance class `/conf/swecommon-text`.
- OGC 23-002 lists SWE Common 3.0 Text Encoding Rules as the prerequisite.
- Resource condition gates:
  - Observation Schema, Observation Schema mapping, and Observation encoding require `/conf/datastream`.
  - Command Schema, Command Schema mapping, and Command encoding require `/conf/controlstream`.
  - `mediatype-write` also requires `/conf/create-replace-delete`, but this increment is non-mutating API-definition evidence only.
- Annex A.115 note: one mediatype-read API-definition bullet mentions `application/swe+binary`, while Clause 16.3 and the remaining A.115 steps use `application/swe+text`. Generator treats this as a source inconsistency and does not use binary advertisement as SWE Common Text PASS evidence.

## Generator Implementation
- Added `src/main/java/org/opengis/cite/ogcapiconnectedsystems10/conformance/part2/swecommontext/Part2SweCommonTextTests.java`.
- Runtime group: `part2swecommontext`.
- Runtime checks cover:
  - Exact Part 2 `/conf/swecommon-text` declaration.
  - SWE Common 3.0 `/conf/text-encoding-rules` prerequisite visibility as a prerequisite-incomplete SKIP, not PASS.
  - `/conf/datastream`, `/conf/controlstream`, and `/conf/create-replace-delete` condition gates.
  - Observation Schema retrieval with `obsFormat=application/swe+text`, bundled `observationSchemaSwe.json` validation, `obsFormat=application/swe+text`, `recordSchema`, and `encoding.type=TextEncoding`.
  - Command Schema retrieval with `cmdFormat=application/swe+text`, bundled `commandSchemaSwe.json` validation, `commandFormat=application/swe+text`, `recordSchema`, and `encoding.type=TextEncoding`.
  - Canonical Time and IssueTime mapping evidence reused from SWE Common JSON mapping guards.
  - Observation/Command `Accept: application/swe+text` resource guards without JSON fallback parsing and without semantic PASS until a SWE Text validator and candidate evidence exist.
  - Non-mutating `mediatype-write` API-definition advertisement evidence scoped to Observation and Command collection/item request bodies only.
- Added `VerifyPart2SweCommonTextTests` with 11 helper regressions for official identifiers, condition gates, exact content type handling, bundled schemas, `TextEncoding`, canonical Time/IssueTime mapping, write-advertisement scoping, negative media/path cases, and group naming.
- Updated `testng.xml` to wire `Part2SweCommonTextTests` with group dependency `part2swecommontext` -> `core common`.
- Updated `VerifyTestNGSuiteDependency` with `part2swecommontext` dependency, method group, and co-location structural lint.

## Verification
- Formatter: Docker Maven `mvn -B spring-javaformat:apply` returned BUILD SUCCESS.
- Focused Maven: Docker Maven `mvn -B test -Dtest=VerifyPart2SweCommonTextTests,VerifyTestNGSuiteDependency` returned BUILD SUCCESS with `81 tests / 0 failures / 0 errors / 0 skipped`.
- Full Maven: Docker Maven `mvn -B clean test` returned BUILD SUCCESS with `258 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-30-maven-2026-05-27.log`.
- Mandatory TeamEngine Generator E2E against GeoRobotix: `SMOKE_CONTAINER_NAME=ets-csapi-s30-swetext-generator-georobotix SMOKE_OUTPUT_DIR=/tmp/sprint-ets-30-swetext-generator-georobotix-results bash scripts/smoke-test.sh`.
- Generator TeamEngine E2E result: FAILED, `196 total / 33 passed / 28 failed / 135 skipped`.
- Generator TeamEngine artifacts:
  - `ops/test-results/sprint-ets-30-generator-georobotix-smoke-failed-2026-05-27.xml`
  - `ops/test-results/sprint-ets-30-generator-georobotix-smoke-container-failed-2026-05-27.log`
  - `ops/test-results/sprint-ets-30-generator-georobotix-smoke-console-failed-2026-05-27.log`
- New SWE Common Text group outcome: 2 PASS, 6 FAIL, and 2 SKIP.
  - PASS: exact `/conf/swecommon-text` declaration and resource condition-gate visibility.
  - SKIP: missing SWE Common 3.0 `/conf/text-encoding-rules` prerequisite and no parseable exact API-definition `application/swe+text` write advertisement.
  - FAIL: Observation-side HTTP 500 responses and Command-side `/controlstreams` schema validation failure before SWE Common Text command evidence.
- Public-IUT mutation check: `scripts/no-mutation-oracle.py` recognized 91 IUT request logs; explicit counts were `GET 91`, `POST 0`, `PUT 0`, `PATCH 0`, `DELETE 0`.
- Raze implementation review: `.harness/evaluations/sprint-ets-30-adversarial-implementation.yaml` initially returned `GAPS_FOUND` confidence 0.88 for missing test-code traceability comments on the Annex media-honesty and unavailable-endpoint-honesty scenarios. The traceability gap was fixed, formatter/focused Maven/full Maven were rerun, and the focused Raze recheck returned `APPROVE_WITH_CONCERNS` confidence 0.93 with no required fixes remaining.

## Definition of Done
- [x] OpenSpec splits `REQ-ETS-PART2-011` out for Part 2 SWE Common Text and keeps remaining placeholders at `REQ-ETS-PART2-012..013`.
- [x] Story and sprint contract capture official OGC identifiers, prerequisite, media type, and Requirements 115-122.
- [x] Runtime tests reference `REQ-ETS-PART2-011` and `SCENARIO-ETS-PART2-011-*` in comments.
- [x] Runtime suite implements exact declaration gating, condition gates, SWE Text media checks, bundled schema metadata validation, mapping guards, encoding guards, and non-mutating write advertisement checks.
- [x] Helper regressions and TestNG structural lint cover the new group.
- [x] Formatter and Maven verification completed successfully.
- [x] Mandatory public GeoRobotix TeamEngine E2E executed and failed honestly as public-IUT evidence.
- [x] Public GeoRobotix mutation check records no POST/PUT/PATCH/DELETE request lines.
- [x] Raze reviews Generator implementation before completion is reported.
- [x] Generator change is committed and pushed (`b2aad06 Implement Sprint 30 Part 2 SWE Common Text`, `560945c..b2aad06 main -> main`).

## Out of Scope
- Public GeoRobotix mutation.
- Positive SWE Common Text write lifecycle behavior.
- SWE Common Binary encoding class.
- Observation-binding cross-class closure beyond the SWE Common Text schema/mapping evidence guards.
- Full Observation/Command payload semantic validation until a proven SWE Common Text encoding validator and candidate resource evidence are available.
