"""
Spendora API — FastAPI application entrypoint.

- Lifespan hook: Idempotently seeds starter categories on startup.
- CORS Middleware: Allows frontend communication.
- Versioned API: All resources mounted under /api/v1.
- Health Check: Liveness probe at /health.
"""
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy import select

from app.core.config import settings
from app.core.database import AsyncSessionFactory, engine
from app.models.category import Category
from app.routers import api_router

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
    try:
        async with AsyncSessionFactory() as session:
            result = await session.execute(select(Category).limit(1))
            if result.scalars().first() is None:
                for name in STARTER_CATEGORIES:
                    session.add(Category(name=name))
                await session.commit()
    except Exception as e:
        # If database is not ready or tables not yet created, log and continue
        print(f"Lifespan seed notice: {e}")

    yield  # application runs here

    # ── Shutdown ──────────────────────────────────────────────────────────────
    await engine.dispose()


from slowapi import _rate_limit_exceeded_handler
from slowapi.errors import RateLimitExceeded

from app.core.limiter import limiter

# ── Application ───────────────────────────────────────────────────────────────
app = FastAPI(
    title="Spendora API",
    description="Personal expense and budget tracking — V1",
    version="1.0.0",
    lifespan=lifespan,
    docs_url="/docs",
    redoc_url="/redoc",
)
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

# ── CORS Middleware ───────────────────────────────────────────────────────────
# Parse configured origins
configured_origins = []
if settings.CORS_ORIGINS and settings.CORS_ORIGINS != "*":
    configured_origins = [orig.strip() for orig in settings.CORS_ORIGINS.split(",") if orig.strip()]

app.add_middleware(
    CORSMiddleware,
    allow_origins=configured_origins if configured_origins else ["*"],
    allow_origin_regex=r"https://.*\.vercel\.app|https://.*\.onrender\.com|http://localhost:\d+",
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── Include Routers ───────────────────────────────────────────────────────────
app.include_router(api_router)


# ── Health check (liveness — no DB dependency) ────────────────────────────────
@app.get("/health", tags=["Health"])
async def health() -> dict[str, str]:
    """
    Liveness probe.
    Returns 200 OK immediately without touching the database.
    """
    return {"status": "ok"}
