import uuid
from decimal import Decimal
from typing import Literal, Optional
from pydantic import BaseModel, Field

from app.schemas.expense import ExpenseResponse


class DashboardSummaryResponse(BaseModel):
    period_month: str
    total_spent: Decimal
    total_budget: Optional[Decimal] = None
    remaining_budget: Optional[Decimal] = None
    percentage_used: float = 0.0
    status: Literal["on_track", "near_limit", "over_budget", "no_budget"] = "no_budget"
    expense_count: int = 0
    total_income: Decimal = Decimal("0.00")
    net_savings: Decimal = Decimal("0.00")
    savings_rate: float = 0.0


class CategoryBreakdownItem(BaseModel):
    category_id: uuid.UUID
    category_name: str
    amount: Decimal
    percentage: float


class TrendItem(BaseModel):
    label: str  # e.g., "2026-08-01" or "Aug 2026"
    amount: Decimal
    expense_count: int


class MonthComparisonResponse(BaseModel):
    current_month_spend: Decimal
    previous_month_spend: Decimal
    difference_amount: Decimal
    percentage_change: float
    trend: Literal["increased", "decreased", "unchanged"]


class TopCategoryItem(BaseModel):
    rank: int
    category_id: uuid.UUID
    category_name: str
    amount: Decimal
    percentage: float


class DashboardStatsResponse(BaseModel):
    period_month: str
    avg_daily_spend: Decimal
    avg_weekly_spend: Decimal
    highest_expense_amount: Optional[Decimal] = None
    highest_expense_title: Optional[str] = None
    total_expense_count: int
