from __future__ import annotations

import uuid
from decimal import Decimal
from typing import Optional, Sequence
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.goal import Goal
from app.schemas.goal import GoalCreate, GoalUpdate


class GoalRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def get_by_id(self, goal_id: uuid.UUID, user_id: Optional[uuid.UUID] = None) -> Optional[Goal]:
        stmt = select(Goal).where(Goal.id == goal_id)
        if user_id is not None:
            stmt = stmt.where(Goal.user_id == user_id)
        result = await self.session.execute(stmt)
        return result.scalars().first()

    async def list_goals(
        self,
        user_id: uuid.UUID,
        status: Optional[str] = None,
    ) -> Sequence[Goal]:
        stmt = select(Goal).where(Goal.user_id == user_id)
        if status and status.lower() != "all":
            stmt = stmt.where(Goal.status == status)
        stmt = stmt.order_by(Goal.created_at.desc())
        result = await self.session.execute(stmt)
        return result.scalars().all()

    async def create(self, user_id: uuid.UUID, data: GoalCreate) -> Goal:
        goal = Goal(
            user_id=user_id,
            name=data.name.strip(),
            target_amount=data.target_amount,
            current_amount=data.current_amount or Decimal("0.00"),
            target_date=data.target_date,
            category=data.category or "Savings",
            color=data.color or "emerald",
            status="completed" if (data.current_amount or Decimal("0.00")) >= data.target_amount else "active",
            notes=data.notes.strip() if data.notes else None,
        )
        self.session.add(goal)
        await self.session.commit()
        await self.session.refresh(goal)
        return goal

    async def update(self, goal: Goal, data: GoalUpdate) -> Goal:
        if data.name is not None:
            goal.name = data.name.strip()
        if data.target_amount is not None:
            goal.target_amount = data.target_amount
        if data.current_amount is not None:
            goal.current_amount = data.current_amount
        if data.target_date is not None:
            goal.target_date = data.target_date
        if data.category is not None:
            goal.category = data.category
        if data.color is not None:
            goal.color = data.color
        if data.status is not None:
            goal.status = data.status
        if data.notes is not None:
            goal.notes = data.notes.strip() if data.notes else None

        # Auto-update status if target reached
        if goal.current_amount >= goal.target_amount:
            goal.status = "completed"
        elif goal.status == "completed" and goal.current_amount < goal.target_amount:
            goal.status = "active"

        await self.session.commit()
        await self.session.refresh(goal)
        return goal

    async def contribute(self, goal: Goal, amount: Decimal, action: str = "deposit") -> Goal:
        if action == "deposit":
            goal.current_amount = goal.current_amount + amount
        elif action == "withdraw":
            goal.current_amount = max(Decimal("0.00"), goal.current_amount - amount)

        if goal.current_amount >= goal.target_amount:
            goal.status = "completed"
        elif goal.status == "completed" and goal.current_amount < goal.target_amount:
            goal.status = "active"

        await self.session.commit()
        await self.session.refresh(goal)
        return goal

    async def delete(self, goal: Goal) -> None:
        await self.session.delete(goal)
        await self.session.commit()
