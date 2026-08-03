# CP-035: Alternate Mutable IUT Discovery

## Status

COMPLETE

## User Instruction

"Continue as best you can. At the same time, start a Discovery agent to
research for any other open source implementation of the OGC CS API, especially
those that claim increased coverage than that of OSH"

## Problem

The remaining 47 released ATS procedures are mutation-bound. The current local
OSH target is useful as a disposable mutable IUT for safety, provisioning, and
cleanup evidence, but it does not declare enough prerequisite/update
conformance or advertise enough mutation method readiness to close the
remaining candidate mappings as reviewed exact.

## Change

- Run a Discovery agent to research open-source Connected Systems API
  implementations and public demos that may be better mutable-IUT candidates.
- Archive read-only readiness probes against public candidate deployments.
- Keep the probes non-mutating: GET `/conformance` plus OPTIONS only.
- Record findings without promoting any mutation-bound candidate mapping.

## Non-Goals

- Do not issue POST, PUT, PATCH, or DELETE against public candidate IUTs.
- Do not treat public demo readiness as certification evidence.
- Do not patch OSH, TeamEngine, or third-party implementations.
- Do not promote mutation-bound candidates to reviewed exact mappings.

## Acceptance

- Discovery writes `_bmad/product-brief.md` and
  `.harness/handoffs/discovery-handoff.yaml`.
- Public candidate read-only readiness probes are archived with
  `unsafeMethodsIssued=[]`.
- Findings identify whether any candidate is plausibly better than OSH for a
  future dedicated mutable-IUT closure sprint.
- Ops docs distinguish public read-only probe evidence from private dedicated
  mutable-IUT positive lifecycle evidence.

## Result

Discovery found `SomethingCreativeStudios/connected-systems-go` as the best
alternate open-source candidate beyond OSH for future self-run disposable Part
2 Create/Replace/Delete work. The public demo and source evidence are not
enough for exact promotion: public probes remained read-only, OPTIONS did not
provide `Allow` method readiness, Part 1 Create/Replace/Delete and Update
prerequisites remain incomplete, and no `/conf/update` plus real PATCH-route
evidence was found. 52North `connected-systems-pygeoapi` is promising but its
public demo currently declares only OGC API Common Core and showed runtime
health issues. SensorThings implementations are useful adjacent systems, not
direct CS API IUTs. Raze review returned `APPROVE_WITH_CONCERNS 0.91` with
`required_fixes=[]`. Implementation/evidence commit `8e9bb93` is pushed to
Botts `main`.
