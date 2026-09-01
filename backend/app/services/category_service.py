import uuid
from typing import Optional
from fastapi import HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.category_repository import CategoryRepository
from app.schemas.category import (
    CategoryCreate,
    CategoryResponse,
    CategoryUpdate,
    CategoryWithCountResponse,
)


class CategoryService:
    def __init__(self, session: AsyncSession):
        self.repo = CategoryRepository(session)

    async def list_categories(self, user_id: Optional[uuid.UUID] = None) -> list[CategoryWithCountResponse]:
        rows = await self.repo.get_all_with_counts(user_id=user_id)
        return [
            CategoryWithCountResponse(
                id=cat.id,
                name=cat.name,
                created_at=cat.created_at,
                updated_at=cat.updated_at,
                expense_count=count,
            )
            for cat, count in rows
        ]

    async def get_by_id(self, category_id: uuid.UUID, user_id: Optional[uuid.UUID] = None) -> CategoryResponse:
        category = await self.repo.get_by_id(category_id, user_id=user_id)
        if not category:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Category with id '{category_id}' not found",
            )
        return CategoryResponse.model_validate(category)

    async def create_category(self, data: CategoryCreate, user_id: Optional[uuid.UUID] = None) -> CategoryResponse:
        existing = await self.repo.get_by_name(data.name, user_id=user_id)
        if existing:
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail=f"Category '{data.name}' already exists",
            )
        category = await self.repo.create(data.name, user_id=user_id)
        return CategoryResponse.model_validate(category)

    async def rename_category(
        self, category_id: uuid.UUID, data: CategoryUpdate, user_id: Optional[uuid.UUID] = None
    ) -> CategoryResponse:
        category = await self.repo.get_by_id(category_id, user_id=user_id)
        if not category:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Category with id '{category_id}' not found",
            )

        if category.user_id is None and user_id is not None:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Default starter categories cannot be renamed",
            )

        existing = await self.repo.get_by_name(data.name, user_id=user_id)
        if existing and existing.id != category_id:
            raise HTTPException(
                status_code=status.HTTP_409_CONFLICT,
                detail=f"Category '{data.name}' already exists",
            )

        updated = await self.repo.update_name(category, data.name)
        return CategoryResponse.model_validate(updated)

    async def delete_category(
        self, category_id: uuid.UUID, reassign_to: Optional[uuid.UUID] = None, user_id: Optional[uuid.UUID] = None
    ) -> dict[str, str]:
        category = await self.repo.get_by_id(category_id, user_id=user_id)
        if not category:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Category with id '{category_id}' not found",
            )

        if category.user_id is None and user_id is not None:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Default starter categories cannot be deleted",
            )

        expense_count = await self.repo.count_expenses(category_id, user_id=user_id)

        if expense_count > 0:
            if not reassign_to:
                raise HTTPException(
                    status_code=status.HTTP_409_CONFLICT,
                    detail=(
                        f"Cannot delete category '{category.name}' because it contains "
                        f"{expense_count} expense(s). Please specify 'reassign_to' to move them."
                    ),
                )
            if reassign_to == category_id:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Cannot reassign expenses to the category being deleted",
                )
            target_category = await self.repo.get_by_id(reassign_to, user_id=user_id)
            if not target_category:
                raise HTTPException(
                    status_code=status.HTTP_404_NOT_FOUND,
                    detail=f"Reassignment target category with id '{reassign_to}' not found",
                )
            # Reassign expenses
            await self.repo.reassign_expenses(category_id, reassign_to, user_id=user_id)

        await self.repo.delete(category)
        return {"message": f"Category '{category.name}' deleted successfully"}
