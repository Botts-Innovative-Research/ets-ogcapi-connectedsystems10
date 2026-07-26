# Sprint ETS-44 Populated Local OSH Verification

Date: 2026-07-25

## Verdict

The reproducible populated-IUT workflow completed its infrastructure contract.
It did not pass populated conformance.

- Provisioning: PASS (`POST=7`, `GET=10`).
- Populated TestNG: FAIL (`211/91/28/92`).
- Populated gate: `COMPLETE_WITH_CONFORMANCE_FAILURE`.
- Cleanup: PASS; owned containers and ephemeral state absent.
- Primary comparison: PASS; no normalized identity, configuration, mount,
  network, runtime, or state differences.
- Clean-primary TestNG: PASS (`211/69/0/142`), `GET=133`, `OPTIONS=2`, writes
  zero.
- Overall workflow: FAIL, as required by the 28 TestNG failures.

All populated failures are strict unmodified-OSH representation findings:
DataStream collection items omit `live`; ControlStream collection items omit
`issueTime`, `executionTime`, `live`, and `async`.

## Gates

- Test-first: expected FAIL, `6/6/0/0`.
- Behavioral safety suite: PASS, `12/0/0/0`.
- Focused Maven: PASS, `9/0/0/0`.
- Full Docker Maven: PASS, `322/0/0/3`.
- Exact TeamEngine runtime verifier: PASS on image
  `sha256:cc8c9d711e57ed50d2ed08cdef01cb1236052e775ff27ad016185672e9de8169`.
- Populated/clean E2E source manifest: PASS from the fresh synchronized clone.
- E2E artifact manifest: PASS for all 31 manifested artifacts.

## Provenance

- OSH source: `opensensorhub/osh-core`
  `4c87a65c9a967d52af9df476e65d7862c7673a15`, clean, zero commits ahead of
  resolved upstream, and an ancestor of upstream.
- Installed ConSys build metadata: `4c87a65`.
- OSH runtime image:
  `maven:3.9-eclipse-temurin-17@sha256:1ed5d1f54416b706707b4f3238f63a20bb06aab27c6d240090a2bb9ad895ed45`.
- Run ID: `20260725T155343Z-088ab65886f1`.

## Artifacts

Complete evidence, including both TestNG XML reports, both TeamEngine logs,
failure extraction, provenance, ownership, primary fingerprints, provisioning
evidence, summary, and SHA-256 manifests:

`ops/test-results/sprint-ets-44-final-e2e-2026-07-25/`

Raw gates:

- `ops/test-results/sprint-ets-44-test-first-raw-2026-07-25.txt`
- `ops/test-results/sprint-ets-44-behavioral-raw-2026-07-25.txt`
- `ops/test-results/sprint-ets-44-focused-raw-2026-07-25.txt`
- `ops/test-results/sprint-ets-44-full-maven-raw-2026-07-25.txt`
- `ops/test-results/sprint-ets-44-teamengine-runtime-raw-2026-07-25.txt`

Initial Raze returned `GAPS_FOUND` at confidence `0.99`. The ten required
findings were addressed before this replacement E2E run. Focused recheck
returned `APPROVE` at confidence `0.99` after 372 seconds, with all ten findings
closed and no new findings. Token metadata was unavailable.
