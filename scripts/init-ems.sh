#!/bin/bash
# EMS Initialization Script (Bash)
# Launches backend containers, waits for health, seeds DB, then launches all containers

set -e
BACKEND_PROFILE="backend"
ALL_PROFILE="all"
SEED_IMAGE="ghcr.io/kgonidisorg/ems:generator"
SEED_ENV=(
  -e APPLICATION_TYPE=db_seeder
  -e PGHOST=postgres
  -e PGPORT=5432
  -e PGUSER=ems_user
  -e PGPASSWORD=ems_password
)
SEED_NETWORK="ems_default"
HEALTH_TIMEOUT=600  # 10 minutes
CHECK_INTERVAL=5

function launch_profile() {
  echo "Launching containers for profile: $1"
  docker compose --profile "$1" up -d
}

function all_backend_healthy() {
  unhealthy=$(docker compose ps --format json | jq -r '.[] | select(.Profiles != null and (.Profiles | test("backend"))) | select(.Health != "healthy" and .State != "running") | .Name')
  if [[ -n "$unhealthy" ]]; then
    echo "Waiting for healthy containers: $unhealthy"
    return 1
  fi
  return 0
}

function wait_for_backend_health() {
  echo "Waiting for all backend containers to be healthy (timeout: $HEALTH_TIMEOUT seconds)..."
  local start=$(date +%s)
  while (( $(date +%s) - start < HEALTH_TIMEOUT )); do
    if all_backend_healthy; then
      echo "All backend containers are healthy!"
      return 0
    fi
    sleep $CHECK_INTERVAL
  done
  echo "Timeout waiting for backend containers to be healthy."
  return 1
}

function run_db_seeder() {
  echo "Running DB seeder container..."
  docker run --rm --network "$SEED_NETWORK" "${SEED_ENV[@]}" "$SEED_IMAGE"
}

# Main workflow

echo "\n🚀 Initializing EMS stack..."
launch_profile "$BACKEND_PROFILE"
if ! wait_for_backend_health; then
  exit 1
fi

echo "\nSeeding database..."
run_db_seeder

echo "\nBringing up all containers in 'all' profile..."
launch_profile "$ALL_PROFILE"

echo "\n🎉 EMS initialization complete!"
