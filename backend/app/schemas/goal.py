from __future__ import annotations

from datetime import date, datetime
from decimal import Decimal
from typing import List, Literal, Optional
from uuid import UUID
from pydantic import BaseModel, ConfigDict, Field, model_validator


class GoalRunwayForecast(BaseModel):
    required_monthly_savings: Decimal = Field(..., description="Monthly allocation required to hit target date")
    projected_completion_date: Optional[date] = Field(None, description="Projected completion date based on user's net cash flow")
    months_remaining: Optional[float] = Field(None, description="Months remaining until target date or completion")
    days_remaining: Optional[int] = Field(None, description="Days remaining until target date")
    pacing_status: Literal["on_track", "ahead", "at_risk", "behind", "completed", "no_deadline"] = Field(
        ..., description="Current pacing status"
    )
    pacing_message: str = Field(..., description="Human-friendly explanation of pacing")
    speedup_suggestion: Optional[str] = Field(None, description="Actionable AI suggestion to accelerate goal")


class GoalBase(BaseModel):
    name: str = Field(..., min_length=1, max_length=100, description="Name or title of the financial goal")
    target_amount: Decimal = Field(..., gt=0, description="Target savings amount in INR")
    current_amount: Decimal = Field(default=Decimal("0.00"), ge=0, description="Initial saved amount in INR")
    target_date: Optional[date] = Field(None, description="Optional target deadline date")
    category: str = Field(default="Savings", max_length=50, description="Goal category icon/label")
    color: str = Field(default="emerald", max_length=30, description="Theme accent color")
    notes: Optional[str] = Field(None, max_length=1000, description="Optional personal notes")


class GoalCreate(GoalBase):
    pass


class GoalUpdate(BaseModel):
    name: Optional[str] = Field(None, min_length=1, max_length=100)
    target_amount: Optional[Decimal] = Field(None, gt=0)
    current_amount: Optional[Decimal] = Field(None, ge=0)
    target_date: Optional[date] = None
    category: Optional[str] = Field(None, max_length=50)
    color: Optional[str] = Field(None, max_length=30)
    status: Optional[Literal["active", "completed", "paused"]] = None
    notes: Optional[str] = Field(None, max_length=1000)


class GoalContributeRequest(BaseModel):
    amount: Decimal = Field(..., gt=0, description="Amount to deposit or withdraw in INR")
    action: Literal["deposit", "withdraw"] = Field(default="deposit", description="Contribution action")
    notes: Optional[str] = Field(None, max_length=255, description="Optional note for this transaction")


class GoalResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    user_id: UUID
    name: str
    target_amount: Decimal
    current_amount: Decimal
    remaining_amount: Decimal
    progress_percentage: float
    target_date: Optional[date] = None
    category: str
    color: str
    status: str
    notes: Optional[str] = None
    is_completed: bool
    created_at: datetime
    updated_at: datetime
    runway: Optional[GoalRunwayForecast] = None

    @classmethod
    def from_orm_with_computed(
        cls,
        goal: Any,
        runway: Optional[GoalRunwayForecast] = None,
    ) -> GoalResponse:
        current = Decimal(str(goal.current_amount))
        target = Decimal(str(goal.target_amount))
        remaining = max(Decimal("0.00"), target - current)
        progress = float(min(Decimal("100.00"), (current / target * Decimal("100.00")))) if target > 0 else 0.0
        is_completed = goal.status == "completed" or current >= target

        return cls(
            id=goal.id,
            user_id=goal.user_id,
            name=goal.name,
            target_amount=target,
            current_amount=current,
            remaining_amount=remaining,
            progress_percentage=round(progress, 1),
            target_date=goal.target_date,
            category=goal.category,
            color=goal.color,
            status="completed" if is_completed else goal.status,
            notes=goal.notes,
            is_completed=is_completed,
            created_at=goal.created_at,
            updated_at=goal.updated_at,
            runway=runway,
        )


class GoalListResponse(BaseModel):
    items: List[GoalResponse]
    total_count: int
    total_target_amount: Decimal
    total_saved_amount: Decimal
    total_remaining_amount: Decimal
    overall_progress_percentage: float
