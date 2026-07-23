# Sprint ETS-42 Duplicate-Family Gapfix Verification

Date: 2026-07-22

## Scope

Closure of `RAZE-S42-FINAL-001` through `-003` after final Raze found that
`teamengine-resources-6.0.0.jar` duplicated the immutable base image's
`teamengine-resources-6.0.0-RC2.jar` functional paths.

## Changes

- Removed the GA TeamEngine resources jar and the separate runtime dependency
  payload from the Dockerfile. The final image adds only the shaded ETS jar and
  uniquely named CTL tree.
- Added a test-first structural guard rejecting the former selected-payload path
  and GA resources coordinate. The pre-fix test failed `10/1/0/0` at the new
  assertion; the corrected test passed `10/0/0/0`.
- Removed runtime-verifier exceptions for the GA resources jar.
- Added coordinate-aware TeamEngine Maven family multiset comparison between
  base and final images.
- Added direct `FINAL_IMAGE_ID` output to both smoke and runtime-verifier gates.
- Removed the empty `.moduledata/log.txt` gate-run artifact; OSH durable state
  remains outside the repository.

## Results

- Formatter: PASS, Dockerized `spring-javaformat:apply` BUILD SUCCESS.
- Full Docker Maven: PASS, `311 tests / 0 failures / 0 errors / 3 skipped`.
- Replacement image:
  `sha256:b52f4897c553f5d3e37caf62fa14765a774b17f943243be3d99c5d89eec5dcb3`.
- Image TeamEngine resources inventory: inherited
  `teamengine-resources-6.0.0-RC2.jar` only; no GA resources jar and no added
  TeamEngine coordinate family.
- Runtime verifier: PASS with direct base/final image IDs, deployed valid/invalid
  SWE Common adapter execution, coordinate-aware dependency parity, immutable
  TeamEngine base, runtime invariants, and confidential history/context hygiene.
- Primary local OSH E2E from a fresh clone synchronized to the final worktree:
  PASS `211 total / 69 passed / 0 failed / 142 skipped`; 135 recognized IUT
  requests (`GET=133`, `OPTIONS=2`), zero writes, and zero startup errors.

## Artifacts

- `ops/test-results/sprint-ets-42-duplicate-family-test-first-2026-07-22.txt`
- `ops/test-results/sprint-ets-42-duplicate-gapfix-image-inventory-2026-07-22.txt`
- `ops/test-results/sprint-ets-42-duplicate-gapfix-runtime-verifier-final-2026-07-22.txt`
- `ops/test-results/sprint-ets-42-duplicate-gapfix-local-osh-e2e-final-2026-07-22.txt`
- `ops/test-results/sprint-ets-42-duplicate-gapfix-local-osh-final-2026-07-22.xml`
- `ops/test-results/sprint-ets-42-duplicate-gapfix-local-osh-final-container-2026-07-22.log`
- `ops/test-results/sprint-ets-42-duplicate-gapfix-local-osh-final-no-mutation-2026-07-22.txt`
