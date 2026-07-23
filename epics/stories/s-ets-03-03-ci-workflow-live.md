# S-ETS-03-03: Historical Hosted CI Proposal

**Status**: Retired by CP-003/ADR-012 on 2026-07-23
**Requirement**: REQ-ETS-CLEANUP-007 (retired)

## Historical Record

This story formerly proposed moving a dormant GitHub Actions definition into an
active workflow directory. Authentication constraints prevented activation.

The project owner subsequently confirmed that hosted CI will not be approved.
CP-003/ADR-012 permanently remove that path, and the dormant definition was
deleted. No workflow authorization, definition, dispatch, or run is a project
task.

Current verification is local:

- `bash scripts/mvn-test-via-docker.sh`
- `bash scripts/verify-teamengine6-runtime.sh`
- `bash scripts/smoke-test.sh` against the documented unmodified local OSH IUT

Git history and archived test results retain the original chronology. They are
not executable planning instructions.
