# Test Results — OGC API Connected Systems ETS

Last updated: 2026-08-03T09:33Z

## Current Sprint Evidence

Sprint 78 alternate IUT Discovery follow-up:

- Trigger: user instructed BMAD Analyst / Discovery research for alternate
  open-source Connected Systems or adjacent mutable-IUT implementations beyond
  local OSH and `SomethingCreativeStudios/connected-systems-go`.
- Scope: discovery/source research and safe public probes only. Public probes
  used GET/OPTIONS only; no Docker, Maven, TeamEngine, or public mutation.
- Handoff:
  `.harness/handoffs/discovery-sprint-78-handoff.yaml`.
- Evidence directory:
  `ops/test-results/sprint-ets-78-alternate-iut-discovery-followup-2026-08-03/`.
- Implementation/evidence commit:
  pushed `dcc11bc`.
- Main finding:
  no new candidate is ready for exact promotion. 52North
  `connected-systems-pygeoapi` is the best practical source-run watch
  candidate; DGIWG Glaux Server is a direct CS API server claim but currently
  README-only in public source.
- 52North public readiness audit:
  `GET=1`, `OPTIONS=25`, `unsafeMethodsIssued=[]`, zero
  declaration/method-ready classes, zero prerequisite-declaration-ready
  classes. The Python audit hit public TLS/URL errors for readiness details; a
  separate safe `curl -k` probe archived `52north-public-conformance.json`,
  which contains only OGC API Common Core.
- 52North safe probe highlights:
  `/systems` is live and OPTIONS advertises POST, item OPTIONS advertises
  PUT/DELETE, Datastream and Observation OPTIONS advertise write methods, but
  public Datastream GETs returned 500 and `/controlstreams`, `/commands`, and
  `/feasibility` returned 404.
- Source findings:
  52North source exposes some Part 1 and Datastream/Observation write paths,
  but Part 1 provider conformance returns only OGC API Common Core, Part 2
  provider conformance returns `[]`, and PATCH is commented out.
- Other candidates:
  DGIWG Glaux Server is currently claim-only because the server repository
  exposes only README content, no implementation language, no license metadata,
  and no public endpoint evidence. OWSLib is a client library; FROST-Server is
  adjacent SensorThings, not a Connected Systems server.
- Compact summary:
  `ops/test-results/sprint-ets-78-alternate-iut-discovery-followup-2026-08-03/candidate-summary.json`.
- Lightweight verification:
  PASS for 19 archived JSON files, 4 YAML files, required artifact presence,
  public probe policy, ignored-log hygiene, evidence manifest verification, and
  `git diff --check`.
- Raze:
  initial review returned `GAPS_FOUND 0.90`; required fixes were applied by
  renaming ignored `.log` artifacts to `.txt`, correcting the Sprint 78
  scenario in `candidate-summary.json`, archiving
  `52north-public-conformance.json`, and updating the JSON parse count. Focused
  Raze recheck returned `APPROVE 0.94` with `required_fixes=[]`.

Sprint 77 `connected-systems-go` upstream gap package:

- Trigger: user instructed "Continue" after Sprint 76 self-run
  `connected-systems-go` readiness evidence was completed and pushed.
- Scope: `REQ-ETS-CLEANUP-028` documentation and upstream readiness handoff
  only. No IUT was mutated and TeamEngine was not rerun in this sprint.
- Outreach artifacts:
  `ops/outreach/connected-systems-go-readiness-gap-request.md` and
  `ops/outreach/connected-systems-go-readiness-gap-request.json`.
- Implementation/evidence commit:
  pushed `46f2217`.
- Push-status closeout commit:
  pushed `3e91dc5`.
- Evidence directory:
  `ops/test-results/sprint-ets-77-connected-systems-go-upstream-gap-package-2026-08-03/`.
- Upstream HEAD check:
  `upstream-head.txt` records `HEAD` and `refs/heads/main` at
  `7643bb38bc9fa95a50332ed2aa5b1007b56b5028`, matching the Sprint 76 audited
  source commit.
- Package contents:
  records positive direct lifecycle evidence (`29/29`, POST/PUT/DELETE
  lifecycles for DataStream, Observation, ControlStream, and Command) while
  preserving the blocking TeamEngine result (`275/15/1/259`, failure
  `conformancePageDeclaresCsCore`) and readiness audit result (`47`
  candidates, `GET=1`, `OPTIONS=25`, `unsafeMethodsIssued=[]`, zero
  declaration/method-ready classes).
- Blockers captured:
  missing Connected Systems Part 1 Core declaration, missing Part 1
  Create/Replace/Delete and exact inherited OGC API Features CRD prerequisite,
  incomplete OPTIONS `Allow` method advertisement, missing inherited OGC API
  Features Part 4 Create/Replace/Delete declaration, missing Part 2 Feasibility
  condition readiness, and absent Update/PATCH declarations and method
  readiness.
- Outcome:
  the package is ready to file or send manually, but it is not an upstream issue
  submission and does not promote any mutation-bound candidate mapping.
- Raze:
  initial review returned `GAPS_FOUND 0.91` for underreported exact blockers,
  premature completion wording, and one ambiguous OPTIONS example. First
  focused recheck returned `GAPS_FOUND 0.88` for one OPTIONS table cell. Second
  focused recheck returned `APPROVE 0.97` with `required_fixes=[]`.

Sprint 76 self-run `connected-systems-go` readiness:

- Trigger: user approved running the controlled self-run alternate mutable IUT
  readiness experiment.
- Scope: `REQ-ETS-CLEANUP-027` operational readiness evidence only. This is not
  exact ATS promotion evidence.
- Source: `SomethingCreativeStudios/connected-systems-go` commit
  `7643bb38bc9fa95a50332ed2aa5b1007b56b5028`.
- Evidence directory:
  `ops/test-results/sprint-ets-76-connected-systems-go-readiness-2026-08-03/`.
  Evidence/docs commit: pushed `a0f2803`.
- Direct readiness audit:
  `47` remaining candidates, `GET=1`, `OPTIONS=25`,
  `unsafeMethodsIssued=[]`, zero declaration/method-ready classes, zero
  prerequisite-declaration-ready classes, and no exact-promotion-ready classes.
- Lifecycle-ID readiness audit:
  `GET=1`, `OPTIONS=29`, `unsafeMethodsIssued=[]`, zero
  exact-promotion-ready classes.
- Direct lifecycle probe:
  PASS, `29/29` real HTTP steps, method counts `GET=15`, `POST=5`, `PUT=4`,
  `DELETE=5`, and positive POST/GET/PUT/GET/DELETE/GET lifecycles for
  DataStream, Observation, ControlStream, and Command.
- Cleanup:
  direct lifecycle resource counts returned to zero; all Sprint 76 disposable
  containers and networks were removed; primary local OSH was not used.
- TeamEngine E2E:
  NON_GREEN_HONEST, `275 total / 15 passed / 1 failed / 259 skipped`.
  The single failure is `conformancePageDeclaresCsCore` because the IUT omits
  Part 1 `/conf/core`. TeamEngine no-mutation oracle passed:
  `recognized_iut_request_logs=16`, `no_mutation_oracle_exit=0`.
- Lightweight verification:
  JSON parse PASS for 26 archived JSON files; JSONL parse PASS for 2 JSONL
  files; YAML parse PASS for the Sprint 76 contract; evidence manifest
  verification PASS.
- Raze:
  initial static review `GAPS_FOUND 0.88`, with required bookkeeping fixes for
  commit reachability, status reconciliation, and manifest check-log scope. All
  three are addressed by the reconciled docs/manifest and pushed commit
  `a0f2803`.
- Outcome:
  route-level Part 2 CRD lifecycle feasibility is proven locally, but
  declaration/OPTIONS/Update blockers keep all 47 mutation-bound candidates
  non-promoted.

Libby SWE Common validator pointer triage:

- Trigger: Libby pointed to `opengeospatial/ets-swecommon30` branch
  `issue-9-swecommon-validation-module` and
  `docs/swecommon-validation-module.md`.
- Branch/doc finding: upstream `swecommon30-validator` is a reusable validator
  module for ETS consumers and keeps TestNG/TeamEngine behavior in the
  consuming ETS. The branch HEAD inspected locally is
  `3ba75ceabe57cea85f4a8513c59e0f90e386ba96`.
- Local status: this project already source-pins that exact commit through
  `scripts/bootstrap-swecommon30-validator.sh`, declares
  `org.opengis.cite:swecommon30-validator:0.1-SNAPSHOT`, and consumes it
  through `ConnectedSystemsSweValidatorAdapter`.
- Focused Docker Maven verification:
  `bash scripts/mvn-test-via-docker.sh -Dtest=VerifyConnectedSystemsSweValidatorAdapter`.
- Result: upstream source verification PASS, validator bootstrap PASS, focused
  adapter tests `4 tests / 0 failures / 0 errors / 0 skipped`, build SUCCESS.
- Impact: no product code change required. This confirms the SWE Common
  validator path is already integrated; it does not change the remaining
  SensorML validator replacement risk or the 47 mutation-bound IUT blockers.

Sprint 75 alternate mutable IUT discovery:

- Trigger: user instructed continuing while starting Discovery Agent research
  into open-source OGC API Connected Systems implementations beyond OSH,
  especially candidates claiming greater coverage.
- Scope: `REQ-ETS-CLEANUP-026` read-only public-IUT discovery and operational
  blocker evidence only. No Java/TestNG implementation code was edited, no
  Maven/Docker/TeamEngine gate was rerun, no public IUT mutation was issued,
  and no Create/Replace/Delete or Update candidate mapping is promoted.
- Discovery artifacts:
  `_bmad/product-brief.md` and `.harness/handoffs/discovery-handoff.yaml`.
- Public probe evidence directory:
  `ops/test-results/sprint-ets-75-alternate-iut-discovery-2026-08-03/`.
- Public candidate probes:
  `connected-systems-go`, 52North CSA, and public OSH each record `GET=1`,
  `OPTIONS=25`, `unsafeMethodsIssued=[]`, no
  `classesWithDeclarationAndMethodReadiness`, and no
  `classesWithDeclarationMethodAndPrerequisiteDeclarationReadiness`.
- Best alternate candidate found:
  `SomethingCreativeStudios/connected-systems-go`, because current public and
  source evidence show Part 1 `/conf/api-common`, Part 2 `/conf/api-common`,
  Part 2 `/conf/create-replace-delete`, and source-level POST/PUT/DELETE
  routes/e2e CRUD tests. It is a future self-run disposable Part 2
  Create/Replace/Delete candidate, not current exact-promotion evidence.
- Remaining blocker:
  no researched candidate currently provides complete exact prerequisite
  declarations, `/conf/update`, real PATCH-route evidence, `Allow` method
  readiness, positive mutation lifecycle proof, cleanup/isolation, and
  service-description write-operation evidence for all 47 remaining
  mutation-bound procedures.
- Lightweight verification:
  JSON parse PASS for 7 archived JSON files; YAML parse PASS for the Sprint 75
  contract, Discovery handoff, and Raze report; repository manifest
  verification PASS for 22 manifest-listed evidence files; Sprint 75 `.log`
  evidence is unignored.
- Raze returned `APPROVE_WITH_CONCERNS 0.91` with `required_fixes=[]`.
- Implementation/evidence commit `8e9bb93` is pushed to Botts `main`.

Sprint 74 prerequisite declaration field clarity:

- Trigger: user instructed "Continue."
- Scope: `REQ-ETS-CLEANUP-025` compatibility-preserving field clarity only. No
  Create/Replace/Delete or Update lifecycle exact closure is claimed.
- Implementation: `scripts/mutation-readiness-audit.py` now emits explicit
  prerequisite-declaration readiness fields:
  `declarationMethodAndPrerequisiteDeclarationReadiness`,
  `classesWithDeclarationMethodAndPrerequisiteDeclarationReadiness`, and
  `prerequisiteDeclarationReadinessPolicy`.
- Compatibility: Sprint 73 fields
  `declarationMethodAndPrerequisiteReadiness`,
  `classesWithDeclarationMethodAndPrerequisiteReadiness`, and
  `prerequisiteReadinessPolicy` remain present and equal to the explicit
  Sprint 74 fields.
- Python compile: PASS (`pycompile.log`, `pycompile:0`).
- Python unit tests:
  `21 tests / 0 failures / 0 errors` (`unittest.log`).
- Formatter: BUILD SUCCESS (`spring-javaformat.log`).
- Full Docker Maven:
  `787 tests / 0 failures / 0 errors / 3 skipped`
  (`docker-maven-test.log`).
- Direct primary local OSH readiness audit:
  `47` candidates, `GET=1`, `OPTIONS=25`, `unsafeMethodsIssued=[]`,
  `classesWithDeclarationAndMethodReadiness=["1:/conf/create-replace-delete"]`,
  `classesWithDeclarationMethodAndPrerequisiteDeclarationReadiness=[]`, old/new
  alias equality, and no exact promotions.
- Disposable local OSH mutable E2E:
  `sprint-ets-74-fields-20260803T042511Z`. The populated IUT readiness audit
  reported `GET=1`, `OPTIONS=27`, `unsafeMethodsIssued=[]`, one
  declaration/method-ready class, zero prerequisite-declaration-ready classes,
  old/new alias equality, cleanup PASS, and primary-state isolation PASS.
- Full TeamEngine E2E remains honestly non-green on the existing local OSH
  twenty-failure baseline: populated `275 total / 24 passed / 20 failed /
  231 skipped`, clean-primary `275 total / 23 passed / 20 failed /
  232 skipped`, with GET-only TeamEngine request counts.
- Evidence directory:
  `ops/test-results/sprint-ets-74-prerequisite-declaration-fields-2026-08-03/`.
- The copied disposable run `artifact-manifest.sha256` verifies in place.
- `repo-evidence-manifest.sha256` verifies the repository evidence subset.
- Raze returned `APPROVE_WITH_CONCERNS 0.94` with `required_fixes: []`. The
  LOW pycompile-log exit-status concern was addressed by regenerating
  `pycompile.log` with `pycompile:0` and refreshing the repository evidence
  manifest.
- Implementation/evidence commit `b265dc6` is pushed to Botts `main`.

Sprint 73 mutation prerequisite readiness audit:

- Trigger: user instructed "Continue."
- Scope: `REQ-ETS-CLEANUP-024` operational readiness refinement only. No
  Create/Replace/Delete or Update lifecycle exact closure is claimed.
- Implementation: `scripts/mutation-readiness-audit.py` now separates direct
  declaration/method readiness from prerequisite-declaration readiness using
  `missingPrerequisiteConformance`, `prerequisiteConformancePresent`,
  `prerequisiteReadinessScope`,
  `declarationMethodAndPrerequisiteReadiness`, and root
  `classesWithDeclarationMethodAndPrerequisiteReadiness`.
- Python compile: PASS (`python-pycompile.log`).
- Python unit tests:
  `21 tests / 0 failures / 0 errors` (`python-unittest.log`).
- Formatter: BUILD SUCCESS (`spring-javaformat.log`).
- Full Docker Maven:
  `787 tests / 0 failures / 0 errors / 3 skipped`
  (`docker-maven-test.log`).
- Direct primary local OSH readiness audit:
  `47` candidates, `GET=1`, `OPTIONS=25`, `unsafeMethodsIssued=[]`,
  `classesWithDeclarationAndMethodReadiness=["1:/conf/create-replace-delete"]`,
  `classesWithDeclarationMethodAndPrerequisiteReadiness=[]`, and no exact
  promotions.
- Disposable local OSH mutable E2E:
  `sprint-ets-73-prereq-20260803T024709Z`. The populated IUT readiness audit
  reported `GET=1`, `OPTIONS=27`, `unsafeMethodsIssued=[]`, one direct
  declaration/method-ready class, zero prerequisite-ready classes, cleanup
  PASS, and primary-state isolation PASS.
- Full TeamEngine E2E remains honestly non-green on the existing local OSH
  twenty-failure baseline: populated `275 total / 24 passed / 20 failed /
  231 skipped`, clean-primary `275 total / 23 passed / 20 failed /
  232 skipped`, with GET-only TeamEngine request counts.
- Evidence directory:
  `ops/test-results/sprint-ets-73-mutation-prerequisite-readiness-2026-08-03/`.
- The copied disposable run `artifact-manifest.sha256` verifies in place, and
  `repo-evidence-manifest.sha256` verifies the repository evidence subset.
- Raze returned `APPROVE_WITH_CONCERNS 0.94` with `required_fixes: []`. The
  LOW concern is recorded: prerequisite-ready means declaration-only readiness,
  not prerequisite TestNG execution proof.
- Implementation and evidence commit `d110ccd` is pushed to Botts `main`.

Sprint 72 mutation candidate readiness audit:

- Trigger: user instructed "Continue."
- Scope: `REQ-ETS-CLEANUP-023` operational readiness evidence only. No
  Create/Replace/Delete or Update lifecycle exact closure is claimed.
- Implementation: `scripts/mutation-readiness-audit.py` reports all four
  remaining mutation-bound candidate classes and all 47 candidate procedures,
  issues only `GET` and `OPTIONS`, records class-level blockers, keeps
  `exactPromotionReady=false`, and serializes only `credentialSupplied`.
- Populated workflow integration:
  `scripts/local_osh_populated_e2e.py` archives
  `populated-mutation-readiness.json` after provisioning and before TeamEngine
  smoke, strips `SMOKE_AUTH_CREDENTIAL` from the audit subprocess, and uses a
  generated Docker DNS-safe OSH alias for TeamEngine IUT access.
- Python compile: PASS (`python-py-compile.txt`).
- Python unit tests:
  `20 tests / 0 failures / 0 errors` (`python-unittest.txt`).
- Formatter: BUILD SUCCESS (`formatter.txt`).
- Full Docker Maven:
  `787 tests / 0 failures / 0 errors / 3 skipped` (`full-maven.txt`).
- Direct primary local OSH readiness audit:
  `47` candidates, `GET=1`, `OPTIONS=25`, `unsafeMethodsIssued=[]`,
  `classesWithDeclarationAndMethodReadiness=["1:/conf/create-replace-delete"]`,
  and no exact promotions.
- Initial disposable run diagnostic: the long run id
  `sprint-ets-72-readiness-20260803T014708Z` exposed a TeamEngine
  `UnknownHostException` for an overlong generated OSH container hostname.
- Final disposable local OSH mutable E2E:
  `sprint-ets-72-readiness-r2-20260803T015933Z`. The populated IUT was reached
  through `http://ets-csapi-osh-435115cf728c162b:8081/sensorhub/api`; readiness
  audit reported `GET=1`, `OPTIONS=27`, `unsafeMethodsIssued=[]`; cleanup
  PASSed; primary-state diff is empty.
- Full TeamEngine E2E remains honestly non-green on the existing local OSH
  twenty-failure baseline: populated `275 total / 24 passed / 20 failed /
  231 skipped`, clean-primary `275 total / 23 passed / 20 failed /
  232 skipped`, with GET-only TeamEngine request counts.
- Evidence directory:
  `ops/test-results/sprint-ets-72-mutation-readiness-audit-2026-08-03/`.
- Initial Raze review returned `GAPS_FOUND 0.93` for ignored nested `.log`
  evidence. The gapfix makes those logs commit-reachable, verifies
  `local-osh-r2-artifacts/artifact-manifest.sha256`, and adds
  `repo-evidence-manifest.sha256` for the repository evidence subset.
- Raze focused recheck
  `.harness/evaluations/sprint-ets-72-adversarial-recheck.yaml` returned
  `APPROVE 0.97` with `required_fixes: []`.
- Implementation and evidence commit `be56c50` is pushed to Botts `main`.

Sprint 71 Part 2 Update released method surface:

- Trigger: user instructed "Continue."
- Scope: `REQ-ETS-PART2-008` method-surface fidelity only. Full positive
  PATCH lifecycle exact closure is not claimed.
- Test-first red: `focused-red-structural-after-format.txt` failed as expected
  because the historical class exposed multi-target child Update methods.
- Implementation: fourteen independent deployed Part 2 Update child-target
  methods now carry exactly one canonical requirement URI each; foundational
  declaration, prerequisite, condition-gate, readiness, unavailable-endpoint,
  mutation-safety, and schema-rejection honesty checks carry only the parent
  `/req/update` target.
- Coverage: update and audit both exit 0. `ops/ats-coverage-report.json`
  records `2:/conf/update = 14 total / 0 exact / 0 helper / 14 candidate /
  0 unmapped`; overall coverage is `240 total / 191 exact / 2 helper /
  47 candidate / 0 unmapped`.
- Formatter: BUILD SUCCESS (`formatter-after-impl.txt`).
- Focused structural verification:
  `86 tests / 0 failures / 0 errors / 0 skipped`
  (`focused-green-structural.txt`).
- Full Docker Maven:
  `787 tests / 0 failures / 0 errors / 3 skipped`
  (`full-maven.txt`).
- Disposable local OSH mutable E2E run:
  `sprint-ets-71-update-20260802T153902Z`. Provisioning PASSed with owned
  seeding writes, cleanup PASSed, and primary-state diff is empty.
- Full TeamEngine E2E remains honestly non-green on the existing local OSH
  twenty-failure baseline: populated `275 total / 24 passed / 20 failed /
  231 skipped`, clean-primary `275 total / 23 passed / 20 failed /
  232 skipped`. The failures are outside Sprint 71.
- In-scope Part 2 Update E2E evidence:
  `local-osh-part2-update-statuses.json` records all twenty-four deployed
  Update `@Test` methods as SKIP in the populated run; populated request
  counts are `GET=134`, clean-primary `GET=130`, and TeamEngine smoke issued
  zero POST/PUT/PATCH/DELETE.
- Evidence directory:
  `ops/test-results/sprint-ets-71-part2-update-method-surface-2026-08-02/`.
- `repo-evidence-manifest.sha256` verifies the archived repository evidence
  subset in place.
- Raze returned `APPROVE_WITH_CONCERNS 0.96`, with one LOW non-blocking
  source-run manifest hygiene note and `required_fixes: []`
  (`.harness/evaluations/sprint-ets-71-adversarial.yaml`).
- Implementation commit `081e09f` is pushed to Botts `main`.

Sprint 70 Part 2 Create/Replace/Delete released method surface:

- Trigger: user instructed "Continue."
- Scope: `REQ-ETS-PART2-007` method-surface and coverage gapfill only. Full
  positive lifecycle exact closure is not claimed.
- Test-first red: `focused-red-structural.txt` failed as expected because the
  historical class exposed multi-target coarse methods and left four Annex A.7
  procedures unmapped.
- Implementation: sixteen independent deployed Part 2 CRD child-target methods
  now carry exactly one canonical requirement URI each; foundational safety and
  readiness checks carry only the parent `/req/create-replace-delete` target.
- Coverage: update and audit both exit 0. `ops/ats-coverage-report.json`
  records `2:/conf/create-replace-delete = 16 total / 0 exact / 0 helper /
  16 candidate / 0 unmapped`; overall coverage is `240 total / 191 exact /
  2 helper / 47 candidate / 0 unmapped`.
- Formatter: BUILD SUCCESS (`formatter-after-assertion-cleanup.txt`).
- Focused structural verification:
  `86 tests / 0 failures / 0 errors / 0 skipped`
  (`focused-green-structural-final.txt`).
- Full Docker Maven:
  `786 tests / 0 failures / 0 errors / 3 skipped`
  (`full-maven-final.txt`).
- Disposable local OSH mutable E2E run:
  `sprint-ets-70-crd-20260802T052626Z`. Provisioning PASSed, cleanup PASSed,
  and primary-state diff is empty.
- Full TeamEngine E2E remains honestly non-green on the same twenty failures
  seen in the Sprint 69 baseline: populated `265 total / 24 passed /
  20 failed / 221 skipped`, clean-primary `265 total / 23 passed / 20 failed /
  222 skipped`. The failures are outside Sprint 70 and match the prior
  SensorML/Deployment/Procedure/Property failure set.
- In-scope Part 2 CRD E2E evidence:
  `local-osh-part2-crd-statuses.json` records all twenty-two CRD methods as
  SKIP in both populated and clean-primary runs, populated request counts
  `GET=134`, clean-primary `GET=130`, and zero POST/PUT/PATCH/DELETE.
- Evidence directory:
  `ops/test-results/sprint-ets-70-part2-crd-method-surface-2026-08-02/`.
- `repo-evidence-manifest.sha256` verifies the archived repository evidence
  subset in place.
- Raze final focused recheck returned `APPROVE 0.97`, closed
  `RAZE-ETS70-EVIDENCE-001`, and has `required_fixes: []`. Implementation
  commit `9ff24fb` is pushed to Botts `main`.

Sprint 69 Part 2 Binding disposable Command probe diagnostics:

- Trigger: user instructed using a disposable local OSH mutable IUT and
  reset/reseed as needed.
- Scope: `REQ-ETS-PART2-013` populated-IUT diagnostics only. Full Command
  binding closure is not claimed.
- Baseline disposable run:
  `ops/test-results/sprint-ets-69-mutable-local-osh-2026-08-02/summary.txt`.
  Provisioning PASS created System, Procedure, Deployment, SamplingFeature,
  DataStream, Observation, and ControlStream fixtures with `POST=7`, `GET=10`.
  Populated TeamEngine was `252/24/20/208`; Observation binding PASSed and
  Command binding SKIPped for missing associated Command evidence. Cleanup
  PASSed, primary diff was empty, and clean-primary TeamEngine was
  `252/23/20/209`.
- Harness change: `scripts/local-osh-populated-fixture.py` records optional
  Command probe diagnostics from `ops/local-osh-populated-fixtures.json`
  without making provisioning fail solely because Command evidence is absent.
- Post-change disposable run:
  `command-probe-summary.txt`, `command-probe-provisioning-evidence.json`,
  `command-probe-populated-smoke.xml`, and `command-probe-clean-primary-smoke.xml`.
  Provisioning PASS recorded `POST=8`, `GET=11`. Command probe diagnostics:
  `POST /controlstreams/040g/commands` timed out;
  `GET /controlstreams/040g/commands?limit=10` returned
  `application/json` with `itemCount=0`; no Command id was discovered.
- Post-change TeamEngine: populated `252/24/20/208` with Observation binding
  PASS and Command binding SKIP; clean-primary `252/23/20/209`; cleanup PASS;
  primary state diff empty. TeamEngine request counts remain read-only
  (`GET=134` populated, `GET=130` clean-primary).
- Verification: Python workflow regressions `14` tests pass
  (`python-unittest-after-manifest-fix.txt`), fixture JSON validates, formatter
  passes, and final Docker Maven passes `785 tests / 0 failures / 0 errors /
  3 skipped` (`full-maven-final.txt`). Baseline and command-probe SHA and
  provenance manifests are archived under `baseline-manifests/` and
  `command-probe-manifests/`, and copied run summaries reference those
  repository paths. Raze focused recheck returns `APPROVE_WITH_CONCERNS 0.96`
  with `RAZE-S69-001` resolved and `required_fixes: []`.
- Implementation commit `0c6e01a` is pushed to Botts `main`.

Sprint 68 Part 2 SWE Common Binary Encoding released ATS:

- Trigger: user instructed to make the practical metrics fix, then continue the
  project without stopping unless input is needed.
- Released source: OGC 23-002 `v1.0.0`, commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`, Part 2
  `/conf/swecommon-binary`.
- Implementation: exactly eight TestNG methods, one per released Annex A.12
  target; no standalone declaration/prerequisite/resource-condition helper
  procedures; exact `/conf/swecommon-binary` and SWE Common 3.0 Binary
  Encoding Rules setup gate; service-desc API-definition GET response-content
  advertisement plus strict `application/swe+binary` response media for read
  evidence; Connected Systems wrapper plus reusable SWE `recordSchema`
  validation; required `BinaryEncoding`; canonical Time/IssueTime mapping
  evidence across every retrieved schema; missing or noncanonical IssueTime
  evidence fails after Command Schema retrieval; scoped non-mutating
  service-desc OpenAPI mediatype-write checks requiring every advertised scoped
  POST/PUT operation to include exact `application/swe+binary`; no IUT
  mutation.
- Observation/Command encoding methods intentionally SKIP without parent
  schema, candidate body, and proven SWE Common Binary data-value validator
  evidence rather than passing from media or non-empty bytes alone.
- Test-first red: `focused-red.txt` exited non-zero before implementation
  because the exact Sprint 68 structural helper methods under test were
  missing on the Sprint 31 subset.
- Initial Raze found `RAZE-ETS68-FALSEPASS-001` for missing API-definition
  read advertisement evidence, `RAZE-ETS68-FALSEPASS-002` for first-schema-only
  mapping checks, `RAZE-ETS68-FALSESKIP-003` for absent IssueTime SKIP after
  Command Schema retrieval, and `RAZE-ETS68-DOC-001` for a cosmetic TestNG
  description typo. The gapfix addresses all four; focused Raze recheck
  returned `APPROVE_WITH_CONCERNS 0.94` with no blocking gaps.
- Final post-Raze-fix focused verification:
  `120 tests / 0 failures / 0 errors / 0 skipped`
  (`focused-final-after-raze-fixes.txt`).
- Released ATS coverage update exits 0; coverage audit exits 0. Current
  coverage is `240 total / 191 exact / 2 helper / 43 candidate / 4 unmapped`;
  Part 2 is `130 total / 100 exact / 0 helper / 26 candidate / 4 unmapped`;
  Part 2 SWE Common Binary is `8 exact / 0 candidate / 0 unmapped`.
- Formatter: BUILD SUCCESS (`formatter-apply.txt`).
- Full Docker Maven completed
  `785 tests / 0 failures / 0 errors / 3 skipped`
  (`full-maven-after-raze-fixes.txt`).
- Local OSH TeamEngine smoke exited honestly non-green at
  `252 total / 23 passed / 20 failed / 209 skipped`
  (`local-osh-smoke-after-raze-fixes.txt`).
  All eight Sprint 68 methods SKIP with reason:
  `http://www.opengis.net/spec/SWE/3.0/conf/binary-encoding-rules - /req/swecommon-binary prerequisite is missing; Sprint 68 SWE Common Binary procedures skip before SWE Common Binary resource endpoint access.`
  The 20 failures are existing local OSH SensorML/Deployment/Procedure/Property
  gaps outside Sprint 68.
- No-mutation oracle: `recognized_iut_request_logs=130`; request method counts
  are `GET=130`, zero POST/PUT/PATCH/DELETE.
- Evidence directory:
  `ops/test-results/sprint-ets-68-part2-swecommon-binary-2026-08-01/`.
- TeamEngine 6 runtime immutability verification passed for smoke image
  `sha256:47ddcfe1b7143004eba7b9fc88b5173fe1f6b7fa47616459a5d5efde14eee21e`.
- Implementation and evidence are pushed in `bb3935e`.

Sprint 67 Part 2 SWE Common Text Encoding released ATS:

- Trigger: user instructed to make the practical metrics fix, then continue the
  project without stopping unless input is needed.
- Released source: OGC 23-002 `v1.0.0`, commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`, Part 2
  `/conf/swecommon-text`.
- Implementation: exactly eight TestNG methods, one per released Annex A.11
  target; no standalone declaration/prerequisite/resource-condition helper
  procedures; exact `/conf/swecommon-text` and SWE Common 3.0 Text Encoding
  Rules setup gate; strict `application/swe+text` response media for read
  evidence; Connected Systems wrapper plus reusable SWE `recordSchema`
  validation; canonical Time/IssueTime mapping evidence; scoped non-mutating
  service-desc OpenAPI mediatype-write checks requiring every advertised
  scoped POST/PUT operation to include exact `application/swe+text`; no IUT
  mutation.
- Initial Raze found `RAZE-ETS67-FALSESKIP-001` because present but
  noncanonical issueTime/IssueTime evidence could SKIP rather than FAIL, and
  `RAZE-ETS67-DOC-001` because `testng.xml` still described the group as
  Sprint 30 subset work. The gapfix distinguishes absent IssueTime evidence
  from present noncanonical evidence, fails the present-invalid case, adds a
  structural regression, and updates the shipped suite comment.
- Observation/Command encoding methods intentionally SKIP without parent
  schema, candidate body, and proven SWE Common Text data-value validator
  evidence rather than passing from media or shape-only content.
- Test-first red: `focused-red.txt` exited non-zero before implementation
  because the exact Sprint 67 structural helper under test was missing.
- Post-Raze-fix focused verification:
  `115 tests / 0 failures / 0 errors / 0 skipped`
  (`focused-after-raze-fixes.txt`).
- Post-Raze-fix released ATS coverage update exits 0; coverage audit exits 0.
  Current
  coverage is `240 total / 183 exact / 2 helper / 50 candidate / 5 unmapped`;
  Part 2 is `130 total / 92 exact / 0 helper / 33 candidate / 5 unmapped`;
  Part 2 SWE Common Text is `8 exact / 0 candidate / 0 unmapped`.
- Formatter: BUILD SUCCESS (`formatter-after-raze-fixes.txt`).
- Post-Raze-fix full Docker Maven completed
  `775 tests / 0 failures / 0 errors / 3 skipped`
  (`full-maven-after-raze-fixes.txt`).
- Post-Raze-fix local OSH TeamEngine smoke exited honestly non-green at
  `254 total / 25 passed / 20 failed / 209 skipped`
  (`local-osh-smoke-after-raze-fixes.txt`).
  All eight Sprint 67 methods SKIP with reason:
  `http://www.opengis.net/spec/SWE/3.0/conf/text-encoding-rules - /req/swecommon-text prerequisite is missing; Sprint 67 SWE Common Text procedures skip before SWE Common Text resource endpoint access.`
  The 20 failures are existing local OSH SensorML/Deployment/Procedure/Property
  gaps outside Sprint 67.
- No-mutation oracle: `recognized_iut_request_logs=137`; request method counts
  are `GET=137`, zero POST/PUT/PATCH/DELETE.
- Evidence directory:
  `ops/test-results/sprint-ets-67-part2-swecommon-text-2026-08-01/`.
- TeamEngine 6 runtime immutability verification passed for smoke image
  `sha256:84423839aa6f4e5209b679a46ddf6a7cfbb3cbc3eb737ecce25d4e9d65167b0c`.
- Focused Raze recheck returned `APPROVE 0.96`; both findings are closed and
  `required_fixes: []`.
- Implementation commit `5f0a3f6` is pushed to Botts `main`.

S-ETS-66-02 Codex session metrics JSONL support:

- Trigger: user requested the practical fix for missing session JSONL metrics,
  then asked to continue the project without stopping unless input is needed.
- Scope: `scripts/session-metrics.py` now supports both Claude Code
  assistant-message JSONL and Codex rollout JSONL.
- Self-test: `python3 scripts/session-metrics.py --self-test` exits 0 with
  `SELF-TEST PASS` (`self-test.txt`).
- Current checkout extraction: `python3 scripts/session-metrics.py` exits 0,
  selects
  `rollout-2026-07-31T03-34-10-019fb718-4ae0-7601-a699-7adbbcec5d77.jsonl`
  as `Format: codex`, and reports non-zero totals
  (`current-codex-session.txt`).
- Explicit sub-agent extraction:
  `python3 scripts/session-metrics.py /home/nh/.codex/sessions/2026/08/01/rollout-2026-08-01T07-51-48-019fbd2a-8549-7671-adf3-66a7430667e2.jsonl`
  exits 0 as `Format: codex` (`subagent-codex-session.txt`).
- Initial Raze found a high sub-agent auto-discovery classification gap and a
  low premature epic completion label. Post-gapfix evidence:
  `self-test-after-raze-fix.txt`,
  `current-codex-session-after-raze-fix.txt`, and
  `subagent-classification-after-raze-fix.txt`.
- Evidence directory:
  `ops/test-results/s-ets-66-02-codex-session-metrics-2026-08-01/`.
- Focused Raze recheck returned `APPROVE 0.96`; both findings are closed and
  `required_fixes: []`.
- Implementation and evidence are committed and pushed in `6e6d4f3`; this
  reconciliation records the post-push completion state.

Sprint 66 Part 2 SWE Common JSON Encoding released ATS:

