# S-ETS-42-01: External SWE Common and SensorML Validator Architecture

## Status

PLANNED_PROVISIONAL_ARCHITECTURE_DOCUMENTED

## User Instruction

Triggered by: "I forgot if I mentioned and had you document that we will need to pull in two libraries. One is for SWE Common (https://github.com/opengeospatial/ets-swecommon30) that LibbyShih is working on, and the other is a SensorML library that FCU-GIS-Luke is working on (these are their GitHub usernames). So, start a Research agent to analyze the SWE Common library work, try to find and analyze the SensorML library work, then build a provisional architecture for including them (replacing any homegrown work we might have done) and a plan to move towards that architecture."

## Goal

Document the external-domain-validator dependency strategy for SWE Common 3.0 and SensorML 3.0 before product code imports either library. The architecture must replace homegrown schema-validation logic where reusable OGC-owned validators exist while preserving Connected Systems-specific protocol, mapping, skip/fail, no-mutation, and TeamEngine behavior inside this ETS.

## Requirements

- `REQ-ETS-VALIDATOR-001`
- `REQ-ETS-PART1-013`
- `REQ-ETS-PART2-010`
- `REQ-ETS-PART2-011`
- `REQ-ETS-PART2-012`
- `REQ-ETS-TEAMENGINE-007`

## Scenarios

- `SCENARIO-ETS-VALIDATOR-EXTERNAL-LIBRARY-BOUNDARY-001`
- `SCENARIO-ETS-VALIDATOR-SWE-COMMON-ADAPTER-001`
- `SCENARIO-ETS-VALIDATOR-SENSORML-DISCOVERY-001`
- `SCENARIO-ETS-VALIDATOR-HOMEGROWN-REPLACEMENT-001`
- `SCENARIO-ETS-VALIDATOR-RUNTIME-CLOSURE-001`
- `SCENARIO-ETS-VALIDATOR-E2E-GATE-001`

## Acceptance Criteria

- [x] A Research agent analyzed the public SWE Common validator work and attempted to find the FCU-GIS-Luke SensorML library work.
- [x] The research artifact records repo URLs, branches, commits, artifact coordinates, Maven publication status, public API shape, license, dependency risks, and uncertainty.
- [x] OpenSpec defines the external-validator integration boundary and keeps Connected Systems-specific behavior local to this ETS.
- [x] Architecture documents the adapter-first dependency strategy and explicitly rejects importing another ETS jar as a domain library.
- [x] Traceability maps the provisional architecture to existing SensorML and SWE Common requirements.
- [x] Ops status, changelog, and test-results state that this is documentation/planning only and no product dependency was added.
- [x] S-ETS-42-02 added tests citing `REQ-ETS-VALIDATOR-001` before importing the validator dependency.
- [ ] Future implementation story proves Docker Maven, TeamEngine 6 runtime closure, and primary local OSH E2E after a validator dependency is introduced.

## Research Outcome

Research artifact: `ops/test-results/external-validator-library-research-2026-07-22.md`.

SWE Common public state on 2026-07-22:

- `opengeospatial/ets-swecommon30` PR 10 splits reusable validation into `org.opengis.cite:swecommon30-validator:0.1-SNAPSHOT`.
- Relevant branch `issue-9-swecommon-validation-module` was observed at `3ba75ceabe57cea85f4a8513c59e0f90e386ba96`.
- The public API centers on `org.opengis.cite.swecommon30.validation.SweCommonJsonSchemaValidator`.
- The artifact is not published to Maven Central yet.

SensorML public state on 2026-07-22:

- `FCU-GIS-Luke` has no public repositories visible, and no public SensorML library was found under that username.
- `opengeospatial/ets-sensorml30` exists at `d2b2a6308fdf48f113f7c7faed6712dc05e33130`, but it is an ETS scaffold, not a reusable validator module.
- The ETS must not depend directly on `ets-sensorml30`; ask FCU/OGC for the private/unpublished library coordinates or propose extracting `sensorml30-validator`.

## Provisional Architecture

Add a local adapter layer when implementation begins:

- `ConnectedSystemsSweValidatorAdapter` delegates pure SWE Common schema validation to `swecommon30-validator`.
- `ConnectedSystemsSensorMlValidatorAdapter` remains a placeholder until a reusable SensorML module is visible.
- Both adapters return ETS-friendly diagnostics with the correct OGC requirement URI and never own TestNG skip/fail policy.

The ETS keeps ownership of:

- CS API resource discovery and candidate selection.
- `/conformance` declaration and prerequisite gates.
- exact media-type evidence.
- no-mutation and public-IUT hard-denial policies.
- TeamEngine report integration.
- Connected Systems mapping assertions and Observation/Command parent-child binding evidence.

## Migration Plan

1. Confirm upstream artifacts.
   - SWE: wait for PR 10 merge and Maven publication, or document a source-pinned local-install/prebuild path.
   - SensorML: obtain the exact FCU/OGC repo, branch, artifact coordinates, and reusable API, or plan an upstream split from `ets-sensorml30`.

2. Add dependency and runtime guards.
   - Add dependency management/exclusions for validator dependencies.
   - Extend runtime verification to detect duplicate NetworkNT, ITU, Jackson, SLF4J, Jakarta, TeamEngine, and TestNG jar families.
   - Keep external validator runtime types behind the adapter; expose ETS-owned diagnostics to test classes rather than NetworkNT `ValidationMessage`.
   - Do not patch TeamEngine-owned files.

## Raze Review

Raze artifact: `.harness/evaluations/sprint-ets-42-external-validator-architecture-adversarial-2026-07-22.yaml`.

Verdict: `APPROVE_WITH_CONCERNS`, confidence `0.91`, no required fixes. The single low concern was to keep NetworkNT public API coupling visible for future implementation; this story and the OpenSpec/design boundary now require adapter-local conversion to ETS-owned diagnostics.

3. Replace SWE schema-shape validation first.
   - Add parity tests for existing valid/invalid SWE schema fixtures.
   - Route `Part2SchemaValidation` SWE component validation through the adapter.
   - Preserve existing PASS/SKIP behavior for Observation/Command encoded-body assertions until validator support is explicit.

4. Replace SensorML schema-shape validation only after a reusable module exists.
   - Replace minimal `SensorMlTests` shape checks with full SensorML 3.0 JSON Schema validation.
   - Keep deployment/procedure/property mapping and relation-type logic local.

5. Verify implementation.
   - Docker Maven via `bash scripts/mvn-test-via-docker.sh`.
   - TeamEngine 6 runtime verifier.
   - Mandatory local OSH TeamEngine smoke from a clean `/tmp` clone with exact totals and no-mutation evidence.

## Non-Goals

- Adding either dependency to `pom.xml` in this planning story.
- Importing the upstream SWE or SensorML ETS suite jars as dependencies.
- Changing current conformance PASS/SKIP behavior without tests and E2E evidence.
- Treating GeoRobotix advisory evidence as the primary development gate.

## Implementation Successor

S-ETS-42-02 implements the first SWE Common adapter increment with exact source
pinning, ETS-owned diagnostics, dual validation, shaded runtime packaging, and
Maven/runtime/advisory evidence. Its primary local OSH E2E gate passes
`211/69/0/142` with zero writes. SensorML remains deferred.
