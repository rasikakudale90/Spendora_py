from __future__ import annotations

import calendar
from datetime import date, timedelta
from decimal import Decimal
from typing import Optional, Sequence
import uuid
from fastapi import HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.goal import Goal
from app.repositories.dashboard_repository import DashboardRepository
from app.repositories.goal_repository import GoalRepository
from app.repositories.income_repository import IncomeRepository
from app.schemas.goal import (
    GoalContributeRequest,
    GoalCreate,
    GoalListResponse,
    GoalResponse,
    GoalRunwayForecast,
    GoalUpdate,
)


class GoalService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.repo = GoalRepository(db)
        self.income_repo = IncomeRepository(db)
        self.dashboard_repo = DashboardRepository(db)

    async def get_goal(self, goal_id: uuid.UUID, user_id: uuid.UUID) -> Goal:
        goal = await self.repo.get_by_id(goal_id, user_id=user_id)
        if not goal:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Goal with id '{goal_id}' not found.",
            )
        return goal

    async def list_goals(
        self,
        user_id: uuid.UUID,
        status_filter: Optional[str] = None,
    ) -> GoalListResponse:
        goals = await self.repo.list_goals(user_id, status=status_filter)

        # Get user monthly cash flow surplus for runway forecast
        today = date.today()
        start_of_month = date(today.year, today.month, 1)
        _, total_days = calendar.monthrange(today.year, today.month)
        end_of_month = date(today.year, today.month, total_days)

        total_income = await self.income_repo.get_total_for_period(user_id, start_of_month, end_of_month)
        total_spent, _ = await self.dashboard_repo.get_period_spending_and_count(user_id, start_of_month, end_of_month)
        monthly_surplus = max(Decimal("0.00"), total_income - total_spent)

        items: list[GoalResponse] = []
        total_target = Decimal("0.00")
        total_saved = Decimal("0.00")

        for goal in goals:
            total_target += goal.target_amount
            total_saved += goal.current_amount
            runway = self._calculate_runway(goal, monthly_surplus, today)
            items.append(GoalResponse.from_orm_with_computed(goal, runway=runway))

        total_remaining = max(Decimal("0.00"), total_target - total_saved)
        overall_progress = (
            float(min(Decimal("100.00"), (total_saved / total_target * Decimal("100.00"))))
            if total_target > 0
            else 0.0
        )

        return GoalListResponse(
            items=items,
            total_count=len(items),
            total_target_amount=total_target,
            total_saved_amount=total_saved,
            total_remaining_amount=total_remaining,
            overall_progress_percentage=round(overall_progress, 1),
        )

    def _calculate_runway(
        self,
        goal: Goal,
        monthly_surplus: Decimal,
        today: date,
    ) -> GoalRunwayForecast:
        target = Decimal(str(goal.target_amount))
        current = Decimal(str(goal.current_amount))
        remaining = max(Decimal("0.00"), target - current)

        if current >= target or goal.status == "completed":
            return GoalRunwayForecast(
                required_monthly_savings=Decimal("0.00"),
                projected_completion_date=today,
                months_remaining=0.0,
                days_remaining=0,
                pacing_status="completed",
                pacing_message="🎉 Goal fully completed!",
                speedup_suggestion="Target reached! Consider transferring surplus into your emergency buffer or next goal.",
            )

        if goal.target_date:
            days_diff = (goal.target_date - today).days
            if days_diff <= 0:
                return GoalRunwayForecast(
                    required_monthly_savings=remaining,
                    projected_completion_date=None,
                    months_remaining=0.0,
                    days_remaining=0,
                    pacing_status="behind",
                    pacing_message="Deadline has passed. Allocate surplus to close the remaining gap.",
                    speedup_suggestion=f"Allocate ₹{remaining:,.0f} from this month's savings to finalize this goal.",
                )

            months_diff = max(Decimal("0.1"), Decimal(str(round(days_diff / 30.4375, 2))))
            required_monthly = Decimal(str(round(remaining / months_diff, 2)))

            # Project completion date based on user's current surplus
            if monthly_surplus > 0:
                months_needed = remaining / monthly_surplus
                projected_date = today + timedelta(days=int(float(months_needed) * 30.4375))
            else:
                projected_date = None

            if monthly_surplus >= required_monthly * Decimal("1.2"):
                status_val = "ahead"
                msg = f"Ahead of schedule! Saving ₹{monthly_surplus:,.0f}/mo against required ₹{required_monthly:,.0f}/mo."
                speedup = "You have surplus room! You can hit this goal even earlier by increasing allocation."
            elif monthly_surplus >= required_monthly:
                status_val = "on_track"
                msg = f"On track! Your monthly surplus covers the required ₹{required_monthly:,.0f}/mo."
                speedup = f"Maintain your current burn rate to finish comfortably by {goal.target_date.strftime('%b %d, %Y')}."
            else:
                status_val = "at_risk"
                gap = required_monthly - monthly_surplus
                msg = f"At risk: Short by ₹{gap:,.0f}/mo to reach deadline on {goal.target_date.strftime('%b %d, %Y')}."
                speedup = f"Trim discretionary spending by ₹{gap:,.0f}/mo to restore on-track pacing."

            return GoalRunwayForecast(
                required_monthly_savings=required_monthly,
                projected_completion_date=projected_date,
                months_remaining=float(months_diff),
                days_remaining=days_diff,
                pacing_status=status_val,
                pacing_message=msg,
                speedup_suggestion=speedup,
            )
        else:
            # No deadline set
            if monthly_surplus > 0:
                months_needed = remaining / monthly_surplus
                projected_date = today + timedelta(days=int(float(months_needed) * 30.4375))
                msg = f"At current pace (₹{monthly_surplus:,.0f}/mo surplus), finish in ~{float(months_needed):.1f} months."
                speedup = "Setting a target deadline helps unlock personalized daily burn acceleration targets."
            else:
                projected_date = None
                msg = "No deadline specified. Surplus is currently ₹0.00; boost income or cut expenses to build runway."
                speedup = "Review micro-leaks and active budgets to generate a positive monthly cash buffer."

            return GoalRunwayForecast(
                required_monthly_savings=Decimal("0.00"),
                projected_completion_date=projected_date,
                months_remaining=None,
                days_remaining=None,
                pacing_status="no_deadline",
                pacing_message=msg,
                speedup_suggestion=speedup,
            )

    async def create_goal(self, user_id: uuid.UUID, data: GoalCreate) -> GoalResponse:
        goal = await self.repo.create(user_id, data)
        # Compute runway
        today = date.today()
        start_of_month = date(today.year, today.month, 1)
        _, total_days = calendar.monthrange(today.year, today.month)
        end_of_month = date(today.year, today.month, total_days)

        total_income = await self.income_repo.get_total_for_period(user_id, start_of_month, end_of_month)
        total_spent, _ = await self.dashboard_repo.get_period_spending_and_count(user_id, start_of_month, end_of_month)
        monthly_surplus = max(Decimal("0.00"), total_income - total_spent)

        runway = self._calculate_runway(goal, monthly_surplus, today)
        return GoalResponse.from_orm_with_computed(goal, runway=runway)

    async def update_goal(self, goal_id: uuid.UUID, user_id: uuid.UUID, data: GoalUpdate) -> GoalResponse:
        goal = await self.get_goal(goal_id, user_id)
        updated_goal = await self.repo.update(goal, data)

        today = date.today()
        start_of_month = date(today.year, today.month, 1)
        _, total_days = calendar.monthrange(today.year, today.month)
        end_of_month = date(today.year, today.month, total_days)

        total_income = await self.income_repo.get_total_for_period(user_id, start_of_month, end_of_month)
        total_spent, _ = await self.dashboard_repo.get_period_spending_and_count(user_id, start_of_month, end_of_month)
        monthly_surplus = max(Decimal("0.00"), total_income - total_spent)

        runway = self._calculate_runway(updated_goal, monthly_surplus, today)
        return GoalResponse.from_orm_with_computed(updated_goal, runway=runway)

    async def contribute_to_goal(
        self,
        goal_id: uuid.UUID,
        user_id: uuid.UUID,
        data: GoalContributeRequest,
    ) -> GoalResponse:
        goal = await self.get_goal(goal_id, user_id)
        updated_goal = await self.repo.contribute(goal, data.amount, action=data.action)

        today = date.today()
        start_of_month = date(today.year, today.month, 1)
        _, total_days = calendar.monthrange(today.year, today.month)
        end_of_month = date(today.year, today.month, total_days)

        total_income = await self.income_repo.get_total_for_period(user_id, start_of_month, end_of_month)
        total_spent, _ = await self.dashboard_repo.get_period_spending_and_count(user_id, start_of_month, end_of_month)
        monthly_surplus = max(Decimal("0.00"), total_income - total_spent)

        runway = self._calculate_runway(updated_goal, monthly_surplus, today)
        return GoalResponse.from_orm_with_computed(updated_goal, runway=runway)

    async def delete_goal(self, goal_id: uuid.UUID, user_id: uuid.UUID) -> None:
        goal = await self.get_goal(goal_id, user_id)
        await self.repo.delete(goal)