- Trigger: user instructed "Continue with Part 2."
- Released source: OGC 23-002 `v1.0.0`, commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`, Part 2
  `/conf/swecommon-json`.
- Implementation: exactly eight TestNG methods, one per released Annex A.10
  target; no standalone declaration/prerequisite/resource-condition helper
  procedures; exact `/conf/swecommon-json` and SWE Common 3.0 JSON Encoding
  Rules setup gate; strict `application/swe+json` response media for read
  evidence; Connected Systems wrapper plus reusable SWE `recordSchema`
  validation; canonical Time/IssueTime mapping evidence; scoped
  non-mutating service-desc OpenAPI mediatype-write checks requiring every
  advertised scoped POST/PUT operation to include exact `application/swe+json`;
  no IUT mutation.
- Observation/Command encoding methods intentionally SKIP without parent
  schema, candidate body, and proven SWE Common JSON data-value validator
  evidence rather than passing from shape-only content.
- Focused verification:
  `114 tests / 0 failures / 0 errors / 0 skipped` (`focused-maven.txt`).
- Released ATS coverage update exits 0; coverage audit exits 0. Current
  coverage is `240 total / 175 exact / 2 helper / 57 candidate / 6 unmapped`;
  Part 2 is `130 total / 84 exact / 0 helper / 40 candidate / 6 unmapped`;
  Part 2 SWE Common JSON is `8 exact / 0 candidate / 0 unmapped`.
- Formatter: BUILD SUCCESS (`formatter.txt`).
- Full Docker Maven initially failed before tests on a transient Maven Central
  `tagsoup` transfer reset (`full-maven.txt`); retry passed
  `770 tests / 0 failures / 0 errors / 3 skipped` (`full-maven-retry.txt`).
- Local OSH TeamEngine smoke exited honestly non-green at
  `256 total / 27 passed / 20 failed / 209 skipped` (`local-osh-smoke.txt`).
  All eight Sprint 66 methods SKIP with reason:
  `http://www.opengis.net/spec/SWE/3.0/conf/json-encoding-rules - /req/swecommon-json prerequisite is missing; Sprint 66 SWE Common JSON procedures skip before SWE Common JSON resource endpoint access.`
  The 20 failures are existing local OSH SensorML/Deployment/Procedure/Property
  gaps outside Sprint 66.
- No-mutation oracle: `recognized_iut_request_logs=144`; request method counts
  are `GET=144`, zero POST/PUT/PATCH/DELETE.
- Evidence directory:
  `ops/test-results/sprint-ets-66-part2-swecommon-json-2026-08-01/`.
- TeamEngine 6 runtime immutability verification passed for smoke image
  `sha256:5e0b95d12d7639fe56d22e57aab875a45fc06227eed823f37b50ac7e5efa4b00`.
- Raze initial review found one low stale Sprint 29 suite-wiring comment gap;
  the gapfix updated `testng.xml` and `VerifyTestNGSuiteDependency`, formatter
  passed, and focused retry passed `76 tests / 0 failures / 0 errors /
  0 skipped`.
- Final Raze recheck returned `APPROVE 0.96`; `RAZE-ETS66-DOC-001` is closed
  with `required_fixes: []`.
- Implementation commit `6e98ac9` is pushed to Botts `main`; this
  reconciliation records the post-push completion state.

Sprint 65 Part 2 JSON Encoding released ATS:

- Trigger: user instructed "Continue until you need my input."
- Released source: OGC 23-002 `v1.0.0`, commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`, Part 2 `/conf/json`.
- Implementation: exactly fourteen TestNG methods, one per released Annex A.9
  target; no standalone declaration/prerequisite/resource-condition helper
  procedures; exact `/conf/json` and SWE JSON record-components setup gate;
  strict `application/json` response media; bounded all-page root and nested
  collection validation; scoped non-mutating service-desc OpenAPI
  mediatype-write checks requiring every advertised scoped POST/PUT operation
  to include exact `application/json`; no IUT mutation.
- Raze initial review found three exactness gaps: nested endpoint repetitions
  were optional/partial, mediatype-write accepted unrelated POST/PUT operations,
  and DataStream/ControlStream schema procedures inspected only one candidate
  page. The gapfix validates the released nested endpoints, scopes write
  advertisements to Part 2 resource endpoint templates, requires every scoped
  POST/PUT operation to advertise JSON, and traverses bounded same-origin
  pagination for collection resources.
- Focused corrected verification after the Raze recheck gapfix:
  `88 tests / 0 failures / 0 errors / 0 skipped`
  (`focused-after-raze-recheck-fix.txt`).
- Released ATS coverage update exits 0; coverage audit exits 0 with
  `23/0/0/0`. Current coverage is `240 total / 167 exact / 2 helper /
  64 candidate / 7 unmapped`; Part 2 is `130 total / 76 exact / 0 helper /
  47 candidate / 7 unmapped`; Part 2 JSON is
  `14 exact / 0 candidate / 0 unmapped`
  (`coverage-summary-after-raze-recheck-fix.txt`).
- Formatter: BUILD SUCCESS (`formatter-after-raze-recheck-fix.txt`).
- Full Docker Maven: `766 tests / 0 failures / 0 errors / 3 skipped`
  (`full-maven-after-raze-recheck-fix.txt`).
- Local OSH TeamEngine smoke exited honestly non-green at
  `258 total / 29 passed / 20 failed / 209 skipped`
  (`local-osh-smoke-after-raze-recheck-fix.txt`). All fourteen Sprint 65 Part 2 JSON
  methods SKIP with reason:
  `http://www.opengis.net/spec/SWE/3.0/conf/json-record-components - /req/json prerequisite is missing; Sprint 65 JSON procedures skip before JSON resource endpoint access.`
  The 20 failures are existing local OSH SensorML/Deployment/Procedure/Property
  gaps outside Sprint 65.
- No-mutation oracle: `recognized_iut_request_logs=151`; request method counts
  are `GET=151`, zero POST/PUT/PATCH/DELETE.
- Evidence directory:
  `ops/test-results/sprint-ets-65-part2-json-2026-08-01/`.
- TeamEngine 6 runtime immutability verification passed for smoke image
  `sha256:31e6b4eac77f8455638c94160c788f7156689566711c668ea266194837434637`.
- Final Raze recheck approved the mediatype-write mixed-operation gapfix:
  `APPROVE 0.96`, both findings closed, `required_fixes: []`.
- Implementation commit `1acfdfa` is pushed to Botts `main`.

Sprint 64 Part 2 Advanced Filtering released ATS:

- Trigger: user instructed "Continue."
- Released source: OGC 23-002 `v1.0.0`, commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`, Part 2
  `/conf/advanced-filtering`.
- Implementation: exactly eighteen TestNG methods, one per released Annex A.6
  target; direct `part2advancedfiltering -> part2apicommon advancedfiltering`
  dependency; released DataStream, Observation, ControlStream, Command,
  CommandStatus, and SystemEvent filtered-resource validation; seed-derived
  predicate evidence for PASS; honest SKIP behavior for missing declaration,
  incomplete prerequisites, unavailable endpoints, or absent positive seed
  evidence; no mutation or streaming subscription.
- Focused test-first red: formatter initially reported new-code formatting
  violations; after formatting, the focused structural run reproduced the
  Sprint 25 historical gap with stale TestNG dependency, nine methods instead
  of eighteen, standalone declaration/prerequisite helpers, and missing Sprint
  64 scenario trace. Final corrected focused verification: `83/0/0/0`.
- Released ATS coverage update: `1/0/0/0`; coverage audit: `23/0/0/0`.
  Overall coverage is `240 total / 153 exact / 2 helper / 77 candidate /
  8 unmapped`; Part 2 is `130 total / 62 exact / 0 helper / 60 candidate /
  8 unmapped`; Part 2 Advanced Filtering is
  `18 exact / 0 candidate / 0 unmapped`.
- Formatter: BUILD SUCCESS (`formatter-after-unit-fix.txt`).
- Initial Raze review found `RAZE-ETS64-FALSEPASS-001`: generic SystemEvent
  `type` could be used as event-type evidence. The gapfix removes that fallback
  and adds a regression proving `type=SystemEvent` plus actual `definition`
  uses `definition` as the event type. Post-gapfix focused Docker Maven:
  `83/0/0/0` (`focused-after-raze-falsepass-fix.txt`).
- Focused Raze recheck: `APPROVE 0.97`,
  `RAZE-ETS64-FALSEPASS-001` closed, `required_fixes: []`.
- Post-gapfix full Docker Maven: `762 tests / 0 failures / 0 errors /
  3 skipped` (`full-maven-after-raze-falsepass-fix.txt`).
- Post-gapfix local OSH TeamEngine smoke exited honestly non-green at
  `260 total / 36 passed / 21 failed / 203 skipped`. All eighteen Sprint 64
  Advanced Filtering methods SKIP with reason:
  `Part 2 Advanced Filtering setup skipped before IUT access because prerequisite method indirectPropertyFiltersAreTransitive skipped.`
  The 21 failures are existing local OSH SensorML/Deployment/Procedure/
  Property/Sampling Feature gaps outside Sprint 64.
- No-mutation oracle: `recognized_iut_request_logs=181`; request method counts
  are `GET=186`, zero POST/PUT/PATCH/DELETE.
- Current evidence directory:
  `ops/test-results/sprint-ets-64-part2-advanced-filtering-2026-08-01/`;
  durable tracked files include formatter/focused/coverage/full-Maven logs and
  post-gapfix `local-osh-smoke-after-raze-falsepass-fix.txt`,
  `s-ets-01-03-teamengine-smoke-after-raze-falsepass-fix-2026-08-01.xml`,
  `s-ets-01-03-teamengine-container-after-raze-falsepass-fix-2026-08-01.txt`,
  `advanced-filtering-smoke-summary-after-raze-falsepass-fix.txt`,
  `no-mutation-oracle-after-raze-falsepass-fix.txt`,
  `request-method-counts-after-raze-falsepass-fix.txt`,
  `teamengine-runtime-immutability-after-raze-falsepass-fix.txt`, and
  `local-osh-provenance.txt`.
- TeamEngine 6 runtime immutability verification passed for post-gapfix smoke
  image
  `sha256:fe3c3f03d1ffbc0b5e657a23f3e079c3983116203ec2457e6bbaf83f58f63f28`;
  local OSH is clean at checkout `4c87a65`, zero commits ahead, and mounted
  read-only at `/opt/osh`.
- No OSH or TeamEngine source/binary changes, hosted CI, streaming
  subscriptions, or IUT mutation were introduced.
- Implementation commit `880c347` is pushed to Botts `main`.

Sprint 63 Part 2 System Events released ATS:

- Trigger: user instructed "Continue."
- Released source: OGC 23-002 `v1.0.0`, commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`, Part 2
  `/conf/system-event`.
- Implementation: exactly five TestNG methods, one per released System Events
  Annex A.5 target; direct
  `part2systemevent -> part2apicommon systemfeatures` dependency; literal
  Annex A.5 copy text handling; released SystemEvent collection/item schema
  validation; exact `itemType=ControlStream` and `itemType=SystemEvent`
  collection traversal; advertised same-origin canonical-link evidence; no
  mutation or streaming subscription.
- Focused test-first red: formatter initially reported new-test formatting
  violations; after formatting, the focused structural run failed at compile
  time on missing Sprint 63 helper/support methods. Final corrected focused
  verification: `84/0/0/0`. Logs:
  `focused-red-reproduction.txt`, `formatter.txt`, and
  `focused-corrected.txt`.
- Released ATS coverage update: `1/0/0/0`; coverage audit: `23/0/0/0`.
  Overall coverage is `240 total / 135 exact / 2 helper / 90 candidate /
  13 unmapped`; Part 2 is `130 total / 44 exact / 0 helper / 73 candidate /
  13 unmapped`; Part 2 System Events is
  `5 exact / 0 candidate / 0 unmapped`.
- Formatter: BUILD SUCCESS (`formatter.txt`).
- Full Docker Maven: `763 tests / 0 failures / 0 errors / 3 skipped`
  (`full-maven.txt`).
- Local OSH TeamEngine smoke exited honestly non-green at
  `251 total / 36 passed / 21 failed / 194 skipped`. All five Sprint 63 System
  Events methods SKIP with reason:
  `Part 2 System Events setup skipped before IUT access because prerequisite method canonicalSystemsEndpointIsValid skipped.`
  The 21 failures are existing local OSH SensorML/Deployment/Procedure/
  Property/Sampling Feature gaps outside Sprint 63.
- No-mutation oracle: `recognized_iut_request_logs=182`; request method counts
  are `GET=182`, zero POST/PUT/PATCH/DELETE.
- Final evidence:
  `ops/test-results/sprint-ets-63-part2-system-event-2026-08-01/`; durable
  tracked files are `formatter.txt`, `focused-red-reproduction.txt`,
  `focused-corrected.txt`, `coverage-update.txt`, `coverage-audit.txt`,
  `full-maven.txt`, `local-osh-teamengine-smoke.xml`,
  `local-osh-teamengine-container-log.txt`, `smoke-console.txt`,
  `no-mutation-oracle.txt`, `iut-request-method-counts.txt`, and
  `teamengine6-runtime-verification.txt`.
- TeamEngine 6 runtime immutability verification passed for final smoke image
  `sha256:fe0ae15f3f088bddae114aa7780bd507900f37c543b8294b2d4366a53b287c6e`;
  local OSH is clean at checkout `4c87a65` and mounted read-only at `/opt/osh`.
- No OSH or TeamEngine source/binary changes, hosted CI, streaming
  subscriptions, or IUT mutation were introduced.
- Raze initial review found documentation/evidence bookkeeping gaps only. The
  first focused recheck closed the completion-wording gap and found ignored
  `.log` Maven artifacts; the final focused recheck approved the tracked
  `.txt` evidence at `APPROVE 0.97` with `required_fixes: []`.
- Implementation commit `8d0c4fa` is pushed to Botts `main`.

Sprint 62 Part 2 Command Feasibility released ATS:

- Trigger: user instructed "Continue."
- Released source: OGC 23-002 `v1.0.0`, commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`, Part 2
  `/conf/feasibility`.
- Implementation: exactly five TestNG methods, one per released Command
  Feasibility Annex A.4 target; direct
  `part2feasibility -> part2controlstream` dependency; literal Annex A.4 copy
  text handling; released Command, CommandStatus, and CommandResult schema
  validation; exact `itemType=Command` and `itemType=Feasibility` collection
  traversal; advertised canonical-link evidence; no mutation.
- Focused test-first red: formatter initially reported new-test formatting
  violations; after formatting, the focused structural run failed at compile
  time on missing `releasedControlStreamCommandPath(String)` and
  `isCollectionWithItemType(Map,String)` helpers. Final corrected focused
  verification: `83/0/0/0`.
- Released ATS coverage update: `1/0/0/0`; coverage audit: `23/0/0/0`.
  Overall coverage is `240 total / 130 exact / 2 helper / 94 candidate /
  14 unmapped`; Part 2 is `130 total / 39 exact / 0 helper / 77 candidate /
  14 unmapped`; Part 2 Feasibility is
  `5 exact / 0 candidate / 0 unmapped`.
- Formatter: BUILD SUCCESS.
- Full Docker Maven: `760 tests / 0 failures / 0 errors / 3 skipped`.
- Local OSH TeamEngine smoke exited honestly non-green at
  `252 total / 36 passed / 21 failed / 195 skipped`. All five Sprint 62
  Feasibility methods SKIP with reason:
  `Part 2 Feasibility setup skipped before IUT access because prerequisite configuration fetchPart2ControlStreamInputs skipped.`
  The 21 failures are existing local OSH SensorML/Property/Sampling Feature/
  Procedure/Deployment gaps outside Sprint 62.
- No-mutation oracle: `recognized_iut_request_logs=184`; request method counts
  are `GET=184`, zero POST/PUT/PATCH/DELETE.
- Final evidence:
  `ops/test-results/sprint-ets-62-part2-feasibility-2026-07-31/`; durable
  tracked files are `local-osh-teamengine-smoke.xml`,
  `local-osh-teamengine-container-log.txt`, `no-mutation-oracle.txt`, and
  `iut-request-method-counts.txt`.
- TeamEngine 6 runtime immutability verification passed for final smoke image
  `sha256:f50858b23f3a8f3d73a337a39a89dba624ede57d2ad3497b1068235b8033f4d3`;
  local OSH is clean at checkout `4c87a65` and mounted read-only at `/opt/osh`.
- No OSH or TeamEngine source/binary changes, hosted CI, or IUT mutation were
  introduced.
- Raze: initial `GAPS_FOUND 0.93` for premature story completion wording,
  stale TestNG dependency comments, and stale traceability header provenance;
  focused recheck returned `APPROVE 0.96` with no required fixes.

Sprint 61 Part 2 Control Streams and Commands released ATS:

- Trigger: user instructed "Continue."
- Released source: OGC 23-002 `v1.0.0`, commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`, Part 2
  `/conf/controlstream`.
- Implementation: exactly eighteen TestNG methods, one per released Control
  Streams and Commands Annex A.3 target; direct
  `part2controlstream -> part2apicommon` dependency; immutable setup; released
  ControlStream, Command, CommandStatus, and CommandResult schema validation;
  exact `itemType` collection traversal; advertised canonical-link evidence;
  all-format `cmdFormat` schema-op checks; condition-gated Sampling Feature,
  FOI, System, Deployment, and nested Command association checks; no mutation.
- Focused test-first red: `88 tests / 3 failures / 6 errors / 0 skipped`.
  Final corrected focused verification: `88/0/0/0`.
- Released ATS coverage update: `1/0/0/0`; coverage audit: `23/0/0/0`.
  Overall coverage is `240 total / 125 exact / 2 helper / 99 candidate /
  14 unmapped`; Part 2 is `130 total / 34 exact / 0 helper / 82 candidate /
  14 unmapped`; Part 2 ControlStream is
  `18 exact / 0 candidate / 0 unmapped`.
- Formatter: BUILD SUCCESS.
- Full Docker Maven: `758 tests / 0 failures / 0 errors / 3 skipped`.
- Local OSH TeamEngine smoke exited nonzero honestly at
  `254 total / 36 passed / 21 failed / 197 skipped`. Part 2 API Common setup
  and all eighteen ControlStream methods SKIP because local OSH does not
  declare the Part 2 `/conf/api-common` prerequisite.
- No-mutation oracle: `recognized_iut_request_logs=186`; request-line method
  count is `GET=192`, zero POST/PUT/PATCH/DELETE/OPTIONS.
- Final evidence:
  `ops/test-results/sprint-ets-61-part2-controlstream-final-2026-07-31/`;
  durable tracked files are `s-ets-01-03-teamengine-smoke-2026-07-31.xml`,
  `s-ets-01-03-teamengine-container-2026-07-31.txt`,
  `no-mutation-oracle.txt`, and `request-method-counts.txt`.
- No OSH or TeamEngine source/binary changes, hosted CI, or IUT mutation were
  introduced.
- Raze: initial `GAPS_FOUND 0.93` for ignored `.log` evidence and premature
  completion/push wording; gapfix added tracked `.txt` evidence and reconciled
  docs; recheck returned `APPROVE 0.96` with no required fixes.

Sprint 60 Part 2 Datastreams and Observations released ATS:

- Trigger: user instructed "Do Sprint 60."
- Released source: OGC 23-002 `v1.0.0`, commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`, Part 2
  `/conf/datastream`.
- Implementation: exactly fourteen TestNG methods, one per released
  Datastreams and Observations Annex A.2 target; direct
  `part2datastream -> part2apicommon` dependency; immutable setup;
  all-resource canonical traversal, exact `itemType` collection traversal,
  dereferenced canonical-link equivalence, endpoint/collection JSON schema
  validation, all-format schema-op checks, generic GeoJSON validation for
  FeatureOfInterest responses including `application/json` pages, mixed
  Sampling Feature unsupported-media SKIPs, and exact condition gates before
  nested Sampling Feature, FeatureOfInterest, System, or Deployment access; no
  synthesized mutation.
- Focused test-first red: `85 tests / 3 failures / 2 errors / 0 skipped`.
  Final corrected focused verification: `96/0/0/0`.
- Released ATS coverage audit: `23/0/0/0`; overall coverage is
  `240 total / 107 exact / 2 helper / 107 candidate / 24 unmapped`; Part 2 is
  `130 total / 16 exact / 0 helper / 90 candidate / 24 unmapped`; Part 2
  Datastream is `14 exact / 0 candidate / 0 unmapped`.
- Formatter: BUILD SUCCESS.
- Full Docker Maven: `750 tests / 0 failures / 0 errors / 3 skipped`.
- Local OSH TeamEngine smoke exited nonzero honestly at
  `247 total / 38 passed / 21 failed / 188 skipped`. Datastream setup and all
  fourteen Datastream methods SKIP because local OSH does not declare the Part
  2 `/conf/api-common` prerequisite.
- No-mutation oracle: `recognized_iut_request_logs=189`; request-line method
  count is `GET=194`, zero POST/PUT/PATCH/DELETE.
- Final evidence:
  `ops/test-results/sprint-ets-60-part2-datastream-final-raze-2026-07-31/`.
- No OSH or TeamEngine source/binary changes, hosted CI, or IUT mutation were
  introduced.
- Raze: initial `GAPS_FOUND 0.94` and subsequent FOI, condition-gate, mixed
  Sampling Feature, and FOI `application/json` findings have been addressed;
  final recheck returned `PASS` with high confidence and no required fixes.

Sprint 59 Part 2 API Common released ATS:

- Trigger: user added the deploy key, instructed pushing, then continuing with
  the next item.
- Released source: OGC 23-002 `v1.0.0`, commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`, Part 2
  `/conf/api-common`.
- Implementation: exactly two TestNG methods,
  `/conf/api-common/resources` and `/conf/api-common/resource-collection`;
  direct `part2apicommon -> part1apicommon` dependency; per-procedure exact
  `/conf/api-common` declaration honesty; same-origin advertised collection
  probing; no synthesized endpoints or mutation.
- Focused test-first red: `88 tests / 6 failures / 1 error / 0 skipped`.
  Corrected focused verification: `88/0/0/0`.
- Released ATS coverage audit: `23/0/0/0`; overall coverage is
  `240 total / 93 exact / 2 helper / 116 candidate / 29 unmapped`; Part 2 API
  Common is `2 exact / 0 candidate / 0 unmapped`.
- Full Docker Maven: `735 tests / 0 failures / 0 errors / 3 skipped`.
- Local OSH TeamEngine smoke exited nonzero honestly at
  `244 total / 41 passed / 21 failed / 182 skipped`. `fetchPart2ApiCommonInputs`
  passed, and both Part 2 API Common methods SKIP because local OSH does not
  declare Part 2 `/conf/api-common`.
- No-mutation oracle: `recognized_iut_request_logs=194`, zero
  POST/PUT/PATCH/DELETE.
- Evidence:
  `ops/test-results/sprint-ets-59-part2-api-common-2026-07-31/`.
- No OSH or TeamEngine source/binary changes, hosted CI, or IUT mutation were
  introduced.
- Raze: `APPROVE_WITH_CONCERNS 0.95`, no required fixes. Low concern only:
  raw Maven stdout logs are not archived beside the E2E artifacts, but the
  exact totals are recorded consistently in sprint documentation.

Sprint 58 Part 1 SensorML direct ATS:

- Exact source candidate:
  `a593953d8d79d977649db3077696148e90ffb44a`; E2E image
  `sha256:c0227ab3ef9d67a27d8d22a119979eda7615df10bbbc43c9e50a52daffdff093`.
- Released source: OGC 23-001 `v1.0.0`, commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`.
- Implementation: fifteen independent direct procedures, direct Part 1 API
  Common dependency, bounded read-only OpenAPI JSON/YAML inspection, complete
  canonical schema/id/mapping/class/relation validation, and the provisional
  backend-neutral SensorML validator adapter.
- Focused SensorML verification: `37 tests / 0 failures / 0 errors / 0
  skipped`. Exact detached Docker Maven: `729 / 0 / 0 / 3`, BUILD SUCCESS.
- Schema parity: 8 entry schemas and 63 transitive schemas, zero graph or
  semantic mismatches.
- Coverage gate: `23/0/0/0`; SensorML is `15 exact / 0 candidate /
  0 unmapped`. Overall coverage is
  `240 total / 91 exact / 2 helper / 118 candidate / 29 unmapped`.
- Exact-image runtime passes the SWE Common adapter, SensorML adapter, OpenAPI
  3.1 parser, external-reference security probe, tuple guard, dependency
  parity, TeamEngine-owned base immutability, and confidential-history hygiene.
- Exact unmodified-local-OSH TeamEngine exits nonzero honestly at
  `246 total / 41 passed / 21 failed / 184 skipped`. All fifteen SensorML
  methods execute. `mediatype-write` passes; fourteen fail because OSH returns
  generic `application/json` for requested SensorML collections or its
  advertised OpenAPI definition lacks complete read-media evidence.
- Remote-control recovery rerun on 2026-07-31 reached the same deployed
  TeamEngine/local OSH path and exited nonzero at `246/41/21/184`, confirming
  the current dirty docs-only recovery notes did not hide the known local OSH
  SensorML limitation. The captured container log has 199 total request lines:
  194 local-OSH IUT GETs and 5 external `opengeospatial.github.io` GETs, with
  zero POST/PUT/PATCH/DELETE. Artifacts:
  `/tmp/ets-ogcapi-connectedsystems10-remote-recovery-results/`.
- No-mutation oracle: `recognized_iut_request_logs=194`, zero writes.
  Artifact hygiene: PASS, zero credential leaks and zero IUT writes.
- Credential integration: zero literal hits. Wire E2E: zero unmasked artifact
  hits, 30 masked container hits, and 30 intact synthetic stub receipts.
- Dependency sabotage: `246/2/10/234`; API Common setup/tests and all fifteen
  SensorML methods dependency-skip after Core failure.
- Final implementation Raze: `APPROVED`, confidence `0.99`, no required fixes.
  Reconciliation Raze identified two stale baseline labels; focused recheck is
  `APPROVED 0.99` after both were closed. Prior DNS-rebinding,
  blocking-deadline, and deployed-security-probe findings are closed.
- Evidence:
  `ops/test-results/sprint-ets-58-part1-sensorml-final-a593953-2026-07-31/`.
- No OSH or TeamEngine source/binary changes, hosted CI, executable SensorML
  suite-jar dependency, or default IUT mutation were introduced.

Sprint 57 Part 1 Update direct ATS:

- Exact replacement candidate:
  `40cc7039da26a39424f1ffa7626b7b6926a50f0a`; image
  `sha256:8184ad80b160e0854afcf22d5fa996de835bd886c31930bae150a1bb4cb7ee9d`.
- Released sources: OGC 23-001 `v1.0.0` commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`; inherited Features Part 4
  `part4-1.0.0-draft.1` commit
  `ea42aa1de6d8cbb53c526f41e1f66c1887fe71d4`.
- Implementation: five independent procedures with direct causal API Common
  dependency, exact Connected Systems and `ogcapi-4` declarations, canonical
  and custom paths, PATCH negotiation, completed partial-update evidence,
  bounded queued polling, and identity-safe cleanup.
- Earlier test-first remediation: behavioral red `28/6/0/0`; corrected
  controlled HTTP `28/0/0/0`. Final-Raze replacement: behavioral red
  `2/2/0/0`, corrected focused `2/0/0/0`, and complete Update `30/0/0/0`.
  Exact detached full Docker Maven:
  `687 tests / 0 failures / 0 errors / 3 skipped`, BUILD SUCCESS.
- Coverage: `240 total / 76 exact / 2 helper / 130 candidate / 32 unmapped`;
  Update is `5 / 0 / 0 / 5 / 0`.
- Released-source parity, ATS/URI/hygiene/jar-guard self-tests,
  exact-image TeamEngine runtime, deployed SWE Common adapter, added-jar
  collision guard, TeamEngine-owned base immutability, and build-context
  hygiene: PASS.
- Dependency sabotage: `244 total / 2 passed / 10 intentionally failed /
  232 skipped`; parser verdict PASS for API Common setup/tests, and the
  archived XML confirms all Update procedures also cascade to SKIP.
- Credential integration: zero literal credential hits. Credential wire E2E:
  zero unmasked artifact hits, 31 masked container hits, and 31 intact
  synthetic wire transmissions.
- Exact isolated populated OSH: `244 total / 54 passed / 35 failed /
  155 skipped`, GET=196.
- Exact clean primary OSH: `244 total / 40 passed / 7 failed / 197 skipped`,
  GET=167.
- Both OSH conformance runs remain non-green and the workflow exits `1`.
  Provisioning and owned cleanup pass, primary state is unchanged, and
  artifact hygiene finds zero writes and zero credential leaks.
- Part 1 API Common is `4 PASS / 1 SKIP`; missing positive datetime evidence
  causal-SKIPs all five Update methods before POST/PATCH/DELETE. This is honest
  real-protocol dependency evidence, not positive Update evidence.
- Final Raze `GAPS_FOUND 0.98` superseded `c4b6030`: canonical Sampling
  Feature acquisition incorrectly used the optional root collection, and
  ambiguous cleanup could stop at canonical visibility before a delayed
  custom occurrence appeared.
- Requirement-linked tests reproduced both defects at `2/2/0/0`. The
  replacement owns a parent System, creates the child at
  `/systems/{id}/samplingFeatures`, and polls both identity views through the
  bounded cleanup deadline. Focused tests pass `2/0/0/0`, complete Update
  passes `30/0/0/0`, and full precommit Docker Maven passes `687/0/0/3`.
  Replacement exact-image and local-OSH gates pass as recorded above.
- The five mappings stay candidate and Sprint 57 stays in progress until a
  conforming dedicated mutable IUT provides positive completed PATCH evidence.
  No OSH or TeamEngine source/binary changes were made.
- Fresh Raze returns `APPROVE_WITH_CONCERNS` with high confidence, closes both
  prior HIGH findings, and finds no implementation defect. Focused Raze recheck
  returns `APPROVE_WITH_CONCERNS` at confidence `0.98`, closes the sole MEDIUM
  exact-evidence reconciliation concern, and requires no fixes. Positive PATCH
  evidence remains externally blocked, so all five mappings remain candidate.
- Exact replacement evidence:
  `ops/test-results/sprint-ets-57-part1-update-final-40cc703-2026-07-30/`.
- Superseded `c4b6030` evidence:
  `ops/test-results/sprint-ets-57-part1-update-final-c4b6030-2026-07-30/`.

Sprint 56 Part 1 Create/Replace/Delete direct ATS:

