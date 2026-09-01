import uuid
from datetime import date
from decimal import Decimal
from typing import Literal, Tuple
from fastapi import HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.models.budget import Budget
from app.repositories.budget_repository import BudgetRepository
from app.repositories.category_repository import CategoryRepository
from app.schemas.budget import (
    BudgetCreate,
    BudgetListResponse,
    BudgetResponse,
    BudgetUpdate,
    compute_period_bounds,
)


class BudgetService:
    def __init__(self, session: AsyncSession):
        self.session = session
        self.repo = BudgetRepository(session)
        self.category_repo = CategoryRepository(session)

    def _calculate_budget_status(
        self, budget_amount: Decimal, spent: Decimal
    ) -> Tuple[Decimal, float, Literal["on_track", "near_limit", "over_budget"]]:
        remaining = max(Decimal("0.00"), budget_amount - spent)
        percentage_used = (
            float((spent / budget_amount) * 100) if budget_amount > 0 else 0.0
        )

        threshold = Decimal(str(settings.BUDGET_NEAR_LIMIT_THRESHOLD)) * budget_amount

        if spent > budget_amount:
            status_val = "over_budget"
        elif spent >= threshold:
            status_val = "near_limit"
        else:
            status_val = "on_track"

        return remaining, round(percentage_used, 2), status_val

    async def get_budgets(
        self, user_id: uuid.UUID, target_date: date, period_type: str = "monthly"
    ) -> BudgetListResponse:
        start_date, end_date = compute_period_bounds(target_date, period_type)

        budgets = await self.repo.get_by_period(user_id, start_date, period_type)

        overall_resp = None
        category_resps = []

        for b in budgets:
            if b.scope == "overall":
                spent = await self.repo.get_spent_for_period(user_id, start_date, end_date)
                remaining, pct, status_val = self._calculate_budget_status(b.amount, spent)
                overall_resp = BudgetResponse(
                    id=b.id,
                    scope=b.scope,
                    category_id=None,
                    category_name=None,
                    amount=b.amount,
                    period_type=b.period_type,
                    period_start=b.period_start,
                    period_end=b.period_end,
                    period_month=b.period_month,
                    spent=spent,
                    remaining=remaining,
                    percentage_used=pct,
                    status=status_val,
                    created_at=b.created_at,
                    updated_at=b.updated_at,
                )
            else:
                spent = await self.repo.get_spent_for_period(user_id, start_date, end_date, b.category_id)
                remaining, pct, status_val = self._calculate_budget_status(b.amount, spent)
                category_resps.append(
                    BudgetResponse(
                        id=b.id,
                        scope=b.scope,
                        category_id=b.category_id,
                        category_name=b.category.name if b.category else None,
                        amount=b.amount,
                        period_type=b.period_type,
                        period_start=b.period_start,
                        period_end=b.period_end,
                        period_month=b.period_month,
                        spent=spent,
                        remaining=remaining,
                        percentage_used=pct,
                        status=status_val,
                        created_at=b.created_at,
                        updated_at=b.updated_at,
                    )
                )

        return BudgetListResponse(
            period_type=period_type,
            period_start=start_date,
            period_end=end_date,
            overall_budget=overall_resp,
            category_budgets=category_resps,
        )

    async def set_budget(self, user_id: uuid.UUID, data: BudgetCreate) -> BudgetResponse:
        start_date, end_date = compute_period_bounds(
            data.period_start or data.period_month or date.today(),
            data.period_type,
        )

        if data.scope == "category":
            category = await self.category_repo.get_by_id(data.category_id)
            if not category:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=f"Category with id '{data.category_id}' does not exist",
                )
            category_name = category.name
            spent = await self.repo.get_spent_for_period(user_id, start_date, end_date, data.category_id)
        else:
            category_name = None
            spent = await self.repo.get_spent_for_period(user_id, start_date, end_date)

        budget = await self.repo.upsert(data, user_id=user_id)
        remaining, pct, status_val = self._calculate_budget_status(budget.amount, spent)

        return BudgetResponse(
            id=budget.id,
            scope=budget.scope,
            category_id=budget.category_id,
            category_name=category_name,
            amount=budget.amount,
            period_type=budget.period_type,
            period_start=budget.period_start,
            period_end=budget.period_end,
            period_month=budget.period_month,
            spent=spent,
            remaining=remaining,
            percentage_used=pct,
            status=status_val,
            created_at=budget.created_at,
            updated_at=budget.updated_at,
        )

    async def update_budget(self, user_id: uuid.UUID, budget_id: uuid.UUID, data: BudgetUpdate) -> BudgetResponse:
        budget = await self.repo.get_by_id(budget_id, user_id=user_id)
        if not budget:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Budget with id '{budget_id}' not found",
            )

        budget.amount = data.amount
        await self.session.commit()
        await self.session.refresh(budget)

        if budget.scope == "category":
            category_name = budget.category.name if budget.category else None
            spent = await self.repo.get_spent_for_period(user_id, budget.period_start, budget.period_end, budget.category_id)
        else:
            category_name = None
            spent = await self.repo.get_spent_for_period(user_id, budget.period_start, budget.period_end)

        remaining, pct, status_val = self._calculate_budget_status(budget.amount, spent)

        return BudgetResponse(
            id=budget.id,
            scope=budget.scope,
            category_id=budget.category_id,
            category_name=category_name,
            amount=budget.amount,
            period_type=budget.period_type,
            period_start=budget.period_start,
            period_end=budget.period_end,
            period_month=budget.period_month,
            spent=spent,
            remaining=remaining,
            percentage_used=pct,
            status=status_val,
            created_at=budget.created_at,
            updated_at=budget.updated_at,
        )

    async def delete_budget(self, user_id: uuid.UUID, budget_id: uuid.UUID) -> None:
        budget = await self.repo.get_by_id(budget_id, user_id=user_id)
        if not budget:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Budget with id '{budget_id}' not found",
            )
        await self.repo.delete(budget)
