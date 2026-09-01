"""
Expense ORM model.

Table: expenses
- UUID primary key
- title: max 50 chars, required
- category_id: FK → categories.id, NOT NULL (every expense must have a category)
- amount: NUMERIC(12,2), CHECK > 0 (no zero or negative amounts)
- expense_date: DATE, CHECK <= CURRENT_DATE (no future dates)
- notes: TEXT, nullable
- payment_mode: VARCHAR(20), nullable — allowed values defined in PaymentMode enum

DB indexes on expense_date and category_id for dashboard query performance.
"""
import uuid
from datetime import date
from decimal import Decimal
from enum import Enum

from sqlalchemy import (
    CheckConstraint,
    Date,
    ForeignKey,
    Index,
    Numeric,
    String,
    Text,
    text,
)
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base import Base, TimestampMixin


class PaymentMode(str, Enum):
    """
    Closed set of payment modes (SRS Section 5.2.1).
    Stored as VARCHAR(20) in the DB; validated at the API layer via Pydantic.
    """
    CASH = "Cash"
    CARD = "Card"
    UPI = "UPI"
    NET_BANKING = "Net Banking"
    OTHER = "Other"


class Expense(Base, TimestampMixin):
    __tablename__ = "expenses"

    __table_args__ = (
        # Business rule: amount must be > 0
        CheckConstraint("amount > 0", name="ck_expenses_amount_positive"),
        # Business rule: no future expense dates
        CheckConstraint(
            "expense_date <= CURRENT_DATE",
            name="ck_expenses_date_not_future",
        ),
        # Performance indexes for dashboard queries
        Index("ix_expenses_expense_date", "expense_date"),
        Index("ix_expenses_category_id", "category_id"),
        Index("ix_expenses_user_id_date", "user_id", "expense_date"),
        Index("ix_expenses_user_category", "user_id", "category_id"),
        # Full-text search support on title (case-insensitive prefix search via LIKE)
        # A GIN/GIST index for full-text would be added in a later migration if needed
        Index("ix_expenses_title", "title"),
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
    title: Mapped[str] = mapped_column(String(50), nullable=False)
    category_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("categories.id", ondelete="RESTRICT"),
        nullable=False,
        index=False,  # covered by ix_expenses_category_id above
    )
    amount: Mapped[Decimal] = mapped_column(Numeric(12, 2), nullable=False)
    expense_date: Mapped[date] = mapped_column(Date, nullable=False)
    notes: Mapped[str | None] = mapped_column(Text, nullable=True)
    payment_mode: Mapped[str | None] = mapped_column(String(20), nullable=True)

    # ── Relationships ─────────────────────────────────────────────────────────
    category: Mapped["Category"] = relationship(  # noqa: F821
        "Category",
        back_populates="expenses",
        lazy="select",
    )
    user: Mapped["User"] = relationship(  # noqa: F821
        "User",
        back_populates="expenses",
        lazy="select",
    )

    def __repr__(self) -> str:
        return (
            f"<Expense id={self.id} title={self.title!r} "
            f"amount={self.amount} date={self.expense_date}>"
        )
