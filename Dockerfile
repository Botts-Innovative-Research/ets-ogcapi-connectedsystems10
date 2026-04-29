# syntax=docker/dockerfile:1.6
#
# Dockerfile — ets-ogcapi-connectedsystems10
#
# Multi-stage build per ADR-009 + ADR-007 (Sprint 2 S-ETS-02-05):
#   Stage 1 (builder)  = eclipse-temurin:17-jdk-jammy + Maven 3.9.9 with BuildKit
#                        --mount=type=cache,target=/root/.m2 — full mvn lifecycle
#                        baked into the image build; eliminates host ~/.m2 dependency
#                        Raze s03 CONCERN-2/3 flagged.
#   Stage 2 (runtime)  = tomcat:8.5-jre17 + ADR-007's 3 secondary patches preserved
#                        + non-root USER tomcat (CIS Docker Benchmark §4.1).
#
# REQ-ETS-TEAMENGINE-003 (modified Sprint 2 — multi-stage), REQ-ETS-CLEANUP-004 (NEW
# Sprint 2 — Dockerfile multi-stage + non-root USER).

# =============================================================================
# Stage 1 — builder
# =============================================================================
FROM eclipse-temurin:17-jdk-jammy AS builder

ARG MAVEN_VERSION=3.9.9
# archive.apache.org keeps every release indefinitely; dlcdn.apache.org rotates and
# 3.9.9 was 404 as of 2026-04-28. Pin to archive for build reproducibility per
# ADR-009 §"Risks" base-image-cadence note.
ARG MAVEN_BASE=https://archive.apache.org/dist/maven/maven-3

# Install Maven 3.9.9 + git (pom.xml uses buildnumber-maven-plugin which queries
# the git SCM for revision metadata; without git, the plugin fails). Layer order
# rationale: this layer rarely changes (only when MAVEN_VERSION ARG bumps), so it
# stays cache-warm across most commits.
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl ca-certificates git \
 && curl -fsSL "${MAVEN_BASE}/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
    | tar -xz -C /opt \
 && ln -s /opt/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/local/bin/mvn \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /build

# Copy pom.xml FIRST — BuildKit invalidates this layer only when pom.xml changes,
# which is far less frequent than `src/` changes. The dependency:go-offline pre-warm
# pulls the entire dep tree once; subsequent builds hit the BuildKit /root/.m2 cache.
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q dependency:go-offline -DexcludeArtifactIds=teamengine-spi || true

# Now copy source + .git (.git is required by buildnumber-maven-plugin to populate
# the manifest's Implementation-Build / SCM-Revision attributes). This layer
# invalidates per commit (frequent). The mvn package + dependency:copy-dependencies
# steps run against the warmed BuildKit cache so they stay fast even on cold-CI
# runs (first run downloads deps; subsequent runs hit the cache mount).
#
# `-DskipTests`: the test suite already ran in CI before docker build (per Sprint 1
# convention); re-running tests inside the build stage wastes ~30s per docker build.
#
# `rm -f teamengine-*.jar`: the ets-common:17 transitive depMgmt brings TE 6.0.0
# jars; the runtime TE 5.6.1 WAR we install in stage 2 already provides TE classes.
# Filter the 6.0.0 conflict per ADR-007.
COPY .git ./.git
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q clean package -DskipTests \
 && mvn -B -q dependency:copy-dependencies \
        -DoutputDirectory=target/lib-runtime \
        -DincludeScope=runtime \
 && rm -f target/lib-runtime/teamengine-*.jar

# =============================================================================
# Stage 2 — runtime (tomcat:8.5-jre17 per ADR-007)
# =============================================================================
FROM tomcat:8.5-jre17

LABEL org.opencontainers.image.title="ets-ogcapi-connectedsystems10"
LABEL org.opencontainers.image.description="OGC API - Connected Systems Part 1 ETS in TeamEngine 5.6.1 (JDK 17 base, multi-stage)"
LABEL org.opencontainers.image.source="https://github.com/Botts-Innovative-Research/ets-ogcapi-connectedsystems10"
LABEL org.opencontainers.image.licenses="Apache-2.0"

ARG TEAMENGINE_VERSION=5.6.1
ARG TEAMENGINE_BASE=https://repo.maven.apache.org/maven2/org/opengis/cite/teamengine

