#!/usr/bin/env bash
# scripts/mvn-test-via-docker.sh -- ets-ogcapi-connectedsystems10
#
# Sprint 8 S-ETS-08-01 Wedge 6 (REQ-ETS-CLEANUP-019, META-GAP-S7-2 / Quinn W1
# RECURRING-MEDIUM): provide a host-side handle for `mvn clean test` that
# does NOT depend on the host having `mvn` on PATH.
#
# Sprint 1-7 recurring Quinn limitation: Quinn cannot run `mvn clean test`
# directly because the host PATH has no `mvn` binary; only the Dockerfile
# builder stage has Maven. Without a wrapper, Quinn defers surefire-count
# verification to the Docker-baked path, which weakens Quinn's independent
# mvn check at gate time. meta-Raze Sprint 7 META-GAP-S7-2 named this as a
# 7-sprint recurring pattern requiring a wrapper.
#
# Strategy: run `mvn clean test` inside an ephemeral container based on
# `maven:3.9-eclipse-temurin-17-alpine` (matches the JDK 17 baseline declared
# in pom.xml `maven.compiler.source/target/release` 17 -- ADR-007). The
# repo is bind-mounted at /workspace; surefire stdout is streamed back to
# the caller so they see the test run live. The container is removed on
# exit; no persistent state pollutes the user's Docker daemon beyond the
# pulled image.
#
# Usage:
#   bash scripts/mvn-test-via-docker.sh                # default mvn clean test
#   bash scripts/mvn-test-via-docker.sh -DskipTests    # any extra args appended
#   bash scripts/mvn-test-via-docker.sh -Dtest=Foo     # filter to one test class
#
# Hermeticity:
#   - The container does NOT mount the host ~/.m2; deps are downloaded fresh
#     into the container's /root/.m2 each run (acceptable cost, ~30-60s on
#     warm image cache; first run takes longer due to dep download).
#   - The container is stateless beyond the bind-mount; nothing is written
#     back to the host except via the bind-mounted `target/` directory
#     (surefire reports + compiled classes -- expected mvn output).
#
# Exit code: passes through Maven's exit code (0 on success, non-zero on
# build/test failure).

set -eo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

# Maven Docker image -- pinned to a digest-equivalent tag so the script
# behavior is reproducible. Bump only after verifying the new image still
# matches the pom's JDK 17 baseline.
#
# IMPORTANT: the Debian-based eclipse-temurin tag is required because
# pom.xml's buildnumber-maven-plugin shells out to `git` to populate the
# Implementation-Build manifest entry; the alpine maven tag does NOT
# include git in the base image and the buildnumber plugin fails with
# "Cannot run program git" when it is missing.
MAVEN_IMAGE="${MVN_DOCKER_IMAGE:-maven:3.9-eclipse-temurin-17}"

# The pom uses buildnumber-maven-plugin which needs git on PATH. The
# host-side .git directory is bind-mounted via the workspace volume, but
# the Debian image has git pre-installed so no extra setup is needed.
echo "[mvn-test-via-docker] repo root: $REPO_ROOT"
echo "[mvn-test-via-docker] image:     $MAVEN_IMAGE"
echo "[mvn-test-via-docker] command:   mvn clean test -B $*"
echo "[mvn-test-via-docker] (Sprint 8 S-ETS-08-01 Wedge 6 -- closes Quinn host-PATH gap.)"
echo

# -B (--batch-mode) keeps surefire output free of transfer-progress noise so
# the surefire summary at the end stays grep-friendly for gate-time test-count
# verification ('Tests run: N, Failures: F, Errors: E, Skipped: S').
# Run as the host user (--user "$(id -u):$(id -g)") so the target/ output
# directory the container writes into is readable + cleanable from the host
# without sudo. (Default container user is root, which leaves root-owned
# files in the bind-mounted target/ directory.)
docker run --rm \
  --user "$(id -u):$(id -g)" \
  -v "$REPO_ROOT":/workspace \
  -w /workspace \
  -e MAVEN_CONFIG=/tmp/.m2 \
  -e MAVEN_OPTS="-Duser.home=/tmp" \
  "$MAVEN_IMAGE" \
  mvn clean test -B "$@"
