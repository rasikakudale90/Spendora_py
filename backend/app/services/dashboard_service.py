import calendar
from datetime import date, timedelta
from decimal import Decimal
from typing import Optional
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.repositories.budget_repository import BudgetRepository
from app.repositories.dashboard_repository import DashboardRepository
from app.repositories.income_repository import IncomeRepository
from app.schemas.dashboard import (
    CategoryBreakdownItem,
    DashboardStatsResponse,
    DashboardSummaryResponse,
    MonthComparisonResponse,
    TopCategoryItem,
    TrendItem,
)
from app.schemas.expense import ExpenseResponse


class DashboardService:
    def __init__(self, session: AsyncSession):
        self.session = session
        self.repo = DashboardRepository(session)
        self.budget_repo = BudgetRepository(session)
        self.income_repo = IncomeRepository(session)

    def _get_month_bounds(self, period_month: date) -> tuple[date, date]:
        start_date = date(period_month.year, period_month.month, 1)
        _, last_day = calendar.monthrange(period_month.year, period_month.month)
        end_date = date(period_month.year, period_month.month, last_day)
        return start_date, end_date

    def _get_previous_month(self, period_month: date) -> date:
        first_of_month = date(period_month.year, period_month.month, 1)
        last_of_prev = first_of_month - timedelta(days=1)
        return date(last_of_prev.year, last_of_prev.month, 1)

    async def get_summary(self, period_month: date) -> DashboardSummaryResponse:
        normalized_period = date(period_month.year, period_month.month, 1)
        start_date, end_date = self._get_month_bounds(normalized_period)

        total_spent, count = await self.repo.get_period_spending_and_count(start_date, end_date)
        total_income = await self.income_repo.get_total_for_period(start_date, end_date)
        net_savings = total_income - total_spent
        savings_rate = round(float((net_savings / total_income) * 100), 2) if total_income > 0 else 0.0

        overall_budget = await self.budget_repo.get_overall(normalized_period)

        if overall_budget:
            total_budget = overall_budget.amount
            remaining = max(Decimal("0.00"), total_budget - total_spent)
            percentage = float((total_spent / total_budget) * 100) if total_budget > 0 else 0.0
            threshold = Decimal(str(settings.BUDGET_NEAR_LIMIT_THRESHOLD)) * total_budget

            if total_spent > total_budget:
                status_val = "over_budget"
            elif total_spent >= threshold:
                status_val = "near_limit"
            else:
                status_val = "on_track"
        else:
            total_budget = None
            remaining = None
            percentage = 0.0
            status_val = "no_budget"

        return DashboardSummaryResponse(
            period_month=normalized_period.strftime("%Y-%m"),
            total_spent=total_spent,
            total_budget=total_budget,
            remaining_budget=remaining,
            percentage_used=round(percentage, 2),
            status=status_val,
            expense_count=count,
            total_income=total_income,
            net_savings=net_savings,
            savings_rate=savings_rate,
        )

    async def get_recent_expenses(self, limit: int = 5) -> list[ExpenseResponse]:
        expenses = await self.repo.get_recent_expenses(limit=limit)
        return [ExpenseResponse.model_validate(e) for e in expenses]

    async def get_category_breakdown(self, period_month: date) -> list[CategoryBreakdownItem]:
        normalized_period = date(period_month.year, period_month.month, 1)
        start_date, end_date = self._get_month_bounds(normalized_period)

        rows = await self.repo.get_category_breakdown(start_date, end_date)
        total_spent = sum((amount for _, _, amount in rows), Decimal("0.00"))

        result = []
        for cat_id, cat_name, amount in rows:
            pct = float((amount / total_spent) * 100) if total_spent > 0 else 0.0
            result.append(
                CategoryBreakdownItem(
                    category_id=cat_id,
                    category_name=cat_name,
                    amount=amount,
                    percentage=round(pct, 2),
                )
            )
        return result

    async def get_trend(self, period_month: date) -> list[TrendItem]:
        normalized_period = date(period_month.year, period_month.month, 1)
        start_date, end_date = self._get_month_bounds(normalized_period)

        rows = await self.repo.get_daily_trend(start_date, end_date)
        return [
            TrendItem(
                label=d.strftime("%Y-%m-%d"),
                amount=amount,
                expense_count=cnt,
            )
            for d, amount, cnt in rows
        ]

    async def get_comparison(self, period_month: date) -> MonthComparisonResponse:
        current_period = date(period_month.year, period_month.month, 1)
        prev_period = self._get_previous_month(current_period)

        curr_start, curr_end = self._get_month_bounds(current_period)
        prev_start, prev_end = self._get_month_bounds(prev_period)

        curr_spend, _ = await self.repo.get_period_spending_and_count(curr_start, curr_end)
        prev_spend, _ = await self.repo.get_period_spending_and_count(prev_start, prev_end)

        diff = curr_spend - prev_spend
        if prev_spend > 0:
            pct_change = float((diff / prev_spend) * 100)
        else:
            pct_change = 100.0 if curr_spend > 0 else 0.0

        if diff > 0:
            trend = "increased"
        elif diff < 0:
            trend = "decreased"
        else:
            trend = "unchanged"

        return MonthComparisonResponse(
            current_month_spend=curr_spend,
            previous_month_spend=prev_spend,
            difference_amount=diff,
            percentage_change=round(pct_change, 2),
            trend=trend,
        )

    async def get_top_categories(
        self, period_month: date, limit: int = 5
    ) -> list[TopCategoryItem]:
        normalized_period = date(period_month.year, period_month.month, 1)
        start_date, end_date = self._get_month_bounds(normalized_period)

        rows = await self.repo.get_category_breakdown(start_date, end_date)
        total_spent = sum((amount for _, _, amount in rows), Decimal("0.00"))

        result = []
        for rank, (cat_id, cat_name, amount) in enumerate(rows[:limit], start=1):
            pct = float((amount / total_spent) * 100) if total_spent > 0 else 0.0
            result.append(
                TopCategoryItem(
                    rank=rank,
                    category_id=cat_id,
                    category_name=cat_name,
                    amount=amount,
                    percentage=round(pct, 2),
                )
            )
        return result

    async def get_stats(self, period_month: date) -> DashboardStatsResponse:
        normalized_period = date(period_month.year, period_month.month, 1)
        start_date, end_date = self._get_month_bounds(normalized_period)

        total_spent, count = await self.repo.get_period_spending_and_count(start_date, end_date)
        highest = await self.repo.get_highest_expense(start_date, end_date)

        # Days in the month
        days_in_month = (end_date - start_date).days + 1
        avg_daily = (total_spent / Decimal(str(days_in_month))) if days_in_month > 0 else Decimal("0.00")
        avg_weekly = avg_daily * Decimal("7.0")

        highest_title, highest_amount = (highest[0], highest[1]) if highest else (None, None)

        return DashboardStatsResponse(
            period_month=normalized_period.strftime("%Y-%m"),
            avg_daily_spend=round(avg_daily, 2),
            avg_weekly_spend=round(avg_weekly, 2),
            highest_expense_amount=highest_amount,
            highest_expense_title=highest_title,
            total_expense_count=count,
        )
