# CP-020 - Part 2 Datastreams and Observations Released ATS Closure

**Status**: Implemented in Sprint 60
**Date**: 2026-07-31
**Capability**: `ets-ogcapi-connectedsystems`
**Requirement**: `REQ-ETS-PART2-002`
**Story**: `S-ETS-60-01`

## Problem

Sprint 21 implemented a useful read-only Datastreams and Observations subset,
but the released ATS coverage audit still reports the OGC 23-002
`/conf/datastream` class as `0 exact / 9 candidate / 5 unmapped`. The class
also retains two historical non-ATS tracer procedures and TestNG wiring that
allows scoped endpoint evidence to run without the now-exact Part 2 API Common
prerequisite.

## Change

Close all fourteen released OGC 23-002 Part 2 Datastreams and Observations
Annex A procedures exactly:

- `/conf/datastream/sf-ref-from-datastream`
- `/conf/datastream/foi-ref-from-datastream`
- `/conf/datastream/canonical-url`
- `/conf/datastream/resources-endpoint`
- `/conf/datastream/canonical-endpoint`
- `/conf/datastream/ref-from-system`
- `/conf/datastream/ref-from-deployment`
- `/conf/datastream/collections`
- `/conf/datastream/schema-op`
- `/conf/datastream/obs-canonical-url`
- `/conf/datastream/obs-resources-endpoint`
- `/conf/datastream/obs-canonical-endpoint`
- `/conf/datastream/obs-ref-from-datastream`
- `/conf/datastream/obs-collections`

The deployed TestNG class SHALL expose exactly these fourteen procedures. Each
procedure SHALL gate exact `/conf/datastream` declaration at runtime and SHALL
stay read-only. The `part2datastream` group SHALL inherit through
`part2apicommon` now that Sprint 59 closed the Part 2 API Common released ATS.

## Implementation Result

Sprint 60 replaced the historical Datastream class with exactly fourteen
released procedures, removed non-ATS tracer methods, promoted all fourteen
mappings to reviewed exact, and regenerated the released ATS coverage report.
`2:/conf/datastream` is now `14 exact / 0 candidate / 0 unmapped`.

The initial adversarial review found that the first Sprint 60 implementation
still contained bounded approximations. The gap-fix now follows every
applicable canonical resource or exact `itemType` collection, requires
advertised `rel=canonical` dereference and content equality after canonical
links are removed, validates DataStream and Observation endpoint/collection
JSON schemas, validates FeatureOfInterest GeoJSON responses as generic
GeoJSON FeatureCollections rather than SamplingFeature schemas, checks every
advertised Observation schema format for every DataStream, and gates
conditional Sampling Feature, FeatureOfInterest, System, and Deployment
procedures before nested subresource access. The final Raze follow-up gapfix
also prevents mixed Sampling Feature endpoint evidence from false-PASSing when
any DataStream endpoint uses unsupported media and validates FOI
`application/json` pages as GeoJSON FeatureCollections.

## Verification Plan

- Focused test-first red: `85 tests / 3 failures / 2 errors / 0 skipped`.
- Corrected focused Datastream and TestNG dependency verification: `96/0/0/0`.
- Released ATS coverage update and audit: `23/0/0/0`; `2:/conf/datastream`
  is `14 exact / 0 candidate / 0 unmapped`.
- Formatter: BUILD SUCCESS.
- Full Docker Maven: `750 tests / 0 failures / 0 errors / 3 skipped`.
- Mandatory local OSH TeamEngine smoke: `247 total / 38 passed / 21 failed /
  188 skipped`, non-green because the unmodified local OSH does not declare the
  Part 2 `/conf/api-common` prerequisite and still has existing non-Datastream
  interoperability/conformance failures.
- No-mutation oracle: `recognized_iut_request_logs=189`; request-line method
  count is `GET=194`, zero POST/PUT/PATCH/DELETE.
- Raze adversarial review: initial `GAPS_FOUND 0.94`; FOI/stale-evidence
  recheck `GAPS_FOUND`; conditional-gating recheck `GAPS_FOUND`; mixed
  Sampling Feature / FOI `application/json` recheck `GAPS_FOUND`; final recheck
  returned `PASS` with high confidence and no required fixes.

## Non-Goals

- IUT mutation.
- Part 2 Control Streams, JSON, SWE Common, CRD, or Update closure.
- Importing or modifying OSH or TeamEngine source/binaries.
- Claiming Observation result validation against Datastream schemas beyond the
  released Datastreams and Observations Annex A procedures.
