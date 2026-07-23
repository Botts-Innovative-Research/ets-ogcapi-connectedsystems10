# Sprint ETS-42 Final Metadata Gapfix Verification

Date: 2026-07-22

## Scope

This pass addresses all required fixes from
`sprint-ets-42-generic-jar-guard-adversarial-recheck-2026-07-22.yaml`.

- Runtime output now emits every accepted coordinate/path collision tuple and
  fails on unused allowlist entries.
- Adversarial guard self-tests cover unused-entry rejection in addition to
  renamed-coordinate and embedded-path rejection.
- Jenkins no longer requests the removed `docker` Maven profile; structural
  Maven coverage prevents regression.
- The Part 2 epic header and Sprint 41 Raze chronology are reconciled.

## Verification

- Raw expected Jenkins test-first failure: `11/1/0/0`, artifact
  `sprint-ets-42-final-metadata-gapfix-test-first-raw-2026-07-22.txt`.
- Corrected focused Maven: PASS `11/0/0/0`.
- Fresh full Docker Maven: PASS `312/0/0/3`.
- Exact post-change image:
  `sha256:05a592e0f09de6dfb18f3c01457c7f2dcdcdb635d16ff672485130c32b9b988d`.
- Exact-image runtime verifier: PASS. It emits the accepted
  `org.opengis.cite:ets-ogcapi-connectedsystems10` tuples for
  `META-INF/services/com.occamlab.te.spi.jaxrs.TestSuiteController` and
  `test-run-props.xml`, then reports one added jar and two reviewed collisions.
- Fresh-clone local OSH TeamEngine E2E: PASS `211/69/0/142`; 135 recognized
  requests (`GET=133`, `OPTIONS=2`), zero writes, and zero startup errors.
- `.moduledata` remains absent.

## Remaining Gate

One focused final Raze recheck must confirm the bounded required-fix areas before
the stories close. SensorML remains deferred pending a reusable FCU/OGC module.
