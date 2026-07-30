# Sprint 56 Superseded Exact Candidate Verification

Date: 2026-07-30
Story: S-ETS-56-01
Requirements: REQ-ETS-PART1-010, REQ-ETS-COVERAGE-001

## Candidate

- Commit: `1a6c5ec30f76e120a0e2cd676f472699141213ca`
- Disposition: superseded by Raze `GAPS_FOUND 0.98`; retained as immutable
  historical evidence, not a final closure candidate.
- Source: clean detached clone at the commit above
- Image:
  `sha256:6939ef2ea40ff42328d4ff972b691dd4ba1c6a59855fe913d175b09f9555c1da`
- Deployed jar manifest: `Build-Revision: 1a6c5ec30f`
- TeamEngine base:
  `sha256:9d53016965c4cca0f4d8baec136ad11258dccc552e9a8c969a1a6f889336d90c`
- Released OGC 23-001 source:
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`
- Inherited Features Part 4 source:
  `ea42aa1de6d8cbb53c526f41e1f66c1887fe71d4`

## Remediation

The earlier `0023d5b` candidate received Raze `GAPS_FOUND 0.99`. Candidate
`1a6c5ec` added a monotonic deadline across probes, HTTP timeouts, capped
sleeps, late-success rejection, interruption checks, and compound
postconditions. Cleanup failure overrides an inconclusive SKIP. Queued
URI-list cleanup is registered before POST and polls late materialization.

Seven requirement-linked regressions cover those findings. The initial red run
was `25/6/0/0`; the interruption regression was tightened during
implementation rather than counted as an initial failure.

## Verification

- Focused CRD Maven: BUILD SUCCESS, `42/0/0/0`.
- Direct controlled HTTP: `25/0/0/0`.
- Full Docker Maven: BUILD SUCCESS, `644/0/0/3`. The three historical harness
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

Raze returned `GAPS_FOUND 0.98` in
`.harness/evaluations/sprint-ets-56-raze-1a6c5ec-2026-07-30.yaml`.

- HIGH: API Common pagination and candidate requests could begin after the
  queued-operation deadline, and HTTP timeout conversion rounded upward.
- HIGH: queued custom replace/delete setup did not await or cleanup-register
  the custom occurrence before the later write.
- MEDIUM: a distinct occurrence `Location` on HTTP 202 was discarded instead
  of being verified and cleaned, while status-resource Locations needed an
  explicit fail-closed distinction.

These findings supersede `1a6c5ec` despite its passing technical gates. The
next candidate adds requirement-linked controlled regressions and remediation.
Positive real-IUT mutation evidence remains an independent external blocker.

## Evidence

`artifact-manifest.sha256` covers every immutable exact-candidate artifact and
this reconciliation summary in this directory. Run `sha256sum -c
artifact-manifest.sha256` from this directory to verify the bundle.
