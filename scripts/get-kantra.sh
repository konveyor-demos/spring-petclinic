#!/usr/bin/env bash

set -eu

: "${PODMAN_BIN:=podman}"
: "${KANTRA_VERSION:=latest}"
: "${KANTRA:=../kantra-latest}"

image="quay.io/konveyor/kantra:${KANTRA_VERSION}"
${PODMAN_BIN} pull "${image}"

source_exe="kantra"
test "$(uname -o)" = "Darwin" && source_exe=darwin-kantra

${PODMAN_BIN} cp "$(${PODMAN_BIN} create --name kantra-download ${image}):/usr/local/bin/${source_exe}" "${KANTRA}" && ${PODMAN_BIN} rm kantra-download

chmod 755 "${KANTRA}"
