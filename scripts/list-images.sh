#!/usr/bin/env bash

set -euo pipefail

: "${PETCLINIC_BASE:=http://localhost:8080}"

exec curl -s "${PETCLINIC_BASE}/images"
