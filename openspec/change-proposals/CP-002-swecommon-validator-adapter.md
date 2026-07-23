# CP-002: Integrate the SWE Common Validator Through a Local Adapter

> Status: Accepted by user instruction | Author: Codex | Date: 2026-07-22

## References

- Story: `epics/stories/s-ets-42-02-swecommon-validator-adapter.md`
- Spec: `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- Requirement: `REQ-ETS-VALIDATOR-001`
- Architecture: `_bmad/architecture.md` section 21
- Upstream: `opengeospatial/ets-swecommon30` PR 10, commit `3ba75ceabe57cea85f4a8513c59e0f90e386ba96`

## Motivation

The provisional architecture correctly selects `swecommon30-validator` instead of
the upstream TeamEngine ETS jar, but implementation analysis exposed a narrower
API boundary than the planning text implied. The upstream validator can validate a
JSON node against a named SWE Common schema such as `sweCommon.json`; it cannot
replace the Connected Systems Observation/Command wrapper schemas wholesale.

The upstream API also does not enable Draft 2020-12 format assertions, and its
`encodings.json` root omits `BinaryEncoding` even though that definition exists.
Removing the current local validation now could therefore weaken conformance
checks. The first implementation must run both validators at their respective
ownership boundaries and defer removal until parity and upstream support are
demonstrated.

## Accepted Changes

1. Permit a commit-pinned prebuild of `org.opengis.cite:swecommon30-validator`
   while no published release exists. The build must verify the exact Git commit,
   build only the parent plus reusable validator module, and never import
   `ets-swecommon30`. Supported Docker, developer-wrapper, and CI build paths must
   invoke the bootstrap. Release publication remains blocked until an accepted
   repository provides a non-SNAPSHOT reusable validator artifact.
2. Add `ConnectedSystemsSweValidatorAdapter` with an ETS-owned diagnostics result.
   NetworkNT result types must not escape the adapter.
3. Keep Connected Systems wrapper-schema validation local. Extract `recordSchema`
   from validated Observation/Command schema documents and delegate that pure SWE
   component to upstream `sweCommon.json`.
4. Preserve all existing media-type, encoding, mapping, binding, PASS/SKIP, and
   no-mutation behavior during the dual-validation stage.
5. Package the validator class and schema resources inside the slim shaded ETS jar,
   exclude its older NetworkNT/Jackson transitives, and prove no standalone
   duplicate jars enter TeamEngine.
6. Defer local SWE schema removal until the external validator alone passes the
   agreed fixture parity corpus with format assertions enabled and complete JSON,
   Text, and Binary encoding coverage.

## Impact

- **Spec/design**: refine `REQ-ETS-VALIDATOR-001` and add source-pin,
  dual-validation, upstream-limit, diagnostics, packaging, and E2E scenarios.
- **Code**: add the adapter and integrate it into the six Part 2 SWE Common
  Observation/Command schema assertions.
- **Build**: add a source-pinned validator bootstrap path to Docker, developer,
  and CI flows, plus dependency exclusions and shade inclusion. This provisional
  SNAPSHOT path is not a release-publication solution.
- **Tests**: add valid/invalid component parity, diagnostics-boundary, packaging,
  and runtime checks.
- **Operations**: run Docker Maven, inspect the shaded artifact, run the TeamEngine
  6 verifier, and attempt the primary local OSH smoke with exact evidence.

## Alternatives Rejected

- Import `ets-swecommon30`: couples two executable TeamEngine suites and expands
  the runtime closure.
- Remove local wrapper validation immediately: weakens format and Binary encoding
  checks that the current upstream API cannot replace.
- Depend on an unpinned snapshot or JitPack build: does not provide an accepted,
  reproducible OGC dependency boundary.
