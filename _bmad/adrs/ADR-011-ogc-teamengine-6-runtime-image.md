# ADR-011 — OGC-Published TeamEngine 6 Runtime Image

- **Status**: Provisionally accepted decision for Sprint 41; implementation and selected digest acceptance pending verification
- **Date**: 2026-07-20
- **Decider**: Architect (Codex, acting under user instruction)
- **Supersedes**: ADR-007's manual TeamEngine 5.6.1 runtime decision and ADR-009's Tomcat 8.5 runtime stage
- **Preserves**: ADR-009's multi-stage builder, reproducible dependency resolution, minimal runtime artifact copy, and non-root runtime principles
- **Related**: CP-001, S-ETS-41-01, REQ-ETS-TEAMENGINE-001/003/004/005/007, REQ-ETS-CLEANUP-004

## Context

The ETS uses `org.opengis.cite:ets-common:17`, whose dependency management resolves TeamEngine 6.0.0 SPI artifacts and a Jakarta/Jersey 3 lineage. The accepted runtime nevertheless assembles TeamEngine 5.6.1 manually on Tomcat 8.5. That decision was necessary in April 2026 because the assumed versioned production image did not exist and the available production image used JDK 8.

OGC now publishes `ogccite/teamengine-dev` with TeamEngine 6.0.0, JDK 17, and Tomcat 10.1. The current proposed Dockerfile pins the image by digest. Raze confirmed the local digest metadata reports JDK 17.0.15+6, Tomcat 10.1.42, `User=tomcat`, `Cmd=[catalina.sh,run]`, and inherited `TE_BASE`; Raze did not verify image filesystem contents, utilities, library inventory, Docker build success, or E2E behavior.

## Decision

Sprint 41 SHALL use an immutable digest of the OGC-published TeamEngine 6.0.0 image as the runtime stage, subject to Generator verification.

This status accepts the architectural direction only. It does not accept the current Dockerfile, its selected digest, dependency exclusions, or runtime behavior; those remain provisional until every verification gate below passes.

The implementation SHALL:

1. Retain a JDK 17/Maven 3.9 multi-stage builder and copy only the thin ETS jar, CTL resources, and empirically required runtime dependencies.
2. Pin the runtime by digest and record the human-readable TeamEngine, Tomcat, and JDK versions plus a refresh procedure.
3. Run as a non-root user after bounded installation steps.
4. Inventory Maven runtime dependencies and the pinned image libraries before excluding TeamEngine artifacts. Exclusions SHALL be explicit and justified; a broad wildcard is not the architectural target.
5. Align or explicitly document Dockerfile, Compose, Maven Docker profile, and smoke-harness environment behavior.
6. Verify required image utilities and paths rather than assuming `curl`, `unzip`, `WEB-INF/lib`, or `te_base/scripts` exist.
7. Prove suite SPI/CTL registration and real protocol execution against local OSH through the deployed TeamEngine 6 stack.
8. Never modify, patch, replace, delete, or recursively change ownership of TeamEngine-owned base-image files. Installation is strictly additive and limited to ETS-owned jars, explicitly inventoried runtime dependencies, and uniquely named CTL resources at supported extension locations.

## Alternatives Considered

### Continue manual TeamEngine 5.6.1 assembly

Rejected as the forward path. It retains the compile/runtime TeamEngine split and four maintenance-only compatibility interventions.

### Use `ogccite/teamengine-dev:latest`

Rejected because the tag is mutable and insufficient for reproducible CITE review.

### Copy the entire Maven runtime closure

Rejected because TeamEngine-provided libraries duplicated in `WEB-INF/lib` can produce classloader conflicts.

### Remove every `teamengine-*.jar`

Rejected as an unproven blanket rule. The correct exclusion set must follow empirical Maven and image inventories.

## Consequences

### Positive

- Compile and runtime TeamEngine major versions align.
- The ETS uses an OGC-maintained TeamEngine installation instead of rebuilding the engine itself.
- Tomcat/Jakarta runtime lineage matches the modernized ETS dependencies.
- Runtime provenance becomes reproducible through a digest.

### Negative

- The development image has mutable tags and requires a documented digest-refresh process.
- Image layout and utility availability become external-image contracts that must be gated.
- Prior TeamEngine 5.6.1 E2E evidence does not transfer to TeamEngine 6.
- Compose and smoke paths may diverge unless explicitly reconciled.

## Verification Required Before Implementation Status Changes

- Maven unit/lint verification with exact totals.
- Dependency tree plus pinned-image jar/filesystem inventory.
- Docker build, effective-user, healthcheck, SPI/CTL registration, and linkage-error checks.
- Full TeamEngine E2E against the documented local OSH IUT with exact totals and no-mutation evidence.
- Raze review of the final diff and archived evidence.
