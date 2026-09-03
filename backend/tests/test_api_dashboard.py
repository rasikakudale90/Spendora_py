from datetime import date
from decimal import Decimal
import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_dashboard_all_endpoints(client: AsyncClient):
    # Fetch or create required categories for testing
    cat_resp = await client.get("/api/v1/categories")
    categories = cat_resp.json()
    food_cat = next((c for c in categories if c["name"] == "Food"), None)
    if not food_cat:
        f_resp = await client.post("/api/v1/categories", json={"name": "Food"})
        food_cat = f_resp.json()

    transport_cat = next((c for c in categories if c["name"] == "Transport"), None)
    if not transport_cat:
        t_resp = await client.post("/api/v1/categories", json={"name": "Transport"})
        transport_cat = t_resp.json()

    test_month = "2026-08-01"

    # Set overall budget
    await client.post(
        "/api/v1/budgets",
        json={
            "scope": "overall",
            "amount": "15000.00",
            "period_month": test_month,
        },
    )

    # Create test expenses
    exp1 = await client.post(
        "/api/v1/expenses",
        json={
            "title": "Supermarket Groceries",
            "category_id": food_cat["id"],
            "amount": "1200.00",
            "expense_date": "2026-08-05",
            "payment_mode": "UPI",
        },
    )
    exp2 = await client.post(
        "/api/v1/expenses",
        json={
            "title": "Metro Card Recharge",
            "category_id": transport_cat["id"],
            "amount": "500.00",
            "expense_date": "2026-08-12",
            "payment_mode": "Card",
        },
    )
    exp1_id = exp1.json()["id"]
    exp2_id = exp2.json()["id"]

    try:
        # 1. Dashboard Summary
        sum_res = await client.get("/api/v1/dashboard/summary", params={"period_month": test_month})
        assert sum_res.status_code == 200
        summary = sum_res.json()
        assert Decimal(summary["total_spent"]) >= Decimal("1700.00")
        assert summary["total_budget"] == "15000.00"
        assert summary["status"] in ["on_track", "near_limit", "over_budget"]
        assert summary["expense_count"] >= 2

        # 2. Recent Expenses
        rec_res = await client.get("/api/v1/dashboard/recent-expenses", params={"limit": 50})
        assert rec_res.status_code == 200
        rec_list = rec_res.json()
        assert isinstance(rec_list, list)
        assert len(rec_list) >= 2
        assert any(e["id"] == exp1_id for e in rec_list)

        # 3. Category Breakdown
        cb_res = await client.get("/api/v1/dashboard/category-breakdown", params={"period_month": test_month})
        assert cb_res.status_code == 200
        breakdown = cb_res.json()
        assert isinstance(breakdown, list)
        assert len(breakdown) >= 2
        total_pct = sum(item["percentage"] for item in breakdown)
        assert 99.0 <= total_pct <= 101.0  # Approx 100% due to rounding

        # 4. Trend
        trend_res = await client.get("/api/v1/dashboard/trend", params={"period_month": test_month})
        assert trend_res.status_code == 200
        trend = trend_res.json()
        assert isinstance(trend, list)
        assert len(trend) >= 2
        for t in trend:
            assert "label" in t
            assert "amount" in t
            assert "expense_count" in t

        # 5. Month-over-Month Comparison
        comp_res = await client.get("/api/v1/dashboard/comparison", params={"period_month": test_month})
        assert comp_res.status_code == 200
        comp = comp_res.json()
        assert "current_month_spend" in comp
        assert "previous_month_spend" in comp
        assert "percentage_change" in comp
        assert comp["trend"] in ["increased", "decreased", "unchanged"]

        # 6. Top Categories
        top_res = await client.get("/api/v1/dashboard/top-categories", params={"period_month": test_month, "limit": 3})
        assert top_res.status_code == 200
        top_cats = top_res.json()
        assert isinstance(top_cats, list)
        assert len(top_cats) >= 2
        assert top_cats[0]["rank"] == 1
        assert Decimal(top_cats[0]["amount"]) >= Decimal(top_cats[1]["amount"])

        # 7. Stats
        stats_res = await client.get("/api/v1/dashboard/stats", params={"period_month": test_month})
        assert stats_res.status_code == 200
        stats = stats_res.json()
        assert Decimal(stats["avg_daily_spend"]) > 0
        assert Decimal(stats["avg_weekly_spend"]) > 0
        assert stats["highest_expense_amount"] is not None
        assert Decimal(stats["highest_expense_amount"]) >= Decimal("1200.00")
        assert stats["total_expense_count"] >= 2

    finally:
        # Cleanup test expenses
        await client.delete(f"/api/v1/expenses/{exp1_id}")
        await client.delete(f"/api/v1/expenses/{exp2_id}")
