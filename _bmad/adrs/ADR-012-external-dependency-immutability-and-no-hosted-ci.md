# ADR-012: External Dependency Immutability and No Hosted CI

**Status**: Accepted
**Date**: 2026-07-23

## Context

This repository delivers the Connected Systems ETS. OSH is an implementation
under test and TeamEngine is the external execution platform. Modifying either
system to make this ETS pass is outside the approved project scope. The project
also will not receive approval for a hosted CI service.

Earlier work temporarily treated local OSH patches and future GitHub Actions
activation as project options. The current audit shows neither external code
change remains active, but the planning documents could cause that work to recur.

## Decision

1. OSH and TeamEngine source code and binaries are immutable external
   dependencies from this project's perspective.
2. The project may configure an IUT and create test fixtures through supported
   interfaces. It may install ETS-owned jars and CTL resources only through
   documented TeamEngine extension locations.
3. ETS verification must record provenance sufficient to show the tested OSH
   runtime and TeamEngine base were not patched by this project.
4. Project-operated hosted CI is out of scope. GitHub Actions definitions and
   activation instructions are removed.
5. Repository Jenkinsfiles remain only as inert OGC submission/build metadata.
   They do not represent an approved or connected project CI service.
6. Local Docker Maven checks, exact-image runtime verification, and TeamEngine E2E
   remain the authoritative development gates.

## Consequences

- IUT defects remain visible as FAIL or SKIP evidence; they are not repaired in
  OSH by this project.
- TeamEngine integration remains additive and is guarded by base-image inventory
  comparisons.
- Cross-platform hosted CI evidence is unavailable and must not be claimed.
- Historical external-patch artifacts remain for audit chronology but are
  explicitly non-authoritative for current implementation status.

## Supersedes

- The GitHub Actions topology in `_bmad/architecture.md` and `_bmad/prd.md`.
- The future-activation branch of REQ-ETS-CLEANUP-007/009.
- S-ETS-40-01 as an allowed implementation path.

ADR-011 remains authoritative for the immutable TeamEngine runtime.
