# Sprint 55 Final R3 Candidate Verification

Date: 2026-07-29

> Superseded audit evidence. Final Raze R4 returned `GAPS_FOUND 0.99`; none of
> these exact gates applies to the subsequent direct-relation and
> deployed-System target-type remediation candidate.

The R4 remediation evidence retained in this directory is:

- `r4-regressions-red.log`: expected `40/4/0/0`.
- `focused-r4-precommit.log`: PASS `40/0/0/0`.
- `full-maven-r4-precommit.log`: PASS `594/0/0/3`.

## Candidate

- Commit: `756d729828d08b88d43ce8ae0ff5f5dd2e5f13b7`
- Source: fresh detached clone at the commit above
- Image:
  `sha256:e6e7f7c7c853081b37970d69d3742d36baaa5a92d1f39ca46adb796535ec4bf1`
- Deployed jar manifest: `Build-Revision: 756d729828`
- TeamEngine base:
  `sha256:9d53016965c4cca0f4d8baec136ad11258dccc552e9a8c969a1a6f889336d90c`

## Verification

- R3 test-first baseline: expected failure, `36/3/0/0`.
- Exact focused controlled HTTP and support tests: PASS, `36/0/0/0`.
- Exact full Docker Maven: BUILD SUCCESS, `590/0/0/3`.
- Scenario inventory: `20/20`, no missing literal Java anchors.
- Released ATS source audit: PASS against
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`.
- Coverage: `240 total / 76 exact / 2 helper / 115 candidate / 47
  unmapped`; Advanced Filtering is `25/25 exact`.
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
are covered by the exact `36/0/0/0` controlled HTTP run. The dedicated
Advanced Filtering E2E inventory records 25 SKIPs, zero non-SKIPs, and zero
filter-query log hits.

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

`exact-756d729-artifact-manifest.sha256` covers every SHA-prefixed exact
artifact in this directory. The prior `085a81f` evidence remains preserved
separately as superseded audit evidence.
