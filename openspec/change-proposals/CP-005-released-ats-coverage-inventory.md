# Change Proposal: CP-005 Released ATS Coverage Inventory

**Date**: 2026-07-26
**Author**: Codex
**Affects**:

- `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md`
- `openspec/capabilities/ets-ogcapi-connectedsystems/design.md`
- `REQ-ETS-COVERAGE-001`
- all Part 1 and Part 2 conformance requirements

**Status**: Accepted by the user's instruction to execute recommendations 1-4

## Motivation

Project planning currently mixes released OGC 23-001/23-002 content, a newer
Connected Systems repository commit used for OpenAPI input, and historical
minimal-subset implementation claims. That produced incorrect planning counts:
Part 1 was described as 14 classes and Part 2 as 13 groups. It also allowed a
class to be called implemented when only a selected subset of its Annex A tests
existed.

The approved standards are the certification authority. Their released source
tag `v1.0.0` at commit
`8e03b236a049849f2ccc24b4fd9fdce5ff69bed2` contains 13 Part 1 conformance
classes with 108 class tests plus two supporting tests, and 12 Part 2
conformance classes with 130 tests.

## New Requirement

### REQ-ETS-COVERAGE-001

The repository SHALL maintain a machine-readable inventory of every released
OGC 23-001 and OGC 23-002 Annex A abstract test. Each entry SHALL identify its
part, conformance class, test identifier, target requirement or recommendation,
and current ETS mapping. The audit SHALL distinguish:

- an exact one-to-one TestNG mapping;
- a candidate mapping found only by target URI;
- a supporting helper mapping;
- and an unmapped test.

Only an exact reviewed mapping may count as implemented. Class and requirement
status SHALL be derived from the inventory and may not be inferred from the
existence of a Java class or a successful smoke run.

## New Scenarios

- `SCENARIO-ETS-COVERAGE-RELEASED-SOURCES-001`
- `SCENARIO-ETS-COVERAGE-EXACT-INVENTORY-001`
- `SCENARIO-ETS-COVERAGE-COMPILED-MAPPING-001`
- `SCENARIO-ETS-COVERAGE-FAIL-CLOSED-001`
- `SCENARIO-ETS-COVERAGE-STATUS-HONESTY-001`

## Authority Boundary

- OGC documents 23-001 and 23-002, version 1.0, are normative.
- Repository tag `v1.0.0` commit `8e03b236...` is the reproducible source form
  used to extract the approved Annex A semantics.
- The newer OpenAPI input pin `3fd86c73...` remains usable for its separately
  documented schema/API purpose, but it is not an ATS authority.
- Later draft branches, IUT-specific declarations, and the frozen web
  application may identify interoperability work; they cannot add, remove, or
  rename released conformance classes.

## Acceptance Boundary

Sprint 45 closes when the semantic source extractor, committed inventory,
compiled TestNG mapping audit, self-tests, Maven verification, and real local
OSH TeamEngine regression all run reproducibly. It does not claim that all
inventory entries are implemented. Unmapped and candidate-only entries are the
authoritative backlog for the following Part 1 and Part 2 closure increments.

