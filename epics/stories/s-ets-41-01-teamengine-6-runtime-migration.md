# S-ETS-41-01: TeamEngine 6 Runtime Migration

## Status

COMPLETE

## User Instruction

Triggered by: “Derive specs, story, archi, traceability and ADRs to support currently committed and proposed code.”

## Goal

Replace the manually assembled TeamEngine 5.6.1 runtime with an immutable OGC-published TeamEngine 6.0.0 image, align the declared Docker runtime version with the `ets-common:17` TeamEngine 6 compile lineage, and prove the resulting container through the mandatory Maven and real local OSH E2E gates.

## Requirements

- `REQ-ETS-TEAMENGINE-001`
- `REQ-ETS-TEAMENGINE-002`
- `REQ-ETS-TEAMENGINE-003`
- `REQ-ETS-TEAMENGINE-004`
- `REQ-ETS-TEAMENGINE-005`
- `REQ-ETS-TEAMENGINE-006`
- `REQ-ETS-TEAMENGINE-007`
- `REQ-ETS-TEAMENGINE-008`
- `REQ-ETS-CLEANUP-004`
- `REQ-ETS-CLEANUP-021`

## Scenarios

- `SCENARIO-ETS-TEAMENGINE-TE6-IMAGE-PROVENANCE-001`
- `SCENARIO-ETS-TEAMENGINE-TE6-DEPENDENCY-INVENTORY-001`
- `SCENARIO-ETS-TEAMENGINE-TE6-BASE-IMMUTABILITY-001`
- `SCENARIO-ETS-TEAMENGINE-TE6-RUNTIME-INVARIANTS-001`
- `SCENARIO-ETS-TEAMENGINE-TE6-LOCAL-OSH-E2E-001`
- `SCENARIO-ETS-TEAMENGINE-TE6-CONFIG-ALIGNMENT-001`
- `SCENARIO-ETS-TEAMENGINE-RUN-ARG-CONTRACT-001`
- `SCENARIO-ETS-TEAMENGINE-PUBLIC-METADATA-001`
- `SCENARIO-ETS-TEAMENGINE-MAVEN-DOCKER-PROFILE-001`
- `SCENARIO-ETS-CLEANUP-CONFIDENTIAL-BUILD-CONTEXT-001`

## Acceptance Criteria

- [x] CP-001 defines the change and its acceptance boundary.
- [x] OpenSpec, architecture, ADRs, epic, traceability, story, and sprint contract describe the migration before implementation acceptance.
- [x] The runtime image is an immutable OGC-published TeamEngine 6.0.0 digest and its provenance is recorded.
- [x] Test-first structural checks cite the applicable Sprint 41 REQ/SCENARIO identifiers and cover the Raze policy findings. Separate pre-fix failure output was not captured before implementation; the checks assert the exact stale surfaces that existed before this remediation.
- [x] CTL, TestNG defaults, Java/docs, README, site docs, Javadoc, sample test-run-props, and smoke documentation use canonical run arguments: `iut`, `auth-credential`, `mutation-tests-enabled`, and `mutation-iut-policy`.
- [x] Public TeamEngine metadata and documentation, including POM-derived suite title/description, Dockerfile labels, Compose comments, and smoke title assertion, describe actual partial OGC 23-001 Part 1 and implemented partial OGC 23-002 Part 2 coverage without archetype placeholders or stale profile guidance.
- [x] The Maven `docker` profile is removed, and structural tests prevent an independent Fabric8/broad-dependency-copy TeamEngine runtime from reappearing.
- [x] Every supported Jenkinsfile selects JDK 17, runs the source-pin bootstrap,
  and requests only Maven profiles declared by this project; structural tests
  cover all Jenkinsfiles.
