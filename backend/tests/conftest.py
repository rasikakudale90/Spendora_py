import os
import uuid
from pathlib import Path
from dotenv import load_dotenv

# Load backend/.env if not already loaded in environment
env_path = Path(__file__).resolve().parent.parent / ".env"
if env_path.exists():
    load_dotenv(dotenv_path=env_path)

import pytest_asyncio
from httpx import ASGITransport, AsyncClient

from app.core.limiter import limiter
from app.main import app, lifespan

# Disable rate limiting in tests so test suites run unrestricted
limiter.enabled = False


@pytest_asyncio.fixture(scope="session", autouse=True)
async def init_test_db():
    """Ensure all database tables and starter categories exist for test execution."""
    from sqlalchemy import select
    from app.core.database import AsyncSessionFactory, engine
    from app.main import STARTER_CATEGORIES
    from app.models import Base
    from app.models.category import Category

    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

    async with AsyncSessionFactory() as session:
        result = await session.execute(select(Category).where(Category.user_id.is_(None)).limit(1))
        if result.scalars().first() is None:
            for name in STARTER_CATEGORIES:
                session.add(Category(name=name, user_id=None))
            await session.commit()

    yield


@pytest_asyncio.fixture(scope="function")
async def client():
    """
    AsyncClient fixture that runs within the FastAPI lifespan context,
    automatically pre-authenticated with a freshly registered test user so that
    existing expense, budget, income, category, and dashboard test suites pass smoothly.
    """
    async with lifespan(app):
        async with AsyncClient(
            transport=ASGITransport(app=app), base_url="http://test"
        ) as ac:
            test_email = f"runner_{uuid.uuid4().hex[:8]}@example.com"
            test_password = "Password123!"
            reg_resp = await ac.post(
                "/api/v1/auth/register",
                json={
                    "email": test_email,
                    "password": test_password,
                    "full_name": "Test Runner",
                },
            )
            if reg_resp.status_code == 201:
                login_resp = await ac.post(
                    "/api/v1/auth/login",
                    json={"email": test_email, "password": test_password},
                )
                if login_resp.status_code == 200:
                    token = login_resp.json()["access_token"]
                    ac.headers["Authorization"] = f"Bearer {token}"
            yield ac


@pytest_asyncio.fixture(scope="function")
async def unauth_client():
    """Unauthenticated AsyncClient for testing unauthenticated access rejections."""
    async with lifespan(app):
        async with AsyncClient(
            transport=ASGITransport(app=app), base_url="http://test"
        ) as ac:
            yield ac


@pytest_asyncio.fixture(scope="function")
def client_factory():
    """Factory to create isolated authenticated clients for multiple distinct users."""
    async def _factory(email: str = None, password: str = "Password123!") -> tuple[AsyncClient, dict]:
        if not email:
            email = f"user_{uuid.uuid4().hex[:8]}@example.com"
        ac = AsyncClient(transport=ASGITransport(app=app), base_url="http://test")
        reg_resp = await ac.post(
            "/api/v1/auth/register",
            json={"email": email, "password": password, "full_name": "Isolated User"},
        )
        assert reg_resp.status_code == 201, f"Failed to register factory user: {reg_resp.text}"
        user_data = reg_resp.json()["user"]

        login_resp = await ac.post(
            "/api/v1/auth/login",
            json={"email": email, "password": password},
        )
        assert login_resp.status_code == 200, f"Failed to login factory user: {login_resp.text}"
        token = login_resp.json()["access_token"]
        ac.headers["Authorization"] = f"Bearer {token}"
        return ac, user_data
    return _factory
