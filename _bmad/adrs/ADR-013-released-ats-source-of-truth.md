# ADR-013: Released ATS Source of Truth

**Status**: Accepted
**Date**: 2026-07-26

## Context

The project uses Connected Systems repository commit `3fd86c73...` for pinned
OpenAPI input. That commit post-dates publication of OGC 23-001 and 23-002 and
contains evolving standard text. Historical plans also used names and class
counts from the frozen web application and IUT declarations. These sources
conflict with the approved Annex A documents and have produced overstated
coverage.

## Decision

1. OGC 23-001 and OGC 23-002 version 1.0 are the normative certification
   authority.
2. The reproducible Annex A source is repository tag `v1.0.0`, commit
   `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`.
3. Official PDF SHA-256 values recorded at adoption are:
   - 23-001: `c444bff07193daf8ce880077b1d728127868b48c056fe35278129e04d439f9e4`
   - 23-002: `78531c637053890dd501bb153a0046261b9c03fa064d0888a39e2b0dc383d154`
4. The committed semantic inventory, not the rendered-document byte layout, is
   the executable source pin. Reproduction compares identifiers, targets,
   class membership, and ordering from the exact release commit.
5. OpenAPI pins, later branches, frozen web-app registries, and IUT
   `conformsTo` values are secondary inputs. They cannot redefine the released
   ATS.
6. Coverage status is fail-closed. Candidate URI matches are review leads, not
   implemented claims.

## Consequences

- Part 1 is tracked as 13 released classes and 110 tests, including two
  supporting tests.
- Part 2 is tracked as 12 released classes and 130 tests.
- Existing implementation labels must be reconciled against the inventory.
- Source evolution after version 1.0 requires an explicit new standards-version
  decision, not an unnoticed pin change.

