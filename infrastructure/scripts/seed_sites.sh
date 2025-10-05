#!/usr/bin/env bash
# Seed the ems_devices database with sites data
# Usage: ./seed_sites.sh

set -euo pipefail

CONTAINER_NAME=ems-postgres
DB_NAME=ems_devices
DB_USER=ems_user
DB_PASSWORD=ems_password
SQL_FILE="/tmp/seed_sites.sql"

# Copy the seed SQL into the container
docker cp "$(pwd)/infrastructure/docker/seed_sites.sql" "${CONTAINER_NAME}:${SQL_FILE}"

# Execute the SQL inside the container
docker exec -e PGPASSWORD="${DB_PASSWORD}" -i "${CONTAINER_NAME}" \
  psql -U "${DB_USER}" -d "${DB_NAME}" -f "${SQL_FILE}"

# Optionally remove the SQL file from container
docker exec "${CONTAINER_NAME}" rm -f "${SQL_FILE}"

echo "Seeded ${DB_NAME} with sites from seed_sites.sql"
