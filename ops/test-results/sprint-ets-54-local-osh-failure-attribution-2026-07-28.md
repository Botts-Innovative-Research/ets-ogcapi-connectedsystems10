# Sprint ETS-54 Local OSH Failure Attribution

Date: 2026-07-28

## Baseline comparison

Sprint 53:
`220 total / 40 passed / 7 failed / 173 skipped`

Sprint 54:
`219 total / 40 passed / 7 failed / 172 skipped`

The one-method total reduction is expected because Sprint 54 replaces thirteen
historical GeoJSON methods with twelve released procedures. The pass and
failure counts are unchanged.

The seven failing methods are identical in both XML reports:

1. `PropertyDefinitionsTests#propertyCollectionsAreValid`
2. `SamplingFeaturesTests#samplingFeatureCollectionsAreValid`
3. `ProceduresTests#everyProcedureHasCanonicalUrl`
4. `ProceduresTests#procedureCollectionsAreValid`
5. `DeploymentsTests#deploymentCollectionsAreValid`
6. `DeploymentsTests#deploymentsReferencedFromSystemsAreValid`
7. `DeploymentsTests#everyDeploymentHasCanonicalUrl`

No Sprint 54 GeoJSON method failed. Each of the twelve GeoJSON methods appears
exactly once with `SKIP` status because unmodified local OSH did not supply the
required actual `application/geo+json` or usable API-definition evidence.
These outcomes are not positive conformance evidence.

Source reports:

- `sprint-ets-53-local-osh-teamengine-2026-07-28.xml`
- `sprint-ets-54-local-osh-teamengine-2026-07-28.xml`
