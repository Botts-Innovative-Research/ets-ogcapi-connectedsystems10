# Sprint 55 Final R2 Remediation Evidence

Date: 2026-07-29

## Candidate

The prior candidate `29753ca85c5263022c82aa94c63267831d2ed281` and image
`sha256:709b5f664d8d8ea4941b836e58594b5868922e1737426575372818dfd3823aa0`
are superseded audit evidence after final Raze R2. The new exact candidate has
not yet been committed.

## Verification

- Reproducible second test-first baseline against pre-fix production:
  expected failure, `33/6/0/0`.
- Precommit focused controlled HTTP and support tests: PASS, `33/0/0/0`.
- Precommit full Docker Maven: BUILD SUCCESS, `587/0/0/3`.
- Scenario inventory: `20/20`, no missing literal Java anchors.
- Released ATS source audit: PASS against
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`.
- Committed coverage inventory: `240 total / 76 exact / 2 helper /
  115 candidate / 47 unmapped`; Advanced Filtering is `25/25 exact`.
- Reviewed mapping JSON parses, and the full Maven coverage verifier passes
  `23/0/0/0`.
- Exact image/runtime, dependency sabotage, credential, immutability, hygiene,
  and unmodified-local-OSH gates are pending.

## Primary Local OSH TeamEngine E2E

The prior candidate's unmodified local OSH execution was `238/40/7/191`. It is
retained only as superseded audit evidence. The same seven established
local-IUT failures were:

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
behavior is covered precommit by the `33/0/0/0` controlled HTTP run.

Artifact hygiene must be rerun from the new exact candidate.

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
