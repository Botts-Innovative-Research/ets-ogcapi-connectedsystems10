# connected-systems-go Readiness Gap Request

## Status

Draft repo-local package. This environment did not file an upstream GitHub issue
or contact maintainers.

## Target

- Repository:
  `https://github.com/SomethingCreativeStudios/connected-systems-go`
- Audited source commit:
  `7643bb38bc9fa95a50332ed2aa5b1007b56b5028`
- Current upstream `main`/`HEAD` observed on 2026-08-03:
  `7643bb38bc9fa95a50332ed2aa5b1007b56b5028`
- Evidence directory:
  `ops/test-results/sprint-ets-76-connected-systems-go-readiness-2026-08-03/`
- ETS evidence commit:
  `a0f2803`

## Summary

The OGC API Connected Systems ETS project is evaluating open-source mutable IUTs
for the remaining mutation-bound Create/Replace/Delete and Update conformance
tests. A self-run disposable `connected-systems-go` instance shows promising
Part 2 Create/Replace/Delete route behavior, but it is not yet usable as an
exact-promotion IUT for the ETS because declaration and method-readiness
blockers remain.

## Positive Evidence

- The service built and ran locally from upstream commit
  `7643bb38bc9fa95a50332ed2aa5b1007b56b5028` with disposable PostGIS storage.
- Direct lifecycle probe passed `29/29` HTTP steps.
- Real mutation method counts were `POST=5`, `PUT=4`, `DELETE=5`, with
  `GET=15`.
- Positive POST/GET/PUT/GET/DELETE/GET lifecycles were proven for DataStream,
  Observation, ControlStream, and Command resources.
- Direct cleanup returned sprint-created lifecycle resource counts to zero.

## Blockers To Exact ETS Use

### 1. Connected Systems Part 1 Core Declaration

TeamEngine E2E against the self-run IUT returned
`275 total / 15 passed / 1 failed / 259 skipped`. The single failure was
`conformancePageDeclaresCsCore` because `/conformance` declares Part 1
`/conf/api-common` but not:

`http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/core`

Request: declare the Connected Systems Part 1 Core conformance class when the
implementation satisfies that class, or document why the current conformance
page intentionally omits it.

### 2. Connected Systems Part 1 Create/Replace/Delete Declarations

Sprint 76 readiness evidence also keeps Part 1 Create/Replace/Delete
non-promotable. The Part 1 CRD bucket reports missing:

- `http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/create-replace-delete`
- exact inherited prerequisite
  `http://www.opengis.net/spec/ogcapi-4/1.0/conf/create-replace-delete`

Request: if Part 1 Create/Replace/Delete is in scope, declare the Connected
Systems Part 1 CRD conformance class and the exact inherited OGC API Features
CRD prerequisite expected for Part 1 exactness. If the implementation uses a
newer Features Part 4 URI instead, document the intended mapping so the ETS can
distinguish Part 1 exactness from Part 2 Features Part 4 declarations.

### 3. OPTIONS Allow Does Not Advertise Implemented Write Routes

The readiness audit intentionally uses GET `/conformance` and OPTIONS probes
only. It reported `GET=1`, `OPTIONS=25`, `unsafeMethodsIssued=[]`, and zero
declaration/method-ready classes. This conflicts with direct lifecycle evidence
that the service accepts POST/PUT/DELETE on several Part 2 routes.

Examples from Sprint 76:

