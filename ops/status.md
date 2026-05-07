# Operational Status — OGC API Connected Systems ETS

Last updated: 2026-05-07T18:05Z

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
- `.harness/contracts/sprint-ets-20.yaml`

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

- ETS HEAD includes Sprint 19 planning commit `d4554aa Plan Sprint 19 mediatype write checks`; Sprint 19 Generator committed as `Implement Sprint 19 mediatype write checks`.
- Latest csapi docs handoff commit before migration: `1568f36`
- Latest implemented story: `S-ETS-19-01` Generator complete as PARTIAL; local OSH follow-up produced positive system-resource mediatype-write evidence and Raze follow-up gapfix approved reconciliation.
- Current sprint status: Sprint ets-20 Part 2 API Common planning in progress.
- Latest pushed commits: `4bdc930 Implement Sprint 19 mediatype write checks` and `ff9efa2 Record SSH push status`.
- Push status: remote switched to SSH and `git push origin main` succeeded on 2026-05-07T17:54Z and 2026-05-07T17:55Z.

## Sprint ets-20 Planning Evidence

Part 2 API Common planning:

- Story: `epics/stories/s-ets-20-01-part2-api-common-planning.md`
- Contract: `.harness/contracts/sprint-ets-20.yaml`
- OpenSpec: activates `REQ-ETS-PART2-001`; remaining Part 2 classes remain deferred placeholders.
- Scope planned: OGC 23-002 Requirements Class "Common" using official `/req/api-common`, `/conf/api-common`, `/req/api-common/resources`, and `/req/api-common/resource-collection` identifiers.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-07 and not stale.
- OGC source verification: official OGC 23-002 HTML `https://docs.ogc.org/is/23-002/23-002.html`, Clause 8 "Requirements Class Common"; prerequisite is Part 1 API Common.
- Correction: frozen web-app Part 2 `dynamic-common` / `dynamic-json` names are historical and must not be used in Java ETS `@Test` descriptions.
- GeoRobotix planning probe: `/conformance` declares Part 2 `/conf/datastream`, `/conf/controlstream`, `/conf/json`, `/conf/create-replace-delete`, `/conf/system-event`, `/conf/system-history`, and SWE Common encoding classes, but not `/conf/api-common`.
- GeoRobotix read-only probes: landing page exposes `datastreams` and `observations`; `GET /datastreams?limit=1`, `/observations?limit=1`, and `/controlstreams?limit=1` return HTTP 200 JSON with `items` and `links`; `GET /commands?limit=1` returns HTTP 400.
- Planned verdict policy: absence of `/conf/api-common` is SKIP-with-reason for Part 2 API Common declaration/resource-judgment tests; sibling Part 2 class declarations do not imply API Common PASS.
- Raze planning review `.harness/evaluations/sprint-ets-20-plan-adversarial.yaml` returned `APPROVE_WITH_CONCERNS` confidence 0.92 with no required fixes. Non-blocking concern: broader Part 2 placeholder taxonomy still says 14 classes and duplicates API Common in the remaining placeholder block.
- Out of scope: Part 2 JSON, Datastream/Observation closure, ControlStream/Command closure, SWE Common encodings, Part 2 CRD/Update mutation, and full schema validation.
- Next action: commit and push Sprint 20 planning, then start Generator for `S-ETS-20-01`.

## Sprint ets-19 Generator Evidence

Encoding mediatype-write safety-gated checks:

