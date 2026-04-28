# S-ETS-02-03 — Verified-canonical-URI mapping table (2026-04-28)

> Per S-ETS-02-03 acceptance criterion #1 ("Generator produces a verified-canonical-URI
> mapping table BEFORE editing any code"). Each candidate canonical URI was curl-verified
> against `raw.githubusercontent.com/opengeospatial/ogcapi-common/master/19-072/requirements/<class>/<file>.adoc`.
> The .adoc file's `*Requirement {counter:req-id}* |*/req/<class>/<X>*` line is the
> verified canonical form. Quinn spot-checks this table during gate review.

## Methodology

```bash
# Discover the actual subdirectory layout (not assumed)
curl -s "https://api.github.com/repos/opengeospatial/ogcapi-common/contents/19-072/requirements" | grep '"name"'
# Result: only 4 subdirs: html, json, landing-page, oas30
# The /req/core/* form is FICTIONAL — neither OGC 19-072 nor any of its subdirs
# defines `core/` as a requirements class. All landing-page-related reqs are under
# the `landing-page/` subdir. Sprint 1's `/req/core/...` form was a Java-side
# convention error; the v1.0 TS web app used `/req/ogcapi-common/...` which was
# similarly drift from the canonical `.adoc` form.

# For each candidate, fetch the .adoc and inspect the canonical req-id line
for f in REQ_root-success REQ_conformance-success REQ_api-definition-success; do
  curl -s "https://raw.githubusercontent.com/opengeospatial/ogcapi-common/master/19-072/requirements/landing-page/${f}.adoc" \
    | grep '\*Requirement.*\*/req/'
done
curl -s "https://raw.githubusercontent.com/opengeospatial/ogcapi-common/master/19-072/requirements/oas30/REQ_oas-impl.adoc" \
  | grep '\*Requirement.*\*/req/'
```

## Mapping table

| Java constant (in `conformance/core/*.java`) | Sprint 1 form (incorrect) | OGC canonical (.adoc verified 2026-04-28) | .adoc source URL | HTTP status |
|---|---|---|---|---|
| `REQ_ROOT_SUCCESS` | `/req/core/root-success` | `/req/landing-page/root-success` | https://raw.githubusercontent.com/opengeospatial/ogcapi-common/master/19-072/requirements/landing-page/REQ_root-success.adoc | 200 |
| `REQ_CONFORMANCE_SUCCESS` | `/req/core/conformance-success` | `/req/landing-page/conformance-success` | https://raw.githubusercontent.com/opengeospatial/ogcapi-common/master/19-072/requirements/landing-page/REQ_conformance-success.adoc | 200 |
| `REQ_API_DEFINITION_SUCCESS` | `/req/core/api-definition-success` | `/req/landing-page/api-definition-success` | https://raw.githubusercontent.com/opengeospatial/ogcapi-common/master/19-072/requirements/landing-page/REQ_api-definition-success.adoc | 200 |
| `REQ_OAS30_OAS_IMPL` | `/req/oas30/oas-impl` | `/req/oas30/oas-impl` (already canonical — no change needed) | https://raw.githubusercontent.com/opengeospatial/ogcapi-common/master/19-072/requirements/oas30/REQ_oas-impl.adoc | 200 |

## CS API conformance class URI (separate — not /req)

| Java constant | URI form | Notes |
|---|---|---|
| `CS_CORE_CONFORMANCE_URI` | `/conf/core` | OGC 23-001 conformance-class identifier (NOT a /req/* requirement). Path layout is `ogcapi-connectedsystems-1/1.0/conf/core` per the IUT's conformsTo declaration; no .adoc lookup needed because the IUT itself declares the URI string. Preserved verbatim from Sprint 1. |

## Sweep scope

Files modified by S-ETS-02-03:

### New repo (`ets-ogcapi-connectedsystems10`)
- `src/main/java/.../conformance/core/LandingPageTests.java` — 3 `static final String REQ_*` constants (root-success, conformance-success, api-definition-success)
- `src/main/java/.../conformance/core/ConformanceTests.java` — 1 `static final String REQ_*` constant (conformance-success)
- `src/main/java/.../conformance/core/ResourceShapeTests.java` — 1 `static final String REQ_*` constant (conformance-success); REQ_OAS30_OAS_IMPL already canonical
- `src/test/java/.../VerifyETSAssert.java` — `REQ_TEST_URI` test fixture string updated to canonical form (smoke for the sweep)
- Javadoc references in test class headers updated where they mention the old `/req/core/...` form

### csapi_compliance repo (spec + traceability documentation)
- `openspec/capabilities/ets-ogcapi-connectedsystems/spec.md` — REQ-ETS-CORE-002/003/004 description text + SCENARIO descriptions + Implementation Status notes
- `_bmad/traceability.md` — cross-reference rows that mention the old form

## Spot-check (post-sweep verification)

Per acceptance criterion: "Spot-check: dereference 3 randomly-chosen updated URIs against OGC's normative document". Reproduce with:

```bash
# All 4 canonical forms must return HTTP 200 against the .adoc source:
for u in \
  "https://raw.githubusercontent.com/opengeospatial/ogcapi-common/master/19-072/requirements/landing-page/REQ_root-success.adoc" \
  "https://raw.githubusercontent.com/opengeospatial/ogcapi-common/master/19-072/requirements/landing-page/REQ_conformance-success.adoc" \
  "https://raw.githubusercontent.com/opengeospatial/ogcapi-common/master/19-072/requirements/landing-page/REQ_api-definition-success.adoc" \
  "https://raw.githubusercontent.com/opengeospatial/ogcapi-common/master/19-072/requirements/oas30/REQ_oas-impl.adoc"; do
  echo "$(curl -s -o /dev/null -w "%{http_code}" "$u")  $u"
done
# Expected: 4× 200 (all canonical paths resolve)
```

## Closes

- Sprint 1 contract `uri_mapping_fidelity_preserved` PARTIAL → PASS
- Quinn s02 GAP-2, Raze s02 GAP-2, Quinn s03 + Raze s03 inherited PARTIAL
- SCENARIO-ETS-CLEANUP-URI-CANONICALIZATION-001 (CRITICAL)
