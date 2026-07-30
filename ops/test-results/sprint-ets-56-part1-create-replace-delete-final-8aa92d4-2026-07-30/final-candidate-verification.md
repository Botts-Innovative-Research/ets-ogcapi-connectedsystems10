# Sprint 56 Exact Remediation Candidate Verification

Date: 2026-07-30
Story: S-ETS-56-01
Requirements: REQ-ETS-PART1-010, REQ-ETS-COVERAGE-001

## Candidate

- Commit: `8aa92d4da33aeb3b1c545378c0a68cb84a565ccb`
- Source: clean detached clone at the commit above
- Image:
  `sha256:3865aca8a80b5a23fd94531705e0228db5e71c7b2ef65cbc596f83f9c0145d7a`
- Deployed jar manifest: `Build-Revision: 8aa92d4da3`
- TeamEngine base:
  `sha256:9d53016965c4cca0f4d8baec136ad11258dccc552e9a8c969a1a6f889336d90c`
- Released OGC 23-001 source:
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`
- Inherited Features Part 4 source:
  `ea42aa1de6d8cbb53c526f41e1f66c1887fe71d4`

## Remediation

Raze returned `GAPS_FOUND 0.98` on superseded candidate `1a6c5ec`. This
candidate refuses expired or sub-millisecond budgets at every first-page,
pagination-page, and candidate-resource requester boundary without rounding
timeouts upward. Queued custom replace/delete setup awaits and
cleanup-registers the created occurrence. Queued `text/uri-list` treats only a
direct target-collection item Location as an occurrence; status Locations are
not dereferenced or deleted.

Six requirement-linked regressions cover those paths. Test-first compilation
failed at the injected-clock constructor, the behavioral red was `30/4/0/0`,
and an initial green attempt exposed an unbounded no-request-budget loop and
was aborted before the final correction.

## Verification

- Focused CRD Maven: BUILD SUCCESS, `48/0/0/0`.
- Direct controlled HTTP: `31/0/0/0`.
- Full Docker Maven: BUILD SUCCESS, `650/0/0/3`. The three historical harness
  fixture skips are not reported as passes.
- Released ATS inventory: PASS against the pinned OGC 23-001 checkout.
- GeoJSON and SensorML Property schema parity: PASS with zero mismatches.
- Runtime verification: PASS for pinned provenance, deployed SWE Common
  execution, dependency collisions, TeamEngine base-file immutability,
  runtime invariants, and confidential context/history hygiene.
- Core sabotage: intentional `244/2/10/232`; all twelve substantive Part 1
  Create/Replace/Delete methods dependency-SKIP.
- Credential integration and wire E2E: PASS with zero unmasked artifact hits,
  32 masked events, and 32 intact synthetic wire transmissions.
- Artifact hygiene: PASS across six files with 365 IUT GETs, zero IUT writes,
  and zero credential leaks.

## Local OSH E2E

The exact candidate ran through Dockerized TeamEngine against an isolated,
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

Part 1 API Common reports three substantive PASS results and one datetime SKIP
on both targets; setup separately passes. The IUT advertises Part 1
`/conf/create-replace-delete`, but causal API Common inheritance
dependency-SKIPs all twelve mutation procedures before declaration checks or
writes. OSH also omits exact Connected Systems API Common and inherited
`ogcapi-4` declarations; its `ogcapi-features-4` declaration is a near-match.

This is correct fail-closed behavior, not positive mutation conformance.
Controlled HTTP verifies the implementation but cannot substitute for the
required real-IUT mutation run. All twelve mappings remain candidate, and the
story remains IN PROGRESS until a dedicated unmodified IUT completes API
Common and advertises the exact inherited class.

## Adversarial Review

A fresh read-only Raze review of this exact candidate is in progress. This
candidate must not be reported complete unless that review leaves no unresolved
required fixes.

## Evidence

`artifact-manifest.sha256` will cover every immutable exact-candidate artifact
and this reconciliation summary after the Raze report is archived.
