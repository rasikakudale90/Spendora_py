import uuid
from typing import Optional, Sequence
from sqlalchemy import func, or_, select, update
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.category import Category
from app.models.expense import Expense


class CategoryRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def get_all_with_counts(self, user_id: Optional[uuid.UUID] = None) -> Sequence[tuple[Category, int]]:
        if user_id is not None:
            # Show global starter categories (user_id IS NULL) + user's custom categories
            category_filter = or_(Category.user_id == user_id, Category.user_id.is_(None))
            join_cond = (Expense.category_id == Category.id) & (Expense.user_id == user_id)
        else:
            category_filter = Category.user_id.is_(None)
            join_cond = (Expense.category_id == Category.id)

        stmt = (
            select(Category, func.count(Expense.id).label("expense_count"))
            .outerjoin(Expense, join_cond)
            .where(category_filter)
            .group_by(Category.id)
            .order_by(Category.name.asc())
        )
        result = await self.session.execute(stmt)
        return result.all()  # List of (Category, int)

    async def get_by_id(self, category_id: uuid.UUID, user_id: Optional[uuid.UUID] = None) -> Optional[Category]:
        stmt = select(Category).where(Category.id == category_id)
        if user_id is not None:
            stmt = stmt.where(or_(Category.user_id == user_id, Category.user_id.is_(None)))
        result = await self.session.execute(stmt)
        return result.scalars().first()

    async def get_by_name(self, name: str, user_id: Optional[uuid.UUID] = None) -> Optional[Category]:
        stmt = select(Category).where(func.lower(Category.name) == name.strip().lower())
        if user_id is not None:
            stmt = stmt.where(or_(Category.user_id == user_id, Category.user_id.is_(None)))
        else:
            stmt = stmt.where(Category.user_id.is_(None))
        result = await self.session.execute(stmt)
        return result.scalars().first()

    async def create(self, name: str, user_id: Optional[uuid.UUID] = None) -> Category:
        category = Category(name=name.strip(), user_id=user_id)
        self.session.add(category)
        await self.session.commit()
        await self.session.refresh(category)
        return category

    async def update_name(self, category: Category, new_name: str) -> Category:
        category.name = new_name.strip()
        await self.session.commit()
        await self.session.refresh(category)
        return category

    async def count_expenses(self, category_id: uuid.UUID, user_id: Optional[uuid.UUID] = None) -> int:
        stmt = select(func.count(Expense.id)).where(Expense.category_id == category_id)
        if user_id is not None:
            stmt = stmt.where(Expense.user_id == user_id)
        result = await self.session.execute(stmt)
        return result.scalar_one() or 0

    async def reassign_expenses(
        self, from_category_id: uuid.UUID, to_category_id: uuid.UUID, user_id: Optional[uuid.UUID] = None
    ) -> int:
        stmt = (
            update(Expense)
            .where(Expense.category_id == from_category_id)
            .values(category_id=to_category_id)
        )
        if user_id is not None:
            stmt = stmt.where(Expense.user_id == user_id)
        result = await self.session.execute(stmt)
        return result.rowcount

    async def delete(self, category: Category) -> None:
        from app.models.budget import Budget
        from sqlalchemy import delete as sql_delete

        await self.session.execute(sql_delete(Budget).where(Budget.category_id == category.id))
        await self.session.delete(category)
        await self.session.commit()
