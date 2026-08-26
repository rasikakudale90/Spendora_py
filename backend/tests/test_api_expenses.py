import uuid
from datetime import date, timedelta
from decimal import Decimal
import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_expense_crud_lifecycle(client: AsyncClient):
    # Fetch a category
    cat_resp = await client.get("/api/v1/categories")
    categories = cat_resp.json()
    cat_id = categories[0]["id"]

    # 1. Create Expense
    create_payload = {
        "title": "Office Lunch",
        "category_id": cat_id,
        "amount": "250.00",
        "expense_date": date.today().isoformat(),
        "notes": "Team buffet",
        "payment_mode": "UPI",
    }
    create_res = await client.post("/api/v1/expenses", json=create_payload)
    assert create_res.status_code == 201
    created_expense = create_res.json()
    exp_id = created_expense["id"]
    assert created_expense["title"] == "Office Lunch"
    assert created_expense["amount"] == "250.00"
    assert created_expense["payment_mode"] == "UPI"
    assert created_expense["category_id"] == cat_id

    # 2. Get Expense by ID
    get_res = await client.get(f"/api/v1/expenses/{exp_id}")
    assert get_res.status_code == 200
    assert get_res.json()["id"] == exp_id

    # 3. Update Expense
    update_payload = {
        "title": "Office Lunch & Coffee",
        "amount": "320.00",
        "payment_mode": "Card",
    }
    update_res = await client.put(f"/api/v1/expenses/{exp_id}", json=update_payload)
    assert update_res.status_code == 200
    updated_exp = update_res.json()
    assert updated_exp["title"] == "Office Lunch & Coffee"
    assert updated_exp["amount"] == "320.00"
    assert updated_exp["payment_mode"] == "Card"

    # 4. Delete Expense
    del_res = await client.delete(f"/api/v1/expenses/{exp_id}")
    assert del_res.status_code == 200

    # 5. Verify 404 after deletion
    get_after_del = await client.get(f"/api/v1/expenses/{exp_id}")
    assert get_after_del.status_code == 404


@pytest.mark.asyncio
async def test_expense_creation_errors(client: AsyncClient):
    # Non-existent category -> 400
    res = await client.post(
        "/api/v1/expenses",
        json={
            "title": "Coffee",
            "category_id": str(uuid.uuid4()),
            "amount": "50.00",
            "expense_date": date.today().isoformat(),
        },
    )
    assert res.status_code == 400

    # Future date -> 422
    cat_resp = await client.get("/api/v1/categories")
    cat_id = cat_resp.json()[0]["id"]
    future_date = (date.today() + timedelta(days=5)).isoformat()
    res_future = await client.post(
        "/api/v1/expenses",
        json={
            "title": "Future train",
            "category_id": cat_id,
            "amount": "100.00",
            "expense_date": future_date,
        },
    )
    assert res_future.status_code == 422

    # Negative / zero amount -> 422
    res_zero = await client.post(
        "/api/v1/expenses",
        json={
            "title": "Zero expense",
            "category_id": cat_id,
            "amount": "0.00",
            "expense_date": date.today().isoformat(),
        },
    )
    assert res_zero.status_code == 422


@pytest.mark.asyncio
async def test_expense_filters_and_search(client: AsyncClient):
    cat_resp = await client.get("/api/v1/categories")
    cat_id = cat_resp.json()[0]["id"]

    # Seed 3 distinct expenses
    e1 = await client.post(
        "/api/v1/expenses",
        json={
            "title": "Alpha Item Alpha",
            "category_id": cat_id,
            "amount": "100.00",
            "expense_date": "2026-08-01",
            "payment_mode": "Cash",
            "notes": "TagOne",
        },
    )
    e2 = await client.post(
        "/api/v1/expenses",
        json={
            "title": "Beta Item Beta",
            "category_id": cat_id,
            "amount": "500.00",
            "expense_date": "2026-08-10",
            "payment_mode": "UPI",
            "notes": "TagTwo",
        },
    )
    e3 = await client.post(
        "/api/v1/expenses",
        json={
            "title": "Gamma Item Gamma",
            "category_id": cat_id,
            "amount": "1500.00",
            "expense_date": "2026-08-20",
            "payment_mode": "Net Banking",
            "notes": "TagThree",
        },
    )
    e1_id, e2_id, e3_id = e1.json()["id"], e2.json()["id"], e3.json()["id"]

    try:
        # Search by Title
        search_res = await client.get("/api/v1/expenses", params={"search": "Beta Item"})
        assert search_res.status_code == 200
        items = search_res.json()["items"]
        assert any(i["id"] == e2_id for i in items)
        assert not any(i["id"] == e1_id for i in items)

        # Search by Notes
        notes_search = await client.get("/api/v1/expenses", params={"search": "TagThree"})
        assert notes_search.status_code == 200
        assert any(i["id"] == e3_id for i in notes_search.json()["items"])

        # Filter by Amount Range
        amt_res = await client.get("/api/v1/expenses", params={"min_amount": "400.00", "max_amount": "1000.00"})
        assert amt_res.status_code == 200
        assert any(i["id"] == e2_id for i in amt_res.json()["items"])
        assert not any(i["id"] == e1_id for i in amt_res.json()["items"])
        assert not any(i["id"] == e3_id for i in amt_res.json()["items"])

        # Filter by Date Range
        date_res = await client.get("/api/v1/expenses", params={"date_from": "2026-08-05", "date_to": "2026-08-15"})
        assert date_res.status_code == 200
        assert any(i["id"] == e2_id for i in date_res.json()["items"])
        assert not any(i["id"] == e1_id for i in date_res.json()["items"])

        # Filter by Payment Mode
        pm_res = await client.get("/api/v1/expenses", params={"payment_mode": "Cash"})
        assert pm_res.status_code == 200
        assert any(i["id"] == e1_id for i in pm_res.json()["items"])
        assert not any(i["id"] == e2_id for i in pm_res.json()["items"])

        # Sorting Test: Amount Descending
        sort_res = await client.get(
            "/api/v1/expenses",
            params={"category_id": cat_id, "sort_by": "amount", "sort_order": "desc", "page_size": 50},
        )
        assert sort_res.status_code == 200
        sorted_items = sort_res.json()["items"]
        amounts = [Decimal(i["amount"]) for i in sorted_items]
        assert amounts == sorted(amounts, reverse=True)

        # Pagination Test
        pag_res = await client.get("/api/v1/expenses", params={"page": 1, "page_size": 2})
        assert pag_res.status_code == 200
        data = pag_res.json()
        assert len(data["items"]) <= 2
        assert data["page"] == 1
        assert data["page_size"] == 2
        assert data["total_pages"] >= 1

    finally:
        # Clean up seeded test records
        await client.delete(f"/api/v1/expenses/{e1_id}")
        await client.delete(f"/api/v1/expenses/{e2_id}")
        await client.delete(f"/api/v1/expenses/{e3_id}")
