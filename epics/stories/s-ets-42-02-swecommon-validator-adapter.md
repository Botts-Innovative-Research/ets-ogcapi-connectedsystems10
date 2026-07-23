# S-ETS-42-02: SWE Common Validator Adapter Implementation

## Status

COMPLETE

## User Instruction

Triggered by: "Make sure the notes you just provided are documented appropriately
in our specs, then move forward on the final gate."

## Goal

Integrate the reusable SWE Common validator at its actual API boundary without
weakening existing Connected Systems wrapper, format, encoding, mapping, binding,
PASS/SKIP, no-mutation, or TeamEngine runtime behavior.

## Requirements

- `REQ-ETS-VALIDATOR-001`
- `REQ-ETS-PART2-010`
- `REQ-ETS-PART2-011`
- `REQ-ETS-PART2-012`
- `REQ-ETS-TEAMENGINE-007`

## Scenarios

- `SCENARIO-ETS-VALIDATOR-SWE-COMMON-ADAPTER-001`
- `SCENARIO-ETS-VALIDATOR-SOURCE-PIN-001`
- `SCENARIO-ETS-VALIDATOR-SWE-COMMON-DUAL-VALIDATION-001`
- `SCENARIO-ETS-VALIDATOR-SWE-COMMON-PARITY-CORPUS-001`
- `SCENARIO-ETS-VALIDATOR-SWE-COMMON-UPSTREAM-LIMITS-001`
- `SCENARIO-ETS-VALIDATOR-DIAGNOSTICS-BOUNDARY-001`
- `SCENARIO-ETS-VALIDATOR-RUNTIME-CLOSURE-001`
- `SCENARIO-ETS-VALIDATOR-RUNTIME-EXECUTION-001`
- `SCENARIO-ETS-VALIDATOR-E2E-GATE-001`

## Acceptance Criteria

- [x] A commit-pinned prebuild verifies the exact upstream SHA and builds only
  `swecommon30-validator`, never `ets-swecommon30`.
- [x] Supported Java 17 CI build and release definitions invoke the same bootstrap
  and request only declared Maven profiles; release remains blocked until a
  non-SNAPSHOT reusable artifact is published.
- [x] The POM excludes upstream NetworkNT/Jackson versions and packages validator
  classes/resources in the slim shaded ETS jar.
- [x] The adapter exposes only ETS-owned diagnostics and distinguishes IUT
  validation failures from suite configuration/resource failures.
- [x] Valid and invalid SWE component fixtures prove the adapter boundary.
- [x] All six complete Observation/Command JSON/Text/Binary wrappers pass local
  validation and the extracted-component adapter, with active requirement-URI and
  operational-error behavior covered.
- [x] The six SWE JSON/Text/Binary Observation/Command schema assertions retain
  local wrapper validation and additionally validate extracted `recordSchema`.
- [x] Existing media-type, encoding, mapping, binding, PASS/SKIP, and no-mutation
  behavior remains unchanged.
- [x] Packaging tests and the runtime verifier prove there is no standalone,
  split-version, or cross-version duplicate TeamEngine dependency in the final image,
  and the behavioral self-test asserts the complete exact multi-tuple guard output.
- [x] The final-image runtime verifier executes valid and invalid adapter calls on
  the deployed classpath, including shaded schema resolution and relocated NetworkNT.
- [x] Exact replacement image `sha256:829a97414c07dd5763ed302e32b3178d301ca098bc9025f4b1f58b692ddad5f9` passes Docker Maven, shaded-jar inspection,
  TeamEngine 6 runtime verification, and primary local OSH TeamEngine E2E with its
  exact image ID emitted directly in the gate outputs.
- [x] Final Raze reviewed the replacement image and returned `APPROVE` at `0.99` confidence with no required actions.

## Implementation Boundary

This story is the dual-validation stage. It does not remove local SWE schemas or
delegate `encoding` validation because the upstream API does not yet enable format
assertions and does not expose Binary encoding through the root of
`encodings.json`. SensorML integration remains outside this story.

## Verification

- Adapter/parity focused Maven: `19/0/0/0`; final packaging focused Maven:
  `11/0/0/0` after expected test-first failure `11/1/0/0`.
- Fresh full Docker Maven: `312/0/0/3`.
- Exact replacement image `sha256:829a97414c07dd5763ed302e32b3178d301ca098bc9025f4b1f58b692ddad5f9`
  and strengthened TeamEngine 6 runtime verifier: PASS, including isolated
  message resources, generic jar-content checks, and valid/invalid adapter execution.
- Advisory GeoRobotix: unchanged Sprint 41 baseline `211/38/34/139`, zero
  writes, and no adapter/linkage regressions; these public paths did not reach
  the adapter and are not the positive runtime proof.
- Primary local OSH: PASS from a fresh `/tmp` clone synchronized to the final
  worktree, `211/69/0/142`; the no-mutation oracle recognized 135 IUT requests
  and found zero POST/PUT/PATCH/DELETE requests.
- Evidence:
  `ops/test-results/sprint-ets-42-final-raze-gapfix-verification-2026-07-22.md`.

The primary local OSH E2E gate now passes. The earlier final Raze recheck
returned `PASS_WITH_EXTERNAL_BLOCKER`, confidence `0.99`; the external blocker
was subsequently cleared by the archived local OSH run.

The later replacement-image Raze recheck returned `GAPS_FOUND`, confidence
`0.99`. It confirmed direct image provenance and removal of the GA TeamEngine
resources jar, but found unisolated `jsv-messages*.properties`, filename-limited
coordinate scanning, no generic functional-path intersection, stale Maven
chronology, and an empty `.moduledata` directory. The current candidate closes
those implementation gaps. A subsequent generic-guard recheck required explicit
accepted collision tuples, unused-allowlist rejection, Jenkins profile cleanup,
and final metadata reconciliation. The first metadata recheck found release-CI
and executable tuple-output coverage gaps; those findings and all fresh gates
are now remediated. The final Raze recheck returned `APPROVE` at `0.99`
confidence with no required actions.