- Released sources: OGC 23-001 `v1.0.0` commit
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`; inherited Features Part 4
  `part4-1.0.0-draft.1` commit
  `ea42aa1de6d8cbb53c526f41e1f66c1887fe71d4`.
- Implementation: twelve independent procedures with causal API Common
  dependency, exact `ogcapi-4` inheritance, schema-validated writes,
  identity-safe cleanup, cascade graph preconditions, root canonical nested
  checks, and complete custom-collection lifecycle assertions.
- Raze returned `GAPS_FOUND 0.99` on `0023d5b`, superseding it for uncapped
  polling probes/sleeps, hidden cleanup failure, incomplete queued compound
  postconditions, late URI-list cleanup registration, and stale CP-016 status
  semantics.
- Seven requirement-linked remediations pass direct controlled HTTP
  `25/0/0/0`; the initial red run was `25/6/0/0`, with the interruption case
  tightened during implementation.
- Coverage inventory: overall `240 total / 76 exact / 2 helper / 126 candidate
  / 36 unmapped`; Create/Replace/Delete `12 / 0 / 0 / 12 / 0`.
- Superseded exact remediation candidate
  `1a6c5ec30f76e120a0e2cd676f472699141213ca` passes focused `42/0/0/0` and
  full Maven `644/0/0/3`. Its image is
  `sha256:6939ef2ea40ff42328d4ff972b691dd4ba1c6a59855fe913d175b09f9555c1da`
  with `Build-Revision: 1a6c5ec30f`; released-source, schema-parity, runtime,
  immutable-base, dependency, credential, and hygiene gates pass.
- Superseded candidate `f6e3587` focused Maven was `28/0/0/0`; exact full Maven was
  `630/0/0/3`. Image `sha256:f498e3d0...e88a2` passes runtime and immutable
  TeamEngine base verification.
- Exact candidate local OSH E2E: populated `244 total / 54 passed / 35 failed /
  155 skipped`; clean primary `244 / 40 / 7 / 197`. Provisioning and cleanup
  passed, primary state was unchanged, and TeamEngine issued zero IUT writes.
- Part 1 API Common is `4 PASS / 1 SKIP`; causal inheritance therefore makes
  all twelve procedures dependency-SKIP before declaration checks, although
  OSH advertises Part 1 `/conf/create-replace-delete`. OSH omits exact
  Connected Systems API Common and inherited `ogcapi-4` declarations.
- Core sabotage is `244/2/10/232` and all twelve CRD methods SKIP. Credential
  wire E2E is zero unmasked hits, 32 masked events, and 32 intact synthetic
  transmissions. Hygiene is 365 IUT GETs, zero writes, and zero leaks.
- Raze returned `GAPS_FOUND 0.98` on `f6e3587`; its exact evidence is retained
  under `sprint-ets-56-part1-create-replace-delete-f6e3587-superseded-2026-07-30`.
- Superseded exact evidence is archived under
  `sprint-ets-56-part1-create-replace-delete-final-0023d5b-2026-07-30`.
- Superseded exact remediation evidence is archived with a verified checksum
  manifest under
  `sprint-ets-56-part1-create-replace-delete-final-1a6c5ec-2026-07-30`.
- Raze returned `GAPS_FOUND 0.98` on `1a6c5ec` for requester-boundary deadline
  overruns, unawaited/unregistered queued custom replace/delete setup
  occurrences, and discarded distinct queued URI-list occurrence Locations.
- Six new controlled regressions cover deadline expiry between pages,
  sub-millisecond no-request handling, delayed custom replace/delete setup,
  distinct queued occurrence Location verification/cleanup, and status
  Location non-use. Test-first compilation failed at the clock constructor,
  behavioral red was `30/4/0/0`, and an initial green attempt was aborted for
  an unbounded no-request-budget loop. Corrected direct HTTP is `31/0/0/0`,
  focused aggregate is `48/0/0/0`, and full Docker Maven is `650/0/0/3`.
- Exact candidate `8aa92d4da33aeb3b1c545378c0a68cb84a565ccb` repeats those
  Maven gates. Exact image
  `sha256:3865aca8a80b5a23fd94531705e0228db5e71c7b2ef65cbc596f83f9c0145d7a`
  embeds `Build-Revision: 8aa92d4da3`; released ATS, both schema parity
  graphs, runtime immutability, local-OSH, sabotage, credential, and hygiene
  gates pass. Exact evidence is archived under
  `sprint-ets-56-part1-create-replace-delete-final-8aa92d4-2026-07-30`.
- Raze `GAPS_FOUND 0.97` supersedes `8aa92d4` because occurrence cleanup could
  delete a mismatched direct Location, compound polling could compose
  transient states, and exact evidence lacked checkout/archive hygiene proof.
  Four requirement-linked regressions produced behavioral red `35/2/0/0`.
  Corrected direct HTTP passes `35/0/0/0`, focused aggregate passes
  `52/0/0/0`, and full clean-cache Docker Maven passes `654/0/0/3` precommit.
- Exact candidate `700c697e59eb2a03d3a41a37ec9a745cd1aa3583` passes focused
  `52/0/0/0`, full Maven `654/0/0/3`, exact image/runtime, released-source,
  both parity graphs, local-OSH, sabotage, credential, immutable-base, and
  hygiene gates. Its image is
  `sha256:5d9c0b9d1d12cf68aaf83e2aec512ecc5abed2fdd9df9568078776443af658f7`
  with `Build-Revision: 700c697e59`; the recursive archive manifest verifies.
- Raze `GAPS_FOUND 0.98` supersedes `700c697`. Prior required findings are
  closed, but an absolute cross-origin HTTP 202 status Location falsely failed
  before classification and raw behavioral-red output was not archived.
- The latest requirement-linked test records red `1/1/0/0`; corrected direct
  HTTP passes `36/0/0/0`, focused Maven `53/0/0/0`, and full clean-cache Docker
  Maven `655/0/0/3`. The replacement exact archive will preserve the raw red.
- Exact candidate `a2ce5478e25542a766025a2a5fde246fc2d5f8d6` passes direct
  HTTP `36/0/0/0`, focused Maven `53/0/0/0`, and full clean-cache Maven
  `655/0/0/3`. Exact image
  `sha256:3e805b4227eda61d5b92bf01ecf83576ad0eca5ed9490eddc73f92c05e6ba9bb`
  embeds `Build-Revision: a2ce5478e2`.
- Released ATS, GeoJSON parity `8/20/0`, Property parity `3/53/0`,
  validator/runtime, immutable-base, local-OSH, sabotage, credential, and
  hygiene gates pass. Populated OSH is `244/54/35/155`; clean primary is
  `244/40/7/197`; provisioning/cleanup pass, state is unchanged, and all 365
  IUT requests are GETs. The pre-review 71-file root and nested 31-file
  manifests verify; the final 72-file root manifest also seals the Raze report.
- Fresh Raze returns `APPROVE_WITH_CONCERNS 0.99`; every prior finding is
  closed and there are no candidate-scoped required fixes.
- Verdict: exact technical/E2E gates and Gate 4 pass. Positive real-IUT
  mutation E2E remains open; mappings stay candidate and Sprint 56 stays IN
  PROGRESS.

Sprint 55 Part 1 Advanced Filtering direct ATS:

- Exactly 25 independent methods implement the 25 released procedures; all
  mappings are reviewed exact.
- Coverage is `240/76 exact/2 helper/115 candidate/47 unmapped`;
  `/conf/advanced-filtering` is `25/25 exact`.
- R6 test-first controlled HTTP is `46/3/0/0`; remediation is `46/0/0/0`,
  focused Maven is `51/0/0/0`, and full Docker Maven is `605/0/0/3`.
- R7 test-first controlled HTTP is `48/2/0/0`; representation-scoped
  remediation is `48/0/0/0`, focused Maven is `53/0/0/0`, and full Docker
  Maven is `607/0/0/3`.
- Scenario trace is `21/21` with no missing literal Java anchors.
- Raze R5 returned `GAPS_FOUND 0.99`; canonical relation vocabulary,
  media-aware System typing, failed representation validation, and keyword
  mapping evidence are remediated with requirement-linked tests.
- Candidate `f2a88d5` exact image `sha256:d7bdc607254...e7f1b` passed runtime
  and released-source audits before Raze R6 superseded the candidate.
- Unmodified local OSH TeamEngine is honestly `238/40/7/191`; all 25 Advanced
  Filtering methods SKIP at the absent declaration and are not positive
  local-IUT conformance evidence.
- API Common sabotage is `238/2/10/226`; credential, immutability, and hygiene
  gates pass with 174 recognized GETs, 169 IUT GETs, zero writes, zero leaks,
  zero unmasked credential hits, 33 masked events, and 33 intact
  transmissions.
- Raze R6 returned `GAPS_FOUND 0.99` for case/punctuation normalization,
  trailing-`Link` stripping, and broad aliases. Separate exact,
  case-sensitive vocabularies close that path.
- Candidate `b5bc49b` exact image `sha256:b883633f236...aec42` passed every
  technical and E2E gate, but Raze R7 returned `GAPS_FOUND 0.99` because
  GeoJSON link relations were still processed for SensorML representations.
- Final candidate `fce461288e99167bab6f391085493784da42cc58` scopes link
  relations to GeoJSON and builds image `sha256:ed03d1f943d...402a79` with
  `Build-Revision: fce461288e`. Exact focused/full Maven pass `53/0/0/0` and
  `607/0/0/3`; released-source, runtime, local OSH, sabotage, credential,
  immutability, and hygiene gates all complete.
- Fresh Raze R8 is `APPROVE 0.99`, closes both prior findings, and reports no
  new findings.
- Final R8 evidence:
  `ops/test-results/sprint-ets-55-part1-advanced-filtering-final-r8-2026-07-29/`.
- Raze report:
  `.harness/evaluations/sprint-ets-55-adversarial-final-r8.yaml`.
- Verification summary:
  `ops/test-results/sprint-ets-55-part1-advanced-filtering-verification-2026-07-29.md`.
- Superseded exact `f2a88d5` candidate evidence:
  `ops/test-results/sprint-ets-55-part1-advanced-filtering-final-r6-2026-07-29/`.
- Superseded exact `b5bc49b` candidate evidence:
  `ops/test-results/sprint-ets-55-part1-advanced-filtering-final-r7-2026-07-29/`.
- Superseded exact `060a8aa` candidate and current R5 remediation evidence:
  `ops/test-results/sprint-ets-55-part1-advanced-filtering-final-r5-2026-07-29/`.
- Superseded `756d729` exact candidate and current R4 remediation evidence:
  `ops/test-results/sprint-ets-55-part1-advanced-filtering-final-r3-2026-07-29/`.
- Superseded R2/R3 remediation evidence:
  `ops/test-results/sprint-ets-55-part1-advanced-filtering-final-r2-2026-07-29/`.

---

Sprint 54 Part 1 GeoJSON direct ATS:

- Twelve released `/conf/geojson` procedures have reviewed exact mappings.
  Coverage is `240 total / 51 exact / 2 helper / 119 candidate / 68 unmapped`;
  `/conf/geojson` is `12/12 exact`.
- Focused Docker Maven is `45/0/0/0`; full Docker Maven is `548/0/0/3`, with
  three unchanged historical harness skips.
- Resolver-normalized GeoJSON schema parity passes for eight entry schemas and
  20 transitive schemas. Property parity provenance and dedicated pagination
  and later-evidence carryovers pass.
- Controlled HTTP executes all twelve positive procedures and key fail-closed
  branches. API Common sabotage skips every GeoJSON method before IUT access.
- Exact image `sha256:9277fe99e6c...245c19` passes TeamEngine provenance,
  deployed SWE Common adapter, collision, immutable-base, runtime, and
  confidential-context gates.
- Primary unmodified local OSH TeamEngine is honestly `219/40/7/172`. All
  twelve GeoJSON procedures are evidence SKIPs, not conformance passes.
- Credential integration and wire E2E pass with zero unmasked artifact hits,
  34 masked events, and 34 intact synthetic transmissions.
- Hygiene recognizes 150 request entries, including 145 IUT GETs, zero writes,
  and zero credential leaks.
- OSH remains clean at `4c87a65`, with `/opt/osh` read-only. No OSH or
  TeamEngine modification.
- Initial Raze findings on documentation state, positive-IUT scope, raw red
  evidence, and failure attribution are closed. Final Raze is `APPROVE` at
  confidence `0.99`, with no required fixes.
- Raze evidence:
  `.harness/evaluations/sprint-ets-54-adversarial-final.yaml`.
- Verification summary:
  `ops/test-results/sprint-ets-54-part1-geojson-verification-2026-07-28.md`.

---

Sprint 53 Part 1 Property Definitions direct ATS:

- Four released `/conf/property` procedures have reviewed exact mappings.
  Coverage is `240 total / 39 exact / 2 helper / 130 candidate / 69 unmapped`;
  `/conf/property` is `4/4 exact`; deployed suite methods remain 220.
- Test-first compilation has the expected 39 missing production symbols.
  Focused Docker Maven is `95/0/0/0`; full Docker Maven is `525/0/0/3`, with
  three unchanged historical harness skips.
- Controlled HTTP has nine tests covering all four positive procedures and
  media, schema, metadata, canonical, pagination, completeness, isolation, and
  dependency behavior.
- Released-source inventory reproduction passes at
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`.
- Resolver-normalized Property schema parity passes for three entry schemas
  and 53 transitive schemas with zero semantic or graph mismatches.
- Exact image `sha256:80ca0a313...2bb0` passes TeamEngine provenance,
  deployed SWE Common adapter, collision, immutable-base, runtime, and
  confidential-context gates.
- Primary unmodified local OSH TeamEngine is honestly `220/40/7/173`.
  Property outcomes are one missing-collection FAIL and three unsupported
  media or missing-evidence SKIPs.
- Core sabotage causes API Common configuration and tests to skip; all four
  Property methods then skip before Property endpoint access.
- Primary hygiene recognizes 120 IUT GETs, zero writes, and zero credential
  leaks.
- Credential integration and wire E2E pass with zero unmasked artifact hits,
  35 masked events, and 35 intact synthetic transmissions.
- OSH remains clean at `4c87a65`, with `/opt/osh` read-only. No OSH or
  TeamEngine modification.
- Raze is `APPROVE_WITH_CONCERNS` at confidence `0.98`, with no required
  fixes. Two LOW future-hardening concerns cover parity-checkout provenance and
  dedicated Property pagination/later-item fixtures.
- Raze evidence:
  `.harness/evaluations/sprint-ets-53-adversarial.yaml`.
- Verification summary:
  `ops/test-results/sprint-ets-53-part1-property-definitions-verification-2026-07-28.md`.

---

Sprint 52 Part 1 Sampling Features direct ATS:

- Coverage: `240 total / 35 exact / 2 helper / 133 candidate / 70 unmapped`;
  `/conf/sf` is `5/5 exact`; deployed suite methods are 220.
- Test-first evidence includes the expected initial compile failure, a
  `6/1/0/0` prerequisite execution defect, and a `1/1/0/0`
  partial-collection false PASS. Both behavioral defects are corrected.
- Initial Raze returned `GAPS_FOUND` at confidence `0.98`. Gap-fix test-first
  runs reproduced omitted nested GeoJSON validation and early-SKIP masking at
  `13/5/0/0` and `16/3/0/0`.
- The remediated page observer validates supported pages before pagination
  advances. Narrow collection/item/System evidence SKIPs aggregate only after
  all independently inspectable candidates have run.
- Final focused Maven is `49/0/0/0`; full Docker Maven is `506/0/0/3`.
- ATS audit consistency passes against released source
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`.
- Exact image `sha256:ae3a7b6b...580ff3` passes TeamEngine provenance, deployed
  SWE Common adapter execution, dependency collision, immutable-base, and
  confidential-context verification.
- Primary unmodified local OSH TeamEngine is honestly `220/40/6/174`; all five
  Sampling Features procedures execute as one PASS, one genuine FAIL, and
  three evidence SKIPs.
- Sampling Features outcomes: System-reference PASS; collections FAIL because
  no exact `sosa:Sample` collection exists; canonical URL SKIP for missing
  collection evidence; both endpoint-schema procedures SKIP on unsupported
  actual `application/json`.
- Controlled HTTP executes every positive path and fail-closed media, schema,
  metadata, canonical, partial-collection, pagination, isolation, and
  dependency branches.
- System sabotage is `220/37/6/177`; all five Sampling Features methods SKIP
  before their IUT access.
- Primary hygiene records 117 recognized requests, zero writes, and zero
  credential leaks.
- Credential integration and wire E2E pass with zero unmasked artifact hits,
  36 masked events, and 36 intact synthetic transmissions.
- OSH is clean at `4c87a65`, zero commits ahead, `/opt/osh` read-only. No OSH
  or TeamEngine modification.
- Focused Raze recheck: `APPROVE`, confidence `0.99`, duration 589 seconds.
  `RAZE-S52-001` and `RAZE-S52-002` are closed; no new findings or required
  fixes remain.
- Raze evidence:
  `.harness/evaluations/sprint-ets-52-adversarial-recheck.yaml`.
- Verification summary:
  `ops/test-results/sprint-ets-52-part1-sampling-features-verification-2026-07-27.md`.

---

Sprint 51 Part 1 Subdeployment direct ATS:

- Coverage: `240 total / 30 exact / 2 helper / 136 candidate / 72 unmapped`;
  `/conf/subdeployment` is `5/5 exact`; deployed suite methods are 219.
- Test-first compile fails as expected with 45 missing-symbol errors.
- Initial Raze returned `GAPS_FOUND` at confidence `0.99`. All four required
  association-oracle, dependency-causality, normalized-URI, and
  multi-occurrence link-selection findings are corrected.
- Corrected focused Maven is `131/0/0/0`; full Maven is `480/0/0/3`.
- ATS audit self-test and released-source reproduction pass at
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`.
- Final exact image `sha256:e88aa5f9...b1dca` passes TeamEngine provenance, embedded
  SWE Common execution, collision, immutable-base, and context-hygiene gates.
- Primary unmodified local OSH TeamEngine is honestly `219/39/5/175`; all five
  Subdeployment methods SKIP before IUT access because inherited Deployment
  fails. The five genuine Deployment/Procedure failures remain visible.
- Controlled HTTP executes all five positive procedures and fail-closed media,
  pagination, schema, link, graph, recursive-set, association, isolation, and
  dependency branches.
- A causal programmatic TestNG experiment proves all five methods reach the IUT
  under a passing synthetic Deployment prerequisite and all five become
  pre-IUT SKIPs when only that prerequisite changes to FAIL. The earlier direct
  local-OSH sabotage is retained only as historical non-causal evidence.
- Primary hygiene: 118 recognized requests, 113 IUT GETs, zero writes, zero
  leaks.
- Credential integration and wire E2E pass with zero unmasked artifact hits,
  37 masked events, and 37 intact synthetic transmissions.
- OSH is clean at `4c87a65`, zero commits ahead, `/opt/osh` read-only. No OSH
  or TeamEngine modification.
- First focused Raze recheck closes all four initial findings and identifies
  repository-root TestNG output plus stale records. Programmatic TestNG now
  writes only to JUnit-managed temporary storage; focused/full Maven leave no
  `test-output/`, and the records are reconciled.
- Independently committed clean source snapshot: full Maven `480/0/0/3`,
  post-run Git status empty, repository-root `test-output/` absent.
- Final Raze: `APPROVE_WITH_CONCERNS`, confidence `0.99`; all six findings are
  closed and no required fixes remain.
- Verification summary:
  `ops/test-results/sprint-ets-51-part1-subdeployment-verification-2026-07-27.md`.

---

Sprint 50 Part 1 Procedure direct ATS:

- Coverage: `240 total / 25 exact / 2 helper / 137 candidate / 76 unmapped`;
  `/conf/procedure` is `5/5 exact`.
- Test-first compilation produced the expected 39 missing-symbol errors; the
  Raze gap-fix test-first run produced two expected missing-method errors.
  Final focused Maven is `116/0/0/0`; full Docker Maven is `451/0/0/3`.
- ATS audit self-test, internal consistency, and released-source reproduction
  pass at `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`.
- Exact image:
  `sha256:6e1beeb598ab4c734f2e2d30e0ecb70d3270af9f9f2d5a1029d1b74259b54d98`.
  Exact-image TeamEngine runtime, adapter, dependency-collision,
  immutable-base, and context-hygiene verification pass.
- Primary unmodified local OSH TeamEngine: `218/39/5/174`; all five Procedure
  methods execute with three unsupported-media SKIPs and two genuine
  missing-collection FAILs.
- Procedure outcomes: location, resources endpoint, and canonical endpoint
  SKIP on unsupported `application/json`; canonical URL and collections FAIL
  because no collection advertises exact `featureType=sosa:Procedure`.
- Positive artifact hygiene: 120 recognized requests, 115 IUT GETs, zero
  writes, zero credential leaks.
- Controlled HTTP coverage executes every successful path for the five released
  procedures and adversarial media, location, type, canonical, and collection
  cases.
- API Common sabotage: `218/34/1/183`; Procedure setup and all five methods
  skip before Procedure IUT access. Sabotage hygiene passes with zero writes
  and zero credential leaks.
- Credential integration and wire E2E pass with zero unmasked execution
  artifact hits, 38 masked events, and 38 intact synthetic transmissions.
- OSH provenance: clean at `4c87a65`, zero commits ahead, `/opt/osh` read-only.
  No OSH or TeamEngine modification.
- Initial Raze review found three canonical-selection, optional-member, and
  dependency-comment gaps; all are remediated with focused regressions.
- Focused Raze recheck: `PASS`, confidence `0.99`, duration 189 seconds, all
  three prior findings closed, no required fixes.
- Verification summary:
  `ops/test-results/sprint-ets-50-part1-procedure-verification-2026-07-26.md`.

---

Sprint 49 Part 1 Deployment direct ATS:

- Coverage: `240 total / 20 exact / 2 helper / 141 candidate / 77 unmapped`;
  `/conf/deployment` is `5/5 exact`.
- Test-first evidence records the intentionally missing restricted-media
  overload. Corrected remediation Maven is `35/0/0/0`; broader focused Maven is
  `90/0/0/0`; full Maven is `434/0/0/3`.
- Released-source reproduction and ATS audit self-test pass at
  `8e03b236a049849f2ccc24b4fd9fdce5ff69bed2`.
- Exact image:
  `sha256:9049b284529b53845403e985fae2b03a9598073724320de2ad2e395006506d47`.
  Exact-image TeamEngine runtime, adapter, dependency-collision,
  immutable-base, and context-hygiene verification pass.
- Primary unmodified local OSH TeamEngine: `217/39/3/175`; all five Deployment
  methods execute with three genuine FAIL and two unsupported-media SKIP
  outcomes.
- Deployment outcomes: collections FAIL for no `sosa:Deployment` collection;
  canonical URL FAIL for the same missing evidence; System reference FAIL on
  nested HTTP 400; resources and canonical endpoints SKIP on unsupported
  `application/json`.
- Positive artifact hygiene: 117 recognized requests, 112 IUT GETs, zero
  writes, zero credential leaks.
- Controlled HTTP coverage executes every successful path for the five released
  procedures and adversarial media, canonical, collection, and System-reference
  cases.
- API Common sabotage: `217/34/1/182`; all five Deployment methods skip
  directly on the injected API Common failure before IUT access. Sabotage
  hygiene passes with 100 recognized requests, 95 IUT GETs, and zero writes.
- Credential integration and corrected wire E2E pass. Exactly one fresh XML and
  container log were selected from the current `SMOKE_OUTPUT_DIR`; XML hashes
  match. Artifacts contain zero unmasked hits, 39 masked events, and the stub
  records 39 intact transmissions.
- OSH provenance: clean at `4c87a65`, zero commits ahead, `/opt/osh` read-only,
  deployed ConSys build `4c87a65`. No OSH or TeamEngine modification.
- Final focused Raze documentation recheck: `APPROVE`, confidence `0.99`,
  duration 46 seconds; `RAZE-S49-FINAL-001` is closed and no required findings
  remain.
- Verification summary:
  `ops/test-results/sprint-ets-49-part1-deployment-verification-2026-07-26.md`.

---

Sprint 48 Part 1 Subsystem direct ATS:

- Coverage: `240 total / 15 exact / 2 helper / 144 candidate / 79 unmapped`;
  `/conf/subsystem` is `5/5 exact`.
- Raze gap-fix test-first: `12/7/0/0`; seven failures reproduce the required
  independent-association, shortcut-edge, media-ordering/status-only, and
  exact-link defects before production fixes.
- Final media recheck test-first: `7/3/0/0`; three failures reproduce
  first-page unsupported/missing media, first-response SensorML, and
  later-page media-order defects before the final correction.
- Root-media test-first: `10/3/0/0`; three failures independently reproduce
  unsupported and missing first-root media plus unsupported later-root
  pagination media before the correction.
- Focused Docker Maven: `45/0/0/0`.
- Full Docker Maven: `417/0/0/3`.
- Exact image:
  `sha256:32a43f81b441f3b687b9e83d9d6688016278f4f7a5fec5d8a3c2b174490f285c`.
- Exact-image TeamEngine runtime, deployed SWE Common adapter, dependency
  collision, and immutable-base verification: PASS.
- Primary local OSH TeamEngine: `216/39/0/177`, 109 recognized requests, zero
  writes, zero startup errors.
- All five Subsystem methods execute. Recursive-param PASSes; collection,
  recursive Systems, recursive Subsystems, and recursive associations SKIP
  before parsing because root `/systems` returns unsupported `application/json`.
- Controlled HTTP coverage executes all five successful positive procedures,
  plus SensorML, unsupported/missing media, nested-404, shortcut, and exact-link
  adversarial regressions.
- SystemFeatures sabotage: `216/37/1/178`; all five Subsystem procedures and
  all 15 dependency descendants SKIP.
- Credential integration and wire gates: PASS with zero unmasked
  execution-artifact hits, 40 masked events, and 40 intact synthetic wire
  transmissions. The durable artifact is a scoped summary; raw synthetic
  verifier and stub captures are not archived.
- Positive and sabotage artifact hygiene: PASS with zero credential leaks and
  zero IUT writes.
- OSH provenance: clean, zero commits ahead, `/opt/osh` read-only, deployed
  ConSys manifest at checkout `4c87a65`.
- Verification summary:
  `ops/test-results/sprint-ets-48-part1-subsystem-verification-2026-07-26.md`.
- Final Raze gap-fix recheck is `APPROVE_WITH_CONCERNS` at confidence `0.99`.
  Every required finding is closed.

---

Sprint 47 Part 1 System direct ATS:

- Coverage: `240 total / 10 exact / 2 helper / 145 candidate / 83 unmapped`;
  `/conf/system` is `6/6 exact`.
- Focused Docker Maven: `46/0/0/0`.
- Released-ATS audit Docker Maven: `23/0/0/0`.
- ATS audit self-test and exact released-source reproduction: PASS.
- Full Docker Maven: `395/0/0/3`.
- Exact image:
  `sha256:101e20653097fea9891ff5fbe1f4c160ae163ca97338cf63cfb5980dd958cf6e`.
- Exact-image TeamEngine runtime, deployed SWE Common adapter, dependency
  collision, and immutable-base verification: PASS.
- Primary local OSH TeamEngine: `215/38/0/177`, 105 recognized GET requests,
  zero writes, zero startup errors.
- System method results: canonical URL PASS, collections PASS, location
  recommendation PASS, canonical endpoint SKIP for unsupported media,
  resources endpoint SKIP for unsupported media, and location-time SKIP for
  absent optional `mobile-system-id`.
- All six methods execute. API Common datetime retains its documented
  no-temporal-extent SKIP; prerequisite failures and every other prerequisite
  SKIP still block System before IUT access.
- Controlled direct HTTP coverage executes successful positive paths for all
  six methods.
- Core-target and System-target dependency sabotage plus both credential gates:
  PASS. The System-target gate verifies all 13 direct and 2 transitive TestNG
  dependency descendant groups SKIP.
- Verification summary:
  `ops/test-results/sprint-ets-47-part1-system-verification-2026-07-26.md`.
- Final Raze: APPROVE (`0.99` confidence), no unresolved required findings.

---

Sprint 46 Part 1 API Common direct ATS:

- Coverage: `240 total / 4 exact / 2 helper / 150 candidate / 84 unmapped`.
- Final Docker Maven: `373 tests / 0 failures / 0 errors / 3 skipped`.
- Implementation commit: `449fbcf`.
- Exact image:
  `sha256:bc5b9cf5a425e6ce2ed1054ee225a68b353f5d8113363414f404b0d4ff27769e`.
- Exact-image TeamEngine 6 runtime verification: PASS.
- Primary local OSH TeamEngine:
  `215 total / 35 passed / 0 failed / 180 skipped`; 102 recognized requests,
  zero writes, and zero startup errors.
- API Common runtime: setup PASS, IDs PASS, UIDs PASS, UID-form recommendation
  PASS with four warnings, and date-time SKIP for no usable temporal extent.
- Dependency sabotage: Core `FAIL=6`, API setup `SKIP=1`, API tests `SKIP=4`,
  SystemFeatures `SKIP=6`.
- Credential integration and wire gates: PASS with zero unmasked artifact hits.
- ATS audit self-test and exact released-source reproduction: PASS.
- Final Raze: `APPROVE`, confidence `0.99`, no required findings.
- Verification summary:
  `ops/test-results/sprint-ets-46-part1-api-common-verification-2026-07-26.md`.
- Durable E2E:
  `ops/test-results/sprint-ets-46-part1-api-common-e2e-2026-07-26/`.

---

Sprint 45 released ATS coverage inventory:

- Released authority: OGC 23-001/23-002 v1.0, reproducible source tag
  `v1.0.0` at commit `8e03b236...`.
- Exact inventory: Part 1 `13 classes / 110 tests` including two supporting
  tests; Part 2 `12 classes / 130 tests`.
- Baseline: `240 total / 0 exact / 0 helper / 150 candidate / 90 unmapped`.
  Candidate mappings do not count as implemented.
- Expected test-first focused failure:
  `5 tests / 5 failures / 0 errors / 0 skipped`.
- Audit self-test and exact-source reproduction: PASS.
- Final focused Maven:
  `23 tests / 0 failures / 0 errors / 0 skipped`.
- Fresh full Docker Maven:
  `345 tests / 0 failures / 0 errors / 3 skipped`.
- Exact candidate image:
  `sha256:ad2594ef5f41beadc5f9de59c8caba27d3af1116d3732892fd498860ee23749c`.
- Primary local OSH TeamEngine E2E:
  `211 total / 69 passed / 0 failed / 142 skipped`; 135 recognized IUT
  requests, zero writes, and zero startup errors.
- Exact-image runtime verification: PASS for deployed adapter execution,
  dependency parity, reviewed collision inventory, TeamEngine base
  immutability, and runtime invariants.
- Verification summary:
  `ops/test-results/sprint-ets-45-released-ats-coverage-inventory-2026-07-26.md`.
- Initial Raze and first recheck: `GAPS_FOUND`, both confidence `0.99`.
  Mapping, membership, evidence, status variants, constructor factories,
  inherited class tests, and all `@Ignore` scopes are remediated.
- Final Raze: `APPROVE`, confidence `0.99`, duration 334 seconds; all findings
  closed and no new findings.

---

Sprint 44 reproducible populated local OSH E2E:

- Workflow infrastructure completed against a separately configured,
  API-populated, unmodified OSH process.
- Provisioning PASS: seven resource-creation POSTs and ten verification GETs.
- Populated TestNG conformance FAIL:
  `211 total / 91 passed / 28 failed / 92 skipped`.
- Populated gate: `COMPLETE_WITH_CONFORMANCE_FAILURE`; the process exited
  non-zero and all 28 class/name/message records are archived.
- All failures are missing unmodified-OSH DataStream/ControlStream collection
  metadata (`live`, `issueTime`, `executionTime`, and `async`).
- Cleanup PASS; no owned containers or ephemeral state remained.
- Normalized primary identity/state comparison PASS with no changed fields.
- Clean-primary TestNG PASS:
  `211 total / 69 passed / 0 failed / 142 skipped`; `GET=133`, `OPTIONS=2`,
  writes zero.
- Behavioral safety tests PASS `12/0/0/0`; focused Maven PASS `9/0/0/0`; full
  Docker Maven PASS `322/0/0/3`.
- Exact-image TeamEngine runtime verification PASS for
  `sha256:cc8c9d711e57ed50d2ed08cdef01cb1236052e775ff27ad016185672e9de8169`.
- Verification summary:
  `ops/test-results/sprint-ets-44-populated-local-osh-verification-2026-07-25.md`.
- Complete hashed E2E evidence:
  `ops/test-results/sprint-ets-44-final-e2e-2026-07-25/`.
- Initial Raze returned `GAPS_FOUND`, confidence `0.99`. Focused recheck
  returned `APPROVE`, confidence `0.99`, after 372 seconds; all ten findings are
  closed, no new findings remain, and token metadata was unavailable.

---

External dependency and CI scope reconciliation:

- Provenance audit:
  `ops/test-results/external-dependency-scope-audit-2026-07-23.md`.
- Current OSH: clean checkout, zero commits ahead of upstream, deployed ConSys
  build metadata reports checkout `4c87a65c...`, `/opt/osh` mount read-only.
  This is provenance metadata, not independent binary byte-equivalence proof.
- Current TeamEngine: no source checkout or project-authored binary patch;
  immutable OGC base digest retained with additive ETS artifacts only.
- Historical Sprint 40 OSH commit `79f89fb` is absent from inspected
  repositories and current fetched refs. Project records classify it as
  local-only; the current environment cannot prove global non-publication.
- No external revert was executed because no current attributable source drift
  or binary-patch evidence was found.
- Dormant GitHub Actions workflow removed; local gates remain authoritative.
- Test-first no-hosted-CI regression: expected pre-cleanup failure,
  `12 tests / 1 failure / 0 errors / 0 skipped`.
- Test-first metadata-aware TeamEngine inventory: expected failure,
  `12 tests / 1 failure / 0 errors / 0 skipped`.
- Corrected focused packaging: `12 tests / 0 failures / 0 errors / 0 skipped`.
- Fresh full Docker Maven:
  `313 tests / 0 failures / 0 errors / 3 skipped`, BUILD SUCCESS.
- Exact candidate image:
  `sha256:7071cc7694aee2d0b3ca2f44dd2fcad79e9f1eff7b6b2c4de52299adb4704b29`.
- Metadata-aware exact-image runtime verifier: PASS for path type, mode,
  ownership, symlink target, directory inventory, and file content.
- Primary unmodified local OSH E2E:
  `211 total / 69 passed / 0 failed / 142 skipped`; 135 recognized IUT request
  logs, zero writes, and zero TeamEngine startup ERROR/SEVERE entries.
- Verification summary:
  `ops/test-results/sprint-ets-43-scope-reconciliation-verification-2026-07-23.md`.
- Initial Raze: `GAPS_FOUND`, confidence `0.99`, duration 274 seconds. All five
  findings were remediated.
- Focused Raze recheck: `APPROVE`, confidence `0.99`, duration 142 seconds, no
  findings. Token metadata was unavailable for both reviews.

Prior SWE Common validator adapter release-build and exact multi-tuple candidate:

- Verification summary:
  `ops/test-results/sprint-ets-42-final-raze-gapfix-verification-2026-07-22.md`.
- Initial final Raze: `GAPS_FOUND`, confidence `0.99`. The first candidate added
  TeamEngine resources 6.0.0 beside inherited resources RC2 with 89 overlapping
  functional paths; image identity was indirect; an empty `.moduledata/log.txt`
  remained. Artifact:
  `.harness/evaluations/sprint-ets-42-final-closure-adversarial-2026-07-22.yaml`.
- Replacement recheck: `GAPS_FOUND`, confidence `0.99`; generic coordinate/path
  checks, message-bundle isolation, fresh Maven, and `.moduledata` removal were
  required. Artifact:
  `.harness/evaluations/sprint-ets-42-duplicate-gapfix-adversarial-recheck-2026-07-22.yaml`.
- Generic-guard recheck: `GAPS_FOUND`, confidence `0.98`; accepted collision
  tuples had to be explicit, unused allowlist entries had to fail, Jenkins had
  to stop requesting a nonexistent Maven profile, and metadata needed final
  reconciliation. Artifact:
  `.harness/evaluations/sprint-ets-42-generic-jar-guard-adversarial-recheck-2026-07-22.yaml`.
- Final metadata recheck: `GAPS_FOUND`, confidence `0.99`; release Jenkins still
  used JDK 8 without the bootstrap and requested undeclared profiles, while the
  self-test did not assert complete exact multi-tuple stdout. Artifact:
  `.harness/evaluations/sprint-ets-42-final-metadata-gapfix-adversarial-recheck-2026-07-22.yaml`.
- Current test-first assertion: expected FAIL,
  `11 tests / 1 failure / 0 errors / 0 skipped`, captured as raw Maven output.
- Corrected focused packaging: PASS,
  `11 tests / 0 failures / 0 errors / 0 skipped`.
- Fresh full Docker Maven: PASS,
  `312 tests / 0 failures / 0 errors / 3 skipped`.
- Exact replacement image:
  `sha256:829a97414c07dd5763ed302e32b3178d301ca098bc9025f4b1f58b692ddad5f9`.
  The final stage adds one ETS jar and CTL resources. NetworkNT bundles are
  isolated; no TeamEngine or duplicate base coordinate is added.
- Strengthened TeamEngine 6 runtime verifier: PASS with direct base/final image
  IDs, generic coordinate and functional-path enforcement, two exact convention
  allowlist entries, exact behavioral multi-tuple output, unused-entry
  rejection, byte-for-byte base immutability, and adapter execution.
- Primary local OSH E2E: PASS from a fresh `/tmp` clone synchronized to the
  final worktree, `211 total / 69 passed / 0 failed / 142 skipped`; 135
  recognized IUT requests, zero writes, and zero startup errors.
- Advisory GeoRobotix: FAIL with exact unchanged Sprint 41 baseline
  `211 total / 38 passed / 34 failed / 139 skipped`; no adapter/linkage regressions;
  `recognized_iut_request_logs=272`; zero writes.
- Current raw gate artifacts:
  `ops/test-results/sprint-ets-42-final-raze-gapfix-test-first-raw-2026-07-22.txt`,
  `ops/test-results/sprint-ets-42-final-raze-gapfix-focused-maven-2026-07-22.txt`,
  `ops/test-results/sprint-ets-42-final-raze-gapfix-full-maven-2026-07-22.txt`,
  `ops/test-results/sprint-ets-42-final-raze-gapfix-local-osh-e2e-2026-07-22.txt`,
  `ops/test-results/sprint-ets-42-final-raze-gapfix-local-osh-final-2026-07-22.xml`,
  `ops/test-results/sprint-ets-42-final-raze-gapfix-local-osh-final-container-2026-07-22.log`,
  `ops/test-results/sprint-ets-42-final-raze-gapfix-local-osh-final-no-mutation-2026-07-22.txt`,
  and `ops/test-results/sprint-ets-42-final-raze-gapfix-runtime-verifier-final-2026-07-22.txt`.
- Pre-E2E Raze recheck: `PASS_WITH_EXTERNAL_BLOCKER`, confidence `0.99`; the
  external blocker was subsequently cleared; artifact
  `.harness/evaluations/sprint-ets-42-swecommon-validator-adversarial-recheck-2026-07-22.yaml`.
- Final Raze gapfix recheck: `APPROVE`, confidence `0.99`, with no required
  actions; artifact
  `.harness/evaluations/sprint-ets-42-final-raze-gapfix-adversarial-recheck-2026-07-22.yaml`.
- Sprint 42 gate status: S-ETS-41-01 and S-ETS-42-02 complete. At that
  checkpoint, REQ-ETS-VALIDATOR-001 remained partial because SensorML was
  deferred; Sprint 58 supersedes that status.

External SWE Common/SensorML validator architecture research:

- Scope: S-ETS-42-01 documentation/planning predecessor; S-ETS-42-02 now implements the first SWE Common adapter increment described above.
- Research artifact: `ops/test-results/external-validator-library-research-2026-07-22.md`.
- SWE Common: `opengeospatial/ets-swecommon30` PR 10 branch `issue-9-swecommon-validation-module` at `3ba75ceabe57cea85f4a8513c59e0f90e386ba96`; intended artifact `org.opengis.cite:swecommon30-validator:0.1-SNAPSHOT`; not published to Maven Central as of 2026-07-22.
- SensorML: no public SensorML validator module found under `FCU-GIS-Luke`; public `opengeospatial/ets-sensorml30` is an ETS scaffold and not accepted as a reusable dependency.
- Verification for this planning pass: static documentation checks only; future implementation that imports a validator must run Docker Maven, the TeamEngine 6 runtime verifier, and primary local OSH TeamEngine E2E.
- Raze review: `.harness/evaluations/sprint-ets-42-external-validator-architecture-adversarial-2026-07-22.yaml` returned `APPROVE_WITH_CONCERNS`, confidence `0.91`, with no required fixes. The one low concern about NetworkNT `ValidationMessage` leakage was captured in the future adapter boundary.

---

Sprint ets-41 readiness pass:

- Scope: canonical run-argument contract, public TeamEngine metadata/docs cleanup, Maven `docker` profile removal, TeamEngine 6 runtime verification, Compose startup verification, advisory deployed execution, and primary local OSH blocker isolation.
- Focused structural test: PASS, `VerifyTeamEngine6Packaging`, `9 tests / 0 failures / 0 errors / 0 skipped`; latest artifact `ops/test-results/sprint-ets-41-readiness-focused-packaging-2026-07-21.txt`.
- Full Docker Maven: PASS, `303 tests / 0 failures / 0 errors / 3 skipped`.
- Maven artifact: `ops/test-results/sprint-ets-41-readiness-maven-2026-07-21.txt`; earlier post-policy Maven artifact `ops/test-results/sprint-ets-41-policy-guidance-maven-2026-07-21.txt` also passed with the same totals.
- Docker image build: PASS, artifact `ops/test-results/sprint-ets-41-readiness-docker-build-2026-07-21.txt`.
- Runtime verifier: PASS for TeamEngine 6 provenance, immutable-base manifests, runtime invariants, and confidential history/context hygiene; artifact `ops/test-results/sprint-ets-41-readiness-runtime-verifier-2026-07-21.txt`.
- Compose startup: PASS; `docker compose up -d --build` reached healthy and `/teamengine/rest/suites` exposed `ogcapi-connectedsystems10` with the expected full title; artifact `ops/test-results/sprint-ets-41-readiness-compose-2026-07-21.txt`.
- Local OSH TeamEngine E2E: PASS `211/69/0/142`, 135 recognized IUT requests,
  zero writes, and zero startup errors; final Sprint 42 closure artifacts listed above.
