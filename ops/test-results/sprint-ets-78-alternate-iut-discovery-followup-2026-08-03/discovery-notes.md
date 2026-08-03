# Sprint 78 Alternate IUT Discovery Follow-up

Date: 2026-08-03
Role: BMAD Analyst / Discovery
Policy: no public mutation; no Docker, Maven, or TeamEngine.

## Bottom Line

No newly discovered open-source implementation beyond local OSH and
`SomethingCreativeStudios/connected-systems-go` is ready to close the remaining
47 mutation-bound ETS candidates.

The best new direct candidate is `52North/connected-systems-pygeoapi`. It is
open source, has a public demo, and source shows some write-capable Part 1 and
Datastream/Observation routes. It is not exact-promotion ready because current
public and source evidence show missing Connected Systems conformance
declarations, no Part 2 conformance declarations, public Datastream 500s, no
ControlStream/Command/Feasibility route surface, and no PATCH/Update exposure.

## Sources

- OGC implementation list:
  https://github.com/opengeospatial/ogcapi-connected-systems/blob/master/implementations.adoc
- OGC CSAPI developer site:
  https://csapi.developer.ogc.org/
- 52North software page:
  https://52north.org/software/software-components/ogc-api-connected-systems/
- 52North source:
  https://github.com/52North/connected-systems-pygeoapi
- 52North pygeoapi feature branch:
  https://github.com/52North/pygeoapi/tree/feature/connected-systems
- DGIWG Glaux:
  https://github.com/DGIWG-P507/glaux
- DGIWG Glaux Server:
  https://github.com/DGIWG-P507/glaux-server
- OWSLib:
  https://github.com/geopython/OWSLib
- FROST-Server:
  https://github.com/FraunhoferIOSB/FROST-Server

## Candidate List

| Candidate | Type | Current Evidence | Mutation/Update Readiness |
| --- | --- | --- | --- |
| 52North `connected-systems-pygeoapi` | Direct CS API server | Main HEAD `18c1ce803fcdb2de3aac9d227ab814306d8a718f`, Apache-2.0, public demo, Elasticsearch + TimescaleDB architecture, source routes for Part 1 writes and Datastream/Observation writes. | Not ready. Archived safe GET body `52north-public-conformance.json` contains only OGC API Common Core; readiness audit reports `GET=1`, `OPTIONS=25`, `unsafeMethodsIssued=[]`, zero ready classes; public Datastream GETs return 500; no ControlStream/Command/Feasibility; PATCH commented out. |
| DGIWG `glaux-server` | Claimed direct CS API server | README says gold-standard OGC API Connected Systems authority node; GitHub contents currently only `README.md`, languages `{}`. | Not usable yet. No runnable source or public IUT evidence. |
| OWSLib Connected Systems client | Client/library | Main HEAD `9d8e5bac414dcce018bc39dfc90424cedd39758c`; source has create/update/delete helpers across many CS API resource families. | Not an IUT. Could help drive local disposable experiments later. |
| FROST-Server | Adjacent SensorThings API server | HEAD `bf7976831b22bcac6dd20a1335d96fed7f809677`; mature mutable SensorThings server. | Not a CS API IUT. Useful only for adapter ideas or adjacent observation/tasking lifecycle patterns. |
| 52North `pygeoapi` `feature/connected-systems` | Historical/direct branch | Official OGC implementation list points here; current branch ref `eb502577fd1ee0c836f709663f50800f4ce361a3`. | Secondary source target if `connected-systems-pygeoapi` self-run needs upstream comparison. |

## 52North Public Probe Evidence

Archived under:
`ops/test-results/sprint-ets-78-alternate-iut-discovery-followup-2026-08-03/`.

Readiness audit:

- Command: `python3 scripts/mutation-readiness-audit.py --iut-url https://csa.demo.52north.org --output /tmp/sprint78-52north-readiness.json`
- Methods: `GET=1`, `OPTIONS=25`
- Unsafe methods: `[]`
- Declaration/method-ready classes: `[]`
- Prerequisite-declaration-ready classes: `[]`

Safe public GET/OPTIONS probes:

- `GET /conformance` -> `200`; archived
  `52north-public-conformance.json` contains only
  `http://www.opengis.net/spec/ogcapi-common-1/1.0/conf/core`.
- `GET /systems` -> `200`; `OPTIONS /systems` advertises `GET, POST`.
- `GET /systems/040g` -> `404`; `OPTIONS /systems/040g` advertises
  `GET, PUT, DELETE`.
- `GET /datastreams` -> `500`; `OPTIONS /datastreams` advertises
  `GET, PUT, DELETE`.
- `GET /datastreams/040g/observations` -> `500`; `OPTIONS` advertises
  `GET, POST, PUT, DELETE`.
- `GET/OPTIONS /controlstreams`, `/commands`, and `/feasibility` -> `404`.

Static 52North source inspection:

- `routes/csa.py` exposes Part 1 write routes for Systems, Procedures,
  Deployments, nested Subsystems, nested SamplingFeatures, and nested
  Datastreams.
- `routes/csa.py` exposes Datastream and Observation write routes, but no
  ControlStream, Command, CommandStatus, CommandResult, Feasibility, or
  SystemEvent route set was found.
- `api.py` has a commented-out PATCH handler; route decorators do not advertise
  PATCH.
- `provider/part1/part1.py#get_conformance()` returns only OGC API Common Core.
- `provider/part2/part2.py#get_conformance()` returns `[]`.

Evidence hygiene note: archived 52North source snippets may contain upstream
sample compose defaults such as `password`. These are public example defaults,
not active credentials from this environment.

## Next Recommended No-Input Experiment

Run a disposable local 52North `connected-systems-pygeoapi` experiment from
pinned commit `18c1ce803fcdb2de3aac9d227ab814306d8a718f`.

Use its own Elasticsearch/TimescaleDB state, generated local read/write
credentials, and bundled simulator/request fixtures. First target direct local
readiness and lifecycle evidence only:

1. Seed System, Procedure, Deployment, SamplingFeature, Property, Datastream,
   and Observation resources.
2. Run `scripts/mutation-readiness-audit.py` against the local URL, including
   concrete discovered IDs where possible.
3. Attempt only source-proven local POST/GET/PUT/GET/DELETE/GET lifecycles.
4. Archive cleanup/isolation evidence.
5. Defer TeamEngine until direct evidence shows at least one
   declaration/method-ready class.

Expected value: this determines whether 52North can complement OSH and
`connected-systems-go` for Part 1 CRD or Datastream/Observation diagnostics
without risking public mutation.
