# Known Issues — OGC API Connected Systems ETS

Last updated: 2026-08-03T05:08Z

## Scope Corrections (2026-07-23)

- OSH and TeamEngine source/binary modifications are outside project scope.
  Sprint 40's local OSH patch is historical only, absent from the current
  checkout/runtime, and must not be recreated. Remaining IUT limitations must be
  handled in the ETS or demonstrated against an unmodified conforming IUT.
- Project-operated hosted CI will not be approved. GitHub Actions activation is
  permanently retired; local Docker Maven, runtime, and TeamEngine E2E checks are
  authoritative. Jenkinsfiles are inert OGC submission/build metadata.

## Active Issues

- Sprint 74 readiness audit confirms all 47 remaining mutation-bound released
  ATS procedures remain candidate. Direct primary local OSH evidence records
  `GET=1`, `OPTIONS=25`, `unsafeMethodsIssued=[]`; populated disposable OSH
  evidence records `GET=1`, `OPTIONS=27`, `unsafeMethodsIssued=[]`. Only
  Part 1 Create/Replace/Delete currently has direct declaration/method
  readiness, and Sprint 74 records zero
  prerequisite-declaration-ready classes. These fields are declaration-only
  readiness evidence, not proof that inherited TestNG prerequisite groups
  passed. Part 1 Create/Replace/Delete remains blocked by missing Part 1
  `/conf/api-common`, missing exact inherited
  `http://www.opengis.net/spec/ogcapi-4/1.0/conf/create-replace-delete`,
  positive POST/PUT/DELETE lifecycle execution, changed-resource GET proof,
  cleanup/isolation proof for the mutated resource type, and
  cascade/collection/URI-list evidence where applicable. Part 1 Update is
  blocked by missing `/conf/update`, Features Part 4 Update, missing exact
  `ogcapi-4` Update, and missing PATCH advertisement. Part 2
  Create/Replace/Delete is blocked by missing Part 2 `/conf/api-common` and
  missing advertised mutation methods on some readiness probes. Part 2 Update
  is blocked by missing Part 2 `/conf/api-common`, missing Part 2
  `/conf/update`, Features Part 4 Update, missing `/conf/feasibility`, and
  missing PATCH advertisement. Do not promote these candidates without
  completed positive lifecycle E2E against a dedicated mutable IUT.
- Sprint 75 alternate-IUT discovery found one stronger open-source candidate
  beyond OSH for future work: `SomethingCreativeStudios/connected-systems-go`.
  It declares Part 1 `/conf/api-common`, Part 2 `/conf/api-common`, and Part 2
  `/conf/create-replace-delete`, and source inspection found POST/PUT/DELETE
  routes and e2e CRUD tests. It still does not close the active mutation issue:
  no `/conf/update` or real PATCH-route evidence was found, public OPTIONS
  probes did not provide `Allow` readiness, the public `/api` response was too
  minimal for service-description write-operation checks, and public demos must
  remain read-only. Treat it as a candidate for a future self-run disposable
  Part 2 CRD experiment, not as current exact-promotion evidence.
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
- Part 2 placeholder taxonomy was corrected during Sprint 25 planning and
  extended during Sprints 26, 27, 28, 29, 30, 31, 59, 60, 61, 62, 63, 64,
  65, 66, 67, 68, 70, and 71. OpenSpec and epic ETS-03 now treat API Common as exact implemented
  `REQ-ETS-PART2-001`, Datastreams & Observations as exact implemented
  `REQ-ETS-PART2-002`, Control Streams & Commands as exact implemented
  `REQ-ETS-PART2-003`, Command Feasibility as exact implemented
  `REQ-ETS-PART2-004`, System Events as exact implemented
  `REQ-ETS-PART2-005`, Advanced Filtering as exact implemented
  `REQ-ETS-PART2-006`, Create/Replace/Delete as partial implemented with a
  Sprint 70 one-method-per-Annex-A.7-target candidate surface and zero unmapped
  released targets for `REQ-ETS-PART2-007`, Update as partial implemented with
  a Sprint 71 one-method-per-Annex-A.8-target candidate surface and zero
  unmapped released targets for `REQ-ETS-PART2-008`,
  JSON Encoding as exact implemented `REQ-ETS-PART2-009`, SWE Common JSON
  Encoding as exact implemented `REQ-ETS-PART2-010`, SWE Common Text
  Encoding as exact implemented `REQ-ETS-PART2-011`, SWE Common Binary
  Encoding as exact implemented `REQ-ETS-PART2-012`, and remaining
  observation-binding placeholder as `REQ-ETS-PART2-013`. The former
  `/conf/system-history` placeholder is retired because OGC 23-002 Annex A does
  not define it; GeoRobotix's `/conf/system-history` declaration is treated as
  non-standard/vendor extension evidence only, and `systemhistory` is no longer
  treated as an OGC Part 2 collection discovery token.