- Field-hub absence check: `path_exists=false`; Docker networks are only `bridge`, `host`, and `none`; no matching field-hub/OSH containers, images, or volumes; artifact `ops/test-results/sprint-ets-41-readiness-field-hub-absence-2026-07-21.txt`.
- Advisory GeoRobotix deployed run: FAIL against the public IUT, `211 total / 38 passed / 34 failed / 139 skipped`; artifacts `ops/test-results/sprint-ets-41-readiness-georobotix-smoke-2026-07-21.xml` and `ops/test-results/sprint-ets-41-readiness-georobotix-container-2026-07-21.log`.
- Advisory GeoRobotix no-mutation evidence: `recognized_iut_request_logs=272` and explicit write counts `POST=0`, `PUT=0`, `PATCH=0`, `DELETE=0`; artifacts `ops/test-results/sprint-ets-41-readiness-georobotix-no-mutation-2026-07-21.txt` and `ops/test-results/sprint-ets-41-readiness-georobotix-write-counts-2026-07-21.txt`.
- Gate 2 evaluator: `CONCERNS`, artifact `.harness/evaluations/sprint-ets-41-policy-guidance-evaluator-2026-07-21.yaml`; the POM-derived metadata/smoke title finding was remediated before the final focused and full Maven reruns.
- Raze implementation review: initial `GAPS_FOUND` at `.harness/evaluations/sprint-ets-41-policy-guidance-adversarial-implementation-2026-07-21.yaml`; focused recheck `APPROVE_WITH_CONCERNS`, confidence `0.93`, at `.harness/evaluations/sprint-ets-41-policy-guidance-adversarial-recheck-2026-07-21.yaml`. RF-001 through RF-004 are now closed.
- Readiness Raze: initial `GAPS_FOUND` at `.harness/evaluations/sprint-ets-41-readiness-adversarial-2026-07-21.yaml`; focused recheck `APPROVE_WITH_CONCERNS`, confidence `0.91`, at `.harness/evaluations/sprint-ets-41-readiness-adversarial-recheck-2026-07-21.yaml`. Documentation findings `RAZE-S41-READINESS-002` through `-005` were closed in Sprint 41, and the 2026-07-22 final local OSH run subsequently cleared `RAZE-S41-READINESS-001`.
- Historical status: this readiness evidence was accepted before the later
  duplicate-family finding. The replacement candidate at the top supersedes its
  runtime payload and awaits fresh final Raze.

---

Policy-guidance Discovery and Raze static review:

- Discovery agent `Zeno` completed a public-source review of OGC compliance policy, TeamEngine 6 migration guidance, TeamEngine Docker/ETS issue signals, OGC API Connected Systems issue guidance, and OpenSensorHub implementation issue signals.
- Raze agent `Averroes` used the Discovery brief for a static docs/implementation conformance review and returned `GAPS_FOUND`, confidence `0.90`.
- Artifact: `.harness/evaluations/teamengine-policy-guidance-adversarial-2026-07-21.yaml`.
- No product code changed. Maven, Docker build/startup, and TeamEngine local OSH E2E were not run during this review-only pass.

---

Sprint ets-41 TeamEngine 6 runtime migration (historical pre-closure state):

- Runtime verifier: PASS for digest provenance, byte-for-byte immutable-base manifests, non-root identity, utilities/paths, corrected base jar inventory, selected two-file payload, Git history, effective build context, and confidential-file hygiene.
- Formatter: PASS (`spring-javaformat:apply`).
- Docker Maven: readiness BUILD SUCCESS, `303 tests / 0 failures / 0 errors / 3 skipped`, artifact `ops/test-results/sprint-ets-41-readiness-maven-2026-07-21.txt`.
- Image build: PASS using digest-pinned Maven 3.9/Temurin 17 builder and digest-pinned OGC TeamEngine 6 runtime.
- Startup: PASS; effective uid/gid 1001 `tomcat`, inherited JDK 17.0.15+6/Tomcat 10.1.42 configuration, healthy status, `ogcapi-connectedsystems10` present in `/teamengine/rest/suites` with the expected full title, and no registration/linkage errors.
- Correctness failures caught and fixed: broad dependency copying produced `/rest/suites` HTTP 500 with a Jersey `Application` class-identity conflict; an inherited copy execution later leaked three TeamEngine distribution archives and failed the base-manifest verifier. The final image adds only the shaded ETS jar and resources 6.0.0, with NetworkNT/ITU relocated, and restores registration.
- Local OSH E2E was `BLOCKED_BEFORE_IUT_EXECUTION` at this checkpoint because the field-hub runtime was absent. This historical blocker is superseded by the 2026-07-22 final pass (`211/69/0/142`) and no-mutation evidence listed above.
- Raze recheck at this checkpoint: `APPROVE_WITH_CONCERNS`, confidence `0.98`, no required fixes. Its infrastructure concern was subsequently cleared by the final local OSH run.

---

SWE Common 3.0 validator import exploration:

- Artifact: `ops/test-results/swecommon30-validator-import-analysis-2026-07-17.txt`.
- Upstream checked: `opengeospatial/ets-swecommon30` branch `issue-9-swecommon-validation-module`, HEAD `3ba75ceabe57cea85f4a8513c59e0f90e386ba96`.
- Result: the correct import target is `org.opengis.cite:swecommon30-validator`, not `ets-swecommon30`; the validator is not published on Maven Central as of 2026-07-17.
- TeamEngine inventory use: the user-provided WEB-INF/lib list was used as the dependency baseline; it appears to be TeamEngine 5.7 and contains `json-schema-validator-1.0.76.jar`, Jackson 2.12.x/2.15.0 jars, `itu-1.7.0.jar`, and `slf4j-api-1.7.30.jar`.
- Verification: Docker Maven compiled the upstream validator with default versions, then compiled it again with `-Dnetworknt.version=1.0.76 -Djackson.version=2.12.1` to prove compatibility with the key TeamEngine-list validation libraries. The upstream validator module has no tests.
- Scope: no Connected Systems product code changed; TeamEngine E2E was not rerun.

---

Sprint ets-40 OSH ConSys populated binding blocker closure:

- Status:
  - Story: `epics/stories/s-ets-40-01-osh-consys-populated-binding-blockers.md`.
  - Contract: `.harness/contracts/sprint-ets-40.yaml`.
  - Scope result: partial closure. The prior schema response media-type, exact SWE Text request, `cmdFormat`, and JSON child-body blockers are cleared; full populated-IUT closure remains open.
  - Raze review: `.harness/evaluations/sprint-ets-40-adversarial-recheck.yaml` returned `APPROVE_WITH_CONCERNS`, confidence `0.91`, with no required fixes.
- OSH ConSys focused Gradle regressions:
  - Artifacts: `ops/test-results/sprint-ets-40-gapfix-r4-osh-TestDataStreams-2026-06-03.xml`, `ops/test-results/sprint-ets-40-gapfix-r4-osh-TestControlStreams-2026-06-03.xml`, and `ops/test-results/sprint-ets-40-gapfix-r4-osh-TestObservations-2026-06-03.xml`.
  - Result: PASS, `23 tests / 0 failures / 0 errors / 0 skipped` (`TestDataStreams 10`, `TestControlStreams 5`, `TestObservations 8`).
  - Coverage: exact `application/swe+text`, legacy CSV compatibility, JSON-compatible schema response content types, `cmdFormat` aliasing, parseable empty/filtered Observation/Command collection/count JSON bodies, invalid filter non-masking, and field-hub runtime compatibility for temporal sort handling.
- ETS Maven:
  - Artifact: `ops/test-results/sprint-ets-40-maven-test-via-docker-2026-06-03.txt`.
  - Result: PASS, `294 tests / 0 failures / 0 errors / 3 skipped`.
- Direct SimUAV blocker probes:
  - Artifacts: `ops/test-results/sprint-ets-40-gapfix-r4-local-osh-simuav-format-probes-2026-06-03.tsv` and `ops/test-results/sprint-ets-40-gapfix-r4-local-osh-simuav-format-probes-parsed-2026-06-03.tsv`.
  - Result: DataStream and ControlStream schema requests for JSON/SWE formats returned HTTP 200 with JSON-compatible schema response media types; exact SWE Text bodies expose `TextEncoding`; `cmdFormat` and `commandFormat` both work for Command schema requests; nested Observation reads returned parseable JSON with `items=1`; nested Command reads returned parseable empty JSON bodies and count `0`.
- SimUAV preseed evidence:
  - Artifact: `ops/test-results/sprint-ets-40-local-osh-simuav-preseed-r2-2026-06-03.json`.
  - Result: `PARTIAL_MISSING_OBSERVATION_OR_COMMAND_CHILD_EVIDENCE`.
  - Summary: 2 DataStreams, 3 ControlStreams, `GET=104`, `POST=1`, waypoint feasibility Command POST HTTP 200 with `statusCode=COMPLETED`; nested Command body parsed as JSON, but the preseed artifact itself had no positive Observation or nested Command item evidence. Later r4 direct probes supplied nested Observation `items=1`; nested Command stayed `count=0`.
- SimUAV-populated local OSH TeamEngine smoke:
  - Report: `ops/test-results/sprint-ets-40-simuav-local-osh-smoke-final-2026-06-03.xml`.
  - Container log: `ops/test-results/sprint-ets-40-simuav-local-osh-container-final-2026-06-03.log`.
  - Hygiene summary: `ops/test-results/sprint-ets-40-artifact-hygiene-simuav-populated-final-2026-06-03.json`.
  - Result: FAIL, `211 total / 86 passed / 17 failed / 108 skipped`.
  - Hygiene evidence: IUT methods `GET=240`, `OPTIONS=14`, writes `0`, credential leaks `0`.
  - Remaining failures: Part 2 JSON/SWE schema shape and mapping checks. Examples include missing `commandFormat`, missing SWE `recordSchema`, missing/incorrect `encoding.type`, Observation schema `recordSchema` validation failures, and JSON schema `obsFormat`/result schema mismatches. Binding tests now SKIP missing positive child evidence instead of failing on unparsable bodies.
- Clean primary local OSH restore:
  - Reseed artifact: `ops/test-results/sprint-ets-40-gapfix-clean-local-osh-reseed-2026-06-03.json`.
  - TeamEngine report: `ops/test-results/sprint-ets-40-gapfix-clean-local-osh-smoke-r2-2026-06-03.xml`.
  - Container log: `ops/test-results/sprint-ets-40-gapfix-clean-local-osh-container-r2-2026-06-03.log`.
  - No-mutation artifact: `ops/test-results/sprint-ets-40-gapfix-clean-local-osh-no-mutation-r2-2026-06-03.txt`.
  - Hygiene summary: `ops/test-results/sprint-ets-40-gapfix-artifact-hygiene-clean-local-osh-r2-2026-06-03.json`.
  - Result: PASS, `211 total / 61 passed / 0 failed / 150 skipped`; no-mutation evidence reports zero IUT-bound POST/PUT/DELETE/PATCH entries.
  - Hygiene evidence: IUT methods `GET=115`, `OPTIONS=2`, writes `0`, credential leaks `0`.

Sprint ets-39 artifact hygiene and URI/schema drift harness:

- Status:
  - Story: `epics/stories/s-ets-39-01-artifact-hygiene-drift-harness.md`.
  - Contract: `.harness/contracts/sprint-ets-39.yaml`.
  - Tools implemented: `scripts/artifact-hygiene.py` and `scripts/uri-drift-audit.py`.
  - Raze review: `.harness/evaluations/sprint-ets-39-adversarial-implementation.yaml` returned `APPROVE_WITH_CONCERNS`, confidence `0.90`, with no required fixes; focused recheck `.harness/evaluations/sprint-ets-39-adversarial-recheck.yaml` returned `APPROVE`, confidence `0.94`, with no required fixes.
  - CI-failing URI drift enforcement is not enabled; the drift audit is report-only until the allowlist is stabilized.
- Self-tests:
  - `python3 scripts/artifact-hygiene.py --self-test`: PASS.
  - `python3 scripts/uri-drift-audit.py --self-test`: PASS.
  - `python3 -m py_compile scripts/artifact-hygiene.py scripts/uri-drift-audit.py`: PASS.
- Sprint 38 artifact hygiene summaries:
  - Clean report: `ops/test-results/sprint-ets-39-artifact-hygiene-s38-clean-2026-06-03.json`; PASS, TestNG `211/68/0/143`, IUT methods `GET=133`, `OPTIONS=2`, writes `0`, credential leaks `0`, explicit secret inputs scanned `1`.
  - Populated read-only gate: `ops/test-results/sprint-ets-39-artifact-hygiene-s38-populated-2026-06-03.json`; FAIL by policy because the mutable populated artifact contains `POST=3`, `PUT=1`, `DELETE=3`.
  - Populated mutable report-only summary: `ops/test-results/sprint-ets-39-artifact-hygiene-s38-populated-mutable-2026-06-03.json`; PASS, TestNG `211/83/29/99`, IUT methods `GET=248`, `OPTIONS=12`, `POST=3`, `PUT=1`, `DELETE=3`, credential leaks `0`, explicit secret inputs scanned `1`.
- URI/schema drift audit:
  - Artifact: `ops/test-results/sprint-ets-39-uri-schema-drift-audit-2026-06-03.json`.
  - Result: PASS in report-only mode with URI drift detected.
  - URI counts: Java `98`, web app `215`, missing-in-Java `162`, missing-in-webapp `45`.
  - Schema bundle parity: Java `126`, web app `126`, missing/extra/hash mismatch `0`.
  - Provenance: Java ETS HEAD `9f8a82fee3822f921400acddc655b07eef2370b5`; frozen web-app HEAD `1568f364bef075fcf8419be966e9b08de677b23c`; both repositories were dirty and the artifact records dirty entry counts without listing file names.
- Maven:
  - Artifact: `ops/test-results/sprint-ets-39-maven-test-via-docker-2026-06-03.txt`.
  - Result: PASS, `294 tests / 0 failures / 0 errors / 3 skipped`.
- Clean primary local OSH TeamEngine E2E:
  - Report: `ops/test-results/sprint-ets-39-clean-local-osh-smoke-2026-06-03.xml`.
  - Container log: `ops/test-results/sprint-ets-39-clean-local-osh-container-2026-06-03.log`.
  - Hygiene summary: `ops/test-results/sprint-ets-39-artifact-hygiene-clean-local-osh-2026-06-03.json`.
  - Result: PASS, `211 total / 68 passed / 0 failed / 143 skipped`.
  - Hygiene evidence: IUT methods `GET=133`, `OPTIONS=2`, writes `0`, credential leaks `0`, explicit secret inputs scanned `1`; JSON now references archived `ops/test-results/` XML/log paths.

Sprint ets-38 SimUAV preseeded populated-IUT fixture:

- Status:
  - Story: `epics/stories/s-ets-38-01-simuav-preseeded-populated-iut-fixture.md`.
  - Contract: `.harness/contracts/sprint-ets-38.yaml`.
  - ETS candidate selection and the SimUAV preseed/probe path are implemented. Clean primary local OSH is restored and passing. Full populated-IUT `part2binding` PASS is not claimed.
- Python preseed script:
  - Command: `python3 -m py_compile scripts/local-osh-simuav-preseed.py`.
  - Result: PASS.
- SimUAV preseed final evidence:
  - Artifact: `ops/test-results/sprint-ets-38-local-osh-simuav-preseed-r3-2026-06-03.json`.
  - Result: `PARTIAL_MISSING_OBSERVATION_OR_COMMAND_CHILD_EVIDENCE`.
  - Summary: 2 DataStreams, 3 ControlStreams, 2 DataStream schema endpoints, 3 command schema endpoints, `GET=102`, `POST=1`.
  - Command evidence: selected `inputName=waypoint_feasibility`; POST returned HTTP 200 and `statusCode=COMPLETED`.
  - Missing evidence: zero parseable Observation child items; nested Command collection returned HTTP 200 with an empty body; POST-returned command id was not dereferenceable through `/commands/{id}`.
  - Credential handling: artifact records `authCredentialSupplied=true` and `authCredentialRecorded=false`; no credential value is stored.
- ETS Maven/regression verification:
  - Required wrapper `bash scripts/mvn-test-via-docker.sh -Dmaven.wagon.rto=30000 -Dmaven.wagon.httpconnectionManager.ttlSeconds=30` failed twice before tests on Maven Central `Connection reset`; a later wrapper retry was stopped after a silent stall.
  - Equivalent Docker Maven run with a reusable host cache passed `294 tests / 0 failures / 0 errors / 3 skipped`.
  - Artifact: `ops/test-results/sprint-ets-38-maven-clean-test-cached-2026-06-03.log`.
  - New focused unit coverage includes `VerifyPart2CandidateSelection` with `3 tests / 0 failures / 0 errors / 0 skipped`.
- SimUAV-populated local OSH TeamEngine smoke after static reseed:
  - Report: `ops/test-results/sprint-ets-38-simuav-preseed-teamengine-smoke-r3-2026-06-03.xml`.
  - Container log: `ops/test-results/sprint-ets-38-simuav-preseed-teamengine-container-r3-2026-06-03.log`.
  - Result: FAIL, `211 total / 83 passed / 29 failed / 99 skipped`.
  - Remaining failures: schema endpoint `Content-Type: auto`, SWE text schema HTTP 400, empty Observation child bodies, empty nested Command bodies, and missing parseable nested child evidence.
- Clean primary local OSH restore:
  - Post-Raze field-hub OSH rebuild/reset artifact: `ops/test-results/sprint-ets-38-post-raze-field-hub-osh-rebuild-reset-2026-06-03.log`.
  - Post-Raze reseed artifact: `ops/test-results/sprint-ets-38-post-raze-clean-local-osh-reseed-2026-06-03.json`.
  - Post-Raze root Observation probe: `ops/test-results/sprint-ets-38-post-raze-clean-local-osh-observations-probe-2026-06-03.txt`.
  - Post-Raze TeamEngine smoke report: `ops/test-results/sprint-ets-38-post-raze-clean-local-osh-smoke-2026-06-03.xml`.
  - Post-Raze TeamEngine container log: `ops/test-results/sprint-ets-38-post-raze-clean-local-osh-container-2026-06-03.log`.
  - Post-Raze TeamEngine console log: `ops/test-results/sprint-ets-38-post-raze-clean-local-osh-smoke-console-2026-06-03.log`.
  - Result: PASS, `211 total / 68 passed / 0 failed / 143 skipped`.
  - No-mutation evidence: smoke stdout reported zero IUT-bound POST/PUT/DELETE/PATCH entries for the clean primary run.
- Fixture state:
  - SimUAV and Sapient are disabled in `/home/nh/docker/gir/sar-ops/field-hub/osh/config/config.json`.
  - `field-hub_osh-data` was reset and static `/systems/040g`, `/procedures/040g`, `/deployments/040g`, and `/samplingFeatures/040g` fixtures were reseeded.
- Raze review:
  - Initial report: `.harness/evaluations/sprint-ets-38-adversarial-implementation.yaml` returned `GAPS_FOUND`.
  - Focused recheck: `.harness/evaluations/sprint-ets-38-adversarial-recheck.yaml` returned `APPROVE_WITH_CONCERNS`, confidence `0.92`, with both prior gaps closed and no required fixes.

Sprint ets-37 local OSH stream metadata unblock:

- Status:
  - Story: `epics/stories/s-ets-37-01-local-osh-stream-metadata-unblock.md`
  - Contract: `.harness/contracts/sprint-ets-37.yaml`
  - ETS and OSH code changes are implemented and unit/regression verified. Clean primary local OSH smoke is restored after the follow-up empty-body fix, but full populated-IUT closure is still not claimed.
  - Full populated-IUT `part2binding` PASS is still not claimed.
- ETS format-assertion regression:
  - Artifact: `ops/test-results/sprint-ets-37-ets-maven-test-2026-06-03.log`.
  - Result: PASS, `291 tests / 0 failures / 0 errors / 3 skipped`.
  - Focused check: `VerifyPart2SchemaValidation` passed `2 tests / 0 failures / 0 errors / 0 skipped`.
- OSH ConSys regression:
  - Artifacts: `ops/test-results/sprint-ets-37-osh-TestDataStreams-2026-06-03.xml`, `ops/test-results/sprint-ets-37-osh-TestControlStreams-2026-06-03.xml`, and `ops/test-results/sprint-ets-37-osh-consys-focused-gradle-test-2026-06-03.log`.
  - Result: `TestDataStreams` passed `9/0/0/0`; `TestControlStreams` passed `1/0/0/0`.
  - Compile: `:sensorhub-service-consys:compileJava` and `:sensorhub-service-consys:compileTestJava` completed successfully with warnings only.
- OSH ConSys empty-body follow-up regression:
  - Artifacts: `ops/test-results/sprint-ets-37-followup-osh-TestObservations-2026-06-03.xml` and `ops/test-results/sprint-ets-37-followup-osh-TestControlStreams-2026-06-03.xml`.
  - Result: `TestObservations` passed `5/0/0/0`; `TestControlStreams` passed `2/0/0/0`.
  - Direct reset/reseed probe: `ops/test-results/sprint-ets-37-followup-empty-collection-live-probe-after-reset-reseed-2026-06-03.txt`; `/observations?limit=1`, `/observations?f=json&limit=1`, and `/observations/count?f=json` returned HTTP 200 `application/json` with parseable JSON bodies.
  - Post-Raze hardening artifacts: `ops/test-results/sprint-ets-37-followup-osh-TestObservations-after-raze-2026-06-03.xml` and `ops/test-results/sprint-ets-37-followup-osh-TestControlStreams-after-raze-2026-06-03.xml`.
  - Post-Raze result: `TestObservations` passed `5/0/0/0`; `TestControlStreams` passed `2/0/0/0` after removing broad RuntimeException-to-empty masking.
  - Post-Raze direct probe: `ops/test-results/sprint-ets-37-followup-empty-collection-live-probe-after-raze-2026-06-03.txt`; root Observation collection/count endpoints returned HTTP 200 `application/json` with parseable JSON bodies after credential rotation and local OSH restart.
- Local OSH redeploy:
  - Built OSH ConSys jar and replaced the field-hub runtime jar; hash evidence is `ops/test-results/sprint-ets-37-field-hub-consys-jar-replacement-2026-06-03.txt`.
  - Post-Raze rebuilt OSH ConSys jar replacement hash evidence is `ops/test-results/sprint-ets-37-followup-field-hub-consys-jar-replacement-after-raze-2026-06-03.txt`.
  - Rebuild log: `ops/test-results/sprint-ets-37-field-hub-osh-rebuild-2026-06-03.log`.
  - Runtime note: Docker healthcheck remains unhealthy because unauthenticated `/systems` returns 401; authenticated CS API probes returned 200.
- SimUAV-populated metadata probe:
  - Artifact: `ops/test-results/sprint-ets-37-local-osh-simuav-stream-metadata-probe-2026-06-03.json`.
  - Result: dynamic DataStreams include `phenomenonTime`, `resultTime`, and `live`; dynamic ControlStreams include `issueTime`, `executionTime`, `live`, and `async`; schema subresources with `f=json` returned HTTP 200 `application/json`.
- SimUAV-populated local OSH TeamEngine smoke:
  - Result: FAIL, `211 total / 78 passed / 35 failed / 98 skipped`.
  - Report: `ops/test-results/sprint-ets-37-simuav-local-osh-smoke-failed-2026-06-03.xml`.
  - Container log: `ops/test-results/sprint-ets-37-simuav-local-osh-container-failed-2026-06-03.log`.
  - Console log: `ops/test-results/sprint-ets-37-simuav-local-osh-smoke-console-2026-06-03.log`.
  - No-mutation evidence: `ops/test-results/sprint-ets-37-simuav-local-osh-smoke-failed-no-mutation-2026-06-03.txt`, `recognized_iut_request_logs=190`, `GET=178`, `OPTIONS=12`, writes `0`.
  - Remaining failures: stream metadata failures are cleared, but non-binding schema requests using only `obsFormat`/`cmdFormat` still receive `Content-Type: auto`; some Observation/Command child endpoints return empty bodies under `application/json`; some SWE text schema requests return HTTP 400.
- Clean primary local OSH TeamEngine smoke after reset/reseed:
  - Reset/reseed artifacts: `ops/test-results/sprint-ets-37-field-hub-osh-reset-clean-2026-06-03.log` and `ops/test-results/sprint-ets-37-clean-local-osh-reseed-2026-06-03.json`.
  - Result: FAIL, `211 total / 67 passed / 4 failed / 140 skipped`.
  - Report: `ops/test-results/sprint-ets-37-clean-local-osh-smoke-failed-2026-06-03.xml`.
  - Container log: `ops/test-results/sprint-ets-37-clean-local-osh-container-failed-2026-06-03.log`.
  - Console log: `ops/test-results/sprint-ets-37-clean-local-osh-smoke-console-2026-06-03.log`.
  - No-mutation evidence: `ops/test-results/sprint-ets-37-clean-local-osh-smoke-failed-no-mutation-2026-06-03.txt`, `recognized_iut_request_logs=135`, `GET=133`, `OPTIONS=2`, writes `0`.
  - Direct blocker probe: `ops/test-results/sprint-ets-37-clean-local-osh-empty-observations-probe-2026-06-03.txt`; `/observations?limit=1` returned HTTP 200 `application/json` with an empty body.
- Follow-up clean primary local OSH TeamEngine smoke after empty-body fix:
  - Reset/reseed artifact: `ops/test-results/sprint-ets-37-followup-clean-local-osh-reseed-2026-06-03.json`.
  - Result: PASS, `211 total / 68 passed / 0 failed / 143 skipped`.
  - Report: `ops/test-results/sprint-ets-37-followup-clean-local-osh-smoke-2026-06-03.xml`.
  - Container log: `ops/test-results/sprint-ets-37-followup-clean-local-osh-container-2026-06-03.log`.
  - No-mutation evidence: `ops/test-results/sprint-ets-37-followup-clean-local-osh-no-mutation-2026-06-03.txt`, `recognized_iut_request_logs=135`, `GET=133`, `OPTIONS=2`, writes `0`.
  - Post-Raze result after removing broad exception masking and rotating local OSH credentials: PASS, `211 total / 68 passed / 0 failed / 143 skipped`.
  - Post-Raze report: `ops/test-results/sprint-ets-37-followup-after-raze-clean-local-osh-smoke-2026-06-03.xml`.
  - Post-Raze container log: `ops/test-results/sprint-ets-37-followup-after-raze-clean-local-osh-container-2026-06-03.log`.
  - Post-Raze no-mutation evidence: `ops/test-results/sprint-ets-37-followup-after-raze-clean-local-osh-no-mutation-2026-06-03.txt`, `recognized_iut_request_logs=135`, `GET=133`, `OPTIONS=2`, writes `0`.
- Raze implementation review:
  - Artifact: `.harness/evaluations/sprint-ets-37-adversarial-implementation.yaml`.
  - Initial verdict: `GAPS_FOUND`, confidence `0.90`, for three reconciliation gaps: TEAMENGINE-006 traceability omitted Sprint 37 E2E failures, the story scenario list omitted the positive local OSH closure scenario, and `ops/known-issues.md` had a stale timestamp.
  - Gapfix: updated traceability, story scenario list, known-issues timestamp, and traceability document header.
  - Final verdict: `APPROVE_WITH_CONCERNS`, confidence `0.93`, no required fixes.
  - Remaining concern: Sprint 37 remains E2E-blocked and must not be reported complete.
  - Follow-up Raze artifact: `.harness/evaluations/sprint-ets-37-followup-empty-collection-adversarial.yaml`.
  - Follow-up verdict: `GAPS_FOUND`, confidence `0.88`.
  - Follow-up fixes applied: removed broad RuntimeException-to-empty-response masking, reconciled OpenSpec/traceability/known-issues, narrowed Command empty-collection claims to focused OSH regression evidence, and rotated local OSH BasicRealm credentials without recording the new values.
  - Focused follow-up recheck artifact: `.harness/evaluations/sprint-ets-37-followup-empty-collection-adversarial-recheck.yaml`.
  - Focused follow-up recheck verdict: `APPROVE_WITH_CONCERNS`, confidence `0.91`, no required fixes; full populated `part2binding` closure remains unapproved.

Sprint ets-36 binding parent schema JSON request shaping:

- Status:
  - Story: `epics/stories/s-ets-36-01-binding-schema-json-request-shaping.md`
  - Contract: `.harness/contracts/sprint-ets-36.yaml`
  - Generator verification and required Raze review are complete; implementation pushed as `50024c4`.
  - Full populated-IUT `part2binding` PASS is not claimed because local OSH stream collection metadata still fails current Part 2 schema checks.
- Source probe:
  - Artifact: `ops/test-results/sprint-ets-36-local-osh-schema-request-shaping-source-probe-2026-06-02.txt`.
  - Result: local OSH ConSys schema handlers accept `f=json`/`format=json` and set response content type from parsed format, so the ETS can request explicit JSON instead of accepting `Content-Type: auto`.
- Formatter:
  - Result: PASS after bounded cached Docker Maven rerun.
  - Note: an earlier unbounded cold-cache formatter attempt was stopped as stalled; this is not accepted as a test result.
- Focused Maven:
  - Artifact: `ops/test-results/sprint-ets-36-focused-maven-2026-06-02.log`.
  - Result: PASS, `14 tests / 0 failures / 0 errors / 0 skipped`.
- Required wrapper attempt:
  - Artifact: `ops/test-results/sprint-ets-36-maven-clean-test-2026-06-02.log`.
  - Result: FAILED before tests because Maven Central reset while resolving `org.apache.maven.plugins:maven-plugins:pom:43`.
- Full Docker Maven fallback:
  - Artifact: `ops/test-results/sprint-ets-36-maven-clean-test-persistent-cache-2026-06-02.log`.
  - Result: PASS, `289 tests / 0 failures / 0 errors / 3 skipped`.
- Clean primary local OSH TeamEngine smoke:
  - Command: `SMOKE_AUTH_CREDENTIAL=<derived, not logged> SMOKE_CONTAINER_NAME=ets-csapi-s36-clean-local-osh SMOKE_DOCKER_NETWORK=field-hub_default SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api SMOKE_OUTPUT_DIR=/tmp/sprint-ets-36-clean-local-osh-2026-06-02-results bash scripts/smoke-test.sh`.
  - Result: PASS, `211 total / 68 passed / 0 failed / 143 skipped`.
  - Report: `ops/test-results/sprint-ets-36-clean-local-osh-smoke-2026-06-02.xml`.
  - Container log: `ops/test-results/sprint-ets-36-clean-local-osh-container-2026-06-02.log`.
  - Console log: `ops/test-results/sprint-ets-36-clean-local-osh-smoke-console-2026-06-02.log`.
  - No-mutation artifact: `ops/test-results/sprint-ets-36-clean-local-osh-no-mutation-2026-06-02.txt`.
  - No-mutation evidence: `recognized_iut_request_logs=135`, `GET=133`, `OPTIONS=2`, writes `0`.
- Raze implementation review:
  - Artifact: `.harness/evaluations/sprint-ets-36-adversarial-implementation.yaml`.
  - Initial verdict: `GAPS_FOUND`, confidence `0.89`, for stale OpenSpec implementation-status wording that still framed the Sprint 35 schema media-type blocker as current.
  - Gapfix: OpenSpec now records Sprint 36 `f=json` request shaping and leaves stream metadata plus unexercised populated live `?f=json` fixture evidence as the remaining blockers.
  - Final verdict: `APPROVE_WITH_CONCERNS`, confidence `0.93`, no required fixes.
  - Remaining concern: clean local OSH smoke passed with empty `/datastreams` and `/controlstreams`, so it did not exercise populated parent schema `?f=json` requests; no full populated `part2binding` PASS is claimed.

Sprint ets-35 local OSH SimUAV tasking fixture:

- Status:
  - Story: `epics/stories/s-ets-35-01-local-osh-simuav-tasking-fixture.md`
  - Contract: `.harness/contracts/sprint-ets-35.yaml`
  - SimUAV is verified as an isolated tasking fixture for terminal CommandStatus plus inline CommandResult evidence.
  - Full populated-IUT `part2binding` PASS is not claimed because local OSH stream collection metadata and schema endpoint media types fail current Part 2 schema/binding checks.
- Isolated SimUAV command E2E:
  - Artifact: `ops/test-results/sprint-ets-35-local-osh-simuav-command-e2e-2026-06-02.json`.
  - Result: PASS for task acknowledgement and inline result evidence.
  - System: `02kargmsuc2g`.
  - DataStreams: `03la3nu3m47g` and `026a5nuan1s0`.
  - ControlStreams: `03jtrf5qmkng`, `03lbn3mgpspg`, and `02481pm3g53g`.
  - Command: `02481pm3g53g1m6uvj80cc85rppg`.
  - Terminal status: `COMPLETED`.
  - Inline result fields: `reachable`, `time_to_waypoint`, and `battery_remaining`.
- SimUAV-populated TeamEngine smoke:
  - Command: `SMOKE_AUTH_CREDENTIAL=<derived, not logged> SMOKE_CONTAINER_NAME=ets-csapi-s35-simuav-local-osh SMOKE_DOCKER_NETWORK=field-hub_default SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api SMOKE_OUTPUT_DIR=/tmp/sprint-ets-35-simuav-local-osh-2026-06-02-results bash scripts/smoke-test.sh`.
  - Result: FAIL, `211 total / 84 passed / 28 failed / 99 skipped`.
  - Cause: local OSH omits schema-required stream collection metadata (`live`, `async`, and some time ranges) and serves schema resources with `Content-Type: auto`.
  - Report: `ops/test-results/sprint-ets-35-simuav-local-osh-smoke-failed-2026-06-02.xml`.
  - Container log: `ops/test-results/sprint-ets-35-simuav-local-osh-container-failed-2026-06-02.log`.
  - No-mutation artifact: `ops/test-results/sprint-ets-35-simuav-local-osh-smoke-failed-no-mutation-2026-06-02.txt`.
  - No-mutation evidence: `recognized_iut_request_logs=164`, `GET=150`, `OPTIONS=14`, writes `0`.
- Clean primary local OSH TeamEngine smoke:
  - Command: `SMOKE_AUTH_CREDENTIAL=<derived, not logged> SMOKE_CONTAINER_NAME=ets-csapi-s35-clean-local-osh SMOKE_DOCKER_NETWORK=field-hub_default SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api SMOKE_OUTPUT_DIR=/tmp/sprint-ets-35-clean-local-osh-2026-06-02-results bash scripts/smoke-test.sh`.
  - Result: PASS, `211 total / 68 passed / 0 failed / 143 skipped`.
  - Report: `ops/test-results/sprint-ets-35-clean-local-osh-smoke-2026-06-02.xml`.
  - Container log: `ops/test-results/sprint-ets-35-clean-local-osh-container-2026-06-02.log`.
  - No-mutation artifact: `ops/test-results/sprint-ets-35-clean-local-osh-no-mutation-2026-06-02.txt`.
  - No-mutation evidence: `recognized_iut_request_logs=135`, `GET=133`, `OPTIONS=2`, writes `0`.
- Raze implementation review:
  - Artifact: `.harness/evaluations/sprint-ets-35-adversarial-implementation.yaml`.
  - Verdict: `APPROVE_WITH_CONCERNS`, confidence `0.90`, no required fixes.

Sprint ets-34 local OSH tasking driver fixture:

- Status:
  - Story: `epics/stories/s-ets-34-01-local-osh-tasking-driver-fixture.md`
  - Contract: `.harness/contracts/sprint-ets-34.yaml`
  - Sapient is verified as an isolated tasking driver fixture for terminal CommandStatus evidence.
  - Full populated-IUT `part2binding` PASS is not claimed because Sapient dynamic streams fail broader Part 2 schema validation.
- Isolated Sapient command E2E:
  - Artifact: `ops/test-results/sprint-ets-34-local-osh-sapient-command-e2e-2026-06-02.json`.
  - Result: PASS for task acknowledgement evidence.
  - Node: `eeee3401-0000-0000-0000-000000000034`.
  - ControlStream: `02nc9lm4r9p0`.
  - Command: `02nc9lm4r9p01sv2vf80cdtdkmf0`.
  - Protocol Task: `f3ee6568d2fe4fc79ab57efc8b`.
  - Terminal status: `COMPLETED`.
