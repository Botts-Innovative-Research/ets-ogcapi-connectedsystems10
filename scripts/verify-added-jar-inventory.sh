#!/usr/bin/env bash
# REQ-ETS-TEAMENGINE-007;
# SCENARIO-ETS-TEAMENGINE-TE6-DEPENDENCY-INVENTORY-001.

set -euo pipefail

jar_guard_fail() {
  printf 'FAIL: %s\n' "$*" >&2
  exit 1
}

runtime_jar_inventory() {
  local lib_dir="$1"
  local jar name coordinate_path properties group_id artifact_id entry

  for jar in "$lib_dir"/*.jar; do
    [ -f "$jar" ] || continue
    name="${jar##*/}"
    printf 'JAR|%s|%s\n' "$name" "$(sha256sum "$jar" | awk '{print $1}')"

    while IFS= read -r coordinate_path; do
      [ -n "$coordinate_path" ] || continue
      properties="$(unzip -p "$jar" "$coordinate_path")"
      group_id="$(printf '%s\n' "$properties" | sed -n 's/^groupId=//p' | head -n 1)"
      artifact_id="$(printf '%s\n' "$properties" | sed -n 's/^artifactId=//p' | head -n 1)"
      [ -n "$group_id" ] || jar_guard_fail "$name has Maven metadata without groupId: $coordinate_path"
      [ -n "$artifact_id" ] || jar_guard_fail "$name has Maven metadata without artifactId: $coordinate_path"
      printf 'COORD|%s|%s:%s\n' "$name" "$group_id" "$artifact_id"
    done < <(unzip -Z1 "$jar" | awk '/^META-INF\/maven\/[^/]+\/[^/]+\/pom.properties$/')

    while IFS= read -r entry; do
      case "$entry" in
        */|META-INF/MANIFEST.MF|META-INF/maven/*|META-INF/*.SF|META-INF/*.RSA|META-INF/*.DSA|META-INF/LICENSE*|META-INF/NOTICE*|META-INF/DEPENDENCIES)
          continue
          ;;
      esac
      printf 'ENTRY|%s|%s\n' "$name" "$entry"
    done < <(unzip -Z1 "$jar")
  done | LC_ALL=C sort -u
}

verify_added_jar_inventory() (
  local base_inventory="$1"
  local final_inventory="$2"
  local allowlist="$3"
  local work_dir added_jar coordinate path collision_count

  work_dir="$(mktemp -d)"
  trap 'rm -rf "$work_dir"' EXIT

  awk -F'|' '$1 == "JAR" { print $2 "|" $3 }' "$base_inventory" | LC_ALL=C sort -u > "$work_dir/base-jars"
  awk -F'|' '$1 == "JAR" { print $2 "|" $3 }' "$final_inventory" | LC_ALL=C sort -u > "$work_dir/final-jars"
  if ! comm -23 "$work_dir/base-jars" "$work_dir/final-jars" > "$work_dir/changed-base-jars"; then
    jar_guard_fail "unable to compare immutable base jar inventory"
  fi
  if [ -s "$work_dir/changed-base-jars" ]; then
    jar_guard_fail "final image removes or changes a base jar: $(head -n 1 "$work_dir/changed-base-jars")"
  fi

  cut -d'|' -f1 "$work_dir/base-jars" > "$work_dir/base-jar-names"
  cut -d'|' -f1 "$work_dir/final-jars" > "$work_dir/final-jar-names"
  comm -13 "$work_dir/base-jar-names" "$work_dir/final-jar-names" > "$work_dir/added-jars"
  awk -F'|' '$1 == "COORD" { print $3 }' "$base_inventory" | LC_ALL=C sort -u > "$work_dir/base-coordinates"
  awk -F'|' '$1 == "ENTRY" { print $3 }' "$base_inventory" | LC_ALL=C sort -u > "$work_dir/base-paths"

  : > "$work_dir/allowed-pairs"
  while IFS='|' read -r coordinate path rationale; do
    case "$coordinate" in
      ''|'#'*) continue ;;
    esac
    [ -n "$path" ] || jar_guard_fail "allowlist entry has no functional path: $coordinate"
    [ -n "$rationale" ] || jar_guard_fail "allowlist entry has no rationale: $coordinate|$path"
    case "$coordinate$path" in
      *'*'*) jar_guard_fail "allowlist entries must be exact, not wildcarded: $coordinate|$path" ;;
    esac
    printf '%s|%s\n' "$coordinate" "$path" >> "$work_dir/allowed-pairs"
  done < "$allowlist"
  LC_ALL=C sort -u -o "$work_dir/allowed-pairs" "$work_dir/allowed-pairs"
  : > "$work_dir/observed-allowed-pairs"

  collision_count=0
  while IFS= read -r added_jar; do
    [ -n "$added_jar" ] || continue
    awk -F'|' -v jar="$added_jar" '$1 == "COORD" && $2 == jar { print $3 }' "$final_inventory" \
      | LC_ALL=C sort -u > "$work_dir/added-coordinates"
    [ -s "$work_dir/added-coordinates" ] || jar_guard_fail "added jar has no Maven coordinate metadata: $added_jar"

    while IFS= read -r coordinate; do
      case "$coordinate" in
        org.opengis.cite.teamengine:*) jar_guard_fail "added jar carries a TeamEngine coordinate independent of filename: $added_jar ($coordinate)" ;;
      esac
      if grep -Fqx "$coordinate" "$work_dir/base-coordinates"; then
        jar_guard_fail "added jar duplicates a base Maven coordinate family: $added_jar ($coordinate)"
      fi
    done < "$work_dir/added-coordinates"

    awk -F'|' -v jar="$added_jar" '$1 == "ENTRY" && $2 == jar { print $3 }' "$final_inventory" \
      | LC_ALL=C sort -u > "$work_dir/added-paths"
    comm -12 "$work_dir/base-paths" "$work_dir/added-paths" > "$work_dir/collisions"
    while IFS= read -r path; do
      [ -n "$path" ] || continue
      collision_count=$((collision_count + 1))
      allowed=false
      while IFS= read -r coordinate; do
        if grep -Fqx "$coordinate|$path" "$work_dir/allowed-pairs"; then
          allowed=true
          printf '%s|%s\n' "$coordinate" "$path" >> "$work_dir/observed-allowed-pairs"
          break
        fi
      done < "$work_dir/added-coordinates"
      [ "$allowed" = true ] || jar_guard_fail "functional-path collision: $added_jar supplies base path $path"
    done < "$work_dir/collisions"
  done < "$work_dir/added-jars"

  LC_ALL=C sort -u -o "$work_dir/observed-allowed-pairs" "$work_dir/observed-allowed-pairs"
  comm -23 "$work_dir/allowed-pairs" "$work_dir/observed-allowed-pairs" > "$work_dir/unused-allowed-pairs"
  if [ -s "$work_dir/unused-allowed-pairs" ]; then
    jar_guard_fail "unused allowlist entry: $(head -n 1 "$work_dir/unused-allowed-pairs")"
  fi
  while IFS= read -r allowed_pair; do
    [ -n "$allowed_pair" ] || continue
    printf 'ALLOWED_COLLISION|%s\n' "$allowed_pair"
  done < "$work_dir/observed-allowed-pairs"

  printf 'PASS: added-jar coordinate and functional-path guard (%s added jars, %s reviewed collisions)\n' \
    "$(wc -l < "$work_dir/added-jars" | tr -d ' ')" "$collision_count"
)

if [ "${BASH_SOURCE[0]}" = "$0" ]; then
  [ "$#" -eq 3 ] || jar_guard_fail "usage: $0 BASE_LIB_DIR FINAL_LIB_DIR ALLOWLIST"
  temp_dir="$(mktemp -d)"
  trap 'rm -rf "$temp_dir"' EXIT
  runtime_jar_inventory "$1" > "$temp_dir/base.inventory"
  runtime_jar_inventory "$2" > "$temp_dir/final.inventory"
  verify_added_jar_inventory "$temp_dir/base.inventory" "$temp_dir/final.inventory" "$3"
fi
