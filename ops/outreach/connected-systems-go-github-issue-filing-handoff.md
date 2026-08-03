# connected-systems-go GitHub Issue Filing Handoff

## Filing Status

Ready for authenticated filing. Not filed from this environment.

Reason: GitHub issues are enabled for
`SomethingCreativeStudios/connected-systems-go`, but this shell has no `gh`
CLI and no `GH_TOKEN` or `GITHUB_TOKEN`.

## Target

- Repository: `https://github.com/SomethingCreativeStudios/connected-systems-go`
- Issues enabled: yes
- Current `HEAD`/`main`: `7643bb38bc9fa95a50332ed2aa5b1007b56b5028`
- Audited Sprint 76 commit: `7643bb38bc9fa95a50332ed2aa5b1007b56b5028`
- Duplicate search: no open duplicate found by the Sprint 80 query; one
  closed issue exists for an API-definition topic.

## Issue Title

Conformance gaps blocking OGC API Connected Systems ETS mutable-IUT use

## Issue Body

The OGC API Connected Systems ETS project is evaluating open-source mutable
IUTs for the remaining Create/Replace/Delete and Update conformance tests.
A self-run disposable `connected-systems-go` instance at commit
`7643bb38bc9fa95a50332ed2aa5b1007b56b5028` shows promising Part 2
Create/Replace/Delete route behavior, but it is not yet usable as an
exact-promotion IUT for the ETS because declaration and method-readiness
blockers remain.

This is not a failure report against public demo data. It is a request to
confirm whether the current conformance declarations and mutation surfaces are
intentional, incomplete, or in scope for future work.

### Positive local evidence

- The service built and ran locally from upstream commit
  `7643bb38bc9fa95a50332ed2aa5b1007b56b5028` with disposable PostGIS storage.
- A direct lifecycle probe passed `29/29` HTTP steps.
- Real mutation method counts were `POST=5`, `PUT=4`, `DELETE=5`, with
  `GET=15`.
- Positive POST/GET/PUT/GET/DELETE/GET lifecycles were proven for DataStream,
  Observation, ControlStream, and Command resources.
- Direct cleanup returned sprint-created lifecycle resource counts to zero.

### Blockers

1. Connected Systems Part 1 Core is not declared.

TeamEngine E2E against the self-run IUT returned
`275 total / 15 passed / 1 failed / 259 skipped`. The single failure was
`conformancePageDeclaresCsCore` because `/conformance` declares Part 1
`/conf/api-common` but not:

`http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/core`

Request: declare Connected Systems Part 1 Core when satisfied, or document why
it is intentionally omitted.

2. Part 1 Create/Replace/Delete declarations are missing or inconclusive.

The ETS readiness evidence keeps Part 1 Create/Replace/Delete non-promotable
because these declarations are missing:

- `http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/create-replace-delete`
- `http://www.opengis.net/spec/ogcapi-4/1.0/conf/create-replace-delete`

Request: if Part 1 Create/Replace/Delete is in scope, declare the Connected
Systems Part 1 CRD class and the exact inherited OGC API Features CRD
prerequisite expected for Part 1 exactness.

3. OPTIONS `Allow` does not advertise implemented write routes.

The read-only readiness audit issued `GET=1`, `OPTIONS=25`, and
`unsafeMethodsIssued=[]`, but found zero declaration/method-ready classes. This
conflicts with direct lifecycle evidence that the service accepts
POST/PUT/DELETE on several Part 2 routes.

Examples:

