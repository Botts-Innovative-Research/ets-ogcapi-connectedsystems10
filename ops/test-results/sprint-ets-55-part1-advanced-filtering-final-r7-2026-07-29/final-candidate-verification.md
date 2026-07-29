# Sprint 55 Final R7 Candidate Verification

Date: 2026-07-29

## Candidate

- Commit: `b5bc49b2922e0a47b73225c2dabc0422ac7998f4`
- Source: clean detached clone at the commit above
- Image:
  `sha256:b883633f236d2e51d63d0245ceab33ba286f22370cb409f7c83a36f3046aec42`
- Deployed jar manifest: `Build-Revision: b5bc49b292`
- TeamEngine base:
  `sha256:9d53016965c4cca0f4d8baec136ad11258dccc552e9a8c969a1a6f889336d90c`

## Verification

- R6 remediation test-first baseline: historical unarchived observation,
  `46/3/0/0`.
- Exact focused controlled HTTP and support tests: PASS, `51/0/0/0`.
- Exact full Docker Maven: BUILD SUCCESS, `605/0/0/3`.
- Released ATS source audit: PASS against
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`.
- R6's permissive relation-matching finding is covered by three new regression
  tests. Matching now uses separate exact, case-sensitive vocabularies for
  GeoJSON properties, SensorML fields, generic fields, and link relations. It
  does not remove punctuation, strip a trailing `Link`, or accept broad
  `parent` and `procedure` aliases as field evidence.
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
conformance. Positive semantics are covered by the exact `51/0/0/0`
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

`exact-b5bc49b-artifact-manifest.sha256` covers every SHA-prefixed exact
artifact in this directory; all 17 entries pass `sha256sum -c`.

## Adversarial Review

Fresh Raze R7 returned `GAPS_FOUND 0.99`. Exact case, punctuation, suffix, and
broad-alias controls pass, but `links[].rel` is still evaluated before
representation dispatch. A SensorML System can therefore reuse GeoJSON
`parentSystem` or `ogc-rel:parentSystem` evidence instead of the released
`attachedTo` mapping. Candidate `b5bc49b` is superseded.
