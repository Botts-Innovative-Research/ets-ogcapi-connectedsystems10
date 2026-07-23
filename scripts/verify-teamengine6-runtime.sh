#!/usr/bin/env bash
# REQ-ETS-TEAMENGINE-003, REQ-ETS-TEAMENGINE-007, REQ-ETS-CLEANUP-021.
# SCENARIO-ETS-TEAMENGINE-TE6-IMAGE-PROVENANCE-001.
# SCENARIO-ETS-TEAMENGINE-TE6-BASE-IMMUTABILITY-001.
# SCENARIO-ETS-TEAMENGINE-TE6-RUNTIME-INVARIANTS-001.
# SCENARIO-ETS-CLEANUP-CONFIDENTIAL-BUILD-CONTEXT-001.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"
source scripts/verify-added-jar-inventory.sh

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

if grep -Fq "target/lib-runtime-selected/" Dockerfile; then
  fail "Dockerfile retains a separate runtime dependency payload"
fi
if grep -Fq "org.opengis.cite.teamengine:teamengine-resources:6.0.0" Dockerfile \
  || grep -Fq "teamengine-resources-6.0.0.jar" Dockerfile; then
  fail "Dockerfile adds a TeamEngine resources coordinate already supplied by the base"
fi
if grep -Fq "/build/target/lib-runtime/ /usr/local/tomcat" Dockerfile; then
  fail "Dockerfile copies the unfiltered dependency closure into TeamEngine"
fi
grep -Fq "maven-shade-plugin" pom.xml \
  || fail "NetworkNT validator isolation is not configured"
grep -Fq "internal.networknt.schema" pom.xml \
  || fail "NetworkNT validator packages are not relocated"
grep -Fq "internal.networknt.schema.i18n.jsv-messages" pom.xml \
  || fail "NetworkNT message bundles are not relocated to an ETS-unique path"
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
inventory_dir="$(mktemp -d)"
trap 'rm -rf "$inventory_dir"; docker image rm -f "$context_image" >/dev/null 2>&1 || true' EXIT
context_audit_output="$(docker run --rm --entrypoint sh "$context_image" -c '
  set -eu
  count="$(find /context -type f \( -name "te2_webapp*" -o -name "CITE_*Transcript*" -o -name "*:Zone.Identifier" -o -name "f10m.xml" \) | wc -l)"
  test "$count" -eq 0
' 2>&1)" || fail "unable to execute build-context audit container: $context_audit_output"

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
  command -v bash >/dev/null
  test -d /usr/local/tomcat/webapps/teamengine/WEB-INF/lib
  test -d /usr/local/tomcat/te_base/scripts
  test -w /usr/local/tomcat/webapps/teamengine/WEB-INF/lib
  test -w /usr/local/tomcat/te_base/scripts
  for jar in teamengine-spi-6.0.0.jar teamengine-core-6.0.0.jar; do
    test -f "/usr/local/tomcat/webapps/teamengine/WEB-INF/lib/$jar"
  done
  test ! -f /usr/local/tomcat/webapps/teamengine/WEB-INF/lib/teamengine-resources-6.0.0.jar
'

base_image_id="$(docker image inspect "$IMAGE_REF" --format '{{.Id}}')"
final_image_id="$(docker image inspect "$FINAL_IMAGE_REF" --format '{{.Id}}' 2>/dev/null)" \
  || fail "final ETS image is unavailable for immutable-base verification: $FINAL_IMAGE_REF"
echo "BASE_IMAGE_ID=$base_image_id"
echo "FINAL_IMAGE_ID=$final_image_id"