# Sprint 4 S-ETS-04-02 (ADR-009 v2 amendment — chown-layer attack):
# Create the tomcat user EARLY (rarely-changes layer) so subsequent COPY/RUN
# steps can use --chown=tomcat:tomcat in-place. Eliminates the post-hoc
# `RUN chown -R tomcat:tomcat /usr/local/tomcat` layer that materialized an
# ~80MB file-attribute COW snapshot of the entire tomcat tree (per Sprint 3
# empirical analysis sprint-ets-03-04-empirical-dedupe-list-2026-04-29.txt).
RUN groupadd -r tomcat && useradd -r -g tomcat -d /usr/local/tomcat -s /sbin/nologin tomcat \
 && chown tomcat:tomcat /usr/local/tomcat

# apt-get + TE WAR download — these layers change only when TEAMENGINE_VERSION ARG
# bumps (rare); placed BEFORE COPY --from=builder to keep the cache warm across most
# commits.
#
# Sprint 4: each `unzip` outputs to a directory we created earlier, then re-chown
# the freshly extracted trees in the same RUN step (so the chown happens BEFORE
# the layer is committed; no second 80MB COW layer).
RUN apt-get update \
 && apt-get install -y --no-install-recommends unzip ca-certificates curl \
 && apt-get clean && rm -rf /var/lib/apt/lists/* \
 && mkdir -p /root/te-stage \
 && cd /root/te-stage \
 && curl -fsSL "${TEAMENGINE_BASE}/teamengine-web/${TEAMENGINE_VERSION}/teamengine-web-${TEAMENGINE_VERSION}.war" -o teamengine-web.war \
 && curl -fsSL "${TEAMENGINE_BASE}/teamengine-web/${TEAMENGINE_VERSION}/teamengine-web-${TEAMENGINE_VERSION}-common-libs.zip" -o teamengine-web-common-libs.zip \
 && curl -fsSL "${TEAMENGINE_BASE}/teamengine-console/${TEAMENGINE_VERSION}/teamengine-console-${TEAMENGINE_VERSION}-base.zip" -o teamengine-console-base.zip \
 && unzip -q teamengine-web.war -d /usr/local/tomcat/webapps/teamengine \
 && unzip -q teamengine-web-common-libs.zip -d /usr/local/tomcat/lib \
 && unzip -q teamengine-console-base.zip -d /usr/local/tomcat/te_base \
 && rm -rf /root/te-stage \
 && chown -R tomcat:tomcat /usr/local/tomcat/webapps /usr/local/tomcat/lib /usr/local/tomcat/te_base \
                          /usr/local/tomcat/conf /usr/local/tomcat/logs /usr/local/tomcat/work \
                          /usr/local/tomcat/temp

# ADR-007 patch 1 — VirtualWebappLoader strip. TE 5.6.1's META-INF/context.xml
# references a Tomcat-7-era class absent from Tomcat 8.5+. Without strip:
# ClassNotFoundException during context-config.
RUN sed -i '/<Loader className="org.apache.catalina.loader.VirtualWebappLoader"/,/\/>/d' \
        /usr/local/tomcat/webapps/teamengine/META-INF/context.xml

# ADR-007 patch 2 — JAXB shared lib. JDK 11+ removed javax.xml.bind.* from the JRE;
# TE 5.6.1's TestSuiteController servlet uses JAXB during init. Without the shared
# jars: TypeNotPresentException: Type javax.xml.bind.JAXBContext not present.
# Sprint 4: chown the freshly-downloaded jars in the same RUN step.
RUN cd /usr/local/tomcat/lib \
 && curl -fsSL "https://repo.maven.apache.org/maven2/javax/xml/bind/jaxb-api/2.3.1/jaxb-api-2.3.1.jar" -o jaxb-api-2.3.1.jar \
 && curl -fsSL "https://repo.maven.apache.org/maven2/com/sun/xml/bind/jaxb-core/2.3.0.1/jaxb-core-2.3.0.1.jar" -o jaxb-core-2.3.0.1.jar \
 && curl -fsSL "https://repo.maven.apache.org/maven2/com/sun/xml/bind/jaxb-impl/2.3.1/jaxb-impl-2.3.1.jar" -o jaxb-impl-2.3.1.jar \
 && curl -fsSL "https://repo.maven.apache.org/maven2/javax/activation/javax.activation-api/1.2.0/javax.activation-api-1.2.0.jar" -o javax.activation-api-1.2.0.jar \
 && chown tomcat:tomcat jaxb-api-2.3.1.jar jaxb-core-2.3.0.1.jar jaxb-impl-2.3.1.jar javax.activation-api-1.2.0.jar

# Stage 1 output — COPY only the runtime artifacts (no build tools, no sources, no
# Maven cache). ADR-007 patch 3 — full deps closure with TE 6.0.0 filter is applied
# in stage 1 before this COPY so the runtime layer carries no clash-prone jars.
#
# IMPORTANT: copy ONLY the slim jar (NOT -aio.jar / -javadoc.jar / -site.jar). The
# aio jar shades transitive deps and would conflict with the loose lib-runtime/
# closure also dropped under WEB-INF/lib. Sprint 1 single-stage pattern preserved.
#
# Sprint 4 (ADR-009 v2 amendment): --chown=tomcat:tomcat embeds ownership in
# the COPY layer itself; no second `chown -R` layer needed.
COPY --from=builder --chown=tomcat:tomcat /build/target/lib-runtime/ /usr/local/tomcat/webapps/teamengine/WEB-INF/lib/
COPY --from=builder --chown=tomcat:tomcat /build/target/ets-ogcapi-connectedsystems10-0.1-SNAPSHOT.jar /usr/local/tomcat/webapps/teamengine/WEB-INF/lib/

# S-ETS-03-04: TE common-libs ↔ deps-closure dedupe per ADR-009 Sprint 3
# amendment + ops/test-results/sprint-ets-03-04-empirical-dedupe-list-2026-04-29.txt.
# Removes the 4 exact-basename overlaps between /usr/local/tomcat/lib (TE
# common-libs from teamengine-web-common-libs.zip) and WEB-INF/lib (our
# mvn dependency:copy-dependencies closure). Same version in both places
# means duplicate code on the classpath; TE's classloader prefers
# /usr/local/tomcat/lib so removing the WEB-INF/lib copies is safe.
#
# Approx. savings: 1.8MB (schema-utils + xercesImpl + xml-apis + xml-resolver).
# This is the SAFE minimum dedupe; intra-WEB-INF/lib duplicate-version dedupe
# (~3-4MB additional) deferred to Sprint 4 with iterative smoke verification.
#
# TE 5.6.1 + ETS 0.1-SNAPSHOT — re-derive on TE version bump per ADR-009.
RUN cd /usr/local/tomcat/webapps/teamengine/WEB-INF/lib \
 && rm -f schema-utils-1.8.jar xercesImpl-2.12.2.jar xml-apis-1.4.01.jar xml-resolver-1.2.jar
COPY --from=builder --chown=tomcat:tomcat /build/target/ets-ogcapi-connectedsystems10-0.1-SNAPSHOT-ctl.zip /tmp/ets-ctl.zip
# Sprint 4: extract + chown in same RUN step (no post-hoc chown layer).
RUN unzip -q -o /tmp/ets-ctl.zip -d /usr/local/tomcat/te_base/scripts \
 && rm /tmp/ets-ctl.zip \
 && chown -R tomcat:tomcat /usr/local/tomcat/te_base/scripts

# Remove the placeholder "note" example suite that ships in console-base; it is
# noise in the suite list and matches the production image's behaviour.
RUN rm -rf /usr/local/tomcat/te_base/scripts/note 2>/dev/null || true

# Sprint 4 S-ETS-04-02 (ADR-009 v2 amendment): standalone post-hoc
# `RUN chown -R tomcat:tomcat /usr/local/tomcat` DELETED. The chown was the
# single largest layer (~80MB COW snapshot of file-attribute changes across
# the entire tomcat tree). Each preceding COPY/RUN now embeds ownership
# in-place via --chown=tomcat:tomcat (COPY) or `chown` in the same RUN as
# the file creation. CIS Docker Benchmark §4.1 non-root USER mandate
# preserved unchanged.
USER tomcat

# TeamEngine env: TE_BASE points at the bundled scripts directory; xerces is the
# DocumentBuilderFactory the engine expects (matches production image config).
ENV JAVA_OPTS="-Xms1024m -Xmx2048m -DTE_BASE=/usr/local/tomcat/te_base -Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl"
ENV CATALINA_OPTS="-Dlog4j2.formatMsgNoLookups=true"

# Tomcat default port. docker-compose.yml maps to host port 8081.
EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=12 \
  CMD curl -fsS -o /dev/null http://localhost:8080/teamengine/ || exit 1

CMD ["catalina.sh", "run"]
