import uuid
from datetime import date
from decimal import Decimal
from typing import Optional, Sequence, Tuple
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.income import Income
from app.schemas.income import IncomeCreate, IncomeUpdate


class IncomeRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def get_by_id(self, income_id: uuid.UUID) -> Optional[Income]:
        stmt = select(Income).where(Income.id == income_id)
        result = await self.session.execute(stmt)
        return result.scalars().first()

    async def get_paginated(
        self,
        *,
        search: Optional[str] = None,
        date_from: Optional[date] = None,
        date_to: Optional[date] = None,
        source: Optional[str] = None,
        min_amount: Optional[Decimal] = None,
        max_amount: Optional[Decimal] = None,
        sort_by: str = "income_date",
        sort_order: str = "desc",
        page: int = 1,
        page_size: int = 20,
    ) -> Tuple[Sequence[Income], int]:
        query = select(Income)
        count_query = select(func.count(Income.id))

        # Filters
        if search:
            search_filter = (
                Income.title.ilike(f"%{search.strip()}%") |
                Income.notes.ilike(f"%{search.strip()}%")
            )
            query = query.where(search_filter)
            count_query = count_query.where(search_filter)

        if date_from:
            query = query.where(Income.income_date >= date_from)
            count_query = count_query.where(Income.income_date >= date_from)

        if date_to:
            query = query.where(Income.income_date <= date_to)
            count_query = count_query.where(Income.income_date <= date_to)

        if source and source.lower() != "all":
            query = query.where(Income.source.ilike(source.strip()))
            count_query = count_query.where(Income.source.ilike(source.strip()))

        if min_amount is not None:
            query = query.where(Income.amount >= min_amount)
            count_query = count_query.where(Income.amount >= min_amount)

        if max_amount is not None:
            query = query.where(Income.amount <= max_amount)
            count_query = count_query.where(Income.amount <= max_amount)

        # Sorting
        sort_column_map = {
            "income_date": Income.income_date,
            "amount": Income.amount,
            "title": Income.title,
            "source": Income.source,
            "created_at": Income.created_at,
        }
        sort_col = sort_column_map.get(sort_by, Income.income_date)
        order_clause = sort_col.asc() if sort_order == "asc" else sort_col.desc()
        query = query.order_by(order_clause, Income.created_at.desc())

        # Total count
        total_res = await self.session.execute(count_query)
        total = total_res.scalar() or 0

        # Pagination
        offset = (page - 1) * page_size
        query = query.offset(offset).limit(page_size)

        result = await self.session.execute(query)
        items = result.scalars().all()

        return items, total

    async def create(self, data: IncomeCreate) -> Income:
        income = Income(
            title=data.title,
            amount=data.amount,
            income_date=data.income_date,
            source=data.source,
            payment_mode=data.payment_mode,
            notes=data.notes,
        )
        self.session.add(income)
        await self.session.commit()
        await self.session.refresh(income)
        return income

    async def update(self, income: Income, data: IncomeUpdate) -> Income:
        update_data = data.model_dump(exclude_unset=True)
        for key, value in update_data.items():
            setattr(income, key, value)
        await self.session.commit()
        await self.session.refresh(income)
        return income

    async def delete(self, income: Income) -> None:
        await self.session.delete(income)
        await self.session.commit()

    async def get_total_for_period(
        self, start_date: date, end_date: date
    ) -> Decimal:
        stmt = (
            select(func.coalesce(func.sum(Income.amount), Decimal("0.00")))
            .where(Income.income_date >= start_date)
            .where(Income.income_date <= end_date)
        )
        result = await self.session.execute(stmt)
        return Decimal(str(result.scalar() or "0.00"))

    async def get_breakdown_by_source(
        self, start_date: date, end_date: date
    ) -> list[tuple[str, Decimal, int]]:
        stmt = (
            select(
                Income.source,
                func.coalesce(func.sum(Income.amount), Decimal("0.00")),
                func.count(Income.id),
            )
            .where(Income.income_date >= start_date)
            .where(Income.income_date <= end_date)
            .group_by(Income.source)
            .order_by(func.sum(Income.amount).desc())
        )
        result = await self.session.execute(stmt)
        return [(r[0], Decimal(str(r[1])), r[2]) for r in result.all()]
