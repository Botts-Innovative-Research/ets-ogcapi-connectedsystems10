# Sprint 78 Alternate IUT Discovery Follow-up Evidence

Scope: `REQ-ETS-CLEANUP-029` and
`SCENARIO-ETS-CLEANUP-ALT-IUT-DISCOVERY-FOLLOWUP-001`.

## Public Probe Policy

Public candidate IUT probes are read-only. The archived readiness outputs use
GET `/conformance` and OPTIONS method-readiness checks only. Sprint 78 does not
issue POST, PUT, PATCH, or DELETE against public IUTs and does not promote any
mutation-bound candidate mapping.

## Key Artifacts

- `candidate-summary.json`: compact candidate disposition summary.
- `ogc-implementations.adoc`: official OGC implementation registry snapshot.
- `ogc-connected-systems-ls-remote.txt`: observed registry repository HEAD.
- `github-52north-connected-systems-pygeoapi.json`: GitHub metadata for the
  52North implementation.
- `52north-connected-systems-pygeoapi-ls-remote.txt` and
  `clone-52north-head.txt`: observed 52North source HEAD.
- `52north-connected-systems-pygeoapi-readme.md`,
  `52north-connected-systems-pygeoapi-gitmodules.txt`, and
  `52north-connected-systems-pygeoapi-docker-compose-dev.yml`: selected
  self-run setup evidence.
- `52north-public-readiness-followup.json` and
  `52north-csa-public-readiness.json`: public 52North read-only readiness
  evidence.
- `52north-public-conformance.json`: archived safe GET body for public 52North
  `/conformance`, showing only OGC API Common Core in the current public demo.
- `github-dgiwg-p507-repos.json`, `github-dgiwg-glaux.json`,
  `github-dgiwg-glaux-server.json`, `dgiwg-glaux-readme.md`,
  `dgiwg-glaux-server-readme.md`, `dgiwg-glaux-ls-remote.txt`, and
  `dgiwg-glaux-server-ls-remote.txt`: DGIWG Glaux discovery evidence.
- `csapi-go-public-refresh-readiness.json`: refreshed public
  `connected-systems-go` readiness evidence.
- `source-grep-summary.txt`: curated source grep summary for 52North routes and
  DGIWG tree contents.

## Summary

The official registry still identifies OpenSensorHub Server and 52North
`connected-systems-pygeoapi` as server-side open-source CS API implementations.
The Sprint 78 refresh adds DGIWG Glaux Server as a new direct-claim watchlist
candidate, but the public repository is README-only and not runnable today.
52North remains a plausible future self-run candidate, but current source and
public-demo evidence do not make it an immediate declaring IUT. The refreshed
public `connected-systems-go` probe stays non-promotable for the same
declaration, OPTIONS, inherited-prerequisite, feasibility, and Update/PATCH
blockers captured in Sprint 77.
