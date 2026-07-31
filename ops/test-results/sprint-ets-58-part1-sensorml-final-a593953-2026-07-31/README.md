# Sprint 58 Final Evidence

- Source candidate: `a593953d8d79d977649db3077696148e90ffb44a`
- TeamEngine image: `sha256:c0227ab3ef9d67a27d8d22a119979eda7615df10bbbc43c9e50a52daffdff093`
- Clean Docker Maven: 729 tests, 0 failures, 0 errors, 3 skipped.
- SensorML focused tests: 37 tests, 0 failures, 0 errors, 0 skipped.
- Coverage: 240 total, 91 exact, 2 helper, 118 candidate, 29 unmapped; SensorML is 15/15 exact.
- Schema parity: 8 entry schemas and 63 transitive schemas, zero graph or semantic mismatches.
- Exact local OSH TeamEngine: 246 total, 41 passed, 21 failed, 184 skipped.
- SensorML E2E: all 15 methods executed; `mediatype-write` passed and 14 procedures failed honestly on OSH media/OpenAPI evidence.
- No mutation: 194 recognized local-OSH request logs, zero IUT writes.
- Exact-image runtime: SWE Common adapter, SensorML adapter, OpenAPI 3.1 parser, external-fetch security controls, dependency parity, and TeamEngine base immutability passed.
- Dependency sabotage: 246 total, 2 passed, 10 failed, 234 skipped; all 15 SensorML methods dependency-skipped.
- Credential integration and wire E2E passed with zero unmasked test-artifact hits, 30 masked container-log hits, and 30 intact stub receipts.
- Artifact hygiene passed with zero credential leaks and zero IUT writes.
- Final implementation Raze: APPROVED, confidence 0.99, no required fixes.
- Final reconciliation Raze: initial GAPS_FOUND for two chronology labels;
  focused recheck APPROVED at 0.99 after both findings were closed.

The 21 local-OSH failures are IUT conformance outcomes, not harness failures.
The unmodified OSH target returns generic `application/json` for requested
SensorML collections and its advertised OpenAPI definition lacks complete read
media evidence. The ETS fails those conditions visibly.
