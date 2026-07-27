# Sprint 52 Part 1 Sampling Features Verification

Date: 2026-07-27

## Scope

Sprint 52 replaces the historical four-method Sampling Features approximation
with the five released OGC 23-001 `/conf/sf` procedures. No OSH or TeamEngine
source or binary was modified.

## Coverage

- Released source: `v1.0.0` commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`.
- ATS inventory consistency: PASS.
- Coverage: `240 total / 35 exact / 2 helper / 133 candidate / 70 unmapped`.
- `/conf/sf`: `5/5 exact`.
- Deployed TestNG methods: `220`.

## Maven And HTTP

- Initial test-first compile: expected FAIL before support and procedure APIs
  existed.
- E2E prerequisite regression: `6/1/0/0`, reproducing all five direct methods
  being blocked by an optional inherited System evidence SKIP.
- Partial-collection regression: `1/1/0/0`, reproducing a canonical
  partial-evidence false PASS before the correction.
- Initial Raze gap-fix regression: `13/5/0/0`, reproducing omitted conditional
  GeoJSON validation and actual-media evidence hiding later defects.
- Page-observer regression: `16/3/0/0`, reproducing a later media SKIP erasing
  an earlier supported-page defect.
- Final focused Maven: `49/0/0/0`.
- Final full Docker Maven: `506/0/0/3`; BUILD SUCCESS.
- Controlled HTTP executes successful paths for all five procedures plus
  status/media, schema, metadata, canonical identity/equivalence,
  partial-collection, pagination, procedure-isolation, and dependency defects.
- Reviewed page observers validate every supported endpoint, collection, and
  nested-System page before pagination advances. Expected evidence SKIPs are
  aggregated only at narrow collection, item, or System boundaries after all
  independently inspectable candidates run.

## Runtime

- Final exact image:
  `sha256:ae3a7b6b17d98c328ca7dff95afa05fbfecf6a2f1ebe313a75c7429ae2580ff3`.
- TeamEngine 6 provenance, deployed SWE Common adapter, dependency collision,
  immutable-base, runtime-invariant, and confidential-context checks: PASS.
- Primary unmodified local OSH TeamEngine:
  `220 total / 40 passed / 6 failed / 174 skipped`.
- Sampling Features outcomes: System-reference PASS; collections FAIL because
  no collection advertises exact `featureType=sosa:Sample`; canonical URL SKIP
  for the same missing collection evidence; resources and canonical endpoints
  SKIP because actual `application/json` is outside released Sampling Feature
  GeoJSON validation.
- The other five failures are preserved Procedure and Deployment conformance
  outcomes. They are not converted to PASS or repaired outside ETS scope.
- Artifact hygiene: 117 recognized IUT requests, zero IUT writes, and zero
  credential leaks.

## Dependency And Credential Gates

- SystemFeatures sabotage produces `220/37/6/177`; the injected System failure
  is visible and all five Sampling Features methods SKIP before their own IUT
  access.
- Credential integration: targeted tests PASS with zero literal
  synthetic-credential hits in Maven or XML artifacts.
- Credential wire E2E: PASS with zero unmasked artifact hits, 36 masked log
  events, and 36 intact synthetic transmissions observed only by the stub IUT.

## External Immutability

- The deployed OSH checkout is clean at
  `4c87a65c9a967d52af9df476e65d7862c7673a15`, zero commits ahead and three
  behind upstream.
- The running OSH container mounts `/opt/osh` read-only.
- TeamEngine base-file identity is verified by the exact-image runtime gate.

## Adversarial Review

- Initial Raze: `GAPS_FOUND`, confidence `0.98`.
- `RAZE-S52-001` identified missing conditional GeoJSON schema validation in
  `/conf/sf/ref-from-system`; every nested supported GeoJSON page is now
  validated, including later pages and later Systems.
- `RAZE-S52-002` identified early expected media SKIPs hiding later defects;
  page observers and narrow aggregate evidence handling now preserve later
  collection, item, page, and System failures.
- Focused Raze recheck: `APPROVE`, confidence `0.99`, duration 589 seconds.
  Both findings are closed, with no new findings and no required fixes.

## Evidence

- `sprint-ets-52-test-first-2026-07-27.log`
- `sprint-ets-52-e2e-gap-behavior-test-first-2026-07-27.log`
- `sprint-ets-52-partial-collection-test-first-2026-07-27.log`
- `sprint-ets-52-raze-gapfix-test-first-2026-07-27.log`
- `sprint-ets-52-page-observer-test-first-2026-07-27.log`
- `sprint-ets-52-raze-gapfix-focused-final-2026-07-27.log`
- `sprint-ets-52-raze-gapfix-full-maven-final-2026-07-27.log`
- `sprint-ets-52-raze-gapfix-coverage-update-2026-07-27.log`
- `sprint-ets-52-final-teamengine-runtime-2026-07-27.log`
- `sprint-ets-52-final-local-osh-teamengine-2026-07-27.xml`
- `sprint-ets-52-final-local-osh-container-2026-07-27.log`
- `sprint-ets-52-final-primary-hygiene-2026-07-27.json`
- `sprint-ets-52-systemfeatures-sabotage-teamengine-2026-07-27.xml`
- `sprint-ets-52-systemfeatures-sabotage-container-2026-07-27.log`
- `sprint-ets-52-credential-integration-2026-07-27.txt`
- `sprint-ets-52-credential-e2e-2026-07-27.txt`
- `.harness/evaluations/sprint-ets-52-adversarial-final.yaml`
- `.harness/evaluations/sprint-ets-52-adversarial-recheck.yaml`

Sprint 52 is complete.
