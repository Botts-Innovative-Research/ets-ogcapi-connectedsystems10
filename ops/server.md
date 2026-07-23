# server.md - operational reference for ets-ogcapi-connectedsystems10

> Last updated: 2026-07-22 - Sprint 42 primary local OSH gate restoration.

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

A future sprint will run a local audit check (REQ-ETS-SYNC-001) that diffs the
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

The forward development runtime is the immutable OGC TeamEngine 6 image pinned
by digest in `Dockerfile` and `pom.xml`:

- `ogccite/teamengine-dev@sha256:981b71566d56434576843798ae8072db15be8478eb7dc724b051c2228460f43c`
- TeamEngine SPI/core: 6.0.0
- Runtime user: `tomcat`
- JDK/Tomcat inherited from the base image: JDK 17.0.15+6, Tomcat 10.1.42

Sprint 1/2 TeamEngine 5.6.1 evidence is historical baseline evidence only.
New runtime work must follow ADR-011 and `REQ-ETS-TEAMENGINE-007/008`.

## Docker smoke test (S-ETS-01-03)

The repo-root `Dockerfile` + `docker-compose.yml` + `scripts/smoke-test.sh`
build a TeamEngine 6 container with this ETS preinstalled and run the suite
against the configured IUT. As of Sprint 32, the development default is
the self-provisioned local OSH target on Docker network `field-hub_default`.
GeoRobotix (`https://api.georobotix.io/ogc/t18/api`) is available only as an
explicit advisory interoperability probe with `SMOKE_TARGET=georobotix`.

The linux/arm64 host uses amd64 emulation registered with `tonistiigi/binfmt`
because the pinned OGC TeamEngine digest is linux/amd64-only. On 2026-07-22 the
local OSH target was restored as `field-hub-osh-1` on `field-hub_default`, using
the local OpenSensorHub 2.0.1 distribution at commit `4c87a65` and durable state
under `/home/nh/docker/ets-ogcapi-connectedsystems10-local-osh/state`. The
versioned, non-secret runtime configuration is `ops/local-osh-gate-config.json`.
The final primary E2E gate passed `211/69/0/142` with zero writes and zero
startup errors; no credential is required by this isolated gate configuration.

### Local OSH seed fixture manifests

- `ops/local-osh-seed-fixtures.json` documents the existing static local OSH
  feature seeds used by prior local-health runs: System, Procedure,
  Deployment, and SamplingFeature, currently observed at id `040g`.
- `ops/local-osh-dynamic-data-seed-fixtures.json` is the Sprint 33
  planned/not-applied dynamic-data fixture contract for DataStream,
  Observation, ControlStream, Command, CommandStatus, and CommandResult
  evidence. It must not be applied unless mutation tests are explicitly enabled
  against a dedicated mutable IUT, and it is not proof of accepted OSH payload
  shape until Generator records actual request/response evidence. Sprint 38
  adds SimUAV preseed evidence to this manifest, but the fixture remains
  partial because Sprint 40 still lacks positive Command child item evidence and
  full populated binding closure is not claimed.

### Local OSH tasking fixture

Sprint 34 configured the field-hub OSH image with the sibling Sapient tasking
driver as an isolated Command-ack fixture:

- Driver jar: `sensorhub-driver-sapient-0.1.0.jar` in the field-hub OSH lib.
- TCP port: `12000` is mapped by `/home/nh/docker/gir/sar-ops/field-hub/docker-compose.yml`.
- Config: `/home/nh/docker/gir/sar-ops/field-hub/osh/config/config.json` includes module `sapient-driver`, currently `autoStart=false`.
- Runtime dependency caveat: Sapient's generated protobuf code requires protobuf runtime `4.31.1`; older protobuf jars were moved out of the active field-hub OSH lib.

Sprint 35 also configured the field-hub OSH image with the `osh-addons` SimUAV
tasking driver as an isolated CommandResult fixture:

- Driver jar: `sensorhub-driver-simuav-1.0.0-bundle.jar` in the field-hub OSH lib.
- Required datamodel jar: `sensorhub-datamodel-uxs-1.0.0.jar` in the field-hub OSH lib.
- Source: built from `/tmp/osh-addons-scan` / `https://github.com/opensensorhub/osh-addons` with Gradle task `:sensorhub-driver-simuav:osgi`.
- Config: `/home/nh/docker/gir/sar-ops/field-hub/osh/config/config.json` includes module `simuav-driver`, currently `autoStart=false`.

Keep Sapient and SimUAV disabled for the primary read-only smoke. When
temporarily enabled, Sapient can accept a real CS API Command through a local
SAPIENT TCP peer, and SimUAV can accept a waypoint feasibility Command without
an external peer and return inline result data. Sprint 40's local ConSys patch
and its direct probe results are historical evidence only. CP-003/ADR-012
prohibit rebuilding or deploying that patch. The current ConSys jar identifies
the clean upstream OSH checkout. Supported SimUAV configuration/test-data usage
remains permitted, but positive Command child item evidence is still missing.
Its generated dynamic
DataStreams/ControlStreams still fail current Part 2 populated TeamEngine
  schema checks because Observation/Command schema documents do not yet match the
  Annex A.9/SWE schema shape. OSH does not delete module-owned
resources through CS API. After a fixture run, reset only `field-hub_osh-data`,
restart OSH, and reseed `ops/local-osh-seed-fixtures.json` before running the
primary TeamEngine smoke.

The field-hub OSH container bakes `osh/lib/` into the image. After replacing a
ConSys jar in `/home/nh/docker/gir/sar-ops/field-hub/osh/lib`, rebuild the
`field-hub-osh` image before relying on a container restart or data-volume reset.

Local OSH BasicRealm credentials were rotated on 2026-06-03 after credential
material was accidentally printed in session tool output. Do not record the
credential values in ETS artifacts. Derive `SMOKE_AUTH_CREDENTIAL` at runtime
from the local field-hub config or from a user-supplied environment variable,
and record only whether a credential was supplied.

### Historical TeamEngine 5.6 baseline

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

**Historical resolution**: the Sprint 1 Dockerfile assembled TeamEngine 5.6.1 on top
of `tomcat:8.5-jre17` by downloading the published TE 5.6.1 web WAR /
console / common-libs zips from Maven Central. This produces a
TeamEngine 5.6.1 + JDK 17 container that is byte-for-byte equivalent in
test behaviour to the production image's TE deployment but loadable by
our JDK 17 ETS jar. Empirical evidence:
12/12 @Test methods PASS against GeoRobotix in 1.6 s via the SPI route
(report at `ops/test-results/s-ets-01-03-teamengine-smoke-2026-04-28.xml`).

Three secondary patches the historical Dockerfile applied, with their root causes:

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

ADR-011 supersedes the TeamEngine 5.6.1/Tomcat 8.5 runtime path for Sprint 41
and later. Keep this block only as the historical explanation for Sprint 1/2
baseline evidence; do not use it as current Dockerfile guidance.

### How to build

```
docker build -t ets-ogcapi-connectedsystems10:smoke .
```

