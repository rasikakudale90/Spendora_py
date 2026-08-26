import pytest
from httpx import AsyncClient, ASGITransport
from sqlalchemy import select

from app.main import app, lifespan
from app.core.database import AsyncSessionFactory
from app.models.category import Category


@pytest.mark.asyncio
async def test_health_check():
    async with AsyncClient(
        transport=ASGITransport(app=app), base_url="http://test"
    ) as ac:
        response = await ac.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "ok"}


@pytest.mark.asyncio
async def test_lifespan_seed_and_categories_api():
    # Run through app lifespan context to seed categories
    async with lifespan(app):
        async with AsyncClient(
            transport=ASGITransport(app=app), base_url="http://test"
        ) as ac:
            response = await ac.get("/api/v1/categories")
            assert response.status_code == 200
            data = response.json()
            assert len(data) >= 9
            names = [c["name"] for c in data]
            assert "Food" in names
            assert "Transport" in names
            assert "Rent" in names
            assert "Shopping" in names
            assert "Education" in names
            assert "Entertainment" in names
            assert "Bills" in names
            assert "Healthcare" in names
            assert "Other" in names