- [x] The image inventory proves required utilities, directories, TeamEngine libraries, ownership, startup command, and effective non-root runtime identity.
- [x] The Dockerfile never modifies, patches, replaces, deletes, or recursively re-owns TeamEngine-owned files; it only adds ETS-owned artifacts at supported extension locations.
- [x] Maven and image inventories prove the final image adds no cross-version duplicate TeamEngine coordinate family or unreviewed functional path; no wildcard deletion remains. The self-test creates at least two rationale-allowlisted paths and asserts the complete exact emitted tuple set.
- [x] Dockerfile, Compose, POM runtime metadata, and smoke harness configuration are aligned or intentionally documented.
- [x] Confidential-file rules are scoped and verified against tracked files and the Docker build context without printing protected contents.
- [x] Unrelated `f10m.xml` worktree pollution is removed.
- [x] Latest Docker Maven verification completes with exact post-change totals: `303 tests / 0 failures / 0 errors / 3 skipped` archived at `ops/test-results/sprint-ets-41-readiness-maven-2026-07-21.txt`; earlier post-policy artifact `ops/test-results/sprint-ets-41-policy-guidance-maven-2026-07-21.txt` has the same totals.
- [x] The post-policy image builds, becomes healthy, exposes `ogcapi-connectedsystems10` through TeamEngine SPI/CTL, and contains no startup linkage/registration errors. 2026-07-21 readiness pass registered amd64 binfmt support on this linux/arm64 host, built the digest-pinned TeamEngine 6 image, passed the runtime verifier, and verified `docker compose` health plus suite metadata.
- [x] Exact replacement image `sha256:829a97414c07dd5763ed302e32b3178d301ca098bc9025f4b1f58b692ddad5f9` executes the full deployed suite against local OSH and archives direct image-ID, `211/69/0/142`, zero-write, and startup evidence.
- [x] Raze re-reviewed the policy-guidance implementation: RF-002/RF-003/RF-004 are closed and RF-001 is cleared by the 2026-07-22 primary local OSH E2E evidence.
- [x] Raze re-reviewed the readiness reconciliation: initial report `GAPS_FOUND` at `.harness/evaluations/sprint-ets-41-readiness-adversarial-2026-07-21.yaml`; focused recheck `APPROVE_WITH_CONCERNS`, confidence `0.91`, at `.harness/evaluations/sprint-ets-41-readiness-adversarial-recheck-2026-07-21.yaml`. Documentation findings `RAZE-S41-READINESS-002` through `-005` are closed; the 2026-07-22 final local OSH run cleared `RAZE-S41-READINESS-001`.
- [x] Final Raze returns no unresolved required fixes and specifications/operations are reconciled to the replacement-image gates. The final recheck approved at `0.99` confidence with no required actions.

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

Runtime verifier and confidential history/effective-context checks pass, including byte-for-byte comparison of TeamEngine-owned base files. A broad dependency-copy attempt failed startup with a Jersey class-identity conflict, and a later inherited copy execution leaked TeamEngine distribution archives; the manifest verifier rejected it. Final Raze then found that the selected `teamengine-resources-6.0.0.jar` duplicated all 89 functional paths from the base's RC2 resource jar. The replacement image removes that jar and selected payload entirely, adds only the shaded ETS jar and uniquely named CTL tree, and verifies coordinate-family parity with the base.

The 2026-07-22 replacement recheck found that this parity check still depended on
`teamengine-*.jar` filenames and did not intersect added-jar contents with base
jars. It also found root NetworkNT message-bundle collisions in the shaded ETS
jar. The story remains in progress until generic metadata/content scanning,
resource isolation, fresh Maven/runtime/E2E evidence, and final Raze recheck pass.

Mandatory local OSH E2E was completed on 2026-07-22 after restoring a real
OpenSensorHub 2.0.1 ConSys target at the canonical network URL. The clean-clone
TeamEngine 6 run passed `211/69/0/142`; the no-mutation oracle recognized 135
IUT requests and found zero writes. The exact E2E image also passed the runtime
verifier, including valid/invalid SWE Common adapter execution.

## Policy-Guidance Remediation Scope — 2026-07-21

Architect handoff `.harness/handoffs/architect-handoff.yaml` and Raze report `.harness/evaluations/teamengine-policy-guidance-adversarial-2026-07-21.yaml` add the following required remediation before Sprint 41 can close:

- Fix run-argument drift: the serialized TestNG contract is required `iut` plus optional `auth-credential`, `mutation-tests-enabled`, and `mutation-iut-policy`; no `iut-url`, `auth-type`, or `ics` run argument is supported by this story.
- Retire the stale Maven `docker` profile as an alternate TeamEngine runtime or prove it delegates to the digest-pinned Dockerfile path without broad dependency copying.
- Replace archetype placeholders and stale profile/scope text in Maven-derived suite metadata, Dockerfile labels, Compose comments, CTL, TeamEngine config, README, site docs, Javadoc, and sample test-run-props with actual Connected Systems scope and TeamEngine 6 status.
- Preserve status honesty at that checkpoint: post-remediation Maven and local OSH TeamEngine 6 E2E evidence were required before the story could move beyond `IN PROGRESS`; both are now archived.

## Policy-Guidance Remediation Evidence — 2026-07-21

- Focused structural verification: `VerifyTeamEngine6Packaging`, `9 tests / 0 failures / 0 errors / 0 skipped`; final post-doc-readiness artifact `ops/test-results/sprint-ets-41-readiness-focused-packaging-2026-07-21.txt`.
- Earlier post-policy Docker Maven verification: `bash scripts/mvn-test-via-docker.sh`, `303 tests / 0 failures / 0 errors / 3 skipped`, artifact `ops/test-results/sprint-ets-41-policy-guidance-maven-2026-07-21.txt`.
- Gate 2 evaluator `.harness/evaluations/sprint-ets-41-policy-guidance-evaluator-2026-07-21.yaml` returned `CONCERNS`; the required POM-derived suite title/description and smoke title assertion follow-up was implemented and covered by the rerun focused/full Maven evidence above.
- Readiness Docker Maven verification: `303 tests / 0 failures / 0 errors / 3 skipped`, artifact `ops/test-results/sprint-ets-41-readiness-maven-2026-07-21.txt`.
- Readiness Docker image build: `PASS`, artifact `ops/test-results/sprint-ets-41-readiness-docker-build-2026-07-21.txt`.
- Readiness runtime verifier: `PASS`, artifact `ops/test-results/sprint-ets-41-readiness-runtime-verifier-2026-07-21.txt`.
- Readiness Compose startup: `PASS`, artifact `ops/test-results/sprint-ets-41-readiness-compose-2026-07-21.txt`.
- Local OSH TeamEngine E2E: final PASS `211/69/0/142`; artifacts `ops/test-results/sprint-ets-42-swecommon-local-osh-final-2026-07-22.xml`, `ops/test-results/sprint-ets-42-swecommon-local-osh-final-container-2026-07-22.log`, and `ops/test-results/sprint-ets-42-swecommon-local-osh-final-no-mutation-2026-07-22.txt`. The earlier 2026-07-21 absence artifact remains historical blocker evidence.
- Advisory GeoRobotix TeamEngine run: `FAIL`, `211 total / 38 passed / 34 failed / 139 skipped`, artifacts `ops/test-results/sprint-ets-41-readiness-georobotix-smoke-2026-07-21.xml`, `ops/test-results/sprint-ets-41-readiness-georobotix-container-2026-07-21.log`, `ops/test-results/sprint-ets-41-readiness-georobotix-no-mutation-2026-07-21.txt`, and `ops/test-results/sprint-ets-41-readiness-georobotix-write-counts-2026-07-21.txt`; explicit write counts are `POST=0`, `PUT=0`, `PATCH=0`, `DELETE=0`.
- Initial implementation Raze `.harness/evaluations/sprint-ets-41-policy-guidance-adversarial-implementation-2026-07-21.yaml` returned `GAPS_FOUND`; local RF-002/RF-003/RF-004 fixes were implemented. Focused Raze recheck `.harness/evaluations/sprint-ets-41-policy-guidance-adversarial-recheck-2026-07-21.yaml` returned `APPROVE_WITH_CONCERNS`, confidence `0.93`; the final local OSH run subsequently cleared RF-001.
- Readiness Raze `.harness/evaluations/sprint-ets-41-readiness-adversarial-2026-07-21.yaml` returned `GAPS_FOUND`; after runbook/story/handoff architecture fixes, focused recheck `.harness/evaluations/sprint-ets-41-readiness-adversarial-recheck-2026-07-21.yaml` returned `APPROVE_WITH_CONCERNS`, confidence `0.91`; the final local OSH run subsequently cleared its sole remaining blocker.
