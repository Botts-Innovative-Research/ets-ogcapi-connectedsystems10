# Change Proposal: CP-006 Part 1 API Common Direct ATS Closure

**Date**: 2026-07-26
**Author**: Codex
**Affects**:

- `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- `openspec/capabilities/ets-ogcapi-connectedsystems/design.md`
- `REQ-ETS-PART1-001`
- `REQ-ETS-COVERAGE-001`

**Status**: Accepted by the user's instruction to execute recommendations 1-4

## Motivation

The released OGC 23-001 API Common conformance class contains four class tests
and two reusable supporting tests. The historical `CommonTests` class exercises
selected inherited OGC API Common requirements, but it does not implement the
released Connected Systems `/conf/api-common` procedures. Sprint 45 therefore
correctly reports all six released tests as unmapped.

API Common is the first Part 1 closure increment because every other released
Part 1 conformance class inherits it. Its retrieval helpers also provide the
bounded pagination behavior needed by later class-specific tests.

## Changed Requirement

### REQ-ETS-PART1-001

The ETS SHALL implement the complete set of directly owned released OGC 23-001
`/conf/api-common` procedures:

- `/conf/api-common/canonical-resources` as a reviewed parameterized helper;
- `/conf/api-common/collection-items` as a reviewed parameterized helper;
- `/conf/api-common/resource-ids` as one exact TestNG test;
- `/conf/api-common/resource-uids` as one exact TestNG test;
- `/conf/api-common/resource-uids-types` as one exact TestNG test; and
- `/conf/api-common/datetime` as one exact TestNG test.

Canonical retrieval SHALL cover every Part 1 resource type supported by the
IUT, negotiate the released GeoJSON and SensorML JSON representations, parse
GeoJSON `features` and SensorML/extension `items` wrappers, follow advertised
`next` links with cycle and page-count bounds, and reject non-200 or malformed
collection responses. Resource IDs SHALL be unique within each resource type.
Resource UIDs SHALL be read from GeoJSON `properties.uid`, SensorML
`uniqueId`, or the extension `uid` form, be valid absolute URIs, and be unique
across all Part 1 resource types. Non-recommended UID forms SHALL produce
warnings, not conformance failures. Registered URN namespace recognition SHALL
use the bundled deterministic snapshot of the IANA Formal and Informal URN
Namespaces registries, including its source URL, registry update date,
retrieval date, and source SHA-256 provenance.

For every advertised collection with a temporal extent and a supported items
media type, the date-time test SHALL query the collection with instant, bounded
interval, open-start interval, and open-end interval values derived from that
extent. It SHALL reject returned features whose `validTime` does not intersect
each query and require every unfiltered feature without `validTime` to remain
in every filtered response. A `validTime` bound equal to `now` SHALL be
evaluated at the captured request time. When no collection advertises a usable
temporal extent, the date-time test SHALL report SKIP with an explicit evidence
limitation.

This increment does not claim full `/conf/api-common` conformance. The released
class inherits five external OGC API Features/Common conformance classes, and
the current inherited `CommonTests` surface is partial. Those inherited suites
remain an explicit prerequisite gap outside the six directly owned OGC 23-001
procedures counted by the released Connected Systems inventory.

## New Scenarios

- `SCENARIO-ETS-PART1-001-CANONICAL-RESOURCES-001`
- `SCENARIO-ETS-PART1-001-COLLECTION-ITEMS-001`
- `SCENARIO-ETS-PART1-001-RESOURCE-IDS-001`
- `SCENARIO-ETS-PART1-001-RESOURCE-UIDS-001`
- `SCENARIO-ETS-PART1-001-RESOURCE-UID-TYPES-001`
- `SCENARIO-ETS-PART1-001-DATETIME-001`
- `SCENARIO-ETS-PART1-001-PAGINATION-FAIL-CLOSED-001`
- `SCENARIO-ETS-PART1-001-DEPENDENCY-CASCADE-001`

## Architecture

`Part1ApiCommonTests` is a new deployed class in group `part1apicommon`.
`part1apicommon` depends on the existing `core` and `common` groups because the
released class inherits OGC API Features Core and OGC API Common. The existing
`systemfeatures` group then depends on `part1apicommon`; all current Part 1
descendants inherit the prerequisite transitively. API Common's resource-fetch
configuration also declares explicit `core` and `common` group dependencies so
it is ordered after those groups. Because TestNG can still invoke a
configuration method after a partial group failure, setup also checks failed
and skipped prerequisite results and raises `SkipException` before reading the
IUT or issuing dependent requests. The sabotage gate selects exactly one report
newer than a per-run marker from an isolated smoke output directory.

UID extraction follows representation authority: SensorML `uniqueId` first,
GeoJSON `properties.uid` second, then direct `uid` as an extension fallback.

`Part1ApiCommonSupport` owns read-only canonical-resource and collection-item
pagination. It is ETS code, not a replacement for the external SWE Common or
SensorML validators. No OSH or TeamEngine source or binary changes are allowed.

## Acceptance Boundary

Sprint 46 closes only when all six directly owned released procedures have
reviewed mappings, the coverage audit reports those procedures complete,
focused and full Maven verification succeed, TeamEngine executes the changed
suite against the real local OSH IUT, dependency/credential gates execute, and
Raze has no unresolved required findings. A SKIP caused by a local IUT with no
advertised temporal extent is not positive date-time conformance evidence and
must remain visible.
