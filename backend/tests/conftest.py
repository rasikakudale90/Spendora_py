import pytest_asyncio
from httpx import ASGITransport, AsyncClient

from app.main import app, lifespan


@pytest_asyncio.fixture(scope="function")
async def client():
    """AsyncClient fixture that runs within the FastAPI lifespan context."""
    async with lifespan(app):
        async with AsyncClient(
            transport=ASGITransport(app=app), base_url="http://test"
        ) as ac:
            yield ac
