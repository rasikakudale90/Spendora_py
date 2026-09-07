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


def _extract_rag_query_metadata(
    message: str,
    user_categories: list,
    today: date,
    start_of_month: date,
    end_of_month: date,
) -> dict:
    import re

    raw = message.strip()
    msg_lower = raw.lower()

    # 1. Temporal bounds extraction
    date_from = None
    date_to = None
    temporal_label = None

    if "today" in msg_lower:
        date_from = today
        date_to = today
        temporal_label = "today"
    elif "yesterday" in msg_lower:
        yesterday = today - timedelta(days=1)
        date_from = yesterday
        date_to = yesterday
        temporal_label = "yesterday"
    elif "this week" in msg_lower:
        start_of_week = today - timedelta(days=today.weekday())
        date_from = start_of_week
        date_to = today
        temporal_label = "this week"
    elif "last week" in msg_lower:
        prev_monday = today - timedelta(days=today.weekday() + 7)
        prev_sunday = today - timedelta(days=today.weekday() + 1)
        date_from = prev_monday
        date_to = prev_sunday
        temporal_label = "last week"
    elif "this month" in msg_lower:
        date_from = start_of_month
        date_to = end_of_month
        temporal_label = "this month"
    elif "last month" in msg_lower:
        last_month_end = start_of_month - timedelta(days=1)
        last_month_start = date(last_month_end.year, last_month_end.month, 1)
        date_from = last_month_start
        date_to = last_month_end
        temporal_label = "last month"
    elif any(k in msg_lower for k in ["last 30 days", "past 30 days", "past month"]):
        date_from = today - timedelta(days=30)
        date_to = today
        temporal_label = "past 30 days"
    elif any(k in msg_lower for k in ["last 90 days", "past 90 days", "3 months"]):
        date_from = today - timedelta(days=90)
        date_to = today
        temporal_label = "past 90 days"
    else:
        # Check explicit ISO date YYYY-MM-DD
        iso_match = re.search(r"\b(\d{4}-\d{2}-\d{2})\b", raw)
        if iso_match:
            try:
                parsed_d = date.fromisoformat(iso_match.group(1))
                date_from = parsed_d
                date_to = parsed_d
                temporal_label = str(parsed_d)
            except ValueError:
                pass

    # 2. Category matching
    matched_category = None
    for cat in user_categories:
        cat_name_lower = cat.name.lower()
        if (
            cat_name_lower in msg_lower
            or (len(cat_name_lower) > 4 and cat_name_lower[:-1] in msg_lower)
            or (cat_name_lower == "food & dining" and any(w in msg_lower for w in ["food", "dining", "restaurant", "lunch", "dinner", "breakfast", "cafe"]))
            or (cat_name_lower == "groceries" and any(w in msg_lower for w in ["grocery", "groceries", "supermarket", "mart"]))
            or (cat_name_lower == "transportation" and any(w in msg_lower for w in ["transport", "travel", "uber", "ola", "cab", "metro", "fuel", "petrol"]))
            or (cat_name_lower == "shopping" and any(w in msg_lower for w in ["shopping", "clothes", "mall", "clothing"]))
            or (cat_name_lower == "utilities" and any(w in msg_lower for w in ["utility", "utilities", "bills", "electricity", "recharge", "wifi"]))
            or (cat_name_lower == "entertainment" and any(w in msg_lower for w in ["entertainment", "movie", "cinema", "game", "gaming"]))
            or (cat_name_lower == "health" and any(w in msg_lower for w in ["health", "medical", "medicine", "doctor", "pharmacy"]))
        ):
            matched_category = {"id": cat.id, "name": cat.name}
            break

    # 3. Intent detection
    is_goal = any(w in msg_lower for w in ["goal", "goals", "target", "milestone", "runway", "save for", "emergency fund", "saving goal"])
    is_budget = any(w in msg_lower for w in ["budget", "budgets", "limit", "exceed", "over budget", "threshold", "allowance", "breach", "overspent", "deficit"])
    is_income = any(w in msg_lower for w in ["income", "salary", "freelance", "earned", "earning", "cash inflow", "paycheck", "credited", "bonus", "earnings", "source", "sources"])
    is_afford = any(w in msg_lower for w in ["can i afford", "should i buy", "worth buying", "getting a", "purchase a"])
    is_safe_spend = any(w in msg_lower for w in ["safe to spend", "daily limit", "burn rate", "safe spend", "daily burn", "burn velocity"])
    is_leak = any(w in msg_lower for w in ["leak", "subscription", "recurring", "audit", "micro-spend"])
    is_highest = any(w in msg_lower for w in ["highest", "biggest", "largest", "maximum expense", "most expensive"])

    # 4. Candidate search token extraction
    quoted = re.findall(r'["\']([^"\']+)["\']', raw)
    search_term = None
    if quoted:
        search_term = quoted[0].strip()
    elif not (is_goal or is_budget or is_income or is_safe_spend or is_leak):
        stop_words = {
            "what", "is", "are", "was", "were", "how", "much", "many", "did", "do", "does",
            "i", "me", "my", "we", "us", "you", "your", "spend", "spent", "spending",
            "pay", "paid", "paying", "buy", "bought", "buying", "on", "for", "at", "in",
            "to", "of", "the", "a", "an", "this", "that", "these", "those", "about",
            "show", "tell", "give", "check", "any", "all", "with", "from", "there",
            "have", "has", "had", "get", "got", "cost", "worth", "date", "time",
            "money", "expense", "expenses", "transaction", "transactions", "rupee",
            "rupees", "rs", "inr", "total", "can", "could", "would", "please",
            "today", "yesterday", "month", "year", "week", "day", "record", "records",
            "history", "list", "see", "find", "ever", "last", "first", "recent", "there",
            "active", "saving", "savings", "goal", "goals", "milestone", "milestones",
            "target", "targets", "budget", "budgets", "limit", "limits", "income",
            "incomes", "salary", "salaries", "earnings", "source", "sources"
        }
        clean_words = re.findall(r"\b[a-zA-Z0-9_-]{2,}\b", msg_lower)
        candidates = [
            w for w in clean_words
            if w not in stop_words and not w.isdigit()
        ]
        if matched_category:
            cat_words = set(re.findall(r"\b\w+\b", matched_category["name"].lower()))
            candidates = [w for w in candidates if w not in cat_words]

        if candidates:
            search_term = " ".join(candidates[:2])

    return {
        "search_term": search_term,
        "matched_category": matched_category,
        "date_from": date_from,
        "date_to": date_to,
        "temporal_label": temporal_label,
        "intents": {
            "is_goal": is_goal,
            "is_budget": is_budget,
            "is_income": is_income,
            "is_afford": is_afford,
            "is_safe_spend": is_safe_spend,
            "is_leak": is_leak,
            "is_highest": is_highest,
        }
    }


