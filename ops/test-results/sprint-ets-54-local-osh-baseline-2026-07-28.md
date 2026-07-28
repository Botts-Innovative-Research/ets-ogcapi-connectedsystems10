# Sprint 54 Local OSH GeoJSON Baseline

Date: 2026-07-28

## Scope

Read-only planning probe for S-ETS-54-01 against the existing unmodified local
OSH container on `field-hub_default`.

## Observations

- Container `field-hub-osh-1` was already running.
- `/conformance` returned HTTP 200 and declared:
  - Connected Systems Part 1 `/conf/geojson`;
  - OGC API Features 1 `/conf/geojson`;
  - Connected Systems System, Deployment, Procedure, and Sampling Feature
    classes.
- The landing page returned HTTP 200 and advertised external Part 1 and Part 2
  OpenAPI 3.1 YAML `service-desc` links.
- `GET /systems?limit=1` with `Accept: application/geo+json` returned HTTP 200
  `application/json`.
- `GET /deployments?limit=1` with `Accept: application/geo+json` returned HTTP
  200 `application/json`.
- `GET /procedures?limit=1` with `Accept: application/geo+json` returned HTTP
  200 `application/json`.
- `GET /samplingFeatures?limit=1` with `Accept: application/geo+json` returned
  HTTP 200 `application/json`.

## Expected Exact-Procedure Outcomes

The primary IUT does not provide actual GeoJSON media for the four canonical
feature collections. Schema, mapping, feature-attribute, and relation
procedures must therefore SKIP at the actual-media boundary without parsing
generic JSON as GeoJSON. API-definition procedures remain independently
executable from `service-desc` evidence.

No OSH or TeamEngine source code, binary, configuration, or test data was
changed by this probe.
