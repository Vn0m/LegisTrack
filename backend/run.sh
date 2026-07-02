#!/usr/bin/env bash
set -euo pipefail

# Load .env (set -a auto-exports; handles quoted values with & ? etc.)
if [ -f ../.env ]; then
  set -a
  source ../.env
  set +a
fi

mvn spring-boot:run