- Sapient-populated TeamEngine smoke:
  - Command: `SMOKE_AUTH_CREDENTIAL=<derived, not logged> SMOKE_CONTAINER_NAME=ets-csapi-s34-local-osh SMOKE_TARGET=local-osh SMOKE_DOCKER_NETWORK=field-hub_default SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api SMOKE_OUTPUT_DIR=/tmp/sprint-ets-34-local-osh-2026-06-02-results bash scripts/smoke-test.sh`.
  - Result: FAIL, `211 total / 82 passed / 28 failed / 101 skipped`.
  - Cause: newly visible Sapient DataStream/ControlStream resources fail existing Part 2 JSON/SWE schema validation; this is not the prior Command timeout.
  - Report: `ops/test-results/sprint-ets-34-local-osh-smoke-failed-2026-06-02.xml`.
  - Container log: `ops/test-results/sprint-ets-34-local-osh-container-failed-2026-06-02.log`.
  - No-mutation artifact: `ops/test-results/sprint-ets-34-local-osh-smoke-failed-no-mutation-2026-06-02.txt`.
  - No-mutation evidence: `recognized_iut_request_logs=163`, `GET=149`, `OPTIONS=14`, writes `0`.
- Clean primary local OSH TeamEngine smoke:
  - Command: `SMOKE_AUTH_CREDENTIAL=<derived, not logged> SMOKE_CONTAINER_NAME=ets-csapi-s34-clean-local-osh SMOKE_TARGET=local-osh SMOKE_DOCKER_NETWORK=field-hub_default SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api SMOKE_OUTPUT_DIR=/tmp/sprint-ets-34-local-osh-clean-2026-06-02-results bash scripts/smoke-test.sh`.
  - Result: PASS, `211 total / 68 passed / 0 failed / 143 skipped`.
  - Report: `ops/test-results/sprint-ets-34-clean-local-osh-smoke-2026-06-02.xml`.
  - Container log: `ops/test-results/sprint-ets-34-clean-local-osh-container-2026-06-02.log`.
  - No-mutation artifact: `ops/test-results/sprint-ets-34-clean-local-osh-no-mutation-2026-06-02.txt`.
  - No-mutation evidence: `recognized_iut_request_logs=135`, `GET=133`, `OPTIONS=2`, writes `0`.
- Maven verification:
  - Command: bounded rerun of `bash scripts/mvn-test-via-docker.sh` with Maven transfer timeouts.
  - Result: PASS, `288 tests / 0 failures / 0 errors / 3 skipped`.
  - Log: `ops/test-results/sprint-ets-34-maven-clean-test-2026-06-02.log`.
  - Note: an earlier unbounded wrapper attempt was stopped after the Maven JVM blocked in an HTTPS dependency download before `clean`; this was dependency-transfer behavior, not an ETS test failure.

Sprint ets-33 Generator partial discovery:

- Status:
  - Story: `epics/stories/s-ets-33-01-local-osh-dynamic-data-seed-fixtures-planning.md`
  - Contract: `.harness/contracts/sprint-ets-33.yaml`
  - Helper regressions are implemented.
  - Observation-side seed shape and ControlStream schema seed shape are discovered under mutation gate.
  - Command fixture evidence remains blocked by OSH acknowledgement timeout; full positive local OSH closure is not claimed.
- Focused Maven:
  - Command: Docker Maven `mvn -B test -Dtest=VerifyPart2ObservationCommandBindingTests`.
  - Result: PASS, `13 tests / 0 failures / 0 errors / 0 skipped`.
- Full Maven:
  - Artifact: `ops/test-results/sprint-ets-33-generator-maven-2026-06-02.log`.
  - Result: PASS, `288 tests / 0 failures / 0 errors / 3 skipped`.
- Mutation-gated local OSH seed discovery:
  - Gate: `SMOKE_MUTATION_TESTS_ENABLED=true`, `SMOKE_MUTATION_IUT_POLICY=dedicated-mutable-iut`, local OSH only.
  - Observation artifact: `ops/test-results/sprint-ets-33-generator-local-osh-observation-seed-probe-2026-06-02.json`.
  - Command timeout artifact: `ops/test-results/sprint-ets-33-generator-local-osh-command-timeout-probe-2026-06-02.json`.
  - Seed shape artifact: `ops/test-results/sprint-ets-33-generator-local-osh-seed-shape-probe-2026-06-02.json`.
  - DataStream schema variants artifact: `ops/test-results/sprint-ets-33-generator-local-osh-datastream-schema-variants-2026-06-02.json`.
  - Accepted Observation seed shape: DataStream `application/om+json` plus SWE DataRecord `resultSchema`; Observation `application/om+json` with `result.temperature`; both HTTP 201 and cleanup HTTP 204.
  - Accepted ControlStream seed shape: `application/json` ControlStream schema with SWE DataRecord `parametersSchema`; HTTP 201 and cleanup HTTP 204.
  - Blocker: Command POST timed out waiting for local OSH command acknowledgement; nested Commands remained empty.
- Mandatory local OSH TeamEngine Generator smoke:
  - Command: `SMOKE_AUTH_CREDENTIAL=<derived, not logged> SMOKE_CONTAINER_NAME=ets-csapi-s33-generator-local-osh SMOKE_TARGET=local-osh SMOKE_DOCKER_NETWORK=field-hub_default SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api SMOKE_OUTPUT_DIR=/tmp/sprint-ets-33-generator-local-osh-2026-06-02-results bash scripts/smoke-test.sh`.
  - Result: PASS, `211 total / 68 passed / 0 failed / 143 skipped`; rerun after Raze concern reconciliation.
  - Report: `ops/test-results/sprint-ets-33-generator-local-osh-smoke-2026-06-02.xml`.
  - Container log: `ops/test-results/sprint-ets-33-generator-local-osh-container-2026-06-02.log`.
  - Console log: `ops/test-results/sprint-ets-33-generator-local-osh-smoke-console-2026-06-02.log`.
  - No-mutation artifact: `ops/test-results/sprint-ets-33-generator-local-osh-no-mutation-2026-06-02.txt`.
  - No-mutation evidence: `recognized_iut_request_logs=135`, `GET=133`, `OPTIONS=2`, `POST=0`, `PUT=0`, `PATCH=0`, and `DELETE=0`.
- Raze implementation review:
  - Artifact: `.harness/evaluations/sprint-ets-33-adversarial-implementation.yaml`.
  - Initial verdict: `APPROVE_WITH_CONCERNS`, no required fixes.
  - Focused recheck: `PASS`, no required fixes.
  - Reconciled concerns: added direct `result` alias mismatch regression and surfaced current full-Maven evidence in OpenSpec.

Sprint ets-33 local OSH dynamic-data seed fixture planning:

- Status:
  - Story: `epics/stories/s-ets-33-01-local-osh-dynamic-data-seed-fixtures-planning.md`
  - Contract: `.harness/contracts/sprint-ets-33.yaml`
  - Planning has drafted the OpenSpec scenarios, traceability mapping, planned seed manifest, and local OSH dynamic-data probe artifact.
  - Planning E2E passed; Raze planning recheck approved.
- Local OSH target:
  - Host URL: `http://localhost:8081/sensorhub/api`.
  - TeamEngine Docker-network URL: `http://field-hub-osh-1:8081/sensorhub/api`.
  - Docker network: `field-hub_default`.
  - Credential handling: Basic auth derived from local OSH config or environment; credential values are not recorded.
- Architecture freshness:
  - `_bmad/architecture.md` last reconciled 2026-06-01; checked 2026-06-02 and not stale.
- Local OSH dynamic-data probes:
  - Artifact: `ops/test-results/sprint-ets-33-plan-local-osh-dynamic-data-probes-2026-06-02.txt`.
  - Existing System: `/systems/040g`.
  - `/datastreams?limit=5`: HTTP 200 `application/json`, empty `items`.
  - `/observations?limit=5`: HTTP 200 `application/json`, empty `items`.
  - `/controlstreams?limit=5`: HTTP 200 `application/json`, empty `items`.
  - `/systems/040g/datastreams?limit=5`: HTTP 200 `application/json`, empty `items`.
  - `/systems/040g/controlstreams?limit=5`: HTTP 200 `application/json`, empty `items`.
  - `/commands?limit=5`: HTTP 400 `application/json`, invalid resource name.
  - `OPTIONS` on dynamic-data collection paths returned HTTP 200 and advertised broad write methods.
- Official API contract probe:
  - Source: `https://opengeospatial.github.io/ogcapi-connected-systems/api/part2/openapi/openapi-connectedsystems-2.yaml`.
  - Relevant POST paths: `/systems/{systemId}/datastreams`, `/datastreams/{dataStreamId}/observations`, `/systems/{systemId}/controlstreams`, `/controlstreams/{controlStreamId}/commands`, `/commands/{cmdId}/status`, and `/commands/{cmdId}/result`.
  - Caveat: referenced JSON schemas include required fields that may be readOnly; Generator must discover accepted local OSH payload shape before recording exact fixtures.
- Planned seed manifest:
  - Artifact: `ops/local-osh-dynamic-data-seed-fixtures.json`.
  - Status: `PLANNED_NOT_APPLIED`.
  - No local OSH dynamic-data mutation was performed during planning.
- Planning TeamEngine E2E:
  - Command: `SMOKE_AUTH_CREDENTIAL=<derived, not logged> SMOKE_CONTAINER_NAME=ets-csapi-s33-plan-local-osh SMOKE_TARGET=local-osh SMOKE_DOCKER_NETWORK=field-hub_default SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api SMOKE_OUTPUT_DIR=/tmp/sprint-ets-33-plan-local-osh-2026-06-02-results bash scripts/smoke-test.sh`.
  - Result: PASS, `211 total / 68 passed / 0 failed / 143 skipped`.
  - Report: `ops/test-results/sprint-ets-33-plan-local-osh-smoke-2026-06-02.xml`.
  - Container log: `ops/test-results/sprint-ets-33-plan-local-osh-container-2026-06-02.log`.
  - No-mutation artifact: `ops/test-results/sprint-ets-33-plan-local-osh-no-mutation-2026-06-02.txt`.
  - No-mutation evidence: `recognized_iut_request_logs=135`, `GET=133`, `OPTIONS=2`, `POST=0`, `PUT=0`, `PATCH=0`, and `DELETE=0`.
  - Disposition: accepted Sprint 33 planning E2E gate for read-only seed-fixture planning.
- Raze planning review:
  - Artifact: `.harness/evaluations/sprint-ets-33-plan-adversarial.yaml`.
  - Initial verdict: `GAPS_FOUND`, confidence 0.88, subagent `019e85e6-c8d6-7942-91b1-9bab1a38bdba`.
  - Required fixes applied: `REQ-ETS-TEAMENGINE-006` traceability now maps Sprint 33; story and epic include explicit Sprint 33 scenario IDs; manifest now forbids GeoRobotix/public/shared IUT mutation classes.
  - Focused recheck verdict: `APPROVE`, confidence 0.94, no required fixes.

Sprint ets-32 local OSH primary E2E and Observation/Command binding Generator:

- Status:
  - Story: `epics/stories/s-ets-32-01-part2-observation-binding-local-osh-planning.md`
  - Contract: `.harness/contracts/sprint-ets-32.yaml`
  - Planning is Raze-approved; Generator initial Raze review found gaps, the gapfixes are implemented, post-Raze Maven plus local OSH TeamEngine verification is complete, and focused Raze recheck returned `APPROVE_WITH_CONCERNS` with no required fixes.
  - GeoRobotix public instance is no longer a default or required development target; it is advisory-only.
  - Local OSH is the primary development IUT for sprint E2E.
- Official OGC source verification:
  - Source: `https://docs.ogc.org/is/23-002/23-002.html`, verified 2026-06-01.
  - OGC 23-002 lists the Part 2 conformance classes but does not define `/conf/observation-binding`.
  - `REQ-ETS-PART2-013` is therefore an internal project closure tying Observation bodies to parent DataStream schema evidence and Command-side bodies to parent ControlStream schema evidence.
- Local OSH target:
  - Host URL: `http://localhost:8081/sensorhub/api`.
  - TeamEngine Docker-network URL: `http://field-hub-osh-1:8081/sensorhub/api`.
  - Docker network: `field-hub_default`.
  - Credential handling: Basic auth derived from local OSH config or environment; credential values are not recorded.
- Local OSH planning probes:
  - Artifact: `ops/test-results/sprint-ets-32-plan-local-osh-probes-2026-06-01.txt`.
  - `/conformance`: HTTP 200.
  - Declared relevant Part 2 classes: `/conf/datastream`, `/conf/controlstream`, `/conf/json`, `/conf/swecommon-json`, `/conf/swecommon-text`, `/conf/swecommon-binary`, `/conf/create-replace-delete`, and `/conf/system-event`.
  - Non-standard vendor class observed: `/conf/system-history`; not OGC Part 2 PASS evidence.
  - `/datastreams?limit=1`: HTTP 200 `application/json`, empty `items`.
  - `/observations?limit=1`: HTTP 200 `application/json`, empty `items`.
  - `/controlstreams?limit=1`: HTTP 200 `application/json`, empty `items`.
  - `/commands?limit=1`: HTTP 400 `application/json`.
  - `/systemEvents?limit=1`: HTTP 400 `application/json`.
- Planning TeamEngine E2E:
  - Command: `SMOKE_AUTH_CREDENTIAL=<derived, not logged> SMOKE_CONTAINER_NAME=ets-csapi-s32-plan-local-osh SMOKE_TARGET=local-osh SMOKE_DOCKER_NETWORK=field-hub_default SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api SMOKE_OUTPUT_DIR=/tmp/sprint-ets-32-local-osh-plan-2026-06-01-results bash scripts/smoke-test.sh`.
  - Result: PASS, `206 total / 65 passed / 0 failed / 141 skipped`.
  - Report: `ops/test-results/sprint-ets-32-plan-local-osh-smoke-2026-06-01.xml`.
  - Container log: `ops/test-results/sprint-ets-32-plan-local-osh-container-2026-06-01.log`.
  - No-mutation artifact: `ops/test-results/sprint-ets-32-plan-local-osh-no-mutation-2026-06-01.txt`.
  - No-mutation evidence: `recognized_iut_request_logs=132`, `GET=130`, `OPTIONS=2`, `POST=0`, `PUT=0`, `PATCH=0`, and `DELETE=0`.
  - Disposition: accepted Sprint 32 planning E2E gate for local-OSH-backed development policy and planning-only docs.
- Generator implementation:
  - Runtime class: `src/main/java/org/opengis/cite/ogcapiconnectedsystems10/conformance/part2/binding/Part2ObservationCommandBindingTests.java`.
  - Helper regressions: `src/test/java/org/opengis/cite/ogcapiconnectedsystems10/conformance/part2/binding/VerifyPart2ObservationCommandBindingTests.java`.
  - TestNG wiring: group `part2binding` in `src/main/resources/org/opengis/cite/ogcapiconnectedsystems10/testng.xml`, with structural lint in `VerifyTestNGSuiteDependency`.
  - Positive binding closure requires documented seed fixtures or existing candidate DataStreams, Observations, ControlStreams, and Command-side resources.
  - Empty local OSH collections cannot PASS; the runtime checks SKIP positive binding checks with exact empty-IUT-state reasons until seed fixtures exist.
- Generator verification:
  - Formatter: PASS via Docker Maven `spring-javaformat:apply`.
  - Initial focused Maven: PASS, `83 tests / 0 failures / 0 errors / 0 skipped`.
  - Initial full Maven: PASS, `282 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-32-maven-2026-06-02.log`.
  - Post-Raze gapfix focused Maven: PASS, `83 tests / 0 failures / 0 errors / 0 skipped`; log archived at `ops/test-results/sprint-ets-32-focused-maven-postraze-2026-06-02.log`.
  - Post-Raze gapfix full Maven: PASS, `282 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-32-maven-postraze-2026-06-02.log`.
  - Docker bridge note: initial wrapper/cache attempts stalled during Maven Central dependency resolution; the accepted Maven runs used Docker host networking and a persistent `/tmp` Maven cache.
  - Initial TeamEngine Generator E2E: PASS, authenticated local OSH smoke `211 total / 68 passed / 0 failed / 143 skipped`.
  - Initial Generator E2E artifacts: `ops/test-results/sprint-ets-32-generator-local-osh-smoke-2026-06-02.xml`, `ops/test-results/sprint-ets-32-generator-local-osh-container-2026-06-02.log`, and `ops/test-results/sprint-ets-32-generator-local-osh-no-mutation-2026-06-02.txt`.
  - Post-Raze gapfix TeamEngine Generator E2E: PASS, authenticated local OSH smoke `211 total / 68 passed / 0 failed / 143 skipped`.
  - Post-Raze Generator E2E artifacts: `ops/test-results/sprint-ets-32-generator-local-osh-smoke-postraze-2026-06-02.xml`, `ops/test-results/sprint-ets-32-generator-local-osh-container-postraze-2026-06-02.log`, and `ops/test-results/sprint-ets-32-generator-local-osh-no-mutation-postraze-2026-06-02.txt`.
  - Generator no-mutation oracle after gapfix: PASS, `recognized_iut_request_logs=135`, `GET=133`, `OPTIONS=2`, `POST=0`, `PUT=0`, `PATCH=0`, and `DELETE=0`.
  - New `part2binding` runtime outcome on current local OSH: 3 PASS methods and 2 SKIP positive-closure methods because local OSH has no candidate dynamic-data Observation/Command evidence.
- Raze implementation review:
  - Artifact: `.harness/evaluations/sprint-ets-32-adversarial-implementation.yaml`.
  - Initial verdict: `GAPS_FOUND`, confidence 0.86, subagent `019e85bc-46d2-7423-971b-e7b93a978bcd`.
  - Required fixes applied: Command-side closure now checks available CommandStatus/CommandResult inline data against parent ControlStream status/result schemas; unavailable schema/resource endpoints and uninspectable parent schemas now SKIP instead of failing when no concrete evidence exists; stale story wording was corrected.
  - Focused recheck verdict: `APPROVE_WITH_CONCERNS`, confidence 0.91, no required fixes, no files modified by reviewer.
  - Concern: add dedicated helper regressions for the new inline CommandStatus/CommandResult skip/fail matrix when extending populated-IUT positive closure coverage.
- Raze planning review:
  - Artifact: `.harness/evaluations/sprint-ets-32-plan-adversarial.yaml`.
  - Final verdict: `APPROVE`, confidence 0.94.
  - Initial required fixes: `RAZE-ETS32-PLAN-GAP-001` for missing non-secret `SMOKE_AUTH_CREDENTIAL` placeholders in primary local OSH command snippets, and `RAZE-ETS32-PLAN-GAP-002` for superseded 2026-05-27 duplicate artifacts.
  - Closure: `AGENTS.md` and `ops/e2e-test-plan.md` now include `SMOKE_AUTH_CREDENTIAL="$SMOKE_AUTH_CREDENTIAL"` in primary local OSH E2E commands; superseded 2026-05-27 Sprint 32 duplicate artifacts were removed, leaving the canonical 2026-06-01 evidence only.

Sprint ets-31 Part 2 SWE Common Binary Encoding Generator:

- Status:
  - Story: `epics/stories/s-ets-31-01-part2-swecommon-binary-planning.md`
  - Contract: `.harness/contracts/sprint-ets-31.yaml`
  - Generator code is implemented as a PARTIAL read-only subset.
  - TeamEngine Generator E2E against GeoRobotix is captured as a failed public check: `206 total / 35 passed / 34 failed / 137 skipped`.
  - No accepted zero-failure Sprint 31 E2E gate exists yet.
- Official OGC source verification:
  - Source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 16.4 "Requirements Class SWE Common Binary Encoding" and Annex A.12.
  - Requirements class identifier: `/req/swecommon-binary`.
  - Conformance class identifier: `/conf/swecommon-binary`.
  - Prerequisite: SWE Common 3.0 Binary Encoding Rules.
  - Media type: exact `application/swe+binary`.
  - Selected requirement set: Requirements 123-130, `/req/swecommon-binary/mediatype-read`, `/req/swecommon-binary/mediatype-write`, `/req/swecommon-binary/obsschema-schema`, `/req/swecommon-binary/obsschema-mapping`, `/req/swecommon-binary/observation-encoding`, `/req/swecommon-binary/cmdschema-schema`, `/req/swecommon-binary/cmdschema-mapping`, and `/req/swecommon-binary/command-encoding`.
  - Resource condition gates: Observation-side assertions require `/conf/datastream`; Command-side assertions require `/conf/controlstream`; mediatype-write requires `/conf/create-replace-delete` and non-mutating API-definition evidence.
  - Source inconsistencies: Clause 16.4's media-type note says "SWE Common Text encoding" while discussing binary media, and Annex A.127/A.130 mention Text encoding rules for binary Observation/Command validation. Generator must not use `TextEncoding`, text validators, or `application/swe+text` as SWE Common Binary PASS evidence.
- GeoRobotix planning probes:
  - Raw transcript: `ops/test-results/sprint-ets-31-plan-georobotix-swebinary-probes-2026-05-27.txt`.
  - `/conformance`: declares Part 2 `/conf/swecommon-binary`, `/conf/swecommon-text`, `/conf/swecommon-json`, `/conf/datastream`, `/conf/controlstream`, `/conf/create-replace-delete`, and `/conf/json`.
  - `/conformance`: does not declare SWE 3.0 `/conf/binary-encoding-rules`, `/conf/text-encoding-rules`, or `/conf/json-encoding-rules`, Part 2 `/conf/api-common`, `/conf/update`, or `/conf/advanced-filtering`.
  - `GET /datastreams?limit=1` with `Accept: application/json`: HTTP 500 `application/json`.
  - `GET /datastreams?limit=1` with `Accept: application/swe+binary`: HTTP 500 with no body.
  - `GET /observations?limit=1` with `Accept: application/swe+binary`: HTTP 500 with no body.
  - `GET /controlstreams?limit=1`: HTTP 200 `application/json`, first ID `0m4qpft9sdag`, with formats including `application/swe+binary`.
  - `GET /controlstreams/0m4qpft9sdag/schema?cmdFormat=application/swe+binary`: HTTP 200, but the body reported `commandFormat=application/json` and `parametersSchema`, not `commandFormat=application/swe+binary`, `recordSchema`, and `BinaryEncoding`.
  - `GET /controlstreams/0m4qpft9sdag/commands?limit=1` with `Accept: application/swe+binary`: HTTP 200 `application/json` with empty `items`.
  - `GET /commands?limit=1` with `Accept: application/swe+binary`: HTTP 400.
- Implementation:
  - `Part2SweCommonBinaryTests` adds exact `/conf/swecommon-binary` declaration, SWE 3.0 `/conf/binary-encoding-rules` prerequisite visibility, `/conf/datastream`/`/conf/controlstream`/`/conf/create-replace-delete` resource condition gates, read-only `application/swe+binary` media checks, bundled `observationSchemaSwe.json` and `commandSchemaSwe.json` metadata validation, canonical Time/IssueTime definition guards, Observation/Command encoding guards, and non-mutating mediatype-write API-definition checks scoped to Observation/Command resource endpoints.
  - `VerifyPart2SweCommonBinaryTests` adds 11 helper regressions.
  - `testng.xml` and `VerifyTestNGSuiteDependency` wire and lint `part2swecommonbinary` with `core common`.
- Planning TeamEngine E2E smoke:
  - Command: `SMOKE_CONTAINER_NAME=ets-csapi-s31-swebinary-plan-georobotix SMOKE_OUTPUT_DIR=/tmp/sprint-ets-31-swebinary-plan-georobotix-results bash scripts/smoke-test.sh`.
  - Result: FAILED, `196 total / 33 passed / 28 failed / 135 skipped`.
  - Report: `ops/test-results/sprint-ets-31-plan-georobotix-smoke-failed-2026-05-27.xml`.
  - Container log: `ops/test-results/sprint-ets-31-plan-georobotix-smoke-container-failed-2026-05-27.log`.
  - Console log: `ops/test-results/sprint-ets-31-plan-georobotix-smoke-console-failed-2026-05-27.log`.
  - Failure cause: known public-IUT read-path and schema-validation failures; no Sprint 31 SWE Common Binary runtime tests exist yet.
  - Public-IUT safety check: `scripts/no-mutation-oracle.py` recognized 91 IUT request logs; explicit counts are archived at `ops/test-results/sprint-ets-31-plan-georobotix-no-mutation-2026-05-27.txt` as `GET=91`, `POST=0`, `PUT=0`, `PATCH=0`, `DELETE=0`.
  - Disposition: mandatory public smoke run, failed external/public-IUT evidence, not an accepted zero-failure E2E gate.
- Generator verification:
  - Formatter: Docker Maven `mvn -B spring-javaformat:apply` returned BUILD SUCCESS.
  - Focused Maven: Docker Maven `mvn -B test -Dtest=VerifyPart2SweCommonBinaryTests,VerifyTestNGSuiteDependency` returned BUILD SUCCESS with `84 tests / 0 failures / 0 errors / 0 skipped`; log archived at `ops/test-results/sprint-ets-31-focused-maven-2026-05-27.log`.
  - Full Maven: `bash scripts/mvn-test-via-docker.sh` returned BUILD SUCCESS with `272 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-31-full-maven-2026-05-27.log`.
  - Mandatory GeoRobotix TeamEngine Generator smoke command: `SMOKE_CONTAINER_NAME=ets-csapi-s31-swebinary-generator-georobotix SMOKE_OUTPUT_DIR=/tmp/sprint-ets-31-swebinary-generator-georobotix-results bash scripts/smoke-test.sh`.
  - Mandatory GeoRobotix TeamEngine Generator smoke result: FAILED, `206 total / 35 passed / 34 failed / 137 skipped`.
  - Generator report: `ops/test-results/sprint-ets-31-generator-georobotix-smoke-failed-2026-05-27.xml`.
  - Generator container log: `ops/test-results/sprint-ets-31-generator-georobotix-smoke-container-failed-2026-05-27.log`.
  - Generator console log: `ops/test-results/sprint-ets-31-generator-georobotix-smoke-console-failed-2026-05-27.log`.
  - New SWE Common Binary group outcome: 3 PASS, 6 FAIL, and 2 SKIP.
  - Failure cause: GeoRobotix lacks SWE 3.0 `/conf/binary-encoding-rules`, Observation-side SWE Binary reads return HTTP 500, and Command-side checks fail existing `/controlstreams` schema validation before SWE Common Command Schema PASS evidence.
  - Public-IUT safety check: `scripts/no-mutation-oracle.py` recognized 99 IUT request logs; explicit counts found 99 GeoRobotix GET request lines and zero matched GeoRobotix POST/PUT/PATCH/DELETE request lines. Counts artifact: `ops/test-results/sprint-ets-31-generator-georobotix-no-mutation-2026-05-27.txt`.
- Raze planning review:
  - Artifact: `.harness/evaluations/sprint-ets-31-plan-adversarial.yaml`.
  - Verdict: `APPROVE_WITH_CONCERNS`, confidence 0.93.
  - Required fix: `RAZE-ETS31-PLAN-CONCERN-001`, a low post-review bookkeeping reconciliation; closed in this turn by updating the contract, story, handoff, ops status, test-results, changelog, and epic/status wording.
  - Remaining concerns: public GeoRobotix smoke remains failed evidence only.
- Raze implementation review:
  - Artifact: `.harness/evaluations/sprint-ets-31-adversarial-implementation.yaml`.
  - Verdict: `APPROVE_WITH_CONCERNS`, confidence 0.91.
  - Required fix: `RAZE-ETS31-IMPL-CONCERN-001`, a low post-review bookkeeping and stale fresh-session pointer reconciliation; closed after the review by updating the contract, story, handoff, traceability header, ops status, test-results, and changelog.
  - Code-level required fixes: none.
- Commit/push:
  - Planning commit `20a8e18 Plan Sprint 31 Part 2 SWE Common Binary` was pushed over SSH on 2026-05-27 (`45717a3..20a8e18 main -> main`).
  - Generator commit `b520b65 Implement Sprint 31 Part 2 SWE Common Binary` was pushed over SSH on 2026-05-27 (`75eac75..b520b65 main -> main`).

Sprint ets-30 Part 2 SWE Common Text Encoding Generator:

- Status:
  - Story: `epics/stories/s-ets-30-01-part2-swecommon-text-planning.md`
  - Contract: `.harness/contracts/sprint-ets-30.yaml`
  - Generator code is implemented as a PARTIAL read-only subset.
  - TeamEngine Generator E2E against GeoRobotix is captured as a failed public check: `196 total / 33 passed / 28 failed / 135 skipped`.
  - No accepted zero-failure Sprint 30 E2E gate exists yet.
- Official OGC source verification:
  - Source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 16.3 "Requirements Class SWE Common Text Encoding" and Annex A.11.
  - Requirements class identifier: `/req/swecommon-text`.
  - Conformance class identifier: `/conf/swecommon-text`.
  - Prerequisite: SWE Common 3.0 Text Encoding Rules.
  - Media type: exact `application/swe+text`.
  - Selected requirement set: Requirements 115-122, `/req/swecommon-text/mediatype-read`, `/req/swecommon-text/mediatype-write`, `/req/swecommon-text/obsschema-schema`, `/req/swecommon-text/obsschema-mapping`, `/req/swecommon-text/observation-encoding`, `/req/swecommon-text/cmdschema-schema`, `/req/swecommon-text/cmdschema-mapping`, and `/req/swecommon-text/command-encoding`.
  - Resource condition gates: Observation-side assertions require `/conf/datastream`; Command-side assertions require `/conf/controlstream`; mediatype-write requires `/conf/create-replace-delete` and non-mutating API-definition evidence.
  - Source inconsistency: Annex A.115's API-definition bullet mentions `application/swe+binary`, but Clause 16.3 and the retrieval/content-type steps use `application/swe+text`; Generator must not use binary advertisement as SWE Common Text PASS evidence.
- Implementation:
  - `Part2SweCommonTextTests` adds exact `/conf/swecommon-text` declaration, SWE 3.0 `/conf/text-encoding-rules` prerequisite visibility, `/conf/datastream`/`/conf/controlstream`/`/conf/create-replace-delete` resource condition gates, read-only `application/swe+text` media checks, bundled `observationSchemaSwe.json` and `commandSchemaSwe.json` metadata validation, canonical Time/IssueTime definition guards, Observation/Command encoding guards, and non-mutating mediatype-write API-definition checks scoped to Observation/Command resource endpoints.
  - `VerifyPart2SweCommonTextTests` adds 11 helper regressions.
  - `testng.xml` and `VerifyTestNGSuiteDependency` wire and lint `part2swecommontext` with `core common`.
- GeoRobotix planning probes:
  - Raw transcript: `ops/test-results/sprint-ets-30-plan-georobotix-swetext-probes-2026-05-26.txt`.
  - `/conformance`: declares Part 2 `/conf/swecommon-text`, `/conf/swecommon-json`, `/conf/swecommon-binary`, `/conf/datastream`, `/conf/controlstream`, `/conf/create-replace-delete`, and `/conf/json`.
  - `/conformance`: does not declare SWE 3.0 `/conf/text-encoding-rules`, `/conf/json-encoding-rules`, or `/conf/binary-encoding-rules`, Part 2 `/conf/api-common`, `/conf/update`, or `/conf/advanced-filtering`.
  - `GET /datastreams?limit=1` with `Accept: application/json`: HTTP 500 `application/json`.
  - `GET /datastreams?limit=1` with `Accept: application/swe+text`: HTTP 500.
  - `GET /observations?limit=1` with `Accept: application/swe+text`: HTTP 500.
  - `GET /controlstreams?limit=1`: HTTP 200 `application/json`, first ID `0m4qpft9sdag`, with formats including `application/swe+csv` but not `application/swe+text`.
  - `GET /controlstreams/0m4qpft9sdag/schema?cmdFormat=application/swe+text`: HTTP 200, but the body reported `commandFormat=application/json` and `parametersSchema`, not `commandFormat=application/swe+text`, `recordSchema`, and `TextEncoding`.
  - `GET /controlstreams/0m4qpft9sdag/commands?limit=1` with `Accept: application/swe+text`: HTTP 200 `application/json` with empty `items`.
  - `GET /commands?limit=1` with `Accept: application/swe+text`: HTTP 400.
- Local OSH planning limit:
  - `field-hub-osh-1` is running but unhealthy.
  - Current shell has no `SMOKE_AUTH_CREDENTIAL`.
  - Unauthenticated `GET /sensorhub/api/conformance`: HTTP 401.
- Planning TeamEngine E2E smoke:
  - Command: `SMOKE_CONTAINER_NAME=ets-csapi-s30-swetext-plan-georobotix SMOKE_OUTPUT_DIR=/tmp/sprint-ets-30-swetext-plan-georobotix-results bash scripts/smoke-test.sh`.
  - Result: FAILED, `186 total / 31 passed / 22 failed / 133 skipped`.
  - Report: `ops/test-results/sprint-ets-30-plan-georobotix-smoke-failed-2026-05-26.xml`.
  - Container log: `ops/test-results/sprint-ets-30-plan-georobotix-smoke-container-failed-2026-05-26.log`.
  - Failure cause: known public-IUT read-path and schema-validation failures; no Sprint 30 SWE Common Text runtime tests exist yet.
  - Public-IUT safety check: `scripts/no-mutation-oracle.py` recognized 83 IUT request logs; explicit container-log grep found 83 GeoRobotix GET request lines and zero matched GeoRobotix POST/PUT/PATCH/DELETE request lines.
  - Disposition: mandatory public smoke run, failed external/public-IUT evidence, not an accepted zero-failure E2E gate.
- Raze planning review:
  - Artifact: `.harness/evaluations/sprint-ets-30-plan-adversarial.yaml`.
  - Verdict: `APPROVE_WITH_CONCERNS`, confidence 0.93.
  - Required fixes: none.
  - Low concern: review bookkeeping was pending because Raze was explicitly read-only; post-review reconciliation closed it.
- Generator verification:
  - Formatter: Docker Maven `mvn -B spring-javaformat:apply` returned BUILD SUCCESS.
  - Focused Maven: Docker Maven `mvn -B test -Dtest=VerifyPart2SweCommonTextTests,VerifyTestNGSuiteDependency` returned BUILD SUCCESS with `81 tests / 0 failures / 0 errors / 0 skipped`.
  - Full Maven: Docker Maven `mvn -B clean test` returned BUILD SUCCESS with `258 tests / 0 failures / 0 errors / 3 skipped`; log archived at `ops/test-results/sprint-ets-30-maven-2026-05-27.log`.
  - Mandatory GeoRobotix TeamEngine Generator smoke command: `SMOKE_CONTAINER_NAME=ets-csapi-s30-swetext-generator-georobotix SMOKE_OUTPUT_DIR=/tmp/sprint-ets-30-swetext-generator-georobotix-results bash scripts/smoke-test.sh`.
  - Mandatory GeoRobotix TeamEngine Generator smoke result: FAILED, `196 total / 33 passed / 28 failed / 135 skipped`.
  - Generator report: `ops/test-results/sprint-ets-30-generator-georobotix-smoke-failed-2026-05-27.xml`.
  - Generator container log: `ops/test-results/sprint-ets-30-generator-georobotix-smoke-container-failed-2026-05-27.log`.
  - Generator console log: `ops/test-results/sprint-ets-30-generator-georobotix-smoke-console-failed-2026-05-27.log`.
  - New SWE Common Text group outcome: 2 PASS, 6 FAIL, and 2 SKIP.
  - Failure cause: GeoRobotix lacks SWE 3.0 `/conf/text-encoding-rules`, Observation-side SWE Text reads return HTTP 500, and Command-side checks fail existing `/controlstreams` schema validation before SWE Common Command Schema PASS evidence.
  - Public-IUT safety check: `scripts/no-mutation-oracle.py` recognized 91 IUT request logs; explicit counts found 91 GeoRobotix GET request lines and zero matched GeoRobotix POST/PUT/PATCH/DELETE request lines.
- Raze implementation review:
  - Artifact: `.harness/evaluations/sprint-ets-30-adversarial-implementation.yaml`.
  - Initial verdict: `GAPS_FOUND`, confidence 0.88, for missing test-code traceability comments on `SCENARIO-ETS-PART2-011-ANNEX-MEDIATYPE-HONESTY-001` and `SCENARIO-ETS-PART2-011-UNAVAILABLE-ENDPOINT-HONESTY-001`.
  - Gapfix verification: formatter returned BUILD SUCCESS, focused Maven returned `81 tests / 0 failures / 0 errors / 0 skipped`, and full Maven returned `258 tests / 0 failures / 0 errors / 3 skipped`.
  - Focused recheck verdict: `APPROVE_WITH_CONCERNS`, confidence 0.93, no required fixes remaining.
- Commit/push:
  - Planning commit `3c68858 Plan Sprint 30 Part 2 SWE Common Text` was pushed over SSH on 2026-05-26 (`6ba24db..3c68858 main -> main`).
  - Generator commit `b2aad06 Implement Sprint 30 Part 2 SWE Common Text` was pushed over SSH on 2026-05-27 (`560945c..b2aad06 main -> main`).

