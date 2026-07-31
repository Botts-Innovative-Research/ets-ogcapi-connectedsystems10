# Known Issues — OGC API Connected Systems ETS

Last updated: 2026-07-31T08:21Z

## Scope Corrections (2026-07-23)

- OSH and TeamEngine source/binary modifications are outside project scope.
  Sprint 40's local OSH patch is historical only, absent from the current
  checkout/runtime, and must not be recreated. Remaining IUT limitations must be
  handled in the ETS or demonstrated against an unmodified conforming IUT.
- Project-operated hosted CI will not be approved. GitHub Actions activation is
  permanently retired; local Docker Maven, runtime, and TeamEngine E2E checks are
  authoritative. Jenkinsfiles are inert OGC submission/build metadata.

## Active Issues

- Codex remote-control thread listing reports host
  `slingshot:env_e_6a62207f03888326a87cd6a61afb2ab0` as
  `thread_list_unavailable`. The reachable task for this folder is idle and
  completed Sprint 58 locally through `9a2e73a`; no recoverable thread id or
  turn content is available for the unavailable host from this session. Do not
  kill the shared local `codex app-server --remote-control` or
  `codex-code-mode-host` as a substitute for task termination; they own the
  active remote-control session. Exposed thread tools currently provide no
  task-specific cancel/terminate operation. The recovery smoke rerun reached
  TeamEngine/local OSH and reproduced the known Sprint 58 `246/41/21/184`
  profile with 194 local-OSH IUT GETs and zero POST/PUT/PATCH/DELETE, so this
  is not evidence of lost Sprint 58 product context or unsafe IUT mutation.
- Sprint 58 completes all fifteen Part 1 SensorML procedures and promotes them
  to reviewed exact mappings. The unmodified local OSH gate is intentionally
  non-green at `246/41/21/184`: `mediatype-write` passes, while fourteen
  SensorML procedures fail because canonical collection requests return
  generic `application/json` or the advertised OpenAPI definition lacks
  complete read-media evidence. This is an OSH interoperability/conformance
  limitation, not an ETS implementation blocker. Controlled read-only HTTP,
  complete schema parity, exact-image runtime/security probes, and final Raze
  `APPROVED 0.99` supply the implementation evidence. Do not patch OSH or
  weaken actual-media/OpenAPI requirements to make this run green.
- Sprint 57 implements all five released Part 1 Update procedures, but exact
  candidate `c4b6030` is superseded by final Raze `GAPS_FOUND 0.98`.
  Requirement-linked replacement regressions close its root Sampling Feature
  fixture and canonical-first/custom-delayed cleanup defects. Exact replacement
  `40cc703` passes complete Update `30/0/0/0`, full Maven `687/0/0/3`, and
  every exact technical/local-OSH honesty gate. Fresh Raze closes both prior
  HIGH findings and finds no implementation defect; focused Raze recheck
  `APPROVE_WITH_CONCERNS 0.98` closes its sole MEDIUM exact-evidence
  reconciliation concern and requires no fixes.
  Unmodified local OSH
  has no usable API Common datetime evidence and omits the exact inherited
  declarations, so all five methods causal-SKIP before writes in both
  populated `244/54/35/155` and clean-primary `244/40/7/197` runs. This is
  valid dependency/no-write evidence, not positive PATCH conformance. Keep the
  five mappings candidate until another conforming dedicated mutable IUT or a
  future unmodified upstream OSH release supplies completed PATCH lifecycle
  evidence. Do not modify OSH or TeamEngine and do not weaken the gates.
