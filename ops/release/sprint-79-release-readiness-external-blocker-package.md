# Sprint 79 Release Readiness External Blocker Package

Status: pre-beta package, not CITE submission  
Generated: 2026-08-03  
Spec anchor: `REQ-ETS-CLEANUP-030` and
`SCENARIO-ETS-CLEANUP-RELEASE-READINESS-BLOCKER-PACKAGE-001`

## Verdict

The ETS is in a reviewable pre-beta state for the implemented released ATS
surface, but it is not ready to claim beta submission or full conformance-suite
completion. The current coverage report records:

| Total | Exact | Helper | Candidate | Unmapped |
|-------|-------|--------|-----------|----------|
| 240 | 191 | 2 | 47 | 0 |

The remaining 47 released ATS procedures are all mutation-bound. They stay
candidate until a dedicated conforming mutable IUT supplies complete
declarations, method readiness, positive lifecycle evidence,
cleanup/isolation proof, and TeamEngine E2E execution.

## Remaining Candidate Classes

| Class | Candidate Procedures | Current Disposition |
|-------|----------------------|---------------------|
| Part 1 `/conf/create-replace-delete` | 12 | ETS technical gates exist, but positive mutation E2E is externally blocked. |
| Part 1 `/conf/update` | 5 | PATCH lifecycle evidence is externally blocked. |
| Part 2 `/conf/create-replace-delete` | 16 | One-method-per-target candidate surface exists; positive lifecycle exactness is externally blocked. |
| Part 2 `/conf/update` | 14 | One-method-per-target candidate surface exists; PATCH lifecycle exactness is externally blocked. |

No released ATS procedures are unmapped. No mutation-bound candidate mapping is
promoted by this package.

## Known IUT Assessment

| IUT | Evidence | Release Impact |
|-----|----------|----------------|
| Local OSH | Sprint 74 readiness audit: `GET=1`, `OPTIONS=25`, `unsafeMethodsIssued=[]`, one declaration/method-ready class, zero prerequisite-declaration-ready classes. | Useful for local E2E, provisioning, cleanup, and no-mutation evidence; cannot close the 47 candidates today. |
| `connected-systems-go` self-run | Sprint 76 direct lifecycle PASS `29/29` across DataStream, Observation, ControlStream, and Command; TeamEngine remained `275/15/1/259`. | Useful diagnostic and outreach target; not exact-promotion ready because Part 1 Core, declaration, `Allow`, Feasibility, and Update/PATCH blockers remain. |
| `connected-systems-go` public refresh | Sprint 78 public readiness: `GET=1`, `OPTIONS=25`, `unsafeMethodsIssued=[]`, zero declaration/method-ready classes. | Public endpoint remains read-only evidence only; no promotion. |
| 52North `connected-systems-pygeoapi` | Sprint 78 public readiness: `GET=1`, `OPTIONS=25`, `unsafeMethodsIssued=[]`, zero declaration/method-ready classes; source has incomplete Part 1/Part 2 conformance provider output. | Best new source-run watch candidate, but not a current declaring mutable IUT. |
| DGIWG Glaux Server | Sprint 78 found a direct CS API server claim, but public `glaux-server` source is README-only with no license metadata, runnable source, or endpoint evidence. | Watchlist only. |

## Release Checklist

| Gate | Status | Evidence or Rationale |
|------|--------|-----------------------|
| Released ATS inventory | Ready with archived evidence | `ops/ats-coverage-report.json` and Sprint 79 `coverage-audit.txt` record `240/191/2/47/0`. |
| Java/TestNG implementation surface | Ready for implemented classes; blocked for candidate promotion | Exact reviewed mappings cover 191 released procedures and 2 helpers; the four mutation classes remain candidate. |
| Docker Maven regression | Prior evidence available; fresh release rerun required | Most recent full Docker Maven evidence remains from the implementation sprints; Sprint 79 did not alter Java/TestNG behavior. |
| TeamEngine runtime | Prior evidence available; fresh release rerun required | TeamEngine 6 runtime and local OSH E2E are documented in prior sprint evidence. |
| Positive mutable-IUT E2E for remaining candidates | Blocked | No known open-source IUT currently meets declaration, method, lifecycle, cleanup, and TeamEngine requirements. |
| Reproducible double-build release check | Not run in Sprint 79 | Remains a release gate before tagging or Maven Central publication. |
| SWE Common validator dependency | Ready | Existing source-pinned validator integration is confirmed by Libby's branch triage and focused adapter tests. |
| SensorML reusable validator replacement | First-party ETS path active; reusable module deferred | Current first-party adapter/backend is implemented and maintained; future reusable module replacement remains outside this package. |
| Three-implementation roster | Blocked | No three passing implementations are secured. |
| Maven Central / OSSRH publication | Deferred | `REQ-ETS-CITE-001` remains beta-milestone-bound. |
| CITE SC submission ticket | Deferred | `REQ-ETS-CITE-003` remains unfiled. |

## Evidence Index

- Coverage report: `ops/ats-coverage-report.json`
- Sprint 79 coverage audit:
  `ops/test-results/sprint-ets-79-release-readiness-external-blocker-2026-08-03/coverage-audit.txt`
- Sprint 79 generated coverage summary:
  `ops/test-results/sprint-ets-79-release-readiness-external-blocker-2026-08-03/coverage-summary.json`
- Sprint 79 generated IUT readiness summary:
  `ops/test-results/sprint-ets-79-release-readiness-external-blocker-2026-08-03/iut-readiness-summary.json`
- Local OSH readiness evidence:
  `ops/test-results/sprint-ets-74-prerequisite-declaration-fields-2026-08-03/local-osh-primary-prerequisite-declaration-readiness.json`
- `connected-systems-go` self-run evidence:
  `ops/test-results/sprint-ets-76-connected-systems-go-readiness-2026-08-03/`
- `connected-systems-go` upstream gap package:
  `ops/outreach/connected-systems-go-readiness-gap-request.md`
- Alternate IUT discovery evidence:
  `ops/test-results/sprint-ets-78-alternate-iut-discovery-followup-2026-08-03/`
- Known issues handoff: `ops/known-issues.md`
- Current operational status: `ops/status.md`
- Initial Raze review: `.harness/evaluations/sprint-ets-79-adversarial.yaml`
- Focused Raze recheck:
  `.harness/evaluations/sprint-ets-79-adversarial-recheck.yaml`

## Verification

- Coverage audit: PASS.
- JSON/YAML parse: PASS.
- Artifact presence and blocker text checks: PASS.
- Evidence manifest: PASS, including `verification-summary.json`.
- `git diff --check`: PASS.
- Raze: initial `GAPS_FOUND 0.86` for manifest scope; focused recheck
  `APPROVE 0.96` with `required_fixes=[]`.

## Non-Claims

This package does not claim:

- full ETS release completion;
- beta readiness;
- Maven Central publication;
- CITE SC submission;
- three passing implementations;
- mutation-bound exact promotion;
- public IUT mutation evidence.

## Next Gates

The next useful project work is not broad IUT discovery. The next useful gates
are either targeted upstream outreach using the prepared
`connected-systems-go` package, a disposable local source-run experiment for
52North if desired, or final release-candidate verification once a conforming
mutable IUT exists.
