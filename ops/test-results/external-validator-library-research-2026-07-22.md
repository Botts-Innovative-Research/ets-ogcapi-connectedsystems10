# External SWE Common and SensorML Validator Research - 2026-07-22

## Scope

User instruction: analyze the SWE Common library work by LibbyShih, find and analyze the SensorML library work by FCU-GIS-Luke, then build a provisional architecture for incorporating both libraries and replacing homegrown validation where appropriate.

Research was performed by the main agent and read-only Research sub-agent Mendel (`019f8810-b0e7-7b03-ae47-4f6584c4ee82`). Mendel's reported execution window was `2026-07-22T04:23:53Z` to `2026-07-22T04:26:49Z`; token metadata was not available from the sub-agent result.

## SWE Common Findings

- Public repo: <https://github.com/opengeospatial/ets-swecommon30>
- Relevant branch / PR: `issue-9-swecommon-validation-module`, PR <https://github.com/opengeospatial/ets-swecommon30/pull/10>
- Issue: <https://github.com/opengeospatial/ets-swecommon30/issues/9>
- Branch HEAD observed on 2026-07-22: `3ba75ceabe57cea85f4a8513c59e0f90e386ba96`
- Commit author on that branch: `LibbyShih <libbyshih@gis.tw>`
- Parent artifact: `org.opengis.cite:swecommon30-parent:0.1-SNAPSHOT`
- Reusable artifact target: `org.opengis.cite:swecommon30-validator:0.1-SNAPSHOT`
- ETS artifact in same repo: `org.opengis.cite:ets-swecommon30:0.1-SNAPSHOT`
- Maven Central status on 2026-07-22: no `org.opengis.cite:swecommon30-validator` artifact found.
- License: Apache-2.0 text in upstream `LICENSE.txt`.

The intended import target is `swecommon30-validator`, not the upstream `ets-swecommon30` suite jar. The validator module is explicitly separated from TestNG and TeamEngine workflow code.

Observed public API:

```java
org.opengis.cite.swecommon30.validation.SweCommonJsonSchemaValidator

JsonNode readJson(File jsonFile)
Set<ValidationMessage> validate(JsonNode document, String schemaName)
String formatValidationErrors(String componentName, Set<ValidationMessage> errors)
```

The validator bundles SWE Common 3.0 JSON schemas under `org/opengis/cite/swecommon30/jsonschema/sweCommon/3.0/json/` and currently covers JSON Schema shape validation. It does not own Connected Systems endpoint discovery, `/conformance` gating, exact media type negotiation, TestNG pass/fail/skip semantics, no-mutation policy, TeamEngine reporting, or Connected Systems-specific mapping assertions.

Dependency risk:

- Upstream validator branch pins `com.networknt:json-schema-validator:1.5.4` and `jackson-databind:2.17.2`.
- This ETS currently uses `json-schema-validator:1.5.9` with NetworkNT/ITU relocation in the TeamEngine 6 runtime path, plus Jackson around the `ets-common:17`/Kaizen dependency closure.
- FCU-GIS-Luke's public comments on the Connected Systems ETS issue <https://github.com/Botts-Innovative-Research/ets-ogcapi-connectedsystems10/issues/2> already flag duplicate jar and classpath risks around TeamEngine, SLF4J, OpenAPI parser, Jackson/Jakarta, and NetworkNT-style libraries.

## SensorML Findings

- The GitHub user exists: <https://github.com/FCU-GIS-Luke>
- Public GitHub API/search observations on 2026-07-22: `FCU-GIS-Luke` exposes zero public repositories, no public SensorML repo, and no public SensorML branch or commit attributable to that username.
- Public candidate repo: <https://github.com/opengeospatial/ets-sensorml30>
- Default branch HEAD observed on 2026-07-22: `d2b2a6308fdf48f113f7c7faed6712dc05e33130`
- Artifact in that repo: `org.opengis.cite:ets-sensorml30:0.1-SNAPSHOT`
- Maven Central status on 2026-07-22: no `org.opengis.cite:ets-sensorml30` or `sensorml30-validator` artifact found.
- License: Apache-2.0 text in upstream `LICENSE.txt`.

The public SensorML candidate is an ETS scaffold, not a reusable validator library. It has TeamEngine/TestNG code and bundled SensorML/SWE schemas in the suite jar. The currently visible helper, `org.opengis.cite.sensorml30.BaseJsonSchemaValidatorTest`, is not a clean consumer API, and one visible enabled test validates a `PhysicalSystem.json` example. Directly depending on this ETS jar would couple two TeamEngine suites and amplify runtime classpath risk.

The user's note that FCU-GIS-Luke is working on a SensorML library may still be correct, but the library is not publicly discoverable from current primary sources. It may be private, unpublished, in a fork without public visibility, or not yet pushed.

## Local Code To Replace Or Wrap

Replace or delegate when stable reusable validators exist:

- SWE Common schema validation helpers in:
  - `src/main/java/org/opengis/cite/ogcapiconnectedsystems10/conformance/part2/Part2SchemaValidation.java`
  - `src/main/java/org/opengis/cite/ogcapiconnectedsystems10/conformance/part2/swecommonjson/Part2SweCommonJsonTests.java`
  - `src/main/java/org/opengis/cite/ogcapiconnectedsystems10/conformance/part2/swecommontext/Part2SweCommonTextTests.java`
  - `src/main/java/org/opengis/cite/ogcapiconnectedsystems10/conformance/part2/swecommonbinary/Part2SweCommonBinaryTests.java`
