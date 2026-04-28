# Dockerfile for ets-ogcapi-connectedsystems10
#
# REQ-ETS-TEAMENGINE-003: build a TeamEngine container with this ETS preinstalled.
#
# Architectural divergence from architect-handoff (S-ETS-01-03 spec drift,
# documented in ops/server.md "Docker smoke test" section): the architect
# directed `FROM ogccite/teamengine-production:5.6.1`, but
#   1. Docker Hub does not publish a `:5.6.1` tag (only `:latest` and
#      `:1.0-SNAPSHOT`, both 2.45GB images that bundle TE 5.6.1).
#   2. `ogccite/teamengine-production:latest` runs on JDK 8 (`JAVA_VERSION=8u212`)
#      and crashes loading our JDK 17 controller class with
#      `UnsupportedClassVersionError ... class file version 61.0` — confirmed
#      by a smoke run on 2026-04-28T19:28Z.
# The architect-handoff CONCERN #1 ("TeamEngine 5.6.1 vs spec's 5.5") fed
# directly into this divergence.
#
# Resolution: assemble a TeamEngine 5.6.1 webapp on top of `tomcat:8.5-jre17`
# (Tomcat 8.5 = javax.servlet, the API TE 5.6.1's WAR was built against; JRE 17
# satisfies the JDK 17 class-version requirement of our ETS jar). Pull the
# published TE 5.6.1 web WAR / console / common-libs artifacts directly from
# Maven Central, then drop this ETS's transitive deps and CTL scripts in the
# right places. End result: TeamEngine 5.6.1, JDK 17, our JDK 17 ETS jar
# loadable via SPI. Verified empirically 2026-04-28T19:44Z: 12/12 @Test PASS
# against GeoRobotix in 1.6s via the TeamEngine SPI route. Bases tried:
#   tomcat:10.1-jre17 → ClassNotFoundException: VirtualWebappLoader
#                       (Jakarta EE 9 namespace + Tomcat 10's pruned classloaders)
#   tomcat:9-jre17    → same VirtualWebappLoader miss (removed in Tomcat 9.0+)
#   tomcat:8.5-jre17  → context.xml `<Loader>` line still references the missing
#                       class but is removable without functional loss (every
#                       jar already lives under WEB-INF/lib). PICKED.

FROM tomcat:8.5-jre17

LABEL org.opencontainers.image.title="ets-ogcapi-connectedsystems10"
LABEL org.opencontainers.image.description="OGC API - Connected Systems Part 1 ETS in TeamEngine 5.6.1 (JDK 17 base)"
LABEL org.opencontainers.image.source="https://github.com/Botts-Innovative-Research/ets-ogcapi-connectedsystems10"
LABEL org.opencontainers.image.licenses="Apache-2.0"

ARG TEAMENGINE_VERSION=5.6.1
ARG TEAMENGINE_BASE=https://repo.maven.apache.org/maven2/org/opengis/cite/teamengine

