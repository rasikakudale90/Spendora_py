from datetime import date
from typing import Optional
import uuid
from fastapi import APIRouter, Depends, Query, Response
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.models.user import User
from app.routers.auth import get_current_user
from app.services.report_service import ReportService

router = APIRouter(prefix="/reports", tags=["Reports & Data Export"])


@router.get("/expenses/csv")
async def export_expenses_csv(
    date_from: Optional[date] = Query(None, description="Start date (inclusive)"),
    date_to: Optional[date] = Query(None, description="End date (inclusive)"),
    category_id: Optional[uuid.UUID] = Query(None, description="Filter by category UUID"),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Export expenses as a downloadable CSV file."""
    service = ReportService(db)
    csv_content = await service.generate_expenses_csv(
        user_id=current_user.id,
        date_from=date_from,
        date_to=date_to,
        category_id=category_id,
    )
    today_str = date.today().isoformat()
    filename = f"spendora_expenses_{today_str}.csv"
    return Response(
        content=csv_content,
        media_type="text/csv",
        headers={"Content-Disposition": f"attachment; filename={filename}"},
    )


@router.get("/incomes/csv")
async def export_incomes_csv(
    date_from: Optional[date] = Query(None, description="Start date (inclusive)"),
    date_to: Optional[date] = Query(None, description="End date (inclusive)"),
    source: Optional[str] = Query(None, description="Filter by income source"),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Export incomes as a downloadable CSV file."""
    service = ReportService(db)
    csv_content = await service.generate_incomes_csv(
        user_id=current_user.id,
        date_from=date_from,
        date_to=date_to,
        source=source,
    )
    today_str = date.today().isoformat()
    filename = f"spendora_incomes_{today_str}.csv"
    return Response(
        content=csv_content,
        media_type="text/csv",
        headers={"Content-Disposition": f"attachment; filename={filename}"},
    )


@router.get("/statement")
async def get_monthly_statement_summary(
    period_month: Optional[str] = Query(
        None,
        description="Month in YYYY-MM format. Defaults to current month.",
    ),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    """Get structured financial statement summary for a specific month."""
    if not period_month:
        today = date.today()
        period_month = f"{today.year:04d}-{today.month:02d}"

    service = ReportService(db)
    return await service.generate_statement_summary(
        user_id=current_user.id,
        period_month=period_month,
    )
