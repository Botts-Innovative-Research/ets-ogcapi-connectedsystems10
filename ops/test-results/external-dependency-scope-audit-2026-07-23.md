# External Dependency Scope Audit - 2026-07-23

## Purpose

Verify CP-003 / ADR-012 before changing or reverting any external repository.
This audit prints no credentials or protected payloads.

## OSH Source and Runtime

```text
remote=https://github.com/opensensorhub/osh-core.git
HEAD=4c87a65c9a967d52af9df476e65d7862c7673a15
HEAD...origin/master ahead=0 behind=3
git status --porcelain lines=0
deployed ConSys Bundle-BuildNumber=4c87a65
/opt/osh mount=/home/nh/docker/osh-core/build/install/osh-core -> /opt/osh rw=false
```

The active OSH checkout is clean and has no local commit ahead of upstream. The
deployed ConSys jar reports the same checkout commit, and the runtime mount is
read-only. This proves no current source drift and reveals no current
project-authored binary-patch evidence. The manifest match is provenance
metadata, not a byte-equivalence proof against an independently trusted binary.

Historical commit `79f89fb` was recorded by Sprint 40 as local-only. The object
is absent from all Git repositories inspected under `/home/nh/docker`, the
historical `/home/nh/docker/gir/osh-core` checkout no longer exists, and no
current fetched remote ref inspected contains it. Project records state it was
not pushed upstream; the current environment alone cannot prove global
non-publication. Its artifacts remain chronology only.

The separate `osh-addons` checkout contains unrelated user work. It was not
modified or reverted by this audit.

## TeamEngine Source and Runtime

```text
TeamEngine source checkouts under /home/nh=0
Dockerfile base=ogccite/teamengine-dev@sha256:981b71566d56434576843798ae8072db15be8478eb7dc724b051c2228460f43c
```

No TeamEngine source checkout or project-authored TeamEngine binary patch was
found. The current Dockerfile uses an immutable OGC image and installs only
ETS-owned artifacts at extension locations. Existing runtime verification
compares base and final image inventories byte-for-byte for TeamEngine-owned
files.

## Disposition

- External source revert: not executed because the audit found no current
  attributable OSH or TeamEngine source drift or binary-patch evidence to revert.
- Historical Sprint 40 OSH patch path: retired as out of scope.
- Hosted CI: dormant GitHub Actions definition and activation instructions
  removed.
- Required gates completed: Docker Maven `313/0/0/3`; exact image
  `sha256:7071cc7694aee2d0b3ca2f44dd2fcad79e9f1eff7b6b2c4de52299adb4704b29`
  passed metadata-aware TeamEngine base verification; TeamEngine E2E against
  this OSH runtime reported `211/69/0/142`, 135 recognized IUT exchanges, zero
  writes, and zero startup errors.
