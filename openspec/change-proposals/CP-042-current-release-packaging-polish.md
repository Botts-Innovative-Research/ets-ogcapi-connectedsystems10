# CP-042: Current Release Packaging Polish

## Status

COMPLETE - RAZE APPROVED

## User Instruction

"Do whatever packing or polish is left, that will be our focus now"

## Problem

Sprint 79 created the release-readiness external-blocker package before the
Sprint 80 `connected-systems-go` filing handoff and Sprint 81 SensorML
first-party validator hardening. Reviewers now need one current entry point
that points at the latest package, outreach, validator, and E2E evidence
without rewriting historical packages or making release claims that remain
externally blocked.

The Sprint 79 JSON also retained stale SensorML wording after Sprint 81 made
the ETS-owned SensorML backend the maintained first-party path.

## Change

- Add `REQ-ETS-CLEANUP-032` and
  `SCENARIO-ETS-CLEANUP-CURRENT-RELEASE-PACKAGE-001`.
- Add `ops/release/current-release-readiness.md` and
  `ops/release/current-release-readiness.json` as the reviewer-facing current
  package overlay.
- Update the historical Sprint 79 machine-readable package to use current
  SensorML first-party wording while preserving its generated date and
  historical package role.
- Reconcile status, test-results, known-issues, changelog, traceability,
  architecture, README, and Epic ETS-05.
- Archive lightweight verification evidence for the packaging overlay.

## Non-Goals

- Do not change Java/TestNG behavior.
- Do not run mutation tests or mutate an IUT.
- Do not promote candidate mappings to reviewed exact.
- Do not claim beta readiness, CITE submission, Maven Central publication, or
  three passing implementations.
- Do not file upstream GitHub or CITE tickets.

## Acceptance

- [x] Current package overlay indexes Sprint 79, Sprint 80, and Sprint 81
  artifacts.
- [x] Current package overlay records `240 total / 191 exact / 2 helper / 47
  candidate / 0 unmapped`.
- [x] Current package overlay records latest full Docker Maven and local OSH
  TeamEngine evidence from Sprint 81.
- [x] Current package overlay preserves all release non-claims.
- [x] Stale SensorML release-package wording is corrected to the first-party
  maintained path.
- [x] Lightweight JSON/YAML/package-consistency/artifact/diff checks are
  archived.
- [x] Initial Raze adversarial review is archived with required fixes.
- [x] Focused Raze recheck is archived and closes all required fixes.
