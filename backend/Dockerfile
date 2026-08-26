# Multi-stage production Dockerfile for Spendora Backend (FastAPI)
FROM python:3.12-slim

# Set environment flags
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PORT=8000

WORKDIR /app

# Install system dependencies (curl for health check)
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements from either root or backend/ context
COPY *requirements.txt backend/requirements.tx[t] ./
RUN if [ -f backend/requirements.txt ]; then cp backend/requirements.txt ./requirements.txt; fi && \
    pip install --no-cache-dir -r requirements.txt

# Copy application source and alembic configuration
COPY . .
RUN if [ -d "backend" ]; then cp -r backend/* . ; fi

# Ensure entrypoint script has Unix line endings and is executable
RUN sed -i 's/\r$//' entrypoint.sh && chmod +x entrypoint.sh

# Expose target port
EXPOSE 8000

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8000}/health || exit 1

# Execute startup entrypoint
ENTRYPOINT ["/app/entrypoint.sh"]