- Story: `epics/stories/s-ets-19-01-encoding-mediatype-write-safety-gated.md`
- Contract: `.harness/contracts/sprint-ets-19.yaml`
- OpenSpec: extends `REQ-ETS-PART1-012`, `REQ-ETS-PART1-013`, and mutation-safety dependency `REQ-ETS-PART1-010`; GeoJSON/SensorML remain PARTIAL.
- Scope implemented: `Content-Type: application/geo+json` and `Content-Type: application/sml+json` parsing checks behind existing mutation opt-in and public-IUT hard-denial gates.
- Out of scope: default mutation against GeoRobotix, Part 2, full external schema validation, full GeoJSON/SensorML closure, and non-system mutation-side encoding coverage.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-07 and not stale.
- OGC source verification: official upstream GeoJSON and SensorML encoding clauses list `mediatype-write`; both condition write-side media type parsing on Create/Replace/Delete support.
- GeoRobotix planning probe: `/conformance` declares `/conf/create-replace-delete`, `/conf/geojson`, and `/conf/sensorml`; `OPTIONS /systems` and `OPTIONS /systems/0mqcvdnfoca0` advertise POST/PUT/DELETE.
- Implementation: `EncodingMediatypeWrite` helper plus GeoJSON and SensorML runtime tests; helper unit coverage prevents public-IUT mutation, status-only PASS, wrong-identity PASS, non-exact media type drift, and OSH-compatible GeoJSON body drift.
- Verification: formatter BUILD SUCCESS; Maven r3 BUILD SUCCESS with `144 tests / 0 failures / 0 errors / 3 skipped`; GeoRobotix TeamEngine smoke r3 `89 total / 55 passed / 0 failed / 34 skipped`.
- No-mutation proof: GeoRobotix smoke recognized 69 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH entries; both mediatype-write lifecycle tests SKIP before mutation by default.
- Local mutable-IUT proof: authenticated local OSH smoke r3 reported `89 total / 52 passed / 4 failed / 33 skipped`; both Sprint 19 mediatype-write tests PASSed with exact `Content-Type=application/geo+json` and `Content-Type=application/sml+json`, follow-up GET, and cleanup DELETE request-log evidence. The four local failures are SensorML deployment/procedure HTTP 500 responses outside Sprint 19.
- Raze planning review `.harness/evaluations/sprint-ets-19-plan-adversarial.yaml` returned `GAPS_FOUND` confidence 0.88 for a missing SensorML OpenSpec scenario body.
- Raze gap-fix review `.harness/evaluations/sprint-ets-19-plan-gapfix.yaml` returned `APPROVE` confidence 0.95 after adding `SCENARIO-ETS-PART1-013-SENSORML-MEDIATYPE-WRITE-SAFETY-GATED-001`.
- Raze implementation follow-up gapfix `.harness/evaluations/sprint-ets-19-adversarial-followup-gapfix.yaml` returned `APPROVE` confidence 0.94 after r3 reconciliation updates.
- Next action: start the next sprint item from pushed commit `4bdc930`.

## Sprint ets-18 Generator Evidence

Encoding relation-types breadth read-only checks:

- Story: `epics/stories/s-ets-18-01-encoding-relation-types-breadth-readonly.md`
- Contract: `.harness/contracts/sprint-ets-18.yaml`
- OpenSpec: extends `REQ-ETS-PART1-012` and `REQ-ETS-PART1-013`; both remain PARTIAL.
- Scope implemented: independent relation-types assertions for selected GeoJSON System/Deployment/Procedure/SamplingFeature and SensorML System/Deployment/Procedure resources.
- Out of scope: GeoJSON/SensorML `mediatype-write`, mutation behavior, full schema validation, Part 2, property GeoJSON mapping, and property-level `@link` relation-types PASS evidence.
- Implementation: `GeoJsonTests` adds Deployment, Procedure, and Sampling Feature relation-types checks; `SensorMlTests` adds Deployment and Procedure relation-types checks; `VerifyEncodingRelationTypes` adds 3 Sprint 18 breadth regressions.
- Maven: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `136 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-18-maven-2026-05-07.log`.
- TeamEngine smoke: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s18-generator bash scripts/smoke-test.sh`, result `87 total / 55 passed / 0 failed / 32 skipped`.
- Smoke no-mutation oracle: recognized 69 IUT-bound request-log entries and zero IUT-bound POST/PUT/DELETE/PATCH entries for GeoRobotix.
- Runtime outcome: GeoJSON System relation-types PASSed; GeoJSON Deployment, Procedure, and Sampling Feature SKIPped independently; SensorML System, Deployment, and Procedure SKIPped independently.
- Raze implementation review `.harness/evaluations/sprint-ets-18-adversarial-implementation.yaml` returned `APPROVE` confidence 0.92 with no required fixes.
- Historical note: Sprint 18 Generator was committed as `81b7dba`.

## Sprint ets-17 Generator Evidence

Encoding relation-types read-only link checks:

- Story: `epics/stories/s-ets-17-01-encoding-relation-types-readonly.md`
- Contract: `.harness/contracts/sprint-ets-17.yaml`
- OpenSpec: extends `REQ-ETS-PART1-012` and `REQ-ETS-PART1-013`; both remain PARTIAL.
- Scope implemented: selected-resource `/req/geojson/relation-types` and `/req/sensorml/relation-types` checks for associations encoded in JSON `links` members.
- Out of scope: GeoJSON/SensorML `mediatype-write`, mutation behavior, full schema validation, Part 2, and property-level `@link` mapping checks beyond existing mapping assertions.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-06 and not stale.
- OGC source verification: `api/part1/standard/sections/clause_20_requirements_class_geojson_encoding.adoc` and `api/part1/standard/sections/clause_21_requirements_class_sensorml_encoding.adoc` fetched HTTP 200 on 2026-05-06. Both clauses state that associations encoded in `links` must use the association name as the link relation type.
- GeoRobotix planning probe: `/systems/0mqcvdnfoca0` has links with generic `canonical`/`alternate` rels plus association rels `samplingFeatures` and `datastreams`; the association links already use association-name rels.
- GeoRobotix planning probe: `/deployments/16sp744ch58g` and `/procedures/164p7ed8l47g` expose only generic `canonical` and `alternate` links in `links`; deployment `deployedSystems@link` is under `properties`, not `links`.
- GeoRobotix planning probe: `/samplingFeatures/0mtff3l0oofg` has no `links` member; `hostedProcedure@link` is under `properties`, not `links`.
- GeoRobotix planning probe: observed SensorML system/deployment/procedure bodies did not expose links-member association links, so SensorML relation-types checks may SKIP honestly on this IUT until such links exist.
- Implemented verdict policy: PASS only when every detected links-member association uses a `rel` valid for the selected encoding and resource type; SKIP when no links-member association exists; FAIL when a links-member association URL is present but `rel` is missing, generic, not the association name, or valid only for another resource type.
- Resource-specific allowlists: GeoJSON System permits `parentSystem`, `subsystems`, `samplingFeatures`, `deployments`, `procedures`, `datastreams`, and `controlstreams`; GeoJSON Deployment permits `parentDeployment`, `subdeployments`, `featuresOfInterest`, `samplingFeatures`, `datastreams`, and `controlstreams`; GeoJSON Procedure permits `implementingSystems`; GeoJSON Sampling Feature permits `parentSystem`, `sampleOf`, `datastreams`, and `controlstreams`. SensorML System excludes `parentSystem` because it maps to `attachedTo`, not `links`; SensorML has no Sampling Feature representation.
- Raze planning review `.harness/evaluations/sprint-ets-17-plan-adversarial.yaml` returned `GAPS_FOUND` confidence 0.88 for a global association-name allowlist false PASS risk.
- Implementation: `EncodingRelationTypes` centralizes resource-specific allowlists; `GeoJsonTests` adds `geoJsonLinksMemberAssociationRelsUseResourceSpecificNames`; `SensorMlTests` adds `sensorMlLinksMemberAssociationRelsUseResourceSpecificNames`; `VerifyEncodingRelationTypes` adds 5 helper regressions.
- Maven: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `133 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-17-maven-2026-05-06.log`.
- TeamEngine smoke: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s17-generator bash scripts/smoke-test.sh`, result `82 total / 55 passed / 0 failed / 27 skipped`.
- Smoke no-mutation oracle: recognized 55 IUT-bound request-log entries and zero IUT-bound POST/PUT/DELETE/PATCH entries for GeoRobotix.
- Runtime outcome: GeoJSON relation-types PASSed on selected System links; SensorML relation-types SKIPped because the selected SensorML system representation has no top-level links-member association links.
- Raze gap-fix review `.harness/evaluations/sprint-ets-17-plan-gapfix.yaml` returned `APPROVE` confidence 0.94 with no remaining required fixes.
- Raze implementation review `.harness/evaluations/sprint-ets-17-adversarial-implementation.yaml` returned `APPROVE` confidence 0.91 with no required fixes.
- Historical note: Sprint 18 planning has started.