- GeoRobotix currently fails the Sprint 26 advisory public smoke with HTTP 500 responses on existing read endpoints. On 2026-05-22, direct probes returned HTTP 500 for `GET /systems/0mqcvdnfoca0`, `GET /datastreams?limit=1`, and `GET /observations?limit=2`; post-gapfix TeamEngine smoke failed `146 total / 27 passed / 5 failed / 114 skipped`. New Part 2 Create/Replace/Delete tests dependency-SKIP because `systemfeatures` does not finish successfully. This is not the accepted Sprint 26 E2E gate after the user accepted seeded local OSH as the IUT.
- GeoRobotix declares `/conf/datastream` but not `/conf/api-common`, even though OGC 23-002 Clause 9 lists `/req/api-common` as a prerequisite. Sprint 60 supersedes Sprint 21's scoped execution policy: `part2datastream` now depends directly on `part2apicommon`, so all fourteen Datastream procedures SKIP before Datastream IUT access when Part 2 API Common is absent or skipped. Scoped endpoint PASS evidence must not be read as API Common PASS.
- The unmodified local OSH Sprint 60 primary E2E target also does not declare Part 2 `/conf/api-common`. Mandatory TeamEngine smoke therefore exits non-green with Datastream prerequisite SKIPs (`247/38/21/188`) even though the exact Datastream implementation, condition gates, and coverage gates pass. This is an IUT conformance limitation, not a reason to weaken the direct prerequisite gate. Final evidence: `ops/test-results/sprint-ets-60-part2-datastream-final-raze-2026-07-31/`.
- GeoRobotix `GET /datastreams/{id}/observations?limit=2` historically returned HTTP 200 JSON with an empty `items` array and no top-level `links` for the selected Datastream. Sprint 60 still requires actual nested Observation/reference evidence for `/req/datastream/obs-ref-from-datastream`; empty nested collections cannot PASS that procedure.
- GeoRobotix declares `/conf/controlstream` but not `/conf/api-common`, even though OGC 23-002 Clause 10 lists `/req/api-common` as a prerequisite. Sprint 61 supersedes Sprint 22's scoped execution policy: `part2controlstream` now depends directly on `part2apicommon`, so all eighteen ControlStream procedures SKIP before ControlStream IUT access when Part 2 API Common is absent or skipped. Scoped endpoint PASS evidence must not be read as API Common PASS.
- The unmodified local OSH Sprint 61 primary E2E target also does not declare Part 2 `/conf/api-common`. Mandatory TeamEngine smoke therefore exits non-green with ControlStream prerequisite SKIPs (`254/36/21/197`) even though the exact ControlStream implementation, condition gates, and coverage gates pass. This is an IUT conformance limitation, not a reason to weaken the direct prerequisite gate. Final evidence: `ops/test-results/sprint-ets-61-part2-controlstream-final-2026-07-31/`.
- GeoRobotix serves `GET /controlstreams/{id}` but historically returned HTTP 400 for `GET /controls/{id}`. Sprint 61 no longer passes canonical URL assertions from guessed `/controlstreams/{id}` or `/controls/{id}` aliases; canonical evidence must be an advertised same-origin `rel=canonical` link whose dereferenced resource validates and compares equal after canonical links are removed.
- GeoRobotix `GET /commands?limit=2` historically returned HTTP 400, while `GET /controlstreams/{id}/commands?limit=2` returned HTTP 200 JSON with empty `items`. Sprint 61 requires the released `/commands`, `/controlstreams/{id}/commands`, `/commands/{cmdId}/status`, and `/commands/{cmdId}/result` schema endpoints when evidence exists, and SKIPs child CommandStatus/CommandResult checks without actual Command ids rather than producing vacuous PASS.
- GeoRobotix does not declare `/conf/feasibility`. Sprint 62 supersedes Sprint 23 with five exact released Feasibility procedures and no public-IUT feasibility POSTs. The ETS must preserve Annex A.4 copy-text behavior literally: A.35 uses `itemType=Command`, A.36 validates `/controlstreams/{id}/commands`, and A.39 uses `itemType=Feasibility` with Command schema validation. Local OSH Sprint 62 primary E2E also SKIPs all five Feasibility procedures because the upstream Part 2 prerequisite chain skips before `part2controlstream`; this is an IUT conformance limitation, not a reason to weaken the direct prerequisite gate. Final evidence: `ops/test-results/sprint-ets-62-part2-feasibility-2026-07-31/`.
- GeoRobotix declares `/conf/system-event`, but historical probes showed `GET /systemEvents?limit=2` returning HTTP 400 `Invalid resource name` and `GET /systems/0mqcvdnfoca0/events?limit=2` returning HTTP 400 `Only streaming requests supported on this resource`. Sprint 63 supersedes Sprint 24 with five exact released Annex A.5 procedures and no streaming/SSE or mutation coverage. The ETS must preserve Annex A.5 copy-text behavior literally: A.40 selects `itemType=ControlStream`, A.42 validates canonical `/systemEvents`, and A.43 validates `/systems/{sysId}/systemEvents` even though Clause 12 Requirement 43 names `/systems/{sysId}/events`. Local OSH Sprint 63 primary E2E also SKIPs all five System Events procedures because the Part 1 System prerequisite chain skips before `canonicalSystemsEndpointIsValid`; this is an IUT conformance limitation, not a reason to weaken the direct prerequisite gate. Final evidence: `ops/test-results/sprint-ets-63-part2-system-event-2026-08-01/`.
- Sprint 55 supersedes Sprint 11's partial Part 1 Advanced Filtering subset and
  implements all 25 Part 1 procedures exactly, including association,
  geometry, combined, and indirect recommendation semantics. Part 2 remains
  partial under Sprint 25: FOI recursion, CommandStatus filters, positive
  Command/SystemEvent closure against a declaring IUT, streaming/SSE filtering,
  and broader endpoint parity remain open. Neither local OSH nor GeoRobotix
  currently declares the relevant Advanced Filtering class, so undeclared
  behavior remains SKIP evidence rather than PASS.
