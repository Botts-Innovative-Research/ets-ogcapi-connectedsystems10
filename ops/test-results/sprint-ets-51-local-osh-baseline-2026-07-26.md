# Sprint 51 Local OSH Subdeployment Baseline

Date: 2026-07-26

## Target

- Container: `field-hub-osh-1`
- Docker network: `field-hub_default`
- API root: `http://field-hub-osh-1:8081/sensorhub/api`
- OSH checkout: `4c87a65c9a967d52af9df476e65d7862c7673a15`
- OSH worktree: clean, zero commits ahead, three commits behind upstream
- `/opt/osh`: read-only mount

## Read-Only Probes

All probes used an isolated Docker-network client and issued GET requests only.

| Endpoint | Status | Actual media | Result |
|---|---:|---|---|
| `/deployments` | 200 | `application/json` | one root Deployment, ID `040g` |
| `/deployments?recursive=false` | 200 | `application/json` | same root ID `040g` |
| `/deployments?recursive=true` | 200 | `application/json` | same root ID `040g` |
| `/deployments/040g` | 200 | reported `auto` | canonical and alternate links; no `rel=subdeployments` |
| `/deployments/040g/subdeployments` | 200 | `application/json` | empty `items` |
| `/deployments/040g/subdeployments?recursive=false` | 200 | `application/json` | empty `items` |
| `/deployments/040g/subdeployments?recursive=true` | 200 | `application/json` | empty `items` |
| `/collections` | 200 | `application/json` | no exact `featureType=sosa:Deployment` collection |

## Expected Sprint 51 E2E

The current released Deployment parent group has genuine unsupported-media and
missing-collection outcomes. Because `/conf/subdeployment` inherits
`/conf/deployment`, TeamEngine should dependency-SKIP all five new
Subdeployment methods before Subdeployment IUT access. This is an honest
inherited-prerequisite outcome, not positive Subdeployment conformance evidence.

Controlled read-only HTTP tests must therefore provide the successful
Subdeployment hierarchy, exact-link, recursive search, and recursive-association
oracle. Sprint 51 must not change OSH or TeamEngine to manufacture a passing
local-IUT result.
