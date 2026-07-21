# S-ETS-41-01: TeamEngine 6 Runtime Migration

## Status

IN PROGRESS — MAVEN/IMAGE/STARTUP VERIFIED; LOCAL OSH E2E BLOCKED

## User Instruction

Triggered by: “Derive specs, story, archi, traceability and ADRs to support currently committed and proposed code.”

## Goal

Replace the manually assembled TeamEngine 5.6.1 runtime with an immutable OGC-published TeamEngine 6.0.0 image, align the declared Docker runtime version with the `ets-common:17` TeamEngine 6 compile lineage, and prove the resulting container through the mandatory Maven and real local OSH E2E gates.

## Requirements

- `REQ-ETS-TEAMENGINE-001`
- `REQ-ETS-TEAMENGINE-003`
- `REQ-ETS-TEAMENGINE-004`
- `REQ-ETS-TEAMENGINE-005`
- `REQ-ETS-TEAMENGINE-006`
- `REQ-ETS-TEAMENGINE-007`
- `REQ-ETS-CLEANUP-004`
- `REQ-ETS-CLEANUP-021`

## Scenarios

- `SCENARIO-ETS-TEAMENGINE-TE6-IMAGE-PROVENANCE-001`
- `SCENARIO-ETS-TEAMENGINE-TE6-DEPENDENCY-INVENTORY-001`
- `SCENARIO-ETS-TEAMENGINE-TE6-BASE-IMMUTABILITY-001`
- `SCENARIO-ETS-TEAMENGINE-TE6-RUNTIME-INVARIANTS-001`
- `SCENARIO-ETS-TEAMENGINE-TE6-LOCAL-OSH-E2E-001`
- `SCENARIO-ETS-TEAMENGINE-TE6-CONFIG-ALIGNMENT-001`
- `SCENARIO-ETS-CLEANUP-CONFIDENTIAL-BUILD-CONTEXT-001`

## Acceptance Criteria

- [x] CP-001 defines the change and its acceptance boundary.
- [x] OpenSpec, architecture, ADRs, epic, traceability, story, and sprint contract describe the migration before implementation acceptance.
- [x] The runtime image is an immutable OGC-published TeamEngine 6.0.0 digest and its provenance is recorded.
- [ ] Test-first structural checks cite the applicable Sprint 41 REQ/SCENARIO identifiers and capture failure against the pre-existing diff before the Generator fix, then PASS afterward.
- [x] The image inventory proves required utilities, directories, TeamEngine libraries, ownership, startup command, and effective non-root runtime identity.
- [x] The Dockerfile never modifies, patches, replaces, deletes, or recursively re-owns TeamEngine-owned files; it only adds ETS-owned artifacts at supported extension locations.
- [x] Maven and image inventories justify the explicit base-absent runtime selection; no unreviewed wildcard deletion remains.
- [x] Dockerfile, Compose, POM runtime metadata, and smoke harness configuration are aligned or intentionally documented.
- [x] Confidential-file rules are scoped and verified against tracked files and the Docker build context without printing protected contents.
- [x] Unrelated `f10m.xml` worktree pollution is removed.
- [ ] Final Docker Maven verification completes with exact post-change totals. Earlier pass: `298 / 0 failures / 0 errors / 3 skipped`; final reruns were blocked before tests by Maven Central DNS/repository I/O.
- [x] The image builds, becomes healthy, exposes `ogcapi-connectedsystems10` through TeamEngine SPI/CTL, and contains no startup linkage/registration errors.
- [ ] TeamEngine executes the full deployed suite against the documented running local OSH IUT; exact totals and no-mutation evidence are archived.
- [x] Raze re-reviews the final implementation and reports no unresolved required fixes (`APPROVE_WITH_CONCERNS`, confidence `0.98`).
- [ ] Specification implementation status and operational documents are reconciled only after evidence exists.

## Non-Goals

- Changing OGC conformance assertions or weakening PASS/FAIL/SKIP behavior.
- Treating prior TeamEngine 5.6.1 Sprint 40 evidence as TeamEngine 6 evidence.
- Publishing confidential OGC-supplied files.
- Claiming that changing `docker.teamengine.version` alone proves runtime compatibility.

## Existing Diff Under Evaluation

- `.dockerignore`
- `.gitignore`
- `Dockerfile`
- `pom.xml`
- untracked `f10m.xml` (presumed scratch until documented otherwise)

## Planning Evidence

Raze review `.harness/evaluations/teamengine-6-migration-adversarial-2026-07-20.yaml` returned `GAPS_FOUND`, confidence `0.91`. It found the direction technically plausible and validated the local pinned-image metadata, but required spec/ADR reconciliation, dependency and filesystem inventory, Maven verification, container startup, and real local OSH E2E before completion.

## Implementation Evidence — 2026-07-21

Runtime verifier and confidential history/effective-context checks pass, including byte-for-byte comparison of TeamEngine-owned base files. An earlier Docker Maven run reports `298 tests / 0 failures / 0 errors / 3 skipped`; the final post-review rerun did not execute tests because Maven Central DNS failed once and the retry remained idle in repository I/O for 17 minutes. The digest-pinned revised image builds, runs as uid/gid 1001 `tomcat`, becomes healthy, and registers `ogcapi-connectedsystems10` through authenticated `/teamengine/rest/suites` without linkage or registration errors. A broad dependency-copy attempt failed startup with a Jersey class-identity conflict, and a later inherited copy execution leaked TeamEngine distribution archives; the manifest verifier rejected it. The final image adds only the shaded ETS jar and `teamengine-resources-6.0.0.jar`, relocating NetworkNT/ITU packages to avoid split-version additions. The prior handoff incorrectly claimed that the base supplied resources 6.0.0; it supplies RC2 resources.

Mandatory local OSH E2E remains `NOT_RUN`: this restarted environment has no documented field-hub directory, Docker image, volume, network, container, or credential source. Sprint completion and implementation status remain open.
