import uuid
from datetime import date
from decimal import Decimal
from typing import Optional, Sequence, Tuple
from sqlalchemy import desc, func, select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.models.category import Category
from app.models.expense import Expense


class DashboardRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def get_period_spending_and_count(
        self, user_id: uuid.UUID, start_date: date, end_date: date
    ) -> Tuple[Decimal, int]:
        stmt = select(
            func.coalesce(func.sum(Expense.amount), Decimal("0.00")),
            func.count(Expense.id),
        ).where(
            Expense.user_id == user_id,
            Expense.expense_date >= start_date,
            Expense.expense_date <= end_date,
        )
        result = await self.session.execute(stmt)
        row = result.first()
        if not row:
            return Decimal("0.00"), 0
        return row[0], row[1]

    async def get_recent_expenses(self, user_id: uuid.UUID, limit: int = 5) -> Sequence[Expense]:
        stmt = (
            select(Expense)
            .options(selectinload(Expense.category))
            .where(Expense.user_id == user_id)
            .order_by(Expense.expense_date.desc(), Expense.created_at.desc())
            .limit(limit)
        )
        result = await self.session.execute(stmt)
        return result.scalars().all()

    async def get_category_breakdown(
        self, user_id: uuid.UUID, start_date: date, end_date: date
    ) -> Sequence[Tuple[uuid.UUID, str, Decimal]]:
        stmt = (
            select(
                Category.id,
                Category.name,
                func.coalesce(func.sum(Expense.amount), Decimal("0.00")).label("total_amount"),
            )
            .join(Expense, Expense.category_id == Category.id)
            .where(
                Expense.user_id == user_id,
                Expense.expense_date >= start_date,
                Expense.expense_date <= end_date,
            )
            .group_by(Category.id, Category.name)
            .order_by(desc("total_amount"))
        )
        result = await self.session.execute(stmt)
        return result.all()

    async def get_daily_trend(
        self, user_id: uuid.UUID, start_date: date, end_date: date
    ) -> Sequence[Tuple[date, Decimal, int]]:
        stmt = (
            select(
                Expense.expense_date,
                func.sum(Expense.amount).label("daily_total"),
                func.count(Expense.id).label("expense_count"),
            )
            .where(
                Expense.user_id == user_id,
                Expense.expense_date >= start_date,
                Expense.expense_date <= end_date,
            )
            .group_by(Expense.expense_date)
            .order_by(Expense.expense_date.asc())
        )
        result = await self.session.execute(stmt)
        return result.all()

    async def get_highest_expense(
        self, user_id: uuid.UUID, start_date: date, end_date: date
    ) -> Optional[Tuple[str, Decimal]]:
        stmt = (
            select(Expense.title, Expense.amount)
            .where(
                Expense.user_id == user_id,
                Expense.expense_date >= start_date,
                Expense.expense_date <= end_date,
            )
            .order_by(Expense.amount.desc())
            .limit(1)
        )
        result = await self.session.execute(stmt)
        return result.first()
