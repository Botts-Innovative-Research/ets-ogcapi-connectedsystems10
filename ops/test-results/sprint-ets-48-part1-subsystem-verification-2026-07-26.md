# Sprint 48 Part 1 Subsystem Verification

## Scope

Sprint 48 replaces the historical four-method Subsystem approximation with one
independently executable TestNG method for each of the five released OGC 23-001
`/conf/subsystem` procedures. No OSH or TeamEngine source or binary is changed.

## Implementation

- Reviewed ATS coverage is `5/5 exact` for `/conf/subsystem`.
- Independent bounded direct-edge traversal rejects duplicate IDs, pagination
  cycles, hierarchy cycles, malformed collections, and shortcut overlap.
- Collection discovery requires exactly one qualifying `rel=subsystems`
  occurrence whose resolved URI exactly matches the canonical endpoint.
- HTTP status and actual media handling occur before representation parsing on
  every root and nested page. The accepted first response is reused. Supported
  GeoJSON and first-response SensorML pages reuse the released System schemas;
  collection validation does not require local IDs, while recursive graph
  discovery does.
- `recursive=false` and `recursive=true` requests require successful responses
  without parsing unrelated response bodies.
- Top-level Sampling Feature, DataStream, and ControlStream endpoints establish
  implementation independently; nested HTTP 404 then fails.

## Verification

| Gate | Result |
|---|---|
| Raze gap-fix test-first | `12 total / 7 failures / 0 errors / 0 skipped` |
| Raze media recheck test-first | `7 total / 3 failures / 0 errors / 0 skipped` |
| Raze root-media test-first | `10 total / 3 failures / 0 errors / 0 skipped` |
| Focused Docker Maven | `45 total / 0 failures / 0 errors / 0 skipped` |
| Full Docker Maven | `417 total / 0 failures / 0 errors / 3 skipped` |
| Released coverage | `240 total / 15 exact / 2 helper / 144 candidate / 79 unmapped` |
| Controlled HTTP | PASS; all five positive procedures and all four Raze gap classes covered |
| Exact image | `sha256:32a43f81b441f3b687b9e83d9d6688016278f4f7a5fec5d8a3c2b174490f285c` |
| TeamEngine runtime | PASS; adapter, exact tuple, one added jar, two reviewed collisions, immutable base |
| Local OSH TeamEngine | `216 total / 39 passed / 0 failed / 177 skipped` |
| Local OSH traffic | 109 recognized requests, zero writes |
| SystemFeatures sabotage | `216 total / 37 passed / 1 intentional failure / 178 skipped` |
| Credential integration | PASS; zero literal leaks |
| Credential wire E2E | PASS; 0 unmasked execution-artifact hits, 40 masked events, 40 intact synthetic wire transmissions |
| Artifact hygiene | PASS for positive and sabotage archives; zero leaks and zero IUT writes |
| Final Raze | `APPROVE_WITH_CONCERNS`, confidence `0.99`; no unresolved required findings |

The local OSH run reaches all five changed methods. The exact recursive boolean
procedure passes. Collection, both recursive-search procedures, and recursive
associations skip at the root media gate because `/systems` returns unsupported
`application/json`; none parse that representation or pass vacuously.

The container log's line 41 XML parser diagnostic is not a startup failure.
`SuiteFixtureListener` first attempts the legacy DOM path for the downloaded
JSON landing page, catches the expected parse failure, and continues through
the documented REST path. Tomcat startup, suite registration, all test
execution, and the startup error scan pass.

## Dependency And Provenance

The SystemFeatures sabotage makes all five Subsystem methods skip and verifies
every one of the 15 direct or transitive dependency descendants. The original
worktree remains unmodified by the isolated sabotage.

The OSH checkout at `/home/nh/docker/osh-core` is clean and zero commits ahead
of `origin/master`. HEAD is `4c87a65c9a967d52af9df476e65d7862c7673a15`;
upstream is `ce8dd961c3f4dfdd739e5e2c78d42d8f62eb99bd`. The running container mounts
that checkout at `/opt/osh` read-only, and
`sensorhub-service-consys-2.0.1.jar` records `Bundle-BuildNumber: 4c87a65`.

## Evidence

- `sprint-ets-48-raze-gapfix-test-first-2026-07-26.log`
- `sprint-ets-48-raze-recheck-test-first-2026-07-26.log`
- `sprint-ets-48-final-root-media-test-first-2026-07-26.log`
- `sprint-ets-48-final-root-media-regression-2026-07-26.log`
- `sprint-ets-48-focused-maven-2026-07-26.log`
- `sprint-ets-48-full-maven-2026-07-26.log`
- `sprint-ets-48-teamengine6-runtime-2026-07-26.log`
- `sprint-ets-48-local-osh-teamengine-2026-07-26.xml`
- `sprint-ets-48-local-osh-teamengine-container-2026-07-26.log`
- `sprint-ets-48-systemfeatures-sabotage-2026-07-26.xml`
- `sprint-ets-48-systemfeatures-sabotage-2026-07-26.log`
- `sprint-ets-48-credential-integration-2026-07-26.log`
- `sprint-ets-48-credential-e2e-2026-07-26.txt`
- `sprint-ets-48-positive-hygiene-2026-07-26.txt`
- `sprint-ets-48-sabotage-hygiene-2026-07-26.txt`
