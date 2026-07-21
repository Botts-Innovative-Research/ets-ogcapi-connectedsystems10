# CP-001: Adopt the OGC-Published TeamEngine 6 Runtime

> Status: Accepted for planning | Author: Codex (acting Planner/Architect) | Date: 2026-07-20

## References

- Story: `epics/stories/s-ets-41-01-teamengine-6-runtime-migration.md`
- Spec: `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- Requirements: `REQ-ETS-TEAMENGINE-001`, `REQ-ETS-TEAMENGINE-003`, `REQ-ETS-TEAMENGINE-004`, `REQ-ETS-TEAMENGINE-005`, `REQ-ETS-TEAMENGINE-006`, `REQ-ETS-TEAMENGINE-007`, `REQ-ETS-CLEANUP-021`
- Architecture: `_bmad/architecture.md` v2.0.6
- Decision: `_bmad/adrs/ADR-011-ogc-teamengine-6-runtime-image.md`
- Adversarial discovery: `.harness/evaluations/teamengine-6-migration-adversarial-2026-07-20.yaml`

## Problem

The ETS compiles through `ets-common:17`, whose dependency management resolves the TeamEngine 6.0.0 SPI, while the accepted runtime architecture manually assembles TeamEngine 5.6.1 on Tomcat 8.5. That split requires compatibility patches and makes runtime linkage depend on an unverified cross-version SPI assumption. OGC now publishes a JDK 17, Tomcat 10.1, TeamEngine 6.0.0 development image. An existing uncommitted Docker/POM change adopts that image, but it currently contradicts the published requirement and ADR chain and has no migration-specific verification evidence.

## Proposed Change

1. Replace the version-specific TeamEngine 5.6.1 runtime requirement with an immutable, OGC-published TeamEngine 6.0.0 runtime image compatible with the `ets-common:17` compile lineage.
2. Retain the multi-stage JDK 17/Maven builder and non-root runtime constraints.
3. Require an empirical inventory before excluding any TeamEngine dependency from the ETS runtime closure; wildcard removal alone is not acceptable evidence.
4. Require Dockerfile, Compose, Maven Docker profile, and smoke harness to use a documented and behaviorally aligned runtime configuration.
5. Require Maven verification, image build/startup checks, SPI/CTL suite registration, and real local OSH E2E evidence before changing implementation status to IMPLEMENTED.
6. Treat confidential OGC reference files and unrelated scratch POMs as hygiene inputs, not runtime source.
7. Prohibit modification, patching, replacement, deletion, or recursive ownership changes of TeamEngine-owned base-image files; permit only additive ETS artifacts in supported extension locations.

The change is accepted against `SCENARIO-ETS-TEAMENGINE-TE6-IMAGE-PROVENANCE-001`, `-DEPENDENCY-INVENTORY-001`, `-BASE-IMMUTABILITY-001`, `-RUNTIME-INVARIANTS-001`, `-CONFIG-ALIGNMENT-001`, `-LOCAL-OSH-E2E-001`, and `SCENARIO-ETS-CLEANUP-CONFIDENTIAL-BUILD-CONTEXT-001`.

## Impact

- **Spec changes**: modernize `REQ-ETS-TEAMENGINE-001` and `REQ-ETS-TEAMENGINE-003`; add `REQ-ETS-TEAMENGINE-007`, `REQ-ETS-CLEANUP-021`, and corresponding TeamEngine 6/hygiene acceptance scenarios.
- **Architecture changes**: replace the local runtime topology with the OGC-published TeamEngine 6 image and reconcile the compile/runtime dependency model.
- **ADR changes**: ADR-011 supersedes ADR-007's manual TeamEngine 5.6.1 runtime and ADR-009's Tomcat 8.5 runtime stage while preserving ADR-009's multi-stage builder principles.
- **Code changes proposed**: `.dockerignore`, `.gitignore`, `Dockerfile`, `pom.xml`, and potentially `docker-compose.yml` plus structural verification tests.
- **Test changes required**: image-invariant checks, dependency/image inventory evidence, Maven unit/lint verification, Docker build/startup/registration verification, and clean local OSH TeamEngine E2E.
- **Operational changes**: reconcile `ops/server.md`, `ops/test-results.md`, `ops/status.md`, `ops/changelog.md`, and handoffs after verification.

## Alternatives Considered

- **Keep manual TeamEngine 5.6.1 assembly**: rejected as the forward path because compile/runtime TeamEngine versions remain split and the compatibility patches are maintenance burden.
- **Use a mutable TeamEngine image tag**: rejected because a CITE-reviewable runtime must be reproducible.
- **Copy all Maven runtime dependencies into TeamEngine**: rejected because duplicate TeamEngine libraries can create classloader conflicts.
- **Remove all `teamengine-*.jar` files by wildcard without inventory**: rejected; exclusions must be explicit and empirically justified.

## Acceptance Boundary

Acceptance of this proposal authorizes planning and reconciliation. It does not establish that the current uncommitted Dockerfile builds, starts, registers the suite, or passes E2E. Those claims require the Sprint 41 Generator evidence.
