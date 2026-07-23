# Sprint ETS-43 Scope Reconciliation Verification - 2026-07-23

## Candidate

- Exact image:
  `sha256:7071cc7694aee2d0b3ca2f44dd2fcad79e9f1eff7b6b2c4de52299adb4704b29`
- TeamEngine base:
  `ogccite/teamengine-dev@sha256:981b71566d56434576843798ae8072db15be8478eb7dc724b051c2228460f43c`
- OSH checkout: `4c87a65c9a967d52af9df476e65d7862c7673a15`,
  clean, `0` ahead and `3` behind `origin/master`
- OSH runtime: deployed ConSys manifest reports `4c87a65`; `/opt/osh` mount is
  read-only

The OSH manifest match is provenance metadata, not independent binary
byte-equivalence proof. The audit found no current project-authored source drift
or binary-patch evidence.

## Test-First Evidence

- Dormant hosted-workflow guard before deletion: expected failure,
  `12 tests / 1 failure / 0 errors / 0 skipped`.
- TeamEngine metadata-inventory assertions before implementation: expected
  failure, `12 tests / 1 failure / 0 errors / 0 skipped`.
- Corrected focused packaging guard:
  `12 tests / 0 failures / 0 errors / 0 skipped`.

## Gates

- Java formatting: BUILD SUCCESS.
- Full Docker Maven:
  `313 tests / 0 failures / 0 errors / 3 skipped`, BUILD SUCCESS.
- Exact-image runtime verifier: PASS. It compared TeamEngine path inventories
  including type, mode, uid, gid, symlink target, and file content while
  excluding only the ETS jar and ETS CTL tree.
- Local OSH TeamEngine E2E:
  `211 total / 69 passed / 0 failed / 142 skipped`.
- Real IUT protocol evidence: `135` recognized request-log entries; zero
  IUT-bound POST/PUT/DELETE/PATCH.
- TeamEngine startup log: zero ERROR/SEVERE.
- E2E report:
  `/tmp/ets-scope-final-results.niPIHh/s-ets-01-03-teamengine-smoke-2026-07-23.xml`
- E2E log:
  `/tmp/ets-scope-final-results.niPIHh/s-ets-01-03-teamengine-container-2026-07-23.log`

## Adversarial Review

Initial Raze review returned `GAPS_FOUND`, confidence `0.99`, after 274 seconds;
token metadata was unavailable. Remediation:

- Converted stale Sprint 40 operational statements to explicit historical
  records and restored S-ETS-43-01 as the current handoff.
- Removed remaining active hosted-CI instructions from Sprint 1 stories, Java
  comments, ADRs, the product brief, Dockerfile, and traceability.
- Qualified OSH provenance claims to state exactly what current evidence proves.
- Extended TeamEngine base verification beyond file hashes to path type, mode,
  ownership, symlink targets, directory inventory, and file content.
- Added current focused, full Maven, exact-image runtime, and E2E evidence.

Final focused Raze recheck independently verified the focused/full Maven
reports, exact image, E2E XML, no-mutation oracle, startup scan, OSH provenance,
and clean diff. It found no remaining issues and returned `APPROVE`, confidence
`0.99`, after 142 seconds. Token metadata was unavailable.
