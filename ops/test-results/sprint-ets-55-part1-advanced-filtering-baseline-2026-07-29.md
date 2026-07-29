# Sprint ETS-55 Part 1 Advanced Filtering Baseline

Date: 2026-07-29
Story: S-ETS-55-01
Requirements: REQ-ETS-PART1-009, REQ-ETS-COVERAGE-001

## Released source

- Repository: `opengeospatial/ogcapi-connected-systems`
- Tag: `v1.0.0`
- Commit: `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`
- Checkout: clean
- Part 1 Advanced Filtering inventory: 25 procedures
- Composition: 22 requirement targets and three recommendation targets

The 25 identifiers are recorded by ADR-013 in
`src/main/resources/org/opengis/cite/ogcapiconnectedsystems10/ats/released-ats-inventory.json`.

## Current implementation

- Historical deployed methods: 6
- Exact mappings: 0
- Candidate mappings: 4
- Unmapped procedures: 21
- Historical dependency: System
- Released direct dependency: API Common

The historical class couples `/conformance` and one System seed in
`@BeforeClass`, covers only System `id`, `q`, and geometry smoke behavior, and
adds declaration/dependency tracers that are not released procedures.

## Released editorial resolutions

The target requirement controls three obvious Annex A prose defects:

- deployment parent filtering uses a parent Deployment relation;
- deployment-by-system repeats the `system` query for UID evidence; and
- indirect Property checks use `/samplingFeatures` for Sampling Feature sets.

Recursive System property checks follow normative subsystem semantics and may
use advertised subsystem/component links rather than requiring a
non-standard literal `/components` path.

## Primary IUT

Unmodified local OSH:

- source commit: `4c87a65c9a967d52af9df476e65d7862c7673a15`
- source worktree: clean
- `/opt/osh` mount: read-only
- Part 1 `/conf/advanced-filtering`: not declared
- representative `/systems?limit=1`: HTTP 200 `application/json`

Primary TeamEngine E2E must discover all 25 deployed methods exactly once and
record declaration-boundary SKIPs before filter-specific IUT access. These
SKIPs are not positive conformance evidence. Controlled read-only HTTP tests
must execute every positive procedure.
