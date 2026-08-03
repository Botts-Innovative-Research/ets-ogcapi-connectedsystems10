# CP-039: Release Readiness External Blocker Package

## Status

COMPLETE_RAZE_APPROVED

## User Instruction

"Do plan 1"

Plan 1 was the recommended release-readiness path: package the ETS as
feature-complete for the implemented surface while clearly preserving the
external mutable-IUT blocker for the 47 remaining candidate procedures.

## Problem

Sprint 78 confirms that currently known open-source IUTs do not supply
certifiable positive mutation evidence for the remaining Create/Replace/Delete
and Update candidates. Continuing broad discovery is unlikely to reduce the
candidate set without a new runnable conforming implementation. The project
needs a durable pre-beta package that communicates the real state without
overstating completion.

## Change

- Create a release-readiness package under `ops/release/` that records current
  ATS coverage, exact/candidate totals, blocking mutable-IUT evidence,
  release checklist status, CITE submission status, and next gates.
- Index the authoritative evidence artifacts needed by reviewers and future
  maintainers.
- State explicitly that the ETS has zero unmapped released ATS procedures, but
  the remaining 47 procedures remain candidate until a conforming dedicated
  mutable IUT supplies declarations, method readiness, positive lifecycle
  evidence, cleanup/isolation, and TeamEngine E2E execution.
- Keep beta submission and Maven Central publication calendar-bound under
  `REQ-ETS-CITE-001..003`; this package is not a CITE submission.

## Non-Goals

- Do not promote any Create/Replace/Delete or Update candidate mapping.
- Do not run public mutation probes or contact public IUTs.
- Do not file the CITE SC ticket.
- Do not publish to Maven Central or OSSRH.
- Do not claim three passing implementations.
- Do not rerun Docker Maven or TeamEngine for this documentation-only package
  unless a local consistency check reveals a concrete need.

## Acceptance

- [x] A Markdown release-readiness package and machine-readable JSON companion
  exist under `ops/release/`.
- [x] The package records `240 total / 191 exact / 2 helper / 47 candidate /
  0 unmapped` and names the four remaining mutation-bound classes.
- [x] The package references local OSH, `connected-systems-go`, 52North, and
  Glaux evidence without treating any known IUT as exact-promotion ready.
- [x] The checklist distinguishes ready, blocked, deferred, and not-run gates.
- [x] OpenSpec, story, contract, traceability, ops status, changelog,
  test-results, and CITE epic notes agree.
- [x] Lightweight JSON/YAML/coverage/diff verification and Raze review are
  recorded.

## Result

Sprint 79 adds the release-readiness package under `ops/release/` plus
generated evidence under
`ops/test-results/sprint-ets-79-release-readiness-external-blocker-2026-08-03/`.
The package records the current coverage state as `240 total / 191 exact / 2
helper / 47 candidate / 0 unmapped`, preserves the four mutation-bound
Create/Replace/Delete and Update classes as candidate, and keeps CITE
submission, Maven Central/OSSRH publication, beta status, and the
three-implementation roster incomplete. Initial Raze found one evidence
manifest gap; the manifest was regenerated to cover `verification-summary.json`
and focused Raze recheck returned `APPROVE 0.96` with `required_fixes=[]`.
