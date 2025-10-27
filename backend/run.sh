#!/usr/bin/env bash
set -euo pipefail

# Load .env file
if [ -f ./.env ]; then
  export $(cat ./.env | grep -v '^#' | xargs)
fi

mvn spring-boot:run