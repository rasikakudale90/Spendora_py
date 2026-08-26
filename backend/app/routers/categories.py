import uuid
from typing import Optional
from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.schemas.category import (
    CategoryCreate,
    CategoryResponse,
    CategoryUpdate,
    CategoryWithCountResponse,
)
from app.services.category_service import CategoryService

router = APIRouter(prefix="/categories", tags=["Categories"])


@router.get("", response_model=list[CategoryWithCountResponse])
async def list_categories(db: AsyncSession = Depends(get_db)):
    """List all categories with expense counts (FR-9)."""
    service = CategoryService(db)
    return await service.list_categories()


@router.post("", response_model=CategoryResponse, status_code=status.HTTP_201_CREATED)
async def create_category(
    data: CategoryCreate, db: AsyncSession = Depends(get_db)
):
    """Create a new category (FR-6)."""
    service = CategoryService(db)
    return await service.create_category(data)


@router.patch("/{category_id}", response_model=CategoryResponse)
async def rename_category(
    category_id: uuid.UUID,
    data: CategoryUpdate,
    db: AsyncSession = Depends(get_db),
):
    """Rename an existing category (FR-7)."""
    service = CategoryService(db)
    return await service.rename_category(category_id, data)


@router.delete("/{category_id}")
async def delete_category(
    category_id: uuid.UUID,
    reassign_to: Optional[uuid.UUID] = Query(
        default=None,
        description="Target category UUID to reassign existing expenses to before deletion",
    ),
    db: AsyncSession = Depends(get_db),
):
    """
    Safely delete a category (FR-8).
    If category contains expenses, returns 409 unless reassign_to is provided.
    """
    service = CategoryService(db)
    return await service.delete_category(category_id, reassign_to)
