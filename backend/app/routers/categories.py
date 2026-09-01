import uuid
from typing import Optional
from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.core.dependencies import get_current_user
from app.models.user import User
from app.schemas.category import (
    CategoryCreate,
    CategoryResponse,
    CategoryUpdate,
    CategoryWithCountResponse,
)
from app.services.category_service import CategoryService

router = APIRouter(prefix="/categories", tags=["Categories"])


@router.get("", response_model=list[CategoryWithCountResponse])
async def list_categories(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """List all categories (global starter categories + custom categories) with expense counts for the authenticated user (FR-9)."""
    service = CategoryService(db)
    return await service.list_categories(user_id=current_user.id)


@router.post("", response_model=CategoryResponse, status_code=status.HTTP_201_CREATED)
async def create_category(
    data: CategoryCreate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Create a new custom category for the authenticated user (FR-6)."""
    service = CategoryService(db)
    return await service.create_category(data, user_id=current_user.id)


@router.get("/{category_id}", response_model=CategoryResponse)
async def get_category(
    category_id: uuid.UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Retrieve a single category by UUID."""
    service = CategoryService(db)
    return await service.get_by_id(category_id, user_id=current_user.id)


@router.put("/{category_id}", response_model=CategoryResponse)
@router.patch("/{category_id}", response_model=CategoryResponse)
async def rename_category(
    category_id: uuid.UUID,
    data: CategoryUpdate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Rename an existing custom category owned by the authenticated user (FR-7). Default starter categories cannot be renamed."""
    service = CategoryService(db)
    return await service.rename_category(category_id, data, user_id=current_user.id)


@router.delete("/{category_id}")
async def delete_category(
    category_id: uuid.UUID,
    reassign_to: Optional[uuid.UUID] = Query(
        default=None,
        description="Target category UUID to reassign existing expenses to before deletion",
    ),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """
    Safely delete a custom category (FR-8).
    Default starter categories cannot be deleted.
    If category contains expenses, returns 409 unless reassign_to is provided.
    """
    service = CategoryService(db)
    return await service.delete_category(category_id, reassign_to, user_id=current_user.id)
