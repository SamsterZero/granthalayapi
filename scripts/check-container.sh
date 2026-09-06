#!/usr/bin/env bash
set -euo pipefail

ENGINE="docker"
IMAGE="granthalay-api:dev"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --engine)
      ENGINE="$2"
      shift 2
      ;;
    --image)
      IMAGE="$2"
      shift 2
      ;;
    *)
      echo "Unknown option: $1"
      exit 1
      ;;
  esac
done

PROJECT="granthalay-check-$RANDOM"
DATABASE="${PROJECT}-db"
APPLICATION="${PROJECT}-app"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

COMPOSE=("$ENGINE" "compose" "-f" "$ROOT_DIR/compose.yaml" "-p" "$PROJECT")

cleanup() {
  echo "Cleaning up containers and volumes..."
  "$ENGINE" rm --force "$APPLICATION" "$DATABASE" 2>/dev/null || true
  "${COMPOSE[@]}" down --volumes --remove-orphans 2>/dev/null || true
}
trap cleanup EXIT

echo "Starting isolated database container..."
"${COMPOSE[@]}" run --detach --name "$DATABASE" postgres

echo "Waiting for PostgreSQL to be ready..."
DEADLINE=$(($(date +%s) + 60))
until "$ENGINE" exec "$DATABASE" pg_isready -U granthalay -d granthalay >/dev/null 2>&1; do
  if [[ $(date +%s) -ge $DEADLINE ]]; then
    echo "Error: PostgreSQL did not become ready within 60 seconds."
    exit 1
  fi
  sleep 1
done

echo "Starting application container..."
"$ENGINE" run --detach --name "$APPLICATION" \
  --network "${PROJECT}_default" -p "127.0.0.1::8080" \
  -e "SPRING_DATASOURCE_URL=jdbc:postgresql://${DATABASE}:5432/granthalay" \
  -e "SPRING_DATASOURCE_USERNAME=granthalay" \
  -e "SPRING_DATASOURCE_PASSWORD=granthalay" "$IMAGE"

PORT=$("$ENGINE" port "$APPLICATION" 8080/tcp | awk -F: '{print $NF}')

echo "Waiting for application readiness on port $PORT..."
DEADLINE=$(($(date +%s) + 60))
while true; do
  RUNNING=$("$ENGINE" inspect --format "{{.State.Running}}" "$APPLICATION" 2>/dev/null || echo "false")
  if [[ "$RUNNING" != "true" ]]; then
    echo "Application container exited unexpectedly. Logs:"
    "$ENGINE" logs "$APPLICATION"
    exit 1
  fi

  STATUS_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:${PORT}/actuator/health/readiness" || echo "000")
  if [[ "$STATUS_CODE" == "200" ]]; then
    break
  fi

  if [[ $(date +%s) -ge $DEADLINE ]]; then
    echo "Error: Application did not become ready within 60 seconds."
    "$ENGINE" logs "$APPLICATION"
    exit 1
  fi
  sleep 1
done

echo "Checking liveness endpoint..."
LIVENESS_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:${PORT}/actuator/health/liveness")
if [[ "$LIVENESS_CODE" != "200" ]]; then
  echo "Error: Liveness check failed with HTTP $LIVENESS_CODE"
  exit 1
fi

echo "Checking sensitive actuator protection..."
ENV_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:${PORT}/actuator/env")
if [[ "$ENV_CODE" != "403" ]]; then
  echo "Error: Sensitive actuator route was not denied (got $ENV_CODE, expected 403)"
  exit 1
fi

echo "Container startup, migrations, and protected probes passed."

echo "Stopping database container to test readiness outage..."
"$ENGINE" stop --time 2 "$DATABASE"

DEADLINE=$(($(date +%s) + 40))
OUTAGE_PASSED=false
while [[ $(date +%s) -lt $DEADLINE ]]; do
  READINESS_OUTAGE_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:${PORT}/actuator/health/readiness" || echo "000")
  if [[ "$READINESS_OUTAGE_CODE" == "503" ]]; then
    OUTAGE_PASSED=true
    break
  fi
  sleep 1
done

if [[ "$OUTAGE_PASSED" != "true" ]]; then
  echo "Error: Readiness did not report database outage (got HTTP $READINESS_OUTAGE_CODE)"
  exit 1
fi

LIVENESS_OUTAGE_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:${PORT}/actuator/health/liveness")
if [[ "$LIVENESS_OUTAGE_CODE" != "200" ]]; then
  echo "Error: Database outage incorrectly degraded liveness (got HTTP $LIVENESS_OUTAGE_CODE)"
  exit 1
fi

echo "Database outage check passed: readiness 503, liveness 200."
