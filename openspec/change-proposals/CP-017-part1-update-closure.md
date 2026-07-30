# Change Proposal: CP-017 Part 1 Update Direct ATS Closure

**Date**: 2026-07-30
**Author**: Codex
**Affects**:

- `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- `openspec/capabilities/ets-ogcapi-connectedsystems/design.md`
- `REQ-ETS-PART1-011`
- `REQ-ETS-COVERAGE-001`

**Status**: Accepted; exact replacement `40cc703` awaits focused Raze recheck and positive PATCH E2E

## Motivation

The released OGC 23-001 `/conf/update` class contains five procedures. The
historical five-method class has one candidate mapping and four unmapped
procedures. It separates declaration and OPTIONS readiness from the System
lifecycle, omits Deployment, Procedure, Sampling Feature, Property, and custom
collection endpoints, guesses `application/json` as the patch-document media
type, rejects normative HTTP 202, and performs only best-effort cleanup.

## Changed Requirement

### REQ-ETS-PART1-011

The ETS SHALL replace the historical subset with one independently executable
TestNG method for each released procedure:

- `/system`;
- `/deployment`;
- `/procedure`;
- `/sampling-feature`; and
- `/property`.

Every procedure SHALL check the Part 1 Update declaration, direct API Common
prerequisite, exact released Annex A
`http://www.opengis.net/spec/ogcapi-4/1.0/conf/update` inheritance declaration,
its resource-class condition, and the explicit dedicated-mutable-IUT gate
before any POST, PATCH, or DELETE. The similarly named
`ogcapi-features-4` URI SHALL NOT satisfy the exact inheritance check. The five
methods SHALL have no sibling-method dependencies and SHALL depend directly on
Part 1 API Common without `alwaysRun`. The released requirements class inherits
Create/Replace/Delete, but the released conformance class does not; resource
creation and deletion are fixture ownership operations, not a TestNG
dependency on the Create/Replace/Delete group.

Each procedure SHALL execute the inherited Update contract at the canonical
resource endpoint and every advertised applicable non-root collection item
endpoint. Collection discovery SHALL use bounded, cycle-safe, same-origin
pagination and actual JSON media gating. Applicability SHALL accept only the
exact compact or canonical SOSA resource type, never an unrelated namespace
that happens to share a local-name suffix. OPTIONS SHALL return HTTP 200 and
advertise PATCH across all received `Allow` header fields.
Because the inherited Features Part 4 draft does not mandate one patch
encoding, the ETS SHALL negotiate a patch document it implements. It SHALL
support JSON Merge Patch and JSON Patch for the JSON representations used by
Part 1. Authoritative negotiation sources are `Accept-Patch` from OPTIONS and
the exact resource path's PATCH `requestBody.content` in the advertised OpenAPI
definition. Every advertised format implemented by the ETS SHALL be exercised.
A declared Update implementation with PATCH in `Allow` but no advertised
implemented patch format SHALL SKIP with a precise no-evidence reason before
PATCH rather than guess that an ordinary resource media type is a patch
document.

PATCH SHALL contain only the intended partial change plus a deliberately
different representation `id`. A successful response SHALL be HTTP 200, 202,
or 204. Positive evidence requires a canonical GET showing the intended field
changed, an unpatched sentinel and stable external identity preserved, and the
submitted different `id` ignored. A status code alone SHALL not PASS. For a
custom collection endpoint, the canonical and collection-item
representations SHALL expose the same completed update in one observation.
Completed evidence for synchronous and queued PATCH SHALL remain jointly true
for two consecutive complete observations; transient or reverting states SHALL
not PASS. Canonical and custom occurrence evidence SHALL compare each
endpoint's untouched sentinel with that endpoint's own pre-update baseline.

HTTP 202 SHALL start one monotonic deadline shared by every required
postcondition. Deadline checks SHALL precede requests, each connect/read
timeout and sleep SHALL be capped to the remaining whole-millisecond budget,
late success SHALL not count, and interruption SHALL fail visibly while
preserving interrupt state.

Every procedure SHALL acquire only schema-valid temporary resources owned by
that procedure. Fixture POST inability is not Update nonconformance: denial or
an unusable response SHALL produce an inconclusive SKIP before PATCH. Because
a failing, disconnected, or timed-out POST may still commit, identity
rediscovery SHALL run after every dispatched POST, including ambiguous
responses. That rediscovery SHALL poll both canonical and applicable custom
collection identity views through a bounded deadline so delayed and route-local
commits are cleaned. Discovery SHALL recognize released GeoJSON collection
members under `features` as well as JSON or SensorML members under `items`.
HTTP 201 without a usable Location, an unsafe Location, or HTTP 202 without a
discoverable owned resource SHALL remain an inconclusive SKIP before PATCH
after bounded identity rediscovery. Cleanup SHALL be registered by submitted
identity before creation, run in reverse ownership order after PASS, FAIL, or
inconclusive SKIP, immediately revalidate current identity before DELETE, and
delete only an identity-verified same-origin resource. Every independently safe
discovery and cleanup route SHALL be attempted even when another route fails,
and failures SHALL be aggregated after all attempts. Every successful DELETE
status SHALL be followed by bounded disappearance proof. Missing, cross-origin,
or mismatched Location metadata SHALL not authorize destructive follow-up.
Cleanup failure SHALL remain visible and SHALL override an
accepted-but-inconclusive SKIP.

