import uuid
from datetime import date
from decimal import Decimal
import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_budget_upsert_and_retrieval(client: AsyncClient):
    cat_resp = await client.get("/api/v1/categories")
    categories = cat_resp.json()
    cat_id = categories[0]["id"]
    test_month = "2026-08-01"

    # 1. Set Overall Budget
    res_overall = await client.post(
        "/api/v1/budgets",
        json={
            "scope": "overall",
            "amount": "20000.00",
            "period_month": test_month,
        },
    )
    assert res_overall.status_code == 200
    overall_data = res_overall.json()
    assert overall_data["scope"] == "overall"
    assert overall_data["amount"] == "20000.00"
    assert overall_data["category_id"] is None

    # 2. Update Overall Budget (upsert)
    res_overall_update = await client.post(
        "/api/v1/budgets",
        json={
            "scope": "overall",
            "amount": "22000.00",
            "period_month": test_month,
        },
    )
    assert res_overall_update.status_code == 200
    assert res_overall_update.json()["amount"] == "22000.00"

    # 3. Set Category Budget
    res_cat = await client.post(
        "/api/v1/budgets",
        json={
            "scope": "category",
            "category_id": cat_id,
            "amount": "5000.00",
            "period_month": test_month,
        },
    )
    assert res_cat.status_code == 200
    cat_budget = res_cat.json()
    assert cat_budget["scope"] == "category"
    assert cat_budget["category_id"] == cat_id
    assert cat_budget["amount"] == "5000.00"
    assert cat_budget["category_name"] is not None

    # 4. Try setting category budget with non-existent category -> 400
    res_bad_cat = await client.post(
        "/api/v1/budgets",
        json={
            "scope": "category",
            "category_id": str(uuid.uuid4()),
            "amount": "1000.00",
            "period_month": test_month,
        },
    )
    assert res_bad_cat.status_code == 400

    # 5. Retrieve budgets for period
    get_res = await client.get("/api/v1/budgets", params={"period_month": test_month})
    assert get_res.status_code == 200
    body = get_res.json()
    assert body["overall_budget"] is not None
    assert body["overall_budget"]["amount"] == "22000.00"
    assert isinstance(body["category_budgets"], list)
    assert any(cb["category_id"] == cat_id for cb in body["category_budgets"])
