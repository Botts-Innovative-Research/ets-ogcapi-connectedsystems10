# S-ETS-82-01: Current Release Packaging Polish

## Status

COMPLETE - RAZE APPROVED

## User Instruction

"Do whatever packing or polish is left, that will be our focus now"

## Scope

Create a compact current release-readiness overlay after Sprint 80 and Sprint
81, reconcile stale reviewer-facing status language, and preserve all external
blockers honestly. This is a packaging and documentation sprint only.

## Requirements

- `REQ-ETS-CLEANUP-032`
- `SCENARIO-ETS-CLEANUP-CURRENT-RELEASE-PACKAGE-001`

## Acceptance Criteria

- [x] `ops/release/current-release-readiness.md` gives reviewers one current
  package entry point.
- [x] `ops/release/current-release-readiness.json` provides the same state in
  machine-readable form.
- [x] Current package references Sprint 79 release-readiness, Sprint 80
  outreach handoff, and Sprint 81 SensorML first-party validator hardening.
- [x] Coverage remains `240 total / 191 exact / 2 helper / 47 candidate / 0
  unmapped`.
- [x] All 47 mutation-bound procedures remain candidate.
- [x] The package cites Sprint 81 as the latest full Docker Maven and local OSH
  TeamEngine evidence, including the non-green E2E result.
- [x] The package states SensorML is currently first-party maintained and does
  not claim a reusable upstream SensorML module exists.
- [x] Ops docs, traceability, architecture, README, changelog, known-issues,
  and Epic ETS-05 are reconciled.
- [x] Initial Raze review is archived with required fixes.
- [x] Focused Raze recheck is archived and closes all required fixes.

## Non-Goals

- No Java/TestNG implementation changes.
- No IUT mutation.
- No Maven Central publication.
- No CITE filing.
- No upstream issue filing.
- No exact-promotion count change.

## Verification

- Coverage audit: PASS.
- JSON parse: PASS.
- YAML parse: PASS.
- Package consistency check: PASS.
- Artifact presence check: PASS after regeneration to cover all gate artifacts.
- Stale wording and release overclaim check: PASS.
- Added-content secret scan: PASS.
- `git diff --check`: PASS after staging intended commit contents.
- Initial Raze: `GAPS_FOUND 0.92`; required fixes
  `RAZE-ETS82-VERIFY-001`, `RAZE-ETS82-RECONCILE-001`, and
  `RAZE-ETS82-MANIFEST-001` addressed before focused recheck.
- Focused Raze recheck: `APPROVE 0.94` with `required_fixes=[]`.
