import calendar
from datetime import date
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
from app.repositories.income_repository import IncomeRepository
from app.schemas.ai import (
    PurchaseSimulationRequest,
    PurchaseSimulationResponse,
    LeakAnalysisResponse,
    SafeToSpendResponse,
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