- Sprint 56 Part 1 Create/Replace/Delete positive mutation E2E remains open.
  Candidate `0023d5b` is separately superseded by Raze `GAPS_FOUND 0.99`;
  exact remediation candidate `1a6c5ec` also passes its technical gates but is
  superseded by Raze `GAPS_FOUND 0.98`. Deadline requester boundaries, queued
  custom setup occurrences, and queued URI-list Location classification are
  remediated by exact candidate
  `8aa92d4da33aeb3b1c545378c0a68cb84a565ccb`, which passes direct HTTP
  `31/0/0/0`, focused aggregate `48/0/0/0`, full Maven `650/0/0/3`, and all
  exact technical, local-OSH, sabotage, credential, immutability, and hygiene
  gates, but Raze `GAPS_FOUND 0.97` supersedes it. Identity-safe occurrence
  cleanup and joint compound polling pass exact candidate `700c697`, but Raze
  `GAPS_FOUND 0.98` supersedes it for cross-origin queued status
  classification and missing raw-red evidence. The requirement-linked
  regression records `1/1/0/0`; corrected direct HTTP passes `36/0/0/0`,
  focused aggregate `53/0/0/0`, and full clean-cache Docker Maven
  `655/0/0/3` without requesting the cross-origin URI. Exact candidate
  `a2ce5478e25542a766025a2a5fde246fc2d5f8d6` repeats those gates and all
  source, parity, image/runtime, immutable-base, local-OSH, sabotage,
  credential, and hygiene gates; raw-red and recursive manifest evidence are
  sealed. Fresh Raze `APPROVE_WITH_CONCERNS 0.99` closes every prior finding
  and has no candidate-scoped required fix. Its sole concern is this external
  positive-mutation blocker.
  The unmodified local OSH API Common result is `4 PASS / 1 SKIP` because no
  advertised collection supplies positive datetime evidence. Causal TestNG
  inheritance therefore dependency-SKIPs all twelve CRD methods before their
  own declaration checks or writes, despite OSH advertising Part 1
  `/conf/create-replace-delete`. OSH also omits the exact Connected Systems API
  Common and released inherited
  `http://www.opengis.net/spec/ogcapi-4/1.0/conf/create-replace-delete`
  declarations; its similarly named Features Part 4 URI is insufficient. OSH
  and TeamEngine modifications are out of scope. A 2026-07-30 feasibility
  audit confirms that an existing real or simulated driver cannot fill these
  gaps: the current and upstream ConSys service hard-code the conformance
  declaration set, instantiate fixed collection metadata without temporal
  extents, and bind temporal filtering only to `validTime`; `datetime` is
  unparsed and ignored. A driver can supply valid-time feature data, but it
  cannot configure these API-service behaviors. Closure therefore requires an
  independent dedicated mutable IUT or a future unmodified upstream OSH
  release that implements the required surfaces.
  Controlled HTTP cannot replace mandatory real-protocol E2E, and all twelve
  mappings remain candidate.
- Local OSH does not declare Part 1 `/conf/advanced-filtering`. Sprint 55
  therefore records all 25 exact procedures as honest declaration-boundary
  SKIPs in primary E2E; controlled read-only HTTP supplies positive procedure
  evidence. Do not patch OSH or weaken the declaration gate.
- Local OSH declares Part 1 `/conf/property`, but `/properties` returns
  `application/json` with empty `items` even when SensorML is requested, and
  `/collections` advertises no `itemType=sosa:Property` collection. Sprint 53
  preserves two endpoint evidence SKIPs, one canonical evidence SKIP, and one
  genuine collections FAIL in the current `219/40/7/172` primary run. Do not patch OSH
  or weaken the ETS; controlled read-only HTTP supplies positive SensorML
  Property procedure evidence. This remains an IUT interoperability
  limitation, not an ETS implementation gap.
- Local OSH declares Part 1 `/conf/sf`, but `/samplingFeatures` returns
  `application/json` even when GeoJSON is requested, and `/collections`
  advertises `featureType=featureOfInterest` rather than exact `sosa:Sample`.
  Sprint 52 therefore records honest endpoint/canonical evidence SKIPs and a
  genuine collections FAIL. The nested System-reference procedure passes. Do
  not patch OSH or weaken the ETS; positive conformance requires another
  unmodified IUT exposing the released metadata and GeoJSON representation.
  Controlled HTTP covers the nested GeoJSON schema branch that this generic
  JSON local-OSH response cannot exercise.
- GeoJSON is `12/12 exact` after Sprint 54. The released write-media procedure
  is non-mutating OpenAPI inspection, not the historical safety-gated
  lifecycle. Local OSH still returns generic `application/json` for canonical
  resources and exposes unusable API-definition evidence, so all twelve
  procedures honestly SKIP there. Controlled HTTP supplies positive procedure
  coverage; these fixtures and local-IUT SKIPs are not external conformance
  certification.
- Historical Sprint 19/26 SensorML subset and seed evidence no longer defines
  implementation status. Sprint 58 supplies complete released-procedure,
  schema, controlled-HTTP, and exact-image closure. Older GeoRobotix and OSH
  limitations remain useful interoperability context only.
