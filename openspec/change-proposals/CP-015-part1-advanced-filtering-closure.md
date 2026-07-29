# Change Proposal: CP-015 Part 1 Advanced Filtering Direct ATS Closure

**Date**: 2026-07-29
**Author**: Codex
**Affects**:

- `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- `openspec/capabilities/ets-ogcapi-connectedsystems/design.md`
- `REQ-ETS-PART1-009`
- `REQ-ETS-COVERAGE-001`

**Status**: R4 remediation precommit green; exact-candidate gates pending

## Motivation

The released OGC 23-001 `/conf/advanced-filtering` class contains 25
procedures: 22 requirements and three recommendations. The historical class
has six methods, only four candidate mappings, and no reviewed exact mapping.
It is limited to System seeds, performs no cross-resource association checks,
uses a geometry smoke assertion instead of intersection semantics, and couples
all evidence acquisition in class setup.

## Changed Requirement

### REQ-ETS-PART1-009

The ETS SHALL replace the historical subset with one independently executable
TestNG method for each released procedure:

- `/id-list-schema`;
- `/resource-by-id`;
- `/resource-by-keyword`;
- `/resource-by-property`;
- `/feature-by-geom`;
- `/system-by-parent`;
- `/system-by-procedure`;
- `/system-by-foi`;
- `/system-by-obsprop`;
- `/system-by-controlprop`;
- `/deployment-by-parent`;
- `/deployment-by-system`;
- `/deployment-by-foi`;
- `/deployment-by-obsprop`;
- `/deployment-by-controlprop`;
- `/procedure-by-obsprop`;
- `/procedure-by-controlprop`;
- `/sf-by-foi`;
- `/sf-by-obsprop`;
- `/sf-by-controlprop`;
- `/prop-by-baseprop`;
- `/prop-by-object`;
- `/combined-filters`;
- `/indirect-prop`; and
- `/indirect-foi`.

Every procedure SHALL establish the Advanced Filtering declaration before
filter-specific access. Each mandatory procedure SHALL derive query evidence
from resources exposed by the same IUT, issue only GET requests, traverse every
response page with bounded same-origin pagination, validate status and actual
media before parsing, and verify every returned resource against the requested
predicate. A filter derived from a known matching resource SHALL not PASS on an
empty result.

Common `id`, `q`, and combined-filter procedures SHALL process every canonical
Part 1 resource endpoint whose owning conformance class is declared. ID
coverage SHALL use local IDs, UIDs, and a non-empty shorter UID prefix followed
by `*`; every wildcard result UID SHALL begin with that prefix and the known
seed SHALL be present across complete pagination. Keyword coverage SHALL use
only seed-derived `name`, `description`, or SensorML-equivalent `label` text at
the resource root or immediate GeoJSON `properties` boundary. Link metadata
and arbitrary extension descendants SHALL not supply keyword evidence. Custom property,
indirect property, and indirect feature-of-interest procedures target released
recommendations and SHALL emit visible warnings rather than conformance
failures when the recommendation is not implemented.

Geometry filtering SHALL process Systems, Deployments, and Sampling Features
with usable GeoJSON geometry. Query geometry and returned geometry SHALL be
parsed with JTS, and every returned feature SHALL intersect the query geometry.

Association procedures SHALL derive local-ID and UID filters from actual
relations, validate the filtered endpoint through the corresponding released
resource representation boundary, and follow the procedure-specific
associations. Same-origin HTTP traversal SHALL be bounded and cycle-safe.
Successfully resolved targets SHALL supply their representation local ID and
UID; link-wrapper IDs, path tokens, and hrefs SHALL not be treated as synthetic
replacements. Procedures that prescribe deployed-System, features-of-interest,
Datastream, or ControlStream subresource traversal SHALL not be satisfied by
equivalent root aliases. Association collection targets SHALL be actual-media
gated and fully paginated. Broken or unsupported-media targets may contribute
only their target URI where the released procedure explicitly permits
unresolved target identity.
Depth, cycle, and reference-read limit exhaustion SHALL fail explicitly.
Cross-origin association targets SHALL not receive IUT credentials; when the
released procedure permits unresolved targets to act as identifiers, their URI
SHALL be used without dereference.

Combined filtering SHALL exercise every independently evidenced pairwise
combination and at least two distinct combinations per inspectable canonical
endpoint, validating every constituent predicate. Its inventory SHALL include
applicable inherited `id`, `q`, `featureType`, `datetime`, and geometry
predicates, all applicable mandatory class-specific predicates, and any
positively supported custom-property recommendation.

Indirect recommendations SHALL inspect every eligible Property and Sampling
Feature, including later-page resources, and aggregate visible warnings rather
than stopping after the first eligible seed. The released indirect-property
procedure uses `observedProperty` outside Properties and `baseProperty` for
Properties; it does not define a controlled-property repetition.

Mandatory evidence limitations SHALL aggregate only after all independently
inspectable endpoints or resources have been processed. Assertion failures,
HTTP defects, invalid supported representations, unsafe traversal, wrong
predicates, and later-page defects SHALL not be caught or downgraded.

## Released Editorial Resolutions

Three obvious Annex A prose defects are resolved against the target normative
requirement:

- `/deployment-by-parent` validates the parent Deployment association, not the
  text's accidental `parentSystem` wording;
- the UID repetition for `/deployment-by-system` uses the `system` parameter,
  not the text's accidental `foi` parameter; and
- `/indirect-prop` evaluates Sampling Features at `/samplingFeatures`, not the
  text's repeated `/systems` endpoint.

For System property recursion, the normative requirement's recursive
subsystem semantics control. The implementation may follow advertised
subsystem/component links and SHALL not require a non-standard path name.

## Architecture

`AdvancedFilteringTests` SHALL retain only immutable API-root setup. Every
released procedure SHALL use `alwaysRun`, retrieve its own evidence, and have
no method dependency. Released direct inheritance is:

```text
Core/Common -> Part 1 API Common -> Advanced Filtering
```

`AdvancedFilteringSupport` SHALL own declaration checks, canonical endpoint
selection, ID and keyword generation, bounded filter traversal, representation
validation dispatch, geometry intersection, association resolution, predicate
evaluation, combined-filter truth checks, transitive recommendation checks,
and cross-origin credential safety.

No executable conformance-suite jar is imported as a library. No OSH or
TeamEngine source code or binary is modified. Project-operated hosted CI
remains out of scope.

## Acceptance Boundary

Sprint 55 closes only when all 25 procedures have reviewed exact mappings,
focused and full Maven verification complete, TeamEngine deploys every changed
method against unmodified local OSH with honest outcomes, controlled read-only
HTTP coverage proves every positive procedure and key fail-closed branch, API
Common sabotage proves pre-IUT dependency behavior, exact-image runtime and
credential/artifact-hygiene gates complete, and Raze reports no unresolved
required findings.

## Implementation Reconciliation

The first exact candidate and its verification evidence are retained as
diagnostics, not completion evidence. Final Raze recheck `GAPS_FOUND 0.99`
reopened association identity provenance, procedure-specific traversal,
reference media/pagination, combined-predicate completeness, keyword
provenance, mapping accuracy, scenario traceability, and exact committed-build
provenance.

R4 has a reproducible `40/4/0/0` red baseline, focused controlled-HTTP
`40/0/0/0`, full precommit Docker Maven `594/0/0/3`, and a `20/20`
scenario-anchor inventory. Direct associations now accept exact recognized
fields only at representation boundaries, and Deployment property
dereferences must be non-collection System representations. Candidate
`756d729` and all its exact gates are superseded audit evidence. A new
committed candidate must rerun exact Maven, image/runtime,
unmodified-local-OSH, sabotage, credential, no-mutation, immutability,
artifact-hygiene, and fresh Raze gates without modifying OSH or TeamEngine.
