# Sprint 50 Test-First Red Gate

- Date: 2026-07-26
- Command:
  `bash scripts/mvn-test-via-docker.sh -Dtest=VerifyProceduresSuite,VerifyProcedureFeaturesSupport,VerifyProceduresHttpProcedures,VerifyTestNGSuiteDependency`
- Result: expected `testCompile` failure
- Failure count: 39 missing-symbol errors
- Missing production surface:
  - `ProcedureFeaturesSupport`
  - `ProceduresTests.configure(URI)`
  - `procedureLocationIsAbsent()`
  - `everyProcedureHasCanonicalUrl()`
  - `procedureResourcesEndpointIsValid()`
  - `canonicalProceduresEndpointIsValid()`
  - `procedureCollectionsAreValid()`

The repository Maven harness first bootstrapped the pinned SWE Common validator
source at commit `3ba75ceabe57cea85f4a8513c59e0f90e386ba96`. Formatting validation passed
before compilation reached the intended missing implementation surface.
