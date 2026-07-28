# Change Proposal: CP-013 Part 1 Property Definitions Direct ATS Closure

**Date**: 2026-07-28
**Author**: Codex
**Affects**:

- `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- `openspec/capabilities/ets-ogcapi-connectedsystems/design.md`
- `REQ-ETS-PART1-008`
- `REQ-ETS-COVERAGE-001`
- `REQ-ETS-VALIDATOR-001`

**Status**: Implemented

## Motivation

The released OGC 23-001 `/conf/property` class contains four procedures:
canonical-URL equivalence, parameterized resources-endpoint validation,
canonical endpoint validation, and Property collection validation.

The historical Sprint 7 class also has four methods, but they are not the four
released procedures. It eagerly caches `/properties`, inspects at most one
item, treats a path dereference as canonical-link evidence, checks generic
`id`/`type` shape instead of the released SensorML Property schema, omits the
released collections procedure, and includes a non-normative dependency
tracer. It also depends on System even though the released class inherits API
Common directly. Those methods cannot be promoted to exact mappings.

## Changed Requirement

### REQ-ETS-PART1-008

The ETS SHALL replace the historical methods with one independently executable
TestNG method for each released `/conf/property` procedure:

- `/canonical-url`;
- `/resources-endpoint`;
- `/canonical-endpoint`; and
- `/collections`.

The resources-endpoint procedure SHALL require HTTP 200, traverse bounded
same-origin pagination, establish actual response media before parsing each
page, and validate every supported `application/sml+json` page against the
bundled released SensorML Property collection schema. Unsupported actual media
SHALL warn and SKIP; HTTP 404 and invalid supported content SHALL fail.
The released Annex A source uses the undefined `{sensorml-mediatype}` token in
this procedure and `/collections`; Sprint 53 interprets it as the release's
defined `{sensorml-json-mediatype}` value, `application/sml+json`.

The canonical-endpoint procedure SHALL independently apply the same procedure
at `{api_root}/properties`.

The collections procedure SHALL inspect every collection whose `itemType` is
exactly `sosa:Property`, require at least one such collection and a non-empty
collection ID, retrieve each items endpoint through the reviewed API Common
helper, and validate every supported SensorML page against the released
Property collection schema. Expected unsupported-media limitations SHALL be
retained while later collections remain inspectable, followed by one aggregate
SKIP. Metadata, HTTP, pagination, and schema defects SHALL remain failures.

The canonical-URL procedure SHALL inspect every item from every advertised
`sosa:Property` collection with a supported SensorML items representation.
Every item SHALL include a canonical relation resolving on the IUT origin
under `{api_root}/properties/{id}`. A comparable canonical occurrence SHALL
return HTTP 200 and contain equal JSON content after canonical links are
removed from both resources. Missing collections, empty item evidence, or
unsupported representations SHALL SKIP after all independently inspectable
evidence is processed. Missing, unsafe, wrong-target, or content-different
canonical links SHALL fail.

Expected unsupported-media or non-comparable-representation evidence
limitations SHALL be caught only at the narrow collection or item boundary.
Every independently inspectable later collection and item SHALL still be
processed before one aggregate SKIP. Assertion failures, unsafe pagination,
non-200 responses, invalid schema or metadata, and canonical identity or
content failures SHALL NOT be caught or downgraded.

## Architecture

`PropertyDefinitionsTests` SHALL retain only immutable API-root setup. Every
released procedure SHALL retrieve its own evidence, use `alwaysRun`, and have
no method dependency. Released inheritance is:

```text
Core/Common -> Part 1 API Common -> Property Definitions
```

The defensive setup gate SHALL inspect only Core, Common, and Part 1 API Common
outcomes. System and unrelated sibling classes SHALL NOT block Property
Definitions.

`PropertyDefinitionsSupport` SHALL own exact collection selection, SensorML
Property schema dispatch, canonical identity, representation selection, and
canonical-link normalization. The support class is the ETS-owned validator
adapter boundary described by `REQ-ETS-VALIDATOR-001`: current bundled schema
validation remains replaceable by FCU-GIS-Luke's future reusable SensorML
library without exposing its types to TestNG procedures. The
`ets-sensorml30` executable suite jar SHALL NOT be imported as a library.

The bundled Property schema graph is resolver-normalized rather than
byte-identical to the pinned release: local `$id` values are added and relative
`$ref` values are rewritten to equivalent absolute local resolver URIs.
Sprint 53 exact status therefore requires a pinned-source semantic and
transitive-reference parity gate for `property.json`, `propertyArray.json`,
and `propertyCollection.json`. Reusing Property schema validation here closes
only the schema-validation steps referenced by the four `/conf/property`
procedures; it SHALL NOT be represented as an exact mapping or closure of the
separate `/conf/sensorml/property-schema` procedure.

No OSH or TeamEngine source code or binary SHALL be modified. Project-operated
hosted CI remains out of scope.

## Acceptance Boundary

Sprint 53 closes only when all four procedures have reviewed exact mappings,
focused and full Maven verification complete, TeamEngine deploys every changed
method against the unmodified local OSH with honest PASS/FAIL/SKIP outcomes,
controlled read-only HTTP coverage proves every positive path and key
fail-closed branches, API Common dependency sabotage proves pre-IUT cascade
behavior, exact-image runtime and credential/artifact-hygiene gates complete,
pinned-source normalized Property-schema parity completes, and Raze reports no
unresolved required findings.