Sprint ets-29 Part 2 SWE Common JSON Encoding Generator:

- Status:
  - Story: `epics/stories/s-ets-29-01-part2-swecommon-json-planning.md`
  - Contract: `.harness/contracts/sprint-ets-29.yaml`
  - Generator code is implemented as a PARTIAL read-only subset.
  - TeamEngine Generator E2E against GeoRobotix is captured as a failed public check: `186 total / 31 passed / 22 failed / 133 skipped`.
  - No accepted zero-failure Sprint 29 E2E gate exists yet.
- Official OGC source verification:
  - Source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 16.2 "Requirements Class SWE Common JSON Encoding" and Annex A.10.
  - Requirements class identifier: `/req/swecommon-json`.
  - Conformance class identifier: `/conf/swecommon-json`.
  - Prerequisite: SWE Common 3.0 JSON Encoding Rules.
  - Media type: exact `application/swe+json`.
  - Selected requirement set: Requirements 107-114, `/req/swecommon-json/mediatype-read`, `/req/swecommon-json/mediatype-write`, `/req/swecommon-json/obsschema-schema`, `/req/swecommon-json/obsschema-mapping`, `/req/swecommon-json/observation-encoding`, `/req/swecommon-json/cmdschema-schema`, `/req/swecommon-json/cmdschema-mapping`, and `/req/swecommon-json/command-encoding`.
  - Resource condition gates: Observation-side assertions require `/conf/datastream`; Command-side assertions require `/conf/controlstream`; mediatype-write requires `/conf/create-replace-delete` and non-mutating API-definition evidence.
- GeoRobotix planning probes:
  - `/conformance`: declares Part 2 `/conf/swecommon-json`, `/conf/swecommon-text`, `/conf/swecommon-binary`, `/conf/datastream`, `/conf/controlstream`, `/conf/create-replace-delete`, and `/conf/json`.
  - `/conformance`: does not declare SWE 3.0 `/conf/json-encoding-rules`, Part 2 `/conf/api-common`, `/conf/update`, or `/conf/advanced-filtering`.
  - `GET /datastreams?limit=1` with `Accept: application/json`: HTTP 500 `application/json`.
  - `GET /datastreams?limit=1` with `Accept: application/swe+json`: HTTP 500 `application/json`.
  - `GET /observations?limit=1` with `Accept: application/swe+json`: HTTP 500 `application/json`.
  - `GET /controlstreams?limit=1`: HTTP 200 `application/json`, first ID `0m4qpft9sdag`, with formats including `application/swe+json`.
  - `GET /controlstreams/0m4qpft9sdag/schema?cmdFormat=application/swe+json`: HTTP 200, but the body reported `commandFormat=application/json` and `parametersSchema`, not `commandFormat=application/swe+json`, `recordSchema`, and `JSONEncoding`.
  - `GET /controlstreams/0m4qpft9sdag/commands?limit=1` with `Accept: application/swe+json`: HTTP 200 JSON with empty `items`.
  - `GET /commands?limit=1` and `GET /systemEvents?limit=1` with `Accept: application/swe+json`: HTTP 400 JSON.
- Generator implementation:
  - `Part2SweCommonJsonTests`: exact `/conf/swecommon-json` declaration, SWE 3.0 `/conf/json-encoding-rules` prerequisite visibility, `/conf/datastream`/`/conf/controlstream`/`/conf/create-replace-delete` condition gates, read-only `application/swe+json` content-type checks, bundled `observationSchemaSwe.json` and `commandSchemaSwe.json` validation, canonical Time/IssueTime definition evidence, Observation/Command encoding guards, and non-mutating mediatype-write API-definition checks scoped to Observation/Command resource endpoints.
  - `VerifyPart2SweCommonJsonTests`: 11 helper regressions covering official identifiers, condition gates, content-type rules, bundled Annex A.10 schema loading, `JSONEncoding`, canonical Time/IssueTime evidence, API-definition request-body parsing, unrelated/subresource path rejection, OPTIONS/vendor-media rejection, and stable group naming.
  - `testng.xml`: adds `part2swecommonjson` with `core common` dependencies.
  - `VerifyTestNGSuiteDependency`: adds `part2swecommonjson` dependency, method-group, and co-location structural checks.
- Local OSH planning limit:
  - `field-hub-osh-1` is running but unhealthy.
  - Current shell has no `SMOKE_AUTH_CREDENTIAL`.
  - Unauthenticated `GET /sensorhub/api/conformance`: HTTP 401.
- Maven and formatting:
  - Formatter: Docker Maven `mvn -B spring-javaformat:apply` returned BUILD SUCCESS.
  - Focused Maven after Raze gapfix: Docker Maven with `maven:3.9-eclipse-temurin-17` and a persistent `/tmp` Maven cache returned BUILD SUCCESS with `78 tests / 0 failures / 0 errors / 0 skipped`; final log archived as `ops/test-results/sprint-ets-29-focused-postraze-2026-05-26.log`.
  - Full Maven after Raze gapfix: Docker Maven `mvn -B clean test` with the same image and persistent `/tmp` Maven cache returned BUILD SUCCESS with `244 tests / 0 failures / 0 errors / 3 skipped`; final log archived as `ops/test-results/sprint-ets-29-maven-postraze-2026-05-26.log`.
  - Network caveat: earlier full-Maven attempts were archived as `ops/test-results/sprint-ets-29-maven-network-failed-2026-05-26.log` and `ops/test-results/sprint-ets-29-maven-central-stalled-2026-05-26.log`; those failures were Maven Central transfer/reset behavior, not project test failures.
- Planning TeamEngine E2E smoke:
  - Command: `SMOKE_CONTAINER_NAME=ets-csapi-s29-swejson-plan-georobotix SMOKE_OUTPUT_DIR=/tmp/sprint-ets-29-swejson-plan-georobotix-results bash scripts/smoke-test.sh`.
  - Result: FAILED, `176 total / 29 passed / 16 failed / 131 skipped`.
  - Report: `ops/test-results/sprint-ets-29-plan-georobotix-smoke-failed-2026-05-26.xml`.
  - Container log: `ops/test-results/sprint-ets-29-plan-georobotix-smoke-container-failed-2026-05-26.log`.
  - Failure cause: known public-IUT read-path and schema-validation failures; no Sprint 29 SWE Common JSON runtime tests exist yet.
  - Public-IUT safety check: explicit container-log grep found 75 GeoRobotix GET request lines and zero matched GeoRobotix POST/PUT/PATCH/DELETE request lines. `scripts/no-mutation-oracle.py` was inconclusive because no IUT-bound request lines were recognized in this log format.
  - Disposition: mandatory public smoke run, failed external/public-IUT evidence, not an accepted zero-failure E2E gate.
- Generator TeamEngine E2E smoke:
  - Command: `SMOKE_CONTAINER_NAME=ets-csapi-s29-swejson-generator-postraze SMOKE_OUTPUT_DIR=/tmp/sprint-ets-29-swejson-generator-postraze-georobotix-results bash scripts/smoke-test.sh`.
  - Result: FAILED, `186 total / 31 passed / 22 failed / 133 skipped`.
  - Report: `ops/test-results/sprint-ets-29-generator-postraze-georobotix-smoke-failed-2026-05-26.xml`.
  - Container log: `ops/test-results/sprint-ets-29-generator-postraze-georobotix-smoke-container-failed-2026-05-26.log`.
  - New SWE Common JSON group: 2 PASS, 6 FAIL, 2 SKIP.
  - Failure cause: public GeoRobotix declares `/conf/swecommon-json` but lacks SWE 3.0 `/conf/json-encoding-rules`, returns HTTP 500 for Observation-side SWE JSON reads, and fails reachable `/controlstreams` schema validation before Command SWE JSON schema PASS evidence.
  - Public-IUT safety check: `scripts/no-mutation-oracle.py` recognized 83 IUT request logs; explicit container-log grep found 83 GeoRobotix GET request lines and zero matched GeoRobotix POST/PUT/PATCH/DELETE request lines.
  - Disposition: mandatory public smoke run, failed external/public-IUT evidence, not an accepted zero-failure E2E gate.
- Raze planning review:
  - Artifact: `.harness/evaluations/sprint-ets-29-plan-adversarial.yaml`.
  - Verdict: `APPROVE_WITH_CONCERNS`, confidence 0.93.
  - Required fixes: none.
  - Low concern: direct planning probe transcripts are summarized rather than archived as raw standalone artifacts; Generator should reproduce or archive any probe bodies used for PASS/SKIP behavior.
- Raze implementation review:
  - Artifact: `.harness/evaluations/sprint-ets-29-adversarial-implementation.yaml`.
  - Initial verdict: `GAPS_FOUND`, confidence 0.90, for noncanonical Time/IssueTime false-PASS evidence and unscoped write-operation evidence.
  - Fixes applied: mapping evidence now requires canonical definition URIs, and mediatype-write API-definition evidence is scoped to Observation/Command resource endpoints.
  - Focused recheck: `APPROVE_WITH_CONCERNS`, confidence 0.94; both required gaps closed and no required fixes remain.
- Commit/push:
  - Planning commit `690dbd3 Plan Sprint 29 Part 2 SWE Common JSON` was pushed over SSH on 2026-05-26 (`be7f1a6..690dbd3 main -> main`).
  - Generator commit `062d4b7 Implement Sprint 29 Part 2 SWE Common JSON` was pushed over SSH on 2026-05-26 (`05c0ee4..062d4b7 main -> main`).

Sprint ets-28 Part 2 JSON Encoding Generator:

- Status:
  - Story: `epics/stories/s-ets-28-01-part2-json-planning.md`
  - Contract: `.harness/contracts/sprint-ets-28.yaml`
  - Generator code is implemented as a PARTIAL read-only subset.
  - TeamEngine Generator E2E against GeoRobotix is captured as a failed public check because the public IUT still returns HTTP 500 on existing read paths and now exposes real JSON schema failures for `/controlstreams`.
  - No accepted zero-failure Sprint 28 E2E gate exists yet.
- Official OGC source verification:
  - Source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 16.1 "Requirements Class JSON Encoding" and Annex A.9.
  - Requirements class identifier: `/req/json`.
  - Conformance class identifier: `/conf/json`.
  - Prerequisite: SWE Common 3.0 JSON record components.
  - Selected requirement set: Requirements 93-106, `/req/json/mediatype-read`, `/req/json/mediatype-write`, `/req/json/datastream-schema`, `/req/json/obsschema-schema`, `/req/json/observation-schema`, `/req/json/observation-constraints`, `/req/json/controlstream-schema`, `/req/json/commandschema-schema`, `/req/json/command-schema`, `/req/json/command-constraints`, `/req/json/commandstatus-schema`, `/req/json/commandresult-schema`, `/req/json/commandresult-constraints`, and `/req/json/systemevent-schema`.
  - Resource condition gates: DataStream/Observation JSON assertions require `/conf/datastream`; ControlStream/Command/CommandStatus/CommandResult assertions require `/conf/controlstream`; SystemEvent JSON assertions require `/conf/system-event`.
- Generator implementation:
  - `Part2JsonTests`: runtime checks for exact `/conf/json` declaration, SWE prerequisite visibility, resource condition gates, read-only JSON media type compatibility, Annex A.9 schema validation, dynamic Observation/Command/CommandResult evidence guards that avoid shape-only PASS, non-mutating exact-`application/json` mediatype-write API-definition checks, and unavailable-endpoint honesty.
  - `VerifyPart2JsonTests`: 8 helper regressions covering official identifiers, condition gates, content type compatibility, bundled schema resources, classpath schema loading, service-desc request body advertisement parsing, OPTIONS/`+json` rejection for write advertisement, and stable group naming.
  - `testng.xml`: adds `part2json` with `core common` dependencies.
  - `VerifyTestNGSuiteDependency`: adds `part2json` dependency, method-group, and co-location structural checks.
  - `pom.xml`: adds `com.networknt:json-schema-validator:1.5.9` for draft 2020-12 schema validation.
- GeoRobotix planning and Generator probes:
  - `/conformance`: declares Part 2 `/conf/json`, `/conf/datastream`, `/conf/controlstream`, `/conf/system-event`, `/conf/create-replace-delete`, `/conf/swecommon-json`, `/conf/swecommon-text`, and `/conf/swecommon-binary`.
  - `/conformance`: does not declare Part 2 `/conf/api-common`, `/conf/update`, `/conf/advanced-filtering`, or SWE 3.0 `/conf/json-record-components`.
  - Current read-health probes: `GET /datastreams?limit=1` and `GET /observations?limit=1` returned HTTP 500 text/html; `GET /controlstreams?limit=1` returned HTTP 200 application/json; `GET /systemEvents?limit=1` and `GET /systems/0mqcvdnfoca0/events?limit=1` returned HTTP 400 JSON.
  - Selected ControlStream `0m4qpft9sdag` advertises `application/json`; `/controlstreams/0m4qpft9sdag/schema?cmdFormat=application/json` returned HTTP 200 with `commandFormat=application/json` and `parametersSchema`.
  - `/controlstreams/0m4qpft9sdag/commands?limit=1` returned HTTP 200 with no candidate Command item.
- Local OSH planning limit:
  - `field-hub-osh-1` is running but unhealthy.
  - Current shell has no `SMOKE_AUTH_CREDENTIAL`.
  - Unauthenticated `GET /sensorhub/api/conformance`: HTTP 401.
- Generator gate expectations:
  - Default GeoRobotix smoke must remain read-only with zero IUT-bound POST/PUT/PATCH/DELETE.
  - Exact `/conf/json` declaration is required before JSON assertions PASS.
  - SWE Common 3.0 JSON record components prerequisite must remain visible before full `/conf/json` closure.
  - Resource-specific JSON assertions must be gated by `/conf/datastream`, `/conf/controlstream`, or `/conf/system-event`.
  - Schema-validation PASS requires candidate resource or collection evidence plus the corresponding bundled schema.
  - Observation, Command, and CommandResult dynamic constraints require parent schema evidence and candidate child resource evidence, then SKIP rather than PASS until semantic parent-schema validation is implemented.
  - HTTP 400, HTTP 500, streaming-only endpoints, empty candidate collections, or text/html error bodies cannot produce JSON conformance PASS.
  - Requirement 94 `mediatype-write` is API-definition/readiness only in the first increment; OPTIONS alone cannot PASS and public GeoRobotix mutation is forbidden.
- Formatter:
  - Command: Docker Maven `mvn -B spring-javaformat:apply`
  - Result: BUILD SUCCESS
- Focused Maven:
  - Command: `bash scripts/mvn-test-via-docker.sh -Dtest=VerifyPart2JsonTests,VerifyTestNGSuiteDependency`
  - Result: BUILD SUCCESS
  - Surefire: `72 tests / 0 failures / 0 errors / 0 skipped`
- Full Maven:
  - Command: Docker Maven `mvn clean test -Dmaven.repo.local=/m2 -Dmaven.artifact.threads=1`
  - Result: BUILD SUCCESS
  - Surefire: `230 tests / 0 failures / 0 errors / 3 skipped`
  - Log: `ops/test-results/sprint-ets-28-maven-2026-05-26.log`
- Planning TeamEngine E2E smoke:
  - First attempt: `git archive` temporary copy failed before TeamEngine because Dockerfile `COPY .git ./.git` requires `.git`; rerun used a temporary Git clone.
  - Command: `SMOKE_CONTAINER_NAME=ets-csapi-s28-plan-georobotix SMOKE_OUTPUT_DIR=/tmp/sprint-ets-28-plan-georobotix-results bash scripts/smoke-test.sh`.
  - Result: `160 total / 27 passed / 5 failed / 128 skipped`.
  - Report: `ops/test-results/sprint-ets-28-plan-georobotix-smoke-failed-2026-05-26.xml`.
  - Container log: `ops/test-results/sprint-ets-28-plan-georobotix-smoke-container-failed-2026-05-26.log`.
  - Failure cause: public GeoRobotix HTTP 500 responses on existing SystemFeatures/GeoJSON/SensorML/Datastream/Observation read paths; no Part 2 JSON runtime tests exist yet.
  - Public-IUT safety check: `scripts/no-mutation-oracle.py` recognized 61 GeoRobotix IUT request logs; explicit container-log search found no matched GeoRobotix POST/PUT/PATCH/DELETE request lines.
  - Disposition: mandatory public smoke run, failed external/public-IUT evidence, not a passing E2E gate.
- Generator TeamEngine E2E smoke:
  - Command: final rerun `SMOKE_CONTAINER_NAME=ets-csapi-s28-json-georobotix-rerun SMOKE_OUTPUT_DIR=/tmp/sprint-ets-28-json-georobotix-results-rerun bash scripts/smoke-test.sh`.
  - Result: FAILED, `176 total / 29 passed / 16 failed / 131 skipped`.
  - Report: `ops/test-results/sprint-ets-28-generator-georobotix-smoke-failed-2026-05-26.xml`.
  - Container log: `ops/test-results/sprint-ets-28-generator-georobotix-smoke-container-failed-2026-05-26.log`.
  - Failure cause: public GeoRobotix still returns HTTP 500 on existing read paths and its `/controlstreams` collection reaches JSON schema validation but fails `controlStreamCollection.json`.
  - Schema-loader status: the initial failure to load bundled schemas was fixed by mapping `https://csapi-compliance.local/schemas/` to `classpath:schemas/`; the rerun has no schema-loader failures.
  - Part 2 JSON runtime outcome: exact `/conf/json` and resource condition-gate checks PASS; SWE prerequisite, mediatype-write, and SystemEvent evidence SKIP; DataStream/Observation paths fail from HTTP 500; ControlStream collection schema validation fails on actual IUT JSON.
  - Public-IUT safety check: explicit container-log grep found 75 GeoRobotix GET request lines and zero matched GeoRobotix POST/PUT/PATCH/DELETE request lines. `scripts/no-mutation-oracle.py` was inconclusive because no IUT-bound request lines were recognized in this log format.
  - Disposition: mandatory public smoke run, failed external/public-IUT evidence, not an accepted zero-failure E2E gate.
- Raze planning review:
  - Artifact: `.harness/evaluations/sprint-ets-28-plan-adversarial.yaml`.
  - Verdict: `APPROVE_WITH_CONCERNS`, confidence 0.92.
  - Required fixes: none.
  - Low concern: direct JSON-specific probe bodies are summarized but not archived as raw transcripts; Generator must reproduce or archive positive schema/readiness evidence used for PASS or SKIP behavior.
- Raze implementation review:
  - Artifact: `.harness/evaluations/sprint-ets-28-adversarial-implementation.yaml`.
  - Initial verdict: `GAPS_FOUND`, confidence 0.87, for narrow schema-loader regression coverage and stale story scope wording.
  - Fixes applied: schema-load regression now checks every Annex A.9 schema, story scope is reconciled, exact `application/json` OpenAPI content keys are required for mediatype-write, and dynamic constraint methods avoid shape-only PASS.
  - Focused recheck: `APPROVE_WITH_CONCERNS`, confidence 0.93, no required fixes.
  - Remaining concern: URI path escaping for resource IDs is a non-blocking hardening item.
- Commit/push:
  - Planning commit `5d95d55 Plan Sprint 28 Part 2 JSON` pushed over SSH on 2026-05-26 (`13b34f7..5d95d55 main -> main`).
  - Generator commit `5850210 Implement Sprint 28 Part 2 JSON` was pushed over SSH on 2026-05-26 (`ce66139..5850210 main -> main`).

Sprint ets-27 Part 2 Update Generator:

- Status:
  - Story: `epics/stories/s-ets-27-01-part2-update-planning.md`
  - Contract: `.harness/contracts/sprint-ets-27.yaml`
  - Generator code is implemented as a PARTIAL safety-gated subset.
  - TeamEngine Generator E2E against GeoRobotix is captured as a failed public check because the public IUT still returns HTTP 500 on existing read paths.
  - Authenticated local OSH TeamEngine E2E is the accepted Sprint 27 Generator gate: `160 total / 62 passed / 0 failed / 98 skipped`.
- Official OGC source verification:
  - Source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 15 "Requirements Class Update" and Annex A.8.
  - Requirements class identifier: `/req/update`.
  - Conformance class identifier: `/conf/update`.
  - Prerequisites: Part 2 `/req/create-replace-delete` and OGC API Features Part 4 `/req/update`.
  - Conformance prerequisites: Part 2 `/conf/create-replace-delete` and OGC API Features Part 4 `/conf/update`.
  - Selected planning requirement set: Requirements 79-92, `/req/update/datastream`, `/req/update/datastream-update-schema`, `/req/update/observation`, `/req/update/observation-schema`, `/req/update/controlstream`, `/req/update/controlstream-update-schema`, `/req/update/command`, `/req/update/command-schema`, `/req/update/command-status`, `/req/update/command-result`, `/req/update/feasibility`, `/req/update/feasibility-status`, `/req/update/feasibility-result`, and `/req/update/system-event`.
  - Clause 15 condition gates: R79-R82 require `/conf/datastream`, R83-R88 require `/conf/controlstream`, R89-R91 require `/conf/feasibility`, and R92 requires `/conf/system-event`; missing condition classes SKIP prerequisite-incomplete rather than PASS.
- GeoRobotix planning probes:
  - `/conformance`: declares Part 2 `/conf/create-replace-delete` and OGC API Features Part 4 `/conf/create-replace-delete`.
  - `/conformance`: does not declare Part 2 `/conf/api-common`, `/conf/update`, or `/conf/advanced-filtering`.
  - Sampled OPTIONS probes for DataStream, Observation, ControlStream, Command, Feasibility, SystemEvent, and system-scoped event endpoints: HTTP 200 with broad `Allow` headers, but PATCH absent.
  - Current read-health probes: `GET /systems/0mqcvdnfoca0`, `GET /datastreams?limit=1`, and `GET /observations?limit=1` returned HTTP 500; `GET /controlstreams?limit=1` returned HTTP 200 JSON.
- Local OSH planning probes:
  - `field-hub-osh-1` is running and requires Basic auth.
  - Unauthenticated `GET /sensorhub/api/conformance`: HTTP 401.
  - Authenticated `GET /sensorhub/api/conformance`: HTTP 200 and no Part 2 `/conf/update` declaration.
  - Authenticated `OPTIONS /sensorhub/api/systems/040g`: HTTP 200 with `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS`; PATCH absent.
- Generator implementation:
  - `Part2UpdateTests`: 14 runtime checks plus shared read-only setup for exact `/conf/update` declaration, prerequisite visibility, Clause 15 condition-gate visibility, public-IUT mutation safety, DataStream/Observation PATCH readiness and deferred lifecycle checks, ControlStream/Command PATCH readiness and deferred lifecycle checks, separate Feasibility and SystemEvent PATCH readiness and deferred lifecycle checks, unavailable-endpoint honesty, and schema-rejection honesty.
  - `VerifyPart2UpdateTests`: 9 helper regressions for official identifiers, missing condition-class reporting, exact declaration matching, public GeoRobotix hard denial, mutation parameters, `Allow: PATCH` parsing, collection shape, condition messages, and group naming.
  - `testng.xml`: adds `part2update` with `core common systemfeatures` dependencies.
  - `VerifyTestNGSuiteDependency`: adds dependency, method-group, and co-location structural checks.
- Generator gate expectations:
  - Default GeoRobotix smoke must remain read-only with zero IUT-bound PATCH/POST/PUT/DELETE.
  - OPTIONS readiness cannot PASS lifecycle behavior.
  - Declared `/conf/update` plus successful OPTIONS omitting PATCH should fail readiness while lifecycle skips before PATCH.
  - Requirements 79-92 must be gated by their Clause 15 resource-class conditions before any PASS.
  - Positive PATCH checks require a dedicated mutable IUT, explicit mutation opt-in, and changed-field GET proof.
  - HTTP 400, HTTP 500, streaming-only, or no-candidate endpoints SKIP honestly rather than PASS.
- Formatter:
  - Command: Docker Maven `mvn -B spring-javaformat:apply`
  - Result: BUILD SUCCESS
- Maven:
  - Command: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `219 tests / 0 failures / 0 errors / 3 skipped`
  - Log: `ops/test-results/sprint-ets-27-maven-2026-05-22.log`
- Planning TeamEngine E2E smoke:
  - Command: `SMOKE_CONTAINER_NAME=ets-csapi-s27-plan-georobotix SMOKE_OUTPUT_DIR=/tmp/sprint-ets-27-plan-georobotix-results bash scripts/smoke-test.sh`
  - Result: `146 total / 27 passed / 5 failed / 114 skipped`
  - Report: `ops/test-results/sprint-ets-27-plan-georobotix-smoke-failed-2026-05-22.xml`
  - Container log: `ops/test-results/sprint-ets-27-plan-georobotix-smoke-container-failed-2026-05-22.log`
  - Failure cause: public GeoRobotix HTTP 500 responses on existing SystemFeatures/GeoJSON/SensorML/Datastream/Observation read paths; no Part 2 Update runtime tests exist yet.
  - Public-IUT safety check: no matched GeoRobotix PATCH/POST/PUT/DELETE request lines in the archived container log.
  - Disposition: advisory public interoperability evidence, not an accepted Sprint 27 E2E gate.
- Generator TeamEngine E2E smoke:
  - Command: `SMOKE_CONTAINER_NAME=ets-csapi-s27-generator-georobotix SMOKE_OUTPUT_DIR=/tmp/sprint-ets-27-generator-georobotix-results bash scripts/smoke-test.sh`
  - Result: `160 total / 27 passed / 5 failed / 128 skipped`
  - Report: `ops/test-results/sprint-ets-27-generator-georobotix-smoke-failed-2026-05-22.xml`
  - Container log: `ops/test-results/sprint-ets-27-generator-georobotix-smoke-container-failed-2026-05-22.log`
  - Failure cause: public GeoRobotix HTTP 500 responses on existing SystemFeatures/GeoJSON/SensorML/Datastream/Observation read paths.
  - Part 2 Update runtime outcome: all 14 runtime tests SKIP because `systemfeatures` did not finish successfully on the public IUT.
  - Public-IUT safety check: `scripts/no-mutation-oracle.py` recognized 61 GeoRobotix IUT request logs and found zero IUT-bound PATCH/POST/PUT/DELETE; explicit grep found no matched write-method lines.
  - Disposition: mandatory public smoke run, failed external/public-IUT evidence, not an accepted zero-failure E2E gate.
- Accepted local OSH TeamEngine E2E smoke:
  - Command: authenticated `scripts/smoke-test.sh` with `SMOKE_CONTAINER_NAME=ets-csapi-s27-generator-local-osh`, `SMOKE_DOCKER_NETWORK=field-hub_default`, `SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api`, `SMOKE_MUTATION_TESTS_ENABLED=true`, and `SMOKE_MUTATION_IUT_POLICY=dedicated-mutable-iut`.
  - Result: `160 total / 62 passed / 0 failed / 98 skipped`
  - Report: `ops/test-results/sprint-ets-27-generator-local-osh-smoke-2026-05-22.xml`
  - Container log: `ops/test-results/sprint-ets-27-generator-local-osh-smoke-container-2026-05-22.log`
  - Part 2 Update runtime outcome: all 14 runtime tests SKIP because local OSH does not declare Part 2 `/conf/update`.
  - PATCH safety check: no PATCH request lines in the local OSH smoke container log.
  - Disposition: accepted Sprint 27 full-stack E2E gate; existing Part 1 system CRD POST/PUT/DELETE requests occurred under explicit dedicated mutable-IUT opt-in.
- Raze local OSH E2E acceptance review:
  - `.harness/evaluations/sprint-ets-27-local-osh-e2e-acceptance-raze.yaml`: `APPROVE_WITH_CONCERNS`, confidence 0.94, no required fixes.
  - Verified the accepted XML totals, all 14 Part 2 Update runtime methods as SKIP, no local OSH PATCH request lines, and GeoRobotix kept as failed advisory public-IUT evidence.
- Commit/push:
  - Generator commit `6ae8f1c Implement Sprint 27 Part 2 Update with local OSH E2E gate` pushed over SSH on 2026-05-22 (`2be355a..6ae8f1c main -> main`).

## Previous Sprint Evidence

Sprint ets-26 Part 2 Create/Replace/Delete Generator:

- Status:
  - Story: `epics/stories/s-ets-26-01-part2-create-replace-delete-planning.md`
  - Contract: `.harness/contracts/sprint-ets-26.yaml`
  - Generator code is implemented and Maven verified.
  - Accepted Sprint 26 E2E gate is the seeded local OSH mutable IUT after fixture repair.
  - GeoRobotix public smoke currently fails on HTTP 500 responses outside the new Part 2 CRD tests and is recorded as advisory external interoperability evidence.
  - Local OSH E2E is recovered after seed fixture repair and now has a zero-failure full-stack TeamEngine run.
- Official OGC source verification:
  - Source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 14 "Requirements Class Create/Replace/Delete" and Annex A.7.
  - Requirements class identifier: `/req/create-replace-delete`.
  - Conformance class identifier: `/conf/create-replace-delete`.
  - Prerequisite: OGC API Features Part 4 Create/Replace/Delete.
  - Selected planning requirement set: Requirements 63-78, `/req/create-replace-delete/datastream`, `/req/create-replace-delete/datastream-update-schema`, `/req/create-replace-delete/datastream-delete-cascade`, `/req/create-replace-delete/observation`, `/req/create-replace-delete/observation-schema`, `/req/create-replace-delete/controlstream`, `/req/create-replace-delete/controlstream-update-schema`, `/req/create-replace-delete/controlstream-delete-cascade`, `/req/create-replace-delete/command`, `/req/create-replace-delete/command-schema`, `/req/create-replace-delete/command-status`, `/req/create-replace-delete/command-result`, `/req/create-replace-delete/feasibility`, `/req/create-replace-delete/feasibility-status`, `/req/create-replace-delete/feasibility-result`, and `/req/create-replace-delete/system-event`.
- GeoRobotix planning probes:
  - `/conformance`: declares `/conf/create-replace-delete` and OGC API Features Part 4 `/conf/create-replace-delete`.
  - `/conformance`: does not declare Part 2 `/conf/api-common`, `/conf/update`, or `/conf/advanced-filtering`.
  - OPTIONS probes for `/datastreams`, `/datastreams/{id}`, `/observations`, `/controlstreams`, `/commands`, `/controlstreams/{id}/commands`, `/systems/{id}/events`, `/systemEvents`, and `/feasibility`: HTTP 200 with broad `Allow` including POST, PUT, and DELETE.
  - Generator gapfix note: the broad global OPTIONS probes are planning diagnostics only. Runtime create-readiness probes now use scoped OGC 23-002 Clause 14 templates or SKIP when required parent IDs cannot be established.
  - `GET /commands?limit=1`: HTTP 400 `Invalid resource name`.
  - `GET /systemEvents?limit=1`: HTTP 400 `Invalid resource name`.
  - `GET /systems/{id}/events?limit=1`: HTTP 400 `Only streaming requests supported on this resource`.
  - `GET /feasibility?limit=1`: HTTP 400 `Invalid resource name`.
- Generator gate expectations:
  - Default GeoRobotix smoke must remain read-only with zero IUT-bound POST/PUT/DELETE/PATCH.
  - OPTIONS readiness cannot PASS lifecycle behavior.
  - Positive lifecycle checks require a dedicated mutable IUT and explicit mutation opt-in.
  - HTTP 400 or streaming-only endpoints SKIP honestly rather than PASS.
- Generator implementation:
  - `Part2CreateReplaceDeleteTests`: 9 safety-gated runtime checks for exact `/conf/create-replace-delete`, Features Part 4 prerequisite visibility, mutation safety, DataStream/Observation OPTIONS readiness, ControlStream/nested Command OPTIONS readiness, unavailable Command/Feasibility/SystemEvent endpoint honesty, and deferred lifecycle opt-in checks.
  - `VerifyPart2CreateReplaceDeleteTests`: 9 helper regressions for official identifiers, scoped readiness path selection, associated-System evidence, exact declaration matching, public GeoRobotix hard denial, explicit mutation parameters, case-insensitive `Allow` parsing, `items[]` collection shape, and stable group naming.
  - `testng.xml`: adds `part2createreplacedelete` with `core common systemfeatures` dependencies.
  - `VerifyTestNGSuiteDependency`: adds dependency, method-group, and co-location structural checks.
- Formatter:
  - Command: Docker Maven `mvn -B spring-javaformat:apply`
  - Result: BUILD SUCCESS
- Raze implementation review:
  - `.harness/evaluations/sprint-ets-26-adversarial-implementation.yaml`: `GAPS_FOUND`, confidence 0.88.
  - Required fix applied: DataStream, Observation, and ControlStream create-readiness now use scoped Clause 14 endpoint templates instead of global collection endpoints.
  - `.harness/evaluations/sprint-ets-26-adversarial-gapfix.yaml`: `APPROVE_WITH_CONCERNS`, confidence 0.94. `RAZE-ETS26-IMPL-GAP-001` is closed; the remaining E2E concern was resolved for Sprint 26 by accepting the seeded local OSH full-stack run as the gate.
- Maven verification:
  - Command: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `207 tests / 0 failures / 0 errors / 3 skipped`
  - Log: `ops/test-results/sprint-ets-26-maven-2026-05-22.log`
- Planning TeamEngine E2E smoke:
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s26-plan bash scripts/smoke-test.sh`
  - Result: `137 total / 72 passed / 0 failed / 65 skipped`
  - Report: `ops/test-results/sprint-ets-26-plan-smoke-2026-05-13.xml`
  - Container log: `ops/test-results/sprint-ets-26-plan-smoke-container-2026-05-13.log`
  - No-mutation oracle: `recognized_iut_request_logs=100`, zero IUT-bound POST/PUT/DELETE/PATCH.
- Generator GeoRobotix public advisory smoke:
  - Command: from `/tmp/sprint-ets-26-generator-smoke`, post-gapfix run with `SMOKE_OUTPUT_DIR=/tmp/sprint-ets-26-generator-gapfix-georobotix-results bash scripts/smoke-test.sh`.
  - Result: FAILED, `146 total / 27 passed / 5 failed / 114 skipped`.
  - Report: `ops/test-results/sprint-ets-26-gapfix-georobotix-smoke-failed-2026-05-22.xml`.
  - Container log: `ops/test-results/sprint-ets-26-gapfix-georobotix-smoke-container-failed-2026-05-22.log`.
  - Failures: existing SystemFeatures/Datastream/Observation reads expected HTTP 200 but got HTTP 500.
  - Direct probes: GeoRobotix returned HTTP 500 for `GET /systems/0mqcvdnfoca0`, `GET /datastreams?limit=1`, and `GET /observations?limit=2`.
  - New Part 2 CRD outcome: methods dependency-SKIP because `systemfeatures` did not finish successfully.
  - Public-IUT mutation evidence: no logged GeoRobotix POST/PUT/DELETE/PATCH requests in the archived container log.
- Generator local OSH TeamEngine E2E fallback:
  - Command: from `/tmp/sprint-ets-26-generator-smoke`, post-gapfix local OSH target on `field-hub_default` with Basic auth plus `SMOKE_MUTATION_TESTS_ENABLED=true` and `SMOKE_MUTATION_IUT_POLICY=dedicated-mutable-iut`.
  - Result: FAILED, `146 total / 61 passed / 4 failed / 81 skipped`.
  - Report: `ops/test-results/sprint-ets-26-gapfix-local-osh-smoke-failed-2026-05-22.xml`.
  - Container log: `ops/test-results/sprint-ets-26-gapfix-local-osh-smoke-container-failed-2026-05-22.log`.
  - Failures: four existing SensorML deployment/procedure alternate-resource checks expected HTTP 200 but got HTTP 500.
  - Direct probe: local OSH returned HTTP 500 for `GET /sensorhub/api/procedures/040g?f=sml3`.
  - New Part 2 CRD runtime outcome: 3 PASS and 6 SKIP. The lifecycle checks SKIP with precise deferred-fixture/no-mutation messages.
  - Mutation scope: existing Part 1 system CRD checks issued system POST/PUT/DELETE under the explicit opt-in; no Part 2 datastream, observation, controlstream, command, feasibility, or system-event lifecycle mutation was issued.
- Accepted local OSH seedfix TeamEngine E2E gate:
  - Seed repair: added `properties.featureType=http://www.w3.org/ns/sosa/Procedure` and `properties.featureType=http://www.w3.org/ns/sosa/Deployment` to the versioned local OSH fixture and updated the live `/procedures/040g` and `/deployments/040g` records in place.
  - Direct probes after seed repair: `GET /sensorhub/api/procedures/040g?f=sml3` and `GET /sensorhub/api/deployments/040g?f=sml3` both returned HTTP 200 with `Content-Type: application/sml+json`.
  - Command: from `/tmp/sprint-ets-26-generator-smoke`, local OSH target on `field-hub_default` with Basic auth plus `SMOKE_MUTATION_TESTS_ENABLED=true` and `SMOKE_MUTATION_IUT_POLICY=dedicated-mutable-iut`.
  - Result: `146 total / 62 passed / 0 failed / 84 skipped`.
  - Report: `ops/test-results/sprint-ets-26-seedfix-local-osh-smoke-2026-05-22.xml`.
  - Container log: `ops/test-results/sprint-ets-26-seedfix-local-osh-smoke-container-2026-05-22.log`.
  - SensorML outcome: `procedureSensorMlHasSchemaAndMapping` PASS; deployment SensorML mapping SKIP because OSH's generated SensorML Deployment has no non-empty `deployedSystems`; deployment/procedure relation-type checks SKIP because generated SensorML bodies have no JSON `links` member.
  - New Part 2 CRD runtime outcome: 3 PASS and 6 SKIP. The lifecycle checks SKIP with precise deferred-fixture/no-mutation messages.
  - Raze seedfix review: `.harness/evaluations/sprint-ets-26-local-osh-seedfix-raze.yaml` initially found stale verification metadata in the fixture, then returned `APPROVE_WITH_CONCERNS` confidence 0.94 after that metadata was split into historical and current Sprint 26 seedfix evidence. No required fixes remain.
