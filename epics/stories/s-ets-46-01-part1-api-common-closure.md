# S-ETS-46-01: Part 1 API Common Direct ATS Closure

## Status

COMPLETE

## User Instruction

Triggered by the accepted recommendation to complete every released Part 1
gap continuously after establishing the released Annex A inventory.

## Scope

Implement and review-map all directly owned released OGC 23-001
`/conf/api-common` procedures and the two supporting tests. Keep the five
inherited external conformance classes explicitly partial.

- Requirements:
  - `REQ-ETS-PART1-001`
  - `REQ-ETS-COVERAGE-001`
- Scenarios:
  - `SCENARIO-ETS-PART1-001-CANONICAL-RESOURCES-001`
  - `SCENARIO-ETS-PART1-001-COLLECTION-ITEMS-001`
  - `SCENARIO-ETS-PART1-001-RESOURCE-IDS-001`
  - `SCENARIO-ETS-PART1-001-RESOURCE-UIDS-001`
  - `SCENARIO-ETS-PART1-001-RESOURCE-UID-TYPES-001`
  - `SCENARIO-ETS-PART1-001-DATETIME-001`
  - `SCENARIO-ETS-PART1-001-PAGINATION-FAIL-CLOSED-001`
  - `SCENARIO-ETS-PART1-001-DEPENDENCY-CASCADE-001`

## Acceptance Criteria

- [x] CP-006, this story, the capability spec, design, architecture, and
  traceability define the increment before implementation.
- [x] Canonical-resource retrieval covers every supported Part 1 resource type,
  negotiates GeoJSON/SensorML JSON, validates `features`/`items`, and follows
  bounded `next` links.
- [x] Collection-item retrieval inspects `rel=items` media types, uses the
  released collection-items endpoint contract, and follows bounded `next` links.
- [x] Resource IDs are checked for uniqueness within each resource type.
- [x] Resource UIDs are required, valid absolute URIs, and globally unique
  across retrieved Part 1 resources using GeoJSON, SensorML, or extension
  member mappings.
- [x] SensorML `uniqueId` has precedence over GeoJSON `properties.uid`, which
  has precedence over direct extension `uid`.
- [x] Non-recommended UID forms produce a TestNG warning without turning the
  recommendation into a failure.
- [x] Date-time filtering covers instant, bounded, open-start, and open-end
  queries, supports request-time `now`, rejects non-intersecting `validTime`,
  and retains every timeless feature.
- [x] No usable advertised temporal extent produces an explicit SKIP, not PASS.
- [x] `part1apicommon` depends on `core common`; `systemfeatures` and all of its
  current descendants inherit the prerequisite transitively.
- [x] A failed or skipped Core/Common prerequisite makes API Common setup SKIP
  before it reads the IUT or issues any request.
- [x] Sabotage consumes only a single fresh report from its isolated run output.
- [x] The two helpers and four TestNG methods have reviewed one-to-one mappings.
- [x] Focused/full Maven, coverage audits, real local OSH TeamEngine E2E, and
  exact-image runtime verification are archived.
- [x] The credential integration gate runs through Docker without host Maven
  and accepts only a non-zero, fully green targeted test result.
- [x] Raze reviews the completed increment and all required findings are closed.
- [x] Full `/conf/api-common` conformance remains unclaimed until all five
  inherited external suites are complete.

## Non-Goals

- Completing any other released Part 1 class in this increment.
- Treating the local OSH fixture as normative or weakening tests for its shape.
- Modifying OSH or TeamEngine source code or binaries.
- Adding hosted CI.

## Verification

- Docker Maven: `373 total / 0 failures / 0 errors / 3 skipped`.
- Released ATS coverage: `240 total / 4 exact / 2 helper / 150 candidate /
  84 unmapped`.
- Exact image:
  `sha256:81e16f442d0733c440e1880440ca4b3f6261324e64a42db6ec99e9250ffe0dd7`.
- Primary local OSH TeamEngine: `215 total / 35 passed / 0 failed / 180
  skipped`; the three executable UID/ID procedures pass, date-time skips
  because the fixture advertises no usable temporal extent, and four
  non-recommended fixture UIDs are warnings.
- Dependency sabotage: Core `FAIL=6`, API Common setup `SKIP=1`, API Common
  tests `SKIP=4`, SystemFeatures `SKIP=6`.
- Credential integration and wire gates pass with zero unmasked credential
  hits in test artifacts.
- Final Raze verdict: `APPROVE`, confidence `0.99`, no required findings.

Full evidence is recorded in
`ops/test-results/sprint-ets-46-part1-api-common-verification-2026-07-26.md`.
