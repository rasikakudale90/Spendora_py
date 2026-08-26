from datetime import date
from typing import Optional
from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.schemas.budget import (
    BudgetCreate,
    BudgetListResponse,
    BudgetResponse,
)
from app.services.budget_service import BudgetService

router = APIRouter(prefix="/budgets", tags=["Budgets"])


@router.get("", response_model=BudgetListResponse)
async def get_budgets(
    period_month: Optional[date] = Query(
        default=None,
        description="Month for the budget (defaults to current month YYYY-MM-01)",
    ),
    db: AsyncSession = Depends(get_db),
):
    """Retrieve overall and category budgets with live spent and remaining balances (FR-26–28)."""
    target_month = period_month or date.today()
    service = BudgetService(db)
    return await service.get_budgets(target_month)


@router.post("", response_model=BudgetResponse)
async def set_budget(
    data: BudgetCreate, db: AsyncSession = Depends(get_db)
):
    """Create or update a budget goal for a specific month (FR-26)."""
    service = BudgetService(db)
    return await service.set_budget(data)