- Bundled SWE schema copies under `src/main/resources/schemas/connected-systems-shared/swecommon/`, after parity tests prove the upstream validator covers the same schema names and diagnostics.
- SensorML shape/schema heuristics in `src/main/java/org/opengis/cite/ogcapiconnectedsystems10/conformance/sensorml/SensorMlTests.java`, once a reusable SensorML validator module exists.
- Bundled SensorML schema copies under `src/main/resources/schemas/connected-systems-shared/sensorml/` and `src/main/resources/schemas/connected-systems-1/sensorml/`, after parity tests prove equivalent coverage.

Keep ETS-owned:

- CS API resource discovery and candidate selection.
- `/conformance` declaration and prerequisite gating.
- Exact media-type checks: `application/sml+json`, `application/swe+json`, `application/swe+text`, `application/swe+binary`.
- TestNG group dependencies, pass/fail/skip policy, and TeamEngine report integration.
- No-mutation/public-IUT safety gates.
- Connected Systems mapping rules: selected system identity, deployment/procedure/property mapping, Observation/Command schema endpoint selection, canonical Time/IssueTime checks, API definition write-advertisement checks, and cross-class binding evidence.
- TeamEngine 6 packaging/runtime discipline.

## Provisional Architecture

Introduce a thin Connected Systems domain-validator adapter layer only after a reproducible upstream artifact or source-pinned prebuild exists.

Planned local adapter boundary:

- `ConnectedSystemsSweValidatorAdapter`
  - Delegates pure SWE Common JSON component/schema validation to `org.opengis.cite:swecommon30-validator`.
  - Converts `Set<ValidationMessage>` into ETS assertion diagnostics that cite the relevant OGC 23-002 requirement URI.
  - Leaves media negotiation, schema endpoint selection, mapping assertions, TestNG skip/fail behavior, and no-mutation policy in this ETS.

- `ConnectedSystemsSensorMlValidatorAdapter`
  - Remains provisional until FCU/OGC provide a real reusable SensorML validator module or `opengeospatial/ets-sensorml30` is split into a validator module analogous to `swecommon30-validator`.
  - Must not import `ets-sensorml30` directly as a suite dependency.

Dependency policy:

- Prefer a published OGC artifact. If schedule requires a pre-publication dependency, use a documented local-install or CI prebuild pinned to an upstream commit.
- Keep all external validator jars inside the ETS dependency/runtime closure; do not patch, replace, or broaden-copy TeamEngine-owned files.
- Use dependency management/exclusions or shading so the TeamEngine 6 + `ets-common:17` runtime retains one coherent NetworkNT, ITU, Jackson, SLF4J, Jakarta, and TestNG family.
- Add a runtime-closure verifier before accepting the first external validator dependency.

## Migration Plan

1. Track upstream readiness.
   - SWE: wait for PR 10 merge and either Maven publication or a documented commit-pinned prebuild path.
   - SensorML: ask FCU/OGC for the exact repo, branch, artifact coordinates, and intended reusable API. If no reusable module exists, propose extracting `sensorml30-validator` from `opengeospatial/ets-sensorml30`.

2. Add the local adapter layer and parity tests.
   - Start with SWE Common JSON schema-shape validation because the public validator API is already visible.
   - Fixture tests must cite `REQ-ETS-VALIDATOR-001` and prove current invalid schema fixtures still fail through the adapter.

3. Add dependency/runtime verification.
   - Introduce dependency management and explicit exclusions before copying any new jar into the TeamEngine image.
   - Extend the runtime verifier to catch duplicate validator/runtime jars and classloader collisions.

4. Replace local schema-shape validation incrementally.
   - Replace `Part2SchemaValidation` calls for SWE Common schema documents only after parity coverage passes.
   - Keep current tests' PASS/SKIP behavior unchanged during the first migration slice.
   - Keep Observation/Command encoded-body semantic checks skipped until a validator explicitly supports those encoding rules with real parent/child evidence.

5. Migrate SensorML only when reusable library evidence exists.
   - Do not depend on `ets-sensorml30` as-is.
   - When a reusable SensorML validator module exists, replace `SensorMlTests` minimal shape checks with full SensorML 3.0 JSON Schema validation while retaining Connected Systems-specific mapping checks locally.

6. Verify end to end.
   - Run Docker Maven, the TeamEngine 6 runtime verifier, and the mandatory local OSH TeamEngine smoke from a clean `/tmp` clone.
   - Archive exact TestNG totals and no-mutation evidence before any implementation story claims completion.

## Decision Summary

Document the dependency need now, but do not add either library to the POM yet. SWE Common has a plausible reusable validator module but no published artifact. SensorML has no public reusable validator module attributable to FCU-GIS-Luke. The correct provisional architecture is adapter-first, dependency-managed, and incremental: external libraries validate domain schema semantics; this ETS continues to own Connected Systems protocol behavior and TeamEngine execution semantics.
