# CP-025 - Part 2 JSON Encoding Released ATS Closure

**Status**: Implemented, Raze approved, pending push
**Date**: 2026-08-01
**Capability**: `ets-ogcapi-connectedsystems`
**Requirement**: `REQ-ETS-PART2-009`
**Story**: `S-ETS-65-01`

## Problem

Sprint 28 implemented a useful read-only `/conf/json` subset, but the released
ATS coverage audit still reports OGC 23-002 Annex A.9 as
`0 exact / 13 candidate / 1 unmapped`. The deployed class includes standalone
declaration/prerequisite/resource-gate helper tests, lacks a released
`/conf/json/mediatype-read` procedure, and keeps Observation/Command dynamic
schema checks as evidence guards rather than executable conformance checks.

## Change

Replace the Sprint 28 subset with the fourteen released Annex A.9 procedures:

- `/conf/json/mediatype-read`
- `/conf/json/mediatype-write`
- `/conf/json/datastream-schema`
- `/conf/json/obsschema-schema`
- `/conf/json/observation-schema`
- `/conf/json/observation-constraints`
- `/conf/json/controlstream-schema`
- `/conf/json/commandschema-schema`
- `/conf/json/command-schema`
- `/conf/json/command-constraints`
- `/conf/json/commandstatus-schema`
- `/conf/json/commandresult-schema`
- `/conf/json/commandresult-constraints`
- `/conf/json/systemevent-schema`

Each deployed TestNG method SHALL map to exactly one released ATS target, SHALL
use official OGC 23-002 `/req/json` and `/conf/json` identifiers, and SHALL
remain read-only. Class setup SHALL gate exact `/conf/json` declaration and the
released SWE Common 3.0 JSON record-components prerequisite before any JSON
resource endpoint is queried. Resource-specific procedures SHALL condition on
the relevant Part 2 resource conformance class before endpoint access.

## Verification Plan

- Add structural red tests for exact method count, released target list,
  one-to-one method mapping, no standalone helper tests, and `mediatype-read`.
- Implement the fourteen methods with bounded read-only GET/API-definition
  probes and bundled JSON Schema validation.
- Promote all fourteen mappings to reviewed exact and regenerate
  `ops/ats-coverage-report.json`.
- Run formatter, focused Maven, coverage update/audit, full Docker Maven, and
  mandatory local OSH TeamEngine smoke with no-mutation evidence.
- Run Raze before completion and reconcile all specs/ops artifacts.

## Implementation Status

Implemented on 2026-08-01. `Part2JsonTests` exposes exactly fourteen released
Annex A.9 methods, validates root and nested JSON collections with bounded
same-origin pagination, scopes mediatype-write to Part 2 resource endpoint
templates in service-desc OpenAPI metadata, and emits no mutation requests.

Evidence directory:
`ops/test-results/sprint-ets-65-part2-json-2026-08-01/`.

Current verification after the Raze recheck gapfix, before push:

- Focused corrected: `88/0/0/0`.
- Coverage audit: `23/0/0/0`; Part 2 JSON `14 exact / 0 candidate /
  0 unmapped`.
- Full Docker Maven: `766/0/0/3`.
- Local OSH TeamEngine: honest non-green `258/29/20/209`; all Part 2 JSON
  methods SKIP before JSON resource endpoint access because local OSH lacks
  the SWE JSON record-components prerequisite.
- No-mutation oracle: 151 IUT-bound GETs and zero POST/PUT/PATCH/DELETE.
- Final Raze recheck: `APPROVE 0.96`, both previously open findings closed,
  `required_fixes: []`.

## Non-Goals

- Mutating an IUT to create JSON resources.
- Positive POST/PUT lifecycle behavior for JSON write media support.
- Closing SWE Common JSON/Text/Binary encoding classes.
- Importing or changing external validator dependencies.