@router.post("/chat", response_model=FinancialChatResponse)
async def chat_with_financial_assistant(
    payload: FinancialChatRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """
    Feature 4 & RAG Engine: Natural Language Financial Assistant / Chatbot.
    Dynamically retrieves user-specific financial records (targeted transactions,
    matching category drill-downs, active budgets, savings goals, and income cash flows)
    to accurately ground both LLM responses and deterministic fallbacks.
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
    category_repo = CategoryRepository(db)
    goal_repo = GoalRepository(db)

    # 1. Fetch user categories for entity matching
    cat_pairs = await category_repo.get_all_with_counts(current_user.id)
    user_categories = [c for c, _ in cat_pairs]

    # 2. Extract query entities (search terms, categories, temporal dates, intents)
    meta = _extract_rag_query_metadata(
        payload.message, user_categories, today, start_of_month, end_of_month
    )

    # 3. Targeted Expense Search (RAG)
    matched_expenses_data = None
    if meta["search_term"]:
        search_res, total_search_count = await expense_repo.get_paginated(
            user_id=current_user.id,
            search=meta["search_term"],
            date_from=meta["date_from"],
            date_to=meta["date_to"],
            page_size=25,
        )
        # If 0 results and a date bound was present, search historical records as fallback
        if total_search_count == 0 and meta["date_from"]:
            search_res_all, total_all_count = await expense_repo.get_paginated(
                user_id=current_user.id,
                search=meta["search_term"],
                page_size=25,
            )
            if total_all_count > 0:
                search_res = search_res_all
                total_search_count = total_all_count

        sum_matched_amount = sum(e.amount for e in search_res)
        matched_expenses_data = {
            "search_term": meta["search_term"],
            "count": total_search_count,
            "total_amount": float(sum_matched_amount),
            "items": [
                {
                    "title": e.title,
                    "amount": float(e.amount),
                    "expense_date": str(e.expense_date),
                    "category_name": e.category.name if e.category else "Uncategorized",
                    "payment_mode": e.payment_mode,
                    "notes": e.notes,
                }
                for e in search_res
            ],
        }

    # 4. Targeted Category Drill-Down (RAG)
    matched_category_data = None
    if meta["matched_category"]:
        cat_id = meta["matched_category"]["id"]
        cat_expenses, cat_count = await expense_repo.get_paginated(
            user_id=current_user.id,
            category_id=cat_id,
            date_from=meta["date_from"] or start_of_month,
            date_to=meta["date_to"] or end_of_month,
            page_size=15,
        )
        cat_spent_total = sum(e.amount for e in cat_expenses)
        cat_budget_obj = await budget_repo.get_category_budget(current_user.id, cat_id, start_of_month)
        cat_budget_amt = float(cat_budget_obj.amount) if cat_budget_obj else None
        cat_budget_spent = float(cat_budget_obj.spent) if cat_budget_obj else float(cat_spent_total)

        matched_category_data = {
            "name": meta["matched_category"]["name"],
            "count": cat_count,
            "total_spent": float(cat_spent_total),
            "budget_amount": cat_budget_amt,
            "budget_spent": cat_budget_spent,
            "items": [
                {
                    "title": e.title,
                    "amount": float(e.amount),
                    "expense_date": str(e.expense_date),
                }
                for e in cat_expenses[:10]
            ],
        }

    # 5. Highest Single Expense
    high_exp_list, _ = await expense_repo.get_paginated(
        user_id=current_user.id,
        date_from=meta["date_from"] or start_of_month,
        date_to=meta["date_to"] or end_of_month,
        sort_by="amount",
        sort_order="desc",
        page_size=1,
    )
    highest_expense = (
        {
            "title": high_exp_list[0].title,
            "amount": float(high_exp_list[0].amount),
            "expense_date": str(high_exp_list[0].expense_date),
            "category_name": high_exp_list[0].category.name if high_exp_list[0].category else "Other",
        }
        if high_exp_list
        else None
    )

    # 6. Active Budgets & Breach Detection
    user_budgets = await budget_repo.get_by_period(current_user.id, start_of_month, "monthly")
    active_budgets_list = []
    breached_budgets = []
    near_limit_budgets = []
    for b in user_budgets:
        cat_name = b.category.name if b.category else "Overall Budget"
        b_amt = float(b.amount)
        b_spent = float(b.spent)
        pct = round((b_spent / b_amt * 100), 1) if b_amt > 0 else 0.0
        b_item = {
            "category": cat_name,
            "amount": b_amt,
            "spent": b_spent,
            "utilization_pct": pct,
            "status": b.status,
        }
        active_budgets_list.append(b_item)
        if b_spent > b_amt:
            breached_budgets.append(b_item)
        elif b_spent >= (b_amt * 0.8):
            near_limit_budgets.append(b_item)

    # 7. Goals Status
    user_goals = await goal_repo.list_goals(current_user.id)
    goals_list = []
    for g in user_goals:
        target = float(g.target_amount)
        current = float(g.current_amount or Decimal("0.00"))
        pct = round((current / target * 100), 1) if target > 0 else 0.0
        rem = max(0.0, target - current)
        goals_list.append({
            "id": str(g.id),
            "name": g.name,
            "target_amount": target,
            "current_amount": current,
            "progress_pct": pct,
            "remaining": rem,
            "target_date": str(g.target_date) if g.target_date else None,
            "status": g.status,
            "category": g.category,
        })

    # 8. Incomes & Cash Flow
    incomes_seq, _ = await income_repo.get_paginated(
        user_id=current_user.id,
        date_from=meta["date_from"] or start_of_month,
        date_to=meta["date_to"] or end_of_month,
        page_size=20,
    )
    income_sources = {}
    incomes_serialized = []
    for inc in incomes_seq:
        src = inc.source or "General"
        amt = float(inc.amount)
        income_sources[src] = income_sources.get(src, 0.0) + amt
        incomes_serialized.append({
            "title": inc.title,
            "amount": amt,
            "income_date": str(inc.income_date),
            "source": src,
        })

    # 9. Total spent & income this month (Macro telemetry)
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

    # 10. Overall budget and safe daily spend
    overall_budget_obj = await budget_repo.get_overall(current_user.id, start_of_month)
    overall_budget = Decimal(str(overall_budget_obj.amount)) if overall_budget_obj else None
    remaining_budget = max(Decimal("0.00"), (overall_budget - total_spent)) if overall_budget else None
    effective_buffer = remaining_budget if remaining_budget is not None else max(Decimal("0.00"), net_savings)
    daily_safe_spend = (effective_buffer / Decimal(str(days_remaining))).quantize(Decimal("0.01"))

    # 11. Top spending categories this month
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

    # 12. Recent 5 expenses
    recent_exp_objs = await dashboard_repo.get_recent_expenses(current_user.id, limit=5)
    recent_expenses = []
    for exp in recent_exp_objs:
        recent_expenses.append({
            "title": exp.title,
            "amount": float(exp.amount),
            "expense_date": str(exp.expense_date),
            "category_name": exp.category.name if exp.category else "Uncategorized",
        })

    # Build Augmented RAG Context Dictionary
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
        "highest_expense": highest_expense,
        "query_metadata": {
            "search_term": meta["search_term"],
            "temporal_label": meta["temporal_label"],
            "intents": meta["intents"],
        },
        "matched_expenses": matched_expenses_data,
        "matched_category": matched_category_data,
        "active_budgets": active_budgets_list,
        "breached_budgets": breached_budgets,
        "near_limit_budgets": near_limit_budgets,
        "goals": goals_list,
        "incomes_list": incomes_serialized,
        "income_sources": income_sources,
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




