# CP-030: Part 2 Create/Replace/Delete Released Method Surface

## Status

COMPLETE_RAZE_APPROVED_PUSHED

## Trigger

User instructed: "Continue" after selecting Sprint 70 as the next work item.

## Motivation

The current Part 2 Create/Replace/Delete class still exposes a coarse Sprint 26
safety-gated method surface. Coverage shows `2:/conf/create-replace-delete` at
`16 total / 0 exact / 12 candidate / 4 unmapped`. The four unmapped procedures
are the DataStream and ControlStream schema-update and delete-cascade
procedures. The next safe improvement is to expose one deployed TestNG method
per released OGC 23-002 Annex A.7 target while preserving mutation safety.

This change is not allowed to claim full positive CRD lifecycle conformance
unless a dedicated mutable IUT supplies real protocol POST/PUT/DELETE evidence
and cleanup.

## Scope

- Replace the coarse Part 2 CRD candidate surface with sixteen independent
  deployed TestNG methods, one for each released Annex A.7 target.
- Ensure each method carries exactly one canonical released requirement target
  in its TestNG description.
- Add structural regression coverage for method count, target uniqueness, and
  mutation-safety behavior.
- Regenerate `ops/ats-coverage-report.json` so the class has zero unmapped
  targets, while remaining candidate until positive lifecycle evidence is
  available.
- Run local Maven and disposable local OSH E2E gates.

## Out of Scope

- Patching OpenSensorHub or TeamEngine.
- Mutating any public or primary IUT.
- Promoting Part 2 CRD reviewed exact mappings without positive lifecycle E2E.
- Part 1 CRD or Part 2 Update closure.

## Verification

- Test-first structural failure before the method-surface change.
- Focused Part 2 CRD structural/runtime Maven verification.
- Coverage update and coverage audit showing
  `2:/conf/create-replace-delete = 0 exact / 16 candidate / 0 unmapped`.
- Docker Maven full verification.
- Disposable local OSH mutable IUT E2E with cleanup and primary-state
  isolation. The full smoke remains non-green on the unchanged Sprint 69
  SensorML/Deployment/Procedure/Property failure set; all in-scope Part 2 CRD
  methods SKIP honestly and the workflow records only GET requests, cleanup
  PASS, and primary-state isolation PASS.
- Raze final focused recheck returned `APPROVE 0.97` with
  `RAZE-ETS70-EVIDENCE-001` closed and `required_fixes: []`.
