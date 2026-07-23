#!/usr/bin/env bash
# REQ-ETS-TEAMENGINE-007;
# SCENARIO-ETS-TEAMENGINE-TE6-DEPENDENCY-INVENTORY-001.

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "$ROOT_DIR/scripts/verify-added-jar-inventory.sh"

temp_dir="$(mktemp -d)"
trap 'rm -rf "$temp_dir"' EXIT
base_dir="$temp_dir/base"
final_dir="$temp_dir/final"
mkdir -p "$base_dir" "$final_dir"

make_jar() {
  local output="$1"
  local group_id="$2"
  local artifact_id="$3"
  local entry_path="$4"
  local entry_content="$5"
  local build_dir="$temp_dir/build-$artifact_id-$RANDOM"

  mkdir -p "$build_dir/META-INF/maven/$group_id/$artifact_id" "$build_dir/$(dirname "$entry_path")"
  printf 'groupId=%s\nartifactId=%s\nversion=1.0.0\n' "$group_id" "$artifact_id" \
    > "$build_dir/META-INF/maven/$group_id/$artifact_id/pom.properties"
  printf '%s\n' "$entry_content" > "$build_dir/$entry_path"
  (cd "$build_dir" && jar --create --file "$output" .)
  rm -rf "$build_dir"
}

inventory_pair() {
  runtime_jar_inventory "$base_dir" > "$temp_dir/base.inventory"
  runtime_jar_inventory "$final_dir" > "$temp_dir/final.inventory"
}

make_jar "$base_dir/base-engine.jar" org.opengis.cite.teamengine teamengine-resources shared/config.properties base
cp "$base_dir/base-engine.jar" "$final_dir/base-engine.jar"

make_jar "$final_dir/renamed.jar" org.opengis.cite.teamengine teamengine-resources unique/path.properties duplicate
inventory_pair
if verify_added_jar_inventory "$temp_dir/base.inventory" "$temp_dir/final.inventory" /dev/null >/dev/null 2>&1; then
  echo "FAIL: renamed TeamEngine coordinate was accepted" >&2
  exit 1
fi

rm "$final_dir/renamed.jar"
make_jar "$final_dir/application.jar" example application shared/config.properties collision
inventory_pair
if verify_added_jar_inventory "$temp_dir/base.inventory" "$temp_dir/final.inventory" /dev/null >/dev/null 2>&1; then
  echo "FAIL: embedded functional-path collision was accepted" >&2
  exit 1
fi

rm "$final_dir/application.jar"
make_jar "$base_dir/base-convention.jar" example base-convention test-run-props.xml base
cp "$base_dir/base-convention.jar" "$final_dir/base-convention.jar"
make_jar "$base_dir/base-service.jar" example base-service META-INF/services/example.Controller base
cp "$base_dir/base-service.jar" "$final_dir/base-service.jar"
make_jar "$final_dir/application.jar" example application test-run-props.xml application
make_jar "$final_dir/application-service.jar" example application-service META-INF/services/example.Controller application
printf '%s\n' \
  'example:application-service|META-INF/services/example.Controller|Intentional per-application service.' \
  'example:application|test-run-props.xml|Intentional per-application descriptor.' \
  > "$temp_dir/allowlist"
inventory_pair
actual_output="$(verify_added_jar_inventory "$temp_dir/base.inventory" "$temp_dir/final.inventory" "$temp_dir/allowlist")"
expected_output="$(printf '%s\n' \
  'ALLOWED_COLLISION|example:application-service|META-INF/services/example.Controller' \
  'ALLOWED_COLLISION|example:application|test-run-props.xml' \
  'PASS: added-jar coordinate and functional-path guard (2 added jars, 2 reviewed collisions)')"
if [ "$actual_output" != "$expected_output" ]; then
  echo "FAIL: accepted collision tuple output is incomplete or out of order" >&2
  diff -u <(printf '%s\n' "$expected_output") <(printf '%s\n' "$actual_output") >&2 || true
  exit 1
fi

printf '%s\n' 'example:application|unused.properties|Must be rejected because it is not observed.' >> "$temp_dir/allowlist"
if verify_added_jar_inventory "$temp_dir/base.inventory" "$temp_dir/final.inventory" "$temp_dir/allowlist" >/dev/null 2>&1; then
  echo "FAIL: unused allowlist entry was accepted" >&2
  exit 1
fi

echo "PASS: renamed-coordinate rejection, functional-path rejection, exact multi-tuple output, and unused-entry rejection"
