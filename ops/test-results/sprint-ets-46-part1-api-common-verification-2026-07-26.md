# Sprint ETS-46 Part 1 API Common Verification

## Scope

S-ETS-46-01 implements the six directly owned released OGC 23-001
`/conf/api-common` procedures: four exact TestNG methods and two reviewed
helpers. The five inherited external OGC API Features/Common suites remain
partial and full-class conformance is not claimed.

## Coverage

- Released inventory: `240 total`.
- Mapping result: `4 exact / 2 helper / 150 candidate / 84 unmapped`.
- Part 1: `110 total / 4 exact / 2 helper / 49 candidate / 55 unmapped`.
- `/conf/api-common`: `4/4 exact`.
- Targetless Part 1 supporting tests: `2/2 helper`.
- Exact-source reproduction from `v1.0.0` commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`: PASS.

## Build And Unit Gates

- Java formatter, shell syntax, and `git diff --check`: PASS.
- Focused API Common/support regressions: PASS.
- Final Docker Maven: `373 total / 0 failures / 0 errors / 3 skipped`.
- Fresh-report regressions prove stale and ambiguous sabotage evidence is
  rejected.

## Runtime Gates

- Implementation commit: `449fbcf`.
- Exact image:
  `sha256:bc5b9cf5a425e6ce2ed1054ee225a68b353f5d8113363414f404b0d4ff27769e`.
- TeamEngine 6 runtime verifier: PASS, including SWE Common adapter execution,
  immutable base provenance, coordinate/path collision checks, and confidential
  build-context hygiene.
- Primary local OSH TeamEngine: `215 total / 35 passed / 0 failed / 180
  skipped`, 102 recognized IUT request logs, zero writes, and zero startup
  errors.
- API Common setup: PASS.
- Resource IDs: PASS.
- Resource UIDs: PASS.
- UID-form recommendation: PASS with four warnings for valid local fixture
  `urn:ets:*` values outside the IANA snapshot.
- Date-time: SKIP because local OSH advertises no usable temporal extent. This
  is an evidence limitation, not positive date-time conformance.

Durable local OSH XML and container logs are under
`ops/test-results/sprint-ets-46-part1-api-common-e2e-2026-07-26/`.

## Dependency And Credential Gates

- Live sabotage: Core `FAIL=6`, API Common setup `SKIP=1`, API Common tests
  `SKIP=4`, and SystemFeatures `SKIP=6`.
- Credential unit integration: targeted `6/0/0/0`; zero unmasked literal hits
  in Maven or Surefire output.
- Credential wire E2E: zero unmasked hits in TestNG/container/smoke artifacts,
  42 masked-form log hits, and 42 unmasked transmissions observed only by the
  hermetic stub IUT.

## Adversarial Review

Raze completed four review cycles. All media parsing, `now`, datetime-form,
status-honesty, registry, stale-evidence, UID precedence, and dependency
documentation findings are closed. Final verdict: `APPROVE`, confidence
`0.99`, no required findings. Token metadata was unavailable.

See `.harness/evaluations/sprint-ets-46-adversarial.yaml`.
