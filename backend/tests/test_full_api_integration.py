import uuid
from datetime import date
from decimal import Decimal
import pytest
from httpx import ASGITransport, AsyncClient

from app.main import app, lifespan


@pytest.mark.asyncio
async def test_full_api_flow():
    async with lifespan(app):
        async with AsyncClient(
            transport=ASGITransport(app=app), base_url="http://test"
        ) as ac:
            # 1. Fetch Categories (seeded)
            cat_resp = await ac.get("/api/v1/categories")
            assert cat_resp.status_code == 200
            categories = cat_resp.json()
            assert len(categories) >= 9
            food_cat = next(c for c in categories if c["name"] == "Food")
            transport_cat = next(c for c in categories if c["name"] == "Transport")

            # 2. Set an Overall Budget & Category Budget
            curr_month_str = date.today().replace(day=1).isoformat()
            budget_resp = await ac.post(
                "/api/v1/budgets",
                json={
                    "scope": "overall",
                    "amount": "10000.00",
                    "period_month": curr_month_str,
                },
            )
            assert budget_resp.status_code == 200
            assert budget_resp.json()["amount"] == "10000.00"

            cat_budget_resp = await ac.post(
                "/api/v1/budgets",
                json={
                    "scope": "category",
                    "category_id": food_cat["id"],
                    "amount": "3000.00",
                    "period_month": curr_month_str,
                },
            )
            assert cat_budget_resp.status_code == 200

            # 3. Create an Expense
            exp_resp = await ac.post(
                "/api/v1/expenses",
                json={
                    "title": "Dinner at Restaurant",
                    "category_id": food_cat["id"],
                    "amount": "750.00",
                    "expense_date": date.today().isoformat(),
                    "payment_mode": "UPI",
                    "notes": "With team",
                },
            )
            assert exp_resp.status_code == 201
            expense = exp_resp.json()
            expense_id = expense["id"]
            assert expense["title"] == "Dinner at Restaurant"

            # 4. List Expenses with Filter & Search
            list_resp = await ac.get(
                "/api/v1/expenses",
                params={"search": "Dinner", "category_id": food_cat["id"]},
            )
            assert list_resp.status_code == 200
            items = list_resp.json()["items"]
            assert any(item["id"] == expense_id for item in items)

            # 5. Check Dashboard Summary
            summary_resp = await ac.get("/api/v1/dashboard/summary")
            assert summary_resp.status_code == 200
            summary = summary_resp.json()
            assert Decimal(summary["total_spent"]) >= Decimal("750.00")
            assert summary["status"] in ["on_track", "near_limit", "over_budget"]

            # 6. Check Dashboard Category Breakdown
            breakdown_resp = await ac.get("/api/v1/dashboard/category-breakdown")
            assert breakdown_resp.status_code == 200
            breakdown = breakdown_resp.json()
            assert len(breakdown) >= 1

            # 7. Check Dashboard Trend & Stats
            trend_resp = await ac.get("/api/v1/dashboard/trend")
            assert trend_resp.status_code == 200

            stats_resp = await ac.get("/api/v1/dashboard/stats")
            assert stats_resp.status_code == 200

            # 8. Try deleting Category with expenses (should fail 409)
            del_cat_resp = await ac.delete(f"/api/v1/categories/{food_cat['id']}")
            assert del_cat_resp.status_code == 409

            # 9. Reassign and Delete Category
            # Create a temporary custom category to test safe deletion
            new_cat_resp = await ac.post(
                "/api/v1/categories", json={"name": "Temp Category"}
            )
            assert new_cat_resp.status_code == 201
            temp_cat = new_cat_resp.json()

            # Create expense in Temp Category
            temp_exp_resp = await ac.post(
                "/api/v1/expenses",
                json={
                    "title": "Temp Expense",
                    "category_id": temp_cat["id"],
                    "amount": "100.00",
                    "expense_date": date.today().isoformat(),
                },
            )
            assert temp_exp_resp.status_code == 201
            temp_exp_id = temp_exp_resp.json()["id"]

            # Delete Temp Category with reassignment to Transport
            reassign_del_resp = await ac.delete(
                f"/api/v1/categories/{temp_cat['id']}",
                params={"reassign_to": transport_cat["id"]},
            )
            assert reassign_del_resp.status_code == 200

            # Verify the expense was moved to Transport
            moved_exp = await ac.get(f"/api/v1/expenses/{temp_exp_id}")
            assert moved_exp.status_code == 200
            assert moved_exp.json()["category_id"] == transport_cat["id"]

            # 10. Clean up created expenses
            await ac.delete(f"/api/v1/expenses/{expense_id}")
            await ac.delete(f"/api/v1/expenses/{temp_exp_id}")
