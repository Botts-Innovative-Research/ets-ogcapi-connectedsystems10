# Change Proposal: CP-016 Part 1 Create/Replace/Delete Direct ATS Closure

**Date**: 2026-07-29
**Author**: Codex
**Affects**:

- `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- `openspec/capabilities/ets-ogcapi-connectedsystems/design.md`
- `REQ-ETS-PART1-010`
- `REQ-ETS-COVERAGE-001`

**Status**: Accepted; identity-safe joint-polling remediation passes precommit
gates, replacement exact candidate, fresh Raze, and positive mutation E2E
pending

## Motivation

The released OGC 23-001 `/conf/create-replace-delete` class contains twelve
procedures. The historical six-method class has one candidate mapping and no
reviewed exact mapping. It exercises only a System lifecycle, treats OPTIONS
as separate readiness tests, does not verify canonical representations after
replacement or deletion, and omits cascade, nested resources, Property
resources, and custom collections.

## Changed Requirement

### REQ-ETS-PART1-010

The ETS SHALL replace the historical subset with one independently executable
TestNG method for each released procedure:

- `/system`;
- `/system-delete-cascade`;
- `/subsystem`;
- `/deployment`;
- `/subdeployment`;
- `/procedure`;
- `/sampling-feature`;
- `/property`;
- `/create-in-collection`;
- `/replace-in-collection`;
- `/delete-in-collection`; and
- `/add-to-collection`.

Every procedure SHALL establish the exact Part 1 class declaration, direct API
Common prerequisite, exact released Annex A
`http://www.opengis.net/spec/ogcapi-4/1.0/conf/create-replace-delete`
inheritance declaration, applicable resource-class condition, and explicit
mutation safety gate before issuing POST, PUT, or DELETE. The similarly named
`ogcapi-features-4` URI SHALL NOT satisfy that exact inheritance check. The
gate requires
`mutation-tests-enabled=true` and
`mutation-iut-policy=dedicated-mutable-iut`; known shared public GeoRobotix
targets remain hard denied. The twelve procedures SHALL have no sibling-method
dependencies, but SHALL preserve the causal TestNG dependency on API Common:
neither class setup nor procedure methods may use `alwaysRun` to bypass a
failed prerequisite. Each procedure SHALL acquire only its own mutable
resources and clean up resources it created.

The reusable transaction procedure SHALL execute the inherited Features Part 4
contract at each prescribed resources and resource endpoint: OPTIONS returns
HTTP 200 with the applicable method in `Allow`; POST sends a supported
representation and returns HTTP 201 with a usable `Location` or HTTP 202;
canonical GET returns HTTP 200 and preserves the submitted representation
content; PUT sends a complete replacement with the same resource identity and
returns HTTP 200, 202, or 204; a subsequent GET proves replacement content;
DELETE returns HTTP 200, 202, or 204; and a later GET proves the canonical
resource unavailable. A response status alone SHALL NOT establish
representation, replacement, or deletion behavior. HTTP 202 SHALL start one
monotonic operation deadline shared by every required postcondition. Deadline
checks SHALL precede probes, each HTTP connect/read timeout and sleep SHALL be
capped to the remaining deadline, interruption SHALL fail visibly, and a
postcondition first observed after expiry SHALL not count as positive evidence.
Every first-page, pagination-page, and candidate-resource requester boundary
SHALL recheck that deadline. A remaining sub-millisecond budget SHALL expire
without a request; conversion to an integer HTTP timeout SHALL not round beyond
the remaining whole milliseconds.
The procedure SHALL exercise each applicable representation declared by the
IUT and implemented by the resource class. Every ETS-generated request
representation SHALL validate against its bundled released single-resource
schema before the write is issued.

Generic resource deletion SHALL not send the System-specific `cascade` query
parameter. The helper may send `cascade=true` only for owned System cleanup or
the explicit cascade procedure, and `cascade=false` only for the explicit
conflict assertion. HTTP 202 remains accepted for DELETE because the pinned
Features Part 4 requirement explicitly permits asynchronous deletion; the ETS
still waits for the required completed postcondition.

The cascade procedure SHALL create its own dependency graphs. For a System
with nested resources, `DELETE ?cascade=false` SHALL return HTTP 409 and leave
the graph intact; `DELETE ?cascade=true` SHALL remove the System and its nested
resources. For a System referenced by a Deployment that also references
another System, a pre-delete GET SHALL prove both references are present, the
false request SHALL return 409, the true request SHALL delete only the target
System, and the surviving Deployment SHALL remain while no longer referencing
the deleted System.

