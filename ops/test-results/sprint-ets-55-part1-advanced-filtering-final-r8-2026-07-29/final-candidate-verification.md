# Sprint 55 Final R8 Candidate Verification

Date: 2026-07-29

## Candidate

- Commit: `fce461288e99167bab6f391085493784da42cc58`
- Source: clean detached clone at the commit above
- Image:
  `sha256:ed03d1f943da442d8c13bdfc5c140b08c1e9155a57f3f10696925a2a0a402a79`
- Deployed jar manifest: `Build-Revision: fce461288e`
- TeamEngine base:
  `sha256:9d53016965c4cca0f4d8baec136ad11258dccc552e9a8c969a1a6f889336d90c`

## Verification

- R7 remediation test-first baseline: FAIL, `48/2/0/0`, preserved as
  `r8-test-first-red-48-2-0-0.log`.
- Exact focused controlled HTTP and support tests: PASS, `53/0/0/0`.
- Exact full Docker Maven: BUILD SUCCESS, `607/0/0/3`.
- Released ATS source audit: PASS against
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`.
- R7's representation-dispatch finding is covered by two controlled HTTP
  regressions. GeoJSON link relations are now evaluated only for GeoJSON
  resources. SensorML System parent and procedure evidence remains limited to
  the released `attachedTo` and `typeOf` mappings.
- TeamEngine runtime verifier: PASS for provenance, embedded SWE Common
  execution, dependency collisions, base-file immutability, runtime
  invariants, and confidential build-context hygiene.
- Core sabotage: PASS. The intentional stub failure produces
  `238/2/10/226`; API Common setup/tests and all SystemFeatures assertions
  dependency-SKIP.
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

The Part 1 Advanced Filtering configuration method passes and all 25
assertions SKIP because local OSH does not declare `/conf/advanced-filtering`.
This proves deployment and declaration-gate behavior, not positive IUT
conformance. Positive semantics are covered by the exact `53/0/0/0`
controlled HTTP/support run. The dedicated inventory records 25 SKIPs, zero
non-SKIP assertions, and zero Advanced Filtering query-log hits.

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

`exact-fce4612-artifact-manifest.sha256` covers every SHA-prefixed exact
artifact in this directory; all 17 entries pass `sha256sum -c`.

## Adversarial Review

Fresh Raze R8 returned `APPROVE 0.99`. It closed ADV-R7-001 and the raw-red
baseline provenance finding, found no new gaps, independently reproduced the
released relation mappings and scenario trace, verified the deployed class
against candidate source, and confirmed every archived exact-gate claim.
