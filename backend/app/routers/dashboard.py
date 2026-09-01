from datetime import date
from typing import Optional
from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.core.dependencies import get_current_user
from app.models.user import User
from app.schemas.dashboard import (
    CategoryBreakdownItem,
    DashboardStatsResponse,
    DashboardSummaryResponse,
    MonthComparisonResponse,
    TopCategoryItem,
    TrendItem,
)
from app.schemas.expense import ExpenseResponse
from app.services.dashboard_service import DashboardService

router = APIRouter(prefix="/dashboard", tags=["Dashboard"])


def _parse_period_month(val: Optional[str] = None) -> date:
    if not val:
        return date.today()
    val = val.strip()
    try:
        if len(val) == 7:  # "YYYY-MM"
            parts = val.split("-")
            return date(int(parts[0]), int(parts[1]), 1)
        elif len(val) == 10:  # "YYYY-MM-DD"
            return date.fromisoformat(val)
    except Exception:
        pass
    return date.today()


@router.get("/summary", response_model=DashboardSummaryResponse)
async def get_dashboard_summary(
    current_user: User = Depends(get_current_user),
    period_month: Optional[str] = Query(
        default=None,
        description="Month for dashboard summary in YYYY-MM or YYYY-MM-DD format",
    ),
    db: AsyncSession = Depends(get_db),
):
    """Total spend, remaining budget, net cash flow, and status indicator for the authenticated user."""
    target_month = _parse_period_month(period_month)
    service = DashboardService(db)
    return await service.get_summary(current_user.id, target_month)


@router.get("/recent-expenses", response_model=list[ExpenseResponse])
async def get_recent_expenses(
    current_user: User = Depends(get_current_user),
    limit: int = Query(default=5, ge=1, le=50, description="Number of recent expenses"),
    db: AsyncSession = Depends(get_db),
):
    """Recent N expenses for the authenticated user."""
    service = DashboardService(db)
    return await service.get_recent_expenses(current_user.id, limit=limit)


@router.get("/category-breakdown", response_model=list[CategoryBreakdownItem])
async def get_category_breakdown(
    current_user: User = Depends(get_current_user),
    period_month: Optional[str] = Query(
        default=None,
        description="Month for category breakdown in YYYY-MM or YYYY-MM-DD format",
    ),
    db: AsyncSession = Depends(get_db),
):
    """Pie / donut chart data per category with percentages for the authenticated user."""
    target_month = _parse_period_month(period_month)
    service = DashboardService(db)
    return await service.get_category_breakdown(current_user.id, target_month)


@router.get("/trend", response_model=list[TrendItem])
async def get_trend(
    current_user: User = Depends(get_current_user),
    period_month: Optional[str] = Query(
        default=None,
        description="Month for spending trend in YYYY-MM or YYYY-MM-DD format",
    ),
    db: AsyncSession = Depends(get_db),
):
    """Bar / line chart data over time for the authenticated user."""
    target_month = _parse_period_month(period_month)
    service = DashboardService(db)
    return await service.get_trend(current_user.id, target_month)


@router.get("/comparison", response_model=MonthComparisonResponse)
async def get_comparison(
    current_user: User = Depends(get_current_user),
    period_month: Optional[str] = Query(
        default=None,
        description="Month to compare against previous month in YYYY-MM or YYYY-MM-DD format",
    ),
    db: AsyncSession = Depends(get_db),
):
    """Month-over-month % change comparison for the authenticated user."""
    target_month = _parse_period_month(period_month)
    service = DashboardService(db)
    return await service.get_comparison(current_user.id, target_month)


@router.get("/top-categories", response_model=list[TopCategoryItem])
async def get_top_categories(
    current_user: User = Depends(get_current_user),
    period_month: Optional[str] = Query(
        default=None,
        description="Month for top categories in YYYY-MM or YYYY-MM-DD format",
    ),
    limit: int = Query(default=5, ge=1, le=20, description="Max top categories to return"),
    db: AsyncSession = Depends(get_db),
):
    """Ranked top spending categories for the authenticated user."""
    target_month = _parse_period_month(period_month)
    service = DashboardService(db)
    return await service.get_top_categories(current_user.id, target_month, limit=limit)


@router.get("/stats", response_model=DashboardStatsResponse)
async def get_stats(
    current_user: User = Depends(get_current_user),
    period_month: Optional[str] = Query(
        default=None,
        description="Month for stats in YYYY-MM or YYYY-MM-DD format",
    ),
    db: AsyncSession = Depends(get_db),
):
    """Average daily/weekly spend, highest expense, and transaction count for the authenticated user."""
    target_month = _parse_period_month(period_month)
    service = DashboardService(db)
    return await service.get_stats(current_user.id, target_month)
