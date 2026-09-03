FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PORT=8000 \
    PYTHONPATH="/app:/app/backend"

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements and install dependencies (supports both root and backend contexts)
COPY requirements.txt* backend/requirements.tx[t] /tmp/
RUN if [ -f "/tmp/backend/requirements.txt" ]; then \
        pip install --no-cache-dir -r /tmp/backend/requirements.txt; \
    elif [ -f "/tmp/requirements.txt" ]; then \
        pip install --no-cache-dir -r /tmp/requirements.txt; \
    fi && rm -rf /tmp/*

# Copy source directly into /app
COPY . /app/

# Ensure backend folder structure is synchronized to /app root
RUN if [ -d "/app/backend/app" ]; then \
        cp -r /app/backend/* /app/ ; \
    fi

# Ensure entrypoint has Unix line endings and executable permission
RUN if [ ! -f "/app/entrypoint.sh" ] && [ -f "/app/backend/entrypoint.sh" ]; then \
        cp /app/backend/entrypoint.sh /app/entrypoint.sh ; \
    fi && \
    sed -i 's/\r$//' /app/entrypoint.sh && chmod +x /app/entrypoint.sh

EXPOSE 8000

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8000}/health || exit 1

ENTRYPOINT ["/app/entrypoint.sh"]

