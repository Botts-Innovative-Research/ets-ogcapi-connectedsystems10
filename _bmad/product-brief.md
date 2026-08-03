# Product Brief: Alternate Mutable IUT Discovery for OGC API Connected Systems
**Date**: 2026-08-03T05:08:39Z
**Status**: Reviewed
**Triggered by**: User requested Discovery Agent research into open-source OGC API Connected Systems implementations, especially candidates with better coverage than OpenSensorHub (OSH), mutation/update support, Part 2 support, or conformance declarations.

## Problem Statement

The ETS has `240 total / 191 exact / 2 helper / 47 candidate / 0 unmapped` released ATS procedures. The remaining 47 are mutation-bound across Part 1 Create/Replace/Delete, Part 1 Update, Part 2 Create/Replace/Delete, and Part 2 Update. Sprint 74 readiness evidence shows the current local OSH target is useful as a disposable mutable IUT for safety, provisioning, cleanup, and baseline diagnostics, but it cannot honestly close those 47 mappings as reviewed exact.

Current blocker evidence:

- Direct local OSH readiness audit: `47` candidates, `GET=1`, `OPTIONS=25`, `unsafeMethodsIssued=[]`, one declaration/method-ready class (`1:/conf/create-replace-delete`), zero prerequisite-declaration-ready classes.
- Disposable local OSH Sprint 74 run `sprint-ets-74-fields-20260803T042511Z`: readiness audit `GET=1`, `OPTIONS=27`, `unsafeMethodsIssued=[]`, provisioning PASS, cleanup PASS, primary-state isolation PASS.
- TeamEngine remains non-green on the known local OSH baseline: populated `275/24/20/231`, clean-primary `275/23/20/232`.
- `ops/known-issues.md` records the missing prerequisites: Part 1 and Part 2 `/conf/api-common`, exact inherited OGC API Features Part 4 create-replace-delete/update declarations, `/conf/update`, `/conf/feasibility`, PATCH advertisement, positive lifecycle proof, changed-resource GET proof, cleanup/isolation, cascade, collection propagation, and URI-list evidence.

The discovery goal is therefore narrow: find an open-source implementation that can become a dedicated, controlled mutable IUT for future positive lifecycle evidence, without mutating public demo services and without promoting candidates from read-only probe evidence.

## Research Findings

### Official Implementation Registry

The official OGC Connected Systems repository currently lists two server-side implementations:

- OpenSensorHub Server, open source, Java, status "In progress", claims OGC API Connected Systems Parts 1, 2, 3, 4, and 5.
- 52North pygeoapi extension, open source, Python, status "In progress", claims Parts 1, 2, 3, and 5.

Source: https://github.com/opengeospatial/ogcapi-connected-systems/blob/master/implementations.adoc

The OGC CSAPI developer site adds live public demo and ecosystem context:

- "CS GO Reference Server" is powered by `connected-systems-go` and implements Part 1 and Part 2.
- The public OSH node exposes live sensors, datastreams, observations, SensorML, and SWE Common payloads through Part 1/2 endpoints.
- The 52North CSA demo is powered by `connected-systems-pygeoapi`.

Source: https://csapi.developer.ogc.org/

### Candidate Assessment

