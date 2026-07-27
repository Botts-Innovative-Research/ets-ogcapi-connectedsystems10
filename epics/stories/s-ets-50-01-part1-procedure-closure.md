# S-ETS-50-01: Part 1 Procedure Direct ATS Closure

## Status

DONE

## User Instruction

Triggered by the accepted recommendation to complete released Part 1 gaps
continuously after Deployment.

## Scope

Replace the historical Procedure approximations with exact implementations of
all five released OGC 23-001 `/conf/procedure` procedures.

- Requirements: `REQ-ETS-PART1-006`, `REQ-ETS-COVERAGE-001`,
  `REQ-ETS-VALIDATOR-001`
- Scenarios:
  - `SCENARIO-ETS-PART1-006-RELEASED-LOCATION-001`
  - `SCENARIO-ETS-PART1-006-RELEASED-MEDIA-GATE-001`
  - `SCENARIO-ETS-PART1-006-RELEASED-CANONICAL-URL-001`
  - `SCENARIO-ETS-PART1-006-RELEASED-RESOURCES-ENDPOINT-001`
  - `SCENARIO-ETS-PART1-006-RELEASED-CANONICAL-ENDPOINT-001`
  - `SCENARIO-ETS-PART1-006-RELEASED-COLLECTIONS-001`
  - `SCENARIO-ETS-PART1-006-RELEASED-COLLECTION-COMPLETE-001`
  - `SCENARIO-ETS-PART1-006-RELEASED-CANONICAL-EQUIVALENCE-001`
  - `SCENARIO-ETS-PART1-006-RELEASED-PROCEDURE-TYPE-001`
  - `SCENARIO-ETS-PART1-006-RELEASED-PROCEDURE-ISOLATION-001`
  - `SCENARIO-ETS-PART1-006-RELEASED-DEPENDENCY-CASCADE-001`
  - `SCENARIO-ETS-PART1-006-RELEASED-E2E-EXECUTION-001`
  - `SCENARIO-ETS-PART1-006-RELEASED-DIRECT-HTTP-COVERAGE-001`

## Acceptance Criteria

- [x] CP-010, this story, contract, capability requirement/scenarios, design,
  architecture, traceability, epic, and operations records define the
  increment before code.
- [x] Exactly five deployed methods implement the five released procedures.
- [x] Location traverses every canonical Procedure page and checks GeoJSON
  `geometry=null` or absent SensorML `position` by actual media.
- [x] Resources, canonical, location, and collection endpoints gate every page
  by HTTP status and actual media before representation parsing.
- [x] Every advertised `sosa:Procedure` collection and every item is processed.
- [x] Missing Procedure collection evidence fails instead of passing vacuously.
- [x] Collection metadata requires `itemType=feature`.
- [x] GeoJSON and SensorML pages use the released Procedure collection schemas.
- [x] Every item has one of the nine released Procedure type URI/CURIE values.
- [x] Every canonical link resolves to exact Procedure identity; the first
  representation-comparable occurrence is dereferenced, and content is
  equivalent after canonical-only links are removed or normalized to omitted.
- [x] Each procedure retrieves only its own prerequisites.
- [x] API Common prerequisite failures cascade before Procedure IUT access;
  unrelated and SystemFeatures configurations cannot block Procedure.
- [x] All five methods have reviewed exact ATS mappings.
- [x] Focused/full Maven, coverage audit, local OSH TeamEngine E2E, controlled
  positive HTTP coverage, exact-image runtime, dependency, credential, and
  artifact-hygiene gates are archived.
- [x] Local OSH genuine unsupported-media and missing-collection outcomes remain
  visible and are not repaired through OSH or TeamEngine changes.
- [x] Raze reports no unresolved required findings.

## Baseline

- `/procedures`: HTTP 200 `application/json` for generic, GeoJSON, and SensorML
  Accept values; one Procedure item with `geometry=null`.
- `/collections`: no collection advertises `featureType=sosa:Procedure`.
- Existing TeamEngine: all four historical Procedure methods SKIP through the
  unrelated SystemFeatures dependency before Procedure assertions execute.
- Deployed `/home/nh/docker/osh-core`: clean at `4c87a65`, zero commits ahead,
  and mounted read-only.

## Completion Evidence

Implementation and all non-adversarial gates are complete.

- Coverage: `25 exact / 2 helper / 137 candidate / 76 unmapped`;
  `/conf/procedure` is `5/5 exact`.
- Final focused Maven: `116/0/0/0`; full Maven: `451/0/0/3`.
- Exact image:
  `sha256:6e1beeb598ab4c734f2e2d30e0ecb70d3270af9f9f2d5a1029d1b74259b54d98`;
  TeamEngine runtime and immutable-base verification pass.
- Local OSH TeamEngine: `218/39/5/174`. Procedure setup passes; location,
  resources, and canonical endpoint SKIP on unsupported `application/json`;
  canonical URL and collections FAIL because no `sosa:Procedure` collection
  is advertised.
- API Common sabotage: `218/34/1/183`; Procedure setup and all five methods
  SKIP before Procedure IUT access.
- Credential integration and wire gates pass. Positive and sabotage hygiene
  report zero IUT writes and zero credential leaks.
- OSH remains clean at `4c87a65`, zero commits ahead, and read-only mounted.
- Initial Raze findings on comparable canonical selection, canonical-only
  `links` normalization, and the dependency comment are remediated.
- Focused Raze recheck: `PASS`, confidence `0.99`, duration 189 seconds; all
  three prior findings closed and no required fixes.
