# Sprint 56 Superseded Candidate Verification

Date: 2026-07-30
Story: S-ETS-56-01
Requirements: REQ-ETS-PART1-010, REQ-ETS-COVERAGE-001

## Candidate

- Commit: `f6e3587fabad6a18f08cac6a038a0dad719035aa`
- Source: clean detached clone at the commit above
- Image:
  `sha256:f498e3d0a8fda820b1d6dec8719e1527defd36b43215c2b403085ddd710e88a2`
- Deployed jar manifest: `Build-Revision: f6e3587fab`
- TeamEngine base:
  `sha256:9d53016965c4cca0f4d8baec136ad11258dccc552e9a8c969a1a6f889336d90c`
- Released OGC 23-001 source:
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`
- Inherited Features Part 4 source:
  `ea42aa1de6d8cbb53c526f41e1f66c1887fe71d4`

This candidate is preserved as audit evidence only. Fresh Raze returned
`GAPS_FOUND 0.98` for incomplete queued-response semantics and a custom alias
canonical-cleanup hole. Replacement implementation and gates supersede it.

## Verification

- Focused CRD Maven: BUILD SUCCESS, `28/0/0/0`.
- Full Docker Maven: BUILD SUCCESS, `630/0/0/3`. The three skips are
  historical harness fixtures and are not reported as passes.
- Released ATS inventory: PASS against the pinned OGC 23-001 checkout.
- GeoJSON and SensorML Property schema parity: PASS with zero graph or
  semantic mismatches.
- Runtime verification: PASS for pinned provenance, deployed SWE Common
  execution, dependency collisions, TeamEngine base-file immutability,
  runtime invariants, and confidential context/history hygiene.
- Core sabotage: intentional `244/2/10/232`; API Common setup/tests,
  SystemFeatures, and all twelve Create/Replace/Delete methods dependency-SKIP.
- The dedicated causal unit gate independently sabotages API Common and proves
  all twelve methods skip before CRD IUT access.
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

Part 1 API Common reports four PASS and one SKIP on both OSH targets because no
advertised collection provides positive datetime evidence. Since Sprint 56
restored causal inheritance, that skipped prerequisite makes all twelve
Create/Replace/Delete procedures TestNG dependency-SKIP before their own
declaration checks or writes. Direct `/conformance` inspection also confirms
OSH omits both
`http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/api-common`
and
`http://www.opengis.net/spec/ogcapi-4/1.0/conf/create-replace-delete`;
the advertised `ogcapi-features-4` near-match is insufficient.

This is correct fail-closed behavior, not positive mutation conformance.
Controlled HTTP verifies the implementation but cannot substitute for the
required real-IUT mutation run. All twelve mappings remain candidate, and the
story remains IN PROGRESS until a dedicated unmodified IUT completes API
Common and declares the exact inherited classes.

## Evidence

`exact-f6e3587-artifact-manifest.sha256` covers the 23 exact artifacts in this
directory; all entries pass `sha256sum -c`.