validator_payload_output="$(docker run --rm --entrypoint sh "$FINAL_IMAGE_REF" -c '
  set -eu
  lib=/usr/local/tomcat/webapps/teamengine/WEB-INF/lib
  ets_jar="$lib/ets-ogcapi-connectedsystems10-0.1-SNAPSHOT.jar"
  test -f "$ets_jar"
  test -z "$(find "$lib" -maxdepth 1 -name "swecommon30-validator-*.jar" -print -quit)"
  entries="$(unzip -Z1 "$ets_jar")"
  printf "%s\n" "$entries" | grep -Fqx "org/opengis/cite/swecommon30/validation/SweCommonJsonSchemaValidator.class"
  printf "%s\n" "$entries" | grep -Fqx "org/opengis/cite/swecommon30/jsonschema/sweCommon/3.0/json/sweCommon.json"
  printf "%s\n" "$entries" | grep -Fqx "org/opengis/cite/ogcapiconnectedsystems10/internal/networknt/schema/JsonSchema.class"
  printf "%s\n" "$entries" | grep -Fqx "org/opengis/cite/ogcapiconnectedsystems10/internal/networknt/schema/i18n/jsv-messages.properties"
  printf "%s\n" "$entries" | grep -Fqx "org/opengis/cite/ogcapiconnectedsystems10/internal/networknt/schema/i18n/jsv-messages_zh_CN.properties"
  if printf "%s\n" "$entries" | grep -Eq "^jsv-messages.*\\.properties$"; then
    exit 1
  fi
  unzip -p "$ets_jar" "org/opengis/cite/ogcapiconnectedsystems10/internal/networknt/schema/i18n/DefaultMessageSource\$Holder.class" \
    | grep -aFq "org.opengis.cite.ogcapiconnectedsystems10.internal.networknt.schema.i18n.jsv-messages"
  if printf "%s\n" "$entries" | grep -Fq "com/networknt/schema/"; then
    exit 1
  fi
  if printf "%s\n" "$entries" | grep -Fq "BaseJsonSchemaValidatorTest.class"; then
    exit 1
  fi
' 2>&1)" || fail "final ETS jar does not contain the isolated SWE Common validator payload: $validator_payload_output"

validator_execution_output="$(docker run --rm --entrypoint sh "$FINAL_IMAGE_REF" -c '
  set -eu
  lib=/usr/local/tomcat/webapps/teamengine/WEB-INF/lib
  exec java -cp "$lib/*" \
    org.opengis.cite.ogcapiconnectedsystems10.validation.swecommon.SweValidatorRuntimeProbe
' 2>&1)" || fail "deployed SWE Common validator execution probe failed: $validator_execution_output"
grep -Fq "PASS: deployed SWE Common adapter executed valid and invalid components" <<<"$validator_execution_output" \
  || fail "deployed SWE Common validator execution probe did not report success: $validator_execution_output"
echo "$validator_execution_output"

runtime_jar_inventory() {
  docker run --rm --entrypoint bash \
    -v "$REPO_ROOT/scripts/verify-added-jar-inventory.sh:/tmp/verify-added-jar-inventory.sh:ro" \
    "$1" -c '
      set -eu
      . /tmp/verify-added-jar-inventory.sh
      runtime_jar_inventory /usr/local/tomcat/webapps/teamengine/WEB-INF/lib
    '
}

teamengine_base_inventory() {
  docker run --rm --entrypoint bash "$1" -c '
    set -euo pipefail
    cd /usr/local/tomcat
    {
      find webapps/teamengine te_base/scripts -mindepth 1 \
        \( -path "webapps/teamengine/WEB-INF/lib/ets-ogcapi-connectedsystems10-0.1-SNAPSHOT.jar" \
           -o -path "te_base/scripts/ogcapi-connectedsystems10" \) -prune \
        -o -printf "META|%y|%m|%U|%G|%p|%l\n"
      find webapps/teamengine te_base/scripts -mindepth 1 \
        \( -path "webapps/teamengine/WEB-INF/lib/ets-ogcapi-connectedsystems10-0.1-SNAPSHOT.jar" \
           -o -path "te_base/scripts/ogcapi-connectedsystems10" \) -prune \
        -o -type f -print0 \
        | sort -z \
        | xargs -0 -r sha256sum -- \
        | sed "s/^/CONTENT|/"
    } | LC_ALL=C sort
  '
}

bash scripts/test-teamengine6-jar-guard.sh
runtime_jar_inventory "$IMAGE_REF" > "$inventory_dir/base.inventory"
runtime_jar_inventory "$FINAL_IMAGE_REF" > "$inventory_dir/final.inventory"
if ! jar_guard_output="$(verify_added_jar_inventory \
  "$inventory_dir/base.inventory" \
  "$inventory_dir/final.inventory" \
  scripts/teamengine6-functional-path-allowlist.txt 2>&1)"; then
  fail "added-jar coordinate or functional-path collision verification failed: $jar_guard_output"
fi
echo "$jar_guard_output"

teamengine_base_inventory "$IMAGE_REF" > "$inventory_dir/base.state"
teamengine_base_inventory "$FINAL_IMAGE_REF" > "$inventory_dir/final.state"
diff -u "$inventory_dir/base.state" "$inventory_dir/final.state" >/dev/null \
  || fail "final image changes TeamEngine-owned content, type, mode, ownership, symlink target, or path inventory"

echo "PASS: TeamEngine 6 provenance, coordinate-aware dependency parity, base-file immutability, runtime invariants, and confidential history/context hygiene"
