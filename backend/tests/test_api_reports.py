from datetime import date
import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_export_expenses_csv(client: AsyncClient):
    # 1. Fetch starter category
    cat_resp = await client.get("/api/v1/categories")
    assert cat_resp.status_code == 200
    categories = cat_resp.json()
    cat_id = categories[0]["id"]

    # 2. Create an expense
    create_resp = await client.post(
        "/api/v1/expenses",
        json={
            "title": "Cloud Server Hosting",
            "amount": 1250.00,
            "expense_date": date.today().isoformat(),
            "category_id": cat_id,
            "payment_mode": "Card",
            "notes": "Monthly billing",
        },
    )
    assert create_resp.status_code == 201

    # 3. Export CSV
    csv_resp = await client.get("/api/v1/reports/expenses/csv")
    assert csv_resp.status_code == 200
    assert csv_resp.headers["content-type"].startswith("text/csv")
    assert "spendora_expenses_" in csv_resp.headers["content-disposition"]
    
    text = csv_resp.text
    assert "Expense ID,Date,Title,Category,Amount (INR),Payment Mode,Notes" in text
    assert "Cloud Server Hosting" in text
    assert "1250.00" in text


@pytest.mark.asyncio
async def test_export_incomes_csv(client: AsyncClient):
    # 1. Create an income
    inc_resp = await client.post(
        "/api/v1/incomes",
        json={
            "title": "Consulting Retainer",
            "amount": 75000.00,
            "income_date": date.today().isoformat(),
            "source": "Freelance",
            "notes": "Q3 milestone",
        },
    )
    assert inc_resp.status_code == 201

    # 2. Export Incomes CSV
    csv_resp = await client.get("/api/v1/reports/incomes/csv")
    assert csv_resp.status_code == 200
    assert csv_resp.headers["content-type"].startswith("text/csv")
    assert "spendora_incomes_" in csv_resp.headers["content-disposition"]
    
    text = csv_resp.text
    assert "Income ID,Date,Title,Source,Amount (INR),Notes" in text
    assert "Consulting Retainer" in text
    assert "75000.00" in text


@pytest.mark.asyncio
async def test_monthly_statement_summary(client: AsyncClient):
    today = date.today()
    period_month = f"{today.year:04d}-{today.month:02d}"

    stmt_resp = await client.get(f"/api/v1/reports/statement?period_month={period_month}")
    assert stmt_resp.status_code == 200
    data = stmt_resp.json()
    assert data["period_month"] == period_month
    assert "total_income" in data
    assert "total_spent" in data
    assert "net_cash_flow" in data
    assert "savings_rate_pct" in data
    assert "category_allocations" in data
