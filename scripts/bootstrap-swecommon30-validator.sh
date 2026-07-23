#!/usr/bin/env bash
# REQ-ETS-VALIDATOR-001; SCENARIO-ETS-VALIDATOR-SOURCE-PIN-001.
set -euo pipefail

readonly REPOSITORY_URL="https://github.com/opengeospatial/ets-swecommon30.git"
readonly EXPECTED_COMMIT="3ba75ceabe57cea85f4a8513c59e0f90e386ba96"

work_dir="$(mktemp -d "${TMPDIR:-/tmp}/swecommon30-validator.XXXXXXXX")"
cleanup() {
  rm -rf -- "${work_dir}"
}
trap cleanup EXIT

git init --quiet "${work_dir}"
git -C "${work_dir}" remote add origin "${REPOSITORY_URL}"
git -C "${work_dir}" fetch --quiet --depth 1 origin "${EXPECTED_COMMIT}"

actual_commit="$(git -C "${work_dir}" rev-parse FETCH_HEAD)"
if [[ "${actual_commit}" != "${EXPECTED_COMMIT}" ]]; then
  printf 'SWE Common validator source mismatch: expected %s, received %s\n' \
    "${EXPECTED_COMMIT}" "${actual_commit}" >&2
	exit 1
fi
printf 'Verified SWE Common validator source: %s@%s\n' "${REPOSITORY_URL}" "${actual_commit}"

git -C "${work_dir}" checkout --quiet --detach FETCH_HEAD

mvn -B -ntp -f "${work_dir}/pom.xml" \
  -pl swecommon30-validator -am \
  install \
  -DskipTests \
  -Dmaven.site.skip=true \
  -Dmaven.javadoc.skip=true
