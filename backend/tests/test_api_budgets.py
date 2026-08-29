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

    # 1. Set Monthly Overall Budget
    res_overall = await client.post(
        "/api/v1/budgets",
        json={
            "scope": "overall",
            "amount": "20000.00",
            "period_type": "monthly",
            "period_month": test_month,
        },
    )
    assert res_overall.status_code == 200
    overall_data = res_overall.json()
    assert overall_data["scope"] == "overall"
    assert overall_data["period_type"] == "monthly"
    assert overall_data["amount"] == "20000.00"
    assert overall_data["category_id"] is None

    # 2. Update Monthly Overall Budget (upsert via POST)
    res_overall_update = await client.post(
        "/api/v1/budgets",
        json={
            "scope": "overall",
            "amount": "22000.00",
            "period_type": "monthly",
            "period_month": test_month,
        },
    )
    assert res_overall_update.status_code == 200
    assert res_overall_update.json()["amount"] == "22000.00"

    # 3. Set Monthly Category Budget
    res_cat = await client.post(
        "/api/v1/budgets",
        json={
            "scope": "category",
            "category_id": cat_id,
            "amount": "5000.00",
            "period_type": "monthly",
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
            "period_type": "monthly",
            "period_month": test_month,
        },
    )
    assert res_bad_cat.status_code == 400

    # 5. Retrieve monthly budgets
    get_res = await client.get("/api/v1/budgets", params={"period_month": test_month, "period_type": "monthly"})
    assert get_res.status_code == 200
    body = get_res.json()
    assert body["period_type"] == "monthly"
    assert body["overall_budget"] is not None
    assert body["overall_budget"]["amount"] == "22000.00"
    assert isinstance(body["category_budgets"], list)
    assert any(cb["category_id"] == cat_id for cb in body["category_budgets"])


@pytest.mark.asyncio
async def test_weekly_and_yearly_budgets(client: AsyncClient):
    cat_resp = await client.get("/api/v1/categories")
    categories = cat_resp.json()
    cat_id = categories[0]["id"]
    test_date = "2026-08-27"

    # 1. Set Weekly Budget
    res_weekly = await client.post(
        "/api/v1/budgets",
        json={
            "scope": "overall",
            "amount": "4000.00",
            "period_type": "weekly",
            "period_start": test_date,
        },
    )
    assert res_weekly.status_code == 200
    weekly_data = res_weekly.json()
    assert weekly_data["period_type"] == "weekly"
    assert weekly_data["amount"] == "4000.00"
    assert weekly_data["period_start"] == "2026-08-24"  # Monday of week containing Aug 27
    assert weekly_data["period_end"] == "2026-08-30"    # Sunday of that week

    # 2. Set Yearly Budget
    res_yearly = await client.post(
        "/api/v1/budgets",
        json={
            "scope": "category",
            "category_id": cat_id,
            "amount": "120000.00",
            "period_type": "yearly",
            "period_start": test_date,
        },
    )
    assert res_yearly.status_code == 200
    yearly_data = res_yearly.json()
    assert yearly_data["period_type"] == "yearly"
    assert yearly_data["amount"] == "120000.00"
    assert yearly_data["period_start"] == "2026-01-01"
    assert yearly_data["period_end"] == "2026-12-31"

    # 3. Retrieve Weekly Budgets
    get_weekly = await client.get(
        "/api/v1/budgets",
        params={"period_date": test_date, "period_type": "weekly"},
    )
    assert get_weekly.status_code == 200
    w_body = get_weekly.json()
    assert w_body["period_type"] == "weekly"
    assert w_body["period_start"] == "2026-08-24"
    assert w_body["period_end"] == "2026-08-30"
    assert w_body["overall_budget"] is not None
    assert w_body["overall_budget"]["amount"] == "4000.00"

    # 4. Retrieve Yearly Budgets
    get_yearly = await client.get(
        "/api/v1/budgets",
        params={"period_date": test_date, "period_type": "yearly"},
    )
    assert get_yearly.status_code == 200
    y_body = get_yearly.json()
    assert y_body["period_type"] == "yearly"
    assert y_body["period_start"] == "2026-01-01"
    assert y_body["period_end"] == "2026-12-31"
    assert any(cb["category_id"] == cat_id for cb in y_body["category_budgets"])


