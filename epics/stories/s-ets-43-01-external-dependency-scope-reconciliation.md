# S-ETS-43-01: Reconcile External Dependency and CI Scope

**Status**: Complete; final Raze recheck approved at confidence 0.99
**Change Proposal**: CP-003
**Requirements**: REQ-ETS-SCOPE-001, REQ-ETS-SCOPE-002

## User Need

As the project owner, I need OSH and TeamEngine to remain unmodified external
systems and hosted CI removed from the roadmap so the ETS stays within approved
scope.

## Acceptance Criteria

- `SCENARIO-ETS-SCOPE-EXTERNAL-SOURCE-IMMUTABILITY-001` passes.
- `SCENARIO-ETS-SCOPE-TEAMENGINE-ADDITIVE-INSTALL-001` passes.
- `SCENARIO-ETS-SCOPE-UNMODIFIED-IUT-PROVENANCE-001` passes.
- `SCENARIO-ETS-SCOPE-HOSTED-CI-NONGOAL-001` passes.
- The active OSH checkout is clean, has no commits ahead of upstream, and the
  deployed ConSys jar identifies the checkout commit.
- No TeamEngine source checkout or project-authored TeamEngine binary patch is
  present; the ETS runtime uses the immutable OGC image and additive extension
  locations only.
- The dormant GitHub Actions workflow and its activation instructions are absent.
- S-ETS-40-01 and its external OSH patch are marked historical and out of scope.
- Docker Maven, exact-image runtime verification, and primary local OSH
  TeamEngine E2E complete against unmodified dependencies.

## Implementation Notes

The initial audit found no current external code change to revert. Historical
OSH commit `79f89fb` is absent from inspected repositories and fetched refs;
project records classify it as local-only, while the current environment cannot
prove global non-publication. The active OSH checkout is clean and behind, not
ahead of, `origin/master`; its deployed ConSys jar reports the active checkout
commit. This is provenance metadata, not independent binary byte-equivalence.

The dormant GitHub Actions definition and activation material are removed.
Current planning surfaces use local gates. Runtime verification compares
TeamEngine path type, mode, ownership, symlink target, directory inventory, and
file content between the pinned base and exact final image.

Verification is recorded in
`ops/test-results/sprint-ets-43-scope-reconciliation-verification-2026-07-23.md`.
Full Docker Maven reported `313/0/0/3`; exact-image runtime verification passed;
local OSH TeamEngine E2E reported `211/69/0/142`, 135 recognized IUT exchanges,
zero writes, and zero startup errors. Initial Raze returned `GAPS_FOUND`,
confidence `0.99`; all findings were remediated. Focused Raze recheck found no
remaining issues and returned `APPROVE`, confidence `0.99`.
