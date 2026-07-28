# Change Proposal: CP-014 Part 1 GeoJSON Direct ATS Closure

**Date**: 2026-07-28
**Author**: Codex
**Affects**:

- `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- `openspec/capabilities/ets-ogcapi-connectedsystems/design.md`
- `REQ-ETS-PART1-012`
- `REQ-ETS-COVERAGE-001`

**Status**: Approved for implementation

## Motivation

The released OGC 23-001 `/conf/geojson` class contains twelve procedures.
The historical GeoJSON class has thirteen methods, but none is a reviewed
one-to-one implementation of those procedures. It adds non-normative
declaration and dependency tracers, performs a mutation for the write-media
test instead of inspecting the API definition, combines some schema and
mapping procedures, splits one relation-types procedure into four methods,
inspects only selected first items, and depends on System instead of the
released API Common inheritance.

## Changed Requirement

### REQ-ETS-PART1-012

The ETS SHALL replace the historical methods with one independently executable
TestNG method for each released procedure:

- `/mediatype-read`;
- `/mediatype-write`;
- `/relation-types`;
- `/feature-attribute-mapping`;
- `/system-schema`;
- `/system-mappings`;
- `/deployment-schema`;
- `/deployment-mappings`;
- `/procedure-schema`;
- `/procedure-mappings`;
- `/sf-schema`; and
- `/sf-mappings`.

The media procedures SHALL discover a `service-desc` API definition from the
landing page, accept JSON or YAML OpenAPI documents, and inspect operation
metadata without issuing a mutation. Read-media validation SHALL require
`application/geo+json` on successful GET response content for every canonical
feature-resource endpoint required by the resource classes declared by the
IUT and on the custom collections items path when custom collections are
advertised. Write-media validation SHALL require the media type in the request
body of at least one POST or PUT operation on a canonical feature-resource
endpoint. Missing or unreadable API-definition evidence SHALL SKIP; a
parseable definition that omits required advertised media SHALL fail.

Each schema procedure SHALL independently retrieve both the canonical
collection and one canonical single resource with `Accept:
application/geo+json`, establish HTTP status and actual media before parsing,
and validate the complete documents against the corresponding released
single and collection schemas. Unsupported actual media or an empty collection
SHALL SKIP without parsing unsupported content. HTTP, schema, identity,
pagination, and supported-content defects SHALL fail.

Manual-inspection procedures SHALL be automated over every inspectable
GeoJSON feature in bounded same-origin canonical collection traversal.
Common-feature mapping SHALL validate `uid`, `name`, and `description` where
the released mapping defines them. Resource mapping SHALL validate each
present attribute and association according to the released System,
Deployment, Procedure, and Sampling Feature tables; optional mapped members
SHALL not be required merely to avoid vacuous evidence. Relation-types SHALL
validate every present association relation against the resource-specific
table, ignore generic links, and SKIP only after all resource types are
inspected when no association relation evidence exists.

Expected unsupported-media or no-resource limitations SHALL be retained at the
narrow resource boundary while later independently inspectable resource types
remain processed. Assertion failures and unsafe or invalid evidence SHALL not
be caught or downgraded.

## Architecture

`GeoJsonTests` SHALL retain only immutable API-root setup. Every released
procedure SHALL retrieve its own evidence, use `alwaysRun`, and have no method
dependency. Released direct inheritance is:

```text
Core/Common -> Part 1 API Common -> GeoJSON
```

The OGC API Features 1 GeoJSON definition step is implemented behind the
ETS-owned OpenAPI inspection boundary; an executable conformance-suite jar is
not imported as a library.

`GeoJsonSupport` SHALL own OpenAPI JSON/YAML inspection, canonical endpoint
selection, actual-media gating, bounded same-origin traversal, schema
dispatch, mapping assertions, and relation tables. The eight released single
and collection entry schemas SHALL pass pinned-source semantic and
transitive-reference parity before exact mappings are promoted.

Sprint 54 also closes both non-blocking Sprint 53 Raze carryovers:

- all pinned-release parity scripts SHALL verify the source checkout is at the
  recorded commit and has a clean worktree before comparison; and
- Property Definitions SHALL have dedicated controlled-HTTP regressions for
  pagination and continuation after a later item or collection limitation.

No OSH or TeamEngine source code or binary SHALL be modified. Project-operated
hosted CI remains out of scope.

## Acceptance Boundary

Sprint 54 closes only when all twelve procedures have reviewed exact mappings,
focused and full Maven verification complete, TeamEngine deploys every changed
method against unmodified local OSH with honest outcomes, controlled read-only
HTTP coverage proves positive and fail-closed branches, API Common dependency
sabotage proves pre-IUT cascade behavior, exact-image runtime and
credential/artifact-hygiene gates complete, schema parity is pinned and
fail-closed, both Sprint 53 carryovers are tested, and Raze reports no
unresolved required findings.
