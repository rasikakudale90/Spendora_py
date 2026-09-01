import uuid
from datetime import date
from typing import Literal, Optional
from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.core.dependencies import get_current_user
from app.models.user import User
from app.schemas.budget import (
    BudgetCreate,
    BudgetListResponse,
    BudgetResponse,
    BudgetUpdate,
)
from app.services.budget_service import BudgetService

router = APIRouter(prefix="/budgets", tags=["Budgets"])


@router.get("", response_model=BudgetListResponse)
async def get_budgets(
    current_user: User = Depends(get_current_user),
    period_date: Optional[date] = Query(
        default=None,
        description="Target reference date for the budget period (defaults to today)",
    ),
    period_month: Optional[date] = Query(
        default=None,
        description="Legacy month parameter for backwards compatibility",
    ),
    period_type: Literal["daily", "weekly", "monthly", "yearly"] = Query(
        default="monthly",
        description="Budget period type (daily, weekly, monthly, or yearly)",
    ),
    db: AsyncSession = Depends(get_db),
):
    """Retrieve overall and category budgets for the authenticated user with live spent and remaining balances."""
    target_date = period_date or period_month or date.today()
    service = BudgetService(db)
    return await service.get_budgets(current_user.id, target_date, period_type=period_type)


@router.post("", response_model=BudgetResponse)
async def set_budget(
    data: BudgetCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Create or update a budget goal for the authenticated user."""
    service = BudgetService(db)
    return await service.set_budget(current_user.id, data)


@router.patch("/{budget_id}", response_model=BudgetResponse)
async def update_budget(
    budget_id: uuid.UUID,
    data: BudgetUpdate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Update an existing budget amount owned by the authenticated user."""
    service = BudgetService(db)
    return await service.update_budget(current_user.id, budget_id, data)


@router.delete("/{budget_id}")
async def delete_budget(
    budget_id: uuid.UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Delete a budget goal owned by the authenticated user."""
    service = BudgetService(db)
    await service.delete_budget(current_user.id, budget_id)
    return {"message": "Budget deleted successfully"}