- Sprint ets-12 and Sprint ets-26 Create/Replace/Delete work is mutation-safety constrained. GeoRobotix declares both Part 1 and Part 2 `/conf/create-replace-delete`, declares OGC API Features Part 4 `/conf/create-replace-delete`, and advertises POST/PUT/DELETE via broad OPTIONS, but default public smoke MUST NOT mutate the public IUT. OPTIONS evidence is readiness only, not lifecycle conformance. GeoRobotix also returns HTTP 400 for `/commands`, `/systemEvents`, and `/feasibility`, and returns HTTP 400 streaming-only for `/systems/{id}/events` when healthy, so Part 2 CRD lifecycle checks for those resources must SKIP unless a dedicated mutable IUT exposes JSON resource endpoints. Sprint 26 adds the first Part 2 CRD safety-gated runtime subset, but full Create/Replace/Delete remains PARTIAL until non-system CRUD and cascade requirements are implemented.
- Sprint ets-27/ets-71 Update implementation is mutation-safety constrained
  and condition-gated. Sprint 71 exposes one deployed method for each released
  Annex A.8 target and keeps coverage at
  `2:/conf/update = 14 candidate / 0 unmapped`, but positive PATCH lifecycle
  and schema-rejection dispatch remain deferred until a non-public dedicated
  mutable IUT declares `/conf/update`, advertises PATCH, supports changed-field
  GET proof, and cleanup. GeoRobotix historically did not declare Part 2
  `/conf/update`, sampled OPTIONS probes returned broad `Allow` headers but
  omitted PATCH, and read-health probes returned HTTP 500 for existing
  `/systems/{id}`, `/datastreams`, and `/observations` reads. Clause 15 also
  requires resource-class condition gates before PASS: `/conf/datastream` for
  R79-R82, `/conf/controlstream` for R83-R88, `/conf/feasibility` for R89-R91,
  and `/conf/system-event` for R92. Sprint 71 disposable local OSH E2E run
  `sprint-ets-71-update-20260802T153902Z` remained honestly non-green on the
  existing twenty-failure local OSH baseline: populated `275/24/20/231`,
  clean-primary `275/23/20/232`; all twenty-four Part 2 Update methods SKIP
  without PATCH dispatch, TeamEngine request counts are GET-only, cleanup
  PASSes, and primary-state isolation PASSes. Implementation commit `081e09f`
  is pushed.
