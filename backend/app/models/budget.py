"""
Budget ORM model.

Table: budgets
- UUID primary key
- scope: 'overall' or 'category' (enforced via CHECK constraint)
- category_id: NULL for overall budgets; required for category budgets
- amount: NUMERIC(12,2), CHECK > 0
- period_month: DATE stored as first-of-month (e.g. 2026-08-01)

Unique constraints (partial indexes):
  - One 'overall' budget per period_month
  - One 'category' budget per (category_id, period_month)

These are implemented as partial unique indexes to handle the conditional
uniqueness (scope-dependent), which cannot be expressed as a simple
UniqueConstraint across all rows.

Budget remaining formula (computed at query time, never stored):
    Remaining = Budget.amount − SUM(expenses.amount WHERE expense_date in period_month)
"""
import uuid
from datetime import date
from decimal import Decimal

from sqlalchemy import (
    CheckConstraint,
    Date,
    ForeignKey,
    Index,
    Numeric,
    String,
    text,
)
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base import Base, TimestampMixin


class Budget(Base, TimestampMixin):
    __tablename__ = "budgets"

    __table_args__ = (
        # Scope must be one of two values
        CheckConstraint(
            "scope IN ('overall', 'category')",
            name="ck_budgets_scope_valid",
        ),
        # Amount must be positive
        CheckConstraint("amount > 0", name="ck_budgets_amount_positive"),
        # One overall budget per period_month (partial unique index)
        Index(
            "uix_budgets_overall_period",
            "period_month",
            unique=True,
            postgresql_where=text("scope = 'overall'"),
        ),
        # One category budget per (category_id, period_month) (partial unique index)
        Index(
            "uix_budgets_category_period",
            "category_id",
            "period_month",
            unique=True,
            postgresql_where=text("scope = 'category'"),
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        primary_key=True,
        server_default=text("gen_random_uuid()"),
    )
    scope: Mapped[str] = mapped_column(String(10), nullable=False)
    category_id: Mapped[uuid.UUID | None] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("categories.id", ondelete="CASCADE"),
        nullable=True,
    )
    amount: Mapped[Decimal] = mapped_column(Numeric(12, 2), nullable=False)
    # Stored as first-of-month: always YYYY-MM-01
    period_month: Mapped[date] = mapped_column(Date, nullable=False)

    # ── Relationships ─────────────────────────────────────────────────────────
    category: Mapped["Category | None"] = relationship(  # noqa: F821
        "Category",
        back_populates="budgets",
        lazy="select",
    )

    def __repr__(self) -> str:
        return (
            f"<Budget id={self.id} scope={self.scope!r} "
            f"period={self.period_month} amount={self.amount}>"
        )
