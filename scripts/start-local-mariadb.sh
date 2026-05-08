#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DATA_DIR="$ROOT_DIR/.local-mariadb/data"
SOCKET="$ROOT_DIR/.local-mariadb/mysqld.sock"
PID_FILE="$ROOT_DIR/.local-mariadb/mysqld.pid"
LOG_FILE="$ROOT_DIR/.local-mariadb/mariadb.err"

if [[ ! -d "$DATA_DIR/mysql" ]]; then
  mariadb-install-db \
    --datadir="$DATA_DIR" \
    --auth-root-authentication-method=normal \
    --user="$(id -un)"
fi

nohup mariadbd \
  --datadir="$DATA_DIR" \
  --socket="$SOCKET" \
  --pid-file="$PID_FILE" \
  --port=3307 \
  --bind-address=127.0.0.1 \
  --skip-networking=0 \
  --log-error="$LOG_FILE" \
  >"$ROOT_DIR/.local-mariadb/mariadb.out" 2>&1 &

echo "MariaDB starting on 127.0.0.1:3307"
echo "PID file: $PID_FILE"
