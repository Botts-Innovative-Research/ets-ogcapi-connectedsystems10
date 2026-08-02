# S-ETS-71-01: Part 2 Update Released Method Surface

## Status

IMPLEMENTED_RAZE_APPROVED_PUSHED

## User Instruction

"Continue"

## Scope

Convert the Part 2 Update runtime class from the Sprint 27 coarse safety-gated
subset to a released Annex A.8 method surface: one deployed TestNG method per
OGC 23-002 Part 2 Update abstract test target. This sprint preserves the
candidate status of all Update targets and does not claim positive PATCH
lifecycle conformance.

## Requirements

- `REQ-ETS-PART2-008`
- `SCENARIO-ETS-PART2-008-RELEASED-METHOD-SURFACE-001`
- `SCENARIO-ETS-PART2-008-UPDATE-CONFORMANCE-DECLARED-001`
- `SCENARIO-ETS-PART2-008-CRD-FEATURES4-PREREQUISITES-001`
- `SCENARIO-ETS-PART2-008-RESOURCE-CONDITION-GATES-001`
- `SCENARIO-ETS-PART2-008-PATCH-MUTATION-SAFETY-GATE-001`
- `SCENARIO-ETS-PART2-008-OPTIONS-PATCH-READINESS-001`
- `SCENARIO-ETS-PART2-008-DATASTREAM-OBSERVATION-PATCH-OPTIN-001`
- `SCENARIO-ETS-PART2-008-CONTROLSTREAM-COMMAND-PATCH-OPTIN-001`
- `SCENARIO-ETS-PART2-008-FEASIBILITY-SYSTEMEVENT-PATCH-OPTIN-001`
- `SCENARIO-ETS-PART2-008-SCHEMA-REJECTION-HONESTY-001`
- `SCENARIO-ETS-PART2-008-UNAVAILABLE-ENDPOINT-HONESTY-001`
- `SCENARIO-ETS-PART2-008-SMOKE-NO-PUBLIC-PATCH-001`

## Acceptance Criteria

- [x] OpenSpec records the Sprint 71 released-method-surface requirement.
- [x] Structural tests fail before implementation because the current class has
  multi-target child Update methods.
- [x] `Part2UpdateTests` exposes fourteen child-target methods and no helper
  method carries multiple child requirement targets.
- [x] Coverage report shows Part 2 Update with zero unmapped targets and
  remains candidate, not exact, without positive PATCH lifecycle evidence.
- [x] Mutation safety remains fail-closed for public and non-opted-in IUTs.
- [x] Formatter, focused Maven, coverage audit, full Maven, and local OSH E2E
  evidence are archived.
- [x] Raze reviews the scoped change before completion.
- [x] Specs, story, traceability, status, changelog, test-results, and metrics
  are reconciled.

## Non-Goals

- Do not patch OSH or TeamEngine.
- Do not mutate a public IUT.
- Do not promote reviewed exact Part 2 Update mappings without positive PATCH
  E2E.
- Do not implement concrete PATCH body semantics in this method-surface sprint.

## Implementation Notes

- `Part2UpdateTests` now exposes twenty-four deployed `@Test` methods: ten
  parent/readiness/honesty methods carrying only `/req/update`, plus fourteen
  one-target deferred lifecycle methods for Annex A.8 Requirements 79-92.
- `VerifyPart2UpdateTests.releasedAnnexA8TargetsHaveOneDeployedMethodEach`
  was added test-first and failed after formatting because the historical
  Update surface carried multiple child targets on coarse methods. It passes
  after the split.
- Coverage remains candidate-honest:
  `2:/conf/update = 14 total / 0 exact / 0 helper / 14 candidate /
  0 unmapped`; overall coverage remains
  `240 total / 191 exact / 2 helper / 47 candidate / 0 unmapped`.
- Verification evidence is archived under
  `ops/test-results/sprint-ets-71-part2-update-method-surface-2026-08-02/`:
  formatter passes, focused structural Maven `86/0/0/0`, coverage
  update/audit passes, full Docker Maven `787/0/0/3`, and
  `repo-evidence-manifest.sha256` verifies the archived repository evidence.
- Disposable local OSH mutable E2E run
  `sprint-ets-71-update-20260802T153902Z` provisioned and cleaned up owned
  data successfully, left primary state unchanged, and kept TeamEngine request
  logs GET-only during populated and clean-primary smoke runs. The full smoke
  remains honestly non-green on the existing local OSH twenty-failure baseline:
  populated `275/24/20/231`, clean-primary `275/23/20/232`. All twenty-four
  in-scope Part 2 Update methods SKIP honestly without PATCH dispatch.
- Raze review
  `.harness/evaluations/sprint-ets-71-adversarial.yaml` returned
  `APPROVE_WITH_CONCERNS 0.96` with a LOW non-blocking note that the copied
  local OSH source-run manifest verifies only in the original `/tmp` run tree.
  The repository evidence subset is independently verified by
  `repo-evidence-manifest.sha256`; `required_fixes: []`.
- Implementation and evidence commit `081e09f` is pushed to Botts `main`.
