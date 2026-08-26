import uuid
from datetime import date
from decimal import Decimal
from typing import Optional, Sequence, Tuple
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.models.category import Category
from app.models.expense import Expense, PaymentMode
from app.schemas.expense import ExpenseCreate, ExpenseUpdate


class ExpenseRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def get_by_id(self, expense_id: uuid.UUID) -> Optional[Expense]:
        stmt = (
            select(Expense)
            .options(selectinload(Expense.category))
            .where(Expense.id == expense_id)
        )
        result = await self.session.execute(stmt)
        return result.scalars().first()

    async def get_paginated(
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
    ) -> Tuple[Sequence[Expense], int]:
        query = select(Expense).options(selectinload(Expense.category))
        count_query = select(func.count(Expense.id))

        # Filters
        if search:
            search_filter = (
                Expense.title.ilike(f"%{search.strip()}%") |
                Expense.notes.ilike(f"%{search.strip()}%")
            )
            query = query.where(search_filter)
            count_query = count_query.where(search_filter)

        if date_from:
            query = query.where(Expense.expense_date >= date_from)
            count_query = count_query.where(Expense.expense_date >= date_from)

        if date_to:
            query = query.where(Expense.expense_date <= date_to)
            count_query = count_query.where(Expense.expense_date <= date_to)

        if category_id:
            query = query.where(Expense.category_id == category_id)
            count_query = count_query.where(Expense.category_id == category_id)

        if min_amount is not None:
            query = query.where(Expense.amount >= min_amount)
            count_query = count_query.where(Expense.amount >= min_amount)

        if max_amount is not None:
            query = query.where(Expense.amount <= max_amount)
            count_query = count_query.where(Expense.amount <= max_amount)

        if payment_mode:
            query = query.where(Expense.payment_mode == payment_mode.value)
            count_query = count_query.where(Expense.payment_mode == payment_mode.value)

        # Sorting
        sort_column_map = {
            "expense_date": Expense.expense_date,
            "amount": Expense.amount,
            "title": Expense.title,
            "created_at": Expense.created_at,
        }
        sort_column = sort_column_map.get(sort_by, Expense.expense_date)
        if sort_order.lower() == "asc":
            query = query.order_by(sort_column.asc())
        else:
            query = query.order_by(sort_column.desc())

        # Total count
        total_result = await self.session.execute(count_query)
        total = total_result.scalar_one() or 0

        # Pagination
        offset = (page - 1) * page_size
        query = query.offset(offset).limit(page_size)

        result = await self.session.execute(query)
        items = result.scalars().all()
        return items, total

    async def create(self, data: ExpenseCreate) -> Expense:
        expense = Expense(
            title=data.title.strip(),
            category_id=data.category_id,
            amount=data.amount,
            expense_date=data.expense_date,
            notes=data.notes.strip() if data.notes else None,
            payment_mode=data.payment_mode.value if data.payment_mode else None,
        )
        self.session.add(expense)
        await self.session.commit()
        await self.session.refresh(expense)
        return await self.get_by_id(expense.id)

    async def update(self, expense: Expense, data: ExpenseUpdate) -> Expense:
        update_dict = data.model_dump(exclude_unset=True)
        if "title" in update_dict and update_dict["title"] is not None:
            expense.title = update_dict["title"].strip()
        if "category_id" in update_dict and update_dict["category_id"] is not None:
            expense.category_id = update_dict["category_id"]
        if "amount" in update_dict and update_dict["amount"] is not None:
            expense.amount = update_dict["amount"]
        if "expense_date" in update_dict and update_dict["expense_date"] is not None:
            expense.expense_date = update_dict["expense_date"]
        if "notes" in update_dict:
            expense.notes = update_dict["notes"].strip() if update_dict["notes"] else None
        if "payment_mode" in update_dict:
            expense.payment_mode = (
                update_dict["payment_mode"].value
                if update_dict["payment_mode"]
                else None
            )

        await self.session.commit()
        await self.session.refresh(expense)
        return await self.get_by_id(expense.id)

    async def delete(self, expense: Expense) -> None:
        await self.session.delete(expense)
        await self.session.commit()
