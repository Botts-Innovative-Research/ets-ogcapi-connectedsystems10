# S-ETS-70-01: Part 2 Create/Replace/Delete Released Method Surface

## Status

COMPLETE_RAZE_APPROVED_PUSHED

## User Instruction

"Continue"

## Scope

Convert the Part 2 Create/Replace/Delete runtime class from the Sprint 26 coarse
safety-gated subset to a released Annex A.7 method surface: one deployed TestNG
method per OGC 23-002 Part 2 CRD abstract test target. The sprint removes the
remaining unmapped Part 2 CRD coverage targets without claiming exact lifecycle
closure until a dedicated mutable IUT proves the required create, replace, and
delete behavior.

## Requirements

- `REQ-ETS-PART2-007`
- `SCENARIO-ETS-PART2-007-RELEASED-METHOD-SURFACE-001`
- `SCENARIO-ETS-PART2-007-CRD-CONFORMANCE-DECLARED-001`
- `SCENARIO-ETS-PART2-007-FEATURES4-PREREQUISITE-001`
- `SCENARIO-ETS-PART2-007-MUTATION-SAFETY-GATE-001`
- `SCENARIO-ETS-PART2-007-DATASTREAM-OBSERVATION-LIFECYCLE-OPTIN-001`
- `SCENARIO-ETS-PART2-007-CONTROLSTREAM-COMMAND-LIFECYCLE-OPTIN-001`
- `SCENARIO-ETS-PART2-007-FEASIBILITY-SYSTEMEVENT-LIFECYCLE-OPTIN-001`
- `SCENARIO-ETS-PART2-007-UNAVAILABLE-ENDPOINT-HONESTY-001`
- `SCENARIO-ETS-PART2-007-SMOKE-NO-PUBLIC-MUTATION-001`

## Acceptance Criteria

- [x] OpenSpec records the Sprint 70 released-method-surface requirement.
- [x] Structural tests fail before implementation because the current class has
  fewer than sixteen released-target methods and four unmapped targets.
- [x] `Part2CreateReplaceDeleteTests` exposes sixteen child-target methods and
  no helper method carries multiple child requirement targets.
- [x] Coverage report shows Part 2 CRD with zero unmapped targets and remains
  candidate, not exact, without positive lifecycle evidence.
- [x] Mutation safety remains fail-closed for public and non-opted-in IUTs.
- [x] Formatter, focused Maven, coverage audit, full Maven, and disposable
  local OSH E2E evidence are archived.
- [x] Raze reviews the scoped change before completion.
- [x] Specs, story, traceability, status, changelog, test-results, and metrics
  are reconciled.

## Implementation Notes

- `Part2CreateReplaceDeleteTests` now has sixteen child-target lifecycle
  methods, each carrying exactly one OGC 23-002 Annex A.7 requirement URI.
- Foundational declaration, prerequisite, mutation-safety, OPTIONS-readiness,
  and unavailable-endpoint honesty checks carry only the parent
  `/req/create-replace-delete` target and no longer create duplicate child
  candidate mappings.
- `ops/ats-coverage-report.json` records `2:/conf/create-replace-delete` as
  `16 total / 0 exact / 0 helper / 16 candidate / 0 unmapped`; no reviewed
  exact mapping was promoted.
- Docker Maven passes `786 tests / 0 failures / 0 errors / 3 skipped`.
- Disposable local OSH E2E ran against an owned mutable populated IUT and the
  clean primary. The workflow remains non-green on the unchanged Sprint 69
  baseline failure set (`20` existing SensorML/Deployment/Procedure/Property
  failures), while all twenty-two in-scope Part 2 CRD methods SKIP honestly,
  request counts are GET-only, cleanup PASSes, and primary-state isolation is
  unchanged.
- Raze final focused recheck returned `APPROVE 0.97` with
  `RAZE-ETS70-EVIDENCE-001` closed and `required_fixes: []`.

## Non-Goals

- Do not patch OSH or TeamEngine.
- Do not mutate a public or primary IUT.
- Do not promote reviewed exact Part 2 CRD mappings without positive lifecycle
  E2E.
