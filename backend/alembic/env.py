"""
Alembic async environment configuration.

Key behaviours:
- Reads DATABASE_URL from app settings (environment variable), never from
  alembic.ini — keeps credentials out of version control.
- Imports all ORM models via `from app.models import Base` so that
  autogenerate can detect schema changes.
- Uses asyncio + async_engine_from_config for SQLAlchemy 2.0 async engines.

Run from the backend/ directory:
    alembic revision --autogenerate -m "describe change"
    alembic upgrade head
    alembic downgrade -1
"""
import asyncio
from logging.config import fileConfig

from sqlalchemy import pool
from sqlalchemy.engine import Connection
from sqlalchemy.ext.asyncio import async_engine_from_config

from alembic import context

# ── Load all ORM models so Base.metadata is fully populated ──────────────────
# This import MUST come before `target_metadata = Base.metadata`
from app.models import Base  # noqa: E402
from app.core.config import settings  # noqa: E402

# Alembic Config object — provides access to values in alembic.ini
config = context.config

# Set up Python logging from alembic.ini [loggers] section
if config.config_file_name is not None:
    fileConfig(config.config_file_name)

# Target metadata for autogenerate comparison
target_metadata = Base.metadata


# ── Offline migrations (no live DB connection) ────────────────────────────────
def run_migrations_offline() -> None:
    """
    Run migrations in 'offline' mode — emits SQL to stdout without
    connecting to the database.  Useful for generating migration scripts
    to review or apply manually.
    """
    context.configure(
        url=settings.DATABASE_URL,
        target_metadata=target_metadata,
        literal_binds=True,
        dialect_opts={"paramstyle": "named"},
    )
    with context.begin_transaction():
        context.run_migrations()


# ── Online migrations (live async DB connection) ──────────────────────────────
def do_run_migrations(connection: Connection) -> None:
    context.configure(connection=connection, target_metadata=target_metadata)
    with context.begin_transaction():
        context.run_migrations()


async def run_async_migrations() -> None:
    """
    Create an async engine from settings, connect, and run migrations.
    NullPool is used so connections are not pooled during migration runs.
    """
    configuration = config.get_section(config.config_ini_section, {})
    configuration["sqlalchemy.url"] = settings.DATABASE_URL

    connectable = async_engine_from_config(
        configuration,
        prefix="sqlalchemy.",
        poolclass=pool.NullPool,
    )

    async with connectable.connect() as connection:
        await connection.run_sync(do_run_migrations)

    await connectable.dispose()


def run_migrations_online() -> None:
    asyncio.run(run_async_migrations())


# ── Entry point ───────────────────────────────────────────────────────────────
if context.is_offline_mode():
    run_migrations_offline()
else:
    run_migrations_online()
