import uuid
from datetime import date
from decimal import Decimal
from typing import Optional, Sequence
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.models.budget import Budget
from app.models.category import Category
from app.models.expense import Expense
from app.schemas.budget import BudgetCreate, compute_period_bounds


class BudgetRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def get_by_period(
        self, user_id: uuid.UUID, period_start: date, period_type: str = "monthly"
    ) -> Sequence[Budget]:
        stmt = (
            select(Budget)
            .options(selectinload(Budget.category))
            .where(
                Budget.user_id == user_id,
                Budget.period_type == period_type,
                Budget.period_start == period_start,
            )
            .order_by(Budget.scope.desc(), Budget.created_at.asc())
        )
        result = await self.session.execute(stmt)
        return result.scalars().all()

    async def get_overall(
        self, user_id: uuid.UUID, period_start: date, period_type: str = "monthly"
    ) -> Optional[Budget]:
        stmt = (
            select(Budget)
            .where(
                Budget.user_id == user_id,
                Budget.scope == "overall",
                Budget.period_type == period_type,
                Budget.period_start == period_start,
            )
        )
        result = await self.session.execute(stmt)
        return result.scalars().first()

    async def get_category_budget(
        self, user_id: uuid.UUID, category_id: uuid.UUID, period_start: date, period_type: str = "monthly"
    ) -> Optional[Budget]:
        stmt = (
            select(Budget)
            .options(selectinload(Budget.category))
            .where(
                Budget.user_id == user_id,
                Budget.scope == "category",
                Budget.category_id == category_id,
                Budget.period_type == period_type,
                Budget.period_start == period_start,
            )
        )
        result = await self.session.execute(stmt)
        return result.scalars().first()

    async def get_by_id(self, budget_id: uuid.UUID, user_id: Optional[uuid.UUID] = None) -> Optional[Budget]:
        stmt = (
            select(Budget)
            .options(selectinload(Budget.category))
            .where(Budget.id == budget_id)
        )
        if user_id is not None:
            stmt = stmt.where(Budget.user_id == user_id)
        result = await self.session.execute(stmt)
        return result.scalars().first()

    async def delete(self, budget: Budget) -> None:
        await self.session.delete(budget)
        await self.session.commit()

    async def upsert(self, data: BudgetCreate, user_id: uuid.UUID) -> Budget:
        start_date, end_date = compute_period_bounds(
            data.period_start or data.period_month or date.today(),
            data.period_type,
        )

        if data.scope == "overall":
            existing = await self.get_overall(user_id, start_date, data.period_type)
        else:
            existing = await self.get_category_budget(user_id, data.category_id, start_date, data.period_type)

        if existing:
            existing.amount = data.amount
            existing.period_start = start_date
            existing.period_end = end_date
            existing.period_month = date(start_date.year, start_date.month, 1)
            await self.session.commit()
            await self.session.refresh(existing)
            return existing
        else:
            budget = Budget(
                user_id=user_id,
                scope=data.scope,
                category_id=data.category_id,
                amount=data.amount,
                period_type=data.period_type,
                period_start=start_date,
                period_end=end_date,
                period_month=date(start_date.year, start_date.month, 1),
            )
            self.session.add(budget)
            await self.session.commit()
            await self.session.refresh(budget)
            return budget

    async def get_spent_for_period(
        self,
        user_id: uuid.UUID,
        start_date: date,
        end_date: date,
        category_id: Optional[uuid.UUID] = None,
    ) -> Decimal:
        stmt = select(func.coalesce(func.sum(Expense.amount), Decimal("0.00"))).where(
            Expense.user_id == user_id,
            Expense.expense_date >= start_date,
            Expense.expense_date <= end_date,
        )
        if category_id:
            stmt = stmt.where(Expense.category_id == category_id)

        result = await self.session.execute(stmt)
        return result.scalar_one() or Decimal("0.00")
