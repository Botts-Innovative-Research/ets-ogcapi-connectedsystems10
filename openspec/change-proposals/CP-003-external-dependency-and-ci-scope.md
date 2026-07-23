# CP-003: External Dependency Immutability and No Hosted CI

> Status: Accepted by user instruction | Author: Codex | Date: 2026-07-23

## References

- Story: `epics/stories/s-ets-43-01-external-dependency-scope-reconciliation.md`
- Spec: `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- Architecture: `_bmad/architecture.md`
- Decision: `_bmad/adrs/ADR-012-external-dependency-immutability-and-no-hosted-ci.md`

## Motivation

The project boundary excludes modifications to OpenSensorHub (OSH), TeamEngine,
and project-operated CI. Earlier planning incorrectly treated a local OSH ConSys
patch and future GitHub Actions activation as acceptable project work.

The current environment does not contain those external source modifications:
the active OSH source tree is clean and has no commits ahead of upstream, its
deployed ConSys jar identifies that clean upstream commit, and the ETS image adds
only ETS-owned artifacts to a digest-pinned OGC TeamEngine image. The obsolete
plans still need to be retired so future agents do not repeat out-of-scope work.

## Accepted Changes

1. Add `REQ-ETS-SCOPE-001`: the project shall not modify, patch, fork, publish,
   or replace OSH or TeamEngine source code or binaries.
2. Permit only supported external interfaces: IUT configuration/test data and
   additive ETS installation at documented TeamEngine extension locations.
3. Require provenance checks showing that the primary E2E IUT and TeamEngine
   base have not been patched by this project.
4. Add `REQ-ETS-SCOPE-002`: project-operated hosted CI is a non-goal. Verification
   remains local Docker Maven, runtime checks, and mandatory TeamEngine E2E.
5. Keep Jenkinsfiles only as inert OGC submission/build metadata; they are not an
   active project CI service.
6. Retire `REQ-ETS-CLEANUP-007`, remove the dormant GitHub Actions workflow, and
   remove all future-activation instructions.
7. Retire S-ETS-40-01 as out of scope. Preserve its historical records, but do not
   treat its external patch as current or repeatable implementation evidence.

## Impact

- **Spec/design**: add scope requirements and scenarios; reconcile stale CI and
  OSH-patch statements.
- **Code**: no OSH or TeamEngine source changes; remove the dormant workflow and
  update the ETS packaging regression.
- **Tests**: assert the absence of hosted workflow definitions and preserve the
  TeamEngine base-immutability checks.
- **Operations**: archive the source/runtime provenance audit and rerun the ETS
  unit, runtime, and local OSH TeamEngine gates without external patches.

## Alternatives Rejected

- Revert an external commit that is not present: destructive and unsupported by
  the current repository/runtime state.
- Keep dormant CI activation material: conflicts with the settled scope and
  invites future agents to re-open a closed path.
- Patch OSH to make the IUT satisfy ETS assertions: changes the implementation
  under test and invalidates the project boundary.
