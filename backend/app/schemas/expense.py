import uuid
from datetime import date, datetime
from decimal import Decimal
from typing import Optional
from pydantic import BaseModel, ConfigDict, Field, field_validator

from app.models.expense import PaymentMode
from app.schemas.category import CategoryResponse


class ExpenseBase(BaseModel):
    title: str = Field(..., min_length=1, max_length=50, description="Title of the expense")
    category_id: uuid.UUID = Field(..., description="UUID of the category")
    amount: Decimal = Field(..., gt=0, decimal_places=2, description="Expense amount in INR (must be > 0)")
    expense_date: date = Field(..., description="Date of expense (must be today or earlier)")
    notes: Optional[str] = Field(default=None, description="Optional notes or remarks")
    payment_mode: Optional[PaymentMode] = Field(default=None, description="Payment mode (Cash, Card, UPI, etc.)")

    @field_validator("expense_date")
    @classmethod
    def validate_not_future_date(cls, v: date) -> date:
        if v > date.today():
            raise ValueError("Expense date cannot be in the future")
        return v


class ExpenseCreate(ExpenseBase):
    pass


class ExpenseUpdate(BaseModel):
    title: Optional[str] = Field(default=None, min_length=1, max_length=50)
    category_id: Optional[uuid.UUID] = Field(default=None)
    amount: Optional[Decimal] = Field(default=None, gt=0, decimal_places=2)
    expense_date: Optional[date] = Field(default=None)
    notes: Optional[str] = Field(default=None)
    payment_mode: Optional[PaymentMode] = Field(default=None)

    @field_validator("expense_date")
    @classmethod
    def validate_not_future_date(cls, v: Optional[date]) -> Optional[date]:
        if v is not None and v > date.today():
            raise ValueError("Expense date cannot be in the future")
        return v


class DailyBudgetAlert(BaseModel):
    exceeded: bool
    limit_amount: Decimal
    total_spent: Decimal
    exceeded_amount: Decimal
    percentage_used: float
    message: str


class ExpenseResponse(ExpenseBase):
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    created_at: datetime
    updated_at: datetime
    category: Optional[CategoryResponse] = None
    daily_budget_alert: Optional[DailyBudgetAlert] = None


class ExpenseListResponse(BaseModel):
    items: list[ExpenseResponse]
    total: int
    page: int
    page_size: int
    total_pages: int
