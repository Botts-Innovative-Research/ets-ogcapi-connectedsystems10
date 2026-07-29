# Sprint 55 Final R6 Candidate Verification

Date: 2026-07-29

## Candidate

- Commit: `f2a88d54e643f9c91cfcc432f7d7bc403bfab6f0`
- Source: clean detached clone at the commit above
- Image:
  `sha256:d7bdc60725409990ac8f79af10dbb9667006127be6313acc0d36cd6bc39e7f1b`
- Deployed jar manifest: `Build-Revision: f2a88d54e6`
- TeamEngine base:
  `sha256:9d53016965c4cca0f4d8baec136ad11258dccc552e9a8c969a1a6f889336d90c`

## Verification

- R5 HTTP test-first baseline: expected failure, `42/3/4/0`.
- Exact focused controlled HTTP and support tests: PASS, `48/0/0/0`.
- Exact full Docker Maven: BUILD SUCCESS, `602/0/0/3`.
- Scenario inventory: `20/20`, no missing literal Java anchors.
- Released ATS source audit: PASS against
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`.
- Coverage: `240 total / 76 exact / 2 helper / 115 candidate / 47
  unmapped`; Advanced Filtering is `25/25 exact`.
- R5 canonical direct and compact relation vocabulary, GeoJSON and SensorML
  System target allowlists, failed representation-validation boundaries, and
  corrected keyword mapping evidence pass the exact focused and full gates.
- TeamEngine runtime verifier: PASS for provenance, embedded SWE Common
  execution, dependency collisions, base-file immutability, runtime
  invariants, and confidential build-context hygiene.
- Core sabotage: PASS. The intentional stub failure produces
  `238/2/10/226`; API Common setup/tests and all 25 Advanced Filtering methods
  SKIP before Advanced Filtering IUT access.
- Credential integration: PASS with zero literal credential hits.
- Credential wire E2E: PASS with zero unmasked test-artifact hits, 33 masked
  events, and 33 intact synthetic wire transmissions.

## Local OSH E2E

Dockerized TeamEngine executed the exact candidate against the running,
unmodified local OSH IUT at
`http://field-hub-osh-1:8081/sensorhub/api`.

The honest result is `238 total / 40 passed / 7 failed / 191 skipped`. The
seven failures are the established local-IUT baseline:

- `PropertyDefinitionsTests.propertyCollectionsAreValid`
- `SamplingFeaturesTests.samplingFeatureCollectionsAreValid`
- `ProceduresTests.everyProcedureHasCanonicalUrl`
- `ProceduresTests.procedureCollectionsAreValid`
- `DeploymentsTests.deploymentCollectionsAreValid`
- `DeploymentsTests.deploymentsReferencedFromSystemsAreValid`
- `DeploymentsTests.everyDeploymentHasCanonicalUrl`

All 25 Advanced Filtering methods execute exactly once and SKIP because local
OSH does not declare `/conf/advanced-filtering`. This proves deployment and
declaration-gate behavior, not positive IUT conformance. Positive semantics
are covered by the exact `48/0/0/0` controlled HTTP run. The dedicated
Advanced Filtering inventory records 25 SKIPs, zero non-SKIPs, and zero
Advanced Filtering query log hits.

Artifact hygiene is PASS: 174 recognized requests, 169 IUT requests, all IUT
requests are GET, zero IUT writes, and zero credential leaks across the
TestNG report, TeamEngine container log, and smoke console log.

## Immutability

- OSH checkout is clean at
  `4c87a65c9a967d52af9df476e65d7862c7673a15`, three commits behind and zero
  ahead of upstream.
- Deployed ConSys bundle build is `4c87a65`; its SHA-256 is
  `4af3bb25e850c959126baa797cecd980dd54bab26cac1140611c91757274d942`.
- `/opt/osh` is read-only and `/state` is the only writable OSH mount.
- No OSH or TeamEngine source or base binary was modified.
- No hosted CI was added.

## Evidence

`exact-f2a88d5-artifact-manifest.sha256` covers every SHA-prefixed exact
artifact in this directory.

## Adversarial Review

Raze R6 returned `GAPS_FOUND 0.99`. The shared relation matcher still
lowercases and removes punctuation, strips a trailing `Link`, and accepts
broad aliases. Non-released values including `parentSystemLink` and
`ogc-rel:parentSystemLink` can therefore manufacture association evidence.
The deployed-System type, representation-validation, keyword-mapping,
provenance, E2E honesty, dependency, credential, GET-only, and checksum gates
were independently verified. Candidate `f2a88d5` is superseded; a new exact
candidate must remediate the remaining relation-vocabulary finding.
