# Sprint 56 Exact Cross-Origin Status Candidate Verification

Date: 2026-07-30
Story: S-ETS-56-01
Requirements: REQ-ETS-PART1-010, REQ-ETS-COVERAGE-001

## Candidate

- Commit: `a2ce5478e25542a766025a2a5fde246fc2d5f8d6`
- Source: clean detached clone at that commit.
- Image: `sha256:3e805b4227eda61d5b92bf01ecf83576ad0eca5ed9490eddc73f92c05e6ba9bb`.
- Deployed jar manifest: `Build-Revision: a2ce5478e2`.
- TeamEngine base remained byte-for-byte immutable.
- Released OGC 23-001 source: `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`.

## Remediation

A requirement-linked test records the prior false failure at `1/1/0/0`.
Queued HTTP 202 Location parsing now occurs before same-origin occurrence
classification. Only a same-origin direct collection item becomes an occurrence;
every other valid URI is status-only and receives no GET or DELETE.
Synchronous HTTP 201 Location handling remains strict same-origin.

## Verification

- Focused CRD Maven: `53/0/0/0`; direct HTTP: `36/0/0/0`.
- Full clean-cache Docker Maven: `655/0/0/3`; three historical harness fixtures SKIP.
- Released ATS inventory and both schema-parity graphs pass with zero mismatches.
- Runtime validator, dependency-collision, and TeamEngine immutable-base gates pass.
- Core sabotage is intentional `244/2/10/232`; all twelve CRD methods SKIP.
- Credential gates report zero unmasked hits, 32 masked events, and 32 intact transmissions.
- Artifact hygiene reports 365 IUT GETs, zero writes, and zero credential leaks.
- The detached checkout is clean and contains no generated Python bytecode.
- Fresh Raze returns `APPROVE_WITH_CONCERNS 0.99`, closes all prior findings,
  and identifies no candidate-scoped required fix.

## Local OSH E2E

- Populated: `244/54/35/155`.
- Clean primary: `244/40/7/197`.
- Provisioning and cleanup: PASS.
- Primary state unchanged: true.
- OSH source: clean, zero commits ahead of reviewed upstream.

All twelve CRD procedures dependency-SKIP because unmodified local OSH lacks
positive API Common datetime evidence and exact inherited declarations. This is
honest fail-closed E2E, not positive mutation conformance. The mappings remain
candidate and Sprint 56 remains IN PROGRESS pending a suitable external IUT.

## Evidence

`SHA256SUMS` recursively covers every file below this root except itself.
The raw requirement-linked failing run is `test-first-cross-origin-red.log`.
The final adversarial report is `raze-review.yaml`.
Nested local-OSH workflow checksums remain independently verifiable.
