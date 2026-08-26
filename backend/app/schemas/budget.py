import uuid
from datetime import date, datetime
from decimal import Decimal
from typing import Literal, Optional
from pydantic import BaseModel, ConfigDict, Field, model_validator


class BudgetBase(BaseModel):
    scope: Literal["overall", "category"] = Field(..., description="Budget scope ('overall' or 'category')")
    category_id: Optional[uuid.UUID] = Field(default=None, description="Category UUID (required if scope is 'category')")
    amount: Decimal = Field(..., gt=0, decimal_places=2, description="Allocated budget amount in INR")
    period_month: date = Field(..., description="Budget month (stored as first of the month YYYY-MM-01)")

    @model_validator(mode="after")
    def validate_category_and_scope(self) -> "BudgetBase":
        if self.scope == "category" and not self.category_id:
            raise ValueError("category_id is required when scope is 'category'")
        if self.scope == "overall" and self.category_id is not None:
            raise ValueError("category_id must be null when scope is 'overall'")
        # Normalize period_month to the 1st of the month
        self.period_month = date(self.period_month.year, self.period_month.month, 1)
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
    overall_budget: Optional[BudgetResponse] = None
    category_budgets: list[BudgetResponse] = []
