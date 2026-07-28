# Sprint ETS-54 Part 1 GeoJSON Verification

Date: 2026-07-28
Story: S-ETS-54-01
Requirements: REQ-ETS-PART1-012, REQ-ETS-COVERAGE-001
Released source: `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`

## Implemented boundary

- Exactly twelve independent TestNG methods implement the twelve released
  `/conf/geojson` procedures.
- `GeoJsonSupport` owns non-mutating JSON/YAML OpenAPI inspection, canonical
  resource traversal, actual-media gates, released schema dispatch, common and
  resource mapping checks, and association relation tables.
- GeoJSON inherits Part 1 API Common directly. System and unrelated sibling
  groups do not block it.
- All IUT operations issued by the GeoJSON procedures are GET requests. Write
  media is established from OpenAPI POST/PUT request-body metadata.
- Discovery endpoints tolerate valid JSON with local OSH's inherited
  `Content-Type: auto`; actual representation gates remain strict.

## Verification

- Test-first evidence records the expected missing-symbol compile red and the
  later `9/1/0/0` discovery-media regression red.
- Focused Docker Maven: `45 tests / 0 failures / 0 errors / 0 skips`.
- Full Docker Maven: `548 tests / 0 failures / 0 errors / 3 skips`.
- Released ATS coverage:
  `240 total / 51 exact / 2 helper / 119 candidate / 68 unmapped`.
- `/conf/geojson`: `12/12 exact`.
- Released-source inventory reproduction: PASS at the pinned clean checkout.
- GeoJSON schema parity: PASS for eight entry schemas and 20 transitive
  schemas.
- Property carryover parity: PASS for three entry schemas and 53 transitive
  schemas; wrong-commit and dirty-checkout self-tests reject their inputs.
- Dedicated Property pagination and later-evidence continuation regressions:
  PASS.
- Controlled HTTP executes all twelve positive procedures and fail-closed
  media, schema, mapping, pagination, cross-origin, association, dependency,
  and credential branches.
- API Common sabotage: PASS. API Common setup and all twelve GeoJSON methods
  skip before GeoJSON IUT access.
- Credential integration and wire E2E: PASS with zero unmasked artifact hits,
  34 masked events, and 34 intact synthetic transmissions.
- Artifact hygiene: PASS across 150 recognized request entries, including 145
  IUT GETs, zero writes, and zero credential leaks.

## TeamEngine E2E

Exact image:
`sha256:9277fe99e6cf4bacbee9b839ab6890e789ebeaac1e6a8de6eecd052494245c19`

The image was built from clean commit
`53094a4412c6487f69715f88aab330d73f3bad40` and executed through Dockerized
TeamEngine against the running local OSH at
`http://field-hub-osh-1:8081/sensorhub/api`.

The honest full-suite result is
`219 total / 40 passed / 7 failed / 172 skipped`. The seven failures are the
established local-IUT baseline and match the Sprint 53 failure set
method-for-method; Sprint 54 introduced no additional failure. The suite total
drops by one because thirteen historical GeoJSON methods became twelve exact
released procedures.
All twelve GeoJSON methods were discovered exactly once and skipped at their
actual-media or API-definition evidence boundaries. These skips are not
positive conformance evidence.

The first exact-image run exposed twelve setup failures caused by valid
discovery JSON labeled `Content-Type: auto`. The requirement and regression
test were updated before the narrowly scoped fix; the second run removed all
twelve regressions.

Local OSH remains clean at
`4c87a65c9a967d52af9df476e65d7862c7673a15`, and `/opt/osh` remains
read-only. Exact-image TeamEngine provenance, deployed SWE Common adapter,
dependency-collision, immutable-base, runtime, and confidential-context gates
pass. No OSH or TeamEngine source or binary was modified.

## Evidence index

- `sprint-ets-54-test-first-2026-07-28.md`
- `sprint-ets-54-initial-compile-red-reproduction-2026-07-28.log`
- `sprint-ets-54-discovery-regression-red-reproduction-2026-07-28.log`
- `sprint-ets-54-focused-maven-2026-07-28.log`
- `sprint-ets-54-full-maven-2026-07-28.log`
- `sprint-ets-54-ats-coverage-audit-2026-07-28.log`
- `sprint-ets-54-geojson-schema-parity.json`
- `sprint-ets-54-geojson-schema-parity-self-test-2026-07-28.log`
- `sprint-ets-54-property-schema-parity.json`
- `sprint-ets-54-property-schema-parity-self-test-2026-07-28.log`
- `sprint-ets-54-local-osh-teamengine-2026-07-28.xml`
- `sprint-ets-54-local-osh-teamengine-container-2026-07-28.log`
- `sprint-ets-54-local-osh-failure-attribution-2026-07-28.md`
- `sprint-ets-54-apicommon-sabotage-teamengine-2026-07-28.xml`
- `sprint-ets-54-apicommon-sabotage-verdict-2026-07-28.log`
- `sprint-ets-54-teamengine-runtime-2026-07-28.log`
- `sprint-ets-54-credential-integration-2026-07-28.txt`
- `sprint-ets-54-credential-e2e-2026-07-28.txt`
- `sprint-ets-54-local-osh-hygiene-2026-07-28.json`
- `sprint-ets-54-local-osh-hygiene-2026-07-28.txt`
- `sprint-ets-54-no-mutation-2026-07-28.log`
- `sprint-ets-54-immutability-2026-07-28.txt`
- `.harness/evaluations/sprint-ets-54-adversarial-initial.yaml`
- `.harness/evaluations/sprint-ets-54-adversarial-final.yaml`

Initial Raze findings are closed. Final Raze is `APPROVE` at confidence
`0.99`, with no required fixes. Evidence:
`.harness/evaluations/sprint-ets-54-adversarial-final.yaml`.
