import uuid
from typing import Optional, Sequence
from sqlalchemy import func, select, update
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.category import Category
from app.models.expense import Expense


class CategoryRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def get_all_with_counts(self) -> Sequence[tuple[Category, int]]:
        stmt = (
            select(Category, func.count(Expense.id).label("expense_count"))
            .outerjoin(Expense, Expense.category_id == Category.id)
            .group_by(Category.id)
            .order_by(Category.name.asc())
        )
        result = await self.session.execute(stmt)
        return result.all()  # List of (Category, int)

    async def get_by_id(self, category_id: uuid.UUID) -> Optional[Category]:
        stmt = select(Category).where(Category.id == category_id)
        result = await self.session.execute(stmt)
        return result.scalars().first()

    async def get_by_name(self, name: str) -> Optional[Category]:
        stmt = select(Category).where(func.lower(Category.name) == name.strip().lower())
        result = await self.session.execute(stmt)
        return result.scalars().first()

    async def create(self, name: str) -> Category:
        category = Category(name=name.strip())
        self.session.add(category)
        await self.session.commit()
        await self.session.refresh(category)
        return category

    async def update_name(self, category: Category, new_name: str) -> Category:
        category.name = new_name.strip()
        await self.session.commit()
        await self.session.refresh(category)
        return category

    async def count_expenses(self, category_id: uuid.UUID) -> int:
        stmt = select(func.count(Expense.id)).where(Expense.category_id == category_id)
        result = await self.session.execute(stmt)
        return result.scalar_one() or 0

    async def reassign_expenses(self, from_category_id: uuid.UUID, to_category_id: uuid.UUID) -> int:
        stmt = (
            update(Expense)
            .where(Expense.category_id == from_category_id)
            .values(category_id=to_category_id)
        )
        result = await self.session.execute(stmt)
        return result.rowcount

    async def delete(self, category: Category) -> None:
        await self.session.delete(category)
        await self.session.commit()