## Architecture

`UpdateTests` SHALL retain only immutable API-root and mutation arguments.
`UpdateSupport` SHALL own declarations, conditions, patch-format negotiation,
owned fixture acquisition, canonical and custom endpoint execution, response
handling, changed/unchanged/identity assertions, bounded asynchronous polling,
and cleanup. Shared mutation fixture and schema behavior may be extracted from
`CreateReplaceDeleteSupport` only where the abstraction has the same ownership
and failure contract.

Released execution inheritance is:

```text
Core/Common -> Part 1 API Common -> Update
```

No executable Features, SWE Common, or SensorML conformance-suite jar is
imported as a library. OSH and TeamEngine source code and binaries remain
unchanged, and hosted CI remains out of scope.

## Verification Boundary

Mappings remain candidate until focused and full Docker Maven checks pass,
controlled HTTP executes all five positive procedures and fail-closed
branches, the committed candidate passes released-source, exact-image runtime,
dependency, credential, immutable-base, and artifact-hygiene gates, local OSH
TeamEngine E2E records honest outcomes and zero unauthorized writes, and fresh
Raze review has no unresolved required finding. A local OSH prerequisite SKIP
is valid E2E evidence but does not provide positive PATCH evidence or upgrade
the five mappings to reviewed exact.

Candidate `cbfa070` is superseded by Raze `GAPS_FOUND 0.98`; delayed/custom-only
ambiguous commit cleanup, independent cleanup-route attempts, and
endpoint-specific sentinel baselines remain normative for the next candidate.

Candidate `9e839e1` is superseded by Raze `GAPS_FOUND 0.97`; the replacement
parses GeoJSON `features` and JSON/SensorML `items`, attempts canonical and
custom discovery independently, aggregates unresolved route failures after
all safe cleanup, and treats accepted fixture responses without a usable safe
owned target as inconclusive SKIP-before-PATCH.

Exact detached candidate
`c4b6030b6931863ccda484f2f2d3468cb045d79f` passes Docker Maven
`685/0/0/3`, controlled HTTP `28/0/0/0`, released-source, coverage,
exact-image/runtime, dependency, credential, immutable-base, and hygiene
gates. Local OSH E2E is populated `244/54/35/155` and clean primary
`244/40/7/197`; both runs preserve their genuine IUT failures, all five Update
procedures causal-SKIP through API Common datetime, and 363 recorded IUT
requests are GETs. The mappings remain candidate until positive PATCH executes
against a conforming dedicated mutable IUT.

Candidate `c4b6030` is superseded by final Raze `GAPS_FOUND 0.98`. Canonical
Sampling Feature fixture acquisition SHALL first create and own a parent System
and SHALL POST the Sampling Feature through
`/systems/{systemId}/samplingFeatures`; direct root `/samplingFeatures` POST
support is not required and cannot be a fixture prerequisite. Child cleanup
SHALL precede parent cleanup.

When a custom fixture POST is ambiguous, canonical-first visibility SHALL NOT
end discovery before the custom occurrence view has also been polled. Canonical
and custom identity views SHALL continue independently through the shared
bounded discovery deadline until both are found or the deadline expires.
Afterward, every safely discovered or derived route SHALL still be attempted,
and failures SHALL remain aggregated.

Exact detached replacement
`40cc7039da26a39424f1ffa7626b7b6926a50f0a` reproduces both final-Raze
findings at `2/2/0/0`, then passes corrected focused `2/0/0/0`, complete Update
`30/0/0/0`, and full Docker Maven `687/0/0/3`. Released-source, coverage,
exact-image runtime, deployed SWE Common adapter, dependency sabotage,
credential, TeamEngine immutable-base, and artifact-hygiene gates pass. Image
`sha256:8184ad80b160e0854afcf22d5fa996de835bd886c31930bae150a1bb4cb7ee9d`
runs against unmodified local OSH with populated `244/54/35/155` and clean
primary `244/40/7/197`; provisioning and cleanup pass, primary state is
unchanged, and all 363 IUT requests are GETs. API Common datetime causally
skips all five Update procedures before writes. At gate capture, fresh Raze
had not yet run; positive isolated PATCH evidence remains pending and all
mappings remain candidate.

Fresh Raze returns `APPROVE_WITH_CONCERNS` with high confidence, closes both
prior HIGH findings, and finds no implementation defect. Its sole MEDIUM
concern requires this follow-up to bind the exact evidence to `40cc703` across
the story, contract, handoff, traceability, OpenSpec, architecture, epic, and
operational documents. A focused recheck remains pending.
