# S-ETS-66-01: Part 2 SWE Common JSON Encoding Released ATS Closure

## Status
DONE - PUSHED. Supersedes the historical Sprint 29 partial
`/conf/swecommon-json` subset with exact reviewed released ATS mappings.

## User Instruction
Triggered by: "Continue with Part 2" after Sprint 65 Part 2 JSON Encoding was
pushed and reconciled.

## Scope
Close OGC 23-002 Clause 16.2 / Annex A.10 "SWE Common JSON Encoding" as eight
released procedures:

- `/req/swecommon-json/mediatype-read`
- `/req/swecommon-json/mediatype-write`
- `/req/swecommon-json/obsschema-schema`
- `/req/swecommon-json/obsschema-mapping`
- `/req/swecommon-json/observation-encoding`
- `/req/swecommon-json/cmdschema-schema`
- `/req/swecommon-json/cmdschema-mapping`
- `/req/swecommon-json/command-encoding`

## Requirements
- `Part2SweCommonJsonTests` exposes exactly eight TestNG methods, one per
  released Annex A.10 target.
- Declaration, SWE Common JSON Encoding Rules prerequisite, and resource-class
  gates are setup/per-procedure gates, not standalone released ATS tests.
- `mediatype-read` requests concrete Observation or Command evidence with
  `Accept: application/swe+json`, requires HTTP 200, exact
  `application/swe+json`, and parseable JSON content before PASS, and SKIPs
  rather than passing when no safe candidate evidence exists.
- Schema procedures validate retrieved Observation Schema and Command Schema
  wrappers against the bundled Connected Systems schemas and dual-validate the
  extracted `recordSchema` through the reusable SWE Common adapter.
- Mapping procedures require canonical Time and IssueTime definition evidence
  from retrieved `recordSchema` content.
- Encoding procedures require parent schema and child Observation/Command
  evidence; without a proven data-encoding validator, they keep an explicit
  no-safe-evidence SKIP rather than shape-only PASS.
- `mediatype-write` uses only non-mutating service-desc API definition metadata
  and requires every advertised scoped Observation/Command POST or PUT
  operation to expose exact `application/swe+json` requestBody content.
- Tests and implementation comments cite `REQ-ETS-PART2-010` and Sprint 66
  scenarios.

## Verification Plan
- Formatter via Docker Maven.
- Focused Maven on SWE Common JSON, suite dependency, and coverage tests.
- Coverage mapping update plus coverage audit.
- Full Docker Maven.
- Mandatory local OSH TeamEngine E2E and no-mutation oracle.
- Raze adversarial review before completion.

## Acceptance
- [x] OpenSpec and traceability identify Sprint 66 as the exact closure story.
- [x] Structural tests fail before implementation for the missing exact
  Annex A.10 surface.
- [x] Runtime implementation exposes exactly eight released methods and no
  standalone helper methods.
- [x] Reviewed mappings promote `2:/conf/swecommon-json` to
  `8 exact / 0 candidate / 0 unmapped`.
- [x] Raze review is approved with no unresolved required fixes.
- [x] Completion evidence is committed and pushed.

## Implementation Evidence

- Structural test-first gaps were observed during implementation but the first
  red run was not archived durably; final structural verification is archived
  in `focused-maven.txt`.
- Formatter passed in `formatter.txt`.
- Focused Docker Maven passed `114 tests / 0 failures / 0 errors / 0 skipped`
  across SWE Common JSON structural tests, TestNG dependency checks, and
  released ATS coverage in `focused-maven.txt`.
- Coverage update/audit passed after evidence-wording reconciliation; current
  coverage is `240 total / 175 exact / 2 helper / 57 candidate / 6 unmapped`,
  Part 2 is `130 total / 84 exact / 0 helper / 40 candidate / 6 unmapped`,
  and `2:/conf/swecommon-json` is `8 exact / 0 candidate / 0 unmapped`.
- Initial full Docker Maven failed before tests on a transient Maven Central
  `tagsoup` transfer reset; retry passed
  `770 tests / 0 failures / 0 errors / 3 skipped`.
- Mandatory local OSH TeamEngine E2E reached the unmodified local OSH IUT and
  exited honestly non-green at `256 total / 27 passed / 20 failed /
  209 skipped`. All eight Sprint 66 methods SKIP before SWE Common JSON
  resource endpoint access because local OSH lacks
  `http://www.opengis.net/spec/SWE/3.0/conf/json-encoding-rules`.
- No-mutation oracle recognized 144 local-OSH IUT request logs. Method counts
  are `GET=144`, zero POST/PUT/PATCH/DELETE.
- TeamEngine 6 runtime immutability verification passed for smoke image
  `sha256:5e0b95d12d7639fe56d22e57aab875a45fc06227eed823f37b50ac7e5efa4b00`.
- Initial Raze review found one low stale Sprint 29 suite-wiring comment gap;
  the gapfix updated `testng.xml` and `VerifyTestNGSuiteDependency`, formatter
  passed, and focused retry passed `76 tests / 0 failures / 0 errors /
  0 skipped`.
- Final Raze recheck returned `APPROVE 0.96`, closed
  `RAZE-ETS66-DOC-001`, and reports `required_fixes: []`.
- Implementation commit `6e98ac9` is pushed to Botts `main`.

## Out of Scope
- IUT mutation or seed-resource creation.
- SWE Common Text or Binary exact closure.
- External validator dependency changes.
