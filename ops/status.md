# Operational Status — OGC API Connected Systems ETS

Last updated: 2026-05-06T14:48Z

## Fresh-Session Entry Point

Start future sessions in:

```bash
cd /home/nh/docker/gir/ets-ogcapi-connectedsystems10
```

Read these first:

- `AGENTS.md`
- `ops/SESSION-HANDOFF-2026-05-05-ETS-REPO-MIGRATION.md`
- `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- `_bmad/traceability.md`
- `.harness/handoffs/planner-handoff.yaml`
- `.harness/contracts/sprint-ets-12.yaml`

## Current State

The active project has moved from the frozen `csapi_compliance` web app repo into this Java/TestNG TeamEngine ETS repo.

Migrated context now lives here:

- `.harness/`
- `openspec/`
- `_bmad/`
- `epics/`
- selected `ops/*.md`
- `AGENTS.md`
- `scripts/orchestrate.py`
- `scripts/session-metrics.py`

Existing ETS evidence in `ops/test-results/` and `ops/server.md` was preserved.

## Current Code State

- ETS HEAD at Sprint 12 Generator start: `7427c3c`
- Latest csapi docs handoff commit before migration: `1568f36`
- Latest implemented story: `S-ETS-12-01` Generator complete as PARTIAL; local OSH mutable-IUT fixture seeding produces full smoke failed=0.
- Current sprint status: Sprint ets-13 planning has Raze review `APPROVE_WITH_CONCERNS` 0.88; both low-severity planning tightenings have been applied and the plan is ready for Generator.
- Latest commit before Sprint 13 planning: `27c182c` (`Validate local OSH full-health fixture`).

## Sprint ets-13 Plan

Update/PATCH safety-gated systems subset:

- Story: `epics/stories/s-ets-13-01-update-safety-gated-systems-subset.md`
- Contract: `.harness/contracts/sprint-ets-13.yaml`
- OpenSpec: `REQ-ETS-PART1-011`, status SPECIFIED for Sprint 13 planning
- Scope planned: declaration-gated `/conf/update`, reuse Sprint 12 mutation opt-in parameters, non-mutating `OPTIONS /systems/{id}` PATCH readiness, default lifecycle SKIP-before-PATCH, public GeoRobotix hard-denial, Update -> CreateReplaceDelete dependency wiring, and PATCH-aware no-mutation smoke oracle
- Explicitly excluded: unguarded PATCH against GeoRobotix, deployment/procedure/sampling-feature/property PATCH, Feature Collection update paths from OGC ATS A.79-A.83, Part 2 `/conf/update`, optimistic locking, and PATCH media-type matrix including JSON Patch, merge patch, and content negotiation
- Corrected story ID: prior epic placeholder reused `S-ETS-07-03`; Sprint 13 planning corrected the Update story to `S-ETS-13-01`.
- Architecture freshness: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-06 and not stale.
- OGC source: OGC API - Connected Systems Part 1 Clause 18, Requirements Class 11 `/req/update`; prerequisite `/req/create-replace-delete`; systems endpoint `{api_root}/systems/{id}` uses HTTP PATCH.
- GeoRobotix planning probe: `/conformance` does not declare `/conf/update`; `OPTIONS /systems/0mqcvdnfoca0` returns HTTP 200 with `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS` and no PATCH.
- Local OSH planning probe: unauthenticated `/conformance` returns HTTP 401; unauthenticated `OPTIONS /systems/040g` returns HTTP 200 with no PATCH in `Allow`.
- Planning interpretation: Sprint 13 should implement skip-first safety and wiring. Current default/public IUT evidence supports no positive PATCH conformance claim.
- Raze planning review: `.harness/evaluations/sprint-ets-13-plan-adversarial.yaml` verdict `APPROVE_WITH_CONCERNS` confidence 0.88. Two planning tightenings were applied: contract media-type exclusions now match story/spec, and OGC ATS A.79-A.83 collection item update paths are cited as deferred.

Sprint 13 guardrails:

- Default TeamEngine smoke MUST NOT issue IUT-bound PATCH.
- PATCH lifecycle assertions must SKIP before PATCH unless `mutation-tests-enabled=true` and `mutation-iut-policy=dedicated-mutable-iut`.
- Even when mutation parameters are present, known shared public GeoRobotix URLs are hard-denied before PATCH.
- OPTIONS checks may PASS only as non-mutating ETS readiness evidence, not OGC update conformance.
- No-mutation smoke proof must treat PATCH as a mutating method alongside POST, PUT, and DELETE.
- Do not promote REQ-ETS-PART1-011 beyond PARTIAL-IMPLEMENTED after this sprint.
- Do not implement Part 2 `/conf/update`.

## Sprint ets-12 Generator Evidence

Create/Replace/Delete safety-gated systems subset:

- Story: `epics/stories/s-ets-12-01-create-replace-delete-safety-gated.md`
- Contract: `.harness/contracts/sprint-ets-12.yaml`
- OpenSpec: `REQ-ETS-PART1-010`, status PARTIAL-IMPLEMENTED for Sprint 12
- Scope implemented: declaration-gated `/conf/create-replace-delete`, explicit mutation opt-in parameters, OPTIONS readiness preconditions, default lifecycle SKIP-before-POST, public GeoRobotix hard-denial, IUT-bound no-mutation log oracle, and `createreplacedelete -> systemfeatures` dependency wiring
- Explicitly excluded: unguarded mutation against GeoRobotix, deployments/subdeployments/procedures/sampling-features/properties CRUD, system delete cascade, custom collection propagation, `text/uri-list`, update/PATCH, and Part 2
- GeoRobotix runtime state: `/conformance` declares `/conf/create-replace-delete`; `OPTIONS /systems` and `OPTIONS /systems/0mqcvdnfoca0` advertise POST/PUT/DELETE; this is readiness evidence only and is not permission to mutate the public smoke target
- Maven: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `105 tests / 0 failures / 0 errors / 3 skipped`
- TeamEngine smoke: `/tmp/sprint-ets-12-generator-smoke-current-r3`, command `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s12-generator-r3 bash scripts/smoke-test.sh`, result `69 total / 52 passed / 0 failed / 17 skipped`
- CreateReplaceDelete runtime outcome against GeoRobotix: 4 PASS and 2 SKIP. The two SKIPs are the expected default safety gate and lifecycle opt-in checks.
- Smoke no-mutation oracle: integrated smoke oracle recognized 40 IUT-bound request log entries and zero IUT-bound POST/PUT/DELETE entries for `https://api.georobotix.io/ogc/t18/api`
- Raze implementation review: `.harness/evaluations/sprint-ets-12-adversarial-implementation.yaml` verdict `GAPS_FOUND` confidence 0.88; GAP-001 and GAP-002 plus low-risk Allow parsing concern were fixed same turn.
- Raze gap-fix review: `.harness/evaluations/sprint-ets-12-adversarial-gapfix.yaml` verdict `APPROVE_WITH_CONCERNS` confidence 0.91; no required fixes remain. Residual concerns: smoke stdout is not archived separately, and positive mutable-IUT lifecycle evidence remains future work.
- Local OSH mutable-IUT follow-up: existing OSH 2.0-beta2 stack in `../sar-ops/field-hub` runs at `http://localhost:8081/sensorhub/api`; TeamEngine reaches it through Docker network `field-hub_default` as `http://field-hub-osh-1:8081/sensorhub/api`.
- Local OSH fixes from probe: service-relative `Location: /systems/{id}` now resolves against the IUT service base; the replacement body preserves the created System `uid` so OSH accepts PUT.
- Local OSH verification: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `110 tests / 0 failures / 0 errors / 3 skipped`; mutable smoke `/tmp/ets-csapi-osh-mutable-smoke-r4` reported `69 total / 32 passed / 3 failed / 34 skipped`, with `systemsCreateReplaceDeleteLifecycle` PASS and real POST, PUT, DELETE observed.
- Local OSH fixture follow-up: `../sar-ops/field-hub/osh/config/config.json` `proxyBaseUrl` is set to `http://field-hub-osh-1:8081`, and the OSH H2 datastore now contains synthetic `/systems/040g`, `/procedures/040g`, `/deployments/040g`, and `/samplingFeatures/040g` seed resources. Exact payloads are versioned in `ops/local-osh-seed-fixtures.json`. The System seed uses `featureType=http://www.w3.org/ns/sosa/System`, which makes `/systems/040g?f=sml3` return local `application/sml+json`.
- Local OSH full-health verification: `/tmp/ets-csapi-osh-full-health-r3` with explicit mutable-IUT parameters reported `69 total / 50 passed / 0 failed / 19 skipped`; corrected smoke stdout printed `SMOKE PASS: total=69 passed=50 failed=0 skipped=19 ...`. The 19 skips are expected for undeclared/unpopulated out-of-scope surfaces, not failed health checks.
- Raze local OSH full-health review: `.harness/evaluations/sprint-ets-12-local-osh-full-health-raze.yaml` verdict `GAPS_FOUND` confidence 0.87. Required fixes applied: smoke stdout now prints exact parsed totals instead of `${total}/${total}`, and the seed payloads are versioned.
- Raze local OSH full-health gap-fix review: `.harness/evaluations/sprint-ets-12-local-osh-full-health-gapfix-raze.yaml` verdict `APPROVE` confidence 0.92; no required fixes remain.

Sprint 12 Generator guardrails:

- Default TeamEngine smoke MUST NOT issue IUT-bound POST, PUT, or DELETE from the Create/Replace/Delete suite.
- Lifecycle mutation assertions must SKIP before POST unless `mutation-tests-enabled=true` and `mutation-iut-policy=dedicated-mutable-iut`.
- Even when mutation parameters are present, known shared public GeoRobotix URLs are hard-denied before POST/PUT/DELETE.
- OPTIONS checks may PASS only as non-mutating ETS readiness evidence, not OGC lifecycle conformance.
- No-mutation smoke proof uses recognized REST Assured `Request: METHOD URI` entries and adjacent `Request method:` + `Request URI:` pairs filtered to the IUT base URL; TeamEngine control-plane POST is excluded.
- Do not promote REQ-ETS-PART1-010 beyond PARTIAL-IMPLEMENTED after this sprint.
- Do not implement `/conf/update` until the CRD safety gate is in place.

Raze planning review:

- Artifact: `.harness/evaluations/sprint-ets-12-plan-adversarial.yaml`
- Verdict: `GAPS_FOUND` confidence 0.87
- Required before Generator: separate OPTIONS readiness from OGC CRD lifecycle conformance; specify full mutation opt-in plumbing plus hard denial for public GeoRobotix; define an IUT-bound log oracle for no-mutation smoke evidence; reconcile stale Sprint 11 traceability/status drift
- Gap-fix review: `.harness/evaluations/sprint-ets-12-plan-gapfix.yaml` verdict `GAPS_FOUND` confidence 0.84. GAP-001, GAP-002, and GAP-004 are closed; GAP-003 was partial because one OpenSpec acceptance-scenario line still required no POST/PUT/DELETE anywhere in the container log instead of the IUT-bound request-log oracle.
- Final wording fix recheck: `.harness/evaluations/sprint-ets-12-plan-gapfix-2.yaml` verdict `APPROVE` confidence 0.93. OpenSpec now consistently uses the IUT-bound adjacent `Request method:` + `Request URI:` oracle, excludes TeamEngine control-plane POST, and the story broad scope sentence uses `IUT-bound`.

## Sprint ets-11 Plan

AdvancedFiltering systems/common-resource read-only subset:

- Story: `epics/stories/s-ets-11-01-advanced-filtering-readonly.md`
- Contract: `.harness/contracts/sprint-ets-11.yaml`
- OpenSpec: `REQ-ETS-PART1-009`, status PARTIAL-IMPLEMENTED after Sprint 11 Generator
- Scope: declaration-gated `/conf/advanced-filtering`, local ID_List helper, `/systems?id=...`, `/systems?q=...`, `/systems?geom=...` smoke shape, and `advancedfiltering -> systemfeatures` dependency wiring
- Explicitly excluded: create-replace-delete, update, Part 2, full association filters, full geometry intersection semantics, combined-filter truth tables, and endpoint parity across every resource type
- GeoRobotix planning state: `/conformance` does not currently declare `/conf/advanced-filtering`; undeclared read-only query behavior is planning evidence only, not conformance PASS evidence

Sprint 11 Generator guardrails:

- Raze planning review gaps were addressed by making ID/keyword filters non-vacuous after seed selection, adding explicit ID_List examples, and separating dependency evidence from default smoke totals.
- Raze gap-fix review: `.harness/evaluations/sprint-ets-11-plan-gapfix.yaml` APPROVE 0.92, static-only per instruction.
- Re-verify `/conformance` before implementing.
- All AdvancedFiltering tests must SKIP-with-reason when `/conf/advanced-filtering` is absent.
- Do not add POST/PUT/PATCH/DELETE requests.
- Do not promote REQ-ETS-PART1-009 beyond PARTIAL-IMPLEMENTED after this sprint.

## Sprint ets-11 Generator Evidence

AdvancedFiltering systems/common-resource read-only subset:

- `AdvancedFilteringTests.java` added with 6 read-only @Tests.
- `testng.xml` wires `<group name="advancedfiltering" depends-on="systemfeatures"/>`.
- `VerifyTestNGSuiteDependency` adds 3 AdvancedFiltering lint tests.
- Current GeoRobotix `/conformance` does not declare `/conf/advanced-filtering`; all 6 AdvancedFiltering @Tests SKIP-with-reason in default smoke.
- No POST/PUT/PATCH/DELETE calls were introduced.

Verification:

- Java formatter via Docker Maven - BUILD SUCCESS
- `bash scripts/mvn-test-via-docker.sh` - BUILD SUCCESS, `98 tests / 0 failures / 0 errors / 3 skipped`
- TeamEngine smoke from `/tmp/sprint-ets-11-generator-smoke` - `63 total / 48 passed / 0 failed / 15 skipped`
- Smoke report: `/tmp/sprint-ets-11-generator-smoke-results/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
- Container log: `/tmp/sprint-ets-11-generator-smoke-results/s-ets-01-03-teamengine-container-2026-05-05.log`
- Raze Gate 4: `.harness/evaluations/sprint-ets-11-adversarial-gate.yaml` APPROVE_WITH_CONCERNS 0.90; independent Maven from the worktree BUILD SUCCESS `98 tests / 0 failures / 0 errors / 3 skipped`; independent TeamEngine smoke from `/tmp/raze-sprint-ets-11` with `SMOKE_CONTAINER_NAME=raze-ets-csapi-smoke-s11` reported `63 total / 48 passed / 0 failed / 15 skipped`. All 6 AdvancedFiltering @Tests SKIP-with-reason because GeoRobotix does not declare `/conf/advanced-filtering`.
- Quinn Gate 3.5: `.harness/evaluations/sprint-ets-11-evaluator-gate.yaml` APPROVE_WITH_CONCERNS 0.90; independent Maven from `/tmp/quinn-sprint-ets-11-gate` BUILD SUCCESS `98 tests / 0 failures / 0 errors / 3 skipped` with log `/tmp/quinn-ets-csapi-mvn-s11.log` after one transient worktree surefire scan/load failure; independent TeamEngine smoke from `/tmp/quinn-sprint-ets-11-gate` with `SMOKE_CONTAINER_NAME=quinn-ets-csapi-smoke-s11` reported `63 total / 48 passed / 0 failed / 15 skipped`. All 6 AdvancedFiltering @Tests SKIP-with-reason because GeoRobotix does not declare `/conf/advanced-filtering`.

## Sprint ets-10 Evidence

SensorML systems read-only subset:

- `SensorMlTests.java` added with 6 read-only @Tests
- `testng.xml` wires `<group name="sensorml" depends-on="systemfeatures"/>`
- VerifyTestNGSuiteDependency adds 3 SensorML lint tests
- Full REQ-ETS-PART1-013 remains open for `mediatype-write`, `relation-types`, deployment/procedure/property SensorML schema/mapping, and full SensorML 3.0 JSON Schema validation

Verification:

- Generator: Java formatter via Docker Maven - BUILD SUCCESS
- Generator: `bash scripts/mvn-test-via-docker.sh` - BUILD SUCCESS, `95 tests / 0 failures / 0 errors / 3 skipped`
- Generator TeamEngine smoke from `/tmp/sprint-ets-10-generator-smoke-git-r2` - `57 total / 48 passed / 0 failed / 9 skipped`
- Quinn independent Maven from `/tmp/quinn-sprint-ets-10` - BUILD SUCCESS, `95 tests / 0 failures / 0 errors / 3 skipped`; surefire includes the three SensorML lint tests
- Quinn independent TeamEngine smoke with unique container `quinn-ets-csapi-smoke-s10` - `57 total / 48 passed / 0 failed / 9 skipped`
- SensorML runtime - 6 PASS; current GeoRobotix direct item `Accept: application/sml+json` falls back to explicit `application/sml+json` alternate link `https://api.georobotix.io/ogc/t18/api/systems/0mqcvdnfoca0?f=sml3`
- Collection-level `GET /systems` `items` JSON is not counted as SensorML PASS
- Raze implementation review initially found two gaps; both were fixed same-turn.
- Raze gap-fix review: `.harness/evaluations/sprint-ets-10-adversarial-gapfix.yaml` APPROVE 0.93, no final blockers.
- Quinn gate: `.harness/evaluations/sprint-ets-10-evaluator-gate.yaml` APPROVE_WITH_CONCERNS 0.91; no blockers.
- Raze Gate 4: `.harness/evaluations/sprint-ets-10-adversarial-gate.yaml` APPROVE 0.91; independent Maven from `/tmp/raze-sprint-ets-10` BUILD SUCCESS `95 tests / 0 failures / 0 errors / 3 skipped`; independent TeamEngine smoke with `SMOKE_CONTAINER_NAME=raze-ets-csapi-smoke-s10` reported `57 total / 48 passed / 0 failed / 9 skipped`.

## Sprint ets-09 Evidence

GeoJSON systems read-only subset:

- `GeoJsonTests.java` added with 5 read-only @Tests
- `testng.xml` wires `<group name="geojson" depends-on="systemfeatures"/>`
- VerifyTestNGSuiteDependency adds 3 GeoJSON lint tests
- Full REQ-ETS-PART1-012 remains open for `mediatype-write`, `relation-types`, and non-system GeoJSON schema/mapping subrequirements

Verification:

- Generator: `bash scripts/mvn-test-via-docker.sh` — BUILD SUCCESS, `92 tests / 0 failures / 0 errors / 3 skipped`
- Generator TeamEngine smoke from `/tmp/sprint-ets-09-smoke-fix` — `51 total / 42 passed / 0 failed / 9 skipped`
- Quinn independent Maven from `/tmp/quinn-sprint-ets-09` — BUILD SUCCESS, `92 tests / 0 failures / 0 errors / 3 skipped`
- Quinn independent TeamEngine smoke with unique container name — `51 total / 42 passed / 0 failed / 9 skipped`
- Raze independent Maven from `/tmp/raze-sprint-ets-09-review` — BUILD SUCCESS, `92 tests / 0 failures / 0 errors / 3 skipped`
- Raze independent TeamEngine smoke — `51 total / 42 passed / 0 failed / 9 skipped`
- GeoJSON runtime — 2 PASS + 3 SKIP; current GeoRobotix `items` JSON is not counted as GeoJSON PASS

Gate Results:

- `.harness/evaluations/sprint-ets-09-adversarial-implementation.yaml` — GAPS_FOUND 0.86 on mediatype-read overclaim
- `.harness/evaluations/sprint-ets-09-adversarial-gapfix.yaml` — APPROVE 0.94 after `b4a97de`
- `.harness/evaluations/sprint-ets-09-evaluator-gate.yaml` — Quinn APPROVE_WITH_CONCERNS 0.90; no blockers
- `.harness/evaluations/sprint-ets-09-adversarial-gate.yaml` — Raze APPROVE_WITH_CONCERNS 0.88; no required fixes

## Next Action

1. Commit Sprint 13 planning when ready.
2. Kick off Generator for `S-ETS-13-01`.
3. During implementation gates, verify exact default-smoke totals and zero IUT-bound PATCH request-log entries.
4. Archive smoke stdout in future gates when relying on no-mutation oracle output.
5. Monitor the transient surefire scan/load failure; open a cleanup story if it recurs.

## Dirty Worktree Notes

Current dirty worktree is expected Sprint ets-13 planning until Raze review and commit:

- `S-ETS-13-01` story and Sprint 13 contract
- OpenSpec `REQ-ETS-PART1-011` and Sprint 13 scenarios
- Traceability and epic row corrected from stale duplicate `S-ETS-07-03` to `S-ETS-13-01`
- Planner handoff updated for Sprint 13
- Ops docs updated for Sprint 13 planning
- Raze planning review artifact `.harness/evaluations/sprint-ets-13-plan-adversarial.yaml`
- External local fixture change still exists in separate `sar-ops` repo and is intentionally not part of ETS commits.