| Route family | Direct lifecycle result | Readiness symptom |
|--------------|-------------------------|-------------------|
| `/systems/{systemId}/datastreams` | POST returned 201 | OPTIONS reported `Allow: GET` |
| `/datastreams/{datastreamId}` | PUT and DELETE returned 204 | OPTIONS omitted part of the expected write surface |
| `/datastreams/{datastreamId}/observations` | POST returned 201 | OPTIONS reported `Allow: GET` |
| `/observations/{observationId}` | PUT and DELETE returned 204 | OPTIONS still omitted PUT/DELETE |
| `/systems/{systemId}/controlstreams` | POST returned 201 | OPTIONS reported `Allow: GET` |
| `/controlstreams/{controlstreamId}` | PUT and DELETE returned 204 | OPTIONS omitted part of the expected write surface |
| `/controlstreams/{controlstreamId}/commands` | POST returned 201 | placeholder OPTIONS reported `Allow: GET`; concrete lifecycle-id OPTIONS reported `Allow: POST` |
| `/commands/{commandId}` | PUT and DELETE returned 204 | OPTIONS still omitted PUT/DELETE |

Request: make OPTIONS `Allow` advertise the actual supported write methods for
collection and item resources, at least for the routes that already accept
POST/PUT/DELETE.

4. OGC API Features Part 4 Create/Replace/Delete is not declared.

The service declares Connected Systems Part 2 Create/Replace/Delete, but the
ETS also needs this inherited OGC API Features Part 4 declaration:

`http://www.opengis.net/spec/ogcapi-features-4/1.0/conf/create-replace-delete`

Request: declare this prerequisite when satisfied, or document the intended
standard mapping if another declaration is being used.

5. Update/PATCH surface is not ready.

The remaining Update candidate procedures cannot run exactly without the
expected declarations, condition classes, and PATCH behavior. Current evidence
does not show:

- `http://www.opengis.net/spec/ogcapi-connectedsystems-1/1.0/conf/update`
- `http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/update`
- `http://www.opengis.net/spec/ogcapi-4/1.0/conf/update`
- `http://www.opengis.net/spec/ogcapi-features-4/1.0/conf/update`
- `http://www.opengis.net/spec/ogcapi-connectedsystems-2/1.0/conf/feasibility`
- OPTIONS `Allow: PATCH` on relevant resource item routes

Request: if Update is in scope, expose and declare the Update/PATCH surface
with changed-resource GET proof and cleanup-safe behavior. If Update is out of
scope, documenting that would keep ETS expectations clear.

### What would unblock a future ETS closure sprint

For Part 2 Create/Replace/Delete exactness:

- `/conformance` declares Connected Systems Part 1 Core.
- `/conformance` declares Connected Systems Part 1
  Create/Replace/Delete and the exact inherited
  `http://www.opengis.net/spec/ogcapi-4/1.0/conf/create-replace-delete` if
  Part 1 CRD exactness is in scope.
- `/conformance` declares
  `http://www.opengis.net/spec/ogcapi-features-4/1.0/conf/create-replace-delete`
  in addition to Connected Systems Part 2 `/conf/create-replace-delete`.
- OPTIONS `Allow` advertises POST/PUT/DELETE on concrete mutable resource
  routes that actually support those methods.
- A disposable local run can repeat POST/GET/PUT/GET/DELETE/GET lifecycles with
  cleanup and primary-state isolation.
- TeamEngine smoke no longer fails the base conformance-page declaration check.

For Update exactness, the analogous path additionally needs exact Update
declarations, OPTIONS `Allow: PATCH`, positive PATCH lifecycle behavior, GET
verification of changed fields, Part 2 `/conf/feasibility` condition readiness,
schema-rejection evidence, and cleanup.

Thanks for any clarification on whether these gaps are intentional, already on
your roadmap, or good candidates for contribution.

## Authenticated Filing Commands

With GitHub CLI:

```bash
gh issue create \
  --repo SomethingCreativeStudios/connected-systems-go \
  --title "Conformance gaps blocking OGC API Connected Systems ETS mutable-IUT use" \
  --body-file ops/outreach/connected-systems-go-github-issue-body.md
```

With GitHub REST API and a token:

```bash
curl -sS -X POST \
  -H "Accept: application/vnd.github+json" \
  -H "Authorization: Bearer $GH_TOKEN" \
  https://api.github.com/repos/SomethingCreativeStudios/connected-systems-go/issues \
  --data @ops/outreach/connected-systems-go-github-issue-payload.json
```
