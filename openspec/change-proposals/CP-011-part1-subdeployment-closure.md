# Change Proposal: CP-011 Part 1 Subdeployment Direct ATS Closure

**Date**: 2026-07-26
**Author**: Codex
**Affects**:

- `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- `openspec/capabilities/ets-ogcapi-connectedsystems/design.md`
- `REQ-ETS-PART1-005`
- `REQ-ETS-COVERAGE-001`
- `REQ-ETS-VALIDATOR-001`

**Status**: Completed

## Motivation

The released OGC 23-001 `/conf/subdeployment` class contains five procedures:
subcollection link and representation validation, boolean recursive parameter
support, recursive search from the canonical Deployment endpoint, recursive
search from a Subdeployment endpoint, and recursive Deployment-association
closure.

The historical four-method class implements only a candidate for the collection
procedure. It eagerly probes at most 15 generic JSON Deployments, skips the
whole class when no non-empty subcollection exists, checks inherited Deployment
shape/link approximations, and includes a non-normative dependency tracer. It
does not test either recursive search procedure or recursive association
closure. Those methods cannot be promoted to exact mappings.

## Changed Requirement

### REQ-ETS-PART1-005

The ETS SHALL replace the historical methods with one independently executable
TestNG method for each released `/conf/subdeployment` test:

- `/collection`;
- `/recursive-param`;
- `/recursive-search-deployments`;
- `/recursive-search-subdeployments`; and
- `/recursive-assoc`.

The ETS SHALL discover the Deployment hierarchy independently by traversing the
canonical top-level Deployment collection and each direct Subdeployment
endpoint with bounded same-origin pagination. Every page used as hierarchy
evidence SHALL establish HTTP status and actual GeoJSON or SensorML media before
parsing and SHALL pass the released Deployment collection schema. Duplicate
IDs, cycles, shortcut edges, and the safety bound SHALL fail closed.

For every parent proven to have direct Subdeployments, the collection procedure
SHALL retrieve the canonical parent Deployment, require at least one
`rel=subdeployments` link, and require every occurrence to resolve on the IUT
origin to exactly
`{api_root}/deployments/{encodedParentId}/subdeployments` without query or
fragment variants. The selected link SHALL return HTTP 200 and every returned
page SHALL pass the released Deployment collection schema selected from actual
media. If no parent has direct Subdeployments, the procedure SHALL warn and
SKIP.

The recursive-parameter procedure SHALL issue status-only requests using the
exact boolean values `false` and `true`. The canonical Deployment recursive
search procedure SHALL require default and `recursive=false` results to equal
the independently discovered root set and `recursive=true` to equal every
discovered Deployment. The Subdeployment recursive search procedure SHALL
require default and `recursive=false` results to equal a selected parent's
direct children and `recursive=true` to equal all descendants. If no transitive
hierarchy exists, the latter procedure SHALL warn and SKIP.

The recursive-association procedure SHALL inspect every parent Deployment with
Subdeployments. For each advertised `deployedSystems`, `featuresOfInterest`,
`samplingFeatures`, `datastreams`, or `controlstreams` relation, it SHALL select
a same-origin JSON-compatible occurrence, require HTTP 200, and require its
resource IDs to include explicit fixture evidence for resources owned directly
by the parent and every descendant. An unsupported or cross-origin first
occurrence SHALL NOT hide a usable later occurrence. Missing independent
ownership evidence or no safe comparable occurrence SHALL warn and SKIP; any
observed omission SHALL fail.

Subdeployment target identity SHALL normalize HTTP scheme/host case, effective
default ports, dot segments, and percent-encoded unreserved path characters
while continuing to reject wrong-parent, trailing-path, query, and fragment
variants.

## Architecture

`SubdeploymentsTests` retains only immutable API-root setup. Each released
procedure retrieves its own prerequisites and has no method dependency.
`SubdeploymentsSupport` owns exact link resolution, fail-closed Deployment graph
construction, exact recursive-result comparison, association-link discovery,
and resource-ID extraction.

Released inheritance remains explicit:

```text
Core/Common -> Part 1 API Common -> Deployment -> Subdeployment
```

The TestNG group and defensive setup gate SHALL block Subdeployment only on
inherited Core, Common, API Common, or Deployment failures. Unrelated sibling
classes SHALL NOT become implicit prerequisites.

Subdeployment collection schema dispatch SHALL reuse the public
`DeploymentFeaturesSupport` validator boundary. No SensorML suite jar is
imported, and no OSH or TeamEngine source or binary is modified.

## Acceptance Boundary

Sprint 51 closes only when all five procedures have reviewed exact mappings,
focused and full Maven verification complete, TeamEngine deploys the changed
class against the unmodified local OSH with honest inherited-SKIP outcomes,
controlled HTTP coverage proves every positive path and fail-closed case, a
causal TestNG baseline/sabotage pair isolates exactly one Deployment failure and
proves all five methods change from IUT-reaching execution to pre-IUT SKIP,
exact-image runtime and credential/artifact-hygiene gates complete, and Raze
reports no unresolved required findings.

## Adversarial Amendment

The initial Sprint 51 Raze review returned `GAPS_FOUND` at confidence `0.99`.
`RAZE-S51-001` through `RAZE-S51-004` required an explicit
parent-plus-descendant ownership oracle, a causal dependency experiment,
normalized HTTP target identity, and all-occurrence safe association
selection. Those corrections are implemented and verified. The earlier direct
local-OSH Deployment sabotage remains archived as historical non-causal
evidence and does not satisfy the corrected dependency gate. Exact mappings
are restored. Final Raze returned `APPROVE_WITH_CONCERNS`, confidence `0.99`,
with all six findings closed and no required fixes.
