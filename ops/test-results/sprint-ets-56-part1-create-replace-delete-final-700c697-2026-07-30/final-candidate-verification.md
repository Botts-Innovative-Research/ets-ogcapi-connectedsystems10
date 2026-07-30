# Sprint 56 Exact Identity-Safe Candidate Verification

Date: 2026-07-30
Story: S-ETS-56-01
Requirements: REQ-ETS-PART1-010, REQ-ETS-COVERAGE-001

## Candidate

- Commit: `700c697e59eb2a03d3a41a37ec9a745cd1aa3583`
- Source: clean detached clone at the commit above
- Image:
  `sha256:5d9c0b9d1d12cf68aaf83e2aec512ecc5abed2fdd9df9568078776443af658f7`
- Deployed jar manifest: `Build-Revision: 700c697e59`
- TeamEngine base:
  `sha256:9d53016965c4cca0f4d8baec136ad11258dccc552e9a8c969a1a6f889336d90c`
- Released OGC 23-001 source:
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`

## Remediation

Raze returned `GAPS_FOUND 0.97` on superseded candidate `8aa92d4`. This
candidate retains submitted content on every queued occurrence cleanup target
and authorizes DELETE only after a bounded GET matches that content. A
mismatched direct collection-item Location remains untouched. Compound queued
postconditions are evaluated jointly under one monotonic deadline and require
two consecutive complete observations.

Four requirement-linked regressions cover mismatched direct Locations and
transient replacement, occurrence-deletion, and cascade states. Behavioral red
was `35/2/0/0`; the corrected direct suite is `35/0/0/0`.

## Verification

- Focused CRD Maven: BUILD SUCCESS, `52/0/0/0`.
- Full clean-cache Docker Maven: BUILD SUCCESS, `654/0/0/3`; the three
  historical harness fixture skips are not reported as passes.
- Released ATS inventory: PASS against the pinned OGC 23-001 checkout.
- GeoJSON parity: 8 entry schemas, 20 transitive schemas, zero mismatches.
- Property parity: 3 entry schemas, 53 transitive schemas, zero mismatches.
- Runtime verification: PASS for pinned provenance, deployed SWE Common
  execution, dependency collisions, TeamEngine base-file immutability,
  runtime invariants, and confidential context/history hygiene.
- Core sabotage: intentional `244/2/10/232`; all twelve substantive Part 1
  Create/Replace/Delete methods dependency-SKIP.
- Credential integration and wire E2E: PASS with zero unmasked artifact hits,
  32 masked events, and 32 intact synthetic wire transmissions.
- Artifact hygiene: PASS across six files with 365 IUT GETs, zero IUT writes,
  and zero credential leaks.
- The detached checkout remained clean after every gate and contains no
  generated Python bytecode.

## Local OSH E2E

The candidate ran through Dockerized TeamEngine against an isolated,
supported-interface-populated OSH process and the unchanged primary OSH. No
OSH or TeamEngine source code or binary was modified.

- Populated: `244 total / 54 passed / 35 failed / 155 skipped`.
- Clean primary: `244 total / 40 passed / 7 failed / 197 skipped`.
- Isolated provisioning: PASS.
- Isolated cleanup: PASS.
- Primary state unchanged: true; the state diff is empty.
- OSH source: clean at `4c87a65c9a967d52af9df476e65d7862c7673a15`,
  zero commits ahead of reviewed upstream.
- OSH install: read-only; deployed ConSys bundle build is `4c87a65`.

Part 1 API Common still reports one datetime SKIP. Causal inheritance therefore
dependency-SKIPs all twelve mutation procedures before declaration checks or
writes. This is correct fail-closed behavior, not positive mutation
conformance. Controlled HTTP verifies the implementation but cannot substitute
for a real IUT satisfying the inherited prerequisites. All twelve mappings
remain candidate, and the story remains IN PROGRESS.

## Evidence

`SHA256SUMS` covers every file below this evidence root except itself. Nested
workflow manifests remain independently verifiable.
