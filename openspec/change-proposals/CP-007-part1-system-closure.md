# Change Proposal: CP-007 Part 1 System Direct ATS Closure

**Date**: 2026-07-26
**Author**: Codex
**Affects**:

- `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- `openspec/capabilities/ets-ogcapi-connectedsystems/design.md`
- `REQ-ETS-PART1-002`
- `REQ-ETS-COVERAGE-001`

**Status**: Accepted by the user's instruction to execute recommendations 1-4

## Motivation

The released OGC 23-001 `/conf/system` class contains six tests. Five historical
methods mention matching target URIs, but several implement different
procedures. Presence of `validTime` or `geometry` does not prove that a changing
system's latest location is updated, and discovery through any landing-page
link does not prove the released collections metadata, type, and schema
procedure.

Sprint 47 replaces those approximations with one reviewed TestNG method per
released abstract test.

## Changed Requirement

### REQ-ETS-PART1-002

The ETS SHALL implement all six released `/conf/system` procedures:

- `/conf/system/location`;
- `/conf/system/location-time`;
- `/conf/system/canonical-url`;
- `/conf/system/resources-endpoint`;
- `/conf/system/canonical-endpoint`; and
- `/conf/system/collections`.

Location is a recommendation. Every canonical System other than assets
classified as `Simulation` or `Process` SHALL be inspected, and a missing
location SHALL emit a warning without producing a conformance failure.

Location-time requires the optional `mobile-system-id` test-run argument. The
argument identifies a mobile System known to change location within the ETS's
30-second bounded polling window. The ETS SHALL retrieve the canonical resource,
wait and poll until its GeoJSON `geometry` or SensorML positional coordinates
change, then compare the two locations. SensorML orientation or reference-frame
metadata changes alone SHALL NOT prove movement. An absent argument SHALL report
SKIP, not PASS.

For every advertised collection whose `featureType` is `sosa:System`, the ETS
SHALL retrieve all pages through the reviewed API Common collection-items
helper. Canonical-URL validation SHALL require an item canonical link,
dereference one unambiguous same-origin canonical URL, require HTTP 200, and
compare the returned resource with the collection item after removing canonical
links from both documents.

The reusable, endpoint-parameterized resources-endpoint procedure SHALL require
HTTP 200 and validate every returned page against the bundled System collection
schema selected by the actual response `Content-Type`: GeoJSON for
`application/geo+json`, SensorML JSON for `application/sml+json`. Unsupported
media SHALL emit a warning and return unsupported evidence to its caller. The
canonical-endpoint procedure SHALL invoke that behavior for
`{api_root}/systems`. Collection procedures SHALL use the same validation
procedure for each retrieved collection endpoint, continue after unsupported
collections, and report SKIP only when no supported collection was executed.

The collections procedure SHALL select every advertised collection whose
`featureType` is exactly `sosa:System`. Every retrieved item SHALL report one of
the five released System type URI/CURIE pairs, using GeoJSON
`properties.featureType` or SensorML `definition`, and every returned page SHALL
pass the corresponding bundled collection schema. The released Annex A
procedure does not independently test collection existence or `itemType`, even
though those are normative parts of its target requirement, so Sprint 47 SHALL
not add stricter steps to the reviewed ATS mapping.

GeoJSON validation SHALL use a pinned, complete copy of every referenced
`geojson.org` schema rather than permissive resolution stubs. Focused tests
SHALL prove malformed collection wrappers, features, and geometries fail
closed. SensorML schema validation SHALL also have positive and negative
focused coverage.

## Architecture

`Part1ApiCommonSupport` becomes a public read-only service for descendant
conformance classes. Its existing reviewed helper signatures remain stable. New
detailed result values expose immutable item lists plus every page's source URI,
actual media type, and parsed JSON document so callers validate the response
that was actually traversed without issuing duplicate requests.

`SystemFeaturesTests` remains in group `systemfeatures` behind
`part1apicommon`, but its setup SHALL only load run arguments. Each released
procedure SHALL retrieve its own prerequisite evidence so a failure in
`/systems` cannot configuration-skip unrelated collection or absent-input
procedures. A new System-specific support component owns representation
extraction, canonical-content normalization, allowed type constants, and
bundled JSON Schema validation. This is local Connected Systems schema
validation, not a substitute for the external SWE Common or SensorML validator
adapters.

No OSH or TeamEngine source code or binary is modified. The local OSH primary
target is expected to SKIP location-time unless a moving-System input is
provided; that SKIP is an explicit evidence limitation.

## Acceptance Boundary

Sprint 47 closes only when all six procedures have reviewed exact mappings,
focused and full Maven verification pass, the changed suite executes through
TeamEngine against the real local OSH IUT, the exact image passes runtime
verification, dependency and credential gates remain green, and Raze has no
unresolved required findings.

## Implementation Outcome

The six released procedures are implemented and review-mapped exactly.
Replacement engineering gates produce coverage
`10 exact / 2 helper / 145 candidate / 83 unmapped`, focused Maven
`46/0/0/0`, released-ATS audit `23/0/0/0`, full Docker Maven `395/0/0/3`,
and exact-image, dependency, and credential PASS results on image
`sha256:101e20653097fea9891ff5fbe1f4c160ae163ca97338cf63cfb5980dd958cf6e`.

The first post-gapfix adversarial re-review blocked closure because the local
OSH run's API Common datetime evidence SKIP caused TestNG to dependency-SKIP
all six changed System procedures. The required remediation was:

- allow direct System procedure execution only when API Common's sole skipped
  result is its documented no-temporal-extent evidence limitation;
- continue to block System execution after any inherited prerequisite failure,
  configuration failure, or other skipped API Common procedure;
- keep the inherited API Common SKIP visible, so the run cannot be represented
  as full `/conf/system` conformance;
- execute all six deployed HTTP paths in a controlled direct regression,
  including a moving coordinate, schema-valid resources, and successful
  canonical dereference; and
- archive focused, ATS-audit, and full Maven output with the replacement E2E
  and runtime evidence before final review.

The remediation is implemented and archived. Primary local OSH TeamEngine
reports `215 total / 38 passed / 0 failed / 177 skipped`. All six System
methods execute: canonical URL, collections, and location recommendation PASS;
canonical endpoint and resources endpoint SKIP because OSH returns the
unsupported `application/json` representation; location-time SKIPs because no
`mobile-system-id` is supplied. A direct HTTP fixture executes successful
positive paths for all six methods, including positional movement and schema
validation. Dependency sabotage proves prerequisite failures still block all
six methods before IUT access. The exact-current System-target sabotage
additionally proves all 13 direct and 2 transitive TestNG dependency descendant
groups entirely SKIP after an injected System failure. Final Raze review
reports no unresolved required findings.
