# Architecture Decision Record Index

Last updated: 2026-07-20

| ADR | Decision | Current status |
|---|---|---|
| [ADR-001](ADR-001-teamengine-spi-registration.md) | TeamEngine SPI registration pattern | Accepted; runtime-specific statement amended by ADR-007/011 |
| [ADR-002](ADR-002-json-schema-bundling.md) | JSON Schema bundling mechanism | Accepted |
| [ADR-003](ADR-003-java-package-naming.md) | Java package naming and Maven coordinates | Accepted |
| [ADR-004](ADR-004-archetype-jdk17-modernization-checklist.md) | Archetype JDK 17 modernization | Accepted |
| [ADR-005](ADR-005-cross-repo-relationship-with-frozen-webapp.md) | Frozen web-app cross-repository relationship | Accepted |
| [ADR-006](ADR-006-jersey-1-to-jakarta-port.md) | Jersey 1 to Jakarta/Jersey 3 port | Accepted |
| [ADR-007](ADR-007-dockerfile-base-image-deviation.md) | Manual TeamEngine 5.6.1 runtime | Superseded for forward runtime by ADR-011; historical baseline retained |
| [ADR-008](ADR-008-etsassert-helper-api.md) | ETS assertion helper API | Accepted |
| [ADR-009](ADR-009-multi-stage-dockerfile.md) | Multi-stage Docker build | Partially superseded by ADR-011; builder/non-root principles retained |
| [ADR-010](ADR-010-dependency-skip-verification-strategy.md) | Dependency-skip verification strategy | Accepted |
| [ADR-011](ADR-011-ogc-teamengine-6-runtime-image.md) | OGC-published TeamEngine 6 runtime image | Provisionally accepted decision; implementation/digest verification pending |

ADR-011 is the current authority for the TeamEngine runtime. ADR-007 remains authoritative only for historical TeamEngine 5.6.1 evidence. ADR-009 remains authoritative for builder-stage and non-root principles except where ADR-011 explicitly replaces its runtime-stage decisions.
