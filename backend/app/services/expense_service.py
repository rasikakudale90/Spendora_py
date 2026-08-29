import math
import uuid
from datetime import date
from decimal import Decimal
from typing import Optional
from fastapi import HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.expense import PaymentMode
from app.repositories.budget_repository import BudgetRepository
from app.repositories.category_repository import CategoryRepository
from app.repositories.expense_repository import ExpenseRepository
from app.schemas.expense import (
    DailyBudgetAlert,
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

    async def _check_daily_budget_alert(
        self, expense_date: date, category_id: uuid.UUID
    ) -> Optional[DailyBudgetAlert]:
        budget_repo = BudgetRepository(self.session)
        # 1. Check overall daily budget first
        overall_budget = await budget_repo.get_overall(expense_date, "daily")
        if overall_budget:
            spent = await budget_repo.get_spent_for_period(expense_date, expense_date)
            if spent > overall_budget.amount:
                exceeded = spent - overall_budget.amount
                pct = round(float((spent / overall_budget.amount) * 100), 2)
                return DailyBudgetAlert(
                    exceeded=True,
                    limit_amount=overall_budget.amount,
                    total_spent=spent,
                    exceeded_amount=exceeded,
                    percentage_used=pct,
                    message=f"Daily overall limit of ₹{overall_budget.amount:.2f} exceeded! Total spent today is ₹{spent:.2f} (₹{exceeded:.2f} over limit).",
                )
        # 2. Check category daily budget
        cat_budget = await budget_repo.get_category_budget(category_id, expense_date, "daily")
        if cat_budget:
            spent = await budget_repo.get_spent_for_period(expense_date, expense_date, category_id)
            if spent > cat_budget.amount:
                exceeded = spent - cat_budget.amount
                pct = round(float((spent / cat_budget.amount) * 100), 2)
                return DailyBudgetAlert(
                    exceeded=True,
                    limit_amount=cat_budget.amount,
                    total_spent=spent,
                    exceeded_amount=exceeded,
                    percentage_used=pct,
                    message=f"Daily category limit of ₹{cat_budget.amount:.2f} exceeded! Spent ₹{spent:.2f} (₹{exceeded:.2f} over limit).",
                )
        return None

    async def create_expense(self, data: ExpenseCreate) -> ExpenseResponse:
        category = await self.category_repo.get_by_id(data.category_id)
        if not category:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Category with id '{data.category_id}' does not exist",
            )

        expense = await self.repo.create(data)
        alert = await self._check_daily_budget_alert(expense.expense_date, expense.category_id)
        resp = ExpenseResponse.model_validate(expense)
        resp.daily_budget_alert = alert
        return resp

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
        alert = await self._check_daily_budget_alert(updated.expense_date, updated.category_id)
        resp = ExpenseResponse.model_validate(updated)
        resp.daily_budget_alert = alert
        return resp

    async def delete_expense(self, expense_id: uuid.UUID) -> dict[str, str]:
        expense = await self.repo.get_by_id(expense_id)
        if not expense:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Expense with id '{expense_id}' not found",
            )
        await self.repo.delete(expense)
        return {"message": "Expense deleted successfully"}
