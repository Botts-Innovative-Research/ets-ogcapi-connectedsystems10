# Change Proposal: CP-004 Reproducible Populated Local OSH E2E

**Date**: 2026-07-23
**Author**: Codex
**Affects**:

- `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- `openspec/capabilities/ets-ogcapi-connectedsystems/design.md`
- `REQ-ETS-PART2-013`
- `REQ-ETS-TEAMENGINE-006`

**Status**: Accepted by user instruction ("Make it so")

## Motivation

The primary local OSH gate is intentionally clean and read-only. It proves
TeamEngine integration but leaves dynamic-resource assertions skipped. Earlier
populated runs depended on manual state changes, tasking drivers, and datastore
restoration. They are useful history but are not a reproducible current gate.

ADR-012 permits supported IUT configuration and test-data creation while
prohibiting OSH or TeamEngine source/binary changes. The project therefore needs
an ETS-owned workflow that provisions a separate local OSH process from the
clean external distribution, populates it only through the public Connected
Systems API, executes TeamEngine, records honest conformance failures, removes
the ephemeral state, and verifies that the clean primary IUT remains healthy.

## Modified Requirements

### REQ-ETS-PART2-013

Add an ETS-owned reproducible populated-IUT workflow. Provisioning readiness and
TeamEngine conformance are separate verdicts: successful fixture creation never
converts an IUT conformance failure into PASS.

### REQ-ETS-TEAMENGINE-006

Retain the clean local OSH instance as the primary development gate. Allow a
separate ephemeral populated local OSH instance as required supplemental E2E
evidence for dynamic-resource work.

## New Scenarios

- `SCENARIO-ETS-PART2-013-EPHEMERAL-POPULATED-IUT-001`
- `SCENARIO-ETS-PART2-013-POPULATED-PROVISIONING-VERDICT-001`
- `SCENARIO-ETS-PART2-013-POPULATED-EVIDENCE-001`
- `SCENARIO-ETS-PART2-013-PRIMARY-STATE-ISOLATION-001`

## Impact Analysis

- **Code changes required**: add an exact populated fixture manifest, a
  mutation-gated API seeder, a Docker lifecycle/E2E orchestrator, and structural
  regression coverage.
- **Test changes required**: tests must prove hard mutation gates, public-target
  denial, read-only external OSH mounting, evidence fields, failure
  preservation, cleanup, and clean-primary rerun.
- **Operational changes required**: document the command and archive current
  populated and clean-primary results.
- **External changes prohibited**: no OSH or TeamEngine source, binary, or
  datastore changes outside the ephemeral workflow.

## Acceptance Boundary

A populated target is acceptable for E2E when it is reproducibly provisioned
from an unmodified OSH distribution, receives fixtures through supported APIs,
is reachable by TeamEngine, produces a non-empty TestNG report, and is cleaned
up deterministically. Its conformance verdict remains the exact TestNG verdict.
The workflow must exit non-zero when TestNG reports failures.

## Implementation Outcome

The accepted design is implemented with owned per-run containers, a
container-bound loopback seeding capability, digest/source/install provenance,
XML-only TestNG verdicts, observable cleanup, abort-safe finalization, normalized
primary-state comparison, and complete hashed evidence.

The final run provisioned all required resources and completed TeamEngine:
populated TestNG `211/91/28/92` is an honest conformance `FAIL`; cleanup passed;
the primary remained unchanged; clean-primary TestNG passed `211/69/0/142`.
This outcome satisfies the proposal's E2E acceptance boundary without claiming
that unmodified OSH passes the populated conformance profile.

Focused Raze recheck returned `APPROVE` at confidence `0.99`, closed all ten
initial findings, and found no new required work.
