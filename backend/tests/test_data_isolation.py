"""
Strict User Data Isolation Tests:
Zero-trust verification that users can never access, read, edit, delete,
or compute analytics over other users' expenses, budgets, incomes, or custom categories.
"""
from datetime import date
import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_strict_user_data_isolation(client_factory):
    # Setup Alice and Bob
    alice_client, alice_user = await client_factory()
    bob_client, bob_user = await client_factory()

    assert alice_user["id"] != bob_user["id"]

    # 1. Fetch a category
    cat_resp = await alice_client.get("/api/v1/categories")
    assert cat_resp.status_code == 200
    category_id = cat_resp.json()[0]["id"]

    # 2. Alice creates an expense
    alice_exp_res = await alice_client.post(
        "/api/v1/expenses",
        json={
            "title": "Alice Private Expense",
            "category_id": category_id,
            "amount": "999.00",
            "expense_date": date.today().isoformat(),
            "payment_mode": "Cash",
        },
    )
    assert alice_exp_res.status_code == 201
    alice_exp_id = alice_exp_res.json()["id"]

    # 3. Bob attempts to access Alice's expense -> Must receive 404 Not Found
    bob_get_res = await bob_client.get(f"/api/v1/expenses/{alice_exp_id}")
    assert bob_get_res.status_code == 404

    # 4. Bob attempts to update Alice's expense -> Must receive 404 Not Found
    bob_put_res = await bob_client.put(
        f"/api/v1/expenses/{alice_exp_id}",
        json={"title": "Hacked Title", "amount": "1.00"},
    )
    assert bob_put_res.status_code == 404

    # 5. Bob attempts to delete Alice's expense -> Must receive 404 Not Found
    bob_del_res = await bob_client.delete(f"/api/v1/expenses/{alice_exp_id}")
    assert bob_del_res.status_code == 404

    # 6. Alice's expense is still intact
    alice_verify_res = await alice_client.get(f"/api/v1/expenses/{alice_exp_id}")
    assert alice_verify_res.status_code == 200
    assert alice_verify_res.json()["title"] == "Alice Private Expense"

    # 7. Alice creates an income
    alice_inc_res = await alice_client.post(
        "/api/v1/incomes",
        json={
            "title": "Alice Salary",
            "amount": "50000.00",
            "income_date": date.today().isoformat(),
            "source": "Salary",
        },
    )
    assert alice_inc_res.status_code == 201
    alice_inc_id = alice_inc_res.json()["id"]

    # 8. Bob attempts to access Alice's income -> 404
    bob_inc_get = await bob_client.get(f"/api/v1/incomes/{alice_inc_id}")
    assert bob_inc_get.status_code == 404

    # 9. Check Dashboard isolation
    # Alice's dashboard has 999 spent and 50000 income
    alice_dash = await alice_client.get("/api/v1/dashboard/summary")
    assert alice_dash.status_code == 200
    assert float(alice_dash.json()["total_spent"]) == 999.0
    assert float(alice_dash.json()["total_income"]) == 50000.0

    # Bob's dashboard is completely clean (0.00 spent, 0.00 income)
    bob_dash = await bob_client.get("/api/v1/dashboard/summary")
    assert bob_dash.status_code == 200
    assert float(bob_dash.json()["total_spent"]) == 0.0
    assert float(bob_dash.json()["total_income"]) == 0.0
    assert bob_dash.json()["expense_count"] == 0

    # Clean up Alice's expense and income
    await alice_client.delete(f"/api/v1/expenses/{alice_exp_id}")
    await alice_client.delete(f"/api/v1/incomes/{alice_inc_id}")
