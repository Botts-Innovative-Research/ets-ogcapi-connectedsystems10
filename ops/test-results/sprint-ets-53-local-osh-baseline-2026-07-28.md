# Sprint 53 Local OSH Property Definitions Baseline

Date: 2026-07-28

## Target

- IUT: `http://field-hub-osh-1:8081/sensorhub/api`
- Docker network: `field-hub_default`
- OSH application mount: `/opt/osh`, read-only
- No OSH or TeamEngine source or binary changes

## Read-Only Probes

`GET /properties` with `Accept: application/sml+json` returned HTTP 200,
`Content-Type: application/json`, and:

```json
{
  "items": []
}
```

`GET /collections` with `Accept: application/json` returned HTTP 200 and four
collections. None advertises `itemType=sosa:Property`.

## Expected Released Procedure Outcomes

- `/conf/property/resources-endpoint`: SKIP because actual media is unsupported.
- `/conf/property/canonical-endpoint`: SKIP because actual media is unsupported.
- `/conf/property/collections`: FAIL because no required Property collection is
  advertised.
- `/conf/property/canonical-url`: SKIP because no Property collection/item
  evidence exists.

These outcomes are IUT evidence, not implementation defects. Controlled
read-only HTTP fixtures provide positive-path and fail-closed ETS verification.