- Part 2 placeholder taxonomy was corrected during Sprint 25 planning and extended during Sprints 26, 27, 28, 29, 30, 31, 59, and 60. OpenSpec and epic ETS-03 now treat API Common as exact implemented `REQ-ETS-PART2-001`, Datastreams & Observations as exact implemented `REQ-ETS-PART2-002`, Control Streams & Commands as `REQ-ETS-PART2-003`, Command Feasibility as partial implemented `REQ-ETS-PART2-004`, System Events as partial implemented `REQ-ETS-PART2-005`, Advanced Filtering as partial implemented `REQ-ETS-PART2-006`, Create/Replace/Delete as partial implemented `REQ-ETS-PART2-007`, Update as partial implemented `REQ-ETS-PART2-008`, JSON Encoding as partial implemented `REQ-ETS-PART2-009`, SWE Common JSON Encoding as partial implemented `REQ-ETS-PART2-010`, SWE Common Text Encoding as partial implemented `REQ-ETS-PART2-011`, SWE Common Binary Encoding as partial implemented `REQ-ETS-PART2-012`, and remaining observation-binding placeholder as `REQ-ETS-PART2-013`. The former `/conf/system-history` placeholder is retired because OGC 23-002 Annex A does not define it; GeoRobotix's `/conf/system-history` declaration is treated as non-standard/vendor extension evidence only, and `systemhistory` is no longer treated as an OGC Part 2 collection discovery token.
- GeoRobotix currently fails the Sprint 26 advisory public smoke with HTTP 500 responses on existing read endpoints. On 2026-05-22, direct probes returned HTTP 500 for `GET /systems/0mqcvdnfoca0`, `GET /datastreams?limit=1`, and `GET /observations?limit=2`; post-gapfix TeamEngine smoke failed `146 total / 27 passed / 5 failed / 114 skipped`. New Part 2 Create/Replace/Delete tests dependency-SKIP because `systemfeatures` does not finish successfully. This is not the accepted Sprint 26 E2E gate after the user accepted seeded local OSH as the IUT.
- GeoRobotix declares `/conf/datastream` but not `/conf/api-common`, even though OGC 23-002 Clause 9 lists `/req/api-common` as a prerequisite. Sprint 60 supersedes Sprint 21's scoped execution policy: `part2datastream` now depends directly on `part2apicommon`, so all fourteen Datastream procedures SKIP before Datastream IUT access when Part 2 API Common is absent or skipped. Scoped endpoint PASS evidence must not be read as API Common PASS.
- The unmodified local OSH Sprint 60 primary E2E target also does not declare Part 2 `/conf/api-common`. Mandatory TeamEngine smoke therefore exits non-green with Datastream prerequisite SKIPs (`247/38/21/188`) even though the exact Datastream implementation, condition gates, and coverage gates pass. This is an IUT conformance limitation, not a reason to weaken the direct prerequisite gate. Final evidence: `ops/test-results/sprint-ets-60-part2-datastream-final-raze-2026-07-31/`.
- GeoRobotix `GET /datastreams/{id}/observations?limit=2` historically returned HTTP 200 JSON with an empty `items` array and no top-level `links` for the selected Datastream. Sprint 60 still requires actual nested Observation/reference evidence for `/req/datastream/obs-ref-from-datastream`; empty nested collections cannot PASS that procedure.
- GeoRobotix declares `/conf/controlstream` but not `/conf/api-common`, even though OGC 23-002 Clause 10 lists `/req/api-common` as a prerequisite. Sprint 22 implements a runtime SKIP for full `/conf/controlstream` closure when `/conf/api-common` is absent; scoped endpoint PASS evidence must not be read as API Common PASS.
- GeoRobotix serves `GET /controlstreams/{id}` but returns HTTP 400 for `GET /controls/{id}`. OGC 23-002 `/req/controlstream/canonical-url` cites canonical ControlStream URL form `{api_root}/controls/{id}`, so Sprint 22 SKIPs canonical URL rather than passing from `/controlstreams/{id}` alias evidence alone.
- GeoRobotix `GET /commands?limit=2` currently returns HTTP 400, while `GET /controlstreams/{id}/commands?limit=2` returns HTTP 200 JSON with empty `items`. Sprint 22 SKIPs the global Command endpoint and `/req/controlstream/cmd-ref-from-controlstream` without actual nested Command/reference evidence.
- GeoRobotix does not declare `/conf/feasibility`. Sprint 23 Generator SKIPs all 7 Feasibility runtime tests by declaration by default, issues no public-IUT feasibility POSTs, and must not PASS `/req/feasibility/ref-from-controlstream` from plural `/controlstreams/{id}/feasibility` alias evidence alone. Full positive Feasibility coverage remains open until a declaring safe/mutable IUT exists.
- GeoRobotix declares `/conf/system-event`, but `GET /systemEvents?limit=2` returns HTTP 400 `Invalid resource name` and `GET /systems/0mqcvdnfoca0/events?limit=2` returns HTTP 400 `Only streaming requests supported on this resource`. Sprint 24 Generator PASSes only the exact declaration check and SKIPs the JSON endpoint/resource/collection checks honestly; it uses Requirement 43's `/systems/{sysId}/events` path rather than Annex A.43's conflicting `/systems/{sysId}/systemEvents` string, and defers streaming/SSE coverage to a later increment.
- Sprint 55 supersedes Sprint 11's partial Part 1 Advanced Filtering subset and
  implements all 25 Part 1 procedures exactly, including association,
  geometry, combined, and indirect recommendation semantics. Part 2 remains
  partial under Sprint 25: FOI recursion, CommandStatus filters, positive
  Command/SystemEvent closure against a declaring IUT, streaming/SSE filtering,
  and broader endpoint parity remain open. Neither local OSH nor GeoRobotix
  currently declares the relevant Advanced Filtering class, so undeclared
  behavior remains SKIP evidence rather than PASS.
