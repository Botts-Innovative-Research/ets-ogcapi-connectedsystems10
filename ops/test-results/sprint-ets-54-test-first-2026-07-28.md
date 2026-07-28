# Sprint ETS-54 Test-First Evidence

Date: 2026-07-28

## Initial implementation red

Requirement-linked structural, support, HTTP, parity, and carryover tests were
written before `GeoJsonSupport` existed. The first compilation failed on the
expected missing `GeoJsonSupport` symbols and missing released-procedure
methods.

The gate was reproduced from detached planning commit `0cf5ec3` by applying
only the three final requirement-linked GeoJSON test classes. Maven stopped at
test compilation with 60 expected errors before executing tests. The raw
transcript is
`sprint-ets-54-initial-compile-red-reproduction-2026-07-28.log`.

After the twelve released procedures and support boundary were implemented,
the archived focused suite passed
`45 tests / 0 failures / 0 errors / 0 skips`.

## E2E-discovered regression red

The first exact-image local OSH execution exposed twelve GeoJSON setup
failures because inherited discovery endpoints returned valid JSON with the
known nonstandard `Content-Type: auto`. Before changing production code, the
capability scenario and a controlled HTTP regression were added. That focused
run was reproduced from detached pre-fix commit `870a15d` with the final
regression test. It produced
`9 tests / 1 failure / 0 errors / 0 skips` in
`VerifyGeoJsonHttpProcedures#nonstandardDiscoveryMediaDoesNotWeakenGeoJsonRepresentationGate`.
The assertion identified `/api/conformance` as not a JSON media type because
its response was labeled `auto`. The raw transcript is
`sprint-ets-54-discovery-regression-red-reproduction-2026-07-28.log`.

Production discovery parsing was then narrowed to accept valid JSON bodies for
landing, conformance, and collections discovery only. Canonical GeoJSON
representation gates continue to require actual `application/geo+json`.
The same test class passes `9/0/0/0` in
`sprint-ets-54-focused-maven-2026-07-28.log`, and the second exact-image E2E
run removed all twelve setup regressions.
