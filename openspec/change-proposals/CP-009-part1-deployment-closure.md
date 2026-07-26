# Change Proposal: CP-009 Part 1 Deployment Direct ATS Closure

**Date**: 2026-07-26
**Author**: Codex
**Affects**:

- `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- `openspec/capabilities/ets-ogcapi-connectedsystems/design.md`
- `REQ-ETS-PART1-004`
- `REQ-ETS-COVERAGE-001`
- `REQ-ETS-VALIDATOR-001`

**Status**: Implemented

## Motivation

The released OGC 23-001 `/conf/deployment` class contains five procedures:
canonical URL equivalence, a parameterized resources endpoint validator, the
canonical endpoint specialization, advertised Deployment collections, and
System-to-Deployment sub-resources.

The historical four-method class tests a different surface. It uses eager
shared setup, checks a non-empty canonical collection and generic item shape,
and treats an encoding declaration as deployed-System evidence. It does not
execute the released collections or `ref-from-system` procedures and cannot be
promoted to exact coverage.

## Changed Requirement

### REQ-ETS-PART1-004

The ETS SHALL replace the historical methods with one independently executable
TestNG method for each released `/conf/deployment` test:

- `/conf/deployment/canonical-url`;
- `/conf/deployment/resources-endpoint`;
- `/conf/deployment/canonical-endpoint`;
- `/conf/deployment/collections`; and
- `/conf/deployment/ref-from-system`.

Every selected Deployment feature collection SHALL be processed. The absence
of an advertised `featureType=sosa:Deployment` collection SHALL fail rather
than produce vacuous canonical or collections PASS evidence. Collection
metadata SHALL use `itemType=feature`. Unsupported actual representation media
SHALL warn and SKIP before parsing, including on later pagination pages.
Restricted collection retrieval SHALL prefer an advertised GeoJSON or SensorML
items representation over an earlier generic JSON link.

At least one canonical link SHALL resolve safely, dereference with HTTP 200, and
return JSON content structurally equal to the collection item after canonical
links are removed from both documents. Every canonical occurrence SHALL resolve
to the canonical Deployment identity. Representation variants and duplicates
are allowed; the first occurrence is dereferenced deterministically. The
canonical URL path SHALL be `{api_root}/deployments/{id}`; a representation
query MAY remain.

The parameterized resources procedure SHALL validate every page against the
released GeoJSON or SensorML Deployment collection schema selected from the
actual `Content-Type`. The canonical-endpoint procedure SHALL invoke that same
behavior at `{api_root}/deployments`.

The System-reference procedure SHALL retrieve all canonical Systems through the
reviewed API Common helper. For every System local ID, it SHALL require HTTP 200
from `{api_root}/systems/{sysId}/deployments`, follow pagination, validate every
page by actual media type, and require every returned Deployment to contain an
explicit representation-specific link to that System ID.

## Architecture

`DeploymentsTests` retains only immutable API-root setup. Each released method
retrieves its own prerequisites and uses no method dependency, so unsupported
media or missing evidence in one procedure cannot suppress the other four.
Defensive result scanning is limited to inherited Core, Common, and Part 1 API
Common tests/configurations; SystemFeatures and sibling configuration outcomes
cannot become implicit blockers.

`DeploymentFeaturesSupport` owns Deployment collection selection, canonical
equivalence, representation-specific System-link extraction, and Deployment
schema dispatch. It is the local adapter seam for the future reusable SensorML
validator: protocol discovery, TestNG verdict policy, pagination, mapping, and
canonical comparison remain ETS-owned, while only SensorML schema semantics
will move behind `ConnectedSystemsSensorMlValidatorAdapter` when a reusable
FCU/OGC module exists. The ETS SHALL NOT import `ets-sensorml30`.

The Deployment group depends on the released Part 1 API Common group, not the
entire System ATS. The `ref-from-system` method executes the released API Common
System retrieval directly. API Common failures and unexpected SKIPs block IUT
access; its documented datetime no-evidence SKIP remains visible but does not
suppress direct Deployment procedures.

No OSH or TeamEngine source or binary is modified. The unmodified local OSH
currently advertises no `sosa:Deployment` collection, returns unsupported
`application/json` from `/deployments`, and returns HTTP 400 from
`/systems/040g/deployments`. TeamEngine E2E SHALL preserve those real
FAIL/SKIP outcomes. A controlled read-only fixture SHALL execute all five
successful paths.

## Acceptance Boundary

Sprint 49 closes only when all five procedures have reviewed exact mappings,
focused and full Maven verification complete, TeamEngine executes all five
against the unmodified local OSH with honest verdicts, controlled HTTP coverage
proves all positive paths and fail-closed cases, exact-image runtime and
dependency/credential gates complete, and Raze has no unresolved required
findings.

The implementation satisfies this boundary. The primary local OSH remains
honestly nonconforming for Deployment (`217/39/3/175` overall; three Deployment
FAIL and two SKIP), while controlled HTTP tests execute every successful
procedure. Coverage is `5/5 exact`; full Maven is `434/0/0/3`; exact-image
runtime, API Common sabotage, credential freshness, and hygiene gates pass.
Focused final Raze recheck is `APPROVE` at confidence `0.99`, with no
unresolved required findings.
