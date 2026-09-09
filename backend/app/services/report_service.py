import csv
import io
import uuid
from datetime import date
from decimal import Decimal
from typing import Optional, Dict, Any, List
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.models.expense import Expense
from app.models.income import Income
from app.models.category import Category


class ReportService:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def generate_expenses_csv(
        self,
        user_id: uuid.UUID,
        date_from: Optional[date] = None,
        date_to: Optional[date] = None,
        category_id: Optional[uuid.UUID] = None,
    ) -> str:
        """Generates an RFC 4180 compliant CSV string of user expenses."""
        stmt = (
            select(Expense)
            .options(selectinload(Expense.category))
            .where(Expense.user_id == user_id)
        )
        if date_from:
            stmt = stmt.where(Expense.expense_date >= date_from)
        if date_to:
            stmt = stmt.where(Expense.expense_date <= date_to)
        if category_id:
            stmt = stmt.where(Expense.category_id == category_id)

        stmt = stmt.order_by(Expense.expense_date.desc(), Expense.created_at.desc())
        result = await self.session.execute(stmt)
        expenses = result.scalars().all()

        output = io.StringIO()
        writer = csv.writer(output, quoting=csv.QUOTE_MINIMAL)
        
        # Header Row
        writer.writerow(["Expense ID", "Date", "Title", "Category", "Amount (INR)", "Payment Mode", "Notes"])

        for exp in expenses:
            category_name = exp.category.name if exp.category else "Uncategorized"
            payment_mode = exp.payment_mode if exp.payment_mode else "Not Specified"
            notes = exp.notes or ""
            writer.writerow([
                str(exp.id),
                exp.expense_date.isoformat(),
                exp.title,
                category_name,
                f"{exp.amount:.2f}",
                payment_mode,
                notes,
            ])

        return output.getvalue()

    async def generate_incomes_csv(
        self,
        user_id: uuid.UUID,
        date_from: Optional[date] = None,
        date_to: Optional[date] = None,
        source: Optional[str] = None,
    ) -> str:
        """Generates an RFC 4180 compliant CSV string of user incomes."""
        stmt = select(Income).where(Income.user_id == user_id)
        if date_from:
            stmt = stmt.where(Income.income_date >= date_from)
        if date_to:
            stmt = stmt.where(Income.income_date <= date_to)
        if source:
            stmt = stmt.where(Income.source == source)

        stmt = stmt.order_by(Income.income_date.desc(), Income.created_at.desc())
        result = await self.session.execute(stmt)
        incomes = result.scalars().all()

        output = io.StringIO()
        writer = csv.writer(output, quoting=csv.QUOTE_MINIMAL)

        # Header Row
        writer.writerow(["Income ID", "Date", "Title", "Source", "Amount (INR)", "Notes"])

        for inc in incomes:
            notes = inc.notes or ""
            writer.writerow([
                str(inc.id),
                inc.income_date.isoformat(),
                inc.title,
                inc.source,
                f"{inc.amount:.2f}",
                notes,
            ])

        return output.getvalue()

    async def generate_statement_summary(
        self,
        user_id: uuid.UUID,
        period_month: str,  # Format: YYYY-MM
    ) -> Dict[str, Any]:
        """Generates a structured statement summary for a specific month."""
        from datetime import datetime
        import calendar

        year, month = map(int, period_month.split("-"))
        _, last_day = calendar.monthrange(year, month)
        start_date = date(year, month, 1)
        end_date = date(year, month, last_day)

        # Query Expenses
        exp_stmt = (
            select(Expense)
            .options(selectinload(Expense.category))
            .where(
                Expense.user_id == user_id,
                Expense.expense_date >= start_date,
                Expense.expense_date <= end_date,
            )
            .order_by(Expense.expense_date.asc())
        )
        exp_result = await self.session.execute(exp_stmt)
        expenses = exp_result.scalars().all()

        # Query Incomes
        inc_stmt = (
            select(Income)
            .where(
                Income.user_id == user_id,
                Income.income_date >= start_date,
                Income.income_date <= end_date,
            )
            .order_by(Income.income_date.asc())
        )
        inc_result = await self.session.execute(inc_stmt)
        incomes = inc_result.scalars().all()

        total_spent = sum((e.amount for e in expenses), Decimal("0.00"))
        total_income = sum((i.amount for i in incomes), Decimal("0.00"))
        net_cash_flow = total_income - total_spent
        savings_rate = (
            round(float((net_cash_flow / total_income) * 100), 2)
            if total_income > 0
            else 0.0
        )

        category_breakdown: Dict[str, Decimal] = {}
        for e in expenses:
            cname = e.category.name if e.category else "Uncategorized"
            category_breakdown[cname] = category_breakdown.get(cname, Decimal("0.00")) + e.amount

        return {
            "period_month": period_month,
            "period_start": start_date.isoformat(),
            "period_end": end_date.isoformat(),
            "total_income": float(total_income),
            "total_spent": float(total_spent),
            "net_cash_flow": float(net_cash_flow),
            "savings_rate_pct": savings_rate,
            "expense_count": len(expenses),
            "income_count": len(incomes),
            "category_allocations": [
                {"category": k, "amount": float(v), "percentage": round(float(v / total_spent * 100), 2) if total_spent > 0 else 0.0}
                for k, v in sorted(category_breakdown.items(), key=lambda x: x[1], reverse=True)
            ],
        }
