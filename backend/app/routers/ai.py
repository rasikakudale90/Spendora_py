from __future__ import annotations

import calendar
from datetime import date, timedelta
from decimal import Decimal
from typing import Optional
from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.core.dependencies import get_current_user
from app.models.user import User
from app.repositories.budget_repository import BudgetRepository
from app.repositories.category_repository import CategoryRepository
from app.repositories.dashboard_repository import DashboardRepository
from app.repositories.expense_repository import ExpenseRepository
from app.repositories.income_repository import IncomeRepository
from app.repositories.goal_repository import GoalRepository
from app.schemas.ai import (
    PurchaseSimulationRequest,
    PurchaseSimulationResponse,
    LeakAnalysisResponse,
    SafeToSpendResponse,
    FinancialChatRequest,
    FinancialChatResponse,
    TransactionExtractionRequest,
    TransactionExtractionResponse,
    FinancialHealthResponse,
    GoalRunwayAnalysisResponse,
)
from app.services.ai_service import ai_service

router = APIRouter(prefix="/ai", tags=["AI Financial Intelligence"])


@router.post("/simulate-purchase", response_model=PurchaseSimulationResponse)
async def simulate_purchase(
    payload: PurchaseSimulationRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """
    Simulate the real-time financial impact of a potential purchase.
    Uses AI reasoning (Gemini/OpenAI/Claude/Groq) with a deterministic mathematical fallback.
    """
    today = date.today()
    start_of_month = date(today.year, today.month, 1)
    _, total_days_in_month = calendar.monthrange(today.year, today.month)
    end_of_month = date(today.year, today.month, total_days_in_month)
    days_remaining = max(1, total_days_in_month - today.day + 1)

    dashboard_repo = DashboardRepository(db)
    budget_repo = BudgetRepository(db)
    income_repo = IncomeRepository(db)
    category_repo = CategoryRepository(db)

    # 1. Total spent & income this month
    total_spent, _ = await dashboard_repo.get_period_spending_and_count(
        current_user.id, start_of_month, end_of_month
    )
    total_income = await income_repo.get_total_for_period(
        current_user.id, start_of_month, end_of_month
    )

    # 2. Overall budget
    overall_budget_obj = await budget_repo.get_overall(current_user.id, start_of_month)
    overall_budget = Decimal(str(overall_budget_obj.amount)) if overall_budget_obj else None

    # 3. Optional category-specific details
    category_name = None
    category_spent = None
    category_budget = None
    if payload.category_id:
        cat = await category_repo.get_by_id(current_user.id, payload.category_id)
        if cat:
            category_name = cat.name
            category_spent = await dashboard_repo.get_category_spending_for_period(
                current_user.id, payload.category_id, start_of_month, end_of_month
            )
            cat_budget_obj = await budget_repo.get_by_category(
                current_user.id, payload.category_id, start_of_month
            )
            if cat_budget_obj:
                category_budget = Decimal(str(cat_budget_obj.amount))

    return await ai_service.simulate_purchase(
        request=payload,
        total_income=total_income,
        total_spent=total_spent,
        overall_budget=overall_budget,
        days_remaining_in_month=days_remaining,
        category_name=category_name,
        category_spent=category_spent,
        category_budget=category_budget,
    )


@router.get("/leak-analysis", response_model=LeakAnalysisResponse)
async def get_leak_analysis(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """
    Scan 90 days of transaction history to identify recurring subscriptions and micro-spending leaks.
    """
    from datetime import date, timedelta
    from app.repositories.expense_repository import ExpenseRepository

    today = date.today()
    ninety_days_ago = today - timedelta(days=90)
    start_of_month = date(today.year, today.month, 1)
    _, total_days_in_month = calendar.monthrange(today.year, today.month)
    end_of_month = date(today.year, today.month, total_days_in_month)

    expense_repo = ExpenseRepository(db)
    income_repo = IncomeRepository(db)

    # 1. Fetch past 90 days expenses for pattern matching
    expenses_seq, _ = await expense_repo.get_paginated(
        user_id=current_user.id,
        date_from=ninety_days_ago,
        date_to=today,
        page=1,
        page_size=500,
    )

    serialized_expenses = []
    for exp in expenses_seq:
        serialized_expenses.append({
            "id": str(exp.id),
            "title": exp.title,
            "amount": float(exp.amount),
            "expense_date": exp.expense_date,
            "category_name": exp.category.name if exp.category else "Uncategorized",
        })

    # 2. Total income for current month
    total_monthly_income = await income_repo.get_total_for_period(
        current_user.id, start_of_month, end_of_month
    )

    return await ai_service.analyze_leaks_and_subscriptions(
        expenses=serialized_expenses,
        total_monthly_income=total_monthly_income,
    )


@router.get("/safe-to-spend", response_model=SafeToSpendResponse)
async def get_safe_to_spend_forecast(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """
    Get dynamic real-time daily safe burn allowance and month-end trajectory forecast.
    """
    today = date.today()
    start_of_month = date(today.year, today.month, 1)
    _, total_days_in_month = calendar.monthrange(today.year, today.month)
    end_of_month = date(today.year, today.month, total_days_in_month)

    dashboard_repo = DashboardRepository(db)
    budget_repo = BudgetRepository(db)
    income_repo = IncomeRepository(db)

    # Total spent & income this month
    total_spent, _ = await dashboard_repo.get_period_spending_and_count(
        current_user.id, start_of_month, end_of_month
    )
    total_income = await income_repo.get_total_for_period(
        current_user.id, start_of_month, end_of_month
    )

    # Overall budget
    overall_budget_obj = await budget_repo.get_overall(current_user.id, start_of_month)
    overall_budget = Decimal(str(overall_budget_obj.amount)) if overall_budget_obj else None

    return await ai_service.calculate_safe_to_spend(
        total_income=total_income,
        total_spent=total_spent,
        overall_budget=overall_budget,
        today=today,
    )


@router.post("/chat", response_model=FinancialChatResponse)
async def chat_with_financial_assistant(
    payload: FinancialChatRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """
    Feature 4: Natural Language Financial Assistant / Chatbot.
    Aggregates real-time financial telemetry (income, spending, active budgets, top categories,
    safe burn velocity, and recent transactions) and answers natural language inquiries.
    """
    today = date.today()
    start_of_month = date(today.year, today.month, 1)
    _, total_days_in_month = calendar.monthrange(today.year, today.month)
    end_of_month = date(today.year, today.month, total_days_in_month)
    days_remaining = max(1, total_days_in_month - today.day + 1)

    dashboard_repo = DashboardRepository(db)
    budget_repo = BudgetRepository(db)
    income_repo = IncomeRepository(db)
    expense_repo = ExpenseRepository(db)

    # 1. Total spent & income this month
    total_spent, _ = await dashboard_repo.get_period_spending_and_count(
        current_user.id, start_of_month, end_of_month
    )
    total_income = await income_repo.get_total_for_period(
        current_user.id, start_of_month, end_of_month
    )
    net_savings = total_income - total_spent
    savings_rate_pct = (
        round(float((net_savings / total_income) * 100), 1)
        if total_income > 0
        else 0.0
    )

    # 2. Overall budget and status
    overall_budget_obj = await budget_repo.get_overall(current_user.id, start_of_month)
    overall_budget = Decimal(str(overall_budget_obj.amount)) if overall_budget_obj else None
    remaining_budget = max(Decimal("0.00"), (overall_budget - total_spent)) if overall_budget else None

    # 3. Daily safe spend
    effective_buffer = remaining_budget if remaining_budget is not None else max(Decimal("0.00"), net_savings)
    daily_safe_spend = (effective_buffer / Decimal(str(days_remaining))).quantize(Decimal("0.01"))

    # 4. Top spending categories this month
    breakdown_data = await dashboard_repo.get_category_breakdown(
        current_user.id, start_of_month, end_of_month
    )
    top_categories = []
    for row in breakdown_data:
        cat_name = row[1]
        cat_spent = row[2]
        pct = round(float((cat_spent / total_spent) * 100), 1) if total_spent > 0 else 0.0
        top_categories.append({
            "name": cat_name,
            "spent": float(cat_spent),
            "percentage": pct,
        })

    # 5. Recent 5 expenses
    recent_exp_objs = await dashboard_repo.get_recent_expenses(current_user.id, limit=5)
    recent_expenses = []
    for exp in recent_exp_objs:
        recent_expenses.append({
            "title": exp.title,
            "amount": float(exp.amount),
            "expense_date": str(exp.expense_date),
            "category_name": exp.category.name if exp.category else "Uncategorized",
        })

    # 6. Active budgets list
    active_budgets_list = []
    try:
        user_budgets = await budget_repo.get_all_for_period(current_user.id, start_of_month, "monthly")
        for b in user_budgets:
            cat_name = b.category.name if b.category else "Overall Budget"
            active_budgets_list.append({
                "category": cat_name,
                "amount": float(b.amount),
                "spent": float(b.spent),
                "status": b.status,
            })
    except Exception:
        pass

    # Build context dictionary
    financial_context = {
        "total_income": total_income,
        "total_spent": total_spent,
        "net_savings": net_savings,
        "savings_rate_pct": savings_rate_pct,
        "daily_safe_spend": daily_safe_spend,
        "days_remaining": days_remaining,
        "overall_budget": overall_budget,
        "remaining_budget": remaining_budget,
        "top_categories": top_categories,
        "recent_expenses": recent_expenses,
        "active_budgets": active_budgets_list,
    }

    serialized_history = [
        {"role": h.role, "content": h.content, "timestamp": h.timestamp}
        for h in (payload.history or [])
    ]

    result = await ai_service.chat_financial_advisor(
        message=payload.message,
        history=serialized_history,
        context=financial_context,
    )

    return FinancialChatResponse(
        reply=result["reply"],
        suggested_prompts=result["suggested_prompts"],
        action_intent=result.get("action_intent"),
        context_summary=result["context_summary"],
        provider_used=result["provider_used"],
    )


@router.post("/extract-transaction", response_model=TransactionExtractionResponse)
async def extract_transaction_endpoint(
    payload: TransactionExtractionRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """
    Feature 5: Smart Receipt & UPI SMS Parser.
    Extracts transaction fields (type, title, amount, category, date, payment mode)
    from pasted bank/UPI SMS notifications or scanned receipts with duplicate detection.
    """
    cat_repo = CategoryRepository(db)
    dashboard_repo = DashboardRepository(db)
    income_repo = IncomeRepository(db)

    # 1. Fetch user categories
    cat_pairs = await cat_repo.get_all_with_counts(current_user.id)
    serialized_categories = [{"id": str(c.id), "name": c.name} for c, _ in cat_pairs]

    # 2. Fetch recent transactions for duplicate detection
    recent_expenses = await dashboard_repo.get_recent_expenses(current_user.id, limit=15)
    recent_incomes, _ = await income_repo.get_paginated(user_id=current_user.id, page=1, page_size=15)

    combined_recent = []
    for exp in recent_expenses:
        combined_recent.append({
            "title": exp.title,
            "amount": exp.amount,
            "expense_date": str(exp.expense_date),
        })
    for inc in recent_incomes:
        combined_recent.append({
            "title": inc.title,
            "amount": inc.amount,
            "income_date": str(inc.income_date),
        })

    result = await ai_service.extract_transaction(
        request_text=payload.text,
        image_base64=payload.image_base64,
        source_type=payload.source_type,
        user_categories=serialized_categories,
        recent_transactions=combined_recent,
    )

    return TransactionExtractionResponse(**result)


@router.get("/health-score", response_model=FinancialHealthResponse)
async def get_financial_health_score(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """
    Feature 6: AI Financial Health Score & 5-Pillar Radar.
    Evaluates Savings Discipline, Budget Adherence, Burn Stability, Cash Cushion,
    and Micro-Leak Control into a composite 0-100 prestige score with actionable boosters.
    """
    today = date.today()
    start_of_month = date(today.year, today.month, 1)
    _, total_days = calendar.monthrange(today.year, today.month)
    end_of_month = date(today.year, today.month, total_days)
    days_passed = max(1, today.day)
    days_remaining = max(1, total_days - today.day + 1)

    dashboard_repo = DashboardRepository(db)
    budget_repo = BudgetRepository(db)
    income_repo = IncomeRepository(db)
    expense_repo = ExpenseRepository(db)

    # 1. Total spent & income this month
    total_spent, _ = await dashboard_repo.get_period_spending_and_count(
        current_user.id, start_of_month, end_of_month
    )
    total_income = await income_repo.get_total_for_period(
        current_user.id, start_of_month, end_of_month
    )

    # 2. Overall budget & Category adherence
    category_breakdown = await dashboard_repo.get_category_breakdown(
        current_user.id, start_of_month, end_of_month
    )
    cat_spent_map = {cat_id: amt for cat_id, _, amt in category_breakdown}

    all_budgets = await budget_repo.get_by_period(
        current_user.id, period_start=start_of_month, period_type="monthly"
    )
    overall_budget_obj = next((b for b in all_budgets if b.scope == "overall"), None)
    overall_budget_amount = overall_budget_obj.amount if overall_budget_obj else None

    category_budgets = [b for b in all_budgets if b.scope == "category" and b.category_id]
    if category_budgets:
        adhered_count = sum(
            1 for b in category_budgets if cat_spent_map.get(b.category_id, Decimal("0.00")) <= b.amount
        )
        budget_adherence_pct = (adhered_count / len(category_budgets)) * 100.0
    else:
        budget_adherence_pct = 75.0 if overall_budget_amount is None else 100.0

    # 3. Leak monthly total from past 90 days
    ninety_days_ago = today - timedelta(days=90)
    expenses_90d, _ = await expense_repo.get_paginated(
        user_id=current_user.id,
        date_from=ninety_days_ago,
        date_to=today,
        page=1,
        page_size=250,
    )
    serialized_expenses = [
        {"title": e.title, "amount": e.amount, "expense_date": e.expense_date, "category_name": e.category.name if e.category else "Other"}
        for e in expenses_90d
    ]
    leak_data = await ai_service.analyze_leaks_and_subscriptions(
        serialized_expenses, total_monthly_income=total_income
    )
    leak_monthly = Decimal(str(leak_data.get("total_monthly_leak", "0.00")))

    # 4. Top spending category
    top_cat_name = category_breakdown[0][1] if category_breakdown else None

    return await ai_service.calculate_financial_health_score(
        total_income=total_income,
        total_spent=total_spent,
        overall_budget=overall_budget_amount,
        budget_adherence_pct=budget_adherence_pct,
        leak_monthly_total=leak_monthly,
        days_remaining_in_month=days_remaining,
        days_passed_in_month=days_passed,
        top_spending_category=top_cat_name,
    )


@router.get("/goals-runway", response_model=GoalRunwayAnalysisResponse)
async def get_goals_runway(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """
    Feature 7: Smart Goal Runway & Acceleration Intelligence.
    Aggregates all active user goals and projects completion timelines against live monthly surplus.
    """
    today = date.today()
    start_of_month = date(today.year, today.month, 1)
    _, total_days = calendar.monthrange(today.year, today.month)
    end_of_month = date(today.year, today.month, total_days)

    goal_repo = GoalRepository(db)
    income_repo = IncomeRepository(db)
    dashboard_repo = DashboardRepository(db)

    goals = await goal_repo.list_goals(current_user.id)
    total_income = await income_repo.get_total_for_period(current_user.id, start_of_month, end_of_month)
    total_spent, _ = await dashboard_repo.get_period_spending_and_count(current_user.id, start_of_month, end_of_month)
    monthly_surplus = max(Decimal("0.00"), total_income - total_spent)

    category_breakdown = await dashboard_repo.get_category_breakdown(
        current_user.id, start_of_month, end_of_month
    )
    top_categories = [
        {"category_name": name, "amount": amount}
        for _, name, amount in category_breakdown[:3]
    ]

    return await ai_service.analyze_goals_runway(
        goals=list(goals),
        monthly_surplus=monthly_surplus,
        top_discretionary_categories=top_categories,
    )