@pytest.mark.asyncio
async def test_budget_patch_and_delete(client: AsyncClient):
    cat_resp = await client.get("/api/v1/categories")
    cat_id = cat_resp.json()[0]["id"]
    test_date = "2026-09-01"

    # 1. Create a budget
    create_res = await client.post(
        "/api/v1/budgets",
        json={
            "scope": "category",
            "category_id": cat_id,
            "amount": "8000.00",
            "period_type": "monthly",
            "period_start": test_date,
        },
    )
    assert create_res.status_code == 200
    budget_id = create_res.json()["id"]

    # 2. Update the budget amount via PATCH
    patch_res = await client.patch(
        f"/api/v1/budgets/{budget_id}",
        json={"amount": "9500.00"},
    )
    assert patch_res.status_code == 200
    assert patch_res.json()["amount"] == "9500.00"

    # 3. Delete the budget via DELETE
    del_res = await client.delete(f"/api/v1/budgets/{budget_id}")
    assert del_res.status_code == 200
    assert "deleted successfully" in del_res.json()["message"]

    # 4. Verify budget is gone from the list
    get_res = await client.get(
        "/api/v1/budgets",
        params={"period_date": test_date, "period_type": "monthly"},
    )
    assert get_res.status_code == 200
    assert not any(b["id"] == budget_id for b in get_res.json()["category_budgets"])


@pytest.mark.asyncio
async def test_daily_budget_lifecycle_and_alerts(client: AsyncClient):
    cat_resp = await client.get("/api/v1/categories")
    categories = cat_resp.json()
    cat_id = categories[0]["id"]
    test_date = "2026-08-28"

    budget_id = None
    expense_id = None
    try:
        # 1. Set Daily Overall Budget
        res_daily = await client.post(
            "/api/v1/budgets",
            json={
                "scope": "overall",
                "amount": "500.00",
                "period_type": "daily",
                "period_start": test_date,
            },
        )
        assert res_daily.status_code == 200
        daily_data = res_daily.json()
        budget_id = daily_data["id"]
        assert daily_data["period_type"] == "daily"
        assert daily_data["amount"] == "500.00"
        assert daily_data["period_start"] == test_date
        assert daily_data["period_end"] == test_date

        # 2. Retrieve Daily Budgets
        get_daily = await client.get(
            "/api/v1/budgets",
            params={"period_date": test_date, "period_type": "daily"},
        )
        assert get_daily.status_code == 200
        d_body = get_daily.json()
        assert d_body["period_type"] == "daily"
        assert d_body["period_start"] == test_date
        assert d_body["period_end"] == test_date
        assert d_body["overall_budget"] is not None
        assert d_body["overall_budget"]["amount"] == "500.00"

        # 3. Create an expense of ₹700 on test_date that exceeds the daily limit
        exp_res = await client.post(
            "/api/v1/expenses",
            json={
                "title": "Daily Limit Test Overspend",
                "category_id": cat_id,
                "amount": "700.00",
                "expense_date": test_date,
                "payment_mode": "UPI",
            },
        )
        assert exp_res.status_code == 201
        exp_data = exp_res.json()
        expense_id = exp_data["id"]

        # 4. Verify daily budget alert was generated in response
        assert exp_data["daily_budget_alert"] is not None
        assert exp_data["daily_budget_alert"]["exceeded"] is True
        assert Decimal(exp_data["daily_budget_alert"]["limit_amount"]) == Decimal("500.00")
        assert Decimal(exp_data["daily_budget_alert"]["total_spent"]) >= Decimal("700.00")
        assert "exceeded" in exp_data["daily_budget_alert"]["message"].lower()

        # 5. Update daily budget to ₹1000 via PATCH
        patch_res = await client.patch(
            f"/api/v1/budgets/{budget_id}",
            json={"amount": "1000.00"},
        )
        assert patch_res.status_code == 200
        assert patch_res.json()["amount"] == "1000.00"

        # 6. Delete daily budget via DELETE
        del_res = await client.delete(f"/api/v1/budgets/{budget_id}")
        assert del_res.status_code == 200
        budget_id = None

        # 7. Verify deletion
        get_after = await client.get(
            "/api/v1/budgets",
            params={"period_date": test_date, "period_type": "daily"},
        )
        assert get_after.status_code == 200
        assert get_after.json()["overall_budget"] is None
    finally:
        if expense_id:
            await client.delete(f"/api/v1/expenses/{expense_id}")
        if budget_id:
            await client.delete(f"/api/v1/budgets/{budget_id}")

