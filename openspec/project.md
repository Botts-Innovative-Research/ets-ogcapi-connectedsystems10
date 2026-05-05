# Project Conventions — OGC API Connected Systems ETS

## Identity

- Project: `ets-ogcapi-connectedsystems10`
- Purpose: Java/TestNG Executable Test Suite for OGC API Connected Systems Part 1, runnable in OGC TeamEngine.
- Language: Java 17
- Build: Maven
- Test framework: TestNG plus JUnit-based structural tests
- HTTP client: REST Assured
- Runtime verification: Dockerized TeamEngine smoke against GeoRobotix by default

## Layout

- `src/main/java/org/opengis/cite/ogcapiconnectedsystems10/` — ETS code
- `src/main/java/.../conformance/<class>/` — conformance class tests
- `src/main/resources/org/opengis/cite/ogcapiconnectedsystems10/testng.xml` — canonical TestNG suite wiring
- `src/test/java/org/opengis/cite/ogcapiconnectedsystems10/` — unit and structural lint tests
- `scripts/smoke-test.sh` — Docker/TeamEngine E2E smoke
- `scripts/mvn-test-via-docker.sh` — Maven test wrapper for environments without host Maven
- `scripts/sabotage-test.sh` — dependency-cascade E2E verification
- `openspec/`, `_bmad/`, `.harness/`, `epics/`, `ops/` — spec/harness/project management context

## Naming

- Java package root: `org.opengis.cite.ogcapiconnectedsystems10`
- New conformance classes live under `conformance.<lowercaseclass>`.
- TestNG groups use concise lowercase names, e.g. `core`, `systemfeatures`, `geojson`.
- Every new TestNG group must have structural lint coverage in `VerifyTestNGSuiteDependency`.

## Requirement Traceability

- Every new assertion should cite the canonical OGC requirement URI in `@Test(description=...)`.
- Tests should reference REQ-* and SCENARIO-* IDs in descriptions or comments.
- OpenSpec, story DoD, `_bmad/traceability.md`, `ops/status.md`, and `ops/changelog.md` must be reconciled after implementation.

## Verification

```bash
docker run --rm --user "$(id -u):$(id -g)" -v "$PWD":/workspace -w /workspace \
  -e MAVEN_CONFIG=/tmp/.m2 -e MAVEN_OPTS="-Duser.home=/tmp" \
  maven:3.9-eclipse-temurin-17 mvn -B spring-javaformat:apply

bash scripts/mvn-test-via-docker.sh

SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results bash scripts/smoke-test.sh
```

For gate runs, use a fresh `/tmp` clone and place smoke artifacts outside the worktree.

## Result Reporting

Report exact totals, including skipped tests. Do not claim all tests pass when output includes skips.
