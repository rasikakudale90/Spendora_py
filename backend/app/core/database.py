"""
Async SQLAlchemy engine and session factory.

- Engine created once at import time from DATABASE_URL.
- get_db() is a FastAPI Depends-compatible async generator that
  yields one AsyncSession per HTTP request and handles rollback/close.
"""
from collections.abc import AsyncGenerator

from sqlalchemy.ext.asyncio import (
    AsyncSession,
    async_sessionmaker,
    create_async_engine,
)

from app.core.config import settings

# ── Engine ────────────────────────────────────────────────────────────────────
# echo=False in production; flip to True locally for SQL debug logs.
engine = create_async_engine(
    settings.DATABASE_URL,
    echo=False,
    pool_pre_ping=True,  # reconnect on stale connections
    connect_args={"statement_cache_size": 0},  # compatible with Supabase connection poolers
)

# ── Session factory ───────────────────────────────────────────────────────────
AsyncSessionFactory = async_sessionmaker(
    bind=engine,
    class_=AsyncSession,
    expire_on_commit=False,  # keep ORM objects usable after commit
)


# ── Dependency ────────────────────────────────────────────────────────────────
async def get_db() -> AsyncGenerator[AsyncSession, None]:
    """
    FastAPI dependency — yields an AsyncSession per request.
    Rolls back on unhandled exceptions; always closes the session.

    Usage:
        async def my_route(db: AsyncSession = Depends(get_db)):
            ...
    """
    async with AsyncSessionFactory() as session:
        try:
            yield session
        except Exception:
            await session.rollback()
            raise
        finally:
            await session.close()
