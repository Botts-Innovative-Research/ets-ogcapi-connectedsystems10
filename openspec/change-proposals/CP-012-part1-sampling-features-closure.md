# Change Proposal: CP-012 Part 1 Sampling Features Direct ATS Closure

**Date**: 2026-07-27
**Author**: Codex
**Affects**:

- `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- `openspec/capabilities/ets-ogcapi-connectedsystems/design.md`
- `REQ-ETS-PART1-007`
- `REQ-ETS-COVERAGE-001`
- `REQ-ETS-VALIDATOR-001`

**Status**: Completed

## Motivation

The released OGC 23-001 `/conf/sf` class contains five procedures:
canonical-URL equivalence, parameterized resources-endpoint validation,
canonical endpoint validation, collection metadata and representation
validation, and nested Sampling Features retrieval from every canonical
System.

The historical Sprint 7 class contains four coupled approximations. It requires
a non-empty canonical collection during setup, checks only one item, treats
path dereferenceability as canonical-link evidence, checks `id` and `type`
instead of the released collection schema, omits `/collections` and
`/systems/{sysId}/samplingFeatures`, and includes a non-normative dependency
tracer. Those methods cannot be promoted to exact mappings.

## Changed Requirement

### REQ-ETS-PART1-007

The ETS SHALL replace the historical methods with one independently executable
TestNG method for each released `/conf/sf` procedure:

- `/canonical-url`;
- `/resources-endpoint`;
- `/canonical-endpoint`;
- `/collections`; and
- `/ref-from-system`.

The resources-endpoint procedure SHALL require HTTP 200, traverse bounded
same-origin pagination, gate every page by its actual response media type
before parsing, and validate every supported GeoJSON page against the bundled
released Sampling Feature collection schema. Unsupported actual media SHALL
warn and SKIP; HTTP 404 or invalid supported content SHALL fail.

The canonical-endpoint procedure SHALL independently apply the same procedure
at `{api_root}/samplingFeatures`.

The collections procedure SHALL inspect every advertised collection whose
`featureType` is exactly `sosa:Sample`, require at least one such collection,
require `itemType=feature` and a non-empty collection ID, traverse every
supported items representation through the reviewed API Common helper, and
validate every supported GeoJSON page against the released Sampling Feature
collection schema. If every selected collection lacks a supported
representation, the procedure SHALL warn and SKIP rather than pass.

The canonical-URL procedure SHALL inspect every item from every advertised
`sosa:Sample` collection with a supported JSON items representation. Every item
SHALL contain a canonical link whose resolved same-origin identity is
`{api_root}/samplingFeatures/{encodedId}`. A comparable occurrence SHALL be
dereferenced, return HTTP 200 with media comparable to the collection item,
and contain identical JSON after canonical links are removed from both
resources. Missing collections or unsupported collection representations
SHALL SKIP as evidence limitations; a missing, unsafe, wrong-target, or
content-different canonical link SHALL fail.

The system-reference procedure SHALL retrieve all canonical Systems through
the reviewed API Common helper. For every System ID it SHALL request
`{api_root}/systems/{encodedSysId}/samplingFeatures`, require HTTP 200, and
iterate all pages with bounded same-origin pagination. Every returned
`application/geo+json` page SHALL pass the released Sampling Feature collection
schema. Unsupported actual representation media SHALL be accumulated at the
System boundary so later Systems remain inspectable, then warn and SKIP;
missing IDs, invalid supported GeoJSON, pagination defects, or non-200
responses SHALL fail.

Expected unsupported-media or non-comparable-representation evidence
limitations SHALL be accumulated at the narrow collection, item, or System
boundary. Every independently inspectable later collection, item, and System
SHALL still be processed before an aggregate SKIP. Assertion failures, unsafe
pagination, non-200 responses, invalid metadata or schema, and canonical
identity or content failures SHALL NOT be caught or downgraded.

## Architecture

`SamplingFeaturesTests` SHALL retain only immutable API-root setup. Every
released procedure SHALL retrieve its own evidence, use `alwaysRun`, and have
no method dependency. `SamplingFeaturesSupport` SHALL own exact collection
selection and metadata, GeoJSON schema dispatch, canonical link identity and
content normalization, and Sampling Feature ID extraction.

Released inheritance remains explicit:

```text
Core/Common -> Part 1 API Common -> System -> Sampling Features
```

The defensive setup gate SHALL inspect only Core, Common, Part 1 API Common,
and System outcomes. Unrelated sibling classes SHALL NOT block Sampling
Features.

The bundled GeoJSON Sampling Feature schema remains behind an ETS-owned
validator boundary. Sampling Features have no SensorML representation in
OGC 23-001, so no SensorML validator dependency enters this increment. No OSH
or TeamEngine source code or binary SHALL be modified.

## Acceptance Boundary

Sprint 52 closes only when all five procedures have reviewed exact mappings,
focused and full Maven verification complete, TeamEngine deploys every changed
method against the unmodified local OSH with honest PASS/FAIL/SKIP outcomes,
controlled read-only HTTP coverage proves every positive path and key
fail-closed branches, System dependency sabotage proves pre-IUT cascade
behavior, exact-image runtime and credential/artifact-hygiene gates complete,
and Raze reports no unresolved required findings.

## Adversarial Remediation

Initial Raze review returned `GAPS_FOUND` at confidence `0.98`.
`RAZE-S52-001` required conditional schema validation for every nested
GeoJSON page in `/ref-from-system`. `RAZE-S52-002` required expected media
limitations to accumulate without hiding later collection, item, page, or
System defects.

The remediated architecture adds reviewed page-observer overloads to
`Part1ApiCommonSupport`. Each safely parsed supported page is observed before
pagination advances. Sampling Features procedures use those observers for
endpoint, collection, canonical, and nested-System schema checks. Expected
media or comparability SKIPs are caught only at narrow independent boundaries
and emitted in aggregate after all later inspectable candidates complete.
Assertion, status, pagination, metadata, schema, and canonical defects remain
failures.

Gap-fix regressions first failed `13/5/0/0` and `16/3/0/0`. The corrected
focused run is `49/0/0/0`, full Docker Maven is `506/0/0/3`, and exact image
`sha256:ae3a7b6b17d98c328ca7dff95afa05fbfecf6a2f1ebe313a75c7429ae2580ff3`
passes runtime and unmodified local OSH E2E gates. Focused Raze recheck returned
`APPROVE` at confidence `0.99`; both findings are closed, with no new findings
and no required fixes.
