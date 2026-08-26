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
from app.schemas.budget import BudgetCreate


class BudgetRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def get_by_period(self, period_month: date) -> Sequence[Budget]:
        stmt = (
            select(Budget)
            .options(selectinload(Budget.category))
            .where(Budget.period_month == period_month)
            .order_by(Budget.scope.desc(), Budget.created_at.asc())
        )
        result = await self.session.execute(stmt)
        return result.scalars().all()

    async def get_overall(self, period_month: date) -> Optional[Budget]:
        stmt = (
            select(Budget)
            .where(Budget.scope == "overall", Budget.period_month == period_month)
        )
        result = await self.session.execute(stmt)
        return result.scalars().first()

    async def get_category_budget(
        self, category_id: uuid.UUID, period_month: date
    ) -> Optional[Budget]:
        stmt = (
            select(Budget)
            .options(selectinload(Budget.category))
            .where(
                Budget.scope == "category",
                Budget.category_id == category_id,
                Budget.period_month == period_month,
            )
        )
        result = await self.session.execute(stmt)
        return result.scalars().first()

    async def upsert(self, data: BudgetCreate) -> Budget:
        normalized_period = date(data.period_month.year, data.period_month.month, 1)

        if data.scope == "overall":
            existing = await self.get_overall(normalized_period)
        else:
            existing = await self.get_category_budget(data.category_id, normalized_period)

        if existing:
            existing.amount = data.amount
            await self.session.commit()
            await self.session.refresh(existing)
            return existing
        else:
            budget = Budget(
                scope=data.scope,
                category_id=data.category_id,
                amount=data.amount,
                period_month=normalized_period,
            )
            self.session.add(budget)
            await self.session.commit()
            await self.session.refresh(budget)
            return budget

    async def get_spent_for_period(
        self,
        start_date: date,
        end_date: date,
        category_id: Optional[uuid.UUID] = None,
    ) -> Decimal:
        stmt = select(func.coalesce(func.sum(Expense.amount), Decimal("0.00"))).where(
            Expense.expense_date >= start_date,
            Expense.expense_date <= end_date,
        )
        if category_id:
            stmt = stmt.where(Expense.category_id == category_id)

        result = await self.session.execute(stmt)
        return result.scalar_one() or Decimal("0.00")
