# CP-038: Alternate IUT Discovery Follow-up

## Status

COMPLETE_RAZE_APPROVED

## User Instruction

"Continue alternate IUT discovery"

## Problem

Sprint 75 found `connected-systems-go` as the strongest alternate mutable-IUT
candidate, Sprint 76 proved useful local Part 2 lifecycle behavior, and Sprint
77 packaged the Go implementation blockers for future upstream outreach. The
alternate-IUT landscape is still moving, and current source/web evidence should
be refreshed before selecting another expensive self-run target or exact
closure sprint.

## Change

- Refresh current source-backed discovery for direct or adjacent open-source
  OGC API Connected Systems implementations beyond local OSH and the already
  audited `connected-systems-go` path.
- Archive evidence from the official OGC implementation registry, GitHub
  repository metadata, source clones, and safe public read-only probes.
- Classify candidates as immediate self-run candidates, medium-term watch
  candidates, or non-direct adjacent systems.
- Keep public probing read-only: GET and OPTIONS only, no credentials, no
  public IUT mutation, and no exact candidate promotion.

## Non-Goals

- Do not mutate any public candidate IUT.
- Do not patch, fork, or modify third-party source.
- Do not rerun TeamEngine or Maven for this documentation/discovery sprint.
- Do not promote Part 1 or Part 2 Create/Replace/Delete or Update candidates
  to reviewed exact.
- Do not file or send upstream outreach from this environment.

## Acceptance

- [x] The follow-up evidence directory records raw source/probe artifacts and a
  compact candidate summary.
- [x] The product brief identifies any newly found direct CS API server claims
  and whether they are runnable IUT candidates.
- [x] Public readiness probes record GET/OPTIONS-only behavior and
  `unsafeMethodsIssued=[]`.
- [x] OpenSpec, story, contract, traceability, ops status, changelog,
  known-issues, test-results, and handoff documents agree.
- [x] Lightweight JSON/YAML/Markdown/diff verification is recorded.
- [x] Raze reviews the scoped documentation and evidence before completion.

## Result

Sprint 78 archives current alternate-IUT follow-up evidence under
`ops/test-results/sprint-ets-78-alternate-iut-discovery-followup-2026-08-03/`.
The official registry still lists OpenSensorHub Server and 52North pygeoapi
extension as server-side open-source implementations. The refresh classifies
52North `connected-systems-pygeoapi` as the best practical source-run watch
candidate, but not exact-promotion ready because current conformance provider
output is incomplete and public readiness remains non-promoting. DGIWG Glaux
Server is recorded as a new direct CS API server claim, but current public
evidence is README-only with no runnable source, license metadata, or public
endpoint. The public `connected-systems-go` refresh remains read-only and
non-promoting. Lightweight JSON/YAML/Markdown/public-probe/ignored-log/diff
verification passes. Initial Raze returned `GAPS_FOUND 0.90`; all required
fixes were applied, and the focused Raze recheck returned `APPROVE 0.94` with
`required_fixes=[]`.
