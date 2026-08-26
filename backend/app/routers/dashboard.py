from datetime import date
from typing import Optional
from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
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


@router.get("/summary", response_model=DashboardSummaryResponse)
async def get_dashboard_summary(
    period_month: Optional[date] = Query(
        default=None,
        description="Month for the dashboard summary (defaults to current month)",
    ),
    db: AsyncSession = Depends(get_db),
):
    """Total spend, remaining budget, and status indicator (FR-17, FR-21, FR-27, FR-28)."""
    target_month = period_month or date.today()
    service = DashboardService(db)
    return await service.get_summary(target_month)


@router.get("/recent-expenses", response_model=list[ExpenseResponse])
async def get_recent_expenses(
    limit: int = Query(default=5, ge=1, le=50, description="Number of recent expenses"),
    db: AsyncSession = Depends(get_db),
):
    """Recent N expenses (FR-18)."""
    service = DashboardService(db)
    return await service.get_recent_expenses(limit=limit)


@router.get("/category-breakdown", response_model=list[CategoryBreakdownItem])
async def get_category_breakdown(
    period_month: Optional[date] = Query(
        default=None,
        description="Month for category breakdown (defaults to current month)",
    ),
    db: AsyncSession = Depends(get_db),
):
    """Pie / donut chart data per category with percentages (FR-19)."""
    target_month = period_month or date.today()
    service = DashboardService(db)
    return await service.get_category_breakdown(target_month)


@router.get("/trend", response_model=list[TrendItem])
async def get_trend(
    period_month: Optional[date] = Query(
        default=None,
        description="Month for spending trend (defaults to current month)",
    ),
    db: AsyncSession = Depends(get_db),
):
    """Bar / line chart data over time (FR-20)."""
    target_month = period_month or date.today()
    service = DashboardService(db)
    return await service.get_trend(target_month)


@router.get("/comparison", response_model=MonthComparisonResponse)
async def get_comparison(
    period_month: Optional[date] = Query(
        default=None,
        description="Month to compare against previous month (defaults to current month)",
    ),
    db: AsyncSession = Depends(get_db),
):
    """Month-over-month % change comparison (FR-23)."""
    target_month = period_month or date.today()
    service = DashboardService(db)
    return await service.get_comparison(target_month)


@router.get("/top-categories", response_model=list[TopCategoryItem])
async def get_top_categories(
    period_month: Optional[date] = Query(
        default=None,
        description="Month for top categories (defaults to current month)",
    ),
    limit: int = Query(default=5, ge=1, le=20, description="Max top categories to return"),
    db: AsyncSession = Depends(get_db),
):
    """Ranked top spending categories (FR-24)."""
    target_month = period_month or date.today()
    service = DashboardService(db)
    return await service.get_top_categories(target_month, limit=limit)


@router.get("/stats", response_model=DashboardStatsResponse)
async def get_stats(
    period_month: Optional[date] = Query(
        default=None,
        description="Month for stats (defaults to current month)",
    ),
    db: AsyncSession = Depends(get_db),
):
    """Average daily/weekly spend, highest expense, and transaction count (FR-25)."""
    target_month = period_month or date.today()
    service = DashboardService(db)
    return await service.get_stats(target_month)
