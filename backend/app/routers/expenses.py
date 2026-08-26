import uuid
from datetime import date
from decimal import Decimal
from typing import Optional
from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.models.expense import PaymentMode
from app.schemas.expense import (
    ExpenseCreate,
    ExpenseListResponse,
    ExpenseResponse,
    ExpenseUpdate,
)
from app.services.expense_service import ExpenseService

router = APIRouter(prefix="/expenses", tags=["Expenses"])


@router.get("", response_model=ExpenseListResponse)
async def list_expenses(
    search: Optional[str] = Query(default=None, description="Search in title and notes"),
    date_from: Optional[date] = Query(default=None, description="Filter from date (YYYY-MM-DD)"),
    date_to: Optional[date] = Query(default=None, description="Filter to date (YYYY-MM-DD)"),
    category_id: Optional[uuid.UUID] = Query(default=None, description="Filter by category UUID"),
    min_amount: Optional[Decimal] = Query(default=None, ge=0, description="Minimum amount filter"),
    max_amount: Optional[Decimal] = Query(default=None, ge=0, description="Maximum amount filter"),
    payment_mode: Optional[PaymentMode] = Query(default=None, description="Filter by payment mode"),
    sort_by: str = Query(
        default="expense_date",
        regex="^(expense_date|amount|title|created_at)$",
        description="Sort field",
    ),
    sort_order: str = Query(
        default="desc",
        regex="^(asc|desc)$",
        description="Sort direction",
    ),
    page: int = Query(default=1, ge=1, description="Page number"),
    page_size: int = Query(default=20, ge=1, le=100, description="Items per page"),
    db: AsyncSession = Depends(get_db),
):
    """List expenses with comprehensive filtering, search, sorting, and pagination (FR-3, FR-11–16)."""
    service = ExpenseService(db)
    return await service.list_expenses(
        search=search,
        date_from=date_from,
        date_to=date_to,
        category_id=category_id,
        min_amount=min_amount,
        max_amount=max_amount,
        payment_mode=payment_mode,
        sort_by=sort_by,
        sort_order=sort_order,
        page=page,
        page_size=page_size,
    )


@router.post("", response_model=ExpenseResponse, status_code=status.HTTP_201_CREATED)
async def create_expense(
    data: ExpenseCreate, db: AsyncSession = Depends(get_db)
):
    """Create a new expense (FR-2)."""
    service = ExpenseService(db)
    return await service.create_expense(data)


@router.get("/{expense_id}", response_model=ExpenseResponse)
async def get_expense(
    expense_id: uuid.UUID, db: AsyncSession = Depends(get_db)
):
    """Retrieve single expense details."""
    service = ExpenseService(db)
    return await service.get_expense(expense_id)


@router.put("/{expense_id}", response_model=ExpenseResponse)
async def update_expense(
    expense_id: uuid.UUID,
    data: ExpenseUpdate,
    db: AsyncSession = Depends(get_db),
):
    """Update an existing expense (FR-4)."""
    service = ExpenseService(db)
    return await service.update_expense(expense_id, data)


@router.delete("/{expense_id}")
async def delete_expense(
    expense_id: uuid.UUID, db: AsyncSession = Depends(get_db)
):
    """Delete an expense (FR-5)."""
    service = ExpenseService(db)
    return await service.delete_expense(expense_id)
