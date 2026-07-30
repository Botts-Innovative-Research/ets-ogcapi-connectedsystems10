# Sprint 56 Superseded Candidate Verification

Date: 2026-07-30
Story: S-ETS-56-01
Requirements: REQ-ETS-PART1-010, REQ-ETS-COVERAGE-001

## Candidate

- Commit: `0023d5b492dff8b5dbeff6c201c257f970b8947a`
- Source: clean detached clone at the commit above
- Image:
  `sha256:4764227eda6ab91d5895df7bce74d440b0c95842127a0514debd67b857ed0744`
- Deployed jar manifest: `Build-Revision: 0023d5b492`
- TeamEngine base:
  `sha256:9d53016965c4cca0f4d8baec136ad11258dccc552e9a8c969a1a6f889336d90c`
- Released OGC 23-001 source:
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`
- Inherited Features Part 4 source:
  `ea42aa1de6d8cbb53c526f41e1f66c1887fe71d4`

## Verification

- Focused CRD Maven: BUILD SUCCESS, `35/0/0/0`.
- Queued/alias controlled HTTP: `18/0/0/0`.
- Full Docker Maven: BUILD SUCCESS, `637/0/0/3`. The three skips are
  historical harness fixtures and are not reported as passes.
- Released ATS inventory: PASS against the pinned OGC 23-001 checkout.
- GeoJSON and SensorML Property schema parity: PASS with zero graph or
  semantic mismatches.
- Runtime verification: PASS for pinned provenance, deployed SWE Common
  execution, dependency collisions, TeamEngine base-file immutability,
  runtime invariants, and confidential context/history hygiene.
- Core sabotage: intentional `244/2/10/232`; API Common setup/tests,
  SystemFeatures, and all twelve Create/Replace/Delete methods dependency-SKIP.
- Credential integration: PASS with zero literal credential hits.
- Credential wire E2E: PASS with zero unmasked artifact hits, 32 masked events,
  and 32 intact synthetic wire transmissions.
- Artifact hygiene: PASS across populated and clean-primary reports/logs with
  365 IUT GETs, zero IUT writes, and zero credential leaks.

## Local OSH E2E

The exact candidate ran through Dockerized TeamEngine against an isolated,
supported-interface-populated OSH process and then the unchanged primary OSH.
No OSH or TeamEngine source code or binary was modified.

- Populated: `244 total / 54 passed / 35 failed / 155 skipped`.
- Clean primary: `244 total / 40 passed / 7 failed / 197 skipped`.
- Isolated provisioning: PASS.
- Isolated cleanup: PASS.
- Primary state unchanged: true; the state diff is empty.
- OSH source: clean at `4c87a65c9a967d52af9df476e65d7862c7673a15`,
  three commits behind and zero ahead of reviewed upstream.
- OSH install: read-only; deployed ConSys bundle build is `4c87a65`.

Part 1 API Common reports three procedure PASS results and one procedure SKIP
on both targets; its setup evidence is not a conformance procedure. The IUT
advertises Part 1 `/conf/create-replace-delete`, but the skipped API Common
datetime prerequisite causally dependency-SKIPs all twelve
Create/Replace/Delete procedures before their declaration checks or writes.
Direct `/conformance` inspection also confirms OSH omits both
`http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/api-common`
and
`http://www.opengis.net/spec/ogcapi-4/1.0/conf/create-replace-delete`;
the advertised `ogcapi-features-4` near-match is insufficient.

This is correct fail-closed behavior, not positive mutation conformance.
Controlled HTTP verifies the implementation but cannot substitute for the
required real-IUT mutation run. All twelve mappings remain candidate, and the
story remains IN PROGRESS until a dedicated unmodified IUT completes API
Common and advertises the exact inherited class.

## Adversarial Review

Raze returned `GAPS_FOUND` at confidence `0.99`. The candidate is superseded
despite passing its reproducible exact gates. Required remediation covers a
hard operation deadline, cleanup-failure precedence, all queued compound
postconditions, pre-registered late URI-list cleanup, and CP-016 status
semantics. The positive real-IUT mutation blocker also remains open.

## Evidence

The checksum manifest in this directory covers all immutable candidate
artifacts and this reconciliation summary.

Canonical adversarial report:
`.harness/evaluations/sprint-ets-56-raze-0023d5b-2026-07-30.yaml`.
