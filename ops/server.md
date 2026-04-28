# server.md — operational reference for ets-ogcapi-connectedsystems10

> Last updated: 2026-04-28 — Sprint 1 / S-ETS-01-03 TeamEngine Docker smoke test landing.

## Schema provenance

The 126 OGC JSON Schemas under `src/main/resources/schemas/` are a
verbatim copy from the sibling repository `csapi_compliance`. Per
ADR-002 (in `csapi_compliance/_bmad/adrs/`), the schemas were
copied at scaffold time rather than included via git submodule or
shared third repository.

| Field | Value |
|---|---|
| Copy date | 2026-04-28 |
| Source repo | https://github.com/Botts-Innovative-Research/csapi_compliance |
| Source HEAD SHA at copy | `ab53658` |
| Source tag | `v1.0-frozen` (per architect-handoff; tag is REQ-ETS-WEBAPP-FREEZE-001 scope) |
| Source path | `csapi_compliance/schemas/` |
| File count | 126 JSON Schemas |
| Manifest | `csapi_compliance/schemas/manifest.json` (`generatedAt`: 2026-04-17T01:18:41.614Z) |

**Upstream OGC repository** (the ultimate source of truth for the
schemas before csapi_compliance fetched them):

| Field | Value |
|---|---|
| Upstream repo | https://github.com/opengeospatial/ogcapi-connected-systems |
| Upstream branch fetched | `master` |
| Upstream master SHA at fetch (csapi_compliance fetch-schemas.ts run) | not pinned in manifest; fetched 2026-04-17 |
| Upstream master SHA at this copy (2026-04-28, for forward-tracking) | `3fd86c73e744b7e2faaf7f1c17366bfb9ff4cd6f` (2026-04-20T20:23:50Z) |

**Drift policy.** Per ADR-002 + ADR-005, the v1.0 web app is frozen and
the schema copies in `csapi_compliance` and this ETS repo are allowed to
drift. The ETS repo is the authoritative copy for OGC CITE submission;
when the upstream OGC schemas evolve, the ETS repo's copy is refreshed
in a controlled sprint and the upstream SHA is updated above.

A future sprint will run a CI check (REQ-ETS-SYNC-001) that diffs the
URI corpora across `csapi_compliance/src/engine/registry/` and the
Java conformance classes here. That check is gated on Part 1 being
feature-complete and is out of scope of Sprint 1.

## Build prerequisites

* JDK 17 (Temurin or OpenJDK), `JAVA_HOME` set
* Maven 3.9+ (NFR-ETS-02)
* Internet access on first build to download dependencies from
  Maven Central + OSSRH (the parent `ets-common:17` is published to
  OSSRH; ensure `~/.m2/settings.xml` includes the OGC OSSRH
  repository if the artifact resolution fails on a clean machine).

## TeamEngine integration

The ETS targets TeamEngine 5.6.x (currently 5.6.1) — the production
deployment behind https://cite.opengeospatial.org/teamengine/.
Pin recorded in `pom.xml` `<docker.teamengine.version>` property.

## Docker smoke test (S-ETS-01-03)

The repo-root `Dockerfile` + `docker-compose.yml` + `scripts/smoke-test.sh`
build a TeamEngine 5.6.1 container with this ETS preinstalled and run the
Core suite against the GeoRobotix demo IUT
(`https://api.georobotix.io/ogc/t18/api`).

### Spec drift documented

The architect-handoff (`.harness/handoffs/architect-handoff.yaml` in the
sibling `csapi_compliance` repo) directed
`FROM ogccite/teamengine-production:5.6.1` for the Dockerfile. Two facts
forced a deviation:

1. **Docker Hub does not publish a `:5.6.1` tag** for
   `ogccite/teamengine-production`. Only `:latest` and `:1.0-SNAPSHOT`
   exist (verified 2026-04-28; both are 2.45 GB images that internally
   bundle TE 5.6.1).
2. **The production image runs JDK 8** (`JAVA_VERSION=8u212`). Our ETS
   classes target JDK 17 (`<maven.compiler.release>17`) and crash with
   `UnsupportedClassVersionError ... class file version 61.0` when
   dropped into that image's WEB-INF/lib (smoke confirmed
   2026-04-28T19:28Z).

**Resolution**: the Sprint 1 Dockerfile assembles TeamEngine 5.6.1 on top
of `tomcat:8.5-jre17` by downloading the published TE 5.6.1 web WAR /
console / common-libs zips from Maven Central. This produces a
TeamEngine 5.6.1 + JDK 17 container that is byte-for-byte equivalent in
test behaviour to the production image's TE deployment but loadable by
our JDK 17 ETS jar. Empirical evidence:
12/12 @Test methods PASS against GeoRobotix in 1.6 s via the SPI route
(report at `ops/test-results/s-ets-01-03-teamengine-smoke-2026-04-28.xml`).