- Sprint 65 supersedes Sprint 28 and closes JSON Encoding as exact released
  ATS: `2:/conf/json` is `14 exact / 0 candidate / 0 unmapped`, final Raze is
  `APPROVE 0.96`, and implementation commit `1acfdfa` is pushed. The current
  local OSH primary E2E still SKIPs all fourteen Part 2 JSON procedures before
  JSON resource endpoint access because local OSH lacks the SWE JSON
  record-components prerequisite; this is an IUT conformance limitation, not a
  reason to weaken the exact `/conf/json` setup gate or schema/media checks.
  Sprint 65 evidence is archived under
  `ops/test-results/sprint-ets-65-part2-json-2026-08-01/`.
- Sprint 66 supersedes Sprint 29 and closes SWE Common JSON Encoding as exact
  released ATS:
  `2:/conf/swecommon-json` is
  `8 exact / 0 candidate / 0 unmapped`. The current local OSH primary E2E
  still SKIPs all eight Part 2 SWE Common JSON procedures before resource
  endpoint access because local OSH lacks
  `http://www.opengis.net/spec/SWE/3.0/conf/json-encoding-rules`; this is an
  IUT conformance limitation, not a reason to weaken the exact setup gate or
  media/schema checks. Observation/Command encoding methods also preserve a
  no-safe-evidence SKIP without parent schema, candidate body, and proven SWE
  Common JSON data-value validator evidence. Final Raze is `APPROVE 0.96`
  with no required fixes, and implementation commit `6e98ac9` is pushed.
  Sprint 66 evidence is archived
  under
  `ops/test-results/sprint-ets-66-part2-swecommon-json-2026-08-01/`.
- Sprint 67 supersedes Sprint 30 and closes SWE Common Text Encoding as exact
  released ATS:
  `2:/conf/swecommon-text` is
  `8 exact / 0 candidate / 0 unmapped`. The current local OSH primary E2E
  still SKIPs all eight Part 2 SWE Common Text procedures before resource
  endpoint access because local OSH lacks
  `http://www.opengis.net/spec/SWE/3.0/conf/text-encoding-rules`; this is an
  IUT conformance limitation, not a reason to weaken the exact setup gate or
  media/schema checks. Observation/Command encoding methods also preserve a
  no-safe-evidence SKIP without parent schema, candidate body, and proven SWE
  Common Text data-value validator evidence; present noncanonical
  issueTime/IssueTime evidence now fails instead of SKIPping. Local OSH smoke is
  `254/25/20/209`; no-mutation evidence is `GET=137`, zero writes. Focused
  Raze recheck is `APPROVE 0.96`, and implementation commit `5f0a3f6` is
  pushed. Sprint 67 evidence is archived under
  `ops/test-results/sprint-ets-67-part2-swecommon-text-2026-08-01/`.
