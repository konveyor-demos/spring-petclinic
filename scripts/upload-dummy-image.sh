#!/usr/bin/env bash

set -euo pipefail

dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

: "${PETCLINIC_BASE:=http://localhost:8080}"
: "${IMAGE_FILE:=${dir}/../src/main/resources/static/resources/images/pets.png}"

test -r "${IMAGE_FILE}" || {
  echo "File not found: ${IMAGE_FILE}" >&2
  exit 1
}

datetime="$(date +%s)"

exec curl \
  -H "Content-Type: multipart/form-data" \
  -F "image=@${IMAGE_FILE}; filename=dummy-${datetime}.png" \
  "${PETCLINIC_BASE}/images"