| Candidate | Open source | CS API claim | Conformance declaration evidence | Mutation evidence | Update/PATCH evidence | Part 2 evidence | ETS usefulness |
|---|---:|---|---|---|---|---|---|
| `SomethingCreativeStudios/connected-systems-go` | Public GitHub repo; license not declared in GitHub API metadata | README says Go implementation of OGC API Connected Systems Part 1 Feature Resources and Part 2 Dynamic Data | Live `/conformance` declares Part 1 `/conf/api-common`, Part 2 `/conf/api-common`, Part 2 Datastream/Observation/ControlStream/Command/SystemEvent/JSON/Create-Replace-Delete | Source routes and e2e tests cover POST/PUT/DELETE for Part 1 and Part 2 resources; public read-only audit issued no writes | No `/conf/update`; no real PATCH routes found beyond global CORS method advertisement | Strongest found Part 2 CRD candidate; live demo has populated systems/datastreams/controlstreams/observations | Best next candidate for a self-run disposable mutable IUT, especially Part 2 CRD. Does not appear able to close Part 1 CRD or Update as-is. |
| OS4CSAPI fork of `connected-systems-go` | Public fork | Same project lineage | OS4CSAPI developer site uses the Go server as reference integration target | Same as upstream unless fork diverges | Same concern as upstream | Same claim | Track if OS4CSAPI hosts deployment-specific patches, but upstream is fresher. |
| `52North/connected-systems-pygeoapi` | Public GitHub repo, Apache-2.0 | 52North says Part 1 is fully implemented and Part 2 is actively developed | Public demo `/conformance` currently advertises only OGC API Common Core | Source has CS-specific GET/POST/PUT/DELETE routes and Part 2 datastream update/provider code comments | Route decorators do not expose PATCH; provider comments mention update targets but observation update is unsupported | Part 2 in active development; public demo `/datastreams` returned server error during probe | Promising medium-term self-run candidate, but public deployment is not a usable declaring IUT today. |
| 52North `pygeoapi` `feature/connected-systems` branch | Public GitHub branch | Official registry links this branch | Static bundled OpenAPI uses older/historical CS URI names such as `system-features` and encoding classes | Generic pygeoapi feature write routes exist; CS route fit is unclear | No convincing current PATCH/update evidence found | Some Part 1/2 provider and OpenAPI assets exist | Not stronger than the standalone 52North CSA app for this ETS without branch reconciliation. |
| OpenSensorHub / OSH | Public GitHub repo, MPL-2.0 | Broad CS API support; OS4CSAPI public demo advertises Parts 1, 2, and 3 classes | Public demo declares many CS classes including Part 1 CRD, Part 2 CRD, JSON, SWE Common JSON/Text/Binary, WebSocket, MQTT | Local disposable OSH workflow has safe provisioning/cleanup evidence, but readiness audit still blocks exact promotion | No Part 1/2 `/conf/update`; no PATCH readiness sufficient for exact closure | Broadest encoding coverage among found implementations | Keep as baseline and local disposable diagnostic IUT, but not enough for the remaining mutation exact closure. |
| FROST-Server | Public GitHub repo, AGPL-3.0 | OGC SensorThings API reference implementation, not CS API | SensorThings conformance, not Connected Systems `/conformance` | Mature create/update/delete support in SensorThings | SensorThings Tasking support, not CS Part 2 Update | SensorThings Part 1/Part 2, not CS Part 2 | Useful adapter/substrate candidate only; not a direct CS API IUT. |
| GOST | Public GitHub repo | SensorThings API Part 1 Sensing plus MQTT | SensorThings ETS badge/status, not CS API | SensorThings Create-Update-Delete tests reported | Tasking planned, not CS Update | SensorThings, not CS | Not a direct CS API IUT. |
| istSOS4 | Public GitHub repo | SensorThings API server | SensorThings surface, not CS API | CRUD via SensorThings stack | No CS Update evidence | SensorThings, not CS | Not a direct CS API IUT. |

### Connected Systems Go Details

Repository: https://github.com/SomethingCreativeStudios/connected-systems-go

Public demo: https://129-80-248-53.sslip.io/csapi-go-v2/

The Go implementation is the strongest alternate IUT candidate found. Its README claims OGC API Connected Systems Part 1 and Part 2 support, and documents POST/PUT/DELETE routes for systems, deployments, procedures, properties, datastreams, observations, controlstreams, commands, and system events.

Local source inspection of a research clone found:

- `internal/api/conformance_handler.go` declares Part 1 `/conf/api-common` and Part 2 `/conf/api-common`, Datastream, Observation, ControlStream, Command, SystemEvent, JSON, and Create/Replace/Delete.
- `internal/api/router.go` registers POST/PUT/DELETE routes for several Part 1 and Part 2 resources.
- `e2e/systems_test.go` includes create, replace, GET proof, and delete proof tests for systems.
- `e2e/control_streams_test.go` includes ControlStream CRUD, cascade delete, and schema update tests.
- No actual PATCH route or `/conf/update` declaration was found. The global CORS header advertises PATCH, but that is not enough to treat Update readiness as proved.

Live read-only probe evidence is archived under `ops/test-results/sprint-ets-75-alternate-iut-discovery-2026-08-03/`:

- `csapi-go-public-conformance.json` declares Part 1 `/conf/api-common` and Part 2 `/conf/api-common` plus Part 2 `/conf/create-replace-delete`.
- `csapi-go-public-readiness.json` records `GET=1`, `OPTIONS=25`, `unsafeMethodsIssued=[]`, and no exact-promotion-ready mutation class because the public OPTIONS probes did not provide `Allow` evidence for expected mutation methods.
- Public demo `/api` returned only a minimal OpenAPI skeleton during research, so service-description-based write operation checks may require self-run configuration or code-backed evidence.

