# Sprint ETS-42 Jar-Collision Gapfix Verification

Date: 2026-07-22

## Scope

This pass closes the implementation gaps from
`sprint-ets-42-duplicate-gapfix-adversarial-recheck-2026-07-22.yaml` without
claiming final Raze closure.

- Added filename-independent inventory of every base and final runtime jar.
- Scanned all embedded Maven coordinates and rejected renamed TeamEngine or
  duplicate base coordinate families.
- Intersected added-jar functional paths with all base jars. The exact,
  rationale-bearing allowlist contains only the conventional TeamEngine
  `test-run-props.xml` and TestSuiteController SPI descriptor paths.
- Relocated NetworkNT `jsv-messages*.properties` to an ETS-unique path and
  verified relocated bytecode references that path.
- Added adversarial guard fixtures for renamed-coordinate rejection, embedded
  path-collision rejection, and exact allowlist acceptance.
- Removed the empty `.moduledata` directory.

## Verification

- Test-first structural evidence: expected FAIL `11/1/0/0`, raw output
  `sprint-ets-42-jar-collision-assertion-test-first-raw-2026-07-22.txt`.
  The earlier `jar-collision-test-first-raw` file records a formatter precheck,
  not a test result.
- Final focused Maven: PASS `11/0/0/0`, raw output
  `sprint-ets-42-jar-collision-gapfix-focused-maven-r4-2026-07-22.txt`.
- Fresh full Docker Maven: BUILD SUCCESS `312/0/0/3`, raw output
  `sprint-ets-42-jar-collision-gapfix-full-maven-2026-07-22.txt`.
- Exact E2E image:
  `sha256:9a34fd4abda872637635271b3f17a977ec3b0c0928fc70b83b0980d20e98f50e`.
- Exact-image runtime verifier: PASS. It executed valid and invalid adapter
  inputs with the normal wildcard runtime classpath, passed adversarial guard
  fixtures, and reported one added jar with two reviewed functional-path
  collisions.
- Fresh-clone primary local OSH TeamEngine E2E: PASS
  `211 total / 69 passed / 0 failed / 142 skipped`; 135 recognized IUT
  requests (`GET=133`, `OPTIONS=2`), zero writes, and zero startup errors.
- `.moduledata` is absent from worktree status.

## Remaining Gate

A fresh final Raze recheck must return no unresolved required fixes before
S-ETS-41-01 or S-ETS-42-02 is marked complete. SensorML remains deferred pending
a reusable FCU/OGC validator module.
