# S-ETS-04-01: Historical Hosted CI Escalation

**Status**: Retired by CP-003/ADR-012 on 2026-07-23
**Requirement**: REQ-ETS-CLEANUP-009 (superseded)

## Historical Record

This story closed an earlier repeated authorization blocker by dropping hosted
workflow activation from that sprint. It did not activate a workflow.

The later project-owner decision is definitive: project-operated hosted CI is
outside scope and will not be approved. The dormant workflow definition and all
future activation steps are removed. Local Docker Maven, exact-image runtime,
and TeamEngine E2E gates are authoritative.

Archived Sprint 4 evidence remains under `ops/test-results/` for chronology
only; it is not a roadmap or fallback activation path.