Subsystem and subdeployment procedures SHALL create through their prescribed
nested endpoints, derive the root canonical endpoint from the returned local
identifier, and verify the created resource there with equivalent submitted
content. A nested `Location` SHALL NOT substitute for root canonical evidence.
Sampling Feature creation SHALL use the System-scoped endpoint and
replacement/deletion SHALL use the canonical Sampling Feature endpoint.

Custom-collection procedures SHALL inspect all advertised collections for the
five released resource types: System, Procedure, Deployment, Sampling Feature,
and Property. Create through a collection SHALL appear at both the custom item
and canonical root. Replace through a collection SHALL change both
representations. Delete from a root collection SHALL remove every collection
occurrence; delete from a non-root collection SHALL remove that occurrence
while leaving the canonical resource. Adding existing resources SHALL verify
OPTIONS advertises POST, then POST `text/uri-list`, one same-IUT canonical URL
or UID per line, require HTTP 201 and a same-origin usable Location or queued
HTTP 202, and verify equivalent representations through both returned and
computed collection-item URLs. Every required custom/canonical propagation,
cascade, surviving-association, and URI-list occurrence postcondition for one
queued operation SHALL be polled under that operation's single deadline.
Positive compound evidence SHALL require every postcondition to be true in the
same polling observation. A transient replacement, disappearance, or cascade
state that reverts before the remaining postconditions become true SHALL not
PASS.
Queued custom creation used as replace or delete setup SHALL await the custom
occurrence before the later write and SHALL pre-register occurrence cleanup so
late propagation cannot leak an alias.
Identity-safe occurrence cleanup SHALL be registered before URI-list POST so
late materialization after an inconclusive timeout is still removed. Cleanup
SHALL delete a computed or returned occurrence only after bounded GET proves
the submitted identity and content; availability alone or a mismatched direct
Location SHALL leave the item untouched. If the IUT exposes no applicable
custom collection, the affected procedure SHALL SKIP
with a precise no-evidence reason; malformed advertised endpoints, unsupported
declared methods, or incorrect propagation SHALL fail.

For queued `text/uri-list`, a supplied Location within the target
collection-item namespace SHALL be treated as a returned occurrence, verified,
and independently cleaned only after submitted-content proof. A mismatched
direct occurrence Location SHALL never be deleted. A supplied Location outside
that namespace SHALL be
treated as an asynchronous status URI and SHALL not be dereferenced or used as
a destructive cleanup target.

Cleanup SHALL run in reverse ownership order after pass, failure, or
accepted-but-inconclusive SKIP. A cleanup failure SHALL become the reported
failure when the primary outcome is SKIP and SHALL not be hidden by an earlier
assertion. A response Location SHALL become a destructive cleanup target only
after dereference proves the submitted UID or URI identity. Missing,
cross-origin, or incorrect Location metadata SHALL trigger same-origin root
discovery by the submitted identity; cleanup SHALL delete only resources whose
representation proves that identity. Credentials SHALL never be sent
cross-origin.

## Architecture

`CreateReplaceDeleteTests` SHALL retain only immutable API-root and mutation
arguments in setup. `CreateReplaceDeleteSupport` SHALL own declaration and
condition checks, exact OPTIONS handling, supported representation selection,
transaction execution, canonical URI resolution, submitted-content comparison,
custom-collection discovery, graph assertions, and cleanup aggregation.

Released direct inheritance is:

```text
Core/Common -> Part 1 API Common -> Create/Replace/Delete
```

The class SHALL not depend on System or another sibling resource class. Each
procedure applies its own released condition from `/conformance`.

No executable conformance-suite jar is imported as a library. The inherited
Features Part 4 operation semantics are implemented as a reusable helper
inside this ETS because a suite jar is an executable container rather than a
stable library API. No OSH or TeamEngine source code or binary is modified.
Project-operated hosted CI remains out of scope.

## Verification Boundary

Mappings remain candidate until focused and full Docker Maven checks pass;
controlled HTTP executes every positive procedure and key fail-closed branch;
the exact committed candidate passes TeamEngine runtime, dependency,
credential, immutable-base, and artifact-hygiene gates; default primary local
OSH smoke records zero writes; an owned isolated real IUT run executes the
mutation paths and records honest positive conformance outcomes, cleanup,
primary-state immutability, and a subsequent clean-primary smoke; and fresh
Raze review has no unresolved required finding. A local OSH prerequisite SKIP
because it omits the exact inherited URI is honest E2E evidence, but does not
upgrade the twelve mappings to reviewed exact or complete this closure.
