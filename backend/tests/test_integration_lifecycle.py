import uuid
from datetime import date
from decimal import Decimal
import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_end_to_end_financial_tracking_lifecycle(client: AsyncClient):
    """
    Comprehensive end-to-end integration test simulating a realistic user journey:
    1. Check OpenAPI docs and Health check
    2. Verify seeded starter categories
    3. User creates a custom category
    4. User sets monthly overall budget & custom category budget
    5. User adds multiple expenses
    6. Verify live budget status transitions (on_track -> near_limit / over_budget)
    7. User updates expense amount and reassigns category
    8. Verify budget recalculations for both affected categories
    9. User tests category safe delete with reassignment
    10. Verify dashboard analytics reflect all real data
    11. Clean up created entities
    """
    # 1. Health & Docs check
    health_resp = await client.get("/health")
    assert health_resp.status_code == 200
    assert health_resp.json() == {"status": "ok"}

    docs_resp = await client.get("/openapi.json")
    assert docs_resp.status_code == 200

    # 2. Check Seeded Categories
    cat_resp = await client.get("/api/v1/categories")
    assert cat_resp.status_code == 200
    categories = cat_resp.json()
    assert len(categories) >= 9
    food_cat = next(c for c in categories if c["name"] == "Food")
    transport_cat = next(c for c in categories if c["name"] == "Transport")

    # 3. Create Custom Category
    custom_cat_name = f"Hobbies_{uuid.uuid4().hex[:6]}"
    custom_cat_res = await client.post("/api/v1/categories", json={"name": custom_cat_name})
    assert custom_cat_res.status_code == 201
    custom_cat = custom_cat_res.json()
    custom_cat_id = custom_cat["id"]

    curr_month = date.today().replace(day=1).isoformat()

    # 4. Set Budgets: Overall = 5000, Custom Category = 1000
    await client.post(
        "/api/v1/budgets",
        json={
            "scope": "overall",
            "amount": "5000.00",
            "period_month": curr_month,
        },
    )
    await client.post(
        "/api/v1/budgets",
        json={
            "scope": "category",
            "category_id": custom_cat_id,
            "amount": "1000.00",
            "period_month": curr_month,
        },
    )

    # 5. Add Expense in Custom Category: 850.00 (85% of budget -> status should be 'near_limit')
    exp1_res = await client.post(
        "/api/v1/expenses",
        json={
            "title": "Guitar Strings & Picks",
            "category_id": custom_cat_id,
            "amount": "850.00",
            "expense_date": date.today().isoformat(),
            "payment_mode": "UPI",
            "notes": "Music store purchase",
        },
    )
    assert exp1_res.status_code == 201
    exp1 = exp1_res.json()
    exp1_id = exp1["id"]

    # 6. Check Budget Status -> near_limit
    budgets_res = await client.get("/api/v1/budgets", params={"period_month": curr_month})
    assert budgets_res.status_code == 200
    b_data = budgets_res.json()
    custom_budget_info = next(cb for cb in b_data["category_budgets"] if cb["category_id"] == custom_cat_id)
    assert custom_budget_info["status"] == "near_limit"
    assert custom_budget_info["percentage_used"] == 85.0

    # 7. Add another expense in Custom Category: 200.00 (Total 1050 > 1000 -> status should become 'over_budget')
    exp2_res = await client.post(
        "/api/v1/expenses",
        json={
            "title": "Music Sheet",
            "category_id": custom_cat_id,
            "amount": "200.00",
            "expense_date": date.today().isoformat(),
            "payment_mode": "Cash",
        },
    )
    assert exp2_res.status_code == 201
    exp2_id = exp2_res.json()["id"]

    budgets_res2 = await client.get("/api/v1/budgets", params={"period_month": curr_month})
    custom_budget_info2 = next(cb for cb in budgets_res2.json()["category_budgets"] if cb["category_id"] == custom_cat_id)
    assert custom_budget_info2["status"] == "over_budget"
    assert custom_budget_info2["remaining"] == "-50.00"

    # 8. Reassign exp2 to Food Category
    update_res = await client.put(
        f"/api/v1/expenses/{exp2_id}",
        json={"category_id": food_cat["id"]},
    )
    assert update_res.status_code == 200
    assert update_res.json()["category_id"] == food_cat["id"]

    # Verify Custom Category budget returned to 850 (near_limit)
    budgets_res3 = await client.get("/api/v1/budgets", params={"period_month": curr_month})
    custom_budget_info3 = next(cb for cb in budgets_res3.json()["category_budgets"] if cb["category_id"] == custom_cat_id)
    assert custom_budget_info3["status"] == "near_limit"
    assert custom_budget_info3["spent"] == "850.00"

    # 9. Delete Custom Category with reassignment of remaining exp1 to Transport
    del_cat_res = await client.delete(
        f"/api/v1/categories/{custom_cat_id}",
        params={"reassign_to": transport_cat["id"]},
    )
    assert del_cat_res.status_code == 200

    # Verify exp1 category is now Transport
    exp1_check = await client.get(f"/api/v1/expenses/{exp1_id}")
    assert exp1_check.status_code == 200
    assert exp1_check.json()["category_id"] == transport_cat["id"]

    # 10. Verify Dashboard Endpoints
    dash_summary = await client.get("/api/v1/dashboard/summary", params={"period_month": curr_month})
    assert dash_summary.status_code == 200
    assert Decimal(dash_summary.json()["total_spent"]) >= Decimal("1050.00")

    dash_breakdown = await client.get("/api/v1/dashboard/category-breakdown", params={"period_month": curr_month})
    assert dash_breakdown.status_code == 200

    # 11. Clean up created expenses
    await client.delete(f"/api/v1/expenses/{exp1_id}")
    await client.delete(f"/api/v1/expenses/{exp2_id}")
