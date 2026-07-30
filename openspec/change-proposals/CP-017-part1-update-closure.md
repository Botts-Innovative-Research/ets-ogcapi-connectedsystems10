# Change Proposal: CP-017 Part 1 Update Direct ATS Closure

**Date**: 2026-07-30
**Author**: Codex
**Affects**:

- `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- `openspec/capabilities/ets-ogcapi-connectedsystems/design.md`
- `REQ-ETS-PART1-011`
- `REQ-ETS-COVERAGE-001`

**Status**: Accepted; candidate `b9143a4` superseded by Raze remediation

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
not PASS.

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
responses. Cleanup SHALL be registered by submitted identity before creation,
run in reverse ownership order after PASS, FAIL, or inconclusive SKIP,
immediately revalidate current identity before DELETE, and delete only an
identity-verified same-origin resource. Every successful DELETE status SHALL be
followed by bounded disappearance proof. Missing, cross-origin, or mismatched
Location metadata SHALL not authorize destructive follow-up. Cleanup failure
SHALL remain visible and SHALL override an accepted-but-inconclusive SKIP.

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
