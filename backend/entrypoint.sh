#!/usr/bin/env bash
set -e

echo "=== Spendora Backend Starting ==="

# Run database migrations
echo "Applying database migrations (Alembic)..."
alembic upgrade head || echo "Migration warning: could not run alembic upgrade head. Proceeding with startup."

# Start FastAPI application with Uvicorn
PORT_TO_USE="${PORT:-8000}"
echo "Starting Uvicorn server on port ${PORT_TO_USE}..."
exec uvicorn app.main:app --host 0.0.0.0 --port "${PORT_TO_USE}"
