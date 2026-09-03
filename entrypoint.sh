#!/usr/bin/env bash
set -e

echo "=== Spendora Backend Starting ==="

# If backend subfolder exists, sync its contents to /app root
if [ -d "/app/backend/app" ] && [ ! -d "/app/app" ]; then
    echo "Syncing backend folder structure to /app..."
    cp -r /app/backend/* /app/
fi

# Set PYTHONPATH to include /app and /app/backend
export PYTHONPATH="/app:/app/backend:${PYTHONPATH}"

# Run database migrations
echo "Applying database migrations (Alembic)..."
if [ -f "/app/alembic.ini" ]; then
    alembic upgrade head || echo "Migration warning: could not run alembic upgrade head. Proceeding with startup."
elif [ -f "/app/backend/alembic.ini" ]; then
    cd /app/backend && alembic upgrade head && cd /app || echo "Migration warning: could not run alembic upgrade head. Proceeding with startup."
else
    echo "No alembic.ini found, proceeding."
fi

# Start FastAPI application with Uvicorn
PORT_TO_USE="${PORT:-8000}"
echo "Starting Uvicorn server on port ${PORT_TO_USE}..."
exec uvicorn app.main:app --host 0.0.0.0 --port "${PORT_TO_USE}"
