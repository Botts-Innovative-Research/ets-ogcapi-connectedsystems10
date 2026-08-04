# Change Proposal: CP-018 Part 1 SensorML Direct ATS Closure

**Date**: 2026-07-30
**Author**: Codex
**Affects**:

- `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- `openspec/capabilities/ets-ogcapi-connectedsystems/design.md`
- `REQ-ETS-PART1-013`
- `REQ-ETS-VALIDATOR-001`
- `REQ-ETS-COVERAGE-001`

**Status**: Implemented and gate-closed

## Motivation

The released OGC 23-001 `/conf/sensorml` class contains fifteen procedures.
The historical thirteen-method class combines procedures, includes declaration
and dependency-tracer methods that are not released ATS tests, depends on
System Features instead of direct API Common, mutates the IUT for a procedure
that only inspects API metadata, validates minimal shapes instead of released
schemas, inspects selected resources instead of complete available evidence,
and leaves three released procedures unmapped.

No public reusable SensorML validator module is available from FCU-GIS-Luke.
The public `opengeospatial/ets-sensorml30` artifact is an executable TeamEngine
suite, not a validator library, and SHALL NOT be imported. The already pinned
released SensorML schema graph is sufficient for a first-party local backend
behind the adapter boundary requested by `REQ-ETS-VALIDATOR-001`.

## Changed Requirement

### REQ-ETS-PART1-013

The ETS SHALL expose exactly one independently executable TestNG method for
each released procedure:

- `mediatype-read`;
- `mediatype-write`;
- `relation-types`;
- `resource-id`;
- `feature-attribute-mapping`;
- `system-schema`;
- `system-sml-class`;
- `system-mappings`;
- `deployment-schema`;
- `deployment-mappings`;
- `procedure-schema`;
- `procedure-sml-class`;
- `procedure-mappings`;
- `property-schema`; and
- `property-mappings`.

The direct TestNG prerequisite SHALL be Part 1 API Common. The methods SHALL
have no sibling-method dependencies and SHALL remain independently executable
when the known API Common datetime evidence limitation is the only inherited
SKIP. A failed Core, Common, or API Common prerequisite SHALL skip all fifteen
before SensorML-specific IUT access.

Media procedures SHALL inspect a JSON or YAML OpenAPI service description.
Read advertisement SHALL cover every declared canonical SensorML resource
collection and the generic custom-collection items operation when custom
collections are advertised. Write advertisement SHALL require SensorML request
content on at least one canonical POST or PUT operation. Neither procedure
SHALL issue mutation requests.

Schema procedures SHALL establish HTTP 200 and actual
`application/sml+json` before parsing, validate complete canonical collection
and single-resource documents through the ETS-owned SensorML adapter, and
remain independent for System, Deployment, Procedure, and Property. Empty
collections are reasoned evidence limitations. Unsupported actual media,
invalid supported content, schema failures, unsafe pagination, and HTTP errors
are failures.

Manual-inspection procedures SHALL automate the released tables over every
available canonical SensorML resource. Present common attributes require an
absolute URI `uniqueId` and string-valued `label` and `description`. Canonical
single-resource `id` SHALL equal the selected canonical URL identifier.
Resource mappings SHALL validate every present released attribute and
association without making optional absent members mandatory. `links`
associations SHALL use exact `ogc-rel:<association>` resource-specific relation
names, excluding only generic links. System and Procedure SensorML classes SHALL be compatible with
the released asset/procedure semantics, and Procedure descriptions SHALL not
contain `position`.

## Architecture

`SensorMlTests` SHALL retain only the immutable API root and implement the
fifteen released procedures. `SensorMlSupport` SHALL own OpenAPI inspection,
resource typing, mapping tables, exact relation vocabularies, URI/CURIE and
temporal checks, and class compatibility.

`validation.sensorml.ConnectedSystemsSensorMlValidatorAdapter` SHALL own domain
schema validation. Its public API SHALL accept a Jackson `JsonNode` and a
closed schema target enum and return immutable, deterministic diagnostics
without TestNG, `ETSAssert`, requirement URIs, or verdict policy. Operational
schema/configuration failures SHALL propagate separately from IUT validation
diagnostics.

The first-party backend SHALL use NetworkNT Draft 2020-12 validation with
format assertions against the bundled released SensorML single/collection
schema graph. This replaces duplicated homegrown shape/schema checks now. A
future reproducible FCU/OGC validator may replace only the backend after
diagnostic and parity review; the TestNG procedures and Connected
Systems-specific mappings remain ETS-owned.

The eight SensorML entry schemas and their transitive graph SHALL pass
resolver-normalized semantic parity against release tag `v1.0.0`, commit
`8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`. The parity gate SHALL reject a
dirty or wrong-commit source checkout.

No OSH or TeamEngine source or binary change, executable SensorML suite-jar
dependency, hosted CI, or default IUT mutation is permitted.

## Verification Boundary

Mappings remained candidate until requirement-linked unit and controlled-HTTP
tests covered all fifteen positive procedures and key fail-closed branches,
focused and full Docker Maven verification passed, released-source and schema
parity gates passed, the exact image executed through Dockerized TeamEngine
against unmodified local OSH with zero writes, and fresh Raze review had no
unresolved required finding. Candidate `a593953d8d79d977649db3077696148e90ffb44a`
cleared those gates, so all fifteen mappings are now reviewed exact mappings.
