#!/bin/sh

set -eu

if [ -z "${SPRING_PROFILES_ACTIVE:-}" ]; then
  export SPRING_PROFILES_ACTIVE=prod
fi

if [ -n "${POSTGRES_DATASOURCE_URL:-}" ] && [ -z "${SPRING_DATASOURCE_URL:-}" ]; then
  export SPRING_DATASOURCE_URL="$POSTGRES_DATASOURCE_URL"
fi

if [ -n "${DATABASE_USERNAME:-}" ] && [ -z "${SPRING_DATASOURCE_USERNAME:-}" ]; then
  export SPRING_DATASOURCE_USERNAME="$DATABASE_USERNAME"
fi

if [ -n "${DATABASE_PASSWORD:-}" ] && [ -z "${SPRING_DATASOURCE_PASSWORD:-}" ]; then
  export SPRING_DATASOURCE_PASSWORD="$DATABASE_PASSWORD"
fi

exec java -jar app.jar