## Sprint ets-18 Planning Evidence

Encoding relation-types breadth read-only checks:

- Story: `epics/stories/s-ets-18-01-encoding-relation-types-breadth-readonly.md`
- Contract: `.harness/contracts/sprint-ets-18.yaml`
- OpenSpec: extends `REQ-ETS-PART1-012` and `REQ-ETS-PART1-013`; both remain PARTIAL.
- Scope planned: broaden Sprint 17 relation-types checks across selected GeoJSON System/Deployment/Procedure/SamplingFeature and SensorML System/Deployment/Procedure resources.
- Out of scope: GeoJSON/SensorML `mediatype-write`, mutation behavior, full schema validation, Part 2, property GeoJSON mapping, and property-level `@link` mapping checks beyond existing mapping assertions.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-07 and not stale.
- OGC source verification: `api/part1/standard/sections/clause_20_requirements_class_geojson_encoding.adoc` and `api/part1/standard/sections/clause_21_requirements_class_sensorml_encoding.adoc` fetched HTTP 200 on 2026-05-07. Both clauses state that associations encoded in `links` must use the association name as the link relation type.
- GeoRobotix planning probe: `/systems/0mqcvdnfoca0` has links with generic `canonical`/`alternate` rels plus association rels `samplingFeatures` and `datastreams`; this remains positive GeoJSON System evidence.
- GeoRobotix planning probe: `/deployments/16sp744ch58g` and `/procedures/164p7ed8l47g` expose only generic `canonical` and `alternate` links in `links`.
- GeoRobotix planning probe: `/samplingFeatures/0mtff3l0oofg` has no `links` member; `hostedProcedure@link` is under `properties`, not `links`.
- GeoRobotix planning probe: observed SensorML system/deployment/procedure bodies expose no top-level `links` member.
- Planned verdict policy: each encoding/resource pair must PASS, FAIL, or SKIP independently; the existing GeoJSON System PASS cannot hide non-system or SensorML SKIPs.
- Raze planning review `.harness/evaluations/sprint-ets-18-plan-adversarial.yaml` returned `APPROVE` confidence 0.92 with no required fixes.
- Historical note: Sprint 18 planning was committed as `41bf9e9`.

## Sprint ets-16 Generator Evidence

SensorML non-system read-only expansion:

- Story: `epics/stories/s-ets-16-01-sensorml-non-system-readonly-expansion.md`
- Contract: `.harness/contracts/sprint-ets-16.yaml`
- OpenSpec: extends `REQ-ETS-PART1-013`; status remains PARTIAL-IMPLEMENTED.
- Scope implemented: deployment, procedure, and property SensorML schema/mapping checks using read-only GET requests.
- Out of scope: `/req/sensorml/mediatype-write`, `/req/sensorml/relation-types`, full external SensorML 3.0 schema validation, mutation-side behavior, sampling feature SensorML claims, GeoJSON, Part 2, and any mutation request.
- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-06 and not stale.
- OGC source verification: `api/part1/standard/requirements/encoding/sensorml/requirements_class_sensorml.adoc` fetched HTTP 200 on 2026-05-06; upstream subrequirements list deployment/procedure/property schema and mapping paths.
- GeoRobotix probes: `/conformance` declares `/conf/sensorml`, `/conf/deployment`, `/conf/procedure`, and `/conf/property`.
- GeoRobotix fallback state: collection `Accept: application/sml+json` requests for `/deployments`, `/procedures`, and `/properties` returned `Content-Type: application/json` CS API wrappers; those wrappers must SKIP rather than PASS SensorML assertions.
- Positive item evidence: `/deployments/16sp744ch58g?f=sml3` returned SensorML JSON with `type=Deployment`, matching identity, and `deployedSystems`; `/procedures/164p7ed8l47g?f=sml3` returned SensorML JSON with `type=PhysicalSystem`, matching identity, and procedure structure.
- Implementation: `SensorMlTests` now has 9 @Tests, adding deployment, procedure, and property read-only SensorML checks with per-resource `/conf/deployment`, `/conf/procedure`, and `/conf/property` gating.
- Resource-specific predicates: deployment requires explicit SensorML `type=Deployment`, preserved identity, and non-empty `deployedSystems`; procedure requires explicit SensorML procedure-compatible type, preserved identity, and non-identity process/procedure structure beyond identifiers; property requires explicit property-compatible SensorML and id/uniqueId/definition/identifier mapping evidence when a property item exists.
- Current IUT state: `/properties` is empty, so `propertySensorMlHasSchemaAndMapping` SKIPs honestly until an IUT supplies a property item.
- Unit coverage: `VerifySensorMlResourceMappingAssertions` adds 6 helper regression tests for empty collection SKIP, first item extraction, identifiers-only procedure rejection, procedure structure acceptance, property evidence, and non-empty mapping values.
- Maven: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `128 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-16-maven-2026-05-06.log`.
- TeamEngine smoke: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s16-generator bash scripts/smoke-test.sh`, result `80 total / 54 passed / 0 failed / 26 skipped`.
- Smoke no-mutation oracle: recognized 53 IUT-bound request-log entries and zero IUT-bound POST/PUT/DELETE/PATCH entries for GeoRobotix.
- Runtime outcome: deployment and procedure SensorML checks PASS through `application/sml+json` alternate links; property SensorML check SKIPs because GeoRobotix `/properties` is empty.
- Raze planning review `.harness/evaluations/sprint-ets-16-plan-adversarial.yaml` returned `GAPS_FOUND` confidence 0.86. Required fixes applied in planning: added resource conformance-class gating for `/conf/deployment`, `/conf/procedure`, and `/conf/property`; tightened procedure mapping so `identifiers` alone cannot satisfy procedure-specific SensorML evidence.
- Raze planning gap-fix `.harness/evaluations/sprint-ets-16-plan-gapfix.yaml` returned `APPROVE` confidence 0.94 with no remaining required fixes.
- Raze implementation review `.harness/evaluations/sprint-ets-16-adversarial-implementation.yaml` returned `APPROVE` confidence 0.92 with no required fixes.
- Historical note: Sprint 16 Generator was committed as `72820e3`.

## Sprint ets-15 Generator Evidence

GeoJSON non-system read-only expansion:

- Story: `epics/stories/s-ets-15-01-geojson-non-system-readonly-expansion.md`
- Contract: `.harness/contracts/sprint-ets-15.yaml`
- OpenSpec: extends `REQ-ETS-PART1-012`; status remains PARTIAL-IMPLEMENTED.
- Scope implemented: deployment, procedure, and sampling feature GeoJSON schema/mapping checks using read-only collection requests.
- Out of scope: `/req/geojson/mediatype-write`, `/req/geojson/relation-types`, property GeoJSON mapping, full external GeoJSON schema validation, SensorML, Part 2, and any mutation request.
- Architecture freshness: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-06 and not stale.
- OGC source: `api/part1/standard/requirements/encoding/geojson/requirements_class_geojson.adoc`, fetched HTTP 200 on 2026-05-06. Relevant subrequirements are `/req/geojson/deployment-schema`, `/req/geojson/deployment-mappings`, `/req/geojson/procedure-schema`, `/req/geojson/procedure-mappings`, `/req/geojson/sf-schema`, and `/req/geojson/sf-mappings`.
- GeoRobotix planning probe: `/conformance` returned HTTP 200 and declares `/conf/geojson`, `/conf/deployment`, `/conf/procedure`, and `/conf/sf`.
- GeoRobotix non-system GeoJSON probe: `GET /deployments?limit=1`, `/procedures?limit=1`, and `/samplingFeatures?limit=1` with `Accept: application/geo+json` all returned HTTP 200 with `Content-Type: application/json` and top-level `items`; this is fallback evidence, not GeoJSON FeatureCollection PASS evidence.
- Implementation: `GeoJsonTests` now has 8 @Tests, adding `/deployments`, `/procedures`, and `/samplingFeatures` read-only GeoJSON checks. CS API default `items` wrappers without GeoJSON `features` SKIP with reason, not PASS schema/mapping assertions.
- Resource-specific predicates: deployment mapping checks `properties.deployedSystems@link`; procedure mapping checks `geometry == null` plus `properties.featureType`; sampling feature mapping checks `properties.featureType` plus `properties.hostedProcedure@link` or `properties.radius`. Generic Feature shape alone is not enough for schema/mapping PASS.
- Unit coverage: `VerifyGeoJsonResourceMappingAssertions` adds 5 helper regression tests for fallback SKIP and mapping-value behavior.
- Maven: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `122 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-15-maven-2026-05-06.log`.
- TeamEngine smoke: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s15-generator bash scripts/smoke-test.sh`, result `77 total / 52 passed / 0 failed / 25 skipped`.
- Smoke no-mutation oracle: recognized 44 IUT-bound request-log entries and zero IUT-bound POST/PUT/DELETE/PATCH entries for GeoRobotix.
- Raze planning review: `.harness/evaluations/sprint-ets-15-plan-adversarial.yaml` returned `GAPS_FOUND` confidence 0.86; required fix was to add resource-specific schema/mapping predicates.
- Raze planning gap-fix recheck: `.harness/evaluations/sprint-ets-15-plan-gapfix.yaml` returned `APPROVE` confidence 0.93 with no remaining required fixes.
- Raze implementation review: `.harness/evaluations/sprint-ets-15-adversarial-implementation.yaml` returned `APPROVE_WITH_CONCERNS` confidence 0.91 for stale class javadoc only.
- Raze gap-fix recheck: `.harness/evaluations/sprint-ets-15-adversarial-gapfix.yaml` returned `APPROVE` confidence 0.96 with no remaining required fixes or concerns.