- Sprint ets-12 and Sprint ets-26 Create/Replace/Delete work is mutation-safety constrained. GeoRobotix declares both Part 1 and Part 2 `/conf/create-replace-delete`, declares OGC API Features Part 4 `/conf/create-replace-delete`, and advertises POST/PUT/DELETE via broad OPTIONS, but default public smoke MUST NOT mutate the public IUT. OPTIONS evidence is readiness only, not lifecycle conformance. GeoRobotix also returns HTTP 400 for `/commands`, `/systemEvents`, and `/feasibility`, and returns HTTP 400 streaming-only for `/systems/{id}/events` when healthy, so Part 2 CRD lifecycle checks for those resources must SKIP unless a dedicated mutable IUT exposes JSON resource endpoints. Sprint 26 adds the first Part 2 CRD safety-gated runtime subset, but full Create/Replace/Delete remains PARTIAL until non-system CRUD and cascade requirements are implemented.
- Sprint ets-27 Update implementation is mutation-safety constrained and condition-gated. GeoRobotix currently does not declare Part 2 `/conf/update`, sampled OPTIONS probes return broad `Allow` headers but omit PATCH, and current read-health probes still return HTTP 500 for existing `/systems/{id}`, `/datastreams`, and `/observations` reads. Clause 15 also requires resource-class condition gates before PASS: `/conf/datastream` for R79-R82, `/conf/controlstream` for R83-R88, `/conf/feasibility` for R89-R91, and `/conf/system-event` for R92. `Part2UpdateTests` adds the first safety-gated runtime subset, but positive PATCH lifecycle and schema-rejection dispatch remain deferred until a non-public dedicated mutable IUT declares `/conf/update`, advertises PATCH, supports changed-field GET proof, and cleanup. Mandatory GeoRobotix Generator smoke failed `160 total / 27 passed / 5 failed / 128 skipped`; all 14 Part 2 Update runtime tests SKIP through `systemfeatures`, and no GeoRobotix PATCH/POST/PUT/DELETE was logged. Accepted local OSH E2E passed `160 total / 62 passed / 0 failed / 98 skipped`; all 14 Part 2 Update runtime tests SKIP because local OSH does not declare `/conf/update`, and no PATCH request lines appear in the local OSH smoke log.
- Sprint ets-28 JSON Encoding implementation is PARTIAL and must not be read as full `/conf/json` closure. GeoRobotix currently declares Part 2 `/conf/json`, `/conf/datastream`, `/conf/controlstream`, `/conf/system-event`, `/conf/create-replace-delete`, and SWE Common encoding classes, but does not expose SWE 3.0 `/conf/json-record-components`. Mandatory Generator smoke failed `176 total / 29 passed / 16 failed / 131 skipped`: existing public-IUT reads still return HTTP 500 for `/datastreams` and `/observations`, and `/controlstreams` now reaches JSON schema validation but fails `controlStreamCollection.json`. The classpath schema loader failure is fixed. Observation/Command/CommandResult dynamic constraints now require parent-schema and child-resource evidence but SKIP instead of shape-only PASS until semantic validation is implemented; mediatype-write is non-mutating API-definition evidence and requires an exact `application/json` request-body content key. Full positive closure remains open for valid candidate DataStream, Observation, ControlStream, Command, CommandStatus, CommandResult, SystemEvent, SWE record-component, and mediatype-write evidence. The archived public log has 75 matched GeoRobotix GET lines and zero matched POST/PUT/PATCH/DELETE lines, while `scripts/no-mutation-oracle.py` is inconclusive for this log format.
- Sprint ets-29 SWE Common JSON Encoding implementation is PARTIAL and must not be read as full `/conf/swecommon-json` closure. GeoRobotix declares `/conf/swecommon-json`, `/conf/datastream`, `/conf/controlstream`, and `/conf/create-replace-delete`, but does not expose SWE 3.0 `/conf/json-encoding-rules`. Direct SWE JSON probes show DataStream and Observation reads return HTTP 500, selected ControlStream schema with `cmdFormat=application/swe+json` returns JSON-format schema evidence rather than SWE Common `recordSchema` plus `JSONEncoding`, and nested Commands are empty. Mandatory post-gapfix Generator smoke failed `186 total / 31 passed / 22 failed / 133 skipped`; the new SWE Common JSON group produced 2 PASS, 6 FAIL, and 2 SKIP. Full positive closure remains open for valid SWE Common JSON Observation Schema, Observation, Command Schema, Command, SWE 3.0 prerequisite, encoding-validator, and mediatype-write evidence. Mapping checks now require canonical Time definition URIs, and mediatype-write API-definition checks are scoped to Observation/Command resource endpoints. `scripts/no-mutation-oracle.py` recognized 83 IUT request logs, and explicit public log grep found 83 GeoRobotix GET lines and zero matched POST/PUT/PATCH/DELETE lines.
- Sprint ets-30 SWE Common Text Encoding implementation is PARTIAL and must not be read as full `/conf/swecommon-text` closure. `Part2SweCommonTextTests` adds exact declaration, SWE 3.0 `/conf/text-encoding-rules` prerequisite visibility, condition gates, exact `application/swe+text`, bundled schema metadata validation requiring `TextEncoding`, canonical Time/IssueTime mapping evidence, Observation/Command encoding guards, and non-mutating mediatype-write API-definition checks. GeoRobotix declares `/conf/swecommon-text`, `/conf/datastream`, `/conf/controlstream`, and `/conf/create-replace-delete`, but does not expose SWE 3.0 `/conf/text-encoding-rules`. Mandatory Generator smoke failed `196 total / 33 passed / 28 failed / 135 skipped`; the new SWE Common Text group produced 2 PASS, 6 FAIL, and 2 SKIP. Full positive closure remains open for valid SWE Common Text Observation Schema, Observation, Command Schema, Command, SWE 3.0 prerequisite, encoding-validator, and mediatype-write evidence. `scripts/no-mutation-oracle.py` recognized 91 IUT request logs, and explicit public log counts found 91 GeoRobotix GET lines and zero matched POST/PUT/PATCH/DELETE lines.
- Sprint ets-31 SWE Common Binary Encoding is PARTIAL and must not be read as full `/conf/swecommon-binary` closure. `Part2SweCommonBinaryTests` adds exact declaration, SWE 3.0 `/conf/binary-encoding-rules` prerequisite visibility, condition gates, exact `application/swe+binary`, bundled schema metadata validation requiring `BinaryEncoding`, canonical Time/IssueTime mapping evidence, Observation/Command encoding guards, and non-mutating mediatype-write API-definition checks. GeoRobotix declares `/conf/swecommon-binary`, `/conf/datastream`, `/conf/controlstream`, and `/conf/create-replace-delete`, but does not expose SWE 3.0 `/conf/binary-encoding-rules`. Mandatory Generator smoke failed `206 total / 35 passed / 34 failed / 137 skipped`; the new SWE Common Binary group produced 3 PASS, 6 FAIL, and 2 SKIP. Full positive closure remains open for valid SWE Common Binary Observation Schema, Observation, Command Schema, Command, SWE 3.0 prerequisite, encoding-validator, and mediatype-write evidence. `scripts/no-mutation-oracle.py` recognized 99 IUT request logs, and explicit public log counts found 99 GeoRobotix GET lines and zero POST/PUT/PATCH/DELETE lines.
- Sprint ets-32 changes the development E2E default from GeoRobotix to self-provisioned local OSH. GeoRobotix public runs are advisory interoperability probes only and should not block local-OSH-backed development work. The 2026-06-01 local OSH planning smoke passed `206/65/0/141` with no read-only mutation (`GET=130`, `OPTIONS=2`, `POST/PUT/PATCH/DELETE=0`). The 2026-06-02 Generator local OSH smoke passed `211/68/0/143` with no read-only mutation (`GET=133`, `OPTIONS=2`, `POST/PUT/PATCH/DELETE=0`) both before and after Raze gapfixes. Local OSH currently has empty `/datastreams`, `/observations`, and `/controlstreams` collections and returns HTTP 400 for `/commands` and `/systemEvents`, so positive `REQ-ETS-PART2-013` Observation/Command binding closure still requires documented dynamic-data seed fixtures or precise SKIPs; declarations and empty collections are not PASS evidence. Sprint 33 planning adds `ops/local-osh-dynamic-data-seed-fixtures.json` as a planned/not-applied fixture contract and requires explicit dedicated mutable-IUT opt-in before any seed mutation; Raze planning recheck approved this safety framing at confidence 0.94 after traceability and public-IUT manifest fixes. Sprint 32 initial Raze implementation review found and the Generator fixed a future false-PASS risk for CommandStatus/CommandResult inline data and a SKIP-honesty issue for unavailable or uninspectable schema evidence; focused Raze recheck returned `APPROVE_WITH_CONCERNS` with no required fixes. The remaining concern is regression-depth only: add dedicated inline CommandStatus/CommandResult skip/fail helper tests when extending populated-IUT closure.
- Full positive `REQ-ETS-PART2-013` populated binding closure remains open
  against unmodified OSH. Sprint 44 now reproducibly creates System, Procedure,
  Deployment, SamplingFeature, DataStream, Observation, and ControlStream
  fixtures through the supported loopback HTTP API. Provisioning is ready, but
  TeamEngine still reports 28 failures because OSH omits required `live` on
  DataStream collection items and omits `issueTime`, `executionTime`, `live`,
  and `async` on ControlStream collection items. Supplying these read-only
  fields in create payloads does not change OSH serialization. Command child
  evidence also remains unavailable without an in-scope tasking fixture. These
  are genuine unmodified-IUT limitations: preserve the FAIL/SKIP evidence or
  use another unmodified conforming IUT; do not patch OSH or weaken the ETS.
