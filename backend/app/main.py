"""
Spendora API — FastAPI application entrypoint.

Lifespan hook (runs on startup):
  - Checks if the `categories` table is empty.
  - If empty, seeds 9 starter categories (idempotent — safe to run repeatedly).
  - Does NOT create tables — schema is managed exclusively by Alembic migrations.

On shutdown:
  - Disposes the async engine (closes connection pool).
"""
from contextlib import asynccontextmanager

from fastapi import FastAPI
from sqlalchemy import select

from app.core.database import AsyncSessionFactory, engine
from app.models.category import Category

# ── Starter categories (SRS Section 5.1) ─────────────────────────────────────
STARTER_CATEGORIES: list[str] = [
    "Food",
    "Transport",
    "Rent",
    "Shopping",
    "Education",
    "Entertainment",
    "Bills",
    "Healthcare",
    "Other",
]


# ── Lifespan ──────────────────────────────────────────────────────────────────
@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Startup: seed starter categories if the table is empty.
    Shutdown: dispose the async engine / connection pool.
    """
    # ── Startup ───────────────────────────────────────────────────────────────
    async with AsyncSessionFactory() as session:
        result = await session.execute(select(Category).limit(1))
        if result.scalars().first() is None:
            for name in STARTER_CATEGORIES:
                session.add(Category(name=name))
            await session.commit()

    yield  # application runs here

    # ── Shutdown ──────────────────────────────────────────────────────────────
    await engine.dispose()


# ── Application ───────────────────────────────────────────────────────────────
app = FastAPI(
    title="Spendora API",
    description="Personal expense and budget tracking — V1",
    version="1.0.0",
    lifespan=lifespan,
    docs_url="/docs",
    redoc_url="/redoc",
)


# ── Health check (liveness — no DB dependency) ────────────────────────────────
@app.get("/health", tags=["Health"])
async def health() -> dict[str, str]:
    """
    Liveness probe.
    Returns 200 OK immediately without touching the database.
    Use a separate readiness probe that queries the DB if needed.
    """
    return {"status": "ok"}