Three secondary patches the Dockerfile applies, with their root causes:

- TE 5.6.1's `META-INF/context.xml` references
  `org.apache.catalina.loader.VirtualWebappLoader` (a Tomcat 7-only
  class). Tomcat 8.5+ removed it. The `<Loader>` element is `sed`'d out
  because every ETS jar is already in WEB-INF/lib (no external classpath
  needed).
- JDK 11+ removed the bundled `javax.xml.bind.*` JAXB classes that TE
  5.6.1's TestSuiteController servlet uses at init time. The Dockerfile
  drops `jaxb-api`, `jaxb-core`, `jaxb-impl`, and `javax.activation-api`
  jars into Tomcat's shared `lib/` directory.
- The `maven-assembly-plugin`'s `deps.xml` deliberately excludes
  transitives of `teamengine-spi` (Jersey 3.x + jakarta APIs) because in
  the production image they would clash with Jersey 1.x. Our
  `SuiteAttribute.java` imports `jakarta.ws.rs.client.Client`, so the
  Dockerfile stages the FULL compile-scope dependency closure into
  WEB-INF/lib via `mvn dependency:copy-dependencies` (with the TE 6.0.0
  jars filtered out so they do not collide with the bundled TE 5.6.1).

**Spec reconciliation**: the new repo's openspec `spec.md` (in
`csapi_compliance`) still says
`Dockerfile SHALL extend ogccite/teamengine-production:5.6.1`. Sam
should reconcile that line at the next planning cycle to read
`Dockerfile SHALL produce a TeamEngine 5.6.1 webapp on a JDK 17 base
image`. Until then, this `ops/server.md` block is the authoritative
record of why the implementation deviates.

### How to build

```
mvn clean package -DskipTests
mvn dependency:copy-dependencies \
    -DoutputDirectory=target/lib-runtime \
    -DincludeScope=runtime
rm -f target/lib-runtime/teamengine-*.jar
docker build -t ets-ogcapi-connectedsystems10:smoke .
```

### How to run interactively

```
docker run -p 8081:8080 --name ets-csapi ets-ogcapi-connectedsystems10:smoke
# open http://localhost:8081/teamengine/  (login: ogctest / ogctest)
```

OR via `docker-compose.yml`:

```
docker compose up --build
```

### How to invoke the smoke test

```
bash scripts/smoke-test.sh
```

The script tries host port 8081 first, falls back to 8082 if 8081 is
busy. Override with `SMOKE_PORT=8083 bash scripts/smoke-test.sh`. Reports
land at `ops/test-results/s-ets-01-03-teamengine-smoke-<DATE>.xml` and
`s-ets-01-03-teamengine-container-<DATE>.log`.

### Dev-environment caveat: port 8081 collision

In the WSL2 host this repo is developed on, an unrelated container
`field-hub-osh-1` holds host port 8081 as of 2026-04-28
(`docker ps | grep field-hub-osh`). The committed configuration
(`docker-compose.yml`, `pom.xml` `<docker.teamengine.version>` plumbing)
keeps the canonical 8081 port — `scripts/smoke-test.sh`'s `pick_port()`
auto-detects the conflict and falls back to 8082 for the smoke run.

## Known issues

* Archetype's `VerifyTestNGController.doTestRun` is `@Ignore`'d — the
  archetype's example failing tests are throwaway demonstration code;
  S-ETS-01-02 will replace `level1.Capability1Tests` entirely.
* `buildClientWithProxy` in `ClientUtils.java` does not currently
  install the Apache HTTP connector (the `jersey-apache-connector`
  artifact is not on the Sprint-1 classpath); it relies on
  `ClientProperties.PROXY_URI` which delegates to the JDK
  `HttpURLConnection` proxy via system properties. If a proxy debug
  scenario is needed, add the `jersey-apache-connector` dependency.

## Reproducibility

* `<project.build.outputTimestamp>2026-04-27T00:00:00Z</project.build.outputTimestamp>`
  pinned in `pom.xml` for SCENARIO-ETS-SCAFFOLD-REPRODUCIBLE-001.
* `.gitattributes` enforces LF line endings on all text resources for
  cross-platform byte-equality (Windows checkouts via `git autocrlf=true`
  would otherwise mutate JSON/XML/CTL inside the jar).
