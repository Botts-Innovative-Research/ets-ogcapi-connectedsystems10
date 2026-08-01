# S-ETS-67-01: Part 2 SWE Common Text Encoding Released ATS Closure

## Status
RAZE APPROVED; PUSH PENDING. Supersedes the historical Sprint 30 partial
`/conf/swecommon-text` subset with exact reviewed released ATS mappings.

## User Instruction
Triggered by: "Make the practical fix, then continue with the project - and
don't stop unless you need my input."

## Scope
Close OGC 23-002 Clause 16.3 / Annex A.11 "SWE Common Text Encoding" as eight
released procedures:

- `/req/swecommon-text/mediatype-read`
- `/req/swecommon-text/mediatype-write`
- `/req/swecommon-text/obsschema-schema`
- `/req/swecommon-text/obsschema-mapping`
- `/req/swecommon-text/observation-encoding`
- `/req/swecommon-text/cmdschema-schema`
- `/req/swecommon-text/cmdschema-mapping`
- `/req/swecommon-text/command-encoding`

## Requirements
- `Part2SweCommonTextTests` exposes exactly eight TestNG methods, one per
  released Annex A.11 target.
- Declaration, SWE Common Text Encoding Rules prerequisite, and resource-class
  gates are setup/per-procedure gates, not standalone released ATS tests.
- `mediatype-read` requests concrete Observation or Command evidence with
  `Accept: application/swe+text`, requires HTTP 200, exact
  `application/swe+text`, and non-empty content before PASS, and SKIPs rather
  than passing when no safe candidate evidence exists.
- Schema procedures validate retrieved Observation Schema and Command Schema
  wrappers against the bundled Connected Systems schemas and dual-validate the
  extracted `recordSchema` through the reusable SWE Common adapter.
- Mapping procedures require canonical Time and IssueTime definition evidence
  from retrieved `recordSchema` content.
- Encoding procedures require parent schema and child Observation/Command
  evidence; without a proven data-value validator, they keep an explicit
  no-safe-evidence SKIP rather than media-only PASS.
- `mediatype-write` uses only non-mutating service-desc API definition metadata
  and requires every advertised scoped Observation/Command POST or PUT
  operation to expose exact `application/swe+text` requestBody content.
- Tests and implementation comments cite `REQ-ETS-PART2-011` and Sprint 67
  scenarios.

## Verification Plan
- Formatter via Docker Maven.
- Focused Maven on SWE Common Text, suite dependency, and coverage tests.
- Coverage mapping update plus coverage audit.
- Full Docker Maven.
- Mandatory local OSH TeamEngine E2E and no-mutation oracle.
- Raze adversarial review before completion.

## Acceptance
- [x] OpenSpec and traceability identify Sprint 67 as the exact closure story.
- [x] Structural tests fail before implementation for the missing exact
  Annex A.11 surface.
- [x] Runtime implementation exposes exactly eight released methods and no
  standalone helper methods.
- [x] Reviewed mappings promote `2:/conf/swecommon-text` to
  `8 exact / 0 candidate / 0 unmapped`.
- [x] Raze review is approved with no unresolved required fixes.
- [ ] Completion evidence is committed and pushed.

## Implementation Evidence

Evidence directory:
`ops/test-results/sprint-ets-67-part2-swecommon-text-2026-08-01/`.

- Test-first red: focused structural run failed before implementation because
  the exact Sprint 67 helper method under test did not exist
  (`focused-red.txt`).
- Formatter passed after implementation and after suite-label cleanup
  (`formatter.txt`, `formatter-after-suite-labels.txt`). Post-Raze-fix
  formatter passed (`formatter-after-raze-fixes.txt`).
- Post-Raze-fix focused verification passed
  `115 tests / 0 failures / 0 errors / 0 skipped`
  (`focused-after-raze-fixes.txt`).
- Released ATS coverage update and audit passed. Current coverage is
  `240 total / 183 exact / 2 helper / 50 candidate / 5 unmapped`; Part 2 is
  `130 total / 92 exact / 0 helper / 33 candidate / 5 unmapped`; Part 2 SWE
  Common Text is `8 exact / 0 candidate / 0 unmapped`.
- Post-Raze-fix full Docker Maven completed
  `775 tests / 0 failures / 0 errors / 3 skipped`
  (`full-maven-after-raze-fixes.txt`).
- Post-Raze-fix mandatory local OSH TeamEngine smoke ran the deployed stack and exited
  honestly non-green at `254 total / 25 passed / 20 failed / 209 skipped`
  (`local-osh-smoke-after-raze-fixes.txt`,
  `local-osh-smoke-after-raze-fixes.xml`,
  `local-osh-swecommon-text-methods-after-raze-fixes.txt`). All eight Sprint
  67 procedures SKIP before SWE Common Text resource endpoint access because
  local OSH lacks
  `http://www.opengis.net/spec/SWE/3.0/conf/text-encoding-rules`.
- No-mutation oracle recognized 137 IUT request logs; explicit method counts
  are `GET=137`, zero POST/PUT/PATCH/DELETE
  (`no-mutation-oracle-after-raze-fixes.txt`,
  `request-method-counts-after-raze-fixes.txt`).
- TeamEngine 6 runtime immutability verification passed for smoke image
  `sha256:84423839aa6f4e5209b679a46ddf6a7cfbb3cbc3eb737ecce25d4e9d65167b0c`
  (`teamengine-runtime-immutability-after-raze-fixes.txt`).
- Initial Raze found `RAZE-ETS67-FALSESKIP-001` and `RAZE-ETS67-DOC-001`.
  Both are closed by the focused recheck, which returned `APPROVE 0.96` with
  `required_fixes: []` (`.harness/evaluations/sprint-ets-67-adversarial.yaml`).

## Out of Scope
- IUT mutation or seed-resource creation.
- SWE Common Binary exact closure.
- External validator dependency changes.
