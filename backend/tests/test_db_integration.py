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
    assert response.json()["status"] == "ok"


@pytest.mark.asyncio
async def test_lifespan_seed_and_categories_api(client: AsyncClient):
    response = await client.get("/api/v1/categories")
    assert response.status_code == 200
    data = response.json()
    assert len(data) >= 5
    for cat in data:
        assert "id" in cat
        assert "name" in cat
        assert "expense_count" in cat
    names = [c["name"] for c in data]
    assert any(name in names for name in ["Food", "Grocery", "Bills", "Shopping", "Transport"])