| Route family | Direct lifecycle result | Placeholder-readiness symptom | Lifecycle-ID readiness symptom |
|--------------|-------------------------|--------------------------------|-------------------------------|
| `/systems/{systemId}/datastreams` | POST returned 201 | OPTIONS reported `Allow: GET` | OPTIONS reported `Allow: GET` |
| `/datastreams/{datastreamId}` | PUT and DELETE returned 204 | OPTIONS omitted part of the expected write surface | OPTIONS omitted part of the expected write surface |
| `/datastreams/{datastreamId}/observations` | POST returned 201 | OPTIONS reported `Allow: GET` | OPTIONS reported `Allow: GET` |
| `/observations/{observationId}` | PUT and DELETE returned 204 | skipped because no concrete id was supplied | OPTIONS still omitted PUT/DELETE |
| `/systems/{systemId}/controlstreams` | POST returned 201 | OPTIONS reported `Allow: GET` | OPTIONS reported `Allow: GET` |
| `/controlstreams/{controlstreamId}` | PUT and DELETE returned 204 | OPTIONS omitted part of the expected write surface | OPTIONS omitted part of the expected write surface |
| `/controlstreams/{controlstreamId}/commands` | POST returned 201 | OPTIONS reported `Allow: GET` | OPTIONS reported `Allow: POST` |
| `/commands/{commandId}` | PUT and DELETE returned 204 | skipped because no concrete id was supplied | OPTIONS still omitted PUT/DELETE |

Request: make OPTIONS `Allow` advertise the actual supported write methods for
collection and item resources, at least for the routes that already accept
POST/PUT/DELETE.

### 4. Inherited OGC API Features Part 4 CRD Declaration

The service declares Connected Systems Part 2 Create/Replace/Delete, but the ETS
also needs the inherited OGC API Features Part 4 Create/Replace/Delete
declaration:

`http://www.opengis.net/spec/ogcapi-features-4/1.0/conf/create-replace-delete`

Request: declare this prerequisite when the implementation satisfies it, or
document the intended standard mapping if another conformance declaration is
being used.

### 5. Update/PATCH Surface

The remaining Update candidate procedures cannot run exactly without both
declarations, condition classes, and PATCH behavior. Sprint 76 found no current
evidence for:

- `http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/update`
- `http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/update`
- `http://www.opengis.net/spec/ogcapi-4/1.0/conf/update`
- `http://www.opengis.net/spec/ogcapi-features-4/1.0/conf/update`
- Part 2 condition class
  `http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/feasibility`
- OPTIONS `Allow: PATCH` on the relevant resource item routes

Request: if Update is in scope for the implementation, expose and declare the
Update/PATCH surface with changed-resource GET proof and cleanup-safe behavior.
If Update is out of scope, documenting that would keep ETS expectations clear.

## What Would Unblock A Future ETS Closure Sprint

For Part 2 Create/Replace/Delete exactness, the practical next validation path
would be:

- `/conformance` declares Connected Systems Part 1 Core.
- `/conformance` declares Connected Systems Part 1
  `http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/create-replace-delete`
  plus the exact inherited Part 1 prerequisite
  `http://www.opengis.net/spec/ogcapi-4/1.0/conf/create-replace-delete` if
  Part 1 CRD exactness is in scope.
- `/conformance` declares
  `http://www.opengis.net/spec/ogcapi-features-4/1.0/conf/create-replace-delete`
  in addition to Connected Systems Part 2 `/conf/create-replace-delete`.
- OPTIONS `Allow` advertises POST/PUT/DELETE on the concrete mutable resource
  routes that actually support those methods.
- A disposable local run can repeat POST/GET/PUT/GET/DELETE/GET lifecycles with
  cleanup and primary-state isolation.
- TeamEngine smoke no longer fails the base conformance-page declaration check.

For Update exactness, the analogous path additionally needs exact Update
declarations, OPTIONS `Allow: PATCH`, positive PATCH lifecycle behavior, GET
verification of changed fields, Part 2 `/conf/feasibility` condition readiness,
schema-rejection evidence, and cleanup.

## Evidence References

- Direct readiness:
  `readiness-audit.json`
- Lifecycle-ID readiness:
  `readiness-audit-lifecycle-ids.json`
- Direct lifecycle:
  `lifecycle-summary.json` and `lifecycle-steps.jsonl`
- TeamEngine E2E:
  `teamengine-smoke-summary.json`
- Cleanup:
  `db-resource-counts-after-lifecycle.tsv`,
  `docker-cleanup-verification.log`, and
  `smoke-docker-cleanup-verification.log`