# Install unzip + curl (curl is already present in tomcat base, unzip is not).
RUN apt-get update \
 && apt-get install -y --no-install-recommends unzip ca-certificates curl \
 && apt-get clean && rm -rf /var/lib/apt/lists/*

# Download + install TeamEngine 5.6.1 webapp.
RUN mkdir -p /root/te-stage \
 && cd /root/te-stage \
 && curl -fsSL "${TEAMENGINE_BASE}/teamengine-web/${TEAMENGINE_VERSION}/teamengine-web-${TEAMENGINE_VERSION}.war" -o teamengine-web.war \
 && curl -fsSL "${TEAMENGINE_BASE}/teamengine-web/${TEAMENGINE_VERSION}/teamengine-web-${TEAMENGINE_VERSION}-common-libs.zip" -o teamengine-web-common-libs.zip \
 && curl -fsSL "${TEAMENGINE_BASE}/teamengine-console/${TEAMENGINE_VERSION}/teamengine-console-${TEAMENGINE_VERSION}-base.zip" -o teamengine-console-base.zip \
 && unzip -q teamengine-web.war -d /usr/local/tomcat/webapps/teamengine \
 && unzip -q teamengine-web-common-libs.zip -d /usr/local/tomcat/lib \
 && unzip -q teamengine-console-base.zip -d /usr/local/tomcat/te_base \
 && rm -rf /root/te-stage

# TE 5.6.1's META-INF/context.xml references
# `org.apache.catalina.loader.VirtualWebappLoader`, a Tomcat 7 class that is
# absent from Tomcat 8.5+. Since this base image already places every ETS jar
# under WEB-INF/lib (no need for a virtual classpath outside the webapp), the
# `<Loader>` element is removed surgically. Verified empirically: removing the
# element lets Tomcat 8.5 deploy the webapp; leaving it in place causes
# `ClassNotFoundException: VirtualWebappLoader` during context-config.
RUN sed -i '/<Loader className="org.apache.catalina.loader.VirtualWebappLoader"/,/\/>/d' /usr/local/tomcat/webapps/teamengine/META-INF/context.xml

# JDK 11+ removed `javax.xml.bind.*` from the JRE. TE 5.6.1's TestSuiteController
# servlet uses JAXB during init and crashes with `TypeNotPresentException: Type
# javax.xml.bind.JAXBContext not present`. Restore JAXB by dropping the API +
# reference impl jars into Tomcat's shared `lib/` directory (rather than the
# webapp's WEB-INF/lib) so the Jersey servlet's classloader sees them at
# servlet-init time before WEB-INF scanning. The production-docker JDK-8 image
# only ships `jaxb-api-2.3.1.jar` because JDK 8 supplies the impl natively;
# on JDK 17 we must ship API + impl + activation.
RUN cd /usr/local/tomcat/lib \
 && curl -fsSL "https://repo.maven.apache.org/maven2/javax/xml/bind/jaxb-api/2.3.1/jaxb-api-2.3.1.jar" -o jaxb-api-2.3.1.jar \
 && curl -fsSL "https://repo.maven.apache.org/maven2/com/sun/xml/bind/jaxb-core/2.3.0.1/jaxb-core-2.3.0.1.jar" -o jaxb-core-2.3.0.1.jar \
 && curl -fsSL "https://repo.maven.apache.org/maven2/com/sun/xml/bind/jaxb-impl/2.3.1/jaxb-impl-2.3.1.jar" -o jaxb-impl-2.3.1.jar \
 && curl -fsSL "https://repo.maven.apache.org/maven2/javax/activation/javax.activation-api/1.2.0/javax.activation-api-1.2.0.jar" -o javax.activation-api-1.2.0.jar

# Drop in this ETS: the slim jar (contains TestNGController + META-INF/services
# SPI registration), all transitive deps as flat jars, and the CTL wrapper.
#
# NOTE on the deps stage: the maven-assembly-plugin's `deps.xml` produces a
# `-deps.zip` of ~36 jars but DELIBERATELY excludes transitives of
# `org.opengis.cite.teamengine:teamengine-spi` (Jersey 3.x + jakarta APIs)
# because in production they would clash with TeamEngine's bundled Jersey 1.x.
# Our SuiteAttribute.java imports `jakarta.ws.rs.client.Client` (Jersey 3.x)
# at compile time, so the runtime container must supply Jersey 3.x. The
# `target/lib-runtime/` directory is staged by `mvn dependency:copy-dependencies`
# at the repo root before this image builds (see scripts/smoke-test.sh) and
# contains the FULL compile-scope dependency closure — both Jersey 1.x (for
# TeamEngine's own use) and Jersey 3.x (for this ETS). Tomcat 8.5+ tolerates
# both on the classpath because the namespaces differ (`com.sun.jersey.*` vs
# `org.glassfish.jersey.*`).
COPY target/lib-runtime/ /usr/local/tomcat/webapps/teamengine/WEB-INF/lib/
COPY target/ets-ogcapi-connectedsystems10-0.1-SNAPSHOT.jar /usr/local/tomcat/webapps/teamengine/WEB-INF/lib/
COPY target/ets-ogcapi-connectedsystems10-*-ctl.zip /tmp/ets-ctl.zip
RUN unzip -q -o /tmp/ets-ctl.zip -d /usr/local/tomcat/te_base/scripts \
 && rm /tmp/ets-ctl.zip

# TeamEngine env: TE_BASE points at the bundled scripts directory; xerces is the
# DocumentBuilderFactory the engine expects (matches production image config).
ENV JAVA_OPTS="-Xms1024m -Xmx2048m -DTE_BASE=/usr/local/tomcat/te_base -Djavax.xml.parsers.DocumentBuilderFactory=com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl"
ENV CATALINA_OPTS="-Dlog4j2.formatMsgNoLookups=true"

# Remove the placeholder "note" example suite that ships in console-base; it is
# noise in the suite list and matches the production image's behaviour.
RUN rm -rf /usr/local/tomcat/te_base/scripts/note 2>/dev/null || true

# Tomcat default port. docker-compose.yml maps to host port 8081.
EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=12 \
  CMD curl -fsS -o /dev/null http://localhost:8080/teamengine/ || exit 1

CMD ["catalina.sh", "run"]
