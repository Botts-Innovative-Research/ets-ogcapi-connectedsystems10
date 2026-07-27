# Sprint 50 Local OSH Procedure Baseline

Date: 2026-07-26

## Direct Read-Only Probes

The unmodified local OSH at
`http://field-hub-osh-1:8081/sensorhub/api` returned:

- `GET /procedures?limit=100`: HTTP 200 and `Content-Type:
  application/json` for `Accept: application/json`,
  `application/geo+json`, and `application/sml+json`.
- The response contained one item, local ID `040g`, with `geometry=null` and
  Procedure type `http://www.w3.org/ns/sosa/Procedure`.
- `GET /collections`: HTTP 200 with no collection advertising
  `featureType=sosa:Procedure`.
- `GET /procedures/040g`: HTTP 200 with canonical, SensorML alternate, and HTML
  alternate links. The generic canonical item response reported a nonstandard
  `Content-Type: auto`.

No write request was issued.

## Existing TeamEngine Behavior

Sprint 49's primary TeamEngine report shows all four historical
`ProceduresTests` methods as SKIP because the `procedures` group depends on the
unsatisfied `systemfeatures` group. The Procedure HTTP assertions therefore do
not execute.

Sprint 50 must replace that unrelated dependency with direct Part 1 API Common
inheritance and execute all five released Procedure methods.

## Expected Replacement Outcomes

Against the current unmodified local OSH:

- `/location`, `/resources-endpoint`, and `/canonical-endpoint` should SKIP
  before parsing because `/procedures` returns unsupported
  `application/json`.
- `/canonical-url` and `/collections` should FAIL because no
  `sosa:Procedure` collection is advertised.

These are genuine IUT outcomes, not reasons to modify OSH or weaken the ETS.

## Provenance

- `/home/nh/docker/osh-core` is clean at
  `4c87a65c9a967d52af9df476e65d7862c7673a15`.
- The checkout is zero commits ahead of upstream.
- The deployed `/opt/osh` mount is read-only.
- No OSH or TeamEngine source or binary was changed by these probes.
