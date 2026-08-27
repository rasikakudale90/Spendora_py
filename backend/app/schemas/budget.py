import calendar
import uuid
from datetime import date, datetime, timedelta
from decimal import Decimal
from typing import Literal, Optional
from pydantic import BaseModel, ConfigDict, Field, model_validator


def compute_period_bounds(target_date: date, period_type: str = "monthly") -> tuple[date, date]:
    """Compute (start_date, end_date) for a given date and period_type."""
    if period_type == "weekly":
        start_date = target_date - timedelta(days=target_date.weekday())  # Monday
        end_date = start_date + timedelta(days=6)  # Sunday
    elif period_type == "yearly":
        start_date = date(target_date.year, 1, 1)
        end_date = date(target_date.year, 12, 31)
    else:  # monthly
        start_date = date(target_date.year, target_date.month, 1)
        _, last_day = calendar.monthrange(target_date.year, target_date.month)
        end_date = date(target_date.year, target_date.month, last_day)
    return start_date, end_date


class BudgetBase(BaseModel):
    scope: Literal["overall", "category"] = Field(..., description="Budget scope ('overall' or 'category')")
    category_id: Optional[uuid.UUID] = Field(default=None, description="Category UUID (required if scope is 'category')")
    amount: Decimal = Field(..., gt=0, decimal_places=2, description="Allocated budget amount in INR")
    period_type: Literal["weekly", "monthly", "yearly"] = Field(default="monthly", description="Budget period type")
    period_start: Optional[date] = Field(default=None, description="Start date of the budget period")
    period_end: Optional[date] = Field(default=None, description="End date of the budget period")
    period_month: Optional[date] = Field(default=None, description="Reference month/date (legacy compatibility)")

    @model_validator(mode="after")
    def validate_budget_data(self) -> "BudgetBase":
        if self.scope == "category" and not self.category_id:
            raise ValueError("category_id is required when scope is 'category'")
        if self.scope == "overall" and self.category_id is not None:
            raise ValueError("category_id must be null when scope is 'overall'")
        
        ref_date = self.period_start or self.period_month or date.today()
        start, end = compute_period_bounds(ref_date, self.period_type)
        self.period_start = start
        self.period_end = end
        self.period_month = date(start.year, start.month, 1)
        return self


class BudgetCreate(BudgetBase):
    pass


class BudgetResponse(BudgetBase):
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    created_at: datetime
    updated_at: datetime
    category_name: Optional[str] = None
    spent: Decimal = Field(default=Decimal("0.00"), description="Total spent in this period")
    remaining: Decimal = Field(default=Decimal("0.00"), description="Remaining budget amount")
    percentage_used: float = Field(default=0.0, description="Percentage of budget consumed")
    status: Literal["on_track", "near_limit", "over_budget"] = Field(default="on_track")


class BudgetListResponse(BaseModel):
    period_type: Literal["weekly", "monthly", "yearly"] = "monthly"
    period_start: date
    period_end: date
    overall_budget: Optional[BudgetResponse] = None
    category_budgets: list[BudgetResponse] = []
