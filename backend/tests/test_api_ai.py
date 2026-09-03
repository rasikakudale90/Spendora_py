from datetime import date
from decimal import Decimal
import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_simulate_purchase_lifecycle(client: AsyncClient):
    # 1. Add monthly income
    inc_resp = await client.post(
        "/api/v1/incomes",
        json={
            "title": "Monthly Salary",
            "amount": "50000.00",
            "income_date": date.today().isoformat(),
            "source": "Salary",
        },
    )
    assert inc_resp.status_code == 201

    # 2. Add an overall budget
    curr_month_str = date.today().replace(day=1).isoformat()
    b_resp = await client.post(
        "/api/v1/budgets",
        json={
            "scope": "overall",
            "amount": "30000.00",
            "period_month": curr_month_str,
        },
    )
    assert b_resp.status_code == 200

    # 3. Simulate a safe small purchase (e.g. Book ₹500)
    sim_safe = await client.post(
        "/api/v1/ai/simulate-purchase",
        json={"title": "Tech Book", "amount": "500.00"},
    )
    assert sim_safe.status_code == 200
    safe_data = sim_safe.json()
    assert safe_data["verdict"] in ["safe", "caution"]
    assert safe_data["item_title"] == "Tech Book"
    assert safe_data["item_amount"] == "500.00"
    assert "ai_analysis" in safe_data
    assert len(safe_data["actionable_tips"]) >= 1
    assert "daily_safe_spend_after" in safe_data

    # 4. Simulate an over-budget purchase (e.g. ₹60,000 Luxury Trip)
    sim_over = await client.post(
        "/api/v1/ai/simulate-purchase",
        json={"title": "Luxury Vacation", "amount": "60000.00"},
    )
    assert sim_over.status_code == 200
    over_data = sim_over.json()
    assert over_data["verdict"] == "over_budget"
    assert "exceed" in over_data["verdict_summary"].lower() or "deficit" in over_data["ai_analysis"].lower()

    # 5. Validation error tests
    sim_invalid = await client.post(
        "/api/v1/ai/simulate-purchase",
        json={"title": "", "amount": "0.00"},
    )
    assert sim_invalid.status_code == 422


@pytest.mark.asyncio
async def test_leak_analysis_endpoint(client: AsyncClient):
    # Add recurring subscription and micro expenses
    cat_resp = await client.get("/api/v1/categories")
    cat_id = cat_resp.json()[0]["id"]

    await client.post(
        "/api/v1/expenses",
        json={
            "title": "Netflix Premium",
            "amount": "649.00",
            "expense_date": date.today().isoformat(),
            "category_id": cat_id,
        },
    )
    await client.post(
        "/api/v1/expenses",
        json={
            "title": "Chai & Samosa",
            "amount": "60.00",
            "expense_date": date.today().isoformat(),
            "category_id": cat_id,
        },
    )

    leak_resp = await client.get("/api/v1/ai/leak-analysis")
    assert leak_resp.status_code == 200
    data = leak_resp.json()
    assert "total_monthly_leak" in data
    assert "total_annual_projected_leak" in data
    assert "detected_subscriptions" in data
    assert "micro_spending_leaks" in data
    assert len(data["detected_subscriptions"]) >= 1
    assert any(s["title"] == "Netflix Premium" for s in data["detected_subscriptions"])
    assert len(data["actionable_savings_tips"]) >= 1

