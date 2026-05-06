# Story S-ETS-14-01: Update Positive Mutable-IUT Hardening

> Sprint: ets-14
> Status: PLANNED
> Priority: P0
> Complexity: M
> Epic: epic-ets-02-part1-classes
> OpenSpec: REQ-ETS-PART1-011

## User Value

As an OGC API Connected Systems server implementer, I need the guarded Update/PATCH lifecycle path to prove an actual representation change when a dedicated mutable IUT supports Update, while staying honest when the current local OSH fixture does not advertise PATCH.

## Scope

Extend the Sprint 13 `/conf/update` systems subset. This sprint does not add a new Part 1 conformance class. It hardens the existing positive PATCH path and records local mutable-IUT readiness.

The Generator must:

1. Correct the Update source-path citation to `api/part1/standard/requirements/crud/update/requirements_class_update.adoc`.
2. Decide and encode readiness semantics with an explicit verdict matrix so declared Update support without PATCH readiness cannot be hidden as an unconditional SKIP.
3. Strengthen `systemsPatchLifecycleOptIn` so a successful PATCH is followed by GET and an assertion that a changed field actually changed.
4. Add focused unit coverage for the changed-field extraction/assertion path so a status-only PATCH cannot be mistaken for lifecycle evidence.
5. Probe the local OSH mutable fixture with credentials supplied through the established smoke path and record whether it declares `/conf/update` and advertises PATCH.
6. Keep default GeoRobotix smoke non-mutating and keep REQ-ETS-PART1-011 at PARTIAL unless positive PATCH actually executes and verifies a changed field on a dedicated mutable IUT.

## OPTIONS/PATCH Verdict Matrix

| Runtime state | Readiness verdict | Lifecycle verdict | PATCH issued |
|---|---|---|---|
| `/conf/update` is absent | SKIP with missing-conformance reason | SKIP | No |
| Mutation opt-in parameters are absent or public GeoRobotix is targeted | SKIP with safety-gate reason | SKIP | No |
| No candidate System resource is available | SKIP with no-candidate reason | SKIP | No |
| `OPTIONS /systems/{id}` returns a successful response with `Allow` omitting PATCH while `/conf/update` is declared | FAIL the readiness assertion for `/req/update/system` | SKIP before PATCH, because the precondition failed | No |
| `OPTIONS /systems/{id}` is unavailable or inconclusive and the IUT otherwise declares `/conf/update` | SKIP with blocked-readiness reason unless future OGC/CITE policy requires FAIL | SKIP | No |
| `/conf/update` is declared, mutation opt-in is explicit, the IUT is not public GeoRobotix, and `Allow` includes PATCH | PASS readiness | Run guarded PATCH lifecycle and assert changed field | Yes |

## Requirements

- REQ-ETS-PART1-011
- SCENARIO-ETS-PART1-011-UPDATE-SYSTEM-PATCH-CHANGED-FIELD-001
- SCENARIO-ETS-PART1-011-UPDATE-LOCAL-OSH-READINESS-001
- SCENARIO-ETS-PART1-011-UPDATE-OPTIONS-PATCH-SKIP-SEMANTICS-001
- SCENARIO-ETS-PART1-011-UPDATE-SMOKE-NO-PATCH-001

## Planned Test Surface

1. `systemsPatchLifecycleOptIn` continues to SKIP before PATCH unless mutation opt-in parameters are present, the IUT declares `/conf/update`, the IUT is not GeoRobotix, and `OPTIONS /systems/{id}` advertises PATCH.
2. If PATCH runs and returns 200 or 204, the test performs GET on the patched temporary System resource and asserts the patched `properties.name` equals the intended new value.
3. If the GET response does not expose the changed value, the test FAILs with the Update requirement URI rather than treating status-only PATCH as conformance evidence.
4. Unit tests cover nested `properties.name` extraction and the failure path for missing or unchanged values.
5. A local OSH readiness probe records authenticated `/conformance` and `OPTIONS /systems/040g`; if `/conf/update` or PATCH advertisement is absent, Generator records SKIP evidence and does not issue PATCH.

## Planning Evidence

- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28, checked 2026-05-06, not stale.
- OGC upstream source: `opengeospatial/ogcapi-connected-systems` master commit `3fd86c73e744b7e2faaf7f1c17366bfb9ff4cd6f`.
- Requirement class source: `api/part1/standard/requirements/crud/update/requirements_class_update.adoc`.
- Clause source: `api/part1/standard/sections/clause_17_requirements_class_update.adoc`.
- Requirement class identifier: `/req/update`.
- Listed subrequirements: `/req/update/system`, `/req/update/deployment`, `/req/update/procedure`, `/req/update/sampling-feature`, and `/req/update/property`.
- Local OSH probe on 2026-05-06: `OPTIONS /systems/040g` returned HTTP 200 with `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS`; PATCH absent. Authenticated `/conformance` could not be confirmed through the simple curl probe because the running OSH instance returned HTTP 401 with the attempted basic credentials.
- Interpretation: local OSH remains a valid dedicated mutable CRD fixture, but current evidence does not support positive Update/PATCH execution. Sprint 14 must harden the ETS code path and produce honest SKIP/readiness evidence unless a mutable IUT with `/conf/update` and PATCH advertisement is available.

## Definition of Done

- [ ] `UpdateTests` positive PATCH lifecycle asserts changed representation content after PATCH.
- [ ] Unit tests cover changed-field extraction/assertion behavior.
- [ ] Missing `OPTIONS Allow: PATCH` uses the verdict matrix: declared `/conf/update` plus successful OPTIONS without PATCH FAILs readiness, while lifecycle still SKIPs before PATCH.
- [ ] GeoRobotix default smoke still emits zero IUT-bound PATCH requests.
- [ ] Local OSH readiness probe is run or explicitly blocked with exact reason and recorded in `ops/test-results.md`.
- [ ] OpenSpec, story, traceability, status, changelog, and test-results are reconciled.
- [ ] Raze reviews non-trivial planning and implementation changes before completion.

## Out Of Scope

- Deployment, procedure, sampling-feature, and property PATCH.
- Feature Collection update paths under `/collections/{collectionId}/items/{id}`.
- Part 2 `/conf/update`.
- Optimistic locking.
- PATCH media-type matrix, including JSON Patch, merge patch, and content negotiation.
- Claiming local OSH positive Update support without observed `/conf/update`, `OPTIONS Allow: PATCH`, and a verified changed field.
