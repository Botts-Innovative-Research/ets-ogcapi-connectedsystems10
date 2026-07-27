# Change Proposal: CP-010 Part 1 Procedure Direct ATS Closure

**Date**: 2026-07-26
**Author**: Codex
**Affects**:

- `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- `openspec/capabilities/ets-ogcapi-connectedsystems/design.md`
- `REQ-ETS-PART1-006`
- `REQ-ETS-COVERAGE-001`
- `REQ-ETS-VALIDATOR-001`

**Status**: Accepted

## Motivation

The released OGC 23-001 `/conf/procedure` class contains five procedures:
location absence, canonical URL equivalence, a parameterized resources endpoint
validator, the canonical endpoint specialization, and advertised Procedure
collections.

The historical four-method class tests a narrower surface. It eagerly retrieves
one generic JSON collection, checks a representative item shape, checks only
canonical-link presence, has no collections procedure, and is hidden behind the
System ATS even though the released class inherits API Common directly. These
methods cannot be promoted to exact mappings.

## Changed Requirement

### REQ-ETS-PART1-006

The ETS SHALL replace the historical methods with one independently executable
TestNG method for each released `/conf/procedure` test:

- `/location`;
- `/canonical-url`;
- `/resources-endpoint`;
- `/canonical-endpoint`; and
- `/collections`.

The location procedure SHALL retrieve every page at `{api_root}/procedures`.
HTTP status and actual response media SHALL be established before parsing each
page. GeoJSON Procedure items SHALL have a null `geometry`; SensorML Procedure
items SHALL not contain `position`. Unsupported or absent actual media SHALL
warn and SKIP before representation parsing.

At least one collection with `featureType=sosa:Procedure` SHALL be advertised.
Every selected collection SHALL use `itemType=feature`, and every item on every
page SHALL be processed. Collection retrieval SHALL prefer advertised GeoJSON
or SensorML over an earlier generic JSON items link where schema-controlled
validation is required.

Every canonical link occurrence SHALL resolve on the IUT origin to
`{api_root}/procedures/{id}`. Representation query variants and duplicate
occurrences MAY remain only when every occurrence resolves to that exact
Procedure identity. The first occurrence whose advertised media type is absent
or equals the collection page media type SHALL be dereferenced, return HTTP
200, and produce content equal to the collection item after canonical links
are removed from both documents. If no occurrence is representation-comparable,
the procedure SHALL warn and SKIP. A `links` member emptied by canonical-link
removal SHALL be equivalent to an omitted optional `links` member in the
canonical response.

The parameterized resources procedure SHALL validate every page using the
released GeoJSON or SensorML Procedure collection schema selected from actual
`Content-Type`. The canonical endpoint procedure SHALL invoke that behavior at
`{api_root}/procedures`.

The collections procedure SHALL validate each item's representation-specific
Procedure type. GeoJSON uses `properties.featureType`; SensorML uses
`definition`. The value SHALL be one of the nine URI/CURIE pairs in OGC 23-001
Clause 12's Procedure Types table. Every selected page SHALL also satisfy the
released Procedure collection schema for its actual media type.

## Architecture

`ProceduresTests` retains only immutable API-root setup. Each released method
retrieves its own prerequisites and uses no method dependency, so missing
collection evidence or unsupported media in one procedure cannot suppress
another.

`ProcedureFeaturesSupport` owns Procedure collection selection, metadata and
type checks, media-specific no-location checks, canonical equivalence, and
Procedure schema dispatch. It is the local adapter boundary for future reusable
SensorML validation. Protocol discovery, pagination, TestNG verdict policy,
canonical comparison, and Connected Systems mapping remain ETS-owned. The ETS
SHALL NOT import `ets-sensorml30`.

The Procedure group depends directly on released Part 1 API Common, not the
System ATS. Defensive result scanning is limited to inherited Core, Common, and
Part 1 API Common tests and configurations. SystemFeatures and sibling outcomes
cannot become implicit blockers.

No OSH or TeamEngine source or binary is modified. The unmodified local OSH
currently returns generic `application/json` from `/procedures` and advertises
no `sosa:Procedure` collection. TeamEngine E2E SHALL preserve those genuine
SKIP/FAIL outcomes. A controlled read-only fixture SHALL execute all five
successful paths.

## Acceptance Boundary

Sprint 50 closes only when all five procedures have reviewed exact mappings,
focused and full Maven verification complete, TeamEngine executes all five
against the unmodified local OSH with honest verdicts, controlled HTTP coverage
proves every positive path and fail-closed case, exact-image runtime and
dependency/credential gates complete, and Raze has no unresolved required
findings.
