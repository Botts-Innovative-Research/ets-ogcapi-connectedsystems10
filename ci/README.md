# ci/ — staged CI workflow (Path-B closure for S-ETS-04-01)

This directory contains a GitHub Actions workflow (`github-workflows-build.yml`) that is **intentionally orphaned** — it is staged here, not at GitHub's required `.github/workflows/` location, because Sprint 1 through Sprint 4 of this project's automation could not move the file via `gh push` (the GitHub Actions API requires the `workflow` OAuth scope, which the local `gh` CLI authentication did not include).

Per `epics/stories/s-ets-04-01-ci-workflow-escalation.md`, Sprint 4 closes this 5-sprint user-action carryover via **Path B (formal-drop)** — `ci_workflow_live` is removed from the sprint cadence as a perpetual environmental blocker.

## Activating CI in a future session

If/when you want CI to actually run on push/PR, choose ONE of the following paths:

### Option 1 — Refresh `gh` scope, then `git mv`

```bash
# In a terminal where the user can complete an OAuth browser flow:
gh auth refresh -s workflow

# Then from a fresh Claude Code / orchestrator session:
cd ~/docker/gir/ets-ogcapi-connectedsystems10
mkdir -p .github/workflows
git mv ci/github-workflows-build.yml .github/workflows/build.yml
git commit -m "ci: activate workflow (move to .github/workflows/)"
git push origin main
```

Verify a workflow_run posts to:
`https://github.com/Botts-Innovative-Research/ets-ogcapi-connectedsystems10/actions`

### Option 2 — Upload via GitHub web UI (no scope refresh required)

1. Open `https://github.com/Botts-Innovative-Research/ets-ogcapi-connectedsystems10` in your browser.
2. Navigate to **Settings → Actions → Workflows → New workflow** (or **Add file → Create new file** in the repo root).
3. Name the new file `.github/workflows/build.yml`.
4. Paste the contents of `ci/github-workflows-build.yml` from this repo.
5. Commit directly to `main` via the web UI.
6. (Optional cleanup) Delete `ci/github-workflows-build.yml` and `ci/README.md` in a follow-up commit.

## Why is the workflow staged here instead of activated?

See `epics/stories/s-ets-04-01-ci-workflow-escalation.md` and `_bmad/adrs/` for the full audit trail. Short summary:

- The local `gh` CLI authentication uses a token without the `workflow` OAuth scope.
- Pushing a file to `.github/workflows/` requires that scope; GitHub returns HTTP 403 otherwise.
- 5 consecutive sprints (Sprint 1 → Sprint 4) carried this user-action item; Sprint 4 closes it formally per Pat's binary-close design + Raze's ESCALATION TERRITORY flag.
- The workflow contents are preserved (not deleted) so that a single `git mv` from a future scope-refreshed session activates CI cleanly.

## Why not just delete the workflow?

Deleting it would lose ~30 lines of (validated, JDK-17, Maven-cache-aware) workflow definition. Preserving it keeps the option of one-line activation cheap.
