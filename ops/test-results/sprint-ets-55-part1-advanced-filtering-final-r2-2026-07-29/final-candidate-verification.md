# Sprint 55 Final R2 Candidate Verification

Date: 2026-07-29

> Superseded audit evidence. Final Raze R3 returned `GAPS_FOUND 0.98`; none of
> these exact gates applies to the subsequent remediation candidate.

## Candidate

- Implementation candidate:
  `085a81fdaa8fb2b823dc029532a1e9b41b8cd16c`
- Candidate source: fresh detached clone at the commit above
- Exact image:
  `sha256:f1a6fed0a0a899a1f09f660ad7dbb947eed91823d6ae94176e06498ae24fc455`
- Deployed ETS jar manifest: `Build-Revision: 085a81fdaa`
- TeamEngine base image:
  `sha256:9d53016965c4cca0f4d8baec136ad11258dccc552e9a8c969a1a6f889336d90c`

R3 invalidated this candidate because Deployment property traversal could use
wrapper or unrelated nested href shortcuts and malformed hrefs could become
synthetic identities.

## Verification

- Reproducible second test-first baseline against pre-fix production:
  expected failure, `33/6/0/0`.
- Exact focused controlled HTTP and support tests: PASS, `33/0/0/0`.
- Exact full Docker Maven: BUILD SUCCESS, `587/0/0/3`.
- Scenario inventory: `20/20`, no missing literal Java anchors.
- Released ATS source audit: PASS against
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`.
- Committed coverage inventory: `240 total / 76 exact / 2 helper /
  115 candidate / 47 unmapped`; Advanced Filtering is `25/25 exact`.
- Reviewed mapping JSON parses, and the full Maven coverage verifier passes
  `23/0/0/0`.
- TeamEngine 6 runtime verifier: PASS for provenance, dependency collision,
  embedded SWE Common execution, base-file immutability, runtime invariants,
  and confidential build-context hygiene.
- Core sabotage: PASS. The intentional stub failure produces
  `238/2/10/226`; API Common setup/tests and all 25 Advanced Filtering methods
  SKIP before Advanced Filtering IUT access.
- Credential integration: PASS with zero literal credential hits.
- Credential wire E2E: PASS with zero unmasked test-artifact hits, 33 masked
  events, and 33 intact synthetic wire transmissions.

## Primary Local OSH TeamEngine E2E

The exact candidate's unmodified local OSH execution is `238/40/7/191`. This
is not a zero-failure conformance run and is not reported as one. The same
seven established local-IUT failures remain:

- `PropertyDefinitionsTests.propertyCollectionsAreValid`
- `SamplingFeaturesTests.samplingFeatureCollectionsAreValid`
- `ProceduresTests.everyProcedureHasCanonicalUrl`
- `ProceduresTests.procedureCollectionsAreValid`
- `DeploymentsTests.deploymentCollectionsAreValid`
- `DeploymentsTests.deploymentsReferencedFromSystemsAreValid`
- `DeploymentsTests.everyDeploymentHasCanonicalUrl`

All 25 deployed Advanced Filtering methods executed exactly once and SKIP with
the explicit reason that the IUT does not declare
`/conf/advanced-filtering`. These SKIPs prove deployment and declaration-gate
honesty, not positive IUT conformance. Positive procedure and fail-closed
behavior is covered by the exact `33/0/0/0` controlled HTTP run.

Artifact hygiene is PASS: 174 recognized requests, 169 IUT requests, all IUT
requests are GET, zero IUT writes, and zero credential leaks across the
TestNG report, TeamEngine container log, and smoke console log.

## External Dependency Immutability

- OSH checkout: clean at
  `4c87a65c9a967d52af9df476e65d7862c7673a15`, three commits behind and zero
  commits ahead of upstream.
- Deployed ConSys bundle: `Bundle-BuildNumber: 4c87a65`.
- Host and runtime ConSys jar SHA-256:
  `4af3bb25e850c959126baa797cecd980dd54bab26cac1140611c91757274d942`.
- Runtime `/opt/osh` mount: read-only.
- Runtime `/state` mount: writable durable state only.
- TeamEngine base-file comparison: PASS.
- No OSH or TeamEngine source or binary was modified. No hosted CI was added.
