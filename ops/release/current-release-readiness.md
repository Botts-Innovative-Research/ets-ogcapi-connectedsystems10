# Current Release Readiness

Status: pre-beta external-blocker package, not CITE submission
Generated: 2026-08-04T05:32:04Z
Spec anchor: `REQ-ETS-CLEANUP-032` and
`SCENARIO-ETS-CLEANUP-CURRENT-RELEASE-PACKAGE-001`

## Verdict

The ETS is reviewable for the implemented released ATS surface, with all
non-IUT-bound packaging now collected into one current handoff. It is still not
beta-ready and does not claim full conformance-suite completion.

| Total | Exact | Helper | Candidate | Unmapped |
|-------|-------|--------|-----------|----------|
| 240 | 191 | 2 | 47 | 0 |

The remaining 47 released ATS procedures are mutation-bound and remain
candidate until a dedicated conforming mutable IUT supplies complete
declarations, method readiness, positive lifecycle evidence, cleanup/isolation
proof, and TeamEngine E2E execution.

## Current Package Index

| Area | Current Artifact | Status |
|------|------------------|--------|
| Release blocker package | `ops/release/sprint-79-release-readiness-external-blocker-package.md` | Still authoritative for external mutable-IUT blockers; supplemented by this overlay. |
| Machine-readable current package | `ops/release/current-release-readiness.json` | Current reviewer entry point. |
| connected-systems-go outreach | `ops/outreach/connected-systems-go-github-issue-filing-handoff.md` | Issue-ready; creator contacted separately; no local filing claim. |
| SensorML validator | `ops/test-results/sprint-ets-81-sensorml-first-party-hardening-2026-08-04/` | First-party ETS backend hardened; no reusable upstream SensorML module imported. |
| Latest full Maven | `ops/test-results/sprint-ets-81-sensorml-first-party-hardening-2026-08-04/full-maven.txt` | BUILD SUCCESS with `792/0/0/3`. |
| Latest local OSH TeamEngine E2E | `ops/test-results/sprint-ets-81-sensorml-first-party-hardening-2026-08-04/local-osh-smoke-summary.json` | Non-green IUT result: `275 total / 23 passed / 20 failed / 232 skipped`. |

## Remaining External Blockers

| Blocker | Current State |
|---------|---------------|
| Mutable IUT | No known open-source IUT currently closes all 47 mutation-bound candidates. |
| Part 1 Create/Replace/Delete | Candidate until positive POST/PUT/DELETE lifecycle and inherited prerequisite evidence exists. |
| Part 1 Update | Candidate until positive PATCH lifecycle and update declarations exist. |
| Part 2 Create/Replace/Delete | Candidate until positive lifecycle, inherited Features Part 4 evidence, cleanup, and TeamEngine E2E exist. |
| Part 2 Update | Candidate until PATCH, `/conf/update`, `/conf/feasibility`, and lifecycle evidence exist. |
| Three implementations | Not secured. |
| CITE submission | Not filed. |
| Maven Central | Not published. |

## Current Non-Claims

This package does not claim:

- beta readiness;
- CITE SC submission;
- Maven Central publication;
- three passing implementations;
- mutation-bound exact promotion;
- public IUT mutation evidence;
- a green local OSH TeamEngine run;
- a reusable upstream SensorML validator dependency.

## Latest Verification

- Full Docker Maven: Sprint 81 BUILD SUCCESS `792` tests, `0` failures, `0`
  errors, `3` skips.
- Focused SensorML adapter Docker Maven: Sprint 81 PASS `9/0/0/0`.
- Local OSH TeamEngine E2E: Sprint 81 non-green IUT result `275 total / 23
  passed / 20 failed / 232 skipped`.
- Raze: Sprint 81 returned `APPROVE_WITH_CONCERNS 0.91` with
  `required_fixes=[]`.

## Reviewer Path

1. Start with this file and `ops/release/current-release-readiness.json`.
2. Use Sprint 79 for the detailed external blocker package.
3. Use Sprint 80 outreach artifacts for the connected-systems-go maintainer
   handoff.
4. Use Sprint 81 evidence for the current SensorML validator and latest full
   Docker Maven/local OSH TeamEngine evidence.
5. Treat all mutation-bound classes as candidate until a more compliant
   dedicated mutable IUT exists.