- Local OSH is the accepted Sprint 26 E2E gate and seeded mutable health target after the seedfix restored it as a clean full-suite run: `146 total / 62 passed / 0 failed / 84 skipped` on 2026-05-22 after adding Procedure/Deployment `featureType` metadata to the seeded records. It is still not evidence for out-of-scope CRD subrequirements such as non-system CRUD, cascade behavior, `text/uri-list`, or `/conf/update`. Existing Part 1 system CRD checks issued system POST/PUT/DELETE under explicit opt-in during that run; the new Part 2 lifecycle checks did not issue datastream, observation, controlstream, command, feasibility, or system-event lifecycle mutation.
- GeoRobotix has historically declared `/conf/geojson`, but its sampled
  canonical responses use generic `application/json`. Sprint 54's exact
  procedures SKIP unsupported actual media per resource boundary; GeoRobotix
  remains advisory and is not the primary development gate.
- Sprint 9 non-blocking gate concerns remain as cleanup candidates: smoke log archival can lose the container log when Docker cleanup races `docker logs`, and future default-JSON GeoJSON FeatureCollection fallback PASS branches need clearer runtime reporting.

## Worktree Hygiene

Worktree was clean at Sprint 10 planning start. The earlier untracked `*:Zone.Identifier` files were removed, and the six script executable bits were restored.

## Verification Caveats

- Host Maven is not assumed to exist in WSL2. Use `scripts/mvn-test-via-docker.sh`.
- Gate smoke runs should use `/tmp` clones and `SMOKE_OUTPUT_DIR=/tmp/...` to avoid worktree pollution.
- Do not report skipped tests as pass. Always report totals including skipped counts.

## Historical External-Patch Evidence

- Sprints 33-38 established fixture and ETS-side evidence. Sprint 40 then used a
  local OSH ConSys patch to clear some media-type and child-body blockers, but
  populated smoke still failed `211/86/17/108`. CP-003/ADR-012 retires that
  external patch path. The commit is absent from the current checkout/runtime,
  and this chronology is not active implementation guidance.