Interpretation: this is a viable open-source candidate beyond OSH for exploratory Part 2 Create/Replace/Delete lifecycle closure in a self-run disposable environment. It is not an all-47-candidate solution today.

### 52North CSA / pygeoapi Details

Project page: https://52north.org/software/software-components/ogc-api-connected-systems/

Repository: https://github.com/52North/connected-systems-pygeoapi

Public demo: https://csa.demo.52north.org/

52North states that its implementation is Python-based, uses pygeoapi with Elasticsearch and TimescaleDB, has Part 1 already fully implemented, and is actively developing Part 2. The repository is Apache-2.0 and contains CS-specific routes and providers.

The live public demo is not currently a usable declaring IUT for this ETS:

- `/conformance` returned only `http://www.opengis.net/spec/ogcapi-common-1/1.0/conf/core`.
- TLS validation failed because the certificate was expired during research; `curl -k` was required.
- `/datastreams?limit=1` returned HTTP 500 during research.

Interpretation: 52North is worth tracking and may be useful if self-run, seeded, and configured with current conformance declarations. It is not a better immediate target than connected-systems-go.

### OpenSensorHub Details

Repository: https://github.com/opensensorhub/osh-core

Public OS4CSAPI demo: https://129-80-248-53.sslip.io/sensorhub/api

The public OSH demo declares broad CS API classes, including Part 1 CRD, Part 2 CRD, JSON, SWE Common JSON/Text/Binary, and Part 3 WebSocket/MQTT. That breadth is valuable for read-only and encoding conformance work, but it does not solve the current mutation blockers because the exact current prerequisite declarations and Update/PATCH surface are missing.

Interpretation: OSH remains useful, but current evidence supports the user's suspected gap: it is not enough for the remaining mutation exact closure.

### SensorThings-Related Systems

FROST-Server, GOST, and istSOS4 are mature open-source SensorThings implementations. They may be useful substrate projects if the ETS team later decides to build an adapter or seed data from SensorThings concepts, but they do not expose a current OGC API Connected Systems conformance surface and should not be treated as direct IUT candidates.

Sources:

- https://github.com/FraunhoferIOSB/FROST-Server
- https://github.com/gost/server
- https://github.com/istSOS/istSOS4/

## Proposed Approach

Use `connected-systems-go` as the first alternate mutable-IUT investigation target, but keep the next sprint read-only unless and until a dedicated self-run instance is available.

Recommended sequence:

1. Keep public deployments strictly read-only. Public probes may fetch `/conformance`, service description, representative collection resources, and OPTIONS headers only.
2. Create a dedicated local or disposable `connected-systems-go` IUT outside public demo infrastructure.
3. Seed predictable systems, procedures, deployments, datastreams, controlstreams, observations, commands, and system events.
4. Run the same readiness audit against that self-run instance and compare declarations, OpenAPI/service-description quality, OPTIONS `Allow` headers, and route behavior.
5. If the self-run Go IUT can satisfy exact declarations and cleanup/isolation requirements, scope a future sprint to one narrow positive lifecycle slice, most likely Part 2 Create/Replace/Delete for Datastream or ControlStream.
6. Do not pursue Update/PATCH exact closure from this candidate until `/conf/update` and real PATCH routes exist.
7. Track 52North as a secondary self-run candidate, mainly for Part 1, after resolving public demo conformance, TLS, and runtime health issues.

### Alternatives Considered

**Use public `connected-systems-go` demo directly for mutations**: Rejected. Public demos are not controlled mutable IUTs. Positive lifecycle proof must be collected against a dedicated instance with cleanup and primary-state isolation.

**Continue relying only on local OSH**: Rejected as a closure path for the remaining 47 candidates. Sprint 74 proves OSH is safe for diagnostics but currently lacks exact prerequisite/update declarations and sufficient mutation lifecycle evidence.

**Use 52North public demo as the next IUT**: Rejected for immediate work. The public demo's conformance declaration currently exposes only OGC API Common Core, and `/datastreams` was unhealthy during research.

**Adapt a SensorThings server such as FROST-Server**: Deferred. These systems are strong open-source mutable sensor API stacks, but building a CS API facade would be implementation work, not discovery of an existing IUT.

## Requirements Summary