- Raze local OSH E2E acceptance review:
  - `.harness/evaluations/sprint-ets-26-local-osh-e2e-acceptance-raze.yaml`: `APPROVE_WITH_CONCERNS`, confidence 0.93, no required fixes.
  - Confirmed no false claim that GeoRobotix passes, no stale mandatory GeoRobotix gate language, local OSH acceptance consistency with `AGENTS.md` and `ops/e2e-test-plan.md`, and matching archived totals for local OSH `146 / 62 / 0 / 84` and GeoRobotix advisory failure `146 / 27 / 5 / 114`.
- Raze planning review:
  - `.harness/evaluations/sprint-ets-26-plan-adversarial.yaml`: initial `GAPS_FOUND` confidence 0.94 for missing Sprint 26 changelog entry.
  - Required fix applied: `ops/changelog.md` now records the Sprint 26 planning instruction, story, `REQ-ETS-PART2-007`, Clause 14 identifiers, GeoRobotix probes, smoke totals, and zero-mutation evidence.
  - Focused recheck verdict: `APPROVE`, confidence 0.96, no remaining required fixes.
- Commit/push:
  - Planning commit `146c4c6 Plan Sprint 26 Part 2 CRD` pushed over SSH on 2026-05-13 (`7d57d9f..146c4c6 main -> main`).
  - Generator commit `c2d9d1e Implement Sprint 26 Part 2 CRD with local OSH E2E gate` pushed over SSH on 2026-05-22 (`d9caf33..c2d9d1e main -> main`).

Sprint ets-25 Part 2 Advanced Filtering Generator:

- Generator implementation:
  - `Part2AdvancedFilteringTests` adds 9 read-only runtime checks for exact `/conf/advanced-filtering`, prerequisite visibility, DataStream time filters, DataStream `observedProperty`, Observation time filters, ControlStream time filters, ControlStream `controlledProperty`, Command filters when `/commands` is available, and SystemEvent `eventType` when `/systemEvents` is available.
  - `VerifyPart2AdvancedFilteringTests` adds 8 helper regressions for official identifiers, canonical `systemEvents` casing, parsed time interval intersection, malformed substring rejection, strict Observation `phenomenonTime` evidence, property definition matching, command/event predicate extraction, and collection shape.
  - `VerifyTestNGSuiteDependency` adds structural coverage for `part2advancedfiltering` group dependency, method tagging, and suite co-location.
  - `Part2ApiCommonTests` no longer treats `systemhistory` as an OGC Part 2 collection token; `VerifyPart2ApiCommonTests` has a regression for GeoRobotix's vendor extension.
- Official OGC source verification:
  - Source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 13 "Requirements Class Advanced Filtering" and Annex A.6.
  - Requirements class identifier: `/req/advanced-filtering`.
  - Conformance class identifier: `/conf/advanced-filtering`.
  - Prerequisites: `/req/api-common` and Part 1 `/req/advanced-filtering`.
  - First implemented subset: `/req/advanced-filtering/datastream-by-phenomenontime`, `/datastream-by-resulttime`, `/datastream-by-obsprop`, `/obs-by-phenomenontime`, `/obs-by-resulttime`, `/controlstream-by-issuetime`, `/controlstream-by-exectime`, `/controlstream-by-controlprop`, `/cmd-by-issuetime`, `/cmd-by-exectime`, `/cmd-by-status`, `/cmd-by-sender`, and `/event-by-type`.
- Taxonomy correction:
  - OGC 23-002 Annex A does not define `/conf/system-history` or `/req/system-history`.
  - GeoRobotix advertises `/conf/system-history`; planning treats it as non-standard/vendor extension evidence only.
- GeoRobotix planning probes:
  - `/conformance`: does not declare `/conf/advanced-filtering`.
  - `GET /datastreams?phenomenonTime=2026-04-20T00:00:00Z&limit=2`: HTTP 200 JSON with `items`.
  - `GET /datastreams?resultTime=2026-04-20T00:00:00Z&limit=2`: HTTP 200 JSON with `items`.
  - `GET /datastreams?observedProperty=http%3A%2F%2Fsensorml.com%2Font%2Fisa%2Fproperty%2FLink_Loss&limit=2`: HTTP 200 JSON with `items`.
  - `GET /observations?phenomenonTime=2026-04-20T00:00:00Z&limit=2` and `GET /observations?resultTime=2026-04-20T00:00:00Z&limit=2`: HTTP 200 JSON with empty `items`.
  - `GET /controlstreams?issueTime=2026-04-20T00:00:00Z&limit=2` and `GET /controlstreams?executionTime=2026-04-20T00:00:00Z&limit=2`: HTTP 200 JSON with `items`.
  - `GET /commands?issueTime=...`, `GET /commands?statusCode=...`, and `GET /commands?sender=...`: HTTP 400 `Invalid resource name`.
  - `GET /systemEvents?eventType=...`: HTTP 400 `Invalid resource name`.
  - `GET /systems/0mqcvdnfoca0/events?eventType=...`: HTTP 400 `Only streaming requests supported on this resource`.
- Formatter:
  - Command: Docker Maven `mvn -B spring-javaformat:apply`
  - Result: BUILD SUCCESS
- Maven verification:
  - Command: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `195 tests / 0 failures / 0 errors / 3 skipped`
  - Log: `ops/test-results/sprint-ets-25-maven-2026-05-13.log`
- TeamEngine E2E smoke:
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s25-final bash scripts/smoke-test.sh`
  - Result: `137 total / 72 passed / 0 failed / 65 skipped`
  - Report: `ops/test-results/sprint-ets-25-smoke-2026-05-13.xml`
  - Container log: `ops/test-results/sprint-ets-25-smoke-container-2026-05-13.log`
  - No-mutation oracle: `recognized_iut_request_logs=100`, zero IUT-bound POST/PUT/DELETE/PATCH.
- Part 2 Advanced Filtering runtime outcome on GeoRobotix:
  - PASS: none, because `/conf/advanced-filtering` is not declared.
  - SKIP: all 9 Part 2 Advanced Filtering runtime tests.
  - The SKIPs are expected for current GeoRobotix state and confirm no PASS is inferred from undeclared HTTP 200 filter behavior, endpoint availability alone, empty collections, sibling declarations, or `/conf/system-history`.
- Raze planning review:
  - `.harness/evaluations/sprint-ets-25-plan-adversarial.yaml`: initial `GAPS_FOUND` for one stale `REQ-ETS-PART2-014` epic acceptance reference.
  - Required fix applied: epic ETS-03 now references corrected `REQ-ETS-PART2-013`.
  - Recheck verdict: `APPROVE`, confidence 0.96.
- Raze implementation review:
  - `.harness/evaluations/sprint-ets-25-adversarial-implementation.yaml`: `GAPS_FOUND`, confidence 0.91.
  - Required fix verified: Observation `phenomenonTime` filters no longer seed or validate via `resultTime` fallback.
  - Related concern fixed: malformed time strings that merely contain a requested instant are rejected because `timeIntersects` now parses both values.
  - `.harness/evaluations/sprint-ets-25-adversarial-gapfix.yaml`: `APPROVE`, confidence 0.96, no required fixes.
- Commit/push:
  - Planning commit `2f4a6de Plan Sprint 25 Advanced Filtering` pushed over SSH on 2026-05-09 (`5dccb36..2f4a6de main -> main`).
  - Generator commit `d9df3ad Implement Sprint 25 Advanced Filtering` pushed over SSH on 2026-05-13 (`f251241..d9df3ad main -> main`).

Sprint ets-24 Part 2 System Events Generator:

- Generator implementation:
  - `Part2SystemEventTests` adds 6 declaration-gated checks for `/conf/system-event`, prerequisite visibility, `/systemEvents`, normative `/systems/{sysId}/events`, optional `/systemEvents/{id}` canonical resource reads, and optional `itemType=SystemEvent` collections.
  - `VerifyPart2SystemEventTests` adds 5 helper regressions for official identifiers, normative path selection, resource-specific SystemEvent evidence, exact collection `itemType`, and collection shape.
  - `VerifyTestNGSuiteDependency` adds structural coverage for `part2systemevent` group dependency and Core/Common/SystemFeatures co-location.
- Formatter:
  - Command: Docker Maven `mvn -B spring-javaformat:apply`
  - Result: BUILD SUCCESS
- Maven verification:
  - Command: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `183 tests / 0 failures / 0 errors / 3 skipped`
  - Log: `ops/test-results/sprint-ets-24-maven-2026-05-09.log`
- TeamEngine E2E smoke:
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s24 bash scripts/smoke-test.sh`
  - Result: `128 total / 72 passed / 0 failed / 56 skipped`
  - Report: `ops/test-results/sprint-ets-24-smoke-2026-05-09.xml`
  - Container log: `ops/test-results/sprint-ets-24-smoke-container-2026-05-09.log`
  - No-mutation oracle: `recognized_iut_request_logs=99`, zero IUT-bound POST/PUT/DELETE/PATCH.
- System Events runtime outcome on GeoRobotix:
  - PASS: `systemEventConformanceDeclared`.
  - SKIP: `systemEventPrerequisitesVisibleForFullClosure`, `systemEventsCanonicalEndpointReadableWhenAvailable`, `systemScopedEventsEndpointUsesNormativePath`, `systemEventCanonicalResourceReadableWhenAvailable`, and `systemEventCollectionsCheckedWhenAdvertised`.
  - The SKIPs are expected for current GeoRobotix state: missing `/conf/api-common`, HTTP 400 `/systemEvents`, HTTP 400 streaming-only `/systems/{id}/events`, no JSON SystemEvent resource evidence, and no advertised `itemType=SystemEvent` collection.
- Raze implementation review `.harness/evaluations/sprint-ets-24-adversarial-implementation.yaml`: `APPROVE` confidence 0.94 with no required fixes.

Sprint ets-24 Part 2 System Events planning:

- Planning only:
  - Story: `epics/stories/s-ets-24-01-part2-system-event-planning.md`
  - Contract: `.harness/contracts/sprint-ets-24.yaml`
  - No Java code changed yet; Maven and TeamEngine smoke are Generator gates.
- Official OGC source verification:
  - Source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 12 "Requirements Class System Events" and Annex A.5.
  - Requirements class identifier: `/req/system-event`.
  - Conformance class identifier: `/conf/system-event`.
  - Prerequisites: `/req/api-common` and Part 1 `/req/system`.
  - Selected Sprint 24 requirements: `/req/system-event/canonical-url`, `/req/system-event/resources-endpoint`, `/req/system-event/canonical-endpoint`, `/req/system-event/ref-from-system`, and `/req/system-event/collections`.
- GeoRobotix planning probes:
  - `/conformance`: declares `/conf/system-event`, but not `/conf/api-common`.
  - `GET /systems?limit=1`: returned System id `0mqcvdnfoca0`.
  - `GET /systemEvents?limit=2`: HTTP 400, `Invalid resource name: 'systemEvents'`.
  - `GET /systems/0mqcvdnfoca0/events?limit=2`: HTTP 400 JSON, `Only streaming requests supported on this resource`.
  - `GET /systems/0mqcvdnfoca0/systemEvents?limit=2`: HTTP 400, `Invalid resource name: 'systemEvents'`.
  - `GET /collections?limit=100`: no collection with `itemType` equal to `SystemEvent` was observed.
- Generator gate expectations:
  - Default GeoRobotix smoke must not PASS System Events from declaration alone.
  - Requirement 42 uses `/systemEvents`; Requirement 43 uses `/systems/{sysId}/events`.
  - Annex A.43's `/systems/{sysId}/systemEvents` string is diagnostic-only unless a standards-backed correction is documented.
  - Streaming/SSE event consumption is outside this first read-only Generator increment.
- Raze planning review:
  - `.harness/evaluations/sprint-ets-24-plan-adversarial.yaml`: `APPROVE` confidence 0.93 with no required fixes.

Sprint ets-23 Part 2 Command Feasibility Generator:

- Generator implementation:
  - `Part2FeasibilityTests` adds 7 declaration-gated, read-only/default-safe checks for `/conf/feasibility`, `/req/controlstream` prerequisite visibility, normative singular `/controlstream/{csId}/feasibility`, optional `/feasibility` resource/canonical evidence, optional `/feasibility/{id}/status`, optional `/feasibility/{id}/result`, and optional Feasibility collections.
  - `VerifyPart2FeasibilityTests` adds 5 helper regressions for official identifiers, singular endpoint path, Feasibility resource evidence, `itemType=Feasibility`, and collection shape.
  - `VerifyTestNGSuiteDependency` adds structural coverage for `part2feasibility` group dependency and Core/Common co-location.
  - No Feasibility POST/PUT/DELETE/PATCH path is implemented.
- Formatter:
  - Command: Docker Maven `mvn -B spring-javaformat:apply`
  - Result: BUILD SUCCESS
- Maven verification:
  - Command: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `175 tests / 0 failures / 0 errors / 3 skipped`
  - Log: `ops/test-results/sprint-ets-23-maven-2026-05-08.log`
- TeamEngine E2E smoke:
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s23-gapfix bash scripts/smoke-test.sh`
  - Result: `122 total / 71 passed / 0 failed / 51 skipped`
  - Report: `ops/test-results/sprint-ets-23-smoke-2026-05-08.xml`
  - Container log: `ops/test-results/sprint-ets-23-smoke-container-2026-05-08.log`
  - No-mutation oracle: `recognized_iut_request_logs=93`, zero IUT-bound POST/PUT/DELETE/PATCH.
- Feasibility runtime outcome on GeoRobotix:
  - SKIP: all 7 Feasibility tests because GeoRobotix does not declare `/conf/feasibility`.
  - PASS: no Feasibility tests; the ETS does not infer Feasibility PASS from `/conf/controlstream` or endpoint probes.
- Raze implementation review `.harness/evaluations/sprint-ets-23-adversarial-implementation.yaml`: `GAPS_FOUND` confidence 0.90 for a collection-shape-only canonical false-PASS risk; fixed by requiring actual Feasibility-shaped resource evidence for canonical/status/result checks.
- Raze gap-fix review `.harness/evaluations/sprint-ets-23-adversarial-gapfix.yaml`: `APPROVE` confidence 0.97 with no required fixes.

Sprint ets-23 Part 2 Command Feasibility planning:

- Planning only:
  - Story: `epics/stories/s-ets-23-01-part2-feasibility-planning.md`
  - Contract: `.harness/contracts/sprint-ets-23.yaml`
  - No Java code changed yet; Maven and TeamEngine smoke are Generator gates.
- Official OGC source verification:
  - Source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 11 "Requirements Class Command Feasibility".
  - Requirements class identifier: `/req/feasibility`.
  - Conformance class identifier: `/conf/feasibility`.
  - Prerequisite: `/req/controlstream`.
  - Selected Sprint 23 requirements: `/req/feasibility/canonical-url`, `/req/feasibility/ref-from-controlstream`, `/req/feasibility/status-endpoint`, `/req/feasibility/result-endpoint`, and `/req/feasibility/collections`.
- GeoRobotix planning probes:
  - `/conformance`: declares `/conf/controlstream` but not `/conf/feasibility` or `/conf/api-common`.
  - `GET /controlstreams?limit=1`: returned ControlStream id `0m4qpft9sdag` with `system@id` `0m5ojudgr570`.
  - `GET /controlstreams/0m4qpft9sdag`: HTTP 200 JSON with ControlStream-specific fields.
  - `GET /feasibility?limit=2`: HTTP 400 JSON, `Invalid resource name: 'feasibility'`.
  - `GET /controlstreams/0m4qpft9sdag/feasibility?limit=2`: HTTP 400 JSON, `Invalid resource name: 'feasibility'`.
  - `GET /controlstream/0m4qpft9sdag/feasibility?limit=2`: HTTP 400 JSON, `Invalid resource name: 'controlstream'`.
  - `GET /collections?limit=100`: no collection with `itemType` equal to `Feasibility` was observed.
  - Endpoint alias guard: the plural `/controlstreams/{id}/feasibility` probe is diagnostic only; `/req/feasibility/ref-from-controlstream` PASS requires the normative singular `{api_root}/controlstream/{csId}/feasibility` endpoint or a documented standards-backed rationale.
- Generator gate expectations:
  - Default GeoRobotix smoke must SKIP Feasibility because `/conf/feasibility` is absent.
  - Default GeoRobotix smoke must report zero IUT-bound POST/PUT/DELETE/PATCH.
  - Any positive feasibility creation path must be guarded behind explicit safe/mutable-IUT opt-in.
- Raze planning review:
  - `.harness/evaluations/sprint-ets-23-plan-adversarial.yaml`: `GAPS_FOUND` confidence 0.89 for endpoint-alias false-PASS risk and stale status metadata.
  - `.harness/evaluations/sprint-ets-23-plan-gapfix.yaml`: `APPROVE` confidence 0.96 with no required fixes.

Sprint ets-22 Part 2 Control Streams & Commands Generator:

- Generator implementation:
  - `Part2ControlStreamTests` adds declaration-gated read-only checks for `/conf/controlstream`, `/controlstreams`, `/controlstreams/{id}`, `/controlstreams/{id}/schema`, `/controlstreams/{id}/commands`, `/commands` when available, `/controls/{id}` when available, populated nested Command reference evidence, and bounded `/systems/{systemId}/controlstreams`.
  - `VerifyPart2ControlStreamTests` adds 4 helper regressions for ControlStream shape, Command reference evidence, empty collection shape, and official identifier drift.
  - `VerifyTestNGSuiteDependency` adds structural coverage for `part2controlstream` group dependency and Core/Common co-location.
- Official OGC source verification:
  - Source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 10 "Requirements Class Control Streams & Commands".
  - Requirements class identifier: `/req/controlstream`.
  - Conformance class identifier: `/conf/controlstream`.
  - Prerequisite: `/req/api-common`.
  - Selected Sprint 22 requirements: `/req/controlstream/resources-endpoint`, `/req/controlstream/canonical-endpoint`, `/req/controlstream/ref-from-system`, `/req/controlstream/schema-op`, `/req/controlstream/cmd-resources-endpoint`, `/req/controlstream/cmd-ref-from-controlstream`, with explicit guardrails around `/req/controlstream/canonical-url` and `/req/controlstream/cmd-canonical-endpoint`.
- GeoRobotix planning probes:
  - `/conformance`: declares `/conf/controlstream` but not `/conf/api-common`.
  - `GET /controlstreams?limit=2`: HTTP 200 JSON with `items` and `links`.
  - `GET /controlstreams/0m4qpft9sdag`: HTTP 200 JSON with ControlStream-specific fields.
  - `GET /controlstreams/0m4qpft9sdag/schema`: HTTP 200 JSON with `commandFormat` and `parametersSchema`.
  - `GET /controlstreams/0m4qpft9sdag/commands?limit=2`: HTTP 200 JSON with empty `items`; endpoint availability evidence only, not `/req/controlstream/cmd-ref-from-controlstream` PASS evidence.
  - `GET /systems/0m5ojudgr570/controlstreams?limit=2`: HTTP 200 JSON with ControlStream items.
  - `GET /commands?limit=2`: HTTP 400 HTML; no global Command endpoint PASS evidence.
  - `GET /controls/0m4qpft9sdag`: HTTP 400 HTML; no `/req/controlstream/canonical-url` PASS from `/controlstreams/{id}` alias evidence.
- Formatter:
  - Command: Docker Maven `mvn -B spring-javaformat:apply`
  - Result: BUILD SUCCESS
- Maven verification:
  - Command: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `167 tests / 0 failures / 0 errors / 3 skipped`
  - Log: `ops/test-results/sprint-ets-22-maven-2026-05-08.log`
- TeamEngine E2E smoke:
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s22 bash scripts/smoke-test.sh`
  - Result: `115 total / 71 passed / 0 failed / 44 skipped`
  - Report: `ops/test-results/sprint-ets-22-smoke-2026-05-08.xml`
  - Container log: `ops/test-results/sprint-ets-22-smoke-container-2026-05-08.log`
  - No-mutation oracle: `recognized_iut_request_logs=91`, zero IUT-bound POST/PUT/DELETE/PATCH.
- ControlStream runtime outcome on GeoRobotix:
  - PASS: declaration, collection, collection item shape, observed `/controlstreams/{id}` read, schema, scoped commands endpoint, and system-scoped ControlStreams endpoint.
  - SKIP: full `/conf/controlstream` closure due missing `/conf/api-common`; `/req/controlstream/canonical-url` because `/controls/{id}` returned HTTP 400; `/req/controlstream/cmd-canonical-endpoint` because `/commands` returned HTTP 400; `/req/controlstream/cmd-ref-from-controlstream` because nested Commands were empty.
- Full `/conf/controlstream` closure remains blocked when `/req/api-common` is absent.
- Raze planning review:
  - `.harness/evaluations/sprint-ets-22-plan-adversarial.yaml`: `APPROVE` confidence 0.93.
- Raze implementation reviews:
  - `.harness/evaluations/sprint-ets-22-adversarial-implementation.yaml`: `GAPS_FOUND` confidence 0.91 for reconciliation/evidence gaps only.
  - `.harness/evaluations/sprint-ets-22-adversarial-gapfix.yaml`: `APPROVE` confidence 0.95 with no required fixes.

Sprint ets-21 Part 2 Datastreams & Observations Generator:

- Generator implementation:
  - `Part2DatastreamTests` adds declaration-gated read-only checks for `/conf/datastream`, `/datastreams`, `/datastreams/{id}`, `/datastreams/{id}/schema`, `/observations`, `/observations/{id}`, `/datastreams/{id}/observations`, and bounded `/systems/{systemId}/datastreams`.
  - `VerifyPart2DatastreamTests` adds 5 helper regressions for Datastream shape, Observation shape, reference evidence, empty collection shape, and official identifier drift.
  - `VerifyTestNGSuiteDependency` adds structural coverage for `part2datastream` group dependency and Core/Common co-location.
- Official OGC source verification:
  - Source: `https://docs.ogc.org/is/23-002/23-002.html`, Clause 9 "Requirements Class Datastreams & Observations".
  - Requirements class identifier: `/req/datastream`.
  - Conformance class identifier: `/conf/datastream`.
  - Prerequisite: `/req/api-common`.
  - Selected Sprint 21 requirements: `/req/datastream/canonical-url`, `/req/datastream/resources-endpoint`, `/req/datastream/canonical-endpoint`, `/req/datastream/ref-from-system`, `/req/datastream/schema-op`, `/req/datastream/obs-canonical-url`, `/req/datastream/obs-resources-endpoint`, `/req/datastream/obs-canonical-endpoint`, and `/req/datastream/obs-ref-from-datastream`.
- GeoRobotix planning probes:
  - `/conformance`: declares `/conf/datastream` but not `/conf/api-common`.
  - `GET /datastreams?limit=2`: HTTP 200 JSON with `items` and `links`.
  - `GET /observations?limit=2`: HTTP 200 JSON with `items` and `links`.
  - `GET /datastreams/0mirhn7lo1kg`: HTTP 200 JSON with Datastream-specific fields and an `observations` link.
  - `GET /datastreams/0mirhn7lo1kg/schema`: HTTP 200 JSON with `obsFormat` and `resultSchema`.
  - `GET /datastreams/0mirhn7lo1kg/observations?limit=2`: HTTP 200 JSON with empty `items`; no top-level `links`; endpoint availability evidence only, not `/req/datastream/obs-ref-from-datastream` PASS evidence.
  - `GET /systems/0nar3cl0tk3g/datastreams?limit=1`: HTTP 200 JSON with a Datastream item.
- Formatter:
  - Command: Docker Maven `mvn -B spring-javaformat:apply`
  - Result: BUILD SUCCESS
- Maven verification:
  - Command: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `160 tests / 0 failures / 0 errors / 3 skipped`
  - Log: `ops/test-results/sprint-ets-21-maven-2026-05-07.log`
- TeamEngine E2E smoke:
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s21-generator bash scripts/smoke-test.sh`
  - Result: `104 total / 64 passed / 0 failed / 40 skipped`
  - Report: `ops/test-results/sprint-ets-21-teamengine-smoke-2026-05-07.xml`
  - Log: `ops/test-results/sprint-ets-21-teamengine-container-2026-05-07.log`
  - No-mutation oracle: recognized 82 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH entries for `https://api.georobotix.io/ogc/t18/api`.
- Runtime outcome: GeoRobotix scoped Datastream endpoint checks run under `/conf/datastream`; full `/conf/datastream` closure remains prerequisite-incomplete because `/conf/api-common` is absent. Empty nested observations SKIP `/req/datastream/obs-ref-from-datastream`.
- Raze planning review:
  - `.harness/evaluations/sprint-ets-21-plan-adversarial.yaml`: `GAPS_FOUND` confidence 0.88.
  - Required fixes: split endpoint availability from `/req/datastream/obs-ref-from-datastream`; explicitly block full `/conf/datastream` closure while `/req/api-common` is absent.
  - `.harness/evaluations/sprint-ets-21-plan-gapfix.yaml`: `APPROVE` confidence 0.95 after fixes.
- Raze implementation review:
  - `.harness/evaluations/sprint-ets-21-adversarial-implementation.yaml`: `GAPS_FOUND` confidence 0.90.
  - Required fixes were reconciliation/evidence only: remove planning-only/next-Generator language and archive Maven evidence durably.
  - Fix status: addressed by updating status/test-results/changelog/handoff references and archiving `ops/test-results/sprint-ets-21-maven-2026-05-07.log`.
  - `.harness/evaluations/sprint-ets-21-adversarial-gapfix.yaml`: `APPROVE` confidence 0.96 with no required fixes.

Sprint ets-20 Part 2 API Common Generator:

- Generator implementation:
  - `Part2ApiCommonTests` adds exact `/conf/api-common` declaration gating, advertised-link-only Part 2 collection discovery, read-only collection shape checks, and dependency runtime tracing.
  - `VerifyPart2ApiCommonTests` adds 5 helper regressions for exact conformance URI matching, missing declaration handling, no synthesized `/commands`, `items`/`links` shape, and stale `dynamic-*` identifier rejection.
  - `VerifyTestNGSuiteDependency` adds 3 structural checks for the `part2apicommon` group and Core/Common co-location.
- Formatter:
  - Command: Docker Maven `mvn -B spring-javaformat:apply`
  - Result: BUILD SUCCESS
- Maven verification:
  - Command: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `152 tests / 0 failures / 0 errors / 3 skipped`
  - Log: `ops/test-results/sprint-ets-20-maven-2026-05-07-post-raze.log`
- TeamEngine E2E smoke:
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s20-generator-rerun bash scripts/smoke-test.sh`
  - Result: `93 total / 55 passed / 0 failed / 38 skipped`
  - Report: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s20-generator-rerun/s-ets-01-03-teamengine-smoke-2026-05-07.xml`
  - Log: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s20-generator-rerun/s-ets-01-03-teamengine-container-2026-05-07.log`
  - No-mutation oracle: recognized 71 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH entries for `https://api.georobotix.io/ogc/t18/api`.
- Runtime outcome: GeoRobotix does not currently declare `/conf/api-common`, so the Part 2 API Common runtime tests SKIP with declaration-honest reasons instead of producing PASS evidence from sibling Part 2 declarations.
- Raze implementation review:
  - `.harness/evaluations/sprint-ets-20-adversarial-implementation.yaml`: `APPROVE_WITH_CONCERNS` confidence 0.94.
  - Required fixes: none.
  - Follow-up: dependency lint now rejects comma syntax and tokenizes `depends-on` on whitespace; Raze said no additional full smoke was needed after this structural JUnit-only hardening.

Sprint ets-19 encoding mediatype-write safety-gated Generator:

- Generator implementation:
  - `EncodingMediatypeWrite` centralizes mutation opt-in, public GeoRobotix hard denial, exact `application/geo+json` and `application/sml+json` constants, created-resource URI resolution, and dereference/identity parse evidence.
  - `GeoJsonTests` adds `geoJsonMediaTypeWriteParsesSystemBodyWhenMutationEnabled`.
  - `SensorMlTests` adds `sensorMlMediaTypeWriteParsesSystemBodyWhenMutationEnabled`.
  - `VerifyEncodingMediatypeWrite` adds 8 focused helper regressions.
- Formatter:
  - Command: Docker Maven `mvn -B spring-javaformat:apply`
  - Result: BUILD SUCCESS
- Maven verification:
  - Command: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `144 tests / 0 failures / 0 errors / 3 skipped`
  - Log: `ops/test-results/sprint-ets-19-maven-r3-2026-05-07.log`
- TeamEngine E2E smoke:
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s19-generator-r3 bash scripts/smoke-test.sh`
  - Result: `89 total / 55 passed / 0 failed / 34 skipped`
  - Report: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s19-generator-r3/s-ets-01-03-teamengine-smoke-2026-05-07.xml`
  - Log: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s19-generator-r3/s-ets-01-03-teamengine-container-2026-05-07.log`
  - No-mutation oracle: recognized 69 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH entries for `https://api.georobotix.io/ogc/t18/api`.
- Local OSH mutable-IUT evidence:
  - Command: `SMOKE_CONTAINER_NAME=ets-csapi-osh-s19-mediatype-auth-r3 SMOKE_DOCKER_NETWORK=field-hub_default SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api SMOKE_AUTH_CREDENTIAL=... SMOKE_MUTATION_TESTS_ENABLED=true SMOKE_MUTATION_IUT_POLICY=dedicated-mutable-iut SMOKE_OUTPUT_DIR=/tmp/ets-csapi-osh-s19-mediatype-auth-r3 bash scripts/smoke-test.sh`
  - Result: `89 total / 52 passed / 4 failed / 33 skipped`
  - Report: `/tmp/ets-csapi-osh-s19-mediatype-auth-r3/s-ets-01-03-teamengine-smoke-2026-05-07.xml`
  - Log: `/tmp/ets-csapi-osh-s19-mediatype-auth-r3/s-ets-01-03-teamengine-container-2026-05-07.log`
  - Positive Sprint 19 evidence: `geoJsonMediaTypeWriteParsesSystemBodyWhenMutationEnabled` PASS and `sensorMlMediaTypeWriteParsesSystemBodyWhenMutationEnabled` PASS with exact `Content-Type=application/geo+json` and `Content-Type=application/sml+json`, follow-up GET, and cleanup DELETE request-log evidence.
  - The local OSH failures were outside Sprint 19 mediatype-write: deployment/procedure SensorML schema/mapping and relation-types checks received HTTP 500 from local OSH SensorML non-system resources. These seeded-resource HTTP 500 failures were later fixed during the Sprint 26 seed repair.
- Runtime outcomes:
  - `geoJsonMediaTypeWriteParsesSystemBodyWhenMutationEnabled`: SKIP before mutation because mutation tests are disabled by default.
  - `sensorMlMediaTypeWriteParsesSystemBodyWhenMutationEnabled`: SKIP before mutation because mutation tests are disabled by default.
  - Both SKIP messages require `mutation-tests-enabled=true` and `mutation-iut-policy=dedicated-mutable-iut`; status-only writes are not PASS evidence.
  - Local OSH mutable run proved the positive system-resource paths after disabling RestAssured default charset appending and using OSH-compatible ordered JSON bodies.
- Raze implementation review:
  - `.harness/evaluations/sprint-ets-19-adversarial-implementation.yaml`: `APPROVE_WITH_CONCERNS` confidence 0.90.
  - Required fixes: none.
  - Initial follow-up concerns: dedicated mutable-IUT mediatype-write evidence and cleanup DELETE diagnostics.
  - Follow-up local OSH evidence addressed the positive mutable-IUT concern for system-resource GeoJSON and SensorML mediatype-write checks; cleanup remains best-effort request-log evidence.
  - `.harness/evaluations/sprint-ets-19-adversarial-followup-gapfix.yaml`: `APPROVE` confidence 0.94 after reconciliation updates.

Sprint ets-19 encoding mediatype-write safety-gated planning:

- OGC source verification:
  - GeoJSON requirement class source includes `/req/geojson/mediatype-write`.
  - SensorML requirement class source includes `/req/sensorml/mediatype-write`.
  - GeoJSON clause requires `Content-Type: application/geo+json` parsing according to resource type when Create/Replace/Delete is implemented.
  - SensorML clause requires `Content-Type: application/sml+json` parsing according to resource type when Create/Replace/Delete is implemented.
- GeoRobotix planning probes:
  - `/conformance`: declares `/conf/create-replace-delete`, `/conf/geojson`, and `/conf/sensorml`.
  - `OPTIONS /systems`: `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS`.
  - `OPTIONS /systems/0mqcvdnfoca0`: `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS`.
- Interpretation: Sprint 19 must reuse mutation opt-in and public-IUT hard-denial gates. OPTIONS readiness is not mediatype-write conformance. A PASS requires exact `Content-Type` plus follow-up dereference evidence against a dedicated mutable IUT.
- Raze planning review:
  - `.harness/evaluations/sprint-ets-19-plan-adversarial.yaml`: `GAPS_FOUND` confidence 0.88.
  - Required fix: add missing OpenSpec body for `SCENARIO-ETS-PART1-013-SENSORML-MEDIATYPE-WRITE-SAFETY-GATED-001`.
  - `.harness/evaluations/sprint-ets-19-plan-gapfix.yaml`: `APPROVE` confidence 0.95, no remaining required fixes.

Sprint ets-18 encoding relation-types breadth Generator:

- Generator implementation:
  - `GeoJsonTests` adds independent Deployment, Procedure, and Sampling Feature relation-types checks while retaining the System check.
  - `SensorMlTests` adds independent Deployment and Procedure relation-types checks while retaining the System check.
  - `VerifyEncodingRelationTypes` adds breadth regressions for aggregate false-PASS prevention, property-level `@link` exclusion, and positive SensorML Deployment relation evidence.
- Maven verification:
  - Command: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `136 tests / 0 failures / 0 errors / 3 skipped`
  - Log: `ops/test-results/sprint-ets-18-maven-2026-05-07.log`
- TeamEngine E2E smoke:
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s18-generator bash scripts/smoke-test.sh`
  - Result: `87 total / 55 passed / 0 failed / 32 skipped`
  - Report: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s18-generator/s-ets-01-03-teamengine-smoke-2026-05-07.xml`
  - Log: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s18-generator/s-ets-01-03-teamengine-container-2026-05-07.log`
  - No-mutation oracle: recognized 69 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH entries for `https://api.georobotix.io/ogc/t18/api`.
- Runtime outcomes:
  - `geoJsonLinksMemberAssociationRelsUseResourceSpecificNames`: PASS on selected System links.
  - `geoJsonDeploymentLinksMemberAssociationRelsUseResourceSpecificNames`: SKIP on generic-only deployment links.
  - `geoJsonProcedureLinksMemberAssociationRelsUseResourceSpecificNames`: SKIP on generic-only procedure links.
  - `geoJsonSamplingFeatureLinksMemberAssociationRelsUseResourceSpecificNames`: SKIP on absent top-level `links`.
  - `sensorMlLinksMemberAssociationRelsUseResourceSpecificNames`: SKIP on absent top-level `links`.
  - `sensorMlDeploymentLinksMemberAssociationRelsUseResourceSpecificNames`: SKIP on absent top-level `links`.
  - `sensorMlProcedureLinksMemberAssociationRelsUseResourceSpecificNames`: SKIP on absent top-level `links`.
