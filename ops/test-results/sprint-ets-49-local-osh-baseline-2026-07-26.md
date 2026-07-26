# Sprint 49 Local OSH Deployment Baseline

Date: 2026-07-26

Target: unmodified `field-hub-osh-1` on `field-hub_default`.

| Probe | Result |
|---|---|
| `GET /collections` | HTTP 200, `application/json`; four collections, none with `featureType=sosa:Deployment` |
| `GET /deployments` | HTTP 200, actual `application/json` despite requesting released Deployment media |
| `GET /systems` | HTTP 200, `application/json`; first local ID `040g` |
| `GET /systems/040g/deployments` | HTTP 400, `application/json`; invalid resource name |
| OSH source provenance | `/home/nh/docker/osh-core` clean at `4c87a65c9a967d52af9df476e65d7862c7673a15` |

These are expected IUT conformance outcomes, not ETS blockers to hide. Sprint 49
must execute all five released methods, preserve resulting FAIL/SKIP verdicts,
and prove positive procedure behavior separately with a controlled read-only
HTTP fixture. No OSH or TeamEngine change is permitted.
