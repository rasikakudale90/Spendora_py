import uuid
from datetime import date, datetime
from decimal import Decimal
from typing import Optional
from pydantic import BaseModel, ConfigDict, Field, field_validator


class IncomeBase(BaseModel):
    title: str = Field(..., min_length=1, max_length=100, description="Title/description of income")
    amount: Decimal = Field(..., gt=0, decimal_places=2, description="Income amount in INR (must be > 0)")
    income_date: date = Field(..., description="Date of income receipt (cannot be future date)")
    source: str = Field(default="Salary", max_length=50, description="Income source (Salary, Freelance, Investment, etc.)")
    payment_mode: Optional[str] = Field(default=None, max_length=30, description="Payment mode (Bank Transfer, Cash, UPI, etc.)")
    notes: Optional[str] = Field(default=None, description="Optional notes or remarks")

    @field_validator("income_date")
    @classmethod
    def validate_not_future_date(cls, v: date) -> date:
        if v > date.today():
            raise ValueError("Income date cannot be in the future")
        return v


class IncomeCreate(IncomeBase):
    pass


class IncomeUpdate(BaseModel):
    title: Optional[str] = Field(default=None, min_length=1, max_length=100)
    amount: Optional[Decimal] = Field(default=None, gt=0, decimal_places=2)
    income_date: Optional[date] = Field(default=None)
    source: Optional[str] = Field(default=None, max_length=50)
    payment_mode: Optional[str] = Field(default=None, max_length=30)
    notes: Optional[str] = Field(default=None)

    @field_validator("income_date")
    @classmethod
    def validate_not_future_date(cls, v: Optional[date]) -> Optional[date]:
        if v is not None and v > date.today():
            raise ValueError("Income date cannot be in the future")
        return v


class IncomeResponse(IncomeBase):
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    created_at: datetime
    updated_at: datetime


class IncomeListResponse(BaseModel):
    items: list[IncomeResponse]
    total: int
    page: int
    page_size: int
    total_pages: int


class SourceBreakdownItem(BaseModel):
    source: str
    total_amount: Decimal
    percentage: float
    count: int


class IncomeSummaryResponse(BaseModel):
    period_month: str
    total_income: Decimal
    income_count: int
    breakdown_by_source: list[SourceBreakdownItem] = []