- `REQ-ALT-IUT-001` - Maintain a source-backed inventory of open-source CS API and adjacent SensorThings implementations, including repository URL, license status, activity, conformance claims, mutation claims, and live-demo status.
- `REQ-ALT-IUT-002` - Preserve public candidate probes as read-only evidence: GET and OPTIONS only, `unsafeMethodsIssued=[]`, no credentials logged, and no exact mapping promotion.
- `REQ-ALT-IUT-003` - Distinguish public-demo readiness from positive lifecycle proof against a dedicated mutable IUT.
- `REQ-ALT-IUT-004` - Prefer `connected-systems-go` for the first disposable alternate-IUT experiment because it is the only researched open-source candidate with live Part 2 API Common and Part 2 Create/Replace/Delete declarations.
- `REQ-ALT-IUT-005` - Before any mutation exact closure sprint, require exact prerequisite declarations, actual method advertisement or equivalent service-description proof, POST/PUT/DELETE or PATCH lifecycle execution, changed-resource GET proof, cleanup/isolation evidence, and no public-IUT writes.
- `REQ-ALT-IUT-006` - Treat Update/PATCH closure as blocked until an implementation declares `/conf/update` and exposes real PATCH handlers, not only CORS method strings.

## Risks and Open Questions

- `connected-systems-go` has no license declared in GitHub API metadata. Confirm legal reuse terms before embedding it into regular project automation.
- `connected-systems-go` README cites Part 2 as IS 24-008, while this ETS project tracks Part 2 as OGC 23-002. Confirm the final standard designation before using the implementation as normative evidence.
- `connected-systems-go` declares `system-history`, which the project has previously treated as non-released or at least not part of the current released ATS closure set. Ignore non-ATS declarations unless the standard mapping is reconciled.
- Public `connected-systems-go` OPTIONS responses do not provide `Allow` method evidence in the archived readiness audit, even though source routes exist. Do not infer exact readiness from CORS alone.
- The public `connected-systems-go` `/api` response was too minimal for service-description write-operation checks during research.
- 52North public demo health is weak: expired TLS certificate, `/conformance` only Common Core, and `/datastreams` error response during research.
- No researched candidate currently demonstrates complete Part 1 CRD, Part 1 Update, Part 2 CRD, and Part 2 Update closure for all 47 remaining candidates.
- Mutation testing requires credentials, fixture ownership, deterministic cleanup, and evidence isolation. None of the public demos satisfy that bar by themselves.

## Feasibility Assessment

- Technical: **FEASIBLE WITH CONCERNS**. A stronger alternate candidate exists beyond OSH: `connected-systems-go`. It plausibly supports enough Part 2 Create/Replace/Delete behavior to justify a self-run disposable-IUT experiment. It does not appear to support Update/PATCH closure today.
- Complexity: **MODERATE** for a read-only compatibility audit and self-run Go instance. **COMPLEX** for exact mutation closure because declarations, service descriptions, fixture seeding, lifecycle proof, and cleanup evidence must all align with ETS expectations.
- Dependencies: `connected-systems-go` local runtime, PostgreSQL/PostGIS or its documented storage stack, deterministic seed data, a non-public mutable-IUT URL, and maintainer/license clarification. 52North requires pygeoapi, Elasticsearch, TimescaleDB, seed data, and conformance configuration before it can compete as an IUT.

## Sources

- OGC Connected Systems implementation registry: https://github.com/opengeospatial/ogcapi-connected-systems/blob/master/implementations.adoc
- OGC CSAPI developer demos: https://csapi.developer.ogc.org/
- `connected-systems-go`: https://github.com/SomethingCreativeStudios/connected-systems-go
- Public `connected-systems-go` demo: https://129-80-248-53.sslip.io/csapi-go-v2/
- OS4CSAPI `connected-systems-go` fork: https://github.com/OS4CSAPI/connected-systems-go
- 52North CS API project page: https://52north.org/software/software-components/ogc-api-connected-systems/
- 52North `connected-systems-pygeoapi`: https://github.com/52North/connected-systems-pygeoapi
- 52North CSA demo: https://csa.demo.52north.org/
- 52North pygeoapi feature branch: https://github.com/52North/pygeoapi/tree/feature/connected-systems
- OpenSensorHub core: https://github.com/opensensorhub/osh-core
- Public OSH demo: https://129-80-248-53.sslip.io/sensorhub/api
- OS4CSAPI meta project: https://github.com/OS4CSAPI/os4csapi-meta
- FROST-Server: https://github.com/FraunhoferIOSB/FROST-Server
- GOST SensorThings server: https://github.com/gost/server
- istSOS4: https://github.com/istSOS/istSOS4/