- Sprint 68 supersedes Sprint 31 and closes SWE Common Binary Encoding as
  exact released ATS:
  `2:/conf/swecommon-binary` is
  `8 exact / 0 candidate / 0 unmapped`. The current local OSH primary E2E
  still SKIPs all eight Part 2 SWE Common Binary procedures before resource
  endpoint access because local OSH lacks
  `http://www.opengis.net/spec/SWE/3.0/conf/binary-encoding-rules`; this is an
  IUT conformance limitation, not a reason to weaken the exact setup gate or
  media/schema checks. Observation/Command encoding methods also preserve a
  no-safe-evidence SKIP without parent schema, candidate body, and proven SWE
  Common Binary data-value validator evidence; missing or noncanonical
  issueTime/IssueTime evidence in a retrieved Command Schema fails instead of
  SKIPping. Local OSH smoke is
  `252/23/20/209`; no-mutation evidence is `GET=130`, zero writes. Focused
  Raze recheck is `APPROVE_WITH_CONCERNS 0.94` with no blocking fixes, and
  implementation commit `bb3935e` is pushed. Sprint 68 evidence is archived under
  `ops/test-results/sprint-ets-68-part2-swecommon-binary-2026-08-01/`.
- Sprint ets-32 changes the development E2E default from GeoRobotix to self-provisioned local OSH. GeoRobotix public runs are advisory interoperability probes only and should not block local-OSH-backed development work. The 2026-06-01 local OSH planning smoke passed `206/65/0/141` with no read-only mutation (`GET=130`, `OPTIONS=2`, `POST/PUT/PATCH/DELETE=0`). The 2026-06-02 Generator local OSH smoke passed `211/68/0/143` with no read-only mutation (`GET=133`, `OPTIONS=2`, `POST/PUT/PATCH/DELETE=0`) both before and after Raze gapfixes. Local OSH currently has empty `/datastreams`, `/observations`, and `/controlstreams` collections and returns HTTP 400 for `/commands` and `/systemEvents`, so positive `REQ-ETS-PART2-013` Observation/Command binding closure still requires documented dynamic-data seed fixtures or precise SKIPs; declarations and empty collections are not PASS evidence. Sprint 33 planning adds `ops/local-osh-dynamic-data-seed-fixtures.json` as a planned/not-applied fixture contract and requires explicit dedicated mutable-IUT opt-in before any seed mutation; Raze planning recheck approved this safety framing at confidence 0.94 after traceability and public-IUT manifest fixes. Sprint 32 initial Raze implementation review found and the Generator fixed a future false-PASS risk for CommandStatus/CommandResult inline data and a SKIP-honesty issue for unavailable or uninspectable schema evidence; focused Raze recheck returned `APPROVE_WITH_CONCERNS` with no required fixes. The remaining concern is regression-depth only: add dedicated inline CommandStatus/CommandResult skip/fail helper tests when extending populated-IUT closure.
- Full positive `REQ-ETS-PART2-013` populated binding closure remains open
  against unmodified OSH. Sprint 44 reproducibly creates System, Procedure,
  Deployment, SamplingFeature, DataStream, Observation, and ControlStream
  fixtures through the supported loopback HTTP API. Sprint 69 revalidates that
  the current populated suite can PASS Observation binding, but Command binding
  still SKIPs because no ControlStream exposes associated Command evidence.
  The optional Sprint 69 Command probe records `POST /controlstreams/040g/commands`
  timing out, nested Commands `itemCount=0`, and no discovered Command id.
  Provisioning is ready, but TeamEngine still reports known non-green totals
  because OSH omits required `live` on DataStream collection items and omits
  `issueTime`, `executionTime`, `live`, and `async` on ControlStream collection
  items in broader JSON/SWE suites. Supplying these read-only fields in create
  payloads does not change OSH serialization. These are genuine
  unmodified-IUT limitations: preserve the FAIL/SKIP evidence or use another
  unmodified conforming IUT; do not patch OSH or weaken the ETS.
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
