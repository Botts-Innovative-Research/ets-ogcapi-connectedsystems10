# syntax=docker/dockerfile:1.6
#
# Dockerfile — ets-ogcapi-connectedsystems10
#
# Multi-stage build:
#   Stage 1 (builder)  = Maven 3.9 / Eclipse Temurin 17 with BuildKit
#                        --mount=type=cache,target=/root/.m2 — full mvn lifecycle
#                        baked into the image build; eliminates host ~/.m2 dependency.
#   Stage 2 (runtime)  = ogccite/teamengine-dev (OGC-published TeamEngine 6.0.0 on
#                        Tomcat 10.1.42 / JDK 17). We add only our ETS jar, its
#                        one reviewed runtime resource jar, and our CTL scripts.
#
# REQ-ETS-TEAMENGINE-007;
# SCENARIO-ETS-TEAMENGINE-TE6-BASE-IMMUTABILITY-001.
# The runtime stage deliberately makes NO modifications to the TeamEngine
# installation itself. Assembling a bespoke TeamEngine (the previous approach,
# ADR-007) put this ETS out of scope for CITE review: an ETS should contribute
# tests and resources, not alter the engine every other ETS shares.
#
# ADR-007 is superseded. Its premise — that OGC published no JDK 17 TeamEngine
# image — was already false when written; ogccite/teamengine-dev has shipped
# TE 6.0.0 / Tomcat 10.1 / JDK 17 since 2025-06-25. The three secondary patches
# it ratified (VirtualWebappLoader strip, JAXB shared libs, TE 6.0.0 jar filter)
# existed only to make TE 5.6.1 boot on a hand-built Tomcat 8.5 base and are all
# unnecessary here.
#
# REQ-ETS-TEAMENGINE-003, REQ-ETS-CLEANUP-004.

# =============================================================================
# Stage 1 — builder
# =============================================================================
# Match the mandatory Maven verification image. The digest fixes the builder toolchain,
# and this Debian-based image includes git for buildnumber-maven-plugin.
FROM maven:3.9-eclipse-temurin-17@sha256:1ed5d1f54416b706707b4f3238f63a20bb06aab27c6d240090a2bb9ad895ed45 AS builder

WORKDIR /build

# Copy pom.xml FIRST so source-only changes do not invalidate dependency metadata.
# The package step uses a persistent BuildKit Maven cache; a separate
# dependency:go-offline step is intentionally avoided because it resolves unrelated
# report/deploy artifacts and can stall an otherwise buildable image.
COPY pom.xml ./
COPY scripts/bootstrap-swecommon30-validator.sh ./scripts/

# Now copy source + .git (.git is required by buildnumber-maven-plugin to populate
# the manifest's Implementation-Build / SCM-Revision attributes). This layer
# invalidates per commit (frequent).
#
# `-DskipTests`: the mandatory local Docker Maven gate runs before image build;
# re-running tests inside the build stage wastes ~30s per docker build.
#
# The pinned image already supplies the runtime closure, often from Tomcat's
# parent classloader. Copying those jars again into WEB-INF/lib creates class-
# identity failures. The Maven shade execution relocates the newer NetworkNT
# validator required by this ETS into the ETS jar. The base already supplies the
# TeamEngine resources coordinate as RC2, so adding the GA jar would duplicate
# every functional path in that artifact family.
COPY .git ./.git
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    bash scripts/bootstrap-swecommon30-validator.sh \
 && mvn -B -q clean package -DskipTests

# Verification-only stage. The runtime verifier targets this stage to inspect the
# effective Docker context after .dockerignore processing without exposing file
# contents in logs. The production runtime remains the final stage below.
FROM ogccite/teamengine-dev@sha256:981b71566d56434576843798ae8072db15be8478eb7dc724b051c2228460f43c AS build-context-audit
COPY . /context

# =============================================================================
# Stage 2 — runtime (OGC-published TeamEngine 6.0.0)
# =============================================================================
#
# Pinned by digest, not tag. ogccite/teamengine-dev publishes only the mutable
# tags `latest` and `1.0-SNAPSHOT` — no versioned tag exists for any TeamEngine
# image — so a tag reference is not reproducible.
#
# Digest below == ogccite/teamengine-dev:latest as of 2026-07-20
# (JAVA_VERSION jdk-17.0.15+6, TOMCAT_VERSION 10.1.42, TeamEngine 6.0.0).
# To refresh: docker pull ogccite/teamengine-dev:latest
#            docker inspect ogccite/teamengine-dev:latest --format '{{index .RepoDigests 0}}'
FROM ogccite/teamengine-dev@sha256:981b71566d56434576843798ae8072db15be8478eb7dc724b051c2228460f43c

LABEL org.opencontainers.image.title="ets-ogcapi-connectedsystems10"
LABEL org.opencontainers.image.description="OGC API - Connected Systems 1.0 ETS with partial Part 1 and Part 2 coverage on OGC TeamEngine 6.0.0"
LABEL org.opencontainers.image.source="https://github.com/Botts-Innovative-Research/ets-ogcapi-connectedsystems10"
LABEL org.opencontainers.image.licenses="Apache-2.0"

# Copy only our slim shaded ETS jar (not -aio.jar / -javadoc.jar / -site.jar). The
# aio jar shades the full transitive closure and would conflict with libraries
# already supplied by TeamEngine.
COPY --from=builder --chown=tomcat:tomcat /build/target/ets-ogcapi-connectedsystems10-0.1-SNAPSHOT.jar /usr/local/tomcat/webapps/teamengine/WEB-INF/lib/

# Our CTL scripts, alongside the suites the base image already ships.
COPY --from=builder --chown=tomcat:tomcat /build/target/ets-ogcapi-connectedsystems10-0.1-SNAPSHOT-ctl.zip /tmp/ets-ctl.zip
RUN test ! -e /usr/local/tomcat/te_base/scripts/ogcapi-connectedsystems10 \
 && mkdir /tmp/ets-ctl \
 && unzip -q /tmp/ets-ctl.zip -d /tmp/ets-ctl \
 && test -d /tmp/ets-ctl/ogcapi-connectedsystems10 \
 && cp -R /tmp/ets-ctl/ogcapi-connectedsystems10 /usr/local/tomcat/te_base/scripts/ \
 && rm -rf /tmp/ets-ctl /tmp/ets-ctl.zip

# Explicitly restate the inherited non-root identity as a structural guardrail.
USER tomcat

# JAVA_OPTS / CATALINA_OPTS are inherited from the base image unchanged; the
# TeamEngine maintainers set TE_BASE and the heap there. Do not override.

# Tomcat default port. docker-compose.yml maps to host port 8081.
EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=12 \
  CMD curl -fsS -o /dev/null http://localhost:8080/teamengine/ || exit 1
