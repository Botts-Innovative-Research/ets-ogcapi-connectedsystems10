# S-ETS-01-03: TeamEngine 5.6.x (currently 5.6.1) Docker Smoke Test

> Status: Historical TeamEngine 5 baseline; superseded by S-ETS-41-01 and ADR-011/012 | Last updated: 2026-07-23
>
> Do not execute the TeamEngine 5 assembly or CI tasks below. Current integration
> uses the immutable OGC TeamEngine 6 image with additive ETS artifacts only.

## Description
Wire the ETS jar from S-ETS-01-01 into TeamEngine 5.6.x (currently 5.6.1) via the TestNG SPI, ship a CTL wrapper, build a Dockerfile that extends `ogccite/teamengine-production:5.6.1`, package a `docker-compose.yml`, and prove the round-trip via a `scripts/smoke-test.sh` that runs the CS API Core suite from S-ETS-01-02 against GeoRobotix and asserts the TestNG report is non-empty with zero suite-registration errors.

This story is the Sprint 1 capstone: it proves the entire vertical slice (scaffold → conformance class → TeamEngine integration → live IUT) works end-to-end. Without it, the Sprint 1 contract cannot be evaluated by Quinn.

## OpenSpec References
- Spec: `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- Requirements: REQ-ETS-TEAMENGINE-001, REQ-ETS-TEAMENGINE-002, REQ-ETS-TEAMENGINE-003, REQ-ETS-TEAMENGINE-004, REQ-ETS-TEAMENGINE-005
- Scenarios: SCENARIO-ETS-TEAMENGINE-LOAD-001 (CRITICAL), SCENARIO-ETS-CORE-SMOKE-001 (CRITICAL)

## Acceptance Criteria
- [ ] SPI registration class (`org.opengis.cite.ogcapiconnectedsystems10.TestNGController` or equivalent) extends `ets-common`'s SPI base and is declared in `META-INF/services/com.occamlab.te.spi.jaxrs.TestSuiteController`
- [ ] CTL wrapper at `src/main/scripts/ctl/ogcapi-connectedsystems10-suite.ctl` accepts `iut-url`, `auth-type`, `auth-credential` parameters and passes them as TestNG suite parameters
- [ ] `Dockerfile` extends `ogccite/teamengine-production:5.6.1` and copies the built jar to `/usr/local/tomcat/webapps/teamengine/WEB-INF/lib/`
- [ ] `docker-compose.yml` brings the stack up at `http://localhost:8081/teamengine/` with healthcheck
- [ ] `docker run` healthcheck passes within 30 seconds (NFR-ETS-04)
- [ ] `GET http://localhost:8081/teamengine/rest/suites` lists the `ogcapi-connectedsystems10` suite
- [ ] `scripts/smoke-test.sh` runs the Core suite against GeoRobotix and exits 0 with a non-empty TestNG report
- [ ] TeamEngine container logs show zero ERROR-level entries during suite registration
- [ ] SCENARIO-ETS-TEAMENGINE-LOAD-001 passes
- [ ] SCENARIO-ETS-CORE-SMOKE-001 passes

## Tasks
1. Implement SPI registration class extending `ets-common`'s base (Architect: confirm exact base class)
2. Write `META-INF/services/com.occamlab.te.spi.jaxrs.TestSuiteController` declaration
3. Write CTL wrapper at `src/main/scripts/ctl/ogcapi-connectedsystems10-suite.ctl` (XSLT/CTL — pattern from `ets-ogcapi-features10/src/main/scripts/ctl/main.ctl`)
4. Write Dockerfile extending `ogccite/teamengine-production:5.6.1`
5. Write `docker-compose.yml` with healthcheck against `/teamengine/`
6. Write `scripts/smoke-test.sh` that builds image, launches container, waits for healthcheck, runs Core suite against GeoRobotix, asserts report non-empty
7. Run the smoke test as a required local release gate
8. Update spec implementation status

## Dependencies
- Depends on: S-ETS-01-01 (jar exists) AND S-ETS-01-02 (Core suite has tests to run)
- Sprint 1 capstone — this is the last story to complete

## Implementation Notes
<!-- Fill after implementation -->
- **TeamEngine SPI mechanics open question**: The `testng-essentials` docs defer SPI registration details to a "Part 2" that is not yet linked. Mitigation: copy the SPI registration pattern from `opengeospatial/ets-ogcapi-features10` directly. Architect to ratify.
- **CTL wrapper reference**: `ets-ogcapi-features10/src/main/scripts/ctl/main.ctl`
- **Smoke-test idempotency**: GeoRobotix is a public demo server; tests must be idempotent and not mutate state (no CRUD/Update tests in Sprint 1's smoke run)

## Definition of Done
- [ ] All acceptance criteria checked
- [ ] Both critical SCENARIOs pass
- [ ] Smoke test release-candidate evidence recorded manually on available target platforms
- [ ] Spec implementation status updated
- [ ] Story status set to Done in this file and in `epic-ets-04-teamengine-integration.md`
- [ ] Sprint 1 contract evaluation criteria met
