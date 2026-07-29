# Change Proposal: CP-016 Part 1 Create/Replace/Delete Direct ATS Closure

**Date**: 2026-07-29
**Author**: Codex
**Affects**:

- `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- `openspec/capabilities/ets-ogcapi-connectedsystems/design.md`
- `REQ-ETS-PART1-010`
- `REQ-ETS-COVERAGE-001`

**Status**: Accepted; implementation pending

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
Common prerequisite, inherited OGC API Features Part 4 Create/Replace/Delete
declaration, applicable resource-class condition, and explicit mutation safety
gate before issuing POST, PUT, or DELETE. The gate requires
`mutation-tests-enabled=true` and
`mutation-iut-policy=dedicated-mutable-iut`; known shared public GeoRobotix
targets remain hard denied. Each procedure SHALL use `alwaysRun`, acquire only
its own mutable resources, and clean up resources it created.

The reusable transaction procedure SHALL execute the inherited Features Part 4
contract at each prescribed resources and resource endpoint: OPTIONS returns
HTTP 200 with the applicable method in `Allow`; POST sends a supported
representation and returns HTTP 201 with a usable `Location`; canonical GET
returns HTTP 200 and preserves the submitted representation content; PUT sends
a complete replacement with the same resource identity and returns HTTP 200 or
204; a subsequent GET proves replacement content; DELETE returns HTTP 200,
202, or 204; and a completed synchronous deletion makes the canonical resource
unavailable. A response status alone SHALL NOT establish representation,
replacement, or deletion behavior. The procedure SHALL exercise each
applicable representation declared by the IUT and implemented by the resource
class.

The cascade procedure SHALL create its own dependency graphs. For a System
with nested resources, `DELETE ?cascade=false` SHALL return HTTP 409 and leave
the graph intact; `DELETE ?cascade=true` SHALL remove the System and its nested
resources. For a System referenced by a Deployment that also references
another System, the false request SHALL return 409, the true request SHALL
delete only the target System, and the surviving Deployment SHALL remain while
no longer referencing the deleted System.

Subsystem and subdeployment procedures SHALL create through their prescribed
nested endpoints and verify the created resource at its canonical URL with
equivalent submitted content. Sampling Feature creation SHALL use the
System-scoped endpoint and replacement/deletion SHALL use the canonical
Sampling Feature endpoint.

Custom-collection procedures SHALL inspect all advertised collections for the
five released resource types: System, Procedure, Deployment, Sampling Feature,
and Property. Create through a collection SHALL appear at the canonical root.
Replace through a collection SHALL change the canonical representation. Delete
from a root collection SHALL remove every collection occurrence; delete from a
non-root collection SHALL leave the canonical resource. Adding existing
resources SHALL POST `text/uri-list`, one same-IUT canonical URL or UID per
line, and verify equivalent representations through collection-item URLs. If
the IUT exposes no applicable custom collection, the affected procedure SHALL
SKIP with a precise no-evidence reason; malformed advertised endpoints,
unsupported declared methods, or incorrect propagation SHALL fail.

Cleanup SHALL run in reverse ownership order after both pass and failure. A
cleanup failure SHALL remain visible and SHALL not be hidden by an earlier
assertion. Credentials SHALL never be sent cross-origin.

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

Sprint 56 closes only when all twelve procedures have reviewed exact mappings;
focused and full Docker Maven checks pass; controlled HTTP executes every
positive procedure and key fail-closed branch; the exact committed candidate
passes TeamEngine runtime, dependency, credential, immutable-base, and
artifact-hygiene gates; default primary local OSH smoke records zero writes;
an owned isolated local OSH run executes the mutation paths and records honest
conformance outcomes, cleanup, primary-state immutability, and a subsequent
clean-primary smoke; and fresh Raze review has no unresolved required finding.

