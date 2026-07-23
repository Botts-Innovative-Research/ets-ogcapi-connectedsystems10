# S-ETS-02-05: Multi-Stage Dockerfile and Runtime Cleanup

**Status**: Implemented for Docker/runtime work; hosted CI subtask retired by
CP-003/ADR-012 on 2026-07-23

## Delivered Scope

- Added a reproducible multi-stage Docker build.
- Removed the host Maven dependency from image assembly.
- Added non-root runtime execution.
- Tightened TeamEngine suite metadata checks.
- Preserved TeamEngine smoke behavior.

The original implementation used a manually assembled TeamEngine 5.6.1
runtime. ADR-011 later superseded that runtime with the digest-pinned OGC
TeamEngine 6 image while preserving the multi-stage and non-root principles.

## Retired Scope

This story also carried a proposed GitHub Actions activation subtask. It was
never activated and is permanently retired by the project-owner decision
recorded in CP-003/ADR-012. No authorization or workflow activation action
remains.

Current verification uses the repository's local Docker Maven, exact-image
runtime, and TeamEngine E2E procedures. Historical implementation detail is
available in Git history and is not a current task list.
