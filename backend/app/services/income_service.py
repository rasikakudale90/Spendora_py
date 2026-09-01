import calendar
import math
import uuid
from datetime import date
from decimal import Decimal
from typing import Optional
from fastapi import HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.income_repository import IncomeRepository
from app.schemas.income import (
    IncomeCreate,
    IncomeListResponse,
    IncomeResponse,
    IncomeSummaryResponse,
    IncomeUpdate,
    SourceBreakdownItem,
)


class IncomeService:
    def __init__(self, session: AsyncSession):
        self.session = session
        self.repo = IncomeRepository(session)

    async def list_incomes(
        self,
        *,
        user_id: uuid.UUID,
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
    ) -> IncomeListResponse:
        items, total = await self.repo.get_paginated(
            user_id=user_id,
            search=search,
            date_from=date_from,
            date_to=date_to,
            source=source,
            min_amount=min_amount,
            max_amount=max_amount,
            sort_by=sort_by,
            sort_order=sort_order,
            page=page,
            page_size=page_size,
        )

        total_pages = math.ceil(total / page_size) if total > 0 else 1

        return IncomeListResponse(
            items=[IncomeResponse.model_validate(item) for item in items],
            total=total,
            page=page,
            page_size=page_size,
            total_pages=total_pages,
        )

    async def get_income(self, income_id: uuid.UUID, user_id: uuid.UUID) -> IncomeResponse:
        income = await self.repo.get_by_id(income_id, user_id=user_id)
        if not income:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Income with id '{income_id}' not found",
            )
        return IncomeResponse.model_validate(income)

    async def create_income(self, data: IncomeCreate, user_id: uuid.UUID) -> IncomeResponse:
        income = await self.repo.create(data, user_id=user_id)
        return IncomeResponse.model_validate(income)

    async def update_income(
        self, income_id: uuid.UUID, data: IncomeUpdate, user_id: uuid.UUID
    ) -> IncomeResponse:
        income = await self.repo.get_by_id(income_id, user_id=user_id)
        if not income:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Income with id '{income_id}' not found",
            )

        updated = await self.repo.update(income, data)
        return IncomeResponse.model_validate(updated)

    async def delete_income(self, income_id: uuid.UUID, user_id: uuid.UUID) -> dict[str, str]:
        income = await self.repo.get_by_id(income_id, user_id=user_id)
        if not income:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Income with id '{income_id}' not found",
            )
        await self.repo.delete(income)
        return {"message": "Income deleted successfully"}

    async def get_summary(
        self, user_id: uuid.UUID, period_month: Optional[str] = None
    ) -> IncomeSummaryResponse:
        today = date.today()
        if period_month:
            try:
                parts = period_month.split("-")
                year, month = int(parts[0]), int(parts[1])
                start_date = date(year, month, 1)
            except Exception:
                start_date = date(today.year, today.month, 1)
        else:
            start_date = date(today.year, today.month, 1)

        _, last_day = calendar.monthrange(start_date.year, start_date.month)
        end_date = date(start_date.year, start_date.month, last_day)

        total_income = await self.repo.get_total_for_period(user_id, start_date, end_date)
        breakdown_tuples = await self.repo.get_breakdown_by_source(user_id, start_date, end_date)

        total_count = sum(t[2] for t in breakdown_tuples)
        breakdown_items = []
        for src, amt, cnt in breakdown_tuples:
            pct = round(float((amt / total_income) * 100), 2) if total_income > 0 else 0.0
            breakdown_items.append(
                SourceBreakdownItem(
                    source=src,
                    total_amount=amt,
                    percentage=pct,
                    count=cnt,
                )
            )

        return IncomeSummaryResponse(
            period_month=start_date.strftime("%Y-%m"),
            total_income=total_income,
            income_count=total_count,
            breakdown_by_source=breakdown_items,
        )
