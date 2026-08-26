import math
import uuid
from datetime import date
from decimal import Decimal
from typing import Optional
from fastapi import HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.expense import PaymentMode
from app.repositories.category_repository import CategoryRepository
from app.repositories.expense_repository import ExpenseRepository
from app.schemas.expense import (
    ExpenseCreate,
    ExpenseListResponse,
    ExpenseResponse,
    ExpenseUpdate,
)


class ExpenseService:
    def __init__(self, session: AsyncSession):
        self.session = session
        self.repo = ExpenseRepository(session)
        self.category_repo = CategoryRepository(session)

    async def list_expenses(
        self,
        *,
        search: Optional[str] = None,
        date_from: Optional[date] = None,
        date_to: Optional[date] = None,
        category_id: Optional[uuid.UUID] = None,
        min_amount: Optional[Decimal] = None,
        max_amount: Optional[Decimal] = None,
        payment_mode: Optional[PaymentMode] = None,
        sort_by: str = "expense_date",
        sort_order: str = "desc",
        page: int = 1,
        page_size: int = 20,
    ) -> ExpenseListResponse:
        items, total = await self.repo.get_paginated(
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

        total_pages = math.ceil(total / page_size) if total > 0 else 1

        return ExpenseListResponse(
            items=[ExpenseResponse.model_validate(item) for item in items],
            total=total,
            page=page,
            page_size=page_size,
            total_pages=total_pages,
        )

    async def get_expense(self, expense_id: uuid.UUID) -> ExpenseResponse:
        expense = await self.repo.get_by_id(expense_id)
        if not expense:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Expense with id '{expense_id}' not found",
            )
        return ExpenseResponse.model_validate(expense)

    async def create_expense(self, data: ExpenseCreate) -> ExpenseResponse:
        category = await self.category_repo.get_by_id(data.category_id)
        if not category:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Category with id '{data.category_id}' does not exist",
            )

        expense = await self.repo.create(data)
        return ExpenseResponse.model_validate(expense)

    async def update_expense(
        self, expense_id: uuid.UUID, data: ExpenseUpdate
    ) -> ExpenseResponse:
        expense = await self.repo.get_by_id(expense_id)
        if not expense:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Expense with id '{expense_id}' not found",
            )

        if data.category_id is not None:
            category = await self.category_repo.get_by_id(data.category_id)
            if not category:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=f"Category with id '{data.category_id}' does not exist",
                )

        updated = await self.repo.update(expense, data)
        return ExpenseResponse.model_validate(updated)

    async def delete_expense(self, expense_id: uuid.UUID) -> dict[str, str]:
        expense = await self.repo.get_by_id(expense_id)
        if not expense:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Expense with id '{expense_id}' not found",
            )
        await self.repo.delete(expense)
        return {"message": "Expense deleted successfully"}