- Raze implementation review:
  - `.harness/evaluations/sprint-ets-18-adversarial-implementation.yaml`: `APPROVE` confidence 0.92.
  - Required fixes: none.

Sprint ets-18 encoding relation-types breadth read-only planning:

- OGC source verification:
  - GeoJSON clause: `api/part1/standard/sections/clause_20_requirements_class_geojson_encoding.adoc`, HTTP 200 on 2026-05-07.
  - SensorML clause: `api/part1/standard/sections/clause_21_requirements_class_sensorml_encoding.adoc`, HTTP 200 on 2026-05-07.
  - Both clauses define `/req/*/relation-types` as requiring association links in JSON `links` members to use the association name as `rel`.
- GeoRobotix planning probes:
  - `/systems/0mqcvdnfoca0`: generic `canonical`/`alternate` links plus association links `samplingFeatures` and `datastreams`; association rels match association names.
  - `/deployments/16sp744ch58g`: `links` contains only generic `canonical`/`alternate`; `deployedSystems@link` is under `properties`.
  - `/procedures/164p7ed8l47g`: `links` contains only generic `canonical`/`alternate`.
  - `/samplingFeatures/0mtff3l0oofg`: no `links` member; `hostedProcedure@link` is under `properties`.
  - SensorML system/deployment/procedure bodies observed during planning: no top-level `links` member.
- Interpretation: Sprint 18 must add independent resource-specific relation-types assertions. The existing GeoJSON System PASS cannot create an aggregate PASS for Deployment, Procedure, Sampling Feature, or SensorML resources. Generic-only or absent links SKIP per resource.
- Raze planning review:
  - `.harness/evaluations/sprint-ets-18-plan-adversarial.yaml`: `APPROVE` confidence 0.92.
  - Required fixes: none.

Sprint ets-17 encoding relation-types read-only Generator:

- OGC source verification:
  - GeoJSON clause: `api/part1/standard/sections/clause_20_requirements_class_geojson_encoding.adoc`, HTTP 200 on 2026-05-06.
  - SensorML clause: `api/part1/standard/sections/clause_21_requirements_class_sensorml_encoding.adoc`, HTTP 200 on 2026-05-06.
  - Both clauses define `/req/*/relation-types` as requiring association links in JSON `links` members to use the association name as `rel`.
- GeoRobotix planning probes:
  - `/systems/0mqcvdnfoca0`: generic `canonical`/`alternate` links plus association links `samplingFeatures` and `datastreams`; association rels match association names.
  - `/deployments/16sp744ch58g`: `links` contains only generic `canonical`/`alternate`; `deployedSystems@link` is under `properties`.
  - `/procedures/164p7ed8l47g`: `links` contains only generic `canonical`/`alternate`.
  - `/samplingFeatures/0mtff3l0oofg`: no `links` member; `hostedProcedure@link` is under `properties`.
  - SensorML system/deployment/procedure bodies observed during planning: no links-member association links.
- Interpretation: Sprint 17 must not count canonical/alternate/pagination links or property-level `@link` objects as relation-types PASS evidence. It should PASS only from links-member association rels valid for the selected encoding and resource type, SKIP when no links-member association exists, and FAIL malformed or wrong-resource association-link rels.
- Raze planning review:
  - `.harness/evaluations/sprint-ets-17-plan-adversarial.yaml`: `GAPS_FOUND` confidence 0.88.
  - Required fix: close the global association-name allowlist false PASS risk by requiring resource-specific GeoJSON and SensorML links-member association allowlists.
  - `.harness/evaluations/sprint-ets-17-plan-gapfix.yaml`: `APPROVE` confidence 0.94, no remaining required fixes.
- Generator implementation:
  - `EncodingRelationTypes` centralizes generic-link filtering and resource-specific links-member association allowlists for GeoJSON and SensorML.
  - `GeoJsonTests` adds `geoJsonLinksMemberAssociationRelsUseResourceSpecificNames`.
  - `SensorMlTests` adds `sensorMlLinksMemberAssociationRelsUseResourceSpecificNames`.
  - `VerifyEncodingRelationTypes` adds 5 focused helper regressions.
- Maven verification:
  - Command: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `133 tests / 0 failures / 0 errors / 3 skipped`
  - Log: `ops/test-results/sprint-ets-17-maven-2026-05-06.log`
- TeamEngine E2E smoke:
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s17-generator bash scripts/smoke-test.sh`
  - Result: `82 total / 55 passed / 0 failed / 27 skipped`
  - Report: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s17-generator/s-ets-01-03-teamengine-smoke-2026-05-06.xml`
  - Log: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s17-generator/s-ets-01-03-teamengine-container-2026-05-06.log`
  - No-mutation oracle: recognized 55 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH entries for `https://api.georobotix.io/ogc/t18/api`.
- Runtime outcome:
  - `geoJsonLinksMemberAssociationRelsUseResourceSpecificNames`: PASS on GeoRobotix selected System links.
  - `sensorMlLinksMemberAssociationRelsUseResourceSpecificNames`: SKIP because the selected SensorML system representation has no top-level links-member association links.
- Raze implementation review:
  - Artifact: `.harness/evaluations/sprint-ets-17-adversarial-implementation.yaml`
  - Verdict: `APPROVE` confidence 0.91.
  - Required fixes: none.

Sprint ets-16 SensorML non-system read-only expansion:

- OGC source verification:
  - Source: `api/part1/standard/requirements/encoding/sensorml/requirements_class_sensorml.adoc`
  - Result: HTTP 200 on 2026-05-06.
  - Listed subrequirements include deployment, procedure, and property SensorML schema/mapping paths.
- GeoRobotix planning probes:
  - `/conformance`: HTTP 200; declares `/conf/sensorml`, `/conf/deployment`, `/conf/procedure`, and `/conf/property`.
  - `GET /deployments?limit=1` with `Accept: application/sml+json`: HTTP 200, `Content-Type: application/json`, top-level `items`.
  - `GET /procedures?limit=1` with `Accept: application/sml+json`: HTTP 200, `Content-Type: application/json`, top-level `items` and `links`.
  - `GET /properties?limit=1` with `Accept: application/sml+json`: HTTP 200, `Content-Type: application/json`, top-level `items`, currently empty.
- SensorML item evidence:
  - `GET /deployments/16sp744ch58g?f=sml3`: HTTP 200, `Content-Type: application/sml+json`, `type=Deployment`, matching `id`, `uniqueId`, and `deployedSystems`.
  - `GET /procedures/164p7ed8l47g?f=sml3`: HTTP 200, `Content-Type: application/sml+json`, `type=PhysicalSystem`, matching `id`, `uniqueId`, and procedure structure.
- Interpretation: Sprint 16 must not count CS API default `items` wrappers or Feature JSON as SensorML PASS evidence. Empty `/properties` is an IUT-state SKIP condition, not PASS. Sampling features are out of scope because upstream SensorML subrequirements list property schema/mapping, not sampling feature schema/mapping.
- Planning review:
  - `.harness/evaluations/sprint-ets-16-plan-adversarial.yaml`: `GAPS_FOUND` confidence 0.86.
  - Required fixes applied: explicit resource conformance-class gating for `/conf/deployment`, `/conf/procedure`, and `/conf/property`; procedure-specific mapping now requires non-identity process/procedure structure, not `identifiers` alone.
  - `.harness/evaluations/sprint-ets-16-plan-gapfix.yaml`: `APPROVE` confidence 0.94, no remaining required fixes.
- Generator implementation:
  - `SensorMlTests` now has 9 read-only @Tests, adding deployment, procedure, and property SensorML schema/mapping checks.
  - Deployment/procedure/property checks gate on matching resource conformance classes before judging SensorML evidence.
  - CS API default `items` wrappers and empty collections SKIP with requirement-cited reasons rather than PASS.
  - Deployment mapping requires explicit SensorML `type=Deployment`, preserved identity, and non-empty `deployedSystems`.
  - Procedure mapping requires explicit SensorML procedure-compatible type, preserved identity, and non-identity process/procedure structure beyond identifiers.
  - Property mapping requires property-compatible SensorML and mapping evidence when a property item exists; GeoRobotix currently has no property items.
  - `VerifySensorMlResourceMappingAssertions` adds 6 focused helper regressions.
- Maven verification:
  - Command: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `128 tests / 0 failures / 0 errors / 3 skipped`
  - Log: `ops/test-results/sprint-ets-16-maven-2026-05-06.log`
- TeamEngine E2E smoke:
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s16-generator bash scripts/smoke-test.sh`
  - Result: `80 total / 54 passed / 0 failed / 26 skipped`
  - Report: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s16-generator/s-ets-01-03-teamengine-smoke-2026-05-06.xml`
  - Log: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s16-generator/s-ets-01-03-teamengine-container-2026-05-06.log`
  - No-mutation oracle: recognized 53 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH entries for `https://api.georobotix.io/ogc/t18/api`.
- Runtime outcome:
  - `deploymentSensorMlHasSchemaAndMapping`: PASS using `application/sml+json` alternate link `https://api.georobotix.io/ogc/t18/api/deployments/16sp744ch58g?f=sml3`.
  - `procedureSensorMlHasSchemaAndMapping`: PASS using `application/sml+json` alternate link `https://api.georobotix.io/ogc/t18/api/procedures/164p7ed8l47g?f=sml3`.
  - `propertySensorMlHasSchemaAndMapping`: SKIP because `/properties` returned an empty collection.
- Raze implementation review:
  - Artifact: `.harness/evaluations/sprint-ets-16-adversarial-implementation.yaml`
  - Verdict: `APPROVE` confidence 0.92.
  - Required fixes: none.

Sprint ets-15 GeoJSON non-system read-only expansion:

- OGC source verification:
  - Source: `api/part1/standard/requirements/encoding/geojson/requirements_class_geojson.adoc`
  - Result: HTTP 200 on 2026-05-06.
  - Listed subrequirements include deployment, procedure, and sampling feature schema/mapping paths.
- GeoRobotix planning probes:
  - `/conformance`: HTTP 200; declares `/conf/geojson`, `/conf/deployment`, `/conf/procedure`, and `/conf/sf`.
  - `GET /deployments?limit=1` with `Accept: application/geo+json`: HTTP 200, `Content-Type: application/json`, top-level `items`.
  - `GET /procedures?limit=1` with `Accept: application/geo+json`: HTTP 200, `Content-Type: application/json`, top-level `items` and `links`.
  - `GET /samplingFeatures?limit=1` with `Accept: application/geo+json`: HTTP 200, `Content-Type: application/json`, top-level `items` and `links`.
- Resource-specific mapping evidence visible in fallback payloads:
  - Deployment item: `properties.deployedSystems@link`.
  - Procedure item: `geometry: null` and `properties.featureType`.
  - Sampling Feature item: `properties.hostedProcedure@link` and `properties.radius`.
- Generator implementation:
  - `GeoJsonTests` now adds read-only `/deployments`, `/procedures`, and `/samplingFeatures` GeoJSON schema/mapping checks.
  - CS API default `items` wrappers without GeoJSON `features` SKIP with requirement-cited fallback reasons rather than PASS.
  - Generic Feature shape alone does not close schema/mapping assertions; deployment, procedure, and sampling feature checks require resource-specific predicates.
  - `VerifyGeoJsonResourceMappingAssertions` adds focused helper coverage for fallback SKIP and mapping-value handling.
- Maven verification:
  - Command: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `122 tests / 0 failures / 0 errors / 3 skipped`
  - Log: `ops/test-results/sprint-ets-15-maven-2026-05-06.log`
- TeamEngine E2E smoke:
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s15-generator bash scripts/smoke-test.sh`
  - Result: `77 total / 52 passed / 0 failed / 25 skipped`
  - Report: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s15-generator/s-ets-01-03-teamengine-smoke-2026-05-06.xml`
  - Log: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s15-generator/s-ets-01-03-teamengine-container-2026-05-06.log`
  - No-mutation oracle: recognized 44 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH entries for `https://api.georobotix.io/ogc/t18/api`.
- Interpretation: Sprint 15 does not count CS API default `items` wrappers as GeoJSON FeatureCollection PASS evidence. They SKIP until `features` is observed, and generic Feature shape alone does not close resource-specific schema/mapping assertions.
- Raze planning review:
  - Artifact: `.harness/evaluations/sprint-ets-15-plan-adversarial.yaml`
  - Verdict: `GAPS_FOUND` confidence 0.86.
  - Required fix: add resource-specific predicates before schema/mapping PASS.
- Raze planning gap-fix recheck:
  - Artifact: `.harness/evaluations/sprint-ets-15-plan-gapfix.yaml`
  - Verdict: `APPROVE` confidence 0.93.
  - Required fixes: none remaining.
- Raze implementation review:
  - Artifact: `.harness/evaluations/sprint-ets-15-adversarial-implementation.yaml`
  - Verdict: `APPROVE_WITH_CONCERNS` confidence 0.91.
  - Concern: stale `GeoJsonTests` class javadoc still described non-system GeoJSON as future work.
- Raze implementation gap-fix recheck:
  - Artifact: `.harness/evaluations/sprint-ets-15-adversarial-gapfix.yaml`
  - Verdict: `APPROVE` confidence 0.96.
  - Required fixes: none remaining.

Sprint ets-14 Update positive mutable-IUT hardening:

- Planning probe:
  - Local OSH IUT host URL: `http://localhost:8081/sensorhub/api`
  - Seeded System resource: `/systems/040g`
  - `OPTIONS /systems/040g`: HTTP 200, `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS`; PATCH absent.
  - Simple authenticated `/conformance` curl probe returned HTTP 401 with attempted basic credentials; established TeamEngine smoke credential path remains the authoritative local path.
- Interpretation: local OSH remains a dedicated mutable CRD fixture, but current evidence does not support positive Update/PATCH execution. Sprint 14 Generator must not issue PATCH unless `/conf/update`, `OPTIONS PATCH`, and changed-field verification are all available. If a future probe confirms `/conf/update` while successful `OPTIONS /systems/{id}` still omits PATCH, the readiness assertion should FAIL for `/req/update/system` and lifecycle should SKIP before PATCH.
- Maven verification:
  - Command: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `117 tests / 0 failures / 0 errors / 3 skipped`
  - Log: `ops/test-results/sprint-ets-14-maven-2026-05-06.log`
- TeamEngine E2E smoke:
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s14-generator bash scripts/smoke-test.sh`
  - Result: `74 total / 52 passed / 0 failed / 22 skipped`
  - Report: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s14-generator/s-ets-01-03-teamengine-smoke-2026-05-06.xml`
  - Log: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s14-generator/s-ets-01-03-teamengine-container-2026-05-06.log`
  - No-mutation oracle: recognized 41 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH entries for `https://api.georobotix.io/ogc/t18/api`.
- Local OSH readiness probe:
  - `/conformance`: HTTP 401 at `http://localhost:8081/sensorhub/api/conformance`.
  - `OPTIONS /systems/040g`: HTTP 200, `Allow: GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS`; PATCH absent.
  - No local OSH PATCH was issued.
- Raze implementation review:
  - Artifact: `.harness/evaluations/sprint-ets-14-adversarial-implementation.yaml`
  - Verdict: `GAPS_FOUND` confidence 0.84.
  - Required fix: add REQ/SCENARIO trace comments to `VerifyUpdateChangedFieldAssertion`.
- Raze gap-fix recheck:
  - Artifact: `.harness/evaluations/sprint-ets-14-adversarial-gapfix.yaml`
  - Verdict: `APPROVE` confidence 0.94.
  - Required fixes: none remaining.

Sprint ets-13 Update/PATCH safety-gated systems subset:

- Current repo base before implementation: `21c409c`
- Maven verification: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `113 tests / 0 failures / 0 errors / 3 skipped`
  - Log: `ops/test-results/sprint-ets-13-maven-2026-05-06.log`
- TeamEngine E2E smoke:
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results bash scripts/smoke-test.sh`
  - Result: `74 total / 52 passed / 0 failed / 22 skipped`
  - Report: `/tmp/ets-ogcapi-connectedsystems10-smoke-results/s-ets-01-03-teamengine-smoke-2026-05-06.xml`
  - Log: `/tmp/ets-ogcapi-connectedsystems10-smoke-results/s-ets-01-03-teamengine-container-2026-05-06.log`
- No-mutation oracle: integrated smoke oracle recognized 41 IUT-bound request-log entries and reported zero IUT-bound POST/PUT/DELETE/PATCH entries for `https://api.georobotix.io/ogc/t18/api`.
- Update runtime outcome against GeoRobotix: the Update configuration method skips with the missing `/conf/update` reason, and the 5 Update @Tests are skipped in default smoke through the `update -> createreplacedelete` dependency because Create/Replace/Delete's public-IUT mutation safety gate intentionally skips. No PATCH was issued.
- Raze implementation review: `.harness/evaluations/sprint-ets-13-adversarial-implementation.yaml` reported `GAPS_FOUND` 0.86 for documentation/evidence gaps; required fixes applied by updating stale headers/status, archiving the Maven log, and documenting dependency-skip masking.
- Quinn independent Gate 3.5:
  - Gate artifact: `.harness/evaluations/sprint-ets-13-evaluator-gate.yaml`
  - Verdict: `APPROVE_WITH_CONCERNS` confidence 0.91
  - Clone: `/tmp/quinn-sprint-ets-13-cd38223` at `cd3822369f3c9b3d99efb61ea623560ca9516446`
  - Maven command: `bash scripts/mvn-test-via-docker.sh`
  - Maven result: BUILD SUCCESS, `113 tests / 0 failures / 0 errors / 3 skipped`
  - Smoke command: `SMOKE_CONTAINER_NAME=quinn-ets13-gate-smoke SMOKE_IMAGE_TAG=ets-ogcapi-connectedsystems10:quinn-ets13-gate SMOKE_OUTPUT_DIR=/tmp/quinn-ets13-gate-smoke-results bash scripts/smoke-test.sh`
  - Smoke result: `74 total / 52 passed / 0 failed / 22 skipped`
  - Smoke report: `/tmp/quinn-ets13-gate-smoke-results/s-ets-01-03-teamengine-smoke-2026-05-06.xml`
  - Smoke log: `/tmp/quinn-ets13-gate-smoke-results/s-ets-01-03-teamengine-container-2026-05-06.log`
  - No-mutation oracle replay: `recognized_iut_request_logs=41`; zero IUT-bound POST/PUT/DELETE/PATCH.
  - Update runtime outcome: `fetchUpdateInputs` SKIP cites missing `/conf/update`; the 5 Update @Tests SKIP through the `update -> createreplacedelete` dependency because the default CRD mutation safety gate skips lifecycle mutation. No PATCH was issued.
- Raze review of Quinn artifact: `.harness/evaluations/sprint-ets-13-quinn-gate-raze-review.yaml` `APPROVE` confidence 0.89 after correcting the Quinn artifact file list.
- Raze independent Gate 4:
  - Gate artifact: `.harness/evaluations/sprint-ets-13-adversarial-gate.yaml`
  - Verdict: `APPROVE_WITH_CONCERNS` confidence 0.90
  - Clone: `/tmp/raze-sprint-ets-13-gate` at `cd38223`
  - Maven command: `bash scripts/mvn-test-via-docker.sh`
  - Maven result: BUILD SUCCESS, `113 tests / 0 failures / 0 errors / 3 skipped`
  - No-mutation oracle self-test: PASS
  - Smoke command: `SMOKE_CONTAINER_NAME=raze-ets-csapi-smoke-s13-gate SMOKE_IMAGE_TAG=ets-ogcapi-connectedsystems10:raze-s13-gate SMOKE_OUTPUT_DIR=/tmp/raze-sprint-ets-13-gate-smoke-results SMOKE_RUN_TIMEOUT_S=900 bash scripts/smoke-test.sh`
  - Smoke result: `74 total / 52 passed / 0 failed / 22 skipped`
  - Smoke report: `/tmp/raze-sprint-ets-13-gate-smoke-results/s-ets-01-03-teamengine-smoke-2026-05-06.xml`
  - Smoke log: `/tmp/raze-sprint-ets-13-gate-smoke-results/s-ets-01-03-teamengine-container-2026-05-06.log`
  - No-mutation oracle replay: `recognized_iut_request_logs=41`; zero IUT-bound POST/PUT/DELETE/PATCH.
  - Required fixes: none. Low follow-up: decide `OPTIONS Allow: PATCH` readiness semantics before using Sprint 13 as a positive mutable-IUT conformance gate.
- Scope note: this is PARTIAL for REQ-ETS-PART1-011. Deployment/procedure/sampling-feature/property PATCH, Feature Collection update paths, Part 2 update, optimistic locking, and PATCH media-type matrix remain open.

Sprint ets-12 Create/Replace/Delete safety-gated systems subset:

- Current repo base before implementation: `7427c3c`
- Maven verification: `bash scripts/mvn-test-via-docker.sh`
  - Original Generator result: BUILD SUCCESS, `105 tests / 0 failures / 0 errors / 3 skipped`
  - Local OSH follow-up result after Location/UID regressions: BUILD SUCCESS, `110 tests / 0 failures / 0 errors / 3 skipped`
- TeamEngine E2E smoke:
  - Copy: `/tmp/sprint-ets-12-generator-smoke-current-r3`
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-smoke-results-s12-generator-r3 bash scripts/smoke-test.sh`
  - Result: `69 total / 52 passed / 0 failed / 17 skipped`
  - Report: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s12-generator-r3/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
  - Log: `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s12-generator-r3/s-ets-01-03-teamengine-container-2026-05-05.log`
- CreateReplaceDelete runtime outcome: 4 PASS and 2 SKIP against GeoRobotix. PASS: declaration, dependency tracer, `OPTIONS /systems`, `OPTIONS /systems/{id}`. SKIP: mutation safety gate and lifecycle opt-in because default smoke does not set mutation parameters.
- No-mutation oracle: integrated smoke oracle recognized 40 IUT-bound request log entries and reported zero IUT-bound POST/PUT/DELETE entries for `https://api.georobotix.io/ogc/t18/api`.
- Raze gap-fix review: `.harness/evaluations/sprint-ets-12-adversarial-gapfix.yaml` `APPROVE_WITH_CONCERNS` 0.91, with no required fixes remaining. Residual low concern: smoke stdout was not archived separately, but the oracle result is reproducible from the r3 container log.
- Local OSH mutable-IUT probe:
  - IUT: local OpenSensorHub 2.0-beta2 at `http://localhost:8081/sensorhub/api`; TeamEngine reached it as `http://field-hub-osh-1:8081/sensorhub/api` on Docker network `field-hub_default`.
  - Command shape: `SMOKE_DOCKER_NETWORK=field-hub_default`, `SMOKE_MUTATION_TESTS_ENABLED=true`, `SMOKE_MUTATION_IUT_POLICY=dedicated-mutable-iut`, output `/tmp/ets-csapi-osh-mutable-smoke-r4`.
  - Result: `69 total / 32 passed / 3 failed / 34 skipped`; not a full-suite PASS.
  - CRD lifecycle outcome: `systemsCreateReplaceDeleteLifecycle` PASS. OSH log evidence shows POST added `/systems/0410`, PUT updated `/systems/0410`, and DELETE removed `/systems/0410`.
  - Remaining local OSH failures: empty `/procedures`, `/deployments`, and `/samplingFeatures`; prior probe also exposed public `https://osh.gis.tw` SensorML alternate links from OSH `proxyBaseUrl`.
  - Cleanup: manual seed `/systems/040g` deleted after the run; `/systems` returned `{"items":[]}`.
- Local OSH full-health run after fixture seeding:
  - OSH config: `proxyBaseUrl` updated to `http://field-hub-osh-1:8081` in `../sar-ops/field-hub/osh/config/config.json`, then OSH restarted.
  - Seed state: synthetic `/systems/040g`, `/procedures/040g`, `/deployments/040g`, and `/samplingFeatures/040g` exist. Exact payloads are versioned in `ops/local-osh-seed-fixtures.json`. System seed uses `featureType=http://www.w3.org/ns/sosa/System`; direct `/systems/040g?f=sml3` returned HTTP 200 and `Content-Type: application/sml+json`.
  - Command: `SMOKE_CONTAINER_NAME=ets-csapi-osh-full-health-r3 SMOKE_DOCKER_NETWORK=field-hub_default SMOKE_IUT_URL=http://field-hub-osh-1:8081/sensorhub/api SMOKE_MUTATION_TESTS_ENABLED=true SMOKE_MUTATION_IUT_POLICY=dedicated-mutable-iut SMOKE_OUTPUT_DIR=/tmp/ets-csapi-osh-full-health-r3 bash scripts/smoke-test.sh`
  - Result: SMOKE PASS, `69 total / 50 passed / 0 failed / 19 skipped`.
  - Smoke stdout now prints exact parsed totals: `SMOKE PASS: total=69 passed=50 failed=0 skipped=19 ...`.
  - Report: `/tmp/ets-csapi-osh-full-health-r3/s-ets-01-03-teamengine-smoke-2026-05-06.xml`
  - Log: `/tmp/ets-csapi-osh-full-health-r3/s-ets-01-03-teamengine-container-2026-05-06.log`
  - CRD lifecycle: real POST, PUT, and DELETE against temporary `/systems/0410`; no-mutation oracle skipped by design because the run explicitly enabled dedicated mutable-IUT mutation tests.
- Raze local OSH full-health review: `.harness/evaluations/sprint-ets-12-local-osh-full-health-raze.yaml` `GAPS_FOUND` 0.87. Required fixes applied: `scripts/smoke-test.sh` now prints exact totals (`total/passed/failed/skipped`) instead of `${total}/${total}`, and `ops/local-osh-seed-fixtures.json` versions the fixture payloads.
- Scope note: this is PARTIAL for REQ-ETS-PART1-010. Positive System CRD lifecycle evidence now exists against local OSH, but deployment/procedure/sampling-feature/property CRUD, system delete cascade, collection propagation, `text/uri-list`, `/conf/update`, PATCH, and Part 2 are still out of scope.

Sprint ets-11 AdvancedFiltering read-only subset:

- Current repo base before implementation: `5cdcdf4`
- Maven verification: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `98 tests / 0 failures / 0 errors / 3 skipped`
- TeamEngine E2E smoke:
  - Clone/copy: `/tmp/sprint-ets-11-generator-smoke`
  - Command: `SMOKE_CONTAINER_NAME=sprint-ets-11-generator-smoke SMOKE_OUTPUT_DIR=/tmp/sprint-ets-11-generator-smoke-results bash scripts/smoke-test.sh`
  - Result: `63 total / 48 passed / 0 failed / 15 skipped`
  - Report: `/tmp/sprint-ets-11-generator-smoke-results/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
  - Log: `/tmp/sprint-ets-11-generator-smoke-results/s-ets-01-03-teamengine-container-2026-05-05.log`
- AdvancedFiltering outcome: 6 AdvancedFiltering @Tests SKIP with reason because current GeoRobotix does not declare `/conf/advanced-filtering`.
- Scope note: this is PARTIAL for REQ-ETS-PART1-009; mutation behavior, Part 2, full cross-resource association filters, full geometry intersection semantics, combined-filter truth tables, and endpoint parity remain open.
- Quinn independent Gate 3.5:
  - Maven command: `bash scripts/mvn-test-via-docker.sh`
  - Maven clone: `/tmp/quinn-sprint-ets-11-gate`
  - Maven result: BUILD SUCCESS, `98 tests / 0 failures / 0 errors / 3 skipped`
  - Maven log: `/tmp/quinn-ets-csapi-mvn-s11.log`
  - Maven note: first worktree invocation exited BUILD FAILURE after report XML totals `98/0/0/3` due surefire fork ClassNotFoundException for `VerifyMaskingRequestLoggingFilter`; the later `/tmp` clone rerun succeeded with persisted log evidence and the same totals.
  - Smoke clone: `/tmp/quinn-sprint-ets-11-gate`
  - Smoke command: `SMOKE_CONTAINER_NAME=quinn-ets-csapi-smoke-s11 SMOKE_OUTPUT_DIR=/tmp/quinn-ets-csapi-smoke-s11-results bash scripts/smoke-test.sh`
  - Smoke result: `63 total / 48 passed / 0 failed / 15 skipped`
  - AdvancedFiltering runtime outcome: 6 SKIP, 0 PASS, 0 FAIL, with missing `/conf/advanced-filtering` reason
  - Report: `/tmp/quinn-ets-csapi-smoke-s11-results/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
  - Gate artifact: `.harness/evaluations/sprint-ets-11-evaluator-gate.yaml`
- Raze independent Gate 4:
  - Maven result: BUILD SUCCESS, `98 tests / 0 failures / 0 errors / 3 skipped`
  - AdvancedFiltering lint evidence: `testAdvancedFilteringGroupDependsOnSystemFeatures`, `testEveryAdvancedFilteringTestMethodCarriesAdvancedFilteringGroup`, and `testAdvancedFilteringCoLocatedWithSystemFeatures` present in `VerifyTestNGSuiteDependency` surefire XML
  - Smoke command: `SMOKE_CONTAINER_NAME=raze-ets-csapi-smoke-s11 SMOKE_OUTPUT_DIR=/tmp/raze-sprint-ets-11-smoke-results bash scripts/smoke-test.sh`
  - Smoke result: `63 total / 48 passed / 0 failed / 15 skipped`
  - AdvancedFiltering runtime outcome: 6 SKIP, 0 PASS, 0 FAIL, with missing `/conf/advanced-filtering` reason
  - Report: `/tmp/raze-sprint-ets-11-smoke-results/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
  - Gate artifact: `.harness/evaluations/sprint-ets-11-adversarial-gate.yaml`

Sprint ets-10 SensorML systems read-only subset:

- Current repo base before implementation: `e7ba5f1`
- Maven verification: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `95 tests / 0 failures / 0 errors / 3 skipped`
- TeamEngine E2E smoke:
  - Clone: `/tmp/sprint-ets-10-generator-smoke-git-r2`
  - Command: `SMOKE_OUTPUT_DIR=/tmp/ets-ogcapi-connectedsystems10-sprint10-smoke-results-git-r2 bash scripts/smoke-test.sh`
  - Result: `57 total / 48 passed / 0 failed / 9 skipped`
  - Report: `/tmp/ets-ogcapi-connectedsystems10-sprint10-smoke-results-git-r2/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
  - Log: `/tmp/ets-ogcapi-connectedsystems10-sprint10-smoke-results-git-r2/s-ets-01-03-teamengine-container-2026-05-05.log`
- SensorML outcome: 6 SensorML @Tests PASS. Runtime report records `SensorML representation source: application/sml+json alternate link (https://api.georobotix.io/ogc/t18/api/systems/0mqcvdnfoca0?f=sml3)`.
- Scope note: this is PARTIAL for REQ-ETS-PART1-013; write media type, relation types, non-system schema/mapping, and full SensorML 3.0 JSON Schema validation remain open.
- Raze implementation review: `.harness/evaluations/sprint-ets-10-adversarial-implementation.yaml` found two gaps; both were fixed.
- Raze gap-fix review: `.harness/evaluations/sprint-ets-10-adversarial-gapfix.yaml` APPROVE 0.93, no final blockers.
- Quinn independent gate:
  - Maven clone: `/tmp/quinn-sprint-ets-10`
  - Maven result: BUILD SUCCESS, `95 tests / 0 failures / 0 errors / 3 skipped`
  - SensorML lint evidence: `testSensorMlGroupDependsOnSystemFeatures`, `testEverySensorMlTestMethodCarriesSensorMlGroup`, and `testSensorMlCoLocatedWithSystemFeatures` present in `VerifyTestNGSuiteDependency` surefire XML
  - Smoke command: `SMOKE_CONTAINER_NAME=quinn-ets-csapi-smoke-s10 SMOKE_OUTPUT_DIR=/tmp/quinn-sprint-ets-10-smoke-results bash scripts/smoke-test.sh`
  - Smoke result: `57 total / 48 passed / 0 failed / 9 skipped`
  - Report: `/tmp/quinn-sprint-ets-10-smoke-results/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
  - Gate artifact: `.harness/evaluations/sprint-ets-10-evaluator-gate.yaml`
- Raze independent Gate 4:
  - Maven clone: `/tmp/raze-sprint-ets-10`
  - Maven result: BUILD SUCCESS, `95 tests / 0 failures / 0 errors / 3 skipped`
  - Smoke command: `SMOKE_CONTAINER_NAME=raze-ets-csapi-smoke-s10 SMOKE_OUTPUT_DIR=/tmp/raze-sprint-ets-10-smoke-results bash scripts/smoke-test.sh`
  - Smoke result: `57 total / 48 passed / 0 failed / 9 skipped`
  - SensorML outcome: 6 PASS, 0 failed, 0 skipped
  - Report: `/tmp/raze-sprint-ets-10-smoke-results/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
  - Gate artifact: `.harness/evaluations/sprint-ets-10-adversarial-gate.yaml`

Sprint ets-09 GeoJSON systems read-only subset:

- Current repo HEAD: `880b391`
- Implementation commits: `28f4ddf` and `b4a97de`
- Maven verification: `bash scripts/mvn-test-via-docker.sh`
  - Result: BUILD SUCCESS
  - Surefire: `92 tests / 0 failures / 0 errors / 3 skipped`
- TeamEngine E2E smoke:
  - Clone: `/tmp/sprint-ets-09-smoke-fix`
  - Command: `SMOKE_OUTPUT_DIR=/tmp/sprint-ets-09-smoke-fix-results bash scripts/smoke-test.sh`
  - Result: `51 total / 42 passed / 0 failed / 9 skipped`
  - Report: `/tmp/sprint-ets-09-smoke-fix-results/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
  - Log: `/tmp/sprint-ets-09-smoke-fix-results/s-ets-01-03-teamengine-container-2026-05-05.log`
- Quinn independent gate:
  - Maven clone: `/tmp/quinn-sprint-ets-09`
  - Maven result: BUILD SUCCESS, `92 tests / 0 failures / 0 errors / 3 skipped`
  - Smoke command: `SMOKE_CONTAINER_NAME=quinn-ets-csapi-smoke-2 SMOKE_OUTPUT_DIR=/tmp/quinn-sprint-ets-09-smoke-results-2 SMOKE_RUN_TIMEOUT_S=1200 bash scripts/smoke-test.sh`
  - Smoke result: `51 total / 42 passed / 0 failed / 9 skipped`
  - Report: `/tmp/quinn-sprint-ets-09-smoke-results-2/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
  - Gate artifact: `.harness/evaluations/sprint-ets-09-evaluator-gate.yaml`
- Raze independent gate:
  - Maven clone: `/tmp/raze-sprint-ets-09-review`
  - Maven result: BUILD SUCCESS, `92 tests / 0 failures / 0 errors / 3 skipped`
  - Smoke command: `SMOKE_OUTPUT_DIR=/tmp/raze-sprint-ets-09-smoke-results bash scripts/smoke-test.sh`
  - Smoke result: `51 total / 42 passed / 0 failed / 9 skipped`
  - Report: `/tmp/raze-sprint-ets-09-smoke-results/s-ets-01-03-teamengine-smoke-2026-05-05.xml`
  - Gate artifact: `.harness/evaluations/sprint-ets-09-adversarial-gate.yaml`

GeoJSON outcome: 2 PASS + 3 SKIP. GeoRobotix declares `/conf/geojson`, but `/systems` with `Accept: application/geo+json` returns `Content-Type: application/json` and top-level `items`; the ETS does not count that as mediatype-read, FeatureCollection, or mapping PASS.

Gate verdicts: Quinn APPROVE_WITH_CONCERNS 0.90 and Raze APPROVE_WITH_CONCERNS 0.88. No blockers or required fixes were found.

## Artifact Location

Persistent ETS evidence lives in this repository under `ops/test-results/`. Recent Sprint 12, Sprint 11, Sprint 10, and Sprint 9 smoke artifacts currently live under `/tmp/...` gate directories, including `/tmp/ets-ogcapi-connectedsystems10-smoke-results-s12-generator-r3/`, `/tmp/sprint-ets-11-generator-smoke-results/`, `/tmp/quinn-sprint-ets-10-smoke-results/`, and `/tmp/raze-sprint-ets-10-smoke-results/`, because the gate runs intentionally avoided polluting the worktree.

## Historical Evidence

Earlier Sprint 1–8 runtime artifacts already present in `ops/test-results/` include TeamEngine smoke XMLs, container logs, sabotage cascade XMLs, bash traces, and surefire output.
