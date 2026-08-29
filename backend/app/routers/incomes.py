import uuid
from datetime import date
from decimal import Decimal
from typing import Optional
from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.schemas.income import (
    IncomeCreate,
    IncomeListResponse,
    IncomeResponse,
    IncomeSummaryResponse,
    IncomeUpdate,
)
from app.services.income_service import IncomeService

router = APIRouter(prefix="/incomes", tags=["Incomes"])


@router.get("", response_model=IncomeListResponse)
async def list_incomes(
    search: Optional[str] = Query(default=None, description="Search in title and notes"),
    date_from: Optional[date] = Query(default=None, description="Filter from date (YYYY-MM-DD)"),
    date_to: Optional[date] = Query(default=None, description="Filter to date (YYYY-MM-DD)"),
    source: Optional[str] = Query(default=None, description="Filter by income source"),
    min_amount: Optional[Decimal] = Query(default=None, ge=0, description="Minimum amount filter"),
    max_amount: Optional[Decimal] = Query(default=None, ge=0, description="Maximum amount filter"),
    sort_by: str = Query(
        default="income_date",
        pattern="^(income_date|amount|title|source|created_at)$",
        description="Sort field",
    ),
    sort_order: str = Query(
        default="desc",
        pattern="^(asc|desc)$",
        description="Sort direction",
    ),
    page: int = Query(default=1, ge=1, description="Page number"),
    page_size: int = Query(default=20, ge=1, le=100, description="Items per page"),
    db: AsyncSession = Depends(get_db),
):
    """List incomes with search, filtering by source/date, sorting, and pagination."""
    service = IncomeService(db)
    return await service.list_incomes(
        search=search,
        date_from=date_from,
        date_to=date_to,
        source=source,
        min_amount=min_amount,
        max_amount=max_amount,
        sort_by=sort_by,
        sort_order=sort_order,
        page=page,
        page_size=page_size,
    )


@router.get("/summary", response_model=IncomeSummaryResponse)
async def get_income_summary(
    period_month: Optional[str] = Query(
        default=None,
        pattern=r"^\d{4}-\d{2}$",
        description="Target month in YYYY-MM format",
    ),
    db: AsyncSession = Depends(get_db),
):
    """Retrieve monthly total income and breakdown by source."""
    service = IncomeService(db)
    return await service.get_summary(period_month=period_month)


@router.get("/{income_id}", response_model=IncomeResponse)
async def get_income(
    income_id: uuid.UUID,
    db: AsyncSession = Depends(get_db),
):
    """Retrieve a single income by UUID."""
    service = IncomeService(db)
    return await service.get_income(income_id)


@router.post("", response_model=IncomeResponse, status_code=status.HTTP_201_CREATED)
async def create_income(
    data: IncomeCreate,
    db: AsyncSession = Depends(get_db),
):
    """Record a new income entry."""
    service = IncomeService(db)
    return await service.create_income(data)


@router.patch("/{income_id}", response_model=IncomeResponse)
async def update_income(
    income_id: uuid.UUID,
    data: IncomeUpdate,
    db: AsyncSession = Depends(get_db),
):
    """Update an existing income entry."""
    service = IncomeService(db)
    return await service.update_income(income_id, data)


@router.delete("/{income_id}")
async def delete_income(
    income_id: uuid.UUID,
    db: AsyncSession = Depends(get_db),
):
    """Delete an income entry by UUID."""
    service = IncomeService(db)
    return await service.delete_income(income_id)
