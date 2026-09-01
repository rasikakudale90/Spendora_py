"""
Budget ORM model.

Table: budgets
- UUID primary key
- scope: 'overall' or 'category' (enforced via CHECK constraint)
- category_id: NULL for overall budgets; required for category budgets
- amount: NUMERIC(12,2), CHECK > 0
- period_type: 'daily', 'weekly', 'monthly', or 'yearly' (default: 'monthly')
- period_start: DATE stored as start of the period (e.g. Monday for week, 1st for month, Jan 1 for year)
- period_end: DATE stored as end of the period
- period_month: DATE stored as first-of-month (maintained for backward compatibility)

Unique constraints (partial indexes):
  - One 'overall' budget per (period_type, period_start)
  - One 'category' budget per (category_id, period_type, period_start)
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
        # Period type must be one of four values
        CheckConstraint(
            "period_type IN ('daily', 'weekly', 'monthly', 'yearly')",
            name="ck_budgets_period_type_valid",
        ),
        # Amount must be positive
        CheckConstraint("amount > 0", name="ck_budgets_amount_positive"),
        # One overall budget per user per (period_type, period_start) (partial unique index)
        Index(
            "uix_budgets_user_overall_period_type_start",
            "user_id",
            "period_type",
            "period_start",
            unique=True,
            postgresql_where=text("scope = 'overall'"),
        ),
        # One category budget per user per (category_id, period_type, period_start) (partial unique index)
        Index(
            "uix_budgets_user_category_period_type_start",
            "user_id",
            "category_id",
            "period_type",
            "period_start",
            unique=True,
            postgresql_where=text("scope = 'category'"),
        ),
    )

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        primary_key=True,
        server_default=text("gen_random_uuid()"),
    )
    user_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
    )
    scope: Mapped[str] = mapped_column(String(10), nullable=False)
    category_id: Mapped[uuid.UUID | None] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("categories.id", ondelete="CASCADE"),
        nullable=True,
    )
    amount: Mapped[Decimal] = mapped_column(Numeric(12, 2), nullable=False)
    period_type: Mapped[str] = mapped_column(String(10), default="monthly", server_default="monthly", nullable=False)
    period_start: Mapped[date] = mapped_column(Date, nullable=False)
    period_end: Mapped[date] = mapped_column(Date, nullable=False)
    period_month: Mapped[date] = mapped_column(Date, nullable=False)

    # ── Relationships ─────────────────────────────────────────────────────────
    category: Mapped["Category | None"] = relationship(  # noqa: F821
        "Category",
        back_populates="budgets",
        lazy="select",
    )
    user: Mapped["User"] = relationship(  # noqa: F821
        "User",
        back_populates="budgets",
        lazy="select",
    )

    def __repr__(self) -> str:
        return (
            f"<Budget id={self.id} scope={self.scope!r} period_type={self.period_type!r} "
            f"period_start={self.period_start} amount={self.amount}>"
        )
