from __future__ import annotations

from typing import Optional
import uuid
from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.core.dependencies import get_current_user
from app.models.user import User
from app.schemas.goal import (
    GoalContributeRequest,
    GoalCreate,
    GoalListResponse,
    GoalResponse,
    GoalUpdate,
)
from app.services.goal_service import GoalService

router = APIRouter(prefix="/goals", tags=["Goals & Savings Runway"])


@router.get("", response_model=GoalListResponse)
async def list_goals(
    status_filter: Optional[str] = Query(None, alias="status", description="Filter by status ('active', 'completed', 'paused', 'all')"),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """List all financial goals for the authenticated user with live AI runway forecasting."""
    service = GoalService(db)
    return await service.list_goals(current_user.id, status_filter=status_filter)


@router.post("", response_model=GoalResponse, status_code=status.HTTP_201_CREATED)
async def create_goal(
    data: GoalCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Create a new savings goal with zero-trust tenancy."""
    service = GoalService(db)
    return await service.create_goal(current_user.id, data)


@router.get("/{goal_id}", response_model=GoalResponse)
async def get_goal(
    goal_id: uuid.UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Get single goal details with computed progress and runway forecast."""
    service = GoalService(db)
    goal = await service.get_goal(goal_id, current_user.id)
    # Wrap in GoalResponse with computed runway
    list_res = await service.list_goals(current_user.id)
    matching = next((g for g in list_res.items if g.id == goal.id), None)
    if matching:
        return matching
    return GoalResponse.from_orm_with_computed(goal)


@router.patch("/{goal_id}", response_model=GoalResponse)
async def update_goal(
    goal_id: uuid.UUID,
    data: GoalUpdate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Update goal attributes (name, target, deadline, notes, status)."""
    service = GoalService(db)
    return await service.update_goal(goal_id, current_user.id, data)


@router.post("/{goal_id}/contribute", response_model=GoalResponse)
async def contribute_to_goal(
    goal_id: uuid.UUID,
    data: GoalContributeRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Deposit or withdraw funds from a specific goal."""
    service = GoalService(db)
    return await service.contribute_to_goal(goal_id, current_user.id, data)


@router.delete("/{goal_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_goal(
    goal_id: uuid.UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Delete a goal."""
    service = GoalService(db)
    await service.delete_goal(goal_id, current_user.id)
    return None