The Dockerfile builder stage runs Maven and selects the reviewed runtime
payload. Do not run a separate broad `dependency:copy-dependencies` path for
TeamEngine packaging.

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
SMOKE_DOCKER_NETWORK=field-hub_default \
SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api \
SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-local-osh-results \
bash scripts/smoke-test.sh
```

The script tries host port 8081 first, falls back to 8082 if 8081 is
busy. Override with `SMOKE_PORT=8083 bash scripts/smoke-test.sh`. When
`SMOKE_OUTPUT_DIR` is set, reports land in that directory; otherwise they land
under `ops/test-results/`.

For an advisory public interoperability probe only:

```
SMOKE_TARGET=georobotix \
SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-georobotix-advisory-results \
bash scripts/smoke-test.sh
```

### Dev-environment caveats

- The pinned TeamEngine 6 digest is linux/amd64-only. On linux/arm64 Docker
  hosts, register amd64 binfmt support before running the Dockerfile:

  ```
  docker run --privileged --rm tonistiigi/binfmt --install amd64
  ```

- `scripts/smoke-test.sh` auto-detects host-port collisions and falls back from
  8081 to 8082/8083 for the TeamEngine container.

## Local OpenSensorHub mutable IUT

The local OpenSensorHub stack used for Sprint 12 mutable-IUT follow-up is
outside this ETS repository. Historical Sprint 32-40 evidence used:

```
/home/nh/docker/gir/sar-ops/field-hub
```

That historical path was absent on 2026-07-21. On 2026-07-22, the gate was
restored using local OpenSensorHub 2.0.1 build `4c87a65` from
`/home/nh/docker/osh-core/build/install/osh-core`. Durable no-secret state is at
`/home/nh/docker/ets-ogcapi-connectedsystems10-local-osh/state`, with the
versioned configuration at `ops/local-osh-gate-config.json`. The running
container is `field-hub-osh-1` on `field-hub_default`.

The current OSH service has no host-port binding; it is reachable from the
TeamEngine smoke container on Docker network `field-hub_default` at:

```
http://field-hub-osh-1:8081/sensorhub/api
```

The restored container was started with this equivalent command after copying
`ops/local-osh-gate-config.json` to the durable state path as `config.json`:

```
docker network create field-hub_default 2>/dev/null || true
docker run -d --name field-hub-osh-1 --network field-hub_default \
  -v /home/nh/docker/ets-ogcapi-connectedsystems10-local-osh/state:/state \
  -v /home/nh/docker/osh-core/build/install/osh-core:/opt/osh:ro \
  maven:3.9-eclipse-temurin-17 \
  java -Xmx512m -Dlogback.configurationFile=/opt/osh/logback.xml \
  -cp '/opt/osh/lib/*' org.sensorhub.impl.SensorHub /state/config.json
```

The H2 files under the durable state path contain the four seeded resources and
are intentionally not version-controlled. To recreate them on a new host,
start with empty state and POST each fixture from
`ops/local-osh-seed-fixtures.json` to its declared collection using its declared
media type. Preserve the resulting state, then run only the read-only smoke gate.

The current config sets `proxyBaseUrl` to `http://field-hub-osh-1:8081` so
dereference links remain reachable inside TeamEngine. It intentionally omits an
`exposedResources` system-filter view because that view caused Deployment item
lookup to enter an unnecessary H2 system-filter path.

The following synthetic fixtures are intentionally present for full-suite
read-only health runs. The exact payloads are versioned in
`ops/local-osh-seed-fixtures.json`.

| Collection | Resource | UID / notable property |
|---|---|---|
| `/systems` | `/systems/040g` | `urn:ets:local-osh:system:alpha`, `featureType=http://www.w3.org/ns/sosa/System` |
| `/procedures` | `/procedures/040g` | `urn:ets:local-osh:procedure:alpha` |
| `/deployments` | `/deployments/040g` | `urn:ets:local-osh:deployment:alpha` |
| `/samplingFeatures` | `/samplingFeatures/040g` | `urn:ets:local-osh:sampling-feature:alpha` |

The current isolated target does not require authentication. For any future
authenticated replacement, supply credentials through `SMOKE_AUTH_CREDENTIAL`
and never record the value in artifacts. The verified read-only command is:

```
SMOKE_DOCKER_NETWORK=field-hub_default \
SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api \
SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-local-osh-results \
bash scripts/smoke-test.sh
```

Current final evidence: `211 total / 69 passed / 0 failed / 142 skipped`,
`recognized_iut_request_logs=135`, zero writes, and zero startup errors.

Historical evidence: `/tmp/ets-csapi-osh-full-health-r3` completed on
2026-05-06 with exact archived totals `69 total / 50 passed / 0 failed /
19 skipped`. The 19 skips are expected for undeclared or unpopulated
surfaces that remain outside the current implemented ETS scope. The CRD
smoke exercised real POST/PUT/DELETE against a temporary `/systems/{id}`
resource and cleaned it up.

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
