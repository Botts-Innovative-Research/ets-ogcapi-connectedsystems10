# S-ETS-66-02: Codex Session Metrics JSONL Support

## Status
DONE - PUSH PENDING.

## User Instruction
Triggered by: "Make the practical fix, then continue with the project - and
don't stop unless you need my input."

## Scope
- Preserve existing Claude Code session JSONL support.
- Add Codex rollout JSONL auto-discovery for the current repository.
- Prefer main-thread Codex rollouts over sub-agent rollouts.
- Extract Codex `token_count` records from `payload.info.last_token_usage`.
- Split cached input and cache-write input out of ordinary input tokens so
  cost categories are not double-counted.
- Add a durable `--self-test` mode covering both supported JSONL schemas.

## Acceptance
- [x] OpenSpec records `REQ-ETS-CLEANUP-022` and
  `SCENARIO-ETS-CLEANUP-CODEX-SESSION-METRICS-001`.
- [x] `python3 scripts/session-metrics.py --self-test` passes.
- [x] `python3 scripts/session-metrics.py` auto-discovers the current checkout
  Codex main rollout and prints non-zero usage totals.
- [x] Raze review is approved with no unresolved required fixes.
- [ ] Completion evidence is committed and pushed.

## Implementation Evidence
- `ops/test-results/s-ets-66-02-codex-session-metrics-2026-08-01/self-test.txt`:
  `SELF-TEST PASS`.
- `ops/test-results/s-ets-66-02-codex-session-metrics-2026-08-01/current-codex-session.txt`:
  auto-discovery selected the main Codex rollout
  `rollout-2026-07-31T03-34-10-019fb718-4ae0-7601-a699-7adbbcec5d77.jsonl`
  with non-zero totals.
- `ops/test-results/s-ets-66-02-codex-session-metrics-2026-08-01/subagent-codex-session.txt`:
  explicit-path extraction supports Codex sub-agent rollout JSONL when passed
  directly.
- Initial Raze found `RAZE-ETS66-02-AUTODISCOVERY-001` and
  `RAZE-ETS66-02-DOC-001`. The gapfix preserves sub-agent identity across
  mixed sub-agent/parent `session_meta` records, extends `--self-test`, and
  changes the epic status back to Raze/push pending.
- Post-gapfix evidence:
  `self-test-after-raze-fix.txt`, `current-codex-session-after-raze-fix.txt`,
  and `subagent-classification-after-raze-fix.txt`.
- Focused Raze recheck returned `APPROVE 0.96` with
  `RAZE-ETS66-02-AUTODISCOVERY-001` and `RAZE-ETS66-02-DOC-001` closed and
  `required_fixes: []`.
- Commit and push remain pending.
