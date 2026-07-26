# Sprint 47 Part 1 System Verification

## Scope

S-ETS-47-01 replaces the historical System approximations with all six released
OGC 23-001 `/conf/system` procedures. It does not modify or proxy OSH or
TeamEngine and does not treat SKIP as positive conformance evidence.

## Candidate

- Exact image:
  `sha256:101e20653097fea9891ff5fbe1f4c160ae163ca97338cf63cfb5980dd958cf6e`
- Released coverage:
  `240 total / 10 exact / 2 helper / 145 candidate / 83 unmapped`
- `/conf/system`: `6/6 exact`

## Verification

- Focused Docker Maven: `46/0/0/0`
- Released-ATS audit Docker Maven: `23/0/0/0`
- ATS audit self-test and exact-source reproduction at released commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`: PASS
- Full Docker Maven: `395/0/0/3`
- Exact-image runtime, added-jar collision, immutable-base, and deployed SWE
  Common adapter probes: PASS
- Credential integration and wire-layer gates: PASS
- Dependency sabotage: expected `215/2/10/203`; all six System methods blocked
  before IUT access after prerequisite failure
- System-target sabotage: expected `215/37/1/177`; the current injection target
  fails, while all 13 direct and 2 transitive TestNG dependency descendant
  groups SKIP

## Primary E2E

Unmodified local OSH through Dockerized TeamEngine reports
`215 total / 38 passed / 0 failed / 177 skipped`, 105 recognized IUT GET
requests, zero writes, and zero startup errors.

All six System methods execute rather than dependency-SKIP:

- PASS: canonical URL, collections, location recommendation
- SKIP: canonical endpoint and resources endpoint because local OSH returns
  unsupported `application/json`
- SKIP: location-time because optional `mobile-system-id` is absent

API Common datetime retains its no-temporal-extent SKIP. The System boundary
allows only that exact evidence limitation; every prerequisite failure,
configuration failure, or other prerequisite SKIP still blocks System IUT
access. The inherited SKIP remains visible, so this run is not represented as
full inherited conformance.

A controlled direct HTTP fixture additionally executes successful positive
paths for all six deployed methods, including changed positional coordinates,
canonical dereference and equality, complete schema-valid resources, canonical
endpoint validation, and collection validation.

## Artifacts

- `ops/test-results/sprint-ets-47-focused-maven-2026-07-26.log`
- `ops/test-results/sprint-ets-47-ats-audit-maven-2026-07-26.log`
- `ops/test-results/sprint-ets-47-ats-audit-{self-test,reproduction}-2026-07-26.log`
- `ops/test-results/sprint-ets-47-full-maven-2026-07-26.log`
- `ops/test-results/sprint-ets-47-teamengine6-runtime-2026-07-26.log`
- `ops/test-results/sprint-ets-47-system-sabotage-2026-07-26.{log,xml}`
- `ops/test-results/sprint-ets-47-system-target-sabotage-2026-07-26.{log,xml}`
- `ops/test-results/sprint-ets-47-credential-{integration,e2e}-2026-07-26.txt`
- `ops/test-results/sprint-ets-47-part1-system-e2e-2026-07-26/`
