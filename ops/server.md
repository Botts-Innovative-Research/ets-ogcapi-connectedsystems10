# server.md — operational reference for ets-ogcapi-connectedsystems10

> Last updated: 2026-04-28 — Sprint 1 (S-ETS-01-01) scaffold landing.

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

The smoke test (S-ETS-01-03) Docker image is built via `mvn -P docker
package` and runs the ETS jar inside `ogccite/teamengine-production:5.6.1`.

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
