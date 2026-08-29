import uuid
from datetime import date
from decimal import Decimal
import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_income_crud_lifecycle(client: AsyncClient):
    income_id = None
    try:
        # 1. Create Income
        create_res = await client.post(
            "/api/v1/incomes",
            json={
                "title": "Monthly Software Engineering Salary",
                "amount": "65000.00",
                "income_date": str(date.today()),
                "source": "Salary",
                "payment_mode": "Bank Transfer",
                "notes": "Direct deposit",
            },
        )
        assert create_res.status_code == 201
        data = create_res.json()
        income_id = data["id"]
        assert data["title"] == "Monthly Software Engineering Salary"
        assert Decimal(data["amount"]) == Decimal("65000.00")
        assert data["source"] == "Salary"
        assert data["payment_mode"] == "Bank Transfer"

        # 2. Get Income by ID
        get_res = await client.get(f"/api/v1/incomes/{income_id}")
        assert get_res.status_code == 200
        assert get_res.json()["id"] == income_id

        # 3. List Incomes
        list_res = await client.get("/api/v1/incomes", params={"source": "Salary"})
        assert list_res.status_code == 200
        list_data = list_res.json()
        assert any(item["id"] == income_id for item in list_data["items"])

        # 4. Update Income via PATCH
        patch_res = await client.patch(
            f"/api/v1/incomes/{income_id}",
            json={"amount": "70000.00", "notes": "Bonus included"},
        )
        assert patch_res.status_code == 200
        assert Decimal(patch_res.json()["amount"]) == Decimal("70000.00")
        assert patch_res.json()["notes"] == "Bonus included"

        # 5. Delete Income via DELETE
        del_res = await client.delete(f"/api/v1/incomes/{income_id}")
        assert del_res.status_code == 200
        assert "deleted successfully" in del_res.json()["message"]
        income_id = None

        # 6. Verify Deletion
        get_after = await client.get(f"/api/v1/incomes/{data['id']}")
        assert get_after.status_code == 404
    finally:
        if income_id:
            await client.delete(f"/api/v1/incomes/{income_id}")


@pytest.mark.asyncio
async def test_income_validation_and_filtering(client: AsyncClient):
    income_ids = []
    try:
        # 1. Reject future date
        future_res = await client.post(
            "/api/v1/incomes",
            json={
                "title": "Future Pay",
                "amount": "1000.00",
                "income_date": "2099-01-01",
                "source": "Salary",
            },
        )
        assert future_res.status_code == 422

        # 2. Reject negative or zero amount
        neg_res = await client.post(
            "/api/v1/incomes",
            json={
                "title": "Zero Pay",
                "amount": "0.00",
                "income_date": str(date.today()),
                "source": "Salary",
            },
        )
        assert neg_res.status_code == 422

        # 3. Create items for filtering
        res1 = await client.post(
            "/api/v1/incomes",
            json={
                "title": "Freelance Web Design",
                "amount": "15000.00",
                "income_date": str(date.today()),
                "source": "Freelance",
            },
        )
        assert res1.status_code == 201
        income_ids.append(res1.json()["id"])

        res2 = await client.post(
            "/api/v1/incomes",
            json={
                "title": "Dividend Payout",
                "amount": "3000.00",
                "income_date": str(date.today()),
                "source": "Investment",
            },
        )
        assert res2.status_code == 201
        income_ids.append(res2.json()["id"])

        # Filter by Freelance
        free_res = await client.get("/api/v1/incomes", params={"source": "Freelance"})
        assert free_res.status_code == 200
        free_items = free_res.json()["items"]
        assert all(i["source"] == "Freelance" for i in free_items)

        # Summary check
        current_month = date.today().strftime("%Y-%m")
        sum_res = await client.get("/api/v1/incomes/summary", params={"period_month": current_month})
        assert sum_res.status_code == 200
        sum_data = sum_res.json()
        assert Decimal(sum_data["total_income"]) >= Decimal("18000.00")
        assert len(sum_data["breakdown_by_source"]) >= 2
    finally:
        for i_id in income_ids:
            await client.delete(f"/api/v1/incomes/{i_id}")


@pytest.mark.asyncio
async def test_dashboard_income_integration(client: AsyncClient):
    income_id = None
    try:
        res = await client.post(
            "/api/v1/incomes",
            json={
                "title": "Consulting Fee",
                "amount": "25000.00",
                "income_date": str(date.today()),
                "source": "Business",
            },
        )
        assert res.status_code == 201
        income_id = res.json()["id"]

        dash_res = await client.get("/api/v1/dashboard/summary")
        assert dash_res.status_code == 200
        data = dash_res.json()
        assert "total_income" in data
        assert "net_savings" in data
        assert "savings_rate" in data
        assert Decimal(data["total_income"]) >= Decimal("25000.00")
    finally:
        if income_id:
            await client.delete(f"/api/v1/incomes/{income_id}")
