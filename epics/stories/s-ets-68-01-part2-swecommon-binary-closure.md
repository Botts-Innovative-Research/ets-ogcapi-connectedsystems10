# S-ETS-68-01: Part 2 SWE Common Binary Encoding Released ATS Closure

## Status
IMPLEMENTED_RAZE_APPROVED_PENDING_PUSH. Supersedes the historical Sprint 31
partial `/conf/swecommon-binary` subset with exact reviewed released ATS
mappings.

## User Instruction
Triggered by: "Make the practical fix, then continue with the project - and
don't stop unless you need my input."

## Scope
Close OGC 23-002 Clause 16.4 / Annex A.12 "SWE Common Binary Encoding" as eight
released procedures:

- `/req/swecommon-binary/mediatype-read`
- `/req/swecommon-binary/mediatype-write`
- `/req/swecommon-binary/obsschema-schema`
- `/req/swecommon-binary/obsschema-mapping`
- `/req/swecommon-binary/observation-encoding`
- `/req/swecommon-binary/cmdschema-schema`
- `/req/swecommon-binary/cmdschema-mapping`
- `/req/swecommon-binary/command-encoding`

## Requirements
- `Part2SweCommonBinaryTests` exposes exactly eight TestNG methods, one per
  released Annex A.12 target.
- Declaration, SWE Common Binary Encoding Rules prerequisite, and resource-class
  gates are setup/per-procedure gates, not standalone released ATS tests.
- `mediatype-read` requires service-desc API definition GET response-content
  advertisement plus concrete Observation or Command evidence with
  `Accept: application/swe+binary`, HTTP 200, exact `application/swe+binary`,
  and non-empty content before PASS, and SKIPs rather than passing when no safe
  candidate evidence exists.
- Schema procedures validate retrieved Observation Schema and Command Schema
  wrappers against the bundled Connected Systems schemas and dual-validate the
  extracted `recordSchema` through the reusable SWE Common adapter.
- Mapping procedures validate every retrieved schema and require canonical Time
  and IssueTime definition evidence from retrieved `recordSchema` content;
  missing or noncanonical IssueTime evidence in a retrieved Command Schema
  FAILs.
- Encoding procedures require parent schema and child Observation/Command
  evidence; without a proven binary data-value validator, they keep an explicit
  no-safe-evidence SKIP rather than media-only PASS.
- `mediatype-write` uses only non-mutating service-desc API definition metadata
  and requires every advertised scoped Observation/Command POST or PUT
  operation to expose exact `application/swe+binary` requestBody content.
- Tests and implementation comments cite `REQ-ETS-PART2-012` and Sprint 68
  scenarios.

## Verification Plan
- Formatter via Docker Maven.
- Focused Maven on SWE Common Binary, suite dependency, and coverage tests.
- Coverage mapping update plus coverage audit.
- Full Docker Maven.
- Mandatory local OSH TeamEngine E2E and no-mutation oracle.
- Raze adversarial review before completion.

## Acceptance
- [x] OpenSpec and traceability identify Sprint 68 as the exact closure story.
- [x] Structural tests fail before implementation for the missing exact
  Annex A.12 surface.
- [x] Runtime implementation exposes exactly eight released methods and no
  standalone helper methods.
- [x] Reviewed mappings promote `2:/conf/swecommon-binary` to
  `8 exact / 0 candidate / 0 unmapped`.
- [x] Raze review is approved with no unresolved required fixes.
- [ ] Completion evidence is committed and pushed.

## Implementation Evidence

Evidence directory:
`ops/test-results/sprint-ets-68-part2-swecommon-binary-2026-08-01/`.

- Test-first red: focused structural run failed before implementation because
  the exact Sprint 68 helper methods under test did not exist on the Sprint 31
  subset (`focused-red.txt`).
- Formatter passed after implementation and after Raze gapfixes
  (`formatter-apply.txt`, `formatter-after-raze-bridge.txt`).
- Initial Raze review found three high exactness gaps:
  missing API-definition read advertisement evidence, first-schema-only mapping
  checks, and absent IssueTime SKIP after Command Schema retrieval. All are
  fixed; focused Raze recheck returned `APPROVE_WITH_CONCERNS 0.94` with no
  blocking implementation, mapping, TestNG wiring, E2E honesty, or no-mutation
  gaps (`.harness/evaluations/sprint-ets-68-adversarial-recheck.yaml`).
- Final post-Raze-fix focused verification passed
  `120 tests / 0 failures / 0 errors / 0 skipped`
  (`focused-final-after-raze-fixes.txt`).
- Released ATS coverage update and audit passed. Current coverage is
  `240 total / 191 exact / 2 helper / 43 candidate / 4 unmapped`; Part 2 is
  `130 total / 100 exact / 0 helper / 26 candidate / 4 unmapped`; Part 2 SWE
  Common Binary is `8 exact / 0 candidate / 0 unmapped`.
- Full Docker Maven completed
  `785 tests / 0 failures / 0 errors / 3 skipped`
  (`full-maven-after-raze-fixes.txt`).
- Mandatory local OSH TeamEngine smoke ran the deployed stack and exited
  honestly non-green at `252 total / 23 passed / 20 failed / 209 skipped`
  (`local-osh-smoke-after-raze-fixes.txt`,
  `local-osh-smoke-after-raze-fixes.xml`,
  `local-osh-swecommon-binary-methods-after-raze-fixes.txt`). All eight
  Sprint 68 procedures SKIP before SWE Common Binary resource endpoint access
  because local OSH lacks
  `http://www.opengis.net/spec/SWE/3.0/conf/binary-encoding-rules`.
- No-mutation oracle recognized 130 IUT request logs; explicit method counts
  are `GET=130`, zero POST/PUT/PATCH/DELETE
  (`no-mutation-oracle-after-raze-fixes.txt`,
  `request-method-counts-after-raze-fixes.txt`).
- TeamEngine 6 runtime immutability verification passed for smoke image
  `sha256:47ddcfe1b7143004eba7b9fc88b5173fe1f6b7fa47616459a5d5efde14eee21e`
  (`teamengine-runtime-immutability-after-raze-fixes.txt`).

## Out of Scope
- IUT mutation or seed-resource creation.
- External validator dependency changes.