## Sprint ets-14 Planning

Update positive mutable-IUT hardening:

- Story: `epics/stories/s-ets-14-01-update-positive-mutable-iut-hardening.md`
- Contract: `.harness/contracts/sprint-ets-14.yaml`
- OpenSpec: extends `REQ-ETS-PART1-011`; status remains PARTIAL unless positive PATCH executes and verifies a changed field.
- Scope planned: correct Update source-path citation to `requirements/crud/update`, add an OPTIONS/PATCH verdict matrix, require GET-after-PATCH changed-field assertion for `properties.name`, add focused unit coverage for status-only PATCH false positives, and record local OSH readiness honestly.
- OPTIONS/PATCH verdict matrix: missing `/conf/update`, absent mutation opt-in, public-IUT hard denial, no candidate System, or inconclusive OPTIONS are SKIP-before-PATCH states; declared `/conf/update` plus successful OPTIONS omitting PATCH FAILs readiness for `/req/update/system`, while lifecycle still SKIPs before PATCH; declared `/conf/update` plus explicit mutation opt-in plus `Allow: PATCH` may run guarded PATCH and must assert the changed field.
- Local OSH planning probe: `OPTIONS /systems/040g` returned HTTP 200 with `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS`; PATCH absent. Simple authenticated `/conformance` curl returned HTTP 401 with attempted basic credentials, so TeamEngine smoke credential path remains the authoritative local path.
- Guardrail: do not claim local OSH positive Update support without observed `/conf/update`, `OPTIONS PATCH`, and changed-field evidence. Default GeoRobotix smoke must still issue zero IUT-bound PATCH.
- Raze planning review: `.harness/evaluations/sprint-ets-14-plan-adversarial.yaml` returned `GAPS_FOUND` confidence 0.87; required fixes applied.
- Raze planning gap-fix review: `.harness/evaluations/sprint-ets-14-plan-gapfix.yaml` returned `APPROVE` confidence 0.93 with no remaining required fixes.
- Implementation: `UpdateTests.systemsPatchLifecycleOptIn` now requires GET after PATCH and asserts `properties.name` equals the intended patched value; `VerifyUpdateChangedFieldAssertion` adds four focused unit tests.
- Maven: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `117 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-14-maven-2026-05-06.log`.
- TeamEngine smoke: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s14-generator bash scripts/smoke-test.sh`, result `74 total / 52 passed / 0 failed / 22 skipped`.
- Smoke no-mutation oracle: recognized 41 IUT-bound request-log entries and zero IUT-bound POST/PUT/DELETE/PATCH entries for GeoRobotix.
- Local OSH readiness probe: `/conformance` HTTP 401; `OPTIONS /systems/040g` HTTP 200 with `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS`; PATCH absent. No local OSH PATCH was issued.
- Raze implementation review: `.harness/evaluations/sprint-ets-14-adversarial-implementation.yaml` returned `GAPS_FOUND` confidence 0.84 for missing REQ/SCENARIO trace comments in `VerifyUpdateChangedFieldAssertion`.
- Raze gap-fix recheck: `.harness/evaluations/sprint-ets-14-adversarial-gapfix.yaml` returned `APPROVE` confidence 0.94 with no remaining required fixes.

## Sprint ets-13 Generator Evidence

Update/PATCH safety-gated systems subset:

- Story: `epics/stories/s-ets-13-01-update-safety-gated-systems-subset.md`
- Contract: `.harness/contracts/sprint-ets-13.yaml`
- OpenSpec: `REQ-ETS-PART1-011`, status PARTIAL-IMPLEMENTED for Sprint 13
- Scope implemented: declaration-gated `/conf/update`, reuse Sprint 12 mutation opt-in parameters, non-mutating `OPTIONS /systems/{id}` PATCH readiness, default lifecycle SKIP-before-PATCH, public GeoRobotix hard-denial, Update -> CreateReplaceDelete dependency wiring, and PATCH-aware no-mutation smoke oracle
- Explicitly excluded: unguarded PATCH against GeoRobotix, deployment/procedure/sampling-feature/property PATCH, Feature Collection update paths from OGC ATS A.79-A.83, Part 2 `/conf/update`, optimistic locking, and PATCH media-type matrix including JSON Patch, merge patch, and content negotiation
- Corrected story ID: prior epic placeholder reused `S-ETS-07-03`; Sprint 13 planning corrected the Update story to `S-ETS-13-01`.
- Architecture freshness: `_bmad/architecture.md` last reconciled 2026-04-28; checked 2026-05-06 and not stale.
- OGC source: OGC API - Connected Systems Part 1 Clause 17, Requirements Class "Update" `/req/update`; upstream source path `api/part1/standard/requirements/crud/update/requirements_class_update.adoc`; prerequisite `/req/create-replace-delete`; systems endpoint `{api_root}/systems/{id}` uses HTTP PATCH.
- GeoRobotix planning probe: `/conformance` does not declare `/conf/update`; `OPTIONS /systems/0mqcvdnfoca0` returns HTTP 200 with `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS` and no PATCH.
- Local OSH planning probe: unauthenticated `/conformance` returns HTTP 401; unauthenticated `OPTIONS /systems/040g` returns HTTP 200 with no PATCH in `Allow`.
- Planning interpretation: Sprint 13 should implement skip-first safety and wiring. Current default/public IUT evidence supports no positive PATCH conformance claim.
- Raze planning review: `.harness/evaluations/sprint-ets-13-plan-adversarial.yaml` verdict `APPROVE_WITH_CONCERNS` confidence 0.88. Two planning tightenings were applied: contract media-type exclusions now match story/spec, and OGC ATS A.79-A.83 collection item update paths are cited as deferred.
- Implementation: `UpdateTests.java` added with 5 `update` @Tests; `testng.xml` declares `<group name="update" depends-on="createreplacedelete"/>`; `VerifyTestNGSuiteDependency` adds 3 Update lint tests.
- No-mutation oracle: `scripts/no-mutation-oracle.py` and `scripts/smoke-test.sh` now treat PATCH as mutating alongside POST, PUT, and DELETE.
- Maven: `bash scripts/mvn-test-via-docker.sh` BUILD SUCCESS, `113 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-13-maven-2026-05-06.log`.
- TeamEngine smoke: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results bash scripts/smoke-test.sh`, result `74 total / 52 passed / 0 failed / 22 skipped`.
- Smoke no-mutation oracle: integrated smoke oracle recognized 41 IUT-bound request-log entries and zero IUT-bound POST/PUT/DELETE/PATCH entries for `https://api.georobotix.io/ogc/t18/api`.
- Runtime note: default GeoRobotix Update config records missing `/conf/update`, then the five Update @Tests are dependency-skipped because the Update group depends on the default-skipped Create/Replace/Delete mutation safety gate. No PATCH was issued.
- Raze implementation review: `.harness/evaluations/sprint-ets-13-adversarial-implementation.yaml` verdict `GAPS_FOUND` confidence 0.86; required documentation/evidence fixes applied. Code safety, no-mutation oracle, TestNG dependency, OGC URI/scope fidelity, and unrelated dirty-file checks were acceptable.
- Raze gap-fix recheck: `.harness/evaluations/sprint-ets-13-adversarial-gapfix.yaml` verdict `APPROVE` confidence 0.91; no required fixes remain.
- Quinn Gate 3.5: `.harness/evaluations/sprint-ets-13-evaluator-gate.yaml` verdict `APPROVE_WITH_CONCERNS` confidence 0.91. Independent `/tmp` clone Maven reported `113 tests / 0 failures / 0 errors / 3 skipped`; independent TeamEngine smoke reported `74 total / 52 passed / 0 failed / 22 skipped`, with 41 recognized IUT-bound request-log entries and zero IUT-bound POST/PUT/DELETE/PATCH. Concerns are positive PATCH coverage pending a dedicated mutable IUT and stronger patched-field assertion before future promotion beyond PARTIAL.
- Raze review of Quinn artifact: `.harness/evaluations/sprint-ets-13-quinn-gate-raze-review.yaml` verdict `APPROVE` confidence 0.89 after correcting the Quinn artifact's sprint file list.
- Raze Gate 4: `.harness/evaluations/sprint-ets-13-adversarial-gate.yaml` verdict `APPROVE_WITH_CONCERNS` confidence 0.90. Independent `/tmp` clone Maven reported `113 tests / 0 failures / 0 errors / 3 skipped`; no-mutation oracle self-test passed; independent TeamEngine smoke reported `74 total / 52 passed / 0 failed / 22 skipped`, with 41 recognized IUT-bound request-log entries and zero IUT-bound POST/PUT/DELETE/PATCH. No required fixes. Low follow-up: decide whether missing `OPTIONS Allow: PATCH` should fail, skip, or be supplemented before positive mutable-IUT Update gates.

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
- Full REQ-ETS-PART1-012 remains open for `mediatype-write`, `relation-types`, property GeoJSON mapping, and full schema-validation closure; Sprint 15 now implements deployment/procedure/sampling-feature read-only schema/mapping checks.

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

1. Commit and push Sprint 20 planning.
2. Start Generator for `S-ETS-20-01`.

## Dirty Worktree Notes

Current dirty worktree is expected Sprint ets-20 planning work plus metrics:

- `epics/stories/s-ets-20-01-part2-api-common-planning.md`
- `.harness/contracts/sprint-ets-20.yaml`
- OpenSpec/traceability/epic/status/changelog/test-results/planner handoff planning reconciliation.
- `ops/metrics.md` turn-log updates.
