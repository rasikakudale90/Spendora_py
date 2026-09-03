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


@pytest.mark.asyncio
async def test_safe_to_spend_forecast(client: AsyncClient):
    # Call safe-to-spend endpoint
    resp = await client.get("/api/v1/ai/safe-to-spend")
    assert resp.status_code == 200
    data = resp.json()
    assert "daily_safe_spend" in data
    assert "burn_rate_status" in data
    assert data["burn_rate_status"] in ["optimal", "warning", "danger"]
    assert "current_burn_rate_per_day" in data
    assert "days_remaining_in_month" in data
    assert "projected_month_end_balance" in data
    assert "ai_recommendation" in data
    assert len(data["actionable_tips"]) >= 1


@pytest.mark.asyncio
async def test_chat_financial_assistant(client: AsyncClient):
    # 1. Ask about safe daily limit
    chat_resp = await client.post(
        "/api/v1/ai/chat",
        json={
            "message": "What is my daily safe spending limit?",
            "history": [],
        },
    )
    assert chat_resp.status_code == 200
    data = chat_resp.json()
    assert "reply" in data
    assert "suggested_prompts" in data
    assert len(data["suggested_prompts"]) >= 1
    assert "context_summary" in data
    assert "provider_used" in data

    # 2. Ask affordability question
    afford_resp = await client.post(
        "/api/v1/ai/chat",
        json={
            "message": "Can I afford to buy new headphones for 2500?",
            "history": [{"role": "user", "content": "What is my balance?"}],
        },
    )
    assert afford_resp.status_code == 200
    afford_data = afford_resp.json()
    assert "reply" in afford_data
    assert "action_intent" in afford_data
    if afford_data["action_intent"]:
        assert afford_data["action_intent"]["action"] == "simulate_purchase"


@pytest.mark.asyncio
async def test_extract_transaction_sms_and_duplicate(client: AsyncClient):
    # 1. Test UPI Debit SMS (Swiggy food order)
    sms_debit = "Sent Rs. 450.00 to Swiggy on 12-09-2026 via UPI Ref 429381029182. Avl Bal Rs. 32,450.00"
    resp = await client.post(
        "/api/v1/ai/extract-transaction",
        json={"text": sms_debit, "source_type": "sms_text"},
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["type"] == "expense"
    assert "Swiggy" in data["title"]
    assert float(data["amount"]) == 450.00
    assert data["payment_mode"] == "UPI"
    assert "Food" in data["category_name"]
    assert "Avl Bal" not in data["sanitized_input"]

    # 2. Test Salary / Income Credit SMS
    sms_credit = "A/c *1234 credited by Rs. 65,000.00 on 01-09-2026 by InfoTech Salary. Net Bal Rs. 85,000"
    resp_credit = await client.post(
        "/api/v1/ai/extract-transaction",
        json={"text": sms_credit, "source_type": "sms_text"},
    )
    assert resp_credit.status_code == 200
    data_credit = resp_credit.json()
    assert data_credit["type"] == "income"
    assert float(data_credit["amount"]) == 65000.00
    assert "*1234" not in data_credit["sanitized_input"]




