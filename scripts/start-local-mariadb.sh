#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOCAL_DIR="$ROOT_DIR/.local-mariadb"
DATA_DIR="$ROOT_DIR/.local-mariadb/data"
SOCKET="$ROOT_DIR/.local-mariadb/mysqld.sock"
PID_FILE="$ROOT_DIR/.local-mariadb/mysqld.pid"
LOG_FILE="$ROOT_DIR/.local-mariadb/mariadb.err"
DATABASE_PROPERTIES="$ROOT_DIR/src/main/resources/database.properties"

mkdir -p "$LOCAL_DIR"

CONFIG_DB_URL="$(awk -F= '$1 == "db.url" { print $2 }' "$DATABASE_PROPERTIES" 2>/dev/null || true)"
DB_URL="${DB_URL:-$CONFIG_DB_URL}"
DB_URL="${DB_URL%%\?*}"
DB_NAME="${CARELY_DB_NAME:-${DB_URL##*/}}"
DB_NAME="${DB_NAME:-carely}"

if [[ ! "$DB_NAME" =~ ^[A-Za-z0-9_]+$ ]]; then
  echo "Invalid database name: $DB_NAME" >&2
  exit 1
fi

if [[ ! -d "$DATA_DIR/mysql" ]]; then
  mariadb-install-db \
    --datadir="$DATA_DIR" \
    --auth-root-authentication-method=normal \
    --user="$(id -un)"
fi

if ! mysqladmin --protocol=TCP -h 127.0.0.1 -P 3307 -u root ping --silent >/dev/null 2>&1; then
  nohup mariadbd \
    --datadir="$DATA_DIR" \
    --socket="$SOCKET" \
    --pid-file="$PID_FILE" \
    --port=3307 \
    --bind-address=127.0.0.1 \
    --skip-networking=0 \
    --log-error="$LOG_FILE" \
    >"$ROOT_DIR/.local-mariadb/mariadb.out" 2>&1 &
fi

for attempt in {1..30}; do
  if mysqladmin --protocol=TCP -h 127.0.0.1 -P 3307 -u root ping --silent >/dev/null 2>&1; then
    break
  fi

  if [[ "$attempt" -eq 30 ]]; then
    echo "MariaDB did not become ready. Check $LOG_FILE" >&2
    exit 1
  fi

  sleep 1
done

mysql --protocol=TCP -h 127.0.0.1 -P 3307 -u root \
  -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

echo "MariaDB ready on 127.0.0.1:3307"
echo "Database ensured: $DB_NAME"
echo "PID file: $PID_FILE"
