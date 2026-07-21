#!/usr/bin/env bash
# REQ-ETS-TEAMENGINE-003, REQ-ETS-TEAMENGINE-007, REQ-ETS-CLEANUP-021.
# SCENARIO-ETS-TEAMENGINE-TE6-IMAGE-PROVENANCE-001.
# SCENARIO-ETS-TEAMENGINE-TE6-BASE-IMMUTABILITY-001.
# SCENARIO-ETS-TEAMENGINE-TE6-RUNTIME-INVARIANTS-001.
# SCENARIO-ETS-CLEANUP-CONFIDENTIAL-BUILD-CONTEXT-001.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

IMAGE_REF="${TEAMENGINE_RUNTIME_IMAGE:-ogccite/teamengine-dev@sha256:981b71566d56434576843798ae8072db15be8478eb7dc724b051c2228460f43c}"
FINAL_IMAGE_REF="${TEAMENGINE_FINAL_IMAGE:-ets-ogcapi-connectedsystems10:sprint41}"
EXPECTED_DIGEST="sha256:981b71566d56434576843798ae8072db15be8478eb7dc724b051c2228460f43c"

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

grep -Fq "FROM ogccite/teamengine-dev@$EXPECTED_DIGEST" Dockerfile \
  || fail "Dockerfile runtime digest is not the approved TeamEngine 6 digest"
grep -Fq "<teamengine.runtime.image.digest>$EXPECTED_DIGEST</teamengine.runtime.image.digest>" pom.xml \
  || fail "POM runtime digest does not match Dockerfile"

if grep -Fq "USER root" Dockerfile; then
  fail "Dockerfile switches the runtime stage to root"
fi
if grep -Fq "teamengine-*.jar" Dockerfile; then
  fail "Dockerfile uses an unbounded TeamEngine dependency wildcard"
fi
if grep -Eq '^RUN .*\b(sed|chmod|chown|rm)\b.*(webapps/teamengine|te_base)' Dockerfile; then
  fail "Dockerfile mutates TeamEngine-owned base-image files"
fi

grep -Fq "target/lib-runtime-selected/" Dockerfile \
  || fail "Dockerfile does not copy the reviewed runtime selection"
grep -Fq "org.opengis.cite.teamengine:teamengine-resources:6.0.0" Dockerfile \
  || fail "reviewed runtime selection omits TeamEngine resources 6.0.0"
if grep -Fq "/build/target/lib-runtime/ /usr/local/tomcat" Dockerfile; then
  fail "Dockerfile copies the unfiltered dependency closure into TeamEngine"
fi
grep -Fq "maven-shade-plugin" pom.xml \
  || fail "NetworkNT validator isolation is not configured"
grep -Fq "internal.networknt.schema" pom.xml \
  || fail "NetworkNT validator packages are not relocated"
if grep -Eq '(json-schema-validator|guava|checker-qual|commons-logging)-[^ ]+\.jar' Dockerfile; then
  fail "Dockerfile selects a split-version library already supplied by TeamEngine"
fi
grep -Fq "test ! -e /usr/local/tomcat/te_base/scripts/ogcapi-connectedsystems10" Dockerfile \
  || fail "CTL installation does not reject base-path collisions"
if grep -Fq "unzip -q -o" Dockerfile; then
  fail "CTL installation permits overwriting a base path"
fi

tracked_protected="$(git ls-files | grep -E '(^|/)(te2_webapp|CITE_.*Transcript|.*:Zone.Identifier|f10m\.xml$)' || true)"
[[ -z "$tracked_protected" ]] || fail "protected or scratch files are tracked: $tracked_protected"

history_protected_count="$(git log --all --name-only --pretty=format: \
  | grep -Ec '(^|/)(te2_webapp|CITE_.*Transcript|.*:Zone.Identifier|f10m\.xml$)' || true)"
[[ "$history_protected_count" == "0" ]] \
  || fail "protected or scratch filenames occur in Git history"

for local_reference in te2_webapp.txt "CITE_ Connected Systems ETS telecon Transcript.txt"; do
  if [[ -e "$local_reference" ]]; then
    git check-ignore -q "$local_reference" || fail "reference file is not ignored: $local_reference"
  fi
done

context_image="$(docker build --quiet --target build-context-audit .)"
trap 'docker image rm -f "$context_image" >/dev/null 2>&1 || true' EXIT
docker run --rm --entrypoint sh "$context_image" -c '
  set -eu
  count="$(find /context -type f \( -name "te2_webapp*" -o -name "CITE_*Transcript*" -o -name "*:Zone.Identifier" -o -name "f10m.xml" \) | wc -l)"
  test "$count" -eq 0
' || fail "protected or scratch files entered the effective Docker build context"

config_json="$(docker image inspect "$IMAGE_REF" --format '{{json .Config}}')"
grep -Fq '"User":"tomcat"' <<<"$config_json" || fail "base image does not run as tomcat"
grep -Fq '"JAVA_VERSION=jdk-17.0.15+6"' <<<"$config_json" || fail "unexpected base JDK"
grep -Fq '"TOMCAT_VERSION=10.1.42"' <<<"$config_json" || fail "unexpected base Tomcat"
grep -Fq '"Cmd":["catalina.sh","run"]' <<<"$config_json" || fail "unexpected base startup command"

docker run --rm --entrypoint sh "$IMAGE_REF" -c '
  set -eu
  test "$(id -u)" -ne 0
  command -v curl >/dev/null
  command -v unzip >/dev/null
  test -d /usr/local/tomcat/webapps/teamengine/WEB-INF/lib
  test -d /usr/local/tomcat/te_base/scripts
  test -w /usr/local/tomcat/webapps/teamengine/WEB-INF/lib
  test -w /usr/local/tomcat/te_base/scripts
  for jar in teamengine-spi-6.0.0.jar teamengine-core-6.0.0.jar; do
    test -f "/usr/local/tomcat/webapps/teamengine/WEB-INF/lib/$jar"
  done
  test ! -f /usr/local/tomcat/webapps/teamengine/WEB-INF/lib/teamengine-resources-6.0.0.jar
'

docker image inspect "$FINAL_IMAGE_REF" >/dev/null 2>&1 \
  || fail "final ETS image is unavailable for immutable-base verification: $FINAL_IMAGE_REF"
diff -u \
  <(docker run --rm --entrypoint sh "$IMAGE_REF" -c '
      cd /usr/local/tomcat
      find webapps/teamengine te_base/scripts -type f -exec sha256sum {} + | sort
    ') \
  <(docker run --rm --entrypoint sh "$FINAL_IMAGE_REF" -c '
      cd /usr/local/tomcat
      find webapps/teamengine te_base/scripts -type f \
        ! -path "webapps/teamengine/WEB-INF/lib/ets-ogcapi-connectedsystems10-0.1-SNAPSHOT.jar" \
        ! -path "webapps/teamengine/WEB-INF/lib/teamengine-resources-6.0.0.jar" \
        ! -path "te_base/scripts/ogcapi-connectedsystems10/*" \
        -exec sha256sum {} + | sort
    ') >/dev/null \
  || fail "final image modifies a TeamEngine-owned base file"

echo "PASS: TeamEngine 6 provenance, base-file immutability, runtime invariants, and confidential history/context hygiene"
