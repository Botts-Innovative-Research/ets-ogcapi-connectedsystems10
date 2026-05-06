# Story S-ETS-16-01: SensorML Non-System Read-Only Expansion

> Sprint: ets-16
> Status: PLANNED
> Priority: P0
> Complexity: M
> Epic: epic-ets-02-part1-classes
> OpenSpec: REQ-ETS-PART1-013

## User Value

As an OGC API Connected Systems server implementer, I need SensorML encoding checks to cover deployment, procedure, and property resources, while the ETS stays honest when an IUT declares `/conf/sensorml` but returns default CS API JSON or has no property definitions to inspect.

## Scope

Extend the Sprint 10 `/conf/sensorml` systems subset. This sprint does not add a new Part 1 conformance class. It adds read-only non-system SensorML schema and mapping coverage for:

1. `/deployments`
2. `/procedures`
3. `/properties`

The sprint remains PARTIAL for `REQ-ETS-PART1-013`.

## Requirements

- REQ-ETS-PART1-013
- SCENARIO-ETS-PART1-013-SENSORML-DEPLOYMENT-SCHEMA-MAPPING-001
- SCENARIO-ETS-PART1-013-SENSORML-PROCEDURE-SCHEMA-MAPPING-001
- SCENARIO-ETS-PART1-013-SENSORML-PROPERTY-SCHEMA-MAPPING-001
- SCENARIO-ETS-PART1-013-SENSORML-NON-SYSTEM-FALLBACK-HONESTY-001
- SCENARIO-ETS-PART1-013-SENSORML-SMOKE-NO-MUTATION-001

## Planning Evidence

- Architecture freshness check: `_bmad/architecture.md` last reconciled 2026-04-28, checked 2026-05-06, not stale.
- OGC upstream source: `api/part1/standard/requirements/encoding/sensorml/requirements_class_sensorml.adoc`, fetched HTTP 200 on 2026-05-06.
- Requirement class identifier: `/req/sensorml`.
- Relevant subrequirements: `/req/sensorml/deployment-schema`, `/req/sensorml/deployment-mappings`, `/req/sensorml/procedure-schema`, `/req/sensorml/procedure-sml-class`, `/req/sensorml/procedure-mappings`, `/req/sensorml/property-schema`, and `/req/sensorml/property-mappings`.
- GeoRobotix `/conformance` returned HTTP 200 and declares `/conf/sensorml`, `/conf/deployment`, `/conf/procedure`, and `/conf/property`.
- GeoRobotix `GET /deployments?limit=1` with `Accept: application/sml+json` returned HTTP 200, `Content-Type: application/json`, and top-level `items`.
- GeoRobotix `GET /procedures?limit=1` with `Accept: application/sml+json` returned HTTP 200, `Content-Type: application/json`, and top-level `items` plus `links`.
- GeoRobotix `GET /properties?limit=1` with `Accept: application/sml+json` returned HTTP 200, `Content-Type: application/json`, top-level `items`, and no current items.
- GeoRobotix `GET /deployments/16sp744ch58g?f=sml3` returned SensorML JSON with `type=Deployment`, matching `id`, `uniqueId`, and `deployedSystems`.
- GeoRobotix `GET /procedures/164p7ed8l47g?f=sml3` returned SensorML JSON with `type=PhysicalSystem`, matching `id`, `uniqueId`, and procedure structure.
- Sampling features are intentionally out of scope for this sprint: upstream `/req/sensorml` lists property schema/mapping subrequirements, not sampling feature SensorML subrequirements.

## Planned Test Surface

1. Add read-only checks that discover one deployment, one procedure, and, when present, one property resource.
2. Gate each resource check on its matching resource conformance class: deployment requires `/conf/deployment`, procedure requires `/conf/procedure`, and property requires `/conf/property`. If the matching resource class is absent, SKIP before fetching or judging resource-specific SensorML evidence.
3. Fetch SensorML JSON through direct `Accept: application/sml+json`, explicit `alternate` links, or `?f=sml3` links discovered from the IUT.
4. For each resource, require parseable SensorML JSON before any schema/mapping PASS is possible.
5. If the response is a CS API default `items` wrapper or a default Feature JSON item, SKIP with a requirement-cited fallback reason rather than PASS.
6. Add resource-specific schema/mapping predicates before claiming PASS:
   - Deployment: `type=Deployment`, identity mapping, and non-empty `deployedSystems`.
   - Procedure: SensorML procedure-compatible type, identity mapping, and at least one non-identity process/procedure structure such as `definition`, `inputs`, `outputs`, `parameters`, `characteristics`, or `capabilities`; `identifiers` alone is not enough.
   - Property: empty `/properties` SKIPs honestly; when present, require SensorML property-compatible type plus identity/definition mapping.
7. Preserve the existing `sensorml -> systemfeatures -> core` dependency wiring.
8. Keep default GeoRobotix smoke non-mutating.

## Definition of Done

- [ ] `SensorMlTests` covers deployment, procedure, and property read-only SensorML schema/mapping paths.
- [ ] New tests include comments or descriptions referencing `REQ-ETS-PART1-013` and the matching `SCENARIO-*` IDs.
- [ ] Deployment/procedure/property SensorML checks SKIP before judgment when the matching resource conformance class is absent.
- [ ] CS API `items` wrappers and default Feature JSON do not count as SensorML PASS evidence.
- [ ] Empty `/properties` SKIPs honestly rather than failing or passing vacuously.
- [ ] Generic JSON identity shape alone does not close deployment/procedure/property schema and mapping predicates.
- [ ] No POST, PUT, PATCH, or DELETE is issued by default smoke.
- [ ] Docker Maven and TeamEngine smoke are run and recorded.
- [ ] OpenSpec, story, traceability, status, changelog, and test-results are reconciled.
- [ ] Raze reviews Sprint 16 planning changes.
- [ ] Raze reviews implementation changes before completion.

## Out Of Scope

- `/req/sensorml/mediatype-write`
- `/req/sensorml/relation-types`
- Full external SensorML 3.0 JSON Schema validation
- Sampling Feature SensorML claims
- GeoJSON work
- Part 2 work
- Any mutation request
