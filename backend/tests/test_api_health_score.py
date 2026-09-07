import pytest
from decimal import Decimal
from datetime import date


@pytest.mark.asyncio
async def test_financial_health_score_endpoint(client):
    """Test GET /api/v1/ai/health-score returns composite score, 5 pillars, and boosters."""
    # 1. Add some income
    await client.post(
        "/api/v1/incomes",
        json={
            "title": "Monthly Salary",
            "amount": "80000.00",
            "income_date": str(date.today()),
            "source": "Salary",
        },
    )

    # 2. Add an expense
    cat_resp = await client.get("/api/v1/categories")
    cat_id = cat_resp.json()[0]["id"]

    await client.post(
        "/api/v1/expenses",
        json={
            "title": "Grocery Shopping",
            "amount": "5000.00",
            "expense_date": str(date.today()),
            "category_id": cat_id,
            "payment_mode": "UPI",
        },
    )

    # 3. Call health score endpoint
    resp = await client.get("/api/v1/ai/health-score")
    assert resp.status_code == 200
    data = resp.json()

    assert "composite_score" in data
    assert 0 <= data["composite_score"] <= 100
    assert data["tier"] in ["elite", "healthy", "vulnerable", "critical"]
    assert "tier_title" in data
    assert "summary" in data
    assert len(data["pillars"]) == 5

    # Verify each of the 5 pillars
    pillar_names = {p["name"] for p in data["pillars"]}
    expected_names = {
        "Savings Discipline",
        "Budget Adherence",
        "Burn Stability",
        "Cash Cushion",
        "Leak Control",
    }
    assert pillar_names == expected_names

    # Check boosters
    assert len(data["score_boosters"]) >= 1
    assert data["monthly_income"] == "80000.00"
    assert data["monthly_spent"] == "5000.00"
    assert Decimal(str(data["monthly_net_savings"])) == Decimal("75000.00")
    assert data["savings_rate_pct"] > 0


@pytest.mark.asyncio
async def test_goals_runway_endpoint(client):
    """Test GET /api/v1/ai/goals-runway calculates multi-goal runway."""
    # Create an active goal
    await client.post(
        "/api/v1/goals",
        json={
            "name": "Japan Travel Fund",
            "target_amount": "150000.00",
            "current_amount": "30000.00",
            "target_date": "2026-11-30",
            "category": "Travel",
        },
    )

    resp = await client.get("/api/v1/ai/goals-runway")
    assert resp.status_code == 200
    data = resp.json()

    assert "monthly_surplus" in data
    assert "active_goals_count" in data
    assert data["active_goals_count"] >= 1
    assert "total_monthly_required" in data
    assert "surplus_coverage_pct" in data
    assert "is_fully_funded" in data
    assert len(data["goals"]) >= 1
    assert "ai_runway_summary" in data
