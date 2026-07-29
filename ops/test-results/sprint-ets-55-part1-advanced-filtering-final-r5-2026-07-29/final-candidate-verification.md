# Sprint 55 Superseded R5 Candidate Verification

Date: 2026-07-29

## Candidate

- Commit: `060a8aa994d59f0adfa6bfa96fd5fb372b3d6743`
- Source: clean detached clone at the commit above
- Image:
  `sha256:a74b3cc8bfe71df11ef4cc13ef8ceb6c0b32e0cffc184e04f9f115c2f215f07e`
- Deployed jar manifest: `Build-Revision: 060a8aa994`
- TeamEngine base:
  `sha256:9d53016965c4cca0f4d8baec136ad11258dccc552e9a8c969a1a6f889336d90c`

## Verification

- R4 test-first baseline: expected failure, `40/4/0/0`.
- Exact focused controlled HTTP and support tests: PASS, `40/0/0/0`.
- Exact full Docker Maven: BUILD SUCCESS, `594/0/0/3`.
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
are covered by the exact `40/0/0/0` controlled HTTP run. The dedicated
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

## Raze Disposition

`exact-060a8aa-artifact-manifest.sha256` covers every SHA-prefixed exact
artifact in this directory. Fresh Raze R5 returned `GAPS_FOUND 0.99` with
four required findings, so this candidate and its exact evidence are
superseded rather than closure evidence. Requirement-linked remediation logs
in this directory record HTTP red `42/3/4/0`, focused green `48/0/0/0`,
coverage regeneration `23/0/0/0`, and full precommit green `602/0/0/3`.
A new committed candidate must repeat every exact gate and pass fresh Raze.
