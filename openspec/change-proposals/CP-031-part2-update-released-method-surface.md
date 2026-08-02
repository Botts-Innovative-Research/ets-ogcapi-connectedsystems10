# CP-031: Part 2 Update Released Method Surface

## Status

IMPLEMENTED_RAZE_APPROVED_PENDING_PUSH

## Trigger

User instructed: "Continue" after Sprint 70 was pushed.

## Motivation

The current Part 2 Update class exposes a Sprint 27 safety-gated subset whose
coverage maps all fourteen OGC 23-002 Annex A.8 child targets as candidates,
but several deployed methods carry multiple released child requirement URIs.
That makes the method surface less reviewable and less aligned with the
released ATS inventory than the newer exact-closure suites.

The safe next increment is to expose one deployed TestNG method per released
Part 2 Update target while preserving PATCH mutation safety and candidate
honesty.

## Scope

- Split the Part 2 Update child-target surface into fourteen independent
  deployed TestNG methods, one for each released Annex A.8 target.
- Ensure each child-target method carries exactly one canonical released
  requirement target in its TestNG description.
- Keep declaration, prerequisite, condition-gate, readiness, unavailable
  endpoint, and schema-rejection honesty checks from duplicating child target
  mappings.
- Add structural regression coverage for method count, target uniqueness, and
  mutation-safety behavior.
- Regenerate `ops/ats-coverage-report.json` so the class remains zero unmapped
  and candidate until positive PATCH evidence exists.
- Run local Maven and local OSH E2E gates.

## Out of Scope

- Patching OpenSensorHub or TeamEngine.
- Mutating any public IUT.
- Promoting Part 2 Update reviewed exact mappings without positive PATCH E2E.
- Implementing concrete JSON Patch or merge-patch lifecycle semantics.

## Verification

- Test-first structural failure before the method-surface change:
  `focused-red-structural-after-format.txt` exits 1 and identifies the
  historical multi-target Update methods.
- Focused Part 2 Update structural/runtime Maven verification:
  `focused-green-structural.txt` exits 0 with `86/0/0/0`.
- Coverage update and coverage audit pass, showing
  `2:/conf/update = 0 exact / 14 candidate / 0 unmapped`.
- Docker Maven full verification exits 0 with `787/0/0/3`.
- Local OSH E2E with request-log no-write evidence: disposable run
  `sprint-ets-71-update-20260802T153902Z` provisions and cleans up owned data,
  leaves primary state unchanged, records GET-only TeamEngine smoke request
  counts, and all twenty-four in-scope Part 2 Update methods SKIP honestly
  without PATCH. Full smoke remains non-green on the existing local OSH twenty
  failure baseline: populated `275/24/20/231`, clean-primary `275/23/20/232`.
- Raze adversarial review:
  `.harness/evaluations/sprint-ets-71-adversarial.yaml` returned
  `APPROVE_WITH_CONCERNS 0.96` with `required_fixes: []`. The only finding is a
  LOW non-blocking artifact-hygiene note about the copied local OSH source-run
  manifest; `repo-evidence-manifest.sha256` verifies the repository evidence
  subset in place.
